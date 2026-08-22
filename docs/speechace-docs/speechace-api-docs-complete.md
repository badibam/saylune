# Speechace API — documentation complete

_Archive generee le 2026-08-22 12:14 depuis https://api-docs.speechace.com_


---

<!-- source: https://api-docs.speechace.com/introduction/overview.md -->
# Speechace API Reference / Overview

# Overview

Speechace API is a Speech Recognition and Assessment API for comprehensive spoken language assessment. Speechace’s patented technology is unique in its ability to automatically evaluate the spoken language proficiency of a learner and present scores aligned with standard language proficiency rubrics such as IELTS and CEFR.

Speechace supports a range a wide range of assessment item types from phoneme and word pronunciation to comprehensive spontaneous speech evaluation.

The Speechace API allows for the submission of audio files for evaluation and provides detailed assessment on pronunciation, fluency, grammar, coherence, vocabulary, and relevance. It utilizes standard HTTP response codes and authentication mechanisms to ensure secure and reliable communication.

## Target Audience

The Speechace API is for developers of a variety of applications who need to use AI to assess and improve speech and language skills, whether for assessment or training purpose. Some of the examples are:

1. Language learning providers and applications
2. Recruiting, Screening and Assessment Software
3. Language Test preparation platforms (IELTS, PTE, TOEFL, CEFR and others)
4. K-12 Learning Platforms
5. Learning Management Systems
6. Early Literacy training and assessment applications
7. Speech Therapy and Speech Pathology applications


---

<!-- source: https://api-docs.speechace.com/introduction/use-cases.md -->
# Speechace API Reference / Use-Cases

# Use-Cases

The Speechace API can be utilized to analyze the speaker's pronunciation, fluency, grammar, coherence and other detailed speech metrics, offering comprehensive assessments to improve language skills. The API supports the following languages:

* **English (UK)**
* **English (US)**
* **French (France)**
* **French (Canada)**
* **Spanish (Spain)**
* **Spanish (Mexico)**

### Use-cases of SpeechAce API

Speechace API is used to support scripted and spontaneous speech language learning activites, and for comprehensive assessment of spoken language.&#x20;

**Learning & Practice Activities:**

* Word and Sentence Practice with pronunciation feedback at phoneme level
* Voice driven Multiple Choice
* Read Aloud (Oral Fluency) with feedback on fluency, speaking rate, pausing, and intonation
* Spontaneous Speech Practice with scores and feedback on Fluency, Pronunciation, Grammar, Vocabulary, Coherence, and Relevance
* Phonics, letter sounds and names activities for early readers

**Assessment Activities:**

With the Speechace API you can create speaking tests to evalaute language proficiency and understand with a general academic focus, or within a particular domain (e.g. medical, customer service, marketing).

1. **Real-Time Speaking Test**
   * **Use Case**: Facilitate real-time speaking exercises with immediate feedback.
   * **Application:** Students practice speaking on given topics or prompts, receiving instant evaluations on grammar, coherence, fluency, vocabulary, and pronunciation.
2. **Coherence and Argumentation Test**
   * **Use Case**: Enhance students' ability to convey thoughts logically and coherently.
   * **Application**: Create exercises where students must argue a point or explain a concept. The API evaluates their coherence, assessing how well they structure their arguments and maintain clarity throughout their speech.
3. **Scenario-Based Test**
   * **Use Case**: Implement scenario-based training for real-world applications.
   * **Application**: Create role-playing situations (e.g., customer service interactions, negotiations) where students must respond appropriately. The API evaluates their performance based on key speaking characteristics, helping them practice contextually relevant skills.
4. **Speech Shadowing Exercise**
   * **Use Case**: Use shadowing techniques to improve fluency and pronunciation.
   * **Application**: Have students listen to audio recordings of fluent speakers and repeat what they hear. The API assesses their pronunciation and fluency, encouraging them to match the rhythm and intonation of native speakers.
5. **Interactive Phonemic Awareness Assessments**
   * **Use Case**: Evaluate students' ability to recognize and pronounce letter sounds.
   * **Application**: Use the SpeechAce API to create activities where students identify and pronounce letters while associating them with corresponding sounds (e.g., "A" for "apple").


---

<!-- source: https://api-docs.speechace.com/introduction/trusting-the-speechace-api.md -->
# Speechace API Reference / Trusting the Speechace API

# Trusting the Speechace API

In a world where AI is everywhere, businesses and end-users should rightly be concerned about where to place their trust. Often, providers don't know what is behind the models serving their use cases, or where data they send to these models might end up.

We understand these concerns and believe trust should be earned. Speechace is a 100% B2B provider that has earned the trust of Fortune 500s, the world's top Education Publishers, Institutes, and Language Learning providers. We take these concerns seriously. Speechace is designed and architected from the ground up for the safety, protection, ethical and compliant use of your data.

Let us share why you should place your trust in Speechace.

### We collect and use our own first-party data for training, with consent

When it comes to training our models, we directly source and collect our own data. First, it allows us to maintain the promise that Speechace API customer data belongs to the customer and only the customer. Second, it allows us to get first-hand control and visibility into the demographics, sources, scale, and nature of our training data. This is fundamental in planning and validating models against demographic or ethnic bias.

### You control Data Sovereignty

Speechace API supports multiple regions. When you choose your API Key, you must select a region. All data processing and storage (if configured) occurs within that regions maintaining isolation and sovereignty within each API region. API keys don't work across regions. Region endpoints are different to ensure your choice of region is explicit and the API caller's responsibility.

### You own and control the retention of your data

It follows from the above that Speechace has no need to hang on to your data. Every API customer can choose their retention policy including zero day retention. This means that after scoring your audio, we  set it to expire right away. If you do choose a longer retention period (e.g. 30 days), we only retain the data to serve support or analysis requests from you. We do not sell, share, or use your data for anything except serving you. All our data lifecycle events are audited and we can unequivocally verify when  your data has been erased.

### &#x20;Our data processing is highly available and fully automated

While Speechace is a Data Sub-processor, our service operates as a highly available cloud service running in 6 worldwide Cloud Regions and in at least 3 Availability Zones within each region. This enables us to commit to 99.95% Service Availability and to maintain no processing facilities outside the cloud, and no personnel direct involvement in data processing.

### Our AI models are rigorously and independently validated

Speechace maintains a home-grown state-of-the-art human data labeling, validation, and evaluation system. Datasets are regularly sampled and blindly rated by multiple independent professional raters. Our platform is designed to eliminate rater bias, evaluate the raters themselves first, and achieve high confidence in the evaluation datasets which drive ultimate model decisions. We maintain the largest and most diverse known non-native speaker language proficiency evaluation datasets.

### Compliance: SOC 2 Type 2 Certified

Speechace is SOC 2 Type 2 certified. Our Information Security Measures are designed and validated in supporting our customer comply with GDPR, FERPA, HIPPA, and other frameworks.

### Speechace API Technical Report

With our close partners and customers, and under NDA, we share a detailed technical report with metrics and KPIs which detail how each of our models perform, and the dataset characteristics that drive training our models. Please reach out to us at <contact@speechace.com> if you would like to gain access to our API Technical Report.


---

<!-- source: https://api-docs.speechace.com/getting-started/pre-requisites.md -->
# Speechace API Reference / Pre-requisites

# Pre-requisites

Before starting the API integration, it's important to understand the following key details to ensure a smooth and effective process:

1. API SKUs and its features
2. API Regions and Endpoints
3. API Keys
4. API Limits


---

<!-- source: https://api-docs.speechace.com/getting-started/pre-requisites/api-features.md -->
# Speechace API Reference / API Features

# API Features

Here's a detailed overview of all features of the API, including its endpoints and descriptions:

<table><thead><tr><th width="127">API</th><th width="213">Endpoint</th><th>Abilities</th></tr></thead><tbody><tr><td><strong>Score Text</strong></td><td>https://api.speechace.co/api/scoring/text/v9</td><td><ul><li>Assess pronunciation and fluency for scripted speech</li><li>Evaluate lexical stress and intonation.</li><li>Score text using markup language.</li></ul></td></tr><tr><td><strong>Score Speech</strong></td><td>https://api.speechace.co/api/scoring/speech/v9</td><td><ul><li>Transcribe and evaluate spontaneous speech</li><li>Assess Relevance given a question prompt or context</li><li>Detect or Enforce a target dialect</li><li>Provide scores and feedback on grammar, vocabulary, coherence, fluency, and pronunciation.</li></ul></td></tr><tr><td><strong>Score Task</strong></td><td>https://api.speechace.co/api/scoring/task/v9</td><td><p>Evaluate a speaker's ability to address a language task such as:</p><ul><li>Describe Image</li><li>Retell Lecture</li><li>Answer Question</li></ul></td></tr><tr><td><strong>Score Phoneme List</strong></td><td>https://api.speechace.co/api/scoring/phone_list/v9</td><td>Evaluate a custom phoneme sequence such as non-words or custom words.</td></tr><tr><td><strong>Validate text</strong></td><td>https://api.speechace.co/api/validating/text/v9</td><td>Validate if certain text is known to the Speechace lexicon.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/getting-started/pre-requisites/getting-the-api-key.md -->
# Speechace API Reference / Getting the API Key

# Getting the API Key

The Speechace API uses API keys to authenticate requests. To obtain your API keys, you need [Speechace API Subscription](https://www.speechace.com/api-plans/#pricing).

#### Follow the steps below to get your API key:

1. Go to the [Speechace API Plans](https://www.speechace.com/api-plans/#pricing).

2. Select the desired API Plan and fill the details: *Email, Phone, Company and Region*. <br>

   3. Enter your payment details and click on **Subscribe**.<br>

   4. An email will be sent to the registered email address containing the following details:<br>

   5. By clicking on the above link in the email, the API key and the endpoints will be found along with the invoicing details:\ <br>

   6. The API Key can be found in the section shown below:

   7. The invoices can be found in the section shown below:<br>

   8. The daily usage information where the count of total requests made under that subscription can be found in the section below. <br>


---

<!-- source: https://api-docs.speechace.com/getting-started/pre-requisites/api-regions-and-endpoints.md -->
# Speechace API Reference / API Regions and endpoints

# API Regions and endpoints

A Speechace API Key is tied to a specific region. The API currently supports the following regions:

{% code title="US West (Oregon)" %}

```
https://api.speechace.co
```

{% endcode %}

{% code title="AP Southeast (Singapore)" overflow="wrap" %}

```xml
https://api2.speechace.com
```

{% endcode %}

{% code title="EU West (Ireland)" %}

```markup
https://api4.speechace.com
```

{% endcode %}

{% code title="AP South (Mumbai)" %}

```
https://api5.speechace.com
```

{% endcode %}

{% hint style="info" %}
Additional API Keys can be requested under the same Subscription for other regions.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/getting-started/pre-requisites/api-limits.md -->
# Speechace API Reference / API Limits

# API Limits

The Speechace API has the following audio length and file size limits to be adhered to:

| Limit            | Basic      | Pro        | Premium   |
| ---------------- | ---------- | ---------- | --------- |
| Max Audio Length | 15 seconds | 45 seconds | 2 minutes |
| Max File Size    | 1.9 MB     | 2.5 MB     | 3.8 MB    |

{% hint style="info" %}

## **Recording Audio** <a href="#recording-audio" id="recording-audio"></a>

Speechace supports a wide range of audio formats commonly used on the web, including WAV, MP3, M4A, OGG, WEBM, and AIFF.&#x20;

To optimize file size and enhance performance, we recommend recording audio using the following settings:

* *sample size*: 16-bit
* *sample rate*: 16Khz
* *channels*: 1 (i.e. mono)
  {% endhint %}


---

<!-- source: https://api-docs.speechace.com/getting-started/api-samples.md -->
# Speechace API Reference / API Samples

# API Samples

Use SpeechAce [API Samples](https://github.com/speechace/speechace-api-samples) repository which provides a collection of example code and use cases to help developers integrate and utilize the SpeechAce API effectively.

The repository includes multiple examples demonstrating how to use the SpeechAce API for different functionalities, such as speech recognition, pronunciation assessment, and more. Samples are available in several programming languages, making it easier for developers to find examples that match their preferred coding environment.

<table><thead><tr><th width="230">Sample Folder</th><th>Description</th></tr></thead><tbody><tr><td>basic_samples</td><td>Simple single script examples calling the Speechace API in curl, node, php, and python</td></tr><tr><td>django_sample</td><td><p>Backend and Frontend implementation with: </p><ul><li>HTML5 recorder</li><li>Python server</li></ul></td></tr><tr><td>php_sample</td><td><p></p><p>Backend and Frontend implementation with: </p><ul><li>HTML5 recorder</li><li>PHP server</li></ul></td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/getting-started/supported-languages.md -->
# Speechace API Reference / Supported Languages

# Supported Languages

Below are the available API dialects along with their corresponding request codes that should be used in the API requests:

| Dialect              | Code  |
| -------------------- | ----- |
| **English (US)**     | en-us |
| **English (UK)**     | en-gb |
| **French (France)**  | fr-fr |
| **French (Canada)**  | fr-ca |
| **Spanish (Spain)**  | es-es |
| **Spanish (Mexico)** | es-mx |

{% hint style="info" %}
**Note:** Not all APIs support all of the above languages. Exceptions will be specified in the respective API guides.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/getting-started/api-versioning.md -->
# Speechace API Reference / API Versioning

# API Versioning

Speechace API versioning is managed through the request URL.&#x20;

Prior to version 9, the API required explicit versioning, where the caller specified the exact major and minor version in the request URL.&#x20;

Starting with major version 9, the API supports explicit or automatic minor version updates, giving callers the option receive incremental updates without needing to specify each minor version explicitly. API callers&#x20;

<table><thead><tr><th width="402">Request URL endpoint</th><th>Resulting version</th></tr></thead><tbody><tr><td><a href="https://api.speechace.co/scoring/text/v9/json?">https://api.speechace.co/scoring/text/v9/json?</a></td><td>This request will use the latest available minor version of v9 (e.g. v9.0 or v9.1 or v9.2 etc.)</td></tr><tr><td><a href="https://api.speechace.co/scoring/text/v9.0/json?">https://api.speechace.co/scoring/text/v9.0/json?</a></td><td>This request will explicitly use the minor version v9.0</td></tr></tbody></table>

{% hint style="info" %}
The latest API version is v9.

You can find [Major versioning history](../other-resources/appendices.md#major-versioning-history) in Appendices.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/getting-started/authentication.md -->
# Speechace API Reference / Authentication

# Authentication

The Speechace API uses [API keys](pre-requisites/getting-the-api-key.md) to authenticate requests. You can view and manage your API keys from the [dashboard](https://shop.speechace.com/subscription/profile).

### Best Practices for Securing Your API Key

API keys are essential for authenticating and authorizing access to your API. However, if not handled properly, they can be a significant security risk. Here are some best practices to ensure your API keys are protected:

**Storage and Handling**

* **Avoid hardcoding:** Never embed API keys directly into your code. This makes them vulnerable to exposure if the code is shared or compromised.
* **Environment variables:** Store API keys as environment variables. This way, they won't be part of your source code.
* **Secret management tools:** Utilize tools like HashiCorp Vault or AWS Secrets Manager to securely store and manage API keys.
* **Secure configuration files:** If you must use configuration files, encrypt them and store them in a secure location.
* **Limit access:** Restrict access to your API keys to only those who need them. Implement role-based access control (RBAC).

**Usage and Monitoring**

* **Rate limiting:** Implement rate limiting to prevent abuse and mitigate the impact of compromised keys.
* **IP address restrictions:** Limit access to specific IP addresses or networks to further enhance security.
* **Token-based authentication:** Consider using token-based authentication (e.g., OAuth 2.0) to provide more granular control and reduce the risk of compromised keys.
* **Logging and monitoring:** Log API key usage and monitor for suspicious activity. Set up alerts for unusual patterns or unauthorized access.
* **Key rotation:** Regularly rotate API keys to minimize the impact of a compromised key.

**Additional Considerations**

* **Secure communication:** Ensure your API uses HTTPS to encrypt data in transit.
* **Input validation:** Validate input to prevent injection attacks and other vulnerabilities.
* **Regular security audits:** Conduct regular security audits to identify and address potential vulnerabilities.
* **Educate developers:** Educate your development team about API security best practices.

Related detail: [Get the API Key](pre-requisites/getting-the-api-key.md)


---

<!-- source: https://api-docs.speechace.com/getting-started/try-the-speechace-api.md -->
# Speechace API Reference / Try the Speechace API

# Try the Speechace API

In this guide, sample code snippets are provided. These samples can be used to explore and understand the different types of API responses that may encountered.

## Instructions to get started with Speechace API

1. Download the following audio files:

* **apple.wav** - contains the native English pronunciation for the word apple.
* **someparents.wav** - contains the native English pronunciation for the sentence: "*Some parents admire famous athletes as strong role models, so they name their children after them*".

{% file src="/files/4zGGaPb7410tdM38uDR2" %}

{% file src="/files/FW4AjRXTUIhTCmVQkIgk" %}

2. Use the following example request to evaluate the pronunciation quality of the "Apple.wav" file available above:

{% tabs %}
{% tab title="cURL for apple.wav" %}
{% code overflow="wrap" lineNumbers="true" %}

```python
curl --form text='apple' --form user_audio_file=@/path/to/apple.wav "https://api.speechace.co/api/scoring/text/v9/json?key=Insert_Your_API_Key_Here" | python -m json.tool
```

{% endcode %}
{% endtab %}
{% endtabs %}

You can follow the [link](https://docs.speechace.com/) to try out these requests in Postman by clicking on "Run in Postman".

{% hint style="info" %}
We can also evaluate .mp3 files and many other [audio format.](pre-requisites/api-limits.md#recording-audio)
{% endhint %}

3. The expected response for above cURL command for the audio "Apple.wav", is as follows:

{% tabs %}
{% tab title="200 OK: Score a word" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "apple",
    "word_score_list": [
      {
        "word": "apple",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "ae",
            "stress_level": 1,
            "extent": [
              12,
              27
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ae"
          },
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              27,
              39
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "p"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              39,
              42
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              42,
              54
            ],
            "quality_score": 98.5,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "l"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 1,
            "stress_level": 1,
            "letters": "ap",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "extent": [
              12,
              27
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "ple",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              27,
              54
            ]
          }
        ]
      }
    ],
    "ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 90
    },
    "speechace_score": {
      "pronunciation": 100
    },
    "toeic_score": {
      "pronunciation": 200
    },
    "cefr_score": {
      "pronunciation": "C2"
    }
  },
  "version": "9.3"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}

[Related Guide: Pronunciation Scoring](../features/scripted-activities/pronunciation-scoring.md)


---

<!-- source: https://api-docs.speechace.com/getting-started/error-handling.md -->
# Speechace API Reference / Error Handling

# Error Handling

Speechace uses conventional HTTP response codes to indicate the success or failure of an API request. In general: Codes in the `2xx` range indicate success. Codes in the `4xx` range indicate an error that failed given the information provided (e.g., a required parameter was omitted, a charge failed, etc.). Codes in the `5xx` range indicate an error with Speechace's servers (these are rare).

#### **HTTP Status Code Summary**

<table><thead><tr><th width="139.33333333333331">Code</th><th width="179">Code Meaning</th><th>Description</th></tr></thead><tbody><tr><td>200</td><td>OK</td><td>Everything worked as expected.</td></tr><tr><td>400</td><td>Bad Request</td><td>The request was unacceptable, often due to missing a required parameter.</td></tr><tr><td>401</td><td>Unauthorized</td><td>No valid API key provided.</td></tr><tr><td>402</td><td>Request Failed</td><td>The parameters were valid but the request failed.</td></tr><tr><td>403</td><td>Forbidden</td><td>The API key doesn’t have permissions to perform the request.</td></tr><tr><td>404</td><td>Not Found</td><td>The requested resource doesn’t exist.</td></tr><tr><td>409</td><td>Conflict</td><td>The request conflicts with another request (perhaps due to using the same idempotent key).</td></tr><tr><td>429</td><td>Too Many Requests</td><td>Too many requests hit the API too quickly. We recommend an exponential backoff of your requests.</td></tr><tr><td>500, 502, 503, 504</td><td>Server Errors</td><td>Something went wrong on the Server's end. (These are rare.)</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/getting-started/error-handling/common-errors.md -->
# Speechace API Reference / Common Errors

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


---

<!-- source: https://api-docs.speechace.com/getting-started/error-handling/retry-strategies.md -->
# Speechace API Reference / Retry Strategies

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


---

<!-- source: https://api-docs.speechace.com/solutions/speaking-practice-for-language-learning.md -->
# Speechace API Reference / Speaking Practice for Language Learning

# Speaking Practice for Language Learning

From absolute beginners (A0) to proficient (C1/C2) speakers, Speechace API supports all the speaking activities for practice and assessment to support language learning.

In this guide we will list a variety of speaking activities for language learners, starting from beginner activities and progressing towards more advanced ones. In each activity you will find a link to a demo application you can try out, and the implementation guide for creating that activity with the Speechace API.

**Word Pronunciation:**

In a language learning app, the ideal time for pronunciation mastery is right when the learner is introduced to new vocabulary. With the Speechace API you can add pronunciation practice right into your vocabulary modules, flashcards, or quizzes.

<div data-full-width="true"></div>

Try a sample activity [here](https://app.speechace.co/placement/course/17/quiz/1/1) or follow [this guide ](../features/scripted-activities/pronunciation-scoring/word-and-sentence-pronunciation.md)to implement it using the Speechace API.

**Sentence Pronunciation:**

If you're looking to improve your users' language learning experience with engaging sentence activities, look no further than Speechace API.

Our AI-powered API gives immediate pronunciation and fluency scores with pinpointed feedback to help learners practice speaking in a natural and intelligible manner.

Try a sample activity [here](https://app.speechace.co/placement/course/17/quiz/2/1) or follow [this guide ](../features/scripted-activities/pronunciation-scoring/word-and-sentence-pronunciation.md)to implement it using the Speechace API.

**Multiple Choice:**

One of the most effective ways to add speaking practice to your eLearning courses is to voice-enable your existing multiple-choice questions.&#x20;

This allows your students to practice speaking while completing lesson activities and allows you to get more value out of already developed content without increasing your course length.

With our Basic API, you can create voice driven multiple-choice, or fill in the blank questions like the ones below.

Try a sample activity [here](https://app.speechace.co/placement/course/17/quiz/5/MC/1) or follow [this guide ](../features/scripted-activities/pronunciation-scoring/multiple-choice.md)to implement it using the Speechace API.

**Fluency:**

Read Aloud or Oral Reading Fluency (ORF) is one of the most common measures of a learner's ability to read a passage correctly while maintain a natural speaking rate and rhythm.

Our fluency scoring evaluates several aspects of speech fluency such as word correct per minute, speaking rate, pausing, hesitations, repetitions, variation in tone and many others.

Learners receive real-time feedback on their fluency performance allowing them to visualize and listen to where to focus and improve on future attempts.

Try a sample activity [here](https://app.speechace.co/placement/course/17/quiz/6/fluency/1) or follow [this guide ](../features/scripted-activities/fluency-scoring.md)to implement it using the Speechace API.

**Spontaneous Speech:**

Creating automated open-ended speaking activities can be challenging. But once implemented correctly, the benefits in scalability, consistency, and cost effectiveness of automated practice or assessment is tremendous.

Today, no API is used for spontaneous speech scoring whether in Education or in Business more than the Speechace API. Our API tackles the challenge of evaluating natural, unscripted speech, providing comprehensive scores, and a full transcript for each candidate. Our scores are aligned with standard rubrics such as CEFR, IELTS, and PTE.

Try a sample activity [here](https://speak.speechace.co) or follow [this guide ](../features/spontaneous-activities/open-ended-scoring.md)to implement it using the Speechace API.

**Task Achievement:**

How do you assess if a learner grasps specific information, a language concept or task? Humans intuitively know how to spot that, but machines require lengthy and tedious questions to achieve the same.\
\
Our Score Task API is here to solve that. With this capability you can test the learner’s comprehension, ability to respond to a prompt with correct, precise, intelligible and natural language.\
Here are some example activities you can create with this feature:

* Answering a question correctly
* Asking the right question
* Describing an image or media
* Summarizing a story or article
* Responding using particular target language

Try a sample activity [here](https://speak.speechace.co) or follow [this guide ](../features/spontaneous-activities/task-achievement-scoring.md)to implement it using the Speechace API.


---

<!-- source: https://api-docs.speechace.com/solutions/automated-language-assessment-with-ai.md -->
# Speechace API Reference / Automated Language Assessment with AI

# Automated Language Assessment with AI

Language assessment is of the first and essential skills assessments required for any job role where verbal communication is required. With the increased volume of mass hiring in industries such as BPO, Contact Centers, and Aviation, automation becomes a must.

With the Speechace API you can automate a variety of hiring screening assessments:

* Screening virtual video interviews and built-in comprehensive language assessment
* Role specific assessment such as Contact Center Agent or Flight Crew
* Business Skill language assessment, evaluating language within the context of addressing a Situational Judgement Test or Critical Thinking business task
* Summative assessment at the end of a Learning & Development unit during employee training
* Formative assessment to establish Employee baseline capabilities


---

<!-- source: https://api-docs.speechace.com/solutions/voice-ai-for-early-literacy.md -->
# Speechace API Reference / Voice AI for Early Literacy

# Voice AI for Early Literacy

Early Literacy assessment and intervention applications today can benefit greatly from applying AI to scale the scale and volume of scored activities within their product.

At Speechace, we believe that advanced Voice AI can take today's literacy products to the next level allowing teachers to achieve greater assessment, earlier detection, and structured intervention to ensure every early learner has the highest chance of mastering the foundational skills they will need throughout their K12 education.

With Speechace API, you can implement activities to practice Phonics, Letter Names and Sounds, Oral Reading Fluency, and more.

**Letter Names:**

Letter Name activities are essential for helping students recognize letters and connect them to sounds, setting the stage for successful reading and writing.

By integrating these activities into your literacy programs, you’ll enhance phonemic awareness and build the foundational skills students need for reading readiness.

Try a sample activity [here](https://speak.speechace.co/placement/p/phonics_demo/courses/4673) or follow [this guide ](../api-reference/score-text-markup-language.md)to implement it using the Speechace API.

**Letter Sounds:**

There’s a special moment when a student connects a letter to its sound — it’s the foundation of reading confidence.

Our letter sound activities are designed to make this learning process engaging and effective, helping students take that first step toward becoming fluent readers. Our markup language allows customizing the expected letter sound down to the phoneme level.

Try a sample activity [here](https://speak.speechace.co/placement/p/phonics_demo/courses/4673) or follow [this guide ](../api-reference/score-text-markup-language.md)to implement it using the Speechace API.

**Alphabet words:**

Transition from letters to words is a crucial step for every young learner, but many struggle without enough practice. This gap can slow down their progress in both reading and speaking.

Our API allows you to easily create Alphabet Words activities that help learners connect letters to words in a structured and, engaging way. With our API you can offer real-time feedback to learners, helping sharpen their word formation and pronunciation skills.

Try a sample activity [here](https://speak.speechace.co/placement/p/phonics_demo/courses/4673) or follow [this guide ](../api-reference/score-text-markup-language.md)to implement it using the Speechace API.

**Nonsense words:**

Nonsense words like "arn," "thame," or "Shuzzle" and such playful combinations of sounds not only spark creativity but can also serve practical purposes - especially when it comes to language exploration and games.

With our API, you can easily generate practice activities with your own nonsense words.

Try a sample activity [here](https://speak.speechace.co/placement/p/phonics_demo/courses/4673) or follow [this guide ](../api-reference/score-text-markup-language.md)to implement it using the Speechace API.

**Oral Reading Fluency (ORF):**

Read-aloud or Oral Reading Fluency (ORF) is one of the most common measures of a learner's ability to read a passage correctly while maintaining a natural speaking rate and rhythm.

Our fluency scoring evaluates several aspects of speech fluency such as word correct per minute, speaking rate, pausing, hesitations, repetitions, variation in tone, and many others.

Try a sample activity [here](https://app.speechace.co/placement/course/17/quiz/6/fluency/1) or follow [this guide ](../api-reference/score-text-fluency.md)to implement it using the Speechace API.


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests.md -->
# Speechace API Reference / Test Prep for Standardized tests

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


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests/pte-speaking-questions.md -->
# Speechace API Reference / PTE Speaking Questions

# PTE Speaking Questions

We will be recreating these PTE speaking questions using SpeechAce APIs:

* Read Aloud
* Repeat Sentence
* Describe Image
* Re-tell Lecture
* Answer Short Question
* Personal Introduction

### Read Aloud

*Objective*: This question type will assess test-taker's pronunciation, fluency, and intonation while reading a text aloud.

{% code title="Example Question" overflow="wrap" %}

```
The Pacific Ocean is the largest and deepest of the Earth's oceanic divisions. It extends from the Arctic Ocean in the north to the Southern Ocean in the south, and from the Asia and Australia in the west to the Americas in the east.
```

{% endcode %}

*Steps to recreate:*

1. **Display Passage on UI**: Provide a passage for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs:** Utilize the [Score Text/Fluency](../../api-reference/score-text-fluency.md) API to analyze the test-taker’s pronunciation, fluency, and intonation.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Pronunciation: `.text_score.speechace_score.pronunciation`
   2. Fluency : `.text_score.speechace_score.fluency`
   3. Content Score: `.text_score.fluency.overall_metrics.correct_word_count` divided by `.text_score.fluency.overall_metrics.word_count`

### Repeat Sentence

*Objective*: This question type will evaluate test-taker's ability to accurately repeat a sentence after hearing it once.

{% code title="Example Question" overflow="wrap" %}

```
It is estimated that over 500 sea turtles die as a result of plastic consumption every year.
```

{% endcode %}

*Steps to recreate:*

1. **Display Sentence's Audio on UI**: Provide audio recordings of the sentences which test-taker needs to repeat after hearing.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs:** Utilize the  [Score Text/Fluency](../../api-reference/score-text-fluency.md) API to analyze the test-taker’s pronunciation, fluency, and intonation.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Pronunciation: `.text_score.speechace_score.pronunciation`
   2. Fluency : `.text_score.speechace_score.fluency`
   3. Content Score: `.text_score.fluency.overall_metrics.correct_word_count` divided by `.text_score.fluency.overall_metrics.word_count`

### Describe Image

*Objective*: This question type will assess test-taker's ability to describe an image in detail and structure your response coherently.

{% code title="Example Question" overflow="wrap" %}

```
Describe the graph that shows the increase in internet usage over the past decade. Mention key trends and any notable changes.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display image on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the PTE score guide:

1. Content: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Pronunciation: `.speech_score.speechace_score.pronunciation`
3. Fluency : `.speech_score.speechace_score.fluency`

### Re-tell Lecture

*Objective*: This question type will evaluate test-taker's ability to summarize and articulate the main points of a lecture or talk.

{% code title="Example Question" overflow="wrap" %}

```
Listen to the following lecture on climate change and summarize the main causes and effects discussed.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display lecture on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the PTE score guide:

1. Content: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Pronunciation: `.speech_score.speechace_score.pronunciation`
3. Fluency : `.speech_score.speechace_score.fluency`

### Answer Short Question

*Objective*: This question type will assess test-taker's ability to respond briefly and accurately to simple questions.

{% code title="Example Question" overflow="wrap" %}

```
What do you call a system of government in which people vote for the people who will represent them?
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display question on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score field to measure components listed in the PTE score guide:

Overall task score: The `task_score,` specifically for Answer Short Question, is a straightforward binary evaluation where the test-taker’s performance is assessed as either correct (1) or incorrect (0), providing a clear measure of whether the task was achieved successfully.

### Personal Introduction

*Objective*: This question type will assess test-taker's ability to introduce themselves and provide relevant personal information.

{% code title="" overflow="wrap" %}

```
Introduce yourself and briefly describe your educational background, current occupation, and a personal interest or hobby.
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the PTE score guide:
   1. Relevance Check: `.speechace_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Vocabulary: `.speech_score.speechace_score.vocab`
   6. Grammar: `.speech_score.speechace_score.grammar`


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests/ielts-speaking-questions.md -->
# Speechace API Reference / IELTS Speaking Questions

# IELTS Speaking Questions

We will be recreating these IELTS speaking questions using SpeechAce APIs:

* Introduction and Interview
* Descriptive/Narrative/Opinion-Based Tasks
* Discussions

### Introduction and Interview

*Objective*: This question type will assess test-taker's ability to discuss their background, experiences, and daily life.

{% code title="Example Questions" overflow="wrap" %}

```
1. Can you tell me about your hometown?
2. Do you prefer to work or study alone or with others?
3. Have you ever had a memorable experience at a festival?
```

{% endcode %}

### Descriptive/Narrative/Opinion-Based Tasks

*Objective*: This question type will assess test-taker's ability to discuss and reflect on their opinions and viewpoints.

{% code title="Example Question" overflow="wrap" %}

```
1. Describe a book or film that has had an impact on your life.
2. Talk about a technology you use frequently and how it has affected your daily life.
```

{% endcode %}

### Discussions

*Objective*: This question type will assess test-taker's ability to compare and evaluate different aspects of a topic or situation.

{% code title="Example Question" overflow="wrap" %}

```
1. How does the importance of technology in education compare to its impact on social interactions?
2. Compare the benefits and drawbacks of online learning versus traditional classroom learning.
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the IELTS score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Vocabulary: `.speech_score.speechace_score.vocab`
   6. Grammar: `.speech_score.speechace_score.grammar`


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests/toefl-speaking-questions.md -->
# Speechace API Reference / TOEFL Speaking Questions

# TOEFL Speaking Questions

We will be recreating these TOEFL speaking questions using SpeechAce APIs:

* Independent Speaking Tasks
* Integrated Speaking Tasks
* Summary and Synthesis Tasks
* Academic Discussion Tasks

## Open-ended Questions

### Independent Speaking Tasks

*Objective*: To evaluate the test-taker’s ability to express personal opinions, preferences, and experiences on familiar topics.

{% code title="Example Question" overflow="wrap" %}

```
1. Do you prefer studying alone or with others? Why?
2. Do you agree or disagree with the statement that technology has made our lives more complicated? Use specific examples to support your answer.
3. If you had the choice between taking a year off from school or starting work immediately after graduation, which would you choose and why?
```

{% endcode %}

*Steps to recreate:*

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEFL score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`

## Task-oriented Questions

### Integrated Speaking Tasks

*Objective*: To assess the test-taker’s ability to integrate and respond to information from multiple sources, such as reading and listening.

{% code title="Example Questions" overflow="wrap" %}

```
Example-1
- Reading Passage: A brief article about a new university policy.
- Listening Passage: A conversation between two students discussing their views on the policy.
- Task: "Summarize the policy and explain the students’ opinions about it."

Example-2
- Listening Passage: A lecture about environmental conservation.
- Task: "Summarize the main points of the lecture and explain how they relate to the concept of sustainability."

Example-3
- Reading Passage: An article about a proposed change in campus facilities.
- Listening Passage: A conversation between students about their views on the change.
- Task: "Discuss the proposed change and the students' opinions, then give your own opinion about it."
```

{% endcode %}

### Summary and Synthesis Tasks

*Objective***:** To test the ability to accurately summarize and synthesize information from different sources.

<pre data-title="Example Question" data-overflow="wrap"><code>Example-1
- Reading Passage: A text on a particular scientific discovery.
- Listening Passage: A professor’s explanation of the discovery's applications.
- Task: "Summarize the main points of the reading passage and the professor’s explanation."

Example-2
<strong>- Reading Passage: An article comparing two theories.
</strong>- Listening Passage: A discussion between experts about the theories.
- Task: "Compare the two theories discussed in the reading and explain the experts' views on them."
</code></pre>

### Academic Discussion Tasks

*Objective***:** To evaluate the ability to engage in discussions about academic topics.

{% code title="Example Questions" overflow="wrap" %}

```
Example-1
- Listening Passage: A university lecture about climate change.
- Task: "Discuss the key points of the lecture and explain their implications for future research."

Example-2
- Listening Passage: A discussion on environmental issues.
- Task: "Describe the environmental problems mentioned in the discussion and propose possible solutions."
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the TOEFL score guide:

1. Overall task score: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Coherence: `.speech_score.speechace_score.coherence`
3. Pronunciation: `.speech_score.speechace_score.pronunciation`
4. Fluency : `.speech_score.speechace_score.fluency`

{% hint style="info" %}
Pro-tip: For a more comprehensive evaluation, including grammar, vocabulary, and coherence, consider using additional [APIs](../../api-reference/score-speech-open-ended/handling-language-scores.md) to assess these aspects as well.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests/cefr-speaking-questions.md -->
# Speechace API Reference / CEFR Speaking Questions

# CEFR Speaking Questions

We will be recreating these CEFR speaking questions using SpeechAce APIs:

* Personal Routine and Activities
* Debating, Negotiating and giving opinions
* Reflective, Evaluative and Persuasive

### Personal Routine and Activities

*Objective*: Measure the ability to recount personal experiences and explain their significance.

{% code title="Example Question" overflow="wrap" %}

```
1. Describe a time when you had to solve a problem.
2. Explain how to prepare your favorite dish.
3. Describe a recent movie you saw and explain why you enjoyed or did not enjoy it.
```

{% endcode %}

### Debating, Negotiating and giving opinions

*Objective*: Assess the ability to engage in expert-level discussions on specialized topics with precision and depth.

{% code title="Example Question" overflow="wrap" %}

```
1. Evaluate different approaches to addressing income inequality from an economic perspective.
2. Discuss the impact of historical events on current geopolitical dynamics.
3. Argue for or against the ethical implications of artificial intelligence in decision-making.
4. Debate the benefits and drawbacks of online education.
```

{% endcode %}

### Reflective, Evaluative and Persuasive

*Objective*: Assess the ability to reflect on and evaluate various aspects of personal and professional experiences.

{% code title="Example Questions" overflow="wrap" %}

```
1. Reflect on how your personal experiences have shaped your career goals.
2. Convince someone to support a cause or initiative you are passionate about.
3. Evaluate the role of lifelong learning in professional success and personal fulfillment.
4. How would you persuade others to adopt a new policy in your workplace?
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the CEFR score guide:
   1. Relevance Check: `.speech_score.relevance.class`
   2. Vocabulary: `.speech_score.speechace_score.vocab`
   3. Coherence: `.speech_score.speechace_score.coherence`
   4. Pronunciation: `.speech_score.speechace_score.pronunciation`
   5. Fluency : `.speech_score.speechace_score.fluency`
   6. Grammar: `.speech_score.speechace_score.grammar`

{% hint style="info" %}
Pro-tip: For a more comprehensive evaluation, including grammar, vocabulary, and coherence, consider using additional [APIs](../../api-reference/score-speech-open-ended/handling-language-scores.md) to assess these aspects as well.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/solutions/test-prep-for-standardized-tests/toeic-speaking-questions.md -->
# Speechace API Reference / TOEIC Speaking Questions

# TOEIC Speaking Questions

We will be recreating these TOEIC speaking questions using SpeechAce APIs:

* Read a Text Aloud
* Describe a Picture
* Respond to Questions
* Respond to a Written Prompt
* Propose a Solution
* Role Play
* Give a Presentation
* Express an Opinion
* Describe a Process
* Summarize Information

## Task-oriented Questions

### Describe a Picture

*Objective*: To evaluate the ability to describe visual information accurately and coherently.

{% code title="Example Question" overflow="wrap" %}

```
Look at the picture provided and describe what you see. Include details about the people, setting, and activities.
```

{% endcode %}

*Steps to recreate:*&#x20;

**Display image on UI, use SpeechAce API and Evaluate**: Follow this [task achievement guide](../../api-reference/score-task-task-achievement.md) to recreate this question type. You can use the following score fields to measure components listed in the TOEIC score guide:

1. Overall task score: `task_score` is the overall score used to evaluate how well the given task was achieved. It ranges from 0 to 5.
2. Coherence: `.speech_score.speechace_score.coherence`
3. Pronunciation: `.speech_score.speechace_score.pronunciation`
4. Fluency : `.speech_score.speechace_score.fluency`

### Read a Text Aloud

*Objective*: To assess pronunciation, intonation, and overall fluency in reading English text.

{% code title="Example Question" overflow="wrap" %}

```
Read the following notice aloud: 
"Due to maintenance, the elevator will be out of service until Friday."
```

{% endcode %}

*Steps to recreate:*

1. **Display Passage on UI**: Provide a passage for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the  [Score Text/Fluency](../../api-reference/score-text-fluency.md) and the [intonation](../../features/scripted-activities/lexical-stress-and-intonation.md) APIs to get the pronunciation, fluency and intonation feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEIC score guide:
   1. Intonation: `.text_score.word_intonation_list`  can be used to create intonation staircase and compare expected v/s actual intonations.
   2. Pronunciation: `.text_score.speechace_score.pronunciation`
   3. Fluency : `.text_score.speechace_score.fluency`

## Open-ended Questions

### Respond to Questions

*Objective*: To assess the ability to answer questions based on personal experiences or general knowledge.

{% code title="Example Questions" overflow="wrap" %}

```
What do you like to do in your free time? Why?
```

{% endcode %}

### Respond to a Written Prompt

*Objective*: To measure the ability to express opinions or ideas in response to a specific written scenario.

{% code title="Example Questions" overflow="wrap" %}

```
Imagine your company is planning a team-building event. What activities would you suggest? Explain why.
```

{% endcode %}

### Propose a Solution

*Objective*: To evaluate the ability to identify problems and propose feasible solutions.

{% code title="Example Question" overflow="wrap" %}

```
Your team is missing deadlines. What steps would you take to improve this situation?
```

{% endcode %}

### Role Play

*Objective*: To assess conversational skills and the ability to engage in professional interactions.

{% code title="Example Question" overflow="wrap" %}

```
You are a manager discussing a project update with your team. How would you start the conversation?
```

{% endcode %}

### Give a Presentation

*Objective*: To evaluate the ability to deliver organized, coherent presentations on a given topic.

{% code title="Example Question" overflow="wrap" %}

```
Prepare a short presentation about a recent project your team completed. Include objectives, outcomes, and lessons learned.
```

{% endcode %}

### Express an Opinion

*Objective*: To assess the ability to articulate opinions on various topics clearly and logically.

{% code title="Example Question" overflow="wrap" %}

```
Do you think remote work is more effective than working in an office? Explain your view.
```

{% endcode %}

### Describe a Process

*Objective*: To evaluate the ability to explain a process clearly and sequentially.

{% code title="Example Question" overflow="wrap" %}

```
Describe how to handle a customer complaint effectively. 
```

{% endcode %}

### Summarize Information

*Objective*: To assess the ability to listen to information and summarize it effectively.

{% code title="Example Question" overflow="wrap" %}

```
After listening to a short lecture on marketing strategies, summarize the main points presented.
```

{% endcode %}

### Recreate the questions

For all the above question types, you can refer the steps below to recreate these questions:

1. **Display question on UI**: Provide a question for the test-taker to read on your app's interface.
2. **Enable Recording**: Allow the test-taker to record their response, with an option for preparation time if needed.
3. **Use SpeechAce APIs**: Use the [Spontaneous Speech API](../../features/spontaneous-activities.md) to get the pronunciation, fluency and other feedback metrics of the test-taker's response.
4. **Evaluate and Report**: Assess the test-taker's response and present a detailed report based on the feedback metrics of your choice. You can use the following score fields to measure components listed in the TOEIC score guide:
   1. Relevance Check: `.speechace_score.relevance.class`
   2. Coherence: `.speech_score.speechace_score.coherence`
   3. Pronunciation: `.speech_score.speechace_score.pronunciation`
   4. Fluency : `.speech_score.speechace_score.fluency`
   5. Grammar: `.speech_score.speechace_score.grammar`
   6. Vocabulary: `.speech_score.speechace_score.vocab`


---

<!-- source: https://api-docs.speechace.com/solutions/speaking-practice-in-spanish-and-french.md -->
# Speechace API Reference / Speaking Practice in Spanish and French

# Speaking Practice in Spanish and French

From absolute beginners (A0) to proficient (C1/C2) speakers, Speechace API supports all the speaking activities for practice and assessment to support language learning in other languages. Speechace supports French (France, Canada) and Spanish (Spain, Latin America) today with more languages coming soon.

In this guide we will list a variety of speaking activities for language learners, starting from beginner activities and progressing towards more advanced ones. In each activity you will find a link to a demo application you can try out, and the implementation guide for creating that activity with the Speechace API.

**Word Pronunciation:**

In a language learning app, the ideal time for pronunciation mastery is right when the learner is introduced to new vocabulary. With the Speechace API you can add pronunciation practice right into your vocabulary modules, flashcards, or quizzes.

Try a sample activity [here](https://speak.speechace.co/placement/p/french-demo/courses/618) or follow [this guide ](../features/scripted-activities/pronunciation-scoring/word-and-sentence-pronunciation.md)to implement it using the Speechace API.

**Sentence Pronunciation:**

If you're looking to improve your users' language learning experience with engaging sentence activities, look no further than Speechace API.

Our AI-powered API gives immediate pronunciation and fluency scores with pinpointed feedback to help learners practice speaking in a natural and intelligible manner.

Try a sample activity [here](https://speak.speechace.co/placement/p/spanish-demo/courses/3444) or follow [this guide ](../features/scripted-activities/pronunciation-scoring/word-and-sentence-pronunciation.md)to implement it using the Speechace API.

**Fluency:**

Read Aloud or Oral Fluency is one of the most common measures of a learner's ability to read a passage correctly while maintain a natural speaking rate and rhythm.

Our fluency scoring evaluates the learner's oral fluency on the standard CEFR level and measures aspects of speech fluency such as word correct per minute, speaking rate, pausing, hesitations, repetitions, variation in tone and many others.

Learners receive real-time feedback on their fluency performance allowing them to visualize and listen to where to focus and improve on future attempts.

Try a sample activity [here](https://speak.speechace.co/placement/p/spanish-demo/courses/3444/quizzes/6491/fluency/1) or follow [this guide ](../features/scripted-activities/fluency-scoring.md)to implement it using the Speechace API.

**Spontaneous Speech:**

Creating automated open-ended speaking activities can be challenging. But once implemented correctly, the benefits in scalability, consistency, and cost effectiveness of automated practice or assessment is tremendous.

The Speechace API offers state of the art spontaneous speech transcription and scoring in French and Spanish with integrated Fluency, Pronunciation, and Task Achievement scores.

Try a sample activity [here](https://speak.speechace.co) or follow [this guide ](../features/spontaneous-activities/task-achievement-scoring.md)to implement it using the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/introduction.md -->
# Speechace API Reference / Introduction

# Introduction

The Speechace API offers a rich feature set that can be used to compute an accurate spoken language proficiency score from a given audio sample for a speaker irrespective of their gender, race and first language. At its core, the API accepts an audio file along with metadata to analyze the audio file and produce a speaking score which indicates the spoken language proficiency level of the speaker.

The API output is highly versatile and can not only produce speaking scores on a scale of 0-100 but can also produce scores for standardized rubrics such as <mark style="color:blue;">**IELTS**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**CEFR**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**TOEFL**</mark><mark style="color:blue;">,</mark> <mark style="color:blue;"></mark><mark style="color:blue;">**PTE**</mark> and <mark style="color:blue;">**TOEIC**</mark>. The Speechace team has spent years calibrating scores to align with these rubrics and currently hundreds of test prep providers use the Speechace API for providing speaking practice for these exams.

In general, the API can be used to implement two main classes of speaking activities:\
\
**a. Scripted speech activities** - In this class of activities, users are typically prompted to read aloud an answer that is visible on screen to the user. As an example, the user may be asked to speak a word such as "Apple" or the user may be asked to read a sentence such as "The quick brown fox jumped over the lazy dog". \
\
**b. Unscripted or spontaneous speech activities -** In this class of activities, the user does not know what to speak before hand and has to come up with an answer on the spot. As an example, a user may be prompted to respond to a question such as "Talk about your best friend and the things you do together.". In this case, the Speechace API can not only score language components of a user's response but also transcribe the user's response.

In the next few sections, we will review the different type of features that are available for both scripted and spontaneous activities.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities.md -->
# Speechace API Reference / Scripted activities

# Scripted activities

For implementing scripted activities, the Speechace API provides Pronunciation scoring and Fluency scoring features. Pronunciation scoring is generally used for evaluating the user's pronunciation for a word or a sentence or a multiple-choice answer. Fluency scoring is typically used to assess a user's ability to recite a longer passage or paragraph.&#x20;

In the following sections, we will review the pronunciation and fluency features in detail.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/pronunciation-scoring.md -->
# Speechace API Reference / Pronunciation Scoring

# Pronunciation Scoring

{% hint style="info" %} <mark style="color:blue;">**This feature is available as part of the Basic Plan.**</mark>
{% endhint %}

The Speechace API's pronunciation scoring can be used to determine how well a user can pronounce a given word or sentence. By using this feature, developers can create the following type of scripted pronunciation assessment activities:

* [Word and Sentence Pronunciation](pronunciation-scoring/word-and-sentence-pronunciation.md)
* [Multiple Choice](pronunciation-scoring/multiple-choice.md)
* [Custom pronunciations](pronunciation-scoring/custom-pronunciations.md)
* [Phoneme list](pronunciation-scoring/phoneme-list.md)

In the next few sections, we will review how the Speechace API can be used to create these activities.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/pronunciation-scoring/word-and-sentence-pronunciation.md -->
# Speechace API Reference / Word and Sentence pronunciation

# Word and Sentence pronunciation

{% hint style="info" %}
Use [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) for word and sentence pronunciation scoring.
{% endhint %}

To better understand the word and sentence pronunciation scoring capabilities in the Speechace API, consider the demo on the following page: [Say: After lunch I like to walk in the afternoon.](https://app.speechace.co/placement/course/17/quiz/2/1)

Here the user is being asked to pronounce the sentence "After lunch I like to walk in the afternoon". Once the user speaks the sentence, the interface shows the below screen:

<div align="left"></div>

As can be observed, the user mostly got the sentence right indicated by the letters marked in <mark style="color:green;">**Green**</mark>. However some parts of the sentence were marked in <mark style="color:red;">**Red**</mark> and the exact syllable and phoneme level mistakes are highlighted to pinpoint the errors in the user's pronunciation.

Such functionality can be built by utilizing the [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) function in the Speechace API.&#x20;

The [Score Text/Pronunciation](../../../api-reference/score-text-pronunciation.md) function accepts the prompt string, "After lunch I like to walk in the afternoon" along with the corresponding user's audio for the prompt and returns a pronunciation quality score in the range of 0 to 100 along with a detailed breakdown of pronunciation quality of words, syllables and phonemes in the user's audio.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/pronunciation-scoring/multiple-choice.md -->
# Speechace API Reference / Multiple choice

# Multiple choice

{% hint style="info" %}
Use [Score Text/Multiple choice](../../../api-reference/score-text-multiple-choice.md) for multiple choice scoring.
{% endhint %}

The multiple choice scoring capability provides an interactive way for language learners to practice pronunciation and comprehension. Using this capability, developers can create activities, wherein user is presented with a list of options to speak from and not only does the user have to speak the right option but also pronounce it correctly.

To gain a better understanding of this capability, please refer to the following demo: [How do you spell the man's name?](https://app.speechace.co/placement/course/17/quiz/4/MC/1) in which the user is being prompted to correctly spell the name as spelt in the reference audio:

As can be observed above, the user speaks the incorrect spelling of the man's name and in this case the system gives them a score of 0. This will prompt the user to make a second try as below:<br>

This time the user got the right spelling but unfortunately didn't pronounce <mark style="color:red;">**P**</mark> and <mark style="color:red;">**H**</mark> very clearly and therefore the system gives them a near perfect score of 88% but pinpoints their mistakes in Red.

This functionality can be built using the [Score Text/Multiple choice](../../../api-reference/score-text-multiple-choice.md) function in the Speechace API. This function allows developers to pass in a collection of text options along with the user's audio attempting to speak one of the options and returns the best matching option spoken by the user along with the user's pronunciation score (0-100) at word, syllable and phoneme level for that option.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/pronunciation-scoring/custom-pronunciations.md -->
# Speechace API Reference / Custom pronunciations

# Custom pronunciations

{% hint style="info" %}
Use  [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) for custom pronunciation scoring.
{% endhint %}

While measuring the pronunciation score, the Speechace engine automatically breaks down the read-aloud target script in to syllables and phonemes using a large language lexicon. However from time to time, developers run in to situations wherein the syllable/phoneme breakup provided by the Speechace API is ambiguous and to resolve such situations, the Speechace API includes a feature that allows developers to prescribe the exact syllable and phoneme breakup they desire. This feature is particularly useful in the following teaching scenarios and can be built by leveraging the [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) function:

1. **Teaching pronunciation of special acronyms, numbers, or terms -** Consider the below demo wherein user is asked to say: [Agent Double O 7 worked for M I 6](https://speak.speechace.co/placement/p/markup_demo01/courses/4960/quizzes/8886/38809)\
   \
   In this example, the user can say "Agent 0 0 7 worked for M I 6" or the user can say "Agent Double O 7 worked for M I 6", the latter being the more stylish way of speaking.\
   \
   Here a developer can use the Score Text Markup Language function to prescribe the desired pronunciation of the word 007 as Double O 7 to the Speechace API and then the Speechace API will only give a good pronunciation score if the user says Double O 7. If the user instead says zero, zero, 7 then they will not get a good score from the Speechace API.<br>

   <br>

2. **Teaching correct pronunciation of Heteronyms -** In some languages such as English, there are words called heteronyms that are spelled alike but they have different meanings. As an example, consider the word "Read" which can be pronounced as ***reed*** or ***red***. \
   \
   If you use the Speechace API to score the pronunciation of the string Read then by default it will accept both ***reed*** and ***rehd*** and return syllable and phoneme breakup for the best matching pronunciation. This may not be ideal as only one of the pronunciations may actually be correct given the larger sentence. In such cases, developers can pass a hint to the Speechace API to suggest the correct pro\
   \
   You can experience this mechanism in the below demo: [Say: He read his fragments aloud.](https://speak.speechace.co/placement/p/markup_demo01/courses/4960/quizzes/8886/38808) You can try saying "He *<mark style="color:red;">**reed**</mark>* his fragments aloud" and you should see the results in the below image.  As can be observed, the word "<mark style="color:red;">read</mark>" is marked in Red and the system suggests that the "EH" sound is missing. If try to pronounce the word read as '***rehd***' and the system will mark the word "read" in Green.

3. **Teaching correct syllables and phonemes** - As mentioned in the above description, there are a few rare times when the Speechace API may not break up the string in to the best combination of syllables and phonemes. Under such circumstances, developers can specify the desired combination of syllables and phonemes using the  [Score Text/Markup Language](../../../api-reference/score-text-markup-language.md) function.\
   \
   As an example, in the following demo, the Speechace API may return the word "nothing" as a single syllable, which is incorrect. Therefore a developer may provide a hint to the Speechace API that the word "nothing" is componsed of two syllables: **noth** & **ing**:<br>


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/pronunciation-scoring/phoneme-list.md -->
# Speechace API Reference / Phoneme list

# Phoneme list

{% hint style="info" %}
Use [Score Text/Phoneme List](../../../api-reference/score-phone-list.md) for scoring a phonetic list.
{% endhint %}

The Speechace API also offers a function to score words that are concocted using a string of phonemes. As an example, consider the word "Gotcha," which is not a true dictionary word but is widely used in American vernacular. Such a word can be scored using the [Score Text/Phoneme List](../../../api-reference/score-phone-list.md) function which accepts a text string in the form of a list of phonemes along with an audio file and provides a quality score based on how closely the pronunciation in the audio file matches the phoneme list.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/fluency-scoring.md -->
# Speechace API Reference / Fluency scoring

# Fluency scoring

{% hint style="info" %} <mark style="color:blue;">**This feature is available as part of the Pro Plan.**</mark>
{% endhint %}

The Speechace API's fluency scoring can be used to determine how well a user speaks at length. The fluency scoring measures vital characteristics in human speech such as words correct per minute, good/bad pauses in speech and articulation rate. Furthermore, Speechace's fluency scoring combines these metrics with pronunciation score to product read-aloud equivalents of <mark style="color:blue;">**IELTS**</mark>**,&#x20;**<mark style="color:blue;">**CEFR**</mark>**,&#x20;**<mark style="color:blue;">**PTE**</mark>**,&#x20;**<mark style="color:blue;">**TOEFL**</mark>  and <mark style="color:blue;">**TOEIC**</mark> scores.

In the next section, we will review how a developer can level fluency scoring in reading longer passages.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/fluency-scoring/passage-scoring.md -->
# Speechace API Reference / Passage scoring

# Passage scoring

{% hint style="info" %}
Use [Score Text/Fluency](../../../api-reference/score-text-fluency.md) for implementing fluency scoring activities.
{% endhint %}

Fluency scoring in scripted speech is typically done by prompting the user to recite long passages as in the following example: [Read the passage: I grew up playing football](https://app.speechace.co/placement/course/17/quiz/6/fluency/1) .&#x20;

Note that the fluency scoring function provides both fluency and pronunciation scores in a sample audio. Once the user has read the passage, the UX will show 2 interfaces: **Fluency** score and **Pronunciation** score.

As can be observed in the, in the fluency tab, we see metrics such as words spoken per minute along with marking of bad pauses and also a projected IELTS score that the speaker may have.&#x20;

On the other hand, if we switch to the pronunciation tab, we see the pronunciation score of the users along with their highlighted mistakes in Red as in the below image:<br>

Such functionality can be built by utilizing the [Score Text/Fluency](../../../api-reference/score-text-fluency.md) function in the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/scripted-activities/lexical-stress-and-intonation.md -->
# Speechace API Reference / Lexical stress and intonation

# Lexical stress and intonation

{% hint style="info" %} <mark style="color:blue;">**This feature is available as part of the Pro Plan.**</mark>
{% endhint %}

The Speechace API also provides a way to measure lexical stress and intonation in a user's pronunciation of a word or sentence. In teaching pronunciation, lexical stress involves emphasizing certain syllables in words to clarify meaning, while intonation refers to pitch variations that convey emotional tone and intent. Please utilize the [Score Text/Stress & Intonation](../../api-reference/score-text-stress-and-intonation.md) function to obtain stress and intonation quality scores.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities.md -->
# Speechace API Reference / Spontaneous activities

# Spontaneous activities

While users are often able to recite sentences presented in a read-aloud activity, they continue to lag in having real-life conversations. This is because readable prompts provide visual cues on how to produce sounds but they limit the user from producing original responses. Therefore merely measuring oral skills based on read-aloud scripted activities is not sufficient. To truly measure a user's spoken language abilities, we need to prompt the user in to producing an original or spontaneous response to open-ended questions. To this effect, the Speechace API provides the following capabilities:\
\
**a.** [**Open-ended scoring**](spontaneous-activities/open-ended-scoring.md)**:** This capability evaluates a user's free speech spontaneous response up to 2 minutes in length and provides a **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** score along with a **transcript** of the candidate's response.

**b.** [**Task achievement scoring**](spontaneous-activities/task-achievement-scoring.md)**:** In addition to scores provided by open-ended scoring, Task achievement scoring provides a fine grained measure of the comprehensiveness and completeness of the user's response for a given task.  \
\
In the next few sections, we will discuss these capabilities in more detail.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/open-ended-scoring.md -->
# Speechace API Reference / Open-ended scoring

# Open-ended scoring

{% hint style="info" %} <mark style="color:blue;">**This feature is available as part of the Premium Plan.**</mark>
{% endhint %}

The Speechace open-ended scoring API evaluates a user's free speech spontaneous response up to 2 minutes in length and provides a comprehensive set of language scores including **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** along with a **transcript** of the user's response.&#x20;

Additionally, the open-ended scoring API includes the following advanced capabilities to evaluate the fidelity and appropriateness of the response:\
a. **Relevance detection** - This capability evaluates if the user gave a response that was relevant to the question given to them.\
b. **Language detection** - This capability detects whether a user provided a response in an unexpected language.

In the next few sections we will look at these scores in detail.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/open-ended-scoring/language-scoring.md -->
# Speechace API Reference / Language scoring

# Language scoring

{% hint style="info" %}
Use [Score Speech/Open-ended](../../../api-reference/score-speech-open-ended.md) for scoring language skills for open-ended responses.
{% endhint %}

Speechace API's open-ended scoring capability provides feedback on the entire range of language skills including **pronunciation**, **fluency**, **vocabulary**, **grammar** and **coherence** scaled to a variety of rubrics such as CEFR, IELTS, PTE, TOEFL and TOEIC. Additionally the API provides a transcript of the user's response.

To experience the language scores, please review the following demo speaking test that has been created using the Speechace API: [Speaking test for software developers](https://speak.speechace.co/placement/courses/91/quizzes/438/speakingTest/1) . The test has 3 open ended questions and a sample report is available here: [Speaking test report for Jeevan Chopra](https://speak.speechace.co/placement/p/speaking-test/report/a497b39391/) . The report presents the following sections of interest:

#### **Overall summary of scores**

In the overall summary, scores are automatically provided in every major English evaluation rubric. Furthermore, scores are partitioned in to Pronunciation, Fluency, Vocabulary and Grammar scores.

#### **Per question scores**

As can be observed, the report provides feedback on the user’s response to each question incuding the user's transcript and the pronunciation, fluency, vocabulary, grammar and cohesion scores for each question.

Such a speaking test app can be built by using the [Score Speech/Open-ended](../../../api-reference/score-speech-open-ended.md) function in the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/open-ended-scoring/relevance-scoring.md -->
# Speechace API Reference / Relevance scoring

# Relevance scoring

{% hint style="info" %}
Use [Score Speech/Relevance](../../../api-reference/score-speech-relevance.md) for scoring the relevance of the response.
{% endhint %}

The relevance function of the Speechace API is quite unique. While passing an open-ended spontaneous audio file to the Speechace API, developers can also pass in the question prompt provided to the user. The Speechace API's relevance function will then automatically evaluate if the user's response in the audio file is relevant to the question prompt or not. If the response is found to be irrelevant then developers can award a score of 0 to the user's response. This functionality is immensely helpful in building trust with end users as they cannot cheat your spoken language assessment by speaking any random content.

{% hint style="info" %}
Note that relevance scoring does not require any training on possible answers to a prompt. The AI has been built to identify correct answers for any question automatically.
{% endhint %}

To illustrate use of the relevance metric, let’s consider an example where two responses to an open-ended question are assessed for relevance.

**Example Activity:**

1. **Question Prompt**: “What are the benefits of learning a new language?”
2. **Probable Responses:**
   * **Relevant Response**:\
     “Learning a new language has numerous benefits. First, it significantly improves communication skills. Being able to converse in another language allows individuals to connect with a wider range of people, fostering better relationships and understanding across cultures. Additionally, it opens up job opportunities. In today’s globalized world, many employers seek candidates who can communicate in multiple languages, as this can enhance business relations and customer service. Overall, the advantages extend beyond personal growth to professional opportunities.”
   * **Irrelevant Response**:\
     “I like pizza because it’s tasty. There are so many types of pizza, from pepperoni to veggie. My favorite is a Margherita with fresh basil. Pizza is great for parties and gatherings, and it’s so easy to order. Plus, you can customize it however you want, which makes it even better.”

If the user utters a relevant response as above, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an irrelevant response, then based on the [API](../../../api-reference/score-speech-open-ended.md), <mark style="color:red;">**zero score**</mark> can be assigned to the user and developer's can create a detailed report as shown below to communicate the user's failure:

Such functionality can be built by utilizing the [Score Speech/Relevance](../../../api-reference/score-speech-relevance.md) function of the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/open-ended-scoring/language-detection.md -->
# Speechace API Reference / Language detection

# Language detection

{% hint style="info" %}
Use Score[ Speechace/Language Detection](../../../api-reference/score-speech-language-detection.md) to detect unexpected languages in the user's response.
{% endhint %}

While taking AI assessments, often times users try to speak in an unexpected language to test if the system will give them a positive or negative score. To cover for such scenarios, the Speechace API includes a language detection function that can not only tell if the user spoke in a different language but can also identify the language used by the user. Developers can use this capability to develop trust in their system by identifying negative responses.

There are two ways in which the language detection can be incorporated:

1. Detect the Language and warn the user.&#x20;
2. Detect the Language and enforce the correct language.

Let’s review the following example to illustrate the warning or error the API generates when the user's response to a prompt is not in the expected language, which is English in this case:

**Question Prompt:** "Tell us something about yourself."

**User Response (in german):** "Ich bin ein begeisterter Reisender und liebe es, neue Kulturen zu entdecken. In meiner Freizeit lese ich gerne Bücher und treibe Sport. Ich interessiere mich besonders für Technologie und Innovation, und ich arbeite als Softwareentwickler in einem dynamischen Team. Außerdem genieße ich es, Zeit mit meiner Familie und meinen Freunden zu verbringen."

In situations, where the primary goal is communication rather than strict adherence to language rules, the [API](../../../api-reference/score-speech-language-detection.md) can detect the language and issue a warning without penalizing scores. In these cases, the developer can create detailed reports where scores remain unaffected as shown below:<br>

In situations where standardized testing or assessments accept only a particular language, enforcing the correct language by issuing an error helps ensure the validity of the evaluation. In these cases, the developer can create reports that reflect a zero score, penalizing the user for using the incorrect language as shown below:

Such functionality can be built by utilizing the [Speechace/Language Detection](../../../api-reference/score-speech-language-detection.md) function of the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/task-achievement-scoring.md -->
# Speechace API Reference / Task achievement scoring

# Task achievement scoring

{% hint style="info" %} <mark style="color:blue;">**This API is only available by invitation. Please email**</mark> <mark style="color:blue;"></mark><mark style="color:blue;"><contact@speechace.com></mark> <mark style="color:blue;"></mark><mark style="color:blue;">**to request access.**</mark>
{% endhint %}

The Speechace task achievement scoring API assesses the completeness and comprehensiveness of the user's response, ensuring that the user not only understood the question prompt but also communicated a clear, cohesive and accurate answer. As an example, when a speaker is tasked with analyzing conclusions from a business chart, it’s crucial to assess not only if the user's response is on-topic but also how correctly the key learnings from the business chart were described.

In addition to providing a task achievement score, this API also provides language scores available under the open-ended scoring API, including **pronunciation**, **fluency**, **vocabulary**, **grammar**, and **coherence**.

Note that the Task score is independent of language scores. To receive a high task score, the speaker must comprehend the question fully and provide a response that addresses every aspect of the question. Also, note that the task score is continuous and therefore a partial response receives a partial score.

While the task achievement technology can score a range of different tasks, we have tried to create a few well defined classes of tasks that are most likely to be used by developers. These classes are:

* **Describe-Image:** The speaker is presented with an image and asked to describe the details, relationships, and conclusion to be drawn from elements of the image. The task score of this type is on the scale of 0-5.
* **Retell-Lecture:** The speaker listens to a 1-2 minute lecture and is asked to summarize the lecture focusing on key elements, concepts and conclusions from the lecture. The task score of this type is on the scale of 0-5.
* **Answer-Question:** The speaker is presented with a short question which typically requires a one or two word answer. The task score of this type is either 0 or 1.

These classes of tasks are very useful in mocking **PTE** practice assessments. In the next few sections we will look at these task-types in detail.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/task-achievement-scoring/describe-image.md -->
# Speechace API Reference / Describe Image

# Describe Image

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function to score describe image style questions.
{% endhint %}

In order to score describe image style questions, developers can pass a description of the image along with an audio file response from a user and the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function will automatically assess how closely the audio describes description of the image.&#x20;

The image description provided to the API is used for guidance and not for pattern matching and instead, the task achievement AI looks for semantic similarity between the user's response and the image description. Semantic matching allows the user to get a high score even if they speak in their own words and their answer does not exactly resemble the image description string.

Let us review the below example to illustrate scoring a user's response to a prompt wherein the user is asked to describe an image:

**Image description:**\
“The image shows a busy street in a city. There are people walking on the sidewalk, and some are waiting at a bus stop. In the background, I can see tall buildings, and there’s a bright blue sky. A few cars are driving by, and it looks like a sunny day.”

**User's Response:**

<details>

<summary>W<strong>here task is achieved:</strong></summary>

"The image depicts a bustling city street filled with activity. People are walking briskly along the sidewalks, some chatting with friends while others are engrossed in their phones. At a nearby bus stop, a small crowd is waiting, checking schedules and exchanging quick conversations.

In the background, tall skyscrapers rise against a clear blue sky, their glass facades reflecting the sunlight. Cars and buses navigate the road, creating a dynamic scene of urban life. Street vendors are set up along the curb, offering snacks and drinks, adding to the vibrant atmosphere. Colorful banners and advertisements hang from buildings, contributing to the lively energy of the city. Overall, the image captures the essence of a busy urban environment, full of movement and life."

</details>

<details>

<summary><strong>Where task is not achieved:</strong></summary>

Aliens have long captivated human imagination, inspiring countless stories, theories, and scientific inquiries. These extraterrestrial beings are often depicted as intelligent life forms from distant planets, sparking debates about the possibility of life beyond Earth. In popular culture, aliens are portrayed in various ways, from benevolent visitors seeking to share knowledge to malevolent invaders threatening humanity. The fascination with aliens extends to scientific exploration, with initiatives like the Search for Extraterrestrial Intelligence (SETI) actively seeking signals from other civilizations. As technology advances and our understanding of the universe deepens, the question remains: are we alone, or do other life forms exist in the vast cosmos?

</details>

***

If the user utters a relevant response as above, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an irrelevant response, then based on the [API](../../../api-reference/score-task-task-achievement.md), <mark style="color:red;">**zero score**</mark> can be assigned to the user and developer's can create a detailed report as shown below to communicate the user's failure. **Please note that while the language scores can be non-zero, the task achievement score can be zero.**

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/task-achievement-scoring/re-tell-lecture.md -->
# Speechace API Reference / Re-tell Lecture

# Re-tell Lecture

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) to score how well the user summarizes an audio lecture.
{% endhint %}

In the "**Re-tell Lecture**," task type, the user is evaluated on their ability to summarize the lecture accurately in their own words. This assessment would focus on how well the user captures the main points, conveys key ideas, and presents them clearly and coherently.

In order to score describe retell-lecture style questions, developers can pass a summary of the lecture along with an audio file response from a user and the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function will automatically assess how closely the audio describes lecture.

The lecture summary provided to the API is used for guidance and not for pattern matching and instead, the task achievement AI looks for semantic similarity between the user's response and the lecture summary. Semantic matching allows the user to get a high score even if they speak in their own words and their answer does not exactly resemble the lecture summary string.

Let us review the below example on how to leverage the task achievement function to score a re-tell lecture style question:

**Question Prompt:** “Please summarize the main points of the lecture.”

**Lecture:**

<details>

<summary><strong>Lecture Title: The Importance of Renewable Energy Sources</strong></summary>

**Introduction:** Good morning, everyone. Today, we will discuss the significance of renewable energy sources and their role in creating a sustainable future. As the world faces the challenges of climate change and dwindling fossil fuel reserves, it is crucial to explore alternatives that can power our society without harming the planet.

**Main Points:**

1. **Understanding Renewable Energy:** Renewable energy comes from natural sources that are constantly replenished, such as sunlight, wind, rain, tides, and geothermal heat. Unlike fossil fuels, which can take millions of years to form, renewable resources are sustainable and can be harnessed indefinitely.
2. **Benefits of Renewable Energy:**
   * **Environmental Impact:** One of the most significant advantages of renewable energy is its potential to reduce greenhouse gas emissions. By transitioning to solar, wind, and other renewable sources, we can decrease our carbon footprint and combat climate change.
   * **Economic Benefits:** Investing in renewable energy creates jobs in manufacturing, installation, and maintenance. Additionally, renewable sources can lead to lower energy costs over time, as they rely on free natural resources.
   * **Energy Independence:** Utilizing renewable energy can reduce a country’s reliance on imported fossil fuels, enhancing energy security and stability.
3. **Challenges in Implementation:** While the benefits are clear, there are challenges to widespread adoption of renewable energy.
   * **Infrastructure Needs:** Transitioning to renewable energy requires significant investment in infrastructure, such as solar panels, wind turbines, and energy storage systems.
   * **Government Support:** Policy frameworks and incentives are necessary to encourage investment and innovation in renewable technologies.
4. **Conclusion:** In conclusion, renewable energy sources play a vital role in addressing environmental concerns, boosting the economy, and ensuring energy independence. By investing in these technologies and overcoming existing challenges, we can pave the way for a sustainable future. Thank you for your attention, and I look forward to your questions

</details>

**User Response (when task is achieved):**\
“The lecture covered the importance of renewable energy sources. The speaker emphasized how solar and wind power can reduce reliance on fossil fuels and combat climate change. Additionally, various benefits of using renewable energy, such as cost savings and environmental sustainability, were discussed. The speaker also mentioned some challenges, like the need for infrastructure improvements and government support.”

***

If the user utters a relevant response as below, the developers can create a detailed report that provides the user a positive score as below:

If the user utters an ***irrelevant*** response as below, the API will give <mark style="color:red;">**zero**</mark> task score but the language scores will be non-zero and can be presented to the user as follows:

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.


---

<!-- source: https://api-docs.speechace.com/features/spontaneous-activities/task-achievement-scoring/answer-question.md -->
# Speechace API Reference / Answer Question

# Answer Question

{% hint style="info" %}
Use [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) to assess if a question was answered correctly.
{% endhint %}

In the "**Answer Question**" task type, the user is expected to provide a concise 1-2 word response to a short question. In this case, the developer can present a boolean result—either right or wrong—since the task score for this type will be binary, resulting in either one (correct) or zero (incorrect) score.

To illustrate how to score a user's response when asked to answer a short question, consider the following example:

**Question Prompt:** “What is the capital of France?”

**User Response:**\
“Paris.”

***

If the user utters a relevant response as below, the developers can create a detailed report that provides the user a positive score as below. Note that the Task Achievement score is full, whereas the overall score is based on fluency, pronunciation and other language skills as well.

If the user utters an ***irrelevant*** response as below, the API will give <mark style="color:red;">**zero**</mark> task score but the language scores will be non-zero and can be presented to the user as follows:

Such functionality can be built by utilizing the [Score Task/Task Achievement](../../../api-reference/score-task-task-achievement.md) function of the Speechace API.


---

<!-- source: https://api-docs.speechace.com/api-reference/postman-api-reference.md -->
# Speechace API Reference / Postman API reference

# Postman API reference

In the following sections, we briefly describe ways to invoke the Speechace API using CURL. We have done so you can get a quick overview of request/response patterns and handling responses coming out of the Speechace API. At the same time, we also maintain a Postman page for reference and samples for coding in other languages. Our Postman page can be accessed via the link below:

[Speechace Postman page](https://docs.speechace.com)

Additionally at the top of each of the following sections, we will provide a link to its corresponding section on our Postman page.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-word.md -->
# Speechace API Reference / Score Word

# Score Word

{% hint style="info" %}
**Run In Postman:** [Score a Word](https://docs.speechace.com/#824b2e5a-4e68-4946-ab15-7c5bdf0e7084)
{% endhint %}

The Score Word API is optimal for scoring pronunciation of a single word. It supports scoring optimizations and model combinations targeted on utterances where the speaker is pronouncing a single word.

\
A word can be a dictionary word, a custom word (aka non-word, non-sense word) defined with markup, a Multiple Choice (MC) combination where each target is a single word.

### Request Format&#x20;

The endpoint to use will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

<mark style="color:$success;">`POST`</mark> [`https://api.speechace.co/api/scoring/word/v9/json`](https://api.speechace.co/api/scoring/writing/v9/json)

### Headers

<table><thead><tr><th width="252.4296875">Name</th><th>Value</th></tr></thead><tbody><tr><td>Content-Type</td><td><code>application/json</code></td></tr></tbody></table>

### Query Parameters

<table><thead><tr><th width="136">Parameter</th><th width="117">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><p><strong>Required</strong></p><p><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></p></td></tr><tr><td>dialect</td><td>String</td><td><p><strong>Optional</strong> <em>default = <code>en-us</code></em> </p><p><em>Supported values are: <code>en-us</code>, <code>en-gb</code>, <code>es-es</code>, <code>es-mx</code>, <code>fr-fr</code>, <code>fr-ca</code></em></p></td></tr><tr><td>user_id</td><td>String</td><td><p><strong>Optional</strong></p><p><em>A unique anonymized identifier (generated by your applications) for the end-user who submitted the response.</em></p></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="175.2265625">Parameter</th><th width="117">Type</th><th>Description</th></tr></thead><tbody><tr><td>word</td><td>String</td><td><p><strong>Required.</strong> Max length: 300 characters.</p><p><em>Word to score. Can be expressed as one of:</em></p><ul><li><em>text: (e.g. "cat")</em></li><li><em>arpa_mark: (e.g. "[cat]{k ah1 t}")</em></li><li><em>Multiple Choice with '\n' as separator: (e.g. "cat\ndog")</em></li><li><em>Multiple choice with arpa_mark: (e.g. "[cat]{k ah1 t}\n[dog]{d ao1 g})</em><br></li></ul><p><em><strong>Note:</strong> arpa_mark and Multiple choice only supported with <code>en-us</code> and <code>en-gb</code> dialects.</em></p></td></tr><tr><td>user_audio_file</td><td>File</td><td><strong>Required.</strong> Max Duration: 30 seconds.<br><em>File with user audio (wav, mp3, m4a, webm, ogg, aiff)</em></td></tr><tr><td>markup_language</td><td>String</td><td><strong>Optional</strong><br><em>Supported values are: <code>arpa_mark</code></em><br><em>Pass this parameter when using markup language in the word parameter.</em><br><br><em><strong>Note:</strong> arpa_mark is only supported with <code>en-us</code> and <code>en-gb</code> dialects.</em></td></tr><tr><td>include_interference_metrics</td><td>String</td><td><p><strong>Optional</strong><br>Use <code>include_interference_metrics=1</code> to include interference metrics in the response. When set, the <code>interference_ratio</code> field is returned inside <code>word_score</code>. <br></p><p>See the <a href="/pages/872216e6aef212f30dc166a8f5384e16b9990a5d">Detecting Speech Interference guide</a> for how to interpret and use this field.<br><br><em><strong>Note:</strong></em> <code>include_interference_metrics</code> <em>is only supported with <code>en-us</code> and <code>en-gb</code> dialects.</em></p></td></tr></tbody></table>

### Request Examples

The below examples show different modes of calling the Score Word API with the responses:

<table><thead><tr><th width="163.21484375">Example</th><th width="282.44140625">Request Body Parameters</th><th>When to Use</th></tr></thead><tbody><tr><td>A. Score a word</td><td><code>word="cat"</code></td><td><em>When scoring a single word and you just want to accept Speechace lexicon for inferring phonetic makeup of the word.</em></td></tr><tr><td>B. Score a word with markup</td><td><code>word="[read]{r iy1 d}"</code><br><code>markup_language="arpa_mark"</code></td><td><em>When you wish to pass the exact expected phonetic markup for the word.</em><br><em>For example in this case you want the present tense of "read" rather than the possibility of present or past tense.</em></td></tr><tr><td>C. Score a word with MC</td><td><code>word="cat\ndog"</code></td><td><em>When you have multiple targets in your activity.</em><br><em>For example, you show 2 pictures of a dog and a cat and the learner speaks their favorite.</em></td></tr><tr><td>D. Score a word with MC and markup</td><td><code>word="[cat]{k ah1 t}\n[dog]{d ao1 g}"</code><br><code>markup_language="arpa_mark"</code></td><td><em>When you have multiple targets but you also wish to explicitly pass the exact phonetic markup for each target.</em></td></tr></tbody></table>

{% hint style="info" %}
You can Review the full arpa\_mark syntax [here](../guides-on-common-topics/markup-language.md#markup-syntax).

**Note:** Score Word relaxes two arpa\_mark syntax constraints:

* Stress is optional (i.e. no 0,1,2 stress required on vowels)
* Syllable boundary is optional (i.e. no '|' required between word syllables)\
  \
  Speechace strongly recommends passing stress and syllable information for best scoring results.
  {% endhint %}

### Response Examples

Below are API response examples for each of the above cases: A, B, C, D.

{% tabs %}
{% tab title="A. word="cat"" %}
{% code overflow="wrap" lineNumbers="true" expandable="true" %}

```json
{
    "status": "success",
    "word_score": {
        "word": "cat",
        "quality_score": 98,
        "quality_class": "pass",
        "phone_score_list": [
            {
                "phone": "k",
                "stress_level": null,
                "extent": [
                    11,
                    17
                ],
                "quality_score": 92,
                "sound_most_like": "k"
            },
            {
                "phone": "ae",
                "stress_level": 1,
                "extent": [
                    17,
                    29
                ],
                "quality_score": 100,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "sound_most_like": "ae"
            },
            {
                "phone": "t",
                "stress_level": null,
                "extent": [
                    29,
                    47
                ],
                "quality_score": 100,
                "sound_most_like": "t"
            }
        ],
        "syllable_score_list": [
            {
                "phone_count": 3,
                "stress_level": 1,
                "letters": "cat",
                "quality_score": 97,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "extent": [
                    11,
                    47
                ]
                ]
            }
        ]
    },
    "version": "9.17"
}
```

{% endcode %}
{% endtab %}

{% tab title="B. word="\[read]{r iy1 d}"" %}
{% code overflow="wrap" lineNumbers="true" expandable="true" %}

```json
{
    "status": "success",
    "word_score": {
        "word": "read",
        "quality_score": 100,
        "quality_class": "pass",
        "phone_score_list": [
            {
                "phone": "r",
                "stress_level": null,
                "extent": [
                    5,
                    17
                ],
                "quality_score": 100,
                "sound_most_like": "r"
            },
            {
                "phone": "iy",
                "stress_level": 1,
                "extent": [
                    17,
                    29
                ],
                "quality_score": 98,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "sound_most_like": "iy"
            },
            {
                "phone": "d",
                "stress_level": null,
                "extent": [
                    29,
                    41
                ],
                "quality_score": 100,
                "sound_most_like": "d"
            }
        ],
        "syllable_score_list": [
            {
                "phone_count": 3,
                "stress_level": 1,
                "letters": "read",
                "quality_score": 99,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "extent": [
                    5,
                    41
                ]
            }
        ]
    },
    "version": "9.17"
}
```

{% endcode %}
{% endtab %}

{% tab title="C. word="cat\ndog"" %}
{% code overflow="wrap" lineNumbers="true" expandable="true" %}

```json
{
    "status": "success",
    "word_score": {
        "word": "cat",
        "quality_score": 98,
        "quality_class": "pass",
        "phone_score_list": [
            {
                "phone": "k",
                "stress_level": null,
                "extent": [
                    11,
                    17
                ],
                "quality_score": 92,
                "sound_most_like": "k"
            },
            {
                "phone": "ae",
                "stress_level": 1,
                "extent": [
                    17,
                    29
                ],
                "quality_score": 100,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "sound_most_like": "ae"
            },
            {
                "phone": "t",
                "stress_level": null,
                "extent": [
                    29,
                    47
                ],
                "quality_score": 100,
                "sound_most_like": "t"
            }
        ],
        "syllable_score_list": [
            {
                "phone_count": 3,
                "stress_level": 1,
                "letters": "cat",
                "quality_score": 97,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "extent": [
                    11,
                    47
                ]
                ]
            }
        ]
    },
    "version": "9.17"
}
```

{% endcode %}
{% endtab %}

{% tab title="D. word="\[cat]{k ah1 t}\n\[dog]{d ao1 g}"" %}
{% code overflow="wrap" lineNumbers="true" expandable="true" %}

```json
{
    "status": "success",
    "word_score": {
        "word": "cat",
        "quality_score": 98,
        "quality_class": "pass",
        "phone_score_list": [
            {
                "phone": "k",
                "stress_level": null,
                "extent": [
                    11,
                    17
                ],
                "quality_score": 92,
                "sound_most_like": "k"
            },
            {
                "phone": "ae",
                "stress_level": 1,
                "extent": [
                    17,
                    29
                ],
                "quality_score": 100,
                "stress_score": 100,
                "predicted_stress_level": 1,
                "sound_most_like": "ae"
            },
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-word/interpreting-api-response.md -->
# Speechace API Reference / Interpreting API Response

# Interpreting API Response

The API response JSON contains the following scores.

<table><thead><tr><th width="300.58984375">Field</th><th>Description</th></tr></thead><tbody><tr><td><code>word_score.word</code></td><td>The target word. If Multiple Choice was used, then this will be the closest target to what the user spoke.</td></tr><tr><td><code>word_score.quality_score</code></td><td>The pronunciation score for the word (scale 0..100). See <a href="/pages/Z3fNsFWjfOU0FvtHH5pL">this guide</a> for interpreting the quality_score.</td></tr><tr><td><code>word_score.quality_class</code></td><td>pass | fail<br>The classification for the word based on its quality_score. The threshold applied for pass is quality_score ≥ 70.</td></tr><tr><td><code>word_score.syllable_score_list</code></td><td>An array of syllables within the word, each with their quality_score, stress, and extent information.</td></tr><tr><td><code>word_score.phone_score_list</code></td><td>An array of phonemes within the word, each with their quality_score, , stress, and extent information.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/api-reference/score-word/migrating-from-score-phonelist.md -->
# Speechace API Reference / Migrating from Score PhoneList

# Migrating from Score PhoneList

The Score Word API can be a complete replacement for using [Score Phonelist](../score-phone-list.md). Score Word is the recommended API for scoring single words (whether it's a dictionary word, or a custom non-word)./&#x20;

### Converting Syntax

Score Word API allows the use of [arpa\_markup](../../guides-on-common-topics/markup-language.md) to express exact phonetic sequence. In addition, Score Word does not require stress notation or syllable boundaries. This makes converting syntax from PhoneList to Word trivial.

<table><thead><tr><th width="195.96484375">Example</th><th width="256.9765625">score phone_list syntax</th><th>score word syntax</th></tr></thead><tbody><tr><td>The word: <strong>gotcha</strong></td><td><code>phone_list="g|ao|ch|ah"</code></td><td><code>word="[gotcha]{g ao ch ah]"</code></td></tr><tr><td>The word: <strong>photographer</strong><br>* no stress or syllable boundaries</td><td><code>phone_list="f|ah|t|aa|g|r|ah|f|er"</code></td><td><code>word="[photographer]{f ah t aa g r ah f er}"</code></td></tr><tr><td>The word: <strong>photographer</strong><br>* with stress and syllable boundaries </td><td><code>phone_list="f|ah|t|aa|g|r|ah|f|er"</code></td><td><code>word="[pho|tog|ra|pher]{f ah0|t aa1 g|r ah0|f er0"</code></td></tr></tbody></table>

{% hint style="info" %}
In the second example, while it is possible to score a long multi-syllable word such as "photographer" with no stress or syllable boundaries, we recommend using stress and syllable boundaries. The information assists improving scoring especially for longer multi-syllable words.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-pronunciation.md -->
# Speechace API Reference / Score Text/Pronunciation

# Score Text/Pronunciation

{% hint style="info" %}
**Run in Postman**: [Score a word or sentence](https://docs.speechace.com/#23421587-4c6c-4a67-8575-295d50d6a55b)
{% endhint %}

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` [`https://api.speechace.co/api/scoring/text/v9/json`](https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}\&dialect=en-us\&user_id=XYZ-ABC-99001)

{% tabs %}
{% tab title="Score a word" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us \
--form 'text="apple"' \
--form 'user_audio_file=@"apple.wav"'
```

{% endcode %}
{% endtab %}

{% tab title="Score a sentence" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us \
--form 'text="Some parents admire famous athletes as strong role models, so they name their children after them."' \
--form 'user_audio_file=@"someparents.wav"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="126">Parameter</th><th width="91">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><p><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>in which the speaker will be assessed.</em> </p><p></p><p><strong>Note: Not all features of the score/text API support every dialect. Applicable dialects will be explicitly mentioned.</strong></p></td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier (generated by your application) for the end-user who spoke the audio.</em></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="196">Parameter</th><th width="98">Type</th><th>Description</th></tr></thead><tbody><tr><td>text</td><td>String</td><td><em>A word, phrase, or sentence to score. This should be in the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>chosen. For example, if <code>fr-fr</code> is the chosen dialect, then the word can be <code>Salut</code>.</em></td></tr><tr><td>user_audio_file</td><td>File</td><td><em>file with user audio (wav, mp3, m4a, webm, ogg, aiff)</em></td></tr><tr><td>question_info</td><td>String</td><td><em><strong>Optional</strong>: A unique identifier (generated by your application) for the activity or question this user audio is answering.</em></td></tr><tr><td>no_mc</td><td>String</td><td><em><strong>Optional</strong>: <code>no_mc = 1</code> to indicate the text field contains multiple lines.</em></td></tr><tr><td>include_fluency</td><td>String</td><td><em><strong>Optional:</strong></em> <code>include_fluency</code><em><code>= 1</code> to include fluency scoring in the response.</em><br><em>See this</em> <a href="/pages/Qd2DoQIAUsxraBWYNEOC"><em>guide</em></a> <em>for how to use this field.</em></td></tr><tr><td>include_intonation</td><td>String</td><td><em><strong>Optional:</strong></em> <code>include_intonation</code><em><code>= 1</code> to include intonation and stress-level scores.</em><br><em>See this</em> <a href="/pages/1trs4oYjz521JyeVRVJy"><em>guide</em></a> <em>for how to use this field.</em></td></tr><tr><td>markup_language</td><td>String</td><td><p><em><strong>Optional:</strong></em> <code>markup_language = arpa_mark</code> <br><em>This key signifies that the</em> <code>text</code> <em>parameter may contain markup annotations.</em></p><p><em>See this</em> <a href="/pages/nzyc6lz256JbaM6SIrfp"><em>guide</em></a> <em>for how to use this field.</em></p></td></tr><tr><td>include_unknown_words</td><td>String</td><td><p><strong>Optional:</strong> <code>include_unknown_words = 1</code></p><p><em>This key instructs Speechace to automatically infer the expected pronunciation for unknown terms, such as names, places, and other specific terminology. This ensures accurate feedback even for unfamiliar words.</em></p></td></tr><tr><td>detect_dialect</td><td>String</td><td><p><em><strong>Optional field</strong>: Possible values - 0 | 1</em></p><p><em>1: will apply language detection and warn if the majority of the response language is different from the intended scoring dialect.</em></p></td></tr><tr><td>enforce_dialect</td><td>String</td><td><p><em><strong>Optional field</strong>: Possible values - 0 | 1</em></p><p><em>1: will apply language detection and error if the majority of the response language is different from the intended scoring dialect.</em></p><p><em>Setting enforce_dialect=1 automatically sets detect_dialect=1.</em></p></td></tr><tr><td>include_interference_metrics</td><td>String</td><td><p><em><strong>Optional:</strong></em><br><code>include_interference_metrics=1</code> to include interference metrics in the response. When set, the <code>interference_ratio</code> field is returned inside <code>text_score</code>. <br></p><p>See the <a href="/pages/872216e6aef212f30dc166a8f5384e16b9990a5d">Detecting Speech Interference guide</a> for how to interpret and use this field.</p></td></tr></tbody></table>

### Response Example

{% tabs %}
{% tab title="200: OK Scoring a word" %}

<pre class="language-json"><code class="lang-json"><strong>{
</strong>  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "apple",
    "word_score_list": [
      {
        "word": "apple",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "ae",
            "stress_level": 1,
            "extent": [
              12,
              27
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ae"
          },
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              27,
              39
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "p"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              39,
              42
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              42,
              54
            ],
            "quality_score": 98.5,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "l"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 1,
            "stress_level": 1,
            "letters": "ap",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "extent": [
              12,
              27
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "ple",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              27,
              54
            ]
          }
        ]
      }
    ],
    "ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 90
    },
    "speechace_score": {
      "pronunciation": 100
    },
    "toeic_score": {
      "pronunciation": 200
    },
    "cefr_score": {
      "pronunciation": "C2"
    }
  },
  "version": "9.3"
}
</code></pre>

{% endtab %}

{% tab title="200: OK Success: Scoring a sentence" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "Some parents admire famous athletes as strong role models, so they name their children after them.",
    "word_score_list": [
      {
        "word": "Some",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              9,
              27
            ],
            "quality_score": 98.66666666666667,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "s"
          },
          {
            "phone": "ah",
            "stress_level": 1,
            "extent": [
              27,
              36
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              36,
              42
            ],
            "quality_score": 99.5,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "m"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "some",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              9,
              42
            ]
          }
        ]
      },
      {
        "word": "parents",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              42,
              51
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "p"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              51,
              57
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "eh"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              57,
              66
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "r"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              66,
              69
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              69,
              75
            ],
            "quality_score": 100,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "n"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              75,
              76
            ],
            "quality_score": 94,
            "word_extent": [
              5,
              6
            ],
            "sound_most_like": "t"
          },
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              76,
              84
            ],
            "quality_score": 99.25,
            "word_extent": [
              6,
              7
            ],
            "sound_most_like": "s"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "pa",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              42,
              57
            ]
          },
          {
            "phone_count": 5,
            "stress_level": 0,
            "letters": "rents",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              57,
              84
            ]
          }
        ]
      },
      {
        "word": "admire",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              84,
              90
            ],
            "quality_score": 97,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              90,
              93
            ],
            "quality_score": 100,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "d"
          },
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              93,
              102
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "m"
          },
          {
            "phone": "ay",
            "stress_level": 1,
            "extent": [
              102,
              117
            ],
            "quality_score": 99.8,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ay"
          },
          {
            "phone": "er",
            "stress_level": 0,
            "extent": [
              117,
              129
            ],
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "er"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 0,
            "letters": "ad",
            "quality_score": 98,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              84,
              93
            ]
          },
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "mi",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              93,
              117
            ]
          },
          {
            "phone_count": 1,
            "stress_level": 0,
            "letters": "re",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              117,
              129
            ]
          }
        ]
      },
      {
        "word": "famous",
        "quality_score": 98,
        "phone_score_list": [
          {
            "phone": "f",
            "stress_level": null,
            "extent": [
              129,
              141
            ],
            "quality_score": 98.75,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "f"
          },
          {
            "phone": "ey",
            "stress_level": 1,
            "extent": [
              141,
              150
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "ey"
          },
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              150,
              156
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "m"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              156,
              162
            ],
            "quality_score": 95.5,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              5
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              162,
              174
            ],
            "quality_score": 95.5,
            "word_extent": [
              5,
              6
            ],
            "sound_most_like": "s"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "fa",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              129,
              150
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "mous",
            "quality_score": 97,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              150,
              174
            ]
          }
        ]
      },
      {
        "word": "athletes",
        "quality_score": 98,
        "phone_score_list": [
          {
            "phone": "ae",
            "stress_level": 1,
            "extent": [
              174,
              186
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ae"
          },
          {
            "phone": "th",
            "stress_level": null,
            "extent": [
              186,
              195
            ],
            "quality_score": 96.66666666666667,
            "word_extent": [
              1,
              3
            ],
            "sound_most_like": "th"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              195,
              204
            ],
            "quality_score": 99.66666666666667,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "l"
          },
          {
            "phone": "iy",
            "stress_level": 2,
            "extent": [
              204,
              213
            ],
            "quality_score": 99.66666666666667,
            "stress_score": 50,
            "predicted_stress_level": 0,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "iy"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              213,
              219
            ],
            "quality_score": 97.5,
            "word_extent": [
              5,
              6
            ],
            "sound_most_like": "t"
          },
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              219,
              225
            ],
            "quality_score": 93,
            "word_extent": [
              7,
              8
            ],
            "sound_most_like": "s"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "ath",
            "quality_score": 98,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              174,
              195
            ]
          },
          {
            "phone_count": 4,
            "stress_level": 2,
            "letters": "letes",
            "quality_score": 97,
            "stress_score": 50,
            "predicted_stress_level": 0,
            "extent": [
              195,
              225
            ]
          }
        ]
      },
      {
        "word": "as",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              225,
              234
            ],
            "quality_score": 99.66666666666667,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "eh"
          },
          {
            "phone": "z",
            "stress_level": null,
            "extent": [
              234,
              240
            ],
            "quality_score": 100,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "z"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "as",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              225,
              240
            ]
          }
        ]
      },
      {
        "word": "strong",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              240,
              246
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "s"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              246,
              252
            ],
            "quality_score": 100,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "t"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              252,
              258
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "r"
          },
          {
            "phone": "ao",
            "stress_level": 1,
            "extent": [
              258,
              267
            ],
            "quality_score": 99.33333333333333,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ao"
          },
          {
            "phone": "ng",
            "stress_level": null,
            "extent": [
              267,
              273
            ],
            "quality_score": 100,
            "word_extent": [
              4,
              6
            ],
            "sound_most_like": "ng"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 5,
            "stress_level": 1,
            "letters": "strong",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              240,
              273
            ]
          }
        ]
      },
      {
        "word": "role",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              273,
              282
            ],
            "quality_score": 99.66666666666667,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "r"
          },
          {
            "phone": "ow",
            "stress_level": 1,
            "extent": [
              282,
              291
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "ow"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              291,
              300
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "l"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "role",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              273,
              300
            ]
          }
        ]
      },
      {
        "word": "models",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              300,
              312
            ],
            "quality_score": 98.25,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "m"
          },
          {
            "phone": "aa",
            "stress_level": 1,
            "extent": [
              312,
              321
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "aa"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              321,
              327
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "d"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              327,
              333
            ],
            "quality_score": 96,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              333,
              339
            ],
            "quality_score": 100,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "l"
          },
          {
            "phone": "z",
            "stress_level": null,
            "extent": [
              339,
              354
            ],
            "quality_score": 100,
            "word_extent": [
              5,
              6
            ],
            "sound_most_like": "z"
          }
        ],
        "ending_punctuation": ",",
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "mo",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              300,
              321
            ]
          },
          {
            "phone_count": 4,
            "stress_level": 0,
            "letters": "dels",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              321,
              354
            ]
          }
        ]
      },
      {
        "word": "so",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              390,
              408
            ],
            "quality_score": 99.33333333333333,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "s"
          },
          {
            "phone": "ow",
            "stress_level": 1,
            "extent": [
              408,
              414
            ],
            "quality_score": 99.5,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "ow"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "so",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              390,
              414
            ]
          }
        ]
      },
      {
        "word": "they",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "dh",
            "stress_level": null,
            "extent": [
              414,
              420
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              2
            ],
            "sound_most_like": "dh"
          },
          {
            "phone": "ey",
            "stress_level": 1,
            "extent": [
              420,
              429
            ],
            "quality_score": 98.33333333333333,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              2,
              4
            ],
            "sound_most_like": "ey"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "they",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              414,
              429
            ]
          }
        ]
      },
      {
        "word": "name",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              429,
              435
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "n"
          },
          {
            "phone": "ey",
            "stress_level": 1,
            "extent": [
              435,
              444
            ],
            "quality_score": 99.66666666666667,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "ey"
          },
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              444,
              453
            ],
            "quality_score": 98,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "m"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "name",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              429,
              453
            ]
          }
        ]
      },
      {
        "word": "their",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "dh",
            "stress_level": null,
            "extent": [
              453,
              459
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              2
            ],
            "sound_most_like": "dh"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              459,
              465
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "eh"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              465,
              468
            ],
            "quality_score": 96,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "r"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "their",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              453,
              468
            ]
          }
        ]
      },
      {
        "word": "children",
        "quality_score": 98,
        "phone_score_list": [
          {
            "phone": "ch",
            "stress_level": null,
            "extent": [
              468,
              483
            ],
            "quality_score": 97.6,
            "word_extent": [
              0,
              2
            ],
            "sound_most_like": "ch"
          },
          {
            "phone": "ih",
            "stress_level": 1,
            "extent": [
              483,
              489
            ],
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "ih"
          },
          {
            "phone": "l",
            "stress_level": null,
            "extent": [
              489,
              495
            ],
            "quality_score": 100,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "l"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              495,
              498
            ],
            "quality_score": 100,
            "word_extent": [
              4,
              5
            ],
            "sound_most_like": "d"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              498,
              507
            ],
            "quality_score": 95.66666666666667,
            "word_extent": [
              5,
              6
            ],
            "sound_most_like": "r"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              507,
              510
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              6,
              7
            ],
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              510,
              519
            ],
            "quality_score": 96.66666666666667,
            "word_extent": [
              7,
              8
            ],
            "sound_most_like": "n"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "chil",
            "quality_score": 99,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              468,
              495
            ]
          },
          {
            "phone_count": 4,
            "stress_level": 0,
            "letters": "dren",
            "quality_score": 98,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              495,
              519
            ]
          }
        ]
      },
      {
        "word": "after",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "ae",
            "stress_level": 1,
            "extent": [
              519,
              531
            ],
            "quality_score": 99.75,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "word_extent": [
              0,
              1
            ],
            "sound_most_like": "ae"
          },
          {
            "phone": "f",
            "stress_level": null,
            "extent": [
              531,
              540
            ],
            "quality_score": 100,
            "word_extent": [
              1,
              2
            ],
            "sound_most_like": "f"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              540,
              546
            ],
            "quality_score": 100,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "t"
          },
          {
            "phone": "er",
            "stress_level": 0,
            "extent": [
              546,
              549
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "word_extent": [
              3,
              5
            ],
            "sound_most_like": "er"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "af",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 2,
            "extent": [
              519,
              540
            ]
          },
          {
            "phone_count": 2,
            "stress_level": 0,
            "letters": "ter",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "extent": [
              540,
              549
            ]
          }
        ]
      },
      {
        "word": "them",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "dh",
            "stress_level": null,
            "extent": [
              549,
              555
            ],
            "quality_score": 100,
            "word_extent": [
              0,
              2
            ],
            "sound_most_like": "dh"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              555,
              564
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "word_extent": [
              2,
              3
            ],
            "sound_most_like": "eh"
          },
          {
            "phone": "m",
            "stress_level": null,
            "extent": [
              564,
              582
            ],
            "quality_score": 100,
            "word_extent": [
              3,
              4
            ],
            "sound_most_like": "m"
          }
        ],
        "ending_punctuation": ".",
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "them",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              549,
              582
            ]
          }
        ]
      }
    ],
    "ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 90
    },
    "speechace_score": {
      "pronunciation": 99
    },
    "toeic_score": {
      "pronunciation": 200
    },
    "cefr_score": {
      "pronunciation": "C2"
    }
  },
  "version": "9.3"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-pronunciation/handling-overall-scores.md -->
# Speechace API Reference / Handling overall scores

# Handling overall scores

The **overall score** provides an assessment of the pronunciation quality for the spoken word or sentence. This numerical score indicates how closely the pronunciation matches the expected standard.&#x20;

Refer to the JSON example below to see how these key elements appear in the response:

{% code lineNumbers="true" %}

```json
"ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 90
    },
    "speechace_score": {
      "pronunciation": 100
    },
    "toeic_score": {
      "pronunciation": 200
    },
    "cefr_score": {
      "pronunciation": "C2"
    }
```

{% endcode %}

{% hint style="info" %}
Refer to the scoring [guide](../../guides-on-common-topics/interpreting-overall-scores.md) to interpret the scores.<br>
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-pronunciation/handling-word-scores.md -->
# Speechace API Reference / Handling word scores

# Handling word scores

The **word-level quality score** helps you assess the pronunciation accuracy of a specific word within a sentence. This score provides insight into how well that particular word was pronounced compared to the expected standard.

<div align="left"></div>

{% hint style="info" %}
The scale of the quality score can be interpreted from the scoring [guide](../../guides-on-common-topics/intepreting-quality-score.md).
{% endhint %}

You can use the `quality_score` at the word level to display the analysis of the spoken response in your app using the following [color codes](../../guides-on-common-topics/intepreting-quality-score.md):

<table><thead><tr><th width="118">Score</th><th width="95">Color</th><th>Description</th></tr></thead><tbody><tr><td>90 - 100</td><td>Green</td><td>Excellent. Native or native-like</td></tr><tr><td>80 - 90</td><td>Green</td><td>Very Good and clearly intelligible.</td></tr><tr><td>70 - 80</td><td>Orange</td><td>Good. Intelligible but with one or two evident mistakes.</td></tr><tr><td>60 - 70</td><td>Red</td><td>Fair. Possibly not intelligible with several evident mistakes.</td></tr><tr><td>0 - 60</td><td>Red</td><td>Poor and must be reattempted.</td></tr></tbody></table>

For example, in the case shown below, the `quality_score` for the word "walk" is 65. Consequently, it is displayed in <mark style="color:red;">**RED**</mark> in the question based on the test-taker's response, indicating several evident mistakes in pronunciation.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-pronunciation/handling-phoneme-and-syllable-scores.md -->
# Speechace API Reference / Handling phoneme and syllable scores

# Handling phoneme and syllable scores

Just as we have word-level pronunciation quality scores, we also assess pronunciation at the phoneme and syllable levels:

<div align="left"></div>

<div align="left"></div>

Additionally, the **lexical stress level** measures how accurately the stressed syllables or phonemes in a word are pronounced compared to the expected stress pattern. In this context, `stress_level` represents the expected stress level, while `predicted_stress_level` indicates the actual stress level determined by the pronunciation evaluation. Possible values are:

1. `0`: unstressed&#x20;
2. `1`: primary stress&#x20;
3. `2`: secondary stress

Note that **lexical stress score** evaluates the quality of stress applied to syllables or phonemes, ranging from 0 to 100. A higher score indicates better accuracy in stress placement according to the expected pattern.

Lastly note the **Sound most like** value&#x73;**, which** indicate how the pronounced phoneme sounded compared to the expected phoneme.

<div align="left"></div>


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-multiple-choice.md -->
# Speechace API Reference / Score Text/Multiple choice

# Score Text/Multiple choice

{% hint style="info" %}
**Run in Postman:** [Score a Multiple Choice Text](https://docs.speechace.com/#c916e0af-af9f-484a-a0dd-e9672c72e3a1)
{% endhint %}

In this example, we will match the test-taker's utterance to the closest option provided in the Multiple Choice Question and evaluate the pronunciation of the spoken word or sentence. For instance, if the multiple-choice options are:

* Apple
* Banana
* Orange

The test-taker’s response will be scored based on how closely it matches one of these options, and the pronunciation of the spoken word or sentence will be assessed for accuracy.

The request parameters which you can see in the cURL below are the same as in [Score Text/Pronunciation](score-text-pronunciation.md). Note that the options {Apple, Banana and Orange} are specified in the **text** field and are separated by newline characters (or \n).

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="\"apple
orange
banana\""'
--form 'user_audio_file=@"orang_16k.wav"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

In the above cURL, the audio file is matched to the closest choice, which is "Orange." Therefore, the response evaluates the pronunciation of the word "Orange":

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "orange",
    "word_score_list": [
      {
        "word": "orange",
        "quality_score": 77,
        "phone_score_list": [
          {
            "phone": "ao",
            "stress_level": 1,
            "extent": [
              79,
              87
            ],
            "quality_score": 99.625,
            "stress_score": 100,
            "sound_most_like": "ao"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              87,
              96
            ],
            "quality_score": 96,
            "sound_most_like": "r"
          },
          {
            "phone": "ih",
            "stress_level": 0,
            "extent": [
              96,
              105
            ],
            "quality_score": 60.33333333333334,
            "stress_score": 100,
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              105,
              117
            ],
            "quality_score": 65.08333333333334,
            "sound_most_like": "ng"
          },
          {
            "phone": "jh",
            "stress_level": null,
            "extent": [
              117,
              129
            ],
            "quality_score": 61.5,
            "sound_most_like": "g",
            "child_phones": [
              {
                "extent": [
                  117,
                  126
                ],
                "quality_score": 70.33333333333333,
                "sound_most_like": "k"
              },
              {
                "extent": [
                  126,
                  129
                ],
                "quality_score": 35,
                "sound_most_like": "ng"
              }
            ]
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "or",
            "quality_score": 98,
            "stress_score": 100,
            "extent": [
              79,
              96
            ]
          },
          {
            "phone_count": 3,
            "stress_level": 0,
            "letters": "ange",
            "quality_score": 62,
            "stress_score": 100,
            "extent": [
              96,
              129
            ]
          }
        ]
      }
    ],
    "speechace_score": {
      "pronunciation": 77
    }
  },
  "version": "9.0"
}
```


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-multiple-choice/handling-multiple-choice-response.md -->
# Speechace API Reference / Handling multiple choice response

# Handling multiple choice response

Firstly, your app should identify the correct answer from the provided multiple-choice options. Once the test-taker responds, their answer should be compared to the expected correct answer. Only if the answer is correct should the app provide pronunciation feedback. The interpretation of the spoken word or sentence remains the same as under [Score Text/Pronunciation](../score-text-pronunciation.md), where feedback includes a review of the syllables and phonemes.

Like in the example below, the response is matched to the expected correct answer and accordingly the pronunciation feedback metrics are provided:


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-markup-language.md -->
# Speechace API Reference / Score Text/Markup Language

# Score Text/Markup Language

{% hint style="info" %}
**Run in Postman:** [Scoring text using markup language](https://docs.speechace.com/#63ea84b0-b0d9-432f-a9ad-b83dff1a4d28)
{% endhint %}

Markup language allows you to override Speechace lexicon and define your own.

### Markup Syntax

When `markup_language=arpa_mark` is specified, the input text can have markups on zero, one or multiple words. This flag can be used in scoring pronunciation, multiple choice or[ Validate Text ](score-text-validate-text.md)requests.

Each markup has the pattern: `[l1|l2|...|ln]{s1|s2|...|sn}`, where:

* `l1`, `l2`, ..., `ln` are substrings of a word in the input text, and
* `s1`, `s2`, ..., `sn` are syllables of the word corresponding to `l1`, `l2`, ..., `ln`.
* Each syllable, `si`, has this pattern: `p1` `p2` ... `pk`, where `pi` is a phoneme in [ARPABET notation](../guides-on-common-topics/phonetic-notation.md) for the dialect.&#x20;
* If `pi` is a vowel phoneme, `pi` ends with 0, 1 or 2 to denote the stress level of the syllable.
* There should be at most one vowel per syllable.

{% hint style="info" %}
Let's take a word "Nothing" and divide it into its syllables and phonemes along with its stress level to understand it better:

1. **\[noth | ing]**: "Nothing" has two syllables, "noth" and "ing".
2. **{n ah1 th | ih0 ng}**: This part is a phonetic transcription where:
   1. **n**: Represents the phoneme /n/, as in "no".
   2. **ah1**: Represents the stressed vowel phoneme /ʌ/, as in "cup". The "1" indicates primary stress.
   3. **th**: Represents the phoneme /θ/, as in "think".
   4. **ih0**: Represents the vowel phoneme /ɪ/, as in "sit", with "0" indicating no stress or secondary stress.
   5. **ng**: Represents the phoneme /ŋ/, as in "sing".
      {% endhint %}

### Markup Language Use-cases

1. Marking up a word to explicitly specify syllable boundaries and phoneme mapping\
   `There was [noth|ing]{n ah1 th|ih0 ng} on the rock.`
2. Specifying which word is intended in a heteronym (i.e. 2 words which share the same spelling but have different pronunciation and meaning). *Here the heteronyms are "read" and "fragments".*\
   `He [read]{r eh1 d} his [frag|ments]{f r ae1 g|m ah0 n t s} aloud.`
3. Handling special acronyms, numbers, or terms\
   `Agent [0||||07]{d ah1 | b ah0 l | ow1 | s eh1 | v ah0 n} worked for MI6.`

{% hint style="info" %}
Note: In the above example in order to map 007 to "Double-O Seven" and no other possible pronunciation of the number "007", we create multiple empty syllables in the word "007".&#x20;

For detailed explanation of markup language, refer the Markup Language [guide](../guides-on-common-topics/markup-language.md).
{% endhint %}

### Request Response Example

We will evaluate the sentence using the markup language applied to the word "read," as demonstrated below:

{% code overflow="wrap" %}

```
I love to [read]{r iy1 d}. Last year I [read]{r eh1 d} Anna Karenina by [Tol|stoy]{t ow1 l|s t oy2}.
```

{% endcode %}

The request parameters which you can see in the cURL below can be found in [Score Text/Pronunciation](score-text-pronunciation.md).

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}' \
--form 'text="I love to [read]{r iy1 d}. Last year I [read]{r eh1 d} Anna Karenina by [Tol|stoy]{t ow1 l|s t oy2}."' \
--form 'user_audio_file=@"ilovetoread.mp3"' \
--form 'markup_language="arpa_mark"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

Notice the different phonemes for both instances of the word "read" in the sentence and compare them with the phonemes present in the response for the same words.

{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "I love to read. Last year I read Anna Karenina by Tolstoy.",
    "word_score_list": [
    {....<word score for other words},
    {
        "word": "read",
        "quality_score": 100,
        "phone_score_list": [
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              63,
              72
            ],
            "quality_score": 99.33333333333333,
            "sound_most_like": "r"
          },
          {
            "phone": "iy",
            "stress_level": 1,
            "extent": [
              72,
              87
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "iy"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              87,
              99
            ],
            "quality_score": 100,
            "sound_most_like": "d"
          }
        ],
        "ending_punctuation": ".",
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "read",
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              63,
              99
            ]
          }
        ]
      },
      {....<word score for other words},
      {
        "word": "read",
        "quality_score": 84,
        "phone_score_list": [
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              213,
              222
            ],
            "quality_score": 99.66666666666667,
            "sound_most_like": "r"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              222,
              231
            ],
            "quality_score": 55.222222222222214,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "iy"
          },
          {
            "phone": "d",
            "stress_level": null,
            "extent": [
              231,
              240
            ],
            "quality_score": 97.33333333333333,
            "sound_most_like": "d"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 3,
            "stress_level": 1,
            "letters": "read",
            "quality_score": 84,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "extent": [
              213,
              240
            ]
          }
        ]
      },
      {....<word score for other words},
      ],
    "ielts_score": {
      "pronunciation": 8.5
    },
    "pte_score": {
      "pronunciation": 83
    },
    "speechace_score": {
      "pronunciation": 94
    }
  },
  "version": "9.1"
}
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-markup-language/handling-markup-response.md -->
# Speechace API Reference / Handling Markup Response

# Handling Markup Response

The interpretation of the spoken word or sentence scores remains the same as described in the sections under [Score Text/Pronunciation](../score-text-pronunciation.md). The difference is that the expected phonemes in a word can be provided by the developer (as explained in the [guide](../../guides-on-common-topics/markup-language.md)), rather than being determined automatically by SpeechAce.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-stress-and-intonation.md -->
# Speechace API Reference / Score Text/Stress & Intonation

# Score Text/Stress & Intonation

{% hint style="info" %}
**Run in Postman:** [Score Lexical Stress and Intonation](https://docs.speechace.com/#cb64e480-d7cd-408c-b34e-a5221292eac7)
{% endhint %}

If the `include_intonation=1` parameter is included in the request body for [Score Text/Pronunciation](score-text-pronunciation.md), then the API result will include lexical and intonation metrics. In this example, lexical (word) stress and intonation (text stress) are evaluated in a given passage.

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="Some parents admire famous athletes as strong role models, so they name their children after them."' \
--form 'user_audio_file=@"someparents.wav"' \
--form 'include_intonation="1"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

{% tabs %}
{% tab title="200: OK Lexical Stress and Intonation" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "Some parents admire famous athletes as strong role models, so they name their children after them.",
    "word_score_list": [
    {<...pronunciation metrices for other words>},
    {
        "word": "parents",
        "quality_score": 99,
        "phone_score_list": [
          {
            "phone": "p",
            "stress_level": null,
            "extent": [
              42,
              51
            ],
            "quality_score": 100,
            "sound_most_like": "p"
          },
          {
            "phone": "eh",
            "stress_level": 1,
            "extent": [
              51,
              57
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 1,
            "sound_most_like": "eh"
          },
          {
            "phone": "r",
            "stress_level": null,
            "extent": [
              57,
              66
            ],
            "quality_score": 100,
            "sound_most_like": "r"
          },
          {
            "phone": "ah",
            "stress_level": 0,
            "extent": [
              66,
              69
            ],
            "quality_score": 100,
            "stress_score": 100,
            "predicted_stress_level": 0,
            "sound_most_like": "ah"
          },
          {
            "phone": "n",
            "stress_level": null,
            "extent": [
              69,
              75
            ],
            "quality_score": 100,
            "sound_most_like": "n"
          },
          {
            "phone": "t",
            "stress_level": null,
            "extent": [
              75,
              76
            ],
            "quality_score": 94,
            "sound_most_like": "t"
          },
          {
            "phone": "s",
            "stress_level": null,
            "extent": [
              76,
              84
            ],
            "quality_score": 99.25,
            "sound_most_like": "s"
          }
        ],
        "syllable_score_list": [
          {
            "phone_count": 2,
            "stress_level": 1,
            "letters": "pa",
            "quality_score": 100,
            "stress_score": 100,
            "intonation": [
              null,
              "FALL"
            ],
            "pitch_range": [
              226.71435643564357,
              211.65643564356435
            ],
            "predicted_stress_level": 1,
            "extent": [
              42,
              57
            ]
          },
          {
            "phone_count": 5,
            "stress_level": 0,
            "letters": "rents",
            "quality_score": 99,
            "stress_score": 100,
            "intonation": [
              null,
              "FALL"
            ],
            "pitch_range": [
              211.65643564356435,
              184.5521782178218
            ],
            "predicted_stress_level": 0,
            "extent": [
              57,
              84
            ]
          }
        ]
      },
      {<...pronunciation metrices for other words>},
      ],
    "word_intonation_list": [
      {
        "word": "Some",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "parents",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "admire",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ],
          [
            "FALL",
            "RISE"
          ]
        ]
      },
      {
        "word": "famous",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "athletes",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "as",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "strong",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "role",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "models",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "so",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "they",
        "syllable_intonation_list": [
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "name",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ]
        ]
      },
      {
        "word": "their",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "children",
        "syllable_intonation_list": [
          [
            "RISE",
            "FALL"
          ],
          [
            null,
            "RISE"
          ]
        ]
      },
      {
        "word": "after",
        "syllable_intonation_list": [
          [
            "FALL",
            "RISE"
          ],
          [
            null,
            "FALL"
          ]
        ]
      },
      {
        "word": "them",
        "syllable_intonation_list": [
          [
            null,
            "FALL"
          ]
        ]
      }
    ],
    "ielts_score": {
      "pronunciation": 9
    },
    "pte_score": {
      "pronunciation": 89
    },
    "speechace_score": {
      "pronunciation": 99
    }
  },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-stress-and-intonation/handing-stress-and-intonation-response.md -->
# Speechace API Reference / Handing stress and intonation response

# Handing stress and intonation response

The API result will include the following lexical and intonation metrics:

1. `syllable_score_list[]`
   * `stress_level`: ***Expected*** lexical stress level based on the Lexicon. Possible values ar&#x65;**,**
     * 0: unstressed
     * 1: primary stress
     * 2: secondary stress
   * `predicted_stress_level`: ***Detected*** lexical stress level based on the user audio. Possible values ar&#x65;**,**
     * 0: unstressed
     * 1: primary stress
     * 2: secondary stress
   * `stress_score`: Floating point number between 0 and 100 indicating the correctness of the user's stress level.
   * `pitch_range[]`: \[begin\_pitch, end\_pitch] - recorded for this syllable in Hertz.
2. `word_intonation_list[]`
   * `syllable_intonation_list[]`: \[pitch\_change\_from\_previous, pitch\_change\_in\_current]
     * **pitch\_change\_from\_previous:** indicates what is the pitch of a word from the previous syllable to the beginning of the current one&#x20;
       * If we can’t recognize the syllable (due to error from our side or due to user not saying the syllable), the value is `null`.&#x20;
       * If we can recognize the syllable but couldn’t determine the pitch (due to error from our side or due to user reducing the sound to unvoiced), the value is `REDUCED`.
     * **pitch\_change\_in\_current:** indicates what is the pitch of a word from the beginning to the end of the current syllable
       * This value is `null` unless one of the following cases occurs.
         * If the pitch of the syllable falls, but the starting pitch of the syllable is higher than the ending pitch of previous syllable, secondary intonation is `RISE` while primary intonation is `FALL`.&#x20;
         * If the pitch of the syllable rises, but the starting pitch of the syllable is lower than the ending pitch of the previous syllable, secondary intonation is `FALL` while primary intonation is `RISE.`
       * Possible values
         * `RISE`
         * `FALL`
         * `FLAT`
         * `REDUCED`
         * `null`

The lexical stress and the intonation results can be read as given in the example below:

{% hint style="info" %}

```json5
word = "parent"
```

{% code overflow="wrap" lineNumbers="true" %}

```json5
"syllable_score_list": [
{
  "phone_count": 2,
  "stress_level": 1,
  "letters": "pa",
  "quality_score": 100,
  "stress_score": 100,
  "intonation": [
    null,
    "FALL"
  ],
  "pitch_range": [
    226.71435643564357,
    211.65643564356435
  ],
  "predicted_stress_level": 1,
  "extent": [
    42,
    57
  ]
},
```

{% endcode %}

So if the array is `intonation = [null, FALL]` as seen above, then,

* from the previous syllable to the beginning of the current one, that is from *`some`* to *`pa`* = NULL
* from the beginning to the end of the current syllable, that is from start of *`pa`* to end of *`pa`* = FALL
  {% endhint %}

You can use the intonation array and pitch range to compare the expected intonation against the actual intonation, visualizing this as an intonation staircase. This will enhance the test-taker's reading and speaking skills.

<div align="left"></div>


---

<!-- source: https://api-docs.speechace.com/api-reference/score-phone-list.md -->
# Speechace API Reference / Score Phone List

# Score Phone List

{% hint style="info" %}
**Run in Postman:** [Score a Phoneme list](https://docs.speechace.com/#d3d86348-4d1e-4ecb-a991-778f037f5f12)
{% endhint %}

With the phone list API, you can score a sequence of phonemes that make up any word or non-word.&#x20;

Individual words, such as "Gotcha," which are American vernacular and not valid dictionary words, can be scored using the phoneme list API. The phoneme list uses a different url endpoint and expects the list of phonemes in [Arpabet notation](https://en.wikipedia.org/wiki/Arpabet).&#x20;

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` [`https://api.speechace.co/api/scoring/phone_list/v9/json`](https://api.speechace.co/api/scoring/phone_list/v9/json?key={{speechacekey}}\&user_id=XYZ-ABC-99001\&dialect=en-us)

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/phone_list/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'phone_list="g|ao|ch|ah"' \
--form 'user_audio_file=@"gotcha.wav"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="146">Parameter</th><th width="89">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>to be scored. Supported only for en-us and en-gb</em></td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier for the end-user who spoke the audio.</em></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="240">Parameter</th><th width="98">Type</th><th>Description</th></tr></thead><tbody><tr><td>phone_list</td><td>String</td><td><em>A phoneme list to score. For example:</em> <code>g|ao|ch|ah</code></td></tr><tr><td>user_audio_file</td><td>File</td><td><em>file with user audio (wav, mp3, m4a, webm, ogg, aiff).</em></td></tr><tr><td>question_info</td><td>String</td><td><em><strong>Optional flag</strong>: A unique identifier (generated by your application) for the activity or question this user audio is answering.</em></td></tr></tbody></table>

### Response Example

The API response includes `phone_score_list[]` : a list of phonemes with it's own `quality_score`.&#x20;

Each element within the `phone_score_list[]` also includes its own **`quality_score`**, its **`extent`** information indicating its start and end, and additional fields like the actual **`sound_most_like`** phone based on the speaker's attempt.&#x20;

{% tabs %}
{% tab title="200: OK Phoneme List" %}
{% code lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "word_score": {
    "word": "g|ao|ch|ah",
    "quality_score": 97,
    "phone_score_list": [
      {
        "phone": "g",
        "stress_level": null,
        "extent": [
          51,
          60
        ],
        "quality_score": 100,
        "sound_most_like": "g"
      },
      {
        "phone": "ao",
        "stress_level": null,
        "extent": [
          60,
          72
        ],
        "quality_score": 91,
        "sound_most_like": "ao"
      },
      {
        "phone": "ch",
        "stress_level": null,
        "extent": [
          72,
          87
        ],
        "quality_score": 100,
        "sound_most_like": "ch"
      },
      {
        "phone": "ah",
        "stress_level": null,
        "extent": [
          87,
          102
        ],
        "quality_score": 96,
        "sound_most_like": "ah"
      }
    ]
  },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-phone-list/handling-phone-list-response.md -->
# Speechace API Reference / Handling Phone List response

# Handling Phone List response

The interpretation of the key elements in the response of the spoken word or sentence even in terms of phonemes remains the same as previously described under the [Score Text/Pronunciation](../score-text-pronunciation.md) function.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-fluency.md -->
# Speechace API Reference / Score Text/Fluency

# Score Text/Fluency

{% hint style="info" %}
**Run in Postman:** [Score Fluency](https://docs.speechace.com/#c34b11dd-8172-441a-bc27-223339d48d8e)
{% endhint %}

Fluency and Pronunciation are scored based on a speaker reading aloud a passage for up to [45 seconds.](#user-content-fn-1)[^1] The request parameters which you can see in the cURL below are the same as in the [Score Text/Pronunciation](score-text-pronunciation.md) function. Please review below sections on making a simple CURL based request to the fluency scoring function:

{% tabs %}
{% tab title="curl" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechace_prokey}}&dialect=en-us' \
--form 'text="Yes, I do. Travel today is vastly different than what it used to be. In the past, a traveller had little idea about what to expect when they arrived at their destination. These days, the internet connects our world in ways previous generations could only dream about. We can instantly review destination information and make travel arrangements. Also, in the past, people could only travel by land or sea and travelling was often long and unsafe."' \
--form 'user_audio_file=@"./traveltoday.wav"' \
--form 'include_fluency="1"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

#### Query Parameters <a href="#query-parameters" id="query-parameters"></a>

| Parameter | Type   | Description                                                                                                                                                                                                                                                                                                               |
| --------- | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| key       | String | *API* [*key*](https://app.gitbook.com/o/8miq9EcDdZOxBexpQST9/s/KaDVy4leVobAfOfVNuic/getting-started/pre-requisites/getting-the-api-key/~/comments) *issued by Speechace.*                                                                                                                                                 |
| dialect   | String | *This is the* [*dialect*](https://app.gitbook.com/o/8miq9EcDdZOxBexpQST9/s/KaDVy4leVobAfOfVNuic/getting-started/supported-languages/~/comments) *in which the speaker will be assessed.*&#x200B;**Note: Not all features of the score/text API support every dialect. Applicable dialects will be explicitly mentioned.** |
| user\_id  | String | ***Optional**: A unique anonymized identifier (generated by your application) for the end-user who spoke the audio.*                                                                                                                                                                                                      |

#### Request Body <a href="#request-body" id="request-body"></a>

| Parameter               | Type   | Description                                                                                                                                                                                                                                                                                                                              |
| ----------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| text                    | String | *A word, phrase, or sentence to score. This should be in the* [*dialect*](https://app.gitbook.com/o/8miq9EcDdZOxBexpQST9/s/KaDVy4leVobAfOfVNuic/getting-started/supported-languages/~/comments) *chosen. For example, if `fr-fr` is the chosen dialect, then the word can be `Salut`.*                                                   |
| user\_audio\_file       | File   | *file with user audio (wav, mp3, m4a, webm, ogg, aiff)*                                                                                                                                                                                                                                                                                  |
| question\_info          | String | ***Optional**: A unique identifier (generated by your application) for the activity or question this user audio is answering.*                                                                                                                                                                                                           |
| no\_mc                  | String | ***Optional**: `no_mc = 1` to indicate the text field contains multiple lines.*                                                                                                                                                                                                                                                          |
| include\_fluency        | String | Set `include_fluency`*`= 1` to include fluency scoring in the response.*                                                                                                                                                                                                                                                                 |
| include\_intonation     | String | ***Optional:*** `include_intonation`*`= 1` to include intonation and stress-level scores. See this* [*guide*](https://app.gitbook.com/o/8miq9EcDdZOxBexpQST9/s/KaDVy4leVobAfOfVNuic/features/scripted-activities/lexical-stress-and-intonation/~/comments) *for how to use this field.*                                                  |
| markup\_language        | String | ***Optional:*** `markup_language = arpa_mark` *This key signifies that the* `text` *parameter may contain markup annotations.See this* [*guide*](https://app.gitbook.com/o/8miq9EcDdZOxBexpQST9/s/KaDVy4leVobAfOfVNuic/features/scripted-activities/pronunciation-scoring/custom-pronunciations/~/comments) *for how to use this field.* |
| include\_unknown\_words | String | **Optional:** `include_unknown_words = 1`*This key instructs Speechace to automatically infer the expected pronunciation for unknown terms, such as names, places, and other specific terminology. This ensures accurate feedback even for unfamiliar words.*                                                                            |
| detect\_dialect         | String | ***Optional field**: Possible values - 0 \| 11: will apply language detection and warn if the majority of the response language is different from the intended scoring dialect.*                                                                                                                                                         |
| enforce\_dialect        | String | ***Optional field**: Possible values - 0 \| 11: will apply language detection and error if the majority of the response language is different from the intended scoring dialect.Setting enforce\_dialect=1 automatically sets detect\_dialect=1.*                                                                                        |

The responses below show a correct response, and a response where the speaker doesn't read the complete passage and as a result triggers fidelity detection.&#x20;

{% hint style="info" %}
[Fidelity detection](score-text-fluency.md#fidelity-detection) identifies when the test-taker deviates from intended utterance and API detects incomplete or off-script attempts.
{% endhint %}

{% tabs %}
{% tab title="Response OK" %}
{% code overflow="wrap" lineNumbers="true" %}

```json5
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "Yes, I do. Travel today is vastly different than what it used to be. In the past, a traveller had little idea about what to expect when they arrived at their destination. These days, the internet connects our world in ways previous generations could only dream about. We can instantly review destination information and make travel arrangements. Also, in the past, people could only travel by land or sea and travelling was often long and unsafe.",
    "word_score_list": [<...word scores for the words in the utterance>],
    "ielts_score": {<...ielts score>},
    "pte_score": {<...pte score>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr scores>},
    "fluency": {
      "segment_metrics_list": [
        {
          "segment": [
            0,
            3
          ],
          "duration": 1.005,
          "articulation_length": 0.69,
          "syllable_count": 3,
          "correct_syllable_count": 3,
          "correct_word_count": 3,
          "word_count": 3,
          "speech_rate": 2.985074626865672,
          "articulation_rate": 4.347826086956522,
          "syllable_correct_per_minute": 179.10447761194033,
          "word_correct_per_minute": 179.10447761194033,
          "all_pause_count": 1,
          "all_pause_duration": 0.315,
          "mean_length_run": 0.69,
          "max_length_run": 0.69,
          "all_pause_list": [
            [
              144,
              175.5
            ]
          ],
          "ielts_score": {
            "pronunciation": 9,
            "fluency": 8.5
          },
          "pte_score": {
            "pronunciation": 90,
            "fluency": 90
          },
          "speechace_score": {
            "pronunciation": 100,
            "fluency": 93
          },
          "toeic_score": {
            "pronunciation": 200,
            "fluency": 190
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "C2"
          }
        },
        {
          "segment": [
            3,
            14
          ],
          "duration": 3.525,
          "articulation_length": 2.82,
          "syllable_count": 15,
          "correct_syllable_count": 14,
          "correct_word_count": 11,
          "word_count": 11,
          "speech_rate": 4.25531914893617,
          "articulation_rate": 5.319148936170213,
          "syllable_correct_per_minute": 238.29787234042553,
          "word_correct_per_minute": 187.2340425531915,
          "all_pause_count": 2,
          "all_pause_duration": 0.705,
          "mean_length_run": 2.82,
          "max_length_run": 2.82,
          "all_pause_list": [
            [
              175.5,
              207
            ],
            [
              489,
              528
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 8
          },
          "pte_score": {
            "pronunciation": 90,
            "fluency": 88
          },
          "speechace_score": {
            "pronunciation": 95,
            "fluency": 91
          },
          "toeic_score": {
            "pronunciation": 190,
            "fluency": 180
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "C1+"
          }
        },
        {
          "segment": [
            14,
            32
          ],
          "duration": 5.205,
          "articulation_length": 4.52,
          "syllable_count": 29,
          "correct_syllable_count": 25,
          "correct_word_count": 15,
          "word_count": 18,
          "speech_rate": 5.571565802113352,
          "articulation_rate": 6.415929203539823,
          "syllable_correct_per_minute": 288.1844380403458,
          "word_correct_per_minute": 172.9106628242075,
          "all_pause_count": 3,
          "all_pause_duration": 0.685,
          "mean_length_run": 2.26,
          "max_length_run": 3.95,
          "all_pause_list": [
            [
              528,
              567
            ],
            [
              624,
              625
            ],
            [
              1020,
              1048.5
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 7.5
          },
          "pte_score": {
            "pronunciation": 89,
            "fluency": 74
          },
          "speechace_score": {
            "pronunciation": 92,
            "fluency": 82
          },
          "toeic_score": {
            "pronunciation": 190,
            "fluency": 170
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "C1"
          }
        },
        {
          "segment": [
            32,
            47
          ],
          "duration": 6.045,
          "articulation_length": 4.92,
          "syllable_count": 25,
          "correct_syllable_count": 22,
          "correct_word_count": 13,
          "word_count": 15,
          "speech_rate": 4.1356492969396195,
          "articulation_rate": 5.08130081300813,
          "syllable_correct_per_minute": 218.36228287841192,
          "word_correct_per_minute": 129.03225806451613,
          "all_pause_count": 3,
          "all_pause_duration": 1.125,
          "mean_length_run": 2.46,
          "max_length_run": 2.85,
          "all_pause_list": [
            [
              1048.5,
              1077
            ],
            [
              1362,
              1422
            ],
            [
              1629,
              1653
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 8
          },
          "pte_score": {
            "pronunciation": 89,
            "fluency": 86
          },
          "speechace_score": {
            "pronunciation": 92,
            "fluency": 90
          },
          "toeic_score": {
            "pronunciation": 190,
            "fluency": 180
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "C1+"
          }
        },
        {
          "segment": [
            47,
            57
          ],
          "duration": 4.05,
          "articulation_length": 3.63,
          "syllable_count": 22,
          "correct_syllable_count": 18,
          "correct_word_count": 9,
          "word_count": 10,
          "speech_rate": 5.432098765432099,
          "articulation_rate": 6.0606060606060606,
          "syllable_correct_per_minute": 266.6666666666667,
          "word_correct_per_minute": 133.33333333333334,
          "all_pause_count": 2,
          "all_pause_duration": 0.42,
          "mean_length_run": 3.63,
          "max_length_run": 3.63,
          "all_pause_list": [
            [
              1653,
              1677
            ],
            [
              2040,
              2058
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 7
          },
          "pte_score": {
            "pronunciation": 90,
            "fluency": 66
          },
          "speechace_score": {
            "pronunciation": 93,
            "fluency": 77
          },
          "toeic_score": {
            "pronunciation": 190,
            "fluency": 160
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "B2"
          }
        },
        {
          "segment": [
            57,
            76
          ],
          "duration": 5.76,
          "articulation_length": 5.16,
          "syllable_count": 26,
          "correct_syllable_count": 23,
          "correct_word_count": 16,
          "word_count": 19,
          "speech_rate": 4.513888888888889,
          "articulation_rate": 5.038759689922481,
          "syllable_correct_per_minute": 239.58333333333334,
          "word_correct_per_minute": 166.66666666666669,
          "all_pause_count": 2,
          "all_pause_duration": 0.6,
          "mean_length_run": 2.58,
          "max_length_run": 2.85,
          "all_pause_list": [
            [
              2058,
              2076
            ],
            [
              2361,
              2403
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 8.5
          },
          "pte_score": {
            "pronunciation": 90,
            "fluency": 90
          },
          "speechace_score": {
            "pronunciation": 95,
            "fluency": 92
          },
          "toeic_score": {
            "pronunciation": 190,
            "fluency": 190
          },
          "cefr_score": {
            "pronunciation": "C2",
            "fluency": "C2"
          }
        }
      ],
      "overall_metrics": {
        "segment": [
          0,
          76
        ],
        "duration": 25.59,
        "articulation_length": 21.74,
        "syllable_count": 120,
        "correct_syllable_count": 105,
        "correct_word_count": 67,
        "word_count": 76,
        "speech_rate": 4.6893317702227435,
        "articulation_rate": 5.519779208831647,
        "syllable_correct_per_minute": 246.18991793669403,
        "word_correct_per_minute": 157.0926143024619,
        "all_pause_count": 8,
        "all_pause_duration": 3.85,
        "mean_length_run": 2.4155555555555552,
        "max_length_run": 3.95,
        "all_pause_list": [
          [
            144,
            207
          ],
          [
            489,
            567
          ],
          [
            624,
            625
          ],
          [
            1020,
            1077
          ],
          [
            1362,
            1422
          ],
          [
            1629,
            1677
          ],
          [
            2040,
            2076
          ],
          [
            2361,
            2403
          ]
        ]
      },
      "fluency_version": null
    }
  },
  "version": "9.8"
}
    
```

{% endcode %}
{% endtab %}

{% tab title="Response Incomplete" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "text_score": {
    "text": "Yes, I do. Travel today is vastly different than what it used to be. In the past, a traveller had little idea about what to expect when they arrived at their destination. These days, the internet connects our world in ways previous generations could only dream about. We can instantly review destination information and make travel arrangements. Also, in the past, people could only travel by land or sea and travelling was often long and unsafe.",
    "word_score_list": [<...word scores for the words in the utterance>],
    "ielts_score": {<...ielts score>},
    "pte_score": {<...pte score>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr scores>},
    "score_issue_list": [
      {
        "status": "warning",
        "short_message": "response_incomplete",
        "detail_message": "The response doesn't follow the script completely.",
        "source": "fluency"
      }
    ],
    "fluency": {
      "segment_metrics_list": [
        {
          "segment": [
            0,
            3
          ],
          "duration": 1.135,
          "articulation_length": 0.87,
          "syllable_count": 3,
          "correct_syllable_count": 3,
          "correct_word_count": 3,
          "word_count": 3,
          "speech_rate": 2.643171806167401,
          "articulation_rate": 3.4482758620689657,
          "syllable_correct_per_minute": 158.59030837004406,
          "word_correct_per_minute": 158.59030837004406,
          "all_pause_count": 3,
          "all_pause_duration": 0.265,
          "mean_length_run": 0.29,
          "max_length_run": 0.41,
          "all_pause_list": [
            [
              54,
              55
            ],
            [
              63,
              64
            ],
            [
              105,
              129.5
            ]
          ],
          "ielts_score": {
            "pronunciation": 9,
            "fluency": 8.5
          },
          "pte_score": {
            "pronunciation": 89,
            "fluency": 85
          },
          "speechace_score": {
            "pronunciation": 99,
            "fluency": 96
          }
        },
        {
          "segment": [
            3,
            14
          ],
          "duration": 2.965,
          "articulation_length": 2.61,
          "syllable_count": 15,
          "correct_syllable_count": 14,
          "correct_word_count": 10,
          "word_count": 11,
          "speech_rate": 5.059021922428331,
          "articulation_rate": 5.74712643678161,
          "syllable_correct_per_minute": 283.3052276559865,
          "word_correct_per_minute": 202.36087689713324,
          "all_pause_count": 8,
          "all_pause_duration": 0.355,
          "mean_length_run": 0.32625,
          "max_length_run": 0.47,
          "all_pause_list": [
            [
              129.5,
              154
            ],
            [
              198,
              199
            ],
            [
              246,
              250
            ],
            [
              297,
              298
            ],
            [
              333,
              334
            ],
            [
              348,
              350
            ],
            [
              357,
              358
            ],
            [
              396,
              397
            ]
          ],
          "ielts_score": {
            "pronunciation": 8.5,
            "fluency": 8
          },
          "pte_score": {
            "pronunciation": 83,
            "fluency": 77
          },
          "speechace_score": {
            "pronunciation": 94,
            "fluency": 89
          }
        },
        {
          "segment": [
            14,
            32
          ],
          "duration": 0,
          "articulation_length": 0,
          "syllable_count": 29,
          "correct_syllable_count": 0,
          "correct_word_count": 0,
          "word_count": 18,
          "speech_rate": 0,
          "articulation_rate": 0,
          "syllable_correct_per_minute": 0,
          "word_correct_per_minute": 0,
          "all_pause_count": 0,
          "all_pause_duration": 0,
          "mean_length_run": 0,
          "max_length_run": 0,
          "all_pause_list": [],
          "ielts_score": {
            "pronunciation": 0,
            "fluency": 0
          },
          "pte_score": {
            "pronunciation": 10,
            "fluency": 10
          },
          "speechace_score": {
            "pronunciation": 0,
            "fluency": 0
          }
        },
        {
          "segment": [
            32,
            47
          ],
          "duration": 0,
          "articulation_length": 0,
          "syllable_count": 26,
          "correct_syllable_count": 0,
          "correct_word_count": 0,
          "word_count": 15,
          "speech_rate": 0,
          "articulation_rate": 0,
          "syllable_correct_per_minute": 0,
          "word_correct_per_minute": 0,
          "all_pause_count": 0,
          "all_pause_duration": 0,
          "mean_length_run": 0,
          "max_length_run": 0,
          "all_pause_list": [],
          "ielts_score": {
            "pronunciation": 0,
            "fluency": 0
          },
          "pte_score": {
            "pronunciation": 10,
            "fluency": 10
          },
          "speechace_score": {
            "pronunciation": 0,
            "fluency": 0
          }
        },
        {
          "segment": [
            47,
            57
          ],
          "duration": 0,
          "articulation_length": 0,
          "syllable_count": 22,
          "correct_syllable_count": 0,
          "correct_word_count": 0,
          "word_count": 10,
          "speech_rate": 0,
          "articulation_rate": 0,
          "syllable_correct_per_minute": 0,
          "word_correct_per_minute": 0,
          "all_pause_count": 0,
          "all_pause_duration": 0,
          "mean_length_run": 0,
          "max_length_run": 0,
          "all_pause_list": [],
          "ielts_score": {
            "pronunciation": 0,
            "fluency": 0
          },
          "pte_score": {
            "pronunciation": 10,
            "fluency": 10
          },
          "speechace_score": {
            "pronunciation": 0,
            "fluency": 0
          }
        },
        {
          "segment": [
            57,
            76
          ],
          "duration": 0,
          "articulation_length": 0,
          "syllable_count": 27,
          "correct_syllable_count": 0,
          "correct_word_count": 0,
          "word_count": 19,
          "speech_rate": 0,
          "articulation_rate": 0,
          "syllable_correct_per_minute": 0,
          "word_correct_per_minute": 0,
          "all_pause_count": 0,
          "all_pause_duration": 0,
          "mean_length_run": 0,
          "max_length_run": 0,
          "all_pause_list": [],
          "ielts_score": {
            "pronunciation": 0,
            "fluency": 0
          },
          "pte_score": {
            "pronunciation": 10,
            "fluency": 10
          },
          "speechace_score": {
            "pronunciation": 0,
            "fluency": 0
          }
        }
      ],
      "overall_metrics": {
        "segment": [
          0,
          76
        ],
        "duration": 4.1,
        "articulation_length": 3.48,
        "syllable_count": 122,
        "correct_syllable_count": 17,
        "correct_word_count": 13,
        "word_count": 76,
        "speech_rate": 29.75609756097561,
        "articulation_rate": 35.05747126436782,
        "syllable_correct_per_minute": 248.78048780487808,
        "word_correct_per_minute": 190.2439024390244,
        "all_pause_count": 10,
        "all_pause_duration": 0.62,
        "mean_length_run": 0.31636363636363635,
        "max_length_run": 0.47,
        "all_pause_list": [
          [
            54,
            55
          ],
          [
            63,
            64
          ],
          [
            105,
            154
          ],
          [
            198,
            199
          ],
          [
            246,
            250
          ],
          [
            297,
            298
          ],
          [
            333,
            334
          ],
          [
            348,
            350
          ],
          [
            357,
            358
          ],
          [
            396,
            397
          ]
        ]
      },
      "fluency_version": null
    }
  },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}

[^1]: This limit is 2 minutes with a Premium API plan.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-fluency/handling-fluency-response.md -->
# Speechace API Reference / Handling fluency response

# Handling fluency response

The pronunciation interpretation of the spoken word or sentence remains the same as in the [Score Text/Pronunciation](../score-text-pronunciation.md) function. To interpret the fluency quality, refer to the below key elements:

**Overall Fluency Scores**

These scores assist test creators in evaluating the overall fluency of spoken responses, offering insights into the quality of the test-taker's speech. Below is an example of how the fluency score is presented. For detailed interpretation, please refer to the [overall score guide](../../guides-on-common-topics/interpreting-overall-scores.md), which includes scales from systems such as IELTS, PTE, and Speechace.

<div align="left"></div>

**Fluency Metrics**

The API returns the following feedback metrics under the `fluency` node:

| Field                          | Description                                                                                                                                                                                       |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| duration                       | total length of speech in seconds                                                                                                                                                                 |
| articulation                   | total length of articulation (speech minus pauses, hesitations and non-speech events such as laughter). Excludes beginning silence on very first segment and ending silence on very last segment. |
| speech\_rate                   | speaking rate in syllables per second.                                                                                                                                                            |
| syllable\_count                | Count of syllables in this segment                                                                                                                                                                |
| word\_count                    | Count of words in this segment                                                                                                                                                                    |
| correct\_syllable\_count       | Count of correctly spoken syllables in this segment                                                                                                                                               |
| correct\_word\_count           | Count of correctly spoken words in this segment                                                                                                                                                   |
| syllable\_correct\_per\_minute | correct\_syllable\_count / duration in mins                                                                                                                                                       |
| word\_correct\_per\_minute     | correct\_word\_count / duration in mins                                                                                                                                                           |
| all\_pause\_count              | count of all pauses (filled and unfilled) which are longer than the minimum pause threshold                                                                                                       |
| all\_pause\_duration           | total duration of all pauses (filled and unfilled) in seconds                                                                                                                                     |
| all\_pause\_list\[]            | a list of all the pauses with the begin/end markers for each in extents of 10 msecs                                                                                                               |
| mean\_length\_run              | mean length of run in syllables between pauses                                                                                                                                                    |
| max\_length\_run               | max length of run in syllables between pauses                                                                                                                                                     |
| segment\_metrics\_list\[]      | A list of segments within the overall text/audio with the fluency metrics for each segment.                                                                                                       |

The following are the most commonly used metrics to provide feedback to the user:

1. `word_correct_per_minute`: This measures the count of words per minute. You can color-code the test-taker's rate and compare it to the standard rate of 120 words per minute, which is widely considered the minimum fluent speaking rate.
2. `all_pause_list []`: This is a list of all pauses, with each pause marked by begin and end times, accurate to within 10 milliseconds. Identify and display the locations of medium pause duration (≥500 milliseconds) and long pause duration (>1 second) based on the length and positions of entries in the `all_pause_list[]`.
3. `duration` and `articulation`: Display the duration and articulation length to show how much time the user spent speaking compared to pausing or using fillers.

   `a. duration` : The total length of the speech in seconds, including all pauses, fillers, and non-speech events.

   `b. articulation`: The total length of actual articulation, calculated as the total speech duration minus pauses, hesitations, non-speech events (such as laughter), and excluding any silence at the very beginning and very end of the speech.

<div align="left"></div>


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-fluency/fidelity-detection.md -->
# Speechace API Reference / Fidelity detection

# Fidelity detection

One of the unique capabilities of the fluency scoring function is that it can detect incomplete or off-script attempts where the speaker deviates from the intended utterance.&#x20;

If fidelity detection is enabled and the test taker deviates from reading the passage by not fully adhering to the test script or spoke freely instead of following the scripted prompts then the API will reduce the overall scores and return a `score_issue_list[]` entry such as the below:

{% hint style="info" %}
Please review additional details on [interpreting fidelity scores](../../guides-on-common-topics/interpreting-fidelity-class.md).
{% endhint %}

{% code overflow="wrap" lineNumbers="true" %}

```json
"score_issue_list":
[
  {
    "status": "warning",
    "short_message": "response_incomplete",
    "detail_message": "The response doesn't follow the script completely.",
    "source": "fluency"
  }
]
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-text-validate-text.md -->
# Speechace API Reference / Score Text/Validate Text

# Score Text/Validate Text

{% hint style="info" %}
**Run in Postman:** [Validate text](https://docs.speechace.com/#00742f28-ea92-44ba-b174-631c4d854642)
{% endhint %}

This API should be used to ensure that all words in the speaker's response are included in the Speechace lexicon. This API enables quick verification of whether authored content can be accurately scored by Speechace, helping to prevent errors during the text authoring process.

Words not found in the lexicon can be reported to `support@speechace.com` for potential inclusion. Alternatively, the [phoneme list API](../features/scripted-activities/pronunciation-scoring/phoneme-list.md) can be utilized to handle out-of-lexicon terms.

Additionally, the Speechace API offers an option to [automatically manage unknown words](../guides-on-common-topics/automatic-handling-of-unknown-words.md) by determining the most likely phonetic mapping for each term.

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` `https://api.speechace.co/api/scoring/text/v9/json`

{% tabs %}
{% tab title="Validate text with unknown words" %}
{% code overflow="wrap" lineNumbers="true" %}

```curl
curl --location -g --request POST 'https://api.speechace.co/api/validating/text/v9/json?key={{speechacekey}}&text=%22Validate%20these%20words%20existeee.%22&dialect=en-us'
```

{% endcode %}
{% endtab %}

{% tab title="Validate text with known words" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g --request POST 'https://api.speechace.co/api/validating/text/v9/json?key={{speechacekey}}&text=%22Validate%20these%20words%20exist.%22&dialect=en-us'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="124">Parameter</th><th width="97">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API key issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the dialect in which the speaker will be assessed. Supported only for en-us and en-gb.</em></td></tr><tr><td>text</td><td>String</td><td><em>A sentence or sequence of words to validate. For example:</em><br><em>- With unknown words:</em> <code>Validate these words existeee.</code><br>- <em>With known words:</em> <code>Validate these words exist.</code></td></tr></tbody></table>

### Response Example

If unknown words are found, the `error_unknown_words` [error](../getting-started/error-handling/common-errors.md) will be highlighted, accompanied by a detailed explanation provided in the `detail_message` parameter.&#x20;

{% tabs %}
{% tab title="Error: Validate text with unknown words" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "error",
  "short_message": "error_unknown_words",
  "detail_message": "existeee"
}
```

{% endcode %}
{% endtab %}

{% tab title="200: OK Validate text with known words " %}
{% code lineNumbers="true" %}

```json5
{
  "status": "success",
  "quota_remaining": -1
}
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Interpret the response

If any unknown word is found, it will be highlighted under the key `detail_message` with the error = `error_unknown_words`.


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended.md -->
# Speechace API Reference / Score Speech/Open-ended

# Score Speech/Open-ended

{% hint style="info" %}
**Run in Postman:** [Language Scores](https://docs.speechace.com/#2f536692-1645-4a14-b729-ef781e1390a1)
{% endhint %}

### Request Format

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>

`POST` [`https://api.speechace.co/api/scoring/speech/v9/json`](https://api.speechace.co/api/scoring/speech/v9/json)

{% tabs %}
{% tab title="Score Language Skills" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"traveltoday.wav"' \
--form 'include_ielts_feedback=1'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="134">Parameter</th><th width="95">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>in which the speaker will be assessed.</em> Supported values are: en-us, en-gb, fr-fr, fr-ca, es-es, es-mx</td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier (generated by your applications) for the end-user who spoke the audio.</em></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="215">Parameter</th><th width="107">Type</th><th>Description</th></tr></thead><tbody><tr><td>user_audio_file</td><td>File</td><td><em>file with user audio (wav, mp3, m4a, webm, ogg, aiff)</em></td></tr><tr><td>question_info</td><td>String</td><td><em><strong>Optional flag</strong>: A unique identifier (generated by your application) for the activity or question this user audio is answering.</em></td></tr><tr><td>include_ielts_feedback</td><td>String</td><td><code>1</code><br><em><strong>Optional</strong>:</em> Includes detailed feedback metrics for Grammar, Vocab, Coherence.</td></tr><tr><td>relevance_context</td><td>String</td><td><em><strong>Optional</strong>: Question Prompt text provided to the user. When this parameter is passed, the relevance of the user audio transcript is evaluated given the relevance_context and a resulting relevance class is returned in .speech_score.relevance.class</em></td></tr><tr><td>pronunciation_score_mode</td><td>String</td><td><p><em><strong>Optional field:</strong> Possible values -</em></p><ul><li><em><code>default</code>: will only penalize pronunciation errors which affect word intelligibility.</em></li><li><em><code>strict</code>: will penalize prononciation errors even those which do not affect intelligibility resulting in generally lower pronunciation scores.</em></li></ul></td></tr><tr><td>detect_dialect</td><td>String</td><td><p><em><strong>Optional field</strong>: Possible values - 0 | 1</em></p><p><em>1: will apply language detection and warn if the majority of the response language is different from the intended scoring dialect.</em></p></td></tr><tr><td>enforce_dialect</td><td>String</td><td><p><em><strong>Optional field</strong>: Possible values - 0 | 1</em></p><p><em>1: will apply language detection and error if the majority of the response language is different from the intended scoring dialect.</em></p><p><em>Setting enforce_dialect=1 automatically sets detect_dialect=1.</em></p></td></tr></tbody></table>

### Response Example

{% tabs %}
{% tab title="200: OK Language Scores" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "Yes, I do. Travel today is vastly different than what it used to be and the pasta traveler had little idea about what to expect when they arrived at their destination. These days the Internet connects our world in many ways previous generation could only dream about. We can instantly review destination information and make travel arrangements. Also, in the past people could only travel by land or sea and traveling was often long and unsafe.",
    "word_score_list": [...<metrics for all words in the utterance>],
    "ielts_score": {
      "pronunciation": 8.5,
      "fluency": 8.5,
      "grammar": 7,
      "coherence": 7,
      "vocab": 7,
      "overall": 7.5
    },
    "pte_score": {
      "pronunciation": 90,
      "fluency": 90,
      "grammar": 69,
      "coherence": 64,
      "vocab": 69,
      "overall": 78
    },
    "speechace_score": {
      "pronunciation": 94,
      "fluency": 96,
      "grammar": 79,
      "coherence": 76,
      "vocab": 79,
      "overall": 84
    },
    "toeic_score": {
      "pronunciation": 190,
      "fluency": 190,
      "grammar": 160,
      "coherence": 160,
      "vocab": 160,
      "overall": 170
    },
    "cefr_score": {
      "pronunciation": "C2",
      "fluency": "C2",
      "grammar": "B2",
      "coherence": "B2",
      "vocab": "B2",
      "overall": "C1"
    },
    "grammar": {
      "overall_metrics": {
        "length": {
          "score": 4,
          "level": "mid"
        },
        "lexical_diversity": {
          "score": 8,
          "level": "high"
        },
        "grammatical_accuracy": {
          "score": 8,
          "level": "high"
        },
        "grammatical_range": {
          "score": 5,
          "level": "mid",
          "message": "Your response has less grammatical range than most advanced speakers. To improve, you should use a wider range of phrasal and clausal structures and verb-argument constructions.",
          "noun_phrase_complexity": {
            "score": 1,
            "level": "low",
            "message": "Your response lacks noun phrase complexity. You should use richer modifiers in noun phrases by using adjectives, relative clauses, prepositional phrases, non-finite elements, determiners, and demonstratives."
          },
          "noun_phrase_variation": {
            "score": 5,
            "level": "mid"
          },
          "verb_construction_variation": {
            "score": 6,
            "level": "mid"
          },
          "adverb_modifier_variation": {
            "score": 9,
            "level": "high"
          }
        }
      },
      "errors": [
        {
          "category": "STYLE",
          "message": "Did you mean 'different from'? 'Different than' is often considered colloquial style.",
          "span": [
            44,
            48
          ],
          "matched_text": "than",
          "replacements": [
            "from"
          ]
        }
      ]
    },
    "vocab": {
      "overall_metrics": {
        "lexical_diversity": {
          "score": 10,
          "level": "high"
        },
        "word_sophistication": {
          "score": 9,
          "level": "high"
        },
        "word_specificity": {
          "score": 6,
          "level": "mid",
          "message": "Your response uses more general words than most advanced speakers. To improve, you should aim to learn and use verbs, nouns, and adjectives more specific to the meaning you wish to convey."
        },
        "academic_language_use": {
          "score": 1,
          "level": "low",
          "message": "Your response is low on use of academic words. You should learn and use some academic language in your responses to improve."
        },
        "collocation_commonality": {
          "score": 9,
          "level": "high"
        },
        "idiomaticity": {
          "score": 4,
          "level": "mid"
        }
      }
    },
    "coherence": {
      "overall_metrics": {
        "lexical_density": {
          "score": 10,
          "level": "high"
        },
        "basic_connectives": {
          "score": 5,
          "level": "mid",
          "examples": [
            "and",
            "or"
          ]
        },
        "causal_connectives": {
          "score": 10,
          "level": "high"
        },
        "negative_connectives": {
          "score": 5,
          "level": "mid",
          "message": "Great Job! Your response used negative connectives which added contrast to your argument. See the list of some of the negative connectives used.",
          "examples": [
            "or"
          ]
        },
        "pronoun_density": {
          "score": 4,
          "level": "mid"
        },
        "adverb_diversity": {
          "score": 10,
          "level": "high"
        },
        "verb_diversity": {
          "score": 10,
          "level": "high"
        }
      }
    },
    "fluency": {...<fluency metrices>},
    "asr_version": "0.4"
  },
  "version": "9.5"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended/handling-language-scores.md -->
# Speechace API Reference / Handling language scores

# Handling language scores

Language scores are derived from transcribing free speaking audio and scoring the response in key aspects such as:

* [Fluency](../score-text-fluency.md)
* [Pronunciation](../score-text-pronunciation.md)
* [Grammar](per-metric-feedback/grammar-metrics.md)
* [Vocabulary](per-metric-feedback/vocabulary-metrics.md)
* [Coherence](per-metric-feedback/coherence-metrics.md)

These overall scores help the test creator assess the overall quality of the response in terms of five different [rubrics](../../guides-on-common-topics/scoring-rubrics.md):

* A `speechace_score` on a scale of 0 to 100
* An `ielts_score` on a standard IELTS scale of 0 to 9.0
* A `pte_score` on a standard PTE scale or 10 to 90
* A `cefr_score` on a standard scale of A0 to C2
* A `toeic_score` on a standard scale of 0 to 200

The following example snippet from the API results demonstrates the overall scores:

{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "speech_score":
  {
    "transcript": "But the residents have felt the strain, they to launched a healthy streets program, opening up, select streets to just walking and cycling. Now, this action proved valuable in helping residents life and broaden the benefit of their tax dollars. That typically pay to serve cars. New designs were implemented on South Congress. The iconic Main Street of Texas, Inn, Downtown Austin, the stretch of road has changed character overtime evolve e with advances in technology Civic priorities or public preferences with City council's Direction. This stretch of road now has just two fewer Lanes of car traffic. A third of the street space was given over to people bicycling and rolling on scooters. Taking them off the busy sidewalks better suited for dining under the oak trees and give them increased comfort and safety.",
    "relevance": { "class": "TRUE" },
    "ielts_score":
    {
        "pronunciation": 8.5,
        "fluency": 9,
        "grammar": 8.5,
        "coherence": 9,
        "vocab": 9,
        "overall": 9
    },
    "pte_score":
    {
        "pronunciation": 86,
        "fluency": 87,
        "grammar": 86,
        "coherence": 90,
        "vocab": 89,
        "overall": 87
    },
    "speechace_score": 
    {
        "pronunciation": 97,
        "fluency": 98,
        "grammar": 97,
        "coherence": 100,
        "vocab": 99,
        "overall": 98
    },
    "toeic_score":
    {
        "pronunciation": 190,
        "fluency": 200,
        "grammar": 190,
        "coherence": 200,
        "vocab": 200,
        "overall": 200
    },
    "cefr_score": 
    {
        "pronunciation": "C2",
        "fluency": "C2",
        "grammar": "C2",
        "coherence": "C2",
        "vocab": "C2",
        "overall": "C2"
    }
     ...
  }
}
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended/per-metric-feedback.md -->
# Speechace API Reference / Per metric feedback

# Per metric feedback

The API also offers detailed feedback on each of the Pronunciation, Fluency, Vocabulary, Grammar and Cohesion metrics. Such feedback can be used to explain why a user got a low score on a specific metric. Such feedback is available in the following nodes:

<table><thead><tr><th width="188">Node</th><th>Description</th></tr></thead><tbody><tr><td>fluency</td><td>This node contains <a href="/pages/Qd2DoQIAUsxraBWYNEOC"><strong>fluency</strong></a> metrics and sub-scores for the overall utterance and for each segment (sentence) within the utterance.</td></tr><tr><td>word_score_list[]</td><td>This node contains <a href="/pages/H0hoZyGd3ZOIVlASwcEy"><strong>pronunciation</strong></a> scores and metrics for each word, syllable, and phoneme within the utterance.</td></tr><tr><td>grammar</td><td>This node contains <strong>grammar</strong> metrics, errors and feedback for the overall utterance.</td></tr><tr><td>vocabulary</td><td>This node contains <strong>vocabulary</strong> metrics, errors and feedback for the overall utterance.</td></tr><tr><td>coherence</td><td>This node contains <strong>coherence</strong> metrics, errors and feedback for the overall utterance.</td></tr></tbody></table>

Details regarding [fluency](../../features/scripted-activities/fluency-scoring.md) and [pronunciation](../../features/scripted-activities/pronunciation-scoring.md) scoring were explained in detail in earlier parts of this documentation. Each of the Grammar, Vocabulary, and Coherence feedback metrics has 3 sub-elements:

1. **score:** on a scale of 1 to 10
2. **level:** an interpretation of the score as low/mid/high
3. **message:** a feedback message if the score is low.

In the next few sections, we will review the grammar, vocabulary and coherence scores in detail:


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended/per-metric-feedback/grammar-metrics.md -->
# Speechace API Reference / Grammar metrics

# Grammar metrics

Below are the detailed feedback metrics appearing in the response for `grammar` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="411">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>grammar.overall_metrics.length</td><td>The sufficiency of the response length in words to demonstrate the necessary grammatical range.</td></tr><tr><td>grammar.overall_metrics.lexical_diversity</td><td>The degree of variation in syntactic structures such as diversity in verbs, adjectives and adverbial modifiers.</td></tr><tr><td>grammar.overall_metrics.grammatical_accuracy</td><td>The degree of grammatical inaccuracies in the response. A list of grammatical errors with suggested replacements is returned in the <code>grammar.errors</code> node when this index is low.</td></tr><tr><td>grammar.overall_metrics.grammatical_range</td><td><p></p><p>The degree of grammatical range demonstrated in the response. This score is further broken down into 4 additional sub-indices:</p><ul><li>noun_phrase_variation</li><li>noun_phrase_complexity</li><li>verb_construction_variation</li><li>adverb_modifier_variation</li></ul></td></tr><tr><td>grammar.overall_metrics.grammatical_range.noun_phrase_variation</td><td>The degree of variation in structure of noun phrases such as the number and types of modifiers used in the response.</td></tr><tr><td>grammar.overall_metrics.grammatical_range.noun_phrase_complexity</td><td>The degree of complexity of noun phrases such as the richness of adjectives, relative clauses, prepositional phrases, nonfinite elements, determiners, and demonstratives used in the response.</td></tr><tr><td>grammar.overall.metrics.grammatical_range.verb_construction_variation</td><td>The degree of variation in verbal structures such as the number and types of verb structural elements used in the response.</td></tr><tr><td>grammar.overall.metrics.grammatical_range.adverb_modifier_variation</td><td>The degree of variation in types of adverbs or adverb phrases to modify clauses, verbs, and adjectives used in the response.</td></tr></tbody></table>

An example of `grammar` feedback metrics looks like below:

{% code overflow="wrap" lineNumbers="true" fullWidth="false" %}

```json
{   
"grammar": 
  {
    "overall_metrics": {
      "length": {
         "score": 4,
         "level": "mid"
       },
       "lexical_diversity": {
          "score": 8,
          "level": "high"
       },
       "grammatical_accuracy": {
           "score": 8,
           "level": "high"
        },
        "grammatical_range": {
            "score": 5,
            "level": "mid",
            "message": "Your response has less grammatical range than most advanced speakers. To improve, you should use a wider range of phrasal and clausal structures and verb-argument constructions.",
            "noun_phrase_complexity": {
               "score": 1,
               "level": "low",
               "message": "Your response lacks noun phrase complexity. You should use richer modifiers in noun phrases by using adjectives, relative clauses, prepositional phrases, non-finite elements, determiners, and demonstratives."
            },
            "noun_phrase_variation": {
                "score": 5,
                "level": "mid"
            },
            "verb_construction_variation": {
                "score": 6,
                "level": "mid"
             },
             "adverb_modifier_variation": {
                 "score": 9,
                 "level": "high"
              }
         }
     },
     "errors": [
      {
         "category": "STYLE",
         "message": "Did you mean 'different from'? 'Different than' is often considered colloquial style.",
         "span": [44,48],
         "matched_text": "than",
         "replacements": ["from"]
      }
     ]
 }
}
```

{% endcode %}

#### **Grammatical Error**

The `grammar.errors` node contains a JSON array of grammatical errors found in the response. Each array element is an object with the following properties:

<table data-full-width="false"><thead><tr><th width="157">key</th><th>Description</th></tr></thead><tbody><tr><td>category</td><td>The type of error such as; STYLE, GRAMMAR, COLLOCATION, CONFUSED_WORDS</td></tr><tr><td>message</td><td>A descriptive message of the error. The message may refer to words within the evaluated text and include suggested replacements within the ... markup tags.</td></tr><tr><td>span</td><td>The [begin, end] indices of the matched text in characters.</td></tr><tr><td>matched_text</td><td>The matched text where the error was found.</td></tr><tr><td>replacements</td><td>An array of zero or more suggested replacements where applicable.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended/per-metric-feedback/vocabulary-metrics.md -->
# Speechace API Reference / Vocabulary metrics

# Vocabulary metrics

Below are the detailed feedback metrics appearing in the response for `vocabulary` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="398">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>vocab.overall_metrics.lexical_diversity</td><td>The degree of word diversity in the response.</td></tr><tr><td>vocab.overall_metrics.word_sophistication</td><td>The degree of use of advanced, less common vocabulary in the response.</td></tr><tr><td>vocab.overall_metrics.word_specificity</td><td>The degree of use of specific (less general) verbs, nouns, and adjectives which are specific to the meaning being conveyed.</td></tr><tr><td>vocab.overall_metrics.academic_language_use</td><td>The degree of use of academic language in the response.</td></tr><tr><td>vocab.overall_metrics.collocation_commonality</td><td>The degree of use of advanced word combinations.</td></tr><tr><td>vocab.overall_metrics.idiomaticity</td><td>The degree of use of idiomatic language.</td></tr></tbody></table>

Below is an example showing the snippet of `vocabulary` metrics:

{% code overflow="wrap" lineNumbers="true" %}

```json
"vocab": {
      "overall_metrics": {
        "lexical_diversity": {
          "score": 10,
          "level": "high"
        },
        "word_sophistication": {
          "score": 9,
          "level": "high"
        },
        "word_specificity": {
          "score": 6,
          "level": "mid",
          "message": "Your response uses more general words than most advanced speakers. To improve, you should aim to learn and use verbs, nouns, and adjectives more specific to the meaning you wish to convey."
        },
        "academic_language_use": {
          "score": 1,
          "level": "low",
          "message": "Your response is low on use of academic words. You should learn and use some academic language in your responses to improve."
        },
        "collocation_commonality": {
          "score": 9,
          "level": "high"
        },
        "idiomaticity": {
          "score": 4,
          "level": "mid"
        }
      }
    }
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-open-ended/per-metric-feedback/coherence-metrics.md -->
# Speechace API Reference / Coherence metrics

# Coherence metrics

Below are the detailed feedback metrics appearing in the response for `coherence` along with their descriptions, are as follows.&#x20;

These metrics are returned by including `include_ielts_feedback = 1` parameter in the [Spontaneous Speech API](../../score-speech-open-ended.md).

<table data-full-width="false"><thead><tr><th width="414">Parameter</th><th>Description</th></tr></thead><tbody><tr><td>coherence.overall_metrics.lexical_density</td><td>The degree of use of content words within the response.</td></tr><tr><td>coherence.overall_metrics.basic_connectives</td><td>The degree and variety of basic connectives within the response. A list of most overused basic connectives is included.</td></tr><tr><td>coherence.overall_metrics.causal_connectives</td><td>The degree and variety of causal connectives within the response. A list of most overused causal connectives is included.</td></tr><tr><td>coherence.overall_metrics.negative_connectives</td><td>The degree and variety of negative connectives within the response. A list of most used negative connectives is included.</td></tr><tr><td>coherence.overall_metrics.pronoun_density</td><td>The degree of use of pronouns within the response.</td></tr><tr><td>coherence.overall_metrics.adverb_diversity</td><td>The degree and variety of adverbs within the response. A list of most overused adverbs is included.</td></tr><tr><td>coherence.overall_metrics.verb_diversity</td><td>The degree and variety of verbs within the response. A list of most overused verbs is included.</td></tr></tbody></table>

Below is an example showing the snippet of `coherence` metrics:

{% code overflow="wrap" lineNumbers="true" %}

```json5
"coherence": {
      "overall_metrics": {
        "lexical_density": {
          "score": 10,
          "level": "high"
        },
        "basic_connectives": {
          "score": 5,
          "level": "mid",
          "examples": [
            "and",
            "or"
          ]
        },
        "causal_connectives": {
          "score": 10,
          "level": "high"
        },
        "negative_connectives": {
          "score": 5,
          "level": "mid",
          "message": "Great Job! Your response used negative connectives which added contrast to your argument. See the list of some of the negative connectives used.",
          "examples": [
            "or"
          ]
        },
        "pronoun_density": {
          "score": 4,
          "level": "mid"
        },
        "adverb_diversity": {
          "score": 10,
          "level": "high"
        },
        "verb_diversity": {
          "score": 10,
          "level": "high"
        }
      }
    },
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-relevance.md -->
# Speechace API Reference / Score Speech/Relevance

# Score Speech/Relevance

{% hint style="info" %}
**Run in Postman:** [Score Revelance of a response](https://docs.speechace.com/#2bb020ad-bf76-4d48-9460-d1884910ba04)
{% endhint %}

### Request Format

The request parameters in the cURL code below are the same as in the [Score Text/Open-ended](score-speech-open-ended.md) scoring function. The only difference is that to get relevance scores we have to pass in a `relevance_context` field which either has the question that was asked or a topic on which the user is speaking.

{% tabs %}
{% tab title="cURL" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'relevance_context="Describe the healthy streets program and its impact on the residents of Austin Texas."'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Response Example

The tabs below show a relevant response, an irrelevant response and a response which is too similar to the question prompt or the text provided in relevance\_context field:

{% tabs %}
{% tab title="200: OK Response" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "But the residents have felt the strain, they to launched a healthy streets program, opening up, select streets to just walking and cycling. Now, this action proved valuable in helping residents life and broaden the benefit of their tax dollars. That typically pay to serve cars. New designs were implemented on South Congress. The iconic Main Street of Texas, Inn, Downtown Austin, the stretch of road has changed character overtime evolve e with advances in technology Civic priorities or public preferences with City council's Direction. This stretch of road now has just two fewer Lanes of car traffic. A third of the street space was given over to people bicycling and rolling on scooters. Taking them off the busy sidewalks better suited for dining under the oak trees and give them increased comfort and safety.",
    "word_score_list": [....<pronunciation metrics for all words in utterance>],
    "relevance": {
      "class": "TRUE"
    },
    "ielts_score": {
      "pronunciation": 8.5,
      "fluency": 9,
      "grammar": 8.5,
      "coherence": 9,
      "vocab": 9,
      "overall": 9
    },
    "pte_score": {
      "pronunciation": 86,
      "fluency": 87,
      "grammar": 86,
      "coherence": 90,
      "vocab": 89,
      "overall": 87
    },
    "speechace_score": {
      "pronunciation": 97,
      "fluency": 98,
      "grammar": 97,
      "coherence": 100,
      "vocab": 99,
      "overall": 98
    },
    "toeic_score": {
      "pronunciation": 190,
      "fluency": 200,
      "grammar": 190,
      "coherence": 200,
      "vocab": 200,
      "overall": 200
    },
    "cefr_score": {
      "pronunciation": "C2",
      "fluency": "C2",
      "grammar": "C2",
      "coherence": "C2",
      "vocab": "C2",
      "overall": "C2"
    },
    "fluency": {....<fluency metrics for all words in utterance>}
    },
  "version": "9.2"
}
```

{% endcode %}
{% endtab %}

{% tab title="Error: response\_relevance\_false" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "Yes, I do travel today is vastly different and it used to be in the past the traveler had little idea about what to expect when they arrived at their destination. These days, the internet sucks whole world in many ways previous generation could only dream about we can instantly review destination information and make travel Arrangements. Also, in the past, people could only travel by land or Sea Foam traveling was often long and unsafe.",
    "word_score_list": [....<list of word scores>],
    "relevance": {
      "class": "FALSE"
    },
    "ielts_score": [<list of scores>],
    "pte_score": [<list of scores>],
    "speechace_score": [<list of scores>],
    "toeic_score": [<list of scores>],
    "cefr_score": [<list of scores>],
    "score_issue_list": [
      {
        "source": "overall",
        "status": "warning",
        "short_message": "response_relevance_false",
        "detail_message": "The response is not relevant to the context."
      }
    ],
    "fluency": {<list of fluency scores>},
     },
  "version": "9.0"
}js
```

{% endcode %}
{% endtab %}

{% tab title="Error: response\_too\_similar" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "In my opinion, is job satisfaction more important than salary when considering a job. Why or why not job satisfaction is more important than salary when considering a job.",
    "word_score_list": [<.....list of word scores>],
    "relevance": {
      "class": "FALSE"
    },
    "ielts_score": [<....list of scores>],
    "pte_score": [<.....list of scores>],
    "speechace_score": [<.....list of scores>],
    "score_issue_list": [
      {
        "source": "overall",
        "status": "warning",
        "short_message": "response_too_similar",
        "detail_message": "The response is too similar to the context."
      }
    ],
    "fluency": {<....list of fluency scores>},
     },
  "version": "9.0"
}
```

{% endcode %}
{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-relevance/handling-relevance-response.md -->
# Speechace API Reference / Handling relevance response

# Handling relevance response

The [pronunciation](../../features/scripted-activities/pronunciation-scoring.md) and [fluency](../../features/scripted-activities/fluency-scoring.md) interpretation of the key elements in the response of the spoken word or sentence remains the same as previously described. The parameter which we need to notice is the relevance check.&#x20;

**Relevance Check**

If the response is relevant, then `relevance.class` will be `TRUE`:

<div align="left"></div>

If the response is not relevant to the `context`, then `relevance.class` will be `FALSE` and the error will be shown:

<div align="left"></div>

If the response is too similar to the question prompt, then the error will be as follows:

<div align="left"></div>

**Other fields**

<table><thead><tr><th width="187">Field</th><th>Description</th></tr></thead><tbody><tr><td>transcript</td><td>The speech-to-text transcript of what the user has said.</td></tr><tr><td>speechace_score</td><td>An overall score on a scale of 0 to 100, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>ielts_score</td><td>An overall score on an IELTS scale of 0 to 9.0, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>pte_score</td><td>An overall score on a PTE scale of 10 to 90, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>cefr_score</td><td>An overall score on CEFR scale of A0 to C2, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>toeic_score</td><td>An overall score on an TOEIC scale of 0 to 200, in addition to subscores for: Fluency, Pronunciation, Grammar, Vocabulary, Coherence.</td></tr><tr><td>relevance.class</td><td>TRUE or FALSE indicating whether the response was relevant given the <code>relevance_context</code> passed as input to the API.</td></tr></tbody></table>

{% hint style="info" %}
**Protip:** The `relevance_context` is an optional field and can be included only if the relevance of an audio response needs to be assessed by transcribing the response first and then comparing it to the provided question prompt.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-speech-language-detection.md -->
# Speechace API Reference / Score Speech/Language Detection

# Score Speech/Language Detection

{% hint style="info" %}
**Run in Postman:** [Language Scores](https://docs.speechace.com/#2f536692-1645-4a14-b729-ef781e1390a1)
{% endhint %}

### Request Format

The request parameters in the cURL code below are the same as in the [Score Speech/Open-ended](score-speech-open-ended.md) or [Score Text/Pronunciation](score-text-pronunciation.md) scoring function. To detect the language following parameters can be used:

1. `detect_dialect = 1`: This parameter is used to detect the dialect of the spoken language without impacting the user's test score. This approach is more lenient, allowing the user to proceed with their score intact even if the detected dialect doesn’t match the expected one. In this case, the API output will return the potential language in which the user provided a response.
2. `enforce_dialect = 1`: This parameter is used to enforce the correct dialect, resulting in a hard error if the response is in an unexpected language. If the detected dialect is incorrect, the API will generate an error, and the developer can set the user's score to zero based on this error response.

{% tabs %}
{% tab title="Detect the dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'detect_dialect = 1'
```

{% endcode %}
{% endtab %}

{% tab title="Enforce detected dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/speech/v9/json?key={{speechace_premiumkey}}&dialect=en-us' \
--form 'user_audio_file=@"kevin.m4a"' \
--form 'enforce_dialect = 1'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Response Example

The following responses illustrate two scenarios: in the first case, the language code for the detected language/dialect is reported under the key `detected_dialect`, resulting in a warning for the user. In the second case, an incorrect dialect triggers an enforced error under the key `detail_message`, preventing further action.

{% tabs %}
{% tab title="Detect the dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
    "status": "success",
    "quota_remaining": -1,
    "speech_score": {
        "transcript": "Ich liebe den Winterurlaub, weil ich die kalte Luft und den Schnee genieße.",
        "word_score_list": [....],
        "ielts_score": {...},
        "pte_score": {...},
        "speechace_score": {...},
        "toeic_score": {...},
        "cefr_score": {...},
        "fluency": {...},
        "detected_dialect": {
            "lang_id": "ge"
        },
        "asr_version": "0.4"
    },
    "version": "9.9"
}
```

{% endcode %}
{% endtab %}

{% tab title="Enforce the detected dialect" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
    "status": "error",
    "short_message": "non_dialect_language_detected",
    "detail_message": "The audio file contains speech in 'ge' language different from dialect 'en-us'.",
    "version": "9.9"
}
```

{% endcode %}

{% endtab %}
{% endtabs %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-task-task-achievement.md -->
# Speechace API Reference / Score Task/Task Achievement

# Score Task/Task Achievement

{% hint style="info" %}
**Run in postman:** [Score Task Achievement](https://docs.speechace.com/#07bbedef-8c33-48dc-b84f-6e94ebb3fc80)
{% endhint %}

The Speechace Task Achievement API supports following task types:

* **Describe-Image:** The speaker is presented with an image and asked to describe the details, relationships, and conclusion to be drawn from elements of the image.
* **Retell-Lecture:** The speaker listens to a 1-2 minute lecture and is asked to summarize the lecture focusing on key elements, concepts and conclusions from the lecture.
* **Answer-Question:** The speaker is presented with a short question which typically requires a one or two word answer.

Each task type has particular input and outputs:

<table><thead><tr><th width="170">Task Type</th><th width="332">Inputs</th><th>Outputs</th></tr></thead><tbody><tr><td>describe-image</td><td><code>task_context</code>: A model description of the image which is presented to the speaker.<br>Max length: 1024 chars.</td><td>Task score on scale of 0-5.</td></tr><tr><td>retell-lecture</td><td><code>task_context</code>: A model summary of the lecture which is presented to the speaker.<br>Max length: 1024 chars.</td><td>Task score on scale of 0-5.</td></tr><tr><td>answer-question</td><td><code>task-question</code>: The question presented to the user.</td><td>Task score on scale of 0-1 where 0 is incorrect and 1 is correct.</td></tr></tbody></table>

The API supports different modes in combining task scores and language scores in assessment:

1. `user_audio_file` or `user_audio_text`:  The speaker's response can be submitted as either audio or text, allowing task scoring to be used with written responses as well.
2. &#x20;`include_speech_score`: Speech scoring can be included or excluded along with the task score. Note that if `user_audio_text` is used, the `include_speech_score` will always be zero. Therefore, in written responses, only task scores are provided.

All tasks are available in the following languages:

* English (en-us, en-gb)
* Spanish (es-es, es-mx)
* French (fr-fr, fr-ca)

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` [`https://api.speechace.co/api/scoring/task/v9/json`<br>](<https://api.speechace.co/api/scoring/task/v9/json&#xA;>)

{% tabs %}
{% tab title="Example with audio" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/task/v9/json?key={{speechace_premiumkey}}&task_type=describe-image&dialect=en-us' \
--form 'task_context="This bar chart illustrates the declining trend related to the percent of U.S. workforce engaged in farm labor in the 19th century. In 1840, for example, around 69% of the U.S. workforce was engaged in farm labor; in 1860, almost 60% of the U.S. workforce was engaged in farm labor; in 1880, only 50% of the U.S. workforce was engaged in farm labor; and in 1900, less than 40% of the U.S. workforce was engaged in farm labor."' \
--form 'user_audio_file=@"barchataudiofile.mp3"' \
--form 'include_speech_score="1"'
```

{% endcode %}
{% endtab %}

{% tab title="Example with text" %}
{% code overflow="wrap" lineNumbers="true" %}

```
curl --location -g 'https://api.speechace.co/api/scoring/task/v9/json?key={{speechace_premiumkey}}&task_type=describe-image&dialect=en-us' \
--form 'task_context="This line graph illustrates the overall behaviour of France’s national debt in the period 1995-2011, calculated in comparison to the country’s GDP (Gross Domestic Product). Thus, in 1995, France’s national debt was equivalent to about 56% of its GDP; between 1996 and 1997, the debt rose to a little more than 60% of the country’s GDP, dropping to a little less than 60% between the years 2000 and 2001; however, it started to rise again in 2002, reaching almost 70% of the country’s GDP in 2005; unfortunately, between 2009 and 2010, the debt had reached around 85% of France’s GDP, reaching roughly 88% by 2011."' \
--form 'user_audio_text="This is a beautiful image infront of me with a chart depicting many colors and numbers. I can see 1995, 1996, 1997, 1998, 1999 and France'\''s national debt."' \
--form 'include_speech_score="0"'
```

{% endcode %}
{% endtab %}
{% endtabs %}

### Query Parameters

<table><thead><tr><th width="136">Parameter</th><th width="117">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>in which the speaker will be assessed. Supported values are: en-us, en-gb, fr-fr, fr-ca, es-es, es-mx.</em></td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier (generated by your applications) for the end-user who spoke the audio.</em></td></tr><tr><td>task_type</td><td>String</td><td><p><em>The task_type to score. Supported types are:</em> </p><ul><li><em>describe-image</em></li><li><em>retell-lecture</em></li><li><em>answer-question.</em></li></ul></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="169">Parameter</th><th width="100">Type</th><th>Description</th></tr></thead><tbody><tr><td>task_context</td><td>String</td><td><p><em>The context or model or model answer for the task presented to the speaker.</em></p><p><em><strong>Used in the following task-types:</strong></em> </p><ul><li><em><strong>describe-image: a model description of the image</strong></em> </li><li><em><strong>retell-lecture: a model description of the lecture</strong></em></li></ul><p><em>This must be provided in the same language as the one being assessed.</em></p></td></tr><tr><td>task_question</td><td>String</td><td><p><em><strong>The task question presented to the speaker, used in task-type = answer-question.</strong></em></p><p><em>This must be provided in the same language as the one being assessed.</em></p></td></tr><tr><td>user_audio_file</td><td>File</td><td><em>file with user audio (wav, mp3, m4a, webm, ogg, aiff)</em></td></tr><tr><td>include_speech_score</td><td>String</td><td><ul><li><em>Set to</em> <code>1</code><em>, to include scoring other aspects of the speech: Pronunciation, Fluency, Grammar, Vocab, Coherence.</em> </li><li><em>Set to</em> <code>0</code> <em>if you only want to receive the task score only.</em></li></ul></td></tr><tr><td>user_audio_text</td><td>String</td><td><p><em>A text transcript of the speaker's response.</em> </p><ul><li><em>Use this field instead of <code>user_audio_file</code> if you already have a transcript of the user's response and do not wish to re-transcribe an audio.</em></li><li><em>Note: In this case, you will only be able to receive an overall <code>task_score.</code></em></li></ul></td></tr></tbody></table>

### Response Example

Notice the `task_score.score` key for the overall task achievement score in the response below:

{% tabs %}
{% tab title="Task = describe-image" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "describe-image",
    "version": "0.1",
    "score": 4,
    "transcript": "This bar graph shows the percent of US workforce engaged in farm labor, and that's data from 1840 to 1900. Ear now starting with 1840, the percentage was 70 percentage. After that there is a gradual decrease in the number of workforce engaged in farm labor to 60 percentage in 1860 and further down to 18 around 50% in 1880. And then in 1900 it decreased to 40 percentage. Overall, there is a continuous decrease in the engagement in the farm sector."
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "This bar graph shows the percent of US workforce engaged in farm labor, and that's data from 1840 to 1900. Ear now starting with 1840, the percentage was 70 percentage. After that there is a gradual decrease in the number of workforce engaged in farm labor to 60 percentage in 1860 and further down to 18 around 50% in 1880. And then in 1900 it decreased to 40 percentage. Overall, there is a continuous decrease in the engagement in the farm sector.",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {
      "pronunciation": "B2",
      "fluency": "B2",
      "grammar": "B1+",
      "coherence": "B1",
      "vocab": "B1+",
      "overall": "B1+"
    },
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
}
```

{% endcode %}
{% endtab %}

{% tab title="Task = retell-lecture" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "retell-lecture",
    "version": "0.1",
    "score": 2,
    "transcript": "The lecture was about the ecosystem. The lecture said that ecology is the study of living organisms in an environment. The lecturer also said that there are two factors in an ecosystem. The first one is biotic which is considered as the living things. The second one is abiotic which is considered as the non living things in the environment. The biotic factors is considered as the primary producers. Herbivores, carnivores, omnivores and detritivores. However, the abiotic factors."
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "The lecture was about the ecosystem. The lecture said that ecology is the study of living organisms in an environment. The lecturer also said that there are two factors in an ecosystem. The first one is biotic which is considered as the living things. The second one is abiotic which is considered as the non living things in the environment. The biotic factors is considered as the primary producers. Herbivores, carnivores, omnivores and detritivores. However, the abiotic factors.",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr score>},
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
}
```

{% endcode %}
{% endtab %}

{% tab title="Task = answer-question" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "status": "success",
  "task_score": {
    "type": "answer-question",
    "version": "0.1",
    "score": 1,
    "transcript": "A democracy?"
  },
  "quota_remaining": -1,
  "speech_score": {
    "transcript": "A democracy?",
    "word_score_list": [<.....pronunciation metrics>],
    "ielts_score": {<....ielts scores>},
    "pte_score": {<...pte scores>},
    "speechace_score": {<...speechace scores>},
    "toeic_score": {<...toeic scores>},
    "cefr_score": {<...cefr score>}
    ],
    "fluency": {<...fluency metrics>},
    "asr_version": "0.4"
  },
  "version": "9.7"
} 
```

{% endcode %}
{% endtab %}
{% endtabs %}

The [pronunciation](score-text-pronunciation.md) and [fluency](score-text-fluency.md) interpretation of the key elements in the response of the spoken word or sentence remains the same.

The new addition is the task score parameters, which indicate the extent to which the task has been achieved.&#x20;

<div align="left"></div>

{% hint style="info" %}
**Difference between `task_context` and `relevance_context`**

* [**Relevance**](score-speech-relevance.md) is binary and is higher level. It evaluates whether the response is on-topic or not (True or False)
* [**Task Achievement**](score-task-task-achievement.md) is more nuanced and scores how well the response addresses the task

For a general question such as "Do you think the government should subsidize healthcare?" relevance is primarily assessed, as there is no definitive right or wrong answer; the focus is on whether the response is on topic.

In contrast, for a specific question like "What does the following business chart tell us?" a specific answer is expected. Therefore, a nuanced task context and detailed task score are required to evaluate how well the response addresses the specific elements of the task.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/api-reference/score-writing.md -->
# Speechace API Reference / Score Writing

# Score Writing

{% hint style="info" %}
This features requires a Premium Subscription

**Run in postman:** [Score Writing](https://docs.speechace.com/#b21cde34-7649-450a-ae0e-dabee579d1f3)
{% endhint %}

The Score Writing API evaluates 3 different writing tasks and returns scores aligned with CEFR, IELTS, PTE, and the Speechace rubrics.

<table><thead><tr><th width="142.4140625">Task</th><th width="100">Length</th><th>Description</th></tr></thead><tbody><tr><td>essay-writing</td><td>150 - 300 words</td><td>Long form opinion-based writing such as IELTS Task 2 writing prompts.</td></tr><tr><td>short-writing</td><td>50 - 100 words</td><td>Short form school or business writing such as describing an image, or writing a short letter.</td></tr><tr><td>chat-writing</td><td>50 - 100 words</td><td>Brief responses to a query or situation expressed in a chat context. Suitable for business contexts such as customer support simulation.</td></tr></tbody></table>

The Score Writing API is available in the following languages:

* English (en-us, en-gb)
* Spanish (es-es)

The Score Writing API returns scores in the following formats:

* Speechace scale (0..100)
* CEFR scale (A0..C2)
* IELTS scale (0..9)
* PTE scale (10..90)
* TOEIC scale (0..200)

### Request Format&#x20;

The endpoint that is to be used will depend on the [region](../getting-started/pre-requisites/api-regions-and-endpoints.md) of your subscription. For example, for US West, the endpoint is <https://api.speechace.co>.

`POST` [`https://api.speechace.co/api/scoring/writing/v9/json`](https://api.speechace.co/api/scoring/writing/v9/json)

### Query Parameters

<table><thead><tr><th width="136">Parameter</th><th width="117">Type</th><th>Description</th></tr></thead><tbody><tr><td>key</td><td>String</td><td><strong>Required:</strong> <em>API</em> <a href="/pages/FY5QJQ5NVDkUtPtG080s"><em>key</em></a> <em>issued by Speechace.</em></td></tr><tr><td>dialect</td><td>String</td><td><p><strong>Optional:</strong> default = en-us </p><p><em>This is the</em> <a href="/pages/PM2D802SgqeoWM8lrey5"><em>dialect</em></a> <em>in which the writing sample will be assessed.</em></p><p><em>Supported values are: <code>en-us</code>, <code>en-gb</code>, <code>es-es</code>.</em></p></td></tr><tr><td>user_id</td><td>String</td><td><em><strong>Optional</strong>: A unique anonymized identifier (generated by your applications) for the end-user who submitted the response.</em></td></tr><tr><td>task_type</td><td>String</td><td><p><em>The writing task_type to score. Supported types are:</em></p><p></p><ul><li><em>essay-writing</em></li><li><em>short-writing</em></li><li><em>chat-writing</em></li></ul></td></tr></tbody></table>

### Request Body

<table><thead><tr><th width="169">Parameter</th><th width="100">Type</th><th>Description</th></tr></thead><tbody><tr><td>prompt</td><td>String</td><td><p><strong>Required:</strong> Max 2048 chars</p><p>The writing task prompt given to the end-user.</p></td></tr><tr><td>answer</td><td>String</td><td><p><strong>Required:</strong> Max 4096 chars</p><p>The response submitted by the end-user which will be scored in context of the prompt.</p></td></tr><tr><td>include_feedback</td><td>String</td><td><p><strong>Optional:</strong> 0 | 1, default = 0<br>Indicates whether to return additional feedback also as part of the API response.</p><p><br><span data-gb-custom-inline data-tag="emoji" data-code="26a0">⚠️</span><strong>Warning:</strong> This field has a cost and performance impact. Use only when needed and avoid making the default on all requests.</p></td></tr><tr><td>detect_dialect</td><td>String</td><td><p><strong>Optional:</strong> 0 | 1, default = 0</p><p>Indicates whether to return detected dialect of the user response as part of of the API response.</p></td></tr><tr><td>enforce_dialect</td><td>String</td><td><p><strong>Optional:</strong> 0 | 1, default = 0</p><p>Indicates whether to enforce detected dialect of the user response be same as dialect of the calling API i.e. raises error if the user response dialect doesn't match expected dialect.</p></td></tr><tr><td>include_metrics</td><td>String</td><td><p><strong>Optional:</strong> array of string enums, default = []</p><p>Indicates which metrics to use for score. The list of enum values are: <code>vocab</code>, <code>grammar</code>, <code>coherence</code>, <code>task_response</code>. default of [] indicates all metrics to score.</p></td></tr><tr><td>score_mode</td><td>String</td><td><strong>Optional:</strong> standard | enhanced, default = "standard"<br>standard: default scoring mode (balances accuarcy, performance, and cost)<br>enhanced: enhanced scoring mode (higher accuracy model with also higher compute and cost impact)</td></tr></tbody></table>

### Response Examples

The `writing_score.speechace.overall` key contrains the overall writing score in the Speechace numerical scale (0..100). The overall score is the average of 4 sub-scores:

* `writing_score.speechace.task_response`&#x20;
* `writing_score.speechace.grammar`&#x20;
* `writing_score.speechace.vocab`&#x20;
* `writing_score.speechace.coherence`&#x20;

{% hint style="info" %}
Refer to the [Writing Score Rubric guide](../guides-on-common-topics/interpreting-writing-scores/writing-scoring-rubrics.md) for interpreting the scores.
{% endhint %}

{% tabs %}
{% tab title="essay-writing" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "request_id": "8bf1ccf7d5225aaccb35f80acdacd5a6",
  "status": "success",
  "task_type": "essay-writing",
  "task_version": "0.1",
  "answer": "Some people insist that volunteer services like charity activity should be introduced to the high school curriculum, whereas others think it should not be treated as part of mandatory education. Nonetheless, I personally lean more toward the latter view, simply because the level of benefits is heavily affected by the individual point of view or circumstance.\n\nThere are several cases that unpaid service would be beneficial. For instance, they would be able to develop a sense of community or meet new people regardless of their individual skill or aptitude. According to a research paper issued by Cambridge University last year, the majority of successful people in business intentionally participate some volunteer activity at an early age in order to create connections for the future. Considering that childhood is a preparation period for a member of society, it is no doubt providing them such opportunities are more practical and fruitful idea.\n\nTurning to the other side of the argument, it could be acknowledged as a just hindrance for achieving an individual goal. Firstly, for the people who are considering to go to the university as their future plan, their top priority may not make  connections but building up academic knowledge or conquering examination. Secondly, there might be students who dedicate their time for part-time job to set aside money as a tuition fee or provide their family. From their point of view, volunteer work is just wasting their time and not necessarily essential. \n\nIn conclusion, while some people say imposing unpaid service for high school students is significant, we cannot ignore the fact that it has not only a positive aspect but also a negative side. I believe that as long as it has some drawbacks, it should not be a compulsory subject and needlessly deprive student's precious time.",
  "char_count": 1840,
  "writing_score": {
    "speechace": {
      "task_response": 78,
      "vocab": 78,
      "coherence": 78,
      "grammar": 72,
      "overall": 78
    },
    "ielts": {
      "task_response": 7,
      "vocab": 7,
      "coherence": 7,
      "grammar": 6.5,
      "overall": 7
    },
    "cefr": {
      "task_response": "B2+",
      "vocab": "B2+",
      "coherence": "B2+",
      "grammar": "B2",
      "overall": "B2+"
    },
    "pte": {
      "task_response": 66,
      "vocab": 66,
      "coherence": 66,
      "grammar": 56,
      "overall": 66
    },
    "toeic": {
      "task_response": 160,
      "vocab": 160,
      "coherence": 160,
      "grammar": 140,
      "overall": 160
    }
  },
  "version": "9.14"
}
```

{% endcode %}
{% endtab %}

{% tab title="short-writing" %}
{% code overflow="wrap" lineNumbers="true" %}

```json
{
  "request_id": "cc387bb553cbd4444fd0942b84b1a6ea",
  "status": "success",
  "task_type": "short-writing",
  "task_version": "0.1",
  "answer": "Last week I went to Hawaii with my family.It was good,first I go to swim on the ocean it was the best of my vacations ,but then I see lots of fishes on the ocean they were beutiful and they swim so fast.My dad talk me that the fishes were so fast because they has to swim from they born and if they do not swim fast the sharks eats them.It was all beutiful and interesting.That was my best vacations of all.",
  "char_count": 407,
  "writing_score": {
    "speechace": {
      "task_response": 44,
      "vocab": 33,
      "coherence": 28,
      "grammar": 22,
      "overall": 33
    },
    "ielts": {
      "task_response": 4,
      "vocab": 3,
      "coherence": 2.5,
      "grammar": 2,
      "overall": 3
    },
    "cefr": {
      "task_response": "A2+",
      "vocab": "A2+",
      "coherence": "A2",
      "grammar": "A2",
      "overall": "A2+"
    },
    "pte": {
      "task_response": 20,
      "vocab": 15,
      "coherence": 12,
      "grammar": 10,
      "overall": 15
    },
    "toeic": {
      "task_response": 50,
      "vocab": 30,
      "coherence": 30,
      "grammar": 20,
      "overall": 30
    }
  },
  "version": "9.14"
}
```

{% endcode %}
{% endtab %}

{% tab title="chat-writing" %}

```json
{
  "request_id": "2350acdfee234783d2746e297fb18d13",
  "status": "success",
  "task_type": "chat-writing",
  "task_version": "0.1",
  "answer": "I am really sorry to hear about your experience. Let me help you address this right away. I will need your order details and a description of the damage, and then I will immediately send you a replacement item. If you prefer a refund, please let me know. Finally, we will also send you instructions on how to return the damaged item.\nWe apologize for this experience and hope to immediately rectify it.",
  "char_count": 402,
  "writing_score": {
    "speechace": {
      "task_response": 78,
      "vocab": 78,
      "coherence": 83,
      "grammar": 78,
      "overall": 83
    },
    "ielts": {
      "task_response": 7,
      "vocab": 7,
      "coherence": 7.5,
      "grammar": 7,
      "overall": 7.5
    },
    "cefr": {
      "task_response": "B2+",
      "vocab": "B2+",
      "coherence": "C1",
      "grammar": "B2+",
      "overall": "C1"
    },
    "pte": {
      "task_response": 66,
      "vocab": 66,
      "coherence": 76,
      "grammar": 66,
      "overall": 76
    },
    "toeic": {
      "task_response": 160,
      "vocab": 160,
      "coherence": 170,
      "grammar": 160,
      "overall": 170
    }
  },
  "version": "9.14"
}
```

{% endtab %}

{% tab title="short-writing w/ feedback" %}

```json
{
  "request_id": "9250948130c4e9e4c16567a995cc37e9",
  "status": "success",
  "task_type": "short-writing",
  "task_version": "0.1",
  "answer": "Last week I went to Hawaii with my family.It was good,first I go to swim on the ocean it was the best of my vacations ,but then I see lots of fishes on the ocean they were beutiful and they swim so fast.My dad talk me that the fishes were so fast because they has to swim from they born and if they do not swim fast the sharks eats them.It was all beutiful and interesting.That was my best vacations of all.",
  "char_count": 407,
  "writing_score": {
    "speechace": {
      "task_response": 44,
      "vocab": 28,
      "coherence": 28,
      "grammar": 22,
      "overall": 33
    },
    "ielts": {
      "task_response": 4,
      "vocab": 2.5,
      "coherence": 2.5,
      "grammar": 2,
      "overall": 3
    },
    "cefr": {
      "task_response": "A2+",
      "vocab": "A2",
      "coherence": "A2",
      "grammar": "A2",
      "overall": "A2+"
    },
    "pte": {
      "task_response": 20,
      "vocab": 12,
      "coherence": 12,
      "grammar": 10,
      "overall": 15
    },
    "toeic": {
      "task_response": 50,
      "vocab": 30,
      "coherence": 30,
      "grammar": 20,
      "overall": 30
    }
  },
  "feedback": {
    "vocab": [
      {
        "category": "Redundancy and Tautology",
        "original_phrase": "first I go to swim on the ocean",
        "replacement_phrase": "first I went swimming in the ocean",
        "rationale": "Avoid redundancy and use a more concise and accurate phrase.",
        "span": [
          54,
          84
        ],
        "replacements": [
          {
            "span": [
              62,
              73
            ],
            "text": "go to swim o",
            "type": "omission"
          },
          {
            "span": [
              74,
              74
            ],
            "text": "went swimming i",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Word Appropriateness",
        "original_phrase": "vacations",
        "replacement_phrase": "vacation",
        "rationale": "The correct term is 'vacation' when referring to a single trip.",
        "span": [
          108,
          116
        ],
        "replacements": [
          {
            "span": [
              116,
              116
            ],
            "text": "s",
            "type": "omission"
          }
        ]
      },
      {
        "category": "Word Appropriateness",
        "original_phrase": "vacations",
        "replacement_phrase": "vacation",
        "rationale": "Use the singular form 'vacation' for consistency.",
        "span": [
          108,
          116
        ],
        "replacements": [
          {
            "span": [
              116,
              116
            ],
            "text": "s",
            "type": "omission"
          }
        ]
      },
      {
        "category": "Redundancy and Tautology",
        "original_phrase": "but then I see lots of fishes on the ocean",
        "replacement_phrase": "but then I saw many fish in the ocean",
        "rationale": "Use the singular 'fish' instead of 'fishes' and improve clarity.",
        "span": [
          119,
          160
        ],
        "replacements": [
          {
            "span": [
              131,
              140
            ],
            "text": "ee lots of",
            "type": "omission"
          },
          {
            "span": [
              141,
              141
            ],
            "text": "aw many",
            "type": "insertion"
          },
          {
            "span": [
              146,
              149
            ],
            "text": "es o",
            "type": "omission"
          },
          {
            "span": [
              150,
              150
            ],
            "text": " i",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Word Appropriateness",
        "original_phrase": "beutiful",
        "replacement_phrase": "beautiful",
        "rationale": "Correct the spelling of 'beautiful'.",
        "span": [
          172,
          179
        ],
        "replacements": [
          {
            "span": [
              174,
              174
            ],
            "text": "a",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Word Appropriateness",
        "original_phrase": "beutiful",
        "replacement_phrase": "beautiful",
        "rationale": "Correct the spelling of 'beautiful'.",
        "span": [
          172,
          179
        ],
        "replacements": [
          {
            "span": [
              174,
              174
            ],
            "text": "a",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Precision and Nuance",
        "original_phrase": "My dad talk me that the fishes were so fast because they has to swim from they born",
        "replacement_phrase": "My dad explained that the fish swim fast because they need to swim from birth",
        "rationale": "Clarify the explanation and use correct verb tenses.",
        "span": [
          203,
          285
        ],
        "replacements": [
          {
            "span": [
              210,
              216
            ],
            "text": "talk me",
            "type": "omission"
          },
          {
            "span": [
              217,
              217
            ],
            "text": "explained",
            "type": "insertion"
          },
          {
            "span": [
              231,
              240
            ],
            "text": "es were so",
            "type": "omission"
          },
          {
            "span": [
              241,
              241
            ],
            "text": " swim",
            "type": "insertion"
          },
          {
            "span": [
              260,
              262
            ],
            "text": "has",
            "type": "omission"
          },
          {
            "span": [
              263,
              263
            ],
            "text": "need",
            "type": "insertion"
          },
          {
            "span": [
              277,
              285
            ],
            "text": "they born",
            "type": "omission"
          },
          {
            "span": [
              286,
              286
            ],
            "text": "birth",
            "type": "insertion"
          }
        ]
      }
    ],
    "coherence": [
      {
        "category": "Logical Progression of Ideas",
        "original_phrase": "first I go to swim on the ocean it was the best of my vacations ,but then I see lots of fishes on the ocean they were beutiful and they swim so fast.",
        "replacement_phrase": "First, I went swimming in the ocean. It was the highlight of my vacation. I saw many beautiful and fast-swimming fishes.",
        "rationale": "The revised sentence provides a clearer and more organized sequence of events, improving the logical progression of ideas.",
        "span": [
          54,
          202
        ],
        "replacements": [
          {
            "span": [
              54,
              54
            ],
            "text": "f",
            "type": "omission"
          },
          {
            "span": [
              55,
              55
            ],
            "text": "F",
            "type": "insertion"
          },
          {
            "span": [
              59,
              59
            ],
            "text": ",",
            "type": "insertion"
          },
          {
            "span": [
              62,
              73
            ],
            "text": "go to swim o",
            "type": "omission"
          },
          {
            "span": [
              74,
              74
            ],
            "text": "went swimming i",
            "type": "insertion"
          },
          {
            "span": [
              85,
              86
            ],
            "text": " i",
            "type": "omission"
          },
          {
            "span": [
              87,
              87
            ],
            "text": ". I",
            "type": "insertion"
          },
          {
            "span": [
              97,
              99
            ],
            "text": "bes",
            "type": "omission"
          },
          {
            "span": [
              100,
              100
            ],
            "text": "highligh",
            "type": "insertion"
          },
          {
            "span": [
              116,
              170
            ],
            "text": "s ,but then I see lots of fishes on the ocean they were",
            "type": "omission"
          },
          {
            "span": [
              171,
              171
            ],
            "text": ". I saw many",
            "type": "insertion"
          },
          {
            "span": [
              174,
              174
            ],
            "text": "a",
            "type": "insertion"
          },
          {
            "span": [
              185,
              201
            ],
            "text": "they swim so fast",
            "type": "omission"
          },
          {
            "span": [
              202,
              202
            ],
            "text": "fast-swimming fishes",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Effective Use of Linking Words",
        "original_phrase": "My dad talk me that the fishes were so fast because they has to swim from they born and if they do not swim fast the sharks eats them.",
        "replacement_phrase": "My dad explained to me that the fishes were fast because they have to swim from birth, and if they don't swim fast, the sharks will eat them.",
        "rationale": "The replacement phrase uses appropriate linking words ('explained,' 'because,' 'and'), making the connection between ideas clearer and improving the flow of the sentence.",
        "span": [
          203,
          336
        ],
        "replacements": [
          {
            "span": [
              210,
              213
            ],
            "text": "talk",
            "type": "omission"
          },
          {
            "span": [
              214,
              214
            ],
            "text": "explained to",
            "type": "insertion"
          },
          {
            "span": [
              239,
              241
            ],
            "text": "so ",
            "type": "omission"
          },
          {
            "span": [
              262,
              262
            ],
            "text": "s",
            "type": "omission"
          },
          {
            "span": [
              263,
              263
            ],
            "text": "ve",
            "type": "insertion"
          },
          {
            "span": [
              277,
              285
            ],
            "text": "they born",
            "type": "omission"
          },
          {
            "span": [
              286,
              286
            ],
            "text": "birth,",
            "type": "insertion"
          },
          {
            "span": [
              301,
              303
            ],
            "text": " no",
            "type": "omission"
          },
          {
            "span": [
              304,
              304
            ],
            "text": "n'",
            "type": "insertion"
          },
          {
            "span": [
              315,
              315
            ],
            "text": ",",
            "type": "insertion"
          },
          {
            "span": [
              327,
              327
            ],
            "text": "will ",
            "type": "insertion"
          },
          {
            "span": [
              330,
              330
            ],
            "text": "s",
            "type": "omission"
          }
        ]
      },
      {
        "category": "Focus and Relevance within Paragraph",
        "original_phrase": "It was all beutiful and interesting.",
        "replacement_phrase": "The experience was both beautiful and fascinating.",
        "rationale": "The revised phrase maintains the positive sentiment while providing a more focused and concise description of the experience.",
        "span": [
          337,
          372
        ],
        "replacements": [
          {
            "span": [
              337,
              338
            ],
            "text": "It",
            "type": "omission"
          },
          {
            "span": [
              339,
              339
            ],
            "text": "The experience",
            "type": "insertion"
          },
          {
            "span": [
              344,
              346
            ],
            "text": "all",
            "type": "omission"
          },
          {
            "span": [
              347,
              347
            ],
            "text": "both",
            "type": "insertion"
          },
          {
            "span": [
              350,
              350
            ],
            "text": "a",
            "type": "insertion"
          },
          {
            "span": [
              361,
              367
            ],
            "text": "interes",
            "type": "omission"
          },
          {
            "span": [
              368,
              368
            ],
            "text": "fascina",
            "type": "insertion"
          }
        ]
      }
    ],
    "grammar": [
      {
        "category": "Subject-Verb Agreement",
        "original_phrase": "first I go to swim on the ocean",
        "replacement_phrase": "first I went swimming in the ocean",
        "rationale": "Subject-verb agreement between 'I' and 'went'",
        "span": [
          54,
          84
        ],
        "replacements": [
          {
            "span": [
              62,
              73
            ],
            "text": "go to swim o",
            "type": "omission"
          },
          {
            "span": [
              74,
              74
            ],
            "text": "went swimming i",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Verb Tense Consistency",
        "original_phrase": "but then I see lots of fishes on the ocean",
        "replacement_phrase": "but then I saw lots of fishes in the ocean",
        "rationale": "Maintaining past tense consistency",
        "span": [
          119,
          160
        ],
        "replacements": [
          {
            "span": [
              131,
              132
            ],
            "text": "ee",
            "type": "omission"
          },
          {
            "span": [
              133,
              133
            ],
            "text": "aw",
            "type": "insertion"
          },
          {
            "span": [
              149,
              149
            ],
            "text": "o",
            "type": "omission"
          },
          {
            "span": [
              150,
              150
            ],
            "text": "i",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Verb Tense Consistency",
        "original_phrase": "My dad talk me that the fishes were so fast",
        "replacement_phrase": "My dad told me that the fishes were so fast",
        "rationale": "Maintaining past tense consistency",
        "span": [
          203,
          245
        ],
        "replacements": [
          {
            "span": [
              211,
              213
            ],
            "text": "alk",
            "type": "omission"
          },
          {
            "span": [
              214,
              214
            ],
            "text": "old",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Subject-Verb Agreement",
        "original_phrase": "they has to swim from they born",
        "replacement_phrase": "they have to swim from birth",
        "rationale": "Subject-verb agreement between 'they' and 'have'",
        "span": [
          255,
          285
        ],
        "replacements": [
          {
            "span": [
              262,
              262
            ],
            "text": "s",
            "type": "omission"
          },
          {
            "span": [
              263,
              263
            ],
            "text": "ve",
            "type": "insertion"
          },
          {
            "span": [
              277,
              285
            ],
            "text": "they born",
            "type": "omission"
          },
          {
            "span": [
              286,
              286
            ],
            "text": "birth",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Articles and Determiners",
        "original_phrase": "It was all beutiful and interesting",
        "replacement_phrase": "It was all beautiful and interesting",
        "rationale": "Correcting the spelling of 'beautiful' and adding the article 'a' before 'beautiful'",
        "span": [
          337,
          371
        ],
        "replacements": [
          {
            "span": [
              350,
              350
            ],
            "text": "a",
            "type": "insertion"
          }
        ]
      },
      {
        "category": "Verb Tense Consistency",
        "original_phrase": "That was my best vacations of all",
        "replacement_phrase": "That was my best vacation ever",
        "rationale": "Maintaining past tense consistency and correcting the plural form of 'vacations' to singular 'vacation'",
        "span": [
          373,
          405
        ],
        "replacements": [
          {
            "span": [
              398,
              405
            ],
            "text": "s of all",
            "type": "omission"
          },
          {
            "span": [
              406,
              406
            ],
            "text": " ever",
            "type": "insertion"
          }
        ]
      }
    ]
  },
  "version": "9.14"
}
```

{% endtab %}
{% endtabs %}

{% hint style="info" %}
**Which writing task\_type should I use?**

Here is when to use each task\_type:

If the writing is an essay and at least 150 words, then use essay-writing. If it's general and less than 150 words, then use short-writing. If the prompt contains a chat scenario with the user expected to respond to a question or situation, then use chat-writing.
{% endhint %}


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/intepreting-quality-score.md -->
# Speechace API Reference / Intepreting quality score

# Intepreting quality score

The Speechace `quality_score` measures the accuracy and intelligibility of the pronunciation of a sentence, word, syllable, or phoneme on a scale of 0 to 100.

The following rubric demonstrates how to use the `quality_score` to provide student feedback:

<table><thead><tr><th width="118">Score</th><th width="95">Color</th><th>Description</th></tr></thead><tbody><tr><td>90 - 100</td><td>Green</td><td>Excellent. Native or native-like</td></tr><tr><td>80 - 90</td><td>Green</td><td>Very Good and clearly intelligible.</td></tr><tr><td>70 - 80</td><td>Orange</td><td>Good. Intelligible but with one or two evident mistakes.</td></tr><tr><td>60 - 70</td><td>Red</td><td>Fair. Possibly not intelligible with several evident mistakes.</td></tr><tr><td>0 - 60</td><td>Red</td><td>Poor and must be reattempted.</td></tr></tbody></table>

Below is a snippet of the `quality_score` code for the word "Some" within the overall response. Each element of the response, including sentences, words, syllables, and phonemes, has its own distinct `quality_score`.

{% code overflow="wrap" lineNumbers="true" %}

```json5
"word_score_list":[
  {
    "word": "Some",
    "quality_score": 100,
    "phone_score_list": [
      {
        "phone": "s",
        "stress_level": null,
        "extent": [10,27],
        "quality_score": 99.05882352941177,
        "sound_most_like": "s"
      },
      ...
    }
]
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores.md -->
# Speechace API Reference / Interpreting overall scores

# Interpreting overall scores

Speechace API IELTS estimated scores are based on a standard IELTS scale of 0.0 to 9.0 and the standard [IELTS speaking band descriptors](https://www.ielts.org/-/media/pdfs/speaking-band-descriptors.ashx).

### Overall Score Bands

**9**

* Demonstrates excellent fluency and coherence with rare to no pauses.
* Has excellent vocabulary and demonstrates preciseness and sophistication in using words.
* Has excellent pronunciation and sounds similar to a native English speaker.

***

**8**

* Demonstrates excellent fluency and coherence with occasional pauses.
* Uses a wide range of vocabulary and idioms with rare mistakes.
* Uses perfect colloquial grammar while speaking.
* Has very good pronunciation with very mild accent.

***

**7**

* Demonstrates generally good fluency and coherence while speaking but may take occasional pauses.
* Is proficient in using sophisticated vocabulary and idiomatic structures.
* Proficient in expressing complex thoughts using a range of grammar structures.
* Has reasonably good pronunciation with some accent.

***

**6**

* Demonstrates reasonable fluency and coherence. Sometimes may hesitate while speaking due to inability to come up with appropriate vocabulary or grammar.
* Has a reasonably high vocabulary and frequently uses uncommon idiomatic structures.
* Generally uses good grammar while speaking but may make occasional mistakes.
* Can generally be understood while speaking but can mispronounce frequently.

***

**5**

* Demonstrates below average fluency and coherence. May need remedial training in speaking fluently and language construction.
* Has limited vocabulary and and may have difficulty expressing complex thoughts.
* Good at speaking simple sentences but regularly makes grammatical mistakes.
* Has below average pronunciation accuracy and may not be easy to understand.

***

**4 and below**

* Does not have adequate fluency and coherence and may need remedial training in speaking fluently.
* Has inadequate vocabulary and may need remedial coaching.
* Has below average knowledge of grammar usage in speaking. Needs language coaching to improve.
* Does not have adequate pronunciation accuracy and may require remedial training in spoken English.


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores/pronunciation-bands.md -->
# Speechace API Reference / Pronunciation Bands

# Pronunciation Bands

**9**

Uses a full range of pronunciation features with precision and subtlety. Sustains flexible use of features throughout. Is effortless to understand.

***

**8**

Uses a wide range of pronunciation features. Sustains flexible use of features, with only occasional lapses. Is easy to understand throughout; First language accent has minimal impact on intelligibility.

***

**7**

Confidently uses a range of pronunciation features and can be easily understood. At times may demonstrate some accent in speech.

***

**6**

Uses a range of pronunciation features with mixed control. Shows some effective use of features but this is not sustained. Can generally be understood throughout, though mispronunciation of individual words or sounds reduces clarity at times.

***

**5**

Produces basic sentence forms with reasonable accuracy in pronunciation. Uses a limited range of more complex structures, but these usually contain pronunciation errors and may cause some comprehension problems.

***

**4**

Uses a limited range of pronunciation features. Attempts to control features but lapses are frequent. Mispronunciations are frequent and cause some difficulty for the listener.

***

**3**

Has very limited pronunciation range and mispronounces regularly. Listeners will have much difficulty in understanding speech.

***

**2**

Speech is often unintelligible.

***

**1**

Speech is often unintelligible.

***

**0**

User did not attempt any questions.


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores/fluency-bands.md -->
# Speechace API Reference / Fluency Bands

# Fluency Bands

**9**

Speaks fluently with only rare repetition or self-correction. Any hesitation in speech is content-related rather than from lack of vocabulary or proper grammar. Speaks coherently with appropriate cohesion between sentences. Develops topics fully and appropriately.

***

**8**

Speaks fluently with only occasional repetition or self-correction; hesitation in speech is usually content-related and only rarely to search for vocabulary or grammatical constructs. Develops topics coherently and appropriately.

***

**7**

Speaks at length without noticeable effort or loss of coherence. May demonstrate hesitation in speech due to inability to come up with appropriate vocabulary or grammar. At times, may demonstrate some repetition and/or self-correction. Uses a range of connectives and discourse markers with some flexibility.

***

**6**

Capable of willing to speak at length, though may lose coherence at times due to occasional repetition, self-correction or hesitation. Uses a range of connectives and discourse markers but not always appropriately.

***

**5**

Usually maintains a good flow of speech but uses repetition, self correction and/or slow speech to keep going. May over-use certain connectives and discourse markers. Produces simple speech fluently, but more complex communication causes fluency problems.

***

**4**

Cannot respond without noticeable pauses and may speak slowly, with frequent repetition and self-correction. Links basic sentences but with repetitious use of simple connectives and some breakdowns in coherence.

***

**3**

Speaks with long pauses. Has limited ability to link simple sentences. Gives only simple responses and is frequently unable to convey basic message.

***

**2**

Pauses lengthily before most words. Little communication possible.

***

**1**

No communication possible. No rate-able language.

***

**0**

User did not attempt any questions.


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores/vocabulary-bands.md -->
# Speechace API Reference / Vocabulary Bands

# Vocabulary Bands

**9**

Uses vocabulary with full flexibility and precision in all topics. Uses idiomatic language naturally and accurately.

***

**8**

Uses a wide vocabulary resource readily and flexibly to convey precise meaning. Uses less common and idiomatic vocabulary skillfully, with occasional inaccuracies. Uses paraphrase effectively as required.

***

**7**

Uses vocabulary resource flexibly to discuss a variety of topics. Uses some less common and idiomatic vocabulary and shows some awareness of style and collocation, with some inappropriate choices. Uses paraphrase effectively.

***

**6**

The test-taker demonstrates a sufficiently broad vocabulary to discuss topics in detail and convey meaning clearly, even if some inaccuracies are present. Generally, they are successful at paraphrasing.

***

**5**

Manages to talk about familiar and unfamiliar topics but uses vocabulary with limited flexibility. Attempts to use paraphrase but with mixed success.

***

**4**

Is able to talk about familiar topics but can only convey basic meaning on unfamiliar topics and makes frequent errors in word choice. Rarely attempts paraphrase.

***

**3**

Uses simple vocabulary to convey personal information. Has insufficient vocabulary for less familiar topics.

***

**2**

Only produces isolated words or memorised utterances.

***

**1**

Only produces isolated words or memorised utterances.

***

**0**

User did not attempt any questions.

<br>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores/grammar-bands.md -->
# Speechace API Reference / Grammar Bands

# Grammar Bands

**9**

Uses a full range of structures naturally and appropriately. Produces consistently accurate structures apart from ‘slips’ characteristic of native speaker speech.

***

**8**

Uses a wide range of structures flexibly. Produces a majority of error-free sentences with only very occasional inappropriacies or basic/non-systematic errors.

***

**7**

Uses a range of complex structures with some flexibility. Frequently produces error-free sentences, though some grammatical mistakes persist.

***

**6**

Uses a mix of simple and complex structures, but with limited flexibility. May make frequent mistakes with complex structures though these rarely cause comprehension problems.

***

**5**

Produces basic sentence forms with reasonable accuracy. Uses a limited range of more complex structures, but these usually contain errors and may cause some comprehension problems.

***

**4**

Produces basic sentence forms and some correct simple sentences but subordinate structures are rare. Errors are frequent and may lead to misunderstanding.

***

**3**

Attempts basic sentence forms but with limited success, or relies on apparently memorised utterances. Makes numerous errors except in memorised expressions.

***

**2**

Cannot produce basic sentence forms.

***

**1**

Cannot produce basic sentence forms.

***

**0**

User did not attempt any questions.


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-overall-scores/coherence-bands.md -->
# Speechace API Reference / Coherence Bands

# Coherence Bands

**9**

Speaks coherently with fully appropriate cohesive features. Develops topics fully and appropriately

***

**8**

Develops topics coherently and appropriately.

***

**7**

Speaks at length without noticeable effort or loss of coherence. Uses a range of connectives and discourse markers with some flexibility.

***

**6**

Uses a range of connectives and discourse markers but not always appropriately.

***

**5**

May over-use certain connectives and discourse markers.

***

**4**

Links basic sentences but with repetitious use of simple connectives and some breakdowns in coherence.

***

**3**

Gives only simple responses and is frequently unable to convey basic message.

***

**2**

Little communication possible.

***

**1**

No communication possible.

***

**0**

User did not attempt any questions.


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores.md -->
# Speechace API Reference / Interpreting Writing Scores

# Interpreting Writing Scores

Speechace API evaluates writing along the 4 components defined by the [IELTS Writing Task 2](https://ielts.org/cdn/ielts-guides/ielts-writing-band-descriptors.pdf) criteria:

* Task Response
* Grammatical Range and Accuracy
* Lexical Resource
* Coherence and Cohesion


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores/writing-scoring-rubrics.md -->
# Speechace API Reference / Writing Scoring rubrics

# Writing Scoring rubrics

The Speechace API returns results within the mapped ranges for different proficiency bands. The following table provides a detailed mapping of cut-off scores for each band across standard rubrics, such as IELTS, CEFR, and how they correspond to Speechace scores.

| IELTS | CEFR | Speechace |
| ----- | ---- | --------- |
| 9.0   | C2   | 97-100    |
| 8.5   | C2   | 92-96     |
| 8.0   | C1+  | 86-91     |
| 7.5   | C1   | 81-85     |
| 7.0   | B2+  | 75-80     |
| 6.5   | B2   | 69-74     |
| 6.0   | B2   | 64-68     |
| 5.5   | B1+  | 58-63     |
| 5.0   | B1   | 53-57     |
| 4.5   | B1   | 47-52     |
| 4.0   | A2+  | 42-46     |
| 3.5   | A2+  | 35-41     |
| 3.0   | A2+  | 31-35     |
| 2.5   | A2   | 26-30     |
| 2.0   | A2   | 21-25     |
| 1.5   | A1+  | 16-20     |
| 1.0   | A1   | 11-15     |
| 0-0.5 | A0   | 0-10      |


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores/task-response-bands.md -->
# Speechace API Reference / Task Response Bands

# Task Response Bands

<table><thead><tr><th width="97.203125" align="center">Band</th><th>Description</th></tr></thead><tbody><tr><td align="center">9</td><td>The task is fully addressed with a clear, well-developed position. Ideas are relevant, well-supported, and explored in depth, with minimal lapses in content.</td></tr><tr><td align="center">8</td><td>The task is fully addressed with a clear and well-developed position. Ideas are relevant, extended, and well-supported, though occasional lapses in content may occur.</td></tr><tr><td align="center">7</td><td>The response effectively addresses the task with clear, well-developed ideas, though some over-generalizations slightly impact clarity. The tone is consistent, with minimal lapses.</td></tr><tr><td align="center">6</td><td>The response addresses the prompt, but some areas lack clarity or detail. The position is clear, though conclusions may be unclear or repetitive. Some ideas lack development, with minor inaccuracies, and the tone is mostly appropriate with slight inconsistencies.</td></tr><tr><td align="center">5</td><td>The response addresses the task, but some areas lack detail or clarity. The position is unclear at times, and ideas are insufficiently developed. There are irrelevant or inaccurate details, and the tone may fluctuate with repetition.</td></tr><tr><td align="center">4</td><td>The response partially addresses the prompt, with unclear position and poorly developed ideas. There is significant repetition, irrelevant details, and some parts of the task are unaddressed. The tone is inconsistent.</td></tr><tr><td align="center">3</td><td>The response fails to address the task, with irrelevant, repetitive ideas and no clear position. The content is minimal and underdeveloped, impacting effectiveness.</td></tr><tr><td align="center">2</td><td>The content is loosely related to the prompt, with no clear position or developed ideas, and lacks relevance to the task.</td></tr><tr><td align="center">1</td><td>The response is extremely limited, unrelated to the task, and lacks a clear position or relevant ideas.</td></tr><tr><td align="center">0</td><td>The submission shows no evidence of task completion and lacks an understanding of the requirements. No attempt is made to address the task.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores/grammar-bands.md -->
# Speechace API Reference / Grammar Bands

# Grammar Bands

<table><thead><tr><th width="97.203125" align="center">Band</th><th>Description</th></tr></thead><tbody><tr><td align="center">9</td><td>A wide range of structures is used with full flexibility and control. Punctuation and grammar are used appropriately throughout. Minor errors are extremely rare and have minimal impact on communication.</td></tr><tr><td align="center">8</td><td>A wide range of structures is flexibly and accurately used. The majority of sentences are error-free, and punctuation is well managed. Occasional, non-systematic errors and inappropriacies occur, but have minimal impact on communication.</td></tr><tr><td align="center">7</td><td>A variety of complex structures is used with some flexibility and accuracy. Grammar and punctuation are generally well controlled, and error-free sentences are frequent. A few errors in grammar may persist, but these do not impede communication.</td></tr><tr><td align="center">6</td><td>A mix of simple and complex sentence forms is used but flexibility is limited. Examples of more complex structures are not marked by the same level of accuracy as in simple structures. Errors in grammar and punctuation occur, but rarely impede communication.</td></tr><tr><td align="center">5</td><td>The range of structures is limited and rather repetitive. Although complex sentences are attempted, they tend to be faulty, and the greatest accuracy is achieved on simple sentences. Grammatical errors may be frequent and cause some difficulty for the reader. Punctuation may be faulty.</td></tr><tr><td align="center">4</td><td>A very limited range of structures is used. Subordinate clauses are rare and simple sentences predominate. Some structures are produced accurately but grammatical errors are frequent and may impede meaning. Punctuation is often faulty or inadequate.</td></tr><tr><td align="center">3</td><td>Sentence forms are attempted, but errors in grammar and punctuation predominate (except in memorized phrases or those taken from the input material). This prevents most meaning from coming through. Length may be insufficient to provide evidence of control of sentence forms.</td></tr><tr><td align="center">2</td><td>There is little or no evidence of sentence forms (except in memorised phrases).</td></tr><tr><td align="center">1</td><td>No rate-able language is evident.</td></tr><tr><td align="center">0</td><td>User did not attempt the question.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores/vocabulary-bands.md -->
# Speechace API Reference / Vocabulary Bands

# Vocabulary Bands

<table><thead><tr><th width="97.203125" align="center">Band</th><th>Description</th></tr></thead><tbody><tr><td align="center">9</td><td>Full flexibility and precise use are evident within the scope of the task. A wide range of vocabulary is used accurately and appropriately with very natural and sophisticated control of lexical features. Minor errors in spelling and word formation are extremely rare and have minimal impact on communication.</td></tr><tr><td align="center">8</td><td>A wide resource is fluently and flexibly used to convey precise meanings within the scope of the task. There is a skillful use of uncommon and/or idiomatic items when appropriate, despite occasional inaccuracies in word choice and collocation. Occasional errors in spelling and/or word formation may occur, but have minimal impact on communication.</td></tr><tr><td align="center">7</td><td>The resource is sufficient to allow some flexibility and precision. There is some ability to use less common and/or idiomatic items. An awareness of style and collocation is evident, though inappropriacies occur. There are only a few errors in spelling and/or word formation, and they do not detract from overall clarity.</td></tr><tr><td align="center">6</td><td>The resource is generally adequate and appropriate for the task. The meaning is generally clear in spite of a rather restricted range or a lack of precision in word choice. If the writer is a risk-taker, there will be a wider range of vocabulary used but higher degrees of inaccuracy or inappropriacy. There are some errors in spelling and/or word formation, but these do not impede communication.</td></tr><tr><td align="center">5</td><td>The resource is limited but minimally adequate for the task. Simple vocabulary may be used accurately but the range does not permit much variation in expression. There may be frequent lapses in the appropriacy of word choice, and a lack of flexibility is apparent in frequent simplifications and/or repetitions. Errors in spelling and/or word formation may be noticeable and may cause some difficulty for the reader.</td></tr><tr><td align="center">4</td><td>The resource is limited and inadequate for or unrelated to the task. Vocabulary is basic and may be used repetitively. There may be inappropriate use of lexical chunks (e.g. memorized phrases, formulaic language and/or language from the input material). Inappropriate word choice and/or errors in word formation and/or in spelling may impede meaning.</td></tr><tr><td align="center">3</td><td>The resource is inadequate (which may be due to the response being significantly underlength). Possible over-dependence on input material or memorized language. Control of word choice and/or spelling is very limited, and errors predominate. These errors may severely impede meaning.</td></tr><tr><td align="center">2</td><td>The resource is extremely limited with few recognisable strings, apart from memorized phrases. There is no apparent control of word formation and/or spelling.</td></tr><tr><td align="center">1</td><td>No resource is apparent, except for a few isolated words.</td></tr><tr><td align="center">0</td><td>User did not attempt the question.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-writing-scores/coherence-bands.md -->
# Speechace API Reference / Coherence Bands

# Coherence Bands

<table><thead><tr><th width="97.203125" align="center">Band</th><th>Description</th></tr></thead><tbody><tr><td align="center">9</td><td>The message can be followed effortlessly. Cohesion is used in such a way that it very rarely attracts attention. Any lapses in coherence or cohesion are minimal. Paragraphing is skillfully managed.</td></tr><tr><td align="center">8</td><td>The message can be followed with ease. Information and ideas are logically sequenced, and cohesion is well managed. Occasional lapses in coherence or cohesion may occur. Paragraphing is used sufficiently and appropriately.</td></tr><tr><td align="center">7</td><td>Information and ideas are logically organised and there is a clear progression throughout the response. A few lapses may occur. A range of cohesive devices including reference and substitution is used flexibly but with some inaccuracies or some over/under use.</td></tr><tr><td align="center">6</td><td>Information and ideas are generally arranged coherently and there is a clear overall progression. Cohesive devices are used to some good effect but cohesion within and/or between sentences may be faulty or mechanical due to misuse, overuse or omission. The use of reference and substitution may lack flexibility or clarity and result in some repetition or error.</td></tr><tr><td align="center">5</td><td>Organisation is evident but is not wholly logical and there may be a lack of overall progression. Nevertheless, there is a sense of underlying coherence to the response. The relationship of ideas can be followed but the sentences are not fluently linked to each other. There may be limited/overuse of cohesive devices with some inaccuracy. The writing may be repetitive due to inadequate and/or inaccurate use of reference and substitution.</td></tr><tr><td align="center">4</td><td>Information and ideas are evident but not arranged coherently, and there is no clear progression within the response. Relationships between ideas can be unclear and/or inadequately marked. There is some use of basic cohesive devices, which may be inaccurate or repetitive. There is inaccurate use or a lack of substitution or referencing.</td></tr><tr><td align="center">3</td><td>There is no apparent logical organisation. Ideas are discernible but difficult to relate to each other. Minimal use of sequencers or cohesive devices. Those used do not necessarily indicate a logical relationship between ideas. There is difficulty in identifying referencing.</td></tr><tr><td align="center">2</td><td>There is little relevant message, or the entire response may be off-topic. There is little evidence of control of organizational features.</td></tr><tr><td align="center">1</td><td>The writing fails to communicate any message and appears to be by a virtual non-writer.</td></tr><tr><td align="center">0</td><td>User did not attempt the question.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/scoring-rubrics.md -->
# Speechace API Reference / Scoring rubrics

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


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/interpreting-fidelity-class.md -->
# Speechace API Reference / Interpreting fidelity class

# Interpreting fidelity class

The `fidelity_class` field in Speechace indicates whether the user accurately completed the text utterance. If the value is not `CORRECT`, it suggests that the user's attempt may be unsuitable for scoring, as it may indicate incomplete, misunderstood, or unintended responses to the activity.

The following are the possible values returned by the `fidelity_class` field:

<table><thead><tr><th width="161">Value</th><th>Description</th></tr></thead><tbody><tr><td>CORRECT</td><td>The attempt is faithful and can be used for scoring and evaluation.</td></tr><tr><td>NO_SPEECH</td><td>No intelligible human speech is detected in the attempt.</td></tr><tr><td>INCOMPLETE</td><td>The attempt is incomplete indicating the recording may have been cut off or timed out.</td></tr><tr><td>FREE_SPEAK</td><td>The user is freely speaking outside the expected activity text.</td></tr></tbody></table>

In such cases the [Speechace Fluency API](../api-reference/score-text-fluency.md) will reduce the overall scores for the utterance and return a `score_issue_list[]` entry such as the below:

```json
"score_issue_list":
[
  {
    "status": "warning",
    "short_message": "response_incomplete",
    "detail_message": "The response doesn't follow the script completely.",
    "source": "fluency"
  }
]
```


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phonetic-notation.md -->
# Speechace API Reference / Phonetic notation

# Phonetic notation

The Speechace API returns phoneme level feedback using the [ARPAbet phonetic notation.](https://en.wikipedia.org/wiki/ARPABET)

You can map the phoneme notation to the commonly used [IPA (International Phonetic Alphabet)](https://en.wikipedia.org/wiki/International_Phonetic_Alphabet) using the below tables:

You can reference the tables in the following sections to map ARPAbet symbols to the commonly used [IPA (International Phonetic Alphabet)](https://en.wikipedia.org/wiki/International_Phonetic_Alphabet) for these languages:

* [**US English**](phonetic-notation/us-english-en-us.md)
* [**UK English**](phonetic-notation/uk-english-en-gb.md)
* [**French**](phonetic-notation/french-fr-fr-fr-ca.md) (France and Canada)
* [**Spanish**](phonetic-notation/spanish-es-es-es-mx.md) (Spain and Mexico)


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phonetic-notation/us-english-en-us.md -->
# Speechace API Reference / US English (en-us)

# US English (en-us)

| ARPABET | UNICODE      | IPA | EXAMPLE   |
| ------- | ------------ | --- | --------- |
| aa      | \u0251       | ɑ   |           |
| ae      | \u00e6       | æ   |           |
| ah0     | \u0259       | ə   | **a**bout |
| ah1     | \u028c       | ʌ   | m**u**d   |
| ao      | \u0254       | ɔ   |           |
| aw      | \u0061\u028a | aʊ  |           |
| ay      | a\u026a      | aɪ  |           |
| b       | b            | b   |           |
| ch      | t\u0283      | tʃ  |           |
| d       | d            | d   |           |
| dh      | \u00f0       | ð   |           |
| eh      | \u025b       | ɛ   |           |
| er      | \u025a       | ɚ   |           |
| ey      | e\u026a      | eɪ  |           |
| f       | f            | f   |           |
| g       | g            | g   |           |
| hh      | h            | h   |           |
| ih      | \u026a       | ɪ   |           |
| iy      | i            | i   |           |
| jh      | d\u0292      | dʒ  |           |
| k       | k            | k   |           |
| l       | l            | l   |           |
| m       | m            | m   |           |
| n       | n            | n   |           |
| ng      | \u014b       | ŋ   |           |
| ow      | o\u028a      | oʊ  |           |
| oy      | \u0254\u026a | ɔɪ  |           |
| p       | p            | p   |           |
| r       | r            | r   |           |
| s       | s            | s   |           |
| sh      | \u0283       | ʃ   |           |
| t       | t            | t   |           |
| th      | \u03b8       | θ   |           |
| uh      | \u028a       | ʊ   |           |
| uw      | u            | u   |           |
| v       | v            | v   |           |
| w       | w            | w   |           |
| y       | j            | j   |           |
| z       | z            | z   |           |
| zh      | \u0292       | ʒ   |           |

In cases of silence or noise, the API may also return the following phoneme codes:

| CODE | DESCRIPTION  |
| ---- | ------------ |
| sil  | Silence      |
| spn  | Spoken noise |


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phonetic-notation/uk-english-en-gb.md -->
# Speechace API Reference / UK English (en-gb)

# UK English (en-gb)

| ARPABET | UNICODE      | IPA | Example   |
| ------- | ------------ | --- | --------- |
| aa      | \u0251\u02d0 | ɑː  |           |
| ae      | \u00e6       | æ   |           |
| ah0     | \u0259       | ə   |           |
| ah1     | \u028c       | ʌ   |           |
| ao      | \u0254\u02d0 | ɔː  |           |
| aw      | a\u028a      | aʊ  |           |
| ax0     | \u0259       | ə   | **a**bort |
| ay      | a\u026a      | aɪ  |           |
| b       | b            | b   |           |
| ch      | t\u0283      | tʃ  |           |
| d       | d            | d   |           |
| dh      | \u00f0       | ð   |           |
| ea      | e\u0259      | eə  | m**a**re  |
| eh      | e            | e   |           |
| er      | \u025c\u02d0 | ɜː  |           |
| ey      | e\u026a      | eɪ  |           |
| f       | f            | f   |           |
| g       | g            | g   |           |
| hh      | h            | h   |           |
| ia      | \u026a\u0259 | ɪə  | mar**ia** |
| ih      | \u026a       | ɪ   |           |
| iy      | i\u02d0      | iː  |           |
| jh      | d\u0292      | dʒ  |           |
| k       | k            | k   |           |
| l       | l            | l   |           |
| m       | m            | m   |           |
| n       | n            | n   |           |
| ng      | \u014b       | ŋ   |           |
| oh      | \u0252       | ɒ   | t**o**m   |
| ow      | \u0259\u028a | əʊ  |           |
| oy      | \u0254\u026a | ɔɪ  |           |
| p       | p            | p   |           |
| r       | r            | r   |           |
| s       | s            | s   |           |
| sh      | \u0283       | ʃ   |           |
| t       | t            | t   |           |
| th      | \u03b8       | θ   |           |
| ua      | \u028a\u0259 | ʊə  | t**ou**r  |
| uh      | \u028a       | ʊ   |           |
| uw      | u\u02d0      | uː  |           |
| v       | v            | v   |           |
| w       | w            | w   |           |
| y       | j            | j   |           |
| z       | z            | z   |           |
| zh      | \u0292       | ʒ   |           |


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phonetic-notation/french-fr-fr-fr-ca.md -->
# Speechace API Reference / French (fr-fr, fr-ca)

# French (fr-fr, fr-ca)

### **Vowels**

| EXAMPLE | ARPABET | IPA |
| ------- | ------- | --- |
| sa      | AD      | a   |
| sans    | AGN     | ɑ̃  |
| gredin  | AH      | ʌ   |
| fée     | EE      | e   |
| fait    | EH      | ɛ   |
| brin    | EHN     | ɛ̃  |
| ceux    | EU      | ø   |
| su      | IO      | y   |
| si      | IY      | iː  |
| jeune   | OE      | œ   |
| brun    | OEN     | œ̃  |
| sort    | OO      | ɔ   |
| son     | OON     | ɔ̃  |
| sot     | UO      | o   |
| sous    | UW      | uː  |

### **Consonants**

| EXAMPLE | ARPABET | IPA |
| ------- | ------- | --- |
| boue    | B       | b   |
| doux    | D       | d   |
| fou     | F       | f   |
| goût    | G       | g   |
| lasagne | GN      | ŋ   |
| pluie   | HJ      | ɥ   |
| cou     | K       | k   |
| loup    | L       | l   |
| mou     | M       | m   |
| nous    | N       | n   |
| kung-fu | NG      | ŋ   |
| pou     | P       | p   |
| roue    | RW      | ʁ   |
| sous    | S       | s   |
| chou    | SH      | ʃ   |
| tout    | T       | t   |
| vous    | V       | v   |
| oui     | W       | w   |
| hier    | Y       | j   |
| zou     | Z       | z   |
| joue    | ZH      | ʒ   |


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phonetic-notation/spanish-es-es-es-mx.md -->
# Speechace API Reference / Spanish (es-es, es-mx)

# Spanish (es-es, es-mx)

### **Vowels**

| EXAMPLE   | ARPABET | IPA |
| --------- | ------- | --- |
| papa      | AD      | a   |
| aire      | AI      | ai  |
| pausa     | AU      | au  |
| esperanza | EE      | e   |
| rey       | EE YI   | ej  |
| reina     | EI      | ei  |
| neutro    | EW      | eu  |
| chica     | IY      | iː  |
| boina     | OI      | oi  |
| bou       | OU      | ou  |
| loco      | UO      | o   |
| hoy       | UO YI   | oj  |
| grupo     | UW      | uː  |
| muy       | UW YI   | uːj |
| cuadro    | WA      | wa  |
| fuego     | WE      | we  |
| fuimos    | WI      | wi  |
| cuota     | WO      | wo  |
| hacia     | YI AD   | ja  |
| tierra    | YI EE   | je  |
| radio     | YI UO   | jo  |
| viuda     | YI UW   | juː |

### **Consonants**

| EXAMPLE | ARPABET | IPA |
| ------- | ------- | --- |
| bello   | B       | b   |
| chico   | CH      | tʃ  |
| dar     | D       | d   |
| fresco  | F       | f   |
| gato    | G       | g   |
| español | GN      | ɲ   |
| cama    | K       | k   |
| pala    | L       | l   |
| madre   | M       | m   |
| noche   | N       | n   |
| padre   | P       | p   |
| ropa    | RR      | r   |
| madera  | RT      | ɾ   |
| solo    | S       | s   |
| tocar   | T       | t   |
| zorro   | TH      | θ   |
| jamón   | X       | x   |
| llama   | YL      | ʎ   |


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/getting-word-timestamps-in-audio.md -->
# Speechace API Reference / Getting word timestamps in audio

# Getting word timestamps in audio

Speechace segments and aligns user audio at the word, syllable, and phoneme levels. The Speechace API provides detailed extent information for each level:

* **Syllable Level:** Data is returned in the `syllable_score_list[]` array.
* **Phoneme Level:** Data is returned in the `phone_score_list[]` array.

The `extent[]` field contains begin and end timestamps for that syllable or phoneme in units of 10 msec.

In the example below the phoneme */sh/* is at msec 250 to 350 in the user audio file:

Timestamp extent information can be used to zoom in and playback specific words, allowing for the demonstration of a test-taker's mistakes or the correct pronunciation of a word from a reference example.

To do so you need to iterate through the Speechace API JSON result as follows:

{% code overflow="wrap" lineNumbers="true" %}

```python
for each word in text_score.word_score_list[]

    get first and last elements of phone_score_list[] for that word
    
    start_timestamp is extent[0] for the first element
    end_timestamp is extent[1] for the last element
    
    # timestamps are in unit of 10 msec
```

{% endcode %}


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/automatic-handling-of-unknown-words.md -->
# Speechace API Reference / Automatic handling of unknown words

# Automatic handling of unknown words

Speechace returns the error code `error_unknown_words` for terms not found in its lexicon. To avoid this error and allow Speechace to automatically determine the phonetic mapping for unknown terms, include the `include_unknown_words` parameter in the request body.

Pass the following form-data parameter:

```json5
include_unknown_words=1
```


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/phoneme-to-letter-mapping.md -->
# Speechace API Reference / Phoneme to letter mapping

# Phoneme to letter mapping

Some applications may highlight letters in a word where pronunciation errors occurred to help learners visualize their mistakes, especially if they are unfamiliar with phonemes.

For example, if a user mispronounces the /sh/ sound in "shift," the application could display the word with the problematic letters crossed out or color-coded to indicate errors:

**Incorrect Example:**

`**sh**ift` (where "sh" is highlighted to show the pronunciation issue)

The Speechace API does not score individual letters; instead, it decomposes each scored word into its phonemes and scores them at the phoneme level.

To identify which letter(s) in a word correspond to a specific phoneme:

1. **Access Phoneme Data:**
   * Retrieve phoneme-level scores from the `word_score_list[].phone_score_list[]` array.
2. **Locate Letter Mapping:**
   * For each phoneme, use the `phone_score_list[].word_extent[]` field to find the character indices within the word that this phoneme maps to. This field contains the `[begin, end]` character indices.

By checking these indices, the application can map the phoneme to its corresponding letters in the word.

In the above example of the word "shift", the contents of `phone_score_list[]` would be:

| phone | word\_extent | letter(s) |
| ----- | ------------ | --------- |
| sh    | \[0,2]       | sh        |
| ih    | \[2,3]       | i         |
| f     | \[3,4]       | f         |
| t     | \[4,5]       | t         |

The letters column is built using the character indices specified in `word_extent`. Notice that a phoneme may correspond to more than one letter. For instance, a phoneme like /sh/ in "shift" covers two letters (e.g., 'sh') as shown above.

There are multiple possibilities of phoneme to letter mapping that the API caller should be prepared for:

<table><thead><tr><th width="271">Case</th><th>word_extent</th><th>Example</th></tr></thead><tbody><tr><td>Phoneme maps to zero letters</td><td>[n, n] where the begin and end indices are the same.<br><br>The value of n depends on the phoneme's position in the word</td><td>The word "#" (i.e. hashtag) which is phonetically expanded to ['hh', 'ae', 'sh', 't', 'ae', 'g']</td></tr><tr><td>Phoneme maps to 1 letter</td><td>[n, n+1]</td><td>/f/ in "shift"</td></tr><tr><td>Phoneme maps to 2 letters</td><td>[n, n+2]</td><td>/sh/ in "shift"</td></tr><tr><td>2 Phonemes map to 1 letter</td><td>Each phoneme will have the same word_extent:<br>[n, n+1]<br>[n, n+1]</td><td>letter 'x' in the word "exempt" maps to the phonemes /g/, /z/</td></tr><tr><td>2 or more Phonemes map to zero letters</td><td>Each phoneme will have the same word_extent:<br>[n, n]</td><td>The word "w" which is phonetically expanded to ['d', 'ah', 'b', 'ah', 'l', 'y', 'u', 'w']</td></tr><tr><td>No Phoneme to letter mapping available</td><td>word_extent field will be missing</td><td>Words outside of the Speechace lexicon processed with markup_language or include_unknown_words handling.</td></tr></tbody></table>


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/markup-language.md -->
# Speechace API Reference / Markup Language

# Markup Language

In [scoring text using markup language](../features/scripted-activities/pronunciation-scoring/custom-pronunciations.md), the input can include markups to explicitly define phonetic mappings for one or more words, allowing you to bypass the Speechace lexicon. This feature is supported for both en-US and en-GB requests and can be utilized in scoring, multiple-choice, or Validate Text requests.

### Markup Syntax

Each markup has the pattern: `[l1|l2|...|ln]{s1|s2|...|sn}`, where:

* `l1`, `l2`, ..., `ln` are substrings of a word in the input text, and
* `s1`, `s2`, ..., `sn` are syllables of the word corresponding to `l1`, `l2`, ..., `ln`.
* Each syllable, `si`, has this pattern: `p1` `p2` ... `pk`, where `pi` is a phoneme in [ARPABET notation](phonetic-notation.md) for the current dialect (i.e. en-us or en-gb).
* If `pi` is a vowel phoneme, `pi` ends with 0, 1 or 2 to denote the stress level of the syllable.
* There should be at most one vowel per syllable.

You can now pass the parameter `markup_language=arpa_mark` to the [API](../features/scripted-activities/pronunciation-scoring/custom-pronunciations.md) and include markup for words within the text as follows:

{% code overflow="wrap" %}

```
He [read]{r eh1 d} his [frag|ments]{f r ae1 g|m ah0 n t s} aloud.
```

{% endcode %}

In the text above, the words "read" and "fragments" are marked up to disambiguate them from heteronyms.

### Expressing letters in markup

Optionally, markup can specify letter-to-phoneme mapping. This allows the Speechace API to return [phoneme-to-letter mapping](phoneme-to-letter-mapping.md) in the results for the marked-up words.

To add letter information in markup:

1. **Prefix the first syllable**: Add `2|` before `s1` (the first syllable) as follows: `[l1|l2|...|ln]{2|s1|s2|...|sn}`. This indicates that the markup contains phoneme-to-letter mapping information.
2. **Mapping Syntax**: Use zero or more pairs of angular brackets `<` and `>` to group substrings in `li` (letters) and `si` (phonemes) for establishing one-to-one correspondence when necessary.
   * `li` consists of a sequence of characters `c1c2...cp`
   * `si` consists of a sequence of phonemes `p1 p2 ... pk`
3. **Simple Mapping**: If there is a one-to-one mapping between letters and phonemes, no additional markup is needed.
4. **Inference of Mapping**: If there are no pairs of `<` and `>` in `li` and `si`, and if `p` (the number of phonemes) equals `k` (the number of letters), we infer:
   * `c1` is mapped to `p1`
   * `c2` is mapped to `p2`
   * ...,
   * `cp` is mapped to `pk`
5. **Complex Cases**: For cases where character-to-phoneme mapping is more complex than one-to-one:
   * Use `<` and `>` to group adjacent characters and/or phonemes.
   * Empty brackets `<>` can be used to denote mappings where `m` characters correspond to `n` phonemes (with `m >= 0` and `n >= 0`, but not both zero).

This transforms `li` and `si` into sequences of the same length to facilitate one-to-one mapping.

This can be quite complex, so let's provide illustrative examples that cover all real-world cases. This way, you can easily copy and modify an example instead of constructing the notation from scratch.

### **Markup examples with letters**

Here’s a polished and professional version of your examples:

#### Example 1:

**Markup**: `[car]{2|k aa1 r}`\
**Explanation**:\
This example features one syllable without the need for `<` and `>`. Here, `l1` is "car" and `s1` is "k aa1 r." We can infer that:

* `c1` maps to `p1`
* `c2` maps to `p2`
* `c3` maps to `p3`

Thus, we have:

* `c1 = c`
* `c2 = a`
* `c3 = r`
* `p1 = k`
* `p2 = aa1`
* `p3 = r`

***

#### Example 2:

**Markup**: `[bi|li<ng>|<>ual]{2|b ay0|l ih1 ng|g w ah0 l}`\
**Explanation**:\
In this case, we have zero characters mapping to one phoneme in `l3` as `<>ual` and `s3` as `g w ah0 l`. The `<>` allows us to infer that the phoneme `g` is mapped to no character. Thus:

* `c1 =`
* `c2 = u`
* `c3 = a`
* `c4 = l`
* `p1 = g`
* `p2 = w`
* `p3 = ah0`
* `p4 = l`

***

#### Example 3:

**Markup**: `[M<>|<><r.>]{2|m <ih1 s>|t er0}`\
**Explanation**:\
This example shows zero characters mapping to two phonemes. Here, `l1` is `M<>` and `s1` is `m <ih1 s>`. The `<>` and `<ih1 s>` allow us to infer that the phonemes `ih1 s` are mapped to no character. Thus:

* `c1 = M`
* `p1 = m`
* `p2 = ih1 s`

***

#### Example 4:

**Markup**: `[ear]{2|iy1 <> r}`\
**Explanation**:\
In this instance, we have one character mapping to zero phonemes. Here, `l1` is "ear" and `s1` is "iy1 <> r." The `<>` indicates that the character `a` is mapped to no phoneme. Thus:

* `c1 = e`
* `c2 = a`
* `c3 = r`
* `p1 = iy1`
* `p2 =`
* `p3 = r`

***

#### Example 5:

**Markup**: `[box]{2|b aa1 <k s>}`\
**Explanation**:\
This example features one character mapping to two phonemes. Here, `l1` is "box" and `s1` is "b aa1 ." The `<k s>` allows us to infer that the character `x` is mapped to the phonemes `k s`. Thus:

* `c1 = b`
* `c2 = o`
* `c3 = x`
* `p1 = b`
* `p2 = aa1`
* `p3 = k s`

***

#### Example 6:

**Markup**: `[si<gh>]{2|s ay1 <>}`\
**Explanation**:\
In this case, we have two characters mapping to zero phonemes. Here, `l1` is `si<gh>` and `s1` is `s ay1 <>`. The `<gh>` and `<>` allow us to infer that the characters `gh` are mapped to no phoneme. Thus:

* `c1 = s`
* `c2 = i`
* `c3 = gh`
* `p1 = s`
* `p2 = ay1`
* `p3 =`

***

#### Example 7:

**Markup**: `[no<th>|i<ng>]{2|n ah1 th|ih0 ng}`\
**Explanation**:\
This example features two characters mapping to one phoneme. Here, `l1` is `no<th>` and `s1` is `n ah1 th`. The `<th>` allows us to infer that the characters `th` are mapped to the phoneme `th`. Thus:

* `c1 = n`
* `c2 = o`
* `c3 = th`
* `p1 = n`
* `p2 = ah1`
* `p3 = th`

***

#### Example 8:

**Markup**: `[qu<eue>]{2|k <y uw1> <>}`\
**Explanation**:\
In this instance, we have three characters mapped to zero phonemes. Here, `l1` is `qu<eue>` and `l2` is `k <y uw1> <>`. The `<eue>` and `<>` allow us to infer that the characters `eue` are mapped to no phoneme. Thus:

* `c1 = q`
* `c2 = u`
* `c3 = eue`
* `p1 = k`
* `p2 = y uw1`
* `p3 =`


---

<!-- source: https://api-docs.speechace.com/guides-on-common-topics/detecting-speech-interference.md -->
# Speechace API Reference / Detecting Speech Interference

# Detecting Speech Interference

When scoring pronunciation, the audio submitted to the Speechace API should contain only the speech the user intends to be assessed — the words in the `text` parameter. In real-world conditions, however, recordings often contain additional speech beyond what was expected: a speaker in the background, the user continuing to speak after the expected text, or another person's voice overlapping with the user's.

This additional speech is called **interference**. The Speechace API can detect and measure it, returning an `interference_ratio` that tells you how much of the audio contains speech that competes with the intended text for alignment and scoring.

## Interference vs. General Noise

It is important to distinguish between **interference** and general background noise:

* **Background noise** — non-speech sounds such as music, traffic, typing, or ambient room noise. These affect recording quality but do not compete directly with the alignment and scoring of the expected words.
* **Speech interference** — *speech* that is present in the audio beyond what the `text` parameter describes. This directly competes with the scoring engine when it attempts to align and score what was spoken. Common sources include:
  * A second speaker in the room talking over the user
  * The user continuing to speak additional sentences after the expected text ends
  * A TV, podcast, or voice recording playing in the background

Speechace's interference detection is specifically focused on **competing speech**, not general noise.

## How to Request Interference Metrics

Pass `include_interference_metrics = 1` as a form body parameter in your [Score Text/Pronunciation](../api-reference/score-text-pronunciation.md) or [Score Word](../api-reference/score-word.md) request:

```bash
curl --location -g 'https://api.speechace.co/api/scoring/text/v9/json?key={{speechacekey}}&dialect=en-us' \
--form 'text="Some parents admire famous athletes as strong role models."' \
--form 'user_audio_file=@"recording.wav"' \
--form 'include_interference_metrics=1'
```

Without this parameter, `interference_ratio` is not returned in the response.

## Response

When `include_interference_metrics = 1` is set, the `text_score` object in the response includes an `interference_ratio` field:

```json
{
  "status": "success",
  "text_score": {
    "text": "Some parents admire famous athletes as strong role models.",
    "interference_ratio": 2.7,
    "word_score_list": [ ... ],
    "speechace_score": {
      "pronunciation": 85
    }
  }
}
```

## Interpreting interference\_ratio

The `interference_ratio` is a numeric value that increases with the severity of detected speech interference. Use the following bands as a guide:

<table><thead><tr><th width="186.50390625" align="center">interference_ratio</th><th width="147.2890625" align="center">Level</th><th>Interpretation</th></tr></thead><tbody><tr><td align="center"><code>0</code></td><td align="center"><strong>None</strong></td><td>No excess speech detected. The audio closely matches the expected text with no competing speech.</td></tr><tr><td align="center"><code>0</code> – <code>1</code></td><td align="center"><strong>Low</strong></td><td>A small amount of extra speech is present — for example, a brief word spoken before or after the intended text. Scoring results are reliable.</td></tr><tr><td align="center"><code>2</code> – <code>3</code></td><td align="center"><strong>Mid</strong></td><td>A moderate amount of competing speech is detected. Scoring may be affected; consider surfacing a caution to the user.</td></tr><tr><td align="center"><code>3+</code></td><td align="center"><strong>High</strong></td><td>Significant speech interference is present. Scoring reliability is reduced. The user should be prompted to retry in a quieter environment.</td></tr></tbody></table>

## Using interference\_ratio in Your Application

The `interference_ratio` gives your application actionable signal about recording conditions. Here are the recommended patterns:

Warn about reduced scoring reliability

```
if interference_ratio >= 2:
    show_warning("Your recording contained background speech. "
                 "Your score may not fully reflect your pronunciation.")
```

Guide users to retry in a quieter environment

```
if interference_ratio >= 3:
    show_prompt("We detected other voices in your recording. "
                "Please try again somewhere quieter for a more accurate score.")
```

Apply reduced weighting to high-interference items

```
if interference_ratio >= 3:
    flag_for_review(item_id, reason="high_interference")
    # Exclude from final score calculation or prompt a re-attempt
```

## Notes

<details>

<summary>Additional notes about interference metrics</summary>

* `interference_ratio` is only present in the response when `include_interference_metrics = 1` is explicitly passed. It is not returned by default.
* Interference detection applies to the Score Text/Pronunciation endpoint. See the [API Reference](broken://pages/b856b7a8a7db6afd35402c83995619e2348d0821) for the full parameter list.

</details>


---

<!-- source: https://api-docs.speechace.com/other-resources/requesting-support.md -->
# Speechace API Reference / Requesting Support

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


---

<!-- source: https://api-docs.speechace.com/other-resources/rate-limiting.md -->
# Speechace API Reference / Rate Limiting

# Rate Limiting

Speechace is architected to be highly available and auto-scalable to serve request spikes as needed. The API provides a concurrency guarantee of up to 16 concurrent Transactions per Second (TPS). Above that limit, the API will throttle calls returning `error_too_many_requests.`

A higher concurrency limit is available with custom plans and annual contracts. You may contact us at <support@speechace.com> to request a higher limit.


---

<!-- source: https://api-docs.speechace.com/other-resources/data-retention.md -->
# Speechace API Reference / Data Retention

# Data Retention

Data passed to the Speechace API is the property of the API caller. We only process and retain the data to serve your request or subsequent support queries. You can choose a specific data retention period, including zero retention, in your Speechace API contract. To query or request specific data retention needs please email <support@speechace.com>


---

<!-- source: https://api-docs.speechace.com/other-resources/sdks-and-libraries.md -->
# Speechace API Reference / SDKs and Libraries

# SDKs and Libraries

* Official SDKs&#x20;
  * List available SDKs for different languages.&#x20;
* Third-Party Libraries&#x20;
  * Mention any useful third-party tools.&#x20;
* Code Samples&#x20;
  * Provide code samples or links to repositories.&#x20;


---

<!-- source: https://api-docs.speechace.com/other-resources/faqs.md -->
# Speechace API Reference / FAQs

# FAQs

## General

#### How are you different from other speech recognition providers?

Most speech recognition providers focus on transcribing what the user is speaking. Speechace is designed and trained to assess how well the user is speaking. Our focus is on evaluating the spoken language of a non-native speaker – i.e. how intelligible, natural and rich is their language. Our models are trained and optimized for this specific purpose.

#### What can I achieve with the Speechace API?

The Speechace API is a technology building block which allows you to add speaking activities with AI scoring within any eLearning platform. If you are looking for a ready to use solution, please use the Contact form below and our team will share additional options less technical than the API.

#### Do you offer a free to try sandbox?

All of our API plans come with a free trial. You can click ‘Start Free Trial’ on any of the[ API plans](https://www.speechace.com/api-plans/#pricing) and get started.

#### How long does it take to integrate the Speechace API into my platform?

If your platform and developers are ready, and you follow the docs and samples, you can implement one speaking activity in less than 1 week and trial it with users. Our API trial is designed to allow you to test out the API before you have to pay for a subscription.

#### How accurate are your API results?

We go to great lengths in running independent, blind, randomized evaluations for all our models to ensure our API results are within close agreement range with qualified human evaluators. Our API has been independently verified by both Academic and Corporate entities.

Please fill out the [Contact Us form](https://www.speechace.com/api-plans/#contact) and we’d be glad to share technical reports detailing our performance and accuracy metrics.

#### How does Speechace cater to a variety of native and non-native accents?

Speechace tackles the challenge of accent through ensuring the training and validation datasets represent a wide range of speakers in terms of demographics, language proficiency and accent. This allows our general model to be optimized for the target result of measuring intelligible, natural, and fluent speech irrespective of accent.

The mere presence of accent is not considered a criteria for penalizing a speaker, so long as it does not interfere with intelligibility and fluency.

## API Calls

#### Why am I seeing a "CORS policy blocked" error when making an API request?

By default, CORS (Cross-Origin Resource Sharing) is disabled on the Speechace API to protect the security of your API key. This means that **direct calls from the frontend**, whether your application is running locally (e.g., `http://localhost`) or in a live production environment (e.g., `https://yourwebsite.com`), are blocked by the browser due to security reasons.

To securely access the Speechace API, it should be called from your **backend server**. If you'd still like to make API calls directly from the frontend, we can enable CORS on your subscription, but please note that this exposes your API key to the client side and introduces security risks.

If you'd still like to enable CORS, please contact Speechace support at <mark style="color:blue;"><contact@speechace.com>.</mark>

## API Billing

#### How does API Billing work?

Your monthly subscription fee counts as credit from which monthly usage is deducted. If your usage is higher than the credit, you will receive an overage charge for the additional usage.

For example:

1. You are subscribed to the API Pro plan
2. Your monthly subscription fee is $80
3. During the month of January, your usage was 15,000 successful Pro 15-Sec requests
4. Therefore your overage is calculated as follows:
   1. Credit: $80
   2. Usage: 15,000 \* $0.008 = $120
   3. Overage: $120 - $80 = $40
5. Your overage charge will be posted after the month of January ends (i.e. in Febraury)

#### What are the API Request rates?

| Request Type | Unit              | Max Request Size | Cost       |
| ------------ | ----------------- | ---------------- | ---------- |
| Basic        | 15-Sec Request    | 30 Sec           | 0.008 USD  |
| Pro          | 15-Sec Request    | 45 Sec           | 0.008 USD  |
| Premium      | 15-Sec Request    | 120 Sec          | 0.0125 USD |
| Writing      | 400 Chars Request | 4096 Chars       | 0.048 USD  |

#### When are API overages calculated?

Overages are calculated on a calendar month basis at the end of the month.

#### How is usage calculated?

Usage is calculated based on the type and number of 15-sec requests. Only successful requests count for billing. Each request duration is rounded to the nearest increment in 15-Sec. For example a 1 minute audio counts as 4 15-Sec requests.

#### What if I don't use my entire monthly fee?

In monthly subscriptions, if you do not consume all of your monthly credits, you lose the remaining credits. You can avoid that by choosing an annual subscription which allows credit usage within the year or credit rollover across contract years. Please use the [Contact form](https://www.speechace.com/api-plans/#contact) to inquire about annual plans.

#### Do you offer Enterprise or custom plans?

Yes, we offer volume, bulk, and discounted annual plans.

Please use the [Contact form](https://www.speechace.com/api-plans/#contact) on our website and share details about your use case and volume. Our team will share details.


---

<!-- source: https://api-docs.speechace.com/other-resources/changelog.md -->
# Speechace API Reference / Changelog

# Changelog

* Versioning&#x20;
  * Explain the versioning system.&#x20;
* Release Notes&#x20;
  * Log updates, new features, and fixes.&#x20;


---

<!-- source: https://api-docs.speechace.com/other-resources/appendices.md -->
# Speechace API Reference / Appendices

# Appendices

## Major versioning history

<table><thead><tr><th width="111">Version</th><th width="424">URL</th><th>Description</th></tr></thead><tbody><tr><td>9.17</td><td><a href="https://api.speechace.co/api/scoring/text/v9.14/json?">https://api.speechace.co/api/scoring/text/v9.17/json?</a></td><td>New scoring model updates for single word scoring<br><a href="/pages/1Ce6eMrvi9C25gYXZL0Z">Score Word API</a>.</td></tr><tr><td>9.16</td><td><a href="https://api.speechace.co/api/scoring/text/v9.14/json?">https://api.speechace.co/api/scoring/text/v9.16/json?</a></td><td>Improvements and fixes to Pitch and Intonation model</td></tr><tr><td>9.15</td><td><a href="https://api.speechace.co/api/scoring/text/v9.14/json?">https://api.speechace.co/api/scoring/text/v9.15/json?</a></td><td>Improvement and fixes to core scoring models.</td></tr><tr><td>9.14</td><td><a href="https://api.speechace.co/api/scoring/text/v9.14/json?">https://api.speechace.co/api/scoring/text/v9.14/json?</a></td><td><p></p><ul><li>Major Spontaneous Speech Transcription model update</li><li>Spontaneous Speech Scoring model updates</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.13</td><td><a href="https://api.speechace.co/api/scoring/text/v9.13/json?">https://api.speechace.co/api/scoring/text/v9.13/json?</a></td><td><p></p><ul><li>Core Pronunciation model update</li><li>Lexicon and alt Dictionary support</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.12</td><td><a href="https://api.speechace.co/api/scoring/text/v9.12/json?">https://api.speechace.co/api/scoring/text/v9.12/json?</a></td><td><p></p><ul><li>Core Pronunciation model update</li><li>Connected Speech expansion</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.11</td><td><a href="https://api.speechace.co/api/scoring/text/v9.11/json?">https://api.speechace.co/api/scoring/text/v9.11/json?</a></td><td><p></p><ul><li>Core Pronunciation model update</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.10</td><td><a href="https://api.speechace.co/api/scoring/text/v9.10/json?">https://api.speechace.co/api/scoring/text/v9.10/json?</a></td><td><p></p><ul><li>VAD model update scoring/text, scoring/speech</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.9</td><td><a href="https://api.speechace.co/api/scoring/text/v9.9/json?">https://api.speechace.co/api/scoring/text/v9.9/json?</a></td><td><ul><li>Core Pronunciation model updates</li><li>Major performance enhancement</li><li>Bug fixes and minor enhancements</li></ul></td></tr><tr><td>9.8</td><td><a href="https://api.speechace.co/api/scoring/text/v9.8/json?">https://api.speechace.co/api/scoring/text/v9.8/json?</a></td><td><p></p><ul><li>Pronunciation model update</li><li>Grammatical accuracy model update</li></ul></td></tr><tr><td>9.7</td><td><a href="https://api.speechace.co/api/scoring/text/v9.7/json?">https://api.speechace.co/api/scoring/text/v9.7/json?</a></td><td><p></p><ul><li>Noise and alignment updates</li><li>New relevance scoring model</li><li>Markup language support for letter to phoneme mapping</li></ul></td></tr><tr><td>9.5</td><td><a href="https://api.speechace.co/api/scoring/text/v9.5/json?">https://api.speechace.co/api/scoring/text/v9.5/json?</a></td><td><p></p><ul><li>New noise reduction enhancement</li><li>Beta Grammar, Coherence, Vocab feedback metrics</li></ul></td></tr><tr><td>9.4</td><td><a href="https://api.speechace.co/api/scoring/text/v9.4/json?">https://api.speechace.co/api/scoring/text/v9.4/json?</a></td><td><p></p><ul><li>New minor version Transcription models</li></ul></td></tr><tr><td>9.2</td><td><a href="https://api.speechace.co/api/scoring/text/v9.2/json?">https://api.speechace.co/api/scoring/text/v9.2/json?</a></td><td>New minor version scoring models</td></tr><tr><td>9</td><td><a href="https://api.speechace.co/api/scoring/text/v9/json?">https://api.speechace.co/api/scoring/text/v9/json?</a></td><td>current latest production major version</td></tr></tbody></table>
