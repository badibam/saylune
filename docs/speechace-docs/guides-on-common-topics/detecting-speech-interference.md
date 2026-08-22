> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](detecting-speech-interference.md).

# Detecting Speech Interference

When scoring pronunciation, the audio submitted to the Speechace API should contain only the speech the user intends to be assessed — the words in the `text` parameter. In real-world conditions, however, recordings often contain additional speech beyond what was expected: a speaker in the background, the user continuing to speak after the expected text, or another person's voice overlapping with the user's.

This additional speech is called **interference**. The Speechace API can detect and measure it, returning an `interference_ratio` that tells you how much of the audio contains speech that competes with the intended text for alignment and scoring.

## Interference vs. General Noise

It is important to distinguish between **interference** and general background noise:

* **Background noise** — non-speech sounds such as music, traffic, typing, or ambient room noise. These affect recording quality but do not compete directly with the alignment and scoring of the expected words.
* **Speech interference** — *speech* that is present in the audio beyond what the `text` parameter describes. This directly competes with the scoring engine when it attempts to align and score what was spoken. Common sources include:
  * A second speaker in the room talking over the user
  * The user continuing to speak additional sentences after the expected text ends
  * A TV, podcast, or voice recording playing in the background

Speechace's interference detection is specifically focused on **competing speech**, not general noise.

## How to Request Interference Metrics

Pass `include_interference_metrics = 1` as a form body parameter in your [Score Text/Pronunciation](../api-reference/score-text-pronunciation.md) or [Score Word](../api-reference/score-word.md) request:

```bash
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="Some parents admire famous athletes as strong role models."' \
--form 'user_audio_file=@"recording.wav"' \
--form 'include_interference_metrics=1'
```

Without this parameter, `interference_ratio` is not returned in the response.

## Response

When `include_interference_metrics = 1` is set, the `text_score` object in the response includes an `interference_ratio` field:

```json
{
  "status": "success",
  "text_score": {
    "text": "Some parents admire famous athletes as strong role models.",
    "interference_ratio": 2.7,
    "word_score_list": [ ... ],
    "speechace_score": {
      "pronunciation": 85
    }
  }
}
```

## Interpreting interference\_ratio

The `interference_ratio` is a numeric value that increases with the severity of detected speech interference. Use the following bands as a guide:

<table><thead><tr><th width="186.50390625" align="center">interference_ratio</th><th width="147.2890625" align="center">Level</th><th>Interpretation</th></tr></thead><tbody><tr><td align="center"><code>0</code></td><td align="center"><strong>None</strong></td><td>No excess speech detected. The audio closely matches the expected text with no competing speech.</td></tr><tr><td align="center"><code>0</code> – <code>1</code></td><td align="center"><strong>Low</strong></td><td>A small amount of extra speech is present — for example, a brief word spoken before or after the intended text. Scoring results are reliable.</td></tr><tr><td align="center"><code>2</code> – <code>3</code></td><td align="center"><strong>Mid</strong></td><td>A moderate amount of competing speech is detected. Scoring may be affected; consider surfacing a caution to the user.</td></tr><tr><td align="center"><code>3+</code></td><td align="center"><strong>High</strong></td><td>Significant speech interference is present. Scoring reliability is reduced. The user should be prompted to retry in a quieter environment.</td></tr></tbody></table>

## Using interference\_ratio in Your Application

The `interference_ratio` gives your application actionable signal about recording conditions. Here are the recommended patterns:

Warn about reduced scoring reliability

```
if interference_ratio >= 2:
    show_warning("Your recording contained background speech. "
                 "Your score may not fully reflect your pronunciation.")
```

Guide users to retry in a quieter environment

```
if interference_ratio >= 3:
    show_prompt("We detected other voices in your recording. "
                "Please try again somewhere quieter for a more accurate score.")
```

Apply reduced weighting to high-interference items

```
if interference_ratio >= 3:
    flag_for_review(item_id, reason="high_interference")
    # Exclude from final score calculation or prompt a re-attempt
```

## Notes

<details>

<summary>Additional notes about interference metrics</summary>

* `interference_ratio` is only present in the response when `include_interference_metrics = 1` is explicitly passed. It is not returned by default.
* Interference detection applies to the Score Text/Pronunciation endpoint. See the [API Reference](broken://pages/b856b7a8a7db6afd35402c83995619e2348d0821) for the full parameter list.

</details>
