package bolt;

import index.i3.QueryProcessing;
import util.*;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.Utils;
import org.joda.time.DateTime;
import structure.POI;
import structure.Tweet;
import topology.Global;
import structure.StoredMsg;
import structure.TopkWindows;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Xianyan Jia on 1/11/2015.
 */
public class CheckUpdateBolt extends BaseRichBolt {
    HashMap<String, TopkWindows> storedMsgWindows;
    BufferedWriter resultWriter;
    Long endTime;
    int task_id;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.task_id = context.getThisTaskIndex();
        loadStoredMsgWindows();
        loadResultWriter();
    }

    @Override
    public void execute(Tuple tuple) {
        if (tuple.contains("i3result")) {
            POI poi = (POI) tuple.getValueByField("poi");
            TopkWindows storedMsgWindow
                    = storedMsgWindows.get(poi.getId());
            Vector<QueryProcessing.ResultTuple> results
                    = (Vector<QueryProcessing.ResultTuple>) tuple.getValueByField("i3result");
            for (QueryProcessing.ResultTuple resultTuple : results) {
                StoredMsg storedMsg = new StoredMsg(resultTuple.score, DateTime.now().minusYears(10), "");
                storedMsgWindow.add(storedMsg);
            }
        } else if (tuple.contains("tweet")) {
            int grp_id = (int) tuple.getValueByField("grp_id");
            //last tuple
            if (grp_id == -1) {
                Long startTime = (Long) tuple.getValueByField("poi");
                processLastTuple(startTime);
            } else {
                POI poi = (POI) tuple.getValueByField("poi");
                Tweet tweet = (Tweet) tuple.getValueByField("tweet");
                checkAndUpdate(poi, tweet);
            }

        }
    }

    @Override
    public void cleanup() {
        if (resultWriter != null) {
            try {
                resultWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (Global.IS_SAVE_RESULT_FILE) {
            GsonUtil.save(storedMsgWindows, String.format(Global.TOPK_SAVE_FILE_FORMAT, task_id));
        }

    }

    public static double calculateDistance(double long1, double long2, double lat1, double lat2) {
        double distance;
        double radius = 3958.82; //earth's radius in miles
        double diffLat = Math.toRadians(lat2 - lat1);
        double diffLong = Math.toRadians(long2 - long1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double arg1 = Math.pow(Math.sin(diffLat / 2), 2);
        double arg2 = Math.cos(rLat1) * Math.cos(rLat2) * Math.pow(Math.sin(diffLong / 2), 2);
        distance = 2 * radius * Math.asin(Math.sqrt(arg1 + arg2));
        return distance;
    }

    private void loadStoredMsgWindows() {
        storedMsgWindows = new HashMap<>();
    }

    public void processLastTuple(Long startTime) {
        if (startTime == null) {
            startTime = endTime = System.nanoTime();
        } else {
            endTime = System.nanoTime();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Global.TIME_FILE, true));
            System.out.println(DateUtil.currentTime() + "\trunning time for task\t"
                    + task_id + "\t"
                    + (endTime - startTime) / 1000000 + "\tms");
            writer.write(DateUtil.currentTime() + "\trunning time for task\t"
                    + task_id + "\t"
                    + (endTime - startTime) / 1000000 + "\tms");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cleanup();
        Utils.sleep(1000);
    }

    public void loadResultWriter() {
        try {
            resultWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(
                            String.format(Global.UPDATE_RESULT_FILE_FORMAT, task_id))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkAndUpdate(POI poi, Tweet tweet) {
        TopkWindows storedMsgWindow = storedMsgWindows.get(poi.getId());
        String[] keywords = poi.getKeywords();
        for (String keyword : keywords) {
            if (tweet.getText().toLowerCase().indexOf(keyword.toLowerCase()) > 0) {
                double currentScore = getTotalScore(poi, tweet);
                if (storedMsgWindow == null) {
                    storedMsgWindow = new TopkWindows(Global.K);
                }
                if (storedMsgWindow.isFull()
                        && (currentScore < storedMsgWindow.getLast().fade(tweet.getDateTime()))) {
                    continue;
                } else {
                    storedMsgWindow.update(currentScore, tweet);
                    try {
                        System.out.println("task id: " + task_id + " tweet ===" + tweet.toString() + " === sent to subscriber " + poi.toString());
                        if (Global.IS_SAVE_RESULT_FILE) {
                            resultWriter.write("tweet ===" + tweet.toString() + " === sent to subscriber " + poi.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private double getTotalScore(POI poi, Tweet tweet) {
        double locScore = getLocScore(poi.getLat(), poi.getLng(), tweet.getLat(), tweet.getLng());
        double textScore = getTextScore(poi.getKeywords(), tweet.getText());
        double total = Global.ALPHA * locScore + (1 - Global.ALPHA) * textScore;
        return total;
    }

    private double getTextScore(String[] keywords, String text) {
        return TextSimilarity.getSimilarity(keywords, text);
    }

    private double getLocScore(double lat, double lng, Double lat1, Double lng1) {
        double distance = calculateDistance(lng, lng1, lat, lat1);
        return 1 - distance / Global.MAX_DISTANCE;
    }


}
