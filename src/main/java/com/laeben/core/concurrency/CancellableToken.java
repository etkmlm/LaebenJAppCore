package com.laeben.core.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unchecked")
public class CancellableToken<T> {
    private volatile AtomicBoolean stopToken;

    /**
     * Checks both the current thread and stopToken.
     */
    public boolean shouldStop(){
        return Thread.currentThread().isInterrupted() || stopRequested();
    }

    /**
     * Checks the stopToken.
     */
    public boolean stopRequested(){
        AtomicBoolean token = this.stopToken;
        return token != null && token.get();
    }
    public synchronized void stop(){
        if (stopToken != null) stopToken.set(true);
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
    public synchronized T syncWith(CancellableToken<?> token){
        useStopToken(token == null ? null : token.stopToken);
        return (T) this;
    }
}
