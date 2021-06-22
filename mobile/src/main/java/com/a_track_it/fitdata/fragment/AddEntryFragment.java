package com.a_track_it.fitdata.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;




/**
 * Created by Chris Black
 *
 * Input form used to add a_track_it.com manual entry.
 */

public class AddEntryFragment extends Fragment {

    public static final String ARG_ACTIVITY_ID = "ARG_ACTIVITY_ID";
    public static final String TAG = "AddEntryFragment";

    //private DataManager.IDataManager mCallback;
    private Calendar cal = Calendar.getInstance();
    private ReferencesTools mRefTools;

    TextView textviewAddEntry;
    TextView timeTextView;
    TextView dateTextView;
    Spinner activitySpinner;
    com.google.android.material.textfield.TextInputEditText editTextMinutes;
    com.google.android.material.textfield.TextInputEditText editTextSteps;
    TextInputLayout editInputLayout;
    TextInputLayout editInputLayout2;
    TextInputLayout editInputLayoutTime;
    TextView labelText2;
    private int mActivityType;
    int year;
    int month;
    int day;
    int hour;
    int minute;

    public static AddEntryFragment create(int activityType) {
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITY_ID, activityType);
        AddEntryFragment fragment = new AddEntryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityType = getArguments().getInt(ARG_ACTIVITY_ID);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_entry, container, false);

        labelText2 = view.findViewById(R.id.labelText2);
        editInputLayout = view.findViewById(R.id.editInputLayout);
        editInputLayoutTime = view.findViewById(R.id.editInputLayout3);
        editInputLayout2 = view.findViewById(R.id.editInputLayout2);
        editTextMinutes = view.findViewById(R.id.editTextMinutes);
        editTextSteps = view.findViewById(R.id.editTextSteps);
        editTextSteps.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if(!editTextSteps.getText().toString().equals(Constants.ATRACKIT_EMPTY)) {
                    editTextMinutes.setText("" + (int)(Double.parseDouble(editTextSteps.getText().toString()) / 1000.0 * 10));
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        Calendar mCurrentTime = Calendar.getInstance();
        mCurrentTime.add(Calendar.MINUTE, -30);
        year = mCurrentTime.get(Calendar.YEAR);
        month = mCurrentTime.get(Calendar.MONTH);
        day = mCurrentTime.get(Calendar.DAY_OF_MONTH);
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE);
        cal.set(year, month, day, hour, minute);
        timeTextView = view.findViewById(R.id.timeTextView);
        timeTextView.setText(Utilities.getTimeString(cal.getTimeInMillis()));
        timeTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getActivity(),R.style.ThemeOverlay_MaterialComponents_MaterialCalendar, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            hour = selectedHour;
                            minute = selectedMinute;
                            cal.set(year, month, day, hour, minute);
                            timeTextView.setText(Utilities.getTimeString(cal.getTimeInMillis()));
                            textviewAddEntry.setText(Utilities.getPartOfDayString(cal.getTimeInMillis()));
                        }
                    }, hour, minute, false);
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                    if (editTextSteps.getVisibility() == View.VISIBLE) {
                        editTextSteps.requestFocus();
                    } else {
                        editTextMinutes.requestFocus();
                    }
                }
            });
        dateTextView = view.findViewById(R.id.dateTextView);
        dateTextView.setText(Utilities.getDateString(cal.getTimeInMillis()));
        dateTextView.setOnClickListener(v -> {
            DatePickerDialog mTimePicker;
            mTimePicker = new DatePickerDialog(getActivity(),R.style.MyDatePickerStyle, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view1, int selectedYear, int monthOfYear, int dayOfMonth) {
                    day = dayOfMonth;
                    year = selectedYear;
                    month = monthOfYear;
                    cal.set(year, month, day, hour, minute);
                    dateTextView.setText(Utilities.getDateString(cal.getTimeInMillis()));
                }
            }, year,  month, day);
            mTimePicker.setTitle("Select Date");
            mTimePicker.show();
        });
        textviewAddEntry = view.findViewById(R.id.textViewAddEntry);
        textviewAddEntry.setText(Utilities.getPartOfDayString(cal.getTimeInMillis()));
        activitySpinner = view.findViewById(R.id.activitySpinner);
        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        if (mActivityType > 0) {
            if (mRefTools == null) {
                mRefTools = ReferencesTools.getInstance();
                mRefTools.init(getContext());
            }
            int iPos = mRefTools.getActivityListIndexById(mActivityType);
            if (iPos > 0){
                activitySpinner.setSelection(iPos);
                updateView();
            }

        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT, AddEntryFragment.class.getSimpleName());
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(mActivityType));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        return view;
    }

    private void updateView() {
        int selectedIndex = activitySpinner.getSelectedItemPosition();
        switch (selectedIndex) {
            case 0:
            case 1:
            case 3:
            case 6:
            case 8:
            case 9:
                editInputLayout2.setVisibility(View.VISIBLE);
                labelText2.setVisibility(View.VISIBLE);
                break;
            case 2:
            case 4:
            case 5:
            case 7:
            case 10:
            case 11:
                editInputLayout2.setVisibility(View.GONE);
                labelText2.setVisibility(View.GONE);
                break;
            default:
                editInputLayout2.setVisibility(View.VISIBLE);
                labelText2.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
//        if(activity instanceof DataManager.IDataManager) {
//            mCallback = (DataManager.IDataManager)activity;
//        }
    }

    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
      //  mCallback = null;
    }





    public Workout getWorkout() {
        // need validation
        Workout workout = new Workout();

        cal.set(year, month, day, hour, minute);
        long startTime = cal.getTimeInMillis();
        workout.start = startTime;
        workout._id = (startTime);
        cal.add(Calendar.MINUTE, Integer.parseInt(editTextMinutes.getText().toString()));
        workout.duration = cal.getTimeInMillis() - startTime;
        workout.stepCount = Integer.parseInt(editTextSteps.getText().toString());

        int selectedIndex = activitySpinner.getSelectedItemPosition();
        String sRet = mRefTools.getActivityListInfoByIndex(0,selectedIndex);
        try {
            workout.activityID = Long.parseLong(sRet);
        }catch (NumberFormatException nfe){
            workout.activityID = Constants.WORKOUT_TYPE_UNKNOWN;
        }
        switch (selectedIndex) {
            case (int)Constants.WORKOUT_TYPE_WALKING:
            case 5:
            case 4:
            case 2:
                workout.stepCount = 0;
                break;
        }
        workout.wattsTotal = 0F;
        workout.weightTotal = 0F;
        workout.repCount = 0;
        workout.setCount = 0;
        if (workout.activityID == Constants.WORKOUT_TYPE_WALKING) {
            if ((workout.stepCount / 1000) * 10 > workout.duration / (1000 * 60)) {
                workout = null;
                editInputLayout2.setError("Maximum of 1000 steps per 10 minutes walking");
            }
        }

        long timeMs = System.currentTimeMillis();
        cal.setTimeInMillis(timeMs);
        if (workout != null && workout.start + workout.duration > cal.getTimeInMillis()) {
            workout = null;
            editInputLayoutTime.setError("Can't add entry in the future");
        }

        return workout;
    }
}
