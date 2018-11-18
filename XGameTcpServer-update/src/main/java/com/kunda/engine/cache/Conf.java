package com.kunda.engine.cache;


import java.util.HashMap;
import java.util.Map;


/**
 * 在内存中缓存配置项
 *
 * @author yawen
 *
 */
public class Conf {


    /**

    /**
     * 配置项缓存
     */
    private Map<String, String> configs = new HashMap<>();//主要是读取缓存配置项

    private static final Conf instance = new Conf();

    public static Conf inner() {
        return instance;
    }


    /**
     * 增加配置项*/
    public void add(String key, String value ){
        configs.put(key,value);
    }

    public String get(String key ){
        return configs.get(key);
    }





}
