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

package com.a_track_it.fitdata.common;

import java.util.concurrent.TimeUnit;

/**
 * A collection of constants that is shared between the wearable and handset apps.
 */
public class Constants {
    public static final int CHUNK_SIZE = 10;
    public static final int BATTERY_CHUNK_TYPE = 0;
    public static final int DATA_CHUNK_TYPE = 1;
    public static final int DELTA = 20;
    // Shared
    public static final long CONNECTION_TIME_OUT_MS = TimeUnit.SECONDS.toMillis(5);
    public static final long ANIM_FADE_TIME_MS = TimeUnit.SECONDS.toMillis(1);

    public static final String CHANNEL_ID = "channel_A01";
    public static final long DELAY_TIME_MILLIS = (1000 * 3);

    // Name of Notification Channel for verbose notifications of background work
    public static final CharSequence VERBOSE_NOTIFICATION_CHANNEL_NAME =
            "Verbose WorkManager Notifications";
    public static String VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
            "Shows notifications whenever work starts";
    public static final CharSequence NOTIFICATION_TITLE = "WorkRequest Starting";

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    public static final int NOTIFICATION_ID = 11345688;
    public static final String OUTPUT_PATH = "images";
    public static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";
    public static final String KEY_FIT_USER = "KEY_FIT_USER";
    public static final String KEY_FIT_ACTION = "KEY_FIT_ACTION";
    public static final String KEY_COMM_TYPE = "communicationType";
    public static final String KEY_PAYLOAD = "payload";


    // Requests
    public static final int COMM_TYPE_REQUEST_PROMPT_PERMISSION = 1;
    public static final int COMM_TYPE_REQUEST_DATA = 2;

    // Responses
    public static final int COMM_TYPE_RESPONSE_PERMISSION_REQUIRED = 1001;
    public static final int COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION = 1002;
    public static final int COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION = 1003;
    public static final int COMM_TYPE_RESPONSE_DATA = 1004;

    // Phone - DataClients
    public static final String CAPABILITY_PHONE_APP = "phone_app_runtime_permissions";
    public static final String MESSAGE_PATH_PHONE = "/phone_message_path";

    // Wear - DataClients
    public static final String CAPABILITY_WEAR_APP = "wear_app_runtime_permissions";
    public static final String MESSAGE_PATH_WEAR = "/wear_message_path";

    public static final String COUNT_PATH = "/count";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";

    public static final float LBS_TO_KG = 0.453592F;
    public static final float KG_TO_LBS = 2.20462F;
    public static final String TIME_DATE_FORMAT = "dd/MM h:mm";
    public static final String INTENT_REFRESH = "com.a_track_it.com.fitdata.START_REFRESH";
    public static final String INTENT_SETUP = "com.a_track_it.com.fitdata.service.action.SETUP"; // "com.a_track_it.com.fitdata.START_SETUP";
    public static final String INTENT_SERVICESTART = "com.a_track_it.com.fitdata.MyStartServiceReceiver";
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

    public static final int WORKOUT_TYPE_STEPCOUNT = -2;
    public static final int WORKOUT_TYPE_TIME = -1;
    public static final int WORKOUT_TYPE_INVEHICLE = 0;
    public static final int WORKOUT_TYPE_BIKING = 1;
    public static final int WORKOUT_TYPE_STILL = 3;
    public static final int WORKOUT_TYPE_UNKNOWN = 4;
    public static final int WORKOUT_TYPE_WALKING = 7;
    public static final int WORKOUT_TYPE_RUNNING = 8;
    public static final int WORKOUT_TYPE_AEROBICS = 9;
    public static final int WORKOUT_TYPE_KAYAKING = 40;
    public static final int WORKOUT_TYPE_STRENGTH = 80;
    public static final int WORKOUT_TYPE_ARCHERY = 119;
    public static final int WORKOUT_TYPE_VIDEOGAME = 501;
    // CustomListFragment selection types
    public static final int SELECTION_FITNESS_ACTIVITY = 0;
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

    public static final int CHOOSE_ACTIIVITY = 1;
    public static final int CHOOSE_BODYPART = 2;
    public static final int CHOOSE_EXERCISE = 3;
    public static final int CHOOSE_WEIGHT = 4;
    public static final int CHOOSE_GOAL_1 = 5;
    public static final int CHOOSE_GOAL_2 = 6;
    public static final int CHOOSE_SETS = 7;
    public static final int CHOOSE_REPS = 8;
    public static final int CHOOSE_TARGET_FIELD = 9;
    public static final int CHOOSE_DISTANCE = 10;
    public static final int CHOOSE_EQUIPMENT = 11;
    public static final int CHOOSE_TARGET_SIZE = 12;
    public static final int CHOOSE_ENDS = 13;
    public static final int CHOOSE_POSSIBLE_SCORE = 14;
    public static final int CHOOSE_PER_END = 15;
    public static final int CHOOSE_START_SESSION = 16;
    public static final int CHOOSE_CONTINUE_SESSION = 17;
    public static final int CHOOSE_BUILD_SESSION = 18;
    public static final int CHOOSE_BUILD_LONGCLICK = 41;
    public static final int CHOOSE_NEW_EXERCISE = 19;
    public static final int CHOOSE_REPEAT_SET = 39;
    public static final int CHOOSE_ADD_SET = 40;

    public static final int STATE_HOME = 0;
    public static final int STATE_ENTRY = 1;
    public static final int STATE_LIVE = 2;
    public static final int STATE_END_SET = 3;
    public static final int STATE_REPORT = 4;
    public static final int STATE_SETTINGS = 5;
    public static final int STATE_DIALOG = 6;
    public static final int STATE_15DIALOG = 7;
    public static final String CHANNEL_HOME = "ATrackIt";
    public static final String CHANNEL_ENTRY = "ATrackIt";
    public static final String CHANNEL_LIVE = "ATrackIt";
    public static final String CHANNEL_ENDSET = "ATrackIt";
    public static final String CHANNEL_REPORT = "ATrackIt";
    public static final String SESSION_PREFIX = "AKT";
    private Constants() {}

    /**
     * Created by Chris Black
     * Moved WorkoutTypes to here - Daniel Haywood
     */

}