package com.yejunyu.im.dispathcer;

import com.alibaba.fastjson2.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/1/6
 **/
public class KafkaManager {
    static class Singleton {

        static KafkaManager instance = new KafkaManager();

    }

    public static KafkaManager getInstance() {
        return Singleton.instance;
    }

    private KafkaProducer<String, String> producer;

    public KafkaManager() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "127.0.0.1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(properties);
    }

    /**
     * 获取kafka
     * @return
     */
    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    /**
     * 初始化
     */
    public void init(){
        new Thread(() -> {
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
            properties.setProperty("group.id","dispatcher_group");
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String ,String > consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Arrays.asList("send_message_response"));
            while (true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1L));
                for (ConsumerRecord<String, String> record : records) {
                    
                }

            }
        }).start();
    }
}
