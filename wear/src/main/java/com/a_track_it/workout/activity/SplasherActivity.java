package com.a_track_it.workout.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Configuration;
import com.a_track_it.workout.common.data_model.HeightBodypartWorker;
import com.a_track_it.workout.common.data_model.LicenceJobIntentService;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.a_track_it.workout.fragment.CustomConfirmDialog;
import com.a_track_it.workout.fragment.ICustomConfirmDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.workout.common.Constants.ACTIVE_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.ATRACKIT_ATRACKIT_CLASS;
import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.GOALS_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.INTENT_PERMISSION_AGE;
import static com.a_track_it.workout.common.Constants.INTENT_PERMISSION_LOCATION;
import static com.a_track_it.workout.common.Constants.INTENT_PERMISSION_POLICY;
import static com.a_track_it.workout.common.Constants.INTENT_PERMISSION_SENSOR;
import static com.a_track_it.workout.common.Constants.INTENT_SETUP;
import static com.a_track_it.workout.common.Constants.KEY_COMM_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TIME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.KEY_PAYLOAD;
import static com.a_track_it.workout.common.Constants.MAINTAIN_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.QUESTION_AUDIO;
import static com.a_track_it.workout.common.Constants.QUESTION_DEVICE;
import static com.a_track_it.workout.common.Constants.QUESTION_NOTIFY;
import static com.a_track_it.workout.common.Constants.QUESTION_VIBRATE;
import static com.a_track_it.workout.common.Constants.SUMMARY_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.USER_PREF_RECOG;
import static com.a_track_it.workout.common.Constants.WORKOUT_INVALID;
import static com.a_track_it.workout.common.Constants.WORKOUT_TYPE_STRENGTH;

public class SplasherActivity extends AppCompatActivity implements ICustomConfirmDialog {
    private static final String LOG_TAG = SplasherActivity.class.getSimpleName();
    public static final String ARG_ACTION = "SPLASH_ACTION";
    public static final String ARG_RETURN_RESULT = "RETURN_RESULT";
    private static final int LIC_CHECK_MIN_DAYS = 7;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOG = 5008;
    private static final int PERMISSION_REQUEST_BODY_SENSORS = 5009;
    private static final int PERMISSION_REQUEST_LOCATION = 5006;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 5012;
    private static final int REQUEST_SIGN_IN = 5002;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 2;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ActivityResultLauncher<Intent> signInActivityResultLauncher;
    private ActivityResultLauncher<String> storagePermissionResultLauncher;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    TextView mStatusText;
    ProgressBar mProgressBar;
    private Handler mHandler;
    private WorkoutViewModel mSessionViewModel;
    private String mIntentAction;
    private Integer returnResultFlag;
    private int positiveFlag = 0;
    private SavedStateViewModel mSavedStateViewModel;
    private GoogleSignInAccount mGoogleAccount;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
   // private Set<Node> mPhoneNodesWithApp;
    private String phoneNodeId = null;
    private AlertDialog alertDialog;
    private String sUserID;
    private boolean authInProgress;
    private int whichButton;
    private CustomConfirmDialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnResultFlag = 0;
        mHandler = new Handler(getMainLooper());
        // Remove title bar
        Context context = getApplicationContext();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        appPrefs = ApplicationPreferences.getPreferences(context);
        setContentView(R.layout.activity_splash);
        mStatusText = findViewById(R.id.splash_textview);
        mProgressBar = findViewById(R.id.splash_progress_bar);
        long lastLic = appPrefs.getLastLicCheck();
        long timeMs = System.currentTimeMillis();
        try {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_ID, SplasherActivity.class.getSimpleName());
            if (mFirebaseAnalytics != null) mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }catch(Exception e){
            Log.e(LOG_TAG,"initialise analytics failed " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        Intent arrivingIntent = getIntent();
        if (arrivingIntent != null) {
            mIntentAction = arrivingIntent.hasExtra(ARG_ACTION) ?  arrivingIntent.getStringExtra(ARG_ACTION) : arrivingIntent.getAction();
            sUserID = arrivingIntent.hasExtra(KEY_FIT_USER) ?  arrivingIntent.getStringExtra(KEY_FIT_USER) : ATRACKIT_EMPTY;
            returnResultFlag = arrivingIntent.hasExtra(ARG_RETURN_RESULT) ? arrivingIntent.getIntExtra(ARG_RETURN_RESULT, 0) : 0;
        }else {
            if ((savedInstanceState != null) &&  savedInstanceState.containsKey(ARG_ACTION)){
                mIntentAction = savedInstanceState.getString(ARG_ACTION);
                sUserID = savedInstanceState.getString(KEY_FIT_USER);
                returnResultFlag = savedInstanceState.getInt(ARG_RETURN_RESULT);
            }else {
                mIntentAction = Intent.ACTION_MAIN;
                sUserID = ATRACKIT_EMPTY;
                arrivingIntent = new Intent(mIntentAction);
            }
        }
        if (sUserID.length() > 0) userPrefs = UserPreferences.getPreferences(context,sUserID);
        boolean bInitialLoad = (userPrefs == null || userPrefs.getLastUserSignIn() == 0);
        if (!bInitialLoad) bInitialLoad = (mIntentAction.equals(Constants.INTENT_PERMISSION_POLICY) || mIntentAction.equals(ATRACKIT_ATRACKIT_CLASS));
        //  ONLY IN PRODUCTION   doLicenceLookupJob(getIntent());
        mSavedStateViewModel = new ViewModelProvider(SplasherActivity.this).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(context.getApplicationContext());
        mSessionViewModel = new ViewModelProvider(SplasherActivity.this, factory).get(WorkoutViewModel.class);
        signInActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    authInProgress = false;
                    if (resultCode == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            try {
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                                mGoogleAccount = task.getResult(ApiException.class);
                                sUserID = mGoogleAccount.getId();
                                appPrefs.setLastUserID(sUserID);
                                appPrefs.setLastUserLogIn(System.currentTimeMillis());
                                if (!mIntentAction.equals(ATRACKIT_ATRACKIT_CLASS)) mIntentAction = ATRACKIT_ATRACKIT_CLASS;
                                positiveFlag = 1;
                                userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                                final String idToken = mGoogleAccount.getIdToken();
                                long lastAppLogin = appPrefs.getLastUserLogIn();
                                long lastUserLogin = userPrefs.getLastUserSignIn();
                                appPrefs.setLastUserLogIn(timeMs);
                                appPrefs.setLastUserID(sUserID);
                                userPrefs.setUserEmail(mGoogleAccount.getEmail());
                                userPrefs.setLastUserName(mGoogleAccount.getDisplayName());
                                userPrefs.setLastUserSignIn(timeMs);
                                int configCount = mSessionViewModel.getConfigCount(sUserID);
                                if (lastUserLogin == 0 || configCount == 0){
                                    createNotificationChannels(); // setup notification manager and channels if needed
                                    if (configCount == 0) doSetupUserConfig(sUserID);
                                    appPrefs.setLastSyncInterval(TimeUnit.MINUTES.toMillis(15));
                                    appPrefs.setNetworkCheckInterval(TimeUnit.MINUTES.toMillis(1));
                                    appPrefs.setPhoneSyncInterval(TimeUnit.MINUTES.toMillis(10));
                                    appPrefs.setUseLocation(false);
                                    appPrefs.setUseSensors(false);
                                    userPrefs.setUseFirebase(true);
                                    userPrefs.setRestAutoStart(true);
                                    userPrefs.setTimedRest(false);
                                    appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(20));
                                    appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, false);
                                    appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, false);
                                    appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, false);
                                    userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_START,0);
                                    userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_END,0);
                                    userPrefs.setUseKG(!(Locale.getDefault().equals(Locale.US)));
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
                                    appPrefs.setPrefByLabel(Constants.LABEL_USE_GRID, true);
                                    String sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_GYM);
                                    userPrefs.setPrefStringByLabel(sLabel, Long.toString(WORKOUT_TYPE_STRENGTH));  // default to strength training
                                    userPrefs.setConfirmExitApp(true);
                                    userPrefs.setConfirmStartSession(true);
                                    userPrefs.setConfirmEndSession(true);
                                    userPrefs.setConfirmSetSession(true);
                                    userPrefs.setConfirmDeleteSession(true);
                                    userPrefs.setLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION,90);
                                    userPrefs.setLongPrefByLabel(Constants.USER_PREF_GYM_DURATION,90);
                                    userPrefs.setLongPrefByLabel(Constants.USER_PREF_REST_DURATION,270);
                                    userPrefs.setConfirmDuration(3000);
                                    userPrefs.setDefaultNewReps(10);
                                    userPrefs.setDefaultNewSets(3);
                                    userPrefs.setStepsSampleRate(120L);
                                    userPrefs.setBPMSampleRate(60L);
                                    userPrefs.setOthersSampleRate(300L);
                                    mSavedStateViewModel.setUserPreferences(userPrefs);
                                }
                                if ((idToken != null) && (idToken.length() > 0)) {
                                    if ((appPrefs.getLastUserLogIn() == 0) || appPrefs.getFirebaseAvail()) {
                                        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                        mFirebaseAuth.signInWithCredential(credential)
                                                .addOnCompleteListener(this, task1 -> {
                                                    if (!task1.isSuccessful()) {
                                                        Log.w(LOG_TAG, "signInWithCredential", task1.getException());
                                                        appPrefs.setFirebaseAvail(false);
                                                    } else {
                                                        mFirebaseUser = task1.getResult().getUser();
                                                        appPrefs.setFirebaseAvail(true);
                                                        appPrefs.setPrefByLabel("UseFirebase", true);
                                                        Toast.makeText(context, mFirebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
                                                    }
                                                    checkFinished();
                                                });
                                    }else
                                        checkFinished();
                                }
                                else {
                                    Log.e(LOG_TAG, " idToken on login " + mGoogleAccount.getDisplayName() + " " + idToken.length());
                                    checkFinished();
                                }
                            }
                            catch (ApiException e) {
                                signInError(e);
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                        else
                            doStartUp(getReturnIntent());
                    }
                    else {
                        doStartUp(getReturnIntent());
                    }
                });
        storagePermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result) {
                            Log.e(LOG_TAG, "storagePermissionResultLauncher: PERMISSION GRANTED");
                            // Permission has been granted.
                            mIntentAction = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                            positiveFlag = 1;
                            doStartUp(getReturnIntent());
                        }
                        else {
                            Log.e(LOG_TAG, "storagePermissionResultLauncher: PERMISSION DENIED");
                            mIntentAction = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                            positiveFlag = 0;
                            doStartUp(getReturnIntent());
                        }
                    }
                });
        if (bInitialLoad || (sUserID.length() == 0) || (TimeUnit.MILLISECONDS.toDays(timeMs-lastLic) >= LIC_CHECK_MIN_DAYS)){
            if (!isNetworkConnected(context) && (lastLic == 0)) {
                doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null);
                return;
            }
            if (bInitialLoad) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int availCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
                if (availCode != ConnectionResult.SUCCESS){
                    if (googleApiAvailability.isUserResolvableError(availCode)){
                        googleApiAvailability.getErrorDialog(SplasherActivity.this, availCode, REQUEST_GOOGLE_PLAY_SERVICES, dialog -> finish());
                    }else{
                        toastResult("Google Api needs updating check Google Play");
                        mHandler.postDelayed(() -> finish(), 3000);
                    }
                }
                if (!mIntentAction.equals(ATRACKIT_ATRACKIT_CLASS))
                    doConfirmDialog(Constants.QUESTION_POLICY, getString(R.string.policy_text), null);
                else
                    signIn();
                return;
            }else {
                if (!appPrefs.getAppSetupCompleted()) {
                    mIntentAction = Constants.INTENT_SETUP;
                }
                appPrefs.setLastLicCheck(System.currentTimeMillis());  //TODO: implement proper licence checking - set this after completion
            }
        }
        if (sUserID.length() > 0) userPrefs = UserPreferences.getPreferences(context,sUserID);
        if (mSavedStateViewModel.getIsInProgress() != 0)
            Log.e(LOG_TAG, "already busy");
        else
            if (mIntentAction.equals(Intent.ACTION_MAIN) || ((arrivingIntent != null) && !arrivingIntent.hasExtra(ARG_ACTION)))
                doStartUp(arrivingIntent);
            else
                doUserConfig(mIntentAction);  // permissions and questions


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ARG_ACTION, mIntentAction);
        outState.putString(KEY_FIT_USER, sUserID);
        outState.putInt(ARG_RETURN_RESULT, returnResultFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Context context = getApplicationContext();
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    appPrefs.setUseLocation(true);
                } else {
                    toastResult(getString(R.string.action_no_location));
                    appPrefs.setUseLocation(false);
                }
            }else{
                toastResult(getString(R.string.action_no_location));
                appPrefs.setUseLocation(false);
            }
            displayResult("Sensors");
            doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason),null);
        }
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_SENSORS_ASKED, true);
            String sMsg = getResources().getString(R.string.sensors_permission_granted);
            if ((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (userPrefs != null) userPrefs.setReadSensorsPermissions(true);
                SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                // Permission has been granted.
                List<Sensor> bpmSensorList = mSensorManager.getSensorList(Sensor.TYPE_HEART_RATE);
                String sLabelName = getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "bpm";
                String sLabel = getString(R.string.label_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                boolean bAvail = ((bpmSensorList != null) && (bpmSensorList.size() > 0));
                int wakefulIndex = 0;
                if (bAvail)
                    for(int i=0; i< bpmSensorList.size(); i++){
                        if (!bpmSensorList.get(i).isWakeUpSensor()){
                            wakefulIndex = i;
                            break;
                        }
                    }
                appPrefs.setPrefByLabel(sLabel, bAvail);
                if (bAvail) appPrefs.setPrefStringByLabel(sLabelName, bpmSensorList.get(wakefulIndex).getName());
                if (bAvail) appPrefs.setBPMSensorCount(bpmSensorList.size());
                List<Sensor> stepSensorList = mSensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER);
                bAvail = ((stepSensorList != null) && (stepSensorList.size() > 0));
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
                sLabelName = getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "step";
                appPrefs.setPrefByLabel(sLabel, bAvail);
                wakefulIndex = 0;
                if (bAvail)
                    for(int i=0; i< stepSensorList.size(); i++){
                        if (!stepSensorList.get(i).isWakeUpSensor()){
                            wakefulIndex = i;
                            break;
                        }
                    }
                if (bAvail) appPrefs.setPrefStringByLabel(sLabelName, stepSensorList.get(wakefulIndex).getName());
                if (bAvail) appPrefs.setStepsSensorCount(stepSensorList.size());

                List<Sensor> pressureSensorList = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
                bAvail = ((pressureSensorList != null) && (pressureSensorList.size() > 0));
                sLabel = getString(R.string.label_sensor) + "pressure";
                sLabelName = getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "hPa";
                appPrefs.setPrefByLabel(sLabel, bAvail);
                wakefulIndex = 0;
                if (bAvail)
                    for(int i=0; i< pressureSensorList.size(); i++){
                        if (!pressureSensorList.get(i).isWakeUpSensor()){
                            wakefulIndex = i;
                            break;
                        }
                    }
                if (bAvail) appPrefs.setPrefStringByLabel(sLabelName, pressureSensorList.get(wakefulIndex).getName());
                if (bAvail) appPrefs.setPressureSensorCount(pressureSensorList.size());

                List<Sensor> tempSensorList = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
                bAvail = ((tempSensorList != null) && (tempSensorList.size() > 0));
                sLabel = getString(R.string.label_sensor) + "temp";
                sLabelName = getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "temp";
                appPrefs.setPrefByLabel(sLabel, bAvail);
                wakefulIndex = 0;
                if (bAvail)
                    for(int i=0; i< tempSensorList.size(); i++){
                        if (!tempSensorList.get(i).isWakeUpSensor()){
                            wakefulIndex = i;
                            break;
                        }
                    }
                if (bAvail) appPrefs.setPrefStringByLabel(sLabelName, tempSensorList.get(wakefulIndex).getName());
                if (bAvail) appPrefs.setTempSensorCount(tempSensorList.size());                
                List<Sensor> humiditySensorList = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
                bAvail = ((humiditySensorList != null) && (humiditySensorList.size() > 0));
                sLabel = getString(R.string.label_sensor) + "humidity";
                sLabelName = getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "humidity";
                appPrefs.setPrefByLabel(sLabel, bAvail);
                wakefulIndex = 0;
                if (bAvail)
                    for(int i=0; i< humiditySensorList.size(); i++){
                        if (!humiditySensorList.get(i).isWakeUpSensor()){
                            wakefulIndex = i;
                            break;
                        }
                    }

                if (bAvail) appPrefs.setPrefStringByLabel(sLabelName, humiditySensorList.get(wakefulIndex).getName());
                if (bAvail) appPrefs.setHumiditySensorCount(humiditySensorList.size());
                
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_LOCATION_SAMPLE.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_ACTIVITY_SEGMENT.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_HEART_POINTS.getName();
                appPrefs.setPrefByLabel(sLabel, true);
                appPrefs.setUseSensors(true);
            }
            else {
                sMsg = getString(R.string.no_permission_okay);
                appPrefs.setUseSensors(false);
                if (userPrefs != null) userPrefs.setReadSensorsPermissions(false);
            }
            toastResult(sMsg);
//            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
//                    != PackageManager.PERMISSION_GRANTED) {
            displayResult("Activity");
            doConfirmDialog(Constants.QUESTION_ACT_RECOG, getString(R.string.recog_permission_reason), null);
//            }else {
//                if (userPrefs != null) userPrefs.setReadSensorsPermissions(true);
//                checkFinished();
//            }
            return;
        }
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOG){
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_RECOG_ASKED, true);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, true);
                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED)) {
                    checkFinished();
                } else {
                    displayResult("Sensors");
                    doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason), null);
                }
            }
            else {
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, false);
                String sMsg = getString(R.string.no_permission_okay);
                toastResult(sMsg);
                checkFinished();
            }
        }
        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE){
            mIntentAction = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            positiveFlag = 1;
            doStartUp(getReturnIntent());
        }
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        positiveFlag = button;
        mSavedStateViewModel.setIsInProgress(0);
        if (userPrefs == null && (sUserID != null && sUserID.length() > 0)) userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
        if (question == Constants.QUESTION_NOTIFY){
            appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, true);
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, (button > 0));
            mIntentAction = Constants.USER_PREF_USE_NOTIFICATION;
            doStartUp(getReturnIntent());
            return;
        }
        if (question == Constants.QUESTION_AUDIO){
            appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, true);
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_USE_AUDIO, (button > 0));
            mIntentAction = Constants.USER_PREF_USE_AUDIO;
            doStartUp(getReturnIntent());
            return;
        }
        if (question == Constants.QUESTION_VIBRATE){
            appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, true);
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_USE_VIBRATE, (button > 0));
            mIntentAction = Constants.USER_PREF_USE_VIBRATE;
            doStartUp(getReturnIntent());
            return;
        }
        if (question == Constants.QUESTION_POLICY){
            if (button > 0) {
                if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
                long timeMs = System.currentTimeMillis();
                appPrefs.setLastLicCheck(timeMs);
                if ((sUserID.length() == 0) || mIntentAction.equals(INTENT_PERMISSION_POLICY)
                        || mIntentAction.equals(ATRACKIT_ATRACKIT_CLASS))
                    signIn();
                else
                    doUserConfig(mIntentAction);
            }else{
                displayResult("Exiting...");
                mHandler.postDelayed(() -> {
                    positiveFlag=0;
                    if (returnResultFlag > 0) setResult(RESULT_CANCELED);
                    finish();
                }, 3000);
            }
        }
        if (question == Constants.QUESTION_NETWORK){
            displayResult("Exiting...");
            mHandler.postDelayed(() -> {
                if (returnResultFlag > 0) setResult(RESULT_CANCELED);
                finish();
            }, 3000);
        }
        if (question == Constants.QUESTION_STORAGE) {
            if (button > 0)
                doGetExternalStoragePermission();
            else {
                mIntentAction = Constants.INTENT_PERMISSION_SENSOR;
                positiveFlag = -1;
                doStartUp(getReturnIntent());
            }
        }
        if (question == Constants.QUESTION_LOCATION) {
            if (button > 0)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            else {
                appPrefs.setUseLocation(false);
                mIntentAction = Constants.INTENT_PERMISSION_SENSOR;
                displayResult("Sensors...");
                doUserConfig(mIntentAction);
            }
        }
        String sMsg = getString(R.string.sensors_permission_granted);
        if (question == Constants.QUESTION_SENSORS) {
            if (button > 0) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this,Manifest.permission.BODY_SENSORS))
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            PERMISSION_REQUEST_BODY_SENSORS);
                else {
                    // Permission has been granted.
                    String sLabel = getString(R.string.label_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_LOCATION_SAMPLE.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_ACTIVITY_SEGMENT.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    sLabel = getString(R.string.label_sensor) + DataType.TYPE_HEART_POINTS.getName();
                    appPrefs.setPrefByLabel(sLabel, true);
                    appPrefs.setUseSensors(true);
                }
            }else{
                sMsg = getString(R.string.no_permission_okay);
                appPrefs.setUseSensors(false);
            }
            toastResult(sMsg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    displayResult("Activity");
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG, getString(R.string.recog_permission_reason), null);
                }else{
                    if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG,true);
                    checkFinished();
                }
            }else {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Constants.ATRACKIT_RECOG28_CLASS) != PackageManager.PERMISSION_GRANTED) {
                    displayResult("Activity");
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG, getString(R.string.recog_permission_reason), null);
                 }else{
                    if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG,true);
                    checkFinished();
                }
            }
        }
        if (question == Constants.QUESTION_ACT_RECOG) {
            if (button > 0)
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION))
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_ACTIVITY_RECOG);
                    else
                    if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, true);
                }else {
                    if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, true);
                    ActivityCompat.requestPermissions(this,
                            new String[]{Constants.ATRACKIT_RECOG28_CLASS},
                            PERMISSION_REQUEST_ACTIVITY_RECOG);
                }
            else {
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, false);
                checkFinished();
            }
        }
        if (question == QUESTION_DEVICE){
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_ASKED, true);
            if (button > 0) {
                new SetupPhoneNodeId().run();
            }else {
                positiveFlag = -1;
                if (userPrefs != null) userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                mIntentAction = Constants.INTENT_PERMISSION_DEVICE;
                doStartUp(getReturnIntent());
            }
        }
        if (question == Constants.QUESTION_AGE) {
            if (button > 0) {
                if (sUserID == null || sUserID.length() == 0) sUserID = appPrefs.getLastUserID();
                if (sUserID != null && sUserID.length() > 0) {
                    UserPreferences userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                    if (userPrefs != null) userPrefs.setAskAge(true);
                }
                String sAge = userPrefs.getPrefStringByLabel(INTENT_PERMISSION_AGE);
                if ((sAge.length() > 0) && TextUtils.isDigitsOnly(sAge)){
                    appPrefs.setUseLocation((Integer.parseInt(sAge) > 9));
                }
            }else {
                appPrefs.setUseLocation(false);
            }
            checkFinished();
        }
        if (question == Constants.QUESTION_HEIGHT){
            if (button > 0) {
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.KEY_FIT_USER, sUserID); // userID
                Constraints constraints = new Constraints.Builder().build();
                OneTimeWorkRequest oneTimeWorkRequest =
                        new OneTimeWorkRequest.Builder(HeightBodypartWorker.class)
                                .setInputData(builder.build())
                                .setConstraints(constraints)
                                .build();
                WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(oneTimeWorkRequest);
            }
            checkFinished();
        }
    }

    @Override
    public void onCustomConfirmDetach() {
        mSavedStateViewModel.setIsInProgress(0);
    }
    private void signIn(){
        if (!authInProgress) {
            displayResult("Sign in...");
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail().requestId()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SplasherActivity.this, gso);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                authInProgress = true;
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeCustomAnimation(SplasherActivity.this, R.anim.jump_in, R.anim.jump_out);
                ActivityCompat.startActivityForResult(SplasherActivity.this,signInIntent, REQUEST_SIGN_IN, options.toBundle());
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, "signIn error " + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GOOGLE_PLAY_SERVICES){
            finish();
        }
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode != RESULT_OK)
                finish();
            else {
                try {
                    authInProgress = false;
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    mGoogleAccount = task.getResult(ApiException.class);
                    if (mGoogleAccount != null) {
                        sUserID = mGoogleAccount.getId();
                        appPrefs.setLastUserID(sUserID);
                        positiveFlag = 1;
                        userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                        long lastAppLogin = appPrefs.getLastUserLogIn();
                        long lastUserLogin = userPrefs.getLastUserSignIn();
                        final long timeMs = System.currentTimeMillis();
                        appPrefs.setLastUserLogIn(timeMs);
                        appPrefs.setLastUserID(sUserID);
                        userPrefs.setUserEmail(mGoogleAccount.getEmail());
                        userPrefs.setLastUserName(mGoogleAccount.getDisplayName());
                        userPrefs.setLastUserSignIn(timeMs);
                        int configCount = mSessionViewModel.getConfigCount(sUserID);
                        if (configCount == 0){
                            createNotificationChannels(); // setup notification manager and channels if needed
                            doSetupUserConfig(sUserID);
                            appPrefs.setLastSyncInterval(TimeUnit.MINUTES.toMillis(15));
                            appPrefs.setNetworkCheckInterval(TimeUnit.MINUTES.toMillis(1));
                            appPrefs.setPhoneSyncInterval(TimeUnit.MINUTES.toMillis(10));
                            appPrefs.setUseLocation(false);
                            appPrefs.setUseSensors(false);
                            userPrefs.setUseFirebase(true);
                            userPrefs.setRestAutoStart(true);
                            userPrefs.setTimedRest(false);
                            appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(20));
                            appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, false);
                            appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, false);
                            appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, false);
                            userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_START,0);
                            userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_END,0);
                            userPrefs.setUseKG(!(Locale.getDefault().equals(Locale.US)));
                            userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
                            appPrefs.setPrefByLabel(Constants.LABEL_USE_GRID, true);
                            String sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_GYM);
                            userPrefs.setPrefStringByLabel(sLabel, Long.toString(WORKOUT_TYPE_STRENGTH));  // default to strength training
                            userPrefs.setConfirmExitApp(true);
                            userPrefs.setConfirmStartSession(true);
                            userPrefs.setConfirmEndSession(true);
                            userPrefs.setConfirmSetSession(true);
                            userPrefs.setConfirmDeleteSession(true);
                            userPrefs.setLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION,90);
                            userPrefs.setLongPrefByLabel(Constants.USER_PREF_GYM_DURATION,90);
                            userPrefs.setLongPrefByLabel(Constants.USER_PREF_REST_DURATION,270);
                            userPrefs.setConfirmDuration(3000);
                            userPrefs.setDefaultNewReps(10);
                            userPrefs.setDefaultNewSets(3);
                            userPrefs.setStepsSampleRate(120L);
                            userPrefs.setBPMSampleRate(60L);
                            userPrefs.setOthersSampleRate(300L);
                            mSavedStateViewModel.setUserPreferences(userPrefs);
                        }
                        final String idToken = mGoogleAccount.getIdToken();
                        if ((idToken != null) && (idToken.length() > 0)) {
                            if ((appPrefs.getLastUserLogIn() == 0 || !appPrefs.getAppSetupCompleted()) || appPrefs.getFirebaseAvail()) { // first login or available
                                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                mFirebaseAuth.signInWithCredential(credential)
                                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(LOG_TAG, "signInWithCredential", task.getException());
                                                    //broadcastToast("Authentication failed.");
                                                    appPrefs.setFirebaseAvail(false);
                                                } else {
                                                    mFirebaseUser = task.getResult().getUser();
                                                    appPrefs.setFirebaseAvail(true);
                                                    appPrefs.setPrefByLabel("UseFirebase", true);
                                                    if (mFirebaseUser != null) {
                                                        Log.e(LOG_TAG, "Firebase " + mFirebaseUser.getDisplayName());
                                                    }
                                                }
                                                doStartUp(getReturnIntent());
                                            }
                                        });
                            }else
                                checkFinished();
                        } else
                            checkFinished();
                          //  doStartUp(getReturnIntent());
                    } else
                        signIn();
                } catch (ApiException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    signInError(e);
                }
            }
        }
    }

    private void doSetupUserConfig(String sUserID){
        if (sUserID.length() == 0) return;
        String sKey = Constants.MAP_HISTORY_RANGE;
        Configuration configuration = new Configuration(sKey, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, Long.toString(0), Long.toString(0));
        mSessionViewModel.insertConfig(configuration);

        final Device device = Device.getLocalDevice(getApplicationContext());
        Wearable.getNodeClient(this).getLocalNode().addOnSuccessListener(new OnSuccessListener<Node>() {
            @Override
            public void onSuccess(@NonNull Node node) {
                Configuration c = new Configuration(Constants.KEY_DEVICE1, sUserID,node.getId(),System.currentTimeMillis(),device.getModel(),device.getUid());
                mSessionViewModel.insertConfig(c);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Configuration c = new Configuration(Constants.KEY_DEVICE1, sUserID,ATRACKIT_EMPTY,System.currentTimeMillis(),device.getModel(),device.getUid());
                mSessionViewModel.insertConfig(c);
            }
        });

        sKey = Constants.KEY_DEVICE2;
        configuration = new Configuration(sKey, sUserID, ATRACKIT_EMPTY, 0L, ATRACKIT_EMPTY, null);
        mSessionViewModel.insertConfig(configuration);

        sKey = Constants.MAP_CURRENT_STATE;
        configuration = new Configuration(sKey, sUserID, "", (long)WORKOUT_INVALID, ATRACKIT_EMPTY, ATRACKIT_EMPTY);
        mSessionViewModel.insertConfig(configuration);

        sKey = Constants.MAP_CURRENT_USER;
        configuration = new Configuration(sKey, sUserID, "", (long)0, ATRACKIT_EMPTY, ATRACKIT_EMPTY);
        mSessionViewModel.insertConfig(configuration);
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    private void createNotificationChannels() {

        // Create a notification manager object.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelActive = new NotificationChannel
                (ACTIVE_CHANNEL_ID,
                        getString(R.string.notify_channel_activity_name),
                        NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannelActive.enableLights(true);
        notificationChannelActive.setLightColor(Color.GREEN);
        notificationChannelActive.enableVibration(true);
        notificationChannelActive.setDescription
                (getString(R.string.notify_channel_activity_desc));
        notificationChannelActive.setShowBadge(true);
        notificationChannelActive.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(notificationChannelActive);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelSummary = new NotificationChannel
                (SUMMARY_CHANNEL_ID,
                        getString(R.string.notify_channel_summary_name),
                        NotificationManager.IMPORTANCE_LOW);
        notificationChannelSummary.enableLights(true);
        notificationChannelSummary.setLightColor(Color.GREEN);
        notificationChannelSummary.enableVibration(false);
        notificationChannelSummary.setDescription
                (getString(R.string.notify_channel_summary_desc));
        notificationChannelSummary.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        notificationChannelSummary.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelSummary);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelGoals = new NotificationChannel
                (GOALS_CHANNEL_ID,
                        getString(R.string.notify_channel_goals_name),
                        NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannelGoals.enableLights(true);
        notificationChannelGoals.setLightColor(Color.RED);
        notificationChannelGoals.enableVibration(false);
        notificationChannelGoals.setDescription
                (getString(R.string.notify_channel_goals_desc));
        notificationChannelGoals.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelGoals);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelMaintenance = new NotificationChannel
                (MAINTAIN_CHANNEL_ID,
                        getString(R.string.notify_channel_maintain_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannelMaintenance.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannelMaintenance.enableLights(true);
        notificationChannelMaintenance.setLightColor(Color.GREEN);
        notificationChannelMaintenance.enableVibration(true);
        notificationChannelMaintenance.setDescription
                (getString(R.string.notify_channel_maintain_desc));
        notificationChannelMaintenance.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelMaintenance);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelFirebase = new NotificationChannel
                (Constants.FIREBASE_CHANNEL_ID,
                        getString(R.string.notify_channel_firebase_name),
                        NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannelFirebase.enableLights(true);
        notificationChannelFirebase.setLightColor(Color.YELLOW);
        notificationChannelFirebase.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        notificationChannelFirebase.enableVibration(true);
        notificationChannelFirebase.setDescription
                (getString(R.string.notify_channel_firebase_desc));
        notificationChannelFirebase.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelFirebase);
    }

    private void signInError(ApiException e){
        Context context = getApplicationContext();
        if ((e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS) ||
                (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED)) {
            Log.d(LOG_TAG, "signin already inprog or canncelled "  + e.getStatusCode());
            return;
        }

        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
        int statusCode = e.getStatusCode();
        String sMsg = ATRACKIT_EMPTY;
        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
            sMsg = "Sign in is required";
            Toast.makeText(context, sMsg, Toast.LENGTH_LONG).show();
            if (authInProgress) {
                authInProgress = false;
                new Handler(getMainLooper()).postDelayed(() -> signIn(), 2000);
            }
            return;
        }
        authInProgress = false;
        if (statusCode == GoogleSignInStatusCodes.NETWORK_ERROR)
            sMsg = "Check your network - unavailable";
        if (statusCode == GoogleSignInStatusCodes.TIMEOUT)
            sMsg = "Check your network - sign in timed out";
        if (statusCode == GoogleSignInStatusCodes.API_NOT_CONNECTED)
            sMsg = "Check your network - not connected error";
        if (statusCode == GoogleSignInStatusCodes.ERROR)
            sMsg = "Non-specific error occurred - try again";
        if (sMsg.length() > 0) {
            Toast.makeText(context, sMsg, Toast.LENGTH_LONG).show();
            new Handler(getMainLooper()).postDelayed(() -> finish(),5100);
        }else {
            sMsg = String.format(Locale.getDefault(), getString(R.string.common_google_play_services_unknown_issue), getString(R.string.app_name));
            Intent intent = new Intent();
            intent.setAction(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // app settings
            Uri uri = Uri.fromParts("package",
                    Constants.ATRACKIT_ATRACKIT_CLASS, null);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent,null);
            finish();   // YUP - finish - dead dude
        }
    }
    private void doLicenceLookupJob(final Intent intent){
        Context context = getApplicationContext();
        final ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        final boolean bSetup = appPrefs.getAppSetupCompleted();
        if (context != null) {
            displayResult("Verifying...");
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mProgressBar.setIndeterminate(true);
            doLicenceCheckJobIntent();
        }
    }
    private void doLicenceCheckJobIntent(){
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                long timeMs = System.currentTimeMillis();
                mProgressBar.setVisibility(ProgressBar.GONE);
                boolean bSetup = appPrefs.getAppSetupCompleted();
                if (resultCode == 200){
                    appPrefs.setLastLicCheck(timeMs);  // only if successful
                    if (resultData.containsKey(KEY_FIT_VALUE)) {
                        int reason = resultData.getInt(KEY_FIT_VALUE);
                        displayResult("Verified!");
                    }
                    if (!bSetup){
                        doConfirmDialog(Constants.QUESTION_POLICY, getString(R.string.policy_text),null);
                    }else
                        doStartUp(new Intent(Intent.ACTION_MAIN));
                }else {
                    if (resultData.containsKey(KEY_FIT_VALUE)) {
                        int reason = resultData.getInt(KEY_FIT_VALUE);
                        displayResult("FAILED Product Check  " + reason);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
            }
        };

        Context context = getApplicationContext();
        String sUserId = appPrefs.getLastUserID();
        Device device = Device.getLocalDevice(context);
        int initialLoad = (!Utilities.fileExist(context, Constants.BODYPART_FILENAME)) ? 1 : 0;
        long lastLic = appPrefs.getLastLicCheck();

        Intent mIntent = new Intent(context, LicenceJobIntentService.class);
        mIntent.putExtra(Constants.KEY_FIT_REC, resultReceiver);
        mIntent.putExtra(KEY_FIT_USER, sUserId);
        mIntent.putExtra(Constants.ATRACKIT_ATRACKIT_CLASS, device.getUid());
        mIntent.putExtra(KEY_FIT_TYPE, initialLoad);
        mIntent.putExtra("lastLic", lastLic);
        LicenceJobIntentService.enqueueWork(context, mIntent);
    }

    private Intent getReturnIntent(){
        Intent myRoomIntent = new Intent(Intent.ACTION_MAIN);
        myRoomIntent.putExtra(SplasherActivity.ARG_ACTION, mIntentAction); // this is important
        myRoomIntent.putExtra(KEY_FIT_USER, sUserID);
        myRoomIntent.putExtra(KEY_FIT_TYPE, positiveFlag);
        return  myRoomIntent;
    }

    private void doStartUp(Intent intent){
        Context context = getApplicationContext();
        Intent myRoomIntent = new Intent(context, RoomActivity.class);
        myRoomIntent.setAction(intent.getAction());
        myRoomIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        myRoomIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        myRoomIntent.putExtra(KEY_FIT_NAME, SplasherActivity.class.getName()); // this is important
        myRoomIntent.putExtra(KEY_FIT_TYPE, positiveFlag);
        myRoomIntent.putExtra(KEY_FIT_USER, sUserID);
        myRoomIntent.putExtra(SplasherActivity.ARG_ACTION, mIntentAction); // this is important
        if (intent != null)
            myRoomIntent.putExtras(intent);
        if (returnResultFlag > 0)
            setResult(RESULT_OK, myRoomIntent);
        else
            startActivity(myRoomIntent);
        finish();
    }

    private void doUserConfig(String sIntent){
        Context context = getApplicationContext();
        if (sIntent.equals(ATRACKIT_ATRACKIT_CLASS)){
            signIn();
        }else
        if (INTENT_SETUP.equals(sIntent) ||  Constants.INTENT_PERMISSION_LOCATION.equals(sIntent)){
            if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) || ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)) {
                        final String sIntent2 = checkApplicationSetup();
                        if (sIntent2.length() > 0){
                            if (Constants.INTENT_PERMISSION_AGE.equals(sIntent2)) {
                                displayResult("Age");
                                doConfirmDialog(Constants.QUESTION_AGE, getString(R.string.ask_user_age), null);
                            }
                            if (Constants.INTENT_PERMISSION_HEIGHT.equals(sIntent2)) {
                                displayResult("Height");
                                doConfirmDialog(Constants.QUESTION_HEIGHT, getString(R.string.ask_user_height), null);
                            }
                            if (sIntent2.equals(Constants.INTENT_PERMISSION_DEVICE)) {
                                displayResult("Device");
                                doGetUsePhoneApp();

                            }
                            if (sIntent2.equals(Constants.INTENT_CLOUD_SYNC)) {
                                doGetTotalsRefresh();
                            }
                            if (sIntent2.equals(Constants.USER_PREF_USE_VIBRATE)){
                                //    || sIntent.equals(Constants.USER_PREF_USE_AUDIO)
                                //   || sIntent.equals(Constants.USER_PREF_USE_VIBRATE)) {
                                displayResult("Alerts");
                                doConfirmDialog(QUESTION_VIBRATE, getString(R.string.confirm_use_vibrate_prompt),null);
                                // doAlertDialog(sIntent2);
                            }
                            if (sIntent2.equals(Constants.USER_PREF_USE_AUDIO)){
                                displayResult("Alerts");
                                doConfirmDialog(QUESTION_AUDIO, getString(R.string.confirm_use_audio_prompt),null);
                            }
                            if (sIntent2.equals(Constants.USER_PREF_USE_NOTIFICATION)){
                                displayResult("Alerts");
                                doConfirmDialog(QUESTION_NOTIFY, getString(R.string.confirm_use_notifications_prompt),null);
                            }
/*                            if (sIntent2.equals(Constants.USER_PREF_USE_NOTIFICATION)
                                    || sIntent.equals(Constants.USER_PREF_USE_AUDIO)
                                    || sIntent.equals(Constants.USER_PREF_USE_VIBRATE)) {
                                displayResult("Alerts");
                                doAlertDialog(sIntent2);
                            }*/
                            return;
                        }else doStartUp(getReturnIntent());

                    } else{
                        displayResult("Sensors");
                        doConfirmDialog(Constants.QUESTION_SENSORS,getString(R.string.sensor_permission_reason),null);
                    }
                }else{
                    displayResult("Activity");
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG,getString(R.string.recog_permission_reason),null);
                }
            }
            else {
                displayResult("Location");
                doConfirmDialog(Constants.QUESTION_LOCATION, getString(R.string.location_permission_reason), null);
            }
            return;
        }
        else {
            if (Constants.INTENT_PERMISSION_SENSOR.equals(sIntent)) {
                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)) {
                    sIntent = checkApplicationSetup();
                } else {
                    displayResult("Sensors");
                    doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason), null);
                }
            }

            if (Constants.INTENT_PERMISSION_RECOG.equals(sIntent)) {
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    checkFinished();
                } else {
                    displayResult("Activity");
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG, getString(R.string.recog_permission_reason), null);
                }
            }
            if (Constants.INTENT_PERMISSION_AGE.equals(sIntent)) {
                displayResult("Age");
                doConfirmDialog(Constants.QUESTION_AGE, getString(R.string.ask_user_age), null);
            }
            if (Constants.INTENT_PERMISSION_HEIGHT.equals(sIntent)) {
                displayResult("Height");
                doConfirmDialog(Constants.QUESTION_HEIGHT, getString(R.string.ask_user_height), null);
            }
            if (sIntent.equals(Constants.INTENT_PERMISSION_DEVICE)) {
                displayResult("Device");
                doGetUsePhoneApp();
            }
            if (sIntent.equals(Constants.INTENT_CLOUD_SYNC)) {
                doGetTotalsRefresh();
            }
            if (sIntent.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                displayResult("Storage");
                doConfirmDialog(Constants.QUESTION_STORAGE, getString(R.string.ask_user_storage), null);
            }
            if (sIntent.equals(Constants.USER_PREF_USE_NOTIFICATION)
                    || sIntent.equals(Constants.USER_PREF_USE_AUDIO) || sIntent.equals(Constants.USER_PREF_USE_VIBRATE)) {
                displayResult("Alerts");
                final String sIntent3 = sIntent;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doAlertConfirmDialog(sIntent3);
                    }
                }, 1000);
            }
        }
    }

    private void doGetUsePhoneApp(){
        final ICustomConfirmDialog callbackDevice = new ICustomConfirmDialog() {
            @Override
            public void onCustomConfirmButtonClicked(int question, int button) {
                userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_ASKED, true);
                if (button > 0) {
                    new Handler().post(new SetupPhoneNodeId());
                }else {
                    positiveFlag = -1;
                    userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                    checkFinished();
                }
            }

            @Override
            public void onCustomConfirmDetach() {
            }
        };
        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
            new SetupPhoneNodeId().run();
        else
        doConfirmDialog(Constants.QUESTION_DEVICE,getString(R.string.has_device_text),callbackDevice);
    }
    private void doAlertDialog(String intentString){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SplasherActivity.this, R.style.AlertDialogTheme);
        final DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            userPrefs.setPrefByLabel(intentString, (which == DialogInterface.BUTTON_POSITIVE));
            whichButton = which;
            if (alertDialog != null) alertDialog.dismiss();
        };
        if (intentString.equals(Constants.USER_PREF_USE_AUDIO))
            builder.setMessage(getString(R.string.confirm_use_audio_prompt));
        if (intentString.equals(Constants.USER_PREF_USE_NOTIFICATION))
            builder.setMessage(getString(R.string.confirm_use_notifications_prompt));
        if (intentString.equals(Constants.USER_PREF_USE_VIBRATE))
            builder.setMessage(getString(R.string.confirm_use_vibrate_prompt));

    //    builder.setPositiveButtonIcon(AppCompatResources.getDrawable(SplasherActivity.this ,R.drawable.ic_outline_check_white));
    //    builder.setNegativeButtonIcon(AppCompatResources.getDrawable(SplasherActivity.this ,R.drawable.ic_outline_cancel_white));
        builder.setPositiveButton(R.string.action_okay, clickListener);
        builder.setNegativeButton(R.string.action_cancel, clickListener);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.noti_white_logo);
        try {
            alertDialog = builder.create();
            alertDialog.setOnDismissListener(dialog -> {
                if (intentString.equals(Constants.USER_PREF_USE_NOTIFICATION)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, whichButton == DialogInterface.BUTTON_POSITIVE);
                    onCustomConfirmButtonClicked(QUESTION_NOTIFY,((whichButton == DialogInterface.BUTTON_POSITIVE) ? 1 : 0));
                }
                if (intentString.equals(Constants.USER_PREF_USE_AUDIO)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, whichButton == DialogInterface.BUTTON_POSITIVE);
                    onCustomConfirmButtonClicked(QUESTION_AUDIO,((whichButton == DialogInterface.BUTTON_POSITIVE) ? 1 : 0));
                }
                if (intentString.equals(Constants.USER_PREF_USE_VIBRATE)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, whichButton == DialogInterface.BUTTON_POSITIVE);
                    onCustomConfirmButtonClicked(QUESTION_VIBRATE,((whichButton == DialogInterface.BUTTON_POSITIVE) ? 1 : 0));
                }
            });
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void doAlertConfirmDialog(String intentString){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SplasherActivity.this, R.style.AlertDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_confirmation,null,false);
        TextView title = view.findViewById(R.id.confirm_message);
        ImageButton btnPositive = view.findViewById(R.id.PositiveButton);
        ImageButton btnNegative = view.findViewById(R.id.NegativeButton);

        final View.OnClickListener listener = v -> {
            whichButton = v.getId();
            userPrefs.setPrefByLabel(intentString, (whichButton == R.id.PositiveButton));
            if (alertDialog != null) alertDialog.dismiss();
        };
        btnPositive.setOnClickListener(listener);
        btnNegative.setOnClickListener(listener);
        if (intentString.equals(Constants.USER_PREF_USE_AUDIO))
            title.setText(getString(R.string.confirm_use_audio_prompt));
        if (intentString.equals(Constants.USER_PREF_USE_NOTIFICATION))
            title.setText(getString(R.string.confirm_use_notifications_prompt));
        if (intentString.equals(Constants.USER_PREF_USE_VIBRATE))
            title.setText(getString(R.string.confirm_use_vibrate_prompt));

        builder.setCancelable(true);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        try {
            alertDialog = builder.create();
            alertDialog.setOnDismissListener(dialog -> {
                boolean bSet = (whichButton) == R.id.PositiveButton;
                if (intentString.equals(Constants.USER_PREF_USE_NOTIFICATION)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, bSet);
                    onCustomConfirmButtonClicked(QUESTION_NOTIFY,((bSet) ? 1 : 0));
                }
                if (intentString.equals(Constants.USER_PREF_USE_AUDIO)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, bSet);
                    onCustomConfirmButtonClicked(QUESTION_AUDIO,((bSet) ? 1 : 0));
                }
                if (intentString.equals(Constants.USER_PREF_USE_VIBRATE)) {
                    appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, bSet);
                    onCustomConfirmButtonClicked(QUESTION_VIBRATE,((bSet) ? 1 : 0));
                }
            });
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void displayResult(final String result) {
        mHandler.post(() -> {
            mStatusText.setText(result);
        });
    }
    private void toastResult(final String result){
        final Context context = getApplicationContext();
        mHandler.post(() -> Toast.makeText(context,result,Toast.LENGTH_SHORT).show());
    }
    private void checkFinished(){
        if (confirmDialog != null && (!confirmDialog.isDetached())){
            confirmDialog.dismissAllowingStateLoss();
            confirmDialog = null;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String sCheck = checkApplicationSetup();
                if (sCheck.length() > 0)
                    doUserConfig(sCheck);  // more to do!
                else
                    doStartUp(getReturnIntent());  // all done!
            }
        });
    }
    private void doConfirmDialog(int questionType, String sQuestion, ICustomConfirmDialog callback){
        try {
            int iExistingQuestion = mSavedStateViewModel.getIsInProgress();
            boolean bNewQ = (iExistingQuestion == 0 || (questionType != iExistingQuestion));
            FragmentManager fm = getSupportFragmentManager();
            if (bNewQ) {
                if (fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName()) != null) {
                    CustomConfirmDialog confirmDialog2 = (CustomConfirmDialog) fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName());
                    if (confirmDialog2 != null) fm.beginTransaction().remove(confirmDialog2).commit();
                }
            }else {  /// same question
                if (fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName()) != null) {
                    CustomConfirmDialog confirmDialog2 = (CustomConfirmDialog) fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName());
                    if (confirmDialog2 != null) confirmDialog2.getView().bringToFront();
                }
                return;
            }
            confirmDialog = CustomConfirmDialog.newInstance(questionType, sQuestion, ((callback == null)? SplasherActivity.this : callback));
            if (questionType == Constants.QUESTION_NETWORK)
                confirmDialog.setSingleButton(true);

            mSavedStateViewModel.setIsInProgress(questionType);
            confirmDialog.show(fm, CustomConfirmDialog.class.getSimpleName());
            if (callback != null) confirmDialog.setCallback(callback);
        }catch (IllegalStateException ie){
            FirebaseCrashlytics.getInstance().recordException(ie);
            getSupportFragmentManager().executePendingTransactions();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "doConfirmDialog Error " + e.getMessage());
        }

    }

    private String checkApplicationSetup(){
        Context context = getApplicationContext();
        String intentString = Constants.ATRACKIT_EMPTY;
        ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        if (appPrefs.getLastUserID().length() > 0){
            UserPreferences userPrefs = UserPreferences.getPreferences(context, appPrefs.getLastUserID());
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                intentString = INTENT_PERMISSION_LOCATION;
            else
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED)
                intentString = INTENT_PERMISSION_SENSOR;
            if (intentString.length() == 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (((userPrefs != null) && !userPrefs.getPrefByLabel(Constants.USER_PREF_RECOG_ASKED))
                            || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                        intentString = Constants.INTENT_PERMISSION_RECOG;
                    }else{
                        if (ContextCompat.checkSelfPermission(SplasherActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED)
                            intentString = Manifest.permission.READ_EXTERNAL_STORAGE;
                        else
                        if (userPrefs != null){
                            if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
                                intentString = Constants.INTENT_PERMISSION_DEVICE;
                        }
                    }
                }else {
                    if (((userPrefs != null) && !userPrefs.getPrefByLabel(Constants.USER_PREF_RECOG_ASKED))
                            || ContextCompat.checkSelfPermission(getApplicationContext(), Constants.ATRACKIT_RECOG28_CLASS) != PackageManager.PERMISSION_GRANTED){
                        intentString = Constants.INTENT_PERMISSION_RECOG;
                    }else{
                        if (ContextCompat.checkSelfPermission(SplasherActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED)
                            intentString = Manifest.permission.READ_EXTERNAL_STORAGE;
                        else
                        if (userPrefs != null){
                            if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
                                intentString = Constants.INTENT_PERMISSION_DEVICE;
                        }
                    }
                }
        }else {
            intentString = ATRACKIT_ATRACKIT_CLASS;
        }
        return intentString;
    }
    private void doGetExternalStoragePermission(){
        if (ContextCompat.checkSelfPermission(SplasherActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            if (storagePermissionResultLauncher != null)
                storagePermissionResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            else
                ActivityCompat.requestPermissions(SplasherActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void doGetTotalsRefresh(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SplasherActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_dropdown,null,false);
        TextView title = view.findViewById(R.id.filterLabel);
        title.setText(getString(R.string.label_settings_daily_interval));
        Spinner spinner = view.findViewById(R.id.filterDropDown);
        long lValue = appPrefs.getDailySyncInterval();
        String sText = Long.toString(TimeUnit.MILLISECONDS.toMinutes(lValue));
        final String[] mins_list = getResources().getStringArray(com.a_track_it.workout.common.R.array.minutes_list);
        int indexTimeFrame = 0;
        ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.minutes_list, android.R.layout.simple_spinner_item);
        adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapterFilter);
        if (lValue == 0)
            spinner.setSelection(indexTimeFrame);
        else {
            for(String s: mins_list){
                if (s.equals(sText))
                    break;
                else
                    indexTimeFrame++;
            }
            spinner.setSelection(indexTimeFrame);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        ImageButton btnOk = view.findViewById(R.id.filterPositiveButton);
        ImageButton btnCancel = view.findViewById(R.id.filterNegativeButton);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v2 -> {
            alertDialog.dismiss();
        });
        btnOk.setOnClickListener(v2 -> {
            int iPos = spinner.getSelectedItemPosition();
            if (iPos > 0) {
                String sTime = mins_list[iPos];
                long lValue2 = TimeUnit.MINUTES.toMillis(Long.parseLong(sTime));
                if (lValue2 > 0) appPrefs.setDailySyncInterval(lValue2);
            }else
                appPrefs.setDailySyncInterval(0);
            alertDialog.dismiss();
        });
        btnCancel.setOnClickListener(v2 -> {
            alertDialog.dismiss();

        });
        alertDialog.show();
    }
    private boolean isNetworkConnected(Context context) {
        boolean isConnected = false;
        try {
            final ConnectivityManager cm = (context != null) ? (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE) : null;
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


    private class SetupPhoneNodeId implements Runnable {
        @Override
        public void run() {
            final String sUserID = appPrefs.getLastUserID();
            Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(SplasherActivity.this)
                    .getCapability(Constants.PHONE_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE);
            capabilityInfoTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    if (!capabilityInfo.getName().equals(Constants.PHONE_CAPABILITY_NAME)){
                        toastResult("Phone was not found");
                        appPrefs.setLastNodeID(Constants.ATRACKIT_EMPTY);
                        appPrefs.setLastNodeName(Constants.ATRACKIT_EMPTY);
                        appPrefs.setLastPhoneSync(0L);
                        userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                        SystemClock.sleep(2000L);
                        mIntentAction = Constants.INTENT_PERMISSION_DEVICE;
                        doStartUp(getReturnIntent());
                        return;
                    }

                    final Set<Node> connectedNodes = capabilityInfo.getNodes();
                    if (connectedNodes.size() == 0){
                        toastResult("Phone was not found");
                        appPrefs.setLastNodeID(Constants.ATRACKIT_EMPTY);
                        appPrefs.setLastNodeName(Constants.ATRACKIT_EMPTY);
                        appPrefs.setLastPhoneSync(0L);
                        userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                        SystemClock.sleep(2000L);
                        mIntentAction = Constants.INTENT_PERMISSION_DEVICE;
                        doStartUp(getReturnIntent());
                    }else{
                        String[] possibles = new String[connectedNodes.size()];
                        int index = 0;
                        for(Node n: connectedNodes){
                            possibles[index++] = n.getDisplayName();
                        }
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SplasherActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.dialog_confirmation_listview,null,false);
                        TextView title = view.findViewById(R.id.confirm_message);
                        title.setText("Select phone");
                        ListView listView = view.findViewById(R.id.list_view);
                        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(SplasherActivity.this,R.layout.item_alertlist,possibles);
                      //  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        listView.setAdapter(adapter);
                        listView.setClickable(true);
                       // listView.setSelector(R.drawable.bg_selector);
                      //  mPhoneNodesWithApp = connectedNodes;
                        ImageButton posButton = view.findViewById(R.id.PositiveButton);
                        posButton.setOnClickListener(v -> {
                            if (positiveFlag >= 0) {
                                Iterator<Node> iterator = connectedNodes.iterator();
                                int index1 = 0;
                                while (iterator.hasNext()) {
                                    Node node = iterator.next();
                                    if (index1 == positiveFlag) {
                                        phoneNodeId = node.getId();
                                        appPrefs.setLastNodeID(phoneNodeId);
                                        appPrefs.setLastNodeName(node.getDisplayName());
                                        userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, true);
                                        positiveFlag = 1;
                                        String sKey = Constants.KEY_DEVICE2;
                                        Configuration configuration = null;
                                        List<Configuration> deviceList = mSessionViewModel.getConfigurationLikeName(sKey, sUserID);
                                        if (deviceList.size() == 0) {
                                            configuration = new Configuration(sKey, sUserID, phoneNodeId, 0L, node.getDisplayName(), null);
                                            mSessionViewModel.insertConfig(configuration);
                                        } else {
                                            configuration = deviceList.get(0);
                                            if (!configuration.stringValue1.equals(node.getDisplayName())){
                                                configuration.stringValue = phoneNodeId;
                                                configuration.stringValue1 = node.getDisplayName();
                                                configuration.longValue = 0;
                                                mSessionViewModel.updateConfig(configuration);
                                            }
                                        }
                                        // get ID for non validated (longValue == 0) configuration Device2
                                        if (configuration.longValue == 0){
                                            List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.KEY_DEVICE1, sUserID);
                                            if (existingConfigs.size() > 0) {
                                                Configuration configDevice = existingConfigs.get(0);
                                                DataMap dataMap = new DataMap();
                                                dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                                                dataMap.putInt(KEY_FIT_TYPE, (int)1); // new setup indicator
                                                dataMap.putString(KEY_FIT_USER, sUserID);
                                                dataMap.putString(KEY_FIT_DEVICE_ID, configDevice.stringValue);
                                                dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                                                dataMap.putLong(KEY_FIT_TIME, configDevice.longValue);
                                                dataMap.putString(KEY_PAYLOAD, Constants.MESSAGE_PATH_WEAR_SERVICE);
                                                dataMap.putString(Constants.KEY_DEVICE1, configDevice.stringValue1);
                                                dataMap.putString(Constants.KEY_DEVICE2, configDevice.stringValue2);
                                                Log.w(LOG_TAG, "Sending COMM_TYPE_REQUEST_INFO to phone");
                                                Task<Integer> sendMessageTask =
                                                        Wearable.getMessageClient(
                                                                getApplicationContext()).sendMessage(
                                                                phoneNodeId,
                                                                Constants.MESSAGE_PATH_PHONE_SERVICE,
                                                                dataMap.toByteArray());

                                                sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                                                    @Override
                                                    public void onComplete(Task<Integer> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.w(LOG_TAG, "COMM_TYPE_REQUEST_INFO Message sent successfully " + dataMap.toString());
                                                        } else {
                                                            Log.e(LOG_TAG, "COMM_TYPE_REQUEST_INFO Message failed.");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            alertDialog.dismiss();

                        });
                        ImageButton negButton = view.findViewById(R.id.NegativeButton);
                        negButton.setOnClickListener(v -> {
                            appPrefs.setLastNodeID(ATRACKIT_EMPTY);
                            appPrefs.setLastNodeName(ATRACKIT_EMPTY);
                            appPrefs.setLastNodeSync(0L);
                            userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                            phoneNodeId = ATRACKIT_EMPTY;
                            positiveFlag = -1;
                            alertDialog.dismiss();
                        });
                        builder.setIcon(R.drawable.noti_white_logo);
                        builder.setView(view);
                        alertDialog = builder.create();
                        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position >= 0) {
                                    positiveFlag = position;
                                    ((TextView)view).setTextColor(getColor(R.color.white));
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                positiveFlag = -1;
                            }
                        });
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position >= 0) {
                                    view.setSelected(true);
                                    positiveFlag = position;
                                }
                            }
                        });
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mIntentAction = Constants.INTENT_PERMISSION_DEVICE;
                                doStartUp(getReturnIntent());

                            }
                        });
                        alertDialog.show();
                    }
                }
                else {
                    phoneNodeId = ATRACKIT_EMPTY;
                    toastResult(getString(R.string.action_phone_unavailable));
                    appPrefs.setLastNodeID(ATRACKIT_EMPTY);
                    appPrefs.setLastNodeName(ATRACKIT_EMPTY);
                    appPrefs.setLastNodeSync(0L);
                    positiveFlag = 0;
                    mIntentAction = Constants.INTENT_PERMISSION_DEVICE;
                    doStartUp(getReturnIntent());
                }
            });
        }
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        /* Find a nearby node or pick one arbitrarily. There should be only one phone connected
         * that supports this sample.
         */
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

}
