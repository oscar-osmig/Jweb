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
select(name("country"),
    option(value(""), "Select a country"),
    option(value("us"), "United States"),
    option(value("uk"), "United Kingdom"),
    option(value("ca"), "Canada")
)

// With selected option
select(name("status"),
    option(value("active"), selected(), "Active"),
    option(value("inactive"), "Inactive")
)

// Option groups
select(name("car"),
    optgroup(attrs().label("Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")
    ),
    optgroup(attrs().label("German Cars"),
        option(value("mercedes"), "Mercedes"),
        option(value("audi"), "Audi")
    )
)"""),

            h3Title("Dynamic Options"),
            codeBlock("""
record Country(String code, String name) {}
List<Country> countries = List.of(new Country("us", "United States"), new Country("ca", "Canada"));

select(name("country"),
    option(value(""), "Select..."),
    each(countries, c ->
        option(value(c.code()), c.name())
    )
)""")
        );
    }
}
