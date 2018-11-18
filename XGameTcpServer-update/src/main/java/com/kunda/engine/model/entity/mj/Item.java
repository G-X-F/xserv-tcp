package com.kunda.engine.model.entity.mj;


import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.kunda.engine.utils.Const.RDSKEY_ITEM;

/**
 * 背包物品基类
 * */
public class Item {
    private  long id;//唯一标识(物品配置id + 随机码)
    private  long role;//角色id
    private  int item_id;//物品配置id
    private  String name;//名称
    private  int type;//物品类型(物品类型配置表)
    private  int num;//数量
    private  long value1;//参数1 type=16礼包：物品掉落ID type=100伙伴生成：伙伴初始化ID  type=200装备：装备数值ID type=300时装：时装初始化ID
    private  long value2;//参数2  type=200装备 装备初始ID
    private  int use;//是否可用 0 不可用 1 可用
    private  int lot_use;//是否可批量使用 0 不可 1 可以
    private  int use_level;//使用等级
    private  int visible;//显示分类 0 不显示 1 物品背包 2 装备背包  3 时装背包
    private  int pile;// 叠加 0 不可叠加  1 可叠加
    private  int pile_limit;// 叠加限制


    //构造函数
    public Item(Map<String,String> mp){
        this.id =Long.parseLong(mp.get("id"));
        this.role =Long.parseLong(mp.get("role"));
        this.item_id=Integer.parseInt(mp.get("item_id"));
        this.name=mp.get("name");
        this.type=Integer.parseInt(mp.get("type"));
        this.num=Integer.parseInt(mp.get("num"));
        this.value1=Integer.parseInt(mp.get("value1"));
        this.value2 = Integer.parseInt(mp.get("value2"));
        this.use = Integer.parseInt(mp.get("use"));
        this.lot_use = Integer.parseInt(mp.get("lot_use"));
        this.use_level =Integer.parseInt(mp.get("use_level")) ;
        this.visible =Integer.parseInt(mp.get("visible")) ;
        this.pile =Integer.parseInt(mp.get("pile")) ;
        this.pile_limit =Integer.parseInt(mp.get("pile_limit")) ;
    }



    //根据id获得物品信息
    public static Item getItemById(long role,long id){
        if(Rdm.instance().exists(KEY_ITEM(role,id))){
            Map<String,String> mp =  Rdm.instance().hgetAll(KEY_ITEM(role,id));
            return new Item(mp);
        }
        return null;
    }



    //创建一个物品
    public static long createItem( long role,int item_id ,int num){
        long id = makeId(item_id);
        while (Rdm.instance().exists( KEY_ITEM(role,id) )){
            id =  makeId(item_id);
        }

        if(!Rdm.instance().exists( KEY_ITEM(role,id) )){//不存在该ITEM

           String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",item_id).split(",");
           String[] typeconf = ExcelCache.inner().get("背包数值表","物品类型配置表",Integer.parseInt(itemconf[2])).split(",");

            //创建Buddy表
            Map<String,String> itemmp = new HashMap<>();
            itemmp.put("id",String.valueOf(id));//唯一标识
            itemmp.put("role",String.valueOf(role));//角色id
            itemmp.put("item_id",String.valueOf(item_id));//物品配置id
            itemmp.put("name",itemconf[1]);//名称
            itemmp.put("type",itemconf[2]);//类型
            itemmp.put("num",String.valueOf(num));//物品数量
            itemmp.put("value1",itemconf[3]);//参数1
            itemmp.put("value2",itemconf[4]);//参数2
            itemmp.put("use",itemconf[5]);//可用
            itemmp.put("lot_use",typeconf[5]);//批量使用
            itemmp.put("use_level",itemconf[6]);//使用等级
            itemmp.put("visible",typeconf[2]);//显示分类 0 不显示 1 物品背包 2 装备背包  3 时装背包
            itemmp.put("pile",typeconf[3]);//是否可叠加
            itemmp.put("pile_limit",typeconf[4]);//叠加上限

            Rdm.instance().hmset(KEY_ITEM(role,id),itemmp);
        }

        return  id;
    }


    //移除一个物品
    public  void rmvItem(){
        if(Rdm.instance().exists( KEY_ITEM(role,id) )){
            Rdm.instance().del(KEY_ITEM(role,id));
        }
    }

    //静态 修改物品数量
    public static void modifyItemNum(long role,long id,int num){
        if(Rdm.instance().exists( KEY_ITEM(role,id) )){
            Rdm.instance().hmset(KEY_ITEM(role,id),"num",String.valueOf(num));
        }
    }


    //转成pb数据
    public PbBodyPacket.Item toPbBody(){
        PbBodyPacket.Item.Builder item = PbBodyPacket.Item.newBuilder();
        item.setId(this.id);
        item.setItemid(this.item_id);
        item.setNum(this.num);
        return item.build();
    }

    //修改物品数量
    public void setNum(int num){
        this.num =num;
        if(Rdm.instance().exists( KEY_ITEM(role,id) )){
            Rdm.instance().hmset(KEY_ITEM(role,id),"num",String.valueOf(num));
        }
    }

    public long getRole() {
        return role;
    }

    public long getId() {
        return id;
    }

    public int getItem_id() {
        return item_id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getNum() {
        return num;
    }

    public long getValue1() {
        return value1;
    }

    public long getValue2() {
        return value2;
    }

    public int getUse() {
        return use;
    }

    public int getUse_level() {
        return use_level;
    }

    public int getLot_use() {
        return lot_use;
    }

    public int getVisible() {
        return visible;
    }

    public int getPile() {
        return pile;
    }

    public int getPile_limit() {
        return pile_limit;
    }

    //生成ItemKey
    public static String KEY_ITEM(long role,long id){
        return RDSKEY_ITEM + ":" + role +":" + id ;
    }


    /**
     *生成itemId
     * */
    private static long makeId(int item_id ){
        Random rand = new Random();
        return item_id * 1000 +  rand.nextInt(999); //最后3位为随机码
    }


}
