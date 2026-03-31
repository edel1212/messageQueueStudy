package com.yoo.consumerServer.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@Log4j2
public class KafkaConsumerService {
    // topics명을 지정하여 확인
    @KafkaListener(topics = "gom")
    public void listener(Object data) {
       log.info("--------------------");
       log.info("Group Name :: group_10");
       log.info("Topic Name :: gom");
        log.info("data :: {}", data);
       log.info("--------------------");
    }
}
