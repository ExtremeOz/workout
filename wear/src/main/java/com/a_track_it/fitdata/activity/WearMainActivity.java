package com.a_track_it.fitdata.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.SavedStateVMFactory;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.SwipeDismissFrameLayout;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.fragment.CustomListFragment;
import com.a_track_it.fitdata.fragment.CustomScoreDialogFragment;
import com.a_track_it.fitdata.fragment.EndOfSetFragment;
import com.a_track_it.fitdata.fragment.HomePageFragment;
import com.a_track_it.fitdata.fragment.LiveFragment;
import com.a_track_it.fitdata.fragment.SessionEntryFragment;
import com.a_track_it.fitdata.fragment.SessionReportFragment;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.model.SavedStateViewModel;
import com.a_track_it.fitdata.model.SessionViewModel;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.a_track_it.fitdata.service.FITAPIManager;

import java.util.Calendar;

import static com.a_track_it.fitdata.common.Constants.STATE_HOME;

public class WearMainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider,
        HomePageFragment.OnHomePageFragmentInteractionListener,
        LiveFragment.OnLiveFragmentInteractionListener,
        CustomListFragment.OnCustomListItemSelectedListener,
        CustomScoreDialogFragment.onCustomScoreSelected,
        SessionEntryFragment.OnSessionEntryFragmentListener,
        EndOfSetFragment.OnEndOfSetInteraction, SessionReportFragment.OnSessionReportInteraction,
        FITAPIManager.IFITAPIManager, CustomConfirmDialog.ICustomConfirmDialog {
    private static final int ACTION_INTIALISING = -1;
    private static final int ACTION_STARTING = 0;
    private static final int ACTION_STOPPING = 1;
    private static final int ACTION_RESUMING = 2;
    private static final int ACTION_CANCELLING = 3;
    private static final int ACTION_EXITING = 4;
    private static final int ACTION_QUICK_STOP = 5;
    private static final int ACTION_STOP_QUIT = 6;

    private int currentState = ACTION_INTIALISING;

    private static final String TAG = WearMainActivity.class.getSimpleName();
    private SwipeDismissFrameLayout mSwipeDismissFrameLayout;



    private class MySwipeDismissCallback extends SwipeDismissFrameLayout.Callback {
        @Override
        public void onSwipeStarted(SwipeDismissFrameLayout layout) {
            super.onSwipeStarted(layout);
            Log.i(TAG, "onSwipeStart " );
            if (!mNavigationController.popBackStack()){
                final androidx.constraintlayout.widget.ConstraintLayout frameLayout = findViewById(R.id.wear_constraintLayout);
                if (UserPreferences.getConfirmDismissSession(getApplicationContext())) {
                    CustomConfirmDialog customConfirmDialog = CustomConfirmDialog.newInstance(0, getString(R.string.confirm_dismiss_toggle_prompt), WearMainActivity.this);

                    customConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
                } else
                if (frameLayout != null)
                    showAlertDialogConfirm(frameLayout, ACTION_EXITING);
            }
        }

        @Override
        public void onDismissed(SwipeDismissFrameLayout layout) {
            Log.i(TAG, "onSwipeStart " );
            }
        }

    private long mPauseDuration = 0L;
    private long mCurrentRestStart = 0L;                 // set when active part of set completed.
    private long mExpectedRestDuration = 0L;              // the expected rest duration
    private int mCurrentSetIndex = 0;
    private int mStepTarget = 0;
    private int mDurationTarget = 0;
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
    private WorkManager mWorkManager;

    private boolean mNetworkConnected;
    private SessionViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private NavController mNavigationController;
    private FITAPIManager mService;
    private boolean mServiceBound = false;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FITAPIManager.LocalBinder binder = (FITAPIManager.LocalBinder) service;
            mService = binder.getService();
            mService.addListener(WearMainActivity.this);
            mServiceBound = true;
            Log.d(TAG, "Service Bound with Listener");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.removeListener(WearMainActivity.this);
            mService = null;
            mServiceBound = false;
            Log.d(TAG, "Service Disconnected");
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        //boolean SessionInProgress = false;
        String sMsg = "";
        if (!UserPreferences.getAppSetupCompleted(context)){
            Intent myInitialIntent = new Intent(context, InitialActivity.class);
            startActivity(myInitialIntent);
            finish();
            return;
        }
        setContentView(R.layout.activity_main_linearwear);
        mSwipeDismissFrameLayout = findViewById(R.id.wear_swipe_layout);
        mSwipeDismissFrameLayout.addCallback(new MySwipeDismissCallback());
        mNavigationController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavigationController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d(TAG, "onDestChanged " + destination.getLabel());
            }
        });


        long timeMs = System.currentTimeMillis();
        mMessagesViewModel = ViewModelProviders.of(WearMainActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.addMessage(getString(R.string.app_name));
        mMessagesViewModel.addOtherMessage(Utilities.getDateString(timeMs));
        mWorkManager = WorkManager.getInstance(this);
        mSessionViewModel = ViewModelProviders.of(WearMainActivity.this).get(SessionViewModel.class);

        // Obtain the ViewModel, passing in an optional
        // SavedStateVMFactory so that you can use SavedStateHandle
        mSavedStateViewModel = ViewModelProviders.of(this, new SavedStateVMFactory(this))
                .get(SavedStateViewModel.class);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart" );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop" );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy" );
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause" );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume" );
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {

        Log.i(TAG, "onRestoreInstanceState" );
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.i(TAG, "onSaveInstanceState" );
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {

    }

    @Override
    public void onCustomItemSelected(int type, long position, String title, int resid, String identifer) {

    }

    @Override
    public void onCustomScoreSelected(int type, long position, String title, int iSetIndex) {

    }

    @Override
    public void onEndOfSetRequest(int index, int setIndex, String defaultValue) {

    }

    @Override
    public void onEndOfSetMethod(int index, int setIndex) {

    }

    @Override
    public void onEndOfSetExit() {

    }

    @Override
    public void onHomePageFragmentInteraction(int src, int id, String text, int color) {

    }

    @Override
    public void onHomePageFragmentComplete(int id) {
        currentState = STATE_HOME;
    }

    @Override
    public void onHomePageFragmentLongClick(int id) {

    }

    @Override
    public void onLiveFragmentInteraction(int src, int id, String text, int color) {

    }

    @Override
    public void onLiveFragmentComplete(int id) {

    }

    @Override
    public void onSessionEntryRequest(int index, String defaultValue) {

    }

    @Override
    public void onSessionReportExit() {

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
    public void onDataComplete(Bundle resultData) {

    }


    private void showAlertDialogConfirm(final View parent_view, final int action){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppTheme_myAlertDialog);
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
                try {
                    // start
                    if (action == ACTION_STARTING) {
                        //startSession();
                        //startLiveFragment();
                    }
                    // end
                    if (action == ACTION_STOPPING) {
                        //Toast.makeText(getApplicationContext(), "Exit Session", Toast.LENGTH_SHORT).show();
                        // stopSession();
                        // startReportSessionFragment();
                    }
                    if (action == ACTION_STOP_QUIT) {
                        //stopSession();
                        quitApp();
                    }
                    if (action == ACTION_QUICK_STOP) {
                        //stopSession();
                    }
                    // resume
                    if (action == ACTION_RESUMING) {
                        //startSession();
                        //startLiveFragment();
                    }
                    if (action == ACTION_EXITING)
                        quitApp();
                }catch (Exception e){
                    Log.d(TAG, "alertdialog confirm error " + e.getLocalizedMessage());
                }
            });
            circularProgressLayout.setOnClickListener(view -> {
                circularProgressLayout.stopTimer();
                dialog.dismiss();
                if (action == ACTION_QUICK_STOP){
                    //startLiveFragment();
                }
            });
            circularProgressLayout.startTimer();
        }
        dialog.setCancelable(true);
        dialog.show();

    }
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
}
