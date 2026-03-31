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

            // 메세지가 들어올 때까지 Loop
            while (true) {
                /**
                 * ℹ️ Duration 지정이유
                 *   - CPU 및 네트워크 자원의 효율적인 사용, 컨슈머의 응답성 조절, 유휴 시간 방지
                 *    리밸런싱 처리 등의 장점을 얻을 수 있습니다
                 * */
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("---------------------");
                    log.info("Received message: Key - {}, Value - {}, Partition - {}, Offset - {}",
                            record.key(), record.value(), record.partition(), record.offset());
                    log.info("---------------------");

                    /**
                     * ℹ️ 메시지 하나를 처리한 후 커밋 - 동기식으로 처리로 정확한 처리를 보장
                     *      - 비동기 식으로 처리를 원할 경우 consumer.commitASync(); 사용
                     *      - 파라미터 미사용 시 전체가 커밋 된다.
                     * **/
                    consumer.commitSync(Collections.singletonMap(
                              new TopicPartition(record.topic(), record.partition())
                            , new OffsetAndMetadata(record.offset() + 1)));

                    consumer.commitAsync();
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
