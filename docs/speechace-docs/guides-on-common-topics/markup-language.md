> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](markup-language.md).

# Markup Language

In [scoring text using markup language](../features/scripted-activities/pronunciation-scoring/custom-pronunciations.md), the input can include markups to explicitly define phonetic mappings for one or more words, allowing you to bypass the Speechace lexicon. This feature is supported for both en-US and en-GB requests and can be utilized in scoring, multiple-choice, or Validate Text requests.

### Markup Syntax

Each markup has the pattern: `[l1|l2|...|ln]{s1|s2|...|sn}`, where:

* `l1`, `l2`, ..., `ln` are substrings of a word in the input text, and
* `s1`, `s2`, ..., `sn` are syllables of the word corresponding to `l1`, `l2`, ..., `ln`.
* Each syllable, `si`, has this pattern: `p1` `p2` ... `pk`, where `pi` is a phoneme in [ARPABET notation](phonetic-notation.md) for the current dialect (i.e. en-us or en-gb).
* If `pi` is a vowel phoneme, `pi` ends with 0, 1 or 2 to denote the stress level of the syllable.
* There should be at most one vowel per syllable.

You can now pass the parameter `markup_language=arpa_mark` to the [API](../features/scripted-activities/pronunciation-scoring/custom-pronunciations.md) and include markup for words within the text as follows:

{% code overflow="wrap" %}

```
He [read]{r eh1 d} his [frag|ments]{f r ae1 g|m ah0 n t s} aloud.
```

{% endcode %}

In the text above, the words "read" and "fragments" are marked up to disambiguate them from heteronyms.

### Expressing letters in markup

Optionally, markup can specify letter-to-phoneme mapping. This allows the Speechace API to return [phoneme-to-letter mapping](phoneme-to-letter-mapping.md) in the results for the marked-up words.

To add letter information in markup:

1. **Prefix the first syllable**: Add `2|` before `s1` (the first syllable) as follows: `[l1|l2|...|ln]{2|s1|s2|...|sn}`. This indicates that the markup contains phoneme-to-letter mapping information.
2. **Mapping Syntax**: Use zero or more pairs of angular brackets `<` and `>` to group substrings in `li` (letters) and `si` (phonemes) for establishing one-to-one correspondence when necessary.
   * `li` consists of a sequence of characters `c1c2...cp`
   * `si` consists of a sequence of phonemes `p1 p2 ... pk`
3. **Simple Mapping**: If there is a one-to-one mapping between letters and phonemes, no additional markup is needed.
4. **Inference of Mapping**: If there are no pairs of `<` and `>` in `li` and `si`, and if `p` (the number of phonemes) equals `k` (the number of letters), we infer:
   * `c1` is mapped to `p1`
   * `c2` is mapped to `p2`
   * ...,
   * `cp` is mapped to `pk`
5. **Complex Cases**: For cases where character-to-phoneme mapping is more complex than one-to-one:
   * Use `<` and `>` to group adjacent characters and/or phonemes.
   * Empty brackets `<>` can be used to denote mappings where `m` characters correspond to `n` phonemes (with `m >= 0` and `n >= 0`, but not both zero).

This transforms `li` and `si` into sequences of the same length to facilitate one-to-one mapping.

This can be quite complex, so let's provide illustrative examples that cover all real-world cases. This way, you can easily copy and modify an example instead of constructing the notation from scratch.

### **Markup examples with letters**

Here’s a polished and professional version of your examples:

#### Example 1:

**Markup**: `[car]{2|k aa1 r}`\
**Explanation**:\
This example features one syllable without the need for `<` and `>`. Here, `l1` is "car" and `s1` is "k aa1 r." We can infer that:

* `c1` maps to `p1`
* `c2` maps to `p2`
* `c3` maps to `p3`

Thus, we have:

* `c1 = c`
* `c2 = a`
* `c3 = r`
* `p1 = k`
* `p2 = aa1`
* `p3 = r`

***

#### Example 2:

**Markup**: `[bi|li<ng>|<>ual]{2|b ay0|l ih1 ng|g w ah0 l}`\
**Explanation**:\
In this case, we have zero characters mapping to one phoneme in `l3` as `<>ual` and `s3` as `g w ah0 l`. The `<>` allows us to infer that the phoneme `g` is mapped to no character. Thus:

* `c1 =`
* `c2 = u`
* `c3 = a`
* `c4 = l`
* `p1 = g`
* `p2 = w`
* `p3 = ah0`
* `p4 = l`

***

#### Example 3:

**Markup**: `[M<>|<><r.>]{2|m <ih1 s>|t er0}`\
**Explanation**:\
This example shows zero characters mapping to two phonemes. Here, `l1` is `M<>` and `s1` is `m <ih1 s>`. The `<>` and `<ih1 s>` allow us to infer that the phonemes `ih1 s` are mapped to no character. Thus:

* `c1 = M`
* `p1 = m`
* `p2 = ih1 s`

***

#### Example 4:

**Markup**: `[ear]{2|iy1 <> r}`\
**Explanation**:\
In this instance, we have one character mapping to zero phonemes. Here, `l1` is "ear" and `s1` is "iy1 <> r." The `<>` indicates that the character `a` is mapped to no phoneme. Thus:

* `c1 = e`
* `c2 = a`
* `c3 = r`
* `p1 = iy1`
* `p2 =`
* `p3 = r`

***

#### Example 5:

**Markup**: `[box]{2|b aa1 <k s>}`\
**Explanation**:\
This example features one character mapping to two phonemes. Here, `l1` is "box" and `s1` is "b aa1 ." The `<k s>` allows us to infer that the character `x` is mapped to the phonemes `k s`. Thus:

* `c1 = b`
* `c2 = o`
* `c3 = x`
* `p1 = b`
* `p2 = aa1`
* `p3 = k s`

***

#### Example 6:

**Markup**: `[si<gh>]{2|s ay1 <>}`\
**Explanation**:\
In this case, we have two characters mapping to zero phonemes. Here, `l1` is `si<gh>` and `s1` is `s ay1 <>`. The `<gh>` and `<>` allow us to infer that the characters `gh` are mapped to no phoneme. Thus:

* `c1 = s`
* `c2 = i`
* `c3 = gh`
* `p1 = s`
* `p2 = ay1`
* `p3 =`

***

#### Example 7:

**Markup**: `[no<th>|i<ng>]{2|n ah1 th|ih0 ng}`\
**Explanation**:\
This example features two characters mapping to one phoneme. Here, `l1` is `no<th>` and `s1` is `n ah1 th`. The `<th>` allows us to infer that the characters `th` are mapped to the phoneme `th`. Thus:

* `c1 = n`
* `c2 = o`
* `c3 = th`
* `p1 = n`
* `p2 = ah1`
* `p3 = th`

***

#### Example 8:

**Markup**: `[qu<eue>]{2|k <y uw1> <>}`\
**Explanation**:\
In this instance, we have three characters mapped to zero phonemes. Here, `l1` is `qu<eue>` and `l2` is `k <y uw1> <>`. The `<eue>` and `<>` allow us to infer that the characters `eue` are mapped to no phoneme. Thus:

* `c1 = q`
* `c2 = u`
* `c3 = eue`
* `p1 = k`
* `p2 = y uw1`
* `p3 =`
