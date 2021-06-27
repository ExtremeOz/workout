/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.a_track_it.workout.common;

import java.util.concurrent.TimeUnit;

/**
 * A collection of constants that is shared between the wearable and handset apps.
 */
public class Constants {
    public static final float METRE_TO_FEET = 3.28084F;  // multiply to create ft - divide to create metres
    public static final float KG_TO_LBS = 2.20462F;   // multiply for LBS - divide for KG
    public static final double GRAVITY_CONSTANT = 9.81;  // m/s2
    public static final String LABEL_DEVICE_ASKED = "asked_device";
    public static final String LABEL_DEVICE_USE = "use_device";
    public static final String LABEL_LONG = "LONG";
    public static final String LABEL_LOGO = "Logo";
    public static final String LABEL_PROFILE = "Profile";
    public static final String LABEL_FILE = "File";
    public static final String LABEL_CAMERA = "Camera";
    public static final String LABEL_INT_FILE = "internal_file";
    public static final String LABEL_EXT_FILE = "external_file";
    public static final String LABEL_CAMERA_FILE = "camera_file";
    public static final String LABEL_LOGO_SOURCE = "logo_file_source";
    public static final int CHUNK_SIZE = 10;
    public static final int BATTERY_CHUNK_TYPE = 0;
    public static final int DATA_CHUNK_TYPE = 1;
    public static final int DELTA = 20;
    public static final int FLAG_NON_TRACKING = -2;    // used with scoreTotal to indicate status for Non-Shooting workouts and sets
    public static final int FLAG_BUILDING = -1;
    public static final int FLAG_PENDING = 0;
    public static final int FLAG_CHECKED = 1;
    public static final int REQUEST_SIGNIN_SYNC = 5013;
    public static final int REQUEST_SIGNIN_DAILY = 5014;
    public static final String ATRACKIT_FONT = "Maiandra GD Regular.ttf";
    public static final String ATRACKIT_SPACE = " ";
    public static final String ATRACKIT_EMPTY = "";
    public static final String ATRACKIT_DRAWABLE = "drawable";

    public static final String ATRACKIT_ATRACKIT_CLASS = "com.a_track_it.workout";
    public static final String ATRACKIT_PLAY_CLASS = "com.google.android.gms";
    public static final String ATRACKIT_GFIT_CLASS = "com.google.android.apps.fitness";
    public static final String ATRACKIT_RECOG28_CLASS = "com.google.android.gms.permission.ACTIVITY_RECOGNITION";
    public static final String ATRACKIT_HAMMER_STRENGTH = "Hammer-Strength";
    public static final String ATRACKIT_PERCENT_SIGN = "%";
    public static final String ATRACKIT_OFFLINE = "Offline";
        public static final String ATRACKIT_TRACKING = " Tracking ";
    public static final String ATRACKIT_SETUP = "Setup";
    public static final String SINGLE_FLOAT = "%1$.1f";
    public static final String SINGLE_INT = "%1d";
    public final static String KG_TAIL = " kg";
    public final static String KM_TAIL = " km";
    public final static String ALT_TAIL = " m";
    public final static String SPD_TAIL = " m/s";
    public final static String CAL_TAIL = " cal";
    public final static String KCAL_TAIL = " Kcal";
    public final static String KJ_TAIL = " KJ";
    public final static String LBS_TAIL = " lbs";
    public final static String REPS_TAIL = " reps";
    public final static String SETS_TAIL = " sets";
    public final static String MOVE_MINS_TAIL = " m";
    public final static String HEART_PTS_TAIL = " pts";
    public final static String EDIT_SET_TAIL = "Edit Set ";
    public final static String NEW_SET_TAIL = "Sets x ";
    public final static String LINE_DELIMITER = "\n";
    public final static String HOURS_TAIL = " hours";
    public final static String MINS_TAIL = " mins";
    public final static String SECS_TAIL = " secs";
    public final static String DURATION_HEAD = "For ";
    public final static String AT_HEAD = "At ";

    public final static String SHOT_SCORE = "0";
    public final static String SHOT_XY = "0:0";
    public final static String SHOT_X = "X";
    public final static String SHOT_XY_DELIM = ":";
    public final static String SHOT_DELIM = ",";
    public final static String SHOT_END_DELIM = ";";
    public final static String RECOG_STILL = "Still ";
    public final static String RECOG_UNKWN = "Unknown ";
    public final static String RECOG_VEHCL = "In Vehicle ";
    public final static String RECOG_BIKE = "On Bicycle ";
    public final static String RECOG_FOOT = "On Foot ";
    public final static String RECOG_RUN = "Running ";
    public final static String RECOG_TILT = "Tilting ";
    public final static String RECOG_WALK = "Walking ";

    public final static String USER_PREF_ACTIVITY_SETTINGS = "ActivitySetting";
    public final static String USER_PREF_REPORT_SETTINGS = "ReportSetting";

    public final static String USER_PREF_CONF_START_SESSION = "ConfStartSess";
    public final static String USER_PREF_CONF_END_SESSION = "ConfEndSess";
    public final static String USER_PREF_CONF_SET_SESSION = "ConfSetSess";
    public final static String USER_PREF_CONF_DEL_SESSION = "ConfDeleteSess";
    public final static String USER_PREF_CONF_EXIT_APP = "ConfExitApp";
    public final static String USER_PREF_USE_ROUND_IMAGE = "UseRndedImage";
    public final static String USER_PREF_SHOW_GOALS = "homeShowGoal";
    public final static String USER_PREF_GYM_REST_DURATION = "GymRestDur";
    public final static String USER_PREF_SHOOT_REST_DURATION = "ShootRestDur";
    public final static String USER_PREF_SHOOT_CALL_DURATION = "ShootCallDur";
    public final static String USER_PREF_SHOOT_END_DURATION = "ShootEndDur";
    public final static String USER_PREF_DEF_NEW_SETS = "DefNewSets";
    public final static String USER_PREF_DEF_NEW_REPS = "DefNewReps";
    public final static String USER_PREF_BPM_SAMPLE_RATE = "BPMSR";
    public final static String USER_PREF_STEP_SAMPLE_RATE = "StepsSR";
    public final static String USER_PREF_OTHERS_SAMPLE_RATE = "OthersSR";
    public final static String USER_PREF_USE_SET_TRACKING = "useSetTracking";
    public final static String USER_PREF_USE_AUDIO = "useAudio";
    public final static String USER_PREF_USE_VIBRATE = "useVibrate";
    public final static String USER_PREF_STOP_INVEHICLE = "stopInVehicle";
    public final static String USER_PREF_USE_NOTIFICATION = "useNotify";
    public final static String AP_PREF_USE_LOCATION = "UseLoc";
    public final static String AP_PREF_USE_SENSORS = "UseSens";
    public final static String AP_PREF_SYNC_INT_PHONE = "phoneSyncInt";
    public final static String AP_PREF_SYNC_INT = "lastSyncInt";
    public final static String AP_PREF_SYNC_INT_DAILY = "DailySyncInt";
    public final static String AP_PREF_SYNC_INT_NETWORK = "NetworkCheckInt";
    public final static String AP_PREF_ASK_AGE = "AskAge";
    public final static String AP_PREF_USE_TIMED_REST = "useTimedRest";
    public final static String AP_PREF_USE_TIMED_AUTO_START = "restAutoStart";
    public final static String AP_PREF_USE_KG = "UseKG";
    public final static String AP_PREF_NOTIFY_ASKED = "NotifyAsked";
    public final static String AP_PREF_AUDIO_ASKED = "AudioAsked";
    public final static String AP_PREF_VIBRATE_ASKED = "VibrateAsked";
    public final static String AP_PREF_HISTORY_ASKED = "HistoryAsked";
    public final static String AP_PREF_HEIGHT_ASKED = "HeightAsked";
    public final static String USER_PREF_GYM_DURATION = "GymDuration";
    public final static String USER_PREF_SESSION_DURATION = "SessionDuration";
    public final static String USER_PREF_REST_DURATION = "RestDuration";
    public final static String AP_PREF_DEVICE_INT_MSG = "DeviceMsgInt";
    public final static String AP_PREF_HPA_SENSOR_COUNT = "PressureSensorCount";
    public final static String AP_PREF_BPM_SENSOR_COUNT = "BPMSensorCount";
    public final static String AP_PREF_STEP_SENSOR_COUNT = "StepsSensorCount";
    public final static String AP_PREF_TEMP_SENSOR_COUNT = "TempSensorCount";
    public final static String AP_PREF_HUMIDITY_SENSOR_COUNT = "HumiditySensorCount";
    public final static String AP_PREF_HPA_SENSOR2_COUNT = "Pressure2SensorCount";
    public final static String AP_PREF_BPM_SENSOR2_COUNT = "BPM2SensorCount";
    public final static String AP_PREF_STEP_SENSOR2_COUNT = "Steps2SensorCount";
    public final static String AP_PREF_TEMP_SENSOR2_COUNT = "Temp2SensorCount";
    public final static String AP_PREF_HUMIDITY_SENSOR2_COUNT = "Humidity2SensorCount";
    public final static String USER_PREF_SENSORS_ASKED = "SensorsPermissionAsked";
    public final static String USER_PREF_RECOG_ASKED = "RecogActivityAsked";
    public final static String USER_PREF_RECOG = "RecogActivity";
    public final static String USER_PREF_STORAGE = "Storage";

    // Shared
    public static final long CONNECTION_TIME_OUT_MS = TimeUnit.SECONDS.toMillis(5);
    public static final long ANIM_FADE_TIME_MS = TimeUnit.SECONDS.toMillis(1);

   // public static  final String CLIENT_ID = "720698840782-ds1bdc9fh23i9ua7tj3d5rafk0i0r1vf.apps.googleusercontent.com";
    public static  final String CLIENT_ID = "550125154478-fclhtg3gjm974vn0lk8rvvmshn4ht40b.apps.googleusercontent.com";

    public static final String ATRACKIT_LICENCE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlloLPPJ3h5LC9RlL1oRCp4eREJoMpdYr3dmS9IX1u9WvEEDAbjlIU9TATYs1YHrc8tqe8pFrTT0n/DC1ZmgH0xEr1Q4q8Kc9z7NJO0upkHY0QSwS32ZWISalnvwCgsy9m9laTWaROj8UTq0wcdLeCKME8TgnHF8FZaUs6d8LrLeTPSgh6/fZZs9162CqwEOUW4qkDDB+xKSTMPma8IGg3ILsr5z9QE8pIr1rGDL3+3UgvGjh/cH1+Nanhx6K8WPbCJH+CNJRaDmHLPljvZv1Sc1ghEFSGOoBkbiZkojNh/ta2uh3XB7dTDmLP8kiLxQmmj8XdyCLXLfNfBhYIIuCOQIDAQAB";
    public static final byte[] SALT = new byte[] { 105, -12, 112, 82, -85, -10, -11, 61, 15, 64, -44, -66, -117, -89, -64, 110, -53, 123, 33
    };
    public static final long DURATION_MIN_TIME_MILLIS = TimeUnit.MINUTES.toMillis(2);
    public static final String DATE_FORMAT_DAYTIME = "EEE HH:mm";
    public static final String DATE_FORMAT_24Full = "dd MMM HH:mm";
    public static final String DATE_FORMAT_YEARDAY = "YY-MM-dd";

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    public static final int NOTIFICATION_ACTIVE_ID = 11345688;
    public static final int NOTIFICATION_SUMMARY_ID = 11345687;
    public static final int NOTIFICATION_GOAL_ID = 11345686;
    public static final int NOTIFICATION_SCHEDULE_ID = 11345685;
    public static final int NOTIFICATION_MAINTAIN_ID = 11345684;
    public static final int NOTIFICATION_FIREBASE_ID = 11345681;

    public static final String BRIDGE_TAG_FIREBASE = "firebase";
    public static final String BRIDGE_TAG_SYNC = "notifySync";

    public  static final String ACTIVE_CHANNEL_ID = "notify_active_channel";
    public  static final String SUMMARY_CHANNEL_ID = "notify_summary_channel";
    public  static final String GOALS_CHANNEL_ID = "notify_goals_channel";
    public  static final String MAINTAIN_CHANNEL_ID = "notify_maintain_channel";
    public  static final String FIREBASE_CHANNEL_ID = "notify_firebase_channel";

    public static final String OUTPUT_PATH = "images";
    public static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";
    public static final String KEY_FIT_ACTIVITYID = "KEY_FIT_ACTIVITY_ID";
    public static final String KEY_FIT_WORKOUTID = "KEY_FIT_WORKOUT_ID";
    public static final String KEY_FIT_WORKOUT_SETID = "KEY_FIT_WORKOUT_SETID";
    public static final String KEY_FIT_WORKOUT_METAID = "KEY_FIT_WORKOUT_META_ID";
    public static final String KEY_FIT_DEVICE_ID = "KEY_FIT_DEVICE_ID";
    public static final String KEY_FIT_USER = "KEY_FIT_USER";
    public static final String KEY_FIT_ACTION = "KEY_FIT_ACTION";
    public static final String KEY_FIT_RECOG = "KEY_ACT_RECOG";
    public static final String KEY_FIT_NAME = "KEY_ACT_NAME";
    public static final String KEY_FIT_TYPE = "KEY_ACT_TYPE";
    public static final String KEY_FIT_REC = "KEY_ACT_REC";
    public static final String KEY_FIT_VALUE = "KEY_ACT_VALUE";
    public static final String KEY_FIT_BUNDLE = "KEY_ACT_BUNDLE";
    public static final String KEY_FIT_HOST = "KEY_ACT_HOST";
    public static final String KEY_FIT_TIME = "KEY_TIME_VALUE";
    public static final String KEY_FIT_SETS = "KEY_SET_ID_VALUE";
    public static final String KEY_LIST_WORKOUTS = "KEY_WORKOUT_LIST";
    public static final String KEY_LIST_SETS = "KEY_SET_LIST";
    public static final String KEY_LIST_META = "KEY_META_LIST";
    public static final String KEY_AGG_META = "KEY_AGG_META_WORKOUT";
    public static final String KEY_AGG_WORKOUT = "KEY_AGG_WORKOUT";
    public static final String KEY_AGG_BODYPART = "KEY_AGG_BODYPART";
    public static final String KEY_AGG_EXERCISE = "KEY_AGG_EXERCISE";
    public static final String KEY_COMM_TYPE = "communicationType";
    public static final String KEY_CHANGE_STATE = "changeState";
    public static final String KEY_PAYLOAD = "payload";
    public static final String KEY_RESULT = "result";
    public static final String KEY_USE_KG = "useKG";
    public static final String KEY_INDEX_GROUP = "KEY_GROUP";
    public static final String KEY_INDEX_METRIC = "KEY_METRIC";
    public static final String KEY_INDEX_UOY = "KEY_UNIT_OF_YEAR";
    public static final String KEY_INDEX_FILTER = "KEY_FILTER";
    public static final String KEY_GOOGLE_LIKE = "com.google.%";
    public static final String KEY_LOC_LAT = "KEY_LOC_LAT";
    public static final String KEY_LOC_LNG = "KEY_LOC_LNG";
    public static final String KEY_LOC_ALT = "KEY_LOC_ALT";
    public static final String KEY_LOC_SPD = "KEY_LOC_SPD";
    public static final String KEY_LOC_LOC = "KEY_LOC_LOC";
    public static final String KEY_DEVICE1 = "Device1";
    public static final String KEY_DEVICE2 = "Device2";

    public static final String
            PHONE_CAPABILITY_NAME = "phone_capable";  //phone capabilities
    public static final String
            WEAR_CAPABILITY_NAME = "wear_capable";  //wear capabilities
    public static final String
            BPM_CAPABILITY_NAME = "bpm_capable";  //wear capabilities
    public static final String
            STEP_CAPABILITY_NAME = "step_capable";  //wear capabilities
    // Phone - DataClients
    // Wear - DataClients
    // Wearable Communications
    public static final String MESSAGE_PATH_WEAR = "/wear_message_path";
    public static final String MESSAGE_PATH_WEAR_SERVICE = "/wear_message_path_service";
    public static final String MESSAGE_PATH_PHONE = "/phone_message_path";
    public static final String MESSAGE_PATH_PHONE_SERVICE = "/phone_message_path_service";
    public static final String MSG_SENSOR_RECEIVED_PATH = "/sensor-item";
    public static final String WEAR_DATA_BUNDLE_RECEIVED_PATH = "/wear-data-bundle";
    public static final String WEAR_DATA_ITEM_RECEIVED_PATH = "/wear-data-item";
    public static final String WEAR_SYNC_ITEM_RECEIVED_PATH = "/wear-sync-item";
    public static final String PHONE_DATA_BUNDLE_RECEIVED_PATH = "/phone-data-bundle";
    public static final String PHONE_DATA_ITEM_RECEIVED_PATH = "/phone-data-item";
    public static final String PHONE_SYNC_ITEM_RECEIVED_PATH = "/phone-sync-item";
    public static final String DATA_START_ACTIVITY = "/data-start-activity";
    public static final String DATA_START_WORKOUT = "/data-start-workout";
    public static final String DATA_START_WORKOUT_SET = "/data-start-workout-set";
    public static final String DATA_STOP_WORKOUT = "/data-stop-workout";
    public static final String DATA_STOP_WORKOUT_SET = "/data-stop-workout-set";
    public static final String COUNT_PATH = "/count";
    public static final String CAMERA_PATH = "/camera";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";
    public static final String KEY_FIT_ACTION_QUIT = "quit";
    public static final String KEY_FIT_ACTION_QUICK = "quick";
    // SPEECH TARGETS
    public static final String TARGET_ROUTINE_NAME = "workout_table.sessionName";
    public static final String TARGET_EXERCISE_NAME = "exercise_table.exerciseName";
    // Requests
    public static final int COMM_TYPE_REQUEST_PROMPT_PERMISSION = 1;
    public static final int COMM_TYPE_REQUEST_DATA = 2;
    public static final int COMM_TYPE_REQUEST_INFO = 3;
    public static final int COMM_TYPE_DEVICE_UPDATE = 4;
    public static final int COMM_TYPE_DAILY_UPDATE = 5;
    public static final int COMM_TYPE_SENSOR_UPDATE = 6;
    public static final int COMM_TYPE_STARTUP_INFO = 7;
    public static final int COMM_TYPE_SETUP_INFO = 8;
    public static final int COMM_TYPE_TABLE_INFO = 9;
    public static final int COMM_TYPE_URL_VIEW = 10;
    public static final int COMM_TYPE_REQUEST_ACTION = 11;

    // Responses
    public static final int COMM_TYPE_RESPONSE_PERMISSION_REQUIRED = 1001;
    public static final int COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION = 1002;
    public static final int COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION = 1003;
    public static final int COMM_TYPE_RESPONSE_DATA = 1004;


    public static final String TIME_DATE_FORMAT = "dd/MM h:mm";
    public static final String INTENT_REFRESH = "com.a_track_it.com.workout.START_REFRESH";
    public static final String INTENT_SCREEN = "com.a_track_it.com.workout.screen";
    public static final String INTENT_RECOG = "com.a_track_it.com.workout.ACT_RECOG";
    public static final String INTENT_DAILY = "com.a_track_it.com.workout.ACT_DAILY";
    public static final String INTENT_EXER_RECOG = "com.a_track_it.workout.EXER_RECOG";
    public static final String INTENT_WIFI_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final String INTENT_NETWORK_CHANGED = "android.net.wifi.STATE_CHANGE";
    public static final String INTENT_BIND_DEVICE = "com.a_track_it.com.workout.device";

    public static final String INTENT_SETUP = "com.a_track_it.com.workout.service.action.SETUP"; // "com.a_track_it.com.workout.START_SETUP";
    public static final String INTENT_CLOUD_SYNC = "com.a_track_it.com.workout.sync";
    public static final String INTENT_CLOUD_POPULATE = "com.a_track_it.com.workout.populate";
    public static final String INTENT_PHONE_SYNC = "com.a_track_it.com.workout.phone.sync";
    public static final String INTENT_PHONE_REQUEST = "com.a_track_it.com.workout.phone.request";
    public static final String INTENT_CLOUD_META = "com.a_track_it.com.workout.meta";
    public static final String INTENT_CLOUD_LICENCE = "com.a_track_it.workout.licence";
    public static final String INTENT_CLOUD_SKU = "com.a_track_it.workout.sku";

    public static final String INTENT_INPROGRESS_RESUME = "com.a_track_it.com.workout.workout.INPROGRESS";
    public static final String INTENT_ACTIVE_LOGIN = "com.a_track_it.com.workout.LOGIN";
    public static final String INTENT_ACTIVE_LOGOUT = "com.a_track_it.com.workout.LOGOUT";
    public static final String INTENT_ACTIVE_START = "com.a_track_it.com.workout.workout.START";
    public static final String INTENT_TEMPLATE_START = "com.a_track_it.com.workout.workout.start.TEMPLATE";
    public static final String INTENT_EXERCISE_NEW = "com.a_track_it.com.workout.exercise.NEW";
    public static final String INTENT_EXERCISE_PENDING = "com.a_track_it.com.workout.exercise.PENDING";
    public static final String INTENT_NETWORK_CHECK = "com.a_track_it.com.workout.NETWORK";
    public static final String INTENT_ACTIVE_STOP = "com.a_track_it.com.workout.workout.STOP";
    public static final String INTENT_ACTIVE_PAUSE = "com.a_track_it.com.workout.workout.PAUSE";
    public static final String INTENT_ACTIVE_RESUMED = "com.a_track_it.com.workout.workout.RESUME";
    public static final String INTENT_ACTIVESET_START = "com.a_track_it.com.workout.workoutset.START";
    public static final String INTENT_WORKOUT_DELETE = "com.a_track_it.com.workout.workout.DELETE";
    public static final String INTENT_SET_DELETE = "com.a_track_it.com.workout.workoutset.DELETE";

    public static final String INTENT_ACTIVESET_STOP = "com.a_track_it.com.workout.workoutset.STOP";
    public static final String INTENT_ACTIVESET_SAVED = "com.a_track_it.com.workout.workoutset.SAVE";
    public static final String INTENT_WORKOUT_REPORT = "com.a_track_it.com.workout.workout.REPORT";
    public static final String INTENT_WORKOUT_EDIT = "com.a_track_it.com.workout.workout.EDIT";
    public static final String INTENT_SUMMARY_DAILY = "com.a_track_it.com.workout.daily.REPORT";
    public static final String INTENT_GOAL_TRIGGER = "com.a_track_it.com.workout.goal.TRIGGER";
    public static final String INTENT_CALL_TRIGGER = "com.a_track_it.com.workout.goal.CALL";
    public static final String INTENT_SCHEDULE_TRIGGER = "com.a_track_it.com.workout.workout.SCHEDULE";
    public static final String INTENT_QUIT_APP = "com.a_track_it.com.workout.QUIT";
    public static final String INTENT_HOME_REFRESH = "com.a_track_it.com.workout.HOME";
    public static final String INTENT_TOTALS_REFRESH = "com.a_track_it.com.workout.TOTALS";

    public static final String INTENT_EXTRA_WORKOUT = "com.a_track_it.com.workout.workout.data";
    public static final String INTENT_EXTRA_SET = "com.a_track_it.com.workout.workoutset.data";
    public static final String INTENT_EXTRA_MSG = "com.a_track_it.com.workout.workout.message";
    public static final String INTENT_EXTRA_RESULT = "com.a_track_it.com.workout.workout.result";
    public static final String INTENT_LOCATION_UPDATE = "com.a_track_it.workout.location.LOCATION_UPDATE";
    public static final String INTENT_MESSAGE_TOAST = "com.a_track_it.workout.message.TOAST";
    public static final String INTENT_VIBRATE = "com.a_track_it.workout.message.VIBRATE";
    public static final String INTENT_PERMISSION_LOCATION = "com.a_track_it.workout.permission.LOCATION";
    public static final String INTENT_PERMISSION_SENSOR = "com.a_track_it.workout.permission.SENSOR";
    public static final String INTENT_PERMISSION_RECOG = "com.a_track_it.workout.permission.RECOG";
    public static final String INTENT_PERMISSION_AGE = "com.a_track_it.workout.permission.AGE";
    public static final String INTENT_PERMISSION_HEIGHT = "com.a_track_it.workout.permission.HEIGHT";
    public static final String INTENT_PERMISSION_DEVICE = "com.a_track_it.workout.permission.DEVICE";
    public static final String INTENT_PERMISSION_POLICY = "com.a_track_it.workout.permission.POLICY";
    public static final String ACTIVITY_FILENAME = "activity.json";
    public static final String BODYPART_FILENAME = "bodypart.json";
    public static final String EXERCISE_FILENAME = "exercise.json";

    public static final String ROUTINE_SAMPLE_FILENAME = "routineSample.json";
    public static final String COMPLETED_WORKOUTS = "workouts_history.json";
    public static final String HISTORY_WORKOUTS = "fitness_history.json";
    public static final String ACTIVE_WORKOUT_SET = "active_workout_set.json";
    public static final String ACTIVE_WORKOUT = "active_workout.json";
    public static final String ACTIVE_TODO_SETS = "todo_sets.json";
    public static final String COMPLETED_SETS_HISTORY = "sets_history.json";
    public static final String STATE_IN_PROGRESS = "In-Progress";
    public static final String LABEL_CURRENT_SET = "current_set";
    public static final String LABEL_WORKOUT = "workout";
    public static final String LABEL_FRAG_STATE = "state";
    public static final String LABEL_SET = "set";
    public static final String LABEL_USER = "atktuser";
    public static final String LABEL_USE_GRID = "UseGridView";
    public static final String FB_WORKOUT_START = "workout_start";
    public static final String FB_WORKOUT_STOP = "workout_stop";
    public static final String FB_WORKOUT_PAUSE = "workout_pause";
    public static final String FB_WORKOUT_RESUME = "workout_resume";
    public static final String FB_SEGMENT_START = "segment_start";
    public static final String FB_SEGMENT_STOP = "segment_stop";
    public static final String FB_WORKOUT_HISTORY = "workout_history";

    public static final String MAP_DATA_TYPE = "DataType";
    public static final String MAP_MOVE_MINS = "MoveMins";
    public static final String MAP_HEART_POINTS = "HeartPts";
    public static final String MAP_HEART_DURATION = "HeartDuration";
    public static final String MAP_STEPS = "Steps";
    public static final String MAP_ALTITUDE = "Altitude";
    public static final String MAP_DISTANCE = "Distance";
    public static final String MAP_CALORIES = "Calories";
    public static final String MAP_WATTS = "Watts";
    public static final String MAP_START = "Start";
    public static final String MAP_END = "End";
    public static final String MAP_COUNT = "Count";
    public static final String MAP_BPM_AVG = "BPMAvg";
    public static final String MAP_BPM_MIN = "BPMMin";
    public static final String MAP_BPM_MAX = "BPMMax";   
    public static final String MAP_SPEED_AVG = "SPEEDAvg";
    public static final String MAP_SPEED_MIN = "SPEEDMin";
    public static final String MAP_SPEED_MAX = "SPEEDMax";
    public static final String MAP_HISTORY_RANGE = "HistoryRange";
    public static final String MAP_HISTORY_START = "HistoryStart";
    public static final String MAP_HISTORY_END = "HistoryEnd";
    public static final String MAP_CURRENT_STATE = "CurrentState";
    public static final String MAP_CURRENT_USER = "CurrentUser";

    public static final int ACTION_STARTING = -1;
    public static final int ACTION_STOPPING = -2;
    public static final int ACTION_RESUMING = -3;
    public static final int ACTION_CANCELLING = -4;
    public static final int ACTION_EXITING = -5;
    public static final int ACTION_QUICK_STOP = -6;
    public static final int ACTION_STOP_QUIT = -7;
    public static final int ACTION_SIGNOUT_QUIT = -8;
    public static final int ACTION_END_SET = -9;
    public static final int ACTION_START_SET = -10;
    public static final int ACTION_PAUSING = -11;
    public static final int ACTION_REPEAT_SET = -12;
    public static final int ACTION_DELETE_SET = -13;
    public static final int ACTION_DELETE_WORKOUT = -14;
    public static final int ACTION_QUICK_REPORT = -15;
    public static final int TASK_ACTION_START_SESSION = 1;
    public static final int TASK_ACTION_STOP_SESSION = 2;
    public static final int TASK_ACTION_READ_HISTORY = 3;
    public static final int TASK_ACTION_EXER_SEGMENT = 4;
    public static final int TASK_ACTION_ACT_SEGMENT = 5;
    public static final int TASK_ACTION_INSERT_HISTORY = 6;
    public static final int TASK_ACTION_READ_SESSION = 7;
    public static final int TASK_ACTION_READ_BPM = 8;
    public static final int TASK_ACTION_SYNC_WORKOUT = 9;
    public static final int TASK_ACTION_READ_GOALS = 10;
    public static final int TASK_ACTION_READ_LOCAL = 11;
    public static final int TASK_ACTION_WRITE_CLOUD = 12;
    public static final int TASK_ACTION_SYNC_DEVICE = 13;
    public static final int TASK_ACTION_READ_CLOUD = 14;
    public static final int TASK_ACTION_RECORD_START = 15;
    public static final int TASK_ACTION_RECORD_END = 16;
    public static final int TASK_ACTION_DAILY_SUMMARY = 17;


    public static final int OBJECT_TYPE_BODY_REGION = 1;
    public static final int OBJECT_TYPE_BODYPART = 2;
    public static final int OBJECT_TYPE_EXERCISE = 3;
    public static final int OBJECT_TYPE_DAILY_TOTAL = 4;
    public static final int OBJECT_TYPE_RESISTANCE_TYPE = 5;
    public static final int OBJECT_TYPE_WORKOUT = 10;
    public static final int OBJECT_TYPE_WORKOUT_SET = 11;
    public static final int OBJECT_TYPE_WORKOUT_META = 12;
    public static final int OBJECT_TYPE_WORKOUT_AGG = 13;



    public static final int QUESTION_PAUSESTOP = 8;
    public static final int QUESTION_RESUME_END = 9;
    public static final int QUESTION_DURATION_DELETE = 10;
    public static final int QUESTION_LOCATION = 11;
    public static final int QUESTION_SENSORS = 12;
    public static final int QUESTION_ACT_RECOG = 13;
    public static final int QUESTION_AGE = 14;
    public static final int QUESTION_HEIGHT = 21;
    public static final int QUESTION_POLICY = 15;
    public static final int QUESTION_NETWORK = 16;
    public static final int QUESTION_DEVICE = 17;
    public static final int QUESTION_DAILY_REFRESH = 18;
    public static final int QUESTION_HISTORY_LOAD = 19;
    public static final int QUESTION_KEEP_DELETE = 20;
    public static final int QUESTION_PICK_EXERCISE = 22;
    public static final int QUESTION_DELETE_SET = 23;
    public static final int QUESTION_DELETE_WORKOUT = 24;
    public static final int QUESTION_HISTORY_CREATE = 25;
    public static final int QUESTION_COPY_SET = 26;
    public static final int QUESTION_NOTIFY = 27;
    public static final int QUESTION_AUDIO = 28;
    public static final int QUESTION_VIBRATE = 29;
    public static final int QUESTION_STORAGE = 30;

    public static final long WORKOUT_TYPE_STEPCOUNT = -2;
    public static final long WORKOUT_TYPE_TIME = -1;
    public static final long WORKOUT_TYPE_INVEHICLE = 0;
    public static final long WORKOUT_TYPE_BIKING = 1;
    public static final long WORKOUT_TYPE_STILL = 3;
    public static final long WORKOUT_TYPE_UNKNOWN = 4;
    public static final long WORKOUT_TYPE_WALKING = 7;
    public static final long WORKOUT_TYPE_RUNNING = 8;
    public static final long WORKOUT_TYPE_AEROBICS = 9;
    public static final long WORKOUT_TYPE_KAYAKING = 40;
    public static final long WORKOUT_TYPE_STRENGTH = 80;
    public static final long WORKOUT_TYPE_ARCHERY = 119;
    public static final long WORKOUT_TYPE_ATRACKIT = 500;
    public static final long WORKOUT_TYPE_VIDEOGAME = 501;
    public static final long WORKOUT_TYPE_TENNIS = 87;
    public static final long WORKOUT_TYPE_GOLF = 32;
    public static final long WORKOUT_TYPE_WINTER = 61;
    // CustomListFragment selection types
    public static final int SELECTION_FITNESS_ACTIVITY = 1000;
    public static final int SELECTION_BODYPART = 1;
    public static final int SELECTION_EXERCISE = 2;
    public static final int SELECTION_ROUTINE = 3;
    public static final int SELECTION_USER_PREFS = 4;
    public static final int SELECTION_WEIGHT_KG = 5;
    public static final int SELECTION_REPS = 6;
    public static final int SELECTION_TARGET_FIELD = 7;
    public static final int SELECTION_TARGET_DISTANCE_FIELD = 8;
    public static final int SELECTION_TARGET_DISTANCE_TARGET = 9;
    public static final int SELECTION_TARGET_EQUIPMENT = 10;
    public static final int SELECTION_TARGET_TARGET_SIZE_FIELD = 11;
    public static final int SELECTION_TARGET_TARGET_SIZE_TARGET = 12;
    public static final int SELECTION_TARGET_ENDS = 13;
    public static final int SELECTION_TARGET_SHOTS_PER_END = 14;
    public static final int SELECTION_TARGET_POSSIBLE_SCORE = 15;
    public static final int SELECTION_SETS = 16;
    public static final int SELECTION_REST_DURATION_GYM = 17;
    public static final int SELECTION_REFRESH_START = 47;
    public static final int SELECTION_TEMPLATE = 48;
    public static final int SELECTION_MAP = 49;
    public static final int SELECTION_BODYPART_AGG = 50;
    public static final int SELECTION_EXERCISE_AGG = 51;
    public static final int SELECTION_WORKOUT_AGG = 52;
    public static final int SELECTION_WORKOUT_REPORT = 53;
    public static final int SELECTION_SENSOR_BINDINGS = 54;
    public static final int SELECTION_WEIGHT_LBS = 18;
    public static final int SELECTION_ACTIVITY_CARDIO = 19;
    public static final int SELECTION_ACTIVITY_SPORT = 20;
    public static final int SELECTION_ACTIVITY_GYM = 21;
    public static final int SELECTION_ACTIVITY_RUN = 22;
    public static final int SELECTION_ACTIVITY_WATER = 23;
    public static final int SELECTION_ACTIVITY_WINTER = 24;
    public static final int SELECTION_ACTIVITY_BIKE = 25;
    public static final int SELECTION_ACTIVITY_MISC = 26;
    public static final int SELECTION_REST_DURATION_TARGET = 27;
    public static final int SELECTION_WORKOUT_HISTORY = 28;
    public static final int SELECTION_WORKOUT_SET_HISTORY = 29;
    public static final int SELECTION_GOOGLE_HISTORY = 30;
    public static final int SELECTION_TO_DO_SETS = 31;
    public static final int SELECTION_ACTIVE_SESSION = 32;
    public static final int SELECTION_ACTIVE_SET = 33;
    public static final int SELECTION_DAYS = 34;
    public static final int SELECTION_MONTHS = 35;
    public static final int SELECTION_GOAL_STEPS = 36;
    public static final int SELECTION_GOAL_DURATION = 37;
    public static final int SELECTION_WEIGHT_BODYWEIGHT = 38;
    public static final int SELECTION_BODY_REGION = 41;
    public static final int SELECTION_WORKOUT_INPROGRESS = 42;
    public static final int SELECTION_INCOMPLETE_DURATION = 43;
    public static final int SELECTION_REST_DURATION_SETTINGS = 44;
    public static final int SELECTION_WORKOUT_TEMPLATES = 45;
    public static final int CHOOSE_REPEAT_SET = 39;
    public static final int CHOOSE_ADD_SET = 40;
    public static final int SELECTION_DOY = 46;
    public static final int SELECTION_ACTIVITY_SHOOT = 119;
    public static final int SELECTION_CALL_DURATION = 47;
    public static final int SELECTION_END_DURATION = 48;
    public static final int WORKOUT_SETUP = -3;
    public static final int WORKOUT_TEMPLATE = -2;
    public static final int WORKOUT_INVALID = -1;
    public static final int WORKOUT_PENDING = 0;
    public static final int WORKOUT_LIVE = 1;
    public static final int WORKOUT_COMPLETED = 2;
    public static final int WORKOUT_SYNC = 4;
    public static final int WORKOUT_PAUSED = 5;
    public static final int WORKOUT_RESUMED = 6;  // theoretcially
    public static final int WORKOUT_CALL_TO_LINE = 7;

    public static final int UID_btnRegion = 1000;
    public static final int UID_btnBodypart = 1001;
    public static final int UID_btnExercise = 1002;
    public static final int UID_btnAddExercise = 1003;
    public static final int UID_btnSets = 1004;
    public static final int UID_btnReps = 1005;
    public static final int UID_btnWeight = 1006;
    public static final int UID_btnBuild = 1007;
    public static final int UID_btnStart = 1008;
    public static final int UID_btnFinish = 1009;
    public static final int UID_btnRest = 1010;
    public static final int UID_btnRoutineName = 1011;
    public static final int UID_btnSave = 1012;
    public static final int UID_btnContinue = 1013;
    public static final int UID_btnRepeat = 1014;
    public static final int UID_btnConfirmFinish = 1015;
    public static final int UID_btnConfirmEdit = 1016;
    public static final int UID_btnWeightPlus = 1017;
    public static final int UID_btnWeightMinus = 1018;
    public static final int UID_btnSetsPlus = 1019;
    public static final int UID_btnSetsMinus = 1020;
    public static final int UID_btnRepsPlus = 1021;
    public static final int UID_btnRepsMinus = 1022;
    public static final int UID_btnHomeStart = 1023;
    public static final int UID_textViewMsgLeft = 1024;
    public static final int UID_textViewMsgCenterLeft = 1025;

    public static final int UID_textViewMsgCenterRight = 1027;
    public static final int UID_textViewCenter = 1028;
    public static final int UID_textViewCenter1 = 1029;
    public static final int UID_textViewCenter3 = 1030;
    public static final int UID_textViewMsgRight = 1031;
    public static final int UID_textViewCenter2 = 1032;
    public static final int UID_chronometerViewCenter = 1033;
    public static final int UID_textViewMsgBottomLeft = 1034;
    public static final int UID_textViewMsgBottomRight = 1035;
    public static final int UID_textViewBottom = 1036;
    public static final int UID_chronoClock = 1048;
    public static final int UID_archery_confirm_next_end_button = 1037;
    public static final int UID_archery_confirm_exit_button = 1038;
    public static final int UID_settings_sign_out_button = 1039;
    public static final int UID_settings_find_phone_button = 1040;
    public static final int UID_settings_age_button = 1041;
    public static final int UID_settings_height_button = 1042;
    public static final int UID_settings_notifications_button = 1043;
    public static final int UID_settings_load_history_button = 1044;
    public static final int UID_settings_has_device_toggle = 1045;
    public static final int UID_btnSettingsRest = 1046;
    public static final int UID_SwipeView = 1047;
    public static final int UID_action_use_location = 1049;
    public static final int UID_action_open_template = 1050;
    public static final int UID_action_open_history = 1051;
    public static final int UID_action_settings = 1052;
    public static final int UID_action_bpm_permonth = 1053;
    public static final int UID_action_bpm_perhour = 1054;
    public static final int UID_action_phone_send = 1055;
    public static final int UID_action_signout = 1056;
    public static final int UID_action_data_policy = 1057;
    public static final int UID_archery_field_button = 1058;
    public static final int UID_archery_target_size = 1059;
    public static final int UID_archery_equipment_button = 1060;
    public static final int UID_archery_distance_button = 1061;
    public static final int UID_archery_ends_button = 1062;
    public static final int UID_archery_per_end_button = 1063;
    public static final int UID_entry_message1 = 1064;
    public static final int UID_entry_message2 = 1065;
    public static final int UID_archery_start_button = 1066;
    public static final int UID_archery_rest_button = 1067;
    public static final int UID_archery_call_button = 1091;

    public static final int UID_btnExerciseUseMatch = 1068;
    public static final int UID_home_image_view = 1069;
    public static final int UID_settings_read_permissions = 1070;
    public static final int UID_settings_sensors_permissions = 1071;
    public static final int UID_settings_location_permissions = 1072;
    public static final int UID_btnSaveHistory = 1073;
    public static final int UID_btn_recycle_item_delete = 1074;
    public static final int UID_btn_recycle_item_copy = 1075;
    public static final int UID_btn_recycle_item_select = 1076;
    public static final int UID_btn_recycle_item_report = 1077;
    public static final int UID_settings_sensors_use = 1078;
    public static final int UID_settings_location_use = 1079;
    public static final int UID_settings_load_goals_button = 1080;
    public static final int UID_btn_recycle_session_delete = 1081;
    public static final int UID_btn_recycle_session_edit = 1082;
    public static final int UID_btn_recycle_session_report = 1083;
    public static final int UID_btn_recycle_session_select = 1084;
    public static final int UID_settings_sensors_button = 1085;
    public static final int UID_action_phone_camera = 1086;
    public static final int UID_action_phone_pick = 1087;
    public static final int UID_settings_get_info = 1088;
    public static final int UID_settings_send_info = 1089;
    public static final int UID_toggle_track_activity = 1090;
    public static final int UID_archery_end_button = 1092;
    public static final int UID_settings_show_goals_button = 1093;
/*    public static final int UID_action_send_workout = 1091;
    public static final int UID_action_template_workout = 1092;
    public static final String CHANNEL_HOME = "ATrackIt";
    public static final String CHANNEL_ENTRY = "ATrackIt";
    public static final String CHANNEL_LIVE = "ATrackIt";
    public static final String CHANNEL_ENDSET = "ATrackIt";
    public static final String CHANNEL_REPORT = "ATrackIt"; */
    public static final String SESSION_PREFIX = "AKT";
    private Constants() {}
}