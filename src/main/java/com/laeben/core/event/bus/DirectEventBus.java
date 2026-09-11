package com.laeben.core.event.bus;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.type.BaseEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DirectEventBus<T extends EventContext, H extends BaseEvent<T, H>> extends EventBus<T, H> {
    private final ExecutorService executor;

    public DirectEventBus(ExecutorService executor) {
        this.executor = executor;
    }
    public DirectEventBus() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(H event){
        executor.submit(() -> {
            for (final var register : getRegisters().entrySet()){
                try{
                    register.getValue().getHandler().handle(event);
                } catch (Throwable e) {
                    onExceptionThrown(register.getKey(), register.getValue(), event, e);
                }
            }
        });
    }
}
