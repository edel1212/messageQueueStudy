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

## CLUSTER_ID 생성 및 지정 (물리서버 구축)
>  클러스터로 묶인 모든 브로커가  "같은 클러스터 ID"를 가져야 하나의 팀으로 묶임
```shell
# 1. CLUSTER_ID 생성 (한 서버에서 딱 한번만)
kafka-storage.sh random-uuid

# 2. 3대 서버 모두 동일한 UUID로 포맷 (Kafka 기동 전)
kafka-storage.sh format -t {생성된UUID} -c /path/server.properties
```


## 정상 Cluster 동작 확인 (KRaft 쿼럼 상태 확인)
> `bootstrap-server` 지정 시 `,`를 통해 여러 서버의 주소를 입력하여, 클러스터의 전반적인 투표권자(Quorum) 상태를 확인 할 수 있음
### 확인 명령어
```shell
kafka-metadata-quorum --bootstrap-server 192.168.0.50:9092,192.168.0.51:9092,192.168.0.52:9092 describe --status
```
### 응답
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