package com.a_track_it.workout.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.R;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.UserDailyTotalsWorker;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.service.DailySummaryJobIntentService;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.INTENT_DAILY;
import static com.a_track_it.workout.common.Constants.INTENT_EXER_RECOG;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.INTENT_NETWORK_CHANGED;
import static com.a_track_it.workout.common.Constants.INTENT_QUIT_APP;
import static com.a_track_it.workout.common.Constants.INTENT_RECOG;
import static com.a_track_it.workout.common.Constants.INTENT_SUMMARY_DAILY;
import static com.a_track_it.workout.common.Constants.INTENT_WIFI_CHANGED;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_RECOG;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.MAP_HEART_POINTS;
import static com.a_track_it.workout.common.Constants.MAP_MOVE_MINS;
import static com.a_track_it.workout.common.Constants.MAP_STEPS;
import static com.a_track_it.workout.common.Constants.NOTIFICATION_SUMMARY_ID;
import static com.a_track_it.workout.common.Constants.SUMMARY_CHANNEL_ID;

public class CustomIntentReceiver extends BroadcastReceiver {
    private final static String LOG_TAG = CustomIntentReceiver.class.getSimpleName();
    private static final String GOAL_PREFIX = "goal.";
    private static final int ALARM_CODE = 55347;
    public CustomIntentReceiver(){
        this.bRegistered = false;
    }
    public CustomIntentReceiver(ResultReceiver resReceiver){ this.bRegistered = false; this.resultReceiver = resReceiver;}
    public boolean isRegistered() {
        return bRegistered;
    }
    public void setRegistered(boolean bRegistered) {
        this.bRegistered = bRegistered;
    }
    private Context ctx;   /* get the application context */
    private boolean bRegistered = false;
    private ReferencesTools referencesTools;
    private ResultReceiver resultReceiver;
    private ApplicationPreferences appPrefs = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String intentAction = intent.getAction();
            String intentPath = intent.getType();
            int iType = 0;
            String toastMessage = "unknown intent action";
            ctx = context.getApplicationContext();
            referencesTools = ReferencesTools.setInstance(ctx);
            Bundle bundle = new Bundle();
            boolean isConnected = false;
            switch (intentAction){
                case Constants.INTENT_VIBRATE:
                    int count = 1;
                    if (intent.hasExtra(KEY_FIT_TYPE))
                        count = intent.getIntExtra(KEY_FIT_TYPE,1);
                    vibrate(context, (long)count);
                    break;
                case INTENT_MESSAGE_TOAST:
                    if (intent.hasExtra(Constants.INTENT_EXTRA_MSG)){
                        iType = 0;
                        if (intent.hasExtra(KEY_FIT_TYPE)) iType = intent.getIntExtra(KEY_FIT_TYPE,0);
                       // if (iType == 1) bDayMode = false;
                        int length = Toast.LENGTH_SHORT;
                        if (intent.hasExtra(KEY_FIT_VALUE)) length = intent.getIntExtra(KEY_FIT_VALUE, length);
                        String sMessage = intent.getStringExtra(Constants.INTENT_EXTRA_MSG);
                        Toast toast = Toast.makeText(ctx.getApplicationContext(), sMessage, length);
                       // toast.
                        View view = toast.getView();
                        TextView toastMessage2 = (TextView) view.findViewById(android.R.id.message);
                        if (iType == 1) {
                            view.setBackgroundResource(android.R.drawable.toast_frame);
                            view.setBackgroundColor(Color.TRANSPARENT);
                            toastMessage2.setBackground(AppCompatResources.getDrawable(ctx.getApplicationContext(),R.drawable.custom_toast));
                            toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx.getApplicationContext(), R.color.white_pressed));
                        }else {
                            if (iType == 2){
                                view.setBackgroundResource(android.R.drawable.toast_frame);
                                view.setBackgroundColor(Color.TRANSPARENT);
                                toastMessage2.setBackground(AppCompatResources.getDrawable(ctx.getApplicationContext(),R.drawable.custom_wear_toast));
                                toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx.getApplicationContext(), R.color.secondaryTextColor));
                            }else
                                toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx.getApplicationContext(), android.R.color.black));
                        }
                        toast.show();
                    }
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    if ((level != -1) && (scale != -1)) {
                        toastMessage = "battery_changed";
                        float batteryPercentage = ((float) level / (float) scale) * 100.0f;
                        if (resultReceiver != null) bundle.putInt(BatteryManager.EXTRA_LEVEL, Math.round(batteryPercentage));
                    }
                    break;
                case Intent.ACTION_SHUTDOWN :
                    toastMessage = "system shutdown";
                    Intent quitIntent = new Intent(INTENT_QUIT_APP);
                    quitIntent.setAction(INTENT_QUIT_APP);
                    quitIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    ctx.sendBroadcast(quitIntent);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (resultReceiver != null) bundle.putInt(Constants.INTENT_SCREEN, 0);
                    Log.e(LOG_TAG, "screen OFF");
                    break;
                case Intent.ACTION_SCREEN_ON:
                    if (resultReceiver != null) bundle.putInt(Constants.INTENT_SCREEN, 1);
                    Log.e(LOG_TAG, "screen ON");
                    break;
                case INTENT_WIFI_CHANGED :
                case INTENT_NETWORK_CHANGED :
                    if (intent.hasExtra(EXTRA_NO_CONNECTIVITY)) {
                        isConnected = !(intent.getBooleanExtra(EXTRA_NO_CONNECTIVITY, true));
                        toastMessage = "network changed " + isConnected;
                        if (resultReceiver != null) bundle.putBoolean(KEY_FIT_VALUE, isConnected);
                    }
                    break;
                case INTENT_DAILY:
                    Intent mIntent = new Intent(ctx, DailySummaryJobIntentService.class);
                    if (resultReceiver == null) resultReceiver = dailyReceiver;
                    mIntent.putExtra(Constants.KEY_FIT_REC, resultReceiver);
                    appPrefs = ApplicationPreferences.getPreferences(ctx);
                    long syncInterval = appPrefs.getDailySyncInterval();
                    intentPath = appPrefs.getLastUserID();
                    if (intentPath.length() > 0) {
                        mIntent.putExtra(KEY_FIT_USER, intentPath);
                        mIntent.putExtra(Constants.KEY_FIT_DEVICE_ID, appPrefs.getDeviceID());
                        if (intent.hasExtra(KEY_FIT_VALUE)) mIntent.putExtra(KEY_FIT_VALUE, (Parcelable)intent.getParcelableExtra(KEY_FIT_VALUE));
                        int iForce = intent.getIntExtra(KEY_FIT_TYPE, 0);
                        mIntent.putExtra(KEY_FIT_TYPE, iForce);
                        if (syncInterval > 0) {
                            DailySummaryJobIntentService.enqueueWork(ctx, mIntent);
                        }
                    }
                    break;
                case INTENT_EXER_RECOG :
                    Parcelable parcelable = intent.getParcelableExtra(INTENT_EXER_RECOG);
                    if (parcelable != null)
                        Log.e(LOG_TAG,"exercise recognised " + parcelable.toString());
                    intentPath = intent.getStringExtra(KEY_FIT_NAME);
                    iType = intent.getIntExtra(KEY_FIT_TYPE, 0);
                    if ((intentPath != null) && (intentPath.length() > 0))
                        toastMessage = "exercise recognised " + intentPath;
                    break;
                case INTENT_RECOG :
                    intentPath = intent.getStringExtra(KEY_FIT_NAME);
                    iType = intent.getIntExtra(KEY_FIT_TYPE, 0);
                    toastMessage = "activity detected";
                    break;
                default:
                    // an Intent broadcast.
                    throw new UnsupportedOperationException("Not yet implemented");
            }
            if (resultReceiver != null){
                bundle.putString(intentAction, toastMessage);
                if ((intentAction.equals(INTENT_EXER_RECOG) || intentAction.equals(INTENT_RECOG)) && (intentPath != null)) {
                    bundle.putString(KEY_FIT_NAME, intentPath);
                    if (intentAction.equals(INTENT_RECOG)){
                        bundle.putInt(KEY_FIT_TYPE, iType);
                        DetectedActivity detectedActivity = intent.getParcelableExtra(KEY_FIT_RECOG);
                        bundle.putParcelable(KEY_FIT_RECOG, detectedActivity);
                        Log.e(LOG_TAG, "sending bundle with INTENT_RECOG " + detectedActivity.toString());
                    }
                }
                resultReceiver.send(200, bundle);
            }
        }
    }
    ResultReceiver dailyReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.w(LOG_TAG, "CustomIntentReceiver dailyReceiver onResult " + resultCode);
            if (resultCode == 200 && (resultData != null)) {
                String sKeyWorkoutList = Workout.class.getSimpleName() + "_list";
                long timeMs = (System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(timeMs);
                if (!resultData.containsKey(Constants.KEY_FIT_USER)) return;
                String sUserID = resultData.getString(Constants.KEY_FIT_USER);
                if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(ctx);
                appPrefs.setLastDailySync(timeMs);
                UserPreferences userPrefs = UserPreferences.getPreferences(ctx,sUserID);
                if (resultData.containsKey(Constants.MAP_CALORIES) || resultData.containsKey(Constants.MAP_DISTANCE) || resultData.containsKey(MAP_MOVE_MINS)
                        || resultData.containsKey(Constants.MAP_BPM_AVG) || resultData.containsKey(MAP_STEPS) || resultData.containsKey(Constants.MAP_WATTS)
                        || resultData.containsKey(sKeyWorkoutList)) {

                    if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(resultData, sUserID);
                    // existing total  -update

                    Data.Builder builder = new Data.Builder();
                    builder.putString(KEY_FIT_USER, sUserID);
                    builder.putInt(KEY_FIT_TYPE, 0);

                    int iAdded = 0;
                    if (resultData.containsKey(Constants.MAP_MOVE_MINS)){
                        builder.putInt(Constants.MAP_MOVE_MINS, resultData.getInt(Constants.MAP_MOVE_MINS));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_STEPS)){
                        builder.putInt(Constants.MAP_STEPS, resultData.getInt(Constants.MAP_STEPS));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_DISTANCE)){
                        builder.putFloat(Constants.MAP_DISTANCE, resultData.getFloat(Constants.MAP_DISTANCE));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_HEART_POINTS)){
                        builder.putFloat(Constants.MAP_HEART_POINTS, resultData.getFloat(Constants.MAP_HEART_POINTS));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_HEART_DURATION)){
                        builder.putInt(Constants.MAP_HEART_DURATION, resultData.getInt(Constants.MAP_HEART_DURATION));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_CALORIES)){
                        builder.putFloat(Constants.MAP_CALORIES, resultData.getFloat(Constants.MAP_CALORIES));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_BPM_MIN)) {
                        builder.putFloat(Constants.MAP_BPM_MIN,resultData.getFloat(Constants.MAP_BPM_MIN));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_BPM_MAX)){
                        builder.putFloat(Constants.MAP_BPM_MAX,resultData.getFloat(Constants.MAP_BPM_MAX));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_BPM_AVG)){
                        builder.putFloat(Constants.MAP_BPM_AVG,resultData.getFloat(Constants.MAP_BPM_AVG));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_SPEED_MIN)){
                        builder.putFloat(Constants.MAP_SPEED_MIN,resultData.getFloat(Constants.MAP_SPEED_MIN));
                        iAdded++;
                    }
                    if (resultData.containsKey(Constants.MAP_SPEED_MAX)) builder.putFloat(Constants.MAP_SPEED_MAX,resultData.getFloat(Constants.MAP_SPEED_MAX));
                    if (resultData.containsKey(Constants.MAP_SPEED_AVG)) builder.putFloat(Constants.MAP_SPEED_AVG,resultData.getFloat(Constants.MAP_SPEED_AVG));
                    Log.e(LOG_TAG, "added metrics " + iAdded);

                    if (resultData.containsKey(sKeyWorkoutList)) {
                        ArrayList<Workout> list = resultData.getParcelableArrayList(sKeyWorkoutList);
                        int iSize = list.size();
                        if (iSize > 0) {
                            Gson gson = new Gson();
                            String sList = gson.toJson(list);
                            if (sList.length() > 0)
                                builder.putString(Workout.class.getSimpleName() + "_list", sList);
                        }
                    }
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(UserDailyTotalsWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    WorkManager mWorkManager  = WorkManager.getInstance(ctx);
                    if (mWorkManager != null)
                        mWorkManager.enqueue(workRequest);

                }


                long delay_sync = appPrefs.getDailySyncInterval();
                if (delay_sync > 0)
                    CustomIntentReceiver.setAlarm(ctx,false, delay_sync, sUserID,null);  // repeat
            }
        }
    };

    private void vibrate(Context context, long version) {
        Vibrator mVibrator;
        mVibrator = (Vibrator)  context.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null) return;
        try {
            if (version == 1) {
                long[] pattern = {0, 400, 200, 400};
                mVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
            if (version == 2){
                long[] pattern = {0, 800};
                mVibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (version == 3){
                long[] pattern = {0, 400};
                mVibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
            }else{
                long[] pattern = { 0, 100, 500, 100, 500};
                mVibrator.vibrate(VibrationEffect.createWaveform(pattern,VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }catch (Exception e){
            Log.d(LOG_TAG, "Vibrate error " + e.getMessage());
        }
    }

    private void sendNotification(Bundle dataBundle, String sUserID){
        if ((ctx == null) || (dataBundle == null) || (dataBundle.isEmpty())) return;

       NotificationManager mNotifyManager =
                (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
       int notificationID = NOTIFICATION_SUMMARY_ID;
       Resources resources = ctx.getResources();
       Intent notificationIntent = new Intent(INTENT_SUMMARY_DAILY);
        notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notificationIntent.setAction(INTENT_SUMMARY_DAILY);
        notificationIntent.putExtra(KEY_FIT_TYPE, 1);
        notificationIntent.putExtra(Constants.KEY_FIT_USER, sUserID);

        String sTime = SimpleDateFormat.getTimeInstance().format(new Date());
        PendingIntent notificationPendingIntent = PendingIntent.getActivity
                (ctx, notificationID, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String sTitle =  String.format(Locale.getDefault(),resources.getString(R.string.note_summary_title), sTime);
        String sContent = Constants.ATRACKIT_EMPTY; String sTemp;
        String sKey = GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName();
        try {
            int steps = dataBundle.getInt(MAP_STEPS);
            if (dataBundle.containsKey(sKey)) {
                sTemp = dataBundle.getString(sKey, "0.0");
                int goalSteps = Math.round(Float.parseFloat(sTemp));
                if (goalSteps > 0){
                    float percentGoal = (steps > 0) ? ((float)steps/goalSteps)*100 : 0F;
                    sContent = String.format(Locale.getDefault(), resources.getString(R.string.note_summary_steps_goals), steps, goalSteps, percentGoal);
                }
            }else
                sContent = String.format(Locale.getDefault(),resources.getString(R.string.note_summary_steps), steps);

            sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_dist),dataBundle.getFloat(Constants.MAP_DISTANCE));

            sKey = GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName();
            int moveMins = dataBundle.getInt(MAP_MOVE_MINS);
            if (dataBundle.containsKey(sKey)){
                sTemp = dataBundle.getString(sKey, "0.0");
                int goalMove = Math.round(Float.parseFloat(sTemp));
                if (goalMove > 0){
                    float percentGoal = (moveMins > 0) ? ((float)moveMins/goalMove)*100 : 0F;
                    sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_move_goals), moveMins, goalMove, percentGoal);
                }
            }else
                sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_move), moveMins);

            sKey = GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName();
            int heartPts = Math.round(dataBundle.getFloat(MAP_HEART_POINTS));
            if (dataBundle.containsKey(sKey)){
                sTemp = dataBundle.getString(sKey, "0");
                int goalHeart = Math.round(Float.parseFloat(sTemp));
                if (goalHeart> 0){
                    float percentGoal = (heartPts > 0) ? ((float)heartPts/goalHeart)*100 : 0F;
                    sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_heart_pts_goals), heartPts, goalHeart, percentGoal);
                }
            }else
                sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_heart_pts), heartPts);

            sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(), resources.getString(R.string.note_summary_heart),dataBundle.getFloat(Constants.MAP_BPM_MIN),
                    dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG));
            sContent = sContent + Constants.ATRACKIT_SPACE + String.format(Locale.getDefault(),resources.getString(R.string.note_summary_calories),dataBundle.getFloat(Constants.MAP_CALORIES));
            sKey = Workout.class.getSimpleName() + "_list";
            if (dataBundle.containsKey(sKey)){
                List<Workout> list = dataBundle.getParcelableArrayList(sKey);
                sContent = sContent + "\nDetected Activity\n";
                for(Workout w: list){
                    sContent = sContent + referencesTools.getFitnessActivityTextById(w.activityID) + ATRACKIT_SPACE + Utilities.getDurationBreakdown(w.duration) + "\n";
                }
            }

        }catch (Exception e){
            Log.e(LOG_TAG,e.getMessage());
        }
        // No goals option!
        if (!dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName()))
            sContent = String.format(Locale.getDefault(),resources.getString(R.string.note_summary_desc), dataBundle.getInt(MAP_MOVE_MINS), dataBundle.getFloat(Constants.MAP_DISTANCE),
                dataBundle.getFloat(Constants.MAP_BPM_MIN),  dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(MAP_STEPS));
        //Bitmap bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
        Drawable d = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_home, ctx.getTheme());
        Bitmap bitmap = vectorToBitmap(d);
        IconCompat bubbleIcon = IconCompat.createWithResource(ctx, R.drawable.ic_a_outlined);
        NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder(notificationPendingIntent,bubbleIcon).setAutoExpandBubble(true).build();
        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(ctx, SUMMARY_CHANNEL_ID)
                .setContentTitle(sTitle).setContentText(sContent)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(sContent).setSummaryText(sTitle))
                .setBubbleMetadata(metaBubble)
                .setSmallIcon(R.drawable.ic_a_outlined)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                //.extend(extender)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        mNotifyManager.notify(notificationID, notifyBuilder.build());


    }

    private Bitmap vectorToBitmap(Drawable drawable){
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    public static void cancelAlarm(Context ctx) {
        AlarmManager alarms = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            /* cancel any pending alarm */
            alarms.cancel(getPendingIntent(ctx, false, Constants.ATRACKIT_EMPTY, null));
        }
    }
    public static void setAlarm(Context ctx, boolean force, long delay, String sUserID, Parcelable parcel) {
        cancelAlarm(ctx);
        AlarmManager alarms = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        // EVERY X MINUTES
        long when = System.currentTimeMillis();
        if (!force) {
            when += delay;
        }
        /* fire the broadcast */
        try {
            PendingIntent pendingIntent = getPendingIntent(ctx, force, sUserID, parcel);
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        }catch(NullPointerException e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    private static PendingIntent getPendingIntent(Context ctx, boolean force, String sUserID, Parcelable parcel) {
        Intent alarmIntent = new Intent(ctx, CustomIntentReceiver.class);
        alarmIntent.setAction(INTENT_DAILY);
        alarmIntent.putExtra(KEY_FIT_TYPE, (force ? 1: 0));
        alarmIntent.putExtra(KEY_FIT_USER, sUserID);
        if (parcel != null) alarmIntent.putExtra(KEY_FIT_VALUE, parcel);
        return PendingIntent.getBroadcast(ctx, ALARM_CODE, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
