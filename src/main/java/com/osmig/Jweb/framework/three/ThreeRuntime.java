package com.osmig.Jweb.framework.three;

/**
 * The client-side interpreter for {@code data-three} scene graphs, served at
 * {@code /jweb/three-runtime.js}. It owns everything three.js normally makes
 * callers hand-write: renderer setup, container sizing, the render loop
 * (only when something animates — still scenes render on demand), shadow-map
 * configuration, async model/texture loading, raycast click dispatch into the
 * JWeb event pipeline, re-init when a swap or morph changes the scene, and
 * full disposal when the element leaves the DOM.
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
            function color(c,fallback){return new THREE.Color(c||fallback);}
            function render(inst){inst.renderer.render(inst.scene,inst.camera);}

            function wantsShadows(nodes){
                for(var i=0;i<nodes.length;i++){
                    if(nodes[i].shadows)return true;
                    if(nodes[i].children&&wantsShadows(nodes[i].children))return true;
                }
                return false;
            }

            function geometry(n){
                if(n.t==='box'){var s=n.size||[1,1,1];return new THREE.BoxGeometry(s[0],s[1],s[2]);}
                if(n.t==='sphere')return new THREE.SphereGeometry(n.radius!=null?n.radius:1,48,24);
                if(n.t==='plane'){var p=n.size||[1,1];return new THREE.PlaneGeometry(p[0],p[1]);}
                if(n.t==='cylinder'){
                    var rt=n.radii?n.radii[0]:(n.radius!=null?n.radius:1);
                    var rb=n.radii?n.radii[1]:(n.radius!=null?n.radius:1);
                    return new THREE.CylinderGeometry(rt,rb,n.height!=null?n.height:1,32);
                }
                if(n.t==='cone')return new THREE.ConeGeometry(n.radius!=null?n.radius:1,n.height!=null?n.height:1,32);
                if(n.t==='torus')return new THREE.TorusGeometry(n.radius!=null?n.radius:1,n.tube!=null?n.tube:0.4,20,80);
                return null;
            }

            function material(n,inst){
                var m=new THREE.MeshStandardMaterial({color:color(n.color,'#8b9dc3')});
                if(n.emissive)m.emissive=color(n.emissive,'#000000');
                if(n.metal!=null)m.metalness=n.metal;
                if(n.rough!=null)m.roughness=n.rough;
                if(n.opacity!=null){m.transparent=true;m.opacity=n.opacity;}
                if(n.wire)m.wireframe=true;
                if(n.t==='plane')m.side=THREE.DoubleSide;
                if(n.map){
                    new THREE.TextureLoader().load(n.map,function(tex){
                        if(inst.disposed)return;
                        tex.colorSpace=THREE.SRGBColorSpace;
                        m.map=tex;m.needsUpdate=true;
                        if(!inst.animated)render(inst);
                    },undefined,function(){console.error('[JWeb] texture failed:',n.map);});
                }
                return m;
            }

            function applyCommon(obj,n,inst){
                vec(obj.position,n.pos);
                rad(obj.rotation,n.rot);
                vec(obj.scale,n.scl);
                if(n.spin)inst.spins.push({obj:obj,rate:[n.spin[0]*RAD,n.spin[1]*RAD,n.spin[2]*RAD]});
                if(n['float'])inst.floats.push({obj:obj,amp:n['float'][0],speed:n['float'][1],base:obj.position.y});
                if(n.name)inst.objects[n.name]=obj;
                if(n.click||n.swap){
                    obj.userData.jweb={click:n.click,swap:n.swap,name:n.name||''};
                    inst.clickable=true;
                }
            }

            function buildNode(n,inst,parent){
                var geo=geometry(n);
                if(geo){
                    var mesh=new THREE.Mesh(geo,material(n,inst));
                    if(inst.shadows){mesh.castShadow=true;mesh.receiveShadow=true;}
                    applyCommon(mesh,n,inst);
                    parent.add(mesh);
                }else if(n.t==='group'){
                    var g=new THREE.Group();
                    applyCommon(g,n,inst);
                    (n.children||[]).forEach(function(c){buildNode(c,inst,g);});
                    parent.add(g);
                }else if(n.t==='model'){
                    var wrap=new THREE.Group();
                    applyCommon(wrap,n,inst);
                    parent.add(wrap);
                    new THREE.GLTFLoader().load(n.url,function(gltf){
                        if(inst.disposed)return;
                        if(inst.shadows)gltf.scene.traverse(function(o){
                            if(o.isMesh){o.castShadow=true;o.receiveShadow=true;}
                        });
                        wrap.add(gltf.scene);
                        if(!inst.animated)render(inst);
                    },undefined,function(err){console.error('[JWeb] model failed:',n.url,err);});
                }else if(n.t==='dirLight'){
                    var dir=new THREE.DirectionalLight(color(n.color,'#ffffff'),n.intensity!=null?n.intensity:1);
                    if(n.pos)vec(dir.position,n.pos);else dir.position.set(3,5,2);
                    if(n.shadows){
                        dir.castShadow=true;
                        dir.shadow.mapSize.set(2048,2048);
                        dir.shadow.camera.left=-10;dir.shadow.camera.right=10;
                        dir.shadow.camera.top=10;dir.shadow.camera.bottom=-10;
                        dir.shadow.camera.near=0.5;dir.shadow.camera.far=50;
                        dir.shadow.bias=-0.0005;
                    }
                    if(n.name)inst.objects[n.name]=dir;
                    inst.hasLight=true;
                    inst.scene.add(dir);
                }else if(n.t==='pointLight'){
                    var pt=new THREE.PointLight(color(n.color,'#ffffff'),n.intensity!=null?n.intensity:1);
                    if(n.pos)vec(pt.position,n.pos);else pt.position.set(2,3,2);
                    if(n.shadows){pt.castShadow=true;pt.shadow.mapSize.set(1024,1024);pt.shadow.bias=-0.0005;}
                    if(n.name)inst.objects[n.name]=pt;
                    inst.hasLight=true;
                    inst.scene.add(pt);
                }else if(n.t==='ambLight'){
                    var amb=new THREE.AmbientLight(color(n.color,'#ffffff'),n.intensity!=null?n.intensity:1);
                    if(n.name)inst.objects[n.name]=amb;
                    inst.hasLight=true;
                    inst.scene.add(amb);
                }else if(n.t==='hemiLight'){
                    var hemi=new THREE.HemisphereLight(color(n.sky,'#ffffff'),color(n.ground,'#444444'),n.intensity!=null?n.intensity:1);
                    if(n.name)inst.objects[n.name]=hemi;
                    inst.hasLight=true;
                    inst.scene.add(hemi);
                }else if(n.t==='bg'){
                    inst.scene.background=color(n.color,'#000000');
                }else if(n.t==='fog'){
                    inst.scene.fog=new THREE.Fog(color(n.color,'#ffffff'),n.near!=null?n.near:1,n.far!=null?n.far:50);
                }else if(n.t==='grid'){
                    var grid=new THREE.GridHelper(n.size!=null?n.size:10,n.divisions!=null?n.divisions:10);
                    applyCommon(grid,n,inst);
                    parent.add(grid);
                }else if(n.t!=='camera'){
                    console.warn('[JWeb] unknown scene node type:',n.t);
                }
            }

            function hit(inst,ev){
                var r=inst.renderer.domElement.getBoundingClientRect();
                inst.pointer.set(((ev.clientX-r.left)/r.width)*2-1,-((ev.clientY-r.top)/r.height)*2+1);
                inst.ray.setFromCamera(inst.pointer,inst.camera);
                var hits=inst.ray.intersectObjects(inst.scene.children,true);
                for(var i=0;i<hits.length;i++){
                    var o=hits[i].object;
                    while(o){
                        if(o.userData&&o.userData.jweb)return o.userData.jweb;
                        o=o.parent;
                    }
                }
                return null;
            }

            function wireClicks(inst){
                var canvas=inst.renderer.domElement;
                inst.pointer=new THREE.Vector2();
                inst.ray=new THREE.Raycaster();
                canvas.addEventListener('click',function(ev){
                    var d=hit(inst,ev);
                    if(!d||!window.JWeb)return;
                    if(d.click){
                        JWeb.call(d.click,{type:'click',clientX:ev.clientX,clientY:ev.clientY,
                            target:{id:inst.el.id||'',value:d.name,dataset:{mesh:d.name,scene:inst.el.id||''}}});
                    }else if(d.swap){
                        JWeb.swap(d.swap.url,null,{target:d.swap.target});
                    }
                });
                var pending=false;
                canvas.addEventListener('pointermove',function(ev){
                    if(pending)return;
                    pending=true;
                    requestAnimationFrame(function(){
                        pending=false;
                        if(inst.disposed)return;
                        canvas.style.cursor=hit(inst,ev)?'pointer':'';
                    });
                });
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
                          objects:{},spins:[],floats:[],hasLight:false,clickable:false,
                          shadows:wantsShadows(graph.nodes),animated:false,disposed:false,
                          raf:0,ro:null,last:0,time:0};
                if(inst.shadows)renderer.shadowMap.enabled=true;

                var camNode=null;
                graph.nodes.forEach(function(n){
                    if(n.t==='camera'&&!camNode)camNode=n;
                    buildNode(n,inst,inst.scene);
                });
                if(!inst.hasLight)inst.scene.add(new THREE.HemisphereLight(0xffffff,0x444444,1));

                var aspect=(el.clientWidth||1)/(el.clientHeight||1);
                var cam=new THREE.PerspectiveCamera(
                    (camNode&&camNode.fov)||50,aspect,
                    (camNode&&camNode.near)||0.1,(camNode&&camNode.far)||2000);
                if(camNode&&camNode.pos)vec(cam.position,camNode.pos);else cam.position.set(0,0,5);
                var look=(camNode&&camNode.look)||[0,0,0];
                cam.lookAt(look[0],look[1],look[2]);
                inst.camera=cam;

                var autoRotate=camNode&&camNode.auto;
                var animated=inst.spins.length>0||inst.floats.length>0||!!autoRotate;
                inst.animated=animated;
                if(camNode&&camNode.orbit&&THREE.OrbitControls){
                    var controls=new THREE.OrbitControls(cam,canvas);
                    controls.target.set(look[0],look[1],look[2]);
                    controls.enableDamping=animated;
                    if(autoRotate){controls.autoRotate=true;controls.autoRotateSpeed=autoRotate;}
                    controls.update();
                    if(!animated)controls.addEventListener('change',function(){render(inst);});
                    inst.controls=controls;
                }
                if(inst.clickable)wireClicks(inst);

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
                        inst.time+=dt;
                        inst.spins.forEach(function(s){
                            s.obj.rotation.x+=s.rate[0]*dt;
                            s.obj.rotation.y+=s.rate[1]*dt;
                            s.obj.rotation.z+=s.rate[2]*dt;
                        });
                        inst.floats.forEach(function(f){
                            f.obj.position.y=f.base+Math.sin(inst.time*f.speed*Math.PI*2)*f.amp;
                        });
                        if(inst.controls)inst.controls.update();
                        render(inst);
                    };
                    inst.raf=requestAnimationFrame(loop);
                }else{
                    render(inst);
                }
                instances.set(el,inst);
            }

            function dispose(inst){
                inst.disposed=true;
                if(inst.raf)cancelAnimationFrame(inst.raf);
                if(inst.ro)inst.ro.disconnect();
                if(inst.controls)inst.controls.dispose();
                inst.scene.traverse(function(o){
                    if(o.geometry)o.geometry.dispose();
                    if(o.material){
                        var mats=Array.isArray(o.material)?o.material:[o.material];
                        mats.forEach(function(m){
                            if(m.map)m.map.dispose();
                            m.dispose();
                        });
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
