package com.yejunyu.im.gateway.tcp;

import redis.clients.jedis.Jedis;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/12/17
 **/
public class JedisManager {
    private JedisManager() {
    }

    private Jedis jedis = new Jedis("127.0.0.1");

    static class Singleton {
        public static JedisManager instance = new JedisManager();
    }

    public static JedisManager getInstance() {
        return JedisManager.Singleton.instance;
    }

    public Jedis getJedis() {
        return this.jedis;
    }
}
