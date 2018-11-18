package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.core.Groups;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.List;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;
import static com.kunda.engine.model.entity.mj.Role.getAllRole;
import static com.kunda.engine.model.entity.mj.Role.lastLoginRole;
import static com.kunda.engine.tools.security.UnicodeUtil.checkToken;
import static com.kunda.engine.utils.Const.CH_KEY_SERVER;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10008;


public class LoginRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(LoginRequestHandler.class);

        public static int requestId = CMD.t_login.id();



        /**
         * Login 登录事件处理
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {
              //解析数据
              PbBodyTcpLogin.LoginReq body = PbBodyTcpLogin.LoginReq.parseFrom(request.getBody());
              //Token检查
              if(!checkToken(String.valueOf(body.getUid()),body.getToken(),true)) {
                   SendTo(ctx,ErrMsg(requestId,request.getSub(),E10008,"token is invalid!"));
                   return;
              }

            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));

              //绑定uid到channel属性
              ctx.channel().attr(CTX_ATTR_UID).set(String.valueOf(body.getUid()));

              //获取玩家所有角色信息
              List<Role> list = getAllRole(body.getUid(),wid);
              PbBodyRole.RoleList.Builder pblist = PbBodyRole.RoleList.newBuilder();
              for(Role r : list ){
                  pblist.addRole(r.toPbBody());
              }

              pblist.setLast(lastLoginRole(body.getUid()));//上一次登录的角色id 没有则为空


              response.setCmd(requestId);
              response.setSub(request.getSub());
              response.setCode(ZERO);
              response.setBody(pblist.build().toByteString());

              SendTo(ctx,response.build());
        }





































}
