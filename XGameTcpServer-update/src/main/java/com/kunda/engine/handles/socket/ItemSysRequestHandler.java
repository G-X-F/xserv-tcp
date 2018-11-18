package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.entity.mj.Item;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Item.getItemById;
import static com.kunda.engine.model.entity.mj.Packet.*;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10012;


/**
 * 物品使用接口
 * */
public class ItemSysRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(ItemSysRequestHandler.class);

        public static int requestId = CMD.t_item_sys.id();



        /**
         * 物品使用接口
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://查看物品详情
                    ItemDetail(ctx);
                    break;
                case 1://使用物品
                    useitems(ctx);
                    break;
                default:
                    useitems(ctx);

            }


        }




    //查看物品详情
    private void ItemDetail(ChannelHandlerContext ctx ) throws InvalidProtocolBufferException {
        long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID

        PbBodyItemSys.ItemDetail body = PbBodyItemSys.ItemDetail.parseFrom(request.getBody());
        Item item = getItemById(rid,body.getId());

        Rdm.instance().sadd(KEY_PACKET_BOOK(rid),String.valueOf(item.getItem_id()));//加入图鉴，解除New标记


        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        response.setBody(item.toPbBody().toByteString());

        SendTo(ctx,response.build());
    }






        //使用物品
        private void useitems(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID

            PbBodyPacket.UseItems body = PbBodyPacket.UseItems.parseFrom(request.getBody());
            Item item = getItemById(rid,body.getId()) ;//获得该物品信息
            if(item == null){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"item is not exist!"));
                return;
            }
            int onum = item.getNum();
            if(body.getNum() > onum){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"not enough items!"));
                return;
            }

            //兑现物品使用效果
            switch (item.getType()){
                case 11: //伴侣经验丹道具
                    useExpItem(ctx, item, body.getNum());
                    break;
                case 12: //金币道具
                    useGoldItem(ctx, item ,body.getNum());
                    break;
                case 14: //体力道具
                    useHpItem(ctx, item ,body.getNum());
                    break;
                case 15: //心情道具
                    useMoodItem(ctx, item ,body.getNum());
                    break;
                case 16: //礼包
                    break;
                case 21: //常规道具
                    break;
            }

            item.setNum(onum - body.getNum());//修改剩余数量

            if(onum == body.getNum()){ //用完该物品
                item.rmvItem();//删除item
                Rdm.instance().zrem(KEY_PACKET( rid,item.getVisible()),String.valueOf(body.getId()));//删除背包中id
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(item.toPbBody().toByteString());

            SendTo(ctx,response.build());
        }



        //经验道具
        private void useExpItem(ChannelHandlerContext ctx, Item item ,int use_num ){
            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item.getItem_id()).split(",");

            Role role = Role.getRoleById(item.getRole());
            long add_exp = Integer.parseInt(itemconf[3]) * use_num; //单价 * 数量

            long total = role.getExp() + add_exp;

            int level = role.getLevel();
            //级别计算
            String top = ExcelCache.inner().get("觅我游戏版配置表","账号等级",0);
            String[] lev = top.split(",");
            int top_level = Integer.parseInt( lev[lev.length-1] );//顶级

            String level_record =  ExcelCache.inner().get("觅我游戏版配置表","账号等级",level);
            long need_exp = Long.parseLong(level_record.split(",")[1]) ;//当前级别升级所需经验
            long exp = total;
            while (exp >= need_exp ){
                if(level == top_level){
                    break;
                }
                level += 1;
                exp = exp - need_exp;
                String l_record =  ExcelCache.inner().get("觅我游戏版配置表","账号等级",level);
                need_exp = Long.parseLong(l_record.split(",")[1]) ;//当前级别升级所需经验
            }

            //推送升级消息
            role.setLevel(level);
            role.setExp(exp);

            //构造刷新数据
            PbBodyFreshData.ExpUp.Builder numbers = PbBodyFreshData.ExpUp.newBuilder();
            numbers.setExp(exp);
            numbers.setLevel(level);

            //推送刷新经验
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(1);
            fresh.setCode(ZERO);
            fresh.setBody(numbers.build().toByteString());
            SendTo(ctx,fresh.build());
        }

        //金币道具
        private void useGoldItem(ChannelHandlerContext ctx, Item item ,int use_num){
            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item.getItem_id()).split(",");

            Role role = Role.getRoleById(item.getRole());
            long add_gold = Integer.parseInt(itemconf[3]) * use_num; //单价 * 数量
            long total = role.getGold() + add_gold;
            role.setGold(total);

            //构造刷新数据
            PbBodyFreshData.Numbers.Builder numbers = PbBodyFreshData.Numbers.newBuilder();
            numbers.setNum(total);

            //推送刷新金币
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(0);
            fresh.setCode(ZERO);
            fresh.setBody(numbers.build().toByteString());
            SendTo(ctx,fresh.build());
        }

        //体力道具
        private void useHpItem(ChannelHandlerContext ctx, Item item ,int use_num ){
            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item.getItem_id()).split(",");

            Role role = Role.getRoleById(item.getRole());
            int add_hp = Integer.parseInt(itemconf[3]) * use_num; //单价 * 数量
            int total = role.getHp() + add_hp;
            role.setHp(total);

            //构造刷新数据
            PbBodyFreshData.Numbers.Builder numbers = PbBodyFreshData.Numbers.newBuilder();
            numbers.setNum(total);

            //推送刷新体力
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(2);
            fresh.setCode(ZERO);
            fresh.setBody(numbers.build().toByteString());
            SendTo(ctx,fresh.build());
        }

        //心情道具
        private void useMoodItem(ChannelHandlerContext ctx, Item item ,int use_num ){
            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item.getItem_id()).split(",");

            Role role = Role.getRoleById(item.getRole());
            int add_mood = Integer.parseInt(itemconf[3]) * use_num; //单价 * 数量
            int total = role.getMood() + add_mood;
            role.setMood(total);

            //构造刷新数据
            PbBodyFreshData.Numbers.Builder numbers = PbBodyFreshData.Numbers.newBuilder();
            numbers.setNum(total);

            //推送刷新心情
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(3);
            fresh.setCode(ZERO);
            fresh.setBody(numbers.build().toByteString());
            SendTo(ctx,fresh.build());
        }

















}
