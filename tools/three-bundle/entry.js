// Everything JWeb's Three DSL needs, bundled under one global: THREE.
// Core three.js plus the addons the DSL wires up: OrbitControls for
// camera().orbit(), GLTFLoader for model(url).
export * from 'three';
export { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
export { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
