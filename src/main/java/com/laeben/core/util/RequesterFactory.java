package com.laeben.core.util;


public class RequesterFactory{
    private final String baseUrl;
    private boolean http;
    public RequesterFactory(String baseUrl){
        if (baseUrl.startsWith("http://"))
            http = true;

        this.baseUrl = baseUrl.replace("https://", "").replace("http://", "");
    }

    /**
     * Create a new requester with given base url.
     * @return new requester
     */
    public Requester create(){
        return http ? new Requester(baseUrl).http() : new Requester(baseUrl);
    }
}
