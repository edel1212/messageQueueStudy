# 메세지 큐(Message Queue)
- 컴퓨팅 시스템에서 서로 다른 애플리케이션이나 서비스 간에 메시지를 교환하기 위한 통신 메커니즘
- 더 큰 개념으로 `메세지 지향 미들웨어(MOM)`를 구현한 시스템을 의미함
- 여러 컴포넌트가 비동기적으로 통신할 수 있게 해 줌
- 시스템의 확장성과 안정성을 크게 향상
- 흐름 : `[Producer(s)] --> [Queue (Broker)] --> [Consumer(s)]`
- 분산 시스템에서 효율적이고 신뢰성 있는 메시지 전달을 가능하게 하는 핵심 기술이며 이를 통해 시스템은 확장성, 안정성 및 유연성을 유지하면서 복잡한 작업을 처리가 가능함

## Message Queue의 주요 개념
- `큐(Queue)`
  - 메시지를 일시적으로 저장하는 버퍼입니다. 큐는 메시지를 순서대로 저장하고 전달하며, 일반적으로 `FIFO(First In, First Out) 방식`으로 동작합니다.
- `메시지(Message)`
  -  송신자 -> 수신자 보내는 데이터 단위 헤더와 본문으로 구성되며, 해더에는 메세지의 속성, 메타 데이터, 라우팅 정보를 포함 할 수 있고 본문에는 실제 데이터가 들어 있음
-  `프로듀서(Producer)`
  -  메시지를 **생성 하는 주체이며** 큐에 넣는 역할을 하고 프로듀서는 메시지를 큐에 넣기만 하면 되므로 **수신자가 메시지를 처리할 수 있는지 여부를 확인할 필요가 없음**
-  `컨슈머(Consumer)`
  - 큐에서 **메시지를 읽어와 처리**하는 역할을 함 컨슈머는 큐에 접근하여 메시지를 가져와서 **필요한 작업을 수행 함**
- `브로커(Broker)`
  -  메시지 큐 시스템을 관리하고 메시지의 송수신을 조율하는 **서버 역할을 한다**
  -  메시지의 라우팅, 전달 보장, 일관성 유지 등을 담당합니다.-  

## 메시지 지향 미들웨어(MOM - Message Oriented Middleware)
- 시스템과 애플리케이션 간의 메시지 기반 통신을 지원하는 소프트웨어 인프라스트럭처입니다. (어플리케이션들의 메시지를 중간에서 관리해주는 시스템)
  -  기반을 형성하는 기초적인 시설과 시스템
-  이론, 개념, 설계적인 방향성을 제시하고 있다고 보면 됩니다. 직접적인 구현체는 아닙니다.
- ℹ️ 쉽게 설명하면 - 브로커는 MOM의 하위 개념으로 볼 수 있다
  -  `브로커`는 **메시지 큐를 관리하고 메시지의 안전한 전달을 보장**하는 반면, `MOM`은 **더 포괄적인 기능을 제공 시스템 간의 메시지 기반 통신 전반을 다룸**

## 장점
- 비동기 처리
  - 시스템의 컴포넌트들이 메시지를 즉시 처리하지 않아도 되어, 다른 작업을 동시에 수행할 수 있음 따라서 병렬 처리와 비동기 통신을 가능하다.
    - 우편함에 우편을 넣고 우편을 꺼내가는 형식으로 생각하면 쉽다.
- 내결함성(Fault Tolerance):
  - 시스템의 일부가 실패하더라도 나머지 시스템이 계속 동작할 수 있도록 지원합니다. 컨슈머가 다운되더라도 메시지는 큐에 저장되어 나중에 처리될 수 있음
    - 컨슈머(우편물을 가져갈 사람)이 안가져가면 우편물은 그대로 있음
- 확장성(Scalability):
  - 다수의 프로듀서(프로세스)**들**이 큐에 메세지를 보낼 수 있다
- 디커플링(Decoupling):
  - 시스템의 컴포넌트 간 결합도를 낮춰 독립적으로 개발, 테스트, 배포할 수 있게 해줍니다. 이는 복잡한 시스템을 더 쉽게 관리하고 유지보수할 수 있도록 합니다.

## 데이터 운방 방식
- `메세지 브로커(Message Broker)` 방식
  - `Message Broker`는 `Event Broker`의 기능을 하지 못합니다  
  - Producer가 생산한 메세지를 메세지 큐에 저장, 저장된 메세지를 Consumer가 가져가는 형식이로 메세지 브로커는 Consumer가 메세지 큐에서 데이터를 가져가게 되면 짧은 시간 내에 메세지 큐에서 삭제된다.
- `이벤트 브로커(Event Broker)`
  - 기본적으로 메세지 브로커의 역할 수행이 가능
  - 이벤트 브로커가 관리하는 데이터를 이벤트라고 하며 Consumer가 소비한 데이터를 필요한 경우 다시 소비할 수 있습니다

## 주요 Message Queue 시스템
- Apache Kafka ( ℹ️ 해당 시스템을 사용허여 토이 프로젝트를 진행 )
  - 오픈 소스 스트리밍 플랫폼으로, 대량의 데이터를 신속하게 처리하고, 저장하며, 전송할 수 있습니다.
  - 큐를 구현하지 않음 대신에 토픽(`topic`)라고 불리는 카테고리에 데이터 집합을 저장
    - 하나의 topic은 다수개의 partition으로 나뉘어진다.
  - `Kafka Cluster`를 통해 병렬처리가 주요 차별점인 만큼 방대한 양의 데이터를 처리할 때, 장점이 부각된다.
  - `이벤트 브로커(Event Broker)` 방식이다.
  - 👉 [Kafka 처리 구조]
    - `Producer`가 토픽(`topic`)으로 메시지를 전송합니다.
    - `Broker`에서 메시지는 특정 파티션(`partition`)으로 분배됩니다.
    - 각 `Consumer Group`에 속한 `Consumer`가 토픽(`topic`)의 파티션(`partition`)을 구독하고 메시지를 소비합니다.
    - `Consumer Group` 내에서는 각 `Consumer`가 독립적으로 처리할 수 있습니다.
    - **데이터를 저장**할 때 기본적으로 **디스크에 저장**하므로, 데이터를 장기간 보관하는 데 적합합니다.
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

    

    
