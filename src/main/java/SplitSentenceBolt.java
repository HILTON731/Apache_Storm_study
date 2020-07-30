import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class SplitSentenceBolt extends BaseRichBolt { // IComponent, IBolt 인터페이스를 구현한 편의 클래스
    private OutputCollector collector;
    public void prepare(Map config, TopologyContext context, OutputCollector collector) { // IBolt 인터페이스에 정의된 메소드로 볼트가 초기화될 때 호출하기에 자원을 초기화하기 적절한 장소
        this.collector = collector;
    }

    public void execute(Tuple tuple) { // Bolt의 입력 튜플 스트림으로부터 새로운 튜플을 받을 때마다 호출
        String sentence = tuple.getStringByField("sentence");
        String[] words = sentence.split(" ");
        for(String word: words){
            this.collector.emit(new Values(word));
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) { // SplitSentenceBolt 클래스가 'word'필ㄹ드만 가진 튜플 스트림을 내보낸다고 정의
        declarer.declare(new Fields("word"));
    }
}
