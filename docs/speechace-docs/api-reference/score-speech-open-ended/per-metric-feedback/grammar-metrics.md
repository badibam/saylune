> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](grammar-metrics.md).

# Grammar metrics

Below are the detailed feedback metrics appearing in the response for `grammar` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="411">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>grammar.overall_metrics.length</td><td>The sufficiency of the response length in words to demonstrate the necessary grammatical range.</td></tr><tr><td>grammar.overall_metrics.lexical_diversity</td><td>The degree of variation in syntactic structures such as diversity in verbs, adjectives and adverbial modifiers.</td></tr><tr><td>grammar.overall_metrics.grammatical_accuracy</td><td>The degree of grammatical inaccuracies in the response. A list of grammatical errors with suggested replacements is returned in the <code>grammar.errors</code> node when this index is low.</td></tr><tr><td>grammar.overall_metrics.grammatical_range</td><td><p></p><p>The degree of grammatical range demonstrated in the response. This score is further broken down into 4 additional sub-indices:</p><ul><li>noun_phrase_variation</li><li>noun_phrase_complexity</li><li>verb_construction_variation</li><li>adverb_modifier_variation</li></ul></td></tr><tr><td>grammar.overall_metrics.grammatical_range.noun_phrase_variation</td><td>The degree of variation in structure of noun phrases such as the number and types of modifiers used in the response.</td></tr><tr><td>grammar.overall_metrics.grammatical_range.noun_phrase_complexity</td><td>The degree of complexity of noun phrases such as the richness of adjectives, relative clauses, prepositional phrases, nonfinite elements, determiners, and demonstratives used in the response.</td></tr><tr><td>grammar.overall.metrics.grammatical_range.verb_construction_variation</td><td>The degree of variation in verbal structures such as the number and types of verb structural elements used in the response.</td></tr><tr><td>grammar.overall.metrics.grammatical_range.adverb_modifier_variation</td><td>The degree of variation in types of adverbs or adverb phrases to modify clauses, verbs, and adjectives used in the response.</td></tr></tbody></table>

An example of `grammar` feedback metrics looks like below:

{% code overflow="wrap" lineNumbers="true" fullWidth="false" %}

```json
{   
"grammar": 
  {
    "overall_metrics": {
      "length": {
         "score": 4,
         "level": "mid"
       },
       "lexical_diversity": {
          "score": 8,
          "level": "high"
       },
       "grammatical_accuracy": {
           "score": 8,
           "level": "high"
        },
        "grammatical_range": {
            "score": 5,
            "level": "mid",
            "message": "Your response has less grammatical range than most advanced speakers. To improve, you should use a wider range of phrasal and clausal structures and verb-argument constructions.",
            "noun_phrase_complexity": {
               "score": 1,
               "level": "low",
               "message": "Your response lacks noun phrase complexity. You should use richer modifiers in noun phrases by using adjectives, relative clauses, prepositional phrases, non-finite elements, determiners, and demonstratives."
            },
            "noun_phrase_variation": {
                "score": 5,
                "level": "mid"
            },
            "verb_construction_variation": {
                "score": 6,
                "level": "mid"
             },
             "adverb_modifier_variation": {
                 "score": 9,
                 "level": "high"
              }
         }
     },
     "errors": [
      {
         "category": "STYLE",
         "message": "Did you mean 'different from'? 'Different than' is often considered colloquial style.",
         "span": [44,48],
         "matched_text": "than",
         "replacements": ["from"]
      }
     ]
 }
}
```

{% endcode %}

#### **Grammatical Error**

The `grammar.errors` node contains a JSON array of grammatical errors found in the response. Each array element is an object with the following properties:

<table data-full-width="false"><thead><tr><th width="157">key</th><th>Description</th></tr></thead><tbody><tr><td>category</td><td>The type of error such as; STYLE, GRAMMAR, COLLOCATION, CONFUSED_WORDS</td></tr><tr><td>message</td><td>A descriptive message of the error. The message may refer to words within the evaluated text and include suggested replacements within the ... markup tags.</td></tr><tr><td>span</td><td>The [begin, end] indices of the matched text in characters.</td></tr><tr><td>matched_text</td><td>The matched text where the error was found.</td></tr><tr><td>replacements</td><td>An array of zero or more suggested replacements where applicable.</td></tr></tbody></table>
