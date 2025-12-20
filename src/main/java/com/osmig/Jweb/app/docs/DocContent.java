package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

/**
 * Documentation content sections with detailed explanations.
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
            case "form-builder" -> formBuilder();
            case "layouts" -> layouts();
            case "ui" -> uiComponents();
            case "api" -> api();
            default -> intro();
        };
    }

    // ==================== Intro Section ====================

    private static Element intro() {
        return section(
            sectionTitle("Introduction"),
            paragraph("JWeb is a Java web framework for building complete web applications entirely in Java. Define components, layouts, routes, styles, and state all in one language with full compile-time safety and IDE support."),

            subheading("Why JWeb?"),
            paragraph("Traditional web development requires multiple languages and toolchains. JWeb takes a unified approach:"),

            featureList(
                "Type-Safe Components - Compile-time verification of your entire UI",
                "Fluent Styling API - Define styles with IDE autocomplete and type checking",
                "Component Model - Create reusable UI components with the Template interface",
                "No Build Tools - No webpack, no npm, just Maven and Java",
                "Spring Boot Integration - Seamlessly works with the Spring ecosystem"
            ),

            subheading("Core Philosophy"),
            paragraph("JWeb follows a simple principle: your entire web application is Java. This means:"),

            codeBlock(INTRO_PHILOSOPHY),

            paragraph("Components are Java classes. Styles are fluent builders. Routes are lambdas. Everything compiles together."),

            subheading("How It Works"),
            paragraph("JWeb uses a Virtual DOM architecture:"),

            orderedList(
                "You define components using JWeb's fluent API",
                "JWeb builds a Virtual DOM (VNode tree) from your components",
                "The Virtual DOM renders to the response",
                "Spring Boot serves your application"
            ),

            paragraph("This architecture provides server-side rendering with a component-based development model, all in pure Java.")
        );
    }

    // ==================== Setup Section ====================

    private static Element setup() {
        return section(
            sectionTitle("Getting Started"),
            paragraph("Get up and running with JWeb in minutes. This guide covers installation, project structure, and creating your first route."),

            subheading("Prerequisites"),
            featureList(
                "Java 17 or higher",
                "Maven 3.6+",
                "Your favorite IDE (IntelliJ IDEA, VS Code, Eclipse)"
            ),

            subheading("Adding JWeb to Your Project"),
            paragraph("JWeb is currently in active development. A public release will be available soon on Maven Central. Stay tuned for updates!"),

            subheading("Project Structure"),
            paragraph("A typical JWeb application follows this structure:"),

            codeBlock(SETUP_PROJECT_STRUCTURE),

            subheading("Creating Your First Route"),
            paragraph("Routes are defined in a class implementing JWebRoutes:"),

            codeBlock(SETUP_FIRST_ROUTE),

            subheading("Static Imports"),
            paragraph("For the cleanest syntax, add these static imports to your classes:"),

            codeBlock(SETUP_IMPORTS),

            subheading("Running Your App"),
            paragraph("Start your application like any Spring Boot app:"),

            codeBlock(SETUP_RUN),

            paragraph("Your app will be available at http://localhost:8080")
        );
    }

    // ==================== Routing Section ====================

    private static Element routing() {
        return section(
            sectionTitle("Routing"),
            paragraph("JWeb provides a clean, fluent API for defining routes. Routes map URL paths to handlers that return HTML elements."),

            subheading("Basic Routes"),
            paragraph("Define routes using HTTP method helpers on the JWeb app:"),
            codeBlock(ROUTING_BASIC),

            subheading("Path Parameters"),
            paragraph("Capture dynamic segments using :paramName syntax:"),
            codeBlock(ROUTING_PATH_PARAMS),

            subheading("Query Parameters"),
            paragraph("Access query string parameters with req.query():"),
            codeBlock(ROUTING_QUERY_PARAMS),

            subheading("Request Body"),
            paragraph("For POST/PUT requests, access form data and JSON bodies:"),
            codeBlock(ROUTING_REQUEST_BODY),

            subheading("Route Handlers"),
            paragraph("Routes can return different types:"),
            codeBlock(ROUTING_HANDLERS),

            subheading("Layouts with Routes"),
            paragraph("Wrap pages in layouts for consistent structure:"),
            codeBlock(ROUTING_LAYOUTS),

            subheading("Middleware"),
            paragraph("Add cross-cutting concerns with middleware:"),
            codeBlock(ROUTING_MIDDLEWARE)
        );
    }

    // ==================== Templates Section ====================

    private static Element templates() {
        return section(
            sectionTitle("Templates"),
            paragraph("Templates are the building blocks of JWeb applications. They're Java classes that implement the Template interface and define reusable UI components."),

            subheading("The Template Interface"),
            paragraph("Every component implements Template with a single render() method:"),
            codeBlock(TEMPLATES_INTERFACE),

            subheading("Creating a Component"),
            paragraph("Here's a simple Card component:"),
            codeBlock(TEMPLATES_CARD),

            subheading("Using Components"),
            paragraph("Components are used like any other element:"),
            codeBlock(TEMPLATES_USAGE),

            subheading("Component Composition"),
            paragraph("Components can contain other components:"),
            codeBlock(TEMPLATES_COMPOSITION),

            subheading("Layout Components"),
            paragraph("Layouts wrap pages with common structure like headers and footers:"),
            codeBlock(TEMPLATES_LAYOUT),

            subheading("Conditional Rendering"),
            paragraph("Use when() and ifElse() for conditional content:"),
            codeBlock(TEMPLATES_CONDITIONAL),

            subheading("List Rendering"),
            paragraph("Use each() to render collections:"),
            codeBlock(TEMPLATES_LIST),

            subheading("Component Organization"),
            paragraph("Organize components by their purpose:"),
            codeBlock(TEMPLATES_ORGANIZATION)
        );
    }

    // ==================== Styling Section ====================

    private static Element styling() {
        return section(
            sectionTitle("Styling"),
            paragraph("JWeb provides a comprehensive type-safe styling API. Define styles with IDE autocomplete, compile-time checks, and full refactoring support."),

            subheading("Inline Styles"),
            paragraph("Apply styles directly to elements using the fluent style builder:"),
            codeBlock(STYLING_INLINE),

            subheading("Units"),
            paragraph("All standard units are available as type-safe methods:"),
            codeBlock(STYLING_UNITS),

            subheading("Colors"),
            paragraph("Named colors and color functions:"),
            codeBlock(STYLING_COLORS),

            subheading("Layout Properties"),
            paragraph("Flexbox and Grid layouts:"),
            codeBlock(STYLING_LAYOUT),

            subheading("Common Style Values"),
            paragraph("Pre-defined constants for common values:"),
            codeBlock(STYLING_VALUES),

            subheading("Design Tokens (Theme)"),
            paragraph("Create a Theme class for consistent design:"),
            codeBlock(STYLING_THEME),

            subheading("Transitions & Animations"),
            paragraph("Add smooth transitions:"),
            codeBlock(STYLING_TRANSITIONS),

            subheading("Gradients"),
            paragraph("Create gradient backgrounds:"),
            codeBlock(STYLING_GRADIENTS),

            subheading("Custom Properties"),
            paragraph("Reference custom properties in styles:"),
            codeBlock(STYLING_CUSTOM_PROPS)
        );
    }

    // ==================== State Section ====================

    private static Element state() {
        return section(
            sectionTitle("State Management"),
            paragraph("JWeb provides reactive state management with the useState hook. State changes trigger re-renders and can be synchronized across clients via WebSocket."),

            subheading("Creating State"),
            paragraph("Use useState() to create reactive state containers:"),
            codeBlock(STATE_CREATE),

            subheading("Reading State"),
            paragraph("Use get() to read the current value:"),
            codeBlock(STATE_READ),

            subheading("Updating State"),
            paragraph("Use set() to update the value, or update() for transformations:"),
            codeBlock(STATE_UPDATE),

            subheading("Subscribing to Changes"),
            paragraph("React to state changes with subscribers:"),
            codeBlock(STATE_SUBSCRIBE),

            subheading("State with Forms"),
            paragraph("Bind state to form inputs:"),
            codeBlock(STATE_FORMS),

            subheading("State Patterns"),
            paragraph("Common patterns for managing state:"),
            codeBlock(STATE_PATTERNS),

            subheading("State Serialization"),
            paragraph("State can be serialized to JSON for WebSocket sync:"),
            codeBlock(STATE_SERIALIZATION)
        );
    }

    // ==================== Forms Section ====================

    private static Element forms() {
        return section(
            sectionTitle("Forms"),
            paragraph("JWeb provides fluent APIs for building forms and validating user input with type-safe validators."),

            subheading("Building Forms"),
            paragraph("Use the Elements DSL to create form elements:"),
            codeBlock(FORMS_BASIC),

            subheading("Form Elements"),
            paragraph("All standard HTML form elements are available:"),
            codeBlock(FORMS_ELEMENTS),

            subheading("Form Validation"),
            paragraph("Use FormValidator for server-side validation:"),
            codeBlock(FORMS_VALIDATION),

            subheading("Available Validators"),
            paragraph("Built-in validation rules:"),
            codeBlock(FORMS_VALIDATORS),

            subheading("Custom Validators"),
            paragraph("Create custom validation rules:"),
            codeBlock(FORMS_CUSTOM),

            subheading("Displaying Errors"),
            paragraph("Show validation errors to users:"),
            codeBlock(FORMS_ERRORS),

            subheading("Event Handlers"),
            paragraph("Handle form events with Java callbacks:"),
            codeBlock(FORMS_EVENTS)
        );
    }

    // ==================== Form Builder Section ====================

    private static Element formBuilder() {
        return section(
            sectionTitle("Form Builder"),
            paragraph("The Form Builder provides a fluent DSL for creating forms with minimal boilerplate. Build complete forms with labels, validation, and proper structure in a readable, chainable API."),

            subheading("Basic Usage"),
            paragraph("Import the Form class and use Form.create() to start building:"),
            codeBlock(FORM_BUILDER_BASIC),

            subheading("Field Types"),
            paragraph("The Form Builder supports all common input types:"),
            codeBlock(FORM_BUILDER_TYPES),

            subheading("Field Configuration"),
            paragraph("Each field can be configured with validation and display options:"),
            codeBlock(FORM_BUILDER_CONFIG),

            subheading("Select Dropdowns"),
            paragraph("Create select dropdowns with options:"),
            codeBlock(FORM_BUILDER_SELECT),

            subheading("Radio Groups"),
            paragraph("Create radio button groups for single selection:"),
            codeBlock(FORM_BUILDER_RADIO),

            subheading("Form Buttons"),
            paragraph("Add submit and reset buttons:"),
            codeBlock(FORM_BUILDER_BUTTONS),

            subheading("Complete Example"),
            paragraph("Here's a complete registration form:"),
            codeBlock(FORM_BUILDER_COMPLETE)
        );
    }

    // ==================== Layouts Section ====================

    private static Element layouts() {
        return section(
            sectionTitle("Layout Helper"),
            paragraph("The Layout module provides pre-built layout patterns for common UI structures. Stop writing repetitive flexbox and grid CSS - use semantic layout components instead."),

            subheading("Import"),
            paragraph("Import the Layout class to use layout helpers:"),
            codeBlock(LAYOUTS_IMPORT),

            subheading("Page Structure"),
            paragraph("Create standard page layouts with header, main, and footer:"),
            codeBlock(LAYOUTS_PAGE),

            subheading("Containers"),
            paragraph("Center content with max-width containers:"),
            codeBlock(LAYOUTS_CONTAINERS),

            subheading("Flexbox Layouts"),
            paragraph("Quick flexbox layouts for common patterns:"),
            codeBlock(LAYOUTS_FLEXBOX),

            subheading("Grid Layouts"),
            paragraph("CSS Grid layouts made simple:"),
            codeBlock(LAYOUTS_GRID),

            subheading("Multi-Column Layouts"),
            paragraph("Create column layouts with custom ratios:"),
            codeBlock(LAYOUTS_COLUMNS),

            subheading("Sidebar Layouts"),
            paragraph("Common sidebar patterns:"),
            codeBlock(LAYOUTS_SIDEBAR),

            subheading("Stack and Cluster"),
            paragraph("Vertical stacking and horizontal clustering:"),
            codeBlock(LAYOUTS_STACK),

            subheading("Special Layouts"),
            paragraph("Other useful layout patterns:"),
            codeBlock(LAYOUTS_SPECIAL)
        );
    }

    // ==================== UI Components Section ====================

    private static Element uiComponents() {
        return section(
            sectionTitle("UI Components"),
            paragraph("The UI module provides pre-styled, reusable UI components. These components follow common design patterns and are ready to use out of the box."),

            subheading("Import"),
            paragraph("Import the UI class to use components:"),
            codeBlock(UI_IMPORT),

            subheading("Buttons"),
            paragraph("Pre-styled button variants:"),
            codeBlock(UI_BUTTONS),

            subheading("Badges and Tags"),
            paragraph("Small labels for status and categories:"),
            codeBlock(UI_BADGES),

            subheading("Alerts"),
            paragraph("Alert messages for notifications:"),
            codeBlock(UI_ALERTS),

            subheading("Cards"),
            paragraph("Card containers for grouped content:"),
            codeBlock(UI_CARDS),

            subheading("Avatars"),
            paragraph("User avatars with initials or images:"),
            codeBlock(UI_AVATARS),

            subheading("Loading States"),
            paragraph("Components for loading and progress:"),
            codeBlock(UI_LOADING),

            subheading("Typography Helpers"),
            paragraph("Styled code and keyboard elements:"),
            codeBlock(UI_TYPOGRAPHY),

            subheading("Navigation"),
            paragraph("Breadcrumb navigation:"),
            codeBlock(UI_BREADCRUMB),

            subheading("Empty States"),
            paragraph("Placeholder for empty content:"),
            codeBlock(UI_EMPTY),

            subheading("Dividers"),
            paragraph("Visual separators:"),
            codeBlock(UI_DIVIDERS),

            subheading("Complete Example"),
            paragraph("Here's a dashboard card using multiple UI components:"),
            codeBlock(UI_COMPLETE)
        );
    }

    // ==================== DSL Reference Section ====================

    private static Element api() {
        return section(
            sectionTitle("DSL Reference"),
            paragraph("Quick reference for the core JWeb DSL."),

            subheading("Elements"),
            paragraph("All element types are available as static methods:"),
            codeBlock(DSL_ELEMENTS),

            subheading("Attributes"),
            paragraph("Fluent attribute builder methods:"),
            codeBlock(DSL_ATTRIBUTES),

            subheading("Style Properties"),
            paragraph("Style builder methods (partial list):"),
            codeBlock(DSL_STYLES),

            subheading("Routing"),
            paragraph("Route definition methods:"),
            codeBlock(DSL_ROUTING),

            subheading("Request Object"),
            paragraph("Request methods for handlers:"),
            codeBlock(DSL_REQUEST),

            subheading("State"),
            paragraph("Reactive state management:"),
            codeBlock(DSL_STATE)
        );
    }

    // ==================== Helper Methods ====================

    private static Element sectionTitle(String text) {
        return h1(attrs().style()
                .fontSize(TEXT_3XL).fontWeight(700).color(TEXT)
                .marginBottom(SP_4)
            .done(), text(text));
    }

    private static Element subheading(String text) {
        return h2(attrs().style()
                .fontSize(TEXT_XL).fontWeight(600).color(TEXT)
                .marginTop(SP_8).marginBottom(SP_3)
                .prop("border-bottom", "2px solid #e2e8f0")
                .paddingBottom(SP_2)
            .done(), text(text));
    }

    private static Element paragraph(String text) {
        return p(attrs().style()
                .color(TEXT_LIGHT).lineHeight(1.7).marginBottom(SP_4)
            .done(), text(text));
    }

    private static Element codeBlock(String code) {
        return pre(attrs().style()
                .backgroundColor(hex("#1e293b"))
                .color(hex("#e2e8f0"))
                .padding(SP_4)
                .borderRadius(ROUNDED)
                .overflow(scroll)
                .fontSize(TEXT_SM)
                .lineHeight(1.6)
                .marginBottom(SP_6)
            .done(),
            code(text(code))
        );
    }

    private static Element featureList(String... items) {
        return ul(attrs().style()
                .marginBottom(SP_6).paddingLeft(SP_6)
                .prop("list-style-type", "disc")
            .done(),
            each(java.util.List.of(items), item ->
                li(attrs().style()
                        .color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2)
                    .done(), text(item))
            )
        );
    }

    private static Element orderedList(String... items) {
        return ol(attrs().style()
                .marginBottom(SP_6).paddingLeft(SP_6)
            .done(),
            each(java.util.List.of(items), item ->
                li(attrs().style()
                        .color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2)
                    .done(), text(item))
            )
        );
    }
}
