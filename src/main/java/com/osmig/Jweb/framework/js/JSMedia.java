package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * Audio/Video and Media APIs for media playback, recording, and session management.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSMedia.*;
 *
 * // Audio/Video element control
 * Val video = getElem("myVideo");
 * play(video);
 * pause(video);
 * setVolume(video, 0.5);
 * setCurrentTime(video, 30);
 *
 * // Media events
 * video()
 *     .src("/video.mp4")
 *     .onPlay(callback().log(str("Playing")))
 *     .onPause(callback().log(str("Paused")))
 *     .onEnded(callback().call("onVideoEnd"))
 *     .build("videoPlayer");
 *
 * // Media Recorder
 * mediaRecorder(variable("stream"))
 *     .mimeType("video/webm")
 *     .onDataAvailable(callback("e").call("handleData", variable("e").dot("data")))
 *     .onStop(callback().log(str("Recording stopped")))
 *     .build("recorder");
 *
 * // Picture-in-Picture
 * requestPiP(video);
 * exitPiP();
 *
 * // Media Session API
 * mediaSession()
 *     .metadata("Song Title", "Artist Name", "Album", "/album-art.jpg")
 *     .playbackState("playing")
 *     .onPlay(callback().call("play"))
 *     .onPause(callback().call("pause"))
 *     .build();
 * </pre>
 */
public final class JSMedia {
    private JSMedia() {}

    // ==================== HTMLMediaElement Control ====================

    /** Plays media: elem.play() */
    public static Val play(Val media) {
        return new Val(media.js() + ".play()");
    }

    /** Pauses media: elem.pause() */
    public static Val pause(Val media) {
        return new Val(media.js() + ".pause()");
    }

    /** Loads media: elem.load() */
    public static Val load(Val media) {
        return new Val(media.js() + ".load()");
    }

    /** Checks if element can play type: elem.canPlayType(type) */
    public static Val canPlayType(Val media, String mimeType) {
        return new Val(media.js() + ".canPlayType('" + JS.esc(mimeType) + "')");
    }

    // ==================== Media Properties ====================

    /** Gets currentTime: elem.currentTime */
    public static Val currentTime(Val media) {
        return new Val(media.js() + ".currentTime");
    }

    /** Sets currentTime: elem.currentTime = time */
    public static Val setCurrentTime(Val media, double seconds) {
        return new Val(media.js() + ".currentTime=" + seconds);
    }

    /** Sets currentTime from expression: elem.currentTime = expr */
    public static Val setCurrentTime(Val media, Val seconds) {
        return new Val(media.js() + ".currentTime=" + seconds.js());
    }

    /** Gets duration: elem.duration */
    public static Val duration(Val media) {
        return new Val(media.js() + ".duration");
    }

    /** Gets volume: elem.volume (0.0 to 1.0) */
    public static Val volume(Val media) {
        return new Val(media.js() + ".volume");
    }

    /** Sets volume: elem.volume = vol */
    public static Val setVolume(Val media, double vol) {
        return new Val(media.js() + ".volume=" + vol);
    }

    /** Sets volume from expression: elem.volume = expr */
    public static Val setVolume(Val media, Val vol) {
        return new Val(media.js() + ".volume=" + vol.js());
    }

    /** Gets muted state: elem.muted */
    public static Val muted(Val media) {
        return new Val(media.js() + ".muted");
    }

    /** Sets muted: elem.muted = true/false */
    public static Val setMuted(Val media, boolean muted) {
        return new Val(media.js() + ".muted=" + muted);
    }

    /** Sets muted from expression: elem.muted = expr */
    public static Val setMuted(Val media, Val muted) {
        return new Val(media.js() + ".muted=" + muted.js());
    }

    /** Gets playbackRate: elem.playbackRate */
    public static Val playbackRate(Val media) {
        return new Val(media.js() + ".playbackRate");
    }

    /** Sets playbackRate: elem.playbackRate = rate */
    public static Val setPlaybackRate(Val media, double rate) {
        return new Val(media.js() + ".playbackRate=" + rate);
    }

    /** Sets playbackRate from expression: elem.playbackRate = expr */
    public static Val setPlaybackRate(Val media, Val rate) {
        return new Val(media.js() + ".playbackRate=" + rate.js());
    }

    /** Gets paused state: elem.paused */
    public static Val paused(Val media) {
        return new Val(media.js() + ".paused");
    }

    /** Gets ended state: elem.ended */
    public static Val ended(Val media) {
        return new Val(media.js() + ".ended");
    }

    /** Gets seeking state: elem.seeking */
    public static Val seeking(Val media) {
        return new Val(media.js() + ".seeking");
    }

    /** Gets readyState: elem.readyState (0=HAVE_NOTHING, 1=HAVE_METADATA, 2=HAVE_CURRENT_DATA, 3=HAVE_FUTURE_DATA, 4=HAVE_ENOUGH_DATA) */
    public static Val readyState(Val media) {
        return new Val(media.js() + ".readyState");
    }

    /** Gets src: elem.src */
    public static Val src(Val media) {
        return new Val(media.js() + ".src");
    }

    /** Sets src: elem.src = url */
    public static Val setSrc(Val media, String url) {
        return new Val(media.js() + ".src='" + JS.esc(url) + "'");
    }

    /** Sets src from expression: elem.src = expr */
    public static Val setSrc(Val media, Val url) {
        return new Val(media.js() + ".src=" + url.js());
    }

    /** Gets currentSrc: elem.currentSrc */
    public static Val currentSrc(Val media) {
        return new Val(media.js() + ".currentSrc");
    }

    /** Gets loop: elem.loop */
    public static Val loop(Val media) {
        return new Val(media.js() + ".loop");
    }

    /** Sets loop: elem.loop = true/false */
    public static Val setLoop(Val media, boolean loop) {
        return new Val(media.js() + ".loop=" + loop);
    }

    /** Gets autoplay: elem.autoplay */
    public static Val autoplay(Val media) {
        return new Val(media.js() + ".autoplay");
    }

    /** Sets autoplay: elem.autoplay = true/false */
    public static Val setAutoplay(Val media, boolean autoplay) {
        return new Val(media.js() + ".autoplay=" + autoplay);
    }

    /** Gets controls: elem.controls */
    public static Val controls(Val media) {
        return new Val(media.js() + ".controls");
    }

    /** Sets controls: elem.controls = true/false */
    public static Val setControls(Val media, boolean controls) {
        return new Val(media.js() + ".controls=" + controls);
    }

    // ==================== TimeRanges ====================

    /** Gets buffered TimeRanges: elem.buffered */
    public static Val buffered(Val media) {
        return new Val(media.js() + ".buffered");
    }

    /** Gets seekable TimeRanges: elem.seekable */
    public static Val seekable(Val media) {
        return new Val(media.js() + ".seekable");
    }

    /** Gets played TimeRanges: elem.played */
    public static Val played(Val media) {
        return new Val(media.js() + ".played");
    }

    /** Gets TimeRanges length: ranges.length */
    public static Val rangesLength(Val ranges) {
        return new Val(ranges.js() + ".length");
    }

    /** Gets start time of range: ranges.start(index) */
    public static Val rangeStart(Val ranges, int index) {
        return new Val(ranges.js() + ".start(" + index + ")");
    }

    /** Gets end time of range: ranges.end(index) */
    public static Val rangeEnd(Val ranges, int index) {
        return new Val(ranges.js() + ".end(" + index + ")");
    }

    // ==================== Media Element Builder ====================

    /** Creates an audio element builder */
    public static MediaElementBuilder audio() {
        return new MediaElementBuilder("audio");
    }

    /** Creates a video element builder */
    public static MediaElementBuilder video() {
        return new MediaElementBuilder("video");
    }

    public static class MediaElementBuilder {
        private final String type;
        private String src;
        private boolean controls, autoplay, loop, muted;
        private Double volume;
        private Func onPlayFunc, onPauseFunc, onEndedFunc, onTimeupdateFunc, onLoadedMetadataFunc;
        private Func onCanPlayFunc, onErrorFunc, onSeekingFunc, onSeekedFunc, onVolumeChangeFunc, onRateChangeFunc;
        private String onPlayCode, onPauseCode, onEndedCode, onTimeupdateCode, onLoadedMetadataCode;
        private String onCanPlayCode, onErrorCode, onSeekingCode, onSeekedCode, onVolumeChangeCode, onRateChangeCode;

        MediaElementBuilder(String type) { this.type = type; }

        public MediaElementBuilder src(String src) { this.src = src; return this; }
        public MediaElementBuilder controls(boolean controls) { this.controls = controls; return this; }
        public MediaElementBuilder autoplay(boolean autoplay) { this.autoplay = autoplay; return this; }
        public MediaElementBuilder loop(boolean loop) { this.loop = loop; return this; }
        public MediaElementBuilder muted(boolean muted) { this.muted = muted; return this; }
        public MediaElementBuilder volume(double volume) { this.volume = volume; return this; }

        public MediaElementBuilder onPlay(Func handler) { this.onPlayFunc = handler; return this; }
        public MediaElementBuilder onPlay(String code) { this.onPlayCode = code; return this; }
        public MediaElementBuilder onPause(Func handler) { this.onPauseFunc = handler; return this; }
        public MediaElementBuilder onPause(String code) { this.onPauseCode = code; return this; }
        public MediaElementBuilder onEnded(Func handler) { this.onEndedFunc = handler; return this; }
        public MediaElementBuilder onEnded(String code) { this.onEndedCode = code; return this; }
        public MediaElementBuilder onTimeUpdate(Func handler) { this.onTimeupdateFunc = handler; return this; }
        public MediaElementBuilder onTimeUpdate(String code) { this.onTimeupdateCode = code; return this; }
        public MediaElementBuilder onLoadedMetadata(Func handler) { this.onLoadedMetadataFunc = handler; return this; }
        public MediaElementBuilder onLoadedMetadata(String code) { this.onLoadedMetadataCode = code; return this; }
        public MediaElementBuilder onCanPlay(Func handler) { this.onCanPlayFunc = handler; return this; }
        public MediaElementBuilder onCanPlay(String code) { this.onCanPlayCode = code; return this; }
        public MediaElementBuilder onError(Func handler) { this.onErrorFunc = handler; return this; }
        public MediaElementBuilder onError(String code) { this.onErrorCode = code; return this; }
        public MediaElementBuilder onSeeking(Func handler) { this.onSeekingFunc = handler; return this; }
        public MediaElementBuilder onSeeking(String code) { this.onSeekingCode = code; return this; }
        public MediaElementBuilder onSeeked(Func handler) { this.onSeekedFunc = handler; return this; }
        public MediaElementBuilder onSeeked(String code) { this.onSeekedCode = code; return this; }
        public MediaElementBuilder onVolumeChange(Func handler) { this.onVolumeChangeFunc = handler; return this; }
        public MediaElementBuilder onVolumeChange(String code) { this.onVolumeChangeCode = code; return this; }
        public MediaElementBuilder onRateChange(Func handler) { this.onRateChangeFunc = handler; return this; }
        public MediaElementBuilder onRateChange(String code) { this.onRateChangeCode = code; return this; }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder("var ").append(varName).append("=document.createElement('").append(type).append("');");
            if (src != null) sb.append(varName).append(".src='").append(JS.esc(src)).append("';");
            if (controls) sb.append(varName).append(".controls=true;");
            if (autoplay) sb.append(varName).append(".autoplay=true;");
            if (loop) sb.append(varName).append(".loop=true;");
            if (muted) sb.append(varName).append(".muted=true;");
            if (volume != null) sb.append(varName).append(".volume=").append(volume).append(";");

            addEventListeners(sb, varName);
            return new Val(sb.toString());
        }

        private void addEventListeners(StringBuilder sb, String varName) {
            if (onPlayFunc != null) sb.append(varName).append(".onplay=").append(onPlayFunc.toExpr()).append(";");
            else if (onPlayCode != null) sb.append(varName).append(".onplay=function(e){").append(onPlayCode).append("};");

            if (onPauseFunc != null) sb.append(varName).append(".onpause=").append(onPauseFunc.toExpr()).append(";");
            else if (onPauseCode != null) sb.append(varName).append(".onpause=function(e){").append(onPauseCode).append("};");

            if (onEndedFunc != null) sb.append(varName).append(".onended=").append(onEndedFunc.toExpr()).append(";");
            else if (onEndedCode != null) sb.append(varName).append(".onended=function(e){").append(onEndedCode).append("};");

            if (onTimeupdateFunc != null) sb.append(varName).append(".ontimeupdate=").append(onTimeupdateFunc.toExpr()).append(";");
            else if (onTimeupdateCode != null) sb.append(varName).append(".ontimeupdate=function(e){").append(onTimeupdateCode).append("};");

            if (onLoadedMetadataFunc != null) sb.append(varName).append(".onloadedmetadata=").append(onLoadedMetadataFunc.toExpr()).append(";");
            else if (onLoadedMetadataCode != null) sb.append(varName).append(".onloadedmetadata=function(e){").append(onLoadedMetadataCode).append("};");

            if (onCanPlayFunc != null) sb.append(varName).append(".oncanplay=").append(onCanPlayFunc.toExpr()).append(";");
            else if (onCanPlayCode != null) sb.append(varName).append(".oncanplay=function(e){").append(onCanPlayCode).append("};");

            if (onErrorFunc != null) sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            else if (onErrorCode != null) sb.append(varName).append(".onerror=function(e){").append(onErrorCode).append("};");

            if (onSeekingFunc != null) sb.append(varName).append(".onseeking=").append(onSeekingFunc.toExpr()).append(";");
            else if (onSeekingCode != null) sb.append(varName).append(".onseeking=function(e){").append(onSeekingCode).append("};");

            if (onSeekedFunc != null) sb.append(varName).append(".onseeked=").append(onSeekedFunc.toExpr()).append(";");
            else if (onSeekedCode != null) sb.append(varName).append(".onseeked=function(e){").append(onSeekedCode).append("};");

            if (onVolumeChangeFunc != null) sb.append(varName).append(".onvolumechange=").append(onVolumeChangeFunc.toExpr()).append(";");
            else if (onVolumeChangeCode != null) sb.append(varName).append(".onvolumechange=function(e){").append(onVolumeChangeCode).append("};");

            if (onRateChangeFunc != null) sb.append(varName).append(".onratechange=").append(onRateChangeFunc.toExpr()).append(";");
            else if (onRateChangeCode != null) sb.append(varName).append(".onratechange=function(e){").append(onRateChangeCode).append("};");
        }
    }

    // ==================== MediaRecorder ====================

    /** Creates a MediaRecorder builder */
    public static MediaRecorderBuilder mediaRecorder(Val stream) {
        return new MediaRecorderBuilder(stream);
    }

    /** Starts recording: recorder.start() */
    public static Val startRecording(Val recorder) {
        return new Val(recorder.js() + ".start()");
    }

    /** Starts recording with timeslice: recorder.start(timeslice) */
    public static Val startRecording(Val recorder, int timeslice) {
        return new Val(recorder.js() + ".start(" + timeslice + ")");
    }

    /** Stops recording: recorder.stop() */
    public static Val stopRecording(Val recorder) {
        return new Val(recorder.js() + ".stop()");
    }

    /** Pauses recording: recorder.pause() */
    public static Val pauseRecording(Val recorder) {
        return new Val(recorder.js() + ".pause()");
    }

    /** Resumes recording: recorder.resume() */
    public static Val resumeRecording(Val recorder) {
        return new Val(recorder.js() + ".resume()");
    }

    /** Requests data: recorder.requestData() */
    public static Val requestData(Val recorder) {
        return new Val(recorder.js() + ".requestData()");
    }

    /** Gets recorder state: recorder.state */
    public static Val recorderState(Val recorder) {
        return new Val(recorder.js() + ".state");
    }

    /** Checks if type is supported: MediaRecorder.isTypeSupported(type) */
    public static Val isTypeSupported(String mimeType) {
        return new Val("MediaRecorder.isTypeSupported('" + JS.esc(mimeType) + "')");
    }

    public static class MediaRecorderBuilder {
        private final Val stream;
        private String mimeType;
        private Integer audioBitsPerSecond, videoBitsPerSecond, bitsPerSecond;
        private Func onDataAvailableFunc, onStopFunc, onErrorFunc, onPauseFunc, onResumeFunc, onStartFunc;
        private String onDataAvailableCode, onStopCode, onErrorCode, onPauseCode, onResumeCode, onStartCode;

        MediaRecorderBuilder(Val stream) { this.stream = stream; }

        public MediaRecorderBuilder mimeType(String type) { this.mimeType = type; return this; }
        public MediaRecorderBuilder audioBitsPerSecond(int bps) { this.audioBitsPerSecond = bps; return this; }
        public MediaRecorderBuilder videoBitsPerSecond(int bps) { this.videoBitsPerSecond = bps; return this; }
        public MediaRecorderBuilder bitsPerSecond(int bps) { this.bitsPerSecond = bps; return this; }

        public MediaRecorderBuilder onDataAvailable(Func handler) { this.onDataAvailableFunc = handler; return this; }
        public MediaRecorderBuilder onDataAvailable(String code) { this.onDataAvailableCode = code; return this; }
        public MediaRecorderBuilder onStop(Func handler) { this.onStopFunc = handler; return this; }
        public MediaRecorderBuilder onStop(String code) { this.onStopCode = code; return this; }
        public MediaRecorderBuilder onError(Func handler) { this.onErrorFunc = handler; return this; }
        public MediaRecorderBuilder onError(String code) { this.onErrorCode = code; return this; }
        public MediaRecorderBuilder onPause(Func handler) { this.onPauseFunc = handler; return this; }
        public MediaRecorderBuilder onPause(String code) { this.onPauseCode = code; return this; }
        public MediaRecorderBuilder onResume(Func handler) { this.onResumeFunc = handler; return this; }
        public MediaRecorderBuilder onResume(String code) { this.onResumeCode = code; return this; }
        public MediaRecorderBuilder onStart(Func handler) { this.onStartFunc = handler; return this; }
        public MediaRecorderBuilder onStart(String code) { this.onStartCode = code; return this; }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder("var ").append(varName).append("=new MediaRecorder(").append(stream.js());

            if (hasOptions()) {
                sb.append(",{");
                boolean first = true;
                if (mimeType != null) {
                    sb.append("mimeType:'").append(JS.esc(mimeType)).append("'");
                    first = false;
                }
                if (audioBitsPerSecond != null) {
                    if (!first) sb.append(",");
                    sb.append("audioBitsPerSecond:").append(audioBitsPerSecond);
                    first = false;
                }
                if (videoBitsPerSecond != null) {
                    if (!first) sb.append(",");
                    sb.append("videoBitsPerSecond:").append(videoBitsPerSecond);
                    first = false;
                }
                if (bitsPerSecond != null) {
                    if (!first) sb.append(",");
                    sb.append("bitsPerSecond:").append(bitsPerSecond);
                }
                sb.append("}");
            }
            sb.append(");");

            addEventListeners(sb, varName);
            return new Val(sb.toString());
        }

        private boolean hasOptions() {
            return mimeType != null || audioBitsPerSecond != null || videoBitsPerSecond != null || bitsPerSecond != null;
        }

        private void addEventListeners(StringBuilder sb, String varName) {
            if (onDataAvailableFunc != null) sb.append(varName).append(".ondataavailable=").append(onDataAvailableFunc.toExpr()).append(";");
            else if (onDataAvailableCode != null) sb.append(varName).append(".ondataavailable=function(e){").append(onDataAvailableCode).append("};");

            if (onStopFunc != null) sb.append(varName).append(".onstop=").append(onStopFunc.toExpr()).append(";");
            else if (onStopCode != null) sb.append(varName).append(".onstop=function(e){").append(onStopCode).append("};");

            if (onErrorFunc != null) sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            else if (onErrorCode != null) sb.append(varName).append(".onerror=function(e){").append(onErrorCode).append("};");

            if (onPauseFunc != null) sb.append(varName).append(".onpause=").append(onPauseFunc.toExpr()).append(";");
            else if (onPauseCode != null) sb.append(varName).append(".onpause=function(e){").append(onPauseCode).append("};");

            if (onResumeFunc != null) sb.append(varName).append(".onresume=").append(onResumeFunc.toExpr()).append(";");
            else if (onResumeCode != null) sb.append(varName).append(".onresume=function(e){").append(onResumeCode).append("};");

            if (onStartFunc != null) sb.append(varName).append(".onstart=").append(onStartFunc.toExpr()).append(";");
            else if (onStartCode != null) sb.append(varName).append(".onstart=function(e){").append(onStartCode).append("};");
        }
    }

    // ==================== Picture-in-Picture ====================

    /** Requests picture-in-picture: video.requestPictureInPicture() */
    public static Val requestPiP(Val video) {
        return new Val(video.js() + ".requestPictureInPicture()");
    }

    /** Exits picture-in-picture: document.exitPictureInPicture() */
    public static Val exitPiP() {
        return new Val("document.exitPictureInPicture()");
    }

    /** Gets current PiP element: document.pictureInPictureElement */
    public static Val pictureInPictureElement() {
        return new Val("document.pictureInPictureElement");
    }

    /** Checks if PiP is enabled: document.pictureInPictureEnabled */
    public static Val pictureInPictureEnabled() {
        return new Val("document.pictureInPictureEnabled");
    }

    /** Adds enterpictureinpicture event listener */
    public static Val onEnterPiP(Val video, Func handler) {
        return new Val(video.js() + ".addEventListener('enterpictureinpicture'," + handler.toExpr() + ")");
    }

    /** Adds leavepictureinpicture event listener */
    public static Val onLeavePiP(Val video, Func handler) {
        return new Val(video.js() + ".addEventListener('leavepictureinpicture'," + handler.toExpr() + ")");
    }

    // ==================== Media Session API ====================

    /** Creates a Media Session builder */
    public static MediaSessionBuilder mediaSession() {
        return new MediaSessionBuilder();
    }

    /** Sets media session metadata */
    public static Val setMediaMetadata(String title, String artist, String album, String artworkUrl) {
        return new Val("navigator.mediaSession.metadata=new MediaMetadata({" +
                "title:'" + JS.esc(title) + "'," +
                "artist:'" + JS.esc(artist) + "'," +
                "album:'" + JS.esc(album) + "'," +
                "artwork:[{src:'" + JS.esc(artworkUrl) + "'}]})");
    }

    /** Sets playback state: "none", "paused", or "playing" */
    public static Val setPlaybackState(String state) {
        return new Val("navigator.mediaSession.playbackState='" + JS.esc(state) + "'");
    }

    /** Sets action handler: navigator.mediaSession.setActionHandler(action, handler) */
    public static Val setActionHandler(String action, Func handler) {
        return new Val("navigator.mediaSession.setActionHandler('" + JS.esc(action) + "'," + handler.toExpr() + ")");
    }

    public static class MediaSessionBuilder {
        private String title, artist, album;
        private final List<String> artworks = new ArrayList<>();
        private String playbackState;
        private Func onPlayFunc, onPauseFunc, onSeekBackwardFunc, onSeekForwardFunc, onPreviousTrackFunc, onNextTrackFunc, onStopFunc;

        MediaSessionBuilder() {}

        public MediaSessionBuilder metadata(String title, String artist, String album, String... artworkUrls) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            for (String url : artworkUrls) {
                artworks.add("{src:'" + JS.esc(url) + "'}");
            }
            return this;
        }

        public MediaSessionBuilder playbackState(String state) {
            this.playbackState = state;
            return this;
        }

        public MediaSessionBuilder onPlay(Func handler) { this.onPlayFunc = handler; return this; }
        public MediaSessionBuilder onPause(Func handler) { this.onPauseFunc = handler; return this; }
        public MediaSessionBuilder onSeekBackward(Func handler) { this.onSeekBackwardFunc = handler; return this; }
        public MediaSessionBuilder onSeekForward(Func handler) { this.onSeekForwardFunc = handler; return this; }
        public MediaSessionBuilder onPreviousTrack(Func handler) { this.onPreviousTrackFunc = handler; return this; }
        public MediaSessionBuilder onNextTrack(Func handler) { this.onNextTrackFunc = handler; return this; }
        public MediaSessionBuilder onStop(Func handler) { this.onStopFunc = handler; return this; }

        public String build() {
            StringBuilder sb = new StringBuilder();

            if (title != null) {
                sb.append("navigator.mediaSession.metadata=new MediaMetadata({");
                sb.append("title:'").append(JS.esc(title)).append("'");
                if (artist != null) sb.append(",artist:'").append(JS.esc(artist)).append("'");
                if (album != null) sb.append(",album:'").append(JS.esc(album)).append("'");
                if (!artworks.isEmpty()) {
                    sb.append(",artwork:[").append(String.join(",", artworks)).append("]");
                }
                sb.append("});");
            }

            if (playbackState != null) {
                sb.append("navigator.mediaSession.playbackState='").append(JS.esc(playbackState)).append("';");
            }

            if (onPlayFunc != null) sb.append("navigator.mediaSession.setActionHandler('play',").append(onPlayFunc.toExpr()).append(");");
            if (onPauseFunc != null) sb.append("navigator.mediaSession.setActionHandler('pause',").append(onPauseFunc.toExpr()).append(");");
            if (onSeekBackwardFunc != null) sb.append("navigator.mediaSession.setActionHandler('seekbackward',").append(onSeekBackwardFunc.toExpr()).append(");");
            if (onSeekForwardFunc != null) sb.append("navigator.mediaSession.setActionHandler('seekforward',").append(onSeekForwardFunc.toExpr()).append(");");
            if (onPreviousTrackFunc != null) sb.append("navigator.mediaSession.setActionHandler('previoustrack',").append(onPreviousTrackFunc.toExpr()).append(");");
            if (onNextTrackFunc != null) sb.append("navigator.mediaSession.setActionHandler('nexttrack',").append(onNextTrackFunc.toExpr()).append(");");
            if (onStopFunc != null) sb.append("navigator.mediaSession.setActionHandler('stop',").append(onStopFunc.toExpr()).append(");");

            return sb.toString();
        }
    }

    // ==================== Web Audio API (Basics) ====================

    /** Creates AudioContext: new AudioContext() */
    public static Val audioContext() {
        return new Val("new AudioContext()");
    }

    /** Creates AudioContext with options */
    public static Val audioContext(Val options) {
        return new Val("new AudioContext(" + options.js() + ")");
    }

    /** Gets AudioContext destination: ctx.destination */
    public static Val destination(Val ctx) {
        return new Val(ctx.js() + ".destination");
    }

    /** Gets AudioContext currentTime: ctx.currentTime */
    public static Val audioContextCurrentTime(Val ctx) {
        return new Val(ctx.js() + ".currentTime");
    }

    /** Gets AudioContext state: ctx.state */
    public static Val audioContextState(Val ctx) {
        return new Val(ctx.js() + ".state");
    }

    /** Resumes AudioContext: ctx.resume() */
    public static Val resumeAudioContext(Val ctx) {
        return new Val(ctx.js() + ".resume()");
    }

    /** Suspends AudioContext: ctx.suspend() */
    public static Val suspendAudioContext(Val ctx) {
        return new Val(ctx.js() + ".suspend()");
    }

    /** Closes AudioContext: ctx.close() */
    public static Val closeAudioContext(Val ctx) {
        return new Val(ctx.js() + ".close()");
    }

    /** Creates GainNode: ctx.createGain() */
    public static Val createGain(Val ctx) {
        return new Val(ctx.js() + ".createGain()");
    }

    /** Creates OscillatorNode: ctx.createOscillator() */
    public static Val createOscillator(Val ctx) {
        return new Val(ctx.js() + ".createOscillator()");
    }

    /** Creates AnalyserNode: ctx.createAnalyser() */
    public static Val createAnalyser(Val ctx) {
        return new Val(ctx.js() + ".createAnalyser()");
    }

    /** Creates BiquadFilterNode: ctx.createBiquadFilter() */
    public static Val createBiquadFilter(Val ctx) {
        return new Val(ctx.js() + ".createBiquadFilter()");
    }

    /** Creates DelayNode: ctx.createDelay() */
    public static Val createDelay(Val ctx) {
        return new Val(ctx.js() + ".createDelay()");
    }

    /** Creates DelayNode with max delay: ctx.createDelay(maxDelay) */
    public static Val createDelay(Val ctx, double maxDelayTime) {
        return new Val(ctx.js() + ".createDelay(" + maxDelayTime + ")");
    }

    /** Creates MediaElementSourceNode: ctx.createMediaElementSource(elem) */
    public static Val createMediaElementSource(Val ctx, Val mediaElement) {
        return new Val(ctx.js() + ".createMediaElementSource(" + mediaElement.js() + ")");
    }

    /** Creates MediaStreamSourceNode: ctx.createMediaStreamSource(stream) */
    public static Val createMediaStreamSource(Val ctx, Val stream) {
        return new Val(ctx.js() + ".createMediaStreamSource(" + stream.js() + ")");
    }

    /** Connects nodes: source.connect(destination) */
    public static Val connect(Val source, Val destination) {
        return new Val(source.js() + ".connect(" + destination.js() + ")");
    }

    /** Disconnects node: node.disconnect() */
    public static Val disconnect(Val node) {
        return new Val(node.js() + ".disconnect()");
    }

    /** Starts OscillatorNode: osc.start() */
    public static Val startOscillator(Val osc) {
        return new Val(osc.js() + ".start()");
    }

    /** Starts OscillatorNode at time: osc.start(when) */
    public static Val startOscillator(Val osc, double when) {
        return new Val(osc.js() + ".start(" + when + ")");
    }

    /** Stops OscillatorNode: osc.stop() */
    public static Val stopOscillator(Val osc) {
        return new Val(osc.js() + ".stop()");
    }

    /** Stops OscillatorNode at time: osc.stop(when) */
    public static Val stopOscillator(Val osc, double when) {
        return new Val(osc.js() + ".stop(" + when + ")");
    }

    /** Gets gain value: gainNode.gain.value */
    public static Val gainValue(Val gainNode) {
        return new Val(gainNode.js() + ".gain.value");
    }

    /** Sets gain value: gainNode.gain.value = val */
    public static Val setGainValue(Val gainNode, double value) {
        return new Val(gainNode.js() + ".gain.value=" + value);
    }

    /** Sets gain value from expression: gainNode.gain.value = expr */
    public static Val setGainValue(Val gainNode, Val value) {
        return new Val(gainNode.js() + ".gain.value=" + value.js());
    }

    /** Gets oscillator frequency: osc.frequency.value */
    public static Val frequency(Val osc) {
        return new Val(osc.js() + ".frequency.value");
    }

    /** Sets oscillator frequency: osc.frequency.value = freq */
    public static Val setFrequency(Val osc, double freq) {
        return new Val(osc.js() + ".frequency.value=" + freq);
    }

    /** Sets oscillator frequency from expression: osc.frequency.value = expr */
    public static Val setFrequency(Val osc, Val freq) {
        return new Val(osc.js() + ".frequency.value=" + freq.js());
    }

    /** Gets oscillator type: osc.type */
    public static Val oscillatorType(Val osc) {
        return new Val(osc.js() + ".type");
    }

    /** Sets oscillator type: osc.type = type (sine, square, sawtooth, triangle) */
    public static Val setOscillatorType(Val osc, String type) {
        return new Val(osc.js() + ".type='" + JS.esc(type) + "'");
    }
}
