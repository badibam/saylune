> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-text-markup-language.md).

# Score Text/Markup Language

{% hint style="info" %}
**Run in Postman:** [Scoring text using markup language](https://docs.speechace.com/#63ea84b0-b0d9-432f-a9ad-b83dff1a4d28)
{% endhint %}

Markup language allows you to override Speechace lexicon and define your own.

### Markup Syntax

When `markup_language=arpa_mark` is specified, the input text can have markups on zero, one or multiple words. This flag can be used in scoring pronunciation, multiple choice or[ Validate Text ](score-text-validate-text.md)requests.

Each markup has the pattern: `[l1|l2|...|ln]{s1|s2|...|sn}`, where:

* `l1`, `l2`, ..., `ln` are substrings of a word in the input text, and
* `s1`, `s2`, ..., `sn` are syllables of the word corresponding to `l1`, `l2`, ..., `ln`.
* Each syllable, `si`, has this pattern: `p1` `p2` ... `pk`, where `pi` is a phoneme in [ARPABET notation](../guides-on-common-topics/phonetic-notation.md) for the dialect.&#x20;
* If `pi` is a vowel phoneme, `pi` ends with 0, 1 or 2 to denote the stress level of the syllable.
* There should be at most one vowel per syllable.

{% hint style="info" %}
Let's take a word "Nothing" and divide it into its syllables and phonemes along with its stress level to understand it better:

1. **\[noth | ing]**: "Nothing" has two syllables, "noth" and "ing".
2. **{n ah1 th | ih0 ng}**: This part is a phonetic transcription where:
   1. **n**: Represents the phoneme /n/, as in "no".
   2. **ah1**: Represents the stressed vowel phoneme /ʌ/, as in "cup". The "1" indicates primary stress.
   3. **th**: Represents the phoneme /θ/, as in "think".
   4. **ih0**: Represents the vowel phoneme /ɪ/, as in "sit", with "0" indicating no stress or secondary stress.
   5. **ng**: Represents the phoneme /ŋ/, as in "sing".
      {% endhint %}

### Markup Language Use-cases

1. Marking up a word to explicitly specify syllable boundaries and phoneme mapping\
   `There was [noth|ing]{n ah1 th|ih0 ng} on the rock.`
2. Specifying which word is intended in a heteronym (i.e. 2 words which share the same spelling but have different pronunciation and meaning). *Here the heteronyms are "read" and "fragments".*\
   `He [read]{r eh1 d} his [frag|ments]{f r ae1 g|m ah0 n t s} aloud.`
3. Handling special acronyms, numbers, or terms\
   `Agent [0||||07]{d ah1 | b ah0 l | ow1 | s eh1 | v ah0 n} worked for MI6.`

{% hint style="info" %}
Note: In the above example in order to map 007 to "Double-O Seven" and no other possible pronunciation of the number "007", we create multiple empty syllables in the word "007".&#x20;

For detailed explanation of markup language, refer the Markup Language [guide](../guides-on-common-topics/markup-language.md).
{% endhint %}

### Request Response Example

We will evaluate the sentence using the markup language applied to the word "read," as demonstrated below:

{% code overflow="wrap" %}

```
I love to [read]{r iy1 d}. Last year I [read]{r eh1 d} Anna Karenina by [Tol|stoy]{t ow1 l|s t oy2}.
```

{% endcode %}

The request parameters which you can see in the cURL below can be found in [Score Text/Pronunciation](score-text-pronunciation.md).

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}' \
--form 'text="I love to [read]{r iy1 d}. Last year I [read]{r eh1 d} Anna Karenina by [Tol|stoy]{t ow1 l|s t oy2}."' \
--form 'user_audio_file=@"ilovetoread.mp3"' \
--form 'markup_language="arpa_mark"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

Notice the different phonemes for both instances of the word "read" in the sentence and compare them with the phonemes present in the response for the same words.

{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "I love to read. Last year I read Anna Karenina by Tolstoy.",
    "word_score_list": [
    {....<word score for other words},
    {
        "word": "read",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              63,
              72
            ],
            "quality_score": 99.33333333333333,
            "sound_most_like": "r"
          },
          {
            "phone": "iy",
            "stress_level": 1,
            "extent": [
              72,
              87
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "iy"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              87,
              99
            ],
            "quality_score": 100,
            "sound_most_like": "d"
          }
        ],
        "ending_punctuation": ".",
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "read",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              63,
              99
            ]
          }
        ]
      },
      {....<word score for other words},
      {
        "word": "read",
        "quality_score": 84,
        "phone_score_list": [
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              213,
              222
            ],
            "quality_score": 99.66666666666667,
            "sound_most_like": "r"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              222,
              231
            ],
            "quality_score": 55.222222222222214,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "iy"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              231,
              240
            ],
            "quality_score": 97.33333333333333,
            "sound_most_like": "d"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "read",
            "quality_score": 84,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              213,
              240
            ]
          }
        ]
      },
      {....<word score for other words},
      ],
    "ielts_score": {
      "pronunciation": 8.5
    },
    "pte_score": {
      "pronunciation": 83
    },
    "speechace_score": {
      "pronunciation": 94
    }
  },
  "version": "9.1"
}
```

{% endcode %}
