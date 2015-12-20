package spout;

import index.partition.ZCurvePartitioner;
import topology.Global;
import structure.Tweet;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

/**
 * monitor tweet streams from file
 * Created by Xianyan Jia on 26/10/2015.
 */
public class TweetFileSpout extends BaseRichSpout {
    SpoutOutputCollector collector;
    String fileName;
    ZCurvePartitioner zCurve;
    FileInputStream fis;
    InputStreamReader isr;
    BufferedReader br;
    int count;
    private boolean completed = false;

    public TweetFileSpout(String tweetFileName, ZCurvePartitioner zCurve) {
        this.fileName = tweetFileName;
        this.zCurve = zCurve;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("tweet", "twitter_grpId"));
    }


    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        collector = spoutOutputCollector;
        try {
            this.fis = new FileInputStream(Global.TWEET_FILE);
            this.isr = new InputStreamReader(fis, "UTF-8");
            this.br = new BufferedReader(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (br != null) {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ack(Object msgId) {
        System.out.println("######## FileReaderSpout.ack: msgId=" + msgId);
    }

    public void fail(Object msgId) {
        System.out.println("######## FileReaderSpout.fail: msgId=" + msgId);
    }

    @Override
    public void nextTuple() {
        if (completed) {
            Utils.sleep(1000);
        } else {
            String str;
            try {
                while ((str = br.readLine()) != null) {
                    if ((count > Global.N_TWEETS)) {
                        if (!completed) {
                            this.collector.emit(new Values(new Tweet(), -1));
                            completed = true;
                            close();
                        }
                        Utils.sleep(1000);
                        return;
                    } else {
                        Tweet tweet;
                        tweet = new Tweet(str);
                        if (!tweet.isNull()) {
                            Set<Integer> matchedGrpIds = zCurve.overlapPartitions(tweet.getLat(), tweet.getLng(), Global.RADIUS);
                            for (int groupId : matchedGrpIds) {
                                this.collector.emit(new Values(tweet, groupId));
                            }
                            count++;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                completed = true;
            }
        }
    }
}
