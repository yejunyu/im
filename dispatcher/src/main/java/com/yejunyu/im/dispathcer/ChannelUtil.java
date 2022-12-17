package com.yejunyu.im.dispathcer;

import io.netty.channel.socket.SocketChannel;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @Author yjy
 * @Description //请求处理组件
 * @Date 2022/11/27
 **/
@UtilityClass
public class ChannelUtil {

    public String getChannelId(SocketChannel channel) {
        return channel.remoteAddress().getHostName() + ":" + channel.remoteAddress().getPort();
    }
}
