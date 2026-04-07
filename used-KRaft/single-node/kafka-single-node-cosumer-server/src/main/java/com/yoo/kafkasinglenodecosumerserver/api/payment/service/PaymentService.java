package com.yoo.kafkasinglenodecosumerserver.api.payment.service;

import com.yoo.kafkasinglenodecosumerserver.api.payment.dto.PaymentRequestDto;

public interface PaymentService {
    void payment(PaymentRequestDto dto);
}
