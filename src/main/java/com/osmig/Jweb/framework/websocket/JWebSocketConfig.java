package com.osmig.Jweb.framework.websocket;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for JWeb.
 *
 * <p>Enables the /jweb WebSocket endpoint that handles:</p>
 * <ul>
 *   <li>Event handler execution</li>
 *   <li>State synchronization</li>
 *   <li>DOM updates</li>
 * </ul>
 */
@Configuration
@EnableWebSocket
public class JWebSocketConfig implements WebSocketConfigurer {

    private final JWebSocketHandler webSocketHandler;

    /**
     * Comma-separated list of origins allowed to open the WebSocket. Set via
     * {@code jweb.websocket.allowed-origins} (property) or
     * {@code JWEB_WS_ALLOWED_ORIGINS} (env). When empty (the default) only
     * same-origin connections are accepted, which prevents cross-site
     * WebSocket hijacking. Use {@code *} only for local experimentation.
     */
    @Value("${jweb.websocket.allowed-origins:${JWEB_WS_ALLOWED_ORIGINS:}}")
    private String allowedOrigins;

    public JWebSocketConfig(JWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var registration = registry.addHandler(webSocketHandler, "/jweb");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            String[] origins = allowedOrigins.split("\\s*,\\s*");
            registration.setAllowedOrigins(origins);
            Log.info("JWeb WebSocket allowed origins: {}", String.join(", ", origins));
        }
        // Otherwise leave Spring's default (same-origin only) in place rather
        // than opening the socket to every origin.
    }
}
