package be.bagofwords.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        Date result = calendar.getTime();
        return DateUtils.truncate(result, Calendar.DATE);
    }

    public static Date createDate(int year, int month, int day, int hours, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, day);
        calendar.set(Calendar.MINUTE, day);
        calendar.set(Calendar.SECOND, day);
        Date result = calendar.getTime();
        return DateUtils.truncate(result, Calendar.SECOND);
    }

    public static Date createDate(String date) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, "yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date " + date, e);
        }
    }

}
