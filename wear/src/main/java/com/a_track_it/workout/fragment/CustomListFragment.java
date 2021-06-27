package com.a_track_it.workout.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.BodypartAdapter;
import com.a_track_it.workout.adapter.DayMonthAdapter;
import com.a_track_it.workout.adapter.ExerciseAdapter;
import com.a_track_it.workout.adapter.FitnessActivityAdapter;
import com.a_track_it.workout.adapter.RegionListAdapter;
import com.a_track_it.workout.adapter.SetsRepsWeightAdapter;
import com.a_track_it.workout.adapter.WorkoutAdapter;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.BodyRegion;
import com.a_track_it.workout.common.data_model.Bodypart;
import com.a_track_it.workout.common.data_model.Exercise;
import com.a_track_it.workout.common.data_model.FitnessActivity;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.SELECTION_BODY_REGION;
import static com.a_track_it.workout.common.Constants.SELECTION_WORKOUT_HISTORY;
import static com.a_track_it.workout.common.Constants.SELECTION_WORKOUT_SET_HISTORY;

public class CustomListFragment extends DialogFragment implements AmbientInterface {
    private TextView mList_Title1;
    private TextView textViewYearSelect;
    private MaterialButton mDateButton;
    private androidx.wear.widget.WearableRecyclerView mWearableRecyclerView;
    private androidx.wear.widget.WearableRecyclerView mWearableRecyclerView2;
    private WearableLinearLayoutManager mWearableLinearLayoutManager;
    private WearableLinearLayoutManager mWearableLinearLayoutManager2;
    public static final String TAG = "CustomListFragment";
    public static final String ARG_TYPE = "ARG_TYPE";
    public static final String ARG_SET = "ARG_SET";
    public static final String ARG_PRESET = "ARG_PRESET";
    private WorkoutViewModel mSessionViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private DayMonthAdapter dayAdapter;
    private DayMonthAdapter monthAdapter;
    private WorkoutAdapter workoutAdapter;
    private RegionListAdapter regionAdapter;
    private FitnessActivityAdapter activityAdapter;
    private BodypartAdapter bodypartAdapter;
    private ExerciseAdapter exerciseAdapter;

    private SetsRepsWeightAdapter setsRepsWeightAdapter;
    private ArrayList<BodyRegion> bodyRegions;
    private ArrayList<FitnessActivity> fitnessActivities;
    private ArrayList<Bodypart> bodyparts;
    private ArrayList<Exercise> exercises;
    private ArrayList<Workout> workouts;
    private ArrayList<WorkoutSet> workoutSets;
    private Context mContext;
    private int mLoadType;
    private String sTitle;
    private long mPreset;
    private boolean externalLoad = false;
    private Workout mWorkout;
    private WorkoutSet mSet;
    private Calendar calendar;
    private String sUserID;
    private String sDeviceID;
    private boolean bUseKG = false;
    private int mShowMode = 0;
    private int mDay;
    private int mMonth;
    private int mYear;
    private MutableLiveData<List<Integer>> mDateLiveData = new MutableLiveData<>();
    private UserPreferences userPrefs = null;
    public CustomListFragment() {
        // Required empty public constructor
    }
    public static CustomListFragment create(int iType, long iPreset, String sTitle, String sUser, String sDevice) {
        final CustomListFragment fragment = new CustomListFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FIT_TYPE, iType);
        args.putLong(Constants.KEY_FIT_VALUE, iPreset);
        args.putString(Constants.KEY_FIT_NAME, sTitle);
        args.putString(Constants.KEY_FIT_USER, sUser);
        args.putString(KEY_FIT_DEVICE_ID, sDevice);
        fragment.setArguments(args);
        return fragment;
    }

    // Container Activity must implement this interface
    OnCustomListItemSelectedListener mCallback;

    // to access the selected type for setting preferences etc.
    public int getLoadType(){
        return mLoadType;
    }

    public interface OnCustomListItemSelectedListener {
        void onCustomItemSelected(int type, long id, String title, int resid, String identifier);
    }
    public void setWorkout(Workout w){
        if (workouts != null)
            workouts.clear();
        else
            workouts = new ArrayList<>();
        workouts.add(w); mWorkout = w;}
    public Workout getWorkout(){ return mWorkout; }
    public void setWorkoutSets(List<WorkoutSet> sets){
        if (workoutSets != null)
            workoutSets.clear();
        else
            workoutSets = new ArrayList<>();
        workoutSets.addAll(sets);
        if (sets.size() > 0) mSet = sets.get(0);
    }
    public void setStartMode(int i){ mShowMode = i;}
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadType = getArguments().getInt(KEY_FIT_TYPE);
            mPreset = getArguments().getLong(Constants.KEY_FIT_VALUE);
            sTitle = getArguments().getString(Constants.KEY_FIT_NAME);
            sUserID = getArguments().getString(Constants.KEY_FIT_USER);
            sDeviceID = getArguments().getString(Constants.KEY_FIT_DEVICE_ID);
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(KEY_FIT_TYPE))) {
                mLoadType = savedInstanceState.getInt(KEY_FIT_TYPE);
                mPreset = savedInstanceState.getLong(Constants.KEY_FIT_VALUE);
                sTitle = savedInstanceState.getString(Constants.KEY_FIT_NAME);
                sUserID = savedInstanceState.getString(Constants.KEY_FIT_USER);
                sDeviceID = savedInstanceState.getString(Constants.KEY_FIT_DEVICE_ID);
                bUseKG = savedInstanceState.getBoolean(Constants.KEY_USE_KG);
            }
        }
        userPrefs = UserPreferences.getPreferences(requireContext(), sUserID);
        if ((userPrefs != null) && (savedInstanceState == null)) bUseKG = userPrefs.getUseKG();
        calendar = Calendar.getInstance(TimeZone.getDefault());
        if ((mLoadType == SELECTION_WORKOUT_HISTORY) && (mPreset > 0)){
            calendar.setTimeInMillis(mPreset);
        }
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mYear = calendar.get(Calendar.YEAR);
        Integer[] dayMonth = {mDay, mMonth, mYear};
        mDateLiveData.postValue(Arrays.asList(dayMonth));

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_FIT_TYPE, mLoadType);
        outState.putLong(KEY_FIT_VALUE, mPreset);
        outState.putString(KEY_FIT_NAME, sTitle);
        outState.putString(KEY_FIT_USER,sUserID);
        outState.putString(KEY_FIT_DEVICE_ID,sDeviceID);
        outState.putBoolean(Constants.KEY_USE_KG, bUseKG);
        super.onSaveInstanceState(outState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView;
        if (mContext == null) mContext = getContext();
        if (userPrefs != null) bUseKG = userPrefs.getUseKG();
        final Drawable iconContinue = AppCompatResources.getDrawable(mContext,R.drawable.ic_play_button_white);
        if ((mLoadType == Constants.SELECTION_DAYS) || (mLoadType == Constants.SELECTION_MONTHS) || (mLoadType == Constants.SELECTION_DOY)) {
            if(Locale.getDefault().equals(Locale.US))
                rootView = inflater.inflate(R.layout.dialog_custom_date_us_entry, container, false);
            else
                rootView = inflater.inflate(R.layout.dialog_custom_date_entry, container, false);
            textViewYearSelect = rootView.findViewById(R.id.textViewYearSelect);
            textViewYearSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] items = {"2017", "2018", "2019", "2020", "2021"};
                   MaterialAlertDialogBuilder builder =  new MaterialAlertDialogBuilder(mContext);
                   builder.setTitle(mContext.getResources().getString(R.string.label_year))
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mYear = Integer.parseInt(items[which]);
                                    Integer[] dayMonth = {mDay, mMonth, mYear};
                                    mDateLiveData.postValue(Arrays.asList(dayMonth));
                                }
                            });
                    builder.show();
                }
            });
            mDateButton = rootView.findViewById(R.id.btnDateSelect);
            mDateButton.setOnClickListener(v -> {
                getDialog().dismiss();
                mCallback.onCustomItemSelected(mLoadType,  mDay, Constants.ATRACKIT_ATRACKIT_CLASS, mMonth, Integer.toString(mYear));
            });
            mWearableRecyclerView = rootView.findViewById(R.id.day_list);
            CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mWearableLinearLayoutManager = new WearableLinearLayoutManager(mContext, customScrollingLayoutCallback);
            mWearableRecyclerView.setLayoutManager(mWearableLinearLayoutManager);
            mWearableRecyclerView.setCircularScrollingGestureEnabled(false);
            mWearableRecyclerView.setBezelFraction(0.5f);
            mWearableRecyclerView.setScrollDegreesPerScreen(90);
            // To align the edge children (first and last) with the center of the screen
            mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);
            mWearableRecyclerView.setHasFixedSize(true);
            dayAdapter = new DayMonthAdapter(mContext, Constants.SELECTION_DAYS);
            mWearableRecyclerView.setAdapter(dayAdapter);
            mWearableRecyclerView.setVisibility(View.VISIBLE);
            dayAdapter.setOnItemClickListener((view, value, pos) -> {
                mDay = Integer.parseInt(value);
                Integer[] dayMonth = {mDay, mMonth, mYear};
                mDateLiveData.postValue(Arrays.asList(dayMonth));
            });

            mWearableRecyclerView2 = rootView.findViewById(R.id.month_list);
            CustomScrollingLayoutCallback customScrollingLayoutCallback2 = new CustomScrollingLayoutCallback();
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mWearableLinearLayoutManager2 = new WearableLinearLayoutManager(mContext, customScrollingLayoutCallback2);
            mWearableRecyclerView2.setLayoutManager(mWearableLinearLayoutManager2);
            mWearableRecyclerView2.setCircularScrollingGestureEnabled(false);
            mWearableRecyclerView2.setBezelFraction(0.5f);
            mWearableRecyclerView2.setScrollDegreesPerScreen(90);
            // To align the edge children (first and last) with the center of the screen
            mWearableRecyclerView2.setEdgeItemsCenteringEnabled(true);
            mWearableRecyclerView2.setHasFixedSize(true);
            monthAdapter = new DayMonthAdapter(mContext, Constants.SELECTION_MONTHS);
            mWearableRecyclerView2.setAdapter(monthAdapter);
            mWearableRecyclerView2.setVisibility(View.VISIBLE);
            monthAdapter.setOnItemClickListener((view, value, pos) -> {
                mMonth = Integer.parseInt(value);
                Integer[] dayMonth = {mDay, mMonth, mYear};
                mDateLiveData.postValue(Arrays.asList(dayMonth));
            });
            mDateLiveData.observe(getViewLifecycleOwner(), dayMonth -> {
                int day = dayMonth.get(0);
                int month = dayMonth.get(1)-1;
                int year = dayMonth.get(2);
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, day);
                new Handler(Looper.getMainLooper()).post(() -> {
                    String title = Utilities.getDateString2(calendar.getTimeInMillis());
                    mDateButton.setText(title);
                    title = ATRACKIT_EMPTY + calendar.get(Calendar.YEAR);
                    textViewYearSelect.setText(title);
                });
            });
        }
        else {
            rootView = inflater.inflate(R.layout.dialog_customlist, container, false);
            mList_Title1 = rootView.findViewById(R.id.activity_title);
            String sTemp = Utilities.SelectionTypeToString(getActivity(), mLoadType);
            if (sTemp.length() > 0) mList_Title1.setText(sTemp);
            mWearableRecyclerView = rootView.findViewById(R.id.activity_list);
            CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mWearableLinearLayoutManager = new WearableLinearLayoutManager(mContext, customScrollingLayoutCallback);
            mWearableRecyclerView.setLayoutManager(mWearableLinearLayoutManager);
            mWearableRecyclerView.setCircularScrollingGestureEnabled(false);
            mWearableRecyclerView.setBezelFraction(0.5f);
            mWearableRecyclerView.setScrollDegreesPerScreen(90);
            // To align the edge children (first and last) with the center of the screen
            mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);
            mWearableRecyclerView.setHasFixedSize(true);
        }
        if (mLoadType == Constants.SELECTION_BODY_REGION){
            if (regionAdapter == null) regionAdapter = new RegionListAdapter(mContext);
            regionAdapter.setOnItemClickListener(new RegionListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, BodyRegion viewModel) {
                    mCallback.onCustomItemSelected(mLoadType,viewModel._id,viewModel.regionName,0,null);
                    getDialog().dismiss();
                }
            });
            mWearableRecyclerView.setAdapter(regionAdapter);
        }
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)){
                if (activityAdapter == null) activityAdapter = new FitnessActivityAdapter(mContext, mLoadType);
                activityAdapter.setOnItemClickListener((view, viewModel) -> {
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, viewModel.resource_id, viewModel.identifier);
                    getDialog().dismiss();
                });
         //   }
            mWearableRecyclerView.setAdapter(activityAdapter);
        }

        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS ||
              mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS ||  mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY
                || mLoadType == Constants.SELECTION_GOOGLE_HISTORY){
            SimpleDateFormat dateFormatDay = new SimpleDateFormat("MMM d", Locale.getDefault());

            if (mLoadType == SELECTION_WORKOUT_HISTORY){
                long startTime = userPrefs.getLastUserOpen();
                if (startTime == 0){
                    startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                    userPrefs.setLastUserOpen(startTime);
                }
                calendar.setTimeInMillis(startTime);

                dateFormatDay.setCalendar(calendar);
                String sTemp = "On" + ATRACKIT_SPACE + dateFormatDay.format(calendar.getTime());
                mShowMode = 1;
                if (sTemp.length() > 0) mList_Title1.setText(sTemp);
                mList_Title1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    getDialog().dismiss();
    //                    mCallback.onCustomItemSelected(mLoadType,  mDay, Constants.ATRACKIT_ATRACKIT_CLASS, mMonth, Integer.toString(mYear));
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDatePickerStyle,(view, selectedYear, monthOfYear, dayOfMonth) -> {
                            long endTime = 0L;
                            calendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                            mDay = dayOfMonth; mMonth = monthOfYear + 1; mYear = selectedYear;
                            long startTime = calendar.getTimeInMillis();
                            userPrefs.setLastUserOpen(startTime);
                            dateFormatDay.setCalendar(calendar);
                            String sTemp = "On" + ATRACKIT_SPACE + dateFormatDay.format(calendar.getTime());
                            if (sTemp.length() > 0 && mList_Title1 != null) mList_Title1.setText(sTemp);
                            sTemp = ATRACKIT_EMPTY + calendar.get(Calendar.YEAR);
                            if (textViewYearSelect != null) textViewYearSelect.setText(sTemp);
                            endTime = startTime + (TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1));
                            List<Workout> routineList = mSessionViewModel.getWorkoutsByTimesNow(sUserID,sDeviceID,startTime, endTime);
                            if ((routineList != null) && (routineList.size() > 0)){
                                workouts = new ArrayList<>();
                                workouts.addAll(routineList);
                                workoutAdapter.clearList();
                                workoutAdapter.setWorkoutArrayList(workouts);
                                workoutAdapter.notifyDataSetChanged();
                            }else{
                                workoutAdapter.clearList();
                            }

                        },year,month,day);
                        datePickerDialog.show();
                    }
                });
            }
            if (mLoadType == SELECTION_WORKOUT_SET_HISTORY){
                if (mWorkout != null)
                    mList_Title1.setText(mWorkout.activityName);
                if (mShowMode == 1) {
                    mList_Title1.setText(getString(R.string.label_session_select_set));
                    mList_Title1.setCompoundDrawablesWithIntrinsicBounds(iconContinue, null,null,null);
                }else
                    mList_Title1.setOnClickListener(v -> CustomListFragment.this.dismiss());
            }
            workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets,bUseKG);
            workoutAdapter.setListType(mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY
                                        || mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS);
            workoutAdapter.setStartMode(mShowMode);

            workoutAdapter.setOnItemClickListener((view, viewModel, startMode, position) -> {
                if (viewModel instanceof  Workout)
                    mCallback.onCustomItemSelected(mLoadType, ((Workout)viewModel)._id, ((Workout)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                if (viewModel instanceof  WorkoutSet)
                    mCallback.onCustomItemSelected(mLoadType, ((WorkoutSet)viewModel)._id, ((WorkoutSet)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                getDialog().dismiss();
            });
            mWearableRecyclerView.setAdapter(workoutAdapter);
        }
        if (mLoadType == Constants.SELECTION_WORKOUT_TEMPLATES){
            workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets,bUseKG);
            workoutAdapter.setListType(true);
            workoutAdapter.setStartMode(mShowMode);
            mList_Title1.setText(getString(R.string.action_open_template));
            workoutAdapter.setOnItemClickListener((view, viewModel, startMode, position) -> {
                if (viewModel instanceof  Workout)
                    mCallback.onCustomItemSelected(mLoadType, ((Workout)viewModel)._id, ((Workout)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                if (viewModel instanceof  WorkoutSet)
                    mCallback.onCustomItemSelected(mLoadType, ((WorkoutSet)viewModel)._id, ((WorkoutSet)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                getDialog().dismiss();
            });
            if (mPreset > 0){
                workoutAdapter.setTargetId(mPreset);
            }
            mWearableRecyclerView.setAdapter(workoutAdapter);
        }
        if (mLoadType == Constants.SELECTION_BODYPART){
            if (bodypartAdapter == null){
                if (bodypartAdapter == null) bodypartAdapter = new BodypartAdapter(mContext);
                bodypartAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.shortName, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                });
            }
            mWearableRecyclerView.setAdapter(bodypartAdapter);
        }
        if (mLoadType == Constants.SELECTION_EXERCISE){
            if (exerciseAdapter == null){
                if (exerciseAdapter == null) exerciseAdapter = new ExerciseAdapter(mContext);
                exerciseAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                });
            }
            mWearableRecyclerView.setAdapter(exerciseAdapter);

        }
/*        if (mLoadType == Constants.SELECTION_USER_PREFS){
            if (userPreferencesAdapter == null){
                userPreferencesAdapter = new UserPreferencesAdapter(mContext, userPrefs.getUserId());
                userPreferencesAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    if (viewModel.value)
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 1, Constants.ATRACKIT_EMPTY);
                    else
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                });
            }
            mWearableRecyclerView.setAdapter(userPreferencesAdapter);
        }*/
        if ((mLoadType == Constants.SELECTION_SETS) || (mLoadType == Constants.SELECTION_REPS)){
              setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_REPS);
              setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                  @Override
                  public void onItemClick(View view, String value, int pos) {
                      mCallback.onCustomItemSelected(mLoadType, Integer.parseInt(value), value, 0, Constants.ATRACKIT_EMPTY);
                      CustomListFragment.this.dismiss();
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_WEIGHT_BODYWEIGHT);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_WEIGHT_LBS)||(mLoadType == Constants.SELECTION_WEIGHT_KG)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_TARGET_FIELD) || (mLoadType == Constants.SELECTION_TARGET_EQUIPMENT) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_INCOMPLETE_DURATION)
                || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_TARGET)  || (mLoadType == Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType == Constants.SELECTION_REST_DURATION_GYM) || (mLoadType == Constants.SELECTION_REST_DURATION_TARGET)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_ENDS) || (mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType, ++pos, value, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        if (mSavedStateViewModel.getActiveWorkout().getValue() != null)
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        else
            mWorkout = new Workout();

        if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null)
            mSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        else
            mSet = new WorkoutSet();

        if ((savedInstanceState != null) && (mLoadType == 0) && (savedInstanceState.containsKey(KEY_FIT_TYPE))) mLoadType = savedInstanceState.getInt(KEY_FIT_TYPE);
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS ||
                mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY || mLoadType == Constants.SELECTION_GOOGLE_HISTORY
                || (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS)){

            if (workoutAdapter == null) {
                workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets,bUseKG);
                workoutAdapter.setListType(mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS);
                workoutAdapter.setOnItemClickListener((view, viewModel, startMode, position) -> {
                    if (viewModel instanceof  Workout)
                        mCallback.onCustomItemSelected(mLoadType, ((Workout)viewModel)._id, ((Workout)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                    if (viewModel instanceof  WorkoutSet)
                        mCallback.onCustomItemSelected(mLoadType, ((WorkoutSet)viewModel)._id, ((WorkoutSet)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                    getDialog().dismiss();
                });
                mWearableRecyclerView.setAdapter(workoutAdapter);
            }
            if ((mLoadType == Constants.SELECTION_ROUTINE) || (mLoadType == Constants.SELECTION_WORKOUT_HISTORY)
                    || (mLoadType == Constants.SELECTION_WORKOUT_TEMPLATES) || (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS)){
                if (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS){
                    ArrayList<Workout> inProg = new ArrayList<>();
                    inProg.addAll(mSessionViewModel.getInProgressWorkouts(sUserID,sDeviceID, Utilities.TimeFrame.BEGINNING_OF_DAY));
                    Log.d(TAG,"In Prog Size " + inProg.size());
                    if (inProg != null) workoutAdapter.setWorkoutArrayList(inProg);
                }else
                if (mSessionViewModel.getAllWorkoutsSize() > 0){
                    List<Workout> routineList = mSessionViewModel.liveAllWorkouts().getValue();
                    if (routineList != null) {
                        workouts = new ArrayList<>();
                        workouts.addAll(routineList);
                        workoutAdapter.setWorkoutArrayList(workouts);
                    }
                }
            }else
            if ( mSavedStateViewModel.getToDoSetsSize() > 0){
                List<WorkoutSet> setList =  mSavedStateViewModel.getToDoSets().getValue();
                if (setList != null) {
                    workoutSets = new ArrayList<>();
                    workoutSets.addAll(setList);
                    workoutAdapter.setWorkoutSetArrayList(workoutSets);
                }
            }
        }

        if (mLoadType == SELECTION_BODY_REGION){
            if ((bodyRegions == null) || (bodyRegions.size() == 0)) {
                List<BodyRegion> list = mSessionViewModel.getRegionList();
                if (list != null) {
                    bodyRegions = new ArrayList<>();
                    bodyRegions.addAll(list);
                }
            }
            regionAdapter.setRegions(bodyRegions);

        }
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)) {
            if ((fitnessActivities == null) || (fitnessActivities.size() == 0)) {
                fitnessActivities = new ArrayList<>();
                List<FitnessActivity> list = mSessionViewModel.getFitnessActivityList();
                if (list != null) {
                    fitnessActivities.addAll(list);
                    activityAdapter.setItems(fitnessActivities);
                }
            }else {
                activityAdapter.setItems(fitnessActivities);
            }
        }

        if (mLoadType == Constants.SELECTION_BODYPART) {
            if ((bodyparts == null) || (bodyparts.size() == 0)) {
                bodyparts = new ArrayList<>();
                List<Bodypart>list = mSessionViewModel.getBodypartList();
                if (list != null) {
                    bodyparts.addAll(list);
                    bodypartAdapter.setItems(bodyparts, mSet.regionID);
                }
            }else {
                bodypartAdapter.setItems(bodyparts, mSet.regionID);
            }
        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((mSet.bodypartID != null) && (mSet.bodypartID > 0))
                exercises = mSessionViewModel.getExercisesByBodypartId(mSet.bodypartID);
            else {
                if ((mSet.regionID != null) && (mSet.regionID > 0))
                    exercises = mSessionViewModel.getExercisesByRegionId(mSet.regionID);
                else {
                    exercises = new ArrayList<>();
                    List<Exercise> list = mSessionViewModel.getExercisesList();
                    list.sort((o1, o2) -> (Long.compare(o1.first_BPID, o2.first_BPID)));
                    if (list != null) exercises.addAll(list);
                }
            }
            exerciseAdapter.setItems(exercises);
        }
        return rootView;
    }

    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Context context = getContext();
        if (context != null) {
            int bgColor = ContextCompat.getColor(context, R.color.ambientBackground);
            int fgColor = ContextCompat.getColor(context, R.color.ambientForeground);
            if ((mLoadType == Constants.SELECTION_DAYS)||(mLoadType == Constants.SELECTION_MONTHS)||(mLoadType == Constants.SELECTION_DOY)) {
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(false);
                textViewYearSelect.setBackgroundColor(bgColor);
                textViewYearSelect.setTextColor(fgColor);
                textViewYearSelect.getPaint().setAntiAlias(false);
                mWearableRecyclerView.setVisibility(View.INVISIBLE);
                mWearableRecyclerView2.setVisibility(View.INVISIBLE);
            }else{
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(false);
                mWearableRecyclerView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void loadDataAndUpdateScreen(){

    }
    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
        Context context = getContext();
        if (context != null) {
            int bgColor = ContextCompat.getColor(context, R.color.secondaryColor);
            int fgColor = ContextCompat.getColor(context,R.color.white);
            if ((mLoadType == Constants.SELECTION_DAYS)||(mLoadType == Constants.SELECTION_MONTHS)||(mLoadType == Constants.SELECTION_DOY)) {
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(true);
                textViewYearSelect.setBackgroundColor(bgColor);
                textViewYearSelect.setTextColor(fgColor);
                textViewYearSelect.getPaint().setAntiAlias(true);
                mWearableRecyclerView.setVisibility(View.VISIBLE);
                mWearableRecyclerView2.setVisibility(View.VISIBLE);
            }else {
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(true);
                mWearableRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if ((mLoadType == Constants.SELECTION_DAYS) || (mLoadType == Constants.SELECTION_MONTHS) || (mLoadType == Constants.SELECTION_DOY)) {
            int iTargetID = calendar.get(Calendar.DAY_OF_MONTH);
            ArrayList<String>list_items = dayAdapter.getItems();
            for (int i = 0; i < list_items.size(); i++) {
                String val = list_items.get(i);
                if (Integer.parseInt(val) == iTargetID) {
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
            iTargetID = calendar.get(Calendar.MONTH)+1;
            list_items = monthAdapter.getItems();
            for (int i = 0; i < list_items.size(); i++) {
                String val = list_items.get(i);
                if (Integer.parseInt(val) == iTargetID) {
                    final int z2 = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView2 != null) {
                                mWearableLinearLayoutManager2.scrollToPosition(z2);
                            }
                        }
                    });
                    break;
                }

            }
        }else {
            if (mLoadType == Constants.SELECTION_BODY_REGION){
                if (regionAdapter == null){
                    regionAdapter = new RegionListAdapter(mContext);
                    regionAdapter.setOnItemClickListener(new RegionListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, BodyRegion viewModel) {
                            mCallback.onCustomItemSelected(mLoadType,viewModel._id,viewModel.regionName,0,null);
                            getDialog().dismiss();
                        }
                    });
                    mWearableRecyclerView.setAdapter(regionAdapter);
                }
            }
            if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                    || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                    || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                    || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                    || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)){
                if (activityAdapter == null) {
                    activityAdapter = new FitnessActivityAdapter(mContext, mLoadType);
                    activityAdapter.setOnItemClickListener((view, viewModel) -> {
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, viewModel.resource_id, viewModel.identifier);
                        getDialog().dismiss();
                    });
                    mWearableRecyclerView.setAdapter(activityAdapter);
                }
            }
            if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS ||
                    mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS ||  mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY
                    || mLoadType == Constants.SELECTION_GOOGLE_HISTORY){
                if (workoutAdapter == null) {
                    workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets,bUseKG);
                    workoutAdapter.setListType(mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS);
                    workoutAdapter.setOnItemClickListener((view, viewModel, startMode, position) -> {
                        if (viewModel instanceof  Workout)
                            mCallback.onCustomItemSelected(mLoadType, ((Workout)viewModel)._id, ((Workout)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                        if (viewModel instanceof  WorkoutSet)
                            mCallback.onCustomItemSelected(mLoadType, ((WorkoutSet)viewModel)._id, ((WorkoutSet)viewModel).activityName, startMode, Constants.ATRACKIT_EMPTY);
                        getDialog().dismiss();
                    });
                    mWearableRecyclerView.setAdapter(workoutAdapter);
                }
            }
            if (mLoadType == Constants.SELECTION_BODYPART){
                if (bodypartAdapter == null){
                    if (bodypartAdapter == null) bodypartAdapter = new BodypartAdapter(mContext);
                    bodypartAdapter.setOnItemClickListener((view, viewModel) -> {
                        //  callback to MainActivity interface
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.shortName, 0, Constants.ATRACKIT_EMPTY);
                        CustomListFragment.this.dismiss();
                    });
                }
                mWearableRecyclerView.setAdapter(bodypartAdapter);
            }
            if (mLoadType == Constants.SELECTION_EXERCISE) {
                if (exerciseAdapter == null) {
                    if (exerciseAdapter == null) {
                        exerciseAdapter = new ExerciseAdapter(mContext);
                        exerciseAdapter.setOnItemClickListener((view, viewModel) -> {
                            //  callback to MainActivity interface
                            mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, 0, Constants.ATRACKIT_EMPTY);
                            CustomListFragment.this.dismiss();
                        });
                    }
                    mWearableRecyclerView.setAdapter(exerciseAdapter);
                }
            }
/*        if (mLoadType == Constants.SELECTION_USER_PREFS){
            if (userPreferencesAdapter == null){
                userPreferencesAdapter = new UserPreferencesAdapter(mContext, userPrefs.getUserId());
                userPreferencesAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    if (viewModel.value)
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 1, Constants.ATRACKIT_EMPTY);
                    else
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 0, Constants.ATRACKIT_EMPTY);
                    CustomListFragment.this.dismiss();
                });
            }
            mWearableRecyclerView.setAdapter(userPreferencesAdapter);
        }*/
        if ((mLoadType == Constants.SELECTION_SETS) || (mLoadType == Constants.SELECTION_REPS)){
            if (setsRepsWeightAdapter == null){
                setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_REPS);
                setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, String value, int pos) {
                        mCallback.onCustomItemSelected(mLoadType, Integer.parseInt(value), value, 0, Constants.ATRACKIT_EMPTY);
                        CustomListFragment.this.dismiss();
                    }
                });
                mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
            }
        }
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            if (setsRepsWeightAdapter == null){
                setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_WEIGHT_BODYWEIGHT);
                setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, String value, int pos) {
                        mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                        CustomListFragment.this.dismiss();
                    }
                });
                mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
            }
        }
        if ((mLoadType == Constants.SELECTION_WEIGHT_LBS)||(mLoadType == Constants.SELECTION_WEIGHT_KG)){
            if (setsRepsWeightAdapter == null){
                setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
                setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, String value, int pos) {
                        mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                        CustomListFragment.this.dismiss();
                    }
                });
                mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_FIELD) || (mLoadType == Constants.SELECTION_TARGET_EQUIPMENT) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_INCOMPLETE_DURATION)
                || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_TARGET)  || (mLoadType == Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType == Constants.SELECTION_REST_DURATION_GYM) || (mLoadType == Constants.SELECTION_REST_DURATION_TARGET)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_ENDS) || (mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE)){
            if (setsRepsWeightAdapter == null){
                setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
                setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, String value, int pos) {
                        mCallback.onCustomItemSelected(mLoadType, ++pos, value, 0, Constants.ATRACKIT_EMPTY);
                        CustomListFragment.this.dismiss();
                    }
                });
                mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
            }
        }
            setStartScrollPos();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        mCallback = null; mContext = null;
        if( context instanceof OnCustomListItemSelectedListener) {
            mCallback = (OnCustomListItemSelectedListener) context;
            mContext = context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mCallback = null;
        mContext = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    private void setStartScrollPos(){
        mWearableRecyclerView.requestFocus();
        mWearableRecyclerView.setSelected(true);
        ArrayList<String> list_items = new ArrayList<>();
        int iTargetID = 0;
        String sLabel = getString(R.string.default_loadtype) + Integer.toString(mLoadType);
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)) {
            if ((mWorkout != null) && (mWorkout.activityID > 0)) {
                sLabel = getString(R.string.default_loadtype) + mWorkout.activityID;
                String sTarget0 = userPrefs.getPrefStringByLabel(sLabel);
                if (sTarget0.length() > 0)
                    iTargetID = Integer.parseInt(sTarget0);
            }else{
                String sTarget = userPrefs.getPrefStringByLabel(sLabel);
                if (sTarget.length() > 0)
                    iTargetID = Integer.parseInt(sTarget);
            }
            int i = 0;

            if (iTargetID > 0) {
                if (activityAdapter == null) {
                    activityAdapter = new FitnessActivityAdapter(mContext, mLoadType);
                    activityAdapter.setItems(fitnessActivities);
                }
                activityAdapter.setTargetId(iTargetID);
                for (FitnessActivity fitnessActivity : activityAdapter.getItems()) {
                    if ((int) fitnessActivity._id == iTargetID) {
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWearableLinearLayoutManager != null) {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            }
                        });
                        break;
                    } else i++;
                }
            }
        }
        if (mLoadType == SELECTION_BODY_REGION) {
            if ((bodyRegions != null) && (mWorkout != null))
                if ((mSet.regionID != null) && (mSet.regionID > 0)) {
                    if (regionAdapter == null) regionAdapter = new RegionListAdapter(getContext());
                    regionAdapter.setRegions(bodyRegions);
                    regionAdapter.setTargetId(mSet.regionID);
                    int i = 0;
                    if (mSet.regionID != null)
                    for (BodyRegion w : bodyRegions) {
                        if (w._id == mSet.regionID) {
                            final int z = i;
                            regionAdapter.setTargetId(mSet.regionID);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWearableRecyclerView != null) {
                                        mWearableLinearLayoutManager.scrollToPosition(z);
                                    }
                                }
                            });
                            break;
                        } else i++;
                    }
                }
        }
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY) {
            if ((workouts != null) && (mWorkout != null))
                if ((mWorkout._id > 0)) {
                    if (workoutAdapter == null) {
                        workoutAdapter = new WorkoutAdapter(mContext, workouts, workoutSets, bUseKG);
                        workoutAdapter.setListType(true);
                    }
                    int i = 0;
                    for (Workout w : workouts) {
                        if (w._id == mWorkout._id) {
                            workoutAdapter.setTargetId(w._id);
                            final int z = i;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWearableRecyclerView != null) {
                                        mWearableLinearLayoutManager.scrollToPosition(z);
                                    }
                                }
                            });
                            break;
                        } else i++;
                    }
                }
        }
        if (mLoadType == Constants.SELECTION_TO_DO_SETS || mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY || mLoadType == Constants.SELECTION_GOOGLE_HISTORY) {
            if ((workoutSets != null) && (mSet != null))
                if ((mSet._id > 0)) {
                    int i = 0;
                    if (workoutAdapter == null) workoutAdapter = new WorkoutAdapter(mContext,workouts,workoutSets,bUseKG);
                    workoutAdapter.setListType(false);
                    workoutAdapter.setTargetId(mSet._id);
                    for (WorkoutSet s : workoutSets) {
                        if (s._id == mSet._id) {
                            final int z = i;
                            workoutAdapter.setTargetId(s._id);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWearableRecyclerView != null) {
                                        mWearableLinearLayoutManager.scrollToPosition(z);
                                    }
                                }
                            });
                            break;
                        } else i++;
                    }
                }
        }
        if (mLoadType == Constants.SELECTION_BODYPART) {
            if ((mSet != null) && (mSet.bodypartID != null) && (mSet.bodypartID > 0)){
                if (bodypartAdapter == null) {
                    bodypartAdapter = new BodypartAdapter(mContext);
                    bodypartAdapter.setItems(bodyparts, mSet.regionID);
                }
                if ((mSet.bodypartID != null) && (mSet.bodypartID > 0)) {
                    bodypartAdapter.setTargetId(mSet.bodypartID);
                    // if (bodypartAdapter.getSelectedPos() > RecyclerView.NO_POSITION) mWearableLinearLayoutManager.scrollToPosition(bodypartAdapter.getSelectedPos());
                    int i = 0;
                    ArrayList<Bodypart> bps = bodypartAdapter.getItems();
                    for (Bodypart bodypart : bps) {
                        if (bodypart._id == mSet.bodypartID) {
                            final int z = i;
                            mWearableRecyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            });
                        } else i++;
                    }
                }
            }

        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((mSet != null) && (mSet.exerciseID != null) && (mSet.exerciseID > 0)){
                if (exerciseAdapter == null) {
                    exerciseAdapter = new ExerciseAdapter(mContext);
                    exerciseAdapter.setItems(exercises);
                }
                int i=0;
                for (Exercise exercise : exerciseAdapter.getItems()){
                    if ((mSet.exerciseID != null) && (exercise._id == mSet.exerciseID)){
                        final int z = i;
                        exerciseAdapter.setTargetId(exercise._id);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWearableRecyclerView != null) {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            }
                        });
                        break;
                    }else i++;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_SETS) || (mLoadType == Constants.SELECTION_REPS)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if ((mLoadType == Constants.SELECTION_REPS) && (Integer.parseInt(val) == mSet.repCount) && (mSet.repCount >0)){
                    final int y = i;
                    setsRepsWeightAdapter.setTargetId(val);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(y);
                            }
                        }
                    });
                    break;
                }
                if ((mLoadType == Constants.SELECTION_SETS) && (Integer.parseInt(val) == mSet.setCount) && (mSet.setCount > 0)){
                    final int z = i;
                    setsRepsWeightAdapter.setTargetId(val);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }

        if ( ((mLoadType == Constants.SELECTION_WEIGHT_KG) || (mLoadType == Constants.SELECTION_WEIGHT_LBS))){
            list_items = setsRepsWeightAdapter.getItems();
            if ((mSet != null) && (mSet.weightTotal > 0)) {
                if (mPreset > 0 && mSet.weightTotal == 0) mSet.weightTotal = (float)mPreset;
                String testValue = Math.round(mSet.weightTotal)+ATRACKIT_EMPTY;
                if (!bUseKG)
                    testValue = Integer.toString(Utilities.KgToPoundsDisplay(mSet.weightTotal));
                for (int i = 0; i < list_items.size(); i++) {
                    String val = list_items.get(i);
                    if (val.equals(testValue)){
                        setsRepsWeightAdapter.setTargetId(val);
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWearableRecyclerView != null) {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            list_items = setsRepsWeightAdapter.getItems();
            if ((mSet != null) && (mSet.weightTotal > 0))
                for (int i=0; i < list_items.size(); i++){
                    String val = list_items.get(i);
                    if (val.contains("bodyweight - ")){
                        if (Float.parseFloat(val.substring(13)) == mSet.weightTotal){
                            final int z = i;
                            setsRepsWeightAdapter.setTargetId(val);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWearableRecyclerView != null) {
                                        mWearableLinearLayoutManager.scrollToPosition(z);
                                    }
                                }
                            });
                            break;
                        }
                    }else
                        if (val.contains("bodyweight + ")){
                            if (Float.parseFloat(val.substring(13)) == mSet.weightTotal){
                                final int z = i;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mWearableRecyclerView != null) {
                                            mWearableLinearLayoutManager.scrollToPosition(z);
                                        }
                                    }
                                });
                                break;
                            }
                        }else
                            if (0 == mSet.weightTotal){
                            final int z = i;
                                setsRepsWeightAdapter.setTargetId("0");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWearableRecyclerView != null) {
                                        mWearableLinearLayoutManager.scrollToPosition(z);
                                    }
                                }
                            });
                            break;
                        }
                }
        }
 /*       if ((mLoadType == Constants.SELECTION_TARGET_FIELD) && (mWorkout != null) && (mWorkout.shootFormatID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkout.shootFormat.equals(val)){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_EQUIPMENT)  && (mWorkout != null) && (mWorkout.equipmentID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkout.equipmentName.equals(val)){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if (((mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD)) && (mWorkout.regionID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkout.regionName.equals(val)){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if (((mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD)) && (mWorkout.targetSizeID> 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkout.targetSizeName.equals(val)){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_ENDS) && (mWorkout.setCount > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkout.setCount){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END) && (mWorkout.repCount > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkout.repCount){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE) && (mWorkout.totalPossible > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkout.totalPossible){
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mWearableRecyclerView != null) {
                                mWearableLinearLayoutManager.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
*/
        if ((mLoadType== Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType== Constants.SELECTION_REST_DURATION_GYM)
                || (mLoadType== Constants.SELECTION_REST_DURATION_TARGET) || (mLoadType == Constants.SELECTION_CALL_DURATION)
                || (mLoadType == Constants.SELECTION_END_DURATION)){
            int target = 0;
            if ((mLoadType== Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType== Constants.SELECTION_REST_DURATION_GYM)){
                target = userPrefs.getWeightsRestDuration();
            }
            if (mLoadType == Constants.SELECTION_REST_DURATION_TARGET) target = userPrefs.getArcheryRestDuration();
            if (mLoadType == Constants.SELECTION_CALL_DURATION) target = userPrefs.getArcheryCallDuration();
            if (mLoadType == Constants.SELECTION_END_DURATION) target = userPrefs.getArcheryEndDuration();
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == target){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (mWearableRecyclerView != null) {
                            mWearableRecyclerView.scrollToPosition(z);
                        }
                    });
                    break;
                }
            }
        }
    }

    private class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
        private static final float MAX_ICON_PROGRESS = 0.65f;
        private float mProgressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);
            if (!Float.isNaN(mProgressToCenter)) {
                child.setScaleX(1 - mProgressToCenter);
                child.setScaleY(1 - mProgressToCenter);
            }
        }

    }
}
