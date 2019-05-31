package com.a_track_it.fitdata.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import static com.a_track_it.fitdata.common.Constants.CHANNEL_ENDSET;
import static com.a_track_it.fitdata.common.Constants.CHANNEL_ENTRY;
import static com.a_track_it.fitdata.common.Constants.CHANNEL_HOME;
import static com.a_track_it.fitdata.common.Constants.CHANNEL_LIVE;
import static com.a_track_it.fitdata.common.Constants.CHANNEL_REPORT;
import static com.a_track_it.fitdata.common.Constants.SESSION_PREFIX;
import static com.a_track_it.fitdata.common.Constants.STATE_END_SET;
import static com.a_track_it.fitdata.common.Constants.STATE_ENTRY;
import static com.a_track_it.fitdata.common.Constants.STATE_HOME;
import static com.a_track_it.fitdata.common.Constants.STATE_LIVE;
import static com.a_track_it.fitdata.common.Constants.STATE_REPORT;


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

    private ReferencesTools() {
    }
    public void init(Context context){
        mContext = context;
        mResources = mContext.getResources();
    }

    /**
     * Determines if the wear device has a built-in speaker and if it is supported. Speaker, even if
     * physically present, is only supported in Android M+ on a wear device..
     */
    public final boolean speakerIsSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager packageManager = mContext.getPackageManager();
            // The results from AudioManager.getDevices can't be trusted unless the device
            // advertises FEATURE_AUDIO_OUTPUT.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false;
            }
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true;
                }
            }
        }
        return false;
    }

    public static NotificationChannel createNotificationChannel(
            Context context, int notifyType, boolean canVibrate) {

        // NotificationChannels are required for Notifications on O (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The id of the channel.
            String channelId = SESSION_PREFIX + notifyType;

            // The user-visible name of the channel.
            CharSequence channelName = CHANNEL_HOME; // = mockNotificationData.getChannelName();
            // The user-visible description of the channel.
            String channelDescription = mContext.getString(R.string.app_name); // = mockNotificationData.getChannelDescription();
            int channelImportance =  NotificationManager.IMPORTANCE_DEFAULT;  // = mockNotificationData.getChannelImportance();
            boolean channelEnableVibrate = false; // = mockNotificationData.isChannelEnableVibrate();
            int channelLockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC; // =
            switch (notifyType){
                case STATE_HOME:
                    channelName = CHANNEL_HOME;
                    channelDescription = "ATrackIt home";
                    channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
                    channelEnableVibrate = false;
                    break;
                case STATE_ENTRY:
                    channelName = CHANNEL_ENTRY;
                    channelDescription = "Live Session Recoding";
                    channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
                    channelEnableVibrate = false;
                    break;
                case STATE_LIVE:
                    channelName = CHANNEL_LIVE;
                    channelDescription = "Live Session Recording";
                    channelImportance = NotificationManager.IMPORTANCE_HIGH;
                    channelEnableVibrate = canVibrate;
                    break;
                case STATE_END_SET:
                    channelName = CHANNEL_ENDSET;
                    channelDescription = "End of Set";
                    channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
                    channelEnableVibrate = canVibrate;
                    break;
                case STATE_REPORT:
                    channelName = CHANNEL_REPORT;
                    channelDescription = "Session Reporting";
                    channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
                    channelEnableVibrate = false;
                    break;
/*                case STATE_SETTINGS:
                    break;
                case STATE_DIALOG:
                    break;
                case STATE_15DIALOG:
                    break;*/
            }
            // Initializes NotificationChannel.

            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelName, channelImportance);
            notificationChannel.setDescription(channelDescription);
            if (canVibrate)
                notificationChannel.enableVibration(channelEnableVibrate);
            notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);


            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the
            // below sequence.
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return notificationChannel;
        } else {
            // Returns null for pre-O (26) devices.
            return null;
        }
    }

    public static NotificationCompat.Builder makeNotification(String title, String message, String info, Context context, int notifyType, boolean canVibrate) {
        String channelId = SESSION_PREFIX + notifyType;
        NotificationChannel channel;
        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            channel = createNotificationChannel(context, notifyType, canVibrate);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launch)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (canVibrate) builder.setVibrate(new long[0]);
        if (info.length() > 0) builder.setContentInfo(info);
        // Show the notification

        return builder;
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

    public String getFitnessActivityTextById(int id){
        String sRet = "";
        if ((mNamesIDSortedArray == null) || (mNamesIDSortedArray.length == 0)) mNamesIDSortedArray = mResources.getStringArray(R.array.activity_name_id_sorted);
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);

        int index = Arrays.binarySearch(mIDSortedArray, id);

        if (index >= 0)
            sRet = mNamesIDSortedArray[index];

        return sRet;
    }
    public String getFitnessActivityIdentifierById(int id){
        String sRet = "";
        if ((mIdentifiersIDSortedArray == null) || (mIdentifiersIDSortedArray.length == 0)) mIdentifiersIDSortedArray = mResources.getStringArray(R.array.activity_ident_id_sorted);
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);

        int index = Arrays.binarySearch(mIDSortedArray, id);
        if (index >= 0)
            sRet = mIdentifiersIDSortedArray[index];

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
    public int getFitnessActivityIconResById(int id){
        int result = R.drawable.ic_trail_running_shoe;
        if (id < 0){
            if (id == Constants.WORKOUT_TYPE_STEPCOUNT) result = R.drawable.ic_walk_white;
            if (id == Constants.WORKOUT_TYPE_TIME) result = R.drawable.ic_calendar;
            if (id == Constants.WORKOUT_TYPE_INVEHICLE) result = R.drawable.ic_motoring_car;
            if (id == Constants.WORKOUT_TYPE_UNKNOWN) result = R.drawable.ic_search;
        }else {
            if ((mIDSortedArray == null) || (mIDSortedArray.length == 0))
                mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);
            if ((mActivityIconArray == null) || (mActivityIconArray.length == 0))
                mActivityIconArray = mResources.getStringArray(R.array.activity_icon_id_sorted);
            String sRet = "ic_trail_running_shoe";
            int index = Arrays.binarySearch(mIDSortedArray, id);
            if (index >= 0)
                sRet = mActivityIconArray[index];

            result = mResources.getIdentifier(sRet, "drawable", mContext.getPackageName());
            if (result == 0) result = R.drawable.ic_footsteps_silhouette_variant;
        }
        return result;
    }
    public Drawable getRepsDrawableByCount(int count){
        Drawable dRet = null;
        if (count >= 0 && count <= 10)
        switch (count){
            case 0:
                dRet = ContextCompat.getDrawable(mContext,R.drawable.ic_number_zero_circle_white);
                break;
            case 1:
                dRet = ContextCompat.getDrawable(mContext,R.drawable.ic_number_one_circle_white);
                break;
            case 2:
                dRet = ContextCompat.getDrawable(mContext,R.drawable.ic_number_two_circle_white);
                break;
            case 3:
                dRet = ContextCompat.getDrawable(mContext,R.drawable.ic_number_three_circle_white);
                break;
            case 4:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_four_circle_white);
                break;
            case 5:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_five_circle_white);
                break;
            case 6:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_six_circle_white);
                break;
            case 7:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_seven_circle_white);
                break;
            case 8:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_eight_circle_white);
                break;
            case 9:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_nine_circle_white);
                break;
            case 10:
                dRet = ContextCompat.getDrawable(mContext, R.drawable.ic_number_ten_circle_white);
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
        int iRet = Arrays.binarySearch(mActivityListIDs, in_type);
        return iRet;
    }
    public int getActivityHighlyMobileById(int id){
        int retVal = 0;
        if ((mIDSortedArray == null) || (mIDSortedArray.length == 0)) mIDSortedArray = mResources.getIntArray(R.array.activity_id_sorted);

        int index = Arrays.binarySearch(mIDSortedArray, id);
        if (index >= 0) {
            int[] veryActives = mResources.getIntArray(R.array.activity_veryactive_id_sorted);
            retVal = veryActives[index];
        }
        return retVal;

    }

    public int getFitnessActivityColorById(int id) {
        int result = R.color.other;
        switch (id) {
            case Constants.WORKOUT_TYPE_STEPCOUNT:
                result = R.color.steps;
                break;
            case Constants.WORKOUT_TYPE_TIME:
            case 106: case 35: case 104: case  50: case  52: case  70: case  61: case 62: case  63: case  64: case  65: case  66: case  67
                    : case 68: case  71: case  73: case  74: case  75: case  89: case  90: case  91:
                result = R.color.calendar;
                break;
            case Constants.WORKOUT_TYPE_INVEHICLE:
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
            case Constants.WORKOUT_TYPE_ARCHERY:
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
            case Constants.WORKOUT_TYPE_AEROBICS:
            case Constants.WORKOUT_TYPE_RUNNING:
                result = R.color.running;
                break;
            case Constants.WORKOUT_TYPE_VIDEOGAME:
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
            case 7:
            case 93:
            case 94:
            case 95:
                result = R.color.walking;
                break;
            case 10: case 11: case 12: case 20: case 23:
            case 27: case 28: case 29: case 32: case 33: case 34:
            case 36: case 37: case 69: case 51: case 55: case 120:
            case 76: case 85: case 86: case 87:
                result = R.color.golfing;
                break;
            case 97:
            case Constants.WORKOUT_TYPE_STRENGTH:
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
    public boolean isNetworkConnected() {
        boolean isConnected = false;
        if (mContext != null) {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            isConnected = cm.getActiveNetworkInfo() != null;
        }

        return isConnected;
    }
}
