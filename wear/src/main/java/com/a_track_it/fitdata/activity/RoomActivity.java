package com.a_track_it.fitdata.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.BuildConfig;
import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.data_model.PeakDetProcessor;
import com.a_track_it.fitdata.data_model.Processor;
import com.a_track_it.fitdata.data_model.SessionViewModel;
import com.a_track_it.fitdata.data_model.Word;
import com.a_track_it.fitdata.user_model.LiveDataTimerViewModel;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.a_track_it.fitdata.workers.BoundFitnessSensorManager;
import com.a_track_it.fitdata.workers.BoundFusedLocationClient;
import com.a_track_it.fitdata.workers.BoundSensorManager;

import com.a_track_it.fitdata.workers.FitnessRecordingWorker;
import com.a_track_it.fitdata.workers.HistoryWorker;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.KEY_IMAGE_URI;

//import androidx.lifecycle.Observer;

public class RoomActivity extends AppCompatActivity implements AmbientModeSupport.AmbientCallbackProvider{
    private static final String LOG_TAG = RoomActivity.class.getSimpleName();
    public  static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 2;
    private static final int REQUEST_SENSOR_PERMISSION_CODE = 3;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 5004;
    private static final String TIME_FORMAT = "HH:mm:ss";
    private SessionViewModel mWordViewModel;
    private WorkManager mWorkManager;
    private GoogleSignInAccount mGoogleAccount;
    private boolean bRecordingApiRunning = false;
    private LocationCallback mLocationListener;
    private SensorEventListener mSensorListener = new xSensorListener(1);
    private OnDataPointListener mDataPointListener = new xFITSensorListener();
    private MessagesViewModel mMessagesViewModel;
    private LiveDataTimerViewModel mTimerViewModel;
    private ReferencesTools mReferenceTools;
    private Geocoder mGeoCoder;
    private Location mLocation;
    private SimpleDateFormat simpleDateFormat;
    /**
     * Ambient mode controller attached to this display. Used by the Activity to see if it is in
     * ambient mode.
     */
    private AmbientModeSupport.AmbientController mAmbientController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        if (!UserPreferences.getAppSetupCompleted(context)){
            Intent myInitialIntent = new Intent(context, InitialActivity.class);
            startActivity(myInitialIntent);
            finish();
            return;
        }
        setContentView(R.layout.activity_room_home);
        if(Answers.getInstance() != null) {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("EnterMain")
                    .putContentType("Create")
                    .putContentId("RoomActivity"));
        }
        // Enables Always-on
        // Enables Ambient mode.
        mAmbientController = AmbientModeSupport.attach(this);
        mWorkManager = WorkManager.getInstance(context);
        mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(context);
        this.mGeoCoder = new Geocoder(context, Locale.getDefault());
        this.simpleDateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        mMessagesViewModel = ViewModelProviders.of(RoomActivity.this).get(MessagesViewModel.class);
        mTimerViewModel = ViewModelProviders.of(RoomActivity.this).get(LiveDataTimerViewModel.class);

        mMessagesViewModel.getExercisesMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(LOG_TAG, "Exercise detected " + s);
            }
        });

        final MaterialButton btn_home = findViewById(R.id.button_room_home);
        final TextView textViewLeft = findViewById(R.id.textViewMsgLeft);
        final TextView textViewCenter = findViewById(R.id.textViewMsgCenter);
        final TextView textViewRight = findViewById(R.id.textViewMsgRight);
        mMessagesViewModel.getBpmMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (textViewLeft!= null) textViewLeft.setText(s);
            }
        });
        mTimerViewModel.getCurrentTime().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                if (textViewCenter!=null) textViewCenter.setText(simpleDateFormat.format(aLong));
            }
        });
        mMessagesViewModel.getStepsMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (textViewRight!=null) textViewRight.setText(s);
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRecordingWork(bRecordingApiRunning ? 0 : 1);
                 Intent intent = new Intent(RoomActivity.this, NewWordActivity.class);
                 startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
            }
        });
        btn_home.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent myMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                myMainIntent.putExtra("MSG", getString(R.string.action_continue));
                startActivity(myMainIntent);
                Log.i(LOG_TAG, "started Main finishing RoomActivity");
                finish();
                return false;
            }
        });

        /*        RoomCustomScrollingLayoutCallback customScrollingLayoutCallback = new RoomCustomScrollingLayoutCallback();
                androidx.wear.widget.WearableRecyclerView recyclerView = findViewById(R.id.recyclerview);
                final WordListAdapter adapter = new WordListAdapter(context);
                recyclerView.setAdapter(adapter);
                //recyclerView.setLayoutManager(new LinearLayoutManager(context));
                WearableLinearLayoutManager mWearableLinearLayoutManager = new WearableLinearLayoutManager(context, customScrollingLayoutCallback);
                recyclerView.setLayoutManager(mWearableLinearLayoutManager);
                recyclerView.setCircularScrollingGestureEnabled(false);
                recyclerView.setBezelFraction(0.5f);
                recyclerView.setScrollDegreesPerScreen(90);
                // To align the edge children (first and last) with the center of the screen
                recyclerView.setEdgeItemsCenteringEnabled(true);
                recyclerView.setHasFixedSize(true);
                mWordViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

                mWordViewModel.getAllWords().observe(this, new Observer<List<Word>>() {
                    @Override
                    public void onChanged(@Nullable final List<Word> words) {
                        // Update the cached copy of the words in the adapter.
                        adapter.setWords(words);

                    }
                });*/
        if (!hasOAuthPermission())
            requestOAuthPermission();
        else {
            bindFitSensorListener();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(RoomActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_CODE);
            } else {
                bindLocationListener();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(RoomActivity.this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            REQUEST_SENSOR_PERMISSION_CODE);
                } else {
                    bindSensorListener(1);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==  REQUEST_OAUTH_REQUEST_CODE)
            if (resultCode == android.app.Activity.RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result != null) {
                    mGoogleAccount = result.getSignInAccount();
                    Log.i(LOG_TAG, "Fitness permissions have been granted");
                    if (!GoogleSignIn.hasPermissions(mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE) ,new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))) {
                        Log.i(LOG_TAG, "Requesting permissions for extra permissions " + mGoogleAccount.getDisplayName());
                        GoogleSignIn.requestPermissions(RoomActivity.this, REQUEST_OAUTH_REQUEST_CODE, mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE), new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
                        return;
                    }else
                        Log.d(LOG_TAG, "google sign ok");
                }else {
                    Log.e(LOG_TAG, "Null result from REQUEST_OAUTH - drive api scope");
                    finish();
                }
            }else{
                Intent intent = new Intent();
                intent.setAction(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // app settings
                Uri uri = Uri.fromParts("package",
                        BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
               finish();
            }

        if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE)
            if (resultCode == RESULT_OK) {
                Word word = new Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY));
                mWordViewModel.insertWord(word);
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.empty_not_saved,
                        Toast.LENGTH_LONG).show();
            }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                bindLocationListener();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(RoomActivity.this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            REQUEST_SENSOR_PERMISSION_CODE);
                } else {
                    bindSensorListener(1);
                }
            } else {
                Toast.makeText(this, "This sample requires location access", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_SENSOR_PERMISSION_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindSensorListener(1);
            } else {
                Toast.makeText(getApplicationContext(), "Sensor access ensure correct measurement of exercise parameters", Toast.LENGTH_LONG).show();
                UserPreferences.setConfirmUseSensors(getApplicationContext(), false);
            }
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            Log.d(LOG_TAG, "onEnterAmbient() " + ambientDetails);

        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();

            Log.d(LOG_TAG, "onExitAmbient()");


        }
    }

    private class RoomCustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
        private static final float MAX_ICON_PROGRESS = 0.65f;
        private float mProgressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);

            child.setScaleX(1 - mProgressToCenter);
            child.setScaleY(1 - mProgressToCenter);
        }

    }

    private void bindLocationListener(){
        if (mLocationListener == null){
            mLocationListener = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult.getLastLocation() != null) {
                        Location loc = locationResult.getLastLocation();
                        Log.d(LOG_TAG, "loc lat " + loc.getLatitude() + " lng " + loc.getLongitude());
                        onNewLocation(loc);
                    }
                }
            };
        }
        BoundFusedLocationClient.bindFusedLocationListenerIn(this, mLocationListener,getApplicationContext());
    }
    private void bindFitSensorListener(){
        if (mDataPointListener == null)
            mDataPointListener = new xFITSensorListener();
        BoundFitnessSensorManager.bindFitnessSensorListenerIn(this, mDataPointListener, getApplicationContext());
    }
    private void bindSensorListener(int iActiveType){
        if (mSensorListener == null){
            mSensorListener = new xSensorListener(iActiveType);
        }
        BoundSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext());
    }

    public class xSensorListener implements SensorEventListener {
        int ActiveType = 0;
        private final Processor peakDetProcessor = new PeakDetProcessor(Constants.DELTA);
        public xSensorListener(int iActiveType){
            ActiveType = iActiveType;
        }

        public int getActiveType(){ return ActiveType;}
        public void setActiveType(int type){ ActiveType = type; return; }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            String sName = event.sensor.getName();
            Float val;
            String sValue;
            Integer iNowDOY; Integer iLastDOY;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.US);

            switch (iType) {
                case Sensor.TYPE_ACCELEROMETER:
                    Log.d(LOG_TAG, "sensor accel " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition) {
                       // vibrate(3);
                        sValue = getString(R.string.label_rep) + getString(R.string.my_space_string);
                        mMessagesViewModel.addOtherMessage(sValue);
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    Log.d(LOG_TAG, "sensor linear accel " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition2 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition2) {
                        // vibrate(3);
                        sValue = getString(R.string.label_rep) + getString(R.string.my_space_string);
                        mMessagesViewModel.addOtherMessage(sValue);
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    Log.d(LOG_TAG, "sensor gyro " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition3 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition3) {
                        // vibrate(3);
                        sValue = getString(R.string.label_rep) + getString(R.string.my_space_string);
                        mMessagesViewModel.addOtherMessage(sValue);
                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    val = event.values[0];
                    sValue = Integer.toString(Math.round(val));
                    Log.d(LOG_TAG, "sensor step " + event.timestamp + " " + sValue);
                    sValue = getString(R.string.label_steps) + getString(R.string.my_space_string) + sValue;
                    mMessagesViewModel.addStepsMsg(sValue);
                    break;
                case Sensor.TYPE_HEART_RATE:
                    val = event.values[0];
                    sValue = Integer.toString(Math.round(val));
                    sValue = getString(R.string.label_bpm) + getString(R.string.my_space_string) + sValue;
                    mMessagesViewModel.addBpmMsg(sValue);
                    Log.d(LOG_TAG, "sensor bpm " + event.timestamp + " " + sValue);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(LOG_TAG, "sensor changed accuracy " + sensor.getName());
        }
    }

    public class xFITSensorListener implements OnDataPointListener{
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            String sSuggested;

            if (DataType.TYPE_HEART_RATE_BPM.equals(dataPoint.getDataType())) {
                sSuggested =  getString(R.string.label_bpm) + getString(R.string.my_space_string) + dataPoint.getValue(Field.FIELD_BPM).asFloat();
                mMessagesViewModel.addBpmMsg(sSuggested);
            }else
            if (DataType.TYPE_ACTIVITY_SAMPLES.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_CONFIDENCE).asFloat() + "% " + mReferenceTools.getFitnessActivityTextById(dataPoint.getValue(Field.FIELD_ACTIVITY).asInt());
                Log.v(LOG_TAG, "sampled " + sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_MOVE_MINUTES.equals(dataPoint.getDataType())) {
                long moveDuration = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (moveDuration > 0) {
                    sSuggested = Long.toString(TimeUnit.MILLISECONDS.toMinutes(moveDuration));
                    Log.v(LOG_TAG, "move mins " + sSuggested);
                    mMessagesViewModel.addOtherMessage(sSuggested);
                }
            }else
            if (DataType.TYPE_WORKOUT_EXERCISE.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_EXERCISE).asString() + " ";
                Log.v(LOG_TAG, sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataPoint.getDataType())) {
                sSuggested = getString(R.string.label_steps) + getString(R.string.my_space_string) + dataPoint.getValue(Field.FIELD_STEPS).asInt();
                Log.v(LOG_TAG, sSuggested);
                mMessagesViewModel.addStepsMsg(sSuggested);
            }else
            if (DataType.TYPE_POWER_SAMPLE.equals(dataPoint.getDataType())) {
                sSuggested = "watts " + dataPoint.getValue(Field.FIELD_WATTS).asFloat() + " ";
                Log.v(LOG_TAG, sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_LOCATION_SAMPLE.equals(dataPoint.getDataType())) {
                Location loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(dataPoint.getValue(Field.FIELD_LATITUDE).asFloat());
                loc.setLongitude(dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
                onNewLocation(loc);
                sSuggested = "location lat: " + dataPoint.getValue(Field.FIELD_LATITUDE).asFloat() + " long: " + dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
                Log.v(LOG_TAG, sSuggested);

            }else{
                String sDataType = dataPoint.getDataType().getName();
                sSuggested = sDataType + ": " + dataPoint.toString();
                Log.i(LOG_TAG, "other data point received: " + sSuggested);
            }
            if (mMessagesViewModel.getMessageCount() > 0) mMessagesViewModel.getNextMessage();
        }
    }

    private void doHistoryWork(){
        Data.Builder builder = new Data.Builder();

            builder.putString(KEY_IMAGE_URI, "ok");
            Data inputData = builder.build();
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest oneTimeWorkRequest =
                    new OneTimeWorkRequest.Builder(HistoryWorker.class)
                            .setInputData(inputData)
                            .setConstraints(constraints)
                            .build();
            mWorkManager.enqueue(oneTimeWorkRequest);
            mWorkManager.beginWith(oneTimeWorkRequest).getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
                @Override
                public void onChanged(List<WorkInfo> workInfos) {
                    if (workInfos.size() > 0) {
                        WorkInfo workInfo = workInfos.get(0);
                        boolean finished = workInfo.getState().isFinished();
                        if (!finished) {
                            Log.d(LOG_TAG, "changed not finished");
                            //showWorkInProgress();
                        } else {
                            Log.d(LOG_TAG, "changed finished");
                            // showWorkFinished();
                            Data outputData = workInfo.getOutputData();

                            String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);

                            // If there is an output file show "See File" button
                            if (!TextUtils.isEmpty(outputImageUri))
                                Log.d(LOG_TAG, outputImageUri.toLowerCase());

                        }
                    }
                }
            });

        }
    private void doRecordingWork(int action){
        Data.Builder builder = new Data.Builder();

        builder.putInt("record_action", action);
        Data inputData = builder.build();
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(FitnessRecordingWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
        mWorkManager.enqueue(oneTimeWorkRequest);
        mWorkManager.beginWith(oneTimeWorkRequest).getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos.size() > 0) {
                    WorkInfo workInfo = workInfos.get(0);
                    boolean finished = workInfo.getState().isFinished();
                    if (!finished) {
                        Log.d(LOG_TAG, "changed not finished");
                        //showWorkInProgress();
                    } else {
                        Log.d(LOG_TAG, "changed finished");
                        // showWorkFinished();
/*                        Data outputData = workInfo.getOutputData();

                        String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);

                        // If there is an output file show "See File" button
                        if (!TextUtils.isEmpty(outputImageUri))
                            Log.d(LOG_TAG, outputImageUri.toLowerCase());*/

                    }
                }
            }
        });

    }
    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
    }

    /** Launches the Google SignIn activity to request OAuth permission for the user. */
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
                RoomActivity.this,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions);
    }

    /** Gets {@link FitnessOptions} in order to check or request OAuth permission for the user. */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_POWER_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
    }

    private void onNewLocation(Location location) {
        boolean newLocation;
        if (mLocation != null){
            newLocation = (mLocation.getLatitude() != location.getLatitude())
                    && (mLocation.getLongitude() != location.getLongitude());
            if (mMessagesViewModel.getLocationMsg().getValue() != null){
                newLocation = (mMessagesViewModel.getLocationMsg().getValue().length() == 0);
            }else
                newLocation = true;
        }else
            newLocation = true;

        if (newLocation) {
            try {
                mLocation = new Location(location);

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                List<Address> addresses = null;
                addresses = mGeoCoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getAddressLine(0) + "";
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                Log.i(LOG_TAG, "New GEO location: " + cityName);
                mMessagesViewModel.addLocationMsg(cityName);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }
}
