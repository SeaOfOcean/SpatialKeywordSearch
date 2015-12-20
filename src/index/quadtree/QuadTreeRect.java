package index.quadtree;

import java.io.Serializable;

/**
 * Created by Xianyan Jia on 25/10/2015.
 */
public class QuadTreeRect implements Serializable {

    static final long serialVersionUID = -5585535433679092922L;

    /**
     * x1<x2, y1<y2
     */
    public double y2;
    public double y1;
    public double x1;
    public double x2;

    /**
     * @param n y2
     * @param w x1
     * @param s y1
     * @param e x2
     */
    public QuadTreeRect(double n, double w, double s, double e) {
        y2 = n;
        x1 = w;
        y1 = s;
        x2 = e;
        double temp;
        if (x1 > x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
    }

    public QuadTreeRect() {
        x1 = -90;
        x2 = 90;
        y1 = -90;
        y2 = 90;
    }

    public boolean within(QuadTreeRect rect) {
        return within(rect.x1, rect.x2, rect.y1, rect.y2);
    }

    public boolean within(double x1, double x2, double y1, double y2) {
        if (y2 >= this.y2) {
            return false;
        }
        if (y1 < this.y1) {
            return false;
        }
        if (x2 > this.x2) {
            return false;
        }
        if (x1 <= this.x1) {
            return false;
        }
        return true;
    }

    public boolean pointWithinBounds(double lat, double lon) {
        return (lon >= x1 && lon < x2 && lat <= y2 && lat > y1);
    }

    public double borderDistanceSqr(double lat, double lon) {
        double nsdistance;
        double ewdistance;

        if (y1 <= lat && lat <= y2) {
            nsdistance = 0.0;
        } else {
            nsdistance = Math.min((Math.abs(lat - y2)), (Math.abs(lat - y1)));
        }

        if (x1 <= lon && lon <= x2) {
            ewdistance = 0.0;
        } else {
            ewdistance = Math.min((Math.abs(lon - x2)), (Math.abs(lon - x1)));
        }

        if (nsdistance == 0.0 && ewdistance == 0.0) // save computing 0 distance
            return 0.0;

        double dx = ewdistance * ewdistance;
        double dy = nsdistance * nsdistance;
        return dx * dx + dy * dy;
    }

    public double getWidth() {
        return Math.abs(x2 - x1);
    }

    public double getHeight() {

        return Math.abs(y2 - y1);
    }

    public boolean overlap(QuadTreeRect rect) {
        return overlap(rect.x1, rect.x2, rect.y1, rect.y2);
    }

    public boolean overlap(double x1, double x2, double y1, double y2) {
        if (y2 <= this.y2) {
            return true;
        }
        if (y1 >= this.y1) {
            return true;
        }
        if (x2 <= this.x2) {
            return true;
        }
        if (x1 >= this.x1) {
            return true;
        }
        return false;
    }
}