package com.a_track_it.fitdata.common.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.a_track_it.fitdata.common.Constants;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import static com.a_track_it.fitdata.common.Constants.INTENT_RECOG;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_RECOG;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;

public class ActivityRecognizedService extends IntentService {
    private final static String LOG_TAG = ActivityRecognizedService.class.getSimpleName();

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
       try {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                List<DetectedActivity> probableList = result.getProbableActivities();
                handleDetectedActivities(probableList);
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"handleIntent " + e.getMessage());
        }
    }
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int iCounter = 0; String activityName = Constants.ATRACKIT_EMPTY;
        for( DetectedActivity activity : probableActivities ) {
            Integer activityType = activity.getType();
            switch( activityType ) {
                case DetectedActivity.IN_VEHICLE: {
                    activityName = Constants.RECOG_VEHCL.trim();
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    activityName = Constants.RECOG_BIKE.trim();
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    activityName = Constants.RECOG_FOOT.trim();
                    break;
                }
                case DetectedActivity.RUNNING: {
                    activityName = Constants.RECOG_RUN.trim();
                    break;
                }
                case DetectedActivity.STILL: {
                    activityName = Constants.RECOG_STILL.trim();
                    break;
                }
                case DetectedActivity.TILTING: {
                    activityName = Constants.RECOG_TILT.trim();
                    break;
                }
                case DetectedActivity.WALKING: {
                    activityName = Constants.RECOG_WALK.trim();
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    activityName = Constants.RECOG_UNKWN.trim();
                    break;
                }
            }
            if( activity.getConfidence() >= 50 && (iCounter == 0)) {
                Intent intent = new Intent(INTENT_RECOG);
                intent.putExtra(KEY_FIT_RECOG, activity);
                intent.putExtra(KEY_FIT_NAME, activityName);
                intent.putExtra(KEY_FIT_TYPE, activityType);
                intent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                getApplicationContext().sendBroadcast(intent);
            }
            iCounter += 1;
        }
    }
}
