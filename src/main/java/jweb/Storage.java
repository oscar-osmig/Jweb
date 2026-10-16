package jweb;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Where an app keeps the files its users give it: a key, some bytes, a
 * content type — on this machine in development, in a bucket in production,
 * with the same calls.
 *
 * <pre>{@code
 * // An upload, stored under a generated key in a folder
 * UploadedFile file = FileUpload.getFile(req, "avatar");
 * Storage.Stored avatar = Storage.upload("avatars", file);   // avatars/3f2a….png
 * user.avatarKey = avatar.key();
 *
 * // Shown on a page: the URL the browser fetches it from
 * img().src(Storage.url(user.avatarKey))
 *
 * // A private document: a link that stops working after ten minutes
 * a("Download").href(Storage.url("invoices/" + id + ".pdf", Duration.ofMinutes(10)))
 *
 * // Any bytes, any key
 * Storage.put("exports/" + id + ".csv", csv, "text/csv");
 * Optional<byte[]> bytes = Storage.bytes("exports/" + id + ".csv");
 * Storage.delete("exports/" + id + ".csv");
 * }</pre>
 *
 * <p>Three stores ship: {@link Memory} (tests), {@link Local} (a directory,
 * the default — {@code jweb.storage.dir}, {@code storage/} by default) and
 * {@link S3} (Amazon S3 and everything that speaks its API: Cloudflare R2,
 * MinIO, Backblaze B2, DigitalOcean Spaces, Google Cloud Storage in
 * interoperability mode — {@code jweb.storage.s3.bucket} and friends). The
 * S3 store signs its own requests (Signature Version 4) over the JDK's HTTP
 * client; there is no SDK to add. Any {@link Store} bean replaces the lot.</p>
 *
 * <p>The app serves the local store's files itself at {@code jweb.storage.path}
 * ({@code /files} by default): {@code Storage.url(key)} is
 * {@code /files/<key>}, the route is mounted at startup, and it answers with
 * the content type, an ETag (304 on a match), a {@code Cache-Control} and
 * byte ranges. With {@code jweb.storage.access: signed} every URL carries an
 * expiry and a signature under {@code jweb.auth.secret} and the route serves
 * nothing without one — the private-bucket behaviour, on disk. An S3 store
 * hands out presigned URLs instead, or plain ones under
 * {@code jweb.storage.s3.public-url} when the bucket is public.</p>
 */
public final class Storage {

    /** Where the app serves files by default. */
    public static final String DEFAULT_PATH = "/files";

    private static volatile Store store = new Memory();
    private static volatile String path = DEFAULT_PATH;
    private static volatile boolean signedOnly;
    private static volatile Duration urlTtl = Duration.ofHours(1);
    private static volatile String cacheControl = "private, max-age=3600";

    private Storage() {}

    // ==================== configuration ====================

    /** The store every static call goes to. */
    public static void store(Store newStore) {
        store = Objects.requireNonNull(newStore, "store");
    }

    public static Store store() {
        return store;
    }

    /** The URL prefix the app serves files at ({@code /files}). */
    public static void path(String prefix) {
        String p = prefix == null ? "" : prefix.trim();
        if (!p.startsWith("/")) p = "/" + p;
        while (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        path = p;
    }

    public static String path() {
        return path;
    }

    /**
     * Whether every URL is signed and the served route refuses an unsigned
     * request ({@code jweb.storage.access: signed}).
     */
    public static void signedOnly(boolean signed) {
        signedOnly = signed;
    }

    public static boolean signedOnly() {
        return signedOnly;
    }

    /** How long a URL from {@link #url(String)} lives when it has to be signed (an hour). */
    public static void urlTtl(Duration ttl) {
        urlTtl = Objects.requireNonNull(ttl, "ttl");
    }

    /** The {@code Cache-Control} the served route sends ({@code private, max-age=3600}). */
    public static void cacheControl(String value) {
        cacheControl = value;
    }

    // ==================== what a stored file is ====================

    /** One stored file: its key and what the store knows about it. */
    public record Stored(String key, long size, String contentType, Instant modified, String etag) {

        /** The last segment of the key. */
        public String filename() {
            int slash = key.lastIndexOf('/');
            return slash < 0 ? key : key.substring(slash + 1);
        }

        /** {@link Storage#url(String)} for this file. */
        public String url() {
            return Storage.url(key);
        }
    }

    /** A stored file opened for reading; close it when done. */
    public record Blob(Stored info, InputStream in) implements AutoCloseable {

        /** Reads the whole file. */
        public byte[] bytes() {
            try (InputStream s = in) {
                return s.readAllBytes();
            } catch (IOException e) {
                throw new StorageException("Could not read " + info.key(), e);
            }
        }

        @Override
        public void close() {
            try {
                in.close();
            } catch (IOException ignored) {
                // closing
            }
        }
    }

    /** What a store does. Keys are {@code /}-separated paths without a leading slash. */
    public interface Store {

        /** Stores {@code length} bytes from {@code in} under {@code key}, replacing what was there. */
        Stored put(String key, InputStream in, long length, String contentType) throws IOException;

        /** The file's metadata, or empty when there is none under the key. */
        Optional<Stored> head(String key) throws IOException;

        /** Opens the file for reading, or empty when there is none. */
        Optional<Blob> open(String key) throws IOException;

        /** Removes the file; whether there was one. */
        boolean delete(String key) throws IOException;

        /** Every file whose key starts with {@code prefix}, by key. */
        List<Stored> list(String prefix) throws IOException;

        /**
         * A URL the browser fetches the file from when the store serves it
         * itself (a bucket). Empty means the app serves it. A null {@code ttl}
         * asks for a plain, unsigned URL, which a private store may refuse by
         * signing anyway.
         */
        default Optional<String> url(String key, Duration ttl) {
            return Optional.empty();
        }

        /** A URL a browser can {@code PUT} the file to directly, when the store supports it. */
        default Optional<String> uploadUrl(String key, String contentType, Duration ttl) {
            return Optional.empty();
        }
    }

    /** Thrown when the store fails: the disk, the network, the bucket. */
    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ==================== storing ====================

    /** Stores the bytes under the key. */
    public static Stored put(String key, byte[] bytes, String contentType) {
        Objects.requireNonNull(bytes, "bytes");
        return put(key, new ByteArrayInputStream(bytes), bytes.length, contentType);
    }

    /** Stores {@code length} bytes from the stream under the key. */
    public static Stored put(String key, InputStream in, long length, String contentType) {
        String k = checkKey(key);
        String type = contentType == null || contentType.isBlank() ? contentType(k) : contentType.trim();
        try {
            return store.put(k, in, length, type);
        } catch (IOException e) {
            throw new StorageException("Could not store " + k, e);
        }
    }

    /** Stores the file on disk under the key; the type follows the key's extension. */
    public static Stored put(String key, Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return put(key, in, Files.size(file), contentType(key));
        } catch (IOException e) {
            throw new StorageException("Could not read " + file, e);
        }
    }

    /**
     * Stores an upload under the exact key. The type is the one the key's
     * extension implies when the framework knows it, else the browser's when
     * it is a well-formed media type, else {@code application/octet-stream} —
     * a {@code Content-Type} header is the uploader's claim, not a fact.
     */
    public static Stored put(String key, UploadedFile upload) {
        Objects.requireNonNull(upload, "upload");
        try (InputStream in = upload.getInputStream()) {
            return put(key, in, upload.getSize(), uploadType(key, upload.getContentType()));
        } catch (IOException e) {
            throw new StorageException("Could not read the upload " + upload.getFilename(), e);
        }
    }

    static String uploadType(String key, String browserType) {
        String known = TYPES.get(extension(key));
        if (known != null) return known;
        if (browserType != null) {
            String t = browserType.trim();
            int semicolon = t.indexOf(';');
            if (semicolon > 0) t = t.substring(0, semicolon).trim();
            if (t.matches("[A-Za-z0-9!#$&^_.+-]+/[A-Za-z0-9!#$&^_.+-]+")) return t.toLowerCase(Locale.ROOT);
        }
        return contentType(key);
    }

    /**
     * Stores an upload in a folder under a generated key that keeps the
     * file's extension: {@code avatars/3f2a9c…b1.png}. Keep the key; the
     * original name is the upload's to record if the app wants it.
     */
    public static Stored upload(String folder, UploadedFile upload) {
        Objects.requireNonNull(upload, "upload");
        return put(key(folder, upload.getFilename()), upload);
    }

    /** A fresh key in the folder with {@code filename}'s extension. */
    public static String key(String folder, String filename) {
        String ext = extension(filename);
        String name = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String f = folder == null ? "" : folder.trim();
        while (f.startsWith("/")) f = f.substring(1);
        while (f.endsWith("/")) f = f.substring(0, f.length() - 1);
        return checkKey(f.isEmpty() ? name : f + "/" + name);
    }

    // ==================== reading ====================

    /** The file's metadata, when it exists. */
    public static Optional<Stored> get(String key) {
        try {
            return store.head(checkKey(key));
        } catch (IOException e) {
            throw new StorageException("Could not read " + key, e);
        }
    }

    public static boolean exists(String key) {
        return get(key).isPresent();
    }

    /** The whole file, when it exists. */
    public static Optional<byte[]> bytes(String key) {
        return open(key).map(Blob::bytes);
    }

    /** The file opened for reading, when it exists. Close the blob. */
    public static Optional<Blob> open(String key) {
        try {
            return store.open(checkKey(key));
        } catch (IOException e) {
            throw new StorageException("Could not open " + key, e);
        }
    }

    /** Removes the file; whether there was one. */
    public static boolean delete(String key) {
        try {
            return store.delete(checkKey(key));
        } catch (IOException e) {
            throw new StorageException("Could not delete " + key, e);
        }
    }

    /** Every file under the prefix ({@code "avatars/"}, or {@code ""} for all), by key. */
    public static List<Stored> list(String prefix) {
        try {
            return store.list(prefix == null ? "" : prefix.replaceFirst("^/+", ""));
        } catch (IOException e) {
            throw new StorageException("Could not list " + prefix, e);
        }
    }

    // ==================== urls ====================

    /**
     * The URL a browser fetches the file from. The app's own route for the
     * local store ({@code /files/<key>}, signed when {@code jweb.storage.access}
     * is {@code signed}); the bucket's URL for S3 (presigned for an hour, or
     * plain under a configured public URL).
     */
    public static String url(String key) {
        String k = checkKey(key);
        if (signedOnly) return url(k, urlTtl);
        Optional<String> own = store.url(k, null);
        return own.orElseGet(() -> path + "/" + encodePath(k));
    }

    /** A URL that stops working after {@code ttl}: presigned on S3, signed under the app secret here. */
    public static String url(String key, Duration ttl) {
        String k = checkKey(key);
        Objects.requireNonNull(ttl, "ttl");
        Optional<String> own = store.url(k, ttl);
        if (own.isPresent()) return own.get();
        long exp = Instant.now().plus(ttl).getEpochSecond();
        return path + "/" + encodePath(k) + "?exp=" + exp + "&sig=" + signature(k, exp);
    }

    /** A URL a browser can upload to directly, when the store supports it (S3). */
    public static Optional<String> uploadUrl(String key, String contentType, Duration ttl) {
        return store.uploadUrl(checkKey(key), contentType, ttl);
    }

    private static String signature(String key, long exp) {
        return Auth.hmac("storage:" + key, Long.toString(exp));
    }

    /** Whether {@code exp}/{@code sig} from a signed URL are good for the key, now. */
    static boolean signed(String key, String exp, String sig) {
        if (exp == null || sig == null) return false;
        long at;
        try {
            at = Long.parseLong(exp);
        } catch (NumberFormatException e) {
            return false;
        }
        if (at < Instant.now().getEpochSecond()) return false;
        return MessageDigest.isEqual(signature(key, at).getBytes(StandardCharsets.UTF_8),
            sig.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== serving ====================

    /**
     * The route that serves the store's files under {@link #path()}, mounted
     * at startup unless {@code jweb.storage.serve} is false:
     * {@code app.get(Storage.path() + "/*", Storage.serve())}.
     */
    public static RouteHandler serve() {
        return req -> {
            String key = keyFromPath(req.path());
            if (key == null) return Response.notFound("No such file");
            if (signedOnly && !signed(key, req.query("exp"), req.query("sig"))) {
                return Response.forbidden("This link has expired");
            }
            return response(req, key);
        };
    }

    /** The key a served request names, or null when the path is not under {@link #path()}. */
    static String keyFromPath(String requestPath) {
        if (requestPath == null) return null;
        String prefix = path + "/";
        if (!requestPath.startsWith(prefix)) return null;
        String rest = decodePath(requestPath.substring(prefix.length()));
        if (rest.isEmpty()) return null;
        try {
            return checkKey(rest);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * The HTTP response for a stored file: the content type, an ETag and a
     * 304 when the request already has it, {@code Last-Modified},
     * {@code Cache-Control}, byte ranges. A 404 when there is none.
     */
    public static Object response(Request req, String key) {
        return response(req, key, null);
    }

    /** {@link #response} as a download: {@code Content-Disposition: attachment; filename=…}. */
    public static Object download(Request req, String key, String filename) {
        return response(req, key, filename == null || filename.isBlank() ? key.substring(key.lastIndexOf('/') + 1) : filename);
    }

    private static Object response(Request req, String key, String attachment) {
        String k = checkKey(key);
        Optional<Blob> opened = open(k);
        if (opened.isEmpty()) return Response.notFound("No such file");
        Blob blob = opened.get();
        Stored info = blob.info();
        String etag = info.etag() == null ? null : quote(info.etag());
        if (req != null && notModified(req, etag, info.modified())) {
            // decided here, not left to Spring MVC: its own 304 would never touch (or close) the body
            blob.close();
            ResponseEntity.BodyBuilder notModified = ResponseEntity.status(304);
            if (etag != null) notModified.eTag(etag);
            if (cacheControl != null) notModified.header("Cache-Control", cacheControl);
            return notModified.build();
        }
        String type = info.contentType() == null || info.contentType().isBlank() ? "application/octet-stream" : info.contentType();
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .header("Content-Type", type)
            .header("Accept-Ranges", "bytes")
            .header("X-Content-Type-Options", "nosniff");
        if (isActive(type)) {
            // stored HTML, SVG, XML or script is the uploader's: shown, but in an opaque origin with no
            // scripts, so a file somebody uploaded cannot run with this site's cookies
            builder.header("Content-Security-Policy", "sandbox");
        }
        if (etag != null) builder.eTag(etag);
        if (info.modified() != null) builder.lastModified(info.modified());
        if (cacheControl != null) builder.header("Cache-Control", cacheControl);
        if (attachment != null) {
            builder.header("Content-Disposition", "attachment; filename=\"" + asciiFilename(attachment)
                + "\"; filename*=UTF-8''" + java.net.URLEncoder.encode(attachment, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return builder.body(resource(k, blob));
    }

    /** Whether the request's validators say the client already has this version (RFC 9110 §13.2.2 precedence). */
    private static boolean notModified(Request req, String etag, Instant modified) {
        String match = req.header("If-None-Match");
        if (match != null) {
            return etag != null && (match.trim().equals("*") || match.contains(etag));
        }
        String since = req.header("If-Modified-Since");
        if (since == null || modified == null) return false;
        try {
            Instant at = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(since.trim()));
            return modified.getEpochSecond() <= at.getEpochSecond();
        } catch (RuntimeException badDate) {
            return false;
        }
    }

    /** Types a browser would execute or let script: served sandboxed. */
    static boolean isActive(String contentType) {
        String t = contentType.toLowerCase(Locale.ROOT);
        int semicolon = t.indexOf(';');
        if (semicolon > 0) t = t.substring(0, semicolon).trim();
        return t.equals("text/html") || t.equals("application/xhtml+xml") || t.equals("image/svg+xml")
            || t.equals("text/xml") || t.equals("application/xml") || t.endsWith("+xml")
            || t.equals("text/javascript") || t.equals("application/javascript") || t.equals("application/x-javascript")
            || t.equals("text/ecmascript") || t.equals("application/ecmascript");
    }

    /** The quoted-string form of a filename: no quotes, no backslashes, no control characters, so the header stays one header. */
    static String asciiFilename(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            if (c == '"' || c == '\\' || c < 0x20 || c == 0x7f) continue;
            out.append(c < 0x80 ? c : '_');
        }
        return out.toString();
    }

    private static Resource resource(String key, Blob blob) {
        if (blob.in() instanceof ByteArrayInputStream && blob.info().size() <= Integer.MAX_VALUE) {
            return new ByteArrayResource(blob.bytes());
        }
        if (blob.in() instanceof Local.FileStream fs) {
            blob.close();
            return new FileSystemResource(fs.file());
        }
        return new BlobResource(key, blob);
    }

    /**
     * A stream of known length as a Spring resource, so ranges and lengths
     * work. Spring may ask for the stream more than once — once per region of
     * a multi-range request — and expects a fresh one each time, so the first
     * call hands out the stream already open and later calls open the key again.
     */
    private static final class BlobResource extends AbstractResource {
        private final String key;
        private final Blob blob;
        private boolean handedOut;

        BlobResource(String key, Blob blob) {
            this.key = key;
            this.blob = blob;
        }

        @Override
        public String getDescription() {
            return "stored file " + blob.info().key();
        }

        @Override
        public synchronized InputStream getInputStream() throws IOException {
            if (!handedOut) {
                handedOut = true;
                return blob.in();
            }
            Optional<Blob> again = open(key);
            if (again.isEmpty()) throw new java.io.FileNotFoundException("stored file " + key + " is gone");
            return again.get().in();
        }

        @Override
        public long contentLength() {
            return blob.info().size();
        }

        @Override
        public long lastModified() {
            Instant at = blob.info().modified();
            return at == null ? 0 : at.toEpochMilli();
        }

        @Override
        public boolean isOpen() {
            return false;   // the stream can be obtained again: see getInputStream
        }

        @Override
        public String getFilename() {
            return blob.info().filename();
        }
    }

    // ==================== keys and types ====================

    /** A key the stores accept: no leading slash, no {@code ..}, no backslash, no control characters. */
    public static String checkKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("A storage key is required");
        String k = key.trim();
        while (k.startsWith("/")) k = k.substring(1);
        if (k.isEmpty()) throw new IllegalArgumentException("A storage key is required");
        if (k.contains("\\") || k.contains("//")) {
            throw new IllegalArgumentException("Storage keys use single forward slashes: " + key);
        }
        for (String segment : k.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("Storage keys cannot climb: " + key);
            }
        }
        for (int i = 0; i < k.length(); i++) {
            if (k.charAt(i) < 0x20 || k.charAt(i) == 0x7f) {
                throw new IllegalArgumentException("Storage keys cannot contain control characters");
            }
        }
        return k;
    }

    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        String[][] types = {
            {"png", "image/png"}, {"jpg", "image/jpeg"}, {"jpeg", "image/jpeg"}, {"gif", "image/gif"},
            {"webp", "image/webp"}, {"avif", "image/avif"}, {"svg", "image/svg+xml"}, {"ico", "image/x-icon"},
            {"bmp", "image/bmp"}, {"heic", "image/heic"},
            {"pdf", "application/pdf"}, {"txt", "text/plain"}, {"md", "text/markdown"}, {"csv", "text/csv"},
            {"json", "application/json"}, {"xml", "application/xml"}, {"html", "text/html"}, {"htm", "text/html"},
            {"css", "text/css"}, {"js", "text/javascript"}, {"mjs", "text/javascript"},
            {"mp4", "video/mp4"}, {"webm", "video/webm"}, {"mov", "video/quicktime"},
            {"mp3", "audio/mpeg"}, {"wav", "audio/wav"}, {"ogg", "audio/ogg"}, {"m4a", "audio/mp4"},
            {"zip", "application/zip"}, {"gz", "application/gzip"}, {"tar", "application/x-tar"},
            {"woff", "font/woff"}, {"woff2", "font/woff2"}, {"ttf", "font/ttf"}, {"otf", "font/otf"},
            {"doc", "application/msword"},
            {"docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {"xls", "application/vnd.ms-excel"},
            {"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {"ppt", "application/vnd.ms-powerpoint"},
            {"pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
        };
        for (String[] t : types) TYPES.put(t[0], t[1]);
    }

    /** The content type a key or file name implies, {@code application/octet-stream} when unknown. */
    public static String contentType(String keyOrName) {
        String ext = extension(keyOrName);
        String known = TYPES.get(ext);
        if (known != null) return known;
        String guessed = keyOrName == null ? null : java.net.URLConnection.guessContentTypeFromName(keyOrName);
        return guessed != null ? guessed : "application/octet-stream";
    }

    /** The extension of a name, lower case, without the dot; empty when there is none. */
    public static String extension(String name) {
        if (name == null) return "";
        String last = name.substring(name.lastIndexOf('/') + 1);
        int dot = last.lastIndexOf('.');
        if (dot <= 0 || dot == last.length() - 1) return "";
        String ext = last.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,8}") ? ext : "";
    }

    /** Percent-encodes a key for a URL path, keeping the slashes. */
    static String encodePath(String key) {
        StringBuilder sb = new StringBuilder();
        for (byte b : key.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xff);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.' || c == '~' || c == '/') {
                sb.append(c);
            } else {
                sb.append('%').append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xf, 16)))
                    .append(Character.toUpperCase(Character.forDigit(b & 0xf, 16)));
            }
        }
        return sb.toString();
    }

    private static String decodePath(String s) {
        if (s.indexOf('%') < 0) return s;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && i + 2 < s.length()) {
                try {
                    out.write(Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    i += 2;
                    continue;
                } catch (NumberFormatException ignored) {
                    // literal percent
                }
            }
            byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            out.write(bytes, 0, bytes.length);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static String quote(String etag) {
        String e = etag.trim();
        if (e.startsWith("W/")) return e;
        return e.startsWith("\"") ? e : "\"" + e + "\"";
    }

    static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
        }
        return sb.toString();
    }

    static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ==================== Memory ====================

    /** Files in a map: tests, and a throwaway store. */
    public static final class Memory implements Store {

        private record Entry(byte[] bytes, Stored info) {}

        private final Map<String, Entry> files = new ConcurrentHashMap<>();

        @Override
        public Stored put(String key, InputStream in, long length, String contentType) throws IOException {
            byte[] bytes = in.readAllBytes();
            Stored info = new Stored(key, bytes.length, contentType, Instant.now().with(java.time.temporal.ChronoField.NANO_OF_SECOND, 0),
                hex(sha256(bytes)).substring(0, 32));
            files.put(key, new Entry(bytes, info));
            return info;
        }

        @Override
        public Optional<Stored> head(String key) {
            return Optional.ofNullable(files.get(key)).map(Entry::info);
        }

        @Override
        public Optional<Blob> open(String key) {
            return Optional.ofNullable(files.get(key)).map(e -> new Blob(e.info(), new ByteArrayInputStream(e.bytes())));
        }

        @Override
        public boolean delete(String key) {
            return files.remove(key) != null;
        }

        @Override
        public List<Stored> list(String prefix) {
            return files.values().stream().map(Entry::info)
                .filter(s -> s.key().startsWith(prefix))
                .sorted(Comparator.comparing(Stored::key))
                .toList();
        }

        public void clear() {
            files.clear();
        }

        public int size() {
            return files.size();
        }

        @Override
        public String toString() {
            return "Storage.Memory";
        }
    }

    // ==================== Local ====================

    /** Files in a directory; the key is the path under it. The type follows the key's extension. */
    public static final class Local implements Store {

        /** A file stream that remembers its file, so serving can hand the path to the container. */
        static final class FileStream extends java.io.FilterInputStream {
            private final Path file;

            FileStream(Path file) throws IOException {
                super(Files.newInputStream(file));
                this.file = file;
            }

            Path file() {
                return file;
            }
        }

        private final Path root;

        public Local(Path root) {
            this.root = root.toAbsolutePath().normalize();
        }

        public Local(String root) {
            this(Path.of(root));
        }

        public Path root() {
            return root;
        }

        private Path resolve(String key) {
            Path file = root.resolve(key).normalize();
            if (!file.startsWith(root)) throw new IllegalArgumentException("Key leaves the storage directory: " + key);
            return file;
        }

        @Override
        public Stored put(String key, InputStream in, long length, String contentType) throws IOException {
            Path file = resolve(key);
            Files.createDirectories(file.getParent());
            Path temp = Files.createTempFile(file.getParent(), ".upload-", ".part");
            try {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temp);
            }
            return info(key, file);
        }

        private Stored info(String key, Path file) throws IOException {
            long size = Files.size(file);
            Instant modified = Files.getLastModifiedTime(file).toInstant();
            return new Stored(key, size, contentType(key), modified,
                Long.toHexString(size) + "-" + Long.toHexString(modified.toEpochMilli()));
        }

        @Override
        public Optional<Stored> head(String key) throws IOException {
            Path file = resolve(key);
            return Files.isRegularFile(file) ? Optional.of(info(key, file)) : Optional.empty();
        }

        @Override
        public Optional<Blob> open(String key) throws IOException {
            Path file = resolve(key);
            if (!Files.isRegularFile(file)) return Optional.empty();
            return Optional.of(new Blob(info(key, file), new FileStream(file)));
        }

        @Override
        public boolean delete(String key) throws IOException {
            return Files.deleteIfExists(resolve(key));
        }

        @Override
        public List<Stored> list(String prefix) throws IOException {
            if (!Files.isDirectory(root)) return List.of();
            List<Stored> out = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(root)) {
                for (Path file : (Iterable<Path>) walk::iterator) {
                    if (!Files.isRegularFile(file)) continue;
                    String name = file.getFileName().toString();
                    if (name.startsWith(".upload-") && name.endsWith(".part")) continue;
                    String key = root.relativize(file).toString().replace(java.io.File.separatorChar, '/');
                    if (key.startsWith(prefix)) out.add(info(key, file));
                }
            }
            out.sort(Comparator.comparing(Stored::key));
            return out;
        }

        @Override
        public String toString() {
            return "Storage.Local(" + root + ")";
        }
    }

    // ==================== S3 ====================

    /**
     * A bucket that speaks the S3 API — Amazon S3, Cloudflare R2, MinIO,
     * Backblaze B2, DigitalOcean Spaces, Google Cloud Storage with HMAC keys.
     * Requests are signed with Signature Version 4 by the framework itself
     * over the JDK's HTTP client.
     *
     * <pre>{@code
     * Storage.store(new Storage.S3("https://s3.us-east-1.amazonaws.com", "us-east-1", "my-bucket", key, secret));
     * Storage.store(new Storage.S3("https://<account>.r2.cloudflarestorage.com", "auto", "my-bucket", key, secret));
     * }</pre>
     */
    public static final class S3 implements Store {

        private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
        private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        private final URI base;        // scheme://host[:port]/[bucket]  — where keys append
        private final String host;     // what goes in the Host header
        private final String region;
        private final String bucket;
        private final String accessKey;
        private final String secretKey;
        private String publicUrl;
        private HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        /**
         * @param endpoint the service URL ({@code https://s3.us-east-1.amazonaws.com}, an R2 or MinIO
         *                 address); blank for Amazon's virtual-hosted address of the region
         * @param region   the signing region ({@code us-east-1}; {@code auto} on R2)
         */
        public S3(String endpoint, String region, String bucket, String accessKey, String secretKey) {
            this(endpoint, region, bucket, accessKey, secretKey, endpoint != null && !endpoint.isBlank());
        }

        /** @param pathStyle {@code /bucket/key} on the endpoint host instead of {@code bucket.host/key} */
        public S3(String endpoint, String region, String bucket, String accessKey, String secretKey, boolean pathStyle) {
            this.region = region == null || region.isBlank() ? "us-east-1" : region.trim();
            this.bucket = Objects.requireNonNull(bucket, "bucket").trim();
            this.accessKey = Objects.requireNonNull(accessKey, "accessKey").trim();
            this.secretKey = Objects.requireNonNull(secretKey, "secretKey").trim();
            String ep = endpoint == null || endpoint.isBlank()
                ? "https://" + this.bucket + ".s3." + this.region + ".amazonaws.com"
                : endpoint.trim().replaceAll("/+$", "");
            URI uri = URI.create(ep);
            if (pathStyle) {
                this.base = URI.create(ep + "/" + this.bucket);
            } else if (endpoint == null || endpoint.isBlank()) {
                this.base = uri;
            } else {
                this.base = URI.create(uri.getScheme() + "://" + this.bucket + "." + uri.getAuthority() + (uri.getRawPath() == null ? "" : uri.getRawPath()));
            }
            URI b = this.base;
            // the signed host must be the Host header the JDK client sends, and it leaves out the scheme's default port
            boolean defaultPort = b.getPort() == -1
                || ("https".equalsIgnoreCase(b.getScheme()) && b.getPort() == 443)
                || ("http".equalsIgnoreCase(b.getScheme()) && b.getPort() == 80);
            this.host = b.getHost() + (defaultPort ? "" : ":" + b.getPort());
        }

        /** A public bucket or a CDN in front of it: {@link Storage#url(String)} becomes {@code base/key}. */
        public S3 publicUrl(String base) {
            this.publicUrl = base == null || base.isBlank() ? null : base.trim().replaceAll("/+$", "");
            return this;
        }

        S3 client(HttpClient client) {
            this.client = client;
            return this;
        }

        public String bucket() {
            return bucket;
        }

        public String region() {
            return region;
        }

        /** The URL of a key, unsigned. */
        public String objectUrl(String key) {
            return base + "/" + encodePath(key);
        }

        private String canonicalPath(String key) {
            String basePath = base.getRawPath() == null ? "" : base.getRawPath();
            return basePath + "/" + encodePath(key);
        }

        // ---- the store ----

        @Override
        public Stored put(String key, InputStream in, long length, String contentType) throws IOException {
            byte[] body = in.readAllBytes();
            Map<String, String> headers = new TreeMap<>();
            headers.put("content-type", contentType);
            HttpResponse<String> response = send("PUT", key, Map.of(), headers, body, HttpResponse.BodyHandlers.ofString());
            check(response, "store " + key);
            String etag = response.headers().firstValue("ETag").map(e -> e.replace("\"", "")).orElse(null);
            return new Stored(key, body.length, contentType, Instant.now().with(java.time.temporal.ChronoField.NANO_OF_SECOND, 0), etag);
        }

        @Override
        public Optional<Stored> head(String key) throws IOException {
            HttpResponse<Void> response = send("HEAD", key, Map.of(), Map.of(), null, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 404) return Optional.empty();
            check(response, "read " + key);
            return Optional.of(info(key, response));
        }

        @Override
        public Optional<Blob> open(String key) throws IOException {
            HttpResponse<InputStream> response = send("GET", key, Map.of(), Map.of(), null, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 404) {
                response.body().close();
                return Optional.empty();
            }
            if (response.statusCode() / 100 != 2) {
                String text = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new StorageException("S3 could not read " + key + ": " + response.statusCode() + " " + message(text));
            }
            return Optional.of(new Blob(info(key, response), response.body()));
        }

        private Stored info(String key, HttpResponse<?> response) {
            long size = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            String type = response.headers().firstValue("Content-Type").orElseGet(() -> contentType(key));
            Instant modified = response.headers().firstValue("Last-Modified").map(S3::parseHttpDate).orElse(null);
            String etag = response.headers().firstValue("ETag").map(e -> e.replace("\"", "")).orElse(null);
            return new Stored(key, size, type, modified, etag);
        }

        @Override
        public boolean delete(String key) throws IOException {
            HttpResponse<String> before = send("HEAD", key, Map.of(), Map.of(), null, HttpResponse.BodyHandlers.ofString());
            if (before.statusCode() == 404) return false;
            HttpResponse<String> response = send("DELETE", key, Map.of(), Map.of(), null, HttpResponse.BodyHandlers.ofString());
            check(response, "delete " + key);
            return true;
        }

        @Override
        public List<Stored> list(String prefix) throws IOException {
            List<Stored> out = new ArrayList<>();
            String token = null;
            do {
                Map<String, String> query = new TreeMap<>();
                query.put("list-type", "2");
                query.put("prefix", prefix);
                if (token != null) query.put("continuation-token", token);
                HttpResponse<String> response = send("GET", "", query, Map.of(), null, HttpResponse.BodyHandlers.ofString());
                check(response, "list " + prefix);
                Listing page = parseListing(response.body());
                out.addAll(page.files());
                token = page.next();
            } while (token != null);
            out.sort(Comparator.comparing(Stored::key));
            return out;
        }

        @Override
        public Optional<String> url(String key, Duration ttl) {
            if (ttl == null && publicUrl != null) return Optional.of(publicUrl + "/" + encodePath(key));
            return Optional.of(presign("GET", key, Map.of(), ttl == null ? urlTtl : ttl, Instant.now()));
        }

        @Override
        public Optional<String> uploadUrl(String key, String contentType, Duration ttl) {
            return Optional.of(presign("PUT", key, Map.of(), ttl == null ? urlTtl : ttl, Instant.now()));
        }

        @Override
        public String toString() {
            return "Storage.S3(" + base + ")";
        }

        // ---- requests ----

        private <T> HttpResponse<T> send(String method, String key, Map<String, String> query, Map<String, String> extraHeaders,
                                         byte[] body, HttpResponse.BodyHandler<T> handler) throws IOException {
            Instant now = Instant.now();
            String payloadHash = body == null ? EMPTY_SHA256 : hex(sha256(body));
            Map<String, String> headers = new TreeMap<>(extraHeaders);
            headers.put("host", host);
            headers.put("x-amz-content-sha256", payloadHash);
            headers.put("x-amz-date", AMZ_DATE.format(now));
            String authorization = authorization(method, canonicalPath(key), query, headers, payloadHash, now);

            String url = objectUrl(key) + queryString(query);   // the bucket itself is base + "/"
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5));
            headers.forEach((name, value) -> {
                if (!name.equals("host")) request.header(name, value);
            });
            request.header("Authorization", authorization);
            request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));
            try {
                return client.send(request.build(), handler);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted talking to S3", e);
            }
        }

        private static void check(HttpResponse<?> response, String action) {
            if (response.statusCode() / 100 == 2) return;
            String text = response.body() instanceof String s ? s : "";
            throw new StorageException("S3 could not " + action + ": " + response.statusCode() + " " + message(text));
        }

        private static String message(String xml) {
            String code = between(xml, "<Code>", "</Code>");
            String msg = between(xml, "<Message>", "</Message>");
            return code == null ? (msg == null ? "" : msg) : code + (msg == null ? "" : " — " + msg);
        }

        private static String between(String s, String open, String close) {
            int a = s.indexOf(open);
            if (a < 0) return null;
            int b = s.indexOf(close, a + open.length());
            return b < 0 ? null : s.substring(a + open.length(), b);
        }

        private record Listing(List<Stored> files, String next) {}

        private Listing parseListing(String xml) {
            List<Stored> files = new ArrayList<>();
            int at = 0;
            while (true) {
                int start = xml.indexOf("<Contents>", at);
                if (start < 0) break;
                int end = xml.indexOf("</Contents>", start);
                if (end < 0) break;
                String item = xml.substring(start, end);
                String key = unescapeXml(between(item, "<Key>", "</Key>"));
                long size = Long.parseLong(Optional.ofNullable(between(item, "<Size>", "</Size>")).orElse("0").trim());
                String etag = Optional.ofNullable(between(item, "<ETag>", "</ETag>")).map(e -> unescapeXml(e).replace("\"", "")).orElse(null);
                Instant modified = Optional.ofNullable(between(item, "<LastModified>", "</LastModified>")).map(Instant::parse).orElse(null);
                files.add(new Stored(key, size, contentType(key), modified, etag));
                at = end;
            }
            String next = "true".equals(between(xml, "<IsTruncated>", "</IsTruncated>"))
                ? between(xml, "<NextContinuationToken>", "</NextContinuationToken>") : null;
            return new Listing(files, next == null ? null : unescapeXml(next));
        }

        private static String unescapeXml(String s) {
            if (s == null) return null;
            return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&");
        }

        private static Instant parseHttpDate(String value) {
            try {
                return java.time.ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            } catch (RuntimeException e) {
                return null;
            }
        }

        // ---- Signature Version 4 ----

        /** The {@code Authorization} header for a request whose headers include host, x-amz-date and x-amz-content-sha256. */
        String authorization(String method, String canonicalPath, Map<String, String> query, Map<String, String> headers,
                             String payloadHash, Instant at) {
            TreeMap<String, String> sortedHeaders = new TreeMap<>();
            headers.forEach((name, value) -> sortedHeaders.put(name.toLowerCase(Locale.ROOT), value.trim().replaceAll("\\s+", " ")));
            String signedHeaders = String.join(";", sortedHeaders.keySet());
            String canonical = canonicalRequest(method, canonicalPath, query, sortedHeaders, signedHeaders, payloadHash);
            String scope = scope(at);
            String signature = signature(canonical, scope, at);
            return "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + scope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
        }

        /** A presigned URL: the signature in the query, only {@code host} signed, the payload unsigned. */
        String presign(String method, String key, Map<String, String> extraQuery, Duration ttl, Instant at) {
            TreeMap<String, String> query = new TreeMap<>(extraQuery);
            String scope = scope(at);
            query.put("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
            query.put("X-Amz-Credential", accessKey + "/" + scope);
            query.put("X-Amz-Date", AMZ_DATE.format(at));
            query.put("X-Amz-Expires", Long.toString(Math.max(1, Math.min(ttl.getSeconds(), 604800))));
            query.put("X-Amz-SignedHeaders", "host");
            TreeMap<String, String> headers = new TreeMap<>(Map.of("host", host));
            String canonical = canonicalRequest(method, canonicalPath(key), query, headers, "host", "UNSIGNED-PAYLOAD");
            return objectUrl(key) + queryString(query) + "&X-Amz-Signature=" + signature(canonical, scope, at);
        }

        private String scope(Instant at) {
            return DATE.format(at) + "/" + region + "/s3/aws4_request";
        }

        private String signature(String canonicalRequest, String scope, Instant at) {
            String stringToSign = "AWS4-HMAC-SHA256\n" + AMZ_DATE.format(at) + "\n" + scope + "\n"
                + hex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
            byte[] kDate = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), DATE.format(at));
            byte[] kRegion = hmac(kDate, region);
            byte[] kService = hmac(kRegion, "s3");
            byte[] kSigning = hmac(kService, "aws4_request");
            return hex(hmac(kSigning, stringToSign));
        }

        static String canonicalRequest(String method, String canonicalPath, Map<String, String> query,
                                       TreeMap<String, String> headers, String signedHeaders, String payloadHash) {
            StringBuilder canonicalHeaders = new StringBuilder();
            headers.forEach((name, value) -> canonicalHeaders.append(name).append(':').append(value).append('\n'));
            return method + "\n" + canonicalPath + "\n" + canonicalQuery(query) + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;
        }

        private static String canonicalQuery(Map<String, String> query) {
            TreeMap<String, String> sorted = new TreeMap<>(query);
            StringBuilder sb = new StringBuilder();
            sorted.forEach((name, value) -> {
                if (sb.length() > 0) sb.append('&');
                sb.append(encodeQuery(name)).append('=').append(encodeQuery(value == null ? "" : value));
            });
            return sb.toString();
        }

        private static String queryString(Map<String, String> query) {
            if (query.isEmpty()) return "";
            return "?" + canonicalQuery(query);
        }

        private static String encodeQuery(String s) {
            return encodePath(s).replace("/", "%2F");
        }

        private static byte[] hmac(byte[] key, String data) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key, "HmacSHA256"));
                return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            } catch (java.security.GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    // ==================== for tests ====================

    /** Back to the defaults: an empty memory store, {@code /files}, public access. */
    public static void reset() {
        store = new Memory();
        path = DEFAULT_PATH;
        signedOnly = false;
        urlTtl = Duration.ofHours(1);
        cacheControl = "private, max-age=3600";
    }

    static {
        Log.debug("jweb.Storage ready: {}", store);
    }
}
