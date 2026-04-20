# Kafka Cluster

## 참고 사항
- `kafka node id` : 정수만 가능하다.
- `KAFKA_PROCESS_ROLES` : 노드의 역할을 지정한다.
    - **소규모/테스트:** `broker,controller` (통합 노드)
    - **대규모/상용:** `broker` (데이터 처리 전용) / `controller` (상태 관리 및 투표 전용)로 완전히 분리하여 리소스 병목을 방지
- `CONTROLLER` 리스너 분리 이유 : 클러스터의 상태 확인(하트비트) 및 메타데이터 관리를 독립적으로 수행하기 위함이다. 
  - 대규모 데이터 복제 트래픽이 발생하는 `INTERNAL` 망과 합칠 경우, 네트워크 병목 시 상태 확인이 지연되어 멀쩡한 브로커가 죽은 것으로 장애가 될 위험이 있다.

## 구축 설정 비교 ( Docker / 물리 서버 )
| 항목 | Docker (로컬 테스트용) | 물리 서버 (상용 환경) |
| :--- | :--- | :--- |
| **LISTENERS** | `0.0.0.0` (모든 IP 대기) | `0.0.0.0` 또는 실제 사내망 IP |
| **ADVERTISED_LISTENERS** | 내부망: `컨테이너명`<br>외부망: `localhost` | 내부/외부 모두 `실제 서버 IP` |
| **CLUSTER_ID** | 환경변수로 설정 | `kafka-storage.sh`로 생성 후 포맷 지정 |
| **포트 설정** | 한 PC에 띄우므로 `9092, 9093, 9094` 분리 | 서버가 각각 다르므로 모두 `9092` 통일 가능 |

## 정상 Cluster 동작 확인 방법

### KRaft 쿼럼 상태 확인
`bootstrap-server` 옵션에 `,`를 통해 여러 서버의 주소를 입력하여, 클러스터의 전반적인 투표권자(Quorum) 상태를 확인한다.
### 명령어
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