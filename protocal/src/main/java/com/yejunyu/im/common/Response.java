package com.yejunyu.im.common;

import io.netty.buffer.ByteBuf;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/27
 **/
public class Response extends Message {
    public Response(ByteBuf byteBuf) {
        super(byteBuf);
    }

    public Response(int appSdkVersion, int requestType, int sequence, byte[] body) {
        super(appSdkVersion, Constants.MESSAGE_TYPE_RESPONSE, requestType, sequence, body);
    }

    public Response(Request request, byte[] body) {
        super(request.appSdkVersion, Constants.MESSAGE_TYPE_RESPONSE, request.requestCmd, request.sequence, body);
    }
}
