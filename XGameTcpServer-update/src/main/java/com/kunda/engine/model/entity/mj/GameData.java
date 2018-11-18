package com.kunda.engine.model.entity.mj;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kunda.engine.manager.redis.Rdm;

import java.util.ArrayList;
import java.util.Map;

import static com.kunda.engine.utils.Const.*;


/**
 * 游戏统计数据
 *
 * */
public class GameData {



    private String date ;//日期 精确到天
    private String value;//数值

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }





    //新增一个用户
    public static void usrIncr(String date) { //YYYY-MM-DD
        String[] ydate = date.split("-");
        String key = RDSKEY_DAYINCR + ydate[0] + "-" + ydate[1];//DayIncr@2017-2
        Rdm.instance().hincrby(key,date,1);

        //用户总注册
        String regKey = RDSKEY_ALLREG + ydate[0] + "-" + ydate[1];
      //  Rdm.instance().hmset(regKey, date, String.valueOf(Rdm.instance().dbsize()));
    }

    //新增一笔充值  date=20180612
    public static void chargeIncr(String yyyymmdd,int number){
        String key = RDSKEY_CHARGE + ":" + yyyymmdd.substring(0,6);//按月
        Rdm.instance().hincrby(key, yyyymmdd, number);//field 日期
    }


    //获取每日新增统计
    public static JSONArray getDayUserIncr(String year, String month){
        String key = RDSKEY_DAYINCR + year+"-" + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }

    //获取每日总注册人数统计
    public static JSONArray getTotalRegister(String year, String month){
        String key = RDSKEY_ALLREG + year+"-" + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }



    //新增一个房卡消费
    public static void fcardIncr(String date,int num) { //YYYY-MM-DD
        String[] ydate = date.split("-");
        String key = RDSKEY_CARDINCR + ydate[0] + "-" + ydate[1];//FcardIncr@2017-2
        String count = Rdm.instance().hGet(key, date);
        if (count == null || count.equals("")) {//当日第一个用户使用房卡
            Rdm.instance().hmset(key, date, String.valueOf(num));
        } else {
            Rdm.instance().hmset(key, date, String.valueOf(Integer.parseInt(count) + num));
        }
    }



    //获取每日房卡消费数量
    public static JSONArray getDayUsedFcards(String year, String month){
        String key = RDSKEY_CARDINCR + year+"-" + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }


    //获取每日抽红包支出统计
    public static JSONArray getDayRedAward(String year, String month){
        String key = RDSKEY_AWARDINCR + year+"-" + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }


    //获取每日玩家充值统计
    public static JSONArray getDayUsrCharge(String year, String month){
        String key = RDSKEY_CHARGE + ":" + year + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }



    //获取每日活跃玩家统计（每日打完一局就算活跃用户）
    public static JSONArray getDAU(String year, String month){
        String key = RDSKEY_DAUINCR + year+"-" + month;
        Map<String,String> mp  =  Rdm.instance().hgetAll(key);
        java.util.List<String> mapKeyList = new ArrayList<String>(mp.keySet());
        JSONArray array = new JSONArray();
        if( mp.size()>0 ){
            for(String dateKey:mapKeyList ){
                JSONObject ob = new JSONObject();
                ob.put("date",dateKey);
                ob.put("value",mp.get(dateKey));
                array.add( ob );
            }
        }

        return array;
    }















}
