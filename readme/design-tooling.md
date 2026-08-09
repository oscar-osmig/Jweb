# Design Tooling (impeccable)

[impeccable](https://github.com/pbakaus/impeccable) is a design skill for AI coding agents.
It provides two separate things, and only one of them works out of the box on JWeb:

| Part | Works here? |
|---|---|
| The `/impeccable` skill (23 design commands, LLM-driven) | Yes, directly |
| The deterministic detector (59 anti-pattern rules) | Only against **rendered** HTML — see below |

## Install

The skill lives in `.claude/`, which this repo gitignores, so it is per-developer:

```bash
npx impeccable install     # choose: project
```

Then in Claude Code, `/impeccable init` writes `PRODUCT.md` and `DESIGN.md`, and commands
like `/impeccable critique docs` or `/impeccable polish contact` work on the Java sources
normally — the agent reads them like any other code.

## Why the detector needs rendered HTML

The detector reads only `.html`, `.css`, `.jsx`, `.tsx`, `.vue`, `.svelte`, and `.astro`
(`SCANNABLE_EXTENSIONS` in its `node/file-system.mjs`). JWeb's entire UI is `.java`, so:

```bash
npx impeccable detect src/    # -> "[]" — scanned nothing, NOT "clean"
```

That empty result is silence, not a pass. The same caveat applies to the `PostToolUse` hook
the installer adds to `.claude/settings.local.json`: it filters on those same extensions and
is a no-op on every JWeb page edit.

The fix is to scan what the DSL actually emits.

## Scanning

Start the app, then:

```bash
./mvnw spring-boot:run       # in another shell
./tools/design-scan.sh
```

`tools/design-scan.sh` curls each route into a temp directory and runs the detector on the
resulting HTML. Options:

```bash
./tools/design-scan.sh --json          # machine-readable
./tools/design-scan.sh /docs /about    # only these routes
PORT=9000 ./tools/design-scan.sh       # non-default port
```

The default route list is at the top of the script; add new pages there as `Routes.java`
grows. Findings cite the temp `.html` file — map it back to the `app/pages/**` class that
renders that route.

`npx impeccable detect <url>` also exists, but it drives a headless browser and times out on
this setup; the curl-to-disk path is the reliable one.

## Ignoring rules

Detector config is shared via `.impeccable/config.json` and per-developer via
`.impeccable/config.local.json` (gitignored):

```bash
npx impeccable ignores                       # show current
npx impeccable ignores add-rule ai-color-palette
```
