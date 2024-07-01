package com.yoo.producerServer.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        /**
         * ℹ️ 같은 Kafka Cluster에 속해있는 Broker들을 ,(반점)으로 구분하여 여러개 기입이 가능하다
         * */
        final String  BOOTSTRAP_SERVER_LIST = List.of("localhost:9092","localhost:9093","localhost:9094")
                                                    .stream().collect(Collectors.joining(","));
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER_LIST);
        // 직렬화 메커니즘 설정
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        // ℹ️ 배열 형태로도 등록 가능 TopicBuilder.name("a").build(), TopicBuilder.name("b").build();
        return new KafkaAdmin.NewTopics(
                // Topic명 지정
                TopicBuilder.name("gom")
                        // 파티션 수 지정
                        .partitions(3)
                        // 복제본
                        .replicas(2)
                        // 데이터 보존 시간 - 무기한 저장을 원할 경우 -1로 지정
                        .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(1000 * 60 * 60))
                        .build()
        );
    }

}
