package com.kunda.engine.tools;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kunda.engine.utils.Const.QMargin;

/**
 *
 * 二维码工具类
 * */
public class ZXingCodeUtil {


    private static final Logger logger = LoggerFactory.getLogger(ZXingCodeUtil.class);

    //二维码颜色
    private static final int BLACK = 0xFF000000;
    //二维码颜色
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * ZXing 方式生成二维码
     * @param text    二维码内容
     * @param width    二维码宽
     * @param height    二维码高
     * @param logoPath   输入背景图路径
     * @param outPutPath   二维码生成保存路径
     * @param imageType        二维码生成格式
     */
    public static BufferedImage zxingCodeCreate(String text, int width, int height,String logoPath, String outPutPath, String imageType) throws WriterException, IOException {
        Map<EncodeHintType, String> his = new HashMap<EncodeHintType, String>();
        //设置编码字符集
        his.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //1、生成二维码
        BitMatrix encode = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, his);


        //2、获取二维码宽高
        int codeWidth = encode.getWidth();
        int codeHeight = encode.getHeight();


        //3、将二维码放入缓冲流
        BufferedImage image = new BufferedImage(codeWidth, codeHeight, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0 + QMargin ; i < codeWidth - QMargin; i++) {
            for (int j = 0 + QMargin; j < codeHeight - QMargin; j++) {
                //4、循环将二维码内容定入图片
                image.setRGB(i, j, encode.get(i, j) ? BLACK : WHITE);
            }
        }

        URL url = new URL(logoPath);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        //通过输入流获取图片数据
        InputStream inStream = conn.getInputStream();

        AddBackImge2Code(image,  imageType,  outPutPath,  inStream);

       return image;
    }



    //给二维码添加背景图片
    public static void AddBackImge2Code(BufferedImage image, String imageType, String outPutPath, InputStream logoInputStream) throws IOException {

        BufferedImage logo = ImageIO.read(logoInputStream);//读取背景图片数据流
        Graphics2D g = logo.createGraphics();
        int width = image.getWidth();
        int height = image.getHeight();
        int x = 460;
        int y = 465;
        // 绘制背景图
        g.drawImage(image, x, y, width, height, null);
        g.dispose();

        File outPutImage = new File(outPutPath);
        //如果图片不存在,创建图片
        if(!outPutImage.exists()) {
            outPutImage.createNewFile();
        }

        ImageIO.write(logo, imageType, outPutImage);

    }











    /**
     * 二维码解析
     * @param analyzePath    二维码路径
     * @return
     * @throws IOException
     */
    public static Object zxingCodeAnalyze(String analyzePath) throws Exception{
        MultiFormatReader formatReader = new MultiFormatReader();
        Object result = null;
        try {
            File file = new File(analyzePath);
            if (!file.exists())
            {
                return "二维码不存在";
            }
            BufferedImage image = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            result = formatReader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            logger.error("",e);
        }
        return result;
    }




























}
