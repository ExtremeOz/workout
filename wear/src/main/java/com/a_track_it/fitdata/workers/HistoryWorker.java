package com.a_track_it.fitdata.workers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryWorker extends Worker {
    private static final String LOG_TAG = HistoryWorker.class.getSimpleName();
    public HistoryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        long startTime = cal.getTimeInMillis();

        Context context = getApplicationContext();
        ReferencesTools referencesTools = ReferencesTools.getInstance() ;
        referencesTools.init(context);
        try {

/*            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .build();*/
            DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                    .read(DataType.TYPE_ACTIVITY_SEGMENT)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .enableServerQueries() // Used to retrieve data from cloud.
                    .build();
            Task<DataReadResponse> response = Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .readData(dataReadRequest);
             response.addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                 @Override
                 public void onComplete(@NonNull Task<DataReadResponse> task) {
                     if (task.getResult().getStatus().isSuccess()) {
                         boolean wroteDataToCache = false;
                         Bundle resultBundle = new Bundle();
                         Log.i(LOG_TAG, "Successful read session " + task.getResult().getDataSets().size());
                         for (DataSet dataSet : task.getResult().getDataSets()) {
                             //wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
                             List<DataPoint> dpList = dataSet.getDataPoints();
                             for (DataPoint dp : dpList) {
                                 Log.i(LOG_TAG, "received data-point " + dp.getDataType().getName());
                                 if (dp.getDataType().getName().equals("com.google.activity.exercise")) {
                                     WorkoutSet set = new WorkoutSet();
                                     set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                                     if (dp.getEndTime(TimeUnit.MILLISECONDS) > 0) {
                                         set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                                         set.duration = set.end - set.start;
                                     }
                                     set._id = set.start;
                                     String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                                     int iExerciseID = referencesTools.getFitnessActivityIdByText(sExercise);
                                     if (iExerciseID > 0)
                                         set.exerciseID = iExerciseID; // if it fits our list
                                     set.exerciseName = sExercise;
                                     set.resistance_type = dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();

                                     set.weightTotal = dp.getValue(Field.FIELD_WEIGHT).asFloat();
                                     set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
                                     if (set.duration > 0)
                                         set.wattsTotal = set.duration * (set.weightTotal * set.repCount);
                                     set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
                                     set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                                     Log.d(LOG_TAG, "dp " + set.shortText());
                                     wroteDataToCache = true;
                                     resultBundle.putParcelable("WorkoutSet", set);
                                 }
                                 if (dp.getDataType().getName().equals("com.google.activity.sample") || dp.getDataType().getName().equals("com.google.activity.segment")) {
                                     Workout workout = new Workout();
                                     workout.activityID = dp.getValue(Field.FIELD_ACTIVITY).asInt();
                                     workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                                     if (dp.getDataType().getName().equals("com.google.activity.sample")) {
                                         float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
                                         workout.wattsTotal = confidence;
                                         Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence);
                                     }else
                                         Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName);
                                     wroteDataToCache = true;
                                     resultBundle.putParcelable("Workout", workout);
                                 }
                                 if (dp.getDataType().getName().equals("com.google.power.sample")) {
                                     float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
                                     Workout workout = new Workout();
                                     workout.activityName = "Power Sample";
                                     workout.wattsTotal = watts;
                                     Log.d(LOG_TAG, "dp power sample " + watts);
                                     resultBundle.putParcelable("Watts", workout);
                                     wroteDataToCache = true;
                                 }
                             }
                         }
                         if (wroteDataToCache) {
                             Log.d(LOG_TAG, "received activity history " + resultBundle.toString());

                         }
                     }else
                         Log.d(LOG_TAG, "task read not successful " + task.getResult().toString());
                 }
             }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     Log.d(LOG_TAG, "task read not successful " + e.getMessage());
                 }
             });
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, "true")
                    .build();
            return Result.success(outputData);
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }

    }
}
