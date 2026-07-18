package com.osmig.Jweb.framework.js;

/**
 * JWeb Client Runtime - the JavaScript that powers client-side functionality.
 *
 * <p>This generates the client-side JavaScript that handles:</p>
 * <ul>
 *   <li>Reading server-rendered hydration data ({@code __JWEB_DATA__})</li>
 *   <li>Event handler execution via HTTP POST to {@code /jweb/event}</li>
 *   <li>Applying state updates returned by the server to bound DOM elements</li>
 * </ul>
 *
 * <p>Events use a simple request/response round-trip over {@code fetch()}
 * rather than a WebSocket: a handler click POSTs the handler id plus the
 * event data and the server responds with the states that changed, which the
 * runtime then reflects into the DOM. This keeps the interactive path free of
 * WebSocket connection lifecycle and cross-origin handshake concerns.</p>
 */
public final class JWebRuntime {

    private JWebRuntime() {}

    /**
     * Returns the JWeb client runtime JavaScript code.
     */
    public static String getScript() {
        return RUNTIME_SCRIPT;
    }

    /**
     * Returns a script tag containing the JWeb runtime.
     */
    public static String getScriptTag() {
        return "<script>\n" + RUNTIME_SCRIPT + "\n</script>";
    }

    private static final String RUNTIME_SCRIPT = """
        var JWeb={
            state:{},
            data:null,
            endpoint:'/jweb/event',

            init:function(){
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
                var contextId=this.data?this.data.contextId:null;
                if(!contextId){
                    console.warn('[JWeb] No hydration context; cannot dispatch event');
                    return;
                }
                var eventData={
                    handler:handlerId,
                    contextId:contextId,
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
                    clientY:domEvent?domEvent.clientY:-1
                };
                if(domEvent&&domEvent.type==='submit'&&domEvent.target.tagName==='FORM'){
                    var formData=new FormData(domEvent.target);
                    eventData.formData=Object.fromEntries(formData);
                    domEvent.preventDefault();
                }
                var self=this;
                fetch(this.endpoint,{
                    method:'POST',
                    headers:{'Content-Type':'application/json'},
                    body:JSON.stringify(eventData)
                }).then(function(res){
                    return res.json();
                }).then(function(msg){
                    if(msg&&msg.success){
                        self.handleStateUpdate(msg.states);
                        if(msg.html!==undefined||msg.updates){self.handleDomUpdate(msg);}
                    }else if(msg&&msg.error){
                        console.warn('[JWeb] '+msg.error);
                    }
                }).catch(function(err){
                    console.error('[JWeb] Event request failed:',err);
                });
            },

            getState:function(stateId){
                return this.state[stateId];
            }
        };

        if(document.readyState==='loading'){
            document.addEventListener('DOMContentLoaded',function(){JWeb.init();});
        }else{
            JWeb.init();
        }
        """;
}
