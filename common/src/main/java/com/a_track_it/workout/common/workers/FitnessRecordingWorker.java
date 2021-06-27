package com.a_track_it.workout.common.workers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.AppExecutors;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.R;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.service.ExerciseDetectedService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.Context.NOTIFICATION_SERVICE;

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
        try {
            mContext = getApplicationContext();
            ReferencesTools referencesTools = ReferencesTools.setInstance(mContext);
            ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(mContext);
            String sUserID = appPrefs.getLastUserID();
            gsa =  GoogleSignIn.getAccountForExtension(mContext, referencesTools.getFitnessSignInOptions(3));
            if (gsa == null || (gsa.isExpired() || ((sUserID.length() > 0) && !sUserID.equals(gsa.getId())))){
                Log.e(LOG_TAG,"no previous login account ");
                doGetSilentGoogleAccountCallable getSilentCallable = new doGetSilentGoogleAccountCallable(mContext);
                GoogleSignInAccount googleSignInAccount = null;
                try {
                    Future<GoogleSignInAccount> future = AppExecutors.getInstance().diskService().submit(getSilentCallable);
                    googleSignInAccount = future.get(5, TimeUnit.MINUTES);
                    if (googleSignInAccount == null) {
                        Log.e(LOG_TAG,"fail to silent sign-in ");
                        return Result.failure();
                    }
                    Log.w(LOG_TAG,"successful silent sign-in " + googleSignInAccount.getDisplayName());
                    gsa = googleSignInAccount;
                }
                catch (ExecutionException e) {
                    Log.e(LOG_TAG,"fail execution exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG,"fail interrupted exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                } catch (TimeoutException e) {
                    Log.e(LOG_TAG,"fail timeout exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                }
            }
            if (gsa == null){
                return Result.failure();
            }

            int record_action = getInputData().getInt("record_action", 1);
            if (record_action == 1) {
                subscribeRecordingApiByDataType(DataType.TYPE_HEART_RATE_BPM);
                subscribeRecordingApiByDataType(DataType.TYPE_STEP_COUNT_DELTA);
                subscribeRecordingApiByDataType(DataType.TYPE_DISTANCE_DELTA);
                subscribeRecordingApiByDataType(DataType.TYPE_CALORIES_EXPENDED);
                subscribeRecordingApiByDataType(DataType.TYPE_MOVE_MINUTES);
                subscribeRecordingApiByDataType(DataType.TYPE_HEART_POINTS);
                subscribeRecordingApiByDataType(DataType.TYPE_LOCATION_SAMPLE);
                subscribeRecordingApiByDataType(DataType.TYPE_ACTIVITY_SEGMENT);
                subscribeRecordingApiByDataType(DataType.TYPE_WORKOUT_EXERCISE);
                subscribeRecordingApiByDataType(DataType.TYPE_POWER_SAMPLE);
                doRegisterDetectionService(gsa);
            } else {
                unsubscribeRecordingApiByDataType(DataType.TYPE_HEART_RATE_BPM);
                unsubscribeRecordingApiByDataType(DataType.TYPE_STEP_COUNT_DELTA);
                unsubscribeRecordingApiByDataType(DataType.TYPE_DISTANCE_DELTA);
                unsubscribeRecordingApiByDataType(DataType.TYPE_CALORIES_EXPENDED);
                unsubscribeRecordingApiByDataType(DataType.TYPE_MOVE_MINUTES);
                unsubscribeRecordingApiByDataType(DataType.TYPE_HEART_POINTS);
                unsubscribeRecordingApiByDataType(DataType.TYPE_LOCATION_SAMPLE);
                unsubscribeRecordingApiByDataType(DataType.TYPE_ACTIVITY_SEGMENT);
                unsubscribeRecordingApiByDataType(DataType.TYPE_WORKOUT_EXERCISE);
                unsubscribeRecordingApiByDataType(DataType.TYPE_POWER_SAMPLE);
                doUnRegisterDetectionService(gsa);
            }
            Log.e(LOG_TAG,"Recordering worked action " + record_action);
            return Result.success();
        }
        catch (Exception e){
          //  if (e instanceof RuntimeException) handleTaskError(e);
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "recordingWorker " + e.getMessage());
            return Result.failure();
        }
    }
    private void buildPendingIntentNotification(String sTitle, String sContent, PendingIntent pendingIntent){
        Context context = getApplicationContext();
        int notificationID = Constants.NOTIFICATION_SUMMARY_ID;
        Bitmap bitmap = null;
        PackageManager pm = context.getPackageManager();
        boolean bWear = pm.hasSystemFeature(PackageManager.FEATURE_WATCH);
        //Drawable home = ContextCompat.getDrawable(context,R.drawable.ic_launcher_home);
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_home); // vectorToBitmap(home);
        }catch (Exception ee){
            Log.e(LOG_TAG, "load bitmap error " + ee.getMessage());
        }
        if (bitmap == null){
            Log.e(LOG_TAG, "loading bitmap is null" );
        }
        IconCompat bubbleIcon = IconCompat.createWithResource(context, R.drawable.ic_a_outlined);
        NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder(pendingIntent,bubbleIcon).setAutoExpandBubble(true).build();
        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(context, Constants.MAINTAIN_CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sContent).setBigContentTitle(sTitle))
                .setBubbleMetadata(metaBubble)
                .setSmallIcon(R.drawable.ic_a_outlined)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (bWear){
            NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch, sTitle, pendingIntent).build();
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView);
            notifyBuilder.extend(extender);
        }
        NotificationManager mNotifyManager =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyManager != null)
            mNotifyManager.notify(notificationID, notifyBuilder.build());
    }
    private void handleTaskError(Exception ex){
        //Exception ex = task.getException();
        if (ex instanceof ApiException) {
            ApiException apie = ((ApiException) ex);
            PendingIntent pi = null;
            if (apie.getStatus().hasResolution()) pi = apie.getStatus().getResolution();
            if (pi == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail().requestId()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                pi = PendingIntent.getActivity(getApplicationContext(), Constants.REQUEST_SIGNIN_SYNC,signInIntent,PendingIntent.FLAG_ONE_SHOT);
            }
            String sTitle = "Click to continue Google refresh";
            String sContent = "Unable to continue - click to resolve";
            buildPendingIntentNotification(sTitle, sContent, pi);
        }
    }
    private static class doGetSilentGoogleAccountCallable implements Callable<GoogleSignInAccount> {
    private Context mContext;

    doGetSilentGoogleAccountCallable(Context c){
        mContext = c;
    }

    @Override
    public GoogleSignInAccount call() throws Exception {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.CLIENT_ID)
                    .requestEmail().build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(mContext, gso);
            Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
            return signInAccountTask.getResult();
        }catch (Exception e){
            throw e;
        }
    }
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
        final ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
        Boolean defaultValue = appPrefs.getPrefByLabel(sLabel);
        try {
            if (defaultValue)  // only if turned on
                Fitness.getRecordingClient(mContext, gsa)
                    .subscribe(dataType)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(LOG_TAG, "Successfully subscribed! " + dataType.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "There was a problem subscribing." + dataType.getName());
                            appPrefs.setPrefByLabel(sLabel,false);  // turn it off

                        }
                    });
        }catch (Exception e){
            Log.e(LOG_TAG, "Recording client subscribe error" + e.getMessage());
            throw e;
            // isRecordingAPIRunning = false;
        }
        // [END subscribe_to_datatype]
    }
    private void unsubscribeRecordingApiByDataType(final DataType dataType) {
        try{
            String sLabel = mContext.getString(R.string.label_subs) + dataType.getName();
            final ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
            Boolean defaultValue = appPrefs.getPrefByLabel(sLabel);
            if (defaultValue)
                Fitness.getRecordingClient(mContext, gsa).unsubscribe(dataType).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "Successfully un-subscribed for data type: " + dataType.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Un-subscribe failure " + dataType.getName() + " " + e.getMessage());
                    }
                });
        }catch (Exception e){
            throw e;
        }
        // [END unsubscribe_to_datatype]

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


}