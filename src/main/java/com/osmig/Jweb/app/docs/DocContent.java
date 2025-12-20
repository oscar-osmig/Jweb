package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Documentation content sections.
 */
public class DocContent {

    public static Element get(String section) {
        return switch (section) {
            case "setup" -> setup();
            case "routing" -> routing();
            case "templates" -> templates();
            case "styling" -> styling();
            case "state" -> state();
            case "forms" -> forms();
            case "api" -> api();
            default -> intro();
        };
    }

    private static Element intro() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Introduction")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("JWeb lets you build complete web applications entirely in Java. No HTML templates, no CSS files, no JavaScript. Just Java methods that generate your UI with full compile-time safety and IDE support."))
        );
    }

    private static Element setup() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Getting Started")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Add JWeb to your project and create your first route in minutes."))
        );
    }

    private static Element routing() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Routing")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Define routes using a fluent API. Support for GET, POST, and other HTTP methods."))
        );
    }

    private static Element templates() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Templates")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Create reusable components with the Template interface."))
        );
    }

    private static Element styling() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Styling")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Type-safe CSS with a fluent builder API. No more typos in your styles."))
        );
    }

    private static Element state() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("State Management")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Manage application state with reactive updates via WebSocket."))
        );
    }

    private static Element forms() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Forms")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Handle form submissions with type-safe form builders."))
        );
    }

    private static Element api() {
        return section(
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("API Reference")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Complete reference for all JWeb classes and methods."))
        );
    }
}
