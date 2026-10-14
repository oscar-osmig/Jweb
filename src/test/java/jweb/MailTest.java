package jweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Mail}: the message, the outbox, the MIME it becomes, the SMTP
 * conversation against a stand-in server, and {@code later()} through the
 * jobs queue.
 */
class MailTest {

    @BeforeEach
    @AfterEach
    void clean() {
        Mail.sender(Mail.outbox());
        Mail.outbox().clear();
        Jobs.store(new Jobs.Memory());
    }

    @Test
    void aMessageBuildsReadsBackAndDerivesItsTextFromTheHtml() {
        Mail.Message m = Mail.to("ada@example.com", "bob@example.com")
            .cc("cc@example.com").bcc("bcc@example.com")
            .from("App <app@example.com>").replyTo("help@example.com")
            .subject("Welcome")
            .html(div(h1("Welcome, Ada"), p("Your account is ready."), ul(li("one"), li("two")),
                a(href("https://example.com/start"), "Start")))
            .header("X-Campaign", "welcome");

        assertEquals(List.of("ada@example.com", "bob@example.com"), m.to());
        assertEquals(List.of("ada@example.com", "bob@example.com", "cc@example.com", "bcc@example.com"), m.recipients());
        assertEquals("App <app@example.com>", m.from());
        assertEquals("help@example.com", m.replyTo());
        assertTrue(m.html().startsWith("<div><h1>Welcome, Ada</h1>"), m.html());
        String text = m.text();
        assertTrue(text.startsWith("Welcome, Ada\nYour account is ready."), text);
        assertTrue(text.contains("- one\n- two"), text);
        assertTrue(text.contains("Start (https://example.com/start)"), text);
        assertEquals("welcome", m.headers().get("X-Campaign"));

        assertEquals("no-reply@localhost", Mail.message().from(), "the default From");
        Mail.defaultFrom("Site <site@example.com>");
        assertEquals("Site <site@example.com>", Mail.message().from());
        Mail.defaultFrom("no-reply@localhost");
    }

    @Test
    void theOutboxCollectsAndValidationRefusesAnEmptyMessage() {
        Mail.to("ada@example.com").subject("Hi").text("there").send();
        assertEquals(1, Mail.outbox().size());
        assertEquals("Hi", Mail.outbox().last().subject());
        assertTrue(Mail.sender() instanceof Mail.Outbox);

        Mail.MailException noRecipient = assertThrows(Mail.MailException.class, () -> Mail.message().subject("x").send());
        assertTrue(noRecipient.getMessage().contains("no recipient"), noRecipient.getMessage());
        Mail.MailException empty = assertThrows(Mail.MailException.class, () -> Mail.to("a@b").send());
        assertTrue(empty.getMessage().contains("no subject and no body"), empty.getMessage());
        assertEquals(1, Mail.outbox().size());
    }

    @Test
    void theMimeHasBothAlternativesAnAttachmentAndEncodedHeaders() {
        byte[] csv = "a,b\n1,2\n".getBytes(StandardCharsets.UTF_8);
        Mail.Message m = Mail.to("ada@example.com").from("app@example.com")
            .subject("Résumé — naïve café")
            .text("plain").html("<p>rich &amp; <b>bold</b></p>")
            .attach("data.csv", csv, "text/csv");
        String mime = Mail.Mime.of(m);

        assertTrue(mime.contains("From: app@example.com\r\n"), mime);
        assertTrue(mime.contains("To: ada@example.com\r\n"), mime);
        assertTrue(mime.contains("Subject: =?UTF-8?B?"), mime);
        assertTrue(mime.contains("MIME-Version: 1.0\r\n"), mime);
        assertTrue(mime.contains("Message-ID: <"), mime);
        assertTrue(mime.contains("Content-Type: multipart/mixed; boundary=\"mixed-"), mime);
        assertTrue(mime.contains("Content-Type: multipart/alternative; boundary=\"alt-"), mime);
        assertTrue(mime.contains("Content-Type: text/plain; charset=UTF-8\r\n"), mime);
        assertTrue(mime.contains("Content-Type: text/html; charset=UTF-8\r\n"), mime);
        assertTrue(mime.contains("Content-Disposition: attachment; filename=\"data.csv\"\r\n"), mime);

        List<String> bodies = base64Bodies(mime);
        assertEquals(3, bodies.size(), "text, html, attachment");
        assertEquals("plain", bodies.get(0));
        assertEquals("<p>rich &amp; <b>bold</b></p>", bodies.get(1));
        assertEquals("a,b\n1,2\n", bodies.get(2));

        // the subject decodes
        Matcher subject = Pattern.compile("Subject: =\\?UTF-8\\?B\\?([^?]+)\\?=").matcher(mime);
        assertTrue(subject.find());
        assertEquals("Résumé — naïve café", new String(Base64.getDecoder().decode(subject.group(1)), StandardCharsets.UTF_8));

        // text only: no multipart at all
        String plain = Mail.Mime.of(Mail.to("a@b").from("x@y").subject("s").text("t"));
        assertFalse(plain.contains("multipart"), plain);
        assertTrue(plain.contains("Content-Type: text/plain; charset=UTF-8\r\n"), plain);

        assertEquals("a\r\n..b\r\n..", Mail.dotStuff("a\r\n.b\r\n."));
        assertEquals("ada@example.com", Mail.address("Ada Lovelace <ada@example.com>"));
    }

    @Test
    void smtpDeliversThroughTheConversation() throws Exception {
        try (FakeSmtp server = new FakeSmtp("user", "pw")) {
            Mail.Smtp smtp = new Mail.Smtp("127.0.0.1", server.port(), "user", "pw", Mail.Tls.NONE);
            Mail.sender(smtp);
            Mail.to("ada@example.com").bcc("audit@example.com").from("App <app@example.com>")
                .subject("Hello").html("<p>Hi Ada.</p>\r\n.leading dot line").send();

            List<String> commands = server.commands();
            assertEquals("EHLO localhost", commands.get(0));
            assertTrue(commands.get(1).startsWith("AUTH PLAIN "), commands.get(1));
            String token = new String(Base64.getDecoder().decode(commands.get(1).substring("AUTH PLAIN ".length())), StandardCharsets.UTF_8);
            assertEquals("\0user\0pw", token);
            assertEquals("MAIL FROM:<app@example.com>", commands.get(2));
            assertEquals("RCPT TO:<ada@example.com>", commands.get(3));
            assertEquals("RCPT TO:<audit@example.com>", commands.get(4), "bcc is a recipient");
            assertEquals("DATA", commands.get(5));
            assertEquals("QUIT", commands.get(6));

            String data = server.data();
            assertTrue(data.contains("Subject: Hello\r\n"), data);
            assertFalse(data.contains("audit@example.com"), "bcc never appears in the headers");
            List<String> bodies = base64Bodies(data);
            assertEquals("<p>Hi Ada.</p>\r\n.leading dot line", bodies.get(1), "the dot survived base64 untouched");
            assertEquals(1, Mail.outbox().size() == 0 ? 1 : 0, "the outbox was not used");
        }
    }

    @Test
    void smtpRefusalsBecomeMailExceptions() throws Exception {
        try (FakeSmtp server = new FakeSmtp("user", "pw")) {
            Mail.Smtp wrong = new Mail.Smtp("127.0.0.1", server.port(), "user", "nope", Mail.Tls.NONE);
            Mail.MailException auth = assertThrows(Mail.MailException.class,
                () -> wrong.send(Mail.to("a@b").subject("s").text("t")));
            assertTrue(auth.getMessage().contains("AUTH PLAIN refused"), auth.getMessage());

            Mail.Smtp needsTls = new Mail.Smtp("127.0.0.1", server.port(), null, null, Mail.Tls.STARTTLS);
            Mail.MailException noTls = assertThrows(Mail.MailException.class,
                () -> needsTls.send(Mail.to("a@b").subject("s").text("t")));
            assertTrue(noTls.getMessage().contains("does not offer STARTTLS"), noTls.getMessage());
        }
        Mail.Smtp down = new Mail.Smtp("127.0.0.1", 1, null, null, Mail.Tls.NONE, 500);
        assertThrows(Mail.MailException.class, () -> down.send(Mail.to("a@b").subject("s").text("t")));
    }

    @Test
    void laterEnqueuesAJobThatSendsThroughTheSender() {
        Jobs.Job job = Mail.to("ada@example.com").subject("Later").html(p("soon")).later();
        assertEquals(Mail.JOB, job.name());
        assertEquals(0, Mail.outbox().size(), "not sent yet");
        assertTrue(Jobs.handles(Mail.JOB));

        Mail.Envelope envelope = job.payload(Mail.Envelope.class);
        assertEquals(List.of("ada@example.com"), envelope.to());
        assertEquals("<p>soon</p>", envelope.html());

        assertEquals(1, Jobs.runPending());
        assertEquals(1, Mail.outbox().size());
        Mail.Message sent = Mail.outbox().last();
        assertEquals("Later", sent.subject());
        assertEquals("<p>soon</p>", sent.html());
        assertEquals("soon", sent.text());
        assertEquals(Jobs.Status.DONE, Jobs.find(job.id()).orElseThrow().status());
    }

    @Test
    void aSenderThatFailsLeavesTheJobToRetry() {
        Mail.sender(m -> { throw new Mail.MailException("smtp down"); });
        Jobs.Job job = Mail.to("ada@example.com").subject("x").text("y").later();
        Jobs.runPending();
        Jobs.Job after = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.PENDING, after.status(), "retried later");
        assertTrue(after.error().contains("smtp down"), after.error());
        assertNotNull(after.runAt());
        assertNull(Mail.outbox().last());
    }

    // ==================== helpers ====================

    /** Every base64 body in a MIME message, decoded, in order. */
    private static List<String> base64Bodies(String mime) {
        List<String> out = new ArrayList<>();
        String[] parts = mime.split("Content-Transfer-Encoding: base64\r\n\r\n");
        for (int i = 1; i < parts.length; i++) {
            StringBuilder b64 = new StringBuilder();
            for (String line : parts[i].split("\r\n")) {
                if (line.isEmpty() || line.startsWith("--")) break;
                b64.append(line);
            }
            out.add(new String(Base64.getDecoder().decode(b64.toString()), StandardCharsets.UTF_8));
        }
        return out;
    }

    /** An SMTP server that records the conversation: EHLO, AUTH PLAIN, MAIL, RCPT, DATA, QUIT. */
    static final class FakeSmtp implements AutoCloseable {

        private final ServerSocket server;
        private final String user;
        private final String password;
        private final List<String> commands = new CopyOnWriteArrayList<>();
        private volatile String data = "";

        FakeSmtp(String user, String password) throws IOException {
            this.user = user;
            this.password = password;
            server = new ServerSocket(0, 10, java.net.InetAddress.getByName("127.0.0.1"));
            Thread t = new Thread(this::serve, "fake-smtp");
            t.setDaemon(true);
            t.start();
        }

        int port() {
            return server.getLocalPort();
        }

        List<String> commands() {
            return List.copyOf(commands);
        }

        String data() {
            return data;
        }

        private void serve() {
            while (!server.isClosed()) {
                try (Socket client = server.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));
                    OutputStream out = client.getOutputStream();
                    write(out, "220 fake ESMTP\r\n");
                    String line;
                    while ((line = in.readLine()) != null) {
                        commands.add(line);
                        String upper = line.toUpperCase();
                        if (upper.startsWith("EHLO")) {
                            write(out, "250-fake\r\n250-AUTH PLAIN LOGIN\r\n250 8BITMIME\r\n");
                        } else if (upper.startsWith("AUTH PLAIN")) {
                            String token = new String(Base64.getDecoder().decode(line.substring(11).trim()), StandardCharsets.UTF_8);
                            write(out, token.equals("\0" + user + "\0" + password) ? "235 ok\r\n" : "535 bad credentials\r\n");
                        } else if (upper.startsWith("MAIL FROM") || upper.startsWith("RCPT TO")) {
                            write(out, "250 ok\r\n");
                        } else if (upper.equals("DATA")) {
                            write(out, "354 go\r\n");
                            StringBuilder body = new StringBuilder();
                            while ((line = in.readLine()) != null && !line.equals(".")) {
                                body.append(line.startsWith("..") ? line.substring(1) : line).append("\r\n");
                            }
                            data = body.toString();
                            write(out, "250 queued\r\n");
                        } else if (upper.equals("QUIT")) {
                            write(out, "221 bye\r\n");
                            break;
                        } else {
                            write(out, "500 what\r\n");
                        }
                    }
                } catch (IOException e) {
                    return;
                }
            }
        }

        private static void write(OutputStream out, String s) throws IOException {
            out.write(s.getBytes(StandardCharsets.US_ASCII));
            out.flush();
        }

        @Override
        public void close() throws IOException {
            server.close();
        }
    }
}
