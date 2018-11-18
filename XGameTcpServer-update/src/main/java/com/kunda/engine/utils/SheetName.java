package com.kunda.engine.utils;


/**
 * 中英文表Sheet名称对照
 * */
public enum SheetName {
    t_null(""),
    //觅我游戏版配置表
    world_zone("世界分区"),
    account_level("账号等级"),
    life_health_exp("养生经验"),
    level_up_time("升级时间"),
    level_up_conf("升级配置表"),
    charm_ratio("魅力系数"),
    rater_ratio("评委系数"),
    attr_conf("属性配置表"),
    attr_name_conf("属性名称配置表"),
    skill_conf("技能配置表"),
    buff_conf("buff配置表"),
    buff_state_conf("buff状态配置表"),
    immune_conf("免疫配置表"),
    //伙伴数值表
    buddy("伙伴"),
    buddy_level("等级表"),
    buddy_star_expend("升星消耗"),
    piece_synth("碎片合成"),
    dice_init("伙伴骰面初始ID"),
    //邮件通知表
    mail("邮件"),
    sys_notice("系统通知"),
    //背包数值表
    item_conf("物品配置表"),
    item_type_conf("物品类型配置表"),
    //技能表
    skill_level("技能等级"),
    skill("技能"),
    ;







    private String chinese;//中文

    //构造
    SheetName(String chinese){
        this.chinese = chinese;
    }

    //中文名称
    public String chinese(){
        return this.chinese;
    }


    public static SheetName toEnSheet(String chinese){

        switch (chinese){
            //觅我游戏版配置表
            case "世界分区":
                return world_zone;
            case "账号等级":
                return account_level;
            case "养生经验":
                return life_health_exp;
            case "升级时间":
                return level_up_time;
            case "升级配置表":
                return level_up_conf;
            case "评委系数":
                return rater_ratio;
            case "魅力系数":
                return charm_ratio;
            case "属性配置表":
                return attr_conf;
            case "属性名称配置表":
                return attr_name_conf;
            case "技能配置表":
                return skill_conf;
            case "buff配置表":
                return buff_conf;
            case "免疫配置表":
                return immune_conf;
            case "buff状态配置表":
                return buff_state_conf;
            //伙伴数值表
            case "伙伴":
                return buddy;
            case "等级表":
                return buddy_level;
            case "升星消耗":
                return buddy_star_expend;
            case "碎片合成":
                return piece_synth;
            case "伙伴骰面初始ID":
                return dice_init;
            //背包数值表
            case "物品配置表":
                return item_conf;
            case "物品类型配置表":
                return item_type_conf;
            //邮件通知表
            case "邮件":
                return mail;
            case "系统通知":
                return sys_notice;
            //技能表
            case "技能等级":
                return skill_level;
            case "技能":
                return skill;

            default:
                return t_null;
        }




    }






















}