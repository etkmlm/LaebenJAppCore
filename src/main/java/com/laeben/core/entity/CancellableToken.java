package com.laeben.core.entity;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unchecked")
public class CancellableToken<T extends CancellableToken<T>> {
    private volatile AtomicBoolean stopToken;

    public boolean stopRequested(){
        final AtomicBoolean token = this.stopToken;
        return token != null && token.get();
    }
    public synchronized void stop(){
        if (stopToken != null)
            stopToken.set(true);
        else stopToken = new AtomicBoolean(true);
    }

    /**
     * Sets the stop token manually.
     */
    public synchronized T useStopToken(AtomicBoolean token){
        this.stopToken = token;
        return (T) this;
    }

    /**
     * Transfers the stop token from the target.
     * @param token the token whose stop token is going to be taken.
     */
    public synchronized T useStopToken(CancellableToken<?> token){
        useStopToken(token.stopToken);
        return (T) this;
    }

    /**
     * Transfers the stop token to the target.
     * @param target the target whose stop token is going to be set.
     */
    public void transferStopToken(CancellableToken<?> target){
        if (target == null) return;

        target.useStopToken(stopToken);
    }
}
