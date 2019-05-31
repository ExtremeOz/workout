package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.res.Resources;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.BodypartAdapter;
import com.a_track_it.fitdata.adapter.DayMonthAdapter;
import com.a_track_it.fitdata.adapter.ExerciseAdapter;
import com.a_track_it.fitdata.adapter.FitnessActivityAdapter;
import com.a_track_it.fitdata.adapter.SetsRepsWeightAdapter;
import com.a_track_it.fitdata.adapter.UserPreferencesAdapter;
import com.a_track_it.fitdata.adapter.WorkoutAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Bodypart;
import com.a_track_it.fitdata.common.model.Exercise;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;
import com.a_track_it.fitdata.model.UserPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class CustomListFragment extends DialogFragment {
    protected TextView mList_Title1;
    protected TextView mList_Title2;
    protected androidx.wear.widget.WearableRecyclerView mWearableRecyclerView;
    protected androidx.wear.widget.WearableRecyclerView mWearableRecyclerView2;
    protected WearableLinearLayoutManager mWearableLinearLayoutManager;
    protected WearableLinearLayoutManager mWearableLinearLayoutManager2;
    public static final String TAG = "CustomListFragment";
    public static final String ARG_TYPE = "ARG_TYPE";
    public static final String ARG_SET = "ARG_SET";
    public static final String ARG_PRESET = "ARG_PRESET";
    private DayMonthAdapter dayAdapter;
    private DayMonthAdapter monthAdapter;
    private WorkoutAdapter workoutAdapter;
    private FitnessActivityAdapter activityAdapter;
    private FitnessActivity_AsyncListViewLoader fitness_activityListViewLoader;
    private BodypartAdapter bodypartAdapter;
    private Bodypart_AsyncListViewLoader bodypart_asyncListViewLoader;
    private ExerciseAdapter exerciseAdapter;
    private UserPreferencesAdapter userPreferencesAdapter;
    private SetsRepsWeightAdapter setsRepsWeightAdapter;
    private Exercise_AsyncListViewLoader exercise_asyncListViewLoader;
    private ArrayList<FitnessActivity> fitnessActivities;
    private ArrayList<Bodypart> bodyparts;
    private ArrayList<Exercise> exercises;
    private ArrayList<Workout> workouts;
    private ArrayList<WorkoutSet> workoutSets;
    private Context mContext;
    private int mLoadType;
    private int mSetIndex;
    private int mPreset;
    private boolean externalLoad = false;
    private Workout mWorkout;
    private WorkoutSet mSet;
    private Calendar calendar;
    private int mDay;
    private int mMonth;

    public CustomListFragment() {
        // Required empty public constructor
    }
    public static CustomListFragment create(int iType, int iSetIndex, int iPreset) {
        final CustomListFragment fragment = new CustomListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, iType);
        args.putInt(ARG_SET, iSetIndex);
        args.putInt(ARG_PRESET, iPreset);
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
        void onCustomItemSelected(int type, long position, String title, int resid, int set_index, String identifer);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadType = getArguments().getInt(ARG_TYPE);
            mSetIndex = getArguments().getInt(ARG_SET);
            mPreset = getArguments().getInt(ARG_PRESET);
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_TYPE))) {
                mLoadType = savedInstanceState.getInt(ARG_TYPE);
                mSetIndex = savedInstanceState.getInt(ARG_SET);
                mPreset = savedInstanceState.getInt(ARG_PRESET);
            }
        }
        calendar = Calendar.getInstance(TimeZone.getDefault());
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH) + 1;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_TYPE, mLoadType);
        outState.putInt(ARG_SET, mSetIndex);
        outState.putInt(ARG_PRESET, mPreset);
        super.onSaveInstanceState(outState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View rootView;
        if (mContext == null) mContext = getContext();
        if ((savedInstanceState != null) && (mLoadType == 0) && (savedInstanceState.containsKey(ARG_TYPE)))
            mLoadType = savedInstanceState.getInt(ARG_TYPE);

        if ((mLoadType == Constants.SELECTION_DAYS) || (mLoadType == Constants.SELECTION_DAYS)) {
            rootView = inflater.inflate(R.layout.fragment_custom_date_entry, container, false);
            mList_Title1 = rootView.findViewById(R.id.day_title);
            mList_Title1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onCustomItemSelected(mLoadType, mSetIndex, null, mDay, mMonth, null);
                }
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
            dayAdapter.setOnItemClickListener(new DayMonthAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    Log.d(TAG, "DAY Set value " + value);
                    mDay = Integer.parseInt(value);
                }
            });
            Log.d(TAG, "Day size " + Integer.toString(dayAdapter.getItemCount()));

            mList_Title2 = rootView.findViewById(R.id.month_title);
            mList_Title2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onCustomItemSelected(mLoadType, mSetIndex, null, mDay, mMonth, null);
                }
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
            monthAdapter.setOnItemClickListener(new DayMonthAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    Log.d(TAG, "MONTH Set value " + value);
                    mMonth = Integer.parseInt(value);
                }
            });
            Log.d(TAG, "Month size " + Integer.toString(monthAdapter.getItemCount()));
        }else {
            rootView = inflater.inflate(R.layout.fragment_customlist, container, false);
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
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)){
          //  if (activityAdapter == null) {
                activityAdapter = new FitnessActivityAdapter(mContext, mLoadType);
                if (activityAdapter.getItemCount() == 0) activityAdapter.setItems(fitnessActivities);
                activityAdapter.setOnItemClickListener(new FitnessActivityAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, FitnessActivity viewModel) {
                        //  callback to MainActivity interface
                        Log.d(TAG, "OnCustomItemSelected " + viewModel.toString());
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, viewModel.resource_id, viewModel.color, viewModel.identifier);
                    }
                });
         //   }
            mWearableRecyclerView.setAdapter(activityAdapter);
        }
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY || mLoadType == Constants.SELECTION_TO_DO_SETS ||
                mLoadType == Constants.SELECTION_WORKOUT_SET_HISTORY || mLoadType == Constants.SELECTION_GOOGLE_HISTORY){
            workoutAdapter = new WorkoutAdapter(mContext,workouts, workoutSets, UserPreferences.getUseKG(mContext));
            workoutAdapter.setListType(mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY);
            workoutAdapter.setOnItemClickListener(new WorkoutAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Workout viewModel) {
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.activityName, 0,mSetIndex, "");
                }
            });
            mWearableRecyclerView.setAdapter(workoutAdapter);
        }
        if (mLoadType == Constants.SELECTION_BODYPART){
            if (bodypartAdapter == null){
                bodypartAdapter = new BodypartAdapter(mContext);
                bodypartAdapter.setOnItemClickListener(new BodypartAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, Bodypart viewModel) {
                        //  callback to MainActivity interface
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.shortName, 0,mSetIndex, "");
                    }
                });
            }
            mWearableRecyclerView.setAdapter(bodypartAdapter);
        }
        if (mLoadType == Constants.SELECTION_EXERCISE){
            if (exerciseAdapter == null){
                exerciseAdapter = new ExerciseAdapter(mContext);
                exerciseAdapter.setOnItemClickListener(new ExerciseAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, Exercise viewModel) {
                        //  callback to MainActivity interface
                        mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, 0,mSetIndex, "");
                    }
                });
            }
            mWearableRecyclerView.setAdapter(exerciseAdapter);

        }
        if (mLoadType == Constants.SELECTION_USER_PREFS){
            if (userPreferencesAdapter == null){
                userPreferencesAdapter = new UserPreferencesAdapter(mContext);
                userPreferencesAdapter.setOnItemClickListener(new UserPreferencesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, UserPreferencesAdapter.UserPreferenceItem viewModel) {
                        //  callback to MainActivity interface
                        if (viewModel.value)
                            mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 1,0, "");
                        else
                            mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.label, 0,0, "");
                    }
                });
            }
            mWearableRecyclerView.setAdapter(userPreferencesAdapter);
        }
        if ((mLoadType == Constants.SELECTION_SETS) || (mLoadType == Constants.SELECTION_REPS)){
              setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_REPS);
              setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                  @Override
                  public void onItemClick(View view, String value, int pos) {
                      mCallback.onCustomItemSelected(mLoadType, Integer.parseInt(value), value, 0,mSetIndex, "");
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, Constants.SELECTION_WEIGHT_BODYWEIGHT);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType,++pos, value, 0,mSetIndex, "");
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_WEIGHT_LBS)||(mLoadType == Constants.SELECTION_WEIGHT_KG)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType,++pos, value, 0,mSetIndex, "");
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }
        if ((mLoadType == Constants.SELECTION_TARGET_FIELD) || (mLoadType == Constants.SELECTION_TARGET_EQUIPMENT) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD)
                || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_TARGET)  || (mLoadType == Constants.SELECTION_REST_DURATION_GYM) || (mLoadType == Constants.SELECTION_REST_DURATION_TARGET)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_ENDS) || (mLoadType == Constants.SELECTION_TARGET_SHOTS_PER_END)
                || (mLoadType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) || (mLoadType == Constants.SELECTION_TARGET_POSSIBLE_SCORE)){
            setsRepsWeightAdapter = new SetsRepsWeightAdapter(mContext, mLoadType);
            setsRepsWeightAdapter.setOnItemClickListener(new SetsRepsWeightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String value, int pos) {
                    mCallback.onCustomItemSelected(mLoadType, ++pos, value, 0,mSetIndex, "");
                }
            });
            mWearableRecyclerView.setAdapter(setsRepsWeightAdapter);
        }

        return rootView;
    }

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        if (getActivity() != null) {
            Resources resources = getActivity().getResources();
            int bgColor = resources.getColor(R.color.colorAmbientBackground, null);
            int fgColor = resources.getColor(R.color.colorAmbientForeground, null);
            if ((mLoadType == Constants.SELECTION_DAYS)||(mLoadType == Constants.SELECTION_MONTHS)) {
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(false);
                mList_Title2.setBackgroundColor(bgColor);
                mList_Title2.setTextColor(fgColor);
                mList_Title2.getPaint().setAntiAlias(false);
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

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        if (getActivity() != null) {
            Resources resources = getActivity().getResources();
            int bgColor = resources.getColor(R.color.colorAccent, null);
            int fgColor = resources.getColor(R.color.white, null);
            if ((mLoadType == Constants.SELECTION_DAYS)||(mLoadType == Constants.SELECTION_MONTHS)) {
                mList_Title1.setBackgroundColor(bgColor);
                mList_Title1.setTextColor(fgColor);
                mList_Title1.getPaint().setAntiAlias(true);
                mList_Title2.setBackgroundColor(bgColor);
                mList_Title2.setTextColor(fgColor);
                mList_Title2.getPaint().setAntiAlias(true);
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
        }else
            setStartScrollPos();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)) {
            if ((fitnessActivities == null) || (fitnessActivities.size() == 0)) {
                if (fitness_activityListViewLoader == null)
                    fitness_activityListViewLoader = new FitnessActivity_AsyncListViewLoader();
                    fitness_activityListViewLoader.execute();
            }else {
                Log.d(TAG, "onActivityCreated set items " + Integer.toString(fitnessActivities.size()));
                activityAdapter.setItems(fitnessActivities);
            }
        }

        if (mLoadType == Constants.SELECTION_BODYPART) {
            if ((bodyparts == null) || (bodyparts.size() == 0)) {
                if (bodypart_asyncListViewLoader == null)
                    bodypart_asyncListViewLoader = new Bodypart_AsyncListViewLoader();
                else Log.i(TAG, "onAttach bodypart_asyncListViewLoader was init");
                Log.i(TAG, "onAttach execute");
                bodypart_asyncListViewLoader.execute();
            }else {
                bodypartAdapter.setItems(bodyparts);
            }
        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((exercises == null) || (exercises.size() == 0)) {
                if (exercise_asyncListViewLoader == null)
                    exercise_asyncListViewLoader = new Exercise_AsyncListViewLoader();
                    exercise_asyncListViewLoader.execute();
            }else {
                    exerciseAdapter.setItems(exercises);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        mCallback = null; mContext = null;
        if( context instanceof OnCustomListItemSelectedListener) {
            mCallback = (OnCustomListItemSelectedListener) context;
            mContext = context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");
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
        mWearableRecyclerView.requestFocus();
        mWearableRecyclerView.setSelected(true);
        ArrayList<String> list_items = new ArrayList<>();
        int iTargetID = 0;
        if ((mLoadType == Constants.SELECTION_FITNESS_ACTIVITY) || (mLoadType == Constants.SELECTION_ACTIVITY_BIKE)
                || (mLoadType == Constants.SELECTION_ACTIVITY_GYM) || (mLoadType == Constants.SELECTION_ACTIVITY_CARDIO)
                || (mLoadType == Constants.SELECTION_ACTIVITY_SPORT) || (mLoadType == Constants.SELECTION_ACTIVITY_RUN)
                || (mLoadType == Constants.SELECTION_ACTIVITY_WATER) || (mLoadType == Constants.SELECTION_ACTIVITY_WINTER)
                || (mLoadType == Constants.SELECTION_ACTIVITY_MISC)) {
            if (mWorkout.activityID > 0)
                iTargetID = mWorkout.activityID;
            else{
                String sLabel = getString(R.string.default_loadtype) + Integer.toString(mLoadType);
                String sTarget = UserPreferences.getPrefStringByLabel(mContext,sLabel);
                if (sTarget.length() > 0)
                    iTargetID = Integer.parseInt(sTarget);
            }
            int i = 0;

            if (iTargetID > 0)
                for(FitnessActivity fitnessActivity : activityAdapter.getItems()){
                    if ((int)fitnessActivity._id == iTargetID){
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
                    }else i++;
                }

        }
        if (mLoadType == Constants.SELECTION_ROUTINE || mLoadType == Constants.SELECTION_WORKOUT_HISTORY) {
            if ((workouts != null) && (mWorkout != null))
                if ((mWorkout._id > 0)) {
                    int i = 0;
                    for (Workout w : workouts) {
                        if (w._id == mWorkout._id) {
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
                    for (WorkoutSet s : workoutSets) {
                        if (s._id == mSet._id) {
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
        if (mLoadType == Constants.SELECTION_BODYPART) {
            if ((mSet != null) && (mSet.bodypartID > 0)){
                int i=0;
                for (Bodypart bodypart : bodyparts){
                    if ((int)bodypart._id == mSet.bodypartID){
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWearableRecyclerView != null) {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            }
                        });
                    }else i++;
                }
            }

        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((mSet != null) && (mSet.exerciseID > 0)){
                int i=0;
                for (Exercise exercise : exerciseAdapter.getItems()){
                    if ((int)exercise._id == mSet.exerciseID){
                        final int z = i;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWearableRecyclerView != null) {
                                    mWearableLinearLayoutManager.scrollToPosition(z);
                                }
                            }
                        });
                    }else i++;
                }
            }
        }
/*        if ((mLoadType == Utilities.SELECTION_SETS) || (mLoadType == Utilities.SELECTION_REPS)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if ((mLoadType == Utilities.SELECTION_REPS) && (Integer.parseInt(val) == mSet.repCount) && (mSet.repCount >0)){
                    final int y = i;
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
                if ((mLoadType == Utilities.SELECTION_SETS) && (Integer.parseInt(val) == mSet.setCount) && (mSet.setCount > 0)){
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
        }*/

        if ( ((mLoadType == Constants.SELECTION_WEIGHT_KG) || (mLoadType == Constants.SELECTION_WEIGHT_LBS))){
            list_items = setsRepsWeightAdapter.getItems();
            if ((mSet != null) && (mSet.weightTotal > 0))
                for (int i=0; i < list_items.size(); i++){
                    String val = list_items.get(i);
                    if (Float.parseFloat(val) == mSet.weightTotal){
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
        if (mLoadType == Constants.SELECTION_WEIGHT_BODYWEIGHT){
            list_items = setsRepsWeightAdapter.getItems();
            if ((mSet != null) && (mSet.weightTotal > 0))
                for (int i=0; i < list_items.size(); i++){
                    String val = list_items.get(i);
                    if (val.contains("bodyweight - ")){
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
        if ((mLoadType == Constants.SELECTION_TARGET_FIELD) && (mWorkout != null) && (mWorkout.shootFormatID > 0)){
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
        if (((mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (mLoadType == Constants.SELECTION_TARGET_DISTANCE_FIELD)) && (mWorkout.distanceID > 0)){
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (mWorkout.distanceName.equals(val)){
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
        if ((mLoadType== Constants.SELECTION_REST_DURATION_GYM) || (mLoadType== Constants.SELECTION_REST_DURATION_TARGET)){
            int target;
            if (mLoadType== Constants.SELECTION_REST_DURATION_GYM){
                target = UserPreferences.getWeightsRestDuration(mContext);
            }else target = UserPreferences.getArcheryRestDuration(mContext);
            list_items = setsRepsWeightAdapter.getItems();
            for (int i=0; i < list_items.size(); i++){
                String val = list_items.get(i);
                if (Integer.parseInt(val) == target){
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

            child.setScaleX(1 - mProgressToCenter);
            child.setScaleY(1 - mProgressToCenter);
        }

    }
    public void setWorkouts(ArrayList<Workout> routines){
        externalLoad = true;
        workouts = routines;
        if (workoutAdapter != null){
            workoutAdapter.setWorkoutArrayList(workouts);
            workoutAdapter.setListType(true);
            workoutAdapter.notifyDataSetChanged();
        } else{
            Log.i(TAG, "setWorkouts not ready for items " + Integer.toString(workouts.size()));
        }
    }
    public void setWorkoutSets(ArrayList<WorkoutSet> sets){
        externalLoad = true;
        workoutSets = sets;
        if (workoutAdapter != null){
            workoutAdapter.setWorkoutSetArrayList(workoutSets);
            workoutAdapter.setListType(false);
            workoutAdapter.notifyDataSetChanged();
        } else{
            Log.i(TAG, "setWorkoutSets not ready for items " + Integer.toString(workoutSets.size()));
        }
    }
    public void setActivityList(ArrayList<FitnessActivity> faList){
        externalLoad = true;
        fitnessActivities = faList;
        if (activityAdapter != null){
            activityAdapter.setItems(fitnessActivities);
            activityAdapter.notifyDataSetChanged();
        } else Log.i(TAG, "activityAdapter not ready for items");
    }
    public void setBodypartList(ArrayList<Bodypart> bpList){
        externalLoad = true;
        bodyparts = bpList;
        if (bodypartAdapter != null) {
            bodypartAdapter.setItems(bodyparts);
            bodypartAdapter.notifyDataSetChanged();
        }
    }
    public void setExerciseList(ArrayList<Exercise> exList){
        externalLoad = true;
        exercises = exList;
        if (exerciseAdapter != null) {
            exerciseAdapter.setItems(exercises);
            exerciseAdapter.notifyDataSetChanged();
        }        
    }
    public void setWorkout(Workout workout){ mWorkout = workout; }
    public void setWorkoutSet(WorkoutSet workoutSet){ mSet = workoutSet;}

    private class FitnessActivity_AsyncListViewLoader extends AsyncTask<String, Void, ArrayList<FitnessActivity>> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(ArrayList<FitnessActivity> result) {
            super.onPostExecute(result);
            //    dialog.dismiss();
            Log.d(TAG,"PostExecute FA async loader ");
            activityAdapter.setItems(result);
            activityAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mContext == null) mContext = getActivity();
        }

        @Override
        protected ArrayList<FitnessActivity> doInBackground(String... params) {
            ArrayList<FitnessActivity> result = new ArrayList<FitnessActivity>();
            ReferencesTools referencesTools = ReferencesTools.getInstance();

            try {
                if (mContext == null)
                    referencesTools.init(getActivity());
                else
                    referencesTools.init(mContext);

                String[] names = referencesTools.getNamesSorted();
                int[] ids = referencesTools.getIDsNameSorted();
                String[] icons = referencesTools.getIconsNameSorted();
                String[] identifiers = referencesTools.getIdentifiersNameSortedArray();
                int i = 0;

                while (i < ids.length){
                    FitnessActivity fa = new FitnessActivity();
                    fa._id = ids[i];
                    fa.name = names[i];
                    fa.resource_id = mContext.getResources().getIdentifier(icons[i], "drawable", mContext.getPackageName());
                    fa.color = referencesTools.getFitnessActivityColorById(ids[i]);
                    fa.identifier = identifiers[i];
                    result.add(fa);
                    i++;
                }

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
            //    dialog.dismiss();
            bodypartAdapter.setItems(result);
            bodypartAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mContext == null) mContext = getActivity();
            //    dialog.setMessage("Downloading contacts...");
            //    dialog.show();
        }

        @Override
        protected ArrayList<Bodypart> doInBackground(String... params) {
            ArrayList<Bodypart> result = new ArrayList<Bodypart>();
            ReferencesTools referencesTools = ReferencesTools.getInstance();

            try {
                if (mContext == null)
                    referencesTools.init(getActivity());
                else
                    referencesTools.init(mContext);

                String[] names = referencesTools.getBodypartShortNames();
                int[] ids = referencesTools.getBodypartIDs();
               // String[] icons = referencesTools.getIconsNameSorted();
                int[] powerFactors = referencesTools.getBodypartPowerFactors();
                int[] powerFactorColours = referencesTools.getPowerFactorColorArray();
                int i = 0;

                while (i < ids.length){
                    Bodypart bp = new Bodypart();
                    bp._id = ids[i];
                    bp.shortName = names[i];
//                    fa.resource_id = mContext.getResources().getIdentifier(icons[i], "drawable", mContext.getPackageName());
//                    bp.color = referencesTools.getFitnessActivityColorById(ids[i]);
                    result.add(bp);
                    i++;
                }

                return result;
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }
    private class Exercise_AsyncListViewLoader extends AsyncTask<Long, Void, ArrayList<Exercise>> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(ArrayList<Exercise> result) {
            super.onPostExecute(result);
            //    dialog.dismiss();
            exerciseAdapter.setItems(result);

            exerciseAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mContext == null) mContext = getActivity();
            exerciseAdapter.clearList();
            //    dialog.setMessage("Downloading contacts...");
            //    dialog.show();
        }

        @Override
        protected ArrayList<Exercise> doInBackground(Long... params) {
            ArrayList<Exercise> result = new ArrayList<Exercise>();
            ReferencesTools referencesTools = ReferencesTools.getInstance();

            try {
                if (mContext == null) {
                    mContext = getActivity();
                    referencesTools.init(mContext);
                }else
                    referencesTools.init(mContext);

                String[] names = referencesTools.getExerciseNames();
                int[] ids = referencesTools.getExerciseIDs();
                // String[] icons = referencesTools.getIconsNameSorted();
                int[] powerFactors = referencesTools.getExercisePowerRatings();
                int[] powerFactorColours = referencesTools.getPowerFactorColorArray();
                int i = 0;

                while (i < ids.length){
                    Exercise ex = new Exercise();
                    ex._id = ids[i];
                    ex.name = names[i];
//                    fa.resource_id = mContext.getResources().getIdentifier(icons[i], "drawable", mContext.getPackageName());
//                    bp.color = powerFactorColours[powerFactors[i]];
                    result.add(ex);
                    i++;
                }

                return result;
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }    
}
