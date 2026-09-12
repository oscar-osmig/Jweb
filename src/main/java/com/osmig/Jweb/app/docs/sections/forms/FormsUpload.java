package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsUpload {
    private FormsUpload() {}

    public static Element render() {
        return section(
            h3Title("File Upload"),
            para("Handle file uploads with validation and storage."),
            codeBlock("""
import jweb.FileUpload;
import jweb.UploadedFile;
import java.io.IOException;
import java.nio.file.Path;

app.post("/upload", req -> {
    UploadedFile file = FileUpload.getFile(req, "document");

    if (file.isEmpty()) {
        return Response.badRequest("No file provided");
    }

    try {
        Path saved = file.saveTo(Path.of("uploads"));    // saveTo throws IOException
        return Response.json(Map.of("path", saved.toString()));
    } catch (IOException e) {
        return Response.error(500, "Could not store the upload");
    }
});"""),

            h3Title("Upload Form"),
            codeBlock("""
form(action("/upload"), method("post"), attrs().enctype("multipart/form-data"),

    input(type("file"), name("document")),
    button(type("submit"), "Upload")
)"""),

            h3Title("File Properties"),
            codeBlock("""
import java.io.InputStream;

UploadedFile file = FileUpload.getFile(req, "document");

// Check if file was uploaded
if (file.isEmpty()) { /* no file was uploaded */ }

// File info
String name = file.getFilename();     // "report.pdf"
String type = file.getContentType();  // "application/pdf"
long size = file.getSize();           // bytes

// Get content
byte[] bytes = file.getBytes();
InputStream stream = file.getInputStream();

// Type checks
if (file.isImage()) { /* PNG, JPG, GIF */ }
if (file.hasExtension("pdf", "doc")) { /* document */ }"""),

            h3Title("Validation"),
            codeBlock("""
UploadedFile file = FileUpload.getFile(req, "document");

var validation = FileUpload.validate(file)
    .required()                     // must be present
    .maxSizeMB(10)                 // max 10MB
    .imagesOnly();                 // PNG, JPG, GIF only

Object result = validation.isValid()
    ? Response.ok()
    : Response.badRequest(validation.getFirstError());

// Or with specific extensions
FileUpload.validate(file)
    .required()
    .maxSizeMB(5)
    .allowedExtensions("pdf", "doc", "docx")"""),

            h3Title("Multiple Files"),
            codeBlock("""
import java.nio.file.Path;

// HTML
input(type("file"), name("images"), attrs().multiple().accept("image/*"))

// Handler
List<UploadedFile> images = FileUpload.getFiles(req, "images");

for (UploadedFile image : images) {
    var v = FileUpload.validate(image).maxSizeMB(5).imagesOnly();
    if (v.isValid()) {
        String filename = UUID.randomUUID() + "_" + image.getFilename();
        image.saveTo(Path.of("uploads/gallery"), filename);
    }
}"""),

            h3Title("Save Options"),
            codeBlock("""
import java.nio.file.Path;

UploadedFile file = FileUpload.getFile(req, "document");

// Save to directory (generates a unique name)
file.saveTo(Path.of("uploads"));

// Save with custom name
file.saveTo(Path.of("uploads"), "custom-name.pdf");

// Save preserving the original filename
file.saveWithOriginalName(Path.of("uploads"));"""),

            docTip("Configure max file size in application.yaml: spring.servlet.multipart.max-file-size")
        );
    }
}
