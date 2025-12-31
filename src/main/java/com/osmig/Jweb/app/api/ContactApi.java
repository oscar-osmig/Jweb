package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.email.EmailTemplate;
import com.osmig.Jweb.framework.email.Mailer;
import com.osmig.Jweb.framework.server.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.logging.Logger;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Contact form API - sends messages to the admin email.
 */
@REST("/api/v1/contact")
public class ContactApi {
    private static final Logger LOG = Logger.getLogger(ContactApi.class.getName());

    @Value("${jweb.admin.email}")
    private String adminEmail;

    @POST
    public ResponseEntity<String> submit(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String email = data.get("email");
        String message = data.get("message");

        if (name == null || name.isBlank()) {
            return Response.badRequest("Name is required");
        }
        if (email == null || email.isBlank()) {
            return Response.badRequest("Email is required");
        }
        if (message == null || message.isBlank()) {
            return Response.badRequest("Message is required");
        }

        sendContactEmail(name, email, message);
        return Response.json(Map.of("success", true, "message", "Message sent successfully"));
    }

    private void sendContactEmail(String name, String email, String message) {
        var contactEmail = EmailTemplate.create()
            .to(adminEmail)
            .replyTo(email)
            .subject("JWeb Contact Form: Message from " + name)
            .primaryColor("#3b82f6")
            .layout(EmailTemplate.Layout.CARD)
            .header(h1("New Contact Message"))
            .body(
                p(strong("From: "), text(name)),
                p(strong("Email: "), a(email).attr("href", "mailto:" + email)),
                EmailTemplate.divider(),
                p(strong("Message:")),
                p(text(message))
            )
            .footer(
                p("This message was sent via the JWeb contact form."),
                p(small("Reply directly to respond to " + email))
            )
            .build();

        Mailer.sendInBackground(contactEmail);
        LOG.info("Contact form email sent to admin from: " + email);
    }
}
