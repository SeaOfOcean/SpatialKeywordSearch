package structure;

import util.DateUtil;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class Tweet implements Serializable {
    DateTime dateTime;
    Double lat;
    Double lng;
    String text;

    public Tweet() {

    }

    public Tweet(String line) {
        try {
            String[] items = line.split("\t");
            dateTime = DateUtil.formatter.parseDateTime(items[0]);
            text = items[1];
            lat = Double.parseDouble(items[2]);
            lng = Double.parseDouble(items[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return dateTime + "\t" + text + "\t"
                + lat + "\t" + lng;
    }

    public boolean isNull() {
        return dateTime == null || lat == null || lng == null || text == null;
    }
}
