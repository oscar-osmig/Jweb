# JWeb DSL Improvement Tracker

This document tracks completed improvements and remaining tasks for the JWeb framework DSL.

---

## Completed Improvements (Previous Sessions)

### JS DSL Core (JS.java)
- [x] Array methods: `filter`, `map`, `forEach`, `find`, `findIndex`, `some`, `every`, `reduce`, `slice`, `concat`, `join`, `reverse`, `sort`, `includes`, `first`, `last`, `push`, `pop`, `shift`, `unshift`, `flat`, `flatMap`, `atIndex`, `fill`, `copyWithin`, `splice`, `findLast`, `findLastIndex`, `toSorted`, `toReversed`, `indexOfVal`
- [x] String methods: `substring`, `charAt`, `indexOf`, `lastIndexOf`, `split`, `replace`, `replaceAll`, `startsWith`, `endsWith`, `includes`, `repeat`, `sliceStr`, `search`, `match`, `matchAll`, `normalize`, `trimStart`, `trimEnd`, `localeCompare`, `padStart`, `padEnd` (dynamic)
- [x] Object methods: `keys`, `values`, `entries`, `hasOwnProperty`
- [x] Number parsing/checking: `parseInt`, `parseFloat`, `isNaN`, `isFinite`, `numberIsNaN`, `numberIsFinite`, `numberIsInteger`, `numberIsSafeInteger`
- [x] Number formatting: `toFixed`, `toExponential`, `toPrecision`, `toStringRadix`
- [x] Object static methods: `objectAssign`, `objectFreeze`, `objectSeal`, `objectIs`, `objectCreate`, `objectGetOwnPropertyNames`, `objectGetPrototypeOf`, `objectSetPrototypeOf`, `objectIsFrozen`, `objectIsSealed`, `objectFromEntries`
- [x] Array static methods: `arrayFrom`, `arrayIsArray`, `arrayOf`

### Previously Created JS Modules
- [x] **JSAbort.java** - AbortController API for cancellable async operations
- [x] **JSGeolocation.java** - Geolocation API for location services
- [x] **JSNotification.java** - Browser Notification API
- [x] **JSWebAnimations.java** - Web Animations API
- [x] **JSVisibility.java** - Page Visibility API
- [x] **JSFullscreen.java** - Fullscreen API
- [x] **JSShare.java** - Web Share API

### Previously Updated JS Modules
- [x] **JSDate.java** - Enhanced date manipulation
- [x] **JSOperators.java** - Modern JavaScript operators
- [x] **JSStorage.java** - Enhanced storage APIs
- [x] **JSIntl.java** - Comprehensive internationalization
- [x] **Events.java** - Enhanced event handling
- [x] **JSRegex.java** - Enhanced regex support

---

## Completed Improvements (Current Session - 2026-01-21)

### New JS Modules Created (10 modules)

- [x] **JSPromise.java** - Comprehensive Promise utilities
  - Promise.withResolvers() / deferred pattern
  - Cancellable promises with AbortController integration
  - Timeout promises with Promise.race
  - Retry logic with exponential backoff
  - Promise combinators: all, race, allSettled, any
  - Promise chaining helpers with .then/.catch/.finally
  - Delay/sleep, event-to-promise conversions
  - Debounce/throttle promise patterns, memoization

- [x] **JSIterator.java** - Iterator and Generator support
  - Iterator protocol (Symbol.iterator, next, done, value)
  - Generator functions (function*, yield, yield*)
  - Async iterators and async generators
  - Iterator helpers (from, map, filter, take, drop, forEach, reduce, toArray)
  - Creating iterable objects
  - For...of and for await...of loops
  - Range helpers, infinite iterators (count, cycle, repeat)
  - Chain, zip, enumerate utilities

- [x] **JSProxy.java** - Proxy and Reflect API
  - new Proxy(target, handler) with all 13 handler traps
  - Proxy.revocable for revocable proxies
  - Complete Reflect API (get, set, has, deleteProperty, apply, construct, etc.)
  - Property descriptor builders (data and accessor descriptors)

- [x] **JSWorker.java** - Web Workers API
  - Dedicated workers with message passing
  - SharedWorker for cross-tab communication
  - MessageChannel for two-way communication
  - BroadcastChannel for simple cross-tab messaging
  - Inline workers (Blob workers)
  - Transferable objects support

- [x] **JSServiceWorker.java** - Service Worker API
  - Registration, ready, controller, getRegistration(s)
  - Service worker lifecycle events (install, activate, fetch, message, push, sync)
  - Cache API (open, match, add, addAll, put, delete, keys)
  - Push API for push notifications
  - Background Sync API
  - Notification event handling

- [x] **JSMedia.java** - Audio/Video and Media APIs
  - HTMLMediaElement control (play, pause, load, volume, etc.)
  - All media events (play, pause, ended, timeupdate, etc.)
  - MediaRecorder API with all options
  - Picture-in-Picture API
  - Media Session API with action handlers
  - Web Audio API basics (AudioContext, nodes, connections)

- [x] **JSCanvas.java** - Canvas 2D API
  - Context management and OffscreenCanvas
  - Rectangle drawing (fill, stroke, clear)
  - Path drawing (beginPath, moveTo, lineTo, arc, bezier, etc.)
  - Styles (fill, stroke, line properties, shadows)
  - Gradients (linear, radial, conic) and patterns
  - Text rendering and measurement
  - Image drawing and pixel manipulation
  - Transformations (save, restore, scale, rotate, translate, transform)
  - Compositing and clipping

- [x] **JSWebRTC.java** - WebRTC API
  - RTCPeerConnection with ICE servers
  - createOffer/createAnswer, setLocalDescription/setRemoteDescription
  - Media streams (getUserMedia, getDisplayMedia)
  - RTCDataChannel with full options
  - SDP handling (RTCSessionDescription)
  - ICE candidate handling
  - Media constraints builders

- [x] **JSCrypto.java** - Web Crypto API
  - Random values (getRandomValues, randomUUID)
  - Hashing (SHA-1, SHA-256, SHA-384, SHA-512)
  - Encryption/decryption (AES-GCM, AES-CBC, AES-CTR, RSA-OAEP)
  - Signing/verification (HMAC, RSA-PSS, ECDSA)
  - Key generation (AES, HMAC, RSA, ECDSA, ECDH)
  - Key derivation (PBKDF2, HKDF, ECDH)
  - Key import/export (raw, PKCS8, SPKI, JWK)
  - Key wrapping/unwrapping
  - Encoding helpers (TextEncoder/Decoder, Base64, Hex)

- [x] **JSPerformance.java** - Performance API
  - High resolution time (now, timeOrigin)
  - User Timing API (mark, measure, clear, getEntries)
  - Navigation Timing (all 25 timing properties)
  - Resource Timing with buffer management
  - Paint Timing (first-paint, first-contentful-paint)
  - Long Tasks API, Element Timing, LCP
  - PerformanceObserver with builder pattern
  - Memory API (Chrome)
  - Utility calculations (page load time, TTFB, etc.)

### CSS DSL Improvements

- [x] **View Transition Pseudo-Elements** (Selectors.java, CSS.java)
  - ::view-transition, ::view-transition-group
  - ::view-transition-image-pair, ::view-transition-old, ::view-transition-new

- [x] **CSSScope.java** - @scope rule support
  - Scoping CSS to specific DOM subtrees
  - Scope root and limit selectors

- [x] **CSSProperty.java** - @property rule support
  - Custom property registration with syntax validation
  - All syntax types (<color>, <length>, <number>, etc.)

- [x] **light-dark() function** (CSSColors.java)
  - Automatic color switching for light/dark themes

- [x] **CSSVariables.java** - CSS Custom Properties utilities
  - Variable definition and reference (var() with fallbacks)
  - Environment variables (env())
  - Variable naming helpers (scoped, component, theme)
  - Design System builder (spacing, colors, typography, shadows, etc.)
  - Theme builder (light/dark mode variable sets)
  - Common variable patterns

- [x] **CSSAnimations.java** - Enhanced CSS animations
  - Timing functions (ease, linear, cubic-bezier, steps)
  - Animation properties (duration, delay, iteration, direction, fill, play-state)
  - Scroll-driven animations (scroll(), view(), animation-range)
  - Transition utilities (allow-discrete)
  - AnimationBuilder for complex animations
  - 40+ pre-built animations (fade, slide, scale, rotate, flip, shake, bounce, etc.)
  - Animation composition, sequencing, staggering

### HTML DSL Improvements

- [x] **ModernElements.java** - Modern HTML5 elements
  - Dialog element with open attribute
  - Details/Summary with exclusive accordion support
  - Meter element (value, min, max, low, high, optimum)
  - Progress element (determinate and indeterminate)
  - Template element with shadowrootmode
  - Slot element for web components
  - Output element for form calculations
  - Data/Time elements with semantic values
  - Text direction (bdi, bdo)
  - Ruby annotation (ruby, rt, rp)

- [x] **DialogHelper.java** - Dialog JavaScript helpers
  - showModal(), show(), close(), toggle()
  - closeOnBackdropClick(), getReturnValue(), isOpen()

- [x] **DetailsHelper.java** - Details JavaScript helpers
  - open(), close(), toggle()
  - openExclusive(), closeAll(), openAll()

- [x] **El.java Updates** - Added to main entry point
  - All ModernElements methods
  - hgroup(), search(), address(), time(), wbr()

- [x] **Attributes.java Updates**
  - Modern attributes: open, value(double), low, high, optimum, shadowrootmode, datetime
  - Event handlers: onToggle, onCancel, onClose

---

## Remaining Tasks (Future Improvements)

### Lower Priority

#### Framework Integration
- [ ] Better HTMX integration patterns
- [ ] Alpine.js interop helpers
- [ ] SSE/WebSocket component patterns
- [ ] SPA routing helpers

---

## Documentation Created

- [x] CSS_DSL_ENHANCEMENTS.md - Modern CSS features documentation
- [x] MODERN_CSS_EXAMPLE.java - Complete working example
- [x] MODERN_ELEMENTS.md - HTML modern elements guide
- [x] MODERN_HTML_ENHANCEMENTS.md - Summary of HTML changes
- [x] Updated dsl-todos.md (this file)

---

## Module Summary

### JS Modules Created This Session: 10
- JSPromise.java
- JSIterator.java
- JSProxy.java
- JSWorker.java
- JSServiceWorker.java
- JSMedia.java
- JSCanvas.java
- JSWebRTC.java
- JSCrypto.java
- JSPerformance.java

### CSS Modules Created This Session: 4
- CSSScope.java
- CSSProperty.java
- CSSVariables.java
- CSSAnimations.java

### HTML Modules Created This Session: 3
- ModernElements.java
- DialogHelper.java
- DetailsHelper.java

### Total New Modules This Session: 17

### Total JS Modules: 33 files
Previous: 23 + New: 10 = 33

### Module Locations
```
JS DSL:  src/main/java/com/osmig/Jweb/framework/js/
CSS DSL: src/main/java/com/osmig/Jweb/framework/styles/
HTML DSL: src/main/java/com/osmig/Jweb/framework/elements/
```

### Usage Pattern
```java
// JS DSL
import static com.osmig.Jweb.framework.js.JS.*;
import static com.osmig.Jweb.framework.js.JSPromise.*;
import static com.osmig.Jweb.framework.js.JSCanvas.*;
import static com.osmig.Jweb.framework.js.JSCrypto.*;
// etc.

// CSS DSL
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

// HTML DSL
import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.ModernElements.*;
```

---

*Last updated: 2026-01-21*
