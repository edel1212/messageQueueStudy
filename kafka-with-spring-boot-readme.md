# SpringBoot - Kafka 사용 정리
> Topic 생성의 경우 CLI를 통해 생성하는것이 좋다.

## 기본 Produce Server <->  Consumer Server
> Produce / Consumer Server 를 나눠서 구축하여 진행  

### application.yml
- bootstrapServers는 ","를 사용해서 다수의 브로커 권한을 갖는 서버 등록이 가능하다.
  - controller 권한을 갖는 브로커는 **등록 불가능**
```yaml
spring:
  application:
    name: kafka-cluster-producer-server
  ## kafka 설정
  kafka:
    # 🔍 구정된 브로커 서버를 나열
    # - 만약 10개의 브로커라도 전체를 브로커 주소를 전부 나열해줄 필요는 없다
    bootstrap-servers: localhost:9092,localhost:9093,localhost:9094 

```

### Produce Server Config
```java
@Configuration
public class KafkaProducerConfig {
    // 환경 변수를 통해 브로커 서버 주소를 가져옴
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
        config.put(ProducerConfig.RETRIES_CONFIG, 3);               // 실패 시 재시도 횟수
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
```

### Producer KafkaTemplate
> 메세지 전송 시 Key 값은 필수 값이 아님, 하지만 👍 Key가 있을 경우 같은 Partition에 위치하므로 순서가 보장됨
```java
@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentProducerServiceImpl implements PaymentProducerService {

    private final KafkaTemplate<String, PaymentRequestDto> paymentKafkaTemplate;

    @Override
    public void sendMessage(PaymentRequestDto dto) {
        // "대상 토픽명", Key 값, value 값
        paymentKafkaTemplate.send(TOPIC, dto.getOrderId(), dto)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("결제 메시지 전송 실패. orderId={}, eventId={}",
                                dto.getOrderId(), dto.getEventId(), ex);
                    } else {
                        log.info("결제 메시지 전송 성공. orderId={}, eventId={}, offset={}",
                                dto.getOrderId(),
                                dto.getEventId(),
                                result.getRecordMetadata().offset());
                    } // if - else
                });
    }
}
```

### Consumer Server Config
> 공통 설정 분리와 "createContainerFactory(공장)" 과 "createContainerFactory(일꾼)" 을 분리하여 불필요한 공통 코드 제거하여 설정
```java
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

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createContainerFactory(Class<T> targetType) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 실제 Kafka Consumer 인스턴스를 생성하는 대상 지정
        factory.setConsumerFactory(createConsumerFactory(targetType));
        // 수동 커밋 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // factory.setConcurrency(3); // 필요시 활성화
        return factory;
    }

    // =========================================================
    // ✅ 2. 실제 Bean 등록 (도메인 추가 시 여기서 딱 한 줄만 추가하면 끝!)
    // =========================================================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestDto> paymentFactory() {
        return createContainerFactory(PaymentRequestDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequestDto> orderFactory() {
        return createContainerFactory(OrderRequestDto.class);
    }
}
```

### Consumer Listener
> 주의 사항 : @KafkaListener 의 역할과 Service 역할이 다르기에 Class 분리 필요  
> Interface Impl 구조가 아님 비즈니스 로직이 필요하면 DI를 통해 구현
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            // 프로듀서의 TOPIC 상수와 동일한 이름
            topics = "payment.request",
            // 그룹명
            groupId = "payment-processor-group-v10",
            // KafkaConsumerConfig에 설정된 Container Factory 명
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consumePayment(PaymentRequestDto dto, Acknowledgment ack) {
        log.info("결제 메시지 Kafka 수신: {}", dto);

        paymentService.payment(dto);

        // 로직 완료 후 커밋
        // ack.acknowledge();

    }
}
```

## 컨슈머 장애 처리 - DLQ (Dead Letter Queue) 설정

### Consumer Server Error Config
- `KafkaErrorConfig`를 만들어주는 이유는 소모만 하는것이 아닌 `*.DLQ` 토픽에 **직접 메세지를 Producer 해야하기 때문**임
- DefaultErrorHandler를 통해 에러가 발생했을 경우 어떠한 방식으로 처리할지 지정할 수 있다.
```java
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
```

### Consumer Config
- Factory 와 Container 내 상위에 작성했던 `DefaultErrorHandler commonErrorHandler`를 인자로 받아 설정
```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // 공통 설정
    private Map<String, Object> commonConfig() {
        Map<String, Object> config = new HashMap<>();
        // code...
        return config;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType) {
        Map<String, Object> config = commonConfig();
        /// code...
        return new DefaultKafkaConsumerFactory<>(config);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createContainerFactory(
            Class<T> targetType, DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(targetType));
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequestDto> orderFactory(
            DefaultErrorHandler commonErrorHandler) {
        // OrderRequestDto 클래스와 에러 핸들러만 넘겨서 공장 가동!
        return createContainerFactory(OrderRequestDto.class, commonErrorHandler);
    }

}
```

