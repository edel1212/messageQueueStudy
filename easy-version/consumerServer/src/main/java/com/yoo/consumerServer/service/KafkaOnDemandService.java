package com.yoo.consumerServer.service;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
@Log4j2
public class KafkaOnDemandService {
    public void onDemandMessage(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "abc");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("foo"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); // 메시지를 가져올 타임아웃 지정

            for (ConsumerRecord<String, String> record : records) {
                log.info("---------------------");
                log.info(record);
                log.info("---------------------");
            } // for

        } catch (Exception e){
            e.printStackTrace();
            // TODO Exception 처리
        }// try - catch
    }
}
