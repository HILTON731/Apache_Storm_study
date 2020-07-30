import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;

import static java.lang.Thread.sleep;

public class SentenceSpout extends BaseRichSpout { // ISpout, IComponent 인터페이스를 구현한 편의 클래스
    private SpoutOutputCollector collector;
    private String[] sentences = {
            "my dog has fleas",
            "i like cold beverages",
            "the dog ate my homework",
            "don't have a cow man",
            "i don't think i like fleas"
    };
    private int index = 0;
    
    public void declareOutputFields(OutputFieldsDeclarer declarer) { // 모든 스톰 컴포넌트가 구현해야하는 메소드, 컴포넌트가 어떤 스트림을 내보내고 스트림의 튜플이 어떤 필드들로 구성되었는지 스톰에게 알림
        declarer.declare(new Fields("sentence"));
    }
    
    public void open(Map config, TopologyContext context, SpoutOutputCollector collector){ // Spout 컴포넌트가 초기화될 때 호출, 스톰 설정 정보를 가진 Map, 토폴로지에 속한 컴포넌트들의 정보를 가진 TopologyContext 객체, 튜플을 내보낼때 사용하는 메소드를 제공하는 SpoutOutputCollector
        this.collector = collector;
    }
    public void nextTuple(){ // Spuout 구현체의 핵심 메소드, OutputCollector를 이용해 튜플을 내보내라고 Spout에게 요청
        this.collector.emit(new Values(sentences[index]));
        index++;
        if(index >= sentences.length){
            index=0;
        }
        try {
            sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
