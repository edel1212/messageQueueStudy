# 메세지 큐(Message Queue)
- 컴퓨팅 시스템에서 서로 다른 애플리케이션이나 서비스 간에 메시지를 교환하기 위한 통신 메커니즘
- 더 큰 개념으로 `메세지 지향 미들웨어(MOM)`를 구현한 시스템을 의미함
- 여러 컴포넌트가 비동기적으로 통신할 수 있게 해 줌
- 시스템의 확장성과 안정성을 크게 향상
- 흐름 : `[Producer(s)] --> [Queue (Broker)] --> [Consumer(s)]`
- 분산 시스템에서 효율적이고 신뢰성 있는 메시지 전달을 가능하게 하는 핵심 기술이며 이를 통해 시스템은 확장성, 안정성 및 유연성을 유지하면서 복잡한 작업을 처리가 가능함

### Message Queue의 주요 개념
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

### 메시지 지향 미들웨어(MOM - Message Oriented Middleware)
- 시스템과 애플리케이션 간의 메시지 기반 통신을 지원하는 소프트웨어 인프라스트럭처입니다. (어플리케이션들의 메시지를 중간에서 관리해주는 시스템)
  -  기반을 형성하는 기초적인 시설과 시스템
-  이론, 개념, 설계적인 방향성을 제시하고 있다고 보면 됩니다. 직접적인 구현체는 아닙니다.
- ℹ️ 쉽게 설명하면 - 브로커는 MOM의 하위 개념으로 볼 수 있다
  -  `브로커`는 **메시지 큐를 관리하고 메시지의 안전한 전달을 보장**하는 반면, `MOM`은 **더 포괄적인 기능을 제공 시스템 간의 메시지 기반 통신 전반을 다룸**

### 장점
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

### 메세지 브로커 / 이벤트 브로커
데이터를 운반하는 방식에 따라 메세지 브로커 와 이벤트 브로커 로 나눌 수 있다.
- `메세지 브로커(Message Broker)` 방식
  - `Message Broker`는 `Event Broker`의 기능을 하지 못합니다  
  - Producer가 생산한 메세지를 메세지 큐에 저장, 저장된 메세지를 Consumer가 가져가는 형식이로 메세지 브로커는 Consumer가 메세지 큐에서 데이터를 가져가게 되면 짧은 시간 내에 메세지 큐에서 삭제된다.
- `이벤트 브로커(Event Broker)`
  - 기본적으로 메세지 브로커의 역할 수행이 가능
  - 이벤트 브로커가 관리하는 데이터를 이벤트라고 하며 Consumer가 소비한 데이터를 필요한 경우 다시 소비할 수 있습니다

### 주요 Message Queue 시스템
- Apache Kafka ( ℹ️ 해당 시스템을 사용허여 토이 프로젝트를 진행 )
  - 메시지큐 방식 기반, 분산 메시징 시스템이다.
  - 큐를 구현하지 않음 대신에 토픽(`topic`)라고 불리는 카테고리에 데이터 집합을 저장
    - 하나의 topic은 다수개의 partition으로 나뉘어진다.
  - `Kafka Cluster`를 통해 병렬처리가 주요 차별점인 만큼 방대한 양의 데이터를 처리할 때, 장점이 부각된다.
  - `이벤트 브로커(Event Broker)` 방식이다.
- RabbitMQ
  - AMQP(Advanced Message Queuing Protocol)를 기반으로 하는 오픈 소스 메시지 브로커. 다양한 라우팅 기능과 플러그인을 제공.
  - 기본적으로 전통적인 Message Queue 방식을 지원합니다.또한, message exchange( 메시지를 송수신하는 데 사용되는 메커니즘)를 사용하여 pub/sub 방식도 구현합니다.
  - `메세지 브로커(Message Broker)` 방식이다.
- Amazon SQS (Simple Queue Service)
  - AWS에서 제공하는 관리형 메시지 큐 서비스로, 고가용성과 확장성을 제공.  

