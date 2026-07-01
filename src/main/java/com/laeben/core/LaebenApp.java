package com.laeben.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.laeben.core.entity.Announcement;
import com.laeben.core.entity.LaebenAppFile;
import com.laeben.core.entity.RequestParameter;
import com.laeben.core.entity.TranslationBundle;
import com.laeben.core.entity.exception.HttpException;
import com.laeben.core.entity.exception.NoConnectionException;
import com.laeben.core.util.EventHandler;
import com.laeben.core.util.RequesterFactory;
import com.laeben.core.util.events.BaseEvent;
import com.laeben.core.util.events.ValueEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class LaebenApp {
    public static final String EXCEPTION = "exception";
    public static final String NET_EXCEPTION = "netException";

    private static final String FIREBASE = "https://laeben-update-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final RequesterFactory requester = new RequesterFactory(FIREBASE);
    private static final EventHandler<BaseEvent> handler = new EventHandler<>();

    private static final Gson GSON = new GsonBuilder()
            //.registerTypeAdapter(Date.class, new Announcement.DateFactory())
            .registerTypeAdapter(TranslationBundle.class, new TranslationBundle.TranslationBundleFactory())
            .create();

    private String id;

    private double latest;
    private String name;
    private String icon;

    private static boolean isOffline;


    private LaebenApp(){

    }
    private LaebenApp(String id, String name){
        this.id = id;
        this.name = name;
    }


    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }

    public String getIcon(){
        return icon;
    }

    public static EventHandler<BaseEvent> getHandler(){
        return handler;
    }

    public static void handleException(Exception e){
        getHandler().execute(new ValueEvent(EXCEPTION, e));
    }

    public static LaebenApp get(String id, String defaultName) throws NoConnectionException, HttpException {
        String str = requester.create().to("apps").to(id + ".json").getString();
        if (str == null)
            return LaebenApp.offline(id, defaultName);

        LaebenApp app = GSON.fromJson(str, LaebenApp.class);
        app.id = id;
        return app;
    }

    public static LaebenApp offline(String id, String name){
        return new LaebenApp(id, name).asOffline();
    }

    private LaebenApp asOffline(){
        isOffline = true;
        return this;
    }

    public <T> T getObject(String path, Gson gson, Class<T> clazz) throws NoConnectionException, HttpException {
        String str = requester.create().to("apps").to(id).to(path + ".json").getString();
        if (str == null || str.equals("null"))
            return null;

        T t;
        if (gson != null)
            t = gson.fromJson(str, clazz);
        else
            t = GSON.fromJson(str, clazz);

        return t;
    }

    public <T> List<T> getObjects(String path, Gson gson, Class<T> clazz, List<RequestParameter> filters) throws NoConnectionException, HttpException {
        var r = requester.create().to("apps").to(id).to(path + ".json");
        if (filters != null) r.withParams(filters);
        InputStream str = r.getStream();
        if (str == null)
            return null;

        var list = new ArrayList<T>();

        if (gson == null)
            gson = GSON;

        boolean isArray;

        try (final JsonReader reader = new JsonReader(new InputStreamReader(str))){
            switch (reader.peek()){
                case BEGIN_ARRAY:
                    isArray = true;
                    reader.beginArray();
                    break;
                case BEGIN_OBJECT:
                    isArray = false;
                    reader.beginObject();
                    break;
                default:
                    return null;
            }

            while (reader.hasNext()){
                if (!isArray) reader.nextName(); // index
                if (reader.peek() == JsonToken.NULL) continue;
                list.add(gson.fromJson(reader, clazz));
            }

            if (isArray) reader.endArray();
            else reader.endObject();
        } catch (IOException e) {
            handleException(e);
            return null;
        }

        return Collections.unmodifiableList(list);
    }

    public List<LaebenAppFile> getFiles(double fromVersion, double toVersion) throws NoConnectionException, HttpException {
        final var filesTemp = getObjects("files", GSON, LaebenAppFile.class, null);
        if (filesTemp == null) return List.of();

        return Collections.unmodifiableList(filesTemp);
    }

    public List<Announcement> getAnnouncements() throws NoConnectionException, HttpException {
        final var aTemp = getObjects("announcements", GSON, Announcement.class, List.of(
            new RequestParameter("orderBy", "\"end_time\""),
            new RequestParameter("startAt", "\"" + Instant.now().atZone(ZoneOffset.UTC) + "\"")
        ));
        if (aTemp == null) return List.of();

        return Collections.unmodifiableList(aTemp);
    }

    public static boolean isOffline(){
        return isOffline;
    }

    public LaebenAppFile getLatest() throws NoConnectionException, HttpException {
        return getObject("latestMeta", GSON, LaebenAppFile.class);
    }
}
