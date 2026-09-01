package com.osmig.Jweb.framework.three;

/**
 * The client-side interpreter for {@code data-three} scene graphs, served at
 * {@code /jweb/three-runtime.js}. It owns everything three.js normally makes
 * callers hand-write: renderer setup, container sizing, the render loop
 * (only when something animates, paused while scrolled offscreen — still
 * scenes render on demand), shadow-map configuration, async model/texture
 * loading and glTF animation mixing, raycast click/hover dispatch into the
 * JWeb event pipeline (server handlers, Actions-DSL handlers, swaps),
 * re-init when a swap or morph changes the scene, and full disposal when
 * the element leaves the DOM.
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
                if(n.t==='capsule')return new THREE.CapsuleGeometry(n.radius!=null?n.radius:1,n.length!=null?n.length:1,8,24);
                if(n.t==='disc')return new THREE.CircleGeometry(n.radius!=null?n.radius:1,48);
                if(n.t==='ring'){var rr=n.radii||[0.5,1];return new THREE.RingGeometry(rr[0],rr[1],48);}
                if(n.t==='knot')return new THREE.TorusKnotGeometry(n.radius!=null?n.radius:1,n.tube!=null?n.tube:0.4,128,16);
                if(n.t==='tetra')return new THREE.TetrahedronGeometry(n.radius!=null?n.radius:1);
                if(n.t==='octa')return new THREE.OctahedronGeometry(n.radius!=null?n.radius:1);
                if(n.t==='dodeca')return new THREE.DodecahedronGeometry(n.radius!=null?n.radius:1);
                if(n.t==='icosa')return new THREE.IcosahedronGeometry(n.radius!=null?n.radius:1);
                return null;
            }

            function material(n,inst){
                var m=new THREE.MeshStandardMaterial({color:color(n.color,'#8b9dc3')});
                if(n.emissive)m.emissive=color(n.emissive,'#000000');
                if(n.metal!=null)m.metalness=n.metal;
                if(n.rough!=null)m.roughness=n.rough;
                if(n.opacity!=null){m.transparent=true;m.opacity=n.opacity;}
                if(n.wire)m.wireframe=true;
                if(n.t==='plane'||n.t==='disc'||n.t==='ring')m.side=THREE.DoubleSide;
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
                if(n.click||n.swap||n.clickAct||n.hovScale||n.hovColor||n.hovEmissive){
                    obj.userData.jweb={click:n.click,swap:n.swap,act:n.clickAct,name:n.name||'',
                        hovScale:n.hovScale,hovColor:n.hovColor,hovEmissive:n.hovEmissive};
                    inst.interactive=true;
                    if(n.click||n.swap||n.clickAct)inst.clickable=true;
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
                    if(n.anim)inst.needsLoop=true;
                    new THREE.GLTFLoader().load(n.url,function(gltf){
                        if(inst.disposed)return;
                        if(inst.shadows)gltf.scene.traverse(function(o){
                            if(o.isMesh){o.castShadow=true;o.receiveShadow=true;}
                        });
                        wrap.add(gltf.scene);
                        if(n.anim&&gltf.animations&&gltf.animations.length){
                            var mixer=new THREE.AnimationMixer(gltf.scene);
                            var played=0;
                            gltf.animations.forEach(function(clip){
                                if(n.anim===true||clip.name===n.anim){mixer.clipAction(clip).play();played++;}
                            });
                            if(!played)console.warn('[JWeb] model has no animation clip named',n.anim,
                                '- clips:',gltf.animations.map(function(c){return c.name;}));
                            inst.mixers.push(mixer);
                        }else if(n.anim){
                            console.warn('[JWeb] .animate() set but model ships no animations:',n.url);
                        }
                        if(!inst.animated)render(inst);
                    },undefined,function(err){console.error('[JWeb] model failed:',n.url,err);});
                }else if(n.t==='label'){
                    // Canvas-rendered text on a Sprite: crisp, always camera-
                    // facing, no font file. Height = n.size scene units.
                    var cv=document.createElement('canvas');
                    var fs=64,pad=n.bg?28:6,ctx=cv.getContext('2d');
                    ctx.font='600 '+fs+'px system-ui,-apple-system,sans-serif';
                    cv.width=Math.max(2,Math.ceil(ctx.measureText(n.text||'').width)+pad*2);
                    cv.height=fs+pad*2;
                    ctx=cv.getContext('2d');   // resizing reset the context
                    if(n.bg){
                        ctx.fillStyle=n.bg;
                        if(ctx.roundRect){
                            ctx.beginPath();
                            ctx.roundRect(0,0,cv.width,cv.height,cv.height/2);
                            ctx.fill();
                        }else{ctx.fillRect(0,0,cv.width,cv.height);}
                    }
                    ctx.font='600 '+fs+'px system-ui,-apple-system,sans-serif';
                    ctx.fillStyle=n.color||'#ffffff';
                    ctx.textAlign='center';
                    ctx.textBaseline='middle';
                    ctx.fillText(n.text||'',cv.width/2,cv.height/2);
                    var ltex=new THREE.CanvasTexture(cv);
                    ltex.colorSpace=THREE.SRGBColorSpace;
                    var lsp=new THREE.Sprite(new THREE.SpriteMaterial({map:ltex,transparent:true}));
                    applyCommon(lsp,n,inst);
                    // sized via n.size (world height), not .scale — aspect
                    // comes from the measured text
                    var lh=n.size!=null?n.size:0.5;
                    lsp.scale.set(lh*cv.width/cv.height,lh,1);
                    parent.add(lsp);
                }else if(n.t==='sprite'){
                    var smat=new THREE.SpriteMaterial({transparent:true});
                    var spr=new THREE.Sprite(smat);
                    spr.visible=false;   // nothing to show until the image lands
                    applyCommon(spr,n,inst);
                    parent.add(spr);
                    new THREE.TextureLoader().load(n.url,function(tex){
                        if(inst.disposed)return;
                        tex.colorSpace=THREE.SRGBColorSpace;
                        smat.map=tex;smat.needsUpdate=true;
                        var sw=n.size!=null?n.size:1;
                        var sa=(tex.image&&tex.image.width)?tex.image.height/tex.image.width:1;
                        spr.scale.set(sw,sw*sa,1);
                        spr.visible=true;
                        if(!inst.animated)render(inst);
                    },undefined,function(){console.error('[JWeb] sprite failed:',n.url);});
                }else if(n.t==='env'){
                    // Equirect panorama -> PBR light environment (and the
                    // visible sky when bg is set)
                    new THREE.TextureLoader().load(n.url,function(tex){
                        if(inst.disposed)return;
                        tex.mapping=THREE.EquirectangularReflectionMapping;
                        tex.colorSpace=THREE.SRGBColorSpace;
                        inst.scene.environment=tex;
                        if(n.bg)inst.scene.background=tex;
                        if(!inst.animated)render(inst);
                    },undefined,function(){console.error('[JWeb] environment failed:',n.url);});
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
                        if(o.userData&&o.userData.jweb)return {o:o,d:o.userData.jweb};
                        o=o.parent;
                    }
                }
                return null;
            }

            // Declarative hover effects: apply on raycast enter, restore the
            // captured originals on leave — scale on any node, color/emissive
            // on single-material meshes
            function hover(o){
                var d=o.userData.jweb;
                if(d.hovScale){o.userData.jwebScl=o.scale.clone();o.scale.multiplyScalar(d.hovScale);}
                if(o.material&&!Array.isArray(o.material)){
                    if(d.hovColor&&o.material.color){o.userData.jwebCol=o.material.color.clone();o.material.color.set(d.hovColor);}
                    if(d.hovEmissive&&o.material.emissive){o.userData.jwebEmi=o.material.emissive.clone();o.material.emissive.set(d.hovEmissive);}
                }
            }

            function unhover(o){
                if(o.userData.jwebScl){o.scale.copy(o.userData.jwebScl);delete o.userData.jwebScl;}
                if(o.userData.jwebCol){o.material.color.copy(o.userData.jwebCol);delete o.userData.jwebCol;}
                if(o.userData.jwebEmi){o.material.emissive.copy(o.userData.jwebEmi);delete o.userData.jwebEmi;}
            }

            function setHovered(inst,target){
                if(target===inst.hovered)return;
                if(inst.hovered)unhover(inst.hovered);
                inst.hovered=target;
                if(target)hover(target);
                if(!inst.animated)render(inst);
            }

            function wireInteraction(inst){
                var canvas=inst.renderer.domElement;
                inst.pointer=new THREE.Vector2();
                inst.ray=new THREE.Raycaster();
                canvas.addEventListener('click',function(ev){
                    var h=hit(inst,ev);
                    if(!h||!window.JWeb)return;
                    var d=h.d;
                    if(d.click){
                        JWeb.call(d.click,{type:'click',clientX:ev.clientX,clientY:ev.clientY,
                            target:{id:inst.el.id||'',value:d.name,dataset:{mesh:d.name,scene:inst.el.id||''}}});
                    }else if(d.act&&JWeb.runAction){
                        // Actions-DSL handler: defined in __JWEB_ACTIONS__ by
                        // the page's nonce'd definitions script; this = canvas,
                        // event = the real click event
                        JWeb.runAction(d.act,ev,canvas);
                    }else if(d.swap){
                        JWeb.swap(d.swap.url,null,{target:d.swap.target});
                    }
                });
                // rAF-throttled: at most one raycast per frame, always for
                // the LATEST pointer position (not the first move that
                // scheduled the frame)
                var pending=false,lastMove=null;
                canvas.addEventListener('pointermove',function(ev){
                    lastMove=ev;
                    if(pending)return;
                    pending=true;
                    requestAnimationFrame(function(){
                        pending=false;
                        if(inst.disposed)return;
                        var h=hit(inst,lastMove);
                        canvas.style.cursor=(h&&(h.d.click||h.d.act||h.d.swap))?'pointer':'';
                        setHovered(inst,h?h.o:null);
                    });
                });
                canvas.addEventListener('pointerleave',function(){
                    if(!inst.disposed)setHovered(inst,null);
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
                          objects:{},spins:[],floats:[],mixers:[],hasLight:false,
                          clickable:false,interactive:false,hovered:null,needsLoop:false,
                          shadows:wantsShadows(graph.nodes),animated:false,disposed:false,
                          raf:0,ro:null,io:null,last:0,time:0};
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
                var animated=inst.spins.length>0||inst.floats.length>0||!!autoRotate||inst.needsLoop;
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
                if(inst.interactive)wireInteraction(inst);

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
                        inst.mixers.forEach(function(m){m.update(dt);});
                        if(inst.controls)inst.controls.update();
                        render(inst);
                    };
                    inst.startLoop=function(){
                        if(inst.raf||inst.disposed)return;
                        inst.last=performance.now();
                        inst.raf=requestAnimationFrame(loop);
                    };
                    inst.stopLoop=function(){
                        if(inst.raf){cancelAnimationFrame(inst.raf);inst.raf=0;}
                    };
                    // The loop only runs while the scene is actually on
                    // screen — scrolled-away scenes cost nothing
                    inst.io=new IntersectionObserver(function(entries){
                        if(entries[0].isIntersecting)inst.startLoop();
                        else inst.stopLoop();
                    });
                    inst.io.observe(el);
                    inst.startLoop();
                }else{
                    render(inst);
                }
                instances.set(el,inst);
            }

            function dispose(inst){
                inst.disposed=true;
                if(inst.raf)cancelAnimationFrame(inst.raf);
                if(inst.ro)inst.ro.disconnect();
                if(inst.io)inst.io.disconnect();
                if(inst.controls)inst.controls.dispose();
                // env/sky textures hang off the scene, not its children
                if(inst.scene.environment&&inst.scene.environment.isTexture)inst.scene.environment.dispose();
                if(inst.scene.background&&inst.scene.background.isTexture)inst.scene.background.dispose();
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
