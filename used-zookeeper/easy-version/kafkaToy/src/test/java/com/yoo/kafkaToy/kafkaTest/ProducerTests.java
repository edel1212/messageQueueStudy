package com.yoo.kafkaToy.kafkaTest;

import com.yoo.kafkaToy.config.TestProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProducerTests {
    @Autowired
    private TestProducer testProducer;

    @Test
    void generatedProducer() {
        testProducer.create();
    }
}
