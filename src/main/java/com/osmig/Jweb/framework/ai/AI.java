package com.osmig.Jweb.framework.ai;

/**
 * AI integration for JWeb — chat completions, conversations, tools, and
 * agent loops against any OpenAI-compatible API (OpenAI, Ollama, Groq,
 * LM Studio, ...). No extra dependencies; plain HTTP via the framework's
 * Fetch client.
 *
 * <p>Configure once (application.yaml):</p>
 * <pre>
 * jweb:
 *   ai:
 *     enabled: true
 *     base-url: https://api.openai.com/v1     # or http://localhost:11434/v1 for Ollama
 *     api-key: ${AI_API_KEY:}
 *     model: gpt-4o-mini
 * </pre>
 *
 * <p>One-liners:</p>
 * <pre>
 * String answer = AI.ask("Summarize: " + text);
 * </pre>
 *
 * <p>Conversations (history kept):</p>
 * <pre>
 * Chat chat = AI.chat().system("You are a helpful support agent");
 * String a = chat.send("Hello!");
 * String b = chat.send("And a follow-up");
 * </pre>
 *
 * <p>Agents — the model loops, calling your Java tools until done:</p>
 * <pre>
 * Tool weather = Tool.of("get_weather", "Get the weather for a city")
 *     .param("city", "The city name")
 *     .handler(args -> weatherService.lookup(args.get("city")));
 *
 * String result = AI.agent()
 *     .system("You are a travel assistant")
 *     .tools(weather)
 *     .maxSteps(8)
 *     .run("Should I pack an umbrella for Paris this weekend?");
 * </pre>
 */
public final class AI {

    private static volatile AiConfig config = new AiConfig();

    private AI() {}

    /** Applies configuration. Called by the framework from jweb.ai.* properties. */
    public static void configure(AiConfig newConfig) {
        config = newConfig;
    }

    /** The active configuration. */
    public static AiConfig config() {
        return config;
    }

    /** True when an endpoint is configured (enabled + base URL present). */
    public static boolean isConfigured() {
        return config.isEnabled() && config.getBaseUrl() != null && !config.getBaseUrl().isBlank();
    }

    /**
     * Asks a single question and returns the model's reply.
     * Stateless — no history is kept.
     */
    public static String ask(String prompt) {
        return chat().send(prompt);
    }

    /** Starts a new conversation builder. */
    public static Chat chat() {
        return new Chat(config);
    }

    /** Starts an agent builder (a conversation that can call tools in a loop). */
    public static Agent agent() {
        return new Agent(config);
    }
}
