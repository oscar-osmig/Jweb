package com.osmig.Jweb.framework.three;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A scene-wide setting declared as a node — background, fog and the helper
 * grid all use this shape. Settings have no transform of their own; they
 * configure the scene they appear in.
 */
public final class SceneSetting extends ThreeNode<SceneSetting> {

    private final String type;
    private final Map<String, Object> props = new LinkedHashMap<>();

    SceneSetting(String type) {
        this.type = type;
    }

    SceneSetting put(String key, Object value) {
        props.put(key, value);
        return this;
    }

    @Override
    protected String type() {
        return type;
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.putAll(props);
    }
}
