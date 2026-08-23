#!/usr/bin/env python3
"""The sentences a model voice is calibrated on.

Calibration renders each sentence and scores it against its own text: whatever
the engine takes off is the engine's noise or the voice's, never a learner's
fault. So these are the sentences of `docs/design/pronunciation-test-set.md` in
their *correct* form -- the model is always what should have been said.

Kept in one place because three things read the same list: the calibration, the
dialect-agreement check, and the model renders the scoring itself needs.
"""

# (slug, text). The slug names the rendered file, so it stays stable.
CALIBRATION = (
    ("think", "I think you are right"),
    ("sink-broken", "The sink is broken"),
    ("sheep-field", "The sheep is in the field"),
    ("pear-tree", "I picked a pear from the tree"),
    ("turn-right", "Turn right at the corner"),
    ("walking-office", "I am walking to the office"),
    ("comfortable", "The chair is very comfortable"),
    ("doesnt-know", "He doesn't know"),
    ("years-old", "I am 25 years old"),
    ("school", "I go to school every day"),
    ("market", "Yesterday I went to the market"),
    ("think-sheep", "I think the sheep are in the field"),
    ("turn-right-long", "You have to turn right at the corner"),
    ("going-office", "You are going to the office"),
    ("important", "It is important for me"),
    ("interesting", "This lesson is interesting for me"),
)
