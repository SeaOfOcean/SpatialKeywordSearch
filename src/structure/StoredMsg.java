package structure;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class StoredMsg implements Comparable<StoredMsg>, Serializable {
    double score;
    DateTime storeTime;
    String tweetText;

    public StoredMsg(double score, DateTime storeTime, String tweetText) {
        this.score = score;
        this.storeTime = storeTime;
        this.tweetText = tweetText;
    }

    public double fade(DateTime now) {
        return Math.pow(score, -(now.getMillis() - storeTime.getMillis()));
    }

    @Override
    public int compareTo(StoredMsg o) {
        return Double.compare(o.score, score);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public DateTime getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(DateTime storeTime) {
        this.storeTime = storeTime;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }
}
