package com.kunda.engine.handles.socket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.core.Groups;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.entity.mj.Buddy;
import com.kunda.engine.model.entity.mj.Item;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.handles.socket.RoleSkillRequestHandler.ConfServerPost;
import static com.kunda.engine.model.entity.mj.Buddy.getBuddyById;
import static com.kunda.engine.model.entity.mj.Packet.KEY_PACKET;
import static com.kunda.engine.model.entity.mj.Packet.canPileItems;
import static com.kunda.engine.model.entity.mj.Packet.sameItems;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.CH_KEY_SERVER;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.*;


/**
 * 伙伴的升级/升星
 * */
public class StarLevelUpRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(StarLevelUpRequestHandler.class);

        public static int requestId = CMD.t_star_level_up.id();



        /**
         * 伙伴的升级/升星
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://伙伴升级
                    levelup(ctx);
                    break;
                case 1://伙伴升星
                    starup(ctx);
                    break;
                case 2://伙伴技能升级
                    skillup(ctx);
                    break;
                default:
                    levelup(ctx);

            }


        }





        //伙伴升星
        private void starup(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            //解析参数
            PbBodyBuddy.StarUp body = PbBodyBuddy.StarUp.parseFrom(request.getBody());
            int bid  = body.getBid();//要升星的伙伴id
            List<Integer> cost = body.getCostList(); //消耗的伙伴id

            Role role = getRoleById(Long.parseLong(rid));
            Buddy buddy = getBuddyById(Long.parseLong(rid),bid);

            String star_record =  ExcelCache.inner().get("伙伴数值表","升星消耗",buddy.getStar());
            String[] star = star_record.split(",");
            if(buddy.getLevel() != Integer.parseInt(star[2])){ //等级未达到升星
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"buddy level not reached!"));
                return;
            }
            if(cost.size() != Integer.parseInt(star[7])){//消耗伙伴数量不够
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"not enough buddy!"));
                return;
            }
            if(role.getGold() < Integer.parseInt(star[8])){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"not enough gold!"));
                return;
            }

            //消耗伙伴卡
            for(Integer cost_bid : cost){
                rmvMyBuddy(Long.parseLong(rid),cost_bid);
            }

            //扣除金币
            role.setGold( role.getGold() - Integer.parseInt(star[8]) );

            buddy.setStar(buddy.getStar() + 1);//星级+1
            buddy.setLevel(1);
            buddy.setExp(0);

            //达到5星级全服推送
            if(buddy.getStar() == 5){
                //功夫不负有心人，恭喜<color=#ff0000>{0}</color>成功将<color=#ff0000>{1}</color>升到{2}星！
                PbBodyMail.CreateNotice.Builder notice = PbBodyMail.CreateNotice.newBuilder();
                notice.setId(1); //通告id
                //参数
                notice.addParam(role.getNickname());//角色昵称
                notice.addParam(buddy.getName());//细胞名称
                notice.addParam(String.valueOf(buddy.getStar()));//伙伴星级

                ProtobuffFrame.Response.Builder msg = ProtobuffFrame.Response.newBuilder();
                msg.setCmd(CMD.t_system_mail.id());
                msg.setSub(1);
                msg.setCode(ZERO);
                msg.setBody(ByteString.copyFrom(notice.build().toByteArray()));

                Groups.inst().getGroup(CH_KEY_SERVER).broadcast(msg.build());
            }


            int level_index = buddy.getElement()*10000 + buddy.getStar()*1000  + buddy.getLevel();
            String bl_record =  ExcelCache.inner().get("伙伴数值表","等级表",level_index);
            String[] new_buddy =  bl_record.split(",");
            buddy.setAtk(Integer.parseInt(new_buddy[6]));
            buddy.setDef(Integer.parseInt(new_buddy[7]));
            buddy.setHp(Integer.parseInt(new_buddy[8]));

            PbBodyBuddy.BuddyList.Builder list = PbBodyBuddy.BuddyList.newBuilder();
            //我的伙伴列表
            Map<String,String> mp = myBuddyList(Long.parseLong(rid));
            for(String buid : mp.keySet()){
                Buddy budy = getBuddyById(Long.parseLong(rid),Integer.parseInt(buid));
                if(budy != null) {
                    list.addBuddys(budy.toPbBody());
                }
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(list.build().toByteString());

            SendTo(ctx,response.build());
        }




    //伙伴技能升级
    private void skillup(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
        String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
        //解析参数
        PbBodyBuddy.StarUp body = PbBodyBuddy.StarUp.parseFrom(request.getBody());
        int bid  = body.getBid();//要升星的伙伴id
        List<Integer> cost = body.getCostList(); //消耗的伙伴id

        Role role = getRoleById(Long.parseLong(rid));
        Buddy buddy = getBuddyById(Long.parseLong(rid),bid);

        String skill_record =  ExcelCache.inner().get("技能表","技能等级",buddy.getPveskill());
        String[] skill = skill_record.split(",");


        if(cost.size() != Integer.parseInt(skill[6])){//消耗伙伴数量不够
            SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"not enough buddy!"));
            return;
        }
        if(role.getGold() < Integer.parseInt(skill[7])){
            SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"not enough gold!"));
            return;
        }

        //是否是需要的伙伴类型
        for(Integer cost_bid : cost){
            Buddy cost_buddy = getBuddyById(Long.parseLong(rid),cost_bid);
            if(cost_buddy.getCell_id() != Integer.parseInt(skill[5])){ //不是需要的伙伴类型
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"the cell of buddy is wrong!"));
                return;
            }
        }

        //消耗伙伴卡
        for(Integer cost_bid : cost){
            rmvMyBuddy(Long.parseLong(rid),cost_bid);
        }

        //扣除金币
        role.setGold( role.getGold() - Integer.parseInt(skill[7]) );

        buddy.setPveskill(Integer.parseInt(skill[4]));//技能+1

        int level_index = buddy.getElement()*10000 + buddy.getStar()*1000  + buddy.getLevel();
        String bl_record =  ExcelCache.inner().get("伙伴数值表","等级表",level_index);
        String[] new_buddy =  bl_record.split(",");
        buddy.setAtk(Integer.parseInt(new_buddy[6]));
        buddy.setDef(Integer.parseInt(new_buddy[7]));
        buddy.setHp(Integer.parseInt(new_buddy[8]));

        PbBodyBuddy.BuddyList.Builder list = PbBodyBuddy.BuddyList.newBuilder();
        //我的伙伴列表
        Map<String,String> mp = myBuddyList(Long.parseLong(rid));
        for(String buid : mp.keySet()){
            Buddy budy = getBuddyById(Long.parseLong(rid),Integer.parseInt(buid));
            if(budy != null) {
                list.addBuddys(budy.toPbBody());
            }
        }

        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        response.setBody(list.build().toByteString());

        SendTo(ctx,response.build());
    }



        //伙伴升级经验
        private void levelup(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            long rid = Long.parseLong(ctx.channel().attr(CTX_ATTR_RID).get()) ;//角色ID

            //解析参数
            PbBodyBuddy.LevelUp body = PbBodyBuddy.LevelUp.parseFrom(request.getBody());
            int bid  = body.getBid();//伙伴id
            int itemid  = body.getItemid();//升级经验书配置id
            int use_num = body.getNum();//使用数量

            String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",itemid).split(",");
            String[] typeconf = ExcelCache.inner().get("背包数值表","物品类型配置表",Integer.parseInt(itemconf[2])).split(",");
            int visible = Integer.parseInt(typeconf[2]);//背包显示编号

            PbBodyFreshData.PacketItems.Builder packetItems = PbBodyFreshData.PacketItems.newBuilder(); //更新背包数据
            packetItems.setPkt(visible);

            List<Integer> list = sameItems(rid , itemid , visible);//背包中该类型的物品列表


            for(Integer id: list){
                Item item = Item.getItemById(rid,id);
                if(use_num > item.getNum()){ //该堆物品用完
                    use_num = use_num - item.getNum();
                    item.setNum(0);
                    packetItems.addIts( item.toPbBody());
                    item.rmvItem();
                    Rdm.instance().zrem(KEY_PACKET( rid,visible),String.valueOf(id));
                }else {
                    item.setNum(item.getNum() - use_num);//剩余物品
                    packetItems.addIts( item.toPbBody());
                    use_num = 0;
                    break;
                }
            }


            if(use_num != 0){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"not enough items!"));
                return;
            }

            //推送刷新背包数据
            ProtobuffFrame.Response.Builder fresh = ProtobuffFrame.Response.newBuilder();
            fresh.setCmd(CMD.t_fresh_data.id());
            fresh.setSub(4);
            fresh.setCode(ZERO);
            fresh.setBody(packetItems.build().toByteString());
            SendTo(ctx,fresh.build());


            long add_exp = Integer.parseInt(itemconf[3]) * body.getNum();//增加伙伴经验值
            Buddy buddy = getBuddyById(rid,bid);
            long exp =   buddy.getExp()+ add_exp;

            int level = buddy.getLevel();

            String star_record =  ExcelCache.inner().get("伙伴数值表","升星消耗",buddy.getStar());
            int level_limit = Integer.parseInt(star_record.split(",")[2]);//等级上限

            if(level >= level_limit){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10013,"buddy level reached top!"));
                return;
            }

            int buddy_level_id = buddy.getElement()*10000 + buddy.getStar()*1000 + level;
            String level_record =  ExcelCache.inner().get("伙伴数值表","等级表",buddy_level_id);
            long need_exp = Long.parseLong(level_record.split(",")[5]) ;//当前级别升级所需经验
            while (exp >= need_exp ){
                if(level >= level_limit){
                    break;
                }
                level += 1;
                exp = exp - need_exp;
                String l_record =  ExcelCache.inner().get("伙伴数值表","等级表",buddy_level_id);
                need_exp = Long.parseLong(l_record.split(",")[5]) ;//当前级别升级所需经验

            }

            buddy.setExp(exp);
            buddy.setLevel(level);
            int level_index = buddy.getElement()*10000 + buddy.getStar()*1000  + buddy.getLevel();

            String bl_record =  ExcelCache.inner().get("伙伴数值表","等级表",level_index);
            String[] new_buddy =  bl_record.split(",");
            buddy.setAtk(Integer.parseInt(new_buddy[6]));
            buddy.setDef(Integer.parseInt(new_buddy[7]));
            buddy.setHp(Integer.parseInt(new_buddy[8]));

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(buddy.toPbBody().toByteString());

            SendTo(ctx,response.build());
        }























}
