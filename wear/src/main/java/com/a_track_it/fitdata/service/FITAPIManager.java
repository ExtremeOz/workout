package com.a_track_it.fitdata.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.wearable.phone.PhoneDeviceType;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.activity.IDataLoaderCallback;
import com.a_track_it.fitdata.activity.MainActivity;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.DataQueries;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Bodypart;
import com.a_track_it.fitdata.common.model.Exercise;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;
import com.a_track_it.fitdata.model.GSONHelper;
import com.a_track_it.fitdata.model.UserPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.wearable.intent.RemoteIntent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_ID;
import static com.a_track_it.fitdata.common.Constants.SESSION_PREFIX;
import static com.a_track_it.fitdata.common.Constants.STATE_HOME;
import static java.text.DateFormat.getTimeInstance;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FITAPIManager extends IntentService implements IDataLoaderCallback,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String PACKAGE_NAME =
            "com.a_track_it.fitdata.service.FITAPIManager";

    public static final String TAG = FITAPIManager.class.getSimpleName();
    /**
     * The name of the channel for notifications.
     */
    private Node mAndroidPhoneNodeWithApp;

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    public static final String DATE_FORMAT = "dd.MM h:mm";

    private static final String LOCATION_UPDATE_ACTION =
            "com.a_track_it.fitdata.location.LOCATION_UPDATE";
    public static final int ACTION_POPULATE_HISTORY = 1;
    public static final int ACTION_SUBSCRIBE_RECORDING = 2;
    public static final int ACTION_UNSUBSCRIBE_RECORDING = 3;


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = (1000 * 30);
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = (1000 * 60);
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS_IDLE = (1000 * 60 * 5);
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS_IDLE = (1000 * 60);


    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;
    private boolean mustStopSelf = false;
    private GoogleApiClient mClient;
    private Context mContext;
    private GSONHelper mGSONHelper;
    private ReferencesTools mRefTools;
    private List<WeakReference<IFITAPIManager>> mListeners = new ArrayList<>();
    private ArrayList<Workout> historyWorkouts = new ArrayList<>();
    private ArrayList<WorkoutSet> historySets = new ArrayList<>();

    private int mCurrentAction = 0;
    private boolean refreshInProgress = false;
    private boolean mConnectedClient = false;
    private boolean mNetworkConnected = false;
    private String mSessionID;
    private int retryCount;
    private boolean mGetImageInProgress = false;
    private boolean mGetLocationOverride = false;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;
    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    /**
     * The current location.
     */
    private Location mLocation;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    public FITAPIManager() {
        super(TAG);
    }
    // Links to install mobile app for both Android (Play Store) and iOS.
    // TODO: Replace with your links/packages.
    private static final String ANDROID_MARKET_APP_URI =
            "market://details?id=com.example.android.wearable.wear.wearverifyremoteapp";

    // TODO: Replace with your links/packages.
    private static final String APP_STORE_APP_URI =
            "https://itunes.apple.com/us/app/android-wear/id986496028?mt=8";

    // Result from sending RemoteIntent to phone to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == RemoteIntent.RESULT_OK) {
             //   new ConfirmationOverlay().showOn(MainWearActivity.this);

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
            //    new ConfirmationOverlay()
            //            .setType(ConfirmationOverlay.FAILURE_ANIMATION)
            //            .showOn(MainWearActivity.this);

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public int getCurrentAction(){
        return mCurrentAction;
    }
    public void setAction(int action){ mCurrentAction = action;}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mConnectedClient = true;
        Log.d(TAG, "Connection success");
        notifyListenersConnected();


        if (mCurrentAction == ACTION_POPULATE_HISTORY)
            populateHistoricalData();
        if (mCurrentAction == ACTION_SUBSCRIBE_RECORDING)
            subscribeRecordingApi();
        if (mCurrentAction == ACTION_UNSUBSCRIBE_RECORDING)
            unsubscribeRecordingApi();
    }

    public GoogleApiClient GetGoogleApiClient(){
        return mClient;
    }

    public GSONHelper getGSONHelper(){
        return mGSONHelper;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Connection suspended ");
        notifyListenersDisconnected(i);
        mConnectedClient = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        mConnectedClient = false;
        Log.e(TAG, "Connection failed. Cause: " + result.toString());
        notifyListenersConnectionFailure();
        //TODO: add a timer for connection retries
    }

    @Override
    public void setExerciseList(ArrayList<Exercise> exerciseList) {

    }

    @Override
    public void setActivityList(ArrayList<FitnessActivity> activityList) {

    }

    @Override
    public void setBodypartList(ArrayList<Bodypart> bodypartList) {

    }

    @Override
    public void setActiveWorkout(Workout activeWorkout) {

    }

    @Override
    public void setActiveWorkoutSet(WorkoutSet activeSet) {

    }

    @Override
    public void setToDoWorkoutSets(ArrayList<WorkoutSet> toDoWorkoutSets) {

    }

    @Override
    public void setCompletedWorkouts(ArrayList<Workout> completedWorkouts) {
        String sMsg = "Completed workouts loaded " + Integer.toString(mGSONHelper.getCompletedWorkoutsSize());
        Log.i(TAG, sMsg);
        notifyListenersLoadComplete(null);
    }

    @Override
    public void setCompletedWorkoutSets(ArrayList<WorkoutSet> completedSets) {
        Log.i(TAG, "Completed workout sets loaded " + Integer.toString(mGSONHelper.getCompletedWorkoutSetsSize()));
        notifyListenersLoadComplete(null);
    }

    @Override
    public void actionCompleted(boolean saved, int index, int type) {

    }
    public void getLastLocation(boolean override) {
        try {
            mGetLocationOverride = override;
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                onNewLocation(task.getResult());
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);
        boolean newLocation;
        if (mLocation != null){
            newLocation = (mLocation.getLatitude() != location.getLatitude())
                    && (mLocation.getLongitude() != location.getLongitude());
        }else
            newLocation = true;

        if (newLocation || mGetLocationOverride) {
            mLocation.set(location);  // so we wont send the same next time!
            // Notify anyone listening for broadcasts about the new location.
            Intent intent = new Intent(LOCATION_UPDATE_ACTION);
            intent.putExtra(EXTRA_LOCATION, location);
            sendBroadcast(intent);
        }
        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
          //  mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {

        CharSequence text = getLocationText(mLocation);

        Intent intent = new Intent(this, FITAPIManager.class);
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Context context = getApplicationContext();
        NotificationCompat.Builder builder;
        if (UserPreferences.getFeedbackMute(context))
            builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(R.drawable.ic_launcher_home, getString(R.string.launch_activity),
                            activityPendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                            servicePendingIntent)
                    .setContentText(text)
                    .setContentTitle(getLocationTitle(this))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_home)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis());
        else
            builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(R.drawable.ic_launcher_home, getString(R.string.launch_activity),
                            activityPendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                            servicePendingIntent)
                    .setContentText(text)
                    .setContentTitle(getLocationTitle(this))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_home)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
                    .setVibrate(new long[0]);


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }
    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    private String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    private String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    /**
     * Sets the location request parameters.
     */
    private LocationRequest createLocationRequest(int iRequestType) {
        LocationRequest mLocationRequest = new LocationRequest();
        if (iRequestType == 0){
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS_IDLE);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS_IDLE);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (iRequestType == 1){
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public FITAPIManager getService() {
            // Return this instance of LocalService so clients can call public methods
            return FITAPIManager.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"FITAPIManager Created");

        if (mContext == null){
            mContext = getApplicationContext();
            mGetLocationOverride = true;
            mLocation = new Location(LocationManager.GPS_PROVIDER);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult.getLastLocation() != null) {
                        onNewLocation(locationResult.getLastLocation());
                    }
                }
            };
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mServiceHandler = new Handler(handlerThread.getLooper());

            mGSONHelper = GSONHelper.getInstance(mContext, this);
            mRefTools = ReferencesTools.getInstance();
            mRefTools.init(mContext);

        }
        // mGSONHelper.open(mGSONHelper.LOAD_GOOGLE_WORKOUTS);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNetworkConnected = isNetworkConnected();  // can initiate more freq. than create - test here!
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.i(TAG, "Service OnStartCommand");
        if (mContext == null){
            mGetLocationOverride = true;
            mContext = getApplicationContext();
            mLocation = new Location(LocationManager.GPS_PROVIDER);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult.getLastLocation() != null) onNewLocation(locationResult.getLastLocation());
                }
            };
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mServiceHandler = new Handler(handlerThread.getLooper());

            mGSONHelper = GSONHelper.getInstance(mContext, this);
            mRefTools = ReferencesTools.getInstance();
            mRefTools.init(mContext);
        }
        boolean bNotMute = !UserPreferences.getFeedbackMute(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String text = getString(R.string.app_name);
            NotificationChannel channel = mRefTools.createNotificationChannel(mContext, STATE_HOME, bNotMute);

            Intent serviceIntent = new Intent(this, FITAPIManager.class);
            // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
            serviceIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

            // The PendingIntent that leads to a call to onStartCommand() in this service.
            PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, serviceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            // The PendingIntent to launch activity.
            PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channel.getId())
                    .setSmallIcon(R.drawable.noti_white_logo)
                    .setContentTitle(text)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if (bNotMute) builder.setVibrate(new long[0]);
            if (text.length() > 0) builder.setContentInfo(text);
            builder.setWhen(System.currentTimeMillis())
                   .addAction(R.drawable.noti_white_logo, getString(R.string.launch_activity),activityPendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),servicePendingIntent);

            Notification notification = builder.build();
            Log.i(TAG, "Starting foreground service after onStartCmd");
            startForeground(NOTIFICATION_ID, notification);

            NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID, notification);
        }


        // We got here because the user decided to remove location updates from the notification.
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }else {
            mustStopSelf = true;
        }
        if (intent.hasExtra(Constants.LABEL_USER)){
            String sUser = intent.getStringExtra(Constants.LABEL_USER);
            FIT_Connect(sUser);
            requestLocationUpdates();

        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceHandler != null) mServiceHandler.removeCallbacksAndMessages(null);
        if (mConnectedClient && mClient != null) FIT_Disconnect();
        if ( mGSONHelper != null) mGSONHelper.close();
        Log.d(TAG,"Destroyed");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && UserPreferences.requestingLocationUpdates(getApplicationContext())) {
            Log.i(TAG, "Starting foreground service after unbinding");
            //Notification notification = getNotification();
            //startForeground(NOTIFICATION_ID, notification);
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        UserPreferences.setRequestingLocationUpdates(getApplicationContext(), true);
        //startService(new Intent(getApplicationContext(), FITAPIManager.class));

        try {
            if (mLocationRequest == null)
                mLocationRequest = createLocationRequest(0);

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            UserPreferences.setRequestingLocationUpdates(getApplicationContext(), false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {

            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            UserPreferences.setRequestingLocationUpdates(getApplicationContext(), false);
        } catch (SecurityException unlikely) {
            UserPreferences.setRequestingLocationUpdates(getApplicationContext(), true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface IFITAPIManager {
        void insertWorkout(Workout workout);
        void removeWorkout(Workout workout);
        void onConnected();
        void onConnectionFailure();
        void onDisconnected(int reasonCode);
        void onSessionChanged(int ChangeType, String SessionID, int result);
        void onDataChanged(Utilities.TimeFrame timeFrame);
        void onDataFailure();
        void onDataComplete(Bundle resultData);
    }


    public boolean isRefreshInProgress() {
        return refreshInProgress;
    }
    public boolean isClientConnected(){ return mConnectedClient;}

    public void getConnected(){
        if (!mConnectedClient){
            FIT_Connect(UserPreferences.getUserEmail(mContext));
        }
        return;
    }
    public boolean isNetworkConnected() {

        boolean isConnected = false;
        if (mContext != null) {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            isConnected = cm.getActiveNetworkInfo() != null;
        }

        return isConnected;
    }

    private void notifyListenersSessionChanged(int changeType, String sessionID, int result) {
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onSessionChanged(changeType, sessionID, result);
                }
            }
        }
    }
    private void notifyListenersConnected() {
        Log.v(TAG, "Notifying " + this.mListeners.size() + " listeners that FITAPI manager connected.");
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onConnected();
                }
            }
        }
    }
    private void notifyListenersDisconnected(int reasonCode) {
        Log.v(TAG, "Notifying " + this.mListeners.size() + " listeners that FITAPI manager dis-connected.");
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onDisconnected(reasonCode);
                }
            }
        }
    }
    private void notifyListenersConnectionFailure() {
        Log.v(TAG, "Notifying " + this.mListeners.size() + " listeners that FITAPI manager connection failed.");
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onConnectionFailure();
                }
            }
        }
    }
    private void notifyListenersDataChanged(Utilities.TimeFrame timeFrame){
        Log.v(TAG, "Notifying " + this.mListeners.size() + " listeners of data changed.");
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onDataChanged(timeFrame);
                }
            }
        }
    }

    private void notifyListenersLoadComplete(Bundle data) {
        Log.v(TAG, "Notifying " + this.mListeners.size() + " listeners of data load complete.");
        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference<IFITAPIManager> ref = this.mListeners.get(i);
            if (ref != null) {
                IFITAPIManager InterfaceFITAPI = ref.get();
                if (InterfaceFITAPI != null) {
                    InterfaceFITAPI.onDataComplete(data);
                }
            }
        }
    }

    public void addListener(IFITAPIManager listener) {

        int i = 0;
        for(int z = this.mListeners.size(); i < z; ++i) {
            WeakReference ref = (WeakReference)this.mListeners.get(i);
            if(ref != null && ref.get() == listener) {
                return;
            }
        }
        Log.v(TAG, "Added " + listener.getClass().getName() + " to listeners.");
        this.mListeners.add(new WeakReference<>(listener));
    }

    public void removeListener(IFITAPIManager listener) {
        Iterator i = this.mListeners.iterator();
        Log.v(TAG, "Removing " + listener.getClass().getName() + " from listeners.");
        while(true) {
            IFITAPIManager item;
            do {
                if(!i.hasNext()) {
                    return;
                }
                WeakReference ref = (WeakReference)i.next();
                item = (IFITAPIManager)ref.get();
            } while(item != listener && item != null);

            i.remove();
        }
    }

    /**
     *  Build a_track_it.com {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a_track_it.com known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or
     *  having multiple accounts on the device and needing to specify which account to use, etc.
     */
    private  GoogleApiClient buildFitnessClient(String sEmail) {
        Log.i(TAG, "Creating the Google API Client with context: " + this.getClass().getName() + " name " + sEmail);
        // Create the Google API Client
        return new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Fitness.SENSORS_API)
                .setAccountName(sEmail)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(LocationServices.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    public void FIT_Connect(String sEmail){
        if (mClient == null || !mConnectedClient) {
            mClient = buildFitnessClient(sEmail);
            Log.v(TAG, "Connecting client.");
            mClient.connect();
        } else if (!mClient.isConnecting() && !mClient.isConnected()) {
            Log.v(TAG, "Re-connecting client.");
            mClient.reconnect();
        }else{
            if (mClient != null && mClient.isConnected()){
                mConnectedClient = true;
                Log.d(TAG, "Connection success");
                notifyListenersConnected();
                if (mCurrentAction == ACTION_POPULATE_HISTORY)
                    populateHistoricalData();
                if (mCurrentAction == ACTION_SUBSCRIBE_RECORDING)
                    subscribeRecordingApi();
                if (mCurrentAction == ACTION_UNSUBSCRIBE_RECORDING)
                    unsubscribeRecordingApi();
            }
        }
    }
    public void FIT_Disconnect() {
        if (mClient != null && (mClient.isConnected() || mClient.isConnecting())) {
            Log.v(TAG, "Disconnecting client.");
            mClient.disconnect();
            if (mustStopSelf){
                mustStopSelf = false;
                StopService();
            }
        }

        mClient = null;
        mConnectedClient = false;
    }
    public boolean mustStopSelf(){
        return mustStopSelf;
    }

    public void StopService(){
        Log.v(TAG, "Stopping Self.");
        stopSelf();
    }
    public void startLiveFitSession(Workout mWorkout, GoogleSignInAccount mGoogleAccount){
        String sDesc = mWorkout.shortText();
        String sTimeOfDayName = Utilities.getPartOfDayString(mWorkout.start) + getString(R.string.my_space_string) + mWorkout.activityName;
        Device device = Device.getLocalDevice(this);
        String sID = Long.toString(mWorkout._id);
        mSessionID = SESSION_PREFIX + sID;
        final String finalID = mSessionID;
        Log.i(TAG, "SESSION API for " + mWorkout.activityName + " " + Utilities.getDateString(mWorkout.start));
        if (isNetworkConnected()) {
            Session session = new Session.Builder().setName(sTimeOfDayName)
                    .setIdentifier(mSessionID)
                    .setStartTime(mWorkout.start, TimeUnit.MILLISECONDS)
                    .setActivity(mWorkout.identifier).setDescription(sDesc).build();
            Task<Void> response = Fitness.getSessionsClient(mContext, mGoogleAccount).startSession(session);
            response.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Start Session success " + finalID + " " + Boolean.toString(task.isSuccessful()));
                        notifyListenersSessionChanged(0, finalID, 1);
                    } else {
                        Log.w(TAG, "Start Session NOT successful " + finalID + " " + Boolean.toString(task.isSuccessful()));
                        notifyListenersSessionChanged(0, finalID, 0);
                    }
                }
            });
        }else{
            Log.w(TAG, "Start Session NOT successful  OFFLINE" );
            notifyListenersSessionChanged(0, finalID, 0);          // PLAN B is offline recording!
        }
    }
    public void stopLiveFitSession(Workout mWorkout, GoogleSignInAccount mGoogleAccount){
       String sID = Long.toString(mWorkout._id);
       mSessionID = SESSION_PREFIX + sID;
       final String finalID = mSessionID;
       Log.i(TAG, "SESSION API STOP for " + mSessionID + " " + mWorkout.activityName + " " + Utilities.getDateString(mWorkout.start));
       if (isNetworkConnected()) {
           Task<List<Session>> response = Fitness.getSessionsClient(mContext, mGoogleAccount)
                   .stopSession(mSessionID);
           response.addOnCompleteListener(new OnCompleteListener<List<Session>>() {
               @Override
               public void onComplete(@NonNull Task<List<Session>> task) {
                   if (task.isSuccessful()) {
                       Log.i(TAG, "Stop Session success " + Boolean.toString(task.isSuccessful()));
                       notifyListenersSessionChanged(1, finalID, 1);
                   } else {
                       Log.w(TAG, "Stop Session NOT successful " + Boolean.toString(task.isSuccessful()));
                       notifyListenersSessionChanged(1, finalID, 0);
                   }
               }
           });
       }else{
           Log.w(TAG, "Stop Session NOT successful" );
           notifyListenersSessionChanged(1, finalID, 0);  // PLAN B is offline recording!
       }
    }
    public void insertExerciseSet(long start, String exerciseName, int  resistance_type, int repCount, float weightTotal, String packageName, Device device, GoogleSignInAccount mGoogleAccount){
        Log.i(TAG, "insertExerciseSet " + exerciseName + " " + Utilities.getTimeDateString(start));
        //long timeMs = System.currentTimeMillis();
        if (start == 0) start =  System.currentTimeMillis();
        DataSet dataSet = DataQueries.createExerciseDataSet(start,exerciseName,repCount,resistance_type,weightTotal,packageName,device);
        final String finalStart = Long.toString(start);
        Fitness.getHistoryClient(mContext,mGoogleAccount).insertData(dataSet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "insertExerciseSet success ");
                notifyListenersSessionChanged(3, finalStart,1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "insertExerciseSet fail " + e.getMessage());
                notifyListenersSessionChanged(3, finalStart,0);
            }
        });
    }
    public void insertSegmentSet(long start, String segmentName, long  end, String sActivityName, String packageName, Device device, GoogleSignInAccount mGoogleAccount){
        Log.i(TAG, "insertSegmentSet " + segmentName + " " + Utilities.getDateString(start));
        if (start == 0) start =  System.currentTimeMillis();
        DataSet dataSet = DataQueries.createActivityDataSet(start, end, segmentName,sActivityName, packageName, device);
        final String finalStart = Long.toString(start);
        Fitness.getHistoryClient(mContext,mGoogleAccount).insertData(dataSet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "insertSegmentSet ");
                notifyListenersSessionChanged(2,finalStart , 1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "insertSegmentSet fail " + e.getMessage());
                notifyListenersSessionChanged(2, finalStart,0);
            }
        });
/*        if (mClient != null)
            Fitness.HistoryApi.insertData(mClient, dataSet).setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                @Override
                public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                    if (status.isSuccess()){
                        Log.i(TAG, "insertExerciseSet " + status.getStatusMessage());
                        notifyListenersSessionChanged(2,finalStart , 1);
                    }else{
                        Log.e(TAG, "insertExerciseSet " + status.getStatusMessage());
                        notifyListenersSessionChanged(2,finalStart , 0);
                    }
                }
            });
        else notifyListenersSessionChanged(2,finalStart , 0);*/
    }

    public void populateHistoricalData() {
            new ReadHistoricalDataTask().execute();
    }

    private class ReadHistoricalDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            // Setting a_track_it.com start and end date using a_track_it.com range of 1 month before this moment.
            Calendar cal = Calendar.getInstance();

            Date now = new Date();
            cal.setTime(now);
            // You might be in the middle of a_track_it.com fitdata, don't cache the past two hours of data.
            // This could be an issue for workouts longer than 2 hours. Special case for that?
            //cal.add(Calendar.HOUR_OF_DAY, -2);
            long endTime;
            long startTime;

            if (mContext != null && isNetworkConnected()) {
                refreshInProgress = true;
                long lastSync = UserPreferences.getLastSync(mContext);
                if (! isClientConnected()) getConnected();
                // Update step count
                cal.setTime(now);
                endTime = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 1);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                int numberOfDays = 3;  //30;
                if (lastSync > 0) {
                    lastSync -=  (1000 * 60 * 60 * 2);
                    if (lastSync < startTime) {
                        double diff =  startTime - lastSync;
                        if (diff / (1000 * 60 * 60 * 24) < 30) {
                            numberOfDays = (int)Math.floor(diff / (1000 * 60 * 60 * 24));
                            numberOfDays += 1;
                        }
                    } else {
                        numberOfDays = 1;
                    }
                }
                int totalSteps = 0;
                numberOfDays = 4;
                Log.i(TAG, "Loading " + numberOfDays + " days step count");
                for (int i = 0; i < numberOfDays; i++) {
                    DataReadRequest stepCountRequest = DataQueries.queryStepEstimate(startTime, endTime);
 /*                   Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext)).readData(stepCountRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            int stepCount = 0;
                            if (dataReadResponse.getBuckets().size() > 0) {
                                //Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        stepCount += parseDataSet(dataSet);
                                    }

                                }
                            } else if (dataReadResponse.getDataSets().size() > 0) {
                                //Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    stepCount += parseDataSet(dataSet);
                                }
                            }

                            Log.d(TAG, "wrote segments to cache");
                            Workout workout = new Workout();
                            workout.start = startTime;
                            workout.end = endTime;
                            workout._id = startTime;
                            workout.activityID = Utilities.WORKOUT_TYPE_STEPCOUNT;
                            workout.activityName = "Step Count";
                            workout.stepCount = stepCount;
                            totalSteps += stepCount;
                            //fitdata.duration = 1000*60*10;
                            if (numberOfDays < 10) {
                                Log.i(TAG, "Step count: " + workout.toString());
                            }
                            if (stepCount > 0) {
                                historyWorkouts.add(workout);
                            } else {
                                Log.w(TAG, "Warning: step count is 0");
                            }
                            endTime = startTime;
                            cal.add(Calendar.DAY_OF_YEAR, -1);
                            startTime = cal.getTimeInMillis();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // TODO: Notify listeners of failure
                            notifyListenersDataChanged(Utilities.TimeFrame.ALL_TIME);
                            notifyListenersLoadComplete();
                        }
                    });    */
                    final DataReadResult stepCountReadResult = Fitness.HistoryApi.readData(mClient, stepCountRequest).await(5, TimeUnit.MINUTES);
                    if (stepCountReadResult.getStatus().isSuccess()) {
                        retryCount = 0;
                        int stepCount = countStepData(stepCountReadResult);
                        Workout workout = new Workout();
                        workout.start = startTime;
                        workout.end = endTime;
                        workout._id = startTime;
                        workout.activityID = Constants.WORKOUT_TYPE_STEPCOUNT;
                        workout.activityName = "Step Count";
                        workout.stepCount = stepCount;
                        totalSteps += stepCount;
                        //fitdata.duration = 1000*60*10;
                        if (numberOfDays < 10) {
                            Log.i(TAG, "Step count: " + workout.toString());
                        }
                        if (stepCount > 0) {
                            historyWorkouts.add(workout);
                        } else {
                            Log.w(TAG, "Warning: step count is 0");
                        }
                        endTime = startTime;
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        startTime = cal.getTimeInMillis();
                    } else {
                        refreshInProgress = false;
                        //closeDatabase();
                        if (retryCount < 3) {
                            Log.i(TAG, "Attempting to reconnect client.");
                            retryCount++;
                            mClient.reconnect();
                        } else {
                            final Context activityContext = mContext;
                            if (activityContext instanceof Activity) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        GoogleApiAvailability gApi = GoogleApiAvailability.getInstance();
                                        gApi.getErrorDialog((Activity) activityContext, stepCountReadResult.getStatus().getStatusCode(), 0).show();
                                        Log.w(TAG, "FAILURE: Unable to read data.");

                                    }
                                });

                            }
                            // TODO: Notify listeners of failure

                            notifyListenersDataChanged(Utilities.TimeFrame.ALL_TIME);
                            notifyListenersLoadComplete(null);
                        }

                        return null;
                    }
                }
                Log.i(TAG, "Loaded " + numberOfDays + " days. Step count: " + totalSteps);

                // Update activities
                cal.setTime(now);
                endTime = cal.getTimeInMillis();
                if (lastSync > 0) {
                    Log.i(TAG, "Fast data read starting: " + Utilities.getTimeDateString(lastSync));
                    startTime = lastSync;
                } else {
                    Log.i(TAG, "Slow data read");
                    cal.setTime(now);
                    cal.add(Calendar.DAY_OF_YEAR, -90);
                    startTime = cal.getTimeInMillis();
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

                Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
                Log.i(TAG, "Range End: " + dateFormat.format(endTime));

                boolean wroteDataToCache = false;

                // Load today
                long dayStart = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                if (startTime <= dayStart && dayStart < endTime) {
                    Log.i(TAG, "Loading today");
                    // Estimated steps and duration by Activity
                    DataReadRequest activitySegmentRequest = DataQueries.queryActivitySegment(dayStart, endTime, isNetworkConnected());
                    Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext)).readData(activitySegmentRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            boolean wroteDataToCache = false;
                            for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
                            }
                            if (wroteDataToCache) Log.d(TAG, "wrote segments to cache");
                        }
                    });


                    DataReadRequest exerciseActivityRequest = DataQueries.querySessionExercises(dayStart, endTime, isNetworkConnected());
                    Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext)).readData(exerciseActivityRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            boolean wroteDataToCache = false;
                            for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
                            }
                            if (wroteDataToCache) Log.d(TAG, "wrote google history to cache");
                            if (wroteDataToCache){
                                if (historyWorkouts.size() > 0) saveWorkoutsToJSON(historyWorkouts,mGSONHelper.LOAD_GOOGLE_WORKOUTS);
                                if (historySets.size() > 0) saveSetsToJSON(historySets,mGSONHelper.LOAD_COMPETED_SETS);
                            }

                        }
                    });

                    endTime = dayStart;
                    notifyListenersDataChanged(Utilities.TimeFrame.BEGINNING_OF_DAY);
                }

                // Load week
/*
                long weekStart = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_WEEK);

                Log.i(TAG, "Range Start: " + dateFormat.format(weekStart));
                Log.i(TAG, "Range End: " + dateFormat.format(endTime));
                if(startTime <= weekStart && weekStart < endTime) {
                    Log.i(TAG, "Loading week");
                    // Estimated steps and duration by Activity
                    DataReadRequest activitySegmentRequest = DataQueries.queryActivitySegment(weekStart, endTime, isNetworkConnected());
                    DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, activitySegmentRequest).await(10, TimeUnit.MINUTES);
                    wroteDataToCache = writeActivityDataToCache(dataReadResult);
                    endTime = weekStart;
                    notifyListenersDataChanged(Utilities.TimeFrame.BEGINNING_OF_WEEK);
                }
                boolean hasMoreThanOneWeek = false;
                // Load rest
                if (startTime < endTime) {

                    hasMoreThanOneWeek = true;
                    Log.i(TAG, "Range Start: " + startTime);
                    Log.i(TAG, "Range End: " + endTime);
                    Log.i(TAG, "Loading rest");
                    DataReadRequest activitySegmentRequest = DataQueries.queryActivitySegment(startTime, endTime, isNetworkConnected());
                    DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, activitySegmentRequest).await(15, TimeUnit.MINUTES);
                    wroteDataToCache = writeActivityDataToCache(dataReadResult);
                }
*/

                cal.setTime(now);
                Log.i(TAG, "Background load complete");
                if (wroteDataToCache) {
                    UserPreferences.setLastSync(mContext, cal.getTimeInMillis());
                    UserPreferences.setBackgroundLoadComplete(mContext, true);
                }
                if (wroteDataToCache){
                    mGSONHelper.save(true, mGSONHelper.LOAD_GOOGLE_WORKOUTS);
                }
                refreshInProgress = false;
                notifyListenersDataChanged(Utilities.TimeFrame.ALL_TIME);
                notifyListenersLoadComplete(null);

                // Read cached data and calculate real time step estimates
                //populateReport();
            }

            return null;
        }
    }

    public void saveWorkoutsToJSON(ArrayList<Workout> workouts, int loadType){
        mGSONHelper.deleteAll(loadType);
        if (loadType == mGSONHelper.LOAD_ACTIVE_WORKOUTS){
            for(Workout w: workouts){
                mGSONHelper.addActiveWorkout(w);
            }
        }
        if (loadType == mGSONHelper.LOAD_COMPLETED_WORKOUTS){
            mGSONHelper.addCompletedWorkouts(workouts);
        }
        if (loadType == mGSONHelper.LOAD_GOOGLE_WORKOUTS){
            for(Workout w: workouts) {
                mGSONHelper.addGoogleWorkout(w);
            }
        }
        mGSONHelper.setContext(getApplicationContext());
        mGSONHelper.save(false, loadType);
        return;
    }

    public void saveSetsToJSON(ArrayList<WorkoutSet> sets, int loadType){
        mGSONHelper.deleteAll(loadType);
        if (loadType == mGSONHelper.LOAD_ACTIVE_SETS){
            for(WorkoutSet s: sets){
                mGSONHelper.addActiveWorkoutSet(s);
            }
        }
        if (loadType == mGSONHelper.LOAD_COMPETED_SETS){
            //mGSONHelper.addCompletedWorkoutSets(sets);
        }
        if (loadType == mGSONHelper.LOAD_TO_DO_SETS){
                mGSONHelper.addToDoSets(sets);
        }
        mGSONHelper.setContext(getApplicationContext());
        mGSONHelper.save(false, loadType);
        return;
    }

    private boolean writeActivityDataToCache(DataReadResult dataReadResult) {
        boolean wroteDataToCache = false;
        for (DataSet dataSet : dataReadResult.getDataSets()) {
            wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
        }
        return wroteDataToCache;
    }

    /**
     * Walk through all activity fields in a_track_it.com segment dataset and writes them to the cache. Used to
     * store data to display in reports and graphs.
     *
     * @param dataSet set of data from the Google Fit API
     */
    private boolean writeDataSetToCache(DataSet dataSet) {
        boolean wroteDataToCache = true;
        int setCount = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            // Populate db cache with data
            for(Field field : dp.getDataType().getFields()) {
                Log.v(TAG, "dp " + field.getName() + " type " + dp.getDataType().getName());
                if (field.getName().equals("activity") && dp.getDataType().getName().equals("com.google.activity.segment")) {
//                    Log.i(TAG, dp.getOriginalDataSource().getAppPackageName());
                    //  dp.getVersionCode();
                    final long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                    final int activity = dp.getValue(field).asInt();
                    Log.i(TAG, "Found: " + Integer.toString(activity) + " entered by: " + dp.getOriginalDataSource().getAppPackageName());
                    Workout workout;
                    final DataPoint myDP = dp;
                    if (mGSONHelper != null) {
                        if (mGSONHelper.getLoadedSize(mGSONHelper.LOAD_GOOGLE_WORKOUTS) > 0) {
                            workout = mGSONHelper.getWorkoutById(mGSONHelper.LOAD_GOOGLE_WORKOUTS, startTime);
                            if (workout != null)
                                Log.w(TAG, "Warning: we found this workout already in GOOGLE Workouts!");
                        }
                    } else {
                        Log.w(TAG, "Warning: mGSONHelper is null");
                        return false;
                    }

                    // When the workout is null, we need to cache it. If the background task has completed,
                    // then we have at most 8 - 12 hours of data. Recent data is likely to change so over-
                    // write it.
                    if (mContext != null && isClientConnected()) {
                        //if (fitdata == null || UserPreferences.getBackgroundLoadComplete(mContext)) {
                        final long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                        DataReadRequest readRequest = DataQueries.queryStepCount(startTime, endTime);
                        Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext)).readData(readRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                int stepCount = 0;
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    stepCount += parseDataSet(dataSet);
                                }
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        stepCount += parseDataSet(dataSet);
                                    }
                                }
                                if (stepCount > 0){
                                    Log.d(TAG, "wrote segments to cache");
                                   Workout workOut = new Workout();
                                    workOut._id = startTime;
                                    workOut.start = startTime;
                                    workOut.duration = endTime - startTime;
                                    workOut.stepCount = stepCount;
                                    workOut.activityID = activity;
                                    workOut.activityName = mRefTools.getFitnessActivityTextById(activity);
                                    workOut.packageName = myDP.getOriginalDataSource().getAppPackageName();
                                    Log.v(TAG, "Put Cache: regionID - " + workOut.activityName + " " + workOut.duration);
                                    if (workOut.duration > 0) {
                                        historyWorkouts.add(workOut);
                                    } else {
                                        Log.w(TAG, "Warning: duration is 0");
                                    }
                                }

                            }
                        });
                    } else {
                        wroteDataToCache = false;
                    }

                }else{   // not an activity segment
                    Log.w(TAG, "Not an activity data point");
                    if (dp.getDataType().getName().equals("com.google.activity.exercise")) {
                        WorkoutSet set = new WorkoutSet();
                        set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                        if (dp.getEndTime(TimeUnit.MILLISECONDS) > 0){
                            set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                            set.duration = set.end - set.start;
                        }
                        set._id = set.start;
                        String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                        int iExerciseID = mRefTools.getFitnessActivityIdByText(sExercise);
                        if (iExerciseID > 0) set.exerciseID = iExerciseID; // if it fits our list
                        set.exerciseName = sExercise;
                        set.resistance_type = dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();
                        set.setCount = ++setCount;
                        set.weightTotal = dp.getValue(Field.FIELD_WEIGHT).asFloat();
                        set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
                        if (set.duration > 0)
                            set.wattsTotal = set.duration * (set.weightTotal * set.repCount);
                        set.activityName = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
                        set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                        historySets.add(set);
                        wroteDataToCache = true;
                    }
                }
            }
        }
        return wroteDataToCache;
    }

    /**
     * Count step data for a_track_it.com bucket of step count deltas.
     *
     * @param dataReadResult Read result from the step count estimate Google Fit call.
     * @return Step count for data read.
     */
    private int countStepData(DataReadResult dataReadResult) {
        int stepCount = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            //Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    stepCount += parseDataSet(dataSet);
                }

            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            //Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                stepCount += parseDataSet(dataSet);
            }
        }

        return stepCount;
    }
    /**
     * Walk through all fields in a_track_it.com step_count dataset and return the sum of steps. Used to
     * calculate step counts.
     *
     * @param dataSet set of data from the Google Fit API
     */
    private int parseDataSet(DataSet dataSet) {
        int dataSteps = 0; String sName = "";
        for (DataPoint dp : dataSet.getDataPoints()) {
            // Accumulate step count for estimate
            sName = dp.getDataType().getName();
            Log.i(TAG,"data point name " + sName);
            if(sName.equals("com.google.step_count.delta")) {
                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG," field name " + field.getName());
                    if (dp.getValue(field).asInt() > 0) {
                        dataSteps += dp.getValue(field).asInt();
                    }
                }
            }
        }
        return dataSteps;
    }
    public void readHistory(WorkoutSet set){
        new ReadHistoryTask().execute(set);
    }

    public class ReadHistoryTask extends AsyncTask<WorkoutSet, Void, Void> {
        protected Void doInBackground(WorkoutSet... params) {

            WorkoutSet workoutSet = params[0];
            if ((workoutSet != null) && (workoutSet.start > 0)) readLiveActivitySegment(workoutSet.start, workoutSet.end);
            return null;
        }
    }

    private void readLiveActivitySegment(long StartTime, long EndTime){

        long timeMs = System.currentTimeMillis();
        if (EndTime > 0) timeMs = EndTime;
        final DataReadRequest readRequest = DataQueries.queryActivitySegmentDetail(StartTime, timeMs);
        Log.d(TAG, "readRequest " + readRequest.toString());
        final DataReadResult dataReadResponse = Fitness.HistoryApi.readData(mClient, readRequest).await(5,TimeUnit.MINUTES);
        if (dataReadResponse.getStatus().isSuccess()){
            boolean wroteDataToCache = false;
            Bundle resultBundle = new Bundle();
            Log.i(TAG, "Successful read session " + dataReadResponse.getDataSets().size());
            for (DataSet dataSet : dataReadResponse.getDataSets()) {
                //wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
                List<DataPoint> dpList = dataSet.getDataPoints();
                for (DataPoint dp : dpList) {
                    if (dp.getDataType().getName().equals("com.google.activity.exercise")) {
                        WorkoutSet set = new WorkoutSet();
                        set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                        if (dp.getEndTime(TimeUnit.MILLISECONDS) > 0) {
                            set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                            set.duration = set.end - set.start;
                        }
                        set._id = set.start;
                        String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                        int iExerciseID = mRefTools.getFitnessActivityIdByText(sExercise);
                        if (iExerciseID > 0)
                            set.exerciseID = iExerciseID; // if it fits our list
                        set.exerciseName = sExercise;
                        set.resistance_type = dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();

                        set.weightTotal = dp.getValue(Field.FIELD_WEIGHT).asFloat();
                        set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
                        if (set.duration > 0)
                            set.wattsTotal = set.duration * (set.weightTotal * set.repCount);
                        set.activityName = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
                        set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                        Log.d(TAG, "dp " + set.shortText());
                        wroteDataToCache = true;
                        resultBundle.putParcelable("WorkoutSet", set);
                    }
                    if (dp.getDataType().getName().equals("com.google.activity.sample")) {
                        Workout workout = new Workout();
                        workout.activityID = dp.getValue(Field.FIELD_ACTIVITY).asInt();
                        workout.activityName = mRefTools.getFitnessActivityTextById(workout.activityID);
                        float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
                        workout.wattsTotal = confidence;
                        Log.d(TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence);
                        wroteDataToCache = true;
                        resultBundle.putParcelable("Workout", workout);
                    }
                    if (dp.getDataType().getName().equals("com.google.power.sample")) {
                        float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
                        Workout workout = new Workout();
                        workout.activityName = "Power Sample";
                        workout.wattsTotal = watts;
                        Log.d(TAG, "dp power sample " + watts);
                        resultBundle.putParcelable("Watts", workout);
                        wroteDataToCache = true;
                    }
                }
            }
            if (wroteDataToCache) Log.d(TAG, "received activity history");

            notifyListenersLoadComplete(resultBundle);
        }else{
            notifyListenersLoadComplete(null);

        }
    }

    public void subscribeRecordingApi(){
        subscribeRecordingApiByDataType(DataType.TYPE_HEART_RATE_BPM);
        subscribeRecordingApiByDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
        subscribeRecordingApiByDataType(DataType.TYPE_CALORIES_EXPENDED);
        subscribeRecordingApiByDataType(DataType.TYPE_MOVE_MINUTES);
        subscribeRecordingApiByDataType(DataType.TYPE_LOCATION_SAMPLE);
        subscribeRecordingApiByDataType(DataType.TYPE_ACTIVITY_SAMPLES);
        subscribeRecordingApiByDataType(DataType.TYPE_WORKOUT_EXERCISE);
        subscribeRecordingApiByDataType(DataType.TYPE_POWER_SAMPLE);
        notifyListenersLoadComplete(null);
    }

    public void unsubscribeRecordingApi(){
        dumpSubscriptionsListByType(DataType.TYPE_HEART_RATE_BPM);
        dumpSubscriptionsListByType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
        dumpSubscriptionsListByType(DataType.TYPE_CALORIES_EXPENDED);
        dumpSubscriptionsListByType(DataType.TYPE_MOVE_MINUTES);
        dumpSubscriptionsListByType(DataType.TYPE_LOCATION_SAMPLE);
        dumpSubscriptionsListByType(DataType.TYPE_ACTIVITY_SAMPLES);
        dumpSubscriptionsListByType(DataType.TYPE_WORKOUT_EXERCISE);
        dumpSubscriptionsListByType(DataType.TYPE_POWER_SAMPLE);
        notifyListenersLoadComplete(null);
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
        try {
/*            Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .subscribe(dataType)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Successfully subscribed! " + dataType.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "There was a problem subscribing." + dataType.getName());
                        }
                    });  */
            if (mClient != null)
                Fitness.RecordingApi.subscribe(mClient, dataType)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode()
                                        == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.i(TAG, "Existing subscription detected for " + dataType.getName());
                                } else {
                                    Log.i(TAG, "Successfully subscribed to "+ dataType.getName());
                                }
                                //TODO: change how this is set and tracked
                                if ((mContext != null) && (dataType == DataType.TYPE_ACTIVITY_SAMPLES)){
                                  //  Toast.makeText(context, "Successfully subscribed!", Toast.LENGTH_LONG).show();
                                    UserPreferences.setActivityTracking(mContext, true);
                                }
                            } else {
                                Log.i(TAG, "There was a_track_it.com problem subscribing "+ dataType.getName());
                                UserPreferences.setActivityTracking(mContext, false);
                            }
                        }
                    });
        }catch (Exception e){
            Log.e(TAG, "Recording client subscribe error" + e.getMessage());
            // isRecordingAPIRunning = false;
        }
        // [END subscribe_to_datatype]
    }


    /**
     * Fetches a list of all active subscriptions and log it. Since the logger for this sample
     * also prints to the screen, we can see what is happening in this way.
     */
    private void dumpSubscriptionsListByType(DataType dataType) {
        // [START dumpSubscriptionsListByType]

/*            Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this)).listSubscriptions(dataType).addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                @Override
                public void onSuccess(List<Subscription> subscriptions) {
                    for(Subscription sc : subscriptions){
                        DataType dt = sc.getDataType();
                        Log.i(TAG, "Dump subscription found data type: " + dt.getName());
                        cancelRecordingSubscription(dt);
                    }
                }
            });*/
        if (mClient != null)
            Fitness.RecordingApi.listSubscriptions(mClient, dataType).setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                for(Subscription sc : listSubscriptionsResult.getSubscriptions()){
                    DataType dt = sc.getDataType();
                    cancelRecordingSubscription(dt);
                }
            }
        });
        // [END dumpSubscriptionsListByType]
    }


    /**
     * Cancels the subscription by calling unsubscribe on that {@link DataType}.
     */
    // [START cancelRecordingSubscription]
    private void cancelRecordingSubscription(DataType dataType) {
        final String dataTypeStr = dataType.toString();
        Log.i(TAG, "Un-subscribing from data type : " + dataTypeStr);
        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
/*        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this)).unsubscribe(dataType).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Successfully un-subscribed for data type: " + dataTypeStr);
            }
        });*/

        if (mClient != null)
            Fitness.RecordingApi.unsubscribe(mClient, dataType).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.equals(Status.RESULT_SUCCESS)){
                    Log.i(TAG, "Successfully un-subscribed for data type: " + dataTypeStr);
                }else
                    Log.i(TAG, "UnSuccessfully un-subscribed for data type: " + dataTypeStr);
            }
        });
        // [END unsubscribe_from_datatype]
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
    /** Clears all the logging message in the LogView. */

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    private void openAppInStoreOnPhone() {
        Log.d(TAG, "openAppInStoreOnPhone()");

        int phoneDeviceType = PhoneDeviceType.getPhoneDeviceType(getApplicationContext());
        switch (phoneDeviceType) {
            // Paired to Android phone, use Play Store URI.
            case PhoneDeviceType.DEVICE_TYPE_ANDROID:
                Log.d(TAG, "\tDEVICE_TYPE_ANDROID");
                // Create Remote Intent to open Play Store listing of app on remote device.
                Intent intentAndroid =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(ANDROID_MARKET_APP_URI));

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentAndroid,
                        mResultReceiver);
                break;

            // Paired to iPhone, use iTunes App Store URI
            case PhoneDeviceType.DEVICE_TYPE_IOS:
                Log.d(TAG, "\tDEVICE_TYPE_IOS");

                // Create Remote Intent to open App Store listing of app on iPhone.
                Intent intentIOS =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(APP_STORE_APP_URI));

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentIOS,
                        mResultReceiver);
                break;

            case PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN:
                Log.d(TAG, "\tDEVICE_TYPE_ERROR_UNKNOWN");
                break;
        }
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private Node pickBestNodeId(Set<Node> nodes) {
        Log.d(TAG, "pickBestNodeId(): " + nodes);

        Node bestNodeId = null;
        // Find a nearby node/phone or pick one arbitrarily. Realistically, there is only one phone.
        for (Node node : nodes) {
            bestNodeId = node;
        }
        return bestNodeId;
    }
    public void setDownload(Uri sURI){
        if (mGetImageInProgress) return;
        try {
            URL url = new URL(sURI.toString());
            AsyncTask mMyTask = new DownloadTask().execute(url);
        }catch (java.net.MalformedURLException mal){
            Log.d(TAG, "malFormed " + sURI);
        }
    }

    private class DownloadTask extends AsyncTask<URL,Void,Bitmap>{
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start
          //  mProgressDialog.show();
            mGetImageInProgress = true;
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            // Hide the progress dialog
          //  mProgressDialog.dismiss();

            if(result!=null){
                // Display the downloaded image into ImageView
                //mImageView.setImageBitmap(result);

                // Save bitmap to internal storage
                Uri imageInternalUri = saveImageToInternalStorage(result);
                UserPreferences.setLastUserPhotoInternal(mContext, imageInternalUri.toString());
                mGetImageInProgress = false;
                // Set the ImageView image from internal storage
               // mImageViewInternal.setImageURI(imageInternalUri);
            }
        }

        // Custom method to save a bitmap into internal storage
        protected Uri saveImageToInternalStorage(Bitmap bitmap){
            // Initialize ContextWrapper
            ContextWrapper wrapper = new ContextWrapper(mContext);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
            // Initializing a new file
            // The bellow line return a directory in internal storage
            File file = wrapper.getDir(Constants.OUTPUT_PATH,MODE_PRIVATE);
            // Create a file to save the image
            file = new File(file, timeStamp+".jpg");

            try{
                // Initialize a new OutputStream
                OutputStream stream = null;

                // If the output file exists, it can be replaced or appended to it
                stream = new FileOutputStream(file);

                // Compress the bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

                // Flushes the stream
                stream.flush();

                // Closes the stream
                stream.close();

            }catch (IOException e) // Catch the exception
            {
                e.printStackTrace();
            }

            // Parse the gallery image url to uri
            Uri savedImageURI = Uri.parse(file.getAbsolutePath());

            // Return the saved image Uri
            return savedImageURI;
        }
    }



}
