package com.osmig.Jweb.framework.websocket;

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
 *
 * <p>Allowed origins default to same-origin. To allow cross-origin
 * connections, set {@code jweb.websocket.allowed-origins} to a
 * comma-separated list of origins (or {@code *} for all — dev only).</p>
 */
@Configuration
@EnableWebSocket
public class JWebSocketConfig implements WebSocketConfigurer {

    private final JWebSocketHandler webSocketHandler;

    @Value("${jweb.websocket.allowed-origins:}")
    private String allowedOrigins;

    public JWebSocketConfig(JWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var registration = registry.addHandler(webSocketHandler, "/jweb");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            registration.setAllowedOrigins(allowedOrigins.split("\\s*,\\s*"));
        }
        // No explicit origins configured: Spring's default is same-origin only
    }
}
