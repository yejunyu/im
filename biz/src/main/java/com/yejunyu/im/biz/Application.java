package com.yejunyu.im.biz;

import com.alibaba.fastjson2.JSON;
import com.yejunyu.im.common.CMD;
import com.yejunyu.im.common.Constants;
import com.yejunyu.im.common.ImSend;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/1/12
 **/
public class Application {

    private static JdbcTemplate jdbcTemplate;

    private static KafkaProducer<String, String> kafkaProducer;

    static {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://47.94.172.220:3307/test?characterEncoding=utf8&useSSL=true",
                "root",
                "123456");
        Application.jdbcTemplate = new JdbcTemplate(dataSource);
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "127.0.0.1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Application.kafkaProducer = new KafkaProducer<>(properties);
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("group.id", "business_group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);) {
            consumer.subscribe(Arrays.asList(Constants.SEND_MSG_TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1L));
                for (ConsumerRecord<String, String> record : records) {
                    ImSend imSend = JSON.parseObject(record.value(), ImSend.class);
                    int cmd = imSend.getCmd();
                    if (cmd == CMD.SEND_MESSAGE.getType()) {
                        // 将数据写入mysql
                        long messageId = storeMessage(imSend);
                        sendMessageResponse(imSend, messageId);
                    }
                }
            }
        }


    }

    /**
     * 发送回执消息
     *
     * @param imSend
     * @param messageId
     */
    private static void sendMessageResponse(ImSend imSend, long messageId) {
        imSend.setMessageId(messageId);
        imSend.setTimestamp(System.currentTimeMillis());
        ProducerRecord<String, String> record = new ProducerRecord<>(Constants.SEND_MSG_RESPONSE_TOPIC, JSON.toJSONString(imSend));
        kafkaProducer.send(record);
    }

    /**
     * 存储消息
     *
     * @param imSend
     * @return 消息的messageId
     */
    private static long storeMessage(ImSend imSend) {
        String senderId = imSend.getSenderId();
        String receiverId = imSend.getReceiverId();
        String content = imSend.getContent();
        int cmd = imSend.getCmd();
        int sequence = imSend.getSequence();
        Date sendTime = new Date(System.currentTimeMillis());

        String sql = "insert into message_send(" +
                "sender_id," +
                "receiver_id," +
                "cmd," +
                "sequence," +
                "content," +
                "send_time," +
                "message_type) " +
                "VALUES(?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, senderId);
            statement.setString(2, receiverId);
            statement.setInt(3, cmd);
            statement.setInt(4, sequence);
            statement.setString(5, content);
            statement.setDate(6, sendTime);
            statement.setInt(7, 1);
            return statement;
        }, keyHolder);
        long messageId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        String sql1 = "insert into message_receive(" +
                "message_id," +
                "sender_id," +
                "receiver_id," +
                "cmd," +
                "sequence," +
                "content," +
                "send_time," +
                "message_type," +
                "is_delivered) " +
                "VALUES(?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql1, Statement.NO_GENERATED_KEYS);
            statement.setLong(1, messageId);
            statement.setString(2, senderId);
            statement.setString(3, receiverId);
            statement.setInt(4, cmd);
            statement.setInt(5, sequence);
            statement.setString(6, content);
            statement.setDate(7, sendTime);
            statement.setInt(8, 1);
            statement.setInt(9, 0);
            return statement;
        });
        return messageId;
    }
}
