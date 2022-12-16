package com.yejunyu.im.dispathcer;

import com.yejunyu.im.common.*;
import com.yejunyu.im.protocal.Authentication;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/23
 **/
public class DispatcherHandler extends ChannelInboundHandlerAdapter {
    /**
     * 一个接入系统跟分发系统建立了连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String channelId = RequestHelper.getChannelId(channel);
        GatewayInstanceManager gatewayInstanceManager = GatewayInstanceManager.getInstance();
        gatewayInstanceManager.addGatewayInstance(channelId, channel);
        System.out.println("已经跟TCP接入系统建立连接，TCP接入系统地址为：" + channel.remoteAddress());
    }

    /**
     * 一个接入系统跟分发系统的连接断开了
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String channelId = RequestHelper.getChannelId(channel);
        GatewayInstanceManager gatewayInstanceManager = GatewayInstanceManager.getInstance();
        gatewayInstanceManager.removeGatewayInstance(channelId);
        System.out.println("跟TCP接入系统的连接断开，地址为：" + channel);
    }

    /**
     * 接收到一个接入系统发送过来的请求
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestHelper requestHelper = RequestHelper.getInstance();
        Message message = new Message((ByteBuf) msg);
        if (message.getMessageType() == Constants.MESSAGE_TYPE_REQUEST) {
            Request request = message.toRequest();
            if (request.getRequestCmd() == CMD.AUTHENTICATE.getType()) {
                // 找单点登录系统进行认证
                Authentication.Request authenticateRequest = Authentication.Request.parseFrom(request.getBody());
                System.out.println("收到TCP接入系统发送的认证请求：" + authenticateRequest);
                Authentication.Response authenticateResponse = requestHelper.authenticate(authenticateRequest);
                if (authenticateResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                    SocketChannel channel = (SocketChannel) ctx.channel();
                    String channelId = RequestHelper.getChannelId(channel);
                    // todo 在这把channel写入分布式session系统中
                    System.out.println("在Redis中写入分布式Session......");
                }
                Response response = new Response(request, authenticateResponse.toByteArray());
                ctx.writeAndFlush(response.getBuffer());
                System.out.println("返回响应给TCP接入系统：" + authenticateResponse);

            }
        }
    }

    /**
     * 处理完毕一个请求
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 发生异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
