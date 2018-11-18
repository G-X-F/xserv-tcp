package com.kunda.engine.handles.socket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.entity.mj.Role;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.tools.http.HttpRequestUtil;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Role.getRoleById;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ExcelName.en_table;

/**
 *角色比美技能接口
 * */
public class RoleSkillRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(RoleSkillRequestHandler.class);

        public static int requestId = CMD.t_role_skill.id();



        /**
         * 比美技能增加、移除、升级
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {



            int type = request.getSub();//获取请求分支
            switch (type){
                case 0://增加技能
                    addSkill(ctx);
                    break;
                case 1://升级技能
                    upgSkill(ctx);
                    break;
                case 2://移除技能
                    rmvSkill(ctx);
                case 3://技能列表
                    skillList(ctx);
                    break;


                    default:
                        addSkill(ctx);




            }


        }



        /**
         * 增加比美技能
         * */

        private void addSkill(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            Role role = getRoleById(Long.parseLong(rid));

            PbBodySkill.SkillReq body = PbBodySkill.SkillReq.parseFrom(request.getBody());
            role.addskill(body.getId());//增加比美技能id




            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);

            SendTo(ctx,response.build());


        }


        /**
         * 移除比美技能
         * */
        private void rmvSkill(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            Role role = getRoleById(Long.parseLong(rid));

            PbBodySkill.SkillReq body = PbBodySkill.SkillReq.parseFrom(request.getBody());
            role.rmvskill(body.getId());//增加比美技能id




            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);

            SendTo(ctx,response.build());


        }


        /**
         * 升级比美技能
         * */
        private void upgSkill(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            Role role = getRoleById(Long.parseLong(rid));

            PbBodySkill.SkillReq body = PbBodySkill.SkillReq.parseFrom(request.getBody());
            role.upgskill(body.getId());//增加比美技能id

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);

            SendTo(ctx,response.build());
        }




        /**
         * 比美技能列表
         * */
        private void skillList(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            Role role = getRoleById(Long.parseLong(rid));

            PbBodySkill.SkillList.Builder skillList = PbBodySkill.SkillList.newBuilder();
            List<String> records =  ExcelCache.inner().mult("觅我游戏版配置表","技能配置表",role.getSkills());

            for(int i =0; i< role.getSkills().length;i++){
                String record =  records.get(i);
                skillList.addSkills( conf2Skill(record) ) ;
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(skillList.build().toByteString());

            SendTo(ctx,response.build());

        }


    /**
     * 向配置服务器发送Post请求
     * */
    public static ProtobuffFrame.Response ConfServerPost(ProtobuffFrame.Request request ) throws InvalidProtocolBufferException {
        String url = "http://"+ Conf.inner().get("config.server"); //配置服务器地址
        byte[] result = HttpRequestUtil.sendPost(url,request.toByteArray(),"application/octet-stream;charset=utf-8");
        return ProtobuffFrame.Response.parseFrom( result );
    }










        /**
         * 配置数据转换称Protobuffer对象
         * */

        public static PbBodySkill.Skill conf2Skill(String record){
            PbBodySkill.Skill.Builder skill = PbBodySkill.Skill.newBuilder();
            String[] value = record.split(",");
            skill.setId(Integer.parseInt(value[0]));
            skill.setType(Integer.parseInt(value[1]));
            skill.setClazz(Integer.parseInt(value[2]));
            skill.setSeq(Integer.parseInt(value[3]));
            skill.setNameid(Integer.parseInt(value[4]));
            skill.setName((value[5]));
            skill.setStartlevel(Integer.parseInt(value[6]));
            skill.setCastphase(Integer.parseInt(value[7]));
            skill.setCasttarget(Integer.parseInt(value[8]));
            skill.setCastrate(Integer.parseInt(value[9]));
            skill.setCastsort(Integer.parseInt(value[10]));
            skill.setCoolround(Integer.parseInt(value[11]));
            skill.setSkilldesc(value[12]);
            skill.setIconid(Integer.parseInt(value[13]));
            skill.setTextid(Integer.parseInt(value[14]));
            skill.setLevel(Integer.parseInt(value[15]));
            skill.setNextid(Integer.parseInt(value[16]));
            skill.setUpitemid(Integer.parseInt(value[17]));
            skill.setUpitemnum(Integer.parseInt(value[18]));
            skill.setEffect1Id(Integer.parseInt(value[19]));
            skill.setEffect1Rate(Integer.parseInt(value[20]));
            /*skill.setEffect2Id(Integer.parseInt(value[21]));
            skill.setEffect2Rate(Integer.parseInt(value[22]));
            skill.setEffect3Id(Integer.parseInt(value[23]));
            skill.setEffect3Rate(Integer.parseInt(value[24]));*/

            return skill.build();
        }



























}
