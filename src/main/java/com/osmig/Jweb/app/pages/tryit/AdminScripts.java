package com.osmig.Jweb.app.pages.tryit;

import static com.osmig.Jweb.framework.js.JS.*;

/** JavaScript handlers for Admin page. */
public final class AdminScripts {
    private AdminScripts() {}

    public static String adminHandlers() {
        return script()
            .unsafeRaw("(function(){")
            .add(escapeFunc())
            .unsafeRaw(stateAndElements())
            .unsafeRaw(mainLogic())
            .unsafeRaw("})()")
            .build();
    }

    private static Func escapeFunc() {
        return func("esc", "t")
            .let_("d", call("document.createElement", str("div")))
            .set("d.textContent", variable("t").or(str("")))
            .ret(variable("d").dot("innerHTML"));
    }

    private static String stateAndElements() {
        return """
            let adminKey='',adminEmail='',currentTab='pending',requests=[],isLoggedIn=false;
            const container=document.getElementById('requests-container'),
                  loginOverlay=document.getElementById('login-overlay'),
                  loginForm=document.getElementById('login-form'),
                  loginError=document.getElementById('login-error'),
                  modalOverlay=document.getElementById('modal-overlay'),
                  modalBody=document.getElementById('modal-body'),
                  adminContent=document.getElementById('admin-content');
            """;
    }

    private static String mainLogic() {
        return """
            function fmtDate(ts){return ts?new Date(ts).toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}):'';}
            function statusBg(s){return s==='PENDING'?'#fef3c7':s==='APPROVED'?'#d1fae5':'#fee2e2';}
            function statusTxt(s){return s==='PENDING'?'#92400e':s==='APPROVED'?'#065f46':'#991b1b';}
            function showModal(html){modalBody.innerHTML=html;modalOverlay.style.display='flex';}
            function confirmModal(msg,onConfirm){
                showModal(`<div style="text-align:center">
                    <h3 style="margin:0 0 1rem;color:#1f2937">${msg}</h3>
                    <div style="display:flex;gap:0.75rem;justify-content:center;margin-top:1.5rem">
                        <button id="modal-cancel" style="padding:0.6rem 1.5rem;background:#e5e7eb;color:#374151;border:none;border-radius:8px;cursor:pointer;font-weight:500">Cancel</button>
                        <button id="modal-confirm" style="padding:0.6rem 1.5rem;background:#6366f1;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:500">Confirm</button>
                    </div>
                </div>`);
                document.getElementById('modal-cancel').onclick=()=>modalOverlay.style.display='none';
                document.getElementById('modal-confirm').onclick=()=>{modalOverlay.style.display='none';onConfirm();};
            }
            window.copyToken=function(){
                const input=document.getElementById('token-input');
                input.select();navigator.clipboard.writeText(input.value);
                document.getElementById('modal-status').textContent='Token copied!';
            };
            window.sendTokenEmail=async function(email,token){
                const status=document.getElementById('modal-status');
                status.textContent='Sending email...';status.style.color='#6b7280';
                try{
                    if(typeof emailjs!=='undefined'){
                        await emailjs.send('service_0cbj03m','template_s4s7d3r',{to_email:email,token:token});
                        status.textContent='Email sent successfully!';status.style.color='#065f46';
                    }else{status.textContent='Email service not available';status.style.color='#991b1b';}
                }catch(e){status.textContent='Failed to send email';status.style.color='#991b1b';}
            };
            function renderRequests(){
                const inner=container.querySelector('div');
                if(!requests.length){inner.innerHTML='<div style="text-align:center;padding:3rem;color:#6b7280;">No requests found</div>';return;}
                inner.innerHTML=requests.map(r=>requestCard(r)).join('');
            }
            function requestCard(r){
                return `<div style="background:#fff;border-radius:12px;padding:1.5rem;margin-bottom:1rem;box-shadow:0 1px 3px rgba(0,0,0,0.1)">
                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1rem">
                        <div><div style="font-weight:600;font-size:1.1rem;color:#1f2937">${esc(r.email)}</div>
                        <div style="font-size:0.875rem;color:#6b7280">${fmtDate(r.createdAt)}</div></div>
                        <span style="padding:0.25rem 0.75rem;border-radius:9999px;font-size:0.75rem;font-weight:500;background:${statusBg(r.status)};color:${statusTxt(r.status)}">${r.status}</span>
                    </div>
                    <div style="background:#f9fafb;padding:1rem;border-radius:8px;margin-bottom:1rem">
                        <div style="font-size:0.75rem;color:#6b7280;margin-bottom:0.25rem">Message:</div>
                        <div style="color:#374151">${esc(r.message)}</div>
                    </div>
                    ${r.token?`<div style="background:#f0fdf4;padding:0.75rem;border-radius:8px;margin-bottom:1rem"><div style="font-size:0.75rem;color:#065f46;margin-bottom:0.25rem">Token:</div><code style="font-size:0.875rem;color:#065f46;word-break:break-all">${r.token}</code></div>`:''}
                    ${r.status==='PENDING'?`<div style="display:flex;gap:0.5rem">
                        <button onclick="approveReq('${r.id}','${esc(r.email)}')" style="padding:0.5rem 1rem;background:#10b981;color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:500">Approve</button>
                        <button onclick="rejectReq('${r.id}')" style="padding:0.5rem 1rem;background:#ef4444;color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:500">Reject</button>
                    </div>`:''}
                </div>`;
            }
            async function loadRequests(){
                if(!isLoggedIn)return;
                try{
                    const endpoint=currentTab==='pending'?'/api/try-it/admin/requests':'/api/try-it/admin/requests/all';
                    const res=await fetch(endpoint,{headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail}});
                    if(res.status===401){isLoggedIn=false;loginOverlay.style.display='flex';adminContent.style.display='none';return;}
                    requests=await res.json();renderRequests();
                }catch{}
            }
            window.approveReq=function(id,email){
                confirmModal('Approve this request?',async()=>{
                    try{
                        const res=await fetch('/api/try-it/admin/approve/'+id,{method:'POST',headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail}});
                        const data=await res.json();
                        if(res.ok){showModal(`<div style="text-align:center">
                            <h3 style="color:#065f46;margin-bottom:1rem">Approved!</h3>
                            <p style="color:#6b7280;margin-bottom:0.5rem">Token for ${esc(email)}:</p>
                            <input id="token-input" value="${data.token}" readonly style="width:100%;padding:0.75rem;font-family:monospace;border:1px solid #e5e7eb;border-radius:8px;background:#f9fafb;margin-bottom:1rem"/>
                            <div style="display:flex;gap:0.75rem;justify-content:center">
                                <button onclick="copyToken()" style="padding:0.6rem 1.25rem;background:#6366f1;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:500">Copy Token</button>
                                <button onclick="sendTokenEmail('${esc(email)}','${data.token}')" style="padding:0.6rem 1.25rem;background:#10b981;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:500">Send by Email</button>
                            </div>
                            <p id="modal-status" style="margin-top:0.75rem;font-size:0.875rem;color:#6b7280"></p>
                        </div>`);loadRequests();}
                        else showModal('<div style="text-align:center;color:#991b1b"><h3>Error</h3><p>'+(data.message||'Failed')+'</p></div>');
                    }catch(e){showModal('<div style="text-align:center;color:#991b1b"><h3>Error</h3><p>Connection failed</p></div>');}
                });
            };
            window.rejectReq=function(id){
                confirmModal('Reject this request?',async()=>{
                    try{
                        const res=await fetch('/api/try-it/admin/reject/'+id,{method:'POST',headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail,'Content-Type':'application/json'},body:'{}'});
                        if(res.ok)loadRequests();else showModal('<div style="text-align:center;color:#991b1b"><h3>Error</h3><p>Failed to reject</p></div>');
                    }catch(e){showModal('<div style="text-align:center;color:#991b1b"><h3>Error</h3><p>Connection failed</p></div>');}
                });
            };
            loginForm?.addEventListener('submit',async function(e){
                e.preventDefault();
                adminEmail=document.getElementById('admin-email').value;
                adminKey=document.getElementById('admin-key').value;
                const btn=loginForm.querySelector('button[type="submit"]');
                btn.disabled=true;btn.textContent='Logging in...';
                loginError.style.display='none';
                try{
                    const res=await fetch('/api/try-it/admin/requests',{headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail}});
                    if(res.status===401){loginError.textContent='Invalid credentials';loginError.style.display='block';btn.disabled=false;btn.textContent='Login';return;}
                    requests=await res.json();isLoggedIn=true;history.pushState({admin:true},'',location.href);loginOverlay.style.display='none';adminContent.style.display='flex';renderRequests();
                }catch(e){loginError.textContent='Connection error';loginError.style.display='block';btn.disabled=false;btn.textContent='Login';}
            });
            document.getElementById('modal-close')?.addEventListener('click',function(){modalOverlay.style.display='none';});
            modalOverlay?.addEventListener('click',function(e){if(e.target===modalOverlay)modalOverlay.style.display='none';});
            document.querySelectorAll('.tab-btn').forEach(function(btn){btn.addEventListener('click',function(){
                if(!isLoggedIn)return;
                currentTab=btn.dataset.tab;
                document.querySelectorAll('.tab-btn').forEach(function(b){b.style.backgroundColor='transparent';b.style.color='#6b7280';b.style.borderBottomColor='transparent';});
                btn.style.backgroundColor='white';btn.style.color='#6366f1';btn.style.borderBottomColor='#6366f1';
                loadRequests();
            });});
            window.addEventListener('popstate',function(){
                if(isLoggedIn){isLoggedIn=false;adminKey='';adminEmail='';loginOverlay.style.display='flex';adminContent.style.display='none';}
            });
            """;
    }
}
