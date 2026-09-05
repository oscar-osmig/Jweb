# Three DSL — coverage roadmap

The measuring stick for where the DSL goes next is the domain map of the
three.js expert-skill packs (the `threejs-*` agent skills): each pack is a
visual system real scenes reach for. This file tracks which systems the DSL
covers natively, which are planned, and which are deliberately out.

The standing philosophy still governs everything here: curated verbs, not a
mirrored API; defaults that kill traps; scenes must read without any
optional effect; one new concept only when it deletes real user code.

A second yardstick joined it on 2026-09-04: a real app. The Museum of
Elsewhere (the-gallery) — a walkable first-person museum — was audited for
everything it had to do outside the DSL: a 461-line raw-JS walk controller,
hundreds of primitives standing in for arcs, dunes and swarms, and a page
reload for every change of state. Every gap that audit found is closed
below.

## Covered (as of the 3.0 working tree)

| Skill-pack domain | DSL surface |
|---|---|
| Camera direction (framing, pointer look, rigs) | `camera()` position/lookAt/fov, `orbit()` + limits (`noZoom/noPan/distance/polar`), `walk()` + `bounds()` + `sway()`, `Three.patch(...).camera()` glides |
| The walker's body | feet that follow surfaces (`ground()`), `.solid()` / `.solid(r)` colliders, `fly()`, `clickToMove()`, `autoStart()`, `spawn()`, `pointerLock()`, `touch()`, `gamepad()`, `footsteps()`; the page reads the camera through `--three-yaw` / `--three-pitch`, `three-key-*` classes, `jweb:three-look`, `JWebThree.pose()` |
| Places that react | `.link(url)` portals (view transition via the Navigation script), `.near(d)` / `onNear` / `onFar` with `three-near-<name>` and `jweb:three-near`, `zone(...)` with `onEnter` / `onLeave` / `link` and `jweb:three-zone`; handlers carry the camera pose |
| Sound | `sound(url)` global or positional (`.loop / .volume / .range / .paused`), unlocked on the first gesture; patches `.volume().tween()` / `.play()` / `.stop()`; `footsteps()` synthesized or sampled; `JWebThree.mute()` |
| Bloom | `bloom(strength, radius, threshold)` — HDR chain, composited before tone mapping, OutputPass owns the one conversion |
| Exposure / tone mapping | `toneMapped(exposure)` — ACES, single ownership (never doubled with bloom) |
| Procedural geometry (lofts, revolve, sweeps) | `tube()`, `arc()`, `lathe()`; `sweep(w, h, path)` for rectangular profiles — moldings, ribs, rails; `wire(points)` polylines with `.dashed()` / `.draw(ms)` |
| Terrain / heightfields | `terrain(w, d).hills(h, scale).seed(n)` — seeded value noise, raycastable, so `walk()` climbs it |
| Instanced repetition | `group(...).instanced()` — one InstancedMesh per geometry+material signature, per-instance colour |
| Particles / VFX (the entry tier) | `particles(n)` with `drift()` / `fall()` / `colors(...)` — seeded, one draw call |
| Materials | the PBR surface, `material()` presets, `.glass()` transmission |
| Lights | directional / ambient / point / hemisphere / `spotLight()` with `.target / .angle / .penumbra`; one-call `.shadows()` |
| Reflections (planar) | `plane().mirror()` (Reflector), `mirror(strength)` for satin |
| Motion | `spin`, `float_`, `pulse`, `glow`, `appear`, `delay` staggering, `follow(seconds, path)` |
| Interaction | raycast `onClick` (server / Actions / swap / link), hover effects, the walk toggle protocol |
| Live state | `Three.patch` property tweens plus structural `add / addTo / remove / replace` over the socket — a scene changes without a reload |

## Next candidates, in rough order of earn

1. **HDR environments** (`environment(url)` accepting `.hdr`) — the packs'
   single biggest realism lever for PBR product shots; needs RGBELoader in
   the bundle. Small, contained, high visual payoff.
2. **Shadow tuning tier** — the shadow-systems pack is about stability at
   scale; `.shadows(radius)` / map-size hints before ever exposing cascades.
3. **Text along curves / extrusion** — the procedural-geometry pack's
   deeper tier (`extrude(shape)`) wants a 2D shape vocabulary first;
   `sweep()` covers the rectangular case today.
4. **A synthesized-sound tier** — `sound(url)` covers files; the museum's
   generated pads are expressible with the JS DSL's Web Audio verbs, not
   yet as a scene node.
5. **Water and clouds as nodes** — the museum faked both with translucent
   spheres and planes; still the shader-shaped systems below.

## Watching, not planned

- **Water/ocean, volumetric clouds, atmosphere scattering, planets,
  vegetation, architecture grammars** — whole authored systems in the
  packs, each a shader stack the DSL would have to own end-to-end. The
  honest current answer is `model(url)` for the geometry-shaped ones and
  restraint for the shader-shaped ones. Revisit per real app demand, one
  system at a time, never as an API mirror.
- **GTAO / image pipeline composition** — beyond `bloom()`, pass-chain
  composition is escape-hatch territory until a real scene needs one more
  named effect.
- **XR, physics engines, skeletal authoring** — out of scope; different
  products. Walk mode's colliders are obstacles, not physics.

## Explicitly rejected

- Mirroring the three.js API into static methods (standing decision).
- `line(...)` as the polyline's name — a compile-time ambiguity with the SVG
  element under the documented dual wildcard imports, so the node is
  `wire(...)` (the `disc` / `billboard` precedent).
- Selective bloom (per-object bloom layers) — material-substitution
  machinery for an effect whole-scene thresholds already deliver; the
  bloom pack itself calls the dual-pass model its expensive tier.
- Arbitrary shader material injection — the CSP story and the DSL's
  serialization contract both say no; escape hatch exists.
- A built-in walk HUD element — the key classes and `--three-yaw` make one
  in CSS; the markup belongs to the app.
