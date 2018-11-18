package com.kunda.engine.core;

import com.kunda.engine.model.proto.*;
import io.netty.channel.ChannelHandlerContext;

public interface IRequestHandler {

    void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws Exception;
    ProtobuffFrame.Response handleClientHttpRequest(ChannelHandlerContext ctx) throws Exception;

}
