> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-speech-relevance.md).

# Score Speech/Relevance

{% hint style="info" %}
**Run in Postman:** [Score Revelance of a response](https://docs.speechace.com/#2bb020ad-bf76-4d48-9460-d1884910ba04)
{% endhint %}

### Request Format

The request parameters in the cURL code below are the same as in the [Score Text/Open-ended](score-speech-open-ended.md) scoring function. The only difference is that to get relevance scores we have to pass in a `relevance_context` field which either has the question that was asked or a topic on which the user is speaking.

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'relevance_context="Describe the healthy streets program and its impact on the residents of Austin Texas."'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Response Example

The tabs below show a relevant response, an irrelevant response and a response which is too similar to the question prompt or the text provided in relevance\_context field:

{% tabs %}
{% tab title="200: OK Response" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "But the residents have felt the strain, they to launched a healthy streets program, opening up, select streets to just walking and cycling. Now, this action proved valuable in helping residents life and broaden the benefit of their tax dollars. That typically pay to serve cars. New designs were implemented on South Congress. The iconic Main Street of Texas, Inn, Downtown Austin, the stretch of road has changed character overtime evolve e with advances in technology Civic priorities or public preferences with City council's Direction. This stretch of road now has just two fewer Lanes of car traffic. A third of the street space was given over to people bicycling and rolling on scooters. Taking them off the busy sidewalks better suited for dining under the oak trees and give them increased comfort and safety.",
    "word_score_list": [....<pronunciation metrics for all words in utterance>],
    "relevance": {
      "class": "TRUE"
    },
    "ielts_score": {
      "pronunciation": 8.5,
      "fluency": 9,
      "grammar": 8.5,
      "coherence": 9,
      "vocab": 9,
      "overall": 9
    },
    "pte_score": {
      "pronunciation": 86,
      "fluency": 87,
      "grammar": 86,
      "coherence": 90,
      "vocab": 89,
      "overall": 87
    },
    "speechace_score": {
      "pronunciation": 97,
      "fluency": 98,
      "grammar": 97,
      "coherence": 100,
      "vocab": 99,
      "overall": 98
    },
    "toeic_score": {
      "pronunciation": 190,
      "fluency": 200,
      "grammar": 190,
      "coherence": 200,
      "vocab": 200,
      "overall": 200
    },
    "cefr_score": {
      "pronunciation": "C2",
      "fluency": "C2",
      "grammar": "C2",
      "coherence": "C2",
      "vocab": "C2",
      "overall": "C2"
    },
    "fluency": {....<fluency metrics for all words in utterance>}
    },
  "version": "9.2"
}
```

{% endcode %}
{% endtab %}

{% tab title="Error: response\_relevance\_false" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "Yes, I do travel today is vastly different and it used to be in the past the traveler had little idea about what to expect when they arrived at their destination. These days, the internet sucks whole world in many ways previous generation could only dream about we can instantly review destination information and make travel Arrangements. Also, in the past, people could only travel by land or Sea Foam traveling was often long and unsafe.",
    "word_score_list": [....<list of word scores>],
    "relevance": {
      "class": "FALSE"
    },
    "ielts_score": [<list of scores>],
    "pte_score": [<list of scores>],
    "speechace_score": [<list of scores>],
    "toeic_score": [<list of scores>],
    "cefr_score": [<list of scores>],
    "score_issue_list": [
      {
        "source": "overall",
        "status": "warning",
        "short_message": "response_relevance_false",
        "detail_message": "The response is not relevant to the context."
      }
    ],
    "fluency": {<list of fluency scores>},
     },
  "version": "9.0"
}js
```

{% endcode %}
{% endtab %}

{% tab title="Error: response\_too\_similar" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "In my opinion, is job satisfaction more important than salary when considering a job. Why or why not job satisfaction is more important than salary when considering a job.",
    "word_score_list": [<.....list of word scores>],
    "relevance": {
      "class": "FALSE"
    },
    "ielts_score": [<....list of scores>],
    "pte_score": [<.....list of scores>],
    "speechace_score": [<.....list of scores>],
    "score_issue_list": [
      {
        "source": "overall",
        "status": "warning",
        "short_message": "response_too_similar",
        "detail_message": "The response is too similar to the context."
      }
    ],
    "fluency": {<....list of fluency scores>},
     },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}
