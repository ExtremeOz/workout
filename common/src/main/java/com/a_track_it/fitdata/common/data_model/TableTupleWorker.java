package com.a_track_it.fitdata.common.data_model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.a_track_it.fitdata.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;

public class TableTupleWorker extends Worker {
    private static String LOG_TAG = TableTupleWorker.class.getSimpleName();
    private WorkoutRepository workoutRepository;

    public TableTupleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workoutRepository =  InjectorUtils.getWorkoutRepository((Application) context);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<TableTuple> tableTuples = new ArrayList<>();
        List<Long> listWorkouts = new ArrayList<>();
        Context context = getApplicationContext();
        Data inputData = getInputData();
        final String sUserID = inputData.getString(Constants.KEY_FIT_USER);
        final String sDevice = inputData.getString(KEY_FIT_DEVICE_ID);
        String sTable = inputData.getString(Constants.MAP_DATA_TYPE);
        long lCount = inputData.getLong(Constants.MAP_COUNT,0);  // incoming TableTuple values
        long lMin = inputData.getLong(Constants.MAP_START,0);
        long lMax = inputData.getLong(Constants.MAP_END,0);
        final String sAction = inputData.getString(KEY_FIT_ACTION);
        final String sPath = inputData.getString(Constants.KEY_FIT_REC);  // pickup the return path
        Log.d(LOG_TAG, "doWork " + sAction + " " + sTable + " path " + sPath);
        try {
            if (sAction.equals(Constants.INTENT_PHONE_SYNC)) {
                tableTuples = workoutRepository.getTableTuples(sTable, sUserID, sDevice);
                if (tableTuples != null && tableTuples.size() > 0) {
                    TableTuple tuple = tableTuples.get(0);
                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                    dataMapWear.getDataMap().putLong(Constants.MAP_START, tuple.mindate);
                    dataMapWear.getDataMap().putLong(Constants.MAP_END, tuple.maxdate);
                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                        @Override
                        public void onComplete(@NonNull Task<DataItem> task) {
                            boolean bSuccess = task.isSuccessful();
                            Log.d(LOG_TAG, "TableTupleWorker for dataPutItem " + tuple.table_name + " on " + sPath + " INTENT_PHONE_SYNC: " + bSuccess);
                        }
                    });
                  //  }
                }
            }
            // request for information about table
            if (sAction.equals(Constants.INTENT_PHONE_REQUEST)) {
                Gson gson = new Gson();
                List<WorkoutSet> listSets = new ArrayList<>();
                // swapping to data item received path to added to their DB using DataSyncWorker
                if (sTable.equals(Bodypart.class.getSimpleName())) {
                    if (lCount > 0) {
                        Log.d(LOG_TAG, "doWork find BP >  " + lCount + " " + sTable);
                        List<Bodypart> list2 = workoutRepository.getBodyPartGreaterThanID(lCount);
                        if (list2 != null && list2.size() > 0) {
                            Log.e(LOG_TAG, "sending bodypart > " + lCount + " " + sTable + " " + list2.size());
                            for (Bodypart bodypart : list2) {
                                String sObject = gson.toJson(bodypart, Bodypart.class);
                                if (sObject != null && sObject.length() > 0) {
                                    Log.e(LOG_TAG, "sending bodypart  " + sObject);
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(Bodypart.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker bodypart " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }
                    }
                    Log.d(LOG_TAG, "doWork BP since >  " + Utilities.getTimeDateString(lMax) + " " + sTable);
                    List<Bodypart> list = workoutRepository.getBodypartsSince(2, lMax); // updated
                    if (list != null && list.size() > 0) {
                        Log.e(LOG_TAG, "sending bodypart data " + sPath + " " + sTable + " " + list.size());
                        for (Bodypart bodypart : list) {
                            String sObject = gson.toJson(bodypart, Bodypart.class);
                            if (sObject != null && sObject.length() > 0) {
                                Log.e(LOG_TAG, "sending bodypart  " + sObject);
                                PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                dataMapWear.getDataMap().putString(Bodypart.class.getSimpleName(), sObject);
                                PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataItem> task) {
                                        boolean bSuccess = task.isSuccessful();
                                        Log.d(LOG_TAG, "TableTupleWorker bodypart " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                    }
                                });
                            }
                        }
                    }else
                        Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                }
                if (sTable.equals(Exercise.class.getSimpleName())) {
                    if (lCount > 0) {
                        Log.d(LOG_TAG, "doWork find exercise >  " + lCount + " " + sTable);
                        List<Exercise> list2 = workoutRepository.getExercisesSince(3,lCount); // greater than ID
                        if (list2 != null && list2.size() > 0) {
                            Log.e(LOG_TAG, "sending Exercise > " + lCount + " " + sTable + " " + list2.size());
                            for (Exercise Exercise : list2) {
                                String sObject = gson.toJson(Exercise, Exercise.class);
                                if (sObject != null && sObject.length() > 0) {
                                    Log.e(LOG_TAG, "sending exercise  " + sObject);
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(Exercise.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker Exercise " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }
                    }                    
                    List<Exercise> list = new ArrayList<>();
                    if (lMin < lMax) {
                        list = workoutRepository.getExercisesSince(1, lMin); // trained
                        Log.d(LOG_TAG, "doWork EX since min >  " + Utilities.getTimeDateString(lMin) + " " + sTable);
                    }else {
                        list = workoutRepository.getExercisesSince(2, lMax); // updated
                        Log.d(LOG_TAG, "doWork EX since max >  " + Utilities.getTimeDateString(lMax) + " " + sTable);
                    }

                    if (list != null && list.size() > 0) {
                        Log.e(LOG_TAG, "sending data " + sPath + " " + sTable + " " + list.size());
                        for (Exercise exercise : list) {
                            String sObject = gson.toJson(exercise, Exercise.class);
                            if (sObject != null && sObject.length() > 0) {
                                Log.e(LOG_TAG, "sending exercise  " + sObject);
                                PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                dataMapWear.getDataMap().putString(Exercise.class.getSimpleName(), sObject);
                                PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataItem> task) {
                                        boolean bSuccess = task.isSuccessful();
                                        Log.d(LOG_TAG, "TableTupleWorker exercise " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                    }
                                });
                            }
                        }
                    }else
                        Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                }
                if (sTable.equals(Workout.class.getSimpleName())) {
                    List<Workout> list = new ArrayList<>();
                    if (lMin < lMax)
                        list = workoutRepository.getSessionDataSince(sUserID, sDevice, lMin); // trained
                    else
                        list = workoutRepository.getSessionDataSince(sUserID, sDevice, lMax); // updated
                    if (list != null && list.size() > 0) {
                        Log.e(LOG_TAG, "sending workout data " + sPath + " " + sTable + " " + list.size());
                        for (Workout workout : list) {
                            if (workout != null) {
                                listWorkouts.add(workout._id);
                                String sObject = gson.toJson(workout, Workout.class);
                                Log.w(LOG_TAG, "TableTupleWorker sendDataMessage workout " + sObject);
                                if (sObject != null && sObject.length() > 0) {
                                    Log.e(LOG_TAG, "sending workout  " + sObject);
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(Workout.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker workout " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }
                    }
                    else
                        Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                    sTable = WorkoutSet.class.getSimpleName(); // ensure sets are done for these workouts
                }
                if (sTable.equals(WorkoutSet.class.getSimpleName())) {
                    if (lMin < lMax)
                        listSets = workoutRepository.getSessionSetDataSince(sUserID, sDevice, lMin); // trained
                    else
                        listSets = workoutRepository.getSessionSetDataSince(sUserID, sDevice, lMax); // trained

                    if (listSets != null && listSets.size() > 0) {
                        Log.e(LOG_TAG, "sending data " + sPath + " " + sTable + " " + listSets.size());
                        for (WorkoutSet workoutSet : listSets) {
                            if (workoutSet != null) {
                                String sObject = gson.toJson(workoutSet, WorkoutSet.class);
                                if (sObject != null && sObject.length() > 0) {
                                    if (!listWorkouts.contains(workoutSet.workoutID)){
                                        Workout workout = workoutRepository.getWorkoutByIdNow(workoutSet.workoutID,workoutSet.userID,workoutSet.deviceID);
                                        if (workout != null){
                                            String sObject2 = gson.toJson(workout, Workout.class);
                                            Log.w(LOG_TAG, "TableTupleWorker sendDataMessage workout " + sObject);
                                            if (sObject2 != null && sObject2.length() > 0) {
                                                Log.e(LOG_TAG, "sending workout  " + sObject2);
                                                listWorkouts.add(workout._id);
                                                PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                                dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                                dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                                dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                                dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                                dataMapWear.getDataMap().putString(Workout.class.getSimpleName(), sObject2);
                                                PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                                Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                                dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataItem> task) {
                                                        boolean bSuccess = task.isSuccessful();
                                                        Log.d(LOG_TAG, "TableTupleWorker workout " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    Log.e(LOG_TAG, "sending set  " + sObject);
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(WorkoutSet.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker workoutSet " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }
                    }else
                        Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                }
                if (sTable.equals(ObjectAggregate.class.getSimpleName())){
                    if (lMax > 0){
                        List<ObjectAggregate> list = workoutRepository.getObjectAggregatesSince(sUserID,1L,lMax); // last updated type
                        if (list != null && list.size() > 0) {
                            Log.e(LOG_TAG, "sending ObjectAggregate data " + sPath + " " + sTable + " " + list.size());
                            for (ObjectAggregate aggregate : list) {
                                String sObject = gson.toJson(aggregate, ObjectAggregate.class);
                                if (sObject != null && sObject.length() > 0) {
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(ObjectAggregate.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker ObjectAggregate " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }else
                            Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                    }
                    if (lCount > 0){
                        List<ObjectAggregate> list = workoutRepository.getObjectAggregatesSince(sUserID,2L,lCount); // rowid > lCount
                        if (list != null && list.size() > 0) {
                            Log.e(LOG_TAG, "sending bodypart data " + sPath + " " + sTable + " " + list.size());
                            for (ObjectAggregate aggregate : list) {
                                String sObject = gson.toJson(aggregate, ObjectAggregate.class);
                                if (sObject != null && sObject.length() > 0) {
                                    PutDataMapRequest dataMapWear = PutDataMapRequest.create(sPath);
                                    dataMapWear.getDataMap().putString(KEY_FIT_ACTION, sAction);
                                    dataMapWear.getDataMap().putString(KEY_FIT_USER, sUserID);
                                    dataMapWear.getDataMap().putString(KEY_FIT_DEVICE_ID, sDevice);
                                    dataMapWear.getDataMap().putString(Constants.MAP_DATA_TYPE, sTable);
                                    dataMapWear.getDataMap().putString(ObjectAggregate.class.getSimpleName(), sObject);
                                    PutDataRequest wearRequest = dataMapWear.asPutDataRequest();
                                    Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(wearRequest);
                                    dataItemTask.addOnCompleteListener(new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> task) {
                                            boolean bSuccess = task.isSuccessful();
                                            Log.d(LOG_TAG, "TableTupleWorker bodypart " + sPath + " INTENT_PHONE_REQUEST: " + bSuccess);
                                        }
                                    });
                                }
                            }
                        }else
                            Log.w(LOG_TAG, "no " + sTable + " " + Utilities.getTimeDateString(lMax));
                    }
                }
            }
            // Indicate whether the work finished successfully with the Result
            return Result.success();
        }
        catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }

    }
}
