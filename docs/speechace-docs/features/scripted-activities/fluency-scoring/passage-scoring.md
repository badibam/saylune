> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](passage-scoring.md).

# Passage scoring

{% hint style="info" %}
Use [Score Text/Fluency](../../../api-reference/score-text-fluency.md) for implementing fluency scoring activities.
{% endhint %}

Fluency scoring in scripted speech is typically done by prompting the user to recite long passages as in the following example: [Read the passage: I grew up playing football](https://app.speechace.co/placement/course/17/quiz/6/fluency/1) .&#x20;

Note that the fluency scoring function provides both fluency and pronunciation scores in a sample audio. Once the user has read the passage, the UX will show 2 interfaces: **Fluency** score and **Pronunciation** score.

As can be observed in the, in the fluency tab, we see metrics such as words spoken per minute along with marking of bad pauses and also a projected IELTS score that the speaker may have.&#x20;

On the other hand, if we switch to the pronunciation tab, we see the pronunciation score of the users along with their highlighted mistakes in Red as in the below image:<br>

Such functionality can be built by utilizing the [Score Text/Fluency](../../../api-reference/score-text-fluency.md) function in the Speechace API.
