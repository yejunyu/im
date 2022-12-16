package com.yejunyu.im.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yejunyu.im.protocal.Authentication;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/25
 **/
public class ProtocalTest {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        Authentication.Request authenticationRequest = createAuthenticationRequest();
        // 序列化
        byte[] bytes = authenticationRequest.toByteArray();
        System.out.println(bytes.length);
        // 反序列化
        Authentication.Request request = Authentication.Request.parseFrom(bytes);
        System.out.println(request);
    }

    public static Authentication.Request createAuthenticationRequest() {
        Authentication.Request.Builder builder = Authentication.Request.newBuilder();
        builder.setUid("001");
        builder.setToken("001_token");
        builder.setTimestamp(System.currentTimeMillis());
        return builder.build();
    }

    public static Authentication.Response createAuthenticationResponse() {
        Authentication.Response.Builder builder = Authentication.Response.newBuilder();
        builder.setErrorCode(100);
        builder.setStatus(-1);
        builder.setErrorMsg("有问题");
        return builder.build();
    }

}
