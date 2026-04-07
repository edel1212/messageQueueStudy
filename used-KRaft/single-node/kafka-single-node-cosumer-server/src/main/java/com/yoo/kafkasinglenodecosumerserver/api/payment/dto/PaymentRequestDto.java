package com.yoo.kafkasinglenodecosumerserver.api.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PaymentRequestDto {
    private String eventId;       // 멱등성용 고유 ID
    private String orderId;
    private Long userId;
    private BigDecimal amount;
    // Enum을 사용하지 않은 이유는 MSA 구조상 전받 받는쪽의 버전이 맞지 않을 경우를 대비하기 위함
    private String paymentMethod; 
    private LocalDateTime requestedAt;
}
