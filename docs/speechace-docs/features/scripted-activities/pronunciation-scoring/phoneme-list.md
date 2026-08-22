> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](phoneme-list.md).

# Phoneme list

{% hint style="info" %}
Use [Score Text/Phoneme List](../../../api-reference/score-phone-list.md) for scoring a phonetic list.
{% endhint %}

The Speechace API also offers a function to score words that are concocted using a string of phonemes. As an example, consider the word "Gotcha," which is not a true dictionary word but is widely used in American vernacular. Such a word can be scored using the [Score Text/Phoneme List](../../../api-reference/score-phone-list.md) function which accepts a text string in the form of a list of phonemes along with an audio file and provides a quality score based on how closely the pronunciation in the audio file matches the phoneme list.
