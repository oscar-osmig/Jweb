package jweb;

import jweb.css.Selector;
import jweb.css.Stylesheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static jweb.Css.*;
import static jweb.El.*;

/**
 * An admin for the tables an app keeps through {@code Sql.table}: a page
 * per table that lists, searches and pages its rows, a form to add or edit
 * one (the record's components are the fields, its {@code Form.*}
 * annotations the rules), and a delete. Mounted under one path and guarded
 * like any other route:
 *
 * <pre>{@code
 * Sql.Table<Todo> todos = Sql.table("todos", Todo.class);
 * Sql.Table<User> users = Sql.table("users", User.class);
 *
 * Admin.mount(app, "/admin", todos, users)
 *      .guard(Auth.requireRole("admin"));
 * }</pre>
 *
 * <p>The pages are templates in the page table, so the app's layout wraps
 * them and {@code Middlewares.recommended()} protects the forms. A table
 * whose record has no {@code id} component is listed read-only.</p>
 */
public final class Admin {

    private static final int PAGE_SIZE = 50;

    private final JWeb app;
    private final String path;
    private final Map<String, Sql.Table<?>> tables = new LinkedHashMap<>();
    private String title = "Admin";

    private Admin(JWeb app, String path) {
        this.app = app;
        this.path = path;
    }

    /** Mounts the admin for the tables under the path and returns it for a guard or a title. */
    public static Admin mount(JWeb app, String path, Sql.Table<?>... tables) {
        String base = path == null || path.isBlank() ? "/admin" : path.trim();
        if (!base.startsWith("/")) base = "/" + base;
        while (base.length() > 1 && base.endsWith("/")) base = base.substring(0, base.length() - 1);
        Admin admin = new Admin(app, base);
        for (Sql.Table<?> table : tables) admin.tables.put(table.name(), table);
        admin.register();
        return admin;
    }

    /** A guard in front of every admin route: {@code Auth.requireRole("admin")}, {@code Auth.requireLogin("/login")}. */
    public Admin guard(Middleware middleware) {
        app.guard(path + "/**", middleware);
        return this;
    }

    public Admin guard(Guard guard) {
        app.guard(path + "/**", guard);
        return this;
    }

    /** The name on the index page and in titles. */
    public Admin title(String title) {
        this.title = title;
        return this;
    }

    public String path() {
        return path;
    }

    public List<String> tables() {
        return new ArrayList<>(tables.keySet());
    }

    // ==================== routes ====================

    private void register() {
        app.pages(path, req -> new IndexPage());
        app.pages(path + "/:table", req -> page(req, (table, t) -> new ListPage<>(t, req.query("q"), Math.max(1, req.queryInt("page", 1)))));
        app.pages(path + "/:table/new", req -> page(req, (table, t) -> new EditPage<>(t, null, null)));
        app.pages(path + "/:table/:id", req -> page(req, (table, t) -> new EditPage<>(t, req.param("id"), null)));
        app.post(path + "/:table/save", req -> {
            Sql.Table<?> table = tables.get(req.param("table"));
            if (table == null) return Response.notFound("No such table");
            return save(table, req);
        });
        app.post(path + "/:table/:id/delete", req -> {
            Sql.Table<?> table = tables.get(req.param("table"));
            if (table == null) return Response.notFound("No such table");
            table.delete(id(table, req.param("id")));
            return Response.redirect(path + "/" + table.name());
        });
    }

    private interface PageFor {
        Template make(String name, Sql.Table<?> table);
    }

    private Template page(Request req, PageFor factory) {
        String name = req.param("table");
        Sql.Table<?> table = tables.get(name);
        if (table == null) throw JWebException.notFound("No table named " + name + " in the admin");
        return factory.make(name, table);
    }

    private <T extends Record> Object save(Sql.Table<?> raw, Request req) {
        @SuppressWarnings("unchecked")
        Sql.Table<T> table = (Sql.Table<T>) raw;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) table.type();
        if (!type.isRecord() || !table.hasId()) return Response.forbidden("This table is read-only in the admin");
        Form.Bound<T> bound = Form.bind(type, req);
        if (!bound.ok()) {
            String id = req.formParam("id");
            return new EditPage<>(table, id == null || id.isBlank() ? null : id, bound);
        }
        T saved = table.save(bound.value());
        return Response.redirect(path + "/" + table.name() + "?saved=" + segment(table.idOf(saved)));
    }

    /** The id as the record's id type wants it. */
    private static Object id(Sql.Table<?> table, String raw) {
        Class<?> idType = null;
        for (java.lang.reflect.RecordComponent component : table.type().getRecordComponents()) {
            if (component.getName().equals("id")) idType = component.getType();
        }
        try {
            if (idType == Long.class || idType == long.class) return Long.parseLong(raw);
            if (idType == Integer.class || idType == int.class) return Integer.parseInt(raw);
            if (idType == java.util.UUID.class) return java.util.UUID.fromString(raw);
        } catch (IllegalArgumentException notAnId) {
            // NumberFormatException included: a URL with an id of the wrong shape is a missing row, not a crash
            throw JWebException.notFound("No " + table.name() + " with id " + raw);
        }
        return raw;
    }

    /** An id as one path segment of a link — encoded, so a string id with {@code ?}, {@code #} or a space still addresses its row. */
    private static String segment(Object id) {
        return java.net.URLEncoder.encode(String.valueOf(id), java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    // ==================== pages ====================

    static final Cls ROOT = cls("jweb-admin");
    static final Cls NAV = cls("jweb-admin-nav");
    static final Cls BAR = cls("jweb-admin-bar");
    static final Cls TABLE = cls("jweb-admin-table");
    static final Cls ACTIONS = cls("jweb-admin-actions");
    static final Cls BUTTON = cls("jweb-admin-button");
    static final Cls DANGER = cls("jweb-admin-danger");
    static final Cls PAGER = cls("jweb-admin-pager");
    static final Cls NOTICE = cls("jweb-admin-notice");

    private Element shell(String heading, Object... content) {
        List<Object> links = new ArrayList<>();
        links.add(a(href(path), title));
        for (String name : tables.keySet()) links.add(a(href(path + "/" + name), name));
        return section(ROOT,
            nav(NAV, links.toArray()),
            h1(heading),
            div(content));
    }

    private Stylesheet adminStyles() {
        return stylesheet()
            .rule(ROOT, style().padding(rem(1), zero))
            .rule(NAV, style().display(flex).gap(rem(1)).marginBottom(rem(1.5)).flexWrap(wrap))
            .rule(NAV.descendant(Selector.type("a")), style().textDecoration(none).color(hex("#4f46e5")))
            .rule(BAR, style().display(flex).gap(rem(0.75)).alignItems(center).marginBottom(rem(1)).flexWrap(wrap))
            .rule(TABLE, style().width(percent(100)).borderCollapse(collapse))
            .rule(TABLE.descendant(Selector.type("th")), style().textAlign(left).padding(rem(0.5)).borderBottom(px(2), solid, hex("#e5e7eb")).fontSize(rem(0.8)).textTransform(uppercase).color(hex("#6b7280")))
            .rule(TABLE.descendant(Selector.type("td")), style().padding(rem(0.5)).borderBottom(px(1), solid, hex("#e5e7eb")).fontSize(rem(0.9)).verticalAlign("top"))
            .rule(ACTIONS, style().display(flex).gap(rem(0.5)).whiteSpace(nowrap))
            .rule(BUTTON, style().display(inlineBlock).padding(rem(0.4), rem(0.9)).borderRadius(px(6))
                .backgroundColor(hex("#4f46e5")).color(white).textDecoration(none).border(none).cursor(pointer).fontSize(rem(0.9)))
            .rule(DANGER, style().backgroundColor(hex("#dc2626")))
            .rule(PAGER, style().display(flex).gap(rem(1)).marginTop(rem(1)))
            .rule(NOTICE, style().padding(rem(0.75), rem(1)).backgroundColor(hex("#ecfdf5")).color(hex("#065f46")).borderRadius(px(6)).marginBottom(rem(1)));
    }

    /** The tables, with their row counts. */
    final class IndexPage implements Template {
        @Override
        public String pageTitle() {
            return title;
        }

        @Override
        public Element render() {
            return shell(title, ul(each(tables.values(), t ->
                li(a(href(path + "/" + t.name()), t.name()), " — " + t.count() + " rows"))));
        }

        @Override
        public Stylesheet styles() {
            return adminStyles();
        }
    }

    /** One table's rows, searched and paged. */
    final class ListPage<T> implements Template {
        private final Sql.Table<T> table;
        private final String query;
        private final int page;

        ListPage(Sql.Table<T> table, String query, int page) {
            this.table = table;
            this.query = query == null ? "" : query;
            this.page = Math.max(1, page);
        }

        @Override
        public String pageTitle() {
            return table.name() + " — " + title;
        }

        @Override
        public Element render() {
            List<T> rows = table.hasId() ? table.search(query, page, PAGE_SIZE) : table.all();
            List<String> fields = table.fields();
            Request req = com.osmig.Jweb.framework.server.CurrentRequest.get();
            String saved = req == null ? null : req.query("saved");

            List<Object> head = new ArrayList<>();
            for (String field : fields) head.add(th(field));
            if (table.hasId()) head.add(th(""));

            Element body = tbody(each(rows, row -> {
                Object[] values = values(row, fields);
                List<Object> cells = new ArrayList<>();
                for (Object value : values) cells.add(td(value == null ? "" : String.valueOf(value)));
                if (table.hasId()) {
                    Object id = table.idOf(row);
                    cells.add(td(div(ACTIONS,
                        a(BUTTON, href(path + "/" + table.name() + "/" + segment(id)), "Edit"),
                        form(action(path + "/" + table.name() + "/" + segment(id) + "/delete"), method("post"), style().margin(zero),
                            Csrf.tokenField(),
                            button(BUTTON, DANGER, type("submit"), "Delete")))));
                }
                return tr(cells.toArray());
            }));

            return shell(table.name(),
                saved == null ? fragment() : div(NOTICE, "Saved " + table.name() + " " + saved),
                div(BAR,
                    form(action(path + "/" + table.name()), method("get"), style().margin(zero).display(flex).gap(rem(0.5)),
                        input(type("search"), name("q"), value(query), attrs().placeholder("Search")),
                        button(BUTTON, type("submit"), "Search")),
                    table.hasId() ? a(BUTTON, href(path + "/" + table.name() + "/new"), "Add " + table.name()) : fragment()),
                rows.isEmpty() ? p("No rows.") : table(TABLE, thead(tr(head.toArray())), body),
                table.hasId() ? pager(rows.size()) : fragment());
        }

        private Element pager(int shown) {
            String q = query.isEmpty() ? "" : "&q=" + Storage.encodePath(query);
            return div(PAGER,
                page > 1 ? a(href(path + "/" + table.name() + "?page=" + (page - 1) + q), "← Previous") : fragment(),
                span("Page " + page),
                shown == PAGE_SIZE ? a(href(path + "/" + table.name() + "?page=" + (page + 1) + q), "Next →") : fragment());
        }

        @Override
        public Stylesheet styles() {
            return adminStyles();
        }
    }

    /** Add or edit one row: the record is the form. */
    final class EditPage<T> implements Template {
        private final Sql.Table<T> table;
        private final String id;
        private final Form.Bound<?> failed;

        EditPage(Sql.Table<T> table, String id, Form.Bound<?> failed) {
            this.table = table;
            this.id = id;
            this.failed = failed;
        }

        @Override
        public String pageTitle() {
            return (id == null ? "New " : "Edit ") + table.name() + " — " + title;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Element render() {
            if (!table.hasId()) return shell(table.name(), p("This table has no id column, so the admin lists it read-only."));
            Class<? extends Record> type = (Class<? extends Record>) table.type();
            Optional<T> row = Optional.empty();
            if (failed == null && id != null) {
                row = table.find(id(table, id));
                if (row.isEmpty()) throw JWebException.notFound("No " + table.name() + " with id " + id);
            }
            return shell((id == null ? "New " : "Edit ") + table.name(),
                editForm(type, row.orElse(null)),
                p(a(href(path + "/" + table.name()), "← Back to " + table.name())));
        }

        @SuppressWarnings("unchecked")
        private <R extends Record> Form<R> editForm(Class<R> type, Object row) {
            Form<R> form = Form.of(type).action(path + "/" + table.name() + "/save").submit(id == null ? "Add" : "Save");
            form.field("id", f -> f.type("hidden"));
            if (failed != null) {
                form.values(failed.submitted()).errors((Form.Bound<R>) failed);
            } else if (row != null) {
                form.values((R) row);
            }
            return form;
        }

        @Override
        public Stylesheet styles() {
            return adminStyles();
        }
    }

    private static Object[] values(Object row, List<String> fields) {
        Object[] out = new Object[fields.size()];
        java.lang.reflect.RecordComponent[] components = row.getClass().getRecordComponents();
        for (int i = 0; i < components.length && i < out.length; i++) {
            try {
                out[i] = components[i].getAccessor().invoke(row);
            } catch (ReflectiveOperationException e) {
                out[i] = "?";
            }
        }
        return out;
    }
}
