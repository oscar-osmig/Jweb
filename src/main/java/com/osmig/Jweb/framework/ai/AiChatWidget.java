package com.osmig.Jweb.framework.ai;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/**
 * Drop-in AI chat widget. Requires {@code jweb.ai.enabled=true} (which
 * activates the {@link AiChatEndpoint} it talks to).
 *
 * <pre>
 * body(
 *     ...,
 *     AiChatWidget.render()               // default title
 *     AiChatWidget.render("Ask JWeb")     // custom title
 * )
 * </pre>
 */
public final class AiChatWidget {

    private AiChatWidget() {}

    public static Element render() {
        return render("Assistant");
    }

    public static Element render(String title) {
        return div(attrs().id("jweb-ai-widget").style()
                .maxWidth(px(420)).width(percent(100))
                .border(px(1), solid, hex("#e2e8f0")).borderRadius(px(12))
                .display(flex).flexDirection(column)
                .backgroundColor(white)
                .overflow(hidden)
            .done(),
            div(attrs().style()
                    .padding(rem(0.75), rem(1))
                    .borderBottom(px(1), solid, hex("#e2e8f0"))
                    .fontWeight(600).fontSize(rem(0.9))
                .done(),
                text(title)),
            div(attrs().id("jweb-ai-messages").style()
                    .padding(rem(1)).height(px(320)).overflowY(auto)
                    .display(flex).flexDirection(column).gap(rem(0.5))
                    .fontSize(rem(0.875))
                .done()),
            form(attrs().id("jweb-ai-form").style()
                    .display(flex).gap(rem(0.5))
                    .padding(rem(0.75))
                    .borderTop(px(1), solid, hex("#e2e8f0"))
                .done(),
                input(attrs().id("jweb-ai-input").type("text").name("message")
                    .placeholder("Ask anything...")
                    .style().flex(1).padding(rem(0.5))
                    .border(px(1), solid, hex("#d1d5db")).borderRadius(px(6))
                    .done()),
                button(attrs().type("submit").style()
                        .padding(rem(0.5), rem(1))
                        .backgroundColor(hex("#6366f1")).color(white)
                        .border(none).borderRadius(px(6)).cursor(pointer)
                    .done(),
                    text("Send"))),
            inlineScript(widgetScript())
        );
    }

    /** Client logic: session id, optimistic bubbles, POST to /jweb/ai/chat. */
    private static String widgetScript() {
        return """
            (function(){
                if(window.__jwebAiInit)return;window.__jwebAiInit=true;
                var sid=crypto.randomUUID();
                var box=document.getElementById('jweb-ai-messages');
                var form=document.getElementById('jweb-ai-form');
                var input=document.getElementById('jweb-ai-input');
                function bubble(who,text){
                    var el=document.createElement('div');
                    el.textContent=text;
                    el.style.cssText='max-width:85%;padding:0.5rem 0.75rem;border-radius:10px;white-space:pre-wrap;'+
                        (who==='user'?'align-self:flex-end;background:#6366f1;color:#fff':'align-self:flex-start;background:#f1f5f9;color:#1e293b');
                    box.appendChild(el);box.scrollTop=box.scrollHeight;
                    return el;
                }
                form.addEventListener('submit',function(e){
                    e.preventDefault();
                    var msg=input.value.trim();if(!msg)return;
                    input.value='';bubble('user',msg);
                    var pending=bubble('ai','…');
                    fetch('/jweb/ai/chat',{method:'POST',headers:{'Content-Type':'application/json'},
                        body:JSON.stringify({sessionId:sid,message:msg})})
                    .then(function(r){return r.json()})
                    .then(function(d){pending.textContent=d.reply||d.error||'No reply'})
                    .catch(function(){pending.textContent='Connection error'});
                });
            })();
            """;
    }
}
