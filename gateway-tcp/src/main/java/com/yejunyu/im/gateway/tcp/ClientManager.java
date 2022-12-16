package com.yejunyu.im.gateway.tcp;

import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/21
 **/
public class ClientManager {

    /**
     * 存储uid到客户端的映射
     */
    private final ConcurrentHashMap<String, SocketChannel> clients = new ConcurrentHashMap();
    /**
     * channelId到uid的映射
     */
    private final ConcurrentHashMap<String, String> channelId2UId = new ConcurrentHashMap<>();

    private ClientManager() {
    }

    public static class Singleton {
        static ClientManager instance = new ClientManager();
    }

    public static ClientManager getInstance() {
        return Singleton.instance;
    }

    /**
     * 添加一个连接好的客户端
     *
     * @param uid
     * @param channel
     */
    public void addClient(String uid, SocketChannel channel) {
        channelId2UId.put(channel.id().asLongText(), uid);
        clients.put(uid, channel);
    }

    /**
     * 判断认证过的客户端连接是否存在
     *
     * @param uid
     * @return
     */
    public boolean existClient(String uid) {
        return clients.containsKey(uid);
    }

    /**
     * 根据uid获取连接
     *
     * @param uid
     * @return
     */
    public SocketChannel getClient(String uid) {
        return clients.get(uid);
    }

    /**
     * 删除一个客户端连接
     *
     * @param channel
     */
    public void removeClient(SocketChannel channel) {
        String uid = channelId2UId.get(channel.id().asLongText());
        channelId2UId.remove(channel.id().asLongText());
        clients.remove(uid);
    }
}
