package com.laeben.core.entity;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestParameter {

    private final String key;
    private final Object value;
    private boolean escape;

    public RequestParameter(String key, Object value){
        this.key = key;
        this.value = value;
    }

    public String key(){
        return key;
    }

    public Object value(){
        return escape ? URLEncoder.encode(value.toString(), StandardCharsets.UTF_8) : value;
    }

    public RequestParameter markAsEscapable(){
        escape = true;
        return this;
    }

    public static RequestParameter bearer(String token) {
        return new RequestParameter("Authorization", "Bearer " + token);
    }

    public static RequestParameter contentType(String type){
        return new RequestParameter("Content-Type", type);
    }

    public static RequestParameter fromString(String str) {
        String[] spl = str.split(":");
        return new RequestParameter(spl[0], spl[1].trim());
    }

    public static <T> List<RequestParameter> classToParams(T c, Class<T> t){
        List<RequestParameter> params = new ArrayList<RequestParameter>();

        for (Field f : t.getDeclaredFields()) {
            try {
                Object a = f.get(c);
                if (a == null || (a instanceof Integer && (Integer)a == 0))
                    continue;
                if (a instanceof Enum<?>)
                    a = ((Enum<?>)a).ordinal();
                else if (a instanceof List){
                    var xa = (List<?>)a;
                    if (!xa.isEmpty() && xa.get(0) instanceof Enum<?>){
                        a = xa.stream().map(e -> ((Enum<?>)e).ordinal()).collect(Collectors.toUnmodifiableList());
                    }
                }
                params.add(new RequestParameter(f.getName(), a));
            } catch (IllegalAccessException ignored) {

            }
        }

        return params;
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }
}