package com.yejunyu.im.gateway.tcp.dispatcher;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/23
 **/
public class DispatcherInstanceManager {
    /**
     * 分发实例列表
     */
    private static final List<DispatcherInstanceAddress> DISPATCHER_INSTANCE_ADDRESSES = new ArrayList<>();

    /**
     * 静态化分发系统实例地址列表
     */
    static {
        // 分发系统的服务ip和端口
        DISPATCHER_INSTANCE_ADDRESSES.add(new DispatcherInstanceAddress("localhost", "127.0.0.1", 8090));
    }

    private DispatcherInstanceManager() {
    }

    /**
     * 单例
     */
    private static class Singleton {
        private static final DispatcherInstanceManager INSTANCE = new DispatcherInstanceManager();
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static DispatcherInstanceManager getInstance() {
        return Singleton.INSTANCE;
    }

    /**
     * 分发系统channel集合
     */
    private final Map<String, DispatcherInstance> dispatcherInstanceMap = new ConcurrentHashMap<>();

    /**
     * 初始化,与分发系统建立连接
     */
    public void init() {
        // 主动跟一批dispatcher建立长链接
        for (DispatcherInstanceAddress dispatcherInstanceAddress : DISPATCHER_INSTANCE_ADDRESSES) {
            try {
                connectDispatcherInstance(dispatcherInstanceAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 随机选择一个分发系统分发消息
     *
     * @return
     */
    public DispatcherInstance chooseDispatcherInstance() {
        List<DispatcherInstance> dispatcherInstanceList = new ArrayList<>(dispatcherInstanceMap.values());
        Random random = new Random();
        int i = random.nextInt(dispatcherInstanceList.size());
        return dispatcherInstanceList.get(i);
    }

    public void removeDispatcherInstance(String dispatcherInstanceId) {
        dispatcherInstanceMap.remove(dispatcherInstanceId);
    }

    private void connectDispatcherInstance(DispatcherInstanceAddress dispatcherInstanceAddress) throws InterruptedException {
        final EventLoopGroup threadGroup = new NioEventLoopGroup();
        Bootstrap client = new Bootstrap();
        client.group(threadGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                        ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
//                        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
//                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new DispatcherInstanceHandler());
                    }
                });
        ChannelFuture channelFuture = client.connect(dispatcherInstanceAddress.getIp(), dispatcherInstanceAddress.getPort());
        // 给异化的连接加上监听器
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if (channelFuture1.isSuccess()) {
                DispatcherInstance dispatcherInstance = new DispatcherInstance((SocketChannel) channelFuture1.channel());
                dispatcherInstanceMap.put(channelFuture1.channel().id().asShortText(), dispatcherInstance);
                System.out.println("已经跟分发系统建立连接，分发系统地址为：" + channelFuture.channel().remoteAddress());
            } else {
                channelFuture1.channel().close();
                threadGroup.shutdownGracefully();
            }
        });
        channelFuture.sync();
    }
}
