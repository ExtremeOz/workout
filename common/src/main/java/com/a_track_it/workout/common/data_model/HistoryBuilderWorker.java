package com.a_track_it.workout.common.data_model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.InjectorUtils;
import com.a_track_it.workout.common.ReferencesTools;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;

public class HistoryBuilderWorker extends Worker {
    private static final String LOG_TAG = HistoryBuilderWorker.class.getSimpleName();
    private WorkoutRepository mRepository;
    private ReferencesTools referencesTools;
    private Gson gson;
    Calendar calendar;
//    ArrayList<Workout> workoutArrayList = new ArrayList<>();
//    ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
//    ArrayList<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();

    public HistoryBuilderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        referencesTools = ReferencesTools.setInstance(context);
        mRepository = InjectorUtils.getWorkoutRepository((Application) context);
        gson = new Gson();
        calendar = Calendar.getInstance();

    }
    /** startWork
     read un-synchronized workouts and sets - read summaries and insert history
     **/
    @NonNull
    @Override
    public Result doWork() {
        String intentAction = getInputData().getString(Constants.KEY_FIT_ACTION);
        String sUserID = getInputData().getString(Constants.KEY_FIT_USER);
        String sDeviceID = getInputData().getString(Constants.KEY_FIT_DEVICE_ID);
        long workoutID = getInputData().getLong(KEY_FIT_WORKOUTID, 0);
        List<OneTimeWorkRequest> requestList = new ArrayList<>();

        Workout w = new Workout();
        WorkoutSet set = new WorkoutSet();
        WorkoutMeta meta = new WorkoutMeta();
        List<WorkoutSet> workoutSets = new ArrayList<>();
        long setSize = 0L;
        int iRetVal = 0; int setsCount = 0;

        try {
            w = mRepository.getWorkoutByIdNow(workoutID, sUserID,sDeviceID);
            workoutSets = mRepository.getWorkoutSetsByWorkoutIdNow(workoutID, sUserID,sDeviceID);
            meta = mRepository.getWorkoutMetaByWorkoutIdNow(workoutID, sUserID,sDeviceID);
            setSize = workoutSets.size();
            if ((w._id > 0) && (w.activityID > 0)){
                calendar.setTimeInMillis(w.start);
                long startTime = calendar.getTimeInMillis();
                for (int iWorkoutCount = 1; iWorkoutCount < 100; iWorkoutCount++){
                    w.start = getNewStart(startTime, w.start);
                    w._id = w.start;
                    Workout finder = mRepository.getWorkoutByIdNow(w._id, w.userID,w.deviceID);
                    if ((finder == null) || (finder._id == 0L)) {
                        w.end = getNewStart(startTime, w.end);
                        mRepository.insertWorkout(w);
                        ++iRetVal;
                     //   Log.w(LOG_TAG, "adding " + w.shortText());
                        if ((meta != null) && (meta.activityID > 0)) {
                            meta.workoutID = w._id;
                            meta.start = getNewStart(startTime, meta.start);
                            meta.end = getNewStart(startTime, meta.end);
                            mRepository.insertWorkoutMeta(meta);
                        }
                        if ((set != null) && (set.activityID > 0)) {
                            set.start = getNewStart(startTime, set.start);
                            set._id = set.start;
                            set.workoutID = w._id;
                            set.end = getNewStart(startTime, set.end);
                            mRepository.insertWorkoutSet(set);
                            ++setsCount;
                        }
                        if (setSize > 1) {
                            for (int i = 0; i < setSize; i++) {
                                workoutSets.get(i).start = getNewStart(startTime, workoutSets.get(i).start);
                                workoutSets.get(i)._id = workoutSets.get(i).start;
                                workoutSets.get(i).workoutID = w._id;
                                workoutSets.get(i).end = getNewStart(startTime, workoutSets.get(i).end);
                                if (workoutSets.get(i).activityID > 0) {
                                    mRepository.insertWorkoutSet(workoutSets.get(i));
                                    ++setsCount;
                                }
                            }

                        }
                        Data.Builder builder = new Data.Builder();
                        builder.putString(KEY_FIT_USER, w.userID);
                        builder.putLong(KEY_FIT_WORKOUTID, w._id);
                        OneTimeWorkRequest workRequest =
                                new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                        .setInputData(builder.build())
                                        .build();
                        requestList.add(workRequest);
                    }else{
                        Log.w(LOG_TAG, "found existing " + referencesTools.workoutShortText(w));
                    }
                    calendar.add(Calendar.DAY_OF_YEAR,-5);
                    startTime = calendar.getTimeInMillis();
                }
                if (requestList.size() > 0){
                    WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                    mWorkManager.enqueue(requestList);
/*
                    .  .getState().observe(HistoryBuilderWorker.this, new Observer<Operation.State>() {
                        @Override
                        public void onChanged(Operation.State state) {
                            if (state instanceof Operation.State.SUCCESS) {
                                Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                                refreshIntent.putExtra(KEY_FIT_ACTION, "changeState");
                                refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_PENDING);
                                getApplicationContext().sendBroadcast(refreshIntent);
                                Log.d(LOG_TAG, "successful state of session cleanup worker ");
                            }
                        }
                    });
*/
                }
            }
            mRepository.destroyInstance();
            Log.e(LOG_TAG, "data sync updated " + iRetVal);
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_FIT_ACTION, intentAction)
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .putInt(Constants.KEY_FIT_SETS, setsCount)
                    .build();
           // Log.e(LOG_TAG, outputData.);
            return Result.success(outputData);
        }
        catch (Exception e){
            iRetVal = 0;
            mRepository.destroyInstance();
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_FIT_ACTION, intentAction)
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .putInt(Constants.KEY_FIT_SETS, setsCount)
                    .build();
            return Result.failure(outputData);
        }
    }
    private long getNewStart(long beginTime, long currentStart){
        calendar.setTimeInMillis(currentStart);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(Calendar.MINUTE);
        int currentSec = calendar.get(Calendar.SECOND);
        calendar.setTimeInMillis(beginTime);
        calendar.set(Calendar.HOUR_OF_DAY, currentHour);
        calendar.set(Calendar.MINUTE, currentMin);
        calendar.set(Calendar.SECOND, currentSec);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();

    }
}
