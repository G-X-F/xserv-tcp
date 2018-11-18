package com.kunda.engine.utils;



/**
 * 物品类型
 * */
public enum ItemType {
    t_acc_exp(1), //账号经验
    t_gold(2),//金币
    t_stone(3),//钻石
    t_hp(4),//体力
    t_mood(5),//心情指数
    t_role_exp(6),//伴侣经验
    t_item_exp(11),//经验道具
    t_item_gold(12),//金币道具
    t_item_stone(13),//钻石道具
    t_item_hp(14),//体力道具
    t_item_mood(15),//心情道具
    t_gift_packet(16),//礼包
    t_buddy_piece(20),//伙伴碎片
    t_item_normal(21),//常规道具
    t_buddy(100),//伙伴卡
    t_equip(200),//装备
    t_dress(300),//时装
    ;







    private int type;//类型

    //构造
    ItemType(int type){
        this.type = type;
    }

    //类型
    public int type(){
        return this.type;
    }


    private static ItemType getItemType(int num){

        switch (num){
            case 1:
                return t_acc_exp;
            case 2:
                return t_gold;
            case 3:
                return t_stone;
            case 4:
                return t_hp;
            case 5:
                return t_mood;
            case 6:
                return t_role_exp;
            case 11:
                return t_item_exp;
            case 12:
                return t_item_gold;
            case 13:
                return t_item_stone;
            case 14:
                return t_item_hp;
            case 15:
                return t_item_mood;
            case 16:
                return t_gift_packet;
            case 20:
                return t_buddy_piece;
            case 21:
                return t_item_normal;
            case 100:
                return t_buddy;
            case 200:
                return t_equip;
            case 300:
                return t_dress;




            default:
                return t_acc_exp;
        }
    }
























}