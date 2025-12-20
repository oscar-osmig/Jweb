package com.osmig.Jweb.framework.forms;

import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Elements;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Helper class to create a concrete Style type for form styling.
 */
class StyleBuilder extends Style<StyleBuilder> {}

/**
 * Fluent form builder for creating HTML forms with a clean DSL.
 *
 * <p>Example - Simple contact form:</p>
 * <pre>
 * Form.create()
 *     .action("/contact")
 *     .method("POST")
 *     .text("name", f -> f.label("Name").placeholder("John Doe").required())
 *     .email("email", f -> f.label("Email").placeholder("john@example.com").required())
 *     .textarea("message", f -> f.label("Message").rows(5).required())
 *     .submit("Send Message")
 *     .build()
 * </pre>
 *
 * <p>Example - Login form with styling:</p>
 * <pre>
 * Form.create()
 *     .action("/login")
 *     .method("POST")
 *     .style(s -> s.flexCol().gap(rem(1)))
 *     .email("email", f -> f.label("Email").required())
 *     .password("password", f -> f.label("Password").required())
 *     .checkbox("remember", f -> f.label("Remember me"))
 *     .submit("Sign In")
 *     .build()
 * </pre>
 *
 * <p>Example - Form with event handlers:</p>
 * <pre>
 * Form.create()
 *     .onSubmit(e -> {
 *         e.preventDefault();
 *         handleSubmit(e.formData());
 *     })
 *     .text("search", f -> f.placeholder("Search..."))
 *     .submit("Search")
 *     .build()
 * </pre>
 */
public class Form {

    private final Attributes formAttrs = new Attributes();
    private final List<Element> fields = new ArrayList<>();
    private Style<?> formStyle;

    private Form() {}

    /**
     * Creates a new Form builder.
     */
    public static Form create() {
        return new Form();
    }

    // ==================== Form Attributes ====================

    /**
     * Sets the form action URL.
     */
    public Form action(String url) {
        formAttrs.action(url);
        return this;
    }

    /**
     * Sets the form method (GET or POST).
     */
    public Form method(String method) {
        formAttrs.method(method);
        return this;
    }

    /**
     * Sets the form ID.
     */
    public Form id(String id) {
        formAttrs.id(id);
        return this;
    }

    /**
     * Sets the form CSS class.
     */
    public Form class_(String className) {
        formAttrs.class_(className);
        return this;
    }

    /**
     * Applies inline styles to the form.
     */
    public Form style(Consumer<StyleBuilder> styleBuilder) {
        StyleBuilder s = new StyleBuilder();
        styleBuilder.accept(s);
        formAttrs.style(s.build());
        return this;
    }

    /**
     * Registers a submit event handler.
     */
    public Form onSubmit(Consumer<Event> handler) {
        formAttrs.onSubmit(handler);
        return this;
    }

    /**
     * Sets the form encoding type.
     */
    public Form enctype(String enctype) {
        formAttrs.set("enctype", enctype);
        return this;
    }

    /**
     * Sets multipart form encoding (for file uploads).
     */
    public Form multipart() {
        return enctype("multipart/form-data");
    }

    /**
     * Disables browser validation.
     */
    public Form noValidate() {
        formAttrs.set("novalidate", null);
        return this;
    }

    // ==================== Input Fields ====================

    /**
     * Adds a text input field.
     */
    public Form text(String name, Consumer<Field> config) {
        fields.add(createField("text", name, config));
        return this;
    }

    /**
     * Adds a text input field with default config.
     */
    public Form text(String name) {
        return text(name, f -> {});
    }

    /**
     * Adds an email input field.
     */
    public Form email(String name, Consumer<Field> config) {
        fields.add(createField("email", name, config));
        return this;
    }

    /**
     * Adds an email input field with default config.
     */
    public Form email(String name) {
        return email(name, f -> {});
    }

    /**
     * Adds a password input field.
     */
    public Form password(String name, Consumer<Field> config) {
        fields.add(createField("password", name, config));
        return this;
    }

    /**
     * Adds a password input field with default config.
     */
    public Form password(String name) {
        return password(name, f -> {});
    }

    /**
     * Adds a number input field.
     */
    public Form number(String name, Consumer<Field> config) {
        fields.add(createField("number", name, config));
        return this;
    }

    /**
     * Adds a number input field with default config.
     */
    public Form number(String name) {
        return number(name, f -> {});
    }

    /**
     * Adds a tel (phone) input field.
     */
    public Form tel(String name, Consumer<Field> config) {
        fields.add(createField("tel", name, config));
        return this;
    }

    /**
     * Adds a URL input field.
     */
    public Form url(String name, Consumer<Field> config) {
        fields.add(createField("url", name, config));
        return this;
    }

    /**
     * Adds a date input field.
     */
    public Form date(String name, Consumer<Field> config) {
        fields.add(createField("date", name, config));
        return this;
    }

    /**
     * Adds a time input field.
     */
    public Form time(String name, Consumer<Field> config) {
        fields.add(createField("time", name, config));
        return this;
    }

    /**
     * Adds a datetime-local input field.
     */
    public Form datetime(String name, Consumer<Field> config) {
        fields.add(createField("datetime-local", name, config));
        return this;
    }

    /**
     * Adds a color input field.
     */
    public Form color(String name, Consumer<Field> config) {
        fields.add(createField("color", name, config));
        return this;
    }

    /**
     * Adds a range (slider) input field.
     */
    public Form range(String name, Consumer<Field> config) {
        fields.add(createField("range", name, config));
        return this;
    }

    /**
     * Adds a file input field.
     */
    public Form file(String name, Consumer<Field> config) {
        fields.add(createField("file", name, config));
        return this;
    }

    /**
     * Adds a hidden input field.
     */
    public Form hidden(String name, String value) {
        fields.add(input(attrs().type("hidden").name(name).value(value)));
        return this;
    }

    // ==================== Checkbox and Radio ====================

    /**
     * Adds a checkbox field.
     */
    public Form checkbox(String name, Consumer<Field> config) {
        Field field = new Field("checkbox", name);
        config.accept(field);
        fields.add(field.buildCheckbox());
        return this;
    }

    /**
     * Adds a radio button group.
     */
    public Form radio(String name, Consumer<RadioGroup> config) {
        RadioGroup group = new RadioGroup(name);
        config.accept(group);
        fields.add(group.build());
        return this;
    }

    // ==================== Textarea ====================

    /**
     * Adds a textarea field.
     */
    public Form textarea(String name, Consumer<Field> config) {
        Field field = new Field("textarea", name);
        config.accept(field);
        fields.add(field.buildTextarea());
        return this;
    }

    /**
     * Adds a textarea field with default config.
     */
    public Form textarea(String name) {
        return textarea(name, f -> {});
    }

    // ==================== Select ====================

    /**
     * Adds a select dropdown.
     */
    public Form select(String name, Consumer<SelectField> config) {
        SelectField select = new SelectField(name);
        config.accept(select);
        fields.add(select.build());
        return this;
    }

    // ==================== Buttons ====================

    /**
     * Adds a submit button.
     */
    public Form submit(String buttonText) {
        fields.add(Elements.button(attrs().type("submit"), Elements.text(buttonText)));
        return this;
    }

    /**
     * Adds a submit button with custom config.
     */
    public Form submit(String text, Consumer<ButtonConfig> config) {
        ButtonConfig btn = new ButtonConfig("submit", text);
        config.accept(btn);
        fields.add(btn.build());
        return this;
    }

    /**
     * Adds a reset button.
     */
    public Form reset(String buttonText) {
        fields.add(Elements.button(attrs().type("reset"), Elements.text(buttonText)));
        return this;
    }

    /**
     * Adds a regular button.
     */
    public Form button(String buttonText, Consumer<Event> onClick) {
        fields.add(Elements.button(attrs().type("button").onClick(onClick), Elements.text(buttonText)));
        return this;
    }

    // ==================== Custom Elements ====================

    /**
     * Adds a custom element to the form.
     */
    public Form add(Element element) {
        fields.add(element);
        return this;
    }

    /**
     * Adds a fieldset with legend.
     */
    public Form fieldset(String legendText, Consumer<Form> config) {
        Form inner = new Form();
        config.accept(inner);
        fields.add(Elements.fieldset(
            Elements.legend(Elements.text(legendText)),
            fragment(inner.fields.toArray())
        ));
        return this;
    }

    // ==================== Build ====================

    /**
     * Builds the form element.
     */
    public Element build() {
        return form(formAttrs, fields.toArray());
    }

    // ==================== Helper Methods ====================

    private Element createField(String type, String name, Consumer<Field> config) {
        Field field = new Field(type, name);
        config.accept(field);
        return field.build();
    }

    // ==================== Field Builder ====================

    /**
     * Configuration for a form field (input, textarea).
     */
    public static class Field {
        private final String type;
        private final String name;
        private String labelText;
        private String placeholder;
        private String value;
        private String helpText;
        private boolean required;
        private boolean disabled;
        private boolean readonly;
        private boolean autofocus;
        private String pattern;
        private Integer minLength;
        private Integer maxLength;
        private String min;
        private String max;
        private String step;
        private Integer rows;
        private Integer cols;
        private String accept; // for file inputs
        private boolean multiple; // for file/email inputs
        private String autocomplete;
        private Consumer<Event> onChange;
        private Consumer<Event> onInput;
        private Consumer<StyleBuilder> inputStyle;
        private Consumer<StyleBuilder> labelStyle;
        private String id;
        private String className;

        Field(String type, String name) {
            this.type = type;
            this.name = name;
            this.id = name; // default id = name
        }

        public Field label(String text) { this.labelText = text; return this; }
        public Field placeholder(String text) { this.placeholder = text; return this; }
        public Field value(String value) { this.value = value; return this; }
        public Field help(String text) { this.helpText = text; return this; }
        public Field required() { this.required = true; return this; }
        public Field disabled() { this.disabled = true; return this; }
        public Field readonly() { this.readonly = true; return this; }
        public Field autofocus() { this.autofocus = true; return this; }
        public Field pattern(String regex) { this.pattern = regex; return this; }
        public Field minLength(int len) { this.minLength = len; return this; }
        public Field maxLength(int len) { this.maxLength = len; return this; }
        public Field min(String val) { this.min = val; return this; }
        public Field max(String val) { this.max = val; return this; }
        public Field min(int val) { this.min = String.valueOf(val); return this; }
        public Field max(int val) { this.max = String.valueOf(val); return this; }
        public Field step(String val) { this.step = val; return this; }
        public Field step(int val) { this.step = String.valueOf(val); return this; }
        public Field rows(int val) { this.rows = val; return this; }
        public Field cols(int val) { this.cols = val; return this; }
        public Field accept(String val) { this.accept = val; return this; }
        public Field multiple() { this.multiple = true; return this; }
        public Field autocomplete(String val) { this.autocomplete = val; return this; }
        public Field onChange(Consumer<Event> handler) { this.onChange = handler; return this; }
        public Field onInput(Consumer<Event> handler) { this.onInput = handler; return this; }
        public Field id(String val) { this.id = val; return this; }
        public Field class_(String val) { this.className = val; return this; }

        public Field inputStyle(Consumer<StyleBuilder> style) { this.inputStyle = style; return this; }
        public Field labelStyle(Consumer<StyleBuilder> style) { this.labelStyle = style; return this; }

        Element build() {
            List<Element> elements = new ArrayList<>();

            // Label
            if (labelText != null) {
                Attributes labelAttrs = attrs().for_(id);
                if (labelStyle != null) {
                    StyleBuilder s = new StyleBuilder();
                    labelStyle.accept(s);
                    labelAttrs.style(s.build());
                }
                elements.add(Elements.label(labelAttrs, Elements.text(labelText)));
            }

            // Input
            Attributes inputAttrs = attrs().type(type).name(name).id(id);
            if (placeholder != null) inputAttrs.placeholder(placeholder);
            if (value != null) inputAttrs.value(value);
            if (required) inputAttrs.required();
            if (disabled) inputAttrs.disabled();
            if (readonly) inputAttrs.readonly();
            if (autofocus) inputAttrs.autofocus();
            if (pattern != null) inputAttrs.set("pattern", pattern);
            if (minLength != null) inputAttrs.set("minlength", String.valueOf(minLength));
            if (maxLength != null) inputAttrs.set("maxlength", String.valueOf(maxLength));
            if (min != null) inputAttrs.set("min", min);
            if (max != null) inputAttrs.set("max", max);
            if (step != null) inputAttrs.set("step", step);
            if (accept != null) inputAttrs.set("accept", accept);
            if (multiple) inputAttrs.set("multiple", null);
            if (autocomplete != null) inputAttrs.set("autocomplete", autocomplete);
            if (className != null) inputAttrs.class_(className);
            if (onChange != null) inputAttrs.onChange(onChange);
            if (onInput != null) inputAttrs.onInput(onInput);

            if (inputStyle != null) {
                StyleBuilder s = new StyleBuilder();
                inputStyle.accept(s);
                inputAttrs.style(s.build());
            }

            elements.add(input(inputAttrs));

            // Help text
            if (helpText != null) {
                elements.add(small(Elements.text(helpText)));
            }

            return div(elements.toArray());
        }

        Element buildTextarea() {
            List<Element> elements = new ArrayList<>();

            // Label
            if (labelText != null) {
                elements.add(Elements.label(attrs().for_(id), Elements.text(labelText)));
            }

            // Textarea
            Attributes textareaAttrs = attrs().name(name).id(id);
            if (placeholder != null) textareaAttrs.placeholder(placeholder);
            if (required) textareaAttrs.required();
            if (disabled) textareaAttrs.disabled();
            if (readonly) textareaAttrs.readonly();
            if (rows != null) textareaAttrs.set("rows", String.valueOf(rows));
            if (cols != null) textareaAttrs.set("cols", String.valueOf(cols));
            if (minLength != null) textareaAttrs.set("minlength", String.valueOf(minLength));
            if (maxLength != null) textareaAttrs.set("maxlength", String.valueOf(maxLength));
            if (className != null) textareaAttrs.class_(className);
            if (onChange != null) textareaAttrs.onChange(onChange);
            if (onInput != null) textareaAttrs.onInput(onInput);

            elements.add(Elements.textarea(textareaAttrs, value != null ? value : ""));

            if (helpText != null) {
                elements.add(small(Elements.text(helpText)));
            }

            return div(elements.toArray());
        }

        Element buildCheckbox() {
            Attributes inputAttrs = attrs().type("checkbox").name(name).id(id);
            if (value != null) inputAttrs.value(value);
            if (required) inputAttrs.required();
            if (disabled) inputAttrs.disabled();
            if (onChange != null) inputAttrs.onChange(onChange);

            if (labelText != null) {
                return div(
                    input(inputAttrs),
                    Elements.label(attrs().for_(id), Elements.text(labelText))
                );
            }
            return input(inputAttrs);
        }
    }

    // ==================== Radio Group Builder ====================

    /**
     * Configuration for a radio button group.
     */
    public static class RadioGroup {
        private final String name;
        private String labelText;
        private final List<RadioOption> options = new ArrayList<>();

        RadioGroup(String name) {
            this.name = name;
        }

        public RadioGroup label(String labelText) { this.labelText = labelText; return this; }

        public RadioGroup option(String value, String optLabel) {
            options.add(new RadioOption(value, optLabel, false));
            return this;
        }

        public RadioGroup option(String value, String optLabel, boolean checked) {
            options.add(new RadioOption(value, optLabel, checked));
            return this;
        }

        Element build() {
            List<Element> elements = new ArrayList<>();

            if (labelText != null) {
                elements.add(span(Elements.text(labelText)));
            }

            for (RadioOption opt : options) {
                String optId = name + "-" + opt.value;
                Attributes inputAttrs = attrs().type("radio").name(name).id(optId).value(opt.value);
                if (opt.checked) inputAttrs.checked();

                elements.add(div(
                    input(inputAttrs),
                    Elements.label(attrs().for_(optId), Elements.text(opt.optLabel))
                ));
            }

            return div(elements.toArray());
        }

        private record RadioOption(String value, String optLabel, boolean checked) {}
    }

    // ==================== Select Builder ====================

    /**
     * Configuration for a select dropdown.
     */
    public static class SelectField {
        private final String name;
        private String labelText;
        private String placeholder;
        private boolean required;
        private boolean disabled;
        private boolean multiple;
        private final List<SelectOption> options = new ArrayList<>();
        private Consumer<Event> onChange;

        SelectField(String name) {
            this.name = name;
        }

        public SelectField label(String labelText) { this.labelText = labelText; return this; }
        public SelectField placeholder(String placeholderText) { this.placeholder = placeholderText; return this; }
        public SelectField required() { this.required = true; return this; }
        public SelectField disabled() { this.disabled = true; return this; }
        public SelectField multiple() { this.multiple = true; return this; }
        public SelectField onChange(Consumer<Event> handler) { this.onChange = handler; return this; }

        public SelectField option(String value, String optText) {
            options.add(new SelectOption(value, optText, false));
            return this;
        }

        public SelectField option(String value, String optText, boolean selected) {
            options.add(new SelectOption(value, optText, selected));
            return this;
        }

        Element build() {
            List<Element> elements = new ArrayList<>();

            if (labelText != null) {
                elements.add(Elements.label(attrs().for_(name), Elements.text(labelText)));
            }

            Attributes selectAttrs = attrs().name(name).id(name);
            if (required) selectAttrs.required();
            if (disabled) selectAttrs.disabled();
            if (multiple) selectAttrs.set("multiple", null);
            if (onChange != null) selectAttrs.onChange(onChange);

            List<Element> optionElements = new ArrayList<>();

            // Placeholder option
            if (placeholder != null) {
                optionElements.add(Elements.option(attrs().value("").disabled(), placeholder));
            }

            for (SelectOption opt : options) {
                Attributes optAttrs = attrs().value(opt.value);
                if (opt.selected) optAttrs.set("selected", null);
                optionElements.add(Elements.option(optAttrs, opt.optText));
            }

            elements.add(Elements.select(selectAttrs, optionElements.toArray()));

            return div(elements.toArray());
        }

        private record SelectOption(String value, String optText, boolean selected) {}
    }

    // ==================== Button Config ====================

    /**
     * Configuration for buttons.
     */
    public static class ButtonConfig {
        private final String type;
        private final String btnText;
        private String className;
        private boolean disabled;
        private Consumer<Event> onClick;
        private Consumer<StyleBuilder> style;

        ButtonConfig(String type, String btnText) {
            this.type = type;
            this.btnText = btnText;
        }

        public ButtonConfig class_(String className) { this.className = className; return this; }
        public ButtonConfig disabled() { this.disabled = true; return this; }
        public ButtonConfig onClick(Consumer<Event> handler) { this.onClick = handler; return this; }
        public ButtonConfig style(Consumer<StyleBuilder> style) { this.style = style; return this; }

        Element build() {
            Attributes attrs = attrs().type(type);
            if (className != null) attrs.class_(className);
            if (disabled) attrs.disabled();
            if (onClick != null) attrs.onClick(onClick);
            if (style != null) {
                StyleBuilder s = new StyleBuilder();
                style.accept(s);
                attrs.style(s.build());
            }
            return Elements.button(attrs, Elements.text(btnText));
        }
    }
}
