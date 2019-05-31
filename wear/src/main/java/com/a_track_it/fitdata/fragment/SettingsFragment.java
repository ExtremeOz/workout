package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.model.UserPreferences;

/**
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";
    public static final float FACTOR = 0.146467f; // c = a * sqrt(2)
    private Context mContext;
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        final SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        
        ToggleButton confirm_start_toggle = rootView.findViewById(R.id.confirm_start_toggle);
        confirm_start_toggle.setChecked(UserPreferences.getConfirmStartSession(mContext));
        confirm_start_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setConfirmStartSession(mContext, b);
            }
        });
        ToggleButton confirm_end_toggle = rootView.findViewById(R.id.confirm_end_toggle);
        confirm_end_toggle.setChecked(UserPreferences.getConfirmEndSession(mContext));
        confirm_end_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setConfirmEndSession(mContext, b);
            }
        });
        ToggleButton confirm_dismiss_toggle = rootView.findViewById(R.id.confirm_dismiss_toggle);
        confirm_dismiss_toggle.setChecked(UserPreferences.getConfirmDismissSession(mContext));
        confirm_dismiss_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setConfirmDismissSession(mContext, b);
            }
        });
        ToggleButton confirm_sensors_toggle = rootView.findViewById(R.id.use_sensors_toggle);
        confirm_sensors_toggle.setChecked(UserPreferences.getConfirmUseSensors(mContext));
        confirm_sensors_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setConfirmUseSensors(mContext, b);
            }
        });
        ToggleButton use_keys_toggle = rootView.findViewById(R.id.use_kg_toggle);
        use_keys_toggle.setChecked(UserPreferences.getUseKG(mContext));
        use_keys_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setUseKG(mContext, b);
            }
        });
        ToggleButton offline_toggle = rootView.findViewById(R.id.work_offline_toggle);
        offline_toggle.setChecked(UserPreferences.getWorkOffline(mContext));
        offline_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UserPreferences.setWorkOffline(mContext, b);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Log.d(TAG, "onEnterAmbient() " + ambientDetails);

        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);

    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "onExitAmbient()");

        //mImageView.setColorFilter(mImageViewColorFilter);
    }
}
