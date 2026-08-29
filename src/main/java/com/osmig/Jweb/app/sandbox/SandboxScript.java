package com.osmig.Jweb.app.sandbox;

import static com.osmig.Jweb.framework.js.Runtime.*;
import static com.osmig.Jweb.framework.js.Events.*;

/**
 * Client side of the live editor. The textarea is authoritative: renders go
 * out as debounced POSTs and only the dynbar/view/status fragments are
 * swapped back in — the editor itself is never replaced. Knob edits patch
 * the editor text at server-reported offsets, then re-render. All handlers
 * are delegated to .sandbox-layout where possible.
 */
final class SandboxScript {
    private SandboxScript() {}

    static String build() {
        return iife()
            .unsafeRaw(guard("__sandboxInit")
                .unsafeRaw(state())
                .unsafeRaw(linesFunc())
                .unsafeRaw(insertFunc())
                .unsafeRaw(renderFunc())
                .unsafeRaw(sourceFunc())
                .unsafeRaw(editorWiring())
                .unsafeRaw(treeHandlers())
                .unsafeRaw(treeToggle())
                .unsafeRaw(knobHandler())
                .unsafeRaw(resetHandler())
                .unsafeRaw(dotHandlers())
                .unsafeRaw(gutterDrag())
                .build())
            .build();
    }

    private static String state() {
        return "var E=document.getElementById('sandbox-editor');" +
            "var cur=(document.querySelector('.sandbox-file.active')||{dataset:{}}).dataset.file||'home';" +
            "var edits={},sources={},sbT=null,sbKnobBase=E.value;" +
            "sources[cur]=E.value";
    }

    /**
     * Line-number gutter. Each number's height is measured from a hidden
     * mirror styled exactly like the editor, so numbers track the textarea's
     * soft-wrapping — line N in the gutter is line N in error messages.
     */
    private static String linesFunc() {
        return "var LN=document.getElementById('sandbox-lines');" +
            "var MI=document.getElementById('sandbox-mirror');" +
            "var sbErr=0;" +
            "function sbMark(){Array.prototype.forEach.call(LN.children,function(n,i){" +
            "n.className=(i+1===sbErr)?'errline':''})}" +
            "function sbLines(){" +
            "var cs=getComputedStyle(E);" +
            "MI.style.width=(E.clientWidth-parseFloat(cs.paddingLeft)-parseFloat(cs.paddingRight))+'px';" +
            "var ls=E.value.split('\\n');" +
            "MI.textContent='';" +
            "for(var i=0;i<ls.length;i++){var d=document.createElement('div');" +
            "d.textContent=ls[i]===''?'\\u00a0':ls[i];MI.appendChild(d)}" +
            "var frag=document.createDocumentFragment();" +
            "for(var j=0;j<ls.length;j++){var n=document.createElement('div');" +
            "n.textContent=j+1;n.style.height=MI.children[j].offsetHeight+'px';" +
            "frag.appendChild(n)}" +
            "LN.textContent='';LN.appendChild(frag);sbMark();" +
            "LN.scrollTop=E.scrollTop}" +
            "E.addEventListener('scroll',function(){LN.scrollTop=E.scrollTop});" +
            "var sbRT=null;window.addEventListener('resize',function(){" +
            "if(sbRT)clearTimeout(sbRT);sbRT=setTimeout(sbLines,150)});" +
            "if(document.readyState==='loading'){" +
            "document.addEventListener('DOMContentLoaded',sbLines)}else{sbLines()}";
    }

    /** Insert text at the caret, preserving undo history where possible. */
    private static String insertFunc() {
        return "function sbIns(txt){" +
            "var okc=false;try{okc=document.execCommand('insertText',false,txt)}catch(err){}" +
            "if(!okc){var s=E.selectionStart,e2=E.selectionEnd;" +
            "E.value=E.value.slice(0,s)+txt+E.value.slice(e2);" +
            "E.selectionStart=E.selectionEnd=s+txt.length;" +
            "E.dispatchEvent(new Event('input',{bubbles:true}))}}";
    }

    private static String renderFunc() {
        return "function sbRender(){" +
            "var sent=E.value;" +
            "var body=new URLSearchParams();body.set('file',cur);body.set('code',sent);" +
            "fetch('/sandbox/render',{method:'POST',credentials:'same-origin',body:body})" +
            ".then(function(r){return r.text()}).then(function(html){" +
            "var t=document.createElement('template');t.innerHTML=html;" +
            "var st=t.content.querySelector('#rx-status');" +
            "if(st){var out=document.getElementById('sandbox-status');" +
            "out.textContent=st.textContent;" +
            "out.className='sandbox-status '+(st.dataset.ok==='1'?'ok':'err');" +
            "sbErr=0;" +
            "if(st.dataset.ok!=='1'){var lm=st.textContent.match(/line (\\d+)/);" +
            "if(lm)sbErr=+lm[1]}" +
            "sbMark()}" +
            "var db=t.content.querySelector('#rx-dynbar');" +
            "var vw=t.content.querySelector('#rx-view');" +
            "if(db){var ae=document.activeElement,fid=null,pos=0;" +
            "if(ae&&ae.classList&&ae.classList.contains('sandbox-knob')){" +
            "fid=ae.dataset.id;try{pos=ae.selectionStart||0}catch(err){}}" +
            "document.getElementById('sandbox-dynbar').innerHTML=db.innerHTML;" +
            "if(fid){var el=Array.prototype.find.call(document.querySelectorAll('.sandbox-knob')," +
            "function(k){return k.dataset.id===fid});" +
            "if(el){el.focus();if(el.type==='text'){try{el.setSelectionRange(pos,pos)}catch(err){}}}}}" +
            "if(vw){document.getElementById('sandbox-view').innerHTML=vw.innerHTML;sbKnobBase=sent}" +
            "}).catch(function(){})}" +
            "function sbQueue(ms){if(sbT)clearTimeout(sbT);sbT=setTimeout(sbRender,ms)}";
    }

    private static String sourceFunc() {
        return "function sbSrc(id,cb){" +
            "if(edits[id]!==undefined)return cb(edits[id]);" +
            "if(sources[id]!==undefined)return cb(sources[id]);" +
            "fetch('/sandbox/source?file='+encodeURIComponent(id),{credentials:'same-origin'})" +
            ".then(function(r){return r.text()})" +
            ".then(function(txt){sources[id]=txt;cb(txt)}).catch(function(){})}";
    }

    private static String editorWiring() {
        return "E.addEventListener('input',function(){edits[cur]=E.value;sbLines();sbQueue(300)});" +
            "E.addEventListener('keydown',function(e){" +
            "if(e.key==='Tab'){e.preventDefault();sbIns('    ')}" +
            "else if(e.key==='Enter'){e.preventDefault();" +
            "var s=E.value.slice(0,E.selectionStart);" +
            "var line=s.slice(s.lastIndexOf('\\n')+1);" +
            "var ind=(line.match(/^ */)||[''])[0];" +
            "sbIns('\\n'+ind)}});";
    }

    private static String treeHandlers() {
        return delegate(".sandbox-layout", "click", ".sandbox-file")
            .handler("var id=t.dataset.file;if(!id||id===cur)return;" +
                "cur=id;" +
                "document.querySelectorAll('.sandbox-file').forEach(function(el){" +
                "el.classList.toggle('active',el.dataset.file===id)});" +
                "document.getElementById('sandbox-path').textContent='☕ '+(t.dataset.path||'');" +
                "sbSrc(id,function(src){E.value=src;sbKnobBase=src;sbLines();sbRender()})").js() + ";" +
            delegate(".sandbox-layout", "click", ".sandbox-folder")
                .handler("t.classList.toggle('closed');" +
                    "var k=document.querySelector('.sandbox-kids[data-kids=\"'+t.dataset.folder+'\"]');" +
                    "if(k)k.classList.toggle('collapsed')").js();
    }

    /** Knob edits patch the source at the offsets the server reported. */
    private static String knobHandler() {
        return delegate(".sandbox-layout", "input", ".sandbox-knob")
            .handler("if(E.value!==sbKnobBase){sbQueue(150);return}" +
                "var start=+t.dataset.start,len=+t.dataset.len,val=t.value;" +
                "if(t.dataset.kind==='number'&&val==='')return;" +
                "if(t.dataset.kind==='color'&&!/^#[0-9a-fA-F]{6}$/.test(val))return;" +
                "E.value=E.value.slice(0,start)+val+E.value.slice(start+len);" +
                "var delta=val.length-len;t.dataset.len=String(val.length);" +
                "document.querySelectorAll('.sandbox-knob').forEach(function(k){" +
                "if(k!==t&&+k.dataset.start>start)k.dataset.start=String(+k.dataset.start+delta)});" +
                "edits[cur]=E.value;sbKnobBase=E.value;sbLines();sbQueue(250)")
            .js();
    }

    private static String resetHandler() {
        return delegate(".sandbox-layout", "click", "#sandbox-reset")
            .handler("delete edits[cur];" +
                "sbSrc(cur,function(src){E.value=src;sbKnobBase=src;sbLines();sbRender()})")
            .js();
    }

    /** Shared by the code-head «/» toggle and the sidebar's own « button. */
    private static String treeToggle() {
        return "function sbTree(hid){" +
            "document.querySelector('.sandbox-tree').classList.toggle('hidden',hid);" +
            "var b=document.getElementById('sandbox-tree-toggle');" +
            "b.textContent=hid?'\\u00bb':'\\u00ab';" +
            "b.title=hid?'show files':'hide files';" +
            "sbLines()}" +
            delegate(".sandbox-layout", "click", "#sandbox-tree-toggle")
                .handler("sbTree(!document.querySelector('.sandbox-tree')" +
                    ".classList.contains('hidden'))").js() + ";" +
            delegate(".sandbox-layout", "click", "#sandbox-tree-collapse")
                .handler("sbTree(true)").js();
    }

    private static String dotHandlers() {
        return "var PV=document.getElementById('sandbox-preview');" +
            delegate(".sandbox-layout", "click", ".sandbox-dot-g")
                .handler("PV.classList.toggle('sandbox-max')").js() + ";" +
            delegate(".sandbox-layout", "click", ".sandbox-dot-y")
                .handler("PV.classList.remove('sandbox-max')").js() + ";" +
            delegate(".sandbox-layout", "click", ".sandbox-dot-r")
                .handler("PV.classList.add('sandbox-shake');" +
                    "setTimeout(function(){PV.classList.remove('sandbox-shake')},450)").js() + ";" +
            "document.addEventListener('keydown',function(e){" +
            "if(e.key==='Escape')PV.classList.remove('sandbox-max')});";
    }

    private static String gutterDrag() {
        return "var G=document.getElementById('sandbox-gutter');" +
            "var SP=document.getElementById('sandbox-split');" +
            "var CD=document.getElementById('sandbox-code');" +
            "G.addEventListener('mousedown',function(e){e.preventDefault();" +
            "SP.classList.add('dragging');G.classList.add('dragging');" +
            "function mv(ev){var r=SP.getBoundingClientRect();" +
            "var pct=(ev.clientX-r.left)/r.width*100;" +
            "pct=Math.max(20,Math.min(80,pct));" +
            "CD.style.flex='0 0 '+pct+'%'}" +
            "function up(){SP.classList.remove('dragging');G.classList.remove('dragging');" +
            "document.removeEventListener('mousemove',mv);" +
            "document.removeEventListener('mouseup',up);sbLines()}" +
            "document.addEventListener('mousemove',mv);" +
            "document.addEventListener('mouseup',up)});";
    }
}
