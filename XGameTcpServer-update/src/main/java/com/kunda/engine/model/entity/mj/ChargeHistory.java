package com.kunda.engine.model.entity.mj;


import com.kunda.engine.manager.redis.Rdm;

import java.util.Random;

import static com.kunda.engine.utils.Const.DateFormatSSS;


/**
 * 充值历史表
 * */
public class ChargeHistory {

    public static final  String BKEY_CHARGE = "ChaHis@";//玩家自己的充值记录表
    public static final  String BKEY_DEPOST = "Depost@";//玩家自己的提现记录表
    public static final  String BKEY_INCEXP = "IncExp@";//玩家自己的收益记录表
    public static final  String BKEY_INCDEP = "IncDep@";//收益记录被提现后移到此表
    public static final  String BKEY_APLDEP = "AplDep@";//申请提现记录表
    public static final  String BKEY_CASHPL = "CashPl@";//红包现金累积彩池


    private String orderNo; //订单号
    private String uid;//订单关联UID
    private String value;//金额
    private String date;//日期
    private String dealed;//订单处理标记
    private String sum;//余额
    private int type; //收入类型 1、下级充值分成 2、抽取红包奖励

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDealed() {
        return dealed;
    }

    public void setDealed(String dealed) {
        this.dealed = dealed;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    /**
     * 充值记录
     *
     * @return  订单号 [ CZ9038293   10.0_2017-9-14 14:16:38 ]
     * */
    public static String charge(String uid,String orderNo ,String value){
        String chkey = BKEY_CHARGE + uid;

        //充值金额+充值时间
        String creatTime = DateFormatSSS.format(System.currentTimeMillis());
        value += "_" + creatTime; //[value_date]

        Rdm.instance().hmset(chkey,orderNo,value);

        return  orderNo;
    }



    /**
     * 收益 记录
     * 参数  value 收益额 fromOrder 收益来源订单号 fromUid 来源UID sum 账户余额
     *
     * @return  订单号 [ IN2028493   110.0_CZ9038293 ]
     * */
    public static String inCome(String uid,String value,String fromOrder,String fromUid,String sum){

        String inkey = BKEY_INCEXP + uid;

        //生成订单号
        int no =  makeOrderNo();
        String orderNo = "IN" + no;
        while ( Rdm.instance().hGet(inkey,orderNo) != null ){
            no =  makeOrderNo();
        }

        orderNo = "IN" + no;;

        //收支金额+收支来源关联订单号
        value += "_" + fromOrder  + "_" + sum; //[value_date]

        Rdm.instance().hmset(inkey,orderNo,value);

        return  orderNo;
    }



    /**
     *
     * 红包现金彩池累积
     * */
    public static float incrPool(float inc){
        if( !Rdm.instance().exists(BKEY_CASHPL)){
            Rdm.instance().hmset(BKEY_CASHPL,"pool",String.valueOf(inc));
        }else {
            float rec = Float.parseFloat(Rdm.instance().hGet(BKEY_CASHPL,"pool"));
            inc  += rec;
            Rdm.instance().hmset(BKEY_CASHPL,"pool",String.valueOf(inc));
        }
        return inc;
    }

    /**
     *
     * 红包现金累积支出
     * */
    public static float incrCashOut(float inc){
        if( Rdm.instance().hGet(BKEY_CASHPL,"total") == null){
            Rdm.instance().hmset(BKEY_CASHPL,"total",String.valueOf(inc));
        }else {
            float rec = Float.parseFloat(Rdm.instance().hGet(BKEY_CASHPL,"total"));
            inc  += rec;
            Rdm.instance().hmset(BKEY_CASHPL,"total",String.valueOf(inc));
        }
        return inc;
    }




    /**
     *
     * 红包现金彩池清零
     * */
    public static void clsPool(){
        Rdm.instance().hmset(BKEY_CASHPL,"pool",String.valueOf(0));
    }

    /**
     *
     * 查询红包现金彩池
     * */
    public static int queryPool(){
        if(Rdm.instance().exists(BKEY_CASHPL)){
            return Math.round( Float.parseFloat( Rdm.instance().hGet(BKEY_CASHPL,"pool")));
        }
       return 0;
    }

    /**
     *
     * 查询红包现金总支出
     * */
    public static int queryTotalOut(){
        if(Rdm.instance().hGet(BKEY_CASHPL,"total") != null){
            return Math.round( Float.parseFloat( Rdm.instance().hGet(BKEY_CASHPL,"total")));
        }
        return 0;
    }





















    /**
     *生成订单号
     * */
    static int makeOrderNo(){
        Random rand = new Random();
        int rad1 = rand.nextInt(9999);
        int rad2 = rand.nextInt(999);
        int rad3 = rand.nextInt(8)+1;
        int order = rad3 * 10000000 + rad2* 10000 +rad1;
        return order;
    }


}
