package com.osmig.Jweb.framework.ai;

/** Thrown when the AI module is misconfigured or a provider call fails. */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
