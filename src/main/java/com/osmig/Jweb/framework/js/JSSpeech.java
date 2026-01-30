package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Web Speech API DSL for speech synthesis (text-to-speech) and
 * speech recognition (speech-to-text).
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSSpeech.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Text-to-speech
 * speak("Hello, welcome to JWeb!")
 *
 * // With voice configuration
 * speakBuilder("Hello world")
 *     .lang("en-US")
 *     .rate(1.2)
 *     .pitch(1.0)
 *     .volume(0.8)
 *     .onEnd(callback().log("Done speaking"))
 *     .build()
 *
 * // Get available voices
 * getVoices()
 *
 * // Speech recognition
 * recognizer()
 *     .lang("en-US")
 *     .continuous(true)
 *     .interimResults(true)
 *     .onResult(callback("e")
 *         .let_("text", transcript(variable("e")))
 *         .log(variable("text")))
 *     .onEnd(callback().log("Recognition ended"))
 *     .build("rec")
 *
 * // Control speech
 * pauseSpeech()
 * resumeSpeech()
 * cancelSpeech()
 * </pre>
 */
public final class JSSpeech {
    private JSSpeech() {}

    // ==================== Speech Synthesis (TTS) ====================

    /**
     * Speaks text using default voice settings.
     *
     * @param text the text to speak
     * @return a Val representing the speak call
     */
    public static Val speak(String text) {
        return new Val("speechSynthesis.speak(new SpeechSynthesisUtterance('" + JS.esc(text) + "'))");
    }

    /**
     * Speaks text from an expression.
     *
     * @param text the text expression
     * @return a Val representing the speak call
     */
    public static Val speak(Val text) {
        return new Val("speechSynthesis.speak(new SpeechSynthesisUtterance(" + text.js() + "))");
    }

    /**
     * Creates a speech builder for configuring voice, rate, pitch, etc.
     *
     * @param text the text to speak
     * @return a SpeakBuilder
     */
    public static SpeakBuilder speakBuilder(String text) {
        return new SpeakBuilder("'" + JS.esc(text) + "'");
    }

    /**
     * Creates a speech builder with dynamic text.
     *
     * @param text the text expression
     * @return a SpeakBuilder
     */
    public static SpeakBuilder speakBuilder(Val text) {
        return new SpeakBuilder(text.js());
    }

    /** Pauses speech synthesis. */
    public static Val pauseSpeech() {
        return new Val("speechSynthesis.pause()");
    }

    /** Resumes speech synthesis. */
    public static Val resumeSpeech() {
        return new Val("speechSynthesis.resume()");
    }

    /** Cancels all speech in the queue. */
    public static Val cancelSpeech() {
        return new Val("speechSynthesis.cancel()");
    }

    /** Checks if currently speaking: speechSynthesis.speaking */
    public static Val isSpeaking() {
        return new Val("speechSynthesis.speaking");
    }

    /** Checks if speech is paused: speechSynthesis.paused */
    public static Val isSpeechPaused() {
        return new Val("speechSynthesis.paused");
    }

    /** Checks if there are pending utterances: speechSynthesis.pending */
    public static Val isSpeechPending() {
        return new Val("speechSynthesis.pending");
    }

    /**
     * Gets available voices.
     * Note: voices may load asynchronously; use onVoicesChanged for reliable access.
     *
     * @return a Val representing the voices array
     */
    public static Val getVoices() {
        return new Val("speechSynthesis.getVoices()");
    }

    /**
     * Listens for voiceschanged event (fires when voice list is loaded).
     *
     * @param handler callback invoked when voices are available
     * @return a Val representing the event listener
     */
    public static Val onVoicesChanged(Func handler) {
        return new Val("speechSynthesis.addEventListener('voiceschanged'," + handler.toExpr() + ")");
    }

    /**
     * Builder for configuring speech synthesis utterances.
     */
    public static class SpeakBuilder {
        private final String textExpr;
        private String lang;
        private String voice;
        private Double rate;
        private Double pitch;
        private Double volume;
        private String onStart;
        private String onEnd;
        private String onPause;
        private String onResume;
        private String onError;
        private String onBoundary;

        SpeakBuilder(String textExpr) {
            this.textExpr = textExpr;
        }

        /** Sets the language (BCP 47 tag, e.g., "en-US", "fr-FR"). */
        public SpeakBuilder lang(String lang) {
            this.lang = lang;
            return this;
        }

        /** Sets the voice by name. */
        public SpeakBuilder voice(String voiceName) {
            this.voice = voiceName;
            return this;
        }

        /** Sets the voice from expression. */
        public SpeakBuilder voice(Val voice) {
            this.voice = voice.js();
            return this;
        }

        /** Sets the speaking rate (0.1 to 10.0, default 1.0). */
        public SpeakBuilder rate(double rate) {
            this.rate = rate;
            return this;
        }

        /** Sets the pitch (0.0 to 2.0, default 1.0). */
        public SpeakBuilder pitch(double pitch) {
            this.pitch = pitch;
            return this;
        }

        /** Sets the volume (0.0 to 1.0, default 1.0). */
        public SpeakBuilder volume(double volume) {
            this.volume = volume;
            return this;
        }

        /** Callback when speech starts. */
        public SpeakBuilder onStart(Func handler) {
            this.onStart = handler.toExpr();
            return this;
        }

        /** Callback when speech ends. */
        public SpeakBuilder onEnd(Func handler) {
            this.onEnd = handler.toExpr();
            return this;
        }

        /** Callback when speech is paused. */
        public SpeakBuilder onPause(Func handler) {
            this.onPause = handler.toExpr();
            return this;
        }

        /** Callback when speech resumes. */
        public SpeakBuilder onResume(Func handler) {
            this.onResume = handler.toExpr();
            return this;
        }

        /** Callback on error. */
        public SpeakBuilder onError(Func handler) {
            this.onError = handler.toExpr();
            return this;
        }

        /** Callback on word/sentence boundary. */
        public SpeakBuilder onBoundary(Func handler) {
            this.onBoundary = handler.toExpr();
            return this;
        }

        /**
         * Builds and speaks the utterance.
         *
         * @return a Val representing the complete speak operation
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var u=new SpeechSynthesisUtterance(")
                .append(textExpr).append(");");

            if (lang != null) sb.append("u.lang='").append(JS.esc(lang)).append("';");
            if (rate != null) sb.append("u.rate=").append(rate).append(";");
            if (pitch != null) sb.append("u.pitch=").append(pitch).append(";");
            if (volume != null) sb.append("u.volume=").append(volume).append(";");

            if (voice != null) {
                // If voice is a string literal, find voice by name
                if (voice.startsWith("'") || voice.startsWith("\"")) {
                    sb.append("u.voice=speechSynthesis.getVoices().find(function(v){return v.name===")
                      .append(voice).append(";});");
                } else {
                    sb.append("u.voice=").append(voice).append(";");
                }
            }

            if (onStart != null) sb.append("u.onstart=").append(onStart).append(";");
            if (onEnd != null) sb.append("u.onend=").append(onEnd).append(";");
            if (onPause != null) sb.append("u.onpause=").append(onPause).append(";");
            if (onResume != null) sb.append("u.onresume=").append(onResume).append(";");
            if (onError != null) sb.append("u.onerror=").append(onError).append(";");
            if (onBoundary != null) sb.append("u.onboundary=").append(onBoundary).append(";");

            sb.append("speechSynthesis.speak(u);return u;}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Speech Recognition (STT) ====================

    /**
     * Creates a speech recognition builder.
     *
     * @return a RecognizerBuilder
     */
    public static RecognizerBuilder recognizer() {
        return new RecognizerBuilder();
    }

    /**
     * Builder for configuring speech recognition.
     */
    public static class RecognizerBuilder {
        private String lang;
        private Boolean continuous;
        private Boolean interimResults;
        private Integer maxAlternatives;
        private String onResult;
        private String onEnd;
        private String onStart;
        private String onError;
        private String onSpeechStart;
        private String onSpeechEnd;
        private String onNoMatch;

        RecognizerBuilder() {}

        /** Sets the recognition language (BCP 47 tag). */
        public RecognizerBuilder lang(String lang) {
            this.lang = lang;
            return this;
        }

        /** Enables continuous recognition (keeps listening after each result). */
        public RecognizerBuilder continuous(boolean continuous) {
            this.continuous = continuous;
            return this;
        }

        /** Enables interim results (partial, non-final results). */
        public RecognizerBuilder interimResults(boolean interimResults) {
            this.interimResults = interimResults;
            return this;
        }

        /** Sets maximum alternatives per result. */
        public RecognizerBuilder maxAlternatives(int max) {
            this.maxAlternatives = max;
            return this;
        }

        /** Callback with recognition results. Event has .results property. */
        public RecognizerBuilder onResult(Func handler) {
            this.onResult = handler.toExpr();
            return this;
        }

        /** Callback when recognition ends. */
        public RecognizerBuilder onEnd(Func handler) {
            this.onEnd = handler.toExpr();
            return this;
        }

        /** Callback when recognition starts. */
        public RecognizerBuilder onStart(Func handler) {
            this.onStart = handler.toExpr();
            return this;
        }

        /** Callback on recognition error. */
        public RecognizerBuilder onError(Func handler) {
            this.onError = handler.toExpr();
            return this;
        }

        /** Callback when speech is detected. */
        public RecognizerBuilder onSpeechStart(Func handler) {
            this.onSpeechStart = handler.toExpr();
            return this;
        }

        /** Callback when speech stops being detected. */
        public RecognizerBuilder onSpeechEnd(Func handler) {
            this.onSpeechEnd = handler.toExpr();
            return this;
        }

        /** Callback when no speech is recognized. */
        public RecognizerBuilder onNoMatch(Func handler) {
            this.onNoMatch = handler.toExpr();
            return this;
        }

        /**
         * Builds the recognizer and assigns it to a variable.
         *
         * @param varName the variable name for the recognizer instance
         * @return a Val representing the complete recognition setup
         */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder("var ").append(varName)
                .append("=new(window.SpeechRecognition||window.webkitSpeechRecognition)();");

            if (lang != null) sb.append(varName).append(".lang='").append(JS.esc(lang)).append("';");
            if (continuous != null) sb.append(varName).append(".continuous=").append(continuous).append(";");
            if (interimResults != null) sb.append(varName).append(".interimResults=").append(interimResults).append(";");
            if (maxAlternatives != null) sb.append(varName).append(".maxAlternatives=").append(maxAlternatives).append(";");

            if (onResult != null) sb.append(varName).append(".onresult=").append(onResult).append(";");
            if (onEnd != null) sb.append(varName).append(".onend=").append(onEnd).append(";");
            if (onStart != null) sb.append(varName).append(".onstart=").append(onStart).append(";");
            if (onError != null) sb.append(varName).append(".onerror=").append(onError).append(";");
            if (onSpeechStart != null) sb.append(varName).append(".onspeechstart=").append(onSpeechStart).append(";");
            if (onSpeechEnd != null) sb.append(varName).append(".onspeechend=").append(onSpeechEnd).append(";");
            if (onNoMatch != null) sb.append(varName).append(".onnomatch=").append(onNoMatch).append(";");

            return new Val(sb.toString());
        }
    }

    // ==================== Recognition Control ====================

    /**
     * Starts speech recognition.
     *
     * @param recognizer the recognizer variable
     * @return a Val representing the start call
     */
    public static Val startRecognition(Val recognizer) {
        return new Val(recognizer.js() + ".start()");
    }

    /**
     * Stops speech recognition (returns final result).
     *
     * @param recognizer the recognizer variable
     * @return a Val representing the stop call
     */
    public static Val stopRecognition(Val recognizer) {
        return new Val(recognizer.js() + ".stop()");
    }

    /**
     * Aborts speech recognition (no final result).
     *
     * @param recognizer the recognizer variable
     * @return a Val representing the abort call
     */
    public static Val abortRecognition(Val recognizer) {
        return new Val(recognizer.js() + ".abort()");
    }

    // ==================== Result Helpers ====================

    /**
     * Gets the transcript from the last result of a speech recognition event.
     * Extracts: event.results[event.results.length-1][0].transcript
     *
     * @param event the recognition event
     * @return a Val representing the transcript string
     */
    public static Val transcript(Val event) {
        return new Val(event.js() + ".results[" + event.js() + ".results.length-1][0].transcript");
    }

    /**
     * Gets the confidence score from the last result (0.0 to 1.0).
     *
     * @param event the recognition event
     * @return a Val representing the confidence value
     */
    public static Val confidence(Val event) {
        return new Val(event.js() + ".results[" + event.js() + ".results.length-1][0].confidence");
    }

    /**
     * Checks if the last result is final (not interim).
     *
     * @param event the recognition event
     * @return a Val representing the isFinal boolean
     */
    public static Val isFinal(Val event) {
        return new Val(event.js() + ".results[" + event.js() + ".results.length-1].isFinal");
    }

    /**
     * Gets all results from the recognition event as an array.
     *
     * @param event the recognition event
     * @return a Val representing the SpeechRecognitionResultList
     */
    public static Val allResults(Val event) {
        return new Val(event.js() + ".results");
    }

    /**
     * Gets the full transcript by joining all final results.
     *
     * @param event the recognition event
     * @return a Val representing the full transcript string
     */
    public static Val fullTranscript(Val event) {
        return new Val("Array.from(" + event.js() + ".results).map(function(r){return r[0].transcript;}).join('')");
    }
}
