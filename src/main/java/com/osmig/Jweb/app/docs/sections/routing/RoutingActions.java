package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/** Action routes (record binding) and the Session. */
public final class RoutingActions {
    private RoutingActions() {}

    public static Element render() {
        return section(
            h3Title("Action Routes"),
            para("A mutation reachable from a link or a form: the query/form parameters bind " +
                 "to a record before the handler runs. Enums bind by name, numbers parse, " +
                 "Optional<T> marks an optional parameter, @Range/@Length/@Pattern validate. " +
                 "Anything missing or invalid is a 400 with a message — never an exception page."),
            codeBlock("""
import jweb.api.Range;

enum Setting { EBB, SLACK, FLOOD }
record Vane(@Range(min = 1, max = 3) int n, Setting set) {}
record Pref(Optional<Setting> sound, Optional<Boolean> motion) {}

// GET /act/tide/vane?n=2&set=flood  — or a POSTed form with the same fields
app.action("/act/tide/vane", Vane.class, (req, vane) -> {
    Visit v = Session.of(Visit.class, req);
    v.vanes.put(vane.n(), vane.set());
    return Response.redirect("/worlds/tide-archive")
        .anchor(v.vanesAligned() ? "content" : "current-chamber");
});

app.action("/act/pref", Pref.class, (req, pref) -> {
    Visit v = Session.of(Visit.class, req);
    pref.sound().ifPresent(s -> v.soundOn = s != Setting.EBB);
    return Response.redirectBack(req);           // the Referer, or "/"
});

// One method only
app.get("/messages", View.class, (req, view) -> ...);
app.post("/login", Login.class, (req, login) -> ...);

// Anywhere else: bind by hand (BindException → 400 if it escapes)
Search search = req.bind(Search.class);"""),

            h3Title("Session"),
            para("jweb.Session is the visitor's session: typed, null-safe, with one-shot flash " +
                 "messages. A page reaches it without a Request through session()."),
            codeBlock("""
import jweb.Session;

Visit visit = Session.of(Visit.class, req);        // created on first use, shared after
Session session = Session.of(req);
session.put("theme", "dark");
String theme = session.get("theme", String.class, "light");
session.flash("notice", "Saved!");                // read once, then gone
String notice = session.flash("notice");
session.end();                                    // what Auth.logout does

public class AccountPage implements Template {
    public Element render() {
        Visit visit = session().of(Visit.class);   // the request in flight
        return p("Welcome back, " + visit.name);
    }
}"""),
            docTip("The session is the container's: created on the first write, 30 minutes idle by " +
                   "default, kept in server memory — small, serializable values only.")
        );
    }
}
