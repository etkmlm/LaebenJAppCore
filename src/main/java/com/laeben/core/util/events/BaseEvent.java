package com.laeben.core.util.events;

/**
 * Base event.
 * <br/>
 * Includes the source.
 */
public class BaseEvent {
    private Object source;
    public BaseEvent() {

    }

    public Object getSource(){
        return source;
    }

    public BaseEvent setSource(Object source){
        this.source = source;

        return this;
    }
}
