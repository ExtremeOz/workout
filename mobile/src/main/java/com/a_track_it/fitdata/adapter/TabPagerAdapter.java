package com.a_track_it.fitdata.adapter;

import android.os.Parcelable;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.fragment.PageFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris Black
 */
public class TabPagerAdapter extends androidx.fragment.app.FragmentStatePagerAdapter {
    public static final String TAG = "TabPagerAdapter";

    private Map<Integer, Fragment> mPageReferenceMap = new HashMap<>();
    private int mPosition;
    public String filter = Constants.ATRACKIT_EMPTY;
    private boolean useGrid = true;
    private String sUserID;
    private String sDeviceID;
    private static final String[] TITLES = new String[] {
            "Today",
            "Week",
            "Last Week",
            "Month",
            "Last Month",
    };

    public static final int NUM_TITLES = TITLES.length;

    public TabPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
       // Log.d(TAG, "INIT");
    }

    @Override
    public int getCount() {
        return 5; //NUM_TITLES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
            return TITLES[position];
    }

    @Override
    public Fragment getItem(int position) {
        mPosition = position;
        PageFragment myFragment = PageFragment.create(position,TITLES[position],filter, useGrid,sUserID,sDeviceID);
        mPageReferenceMap.put(position, myFragment);
        return myFragment;
    }
    @Override
    public int getItemPosition(Object object) {
        return mPosition;
    }
    @Override
    public Parcelable saveState() {
        Log.d(TAG, "SAVE STATE");
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Log.d(TAG, "RESTORE STATE");
    }

    public void destroy() {
        mPageReferenceMap.clear();
        Log.d(TAG, "DESTROY");
    }

    public Fragment getFragment(int key) {
        PageFragment pf = (PageFragment) mPageReferenceMap.get(key);
        if (pf != null) {
            pf.setFilterText(filter);
        }
        return pf;
    }
    public void setUseGridDisplay(boolean bUseList){
        useGrid = bUseList;
        for (int i=1; i < mPageReferenceMap.size(); i++){
            PageFragment pf = (PageFragment) mPageReferenceMap.get(i);
            if (pf != null) {
                pf.setRecyclerViewLayoutManager(useGrid);
                pf.forceRebuild();
            }
        }
    }
    public void setUserDevice(String u, String d){
        sDeviceID = d;
        sUserID = u;
    }
    public boolean getUseGridDisplay(){ return useGrid;}
}
