> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](coherence-metrics.md).

# Coherence metrics

Below are the detailed feedback metrics appearing in the response for `coherence` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="414">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>coherence.overall_metrics.lexical_density</td><td>The degree of use of content words within the response.</td></tr><tr><td>coherence.overall_metrics.basic_connectives</td><td>The degree and variety of basic connectives within the response. A list of most overused basic connectives is included.</td></tr><tr><td>coherence.overall_metrics.causal_connectives</td><td>The degree and variety of causal connectives within the response. A list of most overused causal connectives is included.</td></tr><tr><td>coherence.overall_metrics.negative_connectives</td><td>The degree and variety of negative connectives within the response. A list of most used negative connectives is included.</td></tr><tr><td>coherence.overall_metrics.pronoun_density</td><td>The degree of use of pronouns within the response.</td></tr><tr><td>coherence.overall_metrics.adverb_diversity</td><td>The degree and variety of adverbs within the response. A list of most overused adverbs is included.</td></tr><tr><td>coherence.overall_metrics.verb_diversity</td><td>The degree and variety of verbs within the response. A list of most overused verbs is included.</td></tr></tbody></table>

Below is an example showing the snippet of `coherence` metrics:

{% code overflow="wrap" lineNumbers="true" %}

```json5
"coherence": {
      "overall_metrics": {
        "lexical_density": {
          "score": 10,
          "level": "high"
        },
        "basic_connectives": {
          "score": 5,
          "level": "mid",
          "examples": [
            "and",
            "or"
          ]
        },
        "causal_connectives": {
          "score": 10,
          "level": "high"
        },
        "negative_connectives": {
          "score": 5,
          "level": "mid",
          "message": "Great Job! Your response used negative connectives which added contrast to your argument. See the list of some of the negative connectives used.",
          "examples": [
            "or"
          ]
        },
        "pronoun_density": {
          "score": 4,
          "level": "mid"
        },
        "adverb_diversity": {
          "score": 10,
          "level": "high"
        },
        "verb_diversity": {
          "score": 10,
          "level": "high"
        }
      }
    },
```

{% endcode %}
