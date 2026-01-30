package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Pointer Events API DSL for unified mouse, touch, and pen input handling.
 *
 * <p>Pointer Events provide a single, unified event model for all pointing
 * devices (mouse, touch, pen/stylus) with additional properties like
 * pressure, tilt, and twist.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSPointer.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Basic pointer event listening
 * onPointerDown("canvas", callback("e")
 *     .log(pointerId(variable("e")))
 *     .log(pressure(variable("e"))))
 *
 * // Pointer capture for continuous tracking
 * onPointerDown("slider", callback("e")
 *     .unsafeRaw("e.target.setPointerCapture(e.pointerId)"))
 *
 * // Multi-touch tracking
 * multiPointerTracker("canvas")
 *     .onStart(callback("e").log("pointer down:", pointerId(variable("e"))))
 *     .onMove(callback("e").log("moving:", clientX(variable("e"))))
 *     .onEnd(callback("e").log("pointer up:", pointerId(variable("e"))))
 *     .build()
 *
 * // Access pointer properties
 * pressure(variable("e"))      // 0.0 to 1.0
 * tiltX(variable("e"))         // -90 to 90
 * pointerType(variable("e"))   // "mouse", "touch", or "pen"
 * isPrimary(variable("e"))     // true for primary pointer
 * </pre>
 */
public final class JSPointer {
    private JSPointer() {}

    // ==================== Event Listeners ====================

    /** Listens for pointerdown events on an element. */
    public static Val onPointerDown(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerdown'," + handler.toExpr() + ")");
    }

    /** Listens for pointerdown events on an element expression. */
    public static Val onPointerDown(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('pointerdown'," + handler.toExpr() + ")");
    }

    /** Listens for pointermove events on an element. */
    public static Val onPointerMove(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointermove'," + handler.toExpr() + ")");
    }

    /** Listens for pointermove events on an element expression. */
    public static Val onPointerMove(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('pointermove'," + handler.toExpr() + ")");
    }

    /** Listens for pointerup events on an element. */
    public static Val onPointerUp(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerup'," + handler.toExpr() + ")");
    }

    /** Listens for pointerup events on an element expression. */
    public static Val onPointerUp(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('pointerup'," + handler.toExpr() + ")");
    }

    /** Listens for pointercancel events on an element. */
    public static Val onPointerCancel(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointercancel'," + handler.toExpr() + ")");
    }

    /** Listens for pointerenter events (does not bubble). */
    public static Val onPointerEnter(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerenter'," + handler.toExpr() + ")");
    }

    /** Listens for pointerleave events (does not bubble). */
    public static Val onPointerLeave(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerleave'," + handler.toExpr() + ")");
    }

    /** Listens for pointerover events (bubbles). */
    public static Val onPointerOver(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerover'," + handler.toExpr() + ")");
    }

    /** Listens for pointerout events (bubbles). */
    public static Val onPointerOut(String elementId, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('pointerout'," + handler.toExpr() + ")");
    }

    /** Listens for gotpointercapture events. */
    public static Val onGotPointerCapture(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('gotpointercapture'," + handler.toExpr() + ")");
    }

    /** Listens for lostpointercapture events. */
    public static Val onLostPointerCapture(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('lostpointercapture'," + handler.toExpr() + ")");
    }

    // ==================== Pointer Capture ====================

    /**
     * Sets pointer capture on an element.
     *
     * @param element the target element
     * @param pointerId the pointer ID to capture
     * @return a Val representing setPointerCapture call
     */
    public static Val setPointerCapture(Val element, Val pointerId) {
        return new Val(element.js() + ".setPointerCapture(" + pointerId.js() + ")");
    }

    /**
     * Releases pointer capture on an element.
     *
     * @param element the target element
     * @param pointerId the pointer ID to release
     * @return a Val representing releasePointerCapture call
     */
    public static Val releasePointerCapture(Val element, Val pointerId) {
        return new Val(element.js() + ".releasePointerCapture(" + pointerId.js() + ")");
    }

    /**
     * Checks if an element has pointer capture.
     *
     * @param element the target element
     * @param pointerId the pointer ID to check
     * @return a Val representing hasPointerCapture result
     */
    public static Val hasPointerCapture(Val element, Val pointerId) {
        return new Val(element.js() + ".hasPointerCapture(" + pointerId.js() + ")");
    }

    // ==================== Pointer Event Properties ====================

    /** Gets the pointer ID: event.pointerId */
    public static Val pointerId(Val event) {
        return new Val(event.js() + ".pointerId");
    }

    /** Gets the pointer type ("mouse", "touch", "pen"): event.pointerType */
    public static Val pointerType(Val event) {
        return new Val(event.js() + ".pointerType");
    }

    /** Gets whether this is the primary pointer: event.isPrimary */
    public static Val isPrimary(Val event) {
        return new Val(event.js() + ".isPrimary");
    }

    /** Gets pressure (0.0 to 1.0, 0.5 for mouse buttons): event.pressure */
    public static Val pressure(Val event) {
        return new Val(event.js() + ".pressure");
    }

    /** Gets tangential pressure (-1.0 to 1.0): event.tangentialPressure */
    public static Val tangentialPressure(Val event) {
        return new Val(event.js() + ".tangentialPressure");
    }

    /** Gets tilt X angle (-90 to 90 degrees): event.tiltX */
    public static Val tiltX(Val event) {
        return new Val(event.js() + ".tiltX");
    }

    /** Gets tilt Y angle (-90 to 90 degrees): event.tiltY */
    public static Val tiltY(Val event) {
        return new Val(event.js() + ".tiltY");
    }

    /** Gets twist angle (0 to 359 degrees): event.twist */
    public static Val twist(Val event) {
        return new Val(event.js() + ".twist");
    }

    /** Gets the width of the pointer contact area: event.width */
    public static Val width(Val event) {
        return new Val(event.js() + ".width");
    }

    /** Gets the height of the pointer contact area: event.height */
    public static Val height(Val event) {
        return new Val(event.js() + ".height");
    }

    /** Gets clientX coordinate: event.clientX */
    public static Val clientX(Val event) {
        return new Val(event.js() + ".clientX");
    }

    /** Gets clientY coordinate: event.clientY */
    public static Val clientY(Val event) {
        return new Val(event.js() + ".clientY");
    }

    /** Gets pageX coordinate: event.pageX */
    public static Val pageX(Val event) {
        return new Val(event.js() + ".pageX");
    }

    /** Gets pageY coordinate: event.pageY */
    public static Val pageY(Val event) {
        return new Val(event.js() + ".pageY");
    }

    /** Gets offsetX coordinate (relative to target): event.offsetX */
    public static Val offsetX(Val event) {
        return new Val(event.js() + ".offsetX");
    }

    /** Gets offsetY coordinate (relative to target): event.offsetY */
    public static Val offsetY(Val event) {
        return new Val(event.js() + ".offsetY");
    }

    /**
     * Gets coalesced events (intermediate points between moves).
     * Useful for drawing applications.
     *
     * @param event the pointer event
     * @return a Val representing the array of coalesced PointerEvents
     */
    public static Val getCoalescedEvents(Val event) {
        return new Val(event.js() + ".getCoalescedEvents()");
    }

    /**
     * Gets predicted events (future predicted points).
     * Useful for reducing perceived latency.
     *
     * @param event the pointer event
     * @return a Val representing the array of predicted PointerEvents
     */
    public static Val getPredictedEvents(Val event) {
        return new Val(event.js() + ".getPredictedEvents()");
    }

    // ==================== Multi-Pointer Tracker ====================

    /**
     * Creates a multi-pointer tracker for handling multiple simultaneous pointers.
     *
     * @param elementId the element to track pointers on
     * @return a MultiPointerBuilder
     */
    public static MultiPointerBuilder multiPointerTracker(String elementId) {
        return new MultiPointerBuilder(elementId);
    }

    /**
     * Creates a multi-pointer tracker with element expression.
     *
     * @param element the element reference
     * @return a MultiPointerBuilder
     */
    public static MultiPointerBuilder multiPointerTracker(Val element) {
        return new MultiPointerBuilder(element);
    }

    /**
     * Builder for multi-pointer tracking with automatic pointer capture.
     */
    public static class MultiPointerBuilder {
        private final String elemExpr;
        private String onStart;
        private String onMove;
        private String onEnd;
        private boolean preventTouch = true;

        MultiPointerBuilder(String elementId) {
            this.elemExpr = "document.getElementById('" + JS.esc(elementId) + "')";
        }

        MultiPointerBuilder(Val element) {
            this.elemExpr = element.js();
        }

        /** Callback for when a pointer starts (down). */
        public MultiPointerBuilder onStart(Func handler) {
            this.onStart = handler.toExpr();
            return this;
        }

        /** Callback for when a pointer moves. */
        public MultiPointerBuilder onMove(Func handler) {
            this.onMove = handler.toExpr();
            return this;
        }

        /** Callback for when a pointer ends (up or cancel). */
        public MultiPointerBuilder onEnd(Func handler) {
            this.onEnd = handler.toExpr();
            return this;
        }

        /** Whether to prevent default touch actions (default: true). */
        public MultiPointerBuilder preventTouch(boolean prevent) {
            this.preventTouch = prevent;
            return this;
        }

        /**
         * Builds the multi-pointer tracker.
         *
         * @return a Val representing the tracker setup
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var el=").append(elemExpr)
                .append(";var ptrs=new Map();");

            if (preventTouch) {
                sb.append("el.style.touchAction='none';");
            }

            sb.append("el.addEventListener('pointerdown',function(e){")
              .append("ptrs.set(e.pointerId,e);el.setPointerCapture(e.pointerId);");
            if (onStart != null) {
                sb.append("(").append(onStart).append(")(e,ptrs);");
            }
            sb.append("});");

            sb.append("el.addEventListener('pointermove',function(e){")
              .append("if(ptrs.has(e.pointerId)){ptrs.set(e.pointerId,e);");
            if (onMove != null) {
                sb.append("(").append(onMove).append(")(e,ptrs);");
            }
            sb.append("}});");

            String endHandler = "function(e){ptrs.delete(e.pointerId);"
                + (onEnd != null ? "(" + onEnd + ")(e,ptrs);" : "")
                + "}";
            sb.append("el.addEventListener('pointerup',").append(endHandler).append(");");
            sb.append("el.addEventListener('pointercancel',").append(endHandler).append(");");

            sb.append("}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Touch Action CSS ====================

    /**
     * Sets touch-action CSS property on an element to control pointer behavior.
     *
     * @param element the element reference
     * @param action one of: "none", "auto", "pan-x", "pan-y", "pan-left", "pan-right",
     *               "pan-up", "pan-down", "manipulation", "pinch-zoom"
     * @return a Val representing the style assignment
     */
    public static Val setTouchAction(Val element, String action) {
        return new Val(element.js() + ".style.touchAction='" + JS.esc(action) + "'");
    }

    /**
     * Disables all touch actions on an element (for custom gesture handling).
     *
     * @param element the element reference
     * @return a Val representing the style assignment
     */
    public static Val disableTouchAction(Val element) {
        return setTouchAction(element, "none");
    }
}
