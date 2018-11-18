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
import static com.kunda.engine.model.entity.mj.Packet.*;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10012;


/**
 * 背包系统
 * */
public class PacketSystemRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(PacketSystemRequestHandler.class);

        public static int requestId = CMD.t_packet_sys.id();



        /**
         * 背包系统
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://向背包加入物品
                    inputItem(ctx);
                    break;
                case 1://打开背包
                    openpacket(ctx);
                    break;
                case 2://出售物品
                    sellitems(ctx);
                    break;
                case 3://背包整理
                    sortpacket(ctx);
                    break;
                case 4://一键出售装备
                    sellall(ctx);
                    break;

                default:
                    openpacket(ctx);

            }


        }




        //整理背包
        private void sortpacket(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID
            PbBodyPacket.SelectPacket body = PbBodyPacket.SelectPacket.parseFrom(request.getBody());

            PbBodyPacket.Items items =  SortPacketItems( rid , body.getPkt());

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(items.toByteString());

            SendTo(ctx,response.build());
        }


        //向背包插入物品
        private void inputItem(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID

            //解析参数
            PbBodyPacket.CreatItem body = PbBodyPacket.CreatItem.parseFrom(request.getBody());

            //添加该物品到背包
            long id = addItem2Packet( rid,body.getItemid() ,body.getNum() );
            Item itm = Item.getItemById(rid,id);

            PbBodyPacket.Items items =  getPacketItems( rid , itm.getVisible());


            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(items.toByteString());

            SendTo(ctx,response.build());
        }



        //打开背包
        private void openpacket(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID

            PbBodyPacket.SelectPacket body = PbBodyPacket.SelectPacket.parseFrom(request.getBody());

            PbBodyPacket.Items items =  getPacketItems( rid , body.getPkt());

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(items.toByteString());

            SendTo(ctx,response.build());
        }




        //销售物品
        private void sellitems(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID
            PbBodyPacket.SellItems body = PbBodyPacket.SellItems.parseFrom(request.getBody());

            Item item = Item.getItemById(rid,body.getId());
            int onum = item.getNum();
            if(onum < body.getNum()){//数量错误
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"sell item  wrong number!"));
                return;
            }

            item.setNum(onum - body.getNum() );


            //增加金币
            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item.getItem_id()).split(",");
            int  price = Integer.parseInt(itemconf[7]) * body.getNum();
            Role role = Role.getRoleById(rid);
            long num = role.getGold() + price;
            role.setGold(num);


            //构造刷新数据
            PbBodyFreshData.Numbers.Builder numbers = PbBodyFreshData.Numbers.newBuilder();
            numbers.setNum(num);

            //推送刷新金币
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(0);
            fresh.setCode(ZERO);
            fresh.setBody(numbers.build().toByteString());
            SendTo(ctx,fresh.build());


            //如果整堆物品全部卖掉，删除该物品
            if(onum == body.getNum()){
                item.rmvItem();
                Rdm.instance().zrem(KEY_PACKET( rid,item.getVisible()),String.valueOf(body.getId()));
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(item.toPbBody().toByteString());

            SendTo(ctx,response.build());



        }

        //一键出售
        private void sellall(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong (ctx.channel().attr(CTX_ATTR_RID).get());//角色ID
            PbBodyPacket.SelectPacket body = PbBodyPacket.SelectPacket.parseFrom(request.getBody());

            PbBodyPacket.Items items =  SortPacketItems( rid , body.getPkt());

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(items.toByteString());

            SendTo(ctx,response.build());
        }



}
