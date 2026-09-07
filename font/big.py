"""The furniture again, at four times the pixels, for the second size.

The register's second size is the same drawing at double the factor: one ink
pixel becomes a square of four, and a glyph read as a button is a coarse copy of
a glyph meant to be read as a letter. Here the drawing is made for that size --
22 by 22 pixels the size of the text's own, so a button carries the same pixel
as the line beside it (`docs/ui.md`, "Les images").

They are a **family of their own**, `Saylune Tile Big`, on the same private-use
codepoints as the furniture of the ordinary font: the app asks for the same
character and picks the family by what it is drawing, a button or a line of
text. Nothing in the ordinary font moves, so `check.py` still proves the letters
did not.

**What is a stroke thins with the weight** exactly as it does in the small
furniture, and here it costs nothing to say twice: a glyph is written as the
shapes it is made of, with the stroke a parameter -- four pixels in the regular,
two in the thin.
"""

from math import atan2, degrees, hypot

from pixelfont import GLYPHS, write_maps

BOX = 22                       # the ink box, twice the ordinary cell each way
STROKE = {"regular": 4, "thin": 2}
MID = BOX / 2                  # the centre, in continuous coordinates


# ── The shapes ──────────────────────────────────────────────────────────────
#
# Every one of them returns a set of lit pixels, x to the right and y up from
# the bottom row. A pixel is tested by its centre, which is what keeps a disc
# round and a diagonal even.

def rect(x0, y0, x1, y1):
    return {(x, y) for x in range(int(x0), int(x1) + 1) for y in range(int(y0), int(y1) + 1)}


def disc(cx, cy, r):
    return {
        (x, y) for x in range(BOX) for y in range(BOX)
        if hypot(x + 0.5 - cx, y + 0.5 - cy) <= r
    }


def ring(cx, cy, r, w, arc=None):
    """A circle of stroke [w], or the part of it between two angles."""
    out = set()
    for x in range(BOX):
        for y in range(BOX):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            if not (r - w <= hypot(dx, dy) <= r):
                continue
            if arc is not None:
                angle = degrees(atan2(dy, dx)) % 360
                start, end = arc
                inside = start <= angle <= end if start <= end else (
                    angle >= start or angle <= end
                )
                if not inside:
                    continue
            out.add((x, y))
    return out


def line(x0, y0, x1, y1, w):
    """A segment of stroke [w], its ends square-ish and its middle even."""
    out = set()
    for x in range(BOX):
        for y in range(BOX):
            px, py = x + 0.5, y + 0.5
            dx, dy = x1 - x0, y1 - y0
            length = hypot(dx, dy) or 1
            t = max(0.0, min(1.0, ((px - x0) * dx + (py - y0) * dy) / (length * length)))
            if hypot(px - (x0 + t * dx), py - (y0 + t * dy)) <= w / 2:
                out.add((x, y))
    return out


def polygon(points):
    """Every pixel whose centre is inside the closed shape [points]."""
    out = set()
    for x in range(BOX):
        for y in range(BOX):
            px, py = x + 0.5, y + 0.5
            inside = False
            for i, (ax, ay) in enumerate(points):
                bx, by = points[i - 1]
                if (ay > py) != (by > py) and px < ax + (py - ay) * (bx - ax) / (by - ay):
                    inside = not inside
            if inside:
                out.add((x, y))
    return out


# ── The furniture ───────────────────────────────────────────────────────────
#
# One function per glyph, taking the stroke width. The filled symbols ignore it
# and come out identical in both weights, as they do in the small font.

def play(w):
    return polygon([(6, 3), (6, 19), (18, 11)])


def record(w):
    return disc(MID, MID, 8)


def stop(w):
    return rect(4, 4, 17, 17)


def pause(w):
    return rect(4, 3, 8, 18) | rect(13, 3, 17, 18)


def gauge(fill):
    """A block of the gauge: [fill] columns of the twenty-two, eight rows tall."""
    return lambda w: rect(0, 7, fill - 1, 14)


def heart(w):
    return (
        disc(7, 14, 4.6) | disc(15, 14, 4.6)
        | polygon([(2.4, 13.6), (19.6, 13.6), (11, 3)])
    )


def mic(w):
    # The capsule, the cradle that holds it, and the stand under it.
    return (
        rect(7, 10, 14, 17) | disc(MID, 17, 4)
        | ring(MID, 11, 8, 3, arc=(200, 340))
        | rect(10, 1, 12, 5) | rect(5, 0, 16, 1)
    )


def magnifier(w):
    return ring(13, 13, 6.5, 3) | line(8.8, 8.8, 3, 3, 4)


def lens(half_width, half_height):
    """An almond: what two big discs have in common, given the size wanted.

    A lens is stated by the box it fills rather than by the discs that cut it --
    the radius and the offset follow from the two, and hollowing one by a stroke
    is then subtracting a smaller box instead of guessing two smaller discs.
    """
    r = (half_width ** 2 + half_height ** 2) / (2 * half_height)
    off = r - half_height
    return disc(MID, MID - off, r) & disc(MID, MID + off, r)


def eye(w):
    return (lens(10, 7.5) - lens(10 - 0.9 * w, 7.5 - 0.9 * w)) | disc(MID, MID, 2.5)


def lock(w):
    return (rect(3, 1, 18, 12) | ring(MID, 12, 6, 4, arc=(20, 160))) - (
        disc(MID, 8, 2) | rect(10, 4, 12, 8)
    )


def histogram(w):
    return rect(2, 0, 6, 9) | rect(8, 0, 12, 14) | rect(14, 0, 18, 20)


def dot_filled(w):
    return disc(MID, MID, 4)


def dot_hollow(w):
    return ring(MID, MID, 4, 3)


def arrow(dx, dy):
    """A shaft with a chevron head, pointing where [dx, dy] points."""
    def draw(w):
        tip = (MID + dx * 8, MID + dy * 8)
        tail = (MID - dx * 8, MID - dy * 8)
        return (
            line(tail[0], tail[1], tip[0], tip[1], w)
            | chevron(tip[0], tip[1], dx, dy, w, arm=7)
        )
    return draw


def check(w):
    return line(3, 11, 8.5, 5, w) | line(8.5, 5, 18, 17, w)


def cross(w):
    return line(4, 4, 18, 18, w) | line(4, 18, 18, 4, w)


def levers(w):
    """Two rails, each with the knob that says where a position sits."""
    return (
        line(2, 15, 19, 15, w) | rect(6, 12, 6 + w, 18)
        | line(2, 6, 19, 6, w) | rect(13, 3, 13 + w, 9)
    )


def back(w):
    """The return key's arrow: the rail one comes down, and the turn to the left."""
    return (
        line(17, 18, 17, 8, w) | line(17, 8, 5, 8, w)
        | line(5, 8, 10, 13, w) | line(5, 8, 10, 3, w)
    )


def chevron(x, y, dx, dy, w, arm=5.5):
    """The head the arrows wear, at a point and pointing where [dx, dy] does.

    The two barbs come back along the way it points, turned an eighth of a turn
    each side -- so they are as long as [arm] says, which a quarter turn taken
    on the diagonal would not be.
    """
    half = 2 ** 0.5 / 2
    return (
        line(x, y, x - (dx - dy) * half * arm, y - (dy + dx) * half * arm, w)
        | line(x, y, x - (dx + dy) * half * arm, y - (dy - dx) * half * arm, w)
    )


def redo(w):
    """The take started over: a circle open at the top right, the head where it stops.

    A filled head rather than the chevron the straight arrows wear: at the end of
    a curve the two barbs of a chevron fall almost on top of each other, and what
    they draw is a stub rather than a point.
    """
    return ring(MID, 10, 7.5, w, arc=(90, 10)) | polygon([
        (MID + 5, 17.5), (MID, 21.75), (MID, 13.25),
    ])


FURNITURE = {
    "play": (0xE010, play),
    "record": (0xE011, record),
    "gauge.quarter": (0xE012, gauge(6)),
    "gauge.half": (0xE013, gauge(11)),
    "gauge.threequarters": (0xE014, gauge(16)),
    "gauge.full": (0xE015, gauge(22)),
    "arrow.left": (0xE016, arrow(-1, 0)),
    "arrow.up": (0xE017, arrow(0, 1)),
    "arrow.right": (0xE018, arrow(1, 0)),
    "arrow.down": (0xE019, arrow(0, -1)),
    "check": (0xE01A, check),
    "cross": (0xE01B, cross),
    "heart": (0xE01C, heart),
    "pause": (0xE01D, pause),
    "stop": (0xE01E, stop),
    "mic": (0xE01F, mic),
    "magnifier": (0xE020, magnifier),
    "eye": (0xE021, eye),
    "levers": (0xE022, levers),
    "back": (0xE023, back),
    "redo": (0xE024, redo),
    "lock": (0xE025, lock),
    "histogram": (0xE026, histogram),
    "dot.filled": (0xE027, dot_filled),
    "dot.hollow": (0xE028, dot_hollow),
}

HEADER = (
    "# The furniture at the second size, drawn rather than doubled.\n"
    "# Written by big.py -- 22 by 22, rows from y=21 down to y=0.\n"
)


def rows_of(cells):
    return [
        "".join("#" if (x, BOX - 1 - i) in cells else "." for x in range(BOX))
        for i in range(BOX)
    ]


def main():
    for weight, w in STROKE.items():
        maps = {
            name: (cp, rows_of({
                (x, y) for x, y in draw(w) if 0 <= x < BOX and 0 <= y < BOX
            }))
            for name, (cp, draw) in FURNITURE.items()
        }
        path = GLYPHS / f"big-{weight}.txt"
        write_maps(path, maps, HEADER)
        print(f"{path.name}: {len(maps)} meubles")


if __name__ == "__main__":
    main()
