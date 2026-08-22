> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](cefr-speaking-questions.md).

# CEFR Speaking Questions

We will be recreating these CEFR speaking questions using SpeechAce APIs:

* Personal Routine and Activities
* Debating, Negotiating and giving opinions
* Reflective, Evaluative and Persuasive

### Personal Routine and Activities

*Objective*: Measure the ability to recount personal experiences and explain their significance.

{% code title="Example Question" overflow="wrap" %}

```
1. Describe a time when you had to solve a problem.
2. Explain how to prepare your favorite dish.
3. Describe a recent movie you saw and explain why you enjoyed or did not enjoy it.
```

{% endcode %}

### Debating, Negotiating and giving opinions

*Objective*: Assess the ability to engage in expert-level discussions on specialized topics with precision and depth.

{% code title="Example Question" overflow="wrap" %}

```
1. Evaluate different approaches to addressing income inequality from an economic perspective.
2. Discuss the impact of historical events on current geopolitical dynamics.
3. Argue for or against the ethical implications of artificial intelligence in decision-making.
4. Debate the benefits and drawbacks of online education.
```

{% endcode %}

### Reflective, Evaluative and Persuasive

*Objective*: Assess the ability to reflect on and evaluate various aspects of personal and professional experiences.

{% code title="Example Questions" overflow="wrap" %}

```
1. Reflect on how your personal experiences have shaped your career goals.
2. Convince someone to support a cause or initiative you are passionate about.
3. Evaluate the role of lifelong learning in professional success and personal fulfillment.
4. How would you persuade others to adopt a new policy in your workplace?
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the CEFR score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Vocabulary: `.speech_score.speechace_score.vocab`
   3. Coherence: `.speech_score.speechace_score.coherence`
   4. Pronunciation: `.speech_score.speechace_score.pronunciation`
   5. Fluency : `.speech_score.speechace_score.fluency`
   6. Grammar: `.speech_score.speechace_score.grammar`

{% hint style="info" %}
Pro-tip: For a more comprehensive evaluation, including grammar, vocabulary, and coherence, consider using additional [APIs](../../api-reference/score-speech-open-ended/handling-language-scores.md) to assess these aspects as well.
{% endhint %}
