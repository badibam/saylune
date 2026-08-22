> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](requesting-support.md).

# Requesting Support

All Speechace API plans include Developer support. You can raise a support issue in the Speechace Customer Support Portal:

{% embed url="<https://speechace.atlassian.net/servicedesk/customer/portals>" %}

The request form requires the following information:

* Your email
* Your Subscription Id
* A description of your issue
* The Severity of the issue you are facing

In addition you may include `request_id` value(s) to reference specific API requests made where you faced this issue. This will enable our support team to look up your requests in logs and speed up identifying the issue.

### Best Practices for better Observability

Speechace logs metadata of all requests and by passing and recording the right fields, developers can preserve all necessary information for support requests without retain any user PII.

| Parameter                                              | Description                                                                                                                                                    | Use                                                                                                                                                                                                                    |
| ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| URL Param: `user_id`                                   | A unique, anonymized identifier for the end-user on behalf of whom you made the API request. This is typically a hash of the user\_id in your own Application. | Allows retrieval of metadata for all requests pertaining to a specific user. This will often be important if a user issues a support request. It allows quick verification of issues such as audio recording failures. |
| Request Body Param: `question_info`                    | A unique code to identify the question id within your Application.                                                                                             | This allows quick identification of a particular activity without referencing the activity text. It supports verification of activities where users don't score as expected.                                           |
| <p>Response parameter:<br><code>request\_id</code></p> | A unique identifier for the API request generated and returned by Speechace on each response.                                                                  | Allows you to reference one or more specific requests when filing support issues.                                                                                                                                      |

### Understanding Error Codes

Speechace API returns 2 levels of errors codes:

* HTTP Code: These will indicate network or service issues meaning the request failed to process the request alltogether
* Speechace ErrorMsg: Even with HTTP 200 requests, Speechace may encounter failures specific to your audios or request. You can review the list of Error Codes [here](../getting-started/error-handling/common-errors.md).
