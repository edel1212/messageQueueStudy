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
    public ResponseEntity sendData(String topic, String data){
        kafkaTemplate.send(topic, data);
        return ResponseEntity.ok().body("Success");
    }
}
