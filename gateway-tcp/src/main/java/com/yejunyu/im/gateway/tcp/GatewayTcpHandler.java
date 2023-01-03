package com.yejunyu.im.gateway.tcp;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableMap;
import com.yejunyu.im.common.*;
import com.yejunyu.im.protocal.Authentication;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import redis.clients.jedis.Jedis;

import java.util.Map;

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
        SessionManager instance = SessionManager.getInstance();
        instance.removeClient(channel);
        System.out.println("检测到客户端的连接断开，删除其连接缓存：" + channel.remoteAddress().toString());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SessionManager sessionManager = SessionManager.getInstance();
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
            if (request.getRequestCmd() == CMD.AUTHENTICATE.getType()) {
                // 消息序列化成认证请求
                Authentication.Request authenticateRequest = Authentication.Request.parseFrom(request.getBody());
                System.out.println("收到客户端发送过来的认证请求：" + authenticateRequest);
                Authentication.Response authenticateResponse = requestHelper.authenticate(authenticateRequest);
                // 假如认证成功的话
                if (authenticateResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                    sessionManager.addClient(authenticateResponse.getUid(), (SocketChannel) ctx.channel());
                    // 记录分布式session
                    String sessionKey = "session_" + authenticateRequest.getUid();
                    Map<String, Object> sessionValue = ImmutableMap.of(
                            "token", authenticateRequest.getToken(),
                            "timestamp", authenticateRequest.getTimestamp()
                    );
                    Jedis jedis = JedisManager.getInstance().getJedis();
                    jedis.set(sessionKey, JSON.toJSONString(sessionValue));
                }
                Response response = new Response(request, authenticateResponse.toByteArray());
                ctx.writeAndFlush(response);
                System.out.println("返回响应给用户：" + authenticateResponse);
            } else {

            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
