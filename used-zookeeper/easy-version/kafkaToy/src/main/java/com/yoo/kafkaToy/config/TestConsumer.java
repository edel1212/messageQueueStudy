package com.yoo.kafkaToy.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TestConsumer {
    @KafkaListener(topics = "one")
    public void listener(Object data) {
        System.out.println("토픽 소모 중!!!");
        System.out.println(data);
    }
}
