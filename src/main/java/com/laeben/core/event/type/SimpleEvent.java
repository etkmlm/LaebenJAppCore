package com.laeben.core.event.type;

import com.laeben.core.event.context.EventContext;

public class SimpleEvent<T extends EventContext> extends BaseEvent<T, SimpleEvent<T>> {
    public SimpleEvent(T context) {
        super(context);
    }
}
