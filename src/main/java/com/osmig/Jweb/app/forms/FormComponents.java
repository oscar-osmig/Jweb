package com.osmig.Jweb.app.forms;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.styles.CSSValue;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Centralized form field helpers.
 * Use with: {@code import static FormComponents.*;}
 */
public final class FormComponents {
    private FormComponents() {}

    /** Creates a labeled input field. */
    public static Element field(String labelText, String name, String type, String placeholder) {
        return div(
            label(attrs().for_(name).style().display(block).fontSize(TEXT_SM).fontWeight(500)
                .color(TEXT).marginBottom(SP_1).done(), text(labelText)),
            input(attrs().type(type).name(name).id(name).placeholder(placeholder).required().style()
                .width(percent(100)).padding(SP_3).border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).fontSize(TEXT_BASE).boxSizing(borderBox).outline(none).done())
        );
    }

    /** Creates a labeled input field with custom border color. */
    public static Element field(String labelText, String name, String type, String placeholder, CSSValue borderColor) {
        return div(
            label(attrs().for_(name).style().display(block).fontSize(TEXT_SM).fontWeight(500)
                .color(TEXT).marginBottom(SP_1).done(), text(labelText)),
            input(attrs().type(type).name(name).id(name).placeholder(placeholder).required().style()
                .width(percent(100)).padding(SP_3).border(px(1), solid, borderColor)
                .borderRadius(ROUNDED).fontSize(TEXT_BASE).boxSizing(borderBox).outline(none).done())
        );
    }

    /** Creates a labeled textarea field. */
    public static Element textareaField(String labelText, String name, String placeholder, int rows) {
        return div(
            label(attrs().for_(name).style().display(block).fontSize(TEXT_SM).fontWeight(500)
                .color(TEXT).marginBottom(SP_1).done(), text(labelText)),
            textarea(attrs().name(name).id(name).placeholder(placeholder).rows(rows).required().style()
                .width(percent(100)).padding(SP_3).border(px(1), solid, BORDER)
                .borderRadius(ROUNDED).fontSize(TEXT_BASE).resize(vertical)
                .fontFamily("inherit").boxSizing(borderBox).outline(none).done())
        );
    }

    /** Creates a submit button with custom background color. */
    public static Element submitButton(String label, CSSValue bgColor) {
        return button(attrs().type("submit").style()
            .width(percent(100)).padding(SP_3).backgroundColor(bgColor).color(white)
            .border(none).borderRadius(ROUNDED).fontSize(TEXT_BASE).fontWeight(600).cursor(pointer)
        .done(), text(label));
    }

    /** Creates a submit button with primary color. */
    public static Element submitButton(String label) {
        return submitButton(label, PRIMARY);
    }

    /** Creates a status message box. */
    public static Element statusBox(String id) {
        return div(attrs().id(id).style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM).done());
    }

    /** Creates a status message box with default text. */
    public static Element statusBox(String id, String defaultText) {
        return div(attrs().id(id).style().padding(SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
            .backgroundColor(hex("#f3f4f6")).color(TEXT_LIGHT).done(), text(defaultText));
    }

    /** Creates an error message box (hidden by default). */
    public static Element errorBox(String id) {
        return div(attrs().id(id).style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM).backgroundColor(hex("#fee2e2")).color(hex("#991b1b")).done());
    }

    /** Creates a success message box (hidden by default). */
    public static Element successBox(String id) {
        return div(attrs().id(id).style().display(none).padding(SP_3).borderRadius(ROUNDED)
            .fontSize(TEXT_SM).backgroundColor(hex("#dcfce7")).color(hex("#166534")).done());
    }
}
