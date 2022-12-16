package com.yejunyu.im.dispathcer;

import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/23
 **/
public class GatewayInstanceManager {
    private GatewayInstanceManager() {
    }

    public static class Singleton {
        public static GatewayInstanceManager instance = new GatewayInstanceManager();
    }

    public static GatewayInstanceManager getInstance() {
        return Singleton.instance;
    }

    private ConcurrentHashMap<String, SocketChannel> gatewayInstances = new ConcurrentHashMap<>();

    /**
     * 添加一个接入实例
     *
     * @param channelId
     * @param socketChannel
     */
    public void addGatewayInstance(String channelId, SocketChannel socketChannel) {
        gatewayInstances.put(channelId, socketChannel);
    }

    /**
     * 删除一个接入实例
     *
     * @param channelId
     */
    public void removeGatewayInstance(String channelId) {
        gatewayInstances.remove(channelId);
    }
}
