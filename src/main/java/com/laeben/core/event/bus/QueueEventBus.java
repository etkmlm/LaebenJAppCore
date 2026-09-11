package com.laeben.core.event.bus;

import com.laeben.core.concurrency.CancellableToken;
import com.laeben.core.entity.exception.StopException;
import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.register.EventRegister;
import com.laeben.core.event.type.BaseEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class QueueEventBus<T extends EventContext, H extends BaseEvent<T, H>> extends EventBus<T, H> {
    protected final Queue<H> executionQueue;
    private final ExecutorService executor;

    public QueueEventBus(ExecutorService executor){
        super();
        this.executionQueue = new ConcurrentLinkedQueue<>();
        this.executor = executor;
    }

    public QueueEventBus(){
        super();
        this.executionQueue = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
    }

    protected abstract void onEvent(Object clazz, EventRegister<T, H> register, H event, CancellableToken<?> cancellableToken);

    public void executeQueue(CancellableToken<?> cancellableToken){
        final var event = executionQueue.poll();
        if (event == null) return;

        executor.submit(() -> {
            for (final var register : getRegisters().entrySet()){
                if (cancellableToken.shouldStop()) {
                    onExceptionThrown(register.getKey(), register.getValue(), event, new StopException());
                    return;
                }
                onEvent(register.getKey(), register.getValue(), event, cancellableToken);
            }
        });
    }

    public void clear(){
        executionQueue.clear();
    }

    @Override
    public void execute(H event){
        executionQueue.add(event);
    }
}
