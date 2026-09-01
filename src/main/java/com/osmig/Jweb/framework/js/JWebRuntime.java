package com.osmig.Jweb.framework.js;

/**
 * JWeb Client Runtime - the JavaScript that powers client-side functionality.
 *
 * <p>This generates the client-side JavaScript that handles:</p>
 * <ul>
 *   <li>WebSocket connection to server</li>
 *   <li>Event handler execution</li>
 *   <li>State synchronization</li>
 *   <li>DOM updates</li>
 * </ul>
 */
public final class JWebRuntime {

    private static volatile boolean enabled = true;

    private JWebRuntime() {}

    /**
     * Enables or disables runtime injection. When disabled,
     * {@link #getScriptTag()} returns an empty string and pages are served
     * without the client runtime (server events/state sync won't work).
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /** Whether the runtime is injected into rendered pages. */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the JWeb client runtime JavaScript code.
     */
    public static String getScript() {
        String script = resolvedScript;
        if (script == null) {
            script = RUNTIME_SCRIPT
                .replace("__THREE_BUNDLE_V__", com.osmig.Jweb.framework.three.ThreeAssets.bundleVersion())
                .replace("__THREE_RUNTIME_V__", com.osmig.Jweb.framework.three.ThreeRuntime.version());
            resolvedScript = script;
        }
        return script;
    }

    // The runtime embeds the three.js asset versions (for cache-busting ?v=
    // params), resolved once — its own content hash then covers them too.
    private static volatile String resolvedScript;

    /**
     * Returns a script tag containing the JWeb runtime,
     * or an empty string when injection is disabled.
     */
    public static String getScriptTag() {
        if (!enabled) return "";
        return "<script>\n" + getScript() + "\n</script>";
    }

    private static final String RUNTIME_SCRIPT = """
        var JWeb={
            ws:null,
            state:{},
            lateQueue:[],
            ready:false,
            data:null,
            connected:false,
            reconnectAttempts:0,
            maxReconnectAttempts:5,
            reconnectDelay:1000,

            init:function(){
                console.log('[JWeb] Starting initialization...');
                var dataEl=document.getElementById('__JWEB_DATA__');
                if(dataEl){
                    try{
                        this.data=JSON.parse(dataEl.textContent);
                        if(this.data.state){
                            var self=this;
                            this.data.state.forEach(function(s){
                                self.state[s.id]=s.value;
                            });
                        }
                    }catch(e){
                        console.error('[JWeb] Failed to parse hydration data:',e);
                    }
                }
                var self=this;
                this.lateQueue.forEach(function(s){self.state[s.id]=s.value;});
                this.lateQueue=[];
                this.ready=true;
                this.connect();
                this.initTransitions();
                this.initBindings();
                this.initSwaps();
                this.initServerEvents();
                this.initThree();
            },

            initServerEvents:function(){
                var self=this;
                // Server event handlers render as data-jweb-on<type> and
                // Actions-DSL handlers as data-jweb-act<type> attributes
                // (inline on<type>= handlers can never run under a nonce CSP).
                // Capture-phase document listeners delegate every type — capture
                // reaches the target even for non-bubbling events (focus, blur,
                // load, error, scroll) — and delegation survives swaps, morphs
                // and streamed chunks for free.
                ['click','dblclick','change','input','submit','focus','blur',
                 'keydown','keyup','keypress','mousedown','mouseup','mousemove',
                 'mouseover','mouseout','contextmenu','wheel','drag','dragstart',
                 'dragend','dragenter','dragleave','dragover','drop','touchstart',
                 'touchmove','touchend','touchcancel','scroll','toggle','cancel',
                 'close','animationstart','animationend','animationiteration',
                 'transitionend','load','error','copy','cut','paste'
                ].forEach(function(type){
                    document.addEventListener(type,function(e){
                        var t=e.target;
                        if(!t||!t.closest)return;
                        var el=t.closest('[data-jweb-on'+type+']');
                        if(el)self.call(el.getAttribute('data-jweb-on'+type),e);
                        var a=t.closest('[data-jweb-act'+type+']');
                        if(a)self.runAction(a.getAttribute('data-jweb-act'+type),e,a);
                    },true);
                });
                // mouseenter/mouseleave don't propagate at all — emulate them
                // from mouseover/mouseout with a relatedTarget boundary check
                document.addEventListener('mouseover',function(e){
                    var t=e.target;
                    if(!t||!t.closest)return;
                    var el=t.closest('[data-jweb-onmouseenter]');
                    if(el&&!(e.relatedTarget&&el.contains(e.relatedTarget)))
                        self.call(el.getAttribute('data-jweb-onmouseenter'),e);
                    var a=t.closest('[data-jweb-actmouseenter]');
                    if(a&&!(e.relatedTarget&&a.contains(e.relatedTarget)))
                        self.runAction(a.getAttribute('data-jweb-actmouseenter'),e,a);
                },true);
                document.addEventListener('mouseout',function(e){
                    var t=e.target;
                    if(!t||!t.closest)return;
                    var el=t.closest('[data-jweb-onmouseleave]');
                    if(el&&!(e.relatedTarget&&el.contains(e.relatedTarget)))
                        self.call(el.getAttribute('data-jweb-onmouseleave'),e);
                    var a=t.closest('[data-jweb-actmouseleave]');
                    if(a&&!(e.relatedTarget&&a.contains(e.relatedTarget)))
                        self.runAction(a.getAttribute('data-jweb-actmouseleave'),e,a);
                },true);
            },

            runAction:function(id,e,el){
                // Actions-DSL handler: defined by a nonce'd script into the
                // global map (page shell, streamed chunk, swapped fragment or
                // domUpdate delivery). Mirrors inline-handler semantics:
                // this = the attributed element, returning false cancels.
                var fn=(window.__JWEB_ACTIONS__||{})[id];
                if(!fn){console.warn('[JWeb] action not defined:',id);return;}
                if(fn.call(el,e)===false)e.preventDefault();
            },

            pageNonce:function(){
                // The document's CSP nonce, recovered from any nonce'd script
                // (the content attribute is hidden post-parse; the IDL
                // property is not). '' when the page has no CSP nonce.
                if(this._nonce!==undefined)return this._nonce;
                var s=document.scripts;
                for(var i=0;i<s.length;i++){
                    if(s[i].nonce){this._nonce=s[i].nonce;return this._nonce;}
                }
                return this._nonce='';
            },

            execScript:function(js){
                // Scripts arriving outside the document parse (fragments,
                // domUpdate payloads) only execute as fresh script elements;
                // stamping the page nonce keeps that legal under CSP.
                var s=document.createElement('script');
                var n=this.pageNonce();
                if(n)s.nonce=n;
                s.textContent=js;
                document.head.appendChild(s);
                s.remove();
            },

            runFragmentScripts:function(tpl){
                // Definition scripts riding a fetched fragment (marked
                // data-jweb-act by the server). Executed once here, then
                // removed so the swapped-in HTML stays inert.
                var self=this;
                tpl.content.querySelectorAll('script[data-jweb-act]').forEach(function(s){
                    self.execScript(s.textContent);
                    s.remove();
                });
            },

            initThree:function(){
                if(window.JWebThree||window.__jwebThreeLoading)return;
                var load=function(){
                    if(window.__jwebThreeLoading)return;
                    window.__jwebThreeLoading=true;
                    ['/jweb/three-bundle.js?v=__THREE_BUNDLE_V__','/jweb/three-runtime.js?v=__THREE_RUNTIME_V__'].forEach(function(src){
                        var s=document.createElement('script');
                        s.src=src;
                        s.async=false;
                        document.head.appendChild(s);
                    });
                };
                if(document.querySelector('[data-three]')){load();return;}
                var mo=new MutationObserver(function(){
                    if(document.querySelector('[data-three]')){mo.disconnect();load();}
                });
                mo.observe(document.body,{childList:true,subtree:true});
            },

            lateStates:function(states){
                // States born inside streamed blocks arrive with their chunk,
                // after the shell's hydration data flushed. Before init they
                // stage (init merges them with __JWEB_DATA__); after init they
                // apply like any server state update.
                if(!states)return;
                if(this.ready){this.handleStateUpdate(states);}
                else{this.lateQueue=this.lateQueue.concat(states);}
            },

            initSwaps:function(){
                var self=this;
                document.addEventListener('click',function(e){
                    var el=e.target.closest('[data-swap-get]');
                    if(!el)return;
                    e.preventDefault();
                    self.swap(el.getAttribute('data-swap-get'),el);
                });
                document.addEventListener('submit',function(e){
                    var form=e.target.closest('form[data-swap-post]');
                    if(!form)return;
                    e.preventDefault();
                    var url=form.getAttribute('data-swap-post');
                    self.swap(url,form,{method:'POST',body:new FormData(form)});
                });
                window.addEventListener('popstate',function(e){
                    if(e.state&&e.state.jwebSwap){
                        self.swap(e.state.jwebSwap.url,null,{target:e.state.jwebSwap.target,noPush:true});
                    }
                });
            },

            swap:function(url,el,opts){
                opts=opts||{};
                var target=opts.target||(el&&el.getAttribute('data-swap-target'));
                var targetEl=target?document.querySelector(target):null;
                if(!targetEl){console.warn('[JWeb] swap target not found:',target);return;}
                var mode=(el&&el.getAttribute('data-swap-mode'))||'inner';
                var push=!opts.noPush&&el&&el.getAttribute('data-swap-push');
                var self=this;
                fetch(url,{method:opts.method||'GET',body:opts.body,credentials:'same-origin'})
                    .then(function(r){return r.text()})
                    .then(function(html){
                        // Run + strip the fragment's action-definition
                        // scripts first: innerHTML never executes scripts,
                        // and the fragment's data-jweb-act handlers are dead
                        // without their definitions
                        if(html.indexOf('data-jweb-act')>-1){
                            var tpl=document.createElement('template');
                            tpl.innerHTML=html;
                            self.runFragmentScripts(tpl);
                            html=tpl.innerHTML;
                        }
                        var apply=function(){
                            if(mode==='outer'){targetEl.outerHTML=html;}
                            else if(mode==='morph'){self.morph(targetEl,html);}
                            else{targetEl.innerHTML=html;}
                            document.dispatchEvent(new CustomEvent('jweb:swap',{detail:{url:url,target:target}}));
                        };
                        if(document.startViewTransition&&document.visibilityState==='visible'){
                            // A skipped transition (hidden tab, debugger
                            // capture, concurrent transition) rejects
                            // ready/finished — swallow those so nothing
                            // surfaces as unhandled; the DOM update (apply)
                            // runs either way. The visibility gate avoids
                            // the guaranteed-skip case up front. Note:
                            // browsers may still self-report an abnormal
                            // abort to the console per spec — cosmetic,
                            // the swap itself always completes.
                            var vt=document.startViewTransition(apply);
                            if(vt){
                                if(vt.ready)vt.ready.catch(function(){});
                                if(vt.finished)vt.finished.catch(function(){});
                                if(vt.updateCallbackDone)vt.updateCallbackDone.catch(function(){});
                            }
                        }
                        else{apply();}
                        if(push){history.pushState({jwebSwap:{url:url,target:target}},'',push);}
                    })
                    .catch(function(err){console.error('[JWeb] swap failed:',err);});
            },

            initBindings:function(){
                var self=this;
                document.querySelectorAll('[data-state-input]').forEach(function(el){
                    var stateId=el.getAttribute('data-state-bind');
                    if(!stateId)return;
                    el.addEventListener('input',function(){
                        self.setState(stateId,el.type==='checkbox'?el.checked:el.value);
                    });
                });
            },

            initTransitions:function(){
                document.querySelectorAll('[data-transition]').forEach(function(el){
                    var enterClass=el.getAttribute('data-enter-class');
                    if(enterClass){
                        var duration=parseInt(el.getAttribute('data-enter-duration')||'300',10);
                        setTimeout(function(){
                            enterClass.split(' ').forEach(function(c){el.classList.remove(c);});
                        },duration);
                    }
                });
            },

            leave:function(elOrId,callback){
                var el=typeof elOrId==='string'?document.getElementById(elOrId):elOrId;
                if(!el){if(callback)callback();return;}
                var leaveClass=el.getAttribute('data-leave-class');
                var leaveActive=el.getAttribute('data-leave-active-class');
                var duration=parseInt(el.getAttribute('data-leave-duration')||'300',10);
                if(!leaveClass&&!leaveActive){
                    el.remove();
                    if(callback)callback();
                    return;
                }
                if(leaveClass)leaveClass.split(' ').forEach(function(c){el.classList.add(c);});
                if(leaveActive)leaveActive.split(' ').forEach(function(c){el.classList.add(c);});
                setTimeout(function(){
                    el.remove();
                    if(callback)callback();
                },duration);
            },

            connect:function(){
                var protocol=window.location.protocol==='https:'?'wss:':'ws:';
                var wsUrl=protocol+'//'+window.location.host+'/jweb';
                try{
                    this.ws=new WebSocket(wsUrl);
                    var self=this;
                    this.ws.onopen=function(){
                        console.log('[JWeb] WebSocket connected');
                        self.connected=true;
                        self.reconnectAttempts=0;
                        if(self.data&&self.data.contextId){
                            self.ws.send(JSON.stringify({type:'init',contextId:self.data.contextId}));
                        }
                    };
                    this.ws.onmessage=function(event){
                        self.handleMessage(JSON.parse(event.data));
                    };
                    this.ws.onclose=function(){
                        console.log('[JWeb] WebSocket disconnected');
                        self.connected=false;
                        self.scheduleReconnect();
                    };
                    this.ws.onerror=function(error){
                        console.error('[JWeb] WebSocket error:',error);
                    };
                }catch(e){
                    console.error('[JWeb] Failed to connect:',e);
                    this.scheduleReconnect();
                }
            },

            scheduleReconnect:function(){
                if(this.reconnectAttempts<this.maxReconnectAttempts){
                    this.reconnectAttempts++;
                    var delay=this.reconnectDelay*this.reconnectAttempts;
                    console.log('[JWeb] Reconnecting in '+delay+'ms');
                    var self=this;
                    setTimeout(function(){self.connect();},delay);
                }
            },

            handleMessage:function(msg){
                switch(msg.type){
                    case 'connected':
                        console.log('[JWeb] Session:',msg.sessionId);
                        break;
                    case 'stateUpdate':
                        this.handleStateUpdate(msg.states);
                        break;
                    case 'domUpdate':
                        // New Actions-DSL definitions first, so patched-in
                        // data-jweb-act attributes resolve when clicked
                        if(msg.actionsJs)this.execScript(msg.actionsJs);
                        this.handleDomUpdate(msg);
                        break;
                    case 'initState':
                        this.handleStateUpdate(msg.states);
                        break;
                    case 'threePatch':
                        // Live scene mutation from Three.patch(...) — the
                        // three runtime owns application (loaded lazily, but
                        // always before a scene that could be patched exists)
                        if(window.JWebThree&&JWebThree.applyPatch)JWebThree.applyPatch(msg);
                        break;
                    case 'eventHandled':
                        document.dispatchEvent(new CustomEvent('jweb:eventHandled',{detail:{handler:msg.handler}}));
                        break;
                    case 'pong':
                        break;
                    case 'error':
                        console.error('[JWeb] Server error:',msg.message);
                        break;
                }
            },

            handleStateUpdate:function(states){
                if(!states)return;
                var self=this;
                states.forEach(function(s){
                    var oldValue=self.state[s.id];
                    self.state[s.id]=s.value;
                    self.updateBoundElements(s.id,s.value,oldValue);
                });
            },

            updateBoundElements:function(stateId,newValue,oldValue){
                document.querySelectorAll('[data-state="'+stateId+'"]').forEach(function(el){
                    if(el.tagName==='INPUT'||el.tagName==='TEXTAREA'){
                        el.value=newValue;
                    }else if(el.tagName==='SELECT'){
                        el.value=newValue;
                    }else{
                        el.textContent=newValue;
                    }
                });
                document.querySelectorAll('[data-state-bind="'+stateId+'"]').forEach(function(el){
                    var textMap=el.getAttribute('data-state-text');
                    if(textMap){
                        var parts=textMap.split(':');
                        el.textContent=newValue?parts[0]:parts[1];
                    }else if(el.tagName==='INPUT'||el.tagName==='TEXTAREA'||el.tagName==='SELECT'){
                        el.value=newValue;
                    }else{
                        el.textContent=newValue;
                    }
                });
                document.querySelectorAll('[data-state-toggle="'+stateId+'"]').forEach(function(el){
                    if(newValue){
                        el.classList.add('toggle-on');
                    }else{
                        el.classList.remove('toggle-on');
                    }
                });
                document.dispatchEvent(new CustomEvent('jweb:stateChange',{detail:{stateId:stateId,newValue:newValue,oldValue:oldValue}}));
            },

            handleDomUpdate:function(msg){
                if(msg.updates&&Array.isArray(msg.updates)){
                    msg.updates.forEach(function(update){
                        var target=document.getElementById(update.id);
                        if(target){
                            target.outerHTML=update.html;
                        }
                    });
                    return;
                }
                if(msg.html&&msg.targetId){
                    var target=document.getElementById(msg.targetId);
                    if(target){
                        target.outerHTML=msg.html;
                    }
                }else if(msg.html){
                    document.body.innerHTML=msg.html;
                }
            },

            call:function(handlerId,domEvent){
                if(!this.connected){
                    console.warn('[JWeb] Not connected');
                    return;
                }
                var eventData={
                    type:'event',
                    handler:handlerId,
                    contextId:this.data?this.data.contextId:null,
                    eventType:domEvent?domEvent.type:'unknown',
                    targetId:domEvent&&domEvent.target?domEvent.target.id:'',
                    value:domEvent&&domEvent.target?domEvent.target.value:'',
                    checked:domEvent&&domEvent.target?!!domEvent.target.checked:false,
                    key:domEvent?domEvent.key:null,
                    keyCode:domEvent?domEvent.keyCode:-1,
                    ctrlKey:domEvent?domEvent.ctrlKey:false,
                    shiftKey:domEvent?domEvent.shiftKey:false,
                    altKey:domEvent?domEvent.altKey:false,
                    metaKey:domEvent?domEvent.metaKey:false,
                    clientX:domEvent?domEvent.clientX:-1,
                    clientY:domEvent?domEvent.clientY:-1,
                    dataset:domEvent&&domEvent.target?Object.assign({},domEvent.target.dataset):{}
                };
                if(domEvent&&domEvent.type==='submit'&&domEvent.target.tagName==='FORM'){
                    var formData=new FormData(domEvent.target);
                    eventData.formData=Object.fromEntries(formData);
                    domEvent.preventDefault();
                }
                this.ws.send(JSON.stringify(eventData));
            },

            morph:function(target,newHtml){
                var tpl=document.createElement('template');
                tpl.innerHTML=newHtml;
                this.morphChildren(target,tpl.content);
            },

            morphNode:function(from,to){
                if(from.nodeType===3&&to.nodeType===3){
                    if(from.nodeValue!==to.nodeValue)from.nodeValue=to.nodeValue;
                    return from;
                }
                if(from.nodeType!==1||to.nodeType!==1||from.tagName!==to.tagName){
                    var replacement=to.cloneNode(true);
                    from.replaceWith(replacement);
                    return replacement;
                }
                // sync attributes
                for(var i=from.attributes.length-1;i>=0;i--){
                    var name=from.attributes[i].name;
                    if(!to.hasAttribute(name))from.removeAttribute(name);
                }
                for(var i=0;i<to.attributes.length;i++){
                    var a=to.attributes[i];
                    if(from.getAttribute(a.name)!==a.value)from.setAttribute(a.name,a.value);
                }
                // preserve live input state on the focused field
                var isField=from.tagName==='INPUT'||from.tagName==='TEXTAREA'||from.tagName==='SELECT';
                if(isField&&from===document.activeElement)return from;
                if(isField&&'value' in to&&from.value!==to.getAttribute('value')&&to.hasAttribute('value')){
                    from.value=to.getAttribute('value');
                }
                this.morphChildren(from,to);
                return from;
            },

            morphChildren:function(from,to){
                var fromKids=Array.from(from.childNodes);
                var toKids=Array.from(to.childNodes);
                // index new element children by id for stable matching
                var byId={};
                toKids.forEach(function(n){if(n.nodeType===1&&n.id)byId[n.id]=n;});
                var used=new Set();
                for(var i=0;i<toKids.length;i++){
                    var want=toKids[i];
                    var have=from.childNodes[i];
                    // id match beats positional match
                    if(want.nodeType===1&&want.id){
                        var existing=null;
                        for(var j=0;j<fromKids.length;j++){
                            if(fromKids[j].nodeType===1&&fromKids[j].id===want.id){existing=fromKids[j];break;}
                        }
                        if(existing&&existing!==have){
                            from.insertBefore(existing,have||null);
                            have=existing;
                        }
                    }
                    if(!have){from.appendChild(want.cloneNode(true));}
                    else{this.morphNode(have,want);}
                    used.add(from.childNodes[i]);
                }
                while(from.childNodes.length>toKids.length){
                    from.removeChild(from.lastChild);
                }
            },

            getState:function(stateId){
                return this.state[stateId];
            },

            setState:function(stateId,value){
                if(!this.connected){
                    return;
                }
                this.ws.send(JSON.stringify({type:'setState',stateId:stateId,value:value,contextId:this.data?this.data.contextId:null}));
            },

            ping:function(){
                if(this.connected){
                    this.ws.send(JSON.stringify({type:'ping'}));
                }
            }
        };

        if(document.readyState==='loading'){
            document.addEventListener('DOMContentLoaded',function(){JWeb.init();});
        }else{
            JWeb.init();
        }
        setInterval(function(){JWeb.ping();},30000);
        """;
}
