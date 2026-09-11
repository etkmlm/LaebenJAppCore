package com.laeben.core.event.type;

import com.laeben.core.event.context.ProgressContext;

/**
 * Handler compatible event to be used in moderate progressions.
 * <br/>
 * Prefer {@link com.laeben.core.event.function.ProgressFunction} instead for high-performance
 * events.
 */
public class ProgressEvent extends BaseEvent<ProgressContext, ProgressEvent> {
    private final long current;
    private final long total;

    public ProgressEvent(ProgressContext context, long current, long total) {
        super(context);

        this.current = current;
        this.total = total;
    }

    public long getCurrent(){
        return current;
    }

    public long getTotal(){
        return total;
    }

    public double getProgressFloored(){
        return Math.floor(current * 1.0 / total * 100) / 100;
    }
    public double getProgress(){
        return current * 1.0 / total;
    }
    public double getProgressPercent(){
        return getProgress() * 100;
    }
}
