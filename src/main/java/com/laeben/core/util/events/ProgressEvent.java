package com.laeben.core.util.events;

/**
 * Progress event.
 * <br/>
 * Includes the source, the key, current, and the total value.
 */
public class ProgressEvent extends KeyEvent {
    private final long current;
    private final long total;

    public ProgressEvent(String key, long current, long total) {
        super(key);
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
