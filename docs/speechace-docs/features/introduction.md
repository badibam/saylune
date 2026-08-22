> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](introduction.md).

# Introduction

The Speechace API offers a rich feature set that can be used to compute an accurate spoken language proficiency score from a given audio sample for a speaker irrespective of their gender, race and first language. At its core, the API accepts an audio file along with metadata to analyze the audio file and produce a speaking score which indicates the spoken language proficiency level of the speaker.

The API output is highly versatile and can not only produce speaking scores on a scale of 0-100 but can also produce scores for standardized rubrics such as <mark style="color:blue;">**IELTS**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**CEFR**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**TOEFL**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**PTE**</mark> and <mark style="color:blue;">**TOEIC**</mark>. The Speechace team has spent years calibrating scores to align with these rubrics and currently hundreds of test prep providers use the Speechace API for providing speaking practice for these exams.

In general, the API can be used to implement two main classes of speaking activities:\
\
**a. Scripted speech activities** - In this class of activities, users are typically prompted to read aloud an answer that is visible on screen to the user. As an example, the user may be asked to speak a word such as "Apple" or the user may be asked to read a sentence such as "The quick brown fox jumped over the lazy dog". \
\
**b. Unscripted or spontaneous speech activities -** In this class of activities, the user does not know what to speak before hand and has to come up with an answer on the spot. As an example, a user may be prompted to respond to a question such as "Talk about your best friend and the things you do together.". In this case, the Speechace API can not only score language components of a user's response but also transcribe the user's response.

In the next few sections, we will review the different type of features that are available for both scripted and spontaneous activities.
