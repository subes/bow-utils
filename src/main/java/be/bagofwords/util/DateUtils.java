package be.bagofwords.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static Date createDate(int year, int month, int day) {
        Date date = new Date();
        date = DateUtils.truncate(date, Calendar.HOUR);
        date = DateUtils.setYears(date, year);
        date = DateUtils.setMonths(date, month - 1);
        date = DateUtils.setDays(date, day);
        return date;
    }

    public static Date createDate(int year, int month, int day, int hours, int minutes, int seconds) {
        Date date = createDate(year, month, day);
        date = DateUtils.setHours(date, hours);
        date = DateUtils.setMinutes(date, minutes);
        date = DateUtils.setSeconds(date, seconds);
        return date;
    }

    public static Date createDate(String date) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, "yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date " + date, e);
        }
    }

}
