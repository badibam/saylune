"""Render pixel-map text as a PNG plank, to judge glyphs by looking at them.

Colours come from `docs/design/pixel-ui.md`: one hue, prune 301, two registers.
Out-of-gamut colours lose chroma until they fit, never a channel clipped on its
own, which would twist the hue.
"""

import math
from pathlib import Path

from PIL import Image, ImageDraw

from pixelfont import CELL_W, GLYPHS, LINE_PX, TOP_PX, read_maps

NIGHT = {"bg": (0.20, 0.045), "panel": (0.26, 0.050), "ink": (0.64, 0.020), "dim": (0.44, 0.015)}
PALE = {"bg": (0.90, 0.075), "panel": (0.84, 0.070), "ink": (0.40, 0.020), "dim": (0.60, 0.020)}
HUE = 301.0


def oklch_to_srgb(lightness, chroma, hue):
    while True:
        rgb = _oklab_to_linear(lightness, chroma * math.cos(math.radians(hue)),
                               chroma * math.sin(math.radians(hue)))
        if all(-1e-6 <= c <= 1 + 1e-6 for c in rgb) or chroma <= 0:
            return tuple(round(255 * _gamma(min(max(c, 0.0), 1.0))) for c in rgb)
        chroma -= 0.002


def _oklab_to_linear(el, a, b):
    l_ = el + 0.3963377774 * a + 0.2158037573 * b
    m_ = el - 0.1055613458 * a - 0.0638541728 * b
    s_ = el - 0.0894841775 * a - 1.2914855480 * b
    l, m, s = l_ ** 3, m_ ** 3, s_ ** 3
    return (
        +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
        -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s,
    )


def _gamma(c):
    return 1.055 * c ** (1 / 2.4) - 0.055 if c > 0.0031308 else 12.92 * c


def colour(register, key):
    lightness, chroma = (NIGHT if register == "night" else PALE)[key]
    return oklch_to_srgb(lightness, chroma, HUE)


class Sheet:
    """The pixel maps of one weight, plus whatever candidates override them."""

    def __init__(self, weight, *extra):
        """extra: a path, or (path, top) when the maps are drawn in a taller box."""
        base = read_maps(GLYPHS / f"{weight}.txt")
        self.maps = {n: (cp, rows, TOP_PX) for n, (cp, rows) in base.items()}
        for item in extra:
            path, top = item if isinstance(item, tuple) else (item, TOP_PX)
            for name, (cp, rows) in read_maps(path).items():
                # a candidate that reuses a base name keeps its codepoint
                known = self.maps.get(name)
                self.maps[name] = (cp if cp is not None else known and known[0], rows, top)
        self.by_cp = {cp: n for n, (cp, _, _) in self.maps.items() if cp is not None}

    def cells(self, ch):
        name = self.by_cp.get(ord(ch)) if len(ch) == 1 else ch
        entry = self.maps.get(name)
        if entry is None:
            return None
        _, rows, top = entry
        return {(x, top - i) for i, r in enumerate(rows) for x, c in enumerate(r) if c == "#"}


def draw_text(img, sheet, text, origin, scale, rgb, names=None, baseline=TOP_PX):
    """Draw a string, or a list of glyph names, one cell after the other.

    `baseline` is where the row y=0 lands, counted in pixels from the top of
    the line box: a taller box just moves the letters down inside it.
    """
    px = ImageDraw.Draw(img)
    ox, oy = origin
    for i, item in enumerate(names or text):
        cells = sheet.cells(item)
        if cells is None:
            continue
        for x, y in cells:
            left = ox + (i * CELL_W + x) * scale
            top = oy + (baseline - y) * scale
            px.rectangle([left, top, left + scale - 1, top + scale - 1], fill=rgb)


def plank(lines, out, weight="regular", extra=(), scale=3, register="night", pitch=None, pad=24,
          baseline=TOP_PX):
    """lines: (text_or_names, key) pairs; a bare None leaves a blank line."""
    sheet = Sheet(weight, *extra)
    pitch = pitch or LINE_PX * scale
    width = pad * 2 + max((len(l[0]) if l else 0) for l in lines) * CELL_W * scale
    height = pad * 2 + len(lines) * pitch
    img = Image.new("RGB", (width, height), colour(register, "bg"))
    for row, line in enumerate(lines):
        if line is None:
            continue
        item, key = line
        args = {"names": item} if isinstance(item, list) else {"text": item}
        draw_text(img, sheet, args.get("text", ""), (pad, pad + row * pitch), scale,
                  colour(register, key), names=args.get("names"), baseline=baseline)
    Path(out).parent.mkdir(parents=True, exist_ok=True)
    img.save(out)
    return out
