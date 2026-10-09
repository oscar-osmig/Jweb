package com.osmig.Jweb.app.forms;

import jweb.Form;

/**
 * The Sandbox's "Add snippet" form: a title and the author's name, with the
 * editor's current code riding in a hidden field. The Sandbox renders the
 * small form itself so the code field can stay hidden; this record is what
 * the route binds and validates.
 */
public record SnippetSubmission(
    @Form.Required @Form.Length(max = 80) String title,
    @Form.Required @Form.Length(max = 80) @Form.Label("Your name") String author,
    // 10 000 characters is the interpreter's own source cap (SandboxDsl.MAX_SOURCE)
    @Form.Required @Form.Length(max = 10_000) String code) {
}
