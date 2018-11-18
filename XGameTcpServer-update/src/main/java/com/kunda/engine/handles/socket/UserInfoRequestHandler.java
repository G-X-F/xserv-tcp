package com.kunda.engine.handles.socket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.tools.http.HttpRequestUtil;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.kunda.engine.common.fun.Avatas.fromString;
import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.tools.security.UnicodeUtil.checkToken;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10008;

/**
 *
 * 向账户服务器查询用户基本信息
 *
 **/
public class UserInfoRequestHandler extends BaseServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoRequestHandler.class);

    public static int requestId = CMD.h_user_info.id();



    @Override
    public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

        PbBodyComm.StrReq body = PbBodyComm.StrReq.parseFrom(request.getBody());
        //-----------------------------------------------------------------------------------------------

        if(!checkToken(body.getS1(), body.getS2(), true)) {
            SendTo(ctx,ErrMsg(requestId,request.getSub(),E10008,"token is invalid!"));
            return;
        }

        ProtobuffFrame.Response resp = AccountServerPost(user_info_0(body.getS1(),body.getS2()));
        if(resp.getCode() > 0){
            SendTo(ctx,ErrMsg(requestId,request.getSub(),resp.getCode(),"account server error!"));
            return;
        }
        //设置返回消息头部
        response.setCmd(requestId);
        response.setSub(0);
        response.setCode(ZERO);
        response.setBody(resp.getBody());
        SendTo(ctx,response.build());
    }




    /**
     * 向帐户服务器发送Post请求
     * */
    public static ProtobuffFrame.Response AccountServerPost(ProtobuffFrame.Request request ) throws InvalidProtocolBufferException {
        String url = "http://"+ Conf.inner().get("account.server"); //账户服务器地址
        byte[] result = HttpRequestUtil.sendPost(url,request.toByteArray(),"application/octet-stream;charset=utf-8");
        return ProtobuffFrame.Response.parseFrom( result );
    }


    /**
     * 构造查询用户信息请求消息体
     * */
    public static  ProtobuffFrame.Request user_info_0(String uid,String token) {
        PbBodyComm.StrReq.Builder body = PbBodyComm.StrReq.newBuilder();
        body.setS1(uid);//uid
        body.setS2(token);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.h_user_info.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }






}
