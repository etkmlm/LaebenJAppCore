package com.laeben.core.util.events;

/**
 * Event with the value.
 * <br/>
 * Includes the source, the key, and the value.
 */
public class ValueEvent extends KeyEvent{
    private final Object value;
    public ValueEvent(String key, Object value) {
        super(key);

        this.value = value;
    }

    public Object getValue(){
        return value;
    }
}
