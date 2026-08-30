package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class UIComponentsSection {
    private UIComponentsSection() {}

    public static Element render() {
        return section(
            docTitle("UI Components"),
            para("Pre-built interactive components for common patterns."),

            docSubtitle("Modal"),
            codeBlock("""
                    Modal.create("confirm-modal")
                        .title("Confirm Action")
                        .body(p("Are you sure?"))
                        .footer(
                            button(attrs().set("onclick", Modal.closeJs("confirm-modal")), "Cancel"),
                            button(attrs().set("onclick", "handleConfirm()"), "Confirm")
                        )
                        .build()"""),

            docSubtitle("Tabs"),
            codeBlock("""
                        Tabs.create("main-tabs")
                            .tab("overview", "Overview", overviewContent())
                            .tab("details", "Details", detailsContent())
                            .tab("settings", "Settings", settingsContent())
                            .build()"""),

            docSubtitle("Dropdown"),
            codeBlock("""
                        Dropdown.create("actions-menu")
                            .trigger("Actions")
                            .item("Edit", e -> editItem())
                            .item("Delete", e -> deleteItem())
                            .divider()
                            .item("Settings", e -> openSettings())
                            .build()"""),

            docSubtitle("Toast Notifications"),
            codeBlock("""
                        // Setup in layout
                        Toast.setup()
                        
                        // Trigger via JavaScript
                        Toast.success("Saved successfully!")
                        Toast.error("Something went wrong")
                        Toast.info("New message received")"""),

            docTip("All UI components include keyboard navigation and ARIA accessibility.")
        );
    }
}
