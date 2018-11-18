package com.kunda.engine.core;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;

import static com.kunda.engine.common.fun.Messages.SendHttpErr;
import static com.kunda.engine.utils.Const.*;
import static com.kunda.engine.utils.ECode.E10002;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 作为服务端受理请求的基础Handler类
 * */
@ChannelHandler.Sharable
public class BaseServerHandler extends ChannelInboundHandlerAdapter implements IRequestHandler {
	private static final Logger logger = LoggerFactory.getLogger(BaseServerHandler.class);

	//Channel属性绑定Uid
	protected AttributeKey<String> CTX_ATTR_UID = AttributeKey.valueOf("CTX_ATTR_UID");
	protected AttributeKey<String> CTX_ATTR_RID = AttributeKey.valueOf("CTX_ATTR_RID");
	protected ProtobuffFrame.Request request = ProtobuffFrame.Request.getDefaultInstance();
	protected ProtobuffFrame.Response.Builder response = ProtobuffFrame.Response.newBuilder();
	/**
	 * 读取通道消息
	 * */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			//http协议支持
			if(msg instanceof FullHttpRequest){

				FullHttpRequest request =  (FullHttpRequest)msg;

				try {
					gate(request);
					httpResponse(ctx,distribute(ctx));
				} catch (Exception e) {
					logger.error("",e);
				}finally {
					if(ctx.channel().isActive()){
						ctx.close();
					}
				}
				request.release();
			}else if(msg instanceof ProtobuffFrame.Request){ //tcp协议
				request = ProtobuffFrame.Request.parseFrom(((ProtobuffFrame.Request) msg).toByteArray());
				//请求分发
				dispatcher(ctx);
			}

		}catch (Exception e){
			logger.error("",e);
		}

	}




	/**
	 *
	 * 消息分发
	 * */
	private void dispatcher(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException, ParseException {
		logger.info("h<= [" + CMD.getCmdById(request.getCmd()) +"#" + request.getSub() + "][len:" + request.getBody().size() + "]");

		BaseServerHandler handler = (BaseServerHandler) XHandler.getInstance().findHandler(request.getCmd(), XHandler.SocketType.protobuf);
		handler.request = this.request;
		handler.handleClientProtoBuffRequest(ctx);//路由到Hander处理

	}


	/**
	 * 1、消息路由策略
	 * 2、合法性检查
	 * 3、返回消息
	 */
	private ProtobuffFrame.Response distribute(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {


		logger.info("h<= [" + CMD.getCmdById(request.getCmd()) +"#" + request.getSub() + "][len:" + request.getBody().size() + "]");

		BaseServerHandler handler =(BaseServerHandler) XHandler.getInstance().findHandler(request.getCmd(), XHandler.SocketType.http);
		if(handler == null) return SendHttpErr(request.getCmd(),request.getSub(), E10002, "未注册的请求!");
		handler.request = this.request;
		return handler.handleClientHttpRequest(ctx);
	}





	@Override
	public void channelInactive(ChannelHandlerContext ctx) {

		Group group =	Groups.inst().getGroup(CH_KEY_SERVER);
		if(group != null) group.rmvChannel(ctx.channel());//移出全服频道

		ctx.fireChannelInactive();
	}









	/**
	 * http外部接口转换、放通
	 * */
	private void gate(FullHttpRequest request) throws IOException {

		if(!request.content().toString(CharsetUtil.UTF_8).equals(BLANK)){
			this.request = ProtobuffFrame.Request.parseFrom( fromByteBuf(request.content()) );
		}

	}


	//ByteBuf转比特数组
	public static byte[] fromByteBuf(ByteBuf buff ){
		int length = buff.writerIndex() - buff.readerIndex();
		byte[] bytes = new byte[length]; // 传入的Byte数据
		buff.getBytes(buff.readerIndex(), bytes);
		return bytes;
	}


	/*
	 * 服务器回应消息
	 *
	 * **/
	private void httpResponse(ChannelHandlerContext ctx , ProtobuffFrame.Response pbdata)  {

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK);

		response.headers().set("Access-Control-Allow-Origin","*");//跨域问题
		response.headers().set("Access-Control-Allow-Methods","GET,POST,PUT,DELETE");
		response.headers().set("Access-Control-Allow-Headers","Content-type,uid,token,cmd,cpno");
		//response.headers().set("Content-Encoding", "gzip");
		response.headers().set("Tansfer-Encoding","chunked");
		response.headers().set("Content-Type", "application/octet-stream;charset=utf-8");

		response.content().writeBytes(pbdata.toByteArray());
		ctx.writeAndFlush(response);
		logger.info("h=> ["+ ctx.channel().id()+ "]["+CMD.getCmdById(pbdata.getCmd())+"#"+pbdata.getSub()+"][code:"+pbdata.getCode() +"]\n");
	}






	@Override
	public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException, ParseException {
		//do nothing
	}

	@Override
	public ProtobuffFrame.Response  handleClientHttpRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException, SocketException {
		return null;
		//do nothing
	}

}
