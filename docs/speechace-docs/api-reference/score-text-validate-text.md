> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-text-validate-text.md).

# Score Text/Validate Text

{% hint style="info" %}
**Run in Postman:** [Validate text](https://docs.speechace.com/#00742f28-ea92-44ba-b174-631c4d854642)
{% endhint %}

This API should be used to ensure that all words in the speaker's response are included in the Speechace lexicon. This API enables quick verification of whether authored content can be accurately scored by Speechace, helping to prevent errors during the text authoring process.

Words not found in the lexicon can be reported to `support@speechace.com` for potential inclusion. Alternatively, the [phoneme list API](../features/scripted-activities/pronunciation-scoring/phoneme-list.md) can be utilized to handle out-of-lexicon terms.

Additionally, the Speechace API offers an option to [automatically manage unknown words](../guides-on-common-topics/automatic-handling-of-unknown-words.md) by determining the most likely phonetic mapping for each term.

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` `https://api.speechace.co/api/scoring/text/v9/json`

{% tabs %}
{% tab title="Validate text with unknown words" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g --request POST 'https://api.speechace.co/api/validating/text/v9/json?key={{speechacekey}}&text=%22Validate%20these%20words%20existeee.%22&dialect=en-us'
```

{% endcode %}
{% endtab %}

{% tab title="Validate text with known words" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g --request POST 'https://api.speechace.co/api/validating/text/v9/json?key={{speechacekey}}&text=%22Validate%20these%20words%20exist.%22&dialect=en-us'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="124">Parameter</th><th width="97">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API key issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the dialect in which the speaker will be assessed. Supported only for en-us and en-gb.</em></td></tr><tr><td>text</td><td>String</td><td><em>A sentence or sequence of words to validate. For example:</em><br><em>- With unknown words:</em> <code>Validate these words existeee.</code><br>- <em>With known words:</em> <code>Validate these words exist.</code></td></tr></tbody></table>

### Response Example

If unknown words are found, the `error_unknown_words` [error](../getting-started/error-handling/common-errors.md) will be highlighted, accompanied by a detailed explanation provided in the `detail_message` parameter.&#x20;

{% tabs %}
{% tab title="Error: Validate text with unknown words" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "error",
  "short_message": "error_unknown_words",
  "detail_message": "existeee"
}
```

{% endcode %}
{% endtab %}

{% tab title="200: OK Validate text with known words " %}
{% code lineNumbers="true" %}

```json5
{
  "status": "success",
  "quota_remaining": -1
}
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Interpret the response

If any unknown word is found, it will be highlighted under the key `detail_message` with the error = `error_unknown_words`.
