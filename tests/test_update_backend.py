import base64
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch, Mock, call

SPEC = importlib.util.spec_from_file_location(
    "updater", Path(__file__).resolve().parents[1] / "scripts/update_backend.py")
updater = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(updater)


class UpdaterTests(unittest.TestCase):
    def test_numeric_order_and_only_higher_versions(self):
        self.assertEqual(updater.newest_version(["1.9.0", "1.10.0", "1.2.99"], "1.9.0"), "1.10.0")
        self.assertIsNone(updater.newest_version(["1.0.0", "1.0.1"], "1.0.1"))
        self.assertIsNone(updater.newest_version([], "1.0.0"))

    def test_release_policy_matches_workflow(self):
        for tag in ["latest", "01.0.0", "v1.0.0", "1.0", "1.0.0-beta.1", "1.0.0+build", "1.0.0\n", "9" * 129 + ".0.0"]:
            with self.subTest(tag=tag):
                self.assertIsNone(updater.version_key(tag))
        self.assertEqual(updater.newest_version(["latest", "2.0.0-beta.1", "1.0.1"], "1.0.0"), "1.0.1")
        with self.assertRaises(ValueError):
            updater.newest_version(["1.0.0"], "latest")

    def test_registry_pagination_even_with_short_page(self):
        with patch.object(updater, "docker_credentials", return_value=None), patch.object(updater, "request_json") as request:
            request.side_effect = [
                ({"token": "secret"}, ""),
                ({"tags": ["1.0.0"]}, '</v2/ensarsmajli/azubi-projekt/tags/list?n=100&last=1.0.0>; rel="next"'),
                ({"tags": ["1.10.0"]}, ""),
            ]
            self.assertEqual(updater.published_tags(), ["1.0.0", "1.10.0"])
            self.assertIn("last=1.0.0", request.call_args.args[0])

    def test_registry_never_sends_token_to_another_host(self):
        with patch.object(updater, "docker_credentials", return_value=None), patch.object(updater, "request_json") as request:
            request.side_effect = [({"token": "secret"}, ""), ({"tags": []}, '<https://other.invalid/>; rel="next"')]
            with self.assertRaises(RuntimeError):
                updater.published_tags()
            self.assertEqual(request.call_count, 2)

    def test_docker_login_credentials(self):
        with tempfile.TemporaryDirectory() as directory:
            auth = base64.b64encode(b"user:token").decode()
            Path(directory, "config.json").write_text(json.dumps({"auths": {"https://index.docker.io/v1/": {"auth": auth}}}))
            with patch.dict(updater.os.environ, {"DOCKER_CONFIG": directory}):
                self.assertEqual(updater.docker_credentials(), ("user", "token"))

    def test_credential_helper(self):
        with tempfile.TemporaryDirectory() as directory:
            Path(directory, "config.json").write_text('{"credsStore":"pass"}')
            with patch.dict(updater.os.environ, {"DOCKER_CONFIG": directory}), patch.object(updater.subprocess, "run") as run:
                run.return_value = Mock(returncode=0, stdout='{"Username":"user","Secret":"token"}')
                self.assertEqual(updater.docker_credentials(), ("user", "token"))
                self.assertEqual(run.call_args.args[0], ["docker-credential-pass", "get"])

    def test_persist_only_version_and_keep_file_private(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(updater, "PROJECT", Path(directory)):
            path = Path(directory, ".env.compose")
            path.write_text("# Settings\nPOSTGRES_PASSWORD='secret$123'\nBACKEND_TAG=1.0.0\n")
            updater.save_version("1.0.1")
            self.assertEqual(path.read_text(), "# Settings\nPOSTGRES_PASSWORD='secret$123'\nBACKEND_TAG=1.0.1\n")
            self.assertEqual(path.stat().st_mode & 0o777, 0o600)

    def test_duplicate_setting_is_rejected_without_modification(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(updater, "PROJECT", Path(directory)):
            path = Path(directory, ".env.compose")
            original = "BACKEND_TAG=1.0.0\nBACKEND_TAG=1.0.1\n"
            path.write_text(original)
            with self.assertRaises(ValueError):
                updater.save_version("1.0.2")
            self.assertEqual(path.read_text(), original)

    def test_success_pulls_then_recreates_then_checks_then_saves(self):
        with patch.object(updater, "compose") as compose, patch.object(updater, "wait_for_backend") as ready, patch.object(updater, "save_version") as save:
            sequence = Mock()
            sequence.attach_mock(compose, "compose")
            sequence.attach_mock(ready, "ready")
            sequence.attach_mock(save, "save")
            updater.deploy("1.0.0", "1.0.1")
            self.assertEqual(sequence.mock_calls, [call.compose("pull", "backend", tag="1.0.1"), call.compose("up", "-d", "--no-deps", "backend", tag="1.0.1"), call.ready(), call.save("1.0.1")])

    def test_failed_pull_does_not_recreate_or_save(self):
        with patch.object(updater, "compose", side_effect=RuntimeError("pull failed")) as compose, patch.object(updater, "save_version") as save:
            with self.assertRaises(RuntimeError):
                updater.deploy("1.0.0", "1.0.1")
            self.assertEqual(compose.call_count, 1)
            save.assert_not_called()

    def test_failed_start_restores_old_backend_without_saving_version(self):
        with patch.object(updater, "compose") as compose, patch.object(updater, "wait_for_backend", side_effect=[RuntimeError("not ready"), None]), patch.object(updater, "save_version") as save:
            with self.assertRaises(RuntimeError):
                updater.deploy("1.0.0", "1.0.1")
            self.assertEqual(compose.call_args, call("up", "-d", "--no-deps", "backend", tag="1.0.0"))
            save.assert_not_called()

    def test_check_mode_never_deploys(self):
        with patch.object(updater, "configured_version", return_value="1.0.0"), patch.object(updater, "verify_running_version"), patch.object(updater, "published_tags", return_value=["1.0.1"]), patch.object(updater, "deploy") as deploy:
            updater.check_once(check_only=True)
            deploy.assert_not_called()

    def test_running_version_mismatch_blocks_update(self):
        with patch.object(updater, "compose", return_value="container-id"), patch.object(updater.subprocess, "run", return_value=Mock(stdout="ensarsmajli/azubi-projekt:2.0.0\n")):
            with self.assertRaises(RuntimeError):
                updater.verify_running_version("1.0.0")


if __name__ == "__main__":
    unittest.main()
