> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](handling-fluency-response.md).

# Handling fluency response

The pronunciation interpretation of the spoken word or sentence remains the same as in the [Score Text/Pronunciation](../score-text-pronunciation.md) function. To interpret the fluency quality, refer to the below key elements:

**Overall Fluency Scores**

These scores assist test creators in evaluating the overall fluency of spoken responses, offering insights into the quality of the test-taker's speech. Below is an example of how the fluency score is presented. For detailed interpretation, please refer to the [overall score guide](../../guides-on-common-topics/interpreting-overall-scores.md), which includes scales from systems such as IELTS, PTE, and Speechace.

<div align="left"></div>

**Fluency Metrics**

The API returns the following feedback metrics under the `fluency` node:

| Field                          | Description                                                                                                                                                                                       |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| duration                       | total length of speech in seconds                                                                                                                                                                 |
| articulation                   | total length of articulation (speech minus pauses, hesitations and non-speech events such as laughter). Excludes beginning silence on very first segment and ending silence on very last segment. |
| speech\_rate                   | speaking rate in syllables per second.                                                                                                                                                            |
| syllable\_count                | Count of syllables in this segment                                                                                                                                                                |
| word\_count                    | Count of words in this segment                                                                                                                                                                    |
| correct\_syllable\_count       | Count of correctly spoken syllables in this segment                                                                                                                                               |
| correct\_word\_count           | Count of correctly spoken words in this segment                                                                                                                                                   |
| syllable\_correct\_per\_minute | correct\_syllable\_count / duration in mins                                                                                                                                                       |
| word\_correct\_per\_minute     | correct\_word\_count / duration in mins                                                                                                                                                           |
| all\_pause\_count              | count of all pauses (filled and unfilled) which are longer than the minimum pause threshold                                                                                                       |
| all\_pause\_duration           | total duration of all pauses (filled and unfilled) in seconds                                                                                                                                     |
| all\_pause\_list\[]            | a list of all the pauses with the begin/end markers for each in extents of 10 msecs                                                                                                               |
| mean\_length\_run              | mean length of run in syllables between pauses                                                                                                                                                    |
| max\_length\_run               | max length of run in syllables between pauses                                                                                                                                                     |
| segment\_metrics\_list\[]      | A list of segments within the overall text/audio with the fluency metrics for each segment.                                                                                                       |

The following are the most commonly used metrics to provide feedback to the user:

1. `word_correct_per_minute`: This measures the count of words per minute. You can color-code the test-taker's rate and compare it to the standard rate of 120 words per minute, which is widely considered the minimum fluent speaking rate.
2. `all_pause_list []`: This is a list of all pauses, with each pause marked by begin and end times, accurate to within 10 milliseconds. Identify and display the locations of medium pause duration (≥500 milliseconds) and long pause duration (>1 second) based on the length and positions of entries in the `all_pause_list[]`.
3. `duration` and `articulation`: Display the duration and articulation length to show how much time the user spent speaking compared to pausing or using fillers.

   `a. duration` : The total length of the speech in seconds, including all pauses, fillers, and non-speech events.

   `b. articulation`: The total length of actual articulation, calculated as the total speech duration minus pauses, hesitations, non-speech events (such as laughter), and excluding any silence at the very beginning and very end of the speech.

<div align="left"></div>
