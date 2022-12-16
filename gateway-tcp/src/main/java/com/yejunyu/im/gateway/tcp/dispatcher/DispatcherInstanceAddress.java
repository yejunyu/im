package com.yejunyu.im.gateway.tcp.dispatcher;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author yjy
 * @Description // 消息分发系统实例
 * @Date 2022/11/23
 **/
@Data
@AllArgsConstructor
public class DispatcherInstanceAddress {
    private String host;
    private String ip;
    private int port;
}
