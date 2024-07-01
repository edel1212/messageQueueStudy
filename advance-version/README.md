# Kafka Cluster 구성 및 사용 예제

```properties
# ℹ️ Docker Compose를 사용해서 Kafka Cluster를 구성함
#
# ℹ️ Cluster란 ?
#  - 여러 대의 컴퓨터들이 연결되어 하나의 시스템처럼 동작하는 컴퓨터들의 집합을 말한다.
#    ㄴ> 쉽게 설명 하자면 다수의 Kafka Broker와 Zookeeper를 묶어서(상호작용이 가능하게) 구성해 놓은 상태라고 보면된다
```

### 브로커(Broker) 란?

- 데이터를 분산 저장하여 장애가 발생하더라고 안전하게 사용할 수 있도록 도와주는 애플리케이션이다.
  - Kafka 서버라고 봐도 무방하다.
- 브로커 서버를 여러대로 구성하여, 클러스터(Cluster)로 묶어서 운영할 수 있다.
  - 카프카 클러스터로 묶인 브로커들은 프로듀서가 보낸 데이터를 안전하게 분산 저장하고 복제하는 역할을 수행함

### 토픽(Topic)이 란?
- 카프카에서 데이터를 **구분하기 위해 사용하는 단위**이다
- 토픽은 **1개 이상**의 **파티션**을 소유한다.

### 파티션(Partition)이 란?
- 데이터를 효율적으로 처리하는 데 필수적인 역할을 한다
- 각각의 파티션은 독립적인 데이터 스트림을 형성하며, 이를 통해 메시지가 병렬로 처리될 수 있다.
- Topic을 이루고 있는 단위이
- Producer가 보낸 데이터들이 파티션에 들어가 저장되고 이 데이터를 레코드라고 부른다.
  
  ![image](https://github.com/edel1212/messageQueueStudy/assets/50935771/1a1b5934-8f14-485d-8674-5d558eb0e41a)


### 커멘드 사용 시 주의  
- `BootStrap-Server`정보 또한 `","`를 사용해서 여러개 등록이 가능하다.
  - 네트워크 주소는 컨테이너명으로 지정해줘야한다.
  - Port는 Docker-Compose 내 설정한 Port로 작성해줘야한다.
- 예시
  - 메세지 등록   
    - `kafka-console-producer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom`
  - 컨슈머 등록
    - 이전 까지 모든 메세지
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --from-beginning`
    -  그룹 지정
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --group zero`
  
