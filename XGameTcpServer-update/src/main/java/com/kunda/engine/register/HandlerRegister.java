package com.kunda.engine.register;

import com.kunda.engine.core.IRegister;
import com.kunda.engine.core.XHandler;
import com.kunda.engine.handles.http.SysMailHttpHandler;
import com.kunda.engine.handles.socket.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class HandlerRegister implements IRegister{

    private static final Logger logger = LoggerFactory.getLogger(HandlerRegister.class);




    /**
     * 注册Handler接口
     * */
    public  void register(){

        //------------------------------------------注册TCP服务-----------------------------------------------
        //注册
        XHandler.getInstance().addHandler(LoginRequestHandler.requestId,new LoginRequestHandler(), XHandler.SocketType.protobuf);
        //心跳
        XHandler.getInstance().addHandler(HeartBeatRequestHandler.requestId,new HeartBeatRequestHandler(), XHandler.SocketType.protobuf);
        //向账户服务器获取用户信息
        XHandler.getInstance().addHandler(UserInfoRequestHandler.requestId,new UserInfoRequestHandler(), XHandler.SocketType.protobuf);
        //创建角色
        XHandler.getInstance().addHandler(CreatRoleRequestHandler.requestId,new CreatRoleRequestHandler(), XHandler.SocketType.protobuf);
        //删除角色
        XHandler.getInstance().addHandler(DeleteRoleRequestHandler.requestId,new DeleteRoleRequestHandler(), XHandler.SocketType.protobuf);
        //进入游戏
        XHandler.getInstance().addHandler(EnterGameRequestHandler.requestId,new EnterGameRequestHandler(), XHandler.SocketType.protobuf);
        //角色技能管理接口
        XHandler.getInstance().addHandler(RoleSkillRequestHandler.requestId,new RoleSkillRequestHandler(), XHandler.SocketType.protobuf);
        //角色比美接口
        XHandler.getInstance().addHandler(BeautyContestRequestHandler.requestId,new BeautyContestRequestHandler(), XHandler.SocketType.protobuf);
        //伙伴入库抽卡(创建伙伴)接口
        XHandler.getInstance().addHandler(CreatBuddyRequestHandler.requestId,new CreatBuddyRequestHandler(), XHandler.SocketType.protobuf);
        //伙伴Pve阵容接口
        XHandler.getInstance().addHandler(BuddyPveStockRequestHandler.requestId,new BuddyPveStockRequestHandler(), XHandler.SocketType.protobuf);
        //伙伴碎片合成/查看
        XHandler.getInstance().addHandler(BuddyPieceRequestHandler.requestId,new BuddyPieceRequestHandler(), XHandler.SocketType.protobuf);
        //伙伴升级/升星
        XHandler.getInstance().addHandler(StarLevelUpRequestHandler.requestId,new StarLevelUpRequestHandler(), XHandler.SocketType.protobuf);
        //系统邮件/通告
        XHandler.getInstance().addHandler(SystemMailRequestHandler.requestId,new SystemMailRequestHandler(), XHandler.SocketType.protobuf);
        //背包系统
        XHandler.getInstance().addHandler(PacketSystemRequestHandler.requestId,new PacketSystemRequestHandler(), XHandler.SocketType.protobuf);
        //使用物品
        XHandler.getInstance().addHandler(ItemSysRequestHandler.requestId,new ItemSysRequestHandler(), XHandler.SocketType.protobuf);






        //---------------------------------------------注册http服务-------------------------------------------------------------------------


        //GM系统邮件
        XHandler.getInstance().addHandler(SysMailHttpHandler.requestId,new SysMailHttpHandler(), XHandler.SocketType.http);









    }



    //返回配置文件地址
    private static String findXmlRootPath(){
        String fileName ="config.properties";
        String xpath0 ="target" +"/" + "classes";
        String xpath1 ="conf";
        String fileToLoad =xpath0 + "/" + fileName ;

        File file = new File(fileToLoad);
        if(!file.exists()){
            return xpath1 ;
        }
        return  xpath0 ;
    }



    //注册所有XmL配置项
    public static void registerAllXml() throws IOException, JDOMException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        registerHandlers();
    }


    //注册handlers
    public static void registerHandlers() throws IOException, JDOMException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String rpath = findXmlRootPath() + "/register";
        File rfile = new File(rpath);
        if (rfile.exists()) {
            //handler
            String[] handlefiles = rfile.list(new FileSuffixFilter("-register.xml"));//文件名过滤器
            if(handlefiles == null) return;
            for (String file: handlefiles) {
                String xmlfile = rpath + "/" + file;
                SAXBuilder saxBuilder = new SAXBuilder();
                InputStream in = new FileInputStream(new File(xmlfile));
                Document document = saxBuilder.build(in);
                Element rootElement = document.getRootElement();
                List<Element> list = rootElement.getChildren();
                for (Element element : list ) {
                    String classPath = element.getAttribute("class").getValue();
                    Class cls = Class.forName(classPath);
                    IRegister obj = (IRegister) cls.newInstance();
                    obj.register();
                    logger.info(file + " handlers registered!");
                }
            }
        }
    }









}
