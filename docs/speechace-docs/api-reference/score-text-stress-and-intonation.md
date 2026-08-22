> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-text-stress-and-intonation.md).

# Score Text/Stress & Intonation

{% hint style="info" %}
**Run in Postman:** [Score Lexical Stress and Intonation](https://docs.speechace.com/#cb64e480-d7cd-408c-b34e-a5221292eac7)
{% endhint %}

If the `include_intonation=1` parameter is included in the request body for [Score Text/Pronunciation](score-text-pronunciation.md), then the API result will include lexical and intonation metrics. In this example, lexical (word) stress and intonation (text stress) are evaluated in a given passage.

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="Some parents admire famous athletes as strong role models, so they name their children after them."' \
--form 'user_audio_file=@"someparents.wav"' \
--form 'include_intonation="1"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

{% tabs %}
{% tab title="200: OK Lexical Stress and Intonation" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "Some parents admire famous athletes as strong role models, so they name their children after them.",
    "word_score_list": [
    {<...pronunciation metrices for other words>},
    {
        "word": "parents",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              42,
              51
            ],
            "quality_score": 100,
            "sound_most_like": "p"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              51,
              57
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "eh"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              57,
              66
            ],
            "quality_score": 100,
            "sound_most_like": "r"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              66,
              69
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              69,
              75
            ],
            "quality_score": 100,
            "sound_most_like": "n"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              75,
              76
            ],
            "quality_score": 94,
            "sound_most_like": "t"
          },
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              76,
              84
            ],
            "quality_score": 99.25,
            "sound_most_like": "s"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "pa",
            "quality_score": 100,
            "stress_score": 100,
            "intonation": [
              null,
              "FALL"
            ],
            "pitch_range": [
              226.71435643564357,
              211.65643564356435
            ],
            "predicted_stress_level": 1,
            "extent": [
              42,
              57
            ]
          },
          {
            "phone_count": 5,
            "stress_level": 0,
            "letters": "rents",
            "quality_score": 99,
            "stress_score": 100,
            "intonation": [
              null,
              "FALL"
            ],
            "pitch_range": [
              211.65643564356435,
              184.5521782178218
            ],
            "predicted_stress_level": 0,
            "extent": [
              57,
              84
            ]
          }
        ]
      },
      {<...pronunciation metrices for other words>},
      ],
    "word_intonation_list": [
      {
        "word": "Some",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "parents",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "admire",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ],
          [
            "FALL",
            "RISE"
          ]
        ]
      },
      {
        "word": "famous",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "athletes",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "as",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "strong",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "role",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "models",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "so",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "they",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "name",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "their",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "children",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ],
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "after",
        "syllable_intonation_list": [
          [
            "FALL",
            "RISE"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "them",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      }
    ],
    "ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 89
    },
    "speechace_score": {
      "pronunciation": 99
    }
  },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}
