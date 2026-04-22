# Kafka Cluster

## 참고 사항
- `kafka node id` : 정수만 가능하다.
- `KAFKA_PROCESS_ROLES` : 노드의 역할을 지정한다.
    - **소규모/테스트:** `broker,controller` (통합 노드)
      - ex) `KAFKA_PROCESS_ROLES: "broker,controller"`
    - **대규모/상용:** `broker` (데이터 처리 전용) / `controller` (상태 관리 및 투표 전용)로 완전히 분리하여 리소스 병목을 방지
      - ex) 상태 관리 및 투표 전용 : `KAFKA_PROCESS_ROLES: "controller"`
      - ex) 데이터 처리 전용 : `KAFKA_PROCESS_ROLES: "broker"`
- `KAFKA_INTER_BROKER_LISTENER_NAME` : 브로커들끼리 **파티션 데이터를 복제**할 때 사용할 **통신 채널 이름 지정**
  - `INTERNAL` 채널을 복제 전용으로 외부(EXTERNAL)과 **채널을 분리하여 복제 성능 보장**
  - ex) `KAFKA_INTER_BROKER_LISTENER_NAME: "INTERNAL"`
- `KAFKA_CONTROLLER_LISTENER_NAMES` : KRaft Controller 중 **리더 선출 및 메타데이터를 동기화**할 때 사용할 **통신 채널 이름 지정**
    - 대규모 데이터 복제 트래픽이 발생하는 `INTERNAL` 망과 합칠 경우, 네트워크 병목 시 상태 확인이 지연되어 멀쩡한 브로커가 죽은 것으로 장애가 될 수 있음
    - ex) `KAFKA_CONTROLLER_LISTENER_NAMES.CONTROLLER` 
- `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` : 관리자가 정의한 리스너 이름과 실제 보안 프로토콜을 매핑 설정
  - `INTERNAL : PLAINTEXT` : 브로커 내부 복제 통신, 암호화 없음
  - `EXTERNAL : PLAINTEXT` : Spring Boot 등 외부 접속, 암호화 없음
  - `CONTROLLER : PLAINTEXT` : 컨트롤러 상태 관리 통신, 암호화 없음

## 전체 통신 흐름
```text
[Spring Boot]
    ↓ EXTERNAL (9092)
[Kafka 브로커]
    ↓ INTERNAL (29092)      ← 파티션 복제
[다른 브로커들]
    ↓ CONTROLLER (29093)    ← 리더 선출 / 메타데이터 동기화
[Controller 브로커]
```

## 구축 설정 비교 ( Docker / 물리 서버 )
| 항목 | Docker (로컬 테스트용) | 물리 서버 (상용 환경) |
| :--- | :--- | :--- |
| **LISTENERS** | `0.0.0.0` (모든 IP 대기) | `0.0.0.0` 또는 실제 사내망 IP |
| **ADVERTISED_LISTENERS** | 내부망: `컨테이너명`<br>외부망: `localhost` | 내부/외부 모두 `실제 서버 IP` |
| **CLUSTER_ID** | 환경변수로 설정 | `kafka-storage.sh`로 생성 후 포맷 지정 |
| **포트 설정** | 한 PC에 띄우므로 `9092, 9093, 9094` 분리 | 서버가 각각 다르므로 모두 `9092` 통일 가능 |

## CLUSTER_ID 생성 및 브로커들 묶음 (물리서버 구축)
>  클러스터로 묶인 모든 브로커가  "같은 클러스터 ID"를 가져야 하나의 팀으로 묶임
```shell
# 1. CLUSTER_ID 생성 (한 서버에서 딱 한번만)
kafka-storage.sh random-uuid

# 2. 3대 서버 모두 동일한 UUID로 포맷 (Kafka 기동 전)
kafka-storage.sh format -t {생성된UUID} -c /path/server.properties
```

## Cluster 동작 확인

### 1. 쿼럼(투표) 상태 확인 방법
> `bootstrap-server` 지정 시 `,`를 통해 여러 서버의 주소를 입력하여, 클러스터의 전반적인 투표권자(Quorum) 상태를 확인 할 수 있음
#### CLI 명령어
```shell
kafka-metadata-quorum --bootstrap-server 192.168.0.50:9092,192.168.0.51:9092,192.168.0.52:9092 describe --status
```
#### 응답
```text
ClusterId:              MkU3OEVBNTcwNTJENDM2Qk
LeaderId:               1
LeaderEpoch:            5
HighWatermark:          1234
MaxFollowerLag:         0
MaxFollowerLagTimeMs:   0
CurrentVoters:          [1, 2, 3]
CurrentObservers:       []
```

### 2. kafka UI 사용
> Docker compose를 사용하여 진행
```yaml
services:
  kafka-ui:
  image: provectuslabs/kafka-ui:latest
  container_name: kafka-ui
  ports:
    - "8090:8080" # 호스트 8090 포트를 컨테이너 8080에 매핑
  networks:
    - kafka-cluster-net
  environment:
    # UI에 표시될 클러스터 이름 (구분용 라벨)
    KAFKA_CLUSTERS_0_NAME: local-kraft-cluster
    # 💡 핵심: UI 컨테이너는 Docker 내부망에서 접속하므로 INTERNAL 주소 사용
    #    (localhost:9092 쓰면 UI 컨테이너가 자기 자신을 찾아서 실패함)
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: "kafka-1:29092,kafka-2:29092,kafka-3:29092"
    # 💡 [UI 설정] 런타임 환경에서 클러스터 설정을 동적으로 수정 허용
    # - true: UI 화면 내 'Config' 탭에서 브로커 설정이나 토픽 설정을 즉시 변경 가능합니다.
    # - false(기본값): UI는 읽기 전용(Read-only) 모드에 가까워지며, 설정 변경 기능이 비활성화됩니다.
    # - 주의: 운영 환경에서는 실수로 설정을 바꾸지 않도록 false로 두는 경우가 많습니다.
    DYNAMIC_CONFIG_ENABLED: "false"
    # 인증 관련 설정
    AUTH_TYPE: "LOGIN"
    SPRING_SECURITY_USER_NAME: "admin"
    SPRING_SECURITY_USER_PASSWORD: "123"

  # 💡 depends_on + condition: service_healthy
  # - 위에서 정의한 healthcheck가 통과해야 Kafka UI 기동
  # - 이 설정이 없으면 Kafka가 아직 준비 안 됐는데 UI가 먼저 떠서 연결 실패 로그 반복
  depends_on:
    kafka-1:
      condition: service_healthy
    kafka-2:
      condition: service_healthy
    kafka-3:
      condition: service_healthy
```

## 🚀 브로커 장애 시 ISR 확인
- `ISR (In-Sync Replicas)` : Leader와 충분히 동일한 로그를 유지하고 있는 replica(브로커)들의 ID 목록
  - Leader는 항상 ISR에 포함된다.
  - Follower는 지연(lag)이 허용 범위(replica.lag.time.max.ms)를 넘으면 ISR에서 제외된다.

### CLI 
```shell
# 토픽 상태 확인
kafka-topics --describe \
  --topic {{토픽명}} \
  --bootstrap-server {{브로커1}},{{브로커2}}

# 특정 브로커 중지 (장애 상황 가정)
docker stop {{브로커}}

# 다시 상태 확인
kafka-topics --describe \
  --topic {{토픽명}} \
  --bootstrap-server {{브로커1}},{{브로커2}}
```

### 응답 값
- `Leader` : 현재 **해당 파티션을 담당**하는 브로커 ID
- `Replicas` : 파티션을 복제하고 있는 전체 브로커 목록
- `Isr` : 현재 정상적으로 동기화된 replica 목록
```text
Topic: order.request    TopicId: y08uwOT3RoK2yoTTWIUT6g PartitionCount: 3       ReplicationFactor: 3    Configs:
        Topic: order.request    Partition: 0    Leader: 3       Replicas: 2,3,1 Isr: 3,1
        Topic: order.request    Partition: 1    Leader: 3       Replicas: 3,1,2 Isr: 3,1
        Topic: order.request    Partition: 2    Leader: 1       Replicas: 1,2,3 Isr: 1,3
```

## 🚀 Producer acks 상황별 설정
> Producer가 브로커(kafka)로부터 "어디까지 응답을 기다릴지" 결정하는 설정 [ 속도와 안전성(데이터 유실 방지)의 트레이드오프 관계 ]

### acks 설정 비교
| 설정값             | 동작 방식                             | 속도   | 안정성                    | 특징                      |
| --------------- | --------------------------------- |------|------------------------| ----------------------- |
| acks=0          | 브로커 응답을 기다리지 않음 (fire-and-forget) | 가장 빠름 | 👎 매우 낮음 (유실 가능)       | 네트워크/브로커 상태 무시하고 전송만 수행 |
| acks=1          | Leader 브로커만 수신 확인                 | 빠름   | 중간 (Leader 장애 시 유실 가능) | 일반적인 기본 설정              |
| acks=all (`-1`) | ISR(복제본 전체) 수신 확인                 | 느림   | 👍 매우 높음 (유실 없음에 가까움)  | 데이터 신뢰성 최우선             |


### 흐름 비교
```text
acks=0   : Producer ─→ Leader                        (응답 대기 ❌)

acks=1   : Producer ─→ Leader ─→ Producer            (Leader 응답만 대기)

acks=all : Producer ─→ Leader ─→ Follower 복제 ─→ Producer (ISR 전체 완료 대기)
```

### 식당 비유
> 차이는 손님(Producer)의 "기다리는 태도"뿐, 주방(Broker)은 똑같이 일한다.

| 구분    | 손님 A (acks=all)            | 손님 B (acks=0) | 주방 (브로커)       |
| ----- | -------------------------- | ------------- | -------------- |
| 주문    | "비빔밥 하나요!"                 | "김밥 하나요!"     | 둘 다 주문을 받음     |
| 처리 방식 | 음식이 완전히 나올 때까지 기다림         | 주문만 하고 바로 떠남  | 평소처럼 요리 진행     |
| 응답 대기 | 모든 과정 완료까지 기다림 (ISR 전체 확인) | 응답 기다리지 않음    | 응답 여부와 관계없이 처리 |



