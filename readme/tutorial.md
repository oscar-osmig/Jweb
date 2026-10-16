[← Back to README](./../README.md)

# Your first JWeb app

A todo app in about fifteen minutes: a page with a layout, a list that updates without a
reload, a form the server validates, a JSON endpoint, and a test for each. Everything is
Java. Nothing here writes HTML, CSS or JavaScript, and every code block on this page is
compiled by the test suite, so it is current.

If you only read one guide first, read this one; the others are references.

## 1. Create the project

The CLI ships inside the framework jar. With the jar on your path:

```bash
alias jweb='java -cp Jweb.jar com.osmig.Jweb.framework.cli.JWebCli'
jweb new todos          # pom.xml, Application, Routes, HomePage, MainLayout, a database
cd todos
mvn spring-boot:run     # http://localhost:8085
```

The project comes with a database: H2 as a file under `./data`, `spring.datasource.url`
pointing at it, and `src/main/resources/db/migration/` for the SQL files `jweb.Migrations`
applies at startup. Nothing to install; swap the driver and the URL for PostgreSQL when the
app leaves your machine.

Or by hand. The dependency comes from JitPack (`vX.Y.Z` is a tag from the
[changelog](../CHANGELOG.md)); JWeb brings Spring Boot's web and websocket starters with it:

```xml
<repositories>
  <repository><id>jitpack.io</id><url>https://jitpack.io</url></repository>
</repositories>
<dependency>
  <groupId>com.github.oscar-osmig</groupId>
  <artifactId>Jweb</artifactId>
  <version>v3.0.3</version>
</dependency>
```

The entry point is a Spring Boot application with the framework's wiring folded in:

```java
import jweb.JWebApplication;
import org.springframework.boot.SpringApplication;

@JWebApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 2. A page and its layout

A page is a `Template`: `render()` is the markup, `styles()` the CSS rules it needs, and
`pageTitle()` the title. Class names are handles — declared once, used by the markup and
the rules, so a typo is a compile error, not a silent miss.

```java
import jweb.Cls;
import jweb.Element;
import jweb.Template;
import jweb.css.Stylesheet;

public class HomePage implements Template {

    static final Cls HERO = cls("hero");
    static final Cls LEAD = cls("hero-lead");

    @Override
    public String pageTitle() {
        return "Todos";
    }

    @Override
    public Element render() {
        return section(HERO,
            h1("Todos"),
            p(LEAD, "A list, a form, and an API — all of it Java."),
            a(href("/todos"), "Open the list"));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(HERO, style().textAlign(center).padding(rem(4), rem(1)))
            .rule(LEAD, style().fontSize(rem(1.25)).color(hex("#6b7280")));
    }
}
```

A layout is a `Template` whose constructor takes the title and the page: the framework
looks for `(String title, Element content)`. The page's `pageTitle()` arrives as the title;
its `styles()` and `scripts()` are collected into the document along with the layout's own.

```java
import jweb.Cls;
import jweb.Element;
import jweb.Template;
import jweb.css.Selector;
import jweb.css.Stylesheet;

public class MainLayout implements Template {

    static final Cls HEADER = cls("site-header");
    static final Cls MAIN = cls("site-main");

    private final String title;
    private final Element content;

    public MainLayout(String title, Element content) {
        this.title = title != null ? title : "Todos";
        this.content = content;
    }

    @Override
    public Element render() {
        return html(attrs().lang("en"),
            head(metaCharset(), meta("viewport", "width=device-width, initial-scale=1.0"), title(title)),
            body(
                header(HEADER, a(href("/"), "Todos")),
                main(MAIN, content)));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(Selector.type("body"), style().margin(zero).fontFamily(systemUi, sansSerif))
            .rule(HEADER, style().padding(rem(1), rem(2)).borderBottom(px(1), solid, hex("#e5e7eb")))
            .rule(MAIN, style().maxWidth(px(720)).margin(zero, auto).padding(rem(2)));
    }
}
```

Routes live in one `JWebRoutes` bean. Pages go in the page table: they get the layout, the
title hooks and the client runtime without any further wiring. Call `layout()` before
`pages()` — the registry captures the default layout as pages register.

```java
import jweb.JWeb;
import jweb.JWebRoutes;
import jweb.Middlewares;

@Component
public class Routes implements JWebRoutes {

    @Override
    public void configure(JWeb app) {
        app.use(Middlewares.recommended());          // security headers, request ids, compression, CSRF

        app.layout(MainLayout.class)
           .pages("/", HomePage.class,
                  "/todos", TodosPage.class);
    }

    // Stubs so this block stands on its own; the real classes are above and below.
    static class MainLayout implements Template {
        MainLayout(String title, Element content) {}
        public Element render() { return div(); }
    }
    static class HomePage implements Template { public Element render() { return div(); } }
    static class TodosPage implements Template { public Element render() { return div(); } }
}
```

Run it and open `http://localhost:8085`. The page is server-rendered HTML; the one script
on it is the framework's runtime, which the next step needs.

## 3. A list that updates without a reload

State is declared in `render()` with `useState`. A `live(...)` region re-renders on the
server whenever a state it reads changes, and the runtime morphs the new markup into the
page over the WebSocket. Handlers such as `onClick(e -> …)` run on the server too. No
JavaScript is written for any of it.

```java
import jweb.Cls;
import jweb.Element;
import jweb.Template;
import jweb.css.Stylesheet;
import jweb.state.State;
import static jweb.State.*;

public class TodosPage implements Template {

    record Todo(String text, boolean done) {
        Todo toggled() { return new Todo(text, !done); }
    }

    static final Cls LIST = cls("todo-list");
    static final Cls DONE = cls("done");

    private State<List<Todo>> todos;

    @Override
    public String pageTitle() {
        return "Your todos";
    }

    @Override
    public Element render() {
        todos = useState(new ArrayList<>(List.of(new Todo("Read the tutorial", false))));

        return div(
            h1("Your todos"),
            form(onSubmit(e -> {
                    e.preventDefault();
                    String text = e.formData().getOrDefault("text", "").trim();
                    if (!text.isEmpty()) todos.mutate(list -> list.add(new Todo(text, false)));
                }),
                input(id("new-todo"), type("text"), name("text"),
                      attrs().placeholder("What needs doing?").autocomplete("off")),
                button(type("submit"), "Add")),
            live(todos, list -> ul(LIST, each(list, todo ->
                li(when(todo.done(), DONE),
                   onClick(e -> todos.mutate(l -> l.replaceAll(t -> t == todo ? t.toggled() : t))),
                   todo.text())))));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(LIST, style().listStyle(none).padding(zero))
            .rule(LIST.descendant(Selector.type("li")), style().padding(rem(0.5), zero).cursor(pointer))
            .rule(DONE, style().textDecoration(lineThrough).color(hex("#9ca3af")));
    }
}
```

What happens on a click: the browser sends the handler's id over the socket, the server
runs the lambda in this page's render context, sees that `todos` changed, re-renders the
`live` region and sends it back as a patch. The region's root carries `data-live`; the
runtime replaces only that subtree, so focus and scroll position survive.

The state lives for the visit — a render context the server keeps for five idle minutes.
The next step keeps the todos in the database, so they survive the visit and are the same
for everyone. When a context expires, the page tells the user and offers a reload
([State & Realtime](./state-and-realtime.md#when-the-connection-drops-and-when-the-page-expires)).

## 4. A form the server validates, a table that keeps it

For data that must be checked and stored, a form is a record: the record declares the
fields and their rules, renders the form, and comes back typed. It also works with JavaScript
off. The same record is a row: `Sql.table("todos", Todo.class)` reads and writes it by
column name, and `id` is null until the row is inserted.

```java
import jweb.Form;

public record Todo(
    Long id,
    @Form.Required @Form.Length(min = 3, max = 80) String text,
    boolean done) {}
```

The table comes from a migration — a file under `src/main/resources/db/migration`, applied
once, at startup, in version order:

```sql
-- src/main/resources/db/migration/V1__create_todos.sql
create table todos (
    id bigint generated by default as identity primary key,
    text varchar(80) not null,
    done boolean not null default false
);
```

Render the form in a page and handle the POST in `Routes`. `Middlewares.recommended()`
includes CSRF protection, and the record form carries the token for you.

```java
import jweb.Form;
import jweb.Sql;

record Todo(Long id, @Form.Required @Form.Length(min = 3, max = 80) String text, boolean done) {}
Sql.Table<Todo> todos = Sql.table("todos", Todo.class);

// In a page: the rows, and the form — the id stays hidden
Element list = ul(each(todos.where("done = ? order by id", false), t -> li(t.text())));
Form<Todo> addForm = form(Todo.class)
    .action("/todos/add")
    .field("id", f -> f.type("hidden"))
    .field("text", f -> f.label("New todo").placeholder("What needs doing?"))
    .submit("Add");

// In Routes.configure(app):
app.post("/todos/add", req -> {
    Form.Bound<Todo> submitted = Form.bind(Todo.class, req);
    if (!submitted.ok()) {
        return form(Todo.class).action("/todos/add").field("id", f -> f.type("hidden")).errors(submitted).submit("Add");
    }
    todos.insert(submitted.value());                   // the copy that comes back carries the id
    return Response.redirect("/todos");
});
```

A submission that fails validation re-renders the form with the message beside the field
and the value the user typed; a good one is a row. The browser's own validation and the
server's come from the same annotations, so they cannot disagree.

All of this is what `jweb generate resource Todo text:string! done:boolean` writes: the
record, the migration, a list page, a form page and their routes. And
`Admin.mount(app, "/admin", todos).guard(Auth.requireRole("admin"))` is a back office for
the table — list, search, add, edit, delete — in one line.

## 5. A JSON endpoint

REST controllers are plain classes with the `jweb.api` annotations, under `/api/v…` (those
paths are reserved for them). Return values are serialized as JSON; a record parameter
annotated `@Body` is read from the request body.

```java
import jweb.api.REST;
import jweb.api.GET;
import jweb.api.POST;
import jweb.api.Body;

@REST("/api/v1/todos")
public class TodosApi {

    record Todo(String text, boolean done) {}
    record NewTodo(String text) {}

    private final List<Todo> todos = Collections.synchronizedList(new ArrayList<>());

    @GET
    public List<Todo> list() {
        return List.copyOf(todos);
    }

    @POST
    public Todo add(@Body NewTodo body) {
        Todo todo = new Todo(body.text(), false);
        todos.add(todo);
        return todo;
    }
}
```

`curl localhost:8085/api/v1/todos` answers `[]`; `OpenApi.scan(...)` can document the whole
API ([Backend](./backend.md#openapi-documentation)).

## 6. Test it without a browser

Page routes and handlers are tested in-process. `JWebTest.test` dispatches a request
through the middleware stack and the page table; `JWebTest.render` renders a page with its
event loop attached, so a test can click and submit and read the patched markup back.

```java
import jweb.JWebTest;
import jweb.MockRequest;
import jweb.RenderedPage;
import jweb.state.State;
import static jweb.State.*;

class TodosPage implements Template {
    State<List<String>> todos;
    public Element render() {
        todos = useState(new ArrayList<>());
        return div(
            form(id("add"), onSubmit(e -> { e.preventDefault(); todos.mutate(l -> l.add(e.formData().get("text"))); })),
            live(todos, list -> ul(each(list, t -> li(t)))));
    }
}

// The route answers
JWeb todoApp = JWeb.create().pages("/todos", TodosPage.class);
JWebTest.TestResult result = JWebTest.test(todoApp, MockRequest.get("/todos"));
JWebTest.assertContains(result.getBody(), "<ul");

// The handler runs and the region re-renders
try (RenderedPage page = JWebTest.render(new TodosPage())) {
    RenderedPage.Result r = page.submit("add", Map.of("text", "Write tests"));
    JWebTest.assertContains(page.html(), "<li>Write tests</li>");
}
```

A test that touches the table gets an in-memory database: connect `Sql` to H2 and run the
migrations, the same files the app runs at startup.

```java
import jweb.Migrations;
import jweb.Sql;

record Todo(Long id, String text, boolean done) {}

org.h2.jdbcx.JdbcDataSource h2 = new org.h2.jdbcx.JdbcDataSource();
h2.setURL("jdbc:h2:mem:todos;DB_CLOSE_DELAY=-1");
Sql.connect(h2);
Migrations.migrate(h2);                                   // classpath:db/migration

Sql.Table<Todo> todos = Sql.table("todos", Todo.class);
Todo saved = todos.insert(new Todo(null, "Write tests", false));
JWeb todoApp = JWeb.create();
JWebTest.TestResult posted = JWebTest.test(todoApp, MockRequest.post("/todos/add").formData(Map.of("text", "Ship it")));
```

The whole suite runs with `mvn test`, no browser required; H2 is already a test dependency
of a generated project.

## 7. Run it for real

```bash
jweb build              # packages the jar and writes a multi-stage Dockerfile
docker build -t todos . && docker run -p 8085:8085 todos
```

Three things to know before a deployment: render contexts (the state behind `useState` and
the handlers behind `onClick`) live in memory on the instance that rendered the page, so run
one instance or route `/jweb` with sticky sessions; set `SESSION_COOKIE_SECURE=true` once
the app is behind HTTPS; and point `spring.datasource.url` at a real database (the H2 file
under `./data` is for your machine — add the PostgreSQL driver and the migrations run there
unchanged). The rest of the production checklist is in
[Configuration](./configuration.md).

## Where to go next

- [HTML DSL](./html-dsl.md) — every element and attribute, forms, conditionals
- [CSS DSL](./css-dsl.md) — rules, units, colors, at-rules, themes
- [State & Realtime](./state-and-realtime.md) — `useState`, `live`, bindings, streaming SSR
- [Backend](./backend.md) — REST, OpenAPI, SQL with migrations, the admin, MongoDB, security, validation, uploads and file storage, mail
- [JavaScript DSL](./javascript-dsl.md) — for the client-side behavior that does need a script
- [Known issues](./known-issues.md) — the sharp edges, before you hit them
