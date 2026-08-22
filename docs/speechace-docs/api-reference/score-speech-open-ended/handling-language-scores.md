> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-language-scores.md).

# Handling language scores

Language scores are derived from transcribing free speaking audio and scoring the response in key aspects such as:

* [Fluency](../score-text-fluency.md)
* [Pronunciation](../score-text-pronunciation.md)
* [Grammar](per-metric-feedback/grammar-metrics.md)
* [Vocabulary](per-metric-feedback/vocabulary-metrics.md)
* [Coherence](per-metric-feedback/coherence-metrics.md)

These overall scores help the test creator assess the overall quality of the response in terms of five different [rubrics](../../guides-on-common-topics/scoring-rubrics.md):

* A `speechace_score` on a scale of 0 to 100
* An `ielts_score` on a standard IELTS scale of 0 to 9.0
* A `pte_score` on a standard PTE scale or 10 to 90
* A `cefr_score` on a standard scale of A0 to C2
* A `toeic_score` on a standard scale of 0 to 200

The following example snippet from the API results demonstrates the overall scores:

{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "speech_score":
  {
    "transcript": "But the residents have felt the strain, they to launched a healthy streets program, opening up, select streets to just walking and cycling. Now, this action proved valuable in helping residents life and broaden the benefit of their tax dollars. That typically pay to serve cars. New designs were implemented on South Congress. The iconic Main Street of Texas, Inn, Downtown Austin, the stretch of road has changed character overtime evolve e with advances in technology Civic priorities or public preferences with City council's Direction. This stretch of road now has just two fewer Lanes of car traffic. A third of the street space was given over to people bicycling and rolling on scooters. Taking them off the busy sidewalks better suited for dining under the oak trees and give them increased comfort and safety.",
    "relevance": { "class": "TRUE" },
    "ielts_score":
    {
        "pronunciation": 8.5,
        "fluency": 9,
        "grammar": 8.5,
        "coherence": 9,
        "vocab": 9,
        "overall": 9
    },
    "pte_score":
    {
        "pronunciation": 86,
        "fluency": 87,
        "grammar": 86,
        "coherence": 90,
        "vocab": 89,
        "overall": 87
    },
    "speechace_score": 
    {
        "pronunciation": 97,
        "fluency": 98,
        "grammar": 97,
        "coherence": 100,
        "vocab": 99,
        "overall": 98
    },
    "toeic_score":
    {
        "pronunciation": 190,
        "fluency": 200,
        "grammar": 190,
        "coherence": 200,
        "vocab": 200,
        "overall": 200
    },
    "cefr_score": 
    {
        "pronunciation": "C2",
        "fluency": "C2",
        "grammar": "C2",
        "coherence": "C2",
        "vocab": "C2",
        "overall": "C2"
    }
     ...
  }
}
```

{% endcode %}
