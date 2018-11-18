package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.entity.mj.Buddy;
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
import static com.kunda.engine.handles.socket.CreatBuddyRequestHandler.initdice;
import static com.kunda.engine.model.entity.mj.Buddy.createBuddyById;
import static com.kunda.engine.model.entity.mj.Buddy.getBuddyById;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10009;
import static com.kunda.engine.utils.ECode.E10010;
import static com.kunda.engine.utils.ECode.E10013;


/**
 * 伙伴碎片
 * */
public class BuddyPieceRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(BuddyPieceRequestHandler.class);

        public static int requestId = CMD.t_buddy_piece.id();



        /**
         * 伙伴碎片
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://打开碎片管理(列表)
                    piecemaster(ctx);
                    break;
                case 1://碎片合成伙伴
                    synbuddy(ctx);
                    break;
                case 2://增加伙伴碎片
                    addbuddypiece(ctx);
                    break;
                default:
                    piecemaster(ctx);

            }


        }




        //增加伙伴碎片
        private void addbuddypiece(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //解析参数
            PbBodyBuddy.AddPiece body = PbBodyBuddy.AddPiece.parseFrom(request.getBody());
            System.out.println("AddPiece: " + body.getCid());

            addBuddyPiece(Long.parseLong(rid) ,String.valueOf(body.getCid()),body.getNum());

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);

            SendTo(ctx,response.build());
        }



        //合成伙伴
        private void synbuddy(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //解析参数
            PbBodyBuddy.SynPiece body = PbBodyBuddy.SynPiece.parseFrom(request.getBody());
            int cid  = body.getCid();//伙伴碎片id

            String piece_record =  ExcelCache.inner().get("伙伴数值表","碎片合成",cid);
            String[] piece = piece_record.split(",");


            int num =  getBuddyPiece(Long.parseLong(rid),String.valueOf(cid));
            if(num < Integer.parseInt(piece[3])){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"碎片不足!"));
                return;
            }

            //背包数值
            String item_record =  ExcelCache.inner().get("背包数值表","物品配置表",Integer.parseInt(piece[4]));
            String[] item = item_record.split(",");
            if(Integer.parseInt(item[2])!= 100){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10010,"物品类型不是伙伴卡!"));
                return;
            }
            String buddy_init_id = item[3];//伙伴初始化id
            String init_record =  ExcelCache.inner().get("伙伴数值表","伙伴骰面初始ID",Integer.parseInt(buddy_init_id));
            String[] init = init_record.split(",");//伙伴骰面初始化=>
            int dice = initdice(init);//骰面初始化

            //伙伴配置表
            String level =init[2];String star =init[3]; //伙伴攻防信息初始化 => 细胞id/等级/星级
            String buddy_record =  ExcelCache.inner().get("伙伴数值表","伙伴",cid);
            String[] cell = buddy_record.split(","); //细胞名称、细胞技能、元素属性

            //伙伴配置表
            int buddy_level_id = Integer.parseInt(cell[2])*10000 + Integer.parseInt(star)*1000 + Integer.parseInt(level);
            String atk_record =  ExcelCache.inner().get("伙伴数值表","等级表",buddy_level_id);
            String[] atk = atk_record.split(","); //攻击防御生命


            //创建一个伙伴
            int bid = createBuddyById(Long.parseLong(rid),item,cell,atk,dice);
            //加入到我的伙伴仓库
            add2MyBuddy( Integer.parseInt(rid), bid);

            Buddy buddy = getBuddyById(Long.parseLong(rid),bid);

            if(buddy == null) {
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10009,"buddy id is not exist!"));
                return;
            }

            //消耗碎片
            int rest =  rmvBuddyPiece( Long.parseLong(rid), String.valueOf(cid), num, Integer.parseInt(piece[3]));

            PbBodyBuddy.SynPieceResp.Builder synresp = PbBodyBuddy.SynPieceResp.newBuilder();
            synresp.setBuddy(buddy.toPbBody());
            synresp.setCid(cid);
            synresp.setHas(rest);


            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(synresp.build().toByteString());

            SendTo(ctx,response.build());
        }



        //打开伙伴碎片管理
        private void piecemaster(ChannelHandlerContext ctx)  {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //获取碎片合成配置索引
            String cid_record =   ExcelCache.inner().get("伙伴数值表","碎片合成",0);
            Integer[] cids = String2Array(cid_record);//细胞id列表
            List<String> records =  ExcelCache.inner().mult("伙伴数值表","碎片合成",cids);

            Map<String,String> mp = getBuddyPieceList(Long.parseLong(rid));//碎片数量

            PbBodyBuddy.PieceList.Builder plist = PbBodyBuddy.PieceList.newBuilder();
            for(String record: records){
               String[] pieces =   record.split(",");
               PbBodyBuddy.PieceInfo.Builder pinfo  = PbBodyBuddy.PieceInfo.newBuilder();
                pinfo.setCid(Integer.parseInt(pieces[0]));//细胞id
                pinfo.setStar(Integer.parseInt(pieces[1]));//合成后星级
                pinfo.setNeed(Integer.parseInt(pieces[3]));//需要碎片数量
                if(mp.containsKey(String.valueOf(pieces[0]))){ //拥有碎片的数量
                    pinfo.setHave(Integer.parseInt(mp.get(String.valueOf(pieces[0]))));
                }else {
                    pinfo.setHave(0);
                }
                plist.addPinfo(pinfo);
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(plist.build().toByteString());

            SendTo(ctx,response.build());
        }


























}
