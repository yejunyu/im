package com.yejunyu.im.gateway.tcp.push;

import java.util.concurrent.TimeUnit;

/**
 * @Author yjy
 * @Description //消息推送组件
 * @Date 2022/11/21
 **/
public class PushManager {

    public void start() {
        new PushThread().start();
    }

    static class PushThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(60L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
