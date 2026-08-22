> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](phoneme-to-letter-mapping.md).

# Phoneme to letter mapping

Some applications may highlight letters in a word where pronunciation errors occurred to help learners visualize their mistakes, especially if they are unfamiliar with phonemes.

For example, if a user mispronounces the /sh/ sound in "shift," the application could display the word with the problematic letters crossed out or color-coded to indicate errors:

**Incorrect Example:**

`**sh**ift` (where "sh" is highlighted to show the pronunciation issue)

The Speechace API does not score individual letters; instead, it decomposes each scored word into its phonemes and scores them at the phoneme level.

To identify which letter(s) in a word correspond to a specific phoneme:

1. **Access Phoneme Data:**
   * Retrieve phoneme-level scores from the `word_score_list[].phone_score_list[]` array.
2. **Locate Letter Mapping:**
   * For each phoneme, use the `phone_score_list[].word_extent[]` field to find the character indices within the word that this phoneme maps to. This field contains the `[begin, end]` character indices.

By checking these indices, the application can map the phoneme to its corresponding letters in the word.

In the above example of the word "shift", the contents of `phone_score_list[]` would be:

| phone | word\_extent | letter(s) |
| ----- | ------------ | --------- |
| sh    | \[0,2]       | sh        |
| ih    | \[2,3]       | i         |
| f     | \[3,4]       | f         |
| t     | \[4,5]       | t         |

The letters column is built using the character indices specified in `word_extent`. Notice that a phoneme may correspond to more than one letter. For instance, a phoneme like /sh/ in "shift" covers two letters (e.g., 'sh') as shown above.

There are multiple possibilities of phoneme to letter mapping that the API caller should be prepared for:

<table><thead><tr><th width="271">Case</th><th>word_extent</th><th>Example</th></tr></thead><tbody><tr><td>Phoneme maps to zero letters</td><td>[n, n] where the begin and end indices are the same.<br><br>The value of n depends on the phoneme's position in the word</td><td>The word "#" (i.e. hashtag) which is phonetically expanded to ['hh', 'ae', 'sh', 't', 'ae', 'g']</td></tr><tr><td>Phoneme maps to 1 letter</td><td>[n, n+1]</td><td>/f/ in "shift"</td></tr><tr><td>Phoneme maps to 2 letters</td><td>[n, n+2]</td><td>/sh/ in "shift"</td></tr><tr><td>2 Phonemes map to 1 letter</td><td>Each phoneme will have the same word_extent:<br>[n, n+1]<br>[n, n+1]</td><td>letter 'x' in the word "exempt" maps to the phonemes /g/, /z/</td></tr><tr><td>2 or more Phonemes map to zero letters</td><td>Each phoneme will have the same word_extent:<br>[n, n]</td><td>The word "w" which is phonetically expanded to ['d', 'ah', 'b', 'ah', 'l', 'y', 'u', 'w']</td></tr><tr><td>No Phoneme to letter mapping available</td><td>word_extent field will be missing</td><td>Words outside of the Speechace lexicon processed with markup_language or include_unknown_words handling.</td></tr></tbody></table>
