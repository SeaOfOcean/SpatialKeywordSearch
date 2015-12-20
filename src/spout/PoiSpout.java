package spout;

import index.partition.ZCurvePartitioner;
import structure.POI;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xianyan Jia on 24/10/2015.
 */
public class PoiSpout extends BaseRichSpout {
    SpoutOutputCollector collector;
    String poiFileName;
    ZCurvePartitioner zCurve;
    HashMap<Integer, Integer> grpCount = new HashMap<Integer, Integer>();

    public PoiSpout(String poiFileName, ZCurvePartitioner zCurve) {
        this.poiFileName = poiFileName;
        this.zCurve = zCurve;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("POI", "grpId"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        collector = spoutOutputCollector;

    }

    @Override
    public void nextTuple() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(poiFileName));
            // skip header line
            String line;
            br.readLine();
            POI poi;
            int groupId;
            while ((line = br.readLine()) != null) {
                try {
                    poi = new POI(line);
                    groupId = zCurve.overlapPartition(poi.getLat(), poi.getLng());
                    collector.emit(new Values(poi, groupId));
                } catch (Exception e) {
                    continue;
                }
            }
            System.out.println("all tweets are output!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Utils.sleep(100);
    }

    @Override
    public void close() {
        for (Map.Entry<Integer, Integer> entry : grpCount.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
