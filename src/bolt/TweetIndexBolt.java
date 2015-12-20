package bolt;

import index.i3.I3Indexer;
import index.i3.Point;
import index.i3.QueryProcessing;
import topology.Global;
import structure.POI;
import structure.Tweet;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

/**
 * Build the index for tweets
 * and initialize POIs with recent tweets if they are newly created
 * Created by Xianyan Jia on 1/11/2015.
 */
public class TweetIndexBolt extends BaseRichBolt {
    I3Indexer i3Indexer;
    QueryProcessing queryProcessor;
    private OutputCollector _collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        String i3IndexSaveFolder = Global.i3IndexSaveFolder;
        i3Indexer = new I3Indexer(i3IndexSaveFolder);
        queryProcessor = new QueryProcessing(i3Indexer.idx);
        _collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        if (tuple.contains("tweet")) {
            Tweet tweet = (Tweet) tuple.getValueByField("tweet");
            i3Indexer.build(tweet.getLat(), tweet.getLng(), tweet.getText());
        } else if (tuple.contains("poi")) {
            POI poi = (POI) tuple.getValueByField("poi");
            int grp_id = (Integer) tuple.getValueByField("grp_id");
            Vector<String> keywords = new Vector<>();
            keywords.addAll(Arrays.asList(poi.getKeywords()));
            Vector<QueryProcessing.ResultTuple> result = queryProcessor.query(new Point(poi.getLat(), poi.getLng()), keywords, Global.K);
            _collector.emit(new Values(result, poi, grp_id));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("i3result", "poi", "grp_id"));
    }
}
