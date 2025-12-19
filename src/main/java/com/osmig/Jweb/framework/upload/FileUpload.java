package com.osmig.Jweb.framework.upload;

import com.osmig.Jweb.framework.server.Request;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * File upload handling utilities.
 *
 * <h2>Single File Upload</h2>
 * <pre>{@code
 * app.post("/upload", req -> {
 *     UploadedFile file = FileUpload.getFile(req, "document");
 *     if (file.isEmpty()) {
 *         return Response.badRequest("No file provided");
 *     }
 *     Path saved = file.saveTo(Path.of("uploads"));
 *     return Response.json(Map.of("path", saved.toString()));
 * });
 * }</pre>
 *
 * <h2>Multiple Files</h2>
 * <pre>{@code
 * app.post("/upload-many", req -> {
 *     List<UploadedFile> files = FileUpload.getFiles(req, "images");
 *     for (UploadedFile file : files) {
 *         if (file.isImage()) {
 *             file.saveTo(Path.of("uploads/images"));
 *         }
 *     }
 *     return Response.redirect("/gallery");
 * });
 * }</pre>
 *
 * <h2>HTML Form</h2>
 * <pre>{@code
 * form(attrs().action("/upload").method("post").enctype("multipart/form-data"),
 *     input(attrs().type("file").name("document")),
 *     button(attrs().type("submit"), text("Upload"))
 * )
 * }</pre>
 *
 * <h2>Configuration</h2>
 * In application.properties:
 * <pre>
 * spring.servlet.multipart.max-file-size=10MB
 * spring.servlet.multipart.max-request-size=50MB
 * </pre>
 */
public final class FileUpload {

    private FileUpload() {}

    /**
     * Gets a single uploaded file by name.
     *
     * @param request the HTTP request
     * @param name the form field name
     * @return the uploaded file (may be empty if not provided)
     */
    public static UploadedFile getFile(Request request, String name) {
        return getFileOptional(request, name)
            .orElse(new UploadedFile(new EmptyMultipartFile(name)));
    }

    /**
     * Gets a single uploaded file, returning empty if not present.
     *
     * @param request the HTTP request
     * @param name the form field name
     * @return optional containing the file if present
     */
    public static Optional<UploadedFile> getFileOptional(Request request, String name) {
        HttpServletRequest raw = request.raw();
        if (raw instanceof MultipartHttpServletRequest multipart) {
            MultipartFile file = multipart.getFile(name);
            if (file != null && !file.isEmpty()) {
                return Optional.of(new UploadedFile(file));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets multiple uploaded files by name.
     *
     * @param request the HTTP request
     * @param name the form field name
     * @return list of uploaded files (empty if none)
     */
    public static List<UploadedFile> getFiles(Request request, String name) {
        HttpServletRequest raw = request.raw();
        if (raw instanceof MultipartHttpServletRequest multipart) {
            List<MultipartFile> files = multipart.getFiles(name);
            List<UploadedFile> result = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    result.add(new UploadedFile(file));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Gets all uploaded files from the request.
     *
     * @param request the HTTP request
     * @return list of all uploaded files
     */
    public static List<UploadedFile> getAllFiles(Request request) {
        HttpServletRequest raw = request.raw();
        if (raw instanceof MultipartHttpServletRequest multipart) {
            List<UploadedFile> result = new ArrayList<>();
            multipart.getFileMap().values().forEach(file -> {
                if (!file.isEmpty()) {
                    result.add(new UploadedFile(file));
                }
            });
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Checks if the request is a multipart request.
     *
     * @param request the HTTP request
     * @return true if multipart
     */
    public static boolean isMultipart(Request request) {
        String contentType = request.contentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * Validates file upload constraints.
     *
     * @param file the uploaded file
     * @return a validation builder
     */
    public static FileValidation validate(UploadedFile file) {
        return new FileValidation(file);
    }

    /**
     * File validation builder.
     */
    public static class FileValidation {
        private final UploadedFile file;
        private final List<String> errors = new ArrayList<>();

        FileValidation(UploadedFile file) {
            this.file = file;
        }

        /**
         * Requires the file to not be empty.
         */
        public FileValidation required() {
            if (file.isEmpty()) {
                errors.add("File is required");
            }
            return this;
        }

        /**
         * Sets maximum file size.
         *
         * @param maxBytes maximum size in bytes
         */
        public FileValidation maxSize(long maxBytes) {
            if (file.getSize() > maxBytes) {
                errors.add("File exceeds maximum size of " + formatSize(maxBytes));
            }
            return this;
        }

        /**
         * Sets maximum file size.
         *
         * @param maxMB maximum size in megabytes
         */
        public FileValidation maxSizeMB(int maxMB) {
            return maxSize(maxMB * 1024L * 1024L);
        }

        /**
         * Restricts to specific extensions.
         *
         * @param extensions allowed extensions (without dot)
         */
        public FileValidation allowedExtensions(String... extensions) {
            if (!file.isEmpty() && !file.hasExtension(extensions)) {
                errors.add("File type not allowed. Allowed: " + String.join(", ", extensions));
            }
            return this;
        }

        /**
         * Restricts to image files only.
         */
        public FileValidation imagesOnly() {
            if (!file.isEmpty() && !file.isImage()) {
                errors.add("Only image files are allowed");
            }
            return this;
        }

        /**
         * Checks if validation passed.
         */
        public boolean isValid() {
            return errors.isEmpty();
        }

        /**
         * Gets validation errors.
         */
        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        /**
         * Gets the first error message.
         */
        public String getFirstError() {
            return errors.isEmpty() ? null : errors.getFirst();
        }

        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
            return (bytes / (1024 * 1024)) + " MB";
        }
    }

    /**
     * Empty MultipartFile implementation for null safety.
     */
    private static class EmptyMultipartFile implements MultipartFile {
        private final String name;

        EmptyMultipartFile(String name) {
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return ""; }
        @Override public String getContentType() { return null; }
        @Override public boolean isEmpty() { return true; }
        @Override public long getSize() { return 0; }
        @Override public byte[] getBytes() { return new byte[0]; }
        @Override public java.io.InputStream getInputStream() {
            return java.io.InputStream.nullInputStream();
        }
        @Override public void transferTo(java.io.File dest) {}
    }
}
