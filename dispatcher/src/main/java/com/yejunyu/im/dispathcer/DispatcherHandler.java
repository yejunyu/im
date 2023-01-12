package com.yejunyu.im.dispathcer;

import com.alibaba.fastjson2.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yejunyu.im.common.*;
import com.yejunyu.im.protocal.Authentication;
import com.yejunyu.im.protocal.MessageSend;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

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
        String channelId = ChannelUtil.getChannelId(channel);
        GatewayInstanceManager gatewayInstanceManager = GatewayInstanceManager.getInstance();
        gatewayInstanceManager.addGatewayInstance(channelId, channel);
        System.out.println("已经跟gateway接入系统建立连接，TCP接入系统地址为：" + channel.remoteAddress());
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
        String channelId = ChannelUtil.getChannelId(channel);
        GatewayInstanceManager gatewayInstanceManager = GatewayInstanceManager.getInstance();
        gatewayInstanceManager.removeGatewayInstance(channelId);
        System.out.println("跟gateway接入系统的连接断开，地址为：" + channel);
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
        Message message = new Message((ByteBuf) msg);
        System.out.println("收到一条消息，消息类型为：" + message.getRequestCmd());

        if (message.getMessageType() == Constants.MESSAGE_TYPE_REQUEST) {
            Request request = message.toRequest();
            if (request.getRequestCmd() == CMD.SEND_MESSAGE.getType()) {
                sendMessage(ctx, request);
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

    /**
     * 分发系统发出单聊消息
     *
     * @param ctx
     * @param request
     */
    private void sendMessage(ChannelHandlerContext ctx, Request request) throws InvalidProtocolBufferException {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String channelId = ChannelUtil.getChannelId(channel);
        MessageSend.Request messageSendRequest = MessageSend.Request.parseFrom(request.getBody());
        ImSend imSend = new ImSend();
        imSend.setSenderId(messageSendRequest.getSenderId());
        imSend.setReceiverId(messageSendRequest.getReceiverId());
        imSend.setContent(messageSendRequest.getContent());
        imSend.setCmd(CMD.SEND_MESSAGE.getType());
        imSend.setSequence(request.getSequence());
        imSend.setChannelId(channelId);

        // 消息发送到kafka去
        KafkaManager kafkaManager = KafkaManager.getInstance();
        KafkaProducer<String, String> producer = kafkaManager.getProducer();

        ProducerRecord<String, String> record = new ProducerRecord<>(Constants.SEND_MSG_TOPIC, JSON.toJSONString(imSend));
        producer.send(record);

    }
}
