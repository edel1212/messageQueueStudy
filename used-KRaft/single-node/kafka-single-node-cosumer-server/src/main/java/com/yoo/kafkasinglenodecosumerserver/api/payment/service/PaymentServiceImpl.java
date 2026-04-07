package com.yoo.kafkasinglenodecosumerserver.api.payment.service;

import com.yoo.kafkasinglenodecosumerserver.api.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    @Transactional
    @Override
    public void payment(PaymentRequestDto dto) {
        log.info("Service단 비즈니스 로직 수행: {}", dto);
    }
}
