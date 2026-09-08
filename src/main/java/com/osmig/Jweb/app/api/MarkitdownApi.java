package com.osmig.Jweb.app.api;

import jweb.Markitdown;
import jweb.UploadedFile;
import jweb.api.GET;
import jweb.api.POST;
import jweb.api.REST;
import jweb.api.Upload;
import jweb.api.Value;

import java.io.IOException;
import java.util.Map;

/** Document-to-Markdown conversion API, powered by Microsoft's markitdown. */
@REST("/api/v1/markitdown")
public class MarkitdownApi {

    public MarkitdownApi(@Value("${jweb.markitdown.command:}") String command,
                         @Value("${jweb.markitdown.timeout-seconds:120}") long timeoutSeconds) {
        Markitdown.setCommand(command);
        Markitdown.setTimeoutSeconds(timeoutSeconds);
    }

    /** Reports whether the markitdown CLI is installed and reachable. */
    @GET("/status")
    public Map<String, Object> status() {
        return Map.of("available", Markitdown.isAvailable());
    }

    /** Converts an uploaded document (PDF, Word, Excel, HTML, ...) to Markdown. */
    @POST("/convert")
    public Map<String, Object> convert(@Upload("file") UploadedFile file) {
        if (file.isEmpty()) {
            return Map.of("error", "No file uploaded");
        }
        String extension = file.getExtension();
        if (extension.isEmpty()) {
            return Map.of("error", "File must have an extension (e.g. .pdf, .docx)");
        }
        try {
            String markdown = Markitdown.convert(file.getBytes(), extension);
            return Map.of(
                "filename", file.getFilename(),
                "markdown", markdown
            );
        } catch (Markitdown.MarkitdownException e) {
            return Map.of("error", e.getMessage());
        } catch (IOException e) {
            return Map.of("error", "Could not read uploaded file");
        }
    }
}
