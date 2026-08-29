package com.osmig.Jweb.app.docs;

import static com.osmig.Jweb.framework.js.Runtime.*;
import static com.osmig.Jweb.framework.js.Events.*;

/**
 * Click-to-copy for docs code blocks. Delegated to .docs-layout so buttons
 * keep working after DocsNavScript swaps .docs-content's innerHTML.
 */
final class CodeCopyScript {
    private CodeCopyScript() {}

    static String build() {
        return iife()
            .unsafeRaw(guard("__codeCopyInit")
                .unsafeRaw(feedbackFunc())
                .unsafeRaw(legacyCopyFunc())
                .unsafeRaw(clickHandler())
                .build())
            .build();
    }

    private static String feedbackFunc() {
        return "function copyFeedback(btn,ok){" +
            "btn.textContent=ok?'Copied!':'Copy failed';" +
            "btn.classList.toggle('copied',ok);" +
            "if(btn.__copyTimer)clearTimeout(btn.__copyTimer);" +
            "btn.__copyTimer=setTimeout(function(){" +
            "btn.textContent='Copy';btn.classList.remove('copied')},1600)}";
    }

    // execCommand fallback for browsers/contexts without navigator.clipboard
    private static String legacyCopyFunc() {
        return "function legacyCopy(txt){" +
            "var ta=document.createElement('textarea');ta.value=txt;" +
            "ta.setAttribute('readonly','');ta.style.position='fixed';ta.style.opacity='0';" +
            "document.body.appendChild(ta);ta.select();" +
            "var ok=false;try{ok=document.execCommand('copy')}catch(err){}" +
            "document.body.removeChild(ta);return ok}";
    }

    private static String clickHandler() {
        return delegate(".docs-layout", "click", ".code-copy-btn")
            .handler("var pre=t.parentElement.querySelector('pre');if(!pre)return;" +
                "var txt=pre.textContent;" +
                "if(navigator.clipboard&&navigator.clipboard.writeText){" +
                "navigator.clipboard.writeText(txt).then(" +
                "function(){copyFeedback(t,true)}," +
                "function(){copyFeedback(t,legacyCopy(txt))})}" +
                "else{copyFeedback(t,legacyCopy(txt))}")
            .js();
    }
}
