package com.a_track_it.fitdata.service;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;

import java.lang.ref.WeakReference;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReadHistoryIntentService extends IntentService implements FITAPIManager.IFITAPIManager, ServiceConnection {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_READ = "com.a_track_it.fitdata.service.action.READ";
    public static final String ACTION_EXERCISE = "com.a_track_it.fitdata.service.action.EXERCISE";

    public static final String EXTRA_START = "com.a_track_it.fitdata.service.extra.START";
    public static final String EXTRA_END = "com.a_track_it.fitdata.service.extra.END";
    public static final String EXTRA_REC = "com.a_track_it.fitdata.service.extra.RECEIVER";
    public static final String EXTRA_ACT = "com.a_track_it.fitdata.service.extra.ACCOUNT";

    private WeakReference<ResultReceiver> mReceiver;
    private FITAPIManager mService;
    private long mStart;
    private boolean mBound = false;
    private long mEnd;
    private String mAction;
    private String mAccount;
    private FITAPIManager.LocalBinder binder;

    public ReadHistoryIntentService() {
        super("ReadHistoryIntentService");
    }

    /**
     * Starts this service to perform action ACTION_READ with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionReadHistory(Context context, long param1, long param2, String account, ResultReceiver callback) {
        Intent intent = new Intent(context, ReadHistoryIntentService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_START, param1);
        intent.putExtra(EXTRA_END, param2);
        intent.putExtra(EXTRA_REC, callback);
        intent.putExtra(EXTRA_ACT, account);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionExercise(Context context, long param1, long param2, ResultReceiver callback) {
        Intent intent = new Intent(context, ReadHistoryIntentService.class);
        intent.setAction(ACTION_EXERCISE);
        intent.putExtra(EXTRA_START, param1);
        intent.putExtra(EXTRA_END, param2);
        intent.putExtra(EXTRA_REC, callback);
        context.startService(intent);
    }


    @Override
    public void onConnected() {

    }

    @Override
    public void onConnectionFailure() {

    }

    @Override
    public void onDisconnected(int reasonCode) {

    }

    @Override
    public void onSessionChanged(int ChangeType, String SessionID, int result) {

    }

    @Override
    public void onDataChanged(Utilities.TimeFrame timeFrame) {

    }

    @Override
    public void onDataComplete(Bundle results) {
        Log.w("ReadHistoryIntentService", "onDataComplete " + Boolean.toString(results == null));
        Bundle bundle = new Bundle();
        bundle.putParcelable("workoutList", results);
        ResultReceiver receiver = mReceiver.get();
        if(receiver != null) {
            receiver.send(200, results);
            Log.d("ReadHistoryIntentService", "listener sent ." + results.toString());
        }else {
            Log.w("ReadHistoryIntentService", "Weak listener is NULL.");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mService = null;
        mBound = false;
        Intent intent = new Intent(getApplicationContext(), FITAPIManager.class);
        bindService(intent,this, Service.BIND_AUTO_CREATE);
        Log.d("ReadHistoryIntentService", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBound){
            if (mService != null) mService.removeListener(this);
            unbindService(this);
            mBound = false;
        }
        Log.d("ReadHistoryIntentService", "onDestroy");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("ReadHistoryIntentService", "onHandleIntent");
            final String action = intent.getAction();
            ResultReceiver resultReceiver = intent.getParcelableExtra(EXTRA_REC);
            mReceiver = new WeakReference<>(resultReceiver);
            mStart = intent.getLongExtra(EXTRA_START, 0);
            mEnd = intent.getLongExtra(EXTRA_END, 0);
            mAccount = intent.getStringExtra(EXTRA_ACT);
            mAction = action;
            if (mBound) {
                Log.d("ReadHistoryIntentService", "already Bound proceeding now");
                if (ACTION_READ.equals(action)) {
                    if (mService != null) handleActionReadHistory();
                } else if (ACTION_EXERCISE.equals(action)) {
                    if (mService != null) handleActionExercise();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionReadHistory(){
        WorkoutSet set = new WorkoutSet();
        set.start = mStart;
        set.end = mEnd;
        mService.readHistory(set);
        Log.d("ReadHistoryIntentService", "readHistory");


/*            Bundle bundle = new Bundle();
            Workout workout = new Workout();
            workout._id = mStart;
            workout.start = mStart;
            workout.end = mEnd;
            workout.activityID = 80;
            workout.activityName = "Strength training";*/
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionExercise() {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (FITAPIManager.LocalBinder) service;
        Log.d("ReadHistoryIntentService", "onServiceConnected");
        mService = binder.getService();
        mService.addListener(ReadHistoryIntentService.this);
        mBound = true;
        if ((mAction != null) && (mAction.length() > 0)) {
            Log.d("ReadHistoryIntentService", "ready to go " + mAction);
            if (ACTION_READ.equals(mAction)) {
                if (mBound) handleActionReadHistory();
            } else if (ACTION_EXERCISE.equals(mAction)) {
                if (mBound) handleActionExercise();
            }

        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        Log.d("ReadHistoryIntentService", "onServiceDISConnected");
    }
}
