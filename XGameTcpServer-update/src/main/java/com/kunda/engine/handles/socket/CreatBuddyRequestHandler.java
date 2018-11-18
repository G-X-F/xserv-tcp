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

import java.util.Map;
import java.util.Random;

import static com.kunda.engine.common.fun.Avatas.ArrayAdd;
import static com.kunda.engine.common.fun.Avatas.ArrayCount;
import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Buddy.createBuddyById;
import static com.kunda.engine.model.entity.mj.Buddy.getBuddyById;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.*;


public class CreatBuddyRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(CreatBuddyRequestHandler.class);

        public static int requestId = CMD.t_create_buddy.id();



        /**
         * 创建伙伴
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://随机抽卡
                    randombuddy(ctx);
                    break;
                case 1://指定创建
                    creatbuddy(ctx);
                    break;
                case 2://删除伙伴
                    rmvbuddy(ctx);
                    break;
                case 3://伙伴列表
                    buddylist(ctx);
                    break;
                case 4://伙伴加锁/解锁
                    lockbuddy(ctx);
                    break;
                default:
                    randombuddy(ctx);

            }


        }




        //指定创建伙伴
        private void creatbuddy(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //解析参数
            PbBodyBuddy.CreateBuddy body = PbBodyBuddy.CreateBuddy.parseFrom(request.getBody());
            String itemid = body.getItemid();//物品id(背包数值/物品配置表) 2100111
            //背包数值
            String item_record =  ExcelCache.inner().get("背包数值表","物品配置表",Integer.parseInt(itemid));
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
            String cid =init[1]; String level =init[2];String star =init[3]; //伙伴攻防信息初始化 => 细胞id/等级/星级
            String buddy_record =  ExcelCache.inner().get("伙伴数值表","伙伴",Integer.parseInt(cid));
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

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(buddy.toPbBody().toByteString());

            SendTo(ctx,response.build());
        }



    //随机抽卡
    private void randombuddy(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
        String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

        //随机伙伴初始化id
        String rand_record =  ExcelCache.inner().get("伙伴数值表","碎片合成",0);
        String[] rand_id =  rand_record.split(",");
        String cell_id = rand_id[new Random().nextInt(rand_id.length)];

        String piece_record =  ExcelCache.inner().get("伙伴数值表","碎片合成",Integer.parseInt(cell_id));
        String itemid = piece_record.split(",")[4];

        //背包数值
        String item_record =  ExcelCache.inner().get("背包数值表","物品配置表",Integer.parseInt(itemid));
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
        String cid =init[1]; String level =init[2];String star =init[3]; //伙伴攻防信息初始化 => 细胞id/等级/星级
        String buddy_record =  ExcelCache.inner().get("伙伴数值表","伙伴",Integer.parseInt(cid));
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

        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        response.setBody(buddy.toPbBody().toByteString());

        SendTo(ctx,response.build());
    }


    //伙伴仓库列表
    private void buddylist(ChannelHandlerContext ctx) {
        String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

        PbBodyBuddy.BuddyList.Builder list = PbBodyBuddy.BuddyList.newBuilder();
        //加入到我的伙伴仓库
        Map<String,String> mp = myBuddyList(Long.parseLong(rid));
        for(String bid : mp.keySet()){
            Buddy buddy = getBuddyById(Long.parseLong(rid),Integer.parseInt(bid));
            if(buddy != null) {
                list.addBuddys(buddy.toPbBody());
            }
        }

        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        response.setBody(list.build().toByteString());

        SendTo(ctx,response.build());
    }


    //伙伴加锁
    private void lockbuddy(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
        String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
        //解析参数
        PbBodyBuddy.LockBuddy body = PbBodyBuddy.LockBuddy.parseFrom(request.getBody());
        int bid = body.getBid();//伙伴ID

        Buddy buddy = getBuddyById(Long.parseLong(rid),bid);
        buddy.lock(body.getLock());

        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        response.setBody(buddy.toPbBody().toByteString());
        SendTo(ctx,response.build());

    }



    //从仓库移除伙伴
    private void rmvbuddy(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
        String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
        //解析参数
        PbBodyBuddy.DeleteBuddy body = PbBodyBuddy.DeleteBuddy.parseFrom(request.getBody());
        int bid = body.getBid();//伙伴ID

        rmvMyBuddy( Long.parseLong(rid), bid);

        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);

        SendTo(ctx,response.build());
    }















    //初始化6个骰面  1剑 2盾 3星 4电
    public static int initdice( String[] init ){
        //5~18 初始攻数量	初始防数量	初始星数量	初始电数量	攻数量上限	防数量上限	星数量上限	电数量上限	攻权重	防权重	星权重	电权重	2个权重衰减	3个权重衰减
        Integer[] dice = new Integer[0];
        //攻击预设
        for(int i=0;i< Integer.parseInt(init[5]) ; i++ ){
            dice = ArrayAdd(dice,1);
        }
        //盾牌预设
        for(int i=0;i< Integer.parseInt(init[6]) ; i++ ){
            dice = ArrayAdd(dice,2);
        }
        //星星预设
        for(int i=0;i< Integer.parseInt(init[7]) ; i++ ){
            dice = ArrayAdd(dice,3);
        }
        //闪电预设
        for(int i=0;i< Integer.parseInt(init[8]) ; i++ ){
            dice = ArrayAdd(dice,4);
        }

        int len = 6-dice.length;
        //剩余部分按权重补齐
        for(int i = 0; i< len;i++ ){
            int atk_pri = Integer.parseInt(init[13]);//攻击权重
            if(ArrayCount(dice,1) == 2){
                atk_pri =  Integer.parseInt(init[13]) * Integer.parseInt(init[17])/100;//已存在2个权重衰减
            }
            if(ArrayCount(dice,1) == 3){
                atk_pri =  Integer.parseInt(init[13]) * Integer.parseInt(init[18])/100;//已存在3个权重衰减
            }

            int def_pri = Integer.parseInt(init[14]);//盾牌权重
            if(ArrayCount(dice,2) == 2){
                def_pri =  Integer.parseInt(init[14]) * Integer.parseInt(init[17])/100;//已存在2个权重衰减
            }
            if(ArrayCount(dice,2) == 3){
                def_pri =  Integer.parseInt(init[14]) * Integer.parseInt(init[18])/100;//已存在3个权重衰减
            }

            int star_pri = Integer.parseInt(init[15]);//星星权重
            if(ArrayCount(dice,3) == 2){
                star_pri =  Integer.parseInt(init[15]) * Integer.parseInt(init[17])/100;//已存在2个权重衰减
            }
            if(ArrayCount(dice,3) == 3){
                star_pri =  Integer.parseInt(init[15]) * Integer.parseInt(init[18])/100;//已存在3个权重衰减
            }

            int bolt_pri = Integer.parseInt(init[16]);//闪电权重
            if(ArrayCount(dice,4) == 2){
                bolt_pri =  Integer.parseInt(init[16]) * Integer.parseInt(init[17])/100;//已存在2个权重衰减
            }
            if(ArrayCount(dice,4) == 3){
                bolt_pri =  Integer.parseInt(init[16]) * Integer.parseInt(init[18])/100;//已存在3个权重衰减
            }

            int rand = new Random().nextInt(100);

            if(100>rand &&  rand>=(100 - atk_pri)){
                dice = ArrayAdd(dice,1);
            }
            if((100 - atk_pri) > rand &&  rand >=(100 - atk_pri- def_pri)){
                dice = ArrayAdd(dice,2);
            }
            if((100 - atk_pri- def_pri) > rand &&  rand >=(100 - atk_pri- def_pri -star_pri)){
                dice = ArrayAdd(dice,3);
            }
            if((100 - atk_pri- def_pri -star_pri) > rand &&  rand >=(100 - atk_pri- def_pri -star_pri - bolt_pri)){
                dice = ArrayAdd(dice,4);
            }

        }

      int c1=  ArrayCount(dice,1);//刀的个数
      int c2=  ArrayCount(dice,2);//盾的个数
      int c3=  ArrayCount(dice,3);//星的个数
      int c4=  ArrayCount(dice,4);//电的个数


        return  c1*1000 + c2* 100 + c3 * 10 + c4;
    }





























}
