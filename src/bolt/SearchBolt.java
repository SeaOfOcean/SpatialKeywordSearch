package bolt;

import index.QueryIndexer;
import index.quadtree.QuadTree;
import index.quadtree.QuadTreeLeaf;
import index.quadtree.QuadTreeNode;
import index.quadtree.QuadTreeRect;
import structure.GeoLocation;
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
import backtype.storm.utils.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * search the POI index to find the overlapping POIs
 * if it receive the poi, it will update the poi index
 * Created by Xianyan Jia on 26/10/2015.
 */
public class SearchBolt extends BaseRichBolt {
    private OutputCollector _collector;
    QuadTree _poiIndex;
    int _taskId;
    Long _startTime;
    BufferedWriter _newTweetWriter;
    boolean _isComplete = false;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
        this._taskId = topologyContext.getThisTaskIndex();
        loadNewTweetWriter();
    }

    public void loadNewTweetWriter() {
        if (Global.IS_SAVE_RESULT_FILE) {
            try {
                if (!Global.IS_TWEET_FROM_FILE) {
                    _newTweetWriter = new BufferedWriter(new FileWriter(String.format(Global.NEW_TWEET_FILE_FORMAT, _taskId)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("poi", "tweet", "grp_id"));
    }

    /**
     * if the tuple contains tweet, then search the candidate pois for that tweet
     * if the tuple contains poi, then insert that poi into the index
     *
     * @param tuple
     */
    @Override
    public void execute(Tuple tuple) {
        if (tuple.contains("tweet")) {
            searchPOIsForTweet(tuple);
        } else if (tuple.contains("poi")) {
            POI pt = (POI) tuple.getValueByField("POI");
            _poiIndex.put(pt.getLng(), pt.getLat(), pt);
        }
    }

    public void searchPOIsForTweet(Tuple tuple) {
        if (_isComplete) {
            Utils.sleep(1000);
            return;
        }
        int grpId = tuple.getIntegerByField("twitter_grpId");
        Tweet tweet = (Tweet) tuple.getValueByField("tweet");
        // end of the file
        if (grpId == -1) {
            _collector.emit(new Values(_startTime, tweet, grpId));
            _isComplete = true;
        } else {
            processTuple(grpId, tweet);
        }
    }

    public void processTuple(int grpId, Tweet tweet) {
        if (grpId >= Global.N_PARTITION) {
            grpId = Global.N_PARTITION - 1;
        }
        if (_poiIndex == null) {
            _poiIndex = QueryIndexer.loadQuadTree_partition(grpId, Global.N_PARTITION);
            if (_startTime == null) {
                _startTime = System.nanoTime();
            }
        }
        GeoLocation geoLocation = GeoLocation.fromDegrees(tweet.getLat(), tweet.getLng());
        GeoLocation[] boundaries = geoLocation.boundingCoordinates(Global.RADIUS);
        QuadTreeNode top = _poiIndex.top;
        QuadTreeRect rect = new QuadTreeRect(boundaries[0].getLatitudeInDegrees(),
                boundaries[0].getLongitudeInDegrees(),
                boundaries[1].getLatitudeInDegrees(),
                boundaries[1].getLongitudeInDegrees());
        get(top, rect, tweet, grpId);
    }


    public void get(QuadTreeNode<POI> top, QuadTreeRect rect, Tweet tweet, Integer grp_id) {
        if (top.children == null) {
            for (QuadTreeLeaf qtl : top.items) {
                if (rect.pointWithinBounds(qtl.latitude, qtl.longitude)) {
                    _collector.emit(new Values(qtl.object, tweet, grp_id));
                }
            }
        } else {
            for (QuadTreeNode<POI> child : top.children) {
                if (child.bounds.within(rect)) {
                    get(child, rect, tweet, grp_id);
                }
            }
        }
    }


    @Override
    public void cleanup() {
        super.cleanup();
        try {
            if (_newTweetWriter != null) {
                _newTweetWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
