package com.yejunyu.im.sdk;

import com.yejunyu.im.protocal.Authentication;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.omg.CORBA.PRIVATE_MEMBER;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/20
 **/
public class ImClient {
    /**
     * 代表的是Netty客户端中的线程池
     */
    private EventLoopGroup threadGroup;
    /**
     * 代表的是Netty客户端
     */
    private Bootstrap client;
    /**
     * 代表的是客户端APP跟TCP接入系统的某台机器的长连接
     */
    private SocketChannel socketChannel;

    /**
     * 头部信息
     */
    private static final int HEADER_LENGTH = 20;
    private static final String DELIMITER = "$_";
    private static final int APP_SDK_VERSION = 1;
    private static final int REQUEST_TYPE_AUTHENTICATE = 1;
    private static final int SEQUENCE = 1;


    public void connect(String host, int port) throws Exception {
        this.threadGroup = new NioEventLoopGroup();
        this.client = new Bootstrap();
        client.group(threadGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ByteBuf delimiter = Unpooled.copiedBuffer(DELIMITER.getBytes());
                        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
//                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new ImClientHandler());
                    }
                });
        System.out.println("完成netty客户端的配置");
        ChannelFuture future = client.connect(host, port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    socketChannel = (SocketChannel) channelFuture.channel();
                    System.out.println("跟TCP接入系统完成长连接的建立");
                } else {
                    channelFuture.channel().close();
                    threadGroup.shutdownGracefully();
                }
            }
        });
        future.sync();
    }

    /**
     * 发起用户认证
     *
     * @param uid   uid
     * @param token token
     */
    public void authenticate(String uid, String token) {
        ByteBuf byteBuf = assembleProtobuf(uid, token);

        // 这样接收不到信息
//        String message = "发起用户认证|" + uid + "|" + token + "$_";
        socketChannel.writeAndFlush(byteBuf);
        System.out.println("向TCP接入系统发起用户认证请求");

    }

    private ByteBuf assembleProtobuf(String uid, String token) {
        Authentication.Request.Builder builder = Authentication.Request.newBuilder();
        Authentication.Request request = builder.setUid(uid).setToken(token)
                .setTimestamp(System.currentTimeMillis()).build();
        byte[] bytes = request.toByteArray();
        ByteBuf byteBuf = Unpooled.buffer(HEADER_LENGTH + bytes.length + DELIMITER.length());
        byteBuf.writeInt(HEADER_LENGTH);
        byteBuf.writeInt(APP_SDK_VERSION);
        byteBuf.writeInt(REQUEST_TYPE_AUTHENTICATE);
        byteBuf.writeInt(SEQUENCE);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        byteBuf.writeBytes(DELIMITER.getBytes());
        return byteBuf;
    }

    /**
     * 发送消息
     *
     * @param uid
     * @param message
     */
    public void send(String uid, String message) {
        byte[] messageBytes = (message + "|" + uid + "$_").getBytes();
        ByteBuf byteBuf = Unpooled.copiedBuffer(messageBytes);
        socketChannel.writeAndFlush(byteBuf);
        System.out.println("向TCP接入系统发送第一条消息，推送给test002用户");

    }

    /**
     * 关闭跟机器的连接1
     *
     * @throws Exception
     */
    public void close() throws Exception {
        this.socketChannel.close();
        this.threadGroup.shutdownGracefully();
    }
}
