package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.app.pages.tryit.generator.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/** Generates a complete JWeb starter project as a ZIP file. */
@Service
public class ProjectGenerator {

    private static final String FRAMEWORK_PATH = "src/main/java/com/osmig/Jweb/framework";

    public byte[] generate() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addProjectFiles(zos);
            addFrameworkFolder(zos);
        }
        return baos.toByteArray();
    }

    private void addProjectFiles(ZipOutputStream zos) throws IOException {
        addEntry(zos, "jweb-starter/pom.xml", PomTemplate.generate());
        addEntry(zos, "jweb-starter/src/main/resources/application.yaml", ConfigTemplates.applicationYaml());
        addEntry(zos, "jweb-starter/README.md", ConfigTemplates.readme());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/App.java", AppTemplates.appClass());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/Routes.java", AppTemplates.routesClass());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Layout.java", LayoutTemplates.layout());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Nav.java", LayoutTemplates.nav());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Footer.java", LayoutTemplates.footer());
        addEntry(zos, "jweb-starter/src/main/java/com/example/app/pages/HomePage.java", PageTemplates.homePage());
    }

    private void addEntry(ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes());
        zos.closeEntry();
    }

    private void addFrameworkFolder(ZipOutputStream zos) throws IOException {
        Path frameworkPath = Paths.get(FRAMEWORK_PATH).toAbsolutePath().normalize();
        if (!Files.exists(frameworkPath)) {
            throw new IOException("Framework folder not found: " + frameworkPath);
        }

        Files.walk(frameworkPath)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    // Get relative path from framework folder itself
                    String relativePath = frameworkPath.relativize(file).toString()
                        .replace("\\", "/");
                    String zipPath = "jweb-starter/src/main/java/com/example/framework/" + relativePath;
                    String content = Files.readString(file)
                        .replace("com.osmig.Jweb.framework", "com.example.framework");

                    zos.putNextEntry(new ZipEntry(zipPath));
                    zos.write(content.getBytes());
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }
}
