package com.yoo.kafkasinglenodecosumerserver.api.inventory.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InventoryDto {
    private String productId;
    private String productName;
    private BigDecimal amount;
}
