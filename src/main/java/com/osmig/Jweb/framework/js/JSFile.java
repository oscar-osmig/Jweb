package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * File API for FileReader, Blob, and file operations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSFile.*;
 *
 * // Read file as text
 * readAsText(variable("file"))
 *     .onLoad(callback("e").call("handleText", variable("e").dot("target").dot("result")))
 *
 * // Read file as data URL (for images)
 * readAsDataURL(variable("file"))
 *     .onLoad(callback("e").set("img.src", variable("e").dot("target").dot("result")))
 *
 * // Create Blob and download
 * downloadBlob(textBlob(variable("content")), "file.txt")
 * </pre>
 */
public final class JSFile {
    private JSFile() {}

    // ==================== FileReader ====================

    /** Reads file as text */
    public static FileReaderBuilder readAsText(Val file) {
        return new FileReaderBuilder(file, "readAsText");
    }

    /** Reads file as Data URL (base64) */
    public static FileReaderBuilder readAsDataURL(Val file) {
        return new FileReaderBuilder(file, "readAsDataURL");
    }

    /** Reads file as ArrayBuffer */
    public static FileReaderBuilder readAsArrayBuffer(Val file) {
        return new FileReaderBuilder(file, "readAsArrayBuffer");
    }

    /** Reads file as binary string */
    public static FileReaderBuilder readAsBinaryString(Val file) {
        return new FileReaderBuilder(file, "readAsBinaryString");
    }

    // ==================== Blob Creation ====================

    /** Creates a text Blob */
    public static Val textBlob(Val content) {
        return new Val("new Blob([" + content.js() + "],{type:'text/plain'})");
    }

    /** Creates a JSON Blob */
    public static Val jsonBlob(Val data) {
        return new Val("new Blob([JSON.stringify(" + data.js() + ")],{type:'application/json'})");
    }

    /** Creates a Blob with custom MIME type */
    public static Val blob(Val content, String mimeType) {
        return new Val("new Blob([" + content.js() + "],{type:'" + JS.esc(mimeType) + "'})");
    }

    /** Creates a Blob from array of parts */
    public static Val blob(Val[] parts, String mimeType) {
        StringBuilder sb = new StringBuilder("new Blob([");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(parts[i].js());
        }
        return new Val(sb.append("],{type:'").append(JS.esc(mimeType)).append("'})").toString());
    }

    // ==================== Blob Properties ====================

    /** Gets Blob size */
    public static Val blobSize(Val blob) { return new Val(blob.js() + ".size"); }

    /** Gets Blob type */
    public static Val blobType(Val blob) { return new Val(blob.js() + ".type"); }

    /** Slices a Blob */
    public static Val sliceBlob(Val blob, Val start, Val end) {
        return new Val(blob.js() + ".slice(" + start.js() + "," + end.js() + ")");
    }

    // ==================== Object URLs ====================

    /** Creates an object URL from Blob */
    public static Val createObjectURL(Val blob) {
        return new Val("URL.createObjectURL(" + blob.js() + ")");
    }

    /** Revokes an object URL */
    public static Val revokeObjectURL(Val url) {
        return new Val("URL.revokeObjectURL(" + url.js() + ")");
    }

    // ==================== File Properties ====================

    /** Gets file name */
    public static Val fileName(Val file) { return new Val(file.js() + ".name"); }

    /** Gets file size */
    public static Val fileSize(Val file) { return new Val(file.js() + ".size"); }

    /** Gets file type (MIME) */
    public static Val fileType(Val file) { return new Val(file.js() + ".type"); }

    /** Gets file last modified timestamp */
    public static Val lastModified(Val file) { return new Val(file.js() + ".lastModified"); }

    // ==================== Download ====================

    /** Downloads a Blob as a file */
    public static Val downloadBlob(Val blob, String filename) {
        return new Val("(function(){var u=URL.createObjectURL(" + blob.js() +
            ");var a=document.createElement('a');a.href=u;a.download='" + JS.esc(filename) +
            "';a.click();URL.revokeObjectURL(u)}())");
    }

    /** Downloads a Blob with dynamic filename */
    public static Val downloadBlob(Val blob, Val filename) {
        return new Val("(function(){var u=URL.createObjectURL(" + blob.js() +
            ");var a=document.createElement('a');a.href=u;a.download=" + filename.js() +
            ";a.click();URL.revokeObjectURL(u)}())");
    }

    /** Downloads text as a file */
    public static Val downloadText(Val content, String filename) {
        return downloadBlob(textBlob(content), filename);
    }

    /** Downloads JSON as a file */
    public static Val downloadJson(Val data, String filename) {
        return downloadBlob(jsonBlob(data), filename);
    }

    // ==================== Input Helpers ====================

    /** Gets files from file input */
    public static Val getFiles(String inputId) {
        return new Val("document.getElementById('" + JS.esc(inputId) + "').files");
    }

    /** Gets first file from file input */
    public static Val getFile(String inputId) {
        return new Val("document.getElementById('" + JS.esc(inputId) + "').files[0]");
    }

    /** Gets files from input element */
    public static Val getFiles(Val input) { return new Val(input.js() + ".files"); }

    /** Gets first file from input element */
    public static Val getFile(Val input) { return new Val(input.js() + ".files[0]"); }

    // ==================== Validation ====================

    /** Checks if file size is within limit (bytes) */
    public static Val isWithinSize(Val file, long maxBytes) {
        return new Val("(" + file.js() + ".size<=" + maxBytes + ")");
    }

    /** Checks if file type matches */
    public static Val isType(Val file, String mimeType) {
        return new Val("(" + file.js() + ".type==='" + JS.esc(mimeType) + "')");
    }

    /** Checks if file type starts with (e.g., "image/") */
    public static Val isTypePrefix(Val file, String prefix) {
        return new Val("(" + file.js() + ".type.startsWith('" + JS.esc(prefix) + "'))");
    }

    /** Checks if file is an image */
    public static Val isImage(Val file) { return isTypePrefix(file, "image/"); }

    // ==================== FileReader Builder ====================

    public static class FileReaderBuilder {
        private final Val file;
        private final String method;
        private Func onLoadFunc, onErrorFunc, onProgressFunc;
        private String onLoadCode, onErrorCode, onProgressCode;

        FileReaderBuilder(Val file, String method) {
            this.file = file;
            this.method = method;
        }

        public FileReaderBuilder onLoad(Func callback) { this.onLoadFunc = callback; return this; }
        public FileReaderBuilder onLoad(String code) { this.onLoadCode = code; return this; }

        public FileReaderBuilder onError(Func callback) { this.onErrorFunc = callback; return this; }
        public FileReaderBuilder onError(String code) { this.onErrorCode = code; return this; }

        public FileReaderBuilder onProgress(Func callback) { this.onProgressFunc = callback; return this; }
        public FileReaderBuilder onProgress(String code) { this.onProgressCode = code; return this; }

        public Val toVal() {
            StringBuilder sb = new StringBuilder("(function(){var r=new FileReader();");

            if (onLoadFunc != null) sb.append("r.onload=").append(onLoadFunc.toExpr()).append(";");
            else if (onLoadCode != null) sb.append("r.onload=function(e){").append(onLoadCode).append("};");

            if (onErrorFunc != null) sb.append("r.onerror=").append(onErrorFunc.toExpr()).append(";");
            else if (onErrorCode != null) sb.append("r.onerror=function(e){").append(onErrorCode).append("};");

            if (onProgressFunc != null) sb.append("r.onprogress=").append(onProgressFunc.toExpr()).append(";");
            else if (onProgressCode != null) sb.append("r.onprogress=function(e){").append(onProgressCode).append("};");

            sb.append("r.").append(method).append("(").append(file.js()).append(");return r}())");
            return new Val(sb.toString());
        }

        public String build() { return toVal().js(); }
    }
}
