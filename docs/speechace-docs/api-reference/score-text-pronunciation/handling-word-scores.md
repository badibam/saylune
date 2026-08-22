> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-word-scores.md).

# Handling word scores

The **word-level quality score** helps you assess the pronunciation accuracy of a specific word within a sentence. This score provides insight into how well that particular word was pronounced compared to the expected standard.

<div align="left"></div>

{% hint style="info" %}
The scale of the quality score can be interpreted from the scoring [guide](../../guides-on-common-topics/intepreting-quality-score.md).
{% endhint %}

You can use the `quality_score` at the word level to display the analysis of the spoken response in your app using the following [color codes](../../guides-on-common-topics/intepreting-quality-score.md):

<table><thead><tr><th width="118">Score</th><th width="95">Color</th><th>Description</th></tr></thead><tbody><tr><td>90 - 100</td><td>Green</td><td>Excellent. Native or native-like</td></tr><tr><td>80 - 90</td><td>Green</td><td>Very Good and clearly intelligible.</td></tr><tr><td>70 - 80</td><td>Orange</td><td>Good. Intelligible but with one or two evident mistakes.</td></tr><tr><td>60 - 70</td><td>Red</td><td>Fair. Possibly not intelligible with several evident mistakes.</td></tr><tr><td>0 - 60</td><td>Red</td><td>Poor and must be reattempted.</td></tr></tbody></table>

For example, in the case shown below, the `quality_score` for the word "walk" is 65. Consequently, it is displayed in <mark style="color:red;">**RED**</mark> in the question based on the test-taker's response, indicating several evident mistakes in pronunciation.
