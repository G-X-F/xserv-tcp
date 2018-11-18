package com.kunda.engine.model.task.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThreadPool implements ThreadFactory {

    /**
     * id生成器
     */
    private AtomicInteger idMaker = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "T" + idMaker.incrementAndGet()); //线程池名称 H2 表示http线程池的2号线程
    }
}
