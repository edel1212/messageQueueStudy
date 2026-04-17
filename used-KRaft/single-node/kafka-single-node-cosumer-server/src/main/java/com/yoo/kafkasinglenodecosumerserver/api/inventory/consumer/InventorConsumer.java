package com.yoo.kafkasinglenodecosumerserver.api.inventory.consumer;

import com.yoo.kafkasinglenodecosumerserver.api.inventory.dto.InventoryDto;
import com.yoo.kafkasinglenodecosumerserver.api.order.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorConsumer {

    @Value("${server.port}")
    private String serverPort;

    @KafkaListener(
            // 프로듀서의 TOPIC 상수와 동일한 이름
            topics = "inventory.request",
            // 그룹명
            groupId = "inventory-processor-group",
            // KafkaConsumerConfig에 설정된 Container Factory 명
            containerFactory = "inventoryFactory"
    )
    public void consumeOrder(List<InventoryDto> dtos
                             // Offset 정보
                            , @Header(KafkaHeaders.OFFSET) long offset
                             // 전달 받은 파티션의 ID 정보
                            , @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
                             // 커밋
                            , Acknowledgment ack
    ) {

        // 현재 시스템의 시간(밀리초)을 찍어봅니다.
        long currentTime = System.currentTimeMillis();

        log.info("📌 [Record] partition : {} , Offset: {}, Time: {}ms, dto items: {}", partition, offset, currentTime, dtos);

        // 👍 TODO 여기서 List를 벌크 Insert 혹은 Update를 하면 한건씩 CUD 하는것 보다 훨씬 효율적으로 처리가 가능함

        // 💡 배치 모드에서는  한 번의 ack 호출로 수백 개의 메시지 오프셋이 한 번에 커밋가능
        ack.acknowledge();

    }

}
