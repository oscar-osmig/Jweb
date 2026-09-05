package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
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
            before("v3.0.0",
                para("Use when() for optional content, cond() for either/or choices, " +
                     "and each() for list iteration."),
                codeBlock("""
// Show if true
when(condition, () -> element())

// Either/or
when(condition)
    .then(trueElement())
    .otherwise(falseElement())

// Iterate
each(list, item -> renderItem(item))""")),
            since("v3.0.0",
                para("One conditional shape covers optional content — when(condition, element) " +
                     "— and Java already has the rest: a ternary for either/or, a switch " +
                     "expression for multiple cases, and each() for list iteration."),
                codeBlock("""
// Show if true
when(condition, () -> element())

// Either/or
condition ? trueElement() : falseElement()

// Iterate
each(list, item -> renderItem(item))""")),

            CondWhen.render(),
            CondTernary.render(),
            CondChain.render(),
            CondIteration.render()
        );
    }
}
