package com.osmig.Jweb.framework.hydration;

import com.osmig.Jweb.framework.state.State;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Bundles all data needed for client-side hydration.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>VNode tree (JSON)</li>
 *   <li>Initial state values (JSON)</li>
 *   <li>Session/context ID</li>
 * </ul>
 */
public class HydrationData {

    private final String vnodeJson;
    private final String stateJson;
    private final String contextId;
    private final List<String> handlers;

    private HydrationData(Builder builder) {
        this.vnodeJson = builder.vnodeJson;
        this.stateJson = builder.stateJson;
        this.contextId = builder.contextId;
        this.handlers = new ArrayList<>(builder.handlers);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates HydrationData from a VNode and current state context.
     */
    public static HydrationData from(VNode vnode) {
        Builder builder = new Builder()
                .vnode(vnode);

        StateManager.StateContext context = StateManager.getContext();
        if (context != null) {
            builder.contextId(context.getSessionId());
            builder.states(new ArrayList<>(context.getStates().values()));
        }

        return builder.build();
    }

    public String getVnodeJson() {
        return vnodeJson;
    }

    public String getStateJson() {
        return stateJson;
    }

    public String getContextId() {
        return contextId;
    }

    public List<String> getHandlers() {
        return handlers;
    }

    /**
     * Generates a script tag containing hydration data.
     *
     * <p>The JSON payload can contain user-controlled state values, so any
     * characters that could terminate the script element are neutralized
     * before embedding. Without this, a state value such as
     * {@code </script><script>alert(1)</script>} would break out of the data
     * block and execute.</p>
     */
    public String toScriptTag() {
        return "<script id=\"__JWEB_DATA__\" type=\"application/json\">" +
                escapeForScript(toJson()) +
                "</script>";
    }

    /**
     * Escapes characters that are unsafe inside an inline &lt;script&gt; element.
     * The result is still valid JSON (parsers accept {@code \\uXXXX} escapes),
     * but can no longer close the surrounding script tag or open a comment.
     */
    static String escapeForScript(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        StringBuilder sb = new StringBuilder(json.length() + 16);
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            switch (c) {
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Generates the full hydration data as JSON.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"contextId\":").append(contextId != null ? "\"" + contextId + "\"" : "null");
        sb.append(",\"vnode\":").append(vnodeJson != null ? vnodeJson : "null");
        sb.append(",\"state\":").append(stateJson != null ? stateJson : "[]");
        sb.append(",\"handlers\":[");
        for (int i = 0; i < handlers.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(handlers.get(i)).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Builder for HydrationData.
     */
    public static class Builder {
        private String vnodeJson;
        private String stateJson = "[]";
        private String contextId;
        private List<String> handlers = new ArrayList<>();

        public Builder vnode(VNode vnode) {
            this.vnodeJson = VNodeSerializer.toJson(vnode);
            return this;
        }

        public Builder vnodeJson(String json) {
            this.vnodeJson = json;
            return this;
        }

        public Builder states(List<State<?>> states) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (State<?> state : states) {
                if (!first) sb.append(",");
                sb.append(state.toJson());
                first = false;
            }
            sb.append("]");
            this.stateJson = sb.toString();
            return this;
        }

        public Builder stateJson(String json) {
            this.stateJson = json;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder addHandler(String handlerId) {
            this.handlers.add(handlerId);
            return this;
        }

        public Builder handlers(List<String> handlers) {
            this.handlers = new ArrayList<>(handlers);
            return this;
        }

        public HydrationData build() {
            return new HydrationData(this);
        }
    }
}
