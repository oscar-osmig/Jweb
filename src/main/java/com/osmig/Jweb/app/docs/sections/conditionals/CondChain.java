package com.osmig.Jweb.app.docs.sections.conditionals;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CondChain {
    private CondChain() {}

    public static Element render() {
        return section(
            before("v3.0.0",
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
)""")),

            since("v3.0.0",
                h3Title("Multi-Way Choices"),
                para("One conditional shape — when(condition, element) — covers optional " +
                     "content; Java's own switch expression handles branching on a value, " +
                     "so there is no separate chain or match() to learn."),
                codeBlock("""
// Role-based content
Element panel = switch (role) {
    case ADMIN -> adminPanel();
    case MODERATOR -> moderatorPanel();
    case EDITOR -> editorPanel();
    case USER -> userPanel();
    default -> guestPanel();
};

// Status badge
Element badge = switch (status) {
    case "active" -> greenBadge("Active");
    case "pending" -> yellowBadge("Pending");
    case "suspended" -> redBadge("Suspended");
    case "archived" -> grayBadge("Archived");
    default -> grayBadge("Unknown");
};

// Ranges aren't a switch — a ternary chain (or a few when()s) reads fine
Element result = code >= 500 ? serverError()
    : code >= 400 ? clientError()
    : code >= 300 ? redirect()
    : code >= 200 ? success()
    : unknown();"""))
        );
    }
}
