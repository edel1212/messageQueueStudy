package com.yoo.kafkasinglenode.api.order;

import com.yoo.kafkasinglenode.api.order.dto.OrderCreatedDto;
import com.yoo.kafkasinglenode.api.order.service.OrderProducerService;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProducerService orderProducerService;

    @PostMapping
    public ResponseEntity<String> sendOrder(@RequestBody OrderCreatedDto dto) {

        OrderCreatedDto message = OrderCreatedDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(dto.getOrderId())
                .userId(dto.getUserId())
                .items(dto.getItems())
                .totalAmount(dto.getTotalAmount())
                .orderedAt(LocalDateTime.now())
                .build();

        orderProducerService.sendMessage(message);
        return ResponseEntity.ok("주문 메시지 전송 완료. eventId=" + message.getEventId());
    }
}
