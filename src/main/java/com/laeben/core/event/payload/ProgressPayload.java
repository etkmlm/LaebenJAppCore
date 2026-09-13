package com.laeben.core.event.payload;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.function.ProgressFunction;

public class ProgressPayload {
    public static final ProgressPayload EMPTY = new ProgressPayload(null, null, 0);

    private final ProgressFunction onProgress;
    private final EventContext context;
    private final long length;

    private ProgressPayload(ProgressFunction onProgress, EventContext context, long length) {
        this.onProgress = onProgress;
        this.context = context;
        this.length = length;
    }

    public static ProgressPayload create(ProgressFunction onProgress, EventContext context, long length) {
        return onProgress == null ? EMPTY : new ProgressPayload(onProgress, context, length);
    }

    public void onProgress(long current){
        if (this.onProgress == null) return;

        this.onProgress.onProgress(current, length, context);
    }
}
