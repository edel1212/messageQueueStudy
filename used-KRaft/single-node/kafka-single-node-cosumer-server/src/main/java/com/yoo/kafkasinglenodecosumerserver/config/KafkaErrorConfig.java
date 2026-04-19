package com.yoo.kafkasinglenodecosumerserver.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaErrorConfig {

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

    // ✅ 1. 모든 DTO를 수용할 수 있는 단 하나의 공통 KafkaTemplate
    @Bean
    public KafkaTemplate<String, Object> commonKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(baseConfig()));
    }

    // ✅ 2. 단 하나의 공통 ErrorHandler
    @Bean
    public DefaultErrorHandler commonErrorHandler(KafkaTemplate<String, Object> commonKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                commonKafkaTemplate,
                (record, exception) -> {
                    // 💡 핵심: 하드코딩된 상수 대신, 실패한 원본 토픽명 뒤에 ".DLQ"를 붙여 동적 라우팅
                    String dlqTopic = record.topic() + ".DLQ";

                    log.error("[{}] DLQ 전송 - offset: {}, cause: {}",
                            dlqTopic, record.offset(), exception.getMessage());

                    return new TopicPartition(dlqTopic, -1);
                }
        );

        FixedBackOff backOff = new FixedBackOff(1_000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                JsonParseException.class,
                IllegalArgumentException.class
        );

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("재시도 중 - 시도 횟수: {}/{}, topic: {}, offset: {}, cause: {}",
                        deliveryAttempt, 3, record.topic(), record.offset(), ex.getMessage())
        );

        return errorHandler;
    }

}
