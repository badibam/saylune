"""Prove the round trip: every glyph we did not touch rasterises like upstream.

The five descenders are redrawn on purpose and are named here, so that any
other difference is a bug rather than a decision nobody remembers.

It also proves the app is carrying **this** font. Putting the compiled files in
`app/src/main/res/font/` is a copy made by hand, and a copy nobody checks goes
quietly out of date: the app's went on shipping a font that stopped at U+E01C
long after the furniture past it had been drawn, and a character the font does
not carry draws nothing at all rather than an empty box. Comparing the bytes is
the whole of it -- the files are a product of `build.py`, so they are equal or
one of them is old.
"""

import sys
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.pens.pointInsidePen import PointInsidePen

from pixelfont import BOTTOM_PX, CELL_W, TOP_PX

PAIRS = [
    ("tmp/Mono10 - Community Pack/TTF/Mono10 Regular.ttf", "font/ttf/speakup_tile_regular.ttf"),
    ("tmp/Mono10 - Community Pack/TTF/Mono10 Thin.ttf", "font/ttf/speakup_tile_thin.ttf"),
]


def raster(font, cp):
    gs = font.getGlyphSet()
    name = font.getBestCmap()[cp]
    cells = set()
    for y in range(BOTTOM_PX, TOP_PX + 1):
        for x in range(CELL_W):
            pen = PointInsidePen(gs, (x * 64 + 32, y * 64 + 32))
            gs[name].draw(pen)
            if pen.getResult():
                cells.add((x, y))
    return cells, font["hmtx"][name][0]


def shipped(root):
    """The app's copies, against the compiled ones."""
    built = sorted((root / "font/ttf").glob("*.ttf"))
    late = [
        path.name for path in built
        if (root / "app/src/main/res/font" / path.name).read_bytes() != path.read_bytes()
    ]
    print(f"app/src/main/res/font: {len(built)} fichiers, {len(late)} en retard")
    for name in late:
        print(f"  en retard : {name} — recopier depuis font/ttf/")
    return not late


def main(root):
    if not shipped(root):
        sys.exit(1)
    dropped = {0x0000, 0x000D}  # control artefacts of the upstream file, not carried over
    redrawn = {ord(c) for c in "gjpqy"}  # tails under the baseline

    for src, out in PAIRS:
        a, b = TTFont(root / src), TTFont(root / out)
        shared = sorted(set(a.getBestCmap()) & set(b.getBestCmap()))
        expected = set(a.getBestCmap()) - dropped
        bad = [cp for cp in shared if cp not in redrawn and raster(a, cp) != raster(b, cp)]
        missing = sorted(expected - set(b.getBestCmap()))
        print(f"{Path(out).name}: {len(shared) - len(redrawn)} compares, {len(bad)} ecarts, "
              f"{len(missing)} manquants, {len(redrawn)} redessines")
        for cp in bad[:5]:
            print("  ecart sur U+%04X" % cp)
        if bad or missing:
            sys.exit(1)


if __name__ == "__main__":
    main(Path(sys.argv[1] if len(sys.argv) > 1 else "/mnt/data/OUTILS/speakup"))
