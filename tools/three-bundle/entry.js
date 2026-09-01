// Everything JWeb's Three DSL needs, bundled under one global: THREE.
// Core three.js plus the addons the DSL wires up: OrbitControls for
// camera().orbit(), GLTFLoader for model(url), the postprocessing chain
// for bloom(), and Reflector for plane().mirror().
export * from 'three';
export { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
export { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
export { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
export { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
export { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
export { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
export { Reflector } from 'three/examples/jsm/objects/Reflector.js';
