> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](custom-pronunciations.md).

# Custom pronunciations

{% hint style="info" %}
Use  [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) for custom pronunciation scoring.
{% endhint %}

While measuring the pronunciation score, the Speechace engine automatically breaks down the read-aloud target script in to syllables and phonemes using a large language lexicon. However from time to time, developers run in to situations wherein the syllable/phoneme breakup provided by the Speechace API is ambiguous and to resolve such situations, the Speechace API includes a feature that allows developers to prescribe the exact syllable and phoneme breakup they desire. This feature is particularly useful in the following teaching scenarios and can be built by leveraging the [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) function:

1. **Teaching pronunciation of special acronyms, numbers, or terms -** Consider the below demo wherein user is asked to say: [Agent Double O 7 worked for M I 6](https://speak.speechace.co/placement/p/markup_demo01/courses/4960/quizzes/8886/38809)\
   \
   In this example, the user can say "Agent 0 0 7 worked for M I 6" or the user can say "Agent Double O 7 worked for M I 6", the latter being the more stylish way of speaking.\
   \
   Here a developer can use the Score Text Markup Language function to prescribe the desired pronunciation of the word 007 as Double O 7 to the Speechace API and then the Speechace API will only give a good pronunciation score if the user says Double O 7. If the user instead says zero, zero, 7 then they will not get a good score from the Speechace API.<br>

   <br>

2. **Teaching correct pronunciation of Heteronyms -** In some languages such as English, there are words called heteronyms that are spelled alike but they have different meanings. As an example, consider the word "Read" which can be pronounced as ***reed*** or ***red***. \
   \
   If you use the Speechace API to score the pronunciation of the string Read then by default it will accept both ***reed*** and ***rehd*** and return syllable and phoneme breakup for the best matching pronunciation. This may not be ideal as only one of the pronunciations may actually be correct given the larger sentence. In such cases, developers can pass a hint to the Speechace API to suggest the correct pro\
   \
   You can experience this mechanism in the below demo: [Say: He read his fragments aloud.](https://speak.speechace.co/placement/p/markup_demo01/courses/4960/quizzes/8886/38808) You can try saying "He *<mark style="color:red;">**reed**</mark>* his fragments aloud" and you should see the results in the below image.  As can be observed, the word "<mark style="color:red;">read</mark>" is marked in Red and the system suggests that the "EH" sound is missing. If try to pronounce the word read as '***rehd***' and the system will mark the word "read" in Green.

3. **Teaching correct syllables and phonemes** - As mentioned in the above description, there are a few rare times when the Speechace API may not break up the string in to the best combination of syllables and phonemes. Under such circumstances, developers can specify the desired combination of syllables and phonemes using the  [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) function.\
   \
   As an example, in the following demo, the Speechace API may return the word "nothing" as a single syllable, which is incorrect. Therefore a developer may provide a hint to the Speechace API that the word "nothing" is componsed of two syllables: **noth** & **ing**:<br>

   