package com.laeben.core.event.type;

import com.laeben.core.event.context.EventContext;

public class SimpleEvent extends BaseEvent<EventContext, SimpleEvent> {
    public SimpleEvent(EventContext context) {
        super(context);
    }
}
