> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](getting-word-timestamps-in-audio.md).

# Getting word timestamps in audio

Speechace segments and aligns user audio at the word, syllable, and phoneme levels. The Speechace API provides detailed extent information for each level:

* **Syllable Level:** Data is returned in the `syllable_score_list[]` array.
* **Phoneme Level:** Data is returned in the `phone_score_list[]` array.

The `extent[]` field contains begin and end timestamps for that syllable or phoneme in units of 10 msec.

In the example below the phoneme */sh/* is at msec 250 to 350 in the user audio file:

Timestamp extent information can be used to zoom in and playback specific words, allowing for the demonstration of a test-taker's mistakes or the correct pronunciation of a word from a reference example.

To do so you need to iterate through the Speechace API JSON result as follows:

{% code overflow="wrap" lineNumbers="true" %}

```python
for each word in text_score.word_score_list[]

    get first and last elements of phone_score_list[] for that word
    
    start_timestamp is extent[0] for the first element
    end_timestamp is extent[1] for the last element
    
    # timestamps are in unit of 10 msec
```

{% endcode %}
