package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

public final class UIComponentsSection {
    private UIComponentsSection() {}

    public static Element render() {
        return section(
            title("UI Components"),
            text("JWeb includes pre-built UI components for common patterns: " +
                 "modals, tabs, dropdowns, accordions, toasts, and more."),

            subtitle("Modal Dialogs"),
            code(UI_MODAL),

            subtitle("Tabs"),
            code(UI_TABS),

            subtitle("Dropdown Menus"),
            code(UI_DROPDOWN),

            subtitle("Accordion"),
            code(UI_ACCORDION),

            subtitle("Toast Notifications"),
            text("Show temporary notifications with Toast.setup() and JavaScript calls:"),
            code(UI_TOAST),

            subtitle("Data Tables"),
            text("Create sortable, filterable tables from your data:"),
            code(UI_DATATABLE),

            subtitle("Navigation Bar"),
            code(UI_NAVBAR)
        );
    }
}
