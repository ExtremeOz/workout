package com.a_track_it.fitdata.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class FitnessRecordingWorker extends Worker {
    private static final String LOG_TAG = FitnessRecordingWorker.class.getSimpleName();
    private GoogleSignInAccount gsa;
    private Context mContext;

    public FitnessRecordingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        mContext = getApplicationContext();
        gsa = GoogleSignIn.getLastSignedInAccount(mContext);
        int record_action = getInputData().getInt("record_action", 1);
        if (gsa != null) {
            if (record_action == 1) {
                subscribeRecordingApiByDataType(DataType.TYPE_HEART_RATE_BPM);
                subscribeRecordingApiByDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
                subscribeRecordingApiByDataType(DataType.TYPE_CALORIES_EXPENDED);
                subscribeRecordingApiByDataType(DataType.TYPE_MOVE_MINUTES);
                subscribeRecordingApiByDataType(DataType.TYPE_LOCATION_SAMPLE);
                subscribeRecordingApiByDataType(DataType.TYPE_ACTIVITY_SAMPLES);
                subscribeRecordingApiByDataType(DataType.TYPE_WORKOUT_EXERCISE);
                subscribeRecordingApiByDataType(DataType.TYPE_POWER_SAMPLE);
            }else{
                unsubscribeRecordingApiByDataType(DataType.TYPE_HEART_RATE_BPM);
                unsubscribeRecordingApiByDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
                unsubscribeRecordingApiByDataType(DataType.TYPE_CALORIES_EXPENDED);
                unsubscribeRecordingApiByDataType(DataType.TYPE_MOVE_MINUTES);
                unsubscribeRecordingApiByDataType(DataType.TYPE_LOCATION_SAMPLE);
                unsubscribeRecordingApiByDataType(DataType.TYPE_ACTIVITY_SAMPLES);
                unsubscribeRecordingApiByDataType(DataType.TYPE_WORKOUT_EXERCISE);
                unsubscribeRecordingApiByDataType(DataType.TYPE_POWER_SAMPLE);
            }
        }
        return null;
    }

    /**
     *
     * Subscribes to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     */
    private void subscribeRecordingApiByDataType(final DataType dataType) {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        final String sLabel = mContext.getString(R.string.label_subs) + dataType.getName();
        Boolean defaultValue = UserPreferences.getPrefByLabel(getApplicationContext(), sLabel);
        try {
            if (defaultValue)  // only if turned on
                Fitness.getRecordingClient(mContext, gsa)
                    .subscribe(dataType)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(LOG_TAG, "Successfully subscribed! " + dataType.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(LOG_TAG, "There was a problem subscribing." + dataType.getName());
                            UserPreferences.setPrefByLabel(mContext, sLabel,false);  // turn it off

                        }
                    });
        }catch (Exception e){
            Log.e(LOG_TAG, "Recording client subscribe error" + e.getMessage());
            // isRecordingAPIRunning = false;
        }
        // [END subscribe_to_datatype]
    }
    private void unsubscribeRecordingApiByDataType(final DataType dataType) {
        try{
            String sLabel = mContext.getString(R.string.label_subs) + dataType.getName();
            Boolean defaultValue = UserPreferences.getPrefByLabel(getApplicationContext(), sLabel);
            if (defaultValue)
                Fitness.getRecordingClient(mContext, gsa).unsubscribe(dataType).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(LOG_TAG, "Successfully un-subscribed for data type: " + dataType.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Un-subscribe failure " + dataType.getName() + " " + e.getMessage());
                    }
                });
        }catch (Exception e){

        }
        // [END unsubscribe_to_datatype]

    }
}