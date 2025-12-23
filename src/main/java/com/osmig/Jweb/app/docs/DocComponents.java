package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import java.util.List;
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.app.docs.DocStyles.*;

public final class DocComponents {
    private DocComponents() {}

    public static Element section(Element... children) {
        return div(attrs().style().maxWidth(px(900)).done(), fragment(children));
    }

    public static Element docTitle(String t) { return h1(attrs().style(title()), text(t)); }

    public static Element docSubtitle(String t) { return h2(attrs().style(subtitle()), text(t)); }

    public static Element h3Title(String t) {
        return h3(attrs().style(subtitle().fontSize(rem(1.1)).marginTop(rem(1.5))), text(t));
    }

    public static Element para(String t) { return p(attrs().style(paragraph()), text(t)); }

    public static Element codeBlock(String c) { return pre(attrs().style(DocStyles.codeBlock()), code(text(c))); }

    public static Element inlineCode(String c) { return span(attrs().style(DocStyles.inlineCode()), text(c)); }

    public static Element docList(String... items) {
        return ul(attrs().style(list()), each(List.of(items), i -> li(attrs().style(listItem()), text(i))));
    }

    public static Element docTip(String t) { return div(attrs().style(tip()), text(t)); }

    public static Element warn(String t) { return div(attrs().style(warning()), text(t)); }

    public static Element spacer() { return div(attrs().style().height(rem(2)).done()); }
}
