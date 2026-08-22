> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](answer-question.md).

# Answer Question

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) to assess if a question was answered correctly.
{% endhint %}

In the "**Answer Question**" task type, the user is expected to provide a concise 1-2 word response to a short question. In this case, the developer can present a boolean result—either right or wrong—since the task score for this type will be binary, resulting in either one (correct) or zero (incorrect) score.

To illustrate how to score a user's response when asked to answer a short question, consider the following example:

**Question Prompt:** “What is the capital of France?”

**User Response:**\
“Paris.”

***

If the user utters a relevant response as below, the developers can create a detailed report that provides the user a positive score as below. Note that the Task Achievement score is full, whereas the overall score is based on fluency, pronunciation and other language skills as well.

If the user utters an ***irrelevant*** response as below, the API will give <mark style="color:red;">**zero**</mark> task score but the language scores will be non-zero and can be presented to the user as follows:

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.
