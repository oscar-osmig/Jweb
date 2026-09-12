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
                para("when() covers one branch or two. Branching on a value is what Java's " +
                     "switch expression is for, so there is no elif chain and no match() — " +
                     "both are gone in 3.0."),
                codeBlock("""
enum Role { ADMIN, MODERATOR, EDITOR, USER }
Role role = Role.ADMIN;
String status = "success";
int code = 200;
Element adminPanel() { return div("Admin"); }
Element moderatorPanel() { return div("Moderator"); }
Element editorPanel() { return div("Editor"); }
Element userPanel() { return div("User"); }
Element guestPanel() { return div("Guest"); }
Element greenBadge(String s) { return div(s); }
Element yellowBadge(String s) { return div(s); }
Element redBadge(String s) { return div(s); }
Element grayBadge(String s) { return div(s); }
Element serverError() { return div("500"); }
Element clientError() { return div("400"); }
Element redirect() { return div("300"); }
Element success() { return div("200"); }
Element unknown() { return div("?"); }

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
