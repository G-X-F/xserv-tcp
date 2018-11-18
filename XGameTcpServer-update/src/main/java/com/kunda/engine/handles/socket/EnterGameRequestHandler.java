package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.core.Groups;
import com.kunda.engine.model.entity.mj.Buddy;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.kunda.engine.common.fun.Avatas.String2Array;
import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Buddy.getBuddyById;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.CH_KEY_SERVER;
import static com.kunda.engine.utils.Const.Uidbase;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10001;
import static com.kunda.engine.utils.ECode.E10011;


public class EnterGameRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(EnterGameRequestHandler.class);

        public static int requestId = CMD.t_enter_game.id();



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
            String sign = body.getSign();
            Role role = getRoleBySign(sign);
            long rid = role.getId();

            if(role == null) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10001,"角色不存在!"));
                return;
            }
            PbBodyRole.Role rolebody =   role.toPbBody();

            updateLastRole(Long.parseLong(uid),String.valueOf(rid));//更新上次登录角色记录
            ctx.channel().attr(CTX_ATTR_RID).set(String.valueOf(rid));//缓存角色ID到此连接
            Groups.inst().newGroup(CH_KEY_SERVER).addChannel(ctx.channel());//加入全服频道

            //我的伙伴列表
            PbBodyBuddy.BuddyList.Builder buddylist = PbBodyBuddy.BuddyList.newBuilder();

            Map<String,String> buddymp = myBuddyList(rid);
            for(String bid : buddymp.keySet()){
                Buddy buddy = getBuddyById(rid,Integer.parseInt(bid));
                if(buddy != null) {
                    buddylist.addBuddys(buddy.toPbBody());
                }
            }

            //伙伴碎片列表
            String cid_record = ExcelCache.inner().get("伙伴数值表","碎片合成",0);
            Integer[] cids = String2Array(cid_record);//细胞id列表
            List<String> records =  ExcelCache.inner().mult("伙伴数值表","碎片合成",cids);

            Map<String,String> piecemp = getBuddyPieceList(rid);//碎片数量

            PbBodyBuddy.PieceList.Builder plist = PbBodyBuddy.PieceList.newBuilder();
            for(String record: records){
                String[] pieces =   record.split(",");
                PbBodyBuddy.PieceInfo.Builder pinfo  = PbBodyBuddy.PieceInfo.newBuilder();
                pinfo.setCid(Integer.parseInt(pieces[0]));//细胞id
                pinfo.setStar(Integer.parseInt(pieces[1]));//合成后星级
                pinfo.setNeed(Integer.parseInt(pieces[3]));//需要多少
                if(piecemp.containsKey(String.valueOf(pieces[0]))){ //拥有碎片的数量
                    pinfo.setHave(Integer.parseInt(piecemp.get(String.valueOf(pieces[0]))));
                }else {
                    pinfo.setHave(0);
                }
                plist.addPinfo(pinfo);
            }


            PbBodyRole.EnterGame.Builder entergame = PbBodyRole.EnterGame.newBuilder();
            entergame.setRole(rolebody);
            entergame.setBuddylist(buddylist);
            entergame.setPiecelist(plist);

           response.setCmd(requestId);
           response.setSub(request.getSub());
           response.setCode(ZERO);
           response.setBody(entergame.build().toByteString());

           SendTo(ctx,response.build());
        }





































}
