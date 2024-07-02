# Kafka Cluster 구성 및 사용 예제

```properties
# ℹ️ Docker Compose를 사용해서 Kafka Cluster를 구성함
#
# ℹ️ Cluster란 ?
#  - 여러 대의 컴퓨터들이 연결되어 하나의 시스템처럼 동작하는 컴퓨터들의 집합을 말한다.
#    ㄴ> 쉽게 설명 하자면 다수의 Kafka Broker와 Zookeeper를 묶어서(상호작용이 가능하게) 구성해 놓은 상태라고 보면된다
```

### 브로커(Broker) 란?

- 데이터를 분산 저장하여 장애가 발생하더라고 안전하게 사용할 수 있도록 도와주는 애플리케이션이다.
  - Kafka 서버라고 봐도 무방하다.
- 브로커 서버를 여러대로 구성하여, 클러스터(Cluster)로 묶어서 운영할 수 있다.
  - 카프카 클러스터로 묶인 브로커들은 프로듀서가 보낸 데이터를 안전하게 분산 저장하고 복제하는 역할을 수행함

### 토픽(Topic)이 란?
- 카프카에서 데이터를 **구분하기 위해 사용하는 단위**이다
- 토픽은 **1개 이상**의 **파티션**을 소유한다.

### 파티션(Partition)이 란?
- 데이터를 효율적으로 처리하는 데 필수적인 역할을 한다
- 각각의 파티션은 독립적인 데이터 스트림을 형성하며, 이를 통해 메시지가 병렬로 처리될 수 있다.
- Topic을 이루고 있는 단위다
  - Topic 안에 여러 개의 파티션을 가질 수 있다.(즉, 파티션이란 토픽을 분할한 것이라 할 수 있다.)
- 처음 생성한 이후로는 추가할 수만 있고, 줄일 수는 없다는 특징이 있다.
- Producer가 보낸 데이터들이 파티션에 들어가 저장되고 이 데이터를 레코드라고 부른다.
  
  ![image](https://github.com/edel1212/messageQueueStudy/assets/50935771/1a1b5934-8f14-485d-8674-5d558eb0e41a)

### 복제(Replication) 란?
- 메시지를 복제해 관리 브로커에 장애가 발생해도 다른 브로커가 해당 브로커의 역할을 대신할 수 있도록 하는 것이다.
  - 더 쉽게 설명 : 토픽의 파티션의 복제본을 몇 개를 생성할 지에 대한 설정이다.
  - 이미지 예시
    - topic01 는 `replication.factor`를 **2로**, topic02 는 **3으로** 설정한 경우
      
      ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/786b02f4-47be-422f-aa93-3010c435e924)

- replication 설정 수가 많아지면 그만큼 데이터의 복제로 인한 성능 하락이 있을 수 있기 때문에 무조건 크게 설정하는 것이 권장되지는 않는다.
  - 설정 수를 높이는건 쉽지만 높여 놓은 설정을 다시 낮추는건 어려우니 상황에 맞게 점차적으로 올려가면서 맞추는게 중요하다.
- 흐름

  <img width="837" alt="211142238-ef4c58a3-d488-41f5-9ebb-4663d9643feb" src="https://github.com/edel1212/messageQueueStudy/assets/50935771/bf0c9df3-24d5-41dc-98ff-27d711733d04">


- #### Leader & Follower  
  - 역할 
    - Leader : Topic 으로 통하는 모든 데이터의 Read/Write 는 오직 Leader 를 통해서 이루어진다.
      - 브로커별로 나눠진 토픽중 하나로 선정된다.    
    - Follower : 리더가 되지 못한 나머지 브로커들
  - 사용 이유
    -  리더는 항상 정상적으로 동작해야 한다. 하지만 어떻게 그렇게 할 수 있을까? 리더도 똑같은 브로커 중 하나일 뿐인데 장애가 발생했을 때 처리를 위함이다.

- #### ISR(In-Sync Replication)
  - `Replication`을 나눠 놓은 각각의 Leader 혹은 Flower들을 모아 놓은 Group 이다.

    ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/397adb2c-b574-4487-9786-cd974f2d42e1)

### Lag란?
- Poducer가 데이터를 넣고 Cousumer가 데이터를 데이터를 읽을 경우 이 차이(Offset 차이)를 말한다.

  <img width="1023" alt="211139263-ccb27719-6b14-42d0-9fea-b41283461794" src="https://github.com/edel1212/messageQueueStudy/assets/50935771/3c542364-d0a9-4515-9a73-c26af0e101ba">

- Kafka에서 Lag 값을 통해 Producer, Consumer의 상태를 유추할 수 있다. 즉, Lag이 높다면 Consumer에 문제가 있다는 뜻일 수 있다

  
### 컨트롤러(Contoller)
- Kafka 클러스터의 다수 브로커 중 한 대가 컨트롤러 역할을 한다. 컨트롤러는 다른 브로커들의 상태를 체크하고 브로커가 클러스터에서 빠지는 경우 해당 브로커에 존재하는 리더 파티션을 재분배한다.

  ![image](https://github.com/edel1212/messageQueueStudy/assets/50935771/cc62f245-86f2-422b-9b8f-f06f51d529d0)

### Consumer 대표 옵션
- group.id
  - 컨슈머가 속한 컨슈머 그룹을 식별하는 식별자이다 Group이 지정되면 같은 그룹끼리 확인한 메세지는 보이지 않는다
- enable.auto.commit
  - 백그라운드로 주기적으로 오프셋(`offset`)을 커밋한다.
  - 기본 값: true
- auto.offset.reset
  - Kafka에서 초기 오프셋이 없거나 현재 오프셋이 더 이상 존재하지 않은 경우(데이터가 삭제)에 다음 옵션으로 리셋한다.
    - earliest: 가장 초기의 오프셋값으로 설정한다.
    - latest: 가장 마지막의 오프셋값으로 설정한다.(기본 값)
    - none: 이전 오프셋값을 찾지 못하면 에러를 나타낸다
- max.poll.records
  - 단일 호출 poll()에 대한 최대 레코드 수를 조정한다.
  - 기본 값은 500 이다.
- max.poll.interval.ms
  - 해당 옵션 시간 만큼 컨슈머 그룹에서 컨슈머가 살아 있지만 poll() 메소드를 호출하지 않을 때 장애라고 판단하여 컨슈머 그룹에서 제외 후 다른 컨슈머에게 전달
  - 기본 값: 5분
- auto.commit.interval.ms
  - 주기적으로 오프셋을 커밋하는 시간
  - 기본 값: 5초

    
### 커멘드 사용 시 주의 사항 
- `BootStrap-Server`정보 또한 `","`를 사용해서 여러개 등록이 가능하다.
  - 네트워크 주소는 컨테이너명으로 지정해줘야한다.
  - Port는 Docker-Compose 내 설정한 내부용 Port로 작성해줘야한다.
    - ℹ️  `Docker Compose`에서 외부, 내부, Docker 간 의 포트를 나눈 이유는 이 때문이다.
- 예시
  - 메세지 등록   
    - `kafka-console-producer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom`
  - 컨슈머 등록
    - 이전 까지 모든 메세지
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --from-beginning`
    -  그룹 지정
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --group zero`
   

### SpringBoot Flow

```properties
# ℹ️ 간단한 구조로 진행하였으며, dependencies는 생략하였다 ( easy-version ) 확인
```

#### Producer Server

- Config
  - `BOOTSTRAP_SERVERS_CONFIG` 설정은 Cluster로 구성할 경우 다수의 Broker는 `","`를 사용해서 지정 가능하다.
  - Topic 생성 시 `new KafkaAdmin.NewTopics`를 사용해 만들 경우 배열 형태로 다수의 Topic 생성이 가능하다.
  ```java
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

    /**
     * Topic을 생성함
     * - 배열 형태로도 가능하다
     *
     * @return the kafka admin . new topics
     */
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
  ```
- Controller
  ```java
  @RequiredArgsConstructor
  @RestController
  @RequestMapping("/api")
  public class ProducerController {
      private final KafkaTemplate<String, Object> kafkaTemplate;
      @GetMapping
      public ResponseEntity sendData(String data){
          // KafkaProducerConfig에서 미리 생성한 Topic 명임
          String topic = "gom";
          kafkaTemplate.send(topic, data);
          return ResponseEntity.ok().body("Success");
      }
  }
  ```
