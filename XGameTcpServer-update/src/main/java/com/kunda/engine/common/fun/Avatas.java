package com.kunda.engine.common.fun;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by 亚文 on 2016/9/9.
 */
public class Avatas {

    private static final Logger logger = LoggerFactory.getLogger(Avatas.class);

    //  6|203,207,206^0&205,205,205,205^1
    public static final   String MARK_LIST = "|";
    public static final   String MARK_LIST_T = "\\|";
    public static final   String MARK_ARRAY = ",";

    private static final  double EARTH_RADIUS = 6378137;//赤道半径(单位m)


/* --------------------------------------------------------------------------------------------------------------------
 * 常规数组操作    通用
 * ---------------------------------------------------------------------------------------------------------------------
 */

    //数组转字符串
    public static String Array2String(Integer[] array) {
        String str = "";
        if (array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                str += MARK_ARRAY + array[i];
            }
        }

        str = str.replaceFirst(MARK_ARRAY, "");
        return str;
    }

    //字符串转数组
    public static Integer[] String2Array(String str) {
        Integer[] arryInt = new Integer[0];
        if (!str.equals("")) {
            String[] arryStr = str.split(MARK_ARRAY);
            arryInt = new Integer[arryStr.length];
            for (int i = 0; i < arryStr.length; i++) {
                arryInt[i] = Integer.parseInt(arryStr[i]);
            }
        }

        return arryInt;
    }

    //列表<数组> 转字符串
    public static String ArrayList2String(List<Integer[]> arrayList) {

        String str = "";
        if (arrayList.size() > 0) {
            for (Integer[] array : arrayList) {
                str += MARK_LIST;
                if (array != null) {
                    String strArr = "";
                    for (int i = 0; i < array.length; i++) {
                        strArr += MARK_ARRAY + array[i];
                    }
                    strArr = strArr.replaceFirst(MARK_ARRAY, "");
                    str += strArr;
                }

            }
        }

        str = str.replaceFirst(MARK_LIST, "");

        return str;
    }

    //字符串转 列表<数组>
    public static List<Integer[]> String2ArrayList(String str) {

        List<Integer[]> list = new ArrayList<>();
        if (!str.equals("")) {
            String[] arryStr = str.split(MARK_LIST_T);
            for (int i = 0; i < arryStr.length; i++) {
                Integer[] arryIn = String2Array(arryStr[i]);
                list.add(arryIn);
            }
        }
        return list;
    }


    //数组 转 列表
    public static List<Integer> Array2List(Integer[] array) {
        List<Integer> list = new ArrayList<>();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
        }
        return list;
    }

    //列表 转 数组
    public static Integer[] List2Array(List<Integer> list) {
        Integer[] array = new Integer[0];
        if (list != null && list.size() > 0) {
            array = new Integer[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
        }
        return array;
    }

    //字符列表 转 字符数组
    public static String[] StringList2StringArray(List<String> list) {
        String[] arr = new String[list.size()];
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
        }

        return arr;
    }

    //字符数组 转 字符列表
    public static List<String> StringArray2StringList(String[] arr) {
        List<String> list = new ArrayList<>();
        if (arr.length > 0) {
            for (int i = 0; i < arr.length; i++) {
                list.add(arr[i]);
            }

        }
        return list;
    }



    //判断两数组相等
    public static boolean ArrayEquals(Integer[] arr1, Integer[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }

        for (int i = 0; i < arr1.length; i++) {
            if (Contains(arr2, arr1[i])) {
                arr2 = ArrayRemove(arr2, arr1[i]);
            }
        }

        if (arr2.length == 0) {
            return true;
        }
        return false;
    }



    //数组初始化
    public static Integer[] ArrayInit(int value, int num) {
        Integer[] arr = new Integer[num];
        for (int i = 0; i < num; i++) {
            arr[i] = value;
        }

        return arr;
    }

    //数组长初始化
    public static Integer[] ArrayLongInit(int start, int len) {
        Integer[] arr = new Integer[len];
        for (int i = 0; i < len; i++) {
            arr[i] = start;
            start++;
        }

        return arr;
    }

    //字符数组初始化
    public static String[] StringArrayInit(String value, int num) {
        String[] sarr = new String[num];
        for (int i = 0; i < sarr.length; i++) {
            sarr[i] = value;
        }
        return sarr;
    }

    //数组横向相加
    public static Integer[] ArrayPerAdd(Integer[] score, int value) {
        for (int i = 0; i < score.length; i++) {
            score[i] += value;
        }

        return score;
    }

    //排序
    public static Integer[] ArraySort(Integer[] cards) {
        if (cards != null) {
            for (int i = 0; i < cards.length; i++) {
                Integer tmp = cards[i];
                for (int j = i + 1; j < cards.length; j++) {
                    if (tmp > cards[j]) {
                        tmp = cards[j];
                        cards[j] = cards[i];
                        cards[i] = tmp;
                    }
                }
            }
        }
        return cards;
    }


    //找出最大
    public static Integer ArrayMax(Integer[] cards) {
        Integer temp = null;
        if (cards != null) {
            temp = cards[0];
            for (int i = 0; i < cards.length; i++) {
                if (temp < cards[i]) {
                    temp = cards[i];
                }
            }
        }
        return temp;
    }

    //找出最大数的索引
    public static int ArrayMaxIndex(Integer[] cards) {
        Integer temp = null;
        int indexTemp = 0 ;
        if (cards != null) {
            temp = cards[0];
            for (int i = 0; i < cards.length; i++) {
                if (temp < cards[i]) {
                    temp = cards[i];
                    indexTemp = i;
                }
            }
        }
        return indexTemp;
    }

    //找出最小
    public static Integer ArrayMin(Integer[] cards) {
        Integer temp = null;
        if (cards != null) {
            temp = cards[0];
            for (int i = 0; i < cards.length; i++) {
                if (temp > cards[i]) {
                    temp = cards[i];
                }
            }
        }
        return temp;
    }


    //数组删除一个数字
    public static Integer[] ArrayRemove(Integer[] cards, Integer ocard) {
        int count = 0;
        Integer[] reCards = new Integer[cards.length - 1];
        int j = 0;
        for (int i = 0; i < cards.length; i++) {
            if (count == 0 && cards[i].equals(ocard)) {
                count++;
            } else {
                reCards[j] = cards[i];
                j++;
            }
        }
        return reCards;
    }

    //数组删除 子数组
    public static Integer[] ArrayRemove(Integer[] cards, Integer[] ocards) {
        for (int i = 0; i < ocards.length; i++) {
            if (Contains(cards, ocards[i])) {
                cards = ArrayRemove(cards, ocards[i]);
            }
        }
        return cards;
    }


    //全局删除
    public static Integer[] ArrayRemoveAll(Integer[] cards, Integer ocard) {
        List<Integer> list =  Array2List(cards);
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()){
            if(it.next().equals(ocard) ){
                it.remove();
            }
        }

        if(list.size()==0){
            return new Integer[0];
        }

        return List2Array(list);
    }


    //数组增加
    public static Integer[] ArrayAdd(Integer[] cards, Integer ocard) {
        Integer[] reCards = null;
        if (cards != null) {
            reCards = new Integer[cards.length + 1];
            for (int i = 0; i < cards.length; i++) {
                reCards[i] = cards[i];
            }
            reCards[cards.length] = ocard;
        } else {
            reCards = new Integer[1];
            reCards[0] = ocard;
        }

        return reCards;
    }

    //数组增加 子数组
    public static Integer[] ArrayAdd(Integer[] cards, Integer[] ocard) {
        Integer[] reCards = new Integer[cards.length + ocard.length];
        for (int i = 0; i < cards.length; i++) {
            reCards[i] = cards[i];
        }
        for (int j = 0; j < ocard.length; j++) {
            reCards[cards.length + j] = ocard[j];
        }
        return reCards;
    }


    //数组拷贝
    public static Integer[] ArrayCopy(Integer[] cards) {
        Integer[] ncards = new Integer[cards.length];
        if (cards.length > 0) {
            for (int i = 0; i < cards.length; i++) {
                ncards[i] = cards[i];
            }
        }
        return ncards;
    }

    //数组包含
    public static boolean Contains(Integer[] cards, Integer ocard) {
        if (cards != null && cards.length > 0) {
            for (int i = 0; i < cards.length; i++) {
                if (cards[i].equals(ocard)) {
                    return true;
                }
            }
        }
        return false;
    }


    //子数组包含
    public static boolean Contains(Integer[] cards, Integer[] subCards) {
        if (cards != null && cards.length > 0) {
            for (int i = 0; i < subCards.length; i++) {
                if (Contains(cards, subCards[i])) {
                    cards = ArrayRemove(cards, subCards[i]);
                } else {
                    return false;
                }
            }
        }
        return true;
    }


    //求数组的单一集合
    public static Integer[] ArraySingle(Integer[] cardPool) {
        Integer[] single = {};

        for (int i = 0; i < cardPool.length; i++) {
            if (!Contains(single, cardPool[i])) {
                single = ArrayAdd(single, cardPool[i]);
            }
        }
        return single;
    }


    //按值排序
    public static Integer[] ArraySortValue(Integer[] cards) {
        if (cards != null) {
            for(int i = 0; i < cards.length; ++i) {
                Integer tmp = cards[i];

                for(int j = i + 1; j < cards.length; ++j) {
                    if (tmp%100 > cards[j]%100) {
                        tmp = cards[j];
                        cards[j] = cards[i];
                        cards[i] = tmp;
                    }
                }
            }
        }

        return cards;
    }

    //数组中元素计数
    public static int ArrayCount(Integer[] cards, Integer ocard) {
        int count = 0;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].equals(ocard)) {
                count++;
            }
        }
        return count;
    }


    //计算GPS距离
    public static int disGPS(String[] point1, String[] point2) {

        double lon1 = Double.parseDouble(point1[0]);
        double lat1 = Double.parseDouble(point1[1]);

        double lon2 = Double.parseDouble(point2[0]);
        double lat2 = Double.parseDouble(point2[1]);

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double radLon1 = rad(lon1);
        double radLon2 = rad(lon2);

        if (radLat1 < 0)
            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
        if (radLat1 > 0)
            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
        if (radLon1 < 0)
            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
        if (radLat2 < 0)
            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
        if (radLat2 > 0)
            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
        if (radLon2 < 0)
            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
        double x1 = EARTH_RADIUS * Math.cos(radLon1) * Math.sin(radLat1);
        double y1 = EARTH_RADIUS * Math.sin(radLon1) * Math.sin(radLat1);
        double z1 = EARTH_RADIUS * Math.cos(radLat1);

        double x2 = EARTH_RADIUS * Math.cos(radLon2) * Math.sin(radLat2);
        double y2 = EARTH_RADIUS * Math.sin(radLon2) * Math.sin(radLat2);
        double z2 = EARTH_RADIUS * Math.cos(radLat2);

        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        //余弦定理求夹角
        double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS * EARTH_RADIUS - d * d) / (2 * EARTH_RADIUS * EARTH_RADIUS));
        return  new Double(theta * EARTH_RADIUS).intValue();
    }

    //经纬度转为弧度
    public static double rad(double d) {
        return d * Math.PI / 180.0;
    }


    //字符串追加，分隔符[,]
    public static  String AppendStr(String vector, String str){
        if(vector==null || "".equals(vector)) return str;
        vector+= MARK_ARRAY + str;
        return vector;
    }


    //手机号检查
    public static boolean isPhoneNumber(String number){
        //第一位数字为1 第二位数字为3 4 5 6 7 8 9 第三位往后为0-9 一共十一位
        String telRegex = "[1][3456789]\\d{9}";
        if (number == null){
            return false;
        }else{
            return number.matches(telRegex);
        }
    }

    //是否是数字
    public static boolean isNumberic(String str){
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }



    //ByteBuf转比特数组
    public static byte[] fromByteBuf(ByteBuf buff ){
        int length = buff.writerIndex() - buff.readerIndex();
        byte[] bytes = new byte[length]; // 传入的Byte数据
        buff.getBytes(buff.readerIndex(), bytes);
        return bytes;
    }

    //字符串转比特数组
    public static byte[] fromString(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        byte[] result = new byte[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Byte.parseByte(strings[i]);
        }
        return result;
    }








}
