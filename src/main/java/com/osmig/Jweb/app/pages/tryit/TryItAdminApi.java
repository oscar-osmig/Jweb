package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.api.GET;
import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.email.EmailTemplate;
import com.osmig.Jweb.framework.email.Mailer;
import static com.osmig.Jweb.framework.elements.El.*;
import com.osmig.Jweb.framework.server.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;

/** Admin API endpoints for managing access requests. */
@REST("/api/try-it/admin")
public class TryItAdminApi {
    private static final Logger LOG = Logger.getLogger(TryItAdminApi.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm").withZone(ZoneId.systemDefault());

    @Value("${jweb.admin.token}")
    private String adminToken;

    @Value("${jweb.admin.email}")
    private String adminEmail;

    @GET("/requests")
    public ResponseEntity<String> getPendingRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        return Response.json(TryItDb.findByStatus("PENDING").stream().map(TryItDb::docToMap).toList());
    }

    @GET("/requests/all")
    public ResponseEntity<String> getAllRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        return Response.json(TryItDb.findAll().stream().map(TryItDb::docToMap).toList());
    }

    @POST("/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable String id,
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        Doc request = TryItDb.findById(id);
        if (request == null) return Response.badRequest("Request not found");
        if ("APPROVED".equals(request.getString("status")) && TryItDb.isTokenValid(request))
            return Response.badRequest("Request already approved with active token");

        String token = TryItDb.approveRequest(id);
        String reqEmail = request.getString("email");
        long expiry = Instant.now().plusSeconds(7 * 24 * 60 * 60).toEpochMilli();
        sendApprovalEmail(reqEmail, token, DATE_FMT.format(Instant.ofEpochMilli(expiry)));
        return Response.json(Map.of("success", true, "message", "Request approved. Token sent to " + reqEmail, "token", token));
    }

    @POST("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable String id, @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        Doc request = TryItDb.findById(id);
        if (request == null) return Response.badRequest("Request not found");
        TryItDb.rejectRequest(id);
        String reason = body != null ? body.get("reason") : null;
        sendRejectionEmail(request.getString("email"), reason);
        return Response.json(Map.of("success", true, "message", "Request rejected"));
    }

    private boolean isValidAdmin(String key, String email) {
        return adminToken.equals(key) && adminEmail.equalsIgnoreCase(email);
    }

    private void sendApprovalEmail(String to, String token, String validUntil) {
        var email = EmailTemplate.create()
            .to(to)
            .subject("Your JWeb Framework Access Has Been Approved!")
            .primaryColor("#10b981")
            .layout(EmailTemplate.Layout.CARD)
            .header(h1("Access Approved!"))
            .body(
                p("Congratulations! Your request for JWeb Framework access has been approved."),
                div(
                    p(strong("YOUR ACCESS TOKEN")),
                    p(code(token))
                ).style(com.osmig.Jweb.framework.styles.Styles.style()
                    .unsafeProp("background", "#f0fdf4")
                    .unsafeProp("padding", "16px")
                    .unsafeProp("border-radius", "8px")
                    .unsafeProp("text-align", "center")
                    .unsafeProp("margin", "16px 0")),
                p("Valid until: " + validUntil),
                EmailTemplate.button("Download Starter Project", "https://jweb.dev/try-it", "#10b981"),
                p(small("Use your token on the Try It page to download your starter project."))
            )
            .footer(
                p("Happy coding!"),
                p("The JWeb Team")
            )
            .build();

        Mailer.sendInBackground(email);
        LOG.info("Approval email sent to: " + to);
    }

    private void sendRejectionEmail(String to, String reason) {
        var email = EmailTemplate.create()
            .to(to)
            .subject("Update on Your JWeb Framework Access Request")
            .primaryColor("#ef4444")
            .layout(EmailTemplate.Layout.CARD)
            .header(h1("Request Update"))
            .body(
                p("Thank you for your interest in JWeb Framework."),
                p("Unfortunately, your access request has been declined."),
                reason != null ? p(strong("Reason: "), text(reason)) : null,
                p("If you believe this was in error, please submit a new request with more details about your intended use.")
            )
            .footer(p("The JWeb Team"))
            .build();

        Mailer.sendInBackground(email);
        LOG.info("Rejection email sent to: " + to);
    }
}
