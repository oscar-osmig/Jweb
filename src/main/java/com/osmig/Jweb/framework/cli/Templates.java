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
        return """
            package %s.pages;

            import jweb.Element;
            import jweb.Template;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s page.
             */
            public class %s implements Template {

                @Override
                public Element render() {
                    return div(style()
                            .padding(rem(2)),
                        h1("%s"),
                        p("This is the %s page.")
                    );
                }
            }
            """.formatted(packageName, className, className,
                className.replace("Page", ""),
                className.replace("Page", "").toLowerCase());
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

            import jweb.Element;
            import jweb.Template;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s component.
             */
            public class %s implements Template {
            %s%s
                @Override
                public Element render() {
                    return div(style()
                            .padding(rem(1))
                            .rounded(px(8))
                            .border(px(1), solid, hex("#e5e7eb")),
            %s
                    );
                }
            }
            """.formatted(packageName, className, className,
                propsSection.isEmpty() ? "" : "\n" + propsSection,
                constructor,
                renderContentStr);
    }

    // ==================== Layout Template ====================

    static String layout(String packageName, String className) {
        return """
            package %s.layouts;

            import jweb.Element;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * %s layout.
             */
            public class %s {

                public static Element wrap(Element... content) {
                    return html(
                        head(
                            meta(attrs().set("charset", "UTF-8")),
                            meta(attrs()
                                .name("viewport")
                                .content("width=device-width, initial-scale=1.0")),
                            title("JWeb App")
                        ),
                        body(style()
                                .margin(zero)
                                .fontFamily("system-ui, sans-serif"),
                            // Header
                            header(style()
                                    .backgroundColor(white)
                                    .borderBottom(px(1), solid, hex("#e5e7eb"))
                                    .padding(rem(1), rem(2)),
                                nav(style()
                                        .maxWidth(px(1200))
                                        .margin(zero, auto)
                                        .flex()
                                        .justifyContent(spaceBetween)
                                        .alignItems(center),
                                    a(attrs().href("/").style(s -> s
                                            .fontWeight(700)
                                            .fontSize(rem(1.25))
                                            .color(hex("#111"))
                                            .textDecoration(none)),
                                        "JWeb"))),
                            // Main content
                            main(style()
                                    .maxWidth(px(1200))
                                    .margin(zero, auto)
                                    .padding(rem(2)),
                                content),
                            // Footer
                            footer(style()
                                    .textCenter()
                                    .padding(rem(2))
                                    .color(hex("#6b7280"))
                                    .fontSize(rem(0.875)),
                                "Built with JWeb"))
                    );
                }
            }
            """.formatted(packageName, className, className);
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

            import jweb.Element;
            import jweb.Template;
            import jweb.UI;
            import %s.models.%s;
            import %s.repositories.%sRepository;
            import org.springframework.beans.factory.annotation.Autowired;

            import java.util.List;

            import static jweb.El.*;
            import static jweb.Css.*;
            import static jweb.UI.*;

            public class %sListPage implements Template {

                @Autowired
                private %sRepository repository;

                @Override
                public Element render() {
                    List<%s> items = repository.findAll();

                    return div(style().padding(rem(2)),
                        div(style()
                                .flex()
                                .justifyContent(spaceBetween)
                                .alignItems(center)
                                .marginBottom(rem(2)),
                            h1("%ss"),
                            a(attrs().href("/%ss/new").style(s -> s
                                    .backgroundColor(hex("#6366f1"))
                                    .color(white)
                                    .padding(rem(0.5), rem(1))
                                    .rounded(px(6))
                                    .textDecoration(none)),
                                "Add New")),
                        UI.DataTable.<%s>create()
            %s                .data(items)
                            .striped()
                            .hoverable()
                            .build()
                    );
                }
            }
            """.formatted(packageName, packageName, entityName, packageName, entityName,
                entityName, entityName, entityName, entityName, entityName.toLowerCase(),
                entityName, columns.toString());
    }

    // ==================== Form Page Template ====================

    static String formPage(String packageName, String entityName, String[] fields) {
        return """
            package %s.pages;

            import jweb.Element;
            import jweb.Template;
            import %s.models.%s;
            import %s.repositories.%sRepository;
            import org.springframework.beans.factory.annotation.Autowired;

            import static jweb.El.*;
            import static jweb.Css.*;

            public class %sFormPage implements Template {

                @Autowired
                private %sRepository repository;

                private Long id;

                public %sFormPage() {}

                public %sFormPage(Long id) {
                    this.id = id;
                }

                @Override
                public Element render() {
                    %s entity = id != null
                        ? repository.findById(id).orElse(new %s())
                        : new %s();

                    return div(style().padding(rem(2)),
                        h1(id != null ? "Edit %s" : "New %s"),
                        buildForm(entity)
                    );
                }

                // This is a JPA entity, not a record, so it renders through plain
                // elements rather than the record-based jweb.Form — add a field
                // for each property, e.g.:
                // div(style().marginBottom(rem(1)),
                //     label(for_("name"), "Name"),
                //     input(type("text"), name("name"), id("name"), value(entity.getName())))
                private Element buildForm(%s entity) {
                    return form(attrs()
                            .action("/%ss" + (id != null ? "/" + id : ""))
                            .method("POST"),
                        button(type("submit"), id != null ? "Update" : "Create")
                    );
                }
            }
            """.formatted(packageName, packageName, entityName, packageName, entityName,
                entityName, entityName, entityName, entityName,
                entityName, entityName, entityName,
                entityName, entityName, entityName, entityName.toLowerCase());
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
