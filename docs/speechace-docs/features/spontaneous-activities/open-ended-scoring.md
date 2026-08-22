> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](open-ended-scoring.md).

# Open-ended scoring

{% hint style="info" %} <mark style="color:blue;">**This feature is available as part of the Premium Plan.**</mark>
{% endhint %}

The Speechace open-ended scoring API evaluates a user's free speech spontaneous response up to 2 minutes in length and provides a comprehensive set of language scores including **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** along with a **transcript** of the user's response.&#x20;

Additionally, the open-ended scoring API includes the following advanced capabilities to evaluate the fidelity and appropriateness of the response:\
a. **Relevance detection** - This capability evaluates if the user gave a response that was relevant to the question given to them.\
b. **Language detection** - This capability detects whether a user provided a response in an unexpected language.

In the next few sections we will look at these scores in detail.
