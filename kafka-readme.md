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
- `Consumer` : 토픽의 파티션에 저장된 메시지를 읽어와서 처리하는 주체
  - `Topic`에 등록한 메세지는 `Conumer`가 읽는다고 **사라지는게 아님**
  - 전통적인 메시지 큐 시스템과는 **다르게 설계**되었기 때문이다. **Kafka는 로그를 기반으로 하는 메시징 시스템이다.**
- `Consumer Group` : 토픽의 데이터를 병렬로 분담해서 처리하기 위해 묶어 놓은 Consumer들의 집합. 
  - 하나의 파티션은 동일 그룹 내에서 오직 한 개의 Consumer만 접근 가능하여 중복 처리를 방지함
  - ✅ 하나의 메세지를 다양하게 처리하기 위해서는 그만큼의 `Consumer Group`을 만들어줘야 함

## Zookeeper 사용 버전
### 단일 노드 방식 예시 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/easy-version)
- 경량화된 Kafa, Zookeeper를 사용하여 Producer, Conuser 사용
### Cluster 방식 예시 [링크](https://github.com/edel1212/messageQueueStudy/tree/main/advance-version)
- 3개의 Borkder, Zookeeper를 이용하여 Cluster 구성과 파티셔닝 및 복제 사용



## 🚨 Producer 파티션 쏠림(Skew) 이슈와 성능 튜닝
![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/1a87a924-1432-4efa-8d5e-a5333311f32c)
- **현상**: 메시지가 여러 파티션에 골고루 분산되지 않고, 특정 **파티션에만 쏠려서 쌓이는 현상** 발생
- **원인** (Sticky Partitioner):
  - 카프카(v2.4 이후) 기본 파티셔너 정책 때문이다.
  - 네트워크 통신(I/O) 효율을 높이기 위해, 하나의 파티션에 보낼 택배 상자(Batch)가 다 찰 때까지 한 파티션에만 메시지를 계속 몰아주는(Sticky) 방식을 사용하기 때문
- **해결 및 튜닝**: `batch.size`와 `linger.ms`를 서비스 트래픽 목적에 맞게 세트로 튜닝 진행
  - `batch.size` : 한 번에 묶어서 보낼 메시지 묶음의 최대 용량.
  - `linger.ms` : 배치가 꽉 차지 않더라도, 브로커로 출발하기 전까지 기다려주는 최대 대기 시간.
- **결론**: 
  - 무작정 기본값을 쓰거나 배치 사이즈를 줄이는 것이 정답이 아니다.
  - 대용량 로그 수집(처리량 우선)인지, 실시간 알림(지연시간 최소화)인지 시스템의 성격에 맞춰 두 설정값을 조절해야 네트워크 부하를 줄이면서 분산 밸런스 맞춰줘야 한다.


- ### `partition`의 순서 보장
    - 간단하다 분산 병렬 처리를 위해서이다!
    - Topic 안에 설정한 파티션으로 존재한다
        - 각각의 메세지가 파티션에 들어가 있는 형식이다!
        - 일정 Batch Size 설정을 해주지 않으면 한 파티션에만 들어 있기에 메세지가 들어오지 않는다.
    - Topic의 메세지가 파티션별로 들어가기에 전체 출력 시 순서서가 보장 되지 않을 수있다.
        - `--from-beginning` 옵션을 사용 할 경우에 그러하다 전체 출력 시 그러함
        - 원인 : 카프카 컨슈머에서의 메시지 순서는 동일한 파티션 내에서는 프로듀서가 생성한 순서와 동일하게 처리하지만, 파티션과 파티션 사이에서는 순서를 보장하지 않습니다.
        - 해결 방법 : 순서가 중요한 경우에는 파티션을 1개로 설정해주자
    - 예시
        - Producer가 `a,b,c,e,e,1,2,3,4,5`를 Topic에 입력하고 해당 Topic이 3개의 파티션으로 구성되었을 경우

      ![img1 daumcdn](https://github.com/edel1212/messageQueueStudy/assets/50935771/32f16cd8-5dc3-4c00-9067-5171f5fd6f89)

    -  출력 값

        ```properties
        ## 컨슈머 콘솔 명령어
        /usr/local/kafka/bin/kafka-console-consumer.sh \
        --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
        --topic peter-01 \
        --from-beginning
        
        출력 
        a
        d
        1
        4
        b
        e
        2
        5
        c
        3
        ```