package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingSection {
    private StylingSection() {}

    public static Element render() {
        return section(
            title("Styling"),
            text("Type-safe CSS with IDE autocomplete and compile-time checks."),

            subtitle("Inline Styles"),
            code("""
                div(attrs().style()
                    .display(flex)
                    .padding(rem(2))
                    .backgroundColor(hex("#f5f5f5"))
                    .borderRadius(px(8))
                .done(),
                    p("Styled content")
                )"""),

            subtitle("Units"),
            code("""
                px(16)       // 16px
                rem(1.5)     // 1.5rem
                em(2)        // 2em
                percent(50)  // 50%
                vh(100)      // 100vh
                vw(50)       // 50vw"""),

            subtitle("Colors"),
            code("""
                hex("#6366f1")
                rgb(99, 102, 241)
                rgba(0, 0, 0, 0.5)
                hsl(239, 84, 67)
                // Named: red, blue, white, black, transparent"""),

            subtitle("Layout"),
            code("""
                // Flexbox
                .display(flex)
                .flexDirection(column)
                .justifyContent(center)
                .alignItems(center)
                .gap(rem(1))

                // Grid
                .display(grid)
                .gridTemplateColumns(repeat(3, fr(1)))
                .gap(rem(2))""")
        );
    }
}
