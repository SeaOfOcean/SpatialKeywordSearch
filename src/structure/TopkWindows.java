package structure;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class TopkWindows implements Serializable {
    public TreeSet<StoredMsg> storedMsgs;
    int topk;

    public TopkWindows(int topk) {
        this.topk = topk;
        storedMsgs = new TreeSet<StoredMsg>();
    }

    public StoredMsg getLast() {
        return storedMsgs.last();
    }

    public void update(double currentScore, Tweet tweet) {
        TreeSet<StoredMsg> newStored = new TreeSet<StoredMsg>();
        for (StoredMsg storedMsg : storedMsgs) {
            StoredMsg newMsg = new StoredMsg(storedMsg.fade(tweet.getDateTime()), tweet.getDateTime(), storedMsg.tweetText);
            newStored.add(newMsg);
        }
        if (storedMsgs.size() < topk) {
            storedMsgs.add(new StoredMsg(currentScore, tweet.getDateTime(), tweet.getText()));
        } else if (storedMsgs.size() == topk) {
            storedMsgs.pollLast();
            storedMsgs.add(new StoredMsg(currentScore, tweet.getDateTime(), tweet.getText()));
        } else {
            System.err.println("it should not be larger than topk?");
        }
    }

    public void add(StoredMsg storedMsg) {
        if (storedMsgs.size() < topk) {
            storedMsgs.add(storedMsg);
        }
    }

    public boolean isFull() {
        return storedMsgs.size() == topk ? true : false;
    }
}
