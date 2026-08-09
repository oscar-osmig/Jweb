package com.osmig.Jweb.framework.ai;

/**
 * Configuration for the AI module. Populated from {@code jweb.ai.*}
 * properties by the framework, or built manually for tests:
 *
 * <pre>
 * AI.configure(new AiConfig()
 *     .enabled(true)
 *     .baseUrl("http://localhost:11434/v1")   // Ollama
 *     .model("llama3.2"));
 * </pre>
 */
public class AiConfig {

    private boolean enabled = false;
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
    private double temperature = 0.7;
    private long timeoutSeconds = 60;

    public AiConfig enabled(boolean value) { this.enabled = value; return this; }
    public AiConfig baseUrl(String value) { this.baseUrl = value; return this; }
    public AiConfig apiKey(String value) { this.apiKey = value; return this; }
    public AiConfig model(String value) { this.model = value; return this; }
    public AiConfig temperature(double value) { this.temperature = value; return this; }
    public AiConfig timeoutSeconds(long value) { this.timeoutSeconds = value; return this; }

    public boolean isEnabled() { return enabled; }
    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public String getModel() { return model; }
    public double getTemperature() { return temperature; }
    public long getTimeoutSeconds() { return timeoutSeconds; }
}
