package com.a_track_it.fitdata.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.ExpandableListAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.MetaAggTuple;
import com.a_track_it.fitdata.common.data_model.SetAggTuple;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutAggTuple;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.service.CacheResultReceiver;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.database.CacheManager;
import com.a_track_it.fitdata.reports.BarTimeChart;
import com.a_track_it.fitdata.reports.FitnessTimeChart;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.util.MathHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_HOME_REFRESH;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.MAINTAIN_CHANNEL_ID;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_MAINTAIN_ID;
import static com.a_track_it.fitdata.common.Constants.SELECTION_ACTIVITY_MISC;
import static com.a_track_it.fitdata.common.Constants.SUMMARY_CHANNEL_ID;


/**
 * Created by Chris Black - updated Daniel Haywood
 *
 * Displays a_track_it.com detail page for the selected fitdata activityID
 */
public class DetailActivity extends BaseActivity implements CacheResultReceiver.Receiver {
    private final static String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String ARG_CHART = "DetailActivity:chart";
    public static final String ARG_PAGE = "DetailActivity:page";
    public static final String EXTRA_TYPE = "DetailActivity:activityID";
    public static final String EXTRA_TITLE = "DetailActivity:title";
    public static final String EXTRA_IMAGE = "DetailActivity:image";

    private MessagesViewModel mMessagesViewModel;
    private AppBarLayout mAppBarLayout;
    private ImageButton btnFilters;
    private Spinner spinnerUOY;
    private TextView sessionText;
    private TextView labelSetFilters;
    private TextView textViewTimeFrame;
    private FrameLayout reportFrame;
    //private FrameLayout reportFrame2;
    //private FrameLayout reportFrame3;
    private ImageView imageView;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<String>> listDataChild = new HashMap<>();
    private int indexTimeFrame;
    private int reportType;
    private long workoutType;
    private boolean bLoading;
    private int indexMetric;
    private int indexUOY;
    private int indexGroup;
    private int indexChart;
    private int indexFilter;
    private List<Long> params = new ArrayList<>();
    private String workoutTitle;
    private String sUserID;
    private String sDeviceID;
    private Workout mWorkout;
    private CacheResultReceiver mReceiver;
    protected Handler mHandler;
    protected Runnable mCardViewRunnable;
    protected Runnable mGraphicalViewRunnable;

    private ReferencesTools mRefTools;
    private List<Workout> workoutList = new ArrayList<>();
    private List<WorkoutSet> setList = new ArrayList<>();
    private List<WorkoutAggTuple> workoutAggList = new ArrayList<>();
    private List<MetaAggTuple> metaAggList = new ArrayList<>();
    private List<SetAggTuple> setAggList = new ArrayList<>();
    private String[] mRegionList = new String[6];
    private int shortAnimationDuration;
    private int densityDpi;
    private boolean bUseKG;


    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null){
            indexTimeFrame = getIntent().getIntExtra(ARG_PAGE,1);
            indexChart = getIntent().getIntExtra(ARG_CHART,0);
            workoutType = getIntent().getLongExtra(EXTRA_TYPE, 0);
            workoutTitle = getIntent().getStringExtra(EXTRA_TITLE);
            sUserID = getIntent().getStringExtra(Constants.KEY_FIT_USER);
            sDeviceID = getIntent().getStringExtra(Constants.KEY_FIT_DEVICE_ID);
            reportType = getIntent().getIntExtra(Constants.KEY_FIT_TYPE,1);
            indexGroup = getIntent().getIntExtra(Constants.KEY_INDEX_GROUP,0);
            indexMetric = getIntent().getIntExtra(Constants.KEY_INDEX_METRIC,0);
            indexUOY = getIntent().getIntExtra(Constants.KEY_INDEX_UOY,0);
            indexFilter = getIntent().getIntExtra(Constants.KEY_INDEX_FILTER,0);
        }else
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(EXTRA_TYPE))) {
            workoutType = savedInstanceState.getLong(EXTRA_TYPE);
            workoutTitle = savedInstanceState.getString(EXTRA_TITLE);
            indexTimeFrame = savedInstanceState.getInt(ARG_PAGE,1);
            indexChart = savedInstanceState.getInt(ARG_CHART,0);
            reportType = savedInstanceState.getInt(Constants.KEY_FIT_TYPE,1);
            sUserID = savedInstanceState.getString(Constants.KEY_FIT_USER);
            sDeviceID = savedInstanceState.getString(Constants.KEY_FIT_DEVICE_ID);
            indexGroup = savedInstanceState.getInt(Constants.KEY_INDEX_GROUP);
            indexMetric = savedInstanceState.getInt(Constants.KEY_INDEX_METRIC);
            indexUOY = savedInstanceState.getInt(Constants.KEY_INDEX_UOY);
            indexFilter = savedInstanceState.getInt(Constants.KEY_INDEX_FILTER);
        }
        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
        bLoading = true;

        if (getIntent().hasExtra(Workout.class.getSimpleName())){
            mWorkout = getIntent().getParcelableExtra(Workout.class.getSimpleName());
        } else{
            mWorkout = new Workout();
        }
        Utilities.TimeFrame tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
        if (indexTimeFrame == 1) tf = Utilities.TimeFrame.BEGINNING_OF_WEEK;
        if (indexTimeFrame == 2) tf = Utilities.TimeFrame.LAST_WEEK;
        if (indexTimeFrame == 3) tf = Utilities.TimeFrame.BEGINNING_OF_MONTH;
        if (indexTimeFrame == 4) tf = Utilities.TimeFrame.LAST_MONTH;
        if (indexTimeFrame == 5) tf = Utilities.TimeFrame.NINETY_DAYS;
        if (indexTimeFrame == 6) tf = Utilities.TimeFrame.BEGINNING_OF_YEAR;
        UserPreferences userPrefs = UserPreferences.getPreferences(getApplicationContext(),sUserID);
        bUseKG = userPrefs.getUseKG();
        mWorkout.start = Utilities.getTimeFrameStart(tf);
        mWorkout.end = Utilities.getTimeFrameEnd(tf);
        if (workoutType != Constants.WORKOUT_TYPE_TIME){
            workoutTitle = mWorkout.activityName;
        }
        if (workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) workoutTitle = "Steps";
        mRegionList = getResources().getStringArray(R.array.region_list);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(getApplicationContext());
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mHandler = new Handler();
        mReceiver = new CacheResultReceiver(new Handler());
        mMessagesViewModel = new ViewModelProvider(DetailActivity.this).get(MessagesViewModel.class);
        mMessagesViewModel.getWorkInProgress().observe(DetailActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DetailActivity.this);
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
                }else{
                    notificationManager.cancel(notificationID);
                }
            }
        });
        mMessagesViewModel.getLiveIntent().observe(this, (Observer<Intent>) intent -> {
            if (intent == null) return;
            final String intentAction = intent.getAction();
            final Context ctx = getApplicationContext();
            Log.i(LOG_TAG, "liveIntent " + intentAction);
            if (intentAction.equals(INTENT_MESSAGE_TOAST)){
                if (intent.hasExtra(INTENT_EXTRA_MSG)) {
                    final int iType = intent.getIntExtra(KEY_FIT_TYPE, 0);
                    final int length = intent.getIntExtra(KEY_FIT_VALUE, Toast.LENGTH_SHORT);
                    String sMessage = intent.getStringExtra(INTENT_EXTRA_MSG);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(ctx, sMessage, length);
                            // toast.
                            View view = toast.getView();
                            TextView toastMessage2 = (TextView) view.findViewById(android.R.id.message);
                            if (iType == 1) {
                                view.setBackgroundResource(android.R.drawable.toast_frame);
                                view.setBackgroundColor(Color.TRANSPARENT);
                                toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.fitdata.common.R.drawable.custom_toast));
                                toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.fitdata.common.R.color.white_pressed));
                            } else {
                                if (iType == 2) {
                                    view.setBackgroundResource(android.R.drawable.toast_frame);
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                    toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.fitdata.common.R.drawable.custom_wear_toast));
                                    toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.fitdata.common.R.color.secondaryTextColor));
                                } else
                                    toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, android.R.color.black));
                            }
                            toast.show();
                        }
                    });
                }
            }
        });
        bindActivity();
        int reportType = Constants.OBJECT_TYPE_WORKOUT;
        if (Utilities.isGymWorkout(workoutType)) reportType = Constants.OBJECT_TYPE_WORKOUT_SET;
        //summaryAdapter = new ObjectAggregateSummaryAdapter(DetailActivity.this,reportType,bUseKG);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
    //    recyclerView.setLayoutManager(llm);
    //    recyclerView.setHasFixedSize(true);
    //    recyclerView.setAdapter(summaryAdapter);
        ViewCompat.setTransitionName(imageView, EXTRA_IMAGE);
        //imageView.setImageResource(getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.heart_icon));
        //imageView.setColorFilter(this.getApplicationContext().getColor(R.color.secondaryDarkColor), PorterDuff.Mode.SRC_IN);
        int vibrant = ContextCompat.getColor(this, mRefTools.getFitnessActivityColorById(workoutType));

        textViewTimeFrame.setText(Utilities.getTimeFrameText(tf));
        textViewTimeFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bLoading) return;
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(DetailActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_filter_dropdown,null,false);
                TextView title = view.findViewById(R.id.filterLabel);
                title.setText(getString(R.string.label_timeframe));
                Spinner spinner = view.findViewById(R.id.filterDropDown);
                ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.period_types, android.R.layout.simple_spinner_item);
                adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(adapterFilter);
                spinner.setSelection(indexTimeFrame);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        indexTimeFrame = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
                MaterialButton btnOk = view.findViewById(R.id.filterPositiveButton);
                MaterialButton btnCancel = view.findViewById(R.id.filterNegativeButton);
                builder.setIcon(R.drawable.noti_white_logo);
                builder.setView(view);
                final AlertDialog alertDialog = builder.create();
                title.setOnClickListener(v2 -> {
                    alertDialog.dismiss();
                });
                btnOk.setOnClickListener(v2 -> {
                    alertDialog.dismiss();
                    reloadReportData();
                });
                btnCancel.setOnClickListener(v2 -> {
                    alertDialog.dismiss();

                });
                alertDialog.show();
            }
        });
        ((TextView)findViewById(R.id.session_title)).setText(Utilities.getSummaryActivityName(getApplicationContext(),workoutType));
        imageView.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
        btnFilters.setOnClickListener(v -> showFiltersDialog());
        labelSetFilters.setOnClickListener(v -> showFiltersDialog());
        //container.setBackgroundColor(vibrant);
        final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        if (toolbar != null) toolbar.setTitle(workoutTitle);
        collapsingToolbar.setTitleEnabled(false);
        Drawable drawableUnChecked = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(getApplicationContext(), R.color.secondaryTextColor));
        toolbar.setNavigationIcon(drawableUnChecked);
        spinnerUOY.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexUOY = position;
                if (!bLoading) reloadReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final MaterialButton btnExpandCollapse = findViewById(R.id.toggle_expand_collapsible);
        btnExpandCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler == null) mHandler = new Handler();
                Runnable mResizeViewRunnable = () -> {
                    final int originalDimension = Math.round(getResources().getDimension(R.dimen.report_backdrop_height));
                    final int newDimension = Math.round(getResources().getDimension(R.dimen.report_backdrop_height2));
                    final Drawable downIcon = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_down_arrow);
                    final Drawable upIcon = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_up_arrow);
                    ViewGroup.LayoutParams params = collapsingToolbar.getLayoutParams();
                  //  ViewGroup.LayoutParams params2 = reportFrame.getLayoutParams();
                 //   ViewGroup.LayoutParams params3 = imageView.getLayoutParams();
                    TransitionManager.beginDelayedTransition(collapsingToolbar, new TransitionSet()
                            .addTransition(new ChangeBounds()).setDuration(2000l));
                    final boolean bOriginalSize = (params.height == originalDimension);
                    if (bOriginalSize) {
                        params.height = newDimension;
                  //      params2.height = newDimension;
                     //   params3.height = newDimension;
                    }else {
                        params.height = originalDimension;
                  //      params2.height = originalDimension;
                    //    params3.height = originalDimension;
                    }
                    final ViewGroup.LayoutParams finalParams = params;
               //     final ViewGroup.LayoutParams finalParams2 = params2;
               //     final ViewGroup.LayoutParams finalParams3 = params3;
                    // Update the UI
                    runOnUiThread(() -> {
                        collapsingToolbar.setLayoutParams(finalParams);
                        if (bOriginalSize) {
                            btnExpandCollapse.setIcon(upIcon);
                        }else {
                            btnExpandCollapse.setIcon(downIcon);
                        }
                    });
                };

                mHandler.postDelayed(mResizeViewRunnable, 500);
            }
        });


        loadBackdrop();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bLoading = false;
            }
        }, 2000L);


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(Constants.KEY_FIT_USER,sUserID);
        outState.putString(Constants.KEY_FIT_DEVICE_ID,sDeviceID);
        outState.putLong(EXTRA_TYPE, workoutType);
        outState.putString(EXTRA_TITLE,workoutTitle);
        outState.putInt(ARG_PAGE,indexTimeFrame);
        outState.putInt(ARG_CHART,indexChart);
        outState.putInt(Constants.KEY_FIT_TYPE,reportType);
        outState.putInt(Constants.KEY_INDEX_GROUP,indexGroup);
        outState.putInt(Constants.KEY_INDEX_METRIC,indexMetric);
        outState.putInt(Constants.KEY_INDEX_UOY,indexUOY);
        outState.putInt(Constants.KEY_INDEX_FILTER,indexFilter);
        super.onSaveInstanceState(outState);

    }    
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadReportData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
        if (mHandler != null && mCardViewRunnable != null) {
            mHandler.removeCallbacks(mCardViewRunnable);
        }

    }
    ///////////////////////////////////////
    // Private Functions
    ///////////////////////////////////////
    private void showFiltersDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(DetailActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.dialog_summary_filter_dropdown,null,false);
        TextView title = view.findViewById(R.id.filterTitle);
        bLoading = true;
        title.setText(workoutTitle);
        final Spinner spinnerChart = view.findViewById(R.id.chartDropDown);
        final Spinner spinnerMetrics = view.findViewById(R.id.spinnerMetricsFilter);
        final Spinner spinnerGrouping = view.findViewById(R.id.spinnerGroupingFilter);
        final Spinner spinnerFilter = view.findViewById(R.id.filterDropDown);
        final TextView filterLabel = view.findViewById(R.id.filterLabel);
        final Spinner spinnerTimeFrame = view.findViewById(R.id.spinnerTimeFrameFilter);

        if (!Utilities.isGymWorkout(workoutType)){
            spinnerGrouping.setVisibility(View.GONE);
            view.findViewById(R.id.groupingLabel).setVisibility(View.GONE);
            view.findViewById(R.id.filterLabel).setVisibility(View.GONE);
            spinnerFilter.setVisibility(View.GONE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.graph_cardio_metrics, R.layout.spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMetrics.setAdapter(adapter);
            spinnerMetrics.setSelection(indexMetric);
        }else{
            ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.region_list, android.R.layout.simple_spinner_item);
            adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            spinnerFilter.setAdapter(adapterFilter);
            spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    indexFilter = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
            spinnerFilter.setSelection(indexFilter);
            spinnerGrouping.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    indexGroup = position;
                    boolean bHideTarget = true;
                    bHideTarget = (position != 2 && position != 3);
                    final boolean bHide = bHideTarget;
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            spinnerFilter.setEnabled(bHide);
                            filterLabel.setEnabled(bHide);
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
            spinnerGrouping.setSelection(indexGroup);
            spinnerMetrics.setSelection(indexMetric);
        }
        spinnerChart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexChart=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerChart.setSelection(indexChart);
        spinnerMetrics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                indexMetric = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        spinnerTimeFrame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexTimeFrame = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerTimeFrame.setSelection(indexTimeFrame);
        MaterialButton btnOk = view.findViewById(R.id.filterPositiveButton);
        MaterialButton btnCancel = view.findViewById(R.id.filterNegativeButton);
        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        title.setOnClickListener(v -> {
            bLoading = false;
            if (alertDialog != null) alertDialog.dismiss();
        });
        btnOk.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
            bLoading = false;
            reloadReportData();
        });
        btnCancel.setOnClickListener(v -> {
            bLoading = false;
            if (alertDialog != null) alertDialog.dismiss();

        });
        alertDialog.show();
    }

    private void loadBackdrop() {
        Glide.with(this).load(R.drawable.back_workouts_for_men_the_6_best_routines).apply(RequestOptions.centerCropTransform()).into(imageView);
    }

    private void reloadReportData(){
        if (mMessagesViewModel.isWorkInProgress()) return;
        mReceiver.setReceiver(this);
        try {
            mMessagesViewModel.setWorkType(Constants.TASK_ACTION_READ_CLOUD);
            mMessagesViewModel.setWorkInProgress(true);
            Utilities.TimeFrame tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
            if (indexTimeFrame == 1) tf = Utilities.TimeFrame.BEGINNING_OF_WEEK;
            if (indexTimeFrame == 2) tf = Utilities.TimeFrame.LAST_WEEK;
            if (indexTimeFrame == 3) tf = Utilities.TimeFrame.BEGINNING_OF_MONTH;
            if (indexTimeFrame == 4) tf = Utilities.TimeFrame.LAST_MONTH;
            if (indexTimeFrame == 5) tf = Utilities.TimeFrame.NINETY_DAYS;
            if (indexTimeFrame == 6) tf = Utilities.TimeFrame.BEGINNING_OF_YEAR;
            final String tfName = Utilities.getTimeFrameText(tf);
            String sFilter = getString(R.string.label_bodyregion);
            if (Utilities.isGymWorkout(workoutType)) {
                if (indexGroup == 1) {
                    if (indexFilter == 0)
                        sFilter = getString(R.string.label_bodypart_plural);
                    else
                        sFilter = mRegionList[indexFilter] + Constants.ATRACKIT_SPACE + getString(R.string.label_bodypart_plural);
                }
                if (indexGroup == 2) {
                    if (indexFilter == 0)
                        sFilter = getString(R.string.label_exercise);
                    else
                        sFilter = mRegionList[indexFilter] + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);

                }
                if (indexGroup == 3)
                    sFilter = getString(R.string.label_resistance_type);
            }
            String sFilterMsg = sFilter;
            runOnUiThread(() -> {
                textViewTimeFrame.setText(tfName);
                labelSetFilters.setText(sFilterMsg);
            });

            if (Utilities.isGymWorkout(workoutType))
                CacheManager.getSummary(indexTimeFrame, workoutType, mReceiver, DetailActivity.this,sUserID,indexGroup,indexMetric, indexUOY, indexFilter);
            else
                CacheManager.getSummary(indexTimeFrame, workoutType, mReceiver, DetailActivity.this,sUserID,0, indexMetric, indexUOY, indexFilter);
        }catch (Exception e){
            Log.e(DetailActivity.class.getSimpleName()," error resuming " + e.getMessage());
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
    private void bindActivity(){
        mAppBarLayout = findViewById(R.id.appbar);
        imageView = findViewById(R.id.image_drop);
        //recyclerView = findViewById(R.id.recyclerview_report_detail);
        expandableListView = findViewById(R.id.expandable_list);
        listDataHeader = new ArrayList<>();
        expandableListAdapter = new ExpandableListAdapter(DetailActivity.this,listDataHeader,listDataChild);
        expandableListView.setAdapter(expandableListAdapter);
        reportFrame = findViewById(R.id.frame_drop);
       // reportFrame2 = findViewById(R.id.viewDrop);
        //reportFrame3 = findViewById(R.id.viewDrop2);
        btnFilters = findViewById(R.id.filterImageButton);
        spinnerUOY = findViewById(R.id.spinnerUOY);
        textViewTimeFrame =findViewById(R.id.textViewTimeFrame);
        sessionText = findViewById(R.id.session_text);
        labelSetFilters = findViewById(R.id.labelSetFilters);
    }
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_detail;
    }


    private void setGraphicalView() {
        if (mHandler == null) mHandler = new Handler();
        mGraphicalViewRunnable = () -> {
            Boolean bGym = Utilities.isGymWorkout(workoutType);
            Calendar calendar = Calendar.getInstance();
            long startOfDay = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            calendar.setTimeInMillis(startOfDay);
            String[] metricsArray = getResources().getStringArray(bGym ? R.array.graph_gym_metrics : R.array.graph_cardio_metrics);
            String[] uoyArray = getResources().getStringArray(R.array.graph_unit_of_year);
            String uoyTitle = uoyArray[indexUOY];
            String reportTitle;
            ArrayList<String> seriesTitlesArrayList = new ArrayList<>();
            ArrayList<String> seriesUOYArrayList = new ArrayList<>();
            ArrayList<Date> dateArrayList = new ArrayList<>();

            String objectGroupType = getString(R.string.label_bodyregion);
            if (bGym) {
                if (indexGroup == 1)
                    objectGroupType = getString(R.string.label_bodypart_plural);
                if (indexGroup == 2)
                    objectGroupType = getString(R.string.label_exercise);
                if (indexGroup == 3)
                    objectGroupType = getString(R.string.label_resistance_type);
                for (SetAggTuple setAgg : setAggList) {
                    if (setAgg.desc != null) {
                        try {
                            if (!seriesUOYArrayList.contains(setAgg.unit_of_year)) {
                                seriesUOYArrayList.add(setAgg.unit_of_year);
                                int UOY = Integer.parseInt(setAgg.unit_of_year);
                                if (indexUOY == 0)
                                    calendar.set(Calendar.DAY_OF_YEAR, UOY);
                                if (indexUOY == 1) {
                                    calendar.set(Calendar.WEEK_OF_YEAR, UOY);
                                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                                }
                                if (indexUOY == 2) {
                                    calendar.set(Calendar.MONTH, UOY);
                                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                                }
                                if (!dateArrayList.contains(calendar.getTime()))
                                    dateArrayList.add(calendar.getTime());
                            }
                            if (!seriesTitlesArrayList.contains(setAgg.desc))
                                seriesTitlesArrayList.add(setAgg.desc);
                        } catch (NumberFormatException ne) {
                            ne.printStackTrace();
                        }
                    }
                }
            }
            else {
                for (WorkoutAggTuple wAgg : workoutAggList) {
                    if (wAgg.desc != null) {
                        try {
                            if (!seriesUOYArrayList.contains(wAgg.unit_of_year)) {
                                seriesUOYArrayList.add(wAgg.unit_of_year);
                                int UOY = Integer.parseInt(wAgg.unit_of_year);
                                if (indexUOY == 0)
                                    calendar.set(Calendar.DAY_OF_YEAR, UOY);
                                if (indexUOY == 1) {
                                    calendar.set(Calendar.WEEK_OF_YEAR, UOY);
                                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                                }
                                if (indexUOY == 2) {
                                    calendar.set(Calendar.MONTH, UOY);
                                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                                }
                                if (!dateArrayList.contains(calendar.getTime())) dateArrayList.add(calendar.getTime());
                            }
                            if (!seriesTitlesArrayList.contains(wAgg.desc)) seriesTitlesArrayList.add(wAgg.desc);
                        } catch (NumberFormatException ne) {
                            ne.printStackTrace();
                        }
                    }
                }
            }
            int titlesCount = seriesTitlesArrayList.size();
            int[] rainbow = getResources().getIntArray(R.array.rainbow);
            int[] setColors = Arrays.copyOfRange(rainbow, 0, titlesCount);
            PointStyle[] templates = new PointStyle[]{PointStyle.DIAMOND, PointStyle.SQUARE, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE, PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE, PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE};
            PointStyle[] setStyles = Arrays.copyOfRange(templates, 0, titlesCount);
            String[] seriesTitles = new String[titlesCount];
            seriesTitles = seriesTitlesArrayList.toArray(seriesTitles);
            Date[] seriesDates = new Date[dateArrayList.size()];
            seriesDates = dateArrayList.toArray(seriesDates);
            List<double[]> values = new ArrayList<double[]>();
            Double dMax = null; Double dMin = null;
            Date minDate = null; Date maxDate = null;
            if (bGym){
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[seriesUOYArrayList.size()]; // a value for each date - null if not set
                    List<String> childValues = new ArrayList<String>();
                    int i = 0; // index into values array - one for each dateArray
                    int indexDate = 0;
                    for (String uoyValue: seriesUOYArrayList){  // each date
                        boolean bFound = false;
                        for (SetAggTuple setAgg : setAggList){  // find sets with data
                            if ((setAgg.unit_of_year.equals(uoyValue)) && (setAgg.desc != null) && (setAgg.desc.equals(desc))){
                                bFound = true;
                                switch (indexMetric){
                                    case 0:             // reps
                                        if (setAgg.repSum != null) {
                                            itemValues[i++] = setAgg.repSum;
                                            if ((dMax == null) || (setAgg.repSum > dMax))
                                                dMax = (double) setAgg.repSum;
                                            if ((dMin == null) || (setAgg.repSum < dMin))
                                                dMin = (double) setAgg.repSum;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 1:             // sets
                                        itemValues[i++] = setAgg.setCount;
                                        if ((dMax == null) || (setAgg.setCount > dMax))
                                            dMax = (double) setAgg.setCount;
                                        if ((dMin == null) || (setAgg.setCount < dMin))
                                            dMin = (double) setAgg.setCount;
                                        break;
                                    case 2:             // weight
                                        if (setAgg.weightSum != null) {
                                            itemValues[i++] = setAgg.weightSum;
                                            if ((dMax == null) || (setAgg.weightSum > dMax))
                                                dMax = (double) setAgg.weightSum;
                                            if ((dMin == null) || (setAgg.weightSum < dMin))
                                                dMin = (double) setAgg.weightSum;
                                        }else
                                            itemValues[i++] =MathHelper.NULL_VALUE;
                                        break;
                                    case 3:             // watts
                                        if (setAgg.wattsSum != null) {
                                            itemValues[i++] = setAgg.wattsSum;
                                            if ((dMax == null) || (setAgg.wattsSum > dMax)) dMax = (double) setAgg.wattsSum;
                                            if ((dMin == null) || (setAgg.wattsSum < dMin)) dMin = (double) setAgg.wattsSum;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 4:             // duration
                                        if (setAgg.durationSum != null) {
                                            double dbl = setAgg.durationSum/60000;
                                            itemValues[i++] = dbl;  // minutes
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 5:             // bpm
                                        if (setAgg.startBPM != null || setAgg.endBPM != null) {
                                            float fBPM = (((setAgg.startBPM != null) ? setAgg.startBPM:0F) + ((setAgg.endBPM!=null)? setAgg.endBPM:0F));
                                            if ((setAgg.startBPM != null && setAgg.endBPM != null)) {
                                                fBPM = fBPM/2;
                                            }
                                            if ((dMax == null) || (fBPM > dMax))
                                                dMax = (double) fBPM;
                                            if ((dMin == null) || (fBPM < dMin))
                                                dMin = (double) fBPM;
                                            itemValues[i++] = fBPM;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;                                        
                                }

                                String sTemp = Utilities.getDateString(seriesDates[indexDate].getTime()) + Constants.ATRACKIT_SPACE;
                                String setDesc = Constants.ATRACKIT_EMPTY;
                                if (setAgg.sessionCount != null && setAgg.sessionCount > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_session_count) + setAgg.sessionCount;

                                if (setAgg.durationSum != null && setAgg.durationSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_duration_sum) + Utilities.getDurationBreakdown(setAgg.durationSum);
                                if (setAgg.stepSum != null && setAgg.stepSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_step_sum) + setAgg.stepSum;

                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                if (setAgg.startBPM != null && setAgg.startBPM > 0) {
                                    if ((setAgg.endBPM != null && setAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_avg) + (setAgg.startBPM + setAgg.endBPM) / 2;
                                    } else {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_start) + setAgg.startBPM;
                                    }
                                } else {
                                    if ((setAgg.endBPM != null && setAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_end) + setAgg.endBPM;
                                    }
                                }
                                if (sTemp.length() > 0)
                                    setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                sTemp += getString(R.string.label_agg_set_sum) + setAgg.setCount;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_rep_sum) + setAgg.repSum;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_avg) + setAgg.repAvg;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                String weight;
                                String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
                                float tempWeight = 0f;
                                double intWeight = 0d;
                                if (setAgg.weightSum != null)
                                    tempWeight = ((bUseKG) ? setAgg.weightSum : Utilities.KgToPoundsDisplay(setAgg.weightSum));

                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp = getString(R.string.label_agg_weight_total) + weight;

                                tempWeight = 0f; intWeight = 0d;
                                tempWeight = ((bUseKG) ? setAgg.weightAvg : Utilities.KgToPoundsDisplay(setAgg.weightAvg));
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_weight_avg) + weight;
                                tempWeight = setAgg.wattsSum;
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += Constants.KJ_TAIL;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_watts) + weight;
                                setDesc += sTemp;
                                childValues.add(setDesc);
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                        else
                            listDataChild.put(desc,childValues);
                        indexDate++;
                    }

                    values.add(itemValues);
                  //  dateList.add(seriesDates);
                }
            }
            else {
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[seriesUOYArrayList.size()];
                    int indexDate = 0;
                    int i = 0; // index into values array - one for each dateArray
                    for (String uoyValue: seriesUOYArrayList) {
                        boolean bFound = false;
                        List<String> childValues = new ArrayList<String>();
                        for (WorkoutAggTuple wAgg : workoutAggList) {
                            if ((wAgg.unit_of_year.equals(uoyValue)) && (wAgg.desc != null) && (wAgg.desc.equals(desc))) {
                                bFound = true;
                                switch (indexMetric) {
                                    case 2:             // bpm
                                        if (wAgg.startBPM != null || wAgg.endBPM != null) {
                                            float fBPM = (((wAgg.startBPM != null) ? wAgg.startBPM:0F) + ((wAgg.endBPM!=null)? wAgg.endBPM:0F));
                                            if ((wAgg.startBPM != null && wAgg.endBPM != null)) {
                                                fBPM = fBPM/2;
                                            }
                                            if ((dMax == null) || (fBPM > dMax))
                                                dMax = (double) fBPM;
                                            if ((dMin == null) || (fBPM < dMin))
                                                dMin = (double) fBPM;
                                            itemValues[i++] = fBPM;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 1:             // steps
                                        if (wAgg.stepSum != null) {
                                            itemValues[i++] = wAgg.stepSum;
                                            if ((dMax == null) || (wAgg.stepSum > dMax))
                                                dMax = (double) wAgg.stepSum;
                                            if ((dMin == null) || (wAgg.stepSum < dMin))
                                                dMin = (double) wAgg.stepSum;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 0:             // duration
                                        if (wAgg.durationSum != null) {
                                            double dbl = wAgg.durationSum/60000;
                                            itemValues[i++] = dbl;
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                }

                                String sTemp = Utilities.getDateString(seriesDates[indexDate].getTime()) + Constants.ATRACKIT_SPACE;
                                String setDesc = sTemp;
                                if (wAgg.sessionCount != null && wAgg.sessionCount > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_session_count) + wAgg.sessionCount;

                                if (wAgg.durationSum != null && wAgg.durationSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_duration_sum) + Utilities.getDurationBreakdown(wAgg.durationSum);
                                if (wAgg.stepSum != null && wAgg.stepSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_step_sum) + wAgg.stepSum;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                if (wAgg.startBPM != null && wAgg.startBPM > 0) {
                                    if ((wAgg.endBPM != null && wAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_avg) + (wAgg.startBPM + wAgg.endBPM) / 2;
                                    } else {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_start) + wAgg.startBPM;
                                    }
                                } else {
                                    if ((wAgg.endBPM != null && wAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_end) + wAgg.endBPM;
                                    }
                                }
                                if (sTemp.length() > 0)
                                    setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                sTemp += getString(R.string.label_agg_set_sum) + wAgg.setCount;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_rep_sum) + wAgg.repSum;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_avg) + wAgg.repAvg;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                String weight;
                                String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
                                float tempWeight = 0f;
                                double intWeight = 0d;
                                if (wAgg.weightSum != null)
                                    tempWeight = ((bUseKG) ? wAgg.weightSum : Utilities.KgToPoundsDisplay(wAgg.weightSum));

                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp = getString(R.string.label_agg_weight_total) + weight;

                                tempWeight = 0f; intWeight = 0d;
                                if (wAgg.weightAvg != null)
                                    tempWeight = ((bUseKG) ? wAgg.weightAvg : Utilities.KgToPoundsDisplay(wAgg.weightAvg));
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_weight_avg) + weight;
                                tempWeight = 0F;
                                if (wAgg.wattsSum != null)
                                    tempWeight = wAgg.wattsSum;
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += Constants.KJ_TAIL;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_watts) + weight;
                                setDesc += sTemp;
                                childValues.add(setDesc);                                
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                        else
                            listDataChild.put(desc,childValues);
                        indexDate++;
                    }
                    values.add(itemValues);
                   // dateList.add(seriesDates);
                }
            }
            listDataHeader.addAll(seriesTitlesArrayList);
            if (bGym)
                 reportTitle= metricsArray[indexMetric] + Constants.ATRACKIT_SPACE + "Per " + objectGroupType;
            else
                 reportTitle= metricsArray[indexMetric] + Constants.ATRACKIT_SPACE + "Per " + uoyTitle;
            BarTimeChart barTimeChart = new BarTimeChart();
            barTimeChart.setDisplayMetrics(densityDpi);
            barTimeChart.setBackgroundColor(getColor(R.color.primaryDarkColor));
            if ((bGym && (indexMetric == 4)) || (indexMetric == 0))
                barTimeChart.addYTitle(getString(R.string.label_mins));
            else
                barTimeChart.addYTitle(metricsArray[indexMetric]);

            barTimeChart.addXTitle(uoyTitle);
            barTimeChart.addColors(setColors);
            barTimeChart.addDates(seriesDates);
            barTimeChart.addStyles(setStyles);
            barTimeChart.setValuesList(values);
            barTimeChart.setUOY(indexUOY);
            String[] chartList= getResources().getStringArray(R.array.graph_chart_types);
            barTimeChart.setChartType(chartList[indexChart]);
            barTimeChart.addTitles(seriesTitles);
            barTimeChart.addReportTitle(reportTitle)            ;
            minDate = Collections.min(dateArrayList);
            maxDate = Collections.max(dateArrayList);
            barTimeChart.setXMax(maxDate.getTime());
            barTimeChart.setXMin(minDate.getTime());
            if (dMin != null) barTimeChart.setYMin(dMin);
            if (dMax != null) barTimeChart.setYMax(dMax);
            final GraphicalView graphicalView = barTimeChart.getView(getApplicationContext(),densityDpi);
            // Update the UI
            if (graphicalView != null)
                runOnUiThread(() -> {
                    expandableListAdapter.setListData(listDataHeader,listDataChild);
                    expandableListAdapter.notifyDataSetChanged();
                    expandableListView.setVisibility(View.VISIBLE);
                    if (imageView.getVisibility() == View.VISIBLE) crossFadeOut(imageView);
                    if (reportFrame.getChildCount() > 0) reportFrame.removeAllViews();
                    reportFrame.addView(graphicalView,new ViewGroup.LayoutParams (
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    crossFadeIn(reportFrame);
                    mAppBarLayout.setMinimumHeight(550);
                    toolbar.setTitle(metricsArray[indexMetric]);
                });
        };
        mHandler.postDelayed(mGraphicalViewRunnable, 200);
    }

    private void setFitnessGraphicalView() {
        if (mHandler == null) mHandler = new Handler();
        mGraphicalViewRunnable = () -> {
            Boolean bGym = Utilities.isGymWorkout(workoutType);
            Calendar calendar = Calendar.getInstance();

            String[] metricsArray = getResources().getStringArray(bGym ? R.array.graph_gym_metrics : R.array.graph_cardio_metrics);
            String[] uoyArray = getResources().getStringArray(R.array.graph_unit_of_year);
            String uoyTitle = uoyArray[indexUOY];
            String reportTitle;
            String sFilter = null;
            ArrayList<String> seriesTitlesArrayList = new ArrayList<>();
            ArrayList<String> seriesUOYArrayList = new ArrayList<>();
            ArrayList<Date> dateArrayList = new ArrayList<>();
            String objectGroupType = getString(R.string.label_bodyregion);
            if (bGym){
                if (indexGroup == 1)
                    objectGroupType = getString(R.string.label_bodypart_plural);
                if (indexGroup == 2)
                    objectGroupType = getString(R.string.label_exercise);
                if (indexGroup == 3)
                    objectGroupType = getString(R.string.label_resistance_type);
                for(WorkoutSet set : setList){
                    calendar.setTimeInMillis(set.start);
                    String sTitle = Constants.ATRACKIT_EMPTY;
                    if (indexGroup == 0)
                        sTitle = set.regionName;
                    if (indexGroup == 1)
                        sTitle = set.bodypartName;
                    if (indexGroup == 2)
                        sTitle = set.exerciseName;
                    if (indexGroup == 3)
                        sTitle = Utilities.getResistanceType(set.resistance_type);

                    if (sTitle.length() > 0 && !seriesTitlesArrayList.contains(sTitle)) seriesTitlesArrayList.add(sTitle);
                    if (!dateArrayList.contains(calendar.getTime())) dateArrayList.add(calendar.getTime());
                }
            }
            else {
                for (Workout w: workoutList){
                    String sTitle = Constants.ATRACKIT_EMPTY;
                        if (Utilities.isDetectedActivity(workoutType) && Utilities.isDetectedActivity(w.activityID))
                            sTitle = "Detected";
                        if ((workoutType == Constants.SELECTION_ACTIVITY_GYM) && (Utilities.isGymWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_workout);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_SHOOT) && (Utilities.isShooting(w.activityID)))
                            sTitle = getString(R.string.action_new_archery);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_CARDIO) && (Utilities.isCardioWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_cardio);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_RUN) && (Utilities.isRunning(w.activityID)))
                            sTitle = getString(R.string.action_new_run);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_BIKE) && (Utilities.isBikeWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_bike);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_SPORT) && (Utilities.isSportWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_sport);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_WINTER) && (Utilities.isWinterWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_winter);
                        if ((workoutType == Constants.SELECTION_ACTIVITY_WATER) && (Utilities.isAquatic(w.activityID)))
                            sTitle = getString(R.string.action_new_water);
                        if ((workoutType == SELECTION_ACTIVITY_MISC) && (Utilities.isMiscellaneousWorkout(w.activityID)))
                            sTitle = getString(R.string.action_new_misc);
                        if (!dateArrayList.contains(calendar.getTime()))
                           dateArrayList.add(calendar.getTime());
                        if (!seriesTitlesArrayList.contains(sTitle)) seriesTitlesArrayList.add(sTitle);
                }
            }
            int titlesCount = seriesTitlesArrayList.size();
            int[] rainbow = getResources().getIntArray(R.array.rainbow);
            int[] setColors = Arrays.copyOfRange(rainbow,0,titlesCount);
            PointStyle[] templates = new PointStyle[] { PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE,PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE,PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
                    PointStyle.SQUARE, PointStyle.CIRCLE };
            PointStyle[] setStyles = Arrays.copyOfRange(templates,0,titlesCount);


            FitnessTimeChart fitnessTimeChart = new FitnessTimeChart();
            fitnessTimeChart.setDisplayMetrics(densityDpi);

            fitnessTimeChart.setBackgroundColor(getColor(R.color.primaryDarkColor));
            if ((bGym && (indexMetric == 4)) || (indexMetric == 0))
                fitnessTimeChart.addYTitle(getString(R.string.label_mins));
            else
                fitnessTimeChart.addYTitle(metricsArray[indexMetric]);

            fitnessTimeChart.addXTitle(uoyTitle);
            fitnessTimeChart.addColors(setColors);
            fitnessTimeChart.addStyles(setStyles);

            String[] seriesTitles = new String[titlesCount];
            seriesTitles = seriesTitlesArrayList.toArray(seriesTitles);
            Date[] seriesDates = new Date[dateArrayList.size()];
            seriesDates = dateArrayList.toArray(seriesDates);
            List<double[]> values = new ArrayList<double[]>();
            Double dMax = null; Double dMin = null;
            Date minDate = null; Date maxDate = null;
            if (bGym){
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[seriesUOYArrayList.size()]; // a value for each date - null if not set
                    List<String> childValues = new ArrayList<String>();
                    int i = 0; // index into values array - one for each dateArray
                    int indexDate = 0;
                    for (String uoyValue: seriesUOYArrayList){  // each date
                        boolean bFound = false;
                        for (SetAggTuple setAgg : setAggList){  // find sets with data
                            if ((setAgg.unit_of_year.equals(uoyValue)) && (setAgg.desc != null) && (setAgg.desc.equals(desc))){
                                bFound = true;
                                switch (indexMetric){
                                    case 0:             // reps
                                        if (setAgg.repSum != null) {
                                            itemValues[i++] = setAgg.repSum;
                                            if ((dMax == null) || (setAgg.repSum > dMax))
                                                dMax = (double) setAgg.repSum;
                                            if ((dMin == null) || (setAgg.repSum < dMin))
                                                dMin = (double) setAgg.repSum;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 1:             // sets
                                        itemValues[i++] = setAgg.setCount;
                                        if ((dMax == null) || (setAgg.setCount > dMax))
                                            dMax = (double) setAgg.setCount;
                                        if ((dMin == null) || (setAgg.setCount < dMin))
                                            dMin = (double) setAgg.setCount;
                                        break;
                                    case 2:             // weight
                                        if (setAgg.weightSum != null) {
                                            itemValues[i++] = setAgg.weightSum;
                                            if ((dMax == null) || (setAgg.weightSum > dMax))
                                                dMax = (double) setAgg.weightSum;
                                            if ((dMin == null) || (setAgg.weightSum < dMin))
                                                dMin = (double) setAgg.weightSum;
                                        }else
                                            itemValues[i++] =MathHelper.NULL_VALUE;
                                        break;
                                    case 3:             // watts
                                        if (setAgg.wattsSum != null) {
                                            itemValues[i++] = setAgg.wattsSum;
                                            if ((dMax == null) || (setAgg.wattsSum > dMax)) dMax = (double) setAgg.wattsSum;
                                            if ((dMin == null) || (setAgg.wattsSum < dMin)) dMin = (double) setAgg.wattsSum;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 4:             // duration
                                        if (setAgg.durationSum != null) {
                                            double dbl = setAgg.durationSum/60000;
                                            itemValues[i++] = dbl;  // minutes
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 5:             // bpm
                                        if (setAgg.startBPM != null || setAgg.endBPM != null) {
                                            float fBPM = (((setAgg.startBPM != null) ? setAgg.startBPM:0F) + ((setAgg.endBPM!=null)? setAgg.endBPM:0F));
                                            if ((setAgg.startBPM != null && setAgg.endBPM != null)) {
                                                fBPM = fBPM/2;
                                            }
                                            if ((dMax == null) || (fBPM > dMax))
                                                dMax = (double) fBPM;
                                            if ((dMin == null) || (fBPM < dMin))
                                                dMin = (double) fBPM;
                                            itemValues[i++] = fBPM;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                }

                                String sTemp = Utilities.getDateString(seriesDates[indexDate].getTime()) + Constants.ATRACKIT_SPACE;
                                String setDesc = Constants.ATRACKIT_EMPTY;
                                if (setAgg.sessionCount != null && setAgg.sessionCount > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_session_count) + setAgg.sessionCount;

                                if (setAgg.durationSum != null && setAgg.durationSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_duration_sum) + Utilities.getDurationBreakdown(setAgg.durationSum);
                                if (setAgg.stepSum != null && setAgg.stepSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_step_sum) + setAgg.stepSum;

                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                if (setAgg.startBPM != null && setAgg.startBPM > 0) {
                                    if ((setAgg.endBPM != null && setAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_avg) + (setAgg.startBPM + setAgg.endBPM) / 2;
                                    } else {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_start) + setAgg.startBPM;
                                    }
                                } else {
                                    if ((setAgg.endBPM != null && setAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_end) + setAgg.endBPM;
                                    }
                                }
                                if (sTemp.length() > 0)
                                    setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                sTemp += getString(R.string.label_agg_set_sum) + setAgg.setCount;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_rep_sum) + setAgg.repSum;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_avg) + setAgg.repAvg;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                String weight;
                                String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
                                float tempWeight = 0f;
                                double intWeight = 0d;
                                if (setAgg.weightSum != null)
                                    tempWeight = ((bUseKG) ? setAgg.weightSum : Utilities.KgToPoundsDisplay(setAgg.weightSum));

                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp = getString(R.string.label_agg_weight_total) + weight;

                                tempWeight = 0f; intWeight = 0d;
                                tempWeight = ((bUseKG) ? setAgg.weightAvg : Utilities.KgToPoundsDisplay(setAgg.weightAvg));
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_weight_avg) + weight;
                                tempWeight = setAgg.wattsSum;
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += Constants.KJ_TAIL;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_watts) + weight;
                                setDesc += sTemp;
                                childValues.add(setDesc);
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                        else
                            listDataChild.put(desc,childValues);
                        indexDate++;
                    }

                    values.add(itemValues);
                   // dateList.add(seriesDates);
                }
            }
            else {
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[seriesUOYArrayList.size()];
                    int indexDate = 0;
                    int i = 0; // index into values array - one for each dateArray
                    for (String uoyValue: seriesUOYArrayList) {
                        boolean bFound = false;
                        List<String> childValues = new ArrayList<String>();
                        for (WorkoutAggTuple wAgg : workoutAggList) {
                            if ((wAgg.unit_of_year.equals(uoyValue)) && (wAgg.desc != null) && (wAgg.desc.equals(desc))) {
                                bFound = true;
                                switch (indexMetric) {
                                    case 2:             // bpm
                                        if (wAgg.startBPM != null || wAgg.endBPM != null) {
                                            float fBPM = (((wAgg.startBPM != null) ? wAgg.startBPM:0F) + ((wAgg.endBPM!=null)? wAgg.endBPM:0F));
                                            if ((wAgg.startBPM != null && wAgg.endBPM != null)) {
                                                fBPM = fBPM/2;
                                            }
                                            if ((dMax == null) || (fBPM > dMax))
                                                dMax = (double) fBPM;
                                            if ((dMin == null) || (fBPM < dMin))
                                                dMin = (double) fBPM;
                                            itemValues[i++] = fBPM;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 1:             // steps
                                        if (wAgg.stepSum != null) {
                                            itemValues[i++] = wAgg.stepSum;
                                            if ((dMax == null) || (wAgg.stepSum > dMax))
                                                dMax = (double) wAgg.stepSum;
                                            if ((dMin == null) || (wAgg.stepSum < dMin))
                                                dMin = (double) wAgg.stepSum;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 0:             // duration
                                        if (wAgg.durationSum != null) {
                                            double dbl = wAgg.durationSum/60000;
                                            itemValues[i++] = dbl;
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                }

                                String sTemp = Utilities.getDateString(seriesDates[indexDate].getTime()) + Constants.ATRACKIT_SPACE;
                                String setDesc = sTemp;
                                if (wAgg.sessionCount != null && wAgg.sessionCount > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_session_count) + wAgg.sessionCount;

                                if (wAgg.durationSum != null && wAgg.durationSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_duration_sum) + Utilities.getDurationBreakdown(wAgg.durationSum);
                                if (wAgg.stepSum != null && wAgg.stepSum > 0)
                                    sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_step_sum) + wAgg.stepSum;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                if (wAgg.startBPM != null && wAgg.startBPM > 0) {
                                    if ((wAgg.endBPM != null && wAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_avg) + (wAgg.startBPM + wAgg.endBPM) / 2;
                                    } else {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_start) + wAgg.startBPM;
                                    }
                                } else {
                                    if ((wAgg.endBPM != null && wAgg.endBPM > 0)) {
                                        sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_bpm_end) + wAgg.endBPM;
                                    }
                                }
                                if (sTemp.length() > 0)
                                    setDesc += sTemp + Constants.LINE_DELIMITER;

                                sTemp = Constants.ATRACKIT_EMPTY;
                                sTemp += getString(R.string.label_agg_set_sum) + wAgg.setCount;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_rep_sum) + wAgg.repSum;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_avg) + wAgg.repAvg;
                                setDesc += sTemp + Constants.LINE_DELIMITER;

                                String weight;
                                String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
                                float tempWeight = 0f;
                                double intWeight = 0d;
                                if (wAgg.weightSum != null)
                                    tempWeight = ((bUseKG) ? wAgg.weightSum : Utilities.KgToPoundsDisplay(wAgg.weightSum));

                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp = getString(R.string.label_agg_weight_total) + weight;

                                tempWeight = 0f; intWeight = 0d;
                                if (wAgg.weightAvg != null)
                                    tempWeight = ((bUseKG) ? wAgg.weightAvg : Utilities.KgToPoundsDisplay(wAgg.weightAvg));
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += tail;

                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_weight_avg) + weight;
                                tempWeight = 0F;
                                if (wAgg.wattsSum != null)
                                    tempWeight = wAgg.wattsSum;
                                intWeight = Math.floor(tempWeight);
                                if (tempWeight % intWeight != 0)
                                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                                else
                                    weight = new DecimalFormat("#").format(intWeight);
                                weight += Constants.KJ_TAIL;
                                sTemp += Constants.ATRACKIT_SPACE + getString(R.string.label_agg_watts) + weight;
                                setDesc += sTemp;
                                childValues.add(setDesc);
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                        else
                            listDataChild.put(desc,childValues);
                        indexDate++;
                    }
                    values.add(itemValues);
                  //  dateList.add(seriesDates);
                }
            }
            listDataHeader.addAll(seriesTitlesArrayList);
            if (bGym){
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[dateArrayList.size()]; // a value for each date - null if not set
                    int i = 0; // index into values array - one for each dateArray
                    for (Date dateValue: dateArrayList){  // each date
                        boolean bFound = false;
                        long starting = dateValue.getTime();
                        for (WorkoutSet set : setList){  // find sets with data
                            String sTitle = Constants.ATRACKIT_EMPTY;
                            if (indexGroup == 0)
                                sTitle = set.regionName;
                            if (indexGroup == 1)
                                sTitle = set.bodypartName;
                            if (indexGroup == 2)
                                sTitle = set.exerciseName;
                            if (indexGroup == 3)
                                sTitle = Utilities.getResistanceType(set.resistance_type);                            
                            if ((set.start == starting) && (sTitle.length() > 0) && (desc.equals(sTitle))){
                                bFound = true;
                                switch (indexMetric){
                                    case 0:             // reps
                                        if (set.repCount != null) {
                                            itemValues[i++] = set.repCount;
                                            if ((dMax == null) || (set.repCount > dMax))
                                                dMax = (double) set.repCount;
                                            if ((dMin == null) || (set.repCount < dMin))
                                                dMin = (double) set.repCount;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 1:             // sets
                                        itemValues[i++] = set.setCount;
                                        if ((dMax == null) || (set.setCount > dMax))
                                            dMax = (double) set.setCount;
                                        if ((dMin == null) || (set.setCount < dMin))
                                            dMin = (double) set.setCount;
                                        break;
                                    case 2:             // weight
                                        if (set.weightTotal != null) {
                                            itemValues[i++] = set.weightTotal;
                                            if ((dMax == null) || (set.weightTotal > dMax))
                                                dMax = (double) set.weightTotal;
                                            if ((dMin == null) || (set.weightTotal < dMin))
                                                dMin = (double) set.weightTotal;
                                        }else
                                            itemValues[i++] =MathHelper.NULL_VALUE;
                                        break;
                                    case 3:             // watts
                                        if (set.wattsTotal != null) {
                                            itemValues[i++] = set.wattsTotal;
                                            if ((dMax == null) || (set.wattsTotal > dMax)) dMax = (double) set.wattsTotal;
                                            if ((dMin == null) || (set.wattsTotal < dMin)) dMin = (double) set.wattsTotal;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 4:             // duration
                                        if (set.duration > 0) {
                                            double dbl = set.duration/1000;
                                            itemValues[i++] = dbl;  // seconds
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        }else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                }
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                    }
                    values.add(itemValues);
                }
            }
            else {
                for(String desc : seriesTitlesArrayList){  // for each series - region/bodypart/exercise/resistance-type
                    double[] itemValues = new double[dateArrayList.size()];
                    int i = 0; // index into values array - one for each dateArray
                    for (Date dateValue: dateArrayList) {
                        boolean bFound = false;
                        long starting = dateValue.getTime();
                        for (Workout w : workoutList) {
                            boolean correctType = false;
                            if (desc.equals("Detected") && Utilities.isDetectedActivity(w.activityID))
                                correctType = true;
                            if (((desc.equals(getString(R.string.action_new_workout))) && (Utilities.isGymWorkout(w.activityID))))
                                correctType = true;
                            if (desc.equals(getString(R.string.action_new_archery)) && (Utilities.isShooting(w.activityID)))
                                correctType = true;
                            if (desc.equals(getString(R.string.action_new_cardio)) && (Utilities.isCardioWorkout(w.activityID)))
                                correctType = true;
                            if ((desc.equals(getString(R.string.action_new_run))) && (Utilities.isRunning(w.activityID)))
                                correctType = true;
                            if (desc.equals(getString(R.string.action_new_bike)) && (Utilities.isBikeWorkout(w.activityID)))
                                correctType = true;
                            if ((desc.equals(getString(R.string.action_new_sport))) && (Utilities.isSportWorkout(w.activityID)))
                                correctType = true;
                            if ((desc.equals(getString(R.string.action_new_winter))) && (Utilities.isWinterWorkout(w.activityID)))
                                correctType = true;
                            if ((desc.equals(getString(R.string.action_new_water))) && (Utilities.isAquatic(w.activityID)))
                                correctType = true;
                            if (desc.equals(getString(R.string.action_new_misc)) && (Utilities.isMiscellaneousWorkout(w.activityID)))
                                correctType = true;
                            if ((w.start == starting) && correctType) {
                                bFound = true;
                                switch (indexMetric) {
                                    case 1:             // steps
                                        if (w.stepCount > 0) {
                                            itemValues[i++] = w.stepCount;
                                            if ((dMax == null) || (w.stepCount > dMax))
                                                dMax = (double) w.stepCount;
                                            if ((dMin == null) || (w.stepCount < dMin))
                                                dMin = (double) w.stepCount;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                    case 0:             // duration
                                        if (w.duration > 0) {
                                            double dbl = w.duration/1000;
                                            itemValues[i++] = dbl;
                                            if ((dMax == null) || (dbl > dMax))
                                                dMax = dbl;
                                            if ((dMin == null) || (dbl < dMin))
                                                dMin = dbl;
                                        } else
                                            itemValues[i++] = MathHelper.NULL_VALUE;
                                        break;
                                }
                                break;
                            }
                        }
                        if (!bFound)
                            itemValues[i++] = MathHelper.NULL_VALUE;
                    }
                    values.add(itemValues);
                }
            }
            if (bGym) {
                reportTitle = metricsArray[indexMetric] + Constants.ATRACKIT_SPACE + "Per " + objectGroupType;
                if (indexFilter > 0){

                }
            }else
                 reportTitle= metricsArray[indexMetric] + Constants.ATRACKIT_SPACE;
            fitnessTimeChart.setValuesList(values);
            fitnessTimeChart.addDates(seriesDates);
            fitnessTimeChart.setUOY(indexUOY);
            String[] chartList= getResources().getStringArray(R.array.graph_chart_types);
            fitnessTimeChart.setChartType(chartList[indexChart]);
            fitnessTimeChart.addTitles(seriesTitles);
            fitnessTimeChart.addReportTitle(reportTitle);
           // minDate = Collections.min(dateArrayList);
            //maxDate = Collections.max(dateArrayList);
            fitnessTimeChart.setXMax(dateArrayList.size());
            fitnessTimeChart.setXMin(0);
            if (dMin != null) fitnessTimeChart.setYMin(dMin);
            if (dMax != null) fitnessTimeChart.setYMax(dMax);

            List<String> labelList = new ArrayList<>();
            String sFormat = "dd";
            if (indexUOY == 1) sFormat = "dd-MMM";
            if (indexUOY == 2) sFormat = "MMM";
            SimpleDateFormat format = new SimpleDateFormat(sFormat, Locale.getDefault());
            for(Date d : dateArrayList){
                String sTemp = format.format(d);
                labelList.add(sTemp);
            }
            fitnessTimeChart.addXLabels(labelList);
            final GraphicalView graphicalView = fitnessTimeChart.getView(DetailActivity.this, densityDpi);
            // Update the UI
            if (graphicalView != null)
                runOnUiThread(() -> {
                    expandableListAdapter.setListData(listDataHeader,listDataChild);
                    if (reportFrame.getChildCount() > 0) reportFrame.removeAllViews();
                    reportFrame.addView(graphicalView,new ViewGroup.LayoutParams (
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    reportFrame.setMinimumHeight(550);
                    crossFadeIn(reportFrame);
                });
        };
        mHandler.postDelayed(mGraphicalViewRunnable, 500);
    }
    ///////////////////////////////////////
    // CALLBACKS
    ///////////////////////////////////////
    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, msg);
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
        Context context = getApplicationContext();
        if (bDayMode)
            msgIntent.putExtra(KEY_FIT_TYPE, 2);
        else
            msgIntent.putExtra(KEY_FIT_TYPE, 1);
      //  LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
       context.sendBroadcast(msgIntent);
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (mHandler != null && mCardViewRunnable != null) {
            mHandler.removeCallbacks(mCardViewRunnable);
        }
        mMessagesViewModel.setWorkInProgress(false);
        if (resultCode == 200) {
            int retVal = resultData.getInt(Constants.KEY_FIT_VALUE);
            if (retVal == 0){
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, "Not data found");
                msgIntent.putExtra(KEY_FIT_TYPE, 2); // wear os flag
                mMessagesViewModel.addLiveIntent(msgIntent);
            }else {
                long activityID = resultData.getLong(Constants.MAP_DATA_TYPE);
                indexMetric = resultData.getInt(Constants.KEY_INDEX_METRIC,0);
                indexUOY = resultData.getInt(Constants.KEY_INDEX_UOY,spinnerUOY.getSelectedItemPosition());
                indexGroup = resultData.getInt(Constants.KEY_INDEX_GROUP, 0);
                indexTimeFrame = resultData.getInt(Constants.KEY_FIT_TYPE);
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                densityDpi = metrics.densityDpi;
                if (indexUOY > 0) {
                    if (Utilities.isGymWorkout(activityID)) {
                        setAggList = resultData.getParcelableArrayList(Constants.KEY_LIST_SETS);
                    } else {
                        workoutAggList = resultData.getParcelableArrayList(Constants.KEY_AGG_WORKOUT);
                        metaAggList = resultData.getParcelableArrayList(Constants.KEY_AGG_META);
                    }
                    setGraphicalView();
                }else{
                    workoutList = resultData.getParcelableArrayList(Constants.KEY_LIST_WORKOUTS);
                    setList = resultData.getParcelableArrayList(Constants.KEY_LIST_SETS);
                    metaAggList = resultData.getParcelableArrayList(Constants.KEY_AGG_META);
                    setFitnessGraphicalView();
                }

            }
        }
    }
}