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
            before("v3.0.1",
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
            since("v3.0.1",
                para("Two shapes: when(condition, element) for optional content and " +
                     "when(condition, ifTrue, ifFalse) for a choice — the same call, one " +
                     "argument longer. When a branch is long enough that the argument list " +
                     "stops reading, when(condition).then(...).otherwise(...) says the same " +
                     "thing down the page. Both branches take an Element, a lambda, or a " +
                     "String; each() iterates."),
                codeBlock("""
boolean condition = true;
Element element() { return div("content"); }
Element trueElement() { return div("yes"); }
Element falseElement() { return div("no"); }
List<String> list = List.of("a", "b", "c");
Element renderItem(String item) { return li(item); }

// Show if true
when(condition, element())

// Either/or — one argument longer
when(condition, trueElement(), falseElement())

// The same choice, chained, when a branch is long
when(condition)
    .then(trueElement())
    .otherwise(falseElement())

// Iterate
each(list, item -> renderItem(item))""")),

            CondWhen.render(),
            CondTernary.render(),
            CondChain.render(),
            CondIteration.render()
        );
    }
}
