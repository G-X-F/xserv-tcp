package com.kunda.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class XHandler implements IHandler {

    private final Map<Integer, BaseServerHandler> handlers = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(XHandler.class);


    private static final XHandler instance = new XHandler();

    public  enum SocketType{
        http(0),
        protobuf(1),
        websocket(2);

        private int code;

        public int code(){
            return code;
        }

        SocketType(int num){
            this.code= num;
        }

    }

    public static XHandler getInstance() {
        return instance;
    }



    @Override
    public void addHandler(int handlerId, BaseServerHandler handler, SocketType type) {
        Integer key = handlerId*10 + type.code;
        this.handlers.put(key,handler);



    }


    @Override
    public Object findHandler(int handlerId,SocketType type)  {

        Integer key = handlerId*10 + type.code;
        BaseServerHandler handler =  this.handlers.get(key);
           if(handler!= null){
               return  handler;
           }

        return null;
    }


}
