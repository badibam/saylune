> For the complete documentation index, see [llms.txt](https://api-docs.speechace.com/llms.txt). Markdown versions of documentation pages are available by appending `.md` to page URLs; this page is available as [Markdown](authentication.md).

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
