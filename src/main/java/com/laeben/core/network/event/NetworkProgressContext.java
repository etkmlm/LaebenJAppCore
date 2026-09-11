package com.laeben.core.network.event;

import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.context.ProgressContext;

public final class NetworkProgressContext extends ProgressContext {
    public static final String LABEL = "NetworkProgress";
    public static final NetworkProgressContext SELF = new NetworkProgressContext(LABEL);

    public NetworkProgressContext(String label) {
        super(label, null);
    }

    public NetworkProgressContext(String label, EventContext context) {
        super(label, context);
    }
}
