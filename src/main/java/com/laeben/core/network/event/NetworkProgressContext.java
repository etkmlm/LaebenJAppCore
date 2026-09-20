package com.laeben.core.network.event;

import com.laeben.core.event.context.EventContext;

public final class NetworkProgressContext extends EventContext {
    public static final String LABEL = "NetworkProgress";
    public static final NetworkProgressContext SELF = new NetworkProgressContext(LABEL);

    public NetworkProgressContext(String label) {
        super(label, null);
    }

    public NetworkProgressContext(String label, EventContext context) {
        super(label, context);
    }
}
