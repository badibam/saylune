> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](fidelity-detection.md).

# Fidelity detection

One of the unique capabilities of the fluency scoring function is that it can detect incomplete or off-script attempts where the speaker deviates from the intended utterance.&#x20;

If fidelity detection is enabled and the test taker deviates from reading the passage by not fully adhering to the test script or spoke freely instead of following the scripted prompts then the API will reduce the overall scores and return a `score_issue_list[]` entry such as the below:

{% hint style="info" %}
Please review additional details on [interpreting fidelity scores](../../guides-on-common-topics/interpreting-fidelity-class.md).
{% endhint %}

{% code overflow="wrap" lineNumbers="true" %}

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

{% endcode %}
