#!/usr/bin/env bash
# Render JWeb pages from a running server and scan the HTML with impeccable's
# anti-pattern detector.
#
# The detector only reads .html/.css/.jsx/.tsx/.vue/.svelte/.astro files, so it
# cannot see a UI that lives in .java sources. Pointing it at src/ silently
# reports zero findings. This renders the real output first, then scans that.
#
#   ./tools/design-scan.sh                # scan default routes
#   ./tools/design-scan.sh --json         # machine-readable
#   ./tools/design-scan.sh /docs /about   # scan specific routes
#
# Requires the app to be running (./mvnw spring-boot:run).

set -euo pipefail

PORT="${PORT:-8085}"
BASE="http://localhost:${PORT}"
OUT="${TMPDIR:-/tmp}/jweb-design-scan"

ROUTES=(
  "/"
  "/about"
  "/contact"
  "/docs"
  "/docs?section=routing"
  "/docs?section=styling"
  "/docs?section=examples"
  "/demo/streaming"
  "/only-admin/log/in"
)

JSON=""
CUSTOM=()
for arg in "$@"; do
  case "$arg" in
    --json) JSON="--json" ;;
    -*) echo "unknown option: $arg" >&2; exit 2 ;;
    *) CUSTOM+=("$arg") ;;
  esac
done
if [ ${#CUSTOM[@]} -gt 0 ]; then
  ROUTES=("${CUSTOM[@]}")
fi

if ! curl -fsS -o /dev/null "$BASE/" 2>/dev/null; then
  echo "No JWeb server on $BASE — start it with ./mvnw spring-boot:run" >&2
  exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT"

for route in "${ROUTES[@]}"; do
  # "/docs?section=routing" -> "docs-section-routing.html"
  name=$(printf '%s' "${route#/}" | tr -c 'A-Za-z0-9' '-' | sed 's/-\{1,\}/-/g; s/^-//; s/-$//')
  [ -z "$name" ] && name="index"
  if ! curl -fsS "$BASE$route" -o "$OUT/$name.html"; then
    echo "warning: could not fetch $route" >&2
    rm -f "$OUT/$name.html"
  fi
done

if [ -z "$JSON" ]; then
  printf 'Scanning %s rendered pages from %s\n\n' "$(ls -1 "$OUT" | wc -l | tr -d ' ')" "$BASE"
fi

# Findings cite the rendered .html; map them back to the Java page that emits it.
npx --yes impeccable@latest detect "$OUT" $JSON
