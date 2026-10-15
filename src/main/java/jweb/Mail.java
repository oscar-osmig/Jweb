package jweb;

import com.osmig.Jweb.framework.util.Log;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Email: a message built like an element, sent now or later.
 *
 * <pre>
 * Mail.to("ada@example.com")
 *     .subject("Welcome")
 *     .html(div(h1("Welcome, Ada"), p("Your account is ready.")))   // the HTML DSL; a text part is derived
 *     .send();
 *
 * Mail.to(user.email()).subject("Your export").attach("export.csv", csv, "text/csv").later();   // a durable job
 * </pre>
 *
 * <p><b>Senders.</b> With {@code jweb.mail.smtp.host} set, mail goes out over
 * SMTP — STARTTLS on 587 by default, implicit TLS on 465, AUTH PLAIN or
 * LOGIN when there is a username — through the framework's own client, so
 * no mail library is needed. Without it, the {@link Outbox} keeps the last
 * two hundred messages in memory and logs each one: what development and
 * tests want, and what {@link #outbox()} reads back. Any {@link Sender}
 * plugs in with {@link #sender(Sender)} — an HTTP API like Resend or
 * Postmark is ten lines.</p>
 *
 * <pre>
 * jweb:
 *   mail:
 *     from: "App &lt;no-reply@example.com&gt;"
 *     smtp:
 *       host: ${SMTP_HOST:}
 *       port: 587
 *       username: ${SMTP_USER:}
 *       password: ${SMTP_PASSWORD:}
 *       tls: starttls      # starttls | implicit | none
 * </pre>
 *
 * <p>{@link Message#later()} enqueues the message as the durable job
 * {@code jweb.mail}: it is sent by the {@link Jobs} worker, retried with
 * backoff when the server is down, and survives a restart on the SQL store.</p>
 */
public final class Mail {

    /** Delivers messages. */
    public interface Sender {
        void send(Message message) throws MailException;
    }

    /** A message that could not be sent. */
    public static class MailException extends RuntimeException {
        public MailException(String message) {
            super(message);
        }

        public MailException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** A file on a message. Bytes travel base64-encoded when the message is a job payload. */
    public record Attachment(String filename, byte[] content, String contentType) {}

    public static final String JOB = "jweb.mail";

    private static final Outbox OUTBOX = new Outbox();
    private static volatile Sender sender = OUTBOX;
    private static volatile String defaultFrom = "no-reply@localhost";

    static {
        Jobs.handle(JOB, Envelope.class, envelope -> send(envelope.toMessage()));
    }

    /** A message as a job payload: the same fields, as a record JSON round-trips. */
    public record Envelope(String from, List<String> to, List<String> cc, List<String> bcc, String replyTo,
                           String subject, String text, String html, List<Attachment> attachments,
                           Map<String, String> headers) {

        public Message toMessage() {
            Message m = new Message();
            m.from = from;
            if (to != null) m.to.addAll(to);
            if (cc != null) m.cc.addAll(cc);
            if (bcc != null) m.bcc.addAll(bcc);
            m.replyTo = replyTo;
            m.subject = subject == null ? "" : subject;
            m.text = text;
            m.html = html;
            if (attachments != null) m.attachments.addAll(attachments);
            if (headers != null) m.headers.putAll(headers);
            return m;
        }
    }

    private Mail() {}

    // ==================== building ====================

    /** Starts a message to one or more recipients. */
    public static Message to(String... recipients) {
        Message m = new Message();
        m.to.addAll(List.of(recipients));
        return m;
    }

    /** A message with nothing set yet. */
    public static Message message() {
        return new Message();
    }

    /** The message, as the builder and the value: every field readable, {@link #send()} and {@link #later()} on it. */
    public static final class Message {

        private String from;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String replyTo;
        private String subject = "";
        private String text;
        private String html;
        private final List<Attachment> attachments = new ArrayList<>();
        private final Map<String, String> headers = new LinkedHashMap<>();

        public Message() {}

        public Message from(String from) {
            this.from = from;
            return this;
        }

        public Message to(String... recipients) {
            to.addAll(List.of(recipients));
            return this;
        }

        public Message cc(String... recipients) {
            cc.addAll(List.of(recipients));
            return this;
        }

        public Message bcc(String... recipients) {
            bcc.addAll(List.of(recipients));
            return this;
        }

        public Message replyTo(String address) {
            this.replyTo = address;
            return this;
        }

        public Message subject(String subject) {
            this.subject = subject == null ? "" : subject;
            return this;
        }

        /** The plain-text part. Without one, it is derived from the HTML. */
        public Message text(String text) {
            this.text = text;
            return this;
        }

        /** The HTML part, as a string. */
        public Message html(String html) {
            this.html = html;
            return this;
        }

        /** The HTML part from the DSL — the body you would put in a page. */
        public Message html(Element body) {
            this.html = body.toHtml();
            return this;
        }

        public Message attach(String filename, byte[] content, String contentType) {
            attachments.add(new Attachment(filename, content, contentType));
            return this;
        }

        public Message header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        // ---- reading

        /** The sender, or the configured default. */
        public String from() {
            return from == null || from.isBlank() ? defaultFrom : from;
        }

        public List<String> to() {
            return Collections.unmodifiableList(to);
        }

        public List<String> cc() {
            return Collections.unmodifiableList(cc);
        }

        public List<String> bcc() {
            return Collections.unmodifiableList(bcc);
        }

        public String replyTo() {
            return replyTo;
        }

        public String subject() {
            return subject;
        }

        /** The text part — given, or derived from the HTML. */
        public String text() {
            if (text != null) return text;
            return html == null ? "" : textOf(html);
        }

        public String html() {
            return html;
        }

        public List<Attachment> attachments() {
            return Collections.unmodifiableList(attachments);
        }

        public Map<String, String> headers() {
            return Collections.unmodifiableMap(headers);
        }

        /** Everyone who gets a copy. */
        public List<String> recipients() {
            List<String> all = new ArrayList<>(to);
            all.addAll(cc);
            all.addAll(bcc);
            return all;
        }

        // ---- sending

        /** Sends through the installed sender, now, on this thread. */
        public void send() {
            Mail.send(this);
        }

        /** Enqueues the message as the {@code jweb.mail} job: sent by the worker, retried, durable. */
        public Jobs.Job later() {
            validate();
            return Jobs.enqueue(JOB, envelope());
        }

        /** The message as the record a job carries. */
        public Envelope envelope() {
            return new Envelope(from, List.copyOf(to), List.copyOf(cc), List.copyOf(bcc), replyTo, subject, text, html,
                List.copyOf(attachments), Map.copyOf(headers));
        }

        void validate() {
            if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) throw new MailException("The message has no recipient");
            if (subject.isBlank() && text == null && html == null) throw new MailException("The message has no subject and no body");
        }

        @Override
        public String toString() {
            return "Mail[to=" + to + ", subject=" + subject + "]";
        }
    }

    // ==================== sending ====================

    /** Sends through the installed sender. */
    public static void send(Message message) {
        message.validate();
        long started = System.nanoTime();
        try (Trace.Span span = Trace.span("jweb.mail.send")) {
            span.tag("mail.recipients", message.to().size() + message.cc().size() + message.bcc().size());
            try {
                sender.send(message);
                Metrics.counter("jweb.mail.sent").increment();
            } catch (MailException e) {
                span.error(e);
                Metrics.counter("jweb.mail.failed").increment();
                throw e;
            }
        } finally {
            Metrics.timer("jweb.mail.send").record((System.nanoTime() - started) / 1_000_000);
        }
    }

    /** Installs the sender. */
    public static void sender(Sender next) {
        sender = next;
    }

    public static Sender sender() {
        return sender;
    }

    /** The {@code From} used when a message sets none. */
    public static void defaultFrom(String from) {
        if (from != null && !from.isBlank()) defaultFrom = from;
    }

    public static String defaultFrom() {
        return defaultFrom;
    }

    /** The outbox that collects messages when no real sender is installed — read it in a test. */
    public static Outbox outbox() {
        return OUTBOX;
    }

    // ==================== the outbox ====================

    /** Keeps the last messages in memory and logs each — the sender for development and tests. */
    public static final class Outbox implements Sender {

        private static final int KEEP = 200;
        private final List<Message> messages = new CopyOnWriteArrayList<>();

        @Override
        public void send(Message message) {
            messages.add(message);
            while (messages.size() > KEEP) messages.remove(0);
            Log.info("Mail (outbox, not sent): to {} — {}", message.to(), message.subject());
        }

        /** The messages, oldest first. */
        public List<Message> messages() {
            return List.copyOf(messages);
        }

        /** The most recent message, or {@code null}. */
        public Message last() {
            return messages.isEmpty() ? null : messages.get(messages.size() - 1);
        }

        public int size() {
            return messages.size();
        }

        public void clear() {
            messages.clear();
        }

        @Override
        public String toString() {
            return "Mail.Outbox";
        }
    }

    // ==================== SMTP ====================

    /** How the connection is secured. */
    public enum Tls { STARTTLS, IMPLICIT, NONE }

    /**
     * Mail over SMTP, spoken by the framework: EHLO, STARTTLS or implicit
     * TLS, AUTH PLAIN or LOGIN, one connection per message.
     */
    public static final class Smtp implements Sender {

        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final Tls tls;
        private final int timeoutMs;

        public Smtp(String host, int port, String username, String password, Tls tls) {
            this(host, port, username, password, tls, 15_000);
        }

        public Smtp(String host, int port, String username, String password, Tls tls, int timeoutMs) {
            this.host = host;
            this.port = port;
            this.username = username == null || username.isBlank() ? null : username;
            this.password = password;
            this.tls = tls == null ? Tls.STARTTLS : tls;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public void send(Message message) {
            try (Session session = new Session()) {
                session.deliver(message);
            } catch (IOException e) {
                throw new MailException("SMTP " + host + ":" + port + ": " + e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return "Mail.Smtp(" + host + ":" + port + ", " + tls + (username == null ? "" : ", as " + username) + ")";
        }

        /** One SMTP conversation. */
        private final class Session implements AutoCloseable {

            private Socket socket;
            private BufferedReader in;
            private OutputStream out;

            Session() throws IOException {
                Socket plain = new Socket();
                plain.connect(new InetSocketAddress(host, port), timeoutMs);
                plain.setSoTimeout(timeoutMs);
                socket = tls == Tls.IMPLICIT ? upgrade(plain) : plain;
                open();
            }

            private void open() throws IOException {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
                out = socket.getOutputStream();
            }

            private Socket upgrade(Socket plain) throws IOException {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket ssl = (SSLSocket) factory.createSocket(plain, host, port, true);
                ssl.setUseClientMode(true);
                ssl.startHandshake();
                return ssl;
            }

            void deliver(Message message) throws IOException {
                expect(220, null);
                String me = "localhost";
                List<String> extensions = ehlo(me);
                if (tls == Tls.STARTTLS) {
                    if (extensions.stream().noneMatch(e -> e.toUpperCase(Locale.ROOT).startsWith("STARTTLS"))) {
                        throw new IOException("the server does not offer STARTTLS; set jweb.mail.smtp.tls to implicit or none");
                    }
                    send("STARTTLS");
                    expect(220, "STARTTLS");
                    socket = upgrade(socket);
                    open();
                    extensions = ehlo(me);
                }
                if (username != null) {
                    authenticate(extensions);
                }
                send("MAIL FROM:<" + address(message.from()) + ">");
                expect(250, "MAIL FROM");
                for (String recipient : message.recipients()) {
                    send("RCPT TO:<" + address(recipient) + ">");
                    expect(250, "RCPT TO " + recipient);
                }
                send("DATA");
                expect(354, "DATA");
                String mime = Mime.of(message);
                out.write(dotStuff(mime).getBytes(StandardCharsets.UTF_8));
                out.write("\r\n.\r\n".getBytes(StandardCharsets.US_ASCII));
                out.flush();
                expect(250, "message body");
                send("QUIT");
                try {
                    expect(221, "QUIT");
                } catch (IOException ignored) {
                    // some servers close without a goodbye
                }
            }

            private List<String> ehlo(String me) throws IOException {
                send("EHLO " + me);
                return expect(250, "EHLO");
            }

            private void authenticate(List<String> extensions) throws IOException {
                String auth = extensions.stream()
                    .map(e -> e.toUpperCase(Locale.ROOT))
                    .filter(e -> e.startsWith("AUTH"))
                    .findFirst().orElse("AUTH PLAIN LOGIN");
                if (auth.contains("PLAIN")) {
                    String token = Base64.getEncoder().encodeToString(("\0" + username + "\0" + password).getBytes(StandardCharsets.UTF_8));
                    send("AUTH PLAIN " + token);
                    expect(235, "AUTH PLAIN");
                } else if (auth.contains("LOGIN")) {
                    send("AUTH LOGIN");
                    expect(334, "AUTH LOGIN");
                    send(Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
                    expect(334, "AUTH LOGIN username");
                    send(Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
                    expect(235, "AUTH LOGIN password");
                } else {
                    throw new IOException("the server offers neither AUTH PLAIN nor AUTH LOGIN (" + auth + ")");
                }
            }

            private void send(String line) throws IOException {
                out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            /** Reads one (possibly multi-line) reply; returns its lines without the code; fails on another code. */
            private List<String> expect(int code, String step) throws IOException {
                List<String> lines = new ArrayList<>();
                while (true) {
                    String line = in.readLine();
                    if (line == null) throw new IOException("the server closed the connection" + (step == null ? "" : " at " + step));
                    if (line.length() < 3) throw new IOException("malformed reply: " + line);
                    int got = Integer.parseInt(line.substring(0, 3));
                    boolean last = line.length() == 3 || line.charAt(3) != '-';
                    if (got != code) {
                        throw new IOException((step == null ? "greeting" : step) + " refused: " + line);
                    }
                    lines.add(line.length() > 4 ? line.substring(4) : "");
                    if (last) return lines;
                }
            }

            @Override
            public void close() {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // closing anyway
                }
            }
        }
    }

    // ==================== MIME ====================

    /** The message as the bytes on the wire: headers, text and HTML alternatives, attachments. */
    public static final class Mime {

        private Mime() {}

        public static String of(Message message) {
            StringBuilder sb = new StringBuilder();
            header(sb, "From", message.from());
            header(sb, "To", String.join(", ", message.to()));
            if (!message.cc().isEmpty()) header(sb, "Cc", String.join(", ", message.cc()));
            if (message.replyTo() != null) header(sb, "Reply-To", message.replyTo());
            header(sb, "Subject", encodeWord(message.subject()));
            header(sb, "Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            header(sb, "Message-ID", "<" + randomId() + "@" + domainOf(message.from()) + ">");
            header(sb, "MIME-Version", "1.0");
            for (var h : message.headers().entrySet()) header(sb, h.getKey(), h.getValue());

            String body = alternative(message);
            if (message.attachments().isEmpty()) {
                sb.append(body);
            } else {
                String boundary = "mixed-" + randomId();
                header(sb, "Content-Type", "multipart/mixed; boundary=\"" + boundary + "\"");
                sb.append("\r\n--").append(boundary).append("\r\n");
                sb.append(body);
                for (Attachment a : message.attachments()) {
                    sb.append("\r\n--").append(boundary).append("\r\n");
                    header(sb, "Content-Type", (a.contentType() == null ? "application/octet-stream" : a.contentType())
                        + "; name=\"" + a.filename() + "\"");
                    header(sb, "Content-Disposition", "attachment; filename=\"" + a.filename() + "\"");
                    header(sb, "Content-Transfer-Encoding", "base64");
                    sb.append("\r\n").append(base64Lines(a.content()));
                }
                sb.append("\r\n--").append(boundary).append("--\r\n");
            }
            return sb.toString();
        }

        /** text/plain, text/html, or both as multipart/alternative — headers included, no leading blank line. */
        private static String alternative(Message message) {
            String text = message.text();
            String html = message.html();
            if (html == null) {
                return part("text/plain; charset=UTF-8", text);
            }
            String boundary = "alt-" + randomId();
            StringBuilder sb = new StringBuilder();
            header(sb, "Content-Type", "multipart/alternative; boundary=\"" + boundary + "\"");
            sb.append("\r\n--").append(boundary).append("\r\n");
            sb.append(part("text/plain; charset=UTF-8", text));
            sb.append("\r\n--").append(boundary).append("\r\n");
            sb.append(part("text/html; charset=UTF-8", html));
            sb.append("\r\n--").append(boundary).append("--\r\n");
            return sb.toString();
        }

        private static String part(String contentType, String content) {
            StringBuilder sb = new StringBuilder();
            header(sb, "Content-Type", contentType);
            header(sb, "Content-Transfer-Encoding", "base64");
            sb.append("\r\n").append(base64Lines(content.getBytes(StandardCharsets.UTF_8)));
            return sb.toString();
        }

        private static void header(StringBuilder sb, String name, String value) {
            sb.append(name).append(": ").append(value.replace("\r", "").replace("\n", " ")).append("\r\n");
        }

        /** Base64 in 76-character lines, as MIME wants. */
        static String base64Lines(byte[] bytes) {
            String encoded = Base64.getEncoder().encodeToString(bytes);
            StringBuilder sb = new StringBuilder(encoded.length() + encoded.length() / 76 * 2 + 2);
            for (int i = 0; i < encoded.length(); i += 76) {
                sb.append(encoded, i, Math.min(encoded.length(), i + 76)).append("\r\n");
            }
            return sb.toString();
        }

        /** RFC 2047 for a header that is not plain ASCII. */
        static String encodeWord(String value) {
            boolean ascii = value.chars().allMatch(c -> c >= 32 && c < 127);
            if (ascii) return value;
            return "=?UTF-8?B?" + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)) + "?=";
        }

        private static String randomId() {
            byte[] bytes = new byte[12];
            new SecureRandom().nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        private static String domainOf(String from) {
            String address = address(from);
            int at = address.indexOf('@');
            return at < 0 ? "localhost" : address.substring(at + 1);
        }
    }

    // ==================== helpers ====================

    /** {@code "Ada <ada@example.com>"} → {@code ada@example.com}. */
    static String address(String mailbox) {
        int open = mailbox.indexOf('<');
        int close = mailbox.indexOf('>');
        if (open >= 0 && close > open) return mailbox.substring(open + 1, close).trim();
        return mailbox.trim();
    }

    /** SMTP: a line starting with a dot gets another, so the lone dot still ends the message. */
    static String dotStuff(String data) {
        return data.replace("\r\n.", "\r\n..");
    }

    /** A plain-text rendering of HTML: block tags become line breaks, the rest is stripped, entities decoded. */
    static String textOf(String html) {
        String s = html;
        s = s.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "");
        s = s.replaceAll("(?i)<br\\s*/?>", "\n");
        s = s.replaceAll("(?i)</(p|div|h[1-6]|li|tr|section|article|header|footer|blockquote|pre)>", "\n");
        s = s.replaceAll("(?i)<li[^>]*>", "- ");
        s = s.replaceAll("(?is)<a\\s[^>]*href=\"([^\"]*)\"[^>]*>(.*?)</a>", "$2 ($1)");
        s = s.replaceAll("<[^>]+>", "");
        s = s.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#39;", "'").replace("&#x27;", "'");
        s = s.replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n{3,}", "\n\n");
        return s.strip();
    }
}
