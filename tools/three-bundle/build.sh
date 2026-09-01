#!/bin/sh
# Builds the vendored three.js bundle JWeb serves at /jweb/three-bundle.js.
# Framework-dev tool only — users never run this; the output is committed at
# src/main/resources/jweb/three-bundle.min.js and ships inside the jar.
#
# To upgrade three.js: bump the version in package.json AND in
# framework/three/ThreeAssets.THREE_VERSION (the cache-busting ?v= param),
# then re-run this script and commit both.
set -e
cd "$(dirname "$0")"

npm install --no-audit --no-fund
VERSION=$(node -p "require('./node_modules/three/package.json').version")

npx --yes esbuild entry.js \
  --bundle --minify --format=iife --global-name=THREE \
  --banner:js="/* three.js ${VERSION} + OrbitControls — MIT (c) 2010-2026 three.js authors — bundled for JWeb */" \
  --outfile=../../src/main/resources/jweb/three-bundle.min.js

echo "Built three ${VERSION} -> src/main/resources/jweb/three-bundle.min.js"
