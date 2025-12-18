package com.osmig.Jweb.framework.websocket;

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

    public JWebSocketConfig(JWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/jweb")
                .setAllowedOrigins("*");  // Configure for production
    }
}
