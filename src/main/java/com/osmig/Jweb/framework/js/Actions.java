package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * High-level JavaScript DSL for common UI interactions.
 *
 * <p>Designed to be simple and intuitive - no JS knowledge required.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.Actions.*;
 *
 * // Form submission
 * onSubmit("my-form")
 *     .loading("Saving...")
 *     .post("/api/save").withFormData()
 *     .ok(showMessage("status").fromResponse("message"))
 *     .fail(showMessage("status").text("Network error"))
 *
 * // Button click
 * onClick("delete-btn")
 *     .confirm("Are you sure?")
 *     .post("/api/delete")
 *     .ok(reload())
 *
 * // Show/hide elements
 * onClick("toggle-btn")
 *     .toggle("panel")
 * </pre>
 */
public final class Actions {

    private Actions() {}

    // ==================== Event Entry Points ====================

    /** Handle form submission by form ID. */
    public static FormHandler onSubmit(String formId) {
        return new FormHandler(formId);
    }

    /** Handle form submission with external service (e.g., emailjs). */
    public static ExternalServiceFormHandler onSubmitExternal(String formId) {
        return new ExternalServiceFormHandler(formId);
    }

    /** Handle button/element click by ID. */
    public static ClickHandler onClick(String elementId) {
        return new ClickHandler(elementId);
    }

    /** Handle input change by ID. */
    public static ChangeHandler onChange(String elementId) {
        return new ChangeHandler(elementId);
    }

    /** Run code when page loads. */
    public static OnLoadHandler onLoad() {
        return new OnLoadHandler();
    }

    /** Define a reusable function. */
    public static FunctionDef define(String name) {
        return new FunctionDef(name);
    }

    /** Define state variables. */
    public static StateBuilder state() {
        return new StateBuilder();
    }

    /** Define DOM element references. */
    public static RefsBuilder refs() {
        return new RefsBuilder();
    }

    /**
     * Listen to window/document events.
     * Usage: onEvent("popstate").when("isLoggedIn").then(call("clearSession"))
     */
    public static EventHandler onEvent(String eventName) {
        return new EventHandler(eventName);
    }

    /**
     * Define a window-accessible function.
     * Usage: windowFunc("approveReq").params("id", "email").does(...)
     */
    public static WindowFuncBuilder windowFunc(String name) {
        return new WindowFuncBuilder(name);
    }

    // ==================== Actions (what to do) ====================

    /** Show a message in an element. */
    public static MessageAction showMessage(String elementId) {
        return new MessageAction(elementId);
    }

    /** Show a modal/dialog. */
    public static ModalAction showModal(String modalId) {
        return new ModalAction(modalId);
    }

    /** Hide a modal/dialog. */
    public static Action hideModal(String modalId) {
        return () -> "$_('" + modalId + "').style.display='none'";
    }

    /**
     * Show a styled confirm dialog.
     * Usage: confirmDialog("modal-id").message("Delete this?").onConfirm(action)
     */
    public static ConfirmDialog confirmDialog(String modalId) {
        return new ConfirmDialog(modalId);
    }

    /** Show an element. */
    public static Action show(String elementId) {
        return () -> "$_('" + elementId + "').style.display=''";
    }

    /** Show an element with specific display type. */
    public static Action show(String elementId, String display) {
        return () -> "$_('" + elementId + "').style.display='" + display + "'";
    }

    /** Hide an element. */
    public static Action hide(String elementId) {
        return () -> "$_('" + elementId + "').style.display='none'";
    }

    /** Toggle element visibility. */
    public static Action toggle(String elementId) {
        return () -> "{const e=$_('" + elementId + "');e.style.display=e.style.display==='none'?'':'none'}";
    }

    /** Set element text content. */
    public static Action setText(String elementId, String text) {
        return () -> "$_('" + elementId + "').textContent='" + esc(text) + "'";
    }

    /**
     * Set element text content and color in one call.
     * Usage: setTextAndColor("status", "Success!", "#065f46")
     */
    public static Action setTextAndColor(String elementId, String text, String color) {
        return () -> "{const _e=$_('" + esc(elementId) + "');_e.textContent='" + esc(text) + "';_e.style.color='" + esc(color) + "';}";
    }

    /**
     * Set element text and color from JS expressions.
     * Usage: setTextAndColorExpr("status", "msg", "'#065f46'")
     */
    public static Action setTextAndColorExpr(String elementId, String textExpr, String colorExpr) {
        return () -> "{const _e=$_('" + esc(elementId) + "');_e.textContent=" + textExpr + ";_e.style.color=" + colorExpr + ";}";
    }

    /**
     * Try/catch wrapper for error handling.
     * Usage: tryCatch().try_(asyncAction).catch_(errorAction)
     */
    public static TryCatchBuilder tryCatch() {
        return new TryCatchBuilder();
    }

    /**
     * Check if an external library is defined.
     * Usage: ifDefined("emailjs").then(action).otherwise(fallback)
     */
    public static IfAction ifDefined(String libName) {
        return new IfAction("typeof " + libName + "!=='undefined'");
    }

    /**
     * Status feedback element for loading/success/error states.
     * Usage: statusFeedback("status-el").loading("Sending...").success("Done!").error("Failed")
     */
    public static StatusFeedback statusFeedback(String elementId) {
        return new StatusFeedback(elementId);
    }

    /**
     * Call an external service (like emailjs).
     * Usage: externalService("emailjs").call("send", "'service_id'", "'template_id'", "{email:email}")
     */
    public static ExternalServiceCall externalService(String serviceName) {
        return new ExternalServiceCall(serviceName);
    }

    /**
     * Conditional error message based on HTTP response status.
     * Usage: responseError("error-el").on401("Invalid credentials").otherwise("Connection error")
     */
    public static ResponseErrorBuilder responseError(String elementId) {
        return new ResponseErrorBuilder(elementId);
    }

    /** Set element text from response field. */
    public static Action setTextFromResponse(String elementId, String field) {
        return () -> "$_('" + elementId + "').textContent=_data." + field;
    }

    /** Set element HTML content. */
    public static Action setHtml(String elementId, String html) {
        return () -> "$_('" + elementId + "').innerHTML='" + esc(html) + "'";
    }

    /** Add CSS class to element. */
    public static Action addClass(String elementId, String className) {
        return () -> "$_('" + elementId + "').classList.add('" + className + "')";
    }

    /** Remove CSS class from element. */
    public static Action removeClass(String elementId, String className) {
        return () -> "$_('" + elementId + "').classList.remove('" + className + "')";
    }

    /** Toggle CSS class on element. */
    public static Action toggleClass(String elementId, String className) {
        return () -> "$_('" + elementId + "').classList.toggle('" + className + "')";
    }

    /** Reload the current page. */
    public static Action reload() {
        return () -> "location.reload()";
    }

    /** Navigate to a URL. */
    public static Action navigateTo(String url) {
        return () -> "location.href='" + esc(url) + "'";
    }

    /** Download a file. */
    public static DownloadAction download(String url) {
        return new DownloadAction(url);
    }

    /**
     * Make an async fetch request (standalone, not tied to form).
     * Usage: fetch("/api/data").headerFromVar("X-Token", "myVar").ok(action)
     */
    public static FetchBuilder fetch(String url) {
        return new FetchBuilder("GET", url);
    }

    /**
     * Set up tab switching behavior.
     * Usage: tabs(".tab-btn").activeClass("active").onSwitch(action)
     */
    public static TabHandler tabs(String selector) {
        return new TabHandler(selector);
    }

    /**
     * Render a list of items into a container.
     * Usage: renderList("container").from("dataVar").using("templateFunc").empty("No items")
     */
    public static ListRenderer renderList(String containerId) {
        return new ListRenderer(containerId);
    }

    /**
     * Set innerHTML of an element.
     * Usage: setInnerHtml("container").to(html) or .fromVar("htmlVar")
     */
    public static HtmlSetter setInnerHtml(String elementId) {
        return new HtmlSetter(elementId);
    }

    /**
     * Build a JS template function for rendering items.
     * Usage: template("r").div().style("...").text(field("email")).end()
     */
    public static TemplateBuilder template(String itemVar) {
        return new TemplateBuilder(itemVar);
    }

    /**
     * Reference a field from the item in a template.
     */
    public static TemplateField field(String name) {
        return new TemplateField(name);
    }

    /**
     * Reference a field with escaping for safe HTML.
     */
    public static TemplateField escapedField(String name) {
        return new TemplateField(name, true);
    }

    /**
     * Format a timestamp field as a date.
     */
    public static TemplateDateField dateField(String name) {
        return new TemplateDateField(name);
    }

    /**
     * Create a color switch based on a value.
     * Usage: colorSwitch("status").when("PENDING", "#fef3c7").when("APPROVED", "#d1fae5").otherwise("#fee2e2")
     */
    public static ColorSwitch colorSwitch(String fieldName) {
        return new ColorSwitch(fieldName);
    }

    /**
     * Define an async function with optional guards.
     * Usage: asyncFunc("loadData").guard("!isLoggedIn").does(fetch(...))
     */
    public static AsyncFuncBuilder asyncFunc(String name) {
        return new AsyncFuncBuilder(name);
    }

    /**
     * Reset multiple state variables.
     * Usage: resetVars().var("isLoggedIn", false).var("adminKey", "").array("requests")
     */
    public static ResetVarsBuilder resetVars() {
        return new ResetVarsBuilder();
    }

    /**
     * Show modal with dynamic HTML content.
     * Usage: showModalHtml("modal-overlay", "modal-body").html("<h3>Success!</h3>")
     */
    public static ModalHtmlBuilder showModalHtml(String modalId, String bodyRef) {
        return new ModalHtmlBuilder(modalId, bodyRef);
    }

    /**
     * Create an alert/result modal (success, error, info).
     * Usage: alertModal("modal-overlay", "modal-body").success("Approved!").detail("Token: xyz")
     */
    public static AlertModalBuilder alertModal(String modalId, String bodyRef) {
        return new AlertModalBuilder(modalId, bodyRef);
    }

    /** Conditional action. */
    public static IfAction when(String condition) {
        return new IfAction(condition);
    }

    /** Conditional based on response field being truthy. */
    public static IfAction whenResponse(String field) {
        return new IfAction("_data." + field);
    }

    /** Conditional based on response.ok */
    public static IfAction whenOk() {
        return new IfAction("_res.ok");
    }

    /** Call a defined function. */
    public static Action call(String functionName) {
        return () -> functionName + "()";
    }

    /** Call a defined function with arguments. */
    public static CallAction call(String functionName, String... args) {
        return new CallAction(functionName, args);
    }

    /** Reset a form. */
    public static Action resetForm(String formId) {
        return () -> "$_('" + formId + "').reset()";
    }

    /** Focus an element. */
    public static Action focus(String elementId) {
        return () -> "$_('" + elementId + "').focus()";
    }

    /** Copy text to clipboard. */
    public static Action copyToClipboard(String elementId) {
        return () -> "{const e=$_('" + elementId + "');e.select();navigator.clipboard.writeText(e.value)}";
    }

    /** Log to console (for debugging). */
    public static Action log(String message) {
        return () -> "console.log('" + esc(message) + "')";
    }

    /** Do nothing (placeholder). */
    public static Action noop() {
        return () -> "";
    }

    /**
     * Assign a value to a variable.
     * Usage: assignVar("requests", "_data") or assignVar("isLoggedIn", "true")
     */
    public static Action assignVar(String varName, String expression) {
        return () -> varName + "=" + expression;
    }

    /**
     * Assign a literal string value to a variable.
     * Usage: assignStr("adminKey", "") sets adminKey=''
     */
    public static Action assignStr(String varName, String value) {
        return () -> varName + "='" + esc(value) + "'";
    }

    /**
     * Get input value and store in variable.
     * Usage: getInputValue("admin-email").storeTo("adminEmail")
     */
    public static InputValueGetter getInputValue(String elementId) {
        return new InputValueGetter(elementId);
    }

    /**
     * Set text content of an element from an expression.
     * Usage: setTextExpr("login-error", "_res?.status===401?'Invalid':'Error'")
     */
    public static Action setTextExpr(String elementId, String expression) {
        return () -> "$_('" + esc(elementId) + "').textContent=" + expression;
    }

    /**
     * Push state to browser history.
     * Usage: pushState("admin", "true")
     */
    public static Action pushState(String key, String value) {
        return () -> "history.pushState({" + key + ":" + value + "},'',location.href)";
    }

    /**
     * Create a ternary URL expression based on a condition.
     * Usage: ternaryUrl("tab==='pending'", "/api/pending", "/api/all")
     * Returns: tab==='pending'?'/api/pending':'/api/all'
     */
    public static String ternaryUrl(String condition, String trueUrl, String falseUrl) {
        return condition + "?'" + esc(trueUrl) + "':'" + esc(falseUrl) + "'";
    }

    /**
     * Select input text and copy to clipboard.
     * Usage: selectAndCopy("token-input")
     */
    public static Action selectAndCopy(String elementId) {
        return () -> "{const _inp=$_('" + esc(elementId) + "');_inp.select();navigator.clipboard.writeText(_inp.value);}";
    }

    /**
     * Hide element when clicking on backdrop (the element itself, not children).
     * Usage: hideOnBackdropClick("modalOverlay")
     * Common pattern for closing modals by clicking outside content.
     */
    public static Action hideOnBackdropClick(String refName) {
        return () -> "if(e.target===" + refName + ")" + refName + ".style.display='none'";
    }

    /** Combine multiple actions. */
    public static Action all(Action... actions) {
        return () -> {
            StringBuilder sb = new StringBuilder();
            for (Action a : actions) {
                String code = a.build();
                if (!code.isEmpty()) {
                    sb.append(code);
                    if (!code.endsWith(";") && !code.endsWith("}")) sb.append(";");
                }
            }
            return sb.toString();
        };
    }

    // ==================== Response Field Access ====================

    /** Reference a field from API response. */
    public static ResponseField response(String field) {
        return new ResponseField(field);
    }

    /** Reference form data field. */
    public static FormField formField(String name) {
        return new FormField(name);
    }

    // ==================== Script Builder ====================

    /** Build a complete script from multiple handlers. */
    public static ScriptBuilder script() {
        return new ScriptBuilder();
    }

    // ==================== Utilities ====================

    static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    // ==================== Core Interfaces ====================

    /**
     * Represents a buildable JavaScript action.
     *
     * <p>Actions can be used in two ways:</p>
     * <ol>
     *   <li>With ScriptBuilder for &lt;script&gt; blocks: {@code script().add(onClick("btn").does(show("panel")))}</li>
     *   <li>Inline with event attributes: {@code attrs().onClick(show("panel"))}</li>
     * </ol>
     */
    @FunctionalInterface
    public interface Action {
        /**
         * Builds the JavaScript code for this action.
         * @return the JavaScript code string
         */
        String build();

        /**
         * Returns the action as an inline JavaScript string for use in event attributes.
         * Can be used directly with onclick, onsubmit, etc.
         *
         * <p>Example:</p>
         * <pre>
         * // Using with attrs()
         * button(attrs().set("onclick", show("panel").inline()), "Show Panel")
         *
         * // Or use the convenience method
         * button(attrs().onClick(show("panel")), "Show Panel")
         * </pre>
         *
         * @return the JavaScript code string suitable for inline event handlers
         */
        default String inline() {
            return build();
        }
    }

    /** Represents a value that can come from response or be literal. */
    public interface Value {
        String toJs();
    }

    // ==================== Value Types ====================

    public static class ResponseField implements Value {
        private final String field;
        ResponseField(String field) { this.field = field; }
        @Override public String toJs() { return "_data." + field; }
    }

    public static class FormField implements Value {
        private final String name;
        FormField(String name) { this.name = name; }
        @Override public String toJs() { return "_fd.get('" + name + "')"; }
    }

    public static class LiteralValue implements Value {
        private final String value;
        LiteralValue(String value) { this.value = value; }
        @Override public String toJs() { return "'" + esc(value) + "'"; }
    }

    // ==================== Form Handler ====================

    public static class FormHandler {
        private final String formId;
        private String loadingText;
        private String method = "POST";
        private String url;
        private boolean useFormData = false;
        private String jsonBody;
        private final List<String> headers = new ArrayList<>();
        private Action okAction;
        private Action failAction;
        private Action alwaysAction;
        private Action beforeAction;

        FormHandler(String formId) { this.formId = formId; }

        /** Show loading state on submit button. */
        public FormHandler loading(String text) {
            this.loadingText = text;
            return this;
        }

        /** Execute action before request. */
        public FormHandler before(Action action) {
            this.beforeAction = action;
            return this;
        }

        /** POST to URL. */
        public FormHandler post(String url) {
            this.method = "POST";
            this.url = url;
            return this;
        }

        /** GET from URL. */
        public FormHandler get(String url) {
            this.method = "GET";
            this.url = url;
            return this;
        }

        /** Send form data as JSON (auto-collects form fields). */
        public FormHandler withFormData() {
            this.useFormData = true;
            return this;
        }

        /** Send specific JSON body. */
        public FormHandler withJson(String... keyValues) {
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < keyValues.length; i += 2) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(keyValues[i]).append("\":").append(keyValues[i + 1]);
            }
            this.jsonBody = sb.append("}").toString();
            return this;
        }

        /** Add a header. */
        public FormHandler header(String name, String value) {
            headers.add("'" + name + "':'" + esc(value) + "'");
            return this;
        }

        /** Add a header from a variable. */
        public FormHandler headerFrom(String name, String varName) {
            headers.add("'" + name + "':" + varName);
            return this;
        }

        /** Action on success (HTTP 2xx). */
        public FormHandler ok(Action action) {
            this.okAction = action;
            return this;
        }

        /** Action on failure. */
        public FormHandler fail(Action action) {
            this.failAction = action;
            return this;
        }

        /** Action that always runs (finally). */
        public FormHandler always(Action action) {
            this.alwaysAction = action;
            return this;
        }

        /** Build the handler code. */
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("$_('" + formId + "')?.addEventListener('submit',async function(e){");
            sb.append("e.preventDefault();");
            sb.append("const _form=this,_btn=_form.querySelector('button[type=\"submit\"]');");
            sb.append("const _fd=new FormData(_form);");

            if (useFormData && !"GET".equals(method)) {
                sb.append("const _body={};_fd.forEach((v,k)=>_body[k]=v);");
            }

            if (loadingText != null) {
                sb.append("const _origText=_btn?.textContent;");
                sb.append("if(_btn){_btn.disabled=true;_btn.textContent='" + esc(loadingText) + "';}");
            }

            if (beforeAction != null) {
                sb.append(beforeAction.build()).append(";");
            }

            sb.append("try{");

            // Build URL - for GET, append query params
            if ("GET".equals(method) && useFormData) {
                sb.append("const _params=new URLSearchParams();_fd.forEach((v,k)=>_params.append(k,v));");
                sb.append("const _res=await fetch('" + esc(url) + "?'+_params.toString()");
            } else {
                sb.append("const _res=await fetch('" + esc(url) + "'");
            }

            // Only add options object if needed
            boolean needsOptions = !"GET".equals(method) || !headers.isEmpty() || jsonBody != null || (useFormData && !"GET".equals(method));
            if (needsOptions) {
                sb.append(",{");
                sb.append("method:'" + method + "'");

                // Headers
                List<String> allHeaders = new ArrayList<>(headers);
                if ((useFormData && !"GET".equals(method)) || jsonBody != null) {
                    allHeaders.add("'Content-Type':'application/json'");
                }
                if (!allHeaders.isEmpty()) {
                    sb.append(",headers:{" + String.join(",", allHeaders) + "}");
                }

                // Body (only for non-GET)
                if (useFormData && !"GET".equals(method)) {
                    sb.append(",body:JSON.stringify(_body)");
                } else if (jsonBody != null) {
                    sb.append(",body:'" + esc(jsonBody) + "'");
                }
                sb.append("}");
            }

            sb.append(");");
            sb.append("const _data=await _res.json();");

            // Success/failure handling
            if (okAction != null || failAction != null) {
                sb.append("if(_res.ok){");
                if (okAction != null) sb.append(okAction.build()).append(";");
                sb.append("}else{");
                if (failAction != null) sb.append(failAction.build()).append(";");
                sb.append("}");
            }

            sb.append("}catch(_e){");
            if (failAction != null) sb.append(failAction.build()).append(";");
            sb.append("}");

            // Finally
            if (loadingText != null || alwaysAction != null) {
                sb.append("finally{");
                if (loadingText != null) {
                    sb.append("if(_btn){_btn.disabled=false;_btn.textContent=_origText;}");
                }
                if (alwaysAction != null) {
                    sb.append(alwaysAction.build()).append(";");
                }
                sb.append("}");
            }

            sb.append("})");
            return sb.toString();
        }
    }

    // ==================== Click Handler ====================

    public static class ClickHandler {
        private final String elementId;
        private String confirmMessage;
        private String method;
        private String url;
        private String jsonBody;
        private final List<String> headers = new ArrayList<>();
        private Action okAction;
        private Action failAction;
        private Action simpleAction;
        private String loadingText;

        ClickHandler(String elementId) { this.elementId = elementId; }

        /** Show confirmation dialog before proceeding. */
        public ClickHandler confirm(String message) {
            this.confirmMessage = message;
            return this;
        }

        /** Show loading state. */
        public ClickHandler loading(String text) {
            this.loadingText = text;
            return this;
        }

        /** POST to URL. */
        public ClickHandler post(String url) {
            this.method = "POST";
            this.url = url;
            return this;
        }

        /** GET from URL. */
        public ClickHandler get(String url) {
            this.method = "GET";
            this.url = url;
            return this;
        }

        /** DELETE to URL. */
        public ClickHandler delete(String url) {
            this.method = "DELETE";
            this.url = url;
            return this;
        }

        /** Send JSON body. */
        public ClickHandler withJson(String... keyValues) {
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < keyValues.length; i += 2) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(keyValues[i]).append("\":\"").append(esc(keyValues[i + 1])).append("\"");
            }
            this.jsonBody = sb.append("}").toString();
            return this;
        }

        /** Add header. */
        public ClickHandler header(String name, String value) {
            headers.add("'" + name + "':'" + esc(value) + "'");
            return this;
        }

        /** Add header from variable. */
        public ClickHandler headerFrom(String name, String varName) {
            headers.add("'" + name + "':" + varName);
            return this;
        }

        /** Action on success. */
        public ClickHandler ok(Action action) {
            this.okAction = action;
            return this;
        }

        /** Action on failure. */
        public ClickHandler fail(Action action) {
            this.failAction = action;
            return this;
        }

        /** Simple action (no fetch). */
        public ClickHandler then(Action action) {
            this.simpleAction = action;
            return this;
        }

        /** Toggle an element's visibility. */
        public ClickHandler toggle(String targetId) {
            this.simpleAction = Actions.toggle(targetId);
            return this;
        }

        /** Show an element. */
        public ClickHandler show(String targetId) {
            this.simpleAction = Actions.show(targetId);
            return this;
        }

        /** Hide an element. */
        public ClickHandler hide(String targetId) {
            this.simpleAction = Actions.hide(targetId);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("$_('" + elementId + "')?.addEventListener('click',");

            boolean isAsync = url != null;
            if (isAsync) sb.append("async ");
            sb.append("function(){");

            if (confirmMessage != null) {
                sb.append("if(!confirm('" + esc(confirmMessage) + "'))return;");
            }

            if (simpleAction != null && url == null) {
                sb.append(simpleAction.build());
            } else if (url != null) {
                if (loadingText != null) {
                    sb.append("const _btn=this,_origText=_btn.textContent;");
                    sb.append("_btn.disabled=true;_btn.textContent='" + esc(loadingText) + "';");
                }

                sb.append("try{");
                sb.append("const _res=await fetch('" + esc(url) + "',{");
                sb.append("method:'" + method + "'");

                List<String> allHeaders = new ArrayList<>(headers);
                if (jsonBody != null) {
                    allHeaders.add("'Content-Type':'application/json'");
                }
                if (!allHeaders.isEmpty()) {
                    sb.append(",headers:{" + String.join(",", allHeaders) + "}");
                }
                if (jsonBody != null) {
                    sb.append(",body:'" + esc(jsonBody) + "'");
                }

                sb.append("});");
                sb.append("const _data=await _res.json();");

                if (okAction != null || failAction != null) {
                    sb.append("if(_res.ok){");
                    if (okAction != null) sb.append(okAction.build()).append(";");
                    sb.append("}else{");
                    if (failAction != null) sb.append(failAction.build()).append(";");
                    sb.append("}");
                }

                sb.append("}catch(_e){");
                if (failAction != null) sb.append(failAction.build()).append(";");
                sb.append("}");

                if (loadingText != null) {
                    sb.append("finally{_btn.disabled=false;_btn.textContent=_origText;}");
                }
            }

            sb.append("})");
            return sb.toString();
        }
    }

    // ==================== Change Handler ====================

    public static class ChangeHandler {
        private final String elementId;
        private Action action;

        ChangeHandler(String elementId) { this.elementId = elementId; }

        public ChangeHandler then(Action action) {
            this.action = action;
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("$_('" + elementId + "')?.addEventListener('change',function(){");
            if (action != null) sb.append(action.build());
            sb.append("})");
            return sb.toString();
        }
    }

    // ==================== OnLoad Handler ====================

    public static class OnLoadHandler {
        private final List<Action> actions = new ArrayList<>();

        public OnLoadHandler then(Action action) {
            actions.add(action);
            return this;
        }

        public OnLoadHandler call(String functionName) {
            actions.add(Actions.call(functionName));
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            for (Action a : actions) {
                sb.append(a.build());
                if (!a.build().endsWith(";") && !a.build().endsWith("}")) sb.append(";");
            }
            return sb.toString();
        }
    }

    // ==================== Message Action ====================

    public static class MessageAction implements Action {
        private final String elementId;
        private String text;
        private String responseField;
        private String type; // "success", "error", or null

        MessageAction(String elementId) { this.elementId = elementId; }

        /** Set literal text. */
        public MessageAction text(String text) {
            this.text = text;
            return this;
        }

        /** Set text from response field. */
        public MessageAction fromResponse(String field) {
            this.responseField = field;
            return this;
        }

        /** Mark as success message. */
        public MessageAction success() {
            this.type = "success";
            return this;
        }

        /** Mark as success with text. */
        public MessageAction success(String text) {
            this.type = "success";
            this.text = text;
            return this;
        }

        /** Mark as error message. */
        public MessageAction error() {
            this.type = "error";
            return this;
        }

        /** Mark as error with text. */
        public MessageAction error(String text) {
            this.type = "error";
            this.text = text;
            return this;
        }

        /** Mark as warning message. */
        public MessageAction warning() {
            this.type = "warning";
            return this;
        }

        /** Mark as warning with text. */
        public MessageAction warning(String text) {
            this.type = "warning";
            this.text = text;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const _el=$_('" + elementId + "');");

            // Set content
            if (responseField != null) {
                sb.append("_el.textContent=_data." + responseField + ";");
            } else if (text != null) {
                sb.append("_el.textContent='" + esc(text) + "';");
            }

            // Show element
            sb.append("_el.style.display='block';");

            // Apply type styling
            if ("success".equals(type)) {
                sb.append("_el.style.backgroundColor='#d1fae5';_el.style.color='#065f46';");
            } else if ("error".equals(type)) {
                sb.append("_el.style.backgroundColor='#fee2e2';_el.style.color='#991b1b';");
            } else if ("warning".equals(type)) {
                sb.append("_el.style.backgroundColor='#fef3c7';_el.style.color='#92400e';");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Modal Action ====================

    public static class ModalAction implements Action {
        private final String modalId;
        private String title;
        private String body;
        private String bodyFromResponse;

        ModalAction(String modalId) { this.modalId = modalId; }

        public ModalAction title(String title) {
            this.title = title;
            return this;
        }

        public ModalAction body(String body) {
            this.body = body;
            return this;
        }

        public ModalAction bodyFromResponse(String field) {
            this.bodyFromResponse = field;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const _m=$_('" + modalId + "');");

            if (title != null) {
                sb.append("const _t=_m.querySelector('[data-modal-title]');if(_t)_t.textContent='" + esc(title) + "';");
            }

            if (body != null) {
                sb.append("const _b=_m.querySelector('[data-modal-body]');if(_b)_b.innerHTML='" + esc(body) + "';");
            } else if (bodyFromResponse != null) {
                sb.append("const _b=_m.querySelector('[data-modal-body]');if(_b)_b.innerHTML=_data." + bodyFromResponse + ";");
            }

            sb.append("_m.style.display='flex';}");
            return sb.toString();
        }
    }

    // ==================== Confirm Dialog ====================

    /**
     * A styled confirm dialog that shows in a modal.
     * Replaces browser's window.confirm() with a nice UI.
     */
    public static class ConfirmDialog implements Action {
        private final String modalId;
        private String message = "Are you sure?";
        private String confirmText = "Confirm";
        private String cancelText = "Cancel";
        private String confirmColor = "#6366f1";
        private Action onConfirmAction;
        private Action onCancelAction;

        ConfirmDialog(String modalId) { this.modalId = modalId; }

        /** Set the confirmation message. */
        public ConfirmDialog message(String message) {
            this.message = message;
            return this;
        }

        /** Set confirm button text. */
        public ConfirmDialog confirmText(String text) {
            this.confirmText = text;
            return this;
        }

        /** Set cancel button text. */
        public ConfirmDialog cancelText(String text) {
            this.cancelText = text;
            return this;
        }

        /** Set confirm button color (for danger actions use "#ef4444"). */
        public ConfirmDialog danger() {
            this.confirmColor = "#ef4444";
            return this;
        }

        /** Action to run when user confirms. */
        public ConfirmDialog onConfirm(Action action) {
            this.onConfirmAction = action;
            return this;
        }

        /** Action to run when user cancels. */
        public ConfirmDialog onCancel(Action action) {
            this.onCancelAction = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const _m=$_('" + modalId + "'),_b=_m.querySelector('[data-modal-body]');");
            sb.append("_b.innerHTML='<div style=\"text-align:center\">");
            sb.append("<h3 style=\"margin:0 0 1rem;color:#1f2937\">" + esc(message) + "</h3>");
            sb.append("<div style=\"display:flex;gap:0.75rem;justify-content:center;margin-top:1.5rem\">");
            sb.append("<button data-cancel style=\"padding:0.6rem 1.5rem;background:#e5e7eb;color:#374151;border:none;border-radius:8px;cursor:pointer;font-weight:500\">" + esc(cancelText) + "</button>");
            sb.append("<button data-confirm style=\"padding:0.6rem 1.5rem;background:" + confirmColor + ";color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:500\">" + esc(confirmText) + "</button>");
            sb.append("</div></div>';");
            sb.append("_m.style.display='flex';");

            // Cancel handler
            sb.append("_b.querySelector('[data-cancel]').onclick=()=>{_m.style.display='none';");
            if (onCancelAction != null) {
                sb.append(onCancelAction.build());
            }
            sb.append("};");

            // Confirm handler
            sb.append("_b.querySelector('[data-confirm]').onclick=async()=>{_m.style.display='none';");
            if (onConfirmAction != null) {
                sb.append(onConfirmAction.build());
            }
            sb.append("};");

            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Call Action ====================

    public static class CallAction implements Action {
        private final String functionName;
        private final String[] args;

        CallAction(String functionName, String[] args) {
            this.functionName = functionName;
            this.args = args;
        }

        @Override
        public String build() {
            return functionName + "(" + String.join(",", args) + ")";
        }
    }

    // ==================== Function Definition ====================

    public static class FunctionDef {
        private final String name;
        private final List<String> params = new ArrayList<>();
        private final List<String> body = new ArrayList<>();
        private boolean isAsync = false;

        FunctionDef(String name) { this.name = name; }

        /**
         * Makes this function async.
         *
         * <p>Example:</p>
         * <pre>
         * define("loadData").async().params("id").does(
         *     await_(fetch_("/api/data/" + expr("id"))),
         *     ...
         * )
         * // Output: async function loadData(id) { await fetch('/api/data/' + id); ... }
         * </pre>
         *
         * @return this builder for chaining
         */
        public FunctionDef async() {
            this.isAsync = true;
            return this;
        }

        public FunctionDef params(String... params) {
            for (String p : params) this.params.add(p);
            return this;
        }

        public FunctionDef does(Action... actions) {
            for (Action a : actions) body.add(a.build());
            return this;
        }

        public FunctionDef raw(String js) {
            body.add(js);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            if (isAsync) {
                sb.append("async ");
            }
            sb.append("function " + name + "(" + String.join(",", params) + "){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith(";") && !s.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Async/Await Support ====================

    /**
     * Wraps an action with await keyword.
     *
     * <p>Example:</p>
     * <pre>
     * await_(fetch_("/api/data"))
     * // Output: await fetch('/api/data')
     * </pre>
     *
     * @param action the action to await
     * @return an Action that wraps with await
     */
    public static Action await_(Action action) {
        return () -> "await " + action.build();
    }

    /**
     * Wraps a raw JavaScript expression with await.
     *
     * <p>Example:</p>
     * <pre>
     * await_("response.json()")
     * // Output: await response.json()
     * </pre>
     *
     * @param expression the JavaScript expression to await
     * @return an Action that wraps with await
     */
    public static Action await_(String expression) {
        return () -> "await " + expression;
    }

    /**
     * Creates an async IIFE (Immediately Invoked Function Expression).
     * Useful for running async code in non-async contexts.
     *
     * <p>Example:</p>
     * <pre>
     * asyncBlock(
     *     await_(fetch_("/api/data")),
     *     setText("result", "done")
     * )
     * // Output: (async()=>{await fetch('/api/data');$_('result').textContent='done'})()
     * </pre>
     *
     * @param actions the actions to execute
     * @return an Action containing the async IIFE
     */
    public static Action asyncBlock(Action... actions) {
        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("(async()=>{");
            for (Action a : actions) {
                sb.append(a.build());
                String built = a.build();
                if (!built.endsWith(";") && !built.endsWith("}")) {
                    sb.append(";");
                }
            }
            sb.append("})()");
            return sb.toString();
        };
    }

    /**
     * Creates a try-catch block for async operations.
     *
     * <p>Example:</p>
     * <pre>
     * asyncTry(
     *     await_(fetch_("/api/data"))
     * ).catch_(
     *     showMessage("error").error("Failed to load")
     * )
     * </pre>
     *
     * @param actions the actions to try
     * @return an AsyncTryBuilder for chaining
     */
    public static AsyncTryBuilder asyncTry(Action... actions) {
        return new AsyncTryBuilder(actions);
    }

    /**
     * Builder for async try-catch-finally blocks.
     */
    public static class AsyncTryBuilder implements Action {
        private final Action[] tryActions;
        private Action catchAction;
        private Action finallyAction;

        AsyncTryBuilder(Action[] tryActions) {
            this.tryActions = tryActions;
        }

        /**
         * Adds a catch block. The error is available as '_err'.
         *
         * @param action the action to execute on error
         * @return this builder for chaining
         */
        public AsyncTryBuilder catch_(Action action) {
            this.catchAction = action;
            return this;
        }

        /**
         * Adds a finally block.
         *
         * @param action the action to always execute
         * @return this builder for chaining
         */
        public AsyncTryBuilder finally_(Action action) {
            this.finallyAction = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("try{");
            for (Action a : tryActions) {
                sb.append(a.build());
                String built = a.build();
                if (!built.endsWith(";") && !built.endsWith("}")) {
                    sb.append(";");
                }
            }
            sb.append("}");
            if (catchAction != null) {
                sb.append("catch(_err){");
                sb.append(catchAction.build());
                String built = catchAction.build();
                if (!built.endsWith(";") && !built.endsWith("}")) {
                    sb.append(";");
                }
                sb.append("}");
            }
            if (finallyAction != null) {
                sb.append("finally{");
                sb.append(finallyAction.build());
                String built = finallyAction.build();
                if (!built.endsWith(";") && !built.endsWith("}")) {
                    sb.append(";");
                }
                sb.append("}");
            }
            return sb.toString();
        }
    }

    /**
     * Creates a Promise.all() call for parallel async operations.
     *
     * <p>Example:</p>
     * <pre>
     * promiseAll(
     *     fetch_("/api/users"),
     *     fetch_("/api/posts")
     * )
     * // Output: Promise.all([fetch('/api/users'),fetch('/api/posts')])
     * </pre>
     *
     * @param actions the promises to run in parallel
     * @return an Action for Promise.all
     */
    public static Action promiseAll(Action... actions) {
        return () -> {
            StringBuilder sb = new StringBuilder("Promise.all([");
            for (int i = 0; i < actions.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(actions[i].build());
            }
            sb.append("])");
            return sb.toString();
        };
    }

    /**
     * Creates a sleep/delay action using a Promise.
     *
     * <p>Example:</p>
     * <pre>
     * await_(sleep(1000))
     * // Output: await new Promise(r=>setTimeout(r,1000))
     * </pre>
     *
     * @param ms the delay in milliseconds
     * @return an Action for the sleep promise
     */
    public static Action sleep(int ms) {
        return () -> "new Promise(r=>setTimeout(r," + ms + "))";
    }

    // ==================== Fetch/HTTP Builder ====================

    /**
     * Creates a GET fetch request.
     *
     * <p>Example:</p>
     * <pre>
     * await_(get("/api/users"))
     * // Output: await fetch('/api/users')
     * </pre>
     *
     * @param url the URL to fetch
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder get(String url) {
        return new FetchBuilder("GET", url);
    }

    /**
     * Creates a POST fetch request.
     *
     * <p>Example:</p>
     * <pre>
     * await_(post("/api/users").json("{\"name\":\"John\"}"))
     * // Output: await fetch('/api/users',{method:'POST',headers:{'Content-Type':'application/json'},body:'{"name":"John"}'})
     * </pre>
     *
     * @param url the URL to post to
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder post(String url) {
        return new FetchBuilder("POST", url);
    }

    /**
     * Creates a PUT fetch request.
     *
     * @param url the URL
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder put(String url) {
        return new FetchBuilder("PUT", url);
    }

    /**
     * Creates a PATCH fetch request.
     *
     * @param url the URL
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder patch(String url) {
        return new FetchBuilder("PATCH", url);
    }

    /**
     * Creates a DELETE fetch request.
     *
     * @param url the URL
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder delete(String url) {
        return new FetchBuilder("DELETE", url);
    }

    /**
     * Creates a fetch request with any HTTP method.
     *
     * @param method the HTTP method
     * @param url the URL
     * @return a FetchBuilder for chaining
     */
    public static FetchBuilder fetch(String method, String url) {
        return new FetchBuilder(method, url);
    }

    /**
     * Fluent builder for JavaScript fetch() calls.
     *
     * <p>Examples:</p>
     * <pre>
     * // Simple GET
     * get("/api/users")
     *
     * // POST with JSON
     * post("/api/users")
     *     .json("{\"name\":\"John\"}")
     *     .header("Authorization", "Bearer " + token)
     *
     * // With form data
     * post("/api/upload")
     *     .formData("myForm")
     *
     * // Full request with response handling
     * get("/api/data")
     *     .then("json()")                    // Parse as JSON
     *     .ok(setText("result", "_data"))    // On success
     *     .fail(showMessage("error"))        // On error
     * </pre>
     */
    public static class FetchBuilder implements Action {
        private String method;
        private String url;
        private String urlSuffix = "";
        private String body;
        private String contentType;
        private final List<String> headers = new ArrayList<>();
        private String credentials;
        private String mode;
        private String thenExpr;
        private Action okAction;
        private Action failAction;
        private final List<StatusHandler> statusHandlers = new ArrayList<>();

        private record StatusHandler(int status, Action action) {}

        FetchBuilder(String method, String url) {
            this.method = method;
            this.url = url;
        }

        /**
         * Changes the method to POST.
         *
         * @return this builder
         */
        public FetchBuilder post() {
            this.method = "POST";
            return this;
        }

        /**
         * Changes the method to PUT.
         *
         * @return this builder
         */
        public FetchBuilder put() {
            this.method = "PUT";
            return this;
        }

        /**
         * Changes the method to DELETE.
         *
         * @return this builder
         */
        public FetchBuilder delete() {
            this.method = "DELETE";
            return this;
        }

        /**
         * Sets the URL from a JavaScript variable/expression.
         *
         * <p>Example:</p>
         * <pre>
         * fetch("").urlFromVar("apiUrl")
         * // Output: fetch(apiUrl, {...})
         * </pre>
         *
         * @param varName the variable name or expression
         * @return this builder
         */
        public FetchBuilder urlFromVar(String varName) {
            this.url = null;  // Mark as expression-based
            this.urlSuffix = varName;
            return this;
        }

        /**
         * Appends a JavaScript variable to the URL.
         *
         * <p>Example:</p>
         * <pre>
         * fetch("/api/users/").appendVar("userId")
         * // Output: fetch('/api/users/'+userId, {...})
         * </pre>
         *
         * @param varName the variable name to append
         * @return this builder
         */
        public FetchBuilder appendVar(String varName) {
            this.urlSuffix = varName;
            return this;
        }

        /**
         * Adds a header with value from a JavaScript variable.
         *
         * <p>Example:</p>
         * <pre>
         * post("/api/data").headerFromVar("X-Auth-Token", "authToken")
         * // Output: headers: { 'X-Auth-Token': authToken }
         * </pre>
         *
         * @param name the header name
         * @param varName the JavaScript variable name
         * @return this builder
         */
        public FetchBuilder headerFromVar(String name, String varName) {
            headers.add("'" + esc(name) + "':" + varName);
            return this;
        }

        /**
         * Handles a specific HTTP status code.
         *
         * <p>Example:</p>
         * <pre>
         * get("/api/data")
         *     .onStatus(401, redirect("/login"))
         *     .onStatus(404, showError("Not found"))
         *     .ok(processData())
         * </pre>
         *
         * @param status the HTTP status code
         * @param action the action to execute
         * @return this builder
         */
        public FetchBuilder onStatus(int status, Action action) {
            statusHandlers.add(new StatusHandler(status, action));
            return this;
        }

        /**
         * Sets JSON body.
         *
         * @param jsonBody the JSON string
         * @return this builder
         */
        public FetchBuilder json(String jsonBody) {
            this.body = "'" + esc(jsonBody) + "'";
            this.contentType = "application/json";
            return this;
        }

        /**
         * Sets JSON body from a JavaScript expression (not escaped).
         *
         * <p>Example:</p>
         * <pre>
         * post("/api/save").jsonExpr("JSON.stringify({name: $_('name').value})")
         * </pre>
         *
         * @param jsExpr the JavaScript expression producing JSON
         * @return this builder
         */
        public FetchBuilder jsonExpr(String jsExpr) {
            this.body = jsExpr;
            this.contentType = "application/json";
            return this;
        }

        /**
         * Uses FormData from a form element.
         *
         * @param formId the form element ID
         * @return this builder
         */
        public FetchBuilder formData(String formId) {
            this.body = "new FormData($_(" + "'" + formId + "'))";
            return this;
        }

        /**
         * Uses URLSearchParams from a form element.
         *
         * @param formId the form element ID
         * @return this builder
         */
        public FetchBuilder urlEncoded(String formId) {
            this.body = "new URLSearchParams(new FormData($_(" + "'" + formId + "')))";
            this.contentType = "application/x-www-form-urlencoded";
            return this;
        }

        /**
         * Sets raw body (JavaScript expression).
         *
         * @param bodyExpr the body expression
         * @return this builder
         */
        public FetchBuilder body(String bodyExpr) {
            this.body = bodyExpr;
            return this;
        }

        /**
         * Adds a header.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public FetchBuilder header(String name, String value) {
            headers.add("'" + esc(name) + "':'" + esc(value) + "'");
            return this;
        }

        /**
         * Adds a header from a JavaScript expression.
         *
         * @param name the header name
         * @param valueExpr the JavaScript expression for the value
         * @return this builder
         */
        public FetchBuilder headerExpr(String name, String valueExpr) {
            headers.add("'" + esc(name) + "':" + valueExpr);
            return this;
        }

        /**
         * Adds Authorization Bearer header.
         *
         * @param token the token value
         * @return this builder
         */
        public FetchBuilder bearer(String token) {
            return header("Authorization", "Bearer " + token);
        }

        /**
         * Adds Authorization Bearer header from expression.
         *
         * @param tokenExpr the JavaScript expression for the token
         * @return this builder
         */
        public FetchBuilder bearerExpr(String tokenExpr) {
            return headerExpr("Authorization", "'Bearer '+(" + tokenExpr + ")");
        }

        /**
         * Sets credentials mode.
         *
         * @param mode "include", "same-origin", or "omit"
         * @return this builder
         */
        public FetchBuilder credentials(String mode) {
            this.credentials = mode;
            return this;
        }

        /**
         * Sets credentials to "include" (sends cookies cross-origin).
         *
         * @return this builder
         */
        public FetchBuilder withCredentials() {
            return credentials("include");
        }

        /**
         * Sets CORS mode.
         *
         * @param mode "cors", "no-cors", or "same-origin"
         * @return this builder
         */
        public FetchBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Chains a .then() on the response.
         * Response is available as 'r'.
         *
         * <p>Example:</p>
         * <pre>
         * get("/api/data").then("json()")  // r.json()
         * get("/api/data").then("text()")  // r.text()
         * </pre>
         *
         * @param methodCall the method to call on response (e.g., "json()", "text()")
         * @return this builder
         */
        public FetchBuilder then(String methodCall) {
            this.thenExpr = "r." + methodCall;
            return this;
        }

        /**
         * Chains .then() with a custom expression.
         *
         * @param expr the JavaScript expression (response is 'r')
         * @return this builder
         */
        public FetchBuilder thenExpr(String expr) {
            this.thenExpr = expr;
            return this;
        }

        /**
         * Action on successful response. Data is available as '_data'.
         *
         * @param action the action to execute
         * @return this builder
         */
        public FetchBuilder ok(Action action) {
            this.okAction = action;
            return this;
        }

        /**
         * Action on error. Error is available as '_err'.
         *
         * @param action the action to execute
         * @return this builder
         */
        public FetchBuilder fail(Action action) {
            this.failAction = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();

            // Build options object
            List<String> options = new ArrayList<>();
            if (!"GET".equals(method)) {
                options.add("method:'" + method + "'");
            }

            // Headers
            List<String> allHeaders = new ArrayList<>(headers);
            if (contentType != null) {
                allHeaders.add("'Content-Type':'" + contentType + "'");
            }
            if (!allHeaders.isEmpty()) {
                options.add("headers:{" + String.join(",", allHeaders) + "}");
            }

            // Body
            if (body != null) {
                options.add("body:" + body);
            }

            // Credentials
            if (credentials != null) {
                options.add("credentials:'" + credentials + "'");
            }

            // Mode
            if (mode != null) {
                options.add("mode:'" + mode + "'");
            }

            // Build fetch call with URL handling
            sb.append("fetch(");
            if (url == null) {
                // URL from variable expression
                sb.append(urlSuffix);
            } else if (!urlSuffix.isEmpty()) {
                // URL with appended variable
                sb.append("'" + esc(url) + "'+").append(urlSuffix);
            } else {
                // Plain URL
                sb.append("'" + esc(url) + "'");
            }
            if (!options.isEmpty()) {
                sb.append(",{" + String.join(",", options) + "}");
            }
            sb.append(")");

            // Handle status code checks
            if (!statusHandlers.isEmpty()) {
                sb.append(".then(r=>{");
                for (StatusHandler h : statusHandlers) {
                    sb.append("if(r.status===").append(h.status()).append("){")
                      .append(h.action().build())
                      .append("return;}");
                }
                sb.append("return r.json();})");
            }

            // Handle .then() chains
            if (thenExpr != null || okAction != null || failAction != null) {
                if (thenExpr != null && statusHandlers.isEmpty()) {
                    sb.append(".then(r=>" + thenExpr + ")");
                }
                if (okAction != null) {
                    sb.append(".then(_data=>{if(_data!==undefined){" + okAction.build() + "}})");
                }
                if (failAction != null) {
                    sb.append(".catch(_err=>{" + failAction.build() + "})");
                }
            }

            return sb.toString();
        }
    }

    // ==================== DOM Query Builder ====================

    /**
     * Creates a DOM query builder for selecting and manipulating elements.
     *
     * <p>Examples:</p>
     * <pre>
     * // Select by ID
     * query("#myDiv").hide()
     *
     * // Select all and iterate
     * query(".item").forEach(el -&gt; el.addClass("active"))
     *
     * // Chain operations
     * query("#panel").addClass("visible").setText("Hello")
     *
     * // Query with closest
     * query("#btn").closest(".card").addClass("selected")
     * </pre>
     *
     * @param selector CSS selector
     * @return a DOMQuery builder
     */
    public static DOMQuery query(String selector) {
        return new DOMQuery(selector);
    }

    /**
     * Queries all elements matching a selector.
     *
     * @param selector CSS selector
     * @return a DOMQueryAll builder
     */
    public static DOMQueryAll queryAll(String selector) {
        return new DOMQueryAll(selector);
    }

    /**
     * Builder for DOM queries and manipulations.
     */
    public static class DOMQuery implements Action {
        private final String selector;
        private final List<String> operations = new ArrayList<>();

        DOMQuery(String selector) {
            this.selector = selector;
        }

        /**
         * Adds a CSS class.
         */
        public DOMQuery addClass(String className) {
            operations.add(".classList.add('" + esc(className) + "')");
            return this;
        }

        /**
         * Removes a CSS class.
         */
        public DOMQuery removeClass(String className) {
            operations.add(".classList.remove('" + esc(className) + "')");
            return this;
        }

        /**
         * Toggles a CSS class.
         */
        public DOMQuery toggleClass(String className) {
            operations.add(".classList.toggle('" + esc(className) + "')");
            return this;
        }

        /**
         * Sets text content.
         */
        public DOMQuery setText(String text) {
            operations.add(".textContent='" + esc(text) + "'");
            return this;
        }

        /**
         * Sets text content from expression.
         */
        public DOMQuery setTextExpr(String expr) {
            operations.add(".textContent=" + expr);
            return this;
        }

        /**
         * Sets inner HTML.
         */
        public DOMQuery setHtml(String html) {
            operations.add(".innerHTML='" + esc(html) + "'");
            return this;
        }

        /**
         * Sets inner HTML from expression.
         */
        public DOMQuery setHtmlExpr(String expr) {
            operations.add(".innerHTML=" + expr);
            return this;
        }

        /**
         * Sets an attribute.
         */
        public DOMQuery attr(String name, String value) {
            operations.add(".setAttribute('" + esc(name) + "','" + esc(value) + "')");
            return this;
        }

        /**
         * Removes an attribute.
         */
        public DOMQuery removeAttr(String name) {
            operations.add(".removeAttribute('" + esc(name) + "')");
            return this;
        }

        /**
         * Sets a style property.
         */
        public DOMQuery style(String property, String value) {
            operations.add(".style." + property + "='" + esc(value) + "'");
            return this;
        }

        /**
         * Shows the element (display: block).
         */
        public DOMQuery show() {
            return style("display", "block");
        }

        /**
         * Hides the element (display: none).
         */
        public DOMQuery hide() {
            return style("display", "none");
        }

        /**
         * Sets a data attribute.
         */
        public DOMQuery data(String name, String value) {
            operations.add(".dataset." + name + "='" + esc(value) + "'");
            return this;
        }

        /**
         * Sets the value (for inputs).
         */
        public DOMQuery value(String value) {
            operations.add(".value='" + esc(value) + "'");
            return this;
        }

        /**
         * Focuses the element.
         */
        public DOMQuery focus() {
            operations.add(".focus()");
            return this;
        }

        /**
         * Blurs the element.
         */
        public DOMQuery blur() {
            operations.add(".blur()");
            return this;
        }

        /**
         * Clicks the element.
         */
        public DOMQuery click() {
            operations.add(".click()");
            return this;
        }

        /**
         * Scrolls element into view.
         */
        public DOMQuery scrollIntoView() {
            operations.add(".scrollIntoView({behavior:'smooth'})");
            return this;
        }

        /**
         * Removes the element from DOM.
         */
        public DOMQuery remove() {
            operations.add(".remove()");
            return this;
        }

        /**
         * Appends HTML content.
         */
        public DOMQuery append(String html) {
            operations.add(".insertAdjacentHTML('beforeend','" + esc(html) + "')");
            return this;
        }

        /**
         * Prepends HTML content.
         */
        public DOMQuery prepend(String html) {
            operations.add(".insertAdjacentHTML('afterbegin','" + esc(html) + "')");
            return this;
        }

        /**
         * Finds closest ancestor matching selector.
         */
        public DOMQuery closest(String ancestorSelector) {
            operations.add(".closest('" + esc(ancestorSelector) + "')");
            return this;
        }

        /**
         * Gets parent element.
         */
        public DOMQuery parent() {
            operations.add(".parentElement");
            return this;
        }

        /**
         * Queries child element.
         */
        public DOMQuery find(String childSelector) {
            operations.add(".querySelector('" + esc(childSelector) + "')");
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("document.querySelector('").append(esc(selector)).append("')");
            for (String op : operations) {
                sb.append(op);
            }
            return sb.toString();
        }
    }

    /**
     * Builder for querying all matching elements.
     */
    public static class DOMQueryAll implements Action {
        private final String selector;
        private String varName = "el";
        private final List<String> operations = new ArrayList<>();

        DOMQueryAll(String selector) {
            this.selector = selector;
        }

        /**
         * Performs operation on each element.
         *
         * <p>Example:</p>
         * <pre>
         * queryAll(".item").forEach("el", addClass("el", "active"))
         * // Output: document.querySelectorAll('.item').forEach(el=>{el.classList.add('active')})
         * </pre>
         *
         * @param elementVar variable name for each element
         * @param action the action to perform
         * @return this builder
         */
        public DOMQueryAll forEach(String elementVar, Action action) {
            this.varName = elementVar;
            operations.add(".forEach(" + elementVar + "=>{" + action.build() + "})");
            return this;
        }

        /**
         * Adds class to all elements.
         */
        public DOMQueryAll addClass(String className) {
            operations.add(".forEach(el=>el.classList.add('" + esc(className) + "'))");
            return this;
        }

        /**
         * Removes class from all elements.
         */
        public DOMQueryAll removeClass(String className) {
            operations.add(".forEach(el=>el.classList.remove('" + esc(className) + "'))");
            return this;
        }

        /**
         * Sets text on all elements.
         */
        public DOMQueryAll setText(String text) {
            operations.add(".forEach(el=>el.textContent='" + esc(text) + "')");
            return this;
        }

        /**
         * Hides all elements.
         */
        public DOMQueryAll hide() {
            operations.add(".forEach(el=>el.style.display='none')");
            return this;
        }

        /**
         * Shows all elements.
         */
        public DOMQueryAll show() {
            operations.add(".forEach(el=>el.style.display='')");
            return this;
        }

        /**
         * Removes all elements.
         */
        public DOMQueryAll remove() {
            operations.add(".forEach(el=>el.remove())");
            return this;
        }

        /**
         * Gets number of matching elements.
         */
        public Action length() {
            return () -> "document.querySelectorAll('" + esc(selector) + "').length";
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("document.querySelectorAll('").append(esc(selector)).append("')");
            for (String op : operations) {
                sb.append(op);
            }
            return sb.toString();
        }
    }

    // ==================== State Builder ====================

    public static class StateBuilder {
        private final List<String> vars = new ArrayList<>();

        public StateBuilder var(String name, String value) {
            vars.add("let " + name + "='" + esc(value) + "'");
            return this;
        }

        public StateBuilder var(String name, int value) {
            vars.add("let " + name + "=" + value);
            return this;
        }

        public StateBuilder var(String name, boolean value) {
            vars.add("let " + name + "=" + value);
            return this;
        }

        public StateBuilder array(String name) {
            vars.add("let " + name + "=[]");
            return this;
        }

        public StateBuilder object(String name) {
            vars.add("let " + name + "={}");
            return this;
        }

        public String build() {
            return String.join(";", vars) + ";";
        }
    }

    // ==================== Color Switch ====================

    /**
     * Maps a field value to different colors.
     * Generates: function name(s){return s==='X'?'#color1':s==='Y'?'#color2':'#default'}
     */
    public static class ColorSwitch {
        private final String fieldName;
        private final List<String[]> cases = new ArrayList<>();
        private String defaultColor = "#6b7280";

        ColorSwitch(String fieldName) { this.fieldName = fieldName; }

        /** Map a value to a color. */
        public ColorSwitch when(String value, String color) {
            cases.add(new String[]{value, color});
            return this;
        }

        /** Default color if no match. */
        public ColorSwitch otherwise(String color) {
            this.defaultColor = color;
            return this;
        }

        /** Build as a named function. */
        public String buildAs(String funcName) {
            StringBuilder sb = new StringBuilder();
            sb.append("function ").append(funcName).append("(s){return ");
            for (String[] c : cases) {
                sb.append("s==='").append(esc(c[0])).append("'?'").append(esc(c[1])).append("':");
            }
            sb.append("'").append(esc(defaultColor)).append("'}");
            return sb.toString();
        }
    }

    // ==================== Async Function Builder ====================

    /**
     * Defines an async function with optional early-return guards.
     */
    public static class AsyncFuncBuilder {
        private final String name;
        private final List<String> params = new ArrayList<>();
        private String guard;
        private final List<String> body = new ArrayList<>();

        AsyncFuncBuilder(String name) { this.name = name; }

        /** Add parameters. */
        public AsyncFuncBuilder params(String... params) {
            for (String p : params) this.params.add(p);
            return this;
        }

        /** Add early-return guard condition. */
        public AsyncFuncBuilder guard(String condition) {
            this.guard = condition;
            return this;
        }

        /** Add actions to the function body. */
        public AsyncFuncBuilder does(Action... actions) {
            for (Action a : actions) body.add(a.build());
            return this;
        }

        /** Add raw JS to the function body. */
        public AsyncFuncBuilder raw(String js) {
            body.add(js);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("async function ").append(name).append("(").append(String.join(",", params)).append("){");
            if (guard != null) {
                sb.append("if(").append(guard).append(")return;");
            }
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith(";") && !s.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Input Value Getter ====================

    /**
     * Gets input value and stores in a variable.
     */
    public static class InputValueGetter implements Action {
        private final String elementId;
        private String targetVar;

        InputValueGetter(String elementId) { this.elementId = elementId; }

        /** Store the value in this variable. */
        public InputValueGetter storeTo(String varName) {
            this.targetVar = varName;
            return this;
        }

        @Override
        public String build() {
            return targetVar + "=$_('" + esc(elementId) + "').value";
        }
    }

    // ==================== Status Feedback ====================

    /**
     * Manages status feedback states (loading, success, error) for an element.
     * Generates actions to set text and color based on state.
     */
    public static class StatusFeedback {
        private final String elementId;
        private String loadingText;
        private String loadingColor = "#6b7280";
        private String successText;
        private String successColor = "#065f46";
        private String errorText;
        private String errorColor = "#991b1b";

        StatusFeedback(String elementId) { this.elementId = elementId; }

        /** Set loading state text and optional color. */
        public StatusFeedback loading(String text) {
            this.loadingText = text;
            return this;
        }

        public StatusFeedback loading(String text, String color) {
            this.loadingText = text;
            this.loadingColor = color;
            return this;
        }

        /** Set success state text and optional color. */
        public StatusFeedback success(String text) {
            this.successText = text;
            return this;
        }

        public StatusFeedback success(String text, String color) {
            this.successText = text;
            this.successColor = color;
            return this;
        }

        /** Set error state text and optional color. */
        public StatusFeedback error(String text) {
            this.errorText = text;
            return this;
        }

        public StatusFeedback error(String text, String color) {
            this.errorText = text;
            this.errorColor = color;
            return this;
        }

        /** Get action to show loading state. */
        public Action showLoading() {
            return setTextAndColor(elementId, loadingText, loadingColor);
        }

        /** Get action to show success state. */
        public Action showSuccess() {
            return setTextAndColor(elementId, successText, successColor);
        }

        /** Get action to show error state. */
        public Action showError() {
            return setTextAndColor(elementId, errorText, errorColor);
        }

        /** Get action to show error with custom message. */
        public Action showErrorExpr(String msgExpr) {
            return setTextAndColorExpr(elementId, msgExpr, "'" + esc(errorColor) + "'");
        }
    }

    // ==================== Response Error Builder ====================

    /**
     * Builds conditional error messages based on HTTP response status.
     * Usage: responseError("error-el").on401("Invalid").otherwise("Error")
     */
    public static class ResponseErrorBuilder implements Action {
        private final String elementId;
        private final Map<Integer, String> statusMessages = new LinkedHashMap<>();
        private String defaultMessage = "An error occurred";
        private boolean showElement = true;

        ResponseErrorBuilder(String elementId) { this.elementId = elementId; }

        /** Message for 401 Unauthorized. */
        public ResponseErrorBuilder on401(String message) {
            statusMessages.put(401, message);
            return this;
        }

        /** Message for 403 Forbidden. */
        public ResponseErrorBuilder on403(String message) {
            statusMessages.put(403, message);
            return this;
        }

        /** Message for 404 Not Found. */
        public ResponseErrorBuilder on404(String message) {
            statusMessages.put(404, message);
            return this;
        }

        /** Message for 500 Server Error. */
        public ResponseErrorBuilder on500(String message) {
            statusMessages.put(500, message);
            return this;
        }

        /** Message for any status code. */
        public ResponseErrorBuilder onStatus(int status, String message) {
            statusMessages.put(status, message);
            return this;
        }

        /** Default message if no status matches. */
        public ResponseErrorBuilder otherwise(String message) {
            this.defaultMessage = message;
            return this;
        }

        /** Don't auto-show the element after setting text. */
        public ResponseErrorBuilder noShow() {
            this.showElement = false;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const _el=$_('" + esc(elementId) + "');_el.textContent=");

            // Build ternary chain
            for (Map.Entry<Integer, String> entry : statusMessages.entrySet()) {
                sb.append("_res?.status===").append(entry.getKey())
                  .append("?'").append(esc(entry.getValue())).append("':");
            }
            sb.append("'").append(esc(defaultMessage)).append("';");

            if (showElement) {
                sb.append("_el.style.display='block';");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== External Service Call ====================

    /**
     * Builder for calling external services like emailjs.
     * Handles checking if service is available and awaiting async calls.
     */
    public static class ExternalServiceCall implements Action {
        private final String serviceName;
        private String methodName;
        private final List<String> args = new ArrayList<>();
        private Action onSuccess;
        private Action onNotAvailable;
        private Action onError;

        ExternalServiceCall(String serviceName) { this.serviceName = serviceName; }

        /** Call a method on the service. */
        public ExternalServiceCall call(String method, String... arguments) {
            this.methodName = method;
            for (String arg : arguments) this.args.add(arg);
            return this;
        }

        /** Action on success. */
        public ExternalServiceCall ok(Action action) {
            this.onSuccess = action;
            return this;
        }

        /** Action when service is not available. */
        public ExternalServiceCall notAvailable(Action action) {
            this.onNotAvailable = action;
            return this;
        }

        /** Action on error. */
        public ExternalServiceCall fail(Action action) {
            this.onError = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("if(typeof ").append(serviceName).append("!=='undefined'){");
            sb.append("try{");
            sb.append("await ").append(serviceName).append(".").append(methodName);
            sb.append("(").append(String.join(",", args)).append(");");
            if (onSuccess != null) {
                sb.append(onSuccess.build());
                if (!onSuccess.build().endsWith(";") && !onSuccess.build().endsWith("}")) sb.append(";");
            }
            sb.append("}catch(_err){");
            if (onError != null) {
                sb.append(onError.build());
                if (!onError.build().endsWith(";") && !onError.build().endsWith("}")) sb.append(";");
            }
            sb.append("}}else{");
            if (onNotAvailable != null) {
                sb.append(onNotAvailable.build());
                if (!onNotAvailable.build().endsWith(";") && !onNotAvailable.build().endsWith("}")) sb.append(";");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== External Service Form Handler ====================

    /**
     * Handle form submission with external service call (e.g., emailjs).
     *
     * <p>Usage:</p>
     * <pre>
     * onSubmitExternal("contact-form")
     *     .loading("Sending...")
     *     .service("emailjs")
     *     .call("send", "'service_id'", "'template_id'", "{name:$_('name').value}")
     *     .ok(showMessage("status").success("Sent!"))
     *     .fail(showMessage("status").error("Failed"))
     *     .notAvailable(showMessage("status").error("Service unavailable"))
     * </pre>
     */
    public static class ExternalServiceFormHandler {
        private final String formId;
        private String loadingText;
        private String serviceName;
        private String methodName;
        private final List<String> args = new ArrayList<>();
        private Action okAction;
        private Action failAction;
        private Action notAvailableAction;

        ExternalServiceFormHandler(String formId) { this.formId = formId; }

        /** Show loading state on submit button. */
        public ExternalServiceFormHandler loading(String text) {
            this.loadingText = text;
            return this;
        }

        /** External service name (e.g., "emailjs"). */
        public ExternalServiceFormHandler service(String name) {
            this.serviceName = name;
            return this;
        }

        /** Method to call on the service with arguments. */
        public ExternalServiceFormHandler call(String method, String... arguments) {
            this.methodName = method;
            for (String arg : arguments) this.args.add(arg);
            return this;
        }

        /** Action on success. */
        public ExternalServiceFormHandler ok(Action action) {
            this.okAction = action;
            return this;
        }

        /** Action on failure. */
        public ExternalServiceFormHandler fail(Action action) {
            this.failAction = action;
            return this;
        }

        /** Action when service is not available. */
        public ExternalServiceFormHandler notAvailable(Action action) {
            this.notAvailableAction = action;
            return this;
        }

        /** Build the handler code. */
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("$_('" + formId + "')?.addEventListener('submit',async function(e){");
            sb.append("e.preventDefault();");
            sb.append("const _btn=this.querySelector('button[type=\"submit\"]');");

            if (loadingText != null) {
                sb.append("const _origText=_btn?.textContent;");
                sb.append("if(_btn){_btn.disabled=true;_btn.textContent='" + esc(loadingText) + "';}");
            }

            sb.append("try{");
            sb.append("if(typeof " + serviceName + "!=='undefined'){");
            sb.append("try{");
            sb.append("await " + serviceName + "." + methodName + "(");
            sb.append(String.join(",", args));
            sb.append(");");
            if (okAction != null) {
                sb.append(okAction.build());
                if (!okAction.build().endsWith(";") && !okAction.build().endsWith("}")) sb.append(";");
            }
            sb.append("}catch(_err){");
            if (failAction != null) {
                sb.append(failAction.build());
                if (!failAction.build().endsWith(";") && !failAction.build().endsWith("}")) sb.append(";");
            }
            sb.append("}}else{");
            if (notAvailableAction != null) {
                sb.append(notAvailableAction.build());
                if (!notAvailableAction.build().endsWith(";") && !notAvailableAction.build().endsWith("}")) {
                    sb.append(";");
                }
            }
            sb.append("}");
            sb.append("}finally{");
            if (loadingText != null) {
                sb.append("if(_btn){_btn.disabled=false;_btn.textContent=_origText;}");
            }
            sb.append("}");
            sb.append("})");
            return sb.toString();
        }
    }

    // ==================== Try/Catch Builder ====================

    /**
     * Builder for try/catch blocks.
     * Usage: tryCatch().try_(action).catch_(errorHandler)
     */
    public static class TryCatchBuilder implements Action {
        private Action tryAction;
        private Action catchAction;
        private Action finallyAction;

        /** Code to execute in try block. */
        public TryCatchBuilder try_(Action action) {
            this.tryAction = action;
            return this;
        }

        /** Code to execute on error (has access to '_err' variable). */
        public TryCatchBuilder catch_(Action action) {
            this.catchAction = action;
            return this;
        }

        /** Code to always execute. */
        public TryCatchBuilder finally_(Action action) {
            this.finallyAction = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("try{");
            if (tryAction != null) {
                sb.append(tryAction.build());
                if (!tryAction.build().endsWith(";") && !tryAction.build().endsWith("}")) sb.append(";");
            }
            sb.append("}catch(_err){");
            if (catchAction != null) {
                sb.append(catchAction.build());
                if (!catchAction.build().endsWith(";") && !catchAction.build().endsWith("}")) sb.append(";");
            }
            sb.append("}");
            if (finallyAction != null) {
                sb.append("finally{");
                sb.append(finallyAction.build());
                if (!finallyAction.build().endsWith(";") && !finallyAction.build().endsWith("}")) sb.append(";");
                sb.append("}");
            }
            return sb.toString();
        }
    }

    // ==================== Reset Vars Builder ====================

    /**
     * Resets multiple state variables to their initial values.
     */
    public static class ResetVarsBuilder implements Action {
        private final List<String> resets = new ArrayList<>();

        /** Reset a string variable. */
        public ResetVarsBuilder var(String name, String value) {
            resets.add(name + "='" + esc(value) + "'");
            return this;
        }

        /** Reset a boolean variable. */
        public ResetVarsBuilder var(String name, boolean value) {
            resets.add(name + "=" + value);
            return this;
        }

        /** Reset an array to empty. */
        public ResetVarsBuilder array(String name) {
            resets.add(name + "=[]");
            return this;
        }

        /** Reset form. */
        public ResetVarsBuilder resetForm(String formRef) {
            resets.add(formRef + "?.reset()");
            return this;
        }

        @Override
        public String build() {
            return String.join(";", resets);
        }
    }

    // ==================== Modal HTML Builder ====================

    /**
     * Shows a modal with custom HTML content.
     */
    public static class ModalHtmlBuilder implements Action {
        private final String modalId;
        private final String bodyRef;
        private String html;
        private String htmlVar;

        ModalHtmlBuilder(String modalId, String bodyRef) {
            this.modalId = modalId;
            this.bodyRef = bodyRef;
        }

        /** Set HTML from literal string. */
        public ModalHtmlBuilder html(String html) {
            this.html = html;
            return this;
        }

        /** Set HTML from JS variable. */
        public ModalHtmlBuilder fromVar(String varName) {
            this.htmlVar = varName;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append(bodyRef).append(".innerHTML=");
            if (htmlVar != null) {
                sb.append(htmlVar);
            } else if (html != null) {
                sb.append("'").append(esc(html)).append("'");
            }
            sb.append(";").append(modalId).append(".style.display='flex'");
            return sb.toString();
        }
    }

    // ==================== Alert Modal Builder ====================

    /**
     * Creates styled alert modals (success/error/info) with optional details and buttons.
     */
    public static class AlertModalBuilder implements Action {
        private final String modalId;
        private final String bodyRef;
        private String type = "info"; // success, error, info
        private String title;
        private String detail;
        private String detailVar;
        private final List<String[]> buttons = new ArrayList<>(); // [text, onclick, bgColor]
        private String inputId;
        private String inputValueVar;
        private String statusId;

        AlertModalBuilder(String modalId, String bodyRef) {
            this.modalId = modalId;
            this.bodyRef = bodyRef;
        }

        /** Success alert (green). */
        public AlertModalBuilder success(String title) {
            this.type = "success";
            this.title = title;
            return this;
        }

        /** Error alert (red). */
        public AlertModalBuilder error(String title) {
            this.type = "error";
            this.title = title;
            return this;
        }

        /** Info alert (gray). */
        public AlertModalBuilder info(String title) {
            this.type = "info";
            this.title = title;
            return this;
        }

        /** Add detail text. */
        public AlertModalBuilder detail(String text) {
            this.detail = text;
            return this;
        }

        /** Add detail from JS variable or expression. */
        public AlertModalBuilder detailExpr(String expr) {
            this.detailVar = expr;
            return this;
        }

        /** Add a readonly input field to display a value. */
        public AlertModalBuilder inputField(String id, String valueVar) {
            this.inputId = id;
            this.inputValueVar = valueVar;
            return this;
        }

        /** Add a status message area. */
        public AlertModalBuilder statusArea(String id) {
            this.statusId = id;
            return this;
        }

        /** Add a button. */
        public AlertModalBuilder button(String text, String onclick, String bgColor) {
            buttons.add(new String[]{text, onclick, bgColor});
            return this;
        }

        @Override
        public String build() {
            String titleColor = switch (type) {
                case "success" -> "#065f46";
                case "error" -> "#991b1b";
                default -> "#1f2937";
            };

            StringBuilder sb = new StringBuilder();
            sb.append(bodyRef).append(".innerHTML='<div style=\"text-align:center\">");
            sb.append("<h3 style=\"color:").append(titleColor).append(";margin-bottom:1rem\">").append(esc(title)).append("</h3>");

            // Detail text
            if (detail != null) {
                sb.append("<p style=\"color:#6b7280;margin-bottom:0.5rem\">").append(esc(detail)).append("</p>");
            } else if (detailVar != null) {
                sb.append("'+").append(detailVar).append("+'");
            }

            // Input field
            if (inputId != null) {
                sb.append("<input id=\"").append(esc(inputId)).append("\" value=\"'+").append(inputValueVar)
                  .append("+'\" readonly style=\"width:100%;padding:0.75rem;font-family:monospace;border:1px solid #e5e7eb;border-radius:8px;background:#f9fafb;margin-bottom:1rem\"/>");
            }

            // Buttons
            if (!buttons.isEmpty()) {
                sb.append("<div style=\"display:flex;gap:0.75rem;justify-content:center\">");
                for (String[] btn : buttons) {
                    sb.append("<button onclick=\"").append(btn[1]).append("\" style=\"padding:0.6rem 1.25rem;background:")
                      .append(btn[2]).append(";color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:500\">")
                      .append(esc(btn[0])).append("</button>");
                }
                sb.append("</div>");
            }

            // Status area
            if (statusId != null) {
                sb.append("<p id=\"").append(esc(statusId)).append("\" style=\"margin-top:0.75rem;font-size:0.875rem;color:#6b7280\"></p>");
            }

            sb.append("</div>';").append(modalId).append(".style.display='flex'");
            return sb.toString();
        }
    }

    // ==================== Refs Builder ====================

    /**
     * Builder for DOM element references.
     * Usage: refs().add("container", "requests-container").add("form", "login-form")
     */
    public static class RefsBuilder {
        private final List<String> refs = new ArrayList<>();

        /** Add a reference: const name = $_('elementId') */
        public RefsBuilder add(String name, String elementId) {
            refs.add(name + "=$_('" + esc(elementId) + "')");
            return this;
        }

        public String build() {
            return "const " + String.join(",", refs) + ";";
        }
    }

    // ==================== Event Handler ====================

    /**
     * General event listener for window/document events.
     */
    public static class EventHandler {
        private final String eventName;
        private String target = "window";
        private String condition;
        private Action action;

        EventHandler(String eventName) { this.eventName = eventName; }

        /** Listen on document instead of window. */
        public EventHandler onDocument() {
            this.target = "document";
            return this;
        }

        /** Listen on a specific element by variable name. */
        public EventHandler on(String elementVar) {
            this.target = elementVar;
            return this;
        }

        /** Only run action if condition is true. */
        public EventHandler when(String condition) {
            this.condition = condition;
            return this;
        }

        /** Action to run. */
        public EventHandler then(Action action) {
            this.action = action;
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append(target).append(".addEventListener('").append(esc(eventName)).append("',");
            sb.append("function(e){");
            if (condition != null) {
                sb.append("if(").append(condition).append("){");
            }
            if (action != null) {
                sb.append(action.build());
            }
            if (condition != null) {
                sb.append("}");
            }
            sb.append("})");
            return sb.toString();
        }
    }

    // ==================== Window Function Builder ====================

    /**
     * Builder for window-accessible functions with parameters.
     */
    public static class WindowFuncBuilder {
        private final String name;
        private final List<String> params = new ArrayList<>();
        private boolean isAsync = false;
        private final List<String> body = new ArrayList<>();

        WindowFuncBuilder(String name) { this.name = name; }

        /** Add parameters. */
        public WindowFuncBuilder params(String... params) {
            for (String p : params) this.params.add(p);
            return this;
        }

        /** Make function async. */
        public WindowFuncBuilder async() {
            this.isAsync = true;
            return this;
        }

        /** Add actions to the function body. */
        public WindowFuncBuilder does(Action... actions) {
            for (Action a : actions) body.add(a.build());
            return this;
        }

        /** Add raw JS to the function body. */
        public WindowFuncBuilder raw(String js) {
            body.add(js);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("window.").append(name).append("=");
            if (isAsync) sb.append("async ");
            sb.append("function(").append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith(";") && !s.endsWith("}")) sb.append(";");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Script Builder ====================

    public static class ScriptBuilder {
        private final List<String> parts = new ArrayList<>();

        /** Add helper functions (getElementById, escape, date formatting). */
        public ScriptBuilder withHelpers() {
            parts.add("const $_=id=>document.getElementById(id)");
            parts.add("function esc(t){const d=document.createElement('div');d.textContent=t||'';return d.innerHTML}");
            parts.add("function fmtDate(ts){return ts?new Date(ts).toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}):''}");
            return this;
        }

        /** Add state variables. */
        public ScriptBuilder state(StateBuilder state) {
            parts.add(state.build());
            return this;
        }

        /** Add DOM element references. */
        public ScriptBuilder refs(RefsBuilder refs) {
            parts.add(refs.build());
            return this;
        }

        /** Add a function definition. */
        public ScriptBuilder add(FunctionDef func) {
            parts.add(func.build());
            return this;
        }

        /** Add an event handler. */
        public ScriptBuilder add(FormHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(ClickHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(ChangeHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(ExternalServiceFormHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(TabHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(EventHandler handler) {
            parts.add(handler.build());
            return this;
        }

        public ScriptBuilder add(WindowFuncBuilder func) {
            parts.add(func.build());
            return this;
        }

        public ScriptBuilder add(AsyncFuncBuilder func) {
            parts.add(func.build());
            return this;
        }

        public ScriptBuilder add(ColorSwitch colorSwitch, String funcName) {
            parts.add(colorSwitch.buildAs(funcName));
            return this;
        }

        /** Add onLoad actions. */
        public ScriptBuilder onLoad(OnLoadHandler handler) {
            parts.add(handler.build());
            return this;
        }

        /** Add raw JavaScript (escape hatch). */
        public ScriptBuilder raw(String js) {
            parts.add(js);
            return this;
        }

        /** Build the complete script wrapped in IIFE. */
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("(function(){");
            for (String part : parts) {
                sb.append(part);
                // Always add semicolon for safety - JS ASI can fail with minified code
                if (!part.endsWith(";")) sb.append(";");
            }
            sb.append("})()");
            return sb.toString();
        }
    }

    // ==================== Template Builder ====================

    /**
     * Represents a value that can be used in templates.
     */
    public interface TemplateValue {
        String toTemplateJs(String itemVar);
    }

    /**
     * A field reference from the item object.
     */
    public static class TemplateField implements TemplateValue {
        private final String name;
        private final boolean escape;

        TemplateField(String name) { this.name = name; this.escape = false; }
        TemplateField(String name, boolean escape) { this.name = name; this.escape = escape; }

        @Override
        public String toTemplateJs(String itemVar) {
            if (escape) {
                return "${esc(" + itemVar + "." + name + ")}";
            }
            return "${" + itemVar + "." + name + "}";
        }
    }

    /**
     * A date field that gets formatted.
     */
    public static class TemplateDateField implements TemplateValue {
        private final String name;
        private String format = "MMM d, yyyy 'at' h:mm a";

        TemplateDateField(String name) { this.name = name; }

        public TemplateDateField format(String fmt) {
            this.format = fmt;
            return this;
        }

        @Override
        public String toTemplateJs(String itemVar) {
            return "${" + itemVar + "." + name + "?new Date(" + itemVar + "." + name + ").toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}):''}";
        }
    }

    /**
     * Builder for JS template functions.
     * Generates a function that takes an item and returns HTML string.
     */
    public static class TemplateBuilder {
        private final String itemVar;
        private final StringBuilder html = new StringBuilder();
        private final java.util.Deque<String> tagStack = new java.util.ArrayDeque<>();
        private boolean needsClosingBracket = false;

        TemplateBuilder(String itemVar) { this.itemVar = itemVar; }

        /** Start a div element. */
        public TemplateBuilder div() {
            closeOpeningTag();
            html.append("<div");
            tagStack.push("div");
            needsClosingBracket = true;
            return this;
        }

        /** Start a span element. */
        public TemplateBuilder span() {
            closeOpeningTag();
            html.append("<span");
            tagStack.push("span");
            needsClosingBracket = true;
            return this;
        }

        /** Start a button element. */
        public TemplateBuilder button() {
            closeOpeningTag();
            html.append("<button");
            tagStack.push("button");
            needsClosingBracket = true;
            return this;
        }

        /** Start a code element. */
        public TemplateBuilder code() {
            closeOpeningTag();
            html.append("<code");
            tagStack.push("code");
            needsClosingBracket = true;
            return this;
        }

        /** Start an input element (self-closing). */
        public TemplateBuilder input() {
            closeOpeningTag();
            html.append("<input");
            tagStack.push("input");
            needsClosingBracket = true;
            return this;
        }

        /** Start a p element. */
        public TemplateBuilder p() {
            closeOpeningTag();
            html.append("<p");
            tagStack.push("p");
            needsClosingBracket = true;
            return this;
        }

        /** Start an h3 element. */
        public TemplateBuilder h3() {
            closeOpeningTag();
            html.append("<h3");
            tagStack.push("h3");
            needsClosingBracket = true;
            return this;
        }

        /** Add style attribute. */
        public TemplateBuilder style(String css) {
            html.append(" style=\"").append(esc(css).replace("\"", "&quot;")).append("\"");
            return this;
        }

        /** Add id attribute. */
        public TemplateBuilder id(String id) {
            html.append(" id=\"").append(esc(id)).append("\"");
            return this;
        }

        /** Add id from field. */
        public TemplateBuilder idFromField(String fieldName) {
            html.append(" id=\"${" + itemVar + "." + fieldName + "}\"");
            return this;
        }

        /** Add class attribute. */
        public TemplateBuilder cls(String className) {
            html.append(" class=\"").append(esc(className)).append("\"");
            return this;
        }

        /** Add onclick with function call using item fields. */
        public TemplateBuilder onClick(String funcName, String... fields) {
            html.append(" onclick=\"").append(funcName).append("(");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) html.append(",");
                html.append("'${").append(itemVar).append(".").append(fields[i]).append("}'");
            }
            html.append(")\"");
            return this;
        }

        /** Add onclick with function call using escaped item fields. */
        public TemplateBuilder onClickEscaped(String funcName, String... fields) {
            html.append(" onclick=\"").append(funcName).append("(");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) html.append(",");
                html.append("'${esc(").append(itemVar).append(".").append(fields[i]).append(")}'");
            }
            html.append(")\"");
            return this;
        }

        /** Add value attribute from field. */
        public TemplateBuilder valueFromField(String fieldName) {
            html.append(" value=\"${" + itemVar + "." + fieldName + "}\"");
            return this;
        }

        /** Add readonly attribute. */
        public TemplateBuilder readonly() {
            html.append(" readonly");
            return this;
        }

        /** Add any attribute. */
        public TemplateBuilder attr(String name, String value) {
            html.append(" ").append(name).append("=\"").append(esc(value)).append("\"");
            return this;
        }

        /** Close the opening tag and add content. */
        public TemplateBuilder text(String literal) {
            closeOpeningTag();
            html.append(esc(literal));
            return this;
        }

        /** Add text from field. */
        public TemplateBuilder text(TemplateValue value) {
            closeOpeningTag();
            html.append(value.toTemplateJs(itemVar));
            return this;
        }

        /** Add raw HTML (careful - no escaping). */
        public TemplateBuilder html(String rawHtml) {
            closeOpeningTag();
            html.append(rawHtml);
            return this;
        }

        /** Add child elements (closes opening tag first). */
        public TemplateBuilder child() {
            closeOpeningTag();
            return this;
        }

        /** End current element. */
        public TemplateBuilder end() {
            String tag = tagStack.pop();
            if ("input".equals(tag)) {
                html.append("/>");
            } else {
                closeOpeningTag();
                html.append("</").append(tag).append(">");
            }
            return this;
        }

        /** Conditional block - show only if field is truthy. */
        public TemplateBuilder when(String fieldName) {
            closeOpeningTag();
            html.append("${" + itemVar + "." + fieldName + "?`");
            tagStack.push("when");
            return this;
        }

        /** Conditional block - show only if field equals value. */
        public TemplateBuilder whenEquals(String fieldName, String value) {
            closeOpeningTag();
            html.append("${" + itemVar + "." + fieldName + "==='" + esc(value) + "'?`");
            tagStack.push("when");
            return this;
        }

        /** End conditional block. */
        public TemplateBuilder endWhen() {
            tagStack.pop();
            html.append("`:''}");
            return this;
        }

        /**
         * Add a status badge with dynamic colors.
         * Usage: .badge("status", "statusBg", "statusTxt")
         * Creates a pill-shaped badge using color functions for bg/text.
         */
        public TemplateBuilder badge(String fieldName, String bgColorFunc, String txtColorFunc) {
            closeOpeningTag();
            html.append("<span style=\"padding:0.25rem 0.75rem;border-radius:9999px;font-size:0.75rem;font-weight:500;")
                .append("background:${").append(bgColorFunc).append("(").append(itemVar).append(".").append(fieldName).append(")};")
                .append("color:${").append(txtColorFunc).append("(").append(itemVar).append(".").append(fieldName).append(")}\"")
                .append(">${").append(itemVar).append(".").append(fieldName).append("}</span>");
            return this;
        }

        /**
         * Add a status badge with custom base styles.
         */
        public TemplateBuilder badge(String fieldName, String bgColorFunc, String txtColorFunc, String extraStyles) {
            closeOpeningTag();
            html.append("<span style=\"").append(esc(extraStyles)).append(";")
                .append("background:${").append(bgColorFunc).append("(").append(itemVar).append(".").append(fieldName).append(")};")
                .append("color:${").append(txtColorFunc).append("(").append(itemVar).append(".").append(fieldName).append(")}\"")
                .append(">${").append(itemVar).append(".").append(fieldName).append("}</span>");
            return this;
        }

        private void closeOpeningTag() {
            if (needsClosingBracket) {
                html.append(">");
                needsClosingBracket = false;
            }
        }

        /** Build the template function. */
        public String build() {
            return itemVar + "=>`" + html.toString() + "`";
        }

        /** Build as a named function. */
        public String buildAs(String funcName) {
            return "function " + funcName + "(" + itemVar + "){return `" + html.toString() + "`;}";
        }
    }

    // ==================== List Renderer ====================

    /**
     * Render a list of items from a JS array variable.
     */
    public static class ListRenderer implements Action {
        private final String containerId;
        private String dataVar;
        private String templateFunc;
        private String emptyMessage = "No items found";
        private String emptyStyles = "text-align:center;padding:3rem;color:#6b7280";
        private String selector = "div"; // child selector within container

        ListRenderer(String containerId) { this.containerId = containerId; }

        /** JS variable containing the array data. */
        public ListRenderer from(String varName) {
            this.dataVar = varName;
            return this;
        }

        /** Function name that generates HTML for each item. */
        public ListRenderer using(String funcName) {
            this.templateFunc = funcName;
            return this;
        }

        /** Message to show when list is empty. */
        public ListRenderer empty(String message) {
            this.emptyMessage = message;
            return this;
        }

        /** Styles for empty message. */
        public ListRenderer emptyStyles(String styles) {
            this.emptyStyles = styles;
            return this;
        }

        /** Child element selector within container (default: "div"). */
        public ListRenderer into(String selector) {
            this.selector = selector;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const _c=$_('" + containerId + "')");
            if (!"".equals(selector)) {
                sb.append(".querySelector('" + esc(selector) + "')");
            }
            sb.append(";");

            // Check if empty
            sb.append("if(!" + dataVar + ".length){");
            sb.append("_c.innerHTML='<div style=\"" + esc(emptyStyles) + "\">" + esc(emptyMessage) + "</div>';");
            sb.append("}else{");
            sb.append("_c.innerHTML=" + dataVar + ".map(" + templateFunc + ").join('');");
            sb.append("}}");
            return sb.toString();
        }
    }

    // ==================== HTML Setter ====================

    /**
     * Set innerHTML of an element with various sources.
     */
    public static class HtmlSetter implements Action {
        private final String elementId;
        private String html;
        private String htmlVar;
        private String selector;

        HtmlSetter(String elementId) { this.elementId = elementId; }

        /** Set HTML from literal string. */
        public HtmlSetter to(String html) {
            this.html = html;
            return this;
        }

        /** Set HTML from JS variable. */
        public HtmlSetter fromVar(String varName) {
            this.htmlVar = varName;
            return this;
        }

        /** Target a child element (optional). */
        public HtmlSetter child(String selector) {
            this.selector = selector;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("$_('" + elementId + "')");
            if (selector != null) {
                sb.append(".querySelector('" + esc(selector) + "')");
            }
            if (htmlVar != null) {
                sb.append(".innerHTML=" + htmlVar);
            } else if (html != null) {
                sb.append(".innerHTML='" + esc(html) + "'");
            }
            return sb.toString();
        }
    }

    // ==================== Tab Handler ====================

    /**
     * Tab switching with automatic active state management.
     */
    public static class TabHandler {
        private final String selector;
        private String stateVar;
        private String activeStyles = "backgroundColor:'white',color:'#6366f1',borderBottomColor:'#6366f1'";
        private String inactiveStyles = "backgroundColor:'transparent',color:'#6b7280',borderBottomColor:'transparent'";
        private Action onSwitchAction;
        private String dataAttr = "tab";

        TabHandler(String selector) { this.selector = selector; }

        /** JS variable to store current tab value. */
        public TabHandler storesIn(String varName) {
            this.stateVar = varName;
            return this;
        }

        /** Data attribute to read tab value from (default: "tab" for data-tab). */
        public TabHandler dataAttribute(String attr) {
            this.dataAttr = attr;
            return this;
        }

        /** Set active tab styles (CSS properties). */
        public TabHandler activeStyles(String styles) {
            this.activeStyles = styles;
            return this;
        }

        /** Set inactive tab styles (CSS properties). */
        public TabHandler inactiveStyles(String styles) {
            this.inactiveStyles = styles;
            return this;
        }

        /** Action to run when tab is switched (has access to tab value). */
        public TabHandler onSwitch(Action action) {
            this.onSwitchAction = action;
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("document.querySelectorAll('" + esc(selector) + "').forEach(function(_tab){");
            sb.append("_tab.addEventListener('click',async function(){");

            // Update state variable if defined
            if (stateVar != null) {
                sb.append(stateVar + "=this.dataset." + dataAttr + ";");
            }

            // Reset all tabs to inactive
            sb.append("document.querySelectorAll('" + esc(selector) + "').forEach(function(_t){");
            sb.append("Object.assign(_t.style,{" + inactiveStyles + "});");
            sb.append("});");

            // Set clicked tab to active
            sb.append("Object.assign(this.style,{" + activeStyles + "});");

            // Run onSwitch action
            if (onSwitchAction != null) {
                sb.append(onSwitchAction.build());
            }

            sb.append("});})");
            return sb.toString();
        }
    }

    // ==================== Download Action ====================

    public static class DownloadAction implements Action {
        private final String baseUrl;
        private final List<String> params = new ArrayList<>();
        private String filename = "download";

        DownloadAction(String baseUrl) { this.baseUrl = baseUrl; }

        /** Add URL parameter from form field. */
        public DownloadAction param(String name) {
            params.add(name + "='+encodeURIComponent(_fd.get('" + name + "'))+'");
            return this;
        }

        /** Add URL parameter with literal value. */
        public DownloadAction param(String name, String value) {
            params.add(name + "=" + esc(value));
            return this;
        }

        /** Set download filename. */
        public DownloadAction as(String filename) {
            this.filename = filename;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("{const a=document.createElement('a');a.href='").append(esc(baseUrl));
            if (!params.isEmpty()) {
                sb.append("?").append(String.join("&", params));
            }
            sb.append("';a.download='").append(esc(filename)).append("';a.click();}");
            return sb.toString();
        }
    }

    // ==================== If Action (Conditional) ====================

    public static class IfAction implements Action {
        private final String condition;
        private Action thenAction;
        private Action elseAction;

        IfAction(String condition) { this.condition = condition; }

        /** Action when condition is true. */
        public IfAction then(Action action) {
            this.thenAction = action;
            return this;
        }

        /** Action when condition is false. */
        public IfAction otherwise(Action action) {
            this.elseAction = action;
            return this;
        }

        @Override
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("if(").append(condition).append("){");
            if (thenAction != null) {
                sb.append(thenAction.build());
                if (!thenAction.build().endsWith(";") && !thenAction.build().endsWith("}")) sb.append(";");
            }
            sb.append("}");
            if (elseAction != null) {
                sb.append("else{");
                sb.append(elseAction.build());
                if (!elseAction.build().endsWith(";") && !elseAction.build().endsWith("}")) sb.append(";");
                sb.append("}");
            }
            return sb.toString();
        }
    }
}
