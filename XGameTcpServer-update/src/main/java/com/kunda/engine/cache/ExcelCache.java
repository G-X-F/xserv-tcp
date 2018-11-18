package com.kunda.engine.cache;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kunda.engine.utils.Const.KEY_TABLE;
import static com.kunda.engine.utils.ExcelName.en_table;


/**
 * 在内存中缓存Excel配置表
 *
 * @author yawen
 *
 */
public class ExcelCache {



     /**
     * Excel公共配置表
     */
    private Map<String, String> share = new HashMap<>();//key-record


    private static final ExcelCache instance = new ExcelCache();

    public static ExcelCache inner() {
        return instance;
    }


    /**
     * 增加配置项*/
    public void add(String key ,String value){
        share.put(key,value);
    }


    //获取一个配置项 参数：文件名称 sheet名称 记录id
    public String get(String table_cn, String sheet_cn, int recordid ){
       return share.get( KEY_TABLE(en_table(table_cn,sheet_cn),String.valueOf(recordid) ) );
    }

    //获取多个配置项 参数：文件名称 sheet名称 记录id
    public List<String> mult(String table_cn, String sheet_cn, Integer[] records ){
        List<String> list = new ArrayList<>();
        for(Integer record: records){
            list.add( share.get( KEY_TABLE(en_table(table_cn,sheet_cn),String.valueOf(record) ) ) )  ;
        }

        return list;
    }


    //是否包含
    public boolean contains(String table_cn, String sheet_cn, int recordid ){
        return share.containsKey( KEY_TABLE(en_table(table_cn,sheet_cn),String.valueOf(recordid) ) );
    }



}
