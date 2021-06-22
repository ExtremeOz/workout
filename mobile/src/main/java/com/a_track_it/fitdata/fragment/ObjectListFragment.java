package com.a_track_it.fitdata.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.BodypartAdapter;
import com.a_track_it.fitdata.adapter.ExerciseAdapter;
import com.a_track_it.fitdata.adapter.ObjectAggregateDetailAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.ObjectAggregate;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ObjectListFragment extends Fragment implements SearchView.OnQueryTextListener{
    private TextView mList_Filter;
    private RecyclerView mRecyclerView;
    private int shortAnimationDuration;
    public static final String TAG = "CustomListFragment";
    public static final String ARG_TYPE = "ARG_TYPE";
    public static final String ARG_SET = "ARG_SET";
    public static final String ARG_PRESET = "ARG_PRESET";
    private WorkoutViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private Spinner mMainSortSpinner;
    private Spinner mMainFilterSpinner;
    private Spinner mMainTypeSpinner;
    private ObjectAggregateDetailAdapter bodypartAggAdapter;
    private ObjectAggregateDetailAdapter exerciseAggAdapter;
    private ObjectAggregateDetailAdapter workoutAggAdapter;
    private BodypartAdapter bodypartAdapter;
    private ExerciseAdapter exerciseAdapter;
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
    private WorkoutMeta mWorkoutMeta;
    private Calendar calendar;
    private String sUserID;
    private String sDeviceID;
    private boolean bUseKG = false;
    private int mDay;
    private int mMonth;
    private boolean bLoading;
    private MutableLiveData<List<Integer>> mDateLiveData = new MutableLiveData<>();
    private String[] mFilterArray;
    private String[] mSortArray;
    private Menu mMenu;
    private List<Long> params = new ArrayList<>();

    public ObjectListFragment() {
        // Required empty public constructor
    }
    public static ObjectListFragment create(int iType, int iPreset, String sUser, String sDevice) {
        final ObjectListFragment fragment = new ObjectListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, iType);
        args.putInt(ARG_PRESET, iPreset);
        args.putString(Constants.KEY_FIT_USER, sUser);
        args.putString(Constants.KEY_FIT_DEVICE_ID, sDevice);
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
        setHasOptionsMenu(true);
        bLoading = true;
        if (getArguments() != null) {
            mLoadType = getArguments().getInt(ARG_TYPE);
            mPreset = getArguments().getInt(ARG_PRESET);
            sUserID = getArguments().getString(Constants.KEY_FIT_USER);
            sDeviceID = getArguments().getString(Constants.KEY_FIT_DEVICE_ID);
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_TYPE))) {
                mLoadType = savedInstanceState.getInt(ARG_TYPE);
                mPreset = savedInstanceState.getInt(ARG_PRESET);
                sUserID = savedInstanceState.getString(Constants.KEY_FIT_USER);
                sDeviceID = savedInstanceState.getString(Constants.KEY_FIT_DEVICE_ID);
            }
        }
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        appPrefs = ApplicationPreferences.getPreferences(requireContext());
        userPrefs = UserPreferences.getPreferences(requireContext(), sUserID);
        calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        Integer[] dayMonth = {mDay, mMonth};
        mDateLiveData.postValue(Arrays.asList(dayMonth));
        int arrayID = (mLoadType == Constants.SELECTION_BODYPART_AGG) ? R.array.bodypart_filter_types : R.array.exercise_filter_types;
        mFilterArray = getContext().getResources().getStringArray(arrayID);
        mSortArray = getContext().getResources().getStringArray(R.array.object_sort_types);
        params.add((long)mLoadType);
        params.add((long)0);
        params.add((long)0);
        params.add((long)0);
        params.add((long)0);  // filter not set
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_TYPE, mLoadType);
        outState.putInt(ARG_PRESET, mPreset);
        outState.putString(Constants.KEY_FIT_USER,sUserID);
        outState.putString(Constants.KEY_FIT_DEVICE_ID,sDeviceID);
        super.onSaveInstanceState(outState);

    }
   final Observer<List<Long>> parameterObserver = new Observer<List<Long>>() {
        @Override
        public void onChanged(List<Long> longs) {
            if (bLoading) return;
            long iObjectType = longs.get(0);
            long iFilterIndex = longs.get(1);
            long iSortIndex = longs.get(2);
            long iSpinnerSource = longs.get(3);
            long iSetFilter = (longs.size() >= 5) ? longs.get(4) : 0;
            String sParameters = iObjectType + Constants.SHOT_DELIM + iFilterIndex + Constants.SHOT_DELIM + iSortIndex + Constants.SHOT_DELIM + iSpinnerSource + Constants.SHOT_DELIM + iSetFilter;
            if (userPrefs != null) userPrefs.setPrefStringByLabel(Constants.USER_PREF_REPORT_SETTINGS, sParameters);
            if ((iFilterIndex > 0) && (iSetFilter > 0)){
                int arrayID = 0;
                if (iObjectType == Constants.SELECTION_BODYPART_AGG){
                    if (iFilterIndex == 1) arrayID = R.array.region_list;
                    if (iFilterIndex == 2) arrayID = R.array.push_pull_list;
                    if (iFilterIndex == 3) arrayID = R.array.number_list;
                    if (iFilterIndex == 4) arrayID = R.array.number_list;
                    if (iFilterIndex == 5) arrayID = R.array.number_list;
                }
                if (iObjectType == Constants.SELECTION_EXERCISE_AGG){
                    if (iFilterIndex == 1) arrayID = R.array.bodypart_list;
                    if (iFilterIndex == 2) arrayID = R.array.resistance_type_list;
                    if (iFilterIndex == 3) arrayID = R.array.number_list;
                    if (iFilterIndex == 4) arrayID = R.array.number_list;
                }
                if (arrayID > 0) {
                    String[] filterList = getResources().getStringArray(arrayID);
                    int iSet = Math.toIntExact(iSetFilter);
                    if (iSet < filterList.length) {
                        mList_Filter.setText(filterList[iSet]);
                        crossFadeIn(mList_Filter);
                    }
                }

            }else
                crossFadeOut(mList_Filter);
            if (iObjectType ==  Constants.SELECTION_BODYPART_AGG){
                if ((iSpinnerSource == 0) && (iSortIndex > 0))
                    bodypartAggAdapter.sortItems(iSortIndex);
                if (iSpinnerSource >= 1){  // filter is the source
                    if ((bodyparts == null) || (bodyparts.size() == 0)) bodyparts = new ArrayList<>(mSessionViewModel.getBodypartList());
                    if (iFilterIndex == 0) bodypartAggAdapter.setBodypartItems(bodyparts,0L);
                    if (iFilterIndex == 1){
                        bodypartAggAdapter.setBodypartItems(bodyparts,iSetFilter); // region
                    }
                    if (iFilterIndex == 2){ // push / pull
                        ArrayList<Bodypart> tempList = new ArrayList<>();
                        if (iSetFilter == 0) { // all
                            bodypartAggAdapter.setBodypartItems(bodyparts, null);
                        }else{
                            for(Bodypart bp : bodyparts){
                                if ((iSetFilter == 1) && (bp.flagPushPull == 1)) tempList.add(bp);
                                if ((iSetFilter == 2) && (bp.flagPushPull == -1)) tempList.add(bp);
                            }
                        }
                        bodypartAggAdapter.setBodypartItems(tempList,null); // push-pull
                    }
                }
                mRecyclerView.setAdapter(bodypartAggAdapter);
            }
            if (iObjectType ==  Constants.SELECTION_EXERCISE_AGG){
                if ((iSpinnerSource == 0) && (iSortIndex > 0))
                    exerciseAggAdapter.sortItems(iSortIndex);
                if (iSpinnerSource >= 1) {  // filter is the source
                    if ((exercises == null) || (exercises.size() == 0)) exercises = new ArrayList<>(mSessionViewModel.getExercisesList());
                    if (iSetFilter == 0)
                        exerciseAggAdapter.setExerciseItems(exercises);
                    else
                        if ((iFilterIndex == 1)||(iFilterIndex == 2)){  // bodypart
                            ArrayList<Exercise> tempList = new ArrayList<>();
                            for(Exercise e : exercises){
                                if ((iFilterIndex == 1) && (e.first_BPID == iSetFilter)) tempList.add(e);
                                if ((iFilterIndex == 2) && (e.resistanceType == iSetFilter)) tempList.add(e);
                            }
                            exerciseAggAdapter.setExerciseItems(tempList);
                        }
                }
                mRecyclerView.setAdapter(exerciseAggAdapter);
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        if (mContext == null) mContext = getContext();
        if (mContext == null) mContext = container.getContext();
        rootView = inflater.inflate(R.layout.fragment_objectlist, container, false);

        bUseKG = userPrefs.getUseKG();
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(mContext.getApplicationContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mList_Filter = rootView.findViewById(R.id.filter_title);
        mList_Filter.setOnClickListener(v -> doFilterDialog());
        mRecyclerView = rootView.findViewById(R.id.object_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mMessagesViewModel.getReportParameters().observe(requireActivity(), parameterObserver);
        SavedStateViewModel mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        if (sDeviceID == null || sDeviceID.length() == 0) sDeviceID = mSavedStateViewModel.getDeviceID();
        if (mSavedStateViewModel.getActiveWorkout().getValue() != null)
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        else
            mWorkout = new Workout();

        if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null)
            mSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        else
            mSet = new WorkoutSet();

        if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() != null)
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
        else
            mWorkoutMeta = new WorkoutMeta();

        if ((savedInstanceState != null) && (mLoadType == 0) && (savedInstanceState.containsKey(ARG_TYPE)))
            mLoadType = savedInstanceState.getInt(ARG_TYPE);

        bodypartAggAdapter = new ObjectAggregateDetailAdapter(mContext,Constants.OBJECT_TYPE_BODYPART, userPrefs.getUseKG());
        bodypartAggAdapter.setOnItemClickListener((view, viewModel) -> {
            mCallback.onCustomItemSelected(mLoadType, ((Bodypart)viewModel)._id, ((Bodypart)viewModel).shortName, 0, Constants.ATRACKIT_EMPTY);
        });
        if (mLoadType == Constants.SELECTION_BODYPART_AGG){
            mRecyclerView.setAdapter(bodypartAggAdapter);
        }
        exerciseAggAdapter = new ObjectAggregateDetailAdapter(mContext,Constants.OBJECT_TYPE_EXERCISE, userPrefs.getUseKG());
        exerciseAggAdapter.setOnItemClickListener((view, viewModel) -> {
            mCallback.onCustomItemSelected(mLoadType, ((Exercise)viewModel)._id, ((Exercise)viewModel).name, 0, Constants.ATRACKIT_EMPTY);
        });

        if (mLoadType == Constants.SELECTION_EXERCISE_AGG){
            mRecyclerView.setAdapter(exerciseAggAdapter);
        }
        workoutAggAdapter = new ObjectAggregateDetailAdapter(mContext,Constants.OBJECT_TYPE_WORKOUT, userPrefs.getUseKG());
        workoutAggAdapter.setOnItemClickListener((view, viewModel) -> {
            mCallback.onCustomItemSelected(mLoadType, ((Workout)viewModel)._id, ((Workout)viewModel).activityName, 0, Constants.ATRACKIT_EMPTY);

        });
        if (mLoadType == Constants.SELECTION_WORKOUT_AGG){
            mRecyclerView.setAdapter(workoutAggAdapter);
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
                if ((mSet.regionID != null ) && (mSet.regionID > 0)){
                    exercises = mSessionViewModel.getExercisesByRegionId(mSet.regionID);
                }else {
                    exercises = new ArrayList<>();
                    List<Exercise> list = mSessionViewModel.getExercisesList();
                    if (list != null) exercises.addAll(list);
                }
            }
            exerciseAdapter.setItems(exercises);
        }
        if (mLoadType == Constants.SELECTION_BODYPART_AGG){
            ArrayList<ObjectAggregate> items = new ArrayList<>(mSessionViewModel.getBodypartAggList(sUserID));
            if ((bodyparts == null) || (bodyparts.size() == 0)) bodyparts = new ArrayList<>(mSessionViewModel.getBodypartList());
            bodypartAggAdapter.setItems(items);
            bodypartAggAdapter.setBodypartItems(bodyparts, null);
        }
        if (mLoadType == Constants.SELECTION_EXERCISE_AGG){
            ArrayList<ObjectAggregate> items = new ArrayList<>(mSessionViewModel.getExerciseAggList(sUserID));
            ArrayList<Exercise> exList = new ArrayList<>(mSessionViewModel.getExercisesList());
            exerciseAggAdapter.setItems(items);
            exerciseAggAdapter.setExerciseItems(exList);
        }

        if (mLoadType == Constants.SELECTION_BODYPART){
            if (bodypartAdapter == null){
                if (bodypartAdapter == null) bodypartAdapter = new BodypartAdapter(mContext);
                bodypartAdapter.setOnItemClickListener((view, viewModel) -> {
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.shortName, 0, Constants.ATRACKIT_EMPTY);

                });
            }
            mRecyclerView.setAdapter(bodypartAdapter);
        }
        if (mLoadType == Constants.SELECTION_EXERCISE){
            if (exerciseAdapter == null){
                if (exerciseAdapter == null) exerciseAdapter = new ExerciseAdapter(mContext,0);
                exerciseAdapter.setOnItemClickListener((view, viewModel) -> {
                    mCallback.onCustomItemSelected(mLoadType, viewModel._id, viewModel.name, 0, Constants.ATRACKIT_EMPTY);
                });
            }
            mRecyclerView.setAdapter(exerciseAdapter);
        }
        bLoading = false;
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setStartScrollPos();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.object_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        bLoading = true;
        mMenu = menu;
    //    Context context = getActivity().getApplicationContext();
        mMainTypeSpinner = (Spinner)menu.findItem(R.id.action_type_spinner).getActionView();
        if (mMainTypeSpinner != null){
            ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(mContext, R.array.object_types, android.R.layout.simple_spinner_item);
            adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            mMainTypeSpinner.setForegroundTintList(AppCompatResources.getColorStateList(mContext, R.color.primaryTextColor));
            mMainTypeSpinner.setAdapter(adapterFilter);
            mMainTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    mMainFilterSpinner.setSelection(0);
                    doFilterDialog();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    params.set(0,(long)mLoadType);
                    params.set(1,(long)0);
                    params.set(2,(long)0);
                    params.set(3,(long)0);
                    params.set(4,(long)0);  // filter not set
                    if (!bLoading)
                        mMessagesViewModel.addReportParameters(params);
                }

            });
            if (mLoadType == Constants.SELECTION_BODYPART_AGG || mLoadType == Constants.SELECTION_BODYPART) mMainTypeSpinner.setSelection(1);
            if (mLoadType == Constants.SELECTION_EXERCISE_AGG || mLoadType == Constants.SELECTION_EXERCISE) mMainTypeSpinner.setSelection(2);
            if (mLoadType == Constants.SELECTION_WORKOUT_AGG|| mLoadType == Constants.SELECTION_WORKOUT_REPORT) mMainTypeSpinner.setSelection(3);
        }
        mMainFilterSpinner = (Spinner)menu.findItem(R.id.action_filter_spinner).getActionView();
        if ((mMainFilterSpinner != null) && ((mLoadType == Constants.SELECTION_BODYPART_AGG)||(mLoadType == Constants.SELECTION_EXERCISE_AGG))){
            int arrayID = (mLoadType == Constants.SELECTION_BODYPART_AGG) ? R.array.bodypart_filter_types : R.array.exercise_filter_types;
            ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(mContext, arrayID, android.R.layout.simple_spinner_item);
            adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            mMainFilterSpinner.setForegroundTintList(AppCompatResources.getColorStateList(mContext, R.color.primaryTextColor));
            mMainFilterSpinner.setAdapter(adapterFilter);
            mMainFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    doFilterDialog();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    params.set(0,(long)mLoadType);
                    params.set(1,(long)0);
                    params.set(2,(long)0);
                    params.set(3,(long)0);
                    params.set(4,(long)0);  // filter not set
                    if (!bLoading)
                        mMessagesViewModel.addReportParameters(params);
                }

            });
        }
        mMainSortSpinner = (Spinner)menu.findItem(R.id.action_sort_spinner).getActionView();
        if (mMainSortSpinner != null){
            ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(mContext, R.array.object_sort_types, android.R.layout.simple_spinner_item);
            adapterSort.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            mMainSortSpinner.setForegroundTintList(AppCompatResources.getColorStateList(mContext, R.color.primaryTextColor));
            mMainSortSpinner.setAdapter(adapterSort);
            mMainSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    int iFilter = mMainFilterSpinner.getSelectedItemPosition();
                    params.set(0,(long)mLoadType);
                    params.set(1,(long)iFilter);
                    params.set(2,(long)position);
                    params.set(3,(long)0);
                    params.set(4,(long)0);  // filter not set
                    if (!bLoading)
                        mMessagesViewModel.addReportParameters(params);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

        }
/*        MenuItem mPendingItem = menu.findItem(R.id.action_pending_exercise);
        if (mLoadType == Constants.SELECTION_EXERCISE_AGG){
            DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
            if (exerciseTuple.sync_count > 0){
                String sTitle = String.format(getString(R.string.action_pending_exercise),exerciseTuple.sync_count);
                mPendingItem.setTitle(sTitle);
                Drawable icon = AppCompatResources.getDrawable(context,R.drawable.ic_edit_black_24dp);
                Utilities.setColorFilter(icon, context.getColor(R.color.white));
                mPendingItem.setIcon(icon);
            }else {
                Drawable icon = AppCompatResources.getDrawable(context,R.drawable.ic_outline_check_white);
                mPendingItem.setTitle(getString(R.string.action_no_pending_exercise));
                mPendingItem.setIcon(icon);
            }
        }else
            mPendingItem.setVisible(false);*/
        bLoading = false;

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.w(ObjectListFragment.class.getSimpleName(), "onOptionsItemSelected " + item.getTitle());
        if ((item.getItemId() == R.id.action_type_spinner)||(item.getItemId() == R.id.action_filter_spinner)||(item.getItemId() == R.id.action_sort_spinner))
            return  true;
/*        if (item.getItemId() == R.id.action_pending_exercise) {
            DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
            List<Exercise> pendingExercises = mSessionViewModel.getPendingExercises();
            if ((pendingExercises.size() > 0) && (exerciseTuple.sync_count > 0)) {
                Exercise pending = pendingExercises.get(0);
                mCallback.onCustomItemSelected(Constants.SELECTION_EXERCISE_AGG, pending._id,pending.name,1,pending.otherNames); // pending flag
            }
            return true;
        }*/
            return super.onOptionsItemSelected(item);
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
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

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

    private void doFilterDialog(){
        int arrayID = 0;
        int loadType = mMainTypeSpinner.getSelectedItemPosition();
        if (loadType == 1) mLoadType = Constants.SELECTION_BODYPART_AGG;
        if (loadType == 2) mLoadType = Constants.SELECTION_EXERCISE_AGG;
        if (loadType == 3) mLoadType = Constants.SELECTION_WORKOUT_AGG;
        int filterType = mMainFilterSpinner.getSelectedItemPosition();
        if (filterType == 0){
            params.set(0,(long)mLoadType);
            params.set(1,(long)filterType);
            params.set(2,(long)0);
            params.set(3,(long)1);         // set filter target not sort
            params.set(4,(long)0);  // selected filter
            crossFadeOut(mList_Filter);
            mMessagesViewModel.addReportParameters(params);
            return; // no filter selected
        }
        int sortType = mMainSortSpinner.getSelectedItemPosition();
        String sTitle = mFilterArray[filterType];
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_dropdown,null,false);
        TextView title = view.findViewById(R.id.filterLabel);
        title.setText(sTitle);
        Spinner spinner = view.findViewById(R.id.filterDropDown);
        if (mLoadType == Constants.SELECTION_BODYPART_AGG){
            if (filterType == 1) arrayID = R.array.region_list;
            if (filterType == 2) arrayID = R.array.push_pull_list;
            if (filterType == 3) arrayID = R.array.number_list;
            if (filterType == 4) arrayID = R.array.number_list;
            if (filterType == 5) arrayID = R.array.number_list;
        }
        if (mLoadType == Constants.SELECTION_EXERCISE_AGG){
            if (filterType == 1) arrayID = R.array.bodypart_list;
            if (filterType == 2) arrayID = R.array.resistance_type_list;
            if (filterType == 3) arrayID = R.array.number_list;
            if (filterType == 4) arrayID = R.array.number_list;

        }
        ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(mContext, arrayID, android.R.layout.simple_spinner_item);
        adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapterFilter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    params.set(0,(long)mLoadType);
                    params.set(1,(long)filterType);
                    params.set(2,(long)sortType);
                    params.set(3,(long)1);         // set filter target not sort
                    params.set(4,(long)position);  // selected filter
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
        title.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
        });
        btnOk.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();
            mMessagesViewModel.addReportParameters(params);
        });
        btnCancel.setOnClickListener(v -> {
            if (alertDialog != null) alertDialog.dismiss();

        });
        alertDialog.show();
    }

    private void setStartScrollPos(){
        mRecyclerView.requestFocus();
        mRecyclerView.setSelected(true);
        ArrayList<String> list_items = new ArrayList<>();
        int iTargetID = 0;
        String sLabel = getString(R.string.default_loadtype) + Integer.toString(mLoadType);

        if (mLoadType == Constants.SELECTION_BODYPART) {
            if ((mSet != null) && ((mSet.bodypartID != null ) && (mSet.bodypartID > 0))){
                int i=0;
                if (bodypartAdapter == null) {
                    bodypartAdapter = new BodypartAdapter(mContext);
                    bodypartAdapter.setItems(bodyparts, mSet.regionID);
                }
                if ((mSet.bodypartID != null ) && (mSet.bodypartID > 0)) {
                    bodypartAdapter.setTargetId(mSet.bodypartID);
                    if (bodypartAdapter.getSelectedPos() > RecyclerView.NO_POSITION) mRecyclerView.getLayoutManager().scrollToPosition(bodypartAdapter.getSelectedPos());
                }
            }

        }
        if (mLoadType == Constants.SELECTION_EXERCISE) {
            if ((mSet != null) && (mSet.exerciseID != null) && (mSet.exerciseID > 0)){
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

    } // startScrollPos


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
