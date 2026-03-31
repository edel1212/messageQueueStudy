package com.yoo.kafkaToy.kafkaTest;

import com.yoo.kafkaToy.config.TestConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConsumerTests {
    @Autowired
    private TestConsumer consumer;

    @Test
    void getTopic() {
        consumer.listener(null);
    }
}
