# Database Integration Guide

JWeb is built on Spring Boot, which means you can use any database integration that works with Spring. This guide covers the most common approaches.

## Table of Contents

1. [Auto-Configuration (Zero-Config)](#auto-configuration-zero-config)
2. [Spring Data JPA (Recommended)](#spring-data-jpa-recommended)
3. [JdbcTemplate](#jdbctemplate)
4. [Using with JWeb Routes](#using-with-jweb-routes)
5. [Connection Pooling](#connection-pooling)
6. [Example: Complete CRUD Application](#example-complete-crud-application)

---

## Auto-Configuration (Zero-Config)

JWeb includes built-in JPA auto-configuration. For most applications, you can simply create entities and repositories - no configuration needed!

### Zero-Config Development

For development, JWeb automatically configures an H2 in-memory database:

```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    // getters, setters...
}

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

@Component
public class UserRoutes implements JWebRoutes {
    private final UserRepository users;

    public UserRoutes(UserRepository users) {
        this.users = users;
    }

    @Override
    public void configure(JWeb app) {
        app.get("/users", req -> {
            var allUsers = users.findAll();
            return ul(allUsers.stream()
                .map(u -> li(u.getName()))
                .toArray(Element[]::new));
        });
    }
}
```

That's it! No database configuration needed for development.

### H2 Console

Access the H2 web console at: `http://localhost:8081/h2-console`
- **JDBC URL:** `jdbc:h2:mem:jwebdb`
- **Username:** `sa`
- **Password:** (empty)

### Production Configuration

For production, add your database driver and configure in `application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret

# Or MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret
```

Don't forget to add the driver dependency:

```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Or MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### JWeb Data Properties

| Property | Default | Description |
|----------|---------|-------------|
| `jweb.data.enabled` | `true` | Enable/disable auto-configuration |
| `jweb.data.show-sql` | `false` | Log SQL statements |
| `jweb.data.ddl-auto` | `update` | Schema generation (none, validate, update, create, create-drop) |
| `jweb.data.h2-console` | `true` | Enable H2 web console |

### JWebRepository Base Interface

JWeb provides an optional `JWebRepository` interface with convenience methods:

```java
public interface UserRepository extends JWebRepository<User, Long> {
    // Inherits all JpaRepository methods plus:
    // - getById(id) - throws if not found
    // - hasAny() - check if entities exist
    // - findFirst() - get first entity

    Optional<User> findByEmail(String email);
}
```

### Disabling Auto-Configuration

If you prefer manual configuration:

```properties
jweb.data.enabled=false
```

---

## Spring Data JPA (Recommended)

Spring Data JPA provides a high-level abstraction over JPA/Hibernate with automatic repository implementations.

### 1. Add Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Database driver (choose one) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Or for H2 (development) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Or for MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Configure Database Connection

In `application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update

# Or H2 (in-memory for development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### 3. Create an Entity

```java
package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### 4. Create a Repository

```java
package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data automatically implements these methods
    Optional<User> findByEmail(String email);

    List<User> findByNameContainingIgnoreCase(String name);

    boolean existsByEmail(String email);

    // Custom queries
    @Query("SELECT u FROM User u WHERE u.createdAt > :since")
    List<User> findRecentUsers(LocalDateTime since);
}
```

### 5. Create a Service

```java
package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User create(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(new User(name, email));
    }

    @Transactional
    public User update(Long id, String name, String email) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
```

---

## JdbcTemplate

For simpler use cases or when you need more control over SQL.

### 1. Add Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

### 2. Use JdbcTemplate

```java
package com.example.repository;

import com.example.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class UserJdbcRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    public UserJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", userMapper);
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbc.query(
            "SELECT * FROM users WHERE id = ?",
            userMapper,
            id
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User save(User user) {
        if (user.getId() == null) {
            jdbc.update(
                "INSERT INTO users (name, email) VALUES (?, ?)",
                user.getName(), user.getEmail()
            );
        } else {
            jdbc.update(
                "UPDATE users SET name = ?, email = ? WHERE id = ?",
                user.getName(), user.getEmail(), user.getId()
            );
        }
        return user;
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }
}
```

---

## Using with JWeb Routes

Here's how to integrate database services with JWeb routing.

### Inject Services into Your App

```java
package com.example;

import com.example.service.UserService;
import com.osmig.Jweb.framework.JWeb;
import org.springframework.stereotype.Component;

import static com.osmig.Jweb.framework.core.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;

@Component
public class MyApp {

    private final JWeb app;
    private final UserService userService;

    public MyApp(JWeb app, UserService userService) {
        this.app = app;
        this.userService = userService;
        setupRoutes();
    }

    private void setupRoutes() {
        // List all users
        app.get("/users", req -> {
            var users = userService.findAll();
            return div(
                h1("Users"),
                ul(
                    users.stream()
                        .map(u -> li(u.getName() + " - " + u.getEmail()))
                        .toArray(Element[]::new)
                )
            );
        });

        // Get single user
        app.get("/users/:id", req -> {
            Long id = req.paramLong("id");
            return userService.findById(id)
                .map(user -> div(
                    h1(user.getName()),
                    p("Email: " + user.getEmail())
                ))
                .orElse(div(h1("User not found")));
        });

        // Create user form
        app.get("/users/new", req -> {
            return form(attrs().action("/users").method("post"),
                label("Name:"),
                input(attrs().type("text").name("name").required(true)),
                label("Email:"),
                input(attrs().type("email").name("email").required(true)),
                button(attrs().type("submit"), text("Create"))
            );
        });

        // Handle form submission
        app.post("/users", req -> {
            String name = req.formParam("name");
            String email = req.formParam("email");

            try {
                User user = userService.create(name, email);
                return Response.redirect("/users/" + user.getId());
            } catch (IllegalArgumentException e) {
                return div(
                    p(attrs().style(color("red")), text(e.getMessage())),
                    a(attrs().href("/users/new"), text("Try again"))
                );
            }
        });

        // Delete user
        app.post("/users/:id/delete", req -> {
            Long id = req.paramLong("id");
            userService.delete(id);
            return Response.redirect("/users");
        });
    }
}
```

---

## Connection Pooling

Spring Boot uses HikariCP by default. Configure it in `application.properties`:

```properties
# Pool size
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Connection timeout
spring.datasource.hikari.connection-timeout=30000

# Idle timeout
spring.datasource.hikari.idle-timeout=600000

# Max lifetime
spring.datasource.hikari.max-lifetime=1800000
```

---

## Example: Complete CRUD Application

Here's a complete example combining all the pieces:

### Project Structure

```
src/main/java/com/example/
├── Application.java
├── model/
│   └── Task.java
├── repository/
│   └── TaskRepository.java
├── service/
│   └── TaskService.java
└── app/
    └── TaskApp.java
```

### Task.java

```java
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors, getters, setters...
}
```

### TaskRepository.java

```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCompletedOrderByCreatedAtDesc(boolean completed);
    List<Task> findAllByOrderByCreatedAtDesc();
}
```

### TaskService.java

```java
@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<Task> findPending() {
        return repository.findByCompletedOrderByCreatedAtDesc(false);
    }

    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Task create(String title, String description) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        return repository.save(task);
    }

    @Transactional
    public Task toggleComplete(Long id) {
        Task task = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setCompleted(!task.isCompleted());
        return repository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

### TaskApp.java

```java
@Component
public class TaskApp {

    private final JWeb app;
    private final TaskService taskService;

    public TaskApp(JWeb app, TaskService taskService) {
        this.app = app;
        this.taskService = taskService;
        setupRoutes();
    }

    private void setupRoutes() {
        app.get("/", req -> {
            var tasks = taskService.findAll();
            return page("Tasks",
                div(attrs().className("container"),
                    h1("My Tasks"),

                    // Add task form
                    form(attrs().action("/tasks").method("post").className("add-form"),
                        input(attrs().type("text").name("title").placeholder("New task...").required(true)),
                        button(attrs().type("submit"), text("Add"))
                    ),

                    // Task list
                    ul(attrs().className("task-list"),
                        tasks.stream()
                            .map(this::renderTask)
                            .toArray(Element[]::new)
                    )
                )
            );
        });

        app.post("/tasks", req -> {
            String title = req.formParam("title");
            String description = req.formParam("description");
            taskService.create(title, description);
            return Response.redirect("/");
        });

        app.post("/tasks/:id/toggle", req -> {
            taskService.toggleComplete(req.paramLong("id"));
            return Response.redirect("/");
        });

        app.post("/tasks/:id/delete", req -> {
            taskService.delete(req.paramLong("id"));
            return Response.redirect("/");
        });
    }

    private Element renderTask(Task task) {
        String className = task.isCompleted() ? "task completed" : "task";
        return li(attrs().className(className),
            span(text(task.getTitle())),
            div(attrs().className("actions"),
                form(attrs().action("/tasks/" + task.getId() + "/toggle").method("post"),
                    button(attrs().type("submit"),
                        text(task.isCompleted() ? "Undo" : "Done"))
                ),
                form(attrs().action("/tasks/" + task.getId() + "/delete").method("post"),
                    button(attrs().type("submit").className("delete"), text("Delete"))
                )
            )
        );
    }

    private Element page(String title, Element... content) {
        return html(
            head(
                Elements.title(title),
                style(css(
                    rule(".container", maxWidth("800px"), margin("0 auto"), padding("20px")),
                    rule(".task-list", listStyle("none"), padding("0")),
                    rule(".task", display("flex"), justifyContent("space-between"),
                         padding("10px"), borderBottom("1px solid #eee")),
                    rule(".task.completed span", textDecoration("line-through"),
                         color("#999")),
                    rule(".add-form", display("flex"), gap("10px"), marginBottom("20px")),
                    rule(".add-form input", flex("1"), padding("10px")),
                    rule(".actions", display("flex"), gap("5px")),
                    rule(".delete", background("#dc3545"), color("white"))
                ))
            ),
            body(content)
        );
    }
}
```

---

## Tips

1. **Use `@Transactional`** for write operations to ensure data consistency.

2. **Handle exceptions** gracefully - wrap repository calls in try-catch when needed.

3. **Use DTOs** for complex responses instead of exposing entities directly.

4. **Consider pagination** for large datasets:
   ```java
   Page<User> users = userRepository.findAll(PageRequest.of(0, 20));
   ```

5. **Use database migrations** with Flyway or Liquibase for schema management:
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   ```

6. **Profile your queries** by enabling SQL logging in development:
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   logging.level.org.hibernate.SQL=DEBUG
   ```
