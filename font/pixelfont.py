"""Pixel-map source of truth for the app font.

A glyph is an 11x14 grid of '#' and '.', rows written from y=11 down to y=-2.
Rows 0 to 9 hold the capitals, 10 and 11 the accents above them, -1 and -2 the
descenders. Column 10 is the gutter between two letters and stays empty.

One drawing pixel is 64 font units; the em is 1024, the advance 704.
"""

from pathlib import Path

UPEM = 1024
PX = 64
CELL_W = 11          # advance, in drawing pixels
GLYPH_W = 10         # ink columns
TOP_PX = 11          # highest ink row: the accent of a capital
BOTTOM_PX = -2       # lowest ink row: the hook of a descender
ROWS = TOP_PX - BOTTOM_PX + 1
CAP_PX = 10          # capitals fill rows 0..9
X_PX = 8             # lowercase without ascender fills rows 0..7
ASCENT_PX = TOP_PX + 1
DESCENT_PX = -BOTTOM_PX
LINE_PX = ROWS + 1   # one blank row between two lines of text

ROOT = Path(__file__).resolve().parent
GLYPHS = ROOT / "glyphs"


def read_maps(path):
    """Read a pixel-map file into {name: (codepoint|None, rows)}."""
    maps, name, cp, rows = {}, None, None, []
    for raw in Path(path).read_text(encoding="utf-8").splitlines():
        line = raw.rstrip("\n")
        if line.startswith("@"):
            if name is not None:
                maps[name] = (cp, rows)
            head = line[1:].split()
            name = head[0]
            cp = int(head[1][2:], 16) if len(head) > 1 and head[1].startswith("U+") else None
            rows = []
        elif line and set(line) <= {"#", "."}:
            rows.append(line)
    if name is not None:
        maps[name] = (cp, rows)
    return maps


def write_maps(path, maps, header):
    out = [header.rstrip() + "\n"]
    for name, (cp, rows) in maps.items():
        tag = f"@{name}" + (f" U+{cp:04X}" if cp is not None else "")
        out.append(tag + "\n")
        out.extend(r + "\n" for r in rows)
        out.append("\n")
    Path(path).write_text("".join(out), encoding="utf-8")


def rows_to_cells(rows, top=TOP_PX):
    """{(x, y)} of lit pixels, y counted up from the baseline."""
    return {(x, top - i) for i, r in enumerate(rows) for x, c in enumerate(r) if c == "#"}


def cells_to_rows(cells):
    return [
        "".join("#" if (x, TOP_PX - i) in cells else "." for x in range(CELL_W))
        for i in range(ROWS)
    ]
