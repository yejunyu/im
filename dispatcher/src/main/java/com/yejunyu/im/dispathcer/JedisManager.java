package com.yejunyu.im.dispathcer;

import redis.clients.jedis.Jedis;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/12/17
 **/
public class JedisManager {
    private Jedis jedis;

    private JedisManager() {
        jedis = new Jedis("127.0.0.1");
        jedis.auth("123456");
    }


    static class Singleton {
        public static JedisManager instance = new JedisManager();
    }

    public static JedisManager getInstance() {
        return Singleton.instance;
    }

    public Jedis getJedis() {
        return this.jedis;
    }
}
