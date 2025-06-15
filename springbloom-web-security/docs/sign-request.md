# Request Signing

This document provides detailed information about the request signing functionality provided
by the `springbloom-web-security` module.

## Overview

Request signing is a security mechanism that allows API clients to sign their requests using a shared secret,
ensuring that the requests are authentic and have not been tampered with.
The `springbloom-web-security` module provides a comprehensive implementation of request signing
for Spring-based applications.

> Try not to use APIs that receive medium or large files. These requests are fully loaded into memory
> to verify their signature, which can slow down the system and affect overall performance.

## Key Features

- API key-based authentication
- Request signature validation
- Protection against request tampering
- Configurable signature header name
- Integration with Spring Security

## How It Works

1. The client generates a signature using a shared secret (API Secret)
2. The client includes the signature in the request header
3. The server validates the signature using the same shared secret
4. If the signature is valid, the request is processed; otherwise, it is rejected

## Client Configuration

### Basic Configuration

To enable request signing, add the following properties to your `application.properties` or `application.yml` file:

```properties
# Defines the client id.
spring.web.security.sign-request.client.id=91a3404a-d71a-446a-9722-d2acf2ad4d16

# Defines the secret key associated with the client id, which will be used to sign the request.
spring.web.security.sign-request.client.secret-key=!A%D*G-KaPdSgVkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5
```

### Configuration Properties

| Property                         | Description                                          | Default Value |
|----------------------------------|------------------------------------------------------|---------------|
| `sign-request.client.id`         | The API ID used to identify the client               | Required      |
| `sign-request.client.secret-key` | The shared secret used to sign and validate requests | Required      |

### Generating API Keys

For secure API authentication, you need to generate two key parts:

**API ID:** A unique identifier for the client application.
You can generate a UUID using a tool like [UUID Generator](https://www.uuidgenerator.net/).

**API Secret:** A secure random key used to sign requests and verify signatures.
You can generate a 512-bit key using a tool like [Security Key Generator](https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx).

### Generating a Signature

To generate a signature for a request, the client needs to:

1. Concatenate the request method, URL path, and request body (if any).
   > Try not to use APIs that receive medium or large files. These requests are fully loaded into memory
   > to verify their signature, which can slow down the system and affect overall performance.
2. Sign the concatenated string using the Secret Key.
3. Include the signature in the request header.

### Using with RestTemplate

Here's one of the possible examples of how to use the request signer with Spring's RestTemplate:

```java
import dev.springbloom.web.security.auth.sign.SignRequestService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SignedRestTemplate {

    private final RestTemplate restTemplate;

    public SignedRestTemplate(SignRequestService signRequestAuthorizationService) {
        this.restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor signHttpRequestInterceptor =
            (request, body, execution) ->
                execution.execute(signRequestAuthorizationService.sign(request, body), body);
        this.restTemplate.getInterceptors().add(signHttpRequestInterceptor);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, byte[] body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, entity, responseType);
    }
}
```

## Server Configuration

The `springbloom-web-security` module automatically configures a `SignRequestAuthorizationFilter`
when request signing authorization is enabled. This filter validates the signature of incoming requests.

### Basic Configuration

To enable request signing authorization, add the following properties to your `application.properties`
or `application.yml` file:

```properties
# It defines if Sign Request Authorization is enabled or not.
spring.web.security.sign-request.authorization.enabled=true

```

After that, create the file `sign-request-authorized-clients.json` on directory `src/main/resources`,
and define all client's properties with their roles:

```yaml
[
  {
    "id": "91a3404a-d71a-446a-9722-d2acf2ad4d16",
    "secretKey": "!A%D*G-KaPdSgVkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5",
    "roles": [
      "USER_CLIENT"
    ]
  }
]

```

## Security Considerations

### API Secret Protection

The API Secret should be treated as a sensitive credential and protected accordingly:

1. Never expose the API Secret in client-side code.
2. Store the API Secret securely, preferably in a secure vault or environment variable.
3. Rotate the API Secret periodically.

## Security Improvements

### Request Replay Protection

The basic request signing implementation does not protect against replay attacks. To add replay protection:

1. Include a timestamp in the request.
2. Include the timestamp in the signature calculation.
3. Reject requests with timestamps outside a certain window (e.g., 5 minutes).

## Troubleshooting

### Common Issues

#### Invalid Signature

If you're seeing "Invalid signature" errors, check that:

1. The API Secret is the same on both client and server.
2. The signature calculation is consistent between client and server.
3. The request body is included in the signature calculation if it's not empty.
4. URL encoding/decoding is handled consistently.

#### Missing API ID

If you're seeing "Missing Client ID" errors, check that:

1. The client is including the Client ID header in the request.
2. The header name is configured correctly on both client and server.

#### Request Signing Disabled

If request signing validation is not working, check that:

1. The `spring.web.security.sign-request.authorization.enabled` property is set to `true`.
2. The `SignRequestAuthorizationFilter` is properly registered in the security filter chain.

## Further Reading

- [HMAC Authentication](https://en.wikipedia.org/wiki/HMAC)
- [API Key Authentication Best Practices](https://cloud.google.com/endpoints/docs/openapi/security-authentication-api-keys)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
