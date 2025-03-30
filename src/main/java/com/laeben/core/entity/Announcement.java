package com.laeben.core.entity;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Announcement {

    public static class DateFactory implements JsonDeserializer<Date>{

        private static final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm");

        @Override
        public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return formatter.parse(jsonElement.getAsString());
            } catch (ParseException e) {
                return null;
            }
        }
    }

    private TranslationBundle title;
    private TranslationBundle content;
    private Date date;
    private List<String> versions;

    private int duration;
    private int id;


    public String getTitle(Locale locale){
        return title.get(locale);
    }

    public String getContent(Locale locale){
        return content.get(locale);
    }

    public Date getDate(){
        return date;
    }

    public boolean containingVersion(String version){
        return versions.contains(version);
    }

    public List<String> getVersions(){
        return versions;
    }

    public int getDuration(){
        return duration;
    }

    public int getId(){
        return id;
    }
}
