> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](language-scoring.md).

# Language scoring

{% hint style="info" %}
Use [Score Speech/Open-ended](../../../api-reference/score-speech-open-ended.md) for scoring language skills for open-ended responses.
{% endhint %}

Speechace API's open-ended scoring capability provides feedback on the entire range of language skills including **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** scaled to a variety of rubrics such as CEFR, IELTS, PTE, TOEFL and TOEIC. Additionally the API provides a transcript of the user's response.

To experience the language scores, please review the following demo speaking test that has been created using the Speechace API: [Speaking test for software developers](https://speak.speechace.co/placement/courses/91/quizzes/438/speakingTest/1) . The test has 3 open ended questions and a sample report is available here: [Speaking test report for Jeevan Chopra](https://speak.speechace.co/placement/p/speaking-test/report/a497b39391/) . The report presents the following sections of interest:

#### **Overall summary of scores**

In the overall summary, scores are automatically provided in every major English evaluation rubric. Furthermore, scores are partitioned in to Pronunciation, Fluency, Vocabulary and Grammar scores.

#### **Per question scores**

As can be observed, the report provides feedback on the user’s response to each question incuding the user's transcript and the pronunciation, fluency, vocabulary, grammar and cohesion scores for each question.

Such a speaking test app can be built by using the [Score Speech/Open-ended](../../../api-reference/score-speech-open-ended.md) function in the Speechace API.
