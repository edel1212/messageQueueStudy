package com.yoo.kafkasinglenode.api.payment.service;

import com.yoo.kafkasinglenode.api.payment.dto.PaymentRequestDto;

public interface PaymentProducerService {
    String TOPIC = "payment.request";

    void sendMessage(PaymentRequestDto dto);
}
