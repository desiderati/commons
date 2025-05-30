# User Data Resolution

This document provides detailed information about the user data resolution functionality provided by
the `common-web-security` module.

## Overview

The `common-web-security` module provides a convenient way to access authenticated user data in your controllers,
services, and GraphQL resolvers. This functionality allows you to easily retrieve user information
without having to manually extract it from the security context.

## Key Features

- Automatic resolution of authenticated user data in REST controllers
- Integration with GraphQL for resolving user data in GraphQL resolvers
- Customizable user data retrieval
- Support for reactive programming

## Core Components

### UserData Interface

The `UserData` interface is a marker interface that your user data classes should implement:

```kotlin
interface UserData
```

This interface doesn't define any methods, allowing you to create your own user data structure
according to your application's needs.

### UserDataRetriever Interface

The `UserDataRetriever` interface defines how to retrieve user data by username:

```kotlin
interface UserDataRetriever<U : UserData?> {
    fun findUserByUsername(username: String): U?
}
```

You need to implement this interface to provide the logic for retrieving user data from your data source.

### AuthenticatedUser Annotation

The `@AuthenticatedUser` annotation is used to mark method parameters that should be resolved
to the authenticated user's data:

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AuthenticatedUser
```

### AuthenticatedUsernameGetter Interface

The `AuthenticatedUsernameGetter` interface defines how to retrieve the authenticated username from a request:

```kotlin
interface AuthenticatedUsernameGetter {
    fun getAuthenticatedUsernameFromRequest(): String?
    fun getAuthenticatedUsernameFromReactiveRequest(): Mono<String>
}
```

The module provides a default implementation of this interface that extracts the username
from the Spring Security context.

## Implementation

### Creating a User Data Class

First, create a class that implements the `UserData` interface:

```java
public class MyUserData implements UserData {

    private String username;
    private String email;
    private List<String> roles;

    // Getters and setters
}
```

### Implementing a User Data Retriever

Create a bean that implements the `UserDataRetriever` interface:

```java
@Component
public class MyUserDataRetriever implements UserDataRetriever<MyUserData> {

    private final UserRepository userRepository;

    @Autowired
    public MyUserDataRetriever(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public MyUserData findUserByUsername(String username) {
        // Retrieve user data from your data source
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        MyUserData userData = new MyUserData();
        userData.setUsername(user.getUsername());
        userData.setEmail(user.getEmail());
        userData.setRoles(user.getRoles());
        return userData;
    }
}
```

## Usage

### In REST Controllers

In your REST controllers, you can use the `@AuthenticatedUser` annotation to inject the authenticated user's data:

```java
import io.herd.common.web.rest.exception.UnauthorizedRestApiException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public MyUserData getCurrentUser(@AuthenticatedUser MyUserData userData) {
        if (userData == null) {
            throw new UnauthorizedRestApiException("User not logged!");
        }
        return userData;
    }

    @GetMapping("/profile")
    public UserProfile getUserProfile(@AuthenticatedUser MyUserData userData) {
        if (userData == null) {
            throw new UnauthorizedRestApiException("User not logged!");
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(userData.getUsername());
        profile.setEmail(userData.getEmail());
        // Set other profile properties

        return profile;
    }
}
```

### In GraphQL Resolvers

The module also provides integration with Spring GraphQL for resolving authenticated user data in GraphQL resolvers:

```java
@Controller
public class UserGraphQLController {

    @QueryMapping
    public MyUserData me(@AuthenticatedUser MyUserData userData) {
        return userData;
    }

    @QueryMapping
    public UserProfile userProfile(@AuthenticatedUser MyUserData userData) {
        if (userData == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(userData.getUsername());
        profile.setEmail(userData.getEmail());
        // Set other profile properties

        return profile;
    }
}
```

### In Services

You can also use the `@AuthenticatedUser` annotation in service methods:

```java
@Service
public class UserService {

    public UserProfile getUserProfile(@AuthenticatedUser MyUserData userData) {
        if (userData == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(userData.getUsername());
        profile.setEmail(userData.getEmail());
        // Set other profile properties

        return profile;
    }
}
```

## Advanced Usage

### Custom AuthenticatedUsernameGetter

If you need to customize how the authenticated username is retrieved, you can provide your own implementation
of the `AuthenticatedUsernameGetter` interface:

```java
@Component
public class CustomAuthenticatedUsernameGetter implements AuthenticatedUsernameGetter {

    @Override
    public String getAuthenticatedUsernameFromRequest() {
        // Custom logic to retrieve the authenticated username.
        // For example, from a custom header or a different security context.
        return customLogic();
    }

    @Override
    public Mono<String> getAuthenticatedUsernameFromReactiveRequest() {
        // Custom logic for reactive programming.
        return Mono.just(customLogic());
    }

    private String customLogic() {
        // Your custom logic here!
    }
}
```

### Reactive Support

The module provides support for reactive programming through the `getAuthenticatedUsernameFromReactiveRequest()` method
in the `AuthenticatedUsernameGetter` interface. This method returns a `Mono<String>` that can be used
in reactive applications.

## Error Handling

When using the `@AuthenticatedUser` annotation, you should always check if the resolved user data is `null`.
This can happen in the following cases:

1. The user is not authenticated.
2. The authenticated username could not be retrieved.
3. The user data could not be found for the authenticated username.

Here's an example of proper error handling:

```java
@GetMapping("/profile")
public UserProfile getUserProfile(@AuthenticatedUser MyUserData userData) {

    if (userData == null) {
        throw new UnauthorizedRestApiException("User not logged!");
    }

    // Proceed with the authenticated user data
    UserProfile profile = new UserProfile();
    profile.setUsername(userData.getUsername());
    // Set other profile properties

    return profile;
}
```

## Performance Considerations

The user data resolution process involves retrieving the authenticated username from the security context
and then retrieving the user data from your data source.
This can impact performance if the user data retrieval is expensive.

To optimize performance, consider the following:

1. Cache user data to avoid repeated database queries.
2. Only include necessary information in your `UserData` implementation.
3. Use lazy loading for related entities if using JPA.

## Troubleshooting

### Common Issues

#### User Data Not Being Resolved

If the user data is not being resolved correctly, check the following:

1. Ensure that the user is properly authenticated.
2. Verify that your `UserDataRetriever` implementation is correctly retrieving the user data.
3. Check that the `@AuthenticatedUser` annotation is applied to the correct parameter.

#### NullPointerException When Accessing User Data

If you're getting a `NullPointerException` when accessing user data, ensure that you're checking
if the resolved user data is `null` before accessing its properties.

#### Bean Not Found Exceptions

If you're seeing bean not found exceptions related to `UserDataRetriever` or `AuthenticatedUsernameGetter`,
ensure that you have properly implemented and registered these beans in your application context.

## Examples

### Complete Example with Spring Data JPA

Here's a complete example, using Spring Data JPA for user data retrieval:

```java
// User entity
@Entity
public class User implements UserData {

    @Id
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    // Getters and setters
}

// User repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}

// User data retriever
@Component
public class JpaUserDataRetriever implements UserDataRetriever<User> {

    private final UserRepository userRepository;

    @Autowired
    public JpaUserDataRetriever(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        User user = new User();
        user.setUsername(user.getUsername());
        user.setEmail(user.getEmail());
        user.setFirstName(user.getFirstName());
        user.setLastName(user.getLastName());
        user.setRoles(user.getRoles());
        return user;
    }
}

// Controller using user data
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticatedUser User user) {
        if (user == null) {
            throw new UnauthorizedRestApiException("User not logged!");
        }
        return user;
    }
}
```

## Further Reading

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [Spring GraphQL Documentation](https://docs.spring.io/spring-graphql/docs/current/reference/html/)
