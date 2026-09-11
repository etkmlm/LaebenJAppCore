package com.laeben.core.network.entity;

import com.laeben.core.concurrency.CancellableToken;
import com.laeben.core.entity.Path;
import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.function.ProgressFunction;
import com.laeben.core.network.event.NetworkProgressContext;

public class NetworkToken extends CancellableToken<NetworkToken> {
    private final String url;
    private final Path destination;
    private final boolean useOriginalName;

    private ProgressFunction onProgress;

    private NetworkToken(String url, Path destination, boolean useOriginalName) {
        this.url = url;
        this.destination = destination;
        this.useOriginalName = useOriginalName;
    }

    /**
     * Creates a new network token instance.
     * @param url destination url
     * @param destination file or directory path relative to useOriginalName
     * @param useOriginalName use destination as a file or base dir
     * @return the created network token
     */
    public static NetworkToken create(String url, Path destination, boolean useOriginalName) {
        return new NetworkToken(url, destination, useOriginalName);
    }

    public NetworkToken withLogging(ProgressFunction onProgress){
        this.onProgress = onProgress;
        return this;
    }

    public void onReceivedProgress(long current, long total){
        if (this.onProgress != null) this.onProgress.onProgress(current, total, NetworkProgressContext.SELF);
    }

    public void onReceivedProgress(long current, long total, EventContext context){
        if (this.onProgress != null) this.onProgress.onProgress(current, total, context);
    }

    public String getUrl(){
        return url;
    }
    public Path getDestination(){
        return destination;
    }
    public boolean useOriginalName(){
        return useOriginalName;
    }
}
