package com.osmig.Jweb.app.forms;

import jweb.Element;

import static jweb.El.*;
import static jweb.Css.*;

/** Status fragments returned by the contact form route. */
public final class ContactStatus {

    private ContactStatus() {}

    public static Element success(String message) {
        return box(message, hex("#f0fdf4"), hex("#15803d"));
    }

    public static Element error(String message) {
        return box(message, hex("#fef2f2"), hex("#b91c1c"));
    }

    private static Element box(String message, jweb.CSSValue bg,
                               jweb.CSSValue fg) {
        return div(
            style().padding(rem(0.75), rem(1)).borderRadius(px(6))
                   .backgroundColor(bg).color(fg).fontSize(rem(0.875)),
            text(message));
    }
}
