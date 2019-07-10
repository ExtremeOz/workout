package com.a_track_it.fitdata.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateVMFactory;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.SwipeDismissFrameLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.BuildConfig;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.data_model.Bodypart;
import com.a_track_it.fitdata.data_model.Exercise;
import com.a_track_it.fitdata.data_model.FitnessActivity;
import com.a_track_it.fitdata.data_model.PeakDetProcessor;
import com.a_track_it.fitdata.data_model.Processor;
import com.a_track_it.fitdata.data_model.SystemOutProcessor;
import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.fragment.CustomListFragment;
import com.a_track_it.fitdata.fragment.CustomScoreDialogFragment;
import com.a_track_it.fitdata.fragment.EndOfSetFragment;
import com.a_track_it.fitdata.fragment.HomePageFragment;
import com.a_track_it.fitdata.fragment.LiveFragment;
import com.a_track_it.fitdata.fragment.SessionEntryFragment;
import com.a_track_it.fitdata.fragment.SessionReportFragment;
import com.a_track_it.fitdata.model.GSONHelper;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.model.SavedStateViewModel;
import com.a_track_it.fitdata.model.SessionViewModel;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.a_track_it.fitdata.service.FITAPIManager;
import com.a_track_it.fitdata.workers.BoundFusedLocationClient;
import com.a_track_it.fitdata.workers.BoundSensorManager;
import com.a_track_it.fitdata.workers.ImageWorker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.KEY_IMAGE_URI;
import static com.a_track_it.fitdata.common.Constants.STATE_END_SET;
import static com.a_track_it.fitdata.common.Constants.STATE_ENTRY;
import static com.a_track_it.fitdata.common.Constants.STATE_HOME;
import static com.a_track_it.fitdata.common.Constants.STATE_LIVE;
import static com.a_track_it.fitdata.common.Constants.STATE_SETTINGS;
import static com.a_track_it.fitdata.service.FITAPIManager.EXTRA_LOCATION;


public class MainActivity extends AppCompatActivity implements
        AmbientModeSupport.AmbientCallbackProvider,
        com.google.android.gms.fitness.request.OnDataPointListener,
        HomePageFragment.OnHomePageFragmentInteractionListener,
        LiveFragment.OnLiveFragmentInteractionListener,
        CustomListFragment.OnCustomListItemSelectedListener,
        CustomScoreDialogFragment.onCustomScoreSelected,
        SessionEntryFragment.OnSessionEntryFragmentListener,
        EndOfSetFragment.OnEndOfSetInteraction, SessionReportFragment.OnSessionReportInteraction,
        FITAPIManager.IFITAPIManager, CustomConfirmDialog.ICustomConfirmDialog{

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final long DELAY_MILLIS = TimeUnit.SECONDS.toMillis(10);
    public static final long COUNT_DOWN_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    public static final String RECEIVER_TAG = "MainActivityReceiver";

    /** Custom 'what' for Message sent to Handler. */
    private static final int MSG_UPDATE_SCREEN = 0;
    private static final int SPEECH_REQUEST_CODE = 5003;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 5004;
    private static final int REQUEST_CHOOSE_ACCOUNT_CODE = 5001;
    private static final int RC_SIGN_IN = 5002;
    private static final int NEAR_LOCATION_SETTINGS_CODE = 5005;
    private static final int REQUEST_RECORDING_PERMISSION_CODE = 5007;
    public static final String EXTRA_PROMPT_PERMISSION_FROM_PHONE =
            "com.example.android.wearable.runtimepermissions.extra.PROMPT_PERMISSION_FROM_PHONE";

    /** Milliseconds between updates based on state. */
    private static final long ACTIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long AMBIENT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);
    /** Action for updating the display in ambient mode, per our custom refresh cycle. */
    private static final String AMBIENT_UPDATE_ACTION =
            "com.a_track_it.fitdata.action.AMBIENT_UPDATE";

    private static final String LOCATION_UPDATE_ACTION =
            "com.a_track_it.fitdata.location.LOCATION_UPDATE";

    /** Number of pixels to offset the content rendered in the display to prevent screen burn-in. */
    private static final int BURN_IN_OFFSET_PX = 10;
    private static final int ACTION_INTIALISING = -1;
    private static final int ACTION_STARTING = 0;
    private static final int ACTION_STOPPING = 1;
    private static final int ACTION_RESUMING = 2;
    private static final int ACTION_CANCELLING = 3;
    private static final int ACTION_EXITING = 4;
    private static final int ACTION_QUICK_STOP = 5;
    private static final int ACTION_STOP_QUIT = 6;
    //private int currentState = ACTION_INTIALISING;



    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in ambient
     * mode.
     */
    private AmbientModeSupport.AmbientController mAmbientController;

    /** If the display is low-bit in ambient mode. i.e. it requires anti-aliased fonts. */
    boolean mIsLowBitAmbient;
    /**
     * If the display requires burn-in protection in ambient mode, rendered pixels need to be
     * intermittently offset to avoid screen burn-in.
     */
    boolean mDoBurnInProtection;
    private FITAPIManager mService;
    private boolean mServiceBound = false;
    private HomePageFragment mHomepageFragment;
    private LiveFragment mLiveFragment;
    private EndOfSetFragment mEndOfSetFragment;
    private CustomConfirmDialog mCustomConfirmDialog;
    private CustomListFragment mCustomListFragment;
    private CustomScoreDialogFragment mCustomScoreDialogFragment;
    private SessionEntryFragment mSessionEntryFragment;
    private WorkManager mWorkManager;
    private boolean mNetworkConnected;
   // private CacheResultReceiver mReceiver;
   // private ResultReceiver mReceiver;
    private SwipeDismissFrameLayout mSwipeDismissFrameLayout;
    private int mActivityIcon;
    private int mActivityColor;
    private Location mLocation;
    private LocationCallback mLocationListener;
    private SensorEventListener mSensorListener = new xSensorListener(1);

    private boolean authInProgress = false;
    private boolean isRecordingAPIRunning = false;
    private boolean muteFeedback = false;

    //
    // parcelable model classes with state retained
    //
    //

    private long mPauseDuration = 0L;
    private long mExpectedRestDuration = 0L;              // the expected rest duration
    private int mCurrentSetIndex = 0;

    private CountDownTimer mCountDownTimer;
    private boolean mIsGymWorkout = false;
    private boolean mIsShooting = false;

    // private boolean mSessionInProgress = false;
    private class DailyCounter{
       DailyCounter(){
            this.FirstCount = 0;
            this.LastCount = 0;
            this.LastUpdated = 0;
        }
        String JSONText(){
            String sRet; String sDay = "";
            if (mCalendar != null){
                mCalendar.setTimeInMillis(LastUpdated);
                sDay = mCalendar.toString();
            }
            sRet = "{" +
                    "FirstCount=" + Long.toString(this.FirstCount) +
                    ", LastUpdated=" + Long.toString(this.LastUpdated) +
                    ", LastCount=" + Long.toString(this.LastCount);
            if (sDay.length() > 0)
                sRet += ", DateUpdated=\"" + sDay + "\"";
            sRet += "}";
            return sRet;
        }
        long FirstCount;
        long LastUpdated;
        long LastCount;
   }
    private DailyCounter mLastStep = new DailyCounter();
    private DailyCounter mLastBPM = new DailyCounter();
    private final Calendar mCalendar = Calendar.getInstance();
    private Workout mWorkout;                            // accumulator for the active session!
    private WorkoutSet mWorkoutSet;                      // passed from Session entry becomes the Active Workout Set
    private ArrayList<WorkoutSet> mToDoSets = new ArrayList<>();
    private ArrayList<WorkoutSet> mCompletedSets = new ArrayList<>();  // list of completed sets
    private ArrayList<Workout> mCompletedWorkouts = new ArrayList<>();

    // sensors detection
    private ArrayList<DataSource> mDataSourceList = new ArrayList<>();
    private ReferencesTools mRefTools;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleAccount;
    private Geocoder mGeoCoder;

    private SessionViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private NavController mNavigationController;

    private int mRepetitionCount;

    private final Processor mProcessor = new PeakDetProcessor(Constants.DELTA);
    private final SystemOutProcessor systemOutProcessor = new SystemOutProcessor();
    private FragmentManager fragmentManager;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            //String sName = event.sensor.getName();
            Float val;
            String sValue;
            Integer iNowDOY; Integer iLastDOY;
            switch (iType){
                case Sensor.TYPE_ACCELEROMETER:
                    boolean isRepetition = mProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);
            /*            systemOutProcessor.processPoint(event.timestamp, event.values[0],
                                event.values[1], event.values[2]);*/
                    if (isRepetition) {
                        vibrate(3);
                        sValue = getString(R.string.label_rep) + getString(R.string.my_space_string) + String.valueOf(++mRepetitionCount);
                        mMessagesViewModel.addOtherMessage(sValue);
                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    val = event.values[0];
                    sValue = Integer.toString(Math.round(val));
                    long timeMs = mLastStep.LastUpdated;
                    if (timeMs > 0){
                        mCalendar.setTimeInMillis(timeMs);
                        iLastDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                    }else
                        iLastDOY = 0;
                    timeMs = System.currentTimeMillis();
                    mCalendar.setTimeInMillis(timeMs);
                    iNowDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                    mLastStep.LastCount = Long.parseLong(sValue);
                    // check to initialise first step
                    if (iLastDOY < iNowDOY) mLastStep.FirstCount = mLastStep.LastCount;
                    mLastStep.LastUpdated = timeMs;  //now time
                    sValue = getString(R.string.label_steps) + getString(R.string.my_space_string) + sValue;
                    mMessagesViewModel.addStepsMsg(sValue);
                    break;

                case Sensor.TYPE_HEART_RATE:
                    val = event.values[0];
                    sValue = Integer.toString(Math.round(val));
                    long timeBPMMs = mLastBPM.LastUpdated;
                    if (timeBPMMs > 0){
                        mCalendar.setTimeInMillis(timeBPMMs);
                        iLastDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                    }else
                        iLastDOY = 0;
                    timeBPMMs = System.currentTimeMillis();
                    mCalendar.setTimeInMillis(timeBPMMs);
                    iNowDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                    mLastBPM.LastCount = Long.parseLong(sValue);
                    // check to initialise first step
                    if (iLastDOY < iNowDOY) mLastBPM.FirstCount = mLastBPM.LastCount;
                    mLastBPM.LastUpdated = timeBPMMs;  //now time
                    sValue = getString(R.string.label_bpm) + getString(R.string.my_space_string) + sValue;
                    mMessagesViewModel.addBpmMsg(sValue);
                    break;

                default:
                    boolean isRep = mProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);
                    if (isRep) {
                        vibrate(2);
                        sValue = String.valueOf(++mRepetitionCount);
                        mMessagesViewModel.addOtherMessage(sValue);
                    }
                    break;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /**
     * Since the handler (used in active mode) can't wake up the processor when the device is in
     * ambient mode and undocked, we use an Alarm to cover ambient mode updates when we need them
     * more frequently than every minute. Remember, if getting updates once a minute in ambient mode
     * is enough, you can do away with the Alarm code and just rely on the onUpdateAmbient()
     * callback.
     */
    private AlarmManager mAmbientUpdateAlarmManager;
    private Vibrator mVibrator;
    private PendingIntent mAmbientUpdatePendingIntent;
    private BroadcastReceiver mAmbientUpdateBroadcastReceiver;
    private BroadcastReceiver mLocationUpdateBroadcastReceiver;
    /**
     * This custom handler is used for updates in "Active" mode. We use a separate static class to
     * help us avoid memory leaks.
     */
    private final Handler mActiveModeUpdateHandler = new ActiveModeUpdateHandler(this);

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FITAPIManager.LocalBinder binder = (FITAPIManager.LocalBinder) service;
            mService = binder.getService();
            mService.addListener(MainActivity.this);
            mServiceBound = true;
            Log.d(TAG, "Service Bound with Listener");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.removeListener(MainActivity.this);
            mService = null;
            mServiceBound = false;
            Log.d(TAG, "Service Disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        boolean SessionInProgress = false;
        String sMsg = "";
        if (!UserPreferences.getAppSetupCompleted(context)){
            Intent myInitialIntent = new Intent(context, InitialActivity.class);
            startActivity(myInitialIntent);
            finish();
            return;
        }


        // these next 3 are only save as parcelable on saveInstance
/*
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_IN_PROGRESS)){
                SessionInProgress = savedInstanceState.getBoolean(Constants.STATE_IN_PROGRESS);
            }
            if (savedInstanceState.containsKey(Constants.LABEL_WORKOUT)){
                mWorkout = savedInstanceState.getParcelable(Constants.LABEL_WORKOUT);
                Log.d(TAG, "mWorkout loaded from savedInstanceState");
            }
            if (savedInstanceState.containsKey(Constants.LABEL_SET)){
                mWorkoutSet = savedInstanceState.getParcelable(Constants.LABEL_SET);
            }
            String sKey = getString(R.string.LastLong);
            if (savedInstanceState.containsKey(sKey)){
                Float fPickup = savedInstanceState.getFloat(sKey, 0F);
                sKey = getString(R.string.LastLat);
                Float fPickup2 = savedInstanceState.getFloat(sKey, 0F);
                if ((fPickup > 0) && (fPickup2 > 0)){
                    mLocation.setLongitude(fPickup);
                    mLocation.setLatitude(fPickup2);
                    Intent intent = new Intent(LOCATION_UPDATE_ACTION);
                    intent.putExtra(EXTRA_LOCATION, mLocation);
                    sendBroadcast(intent);

                }
            }
            sKey = getString(R.string.label_last_step);
            if (savedInstanceState.containsKey(sKey)){
                mLastStep.LastCount = savedInstanceState.getLong(sKey);
                sKey = getString(R.string.label_first_step);
                mLastStep.FirstCount = savedInstanceState.getLong(sKey);
                sKey = getString(R.string.label_last_step_upd);
                mLastStep.LastUpdated = savedInstanceState.getLong(sKey);
            }
            if (savedInstanceState.containsKey(Constants.LABEL_CURRENT_SET)) mCurrentSetIndex = savedInstanceState.getInt(Constants.LABEL_CURRENT_SET);
            if (savedInstanceState.containsKey(Constants.LABEL_FRAG_STATE)){
                int iState = savedInstanceState.getInt(Constants.LABEL_FRAG_STATE);
                if (currentState != iState){
                    Log.w(TAG,"onCreate changed current state " + Integer.toString(currentState) + " to " + Integer.toString(iState));
                    currentState = iState;
                }
            }
        }else{
            Intent intent = getIntent();
            String action = intent.getAction();
            sMsg =  (intent.hasExtra("TAG")) ? intent.getStringExtra("MSG") : getString(R.string.my_empty_string);
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && (action != null) ){
                if (action.equals(Intent.ACTION_VIEW)) Log.d(TAG, "VIEW action startup");
            }
        }
*/


        setContentView(R.layout.activity_main_linear);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            sMsg = (intent.hasExtra("TAG")) ? intent.getStringExtra("MSG") : getString(R.string.my_empty_string);
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && (action != null)) {
                if (action.equals(Intent.ACTION_VIEW)) Log.d(TAG, "VIEW action startup");
            }
        }
        mSwipeDismissFrameLayout = findViewById(R.id.swipe_layout);
        mSwipeDismissFrameLayout.addCallback(new MySwipeDismissCallback());
        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        fragmentManager = navHostFragment.getFragmentManager();

        mAmbientController = AmbientModeSupport.attach(this);
        mAmbientUpdateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(context);
        /*
         * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
         * on an interval which we've define.
         */
        Intent ambientUpdateIntent = new Intent(AMBIENT_UPDATE_ACTION);
        /*
         * Retrieves a PendingIntent that will perform a broadcast. You could also use getActivity()
         * to retrieve a PendingIntent that will start a new activity, but be aware that actually
         * triggers onNewIntent() which causes lifecycle changes (onPause() and onResume()) which
         * might trigger code to be re-executed more often than you want.
         *
         * If you do end up using getActivity(), also make sure you have set activity launchMode to
         * singleInstance in the manifest.
         *
         * Otherwise, it is easy for the AlarmManager launch Intent to open a new activity
         * every time the Alarm is triggered rather than reusing this Activity.
         */
        mAmbientUpdatePendingIntent =
                PendingIntent.getBroadcast(context, 0, ambientUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
         * An anonymous broadcast receiver which will receive ambient update requests and trigger
         * display refresh.
         */
        mAmbientUpdateBroadcastReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        refreshDisplayAndSetNextUpdate();
                    }
                };
        this.mGeoCoder = new Geocoder(context, Locale.getDefault());

        mLocationUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location location = (Location)intent.getParcelableExtra(EXTRA_LOCATION);
                onNewLocation(location);
            }
        };

        long timeMs = System.currentTimeMillis();
        mNetworkConnected = mRefTools.isNetworkConnected();
      //  mReceiver = new CacheResultReceiver(new Handler());
//        mReceiver = new ResultReceiver(new Handler()){
//            @Override
//            protected void onReceiveResult(int resultCode, Bundle resultData) {
//                if (resultCode == 200){
//                    final Workout data = resultData.getParcelable("Workout");
//                    final WorkoutSet set = resultData.getParcelable("WorkoutSet");
//                    final WorkoutSet set2 = resultData.getParcelable("Watts");
//                    if (data != null)
//                        Log.d(TAG, "received result " + data.shortText());
//                    else
//                        Log.d(TAG, "received result is okay");
//                    if (set != null)
//                        Log.d(TAG, "received resultset " + set.shortText());
//                }else
//                    Log.e(TAG, "receiver " + resultCode);
//
//                super.onReceiveResult(resultCode, resultData);
//            }
//        };
        muteFeedback = UserPreferences.getFeedbackMute(context);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mRepetitionCount = 0;
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mMessagesViewModel = ViewModelProviders.of(MainActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.addMessage(getString(R.string.app_name));
        mMessagesViewModel.addOtherMessage(Utilities.getDateString(timeMs));
        mWorkManager = WorkManager.getInstance(this);

        mSessionViewModel = ViewModelProviders.of(MainActivity.this).get(SessionViewModel.class);

        // Obtain the ViewModel, passing in an optional
        // SavedStateVMFactory so that you can use SavedStateHandle
        mSavedStateViewModel = ViewModelProviders.of(this, new SavedStateVMFactory(this))
                .get(SavedStateViewModel.class);


        mNavigationController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavigationController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d(TAG, "onDestChanged " + destination.getLabel());
            }
        });

        int imageId;
        if (SessionInProgress && ((mWorkout.activityID > 0) && (mWorkout._id != 0)) && (mSavedStateViewModel.getCurrentState().getValue() == STATE_LIVE)){
            Log.d(TAG, "Starting activity session continue onCreate");
            mSavedStateViewModel.setCurrentState(STATE_ENTRY);


            mSessionEntryFragment = SessionEntryFragment.newInstance(mWorkout.activityID, mActivityIcon, mActivityColor);
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(R.id.fragment_content, mSessionEntryFragment, SessionEntryFragment.TAG).addToBackStack(SessionEntryFragment.TAG).commit();
        }else {
            if (sMsg.length() > 0){
                mMessagesViewModel.addMessage(sMsg);
                Toast.makeText(getApplicationContext(),sMsg,Toast.LENGTH_LONG).show();
            }
            if (mActivityIcon > 0)
                imageId = mActivityIcon;
            else
                imageId = getResources().getIdentifier("ic_launcher","mipmap", getPackageName());

            mSessionViewModel.getCompletedSets().observe(this, new Observer<ArrayList<WorkoutSet>>() {
                @Override
                public void onChanged(@Nullable ArrayList<WorkoutSet> workoutSets) {
                    if (workoutSets == null) return;
                    Log.i(TAG, "completed sets has changed! " + Integer.toString(workoutSets.size()));
                    if (mCompletedSets.size() == 0)
                        mCompletedSets.addAll(workoutSets);
                    else{
                        for (WorkoutSet set : workoutSets){
                            if (!mCompletedSets.contains(set)) mCompletedSets.add(set);
                        }
                    }
                }
            });
            mSessionViewModel.getCompletedWorkouts().observe(this, new Observer<ArrayList<Workout>>() {
                @Override
                public void onChanged(@Nullable ArrayList<Workout> workouts) {
                    if (workouts == null) return;
                    Log.i(TAG, "completed workouts has changed! " + Integer.toString(workouts.size()));
                    if (mCompletedWorkouts.size() == 0)
                        mCompletedWorkouts.addAll(workouts);
                    else{
                        for (Workout workout : workouts){
                            if (!mCompletedWorkouts.contains(workout)) mCompletedWorkouts.add(workout);
                        }
                    }
                }
            });
            mSessionViewModel.getToDoSets().observe(this, new Observer<ArrayList<WorkoutSet>>() {
                @Override
                public void onChanged(@Nullable ArrayList<WorkoutSet> workoutSets) {
                    if (workoutSets == null) return;
                    Log.i(TAG, "todo sets has changed! " + Integer.toString(workoutSets.size()));
                    if (mToDoSets.size() == 0)
                        mToDoSets.addAll(workoutSets);
                    else{
                        for (WorkoutSet workoutset : mToDoSets){
                            if (!mToDoSets.contains(workoutset)) mToDoSets.add(workoutset);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if (mServiceBound){
            if (mService != null) mService.removeListener(MainActivity.this);
            if (mServiceConnection != null) unbindService(mServiceConnection);
            mServiceBound = false;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        mNetworkConnected = mRefTools.isNetworkConnected();
        if (mSavedStateViewModel != null) mSavedStateViewModel.initialise();

        // Bind to FITAPIManager Service!
        if (mNetworkConnected && !authInProgress) {
            if (mGoogleAccount != null) {
                Intent intent = new Intent(this, FITAPIManager.class);
                intent.putExtra(Constants.LABEL_USER, mGoogleAccount.getEmail());
                Log.d(TAG, "starting FIT Service");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intent);
                } else {
                    this.startService(intent);
                }
            }else
                checkUserAuth();  // this continues here if logged in and same user
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        if (mServiceBound){
            if (mService != null) mService.removeListener(this);
            if (mServiceConnection != null) unbindService(mServiceConnection);
            mServiceBound = false;

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        unregisterReceiver(mAmbientUpdateBroadcastReceiver);

        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
        mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mNetworkConnected = mRefTools.isNetworkConnected();
        if (!mServiceBound){
            if (mGoogleAccount != null) {
                Log.d(TAG,"binding to FITAPIManager");
                Intent intent = new Intent(this, FITAPIManager.class);
                intent.putExtra(Constants.LABEL_USER, mGoogleAccount.getEmail());
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
        IntentFilter filter = new IntentFilter(AMBIENT_UPDATE_ACTION);
        registerReceiver(mAmbientUpdateBroadcastReceiver, filter);

        refreshDisplayAndSetNextUpdate();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState ");
        String sKey;
        if (mLastStep.LastUpdated > 0){
            sKey = getString(R.string.label_first_step);
            outState.putLong(sKey, mLastStep.FirstCount);
            sKey = getString(R.string.label_last_step);
            outState.putLong(sKey, mLastStep.LastCount);
            sKey = getString(R.string.label_last_step_upd);
            outState.putLong(sKey, mLastStep.LastUpdated);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    // Activity
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                // Do stuff
                Log.d(TAG, "Button 1 pressed");
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                // Do stuff
                Log.d(TAG, "Button 2 pressed");
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
                Log.d(TAG, "Button 3 pressed");
                // Do stuff
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int currentState = mSavedStateViewModel.getCurrentState().getValue();
        switch (requestCode){
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
            case REQUEST_CHOOSE_ACCOUNT_CODE:
                checkUserAuth();
                break;
            case REQUEST_RECORDING_PERMISSION_CODE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    UserPreferences.setConfirmUseRecord(this, true);
                    displaySpeechRecognizer();
                }
                break;
            case SPEECH_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    List<String> results = data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                        String spokenText = results.get(0);
                        if (currentState == STATE_ENTRY) {
                            WorkoutSet set =  mSavedStateViewModel.getActiveWorkoutSet().getValue();
                            if ((set != null) && (spokenText.length() > 0)) {
                                set.exerciseName = spokenText;
                                Exercise exercise = getExerciseByName(spokenText);
                                if (exercise != null)
                                    set.exerciseID = (int) exercise._id;
                                else
                                    set.exerciseID = (mRefTools.getExerciseNames().length + 1);
                                mSavedStateViewModel.setActiveWorkoutSet(set);
                            }

                        }
                }
                break;
            case REQUEST_OAUTH_REQUEST_CODE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result != null) {
                        mGoogleAccount = result.getSignInAccount();
                        checkUserAuth();
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
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**   HomePageFragment Interfaces     ***/
    @Override
    public void onHomePageFragmentInteraction(int src, int id, String text, int color) {
        String sLabel;
        String defaultValue = "";
        int i; int selectionType = 0;
        if (src == 0){
            return;
        }
        if (src >=1 && src <= 10){   // new item.
            createWorkout();  // initialise classes and id - not start or end times!
            createWorkoutSet();
            switch (src){
                case 1:
                    i = UserPreferences.getActivityID1(this);
                    if (i > 0){
                        mWorkout.activityID = i;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(i);
                        mWorkout.identifier = mRefTools.getFitnessActivityIdentifierById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName =  mWorkout.activityName;
                    }else {
                        mWorkout.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(Constants.SELECTION_ACTIVITY_GYM);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_STRENGTH);
                        mActivityColor = mRefTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_STRENGTH);
                        mSavedStateViewModel.setSetIsGym(true);
                        mSavedStateViewModel.setSetIsShoot(false);
                        mWorkout.identifier = FitnessActivities.STRENGTH_TRAINING;
                        sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.WORKOUT_TYPE_STRENGTH);
                        defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                        selectionType = Constants.SELECTION_ACTIVITY_GYM;
                    }
                    break;
                case 2:
                    i = UserPreferences.getActivityID2(this);
                    if (i > 0){
                        mWorkout.activityID = i;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(i);
                        mWorkout.identifier = mRefTools.getFitnessActivityIdentifierById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(i));
                        mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(i));
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName =  mWorkout.activityName;
                    }else {
                        mWorkout.activityID = Constants.WORKOUT_TYPE_ARCHERY;
                        mWorkout.activityName = Utilities.SelectionTypeToString(this, Constants.WORKOUT_TYPE_ARCHERY);
                        mWorkout.identifier = FitnessActivities.ARCHERY;
                        mWorkout.setCount = 10;   // 10 ends
                        mWorkout.repCount = 3;
                        mWorkout.shotsPerEnd = 3;
                        mSavedStateViewModel.setSetIsShoot(true);
                        mSavedStateViewModel.setSetIsGym(false);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName = mWorkout.activityName;
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_ARCHERY);
                        mActivityColor = mRefTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_ARCHERY);
                    }
                    break;
                case 3:
                    i = UserPreferences.getActivityID3(this);
                    if (i > 0){
                        mWorkout.activityID = i;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(i);
                        mWorkout.identifier = mRefTools.getFitnessActivityIdentifierById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(i));
                        mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(i));

                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName =  mWorkout.activityName;
                    }else {
                        mWorkout.activityID = 0;
                        mWorkout.activityName = "";
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS);
                        mActivityColor = mRefTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_AEROBICS);
                        mSavedStateViewModel.setSetIsGym(false);
                        mSavedStateViewModel.setSetIsShoot(false);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName = mWorkout.activityName;
                        sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.WORKOUT_TYPE_AEROBICS);
                        defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                        selectionType = Constants.SELECTION_ACTIVITY_CARDIO;
                    }
                    break;
                case 4:
                    i = UserPreferences.getActivityID4(this);
                    if (i > 0){
                        mWorkout.activityID = i;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(i);
                        mWorkout.identifier = mRefTools.getFitnessActivityIdentifierById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(i));
                        mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(i));
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName =  mWorkout.activityName;
                    }else {
                        mWorkout.activityID = 0;
                        mWorkout.activityName = "";
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_RUNNING);
                        mActivityColor = mRefTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_RUNNING);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName = mWorkout.activityName;
                        mSavedStateViewModel.setSetIsGym(false);
                        mSavedStateViewModel.setSetIsShoot(false);

                        sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_RUN);
                        defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                        selectionType = Constants.SELECTION_ACTIVITY_RUN;
                    }
                    break;
                case 5:
                    i = UserPreferences.getActivityID5(this);
                    if (i > 0){
                        mWorkout.activityID = i;
                        mWorkout.activityName = mRefTools.getFitnessActivityTextById(i);
                        mWorkout.identifier = mRefTools.getFitnessActivityIdentifierById(i);
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(i);
                        mActivityColor = mRefTools.getFitnessActivityColorById(i);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName =  mWorkout.activityName;
                        mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(i));
                        mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(i));

                    }else {
                        mWorkout.activityID = 0;
                        mWorkout.activityName = "";
                        mActivityIcon = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_KAYAKING);
                        mActivityColor = mRefTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_KAYAKING);
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName = mWorkout.activityName;
                        mSavedStateViewModel.setSetIsGym(false);
                        mSavedStateViewModel.setSetIsShoot(false);
                        sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
                        defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                        selectionType = Constants.SELECTION_ACTIVITY_SPORT;
                    }
                    break;
                case 6:
                    mWorkout.activityID = 0;
                    mWorkout.activityName = "";
                    mActivityIcon = mRefTools.getFitnessActivityIconResById(68);
                    mActivityColor = mRefTools.getFitnessActivityColorById(68); //downhill skiing
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);

                    sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
                    defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_WINTER;
                    break;
                case 7:
                    mWorkout.activityID = 0;
                    mWorkout.activityName = "";
                    mActivityIcon = mRefTools.getFitnessActivityIconResById(83);
                    mActivityColor = mRefTools.getFitnessActivityColorById(83); //swimmer diving into water
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
                    defaultValue = UserPreferences.getPrefStringByLabel(getApplicationContext(), sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_WATER;
                    break;
                case 8:
                    mWorkout.activityID = 0;
                    mWorkout.activityName = "";
                    mActivityIcon = mRefTools.getFitnessActivityIconResById(16);
                    mActivityColor = mRefTools.getFitnessActivityColorById(16); //man cycling
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    selectionType = Constants.SELECTION_ACTIVITY_BIKE;
                    break;
                case 9:
                    mWorkout.activityID = 0;
                    mWorkout.activityName = "";
                    mActivityIcon = mRefTools.getFitnessActivityIconResById(24);
                    mActivityColor = mRefTools.getFitnessActivityColorById(24); //dancing
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    selectionType = Constants.SELECTION_ACTIVITY_MISC;
                    break;
                case 10:
                    mWorkout.activityID = 0;
                    mWorkout.activityName = "";
                    mActivityIcon = mRefTools.getFitnessActivityIconResById(24);
                    mActivityColor = mRefTools.getFitnessActivityColorById(24); //dancing
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    selectionType = 24;
                    break;
            }
            mSavedStateViewModel.setIconID(mActivityIcon);
            mSavedStateViewModel.setColorID(mActivityColor);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);


            if (selectionType == 0)
                mNavigationController.navigate(R.id.action_homePageFragment_to_sessionEntryFragment);
            else
                startCustomList(selectionType, defaultValue);

        }
        if (src == 11){ // settings
            mSavedStateViewModel.setCurrentState(STATE_SETTINGS);
            startCustomList(Constants.SELECTION_USER_PREFS,  defaultValue);
        }
        if (src == 13){
            if (id == HomePageFragment.MSG_LOCATION){
            }
        }
    }
    @Override
    public void onHomePageFragmentLongClick(int id){
        //mHomeImageView
        if (id == 0) {
            Toast.makeText(this, "read live", Toast.LENGTH_SHORT).show();
            if (mServiceBound) {
                WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mWorkoutSet == null){
                    mWorkoutSet = new WorkoutSet();
                    Calendar cal = Calendar.getInstance();
                    Date now = new Date();
                    cal.setTime(now);
                    mWorkoutSet.end = cal.getTimeInMillis();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 1);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    mWorkoutSet.start = cal.getTimeInMillis();
                    mWorkoutSet._id = mWorkoutSet.start;
                }
                Log.d(TAG, "reading seg " + mWorkoutSet.shortText());
                mService.readHistory(mWorkoutSet);
            }
        }
        // home_action1_image
        if (id == 1)
            if (!mCompletedWorkouts.isEmpty())
                startCustomList(Constants.SELECTION_WORKOUT_HISTORY,  "");
            else
                startCustomList(Constants.SELECTION_ACTIVE_SESSION, "");
    }
    @Override
    public void onHomePageFragmentComplete(int id) {
        Log.d(TAG, "oHomePageFragmentComplete");
        mSavedStateViewModel.setCurrentState(STATE_HOME);
        if (mSessionViewModel != null && ((mSessionViewModel.getBodypartArrayList() == null)
                || (mSessionViewModel.getBodypartArrayList().size() == 0))) {
            Log.d(TAG, "loading reference data onLiveFragmentComplete isLoading " + Boolean.toString(mSessionViewModel.isLoadingRefs()));
            if (!mSessionViewModel.isLoadingRefs()) {
                Log.d(TAG, "loading reference data mSessionViewModel.getReferenceData");
                mSessionViewModel.getReferenceData();
            }
        }

    }

    @Override
    public void onLiveFragmentInteraction(int src, int id, String text, int color) {
        switch (src){
            case 1:
                if (mSavedStateViewModel.getPauseStart().getValue() == 0) {
                    pauseSession();
                    Toast.makeText(this, getString(R.string.label_session_paused), Toast.LENGTH_SHORT).show();
                }else{
                    resumeSession();
                    Toast.makeText(this, getString(R.string.label_session_resumed), Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if ((text != null) && (text.equals("long"))) {  // long click 2
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }else {

                }
                break;
            case 3:   // end this set
                if ((text != null) && text.equals("long")){  // long click 3
                    if (UserPreferences.getConfirmEndSession(getApplicationContext())){
                        mCustomConfirmDialog = CustomConfirmDialog.newInstance(5,getString(R.string.confirm_end_toggle_prompt), MainActivity.this);
                        mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
                    }else{
                        final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                        if (frameLayout != null) {
                            showAlertDialogConfirm(frameLayout, ACTION_STOPPING);
                        }
                    }
                }else {
                    mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    if (mWorkoutSet != null) {
                        if ((mRepetitionCount > 0) && (mWorkoutSet != null)) {
                            mWorkoutSet.repCount = mRepetitionCount;
                            mRepetitionCount = 0;
                        }
                    }
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mIsGymWorkout = (mSavedStateViewModel.getIsGym().getValue() != null) ? mSavedStateViewModel.getIsGym().getValue() : false;
                    mIsShooting = (mSavedStateViewModel.getIsShoot().getValue() != null) ? mSavedStateViewModel.getIsShoot().getValue() : false;
                    if (mIsGymWorkout || mIsShooting) {
                        long timeMs = System.currentTimeMillis();
                        completeActiveSet(timeMs);
                        mNavigationController.navigate(R.id.action_global_liveFragment);
                    } else {
                        if (UserPreferences.getConfirmEndSession(getApplicationContext())) {
                            mCustomConfirmDialog = CustomConfirmDialog.newInstance(5, getString(R.string.confirm_end_toggle_prompt), MainActivity.this);
                            mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
                        } else {
                            final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                            if (frameLayout != null) {
                                showAlertDialogConfirm(frameLayout, ACTION_STOPPING);
                            }
                        }
                    }
                }
                break;
            case 4:
                if (UserPreferences.getConfirmEndSession(getApplicationContext())){
                    mCustomConfirmDialog = CustomConfirmDialog.newInstance(5,getString(R.string.confirm_end_toggle_prompt), MainActivity.this);
                    mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
                }else {
                    final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                    if (frameLayout != null) {
                        showAlertDialogConfirm(frameLayout, ACTION_STOPPING);
                    }
                }
                break;
        }

    }

    @Override
    public void onLiveFragmentComplete(int id) {
        Log.i(TAG, "LiveFragment complete");
        mSavedStateViewModel.setCurrentState(STATE_LIVE);
        // in case of deep linking load references now
        if (mSessionViewModel != null && ((mSessionViewModel.getBodypartArrayList() == null)
                || (mSessionViewModel.getBodypartArrayList().size() == 0))) {
            Log.d(TAG, "loading reference data onLiveFragmentComplete isLoading " + Boolean.toString(mSessionViewModel.isLoadingRefs()));
            if (!mSessionViewModel.isLoadingRefs()) {
                Log.d(TAG, "loading reference data mSessionViewModel.getReferenceData");
                mSessionViewModel.getReferenceData();
            }
        }
    }
    // custom score  fragment interactions
    @Override
    public void onCustomScoreSelected(int type, long id, String title, int setIndex) {
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (type == Constants.SELECTION_SETS){
            if (mWorkoutSet != null) mWorkoutSet.setCount = (int)id;
        }
        if (type == Constants.SELECTION_REPS){
            if (mWorkoutSet != null) mWorkoutSet.repCount = (int)id;
        }
        if (type == Constants.SELECTION_GOAL_DURATION){
            mSavedStateViewModel.setGoalDuration((int)id);
        }
        if (type == Constants.SELECTION_GOAL_STEPS){
            mSavedStateViewModel.setGoalSteps((int)id);
        }

        if (mWorkoutSet != null) mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);

        if (mCustomScoreDialogFragment != null) {
            mCustomScoreDialogFragment.dismiss();
            mCustomScoreDialogFragment = null;
            mNavigationController.popBackStack();
           // fragmentManager.popBackStack(CustomScoreDialogFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (mCustomListFragment != null) {
            mCustomListFragment.dismiss();
            mCustomListFragment = null;
            mNavigationController.popBackStack();
            //fragmentManager.popBackStack(CustomListFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    /***  CustomListFragment callbacks   **/
    @Override
    public void onCustomItemSelected(int type, long id, String name, int resid, String identifier) {
        WorkoutSet privateSet;
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (mWorkout == null) createWorkout();
        if (mWorkoutSet == null) createWorkoutSet();

        if ((type == Constants.SELECTION_FITNESS_ACTIVITY) || (type == Constants.SELECTION_ACTIVITY_BIKE)
                || (type == Constants.SELECTION_ACTIVITY_GYM) || (type == Constants.SELECTION_ACTIVITY_CARDIO)
                || (type == Constants.SELECTION_ACTIVITY_SPORT) || (type == Constants.SELECTION_ACTIVITY_RUN)
                || (type == Constants.SELECTION_ACTIVITY_WATER) || (type == Constants.SELECTION_ACTIVITY_WINTER)
                || (type == Constants.SELECTION_ACTIVITY_MISC)) {

            String sLabel = getString(R.string.default_loadtype) + Integer.toString(type);
            String sTarget = Long.toString(id);
            UserPreferences.setPrefStringByLabel(getApplicationContext(),sLabel,sTarget);
            mWorkout.activityID = (int) id;
            mWorkout.activityName = name;
            mWorkout.identifier = identifier;
            mWorkoutSet.activityID = mWorkout.activityID;
            mWorkoutSet.activityName = mWorkout.activityName;
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            mActivityIcon = (resid > 0) ? resid : mRefTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(mActivityIcon);
            mActivityColor = mRefTools.getFitnessActivityColorById(mWorkout.activityID);
            mSavedStateViewModel.setColorID(mActivityColor);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);

            Bundle bundle = new Bundle();
            bundle.putInt(SessionEntryFragment.ARG_ACTIVITYID, mWorkout.activityID);
            bundle.putInt(SessionEntryFragment.ARG_COLORID, mActivityColor);
            bundle.putInt(SessionEntryFragment.ARG_RESID, mActivityIcon);
            mNavigationController.navigate(R.id.action_homePageFragment_to_sessionEntryFragment, bundle);
            return;
        }
        //TODO: day and month selection for FIT queries & results
        if ((type == Constants.SELECTION_DAYS) || (type == Constants.SELECTION_MONTHS)) {
            Log.d(TAG, "CustomItemSelected got " + Long.toString(id) + " months " + Integer.toString(resid));
        }
        int set_index = mSavedStateViewModel.getSetIndex().getValue();
        privateSet =  ((set_index > 0) && (mSessionViewModel.getToDoSetSize() >= set_index)) ? mSessionViewModel.getToDoSets().getValue().get(set_index-1) : null;
        if (privateSet == null) privateSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (type == Constants.SELECTION_EXERCISE) {
         //   if (set_index == 0) {
                privateSet.exerciseID = (int) id;
                privateSet.exerciseName = name;
                Exercise exercise = getExerciseById((int) id);
                if (exercise != null) {
                    if (exercise.first_BPID > 0) {
                        privateSet.bodypartID = exercise.first_BPID;
                        privateSet.bodypartName = exercise.first_BPName;
                        if (exercise.lastTrained == 0) {
                            Bodypart bodypart = getBodypartById(exercise.first_BPID);
                            if (privateSet.repCount == 0)
                                privateSet.repCount = (bodypart.lastReps > 0) ? bodypart.lastReps : bodypart.repMax;
                            if (privateSet.setCount == 0)
                                privateSet.setCount = (bodypart.lastSets > 0) ? bodypart.lastSets : 0;
                            if (privateSet.weightTotal == 0)
                                privateSet.weightTotal = (bodypart.lastWeight > 0) ? bodypart.lastWeight : 0;
                        }
                    }
                    privateSet.resistance_type = exercise.resistanceType;
                    if (privateSet.repCount == 0)
                        privateSet.repCount = (exercise.lastReps > 0) ? exercise.lastReps : 10;
                    if (privateSet.setCount == 0)
                        privateSet.setCount = (exercise.lastSets > 0) ? exercise.lastSets : 1;
                    if (privateSet.weightTotal == 0)
                        privateSet.weightTotal = (exercise.lastWeight > 0) ? exercise.lastWeight : 10F;
                }
         //   }
        }
        if (type == Constants.SELECTION_BODYPART) {
            privateSet.bodypartID = (int)id;
            privateSet.bodypartName = name;
            UserPreferences.setLastBodyPartID(this, Long.toString(id));
            UserPreferences.setLastBodyPartName(this, name);
        }
        if (type == Constants.SELECTION_USER_PREFS){
            UserPreferences.setPrefByLabel(this, name, (resid == 1));
        }
        if (type == Constants.SELECTION_SETS){
            privateSet.setCount = (int)id;
        }
        if (type == Constants.SELECTION_REPS){
            privateSet.repCount = (int)id;
        }
        if ((type == Constants.SELECTION_WEIGHT_KG)||(type == Constants.SELECTION_WEIGHT_LBS)){
            privateSet.weightTotal = Float.parseFloat(name);
        }
        if (type == Constants.SELECTION_WEIGHT_LBS){
            privateSet.weightTotal = (Float.parseFloat(name)>0) ? Constants.LBS_TO_KG * Float.parseFloat(name) : 0;  // stored as KG!
        }

        if (type == Constants.SELECTION_TARGET_FIELD){
            if (mWorkout.shootFormatID != id) {
                mWorkout.shootFormatID = (int) id;
                mWorkout.shootFormat = name;
                String sLabel = "last_" + Utilities.SelectionTypeToString(this,type);
                UserPreferences.setPrefStringByLabel(this,sLabel,name);
                sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_DISTANCE_FIELD);
                String sValue = UserPreferences.getPrefStringByLabel(this, sLabel);
                int index = 0;
                String resourceID ="archery_distance_" + name.toLowerCase();
                int resID = getResources().getIdentifier(resourceID,"array",getPackageName());
                if (sValue.length() > 0){
                    String[] distances = getResources().getStringArray(resID);
                    for (String distance : distances){
                        if (distance.equals(sValue)) {
                            mWorkout.distanceID = index;
                            break;
                        }
                        index++;
                    }
                    mWorkout.distanceName = sValue;

                }
                sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_DISTANCE_FIELD);
                sValue = UserPreferences.getPrefStringByLabel(this, sLabel);
                index = 0;
                resourceID ="archery_distance_" + name.toLowerCase();
                resID = getResources().getIdentifier(resourceID,"array",getPackageName());
            }
        }
        if (type == Constants.SELECTION_TARGET_EQUIPMENT){
            mWorkout.equipmentID = (int)id;
            mWorkout.equipmentName = name;
            String sLabel = "last_" + Utilities.SelectionTypeToString(this,type);
            UserPreferences.setPrefStringByLabel(this,sLabel,name);

        }
        if ((type == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (type == Constants.SELECTION_TARGET_DISTANCE_TARGET)){
            mWorkout.distanceID = (int)id;
            mWorkout.distanceName = name;
            if (mWorkout.shootFormat.length() > 0) {
                String sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                UserPreferences.setPrefStringByLabel(this, sLabel, name);
            }
        }
        if ((type == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD)|| (type == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET)){
            mWorkout.targetSizeID = (int)id;
            mWorkout.targetSizeName = name;
            if (mWorkout.shootFormat.length() > 0) {
                String sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                UserPreferences.setPrefStringByLabel(this, sLabel, name);
            }
        }
        if (type == Constants.SELECTION_TARGET_ENDS){
            mWorkout.setCount = (int)id;
            if (mWorkout.shootFormat.length() > 0) {
                String sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                String value = Long.toString(id);
                UserPreferences.setPrefStringByLabel(this,sLabel,value);
            }
        }
        if (type == Constants.SELECTION_TARGET_SHOTS_PER_END){
            mWorkout.shotsPerEnd = (int)id;
            mWorkout.repCount = (int)id;
            if (mWorkout.shootFormat.length() > 0) {
                String sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                String value = Long.toString(id);
                UserPreferences.setPrefStringByLabel(this,sLabel,value);
            }
        }
        if (type == Constants.SELECTION_TARGET_POSSIBLE_SCORE){
            mWorkout.totalPossible = (int)id;
            if (mWorkout.shootFormat.length() > 0) {
                String sLabel = "last_" + mWorkout.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                String value = Long.toString(id);
                UserPreferences.setPrefStringByLabel(this,sLabel,value);
            }
        }
        if (type == Constants.SELECTION_REST_DURATION_GYM){
            UserPreferences.setWeightsRestDuration(this, Integer.parseInt(name));
            mExpectedRestDuration = (Integer.parseInt(name) * 1000);

        }
        if (type == Constants.SELECTION_REST_DURATION_TARGET){
            UserPreferences.setArcheryRestDuration(this, Integer.parseInt(name));
            mExpectedRestDuration = (Integer.parseInt(name) * 1000);
        }
        if (mWorkout != null) mSavedStateViewModel.setActiveWorkout(mWorkout);
        if (privateSet != null) mSavedStateViewModel.setActiveWorkoutSet(privateSet);

        if (mCustomScoreDialogFragment != null) {
            mCustomScoreDialogFragment.dismiss();
            mCustomScoreDialogFragment = null;
        }
        if (mCustomListFragment != null) {
            mCustomListFragment.dismiss();
            mCustomListFragment = null;
        }


    }
    @Override
    public void onConnected() {
            //Toast.makeText(this,"FIT API Manager service connected!",Toast.LENGTH_LONG);
            // We can now safely use the API we requested access to
            if (UserPreferences.getConfirmUseSensors(this) && UserPreferences.getAppSetupCompleted(this)
                    && (mGoogleAccount != null)){
                try {
                    if ((mSavedStateViewModel != null) && (mSavedStateViewModel.getActiveWorkout().getValue() != null))
                        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                }catch (Exception e){
                    Log.e(TAG, "onConnected getActiveWorkout error " + e.getLocalizedMessage());
                }

             try{
                    if ((mWorkout != null) && ((mWorkout.start != 0) && (mWorkout.end == 0))) { // in-progress
                        listSensorDataSources(DataType.TYPE_HEART_RATE_BPM);
                        listSensorDataSources(DataType.TYPE_STEP_COUNT_CUMULATIVE);
                        listSensorDataSources(DataType.TYPE_LOCATION_SAMPLE);
                        listSensorDataSources(DataType.TYPE_ACTIVITY_SAMPLES);
                        listSensorDataSources(DataType.TYPE_WORKOUT_EXERCISE);
                        listSensorDataSources(DataType.TYPE_CALORIES_EXPENDED);
                        listSensorDataSources(DataType.TYPE_MOVE_MINUTES);
                    } else {
                        listSensorDataSources(DataType.TYPE_HEART_RATE_BPM);
                        listSensorDataSources(DataType.TYPE_STEP_COUNT_CUMULATIVE);
                        listSensorDataSources(DataType.TYPE_CALORIES_EXPENDED);
                        listSensorDataSources(DataType.TYPE_MOVE_MINUTES);
                    }
                }catch (Exception e){
                    Log.w(TAG, "Sensory setup error: " + e.getMessage());
                }

            }


    }

    @Override
    public void onDisconnected(int resultCode) {
        String sMsg = "MainActivity - FIT API Manager - Google disconnected";
        if (resultCode == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            sMsg = "Connection lost.  Cause: Network Lost.";
            Log.i(TAG, sMsg);
        } else if (resultCode == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            sMsg = "Connection lost.  Reason: Service Disconnected";
            Log.i(TAG, sMsg);
        }
        Toast.makeText(this,sMsg,Toast.LENGTH_LONG);
    }

    @Override
    public void onConnectionFailure() {
        Toast.makeText(this,"MainActivity - FIT API Manager - connection failure!",Toast.LENGTH_LONG);
        mWorkout.offline_recording = 1;
    }
    // FITAPI Manager Interface
    @Override
    public void onSessionChanged(int changeType, String sessionID ,int result) {
        Log.d(TAG, "FIT API Manager onSessionChanged " + sessionID + " " + Integer.toString(changeType));
        long lSessionID = Long.parseLong(sessionID.substring(3));
        // start session change type 0 = successful
        if (changeType == 0){
            if (mSavedStateViewModel.getActiveWorkout().getValue() != null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if ((mWorkout != null) && (mWorkout._id == lSessionID)){
                if (result == 1) {
                    long timeMs = System.currentTimeMillis();
                    mWorkout.last_sync = timeMs;
                    Log.d(TAG, "last_sync mWorkouts " + mWorkout.toString());
                    Log.d(TAG, "last_sync mWorkoutSet " + mWorkoutSet.toString());
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    //mSessionViewModel.saveSingleType(true, GSONHelper.LOAD_ACTIVE_SETS);
                }
            }
            mMessagesViewModel.addMessage(getString(R.string.session_start));
        }
        // Stop session
        if (changeType == 1) {
            if (mSessionViewModel.getCompletedWorkouts().getValue() != null) mCompletedWorkouts = mSessionViewModel.getCompletedWorkouts().getValue();
            if (mCompletedWorkouts != null) {
                for (Workout workout : mCompletedWorkouts) {
                    if (workout._id == lSessionID) {
                        if (result == 1) {
                            Log.d(TAG, "last_sync completed mWorkouts " + mWorkout.toString());
                            long timeMs = System.currentTimeMillis();
                            workout.last_sync = timeMs;
                        }
                    }
                }
                mSessionViewModel.setCompletedWorkouts(mCompletedWorkouts);
                mSessionViewModel.saveSingleType(false, GSONHelper.LOAD_COMPLETED_WORKOUTS);
            }
        }
        // insert segment // insert exercise
        if ((changeType == 2)||(changeType == 3)){
            boolean foundSet = false;
            if (mSessionViewModel.getCompletedSets().getValue() != null) mCompletedSets = mSessionViewModel.getCompletedSets().getValue();
            if (mCompletedSets != null)
                for (WorkoutSet activeSet : mCompletedSets){
                    if (activeSet.start  == lSessionID){
                        long timeMs = System.currentTimeMillis();
                        Log.d(TAG, "last_sync activeSet " + activeSet.toString());
                        activeSet.last_sync = timeMs;
                        foundSet = true;
                    }
                }
            if (mCompletedSets != null) {
                mSessionViewModel.setCompletedWorkoutSets(mCompletedSets);
                mSessionViewModel.saveSingleType(false, GSONHelper.LOAD_COMPETED_SETS);
            }
        }
    }


    @Override
    public void onDataChanged(Utilities.TimeFrame timeFrame) {

    }

    @Override
    public void onDataComplete(Bundle results) {
        if (results != null)
            Log.d(TAG, "onDataComplete ." + results.toString());
        else
            Log.d(TAG, "onDataComplete");

    }

    private void vibrate(long version) {
        if (muteFeedback) return;
        try {
            if (version == 1) {
                long[] pattern = {0, 400, 200, 400};

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    if ((mVibrator != null) && (mVibrator.hasVibrator()))
                        mVibrator.vibrate(pattern, -1);
                    else
                        mVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));

            }
            if (version == 2){
                long[] pattern = {0, 800};

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    if ((mVibrator != null) && (mVibrator.hasVibrator()))
                        mVibrator.vibrate(pattern, -1);
                    else
                        mVibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (version == 3){
                long[] pattern = {0, 400};

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    if ((mVibrator != null) && (mVibrator.hasVibrator()))
                        mVibrator.vibrate(pattern, -1);
                    else
                        mVibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }catch (Exception e){
            Log.d(TAG, "Vibrate error " + e.getMessage());
        }
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        if (UserPreferences.getConfirmUseRecord(this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            // Start the activity, the intent will be populated with the speech text
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }else{
            Intent intent = new Intent(this, PermissionsActivity.class);
            intent.putExtra(PermissionsActivity.ARG_REQUESTS,PermissionsActivity.REQUEST_ARG_RECORDING);
            startActivityForResult(intent, REQUEST_RECORDING_PERMISSION_CODE );
        }


    }


    private void startReportSessionFragment(){
/*
        if (fragmentManager.getBackStackEntryCount() > 0) fragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        currentState = STATE_REPORT;
        SessionReportFragment sessionReportFragment = SessionReportFragment.newInstance(mActivityIcon,mActivityColor,mWorkoutSet._id,mWorkout._id,mExpectedRestDuration);
        sessionReportFragment.show(fragmentManager, SessionReportFragment.TAG);
*/

    }

    private void doImageWork(Uri getAddress){
        Data.Builder builder = new Data.Builder();
        if (getAddress != null) {
            builder.putString(KEY_IMAGE_URI, getAddress.toString());
            Data inputData = builder.build();
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest imageRequest =
                    new OneTimeWorkRequest.Builder(ImageWorker.class)
                            .setInputData(inputData)
                            .setConstraints(constraints)
                            .build();
            mWorkManager.enqueue(imageRequest);
            mWorkManager.beginWith(imageRequest).getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
                @Override
                public void onChanged(List<WorkInfo> workInfos) {
                    if (workInfos.size() > 0) {
                        WorkInfo workInfo = workInfos.get(0);
                        boolean finished = workInfo.getState().isFinished();
                        if (!finished) {
                            //showWorkInProgress();
                        } else {
                            // showWorkFinished();
                            Data outputData = workInfo.getOutputData();

                            String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);

                            // If there is an output file show "See File" button
                            if (!TextUtils.isEmpty(outputImageUri)) {
                                Log.d(TAG, outputImageUri.toLowerCase());
                                UserPreferences.setLastUserPhotoInternal(getApplicationContext(), outputImageUri);
                                if (mHomepageFragment != null){
                                    if (mHomepageFragment.isVisible()) mHomepageFragment.setHomeImageViewURI();
                                }
                            }
                        }
                    }
                }
            });

        }

    }

    private void checkUserAuth(){
        if (!authInProgress) {
            Context context = getApplicationContext();
            mGoogleAccount =  GoogleSignIn.getLastSignedInAccount(context);
            if (mGoogleAccount != null) {
                String PersonId = mGoogleAccount.getId();
                String sInternal = UserPreferences.getLastUserPhotoInternal(context);
                if (!PersonId.equals(UserPreferences.getLastUserID(context))) {
                    sInternal = "";
                    String PersonName = mGoogleAccount.getDisplayName();
                    UserPreferences.setLastUserName(context, PersonName);
                    UserPreferences.setLastUserID(context, PersonId);
                    UserPreferences.setLastUserPhotoInternal(context, getString(R.string.my_empty_string));
                    UserPreferences.setWorkOffline(context, false);
                    UserPreferences.setUserEmail(context, mGoogleAccount.getEmail());
                }
                if (sInternal.length()==0){
                    Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                    doImageWork(PersonPhotoUri);
                }
                authInProgress = false;
                Intent intent = new Intent(this, FITAPIManager.class);
                intent.putExtra(Constants.LABEL_USER, mGoogleAccount.getEmail());
                Log.d(TAG, "starting FIT Service");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intent);
                } else {
                    this.startService(intent);
                }
            } else {
                authInProgress = true;
                signIn();  // proper sign-in
            }
        }  // finish if not authInProgress already
    }

    private void createWorkout(){
        mWorkout = new Workout();
        long timeMs = System.currentTimeMillis();
        mWorkout._id = timeMs;
        mWorkout.packageName = getPackageName();
    }
    private void createWorkoutSet(){
        // start fresh and re-initialise the controls
        mWorkoutSet = new WorkoutSet();
        long timeMs = System.currentTimeMillis();
        mWorkoutSet._id = timeMs;
        if (mWorkout != null) {
            mWorkoutSet.activityID = mWorkout.activityID;
            mWorkoutSet.activityName = mWorkout.activityName;
            mWorkout.setCount += 1;
            mWorkoutSet.setCount = mWorkout.setCount; // new guy is the last set - also safer than toDoSet size
        }
    }
    private void startSession(){
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if ((mWorkout == null) || (mWorkout.activityID == 0)) return;  // invalid - just in case
        try {
            mWorkout.offline_recording = !mNetworkConnected ? 1 : 0;

            if (mServiceBound && mNetworkConnected){
                if (!mService.isClientConnected() && mService.isNetworkConnected()) mService.getConnected();
            }
            // first set because we know the start has not been set!
            if ((mWorkout.start == 0) && (mWorkout.end == 0)) mCurrentSetIndex = 1;

            // good to go now!
            long timeMs = System.currentTimeMillis();

            mWorkout.start = timeMs;
            mWorkout._id = timeMs;
            mWorkout.start_steps = mLastStep.LastCount;

            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();

            if (mSavedStateViewModel.getActiveWorkoutSet() != null){
                WorkoutSet workoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mToDoSets.size() == 0 && workoutSet.isValid())
                    addToDoSet(workoutSet); // finalise the current edited set
            }


            // initialise the current set to the first item in toGo list
            mWorkoutSet = mToDoSets.get(mCurrentSetIndex-1);

            if (mWorkoutSet.activityID != mWorkout.activityID) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }
            mWorkoutSet.setCount = mCurrentSetIndex;
            mWorkoutSet.start = timeMs;  // same as workout start

            mSavedStateViewModel.setPauseStart(0L);  // no active pause

            mExpectedRestDuration = mWorkoutSet.rest_duration; // pick up the expected rest duration.

            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSessionViewModel.setToDoWorkoutSets(mToDoSets);
            mSessionViewModel.setCompletedWorkoutSets(mCompletedSets);

            if (mServiceBound && (mWorkout.offline_recording != 1)){
                if (!isRecordingAPIRunning) {
                    mService.subscribeRecordingApi();
                    isRecordingAPIRunning = true;
                }
                mService.startLiveFitSession(mWorkout, mGoogleAccount);
            }
        }catch (Exception e) {
            String sMsg = "Error starting session " + e.getMessage();
            Log.e(TAG, sMsg);
            Toast.makeText(this, sMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void completeActiveSet(long end){
        if (mServiceBound && mNetworkConnected){
            if (!mService.isClientConnected() && mService.isNetworkConnected()){
                Log.d(TAG, "getConnected");
                mService.getConnected();
            }
        }
        int currentSetIndex = mSavedStateViewModel.getSetIndex().getValue();
        WorkoutSet privateSet =  ((currentSetIndex > 0) && (mSessionViewModel.getToDoSetSize() >= currentSetIndex)) ? mSessionViewModel.getToDoSets().getValue().get(currentSetIndex-1) : null;
        if (privateSet != null){
            Log.i(TAG, "completeActiveSet setting the set " + privateSet.activityName + " " + Utilities.getTimeDateString(privateSet.start));
            if (privateSet.start == 0){
                long start = 0;
                start = mSavedStateViewModel.getActiveWorkout().getValue().start;
                if ((start != mWorkout.start) && (start > 0)) Log.e(TAG, "not matching workout starts ");
                Log.e(TAG, "set with not start - using workout " + Utilities.getTimeDateString(start));
                if (start > 0) privateSet.start = start;
            }
            privateSet.end = end;
            mSavedStateViewModel.setPauseStart(end);

            if (privateSet.end - (privateSet.start - privateSet.pause_duration) > 0)
                privateSet.duration = privateSet.end - (privateSet.start - privateSet.pause_duration);
            else
                privateSet.duration = privateSet.end - privateSet.start;  //wrong pause duration
            mCompletedSets = mSessionViewModel.getCompletedSets().getValue();
            if (mCompletedSets == null){
                mCompletedSets = new ArrayList<>();
                mCompletedSets.add(privateSet);
            }else {
                boolean bFound = false; int i=0;
                for (WorkoutSet s : mCompletedSets){
                    if (s._id == privateSet._id){
                        mCompletedSets.set(i, privateSet);
                        bFound = true;
                        break;
                    }
                    i++;
                }
                if (!bFound) mCompletedSets.add(privateSet);
            }
           mSessionViewModel.setCompletedWorkoutSets(mCompletedSets);
           if (mWorkout.offline_recording == 0) {
               if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                   if (mNetworkConnected && mServiceBound) {
                       Device device = Device.getLocalDevice(getApplicationContext());
                       mService.insertExerciseSet(mWorkoutSet.start, mWorkoutSet.exerciseName, mWorkoutSet.resistance_type, mWorkoutSet.repCount,
                               mWorkoutSet.weightTotal, getPackageName(), device, mGoogleAccount);
                   }
               }else
                   if (mNetworkConnected && mServiceBound) {
                       Device device = Device.getLocalDevice(getApplicationContext());
                       String segName = mWorkoutSet.activityName;
                       if (Utilities.isShooting(mWorkoutSet.activityID))
                           segName = getString(R.string.label_shoot_end) + getString(R.string.my_space_string) + mWorkoutSet.setCount;
                       mService.insertSegmentSet(mWorkoutSet.start, segName,
                               end, mWorkoutSet.activityName, getPackageName(), device, mGoogleAccount);

                    }
           }
        }


    }

    private void stopSession(){
        long timeMs = System.currentTimeMillis();
        // connect if we can
        if (mServiceBound && mNetworkConnected){
            if (!mService.isClientConnected() && mService.isNetworkConnected()) mService.getConnected();
        }
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();

        if ((mWorkout == null) || (mWorkoutSet == null)){
            Log.e(TAG, "mWorkout or Set is NULL on stopSession");
            return;
        }
        mWorkout.end = timeMs; // not in progress!
        mWorkout.end_steps = mLastStep.LastCount;
        mWorkout.stepCount = (int)(mWorkout.end_steps - mWorkout.start_steps);
        mWorkout.duration = (timeMs - mWorkout.start) - mPauseDuration;

        try {
            // complete the active set!
           completeActiveSet(timeMs);
           mCompletedWorkouts = mSessionViewModel.getCompletedWorkouts().getValue();
           if (mCompletedWorkouts == null) mCompletedWorkouts = new ArrayList<>();
            if (mCompletedWorkouts.size() > 0){
                boolean bFound = false;
                for (Workout w : mCompletedWorkouts){
                    if (w._id == mWorkout._id){
                        w = mWorkout;
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) mCompletedWorkouts.add(mWorkout);
            }else
                mCompletedWorkouts.add(mWorkout);

            if (mServiceBound && (mWorkout.offline_recording != 1)){
                if (mNetworkConnected) mService.stopLiveFitSession(mWorkout, mGoogleAccount);
                if (isRecordingAPIRunning) {
                    mService.unsubscribeRecordingApi();
                    isRecordingAPIRunning = false;
                }
            }
            mSessionViewModel.setCompletedWorkoutSets(mCompletedSets);
            mSessionViewModel.setCompletedWorkouts(mCompletedWorkouts);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);

            Intent intent = new Intent(this, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                    getString(R.string.label_session_completed) + getString(R.string.my_space_string) + Utilities.getDurationBreakdown(mWorkout.duration));
            startActivity(intent);
        }catch (Exception e){
            Log.e(TAG, "Error stopping live" + e.getMessage());
        }

    }

    private void pauseSession(){
        long timeMs = System.currentTimeMillis();
        mSavedStateViewModel.setPauseStart(timeMs);
    }


    private void resumeSession(){
        long timeMs = System.currentTimeMillis();
        long pauseStart = mSavedStateViewModel.getPauseStart().getValue();
        if (pauseStart == 0L) return;  //not under pause should not be here!

        //if (mWorkout == null) mWorkout = mSessionViewModel.getWorkout().getValue();
        if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        mPauseDuration += (timeMs - pauseStart);
        mWorkoutSet.pause_duration = mWorkoutSet.pause_duration + (timeMs - pauseStart);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        mSavedStateViewModel.setPauseStart(0L);  // not under pause now

    }

     private void refreshDisplayAndSetNextUpdate(){
        //Log.d(TAG, "refreshDisplayAndSetNextUpdate");
        loadDataAndUpdateScreen();

        long timeMs = System.currentTimeMillis();

        if (mAmbientController.isAmbient()) {
            /* Calculate next trigger time (based on state). */
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;

            mAmbientUpdateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, triggerTimeMs, mAmbientUpdatePendingIntent);
        } else {
            /* Calculate next trigger time (based on state). */
            long delayMs = ACTIVE_INTERVAL_MS - (timeMs % ACTIVE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }
    }

    /** Updates display based on Ambient state. If you need to pull data, you should do it here. */
    private void loadDataAndUpdateScreen() {
        long currentTimeMs = System.currentTimeMillis();
        String newMessage = ""; String sOtherMessage = "";

        int currentState = mSavedStateViewModel.getCurrentState().getValue();
        if (mSavedStateViewModel.getActiveWorkout() != null){
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (currentState == STATE_LIVE) {
                if (mWorkoutSet.start > 0) {
                    newMessage = Utilities.getDurationBreakdown(currentTimeMs - mWorkoutSet.start);

                    if (mIsGymWorkout)
                        sOtherMessage = " " + Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " " + getString(R.string.label_set);

                    if (mIsShooting)
                        sOtherMessage = " " + Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " " + getString(R.string.label_shoot_ends);
                    mMessagesViewModel.addOtherMessage(sOtherMessage);
                } else {
                    newMessage = Utilities.getDurationBreakdown(currentTimeMs - mWorkout.start);
                    mMessagesViewModel.addMessage(newMessage);
                }
            }
            if ((currentState == STATE_HOME) || (currentState == STATE_ENTRY)){
                mCalendar.setTimeInMillis(currentTimeMs);
                //if (mCalendar.get(Calendar.HOUR) > 0)
                newMessage = String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.HOUR_OF_DAY))+":";
                newMessage += String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.MINUTE));
                newMessage += ":" + String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.SECOND));
                mMessagesViewModel.addMessage(newMessage);
            }
           if (currentState == STATE_END_SET) {
                if (mWorkoutSet != null) {
                    if (mWorkoutSet.end > 0) currentTimeMs = mWorkoutSet.end;
                    mCalendar.setTimeInMillis(currentTimeMs - mWorkoutSet.start);
                    newMessage = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(currentTimeMs - mWorkoutSet.start),
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeMs - mWorkoutSet.start) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currentTimeMs - mWorkoutSet.start)), // The change is in this line
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeMs - mWorkoutSet.start) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentTimeMs - mWorkoutSet.start)));
                    mMessagesViewModel.setRestMsg(newMessage);
                    newMessage = "";
                    if (mIsGymWorkout)
                        newMessage += " " + Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " " + getString(R.string.label_set);
                    if (mIsShooting)
                        newMessage += " " + Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " " + getString(R.string.label_shoot_ends);
                   if (newMessage.length() > 0) mMessagesViewModel.addOtherMessage(newMessage);
                } else {
                    if (mWorkout.end > 0) currentTimeMs = mWorkout.end;
                    mCalendar.setTimeInMillis(currentTimeMs - mWorkout.start);
                    newMessage = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(currentTimeMs - mWorkout.start),
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeMs - mWorkout.start) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currentTimeMs - mWorkout.start)), // The change is in this line
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeMs - mWorkout.start) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentTimeMs - mWorkout.start)));
                    mMessagesViewModel.setRestMsg(newMessage);
                    newMessage = "";
                }
                newMessage = String.format(Locale.getDefault(), "%02d", mWorkoutSet.stepCount);
                mMessagesViewModel.addOtherMessage(newMessage);
            }
        }else{
            mCalendar.setTimeInMillis(currentTimeMs);
            //if (mCalendar.get(Calendar.HOUR) > 0)
            newMessage = String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.HOUR_OF_DAY))+":";
            newMessage += String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.MINUTE));
            newMessage += ":" + String.format(Locale.getDefault(),"%02d", mCalendar.get(Calendar.SECOND));
            mMessagesViewModel.addMessage(newMessage);
        }

        if (mMessagesViewModel.getMessageCount() > 1) mMessagesViewModel.getNextMessage();
        if (mMessagesViewModel.getOtherMessageCount() > 1) mMessagesViewModel.getNextOtherMessage();
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private void showAlertDialogConfirm(final View parent_view, final int action){
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Dialog_Alert);
        String message_text = getString(R.string.my_empty_string);
        switch (action){
            case ACTION_STARTING:
                message_text = getString(R.string.action_starting);
                break;
            case ACTION_STOPPING:
            case ACTION_STOP_QUIT:
                message_text = getString(R.string.action_stopping);
                break;
            case ACTION_QUICK_STOP:
                message_text = getString(R.string.action_quick_stop);
                break;
            case ACTION_RESUMING:
                message_text = getString(R.string.action_resuming);
                break;
            case ACTION_CANCELLING:
                message_text = getString(R.string.action_cancel);
                break;
            case ACTION_EXITING:
                message_text = getString(R.string.action_exiting);
                break;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_circularconfirm_cancel, (androidx.constraintlayout.widget.ConstraintLayout) parent_view,false);
        dialogBuilder.setView(customView);

        TextView tv = customView.findViewById(R.id.circular_text_message);
        if (tv != null) tv.setText(message_text);
       // dialogBuilder.setTitle(message_text);
        final androidx.wear.widget.CircularProgressLayout circularProgressLayout = customView.findViewById(R.id.circular_progress);
        final AlertDialog dialog = dialogBuilder.create();
		if (circularProgressLayout != null){
            long iConfirmDuration = UserPreferences.getConfirmDuration(this);
            if (iConfirmDuration < 1000) iConfirmDuration = 1000;
            if ((action == ACTION_STOPPING) && (iConfirmDuration < 2000)) iConfirmDuration = 2000;
            if  ((action == ACTION_QUICK_STOP) && (iConfirmDuration < 3000)) iConfirmDuration = 3000;
            circularProgressLayout.setTotalTime(iConfirmDuration);
            circularProgressLayout.setOnTimerFinishedListener(circularProgressLayout1 -> {
                dialog.dismiss();
                // start
                if (action == ACTION_STARTING) {
                    startSession();
                    Bundle bundle = new Bundle();
                    bundle.putInt(LiveFragment.ARG_LIVEPAGE_ACTIVITY_ID, mWorkout.activityID);
                    bundle.putInt(LiveFragment.ARG_LIVEPAGE_COLOR_ID, mActivityColor);
                    bundle.putInt(LiveFragment.ARG_LIVEPAGE_IMAGE_ID, mActivityIcon);
                    bundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUT_ID, mWorkout._id);
                    bundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                    mNavigationController.navigate(R.id.action_global_liveFragment, bundle);
                }
                // end
                if (action == ACTION_STOPPING){
                    //Toast.makeText(getApplicationContext(), "Exit Session", Toast.LENGTH_SHORT).show();
                    stopSession();
                    Bundle argsBundle = new Bundle();
                    argsBundle.putInt(SessionReportFragment.ARG_ICON_ID, mRefTools.getFitnessActivityIconResById(mWorkout.activityID));
                    argsBundle.putInt(SessionReportFragment.ARG_COLOR_ID, mRefTools.getFitnessActivityColorById(mWorkout.activityID));
                    argsBundle.putLong(SessionReportFragment.ARG_SET, mWorkoutSet._id);
                    argsBundle.putLong(SessionReportFragment.ARG_WORKOUT, mWorkout._id);
                    argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                    mNavigationController.navigate(R.id.action_liveFragment_to_sessionReportFragment, argsBundle);

                    //startReportSessionFragment();
                }
                if (action == ACTION_STOP_QUIT){
                    stopSession();
                    quitApp();
                }
                if (action == ACTION_QUICK_STOP){
                    stopSession();
                    Bundle argsBundle = new Bundle();
                    argsBundle.putInt(SessionReportFragment.ARG_ICON_ID, mRefTools.getFitnessActivityIconResById(mWorkout.activityID));
                    argsBundle.putInt(SessionReportFragment.ARG_COLOR_ID, mRefTools.getFitnessActivityColorById(mWorkout.activityID));
                    argsBundle.putLong(SessionReportFragment.ARG_SET, mWorkoutSet._id);
                    argsBundle.putLong(SessionReportFragment.ARG_WORKOUT, mWorkout._id);
                    argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                    mNavigationController.navigate(R.id.action_liveFragment_to_sessionReportFragment, argsBundle);
                }
                // resume
                if (action == ACTION_RESUMING){
                    startSession();
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                }
                if (action == ACTION_EXITING)
                    quitApp();
            });
            circularProgressLayout.setOnClickListener(view -> {
                circularProgressLayout.stopTimer();
                dialog.dismiss();
                if (action == ACTION_QUICK_STOP){
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                }
            });
            circularProgressLayout.startTimer();
        }
        dialog.setCancelable(true);
        dialog.show();

    }

    private void startActivitySession(){
        if (mCurrentSetIndex < 1) { // only starting if this is the first set !
            if (UserPreferences.getConfirmStartSession(this)) {
                String sMsg = "Confirm starting " + mWorkout.activityName;
                mCustomConfirmDialog = CustomConfirmDialog.newInstance(1, sMsg, MainActivity.this);
                mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
            } else {
                final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                if (frameLayout != null) {
                    showAlertDialogConfirm(frameLayout, ACTION_STARTING);
                } else {
                    startSession();
                    Bundle argsBundle = new Bundle();
                    argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_ACTIVITY_ID, mWorkout.activityID);
                    argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_COLOR_ID, mRefTools.getFitnessActivityColorById(mWorkout.activityID));
                    argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_IMAGE_ID, mRefTools.getFitnessActivityIconResById(mWorkout.activityID));
                    argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUT_ID, mWorkout._id);
                    argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                    mNavigationController.navigate(R.id.action_sessionEntryFragment_to_liveFragment, argsBundle);
                }
            }
        }else {
            // set the rest duration for the previous set
            if (mCompletedSets.size() > 0) {
                WorkoutSet set = mCompletedSets.get(mCompletedSets.size() - 1);
                long timeMs = System.currentTimeMillis();
                set.rest_duration = timeMs - set.end;
                mCompletedSets.set(mCompletedSets.size() - 1, set);
            }
            startSession();
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if (mWorkout != null) {
                Bundle argsBundle = new Bundle();
                argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_ACTIVITY_ID, mWorkout.activityID);
                argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_COLOR_ID, mRefTools.getFitnessActivityColorById(mWorkout.activityID));
                argsBundle.putInt(LiveFragment.ARG_LIVEPAGE_IMAGE_ID, mRefTools.getFitnessActivityIconResById(mWorkout.activityID));
                argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUT_ID, mWorkout._id);
                argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                mNavigationController.navigate(R.id.action_sessionEntryFragment_to_liveFragment, argsBundle);
                Log.d(TAG, "starting live bundle " + argsBundle.toString());
            }else
                mNavigationController.navigate(R.id.action_sessionEntryFragment_to_liveFragment);
        }

    }



    private void startCustomList(int selectionType, String defaultValue){
        if ((selectionType == Constants.SELECTION_SETS) || (selectionType == Constants.SELECTION_REPS)
                || (selectionType == Constants.SELECTION_GOAL_DURATION) || (selectionType == Constants.SELECTION_GOAL_STEPS)){
            Integer iPreset = (selectionType == Constants.SELECTION_SETS) ? 3 : 10 ;
            if (defaultValue.length() > 0)
                iPreset = Integer.parseInt(defaultValue);

            mCustomScoreDialogFragment = CustomScoreDialogFragment.newInstance(selectionType,iPreset);
            mCustomScoreDialogFragment.show(fragmentManager, CustomScoreDialogFragment.TAG);
        }else {
            Integer iPreset = 10 ;
            if (defaultValue.length() > 0)
                iPreset = Integer.parseInt(defaultValue);

            mCustomListFragment = CustomListFragment.create(selectionType,  iPreset);

            if (mWorkout != null) mCustomListFragment.setWorkout(mWorkout);
            if (mWorkoutSet != null) mCustomListFragment.setWorkoutSet(mWorkoutSet);

            String sTag = CustomListFragment.TAG;
            if ((selectionType == Constants.SELECTION_FITNESS_ACTIVITY) || (selectionType == Constants.SELECTION_ACTIVITY_BIKE)
                    || (selectionType == Constants.SELECTION_ACTIVITY_GYM) || (selectionType == Constants.SELECTION_ACTIVITY_CARDIO)
                    || (selectionType == Constants.SELECTION_ACTIVITY_SPORT) || (selectionType == Constants.SELECTION_ACTIVITY_RUN)
                    || (selectionType == Constants.SELECTION_ACTIVITY_WATER) || (selectionType == Constants.SELECTION_ACTIVITY_WINTER)
                    || (selectionType == Constants.SELECTION_ACTIVITY_MISC)) {
                mCustomListFragment.setActivityList(mSessionViewModel.getFitnessActivityArrayList());
            }
            if (selectionType == Constants.SELECTION_EXERCISE) {
                sTag = getString(R.string.label_exercise);
                if (mWorkoutSet.bodypartID > 0) {
                    ArrayList<Exercise> shortList = getExercisesByBodypartId(mWorkoutSet.bodypartID);
                    if (shortList.size() > 0) mCustomListFragment.setExerciseList(shortList);
                    else
                        mCustomListFragment.setExerciseList(mSessionViewModel.getExerciseArrayList());
                } else {
                    mCustomListFragment.setExerciseList(mSessionViewModel.getExerciseArrayList());
                }

            }
            if ((selectionType == Constants.SELECTION_ROUTINE) || (selectionType == Constants.SELECTION_WORKOUT_HISTORY) || (selectionType == Constants.SELECTION_TO_DO_SETS) ||
                    (selectionType == Constants.SELECTION_WORKOUT_SET_HISTORY) || (selectionType == Constants.SELECTION_GOOGLE_HISTORY)) {
                sTag = getString(R.string.action_new_workout);
                if (selectionType == Constants.SELECTION_WORKOUT_HISTORY) {
                    ArrayList<Workout> currently = mSessionViewModel.getCompletedWorkouts().getValue();
                    Log.i(TAG, "workout history current was null " + Boolean.toString(currently == null));
                    if (currently == null) currently = mCompletedWorkouts;
                    if (currently != null)
                        mCustomListFragment.setWorkouts(currently);
                    else
                        Toast.makeText(this,"No completed workout history found",Toast.LENGTH_SHORT);
                }
                if (selectionType == Constants.SELECTION_GOOGLE_HISTORY) {
                    ArrayList<Workout> currently = mService.getGSONHelper().getGoogleWorkouts();
                    if (currently != null)
                        mCustomListFragment.setWorkouts(currently);
                    else
                        Toast.makeText(this,"No Google Fit history found",Toast.LENGTH_SHORT);
                }
                if (selectionType == Constants.SELECTION_WORKOUT_SET_HISTORY) {
                    ArrayList<WorkoutSet> currently = mSessionViewModel.getCompletedSets().getValue();
                    if (currently != null)
                        mCustomListFragment.setWorkoutSets(currently);
                    else{
                        if (mCompletedSets != null)
                            mCustomListFragment.setWorkoutSets(mCompletedSets);
                        else
                            Toast.makeText(this,"No completed sets history found",Toast.LENGTH_SHORT);
                    }

                }
                if (selectionType == Constants.SELECTION_TO_DO_SETS) {
                    ArrayList<WorkoutSet> currently = mSessionViewModel.getToDoSets().getValue();
                    if (currently == null) {
                        if (mToDoSets != null)
                            mCustomListFragment.setWorkoutSets(mToDoSets);
                        else{
                            Toast.makeText(this,"No sets to be completed found",Toast.LENGTH_SHORT);
                            Log.e(TAG, "ToDoSets current is null ");
                        }

                    } else {
                        mCustomListFragment.setWorkoutSets(currently);
                    }
                }
                if (selectionType == Constants.SELECTION_ACTIVE_SESSION) {
                    Workout currently = mSavedStateViewModel.getActiveWorkout().getValue();
                    if (currently == null) {
                        if (mWorkout != null)
                            mCustomListFragment.setWorkout(mWorkout);
                        else{
                            Toast.makeText(this,"No active workout found",Toast.LENGTH_SHORT);
                            Log.e(TAG, "getWorkout current is null ");
                        }

                    } else {
                        mCustomListFragment.setWorkout(currently);
                    }
                }
                if (selectionType == Constants.SELECTION_ACTIVE_SET) {
                    WorkoutSet currently = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    if (currently == null) {
                        if (mWorkout != null)
                            mCustomListFragment.setWorkoutSet(mWorkoutSet);
                        else{
                            Toast.makeText(this,"No active set found",Toast.LENGTH_SHORT);
                            Log.e(TAG, "getWorkout current is null ");
                        }

                    } else {
                        mCustomListFragment.setWorkoutSet(currently);
                    }
                }
            }
            if (selectionType == Constants.SELECTION_BODYPART) {
                sTag = getString(R.string.label_bodypart);
                mCustomListFragment.setBodypartList(mSessionViewModel.getBodypartArrayList());
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
            if (selectionType == Constants.SELECTION_TARGET_SHOTS_PER_END) {
                sTag = getString(R.string.label_shoot_per_end);
            }
            mCustomListFragment.setCancelable(true);
            mCustomListFragment.show(fragmentManager, CustomListFragment.TAG);
        }
    }
    /**  CustomListFragment callbacks/interface  **/


    //
    // addToDoSet - called from build on add
    //
    private void addToDoSet(WorkoutSet workoutSet){
        if (!workoutSet.isValid()) return;  // integrity checks

        int iSetOffset; int iSetsToAdd = workoutSet.setCount;
        if (Utilities.isShooting(mWorkout.activityID)) {
            iSetsToAdd = mWorkout.setCount;
            workoutSet.repCount = 0;  // this is the score later!
        }

        long total_duration = 0L; long total_rest_duration = 0L;

        iSetOffset = mToDoSets.size()+1;
        if (iSetsToAdd > 1){
            for (int i=0; (i < iSetsToAdd); i++){
                WorkoutSet set = new WorkoutSet();
                set._id  = workoutSet._id + iSetOffset + i;
                set.activityID = mWorkout.activityID;
                set.activityName = mWorkout.activityName;
                if (workoutSet.exerciseID > 0) {
                    set.exerciseID = workoutSet.exerciseID;
                    set.exerciseName = workoutSet.exerciseName;
                    set.resistance_type = workoutSet.resistance_type;
                    set.weightTotal = workoutSet.weightTotal;
                    set.wattsTotal = workoutSet.wattsTotal;
                }
                if (workoutSet.bodypartID > 0){
                    set.bodypartID = workoutSet.bodypartID;
                }
                set.repCount = workoutSet.repCount;
                set.start = 0;
                set.setCount = iSetOffset + i;
                set.rest_duration = mExpectedRestDuration;
                total_rest_duration += set.rest_duration;
                mToDoSets.add(set);
            }
        }else {
            WorkoutSet set = new WorkoutSet();
            set._id  = workoutSet._id + iSetOffset;
            set.activityID = mWorkout.activityID;
            set.activityName = mWorkout.activityName;
            if (workoutSet.exerciseID > 0) {
                set.exerciseID = workoutSet.exerciseID;
                set.exerciseName = workoutSet.exerciseName;
                set.resistance_type = workoutSet.resistance_type;
                set.weightTotal = workoutSet.weightTotal;
                set.wattsTotal = workoutSet.wattsTotal;
            }
            if (workoutSet.bodypartID > 0){
                set.bodypartID = workoutSet.bodypartID;
            }
            set.repCount = workoutSet.repCount;
            set.start = 0;
            set.setCount = iSetOffset;
            set.rest_duration = mExpectedRestDuration;
            total_rest_duration += set.rest_duration;
            mToDoSets.add(set);
        }
        mSessionViewModel.setToDoWorkoutSets(mToDoSets);
        mWorkout.setCount = mToDoSets.size();
        mWorkout.duration += total_duration;
        mWorkout.rest_duration += total_rest_duration;
        Log.d(TAG, "addToDoSet session: "  + mWorkout.shortText());
        for(WorkoutSet set: mToDoSets){
            Log.d(TAG, "set: " + set.shortText());
        }
    }

    @Override
    public void onSessionEntryRequest(int index, String defaultValue) {
        mWorkout =  mSavedStateViewModel.getActiveWorkout().getValue();
        mWorkoutSet =  mSavedStateViewModel.getActiveWorkoutSet().getValue();

        switch (index){
            case Constants.CHOOSE_ACTIIVITY:
                startCustomList(Constants.SELECTION_FITNESS_ACTIVITY,  defaultValue);
                break;
            case Constants.CHOOSE_BODYPART:
                startCustomList(Constants.SELECTION_BODYPART,  defaultValue);
                break;
            case Constants.CHOOSE_DISTANCE:
                startCustomList(mWorkout.shootFormat.equals("target") ? Constants.SELECTION_TARGET_DISTANCE_TARGET : Constants.SELECTION_TARGET_DISTANCE_FIELD,  defaultValue);
                break;
            case Constants.CHOOSE_ENDS:
                startCustomList(Constants.SELECTION_TARGET_ENDS,  defaultValue);
                break;
            case Constants.CHOOSE_EQUIPMENT:
                startCustomList(Constants.SELECTION_TARGET_EQUIPMENT,  defaultValue);
                break;
            case Constants.CHOOSE_EXERCISE:
                startCustomList(Constants.SELECTION_EXERCISE,  defaultValue);
                break;
            case Constants.CHOOSE_PER_END:
                startCustomList(Constants.SELECTION_TARGET_SHOTS_PER_END, defaultValue);
                break;
            case Constants.CHOOSE_POSSIBLE_SCORE:
                startCustomList(Constants.SELECTION_TARGET_POSSIBLE_SCORE, defaultValue);
                break;
            case Constants.CHOOSE_REPS:
                startCustomList(Constants.SELECTION_REPS, defaultValue);
                break;
            case Constants.CHOOSE_SETS:
                startCustomList(Constants.SELECTION_SETS, defaultValue);
                break;
            case Constants.CHOOSE_TARGET_FIELD:
                startCustomList(Constants.SELECTION_TARGET_FIELD, defaultValue);
                break;
            case Constants.CHOOSE_TARGET_SIZE:
                startCustomList(mWorkout.shootFormat.equals("target") ? Constants.SELECTION_TARGET_TARGET_SIZE_TARGET: Constants.SELECTION_TARGET_TARGET_SIZE_FIELD, defaultValue);
                break;
            case Constants.CHOOSE_WEIGHT:
                WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if ((set != null) && (set.resistance_type == Field.RESISTANCE_TYPE_BODY))
                    startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, defaultValue);
                else
                    if (UserPreferences.getUseKG(this))
                        startCustomList(Constants.SELECTION_WEIGHT_KG, defaultValue);
                    else
                        startCustomList(Constants.SELECTION_WEIGHT_LBS, defaultValue);
                break;
            case Constants.CHOOSE_NEW_EXERCISE:
                displaySpeechRecognizer();
                break;

            case Constants.CHOOSE_CONTINUE_SESSION:
                if (mWorkout != null) {
                    if ((mWorkout.activityID > 0) && (mWorkout._id != 0)) {
                        startActivitySession();
                    }else
                        Toast.makeText(this, R.string.activity_id_not_set, Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(this, R.string.activity_id_not_set, Toast.LENGTH_LONG).show();
                // set the rest duration for the previous set
                if (mCompletedSets.size() > 0 && (mCurrentSetIndex > 1)) {
                    WorkoutSet last_set = mCompletedSets.get(mCompletedSets.size() - 1);
                    long timeMs = System.currentTimeMillis();
                    last_set.rest_duration = timeMs - last_set.end;
                    mCompletedSets.set(mCompletedSets.size() - 1, last_set);
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                }
                break;

            case Constants.CHOOSE_START_SESSION:
                if (mWorkout != null) {
                    if ((mWorkout.activityID > 0) && (mWorkout._id != 0)) {
                        Boolean bBuildClicked = Boolean.parseBoolean(defaultValue);
                        if (!bBuildClicked && ((mWorkoutSet != null) && (mWorkoutSet.isValid()))) {
                            addToDoSet(mWorkoutSet);
                        }
                        startActivitySession();
                    }else
                        Toast.makeText(this, R.string.activity_id_not_set, Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(this, R.string.activity_id_not_set, Toast.LENGTH_LONG).show();
                break;

            case Constants.CHOOSE_BUILD_LONGCLICK:
                startCustomList(Constants.SELECTION_TO_DO_SETS, defaultValue);
                break;

            case Constants.CHOOSE_BUILD_SESSION:
                addToDoSet(mWorkoutSet);
                createWorkoutSet();
                String sTemp = UserPreferences.getLastBodyPartID(this);
                if (sTemp.length() > 0) {
                    mWorkoutSet.bodypartID = Integer.parseInt(sTemp);
                    sTemp = UserPreferences.getLastBodyPartName(this);
                    if (sTemp.length() > 0) mWorkoutSet.bodypartName = sTemp;
                }else{
                    mWorkoutSet.bodypartID = 0;
                    mWorkoutSet.bodypartName = sTemp; // clearing it
                }
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                break;
            case Constants.CHOOSE_GOAL_1:
                startCustomList(Constants.SELECTION_GOAL_DURATION, defaultValue);
                break;
            case Constants.CHOOSE_GOAL_2:
                startCustomList(Constants.SELECTION_GOAL_STEPS, defaultValue);
                break;
        }
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        mCustomConfirmDialog.dismiss();
        mCustomConfirmDialog = null;
        // dismiss ATrackIt
        if (question == 0){
            switch (button){
                case 1:
                    quitApp();
                    break;
                case -1:
                    mNavigationController.navigate(R.id.action_global_homePageFragment);
                    break;
            }
        }
        // confirm start session
        if (question == 1){
            switch (button){
                case 1:
                    startSession();
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                    break;
                case -1:
                    break;
            }
        }
        // confirm stop session
        if (question == 5){
            switch (button) {
                case 1:
                    stopSession();
                    Bundle argsBundle = new Bundle();
                    argsBundle.putInt(SessionReportFragment.ARG_ICON_ID, mRefTools.getFitnessActivityIconResById(mWorkout.activityID));
                    argsBundle.putInt(SessionReportFragment.ARG_COLOR_ID, mRefTools.getFitnessActivityColorById(mWorkout.activityID));
                    argsBundle.putLong(SessionReportFragment.ARG_SET, mWorkoutSet._id);
                    argsBundle.putLong(SessionReportFragment.ARG_WORKOUT, mWorkout._id);
                    argsBundle.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mWorkoutSet._id);
                    mNavigationController.navigate(R.id.action_liveFragment_to_sessionReportFragment, argsBundle);
                    break;
                case -1:
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                    break;
            }
        }
        // confirm stop session and quit
        if (question == 6){
            switch (button) {
                case 1:
                    stopSession();
                    quitApp();
                    break;
                case -1:
                    mNavigationController.navigate(R.id.action_global_liveFragment);
                    break;
            }
        }
    }

    /**
     EndOfSetFragment callbacks - next set/end and exit session.
            **/

    @Override
    public void onEndOfSetRequest(int index, int setIndex, String defaultValue) {
        switch (index){
            case Constants.CHOOSE_ACTIIVITY:
                startCustomList(Constants.SELECTION_FITNESS_ACTIVITY, defaultValue);
                break;
            case Constants.CHOOSE_BODYPART:
                startCustomList(Constants.SELECTION_BODYPART, defaultValue);
                break;
            case Constants.CHOOSE_EXERCISE:
                startCustomList(Constants.SELECTION_EXERCISE, defaultValue);
                break;
            case Constants.CHOOSE_REPS:
                startCustomList(Constants.SELECTION_REPS, defaultValue);
                break;
            case Constants.CHOOSE_SETS:
                startCustomList(Constants.SELECTION_SETS, defaultValue);
                break;
            case Constants.CHOOSE_WEIGHT:
                WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if ((set != null) && (set.resistance_type == Field.RESISTANCE_TYPE_BODY))
                    startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, defaultValue);
                else
                    if (UserPreferences.getUseKG(this))
                        startCustomList(Constants.SELECTION_WEIGHT_KG, defaultValue);
                    else
                        startCustomList(Constants.SELECTION_WEIGHT_LBS, defaultValue);
                break;
            case Constants.CHOOSE_REPEAT_SET:
                if (setIndex <= mToDoSets.size()) {
                    WorkoutSet newSet = mToDoSets.get(setIndex);
                    newSet._id = newSet._id + mToDoSets.size()+1;
                    newSet.start = 0;
                    newSet.end = 0;
                    mToDoSets.add(setIndex, newSet);
                    mSessionViewModel.setToDoWorkoutSets(mToDoSets);
                }else
                    Log.e(TAG, "error setindex for repeat > todo size");
                break;
        }
    }

    @Override
    public void onEndOfSetMethod(int index, int setIndex) {
        long timeMs = System.currentTimeMillis();
        mCurrentSetIndex = (mSavedStateViewModel.getSetIndex().getValue() == null) ? 0 : mSavedStateViewModel.getSetIndex().getValue();
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        switch (index){
            case Constants.CHOOSE_CONTINUE_SESSION:
                if (setIndex == mCurrentSetIndex){
                    completeActiveSet(timeMs);
                }
                // check if unable to continue!
                if (mIsGymWorkout && (++mCurrentSetIndex >= mToDoSets.size())){
                    Toast.makeText(this, R.string.confirm_no_sets_prompt,Toast.LENGTH_LONG).show();
                    return;
                }
                /*
                    INCREMENT THE SET INDEX
                 */
                mCurrentSetIndex += 1;  // next SET

                mCompletedSets = mSessionViewModel.getCompletedSets().getValue();
                // set the rest duration for the previous set
                if ((mCompletedSets != null) && (mCompletedSets.size() > 0)) {
                    WorkoutSet set = mCompletedSets.get(mCompletedSets.size() - 1);
                    timeMs = System.currentTimeMillis();
                    set.rest_duration = timeMs - set.end;
                    mCompletedSets.set(mCompletedSets.size() - 1, set);
                }

                if (mToDoSets.size() <= mCurrentSetIndex) {
                    mWorkoutSet = mToDoSets.get(mCurrentSetIndex - 1);  // array's are zero-based the set count it not!
                    Log.d(TAG, "Picked up NEXT set " + mWorkoutSet.shortText() + " toDo size " + Integer.toString(mToDoSets.size()));
                    // good to go now!
                    mWorkoutSet.setCount = mCurrentSetIndex;  // over-riding original setting mCurrentSetIndex is zero based so adding 1
                    if (mWorkoutSet.activityID != mWorkout.activityID)
                        mWorkoutSet.activityID = mWorkout.activityID;
                    // good to go now!

                    mWorkoutSet.start = timeMs;
                    mExpectedRestDuration = mWorkoutSet.rest_duration;
                }else {
                    mWorkoutSet = null;
                }
                mExpectedRestDuration = 0;
                mSavedStateViewModel.setPauseStart(0L);  // no active pause
                mSavedStateViewModel.setSetIndex(mCurrentSetIndex);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                mSessionViewModel.setCompletedWorkoutSets(mCompletedSets);
                mNavigationController.navigate(R.id.action_global_liveFragment);

                break;
            //TODO: update repeat functionality
            case Constants.CHOOSE_REPEAT_SET:
                if (setIndex <= mToDoSets.size()) {
                    WorkoutSet newSet = mToDoSets.get(setIndex);
                    newSet._id = newSet._id + mToDoSets.size()+1;
                    newSet.start = 0;
                    newSet.end = 0;
                    mToDoSets.add(setIndex, newSet);
                    mSessionViewModel.setToDoWorkoutSets(mToDoSets);
                }else
                    Log.e(TAG, "error setindex for repeat > todo size");
                break;

            case Constants.CHOOSE_ADD_SET:
                Log.d(TAG, "ADD SET");
                break;
        }

    }

    @Override
    public void onEndOfSetExit() {
        if (UserPreferences.getConfirmEndSession(getApplicationContext())){
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(5,getString(R.string.confirm_end_toggle_prompt), MainActivity.this);
            mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
        }else {
            final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
            if (frameLayout != null) {
                showAlertDialogConfirm(frameLayout, ACTION_STOPPING);
            }
        }
    }

    @Override
    public void onSessionReportExit() {
            }



    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
          //  android.util.Log.d(TAG, "onEnterAmbient() " + ambientDetails);
            int currentState = mSavedStateViewModel.getCurrentState().getValue();
            mIsLowBitAmbient =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
            mDoBurnInProtection =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
            /* Clears Handler queue (only needed for updates in active mode). */
            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (mNavigationController.getCurrentDestination() != null) {
                if (mNavigationController.getCurrentDestination().getId() == R.id.homePageFragment) {
                    mHomepageFragment = (HomePageFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mHomepageFragment != null) {
                        mHomepageFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }
                if (mNavigationController.getCurrentDestination().getId() == R.id.liveFragment) {
                    mLiveFragment = (LiveFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mLiveFragment != null) {
                        mLiveFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }
                if (mNavigationController.getCurrentDestination().getId() == R.id.endOfSetFragment) {
                    mEndOfSetFragment = (EndOfSetFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mEndOfSetFragment != null) {
                        mEndOfSetFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }
                if (mNavigationController.getCurrentDestination().getId() == R.id.sessionEntryFragment) {
                    mSessionEntryFragment = (SessionEntryFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mSessionEntryFragment != null) {
                        mSessionEntryFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }
                if (mNavigationController.getCurrentDestination().getId() == R.id.customListFragment) {
                    mCustomListFragment = (CustomListFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mCustomListFragment != null) {
                        mCustomListFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }
                if (mNavigationController.getCurrentDestination().getId() == R.id.customScoreDialogFragment) {
                    mCustomScoreDialogFragment = (CustomScoreDialogFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mCustomScoreDialogFragment != null) {
                        mCustomScoreDialogFragment.onEnterAmbientInFragment(ambientDetails);
                    }
                }

                if (mNavigationController.getCurrentDestination().getId() == R.id.customConfirmDialog) {
                    mCustomConfirmDialog = (CustomConfirmDialog) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (mCustomConfirmDialog != null) {
                        mCustomConfirmDialog.onEnterAmbientInFragment(ambientDetails);
                    }
                }
            }
            refreshDisplayAndSetNextUpdate();

        }
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
            /*
             * If the screen requires burn-in protection, views must be shifted around periodically
             * in ambient mode. To ensure that content isn't shifted off the screen, avoid placing
             * content within 10 pixels of the edge of the screen.
             *
             * Since we're potentially applying negative padding, we have ensured
             * that the containing view is sufficiently padded (see res/layout/activity_main.xml).
             *
             * Activities should also avoid solid white areas to prevent pixel burn-in. Both of
             * these requirements only apply in ambient mode, and only when this property is set
             * to true.
             */
            if (mDoBurnInProtection) {
                int x = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                int y = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.setPadding(x, y, 0, 0);  //TODO: investigate this!
            }
        }
        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            android.util.Log.d(TAG, "onExitAmbient()");
            int currentState = mSavedStateViewModel.getCurrentState().getValue();
            /* Clears out Alarms since they are only used in ambient mode. */
            mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
            /* Reset any random offset applied for burn-in protection. */
            if (mDoBurnInProtection) {
               if (mSwipeDismissFrameLayout != null)  mSwipeDismissFrameLayout.setPadding(0, 0, 0, 0);
            }

            NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (mNavigationController.getCurrentDestination().getId() == R.id.homePageFragment){
                mHomepageFragment = (HomePageFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mHomepageFragment != null) {
                    mHomepageFragment.onExitAmbientInFragment();
                }
            }

            if (mNavigationController.getCurrentDestination().getId() == R.id.liveFragment){
                mLiveFragment = (LiveFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mLiveFragment != null) {
                    mLiveFragment.onExitAmbientInFragment();
                }
            }
            if (mNavigationController.getCurrentDestination().getId() == R.id.endOfSetFragment){
                mEndOfSetFragment = (EndOfSetFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mEndOfSetFragment != null) {
                    mEndOfSetFragment.onExitAmbientInFragment();
                }
            }
            if (mNavigationController.getCurrentDestination().getId() == R.id.sessionEntryFragment){
                mSessionEntryFragment = (SessionEntryFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mSessionEntryFragment != null) {
                    mSessionEntryFragment.onExitAmbientInFragment();
                }
            }
            if (mNavigationController.getCurrentDestination().getId() == R.id.customListFragment){
                mCustomListFragment = (CustomListFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mCustomListFragment != null) {
                    mCustomListFragment.onExitAmbientInFragment();
                }
            }
            if (mNavigationController.getCurrentDestination().getId() == R.id.customScoreDialogFragment){
                mCustomScoreDialogFragment = (CustomScoreDialogFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mCustomScoreDialogFragment != null) {
                    mCustomScoreDialogFragment.onExitAmbientInFragment();
                }
            }
            if (mNavigationController.getCurrentDestination().getId() == R.id.customConfirmDialog){
                mCustomConfirmDialog = (CustomConfirmDialog) navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (mCustomConfirmDialog != null) {
                    mCustomConfirmDialog.onExitAmbientInFragment();
                }
            }
            refreshDisplayAndSetNextUpdate();
        }
    }

    private class MySwipeDismissCallback extends SwipeDismissFrameLayout.Callback {
        @Override
        public void onSwipeStarted(SwipeDismissFrameLayout layout) {
            super.onSwipeStarted(layout);
        }

        @Override
        public void onDismissed(SwipeDismissFrameLayout layout) {
            NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            int iStackCount = navHostFragment.getChildFragmentManager().getBackStackEntryCount();
            if (iStackCount > 1) {
                mNavigationController.popBackStack();
            }else {
                if (mNavigationController.getCurrentDestination().getId() == R.id.homePageFragment) {
                    final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                    if (UserPreferences.getConfirmDismissSession(getApplicationContext())) {
                        mCustomConfirmDialog = CustomConfirmDialog.newInstance(0, getString(R.string.confirm_dismiss_toggle_prompt), MainActivity.this);
                        mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
                    } else
                        if (frameLayout != null)
                            showAlertDialogConfirm(frameLayout, ACTION_EXITING);

                }else{
                    if (mNavigationController.getCurrentDestination().getId() == R.id.liveFragment){
                        if (UserPreferences.getConfirmEndSession(getApplicationContext())) {
                            mCustomConfirmDialog = CustomConfirmDialog.newInstance(6, getString(R.string.confirm_end_toggle_prompt), MainActivity.this);
                            mCustomConfirmDialog.show(fragmentManager, "confirmDialog");
                        } else {
                            final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.main_constraintLayout);
                            if (frameLayout != null) {
                                showAlertDialogConfirm(frameLayout, ACTION_STOP_QUIT);
                            }
                        }
                    }else
                        mNavigationController.navigate(R.id.action_global_homePageFragment);
                }
            }
        }
    }
 
    /** Handler separated into static class to avoid memory leaks. */
    private static class ActiveModeUpdateHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivityWeakReference;

        ActiveModeUpdateHandler(MainActivity reference) {
            mMainActivityWeakReference = new WeakReference<>(reference);
        }
        @Override
        public void handleMessage(Message message) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity != null) {
                switch (message.what) {
                    case MSG_UPDATE_SCREEN:
                        mainActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signInSilent]
/*    private void signInSilent() {
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            task.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        // There's immediate result available.
                        mGoogleAccount = task.getResult();
                        authInProgress = (mGoogleAccount == null);
                        if (!authInProgress) {
                            Log.i(TAG, "silent sign-in SUCCESSFUL " + mGoogleAccount.getDisplayName());
                            String PersonName = mGoogleAccount.getDisplayName();
                            String PersonId = mGoogleAccount.getId();
                            Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                            if (!PersonId.equals(UserPreferences.getLastUserID(getApplicationContext()))) {
                                UserPreferences.setLastUserName(getApplicationContext(), PersonName);
                                UserPreferences.setLastUserID(getApplicationContext(), PersonId);
                                UserPreferences.setLastUserPhotoUri(getApplicationContext(), PersonPhotoUri.toString());
                                Log.i(TAG, "GoogleAccount is " + PersonName);
                                Log.i(TAG, "Photo uri is " + PersonPhotoUri.toString());
                            }
                            if ((mClient == null) || (!mClient.isConnected())) connect();
                        }else
                            signIn();
                    }else {
                        Log.i(TAG, "silent sign-in NOT successful... signing in normally ");
                        signIn();
                    }
                }
            });




    }*/
    // [END signInSilent]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            mGoogleAccount = completedTask.getResult(ApiException.class);
            Log.i(TAG, "handleSignInResult connecting");
            authInProgress = (mGoogleAccount == null);
            if (!authInProgress) {
                String PersonName = mGoogleAccount.getDisplayName();
                String PersonId = mGoogleAccount.getId();
                Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                String sInternal = UserPreferences.getLastUserPhotoInternal(this);

                if (!PersonId.equals(UserPreferences.getLastUserID(this))) {
                    sInternal = "";
                    UserPreferences.setLastUserName(this, PersonName);
                    UserPreferences.setLastUserID(this, PersonId);
                    UserPreferences.setLastUserPhotoUri(this, PersonPhotoUri.toString());
                    UserPreferences.setUserEmail(this, mGoogleAccount.getEmail());
                    Log.i(TAG, "mGoogleAccount is " + PersonName);
                    Log.i(TAG, "Photo uri is " + PersonPhotoUri.toString());
                }
                if (sInternal.length() == 0) {
                    PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                    doImageWork(PersonPhotoUri);
                }
                Intent intent = new Intent(this, FITAPIManager.class);
                intent.putExtra(Constants.LABEL_USER, mGoogleAccount.getEmail());
                Log.d(TAG, "starting FIT Service");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intent);
                } else {
                    this.startService(intent);
                }
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            String sMsg = "signInResult:failed code=" + e.getStatusCode();
            Log.w(TAG, sMsg);
            Toast.makeText(getApplicationContext(),"Sign In Error " + sMsg, Toast.LENGTH_LONG).show();
            authInProgress = false;
           // updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START quitApp]
    private void quitApp() {
        if (mServiceBound){
            if (mService.isClientConnected()) mService.FIT_Disconnect();
            if (mService.mustStopSelf()) mService.StopService();
        }
        finish();
        System.exit(0);
    }

    // [END quitApp]
/*        // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        //TODO: ensure save of state & models
                       quitApp();
                        // [END_EXCLUDE]
                    }
                });

    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
       mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        //TODO: remove state records and files
                        quitApp();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    *//**
     * Checks if user's account has OAuth permission to Fitness API.
     *//*
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(mGoogleAccount, fitnessOptions);
    }

    *//** Launches the Google SignIn activity to request OAuth permission for the user. *//*
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                mGoogleAccount,
                fitnessOptions);
    }*/


    private Bodypart getBodypartById(int id){
        ArrayList<Bodypart> bodypartArrayList = mSessionViewModel.getBodypartArrayList();
        for ( Bodypart bodypart : bodypartArrayList){
            if (bodypart._id == id) return bodypart;
        }
        return null;
    }
    private Exercise getExerciseById(int id){
        ArrayList<Exercise> exerciseArrayList = mSessionViewModel.getExerciseArrayList();
        for ( Exercise exercise : exerciseArrayList){
            if (exercise._id == id) return exercise;
        }
        return null;
    }
    private Exercise getExerciseByName(String name){
        ArrayList<Exercise> exerciseArrayList = mSessionViewModel.getExerciseArrayList();
        for ( Exercise exercise : exerciseArrayList){
            if (exercise.name.equalsIgnoreCase(name)) return exercise;
        }
        return null;
    }
    private FitnessActivity getFitnessActivity(int id){
        ArrayList<FitnessActivity> fitnessActivityArrayList = mSessionViewModel.getFitnessActivityArrayList();
        for (FitnessActivity fitnessActivity : fitnessActivityArrayList){
            if (fitnessActivity._id == id) return fitnessActivity;
        }
        return null;
    }
    private ArrayList<Exercise> getExercisesByBodypartId(int bodypartID){
        ArrayList<Exercise> exerciseArrayList = mSessionViewModel.getExerciseArrayList();
        ArrayList<Exercise> result = new ArrayList<>();
        ArrayList<Exercise> result2 = new ArrayList<>();
        if (exerciseArrayList == null) return result;
        for (Exercise exercise : exerciseArrayList){
            if (bodypartID <= 6){
             switch (bodypartID){
                 case 1:  //upper and lower chest
                     if ((exercise.first_BPID == bodypartID) || (exercise.first_BPID == 7) || (exercise.first_BPID == 8)) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) || (exercise.second_BPID == 7) || (exercise.second_BPID == 8)) result2.add(exercise);
                     break;
                 case 2:
                     if ((exercise.first_BPID == bodypartID) || (exercise.first_BPID == 26) || ((exercise.first_BPID >= 9) && (exercise.first_BPID <= 11))) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) ||(exercise.second_BPID == 26) ||  ((exercise.second_BPID >= 9) && (exercise.second_BPID <= 11))) result2.add(exercise);
                     break;
                 case 3:
                     if ((exercise.first_BPID == bodypartID) || ((exercise.first_BPID >= 13) && (exercise.first_BPID <= 17))) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) || ((exercise.second_BPID >= 13) && (exercise.second_BPID <= 17))) result2.add(exercise);
                     break;
                 case 4:
                     if ((exercise.first_BPID == bodypartID) || ((exercise.first_BPID >= 18) && (exercise.first_BPID <= 20))) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) || ((exercise.second_BPID >= 18) && (exercise.second_BPID <= 20))) result2.add(exercise);
                     break;
                 case 5:
                     if ((exercise.first_BPID == bodypartID) || ((exercise.first_BPID >= 21) && (exercise.first_BPID <= 23))) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) || ((exercise.second_BPID >= 21) && (exercise.second_BPID <= 23))) result2.add(exercise);
                     break;
                 case 6:
                     if ((exercise.first_BPID == bodypartID) || ((exercise.first_BPID >= 24) && (exercise.first_BPID <= 25))) result.add(exercise);
                     if ((exercise.second_BPID == bodypartID) || ((exercise.second_BPID >= 24) && (exercise.second_BPID <= 25))) result2.add(exercise);
                     break;
             }
            }else {
                if (exercise.first_BPID == bodypartID) result.add(exercise);
                if (exercise.second_BPID == bodypartID) result2.add(exercise);
            }
        }
        if (result2.size() > 0) result.addAll(result2);
        return result;
    }
    // Google FIT DataPoint listener
    @Override
    public void onDataPoint(DataPoint dataPoint) {
     //   final StringBuilder dataValue = new StringBuilder();
     //   final StringBuilder pointValue = new StringBuilder();
           // String sDataType = dataPoint.getDataType().getName();
            String sSuggested;

            if (DataType.TYPE_HEART_RATE_BPM.equals(dataPoint.getDataType())) {
                sSuggested =  getString(R.string.label_bpm) + getString(R.string.my_space_string) + dataPoint.getValue(Field.FIELD_BPM).asFloat();
                mMessagesViewModel.addBpmMsg(sSuggested);
            }else
            if (DataType.TYPE_ACTIVITY_SAMPLES.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_CONFIDENCE).asFloat() + "% " + mRefTools.getFitnessActivityTextById(dataPoint.getValue(Field.FIELD_ACTIVITY).asInt());
                Log.v(TAG, "sampled " + sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_MOVE_MINUTES.equals(dataPoint.getDataType())) {
                long moveDuration = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (moveDuration > 0) {
                    sSuggested = Long.toString(TimeUnit.MILLISECONDS.toMinutes(moveDuration));
                    Log.v(TAG, "move mins " + sSuggested);
                    mMessagesViewModel.addOtherMessage(sSuggested);
                }
            }else
            if (DataType.TYPE_WORKOUT_EXERCISE.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_EXERCISE).asString() + " ";
                Log.v(TAG, sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataPoint.getDataType())) {
                sSuggested = getString(R.string.label_steps) + getString(R.string.my_space_string) + dataPoint.getValue(Field.FIELD_STEPS).asInt();
                Log.v(TAG, sSuggested);
                mMessagesViewModel.addStepsMsg(sSuggested);
            }else
            if (DataType.TYPE_POWER_SAMPLE.equals(dataPoint.getDataType())) {
                sSuggested = "watts " + dataPoint.getValue(Field.FIELD_WATTS).asFloat() + " ";
                Log.v(TAG, sSuggested);
                mMessagesViewModel.addOtherMessage(sSuggested);
            }else
            if (DataType.TYPE_LOCATION_SAMPLE.equals(dataPoint.getDataType())) {
                Location loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(dataPoint.getValue(Field.FIELD_LATITUDE).asFloat());
                loc.setLongitude(dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
                onNewLocation(loc);
                sSuggested = "location lat: " + dataPoint.getValue(Field.FIELD_LATITUDE).asFloat() + " long: " + dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
                Log.v(TAG, sSuggested);
                //mMessagesViewModel.addOtherMessage(sSuggested);
            }else{
                String sDataType = dataPoint.getDataType().getName();
                sSuggested = sDataType + ": " + dataPoint.toString();
                Log.i(TAG, "other data point received: " + sSuggested);
            }
            if (mMessagesViewModel.getMessageCount() > 0) mMessagesViewModel.getNextMessage();
    }

    public void addSensorDataListener(DataSource mDataSource)
    {
        SensorRequest request = null;
        int sampleRate; // default

        if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(mDataSource.getDataType())){
            sampleRate = UserPreferences.getStepsSampleRate(this);
            if (sampleRate > 0)
                request = new SensorRequest.Builder()
                .setDataSource(mDataSource)
                .setDataType(mDataSource.getDataType())
                .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                .build();
        }else
            if (DataType.TYPE_HEART_RATE_BPM.equals(mDataSource.getDataType())) {
                sampleRate = UserPreferences.getBPMSampleRate(this);
                if (sampleRate > 0)
                    request = new SensorRequest.Builder()
                    .setDataSource(mDataSource)
                    .setDataType(mDataSource.getDataType())
                    .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                    .build();
            }else
                sampleRate = UserPreferences.getOthersSampleRate(this);
                if (sampleRate > 0)
                    request = new SensorRequest.Builder()
                    .setDataSource(mDataSource)
                    .setDataType(mDataSource.getDataType())
                    .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                    .build();
        if ((sampleRate > 0) && (request != null))
            Fitness.getSensorsClient(this, mGoogleAccount).add(request, this)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Listener registered!");
                                } else {
                                    // intSensorDataListenerCount not added
                                    Log.e(TAG, "Listener not registered.", task.getException());
                                }
                            }
                        });
    }
    public void removeSensorDataListener()
    {
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.getSensorsClient(this, mGoogleAccount)
                .remove(this)
                .addOnCompleteListener(
                        new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if (task.isSuccessful() && task.getResult()) {
                                    Log.i(TAG, "Listener was removed!");
                                } else {
                                    // intSensorDataListenerCount not subtracted
                                    Log.i(TAG, "Listener was not removed.");
                                }
                            }
                        });
    }
    public void listSensorDataSources(final DataType mDataType)
    {
        if (mGoogleAccount != null)
            Fitness.getSensorsClient(this, mGoogleAccount).findDataSources(new DataSourcesRequest.Builder()
                    .setDataTypes(mDataType)
                    .setDataSourceTypes(DataSource.TYPE_DERIVED)
                    .setDataSourceTypes(DataSource.TYPE_RAW)
                    .build()).addOnCompleteListener(new OnCompleteListener<List<DataSource>>() {
                @Override
                public void onComplete(@NonNull Task<List<DataSource>> task) {
                    if (task.isSuccessful()){
                        mDataSourceList.clear();
                        mDataSourceList.addAll(task.getResult());
                        if (mDataSourceList.size() > 0) {
                            for (DataSource dataSource : mDataSourceList) {
                                Log.i(TAG, "Adding sensor listener " + dataSource.toString());
                                addSensorDataListener(dataSource);
                                break;
                            }
                        }else
                            Log.d(TAG, "No sensors for " + mDataType.toString());

                    }
                }
            });

    }

    private void bindLocationListener(){
        if (mLocationListener == null){
            mLocationListener = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult.getLastLocation() != null) {
                        Location loc = locationResult.getLastLocation();
                        Log.d(TAG, "loc lat " + loc.getLatitude() + " lng " + loc.getLongitude());
                        if (!loc.equals(mLocation))
                            onNewLocation(locationResult.getLastLocation());
                    }
                }
            };
        }
        BoundFusedLocationClient.bindFusedLocationListenerIn(this, mLocationListener,getApplicationContext());
    }

    private void bindSensorListener(int iActiveType){
        if (mSensorListener == null){
            mSensorListener = new xSensorListener(iActiveType);
        }
        BoundSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext());
    }

    public class xSensorListener implements SensorEventListener {
        int ActiveType = 0;

        public xSensorListener(int iActiveType){
            ActiveType = iActiveType;
        }

        public int getActiveType(){ return ActiveType;}
        public void setActiveType(int type){ ActiveType = type; return; }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.US);

            switch (iType) {
                case Sensor.TYPE_ACCELEROMETER:
                    Log.d(TAG, "sensor accel " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    Log.d(TAG, "sensor linear accel " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    Log.d(TAG, "sensor gyro " + event.timestamp + " 0: " +event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    Log.d(TAG, "sensor step " + event.timestamp + " " +event.values[0]);
                    break;
                case Sensor.TYPE_HEART_RATE:
                    Log.d(TAG, "sensor bpm " + event.timestamp + " " +event.values[0]);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "sensor changed accuracy " + sensor.getName());
        }
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
            mLocation.set(location);  // so we wont send the same next time!
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            List<Address> addresses = null;
            try {
                addresses = mGeoCoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getAddressLine(0) + "";
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                Log.i(TAG, "New GEO location: " + cityName);
                mMessagesViewModel.addLocationMsg(cityName);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }
}
