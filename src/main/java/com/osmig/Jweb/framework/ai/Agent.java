package com.osmig.Jweb.framework.ai;

/**
 * The pre-3.0 spelling of an agent loop.
 *
 * @deprecated Replaced by {@link jweb.Agent} — {@code AI.agent()} hands out
 *             that type, so declare {@code jweb.Agent}. This subclass only
 *             keeps old subclasses compiling.
 */
@Deprecated
public class Agent extends jweb.Agent {

    protected Agent(AiConfig config) {
        super(config);
    }
}
