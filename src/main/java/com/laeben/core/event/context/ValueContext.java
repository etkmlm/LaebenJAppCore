package com.laeben.core.event.context;

public class ValueContext extends EventContext {
    public static final String LABEL = "Value";
    public static final ValueContext SELF = new ValueContext(LABEL, null);
    public ValueContext(String label, EventContext context) {
        super(label, context);
    }
}
