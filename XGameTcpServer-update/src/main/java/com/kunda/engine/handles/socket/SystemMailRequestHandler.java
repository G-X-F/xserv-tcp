package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.cache.WorldMap;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.core.Groups;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.entity.mj.SysMail;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.SocketException;
import java.util.*;

import static com.kunda.engine.common.fun.Avatas.ArrayAdd;
import static com.kunda.engine.common.fun.Avatas.List2Array;
import static com.kunda.engine.common.fun.Messages.ErrMsg;
import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.common.fun.OsMacIP.InnerIpAddress;
import static com.kunda.engine.model.entity.mj.Packet.addItem2Packet;
import static com.kunda.engine.model.entity.mj.SysMail.*;
import static com.kunda.engine.utils.Const.CH_KEY_SERVER;
import static com.kunda.engine.utils.Const.MAIL_MAX_NUM;
import static com.kunda.engine.utils.Const.ZERO;
import static com.kunda.engine.utils.ECode.E10012;
import static com.kunda.engine.utils.ECode.E10052;


public class SystemMailRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(SystemMailRequestHandler.class);

        public static int requestId = CMD.t_system_mail.id();



        /**
         * 系统邮件/全服邮件/通告
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {

            switch (request.getSub()) {
                case 0://创建全服邮件
                    creatmail(ctx);
                    break;
                case 1://系统通告
                    bulletin();
                    break;
                case 2://打开我的邮箱
                    openmails(ctx);
                    break;
                case 3://阅读邮件
                    readmymails(ctx);
                    break;
                case 4://领取邮件奖励
                    getItems(ctx);
                    break;
                case 5://一键领取邮件奖励
                    getAllItems(ctx);
                    break;
                case 6://一键已读
                    readAllMails(ctx);
                    break;
                case 7://定向邮件
                    patchMails(ctx);
                    break;
                default:
                    creatmail(ctx);

            }


        }




        //系统通告
        private void bulletin(){

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(request.getBody());

            Groups.inst().getGroup(CH_KEY_SERVER).broadcast(response.build());
        }



        //创建全服邮件
        public void creatmail(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {
            PbBodyMail.CreatMail sysmail = PbBodyMail.CreatMail.parseFrom(request.getBody());
            //获取邮件模板id
            int tempid = sysmail.getTmpid();
            //取出邮件数据
            String result = ExcelCache.inner().get("邮件通知表","邮件",tempid);
            if(StringUtils.isEmpty(result)){
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10052,"mail tempid not exist!"));
                return;
            }
            List<Integer> itemsList = new ArrayList<>();
            List<Integer> numsList = new ArrayList<>();
            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));

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
            long mid = createSysMail( wid,String.valueOf(tempid),List2Array(itemsList),List2Array(numsList));
            //构造系统邮件
            SysMail mls = getSysMailById(wid,mid);

            //向全服在线玩家广播系统邮件
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(mls.toPbBody().toByteString());
            Groups.inst().getGroup(CH_KEY_SERVER).broadcast(response.build());

        }




        //打开我的邮箱
        private void openmails(ChannelHandlerContext ctx) throws SocketException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));
            Set<String> mail_keys =  getKeysMail( wid);//获取本服所有系统邮件id的KEY


            Set<String> my =  myMails(wid,Long.parseLong(rid));//玩家已经读取过的邮件id
            Set<String> patch =  myPatchMails(wid,Long.parseLong(rid));//玩家的定向邮件

            List<Long> sysMailIds = new ArrayList<>();//存放系统邮件ID

            //删除角色邮箱中看过的并过期邮件
            for(String mid:my){
                if(!mail_keys.contains(KEY_SYS_MAIL(wid,Long.parseLong(mid)))){
                    rmvMyMails( wid , Long.parseLong(rid),Long.parseLong(mid));
                }
            }

            //整理玩家未读过的邮件
            PbBodyMail.MailList.Builder mlist = PbBodyMail.MailList.newBuilder();
            for(String mail : mail_keys){
                if(!my.contains(mail.split(":")[3])){ //未读过的邮件
                    sysMailIds.add(Long.parseLong(mail.split(":")[3]));
                }
            }

            //玩家的定向邮件
            for(String mail : patch){
                if(!my.contains(mail)){ //未读过的邮件
                    sysMailIds.add(Long.parseLong(mail));
                }
            }

            //整理邮件，限制上限,上限50
            if(sysMailIds.size()>MAIL_MAX_NUM){
                //按照邮件ID从小到大排列，及按时间先后顺序排列
                Collections.sort(sysMailIds, new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        return (o1 > o2)?1:-1;
                    }
                });

                int more = sysMailIds.size() - MAIL_MAX_NUM;
                Iterator<Long>  its =   sysMailIds.iterator();

                for(int i =0 ; i < more ; i++){
                    if(its.hasNext()){
                        long id = its.next();
                        if( patch.contains(String.valueOf(id))){
                            Rdm.instance().srem(PATCH_MAIL_KEY( wid, Long.parseLong(rid)),String.valueOf(id));
                        }
                        its.remove();
                    }
                }
            }

            for(Long mailId : sysMailIds){
                SysMail ml= getSysMailById( wid, mailId);
                mlist.addMlist(ml.toPbBody());
            }


            //返回系统邮件列表
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(mlist.build().toByteString());
            SendTo(ctx,response.build());
        }


        //阅读邮件
        private void readmymails(ChannelHandlerContext ctx) throws SocketException, InvalidProtocolBufferException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));

            PbBodyMail.ReadMail body = PbBodyMail.ReadMail.parseFrom(request.getBody());
            long mail = body.getMailid();
            SysMail ml= getSysMailById( wid, mail);
            if(ml == null){ //邮件已过期
                rmvMyMails( wid , Long.parseLong(rid),mail);
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"mail has expired!"));
                return;
            }

            //没有礼品的邮件，邮件id记入我的邮箱
            if(ml.getItems().length == 0){
                addMyMails( wid ,Long.parseLong(rid),mail);
                rmvPatchMails(wid ,Long.parseLong(rid),mail);//如果是定向邮件，从我的定向邮件删除该id
            }




            //返回系统邮件列表
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            SendTo(ctx,response.build());
        }


        //领取邮件礼品
        private void getItems(ChannelHandlerContext ctx) throws SocketException, InvalidProtocolBufferException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));

            PbBodyMail.ReadMail body = PbBodyMail.ReadMail.parseFrom(request.getBody());
            long mail = body.getMailid();
            SysMail ml= getSysMailById( wid, mail);
            if(ml == null){ //邮件已过期
                rmvMyMails( wid , Long.parseLong(rid),mail);
                SendTo(ctx,ErrMsg(requestId,request.getSub(),E10012,"mail has expired!"));
                return;
            }

            //向背包插入物品
            for(int i = 0; i< ml.getItems().length; i++ ){
                addItem2Packet( Long.parseLong(rid),ml.getItems()[i] ,ml.getNums()[i] );
            }

            if(ml.getItems().length != 0){
                addMyMails( wid ,Long.parseLong(rid),mail);
                rmvPatchMails(wid ,Long.parseLong(rid),mail);//如果是定向邮件，从我的定向邮件删除该id
            }


            //返回系统邮件列表
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            SendTo(ctx,response.build());
        }

        //一键领取邮件礼品
        private void getAllItems(ChannelHandlerContext ctx) throws SocketException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));
            Set<String> mail_keys =  getKeysMail( wid);//获取本服所有系统邮件id

            Set<String> my =  myMails(wid,Long.parseLong(rid));//玩家已经读取过的邮件id
            Set<String> patch =  myPatchMails(wid,Long.parseLong(rid));//玩家的定向邮件

            //删除角色邮箱中看过的并过期邮件
            for(String mid:my){
                if(!mail_keys.contains(KEY_SYS_MAIL(wid,Long.parseLong(mid)))){
                    rmvMyMails( wid , Long.parseLong(rid),Long.parseLong(mid));
                }
            }

            Integer[] items = new Integer[0];
            Integer[] nums = new Integer[0];
            //未领取过的系统邮件
            for(String mail : mail_keys){
                if(!my.contains(mail.split(":")[3])){ //未读过的邮件
                    SysMail ml= getSysMailById( wid, Long.parseLong(mail.split(":")[3]));
                    if(ml.getItems().length > 0){
                        items = ArrayAdd(items,ml.getItems());
                        nums = ArrayAdd(nums,ml.getNums());
                        addMyMails( wid ,Long.parseLong(rid),ml.getId());
                    }
                }
            }

            //未领取过的定向邮件
            for(String mail : patch){
                if(!my.contains(mail)){ //未读过的邮件
                    SysMail ml= getSysMailById( wid, Long.parseLong(mail));
                    if(ml.getItems().length > 0){
                        items = ArrayAdd(items,ml.getItems());
                        nums = ArrayAdd(nums,ml.getNums());
                        addMyMails( wid ,Long.parseLong(rid),ml.getId());
                        rmvPatchMails(wid ,Long.parseLong(rid),ml.getId());
                    }
                }
            }

            //合并同类物品
            ArrayList<Integer> items_new = new ArrayList<> ();
            ArrayList<Integer> nums_new = new ArrayList<> ();
            for(int i = 0; i< items.length; i++){
                if(items_new.contains(items[i])){
                    int index = items_new.indexOf(items[i]);//找到索引
                    int newnum = nums_new.get(index) + nums[i];//数量叠加
                    nums_new.set(index,newnum);
                }else {
                    items_new.add(items[i]);
                    nums_new.add(nums[i]);
                }
            }

            //向背包插入物品
            for(int i = 0; i< items_new.size(); i++ ){
                addItem2Packet( Long.parseLong(rid),items_new.get(i) ,nums_new.get(i) );
            }

            PbBodyMail.ItemList.Builder itemlist = PbBodyMail.ItemList.newBuilder();
            itemlist.addAllItems(items_new);
            itemlist.addAllNums(nums_new);


            //返回系统邮件列表
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(itemlist.build().toByteString());
            SendTo(ctx,response.build());
        }


        //一键已读
        private void readAllMails(ChannelHandlerContext ctx) throws SocketException {

            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID
            //世界编号
            int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));
            Set<String> mail_keys =  getKeysMail( wid);//获取本服所有系统邮件id

            Set<String> my =  myMails(wid,Long.parseLong(rid));//玩家已经读取过的邮件id

            //删除角色邮箱中看过的并过期邮件
            for(String mid:my){
                if(!mail_keys.contains(KEY_SYS_MAIL(wid,Long.parseLong(mid)))){
                    rmvMyMails( wid , Long.parseLong(rid),Long.parseLong(mid));
                }
            }

            //读取所有文本邮件
            for(String mail : mail_keys){
                if(!my.contains(mail.split(":")[3])){ //未读过的邮件
                    SysMail ml= getSysMailById( wid, Long.parseLong(mail.split(":")[3]));
                    if(ml.getItems().length == 0){//文本类邮件
                        addMyMails( wid ,Long.parseLong(rid),ml.getId());
                    }
                }
            }

            //返回系统邮件列表
            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            SendTo(ctx,response.build());
        }




    //定向批量邮件
    private void patchMails(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {

        //世界编号
        int wid =   WorldMap.inner().wid(InnerIpAddress()+":" + Conf.inner().get("xserver.tcp.port"));
        PbBodyMail.PatchMail body = PbBodyMail.PatchMail.parseFrom(request.getBody());

        for(long role : body.getRoleList() ){
            addPatchMails( wid , role, body.getMailid());
        }


        //返回系统邮件列表
        response.setCmd(requestId);
        response.setSub(request.getSub());
        response.setCode(ZERO);
        SendTo(ctx,response.build());
    }







}
