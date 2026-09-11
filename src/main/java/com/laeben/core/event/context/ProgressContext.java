package com.laeben.core.event.context;

public class ProgressContext extends EventContext {
    public static final String LABEL = "Progress";
    public static final ProgressContext SELF = new ProgressContext(LABEL, null);

    public ProgressContext(String label, EventContext context) {
        super(label, context);
    }
}
