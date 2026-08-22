> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](vocabulary-metrics.md).

# Vocabulary metrics

Below are the detailed feedback metrics appearing in the response for `vocabulary` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="398">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>vocab.overall_metrics.lexical_diversity</td><td>The degree of word diversity in the response.</td></tr><tr><td>vocab.overall_metrics.word_sophistication</td><td>The degree of use of advanced, less common vocabulary in the response.</td></tr><tr><td>vocab.overall_metrics.word_specificity</td><td>The degree of use of specific (less general) verbs, nouns, and adjectives which are specific to the meaning being conveyed.</td></tr><tr><td>vocab.overall_metrics.academic_language_use</td><td>The degree of use of academic language in the response.</td></tr><tr><td>vocab.overall_metrics.collocation_commonality</td><td>The degree of use of advanced word combinations.</td></tr><tr><td>vocab.overall_metrics.idiomaticity</td><td>The degree of use of idiomatic language.</td></tr></tbody></table>

Below is an example showing the snippet of `vocabulary` metrics:

{% code overflow="wrap" lineNumbers="true" %}

```json
"vocab": {
      "overall_metrics": {
        "lexical_diversity": {
          "score": 10,
          "level": "high"
        },
        "word_sophistication": {
          "score": 9,
          "level": "high"
        },
        "word_specificity": {
          "score": 6,
          "level": "mid",
          "message": "Your response uses more general words than most advanced speakers. To improve, you should aim to learn and use verbs, nouns, and adjectives more specific to the meaning you wish to convey."
        },
        "academic_language_use": {
          "score": 1,
          "level": "low",
          "message": "Your response is low on use of academic words. You should learn and use some academic language in your responses to improve."
        },
        "collocation_commonality": {
          "score": 9,
          "level": "high"
        },
        "idiomaticity": {
          "score": 4,
          "level": "mid"
        }
      }
    }
```

{% endcode %}
