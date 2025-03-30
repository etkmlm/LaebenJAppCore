package com.laeben.core.util;

public class Cat {
    public static void sleep(long millis){
        try{
            Thread.sleep(millis);
        }
        catch (InterruptedException ignored){

        }
    }
}
