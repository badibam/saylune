> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](faqs.md).

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
