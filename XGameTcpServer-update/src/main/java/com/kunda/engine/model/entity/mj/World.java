package com.kunda.engine.model.entity.mj;

public class World {

    private int id;
    private String name;
    private String addr;
    private String inner;
    private int tport;
    private int hport;


    public World(int id,String name , String addr,String inner,int tport,int hport){
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.inner = inner;
        this.tport = tport;
        this.hport = hport;
    }

    public World(int id,String name , String addr,String inner,int tport){
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.inner = inner;
        this.tport = tport;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

    public String getInner() {
        return inner;
    }

    public int getTport() {
        return tport;
    }

    public int getHport() {
        return hport;
    }
}
