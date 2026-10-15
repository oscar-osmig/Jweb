package jweb;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code jweb.Storage}: the memory and local stores, keys and types, the
 * serving route with ETags and signed URLs, and the S3 store — Signature
 * Version 4 against the vectors in Amazon's documentation, then a round
 * trip through an in-process bucket that verifies every signature.
 */
class StorageTest {

    @BeforeEach
    void fresh() {
        Storage.reset();
        Auth.secret("a-test-secret-that-is-long-enough-for-hmac");
    }

    @AfterEach
    void done() {
        Storage.reset();
    }

    // ==================== memory ====================

    @Test
    void putGetListAndDeleteInMemory() {
        Storage.Stored a = Storage.put("docs/a.txt", "hello".getBytes(), "text/plain");
        assertEquals("docs/a.txt", a.key());
        assertEquals(5, a.size());
        assertEquals("text/plain", a.contentType());
        assertEquals("a.txt", a.filename());
        assertNotNull(a.etag());
        assertEquals("/files/docs/a.txt", a.url());

        Storage.put("docs/b.png", new byte[]{1, 2, 3}, null);
        assertEquals("image/png", Storage.get("docs/b.png").orElseThrow().contentType(), "the type follows the key");
        Storage.put("other.bin", new byte[]{9}, "");
        assertEquals("application/octet-stream", Storage.get("other.bin").orElseThrow().contentType());

        assertTrue(Storage.exists("docs/a.txt"));
        assertFalse(Storage.exists("docs/missing"));
        assertEquals("hello", new String(Storage.bytes("docs/a.txt").orElseThrow()));
        assertEquals(Optional.empty(), Storage.bytes("docs/missing"));
        try (Storage.Blob blob = Storage.open("docs/a.txt").orElseThrow()) {
            assertEquals(5, blob.info().size());
            assertEquals("hello", new String(blob.bytes()));
        }

        assertEquals(List.of("docs/a.txt", "docs/b.png"), Storage.list("docs/").stream().map(Storage.Stored::key).toList());
        assertEquals(3, Storage.list("").size());
        assertEquals(3, Storage.list(null).size());

        Storage.Stored replaced = Storage.put("docs/a.txt", "hello again".getBytes(), "text/plain");
        assertEquals(11, replaced.size());
        assertNotEquals(a.etag(), replaced.etag());

        assertTrue(Storage.delete("docs/a.txt"));
        assertFalse(Storage.delete("docs/a.txt"));
        assertEquals(2, Storage.list("").size());
    }

    @Test
    void anUploadGetsAGeneratedKeyInItsFolder() throws IOException {
        UploadedFile upload = new UploadedFile(new MockMultipartFile("avatar", "Me Photo.PNG", "image/png", new byte[]{1, 2, 3, 4}));
        Storage.Stored stored = Storage.upload("avatars", upload);
        assertTrue(stored.key().matches("avatars/[0-9a-f]{32}\\.png"), stored.key());
        assertEquals(4, stored.size());
        assertEquals("image/png", stored.contentType());
        assertEquals(4, Storage.bytes(stored.key()).orElseThrow().length);

        Storage.Stored exact = Storage.put("users/42/avatar.png", upload);
        assertEquals("users/42/avatar.png", exact.key());

        assertTrue(Storage.key("/a/b/", "report.final.PDF").matches("a/b/[0-9a-f]{32}\\.pdf"));
        assertTrue(Storage.key("", "noext").matches("[0-9a-f]{32}"));
        assertTrue(Storage.key(null, "weird.ext!").matches("[0-9a-f]{32}"), "an extension that is not one is dropped");
    }

    @Test
    void keysAreCheckedAndTypesKnown() {
        assertEquals("a/b.txt", Storage.checkKey("/a/b.txt"));
        assertEquals("a/b.txt", Storage.checkKey("  a/b.txt "));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey("../etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey("a/../b"));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey("a\\b"));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey("a//b"));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey("a\nb"));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey(""));
        assertThrows(IllegalArgumentException.class, () -> Storage.checkKey(null));
        assertThrows(IllegalArgumentException.class, () -> Storage.put("../x", new byte[0], null));

        assertEquals("image/jpeg", Storage.contentType("photo.JPG"));
        assertEquals("application/pdf", Storage.contentType("a/b/c.pdf"));
        assertEquals("font/woff2", Storage.contentType("x.woff2"));
        assertEquals("application/octet-stream", Storage.contentType("noext"));
        assertEquals("application/octet-stream", Storage.contentType(null));
        assertEquals("", Storage.extension(".hidden"));
        assertEquals("gz", Storage.extension("a.tar.gz"));

        assertEquals("a/b%20c/%C3%A9.txt", Storage.encodePath("a/b c/é.txt"));
        assertEquals("/files/a/b%20c/%C3%A9.txt", Storage.url("a/b c/é.txt"));
        assertEquals("a/b c/é.txt", Storage.keyFromPath("/files/a/b%20c/%C3%A9.txt"));
        assertNull(Storage.keyFromPath("/other/a.txt"));
        assertNull(Storage.keyFromPath("/files/"));
        assertNull(Storage.keyFromPath("/files/../x"));

        Storage.path("media/");
        assertEquals("/media", Storage.path());
        assertEquals("/media/a.txt", Storage.url("a.txt"));
    }

    // ==================== local ====================

    @Test
    void theLocalStoreKeepsFilesUnderItsDirectory(@TempDir Path dir) throws IOException {
        Storage.Local local = new Storage.Local(dir);
        Storage.store(local);
        assertEquals(dir.toAbsolutePath().normalize(), local.root());

        Storage.Stored stored = Storage.put("avatars/ada.png", new byte[]{1, 2, 3}, "image/png");
        assertTrue(Files.exists(dir.resolve("avatars/ada.png")));
        assertEquals(3, stored.size());
        assertEquals("image/png", stored.contentType());
        assertNotNull(stored.modified());
        assertNotNull(stored.etag());
        assertEquals(List.of(), Files.list(dir.resolve("avatars")).filter(p -> p.getFileName().toString().startsWith(".upload-")).toList(),
            "no temp file left behind");

        Path source = dir.resolve("source.txt");
        Files.writeString(source, "from disk");
        Storage.Stored copied = Storage.put("docs/copy.txt", source);
        assertEquals("text/plain", copied.contentType());
        assertEquals("from disk", new String(Storage.bytes("docs/copy.txt").orElseThrow()));

        assertEquals(List.of("avatars/ada.png", "docs/copy.txt", "source.txt"), Storage.list("").stream().map(Storage.Stored::key).toList());
        assertEquals(List.of("avatars/ada.png"), Storage.list("avatars/").stream().map(Storage.Stored::key).toList());
        assertEquals(Optional.empty(), Storage.get("avatars/nobody.png"));

        assertTrue(Storage.delete("avatars/ada.png"));
        assertFalse(Files.exists(dir.resolve("avatars/ada.png")));
        assertFalse(Storage.delete("avatars/ada.png"));

        assertThrows(IllegalArgumentException.class, () -> Storage.put("../outside.txt", new byte[0], null));
        assertEquals(List.of(), new Storage.Local(dir.resolve("never-created")).list(""));
    }

    // ==================== serving ====================

    @Test
    void theRouteServesWithTypeEtagAndNotModified() throws Exception {
        Storage.put("docs/hello.txt", "hello".getBytes(), "text/plain");
        RouteHandler serve = Storage.serve();

        ResponseEntity<?> ok = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/hello.txt").build());
        assertEquals(200, ok.getStatusCode().value());
        assertEquals("text/plain", ok.getHeaders().getFirst("Content-Type"));
        assertEquals("bytes", ok.getHeaders().getFirst("Accept-Ranges"));
        assertEquals("private, max-age=3600", ok.getHeaders().getFirst("Cache-Control"));
        String etag = ok.getHeaders().getETag();
        assertNotNull(etag);
        assertTrue(etag.startsWith("\"") && etag.endsWith("\""), etag);
        assertNotNull(ok.getHeaders().getFirst("Last-Modified"));
        assertEquals("hello", body(ok));

        ResponseEntity<?> same = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/hello.txt").header("If-None-Match", etag).build());
        assertEquals(304, same.getStatusCode().value());
        assertNull(same.getBody());

        ResponseEntity<?> missing = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/nope.txt").build());
        assertEquals(404, missing.getStatusCode().value());
        ResponseEntity<?> elsewhere = (ResponseEntity<?>) serve.handle(MockRequest.get("/other/docs/hello.txt").build());
        assertEquals(404, elsewhere.getStatusCode().value());

        ResponseEntity<?> download = (ResponseEntity<?>) Storage.download(MockRequest.get("/x").build(), "docs/hello.txt", "Hello \"World\".txt");
        assertEquals("attachment; filename=\"Hello World.txt\"; filename*=UTF-8''Hello%20%22World%22.txt",
            download.getHeaders().getFirst("Content-Disposition"));
        ResponseEntity<?> named = (ResponseEntity<?>) Storage.download(null, "docs/hello.txt", null);
        assertTrue(named.getHeaders().getFirst("Content-Disposition").startsWith("attachment; filename=\"hello.txt\""));

        Storage.cacheControl(null);
        ResponseEntity<?> uncached = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/hello.txt").build());
        assertNull(uncached.getHeaders().getFirst("Cache-Control"));
    }

    @Test
    void theLocalStoreServesTheFileItself(@TempDir Path dir) throws Exception {
        Storage.store(new Storage.Local(dir));
        Storage.put("a/b.css", "body{}".getBytes(), null);
        ResponseEntity<?> ok = (ResponseEntity<?>) Storage.serve().handle(MockRequest.get("/files/a/b.css").build());
        assertEquals("text/css", ok.getHeaders().getFirst("Content-Type"));
        assertTrue(ok.getBody() instanceof org.springframework.core.io.FileSystemResource, String.valueOf(ok.getBody()));
        assertEquals("body{}", body(ok));
        assertEquals(6, ((Resource) ok.getBody()).contentLength());
    }

    @Test
    void signedUrlsExpireAndTheSignedModeServesNothingElse() throws Exception {
        Storage.put("private/invoice.pdf", "%PDF".getBytes(), null);
        String signed = Storage.url("private/invoice.pdf", Duration.ofMinutes(10));
        assertTrue(signed.startsWith("/files/private/invoice.pdf?exp="), signed);
        Map<String, String> q = query(signed);
        assertTrue(Long.parseLong(q.get("exp")) > Instant.now().getEpochSecond() + 500);
        assertTrue(Storage.signed("private/invoice.pdf", q.get("exp"), q.get("sig")));
        assertFalse(Storage.signed("private/other.pdf", q.get("exp"), q.get("sig")), "another key");
        assertFalse(Storage.signed("private/invoice.pdf", Long.toString(Long.parseLong(q.get("exp")) + 1), q.get("sig")), "another expiry");
        assertFalse(Storage.signed("private/invoice.pdf", q.get("exp"), q.get("sig") + "x"));
        assertFalse(Storage.signed("private/invoice.pdf", "soon", q.get("sig")));
        assertFalse(Storage.signed("private/invoice.pdf", null, null));
        String expired = Storage.url("private/invoice.pdf", Duration.ofSeconds(-5));
        Map<String, String> old = query(expired);
        assertFalse(Storage.signed("private/invoice.pdf", old.get("exp"), old.get("sig")));

        // public mode: the route serves with or without a signature
        RouteHandler serve = Storage.serve();
        assertEquals(200, status(serve.handle(MockRequest.get("/files/private/invoice.pdf").build())));

        // signed mode: every URL is signed, the route refuses the rest
        Storage.signedOnly(true);
        String auto = Storage.url("private/invoice.pdf");
        assertTrue(auto.contains("?exp=") && auto.contains("&sig="), auto);
        assertEquals(403, status(serve.handle(MockRequest.get("/files/private/invoice.pdf").build())));
        Map<String, String> ok = query(auto);
        assertEquals(200, status(serve.handle(MockRequest.get("/files/private/invoice.pdf")
            .queryParam("exp", ok.get("exp")).queryParam("sig", ok.get("sig")).build())));
        assertEquals(403, status(serve.handle(MockRequest.get("/files/private/invoice.pdf")
            .queryParam("exp", old.get("exp")).queryParam("sig", old.get("sig")).build())));

        Auth.secret("another-deploy-another-secret-entirely");
        assertEquals(403, status(serve.handle(MockRequest.get("/files/private/invoice.pdf")
            .queryParam("exp", ok.get("exp")).queryParam("sig", ok.get("sig")).build())));
    }

    // ==================== S3: the signature ====================

    /** The GET Object example from "Examples: Signature Calculations in AWS Signature Version 4". */
    @Test
    void signatureVersion4MatchesAmazonsExample() {
        Storage.S3 s3 = new Storage.S3("https://s3.amazonaws.com", "us-east-1", "examplebucket",
            "AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", false);
        Instant at = Instant.parse("2013-05-24T00:00:00Z");
        String empty = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        Map<String, String> headers = new TreeMap<>(Map.of(
            "host", "examplebucket.s3.amazonaws.com",
            "range", "bytes=0-9",
            "x-amz-content-sha256", empty,
            "x-amz-date", "20130524T000000Z"));

        String authorization = s3.authorization("GET", "/test.txt", Map.of(), headers, empty, at);
        assertEquals("AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, "
            + "SignedHeaders=host;range;x-amz-content-sha256;x-amz-date, "
            + "Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41", authorization);

        // and the presigned GET from the same page, 24 hours
        String url = s3.presign("GET", "test.txt", Map.of(), Duration.ofSeconds(86400), at);
        assertEquals("https://examplebucket.s3.amazonaws.com/test.txt"
            + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
            + "&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20130524%2Fus-east-1%2Fs3%2Faws4_request"
            + "&X-Amz-Date=20130524T000000Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host"
            + "&X-Amz-Signature=aeeed9bbccd4d02ee5c0109b86d86835f995330da4c265957d157751f604d404", url);
    }

    @Test
    void s3AddressesAndUrls() {
        Storage.S3 aws = new Storage.S3("", "eu-west-1", "pics", "AK", "SK");
        assertEquals("https://pics.s3.eu-west-1.amazonaws.com/a/b%20c.png", aws.objectUrl("a/b c.png"));
        Storage.S3 r2 = new Storage.S3("https://abc.r2.cloudflarestorage.com/", "auto", "pics", "AK", "SK");
        assertEquals("https://abc.r2.cloudflarestorage.com/pics/a.png", r2.objectUrl("a.png"), "a custom endpoint is path-style");
        Storage.S3 virtual = new Storage.S3("https://nyc3.digitaloceanspaces.com", "nyc3", "pics", "AK", "SK", false);
        assertEquals("https://pics.nyc3.digitaloceanspaces.com/a.png", virtual.objectUrl("a.png"));

        Storage.store(aws);
        String presigned = Storage.url("a.png");
        assertTrue(presigned.startsWith("https://pics.s3.eu-west-1.amazonaws.com/a.png?X-Amz-Algorithm=AWS4-HMAC-SHA256"), presigned);
        assertTrue(presigned.contains("X-Amz-Expires=3600"), presigned);
        assertTrue(Storage.url("a.png", Duration.ofMinutes(5)).contains("X-Amz-Expires=300"));
        assertTrue(Storage.url("a.png", Duration.ofDays(30)).contains("X-Amz-Expires=604800"), "capped at a week");
        String upload = Storage.uploadUrl("in/new.png", "image/png", Duration.ofMinutes(5)).orElseThrow();
        assertTrue(upload.startsWith("https://pics.s3.eu-west-1.amazonaws.com/in/new.png?"), upload);

        aws.publicUrl("https://cdn.example.com/");
        assertEquals("https://cdn.example.com/a/b%20c.png", Storage.url("a/b c.png"));
        assertTrue(Storage.url("a.png", Duration.ofMinutes(5)).contains("X-Amz-Signature="), "a ttl still signs");
        Storage.signedOnly(true);
        assertTrue(Storage.url("a.png").contains("X-Amz-Signature="), "signed mode signs even with a public url");
    }

    // ==================== S3: a round trip ====================

    @Test
    void theS3StoreRoundTripsThroughABucketThatChecksEverySignature() throws Exception {
        try (FakeS3 bucket = new FakeS3()) {
            Storage.S3 s3 = new Storage.S3(bucket.url(), "us-east-1", "pics", FakeS3.ACCESS, FakeS3.SECRET);
            Storage.store(s3);

            Storage.Stored stored = Storage.put("avatars/ada 1.png", new byte[]{1, 2, 3, 4, 5}, "image/png");
            assertEquals(5, stored.size());
            assertNotNull(stored.etag());
            assertEquals("image/png", bucket.types.get("avatars/ada 1.png"));
            assertEquals(5, bucket.objects.get("avatars/ada 1.png").length);

            Storage.Stored head = Storage.get("avatars/ada 1.png").orElseThrow();
            assertEquals(5, head.size());
            assertEquals("image/png", head.contentType());
            assertNotNull(head.modified());
            assertEquals(stored.etag(), head.etag());
            assertEquals(Optional.empty(), Storage.get("avatars/nobody.png"));

            try (Storage.Blob blob = Storage.open("avatars/ada 1.png").orElseThrow()) {
                assertEquals(5, blob.info().size());
                assertEquals(5, blob.bytes().length);
            }
            assertEquals(Optional.empty(), Storage.open("avatars/nobody.png"));

            Storage.put("avatars/bob.png", new byte[]{1}, "image/png");
            Storage.put("docs/readme.md", "# hi".getBytes(), "text/markdown");
            List<Storage.Stored> avatars = Storage.list("avatars/");
            assertEquals(List.of("avatars/ada 1.png", "avatars/bob.png"), avatars.stream().map(Storage.Stored::key).toList());
            assertEquals(5, avatars.get(0).size());
            assertEquals(3, Storage.list("").size());

            // served through the app's route too: the bytes stream from the bucket
            ResponseEntity<?> served = (ResponseEntity<?>) Storage.serve().handle(MockRequest.get("/files/avatars/ada%201.png").build());
            assertEquals(200, served.getStatusCode().value());
            assertEquals("image/png", served.getHeaders().getFirst("Content-Type"));
            assertEquals(5, ((Resource) served.getBody()).contentLength());
            assertEquals(5, ((Resource) served.getBody()).getInputStream().readAllBytes().length);

            // a presigned URL fetches without any header
            String url = Storage.url("avatars/ada 1.png", Duration.ofMinutes(5));
            java.net.http.HttpResponse<byte[]> direct = java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(URI.create(url)).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, direct.statusCode(), url);
            assertEquals(5, direct.body().length);

            assertTrue(Storage.delete("avatars/ada 1.png"));
            assertFalse(Storage.delete("avatars/ada 1.png"));
            assertEquals(2, Storage.list("").size());

            bucket.rejectEverything = true;
            Storage.StorageException failed = assertThrows(Storage.StorageException.class,
                () -> Storage.put("x.txt", new byte[0], "text/plain"));
            assertTrue(failed.getMessage().contains("AccessDenied"), failed.getMessage());
            assertEquals(0, bucket.badSignatures, "every request the bucket saw was correctly signed");
        }
    }

    // ==================== helpers ====================

    private static String body(ResponseEntity<?> response) throws IOException {
        Object body = response.getBody();
        if (body instanceof Resource resource) {
            try (var in = resource.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return String.valueOf(body);
    }

    private static int status(Object response) {
        return ((ResponseEntity<?>) response).getStatusCode().value();
    }

    private static Map<String, String> query(String url) {
        Map<String, String> out = new LinkedHashMap<>();
        int q = url.indexOf('?');
        if (q < 0) return out;
        for (String pair : url.substring(q + 1).split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            out.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return out;
    }

    /**
     * A bucket in a map behind {@code com.sun.net.httpserver}, path-style at
     * {@code /pics/<key>}. It recomputes every signature from the request it
     * received — the headers it names, the payload hash, the query — with
     * the same credentials, so a request the real S3 would refuse fails here.
     */
    static final class FakeS3 implements AutoCloseable {
        static final String ACCESS = "AKIAFAKEFAKEFAKEFAKE";
        static final String SECRET = "fake/secret/key/that/is/long/enough/for/a/test";

        final HttpServer server;
        final Map<String, byte[]> objects = new ConcurrentHashMap<>();
        final Map<String, String> types = new ConcurrentHashMap<>();
        final Map<String, String> etags = new ConcurrentHashMap<>();
        final Storage.S3 verifier;
        volatile boolean rejectEverything;
        volatile int badSignatures;

        FakeS3() throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", this::handle);
            server.start();
            verifier = new Storage.S3(url(), "us-east-1", "pics", ACCESS, SECRET);
        }

        String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        private void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                String rawPath = ex.getRequestURI().getRawPath();
                Map<String, String> query = query("?" + (ex.getRequestURI().getRawQuery() == null ? "" : ex.getRequestURI().getRawQuery()));
                byte[] body = ex.getRequestBody().readAllBytes();
                if (!verify(ex, method, rawPath, query, body)) {
                    badSignatures++;
                    reply(ex, 403, "application/xml", error("SignatureDoesNotMatch"));
                    return;
                }
                if (rejectEverything) {
                    reply(ex, 403, "application/xml", error("AccessDenied"));
                    return;
                }
                if (!rawPath.startsWith("/pics")) {
                    reply(ex, 404, "application/xml", error("NoSuchBucket"));
                    return;
                }
                String key = rawPath.length() > 6 ? URLDecoder.decode(rawPath.substring(6), StandardCharsets.UTF_8) : "";
                switch (method) {
                    case "PUT" -> {
                        objects.put(key, body);
                        types.put(key, ex.getRequestHeaders().getFirst("Content-Type"));
                        etags.put(key, Storage.hex(Storage.sha256(body)).substring(0, 32));
                        ex.getResponseHeaders().set("ETag", "\"" + etags.get(key) + "\"");
                        reply(ex, 200, null, new byte[0]);
                    }
                    case "HEAD", "GET" -> {
                        if (key.isEmpty() && "2".equals(query.get("list-type"))) {
                            reply(ex, 200, "application/xml", listing(query.getOrDefault("prefix", "")).getBytes(StandardCharsets.UTF_8));
                            return;
                        }
                        byte[] data = objects.get(key);
                        if (data == null) {
                            if (method.equals("HEAD")) {
                                ex.sendResponseHeaders(404, -1);
                                ex.close();
                            } else {
                                reply(ex, 404, "application/xml", error("NoSuchKey"));
                            }
                            return;
                        }
                        ex.getResponseHeaders().set("Content-Type", types.get(key));
                        ex.getResponseHeaders().set("ETag", "\"" + etags.get(key) + "\"");
                        ex.getResponseHeaders().set("Last-Modified", "Fri, 24 May 2013 00:00:00 GMT");
                        if (method.equals("HEAD")) {
                            ex.getResponseHeaders().set("Content-Length", Integer.toString(data.length));
                            ex.sendResponseHeaders(200, -1);
                            ex.close();
                        } else {
                            reply(ex, 200, null, data);
                        }
                    }
                    case "DELETE" -> {
                        objects.remove(key);
                        ex.sendResponseHeaders(204, -1);
                        ex.close();
                    }
                    default -> reply(ex, 405, "text/plain", "no".getBytes());
                }
            } catch (RuntimeException e) {
                reply(ex, 500, "text/plain", String.valueOf(e).getBytes(StandardCharsets.UTF_8));
            }
        }

        private boolean verify(HttpExchange ex, String method, String rawPath, Map<String, String> query, byte[] body) {
            String authorization = ex.getRequestHeaders().getFirst("Authorization");
            if (authorization != null) {
                String signedHeaders = authorization.substring(authorization.indexOf("SignedHeaders=") + 14, authorization.indexOf(", Signature="));
                Map<String, String> headers = new TreeMap<>();
                for (String name : signedHeaders.split(";")) {
                    headers.put(name, ex.getRequestHeaders().getFirst(name));
                }
                String payloadHash = ex.getRequestHeaders().getFirst("x-amz-content-sha256");
                if (!Storage.hex(Storage.sha256(body)).equals(payloadHash)) return false;
                Instant at = Instant.from(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(java.time.ZoneOffset.UTC).parse(ex.getRequestHeaders().getFirst("x-amz-date")));
                String expected = verifier.authorization(method, rawPath, query, headers, payloadHash, at);
                return expected.equals(authorization);
            }
            if (query.containsKey("X-Amz-Signature")) {
                Map<String, String> extra = new TreeMap<>();
                query.forEach((name, value) -> {
                    if (!name.startsWith("X-Amz-")) extra.put(name, value);
                });
                Instant at = Instant.from(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(java.time.ZoneOffset.UTC).parse(query.get("X-Amz-Date")));
                String key = URLDecoder.decode(rawPath.substring(6), StandardCharsets.UTF_8);
                String expected = verifier.presign(method, key, extra, Duration.ofSeconds(Long.parseLong(query.get("X-Amz-Expires"))), at);
                return query.get("X-Amz-Signature").equals(StorageTest.query(expected).get("X-Amz-Signature"))
                    && Instant.now().isBefore(at.plusSeconds(Long.parseLong(query.get("X-Amz-Expires"))));
            }
            return false;
        }

        private String listing(String prefix) {
            StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListBucketResult><Name>pics</Name><Prefix>")
                .append(prefix).append("</Prefix><IsTruncated>false</IsTruncated>");
            objects.keySet().stream().sorted().filter(k -> k.startsWith(prefix)).forEach(k -> xml.append("<Contents><Key>")
                .append(k.replace("&", "&amp;")).append("</Key><LastModified>2013-05-24T00:00:00.000Z</LastModified><ETag>&quot;")
                .append(etags.get(k)).append("&quot;</ETag><Size>").append(objects.get(k).length).append("</Size></Contents>"));
            return xml.append("</ListBucketResult>").toString();
        }

        private static byte[] error(String code) {
            return ("<?xml version=\"1.0\"?><Error><Code>" + code + "</Code><Message>" + code + " from the fake</Message></Error>")
                .getBytes(StandardCharsets.UTF_8);
        }

        private static void reply(HttpExchange ex, int status, String type, byte[] body) throws IOException {
            if (type != null) ex.getResponseHeaders().set("Content-Type", type);
            ex.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(body);
                }
            }
            ex.close();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
