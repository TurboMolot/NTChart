/*
 * Copyright 2016 - 2017 Neurotech MRC. http://neurotech.ru/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.turbomolot.ntchart.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by TurboMolot on 18.04.2017.
 */

public final class ConverterUtil {
    private static DateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
    private static DateFormat formatDate = SimpleDateFormat.getDateInstance();
    private static DateFormat formatTime = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);//SimpleDateFormat.getTimeInstance();
    private static DateFormat formatDateTime = SimpleDateFormat.getDateTimeInstance();
    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private final static Calendar minDate;

    static {
        minDate = Calendar.getInstance();
        minDate.clear();
        minDate.set(1900, 1, 1);
    }

    private ConverterUtil() {
    }

    public static String formatDate(Date date) {
        return (date == null) ? "" : formatDate.format(date);
    }

    public static String formatDateTime(Date date) {
        return (date == null) ? "" : formatDateTime.format(date);
    }

    public static Date formatDate(String date) {
        Date res = null;
        try {
            res = (TextUtils.isEmpty(date)) ? null : formatDate.parse(date);
        } catch (ParseException e) {
            Log.e("[PARSE DATE ERROR]", e.getLocalizedMessage());
        }
        return res;
    }

    public static String formatDateISO(Date date) {
        return (date == null) ? "" : formatISO.format(date);
    }

    public static Date formatDateISO(String date) {
        Date res = null;
        try {
            res = (TextUtils.isEmpty(date)) ? null : formatISO.parse(date);
        } catch (ParseException e) {
            Log.e("[PARSE DATE ERROR]", e.getLocalizedMessage());
        }
        return res;
    }

    public static String formatDouble(double val) {
        return numberFormat.format(val);
    }

    public static String formatLong(double val) {
        return String.valueOf(Math.round(val));
    }

    public static double formatDouble(String val) {
        double res = 0;
        try {
            res = numberFormat.parse(val).doubleValue();
        } catch (ParseException e) {
            Log.e("[PARSE DOUBLE ERROR]", e.getLocalizedMessage());
        }
        return res;
    }

    public static String formTime(float second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, 0, 0, Math.round(second));
        return formatTime.format(calendar.getTime());
    }

    public static String formTimeSimple(float sec, boolean showEmptyHour) {
        return formTimeMsSimple(Math.round(sec * 1000), showEmptyHour);
    }

    public static String formTimeMsSimple(long ms, boolean showEmptyHour) {
//        long second = TimeUnit.MILLISECONDS.toSeconds(ms);
//        long minute = TimeUnit.MILLISECONDS.toMinutes(ms);
//        long hour = TimeUnit.MILLISECONDS.toHours(ms);

        long timeDifference = ms/1000;
        int hour = (int) (timeDifference / (3600));
        int minute = (int) ((timeDifference - (hour * 3600)) / 60);
        int second = (int) (timeDifference - (hour * 3600) - minute * 60);
//        long second = (ms / 1000) % 60;
//        long minute = (ms / (1000 * 60)) % 60;
//        long hour = (ms / (1000 * 60 * 60)) % 24;
        if(hour <= 0 && showEmptyHour || hour > 0)
            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hour, minute, second);
        else
            return String.format(Locale.ENGLISH, "%02d:%02d", minute, second);
    }

    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return c.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return c.getResources().getConfiguration().locale;
        }
    }

    public static String getDecimalSplitter(Context c) {
        NumberFormat nf = NumberFormat.getInstance(ConverterUtil.getCurrentLocale(c));
        String ret = ".";
        if (nf instanceof DecimalFormat) {
            DecimalFormatSymbols sym = ((DecimalFormat) nf).getDecimalFormatSymbols();
            ret = String.valueOf(sym.getDecimalSeparator());
        }
        return ret;
    }

    public static Date getMinDate() {
        return minDate.getTime();
    }

    public static int getYearsBetweenDates(Date first, Date second) {
        Calendar firstCal = Calendar.getInstance();
        Calendar secondCal = Calendar.getInstance();
        firstCal.setTime(first);
        secondCal.setTime(second);
        secondCal.add(Calendar.DAY_OF_YEAR, -firstCal.get(Calendar.DAY_OF_YEAR));
        return secondCal.get(Calendar.YEAR) - firstCal.get(Calendar.YEAR);
    }

    public static float getAge(Date birthday) {
        if(birthday == null)
            return 0;
        Calendar firstCal = Calendar.getInstance();
        Calendar secondCal = Calendar.getInstance();
        firstCal.setTime(birthday);
        secondCal.setTime(new Date());
        float currentDayInYea = secondCal.get(Calendar.DAY_OF_YEAR);
        secondCal.add(Calendar.DAY_OF_YEAR, -firstCal.get(Calendar.DAY_OF_YEAR));

        return Math.abs(secondCal.get(Calendar.YEAR) - firstCal.get(Calendar.YEAR)) +
                (currentDayInYea / secondCal.getActualMaximum(Calendar.DAY_OF_YEAR));
    }

    public static int convertDpToPixels(float dp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static int convertSpToPixels(float sp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static int convertDpToSp(float dp, Context context) {
        int sp = (int) (convertDpToPixels(dp, context) / (float) convertSpToPixels(dp, context));
        return sp;
    }

    public static long getTimeBetweenDates(Date first, Date second) {
        if(first == null || second == null)
            return 0;
        final long from = Math.min(first.getTime(), second.getTime());
        final long to = Math.max(first.getTime(), second.getTime());
        return to - from;
    }

    public static String formatTimeBetweenDates(Date first, Date second) {
        return formTimeMsSimple(getTimeBetweenDates(first, second), false);
    }

}
