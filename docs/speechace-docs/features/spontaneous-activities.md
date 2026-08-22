> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](spontaneous-activities.md).

# Spontaneous activities

While users are often able to recite sentences presented in a read-aloud activity, they continue to lag in having real-life conversations. This is because readable prompts provide visual cues on how to produce sounds but they limit the user from producing original responses. Therefore merely measuring oral skills based on read-aloud scripted activities is not sufficient. To truly measure a user's spoken language abilities, we need to prompt the user in to producing an original or spontaneous response to open-ended questions. To this effect, the Speechace API provides the following capabilities:\
\
**a.** [**Open-ended scoring**](spontaneous-activities/open-ended-scoring.md)**:** This capability evaluates a user's free speech spontaneous response up to 2 minutes in length and provides a **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** score along with a **transcript** of the candidate's response.

**b.** [**Task achievement scoring**](spontaneous-activities/task-achievement-scoring.md)**:** In addition to scores provided by open-ended scoring, Task achievement scoring provides a fine grained measure of the comprehensiveness and completeness of the user's response for a given task.  \
\
In the next few sections, we will discuss these capabilities in more detail.
