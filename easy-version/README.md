# Kafka 간단한 사용 예제
```properties
# ℹ️ 간단한 방법 사용 예제
#     - Docker Compose를 사용해서 구동함
#       ㄴ> wurstmeister(경량화) 이미지를 사용해서 구동
```

- `Kafka Brokder`는 기본적으로 9022 포트에서 구동된다.
- `Zookeeper` 서버가 구동된 상태일때만 Kafka Broker 구동이 가능 하다.
  -  단 다음 버전에서부터는 `Kafka`만으로도 구동이 가능해 질 수 있다는 정보가 있

### Zookeeper 및 Kafka 실행
- 접근 
  - `docker exec -it {{container_name}} /bin/bash`
- `Topic` 생성
  - `kafka-topics.sh --create --topic [생성할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ] --partitions [ 파티션 번호 ]`  
- `Topic` 목록 확인
  - `kafka-topics.sh --bootstrap-server [ Kafka Broker 도메인 ] --list`
- `Topic` 삭제
  - `kafka-topics.sh --delete --topic [삭제할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
- `Topic` 정보 확인
  - `kafka-topics.sh --describe --topic [확인할 topic 이름] --bootstrap-server [ Kafka Broker 도메인 ]`
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
