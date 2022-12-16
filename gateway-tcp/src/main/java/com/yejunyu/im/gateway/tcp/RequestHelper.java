package com.yejunyu.im.gateway.tcp;

import com.yejunyu.im.protocal.Authentication;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/25
 **/
public class RequestHelper {
    private RequestHelper() {
    }

    static class Singleton {
        public static RequestHelper instance = new RequestHelper();
    }

    public static RequestHelper getInstance() {
        return Singleton.instance;
    }

    /**
     * 认证请求处理逻辑
     *
     * @param request
     */
    public void authenticate(Authentication.Request request) {

    }

}
