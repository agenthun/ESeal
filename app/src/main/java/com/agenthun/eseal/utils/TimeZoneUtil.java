package com.agenthun.eseal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/4/28 22:43.
 */

public class TimeZoneUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * UTC时间转本地时间
     */
    public static String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }

    /**
     * 本地时间转UTC时间
     */
    public static String local2Utc(String localTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        Date localDate = null;
        try {
            localDate = localFormater.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTime(localDate);
        localCalendar.add(Calendar.MILLISECOND, -(localCalendar.get(Calendar.ZONE_OFFSET) + localCalendar.get(Calendar.DST_OFFSET)));

        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        String utcTime = utcFormater.format(localCalendar.getTime());
        return utcTime;
    }

    /**
     * 获取当前本地时间转UTC时间
     */
    public static String getLocal2UtcTime() {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.add(Calendar.MILLISECOND, -(localCalendar.get(Calendar.ZONE_OFFSET) + localCalendar.get(Calendar.DST_OFFSET)));
        String utcTime = DATE_FORMAT.format(localCalendar.getTime());
        return utcTime;
    }
}
