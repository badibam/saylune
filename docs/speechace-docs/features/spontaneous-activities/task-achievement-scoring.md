> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](task-achievement-scoring.md).

# Task achievement scoring

{% hint style="info" %} <mark style="color:blue;">**This API is only available by invitation. Please email**</mark> <mark style="color:blue;"></mark><mark style="color:blue;"><contact@speechace.com></mark> <mark style="color:blue;"></mark><mark style="color:blue;">**to request access.**</mark>
{% endhint %}

The Speechace task achievement scoring API assesses the completeness and comprehensiveness of the user's response, ensuring that the user not only understood the question prompt but also communicated a clear, cohesive and accurate answer. As an example, when a speaker is tasked with analyzing conclusions from a business chart, it’s crucial to assess not only if the user's response is on-topic but also how correctly the key learnings from the business chart were described.

In addition to providing a task achievement score, this API also provides language scores available under the open-ended scoring API, including **pronunciation**, **fluency**, **vocabulary**, **grammar**, and **coherence**.

Note that the Task score is independent of language scores. To receive a high task score, the speaker must comprehend the question fully and provide a response that addresses every aspect of the question. Also, note that the task score is continuous and therefore a partial response receives a partial score.

While the task achievement technology can score a range of different tasks, we have tried to create a few well defined classes of tasks that are most likely to be used by developers. These classes are:

* **Describe-Image:** The speaker is presented with an image and asked to describe the details, relationships, and conclusion to be drawn from elements of the image. The task score of this type is on the scale of 0-5.
* **Retell-Lecture:** The speaker listens to a 1-2 minute lecture and is asked to summarize the lecture focusing on key elements, concepts and conclusions from the lecture. The task score of this type is on the scale of 0-5.
* **Answer-Question:** The speaker is presented with a short question which typically requires a one or two word answer. The task score of this type is either 0 or 1.

These classes of tasks are very useful in mocking **PTE** practice assessments. In the next few sections we will look at these task-types in detail.
