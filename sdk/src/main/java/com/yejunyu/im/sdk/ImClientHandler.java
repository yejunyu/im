package com.yejunyu.im.sdk;

import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.Message;
import com.yejunyu.im.common.Response;
import com.yejunyu.im.protocal.Authentication;
import com.yejunyu.im.protocal.MessageSend;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/20
 **/
public class ImClientHandler extends ChannelInboundHandlerAdapter {

    private final ImClient imClient;

    public ImClientHandler(ImClient imClient) {
        this.imClient = imClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.imClient.reconnect();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 代表建立连接完毕
        // 在这里可以发送一个token到服务端进行认证
        System.out.println("建立了连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 服务端发送过来的消息就是在这里收到的
        Message message = new Message((ByteBuf) msg);
        System.out.println("收到TCP接入系统发送过来的消息，消息类型为：" + message.getMessageType());
        if (message.getMessageType() == Constants.MESSAGE_TYPE_RESPONSE) {
            Response response = message.toResponse();

            if (response.getRequestCmd() == CMD.AUTHENTICATE.getType()) {
                Authentication.Response authenticateResponse = Authentication.Response.parseFrom(response.getBody());
                System.out.println("认证请求收到响应：" + authenticateResponse);
            }
            if (response.getRequestCmd() == CMD.SEND_MESSAGE.getType()) {
                MessageSend.Response messageSendResponse = MessageSend.Response.parseFrom(response.getBody());
                System.out.println("客户端收到发送单聊消息的响应，messageId= " + messageSendResponse.getMessageId());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常断开连接
        ctx.close();
    }
}
