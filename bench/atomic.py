"""Write a durable file whole or not at all.

A run killed mid-write otherwise leaves a truncated file, and a truncated file
is worse than a missing one -- nothing detects it as absent, so it raises in the
middle of the next run, or worse reads as a shorter recording. Being regenerable
does not exempt anything: the cost of regenerating is paid only by whoever
notices, and a truncated file is precisely what nobody notices.

The exemption is for what is read without being believed -- a log, a report a
human looks at. Those keep writing in place.
"""

from contextlib import contextmanager
from pathlib import Path


@contextmanager
def opened(path, mode="wb", encoding=None):
    """A handle on a neighbouring temporary, renamed onto `path` on the way out.

    Through a handle rather than a name, because `numpy.savez` appends `.npz` to
    any name that does not already end in it -- the temporary would land beside
    the file instead of becoming it.
    """
    if "b" not in mode and encoding is None:
        encoding = "utf-8"  # never the platform default, which varies by locale
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    part = path.with_name(path.name + ".part")
    try:
        with open(part, mode, encoding=encoding) as handle:
            yield handle
        part.replace(path)
    finally:
        part.unlink(missing_ok=True)


def write_text(path, text, encoding="utf-8"):
    with opened(path, "w", encoding=encoding) as handle:
        handle.write(text)


def write_bytes(path, data):
    with opened(path) as handle:
        handle.write(data)
