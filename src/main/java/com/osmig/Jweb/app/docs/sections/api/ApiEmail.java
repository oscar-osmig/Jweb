package com.osmig.Jweb.app.docs.sections.api;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiEmail {
    private ApiEmail() {}

    public static Element render() {
        return section(
            h3Title("Email"),
            para("Fluent email builder for sending emails with attachments."),
            codeBlock("""
import com.osmig.Jweb.framework.email.Email;
import com.osmig.Jweb.framework.email.Mailer;

// Simple text email
Email email = Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .text("Hello and welcome to our platform.")
    .build();

Mailer.send(email);"""),

            h3Title("HTML Email"),
            codeBlock("""
Email email = Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .html("<h1>Welcome!</h1><p>Thank you for signing up.</p>")
    .build();

// Or use JWeb's element DSL
String html = html(
    body(
        h1("Welcome!"),
        p("Thank you for signing up."),
        a(href("https://app.com"), "Get Started")
    )
).toHtml();

Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .html(html)
    .build();"""),

            h3Title("Multiple Recipients"),
            codeBlock("""
Email.create()
    .to("user1@example.com", "user2@example.com")
    .cc("manager@example.com")
    .bcc("archive@example.com")
    .from("noreply@app.com")
    .replyTo("support@app.com")
    .subject("Team Update")
    .text("Hello everyone!")
    .build();"""),

            h3Title("Attachments"),
            codeBlock("""
// From file
Email.create()
    .to("user@example.com")
    .subject("Invoice")
    .text("Please find your invoice attached.")
    .attach(new File("invoice.pdf"))
    .build();

// From bytes
byte[] pdfContent = generatePdf();
Email.create()
    .to("user@example.com")
    .subject("Report")
    .attach("report.pdf", pdfContent, "application/pdf")
    .build();"""),

            h3Title("Async Sending"),
            codeBlock("""
import com.osmig.Jweb.framework.async.Jobs;

Jobs.run(() -> {
    Email email = Email.create()
        .to("user@example.com")
        .subject("Welcome!")
        .html(welcomeTemplate(user))
        .build();
    Mailer.send(email);
});"""),

            docTip("Configure SMTP in application.yaml: spring.mail.host, port, username, password")
        );
    }
}
