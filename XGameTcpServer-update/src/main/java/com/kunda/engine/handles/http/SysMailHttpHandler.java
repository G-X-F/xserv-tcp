package com.kunda.engine.handles.http;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.core.Groups;
import com.kunda.engine.handles.socket.SystemMailRequestHandler;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.entity.mj.SysMail;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static com.kunda.engine.common.fun.Avatas.List2Array;
import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;
import static com.kunda.engine.model.entity.mj.SysMail.createSysMail;
import static com.kunda.engine.model.entity.mj.SysMail.getSysMailById;
import static com.kunda.engine.utils.Const.CH_KEY_SERVER;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10052;

public class SysMailHttpHandler extends BaseServerHandler {
    private static final Logger logger = LoggerFactory.getLogger(SysMailHttpHandler.class);

    public static int requestId = CMD.h_system_mail.id();

    private SystemMailRequestHandler protoHandler = new SystemMailRequestHandler();

    @Override
    public ProtobuffFrame.Response handleClientHttpRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {
        switch (request.getSub()){
            case 0:
                return broadcastSysMail();//GM全服邮件
            case 1:
                return createPathMail();//GM定向邮件
            default:
                return broadcastSysMail();
        }
    }


    /**
     * 全服广播邮件
     * @return
     * @throws InvalidProtocolBufferException
     * @throws SocketException
     */
    private ProtobuffFrame.Response broadcastSysMail() throws InvalidProtocolBufferException, SocketException {
            PbBodyMail.GmSysMailParams params = PbBodyMail.GmSysMailParams.parseFrom(request.getBody());
            int tempid = params.getTmpId(); //获取邮件模板ID
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));//世界编号

            PbBodyMail.GmSysMailResult.Builder res = PbBodyMail.GmSysMailResult.newBuilder();
            res.setWid(wid);

            response.setCmd(requestId);
            response.setSub(request.getSub());

            SysMail mls =createMail(tempid,wid);
            if(null==mls) {
                logger.info(" mail tempId not exist");
                res.setTmpId(tempid);
                response.setCode(E10052);
                response.setBody(res.build().toByteString());
                return response.build();
            }
            //向全服在线玩家广播系统邮件
            res.setTmpId(mls.getTempId());
            response.setCode(ZERO);
            response.setBody(mls.toPbBody().toByteString());
            Groups.inst().newGroup(CH_KEY_SERVER).broadcast(response.build());;//创建一个群组广播

            response.setBody(res.build().toByteString());
            return response.build();
    }


    //GM定向邮件
    private ProtobuffFrame.Response createPathMail() throws InvalidProtocolBufferException, SocketException {

            PbBodyMail.GmPatchMailParams params = PbBodyMail.GmPatchMailParams.parseFrom(request.getBody());
            int tempid = params.getTmpId(); //获取邮件模板ID
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));//世界编号

            //构造返回消息，返回世界服wid,邮件模板tempids 集合，角色rids集合
            PbBodyMail.GmPatchMailResult.Builder res = PbBodyMail.GmPatchMailResult.newBuilder();
            res.setWid(wid);
            response.setCmd(requestId);
            response.setSub(request.getSub());

            SysMail mls =createMail(tempid,wid);
            if(null==mls){
                logger.info(" mail tempId not exist");
                res.setTmpId(params.getTmpId());
                res.setRid(params.getRid());
                response.setCode(E10052);
                response.setBody(res.build().toByteString());
                return response.build();
            }
            //发送定向邮件
            SysMail.addPatchMails(wid,params.getRid(),mls.getId());

            res.setTmpId(mls.getTempId());
            res.setRid(params.getRid());
            response.setCode(ZERO);
            response.setBody(res.build().toByteString());

            return response.build();

    }



    /**
     * 创建系统邮件
     * @return
     */
    private SysMail createMail(int tempid,int wid)throws InvalidProtocolBufferException, SocketException{
        //取出邮件数据
        String result = ExcelCache.inner().get("邮件通知表","邮件",tempid);
        if(StringUtils.isEmpty(result)){
            return null;
        }
        List<Integer> itemsList = new ArrayList<>();
        List<Integer> numsList = new ArrayList<>();
        //对redis得到的结果进行分割
        String[] arr = result.split(",");
        //从第二个数据开始遍历redis结果，剔除物品ID为0的物品
        int a1=0;
        int a2=0;
        for(int i=1;i<arr.length;i++){
            a1 = Integer.parseInt(arr[i]);
            a2 = Integer.parseInt(arr[i+1]);
            if((a1+a2)==0){
                i++;
                continue;
            }
            itemsList.add(a1);
            numsList.add(a2);
            i++;
        }
        //创建一个系统邮件
        long mid = SysMail.createSysMail( wid,String.valueOf(tempid),List2Array(itemsList),List2Array(numsList));
        //构造系统邮件
        return getSysMailById(wid,mid);
    }
}
