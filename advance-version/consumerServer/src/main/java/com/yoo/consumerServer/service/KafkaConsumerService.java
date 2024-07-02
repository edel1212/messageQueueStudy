package com.yoo.consumerServer.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@Log4j2
public class KafkaConsumerService {
    @KafkaListener(topics = "gom")
    public void listener(Object data) {
       log.info("--------------------");
       log.info("Group Name :: group_1");
       log.info("Topic Name :: gom");
        log.info("data :: {}", data);
       log.info("--------------------");
    }
}
