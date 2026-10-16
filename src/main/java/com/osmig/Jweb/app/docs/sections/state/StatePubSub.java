package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/** PubSub: one publish, every subscribed page re-rendered — across instances with Redis. */
public final class StatePubSub {
    private StatePubSub() {}

    public static Element render() {
        return since("v3.0.3", section(
            h3Title("PubSub: One Change, Every Page"),
            para("A browser event changes one page. PubSub is the other direction: publish a message to a topic, " +
                 "and every page that subscribed re-renders what the message touched — on this instance and, " +
                 "with Redis, on every other. A subscription made while rendering belongs to the page: the handler " +
                 "runs in the page's context and the live regions it changed are patched over the page's socket."),
            codeBlock("""
import jweb.PubSub;
import static jweb.El.*;
import static jweb.State.*;

record Post(String author, String text) {}
String room = "42";

// In a page: subscribe once, in render(), and the live region follows the topic
State<List<Post>> posts = useState(new ArrayList<>());
PubSub.subscribe("room:" + room, Post.class, post -> posts.mutate(list -> list.add(post)));
Element wall = live(posts, list -> ul(each(list, p -> li(p.author() + ": " + p.text()))));

// From anywhere: a handler, a REST endpoint, a job, another instance
PubSub.publish("room:" + room, new Post("ada", "hello"));"""),
            para("Outside a render a subscription is a plain listener until it is closed. Messages travel as JSON " +
                 "and arrive parsed to the class the subscriber asked for."),
            codeBlock("""
import jweb.PubSub;

record Order(long id, String status) {}

PubSub.Subscription audit = PubSub.subscribe("orders", Order.class, o -> System.out.println(o.status()));
PubSub.Subscription raw = PubSub.subscribe("orders", json -> System.out.println(json));
audit.close();
raw.close();"""),
            para("Several instances share topics through Redis — no client library, the framework speaks the " +
                 "protocol itself. Set the URL and every instance with the same one is one PubSub."),
            codeBlock("yaml", """
jweb:
  pubsub:
    redis:
      url: ${REDIS_URL:redis://localhost:6379}
      prefix: jweb:"""),
            docTip("Render contexts stay on the instance that rendered the page (sticky sessions for /jweb); " +
                   "PubSub is what crosses instances. The State & Realtime guide has the full table of what is " +
                   "per-instance and what is shared.")
        ));
    }
}
