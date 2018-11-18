package com.kunda.engine.cache;


import com.kunda.engine.model.entity.mj.World;

import java.net.SocketException;
import java.util.*;

import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;


/**
 * 在内存中缓存世界列表
 *
 * @author yawen
 *
 */
public class WorldMap {


    /**

     /**
     * 世界分服缓存
     */
    private Map<Integer, World> worlds = new HashMap<>();//wid-world
    private Map<String, Integer> server = new HashMap<>();//ip:port-wid

    private int currentWid ;


    private static final WorldMap instance = new WorldMap();

    public  static WorldMap inner() {
        return instance;
    }


    public WorldMap()  {
        try{
            int wid =  instance.wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));
            this.currentWid = wid;
        }catch(Exception e){

        }
    }


    /**
     * 增加配置项*/
    public void add(Integer id,String name,String addr,String inner,int tport,int hport){
        World wd = new World(id,name,addr,inner,tport,hport);
        worlds.put(id,wd);
        String inner_tip = inner+":"+tport;
        String inner_hip = inner+":"+hport;
        server.put(inner_tip,id);
        server.put(inner_hip,id);

    }

    public World get(Integer id ){
        return worlds.get(id);
    }


    //迭代器
    public Collection<World> values(){
        return worlds.values();
    }

    //根据内网地址查询世界id
    public int wid(String inner_port){
        return server.get(inner_port);
    }

    //获取当前服务器世界ID
    public int getCurrentWid() {
        return currentWid;
    }
}
