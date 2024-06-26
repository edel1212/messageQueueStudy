# Kafka 간단한 사용 예제
```properties
# ℹ️ 간단한 방법 사용 예제
#     - Docker Compose를 사용해서 구동함
#       ㄴ> wurstmeister(경량화) 이미지를 사용해서 구동
```

- `Kafka Brokder`는 기본적으로 9022 포트에서 구동된다.
- `Zookeeper` 서버가 구동된 상태일때만 Kafka Broker 구동이 가능 하다.
  - `Zookeeper`가 메타 데이터를 관리해 주기 떄문이다.  

### Zookeeper 및 Kafka 실행
- 접근 
  - `docker exec -it {{container_name}} /bin/bash`
- ℹ️ Producer
  - `Topic` 생성
    - `kafka-topics.sh --create --topic [생성할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ] --partitions [ 분할 파티션 개수 ]`  
  - `Topic` 목록 확인
    - `kafka-topics.sh --bootstrap-server [ Kafka Broker 도메인 ] --list`
  - `Topic` 삭제
    - `kafka-topics.sh --delete --topic [삭제할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
  - `Topic` 정보 확인
    - `kafka-topics.sh --describe --topic [확인할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
- ℹ️ Consumer
  - `Topic` 메세지 확인
    - 일반 구독 확인
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름]`
    - 이전 메세지까지 확인 (`--from-beginning`)
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름] --from-beginning`
    - 확인한 메세지 안보이게 (`--from-beginning`)
      - `kafka-console-consumer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [구독할 topic 이름] --group [ 그룹 지정 ]`
```properties
services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka:2.12-2.5.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
```
