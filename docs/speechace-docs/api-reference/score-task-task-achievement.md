> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](score-task-task-achievement.md).

# Score Task/Task Achievement

{% hint style="info" %}
**Run in postman:** [Score Task Achievement](https://docs.speechace.com/#07bbedef-8c33-48dc-b84f-6e94ebb3fc80)
{% endhint %}

The Speechace Task Achievement API supports following task types:

* **Describe-Image:** The speaker is presented with an image and asked to describe the details, relationships, and conclusion to be drawn from elements of the image.
* **Retell-Lecture:** The speaker listens to a 1-2 minute lecture and is asked to summarize the lecture focusing on key elements, concepts and conclusions from the lecture.
* **Answer-Question:** The speaker is presented with a short question which typically requires a one or two word answer.

Each task type has particular input and outputs:

<table><thead><tr><th width="170">Task Type</th><th width="332">Inputs</th><th>Outputs</th></tr></thead><tbody><tr><td>describe-image</td><td><code>task_context</code>: A model description of the image which is presented to the speaker.<br>Max length: 1024 chars.</td><td>Task score on scale of 0-5.</td></tr><tr><td>retell-lecture</td><td><code>task_context</code>: A model summary of the lecture which is presented to the speaker.<br>Max length: 1024 chars.</td><td>Task score on scale of 0-5.</td></tr><tr><td>answer-question</td><td><code>task-question</code>: The question presented to the user.</td><td>Task score on scale of 0-1 where 0 is incorrect and 1 is correct.</td></tr></tbody></table>

The API supports different modes in combining task scores and language scores in assessment:

1. `user_audio_file` or `user_audio_text`:  The speaker's response can be submitted as either audio or text, allowing task scoring to be used with written responses as well.
2. &#x20;`include_speech_score`: Speech scoring can be included or excluded along with the task score. Note that if `user_audio_text` is used, the `include_speech_score` will always be zero. Therefore, in written responses, only task scores are provided.

All tasks are available in the following languages:

* English (en-us, en-gb)
* Spanish (es-es, es-mx)
* French (fr-fr, fr-ca)

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` [`https://api.speechace.co/api/scoring/task/v9/json`<br>](<https://api.speechace.co/api/scoring/task/v9/json&#xA;>)

{% tabs %}
{% tab title="Example with audio" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/task/v9/json?key={{speechace_premiumkey}}&task_type=describe-image&dialect=en-us' \
--form 'task_context="This bar chart illustrates the declining trend related to the percent of U.S. workforce engaged in farm labor in the 19th century. In 1840, for example, around 69% of the U.S. workforce was engaged in farm labor; in 1860, almost 60% of the U.S. workforce was engaged in farm labor; in 1880, only 50% of the U.S. workforce was engaged in farm labor; and in 1900, less than 40% of the U.S. workforce was engaged in farm labor."' \
--form 'user_audio_file=@"barchataudiofile.mp3"' \
--form 'include_speech_score="1"'
```

{% endcode %}
{% endtab %}

{% tab title="Example with text" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/task/v9/json?key={{speechace_premiumkey}}&task_type=describe-image&dialect=en-us' \
--form 'task_context="This line graph illustrates the overall behaviour of France’s national debt in the period 1995-2011, calculated in comparison to the country’s GDP (Gross Domestic Product). Thus, in 1995, France’s national debt was equivalent to about 56% of its GDP; between 1996 and 1997, the debt rose to a little more than 60% of the country’s GDP, dropping to a little less than 60% between the years 2000 and 2001; however, it started to rise again in 2002, reaching almost 70% of the country’s GDP in 2005; unfortunately, between 2009 and 2010, the debt had reached around 85% of France’s GDP, reaching roughly 88% by 2011."' \
--form 'user_audio_text="This is a beautiful image infront of me with a chart depicting many colors and numbers. I can see 1995, 1996, 1997, 1998, 1999 and France'\''s national debt."' \
--form 'include_speech_score="0"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="136">Parameter</th><th width="117">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>in which the speaker will be assessed. Supported values are: en-us, en-gb, fr-fr, fr-ca, es-es, es-mx.</em></td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier (generated by your applications) for the end-user who spoke the audio.</em></td></tr><tr><td>task_type</td><td>String</td><td><p><em>The task_type to score. Supported types are:</em> </p><ul><li><em>describe-image</em></li><li><em>retell-lecture</em></li><li><em>answer-question.</em></li></ul></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="169">Parameter</th><th width="100">Type</th><th>Description</th></tr></thead><tbody><tr><td>task_context</td><td>String</td><td><p><em>The context or model or model answer for the task presented to the speaker.</em></p><p><em><strong>Used in the following task-types:</strong></em> </p><ul><li><em><strong>describe-image: a model description of the image</strong></em> </li><li><em><strong>retell-lecture: a model description of the lecture</strong></em></li></ul><p><em>This must be provided in the same language as the one being assessed.</em></p></td></tr><tr><td>task_question</td><td>String</td><td><p><em><strong>The task question presented to the speaker, used in task-type = answer-question.</strong></em></p><p><em>This must be provided in the same language as the one being assessed.</em></p></td></tr><tr><td>user_audio_file</td><td>File</td><td><em>file with user audio (wav, mp3, m4a, webm, ogg, aiff)</em></td></tr><tr><td>include_speech_score</td><td>String</td><td><ul><li><em>Set to</em> <code>1</code><em>, to include scoring other aspects of the speech: Pronunciation, Fluency, Grammar, Vocab, Coherence.</em> </li><li><em>Set to</em> <code>0</code> <em>if you only want to receive the task score only.</em></li></ul></td></tr><tr><td>user_audio_text</td><td>String</td><td><p><em>A text transcript of the speaker's response.</em> </p><ul><li><em>Use this field instead of <code>user_audio_file</code> if you already have a transcript of the user's response and do not wish to re-transcribe an audio.</em></li><li><em>Note: In this case, you will only be able to receive an overall <code>task_score.</code></em></li></ul></td></tr></tbody></table>

### Response Example

Notice the `task_score.score` key for the overall task achievement score in the response below:

{% tabs %}
{% tab title="Task = describe-image" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "describe-image",
    "version": "0.1",
    "score": 4,
    "transcript": "This bar graph shows the percent of US workforce engaged in farm labor, and that's data from 1840 to 1900. Ear now starting with 1840, the percentage was 70 percentage. After that there is a gradual decrease in the number of workforce engaged in farm labor to 60 percentage in 1860 and further down to 18 around 50% in 1880. And then in 1900 it decreased to 40 percentage. Overall, there is a continuous decrease in the engagement in the farm sector."
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "This bar graph shows the percent of US workforce engaged in farm labor, and that's data from 1840 to 1900. Ear now starting with 1840, the percentage was 70 percentage. After that there is a gradual decrease in the number of workforce engaged in farm labor to 60 percentage in 1860 and further down to 18 around 50% in 1880. And then in 1900 it decreased to 40 percentage. Overall, there is a continuous decrease in the engagement in the farm sector.",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {
      "pronunciation": "B2",
      "fluency": "B2",
      "grammar": "B1+",
      "coherence": "B1",
      "vocab": "B1+",
      "overall": "B1+"
    },
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
}
```

{% endcode %}
{% endtab %}

{% tab title="Task = retell-lecture" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "retell-lecture",
    "version": "0.1",
    "score": 2,
    "transcript": "The lecture was about the ecosystem. The lecture said that ecology is the study of living organisms in an environment. The lecturer also said that there are two factors in an ecosystem. The first one is biotic which is considered as the living things. The second one is abiotic which is considered as the non living things in the environment. The biotic factors is considered as the primary producers. Herbivores, carnivores, omnivores and detritivores. However, the abiotic factors."
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "The lecture was about the ecosystem. The lecture said that ecology is the study of living organisms in an environment. The lecturer also said that there are two factors in an ecosystem. The first one is biotic which is considered as the living things. The second one is abiotic which is considered as the non living things in the environment. The biotic factors is considered as the primary producers. Herbivores, carnivores, omnivores and detritivores. However, the abiotic factors.",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr score>},
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
}
```

{% endcode %}
{% endtab %}

{% tab title="Task = answer-question" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "answer-question",
    "version": "0.1",
    "score": 1,
    "transcript": "A democracy?"
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "A democracy?",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr score>}
    ],
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
} 
```

{% endcode %}
{% endtab %}
{% endtabs %}

The [pronunciation](score-text-pronunciation.md) and [fluency](score-text-fluency.md) interpretation of the key elements in the response of the spoken word or sentence remains the same.

The new addition is the task score parameters, which indicate the extent to which the task has been achieved.&#x20;

<div align="left"></div>

{% hint style="info" %}
**Difference between `task_context` and `relevance_context`**

* [**Relevance**](score-speech-relevance.md) is binary and is higher level. It evaluates whether the response is on-topic or not (True or False)
* [**Task Achievement**](score-task-task-achievement.md) is more nuanced and scores how well the response addresses the task

For a general question such as "Do you think the government should subsidize healthcare?" relevance is primarily assessed, as there is no definitive right or wrong answer; the focus is on whether the response is on topic.

In contrast, for a specific question like "What does the following business chart tell us?" a specific answer is expected. Therefore, a nuanced task context and detailed task score are required to evaluate how well the response addresses the specific elements of the task.
{% endhint %}
