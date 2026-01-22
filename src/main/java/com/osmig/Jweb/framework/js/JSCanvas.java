package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Canvas 2D API for drawing graphics, shapes, text, and images.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSCanvas.*;
 *
 * // Get canvas context
 * Val ctx = getContext2D(getElem("myCanvas"));
 *
 * // Draw rectangle
 * fillRect(ctx, 10, 10, 100, 50);
 *
 * // Draw path
 * beginPath(ctx);
 * moveTo(ctx, 50, 50);
 * lineTo(ctx, 150, 100);
 * lineTo(ctx, 50, 150);
 * closePath(ctx);
 * stroke(ctx);
 *
 * // Set styles
 * setFillStyle(ctx, "#FF0000");
 * setStrokeStyle(ctx, "blue");
 * setLineWidth(ctx, 3);
 *
 * // Draw text
 * setFont(ctx, "24px Arial");
 * fillText(ctx, "Hello Canvas", 10, 50);
 * </pre>
 */
public final class JSCanvas {
    private JSCanvas() {}

    // ==================== Context ====================

    /** Gets 2D rendering context: canvas.getContext('2d') */
    public static Val getContext2D(Val canvas) {
        return new Val(canvas.js() + ".getContext('2d')");
    }

    /** Creates OffscreenCanvas: new OffscreenCanvas(width, height) */
    public static Val offscreenCanvas(int width, int height) {
        return new Val("new OffscreenCanvas(" + width + "," + height + ")");
    }

    /** Creates OffscreenCanvas with dynamic dimensions */
    public static Val offscreenCanvas(Val width, Val height) {
        return new Val("new OffscreenCanvas(" + width.js() + "," + height.js() + ")");
    }

    // ==================== Drawing Rectangles ====================

    /** Fills rectangle: ctx.fillRect(x, y, width, height) */
    public static Val fillRect(Val ctx, int x, int y, int width, int height) {
        return new Val(ctx.js() + ".fillRect(" + x + "," + y + "," + width + "," + height + ")");
    }

    /** Fills rectangle with dynamic values */
    public static Val fillRect(Val ctx, Val x, Val y, Val width, Val height) {
        return new Val(ctx.js() + ".fillRect(" + x.js() + "," + y.js() + "," + width.js() + "," + height.js() + ")");
    }

    /** Strokes rectangle outline: ctx.strokeRect(x, y, width, height) */
    public static Val strokeRect(Val ctx, int x, int y, int width, int height) {
        return new Val(ctx.js() + ".strokeRect(" + x + "," + y + "," + width + "," + height + ")");
    }

    /** Strokes rectangle outline with dynamic values */
    public static Val strokeRect(Val ctx, Val x, Val y, Val width, Val height) {
        return new Val(ctx.js() + ".strokeRect(" + x.js() + "," + y.js() + "," + width.js() + "," + height.js() + ")");
    }

    /** Clears rectangle: ctx.clearRect(x, y, width, height) */
    public static Val clearRect(Val ctx, int x, int y, int width, int height) {
        return new Val(ctx.js() + ".clearRect(" + x + "," + y + "," + width + "," + height + ")");
    }

    /** Clears rectangle with dynamic values */
    public static Val clearRect(Val ctx, Val x, Val y, Val width, Val height) {
        return new Val(ctx.js() + ".clearRect(" + x.js() + "," + y.js() + "," + width.js() + "," + height.js() + ")");
    }

    // ==================== Drawing Paths ====================

    /** Begins a new path: ctx.beginPath() */
    public static Val beginPath(Val ctx) {
        return new Val(ctx.js() + ".beginPath()");
    }

    /** Closes current path: ctx.closePath() */
    public static Val closePath(Val ctx) {
        return new Val(ctx.js() + ".closePath()");
    }

    /** Moves to point: ctx.moveTo(x, y) */
    public static Val moveTo(Val ctx, int x, int y) {
        return new Val(ctx.js() + ".moveTo(" + x + "," + y + ")");
    }

    /** Moves to point with dynamic values */
    public static Val moveTo(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".moveTo(" + x.js() + "," + y.js() + ")");
    }

    /** Draws line to point: ctx.lineTo(x, y) */
    public static Val lineTo(Val ctx, int x, int y) {
        return new Val(ctx.js() + ".lineTo(" + x + "," + y + ")");
    }

    /** Draws line to point with dynamic values */
    public static Val lineTo(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".lineTo(" + x.js() + "," + y.js() + ")");
    }

    /** Draws arc: ctx.arc(x, y, radius, startAngle, endAngle, counterclockwise) */
    public static Val arc(Val ctx, int x, int y, int radius, double startAngle, double endAngle) {
        return new Val(ctx.js() + ".arc(" + x + "," + y + "," + radius + "," + startAngle + "," + endAngle + ")");
    }

    /** Draws arc with anticlockwise flag */
    public static Val arc(Val ctx, int x, int y, int radius, double startAngle, double endAngle, boolean anticlockwise) {
        return new Val(ctx.js() + ".arc(" + x + "," + y + "," + radius + "," + startAngle + "," + endAngle + "," + anticlockwise + ")");
    }

    /** Draws arc with dynamic values */
    public static Val arc(Val ctx, Val x, Val y, Val radius, Val startAngle, Val endAngle) {
        return new Val(ctx.js() + ".arc(" + x.js() + "," + y.js() + "," + radius.js() + "," + startAngle.js() + "," + endAngle.js() + ")");
    }

    /** Draws arc with dynamic values and anticlockwise flag */
    public static Val arc(Val ctx, Val x, Val y, Val radius, Val startAngle, Val endAngle, boolean anticlockwise) {
        return new Val(ctx.js() + ".arc(" + x.js() + "," + y.js() + "," + radius.js() + "," + startAngle.js() + "," + endAngle.js() + "," + anticlockwise + ")");
    }

    /** Draws arc between two points: ctx.arcTo(x1, y1, x2, y2, radius) */
    public static Val arcTo(Val ctx, int x1, int y1, int x2, int y2, int radius) {
        return new Val(ctx.js() + ".arcTo(" + x1 + "," + y1 + "," + x2 + "," + y2 + "," + radius + ")");
    }

    /** Draws arc between two points with dynamic values */
    public static Val arcTo(Val ctx, Val x1, Val y1, Val x2, Val y2, Val radius) {
        return new Val(ctx.js() + ".arcTo(" + x1.js() + "," + y1.js() + "," + x2.js() + "," + y2.js() + "," + radius.js() + ")");
    }

    /** Draws cubic Bezier curve: ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) */
    public static Val bezierCurveTo(Val ctx, int cp1x, int cp1y, int cp2x, int cp2y, int x, int y) {
        return new Val(ctx.js() + ".bezierCurveTo(" + cp1x + "," + cp1y + "," + cp2x + "," + cp2y + "," + x + "," + y + ")");
    }

    /** Draws cubic Bezier curve with dynamic values */
    public static Val bezierCurveTo(Val ctx, Val cp1x, Val cp1y, Val cp2x, Val cp2y, Val x, Val y) {
        return new Val(ctx.js() + ".bezierCurveTo(" + cp1x.js() + "," + cp1y.js() + "," + cp2x.js() + "," + cp2y.js() + "," + x.js() + "," + y.js() + ")");
    }

    /** Draws quadratic curve: ctx.quadraticCurveTo(cpx, cpy, x, y) */
    public static Val quadraticCurveTo(Val ctx, int cpx, int cpy, int x, int y) {
        return new Val(ctx.js() + ".quadraticCurveTo(" + cpx + "," + cpy + "," + x + "," + y + ")");
    }

    /** Draws quadratic curve with dynamic values */
    public static Val quadraticCurveTo(Val ctx, Val cpx, Val cpy, Val x, Val y) {
        return new Val(ctx.js() + ".quadraticCurveTo(" + cpx.js() + "," + cpy.js() + "," + x.js() + "," + y.js() + ")");
    }

    /** Adds rectangle to path: ctx.rect(x, y, width, height) */
    public static Val rect(Val ctx, int x, int y, int width, int height) {
        return new Val(ctx.js() + ".rect(" + x + "," + y + "," + width + "," + height + ")");
    }

    /** Adds rectangle to path with dynamic values */
    public static Val rect(Val ctx, Val x, Val y, Val width, Val height) {
        return new Val(ctx.js() + ".rect(" + x.js() + "," + y.js() + "," + width.js() + "," + height.js() + ")");
    }

    /** Adds ellipse to path: ctx.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) */
    public static Val ellipse(Val ctx, int x, int y, int radiusX, int radiusY, double rotation, double startAngle, double endAngle) {
        return new Val(ctx.js() + ".ellipse(" + x + "," + y + "," + radiusX + "," + radiusY + "," + rotation + "," + startAngle + "," + endAngle + ")");
    }

    /** Adds ellipse to path with anticlockwise flag */
    public static Val ellipse(Val ctx, int x, int y, int radiusX, int radiusY, double rotation, double startAngle, double endAngle, boolean anticlockwise) {
        return new Val(ctx.js() + ".ellipse(" + x + "," + y + "," + radiusX + "," + radiusY + "," + rotation + "," + startAngle + "," + endAngle + "," + anticlockwise + ")");
    }

    /** Adds ellipse to path with dynamic values */
    public static Val ellipse(Val ctx, Val x, Val y, Val radiusX, Val radiusY, Val rotation, Val startAngle, Val endAngle) {
        return new Val(ctx.js() + ".ellipse(" + x.js() + "," + y.js() + "," + radiusX.js() + "," + radiusY.js() + "," + rotation.js() + "," + startAngle.js() + "," + endAngle.js() + ")");
    }

    /** Fills current path: ctx.fill() */
    public static Val fill(Val ctx) {
        return new Val(ctx.js() + ".fill()");
    }

    /** Fills current path with fill rule: ctx.fill('nonzero' or 'evenodd') */
    public static Val fill(Val ctx, String fillRule) {
        return new Val(ctx.js() + ".fill('" + JS.esc(fillRule) + "')");
    }

    /** Strokes current path: ctx.stroke() */
    public static Val stroke(Val ctx) {
        return new Val(ctx.js() + ".stroke()");
    }

    /** Clips to current path: ctx.clip() */
    public static Val clip(Val ctx) {
        return new Val(ctx.js() + ".clip()");
    }

    /** Clips with fill rule */
    public static Val clip(Val ctx, String fillRule) {
        return new Val(ctx.js() + ".clip('" + JS.esc(fillRule) + "')");
    }

    /** Checks if point is in path: ctx.isPointInPath(x, y) */
    public static Val isPointInPath(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".isPointInPath(" + x.js() + "," + y.js() + ")");
    }

    /** Checks if point is in stroke: ctx.isPointInStroke(x, y) */
    public static Val isPointInStroke(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".isPointInStroke(" + x.js() + "," + y.js() + ")");
    }

    // ==================== Styles and Colors ====================

    /** Sets fill style: ctx.fillStyle = color */
    public static Val setFillStyle(Val ctx, String color) {
        return new Val(ctx.js() + ".fillStyle='" + JS.esc(color) + "'");
    }

    /** Sets fill style with dynamic value */
    public static Val setFillStyle(Val ctx, Val color) {
        return new Val(ctx.js() + ".fillStyle=" + color.js());
    }

    /** Gets fill style: ctx.fillStyle */
    public static Val getFillStyle(Val ctx) {
        return new Val(ctx.js() + ".fillStyle");
    }

    /** Sets stroke style: ctx.strokeStyle = color */
    public static Val setStrokeStyle(Val ctx, String color) {
        return new Val(ctx.js() + ".strokeStyle='" + JS.esc(color) + "'");
    }

    /** Sets stroke style with dynamic value */
    public static Val setStrokeStyle(Val ctx, Val color) {
        return new Val(ctx.js() + ".strokeStyle=" + color.js());
    }

    /** Gets stroke style: ctx.strokeStyle */
    public static Val getStrokeStyle(Val ctx) {
        return new Val(ctx.js() + ".strokeStyle");
    }

    /** Sets line width: ctx.lineWidth = width */
    public static Val setLineWidth(Val ctx, double width) {
        return new Val(ctx.js() + ".lineWidth=" + width);
    }

    /** Sets line width with dynamic value */
    public static Val setLineWidth(Val ctx, Val width) {
        return new Val(ctx.js() + ".lineWidth=" + width.js());
    }

    /** Gets line width: ctx.lineWidth */
    public static Val getLineWidth(Val ctx) {
        return new Val(ctx.js() + ".lineWidth");
    }

    /** Sets line cap: ctx.lineCap = 'butt' | 'round' | 'square' */
    public static Val setLineCap(Val ctx, String cap) {
        return new Val(ctx.js() + ".lineCap='" + JS.esc(cap) + "'");
    }

    /** Gets line cap: ctx.lineCap */
    public static Val getLineCap(Val ctx) {
        return new Val(ctx.js() + ".lineCap");
    }

    /** Sets line join: ctx.lineJoin = 'miter' | 'round' | 'bevel' */
    public static Val setLineJoin(Val ctx, String join) {
        return new Val(ctx.js() + ".lineJoin='" + JS.esc(join) + "'");
    }

    /** Gets line join: ctx.lineJoin */
    public static Val getLineJoin(Val ctx) {
        return new Val(ctx.js() + ".lineJoin");
    }

    /** Sets miter limit: ctx.miterLimit = limit */
    public static Val setMiterLimit(Val ctx, double limit) {
        return new Val(ctx.js() + ".miterLimit=" + limit);
    }

    /** Sets miter limit with dynamic value */
    public static Val setMiterLimit(Val ctx, Val limit) {
        return new Val(ctx.js() + ".miterLimit=" + limit.js());
    }

    /** Gets miter limit: ctx.miterLimit */
    public static Val getMiterLimit(Val ctx) {
        return new Val(ctx.js() + ".miterLimit");
    }

    /** Sets line dash pattern: ctx.setLineDash([dash1, dash2, ...]) */
    public static Val setLineDash(Val ctx, int... segments) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(segments[i]);
        }
        return new Val(ctx.js() + ".setLineDash(" + sb.append("]").toString() + ")");
    }

    /** Sets line dash pattern with dynamic array */
    public static Val setLineDash(Val ctx, Val segments) {
        return new Val(ctx.js() + ".setLineDash(" + segments.js() + ")");
    }

    /** Gets line dash pattern: ctx.getLineDash() */
    public static Val getLineDash(Val ctx) {
        return new Val(ctx.js() + ".getLineDash()");
    }

    /** Sets line dash offset: ctx.lineDashOffset = offset */
    public static Val setLineDashOffset(Val ctx, double offset) {
        return new Val(ctx.js() + ".lineDashOffset=" + offset);
    }

    /** Sets line dash offset with dynamic value */
    public static Val setLineDashOffset(Val ctx, Val offset) {
        return new Val(ctx.js() + ".lineDashOffset=" + offset.js());
    }

    /** Gets line dash offset: ctx.lineDashOffset */
    public static Val getLineDashOffset(Val ctx) {
        return new Val(ctx.js() + ".lineDashOffset");
    }

    // ==================== Gradients ====================

    /** Creates linear gradient: ctx.createLinearGradient(x0, y0, x1, y1) */
    public static Val createLinearGradient(Val ctx, int x0, int y0, int x1, int y1) {
        return new Val(ctx.js() + ".createLinearGradient(" + x0 + "," + y0 + "," + x1 + "," + y1 + ")");
    }

    /** Creates linear gradient with dynamic values */
    public static Val createLinearGradient(Val ctx, Val x0, Val y0, Val x1, Val y1) {
        return new Val(ctx.js() + ".createLinearGradient(" + x0.js() + "," + y0.js() + "," + x1.js() + "," + y1.js() + ")");
    }

    /** Creates radial gradient: ctx.createRadialGradient(x0, y0, r0, x1, y1, r1) */
    public static Val createRadialGradient(Val ctx, int x0, int y0, int r0, int x1, int y1, int r1) {
        return new Val(ctx.js() + ".createRadialGradient(" + x0 + "," + y0 + "," + r0 + "," + x1 + "," + y1 + "," + r1 + ")");
    }

    /** Creates radial gradient with dynamic values */
    public static Val createRadialGradient(Val ctx, Val x0, Val y0, Val r0, Val x1, Val y1, Val r1) {
        return new Val(ctx.js() + ".createRadialGradient(" + x0.js() + "," + y0.js() + "," + r0.js() + "," + x1.js() + "," + y1.js() + "," + r1.js() + ")");
    }

    /** Creates conic gradient: ctx.createConicGradient(angle, x, y) */
    public static Val createConicGradient(Val ctx, double angle, int x, int y) {
        return new Val(ctx.js() + ".createConicGradient(" + angle + "," + x + "," + y + ")");
    }

    /** Creates conic gradient with dynamic values */
    public static Val createConicGradient(Val ctx, Val angle, Val x, Val y) {
        return new Val(ctx.js() + ".createConicGradient(" + angle.js() + "," + x.js() + "," + y.js() + ")");
    }

    /** Adds color stop to gradient: gradient.addColorStop(offset, color) */
    public static Val addColorStop(Val gradient, double offset, String color) {
        return new Val(gradient.js() + ".addColorStop(" + offset + ",'" + JS.esc(color) + "')");
    }

    /** Adds color stop with dynamic values */
    public static Val addColorStop(Val gradient, Val offset, Val color) {
        return new Val(gradient.js() + ".addColorStop(" + offset.js() + "," + color.js() + ")");
    }

    // ==================== Patterns ====================

    /** Creates pattern: ctx.createPattern(image, repetition) */
    public static Val createPattern(Val ctx, Val image, String repetition) {
        return new Val(ctx.js() + ".createPattern(" + image.js() + ",'" + JS.esc(repetition) + "')");
    }

    /** Creates pattern with 'repeat' */
    public static Val createPatternRepeat(Val ctx, Val image) {
        return new Val(ctx.js() + ".createPattern(" + image.js() + ",'repeat')");
    }

    /** Creates pattern with 'repeat-x' */
    public static Val createPatternRepeatX(Val ctx, Val image) {
        return new Val(ctx.js() + ".createPattern(" + image.js() + ",'repeat-x')");
    }

    /** Creates pattern with 'repeat-y' */
    public static Val createPatternRepeatY(Val ctx, Val image) {
        return new Val(ctx.js() + ".createPattern(" + image.js() + ",'repeat-y')");
    }

    /** Creates pattern with 'no-repeat' */
    public static Val createPatternNoRepeat(Val ctx, Val image) {
        return new Val(ctx.js() + ".createPattern(" + image.js() + ",'no-repeat')");
    }

    // ==================== Shadows ====================

    /** Sets shadow color: ctx.shadowColor = color */
    public static Val setShadowColor(Val ctx, String color) {
        return new Val(ctx.js() + ".shadowColor='" + JS.esc(color) + "'");
    }

    /** Sets shadow color with dynamic value */
    public static Val setShadowColor(Val ctx, Val color) {
        return new Val(ctx.js() + ".shadowColor=" + color.js());
    }

    /** Gets shadow color: ctx.shadowColor */
    public static Val getShadowColor(Val ctx) {
        return new Val(ctx.js() + ".shadowColor");
    }

    /** Sets shadow blur: ctx.shadowBlur = blur */
    public static Val setShadowBlur(Val ctx, double blur) {
        return new Val(ctx.js() + ".shadowBlur=" + blur);
    }

    /** Sets shadow blur with dynamic value */
    public static Val setShadowBlur(Val ctx, Val blur) {
        return new Val(ctx.js() + ".shadowBlur=" + blur.js());
    }

    /** Gets shadow blur: ctx.shadowBlur */
    public static Val getShadowBlur(Val ctx) {
        return new Val(ctx.js() + ".shadowBlur");
    }

    /** Sets shadow offset X: ctx.shadowOffsetX = offset */
    public static Val setShadowOffsetX(Val ctx, double offset) {
        return new Val(ctx.js() + ".shadowOffsetX=" + offset);
    }

    /** Sets shadow offset X with dynamic value */
    public static Val setShadowOffsetX(Val ctx, Val offset) {
        return new Val(ctx.js() + ".shadowOffsetX=" + offset.js());
    }

    /** Gets shadow offset X: ctx.shadowOffsetX */
    public static Val getShadowOffsetX(Val ctx) {
        return new Val(ctx.js() + ".shadowOffsetX");
    }

    /** Sets shadow offset Y: ctx.shadowOffsetY = offset */
    public static Val setShadowOffsetY(Val ctx, double offset) {
        return new Val(ctx.js() + ".shadowOffsetY=" + offset);
    }

    /** Sets shadow offset Y with dynamic value */
    public static Val setShadowOffsetY(Val ctx, Val offset) {
        return new Val(ctx.js() + ".shadowOffsetY=" + offset.js());
    }

    /** Gets shadow offset Y: ctx.shadowOffsetY */
    public static Val getShadowOffsetY(Val ctx) {
        return new Val(ctx.js() + ".shadowOffsetY");
    }

    // ==================== Text ====================

    /** Fills text: ctx.fillText(text, x, y) */
    public static Val fillText(Val ctx, String text, int x, int y) {
        return new Val(ctx.js() + ".fillText('" + JS.esc(text) + "'," + x + "," + y + ")");
    }

    /** Fills text with dynamic values */
    public static Val fillText(Val ctx, Val text, Val x, Val y) {
        return new Val(ctx.js() + ".fillText(" + text.js() + "," + x.js() + "," + y.js() + ")");
    }

    /** Fills text with max width: ctx.fillText(text, x, y, maxWidth) */
    public static Val fillText(Val ctx, String text, int x, int y, int maxWidth) {
        return new Val(ctx.js() + ".fillText('" + JS.esc(text) + "'," + x + "," + y + "," + maxWidth + ")");
    }

    /** Fills text with max width and dynamic values */
    public static Val fillText(Val ctx, Val text, Val x, Val y, Val maxWidth) {
        return new Val(ctx.js() + ".fillText(" + text.js() + "," + x.js() + "," + y.js() + "," + maxWidth.js() + ")");
    }

    /** Strokes text: ctx.strokeText(text, x, y) */
    public static Val strokeText(Val ctx, String text, int x, int y) {
        return new Val(ctx.js() + ".strokeText('" + JS.esc(text) + "'," + x + "," + y + ")");
    }

    /** Strokes text with dynamic values */
    public static Val strokeText(Val ctx, Val text, Val x, Val y) {
        return new Val(ctx.js() + ".strokeText(" + text.js() + "," + x.js() + "," + y.js() + ")");
    }

    /** Strokes text with max width */
    public static Val strokeText(Val ctx, String text, int x, int y, int maxWidth) {
        return new Val(ctx.js() + ".strokeText('" + JS.esc(text) + "'," + x + "," + y + "," + maxWidth + ")");
    }

    /** Strokes text with max width and dynamic values */
    public static Val strokeText(Val ctx, Val text, Val x, Val y, Val maxWidth) {
        return new Val(ctx.js() + ".strokeText(" + text.js() + "," + x.js() + "," + y.js() + "," + maxWidth.js() + ")");
    }

    /** Measures text: ctx.measureText(text) */
    public static Val measureText(Val ctx, String text) {
        return new Val(ctx.js() + ".measureText('" + JS.esc(text) + "')");
    }

    /** Measures text with dynamic value */
    public static Val measureText(Val ctx, Val text) {
        return new Val(ctx.js() + ".measureText(" + text.js() + ")");
    }

    /** Gets text metrics width: metrics.width */
    public static Val textWidth(Val metrics) {
        return new Val(metrics.js() + ".width");
    }

    /** Sets font: ctx.font = font */
    public static Val setFont(Val ctx, String font) {
        return new Val(ctx.js() + ".font='" + JS.esc(font) + "'");
    }

    /** Sets font with dynamic value */
    public static Val setFont(Val ctx, Val font) {
        return new Val(ctx.js() + ".font=" + font.js());
    }

    /** Gets font: ctx.font */
    public static Val getFont(Val ctx) {
        return new Val(ctx.js() + ".font");
    }

    /** Sets text align: ctx.textAlign = 'left' | 'right' | 'center' | 'start' | 'end' */
    public static Val setTextAlign(Val ctx, String align) {
        return new Val(ctx.js() + ".textAlign='" + JS.esc(align) + "'");
    }

    /** Gets text align: ctx.textAlign */
    public static Val getTextAlign(Val ctx) {
        return new Val(ctx.js() + ".textAlign");
    }

    /** Sets text baseline: ctx.textBaseline = 'top' | 'middle' | 'bottom' | 'alphabetic' | 'hanging' */
    public static Val setTextBaseline(Val ctx, String baseline) {
        return new Val(ctx.js() + ".textBaseline='" + JS.esc(baseline) + "'");
    }

    /** Gets text baseline: ctx.textBaseline */
    public static Val getTextBaseline(Val ctx) {
        return new Val(ctx.js() + ".textBaseline");
    }

    /** Sets text direction: ctx.direction = 'ltr' | 'rtl' | 'inherit' */
    public static Val setDirection(Val ctx, String direction) {
        return new Val(ctx.js() + ".direction='" + JS.esc(direction) + "'");
    }

    /** Gets text direction: ctx.direction */
    public static Val getDirection(Val ctx) {
        return new Val(ctx.js() + ".direction");
    }

    // ==================== Images ====================

    /** Draws image: ctx.drawImage(image, dx, dy) */
    public static Val drawImage(Val ctx, Val image, int dx, int dy) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + dx + "," + dy + ")");
    }

    /** Draws image with dynamic values */
    public static Val drawImage(Val ctx, Val image, Val dx, Val dy) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + dx.js() + "," + dy.js() + ")");
    }

    /** Draws scaled image: ctx.drawImage(image, dx, dy, dWidth, dHeight) */
    public static Val drawImage(Val ctx, Val image, int dx, int dy, int dWidth, int dHeight) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + dx + "," + dy + "," + dWidth + "," + dHeight + ")");
    }

    /** Draws scaled image with dynamic values */
    public static Val drawImage(Val ctx, Val image, Val dx, Val dy, Val dWidth, Val dHeight) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + dx.js() + "," + dy.js() + "," + dWidth.js() + "," + dHeight.js() + ")");
    }

    /** Draws image slice: ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight) */
    public static Val drawImage(Val ctx, Val image, int sx, int sy, int sWidth, int sHeight, int dx, int dy, int dWidth, int dHeight) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + sx + "," + sy + "," + sWidth + "," + sHeight + "," + dx + "," + dy + "," + dWidth + "," + dHeight + ")");
    }

    /** Draws image slice with dynamic values */
    public static Val drawImage(Val ctx, Val image, Val sx, Val sy, Val sWidth, Val sHeight, Val dx, Val dy, Val dWidth, Val dHeight) {
        return new Val(ctx.js() + ".drawImage(" + image.js() + "," + sx.js() + "," + sy.js() + "," + sWidth.js() + "," + sHeight.js() + "," + dx.js() + "," + dy.js() + "," + dWidth.js() + "," + dHeight.js() + ")");
    }

    /** Creates ImageData: ctx.createImageData(width, height) */
    public static Val createImageData(Val ctx, int width, int height) {
        return new Val(ctx.js() + ".createImageData(" + width + "," + height + ")");
    }

    /** Creates ImageData with dynamic values */
    public static Val createImageData(Val ctx, Val width, Val height) {
        return new Val(ctx.js() + ".createImageData(" + width.js() + "," + height.js() + ")");
    }

    /** Creates ImageData from existing: ctx.createImageData(imageData) */
    public static Val createImageData(Val ctx, Val imageData) {
        return new Val(ctx.js() + ".createImageData(" + imageData.js() + ")");
    }

    /** Gets ImageData: ctx.getImageData(sx, sy, sw, sh) */
    public static Val getImageData(Val ctx, int sx, int sy, int sw, int sh) {
        return new Val(ctx.js() + ".getImageData(" + sx + "," + sy + "," + sw + "," + sh + ")");
    }

    /** Gets ImageData with dynamic values */
    public static Val getImageData(Val ctx, Val sx, Val sy, Val sw, Val sh) {
        return new Val(ctx.js() + ".getImageData(" + sx.js() + "," + sy.js() + "," + sw.js() + "," + sh.js() + ")");
    }

    /** Puts ImageData: ctx.putImageData(imageData, dx, dy) */
    public static Val putImageData(Val ctx, Val imageData, int dx, int dy) {
        return new Val(ctx.js() + ".putImageData(" + imageData.js() + "," + dx + "," + dy + ")");
    }

    /** Puts ImageData with dynamic values */
    public static Val putImageData(Val ctx, Val imageData, Val dx, Val dy) {
        return new Val(ctx.js() + ".putImageData(" + imageData.js() + "," + dx.js() + "," + dy.js() + ")");
    }

    /** Puts ImageData with dirty rect: ctx.putImageData(imageData, dx, dy, dirtyX, dirtyY, dirtyWidth, dirtyHeight) */
    public static Val putImageData(Val ctx, Val imageData, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {
        return new Val(ctx.js() + ".putImageData(" + imageData.js() + "," + dx + "," + dy + "," + dirtyX + "," + dirtyY + "," + dirtyWidth + "," + dirtyHeight + ")");
    }

    /** Puts ImageData with dirty rect and dynamic values */
    public static Val putImageData(Val ctx, Val imageData, Val dx, Val dy, Val dirtyX, Val dirtyY, Val dirtyWidth, Val dirtyHeight) {
        return new Val(ctx.js() + ".putImageData(" + imageData.js() + "," + dx.js() + "," + dy.js() + "," + dirtyX.js() + "," + dirtyY.js() + "," + dirtyWidth.js() + "," + dirtyHeight.js() + ")");
    }

    /** Gets ImageData data array: imageData.data */
    public static Val imageDataArray(Val imageData) {
        return new Val(imageData.js() + ".data");
    }

    /** Gets ImageData width: imageData.width */
    public static Val imageDataWidth(Val imageData) {
        return new Val(imageData.js() + ".width");
    }

    /** Gets ImageData height: imageData.height */
    public static Val imageDataHeight(Val imageData) {
        return new Val(imageData.js() + ".height");
    }

    // ==================== Transformations ====================

    /** Saves context state: ctx.save() */
    public static Val save(Val ctx) {
        return new Val(ctx.js() + ".save()");
    }

    /** Restores context state: ctx.restore() */
    public static Val restore(Val ctx) {
        return new Val(ctx.js() + ".restore()");
    }

    /** Scales: ctx.scale(x, y) */
    public static Val scale(Val ctx, double x, double y) {
        return new Val(ctx.js() + ".scale(" + x + "," + y + ")");
    }

    /** Scales with dynamic values */
    public static Val scale(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".scale(" + x.js() + "," + y.js() + ")");
    }

    /** Rotates: ctx.rotate(angle) */
    public static Val rotate(Val ctx, double angle) {
        return new Val(ctx.js() + ".rotate(" + angle + ")");
    }

    /** Rotates with dynamic value */
    public static Val rotate(Val ctx, Val angle) {
        return new Val(ctx.js() + ".rotate(" + angle.js() + ")");
    }

    /** Translates: ctx.translate(x, y) */
    public static Val translate(Val ctx, double x, double y) {
        return new Val(ctx.js() + ".translate(" + x + "," + y + ")");
    }

    /** Translates with dynamic values */
    public static Val translate(Val ctx, Val x, Val y) {
        return new Val(ctx.js() + ".translate(" + x.js() + "," + y.js() + ")");
    }

    /** Transforms: ctx.transform(a, b, c, d, e, f) */
    public static Val transform(Val ctx, double a, double b, double c, double d, double e, double f) {
        return new Val(ctx.js() + ".transform(" + a + "," + b + "," + c + "," + d + "," + e + "," + f + ")");
    }

    /** Transforms with dynamic values */
    public static Val transform(Val ctx, Val a, Val b, Val c, Val d, Val e, Val f) {
        return new Val(ctx.js() + ".transform(" + a.js() + "," + b.js() + "," + c.js() + "," + d.js() + "," + e.js() + "," + f.js() + ")");
    }

    /** Sets transform: ctx.setTransform(a, b, c, d, e, f) */
    public static Val setTransform(Val ctx, double a, double b, double c, double d, double e, double f) {
        return new Val(ctx.js() + ".setTransform(" + a + "," + b + "," + c + "," + d + "," + e + "," + f + ")");
    }

    /** Sets transform with dynamic values */
    public static Val setTransform(Val ctx, Val a, Val b, Val c, Val d, Val e, Val f) {
        return new Val(ctx.js() + ".setTransform(" + a.js() + "," + b.js() + "," + c.js() + "," + d.js() + "," + e.js() + "," + f.js() + ")");
    }

    /** Resets transform: ctx.resetTransform() */
    public static Val resetTransform(Val ctx) {
        return new Val(ctx.js() + ".resetTransform()");
    }

    /** Gets transform: ctx.getTransform() */
    public static Val getTransform(Val ctx) {
        return new Val(ctx.js() + ".getTransform()");
    }

    // ==================== Compositing ====================

    /** Sets global alpha: ctx.globalAlpha = alpha */
    public static Val setGlobalAlpha(Val ctx, double alpha) {
        return new Val(ctx.js() + ".globalAlpha=" + alpha);
    }

    /** Sets global alpha with dynamic value */
    public static Val setGlobalAlpha(Val ctx, Val alpha) {
        return new Val(ctx.js() + ".globalAlpha=" + alpha.js());
    }

    /** Gets global alpha: ctx.globalAlpha */
    public static Val getGlobalAlpha(Val ctx) {
        return new Val(ctx.js() + ".globalAlpha");
    }

    /** Sets global composite operation: ctx.globalCompositeOperation = operation */
    public static Val setGlobalCompositeOperation(Val ctx, String operation) {
        return new Val(ctx.js() + ".globalCompositeOperation='" + JS.esc(operation) + "'");
    }

    /** Gets global composite operation: ctx.globalCompositeOperation */
    public static Val getGlobalCompositeOperation(Val ctx) {
        return new Val(ctx.js() + ".globalCompositeOperation");
    }

    // ==================== Canvas Properties ====================

    /** Gets canvas width: canvas.width */
    public static Val canvasWidth(Val canvas) {
        return new Val(canvas.js() + ".width");
    }

    /** Sets canvas width: canvas.width = width */
    public static Val setCanvasWidth(Val canvas, int width) {
        return new Val(canvas.js() + ".width=" + width);
    }

    /** Sets canvas width with dynamic value */
    public static Val setCanvasWidth(Val canvas, Val width) {
        return new Val(canvas.js() + ".width=" + width.js());
    }

    /** Gets canvas height: canvas.height */
    public static Val canvasHeight(Val canvas) {
        return new Val(canvas.js() + ".height");
    }

    /** Sets canvas height: canvas.height = height */
    public static Val setCanvasHeight(Val canvas, int height) {
        return new Val(canvas.js() + ".height=" + height);
    }

    /** Sets canvas height with dynamic value */
    public static Val setCanvasHeight(Val canvas, Val height) {
        return new Val(canvas.js() + ".height=" + height.js());
    }

    /** Converts canvas to data URL: canvas.toDataURL() */
    public static Val toDataURL(Val canvas) {
        return new Val(canvas.js() + ".toDataURL()");
    }

    /** Converts canvas to data URL with type: canvas.toDataURL(type) */
    public static Val toDataURL(Val canvas, String type) {
        return new Val(canvas.js() + ".toDataURL('" + JS.esc(type) + "')");
    }

    /** Converts canvas to data URL with type and quality: canvas.toDataURL(type, quality) */
    public static Val toDataURL(Val canvas, String type, double quality) {
        return new Val(canvas.js() + ".toDataURL('" + JS.esc(type) + "'," + quality + ")");
    }

    /** Converts canvas to blob: canvas.toBlob(callback) */
    public static Val toBlob(Val canvas, Func callback) {
        return new Val(canvas.js() + ".toBlob(" + callback.toExpr() + ")");
    }

    /** Converts canvas to blob with type */
    public static Val toBlob(Val canvas, Func callback, String type) {
        return new Val(canvas.js() + ".toBlob(" + callback.toExpr() + ",'" + JS.esc(type) + "')");
    }

    /** Converts canvas to blob with type and quality */
    public static Val toBlob(Val canvas, Func callback, String type, double quality) {
        return new Val(canvas.js() + ".toBlob(" + callback.toExpr() + ",'" + JS.esc(type) + "'," + quality + ")");
    }

    // ==================== Smoothing ====================

    /** Sets image smoothing enabled: ctx.imageSmoothingEnabled = enabled */
    public static Val setImageSmoothingEnabled(Val ctx, boolean enabled) {
        return new Val(ctx.js() + ".imageSmoothingEnabled=" + enabled);
    }

    /** Gets image smoothing enabled: ctx.imageSmoothingEnabled */
    public static Val getImageSmoothingEnabled(Val ctx) {
        return new Val(ctx.js() + ".imageSmoothingEnabled");
    }

    /** Sets image smoothing quality: ctx.imageSmoothingQuality = 'low' | 'medium' | 'high' */
    public static Val setImageSmoothingQuality(Val ctx, String quality) {
        return new Val(ctx.js() + ".imageSmoothingQuality='" + JS.esc(quality) + "'");
    }

    /** Gets image smoothing quality: ctx.imageSmoothingQuality */
    public static Val getImageSmoothingQuality(Val ctx) {
        return new Val(ctx.js() + ".imageSmoothingQuality");
    }

    // ==================== Filter ====================

    /** Sets filter: ctx.filter = filter */
    public static Val setFilter(Val ctx, String filter) {
        return new Val(ctx.js() + ".filter='" + JS.esc(filter) + "'");
    }

    /** Sets filter with dynamic value */
    public static Val setFilter(Val ctx, Val filter) {
        return new Val(ctx.js() + ".filter=" + filter.js());
    }

    /** Gets filter: ctx.filter */
    public static Val getFilter(Val ctx) {
        return new Val(ctx.js() + ".filter");
    }
}
