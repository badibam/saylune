> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](multiple-choice.md).

# Multiple choice

{% hint style="info" %}
Use [Score Text/Multiple choice](../../../api-reference/score-text-multiple-choice.md) for multiple choice scoring.
{% endhint %}

The multiple choice scoring capability provides an interactive way for language learners to practice pronunciation and comprehension. Using this capability, developers can create activities, wherein user is presented with a list of options to speak from and not only does the user have to speak the right option but also pronounce it correctly.

To gain a better understanding of this capability, please refer to the following demo: [How do you spell the man's name?](https://app.speechace.co/placement/course/17/quiz/4/MC/1) in which the user is being prompted to correctly spell the name as spelt in the reference audio:

As can be observed above, the user speaks the incorrect spelling of the man's name and in this case the system gives them a score of 0. This will prompt the user to make a second try as below:<br>

This time the user got the right spelling but unfortunately didn't pronounce <mark style="color:red;">**P**</mark> and <mark style="color:red;">**H**</mark> very clearly and therefore the system gives them a near perfect score of 88% but pinpoints their mistakes in Red.

This functionality can be built using the [Score Text/Multiple choice](../../../api-reference/score-text-multiple-choice.md) function in the Speechace API. This function allows developers to pass in a collection of text options along with the user's audio attempting to speak one of the options and returns the best matching option spoken by the user along with the user's pronunciation score (0-100) at word, syllable and phoneme level for that option.
