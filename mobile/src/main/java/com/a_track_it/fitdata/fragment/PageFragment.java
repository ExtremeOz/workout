package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.RecyclerViewAdapter;
import com.a_track_it.fitdata.animation.ItemAnimator;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.service.CacheResultReceiver;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.database.CacheManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Chris Black - updated Daniel Haywood
 *
 * Used to display a_track_it.com page of data within the view pager.
 */
public class PageFragment extends androidx.fragment.app.Fragment
        implements RecyclerViewAdapter.OnItemClickListener, CacheResultReceiver.Receiver, SearchView.OnQueryTextListener {

    private static final String TAG = "PageFragment";
    private static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_FILTER = "ARG_FILTER";
    private static final String ARG_LIST_VIEW = "ARG_LIST_VIEW";
    private MessagesViewModel mMessagesViewModel;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerViewAdapter adapter;
    private boolean needsRefresh = true;
    private boolean bUseGrid = false;
    private String sDeviceID;
    private String sUserID;
    private int mPage;
    private String pageTitle;
    private int reportType;
    private String mFilterText = Constants.ATRACKIT_EMPTY;
    private CacheResultReceiver mReceiver;
    protected FragmentInterface mCallback;
    protected Handler mHandler;
    protected Runnable mRunnable;
    private RecyclerView mRecyclerView;
    private TextView mNoItemsTextView;
    private Spinner mMainPeriodSpinner;
    private boolean bLoading;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;

    private Observer<List<Long>> parameterObserver = new Observer<List<Long>>() {
        @Override
        public void onChanged(List<Long> longs) {
            int iObjectType = Math.toIntExact(longs.get(0));
            if (iObjectType == Constants.OBJECT_TYPE_WORKOUT) {
                reportType = Math.toIntExact(longs.get(1));
                mPage = Math.toIntExact(longs.get(2));
                String sParameters = iObjectType + Constants.SHOT_DELIM + reportType + Constants.SHOT_DELIM + mPage + Constants.SHOT_DELIM + mFilterText;
                if (userPrefs != null) userPrefs.setPrefStringByLabel(Constants.USER_PREF_ACTIVITY_SETTINGS, sParameters);
                if (!bLoading) {
                    mReceiver.setReceiver(PageFragment.this::onReceiveResult);
                    if (!bLoading) {
                        mHandler = new Handler();
                        mRunnable = () -> {
                            Log.e(PageFragment.class.getSimpleName(), "on getReportParameters refresh");
                            refreshData();
                        };
                        mHandler.postDelayed(mRunnable, 200L);
                    }
                }
            }
        }
    };
    public static PageFragment create(int page,String title, String filterText, boolean useList, String sUserId, String sDeviceId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_FILTER, filterText);
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_LIST_VIEW, (!useList) ? 0 : 1);
        args.putString(Constants.KEY_FIT_USER,sUserId);
        args.putString(Constants.KEY_FIT_DEVICE_ID,sDeviceId);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            Bundle args = getArguments();
            mPage = args.getInt(ARG_PAGE);
            pageTitle = args.getString(ARG_TITLE);
            mFilterText = args.getString(ARG_FILTER);
            sUserID = args.getString(Constants.KEY_FIT_USER);
            sDeviceID = args.getString(Constants.KEY_FIT_DEVICE_ID);
            bUseGrid = (args.getInt(ARG_LIST_VIEW, 0) == 1);
            reportType = (bUseGrid) ? 1 : 0;
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_PAGE))) {
                mPage = savedInstanceState.getInt(ARG_PAGE);
                pageTitle = savedInstanceState.getString(ARG_TITLE);
                sUserID = savedInstanceState.getString(Constants.KEY_FIT_USER);
                sDeviceID = savedInstanceState.getString(Constants.KEY_FIT_DEVICE_ID);
                bUseGrid = (savedInstanceState.getInt(ARG_LIST_VIEW, 0) == 1);
                reportType = (bUseGrid) ? 1 : 0;
            }
        }
        mReceiver = new CacheResultReceiver(new Handler());
        bLoading = true;
        appPrefs = ApplicationPreferences.getPreferences(getContext());
        userPrefs = UserPreferences.getPreferences(getContext(), sUserID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        mRecyclerView =  view.findViewById(R.id.recyclerView);
        mNoItemsTextView = view.findViewById(R.id.textNoItems);
        Context context = getContext();
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        List<Workout> items = new ArrayList<>();
        adapter = new RecyclerViewAdapter(items,context, pageTitle, bUseGrid);
        adapter.setOnItemClickListener(this);
        filter();
        mRecyclerView.setItemAnimator(new ItemAnimator());
        mRecyclerView.setAdapter(adapter);
        setRecyclerViewLayoutManager(bUseGrid);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contentView);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!bLoading) refreshData();
            }
        });
        mNoItemsTextView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setEnabled(false);
        mMessagesViewModel.getReportParameters().observe(getViewLifecycleOwner(), parameterObserver);
        bLoading = false;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.page_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem viewItem =  menu.findItem(R.id.action_use_grid);
        boolean bUseListDisplay = appPrefs.getPrefByLabel(Constants.LABEL_USE_GRID);
        Drawable drawable = AppCompatResources.getDrawable(getContext(),(bUseListDisplay) ? R.drawable.ic_grid_view : R.drawable.ic_list_view);
        String sText = (bUseListDisplay) ? getString(R.string.nav_grid_view) : getString(R.string.nav_list_view);
        viewItem.setIcon(drawable);
        viewItem.setTitle(sText);
        MenuItem spinItem = menu.findItem(R.id.action_page_period_spinner);
        if ((spinItem != null) && (spinItem.getActionView() != null)) {
            mMainPeriodSpinner = (Spinner) spinItem.getActionView();
            ArrayAdapter<CharSequence> adapterFilter = ArrayAdapter.createFromResource(getContext(), R.array.period_types, android.R.layout.simple_spinner_item);
            adapterFilter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            mMainPeriodSpinner.setAdapter(adapterFilter);
            mMainPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    List<Long> params = new ArrayList<>();
                    boolean bSetting = appPrefs.getPrefByLabel(Constants.LABEL_USE_GRID);
                    params.add((long)Constants.OBJECT_TYPE_WORKOUT);
                    if (bSetting) params.add(1l);else params.add(0l);
                    params.add((long) position);
                    params.add((long)3); // period source
                    if (!bLoading)
                        mMessagesViewModel.addReportParameters(params);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final String sUser = appPrefs.getLastUserID();
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        final boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
        if (id == R.id.action_use_grid){
            boolean bSetting = appPrefs.getPrefByLabel(Constants.LABEL_USE_GRID);
            bSetting = !bSetting;
            Drawable drawable = AppCompatResources.getDrawable(getContext(), (bSetting) ? R.drawable.ic_grid_view : R.drawable.ic_list_view);
            String sText = (bSetting) ? getString(R.string.nav_grid_view) : getString(R.string.nav_list_view);
            item.setIcon(drawable);
            item.setTitle(sText);
            appPrefs.setPrefByLabel(Constants.LABEL_USE_GRID, bSetting);
            List<Long> params = new ArrayList<>();
            params.add((long)Constants.OBJECT_TYPE_WORKOUT);
            if (bSetting) params.add(1l); else params.add(0l);
            params.add((long)mMainPeriodSpinner.getSelectedItemPosition());
            params.add((long)2); // use grid source
            mMessagesViewModel.addReportParameters(params);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if(activity instanceof FragmentInterface) {
            mCallback = (FragmentInterface) activity;
        }
    }

    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_PAGE, mPage);
        outState.putInt(ARG_LIST_VIEW, reportType);
        outState.putString(ARG_TITLE, pageTitle);
        outState.putString(Constants.KEY_FIT_USER, sUserID);
        outState.putString(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
        super.onSaveInstanceState(outState);
    }

    public void setFilterText(String text) {
        if(mFilterText.equals(text)) {
            return;
        }
        mFilterText = text;
        filter();
    }

    public void setReportType(int rType) {
        this.reportType = rType;
        bUseGrid = (rType == 1);
    }

    public int getReportType() {
        return reportType;
    }

    public void setTitle(String sTitle){
        pageTitle = sTitle;
    }

    private void refreshData() {
        if (bLoading) return;
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
      //  if (reportType == 0)
      //      mMessagesViewModel.setWorkType(Constants.TASK_ACTION_READ_LOCAL);
      //  mMessagesViewModel.setWorkInProgress(true);
        long startTime;
        long endTime;
        Utilities.TimeFrame tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
        switch (mPage){
            case 0:
                tf = Utilities.TimeFrame.BEGINNING_OF_DAY;
                break;
            case 1:
                tf = Utilities.TimeFrame.BEGINNING_OF_WEEK;
                break;
            case 2:
                tf = Utilities.TimeFrame.LAST_WEEK;
                break;
            case 3:
                tf = Utilities.TimeFrame.BEGINNING_OF_MONTH;
                break;
            case 4:
                tf = Utilities.TimeFrame.LAST_MONTH;
                break;
            case 5:
                tf = Utilities.TimeFrame.NINETY_DAYS;
                break;
            case 6:
                tf = Utilities.TimeFrame.BEGINNING_OF_YEAR;
                break;
        }
        pageTitle = Utilities.getTimeFrameText(tf);
        startTime = Utilities.getTimeFrameStart(tf);
        if (mPage != 0)
            endTime = Utilities.getTimeFrameEnd(tf);
        else
            endTime = System.currentTimeMillis();
        bLoading = true;
        if (getContext() != null)
            CacheManager.getReport(reportType, 0, mReceiver, getContext(),sUserID,sDeviceID,startTime, endTime);
    }

    public void filter() {
        adapter.filter(mFilterText);
    }

    @Override
    public void onResume() {
        super.onResume();
        bLoading = false;
/*        mReceiver.setReceiver(this);
        if (!bLoading){
            mHandler = new Handler();
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.e(PageFragment.class.getSimpleName(),"on Resume refresh");
                    refreshData();
                }
            };
            mHandler.postDelayed(mRunnable, 200L);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        bLoading = true;
/*        mReceiver.setReceiver(null);
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }*/
        if (mSwipeRefreshLayout != null) {
         //   mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
           // mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }
    public void setRecyclerViewLayoutManager(boolean bUse){
        int scrollPos = 0;
        bUseGrid = bUse;
        mRecyclerView.setAdapter(null);
        if (bUseGrid)
            mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 2));
        else{
            LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);
        }
        adapter.setUseGrid(bUse);
        mRecyclerView.setAdapter(adapter);
    }
    public void setSwipeToRefreshEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }
    public void forceRebuild(){
        needsRefresh = true;
        refreshData();
    }

    @Override
    public void onItemClick(View view, Workout viewModel) {
        if (mCallback != null) {
            String sUseGrid = Boolean.toString(bUseGrid);
            //int idImage = view.findViewById(R.id.image).getId();
            if (bUseGrid)
                mCallback.onItemSelected(Constants.SELECTION_WORKOUT_REPORT, viewModel.activityID,viewModel.activityName,mPage,sUseGrid);
            else
                mCallback.onItemSelected(Constants.SELECTION_WORKOUT_REPORT, viewModel._id,pageTitle,mPage,sUseGrid);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        bLoading = false;
        Log.e(PageFragment.class.getSimpleName(),"onReceiveResult");
       // mMessagesViewModel.setWorkInProgress(false);
        List<Workout> workoutList = resultData.getParcelableArrayList(Constants.KEY_LIST_WORKOUTS);
        if ((reportType != 1) && ((workoutList == null) || (workoutList.size() == 0))){
            Log.e(PageFragment.class.getSimpleName(),"workoutList is NULL ");
            if (mHandler == null) mHandler = new Handler();
            mHandler.post(() -> {
                // Update the UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        mNoItemsTextView.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            });
            return;
        }
        if (reportType == 1){  // summary report type
            if (!bUseGrid){
                bUseGrid = true;
                setRecyclerViewLayoutManager(true);
            }
            workoutList.sort((o1, o2) -> ((o1.activityID < o2.activityID) ? -1: ((o1.activityID > o2.activityID) ? 1 : 0)));
        }
        else{
            if (bUseGrid){
                bUseGrid = false;
                setRecyclerViewLayoutManager(false);
            }
            Workout tester = new Workout();
            Iterator<Workout> it = workoutList.iterator();
            while (it.hasNext()){
                tester = it.next();
                if (tester.activityID == Constants.WORKOUT_TYPE_TIME) it.remove();
            }
            workoutList.sort((o1, o2) -> ((o1.start < o2.start) ? -1: ((o1.start > o2.start) ? 1 : 0)));
        }

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        if (mHandler == null) mHandler = new Handler();
        mRunnable = () -> {
            Log.e(PageFragment.class.getSimpleName(),"receiver loading " + workoutList.size());
                adapter.setItems(workoutList, pageTitle);
                filter();
            // Update the UI
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNoItemsTextView.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        };
        mHandler.post(mRunnable);


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}

