#!/bin/sh
# Prints one version's section of CHANGELOG.md — the body of its GitHub Release.
#
#   ./tools/release-notes.sh 3.0.2            # the "## [3.0.2] — date" section
#   ./tools/release-notes.sh 3.0.2 | gh release create v3.0.2 --verify-tag --title v3.0.2 --notes-file -
#
# The section runs from its "## [X.Y.Z]" heading (exclusive) to the next "## " heading or
# the reference-link block at the end of the file. Exits 1 when the version has no section.
set -eu

version="${1:?usage: release-notes.sh X.Y.Z}"
version="${version#v}"
changelog="$(dirname "$0")/../CHANGELOG.md"

awk -v v="$version" '
  /^## \[/ {
    if (found) exit
    found = ($0 ~ ("^## \\[" v "\\]"))
    next
  }
  found && /^\[[^]]+\]: / { exit }
  found { lines[++n] = $0 }
  END {
    # trim leading and trailing blank lines
    first = 1; last = n
    while (first <= last && lines[first] ~ /^[[:space:]]*$/) first++
    while (last >= first && lines[last] ~ /^[[:space:]]*$/) last--
    if (first > last) exit 1
    for (i = first; i <= last; i++) print lines[i]
  }
' "$changelog" || { echo "no CHANGELOG.md section for $version" >&2; exit 1; }
