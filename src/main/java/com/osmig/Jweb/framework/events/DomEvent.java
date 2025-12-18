package com.osmig.Jweb.framework.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the Event interface.
 * Constructed from data received via WebSocket.
 */
public class DomEvent implements Event {

    private final String type;
    private final String targetId;
    private final String value;
    private final String key;
    private final int keyCode;
    private final boolean ctrlKey;
    private final boolean shiftKey;
    private final boolean altKey;
    private final boolean metaKey;
    private final int clientX;
    private final int clientY;
    private final boolean checked;
    private final Map<String, String> formData;
    private final Map<String, String> dataset;

    private boolean defaultPrevented = false;
    private boolean propagationStopped = false;

    private DomEvent(Builder builder) {
        this.type = builder.type;
        this.targetId = builder.targetId;
        this.value = builder.value;
        this.key = builder.key;
        this.keyCode = builder.keyCode;
        this.ctrlKey = builder.ctrlKey;
        this.shiftKey = builder.shiftKey;
        this.altKey = builder.altKey;
        this.metaKey = builder.metaKey;
        this.clientX = builder.clientX;
        this.clientY = builder.clientY;
        this.checked = builder.checked;
        this.formData = builder.formData != null ? new HashMap<>(builder.formData) : Collections.emptyMap();
        this.dataset = builder.dataset != null ? new HashMap<>(builder.dataset) : Collections.emptyMap();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void preventDefault() {
        this.defaultPrevented = true;
    }

    @Override
    public void stopPropagation() {
        this.propagationStopped = true;
    }

    public boolean isDefaultPrevented() {
        return defaultPrevented;
    }

    public boolean isPropagationStopped() {
        return propagationStopped;
    }

    @Override
    public String value() {
        return value != null ? value : "";
    }

    @Override
    public String targetId() {
        return targetId;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public int keyCode() {
        return keyCode;
    }

    @Override
    public boolean ctrlKey() {
        return ctrlKey;
    }

    @Override
    public boolean shiftKey() {
        return shiftKey;
    }

    @Override
    public boolean altKey() {
        return altKey;
    }

    @Override
    public boolean metaKey() {
        return metaKey;
    }

    @Override
    public int clientX() {
        return clientX;
    }

    @Override
    public int clientY() {
        return clientY;
    }

    @Override
    public boolean checked() {
        return checked;
    }

    @Override
    public Map<String, String> formData() {
        return Collections.unmodifiableMap(formData);
    }

    @Override
    public String data(String name) {
        return dataset.get(name);
    }

    @Override
    public Map<String, String> dataset() {
        return Collections.unmodifiableMap(dataset);
    }

    /**
     * Builder for DomEvent.
     */
    public static class Builder {
        private String type = "unknown";
        private String targetId = "";
        private String value = "";
        private String key = null;
        private int keyCode = -1;
        private boolean ctrlKey = false;
        private boolean shiftKey = false;
        private boolean altKey = false;
        private boolean metaKey = false;
        private int clientX = -1;
        private int clientY = -1;
        private boolean checked = false;
        private Map<String, String> formData = null;
        private Map<String, String> dataset = null;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder keyCode(int keyCode) {
            this.keyCode = keyCode;
            return this;
        }

        public Builder ctrlKey(boolean ctrlKey) {
            this.ctrlKey = ctrlKey;
            return this;
        }

        public Builder shiftKey(boolean shiftKey) {
            this.shiftKey = shiftKey;
            return this;
        }

        public Builder altKey(boolean altKey) {
            this.altKey = altKey;
            return this;
        }

        public Builder metaKey(boolean metaKey) {
            this.metaKey = metaKey;
            return this;
        }

        public Builder clientX(int clientX) {
            this.clientX = clientX;
            return this;
        }

        public Builder clientY(int clientY) {
            this.clientY = clientY;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder formData(Map<String, String> formData) {
            this.formData = formData;
            return this;
        }

        public Builder dataset(Map<String, String> dataset) {
            this.dataset = dataset;
            return this;
        }

        public DomEvent build() {
            return new DomEvent(this);
        }
    }
}
