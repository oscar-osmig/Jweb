# HTTP Client (Fetch)

JWeb provides a fluent HTTP client for consuming external APIs.

## Basic Usage

```java
import com.osmig.Jweb.framework.http.Fetch;
import com.osmig.Jweb.framework.http.FetchResult;

// Simple GET request
FetchResult result = Fetch.get("https://api.example.com/users").send();
List<User> users = result.asList(User.class);
```

## HTTP Methods

```java
// GET
Fetch.get("https://api.example.com/users").send();

// POST
Fetch.post("https://api.example.com/users").json(user).send();

// PUT
Fetch.put("https://api.example.com/users/1").json(user).send();

// PATCH
Fetch.patch("https://api.example.com/users/1").json(updates).send();

// DELETE
Fetch.delete("https://api.example.com/users/1").send();
```

## Headers

```java
FetchResult result = Fetch.get("https://api.example.com/data")
    .header("Authorization", "Bearer token123")
    .header("Accept", "application/json")
    .header("X-Custom-Header", "value")
    .send();
```

### Common Header Shortcuts

```java
// Bearer token authentication
Fetch.get(url).bearer("your-jwt-token").send();

// Basic authentication
Fetch.get(url).basicAuth("username", "password").send();

// Content-Type
Fetch.post(url).contentType("application/json").send();

// Accept header
Fetch.get(url).accept("application/json").send();
```

## Request Body

### JSON Body

```java
User user = new User("John", "john@example.com");

FetchResult result = Fetch.post("https://api.example.com/users")
    .json(user)  // Automatically serializes to JSON
    .send();
```

### Form Data

```java
FetchResult result = Fetch.post("https://api.example.com/login")
    .form(Map.of(
        "username", "john",
        "password", "secret"
    ))
    .send();
```

### Raw Body

```java
FetchResult result = Fetch.post("https://api.example.com/data")
    .body("{\"key\": \"value\"}")
    .contentType("application/json")
    .send();
```

## Timeout

```java
import java.time.Duration;

FetchResult result = Fetch.get("https://api.example.com/slow")
    .timeout(Duration.ofSeconds(60))  // 60 second timeout
    .send();
```

## Response Handling

### FetchResult Methods

```java
FetchResult result = Fetch.get(url).send();

// Status code
int status = result.status();

// Status checks
result.isOk();          // 2xx
result.isError();       // 4xx or 5xx
result.isClientError(); // 4xx
result.isServerError(); // 5xx

// Raw body
String body = result.body();

// Headers
Map<String, List<String>> headers = result.headers();
Optional<String> contentType = result.header("Content-Type");
```

### Parse JSON Response

```java
// Parse as object
User user = result.as(User.class);

// Parse as list
List<User> users = result.asList(User.class);

// Parse as map
Map<String, Object> data = result.asMap();

// Safe parsing (returns Optional)
Optional<User> user = result.tryAs(User.class);
```

## Error Handling

```java
FetchResult result = Fetch.get("https://api.example.com/users/123").send();

if (result.isOk()) {
    User user = result.as(User.class);
    // Process user...
} else if (result.status() == 404) {
    // User not found
} else if (result.isClientError()) {
    // Handle 4xx error
    ErrorResponse error = result.as(ErrorResponse.class);
} else if (result.isServerError()) {
    // Handle 5xx error
}
```

### Exception Handling

```java
try {
    FetchResult result = Fetch.get("https://api.example.com/data").send();
} catch (Fetch.FetchException e) {
    // Network error, timeout, invalid URL, etc.
    System.err.println("Request failed: " + e.getMessage());
}
```

## Async Requests

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<FetchResult> future = Fetch.get("https://api.example.com/users")
    .sendAsync();

future.thenAccept(result -> {
    if (result.isOk()) {
        List<User> users = result.asList(User.class);
        // Process users...
    }
});
```

### Multiple Async Requests

```java
CompletableFuture<FetchResult> users = Fetch.get(baseUrl + "/users").sendAsync();
CompletableFuture<FetchResult> posts = Fetch.get(baseUrl + "/posts").sendAsync();

CompletableFuture.allOf(users, posts).thenRun(() -> {
    List<User> userList = users.join().asList(User.class);
    List<Post> postList = posts.join().asList(Post.class);
    // Combine results...
});
```

## Complete Examples

### REST API Client

```java
public class UserApiClient {
    private final String baseUrl = "https://api.example.com";
    private final String token;

    public UserApiClient(String token) {
        this.token = token;
    }

    public List<User> getUsers() {
        return Fetch.get(baseUrl + "/users")
            .bearer(token)
            .send()
            .asList(User.class);
    }

    public User getUser(String id) {
        FetchResult result = Fetch.get(baseUrl + "/users/" + id)
            .bearer(token)
            .send();

        if (result.status() == 404) {
            return null;
        }
        return result.as(User.class);
    }

    public User createUser(User user) {
        return Fetch.post(baseUrl + "/users")
            .bearer(token)
            .json(user)
            .send()
            .as(User.class);
    }

    public User updateUser(String id, User user) {
        return Fetch.put(baseUrl + "/users/" + id)
            .bearer(token)
            .json(user)
            .send()
            .as(User.class);
    }

    public void deleteUser(String id) {
        Fetch.delete(baseUrl + "/users/" + id)
            .bearer(token)
            .send();
    }
}
```

### Using in Routes

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/weather", req -> {
            String city = req.query("city", "London");

            FetchResult result = Fetch.get("https://api.weather.com/v1/current")
                .header("API-Key", System.getenv("WEATHER_API_KEY"))
                .send();

            if (result.isOk()) {
                return new WeatherPage(result.asMap());
            }
            return new ErrorPage("Could not fetch weather data");
        });
    }
}
```
