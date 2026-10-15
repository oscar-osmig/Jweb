# File Storage

`jweb.Storage` keeps the files an app's users give it — a key, bytes, a content type — in a
directory in development and in a bucket in production, with the same calls. The app serves
the local store's files itself at `/files/<key>`.

## Storing and reading

```java
import jweb.Storage;
import jweb.FileUpload;
import jweb.UploadedFile;

byte[] csv = "a,b\n".getBytes();

// An upload under a generated key: avatars/<32 hex>.png
UploadedFile file = FileUpload.getFile(req, "avatar");
Storage.Stored avatar = Storage.upload("avatars", file);
String key = avatar.key();

// Exact keys
Storage.put("users/42/avatar.png", file);
Storage.put("exports/7.csv", csv, "text/csv");
Storage.put("docs/report.pdf", java.nio.file.Path.of("report.pdf"));

Optional<Storage.Stored> info = Storage.get("exports/7.csv");    // size, contentType, modified, etag
boolean there = Storage.exists("exports/7.csv");
Optional<byte[]> bytes = Storage.bytes("exports/7.csv");
try (Storage.Blob blob = Storage.open("docs/report.pdf").orElseThrow()) {   // streams; close it
    long size = blob.info().size();
    java.io.InputStream in = blob.in();
}
List<Storage.Stored> exports = Storage.list("exports/");
boolean removed = Storage.delete("exports/7.csv");
```

Keys are `/`-separated paths without a leading slash. `..`, `\`, `//` and control characters
are refused with `IllegalArgumentException`. A store failure is `Storage.StorageException`.

## URLs

```java
import jweb.Storage;
import static jweb.El.*;

String key = "avatars/3f2a9c.png";

Element picture = img(src(Storage.url(key)), alt("Avatar"));                  // /files/avatars/3f2a9c.png, or the bucket's URL
Element link = a(href(Storage.url("invoices/7.pdf", Duration.ofMinutes(10))), "Download");   // signed, expires
Optional<String> direct = Storage.uploadUrl("in/new.png", "image/png", Duration.ofMinutes(5));   // a presigned PUT (S3)
```

| Store | `url(key)` | `url(key, ttl)` |
|---|---|---|
| Local, `access: public` | `/files/<key>` | `/files/<key>?exp=…&sig=…` under `jweb.auth.secret` |
| Local, `access: signed` | signed for `jweb.storage.url-ttl` (an hour) | signed for `ttl` |
| S3 | presigned for an hour, or `<public-url>/<key>` when set | presigned for `ttl` (a week at most) |

## Serving

The framework mounts `GET /files/*` at startup for the app-served stores
(`jweb.storage.serve: false` to mount `Storage.serve()` yourself). The route sends the content
type, an ETag (304 on `If-None-Match`), `Last-Modified`, `Cache-Control`
(`jweb.storage.cache-control`) and byte ranges. It is a route: guard it like one. With
`access: signed` it serves nothing without a valid signature.

```java
import jweb.Storage;
import jweb.Auth;

app.guard("/files/private/*", Auth.requireAuth());

// Your own URL for a file: the same response, as a page response or a download
app.get("/avatar/:id", r -> Storage.response(r, "avatars/" + r.param("id") + ".png"));
app.get("/invoice/:id", r -> Storage.download(r, "invoices/" + r.param("id") + ".pdf", "invoice.pdf"));
```

## The stores

```java
import jweb.Storage;

Storage.store(new Storage.Memory());                                   // tests
Storage.store(new Storage.Local("storage"));                           // a directory; the type follows the key's extension
Storage.store(new Storage.S3("", "eu-west-1", "my-bucket", "AKIA…", "secret"));                   // Amazon
Storage.store(new Storage.S3("https://<account>.r2.cloudflarestorage.com", "auto", "my-bucket", "key", "secret"));   // R2
Storage.store(new Storage.S3("http://localhost:9000", "us-east-1", "my-bucket", "minio", "minio123"));   // MinIO
Storage.store(new Storage.S3("", "us-east-1", "my-bucket", "key", "secret").publicUrl("https://cdn.example.com"));
```

`Storage.S3` signs its own requests (Signature Version 4) over the JDK's HTTP client; a custom
endpoint is addressed path-style (`endpoint/bucket/key`), Amazon's virtual-hosted
(`bucket.s3.region.amazonaws.com/key`); `jweb.storage.s3.path-style` overrides. A `PUT`
sends the file in one request, read fully first. Any `Storage.Store` bean replaces the store;
the interface is `put`, `head`, `open`, `delete`, `list`, and optionally `url` and `uploadUrl`.

## Configuration

```yaml
jweb:
  storage:
    dir: storage
    path: /files
    access: public            # or signed
    url-ttl: 1h
    cache-control: "private, max-age=3600"
    serve: true
    s3:
      bucket:                 # set → S3
      region: us-east-1
      endpoint:
      access-key:
      secret-key:
      path-style:             # default: true with an endpoint, false on Amazon
      public-url:
```

## In a test

```java
import jweb.Storage;

Storage.store(new Storage.Memory());
Storage.put("docs/a.txt", "hello".getBytes(), "text/plain");
Object response = Storage.serve().handle(MockRequest.get("/files/docs/a.txt").build());
Storage.reset();   // back to an empty memory store and the defaults
```
