package com.osmig.Jweb.framework.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Code generation templates for the JWeb CLI.
 */
final class Templates {

    private Templates() {}

    // ==================== Page Template ====================

    static String page(String packageName, String className, String[] fields) {
        String name = className.replace("Page", "");
        return """
            package %s.pages;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Stylesheet;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s page. Register it in Routes: app.pages("/%s", %s.class)
             */
            public class %s implements Template {

                static final Cls ROOT = cls("%s");

                @Override
                public String pageTitle() {
                    return "%s";
                }

                @Override
                public Element render() {
                    return section(ROOT,
                        h1("%s"),
                        p("This is the %s page."));
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(ROOT, style().padding(rem(2)));
                }
            }
            """.formatted(packageName, name, name.toLowerCase(), className, className,
                name.toLowerCase() + "-page", name, name, name.toLowerCase());
    }

    // ==================== Component Template ====================

    static String component(String packageName, String className, String[] fields) {
        StringBuilder propsBuilder = new StringBuilder();
        StringBuilder constructorParams = new StringBuilder();
        StringBuilder constructorAssigns = new StringBuilder();
        StringBuilder renderContent = new StringBuilder();

        if (fields.length > 0) {
            for (String field : fields) {
                String[] parts = field.split(":");
                String name = parts[0];
                String type = parts.length > 1 ? mapType(parts[1]) : "String";

                propsBuilder.append("    private final %s %s;\n".formatted(type, name));

                if (constructorParams.length() > 0) constructorParams.append(", ");
                constructorParams.append("%s %s".formatted(type, name));

                constructorAssigns.append("        this.%s = %s;\n".formatted(name, name));

                renderContent.append("                span(%s),\n".formatted(
                    type.equals("String") ? name : "String.valueOf(" + name + ")"));
            }
        }

        String propsSection = propsBuilder.length() > 0 ? propsBuilder.toString() : "";
        String constructorParamsStr = constructorParams.length() > 0 ? constructorParams.toString() : "";
        String constructorAssignsStr = constructorAssigns.length() > 0 ? constructorAssigns.toString() : "";
        String renderContentStr = renderContent.length() > 0
            ? renderContent.substring(0, renderContent.length() - 2)  // Remove trailing comma
            : "\"" + className + " Component\"";

        String constructor = constructorParamsStr.isEmpty() ? "" : """

                public %s(%s) {
            %s    }
            """.formatted(className, constructorParamsStr, constructorAssignsStr);

        return """
            package %s.components;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Stylesheet;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s component — a Template is an Element, so it drops into any page:
             * {@code div(new %s(...))}. Its styles() rides the page's stylesheet.
             */
            public class %s implements Template {

                static final Cls ROOT = cls("%s");
            %s%s
                @Override
                public Element render() {
                    return div(ROOT,
            %s
                    );
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(ROOT, style()
                            .padding(rem(1))
                            .borderRadius(px(8))
                            .border(px(1), solid, hex("#e5e7eb")));
                }
            }
            """.formatted(packageName, className, className, className,
                className.toLowerCase(),
                propsSection.isEmpty() ? "" : "\n" + propsSection,
                constructor,
                renderContentStr);
    }

    // ==================== Layout Template ====================

    static String layout(String packageName, String className) {
        return """
            package %s.layouts;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Selector;
            import jweb.css.Stylesheet;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s layout. Use it as the default with app.layout(%s.class) — the
             * (title, content) constructor is what the page registry looks for.
             */
            public class %s implements Template {

                static final Cls HEADER = cls("site-header");
                static final Cls NAV = cls("site-nav");
                static final Cls BRAND = cls("site-brand");
                static final Cls MAIN = cls("site-main");
                static final Cls FOOTER = cls("site-footer");

                private final String title;
                private final Element content;

                public %s(String title, Element content) {
                    this.title = title != null ? title : "JWeb App";
                    this.content = content;
                }

                @Override
                public Element render() {
                    return html(attrs().lang("en"),
                        head(
                            metaCharset(),
                            meta("viewport", "width=device-width, initial-scale=1.0"),
                            title(title)),
                        body(
                            header(HEADER, nav(NAV, a(BRAND, href("/"), "JWeb"))),
                            main(MAIN, content),
                            footer(FOOTER, "Built with JWeb")));
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(Selector.type("body"), style().margin(zero).fontFamily(systemUi, sansSerif))
                        .rule(HEADER, style()
                            .backgroundColor(white).borderBottom(px(1), solid, hex("#e5e7eb"))
                            .padding(rem(1), rem(2)))
                        .rule(NAV, row().maxWidth(px(1200)).margin(zero, auto).justifyContent(spaceBetween))
                        .rule(BRAND, style()
                            .fontWeight(700).fontSize(rem(1.25)).color(hex("#111")).textDecoration(none))
                        .rule(MAIN, style().maxWidth(px(1200)).margin(zero, auto).padding(rem(2)))
                        .rule(FOOTER, style()
                            .textAlign(center).padding(rem(2)).color(hex("#6b7280")).fontSize(rem(0.875)));
                }
            }
            """.formatted(packageName, className, className, className, className);
    }

    // ==================== Form Template ====================
    // A form is a record: the components carry the field types, and
    // jweb.Form.* annotations carry the rules the browser and server share.

    static String formModel(String packageName, String className, String[] fields) {
        List<String> components = new ArrayList<>();

        for (String field : fields) {
            String[] parts = field.split(":");
            String name = parts[0];
            String type = parts.length > 1 ? parts[1] : "string";
            boolean required = type.endsWith("!");
            if (required) {
                type = type.substring(0, type.length() - 1);
            }
            String javaType = mapType(type);

            StringBuilder annotations = new StringBuilder();
            if (required) annotations.append("@Form.Required ");
            switch (type.toLowerCase()) {
                case "email" -> annotations.append("@Form.Email ");
                case "password" -> annotations.append("@Form.Password ");
                case "text", "textarea" -> annotations.append("@Form.Multiline ");
                default -> { }
            }

            components.add("    %s%s %s".formatted(annotations, javaType, name));
        }

        String componentList = components.isEmpty() ? "" : String.join(",\n", components) + "\n";

        return """
            package %s.forms;

            import jweb.Form;

            /**
             * %s form — a record is the form: render it with
             * {@code form(%s.class)}, bind a submission with
             * {@code Form.bind(%s.class, req)}.
             */
            public record %s(
            %s) {}
            """.formatted(packageName, className, className, className, className, componentList);
    }

    // ==================== Entity Template ====================

    static String entity(String packageName, String entityName, String[] fields) {
        StringBuilder fieldsBuilder = new StringBuilder();
        StringBuilder gettersSetters = new StringBuilder();

        // ID field
        fieldsBuilder.append("    @Id\n");
        fieldsBuilder.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        fieldsBuilder.append("    private Long id;\n\n");

        gettersSetters.append("""
                public Long getId() {
                    return id;
                }

                public void setId(Long id) {
                    this.id = id;
                }

            """);

        for (String field : fields) {
            String[] parts = field.split(":");
            String name = parts[0];
            String type = parts.length > 1 ? mapType(parts[1]) : "String";

            fieldsBuilder.append("    private %s %s;\n\n".formatted(type, name));

            String capName = capitalize(name);
            String getterPrefix = type.equals("boolean") ? "is" : "get";

            gettersSetters.append("""
                    public %s %s%s() {
                        return %s;
                    }

                    public void set%s(%s %s) {
                        this.%s = %s;
                    }

                """.formatted(type, getterPrefix, capName, name, capName, type, name, name, name));
        }

        return """
            package %s.models;

            import jakarta.persistence.*;

            @Entity
            @Table(name = "%s")
            public class %s {

            %s
            %s}
            """.formatted(packageName, entityName.toLowerCase() + "s", entityName,
                fieldsBuilder.toString(),
                gettersSetters.toString());
    }

    // ==================== Repository Template ====================

    static String repository(String packageName, String entityName) {
        return """
            package %s.repositories;

            import %s.models.%s;
            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.stereotype.Repository;

            @Repository
            public interface %sRepository extends JpaRepository<%s, Long> {
            }
            """.formatted(packageName, packageName, entityName, entityName, entityName);
    }

    // ==================== List Page Template ====================

    static String listPage(String packageName, String entityName, String[] fields) {
        StringBuilder columns = new StringBuilder();
        for (String field : fields) {
            String name = field.split(":")[0];
            String label = camelToTitle(name);
            columns.append("                    .column(\"%s\", %s -> span(%s.get%s() != null ? %s.get%s().toString() : \"\"))\n"
                .formatted(label, entityName.toLowerCase(),
                    entityName.toLowerCase(), capitalize(name),
                    entityName.toLowerCase(), capitalize(name)));
        }

        return """
            package %s.pages;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.UI;
            import jweb.css.Stylesheet;
            import %s.models.%s;
            import %s.repositories.%sRepository;

            import java.util.List;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * Lists every %s. A page in the page table is built by the registry,
             * not by Spring, so the repository comes in through the constructor:
             * app.pages("/%ss", () -> new %sListPage(repository)) from a Routes
             * bean that has it injected.
             */
            public class %sListPage implements Template {

                static final Cls ROOT = cls("%s-list");
                static final Cls TOOLBAR = cls("%s-list-toolbar");
                static final Cls ADD = cls("%s-list-add");

                private final %sRepository repository;

                public %sListPage(%sRepository repository) {
                    this.repository = repository;
                }

                @Override
                public String pageTitle() {
                    return "%ss";
                }

                @Override
                public Element render() {
                    List<%s> items = repository.findAll();

                    return section(ROOT,
                        div(TOOLBAR,
                            h1("%ss"),
                            a(ADD, href("/%ss/new"), "Add new")),
                        UI.DataTable.<%s>create()
            %s                .data(items)
                            .striped()
                            .hoverable()
                            .build());
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(ROOT, style().padding(rem(2)))
                        .rule(TOOLBAR, row().justifyContent(spaceBetween).marginBottom(rem(2)))
                        .rule(ADD, style()
                            .backgroundColor(hex("#6366f1")).color(white)
                            .padding(rem(0.5), rem(1)).borderRadius(px(6))
                            .textDecoration(none));
                }
            }
            """.formatted(packageName, packageName, entityName, packageName, entityName,
                entityName, entityName.toLowerCase(), entityName,
                entityName,
                entityName.toLowerCase(), entityName.toLowerCase(), entityName.toLowerCase(),
                entityName, entityName, entityName,
                entityName,
                entityName, entityName, entityName.toLowerCase(),
                entityName, columns.toString());
    }

    // ==================== Form Page Template ====================

    static String formPage(String packageName, String entityName, String[] fields) {
        return """
            package %s.pages;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Stylesheet;
            import %s.models.%s;
            import %s.repositories.%sRepository;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * Creates or edits one %s. Registered with the repository and the id
             * handed in: app.pages("/%ss/new", () -> new %sFormPage(repository, null))
             * and app.pages("/%ss/:id", req -> new %sFormPage(repository, req.requireParamLong("id"))).
             */
            public class %sFormPage implements Template {

                static final Cls ROOT = cls("%s-form");

                private final %sRepository repository;
                private final Long id;

                public %sFormPage(%sRepository repository, Long id) {
                    this.repository = repository;
                    this.id = id;
                }

                @Override
                public String pageTitle() {
                    return id != null ? "Edit %s" : "New %s";
                }

                @Override
                public Element render() {
                    %s entity = id != null
                        ? repository.findById(id).orElse(new %s())
                        : new %s();

                    return section(ROOT,
                        h1(pageTitle()),
                        buildForm(entity));
                }

                // A JPA entity is not a record, so it renders through plain elements
                // rather than form(Record.class) — add a field per property, e.g.:
                // div(label(for_("name"), "Name"),
                //     input(type("text"), name("name"), id("name"), value(entity.getName())))
                private Element buildForm(%s entity) {
                    return form(action("/%ss" + (id != null ? "/" + id : "")), method("post"),
                        button(type("submit"), id != null ? "Update" : "Create"));
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet().rule(ROOT, style().padding(rem(2)));
                }
            }
            """.formatted(packageName, packageName, entityName, packageName, entityName,
                entityName, entityName.toLowerCase(), entityName, entityName.toLowerCase(), entityName,
                entityName, entityName.toLowerCase(),
                entityName,
                entityName, entityName,
                entityName, entityName,
                entityName, entityName, entityName,
                entityName, entityName.toLowerCase());
    }

    // ==================== API Template ====================

    static String api(String packageName, String className, String[] fields) {
        String resourceName = className.replace("Api", "").replace("Controller", "");
        String resourceLower = resourceName.toLowerCase();

        return """
            package %s.api;

            import jweb.api.REST;
            import jweb.api.GET;
            import jweb.api.POST;
            import jweb.api.UPDATE;
            import jweb.api.DEL;

            import java.util.List;
            import java.util.Map;

            @REST("/api/v1/%ss")
            public class %s {

                @GET
                public List<?> list() {
                    // TODO: Implement list
                    return List.of();
                }

                @GET("/{id}")
                public Object get(Long id) {
                    // TODO: Implement get by id
                    return Map.of("id", id);
                }

                @POST
                public Object create(Map<String, Object> body) {
                    // TODO: Implement create
                    return Map.of("created", true);
                }

                @UPDATE("/{id}")
                public Object update(Long id, Map<String, Object> body) {
                    // TODO: Implement update
                    return Map.of("updated", true, "id", id);
                }

                @DEL("/{id}")
                public Object delete(Long id) {
                    // TODO: Implement delete
                    return Map.of("deleted", true, "id", id);
                }
            }
            """.formatted(packageName, resourceLower, className);
    }

    // ==================== Utilities ====================

    private static String mapType(String type) {
        type = type.toLowerCase().replace("!", "");
        return switch (type) {
            case "string", "text", "email", "password", "url", "tel" -> "String";
            case "int", "integer" -> "Integer";
            case "long" -> "Long";
            case "double" -> "Double";
            case "float" -> "Float";
            case "boolean", "bool" -> "boolean";
            case "date" -> "java.time.LocalDate";
            case "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "decimal", "bigdecimal" -> "java.math.BigDecimal";
            default -> "String";
        };
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String camelToTitle(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                result.append(' ').append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
