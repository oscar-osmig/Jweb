package com.osmig.Jweb.app.pages.tryit;

import static com.osmig.Jweb.framework.js.JS.*;

/** JavaScript handlers for Try It page forms. */
public final class TryItScripts {
    private TryItScripts() {}

    public static String formHandlers() {
        // Show message helper
        Func showMsg = func("showMsg", "el", "text", "type")
            .set("el.textContent", variable("text"))
            .set("el.style.display", str("block"))
            .set("el.style.backgroundColor", variable("type").eq("ok").ternary(str("#d1fae5"), str("#fee2e2")))
            .set("el.style.color", variable("type").eq("ok").ternary(str("#065f46"), str("#991b1b")));

        return script()
            .unsafeRaw("(function(){")
            .const_("requestForm", getElem("request-form"))
            .const_("requestMsg", getElem("request-message"))
            .const_("downloadForm", getElem("download-form"))
            .const_("downloadMsg", getElem("download-message"))
            .add(showMsg)
            // Initialize EmailJS when ready
            .unsafeRaw("if(typeof emailjs!=='undefined')emailjs.init('jBoWfcyb20GACTvti');")
            // Request form handler with EmailJS
            .unsafeRaw("""
                requestForm?.addEventListener('submit',async(e)=>{
                    e.preventDefault();
                    const btn=requestForm.querySelector('button[type="submit"]');
                    const fd=new FormData(requestForm);
                    const email=fd.get('email'),message=fd.get('message');
                    btn.disabled=true;btn.textContent='Sending...';
                    try{
                        const res=await fetch('/api/try-it/request',{
                            method:'POST',headers:{'Content-Type':'application/json'},
                            body:JSON.stringify({email,message})
                        });
                        const data=await res.json();
                        if(res.ok&&typeof emailjs!=='undefined'){
                            emailjs.send('service_0cbj03m','template_al44e1p',{from_email:email,message});
                        }
                        showMsg(requestMsg,data.message||(res.ok?'Success':'Error'),res.ok?'ok':'err');
                        if(res.ok)requestForm.reset();
                    }catch{showMsg(requestMsg,'Network error','err');}
                    btn.disabled=false;btn.textContent='Send Request';
                })""")
            // Download form handler
            .unsafeRaw("""
                downloadForm?.addEventListener('submit',async(e)=>{
                    e.preventDefault();
                    const btn=downloadForm.querySelector('button[type="submit"]');
                    const fd=new FormData(downloadForm);
                    const email=fd.get('email'),token=fd.get('token');
                    btn.disabled=true;btn.textContent='Validating...';
                    try{
                        const res=await fetch(`/api/try-it/validate?token=${encodeURIComponent(token)}&email=${encodeURIComponent(email)}`);
                        const data=await res.json();
                        if(!data.valid){showMsg(downloadMsg,data.error||'Invalid','err');}
                        else{
                            btn.textContent='Downloading...';
                            const a=document.createElement('a');
                            a.href=`/api/try-it/download?token=${encodeURIComponent(token)}&email=${encodeURIComponent(email)}`;
                            a.download='jweb-starter.zip';a.click();
                            showMsg(downloadMsg,'Download started!','ok');
                        }
                    }catch{showMsg(downloadMsg,'Network error','err');}
                    btn.disabled=false;btn.textContent='Download Project';
                })""")
            .unsafeRaw("})()")
            .build();
    }
}
