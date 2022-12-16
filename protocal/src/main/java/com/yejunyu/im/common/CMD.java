package com.yejunyu.im.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/26
 **/
@AllArgsConstructor
@Getter
public enum CMD {
    /**
     * 用户认证请求
     */
    AUTHENTICATE(1, "用户认证"),
    ;

    private final int type;
    private final String desc;
}
