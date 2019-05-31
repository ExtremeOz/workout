package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.activity.IMainActivityCallback;
import com.a_track_it.fitdata.model.UserPreferences;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Created by chris.black on 7/2/15.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // Butterknife requires public reference of injected views
public class SettingsFragment extends BaseFragment {

    private IMainActivityCallback mCallback;
    private boolean viewLoaded = false;

    @BindView(R.id.step_toggle)
    ToggleButton step_toggle;

    @BindView(R.id.activity_toggle)
    ToggleButton activity_toggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewLoaded = false;
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_white, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        if (step_toggle == null) step_toggle = (ToggleButton)view.findViewById(R.id.step_toggle);
        step_toggle.setChecked(UserPreferences.getCountSteps(getActivity()));

        if (activity_toggle == null) activity_toggle = (ToggleButton)view.findViewById(R.id.activity_toggle);
        activity_toggle.setChecked(UserPreferences.getActivityTracking(getActivity()));

        viewLoaded = true;
        if(Answers.getInstance() != null) {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("Settings view")
                    .putContentType("View")
                    .putContentId("SettingsFragment"));
        }
        return view;
    }

    @OnCheckedChanged(R.id.step_toggle)
    public void onStepCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(viewLoaded && mCallback != null) {
            mCallback.setStepCounting(isChecked);
        }
    }

    @OnCheckedChanged(R.id.activity_toggle)
    public void onActivityCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(viewLoaded && mCallback != null) {
            mCallback.setActivityTracking(isChecked);
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if(activity instanceof IMainActivityCallback) {
            mCallback = (IMainActivityCallback)activity;
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
}
