package com.laeben.core.util;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class StrUtil {
    private static final char[] INVALID_CHARS = {
            34,60,62,124,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,58,42,63,92,47
    };

    public static String trimEnd(String str, char c){
        var n = new StringBuilder();

        boolean x = false;
        int len = str.length();
        int t = 0;

        for (int i = len - 1; i >= 0; i--){
            if (str.charAt(i) != c)
                x = true;

            if (x)
                n.append(str.charAt(len - i - 1 - t));
            else
                t++;
        }

        return n.toString();
    }

    public static String pure(String str){
        return pure(str, null);
    }

    public static String pure(String source, char[] exclude){
        if (source == null)
            return null;
        for(char i : INVALID_CHARS){
            if (exclude != null){
                boolean c = false;
                for (char j : exclude){
                    if (i == j){
                        c = true;
                        break;
                    }
                }
                if (c)
                    continue;
            }
            source = source.replace(String.valueOf(i), "");
        }
        return source;
    }

    public static String toUpperFirst(String target){
        return target.substring(0, 1).toUpperCase(Locale.US) + target.substring(1);
    }

    public static String jsArray(List<String> values){
        return "[" + values.stream().filter(Objects::nonNull).map(x -> x.startsWith("[") ? x : "\"" + x + "\"").collect(Collectors.joining(",")) + "]";
    }

    public static String sub(String t, int s, int e){
        return t.length() > e-s ? t.substring(s, e) : t;
    }
}
