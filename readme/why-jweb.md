[← Back to README](./../README.md)

# Why JWeb?

*How JWeb compares to the 2026 frontend landscape — grounded in
[the framework research](./research.md).*

## The one-paragraph pitch

The 2026 research is blunt: for a team with a real backend, adopting a JS meta-framework
means duplicating your server in JavaScript, and the recommended alternative is the
**server-driven track** (HTMX-style). JWeb *is* that track — with the type safety HTMX
can't offer, the streaming and fragments the meta-frameworks brag about, and everything
in one language your team already knows.

## Feature parity with the leaders

| They lead with... | JWeb has it as... |
|---|---|
| Next.js streaming SSR + Suspense | `Streamed.of(() -> page)` — shell flushes instantly, blocks stream in parallel |
| HTMX fragment swaps | `attrs().swap(url, target)` / `swapForm` / `swapMorph` — typed, built in |
| idiomorph DOM morphing | `swapMorph` + morphing hot reload — focus and input state survive |
| TanStack type-safe routing | `TypedRoute.path("/users/:id", Long.class)` + `Query.of("page", Integer.class)` |
| Astro zero-JS default | One small cached runtime; pages are HTML-first by design |
| View Transitions | Automatic on every swap |
| SvelteKit progressive forms | `swapForm` + native POST fallback — works with JS disabled |
| Next/Astro image optimization | `/jweb/img?src=...&w=...` — ImageIO, zero deps |
| Vercel AI SDK | `AI.ask/chat/agent` with tool loops — zero deps, any OpenAI-compatible API |
| Tailwind design tokens | `Theme` constants + `style().apply(fragment)` composition |

## What the JS stacks can't match

- **One language, one build, one deploy** — no Node toolchain, no JSON layer between
  your data and your HTML, no hydration mismatch class of bugs. `jweb build` → jar + Docker.
- **Compile-time checked everything** — elements, styles, routes, links, query params.
  The research calls HTMX "essentially untyped"; React's safety ends at the API boundary.
- **State lives on the server** — the signals the industry converged on, without
  shipping your state machine to the client.
- **~0 bundle anxiety** — React ships ~42KB before your first component; JWeb ships
  one small immutable-cached runtime and only the HTML you rendered.

## Honest trade-offs

- **Ecosystem and hiring**: React has "10 developers for every 1" of anything else, and
  the largest AI-training corpus. JWeb's counter: every Java developer is already a JWeb
  developer.
- **Rich client-side apps** (canvas editors, collaborative cursors, offline-first): a
  client-state framework is genuinely better there. JWeb's JS DSL covers a lot, but that's
  not the sweet spot.
- **Mobile**: no React Native equivalent. Capacitor-wrapping a JWeb app works for the
  WebView tier.

## The 5-minute start

```bash
# jweb = alias for: java -cp Jweb.jar com.osmig.Jweb.framework.cli.JWebCli
jweb new myapp && cd myapp
./mvnw spring-boot:run          # http://localhost:8085
jweb build                      # production jar + Dockerfile
```
