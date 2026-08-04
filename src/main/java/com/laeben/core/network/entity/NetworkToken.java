package com.laeben.core.network.entity;

import com.laeben.core.entity.CancellableToken;
import com.laeben.core.entity.Path;

public class NetworkToken extends CancellableToken<NetworkToken> {
    private final String url;
    private final Path destination;
    private final boolean uon;

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
