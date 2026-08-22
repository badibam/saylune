> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](per-metric-feedback.md).

# Per metric feedback

The API also offers detailed feedback on each of the Pronunciation, Fluency, Vocabulary, Grammar and Cohesion metrics. Such feedback can be used to explain why a user got a low score on a specific metric. Such feedback is available in the following nodes:

<table><thead><tr><th width="188">Node</th><th>Description</th></tr></thead><tbody><tr><td>fluency</td><td>This node contains <a href="/pages/Qd2DoQIAUsxraBWYNEOC"><strong>fluency</strong></a> metrics and sub-scores for the overall utterance and for each segment (sentence) within the utterance.</td></tr><tr><td>word_score_list[]</td><td>This node contains <a href="/pages/H0hoZyGd3ZOIVlASwcEy"><strong>pronunciation</strong></a> scores and metrics for each word, syllable, and phoneme within the utterance.</td></tr><tr><td>grammar</td><td>This node contains <strong>grammar</strong> metrics, errors and feedback for the overall utterance.</td></tr><tr><td>vocabulary</td><td>This node contains <strong>vocabulary</strong> metrics, errors and feedback for the overall utterance.</td></tr><tr><td>coherence</td><td>This node contains <strong>coherence</strong> metrics, errors and feedback for the overall utterance.</td></tr></tbody></table>

Details regarding [fluency](../../features/scripted-activities/fluency-scoring.md) and [pronunciation](../../features/scripted-activities/pronunciation-scoring.md) scoring were explained in detail in earlier parts of this documentation. Each of the Grammar, Vocabulary, and Coherence feedback metrics has 3 sub-elements:

1. **score:** on a scale of 1 to 10
2. **level:** an interpretation of the score as low/mid/high
3. **message:** a feedback message if the score is low.

In the next few sections, we will review the grammar, vocabulary and coherence scores in detail:
