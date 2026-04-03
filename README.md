# 메세지 큐(Message Queue)
- 서로 다른 애플리케이션이나 서비스 간에 메시지를 교환하기 위한 통신 메커니즘
  - 더 큰 개념 : `메세지 지향 미들웨어(MOM)`를 구현한 시스템
  - 분산 시스템에서 효율적이고 신뢰성 있는 메시지 전달을 가능하게 하는 핵심 기술
  - 시스템은 확장성, 안정성 및 유연성을 유지하면서 복잡한 작업을 처리가 가능함
  - 여러 컴포넌트가 **비동기적**으로 통신할 수 있게 함
- 흐름 : `[Producer(s)] --> [Queue (Broker)] --> [Consumer(s)]`

## 주요 개념
- 메시지(Message)
  - 송신자 -> 수신자 보내는 데이터 단위 헤더와 본문으로 구성, 해더에는 메세지의 속성, 메타 데이터, 라우팅 정보를 포함 할 수 있고 본문에는 실제 데이터가 들어 있음
- 생산자(Producer)
  - 메시지를 생성 하는 주체
  - 큐에 넣는 역할을 히먀, **송신자가 메시지를 처리 가능 여부를 확인할 필요가 없음**
-  소비자(Consumer)
  - **메시지를 읽어와 처리**하는 역할을 함, 메시지를 가져와서 **필요한 작업을 수행**
- 브로커(Broker)
  -  메시지 큐 시스템을 관리하고 메시지의 송수신을 조율하는 **역할을 진행**
  -  메시지의 라우팅, 전달 보장, 일관성 유지 등을 담당

## 메시지 지향 미들웨어(MOM - Message Oriented Middleware)
> MOM 환경에서는 주로 Pub/Sub 모델을 통해 시스템 간 결합도를 낮추고 비동기 처리를 구현하며, 최근에는 카프카와 같은 이벤트 스트리밍 플랫폼으로 그 역할이 확장되고 있음
- 시스템과 애플리케이션 간의 메시지 기반 통신을 지원하는 소프트웨어 (어플리케이션들의 **메시지를 중간에서 관리해주는 시스템**)
- ℹ️ **쉽게 설명**: 브로커(Broker)는 `MOM`의 **하위 개념**으로 볼 수 있다
  -  `브로커`는 **메시지 큐를 관리하고 메시지의 안전한 전달을 보장**
  - `MOM`은 **더 포괄적인 기능을 제공 시스템 간의 메시지 기반 통신 전반을 다룸**

## 데이터 운방 방식
### [예전 방식]`메세지 브로커(Message Broker)`
> 목적: 서로 다른 시스템 간의 빠르고 안정적인 '메시지 전달' 자체에 집중
- 흐름
  - 생산자(Producer)가 생산한 메세지를 메세지 큐에 저장
  - 저장된 메세지는 소비자가(Consumer)가 가져감
  - [특징] 가져간(처리 완료된) 메시지는 짧은 시간 내에 서버(메시지 큐)에서 즉시 삭제됨
- 대표 기술: `RabbitMQ`, `ActiveMQ`, `Amazon SQS`
### [요즌 방식]`이벤트 브로커(Event Broker)`
> 기본적으로 메세지 브로커의 역할 수행 또한 가능
- 흐름
  - 생산자(Producer)가 상태 변경, 로그 등의 '이벤트'를 발생시켜 이벤트 **스트림(토픽)에 순서대로 기록**
  - 여러 소비자(Consumer)가 각자 자신의 속도에 맞춰 **독립적으로 데이터를 읽어감**
    - 어디까지 읽었는지 Offset으로 개별 기록 
  - [특징] 소비자가 메시지를 가져가서 처리하더라도 **데이터는 삭제되지 않고**, 설정된 보존 **기간 동안 디스크에 영구 보관**됨
    - 보관된 기록을 바탕으로 필요에 따라 과거의 이벤트를 다시 읽어오거나(Replay), 장애 발생 시 특정 시점부터 **복구 가능**
- 대표 기술: `Apache Kafka`, `AWS Kinesis`

## 주요 Message Queue
### Apache Kafka 
> `이벤트 브로커(Event Broker)` 방식
- 토픽(`topic`)라고 불리는 카테고리에 데이터 집합을 저장
  - ⭐️하나의 topic은 다수의 partition으로 나뉘어짐
- `Kafka Cluster` 사용 시 병렬처리가 가능 하  방대한 양의 데이터를 처리할 수 있다.
- 👉 [Kafka 처리 구조]
  - CLI 혹은 UI를 통해 `topic` 생성  
  - `Producer`가 `topic`으로 메시지를 전송
    - **메시지에 Key를 지정 시**  : 특정 해시값을 통해 항상 동일한 파티션으로 순서를 보장하며 보낼 수 있음
    - 메세지에 **Key가 없을 경우** : 라운드 로빈(Round-robin) 방식으로 여러 파티션에 균등하게 분산됨
  - `Broker(각각의 kafka server)`에서 메시지는 특정 파티션으로 분배되어 순서대로 끝에 추가
  - `Consumer Group`에 속한 `Consumer`가 `topic`의 `partition`을 구독 후 메시지를 소비를 대기
  - `Consumer Group` 내에서는 각 `Consumer`가 파티션을 분담하여 병렬로(독립적으로) 처리
    - [🔥 핵심 ] : 하나의 파티션은 동일한 컨슈머 그룹 내에서 오직 1개의 컨슈머에게만 할당된다. (1:1 매핑 원칙)
      - 파티션이 3개인데 컨슈머가 4개라면, 1개의 컨슈머는 아무 일도 하지 않고 대기(Idle) 상태가 됨
      - 컨슈머가 2개라면 1개의 컨슈머가 2개의 파티션을 담당하게 됨
- RabbitMQ
  - AMQP(Advanced Message Queuing Protocol)를 기반으로 하는 오픈 소스 메시지 브로커. 다양한 라우팅 기능과 플러그인을 제공.
  - 기본적으로 전통적인 Message Queue 방식을 지원합니다.또한, message exchange( 메시지를 송수신하는 데 사용되는 메커니즘)를 사용하여 pub/sub 방식도 구현합니다.
  - 데이터 처리 보단, 관리적 측면이나 다양한 기능 구현을 위한 서비스를 구축할 때 사용
  - Producer와 Consumer의 결합도가 높습니다.
  - `메세지 브로커(Message Broker)` 방식이다.
  - 👉 [RabbitMQ 처리 구조]
    - `Producer`가 `Broker`로 메세지를 보냅니다.
    - `Broker`내 `Exchange`에서 해당하는 `Key`에 맞게 `Queue`에 분배합니다.(Binding)
    - 해당 `Queue`를 구독하는 `Consumer`가 메세지를 소비합니다.
- Amazon SQS (Simple Queue Service)
  - AWS에서 제공하는 관리형 메시지 큐 서비스로, 고가용성과 확장성을 제공.  


## Kafka 사용 예시
- 간단한 방법 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/easy-version)
  - 경량화된 Kafa, Zookeeper를 사용하여 Producer, Conuser 사용
- Cluster 방법 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/advance-version)
  - 3개의 Borkder, Zookeeper를 이용하여 Cluster 구성과 파티셔닝 및 복제 사용

## Kafka 사용 시 헷갈렸던 개념
- ### `Producer`가 등록한 `Topic`에 메세지를 넣는 개념이다
  - **카프카 클러스터의 브로커에서 데이터를 관리할 때 기준이 되는 개념**
- ### `Topic`에 등록한 메세지는 따로 Conumer가 읽는다고 사라지는게 아니다.
  - Kafak는 전통적인 메시지 큐 시스템과는 **다르게 설계**되었기 때문 전통적인 큐 시스템에서는 메시지를 소비하면 메시지가 큐에서 제거되나 **Kafka는 로그를 기반으로 하는 메시징 시스템 이기 떄문**
    - 👉 단 ! `groupId`를 지정하면 그룹의 컨슈머들은 메시지를 중복 없이 소비하게 됩니다. 이는 Kafka의 컨슈머 그룹 관리 방식 때문이다.
- ### Topic을 바라보고 있는 대상이 여러개일 경우 ?
  - 소켓과 같이 모두에게 전파 될 것으로 예상 -> 하지만 틀렸음 한곳에만 나옴 반대쪽 서버를 끄면 켜있는 곧으로 넘어간다.
    - 👉 단 ! `groupId`을 지정하지 않으면 바라보는 `Topic`의 메세지를 모두가 받는다.  
    ![kafka](https://github.com/edel1212/messageQueueStudy/assets/50935771/6edbb7c7-96ea-4f84-a33b-ccf91cebc2ae)


- ### `partition`을 사용했을 경우 한쪽의 `partition`에만 메세지가 쌓였던 이슈
  - Producer쪽의 Batch Size 설정으로 해결
    -  Batch Size란 ?
      - batch를 이용하면 메세지를 묶음 으로 보내기 때문에 replica처리 로직이 줄어 메세지 send 처리 대기 줄일 수 있다. 
        - 👉 적정 Batch Size 설정이 중요하다
        
        ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/1a87a924-1432-4efa-8d5e-a5333311f32c)     

  - 예시 코드

    ```java
    @Configuration
    public class KafkaProducerConfig {
        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> config = new HashMap<>();
            // 👉 Batch 사이즈 수정으로 한쪽으로 파티션으로 메세지가 몰리는 이슈 수정
            config.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
            return new DefaultKafkaProducerFactory<>(config);
        }
    }
    ```

- ### `partition`의 순서 보장
  - 간단하다 분산 병렬 처리를 위해서이다!
  - Topic 안에 설정한 파티션으로 존재한다
    - 각각의 메세지가 파티션에 들어가 있는 형식이다!
    - 일정 Batch Size 설정을 해주지 않으면 한 파티션에만 들어 있기에 메세지가 들어오지 않는다.
  - Topic의 메세지가 파티션별로 들어가기에 전체 출력 시 순서서가 보장 되지 않을 수있다.
    - `--from-beginning` 옵션을 사용 할 경우에 그러하다 전체 출력 시 그러함
    - 원인 : 카프카 컨슈머에서의 메시지 순서는 동일한 파티션 내에서는 프로듀서가 생성한 순서와 동일하게 처리하지만, 파티션과 파티션 사이에서는 순서를 보장하지 않습니다.
    - 해결 방법 : 순서가 중요한 경우에는 파티션을 1개로 설정해주자
  - 예시
    - Producer가 `a,b,c,e,e,1,2,3,4,5`를 Topic에 입력하고 해당 Topic이 3개의 파티션으로 구성되었을 경우
      
     ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/32f16cd8-5dc3-4c00-9067-5171f5fd6f89)

  -  출력 값
    
      ```properties
      ## 컨슈머 콘솔 명령어
      /usr/local/kafka/bin/kafka-console-consumer.sh \
      --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
      --topic peter-01 \
      --from-beginning
      
      출력 
      a
      d
      1
      4
      b
      e
      2
      5
      c
      3
      ```
    

    
