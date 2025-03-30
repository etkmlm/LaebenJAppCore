package com.laeben.core.entity;

import com.laeben.core.util.events.BaseEvent;

import java.util.function.Consumer;

public class Register<T extends BaseEvent> {

    private final boolean isAsync;
    private final Consumer<T> ex;

    public Register(Consumer<T> ex, boolean isAsync){
        this.ex = ex;
        this.isAsync = isAsync;
    }

    public boolean isAsync(){
        return isAsync;
    }

    public Consumer<T> getEx(){
        return ex;
    }
}
