> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](describe-image.md).

# Describe Image

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function to score describe image style questions.
{% endhint %}

In order to score describe image style questions, developers can pass a description of the image along with an audio file response from a user and the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function will automatically assess how closely the audio describes description of the image.&#x20;

The image description provided to the API is used for guidance and not for pattern matching and instead, the task achievement AI looks for semantic similarity between the user's response and the image description. Semantic matching allows the user to get a high score even if they speak in their own words and their answer does not exactly resemble the image description string.

Let us review the below example to illustrate scoring a user's response to a prompt wherein the user is asked to describe an image:

**Image description:**\
“The image shows a busy street in a city. There are people walking on the sidewalk, and some are waiting at a bus stop. In the background, I can see tall buildings, and there’s a bright blue sky. A few cars are driving by, and it looks like a sunny day.”

**User's Response:**

<details>

<summary>W<strong>here task is achieved:</strong></summary>

"The image depicts a bustling city street filled with activity. People are walking briskly along the sidewalks, some chatting with friends while others are engrossed in their phones. At a nearby bus stop, a small crowd is waiting, checking schedules and exchanging quick conversations.

In the background, tall skyscrapers rise against a clear blue sky, their glass facades reflecting the sunlight. Cars and buses navigate the road, creating a dynamic scene of urban life. Street vendors are set up along the curb, offering snacks and drinks, adding to the vibrant atmosphere. Colorful banners and advertisements hang from buildings, contributing to the lively energy of the city. Overall, the image captures the essence of a busy urban environment, full of movement and life."

</details>

<details>

<summary><strong>Where task is not achieved:</strong></summary>

Aliens have long captivated human imagination, inspiring countless stories, theories, and scientific inquiries. These extraterrestrial beings are often depicted as intelligent life forms from distant planets, sparking debates about the possibility of life beyond Earth. In popular culture, aliens are portrayed in various ways, from benevolent visitors seeking to share knowledge to malevolent invaders threatening humanity. The fascination with aliens extends to scientific exploration, with initiatives like the Search for Extraterrestrial Intelligence (SETI) actively seeking signals from other civilizations. As technology advances and our understanding of the universe deepens, the question remains: are we alone, or do other life forms exist in the vast cosmos?

</details>

***

If the user utters a relevant response as above, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an irrelevant response, then based on the [API](../../../api-reference/score-task-task-achievement.md), <mark style="color:red;">**zero score**</mark> can be assigned to the user and developer's can create a detailed report as shown below to communicate the user's failure. **Please note that while the language scores can be non-zero, the task achievement score can be zero.**

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.
