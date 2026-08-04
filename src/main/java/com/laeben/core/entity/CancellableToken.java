package com.laeben.core.entity;

import java.util.concurrent.atomic.AtomicBoolean;

public class CancellableToken {
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
    public synchronized void useStopToken(AtomicBoolean token){
        this.stopToken = token;
    }

    /**
     * Transfers the stop token to the target.
     * @param target the target whose stop token is going to be set.
     */
    public void transferStopToken(CancellableToken target){
        if (target == null) return;

        target.useStopToken(stopToken);
    }
}
