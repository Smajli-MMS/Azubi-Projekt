#!/usr/bin/env python3
"""Poll Docker Hub for stable releases and update only the Compose backend."""

import argparse
import base64
import fcntl
import json
import logging
import os
from pathlib import Path
import re
import subprocess
import tempfile
import time
from urllib.error import HTTPError
from urllib.parse import urlencode, urljoin, urlsplit
from urllib.request import Request, urlopen

REPOSITORY = "ensarsmajli/azubi-projekt"
REGISTRY = "https://registry-1.docker.io"
PROJECT = Path(__file__).resolve().parents[1]
VERSION = re.compile(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", re.ASCII)
LOG = logging.getLogger("relin-updater")


def version_key(tag):
    """Match the same stable-release policy as ci.yml."""
    if not isinstance(tag, str) or len(tag) > 128 or not VERSION.fullmatch(tag):
        return None
    return tuple(map(int, tag.split(".")))


def newest_version(tags, current):
    current_key = version_key(current)
    if current_key is None:
        raise ValueError("BACKEND_TAG must be a stable version such as 1.0.0")
    candidates = [tag for tag in tags if version_key(tag) is not None]
    newest = max(candidates, key=version_key, default=current)
    return newest if version_key(newest) > current_key else None


def docker_credentials():
    """Read docker login credentials without printing them or passing CLI secrets."""
    directory = Path(os.environ.get("DOCKER_CONFIG", Path.home() / ".docker"))
    path = directory / "config.json"
    config = json.loads(path.read_text()) if path.exists() else {}
    keys = ("https://index.docker.io/v1/", "registry-1.docker.io", "docker.io")
    helpers = config.get("credHelpers", {})
    helper_key = next((key for key in keys if key in helpers), keys[0])
    helper = helpers.get(helper_key) or config.get("credsStore")
    if helper:
        result = subprocess.run(
            ["docker-credential-" + helper, "get"], input=helper_key + "\n",
            text=True, capture_output=True, timeout=15,
        )
        if result.returncode:
            raise RuntimeError("Docker credential helper failed; run docker login as the service user")
        secret = json.loads(result.stdout)
        return secret["Username"], secret["Secret"]
    auths = config.get("auths", {})
    for key in keys:
        auth = auths.get(key, {}).get("auth")
        if auth:
            return tuple(base64.b64decode(auth).decode().split(":", 1))
    return None


def request_json(url, authorization=None):
    headers = {"Accept": "application/json", "User-Agent": "relin-updater/1"}
    if authorization:
        headers["Authorization"] = authorization
    with urlopen(Request(url, headers=headers), timeout=20) as response:
        return json.load(response), response.headers.get("Link", "")


def published_tags():
    query = urlencode({"service": "registry.docker.io", "scope": f"repository:{REPOSITORY}:pull"})
    credentials = docker_credentials()
    authorization = None
    if credentials:
        encoded = base64.b64encode(":".join(credentials).encode()).decode()
        authorization = "Basic " + encoded
    payload, _ = request_json("https://auth.docker.io/token?" + query, authorization)
    token = payload.get("token") or payload.get("access_token")
    if not token:
        raise RuntimeError("Docker Hub did not return an access token")
    url = f"{REGISTRY}/v2/{REPOSITORY}/tags/list?n=100"
    tags, visited = [], set()
    while url:
        if url in visited or len(visited) >= 1000:
            raise RuntimeError("Invalid or excessive registry pagination")
        visited.add(url)
        payload, link = request_json(url, "Bearer " + token)
        tags.extend(payload.get("tags") or [])
        next_link = re.search(r'<([^>]+)>;\s*rel="next"', link)
        url = urljoin(url, next_link.group(1)) if next_link else None
        # Never forward the bearer token to a host supplied by a pagination link.
        if url and (urlsplit(url).scheme != "https" or urlsplit(url).netloc != "registry-1.docker.io"):
            raise RuntimeError("Untrusted registry pagination URL")
    return tags


def compose(*arguments, tag=None):
    environment = os.environ.copy()
    environment.pop("BACKEND_TAG", None)
    if tag is not None:
        environment["BACKEND_TAG"] = tag
    result = subprocess.run(
        ["docker", "compose", "--project-directory", str(PROJECT),
         "--env-file", str(PROJECT / ".env.compose"), "-f", str(PROJECT / "compose.yaml"),
         *arguments], env=environment, text=True, capture_output=True, timeout=600,
    )
    if result.returncode:
        # Compose config contains database credentials; don't log captured output.
        raise RuntimeError("Docker Compose command failed: " + arguments[0])
    return result.stdout


def configured_version():
    config = json.loads(compose("config", "--format", "json"))
    image = config["services"]["backend"]["image"]
    prefix = REPOSITORY + ":"
    if not image.startswith(prefix) or version_key(image[len(prefix):]) is None:
        raise ValueError("Compose backend image must use a stable version of " + REPOSITORY)
    return image[len(prefix):]


def verify_running_version(current):
    container = compose("ps", "-q", "backend").strip()
    if not container:
        raise RuntimeError("Backend is not running; start Compose first")
    result = subprocess.run(
        ["docker", "inspect", "--format", "{{.Config.Image}}", container],
        text=True, capture_output=True, timeout=15, check=True,
    )
    if result.stdout.strip() != REPOSITORY + ":" + current:
        raise RuntimeError("Running backend and BACKEND_TAG differ; reconcile them before updating")


def wait_for_backend(timeout=300):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            with urlopen("http://127.0.0.1:8080/api/markets", timeout=5) as response:
                if response.status == 200 and isinstance(json.load(response), list):
                    return
        except (OSError, ValueError):
            pass
        time.sleep(5)
    raise RuntimeError("Backend did not become ready within 300 seconds")


def save_version(tag):
    """Atomically update only BACKEND_TAG; preserve password and all other lines."""
    path = PROJECT / ".env.compose"
    content = path.read_text()
    pattern = re.compile(r"^BACKEND_TAG=.*$", re.MULTILINE)
    if len(pattern.findall(content)) != 1:
        raise ValueError(".env.compose must contain exactly one BACKEND_TAG= line")
    content = pattern.sub("BACKEND_TAG=" + tag, content)
    descriptor, temporary = tempfile.mkstemp(prefix=".env.compose-", dir=PROJECT)
    try:
        with os.fdopen(descriptor, "w") as output:
            output.write(content)
            output.flush()
            os.fsync(output.fileno())
        os.replace(temporary, path)  # mkstemp creates a private file (0600).
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


def deploy(current, target):
    LOG.info("Downloading backend %s (current: %s)", target, current)
    compose("pull", "backend", tag=target)  # Keep the running container if pull fails.
    try:
        compose("up", "-d", "--no-deps", "backend", tag=target)
        wait_for_backend()
        save_version(target)
    except Exception:
        LOG.error("Update failed; attempting to restore backend %s", current)
        compose("up", "-d", "--no-deps", "backend", tag=current)
        wait_for_backend()
        raise
    LOG.info("Backend %s is ready; .env.compose updated", target)


def check_once(check_only=False):
    current = configured_version()
    verify_running_version(current)
    target = newest_version(published_tags(), current)
    if target:
        LOG.info("New stable version found: %s -> %s", current, target)
        if not check_only:
            deploy(current, target)
    else:
        LOG.info("No higher stable version available (current: %s)", current)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Check once without changing containers or files")
    parser.add_argument("--once", action="store_true", help="Check once and deploy a newer release if present")
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    # A manual invocation and the service must never deploy concurrently.
    with (PROJECT / ".relin-updater.lock").open("a") as lock:
        try:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            LOG.error("Another updater is already running")
            return 1
        while True:
            try:
                check_once(args.check)
            except Exception as error:
                # Do not log HTTP bodies, Docker output, credentials or bearer tokens.
                detail = f"HTTP {error.code}" if isinstance(error, HTTPError) else type(error).__name__
                LOG.error("Update check failed (%s). Check docker login, network and backend logs.", detail)
                if args.once or args.check:
                    return 1
            if args.once or args.check:
                return 0
            time.sleep(10)


if __name__ == "__main__":
    raise SystemExit(main())
