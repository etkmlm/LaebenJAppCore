module com.laeben.core {
    requires com.google.gson;
    requires org.apache.commons.compress;

    opens com.laeben.core to com.laeben,com.google.gson;
    opens com.laeben.core.entity;
    opens com.laeben.core.entity.exception;
    opens com.laeben.core.util;
    opens com.laeben.core.util.events;

    exports com.laeben.core;
    exports com.laeben.core.entity;
    exports com.laeben.core.entity.exception;
    exports com.laeben.core.util;
    exports com.laeben.core.util.events;
    exports com.laeben.core.network.requester;
    opens com.laeben.core.network.requester;
    exports com.laeben.core.network;
    opens com.laeben.core.network;
    exports com.laeben.core.network.entity;
}