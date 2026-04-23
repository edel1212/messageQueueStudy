package com.yoo.cluster_producer.api.order.service;

import com.yoo.cluster_producer.api.order.dto.OrderCreatedDto;

public interface OrderProducerService {
    String TOPIC = "order.request";

    void sendMessage(OrderCreatedDto dto);
}
