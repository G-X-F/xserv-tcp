package com.kunda.engine.model.entity.mj;


import com.kunda.engine.cache.ExcelCache;
import com.kunda.engine.manager.redis.Rdm;
import com.kunda.engine.model.proto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.kunda.engine.model.entity.mj.Item.*;
import static com.kunda.engine.utils.Const.RDSKEY_PACKET;

/**
 * 背包物品基类
 * */
public class Packet {




    //向背包加入物品 (原则上不允许一次性加入超过物品堆叠限制的数量)
    public static long addItem2Packet(long role , int itemid, int num ){
        String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",itemid).split(",");
        String[] typeconf = ExcelCache.inner().get("背包数值表","物品类型配置表",Integer.parseInt(itemconf[2])).split(",");
        int visible = Integer.parseInt(typeconf[2]);//背包显示编号

        int pile = Integer.parseInt(typeconf[3]);//是否可堆叠

        if(pile == 0){ //不可堆叠的物品直接添加
            for(int i = 0; i< num ; i++ ){
                long nid =  createItem(  role, itemid , 1);//创建物品
                Rdm.instance().zadd(KEY_PACKET( role,visible),System.currentTimeMillis(),String.valueOf(nid));//加入id背包列表(zset)
                return nid;
            }
        }else { //可以堆叠的物品计算合堆
            List<Integer> list = canPileItems(role,itemid,visible);

            if(list.size() > 0){  //背包中该类型的物品堆叠未满的
                for(Integer id : list ){
                    Item itm = getItemById(role,id);
                    int sum = itm.getNum() + num;
                    if( sum <  itm.getPile_limit()  ){ //增加数量后没有满堆
                        modifyItemNum( role, id,sum);
                        return id;
                    }else {
                        modifyItemNum( role, id,itm.getPile_limit()); //满堆
                        num =  sum - itm.getPile_limit();
                    }
                }

                if(num > 0){ //补偿之后仍有剩余，新创建一个堆
                    long nid =  createItem(  role, itemid , num);//创建物品
                    Rdm.instance().zadd(KEY_PACKET( role,visible),System.currentTimeMillis(),String.valueOf(nid));//加入id背包列表
                    return nid;
                }
            }else { //需要重新创建一个物品
                long nid =  createItem(  role, itemid , num);//创建物品
                Rdm.instance().zadd(KEY_PACKET( role,visible),System.currentTimeMillis(),String.valueOf(nid));//加入id背包列表
                return nid;
            }
        }
        return 0;
    }


    //获取指定背包的物品列表
    public static PbBodyPacket.Items getPacketItems(long role , int type){

        PbBodyPacket.Items.Builder items = PbBodyPacket.Items.newBuilder();
        items.setPkt(type);//背包类型 1 物品 2 装备 3 时装

        if(Rdm.instance().exists( KEY_PACKET( role,type)) ){
            long size = Rdm.instance().zcard(KEY_PACKET( role,type));
            Set<String> ids = Rdm.instance().zrange(KEY_PACKET( role,type),0,size-1);
            for(String id : ids){
                Item itm = getItemById(role,Long.parseLong(id));
                PbBodyPacket.Item pb = itm.toPbBody();
                Set<String> img =  Rdm.instance().smembers(KEY_PACKET_BOOK(role)); //图鉴
                if(img.contains(String.valueOf(pb.getItemid()))){
                    items.addIts(pb.toBuilder());
                }else {
                    items.addNew(pb.toBuilder());
                }
            }
        }


        return items.build();
    }


    //整理指定背包的物品列表
    public static PbBodyPacket.Items SortPacketItems(long role , int visible){

        PbBodyPacket.Items.Builder items = PbBodyPacket.Items.newBuilder();
        items.setPkt(visible);//背包类型 1 物品 2 装备 3 时装

        if(Rdm.instance().exists( KEY_PACKET( role,visible)) ){
            long size = Rdm.instance().zcard(KEY_PACKET( role,visible));

            Set<String> ids = Rdm.instance().zrange(KEY_PACKET( role,visible),0,size);//(zset)
            System.out.println("ids: " + ids);

            //合堆并按权重排序
            for(String id : ids){
                Item itm = getItemById(role,Long.parseLong(id));
                System.out.println("id: " + id + " " + itm.getNum());

                //获取权重
                String[] itemconf = ExcelCache.inner().get("背包数值表","物品配置表",itm.getItem_id()).split(",");
                //重新按权重排序
                Rdm.instance().zadd(KEY_PACKET( role,visible),Integer.parseInt(itemconf[11]),id);

                //可堆叠的物品组且未满堆
                if(itm.getPile()== 1 && itm.getNum() < itm.getPile_limit()){

                    List<Integer> list = canPileItems(role,itm.getItem_id(),visible);//未满堆的同种物品
                    if(list.contains(Integer.parseInt(id))){
                        list.remove((list.indexOf(Integer.parseInt(id))));//移除自身
                    }
                    int num = itm.getNum();
                    if(list.size() > 0){  //背包中该类型的物品堆叠未满的
                        for(Integer nid : list ){
                            Item item_a = getItemById(role,nid);
                            int sum = item_a.getNum() + num;
                            if( sum <  item_a.getPile_limit() ){ //增加数量后没有满堆
                                modifyItemNum( role, nid,sum);
                                //删除自身
                                itm.rmvItem();
                                Rdm.instance().zrem(KEY_PACKET( role,visible),id);
                                break;
                            }else {
                                modifyItemNum( role, nid,item_a.getPile_limit()); //满堆
                                num =  sum - itm.getPile_limit();
                            }
                        }
                        if(num > 0){ //补偿之后仍有剩余
                            modifyItemNum( role, Long.parseLong(id),num);
                        }
                    }
                }
            }

            //重排序合堆之后
            size = Rdm.instance().zcard(KEY_PACKET( role,visible));
            Set<String> sort_ids = Rdm.instance().zrange(KEY_PACKET( role,visible),0,size);//(zset)


            for(String id : sort_ids){
                Item itm = getItemById(role,Long.parseLong(id));
                PbBodyPacket.Item pb = itm.toPbBody();
                Set<String> img =  Rdm.instance().smembers(KEY_PACKET_BOOK(role)); //图鉴
                if(img.contains(String.valueOf(pb.getItemid()))){
                    items.addIts(pb);
                }else {
                    items.addNew(pb);
                }
            }
        }


        return items.build();
    }



    //背包中可堆叠的该类物品id列表
    public   static List<Integer> canPileItems(long role , int item_id , int visible){
        ArrayList<Integer> list = new ArrayList<>();
        long size = Rdm.instance().zcard(KEY_PACKET( role,visible));
        Set<String> items = Rdm.instance().zrange(KEY_PACKET( role,visible),0,size-1);

        for(String id : items){
            if(Integer.parseInt(id.substring(0,id.length()-3) ) == item_id){ //物品配置id相同
                Item itm = getItemById(role,Long.parseLong(id));
                if(itm.getNum() < itm.getPile_limit()){
                    list.add(Integer.parseInt(id));
                }
            }
        }
        return list;
    }

    //背包中该类物品id列表
    public   static List<Integer> sameItems(long role , int item_id , int visible){
        ArrayList<Integer> list = new ArrayList<>();
        long size = Rdm.instance().zcard(KEY_PACKET( role,visible));
        Set<String> items = Rdm.instance().zrange(KEY_PACKET( role,visible),0,size-1);

        for(String id : items){
            if(Integer.parseInt(id.substring(0,id.length()-3) ) == item_id){ //物品配置id相同
                Item itm = getItemById(role,Long.parseLong(id));
                    list.add(Integer.parseInt(id));
            }
        }
        return list;
    }














    //生成背包Key
    public static String KEY_PACKET(long role,int type){
        return RDSKEY_PACKET + ":" + role +":" + type ;
    }

    //生成背包图鉴Key
    public static String KEY_PACKET_BOOK(long role){
        return RDSKEY_PACKET + ":" + role +":book" ;
    }




}
