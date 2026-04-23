package com.yoo.cluster_producer.api.payment.service;


import com.yoo.cluster_producer.api.payment.dto.PaymentRequestDto;

public interface PaymentProducerService {
    String TOPIC = "payment.request";

    void sendMessage(PaymentRequestDto dto);
}
