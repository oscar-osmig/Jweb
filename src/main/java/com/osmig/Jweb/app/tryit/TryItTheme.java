package com.osmig.Jweb.app.tryit;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.styles.CSSValue;

import com.osmig.Jweb.framework.elements.TextElement;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/** Shared styles and components for Try It feature. */
public final class TryItTheme {
    private TryItTheme() {}

    public static final CSSValue INDIGO = hex("#6366f1"), GREEN = hex("#10b981");
    public static final CSSValue GRAY_50 = hex("#f9fafb"), GRAY_200 = hex("#e5e7eb");
    public static final CSSValue GRAY_300 = hex("#d1d5db"), GRAY_500 = hex("#6b7280");
    public static final CSSValue GRAY_600 = hex("#4b5563"), GRAY_700 = hex("#374151");
    public static final CSSValue GRAY_900 = hex("#111827");

    public static Element field(String labelText, String name, String type, String placeholder) {
        return div(
            label(attrs().for_(name).style().display(block).fontSize(rem(0.8)).fontWeight(500)
                .color(GRAY_700).marginBottom(rem(0.2)).done(), text(labelText)),
            input(attrs().id(name).type(type).name(name).placeholder(placeholder).required().style()
                .width(percent(100)).padding(rem(0.6)).border(px(1), solid, GRAY_300)
                .borderRadius(px(6)).fontSize(rem(0.9)).outline(none).boxSizing(borderBox).done())
        );
    }

    public static Element textareaField(String labelText, String name, String placeholder, int rows) {
        return div(
            label(attrs().style().display(block).fontSize(rem(0.8)).fontWeight(500)
                .color(GRAY_700).marginBottom(rem(0.2)).done(), text(labelText)),
            textarea(attrs().name(name).placeholder(placeholder).rows(rows).required().style()
                .width(percent(100)).padding(rem(0.6)).border(px(1), solid, GRAY_300)
                .borderRadius(px(6)).fontSize(rem(0.9)).resize(vertical).outline(none)
                .fontFamily("inherit").boxSizing(borderBox).done())
        );
    }

    public static Element submitBtn(String label, CSSValue bgColor) {
        return button(attrs().type("submit").style()
            .width(percent(100)).padding(rem(0.7)).backgroundColor(bgColor).color(white)
            .border(none).borderRadius(px(6)).fontSize(rem(0.9)).fontWeight(600).cursor(pointer)
        .done(), text(label));
    }

    public static Element messageBox(String id, String defaultText) {
        return div(attrs().id(id).style().padding(rem(0.6)).borderRadius(px(6)).fontSize(rem(0.8))
            .backgroundColor(hex("#f3f4f6")).color(GRAY_500).done(), text(defaultText));
    }

    public static Element card(String icon, String title, String desc, Element form) {
        String svg = icon.equals("rocket") ? rocketSvg() : downloadSvg();
        return div(attrs().style().flex(() -> "1 1 340px").maxWidth(px(400)).backgroundColor(white)
            .borderRadius(px(12)).boxShadow("0 8px 30px rgba(0,0,0,0.1)").padding(rem(1.5)).done(),
            div(attrs().style().width(px(50)).height(px(50)).borderRadius(px(10)).backgroundColor(INDIGO)
                .color(white).display(flex).alignItems(center).justifyContent(center).marginBottom(rem(1)).done(), TextElement.raw(svg)),
            h2(attrs().style().fontSize(rem(1.25)).fontWeight(600).color(hex("#1a1a2e")).margin(zero).done(), text(title)),
            p(attrs().style().fontSize(rem(0.875)).color(GRAY_500).margin(zero).marginBottom(rem(1)).done(), text(desc)),
            form
        );
    }

    private static String rocketSvg() { return """
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z"/>
            <path d="m12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z"/>
        </svg>"""; }

    private static String downloadSvg() { return """
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
        </svg>"""; }
}
