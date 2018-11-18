package com.kunda.engine.handles.socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kunda.engine.core.BaseServerHandler;
import com.kunda.engine.model.proto.*;
import com.kunda.engine.utils.CMD;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kunda.engine.common.fun.Messages.SendTo;
import static com.kunda.engine.model.entity.mj.Role.*;
import static com.kunda.engine.utils.Const.ZERO;


public class BuddyPveStockRequestHandler extends BaseServerHandler {

        private static final Logger logger = LoggerFactory.getLogger(BuddyPveStockRequestHandler.class);

        public static int requestId = CMD.t_pve_stock.id();



        /**
         * 伙伴pve出战阵容
         * */
        @Override
        public void handleClientProtoBuffRequest(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {

            switch (request.getSub()) {
                case 0://打开阵容
                    openstock(ctx);
                    break;
                case 1://设置/更新阵容
                    upstock(ctx);
                    break;
                default:
                    openstock(ctx);

            }


        }




        //设置/更新阵容
        private void upstock(ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            //解析参数
            PbBodyBuddy.PveStock body = PbBodyBuddy.PveStock.parseFrom(request.getBody());
            List<Integer> bids  = body.getIdList();//细胞ID
            Map<String,String> mp = new HashMap<>();
            for(int i = 0; i<bids.size();i++){
                if(bids.get(i)!=0){
                    mp.put(String.valueOf(i),String.valueOf(bids.get(i)));// 位置 - 伙伴id
                }
            }

            //更新阵容
            upPveStock(Long.parseLong(rid), mp );

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(body.toByteString());

            SendTo(ctx,response.build());
        }



        //打开阵容
        private void openstock(ChannelHandlerContext ctx){
            String rid = ctx.channel().attr(CTX_ATTR_RID).get();//角色ID

            PbBodyBuddy.PveStock.Builder body = PbBodyBuddy.PveStock.newBuilder();
            Map<String,String> mp = getPveStock(Long.parseLong(rid));
            for(int i =0 ; i< 6 ; i++ ){
                if(mp.containsKey(String.valueOf(i))){
                    body.addId(Integer.parseInt(mp.get(String.valueOf(i))));
                }else {
                    body.addId(0);
                }
            }

            response.setCmd(requestId);
            response.setSub(request.getSub());
            response.setCode(ZERO);
            response.setBody(body.build().toByteString());

            SendTo(ctx,response.build());
        }


























}
