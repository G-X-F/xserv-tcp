package com.kunda.engine.model.entity.mj;

import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.kunda.engine.utils.Const.*;


/**
 *
 * 伙伴/细胞
 * */
public class Buddy {

    private static final Logger logger = LoggerFactory.getLogger(Buddy.class);


    private int id;////随机码 => 607823
    private int syn_code;//合成码(背包数值/物品配置表ID）
    private int cell_id;//细胞id
    private int role;//所属角色id
    private int level;//伙伴等级
    private long exp;//经验
    private String name;//名称
    private int element;//元素属性 金木水火土(1~5)
    private int pveskill;//pve技能
    private int pvpskill;//pvp技能
    private int star;//星级
    private int atk;//攻击
    private int def;//防御
    private int hp;//生命
    private int crit;//暴击
    private int crit_def;//抗暴
    private int crit_atk;//暴击伤害/倍率
    private int hit_rat;//命中
    private int dodge;//闪避
    private int dice;//骰子面(攻击、星星、闪电、盾牌)
    private int lock;//锁  1:锁住 0:未锁
    private int speed;//速度



    //构造函数
    public Buddy(Map<String,String> mp){
        this.id = Integer.parseInt(mp.get("id"));
        this.syn_code=Integer.parseInt(mp.get("syn_code"));
        this.cell_id=Integer.parseInt(mp.get("cell_id"));
        this.role=Integer.parseInt(mp.get("role"));
        this.level=Integer.parseInt(mp.get("level"));
        this.exp=Integer.parseInt(mp.get("exp"));
        this.name = mp.get("name");
        this.element = Integer.parseInt(mp.get("element"));
        this.pveskill =Integer.parseInt(mp.get("pveskill")) ;
        this.pvpskill = Integer.parseInt(mp.get("pvpskill"));
        this.star=Integer.parseInt(mp.get("star"));
        this.atk=Integer.parseInt(mp.get("atk"));
        this.def=Integer.parseInt(mp.get("def"));
        this.hp=Integer.parseInt(mp.get("hp"));
        this.crit=Integer.parseInt(mp.get("crit"));
        this.crit_def=Integer.parseInt(mp.get("crit_def"));
        this.crit_atk =Integer.parseInt(mp.get("crit_atk"));
        this.hit_rat =Integer.parseInt(mp.get("hit_rat"));
        this.dodge =Integer.parseInt(mp.get("dodge"));
        this.dice =Integer.parseInt(mp.get("dice"));
        this.lock =Integer.parseInt(mp.get("lock"));
        this.speed =Integer.parseInt(mp.get("speed"));
    }


    //根据id获得伙伴信息
    public static Buddy getBuddyById(long role,int bid){
        if(Rdm.instance().exists(KEY_BUDDY(role,bid))){
            Map<String,String> mp =  Rdm.instance().hgetAll(KEY_BUDDY(role,bid));
            return new Buddy(mp);
        }
        return null;
    }

    /**
     * 创建一个伙伴 返回 伙伴ID
     * 参数：
     *    role 角色id
     *    cells 细胞id
     *    name 细胞名称
     *    element 元素属性（金木水火土)
     *    pveskill pve技能
     *    pvpskill pvp技能
     *    dice     骰子的6个面（攻击、盾牌、星星、闪电)
     * */
    public static int createBuddyById(long role,String[] item,String[] cell,String[] atk,int dice){
        int bid =  makeBid();//607823
        while (Rdm.instance().exists( KEY_BUDDY(role,bid) )){
            bid =  makeBid();
        }

        if(!Rdm.instance().exists( KEY_BUDDY(role,bid) )){//不存在该伙伴ID
            //创建Buddy表
            Map<String,String> buddymp = new HashMap<>();
            buddymp.put("id",String.valueOf(bid));
            buddymp.put("syn_code",item[0]);//伙伴合成码
            buddymp.put("cell_id",cell[0]);//细胞id
            buddymp.put("role",String.valueOf(role));//角色
            buddymp.put("level",atk[3]);
            buddymp.put("exp",String.valueOf(0));
            buddymp.put("name",cell[1]);
            buddymp.put("element",cell[2]);
            buddymp.put("pveskill",cell[3]);
            buddymp.put("pvpskill",cell[3]);
            buddymp.put("star",atk[2]);
            buddymp.put("atk",atk[6]);
            buddymp.put("def",atk[7]);
            buddymp.put("hp",atk[8]);//生命
            buddymp.put("crit","0");//暴击
            buddymp.put("crit_def","0");//抗暴
            buddymp.put("crit_atk","0");//暴击伤害
            buddymp.put("hit_rat","0");//命中
            buddymp.put("dodge","0");//闪避
            buddymp.put("dice",String.valueOf(dice));//骰子面
            buddymp.put("lock",String.valueOf(0));//锁
            buddymp.put("speed",String.valueOf(0));//速度

            Rdm.instance().hmset(KEY_BUDDY(role,bid),buddymp);
        }

        return bid;
    }




    //转成pb数据
    public PbBodyBuddy.Buddy toPbBody(){
        PbBodyBuddy.Buddy.Builder buddy = PbBodyBuddy.Buddy.newBuilder();
        buddy.setId(this.id);
        buddy.setLevel(this.level);
        buddy.setExp(this.exp);
        buddy.setElement(this.element);
        buddy.setPveskill(this.pveskill);
        buddy.setPvpskill(this.pvpskill);
        buddy.setStar(this.star);
        buddy.setAtk(this.atk);
        buddy.setDef(this.def);
        buddy.setHp(this.hp);
        buddy.setCrit(this.crit);
        buddy.setCritDef(this.crit_def);
        buddy.setCritAtk(this.crit_atk);
        buddy.setHitRat(this.hit_rat);
        buddy.setDodge(this.dodge);
        buddy.setDice((this.dice));
        buddy.setCellId((this.cell_id));
        buddy.setSpeed(this.speed);
        buddy.setLock(this.lock);
        buddy.setAtkPlus(0);
        buddy.setDefPlus(0);
        buddy.setHpPlus(0);
        //战力=（生命/10+攻击/2+防御）*(1+命中率)*（1+闪避率）*（1+暴击率*(1+爆伤加成))*（1+抗暴率）

        int strength = (hp/10 + atk/2 + def) * (1+hit_rat) * (1+dodge) * (1 + crit*(1 + crit_atk)) * (1+ crit_def);

        buddy.setStrength( strength );
        //下一星级战力
        String star_record =  ExcelCache.inner().get("伙伴数值表","升星消耗",0);
        if(this.star == star_record.split(",").length + 1){ //到达最高星级
            buddy.setStrengthNext(strength);
        }else {
            //伙伴配置表
            int buddy_level_id = element*10000 + (star+1) *1000 + 1;
            String atk_record =  ExcelCache.inner().get("伙伴数值表","等级表",buddy_level_id);
            String[] atk = atk_record.split(","); //攻击防御生命
            strength = (Integer.parseInt(atk[8])/10 + Integer.parseInt(atk[6])/2 + Integer.parseInt(atk[7])) * (1+hit_rat) * (1+dodge) * (1 + crit*(1 + crit_atk)) * (1+ crit_def);
            buddy.setStrengthNext(strength);
        }

        return buddy.build();
    }


    public void setLevel(int level) {
        this.level = level;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"level",String.valueOf(level));
    }

    public void setStar(int star) {
        this.star = star;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"star",String.valueOf(star));
    }

    public void setExp(long exp) {
        this.exp = exp;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"exp",String.valueOf(exp));
    }

    public void setAtk(int atk) {
        this.atk = atk;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"atk",String.valueOf(atk));
    }

    public void setDef(int def) {
        this.def = def;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"def",String.valueOf(def));
    }

    public void setHp(int hp) {
        this.hp = hp;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"hp",String.valueOf(hp));
    }

    public void lock(int lock) {
        this.lock = lock;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"lock",String.valueOf(lock));
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public String getName() {
        return name;
    }

    public int getElement() {
        return element;
    }

    public int getPveskill() {
        return pveskill;
    }
    public void setPveskill(int skill) {
        this.pveskill = skill;
        Rdm.instance().hmset(KEY_BUDDY(role,id),"pveskill",String.valueOf(skill));
    }

    public int getPvpskill() {
        return pvpskill;
    }

    public int getStar() {
        return star;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getHp() {
        return hp;
    }

    public int getCrit() {
        return crit;
    }

    public int getCrit_def() {
        return crit_def;
    }

    public int getCrit_atk() {
        return crit_atk;
    }

    public int getHit_rat() {
        return hit_rat;
    }

    public int getDodge() {
        return dodge;
    }

    public int getDice() {
        return dice;
    }

    public int getSyn_code() {
        return syn_code;
    }

    public int getCell_id() {
        return cell_id;
    }

    public int getLock() {
        return lock;
    }

    public int getSpeed() {
        return speed;
    }

    //获取伙伴的owner
    public long getRole(){
        return role;
    }





    //生成BuddyKey
    public static String KEY_BUDDY(long role,int bid ){
        return RDSKEY_BUDDY + ":" + role +":" + bid ;
    }

    /**
     *生成6位伙伴id
     * */
    private static int makeBid(){
        Random rand = new Random();
        int rad1 = rand.nextInt(99);
        int rad2 = rand.nextInt(999);
        int rad3 = rand.nextInt(8)+1;
        return rad3 * 100000 + rad2* 100 +rad1;
    }






}
