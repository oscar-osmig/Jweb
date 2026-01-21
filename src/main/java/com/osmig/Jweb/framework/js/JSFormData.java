package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * FormData API for building and manipulating form data.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSFormData.*;
 *
 * // Create from form element
 * formData(getElem("myForm"))
 *
 * // Build manually
 * formDataBuilder()
 *     .append("name", variable("userName"))
 *     .append("email", variable("userEmail"))
 *     .appendFile("avatar", variable("fileInput").dot("files").at(0))
 *     .toVal()
 *
 * // Convert to URL encoded string
 * toUrlEncoded(variable("formData"))
 * </pre>
 */
public final class JSFormData {
    private JSFormData() {}

    // ==================== Creation ====================

    /** Creates FormData from a form element */
    public static Val formData(Val formElement) {
        return new Val("new FormData(" + formElement.js() + ")");
    }

    /** Creates FormData from form by ID */
    public static Val formData(String formId) {
        return new Val("new FormData(document.getElementById('" + JS.esc(formId) + "'))");
    }

    /** Creates empty FormData */
    public static Val formData() {
        return new Val("new FormData()");
    }

    /** Creates a FormData builder for manual construction */
    public static FormDataBuilder formDataBuilder() {
        return new FormDataBuilder();
    }

    // ==================== Operations ====================

    /** Appends a value: formData.append(name, value) */
    public static Val append(Val fd, String name, Val value) {
        return new Val(fd.js() + ".append('" + JS.esc(name) + "'," + value.js() + ")");
    }

    /** Sets a value (replaces if exists): formData.set(name, value) */
    public static Val set(Val fd, String name, Val value) {
        return new Val(fd.js() + ".set('" + JS.esc(name) + "'," + value.js() + ")");
    }

    /** Gets a value: formData.get(name) */
    public static Val get(Val fd, String name) {
        return new Val(fd.js() + ".get('" + JS.esc(name) + "')");
    }

    /** Gets all values for a name: formData.getAll(name) */
    public static Val getAll(Val fd, String name) {
        return new Val(fd.js() + ".getAll('" + JS.esc(name) + "')");
    }

    /** Checks if key exists: formData.has(name) */
    public static Val has(Val fd, String name) {
        return new Val(fd.js() + ".has('" + JS.esc(name) + "')");
    }

    /** Deletes a key: formData.delete(name) */
    public static Val delete(Val fd, String name) {
        return new Val(fd.js() + ".delete('" + JS.esc(name) + "')");
    }

    /** Converts FormData to URL-encoded string */
    public static Val toUrlEncoded(Val fd) {
        return new Val("new URLSearchParams(" + fd.js() + ").toString()");
    }

    /** Converts FormData to plain object */
    public static Val toObject(Val fd) {
        return new Val("Object.fromEntries(" + fd.js() + ")");
    }

    // ==================== Builder ====================

    public static class FormDataBuilder {
        private final StringBuilder code = new StringBuilder("(function(){var fd=new FormData();");

        public FormDataBuilder append(String name, Val value) {
            code.append("fd.append('").append(JS.esc(name)).append("',").append(value.js()).append(");");
            return this;
        }

        public FormDataBuilder append(String name, String value) {
            code.append("fd.append('").append(JS.esc(name)).append("','").append(JS.esc(value)).append("');");
            return this;
        }

        public FormDataBuilder appendFile(String name, Val file) {
            code.append("fd.append('").append(JS.esc(name)).append("',").append(file.js()).append(");");
            return this;
        }

        public FormDataBuilder appendFile(String name, Val file, String filename) {
            code.append("fd.append('").append(JS.esc(name)).append("',").append(file.js())
                .append(",'").append(JS.esc(filename)).append("');");
            return this;
        }

        public Val toVal() {
            return new Val(code.append("return fd}())").toString());
        }

        public String build() {
            return toVal().js();
        }
    }
}
