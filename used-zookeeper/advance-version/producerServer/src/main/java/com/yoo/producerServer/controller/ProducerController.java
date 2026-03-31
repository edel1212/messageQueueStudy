package com.yoo.producerServer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ProducerController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @GetMapping
    public ResponseEntity sendData(String data){
        // KafkaProducerConfig에서 미리 생성한 Topic 명임
        String topic = "gom";
        kafkaTemplate.send(topic, data);
        return ResponseEntity.ok().body("Success");
    }
}
