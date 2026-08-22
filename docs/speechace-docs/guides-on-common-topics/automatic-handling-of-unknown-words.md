> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](automatic-handling-of-unknown-words.md).

# Automatic handling of unknown words

Speechace returns the error code `error_unknown_words` for terms not found in its lexicon. To avoid this error and allow Speechace to automatically determine the phonetic mapping for unknown terms, include the `include_unknown_words` parameter in the request body.

Pass the following form-data parameter:

```json5
include_unknown_words=1
```
