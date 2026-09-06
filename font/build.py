"""Compile the pixel maps into TTF files.

Every lit pixel is a square of 64 units. Squares that touch are welded into one
contour by cancelling the edge they share, so a letter comes out as one outline
with real holes rather than a heap of stacked squares.
"""

import sys
from pathlib import Path

from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen

from pixelfont import ASCENT_PX, CAP_PX, CELL_W, DESCENT_PX, GLYPHS, PX, UPEM, X_PX, read_maps, rows_to_cells

FAMILY = "Speakup Tile"
VERSION = "1.000"
COPYRIGHT = (
    "Derived from Mono10 by Michael Vieth (Community Pack), "
    "licensed under the SIL Open Font License 1.1."
)
WEIGHTS = {"regular": ("Regular", 400), "thin": ("Thin", 250)}


def contours(cells):
    """Trace the outline of a set of pixels. Filled cells run clockwise."""
    edges = {}
    for x, y in cells:
        for a, b in (((x, y), (x, y + 1)), ((x, y + 1), (x + 1, y + 1)),
                     ((x + 1, y + 1), (x + 1, y)), ((x + 1, y), (x, y))):
            if edges.pop((b, a), None) is None:
                edges[(a, b)] = True

    starts = {}
    for a, b in edges:
        starts.setdefault(a, []).append(b)

    loops = []
    while edges:
        (first, second) = next(iter(edges))
        del edges[(first, second)]
        loop, point = [first], second
        while point != first:
            loop.append(point)
            nxt = None
            for cand in starts.get(point, ()):
                if (point, cand) in edges:
                    nxt = cand
                    break
            del edges[(point, nxt)]
            point = nxt
        loops.append(collapse(loop))
    return loops


def collapse(loop):
    """Drop the points that sit in the middle of a straight run."""
    out = []
    n = len(loop)
    for i, point in enumerate(loop):
        before, after = loop[i - 1], loop[(i + 1) % n]
        straight = (before[0] == point[0] == after[0]) or (before[1] == point[1] == after[1])
        if not straight:
            out.append(point)
    return out


def draw(pen, cells):
    for loop in contours(cells):
        pen.moveTo((loop[0][0] * PX, loop[0][1] * PX))
        for point in loop[1:]:
            pen.lineTo((point[0] * PX, point[1] * PX))
        pen.closePath()


def check_box(maps):
    """Nothing may fall outside the box.

    Column 10 is the gutter that keeps two letters apart, and every letter
    leaves it empty; a frame tile fills it, because a border that stopped one
    pixel short would show a gap at every cell. So the box is checked, not the
    convention — `check.py` is what proves the letters did not move.
    """
    from pixelfont import BOTTOM_PX, CELL_W, TOP_PX
    for name, (_, rows) in maps.items():
        assert len(rows) == TOP_PX - BOTTOM_PX + 1, (name, "hauteur", len(rows))
        for x, y in rows_to_cells(rows):
            assert 0 <= x < CELL_W, (name, "colonne", x)
            assert BOTTOM_PX <= y <= TOP_PX, (name, "rangee", y)


def build(weight, maps, out_path):
    check_box(maps)
    style, weight_class = WEIGHTS[weight]
    order = [".notdef"] + [n for n in maps if n != ".notdef"]
    builder = FontBuilder(UPEM, isTTF=True)
    builder.setupGlyphOrder(order)
    builder.setupCharacterMap({cp: n for n, (cp, _) in maps.items() if cp is not None})

    glyphs, metrics = {}, {}
    for name in order:
        pen = TTGlyphPen(None)
        cells = rows_to_cells(maps[name][1]) if name in maps else set()
        draw(pen, cells)
        glyphs[name] = pen.glyph()
        # The left bearing has to agree with the outline, or a rasteriser that
        # trusts hmtx over glyf slides the glyph back against the left edge.
        metrics[name] = (CELL_W * PX, min((x for x, _ in cells), default=0) * PX)
    builder.setupGlyf(glyphs)
    builder.setupHorizontalMetrics(metrics)
    builder.setupHorizontalHeader(ascent=ASCENT_PX * PX, descent=-DESCENT_PX * PX, lineGap=0)

    full = f"{FAMILY} {style}"
    builder.setupNameTable({
        "familyName": FAMILY,
        "styleName": style,
        "uniqueFontIdentifier": f"{full} {VERSION}",
        "fullName": full,
        "version": f"Version {VERSION}",
        "psName": full.replace(" ", ""),
        "copyright": COPYRIGHT,
        "licenseDescription": "This Font Software is licensed under the SIL Open Font License, "
                              "Version 1.1. See OFL.txt.",
        "licenseInfoURL": "https://openfontlicense.org",
    })
    builder.setupOS2(
        sTypoAscender=ASCENT_PX * PX, sTypoDescender=-DESCENT_PX * PX, sTypoLineGap=0,
        usWinAscent=ASCENT_PX * PX, usWinDescent=DESCENT_PX * PX,
        sCapHeight=CAP_PX * PX, sxHeight=X_PX * PX, usWeightClass=weight_class,
    )
    builder.setupPost(isFixedPitch=1)
    builder.save(out_path)
    return len(order)


def main(out_dir):
    out_dir.mkdir(parents=True, exist_ok=True)
    for weight in WEIGHTS:
        maps = read_maps(GLYPHS / f"{weight}.txt")
        path = out_dir / f"speakup_tile_{weight}.ttf"
        count = build(weight, maps, path)
        print(f"{path}: {count} glyphes")


if __name__ == "__main__":
    main(Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).parent / "ttf")
