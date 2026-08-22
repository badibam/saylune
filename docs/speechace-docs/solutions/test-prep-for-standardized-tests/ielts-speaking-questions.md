> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](ielts-speaking-questions.md).

# IELTS Speaking Questions

We will be recreating these IELTS speaking questions using SpeechAce APIs:

* Introduction and Interview
* Descriptive/Narrative/Opinion-Based Tasks
* Discussions

### Introduction and Interview

*Objective*: This question type will assess test-taker's ability to discuss their background, experiences, and daily life.

{% code title="Example Questions" overflow="wrap" %}

```
1. Can you tell me about your hometown?
2. Do you prefer to work or study alone or with others?
3. Have you ever had a memorable experience at a festival?
```

{% endcode %}

### Descriptive/Narrative/Opinion-Based Tasks

*Objective*: This question type will assess test-taker's ability to discuss and reflect on their opinions and viewpoints.

{% code title="Example Question" overflow="wrap" %}

```
1. Describe a book or film that has had an impact on your life.
2. Talk about a technology you use frequently and how it has affected your daily life.
```

{% endcode %}

### Discussions

*Objective*: This question type will assess test-taker's ability to compare and evaluate different aspects of a topic or situation.

{% code title="Example Question" overflow="wrap" %}

```
1. How does the importance of technology in education compare to its impact on social interactions?
2. Compare the benefits and drawbacks of online learning versus traditional classroom learning.
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the IELTS score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Vocabulary: `.speech_score.speechace_score.vocab`
   6. Grammar: `.speech_score.speechace_score.grammar`
