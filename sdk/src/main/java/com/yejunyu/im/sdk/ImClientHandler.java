package com.yejunyu.im.sdk;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/20
 **/
public class ImClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 代表建立连接完毕
        // 在这里可以发送一个token到服务端进行认证
        System.out.println("建立了连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 服务端发送过来的消息就是在这里收到的
        String message = (String) msg;
        System.out.println("收到TCP接入系统发送的消息：" + message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常断开连接
        ctx.close();
    }
}
