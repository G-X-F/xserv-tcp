package com.kunda.engine.tools.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ClientRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public static ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.write(login_0());
        ctx.flush();

    }


    //------------------------------------------------------------------------------------------------------------------





    /**
     * 构造Tcp登录信息请求消息体
     * */
    public static  ProtobuffFrame.Request login_0() {
        PbBodyTcpLogin.LoginReq.Builder body = PbBodyTcpLogin.LoginReq.newBuilder();
        body.setUid(100006);//uid
        body.setToken("98771FFAE9972BA083478BB79A98BCE523B255AC0A91B3E4");

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_login.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


    /**
     * 构造创建角色信息请求消息体
     * */
    public static  ProtobuffFrame.Request create_role_0() {
        PbBodyRole.CreateRole.Builder body = PbBodyRole.CreateRole.newBuilder();
        body.setNickname("疾风剑客");
        body.setSex(0);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_create_role.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


    /**
     * 构造进入游戏信息请求消息体
     * */
    public static  ProtobuffFrame.Request enter_game_0() {
        PbBodyRole.SelectRole.Builder body = PbBodyRole.SelectRole.newBuilder();
        body.setId(1000010052);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_enter_game.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    public static  ProtobuffFrame.Request  create_buddy_4() {
        PbBodyBuddy.LockBuddy.Builder body = PbBodyBuddy.LockBuddy.newBuilder();
        body.setBid(703322);
        body.setLock(1);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_create_buddy.id());
        msg.setSub(4);
         msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //背包系统->添加物品
    public static  ProtobuffFrame.Request packet_sys_0() {
        PbBodyPacket.CreatItem.Builder body = PbBodyPacket.CreatItem.newBuilder();
        body.setItemid(1150001);
        body.setNum(5);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_packet_sys.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //背包系统->销售物品
    public static  ProtobuffFrame.Request packet_sys_2() {
        PbBodyPacket.SellItems.Builder body = PbBodyPacket.SellItems.newBuilder();
        body.setId(1120001086);
        body.setNum(10);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_packet_sys.id());
        msg.setSub(2);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //背包系统->打开背包
    public static  ProtobuffFrame.Request packet_sys_1() {
        PbBodyPacket.SelectPacket.Builder body = PbBodyPacket.SelectPacket.newBuilder();
        body.setPkt(1);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_packet_sys.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //背包系统->整理背包
    public static  ProtobuffFrame.Request packet_sys_3() {
        PbBodyPacket.SelectPacket.Builder body = PbBodyPacket.SelectPacket.newBuilder();
        body.setPkt(1);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_packet_sys.id());
        msg.setSub(3);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //物品系统->查看详情
    public static  ProtobuffFrame.Request item_sys_0() {
        PbBodyItemSys.ItemDetail.Builder body = PbBodyItemSys.ItemDetail.newBuilder();
        body.setId(1120001238);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_item_sys.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }

    //物品系统->使用物品
    public static  ProtobuffFrame.Request item_sys_1() {
        PbBodyPacket.UseItems.Builder body = PbBodyPacket.UseItems.newBuilder();
        body.setId(1150001609);
        body.setNum(1);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.t_item_sys.id());
        msg.setSub(1);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }













    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvalidProtocolBufferException {


        if(msg instanceof ProtobuffFrame.Response) { //http处理
            ProtobuffFrame.Response resp =    (ProtobuffFrame.Response)msg;
            if(resp.getCode() > 0){
                logger.info("=>["+ CMD.getCmdById(resp.getCmd())+"#"  + resp.getSub() +"]["+ resp.getCode() + "] [" + resp.getBody().toStringUtf8() + "]");
            }else {
                parseResponse(ctx,resp);
            }
        }
    }



    private void parseResponse( ChannelHandlerContext ctx ,ProtobuffFrame.Response resp ) throws InvalidProtocolBufferException {
        String logs = "=>["+ CMD.getCmdById(resp.getCmd())+"#"  + resp.getSub() +"]["+ resp.getCode() + "] [";

        switch (CMD.getCmdById(resp.getCmd())){
            case t_heartbeat:
                PbBodyHeart.BeatResp heartresp = PbBodyHeart.BeatResp.parseFrom(resp.getBody());
                logger.info(logs + heartresp.toString().replace("\n"," ") +"][" + (System.currentTimeMillis() -heartresp.getTk()) +"]" );
                break;
            case t_login:
                logger.info(logs + PbBodyRole.RoleList.parseFrom(resp.getBody()).toString().replace("\n"," ") +"]");

                //登录之后操作
                ctx.write(enter_game_0());
                ctx.flush();

                break;
            case h_user_info:
                logger.info(logs+ PbBodyUser.UserInfo.parseFrom(resp.getBody()).getAccount()+"]");
                break;
            case t_create_role:
                logger.info(logs+ PbBodyRole.Role.parseFrom(resp.getBody()).getNickname()+"]");
                break;
            case t_enter_game:
                logger.info(logs+ PbBodyRole.EnterGame.parseFrom(resp.getBody())+"]");
                //进入游戏之后操作
                ctx.write(item_sys_1());
                ctx.flush();
                break;
            case t_role_skill:
                logger.info(logs+ PbBodySkill.SkillList.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_beauty_contest:
                logger.info(logs);

                for( String rec : PbBodyBeautyContest.Contest.parseFrom(resp.getBody()).getRecordList()){
                    logger.info(rec );
                }
                break;
            case t_create_buddy:
                logger.info(logs+ PbBodyBuddy.Buddy.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_pve_stock:
                logger.info(logs+ PbBodyBuddy.PveStock.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_buddy_piece:
                logger.info(logs+ PbBodyComm.StrResp.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_star_level_up:
                logger.info(logs+ PbBodyBuddy.Buddy.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_system_mail:
                logger.info(logs+ PbBodyMail.SysMail.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_packet_sys:
                logger.info(logs+ PbBodyPacket.Items.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_item_sys:
                logger.info(logs+ PbBodyPacket.Item.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;
            case t_fresh_data:
                logger.info(logs+ PbBodyFreshData.Numbers.parseFrom(resp.getBody()).toString().replace("\n"," ")+"]");
                break;



        }






    }





    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)  {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        cause.printStackTrace();
        ctx.close();
    }
}