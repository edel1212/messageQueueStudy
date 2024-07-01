package com.yoo.consumerServer.service;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
@Log4j2
public class KafkaOnDemandSingleMessageService {
    public void onDemandMessage(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "abc");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // Disable automatic offset commit
        props.put("enable.auto.commit", false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("foo"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); // 메시지를 가져올 타임아웃 지정

            for (ConsumerRecord<String, String> record : records) {
                log.info("---------------------");
                log.info(record);
                log.info("---------------------");
            }
        }// try
    }
}
