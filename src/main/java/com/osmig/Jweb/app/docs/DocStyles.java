package com.osmig.Jweb.app.docs;

import jweb.Style;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public final class DocStyles {
    private DocStyles() {}

    public static Style title() {
        return style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).marginBottom(SP_6);
    }

    public static Style subtitle() {
        return style().fontSize(TEXT_XL).fontWeight(600).color(TEXT)
            .marginTop(SP_8).marginBottom(SP_4).paddingBottom(SP_2)
            .borderBottom(px(1), solid, hex("#e2e8f0"));
    }

    public static Style paragraph() {
        return style().color(TEXT_LIGHT).lineHeight(1.7).marginBottom(SP_4);
    }

    public static Style codeBlock() {
        return style().backgroundColor(hex("#1e293b")).color(hex("#e2e8f0"))
            .padding(SP_4).borderRadius(ROUNDED_LG).overflow(auto)
            .fontSize(TEXT_SM).lineHeight(1.6).marginBottom(SP_6);
    }

    public static Style inlineCode() {
        return style().backgroundColor(hex("#f1f5f9")).color(hex("#475569"))
            .padding(px(2), px(6)).borderRadius(px(4)).fontSize(TEXT_SM)
            .fontFamily("monospace");
    }

    public static Style list() {
        return style().marginBottom(SP_6).paddingLeft(SP_6).listStyleType(disc);
    }

    public static Style listItem() {
        return style().color(TEXT_LIGHT).lineHeight(1.8).marginBottom(SP_2);
    }

    public static Style tip() {
        return style().backgroundColor(hex("#ecfdf5")).border(px(1), solid, hex("#a7f3d0"))
            .padding(SP_4).marginBottom(SP_6).borderRadius(px(0), ROUNDED, ROUNDED, px(0));
    }

    public static Style warning() {
        return style().backgroundColor(hex("#fef3c7")).border(px(1), solid, hex("#fcd34d"))
            .padding(SP_4).marginBottom(SP_6).borderRadius(px(0), ROUNDED, ROUNDED, px(0));
    }
}
