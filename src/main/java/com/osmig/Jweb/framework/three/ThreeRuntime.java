package com.osmig.Jweb.framework.three;

/**
 * The client-side interpreter for {@code data-three} scene graphs, served at
 * {@code /jweb/three-runtime.js}. It owns everything three.js normally makes
 * callers hand-write: renderer setup, container sizing, the render loop
 * (only when something animates — static scenes render on demand), re-init
 * when a swap or morph changes the scene, and full disposal when the element
 * leaves the DOM.
 *
 * <p>Loaded lazily by the JWeb runtime, after the vendored bundle, only on
 * pages that contain a scene. Exposes {@code JWebThree.get(id)} →
 * {@code {scene, camera, renderer, controls, objects}} for scripts that need
 * the raw three.js API.</p>
 */
public final class ThreeRuntime {

    private ThreeRuntime() {}

    /** The interpreter script. */
    public static String getScript() {
        return SCRIPT;
    }

    /** Content-hash version used as the script's cache-busting {@code ?v=} value. */
    public static String version() {
        return Integer.toHexString(SCRIPT.hashCode());
    }

    private static final String SCRIPT = """
        (function(){
            if(window.JWebThree)return;
            if(!window.THREE){console.error('[JWeb] three-runtime loaded without THREE');return;}
            var RAD=Math.PI/180;
            var instances=new Map();

            function vec(target,v){if(v)target.set(v[0],v[1],v[2]);}
            function rad(target,v){if(v)target.set(v[0]*RAD,v[1]*RAD,v[2]*RAD);}

            function buildNode(n,inst){
                if(n.t==='box'){
                    var size=n.size||[1,1,1];
                    var geo=new THREE.BoxGeometry(size[0],size[1],size[2]);
                    var mat=new THREE.MeshStandardMaterial({color:new THREE.Color(n.color||'#8b9dc3')});
                    if(n.metal!=null)mat.metalness=n.metal;
                    if(n.rough!=null)mat.roughness=n.rough;
                    var mesh=new THREE.Mesh(geo,mat);
                    vec(mesh.position,n.pos);
                    rad(mesh.rotation,n.rot);
                    vec(mesh.scale,n.scl);
                    if(n.spin)inst.spins.push({obj:mesh,rate:[n.spin[0]*RAD,n.spin[1]*RAD,n.spin[2]*RAD]});
                    if(n.name)inst.objects[n.name]=mesh;
                    inst.scene.add(mesh);
                }else if(n.t==='dirLight'){
                    var dir=new THREE.DirectionalLight(new THREE.Color(n.color||'#ffffff'),n.intensity!=null?n.intensity:1);
                    if(n.pos)vec(dir.position,n.pos);else dir.position.set(3,5,2);
                    if(n.name)inst.objects[n.name]=dir;
                    inst.hasLight=true;
                    inst.scene.add(dir);
                }else if(n.t==='ambLight'){
                    var amb=new THREE.AmbientLight(new THREE.Color(n.color||'#ffffff'),n.intensity!=null?n.intensity:1);
                    if(n.name)inst.objects[n.name]=amb;
                    inst.hasLight=true;
                    inst.scene.add(amb);
                }else if(n.t!=='camera'){
                    console.warn('[JWeb] unknown scene node type:',n.t);
                }
            }

            function init(el){
                if(instances.has(el))return;
                var raw=el.getAttribute('data-three');
                if(!raw)return;
                var graph;
                try{graph=JSON.parse(raw);}
                catch(e){console.error('[JWeb] invalid data-three JSON:',e);return;}
                if(!graph||!graph.nodes){console.error('[JWeb] data-three has no nodes');return;}
                var renderer;
                try{renderer=new THREE.WebGLRenderer({antialias:true,alpha:true});}
                catch(e){console.error('[JWeb] WebGL unavailable:',e);return;}
                renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,2));

                if(getComputedStyle(el).position==='static')el.style.position='relative';
                if(el.clientHeight===0){
                    el.style.minHeight='320px';
                    console.info('[JWeb] scene container had no height; defaulted to 320px — size it with a style or class');
                }
                var canvas=renderer.domElement;
                canvas.style.display='block';
                canvas.style.width='100%';
                canvas.style.height='100%';
                el.appendChild(canvas);

                var inst={el:el,renderer:renderer,scene:new THREE.Scene(),camera:null,controls:null,
                          objects:{},spins:[],hasLight:false,animated:false,raf:0,ro:null,last:0};

                var camNode=null;
                (graph.nodes||[]).forEach(function(n){
                    if(n.t==='camera'&&!camNode)camNode=n;
                    buildNode(n,inst);
                });
                if(!inst.hasLight)inst.scene.add(new THREE.HemisphereLight(0xffffff,0x444444,1));

                var aspect=(el.clientWidth||1)/(el.clientHeight||1);
                var cam=new THREE.PerspectiveCamera((camNode&&camNode.fov)||50,aspect,0.1,2000);
                if(camNode&&camNode.pos)vec(cam.position,camNode.pos);else cam.position.set(0,0,5);
                var look=(camNode&&camNode.look)||[0,0,0];
                cam.lookAt(look[0],look[1],look[2]);
                inst.camera=cam;

                var animated=inst.spins.length>0;
                inst.animated=animated;
                if(camNode&&camNode.orbit&&THREE.OrbitControls){
                    var controls=new THREE.OrbitControls(cam,canvas);
                    controls.target.set(look[0],look[1],look[2]);
                    controls.enableDamping=animated;
                    controls.update();
                    if(!animated)controls.addEventListener('change',function(){render(inst);});
                    inst.controls=controls;
                }

                function resize(){
                    var w=el.clientWidth,h=el.clientHeight;
                    if(!w||!h)return;
                    renderer.setSize(w,h,false);
                    cam.aspect=w/h;
                    cam.updateProjectionMatrix();
                    if(!animated)render(inst);
                }
                inst.ro=new ResizeObserver(resize);
                inst.ro.observe(el);
                resize();

                if(animated){
                    inst.last=performance.now();
                    var loop=function(now){
                        inst.raf=requestAnimationFrame(loop);
                        var dt=Math.min((now-inst.last)/1000,0.1);
                        inst.last=now;
                        inst.spins.forEach(function(s){
                            s.obj.rotation.x+=s.rate[0]*dt;
                            s.obj.rotation.y+=s.rate[1]*dt;
                            s.obj.rotation.z+=s.rate[2]*dt;
                        });
                        if(inst.controls)inst.controls.update();
                        renderer.render(inst.scene,cam);
                    };
                    inst.raf=requestAnimationFrame(loop);
                }else{
                    render(inst);
                }
                instances.set(el,inst);
            }

            function render(inst){
                inst.renderer.render(inst.scene,inst.camera);
            }

            function dispose(inst){
                if(inst.raf)cancelAnimationFrame(inst.raf);
                if(inst.ro)inst.ro.disconnect();
                if(inst.controls)inst.controls.dispose();
                inst.scene.traverse(function(o){
                    if(o.geometry)o.geometry.dispose();
                    if(o.material){
                        if(Array.isArray(o.material))o.material.forEach(function(m){m.dispose();});
                        else o.material.dispose();
                    }
                });
                inst.renderer.dispose();
                if(inst.renderer.domElement.parentNode)inst.renderer.domElement.parentNode.removeChild(inst.renderer.domElement);
                instances.delete(inst.el);
            }

            function scan(){
                document.querySelectorAll('[data-three]').forEach(init);
            }

            var mo=new MutationObserver(function(muts){
                var removed=false,added=false;
                muts.forEach(function(m){
                    if(m.type==='attributes'){
                        var inst=instances.get(m.target);
                        if(inst){dispose(inst);init(m.target);}
                        return;
                    }
                    if(m.removedNodes.length)removed=true;
                    if(m.addedNodes.length)added=true;
                });
                if(removed)instances.forEach(function(inst,el){
                    if(!document.contains(el))dispose(inst);
                });
                if(added||removed)instances.forEach(function(inst,el){
                    // a morph can rebuild the container's children without touching
                    // data-three — re-adopt the live canvas instead of re-initing
                    if(document.contains(el)&&inst.renderer.domElement.parentNode!==el){
                        el.appendChild(inst.renderer.domElement);
                        if(!inst.animated)render(inst);
                    }
                });
                if(added)scan();
            });
            mo.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['data-three']});

            window.JWebThree={
                get:function(id){
                    var el=document.getElementById(id);
                    return (el&&instances.get(el))||null;
                },
                scan:scan
            };
            scan();
        })();
        """;
}
