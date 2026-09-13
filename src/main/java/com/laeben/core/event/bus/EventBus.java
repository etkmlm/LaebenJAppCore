package com.laeben.core.event.bus;

import com.laeben.core.concurrency.CancellableToken;
import com.laeben.core.entity.exception.StopException;
import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.register.EventRegister;
import com.laeben.core.event.type.BaseEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class EventBus<T extends EventContext, H extends BaseEvent<T, H>> {
    private final Map<Object, EventRegister<T, H>> registers;

    public EventBus(){
        registers = new HashMap<>();
    }

    /**
     * Add an event handler.
     * @param clazz key class
     * @param handler the handler
     * @param async should execute async
     */
    public void addHandler(Object clazz, EventRegister.Handler<T, H> handler, boolean async){
        registers.put(clazz, new EventRegister<>(handler, async));
    }

    /**
     * Remove an event handler.
     * @param clazz key class
     */
    public void removeHandler(Object clazz){
        registers.remove(clazz);
    }

    protected Map<Object, EventRegister<T, H>> getRegisters(){
        return registers;
    }


    /**
     * Handles the event.
     * @param clazz key class
     * @param register target register
     * @param event target event
     * @param token cancellation token
     * @return should the handling process continue
     */
    protected boolean handle(Object clazz, EventRegister<T, H> register, H event, CancellableToken<?> token){
        try {
            if (token != null && token.shouldStop())
                throw new StopException();

            register.getHandler().handle(event);
        }
        catch (Throwable e) {
            onExceptionThrown(clazz, register, event, e);

            if (e instanceof StopException) return false;
        }

        return true;
    }
    protected abstract void onExceptionThrown(Object clazz, EventRegister<T, H> register, H event, Throwable exception);
    public abstract void execute(H event);
}
