package com.laeben.core.event.function;

import com.laeben.core.event.context.EventContext;

@FunctionalInterface
public interface ProgressFunction {
    ProgressFunction EMPTY = (current, total, context) -> {};
    void onProgress(long current, long total, EventContext context);
    default void onProgress(long current, long total){
        onProgress(current, total, null);
    }
    default void onContext(EventContext context){
        onProgress(0, 0, context);
    }
}
