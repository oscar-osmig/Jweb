package com.osmig.Jweb.framework.ai;

/**
 * The pre-3.0 spelling of a conversation.
 *
 * @deprecated Replaced by {@link jweb.Chat} — {@code AI.chat()} hands out that
 *             type, so declare {@code jweb.Chat}. This subclass only keeps old
 *             subclasses compiling.
 */
@Deprecated
public class Chat extends jweb.Chat {

    protected Chat(AiConfig config) {
        super(config);
    }
}
