package com.laeben.core.event.type;

import com.laeben.core.event.context.EventContext;

@SuppressWarnings("unchecked")
public class BaseEvent<T extends EventContext, H extends BaseEvent<T, H>> {
    private final T context;
    private Object source;

    public BaseEvent(T context) {
        this.context = context;
    }

    public T getContext(){
        return context;
    }

    /**
     * Unchecked casting over the source.
     * @return cast source
     * @param <G> source type
     */
    public <G> G getSource(){
        return (G)source;
    }

    public H withSource(Object source){
        this.source = source;
        return (H) this;
    }
}
