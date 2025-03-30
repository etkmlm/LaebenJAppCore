package com.laeben.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.laeben.core.entity.Announcement;
import com.laeben.core.entity.LaebenAppFile;
import com.laeben.core.entity.TranslationBundle;
import com.laeben.core.entity.exception.HttpException;
import com.laeben.core.entity.exception.NoConnectionException;
import com.laeben.core.util.EventHandler;
import com.laeben.core.util.RequesterFactory;
import com.laeben.core.util.events.BaseEvent;
import com.laeben.core.util.events.ValueEvent;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LaebenApp {
    public static final String EXCEPTION = "exception";
    public static final String NET_EXCEPTION = "netException";

    private static final String FIREBASE = "https://laeben-update-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final RequesterFactory requester = new RequesterFactory(FIREBASE);
    private static final EventHandler<BaseEvent> handler = new EventHandler<>();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new Announcement.DateFactory())
            .registerTypeAdapter(TranslationBundle.class, new TranslationBundle.TranslationBundleFactory())
            .create();

    private String id;

    private double latest;
    private String name;
    private String icon;
    private List<LaebenAppFile> files;
    private List<Announcement> announcements;

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
        if (app.announcements != null)
            app.announcements.removeIf(Objects::isNull);
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

    public List<LaebenAppFile> getFiles(){
        if (files == null)
            files = List.of();
        return files;
    }

    public List<Announcement> getAnnouncements(){
        if (announcements == null)
            announcements = List.of();
        return announcements;
    }

    public static boolean isOffline(){
        return isOffline;
    }

    public LaebenAppFile getLatest(){
        return getFiles().stream().filter(x -> x.version() == latest).findFirst().orElse(null);
    }
}
