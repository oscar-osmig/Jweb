package jweb;

import com.osmig.Jweb.framework.vdom.VNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static jweb.Css.*;
import static jweb.El.*;

/**
 * The form system: a record is the form.
 *
 * <p>One record declares the fields, their types and their rules; the same
 * record renders the form, validates the submission and comes back as a typed
 * value. There is no second way to build a form.</p>
 *
 * <pre>{@code
 * public record Contact(
 *     @Form.Required String name,
 *     @Form.Required @Form.Email String email,
 *     @Form.Required @Form.Multiline(rows = 4) String message) {}
 *
 * // render — the CSRF hidden field comes from the current request
 * app.get("/contact", req -> page(
 *     form(Contact.class)
 *         .action("/contact/submit")
 *         .swapForm("/contact/submit", "#status")
 *         .field("message", f -> f.label("Message").placeholder("How can we help?"))
 *         .submit("Send message")));
 *
 * // handle — bind, then either use the value or re-render with the errors
 * app.post("/contact/submit", req -> {
 *     Form.Bound<Contact> bound = Form.bind(Contact.class, req);
 *     if (!bound.ok()) return form(Contact.class).action("/contact/submit").errors(bound).submit("Send message");
 *     store.save(bound.value());
 *     return p("Thanks!");
 * });
 * }</pre>
 *
 * <h2>Component type to input type</h2>
 * <table>
 *   <caption>How a record component becomes a control</caption>
 *   <tr><th>Java type</th><th>Control</th></tr>
 *   <tr><td>{@code String}</td><td>{@code <input type="text">}, or email/textarea with a hint</td></tr>
 *   <tr><td>{@code int}, {@code long}, {@code Integer}, {@code Long}</td><td>{@code <input type="number">}</td></tr>
 *   <tr><td>{@code double}, {@code float}, {@code BigDecimal}</td><td>{@code <input type="number" step="any">}</td></tr>
 *   <tr><td>{@code boolean}, {@code Boolean}</td><td>{@code <input type="checkbox">}</td></tr>
 *   <tr><td>an enum</td><td>{@code <select>} over its constants</td></tr>
 *   <tr><td>{@code LocalDate} / {@code LocalTime} / {@code LocalDateTime}</td><td>date / time / datetime-local</td></tr>
 *   <tr><td>{@link UploadedFile}</td><td>{@code <input type="file">}, and the form becomes multipart</td></tr>
 * </table>
 *
 * @param <T> the record type this form renders and binds
 */
public final class Form<T extends Record> implements Element {

    // ==================== Hints ====================

    /** The component must be present; {@link Validators#required()} enforces it. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Required {}

    /** Renders an email input and validates with {@link Validators#email()}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Email {}

    /** Renders a {@code <textarea>} instead of a one-line input. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Multiline {
        /** @return how many rows the textarea shows */
        int rows() default 4;
    }

    /** Overrides the label derived from the component name. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Label {
        /** @return the label text */
        String value();
    }

    /** Renders a password input. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Password {}

    /**
     * Bounds the text length, both as {@code minlength}/{@code maxlength} on the
     * control and with {@link Validators#minLength(int)} /
     * {@link Validators#maxLength(int)} on the server.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Length {
        /** @return the fewest characters accepted */
        int min() default 0;
        /** @return the most characters accepted */
        int max() default Integer.MAX_VALUE;
    }

    // ==================== State ====================

    private final Class<T> recordType;
    private final List<Field> fields = new ArrayList<>();
    private final Map<String, String> values = new LinkedHashMap<>();
    private final Attributes formAttrs = new Attributes();
    private ValidationResult errors = new ValidationResult();
    private String submitLabel = "Submit";
    private CsrfToken token;
    private boolean multipart;

    private Form(Class<T> recordType) {
        this.recordType = recordType;
        formAttrs.method("post").cls("jweb-form");
        for (RecordComponent component : recordType.getRecordComponents()) {
            Field field = new Field(component);
            fields.add(field);
            if (field.type.equals("file")) multipart = true;
        }
    }

    /**
     * Starts a form for a record — the same thing {@code form(Contact.class)} does.
     *
     * @param recordType the record whose components are the fields
     * @param <T> the record type
     * @return the form builder
     */
    public static <T extends Record> Form<T> of(Class<T> recordType) {
        if (!recordType.isRecord()) {
            throw new IllegalArgumentException(
                recordType.getName() + " is not a record — a form is a record: "
                    + "record Contact(@Form.Required String name, …) {}");
        }
        return new Form<>(recordType);
    }

    // ==================== Form-level configuration ====================

    /**
     * Sets the URL the form posts to.
     *
     * @param url the action URL
     * @return this form
     */
    public Form<T> action(String url) { formAttrs.action(url); return this; }

    /**
     * Sets the HTTP method (POST by default).
     *
     * @param method the method
     * @return this form
     */
    public Form<T> method(String method) { formAttrs.method(method); return this; }

    /**
     * Sets the form's id.
     *
     * @param id the element id
     * @return this form
     */
    public Form<T> id(String id) { formAttrs.id(id); return this; }

    /**
     * Adds a CSS class alongside {@code jweb-form}.
     *
     * @param className the class to add
     * @return this form
     */
    public Form<T> cls(String className) { formAttrs.addClass(className); return this; }

    /**
     * Progressive enhancement: submits over fetch and swaps the response into
     * {@code targetSelector}, while the plain {@link #action(String)} still
     * works with JavaScript off.
     *
     * @param actionUrl the URL to post to
     * @param targetSelector the element the response fragment fills
     * @return this form
     */
    public Form<T> swapForm(String actionUrl, String targetSelector) {
        formAttrs.swapForm(actionUrl, targetSelector);
        if (formAttrs.get("action") == null) formAttrs.action(actionUrl);
        return this;
    }

    /**
     * Sets the submit button's text.
     *
     * @param label the button text
     * @return this form
     */
    public Form<T> submit(String label) { this.submitLabel = label; return this; }

    /**
     * Overrides one field: label, placeholder, help text, control type, rows…
     *
     * <pre>
     * form(Contact.class).field("email", f -&gt; f.label("Your email").placeholder("you@example.com"))
     * </pre>
     *
     * @param name the record component's name
     * @param config the override
     * @return this form
     */
    public Form<T> field(String name, UnaryOperator<Field> config) {
        Field field = fields.stream().filter(f -> f.name.equals(name)).findFirst().orElseThrow(
            () -> new IllegalArgumentException(
                recordType.getSimpleName() + " has no component named '" + name + "'"));
        config.apply(field);
        if (field.type.equals("file")) multipart = true;
        return this;
    }

    /**
     * Re-renders the form with the values the user submitted.
     *
     * @param submitted the raw submitted values, by component name
     * @return this form
     */
    public Form<T> values(Map<String, String> submitted) {
        if (submitted != null) values.putAll(submitted);
        return this;
    }

    /**
     * Re-renders the form filled in from an existing record — an edit form.
     *
     * @param value the record holding the current values
     * @return this form
     */
    public Form<T> values(T value) {
        if (value != null) values.putAll(read(value));
        return this;
    }

    /**
     * Renders the validation messages: one next to each field that failed
     * (with {@code aria-invalid}) plus a summary above the fields.
     *
     * @param result the validation result
     * @return this form
     */
    public Form<T> errors(ValidationResult result) {
        if (result != null) this.errors = result;
        return this;
    }

    /**
     * Renders the errors <em>and</em> the submitted values from a failed
     * {@link #bind(Class, Request)} — the whole re-render in one call.
     *
     * @param bound the binding result
     * @return this form
     */
    public Form<T> errors(Bound<T> bound) {
        if (bound != null) {
            errors(bound.errors());
            values(bound.submitted());
        }
        return this;
    }

    /**
     * Uses an explicit CSRF token instead of the one from the current request.
     *
     * @param csrfToken the token to embed
     * @return this form
     */
    public Form<T> csrf(CsrfToken csrfToken) { this.token = csrfToken; return this; }

    // ==================== Rendering ====================

    @Override
    public VNode toVNode() {
        // The form's base rules ride the page's collected stylesheet, so no
        // page has to remember to include them (Form.styles() stays available
        // for a render outside any request context).
        com.osmig.Jweb.framework.styles.PageStyles.add(styles());
        Attributes attributes = new Attributes(formAttrs.toMap());
        if (multipart) attributes.enctype("multipart/form-data");

        List<Object> children = new ArrayList<>();
        CsrfToken csrfToken = token != null ? token : currentToken();
        if (csrfToken != null) {
            children.add(input(type("hidden"),
                name(CsrfToken.TOKEN_PARAM_NAME),
                value(csrfToken.getValue())));
        }
        if (errors.hasErrors()) children.add(summary());
        for (Field field : fields) children.add(field.render(values.get(field.name), errors));
        children.add(button(type("submit"), El.cls("jweb-submit"), submitLabel));

        return form(attributes, children.toArray()).toVNode();
    }

    private Element summary() {
        List<String> messages = errors.getAllMessages();
        return div(El.cls("jweb-errors"), role("alert"),
            ul(each(messages, El::li)));
    }

    /**
     * A drop-in stylesheet for the classes this form emits — {@code jweb-form},
     * {@code jweb-field}, {@code jweb-label}, {@code jweb-control},
     * {@code jweb-help}, {@code jweb-error}, {@code jweb-errors},
     * {@code jweb-submit}. A rendering form adds it to the page's collected stylesheet
     * by itself; call this only for a render outside any request context.
     *
     * @return the CSS text
     */
    public static String styles() {
        return stylesheet()
            .rule(".jweb-form", style().display(flex).flexDirection(column).gap(rem(1)))
            .rule(".jweb-field", style().display(flex).flexDirection(column).gap(rem(0.35)))
            .rule(".jweb-label", style().fontSize(rem(0.875)).fontWeight(500))
            .rule(".jweb-control", style()
                .width(percent(100))
                .padding(rem(0.65), rem(0.75))
                .border(px(1), solid, hex("#d1d5db"))
                .borderRadius(px(6))
                .fontSize(rem(1))
                .fontFamily("inherit")
                .boxSizing(borderBox))
            .rule(".jweb-control:focus", style().outline(none).borderColor(hex("#6366f1")))
            .rule(".jweb-field-checkbox", style().flexDirection(row).alignItems(center).gap(rem(0.5)))
            .rule(".jweb-field-checkbox .jweb-control", style().width(auto))
            .rule(".jweb-help", style().fontSize(rem(0.8)).color(hex("#6b7280")))
            .rule(".jweb-error", style().fontSize(rem(0.8)).color(hex("#b91c1c")))
            .rule(".jweb-control[aria-invalid=\"true\"]", style().borderColor(hex("#b91c1c")))
            .rule(".jweb-errors", style()
                .padding(rem(0.75))
                .borderRadius(px(6))
                .backgroundColor(hex("#fee2e2"))
                .color(hex("#991b1b"))
                .fontSize(rem(0.875)))
            .rule(".jweb-errors ul", style().margin(zero).paddingLeft(rem(1.25)))
            .rule(".jweb-submit", style()
                .padding(rem(0.65), rem(1))
                .border(none)
                .borderRadius(px(6))
                .backgroundColor(hex("#6366f1"))
                .color(white)
                .fontSize(rem(1))
                .fontWeight(600)
                .cursor(pointer))
            .build();
    }

    // ==================== Server side ====================

    /**
     * Reads the submitted values, validates them, and — when they pass —
     * builds the record.
     *
     * @param recordType the record to bind
     * @param request the request carrying the form data
     * @param <T> the record type
     * @return the binding result: the value, the errors, and the raw input
     */
    public static <T extends Record> Bound<T> bind(Class<T> recordType, Request request) {
        Map<String, String> submitted = new LinkedHashMap<>();
        Map<String, UploadedFile> files = new LinkedHashMap<>();
        boolean multipartRequest = FileUpload.isMultipart(request);
        for (RecordComponent component : recordType.getRecordComponents()) {
            if (component.getType() == UploadedFile.class) {
                UploadedFile file = multipartRequest
                    ? FileUpload.getFileOptional(request, component.getName()).orElse(null)
                    : null;
                if (file != null && !file.isEmpty()) files.put(component.getName(), file);
                submitted.put(component.getName(), file == null || file.isEmpty()
                    ? "" : file.getFilename());
            } else {
                String raw = request.formParam(component.getName());
                submitted.put(component.getName(), raw == null ? "" : raw);
            }
        }

        ValidationResult result = validate(recordType, submitted);
        if (result.hasErrors()) return new Bound<>(null, result, submitted);

        try {
            return new Bound<>(construct(recordType, submitted, files), result, submitted);
        } catch (RuntimeException e) {
            return new Bound<>(null,
                result.addError("_form", "Could not read the submitted form: " + e.getMessage()),
                submitted);
        }
    }

    /**
     * Validates raw values against a record's components and hints, without
     * building the record.
     *
     * @param recordType the record describing the fields
     * @param values the raw values, by component name
     * @param <T> the record type
     * @return the validation result, keyed by component name
     */
    public static <T extends Record> ValidationResult validate(Class<T> recordType,
                                                               Map<String, String> values) {
        ValidationResult result = new ValidationResult();
        for (RecordComponent component : recordType.getRecordComponents()) {
            Field field = new Field(component);
            String name = component.getName();
            String raw = values == null ? null : values.get(name);
            String value = raw == null ? "" : raw.trim();

            if (field.required && value.isEmpty()) {
                check(result, name, field.label, Validators.required(), value);
                continue;
            }
            if (value.isEmpty()) continue;
            if (field.email) check(result, name, field.label, Validators.email(), value);
            if (field.minLength > 0) {
                check(result, name, field.label, Validators.minLength(field.minLength), value);
            }
            if (field.maxLength < Integer.MAX_VALUE) {
                check(result, name, field.label, Validators.maxLength(field.maxLength), value);
            }
            String typeError = parseError(component.getType(), value, field.label);
            if (typeError != null) result.addError(name, typeError);
        }
        return result;
    }

    private static void check(ValidationResult into, String name, String label,
                              Validator<String> validator, String value) {
        into.addErrors(name, validator.validate(value, label).getAllMessages());
    }

    private static String parseError(Class<?> type, String value, String label) {
        try {
            convert(type, value, null);
            return null;
        } catch (RuntimeException e) {
            if (type.isEnum()) return label + " must be one of the allowed values";
            if (type == LocalDate.class) return label + " must be a date (YYYY-MM-DD)";
            if (type == LocalTime.class || type == LocalDateTime.class) return label + " must be a valid time";
            return label + " must be a number";
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Record> T construct(Class<T> recordType,
                                                  Map<String, String> values,
                                                  Map<String, UploadedFile> files) {
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            String raw = values.get(components[i].getName());
            args[i] = convert(types[i], raw == null ? "" : raw.trim(),
                files.get(components[i].getName()));
        }
        try {
            java.lang.reflect.Constructor<T> canonical = recordType.getDeclaredConstructor(types);
            canonical.setAccessible(true);
            return canonical.newInstance(args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("Cannot build " + recordType.getName()
                + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convert(Class<?> type, String value, UploadedFile file) {
        if (type == UploadedFile.class) return file;
        boolean empty = value == null || value.isEmpty();
        if (type == String.class) return value;
        if (type == boolean.class || type == Boolean.class) {
            boolean on = "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equals(value);
            return type == boolean.class ? on : (empty ? Boolean.FALSE : on);
        }
        if (empty) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0d;
            if (type == float.class) return 0.0f;
            return null;
        }
        if (type == int.class || type == Integer.class) return Integer.valueOf(value);
        if (type == long.class || type == Long.class) return Long.valueOf(value);
        if (type == double.class || type == Double.class) return Double.valueOf(value);
        if (type == float.class || type == Float.class) return Float.valueOf(value);
        if (type == BigDecimal.class) return new BigDecimal(value);
        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == LocalTime.class) return LocalTime.parse(value);
        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value.contains("T") ? value : value.replace(' ', 'T'));
        }
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);
        return value;
    }

    private static Map<String, String> read(Record value) {
        Map<String, String> out = new LinkedHashMap<>();
        for (RecordComponent component : value.getClass().getRecordComponents()) {
            try {
                java.lang.reflect.Method accessor = component.getAccessor();
                accessor.setAccessible(true);
                Object raw = accessor.invoke(value);
                out.put(component.getName(), raw == null ? "" : format(raw));
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new IllegalStateException("Cannot read " + component.getName()
                    + ": " + e.getMessage(), e);
            }
        }
        return out;
    }

    private static String format(Object raw) {
        if (raw instanceof Enum<?> constant) return constant.name();
        if (raw instanceof UploadedFile file) return file.isEmpty() ? "" : file.getFilename();
        return String.valueOf(raw);
    }

    /**
     * The CSRF token of the request being handled, or {@code null} when there
     * is none (a unit test, a static render) — in which case the form simply
     * carries no token field.
     */
    private static CsrfToken currentToken() {
        Object attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet)) {
            return null;
        }
        return Csrf.getOrCreateToken(new Request(servlet.getRequest()));
    }

    // ==================== Bound ====================

    /**
     * What {@link #bind(Class, Request)} hands back: the record when the
     * submission was valid, the messages when it was not, and always the raw
     * input so the form can be re-rendered as the user typed it.
     *
     * @param value the bound record, or {@code null} when validation failed
     * @param errors the validation messages, keyed by component name
     * @param submitted the raw submitted values
     * @param <T> the record type
     */
    public record Bound<T>(T value, ValidationResult errors, Map<String, String> submitted) {

        /** @return true when the submission was valid and {@link #value()} is set */
        public boolean ok() { return value != null && errors.isValid(); }
    }

    // ==================== Field ====================

    /**
     * One field of the form.
     *
     * <p>{@link Form#field(String, UnaryOperator)} overrides how it is
     * <em>presented</em> — label, placeholder, help text, control type. The
     * rules (required, email, length) stay on the record's annotations, so
     * the browser and the server can never disagree about them.</p>
     */
    public static final class Field {
        private final String name;
        private final Class<?> javaType;
        private String label;
        private String type;
        private String placeholder;
        private String help;
        private String accept;
        private String autocomplete;
        private boolean required;
        private boolean email;
        private int rows;
        private int minLength;
        private int maxLength = Integer.MAX_VALUE;
        private List<String> options = List.of();

        private Field(RecordComponent component) {
            this.name = component.getName();
            this.javaType = component.getType();
            this.label = component.isAnnotationPresent(Label.class)
                ? component.getAnnotation(Label.class).value()
                : humanize(component.getName());
            this.required = component.isAnnotationPresent(Required.class);
            this.email = component.isAnnotationPresent(Email.class);
            this.rows = component.isAnnotationPresent(Multiline.class)
                ? component.getAnnotation(Multiline.class).rows()
                : 0;
            if (component.isAnnotationPresent(Length.class)) {
                Length length = component.getAnnotation(Length.class);
                this.minLength = length.min();
                this.maxLength = length.max();
            }
            this.type = controlType(component);
            if (javaType.isEnum()) {
                options = new ArrayList<>();
                for (Object constant : javaType.getEnumConstants()) {
                    options.add(((Enum<?>) constant).name());
                }
            }
        }

        private static String controlType(RecordComponent component) {
            Class<?> type = component.getType();
            if (component.isAnnotationPresent(Multiline.class)) return "textarea";
            if (component.isAnnotationPresent(Password.class)) return "password";
            if (component.isAnnotationPresent(Email.class)) return "email";
            if (type == UploadedFile.class) return "file";
            if (type.isEnum()) return "select";
            if (type == boolean.class || type == Boolean.class) return "checkbox";
            if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class) return "number";
            if (type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == BigDecimal.class) return "decimal";
            if (type == LocalDate.class) return "date";
            if (type == LocalTime.class) return "time";
            if (type == LocalDateTime.class) return "datetime-local";
            return "text";
        }

        /** @param text the label text @return this field */
        public Field label(String text) { this.label = text; return this; }
        /** @param text the placeholder @return this field */
        public Field placeholder(String text) { this.placeholder = text; return this; }
        /** @param text help text rendered under the control @return this field */
        public Field help(String text) { this.help = text; return this; }
        /** @param inputType an explicit input type ("tel", "url", "search"…) @return this field */
        public Field type(String inputType) { this.type = inputType; return this; }
        /** @param count textarea rows (also switches the control to a textarea) @return this field */
        public Field rows(int count) { this.rows = count; this.type = "textarea"; return this; }
        /** @param mimeTypes the accept attribute of a file input @return this field */
        public Field accept(String mimeTypes) { this.accept = mimeTypes; return this; }
        /** @param value the autocomplete attribute @return this field */
        public Field autocomplete(String value) { this.autocomplete = value; return this; }
        /** @param choices the select options, replacing an enum's constants @return this field */
        public Field options(String... choices) { this.options = List.of(choices); return this; }

        Element render(String value, ValidationResult errors) {
            boolean invalid = errors.hasErrors(name);
            String errorId = name + "-error";
            boolean checkbox = "checkbox".equals(type);

            Attributes control = new Attributes()
                .cls("jweb-control").name(name).id(name)
                .required(required);
            if (invalid) control.aria("invalid", "true").aria("describedby", errorId);
            if (placeholder != null) control.placeholder(placeholder);
            if (autocomplete != null) control.autocomplete(autocomplete);
            if (minLength > 0) control.minlength(minLength);
            if (maxLength < Integer.MAX_VALUE) control.maxlength(maxLength);
            boolean hasValue = value != null && !value.isEmpty();

            Element field = switch (type) {
                case "textarea" -> textarea(control.rows(rows > 0 ? rows : 4), hasValue ? value : "");
                case "select" -> select(control, options.stream()
                    .map(choice -> (Element) option(
                        attrs().value(choice).selected(choice.equals(value)), choice))
                    .toList());
                case "checkbox" -> input(control.type("checkbox").value("true")
                    .checked("true".equalsIgnoreCase(value)));
                case "file" -> input(accept == null ? control.type("file") : control.type("file").accept(accept));
                case "decimal" -> input(hasValue
                    ? control.type("number").step("any").value(value)
                    : control.type("number").step("any"));
                default -> input(hasValue ? control.type(type).value(value) : control.type(type));
            };

            List<Object> parts = new ArrayList<>();
            Element labelElement = El.label(for_(name), El.cls("jweb-label"), label);
            if (checkbox) {
                parts.add(field);
                parts.add(labelElement);
            } else {
                parts.add(labelElement);
                parts.add(field);
            }
            if (help != null) parts.add(small(El.cls("jweb-help"), help));
            if (invalid) {
                parts.add(small(El.id(errorId), El.cls("jweb-error"), errors.getFirstError(name)));
            }

            return div(classes("jweb-field",
                    when(checkbox, "jweb-field-checkbox"),
                    when(invalid, "jweb-field-invalid")),
                parts.toArray());
        }

        /** {@code firstName} reads as "First name" until the author says otherwise. */
        private static String humanize(String componentName) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < componentName.length(); i++) {
                char c = componentName.charAt(i);
                if (i == 0) out.append(Character.toUpperCase(c));
                else if (Character.isUpperCase(c)) out.append(' ').append(Character.toLowerCase(c));
                else out.append(c);
            }
            return out.toString();
        }
    }
}
