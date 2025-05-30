# Common Web Security Module Documentation

This document provides comprehensive documentation for the `common-web-security` module,
which offers security features for Spring-based applications.

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Configuration](#configuration)
   - [OAuth2 Integration](#oauth2-integration)
   - [Security Filter Chain](#security-filter-chain)
4. [Usage](#usage)
   - [Method Security](#method-security)
5. [API Reference](#api-reference)
6. [Troubleshooting](#troubleshooting)

## Detailed Documentation

For more detailed information about specific features, please refer to the following documents:

- [JWT Authentication](docs/jwt-authentication.md) — Detailed information about JWT authentication
- [Sign Request](docs/sign-request.md) — Information about API request signing
- [User Data Resolution](docs/user-data-resolver.md) — Guide to accessing authenticated user data

## Overview

The `common-web-security` module provides a comprehensive security solution for Spring-based applications.
It is designed to be used with minimal configuration through conventions, allowing applications to easily
integrate security features.

This module includes:
- JWT-based authentication and authorization
- OAuth2 integration
- User data resolution for controllers and GraphQL resolvers
- Method-level security
- Request signing capabilities

## Installation

To use the `common-web-security` module in your Spring application, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.herd.common</groupId>
    <artifactId>common-web-security</artifactId>
    <version>${common-web-security.version}</version>
</dependency>
```

Replace `${common-web-security.version}` with the appropriate version number.

## Configuration

The module uses Spring Boot's auto-configuration mechanism to automatically configure security features
based on the presence of certain dependencies and properties.

### OAuth2 Integration

The module provides integration with OAuth2 for authentication.
To enable OAuth2 integration, add the following dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Configure OAuth2 providers in your `application.properties` or `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          login-client:
            client-id: login-client
            client-secret: { your-client-secret }
            client-authentication-method: client_secret_basic
            scope: openid, profile
            redirect-uri: "{baseUrl}/oauth2/login/{registrationId}"
            authorization-grant-type: authorization_code
        provider:
          login-client:
            authorization-uri: http://localhost:9091/oauth2/authorize
            token-uri: http://localhost:9091/oauth2/token
            jwk-set-uri: http://localhost:9091/oauth2/jwks
```

### Security Filter Chain

The module automatically configures a security filter chain with sensible defaults.
You can customize it by providing your own `SecurityFilterChain` bean:

```java
@Bean
public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
    // Your custom configuration
    return http.build();
}
```

## Usage

### Method Security

The module enables method-level security using Spring Security's annotations:

```java
@Service
public class UserService {

    @PreAuthorize("hasRole('ADMIN')")
    public void adminOnlyMethod() {
        // Only accessible to users with an ADMIN role.
    }

    @PreAuthorize("authentication.name == #username")
    public void userSpecificMethod(String username) {
        // Only accessible to the specified user.
    }
}
```

## API Reference

### Key Interfaces

- `UserData`: Marker interface for user data objects
- `UserDataRetriever`: Interface for retrieving user data by username
- `AuthenticatedUsernameGetter`: Interface for retrieving the authenticated username from a request

### Key Annotations

- `@AuthenticatedUser`: Annotation for injecting authenticated user data into controller methods and GraphQL resolvers

### Key Configuration Classes

- `WebSecurityAutoConfiguration`: Main configuration class for web security
- `JwtAuthenticationConfiguration`: Configuration for JWT authentication

## Troubleshooting

### Common Issues

#### JWT Authentication Not Working

1. Verify that your JWT keys are correctly formatted.
2. Check that the `jwt.pub` and `jwt.key` files are in the correct location.
3. Ensure that the JWT token is being sent in the Authorization header with the "Bearer" prefix.

#### CORS Issues

1. Verify that your CORS configuration includes all necessary origins, methods, and headers.
2. Check browse's console for specific CORS error messages.
3. Ensure that credentials are properly configured if you're using cookies.

#### OAuth2 Integration Issues

1. Verify that your client ID and secret are correct.
2. Check that the redirect URI is properly configured.
3. Ensure that the OAuth2 provider is properly configured.

For more specific issues, please refer to the Spring Security documentation or open an issue in the project repository.
