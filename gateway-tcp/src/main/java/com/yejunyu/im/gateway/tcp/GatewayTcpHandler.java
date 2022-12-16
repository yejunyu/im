package com.yejunyu.im.gateway.tcp;

import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.Message;
import com.yejunyu.im.common.Request;
import com.yejunyu.im.protocal.Authentication;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/20
 **/
public class GatewayTcpHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("跟客户端完成连接：" + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        ClientManager instance = ClientManager.getInstance();
        instance.removeClient(channel);
        System.out.println("检测到客户端的连接断开，删除其连接缓存：" + channel.remoteAddress().toString());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ClientManager clientManager = ClientManager.getInstance();
        // ctx.channel()->对应客户端的socketChannel
        // 一旦token认证完毕，就把这个socketChannel缓存起来
        // 后面如果有需要对这个客户端推送消息，从缓存里找这个socketChannel
        // 此时服务端可以推送消息给客户端了

        // 请求处理组件
        RequestHelper requestHelper = RequestHelper.getInstance();
        // 解析收到的请求
        Message message = new Message((ByteBuf) msg);
        System.out.println("收到一个消息，消息类型为：" + message);

        // 如果是认证请求
        if (message.getMessageType() == Constants.MESSAGE_TYPE_REQUEST) {
            Request request = message.toRequest();
            if (request.getRequestCmd()== CMD.AUTHENTICATE.getType()){
               // 消息序列化成认证请求
                Authentication.Request authenticateRequest = Authentication.Request.parseFrom(request.getBody());
                System.out.println("收到客户端发送过来的认证请求：" + authenticateRequest);
                requestHelper.authenticate(authenticateRequest);
            }
        } else {
            if (!clientManager.existClient(uid)) {
                System.out.println("未认证用户，不能处理请求");
                byte[] response = "未认证用户，不能处理请求$_".getBytes();
                ByteBuf responseBuf = Unpooled.buffer(response.length);
                responseBuf.writeBytes(response);
                ctx.writeAndFlush(responseBuf);
            } else {
                // 发送消息
                System.out.println("将消息分发到Kafka中去：");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
        ctx.close();
    }
}
