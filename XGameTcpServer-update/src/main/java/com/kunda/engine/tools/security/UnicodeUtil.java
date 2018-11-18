package com.kunda.engine.tools.security;

import com.kunda.engine.cache.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kunda.engine.tools.security.DESUtil.decrypt;
import static com.kunda.engine.tools.security.DESUtil.desCrypto;
import static com.kunda.engine.utils.Const.TOKEN_DELAY;


public class UnicodeUtil {


    private static final Logger logger = LoggerFactory.getLogger(UnicodeUtil.class);


    /**
     * 含有unicode 的字符串转一般字符串
     * @param unicodeStr 混有 Unicode 的字符串
     * @return
     */
    public static String unicodeStr2String(String unicodeStr) {
        //正则匹配条件，可匹配“\\u”1到4位，一般是4位可直接使用 String regex = "\\\\u[a-f0-9A-F]{4}";
        String regex = "\\\\u[a-f0-9A-F]{1,4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(unicodeStr);

        while(matcher.find()) {
            String oldChar = matcher.group();//原本的Unicode字符
            String newChar = decode(oldChar);//转换为普通字符
            unicodeStr = unicodeStr.replace(oldChar,newChar);

        }
        return unicodeStr;
    }

    /**
     * 字符串转换unicode
     * @param string
     * @return
     */
    public static String encode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }

    /**
     * unicode 转字符串
     * @param unicode 全为 Unicode 的字符串
     * @return
     */
    public static String decode(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }

        return string.toString();
    }




    //token 生成器 uid + 当前时间
    public static String tokenmaker(String uid ){
        String input = uid +"_"+ System.currentTimeMillis();
        byte[] doFinal = desCrypto(input.getBytes(), Conf.inner().get("token.pri.key"));
        return new HexBinaryAdapter().marshal(doFinal);
    }


    //Token 检查
    public static boolean checkToken(String uid,String token,boolean needCheck ){
        //如不需要检查token，直接返回TRUE
        if(!needCheck)return true;

        String input ="";
        try {
            byte[]  doFinal =   decrypt(new HexBinaryAdapter().unmarshal(token), Conf.inner().get("token.pri.key"));
            input = new String(doFinal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( !uid.equals(input.split("_")[0]) ) return  false ; //uid不匹配

        long time = Long.parseLong(input.split("_")[1]);
        long delay =  (System.currentTimeMillis() - time )/1000 ;

        return delay < TOKEN_DELAY ;//过期时间
    }

}
