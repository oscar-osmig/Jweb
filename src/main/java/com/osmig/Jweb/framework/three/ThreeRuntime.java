package com.osmig.Jweb.framework.three;

/**
 * The client-side interpreter for {@code data-three} scene graphs, served at
 * {@code /jweb/three-runtime.js}. It owns everything three.js normally makes
 * callers hand-write: renderer setup, container sizing, the render loop
 * (only when something animates, paused while scrolled offscreen — still
 * scenes render on demand), shadow-map configuration, async model/texture
 * loading and glTF animation mixing, raycast click/hover dispatch into the
 * JWeb event pipeline (server handlers, Actions-DSL handlers, swaps),
 * first-person walk mode (toggled by {@code data-three-walk} elements),
 * live {@code Three.patch} application with tweening, the bloom/tone-mapped
 * post pipeline, re-init when a swap or morph changes the scene, and full
 * disposal when the element leaves the DOM.
 *
 * <p>Loaded lazily by the JWeb runtime, after the vendored bundle, only on
 * pages that contain a scene. Exposes {@code JWebThree}:</p>
 * <ul>
 *   <li>{@code get(id)} → {@code {scene, camera, renderer, controls,
 *       objects}} — the live escape hatch into the full three.js API;</li>
 *   <li>{@code ready(id, cb)} — runs {@code cb(handle)} once the scene is
 *       initialized (immediately if it already is), no polling;</li>
 *   <li>{@code THREE} — the bundled three.js module itself, so page scripts
 *       can construct vectors, materials and helpers;</li>
 *   <li>{@code setWalk(id, on)} / {@code walking(id)} — programmatic walk
 *       control alongside the {@code data-three-walk} toggle protocol;</li>
 *   <li>{@code pose(id)} — where the camera is and looks
 *       ({@code {x, y, z, yaw, pitch, walking}}), also published on the
 *       scene element as {@code --three-yaw}/{@code --three-pitch} and as
 *       bubbling {@code jweb:three-look} events;</li>
 *   <li>{@code mute(id, on)} — silences / restores the scene's sounds.</li>
 * </ul>
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

    // The interpreter is three text blocks joined at class-init: as one block it
    // would exceed the class-file limit for a single string constant (64KB).
    private static final String PART1 = """
        (function(){
            if(window.JWebThree)return;
            if(!window.THREE){console.error('[JWeb] three-runtime loaded without THREE');return;}
            var RAD=Math.PI/180;
            var REDUCED=window.matchMedia&&matchMedia('(prefers-reduced-motion: reduce)').matches;
            var instances=new Map();
            var readyQ=[];
            var DOWN=new THREE.Vector3(0,-1,0),TMP=new THREE.Vector3(),NORMAL=new THREE.Vector3(),NMAT=new THREE.Matrix3();
            var audioUnlocked=false,audioWired=false;

            function vec(target,v){if(v)target.set(v[0],v[1],v[2]);}
            function rad(target,v){if(v)target.set(v[0]*RAD,v[1]*RAD,v[2]*RAD);}
            function color(c,fallback){return new THREE.Color(c||fallback);}
            function render(inst){
                if(inst.composer)inst.composer.render();
                else inst.renderer.render(inst.scene,inst.camera);
            }
            function ease(k){return k*k*(3-2*k);}
            // deterministic PRNG so a re-render doesn't reshuffle particles
            function mulberry(a){return function(){a|=0;a=a+0x6D2B79F5|0;var t=Math.imul(a^a>>>15,1|a);t=t+Math.imul(t^t>>>7,61|t)^t;return((t^t>>>14)>>>0)/4294967296;};}

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
                if(n.t==='tube'){
                    var tp=[];
                    for(var i=0;i+2<n.pts.length;i+=3)tp.push(new THREE.Vector3(n.pts[i],n.pts[i+1],n.pts[i+2]));
                    var curve=new THREE.CatmullRomCurve3(tp,!!n.closed);
                    return new THREE.TubeGeometry(curve,Math.max(24,tp.length*8),n.radius!=null?n.radius:0.1,12,!!n.closed);
                }
                if(n.t==='arc')return new THREE.TorusGeometry(n.radius!=null?n.radius:1,n.tube!=null?n.tube:0.1,16,64,(n.sweep!=null?n.sweep:360)*RAD);
                if(n.t==='lathe'){
                    var lp=[];
                    for(var j=0;j+1<n.profile.length;j+=2)lp.push(new THREE.Vector2(n.profile[j],n.profile[j+1]));
                    return new THREE.LatheGeometry(lp,n.seg||32);
                }
                if(n.t==='sweep')return sweepGeometry(n);
                if(n.t==='terrain')return terrainGeometry(n);
                return null;
            }

            // Sweep: a centered w×h rectangle extruded along a Catmull-Rom curve
            function sweepGeometry(n){
                var sp=[];
                for(var i=0;i+2<n.pts.length;i+=3)sp.push(new THREE.Vector3(n.pts[i],n.pts[i+1],n.pts[i+2]));
                var w=n.profile?n.profile[0]:0.1,h=n.profile?n.profile[1]:0.1;
                var shape=new THREE.Shape();
                shape.moveTo(-w/2,-h/2);shape.lineTo(w/2,-h/2);shape.lineTo(w/2,h/2);shape.lineTo(-w/2,h/2);shape.closePath();
                var curve=new THREE.CatmullRomCurve3(sp,!!n.closed);
                return new THREE.ExtrudeGeometry(shape,{extrudePath:curve,
                    steps:n.steps||Math.max(24,sp.length*8),bevelEnabled:false});
            }

            // Terrain: a flat plane whose vertices ride seeded 3-octave value
            // noise — deterministic per seed, so re-renders don't reshape the land
            function lattice(ix,iz,seed){return mulberry((seed*374761393+ix*668265263+iz*2246822519)|0)();}
            function vnoise(x,z,seed){
                var x0=Math.floor(x),z0=Math.floor(z),fx=ease(x-x0),fz=ease(z-z0);
                var a=lattice(x0,z0,seed),b=lattice(x0+1,z0,seed),c=lattice(x0,z0+1,seed),d=lattice(x0+1,z0+1,seed);
                var top=a+(b-a)*fx,bottom=c+(d-c)*fx;
                return top+(bottom-top)*fz;
            }
            function fbm(x,z,seed){
                return (vnoise(x,z,seed)+0.5*vnoise(x*2+7.3,z*2+3.1,seed+1)+0.25*vnoise(x*4+1.7,z*4+9.2,seed+2))/1.75;
            }
            function terrainGeometry(n){
                var w=n.size?n.size[0]:10,d=n.size?n.size[1]:10;
                var seg=Math.max(8,Math.min(256,n.seg||96));
                var g=new THREE.PlaneGeometry(w,d,seg,seg);
                g.rotateX(-Math.PI/2);   // baked flat: the node's own rotation stays free
                if(n.hills&&n.hills[0]>0){
                    var h=n.hills[0],sc=(n.hills[1]>0)?n.hills[1]:w/4,seed=n.seed!=null?n.seed:1;
                    var pos=g.attributes.position;
                    for(var i=0;i<pos.count;i++){
                        pos.setY(i,(fbm(pos.getX(i)/sc,pos.getZ(i)/sc,seed)-0.5)*2*h);
                    }
                    pos.needsUpdate=true;
                }
                g.computeVertexNormals();
                return g;
            }

            function material(n,inst){
                var m;
                if(n.glass!=null&&n.glass!==false){
                    // physical transmission: light passes through and refracts
                    m=new THREE.MeshPhysicalMaterial({color:color(n.color,'#ffffff'),
                        transmission:n.glass===true?1:n.glass,roughness:n.rough!=null?n.rough:0.05,
                        ior:1.5,thickness:0.5,transparent:false});
                }else{
                    m=new THREE.MeshStandardMaterial({color:color(n.color,'#8b9dc3')});
                }
                if(n.emissive)m.emissive=color(n.emissive,'#000000');
                if(n.metal!=null)m.metalness=n.metal;
                if(n.rough!=null)m.roughness=n.rough;
                if(n.opacity!=null){m.transparent=true;m.opacity=n.opacity;}
                if(n.wire)m.wireframe=true;
                if(n.t==='plane'||n.t==='disc'||n.t==='ring'||n.t==='lathe')m.side=THREE.DoubleSide;
                if(n.map){
                    new THREE.TextureLoader().load(n.map,function(tex){
                        if(inst.disposed)return;
                        tex.colorSpace=THREE.SRGBColorSpace;
                        m.map=tex;m.needsUpdate=true;
                        if(!inst.raf)render(inst);
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
                if(n.click||n.swap||n.clickAct||n.link||n.hovScale||n.hovColor||n.hovEmissive){
                    obj.userData.jweb={click:n.click,swap:n.swap,act:n.clickAct,link:n.link,name:n.name||'',
                        hovScale:n.hovScale,hovColor:n.hovColor,hovEmissive:n.hovEmissive};
                    inst.interactive=true;
                    if(n.click||n.swap||n.clickAct||n.link)inst.clickable=true;
                }
                // places that react: .solid() blocks the walker, .near(d) watches the camera's distance
                if(n.solid!=null)inst.solidNodes.push({obj:obj,r:typeof n.solid==='number'?n.solid:null});
                if(n.near!=null)inst.nears.push({obj:obj,d:n.near,name:n.name||'',inside:false,
                    enterH:n.nearH,enterAct:n.nearAct,leaveH:n.farH,leaveAct:n.farAct});
                applyPresets(obj,n,inst);
            }

            // ==================== Motion presets ====================
            // pulse/glow/follow run every frame; appear is a one-shot tween;
            // delay holds all of them (spin/float included) until it elapses

            var _fm=new THREE.Matrix4(),_fv=new THREE.Vector3(),_fup=new THREE.Vector3(0,1,0);

            function applyPresets(obj,n,inst){
                var delay=(n.delay||0)/1000;
                var appearEnd=n.appear?(n.appear[0]+n.appear[1])/1000:0;
                var start=inst.time+delay+appearEnd;   // continuous presets begin once the reveal is done
                if(n.delay&&(n.spin||n['float'])){
                    // spin/float were queued just above; hold them back until the delay elapses
                    var held=[];
                    if(n.spin&&inst.spins.length&&inst.spins[inst.spins.length-1].obj===obj)held.push({list:inst.spins,item:inst.spins.pop()});
                    if(n['float']&&inst.floats.length&&inst.floats[inst.floats.length-1].obj===obj)held.push({list:inst.floats,item:inst.floats.pop()});
                    if(held.length){inst.pending.push({at:inst.time+delay,held:held});inst.needsLoop=true;}
                }
                if(n.pulse){
                    // base is captured on the first frame, after billboards have sized themselves
                    inst.pulses.push({obj:obj,amt:n.pulse[0],cps:n.pulse[1],base:null,t0:start});
                    inst.needsLoop=true;
                }
                if(n.glow&&obj.material&&!Array.isArray(obj.material)&&obj.material.emissive){
                    inst.glows.push({obj:obj,mat:obj.material,cps:n.glow,base:obj.material.emissiveIntensity,t0:start});
                    inst.needsLoop=true;
                }
                if(n.follow&&n.follow.pts&&n.follow.pts.length>=6){
                    var fp=[];
                    for(var i=0;i+2<n.follow.pts.length;i+=3)fp.push(new THREE.Vector3(n.follow.pts[i],n.follow.pts[i+1],n.follow.pts[i+2]));
                    var fo={obj:obj,curve:new THREE.CatmullRomCurve3(fp,true),sec:n.follow.sec>0?n.follow.sec:10,
                            rot:obj.quaternion.clone(),t0:inst.time+delay};
                    inst.follows.push(fo);
                    inst.needsLoop=true;
                    stepFollow(fo,0);
                }
                if(n.appear){
                    // hidden until the first frame captures the declared scale (billboards
                    // size themselves after applyCommon), then scales in from nothing
                    var am=n.appear[0]/1000,ad=n.appear[1]/1000,at0=inst.time,target=null;
                    obj.visible=false;
                    inst.tweens.push({t0:at0,dur:ad+am,step:function(){
                        if(!target){target=obj.scale.clone();obj.visible=true;}
                        var k=Math.max(0,Math.min(1,(inst.time-at0-ad)/am));
                        obj.scale.copy(target).multiplyScalar(Math.max(1e-4,ease(k)));
                    }});
                }
            }

            function stepFollow(f,u){
                f.curve.getPointAt(u,f.obj.position);
                var tan=f.curve.getTangentAt(u);
                _fm.lookAt(_fv.copy(f.obj.position).add(tan),f.obj.position,_fup);   // +z along the path
                f.obj.quaternion.setFromRotationMatrix(_fm).multiply(f.rot);
            }

            function stepPresets(inst){
                if(inst.pending.length){
                    inst.pending=inst.pending.filter(function(p){
                        if(inst.time<p.at)return true;
                        p.held.forEach(function(h){h.list.push(h.item);});
                        return false;
                    });
                }
                inst.pulses.forEach(function(p){
                    var t=inst.time-p.t0;
                    if(t<0)return;
                    if(!p.base)p.base=p.obj.scale.clone();
                    var s=1+Math.sin(t*p.cps*Math.PI*2)*p.amt;
                    p.obj.scale.set(p.base.x*s,p.base.y*s,p.base.z*s);
                });
                inst.glows.forEach(function(g){
                    var t=inst.time-g.t0;
                    if(t<0)return;
                    g.mat.emissiveIntensity=g.base*(0.8+0.2*Math.sin(t*g.cps*Math.PI*2));   // 60%..100%
                });
                inst.follows.forEach(function(f){
                    var t=inst.time-f.t0;
                    if(t<0)return;
                    stepFollow(f,(t/f.sec)%1);
                });
            }

            function buildNode(n,inst,parent){
                if(n.t==='plane'&&n.mirror){
                    if(!THREE.Reflector){console.warn('[JWeb] .mirror() needs the Reflector addon — rebuild the three bundle');}
                    else{
                        var ms=n.size||[1,1];
                        var refl=new THREE.Reflector(new THREE.PlaneGeometry(ms[0],ms[1]),{
                            color:n.color||'#889199',
                            textureWidth:1024,textureHeight:1024,clipBias:0.003});
                        applyCommon(refl,n,inst);
                        addMirrorFinish(refl,n,inst,ms);
                        parent.add(refl);
                        return;
                    }
                }
                var geo=geometry(n);
                if(geo){
                    var mesh=new THREE.Mesh(geo,material(n,inst));
                    if(inst.shadows){mesh.castShadow=true;mesh.receiveShadow=true;}
                    applyCommon(mesh,n,inst);
                    parent.add(mesh);
                }else if(n.t==='line'){
                    buildLine(n,inst,parent);
                }else if(n.t==='spotLight'){
                    buildSpotLight(n,inst);
                }else if(n.t==='group'&&n.inst){
                    buildInstanced(n,inst,parent);
                }else if(n.t==='group'){
                    var g=new THREE.Group();
                    applyCommon(g,n,inst);
                    (n.children||[]).forEach(function(c){buildNode(c,inst,g);});
                    parent.add(g);
                }else if(n.t==='particles'){
                    var cnt=n.count||100,spread=n.spread||[10,10,10];
                    var rand=mulberry(n.seed!=null?n.seed:1);
                    var pos=new Float32Array(cnt*3),orig=new Float32Array(cnt*3),phase=new Float32Array(cnt);
                    for(var pi=0;pi<cnt;pi++){
                        orig[pi*3]=pos[pi*3]=(rand()-0.5)*spread[0];
                        orig[pi*3+1]=pos[pi*3+1]=(rand()-0.5)*spread[1];
                        orig[pi*3+2]=pos[pi*3+2]=(rand()-0.5)*spread[2];
                        phase[pi]=rand()*Math.PI*2;
                    }
                    // palette: drawn after positions so adding colors never reshuffles the cloud
                    var cols=null;
                    if(n.colors&&n.colors.length){
                        var pal=n.colors.map(function(c){return new THREE.Color(c);});
                        cols=new Float32Array(cnt*3);
                        for(var ci=0;ci<cnt;ci++){
                            var pcol=pal[Math.min(pal.length-1,Math.floor(rand()*pal.length))];
                            cols[ci*3]=pcol.r;cols[ci*3+1]=pcol.g;cols[ci*3+2]=pcol.b;
                        }
                    }
                    var pg=new THREE.BufferGeometry();
                    pg.setAttribute('position',new THREE.BufferAttribute(pos,3));
                    if(cols)pg.setAttribute('color',new THREE.BufferAttribute(cols,3));
                    var pm=new THREE.PointsMaterial({color:cols?new THREE.Color('#ffffff'):color(n.color,'#ffffff'),
                        vertexColors:!!cols,
                        size:n.size!=null?n.size:0.05,sizeAttenuation:true,
                        transparent:true,opacity:n.opacity!=null?n.opacity:1,depthWrite:false});
                    var pts=new THREE.Points(pg,pm);
                    applyCommon(pts,n,inst);
                    parent.add(pts);
                    if(n.drift||n.fall){
                        inst.pclouds.push({geo:pg,orig:orig,phase:phase,count:cnt,
                            drift:n.drift||0,fall:n.fall||0,spread:spread,fallOff:0});
                        inst.needsLoop=true;
                    }
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
                        if(!inst.raf)render(inst);
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
                        if(!inst.raf)render(inst);
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
                        if(!inst.raf)render(inst);
                    },undefined,function(){console.error('[JWeb] environment failed:',n.url);});
                }else if(n.t==='zone'){
                    inst.zones.push({box:n.box,name:n.name||'',inside:false,link:n.link,
                        enterH:n.enterH,enterAct:n.enterAct,leaveH:n.leaveH,leaveAct:n.leaveAct});
                }else if(n.t==='sound'){
                    buildSound(n,inst,parent);
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
                }else if(n.t==='tone'){
                    inst.toneCfg=n;
                }else if(n.t==='bloom'){
                    inst.bloomCfg=n;
                }else if(n.t!=='camera'){
                    console.warn('[JWeb] unknown scene node type:',n.t);
                }
            }

            // ==================== Lines, spot lights, satin mirrors ====================

            // Lines: a 1px unlit polyline; dashed/draw use LineDashedMaterial
            function buildLine(n,inst,parent){
                var lp=[];
                for(var i=0;i+2<n.pts.length;i+=3)lp.push(new THREE.Vector3(n.pts[i],n.pts[i+1],n.pts[i+2]));
                if(n.closed)lp.push(lp[0].clone());
                var lg=new THREE.BufferGeometry().setFromPoints(lp);
                var opts={color:color(n.color,'#ffffff')};
                if(n.opacity!=null){opts.transparent=true;opts.opacity=n.opacity;}
                var dashed=!!(n.dash||n.draw),lm;
                if(dashed){
                    opts.dashSize=n.dash?n.dash[0]:1;opts.gapSize=n.dash?n.dash[1]:0;
                    lm=new THREE.LineDashedMaterial(opts);
                }else{
                    lm=new THREE.LineBasicMaterial(opts);
                }
                var line=new THREE.Line(lg,lm);
                line.raycast=function(){};   // 1px lines make poor click targets; never steal a hit
                if(dashed)line.computeLineDistances();
                applyCommon(line,n,inst);
                parent.add(line);
                if(n.draw){
                    // draws itself: a dash growing from 0 to the line's length, one gap behind it
                    var ld=lg.attributes.lineDistance,total=ld.array[ld.count-1]||1;
                    var dm=n.draw[0]/1000,dd=n.draw[1]/1000,t0=inst.time;
                    lm.dashSize=0;lm.gapSize=total*2;
                    inst.tweens.push({t0:t0,dur:dm+dd,step:function(){
                        var k=Math.max(0,Math.min(1,(inst.time-t0-dd)/dm));
                        if(k<1){lm.dashSize=total*ease(k);lm.gapSize=total*2;}
                        else if(n.dash){lm.dashSize=n.dash[0];lm.gapSize=n.dash[1];}
                        else{lm.dashSize=total*2;lm.gapSize=0;}
                    }});
                }
            }

            // Spot lights: a cone from pos toward target (default: straight down)
            function buildSpotLight(n,inst){
                var sp=new THREE.SpotLight(color(n.color,'#ffffff'),n.intensity!=null?n.intensity:1);
                if(n.pos)vec(sp.position,n.pos);else sp.position.set(2,4,2);
                sp.angle=(n.angle!=null?n.angle:30)*RAD;
                sp.penumbra=n.penumbra!=null?n.penumbra:0.3;
                if(n.target)vec(sp.target.position,n.target);
                else sp.target.position.set(sp.position.x,sp.position.y-1,sp.position.z);
                if(n.shadows){sp.castShadow=true;sp.shadow.mapSize.set(1024,1024);sp.shadow.bias=-0.0005;}
                if(n.name)inst.objects[n.name]=sp;
                sp.userData.jwebTarget=sp.target;   // so a remove patch takes the target along
                inst.hasLight=true;
                inst.scene.add(sp);
                inst.scene.add(sp.target);
            }

            // Satin mirrors: a translucent lid a hair above the Reflector dims it to strength
            function addMirrorFinish(refl,n,inst,ms){
                var strength=n.mirror===true?1:n.mirror;
                if(!(strength<1))return;
                var lid=new THREE.Mesh(new THREE.PlaneGeometry(ms[0],ms[1]),
                    new THREE.MeshStandardMaterial({color:color(n.color,'#889199'),transparent:true,
                        opacity:1-strength,roughness:n.rough!=null?n.rough:0.6}));
                lid.position.z=0.002;   // along the plane's local normal
                if(inst.shadows)lid.receiveShadow=true;
                refl.add(lid);
            }

            // ==================== Instanced groups ====================
            // children batched into one InstancedMesh per geometry+material
            // signature; color is per instance. Members needing their own
            // behaviour (spin/float/click/hover/name/presets) fall back to meshes.

            var INSTANCEABLE={box:1,sphere:1,plane:1,cylinder:1,cone:1,torus:1,capsule:1,disc:1,ring:1,
                knot:1,tetra:1,octa:1,dodeca:1,icosa:1,tube:1,arc:1,lathe:1,sweep:1,terrain:1};

            function perInstanceOnly(c){
                return !!(c.spin||c['float']||c.click||c.clickAct||c.swap||c.hovScale||c.hovColor
                    ||c.hovEmissive||c.name||c.pulse||c.glow||c.appear||c.follow||c.delay);
            }

            function localMatrix(c){
                var p=c.pos||[0,0,0],r=c.rot||[0,0,0],s=c.scl||[1,1,1];
                return new THREE.Matrix4().compose(new THREE.Vector3(p[0],p[1],p[2]),
                    new THREE.Quaternion().setFromEuler(new THREE.Euler(r[0]*RAD,r[1]*RAD,r[2]*RAD)),
                    new THREE.Vector3(s[0],s[1],s[2]));
            }

            function signature(c){
                var s={};
                for(var k in c)if(k!=='pos'&&k!=='rot'&&k!=='scl'&&k!=='color')s[k]=c[k];
                return JSON.stringify(s);
            }

            function buildInstanced(n,inst,parent){
                var g=new THREE.Group();
                applyCommon(g,n,inst);
                parent.add(g);
                var batches={},keys=[],fallbacks=0;
                function collect(children,holder,mat){
                    (children||[]).forEach(function(c){
                        if(c.t==='group'&&!c.inst&&!perInstanceOnly(c)){
                            // a plain nested group flattens: its transform composes into the instances
                            var sub=new THREE.Group();
                            applyCommon(sub,c,inst);
                            holder.add(sub);
                            collect(c.children,sub,new THREE.Matrix4().multiplyMatrices(mat,localMatrix(c)));
                            return;
                        }
                        var mirror=c.t==='plane'&&c.mirror;
                        if(perInstanceOnly(c)||mirror||!INSTANCEABLE[c.t]){
                            if(perInstanceOnly(c)&&(INSTANCEABLE[c.t]||c.t==='group'))fallbacks++;
                            buildNode(c,inst,holder);
                            return;
                        }
                        var sig=signature(c),b=batches[sig];
                        if(!b){b=batches[sig]={proto:c,items:[],colored:false};keys.push(sig);}
                        b.items.push({mat:new THREE.Matrix4().multiplyMatrices(mat,localMatrix(c)),color:c.color||null});
                        if(c.color)b.colored=true;
                    });
                }
                collect(n.children,g,new THREE.Matrix4());
                keys.forEach(function(k){
                    var b=batches[k],proto=b.proto;
                    if(b.colored){proto=Object.assign({},proto);proto.color='#ffffff';}   // tint per instance
                    var im=new THREE.InstancedMesh(geometry(proto),material(proto,inst),b.items.length);
                    b.items.forEach(function(it,i){
                        im.setMatrixAt(i,it.mat);
                        if(b.colored)im.setColorAt(i,color(it.color,'#8b9dc3'));
                    });
                    im.instanceMatrix.needsUpdate=true;
                    if(im.instanceColor)im.instanceColor.needsUpdate=true;
                    if(inst.shadows){im.castShadow=true;im.receiveShadow=true;}
                    g.add(im);
                });
                if(fallbacks)console.warn('[JWeb] instanced group: '+fallbacks+' member(s) carry spin/float/click/hover/name/presets and fall back to normal meshes');
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
                if(!inst.raf)render(inst);
            }

            function wireInteraction(inst){
                var canvas=inst.renderer.domElement;
                inst.pointer=new THREE.Vector2();
                inst.ray=new THREE.Raycaster();
                canvas.addEventListener('click',function(ev){
                    // a walk-mode look-drag ends in a click; don't treat it as one
                    if(inst.suppressClick){inst.suppressClick=false;return;}
                    var h=hit(inst,ev);
                    if(!h)return;
                    var d=h.d;
                    if(d.click&&window.JWeb){
                        JWeb.call(d.click,synthetic(inst,'click',d.name,ev));
                    }else if(d.act&&window.JWeb&&JWeb.runAction){
                        // Actions-DSL handler: defined in __JWEB_ACTIONS__ by
                        // the page's nonce'd definitions script; this = canvas,
                        // event = the real click event
                        JWeb.runAction(d.act,ev,canvas);
                    }else if(d.swap&&window.JWeb){
                        JWeb.swap(d.swap.url,null,{target:d.swap.target});
                    }else if(d.link){
                        navigate(inst,d.link);
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
                        canvas.style.cursor=(h&&(h.d.click||h.d.act||h.d.swap||h.d.link))?'pointer':'';
                        setHovered(inst,h?h.o:null);
                    });
                });
                canvas.addEventListener('pointerleave',function(){
                    if(!inst.disposed)setHovered(inst,null);
                });
            }

        """;

    private static final String PART2 = """
            // ==================== Sound ====================

            function audioListener(inst){
                if(!inst.listener)inst.listener=new THREE.AudioListener();
                return inst.listener;
            }

            // sound(url): global Audio, or PositionalAudio when the node has a position
            function buildSound(n,inst,parent){
                var listener=audioListener(inst);
                var positional=!!n.pos;
                var au=positional?new THREE.PositionalAudio(listener):new THREE.Audio(listener);
                au.setLoop(!!n.loop);
                au.setVolume(n.vol!=null?n.vol:1);
                if(positional)au.setRefDistance(n.ref!=null?n.ref:4);
                au.userData.jwebSound={pending:!n.paused,loaded:false};
                applyCommon(au,n,inst);
                parent.add(au);
                inst.sounds.push(au);
                new THREE.AudioLoader().load(n.url,function(buf){
                    if(inst.disposed)return;
                    au.setBuffer(buf);
                    au.userData.jwebSound.loaded=true;
                    if(au.userData.jwebSound.pending&&audioUnlocked)startSound(au);
                },undefined,function(){console.error('[JWeb] sound failed:',n.url);});
            }

            function startSound(au){
                var s=au.userData.jwebSound;
                if(!s.loaded||au.isPlaying)return;
                try{au.play();}catch(e){console.warn('[JWeb] sound could not start:',e);}
            }

            // browsers unlock audio only inside a user gesture: the first
            // click, tap or key press starts every sound that is waiting
            function wireAudioUnlock(){
                if(audioWired)return;
                audioWired=true;
                var unlock=function(){
                    var ctx=THREE.AudioContext.getContext();
                    var go=function(){
                        audioUnlocked=true;
                        instances.forEach(function(inst){
                            inst.sounds.forEach(function(au){if(au.userData.jwebSound.pending)startSound(au);});
                        });
                    };
                    if(ctx.state==='suspended'&&ctx.resume)ctx.resume().then(go,go);else go();
                    document.removeEventListener('pointerdown',unlock,true);
                    document.removeEventListener('keydown',unlock,true);
                    document.removeEventListener('touchend',unlock,true);
                };
                document.addEventListener('pointerdown',unlock,true);
                document.addEventListener('keydown',unlock,true);
                document.addEventListener('touchend',unlock,true);
            }

            function applySoundPatch(inst,obj,p){
                var s=obj.userData.jwebSound,dur=p.tween||0;
                if(p.vol!=null){
                    var vf=obj.getVolume(),vt=p.vol;
                    startTween(inst,dur,function(k){obj.setVolume(vf+(vt-vf)*k);});
                }
                if(p.play===true){
                    s.pending=true;
                    if(obj.isPlaying)obj.stop();
                    if(audioUnlocked)startSound(obj);
                }else if(p.play===false){
                    s.pending=false;
                    if(obj.isPlaying)obj.stop();
                }
            }

            function loadSteps(inst){
                var st=inst.walk.steps;
                new THREE.AudioLoader().load(st.url,function(buf){
                    if(!inst.disposed)st.buffer=buf;
                },undefined,function(){console.error('[JWeb] footsteps failed:',st.url);});
            }

            // one stride: the sampled clip, or a synthesized scuff-and-thud
            function footstep(inst){
                var st=inst.walk.steps;
                if(!st||!audioUnlocked)return;
                var ctx=THREE.AudioContext.getContext();
                if(ctx.state!=='running')return;
                var t=ctx.currentTime,vol=(st.vol!=null?st.vol:0.5)*(0.8+Math.random()*0.4);
                if(st.url){
                    if(!st.buffer)return;
                    var src=ctx.createBufferSource();
                    src.buffer=st.buffer;
                    src.playbackRate.value=0.9+Math.random()*0.2;
                    var sg=ctx.createGain();sg.gain.value=vol;
                    src.connect(sg);sg.connect(ctx.destination);
                    src.start(t);
                    return;
                }
                if(!inst.noise){
                    var nb=ctx.createBuffer(1,ctx.sampleRate,ctx.sampleRate),nd=nb.getChannelData(0);
                    for(var i=0;i<nd.length;i++)nd[i]=Math.random()*2-1;
                    inst.noise=nb;
                }
                var s=ctx.createBufferSource();s.buffer=inst.noise;s.playbackRate.value=0.7+Math.random()*0.5;
                var f=ctx.createBiquadFilter();f.type='lowpass';f.frequency.value=900*(0.85+Math.random()*0.3);
                var g=ctx.createGain();
                g.gain.setValueAtTime(vol*0.16,t);g.gain.exponentialRampToValueAtTime(0.001,t+0.1);
                s.connect(f);f.connect(g);g.connect(ctx.destination);
                s.start(t);s.stop(t+0.15);
                var o=ctx.createOscillator(),og=ctx.createGain();
                o.frequency.setValueAtTime(85*(0.9+Math.random()*0.2),t);
                o.frequency.exponentialRampToValueAtTime(51,t+0.1);
                og.gain.setValueAtTime(vol*0.11,t);og.gain.exponentialRampToValueAtTime(0.001,t+0.14);
                o.connect(og);og.connect(ctx.destination);
                o.start(t);o.stop(t+0.2);
            }

            // ==================== The camera's place in the page ====================

            function poseOf(inst){
                var p=inst.pose;
                return {x:p.x,y:p.y,z:p.z,yaw:p.yaw,pitch:p.pitch,walking:!!(inst.walk&&inst.walk.active)};
            }

            // --three-yaw / --three-pitch on the scene element and a bubbling
            // jweb:three-look event whenever the camera turns or moves —
            // compass needles and HUDs in CSS, no script
            function syncPose(inst,force){
                var cam=inst.camera,p=inst.pose;
                cam.getWorldDirection(TMP);
                var yaw=Math.atan2(-TMP.x,-TMP.z)/RAD,pitch=Math.asin(Math.max(-1,Math.min(1,TMP.y)))/RAD;
                if(!force&&Math.abs(yaw-p.yaw)<0.1&&Math.abs(pitch-p.pitch)<0.1
                   &&Math.abs(cam.position.x-p.x)<0.01&&Math.abs(cam.position.y-p.y)<0.01
                   &&Math.abs(cam.position.z-p.z)<0.01)return;
                p.yaw=yaw;p.pitch=pitch;p.x=cam.position.x;p.y=cam.position.y;p.z=cam.position.z;
                inst.el.style.setProperty('--three-yaw',yaw.toFixed(1)+'deg');
                inst.el.style.setProperty('--three-pitch',pitch.toFixed(1)+'deg');
                inst.el.dispatchEvent(new CustomEvent('jweb:three-look',{bubbles:true,
                    detail:{id:inst.el.id||'',yaw:yaw,pitch:pitch,x:p.x,y:p.y,z:p.z,
                            walking:!!(inst.walk&&inst.walk.active)}}));
            }

            // the event a server handler receives: value = node name,
            // dataset.pose = where the camera stood ("x,y,z,yaw")
            function synthetic(inst,type,name,ev){
                var p=poseOf(inst);
                return {type:type,clientX:ev?ev.clientX:-1,clientY:ev?ev.clientY:-1,
                    target:{id:inst.el.id||'',value:name||'',
                        dataset:{mesh:name||'',scene:inst.el.id||'',
                            pose:p.x.toFixed(2)+','+p.y.toFixed(2)+','+p.z.toFixed(2)+','+p.yaw.toFixed(1)}}};
            }

            function navigate(inst,url){
                document.body.classList.add('three-crossing');
                if(window.JWebNav&&JWebNav.navigate)JWebNav.navigate(url);
                else location.href=url;
            }

            // zones and near() nodes: enter/leave once per crossing, with a
            // little hysteresis so standing on the line doesn't flicker. The
            // first pass only records where the camera already is.
            function stepProximity(inst){
                var c=inst.camera.position,quiet=!inst.proxInit,i,z,nr,d,now;
                inst.proxInit=true;
                for(i=0;i<inst.zones.length;i++){
                    z=inst.zones[i];
                    now=c.x>=z.box[0]&&c.x<=z.box[2]&&c.z>=z.box[1]&&c.z<=z.box[3];
                    if(now!==z.inside){z.inside=now;fireProximity(inst,z,now,'zone',quiet);}
                }
                for(i=0;i<inst.nears.length;i++){
                    nr=inst.nears[i];
                    nr.obj.getWorldPosition(TMP);
                    d=TMP.distanceTo(c);
                    now=nr.inside?d<nr.d*1.15:d<nr.d;
                    if(now!==nr.inside){nr.inside=now;fireProximity(inst,nr,now,'near',quiet);}
                }
            }

            function fireProximity(inst,p,entering,kind,quiet){
                var el=inst.el,name=p.name,canvas=inst.renderer.domElement;
                if(name){
                    el.classList.toggle('three-near-'+name,entering);
                    document.body.classList.toggle('three-near-'+name,entering);
                }
                if(quiet)return;
                el.dispatchEvent(new CustomEvent('jweb:three-'+kind,{bubbles:true,
                    detail:{id:el.id||'',name:name,inside:entering,pose:poseOf(inst)}}));
                var h=entering?p.enterH:p.leaveH,a=entering?p.enterAct:p.leaveAct;
                var type=kind==='zone'?(entering?'enter':'leave'):(entering?'near':'far');
                if(h&&window.JWeb)JWeb.call(h,synthetic(inst,type,name,null));
                if(a&&window.JWeb&&JWeb.runAction)
                    JWeb.runAction(a,{type:type,target:canvas,preventDefault:function(){}},canvas);
                if(entering&&p.link)navigate(inst,p.link);
            }

            function afterFrame(inst){
                if(inst.zones.length||inst.nears.length)stepProximity(inst);
                syncPose(inst,false);
            }

            // ==================== Walk mode ====================

            var WALK_KEYS={'w':1,'a':1,'s':1,'d':1,' ':1,'shift':1,'arrowup':1,'arrowdown':1,'arrowleft':1,'arrowright':1};

            function clampPitch(p){return Math.max(-1.2,Math.min(1.2,p));}

            function inInput(e){
                var t=e.target;
                return !!(t&&t.closest&&t.closest('input,textarea,select,[contenteditable]'));
            }

            // three-key-w … on the scene element and <body> while a key is down
            function keyClass(inst,k,on){
                if(!WALK_KEYS[k])return;
                var cls='three-key-'+(k===' '?'space':k);
                inst.el.classList.toggle(cls,on);
                document.body.classList.toggle(cls,on);
            }

            function clearKeyClasses(inst){
                for(var k in WALK_KEYS)keyClass(inst,k,false);
            }

            // the ground under the walker: the highest upward-facing surface
            // below the feet — walkways, steps and dune slopes all carry you.
            // Cached per 2cm / 200ms so the raycast stays cheap.
            function ground(inst,x,z,from){
                var w=inst.walk;
                if(!w.ground)return w.floor;
                if(from==null&&Math.abs(x-w.gX)<0.02&&Math.abs(z-w.gZ)<0.02&&inst.time-w.gT<0.2)return w.floor;
                w.gX=x;w.gZ=z;w.gT=inst.time;
                if(!inst.gray)inst.gray=new THREE.Raycaster();
                inst.gray.camera=inst.camera;   // sprites refuse to raycast without one
                TMP.set(x,from==null?w.floor+0.6:from,z);
                inst.gray.set(TMP,DOWN);
                inst.gray.far=from==null?4.5:from+4;
                var hits;
                try{hits=inst.gray.intersectObjects(inst.scene.children,true);}
                catch(e){console.warn('[JWeb] ground raycast failed:',e);return w.floor;}
                for(var i=0;i<hits.length;i++){
                    var o=hits[i].object,f=hits[i].face;
                    if(!f||!o.isMesh||!o.visible)continue;
                    var m=o.material;
                    if(m&&!Array.isArray(m)&&m.transparent&&m.opacity<0.5)continue;
                    NORMAL.copy(f.normal).applyMatrix3(NMAT.getNormalMatrix(o.matrixWorld)).normalize();
                    if(NORMAL.y<0.35)continue;
                    w.floor=hits[i].point.y;
                    break;
                }
                return w.floor;
            }

            // colliders: .solid() = the node's world-space footprint, .solid(r) = a cylinder
            function collectSolids(inst){
                var out=[],box=new THREE.Box3();
                inst.solidNodes.forEach(function(s){
                    var o=s.obj;
                    if(!o.parent)return;   // removed by a patch
                    o.updateWorldMatrix(true,true);
                    box.setFromObject(o);
                    var minY=box.isEmpty()?-1e9:box.min.y,maxY=box.isEmpty()?1e9:box.max.y;
                    if(s.r!=null){
                        o.getWorldPosition(TMP);
                        out.push({c:true,x:TMP.x,z:TMP.z,r:s.r,minY:minY,maxY:maxY});
                    }else if(!box.isEmpty()){
                        out.push({c:false,minX:box.min.x,minZ:box.min.z,maxX:box.max.x,maxZ:box.max.z,minY:minY,maxY:maxY});
                    }
                });
                inst.solids=out;
            }

            function collide(inst,x,z){
                var w=inst.walk,r=w.radius,feet=w.floor;
                for(var i=0;i<inst.solids.length;i++){
                    var s=inst.solids[i];
                    if(s.maxY<feet+0.35||s.minY>feet+w.eye)continue;   // step over it / walk under it
                    if(s.c){
                        var dx=x-s.x,dz=z-s.z,d=Math.hypot(dx,dz),min=s.r+r;
                        if(d<min&&d>1e-4){x=s.x+dx/d*min;z=s.z+dz/d*min;}
                    }else{
                        var minX=s.minX-r,maxX=s.maxX+r,minZ=s.minZ-r,maxZ=s.maxZ+r;
                        if(x>minX&&x<maxX&&z>minZ&&z<maxZ){
                            // push out along the axis of least penetration
                            var px=Math.min(x-minX,maxX-x),pz=Math.min(z-minZ,maxZ-z);
                            if(px<pz)x=(x-minX<maxX-x)?minX:maxX;
                            else z=(z-minZ<maxZ-z)?minZ:maxZ;
                        }
                    }
                }
                if(w.bounds){
                    x=Math.max(w.bounds[0],Math.min(w.bounds[2],x));
                    z=Math.max(w.bounds[1],Math.min(w.bounds[3],z));
                }
                return [x,z];
            }

            function readGamepad(){
                var gps=navigator.getGamepads?navigator.getGamepads():null;
                if(!gps)return null;
                for(var i=0;i<gps.length;i++){
                    var g=gps[i];
                    if(!g)continue;
                    var a=g.axes||[],b=g.buttons||[];
                    return {s:a[0]||0,f:-(a[1]||0),turn:a[2]||0,look:a[3]||0,
                            up:!!(b[0]&&b[0].pressed),run:!!(b[1]&&b[1].pressed)};
                }
                return null;
            }

            function setWalk(inst,on){
                var w=inst.walk;
                if(!w||w.active===on||inst.disposed)return;
                w.active=on;
                var cam=inst.camera,el=inst.el;
                if(on){
                    // one walker at a time — stepping into this scene steps
                    // out of any other
                    instances.forEach(function(other){
                        if(other!==inst&&other.walk&&other.walk.active)setWalk(other,false);
                    });
                    if(w.spawn){
                        w.px=w.spawn[0];w.pz=w.spawn[1];w.yaw=w.spawn[2]*RAD;w.pitch=0;
                    }else{
                        var dir=cam.getWorldDirection(new THREE.Vector3());
                        w.yaw=Math.atan2(-dir.x,-dir.z);
                        w.pitch=Math.asin(Math.max(-1,Math.min(1,dir.y)))*0.35;
                        w.px=cam.position.x;w.pz=cam.position.z;
                    }
                    w.keys={};w.bob=0;w.glide=null;w.flyV=0;w.flyS=0;w.lastStep=0;w.stick=null;
                    collectSolids(inst);w.solidsAt=inst.time;
                    var cp=collide(inst,w.px,w.pz);w.px=cp[0];w.pz=cp[1];
                    // settle onto your feet: find the floor, ease down to eye
                    // height above it, and let the gaze level out
                    w.floor=0;w.gT=-1e9;
                    w.floorS=ground(inst,w.px,w.pz,(w.spawn?w.eye:cam.position.y)+0.5);
                    w.baseY=w.spawn?w.floorS+w.eye:cam.position.y;
                    w.settle=w.spawn?0:0.9;
                    cam.rotation.order='YXZ';
                    cam.rotation.set(w.pitch,w.yaw,0);
                    cam.position.set(w.px,w.baseY,w.pz);
                    if(inst.controls)inst.controls.enabled=false;
                    w.keydown=function(e){
                        if(e.key==='Escape'){setWalk(inst,false);return;}
                        if(inInput(e))return;
                        var k=e.key.toLowerCase();
                        w.keys[k]=true;keyClass(inst,k,true);
                        if(k.indexOf('arrow')===0||k===' ')e.preventDefault();
                    };
                    w.keyup=function(e){var k=e.key.toLowerCase();w.keys[k]=false;keyClass(inst,k,false);};
                    w.blur=function(){w.keys={};clearKeyClasses(inst);};
                    document.addEventListener('keydown',w.keydown);
                    document.addEventListener('keyup',w.keyup);
                    window.addEventListener('blur',w.blur);
                    if(w.plock&&inst.renderer.domElement.requestPointerLock){
                        try{inst.renderer.domElement.requestPointerLock();}catch(e){}
                    }
                }else{
                    document.removeEventListener('keydown',w.keydown);
                    document.removeEventListener('keyup',w.keyup);
                    window.removeEventListener('blur',w.blur);
                    clearKeyClasses(inst);
                    w.glide=null;w.stick=null;
                    if(w.plock&&document.pointerLockElement===inst.renderer.domElement&&document.exitPointerLock){
                        document.exitPointerLock();
                    }
                    // back to the framed view
                    var b=inst.camBase;
                    cam.position.set(b.pos[0],b.pos[1],b.pos[2]);
                    cam.lookAt(b.look[0],b.look[1],b.look[2]);
                    if(inst.controls){inst.controls.enabled=true;inst.controls.update();}
                }
                el.classList.toggle('three-walking',on);
                document.body.classList.toggle('three-walking',on);
                document.querySelectorAll('[data-three-walk]').forEach(function(btn){
                    if(resolveWalkTarget(btn)===el)btn.classList.toggle('three-walking',on);
                });
                el.dispatchEvent(new CustomEvent('jweb:three-walk',
                    {bubbles:true,detail:{id:el.id||'',walking:on}}));
                if(on)kick(inst);else{syncPose(inst,true);if(!inst.raf)render(inst);}
            }

            function stepWalk(inst,dt){
                var w=inst.walk,cam=inst.camera,k=w.keys;
                var gp=w.gamepad?readGamepad():null;
                var turn=(k['arrowleft']?1:0)-(k['arrowright']?1:0);
                if(gp&&Math.abs(gp.turn)>0.15)turn-=gp.turn;
                if(turn){w.yaw+=turn*1.9*dt;w.settle=0;}
                if(gp&&Math.abs(gp.look)>0.15){w.pitch=clampPitch(w.pitch-gp.look*1.2*dt);w.settle=0;}
                var f=((k['w']||k['arrowup'])?1:0)-((k['s']||k['arrowdown'])?1:0);
                var s=(k['d']?1:0)-(k['a']?1:0);
                if(w.stick){f+=w.stick.f;s+=w.stick.s;}
                if(gp){if(Math.abs(gp.f)>0.15)f+=gp.f;if(Math.abs(gp.s)>0.15)s+=gp.s;}
                f=Math.max(-1,Math.min(1,f));s=Math.max(-1,Math.min(1,s));
                var sp=(k['shift']||(gp&&gp.run))?w.run:w.speed;
                var moving=!!(f||s);
                if(moving)w.glide=null;
                if(w.glide){
                    // click-to-move: ease toward the chosen spot
                    w.px+=(w.glide.x-w.px)*Math.min(1,dt*2.6);
                    w.pz+=(w.glide.z-w.pz)*Math.min(1,dt*2.6);
                    w.bob+=dt*6;moving=true;
                    if(Math.hypot(w.glide.x-w.px,w.glide.z-w.pz)<0.08)w.glide=null;
                }
                if(f||s){
                    w.bob+=dt*7;
                    w.px+=(-Math.sin(w.yaw)*f+Math.cos(w.yaw)*s)*sp*dt;
                    w.pz+=(-Math.cos(w.yaw)*f-Math.sin(w.yaw)*s)*sp*dt;
                }
                if(inst.time-w.solidsAt>1.5){collectSolids(inst);w.solidsAt=inst.time;}
                var cp=collide(inst,w.px,w.pz);w.px=cp[0];w.pz=cp[1];
                // float: hold Space to rise, let go to sink (Shift hurries you down)
                if(w.fly){
                    var up=(k[' ']&&!k['shift'])||(gp&&gp.up);
                    w.flyV=up?Math.min(w.fly,w.flyV+3.2*dt):Math.max(0,w.flyV-(k['shift']?5:2.4)*dt);
                    w.flyS+=(w.flyV-w.flyS)*Math.min(1,dt*3.5);
                }
                var grounded=w.flyS<0.12;
                // feet on the ground: ride the surface underfoot; let the
                // gaze settle level as walking begins
                w.floorS+=(ground(inst,w.px,w.pz)-w.floorS)*Math.min(1,dt*9);
                if(w.settle>0){w.pitch+=(-0.05-w.pitch)*Math.min(1,dt*5);w.settle-=dt;}
                w.baseY+=((w.floorS+w.eye+w.flyS)-w.baseY)*Math.min(1,dt*7);
                var bobY=(!REDUCED&&moving&&grounded)?Math.sin(w.bob)*0.035:0;
                if(moving&&grounded&&w.steps&&w.bob-w.lastStep>Math.PI){w.lastStep=w.bob;footstep(inst);}
                cam.position.set(w.px,w.baseY+bobY,w.pz);
                cam.rotation.set(w.pitch,w.yaw,0);
            }

            // double-click on the ground: glide there
            function wireClickMove(inst){
                var w=inst.walk,canvas=inst.renderer.domElement;
                w.dbl=function(e){
                    if(!w.active){if(w.autoStart)setWalk(inst,true);else return;}
                    if(e.target&&e.target.closest&&e.target.closest('a,button,input,label,summary,select,textarea'))return;
                    e.preventDefault();
                    var r=canvas.getBoundingClientRect();
                    var v=new THREE.Vector3(((e.clientX-r.left)/r.width)*2-1,-((e.clientY-r.top)/r.height)*2+1,0.5);
                    v.unproject(inst.camera);
                    var d=v.sub(inst.camera.position).normalize();
                    var cx=w.px,cy=inst.camera.position.y,cz=w.pz,tx,tz;
                    if(d.y<-0.06){var t=(w.floorS-cy)/d.y;tx=cx+d.x*t;tz=cz+d.z*t;}
                    else{var hl=Math.hypot(d.x,d.z)||1;tx=cx+d.x/hl*4.5;tz=cz+d.z/hl*4.5;}
                    var ax=tx-cx,az=tz-cz,al=Math.hypot(ax,az)||1;
                    var keep=Math.max(0,al-1.3)/al;
                    var cp=collide(inst,cx+ax*keep,cz+az*keep);
                    w.glide={x:cp[0],z:cp[1]};
                    kick(inst);
                };
                canvas.addEventListener('dblclick',w.dbl);
            }

            // W A S D / arrows start walking without a toggle
            function wireAutoStart(inst){
                var w=inst.walk;
                w.autoKey=function(e){
                    if(w.active||e.metaKey||e.ctrlKey||e.altKey||inInput(e)||!inst.visible)return;
                    var k=e.key.toLowerCase();
                    if(!(k==='w'||k==='a'||k==='s'||k==='d'||k.indexOf('arrow')===0))return;
                    setWalk(inst,true);
                    w.keys[k]=true;keyClass(inst,k,true);
                    if(k.indexOf('arrow')===0)e.preventDefault();
                };
                document.addEventListener('keydown',w.autoKey);
            }

            // drag to look; with touch(), a thumb-stick on the left half moves;
            // with pointerLock(), the mouse alone looks
            function wireWalkLook(inst){
                var canvas=inst.renderer.domElement,w=inst.walk;
                var dragging=false,lx=0,ly=0,moved=0;
                if(w.touch)canvas.style.touchAction='none';
                canvas.addEventListener('pointerdown',function(e){
                    if(!w.active)return;
                    if(w.touch&&e.pointerType==='touch'){
                        var r=canvas.getBoundingClientRect();
                        if(e.clientX-r.left<r.width/2){
                            if(!w.stick)w.stick={id:e.pointerId,x0:e.clientX,y0:e.clientY,f:0,s:0};
                            return;
                        }
                    }
                    if(w.plock&&document.pointerLockElement!==canvas&&canvas.requestPointerLock){
                        try{canvas.requestPointerLock();}catch(err){}
                    }
                    dragging=true;moved=0;lx=e.clientX;ly=e.clientY;
                });
                w.pm=function(e){
                    if(!w.active)return;
                    if(w.stick&&e.pointerId===w.stick.id){
                        var sx=e.clientX-w.stick.x0,sy=e.clientY-w.stick.y0;
                        w.stick.s=Math.max(-1,Math.min(1,Math.abs(sx)<10?0:sx/60));
                        w.stick.f=Math.max(-1,Math.min(1,Math.abs(sy)<10?0:-sy/60));
                        return;
                    }
                    if(!dragging)return;
                    if(w.plock&&document.pointerLockElement===canvas)return;
                    var dx=e.clientX-lx,dy=e.clientY-ly;
                    moved+=Math.abs(dx)+Math.abs(dy);
                    w.settle=0;
                    w.yaw-=dx*0.003;
                    w.pitch=clampPitch(w.pitch-dy*0.003);
                    lx=e.clientX;ly=e.clientY;
                };
                w.pu=function(e){
                    if(w.stick&&e&&e.pointerId===w.stick.id){w.stick=null;return;}
                    if(dragging&&moved>6)inst.suppressClick=true;
                    dragging=false;
                };
                w.pc=w.pu;
                window.addEventListener('pointermove',w.pm);
                window.addEventListener('pointerup',w.pu);
                window.addEventListener('pointercancel',w.pc);
                if(w.plock){
                    w.mm=function(e){
                        if(!w.active||document.pointerLockElement!==canvas)return;
                        w.settle=0;
                        w.yaw-=e.movementX*0.0025;
                        w.pitch=clampPitch(w.pitch-e.movementY*0.0025);
                    };
                    document.addEventListener('mousemove',w.mm);
                }
            }

            function resolveWalkTarget(btn){
                var v=btn.getAttribute('data-three-walk');
                if(v){var el=document.getElementById(v);return (el&&instances.has(el))?el:null;}
                var first=null;
                instances.forEach(function(inst,el){if(!first)first=el;});
                return first;
            }

            // the toggle protocol: any [data-three-walk] element switches the
            // named scene's walk mode — delegated once, survives swaps
            document.addEventListener('click',function(e){
                if(!e.target||!e.target.closest)return;
                var btn=e.target.closest('[data-three-walk]');
                if(!btn)return;
                var el=resolveWalkTarget(btn);
                var inst=el&&instances.get(el);
                if(!inst)return;
                if(!inst.walk){console.warn('[JWeb] data-three-walk: scene has no camera().walk(...)');return;}
                setWalk(inst,!inst.walk.active);
            },true);

        """;

    private static final String PART3 = """
            // ==================== Live patches ====================

            function startTween(inst,dur,step){
                if(dur>0){
                    inst.tweens.push({t0:inst.time,dur:dur/1000,step:step});
                    kick(inst);
                }else{
                    step(1);
                    if(!inst.raf)render(inst);
                }
            }

            function patchColor(inst,mat,key,value,dur){
                if(!mat||!mat[key])return;
                var from=mat[key].clone(),to=new THREE.Color(value);
                startTween(inst,dur,function(k){mat[key].copy(from).lerp(to,k);});
            }

            function applyNodePatch(inst,obj,p){
                var dur=p.tween||0;
                if(obj.userData&&obj.userData.jwebSound){applySoundPatch(inst,obj,p);return;}
                if(p.visible!=null){obj.visible=p.visible;if(!inst.raf)render(inst);}
                if(p.pos){
                    var pf=obj.position.clone(),pt=new THREE.Vector3(p.pos[0],p.pos[1],p.pos[2]);
                    inst.floats.forEach(function(fl){if(fl.obj===obj)fl.base=p.pos[1];});
                    startTween(inst,dur,function(k){obj.position.lerpVectors(pf,pt,k);});
                }
                if(p.rot){
                    var rf=[obj.rotation.x,obj.rotation.y,obj.rotation.z];
                    var rt=[p.rot[0]*RAD,p.rot[1]*RAD,p.rot[2]*RAD];
                    startTween(inst,dur,function(k){
                        obj.rotation.set(rf[0]+(rt[0]-rf[0])*k,rf[1]+(rt[1]-rf[1])*k,rf[2]+(rt[2]-rf[2])*k);
                    });
                }
                if(p.scl){
                    var sf=obj.scale.clone(),st=new THREE.Vector3(p.scl[0],p.scl[1],p.scl[2]);
                    inst.pulses.forEach(function(pu){if(pu.obj===obj&&pu.base)pu.base.copy(st);});
                    startTween(inst,dur,function(k){obj.scale.lerpVectors(sf,st,k);});
                }
                var mat=obj.material&&!Array.isArray(obj.material)?obj.material:null;
                if(p.color){
                    if(obj.isLight)patchColor(inst,obj,'color',p.color,dur);
                    else patchColor(inst,mat,'color',p.color,dur);
                }
                if(p.emissive)patchColor(inst,mat,'emissive',p.emissive,dur);
                if(p.opacity!=null&&mat){
                    mat.transparent=true;
                    var of=mat.opacity,ot=p.opacity;
                    startTween(inst,dur,function(k){mat.opacity=of+(ot-of)*k;});
                }
                if(p.intensity!=null&&obj.isLight){
                    var lf=obj.intensity,lt=p.intensity;
                    startTween(inst,dur,function(k){obj.intensity=lf+(lt-lf)*k;});
                }
            }

            function applyCameraPatch(inst,p){
                var dur=p.tween||0,b=inst.camBase;
                if(p.pos)b.pos=[p.pos[0],p.pos[1],p.pos[2]];
                if(p.look)b.look=[p.look[0],p.look[1],p.look[2]];
                if(inst.walk&&inst.walk.active)return;   // walking owns the camera; the
                                                         // new framing applies on exit
                var cam=inst.camera;
                if(inst.controls){
                    // OrbitControls owns the camera each frame — reposition
                    // through it instead of tweening against it
                    cam.position.set(b.pos[0],b.pos[1],b.pos[2]);
                    inst.controls.target.set(b.look[0],b.look[1],b.look[2]);
                    inst.controls.update();
                    if(!inst.raf)render(inst);
                    return;
                }
                var pf=cam.position.clone(),pt=new THREE.Vector3(b.pos[0],b.pos[1],b.pos[2]);
                var lf=inst.camLook.clone(),lt=new THREE.Vector3(b.look[0],b.look[1],b.look[2]);
                startTween(inst,dur,function(k){
                    cam.position.lerpVectors(pf,pt,k);
                    inst.camLook.lerpVectors(lf,lt,k);
                    cam.lookAt(inst.camLook);
                });
            }

            // ==================== Structural patches ====================
            // remove named subtrees (disposing them, purging every per-frame
            // registration), then build additions — so a message may replace

            function purgeObject(inst,o){
                ['spins','floats','pulses','glows','follows'].forEach(function(k){
                    inst[k]=inst[k].filter(function(e){return e.obj!==o;});
                });
                inst.pending=inst.pending.filter(function(p){
                    return !p.held.some(function(h){return h.item.obj===o;});
                });
                inst.pclouds=inst.pclouds.filter(function(pc){return pc.geo!==o.geometry;});
                inst.mixers=inst.mixers.filter(function(m){return m.getRoot()!==o;});
                for(var k in inst.objects)if(inst.objects[k]===o)delete inst.objects[k];
                if(inst.hovered===o)inst.hovered=null;
                if(o.geometry)o.geometry.dispose();
                if(o.material){
                    (Array.isArray(o.material)?o.material:[o.material]).forEach(function(m){
                        if(m.map)m.map.dispose();
                        m.dispose();
                    });
                }
                if(o.isInstancedMesh||o.isReflector)o.dispose();
            }

            function removeNode(inst,name,parents){
                var obj=inst.objects[name];
                if(!obj){console.warn('[JWeb] threePatch remove: no node named',name,'in scene',inst.el.id||'');return;}
                parents[name]=obj.parent||inst.scene;
                if(obj.parent)obj.parent.remove(obj);
                var tgt=obj.userData&&obj.userData.jwebTarget;   // a spot light's aim point
                if(tgt&&tgt.parent)tgt.parent.remove(tgt);
                obj.traverse(function(o){purgeObject(inst,o);});
            }

            function enableShadows(inst){
                inst.shadows=true;
                inst.renderer.shadowMap.enabled=true;
                inst.scene.traverse(function(o){
                    if(o.isMesh&&!o.isReflector){o.castShadow=true;o.receiveShadow=true;}
                });
            }

            function addNode(inst,entry,parents){
                if(!entry||!entry.node)return;
                var parent=inst.scene;
                if(entry.replaces!=null){
                    parent=parents[entry.replaces]||inst.scene;
                }else if(entry.into){
                    parent=inst.objects[entry.into];
                    if(!parent){console.warn('[JWeb] threePatch add: no node named',entry.into,'to add into');return;}
                }
                buildNode(entry.node,inst,parent);
                if(entry.node.shadows&&!inst.shadows)enableShadows(inst);
                if(inst.interactive&&!inst.pointer)wireInteraction(inst);
            }

            function recomputeAnimated(inst){
                inst.animated=inst.spins.length>0||inst.floats.length>0||inst.pulses.length>0
                    ||inst.glows.length>0||inst.follows.length>0||inst.pending.length>0
                    ||inst.needsLoop||inst.swayAmt>0||!!(inst.controls&&inst.controls.autoRotate);
            }

            function applyStructure(inst,msg){
                var parents={};
                (msg.remove||[]).forEach(function(name){removeNode(inst,name,parents);});
                (msg.add||[]).forEach(function(e){addNode(inst,e,parents);});
                recomputeAnimated(inst);
                kick(inst);
                if(!inst.raf)render(inst);
            }

            function applyPatch(msg){
                var el=msg.scene?document.getElementById(msg.scene):null;
                var inst=el&&instances.get(el);
                if(!inst){console.warn('[JWeb] threePatch for unknown scene:',msg.scene);return;}
                if(msg.remove||msg.add)applyStructure(inst,msg);   // structure first: nodes may target additions
                (msg.nodes||[]).forEach(function(p){
                    var obj=inst.objects[p.name];
                    if(!obj){console.warn('[JWeb] threePatch: no node named',p.name,'in scene',msg.scene);return;}
                    applyNodePatch(inst,obj,p);
                });
                if(msg.camera)applyCameraPatch(inst,msg.camera);
            }

            // ==================== Init ====================

            function wantsLoop(inst){
                return inst.animated||inst.tweens.length>0||(inst.walk&&inst.walk.active);
            }

            function kick(inst){
                if(!inst.raf&&inst.visible&&wantsLoop(inst))inst.startLoop();
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
                          objects:{},spins:[],floats:[],mixers:[],pclouds:[],tweens:[],
                          pulses:[],glows:[],follows:[],pending:[],
                          hasLight:false,clickable:false,interactive:false,hovered:null,
                          needsLoop:false,walk:null,swayAmt:0,camBase:null,camLook:null,
                          composer:null,toneCfg:null,bloomCfg:null,suppressClick:false,
                          shadows:wantsShadows(graph.nodes),animated:false,disposed:false,
                          solidNodes:[],solids:[],zones:[],nears:[],sounds:[],listener:null,noise:null,
                          pose:{yaw:0,pitch:0,x:0,y:0,z:0},proxInit:false,gray:null,
                          visible:true,raf:0,ro:null,io:null,last:0,time:0};
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
                inst.camBase={pos:[cam.position.x,cam.position.y,cam.position.z],
                              look:[look[0],look[1],look[2]]};
                inst.camLook=new THREE.Vector3(look[0],look[1],look[2]);
                if(inst.listener)cam.add(inst.listener);   // sounds are heard from the camera

                if(camNode&&camNode.walk){
                    var wk=camNode.walk;
                    inst.walk={eye:wk[0]!=null?wk[0]:1.7,speed:wk[1]||3,run:wk[2]||5.5,
                               bounds:camNode.bounds||null,active:false,
                               yaw:0,pitch:0,px:0,pz:0,bob:0,keys:{},
                               ground:camNode.ground!==false,fly:camNode.fly||0,
                               clickMove:!!camNode.clickMove,autoStart:!!camNode.autoStart,
                               radius:camNode.radius!=null?camNode.radius:0.32,spawn:camNode.spawn||null,
                               plock:!!camNode.plock,touch:!!camNode.touch,gamepad:!!camNode.gamepad,
                               steps:camNode.steps||null,
                               floor:0,floorS:0,baseY:0,flyV:0,flyS:0,settle:0,glide:null,lastStep:0,
                               gX:NaN,gZ:NaN,gT:-1e9,solidsAt:-1e9,stick:null};
                    wireWalkLook(inst);
                    if(inst.walk.clickMove)wireClickMove(inst);
                    if(inst.walk.autoStart)wireAutoStart(inst);
                    if(inst.walk.steps&&inst.walk.steps.url)loadSteps(inst);
                }
                if(inst.sounds.length||(inst.walk&&inst.walk.steps))wireAudioUnlock();
                if(camNode&&camNode.sway&&!camNode.orbit&&!REDUCED)inst.swayAmt=camNode.sway;

                // tone mapping + bloom: ACES on the renderer; with bloom, the
                // scene renders HDR through the composer and OutputPass does
                // the one tone-map + sRGB conversion at the end of the chain
                if(inst.toneCfg||inst.bloomCfg){
                    renderer.toneMapping=THREE.ACESFilmicToneMapping;
                    renderer.toneMappingExposure=(inst.toneCfg&&inst.toneCfg.exposure!=null)?inst.toneCfg.exposure:1;
                }
                if(inst.bloomCfg){
                    if(!THREE.EffectComposer||!THREE.UnrealBloomPass||!THREE.OutputPass){
                        console.warn('[JWeb] bloom() needs the postprocessing addons — rebuild the three bundle');
                    }else{
                        var bc=inst.bloomCfg;
                        var composer=new THREE.EffectComposer(renderer);
                        composer.addPass(new THREE.RenderPass(inst.scene,cam));
                        composer.addPass(new THREE.UnrealBloomPass(
                            new THREE.Vector2(el.clientWidth||1,el.clientHeight||1),
                            bc.strength!=null?bc.strength:0.7,
                            bc.radius!=null?bc.radius:0.35,
                            bc.threshold!=null?bc.threshold:0.85));
                        composer.addPass(new THREE.OutputPass());
                        inst.composer=composer;
                    }
                }

                var autoRotate=camNode&&camNode.auto;
                inst.animated=inst.spins.length>0||inst.floats.length>0||!!autoRotate
                               ||inst.needsLoop||inst.swayAmt>0;
                if(camNode&&camNode.orbit&&THREE.OrbitControls){
                    var controls=new THREE.OrbitControls(cam,canvas);
                    controls.target.set(look[0],look[1],look[2]);
                    controls.enableDamping=inst.animated;
                    if(autoRotate){controls.autoRotate=true;controls.autoRotateSpeed=autoRotate;}
                    if(camNode.noZoom)controls.enableZoom=false;
                    if(camNode.noPan)controls.enablePan=false;
                    if(camNode.dist){controls.minDistance=camNode.dist[0];controls.maxDistance=camNode.dist[1];}
                    if(camNode.polar){controls.minPolarAngle=camNode.polar[0]*RAD;controls.maxPolarAngle=camNode.polar[1]*RAD;}
                    controls.update();
                    if(!inst.animated)controls.addEventListener('change',function(){render(inst);afterFrame(inst);});
                    inst.controls=controls;
                }
                if(inst.interactive)wireInteraction(inst);

                function resize(){
                    var w=el.clientWidth,h=el.clientHeight;
                    if(!w||!h)return;
                    renderer.setSize(w,h,false);
                    if(inst.composer)inst.composer.setSize(w,h);
                    cam.aspect=w/h;
                    cam.updateProjectionMatrix();
                    if(!inst.raf)render(inst);
                }
                inst.ro=new ResizeObserver(resize);
                inst.ro.observe(el);
                resize();

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
                    inst.pclouds.forEach(function(pc){
                        var a=pc.geo.attributes.position.array;
                        if(pc.fall)pc.fallOff+=pc.fall*dt;
                        for(var i=0;i<pc.count;i++){
                            var ph=pc.phase[i];
                            if(pc.drift){
                                var da=0.02*pc.drift;
                                a[i*3]=pc.orig[i*3]+Math.sin(inst.time*0.5*pc.drift+ph)*pc.spread[0]*da;
                                a[i*3+2]=pc.orig[i*3+2]+Math.cos(inst.time*0.4*pc.drift+ph*1.7)*pc.spread[2]*da;
                                if(!pc.fall)a[i*3+1]=pc.orig[i*3+1]+Math.sin(inst.time*0.3*pc.drift+ph*2.3)*pc.spread[1]*da;
                            }
                            if(pc.fall){
                                var y=pc.orig[i*3+1]-pc.fallOff;
                                var h=pc.spread[1];
                                y=y-Math.floor((y+h/2)/h)*h;   // wrap into [-h/2, h/2)
                                a[i*3+1]=y;
                            }
                        }
                        pc.geo.attributes.position.needsUpdate=true;
                    });
                    stepPresets(inst);
                    if(inst.walk&&inst.walk.active){
                        stepWalk(inst,dt);
                    }else if(inst.swayAmt){
                        cam.position.x=inst.camBase.pos[0]+Math.sin(inst.time*0.16)*0.14*inst.swayAmt;
                        cam.position.y=inst.camBase.pos[1]+Math.sin(inst.time*0.38)*0.05*inst.swayAmt;
                    }
                    if(inst.tweens.length){
                        inst.tweens=inst.tweens.filter(function(tw){
                            var k=Math.min(1,(inst.time-tw.t0)/tw.dur);
                            tw.step(ease(k));
                            return k<1;
                        });
                    }
                    if(inst.controls&&inst.controls.enabled)inst.controls.update();
                    render(inst);
                    afterFrame(inst);
                    if(!wantsLoop(inst))inst.stopLoop();
                };
                inst.startLoop=function(){
                    if(inst.raf||inst.disposed||!wantsLoop(inst))return;
                    inst.last=performance.now();
                    inst.raf=requestAnimationFrame(loop);
                };
                inst.stopLoop=function(){
                    if(inst.raf){cancelAnimationFrame(inst.raf);inst.raf=0;}
                };
                // The loop only runs while the scene is actually on
                // screen — scrolled-away scenes cost nothing
                inst.io=new IntersectionObserver(function(entries){
                    inst.visible=entries[0].isIntersecting;
                    if(inst.visible)inst.startLoop();
                    else inst.stopLoop();
                });
                inst.io.observe(el);
                if(inst.animated)inst.startLoop();
                else render(inst);
                instances.set(el,inst);
                afterFrame(inst);
                syncPose(inst,true);
                flushReady(el,inst);
            }

            function flushReady(el,inst){
                readyQ=readyQ.filter(function(r){
                    if(r.el===el||(r.id&&r.id===el.id)){
                        try{r.cb(inst);}catch(e){console.error('[JWeb] ready callback failed:',e);}
                        return false;
                    }
                    return true;
                });
            }

            function dispose(inst){
                if(inst.walk&&inst.walk.active)setWalk(inst,false);
                if(inst.walk){
                    var wd=inst.walk;
                    if(wd.pm)window.removeEventListener('pointermove',wd.pm);
                    if(wd.pu)window.removeEventListener('pointerup',wd.pu);
                    if(wd.pc)window.removeEventListener('pointercancel',wd.pc);
                    if(wd.autoKey)document.removeEventListener('keydown',wd.autoKey);
                    if(wd.mm)document.removeEventListener('mousemove',wd.mm);
                }
                inst.sounds.forEach(function(a){try{if(a.isPlaying)a.stop();}catch(e){}});
                inst.disposed=true;
                if(inst.raf)cancelAnimationFrame(inst.raf);
                if(inst.ro)inst.ro.disconnect();
                if(inst.io)inst.io.disconnect();
                if(inst.controls)inst.controls.dispose();
                if(inst.composer&&inst.composer.dispose)inst.composer.dispose();
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
                // instance buffers and reflector render targets hang off the object, not its material
                inst.scene.traverse(function(o){
                    if(o.isInstancedMesh||o.isReflector)o.dispose();
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
                        if(!inst.raf)render(inst);
                    }
                });
                if(added)scan();
            });
            mo.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['data-three']});

            window.JWebThree={
                THREE:THREE,
                get:function(id){
                    var el=document.getElementById(id);
                    return (el&&instances.get(el))||null;
                },
                ready:function(id,cb){
                    var el=typeof id==='string'?document.getElementById(id):id;
                    var inst=el&&instances.get(el);
                    if(inst){cb(inst);return;}
                    readyQ.push({id:typeof id==='string'?id:'',el:el||null,cb:cb});
                },
                applyPatch:applyPatch,
                setWalk:function(id,on){
                    var inst=this.get(id);
                    if(inst&&inst.walk)setWalk(inst,!!on);
                },
                walking:function(id){
                    var inst=this.get(id);
                    return !!(inst&&inst.walk&&inst.walk.active);
                },
                pose:function(id){
                    var inst=this.get(id);
                    return inst?poseOf(inst):null;
                },
                mute:function(id,on){
                    var inst=this.get(id);
                    if(!inst)return;
                    inst.sounds.forEach(function(a){
                        if(on){a.userData.jwebSound.vol=a.getVolume();a.setVolume(0);}
                        else if(a.userData.jwebSound.vol!=null)a.setVolume(a.userData.jwebSound.vol);
                    });
                },
                scan:scan
            };
            scan();
        })();
        """;

    private static final String SCRIPT = String.join("\n", PART1, PART2, PART3);
}
