package com.osmig.Jweb.app.docs.sections.conditionals;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondChain {
    private CondChain() {}

    public static Element render() {
        return section(
            h3Title("If-Elif-Else Chains"),
            para("Handle multiple conditions with fluent chain."),
            codeBlock("""
// Role-based content
when(isAdmin)
    .then(adminPanel())
    .elif(isModerator, moderatorPanel())
    .elif(isEditor, editorPanel())
    .elif(isUser, userPanel())
    .otherwise(guestPanel())

// Status-based styling
when(status.equals("success"))
    .then(successMessage())
    .elif(status.equals("warning"), warningMessage())
    .elif(status.equals("error"), errorMessage())
    .otherwise(infoMessage())"""),

            h3Title("match() - Pattern Matching"),
            para("Match against multiple conditions, first match wins."),
            codeBlock("""
// Status badge
match(
    cond(status.equals("active"), greenBadge("Active")),
    cond(status.equals("pending"), yellowBadge("Pending")),
    cond(status.equals("suspended"), redBadge("Suspended")),
    cond(status.equals("archived"), grayBadge("Archived")),
    otherwise(grayBadge("Unknown"))
)

// HTTP status
match(
    cond(code >= 500, serverError()),
    cond(code >= 400, clientError()),
    cond(code >= 300, redirect()),
    cond(code >= 200, success()),
    otherwise(unknown())
)""")
        );
    }
}
