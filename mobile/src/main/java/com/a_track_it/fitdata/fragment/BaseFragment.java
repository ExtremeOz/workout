package com.a_track_it.fitdata.fragment;

import androidx.fragment.app.Fragment;


/**
 * Created by Chris Black
 *
 * Contains functionality common to all Fragments. Code here should be kept to the bare
 * minimum.
 */
public abstract class BaseFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}