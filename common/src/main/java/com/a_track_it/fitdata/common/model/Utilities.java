package com.a_track_it.fitdata.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.a_track_it.fitdata.common.Constants;
import com.google.android.gms.fitness.data.Field;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Utilities {

    /**
     * Lightens a_track_it.com color by a_track_it.com given factor.
     *
     * @param color
     *            The color to lighten
     * @param factor
     *            The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     *            color white.
     * @return lighter version of the specified color.
     */
    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    private static final String DAY_FORMAT = "dd/MM";

    private static final String DATE_FORMAT = "dd/MM/yyy";

    private static final String TIME_FORMAT = "h:mm";
    public static String getDayString(Long ms) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAY_FORMAT, Locale.getDefault());
        return dateFormat.format(ms);
    }

    public static String getDateString(Long ms) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(ms);
    }

    public static String getTimeString(Long ms) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return dateFormat.format(ms);
    }

    public static String getTimeDateString(Long ms) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIME_DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(ms);
    }
    public static String getPartOfDayString(Long ms){
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ms);
        int HourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        if ((HourOfDay >= 0) && (HourOfDay < 4)) result = "Late Night";
        if ((HourOfDay >= 4) && (HourOfDay <= 7)) result = "Early Morning";
        if ((HourOfDay > 7) && (HourOfDay <= 11)) result = "Morning";
        if ((HourOfDay > 11) && (HourOfDay <= 14)) result = "Lunch Time";
        if ((HourOfDay > 14) && (HourOfDay <= 16)) result = "Afternoon";
        if ((HourOfDay > 16) && (HourOfDay < 18)) result = "Late Afternoon";
        if ((HourOfDay >= 18) && (HourOfDay < 21)) result = "Early Evening";
        if ((HourOfDay >= 21) && (HourOfDay <= 22)) result = "Evening";
        if ((HourOfDay > 22) && (HourOfDay <= 24)) result = "Night";
        return result;
    }

    public enum TimeFrame {
        BEGINNING_OF_DAY,
        BEGINNING_OF_WEEK,
        BEGINNING_OF_MONTH,
        LAST_WEEK,
        LAST_MONTH,
        BEGINNING_OF_YEAR,
        THIRTY_DAYS,
        NINETY_DAYS,
        ALL_TIME;
        private static TimeFrame[] vals = TimeFrame.values();
        public TimeFrame next()
        {
            // Hide last month for now. It takes too long.
            return vals[(this.ordinal()+1) % (vals.length - 1)];
        }
    }

    public static String getTimeFrameText(TimeFrame timeFrame) {
        String result = "";
        switch (timeFrame) {
            case BEGINNING_OF_DAY: // 1 day
                result = "Today";
                break;
            case BEGINNING_OF_WEEK: // 1 week
                result = "This Week";
                break;
            case BEGINNING_OF_MONTH: // 1 month
                result = "This Month";
                break;
            case LAST_WEEK:
                result = "Last Week";
                break;
            case LAST_MONTH: // Last month
                result = "Last Month";
                break;
            case BEGINNING_OF_YEAR: // Year to date
                result = "This Year";
                break;
            case ALL_TIME: // Year to date
                result = "All Time";
                break;
        }
        return result;
    }

    public static long getTimeFrameEnd(TimeFrame timeFrame) {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        switch (timeFrame) {
            case BEGINNING_OF_DAY: // 1 day
            case BEGINNING_OF_WEEK: // 1 week
            case BEGINNING_OF_MONTH: // 1 month
            case BEGINNING_OF_YEAR: // 1 year
            case LAST_WEEK:
            case ALL_TIME: // All time
                break;
            case LAST_MONTH: // 1 month ago
                cal.set(Calendar.DAY_OF_MONTH, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
        }
        return cal.getTimeInMillis();
    }
    /**
     * Convert a_track_it.com millisecond duration to a_track_it.com string format
     *
     * @param millis A duration to convert to a_track_it.com string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if(days > 0) {
            sb.append(days);
            sb.append(" days ");
        }
        if(hours > 0) {
            sb.append(hours);
            sb.append(" hrs ");
        }
        sb.append(minutes);
        sb.append(" min ");

        sb.append(seconds);
        sb.append(" sec");
        return(sb.toString());
    }

    public static long getTimeFrameStart(TimeFrame timeFrame) {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        switch (timeFrame) {
            case BEGINNING_OF_DAY: // 1 day
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case BEGINNING_OF_WEEK: // 1 week
                cal.set(Calendar.DAY_OF_WEEK, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case BEGINNING_OF_MONTH: // 1 month
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                //cal.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case LAST_WEEK: // 1 month ago
                cal.set(Calendar.DAY_OF_WEEK, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case LAST_MONTH: // 1 month ago
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.MONTH, -1);
                break;
            case BEGINNING_OF_YEAR: // This year
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case THIRTY_DAYS: // 30 days
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case NINETY_DAYS: // 30 days
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_YEAR, -90);
                break;
            case ALL_TIME: // All time
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.YEAR, 0);
                break;
        }
        return cal.getTimeInMillis();
    }

    public static String getResistanceType(int rt){
        String sRet = "N/A";
        switch (rt){
            case Field.RESISTANCE_TYPE_BARBELL:
                sRet ="Barbell";
                break;
            case Field.RESISTANCE_TYPE_BODY:
                sRet ="Bodyweight";
                break;
            case Field.RESISTANCE_TYPE_CABLE:
                sRet ="Cable";
                break;
            case Field.RESISTANCE_TYPE_DUMBBELL:
                sRet ="Dumbell";
                break;
            case Field.RESISTANCE_TYPE_KETTLEBELL:
                sRet ="Kettlebell";
                break;
            case Field.RESISTANCE_TYPE_MACHINE:
                sRet ="Machine";
                break;
            case Field.RESISTANCE_TYPE_UNKNOWN:
                sRet ="N/A";
                break;
        }
        return sRet;
    }
    public static String getBodypartType(int bpt){
        String sRet = "N/A";
        switch (bpt){
            case 1:
                sRet ="Torso";
                break;
            case 2:
                sRet ="Legs";
                break;
            case 3:
                sRet ="Shoulders";
                break;
            case 4:
                sRet ="Arms";
                break;
            case 5:
                sRet = "Core";
                break;
        }
        return sRet;
    }
    public static boolean isGymWorkout(int id){
        boolean result = false;
        switch (id) {
            case Constants.WORKOUT_TYPE_STRENGTH:
            case 97:    // weight-lifting
            case 22:     // CIRCUIT TRAINING
            case 113:  // crossfit
            case 114:  //HIIT
            case 115: //interval training
            case 41: //kettle bell
                result = true;
                break;
        }
        return result;
    }
    public static boolean isShooting(int id){
        boolean result = false;
        switch (id) {
            case Constants.WORKOUT_TYPE_ARCHERY:
            case 13:    // BIATHALON
            case 122:   // gun shooting ?
                result = true;
                break;
        }
        return result;
    }
    public static boolean isActiveWorkout(int id) {
        boolean result = true;
        switch (id) {
            case 0:
            case 3:
            case 4:
            case 72:
                result = false;
                break;
        }
        return result;
    }
    public static void saveBitmapFile(Context context, Bitmap b, String picName) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("file", "file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("file", "io exception");
            e.printStackTrace();
        }
    }
}

