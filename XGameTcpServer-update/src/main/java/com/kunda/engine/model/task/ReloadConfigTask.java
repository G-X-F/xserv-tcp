package com.kunda.engine.model.task;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.cache.WTimer;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.core.ServerBootStrap;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.kunda.engine.common.fun.Avatas.ArrayAdd;
import static com.kunda.engine.common.fun.Avatas.String2Array;
import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;
import static com.kunda.engine.handles.socket.RoleSkillRequestHandler.ConfServerPost;
import static com.kunda.engine.utils.ExcelName.en_table;


public class ReloadConfigTask implements TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ReloadConfigTask.class);



    @Override
    public void run(Timeout timeout) throws SocketException, InvalidProtocolBufferException {

        loadSystemSetting();
        loadWorldList();
        cacheConfServer();

        WTimer.instance().add(new ReloadConfigTask(),60, TimeUnit.SECONDS);

    }







    //系统配置文件读取并缓存
    public static void loadSystemSetting() throws SocketException {

        Properties props = ServerBootStrap.getInstance().loadDefaultConfigFile();
        Map<String,String> map = new HashMap<>();
        for(Object obj : props.keySet()){
            String key = (String)obj;
            map.put(key,props.getProperty(key));
            Conf.inner().add( key,props.getProperty( key));
        }
        String RDSKEY_CONF = "00-conf@:" + InnerIpAddress() +":"+props.getProperty("xserver.tcp.port");//系统配置项
        Rdm.instance().hmset(RDSKEY_CONF,map);
    }


    //加载世界分服列表
    private static void loadWorldList() throws InvalidProtocolBufferException {
        //取索引
        ProtobuffFrame.Response wid_index = ConfServerPost(one_record("觅我游戏版配置表","世界分区",0));
        String wid_record =  PbBodyConfService.Record.parseFrom(wid_index.getBody()).getValue();
        Integer[] wids = String2Array(wid_record);//细胞id列表

        for(Integer wid : wids){
            ProtobuffFrame.Response wid_n = ConfServerPost(one_record("觅我游戏版配置表","世界分区",wid));
            String[] wds =  PbBodyConfService.Record.parseFrom(wid_n.getBody()).getValue().split(",");
            WorldMap.inner().add(Integer.parseInt(wds[0]),wds[1],wds[2],wds[3],Integer.parseInt(wds[4]),Integer.parseInt(wds[5]));
        }
    }


    //缓存配置服配置表
    private static void cacheConfServer() throws InvalidProtocolBufferException {
        cacheOneSheet("觅我游戏版配置表","账号等级");
        cacheOneSheet("觅我游戏版配置表","魅力系数");
        cacheOneSheet("觅我游戏版配置表","评委系数");
        cacheOneSheet("觅我游戏版配置表","属性配置表");
        cacheOneSheet("觅我游戏版配置表","技能配置表");
        cacheOneSheet("觅我游戏版配置表","buff配置表");
        cacheOneSheet("觅我游戏版配置表","免疫配置表");
        cacheOneSheet("觅我游戏版配置表","buff状态配置表");

        cacheOneSheet("伙伴数值表","伙伴");
        cacheOneSheet("伙伴数值表","等级表");
        cacheOneSheet("伙伴数值表","升星消耗");
        cacheOneSheet("伙伴数值表","碎片合成");
        cacheOneSheet("伙伴数值表","伙伴骰面初始ID");

        cacheOneSheet("邮件通知表","邮件");

        cacheOneSheet("背包数值表","物品配置表");
        cacheOneSheet("背包数值表","物品类型配置表");

        cacheOneSheet("技能表","技能等级");
        cacheOneSheet("技能表","技能");





    }





    //缓存一个Sheet页配置表
    private static void cacheOneSheet(String table_cn, String sheet_cn) throws InvalidProtocolBufferException {

        ProtobuffFrame.Response first = ConfServerPost(one_record(table_cn,sheet_cn,0));
        String record0 =  PbBodyConfService.Record.parseFrom(first.getBody()).getValue();
        Integer[] ids = String2Array(record0);
        ids = ArrayAdd(ids,0);

        for(Integer id : ids){
            ProtobuffFrame.Response id_n = ConfServerPost(one_record(table_cn,sheet_cn,id));
            PbBodyConfService.Record record =  PbBodyConfService.Record.parseFrom(id_n.getBody());
            ExcelCache.inner().add(record.getKey(),record.getValue());
        }
    }

    /**
     * 向配置服务器请求配置信息请求消息体
     * */
    private static ProtobuffFrame.Request one_record(String table_cn, String sheet_cn, int recordid) {
        PbBodyConfService.ConfReq.Builder body = PbBodyConfService.ConfReq.newBuilder();

        body.setTable(en_table(table_cn,sheet_cn));//表名
        body.setRecid(recordid);

        ProtobuffFrame.Request.Builder msg = ProtobuffFrame.Request.newBuilder();
        msg.setCmd(CMD.h_conf_server.id());
        msg.setSub(0);
        msg.setBody(ByteString.copyFrom(body.build().toByteArray()));
        return msg.build();
    }


}
