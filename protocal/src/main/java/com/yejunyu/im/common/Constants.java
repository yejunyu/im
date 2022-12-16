package com.yejunyu.im.common;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/26
 **/
public class Constants {

    /**
     * 消息头长度
     */
    public static final int HEADER_LENGTH = 24;
    /**
     * sdk版本号
     */
    public static final int APP_SDK_VERSION_1 = 1;
    /**
     * 消息类型: 请求类型
     */
    public static final int MESSAGE_TYPE_REQUEST = 1;
    /**
     * 消息类型: 响应类型
     */
    public static final int MESSAGE_TYPE_RESPONSE = 2;
    /**
     * 顺序消息序列号
     */
    public static final int SEQUENCE_DEFAULT = 1;
    /**
     * 响应状态码: 正常
     */
    public static final int RESPONSE_STATUS_OK = 0;
    /**
     * 响应状态码: 错误
     */
    public static final int RESPONSE_STATUS_ERROR = 1;
    /**
     * 响应状态码: 未知
     */
    public static final int RESPONSE_STATUS_UNKNOWN = -1;
}
