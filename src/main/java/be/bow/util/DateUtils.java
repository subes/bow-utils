package be.bow.util;

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

}
