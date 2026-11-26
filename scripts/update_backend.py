#!/usr/bin/env python3
"""Poll Docker Hub for stable releases and update the Compose backend and frontend independently."""

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
SERVICES = {
    "backend": {"repository": REPOSITORY, "variable": "BACKEND_TAG"},
    "frontend": {"repository": "ensarsmajli/own-webshop-frontend", "variable": "FRONTEND_TAG"},
}
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
        raise ValueError("The current image tag must be a stable version such as 1.0.0")
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


def published_tags(repository=REPOSITORY):
    query = urlencode({"service": "registry.docker.io", "scope": f"repository:{repository}:pull"})
    credentials = docker_credentials()
    authorization = None
    if credentials:
        encoded = base64.b64encode(":".join(credentials).encode()).decode()
        authorization = "Basic " + encoded
    payload, _ = request_json("https://auth.docker.io/token?" + query, authorization)
    token = payload.get("token") or payload.get("access_token")
    if not token:
        raise RuntimeError("Docker Hub did not return an access token")
    url = f"{REGISTRY}/v2/{repository}/tags/list?n=100"
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


def compose(*arguments, tag=None, service="backend"):
    environment = os.environ.copy()
    for settings in SERVICES.values():
        environment.pop(settings["variable"], None)
    if tag is not None:
        environment[SERVICES[service]["variable"]] = tag
    result = subprocess.run(
        ["docker", "compose", "--project-directory", str(PROJECT),
         "--env-file", str(PROJECT / ".env.compose"), "-f", str(PROJECT / "compose.yaml"),
         *arguments], env=environment, text=True, capture_output=True, timeout=600,
    )
    if result.returncode:
        # Compose config contains database credentials; don't log captured output.
        raise RuntimeError("Docker Compose command failed: " + arguments[0])
    return result.stdout


def configured_version(service="backend"):
    config = json.loads(compose("config", "--format", "json"))
    image = config["services"][service]["image"]
    repository = SERVICES[service]["repository"]
    prefix = repository + ":"
    if not image.startswith(prefix) or version_key(image[len(prefix):]) is None:
        raise ValueError(f"Compose {service} image must use a stable version of {repository}")
    return image[len(prefix):]


def verify_running_version(current, service="backend"):
    container = compose("ps", "-q", service).strip()
    if not container:
        raise RuntimeError(f"{service} is not running; start Compose first")
    result = subprocess.run(
        ["docker", "inspect", "--format", "{{.Config.Image}}", container],
        text=True, capture_output=True, timeout=15, check=True,
    )
    if result.stdout.strip() != SERVICES[service]["repository"] + ":" + current:
        raise RuntimeError(f"Running {service} and {SERVICES[service]['variable']} differ; reconcile them before updating")


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


def wait_for_frontend(timeout=300):
    """Check the React page AND its connection to the real backend."""
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            with urlopen("http://127.0.0.1:8082/", timeout=5) as response:
                page_ready = response.status == 200 and b'id="root"' in response.read(1024 * 1024)
            with urlopen("http://127.0.0.1:8082/api/markets", timeout=5) as response:
                api_ready = response.status == 200 and isinstance(json.load(response), list)
            if page_ready and api_ready:
                return
        except (OSError, ValueError):
            pass
        time.sleep(5)
    raise RuntimeError("Frontend or its API proxy did not become ready within 300 seconds")


def save_version(tag, service="backend"):
    """Atomically update one service version, preserving every other setting."""
    if version_key(tag) is None:
        raise ValueError("Refusing to save an invalid stable version")
    variable = SERVICES[service]["variable"]
    path = PROJECT / ".env.compose"
    content = path.read_text()
    pattern = re.compile(r"^" + variable + r"=.*$", re.MULTILINE)
    if len(pattern.findall(content)) != 1:
        raise ValueError(f".env.compose must contain exactly one {variable}= line")
    content = pattern.sub(variable + "=" + tag, content)
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


def deploy(current, target, service="backend"):
    LOG.info("Downloading %s %s (current: %s)", service, target, current)
    compose("pull", service, tag=target, service=service)
    readiness = wait_for_backend if service == "backend" else wait_for_frontend
    try:
        compose("up", "-d", "--no-deps", service, tag=target, service=service)
        readiness()
        save_version(target, service=service)
    except Exception:
        LOG.error("Update failed; attempting to restore %s %s", service, current)
        compose("up", "-d", "--no-deps", service, tag=current, service=service)
        readiness()
        raise
    LOG.info("%s %s is ready; .env.compose updated", service, target)


def check_once(check_only=False, service="backend"):
    current = configured_version(service=service)
    verify_running_version(current, service=service)
    target = newest_version(published_tags(SERVICES[service]["repository"]), current)
    if target:
        LOG.info("New stable %s version found: %s -> %s", service, current, target)
        if not check_only:
            deploy(current, target, service=service)
    else:
        LOG.info("No higher stable %s version available (current: %s)", service, current)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Check once without changing containers or files")
    parser.add_argument("--once", action="store_true", help="Check once and deploy a newer release if present")
    parser.add_argument("--service", choices=["all", *SERVICES], default="all",
                        help="Check both services by default, or select one")
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    # A manual invocation and the service must never deploy concurrently.
    with (PROJECT / ".relin-updater.lock").open("a") as lock:
        try:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            LOG.error("Another updater is already running")
            return 1
        selected = list(SERVICES) if args.service == "all" else [args.service]
        while True:
            failed = False
            for service in selected:
                try:
                    check_once(args.check, service=service)
                except Exception as error:
                    # Never log response bodies, Docker output or credentials.
                    detail = f"HTTP {error.code}" if isinstance(error, HTTPError) else type(error).__name__
                    if isinstance(error, RuntimeError):
                        detail += ": " + str(error)
                    LOG.error("%s check failed (%s). Check docker login, network and service logs.", service, detail)
                    failed = True
            if args.once or args.check:
                return int(failed)
            time.sleep(10)


if __name__ == "__main__":
    raise SystemExit(main())
