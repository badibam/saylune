> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-relevance-response.md).

# Handling relevance response

The [pronunciation](../../features/scripted-activities/pronunciation-scoring.md) and [fluency](../../features/scripted-activities/fluency-scoring.md) interpretation of the key elements in the response of the spoken word or sentence remains the same as previously described. The parameter which we need to notice is the relevance check.&#x20;

**Relevance Check**

If the response is relevant, then `relevance.class` will be `TRUE`:

<div align="left"></div>

If the response is not relevant to the `context`, then `relevance.class` will be `FALSE` and the error will be shown:

<div align="left"></div>

If the response is too similar to the question prompt, then the error will be as follows:

<div align="left"></div>

**Other fields**

<table><thead><tr><th width="187">Field</th><th>Description</th></tr></thead><tbody><tr><td>transcript</td><td>The speech-to-text transcript of what the user has said.</td></tr><tr><td>speechace_score</td><td>An overall score on a scale of 0 to 100, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>ielts_score</td><td>An overall score on an IELTS scale of 0 to 9.0, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>pte_score</td><td>An overall score on a PTE scale of 10 to 90, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>cefr_score</td><td>An overall score on CEFR scale of A0 to C2, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>toeic_score</td><td>An overall score on an TOEIC scale of 0 to 200, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>relevance.class</td><td>TRUE or FALSE indicating whether the response was relevant given the <code>relevance_context</code> passed as input to the API.</td></tr></tbody></table>

{% hint style="info" %}
**Protip:** The `relevance_context` is an optional field and can be included only if the relevance of an audio response needs to be assessed by transcribing the response first and then comparing it to the provided question prompt.
{% endhint %}
