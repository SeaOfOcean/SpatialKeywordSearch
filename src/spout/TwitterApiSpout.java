package spout;

import index.partition.ZCurvePartitioner;
import topology.Global;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * tweet streams from Twitter API
 * Created by Xianyan Jia on 26/10/2015.
 */
public class TwitterApiSpout extends BaseRichSpout {

    public static final String MESSAGE = "message";
    private final String _accessTokenSecret;
    private final String _accessToken;
    private final String _consumerSecret;
    private final String _consumerKey;
    private final ZCurvePartitioner _zCurve;
    private SpoutOutputCollector _collector;
    private TwitterStream _twitterStream;
    private LinkedBlockingQueue _msgs;
    private FilterQuery _tweetFilterQuery;

    /**
     * @param consumerKey
     * @param consumerSecret
     * @param accessToken
     * @param accessTokenSecret
     */
    public TwitterApiSpout(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret
            , ZCurvePartitioner zCurvePartitioner) {
        if (consumerKey == null ||
                consumerSecret == null ||
                accessToken == null ||
                accessTokenSecret == null) {
            throw new RuntimeException("Twitter4j OAuth field cannot be null");
        }
        _zCurve = zCurvePartitioner;
        _consumerKey = consumerKey;
        _consumerSecret = consumerSecret;
        _accessToken = accessToken;
        _accessTokenSecret = accessTokenSecret;

    }

    public TwitterApiSpout(ZCurvePartitioner zCurvePartitioner) {
        _accessToken = Global.TWITTER_ACCESS_TOKEN;
        _accessTokenSecret = Global.TWITTER_ACCESS_SECRET;
        _consumerKey = Global.CONSUMER_KEY;
        _consumerSecret = Global.CONSUMER_SECRET;
        _zCurve = zCurvePartitioner;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(MESSAGE, "twitter_grpId"));
    }

    /**
     * Creates a twitter stream listener which adds messages to a LinkedBlockingQueue. Starts to listen to streams
     */
    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        _msgs = new LinkedBlockingQueue();
        _collector = spoutOutputCollector;
        ConfigurationBuilder _configurationBuilder = new ConfigurationBuilder();
        _configurationBuilder.setOAuthConsumerKey(_consumerKey)
                .setOAuthConsumerSecret(_consumerSecret)
                .setOAuthAccessToken(_accessToken)
                .setOAuthAccessTokenSecret(_accessTokenSecret);
        _twitterStream = new TwitterStreamFactory(_configurationBuilder.build()).getInstance();
        _twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if (meetsConditions(status))
                    _msgs.offer(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            }

            @Override
            public void onStallWarning(StallWarning warning) {
            }

            @Override
            public void onException(Exception ex) {
            }
        });
        if (_tweetFilterQuery == null) {
            _twitterStream.sample();
        } else {
            _twitterStream.filter(_tweetFilterQuery);
        }


    }

    private boolean meetsConditions(Status status) {
        return true;
    }

    /**
     * When requested for next tuple, reads message from queue and emits the message.
     */
    @Override
    public void nextTuple() {
        // emit tweets
        Object s = _msgs.poll();
        if (s == null) {
            Utils.sleep(1000);
        } else {
            Status status = (Status) s;
            if (((Status) s).getLang().equals("en")) {
                if (status.getGeoLocation() != null) {
                    int groupId = _zCurve.overlapPartition(status.getGeoLocation().getLatitude(), status.getGeoLocation().getLatitude());
                    _collector.emit(new Values(s, groupId));
                }
            }


        }
    }

    @Override
    public void close() {
        _twitterStream.shutdown();
        super.close();
    }

}
