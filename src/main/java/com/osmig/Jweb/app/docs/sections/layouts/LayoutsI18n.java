package com.osmig.Jweb.app.docs.sections.layouts;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class LayoutsI18n {
    private LayoutsI18n() {}

    public static Element render() {
        return section(
            h3Title("Internationalization (i18n)"),
            para("Build multilingual applications with translated messages."),
            codeBlock("""
import com.osmig.Jweb.framework.i18n.I18n;

// Get translated message
String title = I18n.t("page.title");

// With parameters
String greeting = I18n.t("welcome", username);
// "welcome" = "Hello, {0}!" → "Hello, John!\""""),

            h3Title("Message Files"),
            para("Create message files in src/main/resources/messages/:"),
            codeBlock("""
// messages_en.properties
page.title=Welcome
nav.home=Home
nav.about=About
welcome=Hello, {0}!
errors.required=This field is required

// messages_es.properties
page.title=Bienvenido
nav.home=Inicio
nav.about=Acerca de
welcome=¡Hola, {0}!
errors.required=Este campo es obligatorio"""),

            h3Title("In Templates"),
            codeBlock("""
public class HomePage implements Template {
    public Element render() {
        return div(
            h1(I18n.t("page.title")),
            nav(
                a(href("/"), I18n.t("nav.home")),
                a(href("/about"), I18n.t("nav.about"))
            )
        );
    }
}"""),

            h3Title("Locale Detection"),
            para("I18n detects locale from (in order):"),
            codeBlock("""
// 1. Query parameter: /page?lang=es
// 2. Session attribute: lang
// 3. Cookie: lang
// 4. Accept-Language header
// 5. Default locale

// Get current locale
Locale locale = I18n.getLocale(req);

// Use middleware for automatic detection
app.use(I18n.middleware());"""),

            h3Title("Language Picker"),
            codeBlock("""
// Get supported locales
List<I18n.LocaleInfo> locales = I18n.getSupportedLocales();
// Each has: code, nativeName, englishName

// In template
form(method("post"), action("/language"),
    select(name("lang"), onChange("this.form.submit()"),
        each(locales, locale ->
            option(
                value(locale.code()),
                selected(locale.code().equals(I18n.current().getLanguage())),
                text(locale.nativeName())  // "Español" for Spanish
            )
        )
    )
)"""),

            h3Title("Switch Language"),
            codeBlock("""
// Via session
app.post("/language", req -> {
    String lang = req.formParam("lang");
    req.session().setAttribute("lang", lang);
    return Response.redirect(req.header("Referer"));
});

// Via cookie (persistent)
Cookie.of("lang", lang)
    .maxAge(Duration.ofDays(365))
    .addTo(response);"""),

            docTip("Use nativeName() in language pickers - users recognize their language better.")
        );
    }
}
