package com.kunda.engine.tools.http;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);



    /**
     * 向指定URL发送GET方法的请求
     * @param url  发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        StringBuilder resp = new StringBuilder();

        HttpURLConnection conn = null;

        try {
            URL realUrl = new URL(url + param);
            // 打开和URL之间的连接
            conn = (HttpURLConnection)realUrl.openConnection();

            // 设置通用的请求属性
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("accept-encoding", "gzip");
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json"); //application/x-www-form-urlencoded application/json

            // 建立实际的连接
            conn.connect();

            if(conn.getResponseCode() == 200 ){
                //gzip解压数据流
                InputStreamReader  reader = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8");
                char[] buff = new char[1024];
                int length;
                while ( (length = reader.read(buff)) != -1  ) {
                    resp.append(new String(buff, 0, length));
                }
                reader.close();
            }

        } catch (Exception e) {
            logger.error("",e);
        }finally {
            if(conn != null) conn.disconnect();
        }
        return resp.toString();
    }



    /**
     * gzip 加密
     * */
    public static byte[] gzip(byte[] data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.finish();
        gzip.close();
        byte[] ret = bos.toByteArray();
        bos.close();
        return ret;
    }





    /**
     * 向指定 URL 发送POST方法的请求
     * @param url 发送请求的 URL
     * @param data 请求数据
     * @return 所代表远程资源的响应结果
     */
    public static byte[] sendPost(String url, byte[] data,String content_type){
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        HttpURLConnection  conn = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            conn =(HttpURLConnection)  realUrl.openConnection();
            conn.setConnectTimeout(15000);
            // 设置通用的请求属性
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", content_type); //application/x-www-form-urlencoded application/json application/octet-stream;charset=utf-8
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            OutputStream out = conn.getOutputStream();
            // 发送请求参数
            out.write(data);
            // flush输出流的缓冲
            out.flush();
            out.close();
            if(conn.getResponseCode() == 200 ) {
                // 定义BufferedReader输入流来读取URL的响应
                InputStream in = conn.getInputStream();

                byte[] buff = new byte[1024];
                int length;
                while ((length = in.read(buff)) != -1) {
                    swapStream.write(buff,0,length);
                }
                in.close();
            }

        } catch (Exception e) {
            logger.error("",e);
        }finally {
            if(conn!= null) conn.disconnect();
        }

        return swapStream.toByteArray();
    }
}