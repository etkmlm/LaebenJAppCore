package com.laeben.core.entity;

public class LaebenAppFile {
    private String url;
    private double version;

    public LaebenAppFile(){}
    public LaebenAppFile(String url, double version) {
        this.url = url;
    }

    public String url(){
        return url;
    }

    public double version(){
        return version;
    }
}
