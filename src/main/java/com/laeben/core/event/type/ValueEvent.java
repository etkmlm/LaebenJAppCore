package com.laeben.core.event.type;

import com.laeben.core.event.context.EventContext;

/**
 * Handler compatible event to be used in distributing a data.
 */
public class ValueEvent<T extends EventContext> extends BaseEvent<T, ValueEvent<T>> {
    private final Object value;

    public ValueEvent(T context, Object value) {
        super(context);

        this.value = value;
    }

    /**
     * Unchecked casting over the value.
     * @param <G> value type
     */
    @SuppressWarnings("unchecked")
    public <G> G getValue(){
        return (G)value;
    }
}
