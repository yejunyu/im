package com.yejunyu.im.gateway.tcp.dispatcher;

import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.Request;
import com.yejunyu.im.protocal.Authentication;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/29
 **/
@Data
@AllArgsConstructor
public class DispatcherInstance {

    private SocketChannel socketChannel;

    /**
     * 向分发系统发送认证请求
     *
     * @param authenticateRequest 认证请求
     */
    public void authenticate(Authentication.Request authenticateRequest) {
        Request request = new Request(Constants.APP_SDK_VERSION_1, CMD.AUTHENTICATE.getType(), Constants.SEQUENCE_DEFAULT,
                authenticateRequest.toByteArray());
        socketChannel.writeAndFlush(request.getBuffer());
    }
}
