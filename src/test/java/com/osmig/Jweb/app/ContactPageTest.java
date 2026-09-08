package com.osmig.Jweb.app;

import com.osmig.Jweb.app.forms.ContactForm;
import com.osmig.Jweb.app.pages.ContactPage;
import jweb.Form;
import jweb.MockRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The framework's own contact page is the proof that a form is a record: the
 * page names {@link ContactForm} and nothing else — no field helpers, no
 * hand-passed CSRF token, no per-field markup.
 */
class ContactPageTest {

    @Test
    void thePageRendersTheRecordForm() {
        String html = new ContactPage().render().toHtml();

        assertTrue(html.contains("id=\"contact-form\""), html);
        assertTrue(html.contains("action=\"/contact/submit\""), html);
        assertTrue(html.contains("data-swap-post=\"/contact/submit\""), html);
        assertTrue(html.contains("data-swap-target=\"#form-status\""), html);
        assertTrue(html.contains("<div id=\"form-status\""), html);

        // one labelled control per record component, typed by the Java type
        assertTrue(html.contains("<label for=\"name\" class=\"jweb-label\">Name</label>"), html);
        assertTrue(html.contains("placeholder=\"Your name\""), html);
        assertTrue(html.contains("type=\"email\""), html);
        assertTrue(html.contains("placeholder=\"you@example.com\""), html);
        assertTrue(html.contains("<textarea"), html);
        assertTrue(html.contains("placeholder=\"How can we help?\""), html);
        assertTrue(html.contains("Send Message</button>"), html);
    }

    @Test
    void theRoutesValidationIsTheRecordsAnnotations() {
        assertTrue(Form.validate(ContactForm.class, Map.of()).hasErrors("name"));
        assertTrue(Form.validate(ContactForm.class,
            Map.of("name", "Ada", "email", "nope", "message", "hi")).hasErrors("email"));
        assertTrue(Form.validate(ContactForm.class,
            Map.of("name", "Ada", "email", "a@b.co", "message", "x".repeat(5_001)))
            .hasErrors("message"));
        assertTrue(Form.validate(ContactForm.class,
            Map.of("name", "Ada", "email", "a@b.co", "message", "hi")).isValid());
    }

    @Test
    void bindTurnsASubmissionIntoTheRecord() {
        var request = MockRequest.post("/contact/submit")
            .queryParam("name", "Ada")
            .queryParam("email", "ada@example.com")
            .queryParam("message", "Hello")
            .build();

        Form.Bound<ContactForm> bound = Form.bind(ContactForm.class, request);

        assertTrue(bound.ok(), bound.errors().toString());
        assertEquals("Ada", bound.value().name());
        assertEquals("ada@example.com", bound.value().email());
        assertEquals("Hello", bound.value().message());
    }
}
