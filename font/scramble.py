"""Cover each letter with squares, so a turn of the AI can be seen and not read.

`pixel-ui.md` asks for the support of the phrase to survive and its content to
go: the height and width of each letter, the length of the words, the
punctuation and the line breaks stay, and nothing can be deciphered. So a
scrambled letter is its own ink box filled with squares the thickness of the
stroke, drawn at random from a seed that is the letter itself — the same letter
is therefore covered the same way everywhere and forever.

Its codepoint is the letter's plus 0xE100, which makes the app's table an
addition: a character is scrambled if the shifted codepoint is in the font.
"""

import random

from pixelfont import CELL_W, GLYPHS, cells_to_rows, read_maps, rows_to_cells, write_maps

SHIFT = 0xE100
# Two pixels in both weights: the doc asks for squares the thickness of the
# regular stroke, and a thin one covered by single pixels reads as static
# rather than as a phrase. Thin simply gets fewer squares, having less ink.
SQUARE = {"regular": 2, "thin": 2}
LETTERS = ("abcdefghijklmnopqrstuvwxyz"
           "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
           "0123456789"
           "àáâäçéèêëíîïóôöùúûüÿæœ"
           "ÀÁÂÄÇÉÈÊËÍÎÏÓÔÖÙÚÛÜŸÆŒ")

# A letter with no twin of its own falls back on this one, so that a forgotten
# character shows as scrambled ink rather than in the clear. Punctuation keeps
# no twin on purpose: `pixel-ui.md` wants it to survive as the support of the
# phrase. It sits at the shift itself, U+E100, which is the scramble of no
# character at all.
FALLBACK = (0x0000, ["#" * 10 + "."] * 8 + ["." * 11] * 2)


def scramble(cells, side, seed):
    """Fill the ink box with square blocks, as much ink as the letter had."""
    xs = [x for x, _ in cells]
    ys = [y for _, y in cells]
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
    slots = [(x, y)
             for x in range(x0, x1 - side + 2, side)
             for y in range(y0, y1 - side + 2, side)]
    wanted = max(1, min(len(slots), round(len(cells) / (side * side))))
    chosen = random.Random(seed).sample(slots, wanted)
    return {(x + i, y + j) for x, y in chosen for i in range(side) for j in range(side)}


def main():
    for weight, side in SQUARE.items():
        path = GLYPHS / f"{weight}.txt"
        maps = read_maps(path)
        by_cp = {cp: rows for _, (cp, rows) in maps.items() if cp is not None}
        for ch in LETTERS:
            rows = by_cp.get(ord(ch))
            assert rows is not None, ch
            cells = scramble(rows_to_cells(rows), side, ord(ch))
            maps[f"scrambled.{ord(ch):04X}"] = (ord(ch) + SHIFT, cells_to_rows(cells))
        cp, rows = FALLBACK
        cells = scramble(rows_to_cells(rows), side, cp)
        maps["scrambled.fallback"] = (cp + SHIFT, cells_to_rows(cells))

        header = path.read_text(encoding="utf-8").split("\n@")[0]
        write_maps(path, maps, header)
        print(f"{path.name}: {len(LETTERS) + 1} lettres brouillees, {len(maps)} glyphes en tout")


if __name__ == "__main__":
    main()
