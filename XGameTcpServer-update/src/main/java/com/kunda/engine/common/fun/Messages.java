package com.kunda.engine.common.fun;

import com.google.protobuf.ByteString;
import com.kunda.engine.model.proto.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messages {


    private static final Logger logger = LoggerFactory.getLogger(Messages.class);


    //发送错误消息
    public static ProtobuffFrame.Response ErrMsg(int cmd ,int sub, int code, String reason)  {
        ProtobuffFrame.Response.Builder resp = ProtobuffFrame.Response.newBuilder();
        resp.setCmd(cmd);
        resp.setSub(sub);
        resp.setCode(code);
        resp.setBody(ByteString.copyFrom(reason.getBytes()));

        return resp.build();
    }

    //发送http错误消息
    public static ProtobuffFrame.Response SendHttpErr(int cmd ,int sub, int code, String reason)   {

        ProtobuffFrame.Response.Builder resp = ProtobuffFrame.Response.newBuilder();
        resp.setCmd(cmd);
        resp.setSub(sub);
        resp.setCode(code);
        resp.setBody(ByteString.copyFrom(reason.getBytes()));

        return resp.build();
    }


    //通知消息(需要判断会话通道是否可接收,用于房间内消息)
    public static void SendTo(ChannelHandlerContext ctx, ProtobuffFrame.Response resp) {
        ctx.writeAndFlush(resp);
    }








}
