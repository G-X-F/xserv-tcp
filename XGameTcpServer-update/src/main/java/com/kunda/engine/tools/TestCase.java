package com.kunda.engine.tools;

import com.google.protobuf.ByteString;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.kunda.engine.tools.security.UnicodeUtil.tokenmaker;
import static com.kunda.engine.utils.Const.Uidbase;


public class TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);



    /**
     * 构造查询用户信息请求消息体
     * */
    public static  ProtobuffFrame.Request user_info_0() {
        PbBodyComm.StrReq.Builder body = PbBodyComm.StrReq.newBuilder();
        body.setS1("100003");//uid
        body.setS2("246EB162AAA44D5283C7F794741E7DD1D6ED8712EABD1C23");

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.h_user_info.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


    public static  ProtobuffFrame.Request  add_skill_0() {
        PbBodySkill.SkillReq.Builder body = PbBodySkill.SkillReq.newBuilder();
        body.setId(1010101);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_role_skill.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  rmv_skill_0() {
        PbBodySkill.SkillReq.Builder body = PbBodySkill.SkillReq.newBuilder();
        body.setId(1010105);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_role_skill.id());
        msg.setSub(2);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }
    public static  ProtobuffFrame.Request  upg_skill_0() {
        PbBodySkill.SkillReq.Builder body = PbBodySkill.SkillReq.newBuilder();
        body.setId(1010101);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_role_skill.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  skill_list_0() {
        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_role_skill.id());
        msg.setSub(3);
        return msg.build();
    }


    public static  ProtobuffFrame.Request  beauty_contest_0() {
        PbBodyRole.SelectRole.Builder body = PbBodyRole.SelectRole.newBuilder();
        body.setId(1000040012);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_beauty_contest.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  pve_stock_0() {
        PbBodyBuddy.PveStock.Builder body = PbBodyBuddy.PveStock.newBuilder();
        body.addId(507860);
        body.addId(128576);
        body.addId(0);
        body.addId(387972);
        body.addId(409466);
        body.addId(0);


        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_pve_stock.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  delete_buddy_0() {
        PbBodyBuddy.DeleteBuddy.Builder body = PbBodyBuddy.DeleteBuddy.newBuilder();
        body.setBid(304933);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_create_buddy.id());
        msg.setSub(2);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }



    public static  ProtobuffFrame.Request  buddy_pieces_1() {
        PbBodyBuddy.SynPiece.Builder body = PbBodyBuddy.SynPiece.newBuilder();
        body.setStar(1);
        body.setCid(24003);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_buddy_piece.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


    public static  ProtobuffFrame.Request  buddy_pieces_2() {
        PbBodyBuddy.AddPiece.Builder body = PbBodyBuddy.AddPiece.newBuilder();
        body.setCid(24003);
        body.setNum(100);


        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_buddy_piece.id());
        msg.setSub(2);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  level_up_0() {
        PbBodyBuddy.LevelUp.Builder body = PbBodyBuddy.LevelUp.newBuilder();
        body.setBid(507860);
        //body.setItemid(1211001673);


        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_star_level_up.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  create_buddy_0() {
        PbBodyBuddy.CreateBuddy.Builder body = PbBodyBuddy.CreateBuddy.newBuilder();
        body.setItemid("2400311");

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_create_buddy.id());
        msg.setSub(0);
        // msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }



    public static  ProtobuffFrame.Request  star_up_0() {
        PbBodyBuddy.StarUp.Builder body = PbBodyBuddy.StarUp.newBuilder();
        body.setBid(507860);
        body.addCost(128576);


        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_star_level_up.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //创建邮件
    public static  ProtobuffFrame.Request sys_mail_0() {
        PbBodyMail.CreatMail.Builder body = PbBodyMail.CreatMail.newBuilder();
        body.setTmpid(90001);


        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_system_mail.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


    //打开邮件
    public static  ProtobuffFrame.Request open_mail_0() {
        PbBodyMail.CreatMail.Builder body = PbBodyMail.CreatMail.newBuilder();
        body.setTmpid(90001);



        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_system_mail.id());
        msg.setSub(2);
        // msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //阅读邮件
    public static  ProtobuffFrame.Request sys_mail_3() {
        PbBodyMail.ReadMail.Builder body = PbBodyMail.ReadMail.newBuilder();
        body.setMailid(1541143656);



        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_system_mail.id());
        msg.setSub(3);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //领取邮件
    public static  ProtobuffFrame.Request get_mail_item_0() {
        PbBodyMail.ReadMail.Builder body = PbBodyMail.ReadMail.newBuilder();
        body.setMailid(1101140306);



        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_system_mail.id());
        msg.setSub(4);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


























    public static void main(String args[]) {
        Conf.inner().add("token.pri.key","Keiewoe0");
        System.out.println(tokenmaker("100006"));







    }


}

