"""The phonemes of the analysis: fifteen sounds the Latin alphabet does not write.

`ui/AnalysisReadout.kt` prints the model's inventory, which is IPA
(`../bench/affinity.json`): thirty-eight symbols over thirty-seven characters,
of which twenty-two are ordinary letters the font already had.

Three of the rest are an existing letter turned or mirrored, seven more are one
with a bar or a hook added, and four are drawn outright. That split is what
keeps both weights honest: a composed glyph inherits the stroke of the letter it
is made of, so nothing has to be pushed twice.

The two affricate ligatures U+02A4 and U+02A7 are deliberately absent. A
ligature in a fixed-width font has to fit one cell either way, so cramming two
letters into ten columns is strictly worse than taking two cells: the screen
writes them `dʒ` and `tʃ`, which is standard IPA, and the diphthongs are already
two cells wide. Only the ezh is drawn.
"""

from pixelfont import (CELL_W, GLYPH_W, GLYPHS, ROWS, cells_to_rows, read_maps,
                       rows_to_cells, write_maps)

STROKE = {"regular": 2, "thin": 1}
BLANK = "." * CELL_W


def _pad(first, rows):
    out = [BLANK] * first + [r.ljust(CELL_W, ".") for r in rows]
    return out + [BLANK] * (ROWS - len(out))


def turned(rows):
    """Half a turn, inside the ink columns and inside the glyph's own band."""
    cells = rows_to_cells(rows)
    ys = [y for _, y in cells]
    lo, hi = min(ys), max(ys)
    return {(GLYPH_W - 1 - x, lo + hi - y) for x, y in cells}


def mirrored(rows):
    return {(GLYPH_W - 1 - x, y) for x, y in rows_to_cells(rows)}


def rect(x0, x1, y0, y1):
    return {(x, y) for x in range(x0, x1 + 1) for y in range(y0, y1 + 1)}


def rightmost(cells):
    return max(x for x, _ in cells)


# name -> (codepoint, base letter, what to do with it)
TURNED = {
    "schwa": (0x0259, "e", turned),          # a rotated e
    "rturned": (0x0279, "r", turned),        # a rotated r
    "oopen": (0x0254, "c", mirrored),        # a mirrored c
}


def composed(name, base, cells, w):
    """The seven glyphs that are a letter plus a bar or a hook."""
    if name == "alpha":                      # a single-storey a: d without its ascender
        return {(x, y) for x, y in cells if y <= 7}
    if name == "eopen":                      # c with a middle bar
        return cells | rect(0, 6, 5 - w, 4)
    if name == "theta":                      # o crossed through
        return cells | rect(0, GLYPH_W - 1, 5 - w, 4)
    if name == "rfishhook":                  # r with its foot turned right
        return cells | rect(2, 6, 0, w - 1)
    if name == "eng":                        # n with a hook under its right leg
        r = rightmost(cells)
        return cells | rect(r - w + 1, r, -1, -1) | rect(r - 5, r, -2, -2)
    if name == "iotasmall":                  # a small capital I
        c = (GLYPH_W - w) // 2
        return (rect(0, GLYPH_W - 1, 8 - w, 7) | rect(0, GLYPH_W - 1, 0, w - 1)
                | rect(c, c + w - 1, 0, 7))
    if name == "erhooked":                   # the open e, with the rhotic hook
        return cells | rect(GLYPH_W - 4, GLYPH_W - 3, -1, -1) | rect(3, GLYPH_W - 3, -2, -2)
    raise KeyError(name)


# name -> (codepoint, base letter or None, first row of the drawing)
COMPOSED = {
    "alpha": (0x0251, "d"),
    "eopen": (0x025B, "c"),
    "theta": (0x03B8, "o"),
    "rfishhook": (0x027E, "r"),
    "eng": (0x014B, "n"),
    "iotasmall": (0x026A, None),
    "erhooked": (0x025D, "eopen"),           # built on the one just above
}

DRAWN = {
    "regular": {
        "eth": (0x00F0, 2, [
            "......##...",
            ".########..",
            ".########..",
            "##########.",
            "##########.",
            "##......##.",
            "##......##.",
            "##......##.",
            "##########.",
            ".########..",
        ]),
        "upsilon": (0x028A, 4, [
            "###....###.",
            ".##....##..",
            ".##....##..",
            ".##....##..",
            ".##....##..",
            ".##....##..",
            ".########..",
            "..######...",
        ]),
        "esh": (0x0283, 2, [
            ".....####..",
            "....#####..",
            "....##.....",
            "....##.....",
            "....##.....",
            "....##.....",
            "....##.....",
            "....##.....",
            "....##.....",
            "....##.....",
            "..#####....",
            ".####......",
        ]),
        "ezh": (0x0292, 2, [
            ".########..",
            "##########.",
            "........##.",
            "...######..",
            "...######..",
            "......##...",
            "......##...",
            "......##...",
            "......##...",
            "......##...",
            "#######....",
            "#####......",
        ]),
    },
    "thin": {
        "eth": (0x00F0, 2, [
            "......#....",
            ".#######...",
            ".#######...",
            "#........#.",
            "#........#.",
            "#........#.",
            "#........#.",
            "#........#.",
            "#........#.",
            ".#######...",
        ]),
        "upsilon": (0x028A, 4, [
            "##.....##..",
            ".#.....#...",
            ".#.....#...",
            ".#.....#...",
            ".#.....#...",
            ".#.....#...",
            ".#######...",
            "..#####....",
        ]),
        "esh": (0x0283, 2, [
            ".....####..",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "....#......",
            "..###......",
        ]),
        "ezh": (0x0292, 2, [
            ".#######...",
            "#.......#..",
            "........#..",
            "...#####...",
            "......#....",
            "......#....",
            "......#....",
            "......#....",
            "......#....",
            "......#....",
            "#######....",
            "####.......",
        ]),
    },
}


def main():
    for weight, w in STROKE.items():
        path = GLYPHS / f"{weight}.txt"
        maps = read_maps(path)
        added = 0

        for name, (cp, base, how) in TURNED.items():
            maps[name] = (cp, cells_to_rows(how(maps[base][1])))
            added += 1

        for name, (cp, base) in COMPOSED.items():
            cells = rows_to_cells(maps[base][1]) if base else set()
            maps[name] = (cp, cells_to_rows(composed(name, base, cells, w)))
            added += 1

        for name, (cp, first, rows) in DRAWN[weight].items():
            maps[name] = (cp, _pad(first, rows))
            added += 1

        header = path.read_text(encoding="utf-8").split("\n@")[0]
        write_maps(path, maps, header)
        print(f"{path.name}: {added} phonemes, {len(maps)} glyphes en tout")


if __name__ == "__main__":
    main()
