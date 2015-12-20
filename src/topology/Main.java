package topology;

import index.partition.ZCurvePartitioner;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import bolt.CheckUpdateBolt;
import bolt.SearchBolt;
import bolt.TweetIndexBolt;
import spout.PoiSpout;
import spout.TweetFileSpout;
import spout.TwitterApiSpout;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class Main extends Global {
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.INFO);

        /**************** SETUP ****************/
        String remoteClusterTopologyName = null;
        if (args != null) {
            if (args.length == 1) {
                remoteClusterTopologyName = args[0];
            }
        }
        TopologyBuilder builder = new TopologyBuilder();
        ZCurvePartitioner zCurvePartitioner = ZCurvePartitioner.createZcurveP();

        /**************** set poi spout ****************/
        if (ACTIVE_POI_SPOUT) {
            builder.setSpout("poi_spout", new PoiSpout(POI_FILE, zCurvePartitioner), N_POI_SPOUT_PARALLELISM);
        }

        /**************** set tweet spout ****************/
        if (IS_TWEET_FROM_FILE) {
            builder.setSpout("twitter_spout", new TweetFileSpout(TWEET_FILE, zCurvePartitioner), N_TWEET_SPOUT_PARALLELISM);
        } else {
            builder.setSpout("twitter_spout", new TwitterApiSpout(zCurvePartitioner), N_TWEET_SPOUT_PARALLELISM);
        }

        /**************** set search bolt ****************/
        builder.setBolt("searcher", new SearchBolt(), N_PARTITION).setNumTasks(N_PARTITION)
                .customGrouping("twitter_spout", new PartitionGrouping());

        /**************** set tweet index bolt ****************/
        if (Global.ACTIVE_TWEET_INDEX_BOLT) {
            builder.setBolt("i3Index", new TweetIndexBolt())
                    .shuffleGrouping("twitter_spout")
                    .shuffleGrouping("poi_spout");
        }

        builder.setBolt("checkUpdate", new CheckUpdateBolt(), N_PARTITION)
                .setNumTasks(N_PARTITION)
                .customGrouping("twitter_spout", new PartitionGrouping())
                .customGrouping("i3Index", new PartitionGrouping());

        Config conf = new Config();
        conf.setDebug(Global.ACTIVE_DEBUG);


        if (remoteClusterTopologyName != null) {
            conf.setNumWorkers(N_WORKER);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

        } else {
            conf.setMaxTaskParallelism(MAX_TASK_PARALLELISM);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("keywordsearch", conf, builder.createTopology());
            Utils.sleep(10000);
            cluster.shutdown();
        }
    }

}
