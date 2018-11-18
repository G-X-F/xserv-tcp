package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Role.getRoleById;
import static com.kunda.engine.utils.Const.BLANK;
import static com.kunda.engine.utils.Const.Uidbase;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.*;


public class DeleteRoleRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(DeleteRoleRequestHandler.class);

        public static int requestId = CMD.t_delete_role.id();



        /**
         * Login 登录事件处理
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
              //解析数据
            String uid = ctx.channel().attr(CTX_ATTR_UID).get();
            //登录验证
            if(uid.length() != String.valueOf(Uidbase).length()) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10011,"请先登录!"));
                return;
            }
            PbBodyRole.SelectRole body = PbBodyRole.SelectRole.parseFrom(request.getBody());
            Role delRole = getRoleById(body.getId());
            if(!delRole.getDel().equals(BLANK)){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10051,"不能重复删除!"));
                return;
            }



            long at =  System.currentTimeMillis() + 72*3600*1000;
            delRole.setDel(at);
            PbBodyRole.Role role =   delRole.toPbBody();


           response.setCmd(requestId);
           response.setSub(request.getSub());
           response.setCode(ZERO);
           response.setBody(role.toByteString());

           SendTo(ctx,response.build());
        }





































}
