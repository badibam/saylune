> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](test-prep-for-standardized-tests.md).

# Test Prep for Standardized tests

Preparation or remediation for a standardized test is best guided through actual test like activities conducted within the scope of a practice module or a full mock test.

In this guide, we will cover creating practice and mock test activities automatically scored by Speechace to mirror the speaking question types found in standardized Languages tests like:

* PTE
* IELTS
* TOEFL
* TOEIC

In each sub-section below we will:

* Describe the type of speaking tasks within each test
* Demonstrate how to use the Speechace API to programmatically recreate each task
* Show how to extract the scores the measure various components based on that task

We refer to the Score Guide for each of the Standardized tests for the definition and scoring criteria for its tasks. In addition, for every request the Speechace API returns mapped result based on the common [Scoring Rubrics](../guides-on-common-topics/scoring-rubrics.md). This allows you to map an IELTS 6.0 learner for example to the expected equivalent TOEIC or PTE level.

You can also refer to the detailed [speaking band definitions ](../guides-on-common-topics/interpreting-overall-scores.md)extracted from the various test guides to understand the expectations of a candidate that meets a specific band or level.
