package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsSelects {
    private FormsSelects() {}

    public static Element render() {
        return section(
            h3Title("Select Dropdowns"),
            para("Create select elements with options."),
            codeBlock("""
// Basic select
select(attrs().name("country"),
    option(attrs().value(""), text("Select a country")),
    option(attrs().value("us"), text("United States")),
    option(attrs().value("uk"), text("United Kingdom")),
    option(attrs().value("ca"), text("Canada"))
)

// With selected option
select(attrs().name("status"),
    option(attrs().value("active").selected(), text("Active")),
    option(attrs().value("inactive"), text("Inactive"))
)

// Option groups
select(attrs().name("car"),
    optgroup(attrs().label("Swedish Cars"),
        option(attrs().value("volvo"), text("Volvo")),
        option(attrs().value("saab"), text("Saab"))
    ),
    optgroup(attrs().label("German Cars"),
        option(attrs().value("mercedes"), text("Mercedes")),
        option(attrs().value("audi"), text("Audi"))
    )
)"""),

            h3Title("Dynamic Options"),
            codeBlock("""
List<Country> countries = countryService.findAll();

select(attrs().name("country"),
    option(attrs().value(""), text("Select...")),
    each(countries, c ->
        option(attrs().value(c.code()), text(c.name()))
    )
)""")
        );
    }
}
