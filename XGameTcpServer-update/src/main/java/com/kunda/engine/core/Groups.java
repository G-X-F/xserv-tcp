package com.kunda.engine.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏群管理器
 *
 * @author yawen
 *
 */
public class Groups {




    private Map<String, Group> channels = new ConcurrentHashMap<>();//mp{key:频道名称，value：游戏频道}

    private static final Groups instance = new Groups();

    public static Groups inst() {
        return instance;
    }

    //通过名称获取一个群
    public Group getGroup(String name) {
            return channels.get(name);
    }

    //创建一个群
    public Group newGroup(String name) {
        Group channel = channels.get(name);
        if (channel != null)
            return channel;
        else {
            channel = new Group(name);
            channels.put(name, channel);
            return channel;
        }
    }

    //通过名称移除一个群
    public void remove(String name) {
         channels.remove(name);
    }


}
