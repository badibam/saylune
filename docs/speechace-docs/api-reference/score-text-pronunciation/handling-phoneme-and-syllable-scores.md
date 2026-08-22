> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-phoneme-and-syllable-scores.md).

# Handling phoneme and syllable scores

Just as we have word-level pronunciation quality scores, we also assess pronunciation at the phoneme and syllable levels:

<div align="left"></div>

<div align="left"></div>

Additionally, the **lexical stress level** measures how accurately the stressed syllables or phonemes in a word are pronounced compared to the expected stress pattern. In this context, `stress_level` represents the expected stress level, while `predicted_stress_level` indicates the actual stress level determined by the pronunciation evaluation. Possible values are:

1. `0`: unstressed&#x20;
2. `1`: primary stress&#x20;
3. `2`: secondary stress

Note that **lexical stress score** evaluates the quality of stress applied to syllables or phonemes, ranging from 0 to 100. A higher score indicates better accuracy in stress placement according to the expected pattern.

Lastly note the **Sound most like** value&#x73;**, which** indicate how the pronounced phoneme sounded compared to the expected phoneme.

<div align="left"></div>
