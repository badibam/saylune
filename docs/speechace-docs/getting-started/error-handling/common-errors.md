> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](common-errors.md).

# Common Errors

{% hint style="info" %}
If you are unable to understand or address an error you can [raise a support ticket](../../other-resources/requesting-support.md).
{% endhint %}

## Attributes of error response

Whenever the API returns an error, the response will appear as follows:

{% code title="Sample error response" %}

```json
{
    "status": "error",
    "short_message": "error_invalid_parameters",
    "detail_message": "key"
}
```

{% endcode %}

The attributes of the response are:

* `status`: This determines whether the response is a success or an error.
* `short_message` : The type of error returned. Some of the [possible errors](#errors) are `error_audio_too_long`, `error_missing_parameters` or `error_invalid_parameters`.
* `detail_message` : A human-readable message providing more details about the error.

## Errors

Below is the list of common errors returned by the API and their solutions:&#x20;

<table><thead><tr><th width="252">Error</th><th>Cause and Solution</th></tr></thead><tbody><tr><td><strong>error_unknown_words</strong></td><td><p><strong>Cause</strong>: One or more words are outside of Speechace lexicon. </p><p><strong>Solution</strong>: You should use the <a href="/pages/Mc1Jb4y3fFEWbzYoRMDG">Validate Text API</a> to validate text at the time of activity authoring to catch such errors and address them by changing the text or making requests to add terms to the Speechace lexicon. Alternatively you can pass the <a href="/pages/9iJltY1AWWBFTnJwU4pf">include_unknown_words</a> parameter to automatically handle such words</p></td></tr><tr><td><strong>error_audio_too_long</strong></td><td><p><strong>Cause</strong>: Your<a href="/pages/xvJjApHAxNUw908WXMWo"> audio duration</a> is longer than is allowed for your API license. </p><p><strong>Solution</strong>: You should limit the recording size or contact <code>support@speechace.com</code> to upgrade your plan.</p></td></tr><tr><td><strong>error_file_too_large</strong></td><td><p><strong>Cause</strong>: Your <a href="/pages/xvJjApHAxNUw908WXMWo">audio file size</a> is larger than is allowed for your API license. </p><p><strong>Solution</strong>: You should ensure you are recording at the required <a href="/pages/xvJjApHAxNUw908WXMWo">sample rate</a> and no higher to optimize file size.</p></td></tr><tr><td><strong>error_audio_missing</strong></td><td><p><strong>Cause</strong>: The audio file was not correctly passed or is missing.</p><p><strong>Solution</strong>: Check the form data field in the request body of the cURL.</p></td></tr><tr><td><strong>error_missing_parameters</strong></td><td><p><strong>Cause</strong>: One or more API parameters were missing. </p><p><strong>Solution</strong>: Check the <code>detail_message</code> in the error response for the missing parameter.</p></td></tr><tr><td><strong>error_invalid_parameters</strong></td><td><p><strong>Cause</strong>: One or more API parameters were missing. </p><p><strong>Solution</strong>: Check the <code>detail_message</code> in the error response for the missing parameter.</p></td></tr><tr><td><strong>error_convert_audio</strong></td><td><p><strong>Cause</strong>: The audio is either corrupt or not provided in a valid format such as mp3, wav, webm, aac etc.</p><p><strong>Solution</strong>: Please check that the audio file is not corrupt and is in acceptable <a href="/pages/xvJjApHAxNUw908WXMWo">formats</a>.</p></td></tr><tr><td><strong>error_too_many_requests</strong></td><td>You are being throttled because you have exceeded the allowed concurrent request volume</td></tr><tr><td><strong>error_feature_unavailable</strong></td><td><p><strong>Cause</strong>: You attempted to access a feature not provided in your subscription. </p><p><strong>Solution</strong>: Contact <code>support@speechace.com</code> to upgrade your plan.</p></td></tr><tr><td><strong>error_key_expired</strong></td><td><p><strong>Cause</strong>: Your API Key is not active. </p><p><strong>Solution</strong>: Please check your subscription status or contact <code>support@speechace.com</code></p></td></tr><tr><td><strong>error_no_speech</strong></td><td><p><strong>Cause</strong>: No human voice detected in the provided audio file. </p><p><strong>Solution</strong>: Ensure that the audio file contains clear human speech. This error often occurs when you have recording logic failures.</p></td></tr><tr><td><strong>error_word_alignment</strong></td><td><p><strong>Cause</strong>: The audio did not align with any of the words in the intended text, possibly due to background noise or unclear speech. </p><p><strong>Solution</strong>: Check for background noise and ensure that the speech in the audio file is clear and distinct.</p></td></tr><tr><td><strong>error_text_too_long</strong></td><td><strong>Cause</strong>: The text you have submitted is longer than 1500 characters.<br><strong>Solution</strong>: Shorten the text to meet the character limit.</td></tr><tr><td><strong>error_internal</strong></td><td><p><strong>Cause</strong>: A catch-all error for various unforseen cases.</p><p><strong>Solution</strong>: Consult <code>support@speechace.com</code> for assistance with this general error.</p></td></tr></tbody></table>

## Support

If you are unable to understand or address an error you can [raise a support ticket](../../other-resources/requesting-support.md).
