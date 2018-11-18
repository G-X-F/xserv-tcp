package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.SocketException;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;
import static com.kunda.engine.handles.socket.UserInfoRequestHandler.AccountServerPost;
import static com.kunda.engine.handles.socket.UserInfoRequestHandler.user_info_0;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.tools.security.UnicodeUtil.tokenmaker;
import static com.kunda.engine.utils.Const.Uidbase;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.*;


public class CreatRoleRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(CreatRoleRequestHandler.class);

        public static int requestId = CMD.t_create_role.id();



        /**
         * 创建角色
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {
            String uid = ctx.channel().attr(CTX_ATTR_UID).get();
            //登录验证
            if(uid.length() != String.valueOf(Uidbase).length()) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10011,"请先登录!"));
                return;
            }

            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));

            //解析数据
            PbBodyRole.CreateRole body = PbBodyRole.CreateRole.parseFrom(request.getBody());
            String nick = body.getNickname();
            int sex = body.getSex();

            if(nickExist(nick,wid )) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10024,"昵称重复!"));
                return;
            }

            //从账户服务器获取用户的头像信息
            ProtobuffFrame.Response resp = AccountServerPost(user_info_0(uid,tokenmaker(uid)));
            if(resp.getCode() > 0){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),resp.getCode(),"account server error!"));
                return;
            }
            PbBodyUser.UserInfo uinfo=  PbBodyUser.UserInfo.parseFrom(resp.getBody());



            String sign = getUnrepeatedSign(7,4);//获取不重复的游戏编号

            long roleId = createRoleById(Long.parseLong(uid),nick,String.valueOf(sex),uinfo.getHead(),wid,sign);

            Role.setSign(sign,String.valueOf(roleId));

            if(roleId == 0) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"创建角色个数超过限制!"));
                return;
            }

            PbBodyRole.Role role =   getRoleById(roleId).toPbBody();

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(role.toByteString());

            SendTo(ctx,response.build());

        }





































}
