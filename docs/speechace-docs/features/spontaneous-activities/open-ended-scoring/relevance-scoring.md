> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](relevance-scoring.md).

# Relevance scoring

{% hint style="info" %}
Use [Score Speech/Relevance](../../../api-reference/score-speech-relevance.md) for scoring the relevance of the response.
{% endhint %}

The relevance function of the Speechace API is quite unique. While passing an open-ended spontaneous audio file to the Speechace API, developers can also pass in the question prompt provided to the user. The Speechace API's relevance function will then automatically evaluate if the user's response in the audio file is relevant to the question prompt or not. If the response is found to be irrelevant then developers can award a score of 0 to the user's response. This functionality is immensely helpful in building trust with end users as they cannot cheat your spoken language assessment by speaking any random content.

{% hint style="info" %}
Note that relevance scoring does not require any training on possible answers to a prompt. The AI has been built to identify correct answers for any question automatically.
{% endhint %}

To illustrate use of the relevance metric, let’s consider an example where two responses to an open-ended question are assessed for relevance.

**Example Activity:**

1. **Question Prompt**: “What are the benefits of learning a new language?”
2. **Probable Responses:**
   * **Relevant Response**:\
     “Learning a new language has numerous benefits. First, it significantly improves communication skills. Being able to converse in another language allows individuals to connect with a wider range of people, fostering better relationships and understanding across cultures. Additionally, it opens up job opportunities. In today’s globalized world, many employers seek candidates who can communicate in multiple languages, as this can enhance business relations and customer service. Overall, the advantages extend beyond personal growth to professional opportunities.”
   * **Irrelevant Response**:\
     “I like pizza because it’s tasty. There are so many types of pizza, from pepperoni to veggie. My favorite is a Margherita with fresh basil. Pizza is great for parties and gatherings, and it’s so easy to order. Plus, you can customize it however you want, which makes it even better.”

If the user utters a relevant response as above, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an irrelevant response, then based on the [API](../../../api-reference/score-speech-open-ended.md), <mark style="color:red;">**zero score**</mark> can be assigned to the user and developer's can create a detailed report as shown below to communicate the user's failure:

Such functionality can be built by utilizing the [Score Speech/Relevance](../../../api-reference/score-speech-relevance.md) function of the Speechace API.
