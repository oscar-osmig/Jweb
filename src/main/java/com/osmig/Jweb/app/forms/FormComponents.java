package com.osmig.Jweb.app.forms;

import jweb.Element;
import jweb.CSSValue;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Centralized form field helpers.
 * Use with: {@code import static FormComponents.*;}
 */
public final class FormComponents {
    private FormComponents() {}

    /** Creates a labeled input field. */
    public static Element field(String labelText, String name, String type, String placeholder) {
        return field(labelText, name, type, placeholder, BORDER);
    }

    /** Creates a labeled input field with custom border color. */
    public static Element field(String labelText, String name, String type, String placeholder, CSSValue borderColor) {
        return div(
            label(for_(name), labelStyle(), labelText),
            input(type(type), name(name), id(name), placeholder(placeholder), required(),
                controlStyle().border(px(1), solid, borderColor))
        );
    }

    /** Creates a labeled textarea field. */
    public static Element textareaField(String labelText, String name, String placeholder, int rows) {
        return div(
            label(for_(name), labelStyle(), labelText),
            textarea(name(name), id(name), placeholder(placeholder), attrs().rows(rows), required(),
                controlStyle().border(px(1), solid, BORDER).resize(vertical).fontFamily("inherit"))
        );
    }

    /** Creates a submit button with custom background color. */
    public static Element submitButton(String label, CSSValue bgColor) {
        return button(type("submit"), style()
            .width(percent(100)).padding(SP_3).backgroundColor(bgColor).color(white)
            .border(none).borderRadius(ROUNDED).fontSize(TEXT_BASE).fontWeight(600).cursor(pointer),
            label);
    }

    /** Creates a submit button with primary color. */
    public static Element submitButton(String label) {
        return submitButton(label, PRIMARY);
    }

    /** Creates a status message box. */
    public static Element statusBox(String id) {
        return div(id(id), style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM));
    }

    /** Creates a status message box with default text. */
    public static Element statusBox(String id, String defaultText) {
        return div(id(id), style().padding(SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
            .backgroundColor(hex("#f3f4f6")).color(TEXT_LIGHT), defaultText);
    }

    /** Creates an error message box (hidden by default). */
    public static Element errorBox(String id) {
        return div(id(id), style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM).backgroundColor(hex("#fee2e2")).color(hex("#991b1b")));
    }

    /** Creates a success message box (hidden by default). */
    public static Element successBox(String id) {
        return div(id(id), style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM).backgroundColor(hex("#dcfce7")).color(hex("#166534")));
    }

    private static jweb.Style<?> labelStyle() {
        return style().display(block).fontSize(TEXT_SM).fontWeight(500)
            .color(TEXT).marginBottom(SP_1);
    }

    private static jweb.Style<?> controlStyle() {
        return style().width(percent(100)).padding(SP_3)
            .borderRadius(ROUNDED).fontSize(TEXT_BASE).boxSizing(borderBox).outline(none);
    }
}
