package com.osmig.Jweb.framework.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A Java function the model can call during an {@link Agent} run.
 *
 * <pre>
 * Tool weather = Tool.of("get_weather", "Get the current weather for a city")
 *     .param("city", "The city name")
 *     .param("unit", "celsius or fahrenheit", false)     // optional param
 *     .handler(args -> weatherService.lookup(args.get("city")));
 * </pre>
 *
 * <p>The handler receives the model's arguments as a Map and returns any
 * object — it is serialized and fed back to the model as the tool result.</p>
 */
public class Tool {

    private final String name;
    private final String description;
    private final List<Param> params = new ArrayList<>();
    private Function<Map<String, Object>, Object> handler;

    private record Param(String name, String description, boolean required) {}

    private Tool(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Creates a tool with a name (what the model calls) and description (when to use it). */
    public static Tool of(String name, String description) {
        return new Tool(name, description);
    }

    /** Adds a required string parameter. */
    public Tool param(String name, String description) {
        return param(name, description, true);
    }

    /** Adds a string parameter, required or optional. */
    public Tool param(String name, String description, boolean required) {
        params.add(new Param(name, description, required));
        return this;
    }

    /** Sets the Java function to run when the model calls this tool. */
    public Tool handler(Function<Map<String, Object>, Object> handler) {
        this.handler = handler;
        return this;
    }

    public String name() {
        return name;
    }

    /** Runs the handler with the model-supplied arguments. */
    Object invoke(Map<String, Object> arguments) {
        if (handler == null) {
            throw new AiException("Tool '" + name + "' has no handler");
        }
        return handler.apply(arguments);
    }

    /** The OpenAI function-tool JSON structure. */
    Map<String, Object> toApiDefinition() {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Param p : params) {
            properties.put(p.name(), Map.of("type", "string", "description", p.description()));
            if (p.required()) {
                required.add(p.name());
            }
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);

        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", name,
                "description", description,
                "parameters", schema));
    }
}
