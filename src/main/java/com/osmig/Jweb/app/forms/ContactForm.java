package com.osmig.Jweb.app.forms;

import jweb.Form;

/**
 * The contact form. The record is the form: it renders the fields, states the
 * rules the server enforces, and comes back from {@code Form.bind} as a value.
 */
public record ContactForm(
    @Form.Required @Form.Length(max = 200) String name,
    @Form.Required @Form.Email @Form.Length(max = 320) String email,
    @Form.Required @Form.Multiline(rows = 4) @Form.Length(max = 5_000) String message) {
}
