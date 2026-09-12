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
                    UI.Modal.create("confirm-modal")
                        .title("Confirm Action")
                        .body(p("Are you sure?"))
                        .footer(
                            button(onClick(UI.Modal.close("confirm-modal")), "Cancel"),
                            button(onClick(call("handleConfirm")), "Confirm")
                        )
                        .build()"""),

            docSubtitle("Tabs"),
            codeBlock("""
                        Element overviewContent() { return div("Overview"); }
                        Element detailsContent() { return div("Details"); }
                        Element settingsContent() { return div("Settings"); }

                        UI.Tabs.create("main-tabs")
                            .tab("overview", "Overview", overviewContent())
                            .tab("details", "Details", detailsContent())
                            .tab("settings", "Settings", settingsContent())
                            .build()"""),

            docSubtitle("Dropdown"),
            codeBlock("""
                        void editItem() {}
                        void deleteItem() {}
                        void openSettings() {}

                        UI.Dropdown.create("actions-menu")
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

                        // Actions to wire into a click handler
                        button(onClick(Toast.success("Saved successfully!")), "Save")
                        button(onClick(Toast.error("Something went wrong")), "Retry")
                        button(onClick(Toast.info("New message received")), "Notify")"""),

            docTip("All UI components include keyboard navigation and ARIA accessibility.")
        );
    }
}
