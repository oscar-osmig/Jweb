[← Back to README](./../README.md)

# Three DSL — 3D scenes in pure Java

Declarative 3D rendered with [three.js](https://threejs.org), declared the way
you declare HTML. The scene graph is a tree; JWeb's nested varargs describe
trees — so a scene is just another element:

```java
import static jweb.El.*;
import static jweb.Css.*;
import static jweb.Three.*;

div(class_("hero"),
    scene(style().height(px(420)),
        camera().position(0, 1.5, 4).orbit(),
        directionalLight().position(3, 5, 2),
        box().color("#10b981").spin()
    )
)
```

That is a complete, animated, orbit-controllable 3D scene. The raw three.js
equivalent is ~30 lines of imperative ceremony — scene, camera, renderer,
canvas mounting, resize handling, a `requestAnimationFrame` loop, and manual
`dispose()` calls to avoid GPU memory leaks. The JWeb runtime owns all of it.

## How it works

`scene(...)` returns a regular element (a `div`) carrying the serialized scene
graph in a `data-three` attribute. On the client:

- The JWeb runtime notices `[data-three]` and lazy-loads the vendored three.js
  bundle (~170KB gzipped, immutable-cached, served from your own jar at
  `/jweb/three-bundle.js`) plus a small interpreter. **Pages without scenes
  load neither** — the zero-JS default is untouched.
- The interpreter builds the scene, sizes it to its container with a
  `ResizeObserver`, and renders. A render loop runs **only if something
  animates**; still scenes render on demand (on drag, on resize) and cost
  zero CPU otherwise.
- When a fragment swap or morph replaces or changes the scene element, the
  interpreter re-initializes from the new JSON; when the element leaves the
  DOM, everything is disposed — geometries, materials, textures, the WebGL
  context.

Because `scene(...)` is a `Tag`, every HTML attribute chains on it:
`.id("hero")`, `.class_("card")`, styles, anything.

## Shapes

| Factory | three.js geometry | Defaults |
|---|---|---|
| `box()` / `box(2)` / `box(w, h, d)` | BoxGeometry | 1×1×1 |
| `sphere()` / `sphere(r)` | SphereGeometry | radius 1 |
| `plane()` / `plane(w, h)` | PlaneGeometry | 1×1, double-sided |
| `cylinder()` / `cylinder(r, h)` / `.radii(top, bottom)` | CylinderGeometry | radius 1, height 1 |
| `cone()` / `cone(r, h)` | ConeGeometry | radius 1, height 1 |
| `torus()` / `torus(r, tube)` | TorusGeometry | radius 1, tube 0.4 |
| `torusKnot()` / `torusKnot(r, tube)` | TorusKnotGeometry | radius 1, tube 0.4 |
| `capsule()` / `capsule(r, length)` | CapsuleGeometry | radius 1, middle 1 |
| `disc()` / `disc(r)` | CircleGeometry | radius 1, double-sided |
| `ring()` / `ring(inner, outer)` | RingGeometry | 0.5 → 1, double-sided |
| `tetrahedron(r)` `octahedron(r)` `dodecahedron(r)` `icosahedron(r)` | the platonic solids | radius 1 |
| `terrain(w, d)` / `.hills(h)` / `.hills(h, scale)` | PlaneGeometry, displaced | flat, lying down |

(`disc`, not `circle` — the SVG element owns that name under dual wildcard
imports. Same reason the text billboard below isn't called `label`, and the
polyline is `wire`, not `line`.)

`terrain` is ground that already lies flat — y up, centered on its
position, no rotation to remember — with rolling hills on request:

```java
terrain(60, 60).color("#5B7F4A")                  // a lawn
terrain(60, 60).hills(1.5).color("#C9B58A")       // low dunes
terrain(60, 60).hills(3, 12).seed(4).detail(128)  // taller hills 12 units across,
                                                  // reshuffled, finer mesh
```

Hills are seeded and deterministic (a re-render doesn't reshape the land);
crests rise and troughs dip around the node's height by about half the hill
height each way. `detail` is the mesh resolution per side (default 96,
clamped 8–256). It's a normal mesh: shadows, clicks, hover and materials all
apply.

Every node shares the transform surface — chains never dead-end:

```java
sphere(0.5)
    .position(2, 1, 0)      // x right, y up, z toward the viewer
    .rotation(0, 45, 0)     // degrees — the DSL is degrees everywhere
    .scale(1.5)             // or .scale(x, y, z)
    .name("moon")           // reachable from scripts (see escape hatch)
```

**Angles are degrees end-to-end** (matching the CSS DSL); the runtime converts
to radians. A ground plane is `plane(30, 30).flat()` — the same as `.rotation(-90, 0, 0)`, named.

## Curves

Three factories cover the shapes you'd otherwise fake with chains of
rotated boxes and cylinders:

```java
// a round tube swept along a smooth curve through x,y,z points —
// vines, pipes, cables, ribs, railings. The curve passes through
// every point; .closed() joins it into a loop.
tube(0.05,
    -2, 0, 0,
     0, 1.4, 0,
     2, 0, 0)

// a partial ring — an archway is half a torus. Sweep is degrees,
// counter-clockwise from the ring's +x; rotate the node to hang it.
arc(1.6, 0.09, 180).position(0, 2.1, -4.5)

// a surface of revolution from radius,height pairs, bottom-up —
// pots, vases, columns, domes, bells. Start or end at radius 0 to
// close that end. Double-sided, so open vessels read from inside.
lathe(0, 0,   0.5, 0,   0.35, 0.8,   0.55, 1.1,   0.3, 1.3)
    .segments(48)   // radial resolution (default 32)
```

An elliptical vault is an `arc` squashed with `.scale(1, 0.7, 1)`. One
`tube` through five points replaces a dozen hand-angled segments — and
reads like what it is.

Two more ride the same smooth curve:

```java
// a rectangular profile (width × height) swept along the curve —
// moldings, ribs, rails, gutters, cornices. The square-section tube.
sweep(0.12, 0.06,  -3, 0, 0,  0, 2.2, 0,  3, 0, 0)   // one vault rib, not 216 boxes
    .closed()         // loop it
    .steps(64)        // resolution along the curve (default max(24, points × 8))

// a thin, unlit polyline — outlines, guides, constellations, the pencil
// line of a diagram. (`wire`, because SVG owns `line`.)
wire(-2, 0, 0,  0, 1.4, 0,  2, 0, 0)
    .color("#fde68a").opacity(0.8)
    .closed()                       // last point back to the first
    .dashed(0.2, 0.1)               // dash, gap in scene units
    .draw(1200)                     // draws itself over 1.2s on load; draw(ms, delayMs)
```

`draw` traces the line from nothing when the scene loads (or scrolls into
view); combined with `dashed`, the dashes settle in once the trace is done.
WebGL lines are always one pixel wide whatever the zoom — for a line with
body, use `tube`. Lines are visual only: no clicks, no hover, no shadows.

## Materials

Shapes use three.js's physically-based `MeshStandardMaterial`, with the
platform's own property names:

```java
sphere()
    .color("#f59e0b")           // any CSS color; hex(...) works too
    .metalness(0.7)             // 0 dielectric … 1 metal (default 0)
    .roughness(0.25)            // 0 mirror … 1 diffuse (default 1)
    .emissive("#331100")        // self-illuminated glow
    .opacity(0.85)              // enables transparency
    .wireframe()                // edges only
    .texture("/assets/crate.png")  // image as the material map
```

### Glass

```java
box().glass()                                  // clear: light passes through and refracts
sphere().glass(0.6).color("#cfe8ff")           // transmission 0–1, tinted
cylinder().glass().roughness(0.4)              // frosted (default roughness 0.05)
```

Real transmission (`MeshPhysicalMaterial`), not a see-through opacity trick
— the scene behind bends through it. It costs a second render of what's
behind the glass; a vase or two is fine, a glass floor is a choice.

### Material presets

Declare a surface once and hand it to any number of shapes:

```java
var brass = material().color("#A07C4B").metalness(0.85).roughness(0.35);
var frosted = material().glass(0.9).roughness(0.4);

box().material(brass)
sphere().material(brass).roughness(0.1)     // brass, but polished
cylinder().material(frosted)
```

A preset carries every material verb (`color`, `emissive`, `metalness`,
`roughness`, `opacity`, `wireframe`, `texture`, `glass`) and copies only the
properties it set, so explicit calls before or after it still win in call
order. It's a plain Java value — `var` needs no import.

## Billboards

Two nodes always face the camera — for annotating scenes:

```java
billboard("Sun")                             // canvas-rendered text, no font file
    .color("#fde68a")                        // text color (default white)
    .background("rgba(15,23,42,0.85)")       // rounded pill behind it (default none)
    .size(0.5)                               // height in scene units
    .position(0, 1.8, 0)

sprite("/assets/pin.png").size(0.6)          // an image; width in scene units,
    .position(2, 1, 0)                       // height keeps the image aspect
```

Both take the full transform/animation/click surface (`float_()` on a
billboard reads nicely). Size them with `.size(...)` — `.scale(...)` doesn't
apply to billboards.

## Lights and shadows

```java
directionalLight(1.2).position(5, 8, 4)   // sun-like, aims at the origin
ambientLight(0.4)                          // even fill
pointLight(2).position(0, 3, 0)            // bulb-like
hemisphereLight("#bde0fe", "#3a5a40")      // sky + ground wash
spotLight(40).position(0, 6, 0)            // a cone of light, aimed straight down
    .target(0, 1, 0)                       // ...or at a point
    .angle(25)                             // cone half-angle, degrees (default 30)
    .penumbra(0.4)                         // edge softness 0–1 (default 0.3)
    .shadows()
```

A scene that declares **no lights** gets a soft hemisphere light so nothing
renders black. Declare any light to take over completely. Point and spot
lights fall off with distance (physical units): a lamp a few units from
its subject wants an intensity in the tens, not `1`.

Shadows — normally a five-place configuration chore — are one call:

```java
directionalLight(1.2).position(5, 8, 4).shadows()
```

That single `.shadows()` enables the renderer's shadow map (soft PCF),
configures the light's shadow camera, and makes every mesh in the scene cast
and receive. `pointLight(...).shadows()` and `spotLight(...).shadows()`
work the same way.

## Camera

```java
camera()
    .position(6, 4, 8)
    .lookAt(0, 1, 0)       // default: the origin
    .fov(60)               // default 50
    .near(0.1).far(2000)   // defaults shown
    .orbit()               // drag to rotate/zoom (OrbitControls)
    .autoRotate()          // slowly circles on its own; implies orbit
```

`autoRotate(speed)` tunes the lap speed (2 ≈ one lap every 30s). A scene
without a camera gets one at `(0, 0, 5)` looking at the origin. One camera
per scene; the first wins.

Orbit takes limits, so a product viewer can't be zoomed through or flipped
under the floor:

```java
camera().orbit()
    .noZoom()            // the scroll wheel stays with the page
    .noPan()             // the subject stays centered
    .distance(2, 12)     // zoom clamps, camera-to-target
    .polar(20, 90)       // vertical swing, degrees from straight overhead
                         // (90 = never below the horizon)
```

### Walk mode

`walk(eyeHeight)` turns the scene into a place. The scene starts framed
exactly as declared; walking is toggled by any element carrying
`data-three-walk="<scene id>"` — a plain button, no script — or starts by
itself with `autoStart()`:

```java
scene(style().height(px(460)),
    camera().position(0, 2, 7).lookAt(0, 2, 0)
        .walk(1.7)                  // eye height; or walk(eye, speed, run)
        .bounds(-8, -8, 8, 8)       // fenced floor: minX, minZ, maxX, maxZ
        .autoStart()                // W A S D / arrows start walking, no toggle
        .clickToMove()              // double-click the ground to glide there
        .fly(4)                     // hold Space to float, up to 4 units
        .footsteps()                // synthesized steps; or footsteps(url[, volume])
        .sway(),                    // gentle idle drift while framed
    ...
).id("hall")

button(attrs().data("three-walk", "hall"), text("Walk here"))
```

While walking: **W A S D** (and ↑↓) move, **← →** turn, dragging looks
around, **Shift** runs, **Esc** steps back out to the framed view.

**Feet on the ground.** The walker's feet follow the surfaces underfoot —
steps, ramps, walkways, dune slopes. The eye rides at its height above the
highest upward-facing surface below it: a `terrain()`, stacked boxes, the
floor of a `model()`, anything the scene draws (surfaces under 50% opacity
don't count). Stepping in, the eye settles down to its height and the
gaze levels out. `ground(false)` keeps the eye at a fixed height instead.

**Solid things.** Mark what blocks the walker with `.solid()` — its
world-space footprint, so a whole `group(...)` is one obstacle — or
`.solid(radius)` for a round column or trunk. Things below knee height are
stepped over, things above head height walked under. `bounds(...)` stays
the outer fence; `radius(r)` is the body's radius (default 0.32).

**Where walking begins.** By default where the framed camera stands,
facing the way it looks. `spawn(x, z, yawDeg)` places the walker instead
(0° faces −z) — a visitor coming back through a doorway starts at that
doorway.

**Other inputs.** `pointerLock()` locks the pointer while walking so the
mouse alone looks (Esc releases it, clicking re-locks). `touch()` gives
phones a thumb-stick where the thumb lands on the left half of the scene
and drag-to-look on the right. `gamepad()`: left stick moves, right stick
looks, A floats, B runs.

**The page knows where you are.** The scene element, the toggle and
`<body>` carry `three-walking` while walking, and `three-key-w` / `-a` /
`-s` / `-d` / `-space` / `-shift` / `-arrowup`… while each key is down — a
HUD that lights its keys is CSS. The scene element always publishes the
camera's heading as CSS variables, `--three-yaw` and `--three-pitch`
(degrees; `0deg` looks down −z), updated whenever it turns or moves:

```css
.compass .needle { transform: rotate(var(--three-yaw)); }
```

The scene also dispatches bubbling events: `jweb:three-walk`
(`detail.walking`) and `jweb:three-look` (`detail.{yaw, pitch, x, y, z,
walking}`). Head-bob and `sway()` are skipped for visitors who prefer
reduced motion. From scripts: `JWebThree.setWalk(id, on)`,
`JWebThree.walking(id)` and `JWebThree.pose(id)`.

## Scene atmosphere

Scenes are **transparent over the page** by default — a scene in a hero
section shows the page background behind it. Opt into a backdrop:

```java
scene(style().height(px(420)),
    background("#0f172a"),
    fog("#0f172a", 10, 30),   // fade to the color between 10 and 30 units
    grid(),                   // 10×10 reference grid; grid(size, divisions)
    ...
)
```

Fog plus a matching background is the classic infinite-depth look. `grid()`
is a placement aid while composing — delete it when done.

An equirectangular panorama (a plain wide jpg/png) can light the scene —
metallic and glossy materials pick up its reflections:

```java
environment("/assets/studio.jpg")   // reflections only, backdrop unchanged
sky("/assets/dusk.jpg")             // the panorama as visible sky AND light
```

`sky(...)` plus `sphere().metalness(1).roughness(0.05)` is a mirror ball in
two lines.

### Glow and tone

```java
scene(bloom(),                       // bright emissives actually glow
    sphere().emissive("#5FA98A"),    // now a lantern, not a flat bright ball
    ...)
```

`bloom()` is an HDR pass composited before tone mapping (which it implies —
the ACES filmic curve). Dials: `bloom(strength)` or
`bloom(strength, radius, threshold)` — only pixels brighter than the
threshold bloom, so raising it keeps the glow on the truly luminous.
Defaults (0.7, 0.35, 0.85) are calibrated for tasteful lantern-light, not
music-video. The scene must still read with bloom off: bloom is the halo,
not the lamp. Bloom composites over an opaque backdrop — declare a
`background(...)`.

`toneMapped()` alone applies the same cinematic curve with no glow —
highlights roll off instead of clipping. `toneMapped(exposure)` dials
brightness around it.

### Mirrors

```java
plane(20, 20).flat().mirror().color("#4a443e")
```

A real-time planar reflection (three.js `Reflector`). `.color(...)` tints
it — darker is dimmer. `mirror()` is chrome-sharp; a strength turns it
satin:

```java
plane(20, 20).flat().mirror(0.4).color("#4a443e").roughness(0.7)   // polished stone
```

At a strength below 1 the reflection shows through a surface in the
plane's `color` and `roughness` — `0.4` reads as a polished floor rather
than a pool of mercury. Other material properties don't apply.

## Animation presets

```java
box().spin()            // 20°/s x, 30°/s y — the pleasant default
box().spin(45)          // y axis, degrees per second
box().spin(10, 20, 30)  // per axis
model("/assets/drone.glb").float_()          // gentle hover: ±0.25 units
sphere().float_(0.5, 1)                      // amplitude, cycles per second

sphere().pulse()                             // scale breathes ±8%, one cycle per 2s
sphere().pulse(0.15, 1)                      // ±amount (fraction), cycles per second
torus().emissive("#5FA98A").glow()           // emissive breathes 60%–100%, 0.6 cps
torus().emissive("#5FA98A").glow(0.5)        // cycles per second

box().appear(600)                            // scales in from nothing over 600ms on load
box().appear(600, 200)                       // ...after a 200ms delay
```

`delay(ms)` holds `pulse`, `glow`, `float_` and `spin` still before they
start, so a row can come alive one member at a time:

```java
repeat(6, i -> torus(0.5, 0.15).position(i * 1.4 - 3.5, 1, 0)
    .emissive("#5FA98A").glow(0.5).delay(i * 220))
```

`appear` touches scale only, so it composes with `float_` and `spin`;
`pulse` and `glow` wait for an `appear` to finish before they begin.

`follow` moves a node along a closed, smooth path, facing the way it goes:

```java
cone(0.2, 0.6).rotation(90, 0, 0)            // a cylinder/cone points along y;
    .follow(4,                               // 90° on x lays it along the path
        3, 2, -2,   4, 2.5, -1,   3, 3, 0,   2, 2.5, -1)   // one lap every 4s
```

The path owns position; the declared rotation becomes an offset from the
direction of travel (with none, the node's +z points along the path).

Presets are why the render loop exists at all: a scene with no `spin`,
`float_`, `pulse`, `glow`, `follow`, `autoRotate`, `.animate()`d model,
drifting `particles` or `sway()` renders once and sleeps — one-shots like
`appear` and `draw` run the loop only until they finish — and an animated
scene's loop pauses whenever it scrolls offscreen (IntersectionObserver),
so a 3D hero costs nothing once the reader is past it.

## Particles

A whole cloud of points as one node and one draw call — dust, spores,
rain, snow, embers — instead of a hundred tiny meshes:

```java
particles(140)                  // dust holding the light
    .color("#E7D6B1").size(0.02)
    .spread(6, 3.4, 10)         // the box they fill, centered on position
    .position(0, 1.9, 0)
    .drift()                    // slow in-place wander; drift(speed) scales it
    .opacity(0.75)

particles(400)                  // rain
    .color("#7FA8C0").size(0.02)
    .spread(12, 8, 12)
    .fall(3)                    // units/sec, wrapping back to the top
```

Positions are seeded deterministically — a re-render doesn't reshuffle the
sky; `.seed(n)` picks a different arrangement. `drift`/`fall` animate the
cloud (and keep the loop alive); a static cloud is free after its first
frame.

A palette gives each particle one of several colors, chosen with the same
seed as its position — embers in three oranges, confetti:

```java
particles(300).colors("#ff7a00", "#ffb347", "#ff3d00").size(0.06)
    .spread(4, 3, 4).drift(2)
```

## Groups

`group(...)` shares one transform across children — build a compound object,
then move or animate it as a unit:

```java
group(
    cylinder(0.1, 2).color("#8899aa"),               // mast
    cone(0.4, 0.6).position(0, 1.3, 0).color("#cc4444")  // tip
).position(-2, 0, 0).spin(15)
```

Groups nest. Transforms, presets and click handlers on a group apply to the
whole subtree.

Three composition helpers keep scene-building declarative when the scene
depends on state:

```java
group(myListOfNodes)                  // any Iterable — no toArray ceremony

when(doorOpen, doorway())             // the node, or nothing; null vanishes
when(aligned, () -> whale())          // Supplier: only built if needed

repeat(10, i -> cylinder(0.08, 1.5)   // a colonnade in one line
    .position(-2.7 + i * 0.6, 0.75, 0))
```

`scene(...)`, `group(...)` and `repeat(...)` all skip nulls, so a
conditional branch never needs an empty-group placeholder.

### Instanced groups

A group of hundreds of similar shapes can draw as GPU instances — one draw
call per distinct geometry-and-material, whatever the count:

```java
repeat(400, i -> cylinder(0.1, 2).color(i % 2 == 0 ? "#bbb" : "#999")
    .position(-20 + i % 40, 1, -10 + i / 40)).instanced()
```

Members are batched by shape, size and material; `color` may differ per
member for free. What members give up is individuality: an instanced
member can't be clicked, hovered, named or animated on its own — one
carrying `spin`, `float_`, `pulse`, `glow`, `appear`, `follow`, `onClick`,
a hover effect or a `name` falls back to a normal mesh (with one console
warning per group). The group itself still moves, spins and takes clicks
as a unit, and nested plain groups flatten into it.

## Models

```java
model("/assets/rocket.glb").scale(0.5).position(2, 0, 0).float_()
```

Loads a glTF file — the standard format every 3D tool (Blender, etc.)
exports. Transforms, presets and click handlers attach to a wrapper the model
loads into, so the scene behaves identically before and after the file
arrives; still scenes re-render when it lands.

Plain `.glb`/`.gltf` only: Draco-compressed geometry needs a decoder JWeb
does not ship. Export uncompressed (in Blender: leave "Compression" off).

Models that ship animation clips play them with one call:

```java
model("/assets/robot.glb").animate()          // every clip the file carries
model("/assets/robot.glb").animate("Walk")    // just the named clip
```

An animated model keeps the render loop alive like `spin()` does; a missing
clip name logs the clips the file actually has.

## Interactivity

Clicking a shape raycasts into the scene and dispatches through the same
pipelines as element events. Two flavors:

**Swap a fragment** — the 3D counterpart of `swap(url, target)` on an element:

```java
sphere().name("product")
    .clickSwap("/api/product/42", "#detail-panel")
```

**Run a server handler** — the same event pipeline as
`onClick(...)` on an element, over the live WebSocket:

```java
box().name("die").onClick(e -> rolls.set(rolls.get() + 1))
```

**Run a client-side Action** — the Actions DSL, dispatched in the browser
with no server round-trip (and CSP-safe, like every Action handler):

```java
sphere().onClick(toggle("info-panel"))          // import static jweb.Js.*
```

The handler's `event.value()` (and `dataset.mesh`) carry the node's `name`,
and `dataset.pose` where the camera stood (`"x,y,z,yaw"`). If several are
set on one node: server `onClick` wins, then the Action, then `clickSwap`,
then `link`. The cursor becomes a pointer over clickable objects. Clicks on
groups and models hit their whole subtree.

**Hover effects** are declared on the shape and run entirely client-side —
raycast on pointer move, applied on enter, restored on leave:

```java
torusKnot()
    .hoverScale(1.1)             // grows while hovered (any node type)
    .hoverEmissive("#4c1d95")    // glow highlight (meshes)
    .hoverColor("#f43f5e")       // or a straight color swap (meshes)
    .onClick(show("hint"))
```

### Places that react

Three more ways a node answers the visitor — none of them a click:

```java
// a painting that is a doorway: clicking navigates. <body> gets
// three-crossing first so CSS can fade the way out; with the Navigation
// script on the page the hop uses its view transition. A drag that ends
// on it is not a click.
plane(2, 3).name("tide").link("/worlds/tide-archive")

// distance: within 3 units the scene element and <body> carry
// three-near-veil and a jweb:three-near event fires (again on leaving,
// detail.inside === false) — a veil that brightens as you approach, in CSS
plane(2, 3).name("veil").near(3)

// …or run handlers on the way in and out
sphere().name("lamp")
    .onNear(2.5, e -> tour.set("lamp"))     // server, over the socket
    .onFar(hide("lamp-caption"))            // or an Action, client-side

// a floor region: the door home, a trigger. Edges are x and z on the
// ground, any height. Being inside on first render doesn't count as
// entering, so a link never fires on load.
zone(-1.6, 14.9, 1.6, 16).name("back-door").link("/")
zone(-3, -3, 3, 3).name("plinth").onEnter(e -> ...).onLeave(e -> ...)
```

Handlers get the node's name in `event.value()` and the camera's place in
`dataset.pose`. Proximity is evaluated whenever the camera moves —
walking, a camera patch, orbit, `sway()` — with a little hysteresis so
standing on the line doesn't flicker. Zones draw nothing; they cost
nothing.

## Sound

```java
sound("/audio/sea.mp3").loop().volume(0.4)                        // everywhere
sound("/audio/fountain.mp3").loop().position(4, 1, -6).range(3)   // from the fountain
sound("/audio/bell.mp3").name("bell").paused()                    // played by a patch
```

A sound with a `.position(...)` is positional: it plays from there, louder
as the camera nears; `range(distance)` is where it's at full volume
(default 4). Browsers start audio only inside a user gesture, so every
sound waits for the visitor's first click, tap or key press and begins
then; `paused()` waits for a patch instead. Live, from any server handler:

```java
Three.patch("hall").node("sea").volume(0.1).tween(800)   // fade the sea
                   .node("bell").play()                   // ring the bell
```

`camera().walk(...).footsteps()` adds a synthesized step on every stride —
a soft scuff and thud, no file — or `footsteps(url[, volume])` plays a
clip. Steps pause while floating. `JWebThree.mute(id, on)` silences a
scene's sounds from a script.

## Live patches

The problem with state-driven scenes used to be that changing state meant
re-rendering the page — and the visitor's camera, walk position and every
animation phase reset with it. `Three.patch` updates the **live** scene
instead, from inside a server event handler, riding the WebSocket answer
that's already in flight:

```java
sphere().name("lantern").onClick(e ->
    Three.patch("hall")
         .node("lantern").emissive("#22d3ee").color("#a5f3fc").tween(500)
         .node("key-light").intensity(1.6).tween(500))
```

No reload, no scene rebuild — the lantern re-lights in place, mid-walk.
Targets are nodes carrying `.name(...)`. Per node:
`position / rotation / scale` (degrees, like everywhere),
`color / emissive / opacity` (meshes), `color / intensity` (lights),
`visible(boolean)`. `tween(ms)` eases the current target's changes;
without it they apply instantly.

`.camera()` glides the framing — `position`, `lookAt`, `tween`:

```java
button(onClick(e ->
    Three.patch("hall").camera()
         .position(0, 2.2, -0.6).lookAt(0, 2.1, -4.5).tween(1200)),
    "Approach the arch")
```

Camera patches respect whoever owns the camera: under OrbitControls they
reposition through the controls (instantly, so the two don't fight), and
while the visitor is walking they only update the framed view that Esc
returns to.

Patches work from any server event handler — a scene click, a button's
`onClick(Consumer)`, a form submit. They ride the event's own socket
answer, so outside a handler there's no page in flight and the patch is
dropped with a warning. Position patches on a `float_()`ing node move its
hover base along x/z and re-center the bob at the new height.

### Adding and removing nodes

The scene's structure changes the same way — nodes are built or disposed
in place, camera and animation phases untouched:

```java
Three.patch("hall")
     .add(sphere(0.3).name("orb").emissive("#5FA98A").appear(400))   // into the scene root
     .addTo("shelf", box(0.4).name("crate"))                        // inside a named group
     .remove("old-lamp", "dust")                                     // the whole subtree
     .replace("vase", lathe(0, 0, 0.5, 0, 0.3, 1.2).color("#8a6"))   // same parent, same name
     .node("orb").position(0, 2, 0).tween(600)                       // and patch what you added
```

Added nodes are exactly what `scene(...)` would have built — presets,
clicks, hover, shadows, all of it. Removal releases the node's geometry,
materials and textures and stops its animations. Within one patch removes
apply first, then additions, then property patches — so an `add` and a
`.node(...)` on the new name compose. `replace` gives the new node the old
name unless it carries its own. An unknown group in `addTo` logs a warning
and skips.

## Escape hatch: the raw three.js API

The DSL covers scenes, not shaders. When you need the full API, give the
scene an id — the live objects are exposed to scripts:

```java
scene(...).id("hero")
```

```java
// JWebThree.get('hero') -> {scene, camera, renderer, controls, objects}
script().unsafeRaw("""
    var hero = JWebThree.get('hero');
    hero.objects.moon.material.emissiveIntensity = 2;
    """)
```

`objects` maps every `.name(...)`d node to its three.js object. From there
the entire three.js API is yours; the runtime still owns sizing, the loop
and disposal.

Two interop courtesies, so page scripts never poll and never resort to
prototype tricks:

```js
// runs when the scene exists — immediately if it already does
JWebThree.ready('hero', h => {
    const v = new JWebThree.THREE.Vector3(0, 1, 0);   // the real module
    h.objects.moon.position.add(v);
});
```

`JWebThree.THREE` is the bundled three.js itself (`Vector3`, `Color`,
materials, everything). `setWalk(id, on)` and `walking(id)` control walk
mode from scripts when a `data-three-walk` element isn't enough;
`pose(id)` is where the camera is and looks (`{x, y, z, yaw, pitch,
walking}`), `mute(id, on)` silences the scene's sounds.

## Sizing

Give a scene a height (style, class — anything CSS). The canvas fills its
container and tracks it responsively. A zero-height container gets a 320px
minimum and a console hint rather than an invisible scene.

## Performance notes

- The bundle is ~798KB raw / ~205KB gzipped, cached immutably per version —
  one fetch per browser per upgrade, and only on pages that use scenes.
- Still scenes render on demand: zero CPU, zero battery until interaction.
  Live patches without a tween apply and render exactly one frame; tweens
  run the loop only until they finish.
- Animated scenes pause offscreen — the loop stops while the element is
  scrolled out of view and resumes when it returns.
- `bloom()` renders the scene through an HDR composer — roughly the cost of
  a second render at bloom resolution. One bloomed hero is fine; five
  bloomed scenes on one page is a choice.
- A `mirror()` re-renders the scene from the reflected view — same
  order-of-magnitude note as bloom.
- `particles(n)` is one draw call regardless of `n`; thousands are cheap.
  So is an `.instanced()` group — a few calls for hundreds of shapes.
- `glass()` renders what's behind it into a buffer first (three.js
  transmission) — like bloom, a per-frame cost that scales with how much
  glass is on screen.
- `terrain` is a mesh of `detail²` quads (96² by default); it builds once
  and costs nothing after.
- Pixel ratio is capped at 2 to keep retina laptops cool.
- Disposal is automatic and complete — geometries, materials, textures,
  composer targets and the GL context are released when the element leaves
  the DOM, including through fragment swaps.

## Limitations

- WebGL is required (universally available; scenes fail with a console error,
  never a crash).
- One camera per scene. Post-processing through the DSL is exactly
  `bloom()`/`toneMapped()` — arbitrary pass chains and custom shaders are
  escape-hatch territory.
- Walk mode's collision model is `bounds(...)` plus `.solid()` footprints
  (boxes) and `.solid(r)` cylinders — obstacles, not mesh-accurate physics;
  the walker never climbs a solid, only steps over low ones.
- Draco/KTX2-compressed assets are not supported (no decoder shipped).
- `onClick` server handlers and `Three.patch` need the JWeb runtime's
  WebSocket (on by default); `clickSwap` needs only fetch.
- `wire(...)` lines are one pixel wide at any zoom (a WebGL limit) and
  take no clicks or hover — use `tube` for a line with body.
- Members of an `.instanced()` group are not individually clickable,
  hoverable, nameable or animatable; members that need that fall back to
  normal meshes (with a console warning).
- The factory is `wire`, not `line`: `line(...)` is the SVG element under
  the `El` wildcard, and a `double...` overload would be ambiguous with it.
