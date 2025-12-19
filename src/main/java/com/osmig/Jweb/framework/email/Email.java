package com.osmig.Jweb.framework.email;

import java.io.File;
import java.util.*;

/**
 * Email message builder.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Email email = Email.create()
 *     .to("user@example.com")
 *     .subject("Welcome!")
 *     .text("Hello and welcome to our platform.")
 *     .build();
 *
 * Mailer.send(email);
 * }</pre>
 *
 * <h2>HTML Email</h2>
 * <pre>{@code
 * Email email = Email.create()
 *     .to("user@example.com")
 *     .subject("Your Order")
 *     .html("<h1>Order Confirmed</h1><p>Thank you for your order!</p>")
 *     .build();
 * }</pre>
 *
 * <h2>With Attachments</h2>
 * <pre>{@code
 * Email email = Email.create()
 *     .to("user@example.com")
 *     .subject("Invoice")
 *     .text("Please find your invoice attached.")
 *     .attach(new File("invoice.pdf"))
 *     .build();
 * }</pre>
 */
public class Email {

    private final List<String> to;
    private final List<String> cc;
    private final List<String> bcc;
    private final String from;
    private final String replyTo;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private final List<Attachment> attachments;
    private final Map<String, String> headers;

    private Email(Builder builder) {
        this.to = List.copyOf(builder.to);
        this.cc = List.copyOf(builder.cc);
        this.bcc = List.copyOf(builder.bcc);
        this.from = builder.from;
        this.replyTo = builder.replyTo;
        this.subject = builder.subject;
        this.textBody = builder.textBody;
        this.htmlBody = builder.htmlBody;
        this.attachments = List.copyOf(builder.attachments);
        this.headers = Map.copyOf(builder.headers);
    }

    public static Builder create() {
        return new Builder();
    }

    public List<String> getTo() { return to; }
    public List<String> getCc() { return cc; }
    public List<String> getBcc() { return bcc; }
    public String getFrom() { return from; }
    public String getReplyTo() { return replyTo; }
    public String getSubject() { return subject; }
    public String getTextBody() { return textBody; }
    public String getHtmlBody() { return htmlBody; }
    public List<Attachment> getAttachments() { return attachments; }
    public Map<String, String> getHeaders() { return headers; }
    public boolean hasHtml() { return htmlBody != null && !htmlBody.isEmpty(); }
    public boolean hasAttachments() { return !attachments.isEmpty(); }

    /**
     * Email attachment.
     */
    public record Attachment(String filename, byte[] content, String contentType) {
        public static Attachment fromFile(File file) {
            try {
                byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
                String contentType = java.nio.file.Files.probeContentType(file.toPath());
                return new Attachment(file.getName(), content,
                    contentType != null ? contentType : "application/octet-stream");
            } catch (Exception e) {
                throw new RuntimeException("Failed to read attachment: " + file.getName(), e);
            }
        }
    }

    /**
     * Email builder.
     */
    public static class Builder {
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String from;
        private String replyTo;
        private String subject = "";
        private String textBody;
        private String htmlBody;
        private final List<Attachment> attachments = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();

        /**
         * Adds a recipient.
         */
        public Builder to(String... addresses) {
            to.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Adds CC recipients.
         */
        public Builder cc(String... addresses) {
            cc.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Adds BCC recipients.
         */
        public Builder bcc(String... addresses) {
            bcc.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Sets the from address.
         */
        public Builder from(String address) {
            this.from = address;
            return this;
        }

        /**
         * Sets the reply-to address.
         */
        public Builder replyTo(String address) {
            this.replyTo = address;
            return this;
        }

        /**
         * Sets the subject.
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets the plain text body.
         */
        public Builder text(String body) {
            this.textBody = body;
            return this;
        }

        /**
         * Sets the HTML body.
         */
        public Builder html(String body) {
            this.htmlBody = body;
            return this;
        }

        /**
         * Adds a file attachment.
         */
        public Builder attach(File file) {
            attachments.add(Attachment.fromFile(file));
            return this;
        }

        /**
         * Adds an attachment with content.
         */
        public Builder attach(String filename, byte[] content, String contentType) {
            attachments.add(new Attachment(filename, content, contentType));
            return this;
        }

        /**
         * Adds a custom header.
         */
        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Builds the email.
         */
        public Email build() {
            if (to.isEmpty()) {
                throw new IllegalStateException("Email must have at least one recipient");
            }
            return new Email(this);
        }
    }
}
