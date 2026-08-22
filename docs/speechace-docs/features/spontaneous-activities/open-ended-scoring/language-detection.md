> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](language-detection.md).

# Language detection

{% hint style="info" %}
Use Score[ Speechace/Language Detection](../../../api-reference/score-speech-language-detection.md) to detect unexpected languages in the user's response.
{% endhint %}

While taking AI assessments, often times users try to speak in an unexpected language to test if the system will give them a positive or negative score. To cover for such scenarios, the Speechace API includes a language detection function that can not only tell if the user spoke in a different language but can also identify the language used by the user. Developers can use this capability to develop trust in their system by identifying negative responses.

There are two ways in which the language detection can be incorporated:

1. Detect the Language and warn the user.&#x20;
2. Detect the Language and enforce the correct language.

Let’s review the following example to illustrate the warning or error the API generates when the user's response to a prompt is not in the expected language, which is English in this case:

**Question Prompt:** "Tell us something about yourself."

**User Response (in german):** "Ich bin ein begeisterter Reisender und liebe es, neue Kulturen zu entdecken. In meiner Freizeit lese ich gerne Bücher und treibe Sport. Ich interessiere mich besonders für Technologie und Innovation, und ich arbeite als Softwareentwickler in einem dynamischen Team. Außerdem genieße ich es, Zeit mit meiner Familie und meinen Freunden zu verbringen."

In situations, where the primary goal is communication rather than strict adherence to language rules, the [API](../../../api-reference/score-speech-language-detection.md) can detect the language and issue a warning without penalizing scores. In these cases, the developer can create detailed reports where scores remain unaffected as shown below:<br>

In situations where standardized testing or assessments accept only a particular language, enforcing the correct language by issuing an error helps ensure the validity of the evaluation. In these cases, the developer can create reports that reflect a zero score, penalizing the user for using the incorrect language as shown below:

Such functionality can be built by utilizing the [Speechace/Language Detection](../../../api-reference/score-speech-language-detection.md) function of the Speechace API.
