package com.a_track_it.fitdata.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.ConfigurationDao;
import com.a_track_it.fitdata.common.data_model.ObjectAggregate;
import com.a_track_it.fitdata.common.data_model.ObjectAggregateDao;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutDao;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutMetaDao;
import com.a_track_it.fitdata.common.data_model.WorkoutReport;
import com.a_track_it.fitdata.common.data_model.WorkoutRoomDatabase;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutSetDao;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReadCacheIntentService extends IntentService {

    private WorkoutReport workoutReport;
    public final static String TAG = ReadCacheIntentService.class.getSimpleName();
    private ResultReceiver mReceiver;
    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
    private ObjectAggregateDao mAggregateDao;
    private ConfigurationDao mConfigDao;
    private Calendar mCalendar;
    public ReadCacheIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        mCalendar = Calendar.getInstance();
        workoutReport = new WorkoutReport();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context);
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
        mAggregateDao = db.aggregateDao();
        mConfigDao = db.configDao();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        ApplicationPreferences applicationPreferences =  ApplicationPreferences.getPreferences(context);
        int reportType = intent.getIntExtra(Constants.KEY_FIT_TYPE, 0);
        String sUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
        String sDeviceID = intent.getStringExtra(Constants.KEY_FIT_DEVICE_ID);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        long timeMs = System.currentTimeMillis();
        long startTime = (intent.hasExtra(Constants.MAP_START) ? intent.getLongExtra(Constants.MAP_START, 0L) : 0L);
        long endTime = (intent.hasExtra(Constants.MAP_END) ? intent.getLongExtra(Constants.MAP_END, 0L) : 0L);
/*        if (startTime == 0)
            startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
        else
            startTime = Utilities.getDayStart(mCalendar,startTime);
        if (Utilities.getDayEnd(mCalendar, startTime) > timeMs)
            endTime = timeMs;
        else
            endTime = Utilities.getDayEnd(mCalendar,startTime);*/

        long workoutID = (intent.hasExtra(Constants.KEY_FIT_WORKOUTID) ? intent.getLongExtra(Constants.KEY_FIT_WORKOUTID, 0L) : 0L);
        int activityID = (intent.hasExtra(Constants.MAP_DATA_TYPE) ? intent.getIntExtra(Constants.MAP_DATA_TYPE, 0) : 0);
        mReceiver = intent.getParcelableExtra(Constants.KEY_FIT_REC);
        ArrayList<Workout> report = new ArrayList<>();
        ArrayList<WorkoutSet> reportSets = new ArrayList<>();
        ArrayList<WorkoutMeta> reportMeta = new ArrayList<>();
        ArrayList<ObjectAggregate> reportAggWorkout = new ArrayList<>();
        ArrayList<ObjectAggregate> reportAggBodypart = new ArrayList<>();
        ArrayList<ObjectAggregate> reportAggExercise = new ArrayList<>();
        ArrayList<Long> bodypartList = new ArrayList<>();
        ArrayList<Long> exerciseList = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_FIT_TYPE, reportType);
        bundle.putString(Constants.KEY_FIT_USER, sUserID);

        if ((sUserID == null) || (sUserID.length() == 0))  sUserID = applicationPreferences.getLastUserID();

        if ((sUserID.length() > 0) && ((sDeviceID == null) || (sDeviceID.length() == 0))){
            List<Configuration> list = mConfigDao.getConfigByNameUser(Constants.KEY_DEVICE1,sUserID);
            if (list.size() > 0)
                sDeviceID = list.get(0).stringValue2;
            else
                sDeviceID = null;
        }

        if ((sUserID == null) || (sUserID.length() == 0)){
            Log.e(ReadCacheIntentService.class.getSimpleName(), "UserID or Device ID is null");
        }
        else {
            workoutReport.setUserID(sUserID);
            if (intent.hasExtra("TimeFrame")) {
                Utilities.TimeFrame mTimeFrame = (Utilities.TimeFrame) intent.getSerializableExtra("TimeFrame");
                if (startTime == 0) {
                    startTime = Utilities.getTimeFrameStart(mTimeFrame);
                    endTime = Utilities.getTimeFrameEnd(mTimeFrame);
                }
            }
            // date range
            if (reportType == 0) {
                List<Workout> aList = (workoutID == 0) ? mWorkoutDao.getAllWorkoutByStarts(sUserID, startTime, endTime): mWorkoutDao.getWorkoutById(workoutID);
                workoutReport.clearWorkoutData();
                for (Workout workout : aList) {
                    if (activityID == 0) {
                        if (!Utilities.isDetectedActivity(workout.activityID) || Constants.ATRACKIT_ATRACKIT_CLASS.equals(workout.packageName))
                            workoutReport.addWorkout(workout);
                    } else {
                        if (activityID == workout.activityID)
                            workoutReport.addWorkout(workout);
                    }
                }
                List<WorkoutSet> itSets = mWorkoutSetDao.getAllWorkoutSetByStarts(sUserID, startTime, endTime);
                workoutReport.clearWorkoutSetData();
                workoutReport.addAllSets((ArrayList<WorkoutSet>) itSets);
                report = workoutReport.getWorkoutData();
                reportSets = workoutReport.getWorkoutSetData();
            }

            // summary report type
            if (reportType == 1) {
                List<Workout> itr = (workoutID == 0) ? mWorkoutDao.getAllWorkoutByStarts(sUserID, startTime, endTime) : mWorkoutDao.getWorkoutById(workoutID);
                workoutReport.clearWorkoutData();
                for (Workout workout : itr) {
                    workoutReport.addWorkoutData(context, workout);
                }
                List<WorkoutSet> itSets = mWorkoutSetDao.getAllWorkoutSetByStarts(sUserID, startTime, endTime);
                workoutReport.clearWorkoutSetData();
                workoutReport.addAllSets((ArrayList<WorkoutSet>) itSets);
                report = workoutReport.getSummaryWorkoutData();
            }

            // templates
            if (reportType == 3) {
                List<Workout> aList = mWorkoutDao.getWorkoutTemplatesByUserId(sUserID);
                for (Workout workout : aList) {
                    if (activityID == 0) {
                        if (!Utilities.isDetectedActivity(workout.activityID) || Constants.ATRACKIT_ATRACKIT_CLASS.equals(workout.packageName)) {
                            workoutReport.addWorkout(workout);
                            List<WorkoutSet> itSets = mWorkoutSetDao.getWorkoutSetByWorkoutID(workout._id, sUserID);
                            for (WorkoutSet s : itSets) {
                                workoutReport.addWorkoutSet(s);
                            }
                        }
                    } else {
                        if (activityID == workout.activityID) {
                            workoutReport.addWorkout(workout);
                            List<WorkoutSet> itSets = mWorkoutSetDao.getWorkoutSetByWorkoutID(workout._id, sUserID);
                            for (WorkoutSet s : itSets) {
                                workoutReport.addWorkoutSet(s);
                            }
                        }
                    }
                }
                report = workoutReport.getWorkoutData();
                reportSets = workoutReport.getWorkoutSetData();
            }
            // WORKOUT ID
            if ((reportType == 4) && (workoutID > 0)) {
                List<Workout> aList = mWorkoutDao.getWorkoutByIdUserId(workoutID, sUserID);
                for (Workout workout : aList) {
                    if (activityID == 0) {
                        if (!Utilities.isDetectedActivity(workout.activityID) || Constants.ATRACKIT_ATRACKIT_CLASS.equals(workout.packageName)) {
                            workoutReport.addWorkout(workout);
                            List<WorkoutSet> itSets = mWorkoutSetDao.getWorkoutSetByWorkoutID(workout._id, sUserID);
                            for (WorkoutSet s : itSets) {
                                workoutReport.addWorkoutSet(s);
                            }
                        }
                    } else {
                        if (activityID == workout.activityID) {
                            workoutReport.addWorkout(workout);
                            List<WorkoutSet> itSets = mWorkoutSetDao.getWorkoutSetByWorkoutID(workout._id, sUserID);
                            for (WorkoutSet s : itSets) {
                                workoutReport.addWorkoutSet(s);
                            }
                        }
                    }
                }
                report = workoutReport.getWorkoutData();
                reportSets = workoutReport.getWorkoutSetData();
            }
            // workout type
            if (reportType == Constants.OBJECT_TYPE_WORKOUT){
                List<Workout> wList = mWorkoutDao.getWorkoutByIdUserId(workoutID, sUserID);
                if (wList != null && (wList.size() > 0)){
                    report.addAll(wList);
                    List<WorkoutSet> sList = mWorkoutSetDao.getWorkoutSetByWorkoutID(workoutID, sUserID);
                    List<WorkoutMeta> mList = mWorkoutMetaDao.getMetaByWorkoutUserDeviceId(workoutID,sUserID,sDeviceID);
                    if (mList != null) reportMeta.addAll(mList);
                    if (sList != null && (sList.size() > 0)){
                        reportSets.addAll(sList);
                        if (Utilities.isGymWorkout(wList.get(0).activityID)) {
                            List<ObjectAggregate> wAggList = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_WORKOUT, workoutID);
                            if ((wAggList != null) && (wAggList.size() > 0))
                                reportAggWorkout.addAll(wAggList);
                            for (WorkoutSet set : sList) {
                                if ((set.bodypartID != null) && (set.exerciseID != null)) {
                                    Long bpLong = (long) set.bodypartID;
                                    Long exLong = (long) set.exerciseID;
                                    if (!bodypartList.contains(bpLong) && (bpLong > 0)) {
                                        bodypartList.add(bpLong);
                                        List<ObjectAggregate> bpAggList = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_BODYPART, bpLong);
                                        if ((bpAggList != null) && (bpAggList.size() > 0))
                                            reportAggBodypart.addAll(bpAggList);
                                    }
                                    if (!exerciseList.contains(exLong) && (exLong > 0)) {
                                        exerciseList.add(exLong);
                                        List<ObjectAggregate> exAggList = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_EXERCISE, exLong);
                                        if ((exAggList != null) && (exAggList.size() > 0))
                                            reportAggExercise.addAll(exAggList);
                                    }
                                }
                            }
                            if (reportAggWorkout.size() > 0) bundle.putParcelableArrayList(Constants.KEY_AGG_WORKOUT, reportAggWorkout);
                            if (reportAggBodypart.size() > 0) bundle.putParcelableArrayList(Constants.KEY_AGG_BODYPART, reportAggBodypart);
                            if (reportAggExercise.size() > 0) bundle.putParcelableArrayList(Constants.KEY_AGG_EXERCISE, reportAggExercise);
                        }
                    }
                }
            }
            if (report.size() > 0)
                bundle.putParcelableArrayList(Constants.KEY_LIST_WORKOUTS, report);
            if (reportSets.size() > 0)
                bundle.putParcelableArrayList(Constants.KEY_LIST_SETS, reportSets);
            if (reportMeta.size() > 0)
                bundle.putParcelableArrayList(Constants.KEY_LIST_META, reportMeta);

        }
        if(mReceiver != null) {
            mReceiver.send(200, bundle);
        }else {
            Log.w(TAG, "Weak listener is NULL.");
        }

    }
}
