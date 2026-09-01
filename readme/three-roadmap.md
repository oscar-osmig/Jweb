# Three DSL — coverage roadmap

The measuring stick for where the DSL goes next is the domain map of the
three.js expert-skill packs (the `threejs-*` agent skills): each pack is a
visual system real scenes reach for. This file tracks which systems the DSL
covers natively, which are planned, and which are deliberately out.

The standing philosophy still governs everything here: curated verbs, not a
mirrored API; defaults that kill traps; scenes must read without any
optional effect; one new concept only when it deletes real user code.

## Covered (as of 2.2.3)

| Skill-pack domain | DSL surface |
|---|---|
| Camera direction (framing, pointer look, rigs) | `camera()` position/lookAt/fov, `orbit()` + limits (`noZoom/noPan/distance/polar`), `walk()` + `bounds()` + `sway()`, `Three.patch(...).camera()` glides |
| Bloom | `bloom(strength, radius, threshold)` — HDR chain, composited before tone mapping, OutputPass owns the one conversion |
| Exposure / tone mapping | `toneMapped(exposure)` — ACES, single ownership (never doubled with bloom) |
| Procedural geometry (lofts, revolve, sweeps) | `tube()` (Catmull-Rom sweep), `arc()`, `lathe()` — the curve family; primitives since 2.1/2.2 |
| Particles / VFX (the entry tier) | `particles(n)` with `drift()` / `fall()` — seeded, one draw call |
| Reflections (planar) | `plane().mirror()` (Reflector) |
| Interaction | raycast `onClick` (server / Actions / swap), hover effects, walk toggle protocol, live `Three.patch` over the socket |

## Next candidates, in rough order of earn

1. **HDR environments** (`environment(url)` accepting `.hdr`) — the packs'
   single biggest realism lever for PBR product shots; needs RGBELoader in
   the bundle. Small, contained, high visual payoff.
2. **Instanced repetition** — `repeat(...)` solves authoring; an
   `.instanced()` group (InstancedMesh) would collapse its draw calls for
   colonnades/forests/chains. GPU-side twin of what shipped.
3. **Terrain / heightfields** — `terrain(w, d).hills(h, scale)` with seeded
   noise displacement (the procedural-fields pack's entry tier). Dunes,
   ground, coastlines-without-water.
4. **Shadow tuning tier** — the shadow-systems pack is about stability at
   scale; the DSL's one-call `.shadows()` could take `.shadows(radius)` /
   map-size hints before ever exposing cascades.
5. **Spot light** — completes the light set; trivial.
6. **Text along curves / extrusion** — the procedural-geometry pack's
   deeper tier (`extrude(shape)`); wants a 2D shape vocabulary first.

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
- **XR, physics, skeletal authoring** — out of scope; different products.

## Explicitly rejected

- Mirroring the three.js API into static methods (standing decision).
- Selective bloom (per-object bloom layers) — material-substitution
  machinery for an effect whole-scene thresholds already deliver; the
  bloom pack itself calls the dual-pass model its expensive tier.
- Arbitrary shader material injection — the CSP story and the DSL's
  serialization contract both say no; escape hatch exists.
