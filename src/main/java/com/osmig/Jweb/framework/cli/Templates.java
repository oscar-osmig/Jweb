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

            import com.osmig.Jweb.framework.core.Element;
            import com.osmig.Jweb.framework.template.Template;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

            /**
             * %s page.
             */
            public class %s implements Template {

                @Override
                public Element render() {
                    return div(attrs().style()
                            .padding(rem(2)),
                        h1(text("%s")),
                        p(text("This is the %s page."))
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

                renderContent.append("                span(text(%s)),\n".formatted(
                    type.equals("String") ? name : "String.valueOf(" + name + ")"));
            }
        }

        String propsSection = propsBuilder.length() > 0 ? propsBuilder.toString() : "";
        String constructorParamsStr = constructorParams.length() > 0 ? constructorParams.toString() : "";
        String constructorAssignsStr = constructorAssigns.length() > 0 ? constructorAssigns.toString() : "";
        String renderContentStr = renderContent.length() > 0
            ? renderContent.substring(0, renderContent.length() - 2)  // Remove trailing comma
            : "text(\"" + className + " Component\")";

        String constructor = constructorParamsStr.isEmpty() ? "" : """

                public %s(%s) {
            %s    }
            """.formatted(className, constructorParamsStr, constructorAssignsStr);

        return """
            package %s.components;

            import com.osmig.Jweb.framework.core.Element;
            import com.osmig.Jweb.framework.template.Template;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

            /**
             * %s component.
             */
            public class %s implements Template {
            %s%s
                @Override
                public Element render() {
                    return div(attrs().style()
                            .padding(rem(1))
                            .rounded(px(8))
                            .prop("border", "1px solid #e5e7eb"),
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

            import com.osmig.Jweb.framework.core.Element;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

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
                            title(text("JWeb App"))
                        ),
                        body(attrs().style()
                                .margin(() -> "0")
                                .fontFamily("system-ui, sans-serif"),
                            // Header
                            header(attrs().style()
                                    .backgroundColor(() -> "white")
                                    .prop("border-bottom", "1px solid #e5e7eb")
                                    .padding(rem(1), rem(2)),
                                nav(attrs().style()
                                        .maxWidth(px(1200))
                                        .margin(() -> "0", () -> "auto")
                                        .flex()
                                        .justifyContent(() -> "space-between")
                                        .alignItems(() -> "center"),
                                    a(attrs().href("/").style()
                                            .fontWeight(700)
                                            .fontSize(rem(1.25))
                                            .color(() -> "#111")
                                            .textDecoration(() -> "none"),
                                        text("JWeb")))),
                            // Main content
                            main(attrs().style()
                                    .maxWidth(px(1200))
                                    .margin(() -> "0", () -> "auto")
                                    .padding(rem(2)),
                                content),
                            // Footer
                            footer(attrs().style()
                                    .textCenter()
                                    .padding(rem(2))
                                    .color(() -> "#6b7280")
                                    .fontSize(rem(0.875)),
                                text("Built with JWeb")))
                    );
                }
            }
            """.formatted(packageName, className, className);
    }

    // ==================== Form Model Template ====================

    static String formModel(String packageName, String className, String[] fields) {
        StringBuilder fieldsBuilder = new StringBuilder();
        StringBuilder gettersSetters = new StringBuilder();

        for (String field : fields) {
            String[] parts = field.split(":");
            String name = parts[0];
            String type = parts.length > 1 ? parts[1] : "string";
            String javaType = mapType(type);
            String fieldType = mapFieldType(type);
            boolean required = type.endsWith("!");
            if (required) {
                type = type.substring(0, type.length() - 1);
            }

            // Field with annotation
            String label = camelToTitle(name);
            String annotation = "@FormField(label = \"%s\"".formatted(label);
            if (!fieldType.equals("TEXT")) {
                annotation += ", type = FieldType." + fieldType;
            }
            if (required) {
                annotation += ", required = true";
            }
            annotation += ")";

            fieldsBuilder.append("    %s\n".formatted(annotation));
            fieldsBuilder.append("    private %s %s;\n\n".formatted(javaType, name));

            // Getter
            String capName = capitalize(name);
            String getterPrefix = javaType.equals("boolean") ? "is" : "get";
            gettersSetters.append("""
                    public %s %s%s() {
                        return %s;
                    }

                """.formatted(javaType, getterPrefix, capName, name));

            // Setter
            gettersSetters.append("""
                    public void set%s(%s %s) {
                        this.%s = %s;
                    }

                """.formatted(capName, javaType, name, name, name));
        }

        return """
            package %s.forms;

            import com.osmig.Jweb.framework.forms.FormModel.FormField;
            import com.osmig.Jweb.framework.forms.FormModel.FieldType;

            /**
             * %s form model.
             */
            public class %s {

            %s
            %s}
            """.formatted(packageName, className, className,
                fieldsBuilder.toString(),
                gettersSetters.toString());
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
            columns.append("                    .column(\"%s\", %s -> text(%s.get%s() != null ? %s.get%s().toString() : \"\"))\n"
                .formatted(label, entityName.toLowerCase(),
                    entityName.toLowerCase(), capitalize(name),
                    entityName.toLowerCase(), capitalize(name)));
        }

        return """
            package %s.pages;

            import com.osmig.Jweb.framework.core.Element;
            import com.osmig.Jweb.framework.template.Template;
            import com.osmig.Jweb.framework.ui.UI;
            import %s.models.%s;
            import %s.repositories.%sRepository;
            import org.springframework.beans.factory.annotation.Autowired;

            import java.util.List;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;
            import static com.osmig.Jweb.framework.ui.UI.*;

            public class %sListPage implements Template {

                @Autowired
                private %sRepository repository;

                @Override
                public Element render() {
                    List<%s> items = repository.findAll();

                    return div(attrs().style().padding(rem(2)),
                        div(attrs().style()
                                .flex()
                                .justifyContent(() -> "space-between")
                                .alignItems(() -> "center")
                                .marginBottom(rem(2)),
                            h1(text("%ss")),
                            a(attrs().href("/%ss/new").style()
                                    .backgroundColor(() -> "#6366f1")
                                    .color(() -> "white")
                                    .padding(rem(0.5), rem(1))
                                    .rounded(px(6))
                                    .textDecoration(() -> "none"),
                                text("Add New"))),
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

            import com.osmig.Jweb.framework.core.Element;
            import com.osmig.Jweb.framework.template.Template;
            import com.osmig.Jweb.framework.forms.Form;
            import %s.models.%s;
            import %s.repositories.%sRepository;
            import org.springframework.beans.factory.annotation.Autowired;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

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

                    return div(attrs().style().padding(rem(2)),
                        h1(text(id != null ? "Edit %s" : "New %s")),
                        buildForm(entity)
                    );
                }

                private Element buildForm(%s entity) {
                    var form = Form.create()
                        .action("/%ss" + (id != null ? "/" + id : ""))
                        .method("POST");

                    // Add form fields here based on entity properties

                    return form
                        .submit(id != null ? "Update" : "Create")
                        .build();
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

            import com.osmig.Jweb.framework.api.REST;
            import com.osmig.Jweb.framework.api.GET;
            import com.osmig.Jweb.framework.api.POST;
            import com.osmig.Jweb.framework.api.UPDATE;
            import com.osmig.Jweb.framework.api.DEL;

            import java.util.List;
            import java.util.Map;

            @REST("/%ss")
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

    private static String mapFieldType(String type) {
        type = type.toLowerCase().replace("!", "");
        return switch (type) {
            case "email" -> "EMAIL";
            case "password" -> "PASSWORD";
            case "url" -> "URL";
            case "tel", "phone" -> "TEL";
            case "int", "integer", "long", "double", "float", "decimal" -> "NUMBER";
            case "boolean", "bool" -> "CHECKBOX";
            case "date" -> "DATE";
            case "datetime" -> "DATETIME";
            case "time" -> "TIME";
            case "textarea", "text" -> "TEXTAREA";
            default -> "TEXT";
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
