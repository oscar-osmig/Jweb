package jweb.js;

import jweb.Action;
import jweb.Element;
import jweb.Func;

import java.util.ArrayList;
import java.util.List;

import static jweb.js.JsOpts.esc;
import static jweb.js.JsOpts.quote;

/**
 * Defines a Web Component — {@code customElements.define} with the lifecycle
 * callbacks as DSL arguments and the shadow template written in the HTML DSL:
 *
 * <pre>{@code
 * import static jweb.El.*;
 * import static jweb.Js.*;
 *
 * inlineScript(actions().does(
 *     customElement("user-card")
 *         .observedAttributes("name")
 *         .shadow()
 *         .template(div(class_("card"), slot()))
 *         .connected(callback().log("card mounted"))
 *         .attributeChanged(callback("name", "old", "now")
 *             .does(dom(".card").setText(v("now"))))
 * ).build())
 * }</pre>
 *
 * <p>The definition is guarded — re-running it (a re-render, a swapped
 * fragment) is a no-op rather than the {@code NotSupportedError} a second
 * {@code define} of the same name throws.</p>
 *
 * <p>{@link #template(Element)} is written into the shadow root on connect, or
 * into the element itself when no shadow was asked for. Inside the callbacks
 * {@code this} is the element; {@code this.shadowRoot} is the shadow.</p>
 */
public final class CustomElement implements Action {

    private final String name;
    private final List<String> observed = new ArrayList<>();
    private String shadowMode;
    private String templateHtml;
    private Func connected;
    private Func disconnected;
    private Func attributeChanged;
    private Func adopted;
    private String extendsTag;

    /** Internal — start one with {@code customElement(name)}. */
    public CustomElement(String name) {
        this.name = name;
    }

    /** The attributes whose changes reach {@link #attributeChanged(Func)}. */
    public CustomElement observedAttributes(String... attributes) {
        for (String a : attributes) observed.add(quote(a));
        return this;
    }

    /** Attach an open shadow root in the constructor. */
    public CustomElement shadow() {
        return shadow("open");
    }

    /** Attach a shadow root — {@code "open"} or {@code "closed"}. */
    public CustomElement shadow(String mode) {
        this.shadowMode = mode;
        return this;
    }

    /** Markup written into the shadow root (or the element) on connect. */
    public CustomElement template(Element markup) {
        this.templateHtml = markup == null ? null : markup.toHtml();
        return this;
    }

    /** Runs when the element enters the document. */
    public CustomElement connected(Func handler) {
        this.connected = handler;
        return this;
    }

    /** Runs when the element leaves the document. */
    public CustomElement disconnected(Func handler) {
        this.disconnected = handler;
        return this;
    }

    /**
     * Runs when an {@link #observedAttributes} attribute changes — the callback
     * takes the attribute name, the old value and the new one.
     */
    public CustomElement attributeChanged(Func handler) {
        this.attributeChanged = handler;
        return this;
    }

    /** Runs when the element moves to a new document. */
    public CustomElement adopted(Func handler) {
        this.adopted = handler;
        return this;
    }

    /** Customized built-in: {@code customElements.define(name, cls, {extends: tag})}. */
    public CustomElement extendsTag(String tag) {
        this.extendsTag = tag;
        return this;
    }

    @Override
    public String build() {
        StringBuilder cls = new StringBuilder("class extends ")
            .append(extendsTag == null ? "HTMLElement" : builtinFor(extendsTag))
            .append("{");
        if (!observed.isEmpty()) {
            cls.append("static get observedAttributes(){return [")
               .append(String.join(",", observed)).append("]}");
        }
        if (shadowMode != null) {
            cls.append("constructor(){super();this.attachShadow({mode:").append(quote(shadowMode)).append("})}");
        }
        cls.append("connectedCallback(){");
        if (templateHtml != null) {
            cls.append("var r=this.shadowRoot||this;")
               .append("if(!r.__jwebTpl){r.__jwebTpl=1;r.innerHTML='")
               .append(escHtml(templateHtml)).append("'}");
        }
        if (connected != null) cls.append("(").append(connected.toExpr()).append(").call(this)");
        cls.append("}");
        if (disconnected != null) {
            cls.append("disconnectedCallback(){(").append(disconnected.toExpr()).append(").call(this)}");
        }
        if (adopted != null) {
            cls.append("adoptedCallback(){(").append(adopted.toExpr()).append(").call(this)}");
        }
        if (attributeChanged != null) {
            cls.append("attributeChangedCallback(name,oldValue,newValue){(")
               .append(attributeChanged.toExpr())
               .append(").call(this,name,oldValue,newValue)}");
        }
        cls.append("}");
        String define = "customElements.define(" + quote(name) + "," + cls
            + (extendsTag == null ? "" : ",{extends:" + quote(extendsTag) + "}") + ")";
        return "if(!customElements.get(" + quote(name) + "))" + define;
    }

    /** Escapes rendered HTML for a single-quoted JS string literal. */
    private static String escHtml(String html) {
        return esc(html.replace("\r", ""));
    }

    /**
     * The interface a customized built-in must extend. Unknown tags fall back
     * to HTMLElement, which is what the platform does for unrecognized names.
     */
    private static String builtinFor(String tag) {
        return switch (tag) {
            case "a" -> "HTMLAnchorElement";
            case "button" -> "HTMLButtonElement";
            case "div" -> "HTMLDivElement";
            case "form" -> "HTMLFormElement";
            case "input" -> "HTMLInputElement";
            case "li" -> "HTMLLIElement";
            case "ol" -> "HTMLOListElement";
            case "ul" -> "HTMLUListElement";
            case "p" -> "HTMLParagraphElement";
            case "span" -> "HTMLSpanElement";
            case "table" -> "HTMLTableElement";
            case "textarea" -> "HTMLTextAreaElement";
            default -> "HTMLElement";
        };
    }
}
