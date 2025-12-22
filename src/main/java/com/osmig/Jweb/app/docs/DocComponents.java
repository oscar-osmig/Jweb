package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Elements;

import java.util.List;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public final class DocComponents {
    private DocComponents() {}

    public static Element section(Element... children) {
        return Elements.section(children);
    }

    public static Element title(String t) {
        return h1(attrs().style()
            .fontSize(TEXT_3XL).fontWeight(700).color(TEXT).marginBottom(SP_4)
        .done(), text(t));
    }

    public static Element subtitle(String t) {
        return h2(attrs().style()
            .fontSize(TEXT_XL).fontWeight(600).color(TEXT)
            .marginTop(SP_8).marginBottom(SP_3)
            .prop("border-bottom", "2px solid #e2e8f0").paddingBottom(SP_2)
        .done(), text(t));
    }

    public static Element text(String t) {
        return p(attrs().style()
            .color(TEXT_LIGHT).lineHeight(1.7).marginBottom(SP_4)
        .done(), Elements.text(t));
    }

    public static Element code(String c) {
        return pre(attrs().style()
            .backgroundColor(hex("#1e293b")).color(hex("#e2e8f0"))
            .padding(SP_4).borderRadius(ROUNDED).overflow(scroll)
            .fontSize(TEXT_SM).lineHeight(1.6).marginBottom(SP_6)
        .done(), Elements.code(Elements.text(c)));
    }

    public static Element list(String... items) {
        return ul(attrs().style()
            .marginBottom(SP_6).paddingLeft(SP_6).prop("list-style-type", "disc")
        .done(), each(List.of(items), item ->
            li(attrs().style().color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2)
            .done(), Elements.text(item))));
    }
}
