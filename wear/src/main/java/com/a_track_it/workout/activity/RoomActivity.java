package com.a_track_it.workout.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
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
import android.graphics.Canvas;
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
import android.os.Message;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.activity.ConfirmationActivity;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.SwipeDismissFrameLayout;
import androidx.wear.widget.drawer.WearableDrawerLayout;
import androidx.wear.widget.drawer.WearableDrawerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.GoalListAdapter;
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
import com.a_track_it.workout.fragment.AmbientInterface;
import com.a_track_it.workout.fragment.CustomAlertDialog;
import com.a_track_it.workout.fragment.CustomConfirmDialog;
import com.a_track_it.workout.fragment.CustomListFragment;
import com.a_track_it.workout.fragment.CustomScoreDialogFragment;
import com.a_track_it.workout.fragment.FragmentInterface;
import com.a_track_it.workout.fragment.ICustomConfirmDialog;
import com.a_track_it.workout.fragment.RoomFragment;
import com.a_track_it.workout.fragment.SettingsDialog;
import com.a_track_it.workout.service.CustomIntentReceiver;
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
import com.google.android.gms.tasks.Task;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.a_track_it.workout.common.Constants.*;
public class RoomActivity extends AppCompatActivity implements AmbientModeSupport.AmbientCallbackProvider,
        CustomListFragment.OnCustomListItemSelectedListener,
        CustomScoreDialogFragment.onCustomScoreSelected,
        ICustomConfirmDialog, FragmentInterface, MessageClient.OnMessageReceivedListener {
    private static final String LOG_TAG = RoomActivity.class.getSimpleName();
    private static final int LIC_CHECK_MIN_DAYS = 7;
    private static final int SKU_CHECK_MIN_DAYS = 3;
    private static final int REQUEST_SIGN_IN = 5002;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 5004;
    private static final int REQUEST_MIC_REQUEST_CODE = 5005;
    private static final int REQUEST_RECORDING_PERMISSION_CODE = 5007;
    private static final int REQUEST_IMAGE_PICKER = 5010;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOG = 5008;
    private static final int PERMISSION_REQUEST_BODY_SENSORS = 5009;
    private static final int PERMISSION_REQUEST_LOCATION = 5006;
    private static final int RC_REQUEST_RECORDING_AND_CONTINUE_SUBSCRIPTION = 5020;
    private static final int RC_REQUEST_FIT_PERMISSION_AND_CONTINUE = 5021;
    public static final int REQUEST_SPLASH_CODE = 55350;
    private static final int MSG_UPDATE_SCREEN = 55340;
    private static final long MIN_CLICK_THRESHOLD = 1000L;
    /** Milliseconds between updates based on state. */
    private static final long ACTIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long AMBIENT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long MIN_REP_INTERVAL_MS = TimeUnit.MILLISECONDS.toMillis(750);
    /** Action for updating the display in ambient mode, per our custom refresh cycle. */
    private static final String AMBIENT_UPDATE_ACTION = "com.a_track_it.workout.action.AMBIENT_UPDATE";
    private static final int GOAL_TYPE_DURATION = 1;
    private static final int GOAL_TYPE_STEPS = 2;
    private static final int GOAL_TYPE_BPM = 3;
    private static final String ANALYTICS_SIGNOUT = "SignOut";
    private static final int ALARM_AMBIENT_CODE = 55341;
    private static final int ALARM_PENDING_SYNC_CODE = 55343;
    private static final int ALARM_PHONE_SYNC_CODE = 55344;
    private static final int ALARM_NETWORK_CHECK_CODE = 55345;
    private static final int ALARM_EXACT_TIME_CODE = 55346;
    /**
     * Since the handler (used in active mode) can't wake up the processor when the device is in
     * ambient mode and undocked, we use an Alarm to cover ambient mode updates when we need them
     * more frequently than every minute. Remember, if getting updates once a minute in ambient mode
     * is enough, you can do away with the Alarm code and just rely on the onUpdateAmbient()
     * callback.
     */
    // Firebase instance variables
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private AlarmManager mAmbientUpdateAlarmManager;
    private AlarmManager mPhoneAlarmManager;
    private AlarmManager mNetworkAlarmManager;
    private PendingIntent mAmbientUpdatePendingIntent;
    private BroadcastReceiver mAmbientUpdateBroadcastReceiver;
    private ActivityRecognitionClient activityRecognitionClient;
    boolean mDoBurnInProtection;
    boolean mIsLowBitAmbient;
    private boolean refreshInProgress;
    private boolean bUseKG = false;
    private WorkManager mWorkManager;
    private boolean authInProgress = false;
    private GoogleSignInAccount mGoogleAccount;
    private Handler mHandler;
    private HandlerThread mSensorThread = new HandlerThread("sensorThread");
    private HandlerThread mWearHandlerThread = new HandlerThread("wearThread");
    private LocationCallback mLocationListener;
    private SensorEventListener mSensorListener;
    private OnDataPointListener mDataPointListener;
    private final xCapabilityListener mCapabilityListener = new xCapabilityListener();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private ActivityResultLauncher<Intent> splashActivityResultLauncher;
    private Runnable sdtRunnable;
    ScheduledFuture<?> detectorHandle0 = null;
    ScheduledFuture<?> sdtHandle = null;
    long lastInteraction;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private ReferencesTools mReferenceTools;
    private Geocoder mGeoCoder;
    private CustomConfirmDialog confirmDialog;
    private long mLastClickTime = 0L;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    final private Calendar mCalendar = Calendar.getInstance();
    String sUserID = ATRACKIT_EMPTY;
    String sDeviceID = ATRACKIT_EMPTY;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private List<WorkoutSet> mToDoList;
    private Location mLocation;
    private ConstraintLayout mConstraintLayout;
    private NavController mNavigationController;
    private WearableNavigationDrawerView mWearableNavigationDrawer;
   // private WearableActionDrawerView mWearableActionDrawer;
    private SettingsNavigationAdapter mSettingsNavAdapter;
    private SwipeDismissFrameLayout mSwipeDismissFrameLayout;

    private ArrayList<DailyCounter> BPM_counterList = new ArrayList<>();
    private ArrayList<DailyCounter> Step_counterList = new ArrayList<>();
    private TennisGame tennisGame;
   // private String transcriptionNodeId = null;
    private Set<Node> mPhoneNodesWithApp = new HashSet<>();
    private String phoneNodeId = null;
    private MediaPlayer mMediaPlayer;
    private Intent mStartupIntent;
   // private xPlaybackListener mPlaybackListener = new xPlaybackListener();
/*    private class xPlaybackListener implements SoundRecorder.OnVoicePlaybackStateChangedListener{
        @Override
        public void onPlaybackStopped() {
                //update UI playback has finished
        }
    }*/

    /**
     * Ambient mode controller attached to this display. Used by the Activity to see if it is in
     * ambient mode.
     */
    private AmbientModeSupport.AmbientController mAmbientController;
    /**
     * This custom handler is used for updates in "Active" mode. We use a separate static class to
     * help us avoid memory leaks.
     */
    private final Handler mActiveModeUpdateHandler = new ActiveModeUpdateHandler(RoomActivity.this);
    /** Handler separated into static class to avoid memory leaks. */
    private static class ActiveModeUpdateHandler extends Handler {
        private final WeakReference<RoomActivity> mMainActivityWeakReference;

        ActiveModeUpdateHandler(RoomActivity reference) {
            mMainActivityWeakReference = new WeakReference<>(reference);
        }
        @Override
        public void handleMessage(Message message) {
            RoomActivity roomActivity = mMainActivityWeakReference.get();
            if (roomActivity != null) {
                switch (message.what) {
                    case MSG_UPDATE_SCREEN:
                        roomActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }
    private final WearableNavigationDrawerView.OnItemSelectedListener mNavigationDrawItemSelectedListener = pos -> {
        Log.d(LOG_TAG, "nav draw item clicked " + pos);
        Context context = getApplicationContext();
        String sUserId = appPrefs.getLastUserID();
       // String sDeviceId = appPrefs1.getDeviceID();
        switch (pos) {
            case 0:
                // settings
                try {
                    if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment) {
                        mNavigationController.navigate(R.id.action_homeRoomFragment_to_settingsDialog);
                    }
                }catch (NullPointerException ne){
                    ne.printStackTrace();
                }
                break;
            case 1:
                boolean bUseLocation = appPrefs.getUseLocation();
                bUseLocation = !bUseLocation; //swap it!
                appPrefs.setUseLocation(bUseLocation);
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, bUseLocation);
                mMessagesViewModel.setUseLocation(bUseLocation);
                broadcastToast(getString(R.string.setting_updated));
                mSettingsNavAdapter.notifyDataSetChanged();
                if (bUseLocation) {
                  //  long lastLocation = appPrefs1.getLastLocationUpdate();
                   // long timeMs2 = System.currentTimeMillis();
                    broadcastToast(getString(R.string.label_location_enabled));
                    BoundFusedLocationClient.standardInterval();
                    //if ((timeMs2 - lastLocation) > TimeUnit.MINUTES.toMillis(5L))
                    BoundFusedLocationClient.doUpdate();
                    if (activityRecognitionClient == null) bindRecognitionListener();
                    Data.Builder builder = new Data.Builder();
                    builder.putString(KEY_FIT_USER, appPrefs.getLastUserID());
                    builder.putInt(KEY_FIT_TYPE, 1); // location refresh
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(CurrentUserDailyTotalsWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    if (mWorkManager == null)
                        mWorkManager = WorkManager.getInstance(context);
                    mWorkManager.enqueue(workRequest);
                }else {
                    broadcastToast(getString(R.string.label_location_disabled));
                    BoundFusedLocationClient.clearInterval();
                }
                //if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) new TaskSendPhoneSettings(0, sUserId).run();  // send
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
                restoreWorkoutVariables();
                if (navHostFragment.getNavController().getCurrentDestination().getId() == R.id.homeFragment) {
                    try {
                        for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                            if (frag.isVisible() && (frag instanceof RoomFragment) && (mWorkout != null)) {
                                new Handler(Looper.getMainLooper()).post(((RoomFragment) frag)::refreshPersonalImage);
                            }
                        }
                    } catch (NullPointerException ne) {
                        FirebaseCrashlytics.getInstance().recordException(ne);
                        Log.e(LOG_TAG, "null pointer image refresh " + ne.getMessage());
                      //  FirebaseCrash
                    }
                }
                break;
            case 2: // cloud history
                startCustomList(SELECTION_MONTHS, Integer.toString(mCalendar.get(Calendar.MONTH)), sUserId);
                break;
            case 3: // open
                showOpenDialog();
                break;
            case 4:  // credits
                showCreditsDialog();
                break;
        }
    };

    private final class SettingsNavigationAdapter
            extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        private final Context mContext;

        public SettingsNavigationAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return  5;
        }

        @Override
        public String getItemText(int pos) {
            String sRet = "";
            boolean bUseLocation = appPrefs.getUseLocation();
            switch (pos){
                case 0:
                    sRet = getString(R.string.nav_setting);
                    break;
                case 1:
                    if (bUseLocation)
                        sRet = getString(R.string.label_location_enabled);
                    else
                        sRet = getString(R.string.label_location_disabled);
                    break;
                case 2:
                    sRet = getString(R.string.nav_fav);
                    break;
                case 3:
                    sRet = getString(R.string.nav_open);
                    break;
                case 4:
                    sRet = getString(R.string.nav_credits);
                    break;
            }
            return sRet;
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            String sPackageName;
            sPackageName = getPackageName();
            int drawableNavigationIconId;
            Drawable drawable = null;
            boolean bUseLocation = appPrefs.getUseLocation();
            switch (pos){
                case 0:
                    drawableNavigationIconId = getResources().getIdentifier("ic_settings_white", Constants.ATRACKIT_DRAWABLE, sPackageName);
                    drawable= AppCompatResources.getDrawable(mContext, drawableNavigationIconId);
                    break;
                case 1:
                    if (bUseLocation)
                        drawableNavigationIconId = getResources().getIdentifier("ic_location_enabled_white", Constants.ATRACKIT_DRAWABLE, sPackageName);
                    else
                        drawableNavigationIconId = getResources().getIdentifier("ic_location_disabled_white", Constants.ATRACKIT_DRAWABLE, sPackageName);
                    drawable = AppCompatResources.getDrawable(mContext, drawableNavigationIconId);
                    Utilities.setColorFilter(drawable,ContextCompat.getColor(mContext, android.R.color.white));

                    break;
                case 2:
                    drawableNavigationIconId = getResources().getIdentifier("ic_heart_add", Constants.ATRACKIT_DRAWABLE, sPackageName);
                    drawable= AppCompatResources.getDrawable(mContext, drawableNavigationIconId);
                    drawable.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ContextCompat.getColor(mContext,android.R.color.white), BlendModeCompat.SRC_IN));
                    break;
                case 3:
                    drawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_folder_open_white);
/*                    if (!UserPreferences.getUseFirebase(getApplicationContext())){
                        drawable = getResources().getDrawable(R.drawable.ic_cloud_warning_white, mContext.getTheme());
                        // Convert image to gray scale for ambient mode.
                        ColorMatrix matrix = new ColorMatrix();
                        matrix.setSaturation(0);
                        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                        drawable.setColorFilter(filter);
                    }else
                        drawable = getResources().getDrawable(R.drawable.ic_cloud_sync_white, mContext.getTheme());*/
                    break;
                case 4:
                    drawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_list_white);
            }
            return drawable;
        }
    }
/*    MenuItem.OnMenuItemClickListener mActionMenuItemClickListener = item -> {
        broadcastToast("action menu item clicked " + item.getTitle());
        mWearableNavigationDrawer.getController().closeDrawer();
        return false;
    };*/
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
            mIsLowBitAmbient = ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
            mDoBurnInProtection = ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);

            /* Clears Handler queue (only needed for updates in active mode). */
            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            try {
                if (mNavigationController.getCurrentDestination() != null) {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
                    for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                        if (frag.isVisible() && (frag instanceof AmbientInterface)) {
                            ((AmbientInterface) frag).onEnterAmbientInFragment(ambientDetails);
                        }
                    }
                }
                refreshDisplayAndSetNextUpdate();
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.w(LOG_TAG, e.getMessage());
            }
            // Log.d(LOG_TAG, "onEnterAmbient() " + ambientDetails);

        }
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
            refreshDisplayAndSetNextUpdate();
        }
        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            try{
                mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
                /* Clears out Alarms since they are only used in ambient mode. */
                if (mNavigationController.getCurrentDestination() != null) {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
                    for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                        if (frag.isVisible() && (frag instanceof AmbientInterface)) {
                            ((AmbientInterface) frag).onExitAmbientInFragment();
                        }
                    }
                }
                refreshDisplayAndSetNextUpdate();
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.w(LOG_TAG, e.getMessage());
            }
        }
    }
    private class MySwipeDismissCallback extends SwipeDismissFrameLayout.Callback {
        @Override
        public void onSwipeStarted(SwipeDismissFrameLayout layout) {
            super.onSwipeStarted(layout);
        }

        @Override
        public void onDismissed(SwipeDismissFrameLayout layout) {
            NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
            if ((navHostFragment == null) || (mNavigationController == null)) return;
            try{
                String sUserId =  appPrefs.getLastUserID();
                int iStackCount = navHostFragment.getChildFragmentManager().getBackStackEntryCount();
                int state = mSavedStateViewModel.getState();
                if ((iStackCount > 0) && (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment)) {
                    if (mNavigationController.getCurrentDestination().getId() == R.id.settingsDialog){
                        Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                        refreshIntent.putExtra(KEY_FIT_USER, sUserId);
                        refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(refreshIntent);
                    }
                    if (mNavigationController.getCurrentDestination().getId() == R.id.entryRoomFragment){
                        Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                        refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                        refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                        refreshIntent.putExtra(KEY_FIT_USER, sUserId);
                        refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(refreshIntent);
                    }
                    mNavigationController.popBackStack();
                }else {
                    if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserId);
                    if ((sUserId.length() > 0) &&  userPrefs.getConfirmExitApp()) {
                        if ((state == WORKOUT_LIVE) || (state == WORKOUT_PAUSED)
                                || (state == WORKOUT_CALL_TO_LINE))
                            showAlertDialogConfirm(Constants.ACTION_STOP_QUIT,null);
                        else
                            showAlertDialogConfirm(Constants.ACTION_EXITING,null);
                    }else
                        quitApp();
                }
            } catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG,"onDismissed " + e.getMessage());
            }

        }
    }
    private AppIntentReceiver mIntentReceiver = new AppIntentReceiver();

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
    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 200){
                        String sTemp = "";
                        String sUserId;
                        if (resultData.containsKey(KEY_FIT_USER))
                            sUserId = resultData.getString(KEY_FIT_USER);
                        else
                            sUserId= appPrefs.getLastUserID();

                        //Log.d(LOG_TAG, "result rec " + resultData.toString());
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
                                long lastDetected = lastSDT.lastActivityType;
                                int lastDetectedType = lastSDT.activityType;
                                if (iType != lastDetectedType || (lastDetected == 0)) {
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
                                                showAlertDialogConfirm(Constants.ACTION_STOPPING,null);
                                            else
                                                sessionStop();
                                        }
                                    }
                                }
                            }
                            if (mMessagesViewModel != null) mMessagesViewModel.addActivityMsg(sTemp);
                        }
                    }

                    if (resultData.containsKey(INTENT_DAILY) && resultData.containsKey(KEY_FIT_ACTION)){
                        mMessagesViewModel.setWorkInProgress(false);
                        mMessagesViewModel.setWorkType(0);
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
                            int state = Utilities.currentWorkoutState(mWorkout);
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
                                if (phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
                                if (phoneNodeId.length() != 0)
                                    sendMessage(phoneNodeId, Constants.MESSAGE_PATH_PHONE, dataMap);
                            }
                        }
                        startDailySummarySyncAlarm();
                    }
                }
                else {
                    Log.w(LOG_TAG, "receiver " + resultCode);
                    if (resultCode == 201 || resultCode == 400){  // busy.retry or account is expired
                        int actionInt = resultData.containsKey(Constants.KEY_FIT_ACTION) ? resultData.getInt(Constants.KEY_FIT_ACTION) : mMessagesViewModel.getWorkType();
                        mMessagesViewModel.setWorkInProgress(false);
                        mMessagesViewModel.setWorkType(0);
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
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
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

                super.onReceiveResult(resultCode, resultData);
            }
        };
    private CustomIntentReceiver mCustomIntentReceiver = new CustomIntentReceiver(mResultReceiver);

    final private ResultReceiver mFitResultReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            int returnAction = 0;
            final int successIndicator = (resultData.containsKey(KEY_FIT_VALUE)) ? resultData.getInt(KEY_FIT_VALUE) : 0;
            mMessagesViewModel.setWorkInProgress(false);
            if (resultCode == 200){
                mMessagesViewModel.setWorkType(0);
                if (resultData.containsKey(Constants.KEY_FIT_ACTION)) {
                    String sUserId = resultData.getString(KEY_FIT_USER);
                    long timeMs = System.currentTimeMillis();
                    returnAction = resultData.getInt(Constants.KEY_FIT_ACTION);
                    if (returnAction == Constants.TASK_ACTION_ACT_SEGMENT) {
                        if (resultData.containsKey(WorkoutSet.class.getSimpleName())) {
                            WorkoutSet s = resultData.getParcelable(WorkoutSet.class.getSimpleName());
                            broadcastToast("segment added " + mReferenceTools.workoutSetShortText(s));
                                /*if (s.last_sync > 0) {
                                    mSessionViewModel.updateWorkoutSet(s);
                                }*/
                        }
                    }
                    if (returnAction == Constants.TASK_ACTION_EXER_SEGMENT) {
                        if (resultData.containsKey(WorkoutSet.class.getSimpleName())) {
                            WorkoutSet s = resultData.getParcelable(WorkoutSet.class.getSimpleName());
                            broadcastToast("set added " + mReferenceTools.workoutSetShortText(s));
                               /* if (s.last_sync > 0) {
                                    mSessionViewModel.updateWorkoutSet(s);
                                }*/
                        }
                    }
                    if (returnAction == Constants.TASK_ACTION_INSERT_HISTORY) {
                        Workout w = resultData.getParcelable(Workout.class.getSimpleName());
                        if (w == null) return;
                        if (w.start == 0 || w.end == 0) return;
                        // clean-up sets removing not started
                        broadcastToast(getString(R.string.action_saved));
                        List<OneTimeWorkRequest> requestList = new ArrayList<>();
                        Data.Builder builder = new Data.Builder();
                        builder.putString(KEY_FIT_USER, w.userID);
                        builder.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                        builder.putLong(KEY_FIT_WORKOUTID, w._id);
                        OneTimeWorkRequest workRequest =
                                new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                        .setInputData(builder.build())
                                        .build();
                        requestList.add(workRequest);
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
                                        .setInitialDelay(20, TimeUnit.SECONDS)
                                        .setConstraints(constraints).addTag(LOG_TAG)
                                        .build();
                        requestList.add(oneTimeWorkRequest);
                        if (mWorkManager == null) mWorkManager = WorkManager.getInstance(getApplicationContext());
                        mWorkManager.enqueue(requestList).getState().observe(RoomActivity.this, new Observer<Operation.State>() {
                            @Override
                            public void onChanged(Operation.State state) {
                                if (state instanceof Operation.State.SUCCESS) {
                                    mSessionViewModel.updateLastUpdate(w.userID,w.deviceID, w._id,OBJECT_TYPE_WORKOUT,2);
                                    broadcastToast(getString(R.string.action_report_completed));
                                }

                            }
                        });
                    }
                    if ((returnAction == Constants.TASK_ACTION_STOP_SESSION)
                            || (returnAction == Constants.TASK_ACTION_START_SESSION)) {
                        if (resultData.containsKey(Workout.class.getSimpleName())) {
                            Workout w = resultData.getParcelable(Workout.class.getSimpleName());
                            if (returnAction == Constants.TASK_ACTION_START_SESSION)
                                broadcastToast("started " + mReferenceTools.workoutShortText(w));
                            else
                                broadcastToast("stopped " + mReferenceTools.workoutShortText(w));

                            if (returnAction==Constants.TASK_ACTION_STOP_SESSION &&  w.last_sync > 0) {
                                List<OneTimeWorkRequest> requestList = new ArrayList<>();
                                Data.Builder builder = new Data.Builder();
                                builder.putString(KEY_FIT_USER, w.userID);
                                builder.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                builder.putLong(KEY_FIT_WORKOUTID, w._id);
                                OneTimeWorkRequest workRequest =
                                        new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                .setInputData(builder.build())
                                                .build();
                                requestList.add(workRequest);
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
                                requestList.add(oneTimeWorkRequest);
                                if (mWorkManager == null)
                                    mWorkManager = WorkManager.getInstance(getApplicationContext());
                                mWorkManager.enqueue(requestList).getState().observe(RoomActivity.this, new Observer<Operation.State>() {
                                    @Override
                                    public void onChanged(Operation.State state) {
                                        if (state instanceof Operation.State.SUCCESS) {
                                            mSessionViewModel.updateLastUpdate(w.userID, w.deviceID, w._id, OBJECT_TYPE_WORKOUT, 2);
                                            broadcastToast(getString(R.string.action_report_completed));
                                        }

                                    }
                                });
                            }
                        }
                    }
                    if (returnAction == Constants.TASK_ACTION_READ_HISTORY) {
                        String sKey = UserDailyTotals.class.getSimpleName() + "_list";
                        if (resultData.containsKey(sKey)) {
                            ArrayList<UserDailyTotals> udtList = resultData.getParcelableArrayList(sKey);
                            for (UserDailyTotals udt : udtList) {
                                Log.w(LOG_TAG, udt.toString());
                            }
                        }
                    }
                    if (returnAction == Constants.TASK_ACTION_READ_BPM) {
                        String sKey = DataType.AGGREGATE_HEART_RATE_SUMMARY.getName();
                        if (resultData.containsKey(sKey)) {
                            ArrayList<Bundle> bpmList = resultData.getParcelableArrayList(sKey);
                            for (Bundle bpmBundle : bpmList) {

                            }
                        }
                    }
                    if (returnAction == Constants.TASK_ACTION_READ_GOALS) {
                        for (int i = 0; i < 3; i++) {
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
                    }
                    if (returnAction == FitSyncJobIntentService.READ_SEGMENT_DETAILS){
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
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY,  0, ATRACKIT_EMPTY ,sUserID,sDeviceID);
                            }
                        }
                    }
                    if (returnAction == TASK_ACTION_SYNC_WORKOUT){
                        if (resultData.containsKey(KEY_LIST_SETS)) doPendingExercisesCheck(sUserID);
                        if (successIndicator > 0)
                            broadcastToast("Sync completed");
                        else
                            broadcastToast("Sync incomplete - rescheduled");
                        appPrefs.setLastSync(timeMs);
                        DateTuple dtSets = mSessionViewModel.getWorkoutSetUnSyncCount(sUserID, sDeviceID);
                        if (dtSets != null && dtSets.sync_count > 0){
                            Log.e(LOG_TAG, "TASK_ACTION_SYNC_WORKOUT " + successIndicator + " found unsync sets" + dtSets.sync_count);
                            startCloudPendingSyncAlarm();
                        }
                    }
                    if (returnAction == TASK_ACTION_RECORD_START){
                        if (successIndicator > 0) broadcastToast("Recording started"); else  broadcastToast("Recording failed to start");
                    }
                    if (returnAction == TASK_ACTION_RECORD_END){
                        if (successIndicator > 0) broadcastToast("Recording stop"); else  broadcastToast("Recording failed to stop");
                    }
                }else{
                    String sKey = Workout.class.getSimpleName();
                    String sTitle;
                    if (resultData.containsKey(sKey)) {
                        Workout routine = resultData.getParcelable(sKey);
                        ArrayList<Workout> workouts = new ArrayList<>();
                        workouts.add(routine);
                        mSessionViewModel.setWorkoutList(workouts);
                        sTitle = getString(R.string.label_active_session);
                        CustomListFragment  customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY,  0, sTitle,sUserID,sDeviceID);
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
                            sTitle = getString(R.string.label_active_session);
                            CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY, 0, sTitle,sUserID,sDeviceID);
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
                            sTitle = getString(R.string.label_sets);
                            CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sTitle,sUserID,sDeviceID);
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
                                sTitle = getString(R.string.label_sets);
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sTitle,sUserID,sDeviceID);
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
                Log.i(LOG_TAG, "actionResultReceiver receiver " + resultCode);
                if (resultCode == 201 || resultCode == 400){
                    int actionInt = resultData.containsKey(Constants.KEY_FIT_ACTION) ? resultData.getInt(Constants.KEY_FIT_ACTION) : mMessagesViewModel.getWorkType();
                    mMessagesViewModel.setWorkType(0);
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
                        if (rr != null)
                            newIntent.putExtra(Constants.KEY_RESULT, rr);
                        else
                            newIntent.putExtra(Constants.KEY_RESULT, mFitResultReceiver);
                        GoogleSignInAccount gsa = resultData.getParcelable(KEY_PAYLOAD);
                        if (gsa == null || gsa.isExpired() && !authInProgress){
                            authInProgress = true;
                            Log.e(LOG_TAG, "about to silent signIn action  " + actionInt);
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(Constants.CLIENT_ID)
                                    .requestEmail().build();
                            // Build a GoogleSignInClient with the options specified by gso.
                            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
                            Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
                            signInAccountTask.addOnCompleteListener(task -> {
                                authInProgress = false;
                                if (task.isSuccessful()){
                                    Log.e(LOG_TAG, "SUCCESS silent signIn action  " + actionInt);
                                    GoogleSignInAccount acct = task.getResult();
                                    newIntent.putExtra(Constants.KEY_PAYLOAD,acct);
                                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), newIntent);
                                }else
                                    Log.e(LOG_TAG, "FAILED silent signIn action  " + actionInt);
                            });
                        }else {
                            newIntent.putExtra(Constants.KEY_PAYLOAD, gsa);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(LOG_TAG, "fitSyncJob restarting 1 action  " + actionInt);
                                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), newIntent);
                                }
                            }, 5000);
                        }
                    }
                    else{
                        Log.e(LOG_TAG, "fitSyncJob action OFFLINE " + actionInt);
                        startNetworkCheckAlarm(getApplicationContext());
                        broadcastToast(getString(R.string.action_offline));
                    }
                }
            }
        }
    };
    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        appPrefs = ApplicationPreferences.getPreferences(context);
        long lastLic = appPrefs.getLastLicCheck();
        long timeMs = System.currentTimeMillis();
        mStartupIntent = getIntent();
        boolean noIntent = ((mStartupIntent == null) || (mStartupIntent.getAction() == null));
        if (!noIntent){
            if (mStartupIntent.getAction() != null && mStartupIntent.getAction().equals(Intent.ACTION_MAIN)){
                if (mStartupIntent.hasExtra(SplasherActivity.ARG_ACTION)){
                    noIntent = false;
                }else
                    noIntent = true;
            }
        }
        mCalendar.setTimeInMillis(timeMs);
        mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(context);
        if (!appPrefs.getAppSetupCompleted()) {
            if (!mReferenceTools.isNetworkConnected()) {
                doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null, null);
                return;
            }
            else
                if ((TimeUnit.MILLISECONDS.toDays(timeMs - lastLic) >= LIC_CHECK_MIN_DAYS)) {
                    Intent mySplashIntent = new Intent(context, SplasherActivity.class);
                    mySplashIntent.putExtra(KEY_FIT_USER, ((mGoogleAccount != null) ? mGoogleAccount.getId() : ATRACKIT_EMPTY));
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    mySplashIntent.putExtra(SplasherActivity.ARG_ACTION, INTENT_PERMISSION_POLICY);
                    startActivity(mySplashIntent);
                    finish();
                    return;
                }
        }
        setContentView(R.layout.activity_room_home);
        try {
            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_ID, RoomActivity.class.getSimpleName());
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, params);
            }
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            if (mGoogleAccount != null) mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }
        catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG,"initialise analytics failed " + e.getMessage());
        }

        // Enables Always-on
        // Enables Ambient mode.
        mHandler = new Handler(getMainLooper());
        mSensorThread.start();
        mWearHandlerThread.start();
        mAmbientController = AmbientModeSupport.attach(this);
        mAmbientUpdateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mNetworkAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mPhoneAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mWorkManager = WorkManager.getInstance(context);
        mSwipeDismissFrameLayout = findViewById(R.id.swipe_layout);
        if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.addCallback(new MySwipeDismissCallback());
        mConstraintLayout = findViewById(R.id.wear_constraintLayout);
        mNavigationController = Navigation.findNavController(RoomActivity.this, R.id.nav_host_room_fragment);
        mNavigationController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence sLabel = destination.getLabel();
            if (TextUtils.equals(sLabel,"home_layout")){
                int state = (mSavedStateViewModel != null) ? mSavedStateViewModel.getState() : WORKOUT_INVALID;
                if ((state == WORKOUT_LIVE) || (state == WORKOUT_PAUSED)) {
                    if (mWearableNavigationDrawer != null) mWearableNavigationDrawer.getController().closeDrawer();
                    if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.setEnabled(false);
                }else {
                    if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.setEnabled(true);
                }
            }else{
                if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.setEnabled(false);
                if (mWearableNavigationDrawer != null) mWearableNavigationDrawer.getController().closeDrawer();
            }
        });
        /*
         * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
         * on an interval which we've define.
         */
        Intent ambientUpdateIntent = new Intent(AMBIENT_UPDATE_ACTION);

        mAmbientUpdatePendingIntent =
                PendingIntent.getBroadcast(context, ALARM_AMBIENT_CODE, ambientUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
         * An anonymous broadcast receiver which will receive ambient update requests and trigger
         * display refresh.
         */
        mAmbientUpdateBroadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        refreshDisplayAndSetNextUpdate();
                    }
                };
        this.mGeoCoder = new Geocoder(context, Locale.getDefault());

        mSavedStateViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(getApplication(), RoomActivity.this)).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(context.getApplicationContext());
        mSessionViewModel = new ViewModelProvider(RoomActivity.this, factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(RoomActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.setPhoneAvailable(false);
        WearableDrawerLayout mWearableDrawerLayout = findViewById(R.id.drawer_layout);
        mWearableDrawerLayout.setDrawerStateCallback(new WearableDrawerLayout.DrawerStateCallback() {
            @Override
            public void onDrawerOpened(WearableDrawerLayout layout, WearableDrawerView drawerView) {
                super.onDrawerOpened(layout, drawerView);
            }

            @Override
            public void onDrawerClosed(WearableDrawerLayout layout, WearableDrawerView drawerView) {
                super.onDrawerClosed(layout, drawerView);
                //Log.d(LOG_TAG, "Draw Closed");
            }
        });
        // Top Navigation Drawer
        mWearableNavigationDrawer = (WearableNavigationDrawerView) findViewById(R.id.top_navigation_drawer);
        mSettingsNavAdapter = new SettingsNavigationAdapter(getApplicationContext());
        mWearableNavigationDrawer.setAdapter(mSettingsNavAdapter);
        // Peeks navigation drawer on the top.
        mWearableNavigationDrawer.getController().peekDrawer();
        mWearableNavigationDrawer.addOnItemSelectedListener(mNavigationDrawItemSelectedListener);

        // Bottom Action Drawer
       // mWearableActionDrawer = (WearableActionDrawerView) findViewById(R.id.bottom_action_drawer);
        //mWearableActionDrawer.setOnMenuItemClickListener(mActionMenuItemClickListener);
        splashActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    mMessagesViewModel.setWorkInProgress(false);
                    mMessagesViewModel.setWorkType(0);
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            String sAction = data.getStringExtra(SplasherActivity.ARG_ACTION);
                            String sUserId = data.getStringExtra(KEY_FIT_USER);
                            int resultFlag = data.getIntExtra(KEY_FIT_TYPE, 0);
                            String sLastId = appPrefs.getLastUserID();
                            long lLastLogin = appPrefs.getLastUserLogIn();
                            long timeNowMs = System.currentTimeMillis();
                            if ((mGoogleAccount == null)
                                    || (ATRACKIT_ATRACKIT_CLASS.equals(sAction))) {
                                if (ATRACKIT_ATRACKIT_CLASS.equals(sAction) ||
                                        (sLastId.equals(sUserId) && (TimeUnit.MILLISECONDS.toSeconds(timeNowMs - lLastLogin) < 10))) {
                                    if (mGoogleAccount == null)
                                        signInSilent();
                                    else {
                                        String setupAction = checkApplicationSetup();
                                        if (setupAction.length() > 0) {
                                            sessionSetCurrentState(WORKOUT_SETUP);
                                            startSplashActivityForResult(setupAction);
                                        }
                                    }
                                }
                                else
                                    signIn();
                            }else {
                                String setupAction = checkApplicationSetup();
                                if (setupAction.length() == 0) {
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
                                        // check returning from type to action the next stage of setup
                                        if (sAction.equals(INTENT_PERMISSION_DEVICE) && (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))) {
                                            int positiveFlag = data.getIntExtra(Constants.KEY_FIT_TYPE, 0);
                                            userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, (positiveFlag > 0));
                                            if (positiveFlag > 0) {
                                                mMessagesViewModel.setNodeDisplayName(appPrefs.getLastNodeName());
                                                new TaskSendPhoneSettings(1, sUserId).run(); // request
                                            } else {
                                                mMessagesViewModel.setPhoneAvailable(false);
                                                appPrefs.setLastNodeSync(0);
                                                appPrefs.setLastNodeID(ATRACKIT_EMPTY);
                                                appPrefs.setLastNodeName(ATRACKIT_EMPTY);
                                            }

                                        }
                                        if (sAction.equals(INTENT_PERMISSION_HEIGHT)) {
                                            onCustomItemSelected(Constants.SELECTION_ACTIVITY_GYM, WORKOUT_TYPE_STRENGTH,
                                                    mReferenceTools.getFitnessActivityTextById(WORKOUT_TYPE_STRENGTH), mReferenceTools.getFitnessActivityIconResById(WORKOUT_TYPE_STRENGTH),
                                                    mReferenceTools.getFitnessActivityIdentifierById(WORKOUT_TYPE_STRENGTH));
                                        }
                                        if (sAction.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                                            userPrefs.setPrefByLabel(USER_PREF_STORAGE, (resultFlag == 1));
                                            if (resultFlag == 1 && mGoogleAccount != null) doImageWork(mGoogleAccount.getPhotoUrl(), 0);
                                        }else
                                            if (appPrefs.getDailySyncInterval() > 0 && !hasOAuthPermission(0)){
                                                mMessagesViewModel.setWorkType(Constants.TASK_ACTION_DAILY_SUMMARY);
                                                requestOAuthPermission(0);
                                            }
                                            else{
                                                Log.e(LOG_TAG, "return from Splash about to ACTIVE_LOGIN");
                                                Intent newIntent = new Intent(INTENT_ACTIVE_LOGIN);
                                                newIntent.putExtra(KEY_FIT_USER, sUserId);
                                                newIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                                                newIntent.putExtra(KEY_FIT_VALUE, lLastLogin);
                                                handleIntent(newIntent);
                                            }
                                    } else
                                        signIn();
                                }
                                else
                                  startSplashActivityForResult(setupAction);                                }

                        }else quitApp();
                    }else quitApp();
                });

        mSavedStateViewModel.getCurrentState().observe(RoomActivity.this, currentState -> {
            try {
                boolean bSetup = appPrefs.getAppSetupCompleted();
                int previousState;
                ColorStateList colorStateList;
                int bgColor;
                Log.e(LOG_TAG, "OBSERVE currentSTATE " + currentState);
                if ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED)){
                    mWearableNavigationDrawer.getController().closeDrawer();
                    mWearableNavigationDrawer.setIsLocked((currentState == WORKOUT_LIVE));
                    colorStateList = getColorStateList(R.color.primaryLightColor);
                    bgColor = getColor(R.color.primaryLightColor);
                    if (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment) {
                        mNavigationController.popBackStack();
                    }
                }
                else{
                    mWearableNavigationDrawer.setIsLocked(false);
                    if (currentState == WORKOUT_COMPLETED){
                        colorStateList = getColorStateList(R.color.secondaryDarkColor);
                        bgColor = getColor(R.color.secondaryDarkColor);
                        if (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment) {
                            mNavigationController.popBackStack();
                        }
                    }else{
                        colorStateList = getColorStateList(R.color.primaryDarkColor);
                        bgColor = getColor(R.color.primaryDarkColor);
                    }
                }
                final ColorStateList cl = colorStateList;
                final int bg = bgColor;
                runOnUiThread(() -> {
                    mConstraintLayout.setBackgroundTintList(cl);
                    mSwipeDismissFrameLayout.setBackgroundColor(bg);
                    //mWearableNavigationDrawer.setBackgroundColor(bg);
                });
                if (currentState == WORKOUT_SETUP || !bSetup){
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
                String sPersonID = (mGoogleAccount != null) ? mGoogleAccount.getId() : ATRACKIT_EMPTY;
                switch (currentState) {
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
                        if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment)
                            doHomeFragmentRefresh();

                        break;
                    case WORKOUT_CALL_TO_LINE:
                        if (appPrefs.getUseSensors() && Utilities.hasSensorDevicesPermission(getApplicationContext())) {
                            if ((userPrefs != null) && userPrefs.getReadSessionsPermissions() && (appPrefs.getUseSensors())) {
                                if (BoundSensorManager.isInitialised() && BoundSensorManager.getCount(0) > 0) BoundSensorManager.doReset(0);
                                if ((appPrefs.getStepsSensorCount() > 0) && (BoundSensorManager.getCount(1) == 0)) bindSensorListener(1,0);
                                if ((appPrefs.getBPMSensorCount() > 0) && (BoundSensorManager.getCount(2) == 0)) bindSensorListener(2,0);
                            }
                            bindAccelSensorListener();
                        }
                        break;
                    case WORKOUT_LIVE:
                    case WORKOUT_RESUMED:
                    case WORKOUT_PAUSED:
                        if (currentState == WORKOUT_PAUSED){
                            if (mWorkout != null && mWorkout.activityID != null) {
                                if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)
                                        || Utilities.isAquatic(mWorkout.activityID)) {
                                    destroyAccelSensorListener();
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
                            if (mWorkout != null && mWorkout.activityID != null){
                                if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)
                                        || Utilities.isAquatic(mWorkout.activityID)) {
                                    bindAccelSensorListener();
                                    if (mSavedStateViewModel.getIsGym() && (mGoogleAccount != null)) doRegisterDetectionService(mGoogleAccount);
                                }
                                if (mMessagesViewModel.getUseLocation().getValue() != null && (mMessagesViewModel.getUseLocation().getValue())) {
                                    if (!BoundFusedLocationClient.isInitialised()) bindLocationListener();
                                    if (mReferenceTools.getActivityHighlyMobileById(mWorkout.activityID) > 0)
                                        BoundFusedLocationClient.customInterval(TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(5));
                                    else
                                        BoundFusedLocationClient.standardInterval();
                                }
                            }
                        }
                        break;
                    case WORKOUT_COMPLETED:
                        if (mSavedStateViewModel.getIsGym() && (mGoogleAccount != null)) doUnRegisterDetectionService(mGoogleAccount);
                        BoundSensorManager.doReset(0);
                        BoundSensorManager.doReset(1);
                        BoundSensorManager.doReset(2);
                        BoundFitnessSensorManager.doReset();
                        if (mMessagesViewModel.getUseLocation().getValue() != null && (mMessagesViewModel.getUseLocation().getValue())) {
                            BoundFusedLocationClient.clearInterval();
                            BoundFusedLocationClient.standardInterval();
                        }
                        if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment)
                            doHomeFragmentRefresh();
                        startDailySummarySyncAlarm();
                        break;
                }

            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                String sMsg = e.getMessage();
                if (sMsg != null) Log.i(LOG_TAG, sMsg);
            }
        });
        mSessionViewModel.liveAllConfigs(Constants.ATRACKIT_ATRACKIT_CLASS, ATRACKIT_SETUP).observe(this, configurations -> {
            if ((configurations == null) || (configurations.size() < 1)) {
                // WORKOUT_SETUP
                if (mWorkout == null) createWorkout(appPrefs.getLastUserID(), appPrefs.getDeviceID());
                if ((mWorkout.start != -1) || (mWorkout.end != -1)){
                    mWorkout.start = -1; mWorkout.end = -1;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    if (appPrefs.getAppSetupCompleted()) appPrefs.setAppSetupCompleted(false);
                    sessionSetCurrentState(WORKOUT_SETUP);
                }
            }
        });
        mMessagesViewModel.getLiveIntent().observe(RoomActivity.this, new Observer<Intent>() {
            @Override
            public void onChanged(Intent intent) {
                if (intent == null) return;
                final String intentAction = intent.getAction();
                final Context ctx = getApplicationContext();
                Log.e(LOG_TAG, "observe liveIntent " + intentAction);
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
                            runOnUiThread(() -> {
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
                            });
                        }
                        break;

                    case Intent.ACTION_SHUTDOWN :
                        final Intent quitIntent = new Intent(INTENT_QUIT_APP);
                        mHandler.post(() ->handleIntent(quitIntent));

                        break;
                    default:
                        handleIntent(intent);
                }
            }
        });
        mMessagesViewModel.getUseLocation().observe(RoomActivity.this,aBoolean -> mSettingsNavAdapter.notifyDataSetChanged());
        mMessagesViewModel.getSpokenMsg().observe(RoomActivity.this, s -> {
            if ((s == null) || (s.length()==0)) return;
            String sTarget = mSavedStateViewModel.getSpeechTarget();
            String sUserId = appPrefs.getLastUserID();
            broadcastToast(s);
            int fragID = mNavigationController.getCurrentDestination().getId();
            if (fragID == R.id.entryRoomFragment){
                if (sTarget.equals(Constants.TARGET_EXERCISE_NAME) ) {
                    List<Exercise> list = mSessionViewModel.getExercisesLike(s);
                    if ((list == null) || (list.size() == 0)){
                        // prompt to create new exercise
                    }else
                        if (mWorkoutSet != null){
                            Exercise exercise = list.get(0);
                            if (exercise != null) {
                                if  ((userPrefs == null) && (sUserId.length() > 0))
                                    userPrefs = UserPreferences.getPreferences(getApplicationContext(),sUserId);
                                int defReps = ((userPrefs != null) ? userPrefs.getDefaultNewReps() : 10);
                                int defSets = ((userPrefs != null) ? userPrefs.getDefaultNewSets() : 3);
                                if (exercise.resistanceType != null)
                                    mWorkoutSet.resistance_type = exercise.resistanceType;
                                   mSavedStateViewModel.setRepsDefault((exercise.lastReps > 0) ? exercise.lastReps :  defReps);
                                    mSavedStateViewModel.setSetsDefault((exercise.lastSets > 0) ? exercise.lastSets : defSets);
                                if (mWorkoutSet.weightTotal == null || mWorkoutSet.weightTotal == 0)
                                           mWorkoutSet.weightTotal = (exercise.lastSelectedWeight > 0) ? exercise.lastSelectedWeight : 10F;
                                    if ((exercise.first_BPID != null) && (exercise.first_BPID > 0)) {
                                        mWorkoutSet.bodypartID = exercise.first_BPID;
                                        mWorkoutSet.bodypartName = exercise.first_BPName;
                                        Bodypart bodypart = mSessionViewModel.getBodypartById(exercise.first_BPID);
                                        mWorkoutSet.regionID = bodypart.regionID;
                                        mWorkoutSet.regionName = bodypart.regionName;
                                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);

                                        // }
                                    }
                                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                            }
                        }

                }
                if (sTarget.equals(Constants.TARGET_ROUTINE_NAME) && (mWorkout != null)){
                    List<Workout> sessionNames = mSessionViewModel.getWorkoutByName(mWorkout.userID, sTarget);
                    if ((sessionNames== null) || (sessionNames.size() == 0)){
                        // prompt confirm name for new session..
                        mWorkoutMeta.description = s;
                        Log.d(LOG_TAG,"meta update "+ s);
                        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                        mWorkout.name = s;
                        mWorkout.parentID = -1L;
                        mWorkout.start = 0; mWorkout.end = 0;
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                    }else {
                        // prompt to load new session
                        broadcastToast("a routine already exists with that name");
                    }
                }
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
            mWorkout.start = -1; mWorkout.end = -1; // setup mode
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            sessionSetCurrentState(WORKOUT_SETUP);
            IInitialActivityCallback setupCallback = () -> {
                broadcastToast("reference files setup has completed");
                Boolean bAvail = Utilities.hasMicrophone(RoomActivity.this);
                appPrefs.setMicrophoneAvail(bAvail);
                if (bAvail)
                    broadcastToast("microphone is available");
                bAvail = Utilities.hasSpeaker(RoomActivity.this);
                appPrefs.setSpeakerAvail(bAvail);
                if (bAvail)
                    broadcastToast("speaker is available");
                if (userPrefs != null) {
                    userPrefs.setUseKG(bUseKG);
                    userPrefs.setPrefByLabel(USER_PREF_USE_AUDIO, bAvail);
                    bAvail = Utilities.hasVibration(RoomActivity.this);
                    userPrefs.setPrefByLabel(USER_PREF_USE_VIBRATE, bAvail);
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, true);
                }
                String sAction = checkApplicationSetup();
                startSplashActivityForResult(sAction);
            };
            AsyncSetupTask setupTask = new AsyncSetupTask(setupCallback, context);
            setupTask.execute();
        }
        else{
            // check if permissions are setup install could still be needed.
            String sSetup = checkApplicationSetup();
            if (sSetup.length() != 0){
                appPrefs.setAppSetupCompleted(false);
                createNotificationChannels(); // setup notification manager and channels if needed
                Bundle bundle = new Bundle();
                bundle.putString(INTENT_SETUP, "false");
                sendNotification(Constants.INTENT_SETUP, bundle);
                mWorkout = new Workout();
                mWorkout._id = (System.currentTimeMillis());
                mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                mWorkout.start = -1; mWorkout.end = -1; // setup mode !
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                sessionSetCurrentState(WORKOUT_SETUP);
                startSplashActivityForResult(sSetup);
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
                        && (!mStartupIntent.hasExtra(SplasherActivity.ARG_ACTION))) {
                    if (appPrefs.getLastUserLogIn() > 0 && ((timeMs - appPrefs.getLastUserLogIn()) < TimeUnit.HOURS.toMillis(24)))
                        signInSilent();
                }
            }
            if (!noIntent && (mStartupIntent != null)){ // has an intent!
                handleIntent(mStartupIntent);
                mStartupIntent = null;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Context context = getApplicationContext();
        if ((context == null) || (sUserID == null) || (sUserID.length() == 0)) Log.i(LOG_TAG, "UserId NOT SET onSaveInstanceState");
        else {
            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserID);
            if (userPrefs != null) {
                outState.putInt(Constants.KEY_USE_KG,(userPrefs.getUseKG() ? 1 : 0));
                outState.putString(KEY_FIT_DEVICE_ID,sDeviceID);
                outState.putString(KEY_FIT_USER,sUserID);
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
    public void onUserInteraction(){
        long previousInteraction = lastInteraction;
        lastInteraction = System.currentTimeMillis();
        if (previousInteraction > 0)
            Log.w(RoomActivity.class.getSimpleName(), "onUserInteraction called inactive " + Utilities.getDurationBreakdown(lastInteraction - previousInteraction));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(mWearHandlerThread.getLooper()).build();
        Wearable.getCapabilityClient(RoomActivity.this, options).addListener(mCapabilityListener, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
        Wearable.getMessageClient(RoomActivity.this).addListener(RoomActivity.this);
        setupListenersAndIntents(getApplicationContext());

    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "onStop");
        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(mWearHandlerThread.getLooper()).build();
        Wearable.getCapabilityClient(RoomActivity.this,options).removeListener(mCapabilityListener);
        Wearable.getMessageClient(RoomActivity.this).removeListener(this);
        if (mCustomIntentReceiver.isRegistered()) {
            RoomActivity.this.unregisterReceiver(mCustomIntentReceiver);
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
        Log.e(LOG_TAG, "onPause");
        if (mAmbientUpdateBroadcastReceiver != null) {
            unregisterReceiver(mAmbientUpdateBroadcastReceiver);
            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG_TAG, "onResume");
        IntentFilter filter = new IntentFilter(AMBIENT_UPDATE_ACTION);
        registerReceiver(mAmbientUpdateBroadcastReceiver, filter);
        refreshDisplayAndSetNextUpdate();
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
    protected void onDestroy() {
        try {
            Log.e(LOG_TAG, "onDestroy ");
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            BoundFusedLocationClient.destroyListener();
            destroyRecognitionListener();
            if (mSensorThread != null) mSensorThread.quitSafely();
            if (mWearHandlerThread != null) mWearHandlerThread.quitSafely();
            if (mCustomIntentReceiver.isRegistered()) {  // using this flag for both receivers
                RoomActivity.this.unregisterReceiver(mCustomIntentReceiver);
                mCustomIntentReceiver.setRegistered(false);
                if (mIntentReceiver != null) this.unregisterReceiver(mIntentReceiver);
            }
            if (mAmbientUpdatePendingIntent != null) mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
            if (mActiveModeUpdateHandler != null) {
                mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            }

        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "onDestroy " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        if (mIntentReceiver == null) setupListenersAndIntents(getApplicationContext());  // ensure intents are received
        switch (requestCode) {
            case REQUEST_SPLASH_CODE:
                if (resultCode != RESULT_OK)
                    quitApp();
                else{
                    String sAction = data.getStringExtra(SplasherActivity.ARG_ACTION);
                    String sUserId = data.getStringExtra(KEY_FIT_USER);
                    int resultFlag = data.getIntExtra(KEY_FIT_TYPE,0);
                    if ((mGoogleAccount == null) || (appPrefs.getLastUserID().length() == 0)
                        || (ATRACKIT_ATRACKIT_CLASS.equals(sAction))) {
                        if (ATRACKIT_ATRACKIT_CLASS.equals(sAction) && (sUserId != null && sUserId.length() > 0)) {
                            signInSilent();
                        }else
                            signIn();
                    }else {
                        String setupAction = checkApplicationSetup();
                        if (setupAction.length() == 0) {
                            cancelNotification(INTENT_SETUP);
                            mSettingsNavAdapter.notifyDataSetChanged();
                            // clear setups
                            mMessagesViewModel.addCurrentMsg(ATRACKIT_EMPTY);
                            if (!appPrefs.getAppSetupCompleted()) {
                                appPrefs.setAppSetupCompleted(true);
                                broadcastToast(getString(R.string.label_setup_complete));
                            }
                            long timeMs = System.currentTimeMillis();
                            if (((mGoogleAccount == null) || (mGoogleAccount.getId() == null) || (mGoogleAccount.isExpired()))
                                    && (!data.hasExtra(SplasherActivity.ARG_ACTION))) {
                                if (appPrefs.getLastUserLogIn() > 0 && ((timeMs - appPrefs.getLastUserLogIn()) < TimeUnit.HOURS.toMillis(24)))
                                    signInSilent();
                                else {
                                    if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserId);
                                    // check returning from type to action the next stage of setup
                                    if (sAction.equals(INTENT_PERMISSION_DEVICE) && (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))) {
                                        int positiveFlag = data.getIntExtra(Constants.KEY_FIT_TYPE, 0);
                                        if (positiveFlag > 0) {
                                            mMessagesViewModel.setNodeDisplayName(appPrefs.getLastNodeName());
                                            new TaskSendPhoneSettings(1, sUserId).run(); // request
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
                                    if (sAction.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                                        userPrefs.setPrefByLabel(USER_PREF_STORAGE, (resultFlag == 1));
                                        if (resultFlag == 1 && mGoogleAccount != null) doImageWork(mGoogleAccount.getPhotoUrl(), 0);
                                    }
                                    if (appPrefs.getDailySyncInterval() > 0 && !hasOAuthPermission(0)){
                                        mMessagesViewModel.setWorkType(Constants.TASK_ACTION_DAILY_SUMMARY);
                                        requestOAuthPermission(0);
                                    }
                                }
                            }
                        } else
                            startSplashActivityForResult(setupAction);

                    }
                }
                break;
            case REQUEST_IMAGE_PICKER:
                // Here we need to check if the activity that was triggers was the Image Gallery.
                // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
                // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
                if (resultCode == RESULT_OK && data != null) {
                    // Let's read picked image data - its URI
                    Uri pickedImage = data.getData();
                    // Let's read picked image path using content resolver
                    String[] filePath = { MediaStore.Images.Media._ID };
                    Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

                    // Do something with the bitmap


                    // At the end remember to close the cursor or you will end with the RuntimeException!
                    cursor.close();
                }
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
                            sUserID = mGoogleAccount.getId();
                            appPrefs.setLastUserID(sUserID);
                            mSavedStateViewModel.setUserIDLive(sUserID);
                            userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                            onSignIn(false);
                           // doHomeFragmentRefresh();
                            final String idToken = mGoogleAccount.getIdToken();
                            if ((idToken != null) && (idToken.length() > 0)) {
                                Log.w(LOG_TAG,"token: " + idToken);
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
            case REQUEST_OAUTH_REQUEST_CODE:
                authInProgress =false;
                if (resultCode == Activity.RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result != null) {
                        if (mGoogleAccount == null) mGoogleAccount = result.getSignInAccount();
                        if (mGoogleAccount != null && (mGoogleAccount.getId().equals(sUserID))) {
                            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(), mGoogleAccount.getId());
                            int requestType = mSavedStateViewModel.getColorID();
                            if (requestType == 0) userPrefs.setReadDailyPermissions(true);
                            if (requestType == 1) userPrefs.setReadSessionsPermissions(true);
                            if (requestType == 2) userPrefs.setReadSessionsPermissions(true);
                            int workType = mMessagesViewModel.getWorkType();
                            if (workType != 0){
                                if (workType == Constants.TASK_ACTION_DAILY_SUMMARY) doDailySummaryJob();
                                else doAsyncGoogleFitAction(workType,mWorkout,mWorkoutSet,mWorkoutMeta);
                            }
                        }else
                            signIn();
                    } else
                        signIn();

                }else
                    quitApp();
                break;
            case REQUEST_MIC_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    final String spokenText = (results != null) ? results.get(0) : Constants.ATRACKIT_EMPTY;
                    if (spokenText.length() > 0) {
                        if (mMessagesViewModel != null){
                            mMessagesViewModel.addSpokenMsg(spokenText);
                        }
                    }
                }
                break;
            case Constants.REQUEST_SIGNIN_SYNC:
                broadcastToast("sign-in for SYNC");
                doAlertDialogMessage("sign-in for SYNC");
                startCloudPendingSyncAlarm();
                break;
            case REQUEST_SIGNIN_DAILY:
                broadcastToast("sign-in for Daily");
                doAlertDialogMessage("sign-in for DAILY");
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
                    mWorkManager.enqueue(oneTimeWorkRequest);
                    mWorkManager.beginWith(oneTimeWorkRequest).getWorkInfosLiveData().observe(this, workInfos -> {
                        if (workInfos.size() > 0) {
                            WorkInfo workInfo = workInfos.get(0);
                            boolean finished = workInfo.getState().isFinished();
                            if (!finished) {
                                Log.d(LOG_TAG, "recording changed not finished");
                                //showWorkInProgress();
                            } else {
                                broadcastToast("live tracking started");
                            }
                        }
                    });
                }
                break;
            case RC_REQUEST_FIT_PERMISSION_AND_CONTINUE:
                if (resultCode == Activity.RESULT_OK){
                    int workType = mMessagesViewModel.getWorkType();
                    if (workType > 0 && mWorkout != null)
                        doAsyncGoogleFitAction(workType,mWorkout,mWorkoutSet, mWorkoutMeta);
                    if (workType == 0)
                        doDailySummaryJob();
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
                    mSettingsNavAdapter.notifyDataSetChanged();
                } else {
                    broadcastToast(getString(R.string.action_no_location));
                    appPrefs.setUseLocation(false);
                    mMessagesViewModel.setUseLocation(false);
                    mSettingsNavAdapter.notifyDataSetChanged();
                }
            }else{
                broadcastToast(getString(R.string.action_no_location));
                appPrefs.setUseLocation(false);
                mMessagesViewModel.setUseLocation(false);
                mSettingsNavAdapter.notifyDataSetChanged();
            }
            mSettingsNavAdapter.notifyDataSetChanged();
        }
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
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
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (hasOAuthPermission(0))
                        if (appPrefs.getUseLocation()) {
                            bindLocationListener();
                        }
                    else
                        requestOAuthPermission(0);
                }else{
                    doConfirmDialog(Constants.QUESTION_ACT_RECOG,getString(R.string.recog_permission_reason),null,null);
                    return;
                }
            }else {
                appPrefs.setUseSensors(false);
                sMsg = getString(R.string.no_permission_okay);
            }
            broadcastToast(sMsg);
        }
        if (requestCode == REQUEST_RECORDING_PERMISSION_CODE){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String sMsg = getResources().getString(R.string.recording_permission_granted);
                broadcastToast(sMsg);
                try {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    // Start the activity, the intent will be populated with the speech text
                    ActivityCompat.startActivityForResult(RoomActivity.this,intent,REQUEST_MIC_REQUEST_CODE, null);
                }catch (ActivityNotFoundException anf){
                    broadcastToast(getString(R.string.action_nospeech_activity));
                }
            }
        }
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOG){
            if (userPrefs != null) userPrefs.setPrefByLabel(Constants.USER_PREF_RECOG_ASKED, true);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, true);
                if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED)) {
                    if (hasOAuthPermission(0)) {
                        onSignIn(false);
                    }else
                        requestOAuthPermission(0);
                } else {
                    doConfirmDialog(Constants.QUESTION_SENSORS, getString(R.string.sensor_permission_reason), null,null);
                    return;
                }
            } else {
                String sMsg = getString(R.string.no_permission_okay);
                broadcastToast(sMsg);
                if (userPrefs != null) userPrefs.setPrefByLabel(USER_PREF_RECOG, false);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
        String messagePath = messageEvent.getPath();
        String host = messageEvent.getSourceNodeId();
        Log.i(LOG_TAG,"onMessageReceived() A message from app was received:"
                + messageEvent.getRequestId()
                + " " + host + " "
                + messageEvent.getPath());
        boolean bPreviously = mMessagesViewModel.hasPhone();
        // Check to see if the message is data update
        if (messagePath.equals(Constants.MESSAGE_PATH_WEAR)) {
            Log.e(LOG_TAG, "Message received MESSAGE_PATH_WEAR");
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            int requestType = dataMap.getInt(KEY_COMM_TYPE);
            String sUserID = dataMap.getString(Constants.KEY_FIT_USER);
            if ((sUserID == null) || (sUserID.length() == 0)) return;
            if (!this.sUserID.equals(sUserID)) return;
            if (userPrefs == null || !userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) return;
            if (!bPreviously)
                mMessagesViewModel.setPhoneAvailable(true);

            if (requestType == Constants.COMM_TYPE_REQUEST_INFO){
                int type = dataMap.getInt(Constants.KEY_FIT_TYPE,0);
                Log.e(LOG_TAG, "Message received COMM_TYPE_REQUEST_INFO " + type + " " + sUserID);
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
                    new TaskSendSensoryInfo(getApplicationContext(), host,sUserID, sDeviceID, appPrefs.getLastUserLogIn(),
                            mSavedStateViewModel.getState(),sdt,udt).run();
                }
                if (type == 3) {
                    SensorDailyTotals sdt = mSessionViewModel.getTopSensorDailyTotal(sUserID);
                    UserDailyTotals udt = mSessionViewModel.getTopUserDailyTotal(sUserID);
                    new TaskSendSensoryInfo(getApplicationContext(),phoneNodeId,sUserID, sDeviceID, appPrefs.getLastUserLogIn(),
                            mSavedStateViewModel.getState(),sdt,udt).run();
                }
                if (type == 2){  // response to request
                    long lastLogin = dataMap.getLong(KEY_FIT_VALUE);
                    String sDevice = dataMap.getString(KEY_FIT_DEVICE_ID);
                    long lastSync = dataMap.getLong(Constants.KEY_FIT_TIME);
                    String sMsg = "Last login " + Utilities.getTimeDateString(lastLogin);
                    if (lastSync > 0) sMsg += "\n Last Sync"  + Utilities.getTimeDateString(lastSync);
                    broadcastToast(sMsg);
                }
                startPhonePendingSyncAlarm();
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
                    sVal = String.format(Locale.getDefault(),SINGLE_FLOAT, fVal);
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
                            if (sdt != null){
                                sdt.deviceBPM = fVal;
                                sdt.lastDeviceBPM = previousTime;
                                updatedSDT = true;
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
                        detectedID = dataMap.getInt(KEY_FIT_TYPE);
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + detectedID + " " + sVal);
                    }else
                        Log.e(LOG_TAG, "INTENT_RECOG dataMap " + sVal);

                    if (((sVal != null) && (sVal.length() > 0)) || (detectedID != -3)){
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
            if (requestType == Constants.COMM_TYPE_SENSOR_UPDATE){
                int iSensorType = dataMap.getInt(KEY_FIT_TYPE);
                String sVal = dataMap.getString(KEY_FIT_VALUE);
                Log.w(LOG_TAG, "phone sensor update " + iSensorType + " val " + sVal);
                switch (iSensorType){
                    case Sensor.TYPE_HEART_RATE:
                        mMessagesViewModel.addDevice2BpmMsg(sVal);
                        if (appPrefs.getBPMSensorCount() == 0) {
                            DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                            if (bpmCounter != null) {
                                long lTemp = (int)Float.parseFloat(sVal);
                                if (lTemp > 0) {
                                    bpmCounter.LastCount = lTemp;
                                    bpmCounter.LastUpdated = System.currentTimeMillis();
                                    mSavedStateViewModel.setBPM(bpmCounter);
                                }
                            }
                        }

                        break;
                    case Sensor.TYPE_STEP_COUNTER:
                        mMessagesViewModel.addDevice2StepsMsg(sVal);
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
                                            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
                                            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                            stepCounter.GoalActive = 0;
                                        }
                                    mSavedStateViewModel.setSteps(stepCounter);

                                }
                            }
                        }
                        break;
                    case Sensor.TYPE_PRESSURE:
                        mMessagesViewModel.addPressure2Msg(sVal);
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                         mMessagesViewModel.addHumidity2Msg(sVal);
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        mMessagesViewModel.addTemperature2Msg(sVal);
                        break;
                }
            }
        }

    }

    /**
     * onCustomItemSelected - Fragment callbacks for @link CustomListFragment lists, scores and generic
     *
     */
    @Override
    public void onCustomItemSelected(int type, long id, String title, int resID, String identifier) {
        String sLabel = getString(R.string.default_loadtype) + Integer.toString(type);
        String defaultValue = ATRACKIT_EMPTY;
        String sTarget = Long.toString(id);
        String sDeviceId = sDeviceID;
        String sUserId = sUserID;
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        restoreWorkoutVariables();
        // don't save defaults for these selection types
        if ((type != Constants.SELECTION_WORKOUT_INPROGRESS) && (type != SELECTION_WORKOUT_HISTORY)){
            userPrefs.setPrefStringByLabel(sLabel,sTarget);
        }

        if (type == Constants.SELECTION_WORKOUT_INPROGRESS){
            Workout w = mSessionViewModel.getWorkoutById(id,sUserId,sDeviceId);
            if (w != null) {
                mWorkout = w;
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                doConfirmDialog(Constants.QUESTION_DURATION_DELETE,title,null,null);
            }
        }
        if ((type == Constants.SELECTION_FITNESS_ACTIVITY) || (type == Constants.SELECTION_ACTIVITY_BIKE) || (type == Constants.WORKOUT_TYPE_ARCHERY)
                || (type == Constants.SELECTION_ACTIVITY_GYM) || (type == Constants.SELECTION_ACTIVITY_CARDIO)
                || (type == Constants.SELECTION_ACTIVITY_SPORT) || (type == Constants.SELECTION_ACTIVITY_RUN)
                || (type == Constants.SELECTION_ACTIVITY_WATER) || (type == Constants.SELECTION_ACTIVITY_WINTER)
                || (type == Constants.SELECTION_ACTIVITY_MISC)) {
            mWorkout._id = 2;
            mWorkout.activityID = id;
            mWorkout.activityName = title;
            mWorkout.identifier = identifier;
            mWorkout.start = 0L;
            if (mWorkoutSet == null) createWorkoutSet();
            mWorkoutSet.activityID = mWorkout.activityID;
            mWorkoutSet.activityName = mWorkout.activityName;
            mWorkoutSet.setCount = 1;
            mWorkoutMeta.activityID = mWorkout.activityID;
            mWorkoutMeta.activityName = mWorkout.activityName;
            mWorkoutMeta.packageName = mWorkout.packageName;
            mWorkoutMeta.identifier =  mWorkout.identifier;
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);

            Boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
            mSavedStateViewModel.setSetIsGym(bGym);
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            int activityIcon = (resID > 0) ? resID : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(activityIcon);
            int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
            mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
            if (bGym){
                mWorkout._id = 1L;
                mWorkoutSet.workoutID = 1L;
                mWorkoutSet.scoreTotal = (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING) ? FLAG_BUILDING : FLAG_NON_TRACKING); // build stage
                mWorkout.scoreTotal = mWorkoutSet.scoreTotal;
                int iRest = (userPrefs.getTimedRest() ? userPrefs.getWeightsRestDuration() : 0);
                mWorkoutSet.rest_duration = TimeUnit.SECONDS.toMillis(iRest);
                DateTuple setTuple = mSessionViewModel.getWorkoutSetTupleByWorkoutID(mWorkout.userID,mWorkout.deviceID, mWorkout._id);
                if (Utilities.isGymWorkout(mWorkout.activityID) && (setTuple.sync_count > 1)) {
                    ICustomConfirmDialog callback = new ICustomConfirmDialog() {
                        @Override
                        public void onCustomConfirmButtonClicked(int question, int button) {
                            if (button <= 0) {
                                mSessionViewModel.deleteWorkoutSetByWorkoutID(1L);
                            }
                            sessionSetCurrentState(WORKOUT_PENDING);
                            mSavedStateViewModel.setColorID(activityColor);
                            mSavedStateViewModel.setActiveWorkout(mWorkout);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                            if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment) {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                                bundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                                mNavigationController.navigate(R.id.action_homeRoomFragment_to_entryRoomFragment, bundle);
                            }
                        }

                        @Override
                        public void onCustomConfirmDetach() {

                        }
                    };
                    doConfirmDialog(Constants.QUESTION_KEEP_DELETE, getString(R.string.label_keep_draft_sets), callback,null);
                    return;
                }
                else
                    mSessionViewModel.deleteWorkoutSetByWorkoutID(mWorkout._id);
            }
            else{
                if (Utilities.isShooting(mWorkout.activityID)) {
                    int iRest = userPrefs.getArcheryRestDuration();
                    mWorkoutSet.rest_duration = TimeUnit.SECONDS.toMillis(iRest);
                }else {
                    mWorkoutSet.scoreTotal = (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING) ? FLAG_BUILDING : FLAG_NON_TRACKING); // build stage
                    mWorkout.scoreTotal = mWorkoutSet.scoreTotal;
                }
            }

            sLabel = getString(R.string.default_act_steps) + Long.toString(mWorkout.activityID);
            defaultValue = userPrefs.getPrefStringByLabel(sLabel);
            if (defaultValue.length() > 0)
                mWorkout.goal_steps = Long.parseLong(defaultValue);

            sLabel = getString(R.string.default_act_dur) + Long.toString(mWorkout.activityID);
            defaultValue = userPrefs.getPrefStringByLabel(sLabel);
            if (defaultValue.length() > 0) mWorkout.goal_duration = Long.parseLong(defaultValue);
            mSavedStateViewModel.setColorID(activityColor);
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount); // == 1
            sessionSetCurrentState(WORKOUT_PENDING);
            Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
            mMessagesViewModel.addLiveIntent(newIntent);
        }
        if (type == Constants.SELECTION_BODY_REGION){
            mWorkoutSet.regionID = id;
            mWorkoutSet.regionName = title;
            if ((mWorkoutSet.bodypartID != null) && (mWorkoutSet.bodypartID > 0)){
                List<Bodypart> list = mSessionViewModel.getBodypartListByRegion(id);
                boolean bFound = false;
                if (list != null && (list.size() > 0))
                    for(Bodypart bp : list){
                        if (bp._id == mWorkoutSet.bodypartID){
                            bFound = true;
                            break;
                        }
                    }
                if (!bFound){
                    mWorkoutSet.bodypartID = null;
                    mWorkoutSet.bodypartName = ATRACKIT_EMPTY;
                }

            }
            mSavedStateViewModel.setDirtyCount(1);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        }
        if (type == Constants.SELECTION_BODYPART) {
            mWorkoutSet.bodypartID = id;
            mWorkoutSet.bodypartName = title;
            int defaultInt;
            Bodypart bodypart = mSessionViewModel.getBodypartById(mWorkoutSet.bodypartID);
            if (bodypart != null) {
                //if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                   defaultInt = (bodypart.lastReps > 0) ? bodypart.lastReps : userPrefs.getDefaultNewReps();
                   mWorkoutSet.repCount = defaultInt;
                   mSavedStateViewModel.setRepsDefault(defaultInt);
                   defaultInt = (bodypart.lastSets > 0) ? bodypart.lastSets : userPrefs.getDefaultNewSets();
                   mSavedStateViewModel.setSetsDefault(defaultInt);
                  //  Float defaultFloat = (bodypart.lastWeight > 0) ? bodypart.lastWeight : 0;
                   // mWorkoutSet.weightTotal = defaultFloat;
                    mWorkoutSet.regionID = bodypart.regionID;
                    mWorkoutSet.regionName = bodypart.regionName;
                    if (bodypart.parentID == null) {
                        userPrefs.setLastBodyPartID(Long.toString(id));
                        userPrefs.setLastBodyPartName(title);
                    }else {
                        userPrefs.setLastBodyPartID(Long.toString(bodypart.parentID));
                        userPrefs.setLastBodyPartName(bodypart.parentName);
                    }


            }
            mSavedStateViewModel.setDirtyCount(1);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        }

        if (type == Constants.SELECTION_EXERCISE) {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mWorkoutSet.exerciseID = id;
            mWorkoutSet.exerciseName = title;
            Exercise exercise = mSessionViewModel.getExerciseById(id);
            if (exercise != null) {
                mWorkoutSet.per_end_xy = exercise.workoutExercise;
                if (exercise.resistanceType != null)
                mWorkoutSet.resistance_type = exercise.resistanceType;
                if (exercise.lastReps > 0) {
                    mSavedStateViewModel.setRepsDefault(exercise.lastReps);
                    mWorkoutSet.repCount = exercise.lastReps;
                }
                if ( exercise.lastSets > 0)
                    mSavedStateViewModel.setSetsDefault(exercise.lastSets);
                if (exercise.lastSelectedWeight > 0) mWorkoutSet.weightTotal = exercise.lastSelectedWeight;
                else
                    if (exercise.lastAvgWeight > 0) mWorkoutSet.weightTotal =  (float)Math.floor(exercise.lastAvgWeight);
                if ((mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0)) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);

                if ((exercise.first_BPID != null) && (exercise.first_BPID > 0)) {
                    mWorkoutSet.bodypartID = exercise.first_BPID;
                    mWorkoutSet.bodypartName = exercise.first_BPName;
                    Bodypart part = mSessionViewModel.getBodypartById(exercise.first_BPID);
                    if (part != null) {
                        mWorkoutSet.regionID = part.regionID;
                        mWorkoutSet.regionName = part.regionName;
                    }

                }
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            //   }
        }
        if (type == Constants.SELECTION_USER_PREFS){
            userPrefs.setPrefByLabel(title, (resID == 1));
        }
        if (type == Constants.SELECTION_SETS){
            int nbrSets = Math.toIntExact(id);
            mSavedStateViewModel.setSetsDefault(nbrSets);
        }
        if (type == Constants.SELECTION_REPS){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            int nbrReps = Math.toIntExact(id);
            mSavedStateViewModel.setRepsDefault(nbrReps);
            if ((mWorkoutSet != null) && (mWorkoutSet.scoreTotal == Constants.FLAG_PENDING)) {
                mWorkoutSet.repCount = nbrReps;
                if (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0f)
                    mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_REST_DURATION_GYM){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            int seconds = Integer.parseInt(title);
            userPrefs.setWeightsRestDuration(seconds);
            if (mWorkoutSet != null) {
                mWorkoutSet.rest_duration = (long) seconds;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_REST_DURATION_SETTINGS){
            int seconds = Integer.parseInt(title);
            userPrefs.setWeightsRestDuration(seconds);
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
            for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                if (frag.isVisible() && (frag instanceof SettingsDialog)) {
                    ((SettingsDialog) frag).setDurationRest(seconds);
                    Log.i(LOG_TAG, "refresh settings duration");
                }
            }
        }
        if ((type == Constants.SELECTION_WEIGHT_KG)){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.weightTotal = Float.parseFloat(title);
                if (mWorkoutSet.repCount != null)
                    if ((mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount > 0))
                        mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                if (mSavedStateViewModel.getToDoSetsSize() > 0) {
                    List<WorkoutSet> sets = mSavedStateViewModel.getToDoSets().getValue();
                    boolean updated = false;
                    if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0))
                        for (WorkoutSet set : sets) {
                            if (set.exerciseID != null)
                                if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID.equals(mWorkoutSet.exerciseID))) {
                                    set.weightTotal = mWorkoutSet.weightTotal;
                                    if ((set.weightTotal > 0f) && (set.repCount != null && set.repCount > 0))
                                        set.wattsTotal = (set.repCount * set.weightTotal);
                                    updated = true;
                                }
                        }
                    if (updated)
                        mSavedStateViewModel.setToDoSets(sets);
                }
                if (mWorkoutSet.exerciseID != null && mWorkoutSet.exerciseID > 0) {
                    Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                    if (ex != null) {
                        ex.lastSelectedWeight = mWorkoutSet.weightTotal;
                        mSessionViewModel.updateExercise(ex);
                    }
                }
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_WEIGHT_LBS){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.weightTotal = (Float.parseFloat(title) > 0) ? Float.parseFloat(title) / Constants.KG_TO_LBS : 0;  // stored as KG!
                if (mWorkoutSet.repCount != null)
                    if ((mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount > 0))
                        mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                if (mSavedStateViewModel.getToDoSetsSize() > 0) {
                    List<WorkoutSet> sets = mSavedStateViewModel.getToDoSets().getValue();
                    boolean updated = false;
                    if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0))
                        for (WorkoutSet set : sets) {
                            if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID == mWorkoutSet.exerciseID)) {
                                set.weightTotal = mWorkoutSet.weightTotal;
                                if ((set.weightTotal > 0f) && (set.repCount != null) && (set.repCount > 0))
                                    set.wattsTotal = (set.repCount * set.weightTotal);
                                updated = true;
                            }
                        }
                    if (updated)
                        mSavedStateViewModel.setToDoSets(sets);
                }
                if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                    Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                    ex.lastSelectedWeight = mWorkoutSet.weightTotal;
                    mSessionViewModel.updateExercise(ex);
                }
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if ((type>= Constants.SELECTION_WORKOUT_HISTORY) && (type <= Constants.SELECTION_GOOGLE_HISTORY)){
            if (type == SELECTION_WORKOUT_HISTORY){
              if (title.equals(Constants.ATRACKIT_ATRACKIT_CLASS)){
                  mCalendar.set(Calendar.YEAR, Integer.parseInt(identifier));
                  mCalendar.set(Calendar.MONTH, resID-1);
                  mCalendar.set(Calendar.DATE, (int)id);
                  mCalendar.set(Calendar.HOUR,0);
                  mCalendar.set(Calendar.MINUTE, 0);
                  startCustomList(Constants.SELECTION_DOY, Long.toString(mCalendar.getTimeInMillis()), sUserId);
              }
              mWorkout = mSessionViewModel.getWorkoutById(id,sUserId,sDeviceId);
              if (mWorkout != null) {
                //  mSavedStateViewModel.setActiveWorkout(mWorkout);
                  List<WorkoutSet> sets = mSessionViewModel.getWorkoutSetByWorkoutID(id,sUserId,sDeviceId);
                  if ((sets == null) || (sets.size() == 0)) {
                      broadcastToast(getString(R.string.label_empty_sets));
                  } else {
                      if (mToDoList != null)
                          mToDoList.clear();
                      else
                          mToDoList = new ArrayList<>();
                      mToDoList.addAll(sets);
                      if (resID == WORKOUT_LIVE){
                          mWorkout.start = 0; mWorkout.end=0;
                          mWorkout.rest_duration=0;mWorkout.pause_duration=0;
                          mWorkout.wattsTotal=0f;mWorkout.weightTotal=0f;
                          mWorkout = mSessionViewModel.getWorkoutById(mWorkoutSet.workoutID, sUserId, sDeviceId);
                          if (userPrefs.getConfirmStartSession()) {
                              showAlertDialogConfirm(Constants.ACTION_STARTING,null);
                          }else
                              onCustomConfirmButtonClicked(ACTION_STARTING, 1);
                      }else
                          startCustomList(SELECTION_WORKOUT_SET_HISTORY, Long.toString(id), sUserId);
                  }
              }
            }
            //TODO: set history action
            if (type == SELECTION_WORKOUT_SET_HISTORY){
                mWorkoutSet = mSessionViewModel.getWorkoutSetById(id,sUserId,sDeviceId);
                if (mWorkoutSet != null) {
                    mWorkout = mSessionViewModel.getWorkoutById(mWorkoutSet.workoutID, sUserId, sDeviceId);
                    mToDoList = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutSet.workoutID,sUserId, sDeviceId);
                    if (resID == WORKOUT_LIVE) {
                        mWorkout.start = 0; mWorkout.end=0;
                        mWorkout.rest_duration=0;mWorkout.pause_duration=0;
                        mWorkout.wattsTotal=0f;mWorkout.weightTotal=0f;
                        mWorkoutSet.start = 0; mWorkoutSet.end=0;
                        mWorkoutSet.last_sync = 0; mWorkoutSet.device_sync = null; mWorkoutSet.meta_sync =null;
                        mWorkoutSet.realElapsedStart=null;mWorkoutSet.realElapsedEnd=null;
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(Constants.ACTION_STARTING,null);
                        } else
                            onCustomConfirmButtonClicked(ACTION_STARTING, 1);
                    }else
                        doOpenWorkouts();

                }

            }
        }
        // selected date for browsing open workouts
        if (type == Constants.SELECTION_DOY){
            mCalendar.set(Calendar.MONTH, resID-1);
            mCalendar.set(Calendar.DATE, (int)id);
            mCalendar.set(Calendar.HOUR,0);
            mCalendar.set(Calendar.MINUTE, 0);
            if (title.equals(Constants.ATRACKIT_ATRACKIT_CLASS)){
                userPrefs.setLastUserOpen(mCalendar.getTimeInMillis());
                doOpenWorkouts();
            }
        }
        if (type == Constants.SELECTION_DAYS || type == SELECTION_MONTHS){
            mCalendar.set(Calendar.MONTH, resID-1);
            mCalendar.set(Calendar.DATE, (int)id);
            mCalendar.set(Calendar.HOUR,0);
            mCalendar.set(Calendar.MINUTE, 0);
            long startTime = mCalendar.getTimeInMillis();
            mCalendar.set(Calendar.HOUR_OF_DAY, 23);
            mCalendar.set(Calendar.MINUTE, 59);
            mCalendar.set(Calendar.SECOND, 59);
            long endTime = mCalendar.getTimeInMillis();
            if (hasOAuthPermission(0)) {
                doCloudHistory(sUserId,startTime,endTime);
            }else
                requestOAuthPermission(0);
        }
    }

    @Override
    public void onCustomScoreSelected(int type, long id, String title, int iSetIndex) {
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (type == Constants.SELECTION_SETS){
            int nbrSets = Math.toIntExact(id);
            mSavedStateViewModel.setSetsDefault(nbrSets);
        }
        if (type == Constants.SELECTION_REPS){
            int nbrReps = Math.toIntExact(id);
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if ((mWorkoutSet != null) && (mWorkoutSet.scoreTotal == Constants.FLAG_PENDING)) {
                mWorkoutSet.repCount = nbrReps;
                if (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0f)
                    mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }else {
                mSavedStateViewModel.setRepsDefault(nbrReps);
            }
        }
        if (type == Constants.SELECTION_INCOMPLETE_DURATION){
            restoreWorkoutVariables();
            mWorkout.duration = TimeUnit.SECONDS.toMillis(id);
            mWorkout.end = mWorkout.start + mWorkout.duration;
            mWorkout.lastUpdated = System.currentTimeMillis();
            mSessionViewModel.updateWorkout(mWorkout);
            Intent newIntent = new Intent(Constants.INTENT_INPROGRESS_RESUME);
            mMessagesViewModel.addLiveIntent(newIntent);
        }
        if (type == Constants.SELECTION_GOAL_DURATION){
           if (mWorkout != null){
               if (mWorkout.activityID > 0){
                   String sLabel = getString(R.string.default_act_dur) + Long.toString(mWorkout.activityID);
                   userPrefs.setPrefStringByLabel(sLabel, Long.toString(id));
               }
               mWorkout.goal_duration = id;
               mSavedStateViewModel.setDirtyCount(1);
               mSavedStateViewModel.setActiveWorkout(mWorkout);
           }
        }
        if (type == Constants.SELECTION_GOAL_STEPS){
            if (mWorkout != null){
                if (mWorkout.activityID > 0){
                    String sLabel = getString(R.string.default_act_steps) + Long.toString(mWorkout.activityID);
                    userPrefs.setPrefStringByLabel( sLabel, Long.toString(id));
                }
                mWorkout.goal_steps = id;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkout(mWorkout);
            }

        }
    }

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {
        String sDefault = "";
        final long timeMs = System.currentTimeMillis();
        try {
            // mis-clicking prevention, using threshold of 1000 ms
            if ((srcId != R.id.btnRepsMinus)&&(srcId != R.id.btnRepsPlus)&&(srcId != R.id.btnWeightMinus)&&(srcId != R.id.btnWeightPlus))
                if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD)
                    return;
            if (userPrefs != null) bUseKG = userPrefs.getUseKG();
            long lastPrevClick = mLastClickTime;
            mLastClickTime = SystemClock.elapsedRealtime();
            restoreWorkoutVariables();
            int currentState = mSavedStateViewModel.getState();
            if (!mWearableNavigationDrawer.isClosed()) mWearableNavigationDrawer.getController().closeDrawer();
            switch (srcId) {
                case Constants.UID_btnHomeStart:
                    /* at home go to new activity type prompt*/
                    try {
                        if ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED) || (currentState == WORKOUT_CALL_TO_LINE)) {
                            Intent actionIntent = new Intent((currentState == WORKOUT_PAUSED) ? INTENT_ACTIVE_RESUMED : INTENT_ACTIVE_PAUSE);
                            actionIntent.putExtra(KEY_FIT_TYPE, 0);
                            if (mWorkout != null) {
                                actionIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                                actionIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                            }
                            if (mWorkoutSet != null)
                                actionIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                            mMessagesViewModel.addLiveIntent(actionIntent);
                        } else {
                            if (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment) mNavigationController.popBackStack();
                            mNavigationController.navigate(R.id.action_homeRoomFragment_to_customActivityListDialog); //if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment)
                        }
                    }
                    catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        String sMsg = e.getMessage();
                        if (sMsg != null)
                            Log.i(LOG_TAG, sMsg);
                    }
                    break;
                case Constants.UID_chronoClock:
                case Constants.UID_chronometerViewCenter:
                    // btnConfirmBodypart tag passed in as selectedId
                    if (currentState == WORKOUT_LIVE) {
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 4);
                        mMessagesViewModel.addLiveIntent(vibIntent);

                        if (Utilities.isGymWorkout(mWorkout.activityID)) {
                            if (userPrefs.getConfirmSetSession()) {
                                if (mConstraintLayout != null) {
                                    showAlertDialogConfirm(ACTION_END_SET,null);
                                    return;
                                }
                            }else
                                onCustomConfirmButtonClicked(ACTION_END_SET, 1);
                        } else {
                            if (userPrefs.getConfirmEndSession()) {
                                if (mConstraintLayout != null) {
                                    showAlertDialogConfirm(Constants.ACTION_STOPPING,null);
                                    return;
                                }
                            }else
                                onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                        }
                    }
                    else
                    if (currentState == WORKOUT_PAUSED) {
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 1);
                        mMessagesViewModel.addLiveIntent(vibIntent);
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(ACTION_RESUMING,null);
                            return;
                        } else
                            onCustomConfirmButtonClicked(ACTION_RESUMING, 1);
                    }
                    else {
                        if (srcId == Constants.UID_chronoClock){
                            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                                String sMsg = appPrefs.getLastNodeName();
                                long lastSync = appPrefs.getLastNodeSync();
                                if (lastSync > 0) sMsg += (ATRACKIT_SPACE + Utilities.getTimeString(lastSync));
                                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) broadcastToast(sMsg);
                                startPhonePendingSyncAlarm();
                            }
                            return;
                        }
                        if (currentState == WORKOUT_COMPLETED) {
                            long lngDuration = userPrefs.getConfirmDuration();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                                    refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                                    refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                                    refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                    mMessagesViewModel.addLiveIntent(refreshIntent);

                                }
                            }, lngDuration);
                        } else {
                            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                                startPhonePendingSyncAlarm();
                            }
                        }
                    }
                    break;
                case Constants.UID_textViewCenter2:
                case Constants.UID_home_image_view:
                    if (currentState == WORKOUT_LIVE) {
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 4);
                        mMessagesViewModel.addLiveIntent(vibIntent);

                        if (Utilities.isGymWorkout(mWorkout.activityID)) {
                            if (userPrefs.getConfirmSetSession()) {
                                if (mConstraintLayout != null) {
                                    showAlertDialogConfirm(ACTION_END_SET,null);
                                    return;
                                }
                            }else
                                onCustomConfirmButtonClicked(ACTION_END_SET, 1);
                        } else {
                            if (userPrefs.getConfirmEndSession()) {
                                if (mConstraintLayout != null) {
                                    showAlertDialogConfirm(Constants.ACTION_STOPPING,null);
                                    return;
                                }
                            }else
                                onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                        }
                    }else
                    if (currentState == WORKOUT_PAUSED) {
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 1);
                        mMessagesViewModel.addLiveIntent(vibIntent);
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(ACTION_RESUMING,null);
                            return;
                        } else
                            onCustomConfirmButtonClicked(ACTION_RESUMING, 1);
                    }else{
                        mWearableNavigationDrawer.setIsLocked(false);
                        if (srcId == Constants.UID_home_image_view) showGoalsDialog();
                    }
                    break;

                case Constants.UID_textViewMsgCenterLeft:
                    if ((mWorkout.activityID == Constants.WORKOUT_TYPE_TENNIS) && (tennisGame != null) && ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED))){
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 4);
                        mMessagesViewModel.addLiveIntent(vibIntent);
                        String sPlayer1 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_1));
                        if (sPlayer1.length() == 0) sPlayer1 = getString(R.string.label_player_1);
                        tennisGame.wonPoint(sPlayer1);
                        mWorkoutSet.score_card = tennisGame.getScore();
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                    break;
                case Constants.UID_textViewMsgCenterRight:
                    if ((mWorkout.activityID == Constants.WORKOUT_TYPE_TENNIS) && (tennisGame != null) && ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED))){
                        Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibIntent.putExtra(KEY_FIT_TYPE, 4);
                        mMessagesViewModel.addLiveIntent(vibIntent);
                        String sPlayer2 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_2));
                        if (sPlayer2.length() == 0) sPlayer2 = getString(R.string.label_player_2);
                        tennisGame.wonPoint(sPlayer2);
                        mWorkoutSet.score_card = tennisGame.getScore();
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                    break;
                    //  session entry
                case Constants.UID_btnRegion:
                    restoreWorkoutVariables();
                    if (mWorkoutSet != null){
                        if ((text != null) && text.equals(Constants.LABEL_LONG)){
                                if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {  // GYM tag
                                    mWorkoutSet.regionName = Constants.ATRACKIT_EMPTY;
                                    mWorkoutSet.regionID = null;
                                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                                }
                        }else {
                            if (Utilities.isGymWorkout(mWorkout.activityID))
                                startCustomList(SELECTION_BODY_REGION, sDefault, sUserID);
                            else{ // START BUTTON FOR NON GYM
                                if (mWorkoutSet == null && mSavedStateViewModel.getToDoSetsSize() > 0){
                                    List<WorkoutSet> todo =  ( mSavedStateViewModel.getToDoSets().getValue() != null) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                                    mWorkoutSet = todo.get(0);
                                }
                                mWorkoutSet.setCount = 1;
                                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                                mSavedStateViewModel.setSetIndex(1);
                                if (userPrefs.getConfirmStartSession())
                                    showAlertDialogConfirm(Constants.ACTION_STARTING,null);
                                else
                                    onCustomConfirmButtonClicked(ACTION_STARTING, 1);


                            }
                        }
                    }
                    break;
                case Constants.UID_btnBodypart:
                    restoreWorkoutVariables();
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        if (mWorkoutSet != null){
                            if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {  // GYM tag
                                mWorkoutSet.bodypartName = Constants.ATRACKIT_EMPTY;
                                mWorkoutSet.bodypartID = null;
                                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                            }else {
                                mWorkout.goal_steps = 0L;
                                mSavedStateViewModel.setActiveWorkout(mWorkout);
                            }
                        }
                    }else {
                        if (Utilities.isGymWorkout(mWorkoutSet.activityID))  // GYM tag
                            startCustomList(SELECTION_BODYPART, sDefault, sUserID);
                        else {
                            sDefault = Long.toString(mWorkout.goal_steps);
                            startCustomList(Constants.SELECTION_GOAL_STEPS, sDefault, sUserID);
                        }
                    }
                    break;
                case Constants.UID_btnAddExercise:
                    if (appPrefs.getMicrophoneAvail()) {
                        mSavedStateViewModel.setSpeechTarget(Constants.TARGET_EXERCISE_NAME);
                        displaySpeechRecognizer();
                    }else{

                    }
                    break;
                case Constants.UID_btnSaveHistory:
                    mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                    doConfirmDialog(QUESTION_HISTORY_CREATE,ATRACKIT_SPACE,RoomActivity.this,mWorkout);
                    break;

                case Constants.UID_btnRoutineName:
                    if (Utilities.isGymWorkout(mWorkout.activityID)) {
                        mSavedStateViewModel.setSpeechTarget(Constants.TARGET_ROUTINE_NAME);
                        displaySpeechRecognizer();
                    }else{
                        if ((text != null) && text.equals(Constants.LABEL_LONG)){
                            mWorkout.goal_duration = 0L;
                            mSavedStateViewModel.setActiveWorkout(mWorkout);
                        }else {
                            sDefault = Long.toString(mWorkout.goal_duration);
                            startCustomList(SELECTION_GOAL_DURATION, sDefault,sUserID);
                        }
                    }
                    break;

                case Constants.UID_btnExercise:
                    restoreWorkoutVariables();
                    if (Utilities.isGymWorkout(mWorkout.activityID)) {
                        if ((text != null) && text.equals(Constants.LABEL_LONG)) {
                            if (mWorkoutSet != null) {
                                int iTag = (findViewById(srcId).getTag() == null) ? 0 : (Integer)findViewById(srcId).getTag();
                                if (iTag == 1) {  // GYM tag
                                    mWorkoutSet.exerciseName = Constants.ATRACKIT_EMPTY;
                                    mWorkoutSet.per_end_xy = Constants.ATRACKIT_EMPTY;
                                    mWorkoutSet.exerciseID = null;
                                    mSavedStateViewModel.setDirtyCount(1);
                                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                                }
                            }
                        } else
                            startCustomList(SELECTION_EXERCISE, sDefault,sUserID);
                    } else{
                        if (currentState == WORKOUT_PENDING) {
                            if (userPrefs.getConfirmStartSession()) {
                                    showAlertDialogConfirm(Constants.ACTION_STARTING,null);
                                    return;
                            }
                            onCustomConfirmButtonClicked(ACTION_STARTING, 1);
                        }
                    }
                    break;
                case Constants.UID_btnReps:
                    restoreWorkoutVariables();
                    if (Utilities.isGymWorkout(mWorkout.activityID)) {
                        startCustomList(Constants.SELECTION_REPS, mSavedStateViewModel.getRepsDefault().toString(),sUserID);
                    }
                    break;
                case Constants.UID_btnSets:
                    startCustomList(Constants.SELECTION_SETS, mSavedStateViewModel.getSetsDefault().toString(),sUserID);
                    break;
                case Constants.UID_btnWeight:
                    restoreWorkoutVariables();
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        if (mWorkoutSet != null){
                            mWorkoutSet.weightTotal = 0F;
                            mSavedStateViewModel.setDirtyCount(1);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }else {
                        sDefault = Long.toString(0);
                        if ((mWorkoutSet != null) && (mWorkoutSet.resistance_type != null && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                                && (mWorkoutSet.exerciseID != null && mWorkoutSet.exerciseID > 0)){
                            if (mWorkoutSet.weightTotal == null || mWorkoutSet.weightTotal == 0) {
                                Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                                if (ex.lastSelectedWeight > 0)
                                    sDefault = Float.toString(ex.lastSelectedWeight);
                            }else
                                sDefault = Float.toString(mWorkoutSet.weightTotal);
                        }
                        if ((mWorkoutSet != null) && (mWorkoutSet.resistance_type != null)  && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY))
                            startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault,sUserID);
                        else if (bUseKG)
                            startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault,sUserID);
                        else
                            startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault,sUserID);
                    }
                    break;

                case Constants.UID_btnBuild:
                    mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    if (mWorkoutSet == null) return;
                    if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                        if (mWorkoutSet.isValid(false)){
                            if (mSavedStateViewModel.getDirtyCount() > 0) sessionBuild(true);
                        } else {
                            if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                                if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) {
                                    if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY)
                                            && (mWorkoutSet.weightTotal==null || mWorkoutSet.weightTotal == 0 ))
                                        startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault, sUserID);
                                    else if (bUseKG)
                                        startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault, sUserID);
                                    else
                                        startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault, sUserID);
                                } else
                                    startCustomList(Constants.SELECTION_REPS, sDefault, sUserID);
                            } else
                                startCustomList(SELECTION_EXERCISE, sDefault, sUserID);
                        }
                    }else
                        OnFragmentInteraction(Constants.UID_btnStart,0,null);
                    break;
                case Constants.UID_btnRest:
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        if (mWorkoutSet != null){
                            mWorkoutSet.rest_duration = 0L;
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }else
                        startCustomList(Constants.SELECTION_REST_DURATION_GYM,String.format(Locale.getDefault(),SINGLE_INT, (mWorkoutSet.rest_duration != null ? mWorkoutSet.rest_duration : 0)),sUserID);
                    break;
                case Constants.UID_btnSettingsRest:
                    startCustomList(Constants.SELECTION_REST_DURATION_SETTINGS,String.format(Locale.getDefault(),SINGLE_INT, userPrefs.getWeightsRestDuration()),sUserID);
                    break;

                case Constants.UID_btnFinish:
                    if (mWorkout.start == 0) return;
                    if (userPrefs.getConfirmEndSession()) {
                        showAlertDialogConfirm(Constants.ACTION_STOPPING,null);
                    }else
                        onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                    break;
                case Constants.UID_btnStart:  // or continue
                    mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    if (mWorkout.start == 0) {
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(Constants.ACTION_STARTING,null);
                        }else
                            onCustomConfirmButtonClicked(ACTION_STARTING, 1);
                    }else{
                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(Constants.ACTION_START_SET,null);
                            return;
                        }else
                            onCustomConfirmButtonClicked(ACTION_START_SET, 1); // which sends the intent to start set
                    }
                    break;
                case Constants.UID_btnSave:
                    if (mSavedStateViewModel.getDirtyCount() > 0)
                        if (!mWorkoutSet.isValid(true) && mSavedStateViewModel.getIsGym()) {
                            String sMessage = Constants.ATRACKIT_EMPTY;
                            if ((mWorkoutSet.exerciseID == null) || (mWorkoutSet.exerciseID == 0))
                                sMessage = getString(R.string.label_exercise);
                            else {
                                if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F))
                                        && (mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                                    sMessage = getString(R.string.label_weight);
                                else {
                                    if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                        sMessage = getString(R.string.label_rep);
                                }
                            }
                            if (sMessage.length() > 0) broadcastToast(sMessage);
                            if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                                if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) {
                                    if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY)
                                            && (mWorkoutSet.weightTotal==null || mWorkoutSet.weightTotal == 0 ))
                                        startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault, sUserID);
                                    else if (bUseKG)
                                        startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault, sUserID);
                                    else
                                        startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault, sUserID);
                                } else
                                    startCustomList(Constants.SELECTION_REPS, sDefault, sUserID);
                            } else
                                startCustomList(SELECTION_EXERCISE, sDefault, sUserID);
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
                        if (sMessage.length() > 0) broadcastToast(sMessage);
                        return;
                    }else{
                        if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
                    }
                    if (mWorkoutSet.end == 0)
                        sessionCompleteActiveSet(System.currentTimeMillis());

                    if ((mWorkout.offline_recording == 0) && (mWorkoutSet.last_sync == 0))
                        sessionGoogleCurrentSet(mWorkoutSet);

                    if (userPrefs.getConfirmStartSession()) {
                        showAlertDialogConfirm(Constants.ACTION_START_SET,null);
                        return;
                    }else
                        onCustomConfirmButtonClicked(ACTION_START_SET, 1); // which sends the intent to start set
                    break;
                case Constants.UID_btnRepeat:
                    restoreWorkoutVariables();
                    if (mWorkoutSet == null) return;
                    if (mWorkoutSet.end == 0)
                        sessionCompleteActiveSet(System.currentTimeMillis());
                    if ((!mWorkoutSet.isValid(true) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) && mSavedStateViewModel.getIsGym()
                            && (mSavedStateViewModel.getDirtyCount() > 0)) {
                        String sMessage = Constants.ATRACKIT_EMPTY;
                        if ((mWorkoutSet.exerciseID == null) ||(mWorkoutSet.exerciseID == 0))
                            sMessage = getString(R.string.label_exercise);
                        else {
                            if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && (mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                                sMessage = getString(R.string.label_weight);
                            else {
                                if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                    sMessage = getString(R.string.label_rep);
                            }
                        }
                        if (sMessage.length() > 0) broadcastToast(sMessage);
                        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                            if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) {
                                if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY)
                                        && (mWorkoutSet.weightTotal==null || mWorkoutSet.weightTotal == 0 ))
                                    startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault, sUserID);
                                else if (bUseKG)
                                    startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault, sUserID);
                                else
                                    startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault, sUserID);
                            } else
                                startCustomList(Constants.SELECTION_REPS, sDefault, sUserID);
                        } else
                            startCustomList(SELECTION_EXERCISE, sDefault, sUserID);
                        return;
                    }else {
                        if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
                        if ((mWorkout.offline_recording == 0) && (mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                            sessionGoogleCurrentSet(mWorkoutSet);

                        if (userPrefs.getConfirmStartSession()) {
                            showAlertDialogConfirm(Constants.ACTION_REPEAT_SET,null);
                            return;
                        }else
                            onCustomConfirmButtonClicked(Constants.ACTION_REPEAT_SET, 1); // which sends the intent to start set

                    }
                    break;
                case Constants.UID_btnConfirmFinish:
                    restoreWorkoutVariables();
                    if (mWorkoutSet == null) return;
                    if ((!mWorkoutSet.isValid(true) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) && (mSavedStateViewModel.getDirtyCount() > 0)) {
                        String sMessage = Constants.ATRACKIT_EMPTY;
                        if ((mWorkoutSet.exerciseID == null) ||(mWorkoutSet.exerciseID == 0))
                            sMessage = getString(R.string.label_exercise);
                        else {
                            if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && (mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                                sMessage = getString(R.string.label_weight);
                            else {
                                if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                    sMessage = getString(R.string.label_rep);
                            }
                        }
                        if (sMessage.length() > 0) broadcastToast(sMessage);
                        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                            if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) {
                                if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY)
                                        && (mWorkoutSet.weightTotal==null || mWorkoutSet.weightTotal == 0 ))
                                    startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault, sUserID);
                                else if (bUseKG)
                                    startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault, sUserID);
                                else
                                    startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault, sUserID);
                            } else
                                startCustomList(Constants.SELECTION_REPS, sDefault, sUserID);
                        } else
                            startCustomList(SELECTION_EXERCISE, sDefault, sUserID);
                        return;
                    }
                    if (mWorkoutSet.end == 0)
                        sessionCompleteActiveSet(System.currentTimeMillis());
                    if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                        sessionGoogleCurrentSet(mWorkoutSet);
                    if (userPrefs.getConfirmEndSession()) {
                        showAlertDialogConfirm(Constants.ACTION_STOPPING,null);
                        return;
                    }else
                        onCustomConfirmButtonClicked(Constants.ACTION_STOPPING, 1);
                    break;

                case Constants.UID_btnConfirmEdit:
                    restoreWorkoutVariables();
                    if (mWorkoutSet == null) return;
                    if (mWorkoutSet.end == 0)
                        sessionCompleteActiveSet(System.currentTimeMillis());
                    if (!mWorkoutSet.isValid(true) && (mSavedStateViewModel.getDirtyCount() > 0)) {
                        String sMessage = Constants.ATRACKIT_EMPTY;
                        if ((mWorkoutSet.exerciseID == null) ||(mWorkoutSet.exerciseID == 0))
                            sMessage = getString(R.string.label_exercise);
                        else {
                            if (((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F)) && (mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                                sMessage = getString(R.string.label_weight);
                            else {
                                if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                                    sMessage = getString(R.string.label_rep);
                            }
                        }
                        if (sMessage.length() > 0) broadcastToast(sMessage);
                        return;
                    }
                    if (mSavedStateViewModel.getDirtyCount() > 0) sessionSaveCurrentSet();
                    if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0)) sessionGoogleCurrentSet(mWorkoutSet);

                    int nextSetIndex = mSavedStateViewModel.getSetIndex() + 1;
                    List<WorkoutSet> sets =   mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID,mWorkout.deviceID);
                    sets.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                    int setSize = sets.size();
                    if ((setSize > 1) && nextSetIndex <= setSize) {
                        mWorkoutSet = sets.get(nextSetIndex - 1);   // zero based set!
                    }else{
                        WorkoutSet newSet =  new WorkoutSet(mWorkoutSet);
                        newSet.setCount = nextSetIndex;  newSet.scoreTotal = FLAG_PENDING; // build mode
                        newSet.exerciseID = null; newSet.exerciseName = ATRACKIT_EMPTY; newSet.weightTotal=0F;
                        newSet.start = 0; newSet.end = 0; newSet.realElapsedEnd = null; newSet.realElapsedStart = null;
                        newSet.pause_duration = 0;  newSet.startBPM = null; newSet.endBPM = null;
                        newSet.rest_duration = (long)userPrefs.getWeightsRestDuration();
                        newSet.last_sync = 0; newSet.device_sync = null; newSet.meta_sync =null;
                        newSet.duration = 0; newSet.weightTotal = 0f; newSet.per_end_xy = ATRACKIT_EMPTY;
                        mWorkoutSet = newSet;
                        sets.add(mWorkoutSet);
                    }
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                    mSavedStateViewModel.setToDoSets(sets);
                    mNavigationController.popBackStack();
                    if (mSavedStateViewModel.getState() != WORKOUT_PAUSED)
                        sessionSetCurrentState(WORKOUT_PAUSED);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
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
                    if (repMinusSet.repCount!=null && repMinusSet.repCount > 1) repMinusSet.repCount -= 1;
                    mSavedStateViewModel.setActiveWorkoutSet(repMinusSet);
                    break;
                case Constants.UID_btnRepsPlus:
                    WorkoutSet repPlusSet = (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) ? mSavedStateViewModel.getActiveWorkoutSet().getValue() : new WorkoutSet(mWorkout);
                    if (repPlusSet._id == 0) return;  // must be the current set only
                    repPlusSet.repCount += 1;
                    mSavedStateViewModel.setActiveWorkoutSet(repPlusSet);
                    break;
                case Constants.UID_settings_sign_out_button:
                    signOut();
                    break;
                case Constants.UID_settings_get_info:
                    getSupportFragmentManager().popBackStack();
                    new TaskSendPhoneSettings(1,sUserID).run();
                    break;
                case Constants.UID_settings_send_info:
                    getSupportFragmentManager().popBackStack();
                    new TaskSendPhoneSettings(0,sUserID).run();
                    break;
                case Constants.UID_settings_find_phone_button:
                    startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                    break;
                case Constants.UID_settings_sensors_button:
                    getSupportFragmentManager().popBackStack();
                    showSensorsDialog();
                    break;
                case Constants.UID_settings_has_device_toggle:
                    boolean bSet = (selectedId == 1);
                    userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, bSet);
                    break;
                case Constants.UID_settings_age_button:
                    // if (Constants.INTENT_PERMISSION_AGE.equals(mIntentAction)){
                    getSupportFragmentManager().popBackStack();
                    doConfirmDialog(Constants.QUESTION_AGE,getString(R.string.ask_user_age),RoomActivity.this, null);
                    //}
                    break;
                case Constants.UID_settings_height_button:
                    getSupportFragmentManager().popBackStack();
                    doConfirmDialog(Constants.QUESTION_HEIGHT,getString(R.string.ask_user_height),RoomActivity.this, null);
                    break;
                case Constants.UID_settings_show_goals_button:
                    boolean bSetGoal = (selectedId == 1);
                    userPrefs.setPrefByLabel(USER_PREF_SHOW_GOALS, bSetGoal);
                    Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(refreshIntent);
                    break;
                case Constants.UID_settings_load_goals_button:
                    if (appPrefs.getAppSetupCompleted() && ((userPrefs != null) &&  userPrefs.getReadDailyPermissions())) {
                        getSupportFragmentManager().popBackStack();
                        doGoalsRefresh();
                    }else
                        if ((userPrefs != null) && !userPrefs.getReadDailyPermissions()) requestOAuthPermission(0);
                    break;
                case Constants.UID_settings_load_history_button:
                    if ((sUserID.length() > 0) && (sDeviceID.length() > 0)){
                        Configuration configHistory = null;
                        List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUserID);
                        Long lStart = timeMs;
                        if (existingConfigs.size() > 0){
                            configHistory = existingConfigs.get(0);
                            if (Long.parseLong(configHistory.stringValue1) > 0) lStart = Long.parseLong(configHistory.stringValue1);
                        }
                        mCalendar.setTimeInMillis(lStart);

                        int year = mCalendar.get(Calendar.YEAR);
                        int month = mCalendar.get(Calendar.MONTH);
                        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog mTimePicker = new DatePickerDialog(RoomActivity.this,R.style.MyDatePickerStyle, (view, selectedYear, monthOfYear, dayOfMonth) -> {
                            long endTime = timeMs;
                            mCalendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                            long startTime = mCalendar.getTimeInMillis();
                            doCloudHistory(sUserID, startTime, endTime);
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
                    try {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", ATRACKIT_ATRACKIT_CLASS);
                        intent.putExtra("app_uid", getApplicationInfo().uid);
                        intent.putExtra("android.provider.extra.APP_PACKAGE", ATRACKIT_ATRACKIT_CLASS);
                        startActivity(intent);
                    }catch (Exception e){
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    break;
                case Constants.UID_settings_read_permissions :
                    if (!hasOAuthPermission(0)) requestOAuthPermission(0);
                    else{
                        broadcastToast("Binding sensors");
                        if ((appPrefs.getUseLocation() || appPrefs.getUseSensors())) doBindSensors();
                    }
                    break;
                case Constants.UID_settings_location_use:
                    if (selectedId > 0){
                        appPrefs.setUseLocation(true);
                        mMessagesViewModel.setUseLocation(true);
                        BoundFusedLocationClient.standardInterval();
                        broadcastToast(getString(R.string.location_permission_used));
                    }else{
                        appPrefs.setUseLocation(false);
                        mMessagesViewModel.setUseLocation(false);
                        BoundFusedLocationClient.clearInterval();
                        broadcastToast(getString(R.string.location_permission_not_used));
                    }
                    mSettingsNavAdapter.notifyDataSetChanged();
                    break;
                case Constants.UID_settings_location_permissions:
                case Constants.UID_settings_sensors_permissions:
                    Intent mySplashIntent = new Intent(RoomActivity.this, SplasherActivity.class);
                    mySplashIntent.putExtra(KEY_FIT_USER,((mGoogleAccount != null) ? mGoogleAccount.getId():ATRACKIT_EMPTY));
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mySplashIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    if (srcId != Constants.UID_settings_sensors_permissions)
                        mySplashIntent.putExtra(SplasherActivity.ARG_ACTION, INTENT_PERMISSION_LOCATION);
                    else
                        mySplashIntent.putExtra(SplasherActivity.ARG_ACTION, INTENT_PERMISSION_SENSOR);
                    mySplashIntent.putExtra(SplasherActivity.ARG_RETURN_RESULT, 1);
                    splashActivityResultLauncher.launch(mySplashIntent);
                    break;
                case Constants.UID_settings_sensors_use:
                    if (selectedId > 0){
                        appPrefs.setUseSensors(true);
                        setupListenersAndIntents(getApplicationContext());
                        broadcastToast(getString(R.string.sensors_permission_used));
                    }else{
                        appPrefs.setUseSensors(false);
                        BoundSensorManager.doReset(0);
                        broadcastToast(getString(R.string.sensors_permission_not_used));
                    }
                    break;
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            String sMsg = e.getMessage();
            if (sMsg != null)
                Log.i(LOG_TAG, sMsg);
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }
    // FragmentInterface ActivityList callback
    @Override
    public void onItemSelected(int typeid, long id, String title, long resid, String identifier) {
        Context context = getApplicationContext();
        int i =0; String sLabel; String defaultValue =""; int selectionType = Constants.SELECTION_ACTIVITY_GYM;
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        long intID = id;
        mLastClickTime = SystemClock.elapsedRealtime();
        if (typeid == Constants.SELECTION_SENSOR_BINDINGS){
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                  //  BoundSensorManager.doReset();
                  //  BoundFitnessSensorManager.doReset();
                  if (appPrefs.getUseLocation() || appPrefs.getUseSensors())   doBindSensors();
                }
            });
            return;
        }

        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        String sDeviceId = (sDeviceID == null) ? appPrefs.getDeviceID() : sDeviceID;
        if (sUserId.length() == 0){
            signIn();
            return;
        }
        if (sDeviceId.length() == 0){
            List<Configuration> list = mSessionViewModel.getConfigurationLikeName(KEY_DEVICE1,sUserId);
            if (list.size() > 0) sDeviceID = list.get(0).stringValue2;
        }
        mReferenceTools.init(context);
        createWorkout(sUserId,sDeviceID);
        mWorkout._id = 2;
        createWorkoutSet();
        mWorkoutSet.setCount = 1;
        mWorkoutSet.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
        mWorkoutSet.scoreTotal = Constants.FLAG_BUILDING; // build stage
        createWorkoutMeta();
        switch (Math.toIntExact(intID)){
            case R.id.home_action1_btn:
                i = userPrefs.getActivityID1();
                if (i > 0){
                    mWorkout.activityID = (long)i;
                    mWorkout._id = (Utilities.isGymWorkout(mWorkout.activityID) ? 1 : 2);
                    mWorkout.activityName = mReferenceTools.getFitnessActivityTextById(i);
                    mWorkout.identifier = mReferenceTools.getFitnessActivityIdentifierById(i);
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(i));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(i));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName =  mWorkout.activityName;
                    mWorkoutSet.workoutID = mWorkout._id;
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
                }else {
                    intID = SELECTION_ACTIVITY_GYM;
                    mWorkout._id = 1;
                    mWorkout.activityID = intID;
                    mWorkout.activityName = title;
                    mSavedStateViewModel.setIconID((int)resid);
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(intID));
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(intID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(intID));
                    mWorkout.identifier = identifier;
                    sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_GYM);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_GYM;
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
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
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
                    int activityIcon = (resid > 0) ? (int)resid : mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                    mSavedStateViewModel.setIconID(activityIcon);
                    int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                    mSavedStateViewModel.setColorID(activityColor);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    mSavedStateViewModel.setSetIndex(1);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
                }else {
                    intID = WORKOUT_TYPE_ARCHERY;
                    mWorkout.activityID = intID;
                    mWorkout.activityName = title;
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(intID));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(intID));
                    mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(intID));
                    mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(intID));
                    mWorkout.identifier = identifier;
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    if (!hasOAuthPermission(1)){
                        requestOAuthPermission(1);
                        return;
                    }else
                        doOpenTemplates();
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
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_AEROBICS));
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    sLabel = getString(R.string.default_loadtype) + Long.toString(Constants.WORKOUT_TYPE_AEROBICS);
                    defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                    selectionType = Constants.SELECTION_ACTIVITY_CARDIO;
                    startCustomList(selectionType, defaultValue,sUserId);
                }
                break;
            case R.id.home_action4_btn:
                i = userPrefs.getActivityID4();
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
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_RUNNING));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_RUNNING));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_RUN);
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
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    Intent newIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                    mMessagesViewModel.addLiveIntent(newIntent);
                }else {
                    mWorkout.activityID = 0L;
                    mWorkout.activityName = "";
                    mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_KAYAKING));
                    mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(Constants.WORKOUT_TYPE_KAYAKING));
                    mWorkoutSet.activityID = mWorkout.activityID;
                    mWorkoutSet.activityName = mWorkout.activityName;
                    mSavedStateViewModel.setSetIsGym(false);
                    mSavedStateViewModel.setSetIsShoot(false);
                    mSavedStateViewModel.setSetIndex(1);
                    mWorkoutMeta.activityID = mWorkout.activityID;
                    mWorkoutMeta.activityName = mWorkout.activityName;
                    mWorkoutMeta.packageName = mWorkout.packageName;
                    mWorkoutMeta.identifier =  mWorkout.identifier;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
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
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.packageName = mWorkout.packageName;
                mWorkoutMeta.identifier =  mWorkout.identifier;
                mSavedStateViewModel.setSetIndex(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
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
                mSavedStateViewModel.setSetIndex(1);
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.packageName = mWorkout.packageName;
                mWorkoutMeta.identifier =  mWorkout.identifier;
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_SPORT);
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
                mSavedStateViewModel.setSetIndex(1);
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
                mSavedStateViewModel.setSetIndex(1);
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.packageName = mWorkout.packageName;
                mWorkoutMeta.identifier =  mWorkout.identifier;
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
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
                mSavedStateViewModel.setSetIndex(1);
                mSavedStateViewModel.setSetIsGym(false);
                mSavedStateViewModel.setSetIsShoot(false);
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.packageName = mWorkout.packageName;
                mWorkoutMeta.identifier =  mWorkout.identifier;
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                selectionType = Constants.SELECTION_FITNESS_ACTIVITY;
                sLabel = getString(R.string.default_loadtype) + selectionType;
                defaultValue = userPrefs.getPrefStringByLabel(sLabel);
                startCustomList(selectionType, defaultValue,sUserId);
                break;
        } // end of switch
    }


    private void startCustomList(int selectionType, String defaultValue, String sUserID){
        CustomListFragment customListFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Context context = getApplicationContext();
        if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserID);
        if (mSavedStateViewModel.isSessionSetup()) {
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        }

       if ((selectionType == Constants.SELECTION_SETS) || (selectionType == Constants.SELECTION_REPS)
                || (selectionType == SELECTION_INCOMPLETE_DURATION)
                || (selectionType == Constants.SELECTION_GOAL_DURATION) || (selectionType == Constants.SELECTION_GOAL_STEPS)){
            Integer iPreset = 0;
            if (defaultValue.length() > 0)
                iPreset = Integer.parseInt(defaultValue);
            else {
                if (selectionType == Constants.SELECTION_SETS) {
                    iPreset = (mWorkoutSet == null) ? userPrefs.getDefaultNewSets() : mWorkoutSet.setCount;
                    if (iPreset == 0) iPreset = 3;
                }
                if (selectionType == Constants.SELECTION_REPS) {
                    iPreset = (mWorkoutSet == null) ? userPrefs.getDefaultNewReps() : mWorkoutSet.repCount;
                    if (iPreset == 0) iPreset = 10;
                }
            }
            CustomScoreDialogFragment customScoreDialogFragment = CustomScoreDialogFragment.newInstance(selectionType,iPreset);
            customScoreDialogFragment.show(fragmentManager, CustomScoreDialogFragment.TAG);
        }
       else {
           String sTag = CustomListFragment.TAG;
           Long iPreset = 10L;
           if ((selectionType == Constants.SELECTION_WEIGHT_KG) || (selectionType == Constants.SELECTION_WEIGHT_LBS)) {
               sTag = getString(R.string.label_weight);
               iPreset = (long)Math.round(Float.parseFloat(defaultValue));
           }else {
               if ((defaultValue != null) && (defaultValue.length() > 0))
                   iPreset = Long.parseLong(defaultValue);
           }
            if (selectionType == Constants.SELECTION_EXERCISE) {
                sTag = getString(R.string.label_exercise);
            }
            if (selectionType == Constants.SELECTION_BODYPART) {
                sTag = getString(R.string.label_bodypart);
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
            if (selectionType == SELECTION_TO_DO_SETS){
                sTag = getString(R.string.label_routine);
                //customListFragment.set
            }
           if (selectionType == SELECTION_WORKOUT_SET_HISTORY){
               sTag = getString(R.string.label_session_select_set);
               customListFragment = CustomListFragment.create(selectionType, iPreset, sTag, sUserID, sDeviceID);
               if ((mToDoList != null) && (mToDoList.size() > 0))
                customListFragment.setWorkoutSets(mToDoList);
               if (mWorkout != null)
                customListFragment.setWorkout(mWorkout);

               //customListFragment
           }else
               customListFragment = CustomListFragment.create(selectionType, iPreset,sTag, sUserID, sDeviceID);
            if (selectionType == Constants.SELECTION_WORKOUT_INPROGRESS) sTag = "Incomplete workouts";
            customListFragment.setCancelable(true);
            customListFragment.show(fragmentManager, customListFragment.TAG);
        }
    }
    // custom confirm dialog interface callback

    @Override
    public void onCustomConfirmDetach() {
        Log.i(LOG_TAG, "confirmDialog detached");
        mSavedStateViewModel.setIsInProgress(0);
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        if (question == QUESTION_HISTORY_CREATE ) {
            if (button > 0) {
                Workout workout = confirmDialog.getWorkout();
                long startTime = workout.start;
                long endTime = workout.end;
                if (endTime == 0) endTime = startTime + workout.duration;
                Intent mIntent = new Intent(getApplicationContext(), FitSyncJobIntentService.class);
                mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_INSERT_HISTORY);
                mIntent.putExtra(Constants.MAP_START, startTime);
                mIntent.putExtra(Constants.MAP_END, endTime);
                mIntent.putExtra(Constants.KEY_RESULT, mFitResultReceiver);
                mIntent.putExtra(KEY_FIT_USER, workout.userID);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, workout.deviceID);
                mIntent.putExtra(Workout.class.getSimpleName(), workout);
                if (mGoogleAccount != null) mIntent.putExtra(Constants.KEY_PAYLOAD, (Parcelable)mGoogleAccount);
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
                Log.w(LOG_TAG, "insertHistory " + Constants.TASK_ACTION_INSERT_HISTORY + " Range Start: " + dateFormat.format(workout.start));
                Log.w(LOG_TAG, "Range End: " + dateFormat.format(workout.end));
                mMessagesViewModel.setWorkType(Constants.TASK_ACTION_INSERT_HISTORY);
                mMessagesViewModel.setWorkInProgress(true);
                try{
                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                }catch (Exception e){
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
            confirmDialog.dismiss();
        }
        if (question == Constants.QUESTION_AGE) {
            if (button > 0) {
                String sAge = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
                userPrefs.setAskAge(true);
                if ((sAge.length() > 0) && TextUtils.isDigitsOnly(sAge)){
                    appPrefs.setUseLocation((Integer.parseInt(sAge) > 9));
                    mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
                    mSettingsNavAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
        if (question == Constants.QUESTION_HEIGHT){
            if (button > 0){

                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.KEY_FIT_USER, sUserID); // userID
                Constraints constraints = new Constraints.Builder().build();
                OneTimeWorkRequest oneTimeWorkRequest =
                        new OneTimeWorkRequest.Builder(HeightBodypartWorker.class)
                                .setInputData(builder.build())
                                .setConstraints(constraints)
                                .build();
                WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(oneTimeWorkRequest).getState().observe(RoomActivity.this, state -> {
                    if (state instanceof Operation.State.SUCCESS){

                    }
                });

            }
            return;
        }
        mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        int currentState = mSavedStateViewModel.getState();
        boolean bExternal = (mWorkout == null || sDeviceID == null || (!sDeviceID.equals(mWorkout.deviceID)));
        if (button > 0) {   // positive
            switch (question) {
                case Constants.QUESTION_HISTORY_LOAD:
                    final long timeMs = System.currentTimeMillis();
                    if ((userPrefs != null) && !userPrefs.getReadSessionsPermissions()){
                        if (hasOAuthPermission(2)){
                            userPrefs.setReadSessionsPermissions(true);
                            startDailySummarySyncAlarm();
                        }else {
                            requestOAuthPermission(2);
                            return;
                        }
                    }
                    else {
                        Configuration configHistory = null;
                        List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUserID);
                        Long lStart = System.currentTimeMillis();
                        if (existingConfigs.size() > 0){
                            configHistory = existingConfigs.get(0);
                            if (Long.parseLong(configHistory.stringValue1) > 0) lStart = Long.parseLong(configHistory.stringValue1);
                        }
                        mCalendar.setTimeInMillis(lStart);
                        DatePickerDialog mTimePicker;
                        int year = mCalendar.get(Calendar.YEAR);
                        int month = mCalendar.get(Calendar.MONTH);
                        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                        mTimePicker = new DatePickerDialog(RoomActivity.this,R.style.MyDatePickerStyle,(view, selectedYear, monthOfYear, dayOfMonth) -> {
                            mCalendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                            long startTime = mCalendar.getTimeInMillis();
                            if (sUserID.length() > 0)
                                doCloudHistory(sUserID, startTime, timeMs);
                        },year,month,day);
                        mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (mMessagesViewModel.isWorkInProgress()) mMessagesViewModel.setWorkInProgress(false);
                            }
                        });
                        mTimePicker.setTitle("Select Start Date");
                        mTimePicker.show();
                    }
                    break;
                case Constants.ACTION_STARTING:
                    if (!bExternal) {
                        Intent startIntent = new Intent(INTENT_ACTIVE_START);
                        startIntent.putExtra(KEY_FIT_TYPE, 0);
                        if (mWorkout != null) {
                            startIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            startIntent.putExtra(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                            startIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        if (mWorkoutSet != null)
                            startIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        mMessagesViewModel.addLiveIntent(startIntent);
                    }else{
                        DataMap dataMap = new DataMap();
                        dataMap.putString(KEY_FIT_USER, mWorkout.userID);
                        dataMap.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
                        if (mWorkout != null) dataMap.putLong(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        if (mWorkoutSet != null) dataMap.putLong(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        if (phoneNodeId == null || phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
                        if (phoneNodeId.length() != 0)
                            sendMessage(phoneNodeId,Constants.DATA_START_WORKOUT,dataMap);
                        else
                            sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME,Constants.DATA_START_WORKOUT,dataMap);
                    }
                    break;
                case Constants.ACTION_STOPPING:
                    Intent stopIntent = new Intent(INTENT_ACTIVE_STOP);
                    stopIntent.putExtra(KEY_FIT_TYPE, 0);
                    if (mWorkout != null) {
                        stopIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        stopIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                    mMessagesViewModel.addLiveIntent(stopIntent);
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
                    mMessagesViewModel.addLiveIntent(stopQuickIntent);
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
                    if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED))
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
                        mMessagesViewModel.addLiveIntent(stopQuitIntent);
                    }
                    break;
                case Constants.ACTION_REPEAT_SET:
                case Constants.ACTION_START_SET:
                    Intent start_setIntent = new Intent(INTENT_ACTIVESET_START);
                    start_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
                    if (question == Constants.ACTION_REPEAT_SET) {
                        start_setIntent.putExtra(KEY_FIT_ACTION, 1); /* repeat flag*/
                        start_setIntent.putExtra(KEY_FIT_VALUE,0);
                    }else
                        start_setIntent.putExtra(KEY_FIT_VALUE,(mWorkoutSet.end == 0) ? 0 : 1);

                    if (mWorkout != null) {
                        start_setIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        start_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    }
                    if (mWorkoutSet != null)
                        start_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);

                    mMessagesViewModel.addLiveIntent(start_setIntent);
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
                    mMessagesViewModel.addLiveIntent(end_setIntent);
                    break;
                case ACTION_DELETE_SET:
                    if (mWorkoutSet != null) {
                        Intent delete_setIntent = new Intent(Constants.INTENT_SET_DELETE);
                        delete_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
                        delete_setIntent.putExtra(KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT_SET);
                        delete_setIntent.putExtra(Constants.KEY_FIT_USER, mWorkoutSet.userID);
                        delete_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkoutSet.workoutID);
                        delete_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
                        mMessagesViewModel.addLiveIntent(delete_setIntent);
                    }
                    break;
                case ACTION_DELETE_WORKOUT:
                    if (mWorkout != null) {
                        Intent delete_workoutIntent = new Intent(INTENT_WORKOUT_DELETE);
                        delete_workoutIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
                        delete_workoutIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                        delete_workoutIntent.putExtra(KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT);
                        delete_workoutIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                        mMessagesViewModel.addLiveIntent(delete_workoutIntent);
                    }
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
                    mMessagesViewModel.addLiveIntent(resumeQIntent);
                    break;
                case Constants.QUESTION_DURATION_DELETE:
                    // start the duration selector
                    startCustomList(SELECTION_INCOMPLETE_DURATION, "",mWorkout.userID);
                    break;
                case Constants.QUESTION_LOCATION:
                    ActivityCompat.requestPermissions(RoomActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                    break;
                case Constants.QUESTION_SENSORS:
                    ActivityCompat.requestPermissions(RoomActivity.this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            PERMISSION_REQUEST_BODY_SENSORS);
                    break;
                case Constants.QUESTION_ACT_RECOG:
                    ActivityCompat.requestPermissions(RoomActivity.this,
                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            PERMISSION_REQUEST_ACTIVITY_RECOG);
                    break;
                case Constants.QUESTION_POLICY:
                    broadcastToast("Thanks - setup will continue");
                    break;
                case Constants.QUESTION_NETWORK:
                    broadcastToast("Exiting");
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
    }else{  // negative btn
            if (question == Constants.QUESTION_PAUSESTOP) {
                Intent stopIntent = new Intent(INTENT_ACTIVE_STOP);
                stopIntent.putExtra(KEY_FIT_TYPE, 0);
                if (mWorkout != null) {
                    stopIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    stopIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                }
                if (mWorkoutSet != null) stopIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, mWorkoutSet._id);
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
                mMessagesViewModel.addLiveIntent(stopIntent);
            }
            if (question == Constants.QUESTION_DURATION_DELETE){
                if (mWorkout != null) {
                    Intent deleteIntent = new Intent(INTENT_WORKOUT_DELETE);
                    deleteIntent.putExtra(KEY_FIT_TYPE, 0);
                    deleteIntent.putExtra(KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT);
                    deleteIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    deleteIntent.putExtra(Constants.KEY_FIT_WORKOUTID, mWorkout._id);
                    mMessagesViewModel.addLiveIntent(deleteIntent);
                }
            }

            if (question == Constants.QUESTION_POLICY) {
                broadcastToast("setup will continue");
                quitApp();
            }
        }
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
                if ((UDT.lastLocation != null) && (UDT.lastLocation.length() > 0))
                    dataMap.putString(DataType.TYPE_LOCATION_SAMPLE.getName(), UDT.lastLocation);
            }
            if (this.sHost == null || (this.sHost.length() == 0))
                sendCapabilityMessage(PHONE_CAPABILITY_NAME, MESSAGE_PATH_PHONE_SERVICE, dataMap);
            else
                sendMessage(sHost, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
        }
    }

    private class TaskSyncPhoneNode implements Runnable{
        String sUserId;
        String sDeviceID;
        long timeMs;

        TaskSyncPhoneNode(String sUser, String sDevice){
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
                            if ((phoneNodeId != null) && (phoneNodeId.length() > 0))
                                sendMessage(phoneNodeId, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                            else
                                sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
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
                        if (phoneNodeId == null || phoneNodeId.length() == 0)
                            phoneNodeId = appPrefs.getLastNodeID();
                        if (phoneNodeId.length() != 0)
                            sendMessage(phoneNodeId, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                        else
                            sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                    }
                }
        }
    }

    private class TaskSendPhoneSettings implements Runnable{
        private int requestDirection;
        private String sUser;

        TaskSendPhoneSettings(int dir, String sUserID){ this.requestDirection = dir; this.sUser = sUserID; }

        @Override
        public void run() {
            if (sUser == null || sUser.length() == 0) return;
            Log.i(LOG_TAG, "running share settings");
            DataMap dataMapResponse = new DataMap();
            dataMapResponse.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_SETUP_INFO);
            dataMapResponse.putString(Constants.KEY_FIT_USER, sUser);
            dataMapResponse.putInt(Constants.KEY_FIT_TYPE, requestDirection);
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
                dataMapResponse.putInt(Constants.USER_PREF_SHOOT_REST_DURATION, userPrefs.getArcheryRestDuration());
                dataMapResponse.putInt(Constants.USER_PREF_SHOOT_CALL_DURATION, userPrefs.getArcheryCallDuration());
                dataMapResponse.putInt(Constants.USER_PREF_SHOOT_END_DURATION, userPrefs.getArcheryEndDuration());
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
            if (phoneNodeId == null || phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
            if (phoneNodeId == null || (phoneNodeId.length() == 0))
                sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME,Constants.MESSAGE_PATH_PHONE_SERVICE, dataMapResponse);
            else
                sendMessage(phoneNodeId,Constants.MESSAGE_PATH_PHONE_SERVICE, dataMapResponse);
        }
    }

    private void startDailySummarySyncAlarm(){
        int currentState = mSavedStateViewModel.getState();
        if (currentState == WORKOUT_LIVE || currentState == WORKOUT_PAUSED || currentState == WORKOUT_PENDING) return;
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
        Context context = getApplicationContext();
        if (syncInterval == 0){
            CustomIntentReceiver.cancelAlarm(context);
        }else
            CustomIntentReceiver.setAlarm(context,((timeMs - lastSync) >= syncInterval), syncInterval, sUserID, (Parcelable)mGoogleAccount);  // repeat
    }

    private void startPhonePendingSyncAlarm(){
        if (userPrefs == null || !userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) return;
        Context context = getApplicationContext();
        if (sUserID.length() == 0) return;
        long timeMs = System.currentTimeMillis();
        long lastSync = appPrefs.getLastPhoneSync();
        long syncInterval = appPrefs.getPhoneSyncInterval();
        if (syncInterval == 0) syncInterval =  (TimeUnit.MINUTES.toMillis(5));
        long triggerTimeMs = timeMs + syncInterval;
        int currentState = mSavedStateViewModel.getState();
        Intent syncCheckIntent = new Intent(Constants.INTENT_PHONE_SYNC);
        syncCheckIntent.putExtra(KEY_FIT_TYPE,1); // not triggered intent
        syncCheckIntent.putExtra(KEY_FIT_USER, sUserID);
        syncCheckIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
        PendingIntent mPhoneSyncPendingIntent = PendingIntent.getBroadcast(context, ALARM_PHONE_SYNC_CODE, syncCheckIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mPhoneAlarmManager.cancel(mPhoneSyncPendingIntent);
        if ((lastSync + syncInterval) <= timeMs
                && (currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED && currentState != WORKOUT_PENDING)) { // check don't wait
            mMessagesViewModel.addLiveIntent(syncCheckIntent);
            Log.e(LOG_TAG, "starting immediate phone sync " + INTENT_PHONE_SYNC);
        }else{
            mPhoneAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mPhoneSyncPendingIntent);
            Log.e(LOG_TAG, "setting phone sync alarm");
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

    private void startCloudPendingSyncAlarm(){
        Context context = getApplicationContext();
        if ((sUserID == null || sUserID.length() == 0) || sDeviceID.length() == 0) return;
        long timeMs = System.currentTimeMillis();
        long lastSync = appPrefs.getLastSync();
        long syncInterval = appPrefs.getLastSyncInterval();
        if (syncInterval == 0) syncInterval =  (TimeUnit.MINUTES.toMillis(5));
        long triggerTimeMs = timeMs + syncInterval;
        boolean isConnected = mReferenceTools.isNetworkConnected();
        DateTuple pendingTuple = mSessionViewModel.getWorkoutUnSyncCount(sUserID,sDeviceID);
        DateTuple pendingSetTuple = mSessionViewModel.getWorkoutSetUnSyncCount(sUserID,sDeviceID);
        Intent syncCheckIntent = new Intent(INTENT_CLOUD_SYNC);
        syncCheckIntent.putExtra(KEY_FIT_TYPE,1); // not triggered intent
        syncCheckIntent.putExtra(KEY_FIT_USER, sUserID);
        syncCheckIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
        PendingIntent mNetworkSyncPendingIntent = PendingIntent.getBroadcast(context, ALARM_PENDING_SYNC_CODE, syncCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNetworkAlarmManager.cancel(mNetworkSyncPendingIntent);
        if (isConnected && ((pendingTuple.sync_count > 0 || pendingSetTuple.sync_count > 0) || (timeMs - lastSync) > syncInterval)) {  // check don't wait
            int currentState = mSavedStateViewModel.getState();
            if (currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED && currentState != WORKOUT_PENDING) {
                Log.e(LOG_TAG, "pendingSync starting now " + syncInterval + " last " + lastSync + "calc diff " + (timeMs - lastSync));
                handleIntent(syncCheckIntent);
            }else{
                Log.e(LOG_TAG, "setting exact alarm for pendingSync");
                mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mNetworkSyncPendingIntent);
            }
        }else {
            Log.e(LOG_TAG, "setting exact alarm for pendingSync");
            mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mNetworkSyncPendingIntent);
        }
    }

    private void startExactTimeAlarm(String intentAction, long workoutID,long setID,long interval){
        long timeMs = System.currentTimeMillis();
        Intent triggeredIntent = new Intent(intentAction);
        if (INTENT_GOAL_TRIGGER.equals(intentAction)|| INTENT_CALL_TRIGGER.equals(intentAction)) {
            triggeredIntent.putExtra(KEY_FIT_WORKOUTID, workoutID);
            if (setID > 0) triggeredIntent.putExtra(KEY_FIT_WORKOUT_SETID, setID);
            triggeredIntent.putExtra(KEY_FIT_TYPE, GOAL_TYPE_DURATION);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Long.toString(interval));
            triggeredIntent.putExtra(KEY_FIT_USER, sUserID);
        }else{
            Log.w(LOG_TAG, "setting exact Intent " + intentAction);
            triggeredIntent.putExtra(KEY_FIT_USER, sUserID);
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
        Intent triggeredIntent = new Intent(intentAction);
        if (INTENT_GOAL_TRIGGER.equals(intentAction)|| INTENT_CALL_TRIGGER.equals(intentAction)) {
            triggeredIntent.putExtra(KEY_FIT_WORKOUTID, workoutID);
            if (setID > 0) triggeredIntent.putExtra(KEY_FIT_WORKOUT_SETID, setID);
            triggeredIntent.putExtra(KEY_FIT_TYPE, GOAL_TYPE_DURATION);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Long.toString(interval));
            triggeredIntent.putExtra(KEY_FIT_USER, sUserID);
        }else{
            triggeredIntent.putExtra(KEY_FIT_USER, sUserID);
            triggeredIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
            int state = Math.toIntExact(workoutID);
            triggeredIntent.putExtra(KEY_FIT_VALUE, state);
        }
        Log.w(LOG_TAG, "cancelling exact Intent " + intentAction);
        triggeredIntent.setPackage(ATRACKIT_ATRACKIT_CLASS);
        PendingIntent goalPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_EXACT_TIME_CODE, triggeredIntent, PendingIntent.FLAG_ONE_SHOT);
        mNetworkAlarmManager.cancel(goalPendingIntent);
    }

     private void startNetworkCheckAlarm(Context context){
         if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
         long syncInterval = appPrefs.getNetworkCheckInterval();
         long lastSync = appPrefs.getLastNetworkCheck();
         long timeMs = System.currentTimeMillis();
         long triggerTimeMs = timeMs + syncInterval;
         if (mReferenceTools != null) mReferenceTools.init(context);

         if (!mReferenceTools.isNetworkConnected() && ((timeMs - lastSync) > syncInterval)) {
            // broadcastToast(getString(R.string.action_offline));
             Intent networkCheckIntent = new Intent(INTENT_NETWORK_CHECK);
             networkCheckIntent.putExtra(KEY_FIT_VALUE, sUserID);
             PendingIntent mNetworkCheckPendingIntent = PendingIntent.getBroadcast(context, ALARM_NETWORK_CHECK_CODE, networkCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT);
             mNetworkAlarmManager.cancel(mNetworkCheckPendingIntent);
             mNetworkAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, mNetworkCheckPendingIntent);
         }
     }

     private void refreshDisplayAndSetNextUpdate(){
        long timeMs = System.currentTimeMillis();
        java.text.DateFormat tf = java.text.DateFormat.getTimeInstance();
        if (mAmbientController == null) return;
        // do the fragments
         if (mNavigationController != null) loadDataAndUpdateScreen();
         ColorStateList colorStateList;
         int bgColor;
        if (mAmbientController.isAmbient()) {
            bgColor = getColor(R.color.ambientBackground);
            colorStateList = getColorStateList(R.color.ambientBackground);
            mConstraintLayout.setBackgroundTintList(colorStateList);
            // mWearableNavigationDrawer.setBackgroundColor(bgColor);
            if (mWearableNavigationDrawer.getController() != null) mWearableNavigationDrawer.getController().closeDrawer();

            /* Calculate next trigger time (based on state). */
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;
            mAmbientUpdateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, triggerTimeMs, mAmbientUpdatePendingIntent);
        } else {
            int currentState = mSavedStateViewModel.getState();
            if (currentState == WORKOUT_LIVE){
                colorStateList = getColorStateList(R.color.secondaryColor);
                bgColor = getColor(R.color.secondaryColor);
            }else{
                if (currentState == WORKOUT_COMPLETED){
                    colorStateList = getColorStateList(R.color.secondaryDarkColor);
                    bgColor = getColor(R.color.secondaryDarkColor);
                }else{
                    colorStateList = getColorStateList(R.color.primaryDarkColor);
                    bgColor = getColor(R.color.primaryDarkColor);
                }
            }
            mConstraintLayout.setBackgroundTintList(colorStateList);

           // mSwipeDismissFrameLayout.setBackgroundColor(bgColor);


            /* Calculate next trigger time (based on state). */
            long delayMs = ACTIVE_INTERVAL_MS - (timeMs % ACTIVE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }
    }
    private void loadDataAndUpdateScreen(){
        if ((mNavigationController != null) && (mNavigationController.getCurrentDestination() != null)) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
            if (navHostFragment != null)
                for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                    if (frag.isVisible() && (frag instanceof AmbientInterface)) {
                        ((AmbientInterface) frag).loadDataAndUpdateScreen();
                    }
                }
        }
    }
    private void bindRecognitionListener(){
        // Activity Recognition using pendingIntent
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sUserId.length() > 0 && appPrefs.getAppSetupCompleted() && (userPrefs != null)) {
      //      Log.w(LOG_TAG, "ACTIVITY RECOGNITION requesting update " + sUserId);
            Context context = getApplicationContext();
            Intent intent = new Intent(context, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            activityRecognitionClient = ActivityRecognition.getClient(context);
            Task<Void> task2 = activityRecognitionClient.requestActivityUpdates(180_000L, pendingIntent);
            task2.addOnCompleteListener(task1 -> {
                boolean isSuccess = task1.isSuccessful();
              //  Log.e(LOG_TAG, "ACTIVITY RECOGNITION requesting update completed " + isSuccess);
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
        if (sUserID == null || sUserID.length() == 0) {
         //   Log.e(LOG_TAG,"bindLocationListener sUSERID EXIT");
            return;
        }

      //  Log.e(LOG_TAG,"bindLocationListener " + (mLocationListener == null ? "null listener":"not null listener"));
        if (mLocationListener == null)
            mLocationListener = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult.getLastLocation() != null) {
                        Location loc = locationResult.getLastLocation();
                        Intent intent = new Intent(INTENT_LOCATION_UPDATE);
                        Context context = getApplicationContext();
                        intent.putExtra(INTENT_LOCATION_UPDATE, loc);
                        intent.putExtra(INTENT_EXTRA_RESULT, (loc != null) ? 1 : 0);
                        intent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                       context.sendBroadcast(intent);
                    }
                }
            };
        BoundFusedLocationClient.bindFusedLocationListenerIn(RoomActivity.this, mLocationListener,getApplicationContext());
        BoundFusedLocationClient.standardInterval();
    }

    private void bindFitSensorListener(){
        if (sUserID == null || sUserID.length() == 0){
            Log.e(LOG_TAG,"bindFitSensorListener sUSERID EXIT");
            return;
        }
     //   Log.e(LOG_TAG,"bindFitSensorListener " + (mDataPointListener == null ? "null listener":"not null listener"));
        if (mDataPointListener == null)
            mDataPointListener = new xFITSensorListener();
        BoundFitnessSensorManager.bindFitnessSensorListenerIn(this, mDataPointListener, getApplicationContext());
        BoundFitnessSensorManager.doSetup();
    }
    private void bindSensorListener(int type, int delay){
        if (sUserID == null || sUserID.length() == 0) {
            Log.e(LOG_TAG,"bindFitSensorListener sUSERID EXIT");
            return;
        }
      //  Log.e(LOG_TAG,"bindSensorListener " + (mSensorListener == null ? "null listener":"not null listener"));
        if (mSensorListener == null) mSensorListener = new xSensorListener();
        if (delay < 0) {
            if (type == 0) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getOthersSampleRate()));
            if (type == 1) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getStepsSampleRate()));
            if (type == 2) delay = Math.toIntExact(TimeUnit.SECONDS.toMicros(userPrefs.getBPMSampleRate()));
        }
        Handler handler = new Handler(mSensorThread.getLooper());
        BoundSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext(), handler);
        BoundSensorManager.doSetup(type, delay);
    }
    private void bindAccelSensorListener(){
        if (mSensorListener == null){
            mSensorListener = new xSensorListener();
        }
        BoundAccelSensorManager.bindSensorListenerIn(this, mSensorListener, getApplicationContext());
    }
    private void destroyAccelSensorListener(){
        BoundAccelSensorManager.DestroyListener();
    }

    private class xCapabilityListener implements CapabilityClient.OnCapabilityChangedListener {
        @Override
        public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
            String sCapability = capabilityInfo.getName();
            if (!sCapability.equals(Constants.PHONE_CAPABILITY_NAME)
                    && !sCapability.equals(Constants.STEP_CAPABILITY_NAME)
                    && !sCapability.equals(Constants.BPM_CAPABILITY_NAME))
                return;
            List<Configuration> existingConfigs = mMessagesViewModel.getLiveConfig();
            Configuration configDevice1 = null;
            Configuration configDevice2 = null;
            if (existingConfigs.size() > 0) {
                for (Configuration c : existingConfigs) {
                    if (c.stringName.equals(KEY_DEVICE1)) configDevice1 = c;
                    if (c.stringName.equals(KEY_DEVICE2)) configDevice2 = c;
                }
            }
            String sHostID = (configDevice2 != null) ? configDevice2.stringValue : appPrefs.getLastNodeID();
            if (configDevice2 == null) return;  // not setup don't do anything
            String nodeName = ATRACKIT_EMPTY;
            Set<Node> connectedNodes = capabilityInfo.getNodes();
            for (Node node : connectedNodes) {
                if (sHostID.equals(node.getId())) {
                    nodeName = node.getDisplayName();
                    Log.e(LOG_TAG, "onCapabilityChanged existing host " + nodeName);
                    mPhoneNodesWithApp.add(node);
                    phoneNodeId = node.getId();
                    appPrefs.setLastNodeID(phoneNodeId);
                    appPrefs.setLastNodeName(nodeName);
                }
                else {
                    Log.e(LOG_TAG, "onCapabilityChanged existing host " + nodeName);
                    if (node.isNearby()) {
                        phoneNodeId = node.getId();
                        //appPrefs.setLastNodeID(phoneNodeId);
                        nodeName = node.getDisplayName();
                    }
                }

            }
            if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE) && (phoneNodeId != null)) {
                final String finalNodeName = nodeName;
                if (configDevice1 != null) {
                    DataMap dataMap = new DataMap();
                    dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                    if ((configDevice2 != null) && (configDevice2.longValue > 0))
                        dataMap.putInt(KEY_FIT_TYPE, (int) 0); // new connection check indicator
                    else
                        dataMap.putInt(KEY_FIT_TYPE, (int) 1); // handshake exchange indicator

                    dataMap.putString(KEY_FIT_USER, sUserID);
                    dataMap.putString(KEY_FIT_DEVICE_ID, configDevice1.stringValue2);
                    dataMap.putString(KEY_FIT_NAME, Constants.KEY_DEVICE1);
                    dataMap.putLong(KEY_FIT_TIME, configDevice1.longValue);
                    dataMap.putString(KEY_PAYLOAD, MESSAGE_PATH_WEAR);
                    dataMap.putString(Constants.KEY_DEVICE1, configDevice1.stringValue1);
                    dataMap.putString(Constants.KEY_DEVICE2, configDevice1.stringValue2);
                    Log.e(LOG_TAG,"capability changed sending handshake COMM_TYPE_REQUEST_INFO ");
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(
                            getApplicationContext()).sendMessage(phoneNodeId,
                            MESSAGE_PATH_PHONE_SERVICE,
                            dataMap.toByteArray());

                    sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                        @Override
                        public void onComplete(Task<Integer> task) {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "capability changed COMM_TYPE_REQUEST_INFO Message sent successfully");
                                if (finalNodeName.length() > 0) {
                                    mMessagesViewModel.setPhoneAvailable(true);
                                    mMessagesViewModel.setNodeDisplayName(finalNodeName);
                                }
                            } else {
                                mMessagesViewModel.setPhoneAvailable(false);
                                Log.d(LOG_TAG, "capability changed COMM_TYPE_REQUEST_INFO Message failed.   onCapabilityChanged");
                            }
                        }
                    });
                }
            }
        }
    }
    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = appPrefs.getLastNodeID();
        boolean bFound = false;
        /* Find a nearby node or pick one arbitrarily. There should be only one phone connected
         * that supports this sample.
         */
        if (bestNodeId.length() > 0)
            for (Node node : nodes) {
                if (bestNodeId.equals(node.getId())){
                    bFound = true;
                    break;
                }
            }
        return (bFound ? bestNodeId : ATRACKIT_EMPTY);
    }

    private void sendDataClientMessage(String intentString, Bundle bundle){
        PutDataRequest request = setDataRequestItem(intentString, bundle);
        request.setUrgent();
        Log.e(LOG_TAG, "sendDataClientMessage request " + intentString);
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask.addOnCompleteListener(task -> Log.e(LOG_TAG, "sendDataClientMessage outcome: " + task.isSuccessful() + " " + bundle.toString()));

    }
    private PutDataRequest setDataRequestItem(String intentString, Bundle bundle){
        Gson gson = new Gson();
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PHONE_DATA_BUNDLE_RECEIVED_PATH);
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

    private void sendMessage(final String sHost, final String sPath, final DataMap dataMap) {
        // Instantiates clients without member variables, as clients are inexpensive to
        // create. (They are cached and shared between GoogleApi instances.)
        if (sHost == null || sHost.length() == 0){
            for (Node node: mPhoneNodesWithApp) {
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
                            Log.e(LOG_TAG, "Send Message failed. " + sPath);
                        }
                    }
                });
            }
        }else {
            Task<Integer> sendMessageTask =
                    Wearable.getMessageClient(
                            getApplicationContext()).sendMessage(
                            sHost,
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


    //TODO: review sendCapabilityMessage
    private void sendCapabilityMessage(final String sCapabilityName, final String sPath, final DataMap dataMap) {
        // Initial check of capabilities to find the phone.
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(RoomActivity.this)
                .getCapability(sCapabilityName, CapabilityClient.FILTER_REACHABLE);
        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Capability request succeeded.");

                    CapabilityInfo capabilityInfo = task.getResult();
                    mPhoneNodesWithApp = capabilityInfo.getNodes();
                    String sPhoneNodeId = pickBestNodeId(mPhoneNodesWithApp);
                    if (sPhoneNodeId != null) {
                        // Instantiates clients without member variables, as clients are inexpensive to
                        // create. (They are cached and shared between GoogleApi instances.)
                        if (!sPhoneNodeId.equals(phoneNodeId) || (phoneNodeId == null)){
                            phoneNodeId = sPhoneNodeId;
                        }
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(
                                        getApplicationContext()).sendMessage(
                                        phoneNodeId,
                                        sPath,
                                        dataMap.toByteArray());

                        sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(Task<Integer> task) {
                                if (task.isSuccessful()) {
                                    Log.w(LOG_TAG, "Cap Message sent successfully " + dataMap.toString());
                                    mSavedStateViewModel.setLastDeviceMsg(System.currentTimeMillis());
                                } else {
                                    Log.e(LOG_TAG, "Cap Message failed.");
                                }
                            }
                        });
                    } else {
                        mMessagesViewModel.setPhoneAvailable(false);
                    }
                } else {
                    mMessagesViewModel.setPhoneAvailable(false);
                }
            }
        });
    }

    private class xSensorListener implements SensorEventListener {
        private final Processor peakDetProcessor = new PeakDetProcessor(Constants.DELTA);
        public xSensorListener(){   }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            String sName = event.sensor.getName();
            Float val;
            String sValue; SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
            long lTemp = 0L;
            long timeMs = System.currentTimeMillis();
            long lastDeviceMsg = mSavedStateViewModel.getLastDeviceMsg();
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
                    Log.w(LOG_TAG,"Bound Step: " + sValue);
                    mMessagesViewModel.addDeviceStepsMsg(sValue);
                    // device steps always loaded
                    DailyCounter stepCounter = mSavedStateViewModel.getSteps();
                    if (stepCounter != null) {
                        lTemp = Math.round(val);
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
                                    resultData.putString(KEY_FIT_USER, sUserID);
                                    if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
                                    if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                    stepCounter.GoalActive = 0;
                                }
                            mSavedStateViewModel.setSteps(stepCounter);
                        }
                    }

                    long lSteps = ((sValue.length() > 0) && TextUtils.isDigitsOnly(sValue)) ? Long.parseLong(sValue) : 0;
                    if (sdt != null && (lSteps > 0)){
                        Log.w(LOG_TAG,"SDT Step: " + sValue);
                        sdt.deviceStep = Math.toIntExact(lSteps);
                        sdt.lastDeviceStep = timeMs;
                        sdt.lastUpdated = timeMs;
                        mSavedStateViewModel.setSDT(sdt);
/*                        if (BoundSensorManager.getState() != WORKOUT_LIVE
                                && (sdt.activityType == DetectedActivity.STILL || sdt.activityType == DetectedActivity.IN_VEHICLE
                                || sdt.activityType == DetectedActivity.UNKNOWN))
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BoundSensorManager.doReset(1);
                                }
                            },1000);*/
                    }
                    if ((lSteps > 0) && (appPrefs.getAppSetupCompleted() && appPrefs.getUseLocation()))
                        new Handler().post(new UpdateCurrentLatLng(0, lSteps));

                    if ((userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && mMessagesViewModel.hasPhone()
                            && ((timeMs - lastDeviceMsg) > TimeUnit.SECONDS.toMillis(15L)))){

                    }
                    break;
                case Sensor.TYPE_HEART_RATE:
                    if (BoundSensorManager.getCount(2) == 0) return;
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        mMessagesViewModel.addDeviceBpmMsg(sValue);
                        DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                        if (bpmCounter != null) {
                            lTemp = Math.round(val);
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
                                        resultData.putString(KEY_FIT_USER, sUserID);
                                        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
                                        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
                                        bpmCounter.GoalActive = 0;
                                    }
                                mSavedStateViewModel.setBPM(bpmCounter);
                            }
                        }
                        if (sdt != null && (val > 0)){
                            Log.w(LOG_TAG,"BPM: " + sValue);
                            sdt.deviceBPM = val;
                            sdt.lastDeviceBPM = timeMs;
                            sdt.lastUpdated = timeMs;
                            mSavedStateViewModel.setSDT(sdt);
/*                            if (BoundSensorManager.getState() != WORKOUT_LIVE
                                    && (sdt.activityType == DetectedActivity.STILL || sdt.activityType == DetectedActivity.IN_VEHICLE
                                    || sdt.activityType == DetectedActivity.UNKNOWN))
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        BoundSensorManager.doReset(2);
                                    }
                                },1000);*/
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
                                sdt.lastDeviceOther = timeMs;
                                sdt.lastUpdated = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getPressureSensorCount() == 0) || (appPrefs.getPressureSensorCount() > 0 && (sdt.pressure > 0))){
                                    if ((appPrefs.getHumiditySensorCount() == 0) || (appPrefs.getHumiditySensorCount() > 0 && (sdt.humidity > 0))){
                                      //  if (BoundSensorManager.getState() != WORKOUT_LIVE)
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
                                sdt.lastDeviceOther = timeMs;
                                sdt.lastUpdated = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getTempSensorCount() == 0) || (appPrefs.getTempSensorCount() > 0 && (sdt.temperature > 0))){
                                    if ((appPrefs.getHumiditySensorCount() == 0) || (appPrefs.getHumiditySensorCount() > 0 && (sdt.humidity > 0))){
                                     //   if (BoundSensorManager.getState() != WORKOUT_LIVE)
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
                                sdt.lastDeviceOther = timeMs;
                                sdt.lastUpdated = timeMs;
                                mSavedStateViewModel.setSDT(sdt);
                                if ((appPrefs.getTempSensorCount() == 0) || (appPrefs.getTempSensorCount() > 0 && (sdt.temperature > 0))){
                                    if ((appPrefs.getPressureSensorCount() == 0) || (appPrefs.getPressureSensorCount() > 0 && (sdt.pressure > 0))){
                                    //    if (BoundSensorManager.getState() != WORKOUT_LIVE)
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
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            String sSuggested;
            long timeMs = System.currentTimeMillis();
            if (DataType.TYPE_HEART_RATE_BPM.equals(dataPoint.getDataType())) {
                Integer iVal =  Math.round(dataPoint.getValue(Field.FIELD_BPM).asFloat());
                if (iVal > 0) {
                    sSuggested = iVal.toString();
                    mMessagesViewModel.addBpmMsg(sSuggested);
                    SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
                    if (sdt != null){
                        sdt.fitBPM = dataPoint.getValue(Field.FIELD_BPM).asFloat();
                        sdt.lastFitBPM = timeMs;
                        sdt.lastUpdated = timeMs;
                        mSavedStateViewModel.setSDT(sdt);
                       // if ((BoundFitnessSensorManager.getState() != WORKOUT_LIVE) && (sdt.lastFitStep > sdt._id)) BoundFitnessSensorManager.doReset();
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
                if (dataPoint.getValue(Field.FIELD_STEPS).asInt() > 0) {
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
            //    Log.e(LOG_TAG, "other data point received: " + sSuggested);
            }
        }
    }


    private void doImageWork(Uri getAddress, int type){
        final Context context = getApplicationContext();
        if (userPrefs == null || !userPrefs.getPrefByLabel(USER_PREF_STORAGE)) return;
        Data.Builder builder = new Data.Builder();
        if (getAddress != null) {
            builder.putString(KEY_IMAGE_URI, getAddress.toString());
            builder.putInt(KEY_FIT_TYPE, type);
            builder.putString(KEY_FIT_USER, sUserID);
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
                        Log.w(LOG_TAG, "working on image");
                    } else {
                        // showWorkFinished();
                        Log.w(LOG_TAG, "finished imageWorker");
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
                            Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH); // image refresh!
                            refreshIntent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
                            refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                            mMessagesViewModel.addLiveIntent(refreshIntent);
                        }else Log.e(LOG_TAG, "finished imageWorker FAILED");
                    }
                }
            });
        }
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
              //          Log.d(LOG_TAG, "changed not finished");
                        //showWorkInProgress();
                    } else {
            //            Log.d(LOG_TAG, "changed finished");
                    }
                }
            }
        });

    }
    private void doConfirmDialog(int questionType, String sQuestion, ICustomConfirmDialog callback, Workout workout){
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

            confirmDialog = CustomConfirmDialog.newInstance(questionType, sQuestion, ((callback == null)? RoomActivity.this : callback));
            if (questionType == Constants.QUESTION_NETWORK)
                confirmDialog.setSingleButton(true);
            if (questionType == QUESTION_HISTORY_CREATE)
                if (workout != null){
                    confirmDialog.setMessageText(workout.activityName);
                    confirmDialog.setWorkout(workout);
                }
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

    private void doAsyncGoogleFitAction(int action, Workout workout, WorkoutSet set, WorkoutMeta meta){
        if (workout != null) {
            if ((workout.start == 0) && (workout.end == 0)) return;
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
            optionsType=4;
        if ((action == TASK_ACTION_SYNC_WORKOUT) || (action == TASK_ACTION_READ_CLOUD))
            optionsType = 5; // everything

        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (optionsType == 0 || (mGoogleAccount == null)) return;
        if (!hasOAuthPermission(optionsType)) {
            Log.e(LOG_TAG, "doAsyncGoogleFitAction no permissions" + optionsType + " action "  + action);
            requestOAuthPermission(optionsType);
            return;
        }
        // test we are not busy already!
        if (action == TASK_ACTION_SYNC_WORKOUT){
            List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.ATRACKIT_SETUP,Constants.ATRACKIT_ATRACKIT_CLASS);
            if (configList != null && (configList.size() > 0)){
                Configuration configSetup = configList.get(0);
                if (configSetup.stringValue1 != null && configSetup.stringValue1.length() > 0){
                    Log.e(LOG_TAG, "doAsyncGoogleFitAction Sync already running - not starting new");
                    startCloudPendingSyncAlarm();
                    return;
                }
            }
        }

        try{
            long startTime = (workout != null) ? workout.start : 0;
            if (startTime == 0) startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            long endTime = (workout != null) ? workout.end : 0;
            if (endTime == 0) endTime = System.currentTimeMillis();
            final Intent newIntent = new Intent();
            newIntent.putExtra(Constants.KEY_FIT_ACTION, action);
            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(getApplicationContext(), mReferenceTools.getFitnessSignInOptions(optionsType));
            newIntent.putExtra(Constants.MAP_START, startTime);
            newIntent.putExtra(Constants.MAP_END, endTime);
            newIntent.putExtra(Constants.KEY_RESULT, mFitResultReceiver);
            if (workout != null) {
                newIntent.putExtra(KEY_FIT_USER, workout.userID);
                newIntent.putExtra(KEY_FIT_DEVICE_ID, workout.deviceID);
                newIntent.putExtra(Workout.class.getSimpleName(), workout);
            }else{
                newIntent.putExtra(KEY_FIT_USER, mGoogleAccount.getId());
                newIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            }
            if (set != null) newIntent.putExtra(WorkoutSet.class.getSimpleName(), set);
            mMessagesViewModel.setWorkType(action);
            mMessagesViewModel.setWorkInProgress(true);
            try{
                if (gsa == null || gsa.isExpired()){
                    authInProgress = true;
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(Constants.CLIENT_ID)
                            .requestEmail().build();
                    // Build a GoogleSignInClient with the options specified by gso.
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
                    Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
                    signInAccountTask.addOnCompleteListener(task -> {
                        authInProgress = false;
                        if (task.isSuccessful()){
                            GoogleSignInAccount acct = task.getResult();
                            newIntent.putExtra(Constants.KEY_PAYLOAD,acct);
                        }
                    });
                }else
                    newIntent.putExtra(Constants.KEY_PAYLOAD,gsa);

                FitSyncJobIntentService.enqueueWork(getApplicationContext(), newIntent);
            }catch (Exception e){
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage() + " history action " + action + Constants.LINE_DELIMITER);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    
/*    private void doAsyncGoogleFitAction(int action, Workout w, WorkoutSet set, WorkoutMeta meta){
        try {
            doCloudSyncJob(action,w,set,meta, actionResultReceiver);
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage() + "session action  error " + action);
            FirebaseCrashlytics.getInstance().recordException(e);
            if (mWorkout != null) {
                if (!mReferenceTools.isNetworkConnected()) mWorkout.offline_recording = 1;
                mWorkout.lastUpdated = System.currentTimeMillis();
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                mSessionViewModel.updateWorkout(mWorkout);
            }
        }
    }

    private void doAsyncHistoryAction(int action, Workout w, WorkoutSet set, WorkoutMeta meta){
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                mMessagesViewModel.setWorkInProgress(false);
                if (resultCode == 200){
                    String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
                    String sDeviceID = appPrefs.getDeviceID();
                    final long timeMs = System.currentTimeMillis();
                    // async history task callbacks!
                    if (resultData.containsKey(Constants.KEY_FIT_ACTION)){
                        int historyAction = resultData.getInt(Constants.KEY_FIT_ACTION);
                        if (historyAction == Constants.TASK_ACTION_READ_GOALS && (sUserId.length() > 0)){
                            for(int i=0; i < 3; i++){
                                String recKey = "KEY_ACT_REC" + i;
                                String recType = "KEY_ACT_TYPE" + i;
                                String recValue = "KEY_ACT_VALUE" + i;
                                if (resultData.containsKey(recKey)){
                                    String sKey = resultData.getString(recKey);
                                    String sValue = resultData.getString(recValue);
                                    try {
                                        Configuration config = new Configuration(sKey, sUserId, sValue, timeMs,null,null);
                                        List<Configuration> existingConfigs = mSessionViewModel.getConfiguration(config, sUserId);
                                        if (existingConfigs.size() == 0)
                                            mSessionViewModel.insertConfig(config);
                                        else
                                            mSessionViewModel.updateConfig(config);
                                    }catch(Exception e){
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                }
                            }
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
                                sKey += "_list";
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
                                sKey += "_list";
                                if (resultData.containsKey(sKey)) {
                                    ArrayList<WorkoutSet> sets = new ArrayList<>();
                                    sets.addAll(resultData.getParcelableArrayList(sKey));
                                     mSavedStateViewModel.setToDoSets(sets);
                                    CustomListFragment  customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY,  0, sUserId,sDeviceID);
                                }
                            }
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
                            customListFragment.setCancelable(true);
                            customListFragment.show(getSupportFragmentManager(), "Workout");
                        }else {
                            sKey += "_list";
                            if (resultData.containsKey(sKey)) {
                                ArrayList<Workout> workouts = new ArrayList<>();
                                workouts.addAll(resultData.getParcelableArrayList(sKey));
                                mSessionViewModel.setWorkoutList(workouts);
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY, 0, sUserId,sDeviceID);
                                customListFragment.setCancelable(true);
                                customListFragment.show(getSupportFragmentManager(), "Workouts");
                            }
                            sKey = WorkoutSet.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                WorkoutSet workoutSet = resultData.getParcelable(sKey);
                                ArrayList<WorkoutSet> sets = new ArrayList<>();
                                sets.add(workoutSet);
                                 mSavedStateViewModel.setToDoSets(sets);
                                CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sUserId,sDeviceID);
                                customListFragment.setCancelable(true);
                                customListFragment.show(getSupportFragmentManager(), "Sets");
                            } else {
                                sKey += "_list";
                                if (resultData.containsKey(sKey)) {
                                    ArrayList<WorkoutSet> sets = new ArrayList<>();
                                    sets.addAll(resultData.getParcelableArrayList(sKey));
                                     mSavedStateViewModel.setToDoSets(sets);
                                    CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_SET_HISTORY, 0, sUserId,sDeviceID);
                                    customListFragment.setCancelable(true);
                                    customListFragment.show(getSupportFragmentManager(), "Sets");
                                }
                            }
                        }

                    }
                }else
                    Log.d(LOG_TAG, "receiver " + resultCode);
            }
        };
        try{
            if (hasOAuthPermission(1)){
                mMessagesViewModel.setWorkType(action);
                mMessagesViewModel.setWorkInProgress(true);
                doCloudSyncJob(action,w, set,meta,resultReceiver);
            }else
                requestOAuthPermission(1);
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
*/
    private void setupListenersAndIntents(Context context){
        if (mCustomIntentReceiver == null) mCustomIntentReceiver = new CustomIntentReceiver(mResultReceiver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction(INTENT_DAILY);
        intentFilter.addAction(INTENT_EXER_RECOG);
        intentFilter.addAction(INTENT_RECOG);
        RoomActivity.this.registerReceiver(mCustomIntentReceiver, intentFilter);
        mCustomIntentReceiver.setRegistered(true);
        if (mIntentReceiver == null) mIntentReceiver = new AppIntentReceiver();
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(INTENT_ACTIVE_LOGIN);
        notifyFilter.addAction(INTENT_MESSAGE_TOAST);
        notifyFilter.addAction(INTENT_CLOUD_SYNC);
        notifyFilter.addAction(Constants.INTENT_PHONE_SYNC);
        notifyFilter.addAction(INTENT_QUIT_APP);
        notifyFilter.addAction(INTENT_HOME_REFRESH);
        notifyFilter.addAction(INTENT_INPROGRESS_RESUME);
        notifyFilter.addAction(INTENT_NETWORK_CHECK);
        notifyFilter.addAction(Constants.INTENT_TEMPLATE_START);
        notifyFilter.addAction(INTENT_ACTIVE_START);
        notifyFilter.addAction(INTENT_ACTIVE_STOP);
        notifyFilter.addAction(INTENT_ACTIVE_PAUSE);
        notifyFilter.addAction(INTENT_ACTIVE_RESUMED);
        notifyFilter.addAction(INTENT_ACTIVESET_START);
        notifyFilter.addAction(INTENT_ACTIVESET_STOP);
        notifyFilter.addAction(INTENT_WORKOUT_REPORT);
        notifyFilter.addAction(INTENT_WORKOUT_EDIT);
        notifyFilter.addAction(INTENT_WORKOUT_DELETE);
        notifyFilter.addAction(INTENT_SET_DELETE);
        notifyFilter.addAction(INTENT_SUMMARY_DAILY);
        notifyFilter.addAction(INTENT_SCHEDULE_TRIGGER);
        notifyFilter.addAction(INTENT_GOAL_TRIGGER);
        notifyFilter.addAction(Constants.INTENT_SETUP);
        notifyFilter.addAction(INTENT_LOCATION_UPDATE);
        notifyFilter.addAction(Constants.INTENT_TOTALS_REFRESH);
        RoomActivity.this.registerReceiver(mIntentReceiver, notifyFilter);
       // LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mIntentReceiver,notifyFilter);
    }

    private void restoreWorkoutVariables(){
        if (mSavedStateViewModel.getActiveWorkout().getValue() != null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() != null) mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
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
            if (localSDT == null) localSDT = mSessionViewModel.getTopSensorDailyTotal(sUserID);
            List<WorkoutSet> sets = new ArrayList<>();
            mWorkout.offline_recording = (isConnected ? 0 : 1);
            mWorkout.start = timeMs;
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            DateTuple setsTuple = mSessionViewModel.getWorkoutSetTupleByWorkoutID(mWorkout.userID,mWorkout.deviceID,mWorkout._id);
            if ((setsTuple.sync_count > 0)){
                sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                sets.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                mWorkoutSet = sets.get(0);
                mWorkoutSet.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
                if (mWorkout.setCount != sets.size()) mWorkout.setCount = sets.size();
                mWorkout.scoreTotal = !userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING) ? FLAG_NON_TRACKING : FLAG_PENDING;
            }
            else {
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
                sessionNewFromOld(sets);
            }
            createWorkoutMeta();
            mWorkoutMeta.start = mWorkout.start;
            // room inserts via viewModel - repository chain
            String sPartDay = Utilities.getPartOfDayString(timeMs);
            if (Utilities.isShooting(mWorkout.activityID)){
                mWorkoutMeta.description = sPartDay + ATRACKIT_SPACE + sWorkoutName + ATRACKIT_SPACE + mWorkout.setCount + getString(R.string.label_shoot_ends);
            }else
                mWorkoutMeta.description = sPartDay + ATRACKIT_SPACE + sWorkoutName;
            if (mWorkout.name == null && mWorkout.parentID == null) mWorkout.name = mWorkoutMeta.description;
            mWorkoutSet.start = mWorkout.start;
            mWorkoutSet.realElapsedStart = timeRealElapsed;   // system elapsed time at start held in move_mins until workout completed!
            if (mWorkoutSet.activityID != mWorkout.activityID) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }

            String sBPM = mMessagesViewModel.getDeviceBpmMsg().getValue();
            if (sBPM == null) mMessagesViewModel.getDevice2BpmMsg().getValue();
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
                if (mWorkoutSet.call_duration != null &&  mWorkoutSet.call_duration > 0)
                    snackMsg = getString(R.string.action_call);
                else
                if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0))
                    snackMsg = connectedMsg + ATRACKIT_SPACE+ mWorkoutSet.exerciseName;
                else
                    snackMsg = connectedMsg + ATRACKIT_SPACE+ mWorkoutSet.activityName;
                Intent intent = new Intent(RoomActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, snackMsg);
                startActivity(intent);
                if (mFirebaseAnalytics != null){
                    Bundle paramsB = new Bundle();
                    paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                }
            }
            else {
                // start of tennis game !
                if (mWorkout.activityID == Constants.WORKOUT_TYPE_TENNIS) {
                    String sPlayer1 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_1));
                    if (sPlayer1.length() == 0) sPlayer1 = getString(R.string.label_player_1);
                    String sPlayer2 = userPrefs.getPrefStringByLabel(getString(R.string.label_player_2));
                    if (sPlayer2.length() == 0) sPlayer2 = getString(R.string.label_player_2);
                      tennisGame = new TennisGame(sPlayer1, sPlayer2);
                }
                Intent intent = new Intent(RoomActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, snackMsg);
                startActivity(intent);
                if (mFirebaseAnalytics != null){
                    Bundle paramsB = new Bundle();
                    paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                }
            }
            if ((mWorkout.offline_recording == 0)) {
                doAsyncGoogleFitAction(TASK_ACTION_RECORD_START, mWorkout, mWorkoutSet, mWorkoutMeta); // start recording
                if (mWorkout.activityID != WORKOUT_TYPE_ATRACKIT) doAsyncGoogleFitAction(Constants.TASK_ACTION_START_SESSION, mWorkout, mWorkoutSet, mWorkoutMeta); // start session
                if (mSavedStateViewModel.getIsGym() && (mGoogleAccount != null)) doRegisterDetectionService(mGoogleAccount);
                // doRecordingWork(Constants.TASK_ACTION_START_SESSION);  // subscribe to sensor stuff
            } else
                if (!isConnected){
                    mWorkout.offline_recording = 1;
                    broadcastToast(getString(R.string.action_offline));
                }
            mSessionViewModel.insertWorkoutMeta(mWorkoutMeta);
            // room updates of final changes via viewModel - repository chain
            mWorkout.lastUpdated = timeMs;
            mWorkoutSet.lastUpdated = timeMs;
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
            }
            else
                sessionSetCurrentState(WORKOUT_LIVE);

            mWearableNavigationDrawer.getController().closeDrawer();
            if (mNavigationController != null) loadDataAndUpdateScreen();
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
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_PAUSE, resultData);
        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
            sendDataClientMessage(INTENT_ACTIVE_PAUSE, resultData);
        
        doPauseStopDialog();
    }
    private void sessionRepeatSet(){
        restoreWorkoutVariables();
        if (mWorkoutSet == null) return;
        int currentIndex = mSavedStateViewModel.getSetIndex(); //(mWorkoutSet.setCount == 0) ? 1: mWorkoutSet.setCount;
        mSessionViewModel.updateWorkoutSetSetCount(mWorkoutSet.workoutID,currentIndex,1);
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
        todo.add(mWorkoutSet);
        mSavedStateViewModel.setSetIndex(currentIndex);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        todo.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
        mSavedStateViewModel.setToDoSets(todo);
    }
    private void sessionNewFromOld(List<WorkoutSet> sets){
        if ((mWorkout == null) || (sets == null)) return;
        long timeMs = System.currentTimeMillis();
        long originalWorkoutID = mWorkout._id;
        List<WorkoutSet> list = new ArrayList<>(sets.size());
        mWorkout._id = timeMs;
        mWorkout.lastUpdated = timeMs;
        mSessionViewModel.insertWorkout(mWorkout);
        if (mWorkout.setCount > 0) {
            int defaultSet = 1;
            for (WorkoutSet set : sets) {
                set.workoutID = timeMs;
                set.userID = mWorkout.userID;
                set.deviceID = mWorkout.deviceID;
                if (set.scoreTotal == FLAG_BUILDING) set.scoreTotal = Constants.FLAG_PENDING;
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
                s.scoreTotal = FLAG_PENDING; s.last_sync = 0;
                s.device_sync = 0L;
                s.start = 0;s.end = 0;
                s.realElapsedEnd = 0L;
                s.realElapsedStart = 0L;
                s.pause_duration = 0;
                s.duration = 0;
                s.lastUpdated = timeMs;
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
        if  ((mWorkoutSet.setCount == 0 || mWorkoutSet.setCount == 1) && mWorkoutSet.start == 0){
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
            mSavedStateViewModel.setPauseStart(0);
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
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkoutSet.exerciseID));
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
        Intent intent = new Intent(RoomActivity.this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
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
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, snackMsg);
        startActivity(intent);
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

        if (mNavigationController != null) loadDataAndUpdateScreen();
        if (currentState != WORKOUT_LIVE) {  // new live
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
            int iSet = (mWorkoutSet.setCount == 0) ? 1: mWorkoutSet.setCount;
            if ((iSet > 1) && (mSavedStateViewModel.getToDoSetsSize() >= iSet)){
                WorkoutSet set = mSavedStateViewModel.getToDoSets().getValue().get(iSet-2);  // previous set
                mWorkoutSet.start = set.end + set.rest_duration;
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
        if (Utilities.isGymWorkout(mWorkoutSet.activityID)){
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
        //sessionSaveCurrentSet();

        Bundle resultData = new Bundle();
        resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
        resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
        resultData.putString(KEY_FIT_USER, mWorkout.userID);
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVESET_STOP, resultData);
        if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
            sendDataClientMessage(INTENT_ACTIVESET_STOP, resultData);
    }
    private void sessionSetCurrentState(int state){
        mSavedStateViewModel.setCurrentState(state);
        if (mSavedStateViewModel.getUserIDLive().getValue() == null){
            Log.w(LOG_TAG, "sessionSetCurrentState NO user leaving now");
            return;
        }
        String sUser = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        String sDevice = (sDeviceID == null || sDeviceID.length() == 0) ? appPrefs.getDeviceID() : sDeviceID;
        String sLastUpdated = Long.toString(System.currentTimeMillis());
        if (sUserID != null && (sUserID.length() > 0)){
            List<Configuration> list = mSessionViewModel.getConfigurationLikeName(MAP_CURRENT_STATE, sUserID);
            Configuration config = null;
            if (list != null && (list.size() > 0)) config = list.get(0);
            if (config != null) {
                if (config.longValue != (long) state) {
                    config.longValue = state;
                    if ((state == WORKOUT_LIVE || state == WORKOUT_PAUSED || state == WORKOUT_CALL_TO_LINE)
                            && (mWorkout != null && mWorkout._id > 2))
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
                bMeta = true;
                createWorkoutMeta();
                mWorkoutMeta.end = mWorkoutSet.end;
            }

            mWorkout.duration = (mWorkout.end - mWorkout.start);
            sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutSet.workoutID,mWorkoutSet.userID,mWorkoutSet.deviceID);
           // if (mWorkout.setCount != sets.size()) mWorkout.setCount = sets.size();
            // do the total score
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

            if (mSavedStateViewModel.getBPM() != null) {
                String sBPM = mMessagesViewModel.getBpmMsg().getValue();
                if (sBPM != null) {
                    DailyCounter bpmCounter = mSavedStateViewModel.getBPM();
                  //  Log.e(LOG_TAG, bpmCounter.toString());
                    mSavedStateViewModel.setBPM(null);
                    Log.d(LOG_TAG, bpmCounter.toJSONString());
                }
            }
            if (mWorkoutMeta != null) mWorkoutMeta.duration = mWorkout.duration;
            if (Utilities.isShooting(mWorkout.activityID)){
                int totalScore = 0; String totalScoreCard = ATRACKIT_EMPTY; String totalXYCard = ATRACKIT_EMPTY;
                if (sets != null)
                for(WorkoutSet set: sets){
                    int scoreVal = 0;
                    try {
                        String[] items = set.score_card.split(Constants.SHOT_DELIM);
                        List<String> mScoreCard = new ArrayList<>(Arrays.asList(items));
                        for (String score : mScoreCard) {
                            if (score.equals(Constants.SHOT_X))
                                scoreVal += 10;
                            else {
                                try {
                                    scoreVal += Integer.parseInt(score);
                                } catch (Exception e) {
                                    scoreVal += 0;
                                }
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
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
            if ((mWorkout.offline_recording == 0) && isConnected)
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

            if ((mWorkout.offline_recording == 0)) {
                doAsyncGoogleFitAction(TASK_ACTION_RECORD_END, mWorkout, mWorkoutSet, mWorkoutMeta); // start recording
                if (mWorkout.activityID != WORKOUT_TYPE_ATRACKIT) doAsyncGoogleFitAction(Constants.TASK_ACTION_STOP_SESSION, mWorkout, mWorkoutSet, mWorkoutMeta); // stop session
            }else {
                if (isConnected)
                    startCloudPendingSyncAlarm();
                else
                    startNetworkCheckAlarm(RoomActivity.this);
            }
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
            Intent intent = new Intent(this, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                    getString(R.string.label_session_completed) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(mWorkout.duration));
            startActivity(intent);

            if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
            if (isConnected){
                Bundle paramsA = new Bundle();
                paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_STOP);
                paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
            }
            sessionSetCurrentState(WORKOUT_COMPLETED);
            startCloudPendingSyncAlarm();
            // now notify
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVE_STOP, resultData);
            if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)) vibrate(5);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                sendDataClientMessage(INTENT_ACTIVE_STOP, resultData);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                    refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                    refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mMessagesViewModel.addLiveIntent(refreshIntent);

                }
            }, 2000);
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG,"sessionStop failed " + e.getMessage());
        }
   }
    //TODO: write simple sessionReport
    private void sessionReport(){
        return;
    }
    private void sessionArcheryBuild(){
        long timeMs = System.currentTimeMillis();
        List<WorkoutSet> todo =  new ArrayList<>();
        int iCountSetsToAdd = mWorkoutMeta.setCount;
        boolean useTimedRest = userPrefs.getTimedRest();
        for(int index = 1; index <= iCountSetsToAdd; index++){
            WorkoutSet set = new WorkoutSet(mWorkout);
            set._id  = timeMs + (index * 50);
            set.start = 0L;
            set.end = 0L;
            if (useTimedRest) {
                set.rest_duration = mWorkoutMeta.rest_duration;
                set.call_duration = mWorkoutMeta.call_duration;
                set.goal_duration = mWorkoutMeta.goal_duration;
            }
            set.repCount = 0;
            set.setCount = index;
            set.scoreTotal = FLAG_PENDING;
            String shotEntry = Constants.SHOT_DELIM + Constants.SHOT_SCORE;
            String shotXY = Constants.SHOT_DELIM + Constants.SHOT_XY;
            for (int shotIndex = 1; shotIndex <= mWorkoutMeta.shotsPerEnd; shotIndex++){
                if ((set.score_card != null) && (set.score_card.length() > 0)) {
                    set.score_card += shotEntry;
                    set.per_end_xy += shotXY;
                } else{
                    set.score_card = Constants.SHOT_SCORE;
                    set.per_end_xy = Constants.SHOT_XY;
                }
                if ((mWorkoutMeta.per_end_xy != null) &&(mWorkoutMeta.per_end_xy.length() > 0)){
                    mWorkoutMeta.score_card += shotEntry;
                    mWorkoutMeta.per_end_xy += shotXY;
                }else{
                    mWorkoutMeta.score_card = Constants.SHOT_SCORE;
                    mWorkoutMeta.per_end_xy = Constants.SHOT_XY;
                }
            }
            todo.add(set);
        }
        mSavedStateViewModel.setDirtyCount(0);
        mSavedStateViewModel.setToDoSets(todo);
        mWorkoutSet = todo.get(0);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
    }
    // add to the to do sets
    private void sessionBuild(boolean bAddNext){
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        String sUserId = mWorkout.userID;
        if ((sUserId == null) || (sUserId.length() == 0)) return;
        long timeMs = System.currentTimeMillis();
        if (Utilities.isShooting(mWorkout.activityID)) {
            mWorkoutSet.repCount = 0;  // this is the score later!
            sessionArcheryBuild();
            return;
        }
        boolean bIsGym = Utilities.isGymWorkout(mWorkoutSet.activityID);
     //   Long existingRegionID = mWorkoutSet.regionID;
     //   String existingRegionName = mWorkoutSet.regionName;
        List<WorkoutSet> todoList =  mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
        int iCountSetsToAdd = mSavedStateViewModel.getSetsDefault();
        int currentSize = (todoList == null) ? 0 : todoList.size();
        int currentIndex = 1;
        for (WorkoutSet s : todoList){
            if (s.start == 0) break;
            currentIndex++;
        }
        String sToast = "Added " + iCountSetsToAdd + Constants.ATRACKIT_SPACE + getString(R.string.label_set);
        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)){
            sToast += " of " + mWorkoutSet.exerciseName;
        }
        int defaultRest = userPrefs.getWeightsRestDuration();
        int defaultReps = mSavedStateViewModel.getRepsDefault();
        if (bIsGym && (mWorkoutSet.repCount != null) && (mWorkoutSet.repCount > 0)
                && (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0F))
            mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
        // only if build
        if (bIsGym && (mWorkoutSet.scoreTotal == FLAG_BUILDING)) {
            for (int index = 1; index <= iCountSetsToAdd; index++) {
                WorkoutSet set = new WorkoutSet(mWorkoutSet);
                set._id = timeMs + (index * 50);
                set.start = 0L;
                set.end = 0L;
                set.repCount = defaultReps;
                set.setCount = currentSize + index;
                set.duration = 0;
                set.rest_duration = (long)defaultRest;
                set.lastUpdated = timeMs;
                set.scoreTotal = FLAG_PENDING;
                set.realElapsedStart = null;
                set.realElapsedEnd = null;
                set.startBPM = null;
                set.endBPM = null;
                mSessionViewModel.insertWorkoutSet(set);
            }
            broadcastToast(sToast);
            // set up the next build set
            if (bAddNext) {
                Bodypart bpSource = (((mWorkoutSet != null) && (mWorkoutSet.bodypartID != null)) ? mSessionViewModel.getBodypartById(mWorkoutSet.bodypartID) : null);
                if ((bpSource != null) && (bpSource.parentID != null)){
                    mWorkoutSet.bodypartID = bpSource.parentID;
                    mWorkoutSet.bodypartName = bpSource.parentName;
                }
                mWorkoutSet.setCount = currentSize + iCountSetsToAdd + 1;
                mWorkoutSet.repCount = defaultReps;
                mWorkoutSet.resistance_type = null;
                mWorkoutSet.exerciseID = null;
                mWorkoutSet.exerciseName = ATRACKIT_EMPTY;
                mWorkoutSet.per_end_xy = ATRACKIT_EMPTY;
                mWorkoutSet.weightTotal = 0f;
                mWorkoutSet.wattsTotal = 0f;
                mWorkoutSet.scoreTotal = Constants.FLAG_BUILDING;
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        else
            sessionSaveCurrentSet();

        todoList = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id,mWorkout.userID,mWorkout.deviceID);
        if ((todoList != null) && (todoList.size() > 0) && (mWorkout.setCount != todoList.size())) {
            mWorkout.setCount = todoList.size();
            mSessionViewModel.updateWorkout(mWorkout);
            mSavedStateViewModel.setToDoSets(todoList);
        }
        mSavedStateViewModel.setDirtyCount(0);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
    }


    private void sessionGoogleCurrentSet(WorkoutSet set){
        if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        Bundle paramsB = new Bundle();
        if (mFirebaseAnalytics != null){
            // completed!
            if ((set.start > 0) && (set.end > 0)) {
                paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_STOP);
                if ((set.exerciseName != null) && (set.exerciseName.length() > 0)) {
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(set.exerciseID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.exerciseName);
                }else{
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, "-" + Long.toString(set.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.activityName);
                }
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
                if ((mWorkout.offline_recording == 0) && (set.last_sync == 0L)){
                    try {
                        if (Utilities.isGymWorkout(set.activityID) && (set.scoreTotal != FLAG_NON_TRACKING))
                            doAsyncGoogleFitAction(Constants.TASK_ACTION_EXER_SEGMENT, mWorkout, set, mWorkoutMeta);
                        else
                            doAsyncGoogleFitAction(Constants.TASK_ACTION_ACT_SEGMENT, mWorkout, set, mWorkoutMeta);

                    }catch(NullPointerException ne){
                        FirebaseCrashlytics.getInstance().recordException(ne);
                        String sMsg =  ne.getMessage();
                        if (sMsg != null) Log.d(LOG_TAG,sMsg);
                    }
                }
            }
            if ((set.start > 0) && (set.end == 0)){
                paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
                if ((set.exerciseName != null) && (set.exerciseName.length() > 0)) {
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(set.exerciseID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.exerciseName);
                }else{
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, "-" + Long.toString(set.activityID));
                    paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, set.activityName);
                }
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
            }

        }
    }
    private void sessionSaveCurrentSet(){
         try{
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet == null) return;
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if (!mWorkoutSet.isValid(false)) return;  // only save to DB built sets
            if ((mWorkoutSet.workoutID != mWorkout._id) && (mWorkout._id > 2)) mWorkoutSet.workoutID = mWorkout._id;
            mWorkoutSet.lastUpdated = System.currentTimeMillis();
            WorkoutSet testSet = mSessionViewModel.getWorkoutSetById(mWorkoutSet._id, mWorkoutSet.userID, mWorkoutSet.deviceID);
            boolean bNew = (testSet == null);
            if (!bNew){
                mSessionViewModel.updateWorkoutSet(mWorkoutSet);
            }else{
                mSessionViewModel.insertWorkoutSet(mWorkoutSet);
            }
            mSavedStateViewModel.setDirtyCount(0);


            // if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
            // if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_ACTIVESET_SAVED, resultData);

            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                Bundle resultData = new Bundle();
                resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
                resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                resultData.putString(KEY_FIT_USER, mWorkout.userID);
                sendDataClientMessage(Constants.INTENT_ACTIVESET_SAVED, resultData);
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    private void sessionStartNextSet(){
        try {
            restoreWorkoutVariables();
            if (mWorkout == null) return;
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet == null) createWorkoutSet();
            int currentSetIndex = (mWorkoutSet.setCount == 0) ? 1: mWorkoutSet.setCount;
            long timeMs = System.currentTimeMillis();
            long lastRestDuration = mWorkoutSet.rest_duration;
            boolean bNew = false;
            mWearableNavigationDrawer.getController().closeDrawer();
            // if WORKOUT_LIVE ensure tidy up
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
               // if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
                if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_ACTIVESET_SAVED, resultData);

                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                    sendDataClientMessage(Constants.INTENT_ACTIVESET_SAVED,resultData);
                if (((mWorkout.offline_recording == 0)  && mReferenceTools.isNetworkConnected()) && (mWorkoutSet.last_sync == 0))
                    sessionGoogleCurrentSet(mWorkoutSet);  // no more changes to therefore Google it!
            }

            currentSetIndex++; // next set
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
                mWorkoutSet.lastUpdated = timeMs;
                mSessionViewModel.updateWorkoutSet(mWorkoutSet);
            }else{
                WorkoutSet oldSet = mWorkoutSet;
                mWorkoutSet = new WorkoutSet(oldSet);
                mWorkoutSet._id = timeMs;
                mWorkoutSet.workoutID = mWorkout._id;
                mWorkoutSet.setCount = currentSetIndex;
                mWorkoutSet.start =timeMs; mWorkoutSet.end=0L;
                mWorkoutSet.last_sync = 0; mWorkoutSet.device_sync = null;
                mWorkoutSet.realElapsedStart = SystemClock.elapsedRealtime();
                mWorkoutSet.rest_duration = lastRestDuration;
                mWorkoutSet.duration = 0; mWorkoutSet.pause_duration =0;
                mWorkoutSet.exerciseName = ATRACKIT_EMPTY;
                mWorkoutSet.per_end_xy = ATRACKIT_EMPTY;
                mWorkoutSet.exerciseID = null; mWorkoutSet.scoreTotal = FLAG_PENDING;
                mWorkoutSet.lastUpdated = timeMs;
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
            // TODO: add a worker to get steps and update active set information
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
            if (mWorkoutSet != null) mSavedStateViewModel.setSetIndex(currentSetIndex);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if (bNew) mSavedStateViewModel.setToDoSets(sets);
            if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
            Bundle paramsB = new Bundle();
            paramsB.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_SEGMENT_START);
            paramsB.putInt(FirebaseAnalytics.Param.INDEX, mWorkoutSet.setCount);
            if (Utilities.isGymWorkout(mWorkout.activityID) && (mWorkoutSet.exerciseID != null)) {
                paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkoutSet.exerciseID));
                paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkoutSet.exerciseName);
            } else {
                paramsB.putString(FirebaseAnalytics.Param.ITEM_ID, "-" + Long.toString(mWorkoutSet.activityID));
                paramsB.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkoutSet.activityName);
            }
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsB);
            Bundle resultData = new Bundle();
            resultData.putParcelable(Workout.class.getSimpleName(), mWorkout);
            resultData.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
            resultData.putString(KEY_FIT_USER, mWorkout.userID);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)
                    && (Utilities.getRingerMode(RoomActivity.this) == AudioManager.RINGER_MODE_NORMAL)) playMusic();
            if (userPrefs.getPrefByLabel(USER_PREF_USE_VIBRATE)
                    && (Utilities.getRingerMode(RoomActivity.this) != AudioManager.RINGER_MODE_SILENT)) vibrate(1);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(INTENT_ACTIVESET_START, resultData);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE))
                sendDataClientMessage(INTENT_ACTIVESET_START, resultData);

            //sessionSaveCurrentSet();
            sessionResume();
        }catch(Exception e){
            String sMsg = e.getMessage();
            FirebaseCrashlytics.getInstance().recordException(e);
            if (sMsg != null) Log.e(LOG_TAG, sMsg);
        }
    }


    private void showOpenDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_open_workout,null,false);
        MaterialButton btnTemplate = view.findViewById(R.id.btn_open_template);
        MaterialButton btnHistory = view.findViewById(R.id.btn_open_history);
        builder.setIcon(R.drawable.ic_folder_open_white);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        btnTemplate.setOnClickListener(v -> {
            doOpenTemplates();
            alertDialog.dismiss();
        });
        btnHistory.setOnClickListener(v -> {
            doOpenWorkouts();
            alertDialog.dismiss();
        });
        alertDialog.show();
    }
    /**
     * doOpenWorkouts() = find workouts by dates
     */
    private void doOpenWorkouts(){
        Context context = getApplicationContext();
        if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context,sUserID);
        long startTime = userPrefs.getLastUserOpen();
        long endTime = (startTime > 0) ? startTime + (TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1))
                : Utilities.getTimeFrameEnd(Utilities.TimeFrame.BEGINNING_OF_DAY);
        if (startTime == 0){
            startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            endTime = Utilities.getTimeFrameEnd(Utilities.TimeFrame.BEGINNING_OF_DAY);
            userPrefs.setLastUserOpen(startTime);
        }
        ArrayList<Workout> workouts = new ArrayList<>(mSessionViewModel.getWorkoutsByTimesNow(sUserID, sDeviceID, startTime, endTime));
        if (workouts.size() == 0) broadcastToast("None found - set date");

        mSessionViewModel.setWorkoutList(workouts);
        CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY, 0,ATRACKIT_EMPTY, sUserID, sDeviceID);
        customListFragment.setCancelable(true);
        customListFragment.show(getSupportFragmentManager(), "Workout");
    }
    /**
     * doOpenTemplates() = find templates
     */

    private void doOpenTemplates(){
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        if (userPrefs == null) userPrefs = UserPreferences.getPreferences(getApplicationContext(),sUserId);
        long workoutID = userPrefs.getLastUserTemplate();
        ArrayList<Workout> workouts = new ArrayList<>(mSessionViewModel.getWorkoutTemplates(sUserId));
        if (workouts.size() == 0) broadcastToast("No templates found");
        mSessionViewModel.setWorkoutList(workouts);
        CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_TEMPLATES, workoutID,ATRACKIT_EMPTY, sUserId, sDeviceID);
        customListFragment.setCancelable(true);
        customListFragment.show(getSupportFragmentManager(), "Templates");
    }

    private void doGoalsRefresh(){
        if (!hasOAuthPermission(2))
            requestOAuthPermission(2);
        else
        new Handler().postDelayed(() -> {
       //     if (mWorkout == null) {
                Workout w = new Workout();
                w.userID = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
                w.deviceID = sDeviceID;
                w.start = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_YEAR);
                w.end = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                doAsyncGoogleFitAction(Constants.TASK_ACTION_READ_GOALS, w, new WorkoutSet(w), null);
        //    }else
        //        doAsyncGoogleFitAction(Constants.TASK_ACTION_READ_GOALS, mWorkout, mWorkoutSet, null);
            }, 1500L);
    }

    private void doAlertDialogMessage(String sMessage){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
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
        });
        btnNegative.setVisibility(View.GONE);
        alertDialog.show();
    }
  /*  private void doCloudHistory(String sUserID, long startTime,long  endTime){
        Data.Builder builder = new Data.Builder();
        builder.putInt(FitnessHistoryWorker.ARG_ACTION_KEY, Constants.TASK_ACTION_READ_SESSION); // all
        builder.putLong(FitnessHistoryWorker.ARG_START_KEY, startTime);
        builder.putLong(FitnessHistoryWorker.ARG_END_KEY, endTime);
        builder.putString(KEY_FIT_USER, sUserID);
        Data inputData = builder.build();

        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest1 =
                new OneTimeWorkRequest.Builder(FitnessHistoryWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
       OneTimeWorkRequest oneTimeWorkRequest2 =
                new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();

        Operation operation = mWorkManager.beginWith(oneTimeWorkRequest1).enqueue();
        operation.getState().observe(RoomActivity.this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                if (state instanceof Operation.State.SUCCESS) {
                    Log.d(LOG_TAG, "successful state of first worker ");
*//*                    Operation operation2 = mWorkManager.beginWith(oneTimeWorkRequest2).enqueue();
                    operation2.getState().observe(RoomActivity.this, new Observer<Operation.State>() {
                                @Override
                                public void onChanged(Operation.State state) {
                                    if (state instanceof Operation.State.SUCCESS) {
                                       Log.d(LOG_TAG, "successful state of 2nd worker ");
                                       Intent finishedIntent = new Intent(Constants.INTENT_CLOUD_META);
                                       finishedIntent.putExtra(KEY_FIT_TYPE, 0);  //internal
                                       finishedIntent.putExtra(KEY_FIT_WORKOUTID, startTime);
                                       mMessagesViewModel.addLiveIntent(finishedIntent);
                                    }
                                }
                            });*//*
                }
            }
        });
    }*/
  private void doCloudHistory(String sUserID, long startTime,long endTime){
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
          doAsyncGoogleFitAction(TASK_ACTION_READ_CLOUD, mWorkout, mWorkoutSet, null);
      }
  }
    private void doPendingExercisesCheck(String sUserId){
        DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
        if (exerciseTuple.sync_count > 0){
            Bitmap ic_launcher = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            int notificationID = Constants.NOTIFICATION_MAINTAIN_ID;
            long iconID = R.drawable.noti_white_logo;
            int activityIcon = R.drawable.ic_gym_equipmemt;
            String sTitle = exerciseTuple.sync_count + " exercises require setup for analysis";
            String sContent = Constants.ATRACKIT_EMPTY;
            String sTime = SimpleDateFormat.getTimeInstance().format(mCalendar.getTime());
            String sChannelID = MAINTAIN_CHANNEL_ID;

            List<Exercise> pendingList = mSessionViewModel.getPendingExercises();
            Intent notificationIntent = new Intent(getApplicationContext(), RoomActivity.class);
            notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.setAction(Constants.INTENT_EXERCISE_PENDING);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            notificationIntent.putExtra(KEY_FIT_TYPE, 1);  // pending exercise lookup for ExerciseActivity
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
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(RoomActivity.this);
                // Deliver the notification.
                notificationManager.notify(notificationID, notifyBuilder.build());
            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage());
                FirebaseCrashlytics.getInstance().recordException(e);
            }

        }else
            Log.w(LOG_TAG,"Zero pending exercises to process");
    }
    private void doBindSensors(){
        Context context = getApplicationContext();
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(context);
        long lastSensorBind = appPrefs.getLastSensorBind(0);
        long timeMs = System.currentTimeMillis();
        // ensure we dont do this too often
        Log.e(LOG_TAG, "doBindSensors");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (appPrefs.getUseLocation()) {
                if (mLocationListener == null){
                    bindLocationListener();
                    BoundFusedLocationClient.standardInterval();
                }
                BoundFusedLocationClient.doCurrentUpdate();
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), "com.google.android.gms.permission.ACTIVITY_RECOGNITION")
                        == PackageManager.PERMISSION_GRANTED) {
                    if (Utilities.hasSensorDevicesPermission(context)) {
                        if (appPrefs.getUseSensors() && hasOAuthPermission(0)) {
                            bindSensorListener(0, -1);
                            bindSensorListener(1, -1);
                            bindSensorListener(2, -1);
                            bindFitSensorListener();
                        } else if (!hasOAuthPermission(0)) requestOAuthPermission(0);
                    } else {
                        startSplashActivityForResult(INTENT_PERMISSION_SENSOR);
                    }
                }else
                    startSplashActivityForResult(INTENT_PERMISSION_RECOG);
            }
            else{
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (Utilities.hasSensorDevicesPermission(context)) {
                        if (appPrefs.getUseSensors() && hasOAuthPermission(0)) {
                            bindSensorListener(0, -1);
                            bindSensorListener(1, -1);
                            bindSensorListener(2, -1);
                            bindFitSensorListener();
                        }else
                            if (!hasOAuthPermission(0)) requestOAuthPermission(0);
                    } else{
                        startSplashActivityForResult(INTENT_PERMISSION_SENSOR);
                    }
                }else{
                    startSplashActivityForResult(INTENT_PERMISSION_RECOG);
                }
            }
        }else{
            startSplashActivityForResult(Constants.INTENT_PERMISSION_LOCATION);
        }
    }

    private void doRegisterDetectionService(GoogleSignInAccount gsa){
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
    private void doUnRegisterDetectionService(GoogleSignInAccount gsa){
        Context context = getApplicationContext();
        Intent intentDetected = new Intent(context, ExerciseDetectedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intentDetected,PendingIntent.FLAG_UPDATE_CURRENT);
        Task<Void> unregisterTask = Fitness.getHistoryClient(context, gsa).unregisterDataUpdateListener(pendingIntent);
        unregisterTask.addOnCompleteListener(task -> Log.d(LOG_TAG, "unregistered exercise listener " + task.isSuccessful()));
    }

    /*
        doStartUpJobs -

     */
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
            if ((sUserId == null) || (sUserId.length() == 0)){
                if (appPrefs.getLastUserID().length() == 0) return;
            }
            mCalendar.setTimeInMillis(previousTimeMs);
            int prevDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
            timeMs = System.currentTimeMillis();
            mCalendar.setTimeInMillis(timeMs);
            int iDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
            if (!appPrefs.getAppSetupCompleted()) return;
            long lastDaily = appPrefs.getLastDailySync();
            long dailyInterval = appPrefs.getDailySyncInterval();
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
            int currentState = mSavedStateViewModel.getState();
            Log.e(LOG_TAG, "running startup job state now " + currentState);
            if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)) {
                Log.w(LOG_TAG, "startup current state not live " + currentState + " interval " + dailyInterval + " last " + Utilities.getTimeString(lastDaily));
                if (((dailyInterval > 0) && ((lastDaily == 0) || (timeMs - lastDaily) > dailyInterval))
                        || sAction.equals(Constants.INTENT_DAILY) && bConnected) {
                    Intent mIntent = new Intent(getApplicationContext(), DailySummaryJobIntentService.class);
                    mIntent.putExtra(Constants.KEY_FIT_USER, mGoogleAccount.getId());
                    mIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceId);
                    mIntent.putExtra(Constants.KEY_FIT_TYPE, 1);  // force
                    Log.w(LOG_TAG, "startup force Daily ");
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
                }
                Log.w(LOG_TAG, "getting current totals");
                Data.Builder builder = new Data.Builder();
                builder.putString(KEY_FIT_USER, mGoogleAccount.getId());
                builder.putString(KEY_FIT_DEVICE_ID, sDeviceId);
                if ((dailyInterval > 0) && ((timeMs - previousTimeMs) > dailyInterval))
                    if (prevDOY < iDOY)
                    builder.putInt(KEY_FIT_TYPE, 2); // everything
                else
                    builder.putInt(KEY_FIT_TYPE, 1); // UDT
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(CurrentUserDailyTotalsWorker.class)
                                .setInputData(builder.build())
                                .build();
                if (mWorkManager == null)
                    mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(workRequest);
                if (bConnected) {
                    new Handler().postDelayed(() -> {
                        Log.e(LOG_TAG, "startupJob connected so startCloudPendingSyncAlarm & Daily");
                        if (appPrefs.getLastSyncInterval() > 0) startCloudPendingSyncAlarm();
                        if (appPrefs.getDailySyncInterval() > 0) startDailySummarySyncAlarm();
                    },10000);
                }
                if (currentState != WORKOUT_INVALID) {
                    // this will trigger the sensors setup via state change to invalid
                    new Handler().postDelayed(() -> {
                        Log.e(LOG_TAG, "currentState is not INVALID about to INTENT_HOME_REFRESH");
                        Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                        refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                        refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_INVALID);
                        if (sUserID != null) refreshIntent.putExtra(KEY_FIT_USER, sUserID);
                        refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        mMessagesViewModel.addLiveIntent(refreshIntent);
                    }, 500);
                }
                // if setup and viable && userPrefs.getReadDailyPermissions()
                if ((appPrefs.getAppSetupCompleted() && ((userPrefs != null)
                        && userPrefs.getReadSensorsPermissions())
                        && (sDeviceId.length() > 0)
                        && (mGoogleAccount != null))) {
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
                        if (phoneNodeId == null || (phoneNodeId.length() == 0))
                            phoneNodeId = appPrefs.getLastNodeID();
                        if (configDevice1 != null) {
                            DataMap dataMap = new DataMap();
                            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                            Log.w(LOG_TAG, "sending direct COMM_TYPE_REQUEST_INFO");
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
                            if (phoneNodeId == null || (phoneNodeId.length() == 0))
                                sendCapabilityMessage(WEAR_CAPABILITY_NAME, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                            else
                                sendMessage(phoneNodeId, MESSAGE_PATH_PHONE_SERVICE, dataMap);
                        }else {

                            new Handler().postDelayed(() -> {
                                Log.w(LOG_TAG, "starting phone sync alarm");
                                startPhonePendingSyncAlarm();
                            }, 3000);
                        }
                    }
                    // check for new Daily
                    // not live so get daily stats and cloud sync
                } else {
                    if (!userPrefs.getReadDailyPermissions()) {
                        if (!hasOAuthPermission(0))
                            requestOAuthPermission(0);
                        else
                            userPrefs.setReadDailyPermissions(true);
                    }
                    if (!userPrefs.getReadSensorsPermissions())
                        startSplashActivityForResult(INTENT_PERMISSION_SENSOR);
                }
            } // [end of - not WORKOUT_LIVE]
        }
    }

    private void doPauseStopDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_pausestop,null,false);
        TextView title = view.findViewById(R.id.action_set_title);
        //title.setText("User Goals");
        MaterialButton btnContinue = view.findViewById(R.id.btn_confirm_paused);
        MaterialButton btnStop = view.findViewById(R.id.btn_confirm_stop);
        Chronometer chronometerConfirm = view.findViewById(R.id.chronometer_confirm_pause);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        if (title != null)
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
    private void showSensorsDialog(){
        Context context = getApplicationContext();
        Resources res = context.getResources();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
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


    private void showGoalsDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
        GoalListAdapter goalsAdapter = new GoalListAdapter(getApplicationContext());
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        List<Configuration> configurationList = mSessionViewModel.getConfigurationLikeName(DataType.TYPE_HEART_POINTS.getName(), sUserId);
        if (configurationList.size() > 0){
            Configuration conf = configurationList.get(0);
            String sHeartPts = mMessagesViewModel.getHeartPtsMsg().getValue();
            if (sHeartPts == null) sHeartPts = Long.toString(0);
            conf.longValue =Long.parseLong(sHeartPts);
            goalsAdapter.AddGoal(conf);
        }
        configurationList.clear();
        configurationList =  mSessionViewModel.getConfigurationLikeName(DataType.TYPE_MOVE_MINUTES.getName(), sUserId);
        if (configurationList.size() > 0){
            String sTemp = mMessagesViewModel.getMoveMinsMsg().getValue();
            if (sTemp == null) sTemp = Long.toString(0);
            Configuration conf = configurationList.get(0);
            conf.longValue =(Long.parseLong(sTemp));
            goalsAdapter.AddGoal(conf);

        }
        configurationList.clear();
        configurationList = mSessionViewModel.getConfigurationLikeName(DataType.TYPE_STEP_COUNT_DELTA.getName(), sUserId);
        if (configurationList.size() > 0){
            Configuration conf = configurationList.get(0);
            String sTemp = mMessagesViewModel.getStepsMsg().getValue();
            if (sTemp == null) sTemp = Long.toString(0);
            conf.longValue = Long.parseLong(sTemp);
            goalsAdapter.AddGoal(conf);
        }
        int itemCount = goalsAdapter.getItemCount();
        if (itemCount == 0){  // no goals indicator
            Configuration configuration = new Configuration(Constants.ATRACKIT_ATRACKIT_CLASS,sUserId, ATRACKIT_EMPTY,0L,null,null);
            goalsAdapter.AddGoal(configuration);
            doGoalsRefresh();
            broadcastToast("checking goals on cloud");
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_customlist,null,false);
        TextView title = view.findViewById(R.id.activity_title);
        title.setText("User Goals");
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
    private void showCreditsDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RoomActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_customlist_alert,null,false);
        TextView title = view.findViewById(R.id.alert_title);
        ListView listView = view.findViewById(R.id.list_view);
// Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.credits_name_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listView.setAdapter(adapter);

        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sURL;
                if (position == 0){
                    sURL = "https://flaticon.com/";
                }else{
                    sURL = "https://ulysses.com.au/";
                }
                if (phoneNodeId == null || phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && (phoneNodeId.length() > 0)){
                    DataMap dataMap = new DataMap();
                    dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_URL_VIEW);
                    dataMap.putString(Constants.KEY_FIT_USER, sUserID);
                    dataMap.putString(KEY_FIT_VALUE, sURL);
                    sendMessage(phoneNodeId,Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                    if (alertDialog != null) alertDialog.dismiss();
                }else{
                    broadcastToast(sURL);
                }

            }
        });
        alertDialog.show();
    }
    private void doSetupUserConfig(String sUserID){
        if (sUserID.length() == 0) return;
        String sKey = Constants.MAP_HISTORY_RANGE;
        Configuration configuration = new Configuration(sKey, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, Long.toString(0), Long.toString(0));
        mSessionViewModel.insertConfig(configuration);

        final Device device = Device.getLocalDevice(getApplicationContext());
        Wearable.getNodeClient(this).getLocalNode().addOnSuccessListener(node -> {
            Configuration c = new Configuration(Constants.KEY_DEVICE1, sUserID,node.getId(),System.currentTimeMillis(),device.getModel(),device.getUid());
            mSessionViewModel.insertConfig(c);
        }).addOnFailureListener(e -> {
            Configuration c = new Configuration(Constants.KEY_DEVICE1, sUserID,ATRACKIT_EMPTY,System.currentTimeMillis(),device.getModel(),device.getUid());
            mSessionViewModel.insertConfig(c);
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

    private void doDailySummaryJob() {
        long delay_sync = appPrefs.getDailySyncInterval();
        int currentState = mSavedStateViewModel.getState();
        String userId = (mGoogleAccount != null) ? mGoogleAccount.getId() : sUserID;
        if ((userPrefs != null) && !userPrefs.getReadDailyPermissions()){
            if (hasOAuthPermission(0)){
                userPrefs.setReadDailyPermissions(true);
            }else {
                requestOAuthPermission(0);
                return;
            }
        }

        if ((delay_sync > 0) && (currentState != WORKOUT_LIVE
                && currentState != WORKOUT_PAUSED && currentState != WORKOUT_PENDING))
            CustomIntentReceiver.setAlarm(getApplicationContext(),true, delay_sync, userId, (Parcelable)mGoogleAccount);
        else
            startDailySummarySyncAlarm();
    }
    private void doHomeFragmentRefresh(){
        final Context context = getApplicationContext();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
        for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
            if (frag.isVisible() && (frag instanceof RoomFragment)) {
                Log.w(LOG_TAG, "doHomeFragmentRefresh");
                new Handler(Looper.getMainLooper()).post(() ->{
                    ((RoomFragment) frag).refreshPersonalImage();
                    ((RoomFragment) frag).doObserveWorkoutSet(mWorkoutSet,context);
                    ((RoomFragment) frag).refreshSummaryData();
                });            }
        }

    }
    /**
     doCloudSyncJob - our DB populate from cloud job
     **/
    private void doCloudSyncJob(int action, Workout workout, WorkoutSet set, WorkoutMeta meta, ResultReceiver resReceiver){
        if (!hasOAuthPermission(5))
            requestOAuthPermission(5);
        else {
            int currentState = mSavedStateViewModel.getState();
            if (currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED && currentState != WORKOUT_PENDING) {
                long startTime = workout.start;
                if (startTime == 0)
                    startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                long endTime = workout.end;
                if (endTime == 0) endTime = System.currentTimeMillis();
                Intent mIntent = new Intent(getApplicationContext(), FitSyncJobIntentService.class);
                mIntent.putExtra(Constants.KEY_FIT_ACTION, action);
                mIntent.putExtra(Constants.MAP_START, startTime);
                mIntent.putExtra(Constants.MAP_END, endTime);
                mIntent.putExtra(Constants.KEY_RESULT, resReceiver);
                mIntent.putExtra(KEY_FIT_USER, workout.userID);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, workout.deviceID);
                mIntent.putExtra(Workout.class.getSimpleName(), workout);
                if (set != null) mIntent.putExtra(WorkoutSet.class.getSimpleName(), set);
                if (meta != null) mIntent.putExtra(WorkoutMeta.class.getSimpleName(), meta);
                if (mGoogleAccount != null)
                    mIntent.putExtra(Constants.KEY_PAYLOAD, (Parcelable) mGoogleAccount);
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
                Log.d(LOG_TAG, "doCloudSyncJob " + action + " Range Start: " + dateFormat.format(workout.start));
                Log.d(LOG_TAG, "Range End: " + dateFormat.format(workout.end));
                try {
                    mMessagesViewModel.setWorkType(action);
                    mMessagesViewModel.setWorkInProgress(true);
                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }else
                startCloudPendingSyncAlarm();
        }

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

    private void doGoogleFitSyncJob(Intent intent) {
        String sUser = intent.getStringExtra(KEY_FIT_USER);
        String sDevice = intent.getStringExtra(KEY_FIT_DEVICE_ID);
        if (!hasOAuthPermission(5)) {
            requestOAuthPermission(5);
            return;
        }
        if ((sUser.length() == 0) || (mSavedStateViewModel.getState() == WORKOUT_LIVE)) {
            startCloudPendingSyncAlarm();
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
        WorkoutMeta meta = new WorkoutMeta(); meta._id = timeMs; meta.workoutID = workout._id;
        meta.userID = sUser; meta.deviceID = sDevice;
        doAsyncGoogleFitAction(TASK_ACTION_SYNC_WORKOUT, workout, workoutSet, meta);
    }

   /* private void doPendingSyncJob(){
        Context mContext = getApplicationContext();
        String sUserID = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        String sDeviceID = appPrefs.getDeviceID();
        if ((sUserID.length() == 0)||(sDeviceID.length() == 0)||refreshInProgress) return;

        long startTime = 0; long endTime = 0;
        final long timeMs = System.currentTimeMillis();
        appPrefs.setLastSync(timeMs);
        int state = mSavedStateViewModel.getState();
        if ((state == WORKOUT_LIVE) || (state == WORKOUT_PAUSED)){
            startCloudPendingSyncAlarm();
            return;
        }
        mReferenceTools.init(mContext);
        if (!mReferenceTools.isNetworkConnected()) {
            startNetworkCheckAlarm(mContext);
            return;
        }
        DateTuple tuple = mSessionViewModel.getWorkoutUnSyncCount(sUserID,sDeviceID);
        DateTuple setTuple = mSessionViewModel.getWorkoutSetUnSyncCount(sUserID,sDeviceID);
        if (((tuple != null) && (tuple.sync_count > 0))
            || ((setTuple != null) && (setTuple.sync_count > 0))){
            if ((setTuple != null) && (setTuple.sync_count > 0)) {
                startTime = setTuple.mindate;
                endTime = setTuple.maxdate;
            }else
            if ((tuple != null) && (tuple.sync_count > 0)){
                startTime = setTuple.mindate;
                endTime = setTuple.maxdate;
            }
            refreshInProgress = true;
            mMessagesViewModel.setWorkType(Constants.TASK_ACTION_WRITE_CLOUD);
            mMessagesViewModel.setWorkInProgress(true);
            broadcastToast(getString(R.string.action_sync_start));
            Data.Builder builder = new Data.Builder();
            builder.putLong(LoadToGoogleWorker.ARG_START_KEY, startTime);
            builder.putLong(LoadToGoogleWorker.ARG_END_KEY, endTime);
            builder.putString(KEY_FIT_USER, sUserID);
            builder.putString(KEY_FIT_DEVICE_ID,sDeviceID);
            Data inputData = builder.build();
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest oneTimeWorkRequest =
                    new OneTimeWorkRequest.Builder(LoadToGoogleWorker.class)
                            .setInputData(inputData)
                            .setConstraints(constraints)
                            .build();
            if (mWorkManager == null) mWorkManager = WorkManager.getInstance(getApplicationContext());
            mWorkManager.beginWith(oneTimeWorkRequest).enqueue();
            mWorkManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.getId()).observe(RoomActivity.this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        refreshInProgress = false;
                        Data outData = workInfo.getOutputData();
                        int iResult = outData.getInt(Constants.KEY_RESULT,0);
                        if (iResult > 0) {
                            int iUTS = outData.getInt(UserDailyTotals.class.getSimpleName(), 0);
                            int iWorkouts = outData.getInt(Workout.class.getSimpleName(), 0);
                            int iSets = outData.getInt(WorkoutSet.class.getSimpleName(), 0);
                            int iMeta = outData.getInt(WorkoutMeta.class.getSimpleName(), 0);
                            long lStart = outData.getLong(LoadToGoogleWorker.ARG_START_KEY, 0);
                            long lEnd;
                            String sUser = outData.getString(Constants.KEY_FIT_USER);
                            String sMsg = "Upload to Fit " + getString(R.string.action_start) + ATRACKIT_SPACE + Utilities.getTimeDateString(lStart);
                            if (iUTS > 0) sMsg += Constants.LINE_DELIMITER + iUTS + " daily totals";
                            if (iWorkouts > 0) sMsg += Constants.LINE_DELIMITER + iWorkouts + " workouts";
                            if (iSets > 0) sMsg += Constants.LINE_DELIMITER + iSets + " exercise sets";
                            if (iMeta > 0) sMsg += Constants.LINE_DELIMITER + iMeta + " session data";
                            if (iWorkouts > 0) showAlertDialogConfirm(ACTION_QUICK_REPORT, sMsg);
                            lStart = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_END);
                            if (lStart == 0)
                                lStart = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                            else
                                lStart = Utilities.getDayStart(mCalendar,lStart);
                            lEnd = System.currentTimeMillis();
                            doCloudHistory(sUser,lStart,lEnd);
                        }
                    }
                }
            });
        }
        else {
            startTime = userPrefs.getLongPrefByLabel(Constants.MAP_HISTORY_END);
            if (startTime == 0) {
                if (!userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && !mMessagesViewModel.hasPhone()) {
                    long historyAsked = userPrefs.getLongPrefByLabel(Constants.AP_PREF_HISTORY_ASKED);
                    if (historyAsked == 0) {
                        userPrefs.setLongPrefByLabel(Constants.AP_PREF_HISTORY_ASKED, timeMs);
                        doConfirmDialog(Constants.QUESTION_HISTORY_LOAD, getString(R.string.load_user_history), RoomActivity.this, null);
                    }
                }
            } else {
                startTime = Utilities.getDayStart(mCalendar, startTime);
                endTime = System.currentTimeMillis();
                doCloudHistory(sUserID, startTime, endTime);
            }
        }
    }*/

    private void broadcastToast(String msg){
        if (mMessagesViewModel == null) return;
        mHandler.post(() -> {
                    Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                    msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
                    msgIntent.putExtra(KEY_FIT_TYPE, 2); // wear os flag
                    mMessagesViewModel.addLiveIntent(msgIntent);
                });
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
                        if (ContextCompat.checkSelfPermission(RoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                        if (ContextCompat.checkSelfPermission(RoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
    private void createWorkout(String sUserID, String sDeviceID){
        mWorkout = new Workout();
        mWorkout._id = System.currentTimeMillis();
        mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
        mWorkout.userID = (sUserID.length() > 0) ? sUserID : null;
        mWorkout.deviceID = (sDeviceID.length() > 0) ? sDeviceID : null;
    }

    private void createWorkoutMeta(){
        // start fresh and re-initialise the controls
        mWorkoutMeta = new WorkoutMeta();

        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        mWorkoutMeta._id = System.currentTimeMillis();
        if (mWorkout != null) {
            mWorkoutMeta.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            sUserId = mWorkout.userID;
            if (mWorkout.activityID > 0) {
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.identifier = mWorkout.identifier;
            }
            if (mWorkout.start > 0){
                mWorkoutMeta.start = mWorkout.start;
                if (mWorkout.end > 0){
                    mWorkoutMeta.end = mWorkout.end;
                    mWorkoutMeta.duration = mWorkout.duration;
                }
            }
        }
        if (sUserId.length() == 0)
            sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        if (sDeviceId.length() == 0)
            sDeviceId = appPrefs.getDeviceID();

        if (sUserId.length() > 0){
            mWorkoutMeta.userID = sUserId;
            mWorkoutMeta.deviceID = sDeviceId;
        }
        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
    }
    private void createWorkoutSet(){
        // start fresh and re-initialise the controls
        mWorkoutSet = new WorkoutSet();
        mWorkoutSet._id = (System.currentTimeMillis());

        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        if (mWorkout != null) {
            mWorkoutSet.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            sUserId = mWorkout.userID;
            if (mWorkout.activityID > 0) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }
        }
        if (sUserId.length() == 0)
            sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): appPrefs.getLastUserID();
        if (sDeviceId.length() == 0)
            sDeviceId = appPrefs.getDeviceID();

        if (sUserId.length() > 0) mWorkoutSet.userID = sUserId;
        if (sDeviceId.length() >0) mWorkoutSet.deviceID = sDeviceId;
    }

    private void showAlertDialogConfirm(final int action, final String sMsg){
        String message_text = Constants.ATRACKIT_EMPTY;
        if (sMsg != null) message_text = sMsg;
        long confirmDuration = userPrefs.getConfirmDuration();
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
            case Constants.ACTION_PAUSING:
                message_text = getString(R.string.action_pausing);
                break;
            case ACTION_REPEAT_SET:
                message_text = getString(R.string.action_repeat_set);
                break;
            case ACTION_START_SET:
                message_text = getString(R.string.action_start_set);
                break;
            case ACTION_END_SET:
                message_text = getString(R.string.action_end_set);
                break;
            case Constants.ACTION_DELETE_SET:
                message_text = getString(R.string.action_delete_set);
                break;
            case Constants.ACTION_DELETE_WORKOUT:
                message_text = getString(R.string.action_delete_workout);
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.findFragmentByTag(CustomAlertDialog.class.getSimpleName()) != null){
                CustomAlertDialog alertDialog = (CustomAlertDialog)fragmentManager.findFragmentByTag(CustomAlertDialog.class.getSimpleName());
                fragmentManager.beginTransaction().remove(alertDialog).commit();
            }
            if (action == ACTION_QUICK_REPORT) confirmDuration += TimeUnit.SECONDS.toMillis(3);
            CustomAlertDialog alertDialog = CustomAlertDialog.newInstance(action, message_text, confirmDuration,RoomActivity.this);
            alertDialog.setCancelable(true);
            alertDialog.show(fragmentManager, CustomAlertDialog.class.getSimpleName());
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "showAlertDialogConfirm " + e.getMessage());
        }
    }

    private void quitApp() {
        Context context = getApplicationContext();
        Wearable.getCapabilityClient(context).removeListener(mCapabilityListener);
        if (mFirebaseAuth != null) mFirebaseAuth.signOut();
        appPrefs = null;
        userPrefs = null;
        mGoogleAccount = null;
        finish();
        System.exit(0);
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannels() {

        // Create a notification manager object.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Context context = getApplicationContext();
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelActive = new NotificationChannel
                (ACTIVE_CHANNEL_ID,
                        getString(R.string.notify_channel_activity_name),
                        IMPORTANCE_DEFAULT);

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
                        IMPORTANCE_LOW);
        notificationChannelSummary.enableLights(true);
        notificationChannelSummary.setLightColor(Color.BLUE);
        notificationChannelSummary.enableVibration(false);
        notificationChannelSummary.setDescription
                (getString(R.string.notify_channel_summary_desc));
        notificationChannelSummary.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationChannelSummary.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelSummary);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelGoals = new NotificationChannel
                (GOALS_CHANNEL_ID,
                        getString(R.string.notify_channel_goals_name),
                        IMPORTANCE_DEFAULT);

        notificationChannelGoals.enableLights(true);
        notificationChannelGoals.setLightColor(Color.RED);
        notificationChannelGoals.enableVibration(false);
        notificationChannelGoals.setDescription
                (getString(R.string.notify_channel_goals_desc));
        notificationChannelGoals.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelGoals);

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelSchedule = new NotificationChannel
                (MAINTAIN_CHANNEL_ID,
                        getString(R.string.notify_channel_maintain_name),
                        IMPORTANCE_DEFAULT);

        notificationChannelSchedule.enableLights(true);
        notificationChannelSchedule.setLightColor(Color.RED);

        notificationChannelSchedule.enableVibration(true);
        notificationChannelSchedule.setDescription
                (getString(R.string.notify_channel_maintain_desc));
        notificationChannelSchedule.setShowBadge(true);
        notificationManager.createNotificationChannel(notificationChannelSchedule);

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

    // [START signOut]
    private void signOut() {
        final Context context = getApplicationContext();
        Log.d(LOG_TAG, "sign out");
        String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
        if (sUserId.length() > 0) {
            if (userPrefs == null) userPrefs = UserPreferences.getPreferences(context, sUserId);
            userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, Constants.ATRACKIT_EMPTY);
            userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
            userPrefs.setLastUserName(Constants.ATRACKIT_EMPTY);
        }
        CustomIntentReceiver.cancelAlarm(context);
        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) {
            DataMap dataMap = new DataMap();
            dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
            dataMap.putInt(KEY_FIT_TYPE, (int)4); // sign-out indicator
            dataMap.putString(KEY_FIT_USER, sUserId);
            if (mGoogleAccount != null) dataMap.putString(KEY_FIT_NAME, mGoogleAccount.getDisplayName());
            if (phoneNodeId == null || phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
            if (phoneNodeId == null || (phoneNodeId.length() == 0))
                sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME, Constants.MESSAGE_PATH_PHONE_SERVICE,dataMap);
            else
                sendMessage(phoneNodeId,Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
        mHandler.postDelayed(() -> googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (appPrefs != null) {
                appPrefs.setLastUserID(Constants.ATRACKIT_EMPTY);
                mSavedStateViewModel.setUserIDLive(Constants.ATRACKIT_EMPTY);
                appPrefs.setLastLicCheck(0L);  // check licence on new sign-in
                appPrefs.setDeviceID(Constants.ATRACKIT_EMPTY);
                mSavedStateViewModel.setDeviceID(Constants.ATRACKIT_EMPTY);
                appPrefs.setLastSync(0L);
                appPrefs.setLastDailySync(0L);
                appPrefs.setLastLocationUpdate(0L);
                mSavedStateViewModel.setActiveWorkout(null);
                mSavedStateViewModel.setSetIndex(1);
                CustomIntentReceiver.cancelAlarm(context);
            }
            if (userPrefs != null) {
                userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, Constants.ATRACKIT_EMPTY); // refresh image on next logon
                userPrefs.setLastUserName(Constants.ATRACKIT_EMPTY);
            }
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_SIGNOUT);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);

            quitApp();
        }),1000L);

    }
    // [END signOut]
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
                mHandler.postDelayed(() -> signIn(), 2000);
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
            ActivityCompat.startActivity(getApplicationContext(),intent,null);
            finish();   // YUP - finish - dead dude
        }
    }


    private void signIn(){
        if (!authInProgress) {
            broadcastToast(getString(R.string.signing_in_google));
            try {

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                authInProgress = true;
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeCustomAnimation(RoomActivity.this, R.anim.jump_in, R.anim.jump_out);
                ActivityCompat.startActivityForResult(RoomActivity.this,signInIntent, REQUEST_SIGN_IN, options.toBundle());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void signInSilent(){
        if (!authInProgress) {
            authInProgress = true;
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.CLIENT_ID)
                    .requestEmail().build();
            final long timeMs = System.currentTimeMillis();
            final long lastAppLogin = appPrefs.getLastUserLogIn();
            // Build a GoogleSignInClient with the options specified by gso.
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
            Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
            signInAccountTask.addOnCompleteListener(task -> {
                authInProgress = false;
                if (task.isSuccessful()){
                    try {
                        mGoogleAccount = task.getResult(ApiException.class);
                        if (mGoogleAccount != null && !mGoogleAccount.isExpired()) {
                            sUserID = mGoogleAccount.getId();
                            appPrefs.setLastUserID(sUserID);
                            userPrefs = UserPreferences.getPreferences(getApplicationContext(), sUserID);
                            long previousUserLogin = userPrefs.getLastUserSignIn();
                            mSavedStateViewModel.setUserIDLive(sUserID);
                            userPrefs.setUserEmail(mGoogleAccount.getEmail());
                            userPrefs.setLastUserName(mGoogleAccount.getDisplayName());
                            // ensure we dont do this too often
                            if (TimeUnit.MILLISECONDS.toSeconds(timeMs - previousUserLogin) > 20) {
                                final String idToken = mGoogleAccount.getIdToken();
                                appPrefs.setFirebaseAvail((idToken != null) && (idToken.length() > 0));
                                onSignIn(false);
                                if ((idToken != null) && (idToken.length() > 0)) {  // && useFireBase
                                    Log.w(LOG_TAG,"token: " + idToken);
                                    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                    mFirebaseAuth.signInWithCredential(credential)
                                            .addOnCompleteListener(RoomActivity.this, task1 -> {
                                                // If sign in fails, display a message to the user. If sign in succeeds
                                                // the auth state listener will be notified and logic to handle the
                                                // signed in user can be handled in the listener.
                                                if (!task1.isSuccessful()) {
                                                    Log.w(LOG_TAG, "silent signInWithCredential", task1.getException());
                                                    //  UserPreferences.setPrefStringByLabel(getApplicationContext(), "idToken", "");
                                                } else {
                                                    mFirebaseUser = task1.getResult().getUser();
                                                    //  UserPreferences.setPrefStringByLabel(getApplicationContext(), "idToken", idToken);
                                                    if (mFirebaseUser != null) {
                                                        broadcastToast(mFirebaseUser.getDisplayName());
                                                    }
                                                }
                                            });
                                } else
                                    broadcastToast("online " + mGoogleAccount.getDisplayName());
                            }
                        }
                        else{
                            if (mReferenceTools.isNetworkConnected()) {
                                signIn();
                            } else {
                                if ((appPrefs.getLastUserID().length() == 0) || ((timeMs - lastAppLogin) < TimeUnit.DAYS.toMillis(2)))
                                    doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null, null);
                                else
                                    onSignIn(true);
                            }
                        }

                    }catch (ApiException aie){
                        FirebaseCrashlytics.getInstance().recordException(aie);
                        signInError(aie);
                        /*Log.e(LOG_TAG,"silent authentication error " + aie.getLocalizedMessage());
                        if ((aie.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS) ||
                                (aie.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED)) {
                            Log.d(LOG_TAG, "signin error "  + aie.getStatusCode());
                        }else{
                            signIn();
                        }*/
                    }
                }else{ // unsuccessful task
                    if (mReferenceTools.isNetworkConnected()) {
                        signIn();
                    } else {
                        if ((appPrefs.getLastUserID().length() == 0) || ((timeMs - lastAppLogin) < TimeUnit.DAYS.toMillis(2)))
                            doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), null, null);
                        else
                            onSignIn(true);
                    }
                }
            });
        }
    }
    private void startSplashActivityForResult(String intentString){
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(RoomActivity.this, R.anim.jump_in, R.anim.jump_out);
        Intent intent = new Intent(RoomActivity.this, SplasherActivity.class);
        if (intentString != null) intent.putExtra(SplasherActivity.ARG_ACTION, intentString);
        if (mGoogleAccount != null)
            intent.putExtra(KEY_FIT_USER, mGoogleAccount.getId());
        else
        if (appPrefs.getLastUserID().length() > 0)
            intent.putExtra(KEY_FIT_USER, appPrefs.getLastUserID());
        intent.putExtra(SplasherActivity.ARG_RETURN_RESULT, 1);
        if (mMessagesViewModel != null) mMessagesViewModel.setWorkInProgress(true);
        splashActivityResultLauncher.launch(intent, options);
    }

    private void onSignIn(boolean bOffline){
        Context context = getApplicationContext();
        final long timeMs = System.currentTimeMillis();
        final long lastAppLogin = appPrefs.getLastUserLogIn();
        final String sLastId = appPrefs.getLastUserID();
        if ((mGoogleAccount == null) && !bOffline && mReferenceTools.isNetworkConnected()) {
            signIn();
            return;
        }
        authInProgress = false;
        String PersonId = (!bOffline) ? mGoogleAccount.getId() : sLastId;
        if (bOffline && ((PersonId == null) || (PersonId.length() == 0)) && (timeMs - lastAppLogin > TimeUnit.DAYS.toMillis(1))) {
            doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), RoomActivity.this, null);
            return;
        }

        long previousTimeMs = 0L;
        userPrefs = UserPreferences.getPreferences(context,PersonId);
        if (userPrefs != null){
            previousTimeMs = userPrefs.getLastUserSignIn();
        }
        if (bOffline && previousTimeMs == 0){
            doConfirmDialog(Constants.QUESTION_NETWORK, getString(R.string.no_network_connection), RoomActivity.this, null);
            return;
        }
        String PersonName = ((bOffline) || (mGoogleAccount == null)) ? userPrefs.getLastUserName() : mGoogleAccount.getDisplayName() ;
        int configCount = mSessionViewModel.getConfigCount(PersonId);
        Log.w(LOG_TAG, "onSignIn " + "offline " + bOffline + " " + PersonName);
        appPrefs.setLastUserLogIn(timeMs);
        String sMsgName;
        if (!bOffline && mGoogleAccount != null) {
            appPrefs.setLastUserID(mGoogleAccount.getId());
            userPrefs.setLastUserName(mGoogleAccount.getDisplayName());
            userPrefs.setUserEmail(mGoogleAccount.getEmail());
            userPrefs.setLastUserSignIn(timeMs);
            if (timeMs - previousTimeMs > TimeUnit.DAYS.toMillis(1))
                sMsgName = Utilities.getTimeDayString(previousTimeMs) + ATRACKIT_SPACE + PersonName;
            else
                sMsgName = Utilities.getTimeString(previousTimeMs) + ATRACKIT_SPACE + PersonName;
            broadcastToast(sMsgName);
        }
        else{
            sMsgName = getString(R.string.action_offline) + ATRACKIT_SPACE + PersonName;
            if (timeMs - previousTimeMs > TimeUnit.DAYS.toMillis(1))
                sMsgName = Utilities.getTimeDayString(previousTimeMs) + ATRACKIT_SPACE + sMsgName;
            else
                sMsgName = Utilities.getTimeString(previousTimeMs) + ATRACKIT_SPACE + sMsgName;
            broadcastToast(sMsgName);
        }
        mSavedStateViewModel.setUserIDLive(PersonId);
        final Device device = Device.getLocalDevice(context);
        sDeviceID = device.getUid();
        String sLabel = getString(R.string.default_loadtype) + Integer.toString(Constants.SELECTION_ACTIVITY_GYM);
        boolean bHasAudio = Utilities.hasSpeaker(context);
        // first load
        if (configCount == 0){
            createNotificationChannels(); // setup notification manager and channels if needed
            doSetupUserConfig(PersonId);
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
            appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(20));
            userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_START,0);
            userPrefs.setLongPrefByLabel(Constants.MAP_HISTORY_END,0);
            appPrefs.setPrefByLabel(Constants.AP_PREF_NOTIFY_ASKED, false);
            appPrefs.setPrefByLabel(Constants.AP_PREF_AUDIO_ASKED, false);
            appPrefs.setPrefByLabel(Constants.AP_PREF_VIBRATE_ASKED, false);
            userPrefs.setPrefByLabel(USER_PREF_USE_VIBRATE,(Utilities.hasVibration(context)));
            userPrefs.setPrefByLabel(USER_PREF_USE_AUDIO,(Utilities.hasSpeaker(context)));
            userPrefs.setPrefByLabel(USER_PREF_USE_NOTIFICATION, true);
            userPrefs.setPrefByLabel(USER_PREF_RECOG, appPrefs.getUseLocation());
            userPrefs.setPrefByLabel(Constants.USER_PREF_SHOW_GOALS, false);
            bUseKG = !(Locale.getDefault().equals(Locale.US));
            userPrefs.setUseKG(bUseKG);
            userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
            appPrefs.setPrefByLabel(Constants.LABEL_USE_GRID, true);
            userPrefs.setPrefStringByLabel(sLabel, Long.toString(WORKOUT_TYPE_STRENGTH));  // default to strength training
            mMessagesViewModel.setUseLocation(false);
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
            mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
            mSavedStateViewModel.setRepsDefault(userPrefs.getDefaultNewReps());
            userPrefs.setStepsSampleRate(120L);
            userPrefs.setBPMSampleRate(60L);
            userPrefs.setOthersSampleRate(300L);

            mSavedStateViewModel.setUserPreferences(userPrefs);
            if (userPrefs.getPrefStringByLabel(sLabel).length() == 0) userPrefs.setPrefStringByLabel(sLabel,Long.toString(WORKOUT_TYPE_STRENGTH));
            if (userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE).length() > 0) // if ready start using it
                userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_INT_FILE);
        }
        else {
            bUseKG = userPrefs.getUseKG();
            mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
            mSavedStateViewModel.setRepsDefault(userPrefs.getDefaultNewReps());
            mSavedStateViewModel.loadFromPreferences(userPrefs);
            userPrefs.setLastUserName(PersonName);
            userPrefs.setLastUserSignIn(timeMs);
            mSavedStateViewModel.setUserPreferences(userPrefs);
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
            if (appPrefs.getUseSensors()) {
                if (userPrefs.getStepsSampleRate() == 0) userPrefs.setStepsSampleRate(120L);
                if (userPrefs.getBPMSampleRate() == 0) userPrefs.setBPMSampleRate(60L);
                if (userPrefs.getOthersSampleRate() == 0) userPrefs.setOthersSampleRate(300L);
            }
            if (!appPrefs.getUseSensors()) broadcastToast(getString(R.string.sensors_permission_not_used));
            if (!appPrefs.getUseLocation()) broadcastToast(getString(R.string.location_permission_not_used));
            String sAction = checkApplicationSetup();
            if (sAction.length() == 0) {
                if (!appPrefs.getAppSetupCompleted()) {
                    appPrefs.setAppSetupCompleted(true);
                    cancelNotification(INTENT_SETUP);
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_AUDIO, bHasAudio);
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, true);
                    createWorkout(sUserID, appPrefs.getDeviceID());
                    mWorkout._id = 2;
                    mWorkout.start = 0;
                    mWorkout.end = 0;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    broadcastToast(getString(R.string.label_setup_complete));
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_AUDIO, bHasAudio);
                    userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, true);
                }
                List<Configuration> configList = mSessionViewModel.getConfigurationLikeName(Constants.ATRACKIT_SETUP,ATRACKIT_ATRACKIT_CLASS);
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
                }
                if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED)){
                    startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                }else phoneNodeId = appPrefs.getLastNodeID();
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
                startSplashActivityForResult(sAction);
                return;
            }
        }
        appPrefs.setBackgroundLoadComplete(true);
        userPrefs.setLastUserSignIn(timeMs);
        mSavedStateViewModel.setUserIDLive(PersonId);
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
        if (!bOffline && mGoogleAccount != null && appPrefs.getAppSetupCompleted()) {
            if (!appPrefs.getUseSensors()) showAlertDialogConfirm (ACTION_QUICK_REPORT, getString(R.string.sensors_permission_not_used));
            if (!appPrefs.getUseLocation()) broadcastToast(getString(R.string.location_permission_not_used));
            mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
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
            if (userPrefs.getPrefByLabel(USER_PREF_STORAGE)) {
                Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                if (PersonPhotoUri != null) {
                    String sFile = userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE);
                    if (sFile.length() == 0)
                        doImageWork(PersonPhotoUri, 0);
                    else {
                        boolean bitmapOK = false;
                        Uri uriImage = Uri.parse(sFile);
                        try {
                            Bitmap bitmap = Utilities.getMyBitmap(context, uriImage);
                            if (bitmap != null) {
                                bitmapOK = true;
                            }
                        } catch (Exception e) {
                            if (e.equals(FileNotFoundException.class) && (userPrefs != null)) {
                                userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, Constants.ATRACKIT_EMPTY); // cause a fix on reload homepage
                            }
                        }
                        if (!bitmapOK) {
                            Log.e(LOG_TAG, "fixing profile image file " + sFile);
                            doImageWork(PersonPhotoUri, 0);
                        }

                    }
                }
            }
            //doSkuLookupJob(PersonId);
        }
        if (Utilities.hasSensorDevicesPermission(context)) {
            new TaskDeviceConfigLoad(PersonId).run();
            sdtRunnable = new TaskSensorDailyTotals(PersonId,mCalendar);
            sdtHandle = scheduler.scheduleWithFixedDelay(sdtRunnable,0,30,TimeUnit.SECONDS);
        }
        List<Workout> inProgressList = (appPrefs.getAppSetupCompleted() && userPrefs != null)
                ? mSessionViewModel.getInProgressWorkouts(PersonId, sDeviceID, Utilities.TimeFrame.BEGINNING_OF_MONTH) : null;
        if ((inProgressList != null) && (inProgressList.size() > 0)){
            mCalendar.setTime(new Date());
            int iDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
            int iHOD = mCalendar.get(Calendar.HOUR_OF_DAY);
            for (Workout inProgressWorkout : inProgressList) {
                mCalendar.setTimeInMillis(inProgressWorkout.start);
                int arrivingDOY = mCalendar.get(Calendar.DAY_OF_YEAR);
                int arrivingHOD = mCalendar.get(Calendar.HOUR_OF_DAY);
                if ((iDOY == arrivingDOY) && ((iHOD - arrivingHOD) <= 3)) {
                    Log.w(LOG_TAG, "loading in progress " + iDOY);
                    //  int state = Utilities.currentWorkoutState(workoutList.get(0));
                    mWorkout = inProgressWorkout;
                    List<WorkoutSet> sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                    List<WorkoutMeta> metas = mSessionViewModel.getWorkoutMetaByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                    if (sets == null || sets.size() == 0) {
                        mSessionViewModel.deleteWorkout(mWorkout);
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
                        if (mWorkoutSet == null)
                            mWorkoutSet = sets.get(sets.size() - 1);
                        if ((metas != null) && (metas.size() > 0)) {
                            mWorkoutMeta = metas.get(0);
                        }
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                        mSavedStateViewModel.setToDoSets(sets);
                        mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
                        mSavedStateViewModel.setIconID(mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID));
                        mSavedStateViewModel.setColorID(mReferenceTools.getFitnessActivityColorById(mWorkout.activityID));
                        // we have sets and everything
                        mCalendar.setTimeInMillis(mWorkout.start);
                        if ((iHOD - arrivingHOD) > 1) {
                            String started = String.format(Locale.getDefault(), "%1$02d:%2$02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                            String sQuestion = String.format(Locale.getDefault(), getString(R.string.confirm_resume_session), mWorkout.activityName, started);
                            doConfirmDialog(Constants.QUESTION_RESUME_END, sQuestion, null,null);
                        } else {
                            if (mWorkoutSet.start > 0 && mWorkoutSet.end == 0) {
                                sessionResume();
                            } else {
                                if (mSavedStateViewModel.getState() != WORKOUT_PAUSED) sessionSetCurrentState(WORKOUT_PAUSED);
                                if ((mWorkoutSet.start > 0) && (mWorkoutSet.end > 0) && (mWorkoutSet.last_sync == 0)) {
                                    long pStart = SystemClock.elapsedRealtime();
                                    if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                                        // go to the gym confirm fragment
                                        mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                                    }
/*                                    if (Utilities.isShooting(mWorkout.activityID)) {
                                        int icon = mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
                                        int color = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
                                        int rest = userPrefs.getArcheryRestDuration();
                                        mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                                        sessionSetCurrentState(WORKOUT_PAUSED);
                                        ShootingConfirmFragment shootingConfirmFragment = ShootingConfirmFragment.newInstance(icon,color, mWorkoutSet, mWorkout,rest,mWorkoutMeta);
                                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                        transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                                        transaction.add(R.id.top_container, shootingConfirmFragment);
                                        transaction.addToBackStack("dialog_shooting_confirm");
                                        transaction.commit();
                                    }*/
                                }
                                Intent editIntent = setResultIntent(INTENT_WORKOUT_EDIT);
                                mMessagesViewModel.addLiveIntent(editIntent);

                            }
                        }
                        return;  // avoid remaining startup stuff
                    }
                }else{ // not same day within 3 hours!
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
    // [START revokeAccess]
    private void revokeAccess() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        Log.d(LOG_TAG, "revoke Access");
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(RoomActivity.this, gso);
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //TODO: clear all userpreferences etc.
                        quitApp();
                    }
                });
    }
    // [END revokeAccess]*/

    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission(int requestType) {
        FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(requestType);
        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
        return (mGoogleAccount != null) && GoogleSignIn.hasPermissions(mGoogleAccount, fitnessOptions);
    }

    /** Launches the Google SignIn activity to request OAuth permission for the user. */
    private void requestOAuthPermission(int requestType) {
        mSavedStateViewModel.setColorID(requestType);
        String sMessage = getString(R.string.ask_fit_read_aggregate);
        if (requestType == 1) sMessage = getString(R.string.ask_fit_write_session);
        if (requestType == 2) sMessage = getString(R.string.ask_fit_read_session);
        doAlertDialogMessage(sMessage);
        FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(requestType);
        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
        GoogleSignIn.requestPermissions(
                RoomActivity.this,
                REQUEST_OAUTH_REQUEST_CODE,
                mGoogleAccount,
                fitnessOptions);
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
    private void onNewLocation(Location location) {
        if (location != null){
            if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
            if ((appPrefs != null) && (!appPrefs.getUseLocation())) return;
            String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId(): sUserID;
            if (sUserId == null || sUserId.length() == 0) return;
            double last_lat = mSavedStateViewModel.getLatitude();
            double last_long = mSavedStateViewModel.getLongitude();
            boolean newLocation = false;
            long timeMs = System.currentTimeMillis();
            mCalendar.setTimeInMillis(timeMs);
            int DOY =  mCalendar.get(Calendar.DAY_OF_YEAR);
            long lastUpdate = appPrefs.getLastLocationUpdate();
            if (lastUpdate > 0) {
                mCalendar.setTimeInMillis(lastUpdate);
                newLocation = (DOY < mCalendar.get(Calendar.DAY_OF_YEAR));
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
                String sLocationMsg = mSavedStateViewModel.getLocationAddress();
                if (!newLocation)
                    newLocation = (last_lat != location.getLatitude())
                            || (last_long != location.getLongitude()
                            || (sLocationMsg.length() == 0));
                if (newLocation) {
                    new TaskLocation(location).run();
                }
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG,"onNewLocation " + e.getMessage());
            }
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
                    if (!Utilities.isDetectedActivity(w.activityID) && (w.device_sync == 0)) {
                        Bundle resultData = new Bundle();
                        resultData.putString(KEY_FIT_USER, sUserID);
                        resultData.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                        resultData.putParcelable(Workout.class.getSimpleName(), w);
                        sendDataClientMessage(INTENT_EXTRA_RESULT, resultData);
                    }
                }
            }
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_DAILY_UPDATE);
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

            sendMessage(Constants.MESSAGE_PATH_PHONE, dataMap);
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

             final long timeMs = System.currentTimeMillis();
             boolean bOffline = (workout != null) ? (workout.offline_recording == 1) : !mReferenceTools.isNetworkConnected();
             if (currentState == WORKOUT_LIVE || currentState == WORKOUT_PAUSED) {
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
             calendar.setTimeInMillis(timeMs);
             boolean bUpdated = false;
             long startHour = Utilities.getDayStart(calendar, timeMs);
             int DOYNow = calendar.get(Calendar.DAY_OF_YEAR);
             DateTuple dt = mSessionViewModel.getSensorDailyDateTuple(sUserID, startHour, timeMs); // how many today
             boolean bExisting = ((dt != null) && (dt.sync_count > 0));
             SensorDailyTotals topSDT = null;
             SensorDailyTotals currentSDT = mSavedStateViewModel.getSDT();
             if (bExisting) {
                 topSDT = mSessionViewModel.getTopSensorDailyTotal(sUserID);
                 if (topSDT != null) {
                     if ((timeMs - topSDT._id) > sdtRefreshPeriod)   // if time since last > the new period = start a new one!
                         bExisting = false;
                     else {
                         if ((currentSDT != null) && (currentSDT.lastUpdated > topSDT.lastUpdated)) {
                             SDT = currentSDT;
                             bUpdated = true;
                         }else
                             SDT = topSDT;

                     }
                 } else bExisting = false;
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
                    // sessionSetCurrentState(WORKOUT_INVALID);
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
             }
             else{
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
             broadcastIntent.putExtra(KEY_CHANGE_STATE, currentState);
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
                builder.putString(KEY_FIT_USER, sUserID);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(LOG_TAG, "onNewIntent " + intent.getAction());
        mStartupIntent = intent;
        handleIntent(intent);  // internal or otherwise not sessions
    }


    private void sendNotification(String intentString, Bundle dataBundle) {
        // Sets up the pending intent to update the notification.
        int notificationID = 0;
        long iconID = R.drawable.ic_a_outlined;
        int activityIcon = R.drawable.ic_running_white;
        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_SPACE;
        String sTime = Utilities.getTimeString(mCalendar.getTimeInMillis());
        String sChannelID = Constants.ATRACKIT_EMPTY;
        NotificationManagerCompat mNotifyManager = NotificationManagerCompat.from(this);
        boolean bIsGym = false;
        NotificationCompat.Builder notifyBuilder =  getNotificationBuilder(intentString, dataBundle);

        Context context = getApplicationContext();
        PendingIntent pendingViewIntent;

        if (INTENT_SETUP.equals(intentString)) {
            Intent viewIntent = new Intent(context, RoomActivity.class);
            viewIntent.setAction(INTENT_SETUP);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            notificationID = NOTIFICATION_MAINTAIN_ID;
            sTitle = getString(R.string.action_setup_running);
            sContent = getString(R.string.app_name);
            sChannelID = MAINTAIN_CHANNEL_ID;
            pendingViewIntent = PendingIntent.getActivity(context, notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action actionView5 =
                    new NotificationCompat.Action.Builder(R.drawable.ic_a_outlined,
                            getString(R.string.action_open), pendingViewIntent)
                            .build();
            NotificationCompat.WearableExtender extender5 = new NotificationCompat.WearableExtender().addAction(actionView5);

            NotificationCompat.Builder notifyBuilder5 = new NotificationCompat
                    .Builder(context, sChannelID)
                    .setContentTitle(sTitle)
                    .setContentText(sContent)
                    .setSmallIcon(R.drawable.ic_a_outlined)
                    .setUsesChronometer(false)
                    .setOngoing(true)
                    .setContentInfo(getString(R.string.action_setup_running))
                    .setAutoCancel(true)
                    .extend(extender5)
                    .setContentIntent(pendingViewIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            try {
                if (mNotifyManager != null)
                    mNotifyManager.notify(notificationID, notifyBuilder5.build());
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
        Intent viewIntent = new Intent(context, RoomActivity.class);
        viewIntent.setAction(Intent.ACTION_VIEW);
        viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        viewIntent.putExtra(KEY_FIT_USER, sUserID);
        viewIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
        if (INTENT_DAILY.equals(intentString) || INTENT_SUMMARY_DAILY.equals(intentString)){
            notificationID = NOTIFICATION_SUMMARY_ID;
            viewIntent.setAction(INTENT_SUMMARY_DAILY);
            pendingViewIntent = PendingIntent.getActivity
                    (getApplicationContext(), notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch, context.getString(R.string.action_open), pendingViewIntent).build();
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView);
            notifyBuilder.extend(extender);
            notifyBuilder.setContentIntent(pendingViewIntent);
            try {
                if (mNotifyManager != null)
                    mNotifyManager.notify(notificationID, notifyBuilder.build());
            } catch(Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, e.getMessage());
            }
            return;
        }

        Workout workout = (dataBundle.containsKey(Workout.class.getSimpleName())) ? dataBundle.getParcelable(Workout.class.getSimpleName()) : null;
        WorkoutSet set = (dataBundle.containsKey(WorkoutSet.class.getSimpleName())) ? dataBundle.getParcelable(WorkoutSet.class.getSimpleName()) : null;
        if (workout == null){
            if (dataBundle.containsKey(KEY_FIT_WORKOUTID)){
                long ID = dataBundle.getLong(KEY_FIT_WORKOUTID,0);
                if (ID > 0){
                    workout = mSessionViewModel.getWorkoutById(ID, sUserID,sDeviceID);
                }
            }
        }
        if (workout != null) {
            bIsGym = (Utilities.isGymWorkout(workout.activityID));
            if (bIsGym && (set == null)) {
                if (dataBundle.containsKey(KEY_FIT_WORKOUT_SETID)) {
                    long ID2 = dataBundle.getLong(KEY_FIT_WORKOUT_SETID, 0);
                    if (ID2 > 0) {
                        set = mSessionViewModel.getWorkoutSetById(ID2, sUserID,sDeviceID);
                    }
                }
            }
            int itemState = Utilities.currentWorkoutState(workout);
            viewIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
            if (set != null) viewIntent.putExtra(KEY_FIT_WORKOUT_SETID, set._id);
            viewIntent.putExtra(KEY_FIT_TYPE, 1); // starting from notification flag
    
            mReferenceTools.init(getApplicationContext());
            int iconAct = mReferenceTools.getFitnessActivityIconResById(workout.activityID);
            if (iconAct > 0)
                activityIcon = iconAct;
            String sLabel = (itemState == WORKOUT_COMPLETED) ? getString(R.string.action_review) : getString(R.string.action_open);
            Drawable drawable =  AppCompatResources.getDrawable(context, activityIcon);
            if (drawable != null) {
                Utilities.setColorFilter(drawable, getColor(R.color.primaryLightColor));
                Bitmap bitmap = vectorToBitmap(drawable);
                if (bitmap != null) notifyBuilder.setLargeIcon(bitmap);
            }
            pendingViewIntent = PendingIntent.getActivity
                    (getApplicationContext(), notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionView =
                    new NotificationCompat.Action.Builder(activityIcon,sLabel, pendingViewIntent)
                            .build();
            Intent activeIntent = new Intent(context, RoomActivity.class);
            activeIntent.setAction(intentString);
            activeIntent.putExtra(Constants.KEY_FIT_TYPE, 0);
            activeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activeIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            activeIntent.putExtra(KEY_FIT_USER, sUserID);
            activeIntent.putExtra(Constants.KEY_FIT_WORKOUTID, workout._id);
            if (set != null) activeIntent.putExtra(KEY_FIT_WORKOUT_SETID, set._id);
            switch (intentString) {
                case INTENT_ACTIVE_START:
                case INTENT_ACTIVESET_START:
                case INTENT_ACTIVE_RESUMED:
                case INTENT_ACTIVE_PAUSE:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sChannelID = ACTIVE_CHANNEL_ID;
                    NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView);
                    notifyBuilder.extend(extender);
                    activeIntent.putExtra(KEY_FIT_VALUE, 1);
                    PendingIntent activePendingIntent = PendingIntent.getActivity(context,
                            notificationID, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Action actionActive =
                            new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                    getString(R.string.action_open), activePendingIntent)
                                    .build();
                    NotificationCompat.WearableExtender extenderActive = new NotificationCompat.WearableExtender()
                            .addAction(actionActive);
                    notifyBuilder.extend(extenderActive);

                    try {
                        // Deliver the notification.
                        if (mNotifyManager != null)
                            mNotifyManager.notify(notificationID, notifyBuilder.build());
                    } catch(Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;
                case INTENT_ACTIVE_STOP:
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sTitle = "Completed " + Utilities.getPartOfDayString(workout.start) + Constants.ATRACKIT_SPACE + workout.activityName;
                    sContent = mReferenceTools.workoutNotifyText(workout);
                    notificationID = NOTIFICATION_ACTIVE_ID;
                    sChannelID = ACTIVE_CHANNEL_ID;

                    NotificationCompat.WearableExtender extender2 = new NotificationCompat.WearableExtender().addAction(actionView);
                    //     extender2.setContentIcon(Math.toIntExact(iconID));

                    notifyBuilder.extend(extender2);

                    try {
                        if (mNotifyManager != null)
                            mNotifyManager.notify(notificationID, notifyBuilder.build());
                    } catch(Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;

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

                    int goal_icon;
                    String goal_action_msg;
                    if (goal_type == GOAL_TYPE_BPM) {
                        sTitle = "BPM Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " bpm";
                    }
                    if (goal_type == GOAL_TYPE_STEPS) {
                        sTitle = "Steps Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " steps";
                    }
                    if (goal_type == GOAL_TYPE_DURATION) {
                        sTitle = "Duration Goal Achieved";
                        sContent = "Achieved " + goal_last_count + " mins";
                    }
                    notifyBuilder.setContentText(sContent);
                    notifyBuilder.setContentTitle(sTitle);
                    NotificationCompat.Action actionView3 =
                            new NotificationCompat.Action.Builder(R.drawable.ic_launch,
                                    getString(R.string.action_open), pendingViewItent3)
                                    .build();
                    NotificationCompat.WearableExtender extender3 = new NotificationCompat.WearableExtender().addAction(actionView3);
                    notifyBuilder.extend(extender3);
                    try {
                        if (mNotifyManager != null)
                            mNotifyManager.notify(notificationID, notifyBuilder.build());
                    } catch(Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    break;

                case INTENT_SCHEDULE_TRIGGER:
                    notificationID = NOTIFICATION_SCHEDULE_ID;
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
                        notificationID, reportingIntent, 0PendingIntent.FLAG_ONE_SHOT);*/
//                notifyBuilder.addAction(R.drawable.ic_action_attarget_vector_dark,
//                        getString(R.string.note_report_review), reportPendingIntent);
                    break;
                default:
            }
        } // workout is not null

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
                notificationID = NOTIFICATION_MAINTAIN_ID;
                break;
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
            if (notificationID > 0)
                notificationManager.cancel(notificationID);
            else
                notificationManager.cancelAll();
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    /**
    public void updateNotification(String intentString, Bundle data) {
        int notificationID = 0;
        long iconActivityId = mSavedStateViewModel.getIconID();
        Resources res = getResources();
        restoreWorkoutVariables();
        int iColor = (mSavedStateViewModel.getColorID() != null) ? mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
        final String sName = (mWorkout != null) ? mWorkout.activityName : getString(R.string.label_activity);

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
            case INTENT_SUMMARY_WORKOUT:
                notificationID = NOTIFICATION_SUMMARY_ID;
                break;
        }
        // Load the drawable resource into the a bitmap image.
        Bitmap androidImage = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_launcher_home);

        // Build the notification with all of the parameters using helper
        // method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(intentString, data);
        if (intentString.equals(INTENT_ACTIVE_RESUMED)) {
            if (mSavedStateViewModel.getIsGym()) {
                notifyBuilder.setProgress( mSavedStateViewModel.getToDoSetsSize(), mSavedStateViewModel.getSetIndex(), false);
            }
        }
        if (intentString.equals(INTENT_ACTIVE_STOP)) {
            if (mSavedStateViewModel.getIsGym()) {
                notifyBuilder.setProgress(0,0, false);
            }
        }
        // Deliver the notification.
        mNotifyManager.notify(notificationID, notifyBuilder.build());

        // Disable the update btnConfirmBodypart, leaving only the cancel btnConfirmBodypart enabled.
        // setNotificationButtonState(false, false, true);
    }

     * Helper method that builds the notification.
     *
     * @return NotificationCompat.Builder: notification build with all the
     * parameters.
     */
    private NotificationCompat.Builder getNotificationBuilder(String intentString, Bundle dataBundle) {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        Intent notificationIntent = new Intent(getApplicationContext(), RoomActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        notificationIntent.putExtra(KEY_FIT_TYPE, 1);
        PendingIntent notificationPendingIntent;
        NotificationCompat.Builder notifyBuilder;
        int notificationID = 0;
        long iconID = R.drawable.ic_a_outlined;
        int activityIcon = R.drawable.ic_running_white;

        String sTitle = Constants.ATRACKIT_SPACE;
        String sContent = Constants.ATRACKIT_EMPTY;
        String sTime = SimpleDateFormat.getTimeInstance().format(mCalendar.getTime());
        String sChannelID = Constants.ATRACKIT_EMPTY;
        final String GOAL_PREFIX = "goal.";
        long startElapsed = SystemClock.elapsedRealtime();
        switch (intentString) {
            case INTENT_ACTIVE_START:
            case INTENT_ACTIVE_RESUMED:
            case INTENT_ACTIVE_PAUSE:
                Workout workout = dataBundle.getParcelable(Workout.class.getSimpleName());
                sTitle =  Utilities.getPartOfDayString(workout.start) + Constants.ATRACKIT_SPACE + workout.activityName;
                if (intentString.equals(INTENT_ACTIVE_PAUSE)) {
                    sContent += "Paused ";
                }else
                    if (workout.offline_recording == 1)
                        sContent += "Offline recording ";
                    else
                        sContent += "Live session recording ";
                startElapsed = workout.start;
                if (Utilities.isGymWorkout(workout.activityID) && (dataBundle.containsKey(WorkoutSet.class.getSimpleName())) ){
                    WorkoutSet workoutSet = dataBundle.getParcelable(WorkoutSet.class.getSimpleName());
                    if (workoutSet != null) {
                        if ((workoutSet.exerciseName != null) && (workoutSet.exerciseName.length() > 0)) sContent += workoutSet.exerciseName;
                        if (workoutSet.start > 0) startElapsed = workoutSet.start;
                    }
                }
                activityIcon = mReferenceTools.getFitnessActivityIconResById(workout.activityID);
                Drawable drawable =  AppCompatResources.getDrawable(getApplicationContext(), activityIcon);
                Utilities.setColorFilter(drawable, getColor(R.color.primaryLightColor));
                Bitmap bitmap = vectorToBitmap(drawable);

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
                        .setShowWhen(true)
                        .setLargeIcon(bitmap)
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
                sContent = mReferenceTools.workoutNotifyText(currentWorkout);
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationIntent.putExtra(KEY_FIT_WORKOUTID, currentWorkout._id);
                notificationIntent.setAction(INTENT_WORKOUT_REPORT);
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                activityIcon = mReferenceTools.getFitnessActivityIconResById(currentWorkout.activityID);
                Drawable drawable2 =  AppCompatResources.getDrawable(getApplicationContext(), activityIcon);
                Utilities.setColorFilter(drawable2, getColor(R.color.primaryLightColor));
                Bitmap bitmap2 = vectorToBitmap(drawable2);

                // 2. Build the BIG_PICTURE_STYLE.
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                        // Provides the bitmap for the BigPicture notification.
                        // Overrides ContentTitle in the big form of the template.
                        .setBigContentTitle(sTitle)
                        // Summary line after the detail section in the big form of the template.
                        .setSummaryText(sContent);
                // NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setStyle(bigTextStyle)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon((int)iconID)
                        .setOngoing(false)
                        .setUsesChronometer(false)
                        .setLargeIcon(bitmap2)
                        //  .setContentInfo(workout2.notifyText())
                        .setAutoCancel(true)
                       // .setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
/*            case INTENT_ACTIVE_PAUSE:
                break;
            case INTENT_ACTIVE_RESUMED:
                break;
            case INTENT_ACTIVESET_START:
                break;
            case INTENT_ACTIVESET_STOP:
                notificationID = NOTIFICATION_ACTIVE_ID;
                sChannelID = ACTIVE_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(R.drawable.noti_white_logo)
                        .setAutoCancel(false).setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;*/
            case INTENT_GOAL_TRIGGER:
                notificationID = NOTIFICATION_GOAL_ID;
                sChannelID = GOALS_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                long[] pattern = { 0, 100, 500, 100, 500};
                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notify_channel_goals_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(R.drawable.noti_white_logo)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        //.setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_SCHEDULE_TRIGGER:
                notificationID = NOTIFICATION_SCHEDULE_ID;
                sChannelID = MAINTAIN_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        .setSmallIcon(R.drawable.noti_white_logo)
                        .setAutoCancel(true)
                        //.setContentIntent(notificationPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_WORKOUT_REPORT:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    Workout workout1 = (Workout)dataBundle.getParcelable(Workout.class.getSimpleName());
                    if (workout1 == null) return null;
                    sTitle = (workout1.start > 0) ? Utilities.getPartOfDayString(workout1.start) : "";
                    sTitle += Constants.ATRACKIT_SPACE + workout1.activityName;
                    sContent =  mReferenceTools.workoutNotifyText(workout1);
                    // Build the notification with all of the parameters.
                    notifyBuilder = new NotificationCompat
                            .Builder(getApplicationContext(), sChannelID)
                            .setContentTitle(sTitle)
                            .setContentText(sContent)
                            .setSmallIcon(R.drawable.ic_a_outlined)
                            .setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case INTENT_SUMMARY_DAILY:
                notificationID = NOTIFICATION_SUMMARY_ID;
                sChannelID = SUMMARY_CHANNEL_ID;
                notificationIntent.setAction(INTENT_SUMMARY_DAILY);
                notificationPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), notificationID, notificationIntent,
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
                            dataBundle.getFloat(Constants.MAP_BPM_MIN),  dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(Constants.MAP_STEPS));
                // Build the notification with all of the parameters.
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true)
                      //  .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
                default:
                    notificationID = NOTIFICATION_FIREBASE_ID;
                    sChannelID = FIREBASE_CHANNEL_ID;
                    notificationPendingIntent = PendingIntent.getActivity
                            (getApplicationContext(), notificationID, notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    // Build the notification with all of the parameters.
                    notifyBuilder = new NotificationCompat
                            .Builder(getApplicationContext(), sChannelID)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(getString(R.string.notification_text))
                            .setSmallIcon(R.drawable.noti_white_logo)
                            .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                    break;
        }
        return notifyBuilder;
    }

    private Bitmap vectorToBitmap(Drawable drawable){
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            // Handle the error
            return null;
        }
    }

    /**
     * Plays back the MP3 file embedded in the application
     */
    private void playMusic() {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(this, R.raw.nfl_score_chine);
                if (mMediaPlayer != null)
                    mMediaPlayer.setOnCompletionListener(mp -> {
                        // we need to transition to the READY/Home state
                        Log.d(LOG_TAG, "Music Finished");
                        //mUIAnimation.transitionToHome();
                    });
            } else
                mMediaPlayer.start();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "playMusic "+ e.getMessage());
            if (mMediaPlayer != null) mMediaPlayer.release();
        }
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
    /** calculates the distance between two locations in MILES */
    private double distanceCalculation(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = (!bUseKG) ? 3958.75 : 6371; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }
    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        if (ActivityCompat.checkSelfPermission(RoomActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                // Start the activity, the intent will be populated with the speech text
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeCustomAnimation(RoomActivity.this, R.anim.jump_in, R.anim.jump_out);
                ActivityCompat.startActivityForResult(RoomActivity.this,intent,REQUEST_MIC_REQUEST_CODE,options.toBundle());
            }catch (ActivityNotFoundException anf){
                broadcastToast(getString(R.string.action_nospeech_activity));
            }
        }else{
            ActivityCompat.requestPermissions(RoomActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORDING_PERMISSION_CODE);
        }
    }
    /**
     *  handleIntent - action all incoming intent requests
     *
     * @param intent
     */
    private void handleIntent(Intent intent){
        if ((intent == null) || (intent.getAction() == null)) return;
        String intentAction = intent.getAction();
        if (!intentAction.equals(INTENT_MESSAGE_TOAST))
            Log.w(LOG_TAG, "handleIntent " + intentAction);

        if (intentAction.equals(INTENT_MESSAGE_TOAST)){
            String sMessage = intent.getStringExtra(Constants.INTENT_EXTRA_MSG);
            if (sMessage != null) broadcastToast(sMessage);
            return;
        }
        if (intent.hasExtra(Constants.KEY_FIT_HOST)) {  // sent from phone
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
            switch (intentAction) {
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
                                Log.d(LOG_TAG, "Message failed.");
                            }
                        });
            }
            return;  // finished with device message
        }
        if (appPrefs == null) appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
        if ((sUserID.length() == 0)||(sDeviceID.length() == 0)) return;   // IMPORTANT CONSIDERATION !!!
        int currentState = mSavedStateViewModel.getState();
        // Splash callbacks
        if (intent.hasExtra(SplasherActivity.ARG_ACTION)){
            String prevIntent = intent.getStringExtra(SplasherActivity.ARG_ACTION);
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
                }
            }
            if (prevIntent.equals(INTENT_PERMISSION_SENSOR)){
                if (!userPrefs.getAskAge()) userPrefs.setAskAge(true);
                String sSetting = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                if (appPrefs.getUseLocation() || appPrefs.getUseSensors()) doBindSensors();
            }
            if (prevIntent.equals(INTENT_PERMISSION_AGE)){
                if (userPrefs != null){
                    if (!userPrefs.getAskAge()) userPrefs.setAskAge(true);
                    String sSetting = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
                    if (sSetting.length() == 0){
                        startSplashActivityForResult(Constants.INTENT_PERMISSION_HEIGHT);
                    }else{
                        if (!userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED)) {
                            userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_ASKED, true);
                            startSplashActivityForResult(INTENT_PERMISSION_DEVICE);
                        }else
                        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)) startPhonePendingSyncAlarm();
                    }

                }
            }
            if (prevIntent.equals(INTENT_PERMISSION_DEVICE)){
                String sDevice = appPrefs.getLastNodeName();
                if (sDevice.length() > 0) broadcastToast(sDevice);
            }
            return;
        }

        if (intentAction.equals(Constants.INTENT_PERMISSION_LOCATION) || intentAction.equals(Constants.INTENT_PERMISSION_SENSOR)){
            mMessagesViewModel.setUseLocation(appPrefs.getUseLocation());
            if (appPrefs.getUseLocation() || appPrefs.getUseSensors()) doBindSensors();
            if (!appPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED))
                startSplashActivityForResult(Constants.INTENT_PERMISSION_DEVICE);
            return;
        }
        if (intentAction.equals(INTENT_SETUP)){
            // check if permissions are setup install could still be needed.
            Context context = getApplicationContext();
            String sAction = checkApplicationSetup();
            if (sAction.length() > 0){
                appPrefs.setAppSetupCompleted(false);
                createNotificationChannels(); // setup notification manager and channels if needed
                Bundle bundle = new Bundle();
                bundle.putString(INTENT_SETUP, "false");
                sendNotification(Constants.INTENT_SETUP, bundle);

                if ((userPrefs != null) && ((mGoogleAccount != null)&& (mGoogleAccount.getId() != null))) {
                    boolean bSetup = userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED);
                    if (!bSetup) sAction = INTENT_PERMISSION_DEVICE;
                }else
                    sAction = Constants.ATRACKIT_ATRACKIT_CLASS;
                mWorkout = new Workout();
                mWorkout._id = (System.currentTimeMillis());
                mWorkout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                mWorkout.start = -1; mWorkout.end = -1; // setup mode !
                mSavedStateViewModel.setActiveWorkout(mWorkout);
                startSplashActivityForResult(sAction);
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
                    broadcastToast(getString(R.string.label_setup_complete));
                }
                if (((mGoogleAccount == null) || (mGoogleAccount.getId() == null) || (mGoogleAccount.isExpired())) && (!intent.hasExtra(SplasherActivity.ARG_ACTION))) {
                    if (appPrefs.getLastUserLogIn() > 0 && ((System.currentTimeMillis() - appPrefs.getLastUserLogIn()) < TimeUnit.HOURS.toMillis(24)))
                        signInSilent();
                    else {
                        sessionSetCurrentState(WORKOUT_INVALID);
                        signIn();
                    }
                }
            }
        }
        if (intentAction.equals(Constants.INTENT_TOTALS_REFRESH)){
            mMessagesViewModel.setWorkInProgress(false);
            mMessagesViewModel.setWorkType(0);
            if (!intent.hasExtra(KEY_FIT_USER) || !intent.getStringExtra(KEY_FIT_USER).equals(sUserID)) return;
            if (sDeviceID == null || (sDeviceID.length() == 0)) sDeviceID = appPrefs.getDeviceID();
            int iType = intent.getIntExtra(KEY_COMM_TYPE, 2);
            boolean bUpdated = intent.getBooleanExtra(KEY_INDEX_METRIC, false);
            boolean bNew = intent.getBooleanExtra(KEY_INDEX_FILTER, false);
            UserDailyTotals userDailyTotals = (intent.hasExtra(UserDailyTotals.class.getSimpleName()) ? intent.getParcelableExtra(UserDailyTotals.class.getSimpleName()) : null);
            SensorDailyTotals sensorDailyTotals = (intent.hasExtra(SensorDailyTotals.class.getSimpleName()) ? intent.getParcelableExtra(SensorDailyTotals.class.getSimpleName()) : null);
            if (userDailyTotals != null) {
                UserDailyTotals currentUDT = mSessionViewModel.getTopUserDailyTotal(userDailyTotals.userID);
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
                //}

                if (userDailyTotals.distanceTravelled >= 0F) {
                    mMessagesViewModel.addDistanceMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.distanceTravelled));
                }
                if (userDailyTotals.caloriesExpended >= 0F) {
                    mMessagesViewModel.addCaloriesMsg(String.format(Locale.getDefault(), SINGLE_FLOAT, userDailyTotals.caloriesExpended));
                }
                if (userDailyTotals.activeMinutes >= 0) {
                    mMessagesViewModel.addMoveMinsMsg(String.format(Locale.getDefault(), SINGLE_INT, userDailyTotals.activeMinutes));
                }
                if (userDailyTotals.heartIntensity >= 0F) {
                    mMessagesViewModel.addHeartPtsMsg(String.format(Locale.getDefault(), SINGLE_INT, Math.round(userDailyTotals.heartIntensity)));
                }
            }
            if ((sensorDailyTotals != null) && (iType == 2)){
                SensorDailyTotals currentSDT = mSessionViewModel.getTopSensorDailyTotal(sensorDailyTotals.userID);
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
                sensorDailyTotals = mSessionViewModel.getTopSensorDailyTotal(sUserID);
            if (userDailyTotals == null) userDailyTotals = mSessionViewModel.getTopUserDailyTotal(sUserID);
            if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE)) {
                long lastSignIn = userPrefs.getLastUserSignIn();
                int state = mSavedStateViewModel.getState();
                if (sensorDailyTotals == null) sensorDailyTotals = mSessionViewModel.getTopSensorDailyTotal(userDailyTotals.userID);
                if (userDailyTotals == null) userDailyTotals = mSessionViewModel.getTopUserDailyTotal(sUserID);
                if (phoneNodeId == null || phoneNodeId.length() == 0) phoneNodeId = appPrefs.getLastNodeID();
                if (phoneNodeId.length() != 0)
                    new TaskSendSensoryInfo(RoomActivity.this, phoneNodeId, sUserID, sDeviceID, lastSignIn,state,sensorDailyTotals,userDailyTotals).run();
            }
            return;
        }
        // requests without workout context
        if (!intent.hasExtra(Constants.KEY_FIT_WORKOUTID)
                && !intent.hasExtra(Workout.class.getSimpleName())) {
            if (intentAction.equals(Constants.INTENT_NETWORK_CHECK)) {
                doNetworkCheck();
                return;
            }
            if (intentAction.equals(INTENT_BIND_DEVICE)){
                //sDeviceId = intent.getStringExtra(KEY_FIT_DEVICE_ID);
                String sUserId = intent.getStringExtra(KEY_FIT_USER);
                if (!this.sUserID.equals(sUserId)) return;
                int sensorType = intent.getIntExtra(KEY_FIT_TYPE,1);
                long period = intent.getLongExtra(KEY_FIT_TIME, 120);
                long delay = intent.getLongExtra(KEY_FIT_ACTION, 0);
                Log.e(LOG_TAG,"Adding SensorControl " + sensorType + " " + delay + " " + period);
                if (BoundSensorManager.getCount(sensorType) != 0) BoundSensorManager.doReset(sensorType);
                bindSensorListener(sensorType,Math.toIntExact(delay));
                return;
            }
            if (intentAction.equals(INTENT_PHONE_SYNC)) {
                String sDeviceId = (intent.hasExtra(KEY_FIT_DEVICE_ID) ? intent.getStringExtra(KEY_FIT_DEVICE_ID) : ATRACKIT_EMPTY);
                if (!intent.hasExtra(KEY_FIT_USER) || !intent.getStringExtra(KEY_FIT_USER).equals(sUserID)) return;
                String sUserId = intent.getStringExtra(KEY_FIT_USER);
                Log.w(LOG_TAG, INTENT_PHONE_SYNC + " " + sUserId);
                if (userPrefs.getPrefByLabel(LABEL_DEVICE_USE) && sDeviceId.length() > 0 && sUserId.length() > 0) {
                    Log.w(LOG_TAG, INTENT_PHONE_SYNC + " " + sUserId + " " + sDeviceId);
                    if ((currentState != WORKOUT_LIVE && currentState != WORKOUT_PAUSED))
                        new TaskSyncPhoneNode(sUserId, sDeviceId).run();
                    else {
                        DataMap dataMap = new DataMap();
                        dataMap.putInt(KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_INFO);
                        dataMap.putInt(KEY_FIT_TYPE, (int) 0); // new request
                        dataMap.putString(KEY_FIT_USER, sUserId);
                        dataMap.putString(KEY_FIT_DEVICE_ID, sDeviceId);
                        if (mGoogleAccount != null)
                            dataMap.putString(KEY_FIT_NAME, mGoogleAccount.getDisplayName());
                        if (phoneNodeId == null || phoneNodeId.length() == 0)
                            phoneNodeId = appPrefs.getLastNodeID();
                        if (phoneNodeId.length() != 0)
                            sendMessage(phoneNodeId, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                        else
                            sendCapabilityMessage(Constants.PHONE_CAPABILITY_NAME, Constants.MESSAGE_PATH_PHONE_SERVICE, dataMap);
                        }
                }else{
                    Log.w(LOG_TAG, "NOT " +INTENT_PHONE_SYNC + " " + sUserId + " " + sDeviceId);
                }
                return;
            }
            if (intentAction.equals(INTENT_SUMMARY_DAILY)) {
                doDailySummaryJob();
                mHandler.postDelayed(new Runnable() {
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
            if (intentAction.equals(INTENT_ACTIVE_LOGIN) || intentAction.equals(Intent.ACTION_MAIN)){
                if (!appPrefs.getUseSensors())
                    broadcastToast(getString(R.string.sensors_permission_not_used));
                if (!appPrefs.getUseLocation())
                    broadcastToast(getString(R.string.location_permission_not_used));
                String sUser = intent.getStringExtra(KEY_FIT_USER);
                mSavedStateViewModel.setUserIDLive(sUser);
                Intent newIntent = new Intent(INTENT_CLOUD_SYNC);
                newIntent.putExtra(KEY_FIT_USER, sUser);
                if (intent.hasExtra(KEY_FIT_DEVICE_ID))
                    newIntent.putExtra(KEY_FIT_DEVICE_ID, intent.getStringExtra(KEY_FIT_DEVICE_ID));
                else
                    newIntent.putExtra(KEY_FIT_DEVICE_ID, appPrefs.getDeviceID());
                long lastLogin = appPrefs.getLastUserLogIn();
                newIntent.putExtra(KEY_FIT_VALUE, lastLogin);
                newIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                Log.e(LOG_TAG, "ACTIVE_LOGIN about to startupJob");
                new TaskStartupJobs(newIntent).run();
                return;
            }

            if (intentAction.equals(INTENT_HOME_REFRESH) || intent.hasExtra(KEY_FIT_ACTION)){
                mWearableNavigationDrawer.setIsLocked((currentState == WORKOUT_LIVE));
                mMessagesViewModel.setWorkInProgress(false);
                mMessagesViewModel.setWorkType(0);
                Log.e(LOG_TAG, "received INTENT_HOME_REFRESH");
                if (intent.hasExtra(KEY_FIT_ACTION)){
                    String sAction = intent.getStringExtra(KEY_FIT_ACTION);
                    if (sAction.equals(Constants.LABEL_DEVICE_USE))
                        startSplashActivityForResult(INTENT_PERMISSION_DEVICE);

                    if (sAction.equals(Constants.KEY_CHANGE_STATE)){
                        int state = intent.hasExtra(KEY_FIT_VALUE) ? intent.getIntExtra(KEY_FIT_VALUE ,0) : WORKOUT_INVALID;
                        Log.e(LOG_TAG, "about to change state: " + state);
                        if (state == WORKOUT_INVALID) {
                            createWorkout(sUserID, sDeviceID);
                            mWorkout._id = 2;
                            createWorkoutSet();
                            mSavedStateViewModel.setActiveWorkout(mWorkout);  // trigger state change
                            mSavedStateViewModel.setActiveWorkoutSet(null);
                            mSavedStateViewModel.setActiveWorkoutMeta(null);
                            mSavedStateViewModel.setToDoSets(new ArrayList<>());
                        }
                        sessionSetCurrentState(state);
                    }else
                        Log.e(LOG_TAG, "not changing state" );
                }
                else{
                    Log.w(LOG_TAG, "INTENT_HOME_REFRESH REFRESH IMAGE ");
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_room_fragment);
                    if (Objects.requireNonNull(navHostFragment.getNavController().getCurrentDestination()).getId() == R.id.homeFragment) {
                        try {
                            for (Fragment frag : navHostFragment.getChildFragmentManager().getFragments()) {
                                if (frag.isVisible() && (frag instanceof RoomFragment)) {
                                    ((RoomFragment) frag).refreshPersonalImage();
                                }
                            }
                        } catch (NullPointerException ne) {
                            FirebaseCrashlytics.getInstance().recordException(ne);
                            Log.e(LOG_TAG, "null pointer image refresh " + ne.getMessage());
                        }
                    }
                }
            }

            if (intentAction.equals(Constants.INTENT_CLOUD_SKU)) doSkuLookupJob(intent.getStringExtra(KEY_FIT_USER),intent.getStringExtra(KEY_FIT_DEVICE_ID));

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
            if ((sUserID.length() > 0) && !TextUtils.equals(sUserID,intentUserId)){
                // do not handle intents for wrong user
                Log.w(LOG_TAG, "incoming intent user not equal " + intentUserId);
                return;
            }
        }
      //  if ((mWorkout == null) && mSavedStateViewModel.isSessionSetup()) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
      //  if ((mWorkoutSet == null) && mSavedStateViewModel.isSessionSetup()) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
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
        }
        else{
            if (intent.hasExtra(Constants.KEY_FIT_WORKOUTID)) {
                lWorkoutID = intent.getLongExtra(Constants.KEY_FIT_WORKOUTID, 0);
                if (lWorkoutID > 0)
                    if (lWorkoutID > 1)
                        arrivingWorkout = mSessionViewModel.getWorkoutById(lWorkoutID, sUserID,sDeviceID);
                    else
                        arrivingWorkout = mSessionViewModel.getWorkoutById(lWorkoutID, sUserID, ATRACKIT_EMPTY);
                if (intent.hasExtra(Constants.KEY_FIT_WORKOUT_SETID)) {
                    lWorkoutSetID = intent.getLongExtra(Constants.KEY_FIT_WORKOUT_SETID, 0);
                    if (lWorkoutSetID > 0) arrivingWorkoutSet = mSessionViewModel.getWorkoutSetById(lWorkoutSetID, sUserID, sDeviceID);
                    List<WorkoutMeta> metaList = mSessionViewModel.getWorkoutMetaByWorkoutID(lWorkoutID,sUserID,sDeviceID);
                    if (metaList != null && metaList.size() > 0) arrivingWorkoutMeta = metaList.get(0);
                }
            }
        }
        if ((lWorkoutID > 0) && (sUserID.length() > 0) && (arrivingWorkout != null)){
            sets = mSessionViewModel.getWorkoutSetByWorkoutID(lWorkoutID, sUserID, sDeviceID);
        }
        int arrivingState = Utilities.currentWorkoutState(arrivingWorkout);
        if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)){
            if (((mWorkout == null) || (mWorkout._id != lWorkoutID)) && (arrivingWorkout != null)){
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
            Log.e(LOG_TAG, "handleIntent " + intentAction + sTemp);
        }else {
            Log.e(LOG_TAG, "handleIntent null set " + intentAction);
            if ((sets != null) && sets.size() > 0) mWorkoutSet = sets.get(0); else return;
        }
        // alive but
        if (((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED)) && (mWorkout != null)){
            if ((arrivingWorkout != null) && (arrivingWorkout._id != mWorkout._id)){
                broadcastToast("Live session tracking - cannot action new session");
                return;
            }
            if (arrivingState == WORKOUT_COMPLETED) {
                broadcastToast("Live session tracking - cannot action a completed request");
                return;
            }
        }
        else {
         /*   if (intentAction.equals(INTENT_WORKOUT_REPORT)) {
                Intent intentReport = new Intent(this, ReportActivity.class);
                intentReport.putExtra(Workout.class.getName(),arrivingWorkout);
                intentReport.putExtra(WorkoutSet.class.getName(),arrivingWorkoutSet);
                startActivityForResult(intent, REQUEST_REPORT_CODE);
                return;
            } */
            if (((mWorkout != null) && (arrivingWorkout != null)) && (arrivingWorkout._id != mWorkout._id)) {
                // workout related intents
                if (intentAction.equals(Intent.ACTION_VIEW) || intentAction.contains("com.a_track_it.com.workout.workout")) {
                    mWorkout = arrivingWorkout;
                    if (arrivingWorkoutSet != null) mWorkoutSet = arrivingWorkoutSet;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    int state = mSavedStateViewModel.getState();
                   // if (arrivingState != state && state != WORKOUT_LIVE && state != WORKOUT_PAUSED) mSavedStateViewModel.setCurrentState(arrivingState);
                   // sessionSetCurrentState(arrivingState);
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
                                                mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
                                                mWorkoutSet = set;
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
                        mMessagesViewModel.addLiveIntent(refreshIntent);
                    }
                    return; // all done -
                }
            } // end of if different
        }

        if (intentAction.equals(Constants.INTENT_ACTIVE_START)) {
            int doNext = (intent.hasExtra(KEY_FIT_VALUE)) ? intent.getIntExtra(KEY_FIT_VALUE,1) : 1;
            if (!hasOAuthPermission(1)){
                requestOAuthPermission(1);
                return;
            }
            if (!isExternal) {
                // int dirtyCount = mSavedStateViewModel.getDirtyCount();
                // check if current set needs to be added
                if (mSavedStateViewModel.getDirtyCount() > 0)
                    if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                        if (!mWorkoutSet.isValid(false) && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)) {
                            String sMsg = ATRACKIT_EMPTY;
                            if ((mWorkoutSet.exerciseName == null) || (mWorkoutSet.exerciseName.length() == 0)) {
                                sMsg = getString(R.string.edit_set_exercise);
                            } else {
                                if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0)) {
                                    sMsg = getString(R.string.edit_set_reps);
                                } else if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY) && ((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F))) {
                                    sMsg = getString(R.string.edit_set_weight);
                                }
                            }
                            if (sMsg.length() > 0)
                                doAlertDialogMessage(sMsg);
                            return;
                        }
                        else
                            if (mWorkoutSet.isValid(false) && (mWorkoutSet.workoutID <= 2))
                                sessionBuild(false);
                            else
                                mSessionViewModel.deleteWorkoutSet(mWorkoutSet);
                    }
                    else{
                        if (mWorkoutSet.workoutID <=2)
                            sessionBuild(false);

                    }
                if ((mWorkout.start == 0L))
                    sessionStart();
                else
                    if (doNext == 1)
                        sessionStartNextSet();
                    else
                        sessionResume();
            }else
                showAlertDialogConfirm(ACTION_STARTING,null);

        }
        if (intentAction.equals(INTENT_ACTIVESET_START)){
            if (!isExternal) {
                if (intent.hasExtra(KEY_FIT_ACTION)){
                    sessionRepeatSet();
                    sessionResume();
                }else {
                    if (intent.hasExtra(KEY_FIT_VALUE)) {
                        if (intent.getIntExtra(KEY_FIT_VALUE, 1) == 0)
                            sessionResume();
                        else
                            sessionStartNextSet();
                    } else
                        sessionStartNextSet();
                }
            }else
                showAlertDialogConfirm(ACTION_START_SET,null);
        }
        if (intentAction.equals(INTENT_ACTIVE_PAUSE)){
            if (isExternal) {
                showAlertDialogConfirm(ACTION_PAUSING,null);
            }else {
                sessionPause();
               // if (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment){
               //     mNavigationController.popBackStack();
              //  }
            }
        }

        if (intentAction.equals(INTENT_ACTIVE_RESUMED)){
            if (isExternal)
                if ((mWorkout.start == 0) || (mWorkout.start > 0 && mWorkout.end > 0) || (mWorkout._id == 1) || (mWorkout._id == 2))
                    showAlertDialogConfirm(ACTION_STARTING,null);
                else
                    showAlertDialogConfirm(ACTION_RESUMING,null);
            else {
                if ((mWorkout.start == 0) || (mWorkout.start > 0 && mWorkout.end > 0) || (mWorkout._id == 1) || (mWorkout._id == 2)){
                    sessionStart();
                    return;
                }else {
                    sessionResume();
                }
            }
        }
        if (intentAction.equals(Constants.INTENT_ACTIVE_STOP)) {
            if (!isExternal) {
                sessionStop();
                if (intent.hasExtra(KEY_FIT_ACTION)) {
                    String sAction = intent.getStringExtra(KEY_FIT_ACTION);
                    if (sAction.equals(Constants.KEY_FIT_ACTION_QUIT))
                        quitApp();
                }else{
                    if (mNavigationController.getCurrentDestination().getId() != R.id.homeFragment){
                        mNavigationController.popBackStack();
                    }
                }
            }else
                showAlertDialogConfirm(Constants.ACTION_STOPPING,null);

        }



        if (intentAction.equals(INTENT_ACTIVESET_STOP)){
            if (!isExternal) {
                sessionCompleteActiveSet(System.currentTimeMillis());

                long pStart = SystemClock.elapsedRealtime();
                mSavedStateViewModel.setPauseStart(pStart);  // measuring current pause
                sessionSetCurrentState(WORKOUT_PAUSED);
                // go to the gym confirm fragment
                if (Utilities.isGymWorkout(mWorkout.activityID)){
                    if (mWorkout.scoreTotal == Constants.FLAG_NON_TRACKING || mWorkoutSet.scoreTotal == FLAG_NON_TRACKING){
                        sessionStop();
                    }else {
                        mSwipeDismissFrameLayout.setEnabled(false);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                        bundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                        if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment)
                            mNavigationController.navigate(R.id.action_homeRoomFragment_to_gymConfirmFragment, bundle);
                    }
                }else
                    sessionStop();

            }else{
                showAlertDialogConfirm(ACTION_END_SET,null);
                return;
            }

        }
        if (intentAction.equals(Constants.INTENT_INPROGRESS_RESUME)){
            if ((currentState != WORKOUT_LIVE) && (currentState != WORKOUT_PAUSED)) {
                DateTuple dateTuple = mSessionViewModel.getInProgressCountDates(sUserID,sDeviceID);
                if ((dateTuple != null) && (dateTuple.sync_count > 0)) {
                    broadcastToast("In progress " + dateTuple.sync_count);
                    startCustomList(Constants.SELECTION_WORKOUT_INPROGRESS, "", sUserID);
                }
            }
        }
        if (intentAction.equals(INTENT_WORKOUT_EDIT)){
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
            if (mNavigationController.getCurrentDestination().getId() == R.id.homeFragment) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(Workout.class.getSimpleName(),mWorkout);
                bundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                bundle.putParcelable(WorkoutMeta.class.getSimpleName(), mWorkoutMeta);
                mNavigationController.navigate(R.id.action_homeRoomFragment_to_entryRoomFragment, bundle);
            }
            return;
        }
        if (intentAction.equals(INTENT_WORKOUT_DELETE)){
            long workoutID = intent.getLongExtra(Constants.KEY_FIT_WORKOUTID,0);
            long setID = intent.getLongExtra(Constants.KEY_FIT_WORKOUT_SETID,0);
            int type = intent.getIntExtra(KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT);
            String personID = intent.getStringExtra(KEY_FIT_USER);
            if (!personID.equals(sUserID)) return;
            if (!isExternal) {
                if ((workoutID == 0 || (type == OBJECT_TYPE_WORKOUT_SET)) && (setID > 0)){
                    WorkoutSet set = mSessionViewModel.getWorkoutSetById(setID,personID, ATRACKIT_EMPTY);
                    if (set != null){
                        mSessionViewModel.deleteWorkoutSet(set);
                        mSavedStateViewModel.setToDoSets(mSessionViewModel.getWorkoutSetByWorkoutID(set.workoutID,set.userID,set.deviceID));
                    }
                    personID = getString(R.string.action_delete) + "d!";
                    broadcastToast(personID);
                }
            }else
                showAlertDialogConfirm(ACTION_DELETE_WORKOUT,null);
            return;
        }
        if (intentAction.equals(Constants.INTENT_SET_DELETE)){
            long workoutID = intent.getLongExtra(Constants.KEY_FIT_WORKOUTID,0);
            long setID = intent.getLongExtra(Constants.KEY_FIT_WORKOUT_SETID,0);
            int type = intent.getIntExtra(KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT_SET);
            String personID = intent.getStringExtra(KEY_FIT_USER);
            if (!personID.equals(sUserID)) return;
            if (!isExternal) {
                if ((workoutID > 0) && (type == OBJECT_TYPE_WORKOUT_SET) && (setID > 0)){
                    WorkoutSet set = mSessionViewModel.getWorkoutSetById(setID,personID, ATRACKIT_EMPTY);
                    if (set != null){
                        mSessionViewModel.deleteWorkoutSet(set);
                        List<WorkoutSet> list = mSessionViewModel.getWorkoutSetByWorkoutID(set.workoutID,set.userID,set.deviceID);
                        // TODO: notify fragment
                        mSavedStateViewModel.setToDoSets(list);
                    }
                    personID = getString(R.string.action_delete) + "d!";
                    broadcastToast(personID);
                }
            }else
                showAlertDialogConfirm(ACTION_DELETE_SET,null);
            return;
        }
        if (intentAction.equals(Constants.INTENT_GOAL_TRIGGER)){
            String sKey = KEY_FIT_TYPE;
            Intent vibIntent = new Intent(Constants.INTENT_VIBRATE);
            vibIntent.putExtra(KEY_FIT_TYPE, 3);
            mMessagesViewModel.addLiveIntent(vibIntent);
            if (intent.hasExtra(sKey)) {
                Bundle resultData = new Bundle();
                resultData.putLong(KEY_FIT_WORKOUTID,intent.getLongExtra(KEY_FIT_WORKOUTID, 0));
                resultData.putInt(KEY_FIT_TYPE, intent.getIntExtra(sKey,0));
                resultData.putLong(KEY_FIT_ACTION, intent.getLongExtra(KEY_FIT_ACTION,0));
                resultData.putString(KEY_FIT_USER, sUserID);
                resultData.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO)) playMusic();
                if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION)) sendNotification(Constants.INTENT_GOAL_TRIGGER, resultData);
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
