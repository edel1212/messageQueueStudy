# Kafka 간단한 사용 예제

```properties
# ℹ️ 간단한 방법 사용 예제
#     - Docker Compose를 사용해서 구동함
#       ㄴ> wurstmeister(경량화) 이미지를 사용해서 구동
```

- `Kafka Brokder`는 기본적으로 9022 포트에서 구동된다.
- `Zookeeper` 서버가 구동된 상태일때만 Kafka Broker 구동이 가능 하다.
  - `Zookeeper`가 메타 데이터를 관리해 주기 떄문이다.

### Zookeeper 및 Kafka 실행

```properties
## ℹ️ Docker를사용해서 테스트를 진행함
##
## ℹ️ Docker를 사용해 구동할 경우 명령어에 확장자를 붙이지 않는다 (.sh) 불필요
```

- 접근
  - `docker exec -it {{container_name}} /bin/bash`
- ℹ️ Producer
  - Topic 생성
    - `kafka-topics.sh --create --topic [ 생성할 topic명 ] --bootstrap-server [ Kafka Broker 도메인 ] --partitions [ 분할 파티션 개수 ]`
  - Topic 목록 확인
    - `kafka-topics.sh --bootstrap-server [ Kafka Broker 도메인 ] --list`
  - Topic 접근 (메세지 등록)
    - `kafka-console-producer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [생성할 topic명]`
  - Topic 삭제
    - `kafka-topics.sh --delete --topic [삭제할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
  - Topic 정보 확인
    - `kafka-topics.sh --describe --topic [확인할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
- ℹ️ Consumer
  - Topic 메세지 확인
    - 일반 구독 확인
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름]`
    - 이전 메세지까지 확인 (`--from-beginning`)
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름] --from-beginning`
    - 확인한 메세지 안보이게 (`--from-beginning`)
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름] --group [ 그룹 지정 ]`
  ```properties
  services:
    zookeeper:
      image: wurstmeister/zookeeper
      container_name: zookeeper
      ports:
        - "2181:2181"
    kafka:
      image: wurstmeister/kafka:2.12-2.5.0
      container_name: kafka
      ports:
        - "9092:9092"
      environment:
        KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
        KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
  ```

### SpringBoot Flow ( 구독 후 메세지를 받는 방식 )

```properties
## ℹ️ 간단한 메세지 전송 , 지정 Topic 수신 형식으로 진행
##    - 2개의 서버를 구동하여 테스트 ( Producer Server, Consumer Server )
##    - 최대한 간단한 방법으로 구현하였기에 Service 생량 및 Get 방식 사용
```

#### Producer Server

- Dependencies
  ```groovy
  implementation 'org.springframework.kafka:spring-kafka'
  testImplementation 'org.springframework.kafka:spring-kafka-test'
  ```
- Kafka 설정

  ```java
  @Configuration
  public class KafkaProducerConfig {
      @Bean
      public ProducerFactory<String, Object> producerFactory() {
          Map<String, Object> config = new HashMap<>();
          // Producer가 처음으로 연결할 Kafka 브로커의 위치를 설정합니다.
          config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
          // 직렬화 메커니즘 설정
          config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
          config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
          return new DefaultKafkaProducerFactory<>(config);
      }

      @Bean
      public KafkaTemplate<String, Object> kafkaTemplate() {
          return new KafkaTemplate<>(producerFactory());
      }

  }
  ```

- Topic 생성 및 메세지 전송

  ```java
  @RequiredArgsConstructor
  @RestController
  @RequestMapping("/api")
  public class ProducerController {
      private final KafkaTemplate<String, Object> kafkaTemplate;
      @GetMapping
      public ResponseEntity sendData(String topic, String data){
          // ℹ️ kafkaTemplate 내 내장된 메서드를 통해 Kafa 사용이 가능하다.
          kafkaTemplate.send(topic, data);
          return ResponseEntity.ok().body("Success");
      }
  }
  ```

#### Consumer Server

- Dependencies
  ```groovy
  implementation 'org.springframework.kafka:spring-kafka'
  testImplementation 'org.springframework.kafka:spring-kafka-test'
  ```
- Kafka 설정

  ```java
  @Configuration
  public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 전달 받을 그룹명 설정 - 해당 설정으로 확인한 메세지는 보이지 않는다.
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_1");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * @KafkaListener 어노테이션이 붙은 메서드에 주입되어 사용된다.
     * - 메시지를 동시에 처리할 수 있는 메시지 리스너 컨테이너를 생성합니다.
     * */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
  }
  ```

- 지정 Topic 메세지 응답
  ```java
  @Component
  @Log4j2
  public class KafkaConsumerService {
      @KafkaListener(topics = "foo")
      public void listener(Object data) {
         log.info("--------------------");
         log.info("Group Name :: group_1");
         log.info("Topic Name :: foo");
          log.info("data :: {}", data);
         log.info("--------------------");
      }
  }
  ```

### SpringBoot Flow ( on-demand 방식 )

```properties
# ℹ️ Conrtoller를 제외하고 비즈니스 로직인 Service만 예시로 작성함
```

- 사용자의 요청이 들어오면 메세지를 한 건식 확인 하는 형식
- `Dependencies` 추가 이후 내가 Comsumer 설정 값을 지정 후 불러오는 방식이다.
- 메세지를 가져올 때 (`consumer.poll()`) 시 최대 대기시간을 지정해줘야한다.
  - CPU 및 네트워크 자원의 효율적인 사용, 컨슈머의 응답성 조절, 유휴 시간 방지 및 리밸런싱 처리를 위함
- 예시 코드
```java
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
```
