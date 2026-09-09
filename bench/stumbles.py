#!/usr/bin/env python3
"""The sentences that carry a hesitation, and their references.

Unlike `phrases.py`, where the text is the *correct* form and the model voice is
what should have been said, here the text is the **verbatim** -- what the mouth
actually produced, hesitation included. That is what a recogniser is asked to
write back, and what it is scored against.

Built on the sentences of `phrases.py` on purpose: the words are already in the
bench's world, already rendered, already measured, so a control pair costs
nothing and nothing new has to be calibrated.

**These are acted hesitations, and that bounds what they measure.** A read `uh`
is clean, articulated and detached from its neighbours; a real one is short and
glued to the word after it. So this block is a *floor*: a model that will not
write an acted hesitation will never write a real one. It says nothing about the
other direction -- that is what the spontaneous block is for.

Scoring rule, decided here so it is not renegotiated in front of a number: any
filler token counts as rendered. A learner whose first language is French
hesitates with a French vowel, not an English one, and a recogniser that writes
`euh`, `eh` or `a` where the reference says `uh` has heard the hesitation. What
counts as a miss is writing nothing, or writing a content word.
"""

# (slug, verbatim text, kind). The slug names the recorded file, so it stays
# stable. `kind` is what the take is there to exercise; `clean` is a control.
#
# Written in the convention of the nyra verbatim benchmark, so our numbers
# compare to theirs without a translation: a cut-off word keeps its trailing
# hyphen, and a filled pause is a word of its own.
STUMBLES = (
    # Filled pauses. The commonest kind by far, and the one the fluency sheet
    # leans on hardest.
    ("filler-market", "Yesterday I went to the uh market", "filler"),
    ("filler-office", "I am walking to the um office", "filler"),
    ("filler-right", "I think you are uh right", "filler"),
    ("filler-opening", "Um I go to school every day", "filler"),
    ("filler-twice", "It is uh important uh for me", "filler"),
    # The same, hesitated in French. Our learner's first language, so this is
    # the case the app will actually meet; the reference stays `uh` under the
    # scoring rule above.
    ("filler-french", "Yesterday I went to the uh market", "filler"),
    # Repetitions.
    ("repeat-i", "I I went to the market", "repetition"),
    ("repeat-the", "The the sink is broken", "repetition"),
    ("repeat-group", "I want to I want to turn right at the corner", "repetition"),
    # False starts and self-repairs: the speaker abandons a form and restates.
    # This is the kind that decides which words are kept, so it is the kind the
    # sound analysis depends on most.
    ("restart-tense", "I go to I went to the market yesterday", "restart"),
    ("restart-group", "The chair is very the chair is very comfortable", "restart"),
    ("repair-word", "Turn left turn right at the corner", "restart"),
    # Cut-off words: a fragment that is not a word of the language. Nothing in
    # a recogniser's vocabulary writes it, so it is the hardest kind and the one
    # most likely to come back as a wrong whole word.
    ("cut-comfortable", "It is very comf- comfortable", "fragment"),
    ("cut-interesting", "This lesson is inter- interesting for me", "fragment"),
    ("cut-tree", "I picked a pear from the tr- tree", "fragment"),
    ("cut-question", "That ques- question was easy", "fragment"),
    # Silence where a hesitation would be: two seconds of nothing, mid-sentence.
    # There is nothing to write, so this take asks the opposite question -- does
    # the model invent a word to fill the gap?
    ("gap-market", "Yesterday I went to the market", "gap"),
    ("gap-corner", "Turn right at the corner", "gap"),
    # A syllable held long -- "I waaaant to". No sheet reads it since the rhythm
    # one was dropped, so this is a probe and not a measurement: it costs one
    # take to know whether a recogniser leaves any trace of it at all.
    ("held-want", "I want to go to the market", "held"),
    # Controls: the same sentences said straight through. A hesitation written
    # where none was said costs exactly as much as one that was missed.
    ("clean-market", "Yesterday I went to the market", "clean"),
    ("clean-office", "I am walking to the office", "clean"),
    ("clean-comfortable", "The chair is very comfortable", "clean"),
    ("clean-corner", "Turn right at the corner", "clean"),
    ("clean-important", "It is important for me", "clean"),
)

# The other half of the set, and the one that measures rather than floors it:
# questions answered cold, where the hesitations are nobody's decision. Each is
# built to make the speaker fetch something -- a word they may not have, a
# sequence they have to keep straight, a constraint that blocks the easy answer.
# None of them takes yes or no, and none has a rehearsed answer.
#
# There is no reference text here. It gets written from the recording afterwards,
# which is the expensive step of the whole set.
QUESTIONS = (
    ("door-lock", "Explain how a door lock works, without using the word key."),
    ("lost-thing", "Tell me about the last time you lost something. What happened?"),
    ("worst-meal", "Describe the worst meal you have ever eaten."),
    ("dark-sky", "How would you explain to a child why the sky is dark at night?"),
    ("this-room", "Describe one object in the room you are in, in detail, without naming it."),
    ("old-photo", "What is in the oldest photograph you can remember seeing?"),
    ("other-job", "If you had to do a completely different job tomorrow, which one, and why?"),
    ("failed-repair", "Tell me about something you tried to repair and failed to."),
    ("to-the-baker", "Give directions from your home to the nearest bakery."),
    ("proved-wrong", "Describe a time you disagreed with someone and were proved wrong."),
    ("file-1850", "Explain what a computer file is, to someone from 1850."),
    ("a-smell", "Describe a smell you like, without saying what produces it."),
)

# How the take is to be spoken when the text alone does not say it. Read by the
# recording session, which shows it before the phrase.
DIRECTIONS = {
    "filler-french": "hesitate the French way -- 'euh', not an English 'uh'",
    "gap-market": "stop dead after 'to the', wait two seconds, then finish",
    "gap-corner": "stop dead after 'turn', wait two seconds, then finish",
    "held-want": "hold the vowel of 'want' for about two seconds",
}
