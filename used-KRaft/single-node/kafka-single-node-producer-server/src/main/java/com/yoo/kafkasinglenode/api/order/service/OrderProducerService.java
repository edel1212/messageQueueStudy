package com.yoo.kafkasinglenode.api.order.service;

import com.yoo.kafkasinglenode.api.order.dto.OrderCreatedDto;

public interface OrderProducerService {
    String TOPIC = "order.created";

    void sendMessage(OrderCreatedDto dto);
}
