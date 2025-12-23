package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
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
                            button(attrs().onClick("Modal.close('confirm-modal')"), "Cancel"),
                            button(attrs().onClick("handleConfirm()"), "Confirm")
                        )
                        .render()"""),

            docSubtitle("Tabs"),
            codeBlock("""
                        Tabs.create()
                            .tab("overview", "Overview", overviewContent())
                            .tab("details", "Details", detailsContent())
                            .tab("settings", "Settings", settingsContent())
                            .render()"""),

            docSubtitle("Dropdown"),
            codeBlock("""
                        Dropdown.create("Actions")
                            .item("Edit", "/edit")
                            .item("Delete", "handleDelete()")
                            .divider()
                            .item("Settings", "/settings")
                            .render()"""),

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
