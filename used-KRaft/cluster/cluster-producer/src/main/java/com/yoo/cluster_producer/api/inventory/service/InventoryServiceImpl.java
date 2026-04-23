package com.yoo.cluster_producer.api.inventory.service;

import com.yoo.cluster_producer.api.inventory.dto.InventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService{

    private final KafkaTemplate<String, InventoryDto> inventoryKafkaTemplate;

    @Override
    public void sendMessage(InventoryDto dto) {
        log.info("재고 추가 dto : {}", dto);
        inventoryKafkaTemplate.send(TOPIC,dto)
                .whenComplete((r, ex) ->{
                    if (ex != null) {
                        log.error("재고 메시지 전송 실패. getProducerId={}, getAmount={}",
                                dto.getProductId(), dto.getAmount(), ex);
                    } else {
                        log.info("재고 메시지 전송 성공. getProducerId={}, getAmount={}, offset={}",
                                dto.getProductId(),
                                dto.getAmount(),
                                r.getRecordMetadata().offset());
                    } // if - else
                })
        ;
    }
}
