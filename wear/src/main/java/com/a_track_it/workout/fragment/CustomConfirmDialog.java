package com.a_track_it.workout.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.HeightAdapter;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;

import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;

public class CustomConfirmDialog extends DialogFragment implements AmbientInterface{
    private static final int POSITIVE_RESULT = 1;
    private static final int NEGATIVE_RESULT = -1;
    private static final String ARG_QUESTION_TYPE = "q_type";
    private static final String ARG_MSG_TEXT = "msg_text";
    private static final String ARG_BTN_SINGLE = "btn_single";
    private static final String LABEL_SESSION = "Session";
    private static long mChronoStart;
    private static int mQuestionType;
    private static String mMessageText;
    private static ICustomConfirmDialog callback;
    private static CustomConfirmDialog instance = null;
    private static boolean singleButton = false;
    private static boolean buttonClicked = false;
    private ColorFilter mImageViewColorFilter;
    private RadioGroup mRadioGroup;
    private UserPreferences userPrefs;
    private Spinner mSpinnerDuration;
    private Spinner mSpinnerExercise;
    private Calendar mCalendar;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private String mMessage2;
    private Workout mWorkout;
    private int ageSelection = 0;
    private boolean bLoading = false;
    public CustomConfirmDialog(){   }
    private View view;

    public static CustomConfirmDialog newInstance(int questionType, String sMessage, ICustomConfirmDialog parentActivity) {
        if (instance == null) instance = new CustomConfirmDialog();
        mQuestionType = questionType;
        mMessageText = sMessage;
        if(parentActivity != null) callback = parentActivity;
        return instance;
    }
    public boolean getButtonClicked(){ return buttonClicked;}
    public void setQuestionType(int type){mQuestionType = type;}
    public void setSingleButton(boolean b){ singleButton = b;}
    public void setMessageText(String s){
        mMessage2 = s;
    }
    public void setWorkout(Workout s){
        mWorkout = s;
        if (mWorkout.start > 0){
            if (mCalendar == null) mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(mWorkout.start);
        }
    }
    public Workout getWorkout(){ return mWorkout;}
    public void setCallback(ICustomConfirmDialog parentActivity){
        callback = parentActivity;
    }
    public void setChronometer(long start){ mChronoStart = start;}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //If don't want toolbar
        try {
            mCalendar = Calendar.getInstance();
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
        }catch (Exception e){
        }

        return dialog;
    }
    final private View.OnClickListener radioClicker = v -> {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();
        // Check which radio button was clicked
        if (checked)
            mRadioGroup.check(v.getId());
        if (bLoading) return;
        switch (v.getId()) {
            case R.id.age_switch1:
                if (checked)
                    ageSelection = 9;
                break;
            case R.id.age_switch2:
                if (checked)
                    ageSelection = 10;
                break;
            case R.id.age_switch3:
                if (checked)
                    ageSelection = 14;
                break;
            case R.id.age_switch4:
                if (checked)
                    ageSelection = 18;
                break;
            case R.id.age_switch5:
                if (checked)
                    ageSelection = 20;
                break;
            case R.id.age_switch6:
                if (checked)
                    ageSelection = 31;
                break;
            case R.id.age_switch7:
                if (checked)
                    ageSelection = 41;
                break;
            case R.id.age_switch8:
                if (checked)
                    ageSelection = 51;
                break;
            case R.id.age_switch9:
                if (checked)
                    ageSelection = 61;
                break;
            case R.id.age_switch10:
                if (checked)
                    ageSelection = 71;
                break;

        }
        if (checked){
            if (userPrefs == null) {
                ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(getContext());
                userPrefs = UserPreferences.getPreferences(getContext(), applicationPreferences.getLastUserID());
            }
            if (userPrefs != null){
                userPrefs.setPrefStringByLabel(Constants.INTENT_PERMISSION_AGE, Integer.toString(ageSelection));
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (userPrefs == null) {
            ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(getContext());
            userPrefs = UserPreferences.getPreferences(getContext(), applicationPreferences.getLastUserID());
        }
        if (mQuestionType == Constants.QUESTION_STORAGE){
            view = inflater.inflate(R.layout.dialog_confirmation, container, false);
            if (mMessageText.length() > 0){
                TextView textView = view.findViewById(R.id.confirm_message);
                if (textView != null) textView.setText(mMessageText);
            }
            ImageButton negButton = view.findViewById(R.id.NegativeButton);
            if (negButton != null){
                negButton.setOnClickListener(view1 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT); getDialog().dismiss();
                });
                if (singleButton) negButton.setVisibility(View.GONE);
                //if (mQuestionType == Constants.QUESTION_KEEP_DELETE) negButton.se
            }
            ImageButton posButton = view.findViewById(R.id.PositiveButton);
            if (posButton != null){
                posButton.requestFocus();
                posButton.setOnClickListener(view12 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT); getDialog().dismiss();
                });
            }
            mImageViewColorFilter = posButton.getColorFilter();
            return view;
        }
        if (mQuestionType == Constants.QUESTION_AGE) {
            bLoading = true;
            String sAgeSet = Constants.ATRACKIT_EMPTY;
            if (userPrefs == null) {
                ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(getContext());
                userPrefs = UserPreferences.getPreferences(getContext(), applicationPreferences.getLastUserID());
            }
            if (userPrefs != null){
               sAgeSet =  userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_AGE);
               if (TextUtils.isDigitsOnly(sAgeSet)) ageSelection = sAgeSet.length();
            }
            view = inflater.inflate(R.layout.dialog_confirmation_age, container, false);
            mRadioGroup = view.findViewById(R.id.radioGroupAge);
            TextView textView = view.findViewById(R.id.confirm_message);
            if (textView != null) textView.setText(getString(R.string.ask_user_age));
            TextView textView2 = view.findViewById(R.id.confirm_message2);
            if (textView2 != null) textView2.setText(getString(R.string.ask_user_age2));
            ImageButton negButton0 = view.findViewById(R.id.NegativeButton);
            if (negButton0 != null) {
                negButton0.setOnClickListener(view1 -> {
                    buttonClicked = true;
                    getDialog().dismiss();
                    if (callback != null)
                        callback.onCustomConfirmButtonClicked(mQuestionType, NEGATIVE_RESULT);
                });
            }
            ImageButton posButton0 = view.findViewById(R.id.PositiveButton);
            if (posButton0 != null) {
                posButton0.requestFocus();
                posButton0.setOnClickListener(view12 -> {
                    if (ageSelection == 0){
                        new Handler(Looper.myLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Intent msgIntent = new Intent(Constants.INTENT_MESSAGE_TOAST);
                                msgIntent.putExtra(INTENT_EXTRA_MSG,"Please select an age");
                                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                getActivity().sendBroadcast(msgIntent);
                            }
                        });
                        return;
                    }

                    buttonClicked = true;
                    getDialog().dismiss();

                    if (callback != null)
                        callback.onCustomConfirmButtonClicked(mQuestionType, POSITIVE_RESULT);
                });
            }
            view.findViewById(R.id.age_switch1).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch2).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch3).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch4).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch5).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch6).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch7).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch8).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch9).setOnClickListener(radioClicker);
            view.findViewById(R.id.age_switch10).setOnClickListener(radioClicker);
            if (sAgeSet.length() > 0){
                int iAge = Integer.parseInt(sAgeSet);
                int iSetId = 0;
                switch (iAge) {
                    case 9:
                        iSetId = R.id.age_switch1;
                        break;
                    case 10:
                        iSetId = R.id.age_switch2;
                        break;
                    case 14:
                        iSetId = R.id.age_switch3;
                        break;
                    case 18:
                        iSetId = R.id.age_switch4;
                        break;
                    case 20:
                        iSetId = R.id.age_switch5;
                        break;
                    case 31:
                        iSetId = R.id.age_switch6;
                        break;
                    case 41:
                        iSetId = R.id.age_switch7;
                        break;
                    case 51:
                        iSetId = R.id.age_switch8;
                        break;
                    case 61:
                        iSetId = R.id.age_switch9;
                        break;
                    case 71:
                        iSetId = R.id.age_switch10;
                        break;
                }
                if (iSetId > 0)
                    mRadioGroup.check(iSetId);
            }
            bLoading = false;
            return view;
        }
        if (mQuestionType == Constants.QUESTION_HEIGHT) {
            if (userPrefs == null) {
                ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(getContext());
                userPrefs = UserPreferences.getPreferences(getContext(), applicationPreferences.getLastUserID());
            }
            bLoading = true;
            view = inflater.inflate(R.layout.dialog_confirmation_height, container, false);
            TextView textView = view.findViewById(R.id.confirm_message);
            if (textView != null) textView.setText(getString(R.string.ask_user_height));
            TextView textView2 = view.findViewById(R.id.confirm_message2);
            if (textView2 != null) textView2.setText(getString(R.string.ask_user_height2));
            ImageButton negButton0 = view.findViewById(R.id.NegativeButton);
            if (negButton0 != null) {
                negButton0.setOnClickListener(view1 -> {
                    buttonClicked = true;
                    getDialog().dismiss();
                    if (callback != null)
                        callback.onCustomConfirmButtonClicked(mQuestionType, NEGATIVE_RESULT);
                });
            }
            ImageButton posButton0 = view.findViewById(R.id.PositiveButton);
            if (posButton0 != null) {
                //posButton0.requestFocus();
                posButton0.setOnClickListener(view12 -> {
                    buttonClicked = true;
                    getDialog().dismiss();
                    if (callback != null)
                        callback.onCustomConfirmButtonClicked(mQuestionType, POSITIVE_RESULT);
                });
            }
            androidx.recyclerview.widget.RecyclerView recyclerView = view.findViewById(R.id.height_list);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
            HeightAdapter heightAdapter = new HeightAdapter(getContext());
            String sHeight = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
            heightAdapter.setOnItemClickListener(new HeightAdapter.OnHeightClickListener() {
                @Override
                public void onItemClick(View view, String viewModel) {
                    if (userPrefs != null)
                        userPrefs.setPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT, viewModel);
                }
            });
            recyclerView.setAdapter(heightAdapter);
            if (sHeight.length() > 0) heightAdapter.setTarget(sHeight);
            bLoading = false;
            textView.requestFocus();
            return view;
        }
        if (mQuestionType == Constants.QUESTION_PICK_EXERCISE){
            view = inflater.inflate(R.layout.dialog_confirmation, container, false);
            if (mMessageText.length() > 0){
                TextView textView = view.findViewById(R.id.confirm_message);
                if (textView != null) textView.setText(mMessageText);
            }
            ImageButton negButton = view.findViewById(R.id.NegativeButton);
            if (negButton != null){
                negButton.setOnClickListener(view1 -> {  if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT);
                    buttonClicked = true; getDialog().dismiss();
                });
            }
            ImageButton posButton = view.findViewById(R.id.PositiveButton);
            if (posButton != null){
                // posButton.requestFocus();
                posButton.setOnClickListener(view12 -> {  if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT);
                    buttonClicked = true; getDialog().dismiss();
                });
            }
            mImageViewColorFilter = posButton.getColorFilter();
            return view;
        }
        if (mQuestionType == Constants.QUESTION_HISTORY_CREATE){
            final String[] durations_array = getResources().getStringArray(R.array.array_rest_duration_gym);
            view = inflater.inflate(R.layout.dialog_history_date_duration, container, false);
            mSpinnerDuration = view.findViewById(R.id.spinner_duration_history);
            mSpinnerExercise = view.findViewById(R.id.spinner_duration_exercise);
            final boolean isGym = (mWorkout != null) && Utilities.isGymWorkout(mWorkout.activityID);
            mSpinnerDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String sDuration = durations_array[position];
                    if (isGym) {
                        mWorkout.rest_duration = Long.parseLong(sDuration) * 1000;  // secs
                        userPrefs.setLongPrefByLabel(Constants.USER_PREF_REST_DURATION, (long)position);
                    }else {
                        mWorkout.duration = Long.parseLong(sDuration) * 60000; // mins
                        userPrefs.setLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION, (long)position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            mSpinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String sDuration = durations_array[position];
                    if (!isGym)
                        userPrefs.setLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION, (long)position);
                    else
                        userPrefs.setLongPrefByLabel(Constants.USER_PREF_GYM_DURATION, (long)position);

                    mWorkout.duration = Long.parseLong(sDuration) * 1000;  // secs
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            if (mCalendar == null) mCalendar = Calendar.getInstance();
            mDatePicker = view.findViewById(R.id.date_picker_history);
            mDatePicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
                mCalendar.set(Calendar.YEAR,year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            });
            mTimePicker = view.findViewById(R.id.time_picker_history);
            mTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
            });
            if (isGym){
                TextView textViewDuration = view.findViewById(R.id.label_duration_history);
                String sMsg = getString(R.string.label_recovery) + Constants.ATRACKIT_SPACE + getString(R.string.label_secs);
                textViewDuration.setText(sMsg);
                int pos = Math.toIntExact(userPrefs.getLongPrefByLabel(Constants.USER_PREF_REST_DURATION));
                if (pos <= mSpinnerDuration.getAdapter().getCount()) mSpinnerDuration.setSelection(pos);
                pos = Math.toIntExact(userPrefs.getLongPrefByLabel(Constants.USER_PREF_GYM_DURATION));
                if (pos <= mSpinnerExercise.getAdapter().getCount()) mSpinnerExercise.setSelection(pos);
            }else{
                view.findViewById(R.id.label_duration_exercise).setVisibility(View.GONE);
                int pos = Math.toIntExact(userPrefs.getLongPrefByLabel(Constants.USER_PREF_SESSION_DURATION));
                if (pos <= mSpinnerDuration.getAdapter().getCount()) mSpinnerDuration.setSelection(pos);
                mSpinnerExercise.setVisibility(View.GONE);
            }
            if (mMessageText.length() > 0){
                TextView textViewTop = view.findViewById(R.id.label_activity_history);
                textViewTop.setText(mMessageText);
            }
            ImageButton negButton = view.findViewById(R.id.NegativeButton);
            if (negButton != null){
                negButton.setOnClickListener(view17 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT);
                });

            }
            ImageButton posButton = view.findViewById(R.id.PositiveButton);
            if (posButton != null){
                posButton.requestFocus();
                posButton.setOnClickListener(view18 -> {buttonClicked = true;
                if (callback != null){
                    if (mWorkout == null) mWorkout = new Workout();
                    mWorkout.start = mCalendar.getTimeInMillis();
                    int position = mSpinnerDuration.getSelectedItemPosition();
                    String sDuration = durations_array[position];
                    long duration = Long.parseLong(sDuration) * (1000*60);
                    if (Utilities.isGymWorkout(mWorkout.activityID)) duration = Long.parseLong(sDuration) * (1000);
                    mWorkout.end = mWorkout.start + duration;
                    mWorkout.duration = duration;
                    callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT);
                }
                });
            }
            return view;
        }
        if ((mQuestionType < Constants.QUESTION_PAUSESTOP) || (mQuestionType >= Constants.QUESTION_LOCATION)){
            view = inflater.inflate(R.layout.dialog_confirmation, container, false);
            if (mMessageText.length() > 0){
                TextView textView = view.findViewById(R.id.confirm_message);
                if (textView != null) textView.setText(mMessageText);
                if (mQuestionType == Constants.QUESTION_NETWORK)
                    textView.setPadding(16, 48,16,0);
            }
            ImageButton negButton = view.findViewById(R.id.NegativeButton);
            if (negButton != null){
                negButton.setOnClickListener(view1 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT); getDialog().dismiss();
                });
                if (singleButton) negButton.setVisibility(View.GONE);
                //if (mQuestionType == Constants.QUESTION_KEEP_DELETE) negButton.se
            }
            ImageButton posButton = view.findViewById(R.id.PositiveButton);
            if (posButton != null){
                posButton.requestFocus();
                posButton.setOnClickListener(view12 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT); getDialog().dismiss();
                });
            }

            mImageViewColorFilter = posButton.getColorFilter();
            return view;
        }
        else {
            if (mQuestionType == Constants.QUESTION_RESUME_END) {
                view = inflater.inflate(R.layout.continueconfirmdialog, container, false);
                MaterialButton negButton = view.findViewById(R.id.button_resume_stop_confirm);
                if (negButton != null){
                    negButton.setOnClickListener(view13 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT); getDialog().dismiss();
                    });
                }
                MaterialButton posButton = view.findViewById(R.id.button_resume_confirm);
                if (posButton != null){
                    posButton.requestFocus();
                    posButton.setOnClickListener(view14 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT); getDialog().dismiss();
                    });
                }
                TextView textView = view.findViewById(R.id.textView_top_confirm);
                if (mMessageText.length() > 0)
                    textView.setText(mMessageText);
                else
                    textView.setText(getString(R.string.action_continue));
                Chronometer chronoView = view.findViewById(R.id.Chronometer_confirm);
                if (mChronoStart > 0) {
                    chronoView.setBase(mChronoStart);
                    chronoView.start();
                }
                return view;
            }
            if (mQuestionType == Constants.QUESTION_PAUSESTOP){
                view = inflater.inflate(R.layout.pausestopconfirmdialog, container, false);
                MaterialButton negButton = view.findViewById(R.id.button_stop_confirm);
                if (negButton != null){
                    negButton.setOnClickListener(view15 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT);getDialog().dismiss();
                    });
                    if (singleButton) negButton.setVisibility(View.GONE);
                }
                MaterialButton posButton = view.findViewById(R.id.button_pause_confirm);
                if (posButton != null){
                    posButton.requestFocus();
                    posButton.setOnClickListener(view16 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT); getDialog().dismiss();
                    });
                }
                Chronometer chronoView = view.findViewById(R.id.Chronometer_pauseconfirm);
                if (mChronoStart > 0) {
                    chronoView.setBase(mChronoStart);
                    chronoView.start();
                }
                return view;
            }
            if (mQuestionType == Constants.QUESTION_DURATION_DELETE){
                view = inflater.inflate(R.layout.deletedurationconfirmdialog, container, false);
                if (mMessage2.length() > 0){
                    TextView textView = view.findViewById(R.id.textViewWorkout);
                    if (textView != null) textView.setText(mMessage2);
                }
                if (mMessageText.length() > 0){
                    TextView textViewTop = view.findViewById(R.id.textView_top_incompleteconfirm);
                    String sTemp = String.valueOf(textViewTop.getText()).concat(" ");
                    if (sTemp.lastIndexOf(LABEL_SESSION) != -1){
                        textViewTop.setText(sTemp.replace(LABEL_SESSION, mMessageText));
                    }
                }
                MaterialButton negButton = view.findViewById(R.id.button_delete_confirm);
                if (negButton != null){
                    negButton.setOnClickListener(view17 -> { buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT); getDialog().dismiss();
                    });

                }
                MaterialButton posButton = view.findViewById(R.id.button_duration_confirm);
                if (posButton != null){
                    posButton.requestFocus();
                    posButton.setOnClickListener(view18 -> {buttonClicked = true; if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT); getDialog().dismiss();
                    });
                }

                return view;
            }
        }
        return null;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mQuestionType == Constants.QUESTION_HEIGHT){
            String sSetting = userPrefs.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
            if ((sSetting != null) && (sSetting.length() > 0)) {
                sSetting += HeightAdapter.CM_TAIL;
                RecyclerView recyclerView = view.findViewById(R.id.height_list);
                List<String> listMetric = ((HeightAdapter) recyclerView.getAdapter()).getListMetric();
                int pos = 0;
                for(String s: listMetric){
                    if (s.equals(sSetting)){
                        Log.i(CustomConfirmDialog.class.getSimpleName(), "found height " + s);
                        final int sSelected = pos;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (recyclerView != null) {
                                    recyclerView.scrollToPosition(sSelected);
                                }else
                                    Log.i(CustomConfirmDialog.class.getSimpleName(), "recycler null ");
                            }
                        });
                        break;
                    }else
                        pos++;
                }
            }

        }

        if (mQuestionType == Constants.QUESTION_HISTORY_CREATE){
            if (mWorkout != null){
                if (mWorkout.duration > 0) {
                    String[] durations_array = getResources().getStringArray(R.array.array_rest_duration_gym);
                    List<String> durations_list = java.util.Arrays.asList(durations_array);
                    String sDuration = Long.toString(mWorkout.duration);
                    int iPos = durations_list.indexOf(sDuration);
                    if (iPos > 0)
                        mSpinnerDuration.setSelection(iPos);
                }
                mDatePicker.setMaxDate(Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY));
                mDatePicker.setSelected(true);
                mTimePicker.setHour(mCalendar.get(Calendar.HOUR_OF_DAY));
                mTimePicker.setMinute(mCalendar.get(Calendar.MINUTE));
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if ((context instanceof ICustomConfirmDialog) && (callback == null)) {
            callback = (ICustomConfirmDialog) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (!buttonClicked && (mQuestionType == Constants.QUESTION_NETWORK || mQuestionType == Constants.QUESTION_AGE)){
            Intent quitIntent = new Intent(Constants.INTENT_QUIT_APP);
            quitIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
            getActivity().sendBroadcast(quitIntent);
        }
        callback.onCustomConfirmDetach();
        callback = null;
    }

 /*   @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_QUESTION_TYPE, mQuestionType);
        outState.putString(ARG_MSG_TEXT, mMessageText);
        outState.putBoolean(ARG_BTN_SINGLE, singleButton);
        super.onSaveInstanceState(outState);
    }*/
    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Context context = getContext();
        int bgColor = ContextCompat.getColor(context, R.color.ambientBackground);
        int foreColor = ContextCompat.getColor(context, R.color.ambientForeground);
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);

        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);
        View rootView = getView();
        if (mQuestionType != 8) {
            if (rootView.findViewById(R.id.confirm_message) != null) {
                TextView textView = rootView.findViewById(R.id.confirm_message);
                textView.setTextColor(foreColor);
/*            Paint textPaint = textViewAddEntry.getPaint();
            textPaint.setAntiAlias(false);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(1);*/
            }
            ImageButton imageButton1 = rootView.findViewById(R.id.NegativeButton);
            imageButton1.getBackground().setColorFilter(filter);
            ImageButton imageButton2 = rootView.findViewById(R.id.PositiveButton);
            imageButton2.getBackground().setColorFilter(filter);
            if (DoBurnInProtection) {
                imageButton1.setVisibility(View.INVISIBLE);
                imageButton2.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void loadDataAndUpdateScreen(){

    }
    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
      //  Log.d(TAG, "CustomLisFragment.onExitAmbient()");
        View rootView = getView();
        Context context = getContext();
        int foreColor = ContextCompat.getColor(context, R.color.primaryTextColor);
        if (mQuestionType != 8) {
            TextView textView = rootView.findViewById(R.id.confirm_message);
            textView.setTextColor(foreColor);
            ImageButton imageButton1 = rootView.findViewById(R.id.NegativeButton);
            imageButton1.setVisibility(View.VISIBLE);
            imageButton1.setColorFilter(mImageViewColorFilter);
            ImageButton imageButton2 = rootView.findViewById(R.id.PositiveButton);
            imageButton2.setVisibility(View.VISIBLE);
            imageButton2.setColorFilter(mImageViewColorFilter);
        }
    }
}