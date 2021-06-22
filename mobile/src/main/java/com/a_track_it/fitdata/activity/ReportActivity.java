package com.a_track_it.fitdata.activity;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.fragment.FragmentInterface;

public class ReportActivity extends BaseActivity implements FragmentInterface {
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_report;
    }

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {

    }

    @Override
    public void onItemSelected(int pos, long id, String title, long resid, String identifier) {

    }
}
