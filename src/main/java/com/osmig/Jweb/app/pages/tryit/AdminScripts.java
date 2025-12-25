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
            window.approveReq=async function(id,email){
                if(!confirm('Approve this request?'))return;
                try{
                    const res=await fetch('/api/try-it/admin/approve/'+id,{method:'POST',headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail}});
                    const data=await res.json();
                    if(res.ok){showModal('<div style="text-align:center"><h3>Approved!</h3><p>Token for '+esc(email)+':</p><input value="'+data.token+'" readonly style="width:100%;padding:0.5rem;font-family:monospace"/></div>');loadRequests();}
                    else alert(data.error||'Failed');
                }catch(e){alert('Error');}
            };
            window.rejectReq=async function(id){
                if(!confirm('Reject this request?'))return;
                try{
                    const res=await fetch('/api/try-it/admin/reject/'+id,{method:'POST',headers:{'X-Admin-Key':adminKey,'X-Admin-Email':adminEmail,'Content-Type':'application/json'},body:'{}'});
                    if(res.ok)loadRequests();else alert('Failed');
                }catch(e){alert('Error');}
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
                    requests=await res.json();isLoggedIn=true;loginOverlay.style.display='none';adminContent.style.display='flex';renderRequests();
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
            """;
    }
}
