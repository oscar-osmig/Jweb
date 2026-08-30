package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
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
    option(attrs().value(""), "Select a country"),
    option(attrs().value("us"), "United States"),
    option(attrs().value("uk"), "United Kingdom"),
    option(attrs().value("ca"), "Canada")
)

// With selected option
select(attrs().name("status"),
    option(attrs().value("active").set("selected", ""), "Active"),
    option(attrs().value("inactive"), "Inactive")
)

// Option groups
select(attrs().name("car"),
    optgroup("Swedish Cars",
        option(attrs().value("volvo"), "Volvo"),
        option(attrs().value("saab"), "Saab")
    ),
    optgroup("German Cars",
        option(attrs().value("mercedes"), "Mercedes"),
        option(attrs().value("audi"), "Audi")
    )
)"""),

            h3Title("Dynamic Options"),
            codeBlock("""
List<Country> countries = countryService.findAll();

select(attrs().name("country"),
    option(attrs().value(""), "Select..."),
    each(countries, c ->
        option(attrs().value(c.code()), c.name())
    )
)""")
        );
    }
}
