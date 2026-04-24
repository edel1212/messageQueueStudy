# Kafka Cluster - used Zookeeper

```properties
# ℹ️ Cluster란 ?
#  - 여러 대의 컴퓨터들이 연결되어 하나의 시스템처럼 동작하는 컴퓨터들의 집합을 말한다.
#    ㄴ> 쉽게 설명 하자면 다수의 Kafka Broker와 Zookeeper를 묶어서(상호작용이 가능하게) 구성해 놓은 상태라고 보면된다
```

### 복제(Replication) 란?
- 메시지를 복제해 관리 브로커에 장애가 발생해도 다른 브로커가 해당 브로커의 역할을 대신할 수 있도록 하는 것이다.
  - 더 쉽게 설명 : 토픽의 파티션의 복제본을 몇 개를 생성할 지에 대한 설정이다.
  - 이미지 예시
    - topic01 는 `replication.factor`를 **2로**, topic02 는 **3으로** 설정한 경우
      
      ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/786b02f4-47be-422f-aa93-3010c435e924)

### 흐름

  <img width="837" alt="211142238-ef4c58a3-d488-41f5-9ebb-4663d9643feb" src="https://github.com/edel1212/messageQueueStudy/assets/50935771/bf0c9df3-24d5-41dc-98ff-27d711733d04">


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

### Lag란?
- Poducer가 데이터를 넣고 Cousumer가 데이터를 데이터를 읽을 경우 이 차이(Offset 차이)를 말한다.

  <img width="1023" alt="211139263-ccb27719-6b14-42d0-9fea-b41283461794" src="https://github.com/edel1212/messageQueueStudy/assets/50935771/3c542364-d0a9-4515-9a73-c26af0e101ba">

- Kafka에서 Lag 값을 통해 Producer, Consumer의 상태를 유추할 수 있다. 즉, Lag이 높다면 Consumer에 문제가 있다는 뜻일 수 있다

  
### 컨트롤러(Contoller)
- Kafka 클러스터의 다수 브로커 중 한 대가 컨트롤러 역할을 한다. 컨트롤러는 다른 브로커들의 상태를 체크하고 브로커가 클러스터에서 빠지는 경우 해당 브로커에 존재하는 리더 파티션을 재분배한다.

  ![image](https://github.com/edel1212/messageQueueStudy/assets/50935771/cc62f245-86f2-422b-9b8f-f06f51d529d0)

### Consumer 대표 옵션
- group.id
  - 컨슈머가 속한 컨슈머 그룹을 식별하는 식별자이다 Group이 지정되면 같은 그룹끼리 확인한 메세지는 보이지 않는다
- enable.auto.commit
  - 백그라운드로 주기적으로 오프셋(`offset`)을 커밋한다.
  - 기본 값: true
- auto.offset.reset
  - Kafka에서 초기 오프셋이 없거나 현재 오프셋이 더 이상 존재하지 않은 경우(데이터가 삭제)에 다음 옵션으로 리셋한다.
    - earliest: 가장 초기의 오프셋값으로 설정한다.
    - latest: 가장 마지막의 오프셋값으로 설정한다.(기본 값)
    - none: 이전 오프셋값을 찾지 못하면 에러를 나타낸다
- max.poll.records
  - 단일 호출 poll()에 대한 최대 레코드 수를 조정한다.
  - 기본 값은 500 이다.
- max.poll.interval.ms
  - 해당 옵션 시간 만큼 컨슈머 그룹에서 컨슈머가 살아 있지만 poll() 메소드를 호출하지 않을 때 장애라고 판단하여 컨슈머 그룹에서 제외 후 다른 컨슈머에게 전달
  - 기본 값: 5분
- auto.commit.interval.ms
  - 주기적으로 오프셋을 커밋하는 시간
  - 기본 값: 5초

    

   
