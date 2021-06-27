package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import static com.a_track_it.workout.common.Constants.KEY_COMM_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TIME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;

public class EntityWorker extends Worker {
    private static final String LOG_TAG = EntityWorker.class.getSimpleName();
    private ConfigurationDao configurationDao;
    private ApplicationPreferences appPrefs;

    public EntityWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Context c = context.getApplicationContext();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(c);
        appPrefs = ApplicationPreferences.getPreferences(c);
        configurationDao = db.configDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        int iResult = 0;
        Data data = getInputData();
        String intentAction = data.getString(Constants.KEY_FIT_ACTION);
        int intentType = data.getInt(Constants.KEY_FIT_TYPE,0);
        String sUserID = data.getString(Constants.KEY_FIT_USER);
        final long timeMs = System.currentTimeMillis();
        Log.e(LOG_TAG, "received action " + intentAction + " " + intentType);
        try{
            if (intentAction.equals(Configuration.class.getSimpleName())){
                String sValue = data.getString(Constants.KEY_FIT_DEVICE_ID);
                String sValue1 = data.getString(Constants.KEY_DEVICE1);
                String sValue2 = data.getString(Constants.KEY_DEVICE2);
                String sName = data.getString(Constants.KEY_FIT_NAME);
                String sHost = data.getString(Constants.KEY_FIT_HOST);
                String sReturnPath = (data.hasKeyWithValueOfType(Constants.KEY_PAYLOAD,String.class) ? data.getString(Constants.KEY_PAYLOAD) : null);
                long lValue = data.getLong(Constants.KEY_FIT_TIME, 0);
                List<Configuration> list2 = configurationDao.getConfigByNameUser(Constants.KEY_DEVICE2, sUserID);
                List<Configuration> list = configurationDao.getConfigByNameUser(Constants.KEY_DEVICE1, sUserID);
                if (intentType == 0) {
                    if (list != null && list.size() > 0) {
                        Configuration configDevice = list.get(0);
                        DataMap dataMap = new DataMap();
                        dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                        dataMap.putInt(Constants.KEY_FIT_TYPE, intentType);
                        dataMap.putString(KEY_FIT_USER, sUserID);
                        dataMap.putString(KEY_FIT_DEVICE_ID, configDevice.stringValue);
                        dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                        UserPreferences userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                        long lastLogin = userPrefs.getLastUserSignIn();
                        dataMap.putLong(Constants.KEY_FIT_VALUE, lastLogin);
                        dataMap.putLong(Constants.KEY_FIT_TIME, appPrefs.getLastSync());
                        if (sHost != null && sReturnPath != null) {
                            Log.e(LOG_TAG, "sending COMM_TYPE_REQUEST_INFO " + intentType + " Message " + sReturnPath);
                            Task<Integer> sendMessageTask =
                                    Wearable.getMessageClient(
                                            getApplicationContext()).sendMessage(
                                            sHost,
                                            sReturnPath,
                                            dataMap.toByteArray());

                            sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                                @Override
                                public void onComplete(Task<Integer> task) {
                                    if (task.isSuccessful()) {
                                        Log.w(LOG_TAG, "COMM_TYPE_REQUEST_INFO 0 Message sent successfully " + sHost);
                                    } else {
                                        Log.e(LOG_TAG, "COMM_TYPE_REQUEST_INFO 0 Message failed." + sHost);
                                    }
                                }
                            });
                        }
                    }
                }
                if ((intentType == 1)||(intentType == 2)){  // Load Device2 Reply with DEVICE1 info
                    if (list2 == null || list2.size() == 0) {
                        Configuration configuration = new Configuration();
                        configuration.userValue = sUserID;
                        configuration.stringName = Constants.KEY_DEVICE2;
                        configuration.longValue = 0;
                        configuration.stringValue = sValue;
                        configuration.stringValue1 = sValue1;
                        configuration.stringValue2 = sValue2;
                        Log.e(LOG_TAG, "insert " + configuration.toString());
                        configurationDao.insert(configuration);
                    } else {
                        Configuration configuration = list2.get(0);
                        boolean bUpdated = ((configuration.longValue == 0) || (!sValue1.equals(configuration.stringValue1)) || (!sValue2.equals(configuration.stringValue2)));
                        if (bUpdated) {
                            configuration.stringValue = sValue;
                            configuration.longValue = timeMs;
                            configuration.stringValue1 = sValue1;
                            configuration.stringValue2 = sValue2;
                            configurationDao.update(configuration);
                        }
                    }
                    if ((sReturnPath != null) && (sReturnPath.length() > 0)){
                        if (list != null && list.size() > 0) {
                            Configuration configDevice = list.get(0);
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                            dataMap.putInt(Constants.KEY_FIT_TYPE, 2); // Reply with DEVICE1 info - no return wanted
                            dataMap.putString(KEY_FIT_USER, sUserID);
                            dataMap.putString(KEY_FIT_DEVICE_ID, configDevice.stringValue);
                            dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                            dataMap.putLong(KEY_FIT_TIME, configDevice.longValue);
                            dataMap.putString(Constants.KEY_DEVICE1, configDevice.stringValue1);
                            dataMap.putString(Constants.KEY_DEVICE2, configDevice.stringValue2);
                            UserPreferences userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                            long lastLogin = userPrefs.getLastUserSignIn();
                            dataMap.putLong(Constants.KEY_FIT_VALUE, lastLogin);
                            dataMap.putLong(Constants.KEY_FIT_TIME, appPrefs.getLastPhoneSync());
                            Log.e(LOG_TAG, "sending COMM_TYPE_REQUEST_INFO 2 Message " + sReturnPath);
                            Task<Integer> sendMessageTask =
                                    Wearable.getMessageClient(
                                            getApplicationContext()).sendMessage(
                                            sHost,
                                            sReturnPath,
                                            dataMap.toByteArray());

                            sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                                @Override
                                public void onComplete(Task<Integer> task) {
                                    if (task.isSuccessful()) {
                                        Log.w(LOG_TAG, "Message sent successfully " + sHost);
                                    } else {
                                        Log.i(LOG_TAG, "Message failed." + sHost);
                                    }
                                }
                            });
                        }
                    }else
                        Log.e(LOG_TAG, "sending COMM_TYPE_REQUEST_INFO Message " + intentType);
                }
            }
            iResult = 1;
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, "error " + e.getMessage());
        }
        Data outputData = new Data.Builder()
                .putString(Constants.KEY_FIT_ACTION, intentAction)
                .putString(Constants.KEY_FIT_USER, sUserID)
                .putInt(Constants.KEY_RESULT, iResult)
                .build();
        if (iResult == 1)
            return Result.success(outputData);
        else
            return Result.failure(outputData);

    }
}
