package com.laeben.core.event.bus;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.type.BaseEvent;

/**
 * Synchronous event bus.
 */
public abstract class DirectEventBus<T extends EventContext, H extends BaseEvent<T, H>> extends EventBus<T, H> {
    @Override
    public void execute(H event){
        for (final var register : getRegisters().entrySet()){
            if (!handle(register.getKey(), register.getValue(), event, null))
                break;
        }
    }
}
