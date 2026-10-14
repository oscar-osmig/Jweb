# Mail

`jweb.Mail` builds a message like an element and sends it now or later. No mail library:
the framework speaks SMTP itself when `jweb.mail.smtp.host` is set; otherwise the outbox
keeps the last two hundred messages in memory and logs each one.

## Sending

```java
import jweb.Mail;
import static jweb.El.*;

byte[] csv = "a,b\n".getBytes();

Mail.to("ada@example.com")
    .subject("Welcome")
    .html(div(h1("Welcome, Ada"), p("Your account is ready.")))   // a text part is derived from the HTML
    .send();

Mail.to("ada@example.com", "bob@example.com").cc("ops@example.com").bcc("audit@example.com")
    .from("Reports <reports@example.com>").replyTo("help@example.com")
    .subject("Your export").text("Attached.")
    .attach("export.csv", csv, "text/csv")
    .header("X-Campaign", "exports")
    .send();
```

## Later: a durable job

```java
import jweb.Mail;
import jweb.Jobs;

Jobs.Job queued = Mail.to("ada@example.com").subject("Digest").text("...").later();
```

`later()` enqueues the message as the `jweb.mail` job: the `Jobs` worker sends it, retries
with backoff when the server is down, and on the SQL store it survives a restart.

## The outbox, in a test

```java
import jweb.Mail;

Mail.to("ada@example.com").subject("Hi").text("there").send();
Mail.Message last = Mail.outbox().last();
List<Mail.Message> all = Mail.outbox().messages();
Mail.outbox().clear();
```

## Configuration

```yaml
jweb:
  mail:
    from: "App <no-reply@example.com>"
    smtp:
      host: ${SMTP_HOST:}        # blank = outbox
      port: 587
      username: ${SMTP_USER:}
      password: ${SMTP_PASSWORD:}
      tls: starttls              # starttls | implicit | none
```

STARTTLS on 587 by default, implicit TLS on 465, AUTH PLAIN or LOGIN with a username.
`send()` throws `Mail.MailException` with the server's refusal. Non-ASCII subjects are
RFC 2047 encoded; bodies and attachments travel base64.

## Another sender

```java
import jweb.Mail;
import jweb.Fetch;

Mail.Sender resend = message -> Fetch.post("https://api.resend.com/emails")
    .header("Authorization", "Bearer " + System.getenv("RESEND_API_KEY"))
    .json(Map.of("from", message.from(), "to", message.to(), "subject", message.subject(),
        "html", message.html(), "text", message.text()))
    .send();
Mail.sender(resend);
```

A `Mail.Sender` bean is picked up at startup.
