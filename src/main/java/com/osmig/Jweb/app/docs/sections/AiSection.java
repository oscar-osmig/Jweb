package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class AiSection {
    private AiSection() {}

    public static Element render() {
        return section(
            docTitle("AI"),
            para("Chat completions, conversations, tools, and agent loops — with zero " +
                 "extra dependencies. JWeb speaks the OpenAI wire format directly, so it " +
                 "works with OpenAI, Ollama (free local models), Groq, LM Studio, or any " +
                 "OpenAI-compatible endpoint."),

            docSubtitle("Configuration"),
            codeBlock("""
                    # application.yaml (disabled by default)
                    jweb:
                      ai:
                        enabled: true
                        base-url: ${AI_BASE_URL:https://api.openai.com/v1}
                        api-key: ${AI_API_KEY:}      # blank for local providers
                        model: ${AI_MODEL:gpt-4o-mini}"""),
            docTip("Local development without a key: install Ollama, then set " +
                   "base-url: http://localhost:11434/v1 and model: llama3.2"),

            docSubtitle("One-liners & Conversations"),
            codeBlock("""
                    String answer = AI.ask("Summarize this: " + text);

                    Chat chat = AI.chat().system("You are a concise support agent");
                    String a = chat.send("What is JWeb?");
                    String b = chat.send("Show me an example");   // history kept"""),

            docSubtitle("Agents with Tools"),
            para("Register Java functions as tools; the model loops — reason, call tools, " +
                 "see results, repeat — until it produces a final answer. Tool errors are " +
                 "fed back as text so the agent can recover; maxSteps bounds the loop."),
            codeBlock("""
                    Tool weather = Tool.of("get_weather", "Get the weather for a city")
                        .param("city", "The city name")
                        .handler(args -> weatherService.lookup((String) args.get("city")));

                    String result = AI.agent()
                        .system("You are a travel assistant")
                        .tools(weather, searchDocs)
                        .maxSteps(8)
                        .onStep((step, info) -> Log.info("step {}: {}", step, info))
                        .run("Should I pack an umbrella for Paris this weekend?");"""),

            docSubtitle("Drop-in Chat Widget"),
            para("A styled chat component plus a ready endpoint (POST /jweb/ai/chat, " +
                 "active when jweb.ai.enabled=true) with per-session history and a " +
                 "30-minute TTL."),
            codeBlock("""
                    body(
                        ...,
                        AiChatWidget.render("Ask JWeb")
                    )"""),

            docTip("Everything is testable without an API key — AiModuleTest scripts an " +
                   "OpenAI-compatible mock using the JDK's built-in HttpServer.")
        );
    }
}
