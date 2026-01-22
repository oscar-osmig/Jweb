package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * WebRTC API for real-time peer-to-peer communication.
 *
 * <p>This module provides a type-safe DSL for WebRTC operations including:</p>
 * <ul>
 *   <li>RTCPeerConnection - peer connection management</li>
 *   <li>Media Streams - getUserMedia, getDisplayMedia, device enumeration</li>
 *   <li>RTCDataChannel - data channel operations</li>
 *   <li>SDP Handling - offer/answer creation and negotiation</li>
 *   <li>ICE - ICE candidate handling and server configuration</li>
 *   <li>Statistics - connection statistics and monitoring</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSWebRTC.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Create peer connection with STUN/TURN servers
 * peerConnection()
 *     .iceServer("stun:stun.l.google.com:19302")
 *     .iceServer("turn:turn.example.com:3478", "user", "pass")
 *     .onIceCandidate(callback("e").if_(
 *         variable("e").dot("candidate"),
 *         call("sendToRemote", variable("e").dot("candidate"))
 *     ))
 *     .onTrack(callback("e").call("attachStream", variable("e").dot("streams").at(0)))
 *     .build("pc")
 *
 * // Get user media
 * getUserMedia()
 *     .video(true)
 *     .audio(true)
 *     .then(callback("stream").call("handleStream", variable("stream")))
 *     .catch_(callback("err").log(variable("err")))
 *     .build()
 *
 * // Create data channel
 * createDataChannel(variable("pc"), "chat")
 *     .ordered(true)
 *     .onOpen(callback().log(str("Channel opened")))
 *     .onMessage(callback("e").call("handleMessage", variable("e").dot("data")))
 *     .build("channel")
 *
 * // Create offer
 * createOffer(variable("pc"))
 *     .then(callback("offer").await_("", setLocalDescription(variable("pc"), variable("offer"))))
 *     .then(callback().call("sendToRemote", variable("pc").dot("localDescription")))
 *     .build()
 * </pre>
 */
public final class JSWebRTC {
    private JSWebRTC() {}

    // ==================== RTCPeerConnection ====================

    /**
     * Creates an RTCPeerConnection builder.
     *
     * @return a new PeerConnectionBuilder
     */
    public static PeerConnectionBuilder peerConnection() {
        return new PeerConnectionBuilder();
    }

    /**
     * Creates an RTCPeerConnection with simple configuration.
     *
     * @param config the configuration object
     * @return a Val representing the peer connection
     */
    public static Val peerConnection(Val config) {
        return new Val("new RTCPeerConnection(" + config.js() + ")");
    }

    /**
     * Creates a simple offer from a peer connection.
     *
     * @param pc the peer connection
     * @return a promise that resolves to the offer
     */
    public static Val createOffer(Val pc) {
        return new Val(pc.js() + ".createOffer()");
    }

    /**
     * Creates an offer with options.
     *
     * @param pc the peer connection
     * @param options the offer options
     * @return a promise that resolves to the offer
     */
    public static Val createOffer(Val pc, Val options) {
        return new Val(pc.js() + ".createOffer(" + options.js() + ")");
    }

    /**
     * Creates an answer from a peer connection.
     *
     * @param pc the peer connection
     * @return a promise that resolves to the answer
     */
    public static Val createAnswer(Val pc) {
        return new Val(pc.js() + ".createAnswer()");
    }

    /**
     * Creates an answer with options.
     *
     * @param pc the peer connection
     * @param options the answer options
     * @return a promise that resolves to the answer
     */
    public static Val createAnswer(Val pc, Val options) {
        return new Val(pc.js() + ".createAnswer(" + options.js() + ")");
    }

    /**
     * Sets the local description.
     *
     * @param pc the peer connection
     * @param description the session description
     * @return a promise that resolves when set
     */
    public static Val setLocalDescription(Val pc, Val description) {
        return new Val(pc.js() + ".setLocalDescription(" + description.js() + ")");
    }

    /**
     * Sets the remote description.
     *
     * @param pc the peer connection
     * @param description the session description
     * @return a promise that resolves when set
     */
    public static Val setRemoteDescription(Val pc, Val description) {
        return new Val(pc.js() + ".setRemoteDescription(" + description.js() + ")");
    }

    /**
     * Adds an ICE candidate.
     *
     * @param pc the peer connection
     * @param candidate the ICE candidate
     * @return a promise that resolves when added
     */
    public static Val addIceCandidate(Val pc, Val candidate) {
        return new Val(pc.js() + ".addIceCandidate(" + candidate.js() + ")");
    }

    /**
     * Adds a media track to the peer connection.
     *
     * @param pc the peer connection
     * @param track the media track
     * @param stream the media stream
     * @return a Val representing the RTP sender
     */
    public static Val addTrack(Val pc, Val track, Val stream) {
        return new Val(pc.js() + ".addTrack(" + track.js() + "," + stream.js() + ")");
    }

    /**
     * Removes a media track.
     *
     * @param pc the peer connection
     * @param sender the RTP sender
     * @return a Val representing the operation
     */
    public static Val removeTrack(Val pc, Val sender) {
        return new Val(pc.js() + ".removeTrack(" + sender.js() + ")");
    }

    /**
     * Gets the connection state.
     *
     * @param pc the peer connection
     * @return a Val representing the connection state
     */
    public static Val connectionState(Val pc) {
        return new Val(pc.js() + ".connectionState");
    }

    /**
     * Gets the ICE connection state.
     *
     * @param pc the peer connection
     * @return a Val representing the ICE connection state
     */
    public static Val iceConnectionState(Val pc) {
        return new Val(pc.js() + ".iceConnectionState");
    }

    /**
     * Gets the signaling state.
     *
     * @param pc the peer connection
     * @return a Val representing the signaling state
     */
    public static Val signalingState(Val pc) {
        return new Val(pc.js() + ".signalingState");
    }

    /**
     * Gets the local description.
     *
     * @param pc the peer connection
     * @return a Val representing the local description
     */
    public static Val localDescription(Val pc) {
        return new Val(pc.js() + ".localDescription");
    }

    /**
     * Gets the remote description.
     *
     * @param pc the peer connection
     * @return a Val representing the remote description
     */
    public static Val remoteDescription(Val pc) {
        return new Val(pc.js() + ".remoteDescription");
    }

    /**
     * Closes the peer connection.
     *
     * @param pc the peer connection
     * @return a Val representing the close operation
     */
    public static Val closePeerConnection(Val pc) {
        return new Val(pc.js() + ".close()");
    }

    /**
     * Gets connection statistics.
     *
     * @param pc the peer connection
     * @return a promise that resolves to RTCStatsReport
     */
    public static Val getStats(Val pc) {
        return new Val(pc.js() + ".getStats()");
    }

    /**
     * Gets statistics for a specific selector.
     *
     * @param pc the peer connection
     * @param selector the media stream track or RTP sender
     * @return a promise that resolves to RTCStatsReport
     */
    public static Val getStats(Val pc, Val selector) {
        return new Val(pc.js() + ".getStats(" + selector.js() + ")");
    }

    /**
     * Builder for RTCPeerConnection with fluent API.
     */
    public static class PeerConnectionBuilder {
        private final List<String> iceServers = new ArrayList<>();
        private String iceTransportPolicy;
        private String bundlePolicy;
        private String rtcpMuxPolicy;
        private Func onIceCandidateFunc, onTrackFunc, onConnectionStateChangeFunc;
        private Func onIceConnectionStateChangeFunc, onSignalingStateChangeFunc, onNegotiationNeededFunc;
        private Func onDataChannelFunc;

        PeerConnectionBuilder() {}

        /**
         * Adds a STUN server.
         *
         * @param url the STUN server URL
         * @return this builder
         */
        public PeerConnectionBuilder stunServer(String url) {
            iceServers.add("{urls:'" + JS.esc(url) + "'}");
            return this;
        }

        /**
         * Adds a TURN server with credentials.
         *
         * @param url the TURN server URL
         * @param username the username
         * @param credential the credential
         * @return this builder
         */
        public PeerConnectionBuilder turnServer(String url, String username, String credential) {
            iceServers.add("{urls:'" + JS.esc(url) + "',username:'" + JS.esc(username) +
                          "',credential:'" + JS.esc(credential) + "'}");
            return this;
        }

        /**
         * Adds an ICE server (STUN or TURN).
         *
         * @param url the server URL
         * @return this builder
         */
        public PeerConnectionBuilder iceServer(String url) {
            iceServers.add("{urls:'" + JS.esc(url) + "'}");
            return this;
        }

        /**
         * Adds an ICE server with credentials.
         *
         * @param url the server URL
         * @param username the username
         * @param credential the credential
         * @return this builder
         */
        public PeerConnectionBuilder iceServer(String url, String username, String credential) {
            return turnServer(url, username, credential);
        }

        /**
         * Sets the ICE transport policy.
         *
         * @param policy "all" or "relay"
         * @return this builder
         */
        public PeerConnectionBuilder iceTransportPolicy(String policy) {
            this.iceTransportPolicy = policy;
            return this;
        }

        /**
         * Sets the bundle policy.
         *
         * @param policy "balanced", "max-compat", or "max-bundle"
         * @return this builder
         */
        public PeerConnectionBuilder bundlePolicy(String policy) {
            this.bundlePolicy = policy;
            return this;
        }

        /**
         * Sets the RTCP mux policy.
         *
         * @param policy "negotiate" or "require"
         * @return this builder
         */
        public PeerConnectionBuilder rtcpMuxPolicy(String policy) {
            this.rtcpMuxPolicy = policy;
            return this;
        }

        /**
         * Sets the onicecandidate event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onIceCandidate(Func callback) {
            this.onIceCandidateFunc = callback;
            return this;
        }

        /**
         * Sets the ontrack event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onTrack(Func callback) {
            this.onTrackFunc = callback;
            return this;
        }

        /**
         * Sets the onconnectionstatechange event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onConnectionStateChange(Func callback) {
            this.onConnectionStateChangeFunc = callback;
            return this;
        }

        /**
         * Sets the oniceconnectionstatechange event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onIceConnectionStateChange(Func callback) {
            this.onIceConnectionStateChangeFunc = callback;
            return this;
        }

        /**
         * Sets the onsignalingstatechange event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onSignalingStateChange(Func callback) {
            this.onSignalingStateChangeFunc = callback;
            return this;
        }

        /**
         * Sets the onnegotiationneeded event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onNegotiationNeeded(Func callback) {
            this.onNegotiationNeededFunc = callback;
            return this;
        }

        /**
         * Sets the ondatachannel event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public PeerConnectionBuilder onDataChannel(Func callback) {
            this.onDataChannelFunc = callback;
            return this;
        }

        /**
         * Builds and assigns to variable.
         *
         * @param varName the variable name
         * @return a Val representing the peer connection
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();

            // Build configuration
            StringBuilder config = new StringBuilder("{");
            if (!iceServers.isEmpty()) {
                config.append("iceServers:[").append(String.join(",", iceServers)).append("]");
            }
            if (iceTransportPolicy != null) {
                if (config.length() > 1) config.append(",");
                config.append("iceTransportPolicy:'").append(iceTransportPolicy).append("'");
            }
            if (bundlePolicy != null) {
                if (config.length() > 1) config.append(",");
                config.append("bundlePolicy:'").append(bundlePolicy).append("'");
            }
            if (rtcpMuxPolicy != null) {
                if (config.length() > 1) config.append(",");
                config.append("rtcpMuxPolicy:'").append(rtcpMuxPolicy).append("'");
            }
            config.append("}");

            // Create peer connection
            sb.append("var ").append(varName).append("=new RTCPeerConnection(")
              .append(config).append(");");

            // Attach event handlers
            if (onIceCandidateFunc != null) {
                sb.append(varName).append(".onicecandidate=").append(onIceCandidateFunc.toExpr()).append(";");
            }
            if (onTrackFunc != null) {
                sb.append(varName).append(".ontrack=").append(onTrackFunc.toExpr()).append(";");
            }
            if (onConnectionStateChangeFunc != null) {
                sb.append(varName).append(".onconnectionstatechange=")
                  .append(onConnectionStateChangeFunc.toExpr()).append(";");
            }
            if (onIceConnectionStateChangeFunc != null) {
                sb.append(varName).append(".oniceconnectionstatechange=")
                  .append(onIceConnectionStateChangeFunc.toExpr()).append(";");
            }
            if (onSignalingStateChangeFunc != null) {
                sb.append(varName).append(".onsignalingstatechange=")
                  .append(onSignalingStateChangeFunc.toExpr()).append(";");
            }
            if (onNegotiationNeededFunc != null) {
                sb.append(varName).append(".onnegotiationneeded=")
                  .append(onNegotiationNeededFunc.toExpr()).append(";");
            }
            if (onDataChannelFunc != null) {
                sb.append(varName).append(".ondatachannel=")
                  .append(onDataChannelFunc.toExpr()).append(";");
            }

            return new Val(sb.toString());
        }

        /**
         * Builds as anonymous peer connection.
         *
         * @return a Val representing the peer connection
         */
        public Val toVal() {
            return build("pc");
        }
    }

    // ==================== Media Streams ====================

    /**
     * Creates a getUserMedia request builder.
     *
     * @return a new UserMediaBuilder
     */
    public static UserMediaBuilder getUserMedia() {
        return new UserMediaBuilder(false);
    }

    /**
     * Creates a getDisplayMedia request builder.
     *
     * @return a new UserMediaBuilder for screen capture
     */
    public static UserMediaBuilder getDisplayMedia() {
        return new UserMediaBuilder(true);
    }

    /**
     * Enumerates available media devices.
     *
     * @return a promise that resolves to array of MediaDeviceInfo
     */
    public static Val enumerateDevices() {
        return new Val("navigator.mediaDevices.enumerateDevices()");
    }

    /**
     * Gets tracks from a media stream.
     *
     * @param stream the media stream
     * @return a Val representing the tracks array
     */
    public static Val getTracks(Val stream) {
        return new Val(stream.js() + ".getTracks()");
    }

    /**
     * Gets audio tracks from a media stream.
     *
     * @param stream the media stream
     * @return a Val representing the audio tracks array
     */
    public static Val getAudioTracks(Val stream) {
        return new Val(stream.js() + ".getAudioTracks()");
    }

    /**
     * Gets video tracks from a media stream.
     *
     * @param stream the media stream
     * @return a Val representing the video tracks array
     */
    public static Val getVideoTracks(Val stream) {
        return new Val(stream.js() + ".getVideoTracks()");
    }

    /**
     * Stops a media stream track.
     *
     * @param track the media stream track
     * @return a Val representing the stop operation
     */
    public static Val stopTrack(Val track) {
        return new Val(track.js() + ".stop()");
    }

    /**
     * Gets the enabled state of a track.
     *
     * @param track the media stream track
     * @return a Val representing the enabled state
     */
    public static Val trackEnabled(Val track) {
        return new Val(track.js() + ".enabled");
    }

    /**
     * Sets the enabled state of a track.
     *
     * @param track the media stream track
     * @param enabled the enabled state
     * @return a Val representing the assignment
     */
    public static Val setTrackEnabled(Val track, boolean enabled) {
        return new Val(track.js() + ".enabled=" + enabled);
    }

    /**
     * Gets the kind of a track ("audio" or "video").
     *
     * @param track the media stream track
     * @return a Val representing the track kind
     */
    public static Val trackKind(Val track) {
        return new Val(track.js() + ".kind");
    }

    /**
     * Gets the label of a track.
     *
     * @param track the media stream track
     * @return a Val representing the track label
     */
    public static Val trackLabel(Val track) {
        return new Val(track.js() + ".label");
    }

    /**
     * Gets the ready state of a track.
     *
     * @param track the media stream track
     * @return a Val representing the ready state
     */
    public static Val trackReadyState(Val track) {
        return new Val(track.js() + ".readyState");
    }

    /**
     * Builder for getUserMedia and getDisplayMedia.
     */
    public static class UserMediaBuilder {
        private final boolean isDisplay;
        private Boolean audioEnabled;
        private Val audioConstraints;
        private Boolean videoEnabled;
        private Val videoConstraints;
        private final List<String> thenChain = new ArrayList<>();
        private String catchHandler;

        UserMediaBuilder(boolean isDisplay) {
            this.isDisplay = isDisplay;
        }

        /**
         * Enables audio.
         *
         * @param enabled true to enable audio
         * @return this builder
         */
        public UserMediaBuilder audio(boolean enabled) {
            this.audioEnabled = enabled;
            return this;
        }

        /**
         * Sets audio constraints.
         *
         * @param constraints the audio constraints object
         * @return this builder
         */
        public UserMediaBuilder audio(Val constraints) {
            this.audioConstraints = constraints;
            return this;
        }

        /**
         * Enables video.
         *
         * @param enabled true to enable video
         * @return this builder
         */
        public UserMediaBuilder video(boolean enabled) {
            this.videoEnabled = enabled;
            return this;
        }

        /**
         * Sets video constraints.
         *
         * @param constraints the video constraints object
         * @return this builder
         */
        public UserMediaBuilder video(Val constraints) {
            this.videoConstraints = constraints;
            return this;
        }

        /**
         * Adds a then handler.
         *
         * @param handler the callback function
         * @return this builder
         */
        public UserMediaBuilder then(Func handler) {
            thenChain.add(".then(" + handler.toExpr() + ")");
            return this;
        }

        /**
         * Adds a catch handler.
         *
         * @param handler the callback function
         * @return this builder
         */
        public UserMediaBuilder catch_(Func handler) {
            this.catchHandler = ".catch(" + handler.toExpr() + ")";
            return this;
        }

        /**
         * Builds the getUserMedia/getDisplayMedia call.
         *
         * @return the JavaScript code
         */
        public String build() {
            return toVal().js();
        }

        /**
         * Builds as a Val.
         *
         * @return a Val representing the media stream promise
         */
        public Val toVal() {
            StringBuilder sb = new StringBuilder("navigator.mediaDevices.");
            sb.append(isDisplay ? "getDisplayMedia" : "getUserMedia").append("({");

            List<String> constraints = new ArrayList<>();
            if (audioConstraints != null) {
                constraints.add("audio:" + audioConstraints.js());
            } else if (audioEnabled != null) {
                constraints.add("audio:" + audioEnabled);
            }
            if (videoConstraints != null) {
                constraints.add("video:" + videoConstraints.js());
            } else if (videoEnabled != null) {
                constraints.add("video:" + videoEnabled);
            }

            sb.append(String.join(",", constraints)).append("})");

            for (String then : thenChain) {
                sb.append(then);
            }
            if (catchHandler != null) {
                sb.append(catchHandler);
            }

            return new Val(sb.toString());
        }
    }

    // ==================== RTCDataChannel ====================

    /**
     * Creates a data channel on a peer connection.
     *
     * @param pc the peer connection
     * @param label the channel label
     * @return a DataChannelBuilder
     */
    public static DataChannelBuilder createDataChannel(Val pc, String label) {
        return new DataChannelBuilder(pc, label);
    }

    /**
     * Sends data through a data channel.
     *
     * @param channel the data channel
     * @param data the data to send
     * @return a Val representing the send operation
     */
    public static Val sendData(Val channel, Val data) {
        return new Val(channel.js() + ".send(" + data.js() + ")");
    }

    /**
     * Sends a string through a data channel.
     *
     * @param channel the data channel
     * @param message the message to send
     * @return a Val representing the send operation
     */
    public static Val sendData(Val channel, String message) {
        return new Val(channel.js() + ".send('" + JS.esc(message) + "')");
    }

    /**
     * Closes a data channel.
     *
     * @param channel the data channel
     * @return a Val representing the close operation
     */
    public static Val closeDataChannel(Val channel) {
        return new Val(channel.js() + ".close()");
    }

    /**
     * Gets the ready state of a data channel.
     *
     * @param channel the data channel
     * @return a Val representing the ready state
     */
    public static Val dataChannelReadyState(Val channel) {
        return new Val(channel.js() + ".readyState");
    }

    /**
     * Gets the buffered amount of a data channel.
     *
     * @param channel the data channel
     * @return a Val representing the buffered amount
     */
    public static Val bufferedAmount(Val channel) {
        return new Val(channel.js() + ".bufferedAmount");
    }

    /**
     * Checks if data channel is open.
     *
     * @param channel the data channel
     * @return a Val representing the check
     */
    public static Val isDataChannelOpen(Val channel) {
        return new Val("(" + channel.js() + ".readyState==='open')");
    }

    /**
     * Builder for RTCDataChannel.
     */
    public static class DataChannelBuilder {
        private final Val pc;
        private final String label;
        private Boolean ordered;
        private Integer maxPacketLifeTime;
        private Integer maxRetransmits;
        private String protocol;
        private Boolean negotiated;
        private Integer id;
        private Func onOpenFunc, onCloseFunc, onMessageFunc, onErrorFunc;

        DataChannelBuilder(Val pc, String label) {
            this.pc = pc;
            this.label = label;
        }

        /**
         * Sets ordered delivery.
         *
         * @param ordered true for ordered delivery
         * @return this builder
         */
        public DataChannelBuilder ordered(boolean ordered) {
            this.ordered = ordered;
            return this;
        }

        /**
         * Sets max packet lifetime in milliseconds.
         *
         * @param ms the max packet lifetime
         * @return this builder
         */
        public DataChannelBuilder maxPacketLifeTime(int ms) {
            this.maxPacketLifeTime = ms;
            return this;
        }

        /**
         * Sets max retransmits.
         *
         * @param count the max retransmit count
         * @return this builder
         */
        public DataChannelBuilder maxRetransmits(int count) {
            this.maxRetransmits = count;
            return this;
        }

        /**
         * Sets the protocol.
         *
         * @param protocol the protocol name
         * @return this builder
         */
        public DataChannelBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets negotiated mode.
         *
         * @param negotiated true for pre-negotiated channel
         * @return this builder
         */
        public DataChannelBuilder negotiated(boolean negotiated) {
            this.negotiated = negotiated;
            return this;
        }

        /**
         * Sets the channel ID.
         *
         * @param id the channel ID
         * @return this builder
         */
        public DataChannelBuilder id(int id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the onopen event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public DataChannelBuilder onOpen(Func callback) {
            this.onOpenFunc = callback;
            return this;
        }

        /**
         * Sets the onclose event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public DataChannelBuilder onClose(Func callback) {
            this.onCloseFunc = callback;
            return this;
        }

        /**
         * Sets the onmessage event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public DataChannelBuilder onMessage(Func callback) {
            this.onMessageFunc = callback;
            return this;
        }

        /**
         * Sets the onerror event handler.
         *
         * @param callback the callback function
         * @return this builder
         */
        public DataChannelBuilder onError(Func callback) {
            this.onErrorFunc = callback;
            return this;
        }

        /**
         * Builds and assigns to variable.
         *
         * @param varName the variable name
         * @return a Val representing the data channel
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();

            // Build options
            List<String> options = new ArrayList<>();
            if (ordered != null) options.add("ordered:" + ordered);
            if (maxPacketLifeTime != null) options.add("maxPacketLifeTime:" + maxPacketLifeTime);
            if (maxRetransmits != null) options.add("maxRetransmits:" + maxRetransmits);
            if (protocol != null) options.add("protocol:'" + JS.esc(protocol) + "'");
            if (negotiated != null) options.add("negotiated:" + negotiated);
            if (id != null) options.add("id:" + id);

            // Create data channel
            sb.append("var ").append(varName).append("=")
              .append(pc.js()).append(".createDataChannel('")
              .append(JS.esc(label)).append("'");

            if (!options.isEmpty()) {
                sb.append(",{").append(String.join(",", options)).append("}");
            }

            sb.append(");");

            // Attach event handlers
            if (onOpenFunc != null) {
                sb.append(varName).append(".onopen=").append(onOpenFunc.toExpr()).append(";");
            }
            if (onCloseFunc != null) {
                sb.append(varName).append(".onclose=").append(onCloseFunc.toExpr()).append(";");
            }
            if (onMessageFunc != null) {
                sb.append(varName).append(".onmessage=").append(onMessageFunc.toExpr()).append(";");
            }
            if (onErrorFunc != null) {
                sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            }

            return new Val(sb.toString());
        }

        /**
         * Builds as anonymous data channel.
         *
         * @return a Val representing the data channel
         */
        public Val toVal() {
            return build("channel");
        }
    }

    // ==================== SDP & Session Description ====================

    /**
     * Creates an RTCSessionDescription.
     *
     * @param type the type ("offer" or "answer")
     * @param sdp the SDP string
     * @return a Val representing the session description
     */
    public static Val sessionDescription(String type, String sdp) {
        return new Val("new RTCSessionDescription({type:'" + type + "',sdp:'" + JS.esc(sdp) + "'})");
    }

    /**
     * Creates an RTCSessionDescription from an object.
     *
     * @param desc the description object
     * @return a Val representing the session description
     */
    public static Val sessionDescription(Val desc) {
        return new Val("new RTCSessionDescription(" + desc.js() + ")");
    }

    /**
     * Gets the type of a session description.
     *
     * @param desc the session description
     * @return a Val representing the type
     */
    public static Val descriptionType(Val desc) {
        return new Val(desc.js() + ".type");
    }

    /**
     * Gets the SDP of a session description.
     *
     * @param desc the session description
     * @return a Val representing the SDP
     */
    public static Val descriptionSdp(Val desc) {
        return new Val(desc.js() + ".sdp");
    }

    // ==================== ICE Candidate ====================

    /**
     * Creates an RTCIceCandidate.
     *
     * @param candidate the candidate object
     * @return a Val representing the ICE candidate
     */
    public static Val iceCandidate(Val candidate) {
        return new Val("new RTCIceCandidate(" + candidate.js() + ")");
    }

    /**
     * Gets the candidate string.
     *
     * @param candidate the ICE candidate
     * @return a Val representing the candidate string
     */
    public static Val candidateString(Val candidate) {
        return new Val(candidate.js() + ".candidate");
    }

    /**
     * Gets the SDP mid.
     *
     * @param candidate the ICE candidate
     * @return a Val representing the SDP mid
     */
    public static Val candidateSdpMid(Val candidate) {
        return new Val(candidate.js() + ".sdpMid");
    }

    /**
     * Gets the SDP M-line index.
     *
     * @param candidate the ICE candidate
     * @return a Val representing the SDP M-line index
     */
    public static Val candidateSdpMLineIndex(Val candidate) {
        return new Val(candidate.js() + ".sdpMLineIndex");
    }

    // ==================== Constraints ====================

    /**
     * Creates video constraints.
     *
     * @return a VideoConstraintsBuilder
     */
    public static VideoConstraintsBuilder videoConstraints() {
        return new VideoConstraintsBuilder();
    }

    /**
     * Creates audio constraints.
     *
     * @return an AudioConstraintsBuilder
     */
    public static AudioConstraintsBuilder audioConstraints() {
        return new AudioConstraintsBuilder();
    }

    /**
     * Builder for video constraints.
     */
    public static class VideoConstraintsBuilder {
        private final List<String> constraints = new ArrayList<>();

        VideoConstraintsBuilder() {}

        public VideoConstraintsBuilder width(int width) {
            constraints.add("width:" + width);
            return this;
        }

        public VideoConstraintsBuilder width(int min, int ideal, int max) {
            constraints.add("width:{min:" + min + ",ideal:" + ideal + ",max:" + max + "}");
            return this;
        }

        public VideoConstraintsBuilder height(int height) {
            constraints.add("height:" + height);
            return this;
        }

        public VideoConstraintsBuilder height(int min, int ideal, int max) {
            constraints.add("height:{min:" + min + ",ideal:" + ideal + ",max:" + max + "}");
            return this;
        }

        public VideoConstraintsBuilder frameRate(int fps) {
            constraints.add("frameRate:" + fps);
            return this;
        }

        public VideoConstraintsBuilder frameRate(int min, int ideal, int max) {
            constraints.add("frameRate:{min:" + min + ",ideal:" + ideal + ",max:" + max + "}");
            return this;
        }

        public VideoConstraintsBuilder facingMode(String mode) {
            constraints.add("facingMode:'" + JS.esc(mode) + "'");
            return this;
        }

        public VideoConstraintsBuilder aspectRatio(double ratio) {
            constraints.add("aspectRatio:" + ratio);
            return this;
        }

        public Val build() {
            return new Val("{" + String.join(",", constraints) + "}");
        }
    }

    /**
     * Builder for audio constraints.
     */
    public static class AudioConstraintsBuilder {
        private final List<String> constraints = new ArrayList<>();

        AudioConstraintsBuilder() {}

        public AudioConstraintsBuilder echoCancellation(boolean enabled) {
            constraints.add("echoCancellation:" + enabled);
            return this;
        }

        public AudioConstraintsBuilder noiseSuppression(boolean enabled) {
            constraints.add("noiseSuppression:" + enabled);
            return this;
        }

        public AudioConstraintsBuilder autoGainControl(boolean enabled) {
            constraints.add("autoGainControl:" + enabled);
            return this;
        }

        public AudioConstraintsBuilder sampleRate(int rate) {
            constraints.add("sampleRate:" + rate);
            return this;
        }

        public AudioConstraintsBuilder channelCount(int count) {
            constraints.add("channelCount:" + count);
            return this;
        }

        public Val build() {
            return new Val("{" + String.join(",", constraints) + "}");
        }
    }
}
