package com.yoo.cluster_producer.api.payment.dto;

import com.yoo.cluster_producer.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private String eventId;       // 멱등성용 고유 ID
    private String orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod; // CARD, KAKAO_PAY, NAVER_PAY
    private LocalDateTime requestedAt;
}
