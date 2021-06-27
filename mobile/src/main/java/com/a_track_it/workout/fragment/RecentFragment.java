package com.a_track_it.workout.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.AnimatedLayoutManager;
import com.a_track_it.workout.adapter.SessionListViewAdapter;
import com.a_track_it.workout.animation.ItemAnimator;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.data_model.FitnessActivity;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.service.CacheResultReceiver;
import com.a_track_it.workout.common.service.CacheManager;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Chris Black, updated by Daniel Haywood
 *
 * Displays historical entries in a_track_it.com vertical list.
 */

public class RecentFragment extends androidx.fragment.app.Fragment implements SessionListViewAdapter.OnItemClickListener, CacheResultReceiver.Receiver  {
    public static final String TAG = "RecentFragment";
    private int mRecentType;
    //private CacheManager.ICacheManager cacheReceiver;
    private CacheResultReceiver mReceiver;
    private RecyclerView mRecyclerView;
    private String sDeviceID;
    private String sUserID;
    private SessionListViewAdapter adapter;
    private WorkoutViewModel mSessionViewModel;
    private long startTime;
    private long endTime;
    private List<FitnessActivity> faList = new ArrayList<>();
    private FragmentInterface mListener;

    public static RecentFragment create(String sUserId, String sDeviceId, int type, long startTime, long endTime) {
        Bundle args = new Bundle();
        args.putInt(Constants.MAP_DATA_TYPE, type);
        args.putString(Constants.KEY_FIT_USER,sUserId);
        args.putString(Constants.KEY_FIT_DEVICE_ID,sDeviceId);
        args.putLong(Constants.MAP_START, startTime);
        args.putLong(Constants.MAP_END, endTime);
        RecentFragment fragment = new RecentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mRecentType = getArguments().getInt(Constants.MAP_DATA_TYPE);
            sUserID = getArguments().getString(Constants.KEY_FIT_USER);
            sDeviceID = getArguments().getString(Constants.KEY_FIT_DEVICE_ID);
            startTime = getArguments().getLong(Constants.MAP_START);
            endTime = getArguments().getLong(Constants.MAP_END);
            mReceiver = new CacheResultReceiver(new Handler());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        Context context = container.getContext();
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(context.getApplicationContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        if (mRecentType <= 0){
            faList = mSessionViewModel.getFitnessActivityList();
        }
        UserPreferences userPrefs = UserPreferences.getPreferences(context, sUserID);
        List<Workout> items = new ArrayList<>();
        Workout[] arrayWorkouts = new Workout[items.size()];
        items.toArray(arrayWorkouts);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new AnimatedLayoutManager(context, 1));
        adapter = new SessionListViewAdapter(userPrefs.getUseKG(), arrayWorkouts);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        adapter.setAllowSelection(true);
        mReceiver.setReceiver(this);
        mRecyclerView.setItemAnimator(new ItemAnimator());
        mRecyclerView.setAdapter(adapter);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT, RecentFragment.class.getSimpleName());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentInterface) {
            mListener = (FragmentInterface) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
    }
    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void setStartTime(long starting){
        startTime = starting;
    }
    public void setEndTime(long ending){ endTime = ending; }
    public void refreshData(){
        mReceiver.setReceiver(this);
        CacheManager.getReport(mRecentType, 0, mReceiver, getActivity(),sUserID,sDeviceID, startTime, endTime);
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 200){
            int reportType = resultData.getInt(Constants.KEY_FIT_TYPE);
            String sUser = resultData.getString(Constants.KEY_FIT_USER);
            if (!sUser.equals(sUserID)) return;
            if ((reportType == -2)||(reportType == -1)||(reportType == 0)||(reportType == 3)){
                List<Workout> workoutList = resultData.getParcelableArrayList(Constants.KEY_LIST_WORKOUTS);
                int resultSize =  (workoutList == null) ? 0 : workoutList.size();
                if (resultSize > 0) {
                    if (reportType <= 0){
                        for(Workout w: workoutList){
                            if (w.activityName.length() > 0 && (w.activityID == null || w.activityID == 0)){
                                w.activityID = getActivityID(w.activityName);
                            }
                        }
                    }
                    adapter.swapList(workoutList.toArray(new Workout[resultSize]));
                }else
                    adapter.setIsEmpty();
                if (resultData.containsKey(Constants.KEY_LIST_SETS)){
                    List<WorkoutSet> sets = resultData.getParcelableArrayList(Constants.KEY_LIST_SETS);
                  //  sets.sort((o1, o2) -> ((o1.workoutID == o2.workoutID) ? Long.compare(o1.start, o2.start) : Long.compare(o1.workoutID, o2.workoutID)));
                    int setsSize = (sets == null) ? 0 : sets.size();
                    if (setsSize > 0) {
                        WorkoutSet[] arraySets = sets.toArray(new WorkoutSet[setsSize]);
                        adapter.swapSets(arraySets);
                    }else
                        adapter.setIsEmpty();
                }
            }

        }
    }

    @Override
    public void onItemClick(int viewUID, Object viewModel) {
        if (viewUID == Constants.UID_btn_recycle_item_delete){
            Log.w(TAG, "recycle item delete pushed " + viewModel.toString());
            WorkoutSet set = ((WorkoutSet)viewModel);
            mListener.onItemSelected(viewUID,set._id, set.userID,set.workoutID,set.deviceID);
            return;
        }
        if (viewUID == Constants.UID_btn_recycle_item_copy){
            Log.w(TAG, "recycle item delete pushed " + viewModel.toString());
            WorkoutSet set = ((WorkoutSet)viewModel);
            mListener.onItemSelected(viewUID,set.workoutID, set.userID,set._id,set.deviceID);
            return;
        }
        if ((viewUID == Constants.UID_btn_recycle_item_report) || (viewUID == Constants.UID_btn_recycle_item_select)){
            Log.w(TAG, "session list report/start pushed " + viewModel.toString());
            if (viewModel instanceof Workout){
                Workout workout = ((Workout)viewModel);
                long rowid = workout._id;
                String userID = ((Workout)viewModel).userID;
                //mListener.OnFragmentInteraction(view.getId(),rowid,userID);
                mListener.onItemSelected(viewUID,workout._id, workout.userID,Math.toIntExact(workout.activityID), workout.deviceID);
            }
            if (viewModel instanceof WorkoutSet){
                WorkoutSet set = ((WorkoutSet)viewModel);
                mListener.onItemSelected(viewUID,set.workoutID, set.userID,Math.toIntExact(set.activityID),set.deviceID);
            }
            return;
        }
    }

    private long getActivityID(String sIdentifier){
        long lRetVal = 0;
        String sLookup = sIdentifier.toLowerCase().trim();
        if (faList.size() > 0) {
            Iterator<FitnessActivity> iterator = faList.iterator();
            while (iterator.hasNext()) {
                FitnessActivity fa = iterator.next();
                if (fa.identifier.equals(sLookup)) {
                    lRetVal = fa._id;
                    break;
                }
            }
        }
        return lRetVal;
    }
/*    public void swapList(Workout[] aList){
        adapter.swapList(aList);
        adapter.notifyDataSetChanged();
    }*/

}
