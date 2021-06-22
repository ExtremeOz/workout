package com.a_track_it.fitdata.service;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.MetaAggTuple;
import com.a_track_it.fitdata.common.data_model.SetAggTuple;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutAggTuple;
import com.a_track_it.fitdata.common.data_model.WorkoutReport;
import com.a_track_it.fitdata.common.data_model.WorkoutRepository;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Black
 *
 * This class is a_track_it.com work in progress. It will be used to return summary data to display on
 * the report page.
 */
public class SummaryCacheIntentService  extends IntentService {

    private WorkoutReport workoutReport = new WorkoutReport();
    public final static String TAG = SummaryCacheIntentService.class.getSimpleName();
    private ResultReceiver mReceiver;
    private long workoutType;
    private int indexTimeFrame;
    private WorkoutRepository workoutRepository;
    private int indexGrouping;
    private int indexUOY;
    private int indexFilter;
    public SummaryCacheIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        workoutReport = new WorkoutReport();
        workoutRepository =  InjectorUtils.getWorkoutRepository((Application) context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mReceiver = intent.getParcelableExtra(Constants.KEY_FIT_REC);
        workoutType = intent.getLongExtra(Constants.MAP_DATA_TYPE, 0);
        indexGrouping = intent.getIntExtra(Constants.KEY_INDEX_GROUP, 0);
        indexUOY = intent.getIntExtra(Constants.KEY_INDEX_UOY, 0);
        indexTimeFrame = intent.getIntExtra(Constants.KEY_FIT_TYPE, 1);
        indexFilter = intent.getIntExtra(Constants.KEY_INDEX_FILTER, 0);
        String sUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
        ApplicationPreferences applicationPreferences =  ApplicationPreferences.getPreferences(getApplicationContext());
        if ((sUserID == null) || (sUserID.length() == 0))  sUserID = applicationPreferences.getLastUserID();
        if (sUserID.length() > 0) workoutReport.setUserID(sUserID);
        if (mReceiver != null) {
            Bundle bundle = new Bundle();
            Utilities.TimeFrame tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
            if (indexTimeFrame == 1) tf = Utilities.TimeFrame.BEGINNING_OF_WEEK;
            if (indexTimeFrame == 2) tf = Utilities.TimeFrame.LAST_WEEK;
            if (indexTimeFrame == 3) tf = Utilities.TimeFrame.BEGINNING_OF_MONTH;
            if (indexTimeFrame == 4) tf = Utilities.TimeFrame.LAST_MONTH;
            if (indexTimeFrame == 5) tf = Utilities.TimeFrame.NINETY_DAYS;
            if (indexTimeFrame == 6) tf = Utilities.TimeFrame.BEGINNING_OF_YEAR;
            long startTime = Utilities.getTimeFrameStart(tf);
            long endTime = Utilities.getTimeFrameEnd(tf);
            int iRetVal = 0;
            // per day -
            if (indexUOY == 0) {
                List<Workout> itr = workoutRepository.getWorkoutsNow(sUserID,Constants.ATRACKIT_EMPTY, startTime, endTime);
                workoutReport.clearWorkoutData();
                for (Workout workout : itr) {
                    if (Utilities.isDetectedActivity(workoutType) && Utilities.isDetectedActivity(workout.activityID))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_STRENGTH) && (Utilities.isGymWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_ARCHERY) && (Utilities.isShooting(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_AEROBICS) && (Utilities.isCardioWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_RUNNING) && (Utilities.isRunning(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_BIKING) && (Utilities.isBikeWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_GOLF) && (Utilities.isSportWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.WORKOUT_TYPE_WINTER) && (Utilities.isWinterWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_WATER) && (Utilities.isAquatic(workout.activityID)))
                        workoutReport.addWorkout(workout);
                    if ((workoutType == 31) && (Utilities.isMiscellaneousWorkout(workout.activityID)))
                        workoutReport.addWorkout(workout);
                }
                List<WorkoutSet> itSets = workoutRepository.getWorkoutSetsNow(sUserID,Constants.ATRACKIT_EMPTY, startTime, endTime);
                workoutReport.clearWorkoutSetData();
                for (WorkoutSet s: itSets){
                    if ((workoutType == Constants.SELECTION_ACTIVITY_GYM) && (Utilities.isGymWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_SHOOT) && (Utilities.isShooting(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_CARDIO) && (Utilities.isCardioWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_RUN) && (Utilities.isRunning(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_BIKE) && (Utilities.isBikeWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_SPORT) && (Utilities.isSportWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_WINTER) && (Utilities.isWinterWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_WATER) && (Utilities.isAquatic(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                    if ((workoutType == Constants.SELECTION_ACTIVITY_MISC) && (Utilities.isMiscellaneousWorkout(s.activityID)))
                        workoutReport.addWorkoutSet(s);
                }
                iRetVal= workoutReport.getWorkoutSize();
                if (iRetVal > 0) {
                    bundle.putParcelableArrayList(Constants.KEY_LIST_WORKOUTS, workoutReport.getWorkoutData());
                    bundle.putParcelableArrayList(Constants.KEY_LIST_SETS, workoutReport.getWorkoutSetData());
                }
            }
            else{
                bundle.putLong(Constants.MAP_DATA_TYPE,workoutType);
                bundle.putInt(Constants.KEY_FIT_TYPE, indexTimeFrame);
                bundle.putInt(Constants.KEY_INDEX_UOY, indexUOY);
                bundle.putInt(Constants.KEY_INDEX_GROUP, indexGrouping);
                bundle.putInt(Constants.KEY_INDEX_FILTER, indexFilter);
                bundle.putInt(Constants.KEY_INDEX_METRIC, intent.getIntExtra(Constants.KEY_INDEX_METRIC,0));
                bundle.putString(Constants.KEY_FIT_USER, sUserID);
                DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                Log.i(SummaryCacheIntentService.class.getSimpleName(),"ReportType " + workoutType + " start " + dateFormat.format(startTime) + " end "
                        + dateFormat.format(endTime) + " indexUOY " + indexUOY + " indexGrouping " + indexGrouping );
                if (Utilities.isGymWorkout(workoutType)){
                    int objectGroupType = Constants.OBJECT_TYPE_BODY_REGION; //indexGrouping == 0
                    if (indexGrouping == 1)
                        objectGroupType = Constants.OBJECT_TYPE_BODYPART;
                    if (indexGrouping == 2)
                        objectGroupType = Constants.OBJECT_TYPE_EXERCISE;
                    if (indexGrouping == 3)
                        objectGroupType = Constants.OBJECT_TYPE_RESISTANCE_TYPE;
                    List<SetAggTuple> setsList = workoutRepository.getSetAggTuple(sUserID,startTime,endTime,objectGroupType, indexUOY, indexFilter);
                    if ((setsList != null) && (setsList.size() > 0)) {
                        iRetVal = setsList.size();
                        ArrayList<SetAggTuple> setAggTupleArrayList = new ArrayList<>(setsList);
                        bundle.putParcelableArrayList(Constants.KEY_LIST_SETS, setAggTupleArrayList);
                    }
                }else{
                    List<WorkoutAggTuple> list = workoutRepository.getWorkoutAggTuple(sUserID, startTime, endTime, workoutType, indexUOY);
                    if ((list != null) && (list.size() > 0)){
                        iRetVal = list.size();
                        ArrayList<WorkoutAggTuple> workoutAggTupleArrayList = new ArrayList<>(list);
                        bundle.putParcelableArrayList(Constants.KEY_AGG_WORKOUT, workoutAggTupleArrayList);
                    }
                }
                int reportType = Math.toIntExact(workoutType);
                List<MetaAggTuple> metaList = workoutRepository.getMetaAggTuple(sUserID, startTime, endTime, reportType, indexUOY);
                if ((metaList != null) && (metaList.size() > 0)){
                    iRetVal = metaList.size();
                    ArrayList<MetaAggTuple> metaAggTupleArrayList = new ArrayList<>(metaList);
                    bundle.putParcelableArrayList(Constants.KEY_AGG_META, metaAggTupleArrayList);
                }
                bundle.putInt(Constants.KEY_FIT_VALUE, iRetVal);
            }
            mReceiver.send(200, bundle);
        }else {
            Log.w(TAG, "Weak listener is NULL.");
        }
    }
}
