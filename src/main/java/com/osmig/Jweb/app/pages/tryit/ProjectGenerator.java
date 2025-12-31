package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.app.pages.tryit.generator.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

/** Generates a complete JWeb starter project as a ZIP file. */
@Service
public class ProjectGenerator {

    private static final String FRAMEWORK_RESOURCE_PATTERN = "classpath:framework-src/**/*";
    private static final String FRAMEWORK_BASE_PATH = "framework-src/";
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

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
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/App.java", AppTemplates.appClass());
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/Routes.java", AppTemplates.routesClass());
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/layout/Layout.java", LayoutTemplates.layout());
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/layout/Nav.java", LayoutTemplates.nav());
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/layout/Footer.java", LayoutTemplates.footer());
        addEntry(zos, "jweb-starter/src/main/java/com/jweb/app/pages/HomePage.java", PageTemplates.homePage());
    }

    private void addEntry(ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void addFrameworkFolder(ZipOutputStream zos) throws IOException {
        Resource[] resources = resolver.getResources(FRAMEWORK_RESOURCE_PATTERN);

        for (Resource resource : resources) {
            if (!resource.isReadable()) continue;

            String uri = resource.getURI().toString();
            int baseIndex = uri.indexOf(FRAMEWORK_BASE_PATH);
            if (baseIndex == -1) continue;

            String relativePath = uri.substring(baseIndex + FRAMEWORK_BASE_PATH.length());
            if (relativePath.isEmpty() || relativePath.endsWith("/")) continue;

            String zipPath = "jweb-starter/src/main/java/com/jweb/framework/" + relativePath;

            try (InputStream is = resource.getInputStream()) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("com.osmig.Jweb.framework", "com.jweb.framework");

                zos.putNextEntry(new ZipEntry(zipPath));
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
    }
}
