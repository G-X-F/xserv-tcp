package com.kunda.engine.model.entity.mj;


import com.kunda.engine.model.proto.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 比美状态
 * */
public class BeautyStat {

    //免疫类状态 <状态id>
    private  HashSet<Integer> immu_stat= new HashSet<>();
    boolean sleep = false;//是否催眠
    private  ArrayList<PbBodyBuff.Buff> buff_stat = new ArrayList<>();//buff


    public HashSet<Integer> getImmu_stat() {
        return immu_stat;
    }


    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public ArrayList<PbBodyBuff.Buff> getBuff_stat() {
        return buff_stat;
    }




    public void addImmuStat( Integer immu){
        immu_stat.add(immu);
    }

    public void rmvImmuStat(Integer immu){
        immu_stat.remove(immu);
    }

    public void addBuff(PbBodyBuff.Buff buff){
        buff_stat.add(buff);
    }

    public void rmvBuff(Integer buffid){
        buff_stat.remove(buffid);
    }


    //计算buff增益/减益数值
    public double values(){
        int value = 0;
        for (PbBodyBuff.Buff buff : buff_stat){
            if(buff.getTarget() == 1){ //释放目标为自身，为增益效果
                value += buff.getValue();
            }
            if(buff.getTarget() == 2){ //释放目标为敌方，为减益效果
                value -= buff.getValue();
            }
        }

        buff_stat.clear();//清空buff

        return  value * 0.0001;
    }
}
