package util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class DateUtil {
    public static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String currentTime() {
        Calendar cal = Calendar.getInstance();
        return formatter.print(cal.getTimeInMillis());
    }
}
