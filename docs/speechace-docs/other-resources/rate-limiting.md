> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](rate-limiting.md).

# Rate Limiting

Speechace is architected to be highly available and auto-scalable to serve request spikes as needed. The API provides a concurrency guarantee of up to 16 concurrent Transactions per Second (TPS). Above that limit, the API will throttle calls returning `error_too_many_requests.`

A higher concurrency limit is available with custom plans and annual contracts. You may contact us at <support@speechace.com> to request a higher limit.
