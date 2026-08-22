> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-overall-scores.md).

# Handling overall scores

The **overall score** provides an assessment of the pronunciation quality for the spoken word or sentence. This numerical score indicates how closely the pronunciation matches the expected standard.&#x20;

Refer to the JSON example below to see how these key elements appear in the response:

{% code lineNumbers="true" %}

```json
"ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 90
    },
    "speechace_score": {
      "pronunciation": 100
    },
    "toeic_score": {
      "pronunciation": 200
    },
    "cefr_score": {
      "pronunciation": "C2"
    }
```

{% endcode %}

{% hint style="info" %}
Refer to the scoring [guide](../../guides-on-common-topics/interpreting-overall-scores.md) to interpret the scores.<br>
{% endhint %}
