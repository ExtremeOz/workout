package com.a_track_it.fitdata.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.activity.MainActivity;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.DataSyncWorker;
import com.a_track_it.fitdata.common.data_model.Device2SensorWorker;
import com.a_track_it.fitdata.common.data_model.DeviceUpdateWorker;
import com.a_track_it.fitdata.common.data_model.EntityWorker;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.ObjectAggregate;
import com.a_track_it.fitdata.common.data_model.SensorDailyTotals;
import com.a_track_it.fitdata.common.data_model.TableTupleWorker;
import com.a_track_it.fitdata.common.data_model.UserDailyTotals;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.a_track_it.fitdata.common.Constants.ACTIVE_CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.COUNT_PATH;
import static com.a_track_it.fitdata.common.Constants.FIREBASE_CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.GOALS_CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVESET_START;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVESET_STOP;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_PAUSE;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_RESUMED;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_START;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_STOP;
import static com.a_track_it.fitdata.common.Constants.INTENT_GOAL_TRIGGER;
import static com.a_track_it.fitdata.common.Constants.INTENT_SCHEDULE_TRIGGER;
import static com.a_track_it.fitdata.common.Constants.INTENT_SETUP;
import static com.a_track_it.fitdata.common.Constants.INTENT_SUMMARY_DAILY;
import static com.a_track_it.fitdata.common.Constants.INTENT_WORKOUT_REPORT;
import static com.a_track_it.fitdata.common.Constants.KEY_COMM_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.fitdata.common.Constants.LABEL_DEVICE_ASKED;
import static com.a_track_it.fitdata.common.Constants.LABEL_DEVICE_USE;
import static com.a_track_it.fitdata.common.Constants.MAINTAIN_CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.MESSAGE_PATH_WEAR_SERVICE;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_ACTIVE_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_FIREBASE_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_GOAL_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_SCHEDULE_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_SUMMARY_ID;
import static com.a_track_it.fitdata.common.Constants.PHONE_DATA_ITEM_RECEIVED_PATH;
import static com.a_track_it.fitdata.common.Constants.SUMMARY_CHANNEL_ID;

public class MyWearListenerService extends WearableListenerService {
    private static final String LOG_TAG = MyWearListenerService.class.getSimpleName();
    private ReferencesTools mReferenceTools;
    private Calendar mCalendar;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private Exercise mExercise;
    private Bodypart mBodypart;
    private List<WorkoutSet> mSets;
    private String sUserID;
    private String sHostID;
    private String sHostName;
    private Handler handler;
    private ApplicationPreferences appPrefs;
    private boolean useNotifications;

    public MyWearListenerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate: " );
        mReferenceTools = ReferencesTools.getInstance();
        mCalendar = Calendar.getInstance(Locale.getDefault());
        final Context context = getApplicationContext();
        mReferenceTools.init(context);
        appPrefs = ApplicationPreferences.getPreferences(context);
        sUserID = appPrefs.getLastUserID();
        sHostID = appPrefs.getLastNodeID();
        sHostName = appPrefs.getLastNodeName();
        handler = new Handler(Looper.getMainLooper());
        UserPreferences userPrefs = UserPreferences.getPreferences(context, sUserID);
        this.useNotifications = (userPrefs != null) ? userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION): true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Gson gson = new Gson();
        Context context = getApplicationContext();
        mReferenceTools.init(context);
        if (sUserID == null || sHostID == null){
            ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
            sUserID = appPrefs.getLastUserID();
            sHostID = appPrefs.getLastNodeID();
            sHostName = appPrefs.getLastNodeName();
            if (sHostID.length() == 0) return;
        }
        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Uri uri = event.getDataItem().getUri();
                String path = uri.getPath();
                if (!Constants.PHONE_SYNC_ITEM_RECEIVED_PATH.equals(path) && !PHONE_DATA_ITEM_RECEIVED_PATH.equals(path)
                        && !Constants.PHONE_DATA_BUNDLE_RECEIVED_PATH.equals(path) && !COUNT_PATH.equals(path))
                    return;

                String host = uri.getHost();
                String hostName = Constants.ATRACKIT_EMPTY;
                if (host.equals(sHostID)){
                    hostName = sHostName;
                    Log.w(LOG_TAG, "onDataChanged: " + hostName + " path " + path);
                }else {
                    Log.w(LOG_TAG, "onDataChanged: " + host + " path " + path);
                }
                // receiving table info from a sync message request
                if (Constants.PHONE_SYNC_ITEM_RECEIVED_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    String intentAction = (dataMap.containsKey(Constants.KEY_FIT_ACTION)) ? dataMap.getString(Constants.KEY_FIT_ACTION) : Constants.ATRACKIT_EMPTY;
                    String sUser = dataMap.getString(KEY_FIT_USER);
                    String sReturnPath = dataMap.getString(Constants.KEY_FIT_REC);
                    if ((intentAction.equals(Constants.INTENT_PHONE_SYNC)) && sUser.equals(sUserID)) {
                        String sDevice = dataMap.getString(KEY_FIT_DEVICE_ID);
                        String sTable = dataMap.getString(Constants.MAP_DATA_TYPE);
                        long lCount = dataMap.getLong(Constants.MAP_COUNT);
                        long lMin = dataMap.getLong(Constants.MAP_START);
                        long lMax = dataMap.getLong(Constants.MAP_END);
                        Data.Builder builder = new Data.Builder();
                        builder.putLong(Constants.MAP_COUNT, lCount);
                        builder.putLong(Constants.MAP_START, lMin);
                        builder.putLong(Constants.MAP_END, lMax);
                        builder.putString(KEY_FIT_ACTION, Constants.INTENT_PHONE_REQUEST); // now request data using supplied values
                        builder.putString(Constants.KEY_FIT_REC, Constants.WEAR_DATA_ITEM_RECEIVED_PATH);// check this table and request items to this path
                        builder.putString(KEY_FIT_DEVICE_ID, sDevice);
                        builder.putString(KEY_FIT_USER, sUserID);
                        builder.putString(Constants.MAP_DATA_TYPE, sTable);
                        Data inputData = builder.build();
                        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                        OneTimeWorkRequest oneTimeWorkRequest =
                                new OneTimeWorkRequest.Builder(TableTupleWorker.class)
                                        .setInputData(inputData)
                                        .setConstraints(constraints)
                                        .build();
                        WorkManager workManager = WorkManager.getInstance(context);
                        workManager.enqueue(oneTimeWorkRequest);
                    }
                } // end of PHONE_SYNC_ITEM_RECEIVED_PATH
                if (PHONE_DATA_ITEM_RECEIVED_PATH.equals(path)) {
                  //  Bundle resultData = new Bundle();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    String intentAction =  (dataMap.containsKey(Constants.KEY_FIT_ACTION)) ? dataMap.getString(Constants.KEY_FIT_ACTION) : Constants.ATRACKIT_EMPTY;
                    String sUser = dataMap.getString(KEY_FIT_USER);
                    String sTableName = dataMap.getString(Constants.MAP_DATA_TYPE);
                    if (sTableName == null) Log.i(LOG_TAG, path + " MAP_DATA_TYPE NOT SET");
                    if ((intentAction.equals(Constants.INTENT_PHONE_REQUEST)) && sUser.equals(sUserID) && (sTableName != null)) {
                        Data.Builder dataBuilder = new Data.Builder();
                        dataBuilder.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_TABLE_INFO);
                        dataBuilder.putString(Constants.KEY_FIT_ACTION, intentAction);
                        dataBuilder.putString(KEY_FIT_USER, sUser);
                        dataBuilder.putString(Constants.KEY_FIT_HOST, host);
                        int iCounter = 0;
                        dataBuilder.putString(Constants.MAP_DATA_TYPE, sTableName);
                        if (sTableName.equals(Bodypart.class.getSimpleName())) {
                            String sBodypart = dataMap.getString(sTableName);
                            if ((sBodypart != null) && (sBodypart.length() > 0)) {
                                mBodypart = gson.fromJson(sBodypart, Bodypart.class);
                                dataBuilder.putString(sTableName, sBodypart);
                                iCounter++;
                            }
                        }
                        if (sTableName.equals(Exercise.class.getSimpleName())) {
                            String sExercise = dataMap.getString(sTableName);
                            if ((sExercise != null) && (sExercise.length() > 0)) {
                                mExercise = gson.fromJson(sExercise, Exercise.class);
                                dataBuilder.putString(sTableName, sExercise);
                                iCounter++;
                            }
                        }                        
                        if (sTableName.equals(Workout.class.getSimpleName())) {
                            String sWorkout = dataMap.getString(sTableName);
                            if ((sWorkout != null) && (sWorkout.length() > 0)) {
                                mWorkout = gson.fromJson(sWorkout, Workout.class);
                                dataBuilder.putString(sTableName, sWorkout);
                                iCounter++;
                            }
                        }
                        if (sTableName.equals(WorkoutSet.class.getSimpleName())) {
                            String sWorkoutSet = dataMap.getString(sTableName);
                            if ((sWorkoutSet != null) && (sWorkoutSet.length() > 0)) {
                                mWorkoutSet = gson.fromJson(sWorkoutSet, WorkoutSet.class);
                                dataBuilder.putString(sTableName, sWorkoutSet);
                                iCounter++;
                            }
                        }
                        if (sTableName.equals(WorkoutMeta.class.getSimpleName())) {
                            String sMeta = dataMap.getString(WorkoutMeta.class.getSimpleName());
                            if ((sMeta != null) && (sMeta.length() > 0)) {
                                mWorkoutMeta = gson.fromJson(sMeta, WorkoutMeta.class);
                                dataBuilder.putString(sTableName, sMeta);
                            }
                        }
                        if (sTableName.equals(ObjectAggregate.class.getSimpleName())) {
                            String sObjAgg = dataMap.getString(ObjectAggregate.class.getSimpleName());
                            if ((sObjAgg != null) && (sObjAgg.length() > 0)) {
                                //mWorkoutMeta = gson.fromJson(sMeta, WorkoutMeta.class);
                                dataBuilder.putString(sTableName, sObjAgg);
                            }
                        }
                        if (dataMap.containsKey(Constants.KEY_LIST_SETS)) {
                            String sList = dataMap.getString(Constants.KEY_LIST_SETS);
                            if ((sList != null) && (sList.length() > 0)) {
                                iCounter++;
                                if (sList.charAt(0) != 91) {
                                    sList = "[" + sList + "]";
                                }
                                mSets = Arrays.asList(gson.fromJson(sList, WorkoutSet[].class));
                                dataBuilder.putString(Constants.KEY_LIST_SETS, sList);
                                dataBuilder.putInt(Constants.KEY_FIT_SETS, sList.length());
                            }
                        }
                        try {
                            if (iCounter > 0) {
                                OneTimeWorkRequest oneTimeWorkRequest =
                                        new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                                                .setInputData(dataBuilder.build()).addTag(LOG_TAG)
                                                .build();
                                WorkManager workManager = WorkManager.getInstance(context);
                                workManager.enqueue(oneTimeWorkRequest);
                            }
                        }catch(Exception e){
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Log.e(LOG_TAG, e.getMessage());
                        }

                    }
                }
                if (Constants.PHONE_DATA_BUNDLE_RECEIVED_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    if (dataMapItem == null) return;
                    DataMap dataMap = dataMapItem.getDataMap();
                    if (dataMap.containsKey(Constants.KEY_FIT_ACTION)) {
                        String intentAction = dataMap.getString(Constants.KEY_FIT_ACTION);
                        String sUser = dataMap.getString(KEY_FIT_USER);
                        Data.Builder dataBuilder = new Data.Builder();
                        dataBuilder.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_TABLE_INFO);
                        dataBuilder.putString(Constants.KEY_FIT_ACTION, intentAction);
                        dataBuilder.putString(Constants.KEY_FIT_HOST, host);
                        dataBuilder.putString(KEY_FIT_USER, sUser);
                        if (dataMap.containsKey(Workout.class.getSimpleName())) {
                            String sWorkout = dataMap.getString(Workout.class.getSimpleName());
                            if ((sWorkout != null) && (sWorkout.length() > 0)) {
                                mWorkout = gson.fromJson(sWorkout, Workout.class);
                                dataBuilder.putString(Workout.class.getSimpleName(), sWorkout);
                            }
                        }
                        if (dataMap.containsKey(WorkoutSet.class.getSimpleName())) {
                            String sWorkset = dataMap.getString(WorkoutSet.class.getSimpleName());
                            if ((sWorkset != null) && (sWorkset.length() > 0)) {
                                mWorkoutSet = gson.fromJson(sWorkset, WorkoutSet.class);
                                dataBuilder.putString(WorkoutSet.class.getSimpleName(), sWorkset);
                            }
                        }
                        if (dataMap.containsKey(WorkoutMeta.class.getSimpleName())) {
                            String sMeta = dataMap.getString(WorkoutMeta.class.getSimpleName());
                            if ((sMeta != null) && (sMeta.length() > 0)) {
                                mWorkoutMeta = gson.fromJson(sMeta, WorkoutMeta.class);
                                dataBuilder.putString(WorkoutMeta.class.getSimpleName(), sMeta);
                            }
                        }
                        if (dataMap.containsKey(Constants.KEY_LIST_SETS)){
                            String sList = dataMap.getString(Constants.KEY_LIST_SETS);
                            if ((sList != null) && (sList.length() > 0)) {
                                if (sList.charAt(0) != 91) {
                                    sList = "[" + sList + "]";
                                }
                                mSets = Arrays.asList(gson.fromJson(sList, WorkoutSet[].class));
                                dataBuilder.putString(Constants.KEY_LIST_SETS, sList);
                                dataBuilder.putInt(Constants.KEY_FIT_SETS, sList.length());
                            }
                        }
                        Log.i(LOG_TAG, "sendNotification " + intentAction + " " + hostName);
                        // now notify
                        // if (this.useNotifications) sendNotification(intentAction, hostName, host);
                        sendNotification(intentAction, hostName, host);
                        try {
                            Data data = dataBuilder.build();
                            OneTimeWorkRequest oneTimeWorkRequest =
                                    new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                                            .setInputData(data).addTag(LOG_TAG)
                                            .build();
                            WorkManager workManager = WorkManager.getInstance(context);
                            workManager.enqueue(oneTimeWorkRequest);

                        }catch(Exception e){
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }
                }
                if (COUNT_PATH.equals(path)) {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    String nodeId = uri.getHost();
                    // Set the data of the message to be the bytes of the Uri.
                    byte[] payload = uri.toString().getBytes();

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(getApplicationContext())
                                    .sendMessage(nodeId, Constants.WEAR_DATA_ITEM_RECEIVED_PATH, payload);

                    sendMessageTask.addOnCompleteListener(
                            new OnCompleteListener<Integer>() {
                                @Override
                                public void onComplete(Task<Integer> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(LOG_TAG, "Message sent successfully");
                                    } else {
                                        Log.d(LOG_TAG, "Message failed.");
                                    }
                                }
                            });
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {

            }
        } // for each event

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String messagePath = messageEvent.getPath();
        String host = messageEvent.getSourceNodeId();
        Context context = getApplicationContext();
        ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        if (sUserID == null || sHostID == null){
            sUserID = appPrefs.getLastUserID();
            sHostID = appPrefs.getLastNodeID();
            sHostName = appPrefs.getLastNodeName();
            if (sHostID.length() == 0) return;
        }
        if (sHostID != null && (sHostID.equals(host))){
            Log.w(LOG_TAG, "onMessageReceived() A message from app was received:"
                    + sHostName
                    + " "
                    + messagePath);
        }else {
            Log.i(LOG_TAG, "onMessageReceived() A message from app was UNKNOWN received:"
                    + messageEvent.getRequestId()
                    + " "
                    + messagePath);
            //TODO: get info from unknown NEW device
           // return;
        }

        if (messagePath.equals(Constants.DATA_START_ACTIVITY)) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(startIntent);
        }
        // Check to see if the message is to start an activity
        if (messagePath.equals(Constants.MESSAGE_PATH_PHONE_SERVICE)) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            int requestType = dataMap.getInt(KEY_COMM_TYPE);
            String sRequestType = Constants.ATRACKIT_EMPTY;
            switch (requestType){
                case Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION:
                    sRequestType = "COMM_TYPE_REQUEST_PROMPT_PERMISSION";
                    break;
                case Constants.COMM_TYPE_REQUEST_DATA:
                    sRequestType = "COMM_TYPE_REQUEST_DATA";
                    break;
                case Constants.COMM_TYPE_REQUEST_INFO:
                    sRequestType = "COMM_TYPE_REQUEST_INFO";
                    break;
                case Constants.COMM_TYPE_DEVICE_UPDATE:
                    sRequestType = "COMM_TYPE_DEVICE_UPDATE";
                    break;
                case Constants.COMM_TYPE_DAILY_UPDATE:
                    sRequestType = "COMM_TYPE_DAILY_UPDATE";
                    break;
                case Constants.COMM_TYPE_SENSOR_UPDATE:
                    sRequestType = "COMM_TYPE_SENSOR_UPDATE";
                    break;
                case Constants.COMM_TYPE_STARTUP_INFO:
                    sRequestType = "COMM_TYPE_STARTUP_INFO";
                    break;
                case Constants.COMM_TYPE_SETUP_INFO:
                    sRequestType = "COMM_TYPE_SETUP_INFO";
                    break;
                case Constants.COMM_TYPE_TABLE_INFO:
                    sRequestType = "COMM_TYPE_TABLE_INFO";
                    break;
                case Constants.COMM_TYPE_URL_VIEW:
                    sRequestType = "COMM_TYPE_URL_VIEW";
                    break;
            }
            Log.i(LOG_TAG, "Message Received type " + sRequestType);
            String sUser = dataMap.getString(Constants.KEY_FIT_USER);
            if ((sUser == null) || (sUser.length() == 0)) return;
            if ((sUserID.length() > 0) && (!sUserID.equals(sUser))) return;
            Data.Builder dataBuilder = new Data.Builder();
            if (requestType == Constants.COMM_TYPE_DEVICE_UPDATE){  // set device sync to values
                Log.i(LOG_TAG, "COMM_TYPE_DEVICE_UPDATE response ");
                dataBuilder.putString(Constants.KEY_FIT_USER, sUser);
                dataBuilder.putLong(Constants.KEY_FIT_WORKOUTID, dataMap.getLong(Constants.KEY_FIT_WORKOUTID));
                dataBuilder.putLong(Constants.KEY_FIT_WORKOUT_SETID, dataMap.getLong(Constants.KEY_FIT_WORKOUT_SETID));
                dataBuilder.putLong(Constants.KEY_FIT_WORKOUT_METAID, dataMap.getLong(Constants.KEY_FIT_WORKOUT_METAID));
                dataBuilder.putInt(Constants.KEY_FIT_SETS, dataMap.getInt(Constants.KEY_FIT_SETS));
                if (dataMap.getLongArray(Constants.KEY_LIST_SETS) != null)
                    dataBuilder.putLongArray(Constants.KEY_LIST_SETS, dataMap.getLongArray(Constants.KEY_LIST_SETS));
                Log.i(LOG_TAG, "device update received " + dataBuilder.toString());
                OneTimeWorkRequest oneTimeWorkRequest =
                        new OneTimeWorkRequest.Builder(DeviceUpdateWorker.class)
                                .setInputData(dataBuilder.build()).addTag(LOG_TAG)
                                .build();
                WorkManager workManager = WorkManager.getInstance(context);
                workManager.enqueue(oneTimeWorkRequest);
                return;
            }
            if (requestType == Constants.COMM_TYPE_SETUP_INFO){
                if ((sUserID.length() > 0) && (sUserID.equals(sUser))) {
                    UserPreferences userPrefs = UserPreferences.getPreferences(context, sUserID);
                    int requestDirection = dataMap.getInt(KEY_FIT_TYPE, 0);
                    long timeMs = System.currentTimeMillis();
                    if (userPrefs == null) return;
                    if (requestDirection == 1){
                        Log.i(LOG_TAG, "COMM_TYPE_SETUP_INFO request to send ");
                        DataMap dataMapResponse = new DataMap();
                        dataMapResponse.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_SETUP_INFO);
                        dataMapResponse.putString(Constants.KEY_FIT_USER, sUser);
                        dataMapResponse.putInt(Constants.KEY_FIT_TYPE, 0); // sending not requesting!
                        String sAge = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                        if (sAge.length() > 0)
                            dataMapResponse.putString(Constants.INTENT_PERMISSION_AGE, sAge);
                        String sHeight = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                        if (sHeight.length() > 0)
                            dataMapResponse.putString(Constants.INTENT_PERMISSION_HEIGHT, sHeight);
                        dataMapResponse.putBoolean(Constants.AP_PREF_ASK_AGE, userPrefs.getAskAge());
                        dataMapResponse.putBoolean(Constants.AP_PREF_USE_KG, userPrefs.getUseKG());
                        dataMapResponse.putBoolean(Constants.AP_PREF_USE_TIMED_REST, userPrefs.getTimedRest());
                        dataMapResponse.putBoolean(Constants.AP_PREF_USE_TIMED_AUTO_START, userPrefs.getRestAutoStart());
                        dataMapResponse.putBoolean(LABEL_DEVICE_USE, userPrefs.getPrefByLabel(LABEL_DEVICE_USE));
                        dataMapResponse.putBoolean(LABEL_DEVICE_ASKED, userPrefs.getPrefByLabel(LABEL_DEVICE_ASKED));
                        dataMapResponse.putBoolean(Constants.USER_PREF_CONF_START_SESSION, userPrefs.getConfirmStartSession());
                        dataMapResponse.putBoolean(Constants.USER_PREF_CONF_END_SESSION, userPrefs.getConfirmEndSession());
                        dataMapResponse.putBoolean(Constants.USER_PREF_CONF_SET_SESSION, userPrefs.getConfirmSetSession());
                        dataMapResponse.putBoolean(Constants.USER_PREF_CONF_DEL_SESSION, userPrefs.getConfirmDeleteSession());
                        // dataMapResponse.putBoolean(Constants.USER_PREF_CONF_EXIT_APP, userPrefs.getConfirmExitApp());
                        dataMapResponse.putBoolean(Constants.USER_PREF_USE_ROUND_IMAGE, userPrefs.getUseRoundedImage());
                        dataMapResponse.putInt(Constants.USER_PREF_GYM_REST_DURATION, userPrefs.getWeightsRestDuration());
                        dataMapResponse.putInt(Constants.USER_PREF_SHOOT_REST_DURATION, userPrefs.getArcheryRestDuration());
                        dataMapResponse.putInt(Constants.USER_PREF_SHOOT_CALL_DURATION, userPrefs.getArcheryCallDuration());
                        dataMapResponse.putInt(Constants.USER_PREF_SHOOT_END_DURATION, userPrefs.getArcheryEndDuration());
                        dataMapResponse.putInt(Constants.USER_PREF_DEF_NEW_SETS, userPrefs.getDefaultNewSets());
                        dataMapResponse.putInt(Constants.USER_PREF_DEF_NEW_REPS, userPrefs.getDefaultNewReps());

                        dataMapResponse.putLong(Constants.USER_PREF_BPM_SAMPLE_RATE, userPrefs.getBPMSampleRate());
                        dataMapResponse.putLong(Constants.USER_PREF_STEP_SAMPLE_RATE, userPrefs.getStepsSampleRate());
                        dataMapResponse.putLong(Constants.USER_PREF_OTHERS_SAMPLE_RATE, userPrefs.getOthersSampleRate());
                        dataMapResponse.putBoolean(Constants.AP_PREF_USE_LOCATION, appPrefs.getUseLocation());
                        dataMapResponse.putBoolean(Constants.AP_PREF_USE_SENSORS, appPrefs.getUseSensors());
                        dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_PHONE, appPrefs.getPhoneSyncInterval());
                        dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT, appPrefs.getLastSyncInterval());
                        dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_DAILY, appPrefs.getDailySyncInterval());
                        dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_NETWORK, appPrefs.getNetworkCheckInterval());
                        Long lHistoryStart = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_START);
                        Long lHistoryEnd = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_END);
                        dataMapResponse.putLong(Constants.MAP_HISTORY_START, lHistoryStart);
                        dataMapResponse.putLong(Constants.MAP_HISTORY_END, lHistoryEnd);
                        sendMessage(MESSAGE_PATH_WEAR_SERVICE, host, dataMapResponse);
                    }
                    else{
                        if (dataMap.containsKey(Constants.INTENT_PERMISSION_AGE)) {
                            String sAgeIn = dataMap.getString(Constants.INTENT_PERMISSION_AGE);
                            String sAgeSet = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                            if (sAgeSet.length() == 0 || (!sAgeSet.equals(sAgeIn)))
                                userPrefs.setPrefStringByLabel(Constants.INTENT_PERMISSION_AGE, sAgeIn);
                        }
                        if (dataMap.containsKey(Constants.INTENT_PERMISSION_HEIGHT)) {
                            String sHeightIn = dataMap.getString(Constants.INTENT_PERMISSION_HEIGHT);
                            String sHeightSet = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                            if (sHeightSet.length() == 0 || (!sHeightSet.equals(sHeightIn)))
                                userPrefs.setPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT, sHeightIn);
                        }
                        boolean bInBoolean = dataMap.getBoolean(Constants.AP_PREF_ASK_AGE, userPrefs.getAskAge());
                        if (bInBoolean != userPrefs.getAskAge())
                            userPrefs.setAskAge(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.AP_PREF_USE_KG, userPrefs.getUseKG());
                        if (bInBoolean != userPrefs.getUseKG())
                            userPrefs.setUseKG(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.AP_PREF_USE_TIMED_REST, userPrefs.getTimedRest());
                        if (bInBoolean != userPrefs.getTimedRest())
                            userPrefs.setTimedRest(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.AP_PREF_USE_TIMED_AUTO_START, userPrefs.getRestAutoStart());
                        if (bInBoolean != userPrefs.getRestAutoStart())
                            userPrefs.setRestAutoStart(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.LABEL_DEVICE_USE, userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE));
                        if (bInBoolean != userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE))
                            userPrefs.setPrefByLabel(LABEL_DEVICE_USE, bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.LABEL_DEVICE_ASKED, userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED));
                        if (bInBoolean != userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
                            userPrefs.setPrefByLabel(LABEL_DEVICE_ASKED, bInBoolean);

                        bInBoolean = dataMap.getBoolean(Constants.USER_PREF_CONF_START_SESSION, userPrefs.getConfirmStartSession());
                        if (bInBoolean != userPrefs.getConfirmStartSession())
                            userPrefs.setConfirmStartSession(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.USER_PREF_CONF_END_SESSION, userPrefs.getConfirmEndSession());
                        if (bInBoolean != userPrefs.getConfirmEndSession())
                            userPrefs.setConfirmEndSession(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.USER_PREF_CONF_SET_SESSION, userPrefs.getConfirmSetSession());
                        if (bInBoolean != userPrefs.getConfirmSetSession())
                            userPrefs.setConfirmSetSession(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.USER_PREF_CONF_DEL_SESSION, userPrefs.getConfirmDeleteSession());
                        if (bInBoolean != userPrefs.getConfirmDeleteSession())
                            userPrefs.setConfirmDeleteSession(bInBoolean);
                        /*bInBoolean = dataMap.getBoolean(Constants.USER_PREF_CONF_EXIT_APP, userPrefs.getConfirmExitApp());
                        if (bInBoolean != userPrefs.getConfirmExitApp()) userPrefs.setConfirmExitApp(bInBoolean);*/
                        bInBoolean = dataMap.getBoolean(Constants.USER_PREF_USE_ROUND_IMAGE, userPrefs.getUseRoundedImage());
                        if (bInBoolean != userPrefs.getUseRoundedImage())
                            userPrefs.setUseRoundedImage(bInBoolean);

                        int iInInt = dataMap.getInt(Constants.USER_PREF_GYM_REST_DURATION, userPrefs.getWeightsRestDuration());
                        if (iInInt != userPrefs.getWeightsRestDuration())
                            userPrefs.setWeightsRestDuration(iInInt);
                        iInInt = dataMap.getInt(Constants.USER_PREF_SHOOT_REST_DURATION, userPrefs.getArcheryRestDuration());
                        if (iInInt != userPrefs.getArcheryRestDuration())
                            userPrefs.setArcheryRestDuration(iInInt);
                        iInInt = dataMap.getInt(Constants.USER_PREF_SHOOT_CALL_DURATION, userPrefs.getArcheryCallDuration());
                        if (iInInt != userPrefs.getArcheryCallDuration())
                            userPrefs.setArcheryCallDuration(iInInt);
                        iInInt = dataMap.getInt(Constants.USER_PREF_SHOOT_END_DURATION, userPrefs.getArcheryEndDuration());
                        if (iInInt != userPrefs.getArcheryEndDuration())
                            userPrefs.setArcheryEndDuration(iInInt);
                        iInInt = dataMap.getInt(Constants.USER_PREF_DEF_NEW_SETS, userPrefs.getDefaultNewSets());
                        if (iInInt != userPrefs.getDefaultNewSets())
                            userPrefs.setDefaultNewSets(iInInt);
                        iInInt = dataMap.getInt(Constants.USER_PREF_DEF_NEW_REPS, userPrefs.getDefaultNewReps());
                        if (iInInt != userPrefs.getDefaultNewReps())
                            userPrefs.setDefaultNewReps(iInInt);

                        long iInLong = dataMap.getLong(Constants.USER_PREF_BPM_SAMPLE_RATE, userPrefs.getBPMSampleRate());
                        if (iInLong != userPrefs.getBPMSampleRate())
                            userPrefs.setBPMSampleRate(iInLong);
                        iInLong = dataMap.getLong(Constants.USER_PREF_STEP_SAMPLE_RATE, userPrefs.getStepsSampleRate());
                        if (iInLong != userPrefs.getStepsSampleRate())
                            userPrefs.setStepsSampleRate(iInLong);
                        iInLong = dataMap.getLong(Constants.USER_PREF_OTHERS_SAMPLE_RATE, userPrefs.getOthersSampleRate());
                        if (iInLong != userPrefs.getOthersSampleRate())
                            userPrefs.setOthersSampleRate(iInLong);

                        bInBoolean = dataMap.getBoolean(Constants.AP_PREF_USE_LOCATION, appPrefs.getUseLocation());
                        if (bInBoolean != appPrefs.getUseLocation())
                            appPrefs.setUseLocation(bInBoolean);
                        bInBoolean = dataMap.getBoolean(Constants.AP_PREF_USE_SENSORS, appPrefs.getUseSensors());
                        if (bInBoolean != appPrefs.getUseSensors())
                            appPrefs.setUseSensors(bInBoolean);

                        iInLong = dataMap.getLong(Constants.AP_PREF_SYNC_INT_PHONE, appPrefs.getPhoneSyncInterval());
                        if (iInLong != appPrefs.getPhoneSyncInterval())
                            appPrefs.setPhoneSyncInterval(iInLong);
                        iInLong = dataMap.getLong(Constants.AP_PREF_SYNC_INT, appPrefs.getLastSyncInterval());
                        if (iInLong != appPrefs.getLastSyncInterval())
                            appPrefs.setLastSyncInterval(iInLong);
                        iInLong = dataMap.getLong(Constants.AP_PREF_SYNC_INT_DAILY, appPrefs.getDailySyncInterval());
                        if (iInLong != appPrefs.getDailySyncInterval())
                            appPrefs.setDailySyncInterval(iInLong);
                        iInLong = dataMap.getLong(Constants.AP_PREF_SYNC_INT_NETWORK, appPrefs.getNetworkCheckInterval());
                        if (iInLong != appPrefs.getNetworkCheckInterval())
                            appPrefs.setNetworkCheckInterval(iInLong);
                        if (dataMap.containsKey(Constants.KEY_FIT_DEVICE_ID)) {
                            Data.Builder builder = new Data.Builder();
                            String sName = dataMap.getString(Constants.KEY_FIT_NAME);
                            String sValue = dataMap.getString(Constants.KEY_FIT_DEVICE_ID);
                            String sValue1 = dataMap.getString(Constants.KEY_DEVICE1);
                            String sValue2 = dataMap.getString(Constants.KEY_DEVICE2);
                            builder.putInt(KEY_FIT_TYPE, 2); // setup DEVICE2 and DONT return with DEVICE1 info
                            builder.putString(Constants.KEY_FIT_ACTION, Configuration.class.getSimpleName());
                            builder.putString(Constants.KEY_FIT_USER, sUserID);
                            builder.putString(Constants.KEY_FIT_DEVICE_ID, sValue);
                            builder.putString(Constants.KEY_DEVICE1, sValue1);
                            builder.putString(Constants.KEY_DEVICE2, sValue2);
                            builder.putLong(Constants.KEY_FIT_TIME, timeMs);
                            builder.putString(Constants.KEY_FIT_NAME, sName);
                            Data inputData = builder.build();
                            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                            OneTimeWorkRequest oneTimeWorkRequest =
                                    new OneTimeWorkRequest.Builder(EntityWorker.class)
                                            .setInputData(inputData)
                                            .setConstraints(constraints)
                                            .build();
                            WorkManager workManager = WorkManager.getInstance(context);
                            workManager.enqueue(oneTimeWorkRequest);
                        }
                        broadcastToast("Settings sync with device");
                    }
                }
            }
            if (requestType == Constants.COMM_TYPE_TABLE_INFO){
                String intentAction = (dataMap.containsKey(Constants.KEY_FIT_ACTION)) ? dataMap.getString(Constants.KEY_FIT_ACTION) : Constants.ATRACKIT_EMPTY;
                String sReturnPath = dataMap.getString(Constants.KEY_FIT_REC);
                Log.i(LOG_TAG,"COMM_TYPE_TABLE_INFO  action " + intentAction + " return " + sReturnPath);
                if ((intentAction.length() > 0) && sUser.equals(sUserID)) {
                    String sDevice = dataMap.getString(KEY_FIT_DEVICE_ID);
                    String sTable = dataMap.getString(Constants.MAP_DATA_TYPE);
                    long lCount = dataMap.getLong(Constants.MAP_COUNT);
                    long lMin = dataMap.getLong(Constants.MAP_START);
                    long lMax = dataMap.getLong(Constants.MAP_END);
                    Data.Builder builder = new Data.Builder();
                    builder.putLong(Constants.MAP_COUNT, lCount);
                    builder.putLong(Constants.MAP_START, lMin);
                    builder.putLong(Constants.MAP_END, lMax);
                    builder.putString(KEY_FIT_ACTION, intentAction);
                    builder.putString(Constants.KEY_FIT_REC, sReturnPath);// check this table and request items to this path
                    builder.putString(KEY_FIT_DEVICE_ID, sDevice);
                    builder.putString(KEY_FIT_USER, sUserID);
                    builder.putString(Constants.MAP_DATA_TYPE, sTable);
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(TableTupleWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);
                }
            }
            if (requestType == Constants.COMM_TYPE_URL_VIEW){
                String sURL = dataMap.getString(Constants.KEY_FIT_VALUE);
                if (sURL != null && sURL.length() > 0) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sURL));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                }
            }
            if (requestType == Constants.COMM_TYPE_REQUEST_INFO){
                int type = dataMap.getInt(Constants.KEY_FIT_TYPE,0);
                long timeMs = System.currentTimeMillis();
                Log.i(LOG_TAG, "COMM_TYPE_REQUEST_INFO new request 3 startup info" + type);
                if (type == 1) {  // startup info request
                    Data.Builder builder = new Data.Builder();
                    builder.putString(Constants.KEY_FIT_ACTION, Configuration.class.getSimpleName());
                    builder.putInt(KEY_FIT_TYPE,type); // setup DEVICE2 and return with DEVICE1 info
                    builder.putString(Constants.KEY_FIT_USER, sUserID);
                    builder.putLong(Constants.KEY_FIT_TIME, dataMap.getLong(Constants.KEY_FIT_TIME,timeMs));
                    builder.putString(Constants.KEY_FIT_HOST, host);
                    String sValue = dataMap.getString(Constants.KEY_FIT_DEVICE_ID);
                    builder.putString(Constants.KEY_FIT_DEVICE_ID, sValue);
                    if (dataMap.containsKey(KEY_FIT_NAME))
                        if (dataMap.getString(KEY_FIT_NAME).equals(Constants.KEY_DEVICE1))
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE2);
                        else
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    builder.putString(Constants.KEY_PAYLOAD, dataMap.getString(Constants.KEY_PAYLOAD));
                    builder.putString(Constants.KEY_DEVICE1, dataMap.getString(Constants.KEY_DEVICE1));
                    builder.putString(Constants.KEY_DEVICE2, dataMap.getString(Constants.KEY_DEVICE2));
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(EntityWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);
                }
                if (type == 2){
                    Data.Builder builder = new Data.Builder();
                    builder.putString(Constants.KEY_FIT_ACTION, Configuration.class.getSimpleName());
                    builder.putInt(KEY_FIT_TYPE,(int)1); // setup DEVICE2
                    builder.putString(Constants.KEY_FIT_USER, sUserID);
                    builder.putLong(Constants.KEY_FIT_TIME, dataMap.getLong(Constants.KEY_FIT_TIME,timeMs));
                    builder.putString(Constants.KEY_FIT_HOST, host);
                    String sValue = dataMap.getString(Constants.KEY_FIT_DEVICE_ID);
                    builder.putString(Constants.KEY_FIT_DEVICE_ID, sValue);
                    if (dataMap.containsKey(KEY_FIT_NAME))
                        if (dataMap.getString(KEY_FIT_NAME).equals(Constants.KEY_DEVICE1))
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE2);
                        else
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    // no payload - so no return sent message
                    //builder.putString(Constants.KEY_PAYLOAD, dataMap.getString(Constants.KEY_PAYLOAD));
                    builder.putString(Constants.KEY_DEVICE1, dataMap.getString(Constants.KEY_DEVICE1));
                    builder.putString(Constants.KEY_DEVICE2, dataMap.getString(Constants.KEY_DEVICE2));
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(EntityWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);
                }
                if (type == 0) {  // handshake info request
                    Data.Builder builder = new Data.Builder();
                    builder.putString(Constants.KEY_FIT_ACTION, Configuration.class.getSimpleName());
                    builder.putInt(KEY_FIT_TYPE,type); // setup DEVICE2 and return with DEVICE1 info
                    builder.putString(Constants.KEY_FIT_USER, sUserID);
                    builder.putLong(Constants.KEY_FIT_TIME, dataMap.getLong(Constants.KEY_FIT_TIME,timeMs));
                    builder.putString(Constants.KEY_FIT_HOST, host);
                    String sValue = dataMap.getString(Constants.KEY_FIT_DEVICE_ID);
                    builder.putString(Constants.KEY_FIT_DEVICE_ID, sValue);
                    if (dataMap.containsKey(KEY_FIT_NAME))
                        if (dataMap.getString(KEY_FIT_NAME).equals(Constants.KEY_DEVICE1))
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE2);
                        else
                            builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    builder.putString(Constants.KEY_PAYLOAD, dataMap.getString(Constants.KEY_PAYLOAD));
                    builder.putString(Constants.KEY_DEVICE1, dataMap.getString(Constants.KEY_DEVICE1));
                    builder.putString(Constants.KEY_DEVICE2, dataMap.getString(Constants.KEY_DEVICE2));
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(EntityWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);
                }
                if (type == 4) {  // signout!
/*                    Data.Builder builder = new Data.Builder();
                    builder.putString(Constants.KEY_FIT_ACTION, Configuration.class.getSimpleName());
                    builder.putInt(KEY_FIT_TYPE,(int) 1); // setup DEVICE2 and return with DEVICE1 info
                    builder.putString(Constants.KEY_FIT_USER, sUserID);
                    builder.putLong(Constants.KEY_FIT_TIME, dataMap.getLong(Constants.KEY_FIT_TIME,timeMs));
                    builder.putString(Constants.KEY_FIT_HOST, host);
                    String sValue = dataMap.getString(Constants.KEY_FIT_DEVICE_ID);
                    builder.putString(Constants.KEY_FIT_DEVICE_ID, sValue);
                    if (dataMap.getString(KEY_FIT_NAME).equals(Constants.KEY_DEVICE1))
                        builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE2);
                    else
                        builder.putString(Constants.KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    builder.putString(Constants.KEY_PAYLOAD, dataMap.getString(Constants.KEY_PAYLOAD));
                    builder.putString(Constants.KEY_DEVICE1, dataMap.getString(Constants.KEY_DEVICE1));
                    builder.putString(Constants.KEY_DEVICE2, dataMap.getString(Constants.KEY_DEVICE2));
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(EntityWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);*/
                }
            }
            // receive new sensor data - pass onto Device2SensorWorker for saving and broadcast INTENT_TOTALS_REFRESH
            if (requestType == Constants.COMM_TYPE_DAILY_UPDATE){
                long previousTime = dataMap.getLong(KEY_FIT_VALUE);
                int currentState = dataMap.getInt(KEY_FIT_ACTION);
                boolean updatedSDT = false; boolean updatedUDT = false;
                String sVal = null;

                Data.Builder builder = new Data.Builder();
                builder.putString(KEY_FIT_USER,sUserID);
                builder.putString(KEY_FIT_DEVICE_ID,dataMap.getString(KEY_FIT_DEVICE_ID));
                builder.putLong(Constants.KEY_FIT_TIME, previousTime);
                builder.putInt(KEY_FIT_ACTION, currentState);
                String sKey  = SensorDailyTotals.class.getSimpleName();
                if (dataMap.containsKey(sKey)){
                    long lVal = dataMap.getLong(sKey);
                    builder.putLong(sKey, lVal);
                    sKey += "_last";
                    long lVal2 = dataMap.getLong(sKey);
                    builder.putLong(sKey, lVal2);
                }
                sKey  = DataType.TYPE_STEP_COUNT_DELTA.getName();
                if (dataMap.containsKey(sKey)){
                    int iVal = dataMap.getInt(sKey,-1);
                    if (iVal >= 0){
                        builder.putInt(sKey, iVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = DataType.TYPE_HEART_RATE_BPM.getName();
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey,-1);
                    if (fVal >= 0) {
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = Sensor.STRING_TYPE_PRESSURE;
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey,-1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = Sensor.STRING_TYPE_RELATIVE_HUMIDITY;
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey,-1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = Sensor.STRING_TYPE_AMBIENT_TEMPERATURE;
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey, -1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
/*                if (dataMap.containsKey(Constants.INTENT_RECOG)){
                    sVal = dataMap.getString(KEY_FIT_NAME);
                    int detectedID = -3;
                    long lastType = dataMap.getLong(Constants.INTENT_RECOG, -1);
                    if (dataMap.containsKey(KEY_FIT_TYPE)){
                        detectedID = dataMap.getInt(KEY_FIT_TYPE);
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + detectedID + " " + sVal);
                    }else
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + sVal);

                    if (((sVal != null) && (sVal.length() > 0)) || (detectedID != -3)){
                        mMessagesViewModel.addActivityMsg(sVal);
                        if (sdt != null) {
                            sVal += ATRACKIT_SPACE;
                            if (detectedID == -3) {
                                switch (sVal) {
                                    case Constants.RECOG_VEHCL:
                                        detectedID = DetectedActivity.IN_VEHICLE;
                                        break;
                                    case RECOG_BIKE:
                                        detectedID = DetectedActivity.ON_BICYCLE;
                                        break;
                                    case RECOG_FOOT:
                                        detectedID = DetectedActivity.ON_FOOT;
                                        break;
                                    case RECOG_RUN:
                                        detectedID = DetectedActivity.RUNNING;
                                        break;
                                    case RECOG_STILL:
                                        detectedID = DetectedActivity.STILL;
                                        break;
                                    case RECOG_TILT:
                                        detectedID = DetectedActivity.TILTING;
                                        break;
                                    case RECOG_WALK:
                                        detectedID = DetectedActivity.WALKING;
                                        break;
                                    case RECOG_UNKWN:
                                        detectedID = DetectedActivity.UNKNOWN;
                                        break;
                                }
                            }
                            if (detectedID != -3) {
                                sdt.activityType = detectedID;
                                sdt.lastActivityType = (lastType == -1) ? System.currentTimeMillis() : lastType;
                                updatedSDT = true;
                            }
                        }
                    }
                }*/
                // now the UDT values if needed.
                sKey  = UserDailyTotals.class.getSimpleName();
                if (dataMap.containsKey(sKey)){
                    long lVal = dataMap.getLong(sKey);
                    builder.putLong(sKey, lVal);
                    sKey += "_last";
                    long lVal2 = dataMap.getLong(sKey);
                    builder.putLong(sKey, lVal2);
                }
                sKey = DataType.TYPE_HEART_POINTS.getName();
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey, -1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = DataType.TYPE_DISTANCE_DELTA.getName();
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey, -1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = DataType.TYPE_CALORIES_EXPENDED.getName();
                if (dataMap.containsKey(sKey)){
                    float fVal = dataMap.getFloat(sKey, -1);
                    if (fVal >= 0){
                        builder.putFloat(sKey, fVal);
                        sKey += "_last";
                        builder.putLong(sKey, dataMap.getLong(sKey));
                        updatedSDT = true;
                    }
                }
                sKey = DataType.TYPE_MOVE_MINUTES.getName();
                if (dataMap.containsKey(sKey)){
                    int iVal = dataMap.getInt(sKey);
                    if (iVal >= 0){
                        builder.putInt(sKey, iVal);
                    }
                }
                sKey = DataType.TYPE_LOCATION_SAMPLE.getName();
                if (dataMap.containsKey(sKey)){
                    sVal = dataMap.getString(DataType.TYPE_LOCATION_SAMPLE.getName());
                    if (sVal.length() > 0){
                        builder.putString(sKey, sVal);
                    }
                }
                if (updatedSDT || updatedUDT){
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(Device2SensorWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    WorkManager workManager = WorkManager.getInstance(context);
                    workManager.enqueue(oneTimeWorkRequest);
                }
            }
        }
        // command to control activities
        if (messagePath.equals(Constants.DATA_START_WORKOUT) || messagePath.equals(Constants.DATA_STOP_WORKOUT) ||
                messagePath.equals(Constants.DATA_START_WORKOUT_SET) || messagePath.equals(Constants.DATA_STOP_WORKOUT_SET)) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            String sUser = dataMap.getString(Constants.KEY_FIT_USER);
            String sDeviceID = dataMap.getString(KEY_FIT_DEVICE_ID);
            if ((sUser == null) || (sUser.length() == 0)) return;
            if (!sUser.equals(sUserID)) return;

            String intentAction = null;
            if (messagePath.equals(Constants.DATA_START_WORKOUT))
                intentAction = Constants.INTENT_ACTIVE_START;
            if (messagePath.equals(Constants.DATA_STOP_WORKOUT))
                intentAction = Constants.INTENT_ACTIVE_STOP;
            if (messagePath.equals(Constants.DATA_START_WORKOUT_SET))
                intentAction = Constants.INTENT_ACTIVESET_START;
            if (messagePath.equals(Constants.DATA_STOP_WORKOUT_SET))
                intentAction = Constants.INTENT_ACTIVESET_STOP;

            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.setAction(intentAction);
            newIntent.putExtra(KEY_FIT_USER, sUserID);
            if (sDeviceID != null) newIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            newIntent.putExtra(Constants.KEY_FIT_TYPE, 1);  //external, from phone
            newIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            String sTemp = null;

            if (dataMap.containsKey(Constants.KEY_FIT_WORKOUTID)){
                newIntent.putExtra(Constants.KEY_FIT_WORKOUTID, dataMap.getLong(Constants.KEY_FIT_WORKOUTID));
            }else
                if (dataMap.containsKey(Workout.class.getSimpleName())){
                    sTemp = dataMap.getString(Workout.class.getSimpleName());
                    if ((sTemp != null) && (sTemp.length() > 0)) {
                        Gson gson = new Gson();
                        mWorkout = gson.fromJson(sTemp, Workout.class);
                        if (mWorkout != null) newIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                }
            sTemp = null;
            if (dataMap.containsKey(Constants.KEY_FIT_WORKOUT_SETID)) {
                newIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, dataMap.getLong(Constants.KEY_FIT_WORKOUT_SETID));
            }else
                if (dataMap.containsKey(WorkoutSet.class.getSimpleName())){
                    sTemp = dataMap.getString(WorkoutSet.class.getSimpleName());
                    if ((sTemp != null) && (sTemp.length() > 0)) {
                        Gson gson = new Gson();
                        mWorkoutSet = gson.fromJson(sTemp, WorkoutSet.class);
                        if (mWorkoutSet != null) newIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    }
                }
            try {
                if ((mWorkout != null) && mWorkout.userID.equals(sUserID)) getApplication().sendBroadcast(newIntent);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    @NonNull
    @Override
    public Looper getLooper() {
        return super.getLooper();
    }

    private void sendNotification(String intentString, String hostName, String host) {
        // Sets up the pending intent to update the notification.
        int notificationID = 0;
        long iconID = R.drawable.noti_white_logo;
        int activityIcon = R.drawable.ic_a_outlined;
        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_SPACE;
       // String sTime = Utilities.getTimeString(mCalendar.getTimeInMillis());
        String sChannelID = Constants.ATRACKIT_EMPTY;

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        boolean bIsGym = false;
        Bundle workoutBundle = new Bundle();
        String sWorkoutKey = Workout.class.getSimpleName();
        String sWorkoutSetKey = WorkoutSet.class.getSimpleName();
        if ((mWorkout != null) && (mWorkout.start > 0L)) {
            workoutBundle.putParcelable(sWorkoutKey, mWorkout);
            bIsGym = Utilities.isGymWorkout(mWorkout.activityID);
        }
        if ((mWorkoutSet != null) && (mWorkoutSet.start > 0L)) workoutBundle.putParcelable(sWorkoutSetKey, mWorkoutSet);
        
        Context context = getApplicationContext();
        Intent viewIntent = new Intent(context, MainActivity.class);
        viewIntent.setAction(Intent.ACTION_VIEW);
        viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        viewIntent.putExtra(KEY_FIT_NAME, hostName);
        viewIntent.putExtra(Constants.KEY_FIT_HOST, host);
        viewIntent.putExtra(KEY_FIT_TYPE, 1); // external
        if (!workoutBundle.isEmpty()) viewIntent.putExtra(Constants.KEY_FIT_BUNDLE, workoutBundle);
        

        Intent pauseIntent = new Intent(context, MainActivity.class);
        if (!bIsGym) {
            if (intentString.equals(INTENT_ACTIVE_PAUSE))
                pauseIntent.setAction(INTENT_ACTIVE_RESUMED);
            else
                pauseIntent.setAction(INTENT_ACTIVE_PAUSE);
        }else {
            if (intentString.equals(INTENT_ACTIVE_PAUSE) ||
                    intentString.equals(INTENT_ACTIVESET_STOP))
                pauseIntent.setAction(INTENT_ACTIVESET_START);
            else
                pauseIntent.setAction(INTENT_ACTIVESET_STOP);
        }
        pauseIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pauseIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        pauseIntent.putExtra(KEY_FIT_NAME, hostName);
        pauseIntent.putExtra(Constants.KEY_FIT_HOST, host);
        pauseIntent.putExtra(KEY_FIT_TYPE, 1); // external
        if (!workoutBundle.isEmpty()) pauseIntent.putExtra(Constants.KEY_FIT_BUNDLE, workoutBundle);
        Intent stopIntent = new Intent(context, MainActivity.class);
        stopIntent.setAction(INTENT_ACTIVE_STOP);
        stopIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        stopIntent.putExtra(KEY_FIT_NAME, hostName);
        stopIntent.putExtra(Constants.KEY_FIT_HOST, host);
        stopIntent.putExtra(KEY_FIT_TYPE, 1); // external
        if (!workoutBundle.isEmpty()) stopIntent.putExtra(Constants.KEY_FIT_BUNDLE, workoutBundle);

        switch (intentString) {
            case INTENT_ACTIVE_START:
            case INTENT_ACTIVE_RESUMED:
            case INTENT_ACTIVE_PAUSE:
            case INTENT_ACTIVESET_START:
            case INTENT_ACTIVESET_STOP:
                notificationID = NOTIFICATION_ACTIVE_ID;
                PendingIntent viewPendingIntent = PendingIntent.getActivity(context,
                        notificationID, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action actionView =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                getString(R.string.action_open), viewPendingIntent)
                                .build();

                PendingIntent pausePendingIntent = PendingIntent.getActivity(context,
                        notificationID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Create the action
                NotificationCompat.Action actionPause;
                if (!bIsGym) {
                    if (intentString.equals(INTENT_ACTIVE_PAUSE))
                        actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_play_button_white,
                            getString(R.string.action_resume), pausePendingIntent)
                            .build();
                    else
                        actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_rounded_pause_button_white,
                                getString(R.string.action_pause), pausePendingIntent)
                                .build();

                }else {
                    if (intentString.equals(INTENT_ACTIVESET_START) || intentString.equals(INTENT_ACTIVE_START) || intentString.equals(INTENT_ACTIVE_RESUMED))
                        actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_last_track_right_arrow_white,
                                getString(R.string.action_stop_set), pausePendingIntent)
                                .build();
                    else
                        actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_right_arrow_outline,
                                getString(R.string.action_continue), pausePendingIntent)
                                .build();
                }
                PendingIntent stopPendingIntent = PendingIntent.getActivity(context,
                        notificationID, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action actionFinish =
                        new NotificationCompat.Action.Builder(R.drawable.ic_stop_circle_white,
                                getString(R.string.action_finish), stopPendingIntent)
                                .build();
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
          //      NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView).addAction(actionFinish);
                // extender.setContentIcon(Math.toIntExact(iconID));
                NotificationCompat.Builder notifyBuilder =  getNotificationBuilder(intentString, hostName);
                //notifyBuilder.addAction(actionView);
                notifyBuilder.addAction(actionPause);
                notifyBuilder.addAction(actionFinish);
                try{
                    // Deliver the notification.
                    mNotifyManager.notify(notificationID, notifyBuilder.build());
                }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage());
                }
                break;
            case INTENT_ACTIVE_STOP:
                notificationID = NOTIFICATION_ACTIVE_ID;
                sTitle =  "Completed " + Utilities.getPartOfDayString(mWorkout.start) + Constants.ATRACKIT_SPACE + mWorkout.activityName;
                sContent = mReferenceTools.workoutShortText(mWorkout);
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                mNotifyManager.cancel(notificationID);
                PendingIntent pendingViewIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, viewIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                if (mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID) > 0)
                    activityIcon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
             //   Drawable drawable = getResources().getDrawable(activityIcon, null);
             //   Bitmap bitmap = vectorToBitmap(drawable);
                NotificationCompat.Action actionReView =
                        new NotificationCompat.Action.Builder(activityIcon,
                                getString(R.string.action_review), pendingViewIntent)
                                .build();
             //   NotificationCompat.WearableExtender extender2 = new NotificationCompat.WearableExtender().addAction(actionReView);
                //     extender2.setContentIcon(Math.toIntExact(iconID));
                NotificationCompat.Builder notifyBuilder2 = getNotificationBuilder(intentString, hostName);
                notifyBuilder2.addAction(actionReView);
                if (bIsGym){
                    notifyBuilder2.setProgress(0,0,false);
                }
                try {
                    if (mNotifyManager != null)
                        mNotifyManager.notify(notificationID, notifyBuilder2.build());
                }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage());
                }
                break;
/**
*             resultData.putLong(KEY_FIT_WORKOUTID,mWorkout._id);
*             resultData.putString(KEY_FIT_TYPE, GOAL_TYPE_BPM);
*             resultData.putLong(KEY_FIT_ACTION, bpmCounter.GoalCount);
*             resultData.putLong(Constants.KEY_FIT_VALUE, bpmCounter.LastCount);
*             sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
*             bpmCounter.GoalActive = 0;
 */
            case INTENT_GOAL_TRIGGER:
                notificationID = NOTIFICATION_GOAL_ID;
                PendingIntent pendingViewItent3 = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, viewIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                activityIcon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);


                //  extender3.setContentIcon(Math.toIntExact(activityIcon));
/*                String goal_type = dataBundle.getString(KEY_FIT_TYPE);
                long goal_goal_count = dataBundle.getLong(KEY_FIT_ACTION);
                long goal_last_count = dataBundle.getLong(KEY_FIT_VALUE);
                NotificationCompat.Builder notifyBuilder3 = getNotificationBuilder(intentString, dataBundle);
                int goal_icon; String goal_action_msg;
                if (goal_type.equals(GOAL_TYPE_BPM)){
                    sTitle = "BPM Goal Achieved";
                    sContent = "Achieved " + goal_last_count + " bpm";
                }
                if (goal_type.equals(GOAL_TYPE_STEPS)){
                    sTitle = "Steps Goal Achieved";
                    sContent = "Achieved " + goal_last_count + " steps";
                }
                if (goal_type.equals(GOAL_TYPE_DURATION)){
                    sTitle = "Duration Goal Achieved";
                    sContent = "Achieved " + goal_last_count + " mins";
                }
                notifyBuilder3.setContentText(sContent);
                notifyBuilder3.setContentTitle(sTitle);
                NotificationCompat.Action actionView3 =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                getString(R.string.action_open), pendingViewItent3)
                                .build();
                NotificationCompat.WearableExtender extender3 = new NotificationCompat.WearableExtender().addAction(actionView3);
                notifyBuilder3.extend(extender3);
                try{
                    if (mNotifyManager != null) mNotifyManager.notify(notificationID, notifyBuilder3.build());
                }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage());
                }*/
                break;
            case INTENT_SETUP:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                sTitle =  getString(R.string.action_setup_running);
                sContent = getString(R.string.app_name);
                sChannelID = MAINTAIN_CHANNEL_ID;
                PendingIntent pendingViewIntent5 = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, viewIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action actionView5 =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                getString(R.string.action_open), pendingViewIntent5)
                                .build();
              //  NotificationCompat.WearableExtender extender5 = new NotificationCompat.WearableExtender().addAction(actionView5);
                NotificationCompat.Builder notifyBuilder5 = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon(Math.toIntExact(iconID))
                        .setUsesChronometer(false)
                        .setOngoing(true)
                        .setContentInfo(getString(R.string.action_setup_running))
                        .setAutoCancel(true)
                     //   .extend(extender5)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                notifyBuilder5.addAction(actionView5);
                try{
                    if (mNotifyManager != null) mNotifyManager.notify(notificationID, notifyBuilder5.build());
                }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage());
                }
                break;
            case INTENT_SCHEDULE_TRIGGER:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                break;
            case INTENT_WORKOUT_REPORT:
                notificationID = NOTIFICATION_SUMMARY_ID;
/*                Workout workout2 = dataBundle.getParcelable(Workout.class.getSimpleName());
                if (workout2 == null) return;
                Intent reportingIntent = new Intent(INTENT_SUMMARY_WORKOUT);
                reportingIntent.setAction(INTENT_SUMMARY_WORKOUT);
                reportingIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout2._id);
                reportingIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                reportingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reportingIntent.putExtra(KEY_FIT_TYPE, 1); // starting from notification flag
                PendingIntent reportPendingIntent = PendingIntent.getActivity(context,
                        notificationID, reportingIntent, 0PendingIntent.FLAG_ONE_SHOT);*/
//                notifyBuilder.addAction(R.drawable.ic_action_attarget_vector_dark,
//                        getString(R.string.note_report_review), reportPendingIntent);
                break;
            default:
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String intentString, String hostName) {
        // Set up the pending intent that is delivered when the notification
        // is clicked.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notificationIntent.putExtra(KEY_FIT_TYPE, 1);
        PendingIntent notificationPendingIntent;
        NotificationCompat.Builder notifyBuilder;
        Bitmap ic_launcher = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        int notificationID = 0;
        long iconID = R.drawable.noti_white_logo;
        int activityIcon = R.drawable.ic_running_white;
        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_EMPTY;
        String sTime = SimpleDateFormat.getTimeInstance().format(mCalendar.getTime());
        String sChannelID = Constants.ATRACKIT_EMPTY;
        long startElapsed = SystemClock.elapsedRealtime();
        boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
        switch (intentString) {
            case INTENT_ACTIVE_START:
            case INTENT_ACTIVE_RESUMED:
            case INTENT_ACTIVE_PAUSE:
            case INTENT_ACTIVESET_START:
            case INTENT_ACTIVESET_STOP:
                sTitle =  hostName + getString(R.string.my_tracking_string) + mWorkout.activityName;
                if (intentString.equals(INTENT_ACTIVE_PAUSE) || intentString.equals(INTENT_ACTIVESET_STOP))
                    if (intentString.equals(INTENT_ACTIVESET_STOP)) {
                        sContent += "Finished ";
                        if (bGym) sContent += "Set ";
                    }else
                        sContent += "Paused ";
                if (intentString.equals(INTENT_ACTIVE_RESUMED))
                    sContent += "Resumed ";
                if (intentString.equals(INTENT_ACTIVE_START) || intentString.equals(INTENT_ACTIVESET_START))
                    if (intentString.equals(INTENT_ACTIVESET_START)) {
                        sContent += "Started ";
                        if (bGym) sContent += "Set ";
                    }else
                        sContent += "Started ";

                startElapsed = mWorkout.start;
                if (bGym){
                    if (mWorkoutSet != null) {
                        if (mWorkoutSet.exerciseName != null && mWorkoutSet.exerciseName.length() > 0)
                            sContent += mWorkoutSet.exerciseName;
                        else
                            sContent += mWorkout.activityName;
                        if (mWorkoutSet.start > 0) startElapsed = mWorkoutSet.start;
                    }else{
                        sContent += mWorkout.activityName;
                    }
                }else
                    sContent += mWorkout.activityName;

                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationIntent.putExtra(KEY_FIT_WORKOUTID, mWorkout._id);
                notificationIntent.setAction(Intent.ACTION_VIEW);
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
//                if (intentString.equals(INTENT_ACTIVE_PAUSE))
//                    startElapsed = mSavedStateViewModel.getPauseStart();
                String sState = mReferenceTools.currentWorkoutStateString(mWorkout);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                if ((mSets!=null) && (mSets.size() > 1)){
                    for (WorkoutSet set : mSets){
                        inboxStyle.addLine(mReferenceTools.workoutSetShortText(set));
                    }
                }else
                    inboxStyle.addLine(mReferenceTools.workoutSetShortText(mWorkoutSet));

                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon((int) iconID)
                        .setLargeIcon(ic_launcher)
                        .setWhen(startElapsed)
                        .setShowWhen(true)
                        .setStyle(inboxStyle)
                        .setOngoing(true)
                        .setContentInfo(sState)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                
                break;
            case INTENT_ACTIVE_STOP:
                sTitle =  "Completed " + Utilities.getPartOfDayString(mWorkout.start) + Constants.ATRACKIT_SPACE + mWorkout.activityName;
                sContent = mReferenceTools.workoutShortText(mWorkout);
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;

                notificationIntent.putExtra(KEY_FIT_WORKOUTID, mWorkout._id);
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                activityIcon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), activityIcon);
                Bitmap bitmap = vectorToBitmap(drawable);

                // 2. Build the BIG_PICTURE_STYLE.
                NotificationCompat.BigTextStyle BigTextStyle = new NotificationCompat.BigTextStyle()
                        // Provides the bitmap for the BigPicture notification.
                        .bigText(mReferenceTools.workoutNotifyText(mWorkout))
                        // Overrides ContentTitle in the big form of the template.
                        .setBigContentTitle(sTitle)
                        // Summary line after the detail section in the big form of the template.
                        .setSummaryText(sContent);
                // NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setLargeIcon(bitmap)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon((int)iconID)
                        .setOngoing(false)
                        .setUsesChronometer(false)
                        .setStyle(BigTextStyle)
                        //  .setContentInfo(workout2.shortText())
                        .setAutoCancel(false).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
/*            case INTENT_ACTIVE_PAUSE:
                break;
            case INTENT_ACTIVE_RESUMED:
                break;
            case INTENT_ACTIVESET_START:
                break;
            case INTENT_ACTIVESET_STOP:
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(R.drawable.noti_white_logo)
                        .setAutoCancel(false).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;*/
            case INTENT_GOAL_TRIGGER:
                notificationID = NOTIFICATION_GOAL_ID;
                sChannelID = GOALS_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notify_channel_goals_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_SCHEDULE_TRIGGER:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                sChannelID = MAINTAIN_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                         .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_WORKOUT_REPORT:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                 
                sTitle = (mWorkout.start > 0) ? Utilities.getPartOfDayString(mWorkout.start) : "";
                sTitle += Constants.ATRACKIT_SPACE + mWorkout.activityName;
                sContent =  mReferenceTools.workoutShortText(mWorkout);
                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_SUMMARY_DAILY:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                sTitle =  String.format(Locale.getDefault(),getString(R.string.note_summary_title), sTime);
 /*               sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_desc), dataBundle.getInt(Constants.MAP_MOVE_MINS), dataBundle.getFloat(Constants.MAP_DISTANCE),
                        dataBundle.getFloat(Constants.MAP_BPM_AVG),  dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_MIN), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(Constants.MAP_STEPS));
 */               // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                         .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            default:
                notificationID = NOTIFICATION_FIREBASE_ID;
                sChannelID = FIREBASE_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                         .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
        }
        return notifyBuilder;
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

    private void sendMessage(final String sPath, final String host, final DataMap dataMap) {
        if (host != null && host.length() > 0) {
            // Instantiates clients without member variables, as clients are inexpensive to
            // create. (They are cached and shared between GoogleApi instances.)
            Task<Integer> sendMessageTask =
                    Wearable.getMessageClient(
                            getApplicationContext()).sendMessage(
                            host,
                            sPath,
                            dataMap.toByteArray());

            sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                @Override
                public void onComplete(Task<Integer> task) {
                    if (task.isSuccessful()) {
                        Log.w(LOG_TAG, "Message sent successfully " + sHostName);
                    } else {
                        Log.i(LOG_TAG, "Message failed." + sHostName);
                    }
                }
            });
        }

    }

    private void broadcastToast(String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyWearListenerService.this.getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }
}