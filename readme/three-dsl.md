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

(`disc`, not `circle` — the SVG element owns that name under dual wildcard
imports. Same reason the text billboard below isn't called `label`.)

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
```

A scene that declares **no lights** gets a soft hemisphere light so nothing
renders black. Declare any light to take over completely.

Shadows — normally a five-place configuration chore — are one call:

```java
directionalLight(1.2).position(5, 8, 4).shadows()
```

That single `.shadows()` enables the renderer's shadow map (soft PCF),
configures the light's shadow camera, and makes every mesh in the scene cast
and receive. `pointLight(...).shadows()` works the same way.

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
`data-three-walk="<scene id>"` — a plain button, no script:

```java
scene(style().height(px(460)),
    camera().position(0, 2, 7).lookAt(0, 2, 0)
        .walk(1.7)                  // eye height; or walk(eye, speed, run)
        .bounds(-8, -8, 8, 8)       // fenced floor: minX, minZ, maxX, maxZ
        .sway(),                    // gentle idle drift while framed
    ...
).id("hall")

button(attrs().data("three-walk", "hall"), text("Walk here"))
```

While walking: **W A S D** (and ↑↓) move, **← →** turn, dragging looks
around, **Shift** runs, **Esc** steps back out to the framed view. The
scene element, the toggle and `<body>` carry a `three-walking` class, and
the scene dispatches a bubbling `jweb:three-walk` event with
`detail.walking` — style the chip's label swap in CSS, or listen if you
must script. Head-bob and `sway()` are skipped for visitors who prefer
reduced motion. Programmatic control: `JWebThree.setWalk(id, on)` /
`JWebThree.walking(id)`.

`bounds(...)` is the honest collision model for a walled room: a rectangle
the feet can't leave. (Per-object collision is not a thing the DSL does.)

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
it — darker is dimmer. A mirror is glass-sharp; for a satin polished floor,
lay a translucent dark plane a hair above it and the reflection blurs into
suggestion. Other material properties don't apply to a mirror.

## Animation presets

```java
box().spin()            // 20°/s x, 30°/s y — the pleasant default
box().spin(45)          // y axis, degrees per second
box().spin(10, 20, 30)  // per axis
model("/assets/drone.glb").float_()          // gentle hover: ±0.25 units
sphere().float_(0.5, 1)                      // amplitude, cycles per second
```

Presets are why the render loop exists at all: a scene with no `spin`,
`float_`, `autoRotate`, `.animate()`d model, drifting `particles` or
`sway()` renders once and sleeps — and an animated scene's loop pauses
whenever it scrolls offscreen (IntersectionObserver), so a 3D hero costs
nothing once the reader is past it.

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

The handler's `event.value()` (and `dataset.mesh`) carry the node's `name`.
If several are set on one node: server `onClick` wins, then the Action, then
`clickSwap`. The cursor becomes a pointer over clickable objects. Clicks on
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
mode from scripts when a `data-three-walk` element isn't enough.

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
- Walk mode's collision model is `bounds(...)` — a fenced rectangle, not
  per-object collision.
- Draco/KTX2-compressed assets are not supported (no decoder shipped).
- `onClick` server handlers and `Three.patch` need the JWeb runtime's
  WebSocket (on by default); `clickSwap` needs only fetch.
