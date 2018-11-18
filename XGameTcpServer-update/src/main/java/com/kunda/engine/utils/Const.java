package com.kunda.engine.utils;


import java.text.SimpleDateFormat;


public class Const {


    public static final long Uidbase = 100000;//UID开始数值 十万
    public static final String BLANK = "";//空字符串，用于设定初始值
    public static final int ZERO = 0;//数字0，用于设定初始值
    public static final int MAIL_MAX_NUM = 5;

    public static final int QMargin = 12;//二维码生成白边宽度

    public static final int TOKEN_DELAY = 3600;//token有效时间1小时
    public static final int MAIL_DELAY = 3600*24*7;//邮件有效时间7天


    public static final String CH_KEY_SERVER = "CH_KEY_SERVER";//全服频道
    public static final String CH_KEY_GROUP = "CH_KEY_GROUP";//单个群频道

    public static final int CREATE_ROLE_LIMIT = 3;//角色创建个数限制

    //时间格式
    public static final SimpleDateFormat DateFormatSSS=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final SimpleDateFormat DateFormatSS=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DateFormatmm=new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat DateFormatDD=new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DateFormatMM=new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat DateFormatms=new SimpleDateFormat("MMddHHmmss");


    //UKEY、RKEY、SKEY、GKEY 分别对应RDS分区的用户区 房间区 会话区 游戏区
    public static final  String RDSKEY_GOOD    = "00-good@:";//商品
    public static final  String RDSKEY_SHOP    = "00-shop@:";//商城
    public static final  String RDSKEY_TABL    = "05-tab@:";//配置表

    public static String KEY_TABLE(String name,String record ){
        return RDSKEY_TABL + name +":" + record ;
    }

    public static final  String RDSKEY_ROLE    = "01-role@";//角色
    public static final  String RDSKEY_SIGN    = "01-role@:sign";//角色游戏标识
    public static final  String RDSKEY_ROLE_NICK = "01-role@:nick";//角色
    public static final  String RDSKEY_MY_BUDDY = "01-role@:buddy";//我的伙伴仓库
    public static final  String RDSKEY_LAST_ROLE = "01-last-role@";//上次角色
    public static final  String RDSKEY_BUDDY    = "01-buddy@";//伙伴
    public static final  String RDSKEY_BUDDY_PIECE    = "01-buddy@:piece";//伙伴碎片
    public static final  String RDSKEY_STOCK_PVE = "01-stock@:pve";//我的PVE阵容
    public static final  String RDSKEY_SYS_MAIL = "01-mail@:sys";//系统邮件
    public static final  String RDSKEY_MY_MAIL = "01-mail@:role";//玩家自己的邮件(系统)
    public static final  String RDSKEY_PATCH_MAIL = "01-mail@:patch";//玩家自己的邮件(定向)
    public static final  String RDSKEY_ITEM    = "01-item@";//物品
    public static final  String RDSKEY_PACKET    = "02-packet@";//背包


    public static final  String RDSKEY_DAYINCR   = "09-gmdata@:dayincre"; //日新增注册统计表
    public static final  String RDSKEY_ALLREG    = "09-gmdata@:allregist"; //用户总注册人数统计表
    public static final  String RDSKEY_CARDINCR  = "09-gmdata@:cardincre"; //每日房卡消费统计表
    public static final  String RDSKEY_AWARDINCR = "09-gmdata@:awardincre"; //每日抽红包支出统计表
    public static final  String RDSKEY_CHARGE    = "09-gmdata@:chargeincre"; //每日用户充值累积统计表
    public static final  String RDSKEY_DAUINCR   = "09-gmdata@:dauincre"; //每日活跃用户统计表







}
