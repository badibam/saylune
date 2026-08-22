> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](retry-strategies.md).

# Retry Strategies

Our client libraries raise exceptions for various reasons, including unknown words, invalid parameters, authentication errors, and server issues. It is advisable to write code that handles all possible API [exceptions](common-errors.md#errors) gracefully, as demonstrated below:

{% tabs %}
{% tab title="Python" %}
{% code overflow="wrap" lineNumbers="true" %}

```python
try:
    # Send the POST request and use Speechace's library to make requests...
    response = requests.post(url, data=payload, files=files)

    # Process the JSON response...
    data = response.json()
    status_value = data['status']
    
    # Print the detailed error reason to get to the solution...
    if status_value == 'error':
        print(data)
finally:
    # Ensure the file is closed after use...
    if 'files' in locals():
        files['user_audio_file'].close()
```

{% endcode %}
{% endtab %}
{% endtabs %}
