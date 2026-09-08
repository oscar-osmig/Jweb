package com.osmig.Jweb.app.forms;

import jweb.Form;

/** The admin sign-in form. */
public record AdminLogin(
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Password @Form.Label("Admin Token") String token) {
}
