package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.conditionals.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ConditionalsSection {
    private ConditionalsSection() {}

    public static Element render() {
        return section(
            docTitle("Conditionals"),
            para("JWeb provides fluent conditional rendering utilities. " +
                 "Control what renders based on conditions, iterate over collections, " +
                 "and handle multiple cases cleanly."),

            docSubtitle("Overview"),
            para("Use when() for optional content, cond() for either/or choices, " +
                 "and each() for list iteration."),
            codeBlock("""
// Show if true
when(condition, () -> element())

// Either/or
cond(condition, trueElement(), falseElement())

// Iterate
each(list, item -> renderItem(item))"""),

            CondWhen.render(),
            CondTernary.render(),
            CondChain.render(),
            CondIteration.render()
        );
    }
}
