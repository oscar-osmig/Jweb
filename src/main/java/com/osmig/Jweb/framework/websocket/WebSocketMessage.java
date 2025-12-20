package com.osmig.Jweb.framework.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * WebSocket message types for client-server communication.
 *
 * <p>Message types:</p>
 * <ul>
 *   <li><b>event</b> - Client sends event (click, change, submit)</li>
 *   <li><b>init</b> - Client initializes connection with context</li>
 *   <li><b>ping/pong</b> - Keep-alive messages</li>
 *   <li><b>stateUpdate</b> - Server sends state changes</li>
 *   <li><b>domUpdate</b> - Server sends DOM patches</li>
 *   <li><b>error</b> - Server sends error message</li>
 * </ul>
 */
public final class WebSocketMessage {

    private WebSocketMessage() {
        // Container class for message types
    }

    /**
     * Base message with type field.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Base {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Event message from client.
     * Sent when user interacts with an element that has an event handler.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventMessage extends Base {
        private String handler;
        private String eventType;
        private String targetId;
        private String value;
        private String contextId;
        private String key;
        private Integer keyCode;
        private Boolean ctrlKey;
        private Boolean shiftKey;
        private Boolean altKey;
        private Boolean metaKey;
        private Integer clientX;
        private Integer clientY;
        private Boolean checked;
        private Map<String, String> formData;
        private Map<String, String> dataset;

        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getContextId() {
            return contextId;
        }

        public void setContextId(String contextId) {
            this.contextId = contextId;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Integer getKeyCode() {
            return keyCode;
        }

        public void setKeyCode(Integer keyCode) {
            this.keyCode = keyCode;
        }

        public Boolean getCtrlKey() {
            return ctrlKey;
        }

        public void setCtrlKey(Boolean ctrlKey) {
            this.ctrlKey = ctrlKey;
        }

        public Boolean getShiftKey() {
            return shiftKey;
        }

        public void setShiftKey(Boolean shiftKey) {
            this.shiftKey = shiftKey;
        }

        public Boolean getAltKey() {
            return altKey;
        }

        public void setAltKey(Boolean altKey) {
            this.altKey = altKey;
        }

        public Boolean getMetaKey() {
            return metaKey;
        }

        public void setMetaKey(Boolean metaKey) {
            this.metaKey = metaKey;
        }

        public Integer getClientX() {
            return clientX;
        }

        public void setClientX(Integer clientX) {
            this.clientX = clientX;
        }

        public Integer getClientY() {
            return clientY;
        }

        public void setClientY(Integer clientY) {
            this.clientY = clientY;
        }

        public Boolean getChecked() {
            return checked;
        }

        public void setChecked(Boolean checked) {
            this.checked = checked;
        }

        public Map<String, String> getFormData() {
            return formData;
        }

        public void setFormData(Map<String, String> formData) {
            this.formData = formData;
        }

        public Map<String, String> getDataset() {
            return dataset;
        }

        public void setDataset(Map<String, String> dataset) {
            this.dataset = dataset;
        }
    }

    /**
     * Init message from client.
     * Sent when WebSocket connection is established.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitMessage extends Base {
        private String contextId;

        public String getContextId() {
            return contextId;
        }

        public void setContextId(String contextId) {
            this.contextId = contextId;
        }
    }

    /**
     * Connected response from server.
     * Sent after WebSocket connection is established.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConnectedResponse extends Base {
        private String sessionId;

        public ConnectedResponse() {
            setType("connected");
        }

        public ConnectedResponse(String sessionId) {
            this();
            this.sessionId = sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * State update response from server.
     * Sent when state changes after event handling.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StateUpdateResponse extends Base {
        private List<StateData> states;

        public StateUpdateResponse() {
            setType("stateUpdate");
        }

        public StateUpdateResponse(List<StateData> states) {
            this();
            this.states = states;
        }

        public List<StateData> getStates() {
            return states;
        }

        public void setStates(List<StateData> states) {
            this.states = states;
        }
    }

    /**
     * State data for serialization.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StateData {
        private String id;
        private Object value;

        public StateData() {}

        public StateData(String id, Object value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    /**
     * DOM update response from server.
     * Sent when components need to be re-rendered.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DomUpdateResponse extends Base {
        private List<DomPatch> updates;

        public DomUpdateResponse() {
            setType("domUpdate");
        }

        public DomUpdateResponse(List<DomPatch> updates) {
            this();
            this.updates = updates;
        }

        public List<DomPatch> getUpdates() {
            return updates;
        }

        public void setUpdates(List<DomPatch> updates) {
            this.updates = updates;
        }
    }

    /**
     * DOM patch for a single component.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DomPatch {
        private String id;
        private String html;

        public DomPatch() {}

        public DomPatch(String id, String html) {
            this.id = id;
            this.html = html;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }
    }

    /**
     * Event handled response from server.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventHandledResponse extends Base {
        private String handler;
        private boolean preventDefault;

        public EventHandledResponse() {
            setType("eventHandled");
        }

        public EventHandledResponse(String handler, boolean preventDefault) {
            this();
            this.handler = handler;
            this.preventDefault = preventDefault;
        }

        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public boolean isPreventDefault() {
            return preventDefault;
        }

        public void setPreventDefault(boolean preventDefault) {
            this.preventDefault = preventDefault;
        }
    }

    /**
     * Error response from server.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse extends Base {
        private String message;
        private String code;

        public ErrorResponse() {
            setType("error");
        }

        public ErrorResponse(String message) {
            this();
            this.message = message;
        }

        public ErrorResponse(String message, String code) {
            this();
            this.message = message;
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * Pong response from server.
     */
    public static class PongResponse extends Base {
        public PongResponse() {
            setType("pong");
        }
    }

    /**
     * Init state response from server.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InitStateResponse extends Base {
        private List<StateData> states;

        public InitStateResponse() {
            setType("initState");
        }

        public InitStateResponse(List<StateData> states) {
            this();
            this.states = states;
        }

        public List<StateData> getStates() {
            return states;
        }

        public void setStates(List<StateData> states) {
            this.states = states;
        }
    }
}
