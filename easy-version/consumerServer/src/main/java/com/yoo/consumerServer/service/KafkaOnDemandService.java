package com.yoo.consumerServer.service;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
@Log4j2
public class KafkaOnDemandService {
    public void onDemandMessage(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG           , "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG                    , "abc");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG      , StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG    , StringDeserializer.class);
        // Auto commit 비활성화
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG          , false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("foo"));

            while (true) { // 계속해서 메시지를 가져오는 무한 루프
                /**
                 * Kafka 소비자가 서버에서 메시지를 가져올 때 최대 대기 시간을 지정하는 방법입니다.
                 *  이를 통해 CPU 및 네트워크 자원의 효율적인 사용, 컨슈머의 응답성 조절, 유휴 시간 방지
                 *  , 리밸런싱 처리 등의 장점을 얻을 수 있습니다.
                 *  이러한 이유들로 poll 메서드의 타임아웃 값은 Kafka 컨슈머 애플리케이션에서 중요한 설정 요소입니다.
                 * */
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("---------------------");
                    log.info("Received message: Key - {}, Value - {}, Partition - {}, Offset - {}",
                            record.key(), record.value(), record.partition(), record.offset());
                    log.info("---------------------");

                    // 메시지 하나를 처리한 후 커밋
                    consumer.commitSync(Collections.singletonMap(
                              new TopicPartition(record.topic(), record.partition())
                            , new OffsetAndMetadata(record.offset() + 1)));

                    // 메시지 하나만 가져오기 위해 루프 종료
                    return;
                } // for
            } // while

        } catch (Exception e){
            e.printStackTrace();
            // TODO Exception 처리
        } // try - catch
    }
}
