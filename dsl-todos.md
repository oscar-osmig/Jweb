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

## Completed Improvements (Session - 2026-01-21)

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

### High Priority - JavaScript DSL

#### New Browser APIs

- [ ] **JSIndexedDB.java** - IndexedDB API for client-side storage
  - Database open/create/delete
  - Object store operations (add, put, get, delete, clear)
  - Index creation and querying
  - Transactions (readonly, readwrite, versionchange)
  - Cursor iteration patterns
  - IDBKeyRange for range queries

- [ ] **JSHistory.java** - History API enhancements
  - pushState/replaceState with type-safe state objects
  - popstate event handling patterns
  - Navigation guards and confirmations
  - Query parameter manipulation
  - Hash change handling
  - Browser back/forward detection

- [ ] **JSDragDrop.java** - Drag and Drop API
  - Draggable elements configuration
  - Drop zones with validation
  - DataTransfer API (setData, getData, types, files)
  - Drag events (dragstart, drag, dragenter, dragover, dragleave, drop, dragend)
  - Custom drag images
  - Touch-to-drag fallbacks

- [ ] **JSPointer.java** - Pointer Events API
  - Unified mouse/touch/pen events
  - Pointer capture (setPointerCapture, releasePointerCapture)
  - Pressure sensitivity
  - Tilt and twist detection
  - Multi-pointer tracking

- [ ] **JSSpeech.java** - Web Speech API
  - Speech synthesis (text-to-speech)
  - Voice selection and configuration
  - Speech recognition (speech-to-text)
  - Continuous vs single-shot recognition
  - Interim results handling

- [ ] **JSBluetooth.java** - Web Bluetooth API
  - Device discovery and filtering
  - GATT server connections
  - Service and characteristic reading/writing
  - Notification subscriptions

- [ ] **JSUSB.java** - WebUSB API
  - Device enumeration
  - Interface claiming
  - Bulk/interrupt transfers

- [ ] **JSNFC.java** - Web NFC API
  - NDEF message reading
  - NDEF message writing
  - Tag scanning

- [ ] **JSSerial.java** - Web Serial API
  - Port selection and opening
  - Read/write streams
  - Signal control (DTR, RTS)

#### JS DSL Core Improvements

- [ ] **Enhanced Template Literals**
  - Tagged template literal support
  - String interpolation with expressions
  - Multi-line template handling

- [ ] **Class DSL**
  - Class declaration builder
  - Constructor methods
  - Getters/setters
  - Static methods and properties
  - Private fields (#field)
  - Inheritance (extends)

- [ ] **Module DSL**
  - Import statements (named, default, namespace)
  - Export statements
  - Dynamic imports
  - Module bundling helpers

- [ ] **Destructuring Helpers**
  - Array destructuring
  - Object destructuring
  - Default values
  - Nested destructuring
  - Rest patterns

- [ ] **Enhanced Async Patterns**
  - Top-level await wrappers
  - Async iteration helpers
  - AbortController integration improvements
  - Timeout/deadline patterns
  - Concurrent limit helpers (Promise pool)

---

### Medium Priority - CSS DSL

#### New CSS Features

- [ ] **CSSAnchorPositioning.java** - CSS Anchor Positioning
  - anchor() function
  - anchor-name property
  - position-area property
  - Fallback positioning
  - Inset area helpers

- [ ] **CSSScrollSnap.java** - Enhanced Scroll Snap
  - scroll-snap-type (mandatory, proximity)
  - scroll-snap-align
  - scroll-snap-stop
  - scroll-padding
  - Snap point helpers

- [ ] **CSSTextWrap.java** - Modern Text Wrapping
  - text-wrap: balance
  - text-wrap: pretty
  - white-space-collapse
  - word-break enhancements

- [ ] **CSSSubgrid.java** - CSS Subgrid Support
  - subgrid value for grid-template-columns/rows
  - Named line references
  - Alignment inheritance

- [ ] **CSSMasking.java** - CSS Masking and Clipping
  - mask-image
  - mask-mode
  - mask-position, mask-size
  - mask-composite
  - clip-path enhancements

- [ ] **CSSLogicalProperties.java** - Logical Properties
  - inline-size, block-size
  - margin-inline, margin-block
  - padding-inline, padding-block
  - inset-inline, inset-block
  - border-inline, border-block

- [ ] **CSSColorMix.java** - Color Manipulation
  - color-mix() function
  - color() function with color spaces
  - relative colors (from keyword)
  - Color contrast functions

- [ ] **CSSCounterStyle.java** - Custom Counter Styles
  - @counter-style rule
  - symbols(), additive-symbols()
  - suffix, prefix
  - Range specification

#### CSS DSL Improvements

- [ ] **MediaQuery Enhancements**
  - prefers-reduced-data
  - prefers-reduced-transparency
  - forced-colors detection
  - scripting detection
  - Custom media queries

- [ ] **Container Query Enhancements**
  - Container style queries
  - Container scroll-state queries
  - Nested container queries

- [ ] **Style Mixin Improvements**
  - accent-color support
  - color-scheme property
  - print-color-adjust
  - forced-color-adjust
  - content-visibility

---

### Medium Priority - HTML DSL

#### New HTML Elements

- [ ] **PopoverElements.java** - Popover API
  - popover attribute (auto, manual)
  - popovertarget attribute
  - popovertargetaction attribute
  - Popover events (toggle, beforetoggle)
  - Popover positioning helpers

- [ ] **PictureElements.java** - Responsive Images
  - picture element
  - source element with srcset and media
  - Art direction patterns
  - Lazy loading helpers

- [ ] **FigureElements.java** - Figure and Caption
  - figure element
  - figcaption element
  - Semantic grouping helpers

- [ ] **DefinitionElements.java** - Definition Lists
  - dl, dt, dd elements
  - Term/definition grouping

- [ ] **InteractiveElements.java** - Interactive Elements
  - abbr (abbreviation)
  - dfn (definition)
  - cite (citation)
  - q (inline quotation)
  - blockquote improvements
  - kbd, samp, var elements

- [ ] **FormEnhancements.java** - Modern Form Features
  - datalist element for autocomplete
  - optgroup for option grouping
  - fieldset/legend improvements
  - input type="color" helpers
  - input type="date/time" helpers
  - input type="range" helpers
  - formaction, formmethod attributes

- [ ] **EmbedElements.java** - Embedding Content
  - object element
  - embed element
  - param element
  - map/area for image maps

---

### Lower Priority - Framework Integration

#### HTMX Integration
- [ ] **HTMXAttributes.java** - HTMX attribute helpers
  - hx-get, hx-post, hx-put, hx-patch, hx-delete
  - hx-trigger with modifiers
  - hx-target, hx-swap
  - hx-indicator, hx-disabled-elt
  - hx-confirm, hx-prompt
  - hx-push-url, hx-replace-url
  - hx-sync, hx-validate
  - hx-boost, hx-preserve
  - Response headers helpers
  - Event handling patterns

#### Alpine.js Integration
- [ ] **AlpineAttributes.java** - Alpine.js attribute helpers
  - x-data, x-init
  - x-show, x-if, x-for
  - x-bind, x-on, x-model
  - x-text, x-html
  - x-ref, x-cloak
  - x-transition modifiers
  - $store, $dispatch helpers
  - Magic property shortcuts

#### SSE/WebSocket Patterns
- [ ] **SSEHelpers.java** - Server-Sent Events patterns
  - EventSource configuration
  - Auto-reconnection with backoff
  - Event type handling
  - Connection state management
  - Heartbeat patterns

- [ ] **WSHelpers.java** - WebSocket patterns
  - Connection lifecycle management
  - Auto-reconnection strategies
  - Message queuing during reconnect
  - Binary message handling
  - Ping/pong heartbeats
  - Room/channel patterns

#### SPA Routing
- [ ] **SPARouter.java** - Client-side routing helpers
  - Route definitions with path params
  - Navigation guards
  - Lazy loading patterns
  - Scroll position restoration
  - Active link highlighting
  - 404 handling

---

### Testing and Documentation

- [ ] **Test Coverage**
  - Unit tests for all new JS modules
  - Integration tests for CSS DSL
  - HTML element rendering tests
  - Browser compatibility verification

- [ ] **Documentation**
  - API documentation for new modules
  - Usage examples for each module
  - Migration guide for breaking changes
  - Best practices guide

---

## Module Summary

### Current Module Counts (as of 2026-01-23)

| DSL Category | Module Count | Location |
|--------------|--------------|----------|
| JavaScript DSL | 38 modules | `framework/js/` |
| CSS DSL | 29 modules | `framework/styles/` |
| HTML DSL | 18 modules | `framework/elements/` |
| Core Framework | 60+ modules | Various packages |
| **Total** | **140+ modules** | |

### JS Modules (38 total)
Core: JS.java, Actions.java, Async.java, Runtime.java, JWebRuntime.java, Events.java
Browser APIs: JSStorage.java, JSWebSocket.java, JSAnimation.java, JSWebAnimations.java, JSGeolocation.java, JSNotification.java, JSClipboard.java, JSFullscreen.java, JSShare.java, JSVisibility.java, JSAbort.java
Data: JSJson.java, JSFormData.java, JSUrl.java, JSDate.java, JSMath.java, JSRegex.java, JSIntl.java
Utilities: JSOperators.java, JSObservers.java, JSConsole.java, JSFile.java
Advanced (2026-01-21): JSPromise.java, JSIterator.java, JSProxy.java, JSWorker.java, JSServiceWorker.java, JSMedia.java, JSCanvas.java, JSWebRTC.java, JSCrypto.java, JSPerformance.java

### CSS Modules (29 total)
Core: CSS.java, Style.java, CSSValue.java, Stylesheet.java, StyledElement.java, Styles.java
Layout: StyleFlex.java, StyleGrid.java, StyleBoxModel.java, StylePosition.java, CSSGrid.java
Styling: StyleTypography.java, StyleEffects.java, CSSColors.java, CSSUnits.java
At-Rules: MediaQuery.java, Keyframes.java, ContainerQuery.java, Supports.java, CSSNested.java, CSSLayer.java, FontFace.java
Modern (2026-01-21): CSSScope.java, CSSProperty.java, CSSVariables.java, CSSAnimations.java
Other: Selectors.java, Theme.java, Utility.java

### HTML Modules (18 total)
Core: El.java, Tag.java, Elements.java, TextElement.java
Categories: DocumentElements.java, SemanticElements.java, TextElements.java, ListElements.java, TableElements.java, FormElements.java, MediaElements.java, SVGElements.java
Form Components: Input.java, Button.java, Form.java
Modern (2026-01-21): ModernElements.java, DialogHelper.java, DetailsHelper.java

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
import static com.osmig.Jweb.framework.js.Actions.*;
import static com.osmig.Jweb.framework.js.Async.*;
import static com.osmig.Jweb.framework.js.Events.*;
import static com.osmig.Jweb.framework.js.JSPromise.*;
import static com.osmig.Jweb.framework.js.JSCanvas.*;
import static com.osmig.Jweb.framework.js.JSCrypto.*;
// etc.

// CSS DSL
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

// HTML DSL
import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.ModernElements.*;
```

---

## Priority Guide

**High Priority** - Features that would significantly improve developer experience:
- IndexedDB for offline storage
- History API for SPA patterns
- Drag and Drop for interactive UIs
- CSS Anchor Positioning (new CSS feature)
- Popover API (new HTML feature)

**Medium Priority** - Useful features that extend capabilities:
- Speech APIs for accessibility
- CSS Scroll Snap improvements
- Logical properties for RTL support
- HTMX/Alpine.js integration

**Lower Priority** - Nice-to-have features:
- Hardware APIs (Bluetooth, USB, NFC, Serial)
- Advanced CSS features (masking, counter-styles)
- Testing infrastructure improvements

---

*Last updated: 2026-01-23*
