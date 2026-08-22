> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](toefl-speaking-questions.md).

# TOEFL Speaking Questions

We will be recreating these TOEFL speaking questions using SpeechAce APIs:

* Independent Speaking Tasks
* Integrated Speaking Tasks
* Summary and Synthesis Tasks
* Academic Discussion Tasks

## Open-ended Questions

### Independent Speaking Tasks

*Objective*: To evaluate the test-taker’s ability to express personal opinions, preferences, and experiences on familiar topics.

{% code title="Example Question" overflow="wrap" %}

```
1. Do you prefer studying alone or with others? Why?
2. Do you agree or disagree with the statement that technology has made our lives more complicated? Use specific examples to support your answer.
3. If you had the choice between taking a year off from school or starting work immediately after graduation, which would you choose and why?
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEFL score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`

## Task-oriented Questions

### Integrated Speaking Tasks

*Objective*: To assess the test-taker’s ability to integrate and respond to information from multiple sources, such as reading and listening.

{% code title="Example Questions" overflow="wrap" %}

```
Example-1
- Reading Passage: A brief article about a new university policy.
- Listening Passage: A conversation between two students discussing their views on the policy.
- Task: "Summarize the policy and explain the students’ opinions about it."

Example-2
- Listening Passage: A lecture about environmental conservation.
- Task: "Summarize the main points of the lecture and explain how they relate to the concept of sustainability."

Example-3
- Reading Passage: An article about a proposed change in campus facilities.
- Listening Passage: A conversation between students about their views on the change.
- Task: "Discuss the proposed change and the students' opinions, then give your own opinion about it."
```

{% endcode %}

### Summary and Synthesis Tasks

*Objective***:** To test the ability to accurately summarize and synthesize information from different sources.

<pre data-title="Example Question" data-overflow="wrap"><code>Example-1
- Reading Passage: A text on a particular scientific discovery.
- Listening Passage: A professor’s explanation of the discovery's applications.
- Task: "Summarize the main points of the reading passage and the professor’s explanation."

Example-2
<strong>- Reading Passage: An article comparing two theories.
</strong>- Listening Passage: A discussion between experts about the theories.
- Task: "Compare the two theories discussed in the reading and explain the experts' views on them."
</code></pre>

### Academic Discussion Tasks

*Objective***:** To evaluate the ability to engage in discussions about academic topics.

{% code title="Example Questions" overflow="wrap" %}

```
Example-1
- Listening Passage: A university lecture about climate change.
- Task: "Discuss the key points of the lecture and explain their implications for future research."

Example-2
- Listening Passage: A discussion on environmental issues.
- Task: "Describe the environmental problems mentioned in the discussion and propose possible solutions."
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the TOEFL score guide:

1. Overall task score: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Coherence: `.speech_score.speechace_score.coherence`
3. Pronunciation: `.speech_score.speechace_score.pronunciation`
4. Fluency : `.speech_score.speechace_score.fluency`

{% hint style="info" %}
Pro-tip: For a more comprehensive evaluation, including grammar, vocabulary, and coherence, consider using additional [APIs](../../api-reference/score-speech-open-ended/handling-language-scores.md) to assess these aspects as well.
{% endhint %}
