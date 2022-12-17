package com.yejunyu.im.gateway.tcp;

import com.yejunyu.im.common.Constants;
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
     * 处理认证请求
     *
     * @param request
     * @return
     */
    public Authentication.Response authenticate(Authentication.Request request) {
        Authentication.Response.Builder builder = Authentication.Response.newBuilder();
        String uid = request.getUid();
        String token = request.getToken();
        builder.setUid(uid)
                .setToken(token)
                .setTimestamp(System.currentTimeMillis());
        try {
            // 先请求单点登录系统,判断用户是否是登录的用户
            if (authenticateBySSO(uid, token)) {
                builder.setStatus(Constants.RESPONSE_STATUS_OK);
            } else {
                builder.setStatus(Constants.RESPONSE_STATUS_ERROR)
                        .setErrorCode(Constants.RESPONSE_STATUS_ERROR)
                        .setErrorMsg("认证失败");
            }
        } catch (Exception e) {
            builder.setStatus(Constants.RESPONSE_STATUS_ERROR)
                    .setErrorCode(Constants.RESPONSE_STATUS_UNKNOWN)
                    .setErrorMsg(e.toString());
        }
        System.out.println("已经向SSO单点登录系统认证完毕......");
        return builder.build();
    }

    /**
     * 通过单点登录系统进行用户token的认证
     *
     * @param uid
     * @param token
     * @return
     */
    private boolean authenticateBySSO(String uid, String token) {
        return true;
    }

    public void otherSend(){

    }
}
