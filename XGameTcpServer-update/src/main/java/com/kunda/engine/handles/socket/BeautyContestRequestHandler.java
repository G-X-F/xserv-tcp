package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.entity.mj.BeautyStat;
import com.kunda.engine.model.entity.mj.Rater;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.util.Random;

import static com.kunda.engine.common.fun.Avatas.ArrayRemove;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.handles.socket.RoleSkillRequestHandler.ConfServerPost;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.ZERO;


public class BeautyContestRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(BeautyContestRequestHandler.class);

        public static int requestId = CMD.t_beauty_contest.id();


        private Role offensive = null;//先手
        private Role defensive = null;//后手

        private BeautyStat offenstat;//先手方状态
        private BeautyStat defenstat;//后手方状态

        private Rater[] offscore;//先手方评分
        private Rater[] defscore;//后手方评分

        private int[] result;//比美结果 [0:0]

        private int round;//回合

        private PbBodyBeautyContest.Round.Builder actions;
        private PbBodyBeautyContest.Contest.Builder records;



        /**
         * 角色比美
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            Role role = getRoleById(Long.parseLong(rid));//已方角色

            //解析数据
            PbBodyRole.SelectRole body = PbBodyRole.SelectRole.parseFrom(request.getBody());
            Role target = getRoleById(body.getId());//比美对手角色

            offenstat = new BeautyStat();//先手方状态
            defenstat = new BeautyStat();//后手方状态

            offscore = new Rater[3];//先手方评分
            defscore = new Rater[3];//后手方评分
            result = new int[]{0,0};//比美结果 [0:0]

            actions = PbBodyBeautyContest.Round.newBuilder();
            records= PbBodyBeautyContest.Contest.newBuilder();


            //先手判断
            first_hand(role,target);

            //评委打分
            rater_score();


            records.addRecord("最终结果  " + result[0] + " : "+ result[1]  + "\n");


            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(records.build().toByteString());

            SendTo(ctx,response.build());


        }



        //先手判断
        private void first_hand(Role me,Role target) throws InvalidProtocolBufferException {
            // 1⃣ 根据实力值判断先手,默认自己是先手
            offensive = me; //先手方
            defensive = target;//后手方
            if(me.charm() <= target.charm() ){ //魅力值高的先手
                offensive = target;
                defensive = me;
            }
            records.addRecord("先手方["+offensive.getId()+ "]  后手方["+defensive.getId() +"]\n");
        }


        //阶段逻辑
        private void step_logic(int step) throws InvalidProtocolBufferException {

            // 2⃣ 先手方遍历被动技能=>判断被动技能权重=>释放被动技能
            skill_logic(offensive,offensive.passiveSkill(step));

            // 3⃣ 后手方遍历被动技能=>判断被动技能权重=>释放被动技能
            skill_logic(defensive,defensive.passiveSkill(step));

            // 4⃣ 先手方遍历主动技能=>判断主动技能权重=>释放主动技能
            skill_logic(offensive,offensive.activeSkill(step));

            // 5⃣ 后手方遍历主动技能=>判断主动技能权重=>释放主动技能
            skill_logic(defensive,defensive.activeSkill(step));


        }

        //技能释放逻辑
        private void skill_logic(Role role,PbBodySkill.SkillList list) throws InvalidProtocolBufferException {
            if(getstat(role).isSleep()){
                records.addRecord("["+role.getId()+ "]处于[沉睡]状态");
                return;
            }

            if(list.getSkillsList().size() == 1){
                PbBodySkill.Skill skill = list.getSkillsList().get(0);
                //1)记录技能释放行为
                PbBodyBeautyContest.Action.Builder action = PbBodyBeautyContest.Action.newBuilder();
                action.setType(0);//动作类型
                action.setId(role.getId());//动作主体
                //技能释放目标 1为自己 2为敌方
                action.setTarget( skill.getCasttarget() == 1 ? role.getId(): opponent(role).getId() );
                //释放技能名称
                action.setSkill(skill.getId());
                actions.addAction(action);//增加到回合动作记录
                records.addRecord("["+role.getId()+ "]向["+ (skill.getCasttarget() == 1 ? "自己":"敌方") +  "]释放技能[" +skill.getName()+"]");

                //2)记录技能生效行为
                switch (effect2type(skill.getEffect1Id())){
                    case 1: //buff效果
                        addbuff(role,skill);
                        break;
                    case 2: //免疫效果
                        addImmu(role,skill);
                        break;
                    default:
                }
            }else {
                records.addRecord("["+role.getId()+ "]技能触发失败");
            }
        }



        //评委打分
        private void rater_score() throws InvalidProtocolBufferException {
            //5个评委中选3个
            Integer[] raters = {1,2,3,4,5};
            for(int i = 0; i< 3; i++ ){ //3个回合
                round = i;

                //第一阶段
                step_logic(1);

                //第二阶段
                step_logic(2);

                Random random = new Random();
                int rater =  raters[random.nextInt(raters.length)];//找出一个评委
                int oscore = offensive.raterscore(rater,offenstat.values());//对先手方给出评分
                int dscore = defensive.raterscore(rater,defenstat.values());//对后手方给出评分

                offscore[i] = new Rater(rater,oscore);//先手方记录得分
                defscore[i] = new Rater(rater,dscore);//后手方记录得分

                records.addRecord("评委"+raters[i]+"对["+offensive.getId()+"]打分 " + offscore[i].getScore());
                records.addRecord("评委"+raters[i]+"对["+defensive.getId()+"]打分 " + defscore[i].getScore());

                //第三阶段
                step_logic(3);

                if(offscore[i].getScore() >= defscore[i].getScore()){ //先手方赢1局
                    result[0] += 1;
                }else {
                    result[1] += 1;
                }

                records.addRecord("当前比分 [" + result[0] + " : "+ result[1]  + "]\n");


                raters =  ArrayRemove(raters,rater);//移除已经上场的评委

                if(Math.abs(result[0]- result[1]) > 1 ){ //比分超过2分直接决胜负
                    break;
                }
            }


        }


        //获得对手
        private Role opponent(Role role){
            if(role.getId() == offensive.getId()){
                return defensive;
            }
            if(role.getId() == defensive.getId()){
                return offensive;
            }
            return defensive;
        }

        //获得对应的状态
        private  BeautyStat getstat(Role role ){
            if(role.getId() == offensive.getId()){
                return defenstat;
            }
            if(role.getId() == defensive.getId()){
                return offenstat;
            }
            return defenstat;

        }


        //增加buff状态
        private void addbuff(Role role,PbBodySkill.Skill skill) throws InvalidProtocolBufferException {
            PbBodyBuff.Buff buff = buffConf(skill.getEffect1Id());

            if((skill.getCasttarget() == 1 && role.getId() == offensive.getId()) || (skill.getCasttarget() == 2 && role.getId() == defensive.getId()) ){//向先手方增加buff
                if(offenstat.getImmu_stat().contains(buff.getStatuid())){
                    records.addRecord("["+offensive.getId()+"]对状态["+  buffStatusConf(buff.getStatuid()).getName()  +"]免疫");
                }else{
                    if(buff.getStatuid() == 14){ //催眠buff
                        offenstat.setSleep(true);
                    }
                    offenstat.addBuff(buff); //如果没有免疫该状态，buff增加成功
                    records.addRecord("["+offensive.getId()+"]获得["+  buffStatusConf(buff.getStatuid()).getName()  +"]状态");
                    if(buff.getStatuid() == 7){ //交换评分
                        Rater tmp = offscore[round];
                        offscore[round] = defscore[round];
                        defscore[round] = tmp;
                        records.addRecord("["+offensive.getId()+"]交换了评分");
                    }
                }

            }else {
                if(defenstat.getImmu_stat().contains(buff.getStatuid())){//如果没有免疫该状态，buff增加成功
                    records.addRecord("["+defensive.getId()+"]对状态["+ buffStatusConf(buff.getStatuid()).getName() +"]免疫");
                }else{
                    if(buff.getStatuid() == 14){ //催眠buff
                        defenstat.setSleep(true);
                    }
                    defenstat.addBuff(buff);
                    records.addRecord("["+defensive.getId()+"]获得["+ buffStatusConf(buff.getStatuid()).getName() +"]状态");
                    if(buff.getStatuid() == 7){ //交换评分
                        Rater tmp  = offscore[round];
                        offscore[round] = defscore[round];
                        defscore[round] = tmp;
                        records.addRecord("["+defensive.getId()+"]交换了评分");
                    }
                }

            }



        }



        //增加免疫状态
        private void addImmu(Role role,PbBodySkill.Skill skill) throws InvalidProtocolBufferException {
            PbBodyBuff.Immu immu = immuConf(skill.getEffect1Id());
            if((skill.getCasttarget() == 1 && role.getId() == offensive.getId()) || (skill.getCasttarget() == 2 && role.getId() == defensive.getId()) ){//针对自己增加免疫状态
                offenstat.addImmuStat(immu.getStatuid());
                records.addRecord("["+offensive.getId()+"]获得免疫状态["+ buffStatusConf(immu.getStatuid()).getName() +"]");
            }else {//针对自己释放
                defenstat.addImmuStat(immu.getStatuid());
                records.addRecord("["+defensive.getId()+"]获得免疫状态["+ buffStatusConf(immu.getStatuid()).getName() +"]");
            }
        }









        //根据效果id得到类型:buff、免疫、驱散
        private int effect2type(int effectid){
            String efid = String.valueOf(effectid);
            BigDecimal decimal = new BigDecimal(Math.pow(10,efid.length()-1));
            return effectid/decimal.intValue() ;
        }

        //得到buff对象
        private PbBodyBuff.Buff buffConf(int effectid) throws InvalidProtocolBufferException {
            return conf2Buff(ExcelCache.inner().get("觅我游戏版配置表","buff配置表",effectid));//得到buff配置
        }

        //得到免疫对象
        private PbBodyBuff.Immu immuConf(int effectid) throws InvalidProtocolBufferException {
            return conf2Immu(ExcelCache.inner().get("觅我游戏版配置表","免疫配置表",effectid));
        }

        //根据状态Id=> status对象
        private PbBodyBuff.BuffStatus buffStatusConf(int statuid) throws InvalidProtocolBufferException {
            return conf2BuffStatus(ExcelCache.inner().get("觅我游戏版配置表","buff状态配置表",statuid));
        }

        //配置数据转化:buff对象
        private PbBodyBuff.Buff conf2Buff(String record){
            PbBodyBuff.Buff.Builder buff = PbBodyBuff.Buff.newBuilder();
            String[] value = record.split(",");
            buff.setId(Integer.parseInt(value[0]));
            buff.setTarget(Integer.parseInt(value[1]));
            buff.setName(value[2]);
            buff.setStatuid(Integer.parseInt(value[3]));
            buff.setRound(Integer.parseInt(value[4]));
            buff.setValue(Integer.parseInt(value[5]));
            return buff.build();
        }

        //配置数据转化:Immu对象
        private PbBodyBuff.Immu conf2Immu(String record){
            PbBodyBuff.Immu.Builder immu = PbBodyBuff.Immu.newBuilder();
            String[] value = record.split(",");
            immu.setId(Integer.parseInt(value[0]));
            immu.setStatuid(Integer.parseInt(value[1]));
            immu.setRound(Integer.parseInt(value[2]));
            return immu.build();
        }

        //配置数据转化:Buff状态对象
        private PbBodyBuff.BuffStatus conf2BuffStatus(String record){
            PbBodyBuff.BuffStatus.Builder status = PbBodyBuff.BuffStatus.newBuilder();
            String[] value = record.split(",");
            status.setId(Integer.parseInt(value[0]));
            status.setType(Integer.parseInt(value[1]));
            status.setName(value[2]);
            status.setCanImmu(Integer.parseInt(value[3]));
            status.setCanDisp(Integer.parseInt(value[4]));
            status.setBeffect(Integer.parseInt(value[5]));
            status.setBufficon(Integer.parseInt(value[6]));
            return status.build();
        }



























}
