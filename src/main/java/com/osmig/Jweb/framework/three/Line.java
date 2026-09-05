package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.List;
import java.util.Map;

/**
 * A thin, unlit polyline through x,y,z points (three.js {@code Line}) —
 * outlines, guides, constellations, laser traces, the pencil line of a
 * diagram. Built with {@code wire(...)} ({@code wire}, because SVG owns
 * {@code line} under the dual wildcard imports):
 *
 * <pre>{@code
 * wire(-2, 0, 0,  0, 1.4, 0,  2, 0, 0).color("#fde68a")
 * wire(...).closed().dashed(0.2, 0.1)          // a dashed loop
 * wire(...).draw(1200)                         // draws itself on load
 * }</pre>
 *
 * <p>WebGL lines are always one pixel wide, whatever the zoom — for a line
 * with thickness use {@code tube(radius, ...)}. Lines are visual only: they
 * take no clicks or hover effects, and cast no shadows.</p>
 */
public class Line extends ThreeNode<Line> {

    private final double[] points;
    private String color;
    private Double opacity;
    private boolean closed;
    private double[] dash;
    private int[] draw;

    Line(double[] points) {
        if (points.length < 6 || points.length % 3 != 0) {
            throw new IllegalArgumentException(
                "wire() needs x,y,z triples for at least 2 points — got "
                + points.length + " values");
        }
        this.points = points;
    }

    /** Line color — any CSS color string. Default: white. */
    public Line color(String color) {
        this.color = color;
        return this;
    }

    /** Line color from a typed CSS value. */
    public Line color(CSSValue color) {
        return color(color.css());
    }

    /** Opacity 0–1. Default 1. */
    public Line opacity(double opacity) {
        this.opacity = opacity;
        return this;
    }

    /** Joins the last point back to the first. */
    public Line closed() {
        this.closed = true;
        return this;
    }

    /** Dashes the line: dash and gap lengths in scene units. */
    public Line dashed(double dash, double gap) {
        if (dash <= 0 || gap < 0) {
            throw new IllegalArgumentException(
                "dashed(dash, gap): dash must be positive and gap non-negative — got "
                + dash + ", " + gap);
        }
        this.dash = new double[]{dash, gap};
        return this;
    }

    /**
     * Draws the line from nothing over the given milliseconds when the scene
     * loads (or scrolls into view) — a pencil tracing the outline. Combine
     * with {@link #dashed} and the dashes settle in once the trace is done.
     */
    public Line draw(int millis) {
        return draw(millis, 0);
    }

    /** Draws the line over {@code millis}, starting after {@code delayMillis}. */
    public Line draw(int millis, int delayMillis) {
        if (millis <= 0 || delayMillis < 0) {
            throw new IllegalArgumentException(
                "draw(ms, delay): ms must be positive and delay non-negative — got "
                + millis + ", " + delayMillis);
        }
        this.draw = new int[]{millis, delayMillis};
        return this;
    }

    @Override
    protected String type() {
        return "line";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("pts", vec(points));
        if (color != null) map.put("color", color);
        if (opacity != null) map.put("opacity", num(opacity));
        if (closed) map.put("closed", true);
        if (dash != null) map.put("dash", vec(dash));
        if (draw != null) map.put("draw", List.of(draw[0], draw[1]));
    }
}
