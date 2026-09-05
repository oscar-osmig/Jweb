package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.js.Actions;
import com.osmig.Jweb.framework.js.ClientActions;

import java.util.Map;
import java.util.function.Consumer;

/**
 * An invisible floor region that reacts when the camera walks into or out
 * of it — the doorway that leads home, the threshold that opens a world,
 * the spot where a caption should appear:
 *
 * <pre>{@code
 * zone(-1.6, 14.9, 1.6, 16).name("back-door").link("/")
 * zone(-3, -3, 3, 3).name("plinth").onEnter(e -> tour.set("compass"))
 * }</pre>
 *
 * <p>Edges are in scene units on the ground: {@code x} from {@code minX}
 * to {@code maxX}, {@code z} from {@code minZ} to {@code maxZ}, any
 * height. While the camera is inside, the scene element and {@code <body>}
 * carry {@code three-near-<name>}, and a bubbling {@code jweb:three-zone}
 * event fires on entering and leaving ({@code detail.inside}). The camera
 * being inside a zone when the scene first renders does not count as
 * entering — a {@link #link} never fires on load.</p>
 */
public class Zone extends ThreeNode<Zone> {

    private final double[] box;
    private String enterHandlerId;
    private String enterActionId;
    private String leaveHandlerId;
    private String leaveActionId;

    Zone(double minX, double minZ, double maxX, double maxZ) {
        this.box = new double[]{Math.min(minX, maxX), Math.min(minZ, maxZ),
                                Math.max(minX, maxX), Math.max(minZ, maxZ)};
    }

    /** Runs a server handler when the camera enters; {@code event.value()} is the zone's name. */
    public Zone onEnter(Consumer<Event> handler) {
        this.enterHandlerId = EventRegistry.register("enter", handler).getId();
        return this;
    }

    /** Runs a client Actions handler when the camera enters. */
    public Zone onEnter(Actions.Action action) {
        this.enterActionId = ClientActions.register(action.inline());
        return this;
    }

    /** Runs a server handler when the camera leaves. */
    public Zone onLeave(Consumer<Event> handler) {
        this.leaveHandlerId = EventRegistry.register("leave", handler).getId();
        return this;
    }

    /** Runs a client Actions handler when the camera leaves. */
    public Zone onLeave(Actions.Action action) {
        this.leaveActionId = ClientActions.register(action.inline());
        return this;
    }

    @Override
    protected String type() {
        return "zone";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("box", vec(box));
        if (enterHandlerId != null) map.put("enterH", enterHandlerId);
        if (enterActionId != null) map.put("enterAct", enterActionId);
        if (leaveHandlerId != null) map.put("leaveH", leaveHandlerId);
        if (leaveActionId != null) map.put("leaveAct", leaveActionId);
    }
}
