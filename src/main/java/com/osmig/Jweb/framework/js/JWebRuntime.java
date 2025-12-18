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
            ws:null,
            state:{},
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
                this.connect();
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
                        this.handleDomUpdate(msg);
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
                    clientY:domEvent?domEvent.clientY:-1
                };
                if(domEvent&&domEvent.type==='submit'&&domEvent.target.tagName==='FORM'){
                    var formData=new FormData(domEvent.target);
                    eventData.formData=Object.fromEntries(formData);
                    domEvent.preventDefault();
                }
                this.ws.send(JSON.stringify(eventData));
            },

            getState:function(stateId){
                return this.state[stateId];
            },

            setState:function(stateId,value){
                if(!this.connected){
                    return;
                }
                this.ws.send(JSON.stringify({type:'setState',stateId:stateId,value:value}));
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
