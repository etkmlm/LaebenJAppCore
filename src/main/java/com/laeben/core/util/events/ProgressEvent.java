package com.laeben.core.util.events;

import com.laeben.core.util.NetUtils;

/**
 * Progress event.
 * <br/>
 * Includes the source, the key, remained, and the total value.
 */
public class ProgressEvent extends KeyEvent {
    public double remain;
    public double total;

    public ProgressEvent(String key, double remain, double total) {
        super(key);
        this.remain = remain;
        this.total = total;
    }

    public double getRemain(){
        double x = getKey().equals(NetUtils.DOWNLOAD) ? remain / 1024 / 1024 : remain;
        return Math.floor(x * 10) / 10;
    }

    public double getTotal(){
        double x = getKey().equals(NetUtils.DOWNLOAD) ? total / 1024 / 1024 : total;
        return Math.floor(x * 10) / 10;
    }

    public double getProgress(){
        return Math.floor(remain / total * 100) / 100;
    }

    public double getProgressPercent(){
        return getProgress() * 100;
    }
}
