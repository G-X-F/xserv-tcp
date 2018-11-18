package com.kunda.engine.core;

import com.kunda.engine.model.proto.*;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏群
 *
 * @author yawen
 *
 */
public class Group {

    private static final Logger logger = LoggerFactory.getLogger(Group.class);

    /**
     * 客户端连接集合
     */
    private DefaultChannelGroup channelGroup;

    public Group(String name) {
        channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    /**
     * 添加连接到群
     *
     */
    public boolean addChannel(Channel ch) {
        return channelGroup.add(ch);
    }

    /**
     * 从群中删除连接
     *
     */
    public boolean rmvChannel(Channel ch) {
        return channelGroup.remove(ch);
    }

    /**
     * 广播消息
     * */
    public void broadcast(ProtobuffFrame.Response response){
        channelGroup.writeAndFlush(response);
    }

    /**
     * 获取群名称
     * */
    public String name(){
        return channelGroup.name();
    }

    /**
     * 获取群内连接的数量
     * */
    public int size(){
        return channelGroup.size();
    }


    /**
     * 群是否包含该连接
     * */

    public boolean contains(Channel channel){
        return channelGroup.contains(channel);
    }

    /**
     * 获取群内所有的连接
     * */
    public Channel[] all(){
        return (Channel[])channelGroup.toArray();
    }

}
