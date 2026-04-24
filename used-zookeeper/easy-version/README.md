# Kafka Single Node - used Zookeeper

## Zookeeper란?
> Kafka 클러스터의 메타데이터를 관리하는 외부 코디네이터입니다.
### `Zookeeper` 서버가 구동된 상태일때만 Kafka Broker 구동 가능
- Zookeeper가 메타데이터를 관리해주기 때문이다.
### 관리하는 정보
- 브로커 목록 및 상태
- 토픽 설정 (파티션 수, 복제 팩터)
- 파티션 리더 정보
- Consumer Group 오프셋

### Zookeeper의 한계 (왜 KRaft로 넘어갔나)
- Kafka와 별도로 설치/운영/모니터링 필요
- Zookeeper 장애 → Kafka 전체 장애
- 파티션 수 증가 시 성능 한계
- 운영 복잡도 증가
- 👍 **Kafka 3.x 부터 KRaft 모드로 대체, Kafka 4.x 부터 Zookeeper 완전 제거** 

### 전체 구조
```text
Producer
   ↓ (메시지 전송)
Kafka Broker (9092)
   ↑ (메타데이터 관리)
Zookeeper (2181)
   ↓ (메시지 소비)
Consumer
```

### docker-compose.yml
```yaml
services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    # ⚠️ 추가 권장
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000  # Zookeeper 내부 시간 단위(ms)

  kafka:
    image: wurstmeister/kafka:2.12-2.5.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    # ⚠️ 추가 권장 - Zookeeper가 먼저 떠야 Kafka 기동 가능
    depends_on:
      - zookeeper
```


## CLI 
> ℹ️ Docker를 사용해 구동할 경우 명령어에 확장자를 붙이지 않는다 (.sh) 불필요 

### ℹ️ Producer
- Topic 생성
> replication-factor를 설정하지 않는 이유는 단일 노드이기 때문임
``` shell
kafka-topics.sh --create \
  --topic [생성할 topic명] \
  --bootstrap-server [Kafka Broker 도메인] \
  --partitions [분할 파티션 개수]
```

- Topic 목록 확인
```shell
kafka-topics.sh --bootstrap-server [Kafka Broker 도메인] --list
```
- Topic 접근 (메시지 등록)
```shell
kafka-console-producer.sh \
  --bootstrap-server [Kafka Broker 도메인] \
  --topic [topic명]
```

- Topic 삭제
```shell
kafka-topics.sh --delete \
  --topic [삭제할 topic 이름] \
  --bootstrap-server [Kafka Broker 도메인]
```

- Topic 정보 확인
```shell
kafka-topics.sh --describe \
  --topic [확인할 topic 이름] \
  --bootstrap-server [Kafka Broker 도메인]
```

### ℹ️ Consumer

- 일반 구독 확인
```shell
kafka-console-consumer.sh \
  --bootstrap-server [Kafka Broker 도메인] \
  --topic [구독할 topic 이름]
```

- 이전 메시지까지 확인 (`--from-beginning`)
> ℹ️ 토픽에 저장된 처음 메시지부터 조회
```shell
kafka-console-consumer.sh \
  --bootstrap-server [Kafka Broker 도메인] \
  --topic [구독할 topic 이름] \
  --from-beginning
```

- 그룹 지정 (`--group`)
```shell
kafka-console-consumer.sh \
  --bootstrap-server [Kafka Broker 도메인] \
  --topic [구독할 topic 이름] \
  --group [그룹명]
```



