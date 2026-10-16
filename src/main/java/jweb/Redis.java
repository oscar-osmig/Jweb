package jweb;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The little of Redis the framework speaks itself — a URL, a socket, RESP —
 * shared by {@link RedisPubSub} and {@link RedisCache}. No client library.
 */
final class Redis {

    private Redis() {}

    /** How long a command may wait for its reply before the connection counts as dead. */
    static final int REPLY_TIMEOUT_MILLIS = 5_000;

    /**
     * Where a Redis is: parsed from {@code redis://[user[:password]@]host[:port][/db]},
     * {@code rediss://} for TLS.
     */
    record Endpoint(String host, int port, boolean tls, String username, String password, int database) {

        static Endpoint parse(String url) {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme() == null ? "redis" : uri.getScheme().toLowerCase();
            if (!scheme.equals("redis") && !scheme.equals("rediss")) {
                throw new IllegalArgumentException("Redis URL must start with redis:// or rediss://, got " + url);
            }
            String host = uri.getHost() == null ? "localhost" : uri.getHost();
            int port = uri.getPort() < 0 ? 6379 : uri.getPort();
            String user = null, pass = null;
            if (uri.getUserInfo() != null) {
                String info = uri.getUserInfo();
                int colon = info.indexOf(':');
                if (colon < 0) {
                    pass = info;
                } else {
                    user = colon == 0 ? null : info.substring(0, colon);
                    pass = info.substring(colon + 1);
                }
            }
            int database = 0;
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                try {
                    database = Integer.parseInt(path.substring(1));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Redis URL path must be a database number (redis://host/2), got " + url);
                }
                if (database < 0) throw new IllegalArgumentException("Redis database must not be negative, got " + url);
            }
            return new Endpoint(host, port, scheme.equals("rediss"), user, pass == null || pass.isEmpty() ? null : pass, database);
        }

        /** A connected, authenticated connection that answered PING; replies time out after {@link #REPLY_TIMEOUT_MILLIS}. */
        Connection connect() throws IOException {
            return connect(REPLY_TIMEOUT_MILLIS);
        }

        /**
         * A connected, authenticated connection that answered PING, with
         * {@code readTimeoutMillis} as its read timeout afterwards — 0 for a
         * subscriber that legitimately waits forever for the next message.
         */
        Connection connect(int readTimeoutMillis) throws IOException {
            Socket socket = tls ? SSLSocketFactory.getDefault().createSocket() : new Socket();
            try {
                socket.connect(new InetSocketAddress(host, port), 5_000);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(REPLY_TIMEOUT_MILLIS);
                if (socket instanceof SSLSocket ssl) {
                    // without this the JDK accepts any CA-signed certificate for any name
                    SSLParameters parameters = ssl.getSSLParameters();
                    parameters.setEndpointIdentificationAlgorithm("HTTPS");
                    ssl.setSSLParameters(parameters);
                    ssl.startHandshake();
                }
            } catch (IOException | RuntimeException e) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // closing anyway
                }
                throw e;
            }
            Connection connection = new Connection(socket);
            try {
                if (password != null) {
                    if (username != null) {
                        connection.command("AUTH", username, password);
                    } else {
                        connection.command("AUTH", password);
                    }
                    connection.readReply();   // +OK, or -ERR which throws
                }
                if (database != 0) {
                    connection.command("SELECT", Integer.toString(database));
                    connection.readReply();   // +OK, or -ERR which throws
                }
                connection.command("PING");
                Object pong = connection.readReply();
                if (!"PONG".equals(pong)) {
                    throw new IOException("Unexpected PING reply: " + pong);
                }
                socket.setSoTimeout(readTimeoutMillis);
                return connection;
            } catch (IOException | RuntimeException e) {
                connection.close();
                throw e;
            }
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }

    /** One connection speaking RESP: send a command, read a reply. */
    static final class Connection {

        final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedInputStream(socket.getInputStream());
            this.out = new BufferedOutputStream(socket.getOutputStream());
        }

        void command(String... args) throws IOException {
            out.write(('*' + Integer.toString(args.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
            for (String arg : args) {
                byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
                out.write(('$' + Integer.toString(bytes.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
                out.write(bytes);
                out.write('\r');
                out.write('\n');
            }
            out.flush();
        }

        /** Sends the command and reads its reply. */
        Object call(String... args) throws IOException {
            command(args);
            return readReply();
        }

        /** A reply: String (simple or bulk), Long, List (array), or null; an error reply throws. */
        Object readReply() throws IOException {
            int type = in.read();
            if (type < 0) throw new EOFException("Redis closed the connection");
            String line = readLine();
            switch (type) {
                case '+':
                    return line;
                case '-':
                    throw new IOException("Redis error: " + line);
                case ':':
                    return number(line);
                case '$': {
                    int length = (int) number(line);
                    if (length < 0) return null;
                    byte[] bytes = in.readNBytes(length);
                    if (bytes.length < length) throw new EOFException("Redis closed the connection mid-reply");
                    readLine();   // the trailing CRLF
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                case '*': {
                    int count = (int) number(line);
                    if (count < 0) return null;
                    List<Object> items = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) items.add(readReply());
                    return items;
                }
                default:
                    throw new IOException("Unexpected Redis reply type '" + (char) type + "'");
            }
        }

        /** A RESP length or integer line; anything else means the stream is out of step and the connection is done for. */
        private static long number(String line) throws IOException {
            try {
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                throw new IOException("Malformed Redis reply: expected a number, got '" + line + "'");
            }
        }

        private String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c < 0) throw new EOFException("Redis closed the connection");
                if (c == '\r') {
                    int next = in.read();
                    if (next == '\n' || next < 0) return sb.toString();
                    sb.append((char) c).append((char) next);
                } else {
                    sb.append((char) c);
                }
            }
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
    }
}
