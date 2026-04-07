package com.yoo.kafkasinglenode.config;

import com.yoo.kafkasinglenode.api.order.dto.OrderCreatedDto;
import com.yoo.kafkasinglenode.api.payment.dto.PaymentRequestDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConfig() {

        Map<String, Object> config = new HashMap<>();

        // 필수 설정
        // Kafka Server (브로커) 등록
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 💡 프로듀서는 데이터를 '보낼 때' 바이트로 변환해야 하므로 Serializer를 사용합니다.
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        // 신뢰성 설정
        config.put(ProducerConfig.ACKS_CONFIG, "all");              // 모든 replica 확인 후 ack
        config.put(ProducerConfig.RETRIES_CONFIG, 3);               // 실패 시 재시도 횟수 (브로커 서버 및 토픽 문제가 아닐 경우)
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 중복 전송 방지

        // 성능 설정
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);        // 배치 사이즈 (16KB)
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);             // 배치 대기 시간 (ms)
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // 버퍼 메모리 (32MB)

        // 토픽이 존재하지 않을 경우 재시도 시간
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10_000);

        return config;
    }

    @Bean
    public KafkaTemplate<String, PaymentRequestDto> paymentKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(baseConfig()));
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedDto> orderKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(baseConfig()));
    }

}
