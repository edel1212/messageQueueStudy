package com.yoo.kafkaToy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TestProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void create() {
        System.out.println("================");
        System.out.println("Producer 생성");
        System.out.println("================");
        kafkaTemplate.send("two", "say hello~2");
    }
}
