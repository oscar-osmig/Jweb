package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * Web Workers API for running JavaScript in background threads.
 *
 * <p>Web Workers provide parallel execution without blocking the main thread.
 * They enable CPU-intensive operations, background processing, and real-time
 * communication via message passing.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSWorker.*;
 *
 * // Create dedicated worker
 * worker("/worker.js")
 *     .onMessage(callback("e").log(variable("e").dot("data")))
 *     .onError(callback("e").call("console.error", variable("e")))
 *     .build("myWorker");
 *
 * // Post message to worker
 * postMessage(variable("myWorker"), obj("action", "process", "data", variable("items")))
 *
 * // Create worker from inline code (Blob Worker)
 * String workerCode = script()
 *     .add(func("onmessage", "e")
 *         .var_("result", variable("e").dot("data").times(2))
 *         .call("postMessage", variable("result"))
 *     )
 *     .build();
 * inlineWorker(workerCode)
 *     .onMessage(callback("e").log(variable("e").dot("data")))
 *     .build("worker");
 *
 * // SharedWorker for cross-tab communication
 * sharedWorker("/shared.js")
 *     .onMessage(callback("e").log(variable("e").dot("data")))
 *     .build("shared");
 *
 * // BroadcastChannel for simple cross-tab messaging
 * broadcastChannel("notifications")
 *     .onMessage(callback("e").call("showNotification", variable("e").dot("data")))
 *     .build("bc");
 * </pre>
 */
public final class JSWorker {
    private JSWorker() {}

    // ==================== Dedicated Worker ====================

    /**
     * Creates a dedicated worker from a script URL.
     *
     * @param scriptUrl the URL of the worker script
     * @return a WorkerBuilder for configuring the worker
     */
    public static WorkerBuilder worker(String scriptUrl) {
        return new WorkerBuilder("'" + JS.esc(scriptUrl) + "'", false);
    }

    /**
     * Creates a dedicated worker from a dynamic script URL.
     *
     * @param scriptUrlExpr the URL expression
     * @return a WorkerBuilder for configuring the worker
     */
    public static WorkerBuilder worker(Val scriptUrlExpr) {
        return new WorkerBuilder(scriptUrlExpr.js(), false);
    }

    /**
     * Creates a worker from inline JavaScript code using Blob and URL.createObjectURL.
     *
     * <p>This allows creating workers without separate files.</p>
     *
     * @param code the JavaScript code to run in the worker
     * @return a WorkerBuilder for configuring the worker
     */
    public static WorkerBuilder inlineWorker(String code) {
        String blobUrl = "URL.createObjectURL(new Blob(['" + JS.esc(code) + "'],{type:'application/javascript'}))";
        return new WorkerBuilder(blobUrl, true);
    }

    /**
     * Creates a worker from inline JavaScript code expression.
     *
     * @param codeExpr the JavaScript code expression
     * @return a WorkerBuilder for configuring the worker
     */
    public static WorkerBuilder inlineWorker(Val codeExpr) {
        String blobUrl = "URL.createObjectURL(new Blob([" + codeExpr.js() + "],{type:'application/javascript'}))";
        return new WorkerBuilder(blobUrl, true);
    }

    // ==================== Worker Operations ====================

    /**
     * Posts a message to a worker: worker.postMessage(message)
     *
     * @param worker the worker reference
     * @param message the message to send
     * @return a Val representing the operation
     */
    public static Val postMessage(Val worker, Val message) {
        return new Val(worker.js() + ".postMessage(" + message.js() + ")");
    }

    /**
     * Posts a message to a worker with string data.
     *
     * @param worker the worker reference
     * @param message the string message
     * @return a Val representing the operation
     */
    public static Val postMessage(Val worker, String message) {
        return new Val(worker.js() + ".postMessage('" + JS.esc(message) + "')");
    }

    /**
     * Posts a message with transferable objects: worker.postMessage(message, [transfer])
     *
     * <p>Transferable objects (like ArrayBuffer) are moved rather than copied,
     * improving performance for large data.</p>
     *
     * @param worker the worker reference
     * @param message the message to send
     * @param transferables array of transferable objects
     * @return a Val representing the operation
     */
    public static Val postMessageTransfer(Val worker, Val message, Val transferables) {
        return new Val(worker.js() + ".postMessage(" + message.js() + "," + transferables.js() + ")");
    }

    /**
     * Terminates a worker: worker.terminate()
     *
     * @param worker the worker reference
     * @return a Val representing the operation
     */
    public static Val terminate(Val worker) {
        return new Val(worker.js() + ".terminate()");
    }

    // ==================== SharedWorker ====================

    /**
     * Creates a SharedWorker for cross-tab/window communication.
     *
     * @param scriptUrl the URL of the shared worker script
     * @return a SharedWorkerBuilder for configuring the worker
     */
    public static SharedWorkerBuilder sharedWorker(String scriptUrl) {
        return new SharedWorkerBuilder("'" + JS.esc(scriptUrl) + "'");
    }

    /**
     * Creates a SharedWorker from a dynamic script URL.
     *
     * @param scriptUrlExpr the URL expression
     * @return a SharedWorkerBuilder for configuring the worker
     */
    public static SharedWorkerBuilder sharedWorker(Val scriptUrlExpr) {
        return new SharedWorkerBuilder(scriptUrlExpr.js());
    }

    /**
     * Accesses the port of a SharedWorker: sharedWorker.port
     *
     * @param sharedWorker the shared worker reference
     * @return a Val representing the port
     */
    public static Val port(Val sharedWorker) {
        return new Val(sharedWorker.js() + ".port");
    }

    /**
     * Starts a SharedWorker port: port.start()
     *
     * @param port the port reference
     * @return a Val representing the operation
     */
    public static Val portStart(Val port) {
        return new Val(port.js() + ".start()");
    }

    /**
     * Closes a SharedWorker port: port.close()
     *
     * @param port the port reference
     * @return a Val representing the operation
     */
    public static Val portClose(Val port) {
        return new Val(port.js() + ".close()");
    }

    /**
     * Posts a message to a SharedWorker port: port.postMessage(message)
     *
     * @param port the port reference
     * @param message the message to send
     * @return a Val representing the operation
     */
    public static Val portPostMessage(Val port, Val message) {
        return new Val(port.js() + ".postMessage(" + message.js() + ")");
    }

    // ==================== MessageChannel ====================

    /**
     * Creates a MessageChannel: new MessageChannel()
     *
     * <p>MessageChannel provides two-way communication between contexts.</p>
     *
     * @return a MessageChannelBuilder
     */
    public static MessageChannelBuilder messageChannel() {
        return new MessageChannelBuilder();
    }

    /**
     * Gets port1 of a MessageChannel: channel.port1
     *
     * @param channel the channel reference
     * @return a Val representing port1
     */
    public static Val port1(Val channel) {
        return new Val(channel.js() + ".port1");
    }

    /**
     * Gets port2 of a MessageChannel: channel.port2
     *
     * @param channel the channel reference
     * @return a Val representing port2
     */
    public static Val port2(Val channel) {
        return new Val(channel.js() + ".port2");
    }

    // ==================== BroadcastChannel ====================

    /**
     * Creates a BroadcastChannel for simple cross-tab messaging.
     *
     * @param channelName the channel name
     * @return a BroadcastChannelBuilder
     */
    public static BroadcastChannelBuilder broadcastChannel(String channelName) {
        return new BroadcastChannelBuilder("'" + JS.esc(channelName) + "'");
    }

    /**
     * Creates a BroadcastChannel with a dynamic name.
     *
     * @param channelNameExpr the channel name expression
     * @return a BroadcastChannelBuilder
     */
    public static BroadcastChannelBuilder broadcastChannel(Val channelNameExpr) {
        return new BroadcastChannelBuilder(channelNameExpr.js());
    }

    /**
     * Posts a message to a BroadcastChannel: channel.postMessage(message)
     *
     * @param channel the channel reference
     * @param message the message to send
     * @return a Val representing the operation
     */
    public static Val broadcastMessage(Val channel, Val message) {
        return new Val(channel.js() + ".postMessage(" + message.js() + ")");
    }

    /**
     * Closes a BroadcastChannel: channel.close()
     *
     * @param channel the channel reference
     * @return a Val representing the operation
     */
    public static Val closeBroadcast(Val channel) {
        return new Val(channel.js() + ".close()");
    }

    // ==================== Transferable Objects ====================

    /**
     * Creates an ArrayBuffer: new ArrayBuffer(byteLength)
     *
     * @param byteLength the size in bytes
     * @return a Val representing the ArrayBuffer
     */
    public static Val arrayBuffer(int byteLength) {
        return new Val("new ArrayBuffer(" + byteLength + ")");
    }

    /**
     * Creates an array of transferable objects for postMessage.
     *
     * @param transferables the transferable objects
     * @return a Val representing the transfer array
     */
    public static Val transfer(Val... transferables) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < transferables.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(transferables[i].js());
        }
        return new Val(sb.append("]").toString());
    }

    // ==================== Worker Builder ====================

    /**
     * Builder for configuring dedicated workers.
     */
    public static class WorkerBuilder {
        private final String url;
        private final boolean isBlob;
        private String workerType;
        private String workerName;
        private String credentials;
        private Func onMessageFunc;
        private String onMessageCode;
        private Func onErrorFunc;
        private String onErrorCode;
        private Func onMessageErrorFunc;
        private String onMessageErrorCode;

        WorkerBuilder(String url, boolean isBlob) {
            this.url = url;
            this.isBlob = isBlob;
        }

        /**
         * Sets the worker type.
         *
         * @param type "classic" (default) or "module" for ES6 module workers
         * @return this builder
         */
        public WorkerBuilder type(String type) {
            this.workerType = type;
            return this;
        }

        /**
         * Makes this a module worker (type: 'module').
         *
         * @return this builder
         */
        public WorkerBuilder moduleWorker() {
            return type("module");
        }

        /**
         * Sets the worker name (for debugging).
         *
         * @param name the worker name
         * @return this builder
         */
        public WorkerBuilder name(String name) {
            this.workerName = name;
            return this;
        }

        /**
         * Sets the credentials mode.
         *
         * @param credentials "omit", "same-origin", or "include"
         * @return this builder
         */
        public WorkerBuilder credentials(String credentials) {
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the message handler: worker.onmessage = handler
         *
         * @param handler the message handler function
         * @return this builder
         */
        public WorkerBuilder onMessage(Func handler) {
            this.onMessageFunc = handler;
            return this;
        }

        /**
         * Sets the message handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public WorkerBuilder onMessage(String code) {
            this.onMessageCode = code;
            return this;
        }

        /**
         * Sets the error handler: worker.onerror = handler
         *
         * @param handler the error handler function
         * @return this builder
         */
        public WorkerBuilder onError(Func handler) {
            this.onErrorFunc = handler;
            return this;
        }

        /**
         * Sets the error handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public WorkerBuilder onError(String code) {
            this.onErrorCode = code;
            return this;
        }

        /**
         * Sets the messageerror handler (for deserialization errors).
         *
         * @param handler the message error handler
         * @return this builder
         */
        public WorkerBuilder onMessageError(Func handler) {
            this.onMessageErrorFunc = handler;
            return this;
        }

        /**
         * Sets the messageerror handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public WorkerBuilder onMessageError(String code) {
            this.onMessageErrorCode = code;
            return this;
        }

        /**
         * Builds the worker and assigns it to a variable.
         *
         * @param varName the variable name for the worker
         * @return a Val representing the worker initialization code
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();

            // Create worker with options if needed
            sb.append("var ").append(varName).append("=new Worker(").append(url);

            if (workerType != null || workerName != null || credentials != null) {
                List<String> options = new ArrayList<>();
                if (workerType != null) options.add("type:'" + workerType + "'");
                if (workerName != null) options.add("name:'" + JS.esc(workerName) + "'");
                if (credentials != null) options.add("credentials:'" + credentials + "'");
                sb.append(",{").append(String.join(",", options)).append("}");
            }

            sb.append(");");

            // Add event handlers
            if (onMessageFunc != null) {
                sb.append(varName).append(".onmessage=").append(onMessageFunc.toExpr()).append(";");
            } else if (onMessageCode != null) {
                sb.append(varName).append(".onmessage=function(e){").append(onMessageCode).append("};");
            }

            if (onErrorFunc != null) {
                sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            } else if (onErrorCode != null) {
                sb.append(varName).append(".onerror=function(e){").append(onErrorCode).append("};");
            }

            if (onMessageErrorFunc != null) {
                sb.append(varName).append(".onmessageerror=").append(onMessageErrorFunc.toExpr()).append(";");
            } else if (onMessageErrorCode != null) {
                sb.append(varName).append(".onmessageerror=function(e){").append(onMessageErrorCode).append("};");
            }

            return new Val(sb.toString());
        }

        /**
         * Builds the worker as an expression (without variable assignment).
         *
         * @return a Val representing the worker expression
         */
        public Val toVal() {
            return build("worker");
        }
    }

    // ==================== SharedWorker Builder ====================

    /**
     * Builder for configuring shared workers.
     */
    public static class SharedWorkerBuilder {
        private final String url;
        private String workerName;
        private Func onMessageFunc;
        private String onMessageCode;
        private Func onErrorFunc;
        private String onErrorCode;

        SharedWorkerBuilder(String url) {
            this.url = url;
        }

        /**
         * Sets the shared worker name.
         *
         * @param name the worker name
         * @return this builder
         */
        public SharedWorkerBuilder name(String name) {
            this.workerName = name;
            return this;
        }

        /**
         * Sets the message handler on the port: worker.port.onmessage = handler
         *
         * @param handler the message handler function
         * @return this builder
         */
        public SharedWorkerBuilder onMessage(Func handler) {
            this.onMessageFunc = handler;
            return this;
        }

        /**
         * Sets the message handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public SharedWorkerBuilder onMessage(String code) {
            this.onMessageCode = code;
            return this;
        }

        /**
         * Sets the error handler: worker.onerror = handler
         *
         * @param handler the error handler function
         * @return this builder
         */
        public SharedWorkerBuilder onError(Func handler) {
            this.onErrorFunc = handler;
            return this;
        }

        /**
         * Sets the error handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public SharedWorkerBuilder onError(String code) {
            this.onErrorCode = code;
            return this;
        }

        /**
         * Builds the shared worker and assigns it to a variable.
         *
         * @param varName the variable name for the shared worker
         * @return a Val representing the worker initialization code
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();

            // Create shared worker
            sb.append("var ").append(varName).append("=new SharedWorker(").append(url);
            if (workerName != null) {
                sb.append(",'" + JS.esc(workerName) + "'");
            }
            sb.append(");");

            // Add port message handler
            if (onMessageFunc != null) {
                sb.append(varName).append(".port.onmessage=").append(onMessageFunc.toExpr()).append(";");
            } else if (onMessageCode != null) {
                sb.append(varName).append(".port.onmessage=function(e){").append(onMessageCode).append("};");
            }

            // Add error handler
            if (onErrorFunc != null) {
                sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            } else if (onErrorCode != null) {
                sb.append(varName).append(".onerror=function(e){").append(onErrorCode).append("};");
            }

            // Start the port
            sb.append(varName).append(".port.start();");

            return new Val(sb.toString());
        }

        /**
         * Builds the shared worker as an expression.
         *
         * @return a Val representing the worker expression
         */
        public Val toVal() {
            return build("sharedWorker");
        }
    }

    // ==================== MessageChannel Builder ====================

    /**
     * Builder for configuring message channels.
     */
    public static class MessageChannelBuilder {
        private Func port1MessageHandler;
        private Func port2MessageHandler;

        MessageChannelBuilder() {}

        /**
         * Sets the message handler for port1.
         *
         * @param handler the message handler
         * @return this builder
         */
        public MessageChannelBuilder onPort1Message(Func handler) {
            this.port1MessageHandler = handler;
            return this;
        }

        /**
         * Sets the message handler for port2.
         *
         * @param handler the message handler
         * @return this builder
         */
        public MessageChannelBuilder onPort2Message(Func handler) {
            this.port2MessageHandler = handler;
            return this;
        }

        /**
         * Builds the message channel and assigns it to a variable.
         *
         * @param varName the variable name for the channel
         * @return a Val representing the channel initialization code
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(varName).append("=new MessageChannel();");

            if (port1MessageHandler != null) {
                sb.append(varName).append(".port1.onmessage=").append(port1MessageHandler.toExpr()).append(";");
            }

            if (port2MessageHandler != null) {
                sb.append(varName).append(".port2.onmessage=").append(port2MessageHandler.toExpr()).append(";");
            }

            return new Val(sb.toString());
        }

        /**
         * Builds the message channel as an expression.
         *
         * @return a Val representing the channel expression
         */
        public Val toVal() {
            return new Val("new MessageChannel()");
        }
    }

    // ==================== BroadcastChannel Builder ====================

    /**
     * Builder for configuring broadcast channels.
     */
    public static class BroadcastChannelBuilder {
        private final String channelName;
        private Func onMessageFunc;
        private String onMessageCode;
        private Func onMessageErrorFunc;
        private String onMessageErrorCode;

        BroadcastChannelBuilder(String channelName) {
            this.channelName = channelName;
        }

        /**
         * Sets the message handler: channel.onmessage = handler
         *
         * @param handler the message handler function
         * @return this builder
         */
        public BroadcastChannelBuilder onMessage(Func handler) {
            this.onMessageFunc = handler;
            return this;
        }

        /**
         * Sets the message handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public BroadcastChannelBuilder onMessage(String code) {
            this.onMessageCode = code;
            return this;
        }

        /**
         * Sets the messageerror handler.
         *
         * @param handler the message error handler
         * @return this builder
         */
        public BroadcastChannelBuilder onMessageError(Func handler) {
            this.onMessageErrorFunc = handler;
            return this;
        }

        /**
         * Sets the messageerror handler with raw code.
         *
         * @param code the handler code
         * @return this builder
         */
        public BroadcastChannelBuilder onMessageError(String code) {
            this.onMessageErrorCode = code;
            return this;
        }

        /**
         * Builds the broadcast channel and assigns it to a variable.
         *
         * @param varName the variable name for the channel
         * @return a Val representing the channel initialization code
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(varName).append("=new BroadcastChannel(").append(channelName).append(");");

            if (onMessageFunc != null) {
                sb.append(varName).append(".onmessage=").append(onMessageFunc.toExpr()).append(";");
            } else if (onMessageCode != null) {
                sb.append(varName).append(".onmessage=function(e){").append(onMessageCode).append("};");
            }

            if (onMessageErrorFunc != null) {
                sb.append(varName).append(".onmessageerror=").append(onMessageErrorFunc.toExpr()).append(";");
            } else if (onMessageErrorCode != null) {
                sb.append(varName).append(".onmessageerror=function(e){").append(onMessageErrorCode).append("};");
            }

            return new Val(sb.toString());
        }

        /**
         * Builds the broadcast channel as an expression.
         *
         * @return a Val representing the channel expression
         */
        public Val toVal() {
            return new Val("new BroadcastChannel(" + channelName + ")");
        }
    }
}
