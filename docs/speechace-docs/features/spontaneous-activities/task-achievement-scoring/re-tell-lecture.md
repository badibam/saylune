> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](re-tell-lecture.md).

# Re-tell Lecture

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) to score how well the user summarizes an audio lecture.
{% endhint %}

In the "**Re-tell Lecture**," task type, the user is evaluated on their ability to summarize the lecture accurately in their own words. This assessment would focus on how well the user captures the main points, conveys key ideas, and presents them clearly and coherently.

In order to score describe retell-lecture style questions, developers can pass a summary of the lecture along with an audio file response from a user and the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function will automatically assess how closely the audio describes lecture.

The lecture summary provided to the API is used for guidance and not for pattern matching and instead, the task achievement AI looks for semantic similarity between the user's response and the lecture summary. Semantic matching allows the user to get a high score even if they speak in their own words and their answer does not exactly resemble the lecture summary string.

Let us review the below example on how to leverage the task achievement function to score a re-tell lecture style question:

**Question Prompt:** “Please summarize the main points of the lecture.”

**Lecture:**

<details>

<summary><strong>Lecture Title: The Importance of Renewable Energy Sources</strong></summary>

**Introduction:** Good morning, everyone. Today, we will discuss the significance of renewable energy sources and their role in creating a sustainable future. As the world faces the challenges of climate change and dwindling fossil fuel reserves, it is crucial to explore alternatives that can power our society without harming the planet.

**Main Points:**

1. **Understanding Renewable Energy:** Renewable energy comes from natural sources that are constantly replenished, such as sunlight, wind, rain, tides, and geothermal heat. Unlike fossil fuels, which can take millions of years to form, renewable resources are sustainable and can be harnessed indefinitely.
2. **Benefits of Renewable Energy:**
   * **Environmental Impact:** One of the most significant advantages of renewable energy is its potential to reduce greenhouse gas emissions. By transitioning to solar, wind, and other renewable sources, we can decrease our carbon footprint and combat climate change.
   * **Economic Benefits:** Investing in renewable energy creates jobs in manufacturing, installation, and maintenance. Additionally, renewable sources can lead to lower energy costs over time, as they rely on free natural resources.
   * **Energy Independence:** Utilizing renewable energy can reduce a country’s reliance on imported fossil fuels, enhancing energy security and stability.
3. **Challenges in Implementation:** While the benefits are clear, there are challenges to widespread adoption of renewable energy.
   * **Infrastructure Needs:** Transitioning to renewable energy requires significant investment in infrastructure, such as solar panels, wind turbines, and energy storage systems.
   * **Government Support:** Policy frameworks and incentives are necessary to encourage investment and innovation in renewable technologies.
4. **Conclusion:** In conclusion, renewable energy sources play a vital role in addressing environmental concerns, boosting the economy, and ensuring energy independence. By investing in these technologies and overcoming existing challenges, we can pave the way for a sustainable future. Thank you for your attention, and I look forward to your questions

</details>

**User Response (when task is achieved):**\
“The lecture covered the importance of renewable energy sources. The speaker emphasized how solar and wind power can reduce reliance on fossil fuels and combat climate change. Additionally, various benefits of using renewable energy, such as cost savings and environmental sustainability, were discussed. The speaker also mentioned some challenges, like the need for infrastructure improvements and government support.”

***

If the user utters a relevant response as below, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an ***irrelevant*** response as below, the API will give <mark style="color:red;">**zero**</mark> task score but the language scores will be non-zero and can be presented to the user as follows:

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.
