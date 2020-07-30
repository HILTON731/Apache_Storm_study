# Storm

- Topology: Storm의 분산 연산 구조
    - Data Stream
    - Spout: Stream 생성자
    - Bolt: 연산자(operator)

Storm Topology: Hadoop과 같은 Batch 처리 시스템의 Job과 유사하지만<br> 
Kill/Undeploy할때까지 계속 동작한다는 차이점이 있음

#### 분산처리
- 코드를 복제해서 노드들에게 전달해주고 노드들은 복제된 코드의 일부를 기능하는 것
- 데이터를 노드에게 전송할때 "직렬화"를 거침
- 직렬화된 코드나 데이터를 역직렬화(Deserialization)하기 위해서 해당 코드가 필요함
- 따라서 분산처리를 하기위해 코드를 복제하여 모든 노드들이 전체 코드를 보유는 하고있음
- 직렬화: 자바 시스템 내부에서 사용되는 객체 또는 데이터를 외부의 자바 시스템에서도 사용할 수 있도록 바이트(byte) 형태로 데이터 변환하는 기술과 바이트로 변환된 데이터를 다시 객체로 변환하는 기술(역직렬화)을 아울러 야기 됨
- JVM(Java Virtual Machine 이하 JVM)의 메모리에 상주(힙 또는 스택)되어 있는 객체 데이터를 바이트 형태로 변환하는 기술과 직렬화된 바이트 형태의 데이터를 객체로 변환해서 JVM으로 상주시키는 형태

### Stream
- Tuple: Storm의 기본 데이터 구조체
    - Named value(Key-value pair)의 목록
- Stream: 연속된 Tuple

### Spout
- Data가 Storm topology로 들어가는 입구
- Adaptor로써 동작하는데 Data source와 연결을 맺고 Data를 Tuple로 변환하여 Stream으로
Tuple을 내보내는 일

### Bolt
- 실시간 연산의 연산자 함수
- 다수의 Stream을 입력받아 Data를 처리하고 선택적으로 하나 이상의 Stream으로 내보냄

## 개발환경 구축(MAVEN)
pom.xml에 해당 Tag 삽입

    <dependencies>
            <dependency>
                <groupId>org.apache.storm</groupId>
                <artifactId>storm-core</artifactId>
                <version>2.0.0</version>
                <scope>provided</scope>
            </dependency>
        </dependencies>
        
## Storm의 병렬성
- Node(노드, 장비): Storm cluster에 포함된 장비(Server), 실제로 Topology가 동작하는 곳
- Worker(워커, JVM): Node에서 동작하는 독립적인 JVM 프로세스
- Executer(스레드): Worker JVM 프로세스에서 동작하는 JAVA 스레드
- Task(작업단위, Bolt/Spout instance): Spout/Bolt instance

![storm_img](.\img\storm_img.png)

## Topology에 Worker 추가
```java

Config config = new Config();
config.setNumWorkers(2);
```
- Topology 1 : Worker 1 --> Topology 1 : Worker 2
- 병렬성이 증가하여 계산 능력 향상

## Executer와 작업단위 수 설정
- 작업 단위 수 증가 : 동일 Layer의 Executer 개수 증가
```java
builder.setBolt(SPLIT_BOLT_ID, splitBolt, 2).setNumTasks(4).shuffleGrouping(SENTENCE_SPOUT_ID);

builder.setBolt(COUNT_BOLT_ID, countBolt, 4).fieldsGrouping(SPLIT_BOLT_ID, new Fields("word"));
``` 
- wordcount 예제에서 2개의 Executer에서 4개의 Task로 동작하도록 설정
- Local mode에서 동작하는 Topology는 단일 JVM process에서 동작:
작업단위나 Executer의 수를 늘려 병렬화하는 것만 효과가 있음

## Stream Grouping

Stream의 Tuple들이 다수의 Bolt로 어떻게 분산되는지에 대해 정의한 것<br>
Topology를 구성하는 것의 일부
- Stateless Operator: 수신자 Task에 상관없이 Tuple을 전송
    - Shuffle Grouping: Target Bolt 작업단위들에게 RR(라운드로빈) 방식으로 Tuple을 나누어 줌
    - None Grouping: 개발자가 어떻게 스트림을 분배할지 관여하지 않음, 무작위로(randomly) 동작
    - Local or Shuffle Grouping: 같은 Worker 프로세스에 Task가 하나 이상 존재하면, Worker내에서 Shuffle Grouping 진행,
    이 외의 경우 Shuffle Grouping과 동일
    - All Grouping: Stream이 모든 Task에 복제, Stream의 양이 Task 개수만큼 증가
    - Global Grouping: 전체 Stream을 Task ID가 가장 낮은 하나의 Task에게 전달, 병목현상 발생 가능
    
- Stateful Operator: 특정 수신자 Task에게 Tuple을 전송
    - Direct Grouping: Stream에서 명시된 Task에게 Tuple 전달, emitDirect() method사용,
     TopologyContext, OutputCollect 객체 활용하여 Task ID 얻을 수 있음
    - Field Grouping: 명시된 Field에 따라 Stream을 분배
    - Partial Key Grouping: Field Grouping과 유사, Field가 동일한 Tuple들을 두개의 Task로 나누어 Load Balancing

- CustomStreamGrouping interface를 통한 구현
    - prepare(): Grouping 구현체가 Tuple을 내보낼 작업단위를 정할 때 필요한 Grouping 정보를 초기화하기 위해 실행시간에 1회 호출
        - WorkerTopologyContext context: Storm cluster의 전반을 담은 객체
        - GlobalStreamId stream: Tuple을 보내는 component의 정보를 담은 객체
        - List\<Integer> targetTasks: 수신자 Task들의 ID들
    - chooseTasks(): Tuple을 보낼 때마다 실행
        - int taskId: 송신자 Task ID
        - List\<Object> values: Tuple
        - return: Message를 전송할 수신자 Tasks