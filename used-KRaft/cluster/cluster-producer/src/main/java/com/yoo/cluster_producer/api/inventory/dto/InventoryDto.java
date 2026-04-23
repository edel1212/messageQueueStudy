package com.yoo.cluster_producer.api.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto {
    private String productId;
    private String productName;
    private BigDecimal amount;
}
