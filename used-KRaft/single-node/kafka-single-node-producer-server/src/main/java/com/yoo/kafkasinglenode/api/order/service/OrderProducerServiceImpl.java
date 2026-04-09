package com.yoo.kafkasinglenode.api.order.service;

import com.yoo.kafkasinglenode.api.order.dto.OrderCreatedDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducerServiceImpl implements OrderProducerService{
    private final KafkaTemplate<String, OrderCreatedDto> orderKafkaTemplate;

    @Override
    public void sendMessage(OrderCreatedDto dto) {
        // 🔍 주문은 Key를 전달하지 않고 메세지 전달
        orderKafkaTemplate.send(TOPIC, dto)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("주문 메시지 전송 실패. orderId={}, eventId={}",
                                dto.getOrderId(), dto.getEventId(), ex);
                    } else {
                        log.info("주문 메시지 전송 성공. orderId={}, eventId={}, offset={}",
                                dto.getOrderId(),
                                dto.getEventId(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
