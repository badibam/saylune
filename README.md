# Saylune

An Android app for practising spoken English with an AI that never interrupts you, and for
checking your pronunciation sound by sound afterwards.

You talk out loud, for as long as you like. When you get something wrong the AI picks it up
in its own reply rather than stopping you — say *I have 25 years* and it answers *Ah, you're
25! And where…* — and the turn keeps a discreet mark, so that being corrected gently is not
the same as never learning it happened. What you do about a mark is yours: say the sentence
again on the spot, or leave it.

## What is unusual about it

Most pronunciation tools have your recording and a text, so they need a dictionary to tell
them what the words should have sounded like. This app has **two recordings of the same
sentence**: yours, and a synthesised model it has to produce anyway — that is the voice you
hear. So it compares one to the other, sound by sound, through the same acoustic network, and
nothing outside those two recordings ever judges anything. No pronunciation dictionary, no
dialect lexicon, no reference of rightness.

The accent you pick is therefore just a choice of voice. If the model is British, everything
derived from it is, with no lexicon to swap.

The analysis runs **on the phone**. It marks three scales at once — the sound, which syllable
of a word carries the stress, and the melody of the sentence — and anchors each one to the
letters on screen.

## What it needs before it can do anything

- **Your own API keys.** The app has none: no account, no server, nothing it pays for on your
  behalf. You bring keys for speech recognition, for the language model and for the voice, and
  they are stored encrypted on the device and sent only to the provider they belong to. This
  is a real wall at the door, and it is deliberate — it is also what makes the app publishable
  on F-Droid, which allows no embedded key.
- **The analysis model, 359 MB, downloaded once.** Not in the APK, and never fetched at first
  launch or in silence: you ask for it, in *Models and keys*. Until it is there the
  pronunciation marks are off and say so. It is Apache-2.0 licensed, and you can also point
  the app at a file you already have instead of downloading it.

## State

Early, and honest about it: this is one person's app, used on one phone. The loop works end to
end — recording, recognition, language model, voice, on-device analysis, marks on screen,
model to listen back to, saying it again. Plenty of what it measures is calibrated by hand and
says so in the docs. There is no icon yet.

## Building

```
./run            # the menu
./run build      # debug APK
./run install    # onto a connected device
./run release    # release APK
```

`./run` delegates to the Gradle wrapper and never to a system Gradle. Nothing else is needed.

## Documentation

`docs/` holds the design, and it is **written in French** — `docs/reference.md` is the way in
and points at the rest. `docs/analysis.md` is the one to read if what interests you is how the
pronunciation analysis works and what has actually been measured about it; `docs/qualification.md`
is the procedure for checking it, meant to be replayable by someone else.

## Licence

GPL-3.0-or-later, see `LICENSE`.

The pixel font in `font/` is derived from Mono10 by Michael Vieth and stays under the SIL Open
Font License 1.1 (`font/OFL.txt`). The per-phoneme reference recordings are from Wikimedia
Commons under CC BY-SA 3.0, credited in the app itself.
