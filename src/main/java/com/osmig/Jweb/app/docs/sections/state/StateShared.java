package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/** The shared cache: values, counters and locks every instance sees — Redis, spoken by the framework. */
public final class StateShared {
    private StateShared() {}

    public static Element render() {
        return section(
            h3Title("Shared Cache and Locks"),
            para("Cache.create and Cache.named live in this instance's memory. Cache.shared(name, Type.class) is " +
                 "what every instance sees: a Redis when jweb.cache.redis.url (or jweb.redis.url, for cache and " +
                 "PubSub together) is set — spoken by the framework itself, no client library — and this " +
                 "instance's memory otherwise, with the same semantics. Values travel as JSON."),
            codeBlock("""
import jweb.Cache;

record Price(String sku, long cents) {}
static Price lookup(String sku) { return new Price(sku, 100); }
static void sendDigest() {}

Cache.Shared<Price> prices = Cache.shared("prices", Price.class, Duration.ofMinutes(10));
Price p = prices.getOrSet("sku-9", () -> lookup("sku-9"));
prices.set("sku-9", p);  prices.get("sku-9");  prices.delete("sku-9");  prices.clear();
Cache.shared("counts", Long.class).increment("signups", 1, Duration.ofDays(1));

// work only one instance should do: whoever takes the lock runs, the rest skip
boolean ranHere = Cache.once("digest", Duration.ofMinutes(5), () -> sendDigest());
try (Cache.Lock lock = Cache.lock("import", Duration.ofMinutes(1)).orElseThrow()) {
    // held until closed, or until a dead holder's minute is up
}"""),
            para("A store that cannot be reached is a miss, a no-op, or \"not acquired\" — logged, never a 500. " +
                 "RateLimit counts in the shared store on its own once one is configured, so perMinute(100) is a " +
                 "hundred per minute across the fleet; .shared(false) keeps a limiter local."),
            docTip("One URL for everything: jweb.redis.url serves both the shared cache and PubSub.")
        );
    }
}
