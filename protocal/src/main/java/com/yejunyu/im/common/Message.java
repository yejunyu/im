package com.yejunyu.im.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/26
 **/
@Getter
@Setter
public class Message {
    /**
     * 消息头长度
     */
    int headerLength;
    /**
     * 客户端SDK版本号
     */
    int appSdkVersion;
    /**
     * 消息类型: 请求/响应
     */
    int messageType;
    /**
     * 请求类型
     */
    int requestCmd;
    /**
     * 顺序消息的序列号
     */
    int sequence;
    /**
     * 消息体长度
     */
    int bodyLength;
    /**
     * 消息体
     */
    byte[] body;
    /**
     * 消息体bytebuffer
     */
    ByteBuf buffer;

    public Message(int appSdkVersion, int messageType, int requestCmd, int sequence, byte[] body) {
        this.headerLength = Constants.HEADER_LENGTH;
        this.appSdkVersion = appSdkVersion;
        this.messageType = messageType;
        this.requestCmd = requestCmd;
        this.sequence = sequence;
        this.body = body;
        this.bodyLength = body.length;

        // 封装完整的带消息头的消息
        this.buffer = Unpooled.buffer(Constants.HEADER_LENGTH + bodyLength);
        this.buffer.writeInt(this.headerLength);
        this.buffer.writeInt(appSdkVersion);
        this.buffer.writeInt(messageType);
        this.buffer.writeInt(requestCmd);
        this.buffer.writeInt(sequence);
        this.buffer.writeInt(bodyLength);
        this.buffer.writeBytes(body);
    }

    public Message(ByteBuf byteBuf) {
        this.headerLength = byteBuf.readInt();
        this.appSdkVersion = byteBuf.readInt();
        this.messageType = byteBuf.readInt();
        this.requestCmd = byteBuf.readInt();
        this.sequence = byteBuf.readInt();
        this.bodyLength = byteBuf.readInt();
        this.body = new byte[bodyLength];
        buffer.readBytes(body);
        this.buffer = byteBuf;
    }

    public Request toRequest() {
        return new Request(appSdkVersion, requestCmd, sequence, body);
    }

    public Response toResponse() {
        return new Response(appSdkVersion, requestCmd, sequence, body);
    }

    @Override
    public String toString() {
        return "Message{" +
                "headerLength=" + headerLength +
                ", appSdkVersion=" + appSdkVersion +
                ", messageType=" + messageType +
                ", requestType=" + requestCmd +
                ", sequence=" + sequence +
                ", bodyLength=" + bodyLength +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
