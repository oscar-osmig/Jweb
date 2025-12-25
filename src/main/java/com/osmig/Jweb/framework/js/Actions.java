package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.List;

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

    /** Represents a buildable action. */
    @FunctionalInterface
    public interface Action {
        String build();
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

        FunctionDef(String name) { this.name = name; }

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
            sb.append("function " + name + "(" + String.join(",", params) + "){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith(";") && !s.endsWith("}")) sb.append(";");
            }
            sb.append("}");
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

    // ==================== Script Builder ====================

    public static class ScriptBuilder {
        private final List<String> parts = new ArrayList<>();

        /** Add helper function for getElementById (used internally). */
        public ScriptBuilder withHelpers() {
            parts.add("const $_=id=>document.getElementById(id)");
            return this;
        }

        /** Add state variables. */
        public ScriptBuilder state(StateBuilder state) {
            parts.add(state.build());
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
                if (!part.endsWith(";") && !part.endsWith("}")) sb.append(";");
            }
            sb.append("})()");
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
