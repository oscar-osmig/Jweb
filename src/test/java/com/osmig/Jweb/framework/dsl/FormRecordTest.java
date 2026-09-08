package com.osmig.Jweb.framework.dsl;

import jweb.Form;
import jweb.Request;
import jweb.UploadedFile;
import jweb.ValidationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The one form system: a record renders the form, states the rules, and comes
 * back from {@code Form.bind} as a value.
 */
class FormRecordTest {

    enum Plan { FREE, PRO }

    record Contact(
        @Form.Required String name,
        @Form.Required @Form.Email String email,
        @Form.Required @Form.Multiline(rows = 4) @Form.Length(max = 20) String message) {}

    record Everything(
        String text,
        @Form.Email String email,
        @Form.Password String secret,
        @Form.Multiline String notes,
        int count,
        long big,
        double amount,
        boolean agreed,
        Plan plan,
        LocalDate startsOn,
        UploadedFile attachment) {}

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ==================== rendering ====================

    @Test
    void aRecordRendersOneLabelledFieldPerComponent() {
        String html = form(Contact.class).action("/contact").submit("Send").toHtml();

        assertTrue(html.startsWith("<form"), html);
        assertTrue(html.contains("action=\"/contact\""), html);
        assertTrue(html.contains("method=\"post\""), html);
        assertTrue(html.contains("<label for=\"name\" class=\"jweb-label\">Name</label>"), html);
        assertTrue(html.contains("<input class=\"jweb-control\" name=\"name\" id=\"name\" required type=\"text\">"), html);
        assertTrue(html.contains("type=\"email\""), html);
        assertTrue(html.contains("<textarea"), html);
        assertTrue(html.contains("rows=\"4\""), html);
        assertTrue(html.contains("maxlength=\"20\""), html);
        assertTrue(html.contains("<button type=\"submit\" class=\"jweb-submit\">Send</button>"), html);
    }

    @Test
    void theJavaTypePicksTheControl() {
        String html = form(Everything.class).toHtml();

        assertTrue(html.contains("name=\"text\" id=\"text\" type=\"text\""), html);
        assertTrue(html.contains("name=\"email\" id=\"email\" type=\"email\""), html);
        assertTrue(html.contains("name=\"secret\" id=\"secret\" type=\"password\""), html);
        assertTrue(html.contains("<textarea class=\"jweb-control\" name=\"notes\""), html);
        assertTrue(html.contains("name=\"count\" id=\"count\" type=\"number\""), html);
        assertTrue(html.contains("name=\"big\" id=\"big\" type=\"number\""), html);
        assertTrue(html.contains("name=\"amount\" id=\"amount\" type=\"number\" step=\"any\""), html);
        assertTrue(html.contains("name=\"agreed\" id=\"agreed\" type=\"checkbox\" value=\"true\""), html);
        assertTrue(html.contains("<select class=\"jweb-control\" name=\"plan\""), html);
        assertTrue(html.contains("<option value=\"FREE\">FREE</option>"), html);
        assertTrue(html.contains("<option value=\"PRO\">PRO</option>"), html);
        assertTrue(html.contains("name=\"startsOn\" id=\"startsOn\" type=\"date\""), html);
        assertTrue(html.contains("name=\"attachment\" id=\"attachment\" type=\"file\""), html);
        // an UploadedFile component makes the whole form multipart
        assertTrue(html.contains("enctype=\"multipart/form-data\""), html);
        // a camelCase component name becomes a readable label
        assertTrue(html.contains(">Starts on</label>"), html);
        // a checkbox puts its control before the label, and says so in the class
        assertTrue(html.contains("<div class=\"jweb-field jweb-field-checkbox\">"
            + "<input class=\"jweb-control\" name=\"agreed\""), html);
    }

    @Test
    void fieldOverridesChangePresentationOnly() {
        String html = form(Contact.class)
            .field("email", f -> f.label("Your email").placeholder("you@example.com").autocomplete("email"))
            .field("message", f -> f.rows(8).help("Markdown is fine."))
            .toHtml();

        assertTrue(html.contains(">Your email</label>"), html);
        assertTrue(html.contains("placeholder=\"you@example.com\""), html);
        assertTrue(html.contains("autocomplete=\"email\""), html);
        assertTrue(html.contains("rows=\"8\""), html);
        assertTrue(html.contains("<small class=\"jweb-help\">Markdown is fine.</small>"), html);
        // the rule still comes from the record, not the override
        assertTrue(html.contains("name=\"email\" id=\"email\" required"), html);
    }

    @Test
    void anUnknownFieldNameFailsLoudly() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> form(Contact.class).field("nope", f -> f));
        assertTrue(ex.getMessage().contains("nope"), ex.getMessage());
    }

    @Test
    void aNonRecordIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Form.of(nonRecord()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Record> Class<T> nonRecord() {
        return (Class<T>) (Class<?>) String.class;
    }

    // ==================== CSRF ====================

    @Test
    void theCsrfFieldComesFromTheCurrentRequest() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        String html = form(Contact.class).action("/contact").toHtml();
        assertTrue(html.contains("<input type=\"hidden\" name=\"_csrf\" value=\""), html);
    }

    @Test
    void withoutARequestTheFormSimplyCarriesNoToken() {
        String html = form(Contact.class).action("/contact").toHtml();
        assertFalse(html.contains("_csrf"), html);
    }

    @Test
    void anExplicitTokenOverridesTheAmbientOne() {
        String html = form(Contact.class).csrf(jweb.CsrfToken.of("abc123", Long.MAX_VALUE)).toHtml();
        assertTrue(html.contains("name=\"_csrf\" value=\"abc123\""), html);
    }

    // ==================== validation and binding ====================

    @Test
    void validateUsesTheRecordsAnnotations() {
        ValidationResult empty = Form.validate(Contact.class, Map.of());
        assertTrue(empty.hasErrors("name"));
        assertTrue(empty.hasErrors("email"));
        assertTrue(empty.hasErrors("message"));
        assertEquals("Name is required", empty.getFirstError("name"));

        ValidationResult badEmail = Form.validate(Contact.class,
            Map.of("name", "Ada", "email", "not-an-email", "message", "hi"));
        assertTrue(badEmail.hasErrors("email"));
        assertFalse(badEmail.hasErrors("name"));
        assertEquals("Email must be a valid email address", badEmail.getFirstError("email"));

        ValidationResult tooLong = Form.validate(Contact.class,
            Map.of("name", "Ada", "email", "ada@example.com", "message", "x".repeat(21)));
        assertTrue(tooLong.hasErrors("message"), tooLong.toString());

        assertTrue(Form.validate(Contact.class,
            Map.of("name", "Ada", "email", "ada@example.com", "message", "hi")).isValid());
    }

    @Test
    void bindBuildsTheRecordWhenTheSubmissionIsValid() {
        Request request = post(Map.of(
            "name", "Ada", "email", "ada@example.com", "message", "Hello"));

        Form.Bound<Contact> bound = Form.bind(Contact.class, request);

        assertTrue(bound.ok());
        assertEquals(new Contact("Ada", "ada@example.com", "Hello"), bound.value());
        assertTrue(bound.errors().isValid());
        assertEquals("Ada", bound.submitted().get("name"));
    }

    @Test
    void bindReportsTheErrorsAndKeepsTheInputWhenItIsNot() {
        Request request = post(Map.of("name", "", "email", "nope", "message", "Hello"));

        Form.Bound<Contact> bound = Form.bind(Contact.class, request);

        assertFalse(bound.ok());
        assertNull(bound.value());
        assertTrue(bound.errors().hasErrors("name"));
        assertTrue(bound.errors().hasErrors("email"));
        assertEquals("Hello", bound.submitted().get("message"));
    }

    @Test
    void bindConvertsEveryComponentType() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("text", "t");
        values.put("email", "a@b.co");
        values.put("secret", "s");
        values.put("notes", "n");
        values.put("count", "3");
        values.put("big", "9");
        values.put("amount", "1.5");
        values.put("agreed", "on");
        values.put("plan", "PRO");
        values.put("startsOn", "2026-01-21");

        Form.Bound<Everything> bound = Form.bind(Everything.class, post(values));

        assertTrue(bound.ok(), bound.errors().toString());
        Everything value = bound.value();
        assertEquals(3, value.count());
        assertEquals(9L, value.big());
        assertEquals(1.5d, value.amount());
        assertTrue(value.agreed());
        assertEquals(Plan.PRO, value.plan());
        assertEquals(LocalDate.of(2026, 1, 21), value.startsOn());
        assertNull(value.attachment());
    }

    @Test
    void aValueThatDoesNotParseIsAnErrorNotAnException() {
        Form.Bound<Everything> bound = Form.bind(Everything.class,
            post(Map.of("count", "twelve", "plan", "ENTERPRISE", "startsOn", "yesterday")));

        assertFalse(bound.ok());
        assertEquals("Count must be a number", bound.errors().getFirstError("count"));
        assertEquals("Plan must be one of the allowed values", bound.errors().getFirstError("plan"));
        assertEquals("Starts on must be a date (YYYY-MM-DD)", bound.errors().getFirstError("startsOn"));
    }

    // ==================== error rendering ====================

    @Test
    void errorsRenderNextToTheFieldAndAsASummaryWithTheValuesAsTyped() {
        Form.Bound<Contact> bound = Form.bind(Contact.class,
            post(Map.of("name", "Ada", "email", "nope", "message", "Hello")));

        String html = form(Contact.class).action("/contact").errors(bound).submit("Send").toHtml();

        // summary
        assertTrue(html.contains("<div class=\"jweb-errors\" role=\"alert\">"), html);
        assertTrue(html.contains("Email must be a valid email address"), html);
        // per-field message + aria wiring
        assertTrue(html.contains("aria-invalid=\"true\""), html);
        assertTrue(html.contains("aria-describedby=\"email-error\""), html);
        assertTrue(html.contains("<small id=\"email-error\" class=\"jweb-error\">"), html);
        assertTrue(html.contains("class=\"jweb-field jweb-field-invalid\""), html);
        // the values the user typed survive the re-render
        assertTrue(html.contains("name=\"name\" id=\"name\" required type=\"text\" value=\"Ada\""), html);
        assertTrue(html.contains(">Hello</textarea>"), html);
        // the valid field is not marked invalid
        assertFalse(html.contains("aria-describedby=\"name-error\""), html);
    }

    @Test
    void valuesFillAnEditForm() {
        String html = form(Contact.class)
            .values(new Contact("Ada", "ada@example.com", "Hi"))
            .toHtml();
        assertTrue(html.contains("value=\"Ada\""), html);
        assertTrue(html.contains("value=\"ada@example.com\""), html);
        assertTrue(html.contains(">Hi</textarea>"), html);
    }

    @Test
    void anEnumValueSelectsItsOption() {
        String html = form(Everything.class).values(Map.of("plan", "PRO")).toHtml();
        assertTrue(html.contains("<option value=\"PRO\" selected>PRO</option>"), html);
        assertTrue(html.contains("<option value=\"FREE\">FREE</option>"), html);
    }

    @Test
    void swapFormKeepsTheNoJavaScriptAction() {
        String html = form(Contact.class).swapForm("/contact", "#status").toHtml();
        assertTrue(html.contains("data-swap-post=\"/contact\""), html);
        assertTrue(html.contains("data-swap-target=\"#status\""), html);
        assertTrue(html.contains("action=\"/contact\""), html);
    }

    @Test
    void stylesCoverTheClassNamesTheFormEmits() {
        String css = Form.styles();
        for (String cls : new String[] {".jweb-form", ".jweb-field", ".jweb-label", ".jweb-control",
                                        ".jweb-help", ".jweb-error", ".jweb-errors", ".jweb-submit"}) {
            assertTrue(css.contains(cls), cls + " missing from Form.styles(): " + css);
        }
    }

    private static Request post(Map<String, String> values) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/contact");
        values.forEach(servletRequest::setParameter);
        return new Request(servletRequest);
    }
}
