package com.laeben.core.event.context;

public class ChangeContext extends EventContext {
    public static final String LABEL = "Change";
    public static final ChangeContext SELF = new ChangeContext(LABEL, null);

    public ChangeContext(String label, EventContext context) {
        super(label, context);
    }
}
