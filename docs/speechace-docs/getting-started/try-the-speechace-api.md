> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](try-the-speechace-api.md).

# Try the Speechace API

In this guide, sample code snippets are provided. These samples can be used to explore and understand the different types of API responses that may encountered.

## Instructions to get started with Speechace API

1. Download the following audio files:

* **apple.wav** - contains the native English pronunciation for the word apple.
* **someparents.wav** - contains the native English pronunciation for the sentence: "*Some parents admire famous athletes as strong role models, so they name their children after them*".

{% file src="/files/4zGGaPb7410tdM38uDR2" %}

{% file src="/files/FW4AjRXTUIhTCmVQkIgk" %}

2. Use the following example request to evaluate the pronunciation quality of the "Apple.wav" file available above:

{% tabs %}
{% tab title="cURL for apple.wav" %}
{% code overflow="wrap" lineNumbers="true" %}

```python
curl --form text='apple' --form user_audio_file=@/path/to/apple.wav "https://api.speechace.co/api/scoring/text/v9/json?key=Insert_Your_API_Key_Here" | python -m json.tool
```

{% endcode %}
{% endtab %}
{% endtabs %}

You can follow the [link](https://docs.speechace.com/) to try out these requests in Postman by clicking on "Run in Postman".

{% hint style="info" %}
We can also evaluate .mp3 files and many other [audio format.](pre-requisites/api-limits.md#recording-audio)
{% endhint %}

3. The expected response for above cURL command for the audio "Apple.wav", is as follows:

{% tabs %}
{% tab title="200 OK: Score a word" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "apple",
    "word_score_list": [
      {
        "word": "apple",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "ae",
            "stress_level": 1,
            "extent": [
              12,
              27
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ae"
          },
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              27,
              39
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "p"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              39,
              42
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              42,
              54
            ],
            "quality_score": 98.5,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "l"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 1,
            "stress_level": 1,
            "letters": "ap",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "extent": [
              12,
              27
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "ple",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              27,
              54
            ]
          }
        ]
      }
    ],
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
  },
  "version": "9.3"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}

[Related Guide: Pronunciation Scoring](../features/scripted-activities/pronunciation-scoring.md)
