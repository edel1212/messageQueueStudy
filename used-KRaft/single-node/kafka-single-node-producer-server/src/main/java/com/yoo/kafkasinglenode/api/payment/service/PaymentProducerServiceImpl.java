package com.yoo.kafkasinglenode.api.payment.service;

import com.yoo.kafkasinglenode.api.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentProducerServiceImpl implements PaymentProducerService {

    private final KafkaTemplate<String, PaymentRequestDto> paymentKafkaTemplate;

    @Override
    public void sendMessage(PaymentRequestDto dto) {
        paymentKafkaTemplate.send(TOPIC, dto.getOrderId(), dto)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("결제 메시지 전송 실패. orderId={}, eventId={}",
                                dto.getOrderId(), dto.getEventId(), ex);
                    } else {
                        log.info("결제 메시지 전송 성공. orderId={}, eventId={}, offset={}",
                                dto.getOrderId(),
                                dto.getEventId(),
                                result.getRecordMetadata().offset());
                    } // if - else
                });
    }
}
