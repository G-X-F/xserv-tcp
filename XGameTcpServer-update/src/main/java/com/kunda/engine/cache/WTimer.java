package com.kunda.engine.cache;

import com.kunda.engine.model.task.ReloadConfigTask;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;


/**
 * 时间轮定时器
 * */
public class WTimer {

    private static Timer timer = new HashedWheelTimer(1, TimeUnit.SECONDS,60);

    private static final WTimer instance = new WTimer();

    public static  WTimer instance() {
        return instance;
    }

    public void add(ReloadConfigTask task, long delay , TimeUnit unit){

        timer.newTimeout(task,delay,unit);
    }




}
