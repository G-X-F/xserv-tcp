package com.kunda.engine.model.entity.mj;

import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.kunda.engine.common.fun.Avatas.Array2String;
import static com.kunda.engine.common.fun.Avatas.String2Array;
import static com.kunda.engine.utils.Const.*;

public class SysMail {
    private  long id;//邮件id
    private int tempId;//邮件模版ID
    private Integer[] items;//礼品id
    private Integer[] nums;//礼品数量




    //构造函数
    public SysMail(Map<String,String> mp){
        this.id = Long.parseLong(mp.get("id"));
        this.tempId=Integer.parseInt(mp.get("tempid"));
        this.items=String2Array(mp.get("items"));
        this.nums=String2Array(mp.get("nums"));
    }


    //根据id获得邮件信息
    public static SysMail getSysMailById(int wid, long mid){
        if(Rdm.instance().exists(KEY_SYS_MAIL(wid,mid))){
            Map<String,String> mp =  Rdm.instance().hgetAll(KEY_SYS_MAIL(wid,mid));
            return new SysMail(mp);
        }
        return null;
    }



    //创建本服系统邮件
    public static long createSysMail(int wid,String tempid,Integer[] items,Integer[] nums){
        long id = System.currentTimeMillis()/1000;

        if(!Rdm.instance().exists( KEY_SYS_MAIL(wid,id) )){//不存在该伙伴ID
            //创建Buddy表
            Map<String,String> mailmp = new HashMap<>();
            mailmp.put("id",String.valueOf(id));//邮件id
            mailmp.put("tempid",tempid);//模版id
            mailmp.put("items",Array2String(items));//礼品id数组
            mailmp.put("nums",Array2String(nums));//礼品数量数组

            Rdm.instance().hmset(KEY_SYS_MAIL(wid,id),mailmp);
            Rdm.instance().setExpireTime(KEY_SYS_MAIL(wid,id),MAIL_DELAY);//7天后消失
        }

        return id;
    }


    //转成pb数据
    public PbBodyMail.SysMail toPbBody(){
        PbBodyMail.SysMail.Builder mail = PbBodyMail.SysMail.newBuilder();
        mail.setId(this.id); //id
        mail.setTmpid(this.tempId);//邮件模版id
        //礼品ID
        for(Integer item :this.items){
            mail.addItems(item);
        }
        //礼品数量
        for(Integer num :this.nums){
            mail.addNums(num);
        }
        return mail.build();
    }

    //本服系统邮件
    public static Set<String> getKeysMail(int wid){
       return Rdm.instance().keys(KEYS_MAIL(wid));
    }


    //获取角色已看过的所有邮件
    public static Set<String> myMails(int wid ,long role){
      return   Rdm.instance().smembers(MY_MAIL_KEY( wid, role));
    }

    //删除角色已看过的并过期的邮件
    public static void rmvMyMails(int wid ,long role,long mailid){
        Rdm.instance().srem(MY_MAIL_KEY( wid, role),String.valueOf(mailid));
    }

    //增加已看过的邮件
    public static void addMyMails(int wid ,long role,long mailid){
       Rdm.instance().sadd(MY_MAIL_KEY( wid, role),String.valueOf(mailid));
    }

    //获取角色所有定向邮件
    public static Set<String> myPatchMails(int wid ,long role){
        return   Rdm.instance().smembers(PATCH_MAIL_KEY( wid, role));
    }

    //删除角色已看过的定向的邮件
    public static void rmvPatchMails(int wid ,long role,long mailid){
        Rdm.instance().srem(PATCH_MAIL_KEY( wid, role),String.valueOf(mailid));
    }

    //定向邮件
    public static void addPatchMails(int wid ,long role,long mailid){
        Rdm.instance().sadd(PATCH_MAIL_KEY( wid, role),String.valueOf(mailid));
    }




    public long getId() {
        return id;
    }

    public int getTempId() {
        return tempId;
    }

    public Integer[] getItems() {
        return items;
    }

    public Integer[] getNums() {
        return nums;
    }

    //生成SysMailKey
    public static String KEY_SYS_MAIL(int wid,long mid ){
        return RDSKEY_SYS_MAIL + ":" + wid +":" + mid ;
    }

    //生成MailKeys
    public static String KEYS_MAIL(int wid){
        return RDSKEY_SYS_MAIL + ":" + wid + ":*" ;
    }

    //生成MYMailKey
    public static String MY_MAIL_KEY(int wid,long role){
        return RDSKEY_MY_MAIL + ":" + wid + ":" + role ;
    }

    //生成PatchMailKey
    public static String PATCH_MAIL_KEY(int wid,long role){
        return RDSKEY_PATCH_MAIL + ":" + wid + ":" + role ;
    }


}
