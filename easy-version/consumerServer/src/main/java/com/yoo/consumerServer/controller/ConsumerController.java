package com.yoo.consumerServer.controller;

import com.yoo.consumerServer.service.KafkaOnDemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class ConsumerController {

    private final KafkaOnDemandService kafkaOnDemandService;

    @GetMapping
    public ResponseEntity getMessage(){
        kafkaOnDemandService.onDemandMessage();
        return ResponseEntity.ok().body("Success");
    }
}
