package com.yejunyu.im.gateway.tcp.dispatcher;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/23
 **/
public class DispatcherInstanceManager {
    /**
     * 分发实例列表
     */
    private static List<DispatcherInstanceAddress> dispatcherInstanceAddressList = new ArrayList<>();

    /**
     * 静态化分发系统实例地址列表
     */
    static {
        dispatcherInstanceAddressList.add(new DispatcherInstanceAddress("localhost", "127.0.0.1", 8090));
    }

    private DispatcherInstanceManager() {
    }

    /**
     * 单例
     */
    static class Singleton {
        static DispatcherInstanceManager instance = new DispatcherInstanceManager();
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static DispatcherInstanceManager getInstance() {
        return Singleton.instance;
    }

    /**
     * 分发系统channel集合
     */
    private final List<SocketChannel> dispatcherInstanceList = new CopyOnWriteArrayList<>();

    public void init() {
        // 主动跟一批dispatcher建立长链接
        for (DispatcherInstanceAddress dispatcherInstanceAddress : dispatcherInstanceAddressList) {
            try {
                connectDispatcherInstance(dispatcherInstanceAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                dispatcherInstanceList.add((SocketChannel) channelFuture1.channel());
            } else {
                channelFuture1.channel().close();
                threadGroup.shutdownGracefully();
            }
        });
        channelFuture.sync();
    }
}
