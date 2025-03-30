package com.laeben.core.util.events;

/**
 * Event with the key.
 * <br/>
 * Includes the source, the key.
 */
public class KeyEvent extends BaseEvent {
    private final String key;
    public KeyEvent(String key) {
        this.key = key;
    }

    public String getKey(){
        return key;
    }
}
