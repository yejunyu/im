package com.yejunyu.im.gateway.tcp;

import com.yejunyu.im.gateway.tcp.dispatcher.DispatcherInstanceManager;
import com.yejunyu.im.gateway.tcp.push.PushManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/20
 **/
public class GatewayTcpServer {
    public static int PORT = 8080;

    public static void main(String[] args) {
        // 启动消息推送组件
        PushManager pushManager = new PushManager();
        pushManager.start();

        // 启动消息分发组件
        DispatcherInstanceManager instance = DispatcherInstanceManager.getInstance();
        instance.init();

        EventLoopGroup connectionThreadGroup = new NioEventLoopGroup();
        EventLoopGroup ioThreadGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(connectionThreadGroup, ioThreadGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addFirst(new GatewayTcpHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            connectionThreadGroup.shutdownGracefully();
            ioThreadGroup.shutdownGracefully();
        }

    }
}
