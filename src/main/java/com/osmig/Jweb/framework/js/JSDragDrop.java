package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Drag and Drop API DSL for building interactive drag-and-drop interfaces.
 *
 * <p>Provides type-safe wrappers for the HTML Drag and Drop API including
 * draggable configuration, drop zones, DataTransfer operations, and
 * all drag events.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSDragDrop.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Make an element draggable with data
 * draggable("card-1")
 *     .data("text/plain", "Card 1 data")
 *     .data("application/json", str("{\"id\":1}"))
 *     .effectAllowed("move")
 *     .onDragStart(callback("e").log("drag started"))
 *     .build()
 *
 * // Create a drop zone
 * dropZone("target-area")
 *     .dropEffect("move")
 *     .onDrop(callback("e")
 *         .let_("data", getData(variable("e"), "text/plain"))
 *         .log(variable("data")))
 *     .onDragOver(callback("e")
 *         .unsafeRaw("e.currentTarget.classList.add('drag-over')"))
 *     .onDragLeave(callback("e")
 *         .unsafeRaw("e.currentTarget.classList.remove('drag-over')"))
 *     .build()
 *
 * // Access DataTransfer
 * getData(variable("event"), "text/plain")
 * getFiles(variable("event"))
 * </pre>
 */
public final class JSDragDrop {
    private JSDragDrop() {}

    // ==================== Draggable Setup ====================

    /**
     * Creates a draggable element configuration.
     *
     * @param elementId the ID of the element to make draggable
     * @return a DraggableBuilder for configuring the drag behavior
     */
    public static DraggableBuilder draggable(String elementId) {
        return new DraggableBuilder(elementId);
    }

    /**
     * Creates a draggable element configuration with expression.
     *
     * @param element the element reference
     * @return a DraggableBuilder for configuring the drag behavior
     */
    public static DraggableBuilder draggable(Val element) {
        return new DraggableBuilder(element);
    }

    /**
     * Builder for configuring a draggable element.
     */
    public static class DraggableBuilder {
        private final String elemExpr;
        private String effectAllowed;
        private String dragImage;
        private final java.util.List<String> dataItems = new java.util.ArrayList<>();
        private String onDragStart;
        private String onDrag;
        private String onDragEnd;

        DraggableBuilder(String elementId) {
            this.elemExpr = "document.getElementById('" + JS.esc(elementId) + "')";
        }

        DraggableBuilder(Val element) {
            this.elemExpr = element.js();
        }

        /**
         * Sets data to transfer during drag.
         *
         * @param type MIME type (e.g., "text/plain", "application/json")
         * @param data the string data
         * @return this builder
         */
        public DraggableBuilder data(String type, String data) {
            dataItems.add("e.dataTransfer.setData('" + JS.esc(type) + "','" + JS.esc(data) + "')");
            return this;
        }

        /**
         * Sets data from expression.
         *
         * @param type MIME type
         * @param data the data expression
         * @return this builder
         */
        public DraggableBuilder data(String type, Val data) {
            dataItems.add("e.dataTransfer.setData('" + JS.esc(type) + "'," + data.js() + ")");
            return this;
        }

        /**
         * Sets the allowed drag effect.
         *
         * @param effect one of: "none", "copy", "link", "move", "copyLink", "copyMove", "linkMove", "all"
         * @return this builder
         */
        public DraggableBuilder effectAllowed(String effect) {
            this.effectAllowed = effect;
            return this;
        }

        /**
         * Sets a custom drag image.
         *
         * @param imageElement the image element expression
         * @param xOffset horizontal offset
         * @param yOffset vertical offset
         * @return this builder
         */
        public DraggableBuilder dragImage(Val imageElement, int xOffset, int yOffset) {
            this.dragImage = "e.dataTransfer.setDragImage(" + imageElement.js() + "," + xOffset + "," + yOffset + ")";
            return this;
        }

        /** Callback fired when drag starts. */
        public DraggableBuilder onDragStart(Func handler) {
            this.onDragStart = handler.toExpr();
            return this;
        }

        /** Callback fired continuously during drag. */
        public DraggableBuilder onDrag(Func handler) {
            this.onDrag = handler.toExpr();
            return this;
        }

        /** Callback fired when drag ends. */
        public DraggableBuilder onDragEnd(Func handler) {
            this.onDragEnd = handler.toExpr();
            return this;
        }

        /**
         * Builds the draggable setup code.
         *
         * @return a Val representing the complete draggable setup
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var el=").append(elemExpr)
                .append(";el.draggable=true;");

            // dragstart handler with data setup
            sb.append("el.addEventListener('dragstart',function(e){");
            for (String data : dataItems) {
                sb.append(data).append(";");
            }
            if (effectAllowed != null) {
                sb.append("e.dataTransfer.effectAllowed='").append(JS.esc(effectAllowed)).append("';");
            }
            if (dragImage != null) {
                sb.append(dragImage).append(";");
            }
            if (onDragStart != null) {
                sb.append("(").append(onDragStart).append(")(e);");
            }
            sb.append("});");

            if (onDrag != null) {
                sb.append("el.addEventListener('drag',").append(onDrag).append(");");
            }
            if (onDragEnd != null) {
                sb.append("el.addEventListener('dragend',").append(onDragEnd).append(");");
            }

            sb.append("}())");
            return new Val(sb.toString());
        }
    }

    // ==================== Drop Zone Setup ====================

    /**
     * Creates a drop zone configuration.
     *
     * @param elementId the ID of the drop target element
     * @return a DropZoneBuilder for configuring the drop zone
     */
    public static DropZoneBuilder dropZone(String elementId) {
        return new DropZoneBuilder(elementId);
    }

    /**
     * Creates a drop zone configuration with expression.
     *
     * @param element the element reference
     * @return a DropZoneBuilder for configuring the drop zone
     */
    public static DropZoneBuilder dropZone(Val element) {
        return new DropZoneBuilder(element);
    }

    /**
     * Builder for configuring a drop zone.
     */
    public static class DropZoneBuilder {
        private final String elemExpr;
        private String dropEffect;
        private String onDrop;
        private String onDragEnter;
        private String onDragOver;
        private String onDragLeave;

        DropZoneBuilder(String elementId) {
            this.elemExpr = "document.getElementById('" + JS.esc(elementId) + "')";
        }

        DropZoneBuilder(Val element) {
            this.elemExpr = element.js();
        }

        /**
         * Sets the drop effect.
         *
         * @param effect one of: "none", "copy", "link", "move"
         * @return this builder
         */
        public DropZoneBuilder dropEffect(String effect) {
            this.dropEffect = effect;
            return this;
        }

        /** Callback fired when a dragged element is dropped on this zone. */
        public DropZoneBuilder onDrop(Func handler) {
            this.onDrop = handler.toExpr();
            return this;
        }

        /** Callback fired when a dragged element enters this zone. */
        public DropZoneBuilder onDragEnter(Func handler) {
            this.onDragEnter = handler.toExpr();
            return this;
        }

        /** Callback fired when a dragged element is over this zone. */
        public DropZoneBuilder onDragOver(Func handler) {
            this.onDragOver = handler.toExpr();
            return this;
        }

        /** Callback fired when a dragged element leaves this zone. */
        public DropZoneBuilder onDragLeave(Func handler) {
            this.onDragLeave = handler.toExpr();
            return this;
        }

        /**
         * Builds the drop zone setup code.
         *
         * @return a Val representing the complete drop zone setup
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("(function(){var el=").append(elemExpr).append(";");

            // Prevent default on dragover to allow drop
            sb.append("el.addEventListener('dragover',function(e){e.preventDefault();");
            if (dropEffect != null) {
                sb.append("e.dataTransfer.dropEffect='").append(JS.esc(dropEffect)).append("';");
            }
            if (onDragOver != null) {
                sb.append("(").append(onDragOver).append(")(e);");
            }
            sb.append("});");

            // Prevent default on dragenter
            sb.append("el.addEventListener('dragenter',function(e){e.preventDefault();");
            if (onDragEnter != null) {
                sb.append("(").append(onDragEnter).append(")(e);");
            }
            sb.append("});");

            if (onDragLeave != null) {
                sb.append("el.addEventListener('dragleave',").append(onDragLeave).append(");");
            }

            if (onDrop != null) {
                sb.append("el.addEventListener('drop',function(e){e.preventDefault();(")
                  .append(onDrop).append(")(e);});");
            }

            sb.append("}())");
            return new Val(sb.toString());
        }
    }

    // ==================== DataTransfer Helpers ====================

    /**
     * Gets data from a drag event's DataTransfer.
     *
     * @param event the drag event
     * @param type the MIME type to retrieve
     * @return a Val representing the data string
     */
    public static Val getData(Val event, String type) {
        return new Val(event.js() + ".dataTransfer.getData('" + JS.esc(type) + "')");
    }

    /**
     * Sets data on a drag event's DataTransfer.
     *
     * @param event the drag event
     * @param type the MIME type
     * @param data the data to set
     * @return a Val representing the setData call
     */
    public static Val setData(Val event, String type, String data) {
        return new Val(event.js() + ".dataTransfer.setData('" + JS.esc(type) + "','" + JS.esc(data) + "')");
    }

    /**
     * Sets data from expression on a drag event's DataTransfer.
     *
     * @param event the drag event
     * @param type the MIME type
     * @param data the data expression
     * @return a Val representing the setData call
     */
    public static Val setData(Val event, String type, Val data) {
        return new Val(event.js() + ".dataTransfer.setData('" + JS.esc(type) + "'," + data.js() + ")");
    }

    /**
     * Gets files from a drag event's DataTransfer.
     *
     * @param event the drag event
     * @return a Val representing the FileList
     */
    public static Val getFiles(Val event) {
        return new Val(event.js() + ".dataTransfer.files");
    }

    /**
     * Gets DataTransfer items (for advanced use with types).
     *
     * @param event the drag event
     * @return a Val representing the DataTransferItemList
     */
    public static Val getItems(Val event) {
        return new Val(event.js() + ".dataTransfer.items");
    }

    /**
     * Gets available MIME types from a drag event.
     *
     * @param event the drag event
     * @return a Val representing the types array
     */
    public static Val getTypes(Val event) {
        return new Val(event.js() + ".dataTransfer.types");
    }

    /**
     * Clears all data from the DataTransfer.
     *
     * @param event the drag event
     * @return a Val representing the clearData call
     */
    public static Val clearData(Val event) {
        return new Val(event.js() + ".dataTransfer.clearData()");
    }

    /**
     * Clears specific type data from the DataTransfer.
     *
     * @param event the drag event
     * @param type the MIME type to clear
     * @return a Val representing the clearData call
     */
    public static Val clearData(Val event, String type) {
        return new Val(event.js() + ".dataTransfer.clearData('" + JS.esc(type) + "')");
    }

    // ==================== Drag Event Helpers ====================

    /**
     * Adds a drag event listener to an element.
     *
     * @param elementId the element ID
     * @param eventType one of: "dragstart", "drag", "dragend", "dragenter", "dragover", "dragleave", "drop"
     * @param handler the event handler
     * @return a Val representing the addEventListener call
     */
    public static Val onDragEvent(String elementId, String eventType, Func handler) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').addEventListener('"
            + JS.esc(eventType) + "'," + handler.toExpr() + ")");
    }

    /**
     * Adds a drag event listener to an element expression.
     *
     * @param element the element reference
     * @param eventType the drag event type
     * @param handler the event handler
     * @return a Val representing the addEventListener call
     */
    public static Val onDragEvent(Val element, String eventType, Func handler) {
        return new Val(element.js() + ".addEventListener('" + JS.esc(eventType) + "'," + handler.toExpr() + ")");
    }
}
