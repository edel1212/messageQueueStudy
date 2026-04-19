package com.yoo.kafkasinglenode.service;

import com.yoo.kafkasinglenode.api.inventory.dto.InventoryDto;
import com.yoo.kafkasinglenode.api.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.stream.IntStream;

@SpringBootTest
public class InventoryServiceImplTest {

    @Autowired
    private InventoryService inventoryService;

    @DisplayName("재고 가데이터 메세지 전송")
    @Test
    void insertDummy() {
        IntStream.rangeClosed(0, 200).forEach(i->{
            InventoryDto dto = InventoryDto.builder()
                    .productId("productId")
                    .amount(BigDecimal.valueOf(i))
                    .productName("제품명 " + i)
                    .build();
            inventoryService.sendMessage(dto);
        });
    }
}
