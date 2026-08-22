> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](toeic-speaking-questions.md).

# TOEIC Speaking Questions

We will be recreating these TOEIC speaking questions using SpeechAce APIs:

* Read a Text Aloud
* Describe a Picture
* Respond to Questions
* Respond to a Written Prompt
* Propose a Solution
* Role Play
* Give a Presentation
* Express an Opinion
* Describe a Process
* Summarize Information

## Task-oriented Questions

### Describe a Picture

*Objective*: To evaluate the ability to describe visual information accurately and coherently.

{% code title="Example Question" overflow="wrap" %}

```
Look at the picture provided and describe what you see. Include details about the people, setting, and activities.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display image on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the TOEIC score guide:

1. Overall task score: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Coherence: `.speech_score.speechace_score.coherence`
3. Pronunciation: `.speech_score.speechace_score.pronunciation`
4. Fluency : `.speech_score.speechace_score.fluency`

### Read a Text Aloud

*Objective*: To assess pronunciation, intonation, and overall fluency in reading English text.

{% code title="Example Question" overflow="wrap" %}

```
Read the following notice aloud: 
"Due to maintenance, the elevator will be out of service until Friday."
```

{% endcode %}

*Steps to recreate:*

1. **Display Passage on UI**: Provide a passage for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the  [Score Text/Fluency](../../api-reference/score-text-fluency.md) and the [intonation](../../features/scripted-activities/lexical-stress-and-intonation.md) APIs to get the pronunciation, fluency and intonation feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEIC score guide:
   1. Intonation: `.text_score.word_intonation_list`  can be used to create intonation staircase and compare expected v/s actual intonations.
   2. Pronunciation: `.text_score.speechace_score.pronunciation`
   3. Fluency : `.text_score.speechace_score.fluency`

## Open-ended Questions

### Respond to Questions

*Objective*: To assess the ability to answer questions based on personal experiences or general knowledge.

{% code title="Example Questions" overflow="wrap" %}

```
What do you like to do in your free time? Why?
```

{% endcode %}

### Respond to a Written Prompt

*Objective*: To measure the ability to express opinions or ideas in response to a specific written scenario.

{% code title="Example Questions" overflow="wrap" %}

```
Imagine your company is planning a team-building event. What activities would you suggest? Explain why.
```

{% endcode %}

### Propose a Solution

*Objective*: To evaluate the ability to identify problems and propose feasible solutions.

{% code title="Example Question" overflow="wrap" %}

```
Your team is missing deadlines. What steps would you take to improve this situation?
```

{% endcode %}

### Role Play

*Objective*: To assess conversational skills and the ability to engage in professional interactions.

{% code title="Example Question" overflow="wrap" %}

```
You are a manager discussing a project update with your team. How would you start the conversation?
```

{% endcode %}

### Give a Presentation

*Objective*: To evaluate the ability to deliver organized, coherent presentations on a given topic.

{% code title="Example Question" overflow="wrap" %}

```
Prepare a short presentation about a recent project your team completed. Include objectives, outcomes, and lessons learned.
```

{% endcode %}

### Express an Opinion

*Objective*: To assess the ability to articulate opinions on various topics clearly and logically.

{% code title="Example Question" overflow="wrap" %}

```
Do you think remote work is more effective than working in an office? Explain your view.
```

{% endcode %}

### Describe a Process

*Objective*: To evaluate the ability to explain a process clearly and sequentially.

{% code title="Example Question" overflow="wrap" %}

```
Describe how to handle a customer complaint effectively. 
```

{% endcode %}

### Summarize Information

*Objective*: To assess the ability to listen to information and summarize it effectively.

{% code title="Example Question" overflow="wrap" %}

```
After listening to a short lecture on marketing strategies, summarize the main points presented.
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEIC score guide:
   1. Relevance Check: `.speechace_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Grammar: `.speech_score.speechace_score.grammar`
   6. Vocabulary: `.speech_score.speechace_score.vocab`
