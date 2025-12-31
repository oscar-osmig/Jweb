package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsUpload {
    private FormsUpload() {}

    public static Element render() {
        return section(
            h3Title("File Upload"),
            para("Handle file uploads with validation and storage."),
            codeBlock("""
import com.osmig.Jweb.framework.upload.FileUpload;
import com.osmig.Jweb.framework.upload.UploadedFile;

app.post("/upload", req -> {
    UploadedFile file = FileUpload.getFile(req, "document");

    if (file.isEmpty()) {
        return Response.badRequest("No file provided");
    }

    Path saved = file.saveTo(Path.of("uploads"));
    return Response.json(Map.of("path", saved.toString()));
});"""),

            h3Title("Upload Form"),
            codeBlock("""
form(
    action("/upload"),
    method("post"),
    enctype("multipart/form-data"),

    input(type("file"), name("document")),
    button(type("submit"), "Upload")
)"""),

            h3Title("File Properties"),
            codeBlock("""
UploadedFile file = FileUpload.getFile(req, "document");

// Check if file was uploaded
if (file.isEmpty()) return error("No file");

// File info
String name = file.getOriginalFilename();  // "report.pdf"
String type = file.getContentType();       // "application/pdf"
long size = file.getSize();                // bytes

// Get content
byte[] bytes = file.getBytes();
InputStream stream = file.getInputStream();

// Type checks
if (file.isImage()) { /* PNG, JPG, GIF */ }
if (file.hasExtension("pdf", "doc")) { /* document */ }"""),

            h3Title("Validation"),
            codeBlock("""
var validation = FileUpload.validate(file)
    .required()                     // must be present
    .maxSizeMB(10)                 // max 10MB
    .imagesOnly();                 // PNG, JPG, GIF only

if (!validation.isValid()) {
    return Response.badRequest(validation.getFirstError());
}

// Or with specific extensions
FileUpload.validate(file)
    .required()
    .maxSizeMB(5)
    .allowedExtensions("pdf", "doc", "docx")"""),

            h3Title("Multiple Files"),
            codeBlock("""
// HTML
input(type("file"), name("images"), multiple(), accept("image/*"))

// Handler
List<UploadedFile> images = FileUpload.getFiles(req, "images");

for (UploadedFile image : images) {
    var v = FileUpload.validate(image).maxSizeMB(5).imagesOnly();
    if (v.isValid()) {
        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        image.saveAs(Path.of("uploads/gallery", filename));
    }
}"""),

            h3Title("Save Options"),
            codeBlock("""
// Save to directory (keeps original name)
file.saveTo(Path.of("uploads"));

// Save with custom name
file.saveAs(Path.of("uploads/custom-name.pdf"));

// Save with unique name
String unique = UUID.randomUUID() + "_" + file.getOriginalFilename();
file.saveAs(Path.of("uploads", unique));"""),

            docTip("Configure max file size in application.yaml: spring.servlet.multipart.max-file-size")
        );
    }
}
