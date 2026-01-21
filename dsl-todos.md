# JWeb DSL Improvement Tracker

This document tracks completed improvements and remaining tasks for the JWeb framework DSL.

---

## Completed Improvements (This Session)

### JS DSL Core (JS.java)
- [x] Array methods: `filter`, `map`, `forEach`, `find`, `findIndex`, `some`, `every`, `reduce`, `slice`, `concat`, `join`, `reverse`, `sort`, `includes`, `first`, `last`, `push`, `pop`, `shift`, `unshift`, `flat`, `flatMap`, `atIndex`, `fill`, `copyWithin`, `splice`, `findLast`, `findLastIndex`, `toSorted`, `toReversed`, `indexOfVal`
- [x] String methods: `substring`, `charAt`, `indexOf`, `lastIndexOf`, `split`, `replace`, `replaceAll`, `startsWith`, `endsWith`, `includes`, `repeat`, `sliceStr`, `search`, `match`, `matchAll`, `normalize`, `trimStart`, `trimEnd`, `localeCompare`, `padStart`, `padEnd` (dynamic)
- [x] Object methods: `keys`, `values`, `entries`, `hasOwnProperty`
- [x] Number parsing/checking: `parseInt`, `parseFloat`, `isNaN`, `isFinite`, `numberIsNaN`, `numberIsFinite`, `numberIsInteger`, `numberIsSafeInteger`
- [x] Number formatting: `toFixed`, `toExponential`, `toPrecision`, `toStringRadix`
- [x] Object static methods: `objectAssign`, `objectFreeze`, `objectSeal`, `objectIs`, `objectCreate`, `objectGetOwnPropertyNames`, `objectGetPrototypeOf`, `objectSetPrototypeOf`, `objectIsFrozen`, `objectIsSealed`, `objectFromEntries`
- [x] Array static methods: `arrayFrom`, `arrayIsArray`, `arrayOf`

### New JS Modules Created
- [x] **JSAbort.java** - AbortController API for cancellable async operations
  - AbortController creation with builder pattern
  - `abortAfterTimeout`, `abortedSignal`, `anySignal`
  - Signal properties: `isAborted`, `reason`, `throwIfAborted`
  - Event listener support with `onAbort`

- [x] **JSGeolocation.java** - Geolocation API for location services
  - `getCurrentPosition`, `watchPosition`, `clearWatch`
  - Position properties: `latitude`, `longitude`, `accuracy`, `altitude`, `heading`, `speed`, `timestamp`
  - Error handling with error codes
  - Builder pattern with high accuracy, timeout, maximumAge options
  - `distanceBetween` utility (Haversine formula)

- [x] **JSNotification.java** - Browser Notification API
  - Permission management: `requestPermission`, `hasPermission`, `isDenied`, `isSupported`
  - NotificationBuilder with all options: `body`, `icon`, `badge`, `image`, `tag`, `lang`, `dir`, `data`, `requireInteraction`, `renotify`, `silent`
  - Event handlers: `onClick`, `onClose`, `onError`, `onShow`

- [x] **JSWebAnimations.java** - Web Animations API
  - `animate` with element and element ID support
  - KeyframeBuilder with all common CSS properties
  - Animation controls: `play`, `pause`, `reverse`, `cancel`, `finish`, `commitStyles`, `persist`
  - Animation properties: `playbackRate`, `currentTime`, `playState`, `finished`, `ready`
  - AnimationBuilder with duration, easing, delay, iterations, direction, fill options
  - Event handlers: `onFinish`, `onCancel`, `onRemove`

- [x] **JSVisibility.java** - Page Visibility API
  - `visibilityState`, `isHidden`, `isVisible`
  - Event listeners: `onVisibilityChange`, `onVisible`, `onHidden`
  - `whenVisible` for deferred execution
  - `visibilityAwareInterval` utility
  - Hidden time tracking

- [x] **JSFullscreen.java** - Fullscreen API
  - `requestFullscreen`, `exitFullscreen`, `toggleFullscreen`
  - State checks: `fullscreenElement`, `isFullscreen`, `isElementFullscreen`, `fullscreenEnabled`
  - Event listeners: `onFullscreenChange`, `onFullscreenError`
  - Element-specific listeners
  - `syncFullscreen`, `fullscreenButton` utilities

- [x] **JSShare.java** - Web Share API
  - ShareBuilder with `title`, `text`, `url`, `files`
  - Support checks: `isSupported`, `canShare`, `canShareFiles`
  - Direct share methods: `shareUrl`, `shareText`, `shareCurrentPage`
  - PWA share target helpers

### Updated JS Modules
- [x] **JSDate.java** - Enhanced date manipulation
  - UTC getters/setters
  - Week operations: `startOfWeek`, `startOfWeekMonday`, `endOfWeek`, `addWeeks`, `getWeekNumber`, `diffWeeks`
  - Quarter operations: `getQuarter`, `startOfQuarter`, `endOfQuarter`, `addQuarters`
  - Year operations: `startOfYear`, `endOfYear`, `isLeapYear`, `getDaysInMonth`
  - Additional formatting: `toUTCString`, `toJSON`, `toLocaleString`

- [x] **JSOperators.java** - Modern JavaScript operators
  - Bitwise operators: `bitwiseAnd`, `bitwiseOr`, `bitwiseXor`, `bitwiseNot`, `leftShift`, `rightShift`, `unsignedRightShift`
  - Bitwise assignment operators
  - Other operators: `pow`, `powAssign`, `in_`, `delete_`, `void_`, `comma`

- [x] **JSStorage.java** - Enhanced storage APIs
  - Storage events: `onStorageChange`, `onStorageKeyChange`, event property getters
  - Storage iteration: `forEachKey`, `getAllKeys`, `toObject`
  - CookieBuilder with all options: `path`, `domain`, `maxAge`, `days`, `expires`, `secure`, `sameSite`
  - Storage quota: `estimateQuota`, `isPersisted`, `requestPersist`

- [x] **JSIntl.java** - Comprehensive internationalization
  - Display names: `languageName`, `regionName`, `scriptName`, `currencyName`, `calendarName`, `dateTimeFieldName`
  - Collator for sorting: `collator`, `localeCompare`, `comparator`, `numericComparator`, `localeSortedArray`
  - Unit formatting: shortcuts for km, miles, meters, feet, kg, lbs, liters, gallons, celsius, fahrenheit, bytes, etc.
  - Currency display options
  - Format to parts: `formatNumberToParts`, `formatDateToParts`
  - Segmenter: `segmentGraphemes`, `segmentWords`, `segmentSentences`, `graphemeCount`
  - Locale negotiation: `matchLocale`, `canonicalLocale`, `canonicalLocales`

- [x] **Events.java** - Enhanced event handling
  - Keyboard events: `onKeyCombo`, `onKey`, `onEscape`, `onEnter`, key/modifier getters
  - Touch events: `onTouchStart`, `onTouchMove`, `onTouchEnd`, `onTouchCancel`, touch coordinate getters
  - SwipeBuilder for swipe gesture detection
  - Event utilities: `preventDefault`, `stopPropagation`, `stopImmediatePropagation`
  - Custom events: `customEvent`, `dispatchCustomEvent`, `onCustomEvent`, `eventDetail`
  - Listener options: `once`, `capture`, `passive`

- [x] **JSRegex.java** - Enhanced regex support
  - Named regex groups: `NamedRegex` class with `group`, `groups`, `extractGroup`, `extractGroups`
  - Lookahead/lookbehind: `lookahead`, `negativeLookahead`, `lookbehind`, `negativeLookbehind`
  - Additional patterns: `HEX_COLOR`, `IPV4`, `IPV6`, `DATE_ISO`, `TIME_24H`, `CREDIT_CARD`, `USERNAME`, `PASSWORD_STRONG`, `WHITESPACE`, `NON_ALPHANUMERIC`
  - Regex utilities: `escapeRegex`, `safeRegex`, `isValidRegex`, `countMatches`

---

## Remaining Tasks (Future Improvements)

### High Priority

#### JS DSL Gaps
- [ ] **JSPromise.java** - Dedicated Promise utilities beyond Async.java
  - Promise.withResolvers()
  - Cancellable promises
  - Promise timeout wrapper
  - Retry logic

- [ ] **JSIterator.java** - Iterator and Generator support
  - Generator functions
  - Async iterators
  - Iterator helpers (map, filter, take, drop, etc.)

- [ ] **JSProxy.java** - Proxy and Reflect API
  - Proxy creation
  - Common handler patterns
  - Reflect methods

- [ ] **JSWorker.java** - Web Workers API
  - Worker creation
  - Message passing
  - SharedWorker support
  - Transferable objects

- [ ] **JSServiceWorker.java** - Service Worker API
  - Registration
  - Cache API
  - Push notifications
  - Background sync

### Medium Priority

#### JS DSL Enhancements
- [ ] **JSMedia.java** - Media APIs
  - Audio/Video element control
  - MediaRecorder API
  - Media Session API
  - Picture-in-Picture

- [ ] **JSCanvas.java** - Canvas API
  - 2D context methods
  - Path operations
  - Drawing utilities
  - Image manipulation

- [ ] **JSWebRTC.java** - WebRTC API
  - RTCPeerConnection
  - Media streams
  - Data channels

- [ ] **JSCrypto.java** - Web Crypto API
  - Hashing
  - Encryption/Decryption
  - Key generation
  - Random values

- [ ] **JSPerformance.java** - Performance API
  - Performance marks/measures
  - Navigation timing
  - Resource timing
  - Long tasks API

- [ ] **JSIntersectionObserver.java** - Intersection Observer
  - Observer creation
  - Entry properties
  - Common patterns (lazy loading, infinite scroll)

### Lower Priority

#### CSS DSL Improvements
- [ ] Add missing CSS properties to Style mixins
  - Container queries support
  - Subgrid support
  - View transitions API
  - Scroll-driven animations
  - CSS anchor positioning

- [ ] **CSSVariables.java** - CSS Custom Properties
  - Variable declaration
  - Variable usage
  - Fallback values
  - Computed styles

- [ ] **CSSAnimations.java** - Enhanced CSS animations
  - Animation sequencing
  - Animation events
  - Motion path

#### HTML DSL Improvements
- [ ] Add missing HTML5 elements to El
  - `<dialog>` element
  - `<details>` / `<summary>`
  - `<meter>` / `<progress>`
  - `<template>` / `<slot>`

- [ ] Form enhancements
  - File input helpers
  - Form validation integration
  - FormData builder

#### Framework Integration
- [ ] Better HTMX integration patterns
- [ ] Alpine.js interop helpers
- [ ] SSE/WebSocket component patterns
- [ ] SPA routing helpers

---

## Documentation Tasks
- [ ] Add JSDoc to all new modules
- [ ] Create usage examples in docs
- [ ] Update CLAUDE.md with new modules
- [ ] Create migration guide for major changes

---

## Testing Tasks
- [ ] Unit tests for new JS modules
- [ ] Integration tests for browser APIs
- [ ] Snapshot tests for generated JS output

---

## Notes

### Completed Module Count
- **JS modules**: 23 files
- **New modules this session**: 7 (JSAbort, JSGeolocation, JSNotification, JSWebAnimations, JSVisibility, JSFullscreen, JSShare)
- **Enhanced modules this session**: 6 (JS.java, JSDate, JSOperators, JSStorage, JSIntl, Events, JSRegex)

### Module Locations
All JS DSL modules are located at:
```
src/main/java/com/osmig/Jweb/framework/js/
```

### Usage Pattern
```java
import static com.osmig.Jweb.framework.js.JS.*;
import static com.osmig.Jweb.framework.js.Async.*;
import static com.osmig.Jweb.framework.js.Events.*;
import static com.osmig.Jweb.framework.js.JSDate.*;
// etc.
```

---

*Last updated: 2026-01-21*
