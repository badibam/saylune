> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-text-multiple-choice.md).

# Score Text/Multiple choice

{% hint style="info" %}
**Run in Postman:** [Score a Multiple Choice Text](https://docs.speechace.com/#c916e0af-af9f-484a-a0dd-e9672c72e3a1)
{% endhint %}

In this example, we will match the test-taker's utterance to the closest option provided in the Multiple Choice Question and evaluate the pronunciation of the spoken word or sentence. For instance, if the multiple-choice options are:

* Apple
* Banana
* Orange

The test-taker’s response will be scored based on how closely it matches one of these options, and the pronunciation of the spoken word or sentence will be assessed for accuracy.

The request parameters which you can see in the cURL below are the same as in [Score Text/Pronunciation](score-text-pronunciation.md). Note that the options {Apple, Banana and Orange} are specified in the **text** field and are separated by newline characters (or \n).

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="\"apple
orange
banana\""'
--form 'user_audio_file=@"orang_16k.wav"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

In the above cURL, the audio file is matched to the closest choice, which is "Orange." Therefore, the response evaluates the pronunciation of the word "Orange":

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "orange",
    "word_score_list": [
      {
        "word": "orange",
        "quality_score": 77,
        "phone_score_list": [
          {
            "phone": "ao",
            "stress_level": 1,
            "extent": [
              79,
              87
            ],
            "quality_score": 99.625,
            "stress_score": 100,
            "sound_most_like": "ao"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              87,
              96
            ],
            "quality_score": 96,
            "sound_most_like": "r"
          },
          {
            "phone": "ih",
            "stress_level": 0,
            "extent": [
              96,
              105
            ],
            "quality_score": 60.33333333333334,
            "stress_score": 100,
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              105,
              117
            ],
            "quality_score": 65.08333333333334,
            "sound_most_like": "ng"
          },
          {
            "phone": "jh",
            "stress_level": null,
            "extent": [
              117,
              129
            ],
            "quality_score": 61.5,
            "sound_most_like": "g",
            "child_phones": [
              {
                "extent": [
                  117,
                  126
                ],
                "quality_score": 70.33333333333333,
                "sound_most_like": "k"
              },
              {
                "extent": [
                  126,
                  129
                ],
                "quality_score": 35,
                "sound_most_like": "ng"
              }
            ]
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "or",
            "quality_score": 98,
            "stress_score": 100,
            "extent": [
              79,
              96
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "ange",
            "quality_score": 62,
            "stress_score": 100,
            "extent": [
              96,
              129
            ]
          }
        ]
      }
    ],
    "speechace_score": {
      "pronunciation": 77
    }
  },
  "version": "9.0"
}
```
