package com.laeben.core.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TranslationBundle {
    public static class TranslationBundleFactory implements JsonDeserializer<TranslationBundle> {

        @Override
        public TranslationBundle deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            var obj = jsonElement.getAsJsonObject();

            var bundle = new TranslationBundle();
            for (var k : obj.keySet()){
                bundle.put(k, obj.get(k).getAsString());
            }

            return bundle;
        }
    }


    private final Map<String, String> translates;

    public TranslationBundle() {
        translates = new HashMap<>();
    }

    public void put(Locale locale, String value){
        translates.put(locale.getLanguage().toLowerCase(), value);
    }

    public String get(Locale locale){
        return translates.get(locale.getLanguage().toLowerCase());
    }

    public boolean has(Locale locale){
        return translates.containsKey(locale.getLanguage().toLowerCase());
    }

    public void put(String key, String value){
        translates.put(key, value);
    }

    public String get(String key){
        return translates.get(key);
    }

    public boolean has(String key){
        return translates.containsKey(key);
    }
}
