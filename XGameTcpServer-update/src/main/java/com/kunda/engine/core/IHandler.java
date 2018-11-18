package com.kunda.engine.core;

public interface IHandler {

    void addHandler(int var1, BaseServerHandler handler, XHandler.SocketType type);

    Object findHandler(int var1,XHandler.SocketType type) ;


}
