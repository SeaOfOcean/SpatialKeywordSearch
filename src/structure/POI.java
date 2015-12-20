package structure;

import util.Tokenizer;

import java.io.Serializable;

/**
 * Created by Xianyan Jia on 24/10/2015.
 */
public class POI implements Serializable {
    /**
     * the first is the id, followed by a list of keywords
     */
    String id;
    String text;
    public double lat;
    public double lng;

    public POI(String line) {
        String[] items = line.split("\t");
        id = items[0];
        String[] locItems = items[1].split(",");
        lat = Double.parseDouble(locItems[0]);
        lng = Double.parseDouble(locItems[1]);
        text = getKeywordsEnglish(items[2], items[3]);
    }

    private String getKeywordsEnglish(String text, String type) {
        StringBuilder sb = new StringBuilder();
        String[] items = Tokenizer.tokenize(text);
        for (String s : items) {
            if (s.trim().length() > 1) {
                sb.append(s + " ");
            }
        }
        items = Tokenizer.tokenize(type);
        for (String s : items) {
            if (s.trim().length() > 1) {
                sb.append(s + " ");
            }
        }
        if (sb.length() > 0) {
            return sb.toString().trim();
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String[] getKeywords() {
        return text.split("\\s+");
    }

    @Override
    public String toString() {
        return "POI{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
