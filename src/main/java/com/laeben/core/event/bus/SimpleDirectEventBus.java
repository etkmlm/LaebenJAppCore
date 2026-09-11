package com.laeben.core.event.bus;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.register.EventRegister;
import com.laeben.core.event.type.BaseEvent;

@SuppressWarnings({"CallToPrintStackTrace"})
public class SimpleDirectEventBus<T extends EventContext, H extends BaseEvent<T, H>> extends DirectEventBus<T, H> {
    @Override
    protected void onExceptionThrown(Object clazz, EventRegister<T, H> register, H event, Throwable exception) {
        System.out.printf("Exception thrown on a class (%s) for event '%s' [isAsync: %b]", clazz.getClass().getName() + "@" + clazz.hashCode(), event.getContext().toString(), register.isAsync());
        exception.printStackTrace();
    }
}
