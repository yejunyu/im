package com.yejunyu.im.dispathcer;

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

/**
 * @Author yjy
 * @Description //消息分发系统启动类
 * @Date 2022/11/23
 **/
public class DispatcherServer {

    public static int PORT = 8090;

    public static void main(String[] args) {
        // 按照层与层的关系而言，应该是接入系统主动分发系统去建立连接
        // 应该是让分发系统用的是Netty的服务端的代码，去监听一个端口号，等待人家跟他建立连接
        // 但凡建立好连接之后，就可以把接入系统的长连接缓存在这个组件里

        EventLoopGroup connectionThreadGroup = new NioEventLoopGroup();
        EventLoopGroup ioThreadGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(connectionThreadGroup, ioThreadGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
//                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
//                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new DispatcherHandler());
                        }

                    });

            ChannelFuture channelFuture = server.bind(PORT).sync();
            System.out.println("消息分发系统已经启动......");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionThreadGroup.shutdownGracefully();
            ioThreadGroup.shutdownGracefully();
        }
    }


}
