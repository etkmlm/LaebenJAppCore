package com.laeben.core.entity.exception;

import java.io.IOException;

public class HttpException extends Exception {
    private final String message;
    private final int code;
    private final String url;
    public HttpException(int code, String message, String url){
        this.code = code;
        this.message = message;
        this.url = url;
    }

    public String getSimpleMessage(){
        return message;
    }

    @Override
    public String getMessage(){
        return "Http Exception (" + code + ") " + message + " on " + url;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    public String getUrl(){
        return url;
    }

    public int getStatusCode(){
        return code;
    }
}
