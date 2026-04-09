package com.yoo.kafkasinglenodecosumerserver.api.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderRequestDto {
    private String eventId;       // 멱등성용 고유 ID
    private String orderId;
    private Long userId;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
