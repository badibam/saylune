> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](api-versioning.md).

# API Versioning

Speechace API versioning is managed through the request URL.&#x20;

Prior to version 9, the API required explicit versioning, where the caller specified the exact major and minor version in the request URL.&#x20;

Starting with major version 9, the API supports explicit or automatic minor version updates, giving callers the option receive incremental updates without needing to specify each minor version explicitly. API callers&#x20;

<table><thead><tr><th width="402">Request URL endpoint</th><th>Resulting version</th></tr></thead><tbody><tr><td><a href="https://api.speechace.co/scoring/text/v9/json?">https://api.speechace.co/scoring/text/v9/json?</a></td><td>This request will use the latest available minor version of v9 (e.g. v9.0 or v9.1 or v9.2 etc.)</td></tr><tr><td><a href="https://api.speechace.co/scoring/text/v9.0/json?">https://api.speechace.co/scoring/text/v9.0/json?</a></td><td>This request will explicitly use the minor version v9.0</td></tr></tbody></table>

{% hint style="info" %}
The latest API version is v9.

You can find [Major versioning history](../other-resources/appendices.md#major-versioning-history) in Appendices.
{% endhint %}
