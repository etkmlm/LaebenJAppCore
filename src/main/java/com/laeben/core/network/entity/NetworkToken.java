package com.laeben.core.network.entity;

import com.laeben.core.entity.Path;

import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkToken {
    private final String url;
    private final Path destination;
    private final boolean uon;

    private AtomicBoolean stopToken;

    private NetworkToken(String url, Path destination, boolean useOriginalName) {
        this.url = url;
        this.destination = destination;
        this.uon = useOriginalName;
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

    public boolean stopRequested(){
        return stopToken != null && stopToken.get();
    }
    public void stop(){
        if (stopToken != null)
            stopToken.set(true);
        else stopToken = new AtomicBoolean(true);
    }
    public NetworkToken useStopToken(AtomicBoolean token){
        this.stopToken = token;
        return this;
    }

    public String getUrl(){
        return url;
    }
    public Path getDestination(){
        return destination;
    }
    public boolean useOriginalName(){
        return uon;
    }
}
