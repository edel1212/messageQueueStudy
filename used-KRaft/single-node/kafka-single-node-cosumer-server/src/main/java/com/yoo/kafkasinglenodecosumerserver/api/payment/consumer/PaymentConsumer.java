package com.yoo.kafkasinglenodecosumerserver.api.payment.consumer;

import com.yoo.kafkasinglenodecosumerserver.api.payment.dto.PaymentRequestDto;
import com.yoo.kafkasinglenodecosumerserver.api.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


// @KafkaListener 의 역할과 Service 역할이 다르기에 Class 분리 필요
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            // 프로듀서의 TOPIC 상수와 동일한 이름
            topics = "payment.request",
            // 그룹명
            groupId = "payment-processor-group-v10",
            // KafkaConsumerConfig에 설정된 Container Factory 명
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consumePayment(PaymentRequestDto dto, Acknowledgment ack) {
        log.info("결제 메시지 Kafka 수신: {}", dto);

        paymentService.payment(dto);

        // 로직 완료 후 커밋
        // ack.acknowledge();

    }
}
