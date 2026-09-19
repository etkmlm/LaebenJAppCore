package com.laeben.core.event.type;

import com.laeben.core.event.context.EventContext;

/**
 * Handler compatible event to be used in difference trackings.
 */
public class ChangeEvent<T extends EventContext> extends BaseEvent<T, ChangeEvent<T>>{
    private final Object oldValue;
    private final Object newValue;

    public ChangeEvent(T context, Object oldValue, Object newValue) {
        super(context);

        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Unchecked casting over the old value.
     * @param <G> value type
     */
    @SuppressWarnings("unchecked")
    public <G> G getOldValue(){
        return (G)oldValue;
    }

    /**
     * Unchecked casting over the new value.
     * @param <G> value type
     */
    @SuppressWarnings("unchecked")
    public <G> G getNewValue(){
        return (G)newValue;
    }
}
