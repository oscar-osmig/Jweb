# Email

JWeb provides a fluent email builder for sending emails.

## Basic Usage

```java
import com.osmig.Jweb.framework.email.Email;
import com.osmig.Jweb.framework.email.Mailer;

Email email = Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .text("Hello and welcome to our platform.")
    .build();

Mailer.send(email);
```

## Building Emails

### Simple Text Email

```java
Email email = Email.create()
    .to("user@example.com")
    .subject("Hello")
    .text("This is a plain text email.")
    .build();
```

### HTML Email

```java
Email email = Email.create()
    .to("user@example.com")
    .subject("Welcome!")
    .html("<h1>Welcome!</h1><p>Thank you for signing up.</p>")
    .build();
```

### Both Text and HTML

```java
Email email = Email.create()
    .to("user@example.com")
    .subject("Newsletter")
    .text("View this email in a browser for the best experience.")
    .html("<h1>Newsletter</h1><p>Our latest updates...</p>")
    .build();
```

## Recipients

### Multiple Recipients

```java
Email email = Email.create()
    .to("user1@example.com", "user2@example.com")
    .subject("Team Update")
    .text("Hello everyone!")
    .build();
```

### CC and BCC

```java
Email email = Email.create()
    .to("main@example.com")
    .cc("copy@example.com", "copy2@example.com")
    .bcc("hidden@example.com")
    .subject("Important Update")
    .text("...")
    .build();
```

## From and Reply-To

```java
Email email = Email.create()
    .from("noreply@myapp.com")
    .replyTo("support@myapp.com")
    .to("user@example.com")
    .subject("Your Order")
    .text("...")
    .build();
```

## Attachments

### From File

```java
import java.io.File;

Email email = Email.create()
    .to("user@example.com")
    .subject("Invoice")
    .text("Please find your invoice attached.")
    .attach(new File("invoice.pdf"))
    .build();
```

### From Bytes

```java
byte[] pdfContent = generatePdf();

Email email = Email.create()
    .to("user@example.com")
    .subject("Report")
    .text("Here is your report.")
    .attach("report.pdf", pdfContent, "application/pdf")
    .build();
```

### Multiple Attachments

```java
Email email = Email.create()
    .to("user@example.com")
    .subject("Documents")
    .text("Please review the attached documents.")
    .attach(new File("doc1.pdf"))
    .attach(new File("doc2.pdf"))
    .attach("data.csv", csvBytes, "text/csv")
    .build();
```

## Custom Headers

```java
Email email = Email.create()
    .to("user@example.com")
    .subject("Notification")
    .text("...")
    .header("X-Priority", "1")
    .header("X-Campaign-ID", "spring-2024")
    .build();
```

## Email Properties

```java
Email email = ...;

// Access properties
List<String> recipients = email.getTo();
List<String> ccList = email.getCc();
String subject = email.getSubject();
String textBody = email.getTextBody();
String htmlBody = email.getHtmlBody();

// Check content type
if (email.hasHtml()) {
    // Has HTML body
}

if (email.hasAttachments()) {
    List<Email.Attachment> attachments = email.getAttachments();
}
```

## Mailer Configuration

Configure the mailer in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Using with Templates

Generate HTML emails using JWeb's element DSL:

```java
public class WelcomeEmail {
    public static Email create(User user) {
        String html = emailTemplate(user).toHtml();

        return Email.create()
            .to(user.getEmail())
            .subject("Welcome to Our App!")
            .html(html)
            .build();
    }

    private static Element emailTemplate(User user) {
        return html(
            head(
                style("""
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; }
                    .header { background: #4a90d9; color: white; padding: 20px; }
                    .content { padding: 20px; }
                    .button { background: #4a90d9; color: white; padding: 10px 20px;
                              text-decoration: none; border-radius: 4px; }
                """)
            ),
            body(
                div(class_("container"),
                    div(class_("header"),
                        h1("Welcome!")
                    ),
                    div(class_("content"),
                        p("Hello " + user.getName() + ","),
                        p("Thank you for joining our platform!"),
                        p("Get started by visiting your dashboard:"),
                        p(
                            a(class_("button"), href("https://myapp.com/dashboard"),
                                "Go to Dashboard"
                            )
                        )
                    )
                )
            )
        );
    }
}
```

## Async Email Sending

Send emails in the background:

```java
import com.osmig.Jweb.framework.async.Jobs;

// Send asynchronously
Jobs.run(() -> {
    Email email = Email.create()
        .to("user@example.com")
        .subject("Welcome!")
        .text("...")
        .build();

    Mailer.send(email);
});
```

## Complete Example

```java
@Component
public class EmailService {
    private final Mailer mailer;

    public void sendWelcomeEmail(User user) {
        Jobs.run(() -> {
            Email email = Email.create()
                .from("noreply@myapp.com")
                .to(user.getEmail())
                .subject("Welcome to MyApp!")
                .html(welcomeTemplate(user))
                .build();

            mailer.send(email);
        });
    }

    public void sendPasswordReset(User user, String resetToken) {
        String resetUrl = "https://myapp.com/reset?token=" + resetToken;

        Email email = Email.create()
            .from("noreply@myapp.com")
            .to(user.getEmail())
            .subject("Password Reset Request")
            .text("Click here to reset your password: " + resetUrl)
            .html("<p>Click <a href=\"" + resetUrl + "\">here</a> to reset your password.</p>")
            .build();

        mailer.send(email);
    }

    public void sendInvoice(User user, byte[] invoicePdf, String invoiceNumber) {
        Email email = Email.create()
            .from("billing@myapp.com")
            .to(user.getEmail())
            .subject("Invoice #" + invoiceNumber)
            .text("Please find your invoice attached.")
            .attach("invoice-" + invoiceNumber + ".pdf", invoicePdf, "application/pdf")
            .build();

        mailer.send(email);
    }

    private String welcomeTemplate(User user) {
        return html(
            body(
                h1("Welcome, " + user.getName() + "!"),
                p("We're excited to have you on board."),
                p(a(href("https://myapp.com/getting-started"), "Get Started"))
            )
        ).toHtml();
    }
}
```

## Route Example

```java
app.post("/contact", req -> {
    String name = req.formParam("name");
    String email = req.formParam("email");
    String message = req.formParam("message");

    Jobs.run(() -> {
        Email contactEmail = Email.create()
            .from(email)
            .to("support@myapp.com")
            .subject("Contact Form: " + name)
            .text(message)
            .build();

        Mailer.send(contactEmail);
    });

    return Response.redirect("/contact?sent=true");
});
```
