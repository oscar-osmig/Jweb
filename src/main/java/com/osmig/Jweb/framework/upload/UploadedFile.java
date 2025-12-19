package com.osmig.Jweb.framework.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Wrapper for uploaded files with convenient methods.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * app.post("/upload", req -> {
 *     UploadedFile file = FileUpload.getFile(req, "avatar");
 *
 *     if (file.isEmpty()) {
 *         return div(text("No file uploaded"));
 *     }
 *
 *     // Validate
 *     if (!file.isImage()) {
 *         return div(text("Only images allowed"));
 *     }
 *     if (file.getSize() > 5 * 1024 * 1024) {
 *         return div(text("File too large (max 5MB)"));
 *     }
 *
 *     // Save
 *     Path saved = file.saveTo(Path.of("uploads"));
 *     return div(text("Saved as: " + saved.getFileName()));
 * });
 * }</pre>
 */
public class UploadedFile {

    private final MultipartFile multipartFile;

    public UploadedFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    /**
     * Gets the original filename.
     *
     * @return the filename, or empty string if none
     */
    public String getFilename() {
        String name = multipartFile.getOriginalFilename();
        return name != null ? name : "";
    }

    /**
     * Gets the file extension (lowercase).
     *
     * @return the extension without dot, or empty string
     */
    public String getExtension() {
        String filename = getFilename();
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    /**
     * Gets the content type (MIME type).
     *
     * @return the content type, or "application/octet-stream" if unknown
     */
    public String getContentType() {
        String type = multipartFile.getContentType();
        return type != null ? type : "application/octet-stream";
    }

    /**
     * Gets the file size in bytes.
     *
     * @return the size in bytes
     */
    public long getSize() {
        return multipartFile.getSize();
    }

    /**
     * Checks if the file is empty (no content).
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return multipartFile.isEmpty();
    }

    /**
     * Gets the file content as bytes.
     *
     * @return the file bytes
     * @throws IOException if reading fails
     */
    public byte[] getBytes() throws IOException {
        return multipartFile.getBytes();
    }

    /**
     * Gets an input stream to read the file.
     *
     * @return the input stream
     * @throws IOException if opening fails
     */
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }

    /**
     * Checks if this is an image file.
     *
     * @return true if the content type is an image
     */
    public boolean isImage() {
        return getContentType().startsWith("image/");
    }

    /**
     * Checks if this is a video file.
     *
     * @return true if the content type is video
     */
    public boolean isVideo() {
        return getContentType().startsWith("video/");
    }

    /**
     * Checks if this is a PDF file.
     *
     * @return true if PDF
     */
    public boolean isPdf() {
        return "application/pdf".equals(getContentType());
    }

    /**
     * Checks if the file has an allowed extension.
     *
     * @param allowed varargs of allowed extensions (without dot)
     * @return true if extension is in the allowed list
     */
    public boolean hasExtension(String... allowed) {
        String ext = getExtension();
        for (String a : allowed) {
            if (a.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the file to a directory with a generated unique name.
     *
     * @param directory the target directory
     * @return the path to the saved file
     * @throws IOException if saving fails
     */
    public Path saveTo(Path directory) throws IOException {
        return saveTo(directory, generateFilename());
    }

    /**
     * Saves the file to a directory with a specific name.
     *
     * @param directory the target directory
     * @param filename the filename to use
     * @return the path to the saved file
     * @throws IOException if saving fails
     */
    public Path saveTo(Path directory, String filename) throws IOException {
        Files.createDirectories(directory);
        Path target = directory.resolve(filename);
        try (InputStream in = getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    /**
     * Saves the file preserving the original filename.
     *
     * @param directory the target directory
     * @return the path to the saved file
     * @throws IOException if saving fails
     */
    public Path saveWithOriginalName(Path directory) throws IOException {
        String filename = sanitizeFilename(getFilename());
        if (filename.isEmpty()) {
            filename = generateFilename();
        }
        return saveTo(directory, filename);
    }

    /**
     * Generates a unique filename preserving the extension.
     *
     * @return a UUID-based filename
     */
    public String generateFilename() {
        String ext = getExtension();
        String uuid = UUID.randomUUID().toString();
        return ext.isEmpty() ? uuid : uuid + "." + ext;
    }

    /**
     * Sanitizes a filename for safe storage.
     *
     * @param filename the original filename
     * @return sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return "";
        // Remove path separators and dangerous characters
        return filename
            .replaceAll("[/\\\\:*?\"<>|]", "_")
            .replaceAll("\\.\\.", "_")
            .trim();
    }

    /**
     * Gets the underlying MultipartFile.
     *
     * @return the Spring MultipartFile
     */
    public MultipartFile raw() {
        return multipartFile;
    }
}
