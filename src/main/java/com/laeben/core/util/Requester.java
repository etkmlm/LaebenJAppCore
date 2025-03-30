package com.laeben.core.util;

import com.laeben.core.entity.exception.HttpException;
import com.laeben.core.entity.exception.NoConnectionException;
import com.laeben.core.entity.RequestParameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Network requester.
 */
public class Requester {
    private final List<RequestParameter> headers;
    private final List<RequestParameter> parameters;
    private String start = "https://";
    private String url;

    public Requester(String baseUrl){
        url = baseUrl
                .replace("https://", "")
                .replace("http://", "");

        if (!url.endsWith("/"))
            url += "/";

        headers = new ArrayList<>();
        parameters = new ArrayList<>();
    }

    /**
     * Create a new requester.
     * @param baseUrl base url
     * @return new requester
     */
    public static Requester begin(String baseUrl){
        return new Requester(baseUrl);
    }

    /**
     * Mark requester as http.
     * @return the requester
     */
    public Requester http(){
        start = "http://";
        return this;
    }

    /**
     * Include a part to the url.
     * @param url the part
     * @return the requester
     */
    public Requester to(String url){
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);

        if (!this.url.endsWith("/"))
            this.url += "/";

        this.url += url;

        this.url = this.url.replace("//", "/");
        return this;
    }

    private String getParams(){
        return "?" + String.join("&", parameters.stream().map(x -> x.key() + "=" + x.value()).toArray(String[]::new));
    }

    public Requester withHeader(RequestParameter h){
        headers.add(h);
        return this;
    }

    public Requester withParam(RequestParameter p){
        parameters.add(p);
        return this;
    }

    public Requester withParams(List<RequestParameter> ps){
        parameters.addAll(ps);

        return this;
    }

    /**
     * Get content as a string.
     * @return the content
     */
    public String getString() throws NoConnectionException, HttpException {
        return NetUtils.urlToString(getUrl(), headers);
    }

    /**
     * Post.
     * @param content content body
     * @return the response
     */
    public String post(String content) throws NoConnectionException {
        return NetUtils.post(getUrl(), content, headers);
    }

    /**
     * Get the url.
     * @return the url
     */
    public String getUrl(){
        return start + url + getParams();
    }

    /**
     * Convert the url to the url object.
     * @return the url object
     */
    public URL toURL(){
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}