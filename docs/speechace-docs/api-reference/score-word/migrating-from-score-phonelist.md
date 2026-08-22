> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](migrating-from-score-phonelist.md).

# Migrating from Score PhoneList

The Score Word API can be a complete replacement for using [Score Phonelist](../score-phone-list.md). Score Word is the recommended API for scoring single words (whether it's a dictionary word, or a custom non-word)./&#x20;

### Converting Syntax

Score Word API allows the use of [arpa\_markup](../../guides-on-common-topics/markup-language.md) to express exact phonetic sequence. In addition, Score Word does not require stress notation or syllable boundaries. This makes converting syntax from PhoneList to Word trivial.

<table><thead><tr><th width="195.96484375">Example</th><th width="256.9765625">score phone_list syntax</th><th>score word syntax</th></tr></thead><tbody><tr><td>The word: <strong>gotcha</strong></td><td><code>phone_list="g|ao|ch|ah"</code></td><td><code>word="[gotcha]{g ao ch ah]"</code></td></tr><tr><td>The word: <strong>photographer</strong><br>* no stress or syllable boundaries</td><td><code>phone_list="f|ah|t|aa|g|r|ah|f|er"</code></td><td><code>word="[photographer]{f ah t aa g r ah f er}"</code></td></tr><tr><td>The word: <strong>photographer</strong><br>* with stress and syllable boundaries </td><td><code>phone_list="f|ah|t|aa|g|r|ah|f|er"</code></td><td><code>word="[pho|tog|ra|pher]{f ah0|t aa1 g|r ah0|f er0"</code></td></tr></tbody></table>

{% hint style="info" %}
In the second example, while it is possible to score a long multi-syllable word such as "photographer" with no stress or syllable boundaries, we recommend using stress and syllable boundaries. The information assists improving scoring especially for longer multi-syllable words.
{% endhint %}
