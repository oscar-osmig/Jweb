package com.osmig.Jweb.framework.forms;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.events.Event;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Form model binding system for auto-generating forms from POJOs.
 *
 * <p>Example - Basic usage:</p>
 * <pre>
 * public class UserForm {
 *     {@literal @}FormField(label = "Full Name", required = true)
 *     private String name;
 *
 *     {@literal @}FormField(label = "Email Address", type = FieldType.EMAIL)
 *     private String email;
 *
 *     {@literal @}FormField(label = "Age", min = 0, max = 150)
 *     private Integer age;
 *
 *     {@literal @}FormField(label = "Bio", type = FieldType.TEXTAREA, rows = 4)
 *     private String bio;
 *
 *     {@literal @}FormField(label = "Role", options = {"USER", "ADMIN", "MODERATOR"})
 *     private String role;
 *
 *     // getters and setters...
 * }
 *
 * // Generate form
 * UserForm user = new UserForm();
 * user.setName("John");
 *
 * Element form = FormModel.of(user)
 *     .action("/users/save")
 *     .submitLabel("Save User")
 *     .build();
 * </pre>
 *
 * <p>Example - Binding request data:</p>
 * <pre>
 * {@literal @}PostMapping("/users/save")
 * public String saveUser(Request request) {
 *     UserForm user = FormModel.bind(UserForm.class, request.formData());
 *     userService.save(user);
 *     return "redirect:/users";
 * }
 * </pre>
 */
public final class FormModel {

    private FormModel() {}

    // ==================== Annotations ====================

    /**
     * Marks a field for form generation with configuration.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FormField {
        /** Display label for the field */
        String label() default "";

        /** Input type (auto-detected if not specified) */
        FieldType type() default FieldType.AUTO;

        /** Placeholder text */
        String placeholder() default "";

        /** Help text shown below the field */
        String help() default "";

        /** Whether the field is required */
        boolean required() default false;

        /** Whether the field is disabled */
        boolean disabled() default false;

        /** Whether the field is read-only */
        boolean readonly() default false;

        /** Minimum value (for numbers) or min length (for strings) */
        int min() default Integer.MIN_VALUE;

        /** Maximum value (for numbers) or max length (for strings) */
        int max() default Integer.MAX_VALUE;

        /** Regex pattern for validation */
        String pattern() default "";

        /** Number of rows (for textarea) */
        int rows() default 3;

        /** Options for select/radio (value=label pairs, or just values) */
        String[] options() default {};

        /** Display order (lower = earlier) */
        int order() default 100;

        /** CSS class for the input */
        String className() default "";

        /** Group name for fieldset grouping */
        String group() default "";
    }

    /**
     * Marks a field to be excluded from form generation.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FormIgnore {}

    /**
     * Marks a field as hidden in the form.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FormHidden {}

    /**
     * Input field types.
     */
    public enum FieldType {
        AUTO,       // Auto-detect from Java type
        TEXT,
        EMAIL,
        PASSWORD,
        NUMBER,
        TEL,
        URL,
        DATE,
        TIME,
        DATETIME,
        TEXTAREA,
        SELECT,
        RADIO,
        CHECKBOX,
        HIDDEN,
        COLOR,
        RANGE,
        FILE
    }

    // ==================== Builder ====================

    /**
     * Creates a form builder for the given model instance.
     *
     * @param model the model instance with current values
     * @param <T> the model type
     * @return a FormBuilder for configuring and building the form
     */
    public static <T> FormBuilder<T> of(T model) {
        return new FormBuilder<>(model);
    }

    /**
     * Creates a form builder for a new instance of the model class.
     *
     * @param modelClass the model class
     * @param <T> the model type
     * @return a FormBuilder for configuring and building the form
     */
    public static <T> FormBuilder<T> of(Class<T> modelClass) {
        try {
            return new FormBuilder<>(modelClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + modelClass.getName() +
                    ". Ensure it has a no-arg constructor.", e);
        }
    }

    /**
     * Builder for configuring and generating forms from model objects.
     */
    public static class FormBuilder<T> {
        private final T model;
        private final Class<?> modelClass;
        private String action = "";
        private String method = "POST";
        private String id;
        private String submitLabel = "Submit";
        private String cancelLabel;
        private String cancelHref;
        private Consumer<Event> onSubmit;
        private boolean noValidate = false;
        private boolean horizontal = false;
        private final Set<String> excludeFields = new HashSet<>();
        private final Set<String> includeFields = new HashSet<>();
        private final Map<String, String> labelOverrides = new HashMap<>();
        private final Map<String, FieldType> typeOverrides = new HashMap<>();
        private final Map<String, String[]> optionsOverrides = new HashMap<>();
        private Consumer<StyleBuilder> formStyle;
        private Consumer<StyleBuilder> fieldStyle;
        private Consumer<StyleBuilder> labelStyle;
        private Consumer<StyleBuilder> inputStyle;

        FormBuilder(T model) {
            this.model = model;
            this.modelClass = model.getClass();
        }

        public FormBuilder<T> action(String action) { this.action = action; return this; }
        public FormBuilder<T> method(String method) { this.method = method; return this; }
        public FormBuilder<T> id(String id) { this.id = id; return this; }
        public FormBuilder<T> submitLabel(String label) { this.submitLabel = label; return this; }
        public FormBuilder<T> cancelLabel(String label) { this.cancelLabel = label; return this; }
        public FormBuilder<T> cancelHref(String href) { this.cancelHref = href; return this; }
        public FormBuilder<T> onSubmit(Consumer<Event> handler) { this.onSubmit = handler; return this; }
        public FormBuilder<T> noValidate() { this.noValidate = true; return this; }
        public FormBuilder<T> horizontal() { this.horizontal = true; return this; }

        /** Exclude specific fields from the form */
        public FormBuilder<T> exclude(String... fields) {
            excludeFields.addAll(Arrays.asList(fields));
            return this;
        }

        /** Include only specific fields (by default all annotated fields are included) */
        public FormBuilder<T> include(String... fields) {
            includeFields.addAll(Arrays.asList(fields));
            return this;
        }

        /** Override a field's label */
        public FormBuilder<T> label(String field, String label) {
            labelOverrides.put(field, label);
            return this;
        }

        /** Override a field's type */
        public FormBuilder<T> type(String field, FieldType type) {
            typeOverrides.put(field, type);
            return this;
        }

        /** Override a field's select/radio options */
        public FormBuilder<T> options(String field, String... options) {
            optionsOverrides.put(field, options);
            return this;
        }

        public FormBuilder<T> formStyle(Consumer<StyleBuilder> style) { this.formStyle = style; return this; }
        public FormBuilder<T> fieldStyle(Consumer<StyleBuilder> style) { this.fieldStyle = style; return this; }
        public FormBuilder<T> labelStyle(Consumer<StyleBuilder> style) { this.labelStyle = style; return this; }
        public FormBuilder<T> inputStyle(Consumer<StyleBuilder> style) { this.inputStyle = style; return this; }

        /**
         * Builds the form element.
         */
        public Element build() {
            List<FieldInfo> fields = extractFields();
            fields.sort(Comparator.comparingInt(f -> f.order));

            // Group fields by group name
            Map<String, List<FieldInfo>> groups = new LinkedHashMap<>();
            groups.put("", new ArrayList<>()); // default group

            for (FieldInfo field : fields) {
                String group = field.group != null ? field.group : "";
                groups.computeIfAbsent(group, k -> new ArrayList<>()).add(field);
            }

            List<Element> formElements = new ArrayList<>();

            for (Map.Entry<String, List<FieldInfo>> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                List<FieldInfo> groupFields = entry.getValue();

                if (groupFields.isEmpty()) continue;

                List<Element> fieldElements = new ArrayList<>();
                for (FieldInfo field : groupFields) {
                    fieldElements.add(buildField(field));
                }

                if (!groupName.isEmpty()) {
                    // Wrap in fieldset
                    formElements.add(fieldset(
                        legend(text(groupName)),
                        fragment(fieldElements.toArray())
                    ));
                } else {
                    formElements.addAll(fieldElements);
                }
            }

            // Buttons
            List<Element> buttons = new ArrayList<>();
            buttons.add(button(attrs().type("submit").style()
                    .backgroundColor(() -> "#6366f1")
                    .color(() -> "white")
                    .padding(() -> "0.5rem", () -> "1rem")
                    .prop("border", "none")
                    .prop("border-radius", "6px")
                    .fontSize(() -> "0.875rem")
                    .fontWeight(500)
                    .clickable(),
                text(submitLabel)));

            if (cancelLabel != null) {
                if (cancelHref != null) {
                    buttons.add(a(attrs().href(cancelHref).style()
                            .color(() -> "#6b7280")
                            .padding(() -> "0.5rem", () -> "1rem")
                            .fontSize(() -> "0.875rem")
                            .textDecoration(() -> "none"),
                        text(cancelLabel)));
                } else {
                    buttons.add(button(attrs().type("button").style()
                            .backgroundColor(() -> "transparent")
                            .color(() -> "#6b7280")
                            .padding(() -> "0.5rem", () -> "1rem")
                            .prop("border", "1px solid #d1d5db")
                            .prop("border-radius", "6px")
                            .fontSize(() -> "0.875rem")
                            .clickable(),
                        text(cancelLabel)));
                }
            }

            formElements.add(div(attrs().style()
                    .flex()
                    .gap(() -> "0.75rem")
                    .marginTop(() -> "1.5rem"),
                buttons.toArray()));

            // Form element
            var formAttrs = attrs()
                .action(action)
                .method(method);

            if (id != null) formAttrs.id(id);
            if (noValidate) formAttrs.set("novalidate", null);
            if (onSubmit != null) formAttrs.onSubmit(onSubmit);

            if (formStyle != null) {
                StyleBuilder s = new StyleBuilder();
                formStyle.accept(s);
                formAttrs.style(s.build());
            } else {
                formAttrs.style()
                    .flex()
                    .flexDirection(() -> "column")
                    .gap(() -> "1rem");
            }

            return form(formAttrs, formElements.toArray());
        }

        private Element buildField(FieldInfo field) {
            if (field.type == FieldType.HIDDEN) {
                return input(attrs().type("hidden").name(field.name).value(field.value != null ? field.value.toString() : ""));
            }

            List<Element> fieldParts = new ArrayList<>();

            // Label
            if (field.label != null && !field.label.isEmpty() && field.type != FieldType.CHECKBOX) {
                var labelAttrs = attrs().for_(field.name);
                if (labelStyle != null) {
                    StyleBuilder s = new StyleBuilder();
                    labelStyle.accept(s);
                    labelAttrs = labelAttrs.style(s.build());
                } else {
                    labelAttrs = labelAttrs.style()
                        .fontSize(() -> "0.875rem")
                        .fontWeight(500)
                        .color(() -> "#374151")
                        .marginBottom(() -> "0.25rem")
                        .display(() -> "block")
                        .done();
                }
                String labelText = field.label + (field.required ? " *" : "");
                fieldParts.add(tag("label", labelAttrs, text(labelText)));
            }

            // Input element
            Element inputElement = switch (field.type) {
                case TEXTAREA -> buildTextarea(field);
                case SELECT -> buildSelect(field);
                case RADIO -> buildRadio(field);
                case CHECKBOX -> buildCheckbox(field);
                default -> buildInput(field);
            };
            fieldParts.add(inputElement);

            // Help text
            if (field.help != null && !field.help.isEmpty()) {
                fieldParts.add(small(attrs().style()
                        .color(() -> "#6b7280")
                        .fontSize(() -> "0.75rem")
                        .marginTop(() -> "0.25rem")
                        .display(() -> "block"),
                    text(field.help)));
            }

            // Wrapper
            var wrapperAttrs = attrs();
            if (fieldStyle != null) {
                StyleBuilder s = new StyleBuilder();
                fieldStyle.accept(s);
                wrapperAttrs.style(s.build());
            } else if (horizontal && field.type != FieldType.CHECKBOX) {
                wrapperAttrs.style()
                    .flex()
                    .alignItems(() -> "center")
                    .gap(() -> "1rem");
            }

            return div(wrapperAttrs, fieldParts.toArray());
        }

        private Element buildInput(FieldInfo field) {
            var inputAttrs = attrs()
                .type(field.htmlType)
                .name(field.name)
                .id(field.name);

            if (field.value != null) {
                inputAttrs.value(formatValue(field.value, field.type));
            }
            if (field.placeholder != null && !field.placeholder.isEmpty()) {
                inputAttrs.placeholder(field.placeholder);
            }
            if (field.required) inputAttrs.required();
            if (field.disabled) inputAttrs.disabled();
            if (field.readonly) inputAttrs.readonly();
            if (field.pattern != null && !field.pattern.isEmpty()) {
                inputAttrs.set("pattern", field.pattern);
            }
            if (field.min != Integer.MIN_VALUE) {
                if (field.type == FieldType.NUMBER || field.type == FieldType.RANGE) {
                    inputAttrs.set("min", String.valueOf(field.min));
                } else {
                    inputAttrs.set("minlength", String.valueOf(field.min));
                }
            }
            if (field.max != Integer.MAX_VALUE) {
                if (field.type == FieldType.NUMBER || field.type == FieldType.RANGE) {
                    inputAttrs.set("max", String.valueOf(field.max));
                } else {
                    inputAttrs.set("maxlength", String.valueOf(field.max));
                }
            }
            if (field.className != null && !field.className.isEmpty()) {
                inputAttrs.class_(field.className);
            }

            // Default styling
            if (inputStyle != null) {
                StyleBuilder s = new StyleBuilder();
                inputStyle.accept(s);
                inputAttrs.style(s.build());
            } else {
                inputAttrs.style()
                    .width(() -> "100%")
                    .padding(() -> "0.5rem", () -> "0.75rem")
                    .prop("border", "1px solid #d1d5db")
                    .prop("border-radius", "6px")
                    .fontSize(() -> "0.875rem");
            }

            return input(inputAttrs);
        }

        private Element buildTextarea(FieldInfo field) {
            var textareaAttrs = attrs()
                .name(field.name)
                .id(field.name);

            if (field.placeholder != null && !field.placeholder.isEmpty()) {
                textareaAttrs.placeholder(field.placeholder);
            }
            if (field.required) textareaAttrs.required();
            if (field.disabled) textareaAttrs.disabled();
            if (field.readonly) textareaAttrs.readonly();
            textareaAttrs.set("rows", String.valueOf(field.rows));

            if (inputStyle != null) {
                StyleBuilder s = new StyleBuilder();
                inputStyle.accept(s);
                textareaAttrs.style(s.build());
            } else {
                textareaAttrs.style()
                    .width(() -> "100%")
                    .padding(() -> "0.5rem", () -> "0.75rem")
                    .prop("border", "1px solid #d1d5db")
                    .prop("border-radius", "6px")
                    .fontSize(() -> "0.875rem")
                    .prop("resize", "vertical");
            }

            String value = field.value != null ? field.value.toString() : "";
            return textarea(textareaAttrs, value);
        }

        private Element buildSelect(FieldInfo field) {
            var selectAttrs = attrs()
                .name(field.name)
                .id(field.name);

            if (field.required) selectAttrs.required();
            if (field.disabled) selectAttrs.disabled();

            if (inputStyle != null) {
                StyleBuilder s = new StyleBuilder();
                inputStyle.accept(s);
                selectAttrs.style(s.build());
            } else {
                selectAttrs.style()
                    .width(() -> "100%")
                    .padding(() -> "0.5rem", () -> "0.75rem")
                    .prop("border", "1px solid #d1d5db")
                    .prop("border-radius", "6px")
                    .fontSize(() -> "0.875rem")
                    .backgroundColor(() -> "white");
            }

            List<Element> options = new ArrayList<>();
            if (field.placeholder != null && !field.placeholder.isEmpty()) {
                options.add(option(attrs().value("").disabled(), field.placeholder));
            }

            String currentValue = field.value != null ? field.value.toString() : "";
            for (String opt : field.options) {
                String[] parts = opt.split("=", 2);
                String optValue = parts[0];
                String optLabel = parts.length > 1 ? parts[1] : parts[0];
                var optAttrs = attrs().value(optValue);
                if (optValue.equals(currentValue)) {
                    optAttrs.set("selected", null);
                }
                options.add(option(optAttrs, optLabel));
            }

            return select(selectAttrs, options.toArray());
        }

        private Element buildRadio(FieldInfo field) {
            List<Element> radios = new ArrayList<>();
            String currentValue = field.value != null ? field.value.toString() : "";

            for (String opt : field.options) {
                String[] parts = opt.split("=", 2);
                String optValue = parts[0];
                String optLabel = parts.length > 1 ? parts[1] : parts[0];
                String optId = field.name + "-" + optValue.replaceAll("\\W+", "-");

                var radioAttrs = attrs()
                    .type("radio")
                    .name(field.name)
                    .id(optId)
                    .value(optValue);

                if (optValue.equals(currentValue)) {
                    radioAttrs.checked();
                }
                if (field.disabled) radioAttrs.disabled();

                radios.add(div(attrs().style()
                        .flex()
                        .alignItems(() -> "center")
                        .gap(() -> "0.5rem").done(),
                    input(radioAttrs),
                    tag("label", attrs().for_(optId).style()
                            .fontSize(() -> "0.875rem")
                            .color(() -> "#374151").done(),
                        text(optLabel))));
            }

            return div(attrs().style()
                    .flex()
                    .flexDirection(() -> "column")
                    .gap(() -> "0.5rem").done(),
                radios.toArray());
        }

        private Element buildCheckbox(FieldInfo field) {
            boolean checked = false;
            if (field.value != null) {
                if (field.value instanceof Boolean) {
                    checked = (Boolean) field.value;
                } else {
                    checked = "true".equalsIgnoreCase(field.value.toString());
                }
            }

            var checkboxAttrs = attrs()
                .type("checkbox")
                .name(field.name)
                .id(field.name)
                .value("true");

            if (checked) checkboxAttrs.checked();
            if (field.disabled) checkboxAttrs.disabled();

            return div(attrs().style()
                    .flex()
                    .alignItems(() -> "center")
                    .gap(() -> "0.5rem").done(),
                input(checkboxAttrs),
                tag("label", attrs().for_(field.name).style()
                        .fontSize(() -> "0.875rem")
                        .color(() -> "#374151").done(),
                    text(field.label)));
        }

        private List<FieldInfo> extractFields() {
            List<FieldInfo> fields = new ArrayList<>();

            for (Field field : getAllFields(modelClass)) {
                field.setAccessible(true);

                // Skip ignored fields
                if (field.isAnnotationPresent(FormIgnore.class)) continue;
                if (excludeFields.contains(field.getName())) continue;
                if (!includeFields.isEmpty() && !includeFields.contains(field.getName())) continue;

                // Get annotation or use defaults
                FormField annotation = field.getAnnotation(FormField.class);
                FormHidden hiddenAnnotation = field.getAnnotation(FormHidden.class);

                FieldInfo info = new FieldInfo();
                info.name = field.getName();
                info.javaType = field.getType();

                // Get value
                try {
                    info.value = field.get(model);
                } catch (IllegalAccessException e) {
                    // Try getter
                    info.value = getFieldValue(field);
                }

                if (hiddenAnnotation != null) {
                    info.type = FieldType.HIDDEN;
                    fields.add(info);
                    continue;
                }

                if (annotation != null) {
                    info.label = labelOverrides.getOrDefault(info.name,
                            annotation.label().isEmpty() ? formatFieldName(info.name) : annotation.label());
                    info.type = typeOverrides.getOrDefault(info.name, annotation.type());
                    info.placeholder = annotation.placeholder();
                    info.help = annotation.help();
                    info.required = annotation.required();
                    info.disabled = annotation.disabled();
                    info.readonly = annotation.readonly();
                    info.min = annotation.min();
                    info.max = annotation.max();
                    info.pattern = annotation.pattern();
                    info.rows = annotation.rows();
                    info.options = optionsOverrides.getOrDefault(info.name, annotation.options());
                    info.order = annotation.order();
                    info.className = annotation.className();
                    info.group = annotation.group();
                } else {
                    // Default values for unannotated fields
                    info.label = labelOverrides.getOrDefault(info.name, formatFieldName(info.name));
                    info.type = typeOverrides.getOrDefault(info.name, FieldType.AUTO);
                    info.options = optionsOverrides.getOrDefault(info.name, new String[0]);
                }

                // Auto-detect type
                if (info.type == FieldType.AUTO) {
                    info.type = detectFieldType(info.javaType, info.options);
                }

                // Set HTML input type
                info.htmlType = getHtmlType(info.type);

                fields.add(info);
            }

            return fields;
        }

        private Object getFieldValue(Field field) {
            try {
                String getterName = "get" + capitalize(field.getName());
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    getterName = "is" + capitalize(field.getName());
                }
                Method getter = modelClass.getMethod(getterName);
                return getter.invoke(model);
            } catch (Exception e) {
                return null;
            }
        }

        private List<Field> getAllFields(Class<?> clazz) {
            List<Field> fields = new ArrayList<>();
            while (clazz != null && clazz != Object.class) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
            return fields;
        }

        private FieldType detectFieldType(Class<?> type, String[] options) {
            if (options != null && options.length > 0) {
                return FieldType.SELECT;
            }
            if (type == String.class) return FieldType.TEXT;
            if (type == int.class || type == Integer.class) return FieldType.NUMBER;
            if (type == long.class || type == Long.class) return FieldType.NUMBER;
            if (type == double.class || type == Double.class) return FieldType.NUMBER;
            if (type == float.class || type == Float.class) return FieldType.NUMBER;
            if (type == BigDecimal.class) return FieldType.NUMBER;
            if (type == boolean.class || type == Boolean.class) return FieldType.CHECKBOX;
            if (type == LocalDate.class) return FieldType.DATE;
            if (type == LocalTime.class) return FieldType.TIME;
            if (type == LocalDateTime.class) return FieldType.DATETIME;
            if (type.isEnum()) return FieldType.SELECT;
            return FieldType.TEXT;
        }

        private String getHtmlType(FieldType type) {
            return switch (type) {
                case EMAIL -> "email";
                case PASSWORD -> "password";
                case NUMBER -> "number";
                case TEL -> "tel";
                case URL -> "url";
                case DATE -> "date";
                case TIME -> "time";
                case DATETIME -> "datetime-local";
                case COLOR -> "color";
                case RANGE -> "range";
                case FILE -> "file";
                case HIDDEN -> "hidden";
                default -> "text";
            };
        }

        private String formatFieldName(String name) {
            // Convert camelCase to Title Case
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
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

        private String formatValue(Object value, FieldType type) {
            if (value == null) return "";
            if (value instanceof LocalDate) {
                return value.toString(); // ISO format
            }
            if (value instanceof LocalDateTime) {
                return value.toString().replace("T", " ");
            }
            if (value instanceof LocalTime) {
                return value.toString();
            }
            if (value.getClass().isEnum()) {
                return ((Enum<?>) value).name();
            }
            return value.toString();
        }

        private String capitalize(String s) {
            return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        private static class FieldInfo {
            String name;
            String label;
            FieldType type;
            String htmlType;
            String placeholder;
            String help;
            boolean required;
            boolean disabled;
            boolean readonly;
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;
            String pattern;
            int rows = 3;
            String[] options = {};
            int order = 100;
            String className;
            String group;
            Object value;
            Class<?> javaType;
        }
    }

    // ==================== Binding ====================

    /**
     * Binds form data to a new instance of the model class.
     *
     * @param modelClass the model class to instantiate
     * @param formData map of form field names to values
     * @param <T> the model type
     * @return a new instance with bound values
     */
    public static <T> T bind(Class<T> modelClass, Map<String, String> formData) {
        try {
            T instance = modelClass.getDeclaredConstructor().newInstance();
            return bind(instance, formData);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + modelClass.getName(), e);
        }
    }

    /**
     * Binds form data to an existing model instance.
     *
     * @param model the model instance to bind to
     * @param formData map of form field names to values
     * @param <T> the model type
     * @return the same instance with updated values
     */
    public static <T> T bind(T model, Map<String, String> formData) {
        Class<?> clazz = model.getClass();

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            try {
                Field field = findField(clazz, fieldName);
                if (field == null) continue;

                field.setAccessible(true);
                Object convertedValue = convert(value, field.getType());
                field.set(model, convertedValue);
            } catch (Exception e) {
                // Try setter
                try {
                    String setterName = "set" + capitalize(fieldName);
                    for (Method method : clazz.getMethods()) {
                        if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                            Object convertedValue = convert(value, method.getParameterTypes()[0]);
                            method.invoke(model, convertedValue);
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        return model;
    }

    /**
     * Binds form data from a servlet request's parameter map.
     *
     * @param modelClass the model class
     * @param parameterMap the request parameter map
     * @param <T> the model type
     * @return a new instance with bound values
     */
    public static <T> T bindFromParameterMap(Class<T> modelClass, Map<String, String[]> parameterMap) {
        Map<String, String> formData = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                formData.put(entry.getKey(), values[0]);
            }
        }
        return bind(modelClass, formData);
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private static Object convert(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) {
            if (targetType.isPrimitive()) {
                if (targetType == boolean.class) return false;
                if (targetType == int.class) return 0;
                if (targetType == long.class) return 0L;
                if (targetType == double.class) return 0.0;
                if (targetType == float.class) return 0.0f;
            }
            return null;
        }

        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
        if (targetType == boolean.class || targetType == Boolean.class) {
            return "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equals(value);
        }
        if (targetType == BigDecimal.class) return new BigDecimal(value);
        if (targetType == LocalDate.class) return LocalDate.parse(value);
        if (targetType == LocalTime.class) return LocalTime.parse(value);
        if (targetType == LocalDateTime.class) {
            // Handle both "T" separator and space separator
            String normalized = value.contains("T") ? value : value.replace(" ", "T");
            return LocalDateTime.parse(normalized);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<Enum>) targetType, value);
            return enumValue;
        }

        return value;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
