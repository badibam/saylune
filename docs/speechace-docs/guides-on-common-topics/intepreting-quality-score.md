> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](intepreting-quality-score.md).

# Intepreting quality score

The Speechace `quality_score` measures the accuracy and intelligibility of the pronunciation of a sentence, word, syllable, or phoneme on a scale of 0 to 100.

The following rubric demonstrates how to use the `quality_score` to provide student feedback:

<table><thead><tr><th width="118">Score</th><th width="95">Color</th><th>Description</th></tr></thead><tbody><tr><td>90 - 100</td><td>Green</td><td>Excellent. Native or native-like</td></tr><tr><td>80 - 90</td><td>Green</td><td>Very Good and clearly intelligible.</td></tr><tr><td>70 - 80</td><td>Orange</td><td>Good. Intelligible but with one or two evident mistakes.</td></tr><tr><td>60 - 70</td><td>Red</td><td>Fair. Possibly not intelligible with several evident mistakes.</td></tr><tr><td>0 - 60</td><td>Red</td><td>Poor and must be reattempted.</td></tr></tbody></table>

Below is a snippet of the `quality_score` code for the word "Some" within the overall response. Each element of the response, including sentences, words, syllables, and phonemes, has its own distinct `quality_score`.

{% code overflow="wrap" lineNumbers="true" %}

```json5
"word_score_list":[
  {
    "word": "Some",
    "quality_score": 100,
    "phone_score_list": [
      {
        "phone": "s",
        "stress_level": null,
        "extent": [10,27],
        "quality_score": 99.05882352941177,
        "sound_most_like": "s"
      },
      ...
    }
]
```

{% endcode %}
