package com.a_track_it.workout.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.GoalListAdapter;
import com.a_track_it.workout.adapter.MainViewPagerAdapter;
import com.a_track_it.workout.adapter.SensorsListAdapter;
import com.a_track_it.workout.common.AppExecutors;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.activity.IInitialActivityCallback;
import com.a_track_it.workout.common.data_model.ATrackItLatLng;
import com.a_track_it.workout.common.data_model.Bodypart;
import com.a_track_it.workout.common.data_model.Configuration;
import com.a_track_it.workout.common.data_model.CurrentUserDailyTotalsWorker;
import com.a_track_it.workout.common.data_model.DailyCounter;
import com.a_track_it.workout.common.data_model.DateTuple;
import com.a_track_it.workout.common.data_model.Exercise;
import com.a_track_it.workout.common.data_model.FitSyncJobIntentService;
import com.a_track_it.workout.common.data_model.FitnessMetaWorker;
import com.a_track_it.workout.common.data_model.HeightBodypartWorker;
import com.a_track_it.workout.common.data_model.PeakDetProcessor;
import com.a_track_it.workout.common.data_model.Processor;
import com.a_track_it.workout.common.data_model.SensorDailyTotals;
import com.a_track_it.workout.common.data_model.SessionCleanupWorker;
import com.a_track_it.workout.common.data_model.SkuJobIntentService;
import com.a_track_it.workout.common.data_model.TableTuple;
import com.a_track_it.workout.common.data_model.TennisGame;
import com.a_track_it.workout.common.data_model.TwoIDsTuple;
import com.a_track_it.workout.common.data_model.UserDailyTotals;
import com.a_track_it.workout.common.data_model.UserDailyTotalsWorker;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutMeta;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.sensors.BoundAccelSensorManager;
import com.a_track_it.workout.common.sensors.BoundFitnessSensorManager;
import com.a_track_it.workout.common.sensors.BoundFusedLocationClient;
import com.a_track_it.workout.common.sensors.BoundSensorManager;
import com.a_track_it.workout.common.service.ActivityRecognizedService;
import com.a_track_it.workout.common.service.AsyncSetupTask;
import com.a_track_it.workout.common.service.DailySummaryJobIntentService;
import com.a_track_it.workout.common.service.ExerciseDetectedService;
import com.a_track_it.workout.common.user_model.MessagesViewModel;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.a_track_it.workout.common.workers.FitnessRecordingWorker;
import com.a_track_it.workout.common.workers.ImageWorker;
import com.a_track_it.workout.fragment.CustomActivityListDialog;
import com.a_track_it.workout.fragment.CustomAlertDialog;
import com.a_track_it.workout.fragment.CustomConfirmDialog;
import com.a_track_it.workout.fragment.CustomListDialogFragment;
import com.a_track_it.workout.fragment.CustomListFragment;
import com.a_track_it.workout.fragment.CustomScoreDialogFragment;
import com.a_track_it.workout.fragment.FragmentInterface;
import com.a_track_it.workout.fragment.GymConfirmFragment;
import com.a_track_it.workout.fragment.HomeFragment;
import com.a_track_it.workout.fragment.ICustomConfirmDialog;
import com.a_track_it.workout.fragment.MapsFragment;
import com.a_track_it.workout.fragment.OnCustomListItemSelectedListener;
import com.a_track_it.workout.fragment.SettingsDialog;
import com.a_track_it.workout.fragment.ShootingConfirmFragment;
import com.a_track_it.workout.service.CustomIntentReceiver;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static androidx.core.view.InputDeviceCompat.SOURCE_UNKNOWN;
import static com.a_track_it.workout.activity.RoutineActivity.ARG_WORKOUT_META_OBJECT;
import static com.a_track_it.workout.activity.RoutineActivity.ARG_WORKOUT_OBJECT;
import static com.a_track_it.workout.activity.RoutineActivity.ARG_WORKOUT_SET_LIST;
import static com.a_track_it.workout.activity.RoutineActivity.ARG_WORKOUT_SET_OBJECT;
import static com.a_track_it.workout.common.Constants.*;


public class MainActivity extends BaseActivity implements FragmentInterface, OnCustomListItemSelectedListener,
        ICustomConfirmDialog, CustomScoreDialogFragment.onCustomScoreSelected, FragmentManager.OnBackStackChangedListener, AppBarLayout.OnOffsetChangedListener,
        FloatingActionsMenu.OnFloatingActionsMenuUpdateListener, MessageClient.OnMessageReceivedListener {
    private static final int LIC_CHECK_MIN_DAYS = 7;
    private static final int SKU_CHECK_MIN_DAYS = 3;
    private static final long MIN_REP_INTERVAL_MS = TimeUnit.MILLISECONDS.toMillis(750);
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_OAUTH_REQUEST_CODE = 5004;
    public static final int REQUEST_SIGN_IN = 5005;
    private static final int PERMISSION_REQUEST_LOCATION = 5006;
   /// private static final int REQUEST_RECORDING_PERMISSION_CODE = 5007;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOG = 5008;
    private static final int PERMISSION_REQUEST_BODY_SENSORS = 5009;
    private static final int PERMISSION_REQUEST_BODY_CAMERA = 5011;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 5012;
    private static final int RC_REQUEST_RECORDING_AND_CONTINUE_SUBSCRIPTION = 5020;
    private static final int RC_REQUEST_FIT_PERMISSION_AND_CONTINUE = 5021;
    private static final long MIN_CLICK_THRESHOLD = 1000L;
    public static final String ACTION_ADD_ACTIVITY = "com.a_track_it.workout.ADD_ACTIVITY";
    public static final String ARG_ACTIVITY_ID = "ARG_ACTIVITY_ID";  // used by shortcuts for 500 activity Type
    private static final String ANALYTICS_SIGNOUT = "SignOut";
    private static final int GOAL_TYPE_DURATION = 1;
    private static final int GOAL_TYPE_STEPS = 2;
    private static final int GOAL_TYPE_BPM = 3;
    private static final int ALARM_PENDING_SYNC_CODE = 55343;
    private static final int ALARM_PHONE_SYNC_CODE = 55344;
    private static final int ALARM_NETWORK_CHECK_CODE = 55345;
    private static final int ALARM_EXACT_TIME_CODE = 55346;
    private static final String MAP_FRAGMENT_TAG = "map";
    private ActivityResultLauncher<Intent> microphoneActivityResultLauncher;
    private ActivityResultLauncher<String> microphonePermissionResultLauncher;
    private ActivityResultLauncher<String[]> locationPermissionResultLauncher;
    private ActivityResultLauncher<String> recognitionPermissionResultLauncher;
    private ActivityResultLauncher<String> sensorsPermissionResultLauncher;
    private ActivityResultLauncher<String> cameraPermissionResultLauncher;
    private ActivityResultLauncher<String> storagePermissionResultLauncher;
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher;
    private ActivityResultLauncher<Intent> imageCaptureActivityResultLauncher;
    private ActivityResultLauncher<Intent> splashActivityResultLauncher;
    private ActivityResultLauncher<Intent> exerciseActivityResultLauncher;
    private ActivityResultLauncher<Intent> routineActivityResultLauncher;
    private ActivityResultLauncher<Intent> reportActivityResultLauncher;
    private ActivityResultLauncher<Intent> recentActivityResultLauncher;
    private ActivityResultLauncher<Intent> signInActivityResultLauncher;
    private ActivityResultLauncher<Intent> signInSyncActivityResultLauncher;
    private ActivityResultLauncher<Intent> signDailyActivityResultLauncher;
    private AlarmManager mNetworkAlarmManager;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    // Firebase instance variables
    private GoogleSignInAccount mGoogleAccount;
    private WorkManager mWorkManager;
    private ReferencesTools mReferenceTools;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private MainViewPagerAdapter mViewPageAdapter;
    private Set<Node> mWearNodesWithApp = new HashSet<>();
    private xCapabilityListener mCapabilityListener = new xCapabilityListener();
    private OnDataPointListener mDataPointListener = new xFITSensorListener();
    private LocationCallback mLocationListener;
    private ActivityRecognitionClient activityRecognitionClient;
    private Handler mHandler;
    private HandlerThread mSensorThread = new HandlerThread("sensorThread");
    private HandlerThread mWearHandlerThread = new HandlerThread("wearThread");
    private SensorEventListener mSensorListener = new xSensorListener();
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private Runnable sdtRunnable;
    ScheduledFuture<?> detectorHandle0 = null;
    ScheduledFuture<?> sdtHandle = null;
    private long lastInteraction;
    private String wearNodeId = null;
    private Geocoder mGeoCoder;
    private Location mLocation = null;
    private boolean authInProgress = false;
    private boolean bUseListDisplay = true;
    private boolean bUseKG = false;

    private long mLastClickTime=0L;
    private Calendar mCalendar = Calendar.getInstance();
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private TennisGame tennisGame;
    private long backPressedTime = 0;    // used by onBackPressed()
    private MediaPlayer mMediaPlayer;
    private Menu mMenu;
    private MenuItem prevMenuItem;
   // private static final String SHARED_PROVIDER_AUTHORITY = Constants.ATRACKIT_ATRACKIT_CLASS + ".provider";
   // private static final String SHARED_FOLDER = "user_img";
    private BottomNavigationView mBottomNavigationView;
    private boolean bLoading;
    private int shortAnimationDuration;
    private int indexScroll = 0;
    private Intent mStartupIntent;
    // UI outside fragments
    private AppBarLayout appBarLayout;
    private androidx.viewpager2.widget.ViewPager2 mViewPager;
    private View overlay;
    private com.google.android.material.floatingactionbutton.FloatingActionButton floatingActionButton;
    private CustomConfirmDialog confirmDialog;

    private void startSplashActivityForResult(String intentString){
       ActivityOptionsCompat options =
               ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
       Intent intent = new Intent(MainActivity.this, SplashActivity.class);
       if (intentString != null) intent.putExtra(SplashActivity.ARG_ACTION, intentString);
       if (mGoogleAccount != null)
           intent.putExtra(KEY_FIT_USER, mGoogleAccount.getId());
       else
           if (appPrefs.getLastUserID().length() > 0)
               intent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
       intent.putExtra(SplashActivity.ARG_RETURN_RESULT, 1);
       if (mMessagesViewModel != null) mMessagesViewModel.setWorkInProgress(true);
       splashActivityResultLauncher.launch(intent, options);
   }

    private void startExerciseActivityForResult(Exercise exercise, int iType){
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        intent.putExtra(Constants.KEY_FIT_USER, mSavedStateViewModel.getUserIDLive().getValue());
        intent.putExtra(Constants.KEY_FIT_DEVICE_ID, mSavedStateViewModel.getDeviceID());
        intent.putExtra(Constants.KEY_FIT_HOST, mMessagesViewModel.hasPhone());
        if (iType > 0) intent.putExtra(Constants.KEY_FIT_TYPE, 1);
        if (exercise != null) intent.putExtra(ExerciseActivity.ARG_EXERCISE_ID, exercise._id);
        else intent.putExtra(ExerciseActivity.ARG_EXERCISE_ID, 0L);
        if (exercise != null) intent.putExtra(ExerciseActivity.ARG_EXERCISE_OBJECT, exercise);
        intent.putExtra(ExerciseActivity.ARG_RETURN_RESULT, 1);
        exerciseActivityResultLauncher.launch(intent,options);

    }

    private void startRoutineActivityForResult(){
     ActivityOptionsCompat options =
             ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
     Intent intent = new Intent(MainActivity.this, RoutineActivity.class);
     if (mWorkout != null) intent.putExtra(RoutineActivity.ARG_WORKOUT_ID, mWorkout._id);
     if (mWorkout != null) intent.putExtra(RoutineActivity.ARG_WORKOUT_OBJECT, mWorkout);
     if (mWorkoutSet != null) intent.putExtra(RoutineActivity.ARG_WORKOUT_SET_OBJECT, mWorkoutSet);
     if (mWorkoutMeta != null) intent.putExtra(RoutineActivity.ARG_WORKOUT_META_OBJECT, mWorkoutMeta);
     if (mWorkout != null) intent.putExtra(KEY_FIT_USER, mWorkout.userID);
     if (mWorkout != null) intent.putExtra(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
     if ((mSavedStateViewModel != null) && (mSavedStateViewModel.getToDoSetsSize() > 0)){
         if (mSavedStateViewModel.getToDoSets().getValue() != null) {
             ArrayList<WorkoutSet> arraySets = new ArrayList<>(mSavedStateViewModel.getToDoSets().getValue());
             intent.putParcelableArrayListExtra(ARG_WORKOUT_SET_LIST, arraySets);
         }
     }
     intent.putExtra(KEY_FIT_HOST, mMessagesViewModel.hasPhone());
     intent.putExtra(RoutineActivity.ARG_RETURN_RESULT, 1);
     routineActivityResultLauncher.launch(intent,options);
 }

    private void startReportDetailActivityForResult(Workout workout){
     ActivityOptionsCompat options =
             ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
     long activityID = workout.activityID;
     String sValue = (activityID > 0) ? mReferenceTools.getFitnessActivityTextById(activityID) : ATRACKIT_EMPTY;
     Intent intent = new Intent(getApplicationContext(), ReportDetailActivity.class);
     intent.putExtra(ReportDetailActivity.EXTRA_NAME, sValue);
     intent.putExtra(Constants.MAP_DATA_TYPE, OBJECT_TYPE_WORKOUT);
     intent.putExtra(Constants.KEY_FIT_USER, workout.userID);
     intent.putExtra(Constants.KEY_FIT_DEVICE_ID, workout.deviceID);
     intent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
     intent.putExtra(ReportDetailActivity.EXTRA_USE_KG, userPrefs.getUseKG());
     intent.putExtra(ReportDetailActivity.ARG_RETURN_RESULT, 1);
     reportActivityResultLauncher.launch(intent,options);

 }

    private void startRecentActivity(int recentType, int returnType, long startTime) {
        ActivityOptionsCompat options =
                (ActivityOptionsCompat) ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(getApplicationContext(), RecentActivity.class);
        intent.putExtra(Constants.MAP_DATA_TYPE,recentType);
        intent.putExtra(Constants.MAP_START, startTime);
        intent.putExtra(RecentActivity.ARG_RETURN_RESULT, returnType);
        if (options != null)
            recentActivityResultLauncher.launch(intent,options);
        else
            recentActivityResultLauncher.launch(intent);
    }

    private void startDetailActivity(View transitionView, Workout workout, int report, int page) {

        ActivityOptionsCompat options = null;
        if (transitionView != null)
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    MainActivity.this, transitionView, DetailActivity.EXTRA_IMAGE);

        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
        ReferencesTools mRefTools = ReferencesTools.getInstance();
        mRefTools.init(getApplicationContext());
        int iconId = (workout != null) ? mRefTools.getFitnessActivityIconResById(workout.activityID) : mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS);
        intent.putExtra(DetailActivity.EXTRA_IMAGE, iconId);
        intent.putExtra(DetailActivity.ARG_PAGE, page);  // TimeFrame index
        intent.putExtra(DetailActivity.ARG_CHART, 0);
        intent.putExtra(DetailActivity.EXTRA_TITLE, workout.activityName);
        intent.putExtra(DetailActivity.EXTRA_TYPE, workout.activityID);
        intent.putExtra(Constants.KEY_FIT_USER, workout.userID);
        intent.putExtra(Constants.KEY_FIT_DEVICE_ID, workout.deviceID);
        intent.putExtra(Workout.class.getSimpleName(), workout);
        intent.putExtra(Constants.KEY_FIT_TYPE, report);

        if (options != null)
            reportActivityResultLauncher.launch(intent,options);
        else
            reportActivityResultLauncher.launch(intent);
    }
    /**
     * The broadcast receiver class for notifications.
     * Responds to the update notification pending intent action.
     */
    private class AppIntentReceiver extends BroadcastReceiver {

        public AppIntentReceiver() {
        }

        /**
         * Receives the incoming broadcasts and responds accordingly.
         *
         * @param context Context of the app when the broadcast is received.
         * @param intent The broadcast intent containing the action.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the notification.
            handleIntent(intent);
        }
    }

    private AppIntentReceiver mIntentReceiver = new AppIntentReceiver();


    /**
     * The result receiver class for JobIntentService.
     * Updates UI via live data view models
     */
    final  private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mMessagesViewModel.setWorkInProgress(false);
            if (resultCode == 200){  //TODO: look at non 200 results
                mMessagesViewModel.setWorkType(0);
                String sTemp;
                String sUserId;
                if (resultData.containsKey(KEY_FIT_USER))
                    sUserId = resultData.getString(KEY_FIT_USER);
                else
                    sUserId= (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();

                if (resultData.containsKey(Intent.ACTION_BATTERY_CHANGED)) {
                    Integer iVal = resultData.getInt(BatteryManager.EXTRA_LEVEL);
                    sTemp = iVal.toString();
                    if (mMessagesViewModel != null) mMessagesViewModel.addBatteryMsg(sTemp);
                }
                if (resultData.containsKey(INTENT_WIFI_CHANGED) || resultData.containsKey(INTENT_NETWORK_CHANGED)){
                    Boolean bAvail = resultData.getBoolean(KEY_FIT_VALUE, false);
                    if (!bAvail) {
                        startNetworkCheckAlarm(getApplicationContext());
                    }
                    mMessagesViewModel.setCloudAvailable(bAvail);
                }
                if (resultData.containsKey(INTENT_RECOG)){
                    sTemp = resultData.getString(KEY_FIT_NAME);
                    int iType = resultData.getInt(KEY_FIT_TYPE);
                    int state = mSavedStateViewModel.getState();
                    DetectedActivity detectedActivity = resultData.getParcelable(KEY_FIT_RECOG);
                    if (detectedActivity != null){
                        SensorDailyTotals lastSDT = mSavedStateViewModel.getSDT();
                        Log.e(LOG_TAG, "intent INTENT_RECOG rec " + sTemp + " " + iType + " state " + state);
                        if (lastSDT != null) {
                            if (lastSDT.activityType != detectedActivity.getType()) {
                                lastSDT.activityType = detectedActivity.getType();
                                lastSDT.lastActivityType = System.currentTimeMillis();
                                Log.e(LOG_TAG, "updated SDT activityType " + lastSDT.activityType + " " + Utilities.getTimeDateString(lastSDT.lastActivityType));
                                mSavedStateViewModel.setSDT(lastSDT);
                                mSessionViewModel.updateSensorDailyTotal(lastSDT);
                            }
                            if ((state == WORKOUT_CALL_TO_LINE || state == WORKOUT_LIVE || state == WORKOUT_PAUSED)){
                                if (iType == DetectedActivity.IN_VEHICLE) {
                                    if (userPrefs.getPrefByLabel(USER_PREF_STOP_INVEHICLE)){
                                        if (userPrefs.getPrefByLabel(USER_PREF_CONF_END_SESSION))
                                            showAlertDialogConfirm(Constants.ACTION_STOPPING);
                                        else
                                            sessionStop();
                                    }
                                }
                            }
                        }
                        if (mMessagesViewModel != null) mMessagesViewModel.addActivityMsg(sTemp);
                    }
                }
                if (resultData.containsKey(INTENT_SCREEN)){
                    int iScreenFlag = resultData.getInt(INTENT_SCREEN);
                    Log.e(LOG_TAG," Screen Intent " + iScreenFlag);
                }
                if (resultData.containsKey(INTENT_DAILY) && resultData.containsKey(KEY_FIT_ACTION)){
                    long timeMs = System.currentTimeMillis();
                    appPrefs.setLastDailySync(timeMs);
                    if (resultData.containsKey(Constants.MAP_CALORIES) || resultData.containsKey(Constants.MAP_DISTANCE) || resultData.containsKey(Constants.MAP_MOVE_MINS)
                            || resultData.containsKey(Constants.MAP_BPM_AVG) || resultData.containsKey(Constants.MAP_STEPS) || resultData.containsKey(Constants.MAP_WATTS)
                            || resultData.containsKey(Workout.class.getSimpleName())) {
                        if (resultData.getInt(Constants.MAP_STEPS) > 0){
                            if (resultData.getInt(Constants.MAP_STEPS) > 0)
                                mMessagesViewModel.addStepsMsg(Integer.toString(resultData.getInt(Constants.MAP_STEPS)));
                        }
                        if (resultData.getFloat(MAP_HEART_POINTS) > 0F) mMessagesViewModel.addHeartPtsMsg(String.format(Locale.getDefault(), SINGLE_INT,Math.round(resultData.getFloat(MAP_HEART_POINTS))));
                        if (resultData.getInt(Constants.MAP_MOVE_MINS) > 0) mMessagesViewModel.addMoveMinsMsg(Integer.toString(resultData.getInt(Constants.MAP_MOVE_MINS)));
                        if (resultData.getFloat(Constants.MAP_CALORIES) > 0F) mMessagesViewModel.addCaloriesMsg(String.format(Locale.getDefault(), SINGLE_FLOAT,resultData.getFloat(Constants.MAP_CALORIES)));
                        if (resultData.getFloat(Constants.MAP_DISTANCE) > 0F) mMessagesViewModel.addDistanceMsg(String.format(Locale.getDefault(), SINGLE_FLOAT,resultData.getFloat(Constants.MAP_DISTANCE)));
                        if (resultData.getFloat(Constants.MAP_SPEED_AVG) > 0F) mMessagesViewModel.addSpeedMsg(String.format(Locale.getDefault(), SINGLE_FLOAT,resultData.getFloat(Constants.MAP_SPEED_AVG)));
                        int state = mSavedStateViewModel.getState();
                        if ((state != WORKOUT_LIVE) && (state!= WORKOUT_PAUSED) && userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION))
                            sendNotification(INTENT_DAILY, resultData);
                        // existing total  -update
                        Data.Builder builder = new Data.Builder();
                        builder.putString(KEY_FIT_USER, sUserId);
                        builder.putInt(KEY_FIT_TYPE, 0);
                        if (resultData.containsKey(Constants.MAP_HEART_POINTS)) builder.putFloat(Constants.MAP_HEART_POINTS, resultData.getFloat(MAP_HEART_POINTS));
                        if (resultData.containsKey(Constants.MAP_MOVE_MINS)) builder.putInt(Constants.MAP_MOVE_MINS, resultData.getInt(Constants.MAP_MOVE_MINS));
                        if (resultData.containsKey(Constants.MAP_STEPS)) builder.putInt(Constants.MAP_STEPS, resultData.getInt(Constants.MAP_STEPS));
                        if (resultData.containsKey(Constants.MAP_DISTANCE)) builder.putFloat(Constants.MAP_DISTANCE, resultData.getFloat(Constants.MAP_DISTANCE));
                        if (resultData.containsKey(Constants.MAP_CALORIES)) builder.putFloat(Constants.MAP_CALORIES, resultData.getFloat(Constants.MAP_CALORIES));
                        if (resultData.containsKey(Constants.MAP_BPM_MIN)) builder.putFloat(Constants.MAP_BPM_MIN,resultData.getFloat(Constants.MAP_BPM_MIN));
                        if (resultData.containsKey(Constants.MAP_BPM_MAX)) builder.putFloat(Constants.MAP_BPM_MAX,resultData.getFloat(Constants.MAP_BPM_MAX));
                        if (resultData.containsKey(Constants.MAP_BPM_AVG)) builder.putFloat(Constants.MAP_BPM_AVG,resultData.getFloat(Constants.MAP_BPM_AVG));
                        if (resultData.containsKey(Constants.MAP_SPEED_MIN)) builder.putFloat(Constants.MAP_SPEED_MIN,resultData.getFloat(Constants.MAP_SPEED_MIN));
                        if (resultData.containsKey(Constants.MAP_SPEED_MAX)) builder.putFloat(Constants.MAP_SPEED_MAX,resultData.getFloat(Constants.MAP_SPEED_MAX));
                        if (resultData.containsKey(Constants.MAP_SPEED_AVG)) builder.putFloat(Constants.MAP_SPEED_AVG,resultData.getFloat(Constants.MAP_SPEED_AVG));
                        OneTimeWorkRequest workRequest =
                                new OneTimeWorkRequest.Builder(UserDailyTotalsWorker.class)
                                        .setInputData(builder.build())
                                        .build();
                        if (mWorkManager == null)
                            mWorkManager = WorkManager.getInstance(getApplicationContext());
                        if (mWorkManager != null)
                            mWorkManager.enqueue(workRequest);

                        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)){
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_DAILY_UPDATE);
                            dataMap.putString(Constants.KEY_FIT_USER, appPrefs.getLastUserID());
                            if (resultData.getFloat(Constants.MAP_HEART_POINTS) > 0F) dataMap.putFloat(Constants.MAP_HEART_POINTS, resultData.getFloat(Constants.MAP_HEART_POINTS));
                            if (resultData.getInt(Constants.MAP_STEPS) > 0) dataMap.putInt(Constants.MAP_STEPS, resultData.getInt(Constants.MAP_STEPS));
                            if (resultData.getInt(Constants.MAP_MOVE_MINS) > 0) dataMap.putInt(Constants.MAP_MOVE_MINS, resultData.getInt(Constants.MAP_MOVE_MINS));
                            if (resultData.getFloat(Constants.MAP_CALORIES) > 0F) dataMap.putFloat(Constants.MAP_CALORIES, resultData.getFloat(Constants.MAP_CALORIES));
                            if (resultData.getFloat(Constants.MAP_DISTANCE) > 0F) dataMap.putFloat(Constants.MAP_DISTANCE, resultData.getFloat(Constants.MAP_DISTANCE));
                            if (resultData.getFloat(Constants.MAP_SPEED_AVG) > 0F) dataMap.putFloat(Constants.MAP_SPEED_AVG, resultData.getFloat(Constants.MAP_SPEED_AVG));
                            if (resultData.getFloat(Constants.MAP_WATTS) > 0F) dataMap.putFloat(Constants.MAP_WATTS, resultData.getFloat(Constants.MAP_WATTS));
                            sendMessage(wearNodeId,Constants.MESSAGE_PATH_WEAR, dataMap);
                        }
                    }
                    startDailySummarySyncAlarm();
                }
            }
            super.onReceiveResult(resultCode, resultData);
        }
    };
    final private CustomIntentReceiver mCustomIntentReceiver = new CustomIntentReceiver(mResultReceiver);

    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        appPrefs = ApplicationPreferences.getPreferences(context);
        final long lastLic = appPrefs.getLastLicCheck();
        final long timeMs = System.currentTimeMillis();
        mStartupIntent = getIntent();
        boolean noIntent = ((mStartupIntent == null) || (mStartupIntent.getAction() == null));
        if (!noIntent){
            if (mStartupIntent.getAction() != null && mStartupIntent.getAction().equals(Intent.ACTION_MAIN)){
                if (mStartupIntent.hasExtra(SplashActivity.ARG_ACTION)){
                    noIntent = false;
                }else
                    noIntent = true;
            }
        }
        mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(context);
        bLoading = true;
        if (!appPrefs.getAppSetupCompleted())
            if (!mReferenceTools.isNetworkConnected()) {
                doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null);
                return;
            }else
                if ((TimeUnit.MILLISECONDS.toDays(timeMs-lastLic) >= LIC_CHECK_MIN_DAYS)){
                    Intent mySplashIntent = new Intent(context, SplashActivity.class);
                    mySplashIntent.putExtra(KEY_FIT_USER,((mGoogleAccount != null) ? mGoogleAccount.getId():ATRACKIT_EMPTY));
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    mySplashIntent.putExtra(SplashActivity.ARG_ACTION, INTENT_PERMISSION_POLICY);
                    startActivity(mySplashIntent);
                    finish();
                    return;
                }
        mHandler = new Handler(getMainLooper());
        mSensorThread.start();
        mWearHandlerThread.start();
        bUseListDisplay = appPrefs.getPrefByLabel(Constants.LABEL_USE_GRID);
        Log.e(LOG_TAG,"onCreate " + MainActivity.class.getCanonicalName());
        signInActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            try {
                                authInProgress = false;
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                                mGoogleAccount = task.getResult(ApiException.class);
                                if (!mGoogleAccount.isExpired()) {
                                    appPrefs.setLastUserID(mGoogleAccount.getId());
                                    if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), mGoogleAccount.getId());
                                    final String idToken = mGoogleAccount.getIdToken();
                                    appPrefs.setFirebaseAvail((idToken != null) && (idToken.length() > 0));
                                    onSignIn(false);
                                    if ((mViewPageAdapter.getBottomIndex() == 0) && appPrefs.getAppSetupCompleted()) doHomeFragmentRefresh();
                                    if ((idToken != null) && (idToken.length() > 0)) {
                                        if ((appPrefs.getLastUserLogIn() == 0 || !appPrefs.getAppSetupCompleted()) || appPrefs.getFirebaseAvail()) {
                                            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                            mFirebaseAuth.signInWithCredential(credential)
                                                    .addOnCompleteListener(this, task1 -> {
                                                        if (!task1.isSuccessful()) {
                                                            Log.w(LOG_TAG, "signInWithCredential", task1.getException());
                                                            appPrefs.setFirebaseAvail(false);
                                                        } else {
                                                            //mFirebaseUser = task1.getResult().getUser();
                                                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                                                            appPrefs.setFirebaseAvail(true);
                                                            appPrefs.setPrefByLabel("UseFirebase", true);
                                                            if (mFirebaseUser != null) {
                                                                broadcastToast("Firebase " + mFirebaseUser.getDisplayName());
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                    else
                                        Log.i(LOG_TAG, "no idToken on login " + mGoogleAccount.getDisplayName());

                                } else
                                    signIn();
                            } catch (ApiException e) {
                                signInError(e);
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }else
                            quitApp();
                    }else {
                        quitApp();
                    }
                });
        exerciseActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if ((result.getResultCode() == Activity.RESULT_OK) && (result.getData() != null)) {
                        // There are no request codes
                        Intent data = result.getData();
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        Exercise returnedExercise = data.getParcelableExtra(ExerciseActivity.ARG_EXERCISE_OBJECT);
                        if (returnedExercise != null) {
                            if (mWorkoutSet != null) {
                                mWorkoutSet.exerciseID = returnedExercise._id;
                                mWorkoutSet.exerciseName = returnedExercise.name;
                                mWorkoutSet.per_end_xy = returnedExercise.workoutExercise;
                                int defReps = ((userPrefs != null) ? userPrefs.getDefaultNewReps() : 10);
                                int defSets = ((userPrefs != null) ? userPrefs.getDefaultNewSets() : 3);
                                if (returnedExercise.resistanceType != null)
                                    mWorkoutSet.resistance_type = returnedExercise.resistanceType;
                                if (mWorkoutSet.scoreTotal == FLAG_BUILDING) {
                                    mWorkoutSet.repCount = (returnedExercise.lastReps > 0) ? returnedExercise.lastReps : defReps;
                                    mWorkoutSet.setCount = (returnedExercise.lastSets > 0) ? returnedExercise.lastSets : defSets;
                                }
                                if ((returnedExercise.first_BPID != null) && (returnedExercise.first_BPID > 0)) {
                                    mWorkoutSet.bodypartID = returnedExercise.first_BPID;
                                    mWorkoutSet.bodypartName = returnedExercise.first_BPName;
                                }
                                mSavedStateViewModel.setDirtyCount(1);
                                sessionSaveCurrentSet();
                            }
                        }
                    }
                });
        routineActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            if (!data.hasExtra(ARG_WORKOUT_OBJECT) || !data.hasExtra(ARG_WORKOUT_SET_OBJECT)) return;
                            String sAction = data.getAction();
                            mWorkout = data.getParcelableExtra(ARG_WORKOUT_OBJECT);
                            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                            mWorkoutSet = data.getParcelableExtra(ARG_WORKOUT_SET_OBJECT);
                            mWorkoutMeta = data.getParcelableExtra(ARG_WORKOUT_META_OBJECT);
                            if (data.hasExtra(ARG_WORKOUT_SET_LIST)){
                                ArrayList<WorkoutSet> sets = data.getParcelableArrayListExtra(ARG_WORKOUT_SET_LIST);
                                if  ((sets != null) && (sets.size() > 0)) {
                                    mSavedStateViewModel.setToDoSets(sets);
                                }
                            }
                            // action decisions
                            if (sAction.equals(INTENT_TEMPLATE_START)){
                                Workout w = data.getParcelableExtra(ARG_WORKOUT_OBJECT);
                                if (w._id > 1)
                                    sessionNewFromTemplate(w);
                                mSavedStateViewModel.setActiveWorkout(mWorkout);
                                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                                sessionStart();
                            }
                            if (sAction.equals(INTENT_ACTIVE_START)){
                                if (userPrefs.getConfirmStartSession()) {
                                    showAlertDialogConfirm(ACTION_STARTING);
                                    return;
                                }else
                                    onCustomConfirmButtonClicked(ACTION_STARTING, 1); // which sends the intent to start set
                            }
                            if (sAction.equals(INTENT_ACTIVESET_START)){
                                if (userPrefs.getConfirmStartSession()) {
                                    showAlertDialogConfirm(Constants.ACTION_START_SET);
                                    return;
                                }else
                                    onCustomConfirmButtonClicked(ACTION_START_SET, 1); // which sends the intent to start set
                            }
                            if (sAction.equals(INTENT_ACTIVE_STOP)){
                                if (userPrefs.getConfirmEndSession()) {
                                    showAlertDialogConfirm(Constants.ACTION_STOPPING);
                                    return;
                                }else
                                    onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                            }
                        }else{
                            Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                            refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                            refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                            refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
                            mMessagesViewModel.addLiveIntent(refreshIntent);
                        }
                    }
                    else{
                        Intent data = result.getData();
                        if (data != null){
                            if (data.getAction().equals(INTENT_QUIT_APP)) quitApp();
                        }
                    }
                });
        reportActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            String sAction = data.getAction();
                            if (sAction.equals(INTENT_WORKOUT_EDIT)){
                                Intent editIntent = new Intent(sAction);
                                editIntent.putExtras(data);
                                mMessagesViewModel.addLiveIntent(editIntent);
                            }
                        }
                    }
                });
        recentActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            long workoutID = data.getLongExtra(KEY_FIT_WORKOUTID, 0);
                            long setID = data.getLongExtra(KEY_FIT_WORKOUT_SETID, 0);
                            int recentType = data.getIntExtra(KEY_FIT_TYPE, 0);
                            int dataType = data.getIntExtra(MAP_DATA_TYPE, 10);
                            long activityID = data.getLongExtra(Constants.KEY_FIT_ACTIVITYID, 0);
                            String sUserID = data.getStringExtra(KEY_FIT_USER);
                            String sDeviceID = data.getStringExtra(KEY_FIT_DEVICE_ID);
                            String sAction = data.getAction();
                            if (((recentType == 0)||(recentType == 1)) && (workoutID > 0)){
                                Intent intentReport = new Intent(sAction);
                                intentReport.putExtra(KEY_FIT_USER, sUserID);
                                intentReport.putExtra(KEY_FIT_TYPE,dataType);
                                intentReport.putExtra(KEY_FIT_WORKOUTID, workoutID);
                                intentReport.putExtra(KEY_FIT_WORKOUT_SETID, setID);
                                intentReport.putExtra(Constants.KEY_FIT_ACTIVITYID, activityID);
                                intentReport.putExtra(MAP_DATA_TYPE, dataType);
                                intentReport.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                                intentReport.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                mMessagesViewModel.addLiveIntent(intentReport);
                            }
                            if ((recentType == 3) && (workoutID > 0)){
                                sAction = Constants.INTENT_WORKOUT_EDIT;
                                Intent intentReport = new Intent(sAction);
                                intentReport.putExtra(KEY_FIT_USER, sUserID);
                                intentReport.putExtra(KEY_FIT_TYPE,0);
                                intentReport.putExtra(KEY_FIT_WORKOUTID, workoutID);
                                intentReport.putExtra(KEY_FIT_WORKOUT_SETID, setID);
                                intentReport.putExtra(Constants.KEY_FIT_ACTIVITYID, activityID);
                                intentReport.putExtra(MAP_DATA_TYPE, dataType);
                                intentReport.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                                intentReport.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                mMessagesViewModel.addLiveIntent(intentReport);
                            }
                        }
                    }
                });
        imagePickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            // Let's read picked image data - its URI
                            Uri pickedImage = data.getData();
                            // Let's read picked image path using content resolver
                            String[] filePath = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.Media.SIZE };
                            String sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
                            Uri contentUri = null;
                            try {
                                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, sortOrder);
                                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                                int nameColumn =
                                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                                if (cursor.moveToFirst()){
                                    // Get values of columns for a given video.
                                    long id = cursor.getLong(idColumn);
                                    String name = cursor.getString(nameColumn);
                                    int size = cursor.getInt(sizeColumn);
                                    broadcastToast(name);
                                    contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                    Bitmap mImageBitmap = Utilities.getMyBitmap(context, contentUri);
                                    if (mImageBitmap != null) {
                                        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) {
                                            Asset asset = toAsset(mImageBitmap);
                                            sendPhoto(asset, Constants.LABEL_EXT_FILE);
                                        }
                                        if (userPrefs != null) {
                                            userPrefs.setPrefStringByLabel(Constants.LABEL_EXT_FILE, contentUri.toString());
                                            userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_EXT_FILE); // two for camera
                                            FragmentManager fragmentManager = getSupportFragmentManager();
                                            for (Fragment frag : fragmentManager.getFragments()){
                                                if (frag instanceof HomeFragment){
                                                    ((HomeFragment) frag).refreshPersonalImage();
                                                }
                                            }

                                        }

                                    }
                                }
                                cursor.close();
                            }catch (Exception e){
                                Log.e(LOG_TAG, e.getMessage());
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                    }
                });
        imageCaptureActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            Bundle extras = data.getExtras();
                            Bitmap mImageBitmap = (Bitmap) extras.get("data");
                            if (mImageBitmap == null){
                                broadcastToast("Invalid image received");
                                return;
                            }
                            Log.w(LOG_TAG, "Picture captured " + (mImageBitmap.getByteCount()/1000) + "K");
                            if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) {
                                Asset asset = toAsset(mImageBitmap);
                                if (asset != null) sendPhoto(asset, Constants.LABEL_CAMERA_FILE);
                            }
                            // Save bitmap
                            ContentResolver resolver = getContentResolver();
                            String fileName = String.format("camera-%s.png", UUID.randomUUID().toString());
                            try {
                                Uri outputUri = Utilities.saveImageToStorage(resolver, mImageBitmap, fileName);
                                if ((userPrefs != null) && (outputUri != null)){
                                    String sStr = outputUri.toString();
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_CAMERA_FILE, sStr);
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_CAMERA_FILE); // two for camera
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    for (Fragment frag : fragmentManager.getFragments()){
                                        if (frag instanceof HomeFragment && (frag.isVisible())){
                                            ((HomeFragment) frag).refreshPersonalImage();
                                        }
                                    }
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                });
         microphonePermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        String sMsg = getResources().getString(R.string.recording_permission_granted);
                        if(result) {
                            doSnackbar(sMsg,Snackbar.LENGTH_LONG);
                            try {
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                // Start the activity, the intent will be populated with the speech text
                                microphoneActivityResultLauncher.launch(intent);
                            }catch (ActivityNotFoundException anf){
                                doSnackbar(getString(R.string.action_nospeech_activity),Snackbar.LENGTH_LONG);
                            }
                            Log.e(LOG_TAG, "microphonePermissionResultLauncher: PERMISSION GRANTED");
                        } else {
                            sMsg = getResources().getString(R.string.recording_permission_denied);
                            doSnackbar(sMsg, Snackbar.LENGTH_SHORT);
                            Log.e(LOG_TAG, "microphonePermissionResultLauncher: PERMISSION DENIED");
                        }
                    }
                });
/*        locationPermissionResultLauncher = (ActivityResultLauncher<String[]>) registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Boolean[]>() {
                    @Override
                    public void onActivityResult(Boolean[] result) {
                        Boolean[] trueArray = {true, true};
                        if(result.equals(trueArray)) {
                            Log.e(LOG_TAG, "locationPermissionResultLauncher: PERMISSION GRANTED");
                            appPrefs.setUseLocation(true);
                            mMessagesViewModel.setUseLocation(true);
                            bindLocationListener();
                            BoundFusedLocationClient.standardInterval();
                        } else {
                            Log.e(LOG_TAG, "locationPermissionResultLauncher: PERMISSION DENIED");
                            doSnackbar(getString(R.string.action_no_location),Snackbar.LENGTH_LONG);
                            appPrefs.setUseLocation(false);
                            mMessagesViewModel.setUseLocation(false);                        }
                    }
                });*/
        sensorsPermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result) {
                            Log.e(LOG_TAG, "sensorsPermissionResultLauncher: PERMISSION GRANTED");
                            // Permission has been granted.
                            appPrefs.setUseSensors(true);
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
                            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                                    || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)) {
                                if (hasOAuthPermission(0)) {
                                }else
                                    requestOAuthPermission(0);
                            }else{
                                doConfirmDialog(Constants.QUESTION_ACT_RECOG,getString(R.string.recog_permission_reason),null);
                            }
                        }
                        else {
                            Log.e(LOG_TAG, "sensorsPermissionResultLauncher: PERMISSION DENIED");
                        }
                    }
                });
        recognitionPermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, result);
                            Log.e(LOG_TAG, "recognitionPermissionResultLauncher: PERMISSION GRANTED");
                            if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                                    == PackageManager.PERMISSION_GRANTED)) {
                                onSignIn(false);
                            } else
                                doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason),null);
                        } else {
                            Log.e(LOG_TAG, "recognitionPermissionResultLauncher: PERMISSION DENIED");
                            String sMsg = getString(R.string.no_permission_okay);
                            broadcastToast(sMsg);
                        }
                    }
                });
        storagePermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            Log.e(LOG_TAG, "storagePermissionResultLauncher: PERMISSION GRANTED");
                            // Permission has been granted.
                            try {
                                dispatchPickImageIntent();
                            } catch (IOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                        else {
                            Log.e(LOG_TAG, "storagePermissionResultLauncher: PERMISSION DENIED");
                        }
                    }
                });
        cameraPermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            Log.e(LOG_TAG, "cameraPermissionResultLauncher: PERMISSION GRANTED");
                            // Permission has been granted.
                            dispatchTakePictureIntent();
                        }
                        else {
                            Log.e(LOG_TAG, "cameraPermissionResultLauncher: PERMISSION DENIED");
                        }
                    }
                });
        microphoneActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            List<String> results = data.getStringArrayListExtra(
                                    RecognizerIntent.EXTRA_RESULTS);
                            final String spokenText = (results != null) ? results.get(0) : Constants.ATRACKIT_EMPTY;
                            if (spokenText.length() > 0) {
                                if (mMessagesViewModel != null){
                                    mMessagesViewModel.addSpokenMsg(spokenText);
                                }
                            }
                        }
                    }
                });
        splashActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    mMessagesViewModel.setWorkInProgress(false);
                    mMessagesViewModel.setWorkType(0);
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            String sAction = data.getStringExtra(SplashActivity.ARG_ACTION);
                            String sUserId = data.getStringExtra(KEY_FIT_USER);
                            String sLastId = appPrefs.getLastUserID();
                            long lLastLogin = appPrefs.getLastUserLogIn();
                            long timeNowMs = System.currentTimeMillis();
                            if ((mGoogleAccount == null)
                                || (ATRACKIT_ATRACKIT_CLASS.equals(sAction))) {
                                if (ATRACKIT_ATRACKIT_CLASS.equals(sAction) ||
                                        (sLastId.equals(sUserId) && (TimeUnit.MILLISECONDS.toSeconds(timeNowMs - lLastLogin) < 10))) {
                                    if (mGoogleAccount == null)
                                        signInSilent();
                                    else
                                    if (!checkApplicationSetup()) {
                                        String setupAction = INTENT_SETUP;
                                        if (userPrefs != null) {
                                            boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                                            if (!bSetup)
                                                setupAction = INTENT_PERMISSION_DEVICE;
/*                                            if (Utilities.hasSpeaker(context))
                                                if (bSetup) {
                                                    bSetup = appPrefs.getPrefByLabel(AP_PREF_AUDIO_ASKED);
                                                    if (!bSetup) setupAction = USER_PREF_USE_AUDIO;
                                                }
                                            if (Utilities.hasVibration(context))
                                                if (bSetup) {
                                                    bSetup = appPrefs.getPrefByLabel(AP_PREF_VIBRATE_ASKED);
                                                    if (!bSetup) setupAction = Constants.USER_PREF_USE_VIBRATE;
                                                }*/
                                        }else
                                            setupAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                                        startSplashActivityForResult(setupAction);
                                    }
                                }
                                else
                                    signIn();
                            }else {
                                //TODO: add checks for single requests and don't do the whole app setup check
                                if (checkApplicationSetup()) {
                                    // clear setups
                                    mMessagesViewModel.addCurrentMsg(ATRACKIT_EMPTY);
                                    cancelNotification(INTENT_SETUP);
                                    if (!appPrefs.getAppSetupCompleted()) {
                                        appPrefs.setAppSetupCompleted(true);

                                        broadcastToast(getString(R.string.label_setup_complete));
                                    }
                                    if ((mGoogleAccount != null) && (mGoogleAccount.getId().length() > 0)
                                            && (mGoogleAccount.getId().equals(sUserId))) {
                                        userPrefs = UserPreferences.getPreferences(context, sUserId);
                                        if (sAction.equals(INTENT_PERMISSION_DEVICE) && (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))) {
                                            int positiveFlag = data.getIntExtra(Constants.KEY_FIT_TYPE, 0);
                                            if (positiveFlag > 0) {
                                                mMessagesViewModel.setNodeDisplayName(appPrefs.getLastNodeName());
                                                new TaskSendSettingsWear(1, sUserId).run(); // request
                                            } else {
                                                mMessagesViewModel.setPhoneAvailable(false);
                                                userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                                                appPrefs.setLastNodeSync(0);
                                                appPrefs.setLastNodeID(ATRACKIT_EMPTY);
                                                appPrefs.setLastNodeName(ATRACKIT_EMPTY);
                                            }
                                            userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, (positiveFlag > 0));
                                        }
                                        if (sAction.equals(INTENT_PERMISSION_HEIGHT)) {
                                            onCustomItemSelected(Constants.SELECTION_ACTIVITY_GYM, WORKOUT_TYPE_STRENGTH,
                                            mReferenceTools.getFitnessActivityTextById(WORKOUT_TYPE_STRENGTH), mReferenceTools.getFitnessActivityIconResById(WORKOUT_TYPE_STRENGTH),
                                            mReferenceTools.getFitnessActivityIdentifierById(WORKOUT_TYPE_STRENGTH));
                                        }
                                        if (appPrefs.getDailySyncInterval() > 0 && !hasOAuthPermission(0)){
                                            mMessagesViewModel.setWorkType(Constants.TASK_ACTION_DAILY_SUMMARY);
                                            requestOAuthPermission(0);
                                        }
                                    } else
                                        signIn();
                                } else {
                                String setupAction = INTENT_SETUP;
                                if (userPrefs != null) {
                                    boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                                    if (!bSetup)
                                        setupAction = INTENT_PERMISSION_DEVICE;
                                }else
                                    setupAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                                startSplashActivityForResult(setupAction);                                }
                            }
                        }else quitApp();
                    }else quitApp();
                });
        Transition fade = new ChangeBounds();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
        mGeoCoder = new Geocoder(context, Locale.getDefault());
        setActionBarIcon(R.drawable.atrackit_logo);
        try {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_ID, MainActivity.class.getSimpleName());
            if (mFirebaseAnalytics != null) mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, params);
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            if (mGoogleAccount != null) mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }catch(Exception e){
            Log.e(LOG_TAG,"initialise analytics failed " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);

        }
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        // bind UI
        appBarLayout = findViewById(R.id.appBarLayout);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        mViewPager = findViewById(R.id.viewPager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        overlay = (View) findViewById(R.id.main_overlay);

        mNetworkAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mSavedStateViewModel = new ViewModelProvider(MainActivity.this).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(getApplicationContext());
        mSessionViewModel = new ViewModelProvider(MainActivity.this, factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(MainActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.setPhoneAvailable(false);
        mMessagesViewModel.getSpokenMsg().observe(this, s -> {
            if ((s == null) || (s.length()==0)) return;
            String sTarget = mSavedStateViewModel.getSpeechTarget();
          //  String sUserId = (mSavedStateViewModel.getUserID().length() > 0) ? mSavedStateViewModel.getUserID() : appPrefs.getLastUserID();
            if (sTarget.equals(TARGET_EXERCISE_NAME)){
                List<Exercise> exerciseList = mSessionViewModel.getExercisesLike(s);
                if (exerciseList.size() > 0) {
                    Exercise ex = exerciseList.get(0);
                    if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    if (mWorkoutSet != null) {
                        mWorkoutSet.exerciseID = ex._id;
                        mWorkoutSet.exerciseName = ex.name;
                        mWorkoutSet.per_end_xy = ex.workoutExercise;
                        if (mWorkoutSet.bodypartID != ex.first_BPID) mWorkoutSet.bodypartID = ex.first_BPID;
                        mSavedStateViewModel.setDirtyCount(1);
                    }
                }else { // nothing found with that name
                    Exercise newExercise = new Exercise();
                    newExercise._id=0;
                    newExercise.name = s;
                    startExerciseActivityForResult(newExercise, 0);
                }
            }
            broadcastToast(s);
        });
        final Observer<List<Long>> parameterObserver = new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> longs) {
                if (bLoading) return;
                long iObjectType = longs.get(0);
                long iFilterIndex = longs.get(1);
                long iSortIndex = longs.get(2);
                long iSpinnerSource = longs.get(3);
                long iSetFilter = (longs.size() >= 5) ? longs.get(4) : 0;
                Log.e(LOG_TAG, "reportParam changed " + iObjectType + " " + iFilterIndex + " " + iSortIndex);
                if (iObjectType == SELECTION_WORKOUT_AGG){
                    String sParameters = iObjectType + Constants.SHOT_DELIM + iFilterIndex + Constants.SHOT_DELIM + iSortIndex + Constants.SHOT_DELIM + iSpinnerSource + Constants.SHOT_DELIM + iSetFilter;
                    if (userPrefs != null) userPrefs.setPrefStringByLabel(Constants.USER_PREF_ACTIVITY_SETTINGS, sParameters);
                }
/*                if ((iFilterIndex > 0) && (iSetFilter > 0)){
                    int arrayID = 0;
                    if (iObjectType == Constants.SELECTION_BODYPART_AGG){
                        if (iFilterIndex == 1) arrayID = R.array.region_list;
                        if (iFilterIndex == 2) arrayID = R.array.push_pull_list;
                        if (iFilterIndex == 3) arrayID = R.array.number_list;
                        if (iFilterIndex == 4) arrayID = R.array.number_list;
                        if (iFilterIndex == 5) arrayID = R.array.number_list;
                    }
                    if (iObjectType == Constants.SELECTION_EXERCISE_AGG){
                        if (iFilterIndex == 1) arrayID = R.array.bodypart_list;
                        if (iFilterIndex == 2) arrayID = R.array.resistance_type_list;
                        if (iFilterIndex == 3) arrayID = R.array.number_list;
                        if (iFilterIndex == 4) arrayID = R.array.number_list;
                    }
                    if (arrayID > 0) {
                        String[] filterList = getResources().getStringArray(arrayID);
                        int iSet = Math.toIntExact(iSetFilter);
                        if (iSet < filterList.length) {
                         //   mList_Filter.setText(filterList[iSet]);
                         //   crossFadeIn(mList_Filter);
                        }
                    }

                }else
                    crossFadeOut(mList_Filter);*/
                if (iObjectType ==  Constants.SELECTION_BODYPART_AGG){
                    final MenuItem menuItem = mBottomNavigationView.getMenu().getItem(2);
                    final BadgeDrawable badgeDrawable = mBottomNavigationView.getBadge(menuItem.getItemId());
                    final String sTemp = getString(R.string.label_bodypart);
                    final Drawable icon = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_musculous_arm_white);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (floatingActionButton.isOrWillBeShown()) floatingActionButton.setVisibility(View.GONE);
                            if (badgeDrawable != null) badgeDrawable.setVisible(false);
                            menuItem.setTitle(sTemp);
                            menuItem.setContentDescription(sTemp);
                            menuItem.setIcon(icon);
                        }
                    });
 /*                   if ((iSpinnerSource == 0) && (iSortIndex > 0))
                        bodypartAggAdapter.sortItems(iSortIndex);
                    if (iSpinnerSource >= 1){  // filter is the source
                        if ((bodyparts == null) || (bodyparts.size() == 0)) bodyparts = new ArrayList<>(mSessionViewModel.getBodypartList());
                        if (iFilterIndex == 0) bodypartAggAdapter.setBodypartItems(bodyparts,0L);
                        if (iFilterIndex == 1){
                            bodypartAggAdapter.setBodypartItems(bodyparts,iSetFilter); // region
                        }
                        if (iFilterIndex == 2){ // push / pull
                            ArrayList<Bodypart> tempList = new ArrayList<>();
                            if (iSetFilter == 0) { // all
                                bodypartAggAdapter.setBodypartItems(bodyparts, null);
                            }else{
                                for(Bodypart bp : bodyparts){
                                    if ((iSetFilter == 1) && (bp.flagPushPull == 1)) tempList.add(bp);
                                    if ((iSetFilter == 2) && (bp.flagPushPull == -1)) tempList.add(bp);
                                }
                            }
                            bodypartAggAdapter.setBodypartItems(tempList,null); // push-pull
                        }
*/        }
                if (iObjectType ==  Constants.SELECTION_EXERCISE_AGG){
                    final MenuItem menuItem = mBottomNavigationView.getMenu().getItem(2);
                    final BadgeDrawable badgeDrawable = mBottomNavigationView.getBadge(menuItem.getItemId());
                    final String sTemp = getString(R.string.label_exercise);
                    final Drawable icon = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_gym_equipmemt);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (floatingActionButton.isOrWillBeShown()) floatingActionButton.setVisibility(View.VISIBLE);
                            if (badgeDrawable != null) badgeDrawable.setVisible(true);
                            menuItem.setTitle(sTemp);
                            menuItem.setContentDescription(sTemp);
                            menuItem.setIcon(icon);
                        }
                    });
                }
                if (iObjectType == Constants.OBJECT_TYPE_WORKOUT) {  // page fragment
                }
            }
        };
        mMessagesViewModel.getReportParameters().observe(this, parameterObserver);
        mMessagesViewModel.getLiveIntent().observe(this, new Observer<Intent>() {
            @Override
            public void onChanged(Intent intent) {
                if (intent == null) return;
                final String intentAction = intent.getAction();
                final Context ctx = getApplicationContext();
                Log.w(LOG_TAG,"liveIntent " + intentAction);
                switch (intentAction){
                    case Constants.INTENT_VIBRATE:
                        int count = 1;
                        if (intent.hasExtra(KEY_FIT_TYPE))
                            count = intent.getIntExtra(KEY_FIT_TYPE,1);
                        vibrate((long)count);
                        break;
                    case INTENT_MESSAGE_TOAST:
                        if (intent.hasExtra(Constants.INTENT_EXTRA_MSG)){
                            final int iType = intent.getIntExtra(KEY_FIT_TYPE,0);
                            final int length = intent.getIntExtra(KEY_FIT_VALUE, Toast.LENGTH_SHORT);
                            String sMessage = intent.getStringExtra(Constants.INTENT_EXTRA_MSG);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast toast = Toast.makeText(ctx, sMessage, length);
                                    // toast.
                                    View view = toast.getView();
                                    TextView toastMessage2 = (TextView) view.findViewById(android.R.id.message);
                                    if (iType == 1) {
                                        view.setBackgroundResource(android.R.drawable.toast_frame);
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.workout.common.R.drawable.custom_toast));
                                        toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.workout.common.R.color.white_pressed));
                                    } else {
                                        if (iType == 2) {
                                            view.setBackgroundResource(android.R.drawable.toast_frame);
                                            view.setBackgroundColor(Color.TRANSPARENT);
                                            toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.workout.common.R.drawable.custom_wear_toast));
                                            toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.workout.common.R.color.secondaryTextColor));
                                        } else
                                            toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, android.R.color.black));
                                    }
                                    toast.show();
                                }
                            });
                        }
                        break;

                    case Intent.ACTION_SHUTDOWN :
                        final Intent quitIntent = new Intent(INTENT_QUIT_APP);
                        mHandler.post(() -> handleIntent(quitIntent));

                        break;
                    default:
                        handleIntent(intent);
                }
            }
        });
        mMessagesViewModel.getWorkInProgress().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                final int workType = mMessagesViewModel.getWorkType();
                String sTitle = getString(R.string.action_cloud_running);
                switch (workType){
                    case Constants.TASK_ACTION_INSERT_HISTORY:
                        sTitle=getString(R.string.action_cloud_history);
                        break;
                    case Constants.TASK_ACTION_READ_SESSION:
                    case Constants.TASK_ACTION_ACT_SEGMENT:
                    case Constants.TASK_ACTION_EXER_SEGMENT:
                    case Constants.TASK_ACTION_SYNC_WORKOUT:
                        sTitle = getString(R.string.action_cloud_save);
                        break;
                    case Constants.TASK_ACTION_READ_LOCAL:
                        sTitle = getString(R.string.action_db_running);
                        break;
                    case Constants.TASK_ACTION_WRITE_CLOUD:
                        sTitle = getString(R.string.action_sync_start);
                        break;
                    case Constants.TASK_ACTION_SYNC_DEVICE:
                        sTitle = getString(R.string.action_device_running);
                        break;
                    case Constants.TASK_ACTION_READ_GOALS:
                    case Constants.TASK_ACTION_READ_BPM:
                    case Constants.TASK_ACTION_READ_HISTORY:
                    case Constants.TASK_ACTION_READ_CLOUD:
                        sTitle = getString(R.string.action_cloud_verify);
                        break;
                }
                int notificationID = NOTIFICATION_SUMMARY_ID;
                if (aBoolean) {
                    //int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                    int iconColor = getColor(R.color.primaryLightColor);
                    Context context = getApplicationContext();
                    Drawable icon_outlined = AppCompatResources.getDrawable(context, R.drawable.ic_a_outlined);
                    Utilities.setColorFilter(icon_outlined, iconColor);
                    Intent viewIntent = new Intent(context, MainActivity.class);
                    viewIntent.setAction(INTENT_HOME_REFRESH);
                    viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    notificationID = NOTIFICATION_SUMMARY_ID;
                    String sContent = getString(R.string.app_name);
                    String sChannelID = SUMMARY_CHANNEL_ID;
                    PendingIntent pendingViewIntent = PendingIntent.getActivity(context, notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingViewIntent5 = PendingIntent.getActivity
                            (getApplicationContext(), notificationID, viewIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder notifyBuilder5 = new NotificationCompat
                            .Builder(context, sChannelID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon(R.drawable.ic_a_outlined)
                            .setProgress(0, 0, true)
                            .setOngoing(true)
                            .setContentInfo(getString(R.string.action_cloud_running))
                            .setContentIntent(pendingViewIntent5)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                    try {
                        if (notificationManager != null)
                            notificationManager.notify(notificationID, notifyBuilder5.build());
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }else{
                    notificationManager.cancel(notificationID);
                }
            }
        });
        mMessagesViewModel.getCloudAvailable().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean cloudAvailable) {
                if (cloudAvailable){
                    if ((userPrefs != null) && (userPrefs.getUserId().length() > 0)
                            && (userPrefs.getReadDailyPermissions() && appPrefs.getAppSetupCompleted())){
                        startDailySummarySyncAlarm();
                        startCloudPendingSyncAlarm();
                    }
                }
            }
        });
        mSavedStateViewModel.getUserIDLive().observe(MainActivity.this, s -> Log.i(LOG_TAG, "liveUSERID " + ((s == null || s.length() == 0) ? "null" : s)));
        mSavedStateViewModel.getCurrentState().observe(MainActivity.this, currentState -> {
            boolean bSetup = appPrefs.getAppSetupCompleted();
            Log.e(LOG_TAG, MainActivity.class.getSimpleName() + " currentState " + Utilities.currentStateTitle(context, currentState));
            int iStackCount = getSupportFragmentManager().getBackStackEntryCount();
            int previousState;
            Log.e(LOG_TAG, "OBSERVE currentSTATE " + currentState);
            if (currentState == WORKOUT_SETUP || !bSetup){
                floatingActionButton.setVisibility(floatingActionButton.GONE);
                mViewPager.setUserInputEnabled(false);
                crossFadeOut(mBottomNavigationView);
                if (!overlay.isShown()) crossFadeIn(overlay);
                return;
            }else{
                if (appPrefs.getUseSensors()) {
                    previousState = BoundSensorManager.getState();
                    if (BoundSensorManager.isInitialised()) BoundSensorManager.setState(currentState);
                    if (BoundFitnessSensorManager.isInitialised()) BoundFitnessSensorManager.setState(currentState);
                }else
                    previousState = WORKOUT_INVALID;

                if (appPrefs.getUseLocation() && BoundFusedLocationClient.isInitialised()) BoundFusedLocationClient.setState(currentState);
            }
            Workout localWorkout = mSavedStateViewModel.getActiveWorkout().getValue() != null
                    ? mSavedStateViewModel.getActiveWorkout().getValue() : mWorkout;
            String sPersonID = (mGoogleAccount != null) ? mGoogleAccount.getId() : ATRACKIT_EMPTY;
            switch (currentState){
                case WORKOUT_PENDING:
                    if (appPrefs.getUseSensors() && userPrefs.getReadSensorsPermissions()) {
                        if (BoundSensorManager.isInitialised()) destroyAccelSensorListener();
                        if (BoundSensorManager.isInitialised()) BoundSensorManager.doReset(0);
                        if (!BoundFitnessSensorManager.isInitialised()) bindFitSensorListener();
                    }
                    if (BoundFusedLocationClient.isInitialised()){
                        BoundFusedLocationClient.clearInterval();
                    }else
                        if (appPrefs.getUseLocation() && (sPersonID.length() > 0)) bindLocationListener();
                    if (userPrefs != null && userPrefs.getPrefByLabel(USER_PREF_RECOG) && (activityRecognitionClient == null)) bindRecognitionListener();
                    break;
                case WORKOUT_CALL_TO_LINE:
                    if (overlay.isShown()) crossFadeOut(overlay);
                    if (appPrefs.getUseSensors() && Utilities.hasSensorDevicesPermission(getApplicationContext())) {
                        if ((userPrefs != null) && userPrefs.getReadSessionsPermissions() && (appPrefs.getUseSensors())) {
                            if (BoundSensorManager.getCount(0) > 0) BoundSensorManager.doReset(0);
                            if ((appPrefs.getStepsSensorCount() > 0) && (BoundSensorManager.getCount(1) == 0)) bindSensorListener(1,0);
                            if ((appPrefs.getBPMSensorCount() > 0) && (BoundSensorManager.getCount(2) == 0)) bindSensorListener(2,0);

                            bindFitSensorListener();
                        }
                        bindAccelSensorListener();
                        if (Utilities.isGymWorkout(localWorkout.activityID) && (mGoogleAccount != null)) doRegisterExerciseDetectionService(mGoogleAccount);

                    }
                    floatingActionButton.setVisibility(floatingActionButton.GONE);
                    if (mBottomNavigationView.getSelectedItemId() != R.id.btm_menu_tracking)
                        mBottomNavigationView.setSelectedItemId(R.id.btm_menu_tracking);
                    mViewPager.setUserInputEnabled(false);
                    crossFadeOut(mBottomNavigationView);
                    break;
                case WORKOUT_LIVE:
                case WORKOUT_RESUMED:
                case WORKOUT_PAUSED:
                    if (overlay.isShown()) crossFadeOut(overlay);
                    if (currentState == WORKOUT_PAUSED){
                        if (localWorkout != null && localWorkout.activityID != null) {
                            if (Utilities.isGymWorkout(localWorkout.activityID) || Utilities.isShooting(localWorkout.activityID)
                                    || Utilities.isAquatic(localWorkout.activityID)) {
                                destroyAccelSensorListener();
                                if (mSavedStateViewModel.getIsGym() && (mGoogleAccount != null)) doUnRegisterExerciseDetectionService(mGoogleAccount);
                            }
                        }
                    }
                    if (currentState == WORKOUT_LIVE){
                        if (previousState != WORKOUT_LIVE) CustomIntentReceiver.cancelAlarm(getApplication());
                        if (previousState == WORKOUT_CALL_TO_LINE){

                        }else
                        if (appPrefs.getUseSensors() && Utilities.hasSensorDevicesPermission(getApplicationContext())) {
                            if ((userPrefs != null) && userPrefs.getReadSessionsPermissions() && (appPrefs.getUseSensors())) {
                                if (BoundSensorManager.getCount(0) > 0) BoundSensorManager.doReset(0);
                                if ((appPrefs.getStepsSensorCount() > 0) && (BoundSensorManager.getCount(1) == 0)) bindSensorListener(1,0);
                                if ((appPrefs.getBPMSensorCount() > 0) && (BoundSensorManager.getCount(2) == 0)) bindSensorListener(2,0);
                                if (!BoundFitnessSensorManager.isInitialised()) bindFitSensorListener();
                            }
                        }
                        if (localWorkout != null && localWorkout.activityID != null){
                            if (Utilities.isGymWorkout(localWorkout.activityID) || Utilities.isShooting(localWorkout.activityID)
                                    || Utilities.isAquatic(localWorkout.activityID)) {
                                bindAccelSensorListener();
                            }
                            if (mMessagesViewModel.getUseLocation().getValue() != null && (mMessagesViewModel.getUseLocation().getValue())) {
                                if (!BoundFusedLocationClient.isInitialised()) bindLocationListener();
                                if (mReferenceTools.getActivityHighlyMobileById(localWorkout.activityID) > 0)
                                    BoundFusedLocationClient.customInterval(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(5));
                                else
                                    BoundFusedLocationClient.standardInterval();
                            }
                        }
                    }
                    floatingActionButton.setVisibility(View.GONE);
                    if (mBottomNavigationView.getSelectedItemId() != R.id.btm_menu_tracking)
                        mBottomNavigationView.setSelectedItemId(R.id.btm_menu_tracking);
                    mViewPager.setUserInputEnabled(false);
                    crossFadeOut(mBottomNavigationView);
                    break;

                case WORKOUT_COMPLETED:
                    BoundSensorManager.doReset(0);
                    BoundSensorManager.doReset(1);
                    BoundSensorManager.doReset(2);
                    BoundFitnessSensorManager.doReset();
                    onMenuCollapsed();
                    crossFadeIn(mBottomNavigationView);
                    floatingActionButton.setVisibility(View.VISIBLE);
                    mViewPager.setUserInputEnabled(false);
                    if (iStackCount == 0){
                        doHomeFragmentRefresh();
                    }
                    break;
                case WORKOUT_INVALID:
                    if (previousState == WORKOUT_COMPLETED){
                        mHandler.postDelayed(() -> startCloudPendingSyncAlarm(),10000);
                    }
                    if (appPrefs.getUseSensors() && (sPersonID.length() > 0)) {
                        bindSensorListener(0, -1);
                        bindSensorListener(1, -1);
                        bindSensorListener(2, -1);
                        if (!BoundFitnessSensorManager.isInitialised()) bindFitSensorListener();
                    }
                    if (appPrefs.getUseLocation()) {
                        if (!BoundFusedLocationClient.isInitialised()) bindLocationListener();
                        BoundFusedLocationClient.standardInterval();
                    }
                    if (userPrefs != null && userPrefs.getPrefByLabel(USER_PREF_RECOG) && (activityRecognitionClient == null)) bindRecognitionListener();
                    onMenuCollapsed();
                    crossFadeIn(mBottomNavigationView);
                    floatingActionButton.setVisibility(View.VISIBLE);
                    mViewPager.setUserInputEnabled(false);
                    if (iStackCount == 0){
                        doHomeFragmentRefresh();
                    }
                    break;
            }

        });
        mSessionViewModel.liveAllConfigs(Constants.ATRACKIT_ATRACKIT_CLASS, ATRACKIT_SETUP).observe(this, configurations -> {
            if ((configurations == null) || (configurations.size() < 1)) {
                // WORKOUT_SETUP
                if (mWorkout == null) createWorkout(Constants.ATRACKIT_ATRACKIT_CLASS,Constants.ATRACKIT_ATRACKIT_CLASS);
                if ((mWorkout.start != -1) || (mWorkout.end != -1)){
                    mWorkout.start = -1; mWorkout.end = -1;  // state setup
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                }
                if (appPrefs.getAppSetupCompleted()) appPrefs.setAppSetupCompleted(false);
                sessionSetCurrentState(WORKOUT_SETUP);
            }
        });
        mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(context);
        mWorkManager = WorkManager.getInstance(context);

        String sReviewSettings = (userPrefs != null) ? userPrefs.getPrefStringByLabel(Constants.USER_PREF_REPORT_SETTINGS) : ATRACKIT_EMPTY;
        int myType = SELECTION_BODYPART_AGG;
        if (sReviewSettings.length() > 0 && sReviewSettings.contains(SHOT_DELIM)){
            String[] params = sReviewSettings.split(SHOT_DELIM);
            if (params[0].length() > 0) myType = Integer.parseInt(params[0]);
        }

        if ((appPrefs.getLastUserID().length() > 0) || (appPrefs.getDeviceID().length() > 0)){
            mViewPageAdapter = new MainViewPagerAdapter(MainActivity.this,appPrefs.getLastUserID(),appPrefs.getDeviceID(),myType);
        }else{
            mViewPageAdapter = new MainViewPagerAdapter(MainActivity.this);
            mViewPageAdapter.setUserDevice(appPrefs.getLastUserID(),appPrefs.getDeviceID());
            mViewPageAdapter.setDefaultType(myType);
        }
        mViewPageAdapter.setPeriodIndex(0);
        mViewPageAdapter.setUseGridDisplay(bUseListDisplay);
        mViewPageAdapter.setPeriodTitles(getResources().getStringArray(R.array.period_types));

        mViewPager.setAdapter(mViewPageAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                if (position == 0)
                    toolbar.setTitle(getString(R.string.app_name));
                if (position == 1) {
                    toolbar.setTitle(getString(R.string.label_activity));
                }
                if (position == 2) {
                    toolbar.setTitle(ATRACKIT_SPACE);
                    List<Long> params = mMessagesViewModel.getReportParameters().getValue();
                    if (params != null) {
                        if (params.get(0) == SELECTION_EXERCISE_AGG) {
                            doPendingExercisesCheck(appPrefs.getLastUserID());
                            final ColorStateList colorStateList = ContextCompat.getColorStateList(MainActivity.this, R.color.primaryLightColor);
                            Drawable d = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_white);
                            Utilities.setColorFilter(d,getColor(R.color.secondaryColor));
                            final Drawable floatIcon = d;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    floatingActionButton.setBackgroundTintList(colorStateList);
                                    floatingActionButton.setImageDrawable(floatIcon);
                                    floatingActionButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }else{
                            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(2);
                            final BadgeDrawable badgeDrawable = mBottomNavigationView.getBadge(menuItem.getItemId());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    floatingActionButton.setVisibility(View.GONE);
                                    if (badgeDrawable != null) badgeDrawable.clearNumber();
                                }
                            });
                        }
                    }
                }
/*                if (position == 3) {
                    toolbar.setTitle(ATRACKIT_SPACE);
                    doPendingExercisesCheck(appPrefs.getLastUserID());
                }*/

                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = mBottomNavigationView.getMenu().getItem(position);
                invalidateOptionsMenu();
            }
        });
        mViewPager.setUserInputEnabled(false);
        invalidateOptionsMenu();

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mHandler.post(() -> {
                    if (item.getItemId() == R.id.btm_menu_tracking){
                        mViewPager.setCurrentItem(0);
                        floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.secondaryColor));
                        Drawable d = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_white);
                        floatingActionButton.setImageDrawable(d);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    }

                    if (item.getItemId() == R.id.btm_menu_completed){
                        toolbar.setTitle(ATRACKIT_EMPTY);
                        mViewPager.setCurrentItem(1);
                        floatingActionButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_selector_archery));
                        floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.secondaryColor));
                        Drawable d = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_white);
                        floatingActionButton.setImageDrawable(d);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        if (userPrefs != null) {
                            long historyAsked = userPrefs.getLongPrefByLabel(Constants.AP_PREF_HISTORY_ASKED);
                            if (historyAsked == 0) {
                                userPrefs.setLongPrefByLabel(Constants.AP_PREF_HISTORY_ASKED, timeMs);
                                doConfirmDialog(Constants.QUESTION_HISTORY_LOAD, getString(R.string.load_user_history), MainActivity.this);
                            }
                        }
                    }
                    if (item.getItemId() == R.id.btm_menu_bodyparts){
/*                        if (mMessagesViewModel.getReportParameters().getValue() == null){
                            List<Long> params = new ArrayList<>(5);
                            params.set(0,(long)(long)Constants.OBJECT_TYPE_BODYPART);
                            params.set(1,(long)0);
                            params.set(2,(long)0);
                            params.set(3,(long)0);
                            params.set(4,(long)0);  // filter not set
                            mMessagesViewModel.addReportParameters(params);
                        }*/
                        mViewPager.setCurrentItem(2);
                    }
                });
                return true;
            }
        });

        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });
        floatingActionButton.setOnClickListener(v -> {
            if (bLoading) return;
            if (mBottomNavigationView.getSelectedItemId() == R.id.btm_menu_bodyparts){
                Exercise newExercise = new Exercise();
                newExercise._id = 0;
                newExercise.name = "";
                startExerciseActivityForResult(newExercise,0);
            }else {
                CustomActivityListDialog activityListDialog = CustomActivityListDialog.getInstance();
                activityListDialog.setFragmentInterfaceCallback(MainActivity.this);
                activityListDialog.show(getSupportFragmentManager(), activityListDialog.TAG);
            }
        });
        if (appPrefs.getLastUserLogIn() == 0) {
            appPrefs.setAppSetupCompleted(false);
            createNotificationChannels(); // setup notification manager and channels if needed
            bUseKG = !(Locale.getDefault().equals(Locale.US));
            Bundle bundle = new Bundle();
            bundle.putString(INTENT_SETUP, "false");
            sendNotification(Constants.INTENT_SETUP, bundle);
            mWorkout = new Workout();
            mWorkout._id = (System.currentTimeMillis());
            mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            mWorkout.start = -1; mWorkout.end = -1; // setup mode !
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            sessionSetCurrentState(WORKOUT_SETUP);
            IInitialActivityCallback setupCallback = () -> {
                broadcastToast("reference files setup has completed");
                Boolean bAvail = Utilities.hasMicrophone(MainActivity.this);
                appPrefs.setMicrophoneAvail(bAvail);
                if (bAvail)
                    broadcastToast("microphone is available");
                bAvail = Utilities.hasSpeaker(MainActivity.this);
                appPrefs.setSpeakerAvail(bAvail);
                if (bAvail)
                    broadcastToast("speaker is available");
                if (userPrefs != null) {
                    userPrefs.setUseKG(bUseKG);
                    userPrefs.setPrefByLabel(USER_PREF_USE_AUDIO, bAvail);
                    bAvail = Utilities.hasVibration(MainActivity.this);
                    userPrefs.setPrefByLabel(USER_PREF_USE_VIBRATE, bAvail);
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, true);
                }
                String sAction = INTENT_SETUP;
                if (userPrefs != null) {
                    boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                    if (!bSetup)
                        sAction = INTENT_PERMISSION_DEVICE;
                }else
                    sAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                startSplashActivityForResult(sAction);
                return;
            };
            AsyncSetupTask setupTask = new AsyncSetupTask(setupCallback, context);
            setupTask.execute();
        }
        else {
            // check if permissions are setup install could still be needed.
            if (!checkApplicationSetup()){
                appPrefs.setAppSetupCompleted(false);
                createNotificationChannels(); // setup notification manager and channels if needed
                Bundle bundle = new Bundle();
                bundle.putString(INTENT_SETUP, "false");
                mWorkout = new Workout();
                mWorkout._id = (System.currentTimeMillis());
                mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                mWorkout.start = -1; mWorkout.end = -1; // setup mode !
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                sessionSetCurrentState(WORKOUT_SETUP);
                sendNotification(Constants.INTENT_SETUP, bundle);
                String sAction = INTENT_SETUP;
                if (userPrefs != null) {
                    boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                    if (!bSetup) sAction = INTENT_PERMISSION_DEVICE;
                }
                else
                    sAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                startSplashActivityForResult(sAction);
                return;
            }
            else{
                cancelNotification(INTENT_SETUP);
                if (!appPrefs.getAppSetupCompleted()) {
                    appPrefs.setAppSetupCompleted(true);
                    Boolean bAvail = Utilities.hasMicrophone(context);
                    appPrefs.setMicrophoneAvail(bAvail);
                    if (bAvail)
                        broadcastToast("microphone is available");
                    bAvail = Utilities.hasSpeaker(context);
                    appPrefs.setSpeakerAvail(bAvail);
                    if (bAvail)
                        broadcastToast("speaker is available");
                    broadcastToast(getString(R.string.label_setup_complete));
                }
                if (((mGoogleAccount == null) || (mGoogleAccount.getId() == null) || (mGoogleAccount.isExpired()))
                        && (!mStartupIntent.hasExtra(SplashActivity.ARG_ACTION))) {
                    if (appPrefs.getLastUserLogIn() > 0 && ((timeMs - appPrefs.getLastUserLogIn()) < TimeUnit.HOURS.toMillis(24)))
                        signInSilent();
                    else {
                        sessionSetCurrentState(WORKOUT_SETUP);
                        startSplashActivityForResult(ATRACKIT_ATRACKIT_CLASS);
                        return;
                    }
                }
            }
            if (!noIntent) // has an intent!
                handleIntent(mStartupIntent);

        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onUserInteraction(){
        lastInteraction = System.currentTimeMillis();
        Log.w(MainActivity.class.getSimpleName(), "onUserInteraction called");
    }
    @Override
    public void onResume() {
        super.onResume();
        if (overlay.getVisibility() == View.VISIBLE){
            onMenuCollapsed();
        }
   //     floatingActionMenu.collapse();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (!authInProgress  && (mGoogleAccount == null)) {
            long timeMs = System.currentTimeMillis();
            if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
            long lLastLogin = appPrefs.getLastUserLogIn();
            if (!appPrefs.getAppSetupCompleted()) return;
            if ((timeMs - lLastLogin) < TimeUnit.HOURS.toMillis(24) && (lLastLogin > 0))
                signInSilent();
            else
                signIn();
        }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Context context = getApplicationContext();
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId() : mSavedStateViewModel.getUserIDLive().getValue();
        if ((context == null) || (sUserId == null) || (sUserId.length() == 0)) Log.i(LOG_TAG, "UserId NOT SET onSaveInstanceState");
        else {
            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserId);
            if (userPrefs != null) {
                outState.putInt(Constants.KEY_USE_KG,(userPrefs.getUseKG() ? 1 : 0));
                outState.putString(KEY_FIT_DEVICE_ID,mSavedStateViewModel.getDeviceID());
                outState.putString(KEY_FIT_USER,sUserId);
                // save in-progress stuff
                if (mWorkout != null && (mWorkout._id > 0) && (mWorkout.start > 0) && (mWorkout.end == 0)){
                    outState.putParcelable(Workout.class.getSimpleName(), mWorkout);
                    outState.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                    outState.putParcelable(WorkoutMeta.class.getSimpleName(), mWorkoutMeta);
                }
                mSavedStateViewModel.saveToPreferences(userPrefs);
            }
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onStart() {
        super.onStart();

        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(mWearHandlerThread.getLooper()).build();
        Wearable.getCapabilityClient(MainActivity.this, options).addListener(mCapabilityListener, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
        Wearable.getMessageClient(MainActivity.this).addListener(MainActivity.this);

        setupListenersAndIntents(getApplicationContext());
        appBarLayout.addOnOffsetChangedListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        bLoading = false;
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, "onStop");
        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(mWearHandlerThread.getLooper()).build();
        Wearable.getCapabilityClient(MainActivity.this,options).removeListener(mCapabilityListener);
        Wearable.getMessageClient(MainActivity.this).removeListener(this);
        appBarLayout.removeOnOffsetChangedListener(this);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        if (mCustomIntentReceiver.isRegistered()) {
            MainActivity.this.unregisterReceiver(mCustomIntentReceiver);
            mCustomIntentReceiver.setRegistered(false);
        }
        if (mIntentReceiver != null) {
            this.unregisterReceiver(mIntentReceiver);
            mIntentReceiver = null;
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  floatingActionMenu.collapse();
    }

    @Override
    protected void onDestroy() {
        try {
            if (mViewPageAdapter != null) mViewPageAdapter.onDestroy();
            Log.w(LOG_TAG, "onDestroy");
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            BoundFusedLocationClient.destroyListener();
            destroyRecognitionListener();
            if ((detectorHandle0 != null) && !detectorHandle0.isDone())
                detectorHandle0.cancel(true);
            if ((sdtHandle != null) && !sdtHandle.isDone()) sdtHandle.cancel(true);
            scheduler.shutdown();
            mSensorThread.quitSafely();
            mWearHandlerThread.quitSafely();
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "onDestroy " + e.getMessage());
        }
        super.onDestroy();
    }

    // wearable interactions
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
        String messagePath = messageEvent.getPath();
        byte[] data = messageEvent.getData();
        String host = messageEvent.getSourceNodeId();
        Log.i(LOG_TAG,"onMessageReceived() A message from app was received:"
                + messageEvent.getRequestId()
                + " " + host + " "
                + messageEvent.getPath());
        // Check to see if the message is to start an activity
        boolean bPreviously = mMessagesViewModel.hasPhone();
        String currentUserID = mSavedStateViewModel.getUserIDLive().getValue();
        String currentDeviceID = mSavedStateViewModel.getDeviceID();
        if (((currentUserID == null) || (currentUserID.length() == 0)) || (currentDeviceID.length() == 0)) return;
        if (userPrefs == null || !userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) return;
        if (messagePath.equals(Constants.DATA_START_ACTIVITY)) {
            if (!bPreviously)
                mMessagesViewModel.setPhoneAvailable(true);
            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
            //ActivityCompat.startActivity(getApplicationContext(),startIntent,null);
        }
        if (messagePath.equals(Constants.DATA_START_WORKOUT_SET)
                || messagePath.equals(Constants.DATA_STOP_WORKOUT_SET)) {
        }
        if (messagePath.equals(Constants.MESSAGE_PATH_PHONE)) {
            DataMap dataMap = DataMap.fromByteArray(data);
            int requestType = dataMap.getInt(Constants.KEY_COMM_TYPE);
            String sUserID = dataMap.getString(Constants.KEY_FIT_USER);
            if ((sUserID == null) || (sUserID.length() == 0)) return;
            if (!sUserID.equals(currentUserID)) return;
            if (!bPreviously)
                mMessagesViewModel.setPhoneAvailable(true);
            if (requestType == Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION) {
                //     promptUserForSensorPermission();
            }
            if (requestType == Constants.COMM_TYPE_REQUEST_INFO){
                int type = dataMap.getInt(Constants.KEY_FIT_TYPE,0);
                // handshake request info reply from service
                if (type == 0){
                    long lastLogin = dataMap.getLong(KEY_FIT_VALUE);
                    String sDevice = dataMap.getString(KEY_FIT_DEVICE_ID);
                    long lastSync = dataMap.getLong(Constants.KEY_FIT_TIME);
                    String sMsg = "HANDSHAKE Last login " + Utilities.getTimeDateString(lastLogin);
                    if (lastSync > 0) sMsg += "\n Last Sync"  + Utilities.getTimeDateString(lastSync);
                    sMsg += "\n Device "  + sDevice;
                    //broadcastToast(sMsg);
                    Log.e(LOG_TAG, sMsg);
                    SensorDailyTotals sdt = mSessionViewModel.getTopSensorDailyTotal(sUserID);
                    UserDailyTotals udt = mSessionViewModel.getTopUserDailyTotal(sUserID);
                    new TaskSendSensoryInfo(getApplicationContext(), host,currentUserID, currentDeviceID, appPrefs.getLastUserLogIn(),
                            mSavedStateViewModel.getState(),sdt,udt).run();
                    startWearPendingSyncAlarm();
                }
                if (type == 3) {
                    SensorDailyTotals sdt = mSessionViewModel.getTopSensorDailyTotal(currentUserID);
                    UserDailyTotals udt = mSessionViewModel.getTopUserDailyTotal(currentUserID);
                    new TaskSendSensoryInfo(getApplicationContext(), wearNodeId,currentUserID, currentDeviceID, appPrefs.getLastUserLogIn(),
                            mSavedStateViewModel.getState(),sdt,udt).run();
                }
                if (type == 2) { // response from 1 request
                    long lastLogin = dataMap.getLong(KEY_FIT_VALUE);
                    String sDevice = dataMap.getString(KEY_FIT_DEVICE_ID);
                    long lastSync = dataMap.getLong(Constants.KEY_FIT_TIME);
                    String sMsg = "Last login " + Utilities.getTimeDateString(lastLogin);
                    if (lastSync > 0) sMsg += "\n Last Sync"  + Utilities.getTimeDateString(lastSync);
                    broadcastToast(sMsg);
                }
            }
            if (requestType == Constants.COMM_TYPE_DAILY_UPDATE){
                long previousTime = dataMap.getLong(KEY_FIT_VALUE);
                int currentState = dataMap.getInt(KEY_FIT_ACTION);
                boolean updatedSDT = false; boolean updatedUDT = false;
                String sVal = null;
                SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
                UserDailyTotals udt = mSessionViewModel.getTopUserDailyTotal(sUserID);
                if (dataMap.containsKey(DataType.TYPE_STEP_COUNT_DELTA.getName())){
                    int iVal = dataMap.getInt(DataType.TYPE_STEP_COUNT_DELTA.getName());
                    sVal = String.format(Locale.getDefault(),SINGLE_INT, iVal);
                    if (iVal >= 0){
                        if (appPrefs.getStepsSensorCount() == 0) {
                            DailyCounter stepCounter = mSavedStateViewModel.getSteps();
                            if (stepCounter != null) {
                                long lTemp = Long.parseLong(sVal);
                                if (lTemp > 0) {
                                    stepCounter.LastCount = lTemp;
                                    stepCounter.LastUpdated = System.currentTimeMillis();
                                    // if have a goal and it is active
                                    if ((stepCounter.GoalCount > 0) && (stepCounter.GoalActive > 0))
                                        if ((stepCounter.LastCount - stepCounter.FirstCount) >= stepCounter.GoalCount){
                                            // sendNotification Step Goal Attached !
                                            Bundle resultData = new Bundle();
                                            resultData.putLong(KEY_FIT_WORKOUTID,mWorkout._id);
                                            resultData.putInt(KEY_FIT_TYPE, GOAL_TYPE_STEPS);
                                            resultData.putLong(KEY_FIT_ACTION, stepCounter.GoalCount);
                                            resultData.putLong(Constants.KEY_FIT_VALUE, stepCounter.LastCount);
                                            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                                                    && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
                                            if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                                                    && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_VIBRATE)) vibrate(3);
                                            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                            stepCounter.GoalActive = 0;
                                        }
                                    mSavedStateViewModel.setSteps(stepCounter);

                                }
                            }
                        }
                        appPrefs.setSteps2SensorCount(1);
                        mMessagesViewModel.addDevice2StepsMsg(sVal);
                        if (sdt != null){
                            updatedSDT = true;
                            sdt.device2Step = iVal;
                            sdt.lastDevice2Step = previousTime;
                        }
                    }
                }
                if (dataMap.containsKey(DataType.TYPE_HEART_RATE_BPM.getName())){
                    float fVal = dataMap.getFloat(DataType.TYPE_HEART_RATE_BPM.getName());
                    sVal = String.format(Locale.getDefault(),SINGLE_INT, fVal);
                    if (fVal >= 0) {
                        if (appPrefs.getBPMSensorCount() == 0) {
                            DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                            if (bpmCounter != null) {
                                long lTemp = (int) Float.parseFloat(sVal);
                                if (lTemp > 0) {
                                    bpmCounter.LastCount = lTemp;
                                    bpmCounter.LastUpdated = System.currentTimeMillis();
                                    mSavedStateViewModel.setBPM(bpmCounter);
                                }
                            }
                        }
                        appPrefs.setBPM2SensorCount(1);
                        mMessagesViewModel.addDevice2BpmMsg(sVal);
                        if (sdt != null){
                            sdt.device2BPM = fVal;
                            sdt.lastDevice2BPM = previousTime;
                            updatedSDT = true;
                        }
                    }
                }
                if (dataMap.containsKey(Sensor.STRING_TYPE_PRESSURE)){
                    float fVal = dataMap.getFloat(Sensor.STRING_TYPE_PRESSURE);
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        appPrefs.setPressure2SensorCount(1);
                        mMessagesViewModel.addPressure2Msg(sVal);
                        if (sdt != null) {
                            sdt.pressure2 = fVal;
                            sdt.lastDevice2Other = previousTime;
                            updatedSDT = true;
                        }
                    }
                }
                if (dataMap.containsKey(Sensor.STRING_TYPE_RELATIVE_HUMIDITY)){
                    float fVal = dataMap.getFloat(Sensor.STRING_TYPE_RELATIVE_HUMIDITY);
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        if (sdt != null) {
                            sdt.humidity2 = fVal;
                            sdt.lastDevice2Other = previousTime;
                            updatedSDT = true;
                        }
                        appPrefs.setHumidity2SensorCount(1);
                        mMessagesViewModel.addHumidity2Msg(sVal);
                    }
                }
                if (dataMap.containsKey(Sensor.STRING_TYPE_AMBIENT_TEMPERATURE)){
                    float fVal = dataMap.getFloat(Sensor.STRING_TYPE_AMBIENT_TEMPERATURE);
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        if (sdt != null) {
                            sdt.temperature2 = fVal;
                            sdt.lastDevice2Other = previousTime;
                            updatedSDT = true;
                        }
                        appPrefs.setTemp2SensorCount(1);
                        mMessagesViewModel.addTemperature2Msg(sVal);
                    }
                }
                if (dataMap.containsKey(Constants.INTENT_RECOG)){
                    sVal = dataMap.getString(KEY_FIT_NAME);
                    int detectedID = -3;
                    long lastType = dataMap.getLong(Constants.INTENT_RECOG, -1);
                    if (dataMap.containsKey(KEY_FIT_TYPE)){
                        detectedID = dataMap.getInt(KEY_FIT_TYPE, -3);
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + detectedID + " " + sVal + " " + Utilities.getTimeString(lastType));
                    }else
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + sVal);
                    if (sVal.length() > 0){
                         mMessagesViewModel.addActivityMsg(sVal);
                         if (sdt != null) {
                             sVal += ATRACKIT_SPACE;
                             if (detectedID == -3) {
                                 switch (sVal) {
                                     case Constants.RECOG_VEHCL:
                                         detectedID = DetectedActivity.IN_VEHICLE;
                                         break;
                                     case RECOG_BIKE:
                                         detectedID = DetectedActivity.ON_BICYCLE;
                                         break;
                                     case RECOG_FOOT:
                                         detectedID = DetectedActivity.ON_FOOT;
                                         break;
                                     case RECOG_RUN:
                                         detectedID = DetectedActivity.RUNNING;
                                         break;
                                     case RECOG_STILL:
                                         detectedID = DetectedActivity.STILL;
                                         break;
                                     case RECOG_TILT:
                                         detectedID = DetectedActivity.TILTING;
                                         break;
                                     case RECOG_WALK:
                                         detectedID = DetectedActivity.WALKING;
                                         break;
                                     case RECOG_UNKWN:
                                         detectedID = DetectedActivity.UNKNOWN;
                                         break;
                                 }
                             }
                             if (detectedID != -3) {
                                 sdt.activityType = detectedID;
                                 sdt.lastActivityType = (lastType == -1) ? System.currentTimeMillis() : lastType;
                                 updatedSDT = true;
                             }
                         }
                    }
                }
                // now the UDT values if needed.
                if (dataMap.containsKey(DataType.TYPE_HEART_POINTS.getName())){
                    float fVal = dataMap.getFloat(DataType.TYPE_HEART_POINTS.getName());
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        if (udt != null) {
                            if (udt.heartIntensity != fVal) {
                                udt.heartIntensity = fVal;
                                mMessagesViewModel.addHeartPtsMsg(String.format(Locale.getDefault(), SINGLE_INT, Math.round(fVal)));
                                updatedUDT = true;
                            }
                        }
                    }
                }
                if (dataMap.containsKey(DataType.TYPE_DISTANCE_DELTA.getName())){
                    float fVal = dataMap.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        if (udt != null) {
                            if (udt.distanceTravelled != fVal) {
                                udt.distanceTravelled = fVal;
                                mMessagesViewModel.addDistanceMsg(sVal);
                                updatedUDT = true;
                            }
                        }
                    }
                }
                if (dataMap.containsKey(DataType.TYPE_CALORIES_EXPENDED.getName())){
                    float fVal = dataMap.getFloat(DataType.TYPE_CALORIES_EXPENDED.getName());
                    if (fVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
                        if (udt != null) {
                            if (udt.caloriesExpended != fVal) {
                                udt.caloriesExpended = fVal;
                                mMessagesViewModel.addCaloriesMsg(sVal);
                                updatedUDT = true;
                            }
                        }
                    }
                }
                if (dataMap.containsKey(DataType.TYPE_MOVE_MINUTES.getName())){
                    int iVal = dataMap.getInt(DataType.TYPE_MOVE_MINUTES.getName());
                    if (iVal >= 0){
                        sVal = String.format(Locale.getDefault(),SINGLE_INT, iVal);
                        if (udt != null) {
                            if (udt.activeMinutes != iVal) {
                                udt.activeMinutes = iVal;
                                mMessagesViewModel.addMoveMinsMsg(sVal);
                                updatedUDT = true;
                            }
                        }
                    }
                }
                if (dataMap.containsKey(DataType.TYPE_LOCATION_SAMPLE.getName())){
                    sVal = dataMap.getString(DataType.TYPE_LOCATION_SAMPLE.getName());
                    if (sVal.length() > 0){
                        if (udt != null) {
                            if (!udt.lastLocation.equals(sVal)) {
                                udt.lastLocation = sVal;
                                mSavedStateViewModel.addLocationMsg(sVal);
                                updatedUDT = true;
                            }
                        }
                    }
                }
                if (updatedSDT) mSavedStateViewModel.setSDT(sdt);
                if (updatedUDT) mSessionViewModel.updateUserDailyTotals(udt);
            }
        }
    }
    //
    // custom confirm dialog interface callback
    //
    @Override
    public void onCustomConfirmDetach() {
        mSavedStateViewModel.setIsInProgress(0);
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        if (question == Constants.QUESTION_AGE) {
            if (button > 0) {
                String sAge = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                userPrefs.setAskAge(true);
                if ((sAge.length() > 0) && TextUtils.isDigitsOnly(sAge)){
                    appPrefs.setUseLocation((Integer.parseInt(sAge) > 9));
                }
                return;
            }
        }
        if (question == Constants.QUESTION_HEIGHT){
            if (button > 0){
                Log.w(LOG_TAG, "Height Question Positive");
                String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.KEY_FIT_USER, sUserId); // userID
                Constraints constraints = new Constraints.Builder().build();
                OneTimeWorkRequest oneTimeWorkRequest =
                        new OneTimeWorkRequest.Builder(HeightBodypartWorker.class)
                                .setInputData(builder.build())
                                .setConstraints(constraints)
                                .build();
                if (mWorkManager == null) mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(oneTimeWorkRequest).getState().observe(MainActivity.this, state -> {
                    if (state instanceof Operation.State.SUCCESS){

                    }
                });
            }
            return;
        }
        int state = mSavedStateViewModel.getState();
        boolean bExternal = mWorkout != null && (!mSavedStateViewModel.getDeviceID().equals(mWorkout.deviceID));
        if (button > 0) {   // positive btnConfirmBodypart
            switch (question) {
                case Constants.ACTION_STARTING:
                 //   if (!bExternal) {
                        Intent startIntent = new Intent(INTENT_ACTIVE_START);
                        startIntent.putExtra(KEY_FIT_TYPE, 0);
                        if (mWorkout != null) {
                            startIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            startIntent.putExtra(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                            startIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        if (mWorkoutSet != null)
                            startIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        startIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(startIntent);
                    /*}else{
                        DataMap dataMap = new DataMap();
                        dataMap.putString(KEY_FIT_USER, mWorkout.userID);
                        dataMap.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
                        if (wearNodeId.length() != 0)
                            sendMessage(wearNodeId,Constants.DATA_START_WORKOUT,dataMap);
                        else
                            sendCapabilityMessage(WEAR_CAPABILITY_NAME,Constants.DATA_START_WORKOUT,dataMap);
                    }*/
                    break;
                case Constants.ACTION_STOPPING:
                    if (!bExternal) {
                        Intent stopIntent = new Intent(INTENT_ACTIVE_STOP);
                        stopIntent.putExtra(KEY_FIT_TYPE, 0);
                        if (mWorkout != null) {
                            stopIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        if (mWorkoutSet != null)
                            stopIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        stopIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(stopIntent);
                    }else if (mWorkout != null){
                        DataMap dataMap = new DataMap();
                        dataMap.putString(KEY_FIT_USER, mWorkout.userID);
                        dataMap.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
                        if (wearNodeId.length() != 0)
                            sendMessage(wearNodeId, DATA_STOP_WORKOUT_SET,dataMap);
                        else
                            sendCapabilityMessage(WEAR_CAPABILITY_NAME, DATA_STOP_WORKOUT_SET,dataMap);                      }
                    break;
                case Constants.ACTION_QUICK_STOP:
                    Intent stopQuickIntent = new Intent(INTENT_ACTIVE_STOP);
                    stopQuickIntent.putExtra(KEY_FIT_TYPE, 0);
                    if (mWorkout != null) {
                        stopQuickIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        stopQuickIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        stopQuickIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);

                    stopQuickIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.KEY_FIT_ACTION_QUICK);
                    stopQuickIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(stopQuickIntent);
                    break;
                case Constants.ACTION_SIGNOUT_QUIT:
                    signOut();
                    break;
                case Constants.QUESTION_HISTORY_LOAD:
                    final String sUserID = (mGoogleAccount != null) ? mGoogleAccount.getId() : mSavedStateViewModel.getUserIDLive().getValue();
                    final String sDeviceID = mSavedStateViewModel.getDeviceID();
                    final long timeMs = System.currentTimeMillis();
                    if (hasOAuthPermission(0)) {
                        if (hasOAuthPermission(5))
                            userPrefs.setReadSessionsPermissions(true);
                        else {
                            requestOAuthPermission(5);
                            return;
                        }
                    } else {
                        requestOAuthPermission(0);
                        return;
                    }
                    Configuration configHistory = null;
                    List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUserID);
                    Long lStart = System.currentTimeMillis();
                    if (existingConfigs.size() > 0){
                        configHistory = existingConfigs.get(0);
                        if (Long.parseLong(configHistory.stringValue1) > 0) lStart = Long.parseLong(configHistory.stringValue1);
                    }
                    mCalendar.setTimeInMillis(lStart);
                    int year = mCalendar.get(Calendar.YEAR);
                    int month = mCalendar.get(Calendar.MONTH);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog mTimePicker = new DatePickerDialog(MainActivity.this,R.style.MyDatePickerStyle,(view, selectedYear, monthOfYear, dayOfMonth) -> {
                        createWorkout(sUserID,sDeviceID);
                        mCalendar.clear();
                        mCalendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                        mCalendar.set(Calendar.SECOND,0); mCalendar.set(Calendar.MILLISECOND,0);
                        mWorkout.start = mCalendar.getTimeInMillis();
                        mWorkout.end = System.currentTimeMillis();
                        List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE,sUserID);
                        Configuration configHistory2;
                        if ((configList != null) && (configList.size() > 0)) {
                            configHistory2 = configList.get(0);
                            configHistory2.stringValue1 = Long.toString(mWorkout.start);
                            configHistory2.stringValue2 = "0";
                            configHistory2.longValue = 0;
                            mSessionViewModel.updateConfig(configHistory2);
                        }else{
                            configHistory2 = new Configuration(Constants.MAP_HISTORY_RANGE, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, Long.toString(mWorkout.start), "0");
                            mSessionViewModel.insertConfig(configHistory2);
                        }

                        doAsyncGoogleFitAction(TASK_ACTION_SYNC_WORKOUT,mWorkout,mWorkoutSet);
                    },year,month,day);
                    mTimePicker.setTitle("Select Start Date");
                    mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (mMessagesViewModel.isWorkInProgress()) mMessagesViewModel.setWorkInProgress(false);
                        }
                    });
                    mTimePicker.show();
                    break;
                case Constants.QUESTION_PAUSESTOP:
                case ACTION_PAUSING:
                    Intent pauseIntent = new Intent(INTENT_ACTIVE_PAUSE);
                    pauseIntent.putExtra(KEY_FIT_TYPE, 0);
                    if (mWorkout != null) {
                        pauseIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        pauseIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        pauseIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    pauseIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(pauseIntent);
                    break;                    
                case Constants.ACTION_RESUMING:
                    Intent resumeIntent = new Intent(INTENT_ACTIVE_RESUMED);
                    resumeIntent.putExtra(KEY_FIT_TYPE, 0);
                    if (mWorkout != null) {
                        resumeIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        resumeIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        resumeIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    resumeIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(resumeIntent);
                    break;
                case Constants.ACTION_CANCELLING:
//                    Intent cancelIntent = new Intent(INTENT_C);
//                    cancelIntent.setAction(INTENT_ACTIVE_RESUMED);
//                    cancelIntent.putExtra(KEY_FIT_TYPE, 0);
//                    cancelIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
//                    mMessagesViewModel.addLiveIntent(cancelIntent);
                    break;
                case Constants.ACTION_EXITING:
                case Constants.ACTION_STOP_QUIT:
                    if ((state != WORKOUT_LIVE) && (state != WORKOUT_PAUSED))
                        quitApp();
                    else {
                        Intent stopQuitIntent = new Intent(INTENT_ACTIVE_STOP);
                        stopQuitIntent.putExtra(KEY_FIT_TYPE, 0);
                        if (mWorkout != null) {
                            stopQuitIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            stopQuitIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        if (question == Constants.ACTION_STOP_QUIT)
                            stopQuitIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.KEY_FIT_ACTION_QUIT);
                        stopQuitIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(stopQuitIntent);
                    }
                    break;
                case Constants.ACTION_REPEAT_SET:
                case Constants.ACTION_START_SET:
                    if (!bExternal) {
                        Intent start_setIntent = new Intent(INTENT_ACTIVESET_START);
                        start_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
                        if (question == Constants.ACTION_REPEAT_SET) {
                            start_setIntent.putExtra(KEY_FIT_ACTION, 1); /* repeat flag*/
                            start_setIntent.putExtra(KEY_FIT_VALUE, 0);
                        } else
                            start_setIntent.putExtra(KEY_FIT_VALUE, (mWorkoutSet.end == 0) ? 0 : 1);
                        if (mWorkout != null) {
                            start_setIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            start_setIntent.putExtra(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                            start_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        if (mWorkoutSet != null)
                            start_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        start_setIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(start_setIntent);
                    }else if(mWorkout != null){
                        DataMap dataMap = new DataMap();
                        dataMap.putString(KEY_FIT_USER, mWorkout.userID);
                        dataMap.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        dataMap.putLong(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
                        if (wearNodeId.length() != 0)
                            sendMessage(wearNodeId,Constants.DATA_START_WORKOUT_SET,dataMap);
                        else
                            sendCapabilityMessage(WEAR_CAPABILITY_NAME,Constants.DATA_START_WORKOUT_SET,dataMap);
                    }
                    break;

                case ACTION_END_SET:
                    Intent end_setIntent = new Intent(INTENT_ACTIVESET_STOP);
                    end_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
                    if (mWorkout != null) {
                        end_setIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        end_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        end_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    end_setIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(end_setIntent);
                    break;
                case Constants.QUESTION_RESUME_END:
                    Intent resumeQIntent = new Intent(INTENT_ACTIVE_RESUMED);
                    if (mWorkout != null) {
                        resumeQIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        resumeQIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        resumeQIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    resumeQIntent.putExtra(KEY_FIT_TYPE, 0);
                    resumeQIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(resumeQIntent);
                    break;
                case Constants.QUESTION_DURATION_DELETE:
                    // start the duration selector
                   // startCustomList(SELECTION_INCOMPLETE_DURATION, "");
                    break;
                case Constants.QUESTION_LOCATION:
                    //locationPermissionResultLauncher
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                    break;
                case Constants.QUESTION_SENSORS:
                    sensorsPermissionResultLauncher.launch(Manifest.permission.BODY_SENSORS);
/*
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            PERMISSION_REQUEST_BODY_SENSORS);
*/
                    break;
                case Constants.QUESTION_ACT_RECOG:
                    recognitionPermissionResultLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
/*
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            PERMISSION_REQUEST_ACTIVITY_RECOG);
*/
                    break;
                case Constants.QUESTION_POLICY:
                    broadcastToast("Thanks - setup will continue");
                    break;
                case Constants.QUESTION_NETWORK:
                    broadcastToast(getString(R.string.no_network_connection));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            quitApp();
                        }
                    },2000);
                    break;
                case Constants.QUESTION_DAILY_REFRESH:
                    doDailySummaryJob();
                    break;
            }
        }
        else{  // negative btnConfirmBodypart
            if (question == Constants.QUESTION_DEVICE){
                if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), appPrefs.getLastUserID());
                userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_ASKED, true);
                userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                mMessagesViewModel.setPhoneAvailable(false);
            }
            if (question == Constants.QUESTION_PAUSESTOP) {
                Intent stopIntent = new Intent(INTENT_ACTIVE_STOP);
                stopIntent.putExtra(KEY_FIT_TYPE, 0);
                if (mWorkout != null) {
                    stopIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                }
                if (mWorkoutSet != null) stopIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                stopIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(stopIntent);
            }
            if (question == Constants.QUESTION_RESUME_END){
                Intent stopIntent = new Intent(INTENT_ACTIVE_STOP);
                stopIntent.putExtra(KEY_FIT_TYPE, 0);
                if (mWorkout != null) {
                    stopIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                }
                if (mWorkoutSet != null) stopIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                stopIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(stopIntent);
            }
            if (question == Constants.QUESTION_DURATION_DELETE){
                Intent deleteIntent = new Intent(INTENT_WORKOUT_DELETE);
                deleteIntent.putExtra(KEY_FIT_TYPE, 0);
                if (mWorkout != null) {
                    deleteIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    deleteIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                }
                deleteIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(deleteIntent);
            }
            if (question == Constants.QUESTION_POLICY) {
                broadcastToast("setup will continue");
            }
        }
    }

    @Override
    public void onCustomScoreSelected(int type, long position, String title, int iSetIndex) {
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (type == Constants.SELECTION_SETS){
            if (mWorkoutSet != null) {
                mWorkoutSet.setCount = (int)position;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            return;
        }
        if (type == Constants.SELECTION_REPS){
            if (mWorkoutSet != null){
                mWorkoutSet.repCount = (int)position;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            return;
        }
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if (type == Constants.SELECTION_GOAL_DURATION){
            if (mWorkout != null){
                if (mWorkout.activityID > 0){
                    String sLabel = getString(R.string.default_act_dur) + Long.toString(mWorkout.activityID);
                    userPrefs.setPrefStringByLabel(sLabel, Long.toString(position));
                }
                mWorkout.goal_duration = position;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkout(mWorkout);
            }
            return;
        }
        if (type == Constants.SELECTION_GOAL_STEPS){
            if (mWorkout != null){
                if (mWorkout.activityID > 0){
                    String sLabel = getString(R.string.default_act_steps) + Long.toString(mWorkout.activityID);
                   userPrefs.setPrefStringByLabel( sLabel, Long.toString(position));
                }
                mWorkout.goal_steps = position;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkout(mWorkout);
            }
        }
    }

    /**
     * onCustomItemSelected - Fragment callbacks for @link CustomListFragment lists, scores and generic
     *
     */
    @Override
    public void onCustomItemSelected(int type, long id, String title, int resid, String identifier) {
        String sLabel = getString(R.string.default_loadtype) + Integer.toString(type);
        String defaultValue = ATRACKIT_EMPTY;
      //  String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        String sTarget = Long.toString(id);
        mReferenceTools.init(getApplicationContext());
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        // don't save defaults for these selection types
        if ((type != Constants.SELECTION_WORKOUT_INPROGRESS) && (type != SELECTION_WORKOUT_HISTORY)){
            userPrefs.setPrefStringByLabel(sLabel,sTarget);
        }
        if ((type == Constants.SELECTION_FITNESS_ACTIVITY) || (type == Constants.SELECTION_ACTIVITY_BIKE) || (type == Constants.WORKOUT_TYPE_ARCHERY)
                || (type == SELECTION_ACTIVITY_GYM) || (type == Constants.SELECTION_ACTIVITY_CARDIO)
                || (type == Constants.SELECTION_ACTIVITY_SPORT) || (type == Constants.SELECTION_ACTIVITY_RUN)
                || (type == Constants.SELECTION_ACTIVITY_WATER) || (type == Constants.SELECTION_ACTIVITY_WINTER)
                || (type == Constants.SELECTION_ACTIVITY_MISC)) {
            mWorkout.activityID = id;
            mWorkout._id = 2;
            mWorkout.activityName = title;
            mWorkout.identifier = identifier;
            if (mWorkoutSet == null) createWorkoutSet();
            mWorkoutSet.activityID = mWorkout.activityID;
            mWorkoutSet.activityName = mWorkout.activityName;

            Boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
            mSavedStateViewModel.setSetIsGym(bGym);
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            int activityIcon = (resid > 0) ? resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(activityIcon);
            int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
            if (bGym){
                mWorkout._id = 1L;
                mWorkoutSet.workoutID = 1L;
                mWorkout.scoreTotal = (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING) ? FLAG_BUILDING : FLAG_NON_TRACKING); // build stage
                mWorkoutSet.scoreTotal = (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING) ? FLAG_BUILDING : FLAG_NON_TRACKING); // build stage
/*                String sSetting = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                long heightAsked = userPrefs.getLongPrefByLabel(Constants.AP_PREF_HEIGHT_ASKED);
                if (sSetting.length() == 0 && (!userPrefs.getAskAge())) {
                    startSplashActivityForResult(Constants.INTENT_PERMISSION_HEIGHT);
                    return;
                }*/
                int iRest = (userPrefs.getTimedRest() ? userPrefs.getWeightsRestDuration() : 0);
                mWorkoutSet.rest_duration = TimeUnit.SECONDS.toMillis(iRest);
                if ((mWorkout._id == 1)||(mWorkout._id == 2)) {
                    String sUserId = mSavedStateViewModel.getUserIDLive().getValue();
                    DateTuple setTuple = mSessionViewModel.getDraftWorkoutSetTuple(sUserId);
                    if (Utilities.isGymWorkout(mWorkout.activityID) && (setTuple.sync_count > 1)) {
                        ICustomConfirmDialog callback = new ICustomConfirmDialog() {
                            @Override
                            public void onCustomConfirmButtonClicked(int question, int button) {
                                if (button <= 0) {
                                    mSessionViewModel.deleteWorkoutSetByWorkoutID(1);
                                }
                                mSavedStateViewModel.setSetIndex(1);
                                mSavedStateViewModel.setColorID(activityColor);
                                mSavedStateViewModel.setActiveWorkout(mWorkout);
                                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                                sessionSetCurrentState(WORKOUT_PENDING);
                                Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                                mMessagesViewModel.addLiveIntent(editIntent);
                            }

                            @Override
                            public void onCustomConfirmDetach() {

                            }
                        };
                        doConfirmDialog(Constants.QUESTION_KEEP_DELETE, getString(R.string.label_keep_draft_sets), callback);
                        return;
                    }
                    else
                        mSessionViewModel.deleteWorkoutSetByWorkoutID(mWorkout._id);
                }
            }else{
                if (Utilities.isShooting(mWorkout.activityID)) {
                    long lRest = userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_CALL_DURATION);
                    mWorkoutSet.call_duration = TimeUnit.SECONDS.toMillis(lRest);
                    lRest = userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_END_DURATION);
                    mWorkoutSet.goal_duration= TimeUnit.SECONDS.toMillis(lRest);
                    if (mWorkoutMeta == null) createWorkoutMeta();
                    mWorkoutMeta.rest_duration = mWorkoutSet.rest_duration;
                    mWorkoutMeta.call_duration = mWorkoutSet.call_duration;
                    mWorkoutMeta.goal_duration = mWorkoutSet.goal_duration;
                }
                mWorkoutSet.scoreTotal = FLAG_PENDING;
                mWorkout.scoreTotal = FLAG_PENDING;
            }
            sLabel = getString(R.string.default_act_steps) + Long.toString(mWorkout.activityID);
            defaultValue = userPrefs.getPrefStringByLabel(sLabel);
            if (defaultValue.length() > 0)
                mWorkout.goal_steps = Long.parseLong(defaultValue);

            sLabel = getString(R.string.default_act_dur) + Long.toString(mWorkout.activityID);
            defaultValue = userPrefs.getPrefStringByLabel(sLabel);
            if (defaultValue.length() > 0) mWorkout.goal_duration = Long.parseLong(defaultValue);
            mSavedStateViewModel.setSetIndex(1);
            mSavedStateViewModel.setColorID(activityColor);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            sessionSetCurrentState(WORKOUT_PENDING);
            Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
            editIntent.putExtra(Workout.class.getSimpleName(), mWorkout);
            editIntent.putExtra(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            if (mWorkoutMeta == null) createWorkoutMeta();
            editIntent.putExtra(WorkoutMeta.class.getSimpleName(), mWorkoutMeta);
            mMessagesViewModel.addLiveIntent(editIntent);
        }

        if (type == Constants.SELECTION_BODYPART) {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.bodypartID = id;
                mWorkoutSet.bodypartName = title;
                mSavedStateViewModel.setDirtyCount(1);
                int defaultInt;
                Bodypart bodypart = (((mWorkoutSet.bodypartID != null) && (mWorkoutSet.bodypartID > 0)) ? mSessionViewModel.getBodypartById(mWorkoutSet.bodypartID) : null);
                if (bodypart != null) {
                    //if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                    defaultInt = userPrefs.getDefaultNewReps();
                    mWorkoutSet.repCount = defaultInt;
                    mSavedStateViewModel.setRepsDefault(defaultInt);
                    defaultInt = userPrefs.getDefaultNewSets();
                    mSavedStateViewModel.setSetsDefault(defaultInt);
                 //   Float defaultFloat = (bodypart.lastWeight > 0) ? bodypart.lastWeight : 0;
                 //   mWorkoutSet.weightTotal = defaultFloat;
                    mWorkoutSet.regionID = bodypart.regionID;
                    mWorkoutSet.regionName = bodypart.regionName;
                }
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            userPrefs.setLastBodyPartID(Long.toString(id));
            userPrefs.setLastBodyPartName(title);
        }
        if (type == Constants.SELECTION_BODYPART_AGG){
            broadcastToast("bodypart selected");
        }
        if (type == Constants.SELECTION_EXERCISE_AGG){
            broadcastToast("exercise selected");
            Exercise exercise = mSessionViewModel.getExerciseById(id);
            if ((exercise != null) && (resid > 0))
                startExerciseActivityForResult(exercise, 1);
            else
                startExerciseActivityForResult(exercise, 0);
            return;
        }
        if (type == Constants.SELECTION_EXERCISE) {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mWorkoutSet.exerciseID = id;
            mWorkoutSet.exerciseName = title;
            mSavedStateViewModel.setDirtyCount(1);
            Exercise exercise = mSessionViewModel.getExerciseById(id);
            if (exercise != null) {
                if (exercise.resistanceType != null)
                    mWorkoutSet.resistance_type = exercise.resistanceType;
                mWorkoutSet.per_end_xy = exercise.workoutExercise;
                if (exercise.lastReps > 0)
                    mSavedStateViewModel.setRepsDefault(exercise.lastReps);
                else
                    mSavedStateViewModel.setRepsDefault(userPrefs.getDefaultNewReps());
                if (exercise.lastSets > 0)
                    mSavedStateViewModel.setSetsDefault(exercise.lastSets);
                else
                    mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
                if (exercise.lastSelectedWeight > 0) mWorkoutSet.weightTotal = exercise.lastSelectedWeight;
                else
                    if (exercise.lastAvgWeight > 0)
                        mWorkoutSet.weightTotal =  (float)Math.floor(exercise.lastAvgWeight);


                if (((mWorkoutSet.weightTotal != null) && (mWorkoutSet.repCount != null)) && ((mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount > 0))) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                if (exercise.first_BPID != null && exercise.first_BPID > 0) {
                    mWorkoutSet.bodypartID = exercise.first_BPID;
                    mWorkoutSet.bodypartName = exercise.first_BPName;
                    Bodypart part = mSessionViewModel.getBodypartById(exercise.first_BPID);
                    if (part != null) {
                        mWorkoutSet.regionID = part.regionID;
                        mWorkoutSet.regionName = part.regionName;
                    }
                }
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_SETS){
            mSavedStateViewModel.setSetsDefault((int) id);
        }
        if (type == Constants.SELECTION_REPS){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mSavedStateViewModel.setRepsDefault((int) id);
            if (mWorkoutSet != null) {
                mWorkoutSet.repCount = (int) id;
                if ((mWorkoutSet.weightTotal != null) && ((mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount > 0))) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }

        if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET)
                ||(type == Constants.SELECTION_CALL_DURATION)||(type == Constants.SELECTION_END_DURATION)){
            if (!TextUtils.isDigitsOnly(title)) return;
            int seconds = Integer.parseInt(title);
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (type == Constants.SELECTION_REST_DURATION_GYM) userPrefs.setWeightsRestDuration(seconds);
            if (type == Constants.SELECTION_REST_DURATION_TARGET) userPrefs.setArcheryRestDuration(seconds);
            if (type == Constants.SELECTION_CALL_DURATION) userPrefs.setArcheryCallDuration(seconds);
            if (type == Constants.SELECTION_END_DURATION) userPrefs.setArcheryEndDuration(seconds);
            if (mWorkoutSet != null) {
                if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET)) mWorkoutSet.rest_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_CALL_DURATION)) mWorkoutSet.call_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_END_DURATION)) mWorkoutSet.goal_duration = TimeUnit.SECONDS.toMillis(seconds);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            if (mWorkoutMeta != null) {
                if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET)) mWorkoutMeta.rest_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_CALL_DURATION)) mWorkoutMeta.call_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_END_DURATION)) mWorkoutMeta.goal_duration = TimeUnit.SECONDS.toMillis(seconds);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            }
        }
        if ((type == Constants.SELECTION_WEIGHT_KG)){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.weightTotal = Float.parseFloat(title);
                if ((mWorkoutSet.repCount != null) && (mWorkoutSet.repCount > 0)) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                    /*if ( mSavedStateViewModel.getToDoSetsSize() > 1) {
                        List<WorkoutSet> sets = ( mSavedStateViewModel.getToDoSets().getValue() != null) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                        boolean updated = false;
                        if (mWorkoutSet.exerciseID > 0)
                            for (WorkoutSet set : sets) {
                                if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID == mWorkoutSet.exerciseID) && (set.start == 0)) {
                                    set.weightTotal = mWorkoutSet.weightTotal;
                                    updated = true;
                                }
                            }
                        if (updated)
                             mSavedStateViewModel.setToDoSets(sets);
                    }*/
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_WEIGHT_LBS){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.weightTotal = (Float.parseFloat(title) > 0) ? Float.parseFloat(title)/Constants.KG_TO_LBS : 0;  // stored as KG!
                if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                    /*if ( mSavedStateViewModel.getToDoSetsSize() > 1) {
                        List<WorkoutSet> sets = ( mSavedStateViewModel.getToDoSets().getValue() != null) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                        boolean updated = false;
                        if (mWorkoutSet.exerciseID > 0)
                            for (WorkoutSet set : sets) {
                                if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID == mWorkoutSet.exerciseID) && (set.start == 0)) {
                                    set.weightTotal = mWorkoutSet.weightTotal;
                                    updated = true;
                                }
                            }
                        if (updated)
                             mSavedStateViewModel.setToDoSets(sets);
                    }*/
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {
        if ((srcId == Constants.UID_btnSetsPlus)||(srcId == Constants.UID_btnSetsMinus)
        ||(srcId == Constants.UID_btnRepsPlus)||(srcId == Constants.UID_btnRepsMinus))
            Log.i(LOG_TAG,"plus minus btns");
        else
            if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD) return;
        long lastPrevClick = mLastClickTime;
        mLastClickTime = SystemClock.elapsedRealtime();
        final String sUserID = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        final String sDeviceID = mSavedStateViewModel.getDeviceID();
        if (userPrefs != null) bUseKG = userPrefs.getUseKG();
        restoreWorkoutVariables();
        int testState = mSavedStateViewModel.getState();
        int currentState = mSavedStateViewModel.getState();
        switch (srcId){
            case Constants.UID_action_use_location:
               // MenuItem item = get
                appPrefs.setUseLocation(!(selectedId == 1));
                Boolean bUse = appPrefs.getUseLocation();
                mMessagesViewModel.setUseLocation(bUse);
                Drawable dLocation = AppCompatResources.getDrawable(getApplicationContext(),bUse ? R.drawable.ic_location_enabled_white : R.drawable.ic_location_disabled_white);
                MenuItem item = mMenu.findItem(R.id.action_use_location);
                item.setIcon(dLocation);
                item.setChecked(bUse);
                supportInvalidateOptionsMenu();
                if (bUse) {
                    BoundFusedLocationClient.standardInterval();
                    BoundFusedLocationClient.doCurrentUpdate();
                    broadcastToast(getString(R.string.label_location_enabled));
                }
                else {
                    BoundFusedLocationClient.clearInterval();
                    broadcastToast(getString(R.string.label_location_disabled));
                }
                break;
            case Constants.UID_action_settings:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                SettingsDialog settingsDialog = SettingsDialog.newInstance(1, mWearNodesWithApp);
                transaction.add(R.id.top_container, settingsDialog, SettingsDialog.TAG);
                transaction.addToBackStack("settings");
                transaction.commit();
                break;
            case android.R.id.home:
                if (getSupportFragmentManager().findFragmentByTag("SettingsDialog") != null){
                    Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(refreshIntent);
                }
                getSupportFragmentManager().popBackStack();
                break;
            case Constants.UID_action_bpm_permonth:
                if (!hasOAuthPermission(2)){
                    requestOAuthPermission(2);
                    return;
                }
                if (!userPrefs.getAskAge())
                    startSplashActivityForResult(INTENT_PERMISSION_AGE);
                else
                    if (sUserID.length() > 0) startRecentActivity(1,0,mCalendar.getTimeInMillis());
                break;
            case Constants.UID_action_bpm_perhour:
                if (!hasOAuthPermission(0)){
                    requestOAuthPermission(0);
                    return;
                }
                if (!userPrefs.getAskAge())
                    startSplashActivityForResult(INTENT_PERMISSION_AGE);
                else
                    if (sUserID.length() > 0) startRecentActivity(2,0,mCalendar.getTimeInMillis());
                break;
            case Constants.UID_action_open_history:
                if (!hasOAuthPermission(2)){
                    requestOAuthPermission(2);
                    return;
                }
                if (sUserID.length() > 0) startRecentActivity(-2,1,mCalendar.getTimeInMillis());
                break;
            case Constants.UID_action_open_template:
                if (!hasOAuthPermission(1)){
                    requestOAuthPermission(1);
                    return;
                }
                if (sUserID.length() > 0) startRecentActivity(3,1,appPrefs.getHistoryOpen());
                break;
            case Constants.UID_action_phone_camera:
                dispatchTakePictureIntent();
                break;
            case Constants.UID_action_phone_pick:
                try {
                    dispatchPickImageIntent();
                }catch (Exception e){
                    if (e.equals(IOException.class)){
                        broadcastToast(e.getMessage());
                    }
                }
                break;
            case Constants.UID_action_phone_send:
                broadcastToast("Send to wear");
                break;
            case Constants.UID_settings_sign_out_button:
            case Constants.UID_action_signout:
                signOut();
                break;
            case Constants.UID_action_data_policy:
                String url = getString(R.string.a_track_it_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.enter_anim, R.anim.no_anim);
                //ActivityCompat.startActivity(getApplicationContext(),i,options.toBundle());
                startActivity(i, options.toBundle());
                break;
            case Constants.UID_settings_find_phone_button:
                getSupportFragmentManager().popBackStack();
                startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                break;
            case Constants.UID_settings_sensors_button:
                getSupportFragmentManager().popBackStack();
                showSensorsDialog();
                break;
            case Constants.UID_settings_get_info:
                getSupportFragmentManager().popBackStack();
                new TaskSendSettingsWear(1,appPrefs.getLastUserID()).run();
                break;
            case Constants.UID_settings_send_info:
                getSupportFragmentManager().popBackStack();
                new TaskSendSettingsWear(0,appPrefs.getLastUserID()).run();
                break;
            case Constants.UID_settings_has_device_toggle:
                boolean bSet = (selectedId == 1);
                userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, bSet);
                mMessagesViewModel.setPhoneAvailable(bSet);
                if (!bSet) getSupportFragmentManager().popBackStack();
                break;
            case Constants.UID_settings_show_goals_button:
                boolean bSetGoal = (selectedId == 1);
                userPrefs.setPrefByLabel(USER_PREF_SHOW_GOALS, bSetGoal);
                Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
                refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(refreshIntent);
                break;
            case Constants.UID_settings_age_button:
               // if (Constants.INTENT_PERMISSION_AGE.equals(mIntentAction)){
                getSupportFragmentManager().popBackStack();
                doConfirmDialog(Constants.QUESTION_AGE,getString(R.string.ask_user_age),MainActivity.this);
                //}
                break;
            case Constants.UID_settings_height_button:
                getSupportFragmentManager().popBackStack();
                doConfirmDialog(Constants.QUESTION_HEIGHT,getString(R.string.ask_user_height),MainActivity.this);
                break;
            case Constants.UID_settings_load_history_button:
                if ((sUserID.length() > 0) && (sDeviceID.length() > 0)){
                    if (hasOAuthPermission(0)) {
                        if (hasOAuthPermission(5))
                            userPrefs.setReadSessionsPermissions(true);
                        else {
                            requestOAuthPermission(5);
                            return;
                        }
                    } else {
                        requestOAuthPermission(0);
                        return;
                    }
                    getSupportFragmentManager().popBackStack();
                    Configuration configHistory = null;
                    List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUserID);
                    Long lStart = System.currentTimeMillis();
                    if (existingConfigs.size() > 0){
                        configHistory = existingConfigs.get(0);
                        if (Long.parseLong(configHistory.stringValue1) > 0) lStart = Long.parseLong(configHistory.stringValue1);
                    }
                    mCalendar.setTimeInMillis(lStart);
                    int year = mCalendar.get(Calendar.YEAR);
                    int month = mCalendar.get(Calendar.MONTH);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog mTimePicker = new DatePickerDialog(MainActivity.this,R.style.MyDatePickerStyle, (view, selectedYear, monthOfYear, dayOfMonth) -> {
                        createWorkout(sUserID,sDeviceID);
                        long endTime = System.currentTimeMillis();
                        mCalendar.clear();
                        mCalendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                        mCalendar.set(Calendar.SECOND,0); mCalendar.set(Calendar.MILLISECOND,0);
                        long startTime = mCalendar.getTimeInMillis();
                        mWorkout.start = startTime;
                        mWorkout.end = endTime;
                        List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE,sUserID);
                        Configuration configHistory2;
                        if ((configList != null) && (configList.size() > 0)) {
                            configHistory2 = configList.get(0);
                            configHistory2.stringValue1 = Long.toString(startTime);
                            configHistory2.stringValue2 = "0";
                            configHistory2.longValue = 0;
                            mSessionViewModel.updateConfig(configHistory2);
                        }else{
                            configHistory2 = new Configuration(Constants.MAP_HISTORY_RANGE, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, Long.toString(startTime), "0");
                            mSessionViewModel.insertConfig(configHistory2);
                        }
                        doAsyncGoogleFitAction(TASK_ACTION_SYNC_WORKOUT,mWorkout,mWorkoutSet);
                    },year,month,day);
                    mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (mMessagesViewModel.isWorkInProgress()) mMessagesViewModel.setWorkInProgress(false);
                        }
                    });
                    mTimePicker.setTitle("Load from Google Fit History");
                    mTimePicker.show();
                }
                break;
            case Constants.UID_settings_notifications_button:
                getSupportFragmentManager().popBackStack();
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", Constants.ATRACKIT_ATRACKIT_CLASS);
                intent.putExtra("app_uid", getApplicationInfo().uid);
                intent.putExtra("android.provider.extra.APP_PACKAGE", Constants.ATRACKIT_ATRACKIT_CLASS);
                ActivityOptionsCompat options1 =
                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.enter_anim, R.anim.no_anim);
                startActivity(intent,options1.toBundle());
                break;
            case Constants.UID_settings_read_permissions :
                if (!hasOAuthPermission(0))
                    requestOAuthPermission(0);
                else{
                    broadcastToast("Binding sensors");
                   // startSensorWorkerAlarm(-1);
                }
                break;
            case Constants.UID_settings_location_permissions:
            case Constants.UID_settings_sensors_permissions:
                Intent mySplashIntent = new Intent(MainActivity.this, SplashActivity.class);
                mySplashIntent.putExtra(KEY_FIT_USER,((mGoogleAccount != null) ? mGoogleAccount.getId():ATRACKIT_EMPTY));
                mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                if (srcId != Constants.UID_settings_sensors_permissions)
                    mySplashIntent.putExtra(SplashActivity.ARG_ACTION, INTENT_PERMISSION_LOCATION);
                else
                    mySplashIntent.putExtra(SplashActivity.ARG_ACTION, INTENT_PERMISSION_SENSOR);
                mySplashIntent.putExtra(SplashActivity.ARG_RETURN_RESULT, 1);
                splashActivityResultLauncher.launch(mySplashIntent);
                break;
            case Constants.UID_settings_sensors_use:
                appPrefs.setUseSensors((selectedId > 0));
                if (selectedId > 0){
                    broadcastToast(getString(R.string.sensors_permission_used));
                    setupListenersAndIntents(getApplicationContext());
                }else{
                    BoundSensorManager.doReset(0);
                    BoundSensorManager.doReset(1);
                    BoundSensorManager.doReset(2);
                    broadcastToast(getString(R.string.sensors_permission_not_used));
                }
                break;
            case Constants.UID_settings_location_use:
                appPrefs.setUseLocation((selectedId > 0));
                if (selectedId > 0){
                    mMessagesViewModel.setUseLocation(true);
                    BoundFusedLocationClient.standardInterval();
                    broadcastToast(getString(R.string.location_permission_used));
                    doHomeFragmentRefresh();
                }else{
                    BoundFusedLocationClient.clearInterval();
                    mMessagesViewModel.setUseLocation(false);
                    broadcastToast(getString(R.string.location_permission_not_used));
                }
                break;
            case Constants.UID_settings_load_goals_button:
                if (appPrefs.getAppSetupCompleted() && ((userPrefs != null) && userPrefs.getReadDailyPermissions())) {
                    getSupportFragmentManager().popBackStack();
                    doGoalsRefresh();
                }else
                    if ((userPrefs != null) && !userPrefs.getReadDailyPermissions()) requestOAuthPermission(0);
                break;
            case Constants.UID_btnHomeStart:
                /* at home go to new activity type prompt*/
                if ((currentState == WORKOUT_LIVE)||(currentState == WORKOUT_PAUSED)) {
                    Intent actionIntent = new Intent((currentState == WORKOUT_PAUSED)? INTENT_ACTIVE_RESUMED:INTENT_ACTIVE_PAUSE);
                    actionIntent.putExtra(KEY_FIT_TYPE, 0);
                    if (mWorkout != null) {
                        actionIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        actionIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        actionIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    actionIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(actionIntent);
                }else{
                    CustomActivityListDialog activityListDialog = CustomActivityListDialog.getInstance();
                    activityListDialog.setFragmentInterfaceCallback(MainActivity.this);
                    activityListDialog.show(getSupportFragmentManager(), activityListDialog.TAG);
                }
                break;
            case Constants.UID_textViewMsgLeft:
                break;
            case Constants.UID_textViewMsgRight:
                break;
            case Constants.UID_textViewMsgBottomRight:
                break;
            case Constants.UID_textViewMsgBottomLeft:
                break;
/*            case R.id.btnShowMap:
                showMapsFragment(null);
                break;*/
            case Constants.UID_SwipeView:
                if (appPrefs.getAppSetupCompleted() && ((userPrefs != null) &&  userPrefs.getReadDailyPermissions() && userPrefs.getReadSensorsPermissions()))
                    if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)) {
                        Intent newIntent = new Intent(INTENT_DAILY);
                        newIntent.putExtra(KEY_FIT_USER, sUserID);
                        newIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                        newIntent.putExtra(KEY_FIT_VALUE, appPrefs.getLastUserLogIn());
                        newIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        if (!mMessagesViewModel.isWorkInProgress()) new Handler().post(new TaskStartupJobs(newIntent));
                    }
                break;
            case Constants.UID_chronoClock:
            case Constants.UID_textViewCenter2:
            case Constants.UID_chronometerViewCenter:
            case Constants.UID_home_image_view:
                if (currentState == WORKOUT_LIVE) {
                    Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                    vibIntent.putExtra(KEY_FIT_TYPE, 2);
                    sendBroadcast(vibIntent);
                    //sendBroadcast(vibIntent);
                    if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)) {
                        if (userPrefs.getConfirmSetSession()) {
                            showAlertDialogConfirm(ACTION_END_SET);
                            return;
                        }else
                            onCustomConfirmButtonClicked(ACTION_END_SET, 1);
                    } else {
                        if (userPrefs.getConfirmEndSession()) {
                            showAlertDialogConfirm(Constants.ACTION_STOPPING);
                            return;
                        }else
                            onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                    }
                }
                else
                    if (currentState == WORKOUT_PAUSED) {
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 1);
                        //sendBroadcast(vibIntent);
                        sendBroadcast(vibIntent);
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(ACTION_RESUMING);
                            return;
                        } else
                            onCustomConfirmButtonClicked(ACTION_RESUMING, 1);
                    }else {
                        if (srcId == Constants.UID_chronoClock){
                            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                                String sMsg = appPrefs.getLastNodeName();
                                long lastSync = appPrefs.getLastNodeSync();
                                long timeMs = System.currentTimeMillis();
                                if (lastSync > 0) sMsg += (ATRACKIT_SPACE + Utilities.getTimeString(lastSync));
                                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)){
                                    broadcastToast(sMsg);
                                }
                            }
                            return;
                        }
                        if (currentState == WORKOUT_COMPLETED) {
                            broadcastToast("Workout Report!");
                            if ((mWorkout != null) && (mWorkout._id > 1)) {
                                startReportDetailActivityForResult(mWorkout);
                            }
                        } else {
                            if (srcId != Constants.UID_textViewCenter2) showGoalsDialog();
                            else{
//                                DataMap dataMap = new DataMap();
//                                dataMap.putString(KEY_FIT_USER, sUserID);
//                                dataMap.putString(KEY_FIT_DEVICE_ID, sDeviceID);
//                                if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
//                                if (wearNodeId.length() != 0)
//                                    sendMessage(wearNodeId,Constants.DATA_START_ACTIVITY,dataMap);
//                                else
//                                    sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME,Constants.DATA_START_ACTIVITY,dataMap);
                            }
                          //  Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                         //   vibIntent.putExtra(KEY_FIT_TYPE, 3);
                         //   mMessagesViewModel.addLiveIntent(vibIntent);
                        }
                }
                break;
            case Constants.UID_btnAddExercise:
                mSavedStateViewModel.setSpeechTarget(Constants.TARGET_EXERCISE_NAME);
                displaySpeechRecognizer();
                break;
            case Constants.UID_btnReps:
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        if (mWorkoutSet != null) {
                            mWorkoutSet.repCount = 0;
                            mSavedStateViewModel.setDirtyCount(1);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }else {
                        String sDefault = String.format(Locale.getDefault(), SINGLE_INT, mWorkoutSet.repCount);
                        startCustomList(Constants.SELECTION_REPS, sDefault, sUserID);
                    }
                }else {
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        mWorkout.goal_duration = 0L;
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                    }else {
                       String sDefault = Long.toString(mWorkout.goal_duration);
                        startCustomList(SELECTION_GOAL_DURATION, sDefault,sUserID);
                    }
                }
                break;
            case Constants.UID_btnSets:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    mSavedStateViewModel.setSetsDefault(0);
                }else {
                    String sDefault = String.format(Locale.getDefault(), SINGLE_INT, mSavedStateViewModel.getSetsDefault());
                    startCustomList(Constants.SELECTION_SETS, sDefault, sUserID);
                }
                break;
            case Constants.UID_btnWeight:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mWorkoutSet != null){
                        mWorkoutSet.weightTotal = 0F;
                        mSavedStateViewModel.setDirtyCount(1);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                }else {
                    if ((mWorkoutSet != null ) && (mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY))
                        startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, ATRACKIT_EMPTY,sUserID);
                    else if (bUseKG)
                        startCustomList(Constants.SELECTION_WEIGHT_KG, ATRACKIT_EMPTY,sUserID);
                    else
                        startCustomList(Constants.SELECTION_WEIGHT_LBS, ATRACKIT_EMPTY,sUserID);
                }
                break;
            case Constants.UID_btnRest:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mWorkoutSet != null){
                        mWorkoutSet.rest_duration = 0L;
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                }
                else
                    startCustomList(Constants.SELECTION_REST_DURATION_GYM,String.format(Locale.getDefault(),SINGLE_INT, (mWorkoutSet.rest_duration != null ? mWorkoutSet.rest_duration : 0)),sUserID);
                break;
            case Constants.UID_btnSettingsRest:
                startCustomList(Constants.SELECTION_REST_DURATION_SETTINGS,String.format(Locale.getDefault(),SINGLE_INT, userPrefs.getWeightsRestDuration()),sUserID);
                break;

            case Constants.UID_btnSave:
                if ((!mWorkoutSet.isValid(true) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) && mSavedStateViewModel.getIsGym()) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
                    if (sMessage.length() > 0) doSnackbar(sMessage, Snackbar.LENGTH_SHORT);
                    return;
                }else
                    sessionSaveCurrentSet();
                break;
            case Constants.UID_btnContinue:
                restoreWorkoutVariables();
                if (mWorkoutSet == null) return;
                if (((mWorkoutSet.scoreTotal != FLAG_NON_TRACKING) && !mWorkoutSet.isValid(true)) && mSavedStateViewModel.getIsGym()
                        && (mSavedStateViewModel.getDirtyCount() > 0)) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
                    if (sMessage.length() > 0)  doSnackbar(sMessage, Snackbar.LENGTH_SHORT);
                    return;
                }else{
                    if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
                }
                if (mWorkoutSet.end == 0)
                    sessionCompleteActiveSet(System.currentTimeMillis());

                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                    sessionGoogleCurrentSet(mWorkoutSet);

                getSupportFragmentManager().popBackStack();
                if (userPrefs.getConfirmStartSession()) {
                    showAlertDialogConfirm(Constants.ACTION_START_SET);
                    return;
                }else
                    onCustomConfirmButtonClicked(ACTION_START_SET, 1); // which sends the intent to start set
                break;
            case Constants.UID_btnRepeat:
            case R.id.btnConfirmExerciseRepeat:
                restoreWorkoutVariables();
                if (mWorkoutSet == null) return;
                if (mWorkoutSet.end == 0)
                    sessionCompleteActiveSet(System.currentTimeMillis());
                if ((!mWorkoutSet.isValid(true) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) && mSavedStateViewModel.getIsGym()
                        && (mSavedStateViewModel.getDirtyCount() > 0)) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
                    if (sMessage.length() > 0)  doSnackbar(sMessage, Snackbar.LENGTH_SHORT);
                    return;
                }else {
                    getSupportFragmentManager().popBackStack();
                    if (mSavedStateViewModel.getDirtyCount() > 0)
                        sessionSaveCurrentSet();

                   if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                        sessionGoogleCurrentSet(mWorkoutSet);

                    if (userPrefs.getConfirmStartSession()) {
                        showAlertDialogConfirm(Constants.ACTION_REPEAT_SET);
                        return;
                    }else
                        onCustomConfirmButtonClicked(Constants.ACTION_REPEAT_SET, 1); // which sends the intent to start set

                }
                break;
            case Constants.UID_btnFinish:
            case Constants.UID_btnConfirmFinish:
                restoreWorkoutVariables();
                if (mWorkout == null || mWorkoutSet == null) return;
                if ((!mWorkoutSet.isValid(true) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) && (mSavedStateViewModel.getDirtyCount() > 0)) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
                    if (sMessage.length() > 0) doSnackbar(sMessage, Snackbar.LENGTH_SHORT);
                    return;
                }
                if (mWorkoutSet.end == 0)
                    sessionCompleteActiveSet(System.currentTimeMillis());
                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                    sessionGoogleCurrentSet(mWorkoutSet);
                getSupportFragmentManager().popBackStack();
                if (userPrefs.getConfirmEndSession()) {
                    showAlertDialogConfirm(Constants.ACTION_STOPPING);
                    return;
                }else
                    onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                break;

            case Constants.UID_btnBodypart:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){  // GYM tag
                    mWorkoutSet.bodypartName = Constants.ATRACKIT_EMPTY;
                    mWorkoutSet.bodypartID = 0L;
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                }else
                    startCustomList(SELECTION_BODYPART, null, sUserID);
                break;
            case Constants.UID_btnExercise:
                if ((text != null) && text.equals(Constants.LABEL_LONG)) { // clear exercise
                    if (mWorkoutSet != null) {
                        mWorkoutSet.exerciseName = Constants.ATRACKIT_EMPTY;
                        mWorkoutSet.per_end_xy = ATRACKIT_EMPTY;
                        mWorkoutSet.exerciseID = null;
                        mSavedStateViewModel.setDirtyCount(1);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                }else {
                    if (mWorkoutSet != null && mWorkoutSet.exerciseID != null)
                        startCustomList(SELECTION_EXERCISE, Long.toString(mWorkoutSet.exerciseID), sUserID);
                    else
                        startCustomList(SELECTION_EXERCISE, ATRACKIT_EMPTY, sUserID);
                }

                break;
            case Constants.UID_btnConfirmEdit:
                restoreWorkoutVariables();
                if (mWorkoutSet == null) return;
                if (mWorkoutSet.end == 0)
                    sessionCompleteActiveSet(System.currentTimeMillis());

                if (!mWorkoutSet.isValid(true) && (mSavedStateViewModel.getDirtyCount() > 0)) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
                    if (sMessage.length() > 0) doSnackbar(sMessage, Snackbar.LENGTH_SHORT);
                    return;
                }
                if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0)) sessionGoogleCurrentSet(mWorkoutSet);

                int nextSetIndex = mSavedStateViewModel.getSetIndex() + 1;
                List<WorkoutSet> sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID,mWorkout.deviceID);
                if ((nextSetIndex <=  sets.size()) && (sets.get(nextSetIndex - 1).end == 0)) {
                    mWorkoutSet = sets.get(nextSetIndex - 1);   // zero based set!
                    //mWorkoutSet.scoreTotal = 0;
                }else{
                    WorkoutSet newSet =  new WorkoutSet(mWorkoutSet);
                    newSet.setCount = nextSetIndex;  newSet.scoreTotal = FLAG_PENDING;
                    newSet.exerciseID = null; newSet.exerciseName = ATRACKIT_EMPTY;
                    newSet.start = 0; newSet.end = 0; newSet.realElapsedEnd = null; newSet.realElapsedStart = null;
                    newSet.pause_duration = 0;  newSet.startBPM = null; newSet.endBPM = null;
                    newSet.last_sync = 0; newSet.device_sync = null; newSet.meta_sync =null;
                    newSet.duration = 0; newSet.weightTotal = 0f; newSet.per_end_xy = ATRACKIT_EMPTY;
                    mWorkoutSet = newSet;
                    sets.add(mWorkoutSet);
                }
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                mSavedStateViewModel.setToDoSets(sets);
                getSupportFragmentManager().popBackStack();
                if (mSavedStateViewModel.getState() != WORKOUT_PAUSED)
                    sessionSetCurrentState(WORKOUT_PAUSED);
                Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                mMessagesViewModel.addLiveIntent(editIntent);
                break;
            case Constants.UID_btnWeightMinus:
                WorkoutSet setMinus = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (setMinus._id == 0) return;  // must be the current set only
                if (setMinus.weightTotal!=null && setMinus.weightTotal > 0F){
                    if (setMinus.weightTotal > 100F)
                        if (setMinus.weightTotal > 2.5F) setMinus.weightTotal = setMinus.weightTotal - 2.5F; else setMinus.weightTotal = 0F;
                    else
                    if (setMinus.weightTotal > 1F) setMinus.weightTotal = setMinus.weightTotal - 1F; else setMinus.weightTotal = 0F;
                    mSavedStateViewModel.setActiveWorkoutSet(setMinus);
                }
                break;
            case Constants.UID_btnWeightPlus:
                WorkoutSet setPlus = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (setPlus._id == 0) return;  // must be the current set only
                if (setPlus.weightTotal!=null && setPlus.weightTotal > 100F)
                    setPlus.weightTotal += 2.5F;
                else
                    setPlus.weightTotal += 1F;

                mSavedStateViewModel.setActiveWorkoutSet(setPlus);
                break;
            case Constants.UID_btnRepsMinus:
                WorkoutSet repMinusSet = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (repMinusSet._id == 0) return;  // must be the current set only
                if (repMinusSet.repCount != null && repMinusSet.repCount > 1) repMinusSet.repCount -= 1;
                mSavedStateViewModel.setActiveWorkoutSet(repMinusSet);
                break;
            case Constants.UID_btnRepsPlus:
                WorkoutSet repPlusSet = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (repPlusSet._id == 0) return;  // must be the current set only
                repPlusSet.repCount += 1;
                mSavedStateViewModel.setActiveWorkoutSet(repPlusSet);
                break;            
           case Constants.UID_btnSetsMinus:
                WorkoutSet setMinusSet = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (setMinusSet._id == 0) return;  // must be the current set only
                if (setMinusSet.setCount > 1) setMinusSet.setCount -= 1;
                mSavedStateViewModel.setActiveWorkoutSet(setMinusSet);
                break;
            case Constants.UID_btnSetsPlus:
                WorkoutSet setPlusSet = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                if (setPlusSet._id == 0) return;  // must be the current set only
                if (setPlusSet.setCount > 1) setPlusSet.setCount += 1;
                mSavedStateViewModel.setActiveWorkoutSet(setPlusSet);
                break;
            case Constants.UID_archery_call_button:
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();

                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                break;
            case Constants.UID_archery_end_button:
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                break;
            case Constants.UID_archery_rest_button:
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                break;
            case Constants.UID_archery_confirm_next_end_button:
                restoreWorkoutVariables();
                if (mWorkoutSet == null) return;
                sessionSaveCurrentSet();
                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                    sessionGoogleCurrentSet(mWorkoutSet);
                if (userPrefs.getConfirmStartSession()) {
                    showAlertDialogConfirm(Constants.ACTION_START_SET);
                    return;
                }else
                    onCustomConfirmButtonClicked(ACTION_START_SET, 1); // which sends the intent to start set
                break;
            case Constants.UID_archery_confirm_exit_button:
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                if (mWorkoutMeta == null) return;

                if (!mWorkoutMeta.isValid(true) && (mSavedStateViewModel.getDirtyCount() > 0)) {
                    String sMessage = Constants.ATRACKIT_EMPTY;
                    //TODO: change this for archery validation
/*
                    if (mWorkoutSet.exerciseID == 0)
                        sMessage = getString(R.string.label_exercise);
                    else {
                        if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                            sMessage = getString(R.string.label_weight);
                        else {
                            if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                sMessage = getString(R.string.label_rep);
                        }
                    }
*/
                    if (sMessage.length() > 0) broadcastToast(sMessage);
                    return;
                }
                if (userPrefs.getConfirmEndSession()) {
                    showAlertDialogConfirm(Constants.ACTION_STOPPING);
                    return;
                }else
                    onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                break;
        } // switch srcId
    }

    @Override
    public void onItemSelected(int pos, long id, String title, long resid, String identifier) {
        int i =0; String sLabel; String defaultValue =""; int selectionType = SELECTION_ACTIVITY_GYM;
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        final String sDeviceId = mSavedStateViewModel.getDeviceID();
        if (pos == Constants.SELECTION_MAP){
            MapsFragment mapsFragment = (MapsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.map_fragment);
            // We only create a fragment if it doesn't already exist.
            if (mapsFragment == null) {
                // To programmatically add the map, we first create a SupportMapFragment.
                mapsFragment = MapsFragment.newInstance();

                // Then we add it using a FragmentTransaction.
                FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(android.R.id.content, mapsFragment, mapsFragment.LOG_TAG);
                fragmentTransaction.addToBackStack(mapsFragment.LOG_TAG);
                fragmentTransaction.commit();
            }else
                mapsFragment.getView().setVisibility(View.VISIBLE);
            return;
        }
        if (pos == Constants.SELECTION_TEMPLATE){
            if (sUserId.length() > 0) startRecentActivity(3,1,appPrefs.getHistoryOpen());
            return;
        }
        if (pos == Constants.SELECTION_SENSOR_BINDINGS){
         //   startSensorWorkerAlarm(-1);
            return;
        }

        if (pos == Constants.SELECTION_WORKOUT_REPORT){
            Workout w = new Workout();
            w.activityID = id;
            long startTime;
            long endTime;
            int mPage = Math.toIntExact(resid);
            Utilities.TimeFrame tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
            switch (mPage){
                case 0:
                    tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
                    break;
                case 1:
                    tf = Utilities.TimeFrame.BEGINNING_OF_WEEK;
                    break;
                case 2:
                    tf = Utilities.TimeFrame.LAST_WEEK;
                    break;
                case 3:
                    tf = Utilities.TimeFrame.BEGINNING_OF_MONTH;
                    break;
                case 4:
                    tf = Utilities.TimeFrame.LAST_MONTH;
                    break;
                case 5:
                    tf = Utilities.TimeFrame.NINETY_DAYS;
                    break;
            }
            startTime = Utilities.getTimeFrameStart(tf);
            endTime = System.currentTimeMillis();
            if (Boolean.parseBoolean(identifier)) { // summary report type
                w.start = startTime; w.end = endTime;
                w.userID = sUserId;
                w.activityName = Utilities.getSummaryActivityName(getApplicationContext(), id);
                w.deviceID = sDeviceId;
                View v = findViewById(R.id.image);
                startDetailActivity(v,w,1,mPage);
            }else {
                Workout w2 = mSessionViewModel.getWorkoutById(id, sUserId, ATRACKIT_EMPTY);
                w2.start = startTime; w2.end = endTime;
                startReportDetailActivityForResult(w2);
            }
            return;
        }
        int intID = 0;
        try {
            intID = Math.toIntExact(id);
        }catch (Exception e){
            e.printStackTrace();
            intID = 0;
        }
        mReferenceTools.init(getApplicationContext());
        createWorkout(sUserId, sDeviceId);
        mWorkout._id = 2;  // new blank !
        mWorkout.start = 0L;     // not live
        createWorkoutSet();
        mWorkoutSet.setCount = 1;
        mWorkoutSet.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
        mWorkoutSet.scoreTotal = Constants.FLAG_BUILDING; // build stage
        mSavedStateViewModel.setToDoSets(new ArrayList<WorkoutSet>());  // clear any previous lists
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        if (!hasOAuthPermission(1)){
            requestOAuthPermission(1);
            return;
        }
        switch (intID){
            case R.id.home_action1_btn:
                i = userPrefs.getActivityID1();
                if (i > 0){
                    mWorkout.activityID = (long)i;
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(i));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(i));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    //startRoutineActivityForResult();
                    Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(editIntent);

                }else {
                    intID = SELECTION_ACTIVITY_GYM;
                    mWorkout.activityID = (long)intID;
                    mWorkout._id = 1;
                    mWorkout.activityName = title;
                    mSavedStateViewModel.setIconID((int)resid);
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(intID));
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(id));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(id));
                    mWorkout.identifier = identifier;
                    sLabel = getString(R.string.default_loadtype) + Integer.toString(SELECTION_ACTIVITY_GYM);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = SELECTION_ACTIVITY_GYM;
                }
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                startCustomList(selectionType, defaultValue, sUserId);
                break;
            case R.id.home_action2_btn:
                i = userPrefs.getActivityID2();
                if (i > 0){
                    mWorkout.activityID = (long)i;
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(i));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(i));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    if (!hasOAuthPermission(1)){
                        requestOAuthPermission(1);
                        return;
                    }else
                        sessionStart();
                }else {
                    id = Constants.WORKOUT_TYPE_ARCHERY;
                    mWorkout.activityID = id;
                    mWorkout.activityName = title;
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(id));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(id));
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(id));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(id));
                    mWorkout.identifier = identifier;
                    mSavedStateViewModel.setSetIndex(1);
                    //startRoutineActivityForResult();
                    Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(editIntent);
                }
                //
                break;
            case R.id.home_action3_btn:
                i = userPrefs.getActivityID3();
                if (i > 0){
                    mWorkout.activityID = (long)i;
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    //startRoutineActivityForResult();
                    Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(editIntent);
                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_AEROBICS));
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.WORKOUT_TYPE_AEROBICS);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_CARDIO;
                    startCustomList(selectionType, defaultValue,sUserId);
                }
                break;
            case R.id.home_action4_btn:
                i = userPrefs.getActivityID4();
                if (i > 0){
                    mWorkout.activityID =(long)i;
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    //startRoutineActivityForResult();
                    Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(editIntent);
                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_RUNNING));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_RUNNING));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);

                    sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.SELECTION_ACTIVITY_RUN);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_RUN;
                    startCustomList(selectionType, defaultValue,sUserId);
                }
                break;
            case R.id.home_action5_btn:
                i = userPrefs.getActivityID5();
                if (i > 0){
                    mWorkout.activityID = (long)i;
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    //startRoutineActivityForResult();
                    Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(editIntent);

                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_KAYAKING));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_KAYAKING));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.SELECTION_ACTIVITY_SPORT);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_SPORT;
                    startCustomList(selectionType, defaultValue,sUserId);
                }
                break;
            case R.id.home_action6_image:
                mWorkout.activityID = 0L;
                mWorkout.activityName = "";
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(68));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(68)); //downhill skiing
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.SELECTION_ACTIVITY_SPORT);
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                selectionType = Constants.SELECTION_ACTIVITY_WINTER;
                startCustomList(selectionType, defaultValue,sUserId);
                break;
            case R.id.home_action7_btn:
                mWorkout.activityID = 0L;
                mWorkout.activityName = "";
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(83));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(83)); //swimmer diving into water
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.SELECTION_ACTIVITY_SPORT);
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                selectionType = Constants.SELECTION_ACTIVITY_WATER;
                startCustomList(selectionType, defaultValue,sUserId);
                break;
            case R.id.home_action8_btn:
                mWorkout.activityID = 0L;
                mWorkout.activityName = "";
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(16));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(16)); //man cycling
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                selectionType = Constants.SELECTION_ACTIVITY_BIKE;
                sLabel = getString(R.string.default_loadtype) + selectionType;
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                startCustomList(selectionType, defaultValue,sUserId);
                break;
            case R.id.home_action9_btn:
                mWorkout.activityID = 0L;
                mWorkout.activityName = "";
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(24));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(24)); //dancing
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                selectionType = Constants.SELECTION_ACTIVITY_MISC;
                sLabel = getString(R.string.default_loadtype) + selectionType;
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                startCustomList(selectionType, defaultValue,sUserId);
                break;
            case R.id.home_action10_btn:
                mWorkout.activityID = 0L;
                mWorkout.activityName = "";
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.SELECTION_FITNESS_ACTIVITY));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.SELECTION_FITNESS_ACTIVITY)); //a-z
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                selectionType = Constants.SELECTION_FITNESS_ACTIVITY;
                sLabel = getString(R.string.default_loadtype) + selectionType;
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                startCustomList(selectionType, defaultValue,sUserId);
                break;
        } // end of switch
    }
    private void vibrate(long version) {

        final Vibrator mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null) return;
        AppExecutors.getInstance().mainThread().execute(() -> {
            try {
                if (version == 1) {
                    long[] pattern = {0, 400, 200, 400};
                    mVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                }
                if (version == 2){
                    long[] pattern = {0, 800};
                    mVibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                if (version == 3){
                    long[] pattern = {0, 400};
                    mVibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
                }else{
                    long[] pattern = { 0, 100, 500, 100, 500};
                    mVibrator.vibrate(VibrationEffect.createWaveform(pattern,VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }catch (Exception e){
                Log.d(LOG_TAG, "Vibrate error " + e.getMessage());
            }
        });
    }
    /**
     * Plays back the MP3 file embedded in the application
     */
    private void playMusic() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.nfl_score_chine);
            if (mMediaPlayer != null)
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // we need to transition to the READY/Home state
                    Log.d(LOG_TAG, "Music Finished");
                    //mUIAnimation.transitionToHome();
                }
            });
        }else
            mMediaPlayer.start();
    }

    /**
     * Stops the playback of the MP3 file.
     */
    private void stopMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                // Start the activity, the intent will be populated with the speech text
                microphoneActivityResultLauncher.launch(intent);
            }catch (ActivityNotFoundException anf){
                broadcastToast(getString(R.string.action_nospeech_activity));
            }
        }else{
            microphonePermissionResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    REQUEST_RECORDING_PERMISSION_CODE);
        }
    }
    private void startCustomList(int selectionType, String defaultValue, String sUserID){
        CustomListDialogFragment customListFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        String sDeviceID = mSavedStateViewModel.getDeviceID();
        if ((selectionType == Constants.SELECTION_SETS) || (selectionType == Constants.SELECTION_REPS) || (selectionType == SELECTION_INCOMPLETE_DURATION)
                || (selectionType == Constants.SELECTION_GOAL_DURATION) || (selectionType == Constants.SELECTION_GOAL_STEPS)){
            Integer iPreset = 0;
            if (defaultValue.length() > 0)
                iPreset = Integer.parseInt(defaultValue);
            else {
                //TODO: setup correct defaults for gym
                if (selectionType == Constants.SELECTION_SETS)
                    iPreset = 3;
                if (selectionType == Constants.SELECTION_REPS)
                    iPreset = 10;
            }
            CustomScoreDialogFragment customScoreDialogFragment = CustomScoreDialogFragment.newInstance(selectionType,iPreset);
            customScoreDialogFragment.show(fragmentManager, CustomScoreDialogFragment.TAG);
        }else {
            Integer iPreset = 10 ;
            if ((defaultValue != null) && (defaultValue.length() > 0))
                iPreset = Integer.parseInt(defaultValue);

            String sTag = CustomListDialogFragment.TAG;
            if (selectionType == Constants.SELECTION_EXERCISE) {
                sTag = getString(R.string.label_exercise);
            }

            if (selectionType == Constants.SELECTION_BODYPART) {
                sTag = getString(R.string.label_bodypart);
            }
            if ((selectionType == Constants.SELECTION_WEIGHT_KG) || (selectionType == Constants.SELECTION_WEIGHT_LBS)) {
                sTag = getString(R.string.label_weight);
            }
/*            if (selectionType == Constants.SELECTION_SETS) {
                sTag = getString(R.string.label_set);
            }
            if (selectionType == Constants.SELECTION_REPS) {
                sTag = getString(R.string.label_rep);
            }*/
            if (selectionType == Constants.SELECTION_USER_PREFS) {
                sTag = getString(R.string.nav_setting);
            }
            if (selectionType == Constants.SELECTION_TARGET_FIELD) {
                sTag = getString(R.string.label_shoot_field);
            }
            if ((selectionType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (selectionType == Constants.SELECTION_TARGET_DISTANCE_TARGET)) {
                sTag = getString(R.string.label_shoot_distance);
            }
            if (selectionType == Constants.SELECTION_TARGET_EQUIPMENT) {
                sTag = getString(R.string.label_shoot_equipment);
            }
            if (selectionType == Constants.SELECTION_TARGET_ENDS) {
                sTag = getString(R.string.label_shoot_ends);
            }
            if ((selectionType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (selectionType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET)) {
                sTag = getString(R.string.label_shoot_target_size);
            }
            if (selectionType == Constants.SELECTION_TARGET_POSSIBLE_SCORE) {
                sTag = getString(R.string.label_shoot_possible_score);
            }
            if (selectionType == Constants.SELECTION_TARGET_SHOTS_PER_END) sTag = getString(R.string.label_shoot_per_end);
            if (selectionType == SELECTION_CALL_DURATION) sTag = getString(R.string.label_archery_call_duration);
            if (selectionType == SELECTION_END_DURATION) sTag = getString(R.string.label_archery_end_duration);
            if (selectionType == SELECTION_REST_DURATION_TARGET) sTag = getString(R.string.label_archery_rest_duration);
            if (selectionType == SELECTION_TO_DO_SETS)sTag = getString(R.string.label_routine);                /*customListFragment.set*/
            if (selectionType == Constants.SELECTION_WORKOUT_INPROGRESS) sTag = "Incomplete workouts";
            if (sTag == CustomListDialogFragment.TAG){
                sTag = Utilities.SelectionTypeToString(MainActivity.this, selectionType);
            }
            customListFragment = CustomListDialogFragment.create(selectionType,  iPreset,sTag, sUserID,sDeviceID);
            customListFragment.setListItemSelectedListener(MainActivity.this);
            customListFragment.setCancelable(true);
            customListFragment.show(fragmentManager, customListFragment.TAG);
        }
    }

    private void sendDataClientMessage(String intentString, Bundle bundle){
        PutDataRequest request = setDataRequestItem(intentString, bundle);
        request.setUrgent();
        Log.i(LOG_TAG, "sendDataClientMessage request " + intentString);
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask.addOnCompleteListener(task -> Log.e(LOG_TAG, "sendDataClientMessage outcome: " + bundle.toString()));
    }
    private PutDataRequest setDataRequestItem(String intentString, Bundle bundle){
        Gson gson = new Gson();
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEAR_DATA_BUNDLE_RECEIVED_PATH);
        dataMap.getDataMap().putString(KEY_FIT_ACTION, intentString);
        if (intentString.contains("com.a_track_it.com.workout.exercise")) {
            Exercise exercise = mSavedStateViewModel.getExercise().getValue();
            if  ((bundle != null) && (bundle.containsKey(Exercise.class.getSimpleName()))) exercise = ((Exercise) bundle.getParcelable(Exercise.class.getSimpleName()));
            if (exercise != null){
                dataMap.getDataMap().putString(Exercise.class.getSimpleName(), gson.toJson(exercise));
            }
        }
        if (intentString.contains("com.a_track_it.com.workout.workout")) {
            Workout workout = mWorkout;
            WorkoutSet set = mWorkoutSet;
            String sUserID = mWorkout.userID;
            if ((bundle != null) && (bundle.containsKey(Workout.class.getSimpleName()))) workout = ((Workout)bundle.getParcelable(Workout.class.getSimpleName()));
            if ((bundle != null) && (bundle.containsKey(WorkoutSet.class.getSimpleName()))) set = ((WorkoutSet)bundle.getParcelable(WorkoutSet.class.getSimpleName()));
            if ((bundle != null) && (bundle.containsKey(KEY_FIT_USER))) sUserID = bundle.getString(KEY_FIT_USER);
            if ((set != null) && (set.start > 0L))
                dataMap.getDataMap().putLong(Constants.KEY_FIT_TIME, set.start);
            else
                dataMap.getDataMap().putLong(Constants.KEY_FIT_TIME, new Date().getTime());
            if (workout != null)
                dataMap.getDataMap().putString(Workout.class.getSimpleName(), gson.toJson(workout));
            if (set != null && !intentString.equals(INTENT_ACTIVE_STOP))
                dataMap.getDataMap().putString(WorkoutSet.class.getSimpleName(), gson.toJson(set));

            if ((bundle != null) && (bundle.containsKey(WorkoutMeta.class.getSimpleName())))
                dataMap.getDataMap().putString(WorkoutMeta.class.getSimpleName(), gson.toJson((WorkoutMeta)bundle.getParcelable(WorkoutMeta.class.getSimpleName())));

            if (workout != null && (intentString.equals(INTENT_ACTIVE_STOP) || intentString.equals(INTENT_WORKOUT_EDIT))) {
                List<WorkoutSet> setList = mSessionViewModel.getWorkoutSetByWorkoutID(workout._id,workout.userID,workout.deviceID);
                int iSetCount = ((setList != null && setList.size() > 0) ? setList.size() : 0);
                if (iSetCount > 0) {
                    dataMap.getDataMap().putString(Constants.KEY_LIST_SETS, gson.toJson(setList));
                }
                dataMap.getDataMap().putInt(Constants.KEY_FIT_SETS, iSetCount);
            } else
                dataMap.getDataMap().putInt(Constants.KEY_FIT_SETS, 0);
            if  ((bundle != null) && (bundle.containsKey(KEY_FIT_USER))) dataMap.getDataMap().putString(KEY_FIT_USER, sUserID);
        }
        else
            if  ((bundle != null) && (bundle.containsKey(KEY_FIT_USER))) dataMap.getDataMap().putString(KEY_FIT_USER, bundle.getString(KEY_FIT_USER));
        PutDataRequest request = dataMap.asPutDataRequest();
        return request;
    }


    private void sendMessage(final String host, final String sPath, final DataMap dataMap) {
        // Instantiates clients without member variables, as clients are inexpensive to
        // create. (They are cached and shared between GoogleApi instances.)

        if (host == null){
            for (Node node: mWearNodesWithApp) {
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(
                                getApplicationContext()).sendMessage(
                                node.getId(),
                                sPath,
                                dataMap.toByteArray());

                sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(Task<Integer> task) {
                        if (task.isSuccessful()) {
                            Log.w(LOG_TAG, "Message sent successfully " + dataMap.toString());
                            mSavedStateViewModel.setLastDeviceMsg(System.currentTimeMillis());
                        } else {
                            Log.i(LOG_TAG, "Send Message failed. " + sPath);
                        }
                    }
                });
            }
        }else {
            Task<Integer> sendMessageTask =
                    Wearable.getMessageClient(
                            getApplicationContext()).sendMessage(
                            host,
                            sPath,
                            dataMap.toByteArray());

            sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                @Override
                public void onComplete(Task<Integer> task) {
                    if (task.isSuccessful()) {
                        //Log.d(LOG_TAG, "Message sent successfully");
                        mSavedStateViewModel.setLastDeviceMsg(System.currentTimeMillis());
                    } else {
                        Log.d(LOG_TAG, "Message failed. " + sPath);
                    }
                }
            });
        }
    }


    private void sendCapabilityMessage(final String sCapabilityName, final String sPath, final DataMap dataMap) {
        // Initial check of capabilities to find the wear.
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(MainActivity.this)
                .getCapability(sCapabilityName, CapabilityClient.FILTER_REACHABLE);
        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Capability request succeeded.");
                    CapabilityInfo capabilityInfo = task.getResult();
                    String sWearNodeId = pickBestNodeId(capabilityInfo.getNodes());
                    if (sWearNodeId != null) {
                        // Instantiates clients without member variables, as clients are inexpensive to
                        // create. (They are cached and shared between GoogleApi instances.)
                        if ((wearNodeId == null) || !sWearNodeId.equals(wearNodeId)){
                            wearNodeId = sWearNodeId;
                           // appPrefs.setLastNodeID(wearNodeId);
                        }
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(
                                        getApplicationContext()).sendMessage(
                                        wearNodeId,
                                        sPath,
                                        dataMap.toByteArray());

                        sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(Task<Integer> sendMsgTask) {
                                if (sendMsgTask.isSuccessful()) {
                                    Log.d(LOG_TAG, "Cap Message sent successfully");
                                    mSavedStateViewModel.setLastDeviceMsg(System.currentTimeMillis());
                                } else {
                                    Log.w(LOG_TAG, "Cap Message failed.");
                                }
                            }
                        });
                    }
                } else {
                    mMessagesViewModel.setPhoneAvailable(false);
                }
            }
        });
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private String pickBestNodeId(Set<Node> nodes) {
        String sKey = appPrefs.getLastNodeID();
      //  List<Configuration> list = mSessionViewModel.getConfigurationLikeName(KEY_DEVICE2, appPrefs.getLastUserID());
       // final Configuration deviceConfig = (list != null && list.size() > 0) ? list.get(0) : null;
        String bestNodeId = null;
        /* Find a nearby node or pick one arbitrarily. There should be only one phone connected
         * that supports this sample.
         */
        for (Node node : nodes) {
            if (sKey != null && (sKey.length() > 0))
                if (sKey.equals(node.getId())){
                    bestNodeId = node.getId();
                    break;
                }
            else
                if (node.isNearby()){
                    bestNodeId = node.getId();
                    break;
                }


        }
        return bestNodeId;
    }
    private void bindRecognitionListener(){
        // Activity Recognition using pendingIntent
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() > 0 && appPrefs.getAppSetupCompleted()) {
            Context context = getApplicationContext();
            Log.w(LOG_TAG, "ACTIVITY RECOGNITION requesting update " + sUserId);
            Intent intent = new Intent(context, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            activityRecognitionClient = ActivityRecognition.getClient(context);
            Task<Void> task2 = activityRecognitionClient.requestActivityUpdates(180_000L, pendingIntent);
            task2.addOnCompleteListener(task1 -> {
                boolean isSuccess = task1.isSuccessful();
                Log.e(LOG_TAG, "ACTIVITY RECOGNITION requesting update completed " + isSuccess);
            });
        }
    }
    private void destroyRecognitionListener(){
        if  (activityRecognitionClient != null) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            activityRecognitionClient.removeActivityUpdates(pendingIntent);
        }
    }
    private void bindLocationListener(){
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() > 0) {
            if (mLocationListener == null)
                mLocationListener = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Location loc = locationResult.getLastLocation();
                        if (loc != null) {
                            Intent intent = new Intent(INTENT_LOCATION_UPDATE);
                            Context context = getApplicationContext();
                            intent.putExtra(INTENT_LOCATION_UPDATE, loc);
                            intent.putExtra(INTENT_EXTRA_RESULT, (loc != null) ? 1 : 0);
                            intent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                            context.sendBroadcast(intent);
                        }
                    }
                };
            Log.w(LOG_TAG, "bindLocationListener ");
            BoundFusedLocationClient.bindFusedLocationListenerIn(MainActivity.this, mLocationListener, getApplicationContext());
            BoundFusedLocationClient.standardInterval();
        }else Log.e(LOG_TAG,"bindLocationListener sUSERID EXIT");
    }

    private void bindFitSensorListener(){
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() > 0) {
            if (mDataPointListener == null)
                mDataPointListener = new xFITSensorListener();
            Log.w(LOG_TAG,"bindFitSensorListener " );
            BoundFitnessSensorManager.bindFitnessSensorListenerIn(this, mDataPointListener, getApplicationContext());
            BoundFitnessSensorManager.doSetup();
        }else Log.e(LOG_TAG,"bindFitSensorListener sUSERID EXIT");
    }
    private void bindSensorListener(int type, int delay){
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() > 0) {
            Handler handler = new Handler(mSensorThread.getLooper());
            if (mSensorListener == null )mSensorListener = new xSensorListener();
            if (delay < 0) {
                if (type == 0) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getOthersSampleRate()));
                if (type == 1) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getStepsSampleRate()));
                if (type == 2) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getBPMSampleRate()));
            }
            Log.w(LOG_TAG,"bindSensorListener " + type + " delay " + delay);
            BoundSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext(), handler);
            BoundSensorManager.doSetup(type, delay);
        }else Log.e(LOG_TAG,"bindSensorListener sUSERID EXIT");
    }
    private void bindAccelSensorListener(){
        if (mSensorListener == null)mSensorListener = new xSensorListener();
        Log.w(LOG_TAG,"bindAccelSensorListener ");
        BoundAccelSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext());
    }
    private void destroyAccelSensorListener(){
        BoundAccelSensorManager.DestroyListener();
    }

    private class xSensorListener implements SensorEventListener {
        private final Processor peakDetProcessor = new PeakDetProcessor(Constants.DELTA);
        public xSensorListener(){   }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            String sName = event.sensor.getName();
            Float val;
            String sValue; 
            long lTemp = 0L;
            long timeMs = System.currentTimeMillis();

            SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
            switch (iType) {
                case Sensor.TYPE_ACCELEROMETER:
                    if ((mWorkout != null) && ((mWorkout.start == 0) || (mWorkout.end != 0))) return;  // ignore accel unless live
                    boolean isRepetition = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition) {
                        DailyCounter repCounter = mSavedStateViewModel.getReps();
                        if (repCounter != null)
                            if ((timeMs - repCounter.LastUpdated) >= MIN_REP_INTERVAL_MS) { //750 ms
                                mMessagesViewModel.addDetectedRep();
                                repCounter.LastCount = mMessagesViewModel.getDetectedRep();
                                mSavedStateViewModel.setReps(repCounter);
                            }
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    if ((mWorkout != null) && ((mWorkout.start == 0) || (mWorkout.end != 0))) return;  // ignore accel unless live
                    boolean isRepetition2 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition2) {
                        DailyCounter repCounter = mSavedStateViewModel.getReps();
                        if (repCounter != null)
                            if ((timeMs - repCounter.LastUpdated) >= MIN_REP_INTERVAL_MS) { //750 ms
                                mMessagesViewModel.addDetectedRep();
                                repCounter.LastCount = mMessagesViewModel.getDetectedRep();
                                mSavedStateViewModel.setReps(repCounter);
                            }
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    if ((mWorkout != null) && ((mWorkout.start == 0) || (mWorkout.end != 0))) return;  // ignore accel unless live
                    boolean isRepetition3 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition3) {
                        DailyCounter repCounter = mSavedStateViewModel.getReps();
                        if (repCounter != null)
                            if ((timeMs - repCounter.LastUpdated) >= MIN_REP_INTERVAL_MS) { //750 ms
                                mMessagesViewModel.addDetectedRep();
                                repCounter.LastCount = mMessagesViewModel.getDetectedRep();
                                mSavedStateViewModel.setReps(repCounter);
                            }

                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    if (BoundSensorManager.getCount(1) == 0) return;
                    val = event.values[0];
                    sValue = Integer.toString(Math.round(val));
                    Log.w(LOG_TAG,"Step: " + sValue);
                    // device steps always loaded
                    if (!Utilities.isInteger(sValue, 0)) return;
                    DailyCounter stepCounter = mSavedStateViewModel.getSteps();
                    mMessagesViewModel.addDeviceStepsMsg(sValue);
                    if (stepCounter != null) {
                        lTemp = Long.parseLong(sValue);
                        long previousCount = stepCounter.LastCount;
                        long previousUpdate = stepCounter.LastUpdated;
                        if ((lTemp > 0)  && (previousCount != lTemp)){
                            stepCounter.LastCount = lTemp;
                            stepCounter.LastUpdated = timeMs;
                            // if have a goal and it is active
                            if ((stepCounter.GoalCount > 0) && (stepCounter.GoalActive > 0))
                                if ((stepCounter.LastCount - stepCounter.FirstCount) >= stepCounter.GoalCount){
                                    Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                                    vibIntent.putExtra(KEY_FIT_TYPE, 3);
                                    mMessagesViewModel.addLiveIntent(vibIntent);
                                    // sendNotification Step Goal Attached !
                                    Bundle resultData = new Bundle();
                                    resultData.putLong(KEY_FIT_WORKOUTID,mWorkout._id);
                                    resultData.putInt(KEY_FIT_TYPE, GOAL_TYPE_STEPS);
                                    resultData.putLong(KEY_FIT_ACTION, stepCounter.GoalCount);
                                    resultData.putLong(Constants.KEY_FIT_VALUE, stepCounter.LastCount);
                                    resultData.putString(KEY_FIT_USER, appPrefs.getLastUserID());
                                    if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                                            && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
                                    if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                                            && (Utilities.getRingerMode(getApplicationContext()) != AudioManager.RINGER_MODE_SILENT)) vibrate(3);
                                    if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                    stepCounter.GoalActive = 0;
                                }
                            mSavedStateViewModel.setSteps(stepCounter);
                        }
                    }
                    int lSteps = Math.round(val);
                    if (sdt != null && (lSteps > 0)){
                        sdt.deviceStep = lSteps;
                        sdt.lastDeviceStep = timeMs;
                        sdt.lastUpdated = timeMs;
                        mSavedStateViewModel.setSDT(sdt);
                    }
                    break;
                case Sensor.TYPE_HEART_RATE:
                    if (BoundSensorManager.getCount(2) == 0) return;
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        mMessagesViewModel.addDeviceBpmMsg(sValue);
                        DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                        lTemp = Math.round(val);
                        if (bpmCounter != null) {
                            long previousCount = bpmCounter.LastCount;
                            long previousUpdate = bpmCounter.LastUpdated;
                            if (lTemp > 0) {
                                bpmCounter.LastCount = lTemp;
                                bpmCounter.LastUpdated = timeMs;
                                if ((bpmCounter.GoalCount > 0) && (bpmCounter.GoalActive > 0))
                                    if (bpmCounter.GoalCount < bpmCounter.LastCount){
                                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                                        vibIntent.putExtra(KEY_FIT_TYPE, 3);
                                        mMessagesViewModel.addLiveIntent(vibIntent);
                                        Bundle resultData = new Bundle();
                                        resultData.putLong(KEY_FIT_WORKOUTID,mWorkout._id);
                                        resultData.putInt(KEY_FIT_TYPE, GOAL_TYPE_BPM);
                                        resultData.putLong(KEY_FIT_ACTION, bpmCounter.GoalCount);
                                        resultData.putLong(Constants.KEY_FIT_VALUE, bpmCounter.LastCount);
                                        resultData.putString(KEY_FIT_USER, appPrefs.getLastUserID());
                                        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                                                && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
                                        if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                                                && (Utilities.getRingerMode(getApplicationContext()) != AudioManager.RINGER_MODE_SILENT)) vibrate(3);
                                        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                        bpmCounter.GoalActive = 0;
                                    }
                                mSavedStateViewModel.setBPM(bpmCounter);
                            }
                        }
                        if (sdt != null && (val > 0)){
                            Log.w(LOG_TAG,"BPM: " + sValue);
                            sdt.deviceBPM = val;
                            sdt.lastUpdated = timeMs;
                            sdt.lastDeviceBPM = timeMs;
                            mSavedStateViewModel.setSDT(sdt);
                        }
                    }
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    if (BoundSensorManager.getCount(0) == 0) return;
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, val);
                        String sExisting = mMessagesViewModel.getTemperatureMsg().getValue();
                        if (!sValue.equals(sExisting)) {
                            mMessagesViewModel.addTemperatureMsg(sValue);
                            if (sdt != null && (val > 0)){
                                Log.w(LOG_TAG,"TEMP: " + sValue);
                                sdt.temperature = val;
                                sdt.lastUpdated = timeMs;
                                sdt.lastDeviceOther = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getPressureSensorCount() == 0) || (appPrefs.getPressureSensorCount() > 0 && (sdt.pressure > 0))){
                                    if ((appPrefs.getHumiditySensorCount() == 0) || (appPrefs.getHumiditySensorCount() > 0 && (sdt.humidity > 0))){
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BoundSensorManager.doReset(0);
                                            }
                                        },2000);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case Sensor.TYPE_PRESSURE:
                    if (BoundSensorManager.getCount(0) == 0) return;
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        String sNow = mMessagesViewModel.getPressureMsg().getValue();
                        if (!sValue.equals(sNow)) {
                            mMessagesViewModel.addPressureMsg(sValue);
                            if (sdt != null && (val > 0)){
                                Log.w(LOG_TAG,"hPa: " + sValue);
                                sdt.pressure = val;
                                sdt.lastUpdated = timeMs;
                                sdt.lastDeviceOther = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getTempSensorCount() == 0) || (appPrefs.getTempSensorCount() > 0 && (sdt.temperature > 0))){
                                    if ((appPrefs.getHumiditySensorCount() == 0) || (appPrefs.getHumiditySensorCount() > 0 && (sdt.humidity > 0))){
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BoundSensorManager.doReset(0);
                                            }
                                        },2000);
                                    }
                                }
                            }
                        }


                    }
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    if (BoundSensorManager.getCount(0) == 0) return;
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        String sNow = mMessagesViewModel.getHumidityMsg().getValue();
                        if (!sValue.equals(sNow)) {
                            mMessagesViewModel.addHumidityMsg(sValue);
                            if (sdt != null && (val > 0)){
                                Log.w(LOG_TAG,"Humidty: " + sValue);
                                sdt.humidity = val;
                                sdt.lastUpdated = timeMs;
                                sdt.lastDeviceOther = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getTempSensorCount() == 0) || (appPrefs.getTempSensorCount() > 0 && (sdt.temperature > 0))){
                                    if ((appPrefs.getPressureSensorCount() == 0) || (appPrefs.getPressureSensorCount() > 0 && (sdt.pressure > 0))){
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BoundSensorManager.doReset(0);
                                            }
                                        },2000);
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Log.d(LOG_TAG, "sensor changed accuracy " + sensor.getName());
        }
    }
    private class xFITSensorListener implements OnDataPointListener{
        public xFITSensorListener(){   }
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            String sSuggested;
            long timeMs = System.currentTimeMillis();

            if (DataType.TYPE_HEART_RATE_BPM.equals(dataPoint.getDataType())) {
                dataPoint.getValue(Field.FIELD_BPM);
                int iVal = Math.round(dataPoint.getValue(Field.FIELD_BPM).asFloat());
                if (iVal > 0) {
                    sSuggested = Integer.toString(iVal);
                    mMessagesViewModel.addBpmMsg(sSuggested);
                    SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
                    if (sdt != null){
                            sdt.fitBPM = dataPoint.getValue(Field.FIELD_BPM).asFloat();
                            sdt.lastFitBPM = timeMs;
                            sdt.lastUpdated = timeMs;
                            mSavedStateViewModel.setSDT(sdt);
                          //  if ((BoundFitnessSensorManager.getState() != WORKOUT_LIVE) && (sdt.lastFitStep > sdt._id)) BoundFitnessSensorManager.doReset();
                    }
                }
            }else
            if (DataType.TYPE_MOVE_MINUTES.equals(dataPoint.getDataType())) {
                long moveDuration = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (moveDuration > 0) {
                    sSuggested = Long.toString(TimeUnit.MILLISECONDS.toMinutes(moveDuration));
                    mMessagesViewModel.addMoveMinsMsg(sSuggested);
                }
            }else
            if (DataType.TYPE_WORKOUT_EXERCISE.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_EXERCISE).asString() + " ";
                mMessagesViewModel.addExerciseMsg(sSuggested);
            }else
            if (DataType.TYPE_STEP_COUNT_DELTA.equals(dataPoint.getDataType())) {
                if (dataPoint.getValue(Field.FIELD_STEPS) != null && dataPoint.getValue(Field.FIELD_STEPS).asInt() > 0) {
                    int lSteps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                    sSuggested = Integer.toString(lSteps);
                    mMessagesViewModel.addStepsMsg(sSuggested);
                    SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
                    if (sdt != null){
                        sdt.fitStep = Math.toIntExact(lSteps);
                        sdt.lastFitStep = timeMs;
                        sdt.lastUpdated = timeMs;
                        mSavedStateViewModel.setSDT(sdt);
                       // if ((BoundFitnessSensorManager.getState() != WORKOUT_LIVE) && (sdt.lastFitBPM > sdt._id)) BoundFitnessSensorManager.doReset();
                    }
                }
            }else
            if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataPoint.getDataType())) {
                if (dataPoint.getValue(Field.FIELD_STEPS).asInt() > 0) {
                    //     sSuggested = "step cumul " + Integer.toString(dataPoint.getValue(Field.FIELD_STEPS).asInt());
                    sSuggested = Integer.toString(dataPoint.getValue(Field.FIELD_STEPS).asInt());
                    mMessagesViewModel.addStepsMsg(sSuggested);
                    //       Log.w(LOG_TAG, sSuggested);
                }
            }else
            if (DataType.TYPE_POWER_SAMPLE.equals(dataPoint.getDataType())) {
                if (dataPoint.getValue(Field.FIELD_WATTS) != null) {
                    sSuggested = "watts " + dataPoint.getValue(Field.FIELD_WATTS).asFloat() + " ";
                    Log.d(LOG_TAG, sSuggested);
                }
            }else
            if (DataType.TYPE_LOCATION_SAMPLE.equals(dataPoint.getDataType())) {
                Location loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(dataPoint.getValue(Field.FIELD_LATITUDE).asFloat());
                loc.setLongitude(dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
                Intent intent = new Intent(INTENT_LOCATION_UPDATE);
                intent.putExtra(INTENT_LOCATION_UPDATE, loc);
                intent.putExtra(INTENT_EXTRA_RESULT, (loc != null) ? 1 : 0);
                sendBroadcast(intent);
                SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
                if (sdt != null){
                    sdt.fitLat = (float)loc.getLatitude();
                    sdt.fitLng = (float)loc.getLongitude();
                    sdt.lastUpdated = timeMs;
                    mSavedStateViewModel.setSDT(sdt);
                }

                sSuggested = "xFIT location lat: " + dataPoint.getValue(Field.FIELD_LATITUDE).asFloat() + " long: " + dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
                Log.w(LOG_TAG, sSuggested);

            }else{
                String sDataType = dataPoint.getDataType().getName();
                sSuggested = sDataType + ": " + dataPoint.toString();
                  Log.e(LOG_TAG, "other data point received: " + sSuggested);
            }
        }
    }
    private class xCapabilityListener implements CapabilityClient.OnCapabilityChangedListener{
        @Override
        public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
            String sCapability = capabilityInfo.getName();
            if ((!sCapability.equals(Constants.WEAR_CAPABILITY_NAME)
                    && !sCapability.equals(Constants.STEP_CAPABILITY_NAME)
                    && !sCapability.equals(Constants.BPM_CAPABILITY_NAME)) || (!appPrefs.getAppSetupCompleted() || (appPrefs.getLastUserID().length() == 0)))
                return;
            List<Configuration> existingConfigs = mMessagesViewModel.getLiveConfig();
            Configuration configDevice1 = null; Configuration configDevice2 = null;
            if (existingConfigs.size() > 0) {
                for (Configuration c : existingConfigs) {
                    if (c.stringName.equals(KEY_DEVICE1)) configDevice1 = c;
                    if (c.stringName.equals(KEY_DEVICE2)) configDevice2 = c;
                }
            }
            String sHostID = (configDevice2 != null) ? configDevice2.stringValue : appPrefs.getLastNodeID();
            boolean bPreviously = mMessagesViewModel.hasPhone() || (sHostID.length() == 0);
            //String nodeName = appPrefs.getLastNodeName();
            Set<Node> connectedNodes = capabilityInfo.getNodes();
            for (Node node : connectedNodes) {
                if (!mWearNodesWithApp.contains(node)) {
                    if ((sHostID.equals(node.getId())) || (node.isNearby() && !bPreviously)){
                        mWearNodesWithApp.add(node);
                        wearNodeId = node.getId();
                    //    nodeName = node.getDisplayName();
                    }
                }else {
                    wearNodeId = node.getId();
              //      nodeName = node.getDisplayName();
                }

            }
            if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE) && (wearNodeId != null)) {
                if (configDevice1 != null) {
                    DataMap dataMap = new DataMap();
                    dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                    if ((configDevice2 != null) && (configDevice2.longValue > 0))
                        dataMap.putInt(KEY_FIT_TYPE, (int) 0); // new connection check indicator
                    else
                        dataMap.putInt(KEY_FIT_TYPE, (int) 1); // handshake exchange indicator

                    dataMap.putString(KEY_FIT_USER, mGoogleAccount.getId());
                    dataMap.putString(KEY_FIT_DEVICE_ID, configDevice1.stringValue2);
                    dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    dataMap.putLong(KEY_FIT_TIME, configDevice1.longValue);
                    dataMap.putString(KEY_PAYLOAD, MESSAGE_PATH_PHONE);
                    dataMap.putString(Constants.KEY_DEVICE1, configDevice1.stringValue1);
                    dataMap.putString(Constants.KEY_DEVICE2, configDevice1.stringValue2);

                    Task<Integer> sendMessageTask = Wearable.getMessageClient(
                            getApplicationContext()).sendMessage(wearNodeId,
                            MESSAGE_PATH_WEAR_SERVICE,
                            dataMap.toByteArray());

                    sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                        @Override
                        public void onComplete(Task<Integer> task) {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "COMM_TYPE_REQUEST_INFO Message sent successfully");
                            } else {
                                Log.d(LOG_TAG, "COMM_TYPE_REQUEST_INFO Message failed.   onCapabilityChanged");
                            }
                        }
                    });
                }
            }
        }
    }


    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        if (mIntentReceiver == null) setupListenersAndIntents(getApplicationContext());  // ensure intents are received
        final long timeMs = System.currentTimeMillis();
        switch (requestCode) {
            case Constants.REQUEST_SIGNIN_SYNC:
                broadcastToast("sign-in for SYNC");
                doAlertMessage(ATRACKIT_EMPTY,"sign-in for SYNC",3000);
                startCloudPendingSyncAlarm();
                break;
            case REQUEST_SIGNIN_DAILY:
                broadcastToast("sign-in for Daily");
                doAlertMessage(ATRACKIT_EMPTY,"sign-in for DAILY",3000);
                startDailySummarySyncAlarm();
                break;
            case RC_REQUEST_RECORDING_AND_CONTINUE_SUBSCRIPTION:
                if (resultCode == Activity.RESULT_OK) {
                    Data.Builder builder = new Data.Builder();
                    builder.putInt("record_action", TASK_ACTION_START_SESSION);
                    Data inputData = builder.build();
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                    OneTimeWorkRequest oneTimeWorkRequest =
                            new OneTimeWorkRequest.Builder(FitnessRecordingWorker.class)
                                    .setInputData(inputData)
                                    .setConstraints(constraints)
                                    .build();
                    mWorkManager.enqueue(oneTimeWorkRequest).getState().observe(MainActivity.this, new Observer<Operation.State>() {
                        @Override
                        public void onChanged(Operation.State state) {
                            if(state instanceof Operation.State.SUCCESS){

                            }
                        }

                    });
                }
                break;
            case RC_REQUEST_FIT_PERMISSION_AND_CONTINUE:
                if (resultCode == Activity.RESULT_OK){
                    int workType = mMessagesViewModel.getWorkType();
                    if (workType > 0 && mWorkout != null)
                        doAsyncGoogleFitAction(workType,mWorkout,mWorkoutSet);
                    if (workType == 0)
                        doDailySummaryJob();
                }
                break;
            case REQUEST_OAUTH_REQUEST_CODE:
                Log.w(LOG_TAG, "onActivityResult " + requestCode + " result " + resultCode);
                if (resultCode == Activity.RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result != null) {
                        if (mGoogleAccount == null)  mGoogleAccount = result.getSignInAccount();
                        if (mGoogleAccount != null) {
                            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), mGoogleAccount.getId());
                            int requestType = mSavedStateViewModel.getColorID();
                            if (requestType == 0) userPrefs.setReadDailyPermissions(true);
                            if (requestType == 1) userPrefs.setReadSessionsPermissions(true);
                            if (requestType == 2) userPrefs.setReadSessionsPermissions(true);
                            int workType = mMessagesViewModel.getWorkType();
                            if (workType != 0){
                                if (workType == Constants.TASK_ACTION_DAILY_SUMMARY) doDailySummaryJob();
                                else doAsyncGoogleFitAction(workType,mWorkout,mWorkoutSet);
                            }
                        }else
                            signIn();
                    }else
                        signIn();

                }else
                    quitApp();
                break;
            case REQUEST_SIGN_IN:
                if (resultCode != RESULT_OK)
                    quitApp();
                else {
                    try {
                        authInProgress = false;
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        mGoogleAccount = task.getResult(ApiException.class);
                        if (mGoogleAccount != null) {
                            appPrefs.setLastUserID(mGoogleAccount.getId());
                            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), mGoogleAccount.getId());
                            onSignIn(false);
                            doHomeFragmentRefresh();
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
                                                            Log.i(LOG_TAG, "Firebase " + mFirebaseUser.getDisplayName());
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                            else
                                Log.i(LOG_TAG, "no idToken on login " + mGoogleAccount.getDisplayName());
                        }else
                            signIn();
                    } catch (ApiException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        signInError(e);
                    }
                }
                break;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Context context = getApplicationContext();
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    appPrefs.setUseLocation(true);
                    mMessagesViewModel.setUseLocation(true);
                    onSignIn(false);
                } else {
                    doSnackbar(getString(R.string.action_no_location),Snackbar.LENGTH_LONG);
                    appPrefs.setUseLocation(false);
                    mMessagesViewModel.setUseLocation(false);
                }
            }else{
                doSnackbar(getString(R.string.action_no_location),Snackbar.LENGTH_LONG);
                appPrefs.setUseLocation(false);
                mMessagesViewModel.setUseLocation(false);
            }
          //  if (appPrefs.getUseLocation() || appPrefs.getUseSensors()) startSensorWorkerAlarm(-1);
        }
/*        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
            String sMsg = getResources().getString(R.string.sensors_permission_granted);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                appPrefs.setUseSensors(true);
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
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)) {
                    if (hasOAuthPermission(0)) {
                        if (appPrefs.getUseLocation()) {
                          bindLocationListener();
                        }
                    }else
                        requestOAuthPermission(0);
                }else{
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG,getString(R.string.recog_permission_reason),null);
                    return;
                }
            }
            else {
                appPrefs.setUseSensors(false);
                sMsg = getString(R.string.no_permission_okay);
            }
            doSnackbar(sMsg,Snackbar.LENGTH_LONG);
        }
*/
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOG){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED)) {
                    onSignIn(false);
                } else
                    doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason),null);
            } else {
                String sMsg = getString(R.string.no_permission_okay);
                broadcastToast(sMsg);
            }
        }
        if (requestCode == PERMISSION_REQUEST_BODY_CAMERA){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE){
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    Uri PersonPhotoUri = (mGoogleAccount != null) ? mGoogleAccount.getPhotoUrl():null;
                    String sFile = userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE);
                    if (PersonPhotoUri != null && (sFile.length() == 0)) {
                       doImageWork(PersonPhotoUri, 0);
                    }else
                        dispatchPickImageIntent();
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        indexScroll = i;
        // position the pageViewer properly
        //MarginLayoutParamsCompat layoutParamsCompat = (MarginLayoutParamsCompat) mViewPager.get
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        try {
            final int action = ev.getActionMasked();
            final int source = ev.getSource();
            if (source != SOURCE_UNKNOWN){
                if (source == R.id.action_use_grid){
                    Log.i(LOG_TAG, "grid pushed ");
                    return true;
                }
            }
            final int currentItem = mViewPager.getCurrentItem();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (currentItem == 0) {

                    }
                    if ((currentItem > 0) && (currentItem < 6)) {
                        try {
/*                            PageFragment pageFragment = (PageFragment) mAdapter.getFragment(currentItem);
                            if (pageFragment != null) {
                                if (indexScroll == 0) {
                                      pageFragment.setSwipeToRefreshEnabled(true);
                                      pageFragment.refreshData();
                                } else {
                                      pageFragment.setSwipeToRefreshEnabled(false);
                                }
                            }*/
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if ((currentItem > 0) && (currentItem < 6))  {
                        try {
/*                            PageFragment pageFragment = (PageFragment) mAdapter.getFragment(currentItem);
                            if (pageFragment != null) {
                                if (indexScroll == 0) {
                                    pageFragment.setSwipeToRefreshEnabled(true);
                                } else {
                                    pageFragment.setSwipeToRefreshEnabled(false);
                                }
                            }*/
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    break;
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onBackStackChanged()
    {
        try {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() == 0 && overlay.getVisibility() == View.VISIBLE) {      //manager.getBackStackEntryCount() == 0 &&
                overlay.setAlpha(0f);
                int state = (mSavedStateViewModel != null) ? mSavedStateViewModel.getState() : WORKOUT_INVALID;
                if (state == WORKOUT_PENDING) startExactTimeAlarm(INTENT_HOME_REFRESH, WORKOUT_INVALID,0,TimeUnit.SECONDS.toMillis(5));
                //     floatingActionMenu.collapse();
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onBackPressed() {
        long t = SystemClock.elapsedRealtime();
        Context context = getApplicationContext();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(SettingsDialog.TAG) != null){
            SettingsDialog frag = (SettingsDialog)fragmentManager.findFragmentByTag(SettingsDialog.TAG);
            if (frag != null)
                fragmentManager.beginTransaction().remove(frag).commit();
            Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
            refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
            refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
            mMessagesViewModel.addLiveIntent(refreshIntent);
        }
        if (overlay.getVisibility() == View.VISIBLE) {      //manager.getBackStackEntryCount() == 0 &&
            overlay.setAlpha(0f);
          //  floatingActionMenu.collapse();
        }
        MapsFragment mapFragment = (MapsFragment)fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment != null){
            fragmentManager.beginTransaction().remove(mapFragment).commit();
            return;
        }
        if (fragmentManager.getBackStackEntryCount() > 0){
            fragmentManager.popBackStack();
            return;
        }
        // at HomeFragment now
        if (t - backPressedTime > 2000) {    // 2 secs
            backPressedTime = t;
            broadcastToast(getString(R.string.app_check_exit));
        } else {    // this guy is serious
            boolean bSetup = appPrefs.getAppSetupCompleted();
            String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
            int state = mSavedStateViewModel.getState();
            // clean up
            if ((userPrefs != null) && (sUserId.length() > 0) && userPrefs.getConfirmExitApp()) {
                if ((state == WORKOUT_LIVE) || (state == WORKOUT_PAUSED))
                    showAlertDialogConfirm(Constants.ACTION_STOP_QUIT);
                else
                    showAlertDialogConfirm(Constants.ACTION_EXITING);
            }else{
                appBarLayout.removeOnOffsetChangedListener(this);
                getSupportFragmentManager().removeOnBackStackChangedListener(this);
                if (mGoogleAccount != null) {
                    Wearable.getCapabilityClient(context).removeListener(mCapabilityListener);
                }
                mGoogleAccount = null;
                // bye
                super.onBackPressed();
            }
        }
    }




    private void crossFadeIn(View contentView) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        // final int idView = contentView.getId();
        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                    }
                });

    }
    private void crossFadeOut(View contentView) {
        contentView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentView.setVisibility(View.GONE);
                    }
                });

    }

    private boolean checkApplicationSetup(){
        Context context = getApplicationContext();
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        boolean bSetup = (appPrefs.getLastUserID().length() > 0) && (ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED);
        if (bSetup){
            bSetup = (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if (bSetup){
                bSetup = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
                if (bSetup) {
                    if ((userPrefs == null) && (appPrefs.getLastUserID().length() > 0))
                        userPrefs = UserPreferences.getPreferences(context, appPrefs.getLastUserID());
                    if (userPrefs != null) {
                        bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                       // if (bSetup) bSetup = appPrefs.getPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED);
                      //  if (bSetup && Utilities.hasSpeaker(context)) bSetup = appPrefs.getPrefByLabel(AP_PREF_AUDIO_ASKED);
                      //  if (bSetup && Utilities.hasVibration(context)) bSetup = appPrefs.getPrefByLabel(AP_PREF_VIBRATE_ASKED);
                    }else
                        bSetup = false;
                }
            }
        }
        return bSetup;
    }
    private void createWorkout(String sUserID, String sDeviceID){
        mWorkout = new Workout();
        long timeMs = System.currentTimeMillis();
        mWorkout._id = (timeMs);
        mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
        mWorkout.userID = (sUserID.length() > 0) ? sUserID : null;
        mWorkout.deviceID = sDeviceID;
    }

    private void createWorkoutMeta(){
        // start fresh and re-initialise the controls
        mWorkoutMeta = new WorkoutMeta();
        long timeMs = System.currentTimeMillis();
        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        mWorkoutMeta._id = (timeMs);
        if ((mWorkout != null) && (mWorkout._id > 1)) {
            mWorkoutMeta._id = mWorkout._id;
            mWorkoutMeta.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            sUserId = mWorkout.userID;
            if (mWorkout.activityID > 0) {
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.identifier = mWorkout.identifier;
                mWorkoutMeta.packageName = mWorkout.packageName;
            }
            if (mWorkout.start > 0){
                mWorkoutMeta.start = mWorkout.start;
                if (mWorkout.end > 0){
                    mWorkoutMeta.end = mWorkout.end;
                    mWorkoutMeta.duration = mWorkout.duration;
                }
            }
        }
        if (sUserId.length() > 0){
            mWorkoutMeta.userID = sUserId;
            mWorkoutMeta.deviceID = sDeviceId;
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
        }
    }
    //
    //TODO: check the setCount mechanism here!
    //
    private void createWorkoutSet(){
        // start fresh and re-initialise the controls
        mWorkoutSet = new WorkoutSet();
        long timeMs = System.currentTimeMillis();
        mWorkoutSet._id = (timeMs);
        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        if (mWorkout != null) {
            mWorkoutSet.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            sUserId = mWorkout.userID;
            if ((mWorkout.activityID != null) && mWorkout.activityID > 0) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }
            mWorkout.setCount++;
            if (mWorkout.setCount > 0) mWorkoutSet.setCount = mWorkout.setCount;
        }
        if (sUserId.length() > 0) mWorkoutSet.userID = sUserId;
        if (sDeviceId.length() > 0) mWorkoutSet.deviceID = sDeviceId;

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w(LOG_TAG,"onNewIntent " + ((intent.getAction() != null) ? intent.getAction() : intent.toString()));
        mStartupIntent = intent;
        handleIntent(intent);
    }


    private class TaskSendSensoryInfo implements Runnable{
        String sHost;
        String sUserId;
        String sDeviceID;
        int currentState;
        UserDailyTotals UDT;
        SensorDailyTotals SDT;
        long lastLogin;
        ApplicationPreferences myAppPrefs;
        
        TaskSendSensoryInfo(Context context, String sHost, String sUser, String sDevice, long lastLogin,int state ,SensorDailyTotals sdt, UserDailyTotals udt){
            this.sHost = sHost;
            this.sUserId = sUser;
            this.sDeviceID = sDevice;
            this.SDT = sdt;
            this.UDT = udt;
            this.lastLogin = lastLogin;
            this.currentState = state;
            this.myAppPrefs = ApplicationPreferences.getPreferences(context);
        }
        @Override
        public void run() {
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_DAILY_UPDATE);
            dataMap.putString(Constants.KEY_FIT_USER, this.sUserId);
            dataMap.putString(KEY_FIT_DEVICE_ID, this.sDeviceID);
            dataMap.putLong(KEY_FIT_VALUE, this.lastLogin);
            dataMap.putInt(KEY_FIT_ACTION, this.currentState);  // current state
            String sKey;
            if (SDT != null) {
                sKey = SensorDailyTotals.class.getSimpleName();
                dataMap.putLong(sKey,SDT._id);
                sKey += "_last";
                dataMap.putLong(sKey, SDT.lastUpdated);
                if ((myAppPrefs.getStepsSensorCount() >= 0) && (SDT.deviceStep != 0)) {
                    sKey = DataType.TYPE_STEP_COUNT_DELTA.getName();
                    dataMap.putInt(sKey, SDT.deviceStep);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastDeviceStep);
                }
                if ((myAppPrefs.getBPMSensorCount() >= 0) && (SDT.deviceBPM > 0)) {
                    sKey = DataType.TYPE_HEART_RATE_BPM.getName();
                    dataMap.putFloat(sKey, SDT.deviceBPM);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastDeviceBPM);
                }
                if ((myAppPrefs.getPressureSensorCount() >= 0) && (SDT.pressure > 0)) {
                    sKey = Sensor.STRING_TYPE_PRESSURE;
                    dataMap.putFloat(sKey, SDT.pressure);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastDeviceOther);
                }
                if ((myAppPrefs.getHumiditySensorCount() >= 0) && (SDT.humidity > 0)) {
                    sKey = Sensor.STRING_TYPE_RELATIVE_HUMIDITY;
                    dataMap.putFloat(sKey, SDT.humidity);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastDeviceOther);
                }
                if ((myAppPrefs.getTempSensorCount() >= 0) && (SDT.temperature > 0)) {
                    sKey = Sensor.STRING_TYPE_AMBIENT_TEMPERATURE;
                    dataMap.putFloat(sKey, SDT.temperature);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastDeviceOther);
                }
                if (SDT.lastActivityType > 0){
                    sKey = DetectedActivity.class.getSimpleName();
                    dataMap.putInt(sKey, SDT.activityType);
                    sKey += "_last";
                    dataMap.putLong(sKey, SDT.lastActivityType);
                }
            }
            if (UDT != null) {
                sKey = UserDailyTotals.class.getSimpleName();
                dataMap.putLong(sKey,UDT._id);
                sKey += "_last";
                dataMap.putLong(sKey, UDT.lastUpdated);
                if (UDT.heartIntensity >= 0)
                    dataMap.putFloat(DataType.TYPE_HEART_POINTS.getName(), UDT.heartIntensity);
                if (UDT.distanceTravelled >= 0)
                    dataMap.putFloat(DataType.TYPE_DISTANCE_DELTA.getName(), UDT.distanceTravelled);
                if (UDT.caloriesExpended >= 0)
                    dataMap.putFloat(DataType.TYPE_CALORIES_EXPENDED.getName(), UDT.caloriesExpended);
                if (UDT.activeMinutes >= 0)
                    dataMap.putInt(DataType.TYPE_MOVE_MINUTES.getName(), UDT.activeMinutes);
                if ((UDT.lastLocation != null) && (UDT.lastLocation.length() > 0)) {
                    dataMap.putString(DataType.TYPE_LOCATION_SAMPLE.getName(), UDT.lastLocation);
                }
            }
            if (this.sHost == null)
                sendCapabilityMessage(WEAR_CAPABILITY_NAME, MESSAGE_PATH_WEAR_SERVICE, dataMap);
            else
                sendMessage(sHost, MESSAGE_PATH_WEAR_SERVICE, dataMap);
        }
    }

    private class TaskSendSettingsWear implements Runnable{
        private int requestDirection;
        private String sUser;
        TaskSendSettingsWear(int dir, String sUserID){ this.requestDirection = dir; this.sUser = sUserID; }

        @Override
        public void run() {
            if (sUser == null || sUser.length() == 0) return;
            Log.w(LOG_TAG, "running share settings direction " + requestDirection);
            DataMap dataMapResponse = new DataMap();
            dataMapResponse.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_SETUP_INFO);
            dataMapResponse.putString(Constants.KEY_FIT_USER, sUser);
            dataMapResponse.putInt(Constants.KEY_FIT_TYPE, requestDirection);
            dataMapResponse.putInt(KEY_FIT_VALUE, 0);
            if (requestDirection == 0) { // we send to them - else we request from them == 1
                String sAge = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                if (sAge.length() > 0)
                    dataMapResponse.putString(Constants.INTENT_PERMISSION_AGE, sAge);
                String sHeight = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                if (sHeight.length() > 0)
                    dataMapResponse.putString(Constants.INTENT_PERMISSION_HEIGHT, sHeight);
                dataMapResponse.putBoolean(Constants.AP_PREF_ASK_AGE, userPrefs.getAskAge());
                dataMapResponse.putBoolean(Constants.AP_PREF_USE_KG, userPrefs.getUseKG());
                dataMapResponse.putBoolean(Constants.AP_PREF_USE_TIMED_REST, userPrefs.getTimedRest());
                dataMapResponse.putBoolean(Constants.AP_PREF_USE_TIMED_AUTO_START, userPrefs.getRestAutoStart());
                dataMapResponse.putBoolean(LABEL_DEVICE_USE, userPrefs.getPrefByLabel(LABEL_DEVICE_USE));
                dataMapResponse.putBoolean(LABEL_DEVICE_ASKED, userPrefs.getPrefByLabel(LABEL_DEVICE_ASKED));
                dataMapResponse.putBoolean(Constants.USER_PREF_CONF_START_SESSION, userPrefs.getConfirmStartSession());
                dataMapResponse.putBoolean(Constants.USER_PREF_CONF_END_SESSION, userPrefs.getConfirmEndSession());
                dataMapResponse.putBoolean(Constants.USER_PREF_CONF_SET_SESSION, userPrefs.getConfirmSetSession());
                dataMapResponse.putBoolean(Constants.USER_PREF_CONF_DEL_SESSION, userPrefs.getConfirmDeleteSession());
                dataMapResponse.putBoolean(Constants.USER_PREF_CONF_EXIT_APP, userPrefs.getConfirmExitApp());
                dataMapResponse.putBoolean(Constants.USER_PREF_USE_ROUND_IMAGE, userPrefs.getUseRoundedImage());
                dataMapResponse.putInt(Constants.USER_PREF_GYM_REST_DURATION, userPrefs.getWeightsRestDuration());
                dataMapResponse.putLong(Constants.USER_PREF_SHOOT_REST_DURATION, userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_REST_DURATION));
                dataMapResponse.putLong(Constants.USER_PREF_SHOOT_CALL_DURATION, userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_CALL_DURATION));
                dataMapResponse.putLong(Constants.USER_PREF_SHOOT_END_DURATION, userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_END_DURATION));
                dataMapResponse.putInt(Constants.USER_PREF_DEF_NEW_SETS, userPrefs.getDefaultNewSets());
                dataMapResponse.putInt(Constants.USER_PREF_DEF_NEW_REPS, userPrefs.getDefaultNewReps());

                dataMapResponse.putLong(Constants.USER_PREF_BPM_SAMPLE_RATE, userPrefs.getBPMSampleRate());
                dataMapResponse.putLong(Constants.USER_PREF_STEP_SAMPLE_RATE, userPrefs.getStepsSampleRate());
                dataMapResponse.putLong(Constants.USER_PREF_OTHERS_SAMPLE_RATE, userPrefs.getOthersSampleRate());
                dataMapResponse.putBoolean(Constants.AP_PREF_USE_LOCATION, appPrefs.getUseLocation());
                dataMapResponse.putBoolean(Constants.AP_PREF_USE_SENSORS, appPrefs.getUseSensors());
                dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_PHONE, appPrefs.getPhoneSyncInterval());
                dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT, appPrefs.getLastSyncInterval());
                dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_DAILY, appPrefs.getDailySyncInterval());
                dataMapResponse.putLong(Constants.AP_PREF_SYNC_INT_NETWORK, appPrefs.getNetworkCheckInterval());
                Long lHistoryStart = 0L;
                Long lHistoryEnd = 0L;
                Configuration configHistory = null;
                List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUser);
                if (existingConfigs.size() > 0) {
                    configHistory = existingConfigs.get(0);
                    lHistoryStart = Long.parseLong(configHistory.stringValue1);
                    lHistoryEnd = Long.parseLong(configHistory.stringValue2);
                    dataMapResponse.putLong(Constants.MAP_HISTORY_START, lHistoryStart);
                    dataMapResponse.putLong(Constants.MAP_HISTORY_END, lHistoryEnd);
                }
                existingConfigs.clear();
                existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.KEY_DEVICE1, sUser);
                if (existingConfigs.size() > 0) {
                    configHistory = existingConfigs.get(0);
                    dataMapResponse.putString(KEY_FIT_DEVICE_ID, configHistory.stringValue);
                    dataMapResponse.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    dataMapResponse.putString(Constants.KEY_DEVICE1, configHistory.stringValue1);
                    dataMapResponse.putString(Constants.KEY_DEVICE2, configHistory.stringValue2);
                }
            }
            if (wearNodeId == null || wearNodeId.length() == 0)
                sendCapabilityMessage(WEAR_CAPABILITY_NAME,Constants.MESSAGE_PATH_WEAR_SERVICE, dataMapResponse);
            else
                sendMessage(wearNodeId, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMapResponse);
        }
    }

    private class TaskSyncWearNode implements Runnable{
        String sUserId;
        String sDeviceID;
        long timeMs;

        TaskSyncWearNode(String sUser, String sDevice){
            sUserId = sUser;
            sDeviceID = sDevice;
        }
        @Override
        public void run() {
            timeMs = System.currentTimeMillis();
            long lastSync = appPrefs.getLastPhoneSync();
            long syncInterval = appPrefs.getPhoneSyncInterval();
            if ((userPrefs == null) || !userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) return;
            if ((sUserId.length() > 0) && (sDeviceID.length() > 0))
                if (((timeMs - lastSync) > syncInterval)) {
                    appPrefs.setLastPhoneSync(timeMs);
                    List<TableTuple> list = mSessionViewModel.getTableTuples(null, sUserId, sDeviceID); // all tables
                    if (list != null && list.size() > 0) {
                        for (TableTuple tuple : list) {
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_TABLE_INFO);
                            dataMap.putString(KEY_FIT_ACTION, INTENT_PHONE_REQUEST);  // give me data greater than my max date
                            dataMap.putString(KEY_FIT_USER, sUserId);
                            dataMap.putString(Constants.KEY_FIT_REC, Constants.WEAR_DATA_ITEM_RECEIVED_PATH); // return path for CustomWearListenerService
                            dataMap.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                            dataMap.putString(Constants.MAP_DATA_TYPE, tuple.table_name);
                            dataMap.putLong(Constants.MAP_COUNT, tuple.sync_count);
                            dataMap.putLong(Constants.MAP_START, tuple.mindate);
                            dataMap.putLong(Constants.MAP_END, tuple.maxdate);
                            if ((wearNodeId != null) && (wearNodeId.length() > 0))
                                sendMessage(wearNodeId, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
                            else
                                sendCapabilityMessage(Constants.WEAR_CAPABILITY_NAME, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
                        }
                    }
                    else {
                        DataMap dataMap = new DataMap();
                        dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                        dataMap.putInt(KEY_FIT_TYPE, (int) 0); // new request
                        dataMap.putString(KEY_FIT_USER, this.sUserId);
                        dataMap.putString(KEY_FIT_DEVICE_ID, this.sDeviceID);
                        if (mGoogleAccount != null)
                            dataMap.putString(KEY_FIT_NAME, mGoogleAccount.getDisplayName());
                        if (wearNodeId == null || wearNodeId.length() == 0)
                            wearNodeId = appPrefs.getLastNodeID();
                        if (wearNodeId.length() != 0)
                            sendMessage(wearNodeId, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
                        else
                            sendCapabilityMessage(Constants.WEAR_CAPABILITY_NAME, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
                    }
                }
            startWearPendingSyncAlarm();
        }
    }


    private void startWearPendingSyncAlarm(){
        if (userPrefs == null || !userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) return;
        final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        final String sDeviceId = mSavedStateViewModel.getDeviceID();
        if ((sUserId == null) || (sUserId.length() == 0) || (sDeviceId.length() == 0)) return;
        long timeMs = System.currentTimeMillis();
        long lastSync = appPrefs.getLastPhoneSync();
        long syncInterval = appPrefs.getPhoneSyncInterval();
        if (syncInterval == 0) syncInterval =  (TimeUnit.MINUTES.toMillis(5));
        long triggerTimeMs = timeMs + syncInterval;
      //  boolean isConnected = (appPrefs.getLastNodeSync() > 0);
        Intent syncCheckIntent = new Intent(Constants.INTENT_PHONE_SYNC);
        syncCheckIntent.putExtra(KEY_FIT_TYPE,1); // not triggered intent
        syncCheckIntent.putExtra(KEY_FIT_USER, sUserId);
        syncCheckIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
        syncCheckIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
        PendingIntent mPhoneSyncPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_PHONE_SYNC_CODE, syncCheckIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if ((lastSync + syncInterval) <= timeMs)  // check don't wait
            mMessagesViewModel.addLiveIntent(syncCheckIntent);
        else {
            mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mPhoneSyncPendingIntent);
            Log.e(LOG_TAG, "setting WEAR sync alarm");
        }
    }

    private void doHomeFragmentRefresh(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (Fragment frag : fragmentManager.getFragments()){
            if (frag instanceof HomeFragment && (frag.isVisible())){
                final Context context = getApplicationContext();
                new Handler(Looper.getMainLooper()).post(() ->{
                    ((HomeFragment) frag).doObserveWorkoutSet(mWorkoutSet,context);
                    ((HomeFragment) frag).loadDataAndUpdateScreen();
                    ((HomeFragment) frag).setLocationText();
                });
            }
        }
    }
    private void doGetUseWearApp(){
        final ICustomConfirmDialog callback = new ICustomConfirmDialog() {
            @Override
            public void onCustomConfirmDetach() {
            }

            @Override
            public void onCustomConfirmButtonClicked(int question, int button) {
                if (button > 0) {
                    userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, true);
                    startWearPendingSyncAlarm();
                }else {
                    userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, false);
                    mMessagesViewModel.setPhoneAvailable(false);
                }
                cancelNotification(INTENT_SETUP);
                if (!appPrefs.getAppSetupCompleted()) {
                    appPrefs.setAppSetupCompleted(true);
                    createWorkout(appPrefs.getLastUserID(), appPrefs.getDeviceID());
                    mWorkout._id = 2;
                    mWorkout.start = 0;
                    mWorkout.end = 0;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    broadcastToast(getString(R.string.label_setup_complete));
                }
            }
        };
        if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED)) {
            doConfirmDialog(Constants.QUESTION_DEVICE, getString(R.string.has_device_text), callback);
        }else
            if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) startWearPendingSyncAlarm();
    }

    private void doRegisterExerciseDetectionService(GoogleSignInAccount gsa){
        Context context = getApplicationContext();
        Intent intentDetected = new Intent(context, ExerciseDetectedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intentDetected,PendingIntent.FLAG_UPDATE_CURRENT);

        DataUpdateListenerRegistrationRequest registrationRequest = new DataUpdateListenerRegistrationRequest.Builder()
                .setDataType(DataType.TYPE_WEIGHT)
                .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                .setPendingIntent(pendingIntent).build();
        Fitness.getHistoryClient(context,gsa).registerDataUpdateListener(registrationRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(LOG_TAG, "added ex detection listener " + task.isSuccessful());
            }
        });

    }
    private void doUnRegisterExerciseDetectionService(GoogleSignInAccount gsa){
        Context context = getApplicationContext();
        Intent intentDetected = new Intent(context, ExerciseDetectedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intentDetected,PendingIntent.FLAG_UPDATE_CURRENT);
        Task<Void> unregisterTask = Fitness.getHistoryClient(context, gsa).unregisterDataUpdateListener(pendingIntent);
        unregisterTask.addOnCompleteListener(task -> Log.d(LOG_TAG, "unregistered exercise listener " + task.isSuccessful()));
    }

    private class TaskCheckMissingMetaData implements Runnable{
        String sUserId;

        TaskCheckMissingMetaData(String u){ sUserId = u;}

        @Override
        public void run() {
            List<OneTimeWorkRequest> requestList = new ArrayList<>();
            List<DateTuple> missingList = mSessionViewModel.getWorkoutMissingTypeList(sUserId, Constants.OBJECT_TYPE_WORKOUT_AGG);
            if (missingList != null && (missingList.size() > 0)){
                Log.e(LOG_TAG,"missing object aggregates " + missingList.size());
                for(DateTuple dt: missingList){
                    Data.Builder builder = new Data.Builder();
                    builder.putString(Constants.KEY_FIT_USER, sUserId);
                    builder.putLong(Constants.KEY_FIT_WORKOUTID, dt.sync_count);
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    requestList.add(workRequest);
                }
            }else
                Log.w(LOG_TAG,"No missing object aggregates ");
            List<DateTuple> missingMeta = mSessionViewModel.getWorkoutMissingTypeList(sUserId, OBJECT_TYPE_WORKOUT_META);
            if (missingMeta != null && (missingMeta.size() > 0)){
                Log.e(LOG_TAG,"missing meta data " + missingMeta.size());
                for(DateTuple dt: missingMeta){
                    Data.Builder builder = new Data.Builder();
                    builder.putInt(FitnessMetaWorker.ARG_ACTION_KEY, Constants.TASK_ACTION_STOP_SESSION); // all
                    builder.putLong(FitnessMetaWorker.ARG_START_KEY, dt.mindate);
                    builder.putLong(FitnessMetaWorker.ARG_END_KEY, dt.maxdate);
                    builder.putLong(FitnessMetaWorker.ARG_WORKOUT_ID_KEY, dt.sync_count);
                    builder.putLong(FitnessMetaWorker.ARG_SET_ID_KEY, 0);
                    builder.putString(Constants.KEY_FIT_USER, sUserId);
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    requestList.add(workRequest);
                }
            }else
                Log.w(LOG_TAG,"No missing meta data ");
            if (requestList.size() > 0){
                Log.e(LOG_TAG,"TaskCheckMissingMetaData requestList size: " + requestList.size());
                if (mWorkManager == null) mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(requestList);
            }
        }
    }

    /**
        doStartUpJobs -

     **/
    private class TaskStartupJobs implements Runnable{
        String sUserId;
        String sDeviceId;
        long timeMs;
        long previousTimeMs;
        String sAction;

        TaskStartupJobs(Intent loginIntent){
            sUserId = loginIntent.getStringExtra(KEY_FIT_USER);
            sDeviceId = loginIntent.getStringExtra(KEY_FIT_DEVICE_ID);
            previousTimeMs = loginIntent.getLongExtra(KEY_FIT_VALUE, 0);
            sAction = loginIntent.getAction();
        }
        @Override
        public void run() {
            Context mContext = getApplicationContext();
            if ((sUserId == null) || (sUserId.length() == 0)) return;
            int currentState = mSavedStateViewModel.getState();
            timeMs = System.currentTimeMillis();
            mCalendar.setTimeInMillis(timeMs);
            int iDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
            if (!appPrefs.getAppSetupCompleted()) return;
            long lastDaily = appPrefs.getLastDailySync();
            long dailyInterval = appPrefs.getDailySyncInterval();
            Log.e(LOG_TAG, "running startup job state now " + currentState);
            boolean bConnected = mReferenceTools.isNetworkConnected();
            if (!bConnected) {
                startNetworkCheckAlarm(mContext);
                Log.w(LOG_TAG, "getting current totals");
                Data.Builder builder = new Data.Builder();
                builder.putString(KEY_FIT_USER, sUserId);
                builder.putString(KEY_FIT_DEVICE_ID, sDeviceId);
                builder.putInt(KEY_FIT_TYPE, 2); // everything
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(CurrentUserDailyTotalsWorker.class)
                                .setInputData(builder.build())
                                .build();
                if (mWorkManager == null)
                    mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(workRequest);
            }

            Configuration configDevice1 = null;
            Configuration configDevice2 = null;
            Configuration configState = null;
            Configuration configParam = new Configuration(ATRACKIT_EMPTY,sUserId,ATRACKIT_EMPTY,0L,null,null);
            List<Configuration> configList = mSessionViewModel.getConfiguration(configParam, sUserId);
            if ((configList != null) && (configList.size() > 0)) {
                for(Configuration c: configList){
                    if (c.stringName.equals(KEY_DEVICE1)) configDevice1 = c;
                    if (c.stringName.equals(KEY_DEVICE2)) configDevice2 = c;
                    if (c.stringName.equals(MAP_CURRENT_STATE)) configState = c;
                }
            }
            /*if (configState != null){
                currentState = Math.toIntExact(configState.longValue);
                Log.e(LOG_TAG, "running startup job picked up config state " + currentState);
            }*/
            if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)) {
                Log.w(LOG_TAG, "startup current state not live " + currentState);
                if (((dailyInterval > 0) && ((lastDaily == 0) || (timeMs - lastDaily) > dailyInterval)) || sAction.equals(Constants.INTENT_DAILY) && bConnected) {
                    Intent mIntent = new Intent(getApplicationContext(), DailySummaryJobIntentService.class);
                    mIntent.putExtra(Constants.KEY_FIT_USER, sUserId);
                    mIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
                    mIntent.putExtra(Constants.KEY_FIT_TYPE, 1);  // force
                    if (!userPrefs.getReadDailyPermissions())
                        if (!hasOAuthPermission(0)) {
                            mMessagesViewModel.setWorkType(Constants.TASK_ACTION_DAILY_SUMMARY); // daily type
                            requestOAuthPermission(0);
                        }else
                            userPrefs.setReadDailyPermissions(true);
                    if (userPrefs.getReadDailyPermissions()) {
                        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(getApplicationContext(), mReferenceTools.getFitnessSignInOptions(0));
                        mIntent.putExtra(KEY_FIT_VALUE, gsa);
                        mIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        DailySummaryJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                    } else {
                        mMessagesViewModel.setWorkType(Constants.TASK_ACTION_DAILY_SUMMARY); // daily type
                        requestOAuthPermission(0);
                    }
                }
                else if ((dailyInterval > 0) && sAction.equals(Constants.INTENT_DAILY)) {
                    startDailySummarySyncAlarm();
                    Log.w(LOG_TAG, "getting current totals");
                    Data.Builder builder = new Data.Builder();
                    builder.putString(KEY_FIT_USER, sUserId);
                    builder.putString(KEY_FIT_DEVICE_ID, sDeviceId);
                    builder.putInt(KEY_FIT_TYPE, 2); // everything
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(CurrentUserDailyTotalsWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    if (mWorkManager == null)
                        mWorkManager = WorkManager.getInstance(getApplicationContext());
                    mWorkManager.enqueue(workRequest);
                }
                if (bConnected && ((timeMs - previousTimeMs) > TimeUnit.MINUTES.toMillis(5))) {
                    Log.e(LOG_TAG, "startupJob connected so startCloudPendingSyncAlarm");
                    startCloudPendingSyncAlarm();
                }
                if (currentState != WORKOUT_INVALID) {
                    Log.e(LOG_TAG, "currentState is not INVALID about to INTENT_HOME_REFRESH");
                    // this will trigger the sensors setup via state change to invalid
                    Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                    refreshIntent.putExtra(KEY_FIT_USER, sUserId);
                    refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                    mMessagesViewModel.addLiveIntent(refreshIntent);
                }
                if (TimeUnit.MILLISECONDS.toMinutes(timeMs - previousTimeMs) > 5)
                    AppExecutors.getInstance().diskService().submit(new TaskCheckMissingMetaData(sUserId));

                // if setup and viable && userPrefs.getReadDailyPermissions()
                if ((appPrefs.getAppSetupCompleted() && ((userPrefs != null)
                        && userPrefs.getReadSensorsPermissions())
                        && (sDeviceId.length() > 0)
                        && (mGoogleAccount != null))) {
                    if (previousTimeMs > 0) mCalendar.setTimeInMillis(previousTimeMs);
                    int prevDOY = (previousTimeMs > 0) ? mCalendar.get(Calendar.DAY_OF_YEAR) : 0;
                    // each day
                    if (Math.abs(iDOY - prevDOY) > 1) {
                        List<Configuration> userConfigs = mSessionViewModel.getConfigurationLikeName(Constants.KEY_GOOGLE_LIKE, sUserId);
                        boolean bGoalsSet = false;
                        long lastChecked = appPrefs.getLastGoalSync();
                        if (userConfigs != null)
                            for (Configuration config : userConfigs) {
                                String sName = config.stringName;
                                if (sName.equals(DataType.AGGREGATE_MOVE_MINUTES.getName()) || sName.equals(DataType.AGGREGATE_HEART_POINTS.getName())
                                        || sName.equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
                                    lastChecked = config.longValue;
                                    bGoalsSet = true;
                                    break;
                                }
                            }
                        // reset the check every 15 days
                        if (bGoalsSet && ((lastChecked == 0) || TimeUnit.DAYS.convert(timeMs - lastChecked, TimeUnit.MILLISECONDS) > 15))
                            bGoalsSet = false;
                        // find goals
                        if (appPrefs.getAppSetupCompleted() && ((userPrefs != null) && userPrefs.getReadDailyPermissions()) && !bGoalsSet && bConnected)
                            doGoalsRefresh();

                        doPendingExercisesCheck(sUserId);
                        Log.w(LOG_TAG, "setting counters");
                        DailyCounter bpmCounter = new DailyCounter();
                        bpmCounter.LastUpdated = timeMs;
                        bpmCounter.FirstCount = 0;
                        mSavedStateViewModel.setBPM(bpmCounter);
                        DailyCounter stepCounter = new DailyCounter();
                        stepCounter.LastUpdated = timeMs;
                        if ((appPrefs.getStepsSensorCount() > 0) && (mMessagesViewModel.getDeviceStepsMsg().getValue() != null)) {
                            try {
                                int iExistingSteps = Integer.parseInt(mMessagesViewModel.getDeviceStepsMsg().getValue());
                                stepCounter.FirstCount = iExistingSteps;
                            } catch (NumberFormatException ne) {
                                stepCounter.FirstCount = 0;
                            }
                        } else
                            stepCounter.FirstCount = 0;
                        mSavedStateViewModel.setSteps(stepCounter);
                    }
                    if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) {
                        if (wearNodeId == null || (wearNodeId.length() == 0))
                            wearNodeId = appPrefs.getLastNodeID();
                        if (configDevice1 != null) {
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                            if ((configDevice2 == null) || (configDevice2.longValue == 0)) {
                                dataMap.putInt(KEY_FIT_TYPE, (int) 1); // new setup indicator
                                dataMap.putString(KEY_PAYLOAD, MESSAGE_PATH_PHONE_SERVICE);  // return to my service!
                            } else {
                                dataMap.putInt(KEY_FIT_TYPE, (int) 0); // check connection alive!
                                dataMap.putString(KEY_PAYLOAD, MESSAGE_PATH_PHONE);  // return to me!
                            }
                            dataMap.putString(KEY_FIT_USER, mGoogleAccount.getId());
                            dataMap.putString(KEY_FIT_DEVICE_ID, configDevice1.stringValue);
                            dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                            dataMap.putLong(KEY_FIT_TIME, configDevice1.longValue);
                            dataMap.putLong(KEY_FIT_VALUE, appPrefs.getLastSync());
                            dataMap.putString(Constants.KEY_DEVICE1, configDevice1.stringValue1);
                            dataMap.putString(Constants.KEY_DEVICE2, configDevice1.stringValue2);
                            if (wearNodeId == null || (wearNodeId.length() == 0))
                                sendCapabilityMessage(WEAR_CAPABILITY_NAME, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
                            else
                                sendMessage(wearNodeId, MESSAGE_PATH_WEAR_SERVICE, dataMap);
                        }else
                            startWearPendingSyncAlarm();
                    }
                    // check for new Daily
                    // not live so get daily stats and cloud sync
                } else {
                    if (!userPrefs.getReadDailyPermissions()) {
                        if (!hasOAuthPermission(0))
                            requestOAuthPermission(0);
                        else
                            userPrefs.setReadDailyPermissions(true);
                    } else if (!userPrefs.getReadSensorsPermissions())
                        startSplashActivityForResult(INTENT_PERMISSION_SENSOR);
                }
            }
        }
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

            confirmDialog = CustomConfirmDialog.newInstance(questionType, sQuestion, ((callback == null)? MainActivity.this : callback));
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

    private void doGoalsRefresh(){
        if (!mReferenceTools.isNetworkConnected())
            doAlertMessage(ATRACKIT_EMPTY,getString(R.string.action_offline),2000);
        else
            if (!hasOAuthPermission(2))
                requestOAuthPermission(2);
            else
                new Handler().postDelayed(() -> {
                        Workout w = new Workout();
                        w.userID = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
                        w.deviceID = mSavedStateViewModel.getDeviceID();
                        w.start = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_YEAR);
                        w.end = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                        if (w.userID != null && w.userID.length() > 0)
                            doAsyncGoogleFitAction(Constants.TASK_ACTION_READ_GOALS, w, new WorkoutSet(w));
                }, 1500L);
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
    private void doDailySummaryJob(){
        long delay_sync = appPrefs.getDailySyncInterval();
        int currentState = mSavedStateViewModel.getState();
        String userId = (mGoogleAccount != null) ? mGoogleAccount.getId() : appPrefs.getLastUserID();
        if (delay_sync > 0 && (currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED))
            CustomIntentReceiver.setAlarm(getApplicationContext(),true, delay_sync, userId, (Parcelable)mGoogleAccount);
    }
    private void startDailySummarySyncAlarm(){
        int currentState = mSavedStateViewModel.getState();
        if (currentState == WORKOUT_LIVE || currentState == WORKOUT_PAUSED) return;

        if ((userPrefs != null) && !userPrefs.getReadDailyPermissions()){
            if (hasOAuthPermission(0)){
                userPrefs.setReadDailyPermissions(true);
            }else {
                requestOAuthPermission(0);
                return;
            }
        }
        long timeMs = System.currentTimeMillis();
        long syncInterval = appPrefs.getDailySyncInterval();
        long lastSync = appPrefs.getLastDailySync();
        String userId = (mGoogleAccount != null) ? mGoogleAccount.getId() : appPrefs.getLastUserID();
        Context context = getApplicationContext();
        if (syncInterval == 0){
            CustomIntentReceiver.cancelAlarm(context);
        }else
            CustomIntentReceiver.setAlarm(context,((timeMs - lastSync) >= syncInterval), syncInterval, userId, (Parcelable)mGoogleAccount);  // repeat
    }


    private void doAsyncGoogleFitAction(int action, Workout workout, WorkoutSet set){
        if (workout != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
            Log.d(LOG_TAG, "doAsyncGoogleFitAction " + action + " Range Start: " + dateFormat.format(workout.start));
            Log.d(LOG_TAG, "Range End: " + dateFormat.format(workout.end));
        }else{
            return;
        }

        mMessagesViewModel.setWorkType(action);
        int optionsType = 0;
        if (action == TASK_ACTION_EXER_SEGMENT || action == TASK_ACTION_ACT_SEGMENT)
            optionsType=1; // data write
        if (action == TASK_ACTION_READ_GOALS)
            optionsType=2; // data read
        if (action == TASK_ACTION_START_SESSION || action == TASK_ACTION_STOP_SESSION)
            optionsType=5;
        if (action == TASK_ACTION_SYNC_WORKOUT)
            optionsType = 5; // everything

        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (optionsType == 0 || (mGoogleAccount == null)) return;
        if (!hasOAuthPermission(optionsType)) {
            Log.e(LOG_TAG, "doAsyncGoogleFitAction no permssions" + optionsType + " action "  + action);
            requestOAuthPermission(optionsType);
            return;
        }
        else{
            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(getApplicationContext(), mReferenceTools.getFitnessSignInOptions(optionsType));
            if (gsa.isExpired()){
                Log.e(LOG_TAG, "doAsyncGoogleFitAction GSA null exiting!!!");
                return;
            }
        }
        // test we are not busy already!
        if (action == TASK_ACTION_SYNC_WORKOUT){
            List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.ATRACKIT_SETUP,Constants.ATRACKIT_ATRACKIT_CLASS);
            if (configList != null && (configList.size() > 0)){
                Configuration configSetup = configList.get(0);
                if (configSetup.stringValue1 != null && configSetup.stringValue1.length() > 0){
                    Log.e(LOG_TAG, "doAsyncGoogleFitAction Sync already running - not starting new");
                    return;
                }
            }
        }

        final ResultReceiver mFitResultReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                mMessagesViewModel.setWorkInProgress(false);
                if (resultCode == 200){
                    mMessagesViewModel.setWorkType(0);
                    final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
                    if (sUserId == null || sUserId.length() == 0) return;
                    final String sDeviceID = mSavedStateViewModel.getDeviceID();
                    final long timeMs = System.currentTimeMillis();
                    final int successIndicator = (resultData.containsKey(KEY_FIT_VALUE)) ? resultData.getInt(KEY_FIT_VALUE) : 0;
                    // async history task callbacks!
                    if (resultData.containsKey(Constants.KEY_FIT_ACTION)){
                        int historyAction = resultData.getInt(Constants.KEY_FIT_ACTION);
                        if (historyAction == Constants.TASK_ACTION_READ_GOALS && (successIndicator > 0) && (sUserId.length() > 0)){
                            appPrefs.setLastGoalSync(System.currentTimeMillis());
                            for(int i=0; i < 3; i++){
                                String recKey = KEY_FIT_REC + i;
                                String recType = KEY_FIT_TYPE + i;
                                String recValue = KEY_FIT_VALUE + i;
                                String recName = KEY_FIT_NAME + i;
                                if (resultData.containsKey(recKey)){
                                    String sKey = resultData.getString(recKey);
                                    String sValue = resultData.getString(recValue);
                                    int iType = resultData.getInt(recType);
                                    String sName = resultData.getString(recName);
                                    try {
                                        List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(sKey,sUserId);
                                        if (existingConfigs.size() == 0) {
                                            Configuration config = new Configuration(sKey, sUserId, sValue, timeMs, Integer.toString(iType),sName);
                                            mSessionViewModel.insertConfig(config);
                                        }else {
                                            Configuration config = existingConfigs.get(0);
                                            config.stringValue = sValue;
                                            config.longValue = timeMs;
                                            config.stringValue1 =  Integer.toString(iType);
                                            config.stringValue2 = sName;
                                            mSessionViewModel.updateConfig(config);
                                        }
                                    }catch(Exception e){
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                }
                            }
                            broadcastToast(getString(R.string.label_goals_updated));
                        }
                        if (historyAction == FitSyncJobIntentService.READ_SEGMENT_DETAILS){
                            String sKey = Workout.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                Workout routine = resultData.getParcelable(sKey);
                                ArrayList<Workout> workouts = new ArrayList<>();
                                workouts.add(routine);
                                mSessionViewModel.setWorkoutList(workouts);
                                //       sendNotification(INTENT_SUMMARY_WORKOUT, resultData);
                            }else{
                                sKey = Constants.KEY_LIST_WORKOUTS;
                                if (resultData.containsKey(sKey)) {
                                    ArrayList<Workout> workouts = new ArrayList<>();
                                    workouts.addAll(resultData.getParcelableArrayList(sKey));
                                    mSessionViewModel.setWorkoutList(workouts);
                                    //    CustomListFragment  customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY,  0);
                                }
                            }
                            sKey = WorkoutSet.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                WorkoutSet workoutSet = resultData.getParcelable(sKey);
                                ArrayList<WorkoutSet> sets = new ArrayList<>();
                                sets.add(workoutSet);
                                 mSavedStateViewModel.setToDoSets(sets);
                                //           sendNotification(INTENT_SUMMARY_WORKOUT, resultData);
                            }else{
                                sKey = Constants.KEY_LIST_SETS;
                                if (resultData.containsKey(sKey)) {
                                    ArrayList<WorkoutSet> sets = new ArrayList<>();
                                    sets.addAll(resultData.getParcelableArrayList(sKey));
                                     mSavedStateViewModel.setToDoSets(sets);
                                    CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY,  0, sUserId,sDeviceID);
                                }
                            }
                        }
                        if (historyAction == TASK_ACTION_SYNC_WORKOUT){
                            if (resultData.containsKey(KEY_LIST_SETS)) doPendingExercisesCheck(sUserId);
                            broadcastToast("Sync completed");
                            appPrefs.setLastSync(timeMs);
                        }
                        if (historyAction == TASK_ACTION_STOP_SESSION || historyAction == TASK_ACTION_INSERT_HISTORY){
                            if (historyAction == TASK_ACTION_STOP_SESSION)
                                if (successIndicator > 0) broadcastToast("TASK_ACTION_STOP_SESSION successful"); else  broadcastToast("TASK_ACTION_STOP_SESSION failed");
                            if (historyAction == TASK_ACTION_INSERT_HISTORY)
                                if (successIndicator > 0) broadcastToast("TASK_ACTION_INSERT_HISTORY successful"); else  broadcastToast("TASK_ACTION_INSERT_HISTORY failed");
                            if (resultData.containsKey(Workout.class.getSimpleName())){
                                Workout w = resultData.getParcelable(Workout.class.getSimpleName());
                                if (historyAction == TASK_ACTION_STOP_SESSION){
                                    Data.Builder builder2 = new Data.Builder();
                                    builder2.putInt(FitnessMetaWorker.ARG_ACTION_KEY, Constants.TASK_ACTION_STOP_SESSION); // all
                                    builder2.putLong(FitnessMetaWorker.ARG_START_KEY, w.start);
                                    builder2.putLong(FitnessMetaWorker.ARG_END_KEY, w.end);
                                    builder2.putLong(FitnessMetaWorker.ARG_WORKOUT_ID_KEY, w._id);
                                    builder2.putLong(FitnessMetaWorker.ARG_SET_ID_KEY, 0);
                                    builder2.putString(KEY_FIT_USER, w.userID);
                                    builder2.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                    OneTimeWorkRequest oneTimeWorkRequest =
                                            new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                                                    .setInputData(builder2.build())
                                                    .setInitialDelay(2, TimeUnit.SECONDS)
                                                    .setConstraints(constraints).addTag(LOG_TAG)
                                                    .build();
                                    mWorkManager.enqueue(oneTimeWorkRequest);
                                }
                            }
                        }
                        if (historyAction == TASK_ACTION_RECORD_START){
                            if (successIndicator > 0) broadcastToast("Recording started"); else  broadcastToast("Recording failed to start");
                        }
                        if (historyAction == TASK_ACTION_RECORD_END){
                            if (successIndicator > 0) broadcastToast("Recording stop"); else  broadcastToast("Recording failed to stop");
                        }
                    }
                    else{
                        String sKey = Workout.class.getSimpleName();
                        if (resultData.containsKey(sKey)) {
                            Workout routine = resultData.getParcelable(sKey);
                            ArrayList<Workout> workouts = new ArrayList<>();
                            workouts.add(routine);
                            mSessionViewModel.setWorkoutList(workouts);
                            CustomListFragment  customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY,  0, sUserId,sDeviceID);
                            //customListFragment.setCancelable(true);
                            //customListFragment.show(getSupportFragmentManager(), "Workout");
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.add(customListFragment, customListFragment.TAG);
                            transaction.commit();

                        }
                        else {
                            sKey = Constants.KEY_LIST_WORKOUTS;
                            if (resultData.containsKey(sKey)) {
                                ArrayList<Workout> workouts = new ArrayList<>();
                                workouts.addAll(resultData.getParcelableArrayList(sKey));
                                mSessionViewModel.setWorkoutList(workouts);
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY, 0, sUserId,sDeviceID);
                                //customListFragment.setCancelable(true);
                                //customListFragment.show(getSupportFragmentManager(), "Workouts");
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.add(customListFragment, customListFragment.TAG);
                                transaction.commit();

                            }
                            sKey = WorkoutSet.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                WorkoutSet workoutSet = resultData.getParcelable(sKey);
                                ArrayList<WorkoutSet> sets = new ArrayList<>();
                                sets.add(workoutSet);
                                 mSavedStateViewModel.setToDoSets(sets);
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sUserId,sDeviceID);
                                //customListFragment.setCancelable(true);
                                //customListFragment.show(getSupportFragmentManager(), "Sets");
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.add(customListFragment, customListFragment.TAG);
                                transaction.commit();

                            }
                            else {
                                sKey = Constants.KEY_LIST_SETS;
                                if (resultData.containsKey(sKey)) {
                                    ArrayList<WorkoutSet> sets = new ArrayList<>();
                                    sets.addAll(resultData.getParcelableArrayList(sKey));
                                     mSavedStateViewModel.setToDoSets(sets);
                                    CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sUserId,sDeviceID);
                                  //  customListFragment.setCancelable(true);
                                   // customListFragment.show(getSupportFragmentManager(), "Sets");
                                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.add(customListFragment, customListFragment.TAG);
                                    transaction.commit();

                                }
                            }
                        }

                    }
                }
                else {
                    Log.d(LOG_TAG, "doAsyncGoogleFitAction internal receiver " + resultCode);
                    if (resultCode == 201 || resultCode == 400){
                        int actionInt = resultData.containsKey(Constants.KEY_FIT_ACTION) ? resultData.getInt(Constants.KEY_FIT_ACTION) : mMessagesViewModel.getWorkType();
                        if (mReferenceTools.isNetworkConnected()){
                            final Intent newIntent = new Intent();
                            newIntent.putExtra(Constants.KEY_FIT_ACTION, actionInt);
                            newIntent.putExtra(Constants.MAP_START, resultData.getLong(MAP_START));
                            newIntent.putExtra(Constants.MAP_END, resultData.getLong(MAP_END));
                            newIntent.putExtra(KEY_FIT_USER, resultData.getString(KEY_FIT_USER));
                            newIntent.putExtra(KEY_FIT_DEVICE_ID, resultData.getString(KEY_FIT_DEVICE_ID));
                            if (resultData.containsKey(Workout.class.getSimpleName())) {
                                Workout w = resultData.getParcelable(Workout.class.getSimpleName());
                                if (w != null) newIntent.putExtra(Workout.class.getSimpleName(), w);
                            }
                            if (resultData.containsKey(WorkoutSet.class.getSimpleName())) {
                                WorkoutSet s = resultData.getParcelable(WorkoutSet.class.getSimpleName());
                                if (s != null) newIntent.putExtra(WorkoutSet.class.getSimpleName(), s);
                            }
                            ResultReceiver rr = resultData.getParcelable(KEY_FIT_REC);
                            if (rr != null) newIntent.putExtra(Constants.KEY_RESULT, rr);
                            GoogleSignInAccount gsa = resultData.getParcelable(KEY_PAYLOAD);
                            if (gsa == null || gsa.isExpired() && !authInProgress){
                                authInProgress = true;
                                Log.e(LOG_TAG, "about to silent signIn action  " + actionInt);
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(Constants.CLIENT_ID)
                                        .requestEmail().build();
                                // Build a GoogleSignInClient with the options specified by gso.
                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                                Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
                                signInAccountTask.addOnCompleteListener(task -> {
                                    authInProgress = false;
                                    if (task.isSuccessful()){
                                        Log.e(LOG_TAG, "SUCCESS silent signIn action  " + actionInt);
                                        GoogleSignInAccount acct = task.getResult();
                                        newIntent.putExtra(Constants.KEY_PAYLOAD,acct);
                                    }else
                                        Log.e(LOG_TAG, "FAILED silent signIn action  " + actionInt);
                                });
                            }else
                                newIntent.putExtra(Constants.KEY_PAYLOAD,gsa);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(LOG_TAG, "fitSyncJob restarting action  " + actionInt);
                                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), newIntent);
                                }
                            },5000);
                        }
                        else{
                            if (actionInt == TASK_ACTION_SYNC_WORKOUT) startCloudPendingSyncAlarm();
                            else broadcastToast(getString(R.string.action_offline));
                        }
                    }
                }
            }
        };  // [end of - ResultReceiver resultReceiver  ]
        try{
                long startTime = workout.start;
                if (startTime == 0) startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                long endTime = workout.end;
                if (endTime == 0) endTime = System.currentTimeMillis();
                Intent mIntent = new Intent();
                mIntent.putExtra(Constants.KEY_FIT_ACTION, action);

                mIntent.putExtra(Constants.MAP_START, startTime);
                mIntent.putExtra(Constants.MAP_END, endTime);
                mIntent.putExtra(Constants.KEY_RESULT, mFitResultReceiver);
                mIntent.putExtra(KEY_FIT_USER, workout.userID);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, workout.deviceID);
                mIntent.putExtra(Workout.class.getSimpleName(), workout);
                if (set != null) mIntent.putExtra(WorkoutSet.class.getSimpleName(), set);
                mMessagesViewModel.setWorkType(action);
                mMessagesViewModel.setWorkInProgress(true);
                try{
                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                }catch (Exception e){
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage() + "doAsyncGoogleFitAction  history action " + action + Constants.LINE_DELIMITER);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void doPendingExercisesCheck(String sUserId){
        DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
        MenuItem menuItem = mBottomNavigationView.getMenu().getItem(2);
        BadgeDrawable badgeDrawable = mBottomNavigationView.getOrCreateBadge(menuItem.getItemId());
        int pendingCount = Math.toIntExact(exerciseTuple.sync_count);
        if (pendingCount == 0){
            if (badgeDrawable != null){
                badgeDrawable.setVisible(false);
                badgeDrawable.clearNumber();
            }
        }else{
            if (badgeDrawable != null) {
                badgeDrawable.setNumber(pendingCount);
                badgeDrawable.setVisible(true);
            }
            Bitmap ic_launcher = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            int notificationID = Constants.NOTIFICATION_MAINTAIN_ID;
            long iconID = R.drawable.noti_white_logo;
            int activityIcon = R.drawable.ic_gym_equipmemt;
            String sTitle = exerciseTuple.sync_count + " exercises require setup for analysis";
            String sContent = Constants.ATRACKIT_EMPTY;
            String sTime = SimpleDateFormat.getTimeInstance().format(mCalendar.getTime());
            String sChannelID = MAINTAIN_CHANNEL_ID;

            List<Exercise> pendingList = mSessionViewModel.getPendingExercises();
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.setAction(Constants.INTENT_EXERCISE_PENDING);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            notificationIntent.putExtra(KEY_FIT_TYPE, 1);  // pending exercise lookup for ExerciseActivity
            notificationIntent.putExtra(KEY_FIT_USER, sUserId);
            notificationIntent.putExtra(KEY_FIT_WORKOUTID, pendingList.get(0)._id);
            notificationIntent.putExtra(Exercise.class.getSimpleName(), pendingList.get(0));

            PendingIntent notificationPendingIntent = PendingIntent.getActivity
                    (getApplicationContext(), notificationID, notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionExercise = new NotificationCompat.Action.Builder(activityIcon,
                    getString(R.string.action_new_exercise), notificationPendingIntent).build();

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(sTitle);
            inboxStyle.setSummaryText(sContent);
            for(Exercise exercise : pendingList) {
                inboxStyle.addLine(exercise.name);
            }
            NotificationCompat.Builder notifyBuilder = new NotificationCompat
                    .Builder(getApplicationContext(), sChannelID)
                    .setStyle(inboxStyle)
                    .setContentTitle(sTitle)
                    .setContentText(sContent)
                    .setSmallIcon((int) iconID)
                    .setLargeIcon(ic_launcher)
                    .setShowWhen(true)
                    .setStyle(inboxStyle)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .addAction(actionExercise)
                    .setContentIntent(notificationPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            try{
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                // Deliver the notification.
                if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder.build());
            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage());
                FirebaseCrashlytics.getInstance().recordException(e);
            }

        }
    }

    private void doSnackbar(String sMsg, int length){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.top_container), sMsg, length).setAction(getString(R.string.action_okay)
                        , view -> {return;}).show();
            }
        });

    }
    private void doAlertDialogMessage(String sMessage, Runnable toRun){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_confirmation,null,false);
        TextView title = view.findViewById(R.id.confirm_message);
        ImageButton btnPositive = view.findViewById(R.id.PositiveButton);
        ImageButton btnNegative = view.findViewById(R.id.NegativeButton);
        title.setText(sMessage);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        btnPositive.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
            toRun.run();
        });
        btnNegative.setVisibility(View.GONE);
        alertDialog.show();
    }
    private void doAlertMessage(String sTitle ,String sMessage, long lDelay){
       final AlertDialog  mAlertDialog = new MaterialAlertDialogBuilder(MainActivity.this,  R.style.Widget_MyApp_CustomAlertDialog).create();
      //  mAlertDialog.setIcon(R.drawable.noti_white_logo);
        if (sMessage.length() > 0) mAlertDialog.setMessage(sMessage);
        if (sTitle.length() > 0) mAlertDialog.setTitle(sTitle);
        if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
        if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() ->{ mAlertDialog.hide(); mAlertDialog.dismiss();}, lDelay);
        try {
            mAlertDialog.show();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "doAlertMessage " + e.getMessage());
        }
    }
    private void doPauseStopDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_pausestop,null,false);
        TextView title = view.findViewById(R.id.pause_title);
        //title.setText("User Goals");
        MaterialButton btnContinue = view.findViewById(R.id.btn_confirm_paused);
        MaterialButton btnStop = view.findViewById(R.id.btn_confirm_stop);
        Chronometer chronometerConfirm = view.findViewById(R.id.chronometer_confirm_pause);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        btnContinue.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
            // start current set !!!!
            Intent start_setIntent = new Intent(INTENT_ACTIVE_RESUMED);
            if (userPrefs.getConfirmStartSession())
                start_setIntent.putExtra(KEY_FIT_TYPE, 1);  //ask
            else
                start_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
            start_setIntent.putExtra(KEY_FIT_VALUE, (mWorkoutSet.end == 0) ? 0 : 1); // start next indicator = start current
            if (mWorkout != null)
                start_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
            if (mWorkoutSet != null)
                start_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
            mMessagesViewModel.addLiveIntent(start_setIntent);

        });
        btnStop.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
            // start current set !!!!
            Intent start_setIntent = new Intent(INTENT_ACTIVE_STOP);
            if (userPrefs.getConfirmEndSession())
                start_setIntent.putExtra(KEY_FIT_TYPE, 1);  //ask
            else
                start_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
            if (mWorkout != null)
                start_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
            if (mWorkoutSet != null)
                start_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
            mMessagesViewModel.addLiveIntent(start_setIntent);

        });
        long startElapsed = mSavedStateViewModel.getPauseStart();
        if (startElapsed == 0){
            startElapsed = SystemClock.elapsedRealtime();
            mSavedStateViewModel.setPauseStart(startElapsed);
        }
        chronometerConfirm.setBase(startElapsed);
        chronometerConfirm.start();
        alertDialog.show();
    }
    private void doSkuLookupJob(final String sUserId, final String sDeviceId){
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                long timeMs = System.currentTimeMillis();
                if (resultCode == 200){
                    if (resultData.containsKey(SkuJobIntentService.class.getSimpleName())) {
                        String processed = resultData.getString(SkuJobIntentService.class.getSimpleName());
                        broadcastToast("Product Check  " + processed);
                    }
                }else {
                    if (resultData.containsKey(SkuJobIntentService.class.getSimpleName())) {
                        String processed = resultData.getString(SkuJobIntentService.class.getSimpleName());
                        broadcastToast("FAILED Product Check  " + processed);
                        quitApp();
                    }
                }
            }
        };
        long timeMs = System.currentTimeMillis();
        Context context = getApplicationContext();
        if (context != null) {
            long lastSKU = appPrefs.getLastSKUCheck();
            if (TimeUnit.MILLISECONDS.toDays(timeMs-lastSKU) >= SKU_CHECK_MIN_DAYS) {
                try {
                    Intent jobIntent = new Intent(context, SkuJobIntentService.class);
                    jobIntent.putExtra(Constants.KEY_FIT_REC, resultReceiver);
                    jobIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
                    jobIntent.putExtra(KEY_FIT_USER, sUserId);
                    jobIntent.putExtra("base_product", "base_product");
                    jobIntent.putExtra("firebase_product", "firebase_product");
                    jobIntent.putExtra("phone_product", "phone_product");
                    SkuJobIntentService.enqueueWork(context, jobIntent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "doSkuLookupJob " + e.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
    }
    private void  doNetworkCheck(){
        Context context = getApplicationContext();
        boolean bCurrentlyAvail = (mMessagesViewModel.getCloudAvailable().getValue() != null) ? mMessagesViewModel.getCloudAvailable().getValue() : false;
        if (context != null) {
            mReferenceTools.init(context);
            long timeMs = System.currentTimeMillis();
            if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
            if (appPrefs != null) appPrefs.setLastNetworkCheck(timeMs);
            if (mReferenceTools.isNetworkConnected()) {
                if (!bCurrentlyAvail) {
                    mMessagesViewModel.setCloudAvailable(true);
                    Log.e(LOG_TAG, "NETWORK BACK ONLINE " + Utilities.getTimeString(timeMs));
                    startCloudPendingSyncAlarm();
                }
            } else {
                mMessagesViewModel.setCloudAvailable(false);
                startNetworkCheckAlarm(context);
            }
        }
    }    
    private void doImageWork(Uri getAddress, int type){
        final Context context = getApplicationContext();
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() == 0) return;
        if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserId);
        Data.Builder builder = new Data.Builder();
        if (getAddress != null) {
            builder.putString(KEY_IMAGE_URI, getAddress.toString());
            builder.putInt(KEY_FIT_TYPE, type);
            builder.putString(KEY_FIT_USER, sUserId);
            Data inputData = builder.build();
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest imageRequest =
                    new OneTimeWorkRequest.Builder(ImageWorker.class)
                            .setInputData(inputData)
                            .setConstraints(constraints)
                            .build();
            WorkManager mWorkManager = WorkManager.getInstance(context);
            mWorkManager.enqueue(imageRequest);
            mWorkManager.beginWith(imageRequest).getWorkInfosLiveData().observe(this, workInfos -> {
                if (workInfos.size() > 0) {
                    WorkInfo workInfo = workInfos.get(0);
                    boolean finished = workInfo.getState().isFinished();
                    if (!finished) {
                        //showWorkInProgress();
                    } else {
                        // showWorkFinished();
                        Data outputData = workInfo.getOutputData();
                        String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);
                        if (!TextUtils.isEmpty(outputImageUri)) {
                            switch(type){
                                case 0:
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_LOGO);
                                    break;
                                case 1:
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_INT_FILE);
                                    break;
                                case 2:
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_CAMERA_FILE);
                                    break;
                                case 3:
                                    userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_EXT_FILE);
                                    break;
                            }
                            userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, outputImageUri);
                            Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                            refreshIntent.putExtra(KEY_FIT_VALUE, outputImageUri);
                            refreshIntent.putExtra(KEY_FIT_USER, outputData.getString(Constants.KEY_FIT_USER));
                            mMessagesViewModel.addLiveIntent(refreshIntent);
                        }
                    }
                }
            });
        }
    }
/*
    private void doRecordingWork(int action){
        FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(3);
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions)) {
            if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            GoogleSignIn.requestPermissions(
                    this,
                    RC_REQUEST_RECORDING_AND_CONTINUE_SUBSCRIPTION,
                    mGoogleAccount,
                    fitnessOptions);
        } else {
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
            mWorkManager.beginWith(oneTimeWorkRequest).getWorkInfosLiveData().observe(this, workInfos -> {
                if (workInfos.size() > 0) {
                    WorkInfo workInfo = workInfos.get(0);
                    boolean finished = workInfo.getState().isFinished();
                    if (!finished) {
                        Log.d(LOG_TAG, "recording changed not finished");
                        //showWorkInProgress();
                    } else {
                        broadcastToast("live tracking " + ((action == 1) ? "started" : "completed"));
                    }
                }
            });
        }
    }

    private void doMetaWorkerAction(int action, long startTime, long endTime, long workoutID, long setID, String sUserID){
        Data.Builder builder = new Data.Builder();
        builder.putInt(FitnessMetaWorker.ARG_ACTION_KEY, action); // all
        builder.putLong(FitnessMetaWorker.ARG_START_KEY, startTime);
        builder.putLong(FitnessMetaWorker.ARG_END_KEY, endTime);
        builder.putLong(FitnessMetaWorker.ARG_WORKOUT_ID_KEY, workoutID);
        builder.putLong(FitnessMetaWorker.ARG_SET_ID_KEY, setID);
        builder.putString(Constants.KEY_FIT_USER, sUserID);
        Data inputData = builder.build();
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
        Operation operation = mWorkManager.beginWith(oneTimeWorkRequest).enqueue();
        operation.getState().observe(MainActivity.this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                if (state instanceof Operation.State.SUCCESS) {
                    Log.d(LOG_TAG, "successful state of meta worker ");
                    mSessionViewModel.updateLastUpdate(sUserID, workoutID,OBJECT_TYPE_WORKOUT,2);
                }
            }
        });
    }*/


    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannels() {

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


    private void sendNotification(String intentString, Bundle dataBundle) {
        // Sets up the pending intent to update the notification.
        int notificationID = 0;
        long iconID = R.drawable.noti_white_logo;
        int activityIcon = R.drawable.ic_running_white;
        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_SPACE;
        long timeMs = System.currentTimeMillis();
        String sTime = Utilities.getTimeString(timeMs);
        String sChannelID = Constants.ATRACKIT_EMPTY;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        boolean bIsGym = false; boolean bIsShooting = false; boolean bIsTennis = false;
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
       // int colorDayNight = (!bDayMode) ? getColor(R.color.primaryTextColor) : getColor(R.color.primaryDarkColor);
        int iconColor = getColor(R.color.primaryLightColor);
        Context context = getApplicationContext();
        mReferenceTools.init(context);

        Drawable icon_outlined = AppCompatResources.getDrawable(context, R.drawable.ic_a_outlined);
        Utilities.setColorFilter(icon_outlined,iconColor);
        if (INTENT_SETUP.equals(intentString)) {
            Intent viewIntent = new Intent(context, MainActivity.class);
            viewIntent.setAction(INTENT_SETUP);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            notificationID = NOTIFICATION_MAINTAIN_ID;
            sTitle = getString(R.string.action_setup_running);
            sContent = getString(R.string.app_name);
            sChannelID = MAINTAIN_CHANNEL_ID;
            PendingIntent pendingViewIntent = PendingIntent.getActivity(context, notificationID, viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action actionView5 =
                    new NotificationCompat.Action.Builder(R.drawable.ic_a_outlined,
                            getString(R.string.action_open), pendingViewIntent)
                            .build();
         //   NotificationCompat.WearableExtender extender5 = new NotificationCompat. WearableExtender().addAction(actionView5);

            NotificationCompat.Builder notifyBuilder5 = new NotificationCompat
                    .Builder(context, sChannelID)
                    .setContentTitle(sTitle)
                    .setContentText(sContent)
                    .setSmallIcon(R.drawable.ic_a_outlined)
                    .setLargeIcon(Utilities.vectorToBitmap(icon_outlined))
                    .setUsesChronometer(false)
                    .setOngoing(true)
                    .setContentInfo(getString(R.string.action_setup_running))
                    .setAutoCancel(true)
              //      .extend(extender5)
                    .addAction(actionView5)
                    .setContentIntent(pendingViewIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            try {
                    notificationManager.notify(notificationID, notifyBuilder5.build());
            } catch(Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, e.getMessage());
            }
            return;
        }
        String sUserID = (dataBundle.containsKey(KEY_FIT_USER) ? dataBundle.getString(KEY_FIT_USER) : ATRACKIT_EMPTY);
        String sDeviceID = (dataBundle.containsKey(KEY_FIT_DEVICE_ID)) ? dataBundle.getString(KEY_FIT_DEVICE_ID) : ATRACKIT_EMPTY;
        if ((sUserID == null) || (sUserID.length() == 0)) return;
        if ((sDeviceID == null) || (sDeviceID.length() == 0)) return;
        if (INTENT_DAILY.equals(intentString) || INTENT_SUMMARY_DAILY.equals(intentString)){
            notificationID = NOTIFICATION_SUMMARY_ID;
            Intent viewIntent = new Intent(context, MainActivity.class);
            viewIntent.setAction(Intent.ACTION_VIEW);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            viewIntent.putExtra(KEY_FIT_USER, sUserID);
            viewIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            viewIntent.setAction(intentString);
            PendingIntent pendingViewIntent = PendingIntent.getActivity
                    (getApplicationContext(), notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch, context.getString(R.string.action_open), pendingViewIntent).build();
            NotificationCompat.Builder notifyBuilder =  getNotificationBuilder(intentString, dataBundle);
            notifyBuilder.addAction(actionView);
            notifyBuilder.setContentIntent(pendingViewIntent);
            try {
                if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder.build());
            } catch(Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, e.getMessage());
            }
            return;
        }


        if (intentString.equals(INTENT_ACTIVE_START) || intentString.equals(INTENT_ACTIVESET_START) || intentString.equals(INTENT_ACTIVE_PAUSE)
        || intentString.equals(INTENT_ACTIVE_RESUMED) || intentString.equals(INTENT_ACTIVE_STOP)){
            Intent viewIntent = new Intent(context, MainActivity.class);
            viewIntent.setAction(INTENT_ACTIVE_RESUMED);
            viewIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            viewIntent.putExtra(KEY_FIT_TYPE, 1);
            viewIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            viewIntent.putExtra(KEY_FIT_USER, sUserID);
            Intent pauseIntent = new Intent(context, MainActivity.class);
            pauseIntent.setAction(INTENT_ACTIVE_PAUSE);
            pauseIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pauseIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            pauseIntent.putExtra(KEY_FIT_USER, sUserID);
            Intent stopIntent = new Intent(context, MainActivity.class);
            stopIntent.setAction(INTENT_ACTIVE_STOP);
            stopIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            stopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stopIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            stopIntent.putExtra(KEY_FIT_TYPE, 1);
            stopIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            stopIntent.putExtra(KEY_FIT_USER, sUserID);
            Workout workout = (dataBundle.containsKey(Workout.class.getSimpleName())) ? dataBundle.getParcelable(Workout.class.getSimpleName()) : null;
            WorkoutSet set = (dataBundle.containsKey(WorkoutSet.class.getSimpleName())) ? dataBundle.getParcelable(WorkoutSet.class.getSimpleName()) : null;

            if (mReferenceTools.getFitnessActivityIconResById(workout.activityID) > 0)
                activityIcon = mReferenceTools.getFitnessActivityIconResById(workout.activityID);
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), activityIcon);
            Utilities.setColorFilter(drawable, iconColor);
            Bitmap bitmap = Utilities.vectorToBitmap(drawable);


            if (workout == null)
                if (dataBundle.containsKey(KEY_FIT_WORKOUTID)){
                    long ID = dataBundle.getLong(KEY_FIT_WORKOUTID,0);
                    if (ID > 0){
                        workout = mSessionViewModel.getWorkoutById(ID, sUserID, sDeviceID);
                    }
                }

            if (workout != null) {
                if (sUserID.length() == 0) sUserID = workout.userID;
                if (workout.activityID > 0) {
                    bIsGym = Utilities.isGymWorkout(workout.activityID);
                    bIsShooting = Utilities.isShooting(workout.activityID);
                    bIsTennis = (workout.activityID == WORKOUT_TYPE_TENNIS);
                }
                if (bIsGym && (set == null)){
                    if (dataBundle.containsKey(KEY_FIT_WORKOUT_SETID)){
                        long ID2 = dataBundle.getLong(KEY_FIT_WORKOUT_SETID,0);
                        if (ID2 > 0){
                            set = mSessionViewModel.getWorkoutSetById(ID2, sUserID, sDeviceID);
                        }
                    }
                }

                viewIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
                if (set != null) viewIntent.putExtra(KEY_FIT_WORKOUT_SETID, set._id);
                viewIntent.putExtra(KEY_FIT_TYPE, 1); // starting from notification flag
                viewIntent.putExtra(Constants.KEY_FIT_ACTIVITYID, workout.activityID);
                pauseIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
                if (set != null) pauseIntent.putExtra(KEY_FIT_WORKOUT_SETID, set._id);
                pauseIntent.putExtra(KEY_FIT_TYPE, 1);
                // Build the notification with all of the parameters using helper
                stopIntent.putExtra(KEY_FIT_TYPE, 1); // starting from notification flag
                if (set != null) stopIntent.putExtra(KEY_FIT_WORKOUT_SETID, set._id);
                stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
            }
            switch (intentString) {
                case INTENT_ACTIVE_PAUSE:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    PendingIntent pauseResumePendingIntent = PendingIntent.getActivity(context,
                            notificationID, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pauseViewPendingIntent = PendingIntent.getActivity(context,
                            notificationID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action actionPauseView = new NotificationCompat.Action.Builder((bDayMode ? R.drawable.ic_play_button_black :R.drawable.ic_play_button_white),
                            getString(R.string.action_resume), pauseResumePendingIntent).build();
                    // Create the action
                    PendingIntent pauseStopPendingIntent = PendingIntent.getActivity(context,
                            notificationID, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action actionPauseFinish =
                            new NotificationCompat.Action.Builder(R.drawable.ic_stop_circle_black,
                                    getString(R.string.action_finish), pauseStopPendingIntent)
                                    .build();
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sChannelID = ACTIVE_CHANNEL_ID;
                    NotificationCompat.Builder notifyBuilderPause =  getNotificationBuilder(intentString, dataBundle);
                    // Show controls on lock screen even when user hides sensitive content.
                    notifyBuilderPause.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    notifyBuilderPause.addAction(actionPauseView).addAction(actionPauseFinish);
                    notifyBuilderPause.setLargeIcon(bitmap);
                    notifyBuilderPause.setSmallIcon(activityIcon);
                    try{
                        // Deliver the notification.
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilderPause.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_ACTIVE_START:
                case INTENT_ACTIVE_RESUMED:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    PendingIntent viewPendingIntent = PendingIntent.getActivity(context,
                            notificationID, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pausePendingIntent = PendingIntent.getActivity(context,
                            notificationID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch,getString(R.string.action_open), viewPendingIntent).build();
                    // Create the action
                    NotificationCompat.Action actionPause =
                            new NotificationCompat.Action.Builder(((!bDayMode) ? R.drawable.ic_rounded_pause_button_white: R.drawable.ic_rounded_pause_button_black),
                                    getString(R.string.action_pause), pausePendingIntent)
                                    .build();

                    PendingIntent stopPendingIntent = PendingIntent.getActivity(context,
                            notificationID, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Action actionFinish =
                            new NotificationCompat.Action.Builder(((bDayMode) ? R.drawable.ic_stop_circle_black : R.drawable.ic_stop_circle_white),
                                    getString(R.string.action_finish), stopPendingIntent)
                                    .build();
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sChannelID = ACTIVE_CHANNEL_ID;

                    // NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionPause).addAction(actionFinish);
                    // extender.setContentIcon(Math.toIntExact(iconID));
                    NotificationCompat.Builder notifyBuilder =  getNotificationBuilder(intentString, dataBundle);
                    // Show controls on lock screen even when user hides sensitive content.
                    notifyBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    notifyBuilder.setLargeIcon(bitmap);
                    notifyBuilder.setSmallIcon(activityIcon);
                    notifyBuilder.addAction(actionView).addAction(actionPause).addAction(actionFinish);
                    if (bIsGym || bIsShooting){
                        notifyBuilder.setProgress(mWorkout.setCount,mWorkoutSet.setCount,false);
                    }
                    if (intentString.equals(INTENT_ACTIVE_PAUSE)){
                        //notifyBuilder.setPriority(Notification.)
                    }
                    //notifyBuilder.setProgress()
                    // notifyBuilder.extend(extender);
                    try{
                        // Deliver the notification.
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_ACTIVE_STOP:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sTitle =  "Completed " + Utilities.getPartOfDayString(workout.start) + Constants.ATRACKIT_SPACE + workout.activityName;
                    sContent =  mReferenceTools.workoutShortText(workout);
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sChannelID = ACTIVE_CHANNEL_ID;
                    viewIntent.setAction(INTENT_WORKOUT_REPORT);
                    PendingIntent pendingViewIntent = PendingIntent.getActivity
                            (getApplicationContext(), notificationID, viewIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action actionView2 =
                            new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                    getString(R.string.action_review), pendingViewIntent)
                                    .build();

                    // NotificationCompat.WearableExtender extender2 = new NotificationCompat.WearableExtender().addAction(actionView2);
                    //     extender2.setContentIcon(Math.toIntExact(iconID));
                    NotificationCompat.Builder notifyBuilder2 = getNotificationBuilder(intentString, dataBundle);
                    notifyBuilder2.addAction(actionView2);
                    notifyBuilder2.setLargeIcon(bitmap);
                    notifyBuilder2.setSmallIcon(activityIcon);
                    notifyBuilder2.setContentTitle(sTitle);
                    notifyBuilder2.setContentText(sContent);
                    notifyBuilder2.setContentIntent(pendingViewIntent);
                    if (bIsGym || bIsShooting){
                        notifyBuilder2.setProgress(0,0,false);
                    }
                    try {//
                        if (notificationManager != null)
                            notificationManager.notify(notificationID, notifyBuilder2.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_ACTIVESET_START:
                case INTENT_ACTIVESET_STOP:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    break;
/**
 *                                         resultData.putLong(KEY_FIT_WORKOUTID,mWorkout._id);
 *                                         resultData.putString(KEY_FIT_TYPE, GOAL_TYPE_BPM);
 *                                         resultData.putLong(KEY_FIT_ACTION, bpmCounter.GoalCount);
 *                                         resultData.putLong(Constants.KEY_FIT_VALUE, bpmCounter.LastCount);
 *                                         sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
 *                                         bpmCounter.GoalActive = 0;
 */
                case INTENT_GOAL_TRIGGER:
                    notificationID = NOTIFICATION_GOAL_ID;
                    PendingIntent pendingViewItent3 = PendingIntent.getActivity
                            (getApplicationContext(), notificationID, viewIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    activityIcon = mReferenceTools.getFitnessActivityIconResById(workout.activityID);


                    //  extender3.setContentIcon(Math.toIntExact(activityIcon));
                    int goal_type = dataBundle.getInt(KEY_FIT_TYPE);
                    long goal_goal_count = dataBundle.getLong(KEY_FIT_ACTION);
                    long goal_last_count = dataBundle.getLong(KEY_FIT_VALUE);
                    NotificationCompat.Builder notifyBuilder3 = getNotificationBuilder(intentString, dataBundle);
                    int goal_icon; String goal_action_msg;
                    if (goal_type == GOAL_TYPE_BPM){
                        sTitle = "BPM Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " bpm";
                    }
                    if (goal_type == GOAL_TYPE_STEPS){
                        sTitle = "Steps Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " steps";
                    }
                    if (goal_type == GOAL_TYPE_DURATION){
                        sTitle = "Duration Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " mins";
                    }
                    notifyBuilder3.setContentText(sContent);
                    notifyBuilder3.setContentTitle(sTitle);
                    NotificationCompat.Action actionView3 =
                            new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                    getString(R.string.action_open), pendingViewItent3)
                                    .build();
                    NotificationCompat.WearableExtender extender3 = new NotificationCompat.WearableExtender().addAction(actionView3);
                    notifyBuilder3.extend(extender3);
                    try{
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder3.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_SETUP:
                    notificationID = NOTIFICATION_SCHEDULE_ID;
                    sTitle =  getString(R.string.action_setup_running);
                    sContent = getString(R.string.app_name);
                    sChannelID = MAINTAIN_CHANNEL_ID;
                    PendingIntent pendingViewIntent5 = PendingIntent.getActivity
                            (getApplicationContext(), notificationID, viewIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Action actionView5 =
                            new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                    getString(R.string.action_open), pendingViewIntent5)
                                    .build();
                    //    NotificationCompat.WearableExtender extender5 = new NotificationCompat.WearableExtender().addAction(actionView5);
                    //  extender3.setContentIcon(Math.toIntExact(activityIcon));
                    NotificationCompat.Builder notifyBuilder5= new NotificationCompat
                            .Builder(context, sChannelID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon(Math.toIntExact(iconID))
                            .setUsesChronometer(false)
                            .setOngoing(true)
                            .setContentInfo(getString(R.string.action_setup_running))
                            .setAutoCancel(true)
                            //  .extend(extender5)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                    try{
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder5.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_SCHEDULE_TRIGGER:
                    notificationID = NOTIFICATION_SCHEDULE_ID;
                    break;
                case INTENT_SUMMARY_DAILY:
                    notificationID = NOTIFICATION_SUMMARY_ID;
                    NotificationCompat.Builder notifyBuilder4 = getNotificationBuilder(intentString, dataBundle);
                    if (notifyBuilder4 == null) return;
                    try{
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder4.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_WORKOUT_REPORT:
                    notificationID = NOTIFICATION_SUMMARY_ID;
/*                Workout workout2 = dataBundle.getParcelable(Workout.class.getSimpleName());
                if (workout2 == null) return;
                Intent reportingIntent = new Intent(INTENT_SUMMARY_WORKOUT);
                reportingIntent.setAction(INTENT_SUMMARY_WORKOUT);
                reportingIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout2._id);
                reportingIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                reportingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reportingIntent.putExtra(KEY_FIT_TYPE, 1); // starting from notification flag
                PendingIntent reportPendingIntent = PendingIntent.getActivity(context,
                        notificationID, reportingIntent, PendingIntent.FLAG_ONE_SHOT);*/
//                notifyBuilder.addAction(R.drawable.ic_action_attarget_vector_dark,
//                        getString(R.string.note_report_review), reportPendingIntent);
                    break;
                default:
            }
        }
        else{
            switch (intentString) {
                case INTENT_EXERCISE_NEW:
                    Exercise exercise = (dataBundle.containsKey(Exercise.class.getSimpleName())) ? dataBundle.getParcelable(Exercise.class.getSimpleName()) : null;
                    notificationID = NOTIFICATION_MAINTAIN_ID;
                    Intent newExIntent = new Intent(context, MainActivity.class);
                    newExIntent.setAction(intentString);
                    newExIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                    newExIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newExIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    newExIntent.putExtra(KEY_FIT_TYPE, 1);
                    newExIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                    sTitle = getString(R.string.action_new_exercise);
                    if (exercise != null) {
                        sContent = exercise.name + " needs setting up";
                        newExIntent.putExtra(Exercise.class.getSimpleName(), exercise);
                    }else{
                        String sName = dataBundle.getString(KEY_FIT_NAME);
                        sContent = sName + " needs setting up";
                        newExIntent.putExtra(KEY_FIT_NAME, sName);
                    }
                    PendingIntent exercisePendingIntent = PendingIntent.getActivity(context,
                            notificationID, newExIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action actionExercise = new NotificationCompat.Action.Builder(((bDayMode) ? R.drawable.ic_bench :R.drawable.ic_bench_white),
                            getString(R.string.action_new_exercise), exercisePendingIntent).build();
                    // Bitmap bitmapEx = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    Drawable icon_exer = AppCompatResources.getDrawable(context, R.drawable.ic_bench_white);
                    Utilities.setColorFilter(icon_exer,iconColor);
                    NotificationCompat.Builder exerciseBuilder = new NotificationCompat
                            .Builder(getApplicationContext(), MAINTAIN_CHANNEL_ID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon((int) iconID)
                            .setLargeIcon(Utilities.vectorToBitmap(icon_exer))
                            .setAutoCancel(true)
                            .setContentIntent(exercisePendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                    exerciseBuilder.addAction(actionExercise);
                    try{
                        // Deliver the notification.
                        notificationManager.notify(notificationID, exerciseBuilder.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_SCHEDULE_TRIGGER:
                    notificationID = NOTIFICATION_SCHEDULE_ID;
                    break;
                case INTENT_SUMMARY_DAILY:
                    notificationID = NOTIFICATION_SUMMARY_ID;
                    NotificationCompat.Builder notifyBuilder4 = getNotificationBuilder(intentString, dataBundle);
                    if (notifyBuilder4 == null) return;
                    try{
                        if (notificationManager != null) notificationManager.notify(notificationID, notifyBuilder4.build());
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
            }
        }
    }

    private void startNetworkCheckAlarm(Context context){
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        long syncInterval = appPrefs.getNetworkCheckInterval();
        long lastSync = appPrefs.getLastNetworkCheck();
        long timeMs = System.currentTimeMillis();
        long triggerTimeMs = timeMs + syncInterval;
        if (mReferenceTools != null) mReferenceTools.init(context);

        if (!mReferenceTools.isNetworkConnected() && ((timeMs - lastSync) > syncInterval)) {
            Log.w(LOG_TAG,"setting pending network check");
           // broadcastToast(getString(R.string.action_offline));
            Intent networkCheckIntent = new Intent(INTENT_NETWORK_CHECK);
            networkCheckIntent.putExtra(KEY_FIT_VALUE, appPrefs.getLastUserID());
            PendingIntent mNetworkCheckPendingIntent = PendingIntent.getBroadcast(context, ALARM_NETWORK_CHECK_CODE, networkCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNetworkAlarmManager.cancel(mNetworkCheckPendingIntent);
            mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mNetworkCheckPendingIntent);
        }
    }

    /*
     * Helper method that builds the notification.
     *
     * @return NotificationCompat.Builder: notification build with all the
     * parameters.
     */
    private NotificationCompat.Builder getNotificationBuilder(String intentString, Bundle dataBundle) {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        final String GOAL_PREFIX = "goal.";
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notificationIntent.putExtra(KEY_FIT_TYPE, 1);

        PendingIntent notificationPendingIntent;
        NotificationCompat.Builder notifyBuilder;
        int notificationID = 0;
        long iconID = R.drawable.noti_white_logo;
        int activityIcon = R.drawable.ic_running_white;
        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_EMPTY;
        long timeMs = System.currentTimeMillis();
        String sTime = Utilities.getTimeString(timeMs);
        String sChannelID = Constants.ATRACKIT_EMPTY;
        long startElapsed = System.currentTimeMillis();
        Workout workout = null;
        WorkoutSet workoutSet = null;
        if (dataBundle.containsKey(Workout.class.getSimpleName())) {
            workout = dataBundle.getParcelable(Workout.class.getSimpleName());
            if (workout == null) return null;
            activityIcon = mReferenceTools.getFitnessActivityIconResById(workout.activityID);
            startElapsed = workout.start;
        }else
            if (!intentString.equals(INTENT_SUMMARY_DAILY)) return null;


        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);

        if (dataBundle.containsKey(WorkoutSet.class.getSimpleName())){
            workoutSet = dataBundle.getParcelable(WorkoutSet.class.getSimpleName());
            if (workoutSet != null){
                if ((workoutSet.exerciseName != null) && (workoutSet.exerciseName.length() > 0)) sContent += workoutSet.exerciseName;
                if (workoutSet.start > 0) startElapsed = workoutSet.start;
            }
        }        
        switch (intentString) {
            case INTENT_ACTIVE_START:
            case INTENT_ACTIVE_RESUMED:
            case INTENT_ACTIVE_PAUSE:
                sTitle =  Utilities.getPartOfDayString(workout.start) + Constants.ATRACKIT_SPACE + workout.activityName;
                if (intentString.equals(INTENT_ACTIVE_PAUSE)) {
                    sContent += "Paused ";
                    startElapsed = mSavedStateViewModel.getPauseStart();
                }else {
                    if (workout.offline_recording == 1)
                        sContent += "Offline recording ";
                    else
                        sContent += "Live session recording ";
                    startElapsed = workout.start;
                }
                if (Utilities.isGymWorkout(workout.activityID) && (dataBundle.containsKey(WorkoutSet.class.getSimpleName())) ){
                    if (workoutSet != null) {
                        if ((workoutSet.exerciseName != null) && (workoutSet.exerciseName.length() > 0)) sContent += workoutSet.exerciseName;
                        if (workoutSet.start > 0) startElapsed = workoutSet.start;
                    }
                }

                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationIntent.putExtra(KEY_FIT_WORKOUTID, workout._id);
                notificationIntent.setAction(INTENT_ACTIVE_RESUMED);
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                if (intentString.equals(INTENT_ACTIVE_PAUSE))
                    startElapsed = mSavedStateViewModel.getPauseStart();
                String sState = mReferenceTools.currentWorkoutStateString(workout);
                if (intentString.equals(INTENT_ACTIVE_PAUSE)){
                    notifyBuilder = new NotificationCompat
                            .Builder(getApplicationContext(), sChannelID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon((int) iconID)
                            .setLargeIcon(bitmap)
                            .setWhen(startElapsed)
                            .setShowWhen(true)
                            .setUsesChronometer(false)
                            .setOngoing(true)
                            .setContentInfo(sState)
                            .setAutoCancel(true)
                            .setContentIntent(notificationPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                }else
                    notifyBuilder = new NotificationCompat
                            .Builder(getApplicationContext(), sChannelID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon((int) iconID)
                            .setWhen(startElapsed)
                            .setLargeIcon(bitmap)
                            .setShowWhen(true)
                            .setUsesChronometer(true)
                            .setOngoing(true)
                            .setContentInfo(sState)
                            .setAutoCancel(true)
                            .setContentIntent(notificationPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_ACTIVE_STOP:
                Workout currentWorkout = dataBundle.getParcelable(Workout.class.getSimpleName());
                sTitle =  "Completed " + Utilities.getPartOfDayString(currentWorkout.start) + Constants.ATRACKIT_SPACE + currentWorkout.activityName;
                sContent =  mReferenceTools.workoutShortText(currentWorkout);
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationIntent.putExtra(KEY_FIT_WORKOUTID, currentWorkout._id);
                notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                notificationIntent.putExtra(KEY_FIT_TYPE, 1);
                notificationPendingIntent = PendingIntent.getActivity(context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // 2. Build the BIG_TEXT_STYLE.
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                        // Overrides ContentTitle in the big form of the template.
                        .setBigContentTitle(sTitle)
                        // Summary line after the detail section in the big form of the template.
                        .setSummaryText(sContent);

                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setStyle(bigTextStyle)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon((int)iconID)
                        .setOngoing(false)
                        .setUsesChronometer(false)
                        //  .setContentInfo(workout2.shortText())
                        .setAutoCancel(false).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

                Drawable drawable =  AppCompatResources.getDrawable(context, activityIcon);
                if (drawable != null) {
                    Bitmap bitmapAct = Utilities.vectorToBitmap(drawable);
                    if (bitmapAct != null) notifyBuilder.setLargeIcon(bitmapAct);
                }
                break;

            case INTENT_GOAL_TRIGGER:
                notificationID = NOTIFICATION_GOAL_ID;
                sChannelID = GOALS_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setContentTitle(getString(R.string.notify_channel_goals_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(com.a_track_it.workout.common.R.drawable.ic_a_outlined)
                        .setLargeIcon(bitmap)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_SCHEDULE_TRIGGER:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                sChannelID = MAINTAIN_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                         .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_WORKOUT_REPORT:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                Workout workout1 = (Workout)dataBundle.getParcelable(Workout.class.getSimpleName());
                if (workout1 == null) return null;
                sTitle = (workout1.start > 0) ? Utilities.getPartOfDayString(workout1.start) : Constants.ATRACKIT_EMPTY;
                sTitle += Constants.ATRACKIT_SPACE + workout1.activityName;
                sContent =  mReferenceTools.workoutShortText(workout1);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(sContent).setBigContentTitle(sTitle))
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setLargeIcon(bitmap)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                if (activityIcon > 0) {
                    Drawable drawable2 = AppCompatResources.getDrawable(context, activityIcon);
                    if (drawable2 != null) {
                        Bitmap bitmapAct2 = Utilities.vectorToBitmap(drawable2);
                        if (bitmapAct2 != null) notifyBuilder.setLargeIcon(bitmapAct2);
                    }
                }
                break;
            case INTENT_SUMMARY_DAILY:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationIntent.setAction(INTENT_SUMMARY_DAILY);
                notificationPendingIntent = PendingIntent.getActivity
                        (context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                sTitle =  String.format(Locale.getDefault(),getString(R.string.note_summary_title), sTime);
                sContent = ATRACKIT_EMPTY;
                String sKey = GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName();
                String sTemp;
                try {
                    int steps = dataBundle.getInt(MAP_STEPS);
                    if (dataBundle.containsKey(sKey)) {
                        sTemp = dataBundle.getString(sKey, "0.0");
                        int goalSteps = Math.round(Float.parseFloat(sTemp));
                        if (goalSteps > 0){
                            float percentGoal = (steps > 0) ? ((float)steps/goalSteps)*100 : 0F;
                            sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_steps_goals), steps, goalSteps, percentGoal);
                        }
                    }else
                        sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_steps), steps);

                    sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_dist),dataBundle.getFloat(Constants.MAP_DISTANCE));

                    sKey = GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName();
                    int moveMins = dataBundle.getInt(MAP_MOVE_MINS);
                    if (dataBundle.containsKey(sKey)){
                        sTemp = dataBundle.getString(sKey, "0.0");
                        int goalMove = Math.round(Float.parseFloat(sTemp));
                        if (goalMove > 0){
                            float percentGoal = (moveMins > 0) ? ((float)moveMins/goalMove)*100 : 0F;
                            sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_move_goals), moveMins, goalMove, percentGoal);
                        }
                    }else
                        sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_move), moveMins);

                    sKey = GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName();
                    int heartPts = Math.round(dataBundle.getFloat(MAP_HEART_POINTS));
                    if (dataBundle.containsKey(sKey)){
                        sTemp = dataBundle.getString(sKey, "0");
                        int goalHeart = Math.round(Float.parseFloat(sTemp));
                        if (goalHeart> 0){
                            float percentGoal = (heartPts > 0) ? ((float)heartPts/goalHeart)*100 : 0F;
                            sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_heart_pts_goals), heartPts, goalHeart, percentGoal);
                        }
                    }else
                        sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_heart_pts), heartPts);

                    sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(), getString(R.string.note_summary_heart),dataBundle.getFloat(Constants.MAP_BPM_MIN),
                            dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG));
                    sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_calories),dataBundle.getFloat(Constants.MAP_CALORIES));
                }catch(Exception e){
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Log.e(LOG_TAG,e.getMessage());
                }
                // No goals option!
                if (!dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName()))
                    sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_desc), dataBundle.getInt(Constants.MAP_MOVE_MINS), dataBundle.getFloat(Constants.MAP_DISTANCE),
                            dataBundle.getFloat(Constants.MAP_BPM_MIN),  dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(Constants.MAP_STEPS)); {
                                // Build the notification with all of the parameters.
            }
            IconCompat bubbleIcon = IconCompat.createWithResource(context, R.drawable.ic_a_outlined);
            NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder(notificationPendingIntent, bubbleIcon).setAutoExpandBubble(true).build();
                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(sContent).setBigContentTitle(sTitle))
                        .setBubbleMetadata(metaBubble)
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setLargeIcon(bitmap)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            default:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (context, notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(context, sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                         .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
        }
        return notifyBuilder;
    }

    public void cancelNotification(String intentString){
        int notificationID = 0;
        switch (intentString) {
            case INTENT_ACTIVE_START:
            case INTENT_ACTIVE_STOP:
            case INTENT_ACTIVE_PAUSE:
            case INTENT_ACTIVE_RESUMED:
            case INTENT_ACTIVESET_START:
            case INTENT_ACTIVESET_STOP:
                notificationID = NOTIFICATION_ACTIVE_ID;
                break;
            case INTENT_GOAL_TRIGGER:
                notificationID = NOTIFICATION_GOAL_ID;
                break;
            case INTENT_SETUP:
            case INTENT_SCHEDULE_TRIGGER:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                break;
            case INTENT_SUMMARY_DAILY:
            case INTENT_WORKOUT_REPORT:
                notificationID = NOTIFICATION_SUMMARY_ID;
                break;
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (!intentString.equals(INTENT_SETUP) && (notificationID > 0))
                notificationManager.cancel(notificationID);
            else
                notificationManager.cancelAll();
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }


    private void broadcastToast(String msg){
        if (mMessagesViewModel == null) return;
        mHandler.post(() -> {
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
                int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
                if (bDayMode)
                    msgIntent.putExtra(KEY_FIT_TYPE, 2);
                else
                    msgIntent.putExtra(KEY_FIT_TYPE, 1);
                mMessagesViewModel.addLiveIntent(msgIntent);
        });
    }
    /**
     * Dispatches an {@link android.content.Intent} to take a photo. Result will be returned back in
     * onActivityResult().
     */
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            if (cameraPermissionResultLauncher != null)
                cameraPermissionResultLauncher.launch(Manifest.permission.CAMERA);
            else
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_BODY_CAMERA);
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            // File photoFile = null;
            try {
                imageCaptureActivityResultLauncher.launch(takePictureIntent);
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.e(LOG_TAG,"dispatchTakePictureIntent error " + ex.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().recordException(ex);
            }

        }
    }
    private void dispatchPickImageIntent() throws IOException {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            if (storagePermissionResultLauncher != null)
                storagePermissionResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            else
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            return;
        }
/*
        // Create a random image and save it in private app folder
        final File sharedFile = createFile();

        // Get the shared file's Uri
        final Uri uri = FileProvider.getUriForFile(this, SHARED_PROVIDER_AUTHORITY, sharedFile);
        // Create a intent
        final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                .setType("image/*")
                .addStream(uri);

        // Start the intent
        final Intent chooserIntent = intentBuilder.createChooserIntent();
        startActivity(chooserIntent);*/
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        imagePickerActivityResultLauncher.launch(photoPickerIntent);
        //startActivityForResult(photoPickerIntent, REQUEST_IMAGE_PICKER);
    }


    /**
     * Builds an {@link com.google.android.gms.wearable.Asset} from a bitmap. The image that we get
     * back from the camera in "data" is a thumbnail size. Typically, your image should not exceed
     * 320x320 and if you want to have zoom and parallax effect in your app, limit the size of your
     * image to 640x400. Resize your image before transferring to your wearable device.
     */
    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Log.e(LOG_TAG, "toAsset error " + e.getMessage());
                }
            }
        }
    }

    /**
     * Sends the asset that was created from the photo we took by adding it to the Data Item store.
     */
    private void sendPhoto(Asset asset, String sType) {
            PutDataMapRequest dataMap = PutDataMapRequest.create(sType.equals(Constants.LABEL_EXT_FILE) ? IMAGE_PATH : CAMERA_PATH);
            dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
            dataMap.getDataMap().putLong("time", new Date().getTime());
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();
            Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
            dataItemTask.addOnSuccessListener(
                    dataItem -> Log.d(LOG_TAG, "Sending " + sType + " was successful: " + dataItem))
                    .addOnFailureListener(
                            e -> Log.e(LOG_TAG, "Sending " + sType + " was FAILED: " + e.getMessage()));

    }


    ///////////////////////////////////////
    // FLOATING MENU
    ///////////////////////////////////////

    @Override
    public void onMenuExpanded() {
        overlay.clearAnimation();
        float viewAlpha = overlay.getAlpha();
        overlay.setVisibility(View.VISIBLE);

        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(overlay, "alpha", viewAlpha, 1f);
        fadeAnim.setDuration(1000L);
        fadeAnim.start();
    }

    @Override
    public void onMenuCollapsed() {
        overlay.clearAnimation();
        float viewAlpha = overlay.getAlpha();
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(overlay, "alpha", viewAlpha, 0f);
        fadeAnim.setDuration(300L);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });

        fadeAnim.start();
    }

    private void onNewLocation(Location location) {
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
        if ((appPrefs != null) && (!appPrefs.getUseLocation())) return;
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() == 0) return;
        double last_lat = mSavedStateViewModel.getLatitude();
        double last_long = mSavedStateViewModel.getLongitude();
        String sLocationMsg = mSavedStateViewModel.getLocationAddress();
        SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
        if (sLocationMsg.length() == 0 && sdt.fitLocation.length() > 0 ) sLocationMsg = sdt.fitLocation;
        boolean newLocation = (sLocationMsg.length() == 0);
        long timeMs = System.currentTimeMillis();
        mCalendar.setTimeInMillis(timeMs);
        int DOY =  mCalendar.get(Calendar.DAY_OF_YEAR);
        int HOD = mCalendar.get(Calendar.HOUR_OF_DAY);
        long lastUpdate = appPrefs.getLastLocationUpdate();
        if (!newLocation && lastUpdate > 0) {
            mCalendar.setTimeInMillis(lastUpdate);
            newLocation = (DOY < mCalendar.get(Calendar.DAY_OF_YEAR) || ((HOD - 3) > mCalendar.get(Calendar.HOUR_OF_DAY)));
            if (location != null) {
                float[] distance = new float[1];
                Location.distanceBetween(last_lat, last_lat, location.getLatitude(), location.getLongitude(), distance);
                if (!newLocation)
                    newLocation = (timeMs - lastUpdate) > TimeUnit.MINUTES.toMillis(15);
                if (!newLocation)
                    newLocation = (distance[0] > 5.0);
            }
        }else
            newLocation = true;
        try {
            if (!newLocation)
                newLocation = ((Math.abs(last_lat -location.getLatitude()) > 0.001)
                        || (Math.abs(last_long - location.getLongitude()) > 0.001)
                        || (sLocationMsg.length() == 0));
            if (newLocation) {
                new TaskLocation(location).run();
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG,"onNewLocation " + e.getMessage());
        }

    }
    private class UpdateCurrentLatLng implements Runnable{
        private float BPM;
        private long Steps;

        UpdateCurrentLatLng(float fBPM, long lSteps){ BPM = fBPM; Steps = lSteps;}

        @Override
        public void run() {
            long timeMidnight = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            List<TwoIDsTuple> tuples = mSessionViewModel.getLatLngCounts(timeMidnight,0);
            if ((tuples.size() > 0) && (tuples.get(0).rowID > 0)){
                List<ATrackItLatLng> listLatLng = mSessionViewModel.getATrackItLatLngsById(tuples.get(0).itemCount);
                if ((listLatLng != null) && (listLatLng.size() > 0)){
                    ATrackItLatLng atlatlng = listLatLng.get(0);
                    boolean bUpdated = false;
                    if (BPM > 0) {
                        atlatlng.BPM = BPM;
                        bUpdated = true;
                    }
                    if (Steps > 0) {
                        atlatlng.Steps = Steps;
                        bUpdated = true;
                    }
                    if (bUpdated) mSessionViewModel.updateLatLng(atlatlng);
                }
            }
        }
    }
 /*   private class TaskSyncWork implements Runnable {
        private String sUserID;
        private String sDeviceID;

        TaskSyncWork(String u, String d) {
            sUserID = u;
            sDeviceID = d;
        }

        @Override
        public void run() {
            long timeMs = System.currentTimeMillis();
            long lastUpdate = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            List<Workout> workoutList = mSessionViewModel.getWorkoutsByTimesNow(sUserID,sDeviceID,lastUpdate,timeMs);
            if (workoutList.size() > 0){
                for (Workout w : workoutList) {
                    if (!Utilities.isDetectedActivity(w.activityID) && (w.device_sync == 0 || (w.lastUpdated > w.device_sync))) {
                        Bundle resultData = new Bundle();
                        resultData.putString(KEY_FIT_USER, sUserID);
                        resultData.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                        resultData.putParcelable(Workout.class.getSimpleName(), w);
                        sendDataClientMessage(INTENT_EXTRA_RESULT, resultData);
                    }
                }
            }
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_STARTUP_INFO);
            dataMap.putString(Constants.KEY_FIT_USER, sUserID);
            dataMap.putInt(KEY_FIT_ACTION, mSavedStateViewModel.getState());  // current state
            if ((appPrefs.getStepsSensorCount() > 0) && (mMessagesViewModel.getDeviceStepsMsg().getValue() != null))
                dataMap.putString(DataType.TYPE_STEP_COUNT_DELTA.getName(), mMessagesViewModel.getDeviceStepsMsg().getValue());
            if ((appPrefs.getBPMSensorCount() > 0) && (mMessagesViewModel.getDeviceBpmMsg().getValue() != null))
                dataMap.putString(DataType.TYPE_HEART_RATE_BPM.getName(), mMessagesViewModel.getDeviceBpmMsg().getValue());
            if ((appPrefs.getPressureSensorCount() > 0) && (mMessagesViewModel.getPressureMsg().getValue() != null))
                dataMap.putString(Sensor.STRING_TYPE_PRESSURE, mMessagesViewModel.getPressureMsg().getValue());
            if ((appPrefs.getTempSensorCount() > 0) && (mMessagesViewModel.getTemperatureMsg().getValue() != null))
                dataMap.putString(Sensor.STRING_TYPE_AMBIENT_TEMPERATURE, mMessagesViewModel.getTemperatureMsg().getValue());
            if (mMessagesViewModel.getActivityMsg().getValue() != null)
                dataMap.putString(INTENT_RECOG, mMessagesViewModel.getActivityMsg().getValue());
            if (mMessagesViewModel.getHeartPtsMsg().getValue() != null)
                dataMap.putString(DataType.TYPE_HEART_POINTS.getName(), mMessagesViewModel.getHeartPtsMsg().getValue());
            if (mMessagesViewModel.getDistanceMsg().getValue() != null)
                dataMap.putString(DataType.TYPE_DISTANCE_DELTA.getName(), mMessagesViewModel.getDistanceMsg().getValue());
            if (mMessagesViewModel.getMoveMinsMsg().getValue() != null)
                dataMap.putString(DataType.TYPE_MOVE_MINUTES.getName(), mMessagesViewModel.getMoveMinsMsg().getValue());

            sendMessage(Constants.MESSAGE_PATH_WEAR, dataMap);
        }
    }*/
 private class TaskSensorDailyTotals implements Runnable {
     private Calendar calendar;
     private String sUserID;
     private SensorDailyTotals SDT;
     private long sdtRefreshPeriod; 

     TaskSensorDailyTotals(String sUserID, Calendar cal) {
         this.sUserID = sUserID;
         if (cal != null) this.calendar = cal;
         else this.calendar = Calendar.getInstance();
     }

     @Override
     public void run() {
         try {
             int currentState = mSavedStateViewModel.getState();
             Workout workout = mSavedStateViewModel.getActiveWorkout().getValue();
             if (workout == null && (mWorkout != null)) workout = mWorkout;
             SensorDailyTotals currentSDT = mSavedStateViewModel.getSDT();
             final long timeMs = System.currentTimeMillis();
             boolean bOffline = (workout != null) ? (workout.offline_recording == 1) : !mReferenceTools.isNetworkConnected();
             if ((currentSDT != null && (currentSDT.activityType != DetectedActivity.STILL && currentSDT.activityType != DetectedActivity.UNKNOWN
                     && currentSDT.activityType != DetectedActivity.IN_VEHICLE && currentSDT.activityType != DetectedActivity.TILTING))
                     || (currentState == WORKOUT_LIVE || currentState == WORKOUT_CALL_TO_LINE || currentState == WORKOUT_PAUSED)) {
                 if ((workout != null) && (workout.activityID != null)) {
                     if (Utilities.isGymWorkout(workout.activityID) || Utilities.isShooting(workout.activityID))
                         sdtRefreshPeriod = (bOffline ? TimeUnit.SECONDS.toMillis(60) : TimeUnit.MINUTES.toMillis(2));
                     else if (Utilities.isInActiveWorkout(workout.activityID))
                         sdtRefreshPeriod = TimeUnit.MINUTES.toMillis(3);
                     else
                         sdtRefreshPeriod = (bOffline ? TimeUnit.SECONDS.toMillis(30) : TimeUnit.MINUTES.toMillis(1));
                 } else
                     sdtRefreshPeriod = TimeUnit.MINUTES.toMillis(5);
             }
             else
                 if (!bOffline)
                    sdtRefreshPeriod = TimeUnit.MINUTES.toMillis(10);
                else
                    sdtRefreshPeriod = TimeUnit.MINUTES.toMillis(5);
             String sMsg = "SDT RefreshPeriod " + Utilities.getDurationBreakdown(sdtRefreshPeriod) + " last diff " + (timeMs- ((currentSDT != null) ? currentSDT._id : 0))
                     + " " + Utilities.getDurationBreakdown(timeMs- ((currentSDT != null) ? currentSDT._id : 0));
             Log.e(LOG_TAG, sMsg);
             calendar.setTimeInMillis(timeMs);
             boolean bUpdated = false;
             long startHour = Utilities.getDayStart(calendar, timeMs);
             int DOYNow = calendar.get(Calendar.DAY_OF_YEAR);
             DateTuple dt = mSessionViewModel.getSensorDailyDateTuple(sUserID, startHour, timeMs); // how many today
             boolean bExisting = ((dt != null) && (dt.sync_count > 0));
             SensorDailyTotals topSDT = null;
             if (bExisting) {
                 topSDT = mSessionViewModel.getTopSensorDailyTotal(sUserID);
                 if (topSDT != null) {
                     if ((timeMs - topSDT._id) > sdtRefreshPeriod)   // if time since last > the new period = start a new one!
                         bExisting = false;
                     else {
                         if ((currentSDT != null) && (currentSDT.lastUpdated > topSDT.lastUpdated)) {
                             SDT = currentSDT;
                             bUpdated = true;
                         }else {
                             SDT = topSDT;
                         }
                     }
                 } else bExisting = false;
             }else{
                 if (currentSDT != null){
                     if ((timeMs - currentSDT._id) < sdtRefreshPeriod){
                         bExisting = true;
                         SDT = currentSDT;
                     }
                 }
             }

             // new required!
             if (!bExisting) {
                 SDT = new SensorDailyTotals(timeMs, this.sUserID);
                 SDT.userID = this.sUserID;
                 SDT.lastUpdated = timeMs;
                 int doySDT = 0;
                 if (topSDT != null) {
                     calendar.setTimeInMillis(topSDT._id);
                     doySDT = calendar.get(Calendar.DAY_OF_YEAR);
                 }
                 Log.w(LOG_TAG, Utilities.getDurationBreakdown(timeMs - sdtRefreshPeriod) + " remaining NEW SDT - adding new last: period is " + Utilities.getDurationBreakdown(sdtRefreshPeriod) + " " + Utilities.getTimeDateString(timeMs));
                 // carry forward!
                 if (topSDT != null && (doySDT == DOYNow)) {
                     if (topSDT.activityType != -1) SDT.activityType = topSDT.activityType;
                     if (topSDT.lastActivityType > 0) SDT.lastActivityType = topSDT.lastActivityType;
                     if (topSDT.deviceStep != 0) SDT.deviceStep = topSDT.deviceStep;
                     if (topSDT.lastDeviceStep != 0) SDT.lastDeviceStep = topSDT.lastDeviceStep;
                     if (topSDT.deviceBPM != 0) SDT.deviceBPM = topSDT.deviceBPM;
                     if (topSDT.lastDeviceBPM != 0) SDT.lastDeviceBPM = topSDT.lastDeviceBPM;
                     if (topSDT.fitBPM != 0) SDT.fitBPM = topSDT.fitBPM;
                     if (topSDT.lastFitBPM != 0) SDT.lastFitBPM = topSDT.lastFitBPM;
                     if (topSDT.fitStep != 0) SDT.fitStep = topSDT.fitStep;
                     if (topSDT.lastFitStep != 0) SDT.lastFitStep = topSDT.lastFitStep;
                     if (topSDT.pressure != 0) SDT.pressure = topSDT.pressure;
                     if (topSDT.temperature != 0) SDT.temperature = topSDT.temperature;
                     if (topSDT.humidity != 0) SDT.humidity = topSDT.humidity;
                     if (topSDT.lastDeviceOther != 0) SDT.lastDeviceOther = topSDT.lastDeviceOther;
                     if (topSDT.fitLat != 0) SDT.fitLat = topSDT.fitLat;
                     if (topSDT.fitLng != 0) SDT.fitLng = topSDT.fitLng;
                     if (topSDT.fitLocation.length() != 0) SDT.fitLocation = topSDT.fitLocation;
                     if (topSDT.device2Step != 0) SDT.device2Step = topSDT.device2Step;
                     if (topSDT.lastDevice2Step != 0) SDT.lastDevice2Step = topSDT.lastDevice2Step;
                     if (topSDT.device2BPM != 0) SDT.device2BPM = topSDT.device2BPM;
                     if (topSDT.lastDevice2BPM != 0) SDT.lastDevice2BPM = topSDT.lastDevice2BPM;
                     if (topSDT.pressure2 != 0) SDT.pressure2 = topSDT.pressure2;
                     if (topSDT.temperature2 != 0) SDT.temperature2 = topSDT.temperature2;
                     if (topSDT.humidity2 != 0) SDT.humidity2 = topSDT.humidity2;
                     if (topSDT.lastDevice2Other != 0) SDT.lastDevice2Other = topSDT.lastDevice2Other;
                     if (topSDT.lastUpdated != 0) SDT.lastUpdated = topSDT.lastUpdated;
                 }
                 else{
                     Log.w(LOG_TAG, "not same DOY for SDT - clearing values");
                     String sZero = getString(R.string.label_number_0);
                     mMessagesViewModel.addDeviceStepsMsg(sZero);
                     mMessagesViewModel.addDeviceBpmMsg(sZero);
                     mMessagesViewModel.addDistanceMsg(sZero);
                     mMessagesViewModel.addStepsMsg(sZero);
                     mMessagesViewModel.addBpmMsg(sZero);
                     mMessagesViewModel.addSpeedMsg(sZero);
                     mMessagesViewModel.addDevice2StepsMsg(sZero);
                     mMessagesViewModel.addDevice2BpmMsg(sZero);
                 }
                 mSessionViewModel.insertSensorDailyTotal(SDT);
             }
             else {
                 if (bUpdated) {
                     mSessionViewModel.updateSensorDailyTotal(SDT);
                 }
             }
             // online - invalid and still etc for more than 2 minutes
             boolean bRelease = ((!bOffline && (currentState == WORKOUT_INVALID || currentState == WORKOUT_PENDING)) && ((timeMs-SDT.lastActivityType) > TimeUnit.MINUTES.toMillis(2)) &&
                     ((SDT.activityType == DetectedActivity.STILL)||(SDT.activityType == DetectedActivity.UNKNOWN)||(SDT.activityType == DetectedActivity.TILTING)));
             if (!bRelease) bRelease = (bOffline && (currentState == WORKOUT_INVALID || currentState == WORKOUT_PENDING) && ((timeMs-lastInteraction) > TimeUnit.MINUTES.toMillis(2)));
             if (bRelease){
                 if  (currentState == WORKOUT_PENDING){
                     Log.e(LOG_TAG, "releasing sensors pending state inactive time-out " + currentState);
                     sessionSetCurrentState(WORKOUT_INVALID);
                 }else {
                     Log.e(LOG_TAG, "releasing sensors inactive time-out " + currentState);
                     long defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getStepsSampleRate());
                     if (appPrefs.getStepsSensorCount() > 0) {
                         if ((BoundSensorManager.getCount(1) > 0) && ((timeMs - SDT.lastDeviceStep) > defaultPeriod)) { // only if we've been updated
                             BoundSensorManager.doReset(1);
                         } else {
                             if ((timeMs - SDT.lastDeviceStep) > defaultPeriod) {
                                 BoundSensorManager.doSetup(1, (int) defaultPeriod);
                             }
                         }
                     }
                     defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getBPMSampleRate());
                     if (appPrefs.getBPMSensorCount() > 0) {
                         if ((BoundSensorManager.getCount(2) > 0) && ((timeMs - SDT.lastDeviceBPM) > defaultPeriod))
                             BoundSensorManager.doReset(2);
                         else {
                             if (((timeMs - SDT.lastDeviceBPM) > defaultPeriod)) {
                                 BoundSensorManager.doSetup(2, (int) defaultPeriod);
                             }
                         }
                     }
                     defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getOthersSampleRate());
                     if ((appPrefs.getPressureSensorCount() > 0) || (appPrefs.getTempSensorCount() > 0) || (appPrefs.getHumiditySensorCount() > 0)) {
                         if (BoundSensorManager.getCount(0) > 0 && ((timeMs - SDT.lastDeviceOther) > defaultPeriod))
                             BoundSensorManager.doReset(0);
                         else {
                             if ((timeMs - SDT.lastDeviceOther) > defaultPeriod)
                                 BoundSensorManager.doSetup(0, (int) defaultPeriod);
                         }
                     }
                 }
             }else{
                 long defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getStepsSampleRate());
                 if (appPrefs.getStepsSensorCount() > 0 && (BoundSensorManager.getCount(1) == 0) && ((timeMs - SDT.lastDeviceStep) > defaultPeriod)){
                     BoundSensorManager.doSetup(1,(int)defaultPeriod);
                 }
                 defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getBPMSampleRate());
                 if (appPrefs.getBPMSensorCount() > 0 && (BoundSensorManager.getCount(2) == 0) && ((timeMs - SDT.lastDeviceBPM) > defaultPeriod)){
                     BoundSensorManager.doSetup(2,(int)defaultPeriod);
                 }
                 defaultPeriod = TimeUnit.SECONDS.toMillis(userPrefs.getOthersSampleRate());
                 if (((appPrefs.getPressureSensorCount() > 0)||(appPrefs.getTempSensorCount() > 0)||(appPrefs.getHumiditySensorCount() > 0))
                         && (BoundSensorManager.getCount(0) == 0) && ((timeMs - SDT.lastDeviceOther) > defaultPeriod)){
                     BoundSensorManager.doSetup(0,(int)defaultPeriod);
                 }
                 String sLabel = getString(R.string.label_fit_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
                 if (appPrefs.getPrefByLabel(sLabel)){
                     if (BoundFitnessSensorManager.getCount(1) == 0) BoundFitnessSensorManager.doSetup();
                 }
                 sLabel = getString(R.string.label_fit_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                 if (appPrefs.getPrefByLabel(sLabel)){
                     if (BoundFitnessSensorManager.getCount(2) == 0) BoundFitnessSensorManager.doSetup();
                 }
             }

             mSavedStateViewModel.setSDT(SDT);
             Intent broadcastIntent = new Intent(Constants.INTENT_TOTALS_REFRESH);
             broadcastIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
             broadcastIntent.putExtra(Constants.KEY_COMM_TYPE, 2);
             broadcastIntent.putExtra(SensorDailyTotals.class.getSimpleName(), SDT);
             broadcastIntent.putExtra(Constants.KEY_INDEX_METRIC, bUpdated);
             broadcastIntent.putExtra(Constants.KEY_INDEX_FILTER, bExisting);
             broadcastIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
             mMessagesViewModel.addLiveIntent(broadcastIntent);

         } catch (Exception e) {
             Log.e(LOG_TAG, "TaskSensorDaily failed " + e.getMessage());
         }
     }
    }
    private class TaskDeviceConfigLoad implements Runnable{
        String sUserId;
        TaskDeviceConfigLoad(String u){
            this.sUserId = u;
        }

        @Override
        public void run() {
            String sDevices = KEY_DEVICE1.replace("1","%");
            List<Configuration> list = mSessionViewModel.getConfigurationLikeName(sDevices,this.sUserId);
            mMessagesViewModel.setLiveConfig(list);
        }
    }

    private class TaskLocation implements Runnable{
        private Location location;

        TaskLocation(Location loc){
            location = loc;
        }

        @Override
        public void run() {
            if (location == null || !appPrefs.getUseLocation()) return;
            String cityName = mSavedStateViewModel.getLocationAddress();
            long timeMs = System.currentTimeMillis();
            long lastUpdate = appPrefs.getLastLocationUpdate();
            if (lastUpdate == 0) lastUpdate = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            Double[] coords = new Double[2];
            coords[0] = location.getLatitude();
            coords[1] = location.getLongitude();
            boolean newLocation = ((coords[0] != 0) && (coords[1] != 0)) || (cityName == null || (cityName.length() == 0));
            float speedValue = (location.hasSpeed()) ? location.getSpeed() : 0F;
            double altitudeValue = (location.hasAltitude()) ? location.getAltitude() : 0D;
            ATrackItLatLng aTrackItLatLng = null;
            boolean bNew = true;
            if (newLocation) {
                if (mLocation == null)
                    mLocation = new Location(location);
                else
                    mLocation.set(location);
                mSavedStateViewModel.setLatitude(coords[0]);
                mSavedStateViewModel.setLongitude(coords[1]);
                appPrefs.setLastLocationUpdate(timeMs);
                cityName = ATRACKIT_EMPTY;
                List<ATrackItLatLng> list = mSessionViewModel.getATrackItLatLngsByLatLng(coords[0],coords[1]);
                if ((list != null) && (list.size() > 0)){
                    aTrackItLatLng = list.get(0);
                    if (aTrackItLatLng != null) {
                        bNew = false;
                        cityName = aTrackItLatLng.shortName;
                        if (cityName.length() > 0) {
                            mSavedStateViewModel.addLocationMsg(cityName);
                            altitudeValue = aTrackItLatLng.Alt;
                        }
                    }
                }
                if (cityName.length() == 0) {
                    List<Address> addresses = null;
                    if ((timeMs - lastUpdate) > TimeUnit.MINUTES.toMillis(1L) || bNew) {
                        try {
                            Log.w(LOG_TAG, "about to geoCode " + location.toString());
                            if (mGeoCoder == null) mGeoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            addresses = mGeoCoder.getFromLocation(coords[0], coords[1], 2);
                        } catch (Exception e1) {
                            // FirebaseCrashlytics.getInstance().recordException(e1);
                            Log.e(LOG_TAG, "FAILED geoCode " + e1.getMessage());
                        }
                        if (addresses != null && addresses.size() > 0) {
                            cityName = addresses.get(0).getAddressLine(0) + ATRACKIT_EMPTY;
                            mSavedStateViewModel.addLocationMsg(cityName);
                        } else
                            cityName = mSavedStateViewModel.getLocationAddress();
                    } else
                        Log.e(LOG_TAG, "too soon for GEOCODE");

                    if (aTrackItLatLng == null) aTrackItLatLng = new ATrackItLatLng(coords[0], coords[1]);
                    if ((cityName != null) && (cityName.length() > 0)) {
                        aTrackItLatLng.shortName = cityName;
                    }
                    aTrackItLatLng.Alt = altitudeValue;
                    aTrackItLatLng.Speed = speedValue;
                    String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
                    if (sBPM == null) {
                        if (mMessagesViewModel.getBpmMsg().getValue() != null)
                            sBPM = mMessagesViewModel.getBpmMsg().getValue();
                        else if (mMessagesViewModel.getDevice2BpmMsg().getValue() != null)
                            sBPM = mMessagesViewModel.getDevice2BpmMsg().getValue();
                    }
                    if ((sBPM != null) && (sBPM.length() > 0)) {
                        Float f = Float.parseFloat(sBPM);
                        aTrackItLatLng.BPM = f;
                    }
                    sBPM = mMessagesViewModel.getDeviceStepsMsg().getValue();
                    if (sBPM == null) {
                        if (mMessagesViewModel.getStepsMsg().getValue() != null)
                            sBPM = mMessagesViewModel.getStepsMsg().getValue();
                        else if (mMessagesViewModel.getDevice2StepsMsg().getValue() != null)
                            sBPM = mMessagesViewModel.getDevice2StepsMsg().getValue();
                    }
                    if ((sBPM != null) && (sBPM.length() > 0)) {
                        Long l = Long.parseLong(sBPM);
                        aTrackItLatLng.Steps = l;
                    }
                    if (bNew) mSessionViewModel.insertLatLng(aTrackItLatLng); else mSessionViewModel.updateLatLng(aTrackItLatLng);
                }
                if (speedValue > 0F){
                    String sSpeed = String.format(Locale.getDefault(), SINGLE_FLOAT, speedValue);
                    mMessagesViewModel.addSpeedMsg(sSpeed);
                }
                if (altitudeValue > 0D) mMessagesViewModel.addAltitudeMsg(altitudeValue);

                Data.Builder builder = new Data.Builder();
                builder.putString(KEY_FIT_USER, appPrefs.getLastUserID());
                builder.putInt(KEY_FIT_TYPE, 1);  // location indicator
                builder.putDouble(Constants.KEY_LOC_LAT, coords[0]);
                builder.putDouble(Constants.KEY_LOC_LNG, coords[1]);
                builder.putDouble(Constants.KEY_LOC_ALT, altitudeValue);
                builder.putFloat(Constants.KEY_LOC_SPD, speedValue);
                builder.putString(Constants.KEY_LOC_LOC, cityName);
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(UserDailyTotalsWorker.class)
                                .setInputData(builder.build())
                                .build();
                mWorkManager.enqueue(workRequest);
            }else // if new location
                Log.e(LOG_TAG, "NOT NEW location " + location.toString());
        }
    }

    private void showAlertDialogConfirm(final int action){

        String message_text = Constants.ATRACKIT_EMPTY;
        switch (action){
            case Constants.ACTION_STARTING:
                message_text = getString(R.string.action_starting);
                break;
            case Constants.ACTION_STOPPING:
                message_text = getString(R.string.action_stopping);
                break;
            case Constants.ACTION_QUICK_STOP:
                message_text = getString(R.string.action_quick_stop);
                break;
            case Constants.ACTION_RESUMING:
                message_text = getString(R.string.action_resuming);
                break;
            case ACTION_PAUSING:
                message_text = getString(R.string.action_pausing);
                break;
            case ACTION_START_SET:
                if (mSavedStateViewModel.getIsGym())
                    message_text = getString(R.string.action_start_set);
                if (mSavedStateViewModel.getIsShoot())
                    message_text = getString(R.string.action_start_end);
                break;
            case ACTION_REPEAT_SET:
                message_text = getString(R.string.action_repeat_set);
                break;
            case ACTION_END_SET:
                if (mSavedStateViewModel.getIsGym())
                    message_text = getString(R.string.action_end_set);
                if (mSavedStateViewModel.getIsShoot())
                    message_text = getString(R.string.action_end_end);
                break;
            case Constants.ACTION_CANCELLING:
                message_text = getString(R.string.action_cancel);
                break;
            case Constants.ACTION_STOP_QUIT:
            case Constants.ACTION_EXITING:
                message_text = getString(R.string.action_exiting);
                break;
            case Constants.ACTION_SIGNOUT_QUIT:
                message_text = getString(R.string.action_signout);
                break;
            case Constants.QUESTION_NETWORK:
                message_text = getString(R.string.no_network_connection);
                break;
        }
        try {
            getSupportFragmentManager().executePendingTransactions();
            CustomAlertDialog existingDialog = (CustomAlertDialog)getSupportFragmentManager().findFragmentByTag(CustomAlertDialog.class.getSimpleName());
            if (existingDialog != null && existingDialog.isAdded()) return;
            CustomAlertDialog alertDialog = CustomAlertDialog.newInstance(action, message_text, MainActivity.this);
            alertDialog.setCancelable(true);
            alertDialog.show(getSupportFragmentManager(), CustomAlertDialog.class.getSimpleName());
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "showAlertDialogConfirm " + e.getMessage());
        }
    }


    // [START signOut]
    private void signOut() {
        final Context context = getApplicationContext();
        broadcastToast(getString(R.string.nav_sign_out));
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        crossFadeIn(overlay);
        // Build a GoogleSignInClient with the options specified by gso.
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() == 0) return;
        appPrefs.setLastUserID(ATRACKIT_EMPTY);
        mSavedStateViewModel.setUserIDLive(ATRACKIT_EMPTY);
        appPrefs.setLastUserID(Constants.ATRACKIT_EMPTY);
        appPrefs.setLastUserLogIn(0L);
        appPrefs.setLastLicCheck(0L);  // check licence on new sign-in
        appPrefs.setDeviceID(Constants.ATRACKIT_EMPTY);
        mSavedStateViewModel.setDeviceID(Constants.ATRACKIT_EMPTY);
        appPrefs.setLastSync(0L);
        appPrefs.setLastDailySync(0L);
        appPrefs.setLastLocationUpdate(0L);
        if (userPrefs != null) {
            userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, Constants.ATRACKIT_EMPTY); // refresh image on next logon
            userPrefs.setLastUserName(Constants.ATRACKIT_EMPTY);
        }
        CustomIntentReceiver.cancelAlarm(context);
        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) {
            DataMap dataMap = new DataMap();
            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
            dataMap.putInt(KEY_FIT_TYPE, (int)4); // sign-out indicator
            dataMap.putString(KEY_FIT_USER, sUserId);
            if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
            if (wearNodeId.length() != 0)
                sendMessage(wearNodeId, Constants.MESSAGE_PATH_WEAR_SERVICE, dataMap);
            else
                sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME, Constants.MESSAGE_PATH_WEAR_SERVICE,dataMap);

        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mHandler.postDelayed(() -> googleSignInClient.signOut()
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        broadcastToast("Sign-out successful");
                        mSavedStateViewModel.setActiveWorkout(null);
                        Bundle params = new Bundle();
                        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_SIGNOUT);
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
                        quitApp();
                    }
                }),1000L);

    }
    // [END signOut]


    private void quitApp() {
        Wearable.getCapabilityClient(MainActivity.this).removeListener(mCapabilityListener);
        if (mFirebaseAuth != null) mFirebaseAuth.signOut();
        mGoogleAccount = null;
        finish();
        System.exit(0);
    }
    private void signInError(ApiException e){
        if ((e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS) ||
                (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED)) {
            Log.d(LOG_TAG, "signin already inprog or canncelled "  + e.getStatusCode());
            return;
        }
        FirebaseCrashlytics.getInstance().recordException(e);
        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
        int statusCode = e.getStatusCode();
        String sMsg = ATRACKIT_EMPTY;
        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
            sMsg = "Sign in is required";
            broadcastToast(sMsg);
            if (authInProgress) {
                authInProgress = false;
                mHandler.postDelayed(() -> signIn(), 2000);
            }
            return;
        }
        //TODO: add to strings
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
            broadcastToast(sMsg);
            mHandler.postDelayed(() -> finish(),5100);
        }else {
            sMsg = String.format(Locale.getDefault(), getString(R.string.common_google_play_services_unknown_issue), getString(R.string.app_name));
            broadcastToast(sMsg);
            cancelNotification(INTENT_SETUP);
            Intent intent = new Intent();
            intent.setAction(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // app settings
            Uri uri = Uri.fromParts("package",
                    Constants.ATRACKIT_ATRACKIT_CLASS, null);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //ActivityCompat.startActivity(getApplicationContext(),intent,null);
            finish();   // YUP - finish - dead dude
        }
    }
    private void signIn(){
//        startSplashActivityForResult(ATRACKIT_ATRACKIT_CLASS); // sign-in remotely
        if (!authInProgress) {
            try {
                if (!mReferenceTools.isNetworkConnected()) broadcastToast(getString(R.string.no_network_connection));
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail().requestId()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                authInProgress = true;
                if (signInActivityResultLauncher != null) signInActivityResultLauncher.launch(signInIntent);
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, "signIn error " + e.getMessage());
            }
        }
    }
    private void signInSilent(){
        if (!authInProgress) {
            authInProgress = true;
            final long timeMs = System.currentTimeMillis();
            final long lastAppLogin = appPrefs.getLastUserLogIn();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.CLIENT_ID)
                    .requestEmail().build();
            // Build a GoogleSignInClient with the options specified by gso.
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
            Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
            signInAccountTask.addOnCompleteListener(task -> {
                authInProgress = false;
                if (task.isSuccessful()){
                    try {
                        mGoogleAccount = task.getResult(ApiException.class);
                        if (!mGoogleAccount.isExpired()) {
                            // ensure we dont do this too often
                            onSignIn(false);
/*                            if (timeMs - lastAppLogin > TimeUnit.SECONDS.toMillis(60)) {
                                final String idToken = mGoogleAccount.getIdToken();
                                boolean useFireBase = (lastAppLogin == 0) || appPrefs.getFirebaseAvail();
                                if ((idToken != null) && (idToken.length() > 0) && useFireBase) {
                                    broadcastToast("online " + mGoogleAccount.getDisplayName());
                                } else if ((idToken != null) && (idToken.length() > 0))
                                    broadcastToast("online " + mGoogleAccount.getDisplayName());

                                if ((mViewPageAdapter.getBottomIndex() == 0) && appPrefs.getAppSetupCompleted())
                                    doHomeFragmentRefresh();
                            }*/
                        }
                        else{
                            if (mReferenceTools.isNetworkConnected()) {
                                signIn();
                            } else {
                                if (appPrefs.getLastUserID().length() == 0)
                                    doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null);
                                else {
                                    onSignIn(true);
                                    if ((mViewPageAdapter.getBottomIndex() == 0) && appPrefs.getAppSetupCompleted())
                                        doHomeFragmentRefresh();
                                }
                                /*CustomConfirmDialog confirmDialog = CustomConfirmDialog.newInstance(Constants.QUESTION_NETWORK,
                                        getString(R.string.no_network_connection), MainActivity.this);
                                confirmDialog.setMessageText(getString(R.string.no_network_connection));
                                confirmDialog.setSingleButton(true);
                                confirmDialog.show(getSupportFragmentManager(), CustomConfirmDialog.class.getSimpleName());*/
                            }
                        }
                    }catch (ApiException aie){
                        FirebaseCrashlytics.getInstance().recordException(aie);
                        signInError(aie);
                    }
                }
                else{ // unsuccessful task
                    if (mReferenceTools.isNetworkConnected()) {
                        signIn();
                    } else {
                        if ((appPrefs.getLastUserID().length() == 0) || ((timeMs - lastAppLogin) < TimeUnit.DAYS.toMillis(2)))
                            doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null);
                        else
                            onSignIn(true);
                    }
                }
            });
        }
    }

    private void onSignIn(boolean bOffline){
            Context context = getApplicationContext();
            long timeMs = System.currentTimeMillis();
            long lastAppLogin = appPrefs.getLastUserLogIn();
            String sLastId = appPrefs.getLastUserID();
            if ((mGoogleAccount == null) && !bOffline && mReferenceTools.isNetworkConnected()) {
                signIn();
                return;
            }
            authInProgress = false;
            String PersonId = (!bOffline) ? mGoogleAccount.getId() : sLastId;
            if (bOffline && ((PersonId == null) || (PersonId.length() == 0)) && (timeMs - lastAppLogin > TimeUnit.DAYS.toMillis(1))) {
                doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), MainActivity.this);
                return;
            }
            long previousTimeMs = 0L;
            userPrefs = UserPreferences.getPreferences(context,PersonId);
            if (userPrefs != null){
                previousTimeMs = userPrefs.getLastUserSignIn();
            }
            if (bOffline && previousTimeMs == 0){
                doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), MainActivity.this);
                return;
            }
            String PersonName = ((bOffline) || (mGoogleAccount == null)) ? userPrefs.getLastUserName() : mGoogleAccount.getDisplayName() ;
            if (bOffline) PersonName += ATRACKIT_SPACE + getString(R.string.action_offline);
            int configCount = mSessionViewModel.getConfigCount(PersonId);
            Log.w(LOG_TAG, "onSignIn " + "offline " + bOffline + " " + PersonName);
            appPrefs.setLastUserLogIn(timeMs);
            String sMsgName;
            if (!bOffline && mGoogleAccount != null) {
                appPrefs.setLastUserID(mGoogleAccount.getId());
                userPrefs.setLastUserName(mGoogleAccount.getDisplayName());
                userPrefs.setUserEmail(mGoogleAccount.getEmail());
                userPrefs.setLastUserSignIn(timeMs);
                if (previousTimeMs == 0) doImageWork(mGoogleAccount.getPhotoUrl(),1);
                else{
                    if (timeMs - previousTimeMs > TimeUnit.DAYS.toMillis(1))
                        sMsgName = Utilities.getTimeDayString(previousTimeMs) + ATRACKIT_SPACE + PersonName;
                    else
                        sMsgName = Utilities.getTimeString(previousTimeMs) + ATRACKIT_SPACE + PersonName;
                    broadcastToast(sMsgName);
                }
            }else{
                sMsgName = getString(R.string.action_offline) + ATRACKIT_SPACE + PersonName;
                if (timeMs - previousTimeMs > TimeUnit.DAYS.toMillis(1))
                    sMsgName = Utilities.getTimeDayString(previousTimeMs) + ATRACKIT_SPACE + sMsgName;
                else
                    sMsgName = Utilities.getTimeString(previousTimeMs) + ATRACKIT_SPACE + sMsgName;
                broadcastToast(sMsgName);
            }
            mSavedStateViewModel.setUserIDLive(PersonId);
            final Device device = Device.getLocalDevice(context);
            final String sDeviceID = device.getUid();
            boolean bHasAudio = Utilities.hasSpeaker(context);
            String sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_GYM);
            // first load
            if (configCount == 0){
                doSetupUserConfig(PersonId);
                createNotificationChannels(); // setup notification manager and channels if needed
                appPrefs.setLastSyncInterval(TimeUnit.MINUTES.toMillis(15));
                appPrefs.setNetworkCheckInterval(TimeUnit.MINUTES.toMillis(1));
                appPrefs.setPhoneSyncInterval(TimeUnit.MINUTES.toMillis(10));
                appPrefs.setUseLocation(false);
                appPrefs.setUseSensors(false);
                userPrefs.setUseFirebase(true);
                userPrefs.setRestAutoStart(true);
                userPrefs.setTimedRest(false);
                userPrefs.setArcheryCallDuration(20);  //seconds
                userPrefs.setArcheryEndDuration(60);
                userPrefs.setArcheryRestDuration(90);
                userPrefs.setWeightsRestDuration(90);
                userPrefs.setPrefByLabel(Constants.USER_PREF_SHOW_GOALS, false);
                userPrefs.setPrefByLabel(USER_PREF_USE_VIBRATE,(Utilities.hasVibration(context)));
                userPrefs.setPrefByLabel(USER_PREF_USE_AUDIO,bHasAudio);
                userPrefs.setPrefByLabel(USER_PREF_USE_NOTIFICATION, true);
                appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(20));
                appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, false);
                appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, false);
                appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, false);
                userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_START,0);
                userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_END,0);
                bUseKG = !(Locale.getDefault().equals(Locale.US));
                userPrefs.setUseKG(bUseKG);
                userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
                appPrefs.setPrefByLabel(Constants.LABEL_USE_GRID, true);
                userPrefs.setPrefByLabel(USER_PREF_STOP_INVEHICLE, true);
                userPrefs.setPrefStringByLabel(sLabel, Long.toString(WORKOUT_TYPE_STRENGTH));  // default to strength training
                mMessagesViewModel.setUseLocation(false);
                userPrefs.setConfirmExitApp(true);
                userPrefs.setConfirmStartSession(true);
                userPrefs.setConfirmEndSession(true);
                userPrefs.setConfirmSetSession(true);
                userPrefs.setConfirmDeleteSession(true);
                userPrefs.setLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION,90);
                userPrefs.setLongPrefByLabel(Constants.USER_PREF_REST_DURATION,270);
                userPrefs.setConfirmDuration(3000);
                userPrefs.setDefaultNewReps(10);
                userPrefs.setDefaultNewSets(3);
                userPrefs.setStepsSampleRate(120L);
                userPrefs.setBPMSampleRate(60L);
                userPrefs.setOthersSampleRate(300L);
                mSavedStateViewModel.setUserPreferences(userPrefs);
            }
            else {
                bUseKG = userPrefs.getUseKG();
                mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
                mSavedStateViewModel.setRepsDefault(userPrefs.getDefaultNewReps());
                mSavedStateViewModel.loadFromPreferences(userPrefs);
                userPrefs.setLastUserName(PersonName);
                appPrefs.setLastUserID(PersonId);
                mSavedStateViewModel.setUserPreferences(userPrefs);
                if (userPrefs.getPrefStringByLabel(sLabel).length() == 0) userPrefs.setPrefStringByLabel(sLabel,Long.toString(WORKOUT_TYPE_STRENGTH));
                if (appPrefs.getUseSensors()) {
                    if (userPrefs.getStepsSampleRate() == 0) userPrefs.setStepsSampleRate(120L);
                    if (userPrefs.getBPMSampleRate() == 0) userPrefs.setBPMSampleRate(60L);
                    if (userPrefs.getOthersSampleRate() == 0) userPrefs.setOthersSampleRate(300L);
                }
                if (userPrefs.getAskAge()){
                    String sAge = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                    if ((sAge.length() > 0) && TextUtils.isDigitsOnly(sAge)){
                        int iAge = 0;
                        try {
                            iAge = Integer.parseInt(sAge);
                            if (iAge < 10) {
                                mMessagesViewModel.setUseLocation(false); // default to false
                                appPrefs.setUseLocation(false);
                            } else {
                                mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
                            }
                        } catch (NumberFormatException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Log.e(LOG_TAG, "error age " + e.getMessage());
                        }
                    }else
                        mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
                }
                else mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
                if (checkApplicationSetup()) {
                    if (!appPrefs.getAppSetupCompleted()) {
                        cancelNotification(INTENT_SETUP);
                        appPrefs.setAppSetupCompleted(true);
                        createWorkout(PersonId, sDeviceID);
                        mWorkout._id = 2;
                        mWorkout.start = 0;
                        mWorkout.end = 0;
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                        broadcastToast(getString(R.string.label_setup_complete));
                    }
                    List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.ATRACKIT_SETUP, ATRACKIT_ATRACKIT_CLASS);
                    if (configList != null && (configList.size() > 0)){
                        Configuration configSetup = configList.get(0);
                        if (configSetup.stringValue1 != null){
                            configSetup.stringValue1 = null;
                            mSessionViewModel.updateConfig(configSetup);
                        }
                    }
                    Configuration configHistory = null;
                    List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, PersonId);
                    Long lStart = System.currentTimeMillis();
                    if ((existingConfigs != null) && existingConfigs.size() > 0){
                        configHistory = existingConfigs.get(0);
                        lStart = Long.parseLong(configHistory.stringValue2); // history end value
                    }else{
                        Configuration configStart = new Configuration(Constants.MAP_HISTORY_RANGE, PersonId, Constants.ATRACKIT_ATRACKIT_CLASS, 0L,"0","0");
                        mSessionViewModel.insertConfig(configStart);
                        userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_START,0);
                        userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_END,0);
                    }
                    if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED)){
                        startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                    }else wearNodeId = appPrefs.getLastNodeID();
                }
                else{
                    appPrefs.setAppSetupCompleted(false);
                    createNotificationChannels(); // setup notification manager and channels if needed
                    Bundle bundle = new Bundle();
                    bundle.putString(INTENT_SETUP, "false");
                    mWorkout = new Workout();
                    mWorkout._id = (System.currentTimeMillis());
                    mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                    mWorkout.start = -1; mWorkout.end = -1; // setup mode !
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    sessionSetCurrentState(WORKOUT_SETUP);
                    sendNotification(Constants.INTENT_SETUP, bundle);
                    String sAction = INTENT_SETUP;
                    if (userPrefs != null) {
                        boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                        if (!bSetup)
                            sAction = INTENT_PERMISSION_DEVICE;
                    }else
                        sAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                    startSplashActivityForResult(sAction);
                    return;
                }
            }
            appPrefs.setBackgroundLoadComplete(true);
            userPrefs.setLastUserSignIn(timeMs);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.METHOD, "google");
            mFirebaseAnalytics.setUserId(PersonId);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);
            FirebaseCrashlytics.getInstance().setUserId(PersonId);
            List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(ATRACKIT_EMPTY, PersonId);
            Configuration historyConfig = null;
            Configuration stateConfig = null;
            Configuration deviceConfig = null;
            if (configList != null)
                for(Configuration config : configList){
                    if (MAP_CURRENT_STATE.equals(config.stringName)) stateConfig = config;
                    if (MAP_HISTORY_RANGE.equals(config.stringName)) historyConfig = config;
                    if (Constants.KEY_DEVICE1.equals(config.stringName)) deviceConfig = config;
                }
            if (historyConfig == null) {
                historyConfig = new Configuration(Constants.MAP_HISTORY_RANGE, PersonId, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, "0", "0");
                mSessionViewModel.insertConfig(historyConfig);
            }
            if (stateConfig == null) {
                stateConfig = new Configuration(MAP_CURRENT_STATE, PersonId, Constants.ATRACKIT_ATRACKIT_CLASS, (long)Constants.WORKOUT_INVALID, "0", "0");
                mSessionViewModel.insertConfig(stateConfig);
            }
            if (!bOffline && mGoogleAccount != null) {
                Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                if (PersonPhotoUri != null) {
                    String sFile = userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE);
                    if (sFile.length() == 0) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
                        }else
                            doImageWork(PersonPhotoUri, 1);
                    }else {
                        boolean bitmapOK = false;
                        Uri uriImage = Uri.parse(sFile);
                        try {
                            Bitmap bitmap = Utilities.getMyBitmap(context, uriImage);
                            if (bitmap != null) {
                                bitmapOK = true;
                            }
                        }catch (Exception e){
                            if (e.equals(FileNotFoundException.class) && (userPrefs != null)){
                                userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE,Constants.ATRACKIT_EMPTY); // cause a fix on reload homepage
                            }
                        }
                        if (!bitmapOK) {
                            Log.e(LOG_TAG, "fixing profile image file " + sFile);
                            doImageWork(PersonPhotoUri, 1);
                        }

                    }
                }
                //doSkuLookupJob(PersonId);
            }
            if ((deviceConfig == null) || (deviceConfig.stringValue.length() == 0)){
                final Configuration cf = deviceConfig;
                Wearable.getNodeClient(this).getLocalNode().addOnSuccessListener(node -> {
                    if (cf == null) {
                        Configuration c = new Configuration(Constants.KEY_DEVICE1, PersonId, node.getId(), System.currentTimeMillis(), device.getModel(), sDeviceID);
                        mSessionViewModel.insertConfig(c);
                    }else{
                        cf.stringValue = node.getId();
                        mSessionViewModel.updateConfig(cf);
                    }
                }).addOnFailureListener(e -> {
                    if (cf == null) {
                        Configuration c = new Configuration(Constants.KEY_DEVICE1, PersonId, ATRACKIT_EMPTY, System.currentTimeMillis(), device.getModel(), sDeviceID);
                        mSessionViewModel.insertConfig(c);
                    }
                });
            }
            appPrefs.setDeviceID(sDeviceID);
            mSavedStateViewModel.setDeviceID(sDeviceID);
            mViewPageAdapter.setUserDevice(PersonId,sDeviceID);  // tab adapter
            String sReviewSettings = (userPrefs != null) ? userPrefs.getPrefStringByLabel(Constants.USER_PREF_REPORT_SETTINGS) : ATRACKIT_EMPTY;
            int myType = SELECTION_BODYPART_AGG;
            if (sReviewSettings.length() > 0 && sReviewSettings.contains(SHOT_DELIM)){
                String[] paramsReport = sReviewSettings.split(SHOT_DELIM);
                if (paramsReport[0].length() > 0) myType = Integer.parseInt(paramsReport[0]);
            }
            mViewPageAdapter.setDefaultType(myType);
            if (!checkApplicationSetup()) {
                startSplashActivityForResult(INTENT_SETUP);
                return;
            }else {
                cancelNotification(INTENT_SETUP);
                if (!appPrefs.getAppSetupCompleted()){
                    appPrefs.setAppSetupCompleted(true);
                    broadcastToast(getString(R.string.label_setup_complete));
                }
            }

            if (Utilities.hasSensorDevicesPermission(context)) {
                new TaskDeviceConfigLoad(PersonId).run();
                sdtRunnable = new TaskSensorDailyTotals(PersonId,mCalendar);
                sdtHandle = scheduler.scheduleWithFixedDelay(sdtRunnable,0,30,TimeUnit.SECONDS);
            }
            int lastState = Math.toIntExact(stateConfig.longValue);
            long lastLiveWorkoutID =  ((stateConfig.stringValue1 != null && stateConfig.stringValue1.length() > 0)
                    && (lastState == WORKOUT_LIVE || lastState == WORKOUT_PAUSED || lastState == WORKOUT_CALL_TO_LINE))
                    ? Long.parseLong(stateConfig.stringValue1): 0;
            List<Workout> inProgressList = mSessionViewModel.getInProgressWorkouts(PersonId, sDeviceID, Utilities.TimeFrame.BEGINNING_OF_MONTH);
            if ((inProgressList != null) && (inProgressList.size() > 0)){
                mCalendar.setTime(new Date());
                int iDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                int iHOD = mCalendar.get(Calendar.HOUR_OF_DAY);
                for (Workout inProgressWorkout : inProgressList) {
                    mCalendar.setTimeInMillis(inProgressWorkout.start);
                    int arrivingDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                    int arrivingHOD = mCalendar.get(Calendar.HOUR_OF_DAY);
                    if ((iDOY == arrivingDOY) && ((iHOD - arrivingHOD) <= 3) || (lastLiveWorkoutID == inProgressWorkout._id)) {
                        Log.w(LOG_TAG, "loading in progress " + iDOY + " " + inProgressWorkout.toString());
                        //  int state = Utilities.currentWorkoutState(workoutList.get(0));
                        List<WorkoutSet> sets = mSessionViewModel.getWorkoutSetByWorkoutID(inProgressWorkout._id, inProgressWorkout.userID, inProgressWorkout.deviceID);
                        List<WorkoutMeta> metas = mSessionViewModel.getWorkoutMetaByWorkoutID(inProgressWorkout._id, inProgressWorkout.userID, inProgressWorkout.deviceID);
                        if (sets == null || sets.size() == 0) {
                            mSessionViewModel.deleteWorkout(inProgressWorkout);
                        } else{
                            for (int i = 0; i < sets.size(); i++) {
                                WorkoutSet set = sets.get(i);
                                if ((set.start > 0) && (set.end == 0)) {
                                    mWorkoutSet = set;
                                    break;
                                } else {
                                    // nothing started
                                    if (((set.start == 0) && (set.end == 0))
                                            || ((set.start > 0) && (set.end > 0) && (set.last_sync == 0))) {
                                            mWorkoutSet = set; // only for the first one
                                    }
                                }
                            }
                            mWorkout = inProgressWorkout;
                            if (mWorkoutSet == null)
                                mWorkoutSet = sets.get(sets.size() - 1);
                            if ((metas != null) && (metas.size() > 0)) {
                                mWorkoutMeta = metas.get(0);
                            }
                            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                            mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID));
                            mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(mWorkout.activityID));
                            mSavedStateViewModel.setActiveWorkout(mWorkout);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                            mSavedStateViewModel.setToDoSets(sets);
                            // we have sets and everything
                            if ((iHOD - arrivingHOD) > 1) {
                                String started = String.format(Locale.getDefault(), "%1$02d:%2$02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                                String sQuestion = String.format(Locale.getDefault(), getString(R.string.confirm_resume_session), mWorkout.activityName, started);
                                doConfirmDialog(Constants.QUESTION_RESUME_END, sQuestion, null);
                            } else {
                                if (mWorkoutSet.start > 0 && mWorkoutSet.end == 0) {
                                    sessionResume();
                                } else {
                                    sessionSetCurrentState(WORKOUT_PAUSED);
                                    if ((mWorkoutSet.start > 0) && (mWorkoutSet.end > 0) && (mWorkoutSet.last_sync == 0)) {
                                        long pStart = SystemClock.elapsedRealtime();
                                        if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                                            // go to the gym confirm fragment
                                            mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                                            GymConfirmFragment gymConfirmFragment = GymConfirmFragment.newInstance(1);
                                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                            transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                                            transaction.add(R.id.top_container, gymConfirmFragment);
                                            transaction.addToBackStack("dialog_gym_confirm");
                                            transaction.commit();
                                        }
                                        if (Utilities.isShooting(mWorkout.activityID)) {
                                            int icon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                                            int color = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                                            int rest = 0;
                                            mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                                            sessionSetCurrentState(WORKOUT_PAUSED);
                                            ShootingConfirmFragment shootingConfirmFragment = ShootingConfirmFragment.newInstance(icon,color, mWorkoutSet, mWorkout,rest,mWorkoutMeta);
                                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                            transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                                            transaction.add(R.id.top_container, shootingConfirmFragment);
                                            transaction.addToBackStack("dialog_shooting_confirm");
                                            transaction.commit();
                                        }
                                    } else {
                                        Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                                        mMessagesViewModel.addLiveIntent(editIntent);
                                    }
                                }
                            }
                            return;  // avoid remaining startup stuff
                        }
                    }
                    else{ // not same day within 3 hours!
                        try {
                            mSessionViewModel.deleteWorkout(inProgressWorkout);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        if (mStartupIntent == null) {
            Log.e(LOG_TAG, "onSignIn about to ACTIVE_LOGIN");
            mStartupIntent = new Intent(INTENT_ACTIVE_LOGIN);
            mStartupIntent.putExtra(KEY_FIT_USER, PersonId);
            mStartupIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            mStartupIntent.putExtra(KEY_FIT_VALUE, previousTimeMs);
        }else Log.e(LOG_TAG, "onSignIn about to use startupIntent " + mStartupIntent.getAction());
        new Handler().post(() -> {
            handleIntent(mStartupIntent);
            mStartupIntent = null;
        });

    }

    private void setupListenersAndIntents(Context context){
     //   if (mCustomIntentReceiver == null) mCustomIntentReceiver = new CustomIntentReceiver(mResultReceiver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction(INTENT_DAILY);
        intentFilter.addAction(INTENT_EXER_RECOG);
        intentFilter.addAction(INTENT_RECOG);

        MainActivity.this.registerReceiver(mCustomIntentReceiver, intentFilter);
        mCustomIntentReceiver.setRegistered(true);

        if (mIntentReceiver == null) mIntentReceiver = new AppIntentReceiver();
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(INTENT_ACTIVE_LOGIN);
        notifyFilter.addAction(INTENT_CLOUD_SYNC);
        notifyFilter.addAction(Constants.INTENT_PHONE_SYNC);
        notifyFilter.addAction(INTENT_QUIT_APP);
        notifyFilter.addAction(INTENT_HOME_REFRESH);
        notifyFilter.addAction(INTENT_BIND_DEVICE);
        notifyFilter.addAction(INTENT_INPROGRESS_RESUME);
        notifyFilter.addAction(INTENT_NETWORK_CHECK);
        notifyFilter.addAction(INTENT_TEMPLATE_START);
        notifyFilter.addAction(INTENT_ACTIVE_START);
        notifyFilter.addAction(INTENT_ACTIVE_STOP);
        notifyFilter.addAction(INTENT_WORKOUT_REPORT);
        notifyFilter.addAction(INTENT_WORKOUT_EDIT);
        notifyFilter.addAction(INTENT_WORKOUT_DELETE);
        notifyFilter.addAction(INTENT_SET_DELETE);
        notifyFilter.addAction(INTENT_ACTIVE_PAUSE);
        notifyFilter.addAction(INTENT_ACTIVE_RESUMED);
        notifyFilter.addAction(INTENT_ACTIVESET_START);
        notifyFilter.addAction(INTENT_ACTIVESET_STOP);
        notifyFilter.addAction(INTENT_SUMMARY_DAILY);
        notifyFilter.addAction(INTENT_SCHEDULE_TRIGGER);
        notifyFilter.addAction(INTENT_GOAL_TRIGGER);
        notifyFilter.addAction(INTENT_CALL_TRIGGER);
        notifyFilter.addAction(INTENT_ACTIVESET_STOP);
        notifyFilter.addAction(Constants.INTENT_SETUP);
        notifyFilter.addAction(INTENT_LOCATION_UPDATE);
        notifyFilter.addAction(INTENT_MESSAGE_TOAST);
        notifyFilter.addAction(Constants.INTENT_TOTALS_REFRESH);
        MainActivity.this.registerReceiver(mIntentReceiver, notifyFilter);
    }

    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission(int requestType) {
        FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(requestType);
        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
        return (mGoogleAccount != null) && GoogleSignIn.hasPermissions(mGoogleAccount, fitnessOptions);
    }
    private class getPermissionTask implements Runnable{
        int requestType;

        getPermissionTask(int iType){
            requestType = iType;
        }
        @Override
        public void run() {
            FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(requestType);
            if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
            GoogleSignIn.requestPermissions(
                    MainActivity.this,
                    RC_REQUEST_FIT_PERMISSION_AND_CONTINUE,
                    mGoogleAccount,
                    fitnessOptions);
        }
    }
    /** Launches the Google SignIn activity to request OAuth permission for the user. */
    private void requestOAuthPermission(int requestType) {
        mSavedStateViewModel.setColorID(requestType);
        String sMessage = getString(R.string.ask_fit_read_aggregate);
        if (requestType == 1) sMessage = getString(R.string.ask_fit_write_session);
        if (requestType == 2) sMessage = getString(R.string.ask_fit_read_session);
        getPermissionTask task = new getPermissionTask(requestType);
        doAlertDialogMessage(sMessage, task);
    }


    private void restoreWorkoutVariables(){
        if (mSavedStateViewModel.getActiveWorkout().getValue() != null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() != null) mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
    }
    private void doCloudHistory(String sUserID, long startTime,long endTime){
        if (startTime == 0 && endTime == 0){
            startTime = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_START);
            endTime = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_END);
        }
        if (startTime == 0 || endTime == 0) return;
        List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE,sUserID);
        Configuration configHistory;
        if ((configList != null) && (configList.size() > 0)) {
            configHistory = configList.get(0);
            configHistory.stringValue1 = Long.toString(startTime);
            configHistory.stringValue2 = "0";
            configHistory.longValue = 0;
            mSessionViewModel.updateConfig(configHistory);
        }else{
            configHistory = new Configuration(Constants.MAP_HISTORY_RANGE, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, Long.toString(startTime), "0");
            mSessionViewModel.insertConfig(configHistory);
        }
        boolean bBusy = (mMessagesViewModel.getWorkInProgress().getValue() != null) ? mMessagesViewModel.getWorkInProgress().getValue() : false;
        if (!bBusy) {
            mWorkout.start = startTime; mWorkout.end = endTime;
            doAsyncGoogleFitAction(TASK_ACTION_READ_CLOUD, mWorkout, mWorkoutSet);
        }
    }
    private void doGoogleFitSyncJob(Intent intent) {
        String sUser = intent.getStringExtra(KEY_FIT_USER);
        String sDevice = intent.getStringExtra(KEY_FIT_DEVICE_ID);
        if ((sUser.length() == 0) || (mSavedStateViewModel.getState() == WORKOUT_LIVE)) return;
        if (!hasOAuthPermission(5)) {
            requestOAuthPermission(5);
            return;
        }
        long timeMs = System.currentTimeMillis();
        long lastSync = appPrefs.getLastSync();
        long syncInterval = appPrefs.getLastSyncInterval();
        if ((timeMs - lastSync) < syncInterval) {
            Log.e(LOG_TAG, "too soon to do pending Sync - doGoogleFitSyncJob");
            return;
        }else{
            Log.e(LOG_TAG, "DOING pending Sync - doGoogleFitSyncJob");
        }
        Workout workout = new Workout(); workout.userID = sUser; workout.deviceID = sDevice;
        workout.start = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_WEEK);
        workout.end = timeMs;
        WorkoutSet workoutSet = new WorkoutSet(workout);
        doAsyncGoogleFitAction(TASK_ACTION_SYNC_WORKOUT, workout, workoutSet);
    }

    private void startCloudPendingSyncAlarm(){
        Context context = getApplicationContext();
        final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        final String sDeviceId = mSavedStateViewModel.getDeviceID();
        if ((sUserId == null || sUserId.length() == 0) || sDeviceId.length() == 0) return;
        long timeMs = System.currentTimeMillis();
        long lastSync = appPrefs.getLastSync();
        long syncInterval = appPrefs.getLastSyncInterval();
        if (syncInterval == 0) syncInterval =  (TimeUnit.MINUTES.toMillis(5));
        long triggerTimeMs = timeMs + syncInterval;
        boolean isConnected = mReferenceTools.isNetworkConnected();
        DateTuple pendingTuple = mSessionViewModel.getWorkoutUnSyncCount(sUserId,sDeviceId);
        DateTuple pendingSetTuple = mSessionViewModel.getWorkoutSetUnSyncCount(sUserId,sDeviceId);
        Intent syncCheckIntent = new Intent(INTENT_CLOUD_SYNC);
        syncCheckIntent.putExtra(KEY_FIT_TYPE,1); // not triggered intent
        syncCheckIntent.putExtra(KEY_FIT_USER, sUserId);
        syncCheckIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
        PendingIntent mNetworkSyncPendingIntent = PendingIntent.getBroadcast(context, ALARM_PENDING_SYNC_CODE, syncCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNetworkAlarmManager.cancel(mNetworkSyncPendingIntent);
        if (isConnected && ((pendingTuple.sync_count > 0 || pendingSetTuple.sync_count > 0) || (timeMs - lastSync) > syncInterval)) {  // check don't wait
            Log.e(LOG_TAG,"pendingSync starting now " + syncInterval + " last " + lastSync + "calc diff " + (timeMs - lastSync));
            mMessagesViewModel.addLiveIntent(syncCheckIntent);
        }else {
            Log.e(LOG_TAG, "setting exact alarm for pendingSync");
            mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mNetworkSyncPendingIntent);
        }
    }
    private void startExactTimeAlarm(String intentAction, long workoutID,long setID,long interval){
        long timeMs = System.currentTimeMillis();

        String sUserId = appPrefs.getLastUserID();
        Intent triggeredIntent = new Intent(intentAction);
        if (INTENT_GOAL_TRIGGER.equals(intentAction)|| INTENT_CALL_TRIGGER.equals(intentAction)) {
            triggeredIntent.putExtra(KEY_FIT_WORKOUTID, workoutID);
            if (setID > 0) triggeredIntent.putExtra(KEY_FIT_WORKOUT_SETID, setID);
            triggeredIntent.putExtra(KEY_FIT_TYPE, GOAL_TYPE_DURATION);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Long.toString(interval));
            triggeredIntent.putExtra(KEY_FIT_USER, sUserId);
        }else{
            Log.w(LOG_TAG, "setting exact Intent " + intentAction);
            triggeredIntent.putExtra(KEY_FIT_USER, sUserId);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
            int state = Math.toIntExact(workoutID);
            triggeredIntent.putExtra(KEY_FIT_VALUE, state);
        }
        triggeredIntent.setPackage(ATRACKIT_ATRACKIT_CLASS);
        long triggerTimeMs = timeMs + interval;
        PendingIntent goalPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_EXACT_TIME_CODE, triggeredIntent, PendingIntent.FLAG_ONE_SHOT);
        mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, goalPendingIntent);
    }

    private void cancelExactTimeAlarm(String intentAction, long workoutID,long setID,long interval){
        long timeMs = System.currentTimeMillis();
        String sUserId = appPrefs.getLastUserID();
        Intent triggeredIntent = new Intent(intentAction);
        if (INTENT_GOAL_TRIGGER.equals(intentAction)|| INTENT_CALL_TRIGGER.equals(intentAction)) {
            triggeredIntent.putExtra(KEY_FIT_WORKOUTID, workoutID);
            if (setID > 0) triggeredIntent.putExtra(KEY_FIT_WORKOUT_SETID, setID);
            triggeredIntent.putExtra(KEY_FIT_TYPE, GOAL_TYPE_DURATION);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Long.toString(interval));
            triggeredIntent.putExtra(KEY_FIT_USER, sUserId);
        }else{
            triggeredIntent.putExtra(KEY_FIT_USER, sUserId);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
            int state = Math.toIntExact(workoutID);
            triggeredIntent.putExtra(KEY_FIT_VALUE, state);
        }
        Log.w(LOG_TAG, "cancelling exact Intent " + intentAction);
        triggeredIntent.setPackage(ATRACKIT_ATRACKIT_CLASS);
        PendingIntent goalPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_EXACT_TIME_CODE, triggeredIntent, PendingIntent.FLAG_ONE_SHOT);
        mNetworkAlarmManager.cancel(goalPendingIntent);
    }


    private void sessionReport(Intent requestIntent){
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(MainActivity.this, R.anim.enter_anim, R.anim.no_anim);
        long activityID = requestIntent.getLongExtra(Constants.KEY_FIT_ACTIVITYID,0l);
        String sValue = (activityID > 0) ? mReferenceTools.getFitnessActivityTextById(activityID) : ATRACKIT_EMPTY;
        Intent intent = new Intent(getApplicationContext(), ReportDetailActivity.class);
        intent.putExtras(requestIntent);
        intent.putExtra(ReportDetailActivity.EXTRA_NAME, sValue);
        if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(),requestIntent.getStringExtra(KEY_FIT_USER));
        intent.putExtra(ReportDetailActivity.EXTRA_USE_KG, userPrefs.getUseKG());
        intent.putExtra(ReportDetailActivity.ARG_RETURN_RESULT, 1);
        reportActivityResultLauncher.launch(intent,options);
        //ActivityCompat.startActivityForResult(MainActivity.this, intent, REQUEST_REPORT_CODE, options.toBundle());
    }
    private void sessionStart(){
        try{
            boolean isConnected = mReferenceTools.isNetworkConnected();
            restoreWorkoutVariables();
            if (mWorkout == null) return;
            if ((mWorkout.userID.length() == 0) || (mWorkout.activityID == null) || (mWorkout.activityID < DetectedActivity.WALKING)) return;
            if (!hasOAuthPermission(1)){
                requestOAuthPermission(1);
                return;
            }
            if (mWorkoutSet != null){
                // check to remove empty "builder set"
                if (mWorkoutSet.scoreTotal == FLAG_BUILDING) {
                    mSessionViewModel.deleteWorkoutSet(mWorkoutSet);
                    mWorkoutSet = null;
                }
            }
            // good to go now!
            final long timeMs = System.currentTimeMillis();
            final long timeRealElapsed = SystemClock.elapsedRealtime();
            final long originalWorkoutID = mWorkout._id;
            SensorDailyTotals localSDT = mSavedStateViewModel.getSDT();
            if (localSDT == null) localSDT = mSessionViewModel.getTopSensorDailyTotal(mWorkout.userID);;
            mWorkout.offline_recording = (isConnected ? 0 : 1);
            mWorkout.start = timeMs;
            List<WorkoutSet> sets = new ArrayList<>();
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            DateTuple setsTuple = mSessionViewModel.getWorkoutSetTupleByWorkoutID(mWorkout.userID,mWorkout.deviceID, mWorkout._id);
            if ((setsTuple.sync_count > 0)){
                sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                sets.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                if (mWorkoutSet == null) mWorkoutSet = sets.get(0);
                mWorkoutSet.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
                if (mWorkout.setCount != sets.size()) mWorkout.setCount = sets.size();
                mWorkout.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
            }else {
                if (mWorkoutSet == null) createWorkoutSet();
                mWorkoutSet.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
                mWorkoutSet.setCount = 1;
                sets = new ArrayList<>();
                sets.add(mWorkoutSet);
                mWorkout.setCount = 1;
                mWorkout.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
            }

            // setup the parent
            // create new from existing workout or from template
            String sWorkoutName = ((mWorkout.name != null && (mWorkout.name.length() > 0)) ? mWorkout.name : mWorkout.activityName);
            if ((mWorkout.parentID != null) && (mWorkout.parentID < 0) && (originalWorkoutID != 1 && originalWorkoutID != 2)) {
                sessionNewFromTemplate(mWorkout);
            }else {
                sWorkoutName = mWorkout.activityName;
                sessionNewFromOld(sets);
            }
            createWorkoutMeta();
            mWorkoutMeta.start = mWorkout.start;
            String sPartDay = Utilities.getPartOfDayString(timeMs);
            if (Utilities.isShooting(mWorkout.activityID)){
                mWorkoutMeta.description = sPartDay + ATRACKIT_SPACE + sWorkoutName + ATRACKIT_SPACE + mWorkout.setCount + getString(R.string.label_shoot_ends);
            }else
                mWorkoutMeta.description = sPartDay + ATRACKIT_SPACE + sWorkoutName;
            if (mWorkout.name == null && mWorkout.parentID == null) mWorkout.name = mWorkoutMeta.description;
            mWorkoutSet.start = mWorkout.start;
            mWorkoutSet.realElapsedStart = timeRealElapsed;   // system elapsed time at start held in move_mins until workout completed!
            if ((mWorkoutSet.activityID == null) || (mWorkoutSet.activityID != mWorkout.activityID)) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }
            String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
            if (sBPM == null){
                if (localSDT != null && (localSDT.deviceBPM > 0 && localSDT.lastDeviceBPM > 0))
                    sBPM = Float.toString(localSDT.deviceBPM);
                else
                if (mMessagesViewModel.getBpmMsg().getValue() != null)
                    sBPM = mMessagesViewModel.getBpmMsg().getValue();
                else {
                    long timeMidnight = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                    List<TwoIDsTuple> tuples = mSessionViewModel.getLatLngCounts(timeMidnight,0);
                    if ((tuples.size() > 0) && (tuples.get(0).rowID > 0)){
                        List<ATrackItLatLng> listLatLng = mSessionViewModel.getATrackItLatLngsById(tuples.get(0).itemCount);
                        if ((listLatLng != null) && (listLatLng.size() > 0)){
                            ATrackItLatLng atlatlng = listLatLng.get(0);
                            if (atlatlng.BPM != null){
                                sBPM = Float.toString(atlatlng.BPM);
                            }
                        }
                    }
                }
            }
            if ((sBPM != null) && (sBPM.length() > 0)){
                Float f = Float.parseFloat(sBPM);
                mWorkoutSet.startBPM = f;
            }
            String steps = mMessagesViewModel.getDeviceStepsMsg().getValue();
            if ((steps != null) && (steps.length() > 0)) {
                mWorkoutMeta.start_steps = Long.parseLong(steps);
            } else {
                if (localSDT != null && (localSDT.deviceStep > 0 && localSDT.lastDeviceStep > 0))
                    steps = Float.toString(localSDT.deviceStep);
                else
                    steps = mMessagesViewModel.getStepsMsg().getValue();
                if (steps != null) {
                    mWorkoutMeta.start_steps = Long.parseLong(steps);
                }
            }
            if (mWorkoutMeta.start_steps > 0) {
                DailyCounter stepCounter = new DailyCounter();
                stepCounter.LastUpdated = timeMs;
                stepCounter.FirstCount = mWorkoutMeta.start_steps;
                if (mWorkout.goal_steps > 0) {
                    stepCounter.GoalCount = mWorkout.goal_steps;  // this allow both types of goals simultaneously
                    stepCounter.GoalActive = System.currentTimeMillis();
                } else stepCounter.GoalActive = 0;
                mSavedStateViewModel.setSteps(stepCounter);
            }
            if (sBPM != null) {
                if ((sBPM.length() > 0) && (Utilities.isInteger(sBPM, -1) || Utilities.isFloat(sBPM, -1))) {
                    DailyCounter bpmCounter = new DailyCounter();
                    bpmCounter.LastUpdated = timeMs;
                    if (Utilities.isFloat(sBPM, -1))
                        bpmCounter.FirstCount = Math.round(Float.parseFloat(sBPM));
                    else
                        bpmCounter.FirstCount = (int)Integer.parseInt(sBPM);
                    if (mWorkoutSet.rest_duration != null && mWorkoutSet.rest_duration > 0) {
                        bpmCounter.GoalCount = (mWorkoutSet.rest_duration * -1);
                        bpmCounter.GoalActive = System.currentTimeMillis();
                    } else bpmCounter.GoalActive = 0;
                    mSavedStateViewModel.setBPM(bpmCounter);
                }
            }
            if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
            Bundle paramsA = new Bundle();
            paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_START);
            paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
            paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            String snackMsg = mWorkoutMeta.description;
            String connectedMsg = (mWorkout.offline_recording == 1) ? getString(R.string.action_offline): getString(R.string.action_live);
            if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)) {
                mMessagesViewModel.setDetectedReps(0);
                DailyCounter repCounter = new DailyCounter();
                repCounter.FirstCount = 0;
                if (mWorkoutSet.repCount != null) repCounter.GoalCount = mWorkoutSet.repCount;
                repCounter.LastUpdated = timeMs;
                mSavedStateViewModel.setReps(repCounter);
                mSavedStateViewModel.setReps(repCounter);
                if (mWorkoutSet.call_duration != null && mWorkoutSet.call_duration > 0)
                    snackMsg = getString(R.string.action_call);
                else
                if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0))
                    snackMsg = connectedMsg + ATRACKIT_SPACE+ mWorkoutSet.exerciseName;
                else
                    snackMsg = connectedMsg + ATRACKIT_SPACE+ mWorkoutSet.activityName;
                doSnackbar(snackMsg, Snackbar.LENGTH_SHORT);
                if (mFirebaseAnalytics != null){
                    Bundle paramsB = new Bundle();
                    paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                }
            } else {
                // start of tennis game !
                if (mWorkout.activityID == Constants.WORKOUT_TYPE_TENNIS) {
                    String sPlayer1 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_1));
                    if (sPlayer1.length() == 0) sPlayer1 = getString(R.string.label_player_1);
                    String sPlayer2 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_2));
                    if (sPlayer2.length() == 0) sPlayer2 = getString(R.string.label_player_2);
                    tennisGame = new TennisGame(sPlayer1, sPlayer2);
                }

                doSnackbar(snackMsg, Snackbar.LENGTH_SHORT);
                if (mFirebaseAnalytics != null){
                    Bundle paramsB = new Bundle();
                    paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                }
            }
            if ((mWorkout.offline_recording == 0)) {
                doAsyncGoogleFitAction(TASK_ACTION_RECORD_START, mWorkout, mWorkoutSet); // start recording
                if (mSavedStateViewModel.getIsGym() && (mGoogleAccount != null)) doRegisterExerciseDetectionService(mGoogleAccount);
                if (mWorkout.activityID != WORKOUT_TYPE_ATRACKIT) doAsyncGoogleFitAction(Constants.TASK_ACTION_START_SESSION, mWorkout, mWorkoutSet); // start session
                // doRecordingWork(Constants.TASK_ACTION_START_SESSION);  // subscribe to sensor stuff
            } else
                if (!isConnected){
                    mWorkout.offline_recording = 1;
                    broadcastToast(getString(R.string.action_offline));
                }

            // room inserts via viewModel - repository chain
            mSessionViewModel.insertWorkoutMeta(mWorkoutMeta);

            // room updates of final changes via viewModel - repository chain
            mWorkout.lastUpdated = timeMs;
            mSessionViewModel.updateWorkout(mWorkout);
            mSessionViewModel.updateWorkoutSet(mWorkoutSet);

            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            mSavedStateViewModel.setDirtyCount(0);
            if (userPrefs.getTimedRest()){
                if ((mWorkoutSet.call_duration != null) && (mWorkoutSet.call_duration > 0)){
                    startExactTimeAlarm(INTENT_CALL_TRIGGER, mWorkout._id, mWorkoutSet._id, TimeUnit.MINUTES.toMillis(mWorkoutSet.call_duration));
                    if (userPrefs.getTimedRest()
                            && ((mWorkoutSet.goal_duration != null) && (mWorkoutSet.goal_duration > 0))) {
                        startExactTimeAlarm(INTENT_GOAL_TRIGGER, mWorkout._id, mWorkoutSet._id, TimeUnit.MINUTES.toMillis(mWorkoutSet.call_duration + mWorkoutSet.goal_duration));
                    }
                    sessionSetCurrentState(WORKOUT_CALL_TO_LINE);
                }else {
                    if (userPrefs.getTimedRest() && mWorkout.goal_duration > 0) {
                        startExactTimeAlarm(INTENT_GOAL_TRIGGER, mWorkout._id, mWorkoutSet._id,
                                TimeUnit.MINUTES.toMillis(mWorkout.goal_duration));
                    }
                    sessionSetCurrentState(WORKOUT_LIVE);
                }
            }else
                sessionSetCurrentState(WORKOUT_LIVE);

            // now notify
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_START, resultData);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                sendDataClientMessage(INTENT_ACTIVE_START, resultData);
            }
        }catch(Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            String sMsg = "Error starting session " + e.getMessage();
            Log.e(LOG_TAG, sMsg);
            broadcastToast(sMsg);
        }
    }

    private void sessionPause(){
        if ((mWorkout == null) || (mWorkoutSet == null)) restoreWorkoutVariables();
        if (mWorkoutSet == null) return;
        long timeMs = SystemClock.elapsedRealtime();
        mSavedStateViewModel.setPauseStart(timeMs);
        sessionSetCurrentState(WORKOUT_PAUSED);
        if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        Bundle paramsA = new Bundle();
        paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_PAUSE);
        paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
        paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
        // now notify
        Bundle resultData = new Bundle();
        resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
        resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
        resultData.putString(KEY_FIT_USER, mWorkout.userID);
        //if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
        if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                && (Utilities.getRingerMode(getApplicationContext()) != AudioManager.RINGER_MODE_SILENT)) vibrate(1);
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_PAUSE, resultData);
        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
            sendDataClientMessage(INTENT_ACTIVE_PAUSE, resultData);

        doPauseStopDialog();
    }
    private void sessionRepeatSet(){
        restoreWorkoutVariables();
        if (mWorkoutSet == null) return;
        int currentIndex = mSavedStateViewModel.getSetIndex(); //  (mWorkoutSet.setCount == 0) ? 1: mWorkoutSet.setCount;
        mSessionViewModel.updateWorkoutSetSetCount(mWorkoutSet.workoutID,currentIndex,1); // add to remaining
        WorkoutSet set = new WorkoutSet(mWorkoutSet);
        set._id  = (System.currentTimeMillis());
        set.start = 0L; set.end = 0L;
        set.setCount = ++currentIndex;
        set.realElapsedStart = null; set.realElapsedEnd = null;
        set.last_sync = 0; set.device_sync = null; set.meta_sync =null;
        set.duration = 0; set.pause_duration =0; set.scoreTotal = FLAG_PENDING;
        set.startBPM = null; set.endBPM = null;
        set.lastUpdated = set._id;
        mSessionViewModel.insertWorkoutSet(set);
        mWorkoutSet = set;
        List<WorkoutSet> todo = new ArrayList<>();
        if (mSavedStateViewModel.getToDoSets().getValue() != null)
            todo = mSavedStateViewModel.getToDoSets().getValue();
        else
            todo = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutSet.workoutID,mWorkoutSet.userID,mWorkoutSet.deviceID);
        mWorkout.setCount = todo.size()+1;
        mSavedStateViewModel.setSetIndex(currentIndex);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        todo.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
        mSavedStateViewModel.setToDoSets(todo);
    }
    private void sessionNewFromOld(List<WorkoutSet> sets){
        if ((mWorkout == null)||(sets == null)) return;
        List<WorkoutSet> list = new ArrayList<>(sets.size());
        long timeMs = System.currentTimeMillis();
        long originalWorkoutID = mWorkout._id;
        mWorkout._id = timeMs;
        mWorkout.lastUpdated = timeMs;
        mSessionViewModel.insertWorkout(mWorkout);
        if (sets.size() > 1) {
            int defaultSet = 1;
            for (WorkoutSet set : sets) {
                set.workoutID = timeMs;
                set.userID = mWorkout.userID;
                set.deviceID = mWorkout.deviceID;
                if (set.scoreTotal == FLAG_BUILDING) set.scoreTotal = FLAG_PENDING;
                set.setCount = defaultSet++;
                set.lastUpdated = timeMs;
                if (defaultSet == 2) mWorkoutSet = set;
                mSessionViewModel.insertWorkoutSet(set);
                list.add(set);
            }
        } else {
            mWorkoutSet.workoutID = timeMs;
            mWorkoutSet.userID = mWorkout.userID;
            mWorkoutSet.deviceID = mWorkout.deviceID;
            mWorkoutSet.lastUpdated = timeMs;
            mWorkoutSet.setCount = 1;
            mWorkoutSet.scoreTotal = FLAG_PENDING;
            mSessionViewModel.insertWorkoutSet(mWorkoutSet);
            list.add(mWorkoutSet);
        }
        mSavedStateViewModel.setToDoSets(list);
        // can now delete the workoutID 1 sets
        if (originalWorkoutID == 1L || originalWorkoutID == 2L)
            mSessionViewModel.deleteWorkoutSetByWorkoutID(originalWorkoutID);
    }

    private void sessionNewFromTemplate(Workout w){
       List<WorkoutSet>  sets = mSessionViewModel.getWorkoutSetByWorkoutID(w._id, w.userID, w.deviceID);
       long timeMs = System.currentTimeMillis();
        mWorkout = new Workout(w);
        mWorkout._id = timeMs;
        mWorkout.lastUpdated = mWorkout._id;
        mWorkout.parentID = w._id;  // control setting this outside the function
        mWorkout.start = 0; mWorkout.end = 0;
        mWorkout.duration = 0; mWorkout.last_sync = 0;
        mWorkout.device_sync = 0;
        mSessionViewModel.insertWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        if (sets != null) {
            for (WorkoutSet oldSet : sets) {
                WorkoutSet s = new WorkoutSet(oldSet);
                s._id = timeMs + (oldSet.setCount * 20);
                s.scoreTotal = Constants.FLAG_PENDING; s.last_sync = 0;
                s.device_sync = 0L;
                s.start = 0;s.end = 0;
                s.realElapsedEnd = 0L;
                s.realElapsedStart = 0L;
                s.pause_duration = 0;
                s.duration = 0;
                s.lastUpdated = timeMs;
                s.scoreTotal = Constants.FLAG_PENDING;
                s.weightTotal = 0F;
                s.workoutID = mWorkout._id;
                mSessionViewModel.insertWorkoutSet(s);
                if (s.setCount == mWorkoutSet.setCount) mWorkoutSet = s;
            }
            mSavedStateViewModel.setToDoSets(sets);
        }
    }

    private void sessionResume(){
        long timeMs;
        restoreWorkoutVariables();
        if (mWorkout == null || mWorkoutSet == null) return;
        if (!hasOAuthPermission(1)){
            requestOAuthPermission(1);
            return;
        }
        // invalid resume - start it!
        if ((mWorkoutSet.setCount == 0 || mWorkoutSet.setCount == 1) && mWorkoutSet.start == 0){
            sessionStart();
            return;
        }
        if ((mWorkout.start == 0) || (mWorkout.start > 0 && mWorkout.end > 0) || (mWorkout._id == 1) || (mWorkout._id == 2)){
            sessionStart();
            return;
        }
        if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        if (mSavedStateViewModel.getState() == WORKOUT_PAUSED) {
            long pauseStart = mSavedStateViewModel.getPauseStart();
            if (pauseStart > 0) {
                timeMs = SystemClock.elapsedRealtime();
                mWorkout.pause_duration += (timeMs - pauseStart);
                mWorkoutSet.pause_duration += (timeMs - pauseStart);
            }
            if (mFirebaseAnalytics != null){
                Bundle paramsA = new Bundle();
                paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_RESUME);
                paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            }
            mSavedStateViewModel.setPauseStart(0l);
        }
        timeMs = System.currentTimeMillis();
        if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)){
            mMessagesViewModel.setDetectedReps(0);
            DailyCounter repCounter = mSavedStateViewModel.getReps();
            if (repCounter == null) repCounter = new DailyCounter();
            repCounter.FirstCount = 0;
            repCounter.LastUpdated = timeMs;
            mSavedStateViewModel.setReps(repCounter);

            if (mFirebaseAnalytics != null){
                Bundle paramsA = new Bundle();
                paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0)) {
                    if (mWorkoutSet.exerciseID != null) paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkoutSet.exerciseID));
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkoutSet.exerciseName);
                }else{
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                }
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            }
        }else{
            if (mFirebaseAnalytics != null){
                Bundle paramsA = new Bundle();
                paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            }
        }
        if (mWorkoutSet.start == 0L){
            mWorkoutSet.start = timeMs;
            mWorkoutSet.realElapsedStart = SystemClock.elapsedRealtime();
        }
        mWorkoutSet.end = 0L;
        mWorkout.end = 0L;  // not finished
        mWorkout.lastUpdated = System.currentTimeMillis();
        mSessionViewModel.updateWorkout(mWorkout);
        mSessionViewModel.updateWorkoutSet(mWorkoutSet);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setPauseStart(0L);  // not under pause now
        int currentState = mSavedStateViewModel.getState();
        String snackMsg = mWorkoutMeta.description;
        if (Utilities.isShooting(mWorkout.activityID) || Utilities.isGymWorkout(mWorkout.activityID)){
            if (userPrefs.getTimedRest()
                    && ((mWorkoutSet.call_duration != null) && (mWorkoutSet.call_duration > 0)))
                snackMsg = getString(R.string.action_call);
            else
                if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0))
                    snackMsg = mWorkoutSet.exerciseName + ATRACKIT_SPACE + getString(R.string.action_go);
                else
                    snackMsg = mWorkoutSet.activityName + ATRACKIT_SPACE + getString(R.string.action_go);
        }
        doSnackbar(snackMsg, Snackbar.LENGTH_SHORT);
        if (userPrefs.getTimedRest()){
            if ((mWorkoutSet.call_duration != null) && (mWorkoutSet.call_duration > 0)){
                startExactTimeAlarm(INTENT_CALL_TRIGGER, mWorkout._id, mWorkoutSet._id, TimeUnit.MINUTES.toMillis(mWorkoutSet.call_duration));
                if (userPrefs.getTimedRest()
                        && ((mWorkoutSet.goal_duration != null) && (mWorkoutSet.goal_duration > 0))) {
                    startExactTimeAlarm(INTENT_GOAL_TRIGGER, mWorkout._id, mWorkoutSet._id, TimeUnit.MINUTES.toMillis(mWorkoutSet.call_duration + mWorkoutSet.goal_duration));
                }
                sessionSetCurrentState(WORKOUT_CALL_TO_LINE);
            }else {
                if (userPrefs.getTimedRest() && mWorkout.goal_duration > 0) {
                    startExactTimeAlarm(INTENT_GOAL_TRIGGER, mWorkout._id, mWorkoutSet._id,
                            TimeUnit.MINUTES.toMillis(mWorkout.goal_duration));
                }
                sessionSetCurrentState(WORKOUT_LIVE);
            }
        }else
            sessionSetCurrentState(WORKOUT_LIVE);
        if (currentState != WORKOUT_LIVE) {
            // now notify
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_RESUMED, resultData);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                sendDataClientMessage(INTENT_ACTIVE_RESUMED, resultData);
        }


    }
    private void sessionCompleteActiveSet(long timeMs){
        restoreWorkoutVariables();
        if (mWorkoutSet == null) return;
        if (mWorkoutSet.start == 0){
            int iSet =  mWorkoutSet.setCount;
            if ((iSet > 1) && (mSavedStateViewModel.getToDoSetsSize() >= iSet)){
                WorkoutSet set = mSavedStateViewModel.getToDoSets().getValue().get(iSet-2);
                mWorkoutSet.start = set.end + ((set.rest_duration != null) ? set.rest_duration : 0);
                Log.e(LOG_TAG, "fixing missing set start time #" + mWorkoutSet.setCount);
            }else mWorkoutSet.start = mWorkout.start;
        }
        if (mWorkoutSet.end == 0L)
            mWorkoutSet.end = timeMs;
        else
            mWorkoutSet.rest_duration = (timeMs - mWorkoutSet.end);
        mWorkoutSet.realElapsedEnd = SystemClock.elapsedRealtime();
        // paused finish add pause
        if (mSavedStateViewModel.getState() == WORKOUT_PAUSED) {
            long pauseStart = mSavedStateViewModel.getPauseStart();
            mWorkoutSet.pause_duration += (timeMs - pauseStart);
            mWorkout.pause_duration += (timeMs - pauseStart);
        }
        if (mWorkoutSet.start > 0)
            mWorkoutSet.duration = (mWorkoutSet.end - mWorkoutSet.start);
        mSavedStateViewModel.setDirtyCount(1);
        if ((mWorkoutSet.goal_duration != null) && (mWorkoutSet.goal_duration > 0)){
            cancelExactTimeAlarm(INTENT_GOAL_TRIGGER, mWorkout._id, mWorkoutSet._id, TimeUnit.MINUTES.toMillis(mWorkoutSet.call_duration + mWorkoutSet.goal_duration));
        }
        // check if reps are "reasonable"
        if (Utilities.isGymWorkout(mWorkoutSet.activityID) || Utilities.isShooting(mWorkoutSet.activityID)){
            int detected = mMessagesViewModel.getDetectedRep();
            if (detected > 0) {
                long set_duration = (mWorkoutSet.end - mWorkoutSet.start);
                float per_rep = TimeUnit.MILLISECONDS.toSeconds(set_duration) / (float)detected;
                if ((detected > 1) && ((per_rep > 1F) && (per_rep < 10F))) {
                    mWorkoutSet.repCount = mMessagesViewModel.getDetectedRep();
                }
                mMessagesViewModel.setDetectedReps(0);
            }
        }

        DailyCounter stepCounter = mSavedStateViewModel.getSteps();
        String sMsg = mMessagesViewModel.getDeviceStepsMsg().getValue();
        if (stepCounter != null){
            if ((sMsg!=null) && (sMsg.length() > 0))
                stepCounter.LastCount = Long.parseLong(sMsg);

            if ((stepCounter.FirstCount > 0) && ((stepCounter.LastCount - stepCounter.FirstCount) >= 0))
                mWorkoutSet.stepCount = Math.toIntExact(stepCounter.LastCount - stepCounter.FirstCount);
        }
        else{
            if ((sMsg != null) && (sMsg.length()  > 0)){
                if ((mWorkoutMeta.start_steps > 0) && ((Long.parseLong(sMsg) - mWorkoutMeta.start_steps) > 0))
                    mWorkoutSet.stepCount = Math.toIntExact(Long.parseLong(sMsg) - mWorkoutMeta.start_steps);
            }
        }
        String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
        if (sBPM == null){
            if (mMessagesViewModel.getBpmMsg().getValue() != null)
                sBPM = mMessagesViewModel.getBpmMsg().getValue();
            else {
                long timeMidnight = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                List<TwoIDsTuple> tuples = mSessionViewModel.getLatLngCounts(timeMidnight,0);
                if ((tuples.size() > 0) && (tuples.get(0).rowID > 0)){
                    List<ATrackItLatLng> listLatLng = mSessionViewModel.getATrackItLatLngsById(tuples.get(0).itemCount);
                    if ((listLatLng != null) && (listLatLng.size() > 0)){
                        ATrackItLatLng atlatlng = listLatLng.get(0);
                        if (atlatlng.BPM != null){
                            sBPM = Float.toString(atlatlng.BPM);
                        }
                    }
                }
            }
        }
        if ((sBPM != null) && (sBPM.length() > 0)){
            Float f = Float.parseFloat(sBPM);
            mWorkoutSet.endBPM = f;
        }
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        mSessionViewModel.updateWorkoutSet(mWorkoutSet);
        sessionSaveCurrentSet();
        //mSavedStateViewModel.setDirtyCount(0);
        Bundle resultData = new Bundle();
        resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
        resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
        resultData.putString(KEY_FIT_USER, mWorkout.userID);

        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
             && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
        if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                && (Utilities.getRingerMode(getApplicationContext()) != AudioManager.RINGER_MODE_SILENT)) vibrate(2);
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVESET_STOP, resultData);
        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
            sendDataClientMessage(INTENT_ACTIVESET_STOP, resultData);
    }

    private void sessionStartNextSet(){
        try {
            restoreWorkoutVariables();
            if (mWorkout == null) return;
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet == null) createWorkoutSet();
            int currentSetIndex = (mWorkoutSet.setCount == 0) ? 1: mWorkoutSet.setCount;

            long timeMs = System.currentTimeMillis();
            long lastRestDuration = (mWorkoutSet.rest_duration != null)? mWorkoutSet.rest_duration : 0;
            // if WORKOUT_LIVE ensure tidy up
            boolean bNew = false;
            if ((mWorkoutSet != null) && (mWorkoutSet.start > 0)){
                if (mWorkoutSet.end == 0) {
                    mWorkoutSet.end = timeMs;
                    mWorkoutSet.realElapsedEnd = SystemClock.elapsedRealtime();
                }else
                    mWorkoutSet.rest_duration = timeMs - mWorkoutSet.end;

                if (mWorkoutSet.endBPM == null || mWorkoutSet.endBPM == 0){
                    String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
                    if (sBPM == null){
                        if (mMessagesViewModel.getBpmMsg().getValue() != null)
                            sBPM = mMessagesViewModel.getBpmMsg().getValue();
                        else {
                            long timeMidnight = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                            List<TwoIDsTuple> tuples = mSessionViewModel.getLatLngCounts(timeMidnight,0);
                            if ((tuples.size() > 0) && (tuples.get(0).rowID > 0)){
                                List<ATrackItLatLng> listLatLng = mSessionViewModel.getATrackItLatLngsById(tuples.get(0).itemCount);
                                if ((listLatLng != null) && (listLatLng.size() > 0)){
                                    ATrackItLatLng atlatlng = listLatLng.get(0);
                                    if (atlatlng.BPM != null){
                                        sBPM = Float.toString(atlatlng.BPM);
                                    }
                                }
                            }
                        }
                    }
                    if ((sBPM != null) && (sBPM.length() > 0)){
                        Float f = Float.parseFloat(sBPM);
                        mWorkoutSet.endBPM = f;
                    }
                }
                mWorkoutSet.lastUpdated = System.currentTimeMillis();
                WorkoutSet testSet = mSessionViewModel.getWorkoutSetById(mWorkoutSet._id, mWorkoutSet.userID, mWorkoutSet.deviceID);
                bNew = (testSet == null);
                if (!bNew){
                    mSessionViewModel.updateWorkoutSet(mWorkoutSet);
                }else{
                    mSessionViewModel.insertWorkoutSet(mWorkoutSet);
                }
                mSavedStateViewModel.setDirtyCount(0);

                Bundle resultData = new Bundle();
                resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
                resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                resultData.putString(KEY_FIT_USER, mWorkout.userID);
                if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_ACTIVESET_SAVED, resultData);

                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                    sendDataClientMessage(Constants.INTENT_ACTIVESET_SAVED,resultData);
                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                    sessionGoogleCurrentSet(mWorkoutSet);  // no more changes to therefore Google it!
            }

            currentSetIndex++;
            bNew = false;
            List<WorkoutSet> sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutSet.workoutID,mWorkoutSet.userID,mWorkoutSet.deviceID);
            sets.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
            int setSize = sets.size();
            // if the last one is used already create a fresh set
            if ((setSize > 1) && currentSetIndex <= setSize){
                WorkoutSet tester = sets.get(currentSetIndex - 1);
                if (mWorkoutSet._id != tester._id)
                    mWorkoutSet = tester;   // zero based set!
                mWorkoutSet.workoutID = mWorkout._id;
                mWorkoutSet.realElapsedStart = SystemClock.elapsedRealtime();
                mWorkoutSet.start = timeMs;  mWorkoutSet.scoreTotal = FLAG_PENDING;
                mWorkoutSet.rest_duration = lastRestDuration;
                mWorkoutSet.end = 0L; mWorkoutSet.last_sync = 0; mWorkoutSet.device_sync = null;
                mWorkoutSet.duration = 0; mWorkoutSet.pause_duration = 0;
                mWorkoutSet.setCount = currentSetIndex;
                mWorkoutSet.lastUpdated = System.currentTimeMillis();
                mSessionViewModel.updateWorkoutSet(mWorkoutSet);
            }else{
                WorkoutSet oldSet = mWorkoutSet;
                mWorkoutSet = new WorkoutSet(oldSet);
                mWorkoutSet.setCount = currentSetIndex;
                mWorkoutSet.workoutID = mWorkout._id;
                mWorkoutSet.start =timeMs; mWorkoutSet.end=0L;
                mWorkoutSet.last_sync = 0; mWorkoutSet.device_sync = null;
                mWorkoutSet.realElapsedStart = SystemClock.elapsedRealtime();
                mWorkoutSet.duration = 0; mWorkoutSet.pause_duration =0;
                mWorkoutSet.exerciseName = ATRACKIT_EMPTY;
                mWorkoutSet.exerciseID = null; mWorkoutSet.scoreTotal = FLAG_PENDING;
                mWorkoutSet.lastUpdated = System.currentTimeMillis();
                sets.add(mWorkoutSet);
                mSessionViewModel.insertWorkoutSet(mWorkoutSet);
                bNew = true;
            }
            mSavedStateViewModel.setViewState(9,Math.toIntExact(mWorkoutSet.activityID));   // set location to activity or exercise name
            String steps = mMessagesViewModel.getDeviceStepsMsg().getValue();
            long lSteps = 0;
            if ((steps != null) && (steps.length()  > 0)) {
                lSteps = Long.parseLong(steps);
            } else {
                steps = mMessagesViewModel.getStepsMsg().getValue();
                if (steps != null) {
                    lSteps = Long.parseLong(steps);
                }
            }
            if (lSteps > 0) {
                DailyCounter stepCounter = new DailyCounter();
                stepCounter.LastUpdated = System.currentTimeMillis();
                stepCounter.FirstCount = lSteps;
                stepCounter.GoalActive = 0;
                mSavedStateViewModel.setSteps(stepCounter);
            }
            String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
            if (sBPM == null){
                if (mMessagesViewModel.getBpmMsg().getValue() != null)
                    sBPM = mMessagesViewModel.getBpmMsg().getValue();
                else {
                    long timeMidnight = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                    List<TwoIDsTuple> tuples = mSessionViewModel.getLatLngCounts(timeMidnight,0);
                    if ((tuples.size() > 0) && (tuples.get(0).rowID > 0)){
                        List<ATrackItLatLng> listLatLng = mSessionViewModel.getATrackItLatLngsById(tuples.get(0).itemCount);
                        if ((listLatLng != null) && (listLatLng.size() > 0)){
                            ATrackItLatLng atlatlng = listLatLng.get(0);
                            if (atlatlng.BPM != null){
                                sBPM = Float.toString(atlatlng.BPM);
                            }
                        }
                    }
                }
            }
            if ((sBPM != null) && (sBPM.length() > 0)){
                Float f = Float.parseFloat(sBPM);
                mWorkoutSet.startBPM = f;
            }
            if (lSteps > 0) {
                DailyCounter stepCounter = new DailyCounter();
                stepCounter.LastUpdated = System.currentTimeMillis();
                stepCounter.FirstCount = lSteps;
                stepCounter.GoalActive = 0;
                mSavedStateViewModel.setSteps(stepCounter);
            }
            mSavedStateViewModel.setSetIndex(currentSetIndex);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if (bNew) mSavedStateViewModel.setToDoSets(sets);
            if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
            Bundle paramsB = new Bundle();
            paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
            if ((Utilities.isGymWorkout(mWorkout.activityID)) && (mWorkoutSet != null)) {
                paramsB.putInt(FirebaseAnalytics.Param.INDEX, mWorkoutSet.setCount);
                if (mWorkoutSet.exerciseID != null) paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkoutSet.exerciseID));
                paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkoutSet.exerciseName);
            } else {
                if (mWorkoutSet != null) paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, "-" + Long.toString(mWorkoutSet.activityID));
                paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkoutSet.activityName);
            }
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                    && (Utilities.getRingerMode(MainActivity.this) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
            if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                    && (Utilities.getRingerMode(MainActivity.this) != AudioManager.RINGER_MODE_SILENT)) vibrate(1);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVESET_START, resultData);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                sendDataClientMessage(INTENT_ACTIVESET_START, resultData);

            sessionSaveCurrentSet();
            sessionResume();
        }catch(Exception e){
            String sMsg = e.getMessage();
            FirebaseCrashlytics.getInstance().recordException(e);
            if (sMsg != null) Log.e(LOG_TAG, sMsg);
        }
    }

    private void sessionSetCurrentState(int state){
        int previousState = mSavedStateViewModel.getState();
        mSavedStateViewModel.setCurrentState(state);
        if (mSavedStateViewModel.getUserIDLive().getValue() == null) return;
        String sUser = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        String sDevice = mSavedStateViewModel.getDeviceID();
        if (sUser != null && (sUser.length() > 0)) {
            String sLastUpdated = Long.toString(System.currentTimeMillis());
            List<Configuration> list = mSessionViewModel.getConfigurationLikeName(MAP_CURRENT_STATE, sUser);
            Configuration config = null;
            if (list != null && (list.size() > 0)) config = list.get(0);
            if (config != null) {
                if (config.longValue != (long) state) {
                    config.longValue = state;
                    if ((state == WORKOUT_LIVE || state == WORKOUT_PAUSED) && (mWorkout != null && mWorkout._id > 2))
                        config.stringValue1 = Long.toString(mWorkout._id);
                    else
                        config.stringValue1 = ATRACKIT_EMPTY;

                    config.stringValue2 = sLastUpdated;
                    mSessionViewModel.updateConfig(config);
                }
            } else {
                config = new Configuration(MAP_CURRENT_STATE, sUser, sDevice, (long) state, ATRACKIT_EMPTY, sLastUpdated);
                mSessionViewModel.insertConfig(config);
            }
        }
    }
    private void sessionStop(){
        boolean isConnected = mReferenceTools.isNetworkConnected();
        List<OneTimeWorkRequest> requestList = new ArrayList<>();
        try {
            restoreWorkoutVariables();
            if ((mWorkout == null) || (mWorkoutSet == null))
                return;
            boolean bMeta = false;
            final long timeMs = System.currentTimeMillis();
            mWorkout.end = timeMs;   // IMPORTANT
            sessionCompleteActiveSet(timeMs);
            List<WorkoutSet> sets = new ArrayList<>();
            if (mWorkout.scoreTotal < FLAG_PENDING) mWorkout.scoreTotal = FLAG_PENDING;
            if (mWorkoutMeta == null) mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta == null){
                mWorkoutMeta = mSessionViewModel.getWorkoutMetaByWorkoutID(mWorkout._id,mWorkout.userID,mWorkout.deviceID).get(0);
            }
            if (mWorkoutMeta == null){
                createWorkoutMeta();
                mWorkoutMeta._id = mWorkout.start;
                bMeta = true;
            }
            mWorkout.duration = (mWorkout.end - mWorkout.start);
            // do the total score
            sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutSet.workoutID,mWorkoutSet.userID,mWorkoutSet.deviceID);
        //    if (mWorkout.setCount != sets.size()) mWorkout.setCount = sets.size();
            if (mSavedStateViewModel.getSteps() != null) {
                DailyCounter stepCounter = mSavedStateViewModel.getSteps();
                if ((stepCounter != null) && (stepCounter.LastCount > 0)) {
                    mWorkoutMeta.end_steps = stepCounter.LastCount;
                    if ((mWorkoutMeta.end_steps - mWorkoutMeta.start_steps) >= 0) {
                        mWorkout.stepCount = Math.toIntExact(mWorkoutMeta.end_steps - mWorkoutMeta.start_steps);
                        mWorkoutMeta.stepCount = mWorkout.stepCount;
                    }
                    mSavedStateViewModel.setSteps(null);  // clear it
                    Log.d(LOG_TAG, stepCounter.toJSONString());
                }
            }
            else {
                String sMsg = mMessagesViewModel.getDeviceStepsMsg().getValue();
                if ((sMsg != null) && (sMsg.length() > 0)) {
                    mWorkoutMeta.end_steps = Long.parseLong(sMsg);
                    mWorkout.stepCount = Math.toIntExact(mWorkoutMeta.end_steps - mWorkoutMeta.start_steps);
                    mWorkoutMeta.stepCount = mWorkout.stepCount;
                }
            }
            //TODO - look at using these values better
            if (mMessagesViewModel.getBpmMsg().getValue() != null) {
                String sBPM = mMessagesViewModel.getBpmMsg().getValue();
                if (sBPM != null) {
                    DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                    Log.e(LOG_TAG, bpmCounter.toString());
                    mSavedStateViewModel.setBPM(null);
                    Log.d(LOG_TAG, bpmCounter.toJSONString());
                }
            }
            if (mWorkoutMeta != null) mWorkoutMeta.duration = mWorkout.duration;
            if (Utilities.isShooting(mWorkout.activityID) && (sets.size() > 0)){
                int totalScore = 0; String totalScoreCard = ATRACKIT_EMPTY; String totalXYCard = ATRACKIT_EMPTY;
                for(WorkoutSet set: sets){
                    String items[] = set.score_card.split(Constants.SHOT_DELIM);
                    List<String> mScoreCard = new ArrayList<>(Arrays.asList(items));
                    int scoreVal = 0;
                    for (String score : mScoreCard){
                        if (score.equals(Constants.SHOT_X))
                            scoreVal += 10;
                        else {
                            try {
                                scoreVal += Integer.parseInt(score);
                            }catch (Exception e){
                                scoreVal += 0;
                            }
                        }
                    }
                    totalScore += scoreVal;
                    if (totalScoreCard.length() > 0)
                        totalScoreCard += Constants.SHOT_DELIM + set.score_card;
                    else
                        totalScoreCard = set.score_card;
                    if (totalXYCard.length() > 0)
                        totalXYCard += Constants.SHOT_DELIM + set.per_end_xy;
                    else
                        totalXYCard = set.per_end_xy;
                }
                mWorkout.scoreTotal = totalScore;
                mWorkoutMeta.totalScore = totalScore;
                mWorkoutMeta.score_card = totalScoreCard;
                mWorkoutMeta.per_end_xy = totalXYCard;
            }
            // save to our DB
            if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
            if ((mWorkout.offline_recording == 0)  && isConnected)
                if (Utilities.isGymWorkout(mWorkoutSet.activityID) && (sets.size() > 0)){
                    Iterator<WorkoutSet> iterator = sets.iterator();
                    while (iterator.hasNext()){
                        WorkoutSet set = iterator.next();
                        if (set.start == 0 || set.end == 0)
                            iterator.remove();
                        else{
                            if (set.isValid(true) && (set.last_sync == 0) && isConnected) sessionGoogleCurrentSet(set);
                        }

                    }
                }
                else
                    if ((mWorkoutSet.last_sync == 0) && isConnected) sessionGoogleCurrentSet(mWorkoutSet);

            mWorkout.lastUpdated = timeMs;
            mSessionViewModel.updateWorkout(mWorkout);
            if (!bMeta)
                mSessionViewModel.updateWorkoutMeta(mWorkoutMeta);
            else
                mSessionViewModel.insertWorkoutMeta(mWorkoutMeta);

            Data.Builder builder = new Data.Builder();
            builder.putString(KEY_FIT_USER, mWorkout.userID);
            builder.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
            builder.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                            .setInputData(builder.build()).build();
            if (mWorkManager == null) mWorkManager = WorkManager.getInstance(getApplicationContext());
            mWorkManager.enqueue(workRequest);

            if ((mWorkout.offline_recording == 0) && isConnected) {
                doAsyncGoogleFitAction(TASK_ACTION_RECORD_END, mWorkout, mWorkoutSet); // start recording
                if (mWorkout.activityID != WORKOUT_TYPE_ATRACKIT) doAsyncGoogleFitAction(Constants.TASK_ACTION_STOP_SESSION, mWorkout, mWorkoutSet); // stop session
            }else {
                if (isConnected)
                    doAsyncGoogleFitAction(TASK_ACTION_INSERT_HISTORY, mWorkout, mWorkoutSet); // stop session
                else
                    startNetworkCheckAlarm(MainActivity.this);
            }
            doSnackbar(getString(R.string.action_stop), Snackbar.LENGTH_SHORT);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            if (mSavedStateViewModel.getState() != WORKOUT_COMPLETED)
                sessionSetCurrentState(WORKOUT_COMPLETED);
            mSavedStateViewModel.setSetIndex(0);

            if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
            if (isConnected){
                Bundle paramsA = new Bundle();
                paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_STOP);
                paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            }

            // now notify
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            resultData.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                    && (Utilities.getRingerMode(getApplicationContext()) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
            if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                    && (Utilities.getRingerMode(getApplicationContext()) != AudioManager.RINGER_MODE_SILENT)) vibrate(5);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_STOP, resultData);
            if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)){
                sendDataClientMessage(INTENT_ACTIVE_STOP,resultData);
                // startWearPendingSyncAlarm();
            }
            new Handler().postDelayed(() -> {
                Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(refreshIntent);

            }, 1000);

        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG,"sessionStop failed " + e.getMessage());
        }
    }

    private Intent setResultIntent(String intentAction){
        Intent resultIntent  = new Intent(intentAction);
        if (mWorkout != null){
            resultIntent.putExtra(Workout.class.getSimpleName(), mWorkout);
            resultIntent.putExtra(KEY_FIT_WORKOUTID, mWorkout._id);
        }
        resultIntent.putExtra(KEY_FIT_TYPE, 0);  // internal
        if (mWorkoutSet != null){
            resultIntent.putExtra(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultIntent.putExtra(KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
        }
        resultIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
        resultIntent.putExtra(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
        return resultIntent;
    }
    private void showGoalsDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        List<Configuration> addConfigList = new ArrayList<>();
        List<Configuration> configurationList = mSessionViewModel.getConfigurationLikeName(DataType.TYPE_HEART_POINTS.getName(), sUserId);
        long startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
        long endTime = System.currentTimeMillis();
        List<UserDailyTotals> uL = mSessionViewModel.getUserDailyTotals(sUserId, startTime,endTime,0); // today
        if ((configurationList != null) && configurationList.size() > 0){
            Configuration conf = configurationList.get(0);
            String sHeartPts = mMessagesViewModel.getHeartPtsMsg().getValue();
            if (sHeartPts == null) sHeartPts = Long.toString(0);
            conf.longValue = (Long.parseLong(sHeartPts));
            addConfigList.add(conf);
        }
        if (configurationList != null) configurationList.clear();
        configurationList =  mSessionViewModel.getConfigurationLikeName(DataType.TYPE_MOVE_MINUTES.getName(),sUserId);
        if ((configurationList != null) && configurationList.size() > 0){
            String sTemp = mMessagesViewModel.getMoveMinsMsg().getValue();
            if (sTemp == null) sTemp = Long.toString(0);
            Configuration conf = configurationList.get(0);
            conf.longValue = (Long.parseLong(sTemp));
            addConfigList.add(conf);

        }
        if (configurationList != null)configurationList.clear();
        configurationList = mSessionViewModel.getConfigurationLikeName(DataType.TYPE_STEP_COUNT_DELTA.getName(),sUserId);
        if ((configurationList != null) && configurationList.size() > 0){
            Configuration conf = configurationList.get(0);
            String sTemp = mMessagesViewModel.getStepsMsg().getValue();
            if (sTemp == null) sTemp = Long.toString(0);
            conf.longValue = Long.parseLong(sTemp);
            addConfigList.add(conf);
        }
        int itemCount = addConfigList.size();
        if (itemCount == 0){
            Configuration configuration = new Configuration(Constants.ATRACKIT_ATRACKIT_CLASS,sUserId, ATRACKIT_EMPTY,0L,null,null);
            addConfigList.add(configuration);
            doGoalsRefresh();
            broadcastToast(getString(R.string.action_load_goals));
        }
        String lastLocation = ATRACKIT_EMPTY;
        long timeMs = System.currentTimeMillis();
        int stepCount = 0; float distanceTravelled = 0f; float caloriesExpended = 0f; float minBPM = 0f; float maxBPM = 0f; float avgBPM = 0f;
        float minSpeed = 0f; float maxSpeed = 0f; float avgSpeed = 0f; int udtCount = 0; long maxRowid = 0;
        for(UserDailyTotals udt : uL){
            udtCount++;
            if ((udt._id > maxRowid) && (udt._id <= timeMs)) maxRowid = udt._id;
            if ((udt.lastLocation != null) && (udt.lastLocation.length() > 0)) lastLocation = udt.lastLocation;
            if (udt.stepCount > stepCount) stepCount = udt.stepCount;
            if (udt.distanceTravelled > distanceTravelled) distanceTravelled = udt.distanceTravelled;
            if (udt.caloriesExpended > caloriesExpended) caloriesExpended = udt.caloriesExpended;
            if (udt.minBPM > minBPM) minBPM = udt.minBPM;
            if (udt.maxBPM > maxBPM) maxBPM = udt.maxBPM;
            if (udt.avgBPM > avgBPM) avgBPM = udt.avgBPM;
            if (udt.minSpeed > minSpeed) minSpeed = udt.minSpeed;
            if (udt.maxSpeed > maxSpeed) maxSpeed = udt.maxSpeed;
            if (udt.avgSpeed > avgSpeed) avgSpeed = udt.avgSpeed;            
        }
        UserDailyTotals nowUDT = null;
        if (udtCount > 0){
            nowUDT = new UserDailyTotals();
            nowUDT._id = maxRowid;
            nowUDT.lastLocation = lastLocation;
            if (nowUDT.lastLocation.equals(getString(R.string.label_not_available0))){
                if (mSavedStateViewModel.getLocationAddress().length() > 0) {
                    nowUDT.lastLocation = mSavedStateViewModel.getLocationAddress();
                    Log.e(LOG_TAG,"setting address from saveState " + nowUDT.lastLocation);
                }
            }
            nowUDT.stepCount = stepCount;
            nowUDT.distanceTravelled = distanceTravelled;
            nowUDT.caloriesExpended = caloriesExpended;
            nowUDT.minBPM = minBPM;
            nowUDT.maxBPM = maxBPM;
            nowUDT.avgBPM = avgBPM;
            nowUDT.avgSpeed = avgSpeed;
            nowUDT.minSpeed = minSpeed;
            nowUDT.maxSpeed = maxSpeed;
        }
        GoalListAdapter goalsAdapter = new GoalListAdapter(getApplicationContext(),nowUDT, bUseKG);
        if (itemCount > 0)
            for(Configuration c : addConfigList){
                goalsAdapter.AddGoal(c);
            }

        View view = getLayoutInflater().inflate(R.layout.dialog_customlist,null,false);
        RelativeLayout rl = view.findViewById(R.id.customRelativeLayout);
        rl.setBackgroundColor(getColor(R.color.secondaryColor));
        TextView title = view.findViewById(R.id.activity_title);
        title.setText(getString(R.string.notify_channel_goals_name));
        RecyclerView recyclerView = view.findViewById(R.id.activity_list);
        recyclerView.setAdapter(goalsAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        goalsAdapter.setOnItemClickListener((view1, viewModel) -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        recyclerView.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void showSensorsDialog(){
        Context context = getApplicationContext();
        Resources res = context.getResources();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        SensorsListAdapter sensorAdapter = new SensorsListAdapter(context);
        // bpm
        boolean bAvail = Utilities.hasBPMSensor(context);
        String sText = getString(R.string.sensor_device_bpm);
        int iSensorCount = appPrefs.getBPMSensorCount();
        if (iSensorCount > 0) sText += " [" + iSensorCount + "]";
        int iIcon = res.getIdentifier("ic_heart_outline",Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        sensorAdapter.setItem(bAvail,sText,iIcon);
        // steps
        bAvail = Utilities.hasStepCounter(context);
        iIcon = res.getIdentifier("ic_footsteps_outline_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        sText = getString(R.string.sensor_device_steps);
        iSensorCount = appPrefs.getStepsSensorCount();
        if (iSensorCount > 0) sText += " [" + iSensorCount + "]";
        sensorAdapter.setItem(bAvail,sText,iIcon);
        // barometer
        bAvail = Utilities.hasPressureSensor(context);
        iIcon = res.getIdentifier("ic_barometer",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        sText = getString(R.string.sensor_device_pressure);
        iSensorCount = appPrefs.getPressureSensorCount();
        if (iSensorCount > 0) sText += " [" + iSensorCount + "]";
        sensorAdapter.setItem(bAvail,sText,iIcon);
        // temperature
        bAvail = Utilities.hasTemperatureSensor(context);
        iIcon = res.getIdentifier("ic_temperature",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        sText = getString(R.string.sensor_device_temperature);
        iSensorCount = appPrefs.getTempSensorCount();
        if (iSensorCount > 0) sText += " [" + iSensorCount + "]";
        sensorAdapter.setItem(bAvail,sText,iIcon);
        // humidity
        bAvail = Utilities.hasHumiditySensor(context);
        iIcon = res.getIdentifier("ic_humidity_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        sText = getString(R.string.sensor_device_humidity);
        iSensorCount = appPrefs.getHumiditySensorCount();
        if (iSensorCount > 0) sText += " [" + iSensorCount + "]";
        sensorAdapter.setItem(bAvail,sText,iIcon);
        // GPS
        bAvail = Utilities.hasGPS(context);
        if (bAvail)
            iIcon = res.getIdentifier("ic_location_enabled_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        else
            iIcon = res.getIdentifier("ic_location_disabled_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);

        sText = getString(R.string.sensor_device_location);
        sensorAdapter.setItem(bAvail,sText,iIcon);

        // Speaker
        bAvail = Utilities.hasSpeaker(context);
        if (bAvail)
            iIcon = res.getIdentifier("ic_speaker",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        else
            iIcon = res.getIdentifier("ic_speaker_na",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        sText = getString(R.string.sensor_device_speaker);
        sensorAdapter.setItem(bAvail,sText,iIcon);
        View view = getLayoutInflater().inflate(R.layout.dialog_customlist,null,false);
        TextView title = view.findViewById(R.id.activity_title);
        title.setText("Device Sensors");
        RecyclerView recyclerView = view.findViewById(R.id.activity_list);
        recyclerView.setAdapter(sensorAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        sensorAdapter.setOnItemClickListener(new SensorsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, SensorsListAdapter.SensorItem viewModel) {
                Log.w(LOG_TAG, "sensor item " + viewModel.getText());
            }
        });
        recyclerView.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        alertDialog.show();
    }
    private void sessionGoogleCurrentSet(WorkoutSet set){
            // completed!
        if (set == null) return;
            Bundle paramsB = new Bundle();
            if ((set.start > 0) && (set.end > 0)) {
                if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
                paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_STOP);
                paramsB.putInt(FirebaseAnalytics.Param.INDEX, set.setCount);
                paramsB.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, set.activityName);
                if ((Utilities.isGymWorkout(set.activityID)) && (set.exerciseName != null))  {
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(set.exerciseID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.exerciseName);
                } else {
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(set.activityID));
                    if (!Utilities.isShooting(set.activityID))
                        paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.activityName);
                    else
                        paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.score_card);
                }
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
//                if ((mWorkout.offline_recording == 0) && (set.last_sync == 0L)){
                try {
                    if (Utilities.isGymWorkout(set.activityID) && (set.scoreTotal != FLAG_NON_TRACKING))
                        doAsyncGoogleFitAction(Constants.TASK_ACTION_EXER_SEGMENT, mWorkout, set);
                    else
                        doAsyncGoogleFitAction(Constants.TASK_ACTION_ACT_SEGMENT, mWorkout, set);

                }catch(NullPointerException ne){
                    FirebaseCrashlytics.getInstance().recordException(ne);
                    String sMsg =  ne.getMessage();
                    if (sMsg != null) Log.d(LOG_TAG,sMsg);
                }
  //              }

            }else
                if ((set.start > 0) && (set.end == 0)){
                    if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
                    paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                    paramsB.putInt(FirebaseAnalytics.Param.INDEX, set.setCount);
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, set.activityName);
                    if ((Utilities.isGymWorkout(set.activityID)) && (set.exerciseName != null)){
                        paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(set.exerciseID));
                        paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.exerciseName);
                    } else {
                        paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, "-" + Long.toString(set.activityID));
                        if (!Utilities.isShooting(set.activityID))
                            paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.activityName);
                        else
                            paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.score_card);
                    }
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                }

    }
    private void sessionSaveCurrentSet(){
        try{
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet == null) return;
            if ((mWorkoutSet.workoutID <= 2) && (mWorkout._id > 2)) mWorkoutSet.workoutID = mWorkout._id;
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if ((mWorkoutSet.scoreTotal != FLAG_PENDING) || !mWorkoutSet.isValid(false)) return; // only valid single build

            mWorkoutSet.lastUpdated = System.currentTimeMillis();
            WorkoutSet testSet = mSessionViewModel.getWorkoutSetById(mWorkoutSet._id, mWorkoutSet.userID, mWorkoutSet.deviceID);
            boolean bNew = (testSet == null);
            if (!bNew){
                mSessionViewModel.updateWorkoutSet(mWorkoutSet);
            }else{
                mSessionViewModel.insertWorkoutSet(mWorkoutSet);
            }
            //mSavedStateViewModel.setDirtyCount(0);

            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);sendNotification(Constants.INTENT_ACTIVESET_SAVED, resultData);

            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                sendDataClientMessage(Constants.INTENT_ACTIVESET_SAVED,resultData);

        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    /**
     *  handleIntent - action all incoming intent requests
     *  - intents are verbs with parameters
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if ((intent == null) || (intent.getAction() == null)) return;
        String intentAction = intent.getAction();
        if (intent.hasExtra(Constants.KEY_FIT_HOST)) {
            String nodeId = intent.getStringExtra(Constants.KEY_FIT_HOST);
            DataMap dataMap = new DataMap();
            Workout workout = null;
            WorkoutSet workoutSet = null;
            if (intent.hasExtra(Constants.KEY_FIT_BUNDLE)) {
                Bundle bundle = intent.getBundleExtra(Constants.KEY_FIT_BUNDLE);
                if (bundle.containsKey(Workout.class.getSimpleName())) {
                    workout = bundle.getParcelable(Workout.class.getSimpleName());
                }
                if (bundle.containsKey(WorkoutSet.class.getSimpleName())) {
                    workoutSet = bundle.getParcelable(WorkoutSet.class.getSimpleName());
                }
            }
            String messagePath = Constants.ATRACKIT_EMPTY;
            switch (intent.getAction()) {
                case Intent.ACTION_VIEW:
                case INTENT_ACTIVE_RESUMED:
                    messagePath = Constants.DATA_START_WORKOUT;
                    break;
                case INTENT_ACTIVE_STOP:
                    messagePath = Constants.DATA_STOP_WORKOUT;
                    break;
                case INTENT_ACTIVESET_START:
                    messagePath = Constants.DATA_START_WORKOUT_SET;
                    break;
                case INTENT_ACTIVESET_STOP:
                    messagePath = Constants.DATA_STOP_WORKOUT_SET;
                    break;
            }
            Log.e(LOG_TAG,"intent from host " + nodeId + " " + messagePath);
            Gson gson = new Gson();
            if (workout != null)
                dataMap.putString(Workout.class.getSimpleName(), gson.toJson(workout));
            if (workoutSet != null)
                dataMap.putString(WorkoutSet.class.getSimpleName(), gson.toJson(workoutSet));
            if (messagePath.length() > 0) {
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(getApplicationContext())
                                .sendMessage(nodeId, messagePath, dataMap.toByteArray());

                sendMessageTask.addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "Message sent successfully");
                            } else {
                                Log.d(LOG_TAG, "Message failed");
                            }
                        });
            }
            return;
        }
        if (intentAction.equals(INTENT_MESSAGE_TOAST)){
            String sMessage = intent.getStringExtra(Constants.INTENT_EXTRA_MSG);
            if (sMessage != null) broadcastToast(sMessage);
            return;
        }
        Log.d(LOG_TAG, "handleIntent " + intentAction);
        long timeMs = System.currentTimeMillis();

        final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): mSavedStateViewModel.getUserIDLive().getValue();
        final String sDeviceId = mSavedStateViewModel.getDeviceID();
        if (sUserId == null || sUserId.length() == 0) return;   // IMPORTANT CONSIDERATION !!!
        int currentState = mSavedStateViewModel.getState();
        // Splash callbacks
        if (intent.hasExtra(SplashActivity.ARG_ACTION)){
            String prevIntent = intent.getStringExtra(SplashActivity.ARG_ACTION);
            if (prevIntent.equals(ATRACKIT_ATRACKIT_CLASS) || prevIntent.equals(INTENT_PERMISSION_POLICY)){
                if (!appPrefs.getAppSetupCompleted())
                    startSplashActivityForResult(INTENT_SETUP);
                else
                if (mGoogleAccount == null && !authInProgress) signInSilent();
            }
            if (prevIntent.equals(INTENT_PERMISSION_LOCATION)) {
                mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                        != PackageManager.PERMISSION_GRANTED) {
                    startSplashActivityForResult(Constants.INTENT_PERMISSION_SENSOR);
                } else
                    startSplashActivityForResult(INTENT_PERMISSION_AGE);
            }
            if (prevIntent.equals(INTENT_PERMISSION_SENSOR)){
                //  if (appPrefs.getUseLocation() || appPrefs.getUseSensors()) startSensorWorkerAlarm(-1);
                if (!userPrefs.getAskAge())
                    startSplashActivityForResult(INTENT_PERMISSION_AGE);
            }
            if (prevIntent.equals(INTENT_PERMISSION_HEIGHT)){
                startSplashActivityForResult(INTENT_PERMISSION_AGE);
            }
            if (prevIntent.equals(INTENT_PERMISSION_AGE)){
                if (userPrefs != null) {
                    if (!userPrefs.getAskAge()) userPrefs.setAskAge(true);
                    String sSetting = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                    if (sSetting.length() == 0){
                        startSplashActivityForResult(Constants.INTENT_PERMISSION_HEIGHT);
                    }else{
                        if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED)) {
                            startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                        }else
                        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) startWearPendingSyncAlarm();
                    }

                }
            }
            if (prevIntent.equals(INTENT_PERMISSION_DEVICE)){
                String sDevice = appPrefs.getLastNodeName();
                if (sDevice.length() > 0) broadcastToast(sDevice);
            }
            return;
        }
        if (ACTION_ADD_ACTIVITY.equals(intentAction)) {
            try {
                if (!hasOAuthPermission(1)){
                    requestOAuthPermission(1);
                    return;
                }
                createWorkout(sUserId, sDeviceId);
                // Invoked via the manifest shortcut.
                //TODO investigate this parameter
                //long activityType = ((getIntent().getExtras().containsKey(ARG_ACTIVITY_ID)) ? getIntent().getLongExtra(ARG_ACTIVITY_ID, WORKOUT_TYPE_ATRACKIT) : WORKOUT_TYPE_ATRACKIT);
                long activityType = ((getIntent().getExtras().containsKey(ARG_ACTIVITY_ID)) ? getIntent().getLongExtra(ARG_ACTIVITY_ID, 0) : 0);
                if (activityType == 0){
                    CustomActivityListDialog activityListDialog = CustomActivityListDialog.getInstance();
                    activityListDialog.setFragmentInterfaceCallback(MainActivity.this);
                    activityListDialog.show(getSupportFragmentManager(), activityListDialog.TAG);
                    return;
                }
                if (activityType == WORKOUT_TYPE_STRENGTH){
                    String sLabel = getString(R.string.default_loadtype) + Integer.toString(SELECTION_ACTIVITY_GYM);
                    String defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    if (defaultValue.length() > 0){
                        mWorkout.activityID = Long.parseLong(defaultValue);
                    }else
                        mWorkout.activityID = (long)activityType;
                }else
                    mWorkout.activityID = (long)activityType;

                mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(activityType);
                createWorkoutSet();
                mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(activityType));
                mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(activityType));
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName =  mWorkout.activityName;
                mWorkoutSet.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
                createWorkoutMeta();
                mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                if (activityType != WORKOUT_TYPE_ATRACKIT) {
                    startRoutineActivityForResult();
                }else
                    sessionStart();

            }catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return;
        }
        if (Constants.INTENT_EXERCISE_PENDING.equals(intentAction)){
            Exercise exercise = new Exercise();
            if (intent.hasExtra(KEY_FIT_WORKOUTID))
                exercise._id = intent.getLongExtra(KEY_FIT_WORKOUTID,0L);
            if (intent.hasExtra(Exercise.class.getSimpleName())){
                exercise = intent.getParcelableExtra(Exercise.class.getSimpleName());
            }else{
                if (intent.hasExtra(KEY_FIT_NAME))
                    exercise.name = intent.getStringExtra(KEY_FIT_NAME);
            }
            ExerciseActivity.launch(MainActivity.this,exercise._id,exercise,1, sUserId, sDeviceId, mMessagesViewModel.hasPhone());
            return;
        }
        if (intentAction.equals(Constants.INTENT_PERMISSION_LOCATION) || intentAction.equals(Constants.INTENT_PERMISSION_SENSOR)){
            mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
            //if (appPrefs.getUseLocation() || appPrefs.getUseSensors()) startSensorWorkerAlarm(-1);
            if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
                startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
            return;
        }
        if (intentAction.equals(INTENT_SETUP)){
            // check if permissions are setup install could still be needed.
            Context context = getApplicationContext();
            if (!checkApplicationSetup()){
                appPrefs.setAppSetupCompleted(false);
                createNotificationChannels(); // setup notification manager and channels if needed
                Bundle bundle = new Bundle();
                bundle.putString(INTENT_SETUP, "false");
                sendNotification(Constants.INTENT_SETUP, bundle);
                String sAction = INTENT_SETUP;
                if ((userPrefs != null) && ((mGoogleAccount != null)&& (mGoogleAccount.getId() != null))) {
                    boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                    if (!bSetup)
                        sAction = INTENT_PERMISSION_DEVICE;
                }else
                    sAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                mWorkout.start = -1; mWorkout.end = -1; // setup mode !
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                Intent mySplashIntent = new Intent(context, SplashActivity.class);
                mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mySplashIntent.putExtra(SplashActivity.ARG_ACTION, sAction);
                if (mGoogleAccount != null) mySplashIntent.putExtra(KEY_FIT_USER,mGoogleAccount.getId());
                startActivity(mySplashIntent);
                finish();
                return;
            }else{
                cancelNotification(INTENT_SETUP);
                if (!appPrefs.getAppSetupCompleted()) {
                    appPrefs.setAppSetupCompleted(true);
                    Boolean bAvail = Utilities.hasMicrophone(context);
                    appPrefs.setMicrophoneAvail(bAvail);
                    if (bAvail)
                        broadcastToast("microphone is available");
                    bAvail = Utilities.hasSpeaker(context);
                    appPrefs.setSpeakerAvail(bAvail);
                    if (bAvail)
                        broadcastToast("speaker is available");

                    createWorkout(sUserId, sDeviceId);
                    mWorkout._id = 1;
                    mWorkout.start = 0;
                    mWorkout.end = 0;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    sessionSetCurrentState(WORKOUT_INVALID);
                    broadcastToast(getString(R.string.label_setup_complete));
                }
                if (((mGoogleAccount == null) || (mGoogleAccount.getId() == null) || (mGoogleAccount.isExpired())) && (!intent.hasExtra(SplashActivity.ARG_ACTION))) {
                    if (appPrefs.getLastUserLogIn() > 0 && ((System.currentTimeMillis() - appPrefs.getLastUserLogIn()) < TimeUnit.HOURS.toMillis(24)))
                        signInSilent();
                    else {
                        sessionSetCurrentState(WORKOUT_INVALID);
                        signIn();
                        return;
                    }
                }
            }
        }
        if (intentAction.equals(Constants.INTENT_TOTALS_REFRESH)){
            mMessagesViewModel.setWorkInProgress(false);
            mMessagesViewModel.setWorkType(0);
            int iType = intent.getIntExtra(KEY_COMM_TYPE, 2);
            boolean bUpdated = intent.getBooleanExtra(KEY_INDEX_METRIC, false);
            boolean bNew = intent.getBooleanExtra(KEY_INDEX_FILTER, false);
            UserDailyTotals userDailyTotals = (intent.hasExtra(UserDailyTotals.class.getSimpleName()) ? intent.getParcelableExtra(UserDailyTotals.class.getSimpleName()) : null);
            SensorDailyTotals sensorDailyTotals = (intent.hasExtra(SensorDailyTotals.class.getSimpleName()) ? intent.getParcelableExtra(SensorDailyTotals.class.getSimpleName()) : null);
            if (userDailyTotals != null) {
                //     UserDailyTotals currentUDT = mSessionViewModel.getTopUserDailyTotal(userDailyTotals.userID);
                Log.i(LOG_TAG, "INTENT_TOTALS_REFRESH UDT " + iType + " updated " + bUpdated + " new " + bNew + " " + userDailyTotals.toString());
                // if (iType >= 1) {
                if (userDailyTotals.lastAltitude >= 0D)
                    mMessagesViewModel.addAltitudeMsg(userDailyTotals.lastAltitude);
                if (userDailyTotals.lastSpeed >= 0F)
                    mMessagesViewModel.addSpeedMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.lastSpeed));
                else {
                    if (userDailyTotals.avgSpeed >= 0F) {
                        mMessagesViewModel.addSpeedMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.avgSpeed));
                    }
                }
                String sNA = getString(R.string.label_not_available0);
                String sCurrentLoc = mSavedStateViewModel.getLocationAddress();
                if ((userDailyTotals.lastLocation.length() > 0) && ((sCurrentLoc.length() == 0) ||
                        (!sCurrentLoc.equals(userDailyTotals.lastLocation) && !sNA.equals(userDailyTotals.lastLocation)))) {
                    mSavedStateViewModel.addLocationMsg(userDailyTotals.lastLocation);
                }
                if (userDailyTotals.distanceTravelled >= 0F) {
                    mMessagesViewModel.addDistanceMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.distanceTravelled));
                }
                if (userDailyTotals.caloriesExpended >= 0F) {
                    mMessagesViewModel.addCaloriesMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.caloriesExpended));
                }
                if (userDailyTotals.activeMinutes >= 0) {
                    mMessagesViewModel.addMoveMinsMsg(String.format(Locale.getDefault(), SINGLE_INT, userDailyTotals.activeMinutes));
                }
                // no heart sensor - add it here
                // if (userDailyTotals.avgBPM > 0) {
                //     mMessagesViewModel.addBpmMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.avgBPM));
                //  }
//                String sStep = mMessagesViewModel.getStepsMsg().getValue();
//                if (sStep != null) {
//                    int iNowStep = Integer.parseInt(sStep);
//                    if (iNowStep < userDailyTotals.stepCount) {
//                        Log.i(LOG_TAG, "existing adding to google steps " + userDailyTotals.stepCount);
//                        mMessagesViewModel.addStepsMsg(String.format(Locale.getDefault(), SINGLE_INT, userDailyTotals.stepCount));
//                    } else {
//                        Log.i(LOG_TAG, "existing NOT adding to google steps " + iNowStep + " new " + userDailyTotals.stepCount);
//                    }
//                } else if (userDailyTotals.stepCount > 0) {
//                    Log.i(LOG_TAG, "adding to google steps " + userDailyTotals.stepCount);
//                    mMessagesViewModel.addStepsMsg(String.format(Locale.getDefault(), SINGLE_INT, userDailyTotals.stepCount));
//                } else
//                    Log.i(LOG_TAG, "NOT adding to google steps 0 " + userDailyTotals.stepCount);

                if (userDailyTotals.heartIntensity >= 0F) {
                    mMessagesViewModel.addHeartPtsMsg(String.format(Locale.getDefault(), SINGLE_INT, Math.round(userDailyTotals.heartIntensity)));
                }
            }
            if (sensorDailyTotals != null && (bUpdated || bNew)){
                //   SensorDailyTotals currentSDT = mSessionViewModel.getTopSensorDailyTotal(sensorDailyTotals.userID);
                Log.i(LOG_TAG, "INTENT_TOTALS_REFRESH SDT " + iType + " " + " updated " + bUpdated + " new " + bNew + " " + sensorDailyTotals.toString());
                String sTemp;
                if (sensorDailyTotals.pressure > 0){
                    sTemp = String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, sensorDailyTotals.pressure);
                    mMessagesViewModel.addPressureMsg(sTemp);
                }
                if (sensorDailyTotals.temperature > 0){
                    sTemp = String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, sensorDailyTotals.temperature);
                    mMessagesViewModel.addTemperatureMsg(sTemp);
                }
                if (sensorDailyTotals.humidity > 0){
                    sTemp = String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, sensorDailyTotals.humidity);
                    mMessagesViewModel.addHumidityMsg(sTemp);
                }
                if (sensorDailyTotals.deviceStep > 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, sensorDailyTotals.deviceStep);
                    mMessagesViewModel.addDeviceStepsMsg(sTemp);
                }
                if (sensorDailyTotals.deviceBPM > 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, Math.round(sensorDailyTotals.deviceBPM));
                    mMessagesViewModel.addDeviceBpmMsg(sTemp);
                }

                if (sensorDailyTotals.device2Step >= 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, sensorDailyTotals.device2Step);
                    mMessagesViewModel.addDevice2StepsMsg(sTemp);
                }
                if (sensorDailyTotals.device2BPM >= 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, Math.round(sensorDailyTotals.device2BPM));
                    mMessagesViewModel.addDevice2BpmMsg(sTemp);
                }
                if (sensorDailyTotals.humidity2 >= 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, Math.round(sensorDailyTotals.humidity2));
                    mMessagesViewModel.addHumidity2Msg(sTemp);
                }
                if (sensorDailyTotals.temperature2 >= 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, Math.round(sensorDailyTotals.temperature2));
                    mMessagesViewModel.addTemperature2Msg(sTemp);
                }
                if (sensorDailyTotals.pressure2 >= 0){
                    sTemp = String.format(Locale.getDefault(), SINGLE_INT, Math.round(sensorDailyTotals.pressure2));
                    mMessagesViewModel.addTemperature2Msg(sTemp);
                }
            }
            else
                sensorDailyTotals = mSessionViewModel.getTopSensorDailyTotal(sUserId);
            if (userDailyTotals == null) userDailyTotals = mSessionViewModel.getTopUserDailyTotal(sUserId);

            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                long lastSignIn = userPrefs.getLastUserSignIn();
                int state = mSavedStateViewModel.getState();
                if (sensorDailyTotals == null) sensorDailyTotals = mSessionViewModel.getTopSensorDailyTotal(sUserId);
                if (userDailyTotals == null) userDailyTotals = mSessionViewModel.getTopUserDailyTotal(sUserId);
                if (wearNodeId == null || wearNodeId.length() == 0) wearNodeId = appPrefs.getLastNodeID();
                if (wearNodeId.length() != 0)
                    new TaskSendSensoryInfo(MainActivity.this, wearNodeId, sUserId, sDeviceId, lastSignIn,state,sensorDailyTotals,userDailyTotals).run();
            }
            return;
        }
        if (!intent.hasExtra(Constants.KEY_FIT_WORKOUTID) && !intent.hasExtra(Workout.class.getSimpleName())) {
            if (intentAction.equals(Constants.INTENT_NETWORK_CHECK)) {
                doNetworkCheck();
                return;
            }
            if (intentAction.equals(INTENT_BIND_DEVICE)){
                //sDeviceId = intent.getStringExtra(KEY_FIT_DEVICE_ID);
                if (intent.getStringExtra(KEY_FIT_USER).equals(sUserId)){
                    int sensorType = intent.getIntExtra(KEY_FIT_TYPE, 1);
                    long period = intent.getLongExtra(KEY_FIT_TIME, 120);
                    long delay = intent.getLongExtra(KEY_FIT_ACTION, 0);
                    Log.e(LOG_TAG, "Adding SensorControl " + sensorType + " " + delay + " " + period);
                    if (BoundSensorManager.getCount(sensorType) != 0)
                        BoundSensorManager.doReset(sensorType);
                    bindSensorListener(sensorType, Math.toIntExact(delay));
                }
                return;
            }
            if (intentAction.equals(INTENT_PHONE_SYNC)) {
                if (!intent.hasExtra(KEY_FIT_USER) || !intent.getStringExtra(KEY_FIT_USER).equals(sUserId)) return;
                String sDevice = (intent.hasExtra(KEY_FIT_DEVICE_ID) ? intent.getStringExtra(KEY_FIT_DEVICE_ID) : ATRACKIT_EMPTY);
                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && sDevice.length() > 0 && sUserId.length() > 0)
                    if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && (currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED))
                        new TaskSyncWearNode(sUserId,sDevice).run();
                    else {
                        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                            dataMap.putInt(KEY_FIT_TYPE, (int)1); // new request indicator
                            dataMap.putString(KEY_FIT_USER, sUserId);
                            dataMap.putString(KEY_FIT_DEVICE_ID, sDevice);
                            sendCapabilityMessage(WEAR_CAPABILITY_NAME,Constants.MESSAGE_PATH_WEAR_SERVICE,  dataMap);
                        }
                    }
                return;
            }
            if (intentAction.equals(INTENT_SUMMARY_DAILY)) {
                if (!intent.hasExtra(KEY_FIT_USER) || !intent.getStringExtra(KEY_FIT_USER).equals(sUserId)) return;
                doDailySummaryJob();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showGoalsDialog();
                    }
                }, 1000);
                return;
            }
            if (intentAction.equals(INTENT_QUIT_APP)) {
                quitApp();
                return;
            }
            if (intentAction.equals(INTENT_ACTIVE_LOGIN)|| intentAction.equals(Intent.ACTION_MAIN)) {
                if (intent.hasExtra(KEY_FIT_USER) && !intent.getStringExtra(KEY_FIT_USER).equals(sUserId)) return;
                if (!appPrefs.getUseSensors()) doSnackbar(getString(R.string.sensors_permission_not_used), Snackbar.LENGTH_SHORT);
                if (!appPrefs.getUseLocation()) broadcastToast(getString(R.string.location_permission_not_used));
                Intent newIntent = new Intent(INTENT_CLOUD_SYNC);
                newIntent.putExtra(KEY_FIT_USER, sUserId);
                if (intent.hasExtra(KEY_FIT_DEVICE_ID))
                    newIntent.putExtra(KEY_FIT_DEVICE_ID, intent.getStringExtra(KEY_FIT_DEVICE_ID));
                else
                    newIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
                long lastLogin = appPrefs.getLastUserLogIn();
                newIntent.putExtra(KEY_FIT_VALUE, lastLogin);
                newIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                Log.e(LOG_TAG, "ACTIVE_LOGIN about to startupJob");
                new TaskStartupJobs(newIntent).run();
                return;
            }
            if (intentAction.equals(INTENT_HOME_REFRESH) || intent.hasExtra(KEY_FIT_ACTION)) {
                mMessagesViewModel.setWorkInProgress(false);
                mMessagesViewModel.setWorkType(0);
                Log.e(LOG_TAG, "received INTENT_HOME_REFRESH");
                if (intent.hasExtra(KEY_FIT_ACTION)){
                    String sAction = intent.getStringExtra(KEY_FIT_ACTION);
                    if (sAction.equals(Constants.LABEL_DEVICE_USE))
                        startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                    // doGetUseWearApp();
                    if (sAction.equals(Constants.KEY_CHANGE_STATE)){
                        int state = intent.hasExtra(KEY_FIT_VALUE) ? intent.getIntExtra(KEY_FIT_VALUE ,0) : WORKOUT_INVALID;
                        Log.e(LOG_TAG, "about to change state: " + state);
                        if (state == WORKOUT_INVALID) {
                            createWorkout(sUserId, sDeviceId);
                            mWorkout._id = 2;
                            createWorkoutSet();
                            mSavedStateViewModel.setActiveWorkout(mWorkout);  // trigger state change
                            mSavedStateViewModel.setActiveWorkoutSet(null);
                            mSavedStateViewModel.setActiveWorkoutMeta(null);
                            mSavedStateViewModel.setToDoSets(new ArrayList<>());
                        }
                        sessionSetCurrentState(state);
                    }
                }else{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    for (Fragment frag : fragmentManager.getFragments()){
                        if (frag instanceof HomeFragment && (frag.isVisible())){
                            Log.e(LOG_TAG, "INTENT_HOME_REFRESH refreshing Image" );
                            ((HomeFragment) frag).refreshPersonalImage();
                        }
                    }
                }
            }
            if (intentAction.equals(Constants.INTENT_CLOUD_SKU))
                doSkuLookupJob(intent.getStringExtra(KEY_FIT_USER),intent.getStringExtra(KEY_FIT_DEVICE_ID));


            if (intentAction.equals(INTENT_CLOUD_SYNC)) doGoogleFitSyncJob(intent);

            if (intentAction.equals(Constants.INTENT_CLOUD_META))
                broadcastToast("Cloud sync finished");

            if (intentAction.equals(INTENT_LOCATION_UPDATE)){
                int iSet = intent.getIntExtra(Constants.INTENT_EXTRA_RESULT,0);
                if (iSet == 1) {
                    Location loc = intent.getParcelableExtra(INTENT_LOCATION_UPDATE);
                    if (loc != null) onNewLocation(loc);
                }
            }
            return;  // every one out without session data extras
        }

        /**
         *
         * handle sessions - load, start & stop
         *
         * **/
        boolean isExternal = (intent.getIntExtra(Constants.KEY_FIT_TYPE, 0) == 1);
        if (intent.hasExtra(KEY_FIT_USER)){
            String intentUserId = intent.getStringExtra(KEY_FIT_USER);
            if (!TextUtils.equals(sUserId,intentUserId)){
                // do not handle intents for wrong user
                Log.e(LOG_TAG, "incoming intent user not equal " + intentUserId);
                return;
            }
        }
        if ((mWorkout == null) && mSavedStateViewModel.isSessionSetup()) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if ((mWorkoutSet == null) && mSavedStateViewModel.isSessionSetup()) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        long lWorkoutID = 0;
        long lWorkoutSetID = 0;
        Workout arrivingWorkout = null;
        WorkoutSet arrivingWorkoutSet = null;
        WorkoutMeta arrivingWorkoutMeta = null;
        List<WorkoutSet> sets = new ArrayList<>();
        if (intent.hasExtra(Workout.class.getSimpleName())) {
            arrivingWorkout = (Workout) intent.getParcelableExtra(Workout.class.getSimpleName());
            lWorkoutID = arrivingWorkout._id;
            if (intent.hasExtra(WorkoutSet.class.getSimpleName())) {
                arrivingWorkoutSet = (WorkoutSet) intent.getParcelableExtra(WorkoutSet.class.getSimpleName());
                if (arrivingWorkoutSet != null) lWorkoutSetID = arrivingWorkoutSet._id;
            }
            if (intent.hasExtra(WorkoutMeta.class.getSimpleName())){
                arrivingWorkoutMeta = (WorkoutMeta) intent.getParcelableExtra(WorkoutMeta.class.getSimpleName());
            }
        }else{
            if (intent.hasExtra(Constants.KEY_FIT_WORKOUTID)) {
                lWorkoutID = intent.getLongExtra(Constants.KEY_FIT_WORKOUTID, 0);
                arrivingWorkout = mSessionViewModel.getWorkoutById(lWorkoutID, sUserId,sDeviceId);
                if ((arrivingWorkout != null) && (intent.hasExtra(Constants.KEY_FIT_WORKOUT_SETID))) {
                    lWorkoutSetID = intent.getLongExtra(Constants.KEY_FIT_WORKOUT_SETID, 0);
                    if (lWorkoutSetID > 0) arrivingWorkoutSet = mSessionViewModel.getWorkoutSetById(lWorkoutSetID, sUserId, sDeviceId);
                    if (arrivingWorkoutSet != null) lWorkoutSetID = arrivingWorkoutSet._id;
                    List<WorkoutMeta> metaList = mSessionViewModel.getWorkoutMetaByWorkoutID(lWorkoutID,arrivingWorkout.userID,arrivingWorkout.deviceID);
                    if (metaList != null && metaList.size() > 0) arrivingWorkoutMeta = metaList.get(0);
                }
            }
        }

        int arrivingState = Utilities.currentWorkoutState(arrivingWorkout);
        if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)){
            if (((mWorkout == null) || (mWorkout._id != lWorkoutID)) && (arrivingWorkout != null)) {
                mWorkout = arrivingWorkout;
                currentState = arrivingState;
            }
            if (((mWorkoutSet == null) || (mWorkoutSet._id != lWorkoutSetID)) && (arrivingWorkoutSet != null)) mWorkoutSet = arrivingWorkoutSet;
        }else{
            if (mWorkout._id == lWorkoutID)
                if (((mWorkoutSet == null) || (mWorkoutSet._id != lWorkoutSetID)) && (arrivingWorkoutSet != null)) mWorkoutSet = arrivingWorkoutSet;
        }
        if ((mWorkoutSet != null) && (mSavedStateViewModel != null)) {
            String sTemp = "set count " + mWorkoutSet.setCount + " index " + mSavedStateViewModel.getSetIndex()
                    + " state " + currentState + "arriving " + arrivingState;
            Log.i(LOG_TAG, "handleIntent " + intentAction + sTemp);
        }else {
            Log.i(LOG_TAG, "handleIntent null set " + intentAction);
            if (lWorkoutID > 0)
                sets = mSessionViewModel.getWorkoutSetByWorkoutID(lWorkoutID, sUserId, sDeviceId);

            if (sets != null && (sets.size() > 0)) mWorkoutSet = sets.get(0); else return;
        }

        if ((intentAction.equals(INTENT_TEMPLATE_START)) && (arrivingWorkout != null)){
            sessionNewFromTemplate(arrivingWorkout);
            sessionStart();
            return;
        }
        if (intentAction.equals(INTENT_WORKOUT_REPORT)) {
            sessionReport(intent);
            return;
        }
        if (((mWorkout != null) && (arrivingWorkout != null)) && (arrivingWorkout._id != mWorkout._id)) {
            // workout related intents
            if (intentAction.equals(Intent.ACTION_VIEW) || intentAction.contains("com.a_track_it.com.workout.workout")) {
                mWorkout = arrivingWorkout;
                if (arrivingWorkoutSet != null) mWorkoutSet = arrivingWorkoutSet;
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                int state = mSavedStateViewModel.getState();
                //if (arrivingState != state && state != WORKOUT_LIVE && state != WORKOUT_PAUSED) mSavedStateViewModel.setCurrentState(arrivingState);
                if (sets != null) {
                    if (Utilities.isGymWorkout(mWorkout.activityID)) {
                        if ((arrivingState == WORKOUT_LIVE) || (arrivingState == WORKOUT_PAUSED)) {
                            for (int i = 0; i < sets.size(); i++) {
                                WorkoutSet set = sets.get(i);
                                if ((set.start > 0) && (set.end == 0)) {
                                    mWorkoutSet = set;
                                    mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                                    break;
                                } else {
                                    // nothing started leave now
                                    if ((set.start == 0) && (set.end == 0) && (i == 0)) {
                                        mWorkoutSet = set;
                                        mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                                        break;
                                    } else {
                                        if (i == (sets.size() - 1)) { // all completed
                                            mWorkoutSet = set;
                                            mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (mWorkoutSet != null) mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    } else {
                        // one set only
                        mWorkoutSet = sets.get(0);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                    }
                    Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(refreshIntent);
                }
                return; // all done -
            }
        } // end of if different

        if (intentAction.equals(Constants.INTENT_WORKOUT_EDIT)) {
            boolean bNewWorkout = false; boolean bNewSet = false;
            if (arrivingWorkout != null) {
                bNewWorkout = (mWorkout == null || mWorkout._id  != arrivingWorkout._id);
                if (bNewWorkout) mWorkout = arrivingWorkout;
            }
            else {
                mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            }
            if (mWorkout == null) return;
            if (arrivingWorkoutSet != null) {
                bNewSet = (mWorkoutSet == null || mWorkoutSet._id != arrivingWorkoutSet._id);
                if (bNewSet) mWorkoutSet = arrivingWorkoutSet;
            }else
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (arrivingWorkoutMeta != null) mWorkoutMeta = arrivingWorkoutMeta;
            else mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta == null) createWorkoutMeta();
            if (bNewWorkout) {
                Boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
                mSavedStateViewModel.setSetIsGym(bGym);
                mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                int activityIcon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                mSavedStateViewModel.setIconID(activityIcon);
                int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                mSavedStateViewModel.setColorID(activityColor);
            }
            if (mSavedStateViewModel.getSetIndex() != mWorkoutSet.setCount)  mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if (mSavedStateViewModel.getToDoSetsSize() == 0 || (bNewWorkout || bNewSet)){
                List<WorkoutSet> setList = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                if (setList != null && setList.size() > 0) mSavedStateViewModel.setToDoSets(setList);
            }
            if (!Utilities.isShooting(mWorkout.activityID) && mWorkout.scoreTotal != FLAG_NON_TRACKING){
                if (mWorkout.start > 0)
                    mWorkoutSet.scoreTotal = FLAG_PENDING; // build stage
                else
                    mWorkoutSet.scoreTotal = FLAG_BUILDING; // build stage
            }
            startRoutineActivityForResult();
        }
        if (intentAction.equals(Constants.INTENT_ACTIVE_START)) {
            int doNext = (intent.hasExtra(KEY_FIT_VALUE)) ? intent.getIntExtra(KEY_FIT_VALUE,1) : 1;
            if (!hasOAuthPermission(1)){
                requestOAuthPermission(1);
                return;
            }else
            if (!isExternal) {
                if ((mWorkout.start == 0L))
                    sessionStart();
                else
                if (doNext == 1)
                    sessionStartNextSet();
                else
                    sessionResume();
            }else
                showAlertDialogConfirm(ACTION_STARTING);
        }
        if (intentAction.equals(Constants.INTENT_ACTIVE_STOP)) {
            if (!isExternal) {
                sessionCompleteActiveSet(timeMs);
                sessionStop();
                if (intent.hasExtra(KEY_FIT_ACTION)) {
                    String sAction = intent.getStringExtra(KEY_FIT_ACTION);
                    if (sAction.equals(Constants.KEY_FIT_ACTION_QUIT))
                        quitApp();
                }
            }else
                showAlertDialogConfirm(Constants.ACTION_STOPPING);

        }
        if (intentAction.equals(INTENT_ACTIVESET_START)){
            if (!isExternal) {
                if (intent.hasExtra(KEY_FIT_ACTION)){
                    sessionRepeatSet();
                    sessionResume();
                }else
                if (intent.hasExtra(KEY_FIT_VALUE)){
                    if (intent.getIntExtra(KEY_FIT_VALUE, 1) == 0)
                        sessionResume();
                    else
                        sessionStartNextSet();
                }else
                    sessionStartNextSet();
            }else
                showAlertDialogConfirm(ACTION_START_SET);
        }
        if (intentAction.equals(INTENT_ACTIVE_PAUSE)){
            if (isExternal) {
                showAlertDialogConfirm(ACTION_PAUSING);
            }else {
                sessionPause();
            }
        }
        if (intentAction.equals(INTENT_ACTIVE_RESUMED)){
            if (isExternal)
                if ((mWorkout.start == 0) || (mWorkout.start > 0 && mWorkout.end > 0) || (mWorkout._id == 1) || (mWorkout._id == 2))
                    showAlertDialogConfirm(ACTION_STARTING);
                else
                    showAlertDialogConfirm(ACTION_RESUMING);
            else {
                if ((mWorkout.start == 0) || (mWorkout.start > 0 && mWorkout.end > 0) || (mWorkout._id == 1) || (mWorkout._id == 2)){
                    sessionStart();
                    return;
                }else {
                    sessionResume();
                }
            }
        }
        if (intentAction.equals(INTENT_ACTIVESET_STOP)){
            if (!isExternal) {
                sessionCompleteActiveSet(timeMs);
                long pStart = SystemClock.elapsedRealtime();
                // go to the gym confirm fragment
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    if (mWorkout.scoreTotal == Constants.FLAG_NON_TRACKING || mWorkoutSet.scoreTotal == FLAG_NON_TRACKING){
                        sessionStop();
                    }else {
                        mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                        sessionSetCurrentState(WORKOUT_PAUSED);
                        GymConfirmFragment gymConfirmFragment = GymConfirmFragment.newInstance(1);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                        transaction.add(R.id.top_container, gymConfirmFragment);
                        transaction.addToBackStack("dialog_gym_confirm");
                        transaction.commit();
                    }
                }else
                if (Utilities.isShooting(mWorkout.activityID)) {
                    int icon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    int color = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    int rest = 0; //userPrefs.getArcheryRestDuration();
                    mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                    sessionSetCurrentState(WORKOUT_PAUSED);
                    ShootingConfirmFragment shootingConfirmFragment = ShootingConfirmFragment.newInstance(icon,color, mWorkoutSet, mWorkout,rest,mWorkoutMeta);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                    transaction.add(R.id.top_container, shootingConfirmFragment);
                    transaction.addToBackStack("dialog_shooting_confirm");
                    transaction.commit();
                }
                else
                    sessionStop();
            }else{
                showAlertDialogConfirm(ACTION_END_SET);
                return;
            }

        }
        if (intentAction.equals(Constants.INTENT_INPROGRESS_RESUME)){
            if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)) {
                DateTuple dateTuple = mSessionViewModel.getInProgressCountDates(sUserId,sDeviceId);
                if ((dateTuple != null) && (dateTuple.sync_count > 0)) {
                    broadcastToast("In progress " + dateTuple.sync_count);
                    startCustomList(Constants.SELECTION_WORKOUT_INPROGRESS, "", sUserId);
                }
            }
        }
        if (intentAction.equals(INTENT_WORKOUT_DELETE)){
            mSessionViewModel.deleteWorkout(mWorkout);
            mWorkout = null;
            mWorkoutSet = null;
            Intent newIntent = new Intent(Constants.INTENT_INPROGRESS_RESUME);
            newIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
            mMessagesViewModel.addLiveIntent(newIntent);
            return;
        }
        if (intentAction.equals(Constants.INTENT_GOAL_TRIGGER)){
            String sKey = KEY_FIT_TYPE;
            Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
            vibIntent.putExtra(KEY_FIT_TYPE, 3);
            mMessagesViewModel.addLiveIntent(vibIntent);
            if (intent.hasExtra(sKey) && userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) {
                Bundle resultData = new Bundle();
                resultData.putLong(KEY_FIT_WORKOUTID,intent.getLongExtra(KEY_FIT_WORKOUTID, 0));
                resultData.putInt(KEY_FIT_TYPE, intent.getIntExtra(KEY_FIT_TYPE, 0));
                resultData.putLong(KEY_FIT_ACTION, intent.getLongExtra(KEY_FIT_ACTION,0));
                resultData.putString(KEY_FIT_USER, sUserId);
                resultData.putString(KEY_FIT_DEVICE_ID, sDeviceId);
                sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
            }
            return;
        }
        if (intentAction.equals(Constants.INTENT_CALL_TRIGGER)){
            Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
            vibIntent.putExtra(KEY_FIT_TYPE, 3);
            mMessagesViewModel.addLiveIntent(vibIntent);
            playMusic();
            if (userPrefs.getRestAutoStart()){
                Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_LIVE);
                refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                mMessagesViewModel.addLiveIntent(refreshIntent);
            }
        }
    }
}
