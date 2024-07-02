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

### 복제(Replication) 란?
- 메시지를 복제해 관리 브로커에 장애가 발생해도 다른 브로커가 해당 브로커의 역할을 대신할 수 있도록 하는 것이다.
  - 더 쉽게 설명 : 토픽의 파티션의 복제본을 몇 개를 생성할 지에 대한 설정이다.
  - 이미지 예시
    - topic01 는 `replication.factor`를 **2로**, topic02 는 **3으로** 설정한 경우
      
      ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/786b02f4-47be-422f-aa93-3010c435e924)

- replication 설정 수가 많아지면 그만큼 데이터의 복제로 인한 성능 하락이 있을 수 있기 때문에 무조건 크게 설정하는 것이 권장되지는 않는다.
  - 설정 수를 높이는건 쉽지만 높여 놓은 설정을 다시 낮추는건 어려우니 상황에 맞게 점차적으로 올려가면서 맞추는게 중요하다.

- #### Leader & Follower  
  - 역할 
    - Leader : Topic 으로 통하는 모든 데이터의 Read/Write 는 오직 Leader 를 통해서 이루어진다.
      - 브로커별로 나눠진 토픽중 하나로 선정된다.    
    - Follower : 리더가 되지 못한 나머지 브로커들
  - 사용 이유
    -  리더는 항상 정상적으로 동작해야 한다. 하지만 어떻게 그렇게 할 수 있을까? 리더도 똑같은 브로커 중 하나일 뿐인데 장애가 발생했을 때 처리를 위함이다.

- #### ISR(In-Sync Replication)
  - `Replication`을 나눠 놓은 각각의 Leader 혹은 Flower들을 모아 놓은 Group 이다.

    ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/397adb2c-b574-4487-9786-cd974f2d42e1)
    
### 커멘드 사용 시 주의  
- `BootStrap-Server`정보 또한 `","`를 사용해서 여러개 등록이 가능하다.
  - 네트워크 주소는 컨테이너명으로 지정해줘야한다.
  - Port는 Docker-Compose 내 설정한 내부용 Port로 작성해줘야한다.
    - ℹ️  `Docker Compose`에서 외부, 내부, Docker 간 의 포트를 나눈 이유는 이 때문이다.
- 예시
  - 메세지 등록   
    - `kafka-console-producer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom`
  - 컨슈머 등록
    - 이전 까지 모든 메세지
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --from-beginning`
    -  그룹 지정
      -  `kafka-console-consumer --bootstrap-server kafka_zookeeper_compose-kafka-1-1:29092,kafka_zookeeper_compose-kafka-2-1:29093,kafka_zookeeper_compose-kafka-3-1:29094 --topic gom   --group zero`
   

  
