package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A sound in the scene — ambience that loops, a chime that plays once, or,
 * given a position, a sound that lives somewhere and swells as the camera
 * approaches:
 *
 * <pre>{@code
 * sound("/audio/sea.mp3").loop().volume(0.4)                        // everywhere
 * sound("/audio/fountain.mp3").loop().position(4, 1, -6).range(3)   // from the fountain
 * sound("/audio/bell.mp3").name("bell").paused()                    // played by a patch
 * }</pre>
 *
 * <p>Browsers only start audio inside a user gesture, so every sound waits
 * for the visitor's first click, tap or key press and begins then. A sound
 * with a {@code position} is positional (three.js {@code PositionalAudio});
 * {@link #range} is the distance at which it plays at full volume. Live:
 * {@code Three.patch(id).node(name).volume(v).tween(ms)}, {@code .play()}
 * and {@code .stop()}.</p>
 */
public class Sound extends ThreeNode<Sound> {

    private final String url;
    private boolean loop;
    private Double volume;
    private Double range;
    private boolean paused;

    Sound(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("sound() needs a URL");
        this.url = url;
    }

    /** Plays continuously — ambience, a hum, the sea. */
    public Sound loop() {
        this.loop = true;
        return this;
    }

    /** Volume 0–1. Default 1. */
    public Sound volume(double volume) {
        this.volume = volume;
        return this;
    }

    /**
     * For a positioned sound: the distance (scene units) at which it plays
     * at full volume, fading beyond. Default 4.
     */
    public Sound range(double distance) {
        this.range = distance;
        return this;
    }

    /** Does not start on its own — a patch's {@code .play()} starts it. */
    public Sound paused() {
        this.paused = true;
        return this;
    }

    @Override
    protected String type() {
        return "sound";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("url", url);
        if (loop) map.put("loop", true);
        if (volume != null) map.put("vol", num(volume));
        if (range != null) map.put("ref", num(range));
        if (paused) map.put("paused", true);
    }
}
