# File Upload

JWeb provides utilities for handling file uploads.

## Basic Usage

```java
import com.osmig.Jweb.framework.upload.FileUpload;
import com.osmig.Jweb.framework.upload.UploadedFile;

app.post("/upload", req -> {
    UploadedFile file = FileUpload.getFile(req, "document");

    if (file.isEmpty()) {
        return Response.badRequest("No file provided");
    }

    Path saved = file.saveTo(Path.of("uploads"));
    return Response.json(Map.of("path", saved.toString()));
});
```

## HTML Form

```java
form(
    action("/upload"),
    method("post"),
    enctype("multipart/form-data"),

    input(type("file"), name("document")),
    button(type("submit"), "Upload")
)
```

## Getting Files

### Single File

```java
UploadedFile file = FileUpload.getFile(req, "document");

// Or with Optional
Optional<UploadedFile> file = FileUpload.getFileOptional(req, "document");
```

### Multiple Files

```java
List<UploadedFile> files = FileUpload.getFiles(req, "images");

for (UploadedFile file : files) {
    file.saveTo(Path.of("uploads/images"));
}
```

### All Files

```java
List<UploadedFile> allFiles = FileUpload.getAllFiles(req);
```

## File Properties

```java
UploadedFile file = FileUpload.getFile(req, "document");

// Check if empty
if (file.isEmpty()) {
    return Response.badRequest("No file");
}

// File info
String name = file.getOriginalFilename();  // "report.pdf"
String contentType = file.getContentType(); // "application/pdf"
long size = file.getSize();                 // bytes

// Get content
byte[] bytes = file.getBytes();
InputStream stream = file.getInputStream();
```

## File Type Checks

```java
UploadedFile file = FileUpload.getFile(req, "image");

// Check content type
if (file.isImage()) {
    // PNG, JPG, GIF, etc.
}

// Check extension
if (file.hasExtension("pdf", "doc", "docx")) {
    // Document file
}
```

## Saving Files

```java
// Save to directory (uses original filename)
Path saved = file.saveTo(Path.of("uploads"));
// Result: uploads/original-name.pdf

// Save with custom filename
Path saved = file.saveAs(Path.of("uploads/custom-name.pdf"));

// Save with unique filename
String unique = UUID.randomUUID() + "_" + file.getOriginalFilename();
Path saved = file.saveAs(Path.of("uploads", unique));
```

## Validation

```java
UploadedFile file = FileUpload.getFile(req, "image");

FileUpload.FileValidation validation = FileUpload.validate(file)
    .required()
    .maxSizeMB(10)
    .imagesOnly();

if (!validation.isValid()) {
    return Response.badRequest(validation.getFirstError());
}
```

### Validation Rules

```java
FileUpload.validate(file)
    .required()                              // File must be present
    .maxSize(5 * 1024 * 1024)               // Max 5MB (in bytes)
    .maxSizeMB(10)                           // Max 10MB
    .imagesOnly()                            // PNG, JPG, GIF only
    .allowedExtensions("pdf", "doc", "docx") // Specific extensions
```

### Getting Errors

```java
FileUpload.FileValidation validation = FileUpload.validate(file)
    .required()
    .maxSizeMB(5);

if (!validation.isValid()) {
    List<String> errors = validation.getErrors();
    String firstError = validation.getFirstError();
}
```

## Check Multipart Request

```java
if (FileUpload.isMultipart(req)) {
    // Handle file upload
} else {
    return Response.badRequest("Expected multipart request");
}
```

## Configuration

In `application.properties`:

```properties
# Maximum file size
spring.servlet.multipart.max-file-size=10MB

# Maximum request size (for multiple files)
spring.servlet.multipart.max-request-size=50MB

# Temp directory for uploads
spring.servlet.multipart.location=/tmp/uploads
```

## Complete Example

```java
@Component
public class UploadRoutes implements JWebRoutes {
    private static final Path UPLOAD_DIR = Path.of("uploads");

    @Override
    public void configure(JWeb app) {
        // Upload form
        app.get("/upload", req -> uploadForm(null, null));

        // Handle upload
        app.post("/upload", req -> {
            if (!FileUpload.isMultipart(req)) {
                return uploadForm("Please select a file", null);
            }

            UploadedFile file = FileUpload.getFile(req, "file");

            // Validate
            var validation = FileUpload.validate(file)
                .required()
                .maxSizeMB(10)
                .allowedExtensions("jpg", "png", "gif", "pdf");

            if (!validation.isValid()) {
                return uploadForm(validation.getFirstError(), null);
            }

            // Save with unique name
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path saved = file.saveAs(UPLOAD_DIR.resolve(filename));

            return uploadForm(null, "File uploaded: " + saved.getFileName());
        });

        // Serve uploaded files
        app.get("/uploads/:filename", req -> {
            String filename = req.param("filename");
            Path file = UPLOAD_DIR.resolve(filename);

            if (!Files.exists(file)) {
                return Response.notFound();
            }

            byte[] content = Files.readAllBytes(file);
            String contentType = Files.probeContentType(file);

            return Response.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
        });
    }

    private Element uploadForm(String error, String success) {
        return div(class_("container"),
            h1("File Upload"),

            when(error != null, () -> div(class_("error"), error)),
            when(success != null, () -> div(class_("success"), success)),

            form(
                action("/upload"),
                method("post"),
                enctype("multipart/form-data"),

                div(class_("field"),
                    label(for_("file"), "Select file:"),
                    input(type("file"), name("file"), id("file"))
                ),

                button(type("submit"), "Upload")
            )
        );
    }
}
```

## Multiple File Upload

```java
app.post("/gallery/upload", req -> {
    List<UploadedFile> images = FileUpload.getFiles(req, "images");

    if (images.isEmpty()) {
        return Response.badRequest("No images provided");
    }

    List<String> uploaded = new ArrayList<>();

    for (UploadedFile image : images) {
        // Validate each file
        var validation = FileUpload.validate(image)
            .maxSizeMB(5)
            .imagesOnly();

        if (validation.isValid()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            image.saveAs(Path.of("uploads/gallery", filename));
            uploaded.add(filename);
        }
    }

    return Response.json(Map.of(
        "uploaded", uploaded.size(),
        "files", uploaded
    ));
});
```

```html
<form action="/gallery/upload" method="post" enctype="multipart/form-data">
    <input type="file" name="images" multiple accept="image/*">
    <button type="submit">Upload Images</button>
</form>
```
