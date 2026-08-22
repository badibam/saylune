> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](interpreting-api-response.md).

# Interpreting API Response

The API response JSON contains the following scores.

<table><thead><tr><th width="300.58984375">Field</th><th>Description</th></tr></thead><tbody><tr><td><code>word_score.word</code></td><td>The target word. If Multiple Choice was used, then this will be the closest target to what the user spoke.</td></tr><tr><td><code>word_score.quality_score</code></td><td>The pronunciation score for the word (scale 0..100). See <a href="/pages/Z3fNsFWjfOU0FvtHH5pL">this guide</a> for interpreting the quality_score.</td></tr><tr><td><code>word_score.quality_class</code></td><td>pass | fail<br>The classification for the word based on its quality_score. The threshold applied for pass is quality_score ≥ 70.</td></tr><tr><td><code>word_score.syllable_score_list</code></td><td>An array of syllables within the word, each with their quality_score, stress, and extent information.</td></tr><tr><td><code>word_score.phone_score_list</code></td><td>An array of phonemes within the word, each with their quality_score, , stress, and extent information.</td></tr></tbody></table>
