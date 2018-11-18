package com.kunda.engine.common.fun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static com.kunda.engine.common.fun.Avatas.AppendStr;

public class OsMacIP {

    private static final Logger logger = LoggerFactory.getLogger(OsMacIP.class);

    /**
     * 获取本机所有IP
     */
    public static String InnerIpAddress() throws SocketException {
        String res = "";
        InetAddress ip;
        Enumeration  netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
            Enumeration nii = ni.getInetAddresses();
            while (nii.hasMoreElements()) {
                ip = (InetAddress) nii.nextElement();
                if (ip.getHostAddress().indexOf(":") == -1 && !ip.getHostAddress().equals("127.0.0.1")) {
                    res = AppendStr(res,ip.getHostAddress());
                }
            }
        }

        return  res;
    }


    /**
     * 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
     */
    public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }




    /**
     * 获取本机所有物理地址
     *
     * @return
     */
    public static String getMacAddress() throws Exception {
        String mac = "";
        String os = getOSName();
        if (os.equals("windows 7")) {
             mac = getWin7MACAddress();
        } else if (os.startsWith("windows")) {
            // 本地是windows
            mac = getWindowsMACAddress();
        }else if (os.startsWith("mac")) {
            // mac os x
            mac = getMacOSMacAddress();
        } else {
            // 本地是非windows系统 一般就是unix
             mac = getUnixMACAddress();
        }

        return mac;
    }





    /**
     * 获取unix网卡的mac地址. 非windows的系统默认调用本方法获取.
     * 如果有特殊系统请继续扩充新的取mac地址方法.
     *
     * @return mac地址
     */
    private static String getUnixMACAddress() throws IOException {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process;
        try {
            // linux下的命令，一般取eth0作为本地主网卡
            process = Runtime.getRuntime().exec("ifconfig");
            // 显示信息中包含有mac地址信息
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[hwaddr]
                index = line.toLowerCase().indexOf("ether");
                if (index >= 0) {// 找到了
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + 6,index+23).trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("",e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return mac;
    }



    //mac os x 获取MAC地址的方法
    private static String getMacOSMacAddress()throws Exception{

        //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();

        //下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();

        for(int i=0;i<mac.length;i++){
            if(i!=0){
                sb.append(":");
            }
            //mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length()==1?0+s:s);
        }

        //把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toLowerCase();
    }



    /**
     * windows 7 专用 获取MAC地址
     *
     * @return
     * @throws Exception
     */
    private static String getWin7MACAddress() throws Exception {

        // 获取本地IP对象
        InetAddress ia = InetAddress.getLocalHost();
        // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

        // 下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            // mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }

        // 把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }


    /**
     * 获取widnows网卡的mac地址.
     *
     * @return mac地址
     */
    private static String getWindowsMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process;
        try {
            // windows下的命令，显示信息中包含有mac地址信息
            process = Runtime.getRuntime().exec("ipconfig /all");
            bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                // 寻找标示字符串[physical
                index = line.toLowerCase().indexOf("physical address");

                if (index >= 0) {// 找到了
                    index = line.indexOf(":");// 寻找":"的位置
                    if (index >= 0) {
                        System.out.println(mac);
                        // 取出mac地址并去除2边空格
                        mac = line.substring(index + 1).trim();
                    }
                    break;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.error("",e);
        }

        return mac;
    }









}
