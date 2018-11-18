package com.kunda.engine.handles.socket;

import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.utils.Const.ZERO;

public class HeartBeatRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(HeartBeatRequestHandler.class);

        public static int requestId = CMD.t_heartbeat.id();

        /**
         * 心跳服务 可用于网络信号检测
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx)  {



            this.response.setCmd(requestId);
            this.response.setSub(this.request.getSub());
            this.response.setCode(ZERO);
            this.response.setBody(this.request.getBody());

            SendTo(ctx,this.response.build());
        }

}
