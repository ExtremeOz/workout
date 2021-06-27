
package com.a_track_it.workout.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.AppBarStateChangeListener;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.ObjectAggregate;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutMeta;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.service.CacheResultReceiver;
import com.a_track_it.workout.common.user_model.MessagesViewModel;
import com.a_track_it.workout.common.service.CacheManager;
import com.a_track_it.workout.reports.SetsStackedBarChart;
import com.a_track_it.workout.reports.StrengthDoughnutChart;
import com.a_track_it.workout.reports.StrengthPieChart;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.workout.common.Constants.INTENT_HOME_REFRESH;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.workout.common.Constants.MAINTAIN_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.NOTIFICATION_MAINTAIN_ID;
import static com.a_track_it.workout.common.Constants.SUMMARY_CHANNEL_ID;

public class ReportDetailActivity extends BaseActivity implements CacheResultReceiver.Receiver{
    private static final String LOG_TAG = ReportDetailActivity.class.getSimpleName();
    public static final String EXTRA_NAME = "activity_name";
    public static final String EXTRA_USE_KG = "use_kg";
    public static final String ARG_WORKOUT_OBJECT = "ARG_WORKOUT_OBJECT";
    public static final String ARG_RETURN_RESULT = "ARG_RETURN_RESULT";
    private long mObjectID;
    private int mReportType;
    private String mUserID;
    private String mDeviceID;
    private String mTitle;
    private CacheResultReceiver mReceiver;
    protected Handler mHandler;
    protected Runnable mCardViewRunnable;
    protected Runnable mGraphicalViewRunnable;
    protected Runnable mResizeViewRunnable;
    private Workout mWorkout;
    private WorkoutMeta mMeta;
    private WorkoutSet mSet;
    private List<Workout> workoutList;
    private List<WorkoutSet> setList = new ArrayList<>();
    private List<ObjectAggregate> aggregateList = new ArrayList<>();
    ArrayList<String> setsList = new ArrayList<>();
    ArrayList<String> bodypartList = new ArrayList<>();
    ArrayList<String> exerciseList = new ArrayList<>();
    ArrayList<Float> setsWeightList = new ArrayList<>();
    ArrayList<Float> bodypartWeightList = new ArrayList<>();
    ArrayList<Float> exerciseWeightList = new ArrayList<>();
    ArrayList<Integer> bodypartSetsList = new ArrayList<>();
    ArrayList<Integer> exerciseSetsList = new ArrayList<>();
    ArrayList<Integer> setsRepsList = new ArrayList<>();
    ArrayList<Integer> bodypartRepsList = new ArrayList<>();
    ArrayList<Integer> exerciseRepsList = new ArrayList<>();
    ArrayList<Long> setsDurationList = new ArrayList<>();
    ArrayList<Long> bodypartDurationList = new ArrayList<>();
    ArrayList<Long> exerciseDurationList = new ArrayList<>();
    private MessagesViewModel mMessagesViewModel;
    float totalWatts = 0F;
    float totalWeight = 0F;
    float tempFloat = 0F;
    long totalElapsed = 0L;
    long totalCall = 0L;
    long totalRest = 0L;
    long totalPause = 0L;
    int totalSets = 0;
    int totalScore = 0;
    String totalScoreCard = Constants.ATRACKIT_EMPTY;
    String totalXYCard = Constants.ATRACKIT_EMPTY;
    int totalSteps = 0;
    int totalReps = 0;
    int lastReps = 0;
    private Integer returnResultFlag;
    private String sSessionTitle;
    private String sSessionBody;
    private String sSetsTitle;
    private String sSetsText;
    private String sBodyPartTitle;
    private String sBodyPartText;
    private String sExerciseTitle;
    private String sExerciseText;
    private ReferencesTools mReferenceTools;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout collapsingToolbar;
    private CoordinatorLayout mainCoordinator;
    private FrameLayout frameDrop;
    private FrameLayout frameSets;
    private FrameLayout frameBodyparts;
    private FrameLayout frameExercises;
    private Spinner spinnerMetrics;
    private MaterialButton toggleButtonExpand;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabTemplate;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabStart;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabDevice;
    private ImageView imageView;
    private TextView session_title;
    private TextView session_text;    
    private TextView sets_title;
    private TextView sets_text;
    private TextView bodypart_title;
    private TextView bodypart_text;
    private TextView exercise_title;
    private TextView exercise_text;
    private int densityDpi;
    private int shortAnimationDuration;
    private boolean bUseKG;
    private long mLastClickTime;
    private long backPressedTime;


    final private View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
         //   mListener.OnFragmentInteraction(v.getId(), 0, null);
        }
    };
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_report_detail;
    }

    @Override
    public void onBackPressed() {
        long t = SystemClock.elapsedRealtime();
        Context context = getApplicationContext();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0){
            fragmentManager.popBackStack();
            return;
        }
//        if (t - backPressedTime > 2000) {    // 2 secs
//            backPressedTime = t;
//            Toast.makeText(context, getString(R.string.app_check_exit), Toast.LENGTH_SHORT).show();
//        } else {
//            // clean up
//            if ((sUserId.length() > 0) && userPrefs.getConfirmExitApp()) {
//
//            }else {
                if (returnResultFlag > 0)
                    setResult(RESULT_CANCELED, getResultIntent(ATRACKIT_EMPTY));
                super.onBackPressed();
 //           }
 //       }
    }
    private Intent getResultIntent(String intentAction){
        Intent resultIntent  = new Intent(intentAction);
        resultIntent.putExtra(KEY_FIT_WORKOUTID, mWorkout._id);
        resultIntent.putExtra(KEY_FIT_TYPE, 0);
        resultIntent.putExtra(Workout.class.getSimpleName(), mWorkout);
        if (mSet != null) resultIntent.putExtra(WorkoutSet.class.getSimpleName(), mSet);
        resultIntent.putExtra(KEY_FIT_USER, mUserID);
        resultIntent.putExtra(KEY_FIT_DEVICE_ID, mDeviceID);
        return resultIntent;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mReportType = intent.getIntExtra(Constants.MAP_DATA_TYPE, 10);
            mUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
            mDeviceID = intent.getStringExtra(Constants.KEY_FIT_DEVICE_ID);
            mObjectID = intent.getLongExtra(Constants.KEY_FIT_WORKOUTID, 0);
            mTitle = intent.getStringExtra(ReportDetailActivity.EXTRA_NAME);
            bUseKG = intent.getBooleanExtra(ReportDetailActivity.EXTRA_USE_KG, (Locale.getDefault() != Locale.US));
            // return a result flat
            returnResultFlag = (intent.hasExtra(ARG_RETURN_RESULT) ? intent.getIntExtra(ARG_RETURN_RESULT, 0) : 0);
        }else
            if (savedInstanceState !=null){
                mReportType = savedInstanceState.getInt(Constants.MAP_DATA_TYPE);
                mUserID = savedInstanceState.getString(KEY_FIT_USER);
                mDeviceID = savedInstanceState.getString(KEY_FIT_DEVICE_ID);
                mObjectID = savedInstanceState.getLong(KEY_FIT_WORKOUTID);
                mTitle = savedInstanceState.getString(ReportDetailActivity.EXTRA_NAME);
                bUseKG = savedInstanceState.getBoolean(ReportDetailActivity.EXTRA_USE_KG);
                returnResultFlag = 0;
            }
        mMessagesViewModel = new ViewModelProvider(ReportDetailActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.getWorkInProgress().observe(ReportDetailActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ReportDetailActivity.this);
                int notificationID = NOTIFICATION_MAINTAIN_ID;
                String sTitle = getString(R.string.action_db_running);
                String sContent = getString(R.string.app_name);
                String sChannelID = MAINTAIN_CHANNEL_ID;

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
                    notificationID = Constants.NOTIFICATION_SUMMARY_ID;
                    sTitle = getString(R.string.action_db_running);
                    sContent = getString(R.string.app_name);
                    sChannelID = SUMMARY_CHANNEL_ID;
                    PendingIntent pendingViewIntent = PendingIntent.getActivity(context, notificationID, viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Action actionView5 =
                            new NotificationCompat.Action.Builder(R.drawable.ic_a_outlined,
                                    getString(R.string.action_open), pendingViewIntent)
                                    .build();
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
                            .setContentInfo(getString(R.string.action_db_running))
                            .setContentIntent(pendingViewIntent5)
                            .setAutoCancel(false)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
                    try {
                        if (notificationManager != null)
                            notificationManager.notify(notificationID, notifyBuilder5.build());
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
                else{
                    notificationManager.cancel(notificationID);
                }
            }
        });
        mMessagesViewModel.getLiveIntent().observe(this, (Observer<Intent>) intent2 -> {
            if (intent == null) return;
            final String intentAction = intent2.getAction();
            final Context ctx = getApplicationContext();
            Log.e(LOG_TAG, "liveIntent " + intentAction);
            if (intentAction.equals(INTENT_MESSAGE_TOAST)){
                if (intent2.hasExtra(INTENT_EXTRA_MSG)) {
                    final int iType = intent2.getIntExtra(KEY_FIT_TYPE, 0);
                    final int length = intent2.getIntExtra(KEY_FIT_VALUE, Toast.LENGTH_SHORT);
                    String sMessage = intent2.getStringExtra(INTENT_EXTRA_MSG);
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
            }
        });
        mReceiver = new CacheResultReceiver(new Handler());
        mReferenceTools = ReferencesTools.setInstance(getApplicationContext());
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        bindActivity();

        Drawable drawableUnChecked = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(getApplicationContext(), R.color.secondaryTextColor));
        toolbar.setNavigationIcon(drawableUnChecked);

        fabStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = getResultIntent(Constants.INTENT_WORKOUT_EDIT);
                if (returnResultFlag > 0)
                    setResult(RESULT_OK, intent);
                finishAfterTransition();
            }
        });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        switch (state){
                            case COLLAPSED:
                                fabStart.hide();
                                fabTemplate.hide();
                                fabDevice.hide();
                                toggleButtonExpand.setVisibility(View.GONE);
                                break;
                            case EXPANDED:
                            case IDLE:
                                fabStart.show();
                                if ((mWorkout != null) && (Utilities.isShooting(mWorkout.activityID) || Utilities.isGymWorkout(mWorkout.activityID)))
                                    fabTemplate.show();
                                else
                                    fabTemplate.hide();
                                if (mMessagesViewModel.hasPhone())
                                    fabDevice.show();
                                else
                                    fabDevice.hide();
                                toggleButtonExpand.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
            }
        });
        spinnerMetrics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setStrengthGraphicalView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toggleButtonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGraphViewLayout();
                setStrengthGraphicalView();
            }
        });
        loadBackdrop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(KEY_FIT_WORKOUTID, mObjectID);
        outState.putInt(Constants.MAP_DATA_TYPE, mReportType);
        outState.putString(KEY_FIT_USER,mUserID);
        outState.putString(KEY_FIT_DEVICE_ID,mDeviceID);
        outState.putString(ReportDetailActivity.EXTRA_NAME, mTitle);
        outState.putBoolean(ReportDetailActivity.EXTRA_USE_KG, bUseKG);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
        if (mHandler != null) {
            if (mCardViewRunnable != null) mHandler.removeCallbacks(mCardViewRunnable);
            if (mGraphicalViewRunnable != null) mHandler.removeCallbacks(mGraphicalViewRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver.setReceiver(this);
        refreshData();
    }
    private void bindActivity() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        mainCoordinator = findViewById(R.id.main_content);
        mAppBarLayout = findViewById(R.id.appbar);
        toggleButtonExpand = findViewById(R.id.toggle_expand_collapsible);
        frameDrop = findViewById(R.id.frame_drop);
        frameSets = findViewById(R.id.sets_frame_layout);
        frameBodyparts = findViewById(R.id.bodypart_frame_layout);
        frameExercises = findViewById(R.id.exercise_frame_layout);
        spinnerMetrics = findViewById(R.id.spinnerMetrics);
        fabTemplate = (FloatingActionButton)findViewById(R.id.fab_template);
        fabStart = (FloatingActionButton)findViewById(R.id.fab_start);
        fabDevice = (FloatingActionButton)findViewById(R.id.fab_device);
        session_title = findViewById(R.id.session_title);
        session_text = findViewById(R.id.session_text);
        sets_title = findViewById(R.id.sets_title);
        sets_text = findViewById(R.id.sets_text);
        bodypart_title = findViewById(R.id.bodypart_title);
        bodypart_text = findViewById(R.id.bodypart_text);
        exercise_title = findViewById(R.id.exercise_title);
        exercise_text = findViewById(R.id.exercise_text);
        session_title.setOnClickListener(myClicker);
        session_text.setOnClickListener(myClicker);
        sets_title.setOnClickListener(myClicker);
        sets_text.setOnClickListener(myClicker);
        bodypart_title.setOnClickListener(myClicker);
        bodypart_text.setOnClickListener(myClicker);
        exercise_title.setOnClickListener(myClicker);
        exercise_text.setOnClickListener(myClicker);
        imageView = findViewById(R.id.image_drop);
    }

    private void loadBackdrop() {
        Glide.with(this).load(R.drawable.man_doing_cable_fly_in_gym).apply(RequestOptions.centerCropTransform()).into(imageView);
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

    public void refreshData() {
        mMessagesViewModel.setWorkType(Constants.TASK_ACTION_READ_LOCAL);
        mMessagesViewModel.setWorkInProgress(true);
        CacheManager.getDetails(mReportType,mObjectID, mReceiver, ReportDetailActivity.this,mUserID,mDeviceID);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.routine, menu);
        return true;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        mMessagesViewModel.setWorkInProgress(false);
        mMessagesViewModel.setWorkType(0);
        if (mHandler != null && mCardViewRunnable != null) {
            mHandler.removeCallbacks(mCardViewRunnable);
        }
        workoutList = resultData.getParcelableArrayList(Constants.KEY_LIST_WORKOUTS);
        if (workoutList == null) return;
        if (workoutList.size() > 0) mWorkout = workoutList.get(0);

        setList = resultData.getParcelableArrayList(Constants.KEY_LIST_SETS);
        if (setList == null){
            Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
            msgIntent.putExtra(INTENT_EXTRA_MSG, "Not data found");
            msgIntent.putExtra(KEY_FIT_TYPE, 2); // wear os flag
            mMessagesViewModel.addLiveIntent(msgIntent);
            return;
        }
        List<WorkoutMeta> metaList = resultData.getParcelableArrayList(Constants.KEY_LIST_META);
        if ((metaList != null) && (metaList.size() > 0)) mMeta = metaList.get(0);
        mSet = ((setList!=null) && (setList.size() > 0)) ? setList.get(0) : new WorkoutSet();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        densityDpi = metrics.densityDpi;
        if (mHandler == null) mHandler = new Handler();
        if (Utilities.isGymWorkout(mWorkout.activityID)||Utilities.isShooting(mWorkout.activityID))
            mHandler.post(() -> {
                sSetsText = Constants.ATRACKIT_EMPTY;
                for (WorkoutSet s : setList) {
                    totalSets += 1;
                    totalReps += ((s.repCount == null) ? 0 :s.repCount);
                    totalSteps += ((s.stepCount == null) ? 0 :s.stepCount);
                    totalWeight += ((s.weightTotal == null) ? 0 :s.weightTotal);
                    totalCall += ((s.call_duration == null) ? 0 :s.call_duration);
                    totalRest += ((s.rest_duration == null) ? 0 :s.rest_duration);
                    totalPause += s.pause_duration;
                    totalElapsed += s.duration;
                    if (sSetsText.length() > 0)
                        sSetsText += Constants.LINE_DELIMITER + mReferenceTools.workoutSetTinyText(s);
                    else
                        sSetsText += mReferenceTools.workoutSetTinyText(s);

                    if (Utilities.isGymWorkout(s.activityID)) {
                        setsList.add(Integer.toString(s.setCount));
                        setsRepsList.add(s.repCount);
                        setsWeightList.add(s.weightTotal);
                        setsDurationList.add(s.duration);
                        if ((s.bodypartName != null) && (s.bodypartName.length() > 0)) {
                            if (bodypartList.size() == 0) {
                                bodypartList.add(s.bodypartName);
                                bodypartSetsList.add(1);
                                bodypartRepsList.add(s.repCount);
                                bodypartWeightList.add(s.weightTotal);
                                bodypartDurationList.add(s.duration);
                            } else if (!bodypartList.contains(s.bodypartName)) {
                                bodypartList.add(s.bodypartName);
                                bodypartSetsList.add(1);
                                bodypartRepsList.add(s.repCount);
                                bodypartWeightList.add(s.weightTotal);
                                bodypartDurationList.add(s.duration);
                            } else {
                                int iIndex = bodypartList.indexOf(s.bodypartName);
                                int iTemp = bodypartRepsList.get(iIndex) + s.repCount;
                                bodypartRepsList.set(iIndex, iTemp);
                                iTemp = bodypartSetsList.get(iIndex) + 1;
                                bodypartSetsList.set(iIndex, iTemp);
                                float fTemp = bodypartWeightList.get(iIndex) + s.weightTotal;
                                bodypartWeightList.set(iIndex, fTemp);
                                long lTemp = bodypartDurationList.get(iIndex);
                                lTemp += s.duration;
                                bodypartDurationList.set(iIndex, lTemp);
                            }
                        }
                        if ((s.exerciseName != null) && (s.exerciseName.length() > 0)) {
                            if (exerciseList.size() == 0) {
                                exerciseList.add(s.exerciseName);
                                exerciseSetsList.add(1);
                                exerciseRepsList.add(s.repCount);
                                exerciseWeightList.add(s.weightTotal);
                                exerciseDurationList.add(s.duration);
                            } else if (!exerciseList.contains(s.exerciseName)) {
                                exerciseList.add(s.exerciseName);
                                exerciseSetsList.add(1);
                                exerciseRepsList.add(s.repCount);
                                exerciseWeightList.add(s.weightTotal);
                                exerciseDurationList.add(s.duration);
                            } else {
                                int iIndex = exerciseList.indexOf(s.exerciseName);
                                int iTemp = exerciseRepsList.get(iIndex) + s.repCount;
                                exerciseRepsList.set(iIndex, iTemp);
                                iTemp = exerciseSetsList.get(iIndex) + 1;
                                exerciseSetsList.set(iIndex, iTemp);
                                float fTemp = exerciseWeightList.get(iIndex) + s.weightTotal;
                                exerciseWeightList.set(iIndex, fTemp);
                                long lTemp = exerciseDurationList.get(iIndex);
                                lTemp += s.duration;
                                exerciseDurationList.set(iIndex, lTemp);
                            }
                        }
                        if (s.weightTotal > 0F) {
                            tempFloat = s.repCount * s.weightTotal;
                            totalWatts += tempFloat;
                        }
                    }
                    if (Utilities.isShooting(s.activityID)) {
                        String items[] = s.score_card.split(Constants.SHOT_DELIM);
                        List<String> mScoreCard = new ArrayList<>(Arrays.asList(items));
                        int scoreVal = 0;
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
                        totalScore += scoreVal;
                        if (totalScoreCard.length() > 0)
                            totalScoreCard += Constants.SHOT_DELIM + s.score_card;
                        else
                            totalScoreCard = s.score_card;
                        if (totalXYCard.length() > 0)
                            totalXYCard += Constants.SHOT_DELIM + s.per_end_xy;
                        else
                            totalXYCard = s.per_end_xy;
                    }
                }  // each set
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    sBodyPartTitle = bodypartList.size() + ATRACKIT_SPACE + getString(R.string.label_bodypart) + ((bodypartList.size() > 1) ? "s" : Constants.ATRACKIT_EMPTY);
                    sExerciseTitle = exerciseList.size() + ATRACKIT_SPACE + getString(R.string.label_exercise) + ((exerciseList.size() > 1) ? "s" : Constants.ATRACKIT_EMPTY);
                    for (int i = 0; i < bodypartList.size(); i++) {
                        String sTemp = bodypartList.get(i) + ATRACKIT_SPACE + bodypartSetsList.get(i) + " sets " + bodypartRepsList.get(i) + " reps ";
                        if (bUseKG) {
                            double intWeight = Math.floor(bodypartWeightList.get(i));
                            if (bodypartWeightList.get(i) % intWeight != 0)
                                sTemp += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, bodypartWeightList.get(i)) + Constants.KG_TAIL;
                            else
                                sTemp += new DecimalFormat("#").format(intWeight) + Constants.KG_TAIL;
                        } else
                            sTemp += Utilities.KgToPoundsDisplay(bodypartWeightList.get(i)) + Constants.LBS_TAIL;
                        // sTemp += ATRACKIT_SPACE + Utilities.getDurationBreakdown(bodypartDurationList.get(i));
                        if ((sBodyPartText != null) && (sBodyPartText.length() > 0))
                            sBodyPartText += Constants.LINE_DELIMITER + sTemp;
                        else
                            sBodyPartText = sTemp;
                    }
                    for (int i = 0; i < exerciseList.size(); i++) {
                        String sTemp = exerciseList.get(i) + ATRACKIT_SPACE + exerciseSetsList.get(i) + " sets " + exerciseRepsList.get(i) + " reps ";
                        if (bUseKG) {
                            double intWeight = Math.floor(exerciseWeightList.get(i));
                            if (exerciseWeightList.get(i) % intWeight != 0)
                                sTemp += String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, exerciseWeightList.get(i)) + Constants.KG_TAIL;
                            else
                                sTemp += new DecimalFormat("#").format(intWeight) + Constants.KG_TAIL;
                        } else
                            sTemp += Utilities.KgToPoundsDisplay(exerciseWeightList.get(i)) + Constants.LBS_TAIL;
                        // sTemp += ATRACKIT_SPACE + Utilities.getDurationBreakdown(exerciseDurationList.get(i));

                        if ((sExerciseText != null) && (sExerciseText.length() > 0))
                            sExerciseText += Constants.LINE_DELIMITER + sTemp;
                        else
                            sExerciseText = sTemp;
                    }
                }
            });
        else
            runOnUiThread(() -> spinnerMetrics.setVisibility(View.GONE));

        setCardViews();
        setStrengthGraphicalView();
    }

    private void setStrengthGraphicalView(){
        if ((mWorkout == null) ||  !Utilities.isGymWorkout(mWorkout.activityID)) return;
        if (mHandler == null) mHandler = new Handler();
        mGraphicalViewRunnable = () -> {
            StrengthDoughnutChart doughnutChart = new StrengthDoughnutChart();
            SetsStackedBarChart barChartSets = new SetsStackedBarChart();
            StrengthPieChart pieChartEx = new StrengthPieChart();
            StrengthPieChart pieChartBp = new StrengthPieChart();
            // load the relevant metric
            String[] metricsArray = getResources().getStringArray(R.array.graph_gym_metrics);
            int iMetric = spinnerMetrics.getSelectedItemPosition();
            if (iMetric == AdapterView.INVALID_POSITION) iMetric = 0;

            String reportTitle = metricsArray[iMetric];
            doughnutChart.addReportTitle(reportTitle);
            barChartSets.addReportTitle(reportTitle);
            pieChartEx.addReportTitle(reportTitle);
            pieChartBp.addReportTitle(reportTitle);
            int setCount = setsList.size();
            int exCount = exerciseList.size();
            int[] rainbow = getResources().getIntArray(R.array.rainbow);
            List<double[]> values = new ArrayList<double[]>();
            List<String[]> titles = new ArrayList<String[]>();

            String[] exTitles = new String[exCount];
            exTitles = exerciseList.toArray(exTitles);
            double[] exValues = new double[exCount];
            int i=0;
            switch (iMetric){
                case 0:             // reps
                    for (int repVal : exerciseRepsList){
                        exValues[i++] = (double) repVal;
                    }
                    break;
                case 1:             // sets
                    for (int setVal : exerciseSetsList){
                        exValues[i++] = (double) setVal;
                    }
                    break;
                case 2:             // weight
                    for (float setVal : exerciseWeightList){
                        exValues[i++] = (double) setVal;
                    }
                    break;
                case 3:             // watts
                    for (float setVal : exerciseWeightList){
                        int reps = exerciseRepsList.get(i);
                        exValues[i++] = (reps > 0)? (reps * (double) setVal) :(double) setVal;
                    }
                    break;
                case 4:             // duration
                    for (long setVal : exerciseDurationList){
                        exValues[i++] = (double) (setVal/1000.0);
                    }
                    break;
            }
            titles.add(exTitles);
            values.add(exValues);
            int bpCount = bodypartList.size();
            String[] bpTitles = new String[bpCount];
            bpTitles = bodypartList.toArray(bpTitles);
            double[] bpValues = new double[bpCount];
            i=0;  // reset back
            switch (iMetric){
                case 0:             // reps
                    for (int repVal : bodypartRepsList){
                        bpValues[i++] = (double) repVal;
                    }
                    break;
                case 1:             // sets
                    for (int setVal : bodypartSetsList){
                        bpValues[i++] = (double) setVal;
                    }
                    break;
                case 2:             // weight
                    for (float setVal : bodypartWeightList){
                        bpValues[i++] = (double) setVal;
                    }
                    break;
                case 3:             // watts
                    for (float setVal : bodypartWeightList){
                        int reps = bodypartRepsList.get(i);
                        bpValues[i++] = (reps > 0)? (reps * (double) setVal) :(double) setVal;
                    }
                    break;
                case 4:             // duration
                    for (long setVal : bodypartDurationList){
                        bpValues[i++] = (double) (setVal/1000.0);
                    }

                    break;
            }
            titles.add(bpTitles);
            values.add(bpValues);
            pieChartBp.addTitles(bpTitles);
            pieChartBp.addValues(bpValues);
            int backgroundColor = getColor(R.color.primaryDarkColor);
            pieChartBp.setBackgroundColor(backgroundColor);
            doughnutChart.setBackgroundColor(backgroundColor);
            barChartSets.setBackgroundColor(backgroundColor);
            int[] colors = Arrays.copyOfRange(rainbow,0,exCount+bpCount);
            int[] setColors = Arrays.copyOfRange(rainbow,0,setCount);
            doughnutChart.addColors(colors);
            doughnutChart.setTitlesList(titles);
            doughnutChart.setValuesList(values);
            pieChartEx.addTitles(exTitles);
            pieChartEx.addValues(exValues);
            pieChartEx.setBackgroundColor(backgroundColor);
            pieChartBp.addColors(Arrays.copyOfRange(rainbow,0,bpCount));
            pieChartEx.addColors(Arrays.copyOfRange(rainbow,0,exCount));
            // NOW FOR SETS
           // String[] setTitles = metricsArray;
            barChartSets.addTitles(metricsArray);
            if (mWorkout != null) barChartSets.setWorkoutID(mWorkout._id);
            List<double[]> setValuesList = new ArrayList<double[]>();
            if (setCount > 0)
            for (int aMetric=0; aMetric < 5; aMetric++){
                double[] setValues = new double[setCount];
                int y = 0;
                switch (aMetric){
                    case 0:             // reps
                        for (int setVal : setsRepsList){
                            setValues[y] = (double) setVal;
                            y++;
                        }
                        break;
                    case 1:             // sets
                        setValues[y] = (double)y+1;
                        y++;
                        break;
                    case 2:             // weight
                        for (float setVal : setsWeightList){
                            setValues[y] = (double) setVal;
                            y++;
                        }
                        break;
                    case 3:             // watts
                        for (float setVal : setsWeightList){
                            setValues[y] = (setVal > 0) ? (double) (setVal * setsRepsList.get(y)) : 0;
                            y++;
                        }
                        break;
                    case 4:             // duration
                        for (long setVal : setsDurationList){
                            setValues[y] = (double) (setVal/1000);
                            y++;
                        }
                        break;
                }
                setValuesList.add(setValues); // add each metric
            }
            barChartSets.setValuesList(setValuesList);
            barChartSets.addColors(setColors);
            barChartSets.setMetric(iMetric);
            barChartSets.setDisplayMetrics(densityDpi);
            final GraphicalView graphicalViewDoughnut = doughnutChart.getView(getApplicationContext(), densityDpi);
            final GraphicalView graphicalViewSetsBar = (iMetric != 1) ?  barChartSets.getView(getApplicationContext(),densityDpi) : null;
            final GraphicalView graphicalViewPieBp = pieChartBp.getView(getApplicationContext(), densityDpi);
            final GraphicalView graphicalViewPieEx = pieChartEx.getView(getApplicationContext(), densityDpi);
            // Update the UI
            runOnUiThread(() -> {
                if (frameDrop.getChildCount() > 0) frameDrop.removeAllViews();
                if (frameSets.getChildCount() > 0) frameSets.removeAllViews();
                if (frameBodyparts.getChildCount() > 0) frameBodyparts.removeAllViews();
                if (frameExercises.getChildCount() > 0) frameExercises.removeAllViews();
                if (graphicalViewDoughnut != null) frameDrop.addView(graphicalViewDoughnut,new ViewGroup.LayoutParams (
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (graphicalViewSetsBar != null) frameSets.addView(graphicalViewSetsBar,new ViewGroup.LayoutParams (
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (graphicalViewPieBp != null) frameBodyparts.addView(graphicalViewPieBp,new ViewGroup.LayoutParams (
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (graphicalViewPieEx != null) frameExercises.addView(graphicalViewPieEx,new ViewGroup.LayoutParams (
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (imageView.getVisibility() == View.VISIBLE) crossFadeOut(imageView);
                if (graphicalViewDoughnut != null) crossFadeIn(frameDrop);
                if (graphicalViewSetsBar != null) crossFadeIn(frameSets); else crossFadeOut(frameSets);
                if (graphicalViewPieBp != null) crossFadeIn(frameBodyparts);
                if (graphicalViewPieEx != null)crossFadeIn(frameExercises);
                mAppBarLayout.setMinimumHeight(500);
            });
        };
        mHandler.postDelayed(mGraphicalViewRunnable, 500);
    }
    private void setGraphViewLayout(){
        if (mHandler == null) mHandler = new Handler();

        mResizeViewRunnable = () -> {
            final int originalDimension = Math.round(getResources().getDimension(R.dimen.detail_backdrop_height));
            final int newDimension = Math.round(getResources().getDimension(R.dimen.detail_backdrop_height2));
            final Drawable downIcon = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_down_arrow);
            final Drawable upIcon = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_up_arrow);
            ViewGroup.LayoutParams params = collapsingToolbar.getLayoutParams();
            final boolean bOriginalSize = (params.height == originalDimension);
            TransitionManager.beginDelayedTransition(collapsingToolbar, new TransitionSet()
                    .addTransition(new ChangeBounds()).setDuration(2000));
            if (bOriginalSize)
                params.height = newDimension;
            else
                params.height = originalDimension;
            final ViewGroup.LayoutParams finalParams = params;
            // Update the UI
            runOnUiThread(() -> {
                collapsingToolbar.setLayoutParams(finalParams);
                if (bOriginalSize) {
                    toggleButtonExpand.setIcon(upIcon);
                    collapsingToolbar.setTitleEnabled(true);
                }else {
                    toggleButtonExpand.setIcon(downIcon);
                    collapsingToolbar.setTitleEnabled(false);
                }
            });
        };

        mHandler.postDelayed(mResizeViewRunnable, 500);
    }
    private void setCardViews(){
        if (mWorkout == null) return;
        mCardViewRunnable = () -> {
            String sTemp = ((mWorkout.name != null) && (mWorkout.name.length() > 0)) ? mWorkout.name : mWorkout.activityName;

            sSessionBody = mReferenceTools.workoutShortText(mWorkout);
            if ((mWorkout.parentID != null) && (mWorkout.parentID < 0))
                sTemp = getString(R.string.label_template);

                if (mMeta != null){
                    if ((mWorkout.parentID != null) && (mWorkout.parentID < 0))  // template
                        sTemp += Constants.ATRACKIT_SPACE + mMeta.description;
                    else
                        sTemp = mMeta.description;
                }else
                    sTemp = mWorkout.activityName;
            sSessionTitle = sTemp;
            if (Utilities.isGymWorkout(mWorkout.activityID)){
                sSetsTitle = totalSets + " sets " + exerciseList.size() + " exercises " + bodypartList.size() + " bodyparts";
            }

            // Update the UI
            runOnUiThread(() -> {
                session_title.setText(sSessionTitle);
                session_text.setText(sSessionBody);
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    sets_title.setText(sSetsTitle);
                    sets_text.setMinLines(setList.size());
                    sets_text.setMaxLines(setList.size()+8);
                    sets_text.setText(sSetsText);
                    bodypart_title.setText(sBodyPartTitle);
                    bodypart_text.setMinLines(bodypartList.size());
                    bodypart_text.setText(sBodyPartText);
                    exercise_title.setText(sExerciseTitle);
                    exercise_text.setMinLines(exerciseList.size());
                    exercise_text.setMaxLines(exerciseList.size() + 5);
                    exercise_text.setText(sExerciseText);
                    mainCoordinator.findViewById(R.id.card_bodypart).setVisibility(View.VISIBLE);
                    mainCoordinator.findViewById(R.id.card_exercise).setVisibility(View.VISIBLE);
                    mainCoordinator.findViewById(R.id.card_sets).setVisibility(View.VISIBLE);
                }else{
                    mainCoordinator.findViewById(R.id.card_bodypart).setVisibility(View.GONE);
                    mainCoordinator.findViewById(R.id.card_exercise).setVisibility(View.GONE);
                    mainCoordinator.findViewById(R.id.card_sets).setVisibility(View.GONE);
                }
            });
        };
        mHandler.postDelayed(mCardViewRunnable, 500);
    }

}
