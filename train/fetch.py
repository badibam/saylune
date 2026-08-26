#!/usr/bin/env python3
"""Bring a Kaggle notebook's checkpoints home, one file at a time and resumably.

`kaggle kernels output` is not usable for a 1.2 GB checkpoint: the transfer dies
on an IncompleteRead, the CLI writes a zero-byte `model.safetensors`, and it
exits 0. A file that size cannot be trusted to arrive in one go, so every
transfer here is resumable and every arrival is checked against the length the
server announced.

The signed URLs the kernel-output API hands out are short-lived, so the list is
re-fetched on every invocation: re-running the script after any interruption
picks up exactly where the bytes stopped.

    train/fetch.py --kernel <owner>/<slug>              # every epoch of that run
    train/fetch.py --kernel <owner>/<slug> --epoch 029  # one snapshot

Authenticates with the OAuth token `kaggle auth login` leaves in
~/.kaggle/access_token, falling back to the API key of ~/.kaggle/kaggle.json.
"""

import argparse
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
API = "https://www.kaggle.com/api/v1/kernels/output"


def credentials():
    """Return the Authorization header value, or fail loudly.

    The API rejects basic auth with `kernels.get` denied when an OAuth token
    exists, so the bearer token is tried first.
    """
    home = Path(os.environ.get("KAGGLE_CONFIG_DIR", Path.home() / ".kaggle"))

    token = home / "access_token"
    if token.is_file():
        value = token.read_text(encoding="utf-8").strip()
        if value:
            return f"Bearer {value}"

    key = home / "kaggle.json"
    if key.is_file():
        payload = json.loads(key.read_text(encoding="utf-8"))
        import base64

        pair = f"{payload['username']}:{payload['key']}".encode("utf-8")
        return "Basic " + base64.b64encode(pair).decode("ascii")

    sys.exit(f"No Kaggle credentials under {home} — run `kaggle auth login`.")


def outputs(kernel, auth):
    """Return [(path, url)] for every file the kernel's last run produced."""
    owner, _, slug = kernel.partition("/")
    query = f"{API}?userName={owner}&kernelSlug={slug}&pageSize=200"
    request = urllib.request.Request(query, headers={"Authorization": auth})
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            payload = json.load(response)
    except urllib.error.HTTPError as error:
        sys.exit(f"Kaggle API refused the listing: {error.code} {error.reason}")

    if "files" not in payload:
        sys.exit(f"Unexpected API payload, keys: {sorted(payload)}")
    return [(f["fileName"], f["url"]) for f in payload["files"] if f.get("url")]


def announced_length(url):
    """Return the byte length the server announces for this URL, or None.

    This is the only trustworthy size: the `fileSize` field of the listing comes
    back null, and `kaggle kernels files` reports three-digit sizes for
    gigabyte-sized weights.

    Asked by a one-byte ranged GET rather than a HEAD, which these signed URLs
    answer with a 404. The total rides in `Content-Range: bytes 0-0/<total>`.
    """
    request = urllib.request.Request(url, headers={"Range": "bytes=0-0"})
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            span = response.headers.get("Content-Range", "")
    except urllib.error.HTTPError:
        return None
    _, _, total = span.rpartition("/")
    return int(total) if total.isdigit() else None


def pull(url, destination, expected, attempts):
    """Fetch one file, resuming a partial local copy, and return whether it is whole."""
    destination.parent.mkdir(parents=True, exist_ok=True)

    for attempt in range(1, attempts + 1):
        have = destination.stat().st_size if destination.exists() else 0
        if expected is not None and have == expected:
            return True
        if expected is not None and have > expected:
            print("  local copy is longer than announced, restarting", flush=True)
            destination.unlink()
            have = 0

        print(
            f"  attempt {attempt}/{attempts}"
            + (f", resuming at {have:,} bytes" if have else ""),
            flush=True,
        )
        subprocess.run(
            [
                "curl", "--location", "--continue-at", "-",
                "--retry", "5", "--retry-all-errors", "--retry-delay", "5",
                "--speed-time", "60", "--speed-limit", "1024",
                "--output", str(destination), url,
            ],
            check=False,
        )

        after = destination.stat().st_size if destination.exists() else 0
        if expected is None:
            # Nothing to check against; a non-empty file is all we can assert.
            return after > 0
        if after == expected:
            return True
        if after == have:
            print("  no progress on this attempt", flush=True)

    return False


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    # Asked for rather than defaulted: a remembered notebook would always name
    # the previous run, and the mistake would look like a finished download.
    parser.add_argument("--kernel", required=True,
                        help="owner/slug of the notebook to fetch from")
    parser.add_argument(
        "--epoch",
        action="append",
        help="epoch number to fetch, repeatable (default: every one found)",
    )
    parser.add_argument(
        "--dest",
        default=str(ROOT),
        help="root the kernel's own paths are recreated under (default: repo root)",
    )
    parser.add_argument(
        "--match",
        default="runs-v3/",
        help="substring a kernel path must contain to be fetched",
    )
    parser.add_argument("--attempts", type=int, default=6)
    args = parser.parse_args()

    auth = credentials()
    files = [(p, u) for p, u in outputs(args.kernel, auth) if args.match in p]
    if args.epoch:
        wanted = tuple(f"epoch-{e.removeprefix('epoch-')}/" for e in args.epoch)
        files = [(p, u) for p, u in files if any(w in p for w in wanted)]
    if not files:
        sys.exit("Nothing to fetch — check --match and --epoch against the listing.")

    dest_root = Path(args.dest).resolve()
    failed = []
    for path, url in sorted(files):
        destination = dest_root / path
        expected = announced_length(url)
        size = f"{expected:,} bytes" if expected else "unknown length"
        print(f"{path} ({size})", flush=True)

        if expected is not None and destination.exists():
            if destination.stat().st_size == expected:
                print("  already whole", flush=True)
                continue

        if not pull(url, destination, expected, args.attempts):
            failed.append(path)
            print("  INCOMPLETE", flush=True)

    print()
    if failed:
        print("Incomplete, re-run to resume:", flush=True)
        for path in failed:
            print(f"  {path}", flush=True)
        sys.exit(1)
    print(f"{len(files)} file(s) whole under {dest_root}.", flush=True)


if __name__ == "__main__":
    main()
