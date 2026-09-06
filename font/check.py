"""Prove the round trip: every glyph we did not touch rasterises like upstream.

The five descenders are redrawn on purpose and are named here, so that any
other difference is a bug rather than a decision nobody remembers.
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


def main(root):
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
