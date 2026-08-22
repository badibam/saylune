> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](scoring-rubrics.md).

# Scoring rubrics

In general, all standard rubrics for measuring speaking proficiency are aligned, as they aim to assess similar aspects of language skills. However, there is no exact one-to-one mapping between them due to overlapping score ranges and varied criteria across different rubrics.

The Speechace API returns results within the mapped ranges for different proficiency bands. The following table provides a detailed mapping of cut-off scores for each band across standard rubrics, such as IELTS, CEFR, PTE, and TOEIC, and how they correspond to Speechace scores.

| IELTS    | CEFR | PTE | TOEIC   | Speechace |
| -------- | ---- | --- | ------- | --------- |
| 9.0      | C2   | 90  | 200     | 97-100    |
| 8.5      | C2   | 90  | 190     | 92-96     |
| 8.0      | C1+  | 85  | 180     | 86-91     |
| 7.5      | C1   | 76  | 170     | 81-85     |
| 7.0      | B2   | 68  | 160     | 75-80     |
| 6.5      | B1+  | 59  | 140-150 | 69-74     |
| 6.0      | B1   | 51  | 120-130 | 64-68     |
| 5.5      | A2+  | 42  | 110     | 58-63     |
| 5.0      | A2   | 34  | 90-100  | 53-57     |
| 4.5      | A1+  | 25  | 80      | 47-52     |
| 4.0      | A1   | 20  | 50-70   | 42-46     |
| 0 to 3.5 | A0   | 10  | 0-40    | 0-41      |
