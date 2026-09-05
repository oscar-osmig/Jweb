package com.osmig.Jweb.framework.attributes;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.js.Actions.Action;
import com.osmig.Jweb.framework.ref.Ref;
import com.osmig.Jweb.framework.transition.TransitionBuilder;
import com.osmig.Jweb.framework.transition.TransitionBuilder.TransitionReceiver;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * The complete HTML attribute surface, defined once and shared by every fluent
 * builder that can carry attributes.
 *
 * <p>Two builder styles exist in JWeb and both need the same attributes:</p>
 * <pre>
 * div(class_("card"), id("x"))          // varargs style, via Attr statics
 * div().id("x").class_("card")          // builder style, via Tag
 * div(attrs().id("x").rel("me"))        // Attributes builder
 * </pre>
 *
 * <p>Rather than copying ~200 setters onto each builder (three copies that drift),
 * every setter lives here as a {@code default} method and each builder simply
 * implements the two primitives below. The self-type parameter {@code SELF} makes
 * every inherited method return the implementor's own type, so a {@code Tag} chain
 * stays a {@code Tag} and an {@code Attributes} chain stays an {@code Attributes}.</p>
 *
 * <pre>
 * public class Tag implements HtmlAttributes&lt;Tag&gt; { ... }
 * public class Attributes implements HtmlAttributes&lt;Attributes&gt; { ... }
 * </pre>
 *
 * <h2>Primitives</h2>
 * <ul>
 *   <li>{@link #set(String, String)} — the write primitive. Every default method
 *       here bottoms out in it (directly, or by composing other defaults).
 *       A {@code null} value marks a bare boolean attribute: {@code <input required>}.</li>
 *   <li>{@link #get(String)} — the read-back primitive, inherited from
 *       {@link TransitionReceiver}. Only the class-composition helpers
 *       ({@link #addClass(String)} and friends) and {@link #transition()} need it.</li>
 * </ul>
 *
 * <p>Element-shaped concerns (children, text, rendering) and builder infrastructure
 * (build, toMap, isEmpty) deliberately stay on the implementing classes.</p>
 *
 * @param <SELF> the implementing builder's own type, for correctly-typed chaining
 * @see Attributes
 * @see com.osmig.Jweb.framework.elements.Tag
 */
public interface HtmlAttributes<SELF extends HtmlAttributes<SELF>> extends TransitionReceiver {

    /**
     * Sets any HTML attribute by name — the single write primitive that every
     * other method in this interface is built on.
     *
     * @param name the attribute name
     * @param value the attribute value ({@code null} renders a bare boolean attribute)
     * @return this builder for chaining
     */
    @Override
    SELF set(String name, String value);

    /**
     * Returns this builder as its own type. Needed by the conditional setters,
     * which return the receiver unchanged when the condition is false.
     *
     * @return {@code this}, typed as {@code SELF}
     */
    @SuppressWarnings("unchecked")
    default SELF self() { return (SELF) this; }

    // ==================== Core Attributes ====================

    /** Sets the id attribute. @param value the element ID @return this for chaining */
    default SELF id(String value) { return set("id", value); }

    /** Sets the class attribute. @param value the CSS class(es) @return this for chaining */
    default SELF class_(String value) { return set("class", value); }

    /**
     * Adds a CSS class to existing classes.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").addClass("primary")
     * // Output: class="btn primary"
     * </pre>
     *
     * @param className the class to add
     * @return this for chaining
     */
    default SELF addClass(String className) {
        String existing = get("class");
        if (existing == null || existing.isBlank()) {
            return set("class", className);
        }
        return set("class", existing + " " + className);
    }

    /**
     * Sets multiple CSS classes at once.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().classes("btn", "primary", "lg")
     * // Output: class="btn primary lg"
     * </pre>
     *
     * @param classNames the class names to add
     * @return this for chaining
     */
    default SELF classes(String... classNames) {
        if (classNames == null || classNames.length == 0) {
            return self();
        }
        return set("class", String.join(" ", classNames));
    }

    /**
     * Fluent conditional class adding.
     * More readable than addClass(boolean, String) for simple conditions.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").classIf("active", isActive).classIf("disabled", isDisabled)
     * </pre>
     *
     * @param className the class name to add if condition is true
     * @param condition whether to add the class
     * @return this for chaining
     */
    default SELF classIf(String className, boolean condition) {
        return condition ? addClass(className) : self();
    }

    /**
     * Adds class based on ternary condition.
     * Adds trueClass if condition is true, falseClass otherwise.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").classToggle(isOpen, "open", "closed")
     * // Result: class="btn open" or class="btn closed"
     * </pre>
     *
     * @param condition the condition to evaluate
     * @param trueClass the class to add if condition is true
     * @param falseClass the class to add if condition is false
     * @return this for chaining
     */
    default SELF classToggle(boolean condition, String trueClass, String falseClass) {
        return addClass(condition ? trueClass : falseClass);
    }

    // ==================== Inline Style ====================

    /** Sets inline style from a string. @param value the CSS style string @return this for chaining */
    default SELF style(String value) { return set("style", value); }

    /** Sets inline style from a Style builder. @param style the Style object @return this for chaining */
    default SELF style(jweb.Style<?> style) { return set("style", style.build()); }

    /** Sets inline style from a CSSValue. @param style the CSSValue @return this for chaining */
    default SELF style(jweb.CSSValue style) { return set("style", style.css()); }

    /**
     * Sets inline style using a lambda builder - NO .done() needed!
     *
     * <p>Example:</p>
     * <pre>
     * attrs()
     *     .class_("card")
     *     .style(s -&gt; s.display(flex).padding(px(10)).backgroundColor(white))
     *     .id("main")
     * </pre>
     *
     * @param builder a lambda that configures the style
     * @return this for chaining
     */
    default SELF style(UnaryOperator<Attributes.InlineStyle> builder) {
        Attributes.InlineStyle s = new Attributes.InlineStyle(new Attributes());
        builder.apply(s);
        return set("style", s.build());
    }

    // ==================== Common Attributes ====================

    /** Sets the title attribute (tooltip). @param value the title text @return this for chaining */
    default SELF title(String value) { return set("title", value); }
    /** Sets the href attribute for links. @param value the URL @return this for chaining */
    default SELF href(String value) { return set("href", value); }
    /** Sets the target attribute for links. @param value the target window/frame @return this for chaining */
    default SELF target(String value) { return set("target", value); }

    /**
     * Sets target="_blank" with proper security attributes.
     * Automatically adds rel="noopener noreferrer" to prevent tabnabbing.
     *
     * @return this for chaining
     */
    default SELF targetBlank() {
        return set("target", "_blank").set("rel", "noopener noreferrer");
    }

    /** Sets the src attribute for images/scripts. @param value the source URL @return this for chaining */
    default SELF src(String value) { return set("src", value); }
    /** Sets the alt attribute for images. @param value the alt text @return this for chaining */
    default SELF alt(String value) { return set("alt", value); }
    /** Sets the width attribute. @param value the width (numeric or with unit) @return this for chaining */
    default SELF width(String value) { return set("width", value); }
    /** Sets the width attribute from a number. @param value the width @return this for chaining */
    default SELF width(int value) { return set("width", String.valueOf(value)); }
    /** Sets the height attribute. @param value the height @return this for chaining */
    default SELF height(String value) { return set("height", value); }
    /** Sets the height attribute from a number. @param value the height @return this for chaining */
    default SELF height(int value) { return set("height", String.valueOf(value)); }

    // ==================== Form Attributes ====================

    /** Sets the type attribute for inputs. @param value the input type @return this for chaining */
    default SELF type(String value) { return set("type", value); }
    /** Sets the name attribute for form elements. @param value the name @return this for chaining */
    default SELF name(String value) { return set("name", value); }
    /** Sets the value attribute. @param value the value @return this for chaining */
    default SELF value(String value) { return set("value", value); }
    /** Sets the placeholder attribute for inputs. @param value the placeholder text @return this for chaining */
    default SELF placeholder(String value) { return set("placeholder", value); }
    /** Sets the action attribute for forms. @param value the form action URL @return this for chaining */
    default SELF action(String value) { return set("action", value); }
    /** Sets the method attribute for forms. @param value the HTTP method @return this for chaining */
    default SELF method(String value) { return set("method", value); }
    /** Sets the for attribute for labels. @param value the target element ID @return this for chaining */
    default SELF for_(String value) { return set("for", value); }

    // ==================== Boolean Attributes ====================

    /** Adds the disabled boolean attribute. @return this for chaining */
    default SELF disabled() { return set("disabled", null); }
    /** Conditionally adds the disabled attribute. @param isDisabled whether to disable @return this for chaining */
    default SELF disabled(boolean isDisabled) { return isDisabled ? disabled() : self(); }
    /** Adds the checked boolean attribute for checkboxes/radios. @return this for chaining */
    default SELF checked() { return set("checked", null); }
    /** Conditionally adds the checked attribute. @param isChecked whether to check @return this for chaining */
    default SELF checked(boolean isChecked) { return isChecked ? checked() : self(); }
    /** Adds the required boolean attribute. @return this for chaining */
    default SELF required() { return set("required", null); }
    /** Adds the readonly boolean attribute. @return this for chaining */
    default SELF readonly() { return set("readonly", null); }
    /** Adds the hidden boolean attribute. @return this for chaining */
    default SELF hidden() { return set("hidden", null); }
    /** Conditionally adds the hidden attribute. @param isHidden whether to hide @return this for chaining */
    default SELF hidden(boolean isHidden) { return isHidden ? hidden() : self(); }
    /** Adds the autofocus boolean attribute. @return this for chaining */
    default SELF autofocus() { return set("autofocus", null); }

    // ==================== Data & ARIA Attributes ====================

    /** Sets a data-* attribute. @param name the data name (without "data-") @param value the value @return this for chaining */
    default SELF data(String name, String value) { return set("data-" + name, value); }
    /** Sets an aria-* attribute. @param name the aria name (without "aria-") @param value the value @return this for chaining */
    default SELF aria(String name, String value) { return set("aria-" + name, value); }
    /** Sets the role attribute for ARIA. @param value the ARIA role @return this for chaining */
    default SELF role(String value) { return set("role", value); }

    // ==================== Table Attributes ====================

    /** Sets the colspan attribute for table cells. @param value the number of columns to span @return this for chaining */
    default SELF colspan(int value) { return set("colspan", String.valueOf(value)); }
    /** Sets the rowspan attribute for table cells. @param value the number of rows to span @return this for chaining */
    default SELF rowspan(int value) { return set("rowspan", String.valueOf(value)); }
    /** Sets the scope attribute for th elements. @param value row, col, rowgroup, colgroup @return this for chaining */
    default SELF scope(String value) { return set("scope", value); }
    /** Sets the headers attribute for table cells. @param value space-separated IDs of th elements @return this for chaining */
    default SELF headers(String value) { return set("headers", value); }

    // ==================== Form Validation Attributes ====================

    /** Sets the pattern attribute for regex validation. @param regex the regex pattern @return this for chaining */
    default SELF pattern(String regex) { return set("pattern", regex); }
    /** Sets the min attribute for numeric/date inputs. @param value the minimum value @return this for chaining */
    default SELF min(String value) { return set("min", value); }
    /** Sets the min attribute with number. @param value the minimum value @return this for chaining */
    default SELF min(int value) { return set("min", String.valueOf(value)); }
    /** Sets the max attribute for numeric/date inputs. @param value the maximum value @return this for chaining */
    default SELF max(String value) { return set("max", value); }
    /** Sets the max attribute with number. @param value the maximum value @return this for chaining */
    default SELF max(int value) { return set("max", String.valueOf(value)); }
    /** Sets the step attribute for numeric inputs. @param value the step increment @return this for chaining */
    default SELF step(String value) { return set("step", value); }
    /** Sets the step attribute with number. @param value the step increment @return this for chaining */
    default SELF step(double value) { return set("step", String.valueOf(value)); }
    /** Sets the minlength attribute for text inputs. @param value minimum number of characters @return this for chaining */
    default SELF minlength(int value) { return set("minlength", String.valueOf(value)); }
    /** Sets the maxlength attribute for text inputs. @param value maximum number of characters @return this for chaining */
    default SELF maxlength(int value) { return set("maxlength", String.valueOf(value)); }
    /** Adds the multiple boolean attribute for file/select inputs. @return this for chaining */
    default SELF multiple() { return set("multiple", null); }
    /** Sets the accept attribute for file inputs. @param value acceptable MIME types @return this for chaining */
    default SELF accept(String value) { return set("accept", value); }
    /** Sets the autocomplete attribute. @param value on, off, or specific tokens @return this for chaining */
    default SELF autocomplete(String value) { return set("autocomplete", value); }
    /** Sets the inputmode attribute for virtual keyboards. @param value none, text, decimal, numeric, tel, search, email, url @return this for chaining */
    default SELF inputmode(String value) { return set("inputmode", value); }
    /** Sets the list attribute to connect to a datalist. @param datalistId the datalist element ID @return this for chaining */
    default SELF list(String datalistId) { return set("list", datalistId); }
    /** Sets the form attribute to associate input with a form. @param formId the form element ID @return this for chaining */
    default SELF form(String formId) { return set("form", formId); }
    /** Sets the enctype attribute for forms. @param value encoding type @return this for chaining */
    default SELF enctype(String value) { return set("enctype", value); }
    /** Adds the novalidate boolean attribute to skip form validation. @return this for chaining */
    default SELF novalidate() { return set("novalidate", null); }
    /** Sets the size attribute for inputs/selects. @param value the visible size @return this for chaining */
    default SELF size(int value) { return set("size", String.valueOf(value)); }
    /** Sets the cols attribute for textareas. @param value number of columns @return this for chaining */
    default SELF cols(int value) { return set("cols", String.valueOf(value)); }
    /** Sets the rows attribute for textareas. @param value number of rows @return this for chaining */
    default SELF rows(int value) { return set("rows", String.valueOf(value)); }
    /** Sets the wrap attribute for textareas. @param value soft or hard @return this for chaining */
    default SELF wrap(String value) { return set("wrap", value); }

    // ==================== Global Attributes ====================

    /** Sets the tabindex attribute. @param value tab order (-1 for not focusable, 0 for natural order, positive for explicit order) @return this for chaining */
    default SELF tabindex(int value) { return set("tabindex", String.valueOf(value)); }
    /** Sets the accesskey attribute for keyboard shortcuts. @param value the key character @return this for chaining */
    default SELF accesskey(String value) { return set("accesskey", value); }
    /** Sets the lang attribute for language. @param value language code (e.g., "en", "es", "zh") @return this for chaining */
    default SELF lang(String value) { return set("lang", value); }
    /** Sets the dir attribute for text direction. @param value ltr, rtl, or auto @return this for chaining */
    default SELF dir(String value) { return set("dir", value); }
    /** Sets the translate attribute. @param value yes or no @return this for chaining */
    default SELF translate(String value) { return set("translate", value); }
    /** Adds the contenteditable boolean attribute. @return this for chaining */
    default SELF contenteditable() { return set("contenteditable", "true"); }
    /** Conditionally sets the contenteditable attribute. @param editable whether content is editable @return this for chaining */
    default SELF contenteditable(boolean editable) { return set("contenteditable", String.valueOf(editable)); }
    /** Adds the draggable attribute. @param draggable whether element is draggable @return this for chaining */
    default SELF draggable(boolean draggable) { return set("draggable", String.valueOf(draggable)); }
    /** Adds the spellcheck attribute. @param check whether to enable spellcheck @return this for chaining */
    default SELF spellcheck(boolean check) { return set("spellcheck", String.valueOf(check)); }
    /** Sets the enterkeyhint attribute for virtual keyboards. @param value enter, done, go, next, previous, search, send @return this for chaining */
    default SELF enterkeyhint(String value) { return set("enterkeyhint", value); }
    /** Sets the inert attribute (non-interactive). @return this for chaining */
    default SELF inert() { return set("inert", null); }
    /** Sets the popover attribute. @param value auto or manual @return this for chaining */
    default SELF popover(String value) { return set("popover", value); }
    /** Sets the popovertarget attribute. @param elementId the ID of the popover element @return this for chaining */
    default SELF popovertarget(String elementId) { return set("popovertarget", elementId); }
    /** Sets the popovertargetaction attribute. @param value show, hide, or toggle @return this for chaining */
    default SELF popovertargetaction(String value) { return set("popovertargetaction", value); }
    /** Sets the part attribute for CSS shadow parts. @param value part name(s) @return this for chaining */
    default SELF part(String value) { return set("part", value); }
    /** Sets the slot attribute for shadow DOM. @param value slot name @return this for chaining */
    default SELF slot(String value) { return set("slot", value); }

    // ==================== Link & Resource Attributes ====================

    /** Sets the rel attribute for links. @param value relationship type @return this for chaining */
    default SELF rel(String value) { return set("rel", value); }
    /** Adds the download attribute for forcing downloads. @return this for chaining */
    default SELF download() { return set("download", null); }
    /** Sets the download attribute with a filename. @param filename the suggested filename @return this for chaining */
    default SELF download(String filename) { return set("download", filename); }
    /** Sets the hreflang attribute for link language. @param value language code @return this for chaining */
    default SELF hreflang(String value) { return set("hreflang", value); }
    /** Sets the referrerpolicy attribute. @param value no-referrer, origin, etc. @return this for chaining */
    default SELF referrerpolicy(String value) { return set("referrerpolicy", value); }
    /** Sets the crossorigin attribute. @param value anonymous or use-credentials @return this for chaining */
    default SELF crossorigin(String value) { return set("crossorigin", value); }
    /** Sets the integrity attribute for subresource integrity. @param hash the SRI hash @return this for chaining */
    default SELF integrity(String hash) { return set("integrity", hash); }
    /** Sets the ping attribute for link tracking. @param urls space-separated URLs @return this for chaining */
    default SELF ping(String urls) { return set("ping", urls); }
    /** Sets the media attribute for responsive resources. @param mediaQuery the media query @return this for chaining */
    default SELF media(String mediaQuery) { return set("media", mediaQuery); }
    /** Sets the as attribute for preload hints. @param value resource type (script, style, image, etc.) @return this for chaining */
    default SELF as(String value) { return set("as", value); }

    // ==================== Image & Media Attributes ====================

    /** Sets the srcset attribute for responsive images. @param value srcset descriptor @return this for chaining */
    default SELF srcset(String value) { return set("srcset", value); }
    /** Sets the sizes attribute for responsive images. @param value sizes descriptor @return this for chaining */
    default SELF sizes(String value) { return set("sizes", value); }
    /** Sets the loading attribute for lazy loading. @param value lazy or eager @return this for chaining */
    default SELF loading(String value) { return set("loading", value); }
    /** Sets the decoding attribute for image decoding. @param value sync, async, or auto @return this for chaining */
    default SELF decoding(String value) { return set("decoding", value); }
    /** Sets the fetchpriority attribute. @param value high, low, or auto @return this for chaining */
    default SELF fetchpriority(String value) { return set("fetchpriority", value); }
    /** Adds the ismap boolean attribute for image maps. @return this for chaining */
    default SELF ismap() { return set("ismap", null); }
    /** Sets the usemap attribute for image maps. @param mapName the map name (with #) @return this for chaining */
    default SELF usemap(String mapName) { return set("usemap", mapName); }

    // ==================== Audio/Video Attributes ====================

    /** Adds the controls boolean attribute for media players. @return this for chaining */
    default SELF controls() { return set("controls", null); }
    /** Adds the autoplay boolean attribute for media. @return this for chaining */
    default SELF autoplay() { return set("autoplay", null); }
    /** Adds the loop boolean attribute for media. @return this for chaining */
    default SELF loop() { return set("loop", null); }
    /** Adds the muted boolean attribute for media. @return this for chaining */
    default SELF muted() { return set("muted", null); }
    /** Sets the preload attribute for media. @param value none, metadata, or auto @return this for chaining */
    default SELF preload(String value) { return set("preload", value); }
    /** Sets the poster attribute for video thumbnails. @param url the poster image URL @return this for chaining */
    default SELF poster(String url) { return set("poster", url); }
    /** Adds the playsinline boolean attribute for inline video playback. @return this for chaining */
    default SELF playsinline() { return set("playsinline", null); }
    /** Adds the disablepictureinpicture attribute. @return this for chaining */
    default SELF disablepictureinpicture() { return set("disablepictureinpicture", null); }

    // ==================== Script Attributes ====================

    /** Adds the async boolean attribute for scripts. @return this for chaining */
    default SELF async() { return set("async", null); }
    /** Adds the defer boolean attribute for scripts. @return this for chaining */
    default SELF defer() { return set("defer", null); }
    /** Adds the nomodule attribute for module fallback scripts. @return this for chaining */
    default SELF nomodule() { return set("nomodule", null); }
    /** Sets the nonce attribute for CSP. @param value the nonce value @return this for chaining */
    default SELF nonce(String value) { return set("nonce", value); }

    // ==================== Meta & Document Attributes ====================

    /** Sets the charset attribute for meta tags. @param value character encoding (usually "UTF-8") @return this for chaining */
    default SELF charset(String value) { return set("charset", value); }
    /** Sets the http-equiv attribute for meta tags. @param value HTTP header name @return this for chaining */
    default SELF httpEquiv(String value) { return set("http-equiv", value); }
    /** Sets the content attribute for meta tags. @param value the content value @return this for chaining */
    default SELF content(String value) { return set("content", value); }

    // ==================== SVG Attributes ====================

    /** Sets the viewBox attribute for SVG. @param value e.g., "0 0 100 100" @return this for chaining */
    default SELF viewBox(String value) { return set("viewBox", value); }
    /**
     * Sets the viewBox attribute from numbers: {@code viewBox(0, 0, 24, 24)}.
     *
     * @param minX the min-x coordinate
     * @param minY the min-y coordinate
     * @param width the viewBox width
     * @param height the viewBox height
     * @return this for chaining
     */
    default SELF viewBox(int minX, int minY, int width, int height) {
        return set("viewBox", minX + " " + minY + " " + width + " " + height);
    }
    /** Sets the preserveAspectRatio attribute for SVG. @param value e.g., "xMidYMid meet" @return this for chaining */
    default SELF preserveAspectRatio(String value) { return set("preserveAspectRatio", value); }
    /** Sets the xmlns attribute for SVG namespace. @param value namespace URI @return this for chaining */
    default SELF xmlns(String value) { return set("xmlns", value); }
    /** Sets the fill attribute for SVG. @param value color or none @return this for chaining */
    default SELF fill(String value) { return set("fill", value); }
    /** Sets the stroke attribute for SVG. @param value color @return this for chaining */
    default SELF stroke(String value) { return set("stroke", value); }
    /** Sets the stroke-width attribute for SVG. @param value width @return this for chaining */
    default SELF strokeWidth(String value) { return set("stroke-width", value); }
    /** Sets the stroke-width attribute from a number. @param value width @return this for chaining */
    default SELF strokeWidth(int value) { return set("stroke-width", String.valueOf(value)); }
    /**
     * Rounds stroke ends and joins — sets both {@code stroke-linecap} and
     * {@code stroke-linejoin} to {@code round}. The usual choice for line icons.
     *
     * @return this for chaining
     */
    default SELF strokeRound() {
        return set("stroke-linecap", "round").set("stroke-linejoin", "round");
    }
    /**
     * Common setup for stroked line icons: no fill, {@code currentColor}
     * stroke at the given width, rounded caps and joins.
     *
     * <pre>
     * svg(attrs().viewBox(0, 0, 24, 24).width(24).height(24).lineIcon(2), path(...))
     * </pre>
     *
     * @param strokeWidth the stroke width
     * @return this for chaining
     */
    default SELF lineIcon(int strokeWidth) {
        return fill("none").stroke("currentColor").strokeWidth(strokeWidth).strokeRound();
    }
    /** Sets the d attribute for SVG path. @param value path data @return this for chaining */
    default SELF d(String value) { return set("d", value); }
    /** Sets the cx attribute for SVG circles. @param value center x @return this for chaining */
    default SELF cx(String value) { return set("cx", value); }
    /** Sets the cy attribute for SVG circles. @param value center y @return this for chaining */
    default SELF cy(String value) { return set("cy", value); }
    /** Sets the r attribute for SVG circles. @param value radius @return this for chaining */
    default SELF r(String value) { return set("r", value); }
    /** Sets the x attribute for SVG elements. @param value x coordinate @return this for chaining */
    default SELF x(String value) { return set("x", value); }
    /** Sets the y attribute for SVG elements. @param value y coordinate @return this for chaining */
    default SELF y(String value) { return set("y", value); }
    /** Sets the x1 attribute for SVG lines. @param value start x @return this for chaining */
    default SELF x1(String value) { return set("x1", value); }
    /** Sets the y1 attribute for SVG lines. @param value start y @return this for chaining */
    default SELF y1(String value) { return set("y1", value); }
    /** Sets the x2 attribute for SVG lines. @param value end x @return this for chaining */
    default SELF x2(String value) { return set("x2", value); }
    /** Sets the y2 attribute for SVG lines. @param value end y @return this for chaining */
    default SELF y2(String value) { return set("y2", value); }
    /** Sets the points attribute for SVG polygons/polylines. @param value coordinate pairs @return this for chaining */
    default SELF points(String value) { return set("points", value); }
    /** Sets the transform attribute for SVG transformations. @param value transform functions @return this for chaining */
    default SELF transform(String value) { return set("transform", value); }

    // ==================== Microdata Attributes ====================

    /** Sets the itemscope boolean attribute for microdata. @return this for chaining */
    default SELF itemscope() { return set("itemscope", null); }
    /** Sets the itemtype attribute for microdata. @param value schema type URL @return this for chaining */
    default SELF itemtype(String value) { return set("itemtype", value); }
    /** Sets the itemprop attribute for microdata. @param value property name @return this for chaining */
    default SELF itemprop(String value) { return set("itemprop", value); }
    /** Sets the itemid attribute for microdata. @param value global identifier @return this for chaining */
    default SELF itemid(String value) { return set("itemid", value); }
    /** Sets the itemref attribute for microdata. @param value space-separated IDs @return this for chaining */
    default SELF itemref(String value) { return set("itemref", value); }

    // ==================== Dialog & Details Attributes ====================

    /** Adds the open boolean attribute for details/dialog. @return this for chaining */
    default SELF open() { return set("open", null); }
    /** Conditionally adds the open attribute. @param isOpen whether element is open @return this for chaining */
    default SELF open(boolean isOpen) { return isOpen ? open() : self(); }

    // ==================== Meter & Progress Attributes ====================

    /** Sets the value attribute for meter/progress. @param value current value @return this for chaining */
    default SELF value(double value) { return set("value", String.valueOf(value)); }
    /** Sets the low attribute for meter. @param value low threshold @return this for chaining */
    default SELF low(double value) { return set("low", String.valueOf(value)); }
    /** Sets the high attribute for meter. @param value high threshold @return this for chaining */
    default SELF high(double value) { return set("high", String.valueOf(value)); }
    /** Sets the optimum attribute for meter. @param value optimal value @return this for chaining */
    default SELF optimum(double value) { return set("optimum", String.valueOf(value)); }

    // ==================== Template & Web Component Attributes ====================

    /** Sets the shadowrootmode attribute for declarative shadow DOM. @param value open or closed @return this for chaining */
    default SELF shadowrootmode(String value) { return set("shadowrootmode", value); }

    // ==================== Semantic Data Attributes ====================

    /** Sets the datetime attribute for time elements. @param value machine-readable datetime @return this for chaining */
    default SELF datetime(String value) { return set("datetime", value); }

    // ==================== iframe Attributes ====================

    /** Sets the sandbox attribute for iframes. @param value sandbox restrictions @return this for chaining */
    default SELF sandbox(String value) { return set("sandbox", value); }
    /** Sets the allow attribute for iframes. @param value feature policy @return this for chaining */
    default SELF allow(String value) { return set("allow", value); }
    /** Adds the allowfullscreen boolean attribute. @return this for chaining */
    default SELF allowfullscreen() { return set("allowfullscreen", null); }
    /** Sets the srcdoc attribute for inline iframe content. @param html the HTML content @return this for chaining */
    default SELF srcdoc(String html) { return set("srcdoc", html); }
    /** Sets the name attribute for iframe targeting. @param value frame name @return this for chaining */
    default SELF frameName(String value) { return set("name", value); }

    // ==================== Fragment Swaps (server-driven UI) ====================

    /**
     * On click, fetch an HTML fragment from the server and swap it into a
     * target element — server-driven UI without writing any JavaScript.
     * The swap is wrapped in a View Transition when the browser supports it.
     *
     * <pre>
     * // The route returns a fragment (an Element without html/body):
     * app.get("/products/list", req -&gt; productList(req.queryInt("page", 1)));
     *
     * // Any element triggers the swap declaratively:
     * button(attrs().swap("/products/list?page=2", "#products"), text("Next page"))
     * </pre>
     *
     * @param url the fragment URL to fetch
     * @param targetSelector CSS selector of the element to fill
     * @return this for chaining
     */
    default SELF swap(String url, String targetSelector) {
        return set("data-swap-get", url).set("data-swap-target", targetSelector);
    }

    /**
     * Like {@link #swap} but replaces the target element itself (outerHTML).
     *
     * @param url the fragment URL to fetch
     * @param targetSelector CSS selector of the element to replace
     * @return this for chaining
     */
    default SELF swapOuter(String url, String targetSelector) {
        return swap(url, targetSelector).set("data-swap-mode", "outer");
    }

    /**
     * Like {@link #swap} but morphs the target in place instead of replacing
     * its HTML: unchanged nodes are kept, so focus, scroll position, and
     * in-progress input survive the update. Best for lists and forms that
     * refresh while the user interacts with them.
     *
     * @param url the fragment URL to fetch
     * @param targetSelector CSS selector of the element to morph
     * @return this for chaining
     */
    default SELF swapMorph(String url, String targetSelector) {
        return swap(url, targetSelector).set("data-swap-mode", "morph");
    }

    /**
     * On submit, POST this form's data and swap the returned fragment into
     * the target — progressive forms without page reloads.
     *
     * <pre>
     * form(attrs().swapForm("/comments", "#comment-list"),
     *     Input.text("message"), button("Post"))
     * </pre>
     *
     * @param actionUrl the URL to POST to
     * @param targetSelector CSS selector of the element to fill
     * @return this for chaining
     */
    default SELF swapForm(String actionUrl, String targetSelector) {
        return set("data-swap-post", actionUrl).set("data-swap-target", targetSelector);
    }

    /**
     * Pushes a browser-history entry when the swap runs, so back/forward
     * re-swap naturally. Pass the address-bar URL to show.
     *
     * @param browserUrl the URL to show in the address bar
     * @return this for chaining
     */
    default SELF swapPush(String browserUrl) {
        return set("data-swap-push", browserUrl);
    }

    // ==================== Event Handlers ====================

    /**
     * Registers a click event handler.
     *
     * <p>Example:</p>
     * <pre>
     * button(attrs().onClick(e -&gt; count.set(count.get() + 1)), text("Click"))
     * </pre>
     *
     * @param handler the handler to execute on click
     * @return this for chaining
     */
    default SELF onClick(Consumer<Event> handler) { return on("click", handler); }

    /**
     * Registers a change event handler (for inputs, selects, textareas).
     *
     * @param handler the handler to execute on change
     * @return this for chaining
     */
    default SELF onChange(Consumer<Event> handler) { return on("change", handler); }

    /**
     * Registers an input event handler (fires on every keystroke).
     *
     * @param handler the handler to execute on input
     * @return this for chaining
     */
    default SELF onInput(Consumer<Event> handler) { return on("input", handler); }

    /**
     * Registers a submit event handler for forms.
     *
     * @param handler the handler to execute on submit
     * @return this for chaining
     */
    default SELF onSubmit(Consumer<Event> handler) { return on("submit", handler); }

    /**
     * Registers a focus event handler.
     *
     * @param handler the handler to execute on focus
     * @return this for chaining
     */
    default SELF onFocus(Consumer<Event> handler) { return on("focus", handler); }

    /**
     * Registers a blur event handler (when element loses focus).
     *
     * @param handler the handler to execute on blur
     * @return this for chaining
     */
    default SELF onBlur(Consumer<Event> handler) { return on("blur", handler); }

    /**
     * Registers a keydown event handler.
     *
     * @param handler the handler to execute on keydown
     * @return this for chaining
     */
    default SELF onKeyDown(Consumer<Event> handler) { return on("keydown", handler); }

    /**
     * Registers a keyup event handler.
     *
     * @param handler the handler to execute on keyup
     * @return this for chaining
     */
    default SELF onKeyUp(Consumer<Event> handler) { return on("keyup", handler); }

    /**
     * Registers a keypress event handler.
     *
     * @param handler the handler to execute on keypress
     * @return this for chaining
     */
    default SELF onKeyPress(Consumer<Event> handler) { return on("keypress", handler); }

    /**
     * Registers a mouseenter event handler.
     *
     * @param handler the handler to execute on mouseenter
     * @return this for chaining
     */
    default SELF onMouseEnter(Consumer<Event> handler) { return on("mouseenter", handler); }

    /**
     * Registers a mouseleave event handler.
     *
     * @param handler the handler to execute on mouseleave
     * @return this for chaining
     */
    default SELF onMouseLeave(Consumer<Event> handler) { return on("mouseleave", handler); }

    /**
     * Registers a mousedown event handler.
     *
     * @param handler the handler to execute on mousedown
     * @return this for chaining
     */
    default SELF onMouseDown(Consumer<Event> handler) { return on("mousedown", handler); }

    /**
     * Registers a mouseup event handler.
     *
     * @param handler the handler to execute on mouseup
     * @return this for chaining
     */
    default SELF onMouseUp(Consumer<Event> handler) { return on("mouseup", handler); }

    /**
     * Registers a mousemove event handler.
     *
     * @param handler the handler to execute on mousemove
     * @return this for chaining
     */
    default SELF onMouseMove(Consumer<Event> handler) { return on("mousemove", handler); }

    /**
     * Registers a mouseover event handler.
     *
     * @param handler the handler to execute on mouseover
     * @return this for chaining
     */
    default SELF onMouseOver(Consumer<Event> handler) { return on("mouseover", handler); }

    /**
     * Registers a mouseout event handler.
     *
     * @param handler the handler to execute on mouseout
     * @return this for chaining
     */
    default SELF onMouseOut(Consumer<Event> handler) { return on("mouseout", handler); }

    /**
     * Registers a contextmenu (right-click) event handler.
     *
     * @param handler the handler to execute on contextmenu
     * @return this for chaining
     */
    default SELF onContextMenu(Consumer<Event> handler) { return on("contextmenu", handler); }

    /**
     * Registers a wheel (scroll wheel) event handler.
     *
     * @param handler the handler to execute on wheel
     * @return this for chaining
     */
    default SELF onWheel(Consumer<Event> handler) { return on("wheel", handler); }

    /**
     * Registers a double-click event handler.
     *
     * @param handler the handler to execute on double-click
     * @return this for chaining
     */
    default SELF onDoubleClick(Consumer<Event> handler) { return on("dblclick", handler); }

    /**
     * Alias for {@link #onDoubleClick(Consumer)} matching the DOM event name
     * ({@code dblclick}).
     *
     * @param handler the handler to execute on double-click
     * @return this for chaining
     */
    default SELF onDblClick(Consumer<Event> handler) { return onDoubleClick(handler); }

    /**
     * Registers a drag event handler.
     *
     * @param handler the handler to execute during drag
     * @return this for chaining
     */
    default SELF onDrag(Consumer<Event> handler) { return on("drag", handler); }

    /**
     * Registers a dragstart event handler.
     *
     * @param handler the handler to execute on drag start
     * @return this for chaining
     */
    default SELF onDragStart(Consumer<Event> handler) { return on("dragstart", handler); }

    /**
     * Registers a dragend event handler.
     *
     * @param handler the handler to execute on drag end
     * @return this for chaining
     */
    default SELF onDragEnd(Consumer<Event> handler) { return on("dragend", handler); }

    /**
     * Registers a dragenter event handler.
     *
     * @param handler the handler to execute when dragged item enters
     * @return this for chaining
     */
    default SELF onDragEnter(Consumer<Event> handler) { return on("dragenter", handler); }

    /**
     * Registers a dragleave event handler.
     *
     * @param handler the handler to execute when dragged item leaves
     * @return this for chaining
     */
    default SELF onDragLeave(Consumer<Event> handler) { return on("dragleave", handler); }

    /**
     * Registers a dragover event handler.
     *
     * @param handler the handler to execute when dragged item is over
     * @return this for chaining
     */
    default SELF onDragOver(Consumer<Event> handler) { return on("dragover", handler); }

    /**
     * Registers a drop event handler.
     *
     * @param handler the handler to execute on drop
     * @return this for chaining
     */
    default SELF onDrop(Consumer<Event> handler) { return on("drop", handler); }

    /**
     * Registers a touchstart event handler.
     *
     * @param handler the handler to execute on touch start
     * @return this for chaining
     */
    default SELF onTouchStart(Consumer<Event> handler) { return on("touchstart", handler); }

    /**
     * Registers a touchmove event handler.
     *
     * @param handler the handler to execute on touch move
     * @return this for chaining
     */
    default SELF onTouchMove(Consumer<Event> handler) { return on("touchmove", handler); }

    /**
     * Registers a touchend event handler.
     *
     * @param handler the handler to execute on touch end
     * @return this for chaining
     */
    default SELF onTouchEnd(Consumer<Event> handler) { return on("touchend", handler); }

    /**
     * Registers a touchcancel event handler.
     *
     * @param handler the handler to execute on touch cancel
     * @return this for chaining
     */
    default SELF onTouchCancel(Consumer<Event> handler) { return on("touchcancel", handler); }

    /**
     * Registers a scroll event handler.
     *
     * @param handler the handler to execute on scroll
     * @return this for chaining
     */
    default SELF onScroll(Consumer<Event> handler) { return on("scroll", handler); }

    /**
     * Registers a toggle event handler for details elements.
     * Fires when the open/closed state changes.
     *
     * @param handler the handler to execute on toggle
     * @return this for chaining
     */
    default SELF onToggle(Consumer<Event> handler) { return on("toggle", handler); }

    /**
     * Registers a cancel event handler for dialog elements.
     * Fires when user cancels the dialog (ESC key).
     *
     * @param handler the handler to execute on cancel
     * @return this for chaining
     */
    default SELF onCancel(Consumer<Event> handler) { return on("cancel", handler); }

    /**
     * Registers a close event handler for dialog elements.
     * Fires when dialog is closed.
     *
     * @param handler the handler to execute on close
     * @return this for chaining
     */
    default SELF onClose(Consumer<Event> handler) { return on("close", handler); }

    /**
     * Registers an animationstart event handler.
     *
     * @param handler the handler to execute when animation starts
     * @return this for chaining
     */
    default SELF onAnimationStart(Consumer<Event> handler) { return on("animationstart", handler); }

    /**
     * Registers an animationend event handler.
     *
     * @param handler the handler to execute when animation ends
     * @return this for chaining
     */
    default SELF onAnimationEnd(Consumer<Event> handler) { return on("animationend", handler); }

    /**
     * Registers an animationiteration event handler.
     *
     * @param handler the handler to execute on animation iteration
     * @return this for chaining
     */
    default SELF onAnimationIteration(Consumer<Event> handler) { return on("animationiteration", handler); }

    /**
     * Registers a transitionend event handler.
     *
     * @param handler the handler to execute when transition ends
     * @return this for chaining
     */
    default SELF onTransitionEnd(Consumer<Event> handler) { return on("transitionend", handler); }

    /**
     * Registers a load event handler.
     *
     * @param handler the handler to execute on load
     * @return this for chaining
     */
    default SELF onLoad(Consumer<Event> handler) { return on("load", handler); }

    /**
     * Registers an error event handler.
     *
     * @param handler the handler to execute on error
     * @return this for chaining
     */
    default SELF onError(Consumer<Event> handler) { return on("error", handler); }

    /**
     * Registers a copy event handler.
     *
     * @param handler the handler to execute on copy
     * @return this for chaining
     */
    default SELF onCopy(Consumer<Event> handler) { return on("copy", handler); }

    /**
     * Registers a cut event handler.
     *
     * @param handler the handler to execute on cut
     * @return this for chaining
     */
    default SELF onCut(Consumer<Event> handler) { return on("cut", handler); }

    /**
     * Registers a paste event handler.
     *
     * @param handler the handler to execute on paste
     * @return this for chaining
     */
    default SELF onPaste(Consumer<Event> handler) { return on("paste", handler); }

    /**
     * Registers a generic event handler for any DOM event type.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().on("scroll", e -&gt; handleScroll(e)))
     * </pre>
     *
     * @param eventType the DOM event type (click, change, scroll, etc.)
     * @param handler the handler to execute
     * @return this for chaining
     */
    default SELF on(String eventType, Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register(eventType, handler);
        // A data attribute the runtime delegates to, not an inline on<type>=
        // attribute: nonce-based CSPs (Middlewares.recommended) can never
        // allow handler attributes, and delegation survives swaps and morphs.
        return set("data-jweb-on" + eventType, eh.getId());
    }

    // ==================== JavaScript Action Event Handlers ====================
    // These allow using the Actions DSL directly with event attributes.
    // Usage: attrs().onClick(show("panel"))
    //        attrs().onClick(all(hide("loader"), show("content")))

    /**
     * Sets a click handler using a JavaScript Action from the Actions DSL.
     *
     * <p>Example:</p>
     * <pre>
     * import static com.osmig.Jweb.framework.js.Actions.*;
     *
     * button(attrs().onClick(show("panel")), "Show Panel")
     * button(attrs().onClick(toggle("dropdown")), "Toggle")
     * button(attrs().onClick(all(hide("a"), show("b"))), "Switch")
     * </pre>
     *
     * @param action the JavaScript action to execute on click
     * @return this for chaining
     */
    default SELF onClick(Action action) { return on("click", action); }

    /** Sets a change handler using a JavaScript Action. @param action the action to execute on change @return this for chaining */
    default SELF onChange(Action action) { return on("change", action); }

    /** Sets an input handler using a JavaScript Action. @param action the action to execute on input @return this for chaining */
    default SELF onInput(Action action) { return on("input", action); }

    /** Sets a submit handler using a JavaScript Action. @param action the action to execute on submit @return this for chaining */
    default SELF onSubmit(Action action) { return on("submit", action); }

    /** Sets a focus handler using a JavaScript Action. @param action the action to execute on focus @return this for chaining */
    default SELF onFocus(Action action) { return on("focus", action); }

    /** Sets a blur handler using a JavaScript Action. @param action the action to execute on blur @return this for chaining */
    default SELF onBlur(Action action) { return on("blur", action); }

    /** Sets a keydown handler using a JavaScript Action. @param action the action to execute on keydown @return this for chaining */
    default SELF onKeyDown(Action action) { return on("keydown", action); }

    /** Sets a keyup handler using a JavaScript Action. @param action the action to execute on keyup @return this for chaining */
    default SELF onKeyUp(Action action) { return on("keyup", action); }

    /** Sets a mouseenter handler using a JavaScript Action. @param action the action to execute on mouseenter @return this for chaining */
    default SELF onMouseEnter(Action action) { return on("mouseenter", action); }

    /** Sets a mouseleave handler using a JavaScript Action. @param action the action to execute on mouseleave @return this for chaining */
    default SELF onMouseLeave(Action action) { return on("mouseleave", action); }

    /** Sets a double-click handler using a JavaScript Action. @param action the action to execute on dblclick @return this for chaining */
    default SELF onDoubleClick(Action action) { return on("dblclick", action); }

    /** Alias for {@link #onDoubleClick(Action)} matching the DOM event name ({@code dblclick}). @param action the action to execute on double-click @return this for chaining */
    default SELF onDblClick(Action action) { return onDoubleClick(action); }

    /**
     * Sets any event handler using a JavaScript Action.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().on("scroll", throttledScrollHandler)
     * </pre>
     *
     * <p>Inside a page render this stores the JS in the render context and
     * emits a {@code data-jweb-act<type>} attribute the runtime delegates to
     * — inline {@code on<type>=} attributes can never run under the nonce
     * CSP from {@code Middlewares.recommended()}, and the definitions travel
     * to the browser in a nonce-stamped script instead. Outside a render
     * context (or with the runtime disabled) it falls back to the classic
     * inline attribute.</p>
     *
     * @param eventType the DOM event type
     * @param action the JavaScript action to execute
     * @return this for chaining
     */
    default SELF on(String eventType, Action action) {
        String js = action.inline();
        String id = com.osmig.Jweb.framework.js.ClientActions.register(js);
        if (id == null) return set("on" + eventType, js);
        return set("data-jweb-act" + eventType, id);
    }

    /**
     * Opts this element's raw {@code on<type>=} attributes out of the
     * CSP-safe rewrite the serializer applies inside page renders, keeping
     * them as genuine inline attributes. For content that ships without the
     * client runtime — error pages, static export — where delegation could
     * never fire; the handlers then only run where no nonce CSP is enforced.
     *
     * @return this for chaining
     */
    default SELF inlineHandlers() { return set("data-jweb-inline", null); }

    // ==================== Refs ====================

    /**
     * Sets a ref on this element for later reference.
     *
     * <p>Example:</p>
     * <pre>
     * Ref inputRef = Ref.create();
     * input(attrs().ref(inputRef).type("text"))
     * // Later: inputRef.id() returns the element's ID
     * </pre>
     *
     * @param ref the ref to attach
     * @return this for chaining
     */
    default SELF ref(Ref ref) {
        return id(ref.id());
    }

    // ==================== Transitions ====================

    /**
     * Starts a transition builder for CSS transitions.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs()
     *     .transition()
     *         .property("opacity", "transform")
     *         .duration("300ms")
     *         .easing("ease-in-out")
     *     .done(),
     *     content
     * )
     * </pre>
     *
     * @return a TransitionBuilder that writes back into this builder
     */
    default TransitionBuilder transition() {
        return new TransitionBuilder(this);
    }

    // ---- boolean-conditional overloads (one rule for every boolean attribute) ----

    /** Conditionally adds the required attribute. @return this for chaining */
    default SELF required(boolean isRequired) { return isRequired ? required() : self(); }
    /** Conditionally adds the readonly attribute. @return this for chaining */
    default SELF readonly(boolean isReadonly) { return isReadonly ? readonly() : self(); }
    /** Conditionally adds the autofocus attribute. @return this for chaining */
    default SELF autofocus(boolean isAutofocus) { return isAutofocus ? autofocus() : self(); }
    /** Conditionally adds the multiple attribute. @return this for chaining */
    default SELF multiple(boolean isMultiple) { return isMultiple ? multiple() : self(); }
    /** Conditionally adds the controls attribute. @return this for chaining */
    default SELF controls(boolean showControls) { return showControls ? controls() : self(); }
    /** Conditionally adds the autoplay attribute. @return this for chaining */
    default SELF autoplay(boolean isAutoplay) { return isAutoplay ? autoplay() : self(); }
    /** Conditionally adds the loop attribute. @return this for chaining */
    default SELF loop(boolean isLoop) { return isLoop ? loop() : self(); }
    /** Conditionally adds the muted attribute. @return this for chaining */
    default SELF muted(boolean isMuted) { return isMuted ? muted() : self(); }
    /** Conditionally adds the playsinline attribute. @return this for chaining */
    default SELF playsinline(boolean isInline) { return isInline ? playsinline() : self(); }
    /** Conditionally adds the novalidate attribute. @return this for chaining */
    default SELF novalidate(boolean skipValidation) { return skipValidation ? novalidate() : self(); }
    /** Conditionally adds the inert attribute. @return this for chaining */
    default SELF inert(boolean isInert) { return isInert ? inert() : self(); }
    /** Conditionally adds the allowfullscreen attribute. @return this for chaining */
    default SELF allowfullscreen(boolean allowed) { return allowed ? allowfullscreen() : self(); }
    /** Conditionally adds the itemscope attribute. @return this for chaining */
    default SELF itemscope(boolean isScope) { return isScope ? itemscope() : self(); }
    /** Conditionally adds the download attribute. @return this for chaining */
    default SELF download(boolean isDownload) { return isDownload ? download() : self(); }

    /** Sets the value attribute from an int — renders "3", not "3.0". @return this for chaining */
    default SELF value(int value) { return set("value", String.valueOf(value)); }
    /** Sets the step attribute from an int. @return this for chaining */
    default SELF step(int value) { return set("step", String.valueOf(value)); }
}
