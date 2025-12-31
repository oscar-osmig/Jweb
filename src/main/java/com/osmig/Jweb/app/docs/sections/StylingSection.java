package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.styling.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingSection {
    private StylingSection() {}

    public static Element render() {
        return section(
            docTitle("Styling"),
            para("JWeb's CSS DSL provides Java methods for all CSS properties. " +
                 "Write styles in Java with IDE autocomplete and compile-time checks."),

            docSubtitle("Overview"),
            para("Every CSS property has a corresponding fluent method. " +
                 "Units and colors are type-safe, preventing invalid values."),
            codeBlock("""
                    import static com.osmig.Jweb.framework.styles.CSS.*;
                    import static com.osmig.Jweb.framework.styles.CSSUnits.*;
                    import static com.osmig.Jweb.framework.styles.CSSColors.*;
                    
                    div(attrs().style()
                        .padding(rem(2))
                        .backgroundColor(hex("#f5f5f5"))
                        .borderRadius(px(8))
                    .done(), content)"""),

            StylingBasics.render(),
            StylingUnits.render(),
            StylingColors.render(),
            StylingFlexbox.render(),
            StylingGrid.render(),
            StylingTypography.render(),
            StylingAdvanced.render(),
            StylingNested.render(),
            StylingSupports.render()
        );
    }
}
