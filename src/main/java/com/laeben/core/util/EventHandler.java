package com.laeben.core.util;


import com.laeben.core.LaebenApp;
import com.laeben.core.entity.Register;
import com.laeben.core.util.events.BaseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Basic event handler.
 */
public class EventHandler<T extends BaseEvent> {
    public static class ExReg<T extends BaseEvent>{
        private final Register<T> reg;
        private final T event;
        public ExReg(Register<T> reg, T event){
            this.reg = reg;
            this.event = event;
        }

        public Register<T> reg(){
            return reg;
        }

        public T event(){
            return event;
        }
    }

    protected final Map<String, Register<T>> handlers;
    private Consumer<ExReg<T>> executeReg;

    public EventHandler(){
        handlers = new HashMap<>();
    }

    /**
     * Add event handler.
     * @param key key of the handler
     * @param handler the handler
     */
    public void addHandler(String key, Consumer<T> handler, boolean isAsync){
        handlers.put(key, new Register<>(handler, isAsync));
    }

    /**
     * @param executeReg function of execute reg
     */
    public void setExecuteReg(Consumer<ExReg<T>> executeReg){
        this.executeReg = executeReg;
    }

    /**
     * Remove a handler.
     * @param key key of the handler
     */
    public void removeHandler(String key){
        handlers.remove(key);
    }

    protected void executeReg(ExReg<T> ex){
        if (executeReg != null)
            executeReg.accept(ex);
        else
            ex.reg().getEx().accept(ex.event());
    }

    /**
     * Execute handlers with a given event.
     * @param e the event
     */
    public void execute(T e){
        handlers.keySet().forEach(x -> {
            Register<T> value = handlers.get(x);
            try{
                executeReg(new ExReg<>(value, e));
            }
            catch (Exception f){
                LaebenApp.handleException(f);
            }
        });
    }
}
