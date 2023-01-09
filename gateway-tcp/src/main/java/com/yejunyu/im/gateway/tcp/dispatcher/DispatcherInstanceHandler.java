package com.yejunyu.im.gateway.tcp.dispatcher;

import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.Message;
import com.yejunyu.im.common.Response;
import com.yejunyu.im.gateway.tcp.SessionManager;
import com.yejunyu.im.protocal.Authentication;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/23
 **/
public class DispatcherInstanceHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开与分发系统的联系
        SocketChannel channel = (SocketChannel) ctx.channel();
        String dispatcherChannelId = channel.id().asShortText();
        DispatcherInstanceManager instance = DispatcherInstanceManager.getInstance();
        instance.removeDispatcherInstance(dispatcherChannelId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = new Message((ByteBuf) msg);
        System.out.println("收到分发系统发送来的消息，消息类型为：" + message.getMessageType());

        // 如果是分发系统回过来的消息
        if (message.getMessageType() == Constants.MESSAGE_TYPE_RESPONSE) {
            Response response = message.toResponse();

            if (response.getRequestCmd() == CMD.SEND_MESSAGE.getType()) {
                // 把认证的返回结果原封不动的返给用户即可
                // 如果认证成功, 此时需要设置本地session和分布式session
                // 可以查找这个响应对应的是哪个uid, 然后根据uid找到session,然后把返回结果发送给用户
                Authentication.Response authenticateResponse = Authentication.Response.parseFrom(response.getBody());
                String uid = authenticateResponse.getUid();
                System.out.println("收到分发系统返回的响应：" + authenticateResponse);
                SessionManager sessionManager = SessionManager.getInstance();
                SocketChannel client = sessionManager.getClient(uid);
                client.writeAndFlush(response);
                System.out.println("将响应发送到客户端，uid=" + uid + "，客户端地址为：" + client);

            }
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
