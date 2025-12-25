package com.osmig.Jweb.app.tryit;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Email service for sending notifications.
 *
 * Note: This is a placeholder implementation that logs emails.
 * For production, integrate with SMTP, SendGrid, or another email provider.
 */
@Service
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());
    private static final String TEAM_EMAIL = "the.jweb.team@gmail.com";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofPattern("MMM dd, yyyy 'at' HH:mm")
        .withZone(ZoneId.systemDefault());

    /**
     * Notify the JWeb team of a new access request.
     */
    public void notifyTeamOfNewRequest(AccessRequest request) {
        String subject = "New JWeb Access Request from " + request.getEmail();
        String body = """
            New JWeb Framework Access Request
            ==================================

            Email: %s
            Submitted: %s

            Message:
            %s

            ---
            Review this request at: http://localhost:8081/admin/requests
            """.formatted(
                request.getEmail(),
                DATE_FORMAT.format(request.getCreatedAt()),
                request.getMessage()
            );

        sendEmail(TEAM_EMAIL, subject, body);
    }

    /**
     * Send approval email with download token to user.
     */
    public void sendApprovalEmail(AccessRequest request) {
        String subject = "Your JWeb Framework Access Has Been Approved!";
        String body = """
            Congratulations! Your JWeb Access Request Has Been Approved
            ============================================================

            Hi there,

            Great news! Your request to try the JWeb Framework has been approved.

            Your Download Token:
            %s

            This token is valid until: %s

            How to download:
            1. Go to: http://localhost:8081/try-it
            2. Click "Download Project"
            3. Enter your token
            4. Click Download

            You'll receive a complete Spring Boot project with the JWeb framework
            ready to use. Just extract, run, and start building!

            Happy coding!
            The JWeb Team
            """.formatted(
                request.getToken(),
                DATE_FORMAT.format(request.getTokenExpiry())
            );

        sendEmail(request.getEmail(), subject, body);
    }

    /**
     * Send rejection email to user.
     */
    public void sendRejectionEmail(AccessRequest request, String reason) {
        String subject = "Update on Your JWeb Framework Access Request";
        String body = """
            Update on Your JWeb Access Request
            ===================================

            Hi there,

            Thank you for your interest in the JWeb Framework.

            Unfortunately, we're unable to approve your request at this time.

            %s

            If you believe this was a mistake or would like to provide more
            information, please submit a new request with additional details.

            Best regards,
            The JWeb Team
            """.formatted(
                reason != null && !reason.isBlank()
                    ? "Reason: " + reason
                    : ""
            );

        sendEmail(request.getEmail(), subject, body);
    }

    /**
     * Send an email.
     *
     * TODO: Implement actual email sending (SMTP, SendGrid, etc.)
     * For now, this logs the email content.
     */
    private void sendEmail(String to, String subject, String body) {
        LOG.info("""

            ========== EMAIL ==========
            To: %s
            Subject: %s

            %s
            ===========================
            """.formatted(to, subject, body));

        // TODO: Implement actual email sending
        // Example with JavaMail:
        // Properties props = new Properties();
        // props.put("mail.smtp.host", "smtp.gmail.com");
        // props.put("mail.smtp.port", "587");
        // props.put("mail.smtp.auth", "true");
        // props.put("mail.smtp.starttls.enable", "true");
        //
        // Session session = Session.getInstance(props, new Authenticator() {
        //     protected PasswordAuthentication getPasswordAuthentication() {
        //         return new PasswordAuthentication(EMAIL, PASSWORD);
        //     }
        // });
        //
        // Message message = new MimeMessage(session);
        // message.setFrom(new InternetAddress(TEAM_EMAIL));
        // message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        // message.setSubject(subject);
        // message.setText(body);
        // Transport.send(message);
    }
}
