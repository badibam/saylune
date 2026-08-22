> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](interpreting-fidelity-class.md).

# Interpreting fidelity class

The `fidelity_class` field in Speechace indicates whether the user accurately completed the text utterance. If the value is not `CORRECT`, it suggests that the user's attempt may be unsuitable for scoring, as it may indicate incomplete, misunderstood, or unintended responses to the activity.

The following are the possible values returned by the `fidelity_class` field:

<table><thead><tr><th width="161">Value</th><th>Description</th></tr></thead><tbody><tr><td>CORRECT</td><td>The attempt is faithful and can be used for scoring and evaluation.</td></tr><tr><td>NO_SPEECH</td><td>No intelligible human speech is detected in the attempt.</td></tr><tr><td>INCOMPLETE</td><td>The attempt is incomplete indicating the recording may have been cut off or timed out.</td></tr><tr><td>FREE_SPEAK</td><td>The user is freely speaking outside the expected activity text.</td></tr></tbody></table>

In such cases the [Speechace Fluency API](../api-reference/score-text-fluency.md) will reduce the overall scores for the utterance and return a `score_issue_list[]` entry such as the below:

```json
"score_issue_list":
[
  {
    "status": "warning",
    "short_message": "response_incomplete",
    "detail_message": "The response doesn't follow the script completely.",
    "source": "fluency"
  }
]
```
