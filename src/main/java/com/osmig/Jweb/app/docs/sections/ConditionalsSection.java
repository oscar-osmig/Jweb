package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ConditionalsSection {
    private ConditionalsSection() {}

    public static Element render() {
        return section(
            title("Conditionals"),
            text("Control flow for conditional rendering."),

            subtitle("Simple Conditional"),
            code("""
                // Show element only if condition is true
                when(isLoggedIn, span("Welcome back!"))

                // With lazy evaluation
                when(isLoggedIn, () -> expensiveComponent())"""),

            subtitle("If / Elif / Else Chain"),
            code("""
                when(isAdmin)
                    .then(adminPanel())
                    .elif(isModerator, modPanel())
                    .elif(isUser, userPanel())
                    .otherwise(loginPrompt())

                // Without else (renders nothing if no match)
                when(isPremium)
                    .then(premiumBadge())
                    .elif(isTrial, trialBadge())
                    .end()"""),

            subtitle("Pattern Matching Style"),
            code("""
                match(
                    cond(isAdmin, adminPanel()),
                    cond(isModerator, modPanel()),
                    cond(isUser, userPanel()),
                    otherwise(loginPrompt())
                )"""),

            subtitle("List Iteration"),
            code("""
                List<String> items = List.of("Apple", "Banana", "Cherry");

                ul(each(items, item -> li(item)))

                // With index
                ul(each(users, user ->
                    li(span(user.getName()),
                       when(user.isAdmin(), span(" (Admin)")))
                ))""")
        );
    }
}
