package com.a_track_it.workout.fragment;

import android.content.Context;
import android.os.AsyncTask;
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
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.a_track_it.workout.common.data_model.WorkoutMeta;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.SELECTION_BODY_REGION;
import static com.a_track_it.workout.common.Constants.USER_PREF_SHOOT_CALL_DURATION;
import static com.a_track_it.workout.common.Constants.USER_PREF_SHOOT_END_DURATION;
import static com.a_track_it.workout.common.Constants.USER_PREF_SHOOT_REST_DURATION;

public class CustomListDialogFragment extends DialogFragment {
    private TextView mList_Title1;
  //  private TextView mList_Title2;
    private MaterialButton mDateButton;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;

    public static final String TAG = "CustomListDialogFragment";
    private WorkoutViewModel mSessionViewModel;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private DayMonthAdapter dayAdapter;
    private DayMonthAdapter monthAdapter;
    private WorkoutAdapter workoutAdapter;
    private RegionListAdapter regionAdapter;
    private BodyRegion_AsyncListViewLoader bodyRegion_asyncListViewLoader;
    private FitnessActivityAdapter activityAdapter;
    private FitnessActivity_AsyncListViewLoader fitness_activityListViewLoader;
    private BodypartAdapter bodypartAdapter;
    private Bodypart_AsyncListViewLoader bodypart_asyncListViewLoader;
    private ExerciseAdapter exerciseAdapter;
    //private UserPreferencesAdapter userPreferencesAdapter;
    private SetsRepsWeightAdapter setsRepsWeightAdapter;
    private Exercise_AsyncListViewLoader exercise_asyncListViewLoader;
    private ArrayList<BodyRegion> bodyRegions;
    private ArrayList<FitnessActivity> fitnessActivities;
    private ArrayList<Bodypart> bodyparts;
    private ArrayList<Exercise> exercises;
    private ArrayList<Workout> workouts;
    private ArrayList<WorkoutSet> workoutSets;
    private Context mContext;
    private int mLoadType;
    private int mSetIndex;
    private long mPreset;
    private boolean externalLoad = false;
    private Workout mWorkout;
    private WorkoutSet mSet;
    private WorkoutMeta mWorkoutMeta;
    private Calendar calendar;
    private String sUserID;
    private String sDeviceID;
    private String sTitle;
    private boolean bUseKG = false;
    private int mDay;
    private int mMonth;
    private MutableLiveData<List<Integer>> mDateLiveData = new MutableLiveData<>();
    public CustomListDialogFragment() {
        // Required empty public constructor
    }
    public static CustomListDialogFragment create(int iType, long iPreset, String sTitle, String sUser, String sDevice) {
        final CustomListDialogFragment fragment = new CustomListDialogFragment();
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
    public void setListItemSelectedListener(OnCustomListItemSelectedListener callback){
        mCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPrefs = ApplicationPreferences.getPreferences(requireContext());
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
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        Integer[] dayMonth = {mDay, mMonth};
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
        bUseKG = userPrefs.getUseKG();
        if ((mLoadType == Constants.SELECTION_DAYS) || (mLoadType == Constants.SELECTION_MONTHS)) {
            if(Locale.getDefault().equals(Locale.US))
                rootView = inflater.inflate(R.layout.dialog_custom_date_us_entry, container, false);
            else
                rootView = inflater.inflate(R.layout.dialog_custom_date_entry, container, false);
            mDateButton = rootView.findViewById(R.id.btnDateSelect);
            mDateButton.setOnClickListener(v -> {
                mCallback.onCustomItemSelected(mLoadType,  mDay, null, mMonth, null);
                getDialog().dismiss();
            });
            mRecyclerView = rootView.findViewById(R.id.day_list);
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView

            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(mContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);
            dayAdapter = new DayMonthAdapter(mContext, Constants.SELECTION_DAYS);
            mRecyclerView.setAdapter(dayAdapter);
            mRecyclerView.setVisibility(View.VISIBLE);
            dayAdapter.setOnItemClickListener((view, value, pos) -> {
                Log.d(TAG, "DAY Set value " + value);
                mDay = Integer.parseInt(value);
                Integer[] dayMonth = {mDay, mMonth};
                mDateLiveData.postValue(Arrays.asList(dayMonth));
            });

            mRecyclerView2 = rootView.findViewById(R.id.month_list);
            LinearLayoutManager llm2 = new LinearLayoutManager(mContext);
            llm2.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView2.setLayoutManager(llm2);
            mRecyclerView2.setHasFixedSize(true);
            monthAdapter = new DayMonthAdapter(mContext, Constants.SELECTION_MONTHS);
            mRecyclerView2.setAdapter(monthAdapter);
            mRecyclerView2.setVisibility(View.VISIBLE);
            monthAdapter.setOnItemClickListener((view, value, pos) -> {
                Log.d(TAG, "MONTH Set value " + value);
                mMonth = Integer.parseInt(value);
                Integer[] dayMonth = {mDay, mMonth};
                mDateLiveData.postValue(Arrays.asList(dayMonth));
            });
            mDateLiveData.observe(getViewLifecycleOwner(), dayMonth -> {
                int day = dayMonth.get(0);
                int month = dayMonth.get(1)-1;
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, day);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String title = Utilities.getDateString2(calendar.getTimeInMillis());
                        mDateButton.setText(title);
                    }
                });
            });
        }
        else {
            rootView = inflater.inflate(R.layout.dialog_customlist, container, false);
            mList_Title1 = rootView.findViewById(R.id.activity_title);
            if (sTitle == null || sTitle.length() == 0)
                sTitle = Utilities.SelectionTypeToString(getActivity(), mLoadType);
            if (sTitle.length() > 0) mList_Title1.setText(sTitle);
            mList_Title1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });
            mRecyclerView = rootView.findViewById(R.id.activity_list);
            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(mContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);
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
            mRecyclerView.setAdapter(regionAdapter);
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
            mRecyclerView.setAdapter(activityAdapter);
        }
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS
            ||  mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS ||  mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY || mLoadType == Constants.SELECTION_GOOGLE_HISTORY){
            if (workoutAdapter == null) workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets,bUseKG);
            workoutAdapter.setListType(mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS);
            workoutAdapter.setOnItemClickListener((view, viewModel, position) -> {
                if (viewModel instanceof  Workout) {
                    Workout w = (Workout)viewModel;
                    if (w != null)
                        mCallback.onCustomItemSelected(mLoadType, w._id, w.activityName, 0, Constants.ATRACKIT_EMPTY);
                }else{
                    WorkoutSet s = (WorkoutSet)viewModel;
                    if (s != null)
                        mCallback.onCustomItemSelected(mLoadType, s._id, s.exerciseName, 0, Constants.ATRACKIT_EMPTY);
                }
                getDialog().dismiss();
            });
            mRecyclerView.setAdapter(workoutAdapter);
        }
        if (mLoadType == Constants.SELECTION_BODYPART){
            if (bodypartAdapter == null){
                if (bodypartAdapter == null) bodypartAdapter = new BodypartAdapter(mContext);
                bodypartAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.shortName, 0, Constants.ATRACKIT_EMPTY);
                    CustomListDialogFragment.this.dismiss();
                });
            }
            mRecyclerView.setAdapter(bodypartAdapter);
        }
        if (mLoadType == Constants.SELECTION_EXERCISE){
            if (exerciseAdapter == null){
                if (exerciseAdapter == null) exerciseAdapter = new ExerciseAdapter(mContext,0);
                exerciseAdapter.setOnItemClickListener((view, viewModel) -> {
                    //  callback to MainActivity interface
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, 0, Constants.ATRACKIT_EMPTY);
                    CustomListDialogFragment.this.dismiss();
                });
            }
            mRecyclerView.setAdapter(exerciseAdapter);
        }
        if ((mLoadType == Constants.SELECTION_SETS) || (mLoadType == Constants.SELECTION_REPS)){
              setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_REPS);
              setsRepsWeightAdapter.setOnItemClickListener((view, value, pos) -> {
                  mCallback.onCustomItemSelected(mLoadType, Integer.parseInt(value), value, 0, Constants.ATRACKIT_EMPTY);
                  CustomListDialogFragment.this.dismiss();
            });
            mRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_WEIGHT_BODYWEIGHT);
            setsRepsWeightAdapter.setOnItemClickListener((view, value, pos) -> {
                mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                CustomListDialogFragment.this.dismiss();
            });
            mRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_WEIGHT_LBS)||(mLoadType == Constants.SELECTION_WEIGHT_KG)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener((view, value, pos) -> {
                mCallback.onCustomItemSelected(mLoadType,++pos, value, 0, Constants.ATRACKIT_EMPTY);
                CustomListDialogFragment.this.dismiss();
            });
            mRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_TARGET_FIELD) || (mLoadType == Constants.SELECTION_TARGET_EQUIPMENT) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_INCOMPLETE_DURATION)
                || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_TARGET)  || (mLoadType == Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType == Constants.SELECTION_REST_DURATION_GYM)
                || (mLoadType == Constants.SELECTION_REST_DURATION_TARGET) || (mLoadType == Constants.SELECTION_CALL_DURATION) || (mLoadType == Constants.SELECTION_END_DURATION)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_ENDS) || (mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener((view, value, pos) -> {
                mCallback.onCustomItemSelected(mLoadType, ++pos, value, 0, Constants.ATRACKIT_EMPTY);
                CustomListDialogFragment.this.dismiss();
            });
            mRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        SavedStateViewModel mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        if (sDeviceID == null || sDeviceID.length() == 0) sDeviceID = mSavedStateViewModel.getDeviceID();
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        if (mSavedStateViewModel.getActiveWorkout().getValue() != null)
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        else
            mWorkout = new Workout();

        if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null)
            mSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        else
            mSet = new WorkoutSet(mWorkout);
        if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() != null)
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
        else
            mWorkoutMeta = new WorkoutMeta();

        if ((savedInstanceState != null) && (mLoadType == 0) && (savedInstanceState.containsKey(KEY_FIT_TYPE))) mLoadType = savedInstanceState.getInt(KEY_FIT_TYPE);
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS ||
                mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY || mLoadType == Constants.SELECTION_GOOGLE_HISTORY
                || (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS)){

            if (workoutAdapter == null) workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets, bUseKG);
            if ((mLoadType == Constants.SELECTION_ROUTINE) || (mLoadType == Constants.SELECTION_WORKOUT_HISTORY) || (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS)){
                if (mLoadType == Constants.SELECTION_WORKOUT_INPROGRESS){
                    ArrayList<Workout> inProg = new ArrayList<>();
                    inProg.addAll(mSessionViewModel.getInProgressWorkouts(sUserID, sDeviceID, Utilities.TimeFrame.BEGINNING_OF_DAY));
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
            if (mSavedStateViewModel.getToDoSetsSize() > 0){
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
    public void onStart() {
        super.onStart();
        if ((mLoadType == Constants.SELECTION_DAYS) || (mLoadType == Constants.SELECTION_MONTHS)) {
            int iTargetID = calendar.get(Calendar.DAY_OF_MONTH);
            ArrayList<String>list_items = dayAdapter.getItems();
            for (int i = 0; i < list_items.size(); i++) {
                String val = list_items.get(i);
                if (Integer.parseInt(val) == iTargetID) {
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
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
                            if (mRecyclerView2 != null) {
                                mRecyclerView2.scrollToPosition(z2);
                            }
                        }
                    });
                    break;
                }

            }
        }else
            setStartScrollPos();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

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
        if (((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)) && !externalLoad)  fitness_activityListViewLoader = null;
        if (mLoadType == Constants.SELECTION_BODYPART && !externalLoad) bodypart_asyncListViewLoader = null;
        if (mLoadType == Constants.SELECTION_EXERCISE && !externalLoad) exercise_asyncListViewLoader = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    private void setStartScrollPos(){
        mRecyclerView.requestFocus();
        mRecyclerView.setSelected(true);
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
                                if (mRecyclerView != null) {
                                    mRecyclerView.scrollToPosition(z);
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
                        if  (w._id == mSet.regionID){
                            final int z = i;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mRecyclerView2 != null) {
                                        mRecyclerView2.scrollToPosition(z);
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
                    workoutAdapter.setTargetId(mWorkout._id);
                    int i = 0;
                    for (Workout w : workouts) {
                        if (w._id == mWorkout._id) {
                            final int z = i;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mRecyclerView != null) {
                                        mRecyclerView.scrollToPosition(z);
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
                    if (workoutAdapter == null) workoutAdapter = new WorkoutAdapter(mContext,workouts,workoutSets,bUseKG);
                    workoutAdapter.setListType(false);
                    workoutAdapter.setTargetId(mSet._id);
                    int i = 0;
                    for (WorkoutSet s : workoutSets) {
                        if (s._id == mSet._id) {
                            final int z = i;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mRecyclerView != null) {
                                        mRecyclerView.scrollToPosition(z);
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
                int i=0;
                if (bodypartAdapter == null) {
                    bodypartAdapter = new BodypartAdapter(mContext);
                    bodypartAdapter.setItems(bodyparts, mSet.regionID);
                }
                if ((mSet.bodypartID != null) && (mSet.bodypartID > 0)){
                    bodypartAdapter.setTargetId(mSet.bodypartID);
                    if (bodypartAdapter.getSelectedPos() > RecyclerView.NO_POSITION) mRecyclerView.getLayoutManager().scrollToPosition(bodypartAdapter.getSelectedPos());
                }
            }

        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((mSet != null)  && (mSet.exerciseID != null) && (mSet.exerciseID > 0)){
                if (exerciseAdapter == null) {
                    exerciseAdapter = new ExerciseAdapter(mContext,0);
                    exerciseAdapter.setItems(exercises);
                }
                exerciseAdapter.setTargetId(mSet.exerciseID);
                int i=0;
                if (mSet.exerciseID != null)
                for (Exercise exercise : exerciseAdapter.getItems()){
                    if (exercise._id == mSet.exerciseID){
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        });
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
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (mRecyclerView != null) {
                            mRecyclerView.scrollToPosition(y);
                        }
                    });
                    break;
                }
                if ((mLoadType == Constants.SELECTION_SETS) && (mSet.setCount > 0) && (Integer.parseInt(val) == mSet.setCount)){
                    final int z = i;
                    setsRepsWeightAdapter.setTargetId(val);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (mRecyclerView != null) {
                            mRecyclerView.scrollToPosition(z);
                        }
                    });
                    break;
                }
            }
        }

        if (((mLoadType == Constants.SELECTION_WEIGHT_KG) || (mLoadType == Constants.SELECTION_WEIGHT_LBS))){
            list_items = setsRepsWeightAdapter.getItems();
            if ((mSet != null) && (mSet.weightTotal > 0)) {
                String testValue = ATRACKIT_EMPTY;
                if (mPreset > 0 && mSet.weightTotal == 0) mSet.weightTotal = (float)mPreset;
                testValue = Math.round(mSet.weightTotal)+ATRACKIT_EMPTY;
                if (!bUseKG)
                    testValue = Integer.toString(Utilities.KgToPoundsDisplay(mSet.weightTotal));
                for (int i = 0; i < list_items.size(); i++) {
                    String val = list_items.get(i);
                    if (val.equals(testValue)) {
                        setsRepsWeightAdapter.setTargetId(val);
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
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
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (mRecyclerView != null) {
                                    mRecyclerView.scrollToPosition(z);
                                }
                            });
                            break;
                        }
                    }else
                        if (val.contains("bodyweight + ")){
                            if (Float.parseFloat(val.substring(13)) == mSet.weightTotal){
                                final int z = i;
                                setsRepsWeightAdapter.setTargetId(val);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    if (mRecyclerView != null) {
                                        mRecyclerView.scrollToPosition(z);
                                    }
                                });
                                break;
                            }
                        }else
                            if (0 == mSet.weightTotal){
                            final int z = i;
                            setsRepsWeightAdapter.setTargetId("0");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (mRecyclerView != null) {
                                    mRecyclerView.scrollToPosition(z);
                                }
                            });
                            break;
                        }
                }
        }
      if ((mLoadType == Constants.SELECTION_TARGET_FIELD) && (mWorkoutMeta != null) && (mWorkoutMeta.shootFormatID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkoutMeta.shootFormat.equals(val)){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_EQUIPMENT)  && (mWorkoutMeta != null) && (mWorkoutMeta.equipmentID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkoutMeta.equipmentName.equals(val)){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if (((mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD)) && (mWorkoutMeta.distanceID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkoutMeta.distanceName.equals(val)){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if (((mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD)) && (mWorkoutMeta.targetSizeID> 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkoutMeta.targetSizeName.equals(val)){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_ENDS) && (mWorkoutMeta.setCount > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkoutMeta.setCount){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END) && (mWorkoutMeta.repCount > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkoutMeta.repCount){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
        if ((mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE) && (mWorkoutMeta.totalPossible > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == mWorkoutMeta.totalPossible){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecyclerView != null) {
                                mRecyclerView.scrollToPosition(z);
                            }
                        }
                    });
                    break;
                }
            }
        }
       if ((mLoadType== Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType== Constants.SELECTION_REST_DURATION_GYM)
               || (mLoadType== Constants.SELECTION_REST_DURATION_TARGET) || (mLoadType == Constants.SELECTION_CALL_DURATION)
                || (mLoadType == Constants.SELECTION_END_DURATION)){
            int target = 0;
            if ((mLoadType== Constants.SELECTION_REST_DURATION_SETTINGS) || (mLoadType== Constants.SELECTION_REST_DURATION_GYM))
                target = userPrefs.getWeightsRestDuration();
            if (mLoadType == Constants.SELECTION_REST_DURATION_TARGET) target = Math.toIntExact(userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_REST_DURATION));
            if (mLoadType == Constants.SELECTION_CALL_DURATION) target = Math.toIntExact(userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_CALL_DURATION));
            if (mLoadType == Constants.SELECTION_END_DURATION) target = Math.toIntExact(userPrefs.getLongPrefByLabel(USER_PREF_SHOOT_END_DURATION));
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == target){
                    setsRepsWeightAdapter.setTargetId(val);
                    final int z = i;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (mRecyclerView != null) {
                            mRecyclerView.scrollToPosition(z);
                        }
                    });
                    break;
                }
            }
        }
    }
    private class BodyRegion_AsyncListViewLoader extends AsyncTask<String, Void, ArrayList<BodyRegion>> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(ArrayList<BodyRegion> result) {
            super.onPostExecute(result);
            if (result != null) {
                regionAdapter.setRegions(result);
                regionAdapter.notifyDataSetChanged();
                setStartScrollPos();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<BodyRegion> doInBackground(String... params) {
            ArrayList<BodyRegion> result = new ArrayList<BodyRegion>();
            try {
                if ((bodyRegions == null) || (bodyRegions.size() == 0)){
                    bodyRegions = new ArrayList<>(mSessionViewModel.getRegionList());
                    result = bodyRegions;
                }else
                    result = bodyRegions;
                return result;
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }
    private class FitnessActivity_AsyncListViewLoader extends AsyncTask<String, Void, ArrayList<FitnessActivity>> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(ArrayList<FitnessActivity> result) {
            super.onPostExecute(result);
            if (result != null) {
                activityAdapter.setItems(result);
                activityAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<FitnessActivity> doInBackground(String... params) {
            ArrayList<FitnessActivity> result = new ArrayList<FitnessActivity>();
            try {
                if ((fitnessActivities == null) || (fitnessActivities.size() == 0)) {
                    if (mSessionViewModel.getFitnessActivityList() != null) {
                        fitnessActivities = new ArrayList<>(mSessionViewModel.getFitnessActivityList());
                        result = fitnessActivities;
                    }
                }else
                    result = fitnessActivities;
                return result;
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }
    private class Bodypart_AsyncListViewLoader extends AsyncTask<String, Void, ArrayList<Bodypart>> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(ArrayList<Bodypart> result) {
            super.onPostExecute(result);
            if (result != null) {
                bodypartAdapter.setItems(result, mSet.regionID);
                bodypartAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bodypartAdapter.clearList();
        }

        @Override
        protected ArrayList<Bodypart> doInBackground(String... params) {
            if ((bodyparts == null) || (bodyparts.size() == 0)){
                List<Bodypart> bpList = mSessionViewModel.getBodypartList();
                ArrayList<Bodypart> result = new ArrayList<>(bpList);
                return result;
            }
            else return bodyparts;
        }

    }
    private class Exercise_AsyncListViewLoader extends AsyncTask<Long, Void, ArrayList<Exercise>> {
         @Override
        protected void onPostExecute(ArrayList<Exercise> result) {
            super.onPostExecute(result);
            if (result != null) {
                exerciseAdapter.setItems(result);
                exerciseAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            exerciseAdapter.clearList();
        }

        @Override
        protected ArrayList<Exercise> doInBackground(Long... params) {
            if ((exercises == null) || (exercises.size() == 0)){
                List<Exercise> list = mSessionViewModel.getExercisesList();
                if (list != null) {
                    ArrayList<Exercise> result = new ArrayList<Exercise>(list);
                    return result;
                } else return null;
            }else
                return exercises;
        }

    }

}
