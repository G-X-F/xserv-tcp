package com.kunda.engine.utils;


import static com.kunda.engine.utils.SheetName.toEnSheet;

/**
 * 中英文配置表名称对照
 * */
public enum ExcelName {
    t_null(""),
    bty_("觅我游戏版配置表"),
    bdy_("伙伴数值表"),
    pkt_("背包数值表"),
    mal_("邮件通知表"),
    skl_("技能表"),
    ;







    private String chinese;//中文

    //构造
    ExcelName(String chinese){
        this.chinese = chinese;
    }

    //中文名称
    public String chinese(){
        return this.chinese;
    }


    private static ExcelName toEnTable(String tableName){

        switch (tableName){
            case "觅我游戏版配置表":
                return bty_;//beauty
            case "伙伴数值表":
                return bdy_;
            case "背包数值表":
                return pkt_;
            case "邮件通知表":
                return mal_;
            case "技能表":
                return skl_;


            default:
                return t_null;
        }
    }


    //确定配置表英文
    public static String en_table(String execl, String sheet){
        return  toEnTable(execl).name() + toEnSheet(sheet).name();
    }






















}