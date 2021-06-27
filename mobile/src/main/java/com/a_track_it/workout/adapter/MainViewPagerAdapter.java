package com.a_track_it.workout.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.fragment.HomeFragment;
import com.a_track_it.workout.fragment.ObjectListFragment;
import com.a_track_it.workout.fragment.PageFragment;

import java.util.HashMap;
import java.util.Map;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    private Map<Integer, Fragment> mPageReferenceMap = new HashMap<>();
    private String sUserID;
    private String sDeviceID;
    private int bottomIndex;
    private int mPeriodIndex;
    private int defaultType;
    private boolean useGrid;
    public String filter;
    private String[] period_titles;
    private static final String[] TITLES = new String[] {
            "Live",
            "Completed",
            "Review" //,"Chart"
    };
    public static final int NUM_TITLES = TITLES.length;

    public MainViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        useGrid = true;
        mPeriodIndex = 0;
        filter = Constants.ATRACKIT_EMPTY;
    }
    public MainViewPagerAdapter(FragmentActivity fragmentActivity, String u, String d, int objectType) {
        super(fragmentActivity);
        useGrid = true;
        mPeriodIndex = 0;
        filter = Constants.ATRACKIT_EMPTY;
        sUserID = u;
        sDeviceID = d;
        defaultType = objectType;
    }
    public void setBottomIndex(int i){ bottomIndex = i;}
    public int getBottomIndex(){ return bottomIndex;}
    public void setDefaultType(int type){
        defaultType = type;
    }
    @Override
    public int getItemCount() {
        return NUM_TITLES;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (mPageReferenceMap.get(position) != null){
            Log.e(MainViewPagerAdapter.class.getSimpleName(),"createFragment has already " + position);
            return mPageReferenceMap.get(position);
        }
        Log.e(MainViewPagerAdapter.class.getSimpleName(),"createFragment " + position);
        if (position == 0){
            HomeFragment myHomeFragment = HomeFragment.newInstance();
            mPageReferenceMap.put(position, myHomeFragment);
            return myHomeFragment;
        }
        if (position == 1){
            String sTitle = (period_titles == null) ? "Today" : period_titles[mPeriodIndex];
            PageFragment pageFragment = PageFragment.create(mPeriodIndex,sTitle,filter,useGrid,sUserID,sDeviceID);
            mPageReferenceMap.put(position, pageFragment);
            return pageFragment;
        }
        if (position == 2){
            if (defaultType == 0) defaultType = Constants.SELECTION_BODYPART_AGG;
            ObjectListFragment objectListFragment = ObjectListFragment.create(defaultType, 0, sUserID, sDeviceID);
            mPageReferenceMap.put(position, objectListFragment);
            return objectListFragment;
        }
        /*if (position == 3){
            ObjectListFragment objectListFragment = ObjectListFragment.create(Constants.SELECTION_EXERCISE_AGG, 0, sUserID, sDeviceID);
            mPageReferenceMap.put(position, objectListFragment);
            return objectListFragment;
        }*/
        return null;
    }



    public Fragment getFragment(int position){
        return mPageReferenceMap.get(position);
    }

    public void onDestroy(){
       mPageReferenceMap.clear();
    }

    public void setUserDevice(String u, String d){
        sDeviceID = d;
        sUserID = u;
    }
    public void setPeriodIndex(int index){ mPeriodIndex = index;}
    public void setPeriodTitles(String[] titles){ period_titles = titles; }
    public void setUseGridDisplay(boolean bUseList) {
        useGrid = bUseList;
    }

}
