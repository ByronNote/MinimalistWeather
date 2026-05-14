package com.byronzhang.android.library.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/12/12
 */
public final class DateConvertUtils {

    public static final String DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String DATA_FORMAT_PATTEN_YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 将时间转换为时间戳
     *
     * @param data             待转换的日期
     * @param dataFormatPatten 待转换日期格式
     */
    public static long dateToTimeStamp(String data, String dataFormatPatten) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataFormatPatten, Locale.CHINA);
        Date date = null;
        try {
            date = simpleDateFormat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }

    /**
     * 将时间戳转换为日期
     *
     * @param time             待转换的时间戳
     * @param dataFormatPatten 转换出的日期格式
     */
    public static String timeStampToDate(long time, String dataFormatPatten) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataFormatPatten, Locale.CHINA);
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    /**
     * 日期转星期
     *
     * @param dateString 日期
     * @return 周一 周二 周三 ...
     */
    public static String convertDataToWeek(String dateString) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATA_FORMAT_PATTEN_YYYY_MM_DD, Locale.CHINA);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (isNow(date))
            return "今天";

        String[] weekDaysName = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekDaysName[intWeek];
    }

    /**
     * 日期转换
     *
     * @return 08.07
     */
    public static String convertDataToString(String dateString) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATA_FORMAT_PATTEN_YYYY_MM_DD, Locale.CHINA);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null)
            return "";
        return (String.valueOf(date.getMonth()).length() == 1 ? "0" + date.getMonth() : String.valueOf(date.getMonth()))
                + "." + (String.valueOf(date.getDay()).length() == 1 ? "0" + date.getDay() : String.valueOf(date.getDay()));
    }

    /**
     * 判断时间是不是今天
     *
     * @return 是返回true，不是返回false
     */
    private static boolean isNow(Date date) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATA_FORMAT_PATTEN_YYYY_MM_DD, Locale.CHINA);
        //获取今天的日期
        String nowDay = simpleDateFormat.format(now);
        //对比的时间
        String day = simpleDateFormat.format(date);
        return day.equals(nowDay);
    }

}
