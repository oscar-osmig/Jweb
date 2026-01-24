package com.osmig.Jweb.app.subheader;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Right sidebar showing sub-headers (h3) from the current documentation section.
 * Sub-headers are dynamically populated via JavaScript scanning the content.
 */
public class SubheaderSidebar implements Template {

    @Override
    public Element render() {
        return aside(attrs().id("subheader-sidebar").class_("subheader-sidebar").style()
                .width(px(220))
                .padding(SP_6)
                .borderLeft(px(1), solid, BORDER)
                .backgroundColor(hex("#fafafa"))
                .flexShrink(0)
                .position(sticky)
                .top(px(0))
                .maxHeight(vh(100))
                .overflowY(hidden)
                .display(none)
            .done(),
            h4(attrs().style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_4).textTransform(uppercase)
                .letterSpacing(em(0.05)).done(),
                text("On This Page")),
            nav(attrs().id("subheader-nav").style()
                .display(flex).flexDirection(column).gap(SP_1)
                .overflowY(auto)
                .maxHeight(calc("100vh - 50px"))
                .paddingRight(SP_2)
                .paddingBottom(rem(10))
                .done())
        );
    }
}
