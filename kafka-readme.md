# Kafka 정리

## 구조
- `Producer` : 메시지를 생성하여 카프카 토픽으로 전송하는 주체
- `Cluster` : 여러 대의 Broker(Kafka servers)들을 묶어 단일 시스템처럼 동작하게 만든 아키텍처 
  - 고가용성 및 확장성 제공
- `Broker` : 카프카 애플리케이션이 설치된 각각의 서버 단위
- `Topic` : 메시지를 구분하는 논리적인 단위. 목적에 따라 관리자(CLI, UI) 또는 서버 내 코드를 통해 생성함
  - DB로 따지면 Table의 개념
- `Partition` : 하나의 `Topic`을 여러 개로 분할한 물리적 단위. 이를 통해 데이터를 분산 저장하고 병렬 처리를 가능하게 함
  - 설정(`Replication Factor`)에 따라 파티션 단위로 복제본(Replica)을 생성하여 여러 브로커에 안전하게 분산 저장할 수 있음.
- `Offset` : 각 파티션 내에서 메시지가 부여받는 고유한 순번
  - `Consumer`가 데이터를 어디까지 읽었는지 추적하는 용도
- `Replica` : 파티션의 복제본. 특정 Broker에 장애가 발생해도 다른 Broker에 저장된 복제본을 통해 데이터 유실 없이 서비스를 지속할 수 있게 함
  - 각각의 브로커가 갖는 Topic 내 같은 파티션의 내용은 복제가 된다. 
    - `1번 브로커 2번 파티션 == 3번 브로커 2번 파티션` 
    - `1번 브로커 1번 파티션 != 3번 브로커 2번 파티션` 
- `Consumer` : 토픽의 파티션에 저장된 메시지를 읽어와서 처리하는 주체
  - `Topic`에 등록한 메세지는 `Conumer`가 읽는다고 **사라지는게 아님**
  - 전통적인 메시지 큐 시스템과는 **다르게 설계**되었기 때문이다. **Kafka는 로그를 기반으로 하는 메시징 시스템이다.**
- `Consumer Group` : 토픽의 데이터를 병렬로 분담해서 처리하기 위해 묶어 놓은 Consumer들의 집합. 
  - 하나의 파티션은 동일 그룹 내에서 오직 한 개의 Consumer만 접근 가능하여 중복 처리를 방지함
  - ✅ 하나의 메세지를 다양하게 처리하기 위해서는 그만큼의 `Consumer Group`을 만들어줘야 함

## 상용 환경고
> 최소 3대 이상의 서버(브로커)에서 분산 운영해야 함
- 하나의 클러스터에서 100개 이상의 브로커 운영이 가능함.
  - 넷플릭스에서는 36개 이상의 카프카 클러스터를 운영하고 구성하는 브로커는 4,000개가 넘는다.
- 파티션
  - Kafka 공식 기본값은 파티션 1개이나, 실무에서는 브로커 3대 구성 기준으로 **파티션 3개를 시작점으로 많이 사용**
  - 처리 효율을 위해 파티션 수와 컨슈머 수를 `1:1`로 맞추는 것이 이상적이지만, 컨슈머 수가 파티션 수를 초과하면 **유휴 컨슈머가 발생하므로 주의가 필요**

## 🚨 Producer 파티션 쏠림(Skew) 이슈와 성능 튜닝 방법
![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/1a87a924-1432-4efa-8d5e-a5333311f32c)
- **현상**: 메시지가 여러 파티션에 골고루 분산되지 않고, 특정 **파티션에만 쏠려서 쌓이는 현상** 발생
- **원인** (Sticky Partitioner):
  - 카프카(v2.4 이후) 기본 파티셔너 정책 때문이다.
  - 네트워크 통신(I/O) 효율을 높이기 위해, 하나의 파티션에 보낼 택배 상자(Batch)가 다 찰 때까지 한 파티션에만 메시지를 계속 몰아주는(Sticky) 방식을 사용하기 때문
- **해결 및 튜닝**: `batch.size`와 `linger.ms`를 서비스 트래픽 목적에 맞게 세트로 튜닝 진행
  - `batch.size` : 한 번에 묶어서 보낼 메시지 묶음의 최대 용량.
    - * **처리량(Throughput) 우선 (예: 대용량 로그 수집):** `batch.size`를 늘리고 `linger.ms`에 여유를 두어 꽉꽉 채워 보냄.
  - `linger.ms` : 배치가 꽉 차지 않더라도, 브로커로 출발하기 전까지 기다려주는 최대 대기 시간.
    - * **실시간성(Low Latency) 우선 (예: 알림/결제):** `linger.ms`를 최소화하여 상자가 덜 차도 즉시 전송하게 유도하여 분산 속도를 높임.
- **결론**: 
  - 무작정 기본값을 쓰거나 배치 사이즈를 줄이는 것이 정답이 아니다.
  - 대용량 로그 수집(처리량 우선)인지, 실시간 알림(지연시간 최소화)인지 시스템의 성격에 맞춰 두 설정값을 조절해야 네트워크 부하를 줄이면서 분산 밸런스 맞춰줘야 한다.

## 🚨 대규모 분산 환경에서 메시지 순서(Order) 보장하기
- **문제:** 카프카는 파티션 내부의 순서는 보장하지만, 여러 파티션으로 데이터가 흩어지면 전체 순서(Global Order)가 보장되지 않음.
- **❌ 잘못된 접근:** 순서를 맞추기 위해 파티션을 1개로 설정 **-->** 동시 병렬 처리 **장점를 완전히 포기하게 되어 병목 발생**
- **✅ 올바른 접근 (메시지 Key 라우팅):** 프로듀서가 메시지를 전송할 때 식별 가능한 **메시지 Key (예: 주문번호, 유저ID)** 지정
  - 카프카의 해시 알고리즘에 의해 **동일한 Key를 가진 메시지는 무조건 동일한 파티션에 할당** 됨.
  - **결과:** 전체 메시지는 여러 파티션으로 분산되어 빠르게 병렬 처리되면서도, "동일한 주문"이나 "동일한 유저"에 대한 상태 변경 이벤트 **순서는 완벽하게 보장**


## ✏️ 메시지 전달 보장 수준
> 데이터 유실을 어디까지 허용할 것인가에 대한 3가지 정책
- At-most-once (최대 한 번):
  - 메시지가 유실될 수 있지만, 절대 중복 처리는 하지 않음.
  - 용도: 일부 유실되어도 무방한 단순 로그 수집.
- At-least-once (최소 한 번) - ⭐️ 실무 기본값: 
  - 메시지가 절대 유실되지 않음을 보장하지만, 네트워크 장애 시 중복 처리될 가능성이 있음.
  - 용도: 일반적인 데이터 파이프라인. (수신 측에서 중복을 걸러내는 멱등성 로직 필요) 
- Exactly-once (정확히 한 번):
  - 유실도 없고, 중복도 없이 무조건 정확히 한 번만 처리됨.
  - 용도: 결제, 정산 등 금융권 수준의 정합성이 필요한 시스템. (설정이 복잡하고 성능 저하가 발생할 수 있음)

## ✏️ 컨슈머의 오프셋 커밋 (Offset Commit) 전략
- 자동 커밋 (Auto Commit): 일정 주기(예: 5초)마다 백그라운드에서 알아서 도장을 찍습니다.
  - Kafka의 기본 값 : 초기 개발 목적이 "로그 수집 파이프라인" 였음
  - 비즈니스 로직을 처리헤야 하는 실무에서는 대부분 수동 커밋을 사용함 
- 수동 커밋 (Manual Commit) - ⭐️ 실무 권장:
  - 개발자가 코드상에서 DB 저장 등 비즈니스 로직이 완벽하게 성공했을 때만 명시적으로 도장을 찍는 방식입니다. 
    - 스프링에서는 `Acknowledgment.acknowledge()` 등을 사용
  - 데이터 유실을 막기 위해 실무에서는 대부분 수동 커밋을 사용합니다.


## ✏️ 컨슈머의 멱등성 (Idempotency) 보장 로직
- **문제 상황**: 결제 완료 메시지가 두 번 들어와서 DB에 결제 데이터가 2건 INSERT 되거나 포인트가 두 번 지급되는 이슈 발생.
- **해결책**: 비즈니르 로직단에서 **멱등성(Idempotency: 연산을 여러 번 적용하더라도 결과가 달라지지 않는 성질)** 을 띠도록 로직 설계 필요.
  - 1 . 도메인 상태(Status) 기반 방어 로직 (JPA 객체 지향적 접근)
    - 저장된 엔티티의 상태를 변경하는(UPDATE) 로직일 때 사용 가능
    - 해당 방법은 DB에 Connect 해야하는 비용이 있지만 사용 규모 및 코드의 가독성을 생각하면 가장 우아한 처리가 될 수 있음
  - 2 . Redis를 활용한 중복 필터링 (대규모 트래픽의 실무 표준)
    - 메시지가 들어오면 Spring 서버가 가장 먼저 Redis에 해당 event_id가 **존재 여부 확인**
    - Redis에 없다면 ➡️ Redis에 event_id를 기록(TTL 설정)하고, **DB에 데이터를 저장**
      - `SETNX` 명령어 나 `Redisson` 분산 락을 활용하여 동시성까지 제어
    - Redis에 있다면 ➡️ 중복 메시지이므로 DB 근처에도 가지 않고 **로직을 즉시 종료**


## Kafka docker cli
###🔍 Topic
```shell
# payment.request 토픽 생성
docker exec -it kafka-kraft kafka-topics --create --bootstrap-server localhost:9092 --topic payment.request --partitions 3 --replication-factor 1

# order.create 토픽 생성
docker exec -it kafka-kraft kafka-topics --create --bootstrap-server localhost:9092 --topic order.created --partitions 3 --replication-factor 1

# 생성된 토픽 목록 확인
docker exec -it kafka-kraft kafka-topics --list --bootstrap-server localhost:9092
```

### 🔍 메시지 확인
```shell
# group을 바꿔가며 확인하면 값을 처음부터 확인이 가능함
docker exec -it kafka-kraft kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.request --from-beginning --group {{test-group-1}}
```

## 🚀 파티션(Partition)과 키(Key) 순서 보장
> Kafka가 다른 MQ와 차별화되는 핵심 기능인 **파티션**를 가지고 있으며, **Key 유무에 따른 파티션별 순서 보장 여부**를 결정한다.

### 테스트 환경
* **Topic:** `order.request`
* **Partitions:** 3개
* **Producer-Server:** Spring Boot (KafkaTemplate)
* **Consumer-Server:** Spring Boot (@KafkaListener)

### 테스트 시나리오 및 결과

#### Case 1: Key를 지정하지 않고 전송 (Key == Null)
* **방식:** 100개의 메시지를 Key 없이 전송.
* **라우팅 전략:** `Sticky Partitioning` (카프카 2.4 이후 기본값 적용)
* **결과 (테스트 검증):**
  * 예상과 달리 100개의 메시지가 파티션 `0, 1, 2`에 1개씩 **균등하게 라운드 로빈(Round-Robin) 방식으로 분배되지 않음**.
  * 특정 파티션에 메시지가 **몰려서 전송되는 현상을 확인**함.
  * **(원인 분석)**: 이는 프로듀서가 네트워크 전송 효율을 높이기 위해, 메시지를 하나의 배치(Batch)로 묶어 특정 파티션에 우선적으로 몰아넣는 **Sticky Partitioner**가 작동했기 때문.
  * **(🚨 한계점)** 전체적인 처리량(Throughput)은 좋으나, 동일한 사용자(또는 동일한 주문)에 대한 이벤트가 **여러 파티션으로 흩어질 가능성이 존재**하므로 **비즈니스 로직의 순서가 역전될 위험**이 있음.

#### Case 2: 특정 Key를 지정하여 전송 (Key == "order-123")
* **방식:** 주문 ID(`orderId`)와 같은 식별자를 Key로 지정하여 100개의 연관 메시지 전송.
* **라우팅 전략:** `Hash-based Routing` (해시 기반 라우팅)
* **결과:**
  * 카프카가 Key의 해시값을 계산하여 **무조건 동일한 파티션(예: 파티션 1)으로만 메시지를 밀어넣음.**
  * 해당 파티션을 담당하는 1개의 컨슈머가 메시지를 큐에 들어온 순서대로 정확하게 처리함.
  * **(✅ 장점)** 특정 도메인 데이터에 대한 **100% 순서 보장(Strict Ordering)** 달성.

### 💡 실무 적용 포인트
- 순서 보장 🙆 : 이벤트의 기준이 되는 고유 식별자(예: `orderId` 또는 `userId`)를 **Kafka Message Key로 지정**
  - **"사용자 A의 주문 ➡️ 결제 승인 ➡️ 배송 시작 ➡️ 취소"** 이벤트는 반드시 발생한 순서대로 적용이 필요한 경우
- 순서 필요 ❌ : 단순 푸시 알림 발송이나 단순 로그 수집과 같이 순서가 중요하지 않은 대용량 트래픽의 경우, Key 없이 발행하여 파티션 전체를 활용한 병렬 분산 처리 효율을 극대화

## 🚀 Consumer Group Scale-out 및 Rebalancing 분석

### 테스트 환경
* **Topic:** `order.request`
* **Partitions:** 3개
* **Producer-Server:** Spring Boot (KafkaTemplate)
* **Consumer-Server1:** Spring Boot (@KafkaListener)
* **Consumer-Server2:** Spring Boot (@KafkaListener)

### 테스트 시나리오 및 결과

#### Case 1: 인스턴스 추가 (Scale-out) 시 파티션 할당
* **방식:** 서버 A(8080) 단독 구동 중 서버 B(8081) 추가 투입.
* **결과:**
  * 서버 A가 독점하던 파티션 [0, 1, 2] 중 일부가 서버 B로 이동. 
    * Result : 서버 A(파티션 2) / 서버 B(파티션 0, 1) 로 분산 처리 확인.

#### Case 2: 리밸런싱 지연 발생 (Stop-the-World)
* **현상:** `서버 B 추가` 또는 `서버 A 종료` 시 약 `30~60초`간 메시지를 받지 못하는 현상 발생.
* **원인 분석:**
  * Session Timeout: 브로커가 기존 컨슈머의 '죽음'이나 '상태 변화'를 인지하기까지 기다리는 대기 시간(session.timeout.ms) 발생.
  * Eager Rebalancing: 모든 컨슈머가 현재 소유한 파티션을 반납하고 다시 할당받는 과정에서 전체 처리가 멈추는 'Stop-the-World' 단계 확인.

## 🚀 메세지 받는 방식 성능 최적화 ( 단건 씩 / 묶음[kafka가 내부 반복으로 단건으로 던져 줌] / List형태로 받기 )
> 메세지를 받는 방식은 원하는 로직에 맞게 선택하는 것이 중요하다

### 단건 씩
- `ConsumerConfig.MAX_POLL_RECORDS_CONFIG : "1"` 설정을 통해 한번에 최대로 받을 수 있는 메세지 묶음의 개수를 1개로 지정하면 된다. 
- `ConsumerConfig.MAX_POLL_RECORDS_CONFIG` 미설정 시 **기본값**은 `500` 이다.    
```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    // 공통 설정
    private Map<String, Object> commonConfig() {
        Map<String, Object> config = new HashMap<>();
        // 💡 브로커에게 메세지를 받을 때 "무조건 1개씩만 받을 수 있게" 강제합니다.
        // -> 기본 값 500
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        return config;
    }
}
```

### 묶음[kafka가 내부 반복으로 단건으로 던져 줌]
- 아무런 설정을 하지 않을 시 기본 값
  - Kafka는 원래 부터 대량의 메세지 전송이기에 효율을 위헤 데이터를 묶어서 전달하여 내부 Kafka 로직안에서 반복을 돌며 단건으로 던져주기에 묶음으로 받지만 단건으로 받는 것 처럼 보였던 것이다.

### List형태로 받기




## Zookeeper 사용 버전
### 단일 노드 방식 예시 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/easy-version)
- 경량화된 Kafa, Zookeeper를 사용하여 Producer, Conuser 사용
### Cluster 방식 예시 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/advance-version)
- 3개의 Borkder, Zookeeper를 이용하여 Cluster 구성과 파티셔닝 및 복제 사용