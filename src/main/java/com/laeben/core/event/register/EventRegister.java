package com.laeben.core.event.register;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.type.BaseEvent;

public class EventRegister<T extends EventContext, H extends BaseEvent<T, H>> {
    @FunctionalInterface
    public interface Handler<T extends EventContext, H extends BaseEvent<T, H>>{
        void handle(H event) throws Throwable;
    }

    private final int flags;
    private final Handler<T, H> handler;

    public EventRegister(Handler<T, H> handler, int flags){
        this.flags = flags;
        this.handler = handler;
    }

    public Handler<T, H> getHandler(){
        return handler;
    }

    public int flags(){
        return flags;
    }
    public boolean checkFlag(int flags){
        return (this.flags & flags) == flags;
    }
}
