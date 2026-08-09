package com.osmig.Jweb.framework.markdown;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Converts documents (PDF, Word, PowerPoint, Excel, HTML, images, and more)
 * to Markdown using Microsoft's markitdown CLI.
 *
 * <p>Requires the markitdown tool installed in the project-local venv
 * ({@code .tools/markitdown/bin/markitdown} by default). Override the binary
 * location with {@code jweb.markitdown.command} or {@link #setCommand}.
 *
 * <p>Usage:
 * <pre>
 * String md = Markitdown.convert(Path.of("report.pdf"));
 * String md = Markitdown.convert(uploadedBytes, "docx");
 * </pre>
 */
public final class Markitdown {

    private static volatile String command = ".tools/markitdown/bin/markitdown";
    private static volatile long timeoutSeconds = 120;

    private Markitdown() {}

    /** Overrides the markitdown executable path. Blank values are ignored. */
    public static void setCommand(String cmd) {
        if (cmd != null && !cmd.isBlank()) command = cmd.trim();
    }

    /** Sets the maximum time a single conversion may take. */
    public static void setTimeoutSeconds(long seconds) {
        if (seconds > 0) timeoutSeconds = seconds;
    }

    /** True when the configured markitdown executable exists and is runnable. */
    public static boolean isAvailable() {
        Path bin = Path.of(command);
        return Files.isExecutable(bin);
    }

    /** Converts a file on disk to Markdown. */
    public static String convert(Path file) {
        if (!Files.isReadable(file)) {
            throw new MarkitdownException("File not found or unreadable: " + file);
        }
        return run(file);
    }

    /**
     * Converts in-memory content to Markdown. The extension (e.g. "pdf",
     * "docx") tells markitdown how to parse the bytes.
     */
    public static String convert(byte[] content, String extension) {
        String ext = sanitizeExtension(extension);
        Path temp = null;
        try {
            temp = Files.createTempFile("markitdown-", "." + ext);
            Files.write(temp, content);
            return run(temp);
        } catch (IOException e) {
            throw new MarkitdownException("Failed to write temp file for conversion", e);
        } finally {
            deleteQuietly(temp);
        }
    }

    private static String run(Path file) {
        try {
            Process process = new ProcessBuilder(command, file.toAbsolutePath().toString())
                    .redirectErrorStream(false)
                    .start();

            String markdown = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String errors = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new MarkitdownException("Conversion timed out after " + timeoutSeconds + "s: " + file.getFileName());
            }
            if (process.exitValue() != 0) {
                throw new MarkitdownException("markitdown failed (exit " + process.exitValue() + "): " + lastLine(errors));
            }
            return markdown;
        } catch (IOException e) {
            throw new MarkitdownException("Could not run markitdown (" + command + ") — is it installed?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MarkitdownException("Conversion interrupted", e);
        }
    }

    private static String sanitizeExtension(String extension) {
        String ext = extension == null ? "" : extension.trim().toLowerCase();
        if (ext.startsWith(".")) ext = ext.substring(1);
        if (ext.isEmpty() || !ext.matches("[a-z0-9]{1,10}")) {
            throw new MarkitdownException("Invalid file extension: " + extension);
        }
        return ext;
    }

    private static String lastLine(String text) {
        String trimmed = text.strip();
        int newline = trimmed.lastIndexOf('\n');
        return newline < 0 ? trimmed : trimmed.substring(newline + 1);
    }

    private static void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    /** Thrown when a conversion fails, times out, or markitdown is missing. */
    public static class MarkitdownException extends RuntimeException {
        public MarkitdownException(String message) { super(message); }
        public MarkitdownException(String message, Throwable cause) { super(message, cause); }
    }
}
