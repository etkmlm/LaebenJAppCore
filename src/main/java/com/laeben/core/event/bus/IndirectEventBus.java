package com.laeben.core.event.bus;

import com.laeben.core.concurrency.CancellableToken;
import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.register.EventRegister;
import com.laeben.core.event.type.BaseEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous event bus.
 */
public abstract class IndirectEventBus<T extends EventContext, H extends BaseEvent<T, H>> extends EventBus<T, H> {
    private final ExecutorService executor;
    private CancellableToken<?> cancellableToken;

    public IndirectEventBus(ExecutorService executor){
        super();
        this.executor = executor;
    }

    public IndirectEventBus(){
        super();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void setCancellableToken(CancellableToken<?> cancellableToken){
        this.cancellableToken = cancellableToken;
    }

    protected boolean onEvent(Object clazz, EventRegister<T, H> register, H event, CancellableToken<?> cancellableToken){
        if (register.isAsync()) executor.submit(() -> handle(clazz, register, event, cancellableToken));
        else return handle(clazz, register, event, cancellableToken);

        return true;
    }

    @Override
    public void execute(H event){
        for (final var register : getRegisters().entrySet()){
            // token checked twice, async executions cannot be tracked
            if (cancellableToken != null && cancellableToken.shouldStop() || !onEvent(register.getKey(), register.getValue(), event, cancellableToken))
                break;
        }
    }
}
