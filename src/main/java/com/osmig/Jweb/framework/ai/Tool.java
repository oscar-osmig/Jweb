package com.osmig.Jweb.framework.ai;

/**
 * The pre-3.0 spelling of a model-callable tool.
 *
 * @deprecated Replaced by {@link jweb.Tool} — {@code Tool.of(...)} (inherited
 *             here) hands out that type, so declare {@code jweb.Tool}. This
 *             subclass only keeps old subclasses compiling.
 */
@Deprecated
public class Tool extends jweb.Tool {

    protected Tool(String name, String description) {
        super(name, description);
    }
}
