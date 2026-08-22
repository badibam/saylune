> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](word-and-sentence-pronunciation.md).

# Word and Sentence pronunciation

{% hint style="info" %}
Use [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) for word and sentence pronunciation scoring.
{% endhint %}

To better understand the word and sentence pronunciation scoring capabilities in the Speechace API, consider the demo on the following page: [Say: After lunch I like to walk in the afternoon.](https://app.speechace.co/placement/course/17/quiz/2/1)

Here the user is being asked to pronounce the sentence "After lunch I like to walk in the afternoon". Once the user speaks the sentence, the interface shows the below screen:

<div align="left"></div>

As can be observed, the user mostly got the sentence right indicated by the letters marked in <mark style="color:green;">**Green**</mark>. However some parts of the sentence were marked in <mark style="color:red;">**Red**</mark> and the exact syllable and phoneme level mistakes are highlighted to pinpoint the errors in the user's pronunciation.

Such functionality can be built by utilizing the [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) function in the Speechace API.&#x20;

The [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) function accepts the prompt string, "After lunch I like to walk in the afternoon" along with the corresponding user's audio for the prompt and returns a pronunciation quality score in the range of 0 to 100 along with a detailed breakdown of pronunciation quality of words, syllables and phonemes in the user's audio.
