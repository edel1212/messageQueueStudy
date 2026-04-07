package com.yoo.kafkasinglenodecosumerserver.config;

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
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PaymentRequestDto> paymentConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

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


        // ✅ Producer 타입 헤더 무시하고 Consumer DTO로 강제 매핑
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentRequestDto.class.getName());

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestDto> paymentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentRequestDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory());

        // 실무 팁: 파티션 수에 맞춰 쓰레드를 늘리면 동시 처리량이 늘어납니다.
        // factory.setConcurrency(3);
        
        // 커밋의 제어건을 SpringBoot에 넘김
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}
