package com.kunda.engine.core;

import com.kunda.engine.cache.Conf;
import com.kunda.engine.cache.WTimer;
import com.kunda.engine.manager.redis.*;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.model.task.ReloadConfigTask;
import com.kunda.engine.model.task.pool.ServerThreadPool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.kunda.engine.model.task.ReloadConfigTask.loadSystemSetting;
import static com.kunda.engine.register.HandlerRegister.registerAllXml;


public class ServerBootStrap {


	private static final Logger logger = LoggerFactory.getLogger(ServerBootStrap.class);

	private static final ServerBootStrap instance = new ServerBootStrap();
	public static  ServerBootStrap getInstance(){
		return instance;
	}


	//线程池设置
	private static final EventLoopGroup BossGroup = new NioEventLoopGroup(1);//设置接收线程池
	private static final EventLoopGroup WorkerGroup = new NioEventLoopGroup(1);//设置工作线程池(默认系统cpu核数*2)
	private static final EventLoopGroup ServerGroup = new NioEventLoopGroup(2,new ServerThreadPool());//设置工作线程池(默认系统cpu核数*2)



	private  void run() throws Exception{



		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			ServerBootstrap protstrap = new ServerBootstrap();

			bootstrap.group(BossGroup, WorkerGroup)
					.channel(NioServerSocketChannel.class)
					//---------------option设置--------------------------
					.option(ChannelOption.SO_BACKLOG, 128) //临时等待处理连接队列长度
					.option(ChannelOption.SO_REUSEADDR,true)//端口重复绑定
					.option(ChannelOption.SO_RCVBUF, 10*1024) //接收缓冲区长度
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//内存池分配方案
					//---------------------------------------------------
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch){
							ch.pipeline().addLast("http-response", new HttpResponseEncoder());//Http响应编码器
							ch.pipeline().addLast("http-request", new HttpRequestDecoder());//Http请求解码器
							ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));//http对象聚合器
							ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());//分块发送
							ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());//利用包头中的包含数组长度来识别半包粘包
							ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(ProtobuffFrame.Request.getDefaultInstance()));//配置Protobuf解码处理器
							ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());// 用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
							ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());//配置Protobuf编码器
							ch.pipeline().addLast(ServerGroup,new BaseServerHandler());//tcp服务

						}
					});

			protstrap.group(BossGroup, WorkerGroup)
					.channel(NioServerSocketChannel.class)
					//---------------option设置--------------------------
					.option(ChannelOption.SO_BACKLOG, 128) //临时等待处理连接队列长度
					.option(ChannelOption.SO_REUSEADDR,true)//端口重复绑定
					.option(ChannelOption.SO_RCVBUF, 10*1024) //接收缓冲区长度
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//内存池分配方案
					//---------------------------------------------------
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch){
							ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());//利用包头中的包含数组长度来识别半包粘包
							ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(ProtobuffFrame.Request.getDefaultInstance()));//配置Protobuf解码处理器
							ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());// 用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
							ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());//配置Protobuf编码器
							ch.pipeline().addLast(ServerGroup,new BaseServerHandler());//http服务基础服务

						}
					});


			//-------------------------------------------------------init------------------------------------------
			redisRdmInit();//redis数据库初始化
			loadSystemSetting();//加载系统配置文件config.properties
			WTimer.instance().add(new ReloadConfigTask(),0, TimeUnit.SECONDS);//定时更新配置
			registerAllXml();

			//-----------------------------------------------------------------------------------------------------

			ChannelFuture httpfuture = bootstrap.bind(Integer.parseInt(Conf.inner().get("xserver.http.port"))).sync();
			ChannelFuture protfuture = protstrap.bind(Integer.parseInt(Conf.inner().get("xserver.tcp.port"))).sync();
			httpfuture.channel().closeFuture().sync();
			protfuture.channel().closeFuture().sync();
		}finally{
			BossGroup.shutdownGracefully();
			WorkerGroup.shutdownGracefully();
			ServerGroup.shutdownGracefully();
		}
		
	}




	//返回配置文件地址
	private  String findConfigRootPath(){
		 String fileName ="config.properties";
		 String xpath0 ="target" +"/" + "classes";
		 String xpath1 ="conf";
		 String fileToLoad =xpath0 + "/" + fileName ;

		File file = new File(fileToLoad);
		if(!file.exists()){
			return xpath1 + "/" + fileName;
		}
		return  xpath0 + "/" + fileName;
	}


	//即时读取配置文件
	public Properties loadDefaultConfigFile(){
		String fileName = findConfigRootPath();
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}







	//redis数据库初始化
	private void redisRdmInit(){
		Rdm.instance().init();//redis 初始化 
	}






	
	public static void main(String[] args) throws Exception {
	    ServerBootStrap.getInstance().run();

	}
	
}
