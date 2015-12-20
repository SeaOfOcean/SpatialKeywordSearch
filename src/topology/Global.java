package topology;

import util.Property;

/**
 * Created by Xianyan Jia on 25/10/2015.
 */
public class Global {

    //############################ file path ############################
    public static final String TWEET_FILE = Property.getResource("tweetFile");
    public static final String POI_FILE = Property.getResource("poiFile");
    public static final String i3IndexSaveFolder = Property.getResource("i3IndexSaveFolder");
    public static final String TIME_FILE = Property.getResource("run_time");

    //############################ Twitter API tokens ############################
    public static final String TWITTER_ACCESS_TOKEN = Property.getProperty("access_token");
    public static final String TWITTER_ACCESS_SECRET = Property.getProperty("access_secret");
    public static final String CONSUMER_KEY = Property.getProperty("api_key");
    public static final String CONSUMER_SECRET = Property.getProperty("api_secret");

    //############################ storm parameter ############################
    public static final boolean ACTIVE_DEBUG = Property.getBooleanProperty("is_debug");
    public static final int N_POI_SPOUT_PARALLELISM = Property.getIntProperty("n_poi_spout_parallelism");
    public static final int N_TWEET_SPOUT_PARALLELISM = Property.getIntProperty("n_tweet_spout_parallelism");
    public static final int N_PARTITION = Property.getIntProperty("numZpartition");
    public static final int N_WORKER = Property.getIntProperty("num_worker");
    public static final int MAX_TASK_PARALLELISM = Property.getIntProperty("MaxTaskParallelism");

    //############################ keyword search parameter ############################
    public static final Double ALPHA = Property.getDoubleProperty("scorePar");
    public static final boolean ACTIVE_POI_SPOUT = Property.getBooleanProperty("active_poi_spout");
    public static final boolean ACTIVE_TWEET_INDEX_BOLT = Property.getBooleanProperty("active_tweet_index_bolt");
    public static final double MAX_DISTANCE = 200;
    public static final int MAX_ITEMS = Property.getIntProperty("quad_tree_max_items");
    public static final float DEFAULT_MIN_SIZE = Property.getFloatProperty("quad_tree_default_min_size");
    //true if tweet read from file, otherwise get tweets from tweet api
    public static final boolean IS_TWEET_FROM_FILE = Property.getBooleanProperty("tweetFromFile");
    public static final Double RADIUS = Double.parseDouble(Property.getProperty("radius"));
    public static final boolean isSavePOITree = Boolean.parseBoolean(Property.getProperty("isSavePOITree"));
    public static final int K = Property.getIntProperty("topk");
    public static String POI_INDEX_FILE_FORMAT = Property.getResource("quadtreeFile");
    public static String TOPK_SAVE_FILE_FORMAT = Property.getResource("result_file_format");
    public static String UPDATE_RESULT_FILE_FORMAT = Property.getResource("update_result_file_format");
    public static String NEW_TWEET_FILE_FORMAT = Property.getResource("new_tweet_file_format");
    public static Long N_TWEETS = Long.parseLong(Property.getProperty("n_tweets"));
    public static boolean IS_SAVE_RESULT_FILE = Property.getBooleanProperty("isSaveResult2File");
}
