> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handing-stress-and-intonation-response.md).

# Handing stress and intonation response

The API result will include the following lexical and intonation metrics:

1. `syllable_score_list[]`
   * `stress_level`: ***Expected*** lexical stress level based on the Lexicon. Possible values ar&#x65;**,**
     * 0: unstressed
     * 1: primary stress
     * 2: secondary stress
   * `predicted_stress_level`: ***Detected*** lexical stress level based on the user audio. Possible values ar&#x65;**,**
     * 0: unstressed
     * 1: primary stress
     * 2: secondary stress
   * `stress_score`: Floating point number between 0 and 100 indicating the correctness of the user's stress level.
   * `pitch_range[]`: \[begin\_pitch, end\_pitch] - recorded for this syllable in Hertz.
2. `word_intonation_list[]`
   * `syllable_intonation_list[]`: \[pitch\_change\_from\_previous, pitch\_change\_in\_current]
     * **pitch\_change\_from\_previous:** indicates what is the pitch of a word from the previous syllable to the beginning of the current one&#x20;
       * If we can’t recognize the syllable (due to error from our side or due to user not saying the syllable), the value is `null`.&#x20;
       * If we can recognize the syllable but couldn’t determine the pitch (due to error from our side or due to user reducing the sound to unvoiced), the value is `REDUCED`.
     * **pitch\_change\_in\_current:** indicates what is the pitch of a word from the beginning to the end of the current syllable
       * This value is `null` unless one of the following cases occurs.
         * If the pitch of the syllable falls, but the starting pitch of the syllable is higher than the ending pitch of previous syllable, secondary intonation is `RISE` while primary intonation is `FALL`.&#x20;
         * If the pitch of the syllable rises, but the starting pitch of the syllable is lower than the ending pitch of the previous syllable, secondary intonation is `FALL` while primary intonation is `RISE.`
       * Possible values
         * `RISE`
         * `FALL`
         * `FLAT`
         * `REDUCED`
         * `null`

The lexical stress and the intonation results can be read as given in the example below:

{% hint style="info" %}

```json5
word = "parent"
```

{% code overflow="wrap" lineNumbers="true" %}

```json5
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
```

{% endcode %}

So if the array is `intonation = [null, FALL]` as seen above, then,

* from the previous syllable to the beginning of the current one, that is from *`some`* to *`pa`* = NULL
* from the beginning to the end of the current syllable, that is from start of *`pa`* to end of *`pa`* = FALL
  {% endhint %}

You can use the intonation array and pitch range to compare the expected intonation against the actual intonation, visualizing this as an intonation staircase. This will enhance the test-taker's reading and speaking skills.

<div align="left"></div>
