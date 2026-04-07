# SpringBoot - Kafka 사용 정리
> Topic 생성의 경우 CLI를 통해 생성하는것이 좋다.

## Produce Server

### Producer Config
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

### Producer Service
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

## Consumer Server

### Consumer Config
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
        return config;
    }

    @Bean
    public ConsumerFactory<String, PaymentRequestDto> paymentConsumerFactory() {
        Map<String, Object> config = commonConfig();

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
        // 예외 처리
        // factory.setCommonErrorHandler(paymentErrorHandler());

        // 커밋의 제어건을 SpringBoot에 넘김
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

}
```

### Consumer Processor
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