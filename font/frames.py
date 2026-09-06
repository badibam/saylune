"""Cut the mockup's frame into glyphs.

The bench (`tmp/pixel-bench.html`) paints a frame as a rounded rectangle: a
border four pixels thick, corners of radius four, the two outer pixels in the
light tone and the two inner ones in the dark one. The same distance is
computed here on a rectangle of whole cells, and the cells are then read back as
glyphs — so a frame written as text is the drawing the mockup settled on, not a
copy of it made by eye.

Eight pieces, each in two layers, at U+E000. A frame is written twice, once per
layer, in two decor colours exactly on top of each other.
"""

import math

from pixelfont import CELL_W, GLYPHS, read_maps, cells_to_rows, write_maps

THICKNESS = 4
RADIUS = 4
TILE = 11              # a frame row is eleven pixels high, unlike a text line
TOP_ROW = 10           # ink sits on rows 10..0, so frame rows stack without a gap

PIECES = ["topleft", "top", "topright", "left", "right", "bottomleft", "bottom", "bottomright"]
FIRST = 0xE000


def depth_map(cols, rows):
    """{(x, y): depth} of the border pixels of a rectangle, in tile pixels."""
    x1, y1 = cols * TILE - 1, rows * TILE - 1
    out = {}
    for y in range(y1 + 1):
        for x in range(x1 + 1):
            ddx = max(RADIUS - x, 0, x - (x1 - RADIUS))
            ddy = max(RADIUS - y, 0, y - (y1 - RADIUS))
            if ddx > 0 and ddy > 0:
                d = RADIUS - math.hypot(ddx, ddy)
            else:
                d = min(x, x1 - x, y, y1 - y)
            if 0 <= d < THICKNESS:
                out[(x, y)] = int(math.floor(d))
    return out


def cut(depths, col, row, layer):
    """The pixels of one tile, kept for the light layer (0) or the dark one (1)."""
    half = math.ceil(THICKNESS / 2)
    cells = set()
    for i in range(TILE):
        for j in range(TILE):
            d = depths.get((col * TILE + i, row * TILE + j))
            if d is None or (d < half) != (layer == 0):
                continue
            cells.add((i, TOP_ROW - j))
    return cells


def main():
    # five by five: wide enough that the middle tiles are pure edges
    depths = depth_map(5, 5)
    spots = {"topleft": (0, 0), "top": (2, 0), "topright": (4, 0), "left": (0, 2),
             "right": (4, 2), "bottomleft": (0, 4), "bottom": (2, 4), "bottomright": (4, 4)}

    # the edges must repeat, or a frame written as text would not join up
    for name, (col, row) in (("top", (1, 0)), ("bottom", (1, 4)), ("left", (0, 1)), ("right", (4, 1))):
        for layer in (0, 1):
            assert cut(depths, col, row, layer) == cut(depths, *spots[name], layer), name

    for weight in ("regular", "thin"):
        path = GLYPHS / f"{weight}.txt"
        maps = read_maps(path)
        for layer in (0, 1):
            for i, name in enumerate(PIECES):
                cells = cut(depths, *spots[name], layer)
                cp = FIRST + layer * len(PIECES) + i
                maps[f"frame.{name}.{'light' if layer == 0 else 'dark'}"] = (cp, cells_to_rows(cells))
        header = path.read_text(encoding="utf-8").split("\n@")[0]
        write_maps(path, maps, header)
        print(f"{path.name}: 16 pieces de cadre, {len(maps)} glyphes en tout")


if __name__ == "__main__":
    main()
