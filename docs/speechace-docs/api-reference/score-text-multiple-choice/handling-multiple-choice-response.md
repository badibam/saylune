> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-multiple-choice-response.md).

# Handling multiple choice response

Firstly, your app should identify the correct answer from the provided multiple-choice options. Once the test-taker responds, their answer should be compared to the expected correct answer. Only if the answer is correct should the app provide pronunciation feedback. The interpretation of the spoken word or sentence remains the same as under [Score Text/Pronunciation](../score-text-pronunciation.md), where feedback includes a review of the syllables and phonemes.

Like in the example below, the response is matched to the expected correct answer and accordingly the pronunciation feedback metrics are provided:

