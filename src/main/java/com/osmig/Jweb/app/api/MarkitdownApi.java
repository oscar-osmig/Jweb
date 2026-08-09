package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.api.GET;
import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.markdown.Markitdown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public Map<String, Object> convert(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("error", "No file uploaded");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (extension == null) {
            return Map.of("error", "File must have an extension (e.g. .pdf, .docx)");
        }
        try {
            String markdown = Markitdown.convert(file.getBytes(), extension);
            return Map.of(
                "filename", file.getOriginalFilename(),
                "markdown", markdown
            );
        } catch (Markitdown.MarkitdownException e) {
            return Map.of("error", e.getMessage());
        } catch (IOException e) {
            return Map.of("error", "Could not read uploaded file");
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1);
    }
}
