package com.yoo.kafkasinglenodecosumerserver.api.order.consumer;

import com.yoo.kafkasinglenodecosumerserver.api.order.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    // 포트 정보
    @Value("${server.port}")
    private String serverPort; 
    
    @KafkaListener(
            // 프로듀서의 TOPIC 상수와 동일한 이름
            topics = "order.request",
            // 그룹명
            groupId = "order-processor-group",
            // KafkaConsumerConfig에 설정된 Container Factory 명
            containerFactory = "orderFactory"
    )
    public void consumeOrder(OrderRequestDto dto
                             // 전달 받은 파티션의 ID 정보
                            , @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
                             // 커밋
                            , Acknowledgment ack
    ) {

        log.info("🔥 [Port:{}] 파티션[{}] 처리 중 - orderId: {}", serverPort, partition, dto.getOrderId());

        if("error".equals(dto.getOrderId())){
            throw new RuntimeException("메세지 내 에러가 있습니다.");
        }// if

        // 로직 완료 후 커밋
        ack.acknowledge();

    }
}
