package com.yejunyu.im.common;

import io.netty.buffer.ByteBuf;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/27
 **/
public class Request extends Message {
    public Request(ByteBuf byteBuf) {
        super(byteBuf);
    }

    public Request(int appSdkVersion, int requestCmd, int sequence, byte[] body) {
        super(appSdkVersion, Constants.MESSAGE_TYPE_REQUEST, requestCmd, sequence, body);
    }
}
