package com.a_track_it.fitdata.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.location.DetectedActivity;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.FLAG_NON_TRACKING;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_BIKE;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_CARDIO;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_GYM;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_MISC;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_RUN;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_SHOOT;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_SPORT;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_WATER;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_WINTER;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_TYPE_STEPCOUNT;


public class ReferencesTools {
    private static final ReferencesTools ourInstance = new ReferencesTools();
    private static Context mContext;
    private static Resources mResources;
    private static String[] mNamesSortedArray;
    private static int[] mIDNamesSortedArray;
    private static String[] mNamesIDSortedArray;
    private static String[] mIdentifiersIDSortedArray;
    private static String[] mIdentifiersNameSortedArray;
    private static int[] mIDSortedArray;
    private static int[] mActivityListIDs;
    private static String[] mActivityListNames;
    private static String[] mActivityIconArray;


    public static ReferencesTools getInstance() {
        return ourInstance;
    }
    public static ReferencesTools setInstance(Context context){
        mContext = context;
        mResources = mContext.getResources();
        return ourInstance;
    }
    private ReferencesTools() {
    }
    public void init(Context context){
        mContext = context;
        mResources = mContext.getResources();
    }
    public Context getMyContext(){
        return mContext;
    }
    public void killme(){
        mContext = null;
        mResources = null;
    }
    public String[] getNamesSorted(){
        if ((mNamesSortedArray == null) || (mNamesSortedArray.length == 0)) mNamesSortedArray = mResources.getStringArray(R.array.activity_names_sorted);
        return mNamesSortedArray;
    }
    public String[] getIdentifiersIDSortedArray(){
        if ((mIdentifiersIDSortedArray == null) || (mIdentifiersIDSortedArray.length == 0)) mIdentifiersIDSortedArray = mResources.getStringArray(R.array.activity_ident_id_sorted);
        return mIdentifiersIDSortedArray;

    }
    public String[] getIdentifiersNameSortedArray(){
        if ((mIdentifiersNameSortedArray == null) || (mIdentifiersNameSortedArray.length == 0)) mIdentifiersNameSortedArray = mResources.getStringArray(R.array.activity_ident_name_sorted);
        return mIdentifiersNameSortedArray;

    }
    public int[] getIDsNameSorted(){
        return mResources.getIntArray(R.array.activity_id_names_sorted);
    }
    public int[] getBodypartIDs(){
        return mResources.getIntArray(R.array.bodypart_ids);
    }
    public String[] getBodypartShortNames(){
        return mResources.getStringArray(R.array.bodypart_shortnames);
    }
    public String[] getBodypartFullNames(){
        return mResources.getStringArray(R.array.bodypart_fullnames);
    }
    public int[] getBodypartParentIDs(){
        return mResources.getIntArray(R.array.bodypart_parentids);
    }
    public String[] getBodypartParentNames(){
        return mResources.getStringArray(R.array.bodypart_parentnames);
    }
    public String[] getBodypartRegionNames(){
        return mResources.getStringArray(R.array.bodypart_regionnames);
    }
    public int[] getBodypartPowerFactors(){
        return mResources.getIntArray(R.array.bodypart_power_rating);
    }
    public int[] getExerciseIDs(){
        return mResources.getIntArray(R.array.exercise_id);
    }
    public String[] getExerciseNames(){
        return mResources.getStringArray(R.array.exercise_name_list);
    }
    public int[] getBP1IDs(){
        return mResources.getIntArray(R.array.exercise_bodypart1_id);
    }
    public String[] getBP1Names(){
        return mResources.getStringArray(R.array.exercise_bodypart1_name);
    }
    public int[] getBP2IDs(){
        return mResources.getIntArray(R.array.exercise_bodypart2_id);
    }
    public String[] getBP2Names(){
        return mResources.getStringArray(R.array.exercise_bodypart2_name);
    }
    public int[] getBP3IDs(){
        return mResources.getIntArray(R.array.exercise_bodypart3_id);
    }
    public String[] getBP3Names(){
        return mResources.getStringArray(R.array.exercise_bodypart3_name);
    }
    public int[] getBP4IDs(){
        return mResources.getIntArray(R.array.exercise_bodypart4_id);
    }
    public String[] getBP4Names(){
        return mResources.getStringArray(R.array.exercise_bodypart4_name);
    }
    public int[] getExerciseResistanceTypes(){
        return mResources.getIntArray(R.array.exercise_resistance_type);
    }
    public int[] getExerciseBodypartCount(){
        return mResources.getIntArray(R.array.exercise_bodypart_count);
    }
    public int[] getExercisePowerRatings(){
        return mResources.getIntArray(R.array.exercise_power_rating);
    }
    public int[] getPowerFactorColorArray(){
        int[] myarray = {mContext.getColor(R.color.power_factor_1), mContext.getColor(R.color.power_factor_2),mContext.getColor(R.color.power_factor_3),
                mContext.getColor(R.color.power_factor_4),mContext.getColor(R.color.power_factor_5),mContext.getColor(R.color.power_factor_6),mContext.getColor(R.color.power_factor_7)
                ,mContext.getColor(R.color.power_factor_8),mContext.getColor(R.color.power_factor_9),mContext.getColor(R.color.power_factor_10)};
        return myarray;
    }

    public AnimationDrawable getAnimatedDialForValue(int percentage){
        AnimationDrawable animatedDrawable = (AnimationDrawable)AppCompatResources.getDrawable(mContext,R.drawable.dial_animation);
        int iCounter = 10;
        while (iCounter <= percentage){
            String sName = "ic_dial_" + iCounter;
            int resId = mResources.getIdentifier(sName,Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
            Drawable item = AppCompatResources.getDrawable(mContext, resId);
            animatedDrawable.addFrame(item, 100);
            iCounter += 10;
        }
        return  animatedDrawable;
    }

    public String[] getIconsNameSorted(){
        return mResources.getStringArray(R.array.activity_icon_name_sorted);
    }
    public int getFitnessActivityIdByText(String sName){
        if ((mNamesSortedArray == null) || (mNamesSortedArray.length == 0)) mNamesSortedArray = mResources.getStringArray(R.array.activity_names_sorted);
        if ((mIDNamesSortedArray == null) || (mIDNamesSortedArray.length == 0)) mIDNamesSortedArray = mResources.getIntArray(R.array.activity_id_names_sorted);

        int iRet = -1;
        int index = Arrays.asList(mNamesSortedArray).indexOf(sName);
        if (index >= 0)
            iRet = mIDNamesSortedArray[index];

        return iRet;
    }

    public String getFitnessActivityTextById(long id){
        String sRet = Constants.ATRACKIT_EMPTY;
        int searchID = Math.toIntExact(id);
        try {
            if (mResources == null) mResources = mContext.getResources();
            if ((mNamesIDSortedArray == null) || (mNamesIDSortedArray.length == 0))
                mNamesIDSortedArray = mResources.getStringArray(R.array.activity_name_id_sorted);
            if ((mIDSortedArray == null) || (mIDSortedArray.length == 0))
                mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);

            int index = Arrays.binarySearch(mIDSortedArray, searchID);

            if (index >= 0)
                sRet = mNamesIDSortedArray[index];
        }catch(Exception e){
            Log.e("ReferenceTools", e.getMessage());
        }
        return sRet;
    }
    public String getFitnessActivityIdentifierById(long id){
        String sRet = Constants.ATRACKIT_EMPTY;
        int searchID = Math.toIntExact(id);
        try{
        if ((mIdentifiersIDSortedArray == null) || (mIdentifiersIDSortedArray.length == 0)) mIdentifiersIDSortedArray = mResources.getStringArray(R.array.activity_ident_id_sorted);
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);

        int index = Arrays.binarySearch(mIDSortedArray, searchID);
        if (index >= 0)
            sRet = mIdentifiersIDSortedArray[index];
        }catch(Exception e){
            Log.e("ReferenceTools", e.getMessage());
        }
        return sRet;
    }
    public String getFitnessActivityIconById(int id){
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);
        if ((mActivityIconArray == null) || (mActivityIconArray.length == 0)) mActivityIconArray = mResources.getStringArray(R.array.activity_icon_id_sorted);

        int index = Arrays.binarySearch(mIDSortedArray, id);
        String sRet = "ic_running";
        if (index >= 0)
            sRet = mActivityIconArray[index];
        return sRet;
    }
    public String currentWorkoutStateString(Workout workout){
        String retState = "Invalid";

        if (workout != null) {
            if (workout.start == -1)
                retState = "template";  // template
            else {
                if (workout.start == 0) {
                    if (workout.activityID > 0) retState = "pending";
                } else {
                    if (workout.end == 0)
                        retState = "live";
                    else
                        retState = "completed";
                }
            }
            // if (workout.last_sync > 0) retState += WORKOUT_SYNCD;2
        }
        return retState;
    }


    /** Gets {@link FitnessOptions} in order to check or request OAuth permission for the user. */
    public FitnessOptions getFitnessSignInOptions(int requestType) {
        FitnessOptions fa = null;
        if (requestType == 0)  // read and aggregates
            fa = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_POWER_SUMMARY, FitnessOptions.ACCESS_READ)
                    .build();
        if (requestType == 1) // data read-write
            fa = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                    .build();
        if (requestType == 2) // data read
            fa = FitnessOptions.builder()
                    .accessActivitySessions(FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .build();

        if (requestType == 3)  // read for subscribe to RecordingClient
            fa = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .build();
        if (requestType == 4) // session read-write
            fa = FitnessOptions.builder()
                    .accessActivitySessions(FitnessOptions.ACCESS_WRITE)
                    .build();
        if (requestType == 5) // session read-write fully
            fa = FitnessOptions.builder()
                    .accessActivitySessions(FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                    .build();
        return  fa;
    }

    public int getFitnessActivityIconResById(long id){
        if (mResources == null)
            mResources = mContext.getResources();
        if (mResources == null) return 0;
        int result = R.drawable.ic_trail_running_shoe;
        if (id == Constants.WORKOUT_TYPE_UNKNOWN) result = R.drawable.ic_search;
        else
        if ((id == Constants.SELECTION_FITNESS_ACTIVITY) || (id <= 0)){
            if (id == Constants.WORKOUT_TYPE_STEPCOUNT) result = R.drawable.ic_walk_white;
            if (id == Constants.WORKOUT_TYPE_TIME) result = R.drawable.ic_running_white;
            if (id == Constants.WORKOUT_TYPE_INVEHICLE) result = R.drawable.ic_motoring_car;
            if (id == Constants.SELECTION_FITNESS_ACTIVITY) result = R.drawable.ic_sort_a_to_z_white;

        }else {
            if ((mIDSortedArray == null) || (mIDSortedArray.length == 0))
                mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);
            if ((mActivityIconArray == null) || (mActivityIconArray.length == 0))
                mActivityIconArray = mResources.getStringArray(R.array.activity_icon_id_sorted);
            String sRet = "ic_trail_running_shoe";
            int idSearch = Math.toIntExact(id);
            int index = Arrays.binarySearch(mIDSortedArray, idSearch);
            if (index >= 0)
                sRet = mActivityIconArray[index];

            result = mResources.getIdentifier(sRet, "drawable", Constants.ATRACKIT_ATRACKIT_CLASS);
            if (result == 0) result = R.drawable.ic_footsteps_silhouette_variant;
        }
        return result;
    }

    public Drawable getRepsDrawableByCount(int count){
        Drawable dRet = null;
        int colorWhite = ContextCompat.getColor(mContext, android.R.color.white);
        if (count >= 0 && count <= 10)
        switch (count){
            case 0:
                dRet = AppCompatResources.getDrawable(mContext,R.drawable.ic_number_zero_circle_white);
                break;
            case 1:
                dRet = AppCompatResources.getDrawable(mContext,R.drawable.ic_number_one_circle_white);
                break;
            case 2:
                dRet = AppCompatResources.getDrawable(mContext,R.drawable.ic_number_two_circle_white);
                break;
            case 3:
                dRet = AppCompatResources.getDrawable(mContext,R.drawable.ic_number_three_circle_white);
                break;
            case 4:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_four_circle_white);
                break;
            case 5:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_five_circle_white);
                break;
            case 6:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_six_circle_white);
                break;
            case 7:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_seven_circle_white);
                break;
            case 8:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_eight_circle_white);
                break;
            case 9:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_nine_circle_white);
                break;
            case 10:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_ten_circle_white);
                break;
            case 11:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_eleven_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 12:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_twelve_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 13:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_thirteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 14:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_fourteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 15:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_fifteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 16:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_sixteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 17:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_seventeen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 18:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_eighteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 19:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_nineteen_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
            case 20:
                dRet = AppCompatResources.getDrawable(mContext, R.drawable.ic_number_twenty_circle);
                Utilities.setColorFilter(dRet, colorWhite);
                break;
        }
        return  dRet;
    }
    public String getActivityListInfoByIndex(int return_type, int selIndex){
        int iRet; String sRet;
        if (return_type == 0){
            if ((mActivityListIDs == null) || (mActivityListIDs.length == 0)) mActivityListIDs = mResources.getIntArray(R.array.activity_types_ids);
            iRet = mActivityListIDs[selIndex];
            return Integer.toString(iRet);
        }else{
            if ((mActivityListNames == null) || (mActivityListNames.length == 0)) mActivityListNames = mResources.getStringArray(R.array.activity_types);
            sRet = mActivityListNames[selIndex];
            return sRet;
        }
    }
    public int getActivityListIndexById(int in_type){
        if ((mActivityListIDs == null) || (mActivityListIDs.length == 0)) mActivityListIDs = mResources.getIntArray(R.array.activity_types_ids);
        int iRet = 0;
        for(int setId : mActivityListIDs){
            if (in_type == setId)
                return iRet;
            else
                iRet += 1;
        }
        iRet = -1;
        return iRet;
    }
    public int getActivityHighlyMobileById(long id){
        int retVal = 0;
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);
        int idSearch = Math.toIntExact(id);
        int index = Arrays.binarySearch(mIDSortedArray, idSearch);
        if (index >= 0) {
            int[] veryActives = mResources.getIntArray(R.array.activity_veryactive_id_sorted);
            retVal = veryActives[index];
        }
        return retVal;

    }
    public static int getSummaryActivityColor(long id){
        int summaryID = R.color.steps;
        if (id == Constants.WORKOUT_TYPE_TIME)
            summaryID = R.color.calendar;
        if (id == DetectedActivity.WALKING)
            summaryID = R.color.steps;
        if (id == DetectedActivity.IN_VEHICLE)
            summaryID = R.color.driving;
        if (id == SELECTION_ACTIVITY_GYM)
            summaryID = R.color.lifting;
        if (id == SELECTION_ACTIVITY_SHOOT)
            summaryID = R.color.archery;
        if (id == SELECTION_ACTIVITY_CARDIO)
            summaryID = R.color.running;
        if (id == SELECTION_ACTIVITY_BIKE)
            summaryID = R.color.biking_graph;
        if (id == SELECTION_ACTIVITY_RUN)
            summaryID = R.color.running;
        if (id == SELECTION_ACTIVITY_WATER)
            summaryID = R.color.firebase_blue;
        if (id == SELECTION_ACTIVITY_WINTER)
            summaryID = R.color.a_cyan;
        if (id == SELECTION_ACTIVITY_SPORT)
            summaryID = R.color.golfing;
        if (id == SELECTION_ACTIVITY_MISC)
            summaryID = R.color.other; // gardening
        return  summaryID;
    }
    public int getFitnessActivityColorById(long id) {
        int result = R.color.other;
        switch (Math.toIntExact(id)) {
            case (int)Constants.WORKOUT_TYPE_STEPCOUNT:
            case DetectedActivity.WALKING:
            case 93:
            case 94:
            case 95:
                result = R.color.steps;
                break;
            case (int)Constants.WORKOUT_TYPE_TIME:
            case 106: case 35: case 104: case  50: case  52: case  70: case  61: case 62: case  63: case  64: case  65: case  66: case  67
                    : case 68: case  71: case  73: case  74: case  75: case  89: case  90: case  91:
                result = R.color.calendar;
                break;
            case (int)Constants.WORKOUT_TYPE_INVEHICLE:
            case 1:
            case 14:
            case 19:
            case 13:
            case 2:
            case 56:
            case 57:
            case 58:
                result = R.color.driving;
                break;
            case (int)Constants.WORKOUT_TYPE_ARCHERY:
            case 102:
            case 40:
            case 48:
            case 53:
            case 59:
            case 60:
            case 79:
            case 81:
            case 82:
            case 83:
            case 84:
            case 92:
            case 96:
            case 99:
                result = R.color.archery;
                break;
            case (int)Constants.WORKOUT_TYPE_AEROBICS:
            case (int)Constants.WORKOUT_TYPE_RUNNING:
                result = R.color.running;
                break;
            case (int)Constants.WORKOUT_TYPE_VIDEOGAME:
            case 21:
            case 117:
            case 25:
            case 39:
            case 103:
            case 118:
            case 49:
            case 54:
            case 77:
            case 78:
            case 88:
                result = R.color.running;
                break;
            case 10: case 11: case 12: case 20: case 23:
            case 27: case 28: case 29: case 32: case 33: case 34:
            case 36: case 37: case 69: case 51: case 55: case 120:
            case 76: case 85: case 86: case 87:
                result = R.color.golfing;
                break;
            case 97:
            case (int)Constants.WORKOUT_TYPE_STRENGTH:
            case 22:
            case 41: case 47: case 113: case 114: case 115:                
                result = R.color.lifting;
                break;
            default:
                result = R.color.steps;
                break;
        }
        return result;
    }

    public String workoutGymSummaryText(Workout w){
        String result = ATRACKIT_EMPTY;
        if (w.scoreTotal > 0)
            result = w.scoreTotal + " sessions";
        if (w.duration > 0) {
            if (result.length() > 0)
                result += Constants.LINE_DELIMITER + "for " + Utilities.getDurationBreakdown(w.duration);
            else
                result = "for " + Utilities.getDurationBreakdown(w.duration);
        }
        if (w.bodypartCount > 0)
            result += Constants.LINE_DELIMITER + "using " + w.bodypartCount + " bodyparts";
        if (w.exerciseCount > 0)
            result += Constants.LINE_DELIMITER + w.exerciseCount + " exercises";
        if (w.setCount > 0)
            result += Constants.LINE_DELIMITER + w.setCount + " sets ";
        if (w.repCount == 1)
            result += w.repCount + " rep ";
        else
            if (w.repCount > 0) 
                result += " : " + w.repCount + " reps ";
        if (w.weightTotal > 0)
            result += Constants.LINE_DELIMITER + "total weight " + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, w.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
        if (w.wattsTotal > 0)
            result += Constants.LINE_DELIMITER + "using " + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, (w.wattsTotal/1000.0)) + " kJ ";

        if (w.stepCount == 1)
            result += Constants.LINE_DELIMITER + w.stepCount + " step ";
        else
            if (w.stepCount > 0) 
                result += Constants.LINE_DELIMITER + w.stepCount + " steps ";

        return result;
    }
    public String workoutListText(Workout w){
        String result = ATRACKIT_EMPTY;
        try {
            if (w.start > 0)
                result = "On " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
            if (w.duration > 0) result += " for " + Utilities.getDurationBreakdown(w.duration);
            else if (w.end > w.start)
                result += " for " + Utilities.getDurationBreakdown(w.end - w.start);
        }catch(IllegalArgumentException iae){
            iae.printStackTrace();
            result = ATRACKIT_EMPTY;
        }
        if (Utilities.isGymWorkout(w.activityID) && (w.scoreTotal != FLAG_NON_TRACKING)){
            if ((w.name != null) && (w.name.length() > 0))
                result = w.name + Constants.ATRACKIT_SPACE;
            if (w.bodypartCount > 0)
                result += " using " + w.bodypartCount + " bodyparts";
            if (w.exerciseCount > 0)
                result += Constants.ATRACKIT_SPACE + w.exerciseCount + " exercises";
            if (w.setCount > 0)
                result += Constants.ATRACKIT_SPACE + w.setCount + " sets ";
            if (w.repCount == 1)
                result += w.repCount + " rep ";
            else
            if (w.repCount > 0) result += w.repCount + " reps ";
            if (w.weightTotal > 0)
                result += "total weight " + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, w.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            if (w.wattsTotal > 0)
                result += " using " + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, (w.wattsTotal/1000.0)) + " kJ ";

        }
        else{
            if (Utilities.isShooting(w.activityID)){
                if (w.setCount > 0)
                    result += " ends: " + w.setCount + Constants.ATRACKIT_SPACE;
                if (w.repCount == 1)
                    result += w.repCount + " arrow per w.end ";
                else
                    result += w.repCount + " arrows per w.end ";
            }
        }
        if (w.stepCount > 0)
            result += " taking " + w.stepCount + " steps ";
        if (w.goal_duration > 0)
            result += " goal " + Utilities.getDurationBreakdown(TimeUnit.MINUTES.toMillis(w.goal_duration));
        if (w.goal_steps > 0)
            result += " goal steps " + w.goal_steps;
        return result;
    }
    public String workoutNotifyText(Workout w){
        String result = ATRACKIT_EMPTY;
        if (Utilities.isGymWorkout(w.activityID)){
            if ((w.name != null) && (w.name.length() > 0))
                result = w.name + Constants.ATRACKIT_SPACE;
            if (w.scoreTotal != FLAG_NON_TRACKING) {
                if (w.bodypartCount > 0)
                    result += " using " + w.bodypartCount + " bodyparts";
                if (w.exerciseCount > 0)
                    result += Constants.ATRACKIT_SPACE + w.exerciseCount + " exercises";
                if (w.setCount > 0)
                    result += Constants.ATRACKIT_SPACE + w.setCount + " sets ";
                if (w.repCount == 1)
                    result += w.repCount + " rep ";
                else if (w.repCount > 0) result += w.repCount + " reps ";
                if (w.weightTotal > 0)
                    result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, w.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            }
            // if (w.start > 0)
            //     result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
            if (w.end > 0)
                result += " for " + Utilities.getDurationBreakdown(w.duration);
            if (w.wattsTotal > 0)
                result += " using " + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, (w.wattsTotal/1000.0)) + " kJ ";

        }else{
            if (Utilities.isShooting(w.activityID)){
                if (w.setCount > 0)
                    result += " ends: " + w.setCount + Constants.ATRACKIT_SPACE;
                if (w.repCount == 1)
                    result += w.repCount + " arrow per w.end ";
                else
                    result += w.repCount + " arrows per w.end ";
                if (w.start > 0)
                    result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
                if (w.duration > 0) result += " for " + Utilities.getDurationBreakdown(w.duration);
            }else{
                if (w.start > 0)
                    result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
                if (w.end > 0) result += " for " + Utilities.getDurationBreakdown(w.end - w.start);
            }
        }
        if (w.stepCount > 0)
            result += " taking " + w.stepCount + " steps ";
        if (w.goal_duration > 0)
            result += " goal " + Utilities.getDurationBreakdown(TimeUnit.MINUTES.toMillis(w.goal_duration));
        if (w.goal_steps > 0)
            result += " goal steps " + w.goal_steps;
        return result;
    }
    public String workoutTemplateText(Workout w){
        String result = w.name;
        if (w.bodypartCount > 0)
            result += "\n using " + w.bodypartCount + " bodyparts";
        if (w.exerciseCount > 0)
            result += Constants.LINE_DELIMITER +  w.exerciseCount + " exercises ";
        if (w.setCount > 0)
            result += Constants.LINE_DELIMITER + w.setCount + " sets ";
        if (w.repCount == 1)
            result += Constants.LINE_DELIMITER + w.repCount + " rep ";
        else
            if (w.repCount > 0) result += Constants.LINE_DELIMITER + w.repCount + " reps ";
        if (w.weightTotal > 0)
            result += Constants.LINE_DELIMITER + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, w.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
        if (w.weightTotal > 0)
            result += Constants.LINE_DELIMITER + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, w.wattsTotal) + " kJ ";
        if ((w.duration) > 0)
            result += Constants.LINE_DELIMITER + " for " + Utilities.getDurationBreakdown(w.duration);
        return result;
    }

    public String workoutShortText(Workout w) {
        String result = ((w.activityID > 0) && (w.start > 0)) ? Utilities.getPartOfDayString(w.start) : Constants.ATRACKIT_EMPTY;
        if (Utilities.isGymWorkout(w.activityID)){
            result += Constants.ATRACKIT_SPACE + w.activityName + Constants.ATRACKIT_SPACE;
            if (w.scoreTotal != FLAG_NON_TRACKING) {
                if (w.bodypartCount > 0)
                    result += " using " + w.bodypartCount + " bodyparts";
                if (w.exerciseCount > 0)
                    result += Constants.ATRACKIT_SPACE + w.exerciseCount + " exercises ";
                if (w.setCount > 0)
                    result += w.setCount + " sets ";
                if (w.repCount == 1)
                    result += w.repCount + " rep ";
                else if (w.repCount > 0) result += w.repCount + " reps ";
                if (w.weightTotal > 0)
                    result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, w.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
                if (w.weightTotal > 0)
                    result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, w.wattsTotal) + " kJ ";
            }
            if (w.start > 0)
                result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
            if ((w.end - w.start) > 0)
                result += " for " + Utilities.getDurationBreakdown(w.end - w.start);
        }
        else{
            if (Utilities.isShooting(w.activityID)){
                result += Constants.ATRACKIT_SPACE + w.activityName + Constants.ATRACKIT_SPACE;
                if (w.setCount > 0)
                    result += " ends: " + w.setCount + Constants.ATRACKIT_SPACE;
                if (w.repCount == 1)
                    result += w.repCount + " arrow per end ";
                else
                    result += w.repCount + " arrows per end ";
                if (w.start > 0)
                    result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
                if (w.duration > 0) result += " for " + Utilities.getDurationBreakdown(w.duration);
                result += " score " + w.scoreTotal;
            }
            else{
                if (w.activityID == WORKOUT_TYPE_STEPCOUNT)
                    result += " Step Count Accumulator ";
                else {
                    if (w.activityID > 0)
                        result += Constants.ATRACKIT_SPACE + w.activityName + Constants.ATRACKIT_SPACE;
                    else
                        result = w.activityName;
                }
                if (w.start > 0)
                    result += " on " + Utilities.getDayString(w.start) + " at " + Utilities.getTimeString(w.start);
                if ((w.end - w.start) > 0) result += " for " + Utilities.getDurationBreakdown(w.end - w.start);
            }
        }
        if (w.stepCount > 0)
            result += " taking " + w.stepCount + " steps ";
        if (w.goal_duration > 0)
            result += " goal " + Utilities.getDurationBreakdown(TimeUnit.MINUTES.toMillis(w.goal_duration));
        if (w.goal_steps > 0)
            result += " goal steps " + w.goal_steps;
        return result;
    }
    public String workoutSetTinyText(WorkoutSet set) {
        String result = ATRACKIT_EMPTY;
        if (Utilities.isGymWorkout(set.activityID)){
            result = Integer.toString(set.setCount) + " : ";
            result += set.exerciseName + ATRACKIT_SPACE;
            if ((set.weightTotal !=null) && (set.weightTotal > 0)) {
                result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, set.weightTotal)
                        + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            }
            if (set.repCount != null)
                result += "x " + Integer.toString(set.repCount);
        }else
            if (Utilities.isShooting(set.activityID)){
                result = "End " + set.setCount + ATRACKIT_SPACE + set.score_card;
            }else{
                result = set.activityName;
                if (set.duration > 0) result += " for " + Utilities.getDurationBreakdown(set.duration);
                if ((set.stepCount !=null) && (set.stepCount > 0))
                    result += Integer.toString(set.stepCount) + " steps ";
            }
        return result;
    }
    public String workoutSetShortText(WorkoutSet set) {
        String result;
        if (Utilities.isGymWorkout(set.activityID)){
            //if ((set.regionName != null) && (set.regionName.length() > 0)) result += " at " + set.regionName + Constants.ATRACKIT_SPACE;
            result = set.exerciseName;
            if (set.start > 0)
                result += " at " + Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
            else
            if (set.setCount > 1)
                result += " pending set " + Integer.toString(set.setCount) + Constants.ATRACKIT_SPACE;
            if (set.repCount != null)
                if (set.repCount == 1)
                    result += Integer.toString(set.repCount) + " rep ";
                else
                    result += Integer.toString(set.repCount) + " reps ";
            if ((set.weightTotal !=null) && (set.weightTotal > 0)) {
                result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, set.weightTotal)
                        + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            }
            if (set.duration > 0) result += "for " + Utilities.getDurationBreakdown(set.duration);
            if ((set.wattsTotal !=null) && (set.wattsTotal > 0))
                result += " using "+ String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, set.wattsTotal) + " watts ";
        }else{
            if (Utilities.isShooting(set.activityID)){
                result = set.activityName;
                if (set.start > 0)
                    result += " on " + Utilities.getDayString(set.start) + " at " + Utilities.getTimeString(set.start);
                if (set.duration > 0) result += " for " + Utilities.getDurationBreakdown(set.duration);
            }else{
                result = set.activityName;
                if (set.start > 0)
                    result += " on " + Utilities.getDayString(set.start) + " at " + Utilities.getTimeString(set.start);
                if (set.duration > 0) result += " for " + Utilities.getDurationBreakdown(set.duration);
                if ((set.stepCount !=null) && (set.stepCount > 0))
                    result += Integer.toString(set.stepCount) + " steps ";
            }
        }
        return result;
    }
    public String workoutSetNotifyText(WorkoutSet set) {
        String result;
        if (Utilities.isGymWorkout(set.activityID)){
            result = set.exerciseName;
            if (set.start > 0)
                result += " at " + Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
            if (set.repCount != null)
                if (set.repCount == 1)
                    result += Integer.toString(set.repCount) + " rep ";
                else
                    result += Integer.toString(set.repCount) + " reps ";
            if ((set.weightTotal !=null) && (set.weightTotal > 0)) {
                result += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, set.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            }
            if (set.duration > 0) result += "for " + Utilities.getDurationBreakdown(set.duration);
            if ((set.wattsTotal !=null) && (set.wattsTotal > 0))
                result += " using "+ String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, set.wattsTotal) + " watts ";
        }else{
            if (Utilities.isShooting(set.activityID)){
                result = set.activityName;
                if (set.start > 0)
                    result += " on " + Utilities.getDayString(set.start) + " at " + Utilities.getTimeString(set.start);
                if (set.duration > 0) result += " for " + Utilities.getDurationBreakdown(set.duration);
            }else{
                result = set.activityName;
                if (set.start > 0)
                    result += " on " + Utilities.getDayString(set.start) + " at " + Utilities.getTimeString(set.start);
                if (set.duration > 0) result += " for " + Utilities.getDurationBreakdown(set.duration);
                if ((set.stepCount !=null) && (set.stepCount > 0))
                    result += Integer.toString(set.stepCount) + " steps ";
            }
        }
        return result;
    }

    public String metaShortText(WorkoutMeta meta) {
        String result = (meta.start > 0) ? Utilities.getPartOfDayString(meta.start) : Constants.ATRACKIT_EMPTY;

        if (Utilities.isGymWorkout(meta.activityID)){
            result += Constants.ATRACKIT_SPACE + meta.activityName + Constants.ATRACKIT_SPACE + meta.description + Constants.ATRACKIT_SPACE;
            if (meta.weightTotal > 0)
                result += String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, meta.weightTotal) + ((UnitLocale.getDefault() == UnitLocale.Metric) ? " kg " : " lbs ");
            if (meta.weightTotal > 0)
                result += String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, meta.wattsTotal) + " kJ ";
            if (meta.start > 0)
                result += " on " + Utilities.getDayString(meta.start) + " at " + Utilities.getTimeString(meta.start);
            if (meta.duration > 0)
                result += " for " + Utilities.getDurationBreakdown(meta.duration);

        }else{
            result += Constants.ATRACKIT_SPACE + meta.activityName + Constants.ATRACKIT_SPACE + meta.description + Constants.ATRACKIT_SPACE;
            if (Utilities.isShooting(meta.activityID)){
                if (meta.shootFormat.length() > 0) result += meta.shootFormat + Constants.ATRACKIT_SPACE;
                if (meta.equipmentName.length() > 0) result += meta.equipmentName + Constants.ATRACKIT_SPACE;
                if (meta.targetSizeName.length() > 0) result += "target " + meta.targetSizeName + Constants.ATRACKIT_SPACE;
                if (meta.distanceName.length() > 0) result += " at " + meta.distanceName + Constants.ATRACKIT_SPACE;
                if (meta.score_card.length() > 0) result += Constants.ATRACKIT_SPACE + meta.score_card + Constants.ATRACKIT_SPACE;
                if (meta.start > 0)
                    result += " on " + Utilities.getDayString(meta.start) + " at " + Utilities.getTimeString(meta.start);
                if (meta.duration > 0) result += " for " + Utilities.getDurationBreakdown(meta.duration);
            }else{
                if (meta.start > 0)
                    result += " on " + Utilities.getDayString(meta.start) + " at " + Utilities.getTimeString(meta.start);
                if (meta.duration > 0) result += " for " + Utilities.getDurationBreakdown(meta.duration);
            }
        }
        if (meta.stepCount > 0)
            result += " taking " + meta.stepCount + " steps ";
        if (meta.distance > 0)
            result += " distance " + meta.distance + " m ";
        if (meta.move_mins > 0)
            result += " move " + meta.move_mins + " mins ";
        if (meta.heart_pts > 0)
            result += " heart " + meta.heart_pts + " pts ";
        if (meta.total_calories > 0)
            result += " calories " + meta.total_calories + " kJ ";
        if (meta.goal_duration > 0)
            result += " goal " + Utilities.getDurationBreakdown(TimeUnit.MINUTES.toMillis(meta.goal_duration));
        if (meta.goal_steps > 0)
            result += " goal steps " + meta.goal_steps;
        return result;
    }
    public boolean isNetworkConnected() {
        boolean isConnected = false;
        try {
            final ConnectivityManager cm = (mContext != null) ? (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE) : null;
            if (cm != null) {
                final Network n = cm.getActiveNetwork();
                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

                    isConnected =(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || nc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
                }

            }
        }catch (Exception e){
            isConnected = false;
        }

        return isConnected;
    }
}
