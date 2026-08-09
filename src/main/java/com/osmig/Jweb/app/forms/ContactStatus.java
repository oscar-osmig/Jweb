package com.osmig.Jweb.app.forms;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/** Status fragments returned by the contact form route. */
public final class ContactStatus {

    private ContactStatus() {}

    public static Element success(String message) {
        return box(message, hex("#f0fdf4"), hex("#16a34a"));
    }

    public static Element error(String message) {
        return box(message, hex("#fef2f2"), hex("#dc2626"));
    }

    private static Element box(String message, com.osmig.Jweb.framework.styles.CSSValue bg,
                               com.osmig.Jweb.framework.styles.CSSValue fg) {
        return div(attrs().style()
                .padding(rem(0.75), rem(1))
                .borderRadius(px(6))
                .backgroundColor(bg).color(fg)
                .fontSize(rem(0.875))
            .done(),
            text(message));
    }
}
