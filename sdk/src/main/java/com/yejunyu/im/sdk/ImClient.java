package com.yejunyu.im.sdk;

import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.Request;
import com.yejunyu.im.protocal.Authentication;
import com.yejunyu.im.protocal.MessageSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
     * 代表的是本客户端
     */
    private Bootstrap client;
    /**
     * 代表的是客户端APP跟TCP接入系统的某台机器的长连接
     */
    private SocketChannel socketChannel;

    private volatile boolean isConnected;


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
//                        ByteBuf delimiter = Unpooled.copiedBuffer(DELIMITER.getBytes());
//                        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
//                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new ImClientHandler(ImClient.this));
                    }
                });
        ChannelFuture future = client.connect(host, port);
        System.out.println(host + port + " : 客户端发起对tcp接入系统的连接。。。");
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                socketChannel = (SocketChannel) channelFuture.channel();
                System.out.println(host + port + " : 跟TCP接入系统完成长连接的建立");
            } else {
                channelFuture.channel().close();
                threadGroup.shutdownGracefully();
            }
        });
        future.sync();
    }

    /**
     * 通过iplist服务得到可用的ip重连
     */
    public void reconnect() throws Exception {
        String uid = "";
        String token = "";
        String ip = "";
        int port = -1;
        connect(ip, port);
        authenticate(uid, token);
    }

    /**
     * 发起用户认证
     *
     * @param uid   uid
     * @param token token
     */
    public void authenticate(String uid, String token) {
        ByteBuf byteBuf = assembleProtobuf(uid, token);
        socketChannel.writeAndFlush(byteBuf);
        System.out.println(uid + " : 向TCP接入系统发起用户认证请求");


    }

    private ByteBuf assembleProtobuf(String uid, String token) {
        Authentication.Request.Builder builder = Authentication.Request.newBuilder();
        Authentication.Request request = builder.setUid(uid).setToken(token)
                .setTimestamp(System.currentTimeMillis()).build();
        byte[] bytes = request.toByteArray();
        ByteBuf byteBuf = Unpooled.buffer(Constants.HEADER_LENGTH + bytes.length);
        byteBuf.writeInt(Constants.HEADER_LENGTH);
        byteBuf.writeInt(Constants.APP_SDK_VERSION_1);
        byteBuf.writeInt(Constants.MESSAGE_TYPE_REQUEST);
        byteBuf.writeInt(CMD.AUTHENTICATE.getType());
        byteBuf.writeInt(Constants.SEQUENCE_DEFAULT);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

    /**
     * 发送单聊消息
     *
     * @param senderId   发送uid
     * @param receiverId
     */
    public void sendMsg(String senderId, String receiverId, String content) {
        MessageSend.Request.Builder builder = MessageSend.Request.newBuilder();
        builder.setSenderId(senderId)
                .setReceiverId(receiverId)
                .setContent(content);
        Request request = new Request(Constants.APP_SDK_VERSION_1, CMD.SEND_MESSAGE.getType(), Constants.SEQUENCE_DEFAULT,
                builder.build().toByteArray());
        System.out.println("客户端向接入系统发送一条单聊消息......");
        socketChannel.writeAndFlush(request.getBuffer());
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

    public void connected(){
        this.isConnected = true;
    }
}
