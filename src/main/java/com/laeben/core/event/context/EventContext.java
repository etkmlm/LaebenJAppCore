package com.laeben.core.event.context;

import java.util.Objects;

public class EventContext {
    public static final EventContext DEFAULT = new EventContext(null);

    private final String label;
    private final EventContext subContext;

    public EventContext(String label) {
        this.label = label;
        this.subContext = null;
    }

    public EventContext(String label, EventContext subContext) {
        this.label = label;
        this.subContext = subContext;
    }

    public String getLabel(){
        return label;
    }

    public EventContext getSubContext(){
        return subContext;
    }

    @Override
    public String toString(){
        if (subContext != null) return subContext.toString();

        return label == null ? getClass().getTypeName() : label;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || subContext == o;
    }

    @Override
    public int hashCode(){
        return Objects.hash(label, subContext);
    }
}
