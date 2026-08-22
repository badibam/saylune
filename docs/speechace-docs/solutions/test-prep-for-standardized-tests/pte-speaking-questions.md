> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](pte-speaking-questions.md).

# PTE Speaking Questions

We will be recreating these PTE speaking questions using SpeechAce APIs:

* Read Aloud
* Repeat Sentence
* Describe Image
* Re-tell Lecture
* Answer Short Question
* Personal Introduction

### Read Aloud

*Objective*: This question type will assess test-taker's pronunciation, fluency, and intonation while reading a text aloud.

{% code title="Example Question" overflow="wrap" %}

```
The Pacific Ocean is the largest and deepest of the Earth's oceanic divisions. It extends from the Arctic Ocean in the north to the Southern Ocean in the south, and from the Asia and Australia in the west to the Americas in the east.
```

{% endcode %}

*Steps to recreate:*

1. **Display Passage on UI**: Provide a passage for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs:** Utilize the [Score Text/Fluency](../../api-reference/score-text-fluency.md) API to analyze the test-taker’s pronunciation, fluency, and intonation.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Pronunciation: `.text_score.speechace_score.pronunciation`
   2. Fluency : `.text_score.speechace_score.fluency`
   3. Content Score: `.text_score.fluency.overall_metrics.correct_word_count` divided by `.text_score.fluency.overall_metrics.word_count`

### Repeat Sentence

*Objective*: This question type will evaluate test-taker's ability to accurately repeat a sentence after hearing it once.

{% code title="Example Question" overflow="wrap" %}

```
It is estimated that over 500 sea turtles die as a result of plastic consumption every year.
```

{% endcode %}

*Steps to recreate:*

1. **Display Sentence's Audio on UI**: Provide audio recordings of the sentences which test-taker needs to repeat after hearing.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs:** Utilize the  [Score Text/Fluency](../../api-reference/score-text-fluency.md) API to analyze the test-taker’s pronunciation, fluency, and intonation.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Pronunciation: `.text_score.speechace_score.pronunciation`
   2. Fluency : `.text_score.speechace_score.fluency`
   3. Content Score: `.text_score.fluency.overall_metrics.correct_word_count` divided by `.text_score.fluency.overall_metrics.word_count`

### Describe Image

*Objective*: This question type will assess test-taker's ability to describe an image in detail and structure your response coherently.

{% code title="Example Question" overflow="wrap" %}

```
Describe the graph that shows the increase in internet usage over the past decade. Mention key trends and any notable changes.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display image on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the PTE score guide:

1. Content: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Pronunciation: `.speech_score.speechace_score.pronunciation`
3. Fluency : `.speech_score.speechace_score.fluency`

### Re-tell Lecture

*Objective*: This question type will evaluate test-taker's ability to summarize and articulate the main points of a lecture or talk.

{% code title="Example Question" overflow="wrap" %}

```
Listen to the following lecture on climate change and summarize the main causes and effects discussed.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display lecture on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the PTE score guide:

1. Content: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Pronunciation: `.speech_score.speechace_score.pronunciation`
3. Fluency : `.speech_score.speechace_score.fluency`

### Answer Short Question

*Objective*: This question type will assess test-taker's ability to respond briefly and accurately to simple questions.

{% code title="Example Question" overflow="wrap" %}

```
What do you call a system of government in which people vote for the people who will represent them?
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display question on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score field to measure components listed in the PTE score guide:

Overall task score: The `task_score,` specifically for Answer Short Question, is a straightforward binary evaluation where the test-taker’s performance is assessed as either correct (1) or incorrect (0), providing a clear measure of whether the task was achieved successfully.

### Personal Introduction

*Objective*: This question type will assess test-taker's ability to introduce themselves and provide relevant personal information.

{% code title="" overflow="wrap" %}

```
Introduce yourself and briefly describe your educational background, current occupation, and a personal interest or hobby.
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Relevance Check: `.speechace_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Vocabulary: `.speech_score.speechace_score.vocab`
   6. Grammar: `.speech_score.speechace_score.grammar`
