package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * WebSocket API for real-time communication.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSWebSocket.*;
 *
 * // Create WebSocket with handlers
 * webSocket("/ws/chat")
 *     .onOpen(callback().log(str("Connected")))
 *     .onMessage(callback("e").call("handleMessage", variable("e").dot("data")))
 *     .onClose(callback().log(str("Disconnected")))
 *     .onError(callback("e").log(variable("e")))
 *     .build("ws")
 *
 * // Send message
 * send(variable("ws"), variable("message"))
 *
 * // Send JSON
 * sendJson(variable("ws"), obj("type", "chat", "text", variable("msg")))
 * </pre>
 */
public final class JSWebSocket {
    private JSWebSocket() {}

    // ==================== Creation ====================

    /** Creates a WebSocket connection */
    public static WebSocketBuilder webSocket(String url) {
        return new WebSocketBuilder(url);
    }

    /** Creates a WebSocket with dynamic URL */
    public static WebSocketBuilder webSocket(Val urlExpr) {
        return new WebSocketBuilder(urlExpr);
    }

    /** Creates WebSocket URL from current host */
    public static Val wsUrl(String path) {
        return new Val("(location.protocol==='https:'?'wss://':'ws://')+location.host+'" + JS.esc(path) + "'");
    }

    // ==================== Operations ====================

    /** Sends data through WebSocket */
    public static Val send(Val ws, Val data) {
        return new Val(ws.js() + ".send(" + data.js() + ")");
    }

    /** Sends string message */
    public static Val send(Val ws, String message) {
        return new Val(ws.js() + ".send('" + JS.esc(message) + "')");
    }

    /** Sends JSON data */
    public static Val sendJson(Val ws, Val data) {
        return new Val(ws.js() + ".send(JSON.stringify(" + data.js() + "))");
    }

    /** Closes WebSocket connection */
    public static Val close(Val ws) {
        return new Val(ws.js() + ".close()");
    }

    /** Closes with code and reason */
    public static Val close(Val ws, int code, String reason) {
        return new Val(ws.js() + ".close(" + code + ",'" + JS.esc(reason) + "')");
    }

    // ==================== State ====================

    /** Gets readyState: 0=CONNECTING, 1=OPEN, 2=CLOSING, 3=CLOSED */
    public static Val readyState(Val ws) {
        return new Val(ws.js() + ".readyState");
    }

    /** Checks if WebSocket is open */
    public static Val isOpen(Val ws) {
        return new Val("(" + ws.js() + ".readyState===1)");
    }

    /** Checks if WebSocket is connecting */
    public static Val isConnecting(Val ws) {
        return new Val("(" + ws.js() + ".readyState===0)");
    }

    /** Gets bufferedAmount */
    public static Val bufferedAmount(Val ws) {
        return new Val(ws.js() + ".bufferedAmount");
    }

    // ==================== Builder ====================

    public static class WebSocketBuilder {
        private final String url;
        private final boolean isDynamic;
        private Func onOpenFunc, onMessageFunc, onCloseFunc, onErrorFunc;
        private String onOpenCode, onMessageCode, onCloseCode, onErrorCode;
        private String protocols;
        private boolean autoReconnect;
        private int reconnectDelay = 3000;

        WebSocketBuilder(String url) {
            this.url = "'" + JS.esc(url) + "'";
            this.isDynamic = false;
        }

        WebSocketBuilder(Val urlExpr) {
            this.url = urlExpr.js();
            this.isDynamic = true;
        }

        public WebSocketBuilder protocols(String... protocols) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < protocols.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("'").append(JS.esc(protocols[i])).append("'");
            }
            this.protocols = sb.append("]").toString();
            return this;
        }

        public WebSocketBuilder onOpen(Func callback) { this.onOpenFunc = callback; return this; }
        public WebSocketBuilder onOpen(String code) { this.onOpenCode = code; return this; }

        public WebSocketBuilder onMessage(Func callback) { this.onMessageFunc = callback; return this; }
        public WebSocketBuilder onMessage(String code) { this.onMessageCode = code; return this; }

        public WebSocketBuilder onClose(Func callback) { this.onCloseFunc = callback; return this; }
        public WebSocketBuilder onClose(String code) { this.onCloseCode = code; return this; }

        public WebSocketBuilder onError(Func callback) { this.onErrorFunc = callback; return this; }
        public WebSocketBuilder onError(String code) { this.onErrorCode = code; return this; }

        public WebSocketBuilder autoReconnect(int delayMs) {
            this.autoReconnect = true;
            this.reconnectDelay = delayMs;
            return this;
        }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(varName).append("=new WebSocket(").append(url);
            if (protocols != null) sb.append(",").append(protocols);
            sb.append(");");

            if (onOpenFunc != null) sb.append(varName).append(".onopen=").append(onOpenFunc.toExpr()).append(";");
            else if (onOpenCode != null) sb.append(varName).append(".onopen=function(e){").append(onOpenCode).append("};");

            if (onMessageFunc != null) sb.append(varName).append(".onmessage=").append(onMessageFunc.toExpr()).append(";");
            else if (onMessageCode != null) sb.append(varName).append(".onmessage=function(e){").append(onMessageCode).append("};");

            if (onErrorFunc != null) sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            else if (onErrorCode != null) sb.append(varName).append(".onerror=function(e){").append(onErrorCode).append("};");

            if (autoReconnect) {
                sb.append(varName).append(".onclose=function(e){");
                if (onCloseFunc != null) sb.append("(").append(onCloseFunc.toExpr()).append(")(e);");
                else if (onCloseCode != null) sb.append(onCloseCode);
                sb.append("setTimeout(function(){").append(varName).append("=new WebSocket(").append(url);
                if (protocols != null) sb.append(",").append(protocols);
                sb.append(")},").append(reconnectDelay).append(")};");
            } else {
                if (onCloseFunc != null) sb.append(varName).append(".onclose=").append(onCloseFunc.toExpr()).append(";");
                else if (onCloseCode != null) sb.append(varName).append(".onclose=function(e){").append(onCloseCode).append("};");
            }

            return new Val(sb.toString());
        }

        public Val toVal() { return build("ws"); }
    }
}
