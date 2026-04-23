package com.yoo.cluster_producer.api.payment;

import com.yoo.cluster_producer.api.payment.dto.PaymentRequestDto;
import com.yoo.cluster_producer.api.payment.service.PaymentProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentProducerService paymentProducerService;

    @PostMapping
    public ResponseEntity<String> sendPayment(@RequestBody PaymentRequestDto dto) {

        // eventId 없으면 자동 생성 (멱등성 보장)
        PaymentRequestDto message = PaymentRequestDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(dto.getOrderId())
                .userId(dto.getUserId())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .requestedAt(LocalDateTime.now())
                .build();

        paymentProducerService.sendMessage(message);
        return ResponseEntity.ok("결제 메시지 전송 완료. eventId=" + message.getEventId());
    }

}
