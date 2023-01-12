package com.yejunyu.im.common;

import lombok.Data;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/1/9
 **/
@Data
public class ImSend {

    private long messageId;

    private String senderId;

    private String receiverId;

    private String content;

    private int cmd = CMD.SEND_MESSAGE.getType();

    private int sequence;

    private String channelId;

    private long timestamp;
}
