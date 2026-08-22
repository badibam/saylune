> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-speech-language-detection.md).

# Score Speech/Language Detection

{% hint style="info" %}
**Run in Postman:** [Language Scores](https://docs.speechace.com/#2f536692-1645-4a14-b729-ef781e1390a1)
{% endhint %}

### Request Format

The request parameters in the cURL code below are the same as in the [Score Speech/Open-ended](score-speech-open-ended.md) or [Score Text/Pronunciation](score-text-pronunciation.md) scoring function. To detect the language following parameters can be used:

1. `detect_dialect = 1`: This parameter is used to detect the dialect of the spoken language without impacting the user's test score. This approach is more lenient, allowing the user to proceed with their score intact even if the detected dialect doesn’t match the expected one. In this case, the API output will return the potential language in which the user provided a response.
2. `enforce_dialect = 1`: This parameter is used to enforce the correct dialect, resulting in a hard error if the response is in an unexpected language. If the detected dialect is incorrect, the API will generate an error, and the developer can set the user's score to zero based on this error response.

{% tabs %}
{% tab title="Detect the dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'detect_dialect = 1'
```

{% endcode %}
{% endtab %}

{% tab title="Enforce detected dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'enforce_dialect = 1'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Response Example

The following responses illustrate two scenarios: in the first case, the language code for the detected language/dialect is reported under the key `detected_dialect`, resulting in a warning for the user. In the second case, an incorrect dialect triggers an enforced error under the key `detail_message`, preventing further action.

{% tabs %}
{% tab title="Detect the dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
    "status": "success",
    "quota_remaining": -1,
    "speech_score": {
        "transcript": "Ich liebe den Winterurlaub, weil ich die kalte Luft und den Schnee genieße.",
        "word_score_list": [....],
        "ielts_score": {...},
        "pte_score": {...},
        "speechace_score": {...},
        "toeic_score": {...},
        "cefr_score": {...},
        "fluency": {...},
        "detected_dialect": {
            "lang_id": "ge"
        },
        "asr_version": "0.4"
    },
    "version": "9.9"
}
```

{% endcode %}
{% endtab %}

{% tab title="Enforce the detected dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
    "status": "error",
    "short_message": "non_dialect_language_detected",
    "detail_message": "The audio file contains speech in 'ge' language different from dialect 'en-us'.",
    "version": "9.9"
}
```

{% endcode %}

{% endtab %}
{% endtabs %}
