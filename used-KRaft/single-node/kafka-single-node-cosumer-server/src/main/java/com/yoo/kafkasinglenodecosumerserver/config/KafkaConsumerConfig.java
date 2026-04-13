package com.yoo.kafkasinglenodecosumerserver.config;

import com.yoo.kafkasinglenodecosumerserver.api.order.dto.OrderRequestDto;
import com.yoo.kafkasinglenodecosumerserver.api.payment.dto.PaymentRequestDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // 공통 설정
    private Map<String, Object> commonConfig() {
        Map<String, Object> config = new HashMap<>();
        // 브로커 서버 지정
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // "초기 오프셋(Offset) 위치를 가장 처음(가장 오래된 데이터)으로 설정"
        // 과거 유실 데이터 방지
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // ✅ 자동 커밋 OFF
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // 💡 역직렬화 세팅 (String -> 객체 변환)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        // 💡 핵심: 어떤 패키지의 DTO로 역직렬화할지 허용 (보안)
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

        // ✅ Producer 타입 헤더 무시
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return config;
    }

    // =========================================================
    // 💡 1. [핵심] 반복되는 설정을 찍어내는 제네릭 팩토리 메서드
    // =========================================================
    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType) {
        Map<String, Object> config = commonConfig();
        // 파라미터로 넘어온 DTO 클래스 이름으로 역직렬화 타겟 강제 지정
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createContainerFactory(
            Class<T> targetType, DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(targetType));
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // factory.setConcurrency(3); // 필요시 활성화
        return factory;
    }

    // =========================================================
    // ✅ 2. 실제 Bean 등록 (도메인 추가 시 여기서 딱 한 줄만 추가하면 끝!)
    // =========================================================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestDto> paymentFactory(
            DefaultErrorHandler commonErrorHandler) {
        // PaymentRequestDto 클래스와 에러 핸들러만 넘겨서 공장 가동!
        return createContainerFactory(PaymentRequestDto.class, commonErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequestDto> orderFactory(
            DefaultErrorHandler commonErrorHandler) {
        // OrderRequestDto 클래스와 에러 핸들러만 넘겨서 공장 가동!
        return createContainerFactory(OrderRequestDto.class, commonErrorHandler);
    }
}
