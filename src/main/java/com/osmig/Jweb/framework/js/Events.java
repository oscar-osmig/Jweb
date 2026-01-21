package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Event patterns: delegation, debouncing, throttling, history, SSE.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.Events.*;
 *
 * // With Func (type-safe)
 * delegate("ul", "click", "li").handler(callback("e").log(variable("e")))
 *
 * // With String (quick)
 * debounce("timer", 300).wrap("search()")
 * </pre>
 */
public final class Events {
    private Events() {}

    public static Delegate delegate(String parent, String event, String child) {
        return new Delegate(parent, event, child);
    }

    public static Debounce debounce(String timerVar, int delayMs) {
        return new Debounce(timerVar, delayMs);
    }

    public static Throttle throttle(String lastVar, int intervalMs) {
        return new Throttle(lastVar, intervalMs);
    }

    public static PopState onPopState(String guardVar) {
        return new PopState(guardVar);
    }

    public static Val pushState(String url) {
        return new Val("history.pushState(null,''," + JS.toJs(url) + ")");
    }

    public static Val pushStateExpr(Val urlExpr) {
        return new Val("history.pushState(null,''," + urlExpr.js() + ")");
    }

    public static Val setInnerHTML(String selector, Val valueExpr) {
        return new Val("document.querySelector('" + JS.esc(selector) + "').innerHTML=" + valueExpr.js());
    }

    public static SSE sse(String url) {
        return new SSE(url);
    }

    // ==================== Delegate ====================

    public static class Delegate {
        private final String parent, event, child;

        Delegate(String parent, String event, String child) {
            this.parent = parent;
            this.event = event;
            this.child = child;
        }

        /** Handler with Func callback (type-safe) */
        public Val handler(Func callback) {
            return new Val("document.querySelector('" + JS.esc(parent) + "').addEventListener('" + event +
                "',function(e){var t=e.target.closest('" + JS.esc(child) + "');if(!t)return;(" +
                callback.toExpr() + ")(e,t)})");
        }

        /** Handler with raw code string */
        public Val handler(String handlerCode) {
            return new Val("document.querySelector('" + JS.esc(parent) + "').addEventListener('" + event +
                "',function(e){var t=e.target.closest('" + JS.esc(child) + "');if(!t)return;" + handlerCode + "})");
        }
    }

    // ==================== Debounce ====================

    public static class Debounce {
        private final String timerVar;
        private final int delayMs;

        Debounce(String timerVar, int delayMs) {
            this.timerVar = timerVar;
            this.delayMs = delayMs;
        }

        /** Wrap with Func callback */
        public Val wrap(Func action) {
            return new Val("if(" + timerVar + ")clearTimeout(" + timerVar + ");" +
                timerVar + "=setTimeout(" + action.toExpr() + "," + delayMs + ")");
        }

        /** Wrap with raw code string */
        public Val wrap(String actionCode) {
            return new Val("if(" + timerVar + ")clearTimeout(" + timerVar + ");" +
                timerVar + "=setTimeout(function(){" + actionCode + "}," + delayMs + ")");
        }

        public Val clear() {
            return new Val("if(" + timerVar + "){clearTimeout(" + timerVar + ");" + timerVar + "=null}");
        }
    }

    // ==================== Throttle ====================

    public static class Throttle {
        private final String lastVar;
        private final int intervalMs;

        Throttle(String lastVar, int intervalMs) {
            this.lastVar = lastVar;
            this.intervalMs = intervalMs;
        }

        /** Wrap with Func callback */
        public Val wrap(Func action) {
            return new Val("if(Date.now()-" + lastVar + ">=" + intervalMs + "){" +
                lastVar + "=Date.now();(" + action.toExpr() + ")()}");
        }

        /** Wrap with raw code string */
        public Val wrap(String actionCode) {
            return new Val("if(Date.now()-" + lastVar + ">=" + intervalMs + "){" +
                lastVar + "=Date.now();" + actionCode + "}");
        }

        /** Throttle with trailing edge */
        public Val wrapTrailing(String timerVar, String actionCode) {
            return new Val("if(Date.now()-" + lastVar + ">=" + intervalMs + "){" +
                lastVar + "=Date.now();" + actionCode +
                "}else if(!" + timerVar + "){" + timerVar + "=setTimeout(function(){" +
                timerVar + "=null;" + lastVar + "=Date.now();" + actionCode + "}," +
                intervalMs + "-(Date.now()-" + lastVar + "))}");
        }

        public Val reset() {
            return new Val(lastVar + "=0");
        }
    }

    // ==================== PopState ====================

    public static class PopState {
        private final String guardVar;

        PopState(String guardVar) {
            this.guardVar = guardVar;
        }

        /** Handler with Func callback */
        public Val handler(Func callback) {
            return new Val("if(!window." + guardVar + "){window." + guardVar + "=true;" +
                "window.addEventListener('popstate'," + callback.toExpr() + ")}");
        }

        /** Handler with raw code string */
        public Val handler(String handlerCode) {
            return new Val("if(!window." + guardVar + "){window." + guardVar + "=true;" +
                "window.addEventListener('popstate',function(){" + handlerCode + "})}");
        }
    }

    // ==================== SSE ====================

    public static class SSE {
        private final String url;
        private Func onMessageFunc;
        private Func onErrorFunc;
        private Func onOpenFunc;
        private String onMessageCode;
        private String onErrorCode;
        private String onOpenCode;

        SSE(String url) {
            this.url = url;
        }

        /** Set onMessage with Func */
        public SSE onMessage(Func callback) {
            this.onMessageFunc = callback;
            this.onMessageCode = null;
            return this;
        }

        /** Set onMessage with raw code */
        public SSE onMessage(String code) {
            this.onMessageCode = code;
            this.onMessageFunc = null;
            return this;
        }

        /** Set onError with Func */
        public SSE onError(Func callback) {
            this.onErrorFunc = callback;
            this.onErrorCode = null;
            return this;
        }

        /** Set onError with raw code */
        public SSE onError(String code) {
            this.onErrorCode = code;
            this.onErrorFunc = null;
            return this;
        }

        /** Set onOpen with Func */
        public SSE onOpen(Func callback) {
            this.onOpenFunc = callback;
            this.onOpenCode = null;
            return this;
        }

        /** Set onOpen with raw code */
        public SSE onOpen(String code) {
            this.onOpenCode = code;
            this.onOpenFunc = null;
            return this;
        }

        /** Returns as Val for use in expressions */
        public Val toVal() {
            StringBuilder sb = new StringBuilder("(function(){var es=new EventSource('")
                .append(JS.esc(url)).append("');");

            if (onMessageFunc != null) {
                sb.append("es.onmessage=").append(onMessageFunc.toExpr()).append(";");
            } else if (onMessageCode != null) {
                sb.append("es.onmessage=function(e){").append(onMessageCode).append("};");
            }

            if (onErrorFunc != null) {
                sb.append("es.onerror=").append(onErrorFunc.toExpr()).append(";");
            } else if (onErrorCode != null) {
                sb.append("es.onerror=function(e){").append(onErrorCode).append("};");
            }

            if (onOpenFunc != null) {
                sb.append("es.onopen=").append(onOpenFunc.toExpr()).append(";");
            } else if (onOpenCode != null) {
                sb.append("es.onopen=function(e){").append(onOpenCode).append("};");
            }

            return new Val(sb.append("return es}())").toString());
        }

        /** Builds as standalone code block */
        public String build() {
            return toVal().js();
        }
    }

    // ==================== Keyboard Events ====================

    /** Creates a keyboard shortcut listener */
    public static Val onKeyCombo(String combo, Func handler) {
        String[] parts = combo.toLowerCase().split("\\+");
        StringBuilder condition = new StringBuilder();
        String key = parts[parts.length - 1];

        for (String part : parts) {
            if (part.equals("ctrl") || part.equals("control")) condition.append("e.ctrlKey&&");
            else if (part.equals("alt")) condition.append("e.altKey&&");
            else if (part.equals("shift")) condition.append("e.shiftKey&&");
            else if (part.equals("meta") || part.equals("cmd")) condition.append("e.metaKey&&");
        }
        condition.append("e.key.toLowerCase()==='").append(key).append("'");

        return new Val("document.addEventListener('keydown',function(e){if(" + condition + "){e.preventDefault();(" + handler.toExpr() + ")(e)}})");
    }

    /** Creates a keyboard shortcut listener with raw code */
    public static Val onKeyCombo(String combo, String code) {
        String[] parts = combo.toLowerCase().split("\\+");
        StringBuilder condition = new StringBuilder();
        String key = parts[parts.length - 1];

        for (String part : parts) {
            if (part.equals("ctrl") || part.equals("control")) condition.append("e.ctrlKey&&");
            else if (part.equals("alt")) condition.append("e.altKey&&");
            else if (part.equals("shift")) condition.append("e.shiftKey&&");
            else if (part.equals("meta") || part.equals("cmd")) condition.append("e.metaKey&&");
        }
        condition.append("e.key.toLowerCase()==='").append(key).append("'");

        return new Val("document.addEventListener('keydown',function(e){if(" + condition + "){e.preventDefault();" + code + "}})");
    }

    /** Listens for specific key press */
    public static Val onKey(String key, Func handler) {
        return new Val("document.addEventListener('keydown',function(e){if(e.key==='" + JS.esc(key) + "')(" + handler.toExpr() + ")(e)})");
    }

    /** Listens for Escape key */
    public static Val onEscape(Func handler) {
        return new Val("document.addEventListener('keydown',function(e){if(e.key==='Escape')(" + handler.toExpr() + ")(e)})");
    }

    /** Listens for Enter key */
    public static Val onEnter(Func handler) {
        return new Val("document.addEventListener('keydown',function(e){if(e.key==='Enter')(" + handler.toExpr() + ")(e)})");
    }

    /** Listens for Enter key on specific element */
    public static Val onEnter(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('keydown',function(e){if(e.key==='Enter')(" + handler.toExpr() + ")(e)})");
    }

    /** Gets key from event */
    public static Val eventKey(Val event) { return new Val(event.js() + ".key"); }

    /** Gets key code from event */
    public static Val eventKeyCode(Val event) { return new Val(event.js() + ".keyCode"); }

    /** Checks if event has modifier key */
    public static Val hasCtrl(Val event) { return new Val(event.js() + ".ctrlKey"); }
    public static Val hasAlt(Val event) { return new Val(event.js() + ".altKey"); }
    public static Val hasShift(Val event) { return new Val(event.js() + ".shiftKey"); }
    public static Val hasMeta(Val event) { return new Val(event.js() + ".metaKey"); }

    // ==================== Touch Events ====================

    /** Listens for touch start */
    public static Val onTouchStart(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('touchstart'," + handler.toExpr() + ",{passive:true})");
    }

    /** Listens for touch move */
    public static Val onTouchMove(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('touchmove'," + handler.toExpr() + ",{passive:true})");
    }

    /** Listens for touch end */
    public static Val onTouchEnd(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('touchend'," + handler.toExpr() + ")");
    }

    /** Listens for touch cancel */
    public static Val onTouchCancel(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('touchcancel'," + handler.toExpr() + ")");
    }

    /** Gets first touch from event */
    public static Val firstTouch(Val event) { return new Val(event.js() + ".touches[0]"); }

    /** Gets touch X coordinate */
    public static Val touchX(Val touch) { return new Val(touch.js() + ".clientX"); }

    /** Gets touch Y coordinate */
    public static Val touchY(Val touch) { return new Val(touch.js() + ".clientY"); }

    /** Gets all touches from event */
    public static Val touches(Val event) { return new Val(event.js() + ".touches"); }

    /** Gets changed touches from event */
    public static Val changedTouches(Val event) { return new Val(event.js() + ".changedTouches"); }

    /** Gets number of active touches */
    public static Val touchCount(Val event) { return new Val(event.js() + ".touches.length"); }

    /** Creates swipe detector */
    public static SwipeBuilder swipe(Val element) {
        return new SwipeBuilder(element);
    }

    // ==================== Swipe Builder ====================

    public static class SwipeBuilder {
        private final Val element;
        private int threshold = 50;
        private Func onLeftFunc, onRightFunc, onUpFunc, onDownFunc;
        private String onLeftCode, onRightCode, onUpCode, onDownCode;

        SwipeBuilder(Val element) { this.element = element; }

        public SwipeBuilder threshold(int px) { this.threshold = px; return this; }

        public SwipeBuilder onLeft(Func handler) { this.onLeftFunc = handler; return this; }
        public SwipeBuilder onLeft(String code) { this.onLeftCode = code; return this; }
        public SwipeBuilder onRight(Func handler) { this.onRightFunc = handler; return this; }
        public SwipeBuilder onRight(String code) { this.onRightCode = code; return this; }
        public SwipeBuilder onUp(Func handler) { this.onUpFunc = handler; return this; }
        public SwipeBuilder onUp(String code) { this.onUpCode = code; return this; }
        public SwipeBuilder onDown(Func handler) { this.onDownFunc = handler; return this; }
        public SwipeBuilder onDown(String code) { this.onDownCode = code; return this; }

        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var sx,sy;");
            sb.append(element.js()).append(".addEventListener('touchstart',function(e){var t=e.touches[0];sx=t.clientX;sy=t.clientY},{passive:true});");
            sb.append(element.js()).append(".addEventListener('touchend',function(e){var t=e.changedTouches[0],dx=t.clientX-sx,dy=t.clientY-sy;");
            sb.append("if(Math.abs(dx)>Math.abs(dy)&&Math.abs(dx)>").append(threshold).append("){");
            if (onLeftFunc != null) sb.append("if(dx<0)(").append(onLeftFunc.toExpr()).append(")();");
            else if (onLeftCode != null) sb.append("if(dx<0){").append(onLeftCode).append("}");
            if (onRightFunc != null) sb.append("if(dx>0)(").append(onRightFunc.toExpr()).append(")();");
            else if (onRightCode != null) sb.append("if(dx>0){").append(onRightCode).append("}");
            sb.append("}else if(Math.abs(dy)>").append(threshold).append("){");
            if (onUpFunc != null) sb.append("if(dy<0)(").append(onUpFunc.toExpr()).append(")();");
            else if (onUpCode != null) sb.append("if(dy<0){").append(onUpCode).append("}");
            if (onDownFunc != null) sb.append("if(dy>0)(").append(onDownFunc.toExpr()).append(")();");
            else if (onDownCode != null) sb.append("if(dy>0){").append(onDownCode).append("}");
            sb.append("}})})()");
            return new Val(sb.toString());
        }
    }

    // ==================== Event Utilities ====================

    /** Prevents default behavior */
    public static Val preventDefault(Val event) {
        return new Val(event.js() + ".preventDefault()");
    }

    /** Stops propagation */
    public static Val stopPropagation(Val event) {
        return new Val(event.js() + ".stopPropagation()");
    }

    /** Stops immediate propagation */
    public static Val stopImmediatePropagation(Val event) {
        return new Val(event.js() + ".stopImmediatePropagation()");
    }

    /** Creates custom event */
    public static Val customEvent(String type, Val detail) {
        return new Val("new CustomEvent('" + JS.esc(type) + "',{detail:" + detail.js() + ",bubbles:true})");
    }

    /** Dispatches custom event on element */
    public static Val dispatchCustomEvent(Val element, String type, Val detail) {
        return new Val(element.js() + ".dispatchEvent(new CustomEvent('" + JS.esc(type) + "',{detail:" + detail.js() + ",bubbles:true}))");
    }

    /** Listens for custom event */
    public static Val onCustomEvent(Val element, String type, Func handler) {
        return new Val(element.js() + ".addEventListener('" + JS.esc(type) + "'," + handler.toExpr() + ")");
    }

    /** Gets custom event detail */
    public static Val eventDetail(Val event) {
        return new Val(event.js() + ".detail");
    }

    /** Adds event listener with once option */
    public static Val once(Val element, String type, Func handler) {
        return new Val(element.js() + ".addEventListener('" + JS.esc(type) + "'," + handler.toExpr() + ",{once:true})");
    }

    /** Adds event listener with capture option */
    public static Val capture(Val element, String type, Func handler) {
        return new Val(element.js() + ".addEventListener('" + JS.esc(type) + "'," + handler.toExpr() + ",{capture:true})");
    }

    /** Adds passive event listener */
    public static Val passive(Val element, String type, Func handler) {
        return new Val(element.js() + ".addEventListener('" + JS.esc(type) + "'," + handler.toExpr() + ",{passive:true})");
    }
}
