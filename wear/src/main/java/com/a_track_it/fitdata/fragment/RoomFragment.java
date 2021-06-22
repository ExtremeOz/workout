package com.a_track_it.fitdata.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.WorkoutAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.UnitLocale;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.VectorDrawableUtils;
import com.a_track_it.fitdata.common.data_model.ATrackItLatLng;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.DailyCounter;
import com.a_track_it.fitdata.common.data_model.ResistanceType;
import com.a_track_it.fitdata.common.data_model.SensorDailyTotals;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.LiveDataTimerViewModel;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.circularreveal.CircularRevealCompat;
import com.google.android.material.circularreveal.CircularRevealWidget;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_DRAWABLE;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.REPS_TAIL;
import static com.a_track_it.fitdata.common.Constants.SINGLE_INT;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_CALL_TO_LINE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_COMPLETED;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_INVALID;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_LIVE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_PAUSED;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_PENDING;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_SETUP;
import static java.text.DateFormat.getTimeInstance;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInterface} interface
 * to handle interaction events.
 * Use the {@link RoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomFragment extends Fragment implements AmbientInterface{
    private static final String LOG_TAG = RoomFragment.class.getSimpleName();
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private LiveDataTimerViewModel mTimerViewModel;
    private MaterialCardView mCardView;
    private ScrollView mScrollView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private LinearLayout mConstraintLayout;
    private ConstraintLayout mCardConstraint;
    private FragmentInterface mListener;
    private ColorFilter mImageViewColorFilter;
    private MaterialButton btn_home;
    private TextView textViewMsgLeft;
    private TextView textViewMsgCenterLeft;
    private Chronometer chronometerClock;
    private TextView textViewMsgCenterRight;
    private TextView textViewCenter;
    private TextView textViewCenter1;
    private TextView textViewCenter3;
    private TextView textViewMsgRight;
    private TextView textViewCenter2;
    private Chronometer chronometerViewCenter;
    private TextView textViewMsgBottomLeft;
    private TextView textViewMsgBottomRight;
    private TextView textViewBottom;
/*    private Animation animationRotate;
    private Animation animationBlink;*/
    private int shortAnimationDuration;
    private androidx.wear.widget.WearableRecyclerView mRecyclerView;
    private WorkoutAdapter mWorkoutAdapter;
    private boolean isAmbient;
    private boolean prevAmbient;
//    private boolean IsLowBitAmbient;
//    private boolean DoBurnInProtection;
    private boolean bDeviceConnected = false;
    private boolean bUseKg = true;
    boolean bShowGoals;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private List<ResistanceType> resistanceTypeList = new ArrayList<>();
    private Configuration configHeartPts = null;
    private Configuration configMoveMins = null;
    private Configuration configStepCount = null;
    private int colorDayNight;
    private int colorConnected;
    private int colorAmbient;
    private int sensorCountBPM;
    private int sensorCountSteps;
    private int sensorCountPressure;
    private int sensorCountTemp;
    private int sensorCountHumidity;
    private int sensor2CountBPM;
    private int sensor2CountSteps;
    private int sensor2CountPressure;
    private int sensor2CountTemp;
    private int sensor2CountHumidity;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private Resources res;
    long timeMax;
    long timeMin;

    private int resId_Device;
    private int resId_BPM_fit;
    private int resId_BPM_device;
    private int resId_BPM_device2;
    private int resId_Step_fit;
    private int resId_Step_device;
    private int resId_Step_device2;
    private int resId_Location;
    private int resId_Motion;
    private int resId_Barometer;
    private int resId_Speed;
    private int resId_Temperature;
    private int resId_Humidity;
    private int resId_Altitude;

    public RoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoomFragment.
     */

    public static RoomFragment newInstance() {
        RoomFragment fragment = new RoomFragment();
        return fragment;
    }
    final View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           if (v.getTag() != null){
               mListener.OnFragmentInteraction((int)v.getTag(), 0, null);
           }
        }
    };
    final View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, Constants.LABEL_LONG);
            return true;
        }
    };
    final Chronometer.OnChronometerTickListener tickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            if ((chronometer.getText() != null) && (chronometer.getText().length() > 8)){
                chronometer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            }else
                chronometer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        }
    };
    private class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
        private static final float MAX_ICON_PROGRESS = 0.65f;
        private float mProgressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);
            if (!Float.isNaN(mProgressToCenter)) {
                child.setScaleX(1 - mProgressToCenter);
                child.setScaleY(1 - mProgressToCenter);
            }
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            timeMin = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            timeMax = Utilities.getTimeFrameEnd(Utilities.TimeFrame.BEGINNING_OF_DAY);

        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }
    private void bindFragment(View rootView){
        mConstraintLayout = rootView.findViewById(R.id.main_constraint);
        mCardView =  rootView.findViewById(R.id.home_card);
        mCardConstraint = mCardView.findViewById(R.id.card_constraint);
        mScrollView = rootView.findViewById(R.id.scrollview_home);
        btn_home = rootView.findViewById(R.id.button_home_start);
        btn_home.setTag(Constants.UID_btnHomeStart);
        textViewMsgLeft = rootView.findViewById(R.id.textViewMsgLeft);
        textViewMsgLeft.setTag(Constants.UID_textViewMsgLeft);
        textViewMsgCenterLeft = rootView.findViewById(R.id.textViewCenterLeft);
        textViewMsgCenterLeft.setTag(Constants.UID_textViewMsgCenterLeft);
        chronometerClock = rootView.findViewById(R.id.chronoClock);
        chronometerClock.setTag(Constants.UID_chronoClock);
        chronometerClock.setOnChronometerTickListener(tickListener);
        chronometerClock.setOnClickListener(myClicker);
        textViewMsgCenterRight = rootView.findViewById(R.id.textViewCenterRight);
        textViewMsgCenterRight.setTag(Constants.UID_textViewMsgCenterRight);
        textViewMsgRight = rootView.findViewById(R.id.textViewMsgRight);
        textViewMsgRight.setTag(Constants.UID_textViewMsgRight);
        textViewCenter = rootView.findViewById(R.id.textViewCenter);
        textViewCenter.setTag(Constants.UID_textViewCenter);
        textViewCenter1 = rootView.findViewById(R.id.textViewCenter1);
        textViewCenter1.setTag(Constants.UID_textViewCenter1);
        textViewCenter3 = rootView.findViewById(R.id.textViewCenter3);
        textViewCenter3.setTag(Constants.UID_textViewCenter3);
        textViewCenter2 = rootView.findViewById(R.id.textViewCenter2);
        textViewCenter2.setTag(Constants.UID_textViewCenter2);
        chronometerViewCenter = rootView.findViewById(R.id.chronoViewCenter);
        chronometerViewCenter.setVisibility(Chronometer.GONE);
        chronometerViewCenter.setTag(Constants.UID_chronometerViewCenter);
        chronometerViewCenter.setOnClickListener(myClicker);
        chronometerViewCenter.setOnChronometerTickListener(tickListener);
        textViewMsgBottomLeft = rootView.findViewById(R.id.textViewMsgBottomLeft);
        textViewMsgBottomLeft.setTag(Constants.UID_textViewMsgBottomLeft);
        textViewMsgBottomRight = rootView.findViewById(R.id.textViewMsgBottomRight);
        textViewMsgBottomRight.setTag(Constants.UID_textViewMsgBottomRight);
        textViewBottom = rootView.findViewById(R.id.textViewBottom);
        textViewBottom.setTag(Constants.UID_textViewBottom);
        mRecyclerView = rootView.findViewById(R.id.home_recycle_view);
        mImageView = rootView.findViewById(R.id.home_image_view);
        mImageView.setTag(Constants.UID_home_image_view);
        mProgressBar = rootView.findViewById(R.id.home_progress_view);
        mProgressBar.setTag(Constants.UID_home_image_view);
        mProgressBar.setOnClickListener(myClicker);
        mImageView.setOnClickListener(myClicker);
        mImageView.setOnLongClickListener(v -> {
            mListener.OnFragmentInteraction(Constants.UID_chronoClock,0,"long");
            return false;
        });
        btn_home.setOnClickListener(myClicker);
        btn_home.setOnLongClickListener(v -> {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),1,null);
            return false;
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Context context = (getContext() != null) ? getContext().getApplicationContext() : container.getContext().getApplicationContext();
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(LiveDataTimerViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(context);
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        resistanceTypeList = mSessionViewModel.getResistanceTypeList();
        appPrefs = ApplicationPreferences.getPreferences(context);
        res = context.getResources();
        if ((appPrefs != null) && (appPrefs.getLastUserID().length() > 0)) {
            userPrefs = UserPreferences.getPreferences(getContext(), appPrefs.getLastUserID());
            if (userPrefs != null){
                bUseKg = userPrefs.getUseKG();
                bShowGoals = userPrefs.getPrefByLabel(Constants.USER_PREF_SHOW_GOALS);
            } else{
                bUseKg = (UnitLocale.getDefault() == UnitLocale.Metric);
                bShowGoals = false;
            }
        }else {
            bUseKg = (UnitLocale.getDefault() == UnitLocale.Metric);
            bShowGoals = false;
        }
        if (resistanceTypeList.size() == 0)
            resistanceTypeList = mSessionViewModel.getResistanceTypeList();
        bindFragment(rootView);

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = res.getInteger(
                android.R.integer.config_shortAnimTime);
        isAmbient = false;
        prevAmbient = false;

        CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
        WearableLinearLayoutManager mWearableLinearLayoutManager = new WearableLinearLayoutManager(context, customScrollingLayoutCallback);
        mRecyclerView.setLayoutManager(mWearableLinearLayoutManager);
        mRecyclerView.setCircularScrollingGestureEnabled(false);
        mRecyclerView.setBezelFraction(0.5f);
        mRecyclerView.setScrollDegreesPerScreen(90);

        // To align the edge children (first and last) with the center of the screen
        mRecyclerView.setEdgeItemsCenteringEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mWorkoutAdapter = new WorkoutAdapter(mConstraintLayout.getContext(),null,null, true);
        mWorkoutAdapter.setListType(false);
        mRecyclerView.setAdapter(mWorkoutAdapter);
        AssetManager asm = context.getAssets();
        if (asm != null) {
            Typeface typeface = Typeface.createFromAsset(asm, Constants.ATRACKIT_FONT);
            if (typeface != null) {
                btn_home.setTypeface(typeface);
                chronometerClock.setTypeface(typeface);
                textViewCenter.setTypeface(typeface);
                textViewCenter1.setTypeface(typeface);
                textViewCenter3.setTypeface(typeface);
                textViewBottom.setTypeface(typeface);
                textViewBottom.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
        }
        colorDayNight = context.getColor(R.color.primaryTextColor);
        colorConnected = context.getColor(R.color.primaryLightColor);
        colorAmbient = context.getColor(R.color.ambientForeground);
        sensorCountBPM = appPrefs.getBPMSensorCount();
        sensorCountSteps = appPrefs.getStepsSensorCount();
        sensorCountPressure = appPrefs.getPressureSensorCount();
        sensorCountTemp = appPrefs.getTempSensorCount();
        sensorCountHumidity = appPrefs.getHumiditySensorCount();
        sensor2CountBPM = appPrefs.getBPM2SensorCount();
        sensor2CountSteps = appPrefs.getSteps2SensorCount();
        sensor2CountPressure = appPrefs.getPressure2SensorCount();
        sensor2CountTemp = appPrefs.getTemp2SensorCount();
        sensor2CountHumidity = appPrefs.getHumidity2SensorCount();
        final String sBPM_fit = getString(R.string.sensor_fit_bpm);
        final String sBPM_device = getString(R.string.sensor_device_bpm);
        final String sBPM_device2 = getString(R.string.sensor_device2_bpm);
        final String sStep_fit = getString(R.string.sensor_fit_steps);
        final String sStep_device = getString(R.string.sensor_device_steps);
        final String sStep_device2 = getString(R.string.sensor_device2_steps);
        final String sLocation_fit = getString(R.string.nav_location).toLowerCase();
        final String sMotion_fit = getString(R.string.nav_motion).toLowerCase();
        final String sBarometer_device = getString(R.string.sensor_device_pressure);
        final String sStatus_device = getString(R.string.nav_status).toLowerCase();
        final String sBattery_device = getString(R.string.sensor_device_battery);
        final String sDistance_fit = getString(R.string.sensor_fit_distance);
        final String sAltitude_fit = getString(R.string.sensor_fit_altitude);
        final String sSpeed_distance = getString(R.string.sensor_device_speed);
        final String sTemperature_device = getString(R.string.sensor_device_temperature);
        final String sHumidity_device = getString(R.string.sensor_device_humidity);
        resId_Device = res.getIdentifier("ic_phone_android",Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_BPM_fit = res.getIdentifier("ic_heart_solid",Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_BPM_device = res.getIdentifier("ic_heart_outline",Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_BPM_device2 = res.getIdentifier("ic_phone_receive_white",Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Step_fit = res.getIdentifier("ic_footsteps_silhouette_variant",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Step_device = res.getIdentifier("ic_footsteps_outline_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Step_device2 = res.getIdentifier("ic_smartphone_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Location = res.getIdentifier("ic_placeholder", Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Motion = res.getIdentifier("ic_motion_white", Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Barometer = res.getIdentifier("ic_barometer", ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Speed = res.getIdentifier("ic_speed_white", ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Temperature = res.getIdentifier("ic_temperature", ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Humidity = res.getIdentifier("ic_humidity_white", ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        resId_Altitude = res.getIdentifier("ic_mountain_white",Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);

        textViewMsgLeft.setOnClickListener(v -> {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),0,null);
            int iType =  mSavedStateViewModel.getViewState(2);
            if (iType == 2){
                iType = 0;
            }
            else{
                if (iType == 1){
                    if ((userPrefs != null) && userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE))
                        iType = 2;
                    else{
                        if (sensorCountBPM > 0)
                            iType = 0;
                    }
                }else{
                    if (sensorCountBPM > 0)
                        iType = 1;
                    else
                        if ((userPrefs != null) && userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE) && (sensor2CountBPM > 0))
                        iType = 2;
                }
            }

            int resId = resId_BPM_fit;
            String sValue = Constants.ATRACKIT_EMPTY;
            Drawable drawable = null;
            Drawable drawableDevice = null;
            String sBroadcast = ATRACKIT_EMPTY;
            final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            if (iType == 1){
                resId = resId_BPM_device;
                if (mMessagesViewModel.getDeviceBpmMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getDeviceBpmMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                } else
                    sValue = getString(R.string.label_na);
                sBroadcast = sBPM_device + ATRACKIT_SPACE + sValue;
            }
            if (iType == 0) {
                resId = resId_BPM_fit;
                if (mMessagesViewModel.getBpmMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getBpmMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                } else
                    sValue = getString(R.string.label_na);
                sBroadcast = sBPM_fit + ATRACKIT_SPACE + sValue;
            }
            if (iType == 2){
                resId = resId_BPM_device2;
                if (mMessagesViewModel.getDevice2BpmMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getDevice2BpmMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                } else
                    sValue = getString(R.string.label_na);
                sBroadcast = sBPM_device2 + ATRACKIT_SPACE + sValue;
            }
            mSavedStateViewModel.setViewState(2,iType);
            if (resId > 0) {
                drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                        ,R.dimen.home_button_icon_size,context);
                drawableDevice = VectorDrawableUtils.loadVectorDrawableWithTint(resId_Device, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                        ,R.dimen.home_button_icon_size,context);
            }
            broadcastToast(sBroadcast);
            final Drawable finalDrawable = (resId > 0) ? drawable : null;
            final Drawable finalDrawableDevice = (resId > 0) ? drawableDevice : null;
            final String sMsg = sValue;
            new Handler(Looper.getMainLooper()).post(() -> {
                textViewMsgLeft.setCompoundDrawablesWithIntrinsicBounds(null,null,finalDrawable,null);
                if ((sMsg != null) && (sMsg.length() > 0)) {
                    textViewMsgLeft.setText(sMsg);
                }
                textViewMsgLeft.setCompoundDrawablePadding(6);
                textViewMsgLeft.setTextColor(foreColor);
            });
        });
        textViewMsgRight.setOnClickListener(v -> {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),0,null);
            int iType = mSavedStateViewModel.getViewState(3);
            if (iType == 2){
                    iType = 0;
            }
            else{
                if (iType == 1){
                    if ((userPrefs != null) && userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE))
                        iType = 2;
                    else{
                        if (sensorCountSteps > 0)
                            iType = 0;
                    }
                }else{
                    if (sensorCountSteps > 0)
                        iType = 1;
                    else
                    if ((userPrefs != null) && userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE) && (sensor2CountSteps > 0))
                        iType = 2;
                }
            }
            int resId = resId_Step_device;
            String sValue = getString(R.string.label_na);
            String sBroadcast = ATRACKIT_EMPTY;
            if (iType == 0){
                resId = resId_Step_device;
                if (mMessagesViewModel.getDeviceStepsMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getDeviceStepsMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                }
                sBroadcast = sStep_device + ATRACKIT_SPACE + sValue;
            }
            if (iType == 1) {
                resId = resId_Step_fit;
                if (mMessagesViewModel.getStepsMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getStepsMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                }

                sBroadcast = sStep_fit + ATRACKIT_SPACE + sValue;
            }
            if (iType == 2){
                resId = resId_Step_device2;
                if (mMessagesViewModel.getDevice2StepsMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getDevice2StepsMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                }
                sBroadcast = sStep_device2 + ATRACKIT_SPACE + sValue;
            }
            broadcastToast(sBroadcast);
            final String sMsg = sValue;
            mSavedStateViewModel.setViewState(3,iType);
            Drawable drawable = null;
            if (resId > 0) {
                drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                        ,R.dimen.home_button_icon_size,context);
            }
            final Drawable finalDrawable = (resId > 0) ? drawable : null;

            new Handler(Looper.getMainLooper()).post(() -> {
                textViewMsgRight.setCompoundDrawablesWithIntrinsicBounds(finalDrawable,null,null,null);
                textViewMsgRight.setCompoundDrawablePadding(6);
                if ((sMsg != null) && (sMsg.length() > 0)) textViewMsgRight.setText(sMsg);
            });
        });
        textViewCenter.setOnClickListener(myClicker);
        textViewCenter1.setOnClickListener(v -> {
            String sValue = getString(R.string.label_na);
            String sBroadcast = ATRACKIT_EMPTY;
            if (mMessagesViewModel.getHeartPtsMsg().getValue() != null){
                sValue = mMessagesViewModel.getHeartPtsMsg().getValue();
            }
            sBroadcast = getString(R.string.label_heart_pts) + ATRACKIT_SPACE + sValue;
            broadcastToast(sBroadcast);
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, ((TextView)v).getText().toString());
        });
        textViewCenter3.setOnClickListener(v -> {
            broadcastToast(getString(R.string.label_move_mins));
            String sValue = getString(R.string.label_na);
            String sBroadcast = ATRACKIT_EMPTY;
            if (mMessagesViewModel.getMoveMinsMsg().getValue() != null){
                sValue = mMessagesViewModel.getMoveMinsMsg().getValue();
            }
            sBroadcast = getString(R.string.label_move_mins) + ATRACKIT_SPACE + sValue;
            broadcastToast(sBroadcast);
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, ((TextView)v).getText().toString());
        });
        textViewMsgCenterLeft.setOnClickListener(v -> {
            String sValue = getString(R.string.label_na);
            String sBroadcast = ATRACKIT_EMPTY;
            if (mMessagesViewModel.getCaloriesMsg().getValue() != null) sValue = mMessagesViewModel.getCaloriesMsg().getValue();

            sBroadcast = getString(R.string.sensor_fit_calories) + ATRACKIT_SPACE + sValue;
            broadcastToast(sBroadcast);
        });
        textViewMsgCenterRight.setOnClickListener(v -> {
            String sValue = getString(R.string.label_na);
            String sBroadcast = ATRACKIT_EMPTY;
            if (mMessagesViewModel.getDistanceMsg().getValue() != null) sValue = mMessagesViewModel.getDistanceMsg().getValue();
            sBroadcast = getString(R.string.sensor_fit_distance) + ATRACKIT_SPACE + sValue;
            broadcastToast(sBroadcast);

        });
        boolean bUseLoc = appPrefs.getUseLocation();
        mSavedStateViewModel.setViewState(9, (bUseLoc) ? 0 : 1);
        if (bUseLoc)
            setLocationText(true);
        else
            textViewCenter2.setVisibility(View.GONE);
        textViewCenter2.setOnLongClickListener(myLongClicker);
        textViewCenter2.setOnClickListener(v -> {
            int currentState = mSavedStateViewModel.getState();
            //  ONLY WORKOUT_PENDING states
            int resId = 0; String sValue = getString(R.string.label_na);
            int iType = mSavedStateViewModel.getViewState(9);
            if ((currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_LIVE) && (iType > DetectedActivity.STILL)) iType = 1;
            if (iType == 0 && bUseLoc) iType = 1;
            else
            if (iType == 1 || !bUseLoc) iType = 2;
            else if (iType == 2) iType = 0;
            mSavedStateViewModel.setViewState(9,iType);
            String sBroadcast = ATRACKIT_EMPTY;
            if ((currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_LIVE)) {
                mListener.OnFragmentInteraction(Constants.UID_textViewCenter2,0,null);
                if (iType == 1) {
                    resId = resId_Location;
                    sValue = mSavedStateViewModel.getLocationAddress();
                    sBroadcast = sLocation_fit + ATRACKIT_SPACE + sValue;
                }
                if (iType == 0){
                        resId = resId_Motion;
                        if (mMessagesViewModel.getActivityMsg().getValue() != null)
                            sValue = mMessagesViewModel.getActivityMsg().getValue();
                        else
                            sValue = getString(R.string.label_unknown);
                        sBroadcast = sMotion_fit + ATRACKIT_SPACE + sValue;
                }
                if (iType == 2) {
                    sValue = Utilities.currentStateTitle(context, currentState);
                    sBroadcast = sStatus_device + ATRACKIT_SPACE + sValue;
                }
            }
            else {
                if ((mWorkout == null) && mSavedStateViewModel.isSessionSetup()) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if ((mWorkoutSet == null) && mSavedStateViewModel.isSessionSetup()) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mWorkout != null) {
                    if (! mSavedStateViewModel.getIsGym()) {
                        sValue = mWorkout.activityName;
                    } else {
                        if (mWorkoutSet != null && mWorkoutSet.exerciseName != null)
                            sValue = mWorkoutSet.exerciseName;
                        else
                            sValue = mWorkout.activityName;
                    }
                    sBroadcast = sValue;
                    resId = 0; //Math.toIntExact(mSavedStateViewModel.getIconID());
                }
            }
            broadcastToast(sBroadcast);
            Drawable drawable = null;
            if (resId > 0) {
                drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                        ,R.dimen.home_button_icon_size,context);
            }
            mSavedStateViewModel.setViewState(9,iType);
            final String sMsg = sValue;
            final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            final Drawable finalDrawable = (resId > 0) ? drawable : null;
            new Handler(Looper.getMainLooper()).post(() -> {
                textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(finalDrawable,null,null,null);
                textViewCenter2.setCompoundDrawablePadding(6);
                if ((sMsg != null) && (sMsg.length() > 0)) textViewCenter2.setText(sMsg);
                textViewCenter2.setTextColor(foreColor);
            });
        });
        textViewMsgBottomRight.setOnClickListener(v -> {
            int currentState = mSavedStateViewModel.getState();
            if ((currentState == WORKOUT_LIVE)||(currentState == WORKOUT_PAUSED)){
                /*textViewMsgBottomRight.post(() -> {
                    textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                });*/
            }else {
                int iType = mSavedStateViewModel.getViewState(11);
                int iIndex;
                if (iType == 6) iIndex = 0; else iIndex = iType + 1;
                while (iIndex != iType){
                    if (iIndex == 0 && sensorCountPressure > 0) break;
                    if (iIndex == 1 && sensorCountTemp > 0) break;
                    if (iIndex == 2 && sensorCountHumidity > 0) break;
                    if (iIndex == 3 && sensor2CountPressure > 0) break;
                    if (iIndex == 4 && sensor2CountHumidity > 0) break;
                    if (iIndex == 5 && sensor2CountTemp > 0) break;
                    if (iIndex == 6 && mMessagesViewModel.getSpeedMsg().getValue() != null) break;
                    if (iIndex == 6) iIndex = 0; else iIndex++;
                }
                iType = iIndex;
                mSavedStateViewModel.setViewState(11,iIndex);
                int resId = resId_Barometer;
                String sValue = getString(R.string.label_not_available0);
                Drawable drawable = null;
                Drawable drawableDevice = null;
                final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
                String sBroadcast = ATRACKIT_EMPTY;
                final String sNodeName = (mMessagesViewModel.getNodeDisplayName().getValue() != null) ? mMessagesViewModel.getNodeDisplayName().getValue() : ATRACKIT_EMPTY;
                if (iType == 0) {
                    sValue = ATRACKIT_EMPTY;
                    resId = resId_Barometer;
                    if (mMessagesViewModel.getPressureMsg().getValue() != null) {
                        sValue = mMessagesViewModel.getPressureMsg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = getString(R.string.label_na);
                        else
                            sValue += Constants.ATRACKIT_SPACE + getString(R.string.label_pressure_hpa);
                    }
                    sBroadcast = sBarometer_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 1){
                    resId = resId_Temperature;
                    sValue = ATRACKIT_EMPTY;
                    if (mMessagesViewModel.getTemperatureMsg().getValue() != null) {
                        sValue = mMessagesViewModel.getTemperatureMsg().getValue();
                        if (!Utilities.isFloat(sValue, 0))
                            sValue = getString(R.string.label_na);
                        else
                            sValue += Constants.ATRACKIT_SPACE + getString(R.string.label_temperature_celcius);
                    }
                    sBroadcast = sTemperature_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 2){
                    resId = resId_Humidity;
                    sValue = ATRACKIT_EMPTY;
                    if (mMessagesViewModel.getHumidityMsg().getValue() != null) {
                        sValue = mMessagesViewModel.getHumidityMsg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = getString(R.string.label_na);
                        else
                            sValue += Constants.ATRACKIT_SPACE + getString(R.string.label_relative_humidity);
                    }
                    sBroadcast = sHumidity_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 3) {
                    sValue = ATRACKIT_EMPTY;
                    resId = resId_Barometer;
                    if (mMessagesViewModel.getPressure2Msg().getValue() != null) {
                        sValue = mMessagesViewModel.getPressure2Msg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = ATRACKIT_SPACE + getString(R.string.label_na);
                        else
                            sValue += Constants.ATRACKIT_SPACE + getString(R.string.label_pressure_hpa);
                    }
                    sBroadcast = sNodeName + ATRACKIT_SPACE + sBarometer_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 4){
                    resId = resId_Temperature;
                    sValue = ATRACKIT_EMPTY;
                    if (mMessagesViewModel.getTemperature2Msg().getValue() != null) {
                        sValue = mMessagesViewModel.getTemperature2Msg().getValue();
                        if (!Utilities.isFloat(sValue, 0))
                            sValue = getString(R.string.label_na);
                        else
                            sValue = sNodeName + ATRACKIT_SPACE + sValue + Constants.ATRACKIT_SPACE + getString(R.string.label_temperature_celcius);
                    }
                    sBroadcast = sNodeName + ATRACKIT_SPACE + sTemperature_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 5){
                    resId = resId_Humidity;
                    sValue = ATRACKIT_EMPTY;
                    if (mMessagesViewModel.getHumidity2Msg().getValue() != null) {
                        sValue = mMessagesViewModel.getHumidity2Msg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = getString(R.string.label_na);
                        else
                            sValue += Constants.ATRACKIT_SPACE + getString(R.string.label_relative_humidity);
                    }
                    sBroadcast = sNodeName + ATRACKIT_SPACE + sHumidity_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 6){
                    resId = resId_Speed;
                    sValue = ATRACKIT_EMPTY;
                    if (mMessagesViewModel.getSpeedMsg().getValue() != null) {
                        sValue = mMessagesViewModel.getSpeedMsg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue =  getString(R.string.label_na);
                    }
                    sBroadcast = sSpeed_distance + ATRACKIT_SPACE + sValue;
                }
                if (resId > 0) {
                    drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                            ,R.dimen.home_button_icon_size,context);
                    if (resId >= 3 && resId != 6)
                        drawableDevice = VectorDrawableUtils.loadVectorDrawableWithTint(resId_Device, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                                ,R.dimen.home_button_icon_size,context);
                    else
                        drawableDevice = null;
                }
                broadcastToast(sBroadcast);
                final Drawable finalDrawable = (resId > 0) ? drawable : null;
                final Drawable finalDrawableDevice = (resId > 0) ? drawableDevice : null;
                final String sMsg = sValue;
                new Handler(Looper.getMainLooper()).post(() -> {
                    textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(finalDrawable,null,finalDrawableDevice,null);
                    if ((sMsg != null) && (sMsg.length() > 0)) {
                        textViewMsgBottomRight.setText(sMsg);
                    }
                    textViewMsgBottomRight.setCompoundDrawablePadding(6);
                    textViewMsgBottomRight.setTextColor(foreColor);
                });
            }
        });
        textViewMsgBottomLeft.setOnClickListener(v -> {
            int currentState = mSavedStateViewModel.getState();
            if ((currentState == WORKOUT_LIVE)||(currentState == WORKOUT_PAUSED)){
              //  return;
            }else{
                int iType = mSavedStateViewModel.getViewState(10);
                if (iType == 1) iType = 0; else iType = 1; // flip
                mSavedStateViewModel.setViewState(10,iType);
                int resId = resId_Barometer;
                String sValue = getString(R.string.label_na);
                Drawable drawable = null;
                String sBroadcast = ATRACKIT_EMPTY;
                int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
                if (iType == 0) {
                    if (mMessagesViewModel.getBatteryMsg().getValue() != null) {
                        sValue = mMessagesViewModel.getBatteryMsg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = getString(R.string.label_na);
                    }
                    String sName = "ic_battery_full_white";
                    if (sValue.equals(getString(R.string.label_na))){
                        sName = getString(R.string.label_battery_full);
                        foreColor = ContextCompat.getColor(context, R.color.power_factor_1);
                    }else {
                        int iVal = Integer.valueOf(sValue);
                        if ((iVal >= 0) && (iVal <= 15)) {
                            sName = getString(R.string.label_battery_empty);
                            foreColor = ContextCompat.getColor(context, R.color.power_factor_10);
                        }
                        if ((iVal > 15) && (iVal <= 25)) {
                            sName = getString(R.string.label_battery_25);
                            foreColor = ContextCompat.getColor(context, R.color.power_factor_7);
                        }
                        if ((iVal > 25) && (iVal <= 50)) {
                            sName = getString(R.string.label_battery_50);
                            foreColor = ContextCompat.getColor(context, R.color.power_factor_3);
                        }
                        if ((iVal > 50) && (iVal <= 75)) {
                            sName = getString(R.string.label_battery_75);
                            foreColor = ContextCompat.getColor(context, R.color.power_factor_1);
                        }
                        if (iVal > 75)
                            sName = getString(R.string.label_battery_full);
                        sValue += "%";
                    }
                    if (sValue.equals(getString(R.string.label_na))) sValue = getString(R.string.label_na);
                    resId = res.getIdentifier(sName,Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
                    sBroadcast = sBattery_device + ATRACKIT_SPACE + sValue;
                }
                if (iType == 1){
                    resId = resId_Altitude;
                    if (mMessagesViewModel.getAltitudeMsg().getValue() != null) {
                        double aFloat = mMessagesViewModel.getAltitudeMsg().getValue();
                        sValue = mMessagesViewModel.getSpeedMsg().getValue();
                        if (!Utilities.isInteger(sValue, 0))
                            sValue = getString(R.string.label_na);

                    }
                    sBroadcast = sAltitude_fit + ATRACKIT_SPACE + sValue;
                }
                if (resId > 0) {
                    drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                            ,R.dimen.home_button_icon_size,context);
                    int iconSize = res.getDimensionPixelSize(R.dimen.home_button_icon_size);
                    drawable.setBounds(0, 0, iconSize, iconSize);
                    Utilities.setColorFilter(drawable, (isAmbient) ? ContextCompat.getColor(context, R.color.ambientForeground) : foreColor);
                }
                broadcastToast(sBroadcast);
                final Drawable finalDrawable = (resId > 0) ? drawable : null;
                final String sMsg = sValue;
                final int finalColor = (isAmbient) ? ContextCompat.getColor(context, R.color.ambientForeground) : foreColor;
                new Handler(Looper.getMainLooper()).post(() -> {
                    textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(null,null,finalDrawable,null);
                    if ((sMsg != null) && (sMsg.length() > 0)) {
                        textViewMsgBottomLeft.setText(sMsg);
                    }
                    textViewMsgBottomLeft.setCompoundDrawablePadding(6);
                    textViewMsgBottomLeft.setTextColor(finalColor);
                });

            }
        });
        chronometerClock.setBase(0);
        chronometerClock.setTag(0L);
        long timeMs = System.currentTimeMillis();
        mTimerViewModel.setInitialTime(timeMs);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTimerViewModel.startTime();
            }
        }, 2000);
        final Observer<Long> timeObserver = aLong -> {
            if ((chronometerClock != null) && (chronometerClock.getBase() == 0) && (chronometerClock.getTag().equals(0L)))
                if (aLong > timeMin && aLong < timeMax)
                chronometerClock.post(() -> {
                    if (chronometerClock.getVisibility() != View.VISIBLE) chronometerClock.setVisibility(View.VISIBLE);
                    chronometerClock.setText(Utilities.getTimeString(aLong));
                    if (isAmbient)
                        chronometerClock.setTextColor(colorAmbient);
                    else
                        if (bDeviceConnected)
                            chronometerClock.setTextColor(colorConnected);
                        else
                            chronometerClock.setTextColor(colorDayNight);
                });
        };
        mTimerViewModel.getCurrentTime().observe(getViewLifecycleOwner(), timeObserver);
        final Observer<Integer> stateObserver = state->{
            final Drawable iconSetup = AppCompatResources.getDrawable(context,R.drawable.ic_settings_white);
            final String  textSetup = getString(R.string.action_setup_running2);
            final Drawable iconPending = AppCompatResources.getDrawable(context,R.drawable.ic_add_white);
            final String  textPending = getString(R.string.label_activity);

            final Drawable iconLive = AppCompatResources.getDrawable(context,R.drawable.ic_pause_white);
            final String  textLive = getString(R.string.action_live);
            CircularRevealCardView revealCardView = mCardConstraint.findViewById(R.id.home_reveal_view);
            final Drawable iconCompleted = AppCompatResources.getDrawable(context,R.drawable.ic_checkered_flag_white);
            final Drawable iconContinue = AppCompatResources.getDrawable(context,R.drawable.ic_play_button_white);
            final String  textCompleted = getString(R.string.label_session_completed);
            final String  textPaused = getString(R.string.label_session_paused);
            if (userPrefs == null && (mWorkout != null) && (mWorkout.userID != null) && (mWorkout.userID.length() > 0)) userPrefs = UserPreferences.getPreferences(context,mWorkout.userID);
            if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            final boolean bUseLoc2 = appPrefs.getUseLocation();
            final Resources res = context.getResources();
            final Animation animationBlink = AnimationUtils.loadAnimation(context, R.anim.blink);
            final ReferencesTools referencesTools = ReferencesTools.setInstance(getContext());
            String sUserId = appPrefs.getLastUserID();
            if ((sUserId.length() > 0) && (userPrefs == null))
                userPrefs = UserPreferences.getPreferences(context, sUserId);
            boolean bUseRounded = false;
            int iLogoSource = 0;
            String sImageSource = ATRACKIT_EMPTY;
            if (userPrefs != null) {
                bUseRounded = userPrefs.getUseRoundedImage();
                bUseKg = userPrefs.getUseKG();
            }else bUseKg = (UnitLocale.getDefault() == UnitLocale.Metric);
            bShowGoals = userPrefs.getPrefByLabel(Constants.USER_PREF_SHOW_GOALS);
            switch (state) {
                case WORKOUT_SETUP:
                    btn_home.setIcon(iconSetup);
                    btn_home.setText(textSetup);
                    btn_home.setEnabled(false);
                    mMessagesViewModel.addCurrentMsg(textSetup);
                    crossFadeIn(textViewCenter);
                    Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                    if (src != null) {
                        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
                        if (dr != null) mImageView.setImageDrawable(dr);
                        mImageView.setVisibility(View.VISIBLE);
                    }
                    crossFadeIn(mProgressBar);
                    mRecyclerView.setVisibility(RecyclerView.GONE);
                    if (!isAmbient) { // check if ambient
                        circularRevealFromMiddle(revealCardView, 2000);
                    }
                    break;
                case WORKOUT_INVALID:
                case WORKOUT_PENDING:
                    mScrollView.post(() -> mScrollView.scrollTo(0, btn_home.getTop()));

                    final int bgColor1 = ContextCompat.getColor(context, R.color.primaryDarkColor);
                    btn_home.setIcon(iconPending);
                    btn_home.setText(textPending);
                    btn_home.setEnabled(true);
                    mMessagesViewModel.addCurrentMsg(ATRACKIT_SPACE);
                    if (state == WORKOUT_PENDING)
                        crossFadeOut(chronometerClock);
                    else {
                        chronometerClock.setBase(0L);
                        chronometerClock.setTag(0L);
                        chronometerClock.setFormat(null);
                        chronometerClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                        crossFadeIn(chronometerClock);
                    }
                    if (appPrefs.getPressureSensorCount() > 0)
                        mSavedStateViewModel.setViewState(11,0);
                    else
                    if (appPrefs.getTempSensorCount() > 0) mSavedStateViewModel.setViewState(11,2);
                    else mSavedStateViewModel.setViewState(11,1);

                    if (mProgressBar.getVisibility() == View.VISIBLE) crossFadeOut(mProgressBar);
                    if (chronometerViewCenter.getVisibility() == View.VISIBLE) crossFadeOut(chronometerViewCenter);
                    textViewCenter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                    crossFadeIn(textViewCenter);
                    if (state == WORKOUT_INVALID) {
                        String sSource = ((userPrefs != null) ? userPrefs.getPrefStringByLabel(Constants.LABEL_LOGO_SOURCE) : Constants.LABEL_LOGO);
                        if (sSource.length() > 0) {
                            switch (sSource) {
                                case Constants.LABEL_LOGO:
                                    iLogoSource = 0;
                                    break;
                                case Constants.LABEL_INT_FILE:
                                    iLogoSource = 1;
                                    sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE);
                                    break;
                                case Constants.LABEL_CAMERA_FILE:
                                    iLogoSource = 2;
                                    sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_CAMERA_FILE);
                                    break;
                                case Constants.LABEL_EXT_FILE:
                                    iLogoSource = 3;
                                    sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_EXT_FILE);
                                    break;
                            }
                        }
                        Bitmap bitmap = null;
                        try {
                            if ((iLogoSource > 0) && (sImageSource != null)) {
                                Uri uriImage = Uri.parse(sImageSource);
                                bitmap = Utilities.getMyBitmap(context, uriImage);
                                if (bitmap == null)
                                    bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                            } else
                                bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);  // iSource = logo
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "setupUI error " + e.getMessage());
                            bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                        } finally {
                            if ((bitmap != null) && (iLogoSource <= 1)) {
                                if (bUseRounded) {
                                    RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, bitmap);
                                    dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                                    if (dr != null) mImageView.setImageDrawable(dr);
                                } else if (bitmap != null) mImageView.setImageBitmap(bitmap);
                            } else {
                                if (bitmap != null) mImageView.setImageBitmap(bitmap);
                            }
                            mImageView.setBackgroundColor(bgColor1);
                        }
                        if (bUseLoc2)
                            setLocationText(true);
                        else
                            textViewCenter2.setVisibility(View.GONE);

                        if (!isAmbient) { // check if ambient
                            revealCardView.setBackgroundColor(bgColor1);
                            circularRevealFromMiddle(revealCardView, 2000);
                            if (mImageView.getVisibility() != View.VISIBLE) crossFadeIn(mImageView);
                        }
                    }
                    mCardView.setCardBackgroundColor(bgColor1);
                    mCardView.setStrokeColor(bgColor1);
                    mCardView.setStrokeWidth(5);
                    mCardView.setElevation(0f);
                    textViewBottom.setText(getString(R.string.app_name));
                    textViewBottom.setTextSize(32F);
                    if (mWorkoutAdapter.getItemCount() == 0)
                        mRecyclerView.setVisibility(RecyclerView.GONE);
                    else
                        mRecyclerView.setVisibility(RecyclerView.VISIBLE);

                    break;
                case WORKOUT_LIVE:
                    final int iColorActivity = (mSavedStateViewModel.getColorID() != null) ? mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
                    int iconActivityId = mSavedStateViewModel.getIconID();
                    final int id = (iconActivityId > 0) ? iconActivityId : R.drawable.noti_white_logo;
                    Drawable imageDrawable = AppCompatResources.getDrawable(context, id);
                    Drawable pauseDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_rounded_pause_button_white);
                    final int iColor = ContextCompat.getColor(context, iColorActivity);
                    final int bgColor = ContextCompat.getColor(context, R.color.primaryColor);

                    AnimationDrawable animatedDrawable = (AnimationDrawable)AppCompatResources.getDrawable(context, R.drawable.recording_animation);
                    animatedDrawable.addFrame(imageDrawable, 2000);;
                    Utilities.setColorFilter(animatedDrawable, iColor);
                    final AnimationDrawable activityDrawable = animatedDrawable;
                    final ColorStateList colorStateList = AppCompatResources.getColorStateList(context, R.color.primaryColor);
                    mCardView.setCardBackgroundColor(colorStateList);
                    mCardView.setStrokeWidth(6);
                    mCardView.setStrokeColor(iColor);
                    mCardView.setElevation(12f);
                    btn_home.setIcon(iconLive);
                    btn_home.setText(textLive);
                    btn_home.setEnabled(true);
                    if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) {
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    }
                    if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() != null){
                        mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                        if ((mWorkout != null) && (mWorkoutMeta != null) && Utilities.isShooting(mWorkout.activityID))
                            mMessagesViewModel.addCurrentMsg("Shooting " + mWorkoutSet.setCount + " of " + mWorkoutMeta.setCount);
                    }
                    mImageView.setImageDrawable(activityDrawable);
                    mImageView.setBackgroundColor(bgColor);

                    textViewCenter1.setVisibility(TextView.GONE);
                    textViewCenter3.setVisibility(TextView.GONE);
                    textViewMsgBottomRight.setCompoundDrawables(null,null,null,null);
                    textViewMsgBottomLeft.setCompoundDrawables(null,null,null,null);
                    if ((mWorkout != null) && Utilities.isGymWorkout(mWorkout.activityID)) {
                        mScrollView.post(() -> mScrollView.scrollTo(0, chronometerViewCenter.getTop()));
                    }else {
                        mScrollView.post(() -> mScrollView.scrollTo(0, textViewMsgLeft.getTop()));
                    }
                    if ((mWorkout != null) && (!Utilities.isGymWorkout(mWorkout.activityID)
                            && !Utilities.isShooting(mWorkout.activityID))) {
                        String sMsg = (mWorkout.offline_recording == 0) ? "": Constants.ATRACKIT_OFFLINE;
                        sMsg += Constants.ATRACKIT_TRACKING + mWorkout.activityName;
                        mMessagesViewModel.addCurrentMsg(sMsg);
                        crossFadeIn(textViewCenter);
                        if (!Utilities.isInActiveWorkout(mWorkout.activityID) && bUseLoc) {
                            mSavedStateViewModel.setViewState(9, 1);
                            setLocationText(true);
                        }
                        // tennis fitness type
                        if ((mWorkout.activityID == Constants.WORKOUT_TYPE_TENNIS || Utilities.isSportWorkout(mWorkout.activityID))) {
                            String sPlayer1 = (userPrefs != null) ? userPrefs.getPrefStringByLabel(getString(R.string.label_player_1)) : getString(R.string.label_player_1);
                            String sPlayer2 = (userPrefs != null) ? userPrefs.getPrefStringByLabel(getString(R.string.label_player_2)) : getString(R.string.label_player_2);
                            mSavedStateViewModel.setViewState(10,Math.toIntExact(mWorkout.activityID));
                            textViewMsgBottomLeft.setText(sPlayer1);
                            textViewMsgBottomLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            mSavedStateViewModel.setViewState(11,Math.toIntExact(mWorkout.activityID));
                            textViewMsgBottomRight.setText(sPlayer2);
                            textViewMsgBottomRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            chronometerClock.setVisibility(View.VISIBLE);
                        }
                    }else{ // gym or shooting workout
                        if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        if (mWorkoutSet != null){
                            if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                                String sMsg = ((mWorkout != null) && mWorkout.offline_recording == 0) ? "": Constants.ATRACKIT_OFFLINE;
                                if (mWorkoutSet.scoreTotal != Constants.FLAG_NON_TRACKING) {
                                    if (mWorkoutSet.exerciseName != null)
                                        sMsg += Constants.ATRACKIT_TRACKING + mWorkoutSet.exerciseName;
                                    else
                                        sMsg += Constants.ATRACKIT_TRACKING + mWorkoutSet.activityName;
                                }else {
                                    sMsg += mWorkoutSet.activityName;
                                }
                                mMessagesViewModel.addCurrentMsg(sMsg);
                                chronometerClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                                String resistanceTypeImage = (((mWorkout != null) && mWorkout.scoreTotal != Constants.FLAG_NON_TRACKING) ? "ic_question_mark_white": "ic_weightlifting_silhouette_2");
                                String resistanceTypeName = (((mWorkout != null) && mWorkout.scoreTotal != Constants.FLAG_NON_TRACKING) ? "Unknown" : "Session");
                                if ((resistanceTypeList.size() > 0) && (mWorkoutSet.resistance_type != null)  && (mWorkoutSet.resistance_type > 0)){
                                    for (ResistanceType rt : resistanceTypeList){
                                        if (mWorkoutSet.resistance_type == rt._id){
                                            resistanceTypeImage = rt.imageName;
                                            resistanceTypeName = rt.resistanceName;
                                            break;
                                        }
                                    }
                                }
                                mSavedStateViewModel.addLocationMsg(mWorkoutSet.exerciseName);
                                mSavedStateViewModel.setViewState(9, 80);
                                textViewCenter2.post(()->{
                                    String sMsg2 = ((mWorkout != null) && mWorkout.offline_recording == 0) ? "": Constants.ATRACKIT_OFFLINE;
                                    if (mWorkoutSet.scoreTotal != Constants.FLAG_NON_TRACKING) {
                                        if (mWorkoutSet.exerciseName != null)
                                            sMsg2 = Constants.ATRACKIT_TRACKING + mWorkoutSet.exerciseName;
                                        else
                                            sMsg2 = Constants.ATRACKIT_TRACKING + mWorkoutSet.activityName;
                                    }else {
                                        sMsg2 = mWorkoutSet.activityName;
                                    }
                                    textViewCenter2.setText(sMsg2);
                                });
                                final ColorStateList tintList = AppCompatResources.getColorStateList(context, R.color.primaryTextColor);
                                final String sLabel = resistanceTypeName;
                                final int resId = res.getIdentifier(resistanceTypeImage, ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
                                textViewMsgBottomLeft.post(() -> {
                                    textViewMsgBottomLeft.setText(sLabel);
                                    textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(0,0,resId,0);
                                    textViewMsgBottomLeft.setCompoundDrawablePadding(6);
                                    TextViewCompat.setCompoundDrawableTintList(textViewMsgBottomLeft,tintList);
                                });
                                //  String repsLabel =  String.format(Locale.getDefault(),getString(R.string.set_reps_label), mWorkoutSet.repCount);
                                final String setsLabel = String.format(Locale.getDefault(), getString(R.string.set_sets_label), mWorkoutSet.setCount);
                                textViewMsgBottomRight.post(() -> textViewMsgBottomRight.setText(setsLabel));
                            }
                            else{
                                String sMsg2 = ((mWorkout != null) && mWorkout.offline_recording == 0) ? "": Constants.ATRACKIT_OFFLINE;
                                sMsg2 += ATRACKIT_SPACE + mWorkout.activityName;
                                textViewCenter2.setText(sMsg2);
                            }
                            // tennis fitness type
                            if (mWorkoutSet.activityID == Constants.WORKOUT_TYPE_TENNIS){
                                mSavedStateViewModel.addLocationMsg(mWorkoutSet.score_card);
                                mSavedStateViewModel.setViewState(9, 123);
                            }
                        }else{
                            String sMsg = ((mWorkout != null) && mWorkout.offline_recording == 0) ? "": Constants.ATRACKIT_OFFLINE;
                            sMsg += Constants.ATRACKIT_TRACKING + ((mWorkout != null) ? mWorkout.activityName : ATRACKIT_EMPTY);
                            mMessagesViewModel.addCurrentMsg(sMsg);
                            crossFadeIn(textViewCenter);
                        }
                    }
                    mRecyclerView.setVisibility(RecyclerView.GONE);
                    textViewBottom.setTextSize(24F);
                    textViewBottom.setText(getString(R.string.label_session_live));

                    if ((mWorkoutSet != null) && (mWorkoutSet.realElapsedStart != null)
                            && (mWorkoutSet.realElapsedStart > 0)) {
                        chronometerViewCenter.setBase(mWorkoutSet.realElapsedStart);
                        chronometerViewCenter.start();
                        if ((Utilities.isGymWorkout(mWorkoutSet.activityID)||Utilities.isShooting(mWorkoutSet.activityID))
                                && (chronometerClock.getTag().equals(0L)) && (mWorkoutSet.setCount > 1)){
                            long startMillis = (mWorkout != null && mWorkout.start > 0) ? mWorkout.start : mWorkoutSet.start;
                            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                            chronometerClock.setFormat("Total: %s");
                            chronometerClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                            chronometerClock.setBase(startMillis - elapsedRealtimeOffset);
                            chronometerClock.setTag(1L);
                            chronometerClock.start();
                        }
                    }
                    crossFadeIn(chronometerClock);
                    crossFadeIn(chronometerViewCenter);
                    revealCardView.setBackgroundColor(iColor);
                    circularRevealFromMiddle(revealCardView, 2000);
                    activityDrawable.start();
                    mScrollView.post(() -> mScrollView.scrollTo(0, mCardView.getTop()));
                    break;
                case WORKOUT_PAUSED:
                    if (chronometerViewCenter.isActivated()) chronometerViewCenter.stop();
                    long pauseStart = mSavedStateViewModel.getPauseStart();
                    if (pauseStart == 0)  pauseStart = SystemClock.elapsedRealtime();
                    chronometerViewCenter.setBase(pauseStart);
                    chronometerViewCenter.start();
                    mMessagesViewModel.addCurrentMsg(textPaused);
                    crossFadeIn(chronometerClock);
                    crossFadeOut(mProgressBar);
                    //crossFadeOut(revealCardView);
                    btn_home.setText(textPaused);
                    btn_home.setCompoundDrawablesWithIntrinsicBounds(iconContinue,null,null,null);
                    mScrollView.post(() -> mScrollView.scrollTo(0, btn_home.getTop()));
                    textViewBottom.setText(getString(R.string.label_session_paused));
                    mMessagesViewModel.addCurrentMsg(getString(R.string.label_session_paused));
                    break;
                case WORKOUT_COMPLETED:
                    int iconActivityId2 = (mSavedStateViewModel.getIconID() != null) ?
                            mSavedStateViewModel.getIconID() : referencesTools.getFitnessActivityIconResById(mWorkout.activityID);
                    final int iColorActivity2 = (mSavedStateViewModel.getColorID() != null) ?
                            mSavedStateViewModel.getColorID() : referencesTools.getFitnessActivityColorById(mWorkout.activityID);
                    final int id2 = (iconActivityId2 > 0) ? Math.toIntExact(iconActivityId2) : R.drawable.noti_white_logo;
                    Drawable imagedrawable2 = AppCompatResources.getDrawable(context, id2);
                    Utilities.setColorFilter(imagedrawable2,ContextCompat.getColor(context, iColorActivity2));
                    //imagedrawable2.setColorFilter(ContextCompat.getColor(context, iColorActivity2), PorterDuff.Mode.SRC_IN);
                    final int bgColor2 = ContextCompat.getColor(context, R.color.secondaryDarkColor);
                    final Drawable activitydrawable2 = imagedrawable2;
                    final String sName = getString(R.string.label_session_completed) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(mWorkout.duration);
                    final String sMessage2 = getString(R.string.label_session_completed) + Constants.ATRACKIT_SPACE + mWorkout.activityName;
                    mScrollView.post(() -> mScrollView.scrollTo(0, btn_home.getTop()));
                    btn_home.setEnabled(true);
                    btn_home.setText(getString(R.string.label_session_completed));
                    textViewCenter.setVisibility(View.VISIBLE);
                    textViewCenter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    textViewCenter2.setText(sName);
                    textViewCenter2.setVisibility(View.VISIBLE);
                    textViewCenter2.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    textViewMsgCenterLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    textViewMsgCenterLeft.setText(ATRACKIT_EMPTY);
                    textViewMsgCenterRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    textViewMsgCenterRight.setText(ATRACKIT_EMPTY);
                    textViewMsgBottomLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    textViewMsgBottomLeft.setText(ATRACKIT_EMPTY);
                    textViewMsgBottomRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    textViewMsgBottomRight.setText(ATRACKIT_EMPTY);

                    if (animationBlink != null) animationBlink.cancel();
                    mImageView.setAnimation(null);
                    mMessagesViewModel.addCurrentMsg(sMessage2);
                    mCardView.setStrokeWidth(1);
                    mCardView.setElevation(6f);

                    crossFadeIn(textViewCenter);
                    crossFadeIn(textViewCenter1);
                    crossFadeIn(textViewCenter3);
                    mRecyclerView.setVisibility(RecyclerView.VISIBLE);
                    mImageView.setAnimation(null);
                    mImageView.setImageDrawable(activitydrawable2);
                    mImageView.setCropToPadding(true);
                    mImageView.setBackgroundColor(bgColor2);
                    mCardView.setElevation(4f);

                    btn_home.setIcon(iconCompleted);
                    btn_home.setText(textCompleted);
                    textViewBottom.setTextSize(24F);
                    chronometerViewCenter.stop();
                    if ((chronometerClock.getBase() > 0) && (!chronometerClock.getTag().equals(0L))){
                        chronometerClock.stop();
                        chronometerClock.setFormat(null);
                    }
                    chronometerClock.setBase(0);
                    crossFadeOut(chronometerViewCenter);
                    textViewBottom.setText(getString(R.string.label_session_completed));
                    if (mProgressBar.getVisibility() == View.VISIBLE) crossFadeOut(mProgressBar);
                    revealCardView.setBackgroundColor(bgColor2);
                    circularRevealFromMiddle(revealCardView, 2000);
                    break;
            }

        };
        mSavedStateViewModel.getCurrentState().observe(getViewLifecycleOwner(), stateObserver);
        chronometerClock.setOnClickListener(view -> {
            mListener.OnFragmentInteraction(Constants.UID_chronoClock,0,null);
        });
        mMessagesViewModel.getPhoneAvailable().observe(getViewLifecycleOwner(), aBoolean -> {
            bDeviceConnected = aBoolean;
            if (isAmbient)
                chronometerClock.setTextColor(colorAmbient);
            else
            if (bDeviceConnected)
                chronometerClock.setTextColor(colorConnected);
            else
                chronometerClock.setTextColor(colorDayNight);
        });
        final Observer<String> userIDObserver = s -> {
            if ((s != null) && (s.length() > 0)){
                boolean bSetup = false;
                if (userPrefs != null){
                    bSetup = (userPrefs.getReadDailyPermissions() && (configHeartPts == null || configMoveMins == null || configStepCount == null));
                }
                if ((mSavedStateViewModel.getGoalsListSize() == 0) && bSetup){
                    // Log.e(LOG_TAG, "Goal size 0");
                    List<Configuration> listGoals = new ArrayList<>(3);
                    Configuration config = new Configuration(DataType.TYPE_HEART_POINTS.getName(), s, ATRACKIT_EMPTY, 0L,null,null);
                    List<Configuration> goalsList = mSessionViewModel.getConfiguration(config, s);
                    if ((goalsList != null) && (goalsList.size() > 0)) {
                        configHeartPts = goalsList.get(0);
                        listGoals.add(configHeartPts);
                    }
                    config = new Configuration(DataType.TYPE_MOVE_MINUTES.getName(), s, ATRACKIT_EMPTY, 0L,null,null);
                    List<Configuration> moveList = mSessionViewModel.getConfiguration(config, s);
                    if ((moveList != null) && (moveList.size() > 0)) {
                        configMoveMins = moveList.get(0);
                        listGoals.add(configMoveMins);
                    }
                    config = new Configuration(DataType.TYPE_STEP_COUNT_DELTA.getName(), s, ATRACKIT_EMPTY, 0L,null,null);
                    List<Configuration> stepsList = mSessionViewModel.getConfiguration(config, s);
                    if ((stepsList != null) && (stepsList.size() > 0)) {
                        configStepCount = stepsList.get(0);
                        listGoals.add(configStepCount);
                    }
                    if (listGoals.size() > 0) mSavedStateViewModel.setGoalsList(listGoals);
                }
            }
        };
        mSavedStateViewModel.getUserIDLive().observe(getViewLifecycleOwner(), userIDObserver);

/*        mSavedStateViewModel.getToDoSets().observe(getViewLifecycleOwner(), workoutSets -> {
            if (workoutSets != null) {
                ArrayList<WorkoutSet> sets1 = new ArrayList<>(workoutSets);
                //sets1.sort((o1, o2) -> ((o1.workoutID == o2.workoutID) ? Long.compare(o1.start, o2.start) : Long.compare(o1.workoutID, o2.workoutID)));
                if ((mWorkoutAdapter != null) && (sets1.size() > 0))
                    mWorkoutAdapter.setWorkoutSetArrayList(sets1);
            }
        });*/
        final Observer<WorkoutSet> workoutSetObserver = workoutSet -> {
            new Handler(Looper.getMainLooper()).post(() -> doObserveWorkoutSet(workoutSet, context));
        };
        mSavedStateViewModel.getActiveWorkoutSet().observe(getViewLifecycleOwner(), workoutSetObserver);
        mSavedStateViewModel.getActiveWorkout().observe(getViewLifecycleOwner(), new Observer<Workout>() {
            @Override
            public void onChanged(Workout workout) {
                if (workout != null) {
                    int currentState = mSavedStateViewModel.getState();
                    Log.e(LOG_TAG, "observeWORKOUT state: " + currentState + " " + workout.toString());
                }
            }
        });

        mMessagesViewModel.getUseLocation().observe(getViewLifecycleOwner(), useLoc -> {
            int currentState = mSavedStateViewModel.getState();
            if (currentState != WORKOUT_LIVE){
                if (useLoc){
                    if ((textViewCenter2 != null) && (textViewCenter2.getVisibility() != View.VISIBLE)){
                        textViewCenter2.post(() -> textViewCenter2.setVisibility(View.VISIBLE));
                    }
                }else{
                    if ((textViewCenter2 != null) && (textViewCenter2.getVisibility() != View.GONE)){
                        textViewCenter2.post(() -> textViewCenter2.setVisibility(View.GONE));
                    }
                }
            }
        });
        mMessagesViewModel.getActivityMsg().observe(getViewLifecycleOwner(), s -> {
            int currentState = mSavedStateViewModel.getState();
            int iTag = mSavedStateViewModel.getViewState(9);
            if ((currentState != WORKOUT_LIVE)
                    && (textViewCenter2 != null) && (iTag == 0)) {
                final Drawable calDrawable = AppCompatResources.getDrawable(getActivity(),R.drawable.ic_motion_white);
                final ColorStateList stateList = AppCompatResources.getColorStateList(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
                final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);

                textViewCenter2.post(() -> {
                    textViewCenter2.setText(s);
                    textViewCenter2.setTextColor(foreColor);
                    textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(calDrawable, null, null, null);
                    TextViewCompat.setCompoundDrawableTintList(textViewCenter2,stateList);
                });
            }
        });
        mSavedStateViewModel.getLocationMsg().observe(getViewLifecycleOwner(), s -> {
            final int iTag = mSavedStateViewModel.getViewState(9);
            //boolean isGym = (mSavedStateViewModel.getIsGym() != null) ? mSavedStateViewModel.getIsGym() : false;
            if ((iTag != 0) && (textViewCenter2 != null)) {
                final Drawable calDrawable = AppCompatResources.getDrawable(getActivity(),R.drawable.ic_placeholder);
                final ColorStateList stateList = AppCompatResources.getColorStateList(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
                final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
                textViewCenter2.post(() -> {
                    textViewCenter2.setText(s);
                    textViewCenter2.setTextColor(foreColor);
                    if (iTag == 1) {
                        textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(calDrawable, null, null, null);
                        TextViewCompat.setCompoundDrawableTintList(textViewCenter2,stateList);
                    }else {
                        textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                });
                Log.w(LOG_TAG,"observe setting location text");
            }
        });
        final Observer<String> caloriesObserver = s -> {
            float distance = (s.length() > 0) ? Float.parseFloat(s) : 0F;
            String sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, (distance))  + Constants.CAL_TAIL;
            if (distance > 999) sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, (distance/1000)) + Constants.KCAL_TAIL;
            final int currentState = mSavedStateViewModel.getState();
            final String sLabel = sTemp;
            final Drawable calDrawable = AppCompatResources.getDrawable(getActivity(),R.drawable.ic_calories_small_white);
            final ColorStateList stateList = AppCompatResources.getColorStateList(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            if ((currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_LIVE))
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (textViewMsgCenterLeft != null){
                        textViewMsgCenterLeft.setText(sLabel);
                        if (sLabel.length() > 0){
                            textViewMsgCenterLeft.setCompoundDrawablesWithIntrinsicBounds(null,calDrawable,null,null);
                            TextViewCompat.setCompoundDrawableTintList(textViewMsgCenterLeft,stateList);
                        }else
                            textViewMsgCenterLeft.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);

                        textViewMsgCenterLeft.setTextColor(foreColor);
                    }
                });
        };
        mMessagesViewModel.getCaloriesMsg().observe(getViewLifecycleOwner(), caloriesObserver);
        final Observer<String> distanceObserver = s -> {
            double distance = (s.length() > 0) ? Double.parseDouble(s) : 0D;
            String sTemp = Math.round(distance) + Constants.ALT_TAIL;
            if (distance > 999) sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, (distance/1000)) + Constants.KM_TAIL;
            if (!bUseKg)
                sTemp = Utilities.kmToMilesString(distance);

            final int currentState = mSavedStateViewModel.getState();
            final String sLabel = sTemp;
            final Drawable calDrawable = AppCompatResources.getDrawable(getActivity(),R.drawable.ic_distance_small_white);
            final ColorStateList stateList = AppCompatResources.getColorStateList(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            final int foreColor = ContextCompat.getColor(context, (!isAmbient) ?  R.color.primaryTextColor : R.color.ambientForeground);
            if ((currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_LIVE))
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (textViewMsgCenterRight != null){
                        textViewMsgCenterRight.setText(sLabel);
                        if (sLabel.length() > 0){
                            textViewMsgCenterRight.setCompoundDrawablesWithIntrinsicBounds(null,calDrawable,null,null);
                            TextViewCompat.setCompoundDrawableTintList(textViewMsgCenterRight,stateList);
                        }else
                            textViewMsgCenterRight.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                        textViewMsgCenterRight.setTextColor(foreColor);
                    }
                });
        };
        mMessagesViewModel.getDistanceMsg().observe(getViewLifecycleOwner(), distanceObserver);
        mMessagesViewModel.getDetectedReps().observe(getViewLifecycleOwner(), reps -> {
            final int currentState = mSavedStateViewModel.getState();
            boolean isGym = (mSavedStateViewModel.getIsGym() != null) ? mSavedStateViewModel.getIsGym() : false;
            if (isGym && ((currentState == WORKOUT_PAUSED) || (currentState == WORKOUT_LIVE))){
                final String sLabel = String.format(Locale.getDefault(),SINGLE_INT + REPS_TAIL,reps);
                textViewMsgCenterRight.post(() -> textViewMsgCenterRight.setText(sLabel));
            }
        });

        final Observer<String> moveMinsObserver = s -> {
            if ((s != null) && (s.length() > 0)){
                Integer iTest = mSavedStateViewModel.getState();
                if ((iTest == null) || (iTest == WORKOUT_INVALID) || (iTest == WORKOUT_PENDING)) {
                    int goalMins = 0;
                    if (configMoveMins != null && bShowGoals)
                        goalMins = Math.round(Float.parseFloat(configMoveMins.stringValue));
                    if (goalMins > 0)
                        s += getString(R.string.my_div_string) + goalMins;

                    final String sText = s + Constants.MOVE_MINS_TAIL;
                    final Drawable drawable = AppCompatResources.getDrawable(getContext(),R.drawable.ic_move_mins_smaller);
                    final ColorStateList colorStateAmbient = AppCompatResources.getColorStateList(context, R.color.ambientForeground);
                    final ColorStateList colorStateList = AppCompatResources.getColorStateList(getContext(),R.color.secondaryTextColor);
                    textViewCenter3.post(() -> {
                            textViewCenter3.setText(sText);
                            textViewCenter3.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            if ((!isAmbient)) {
                                textViewCenter3.setTextColor(colorStateList);
                                TextViewCompat.setCompoundDrawableTintList(textViewCenter3,colorStateList);
                            } else {
                                textViewCenter3.setTextColor(colorStateAmbient);
                                TextViewCompat.setCompoundDrawableTintList(textViewCenter3,colorStateAmbient);
                            }
                            if (textViewCenter3.getVisibility()!=View.VISIBLE) crossFadeIn(textViewCenter3);
                    });
                }
            }else
                textViewCenter3.post(() -> {
                    textViewCenter3.setText(ATRACKIT_EMPTY);
                    textViewCenter3.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                //    crossFadeOut(textViewCenter3);
                });
        };
        mMessagesViewModel.getMoveMinsMsg().observe(getViewLifecycleOwner(), moveMinsObserver);
        final Observer<String> heartPtsObserver = s -> {
            if ((s != null) && (s.length() > 0)){
                Integer iTest = mSavedStateViewModel.getState();
                if ((iTest == WORKOUT_INVALID) || (iTest == WORKOUT_PENDING)) {
                    int goalPts = 0;
                    if (configHeartPts != null && bShowGoals)
                        goalPts = Math.round(Float.parseFloat(configHeartPts.stringValue));
                    if (goalPts > 0)
                        s += getString(R.string.my_div_string) + goalPts;
                    final String sText = s;
                    final Drawable drawable = AppCompatResources.getDrawable(getContext(),R.drawable.ic_heartpts_smaller);
                    final ColorStateList colorStateAmbient = AppCompatResources.getColorStateList(context, R.color.ambientForeground);
                    final ColorStateList colorStateList = AppCompatResources.getColorStateList(getContext(),R.color.secondaryTextColor);
                    textViewCenter1.post(() -> {
                            textViewCenter1.setText(sText);
                            textViewCenter1.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                            if ((!isAmbient)) {
                                textViewCenter1.setTextColor(colorStateList);
                                TextViewCompat.setCompoundDrawableTintList(textViewCenter3,colorStateList);
                            } else {
                                textViewCenter1.setTextColor(colorStateAmbient);
                                TextViewCompat.setCompoundDrawableTintList(textViewCenter1,colorStateAmbient);
                            }
                            if (textViewCenter1.getVisibility()!=View.VISIBLE) crossFadeIn(textViewCenter1);
                    });
                }
            }else
                textViewCenter1.post(() -> {
                    textViewCenter1.setText(ATRACKIT_EMPTY);
                    textViewCenter1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                 //   crossFadeOut(textViewCenter1);
                });
        };
        mMessagesViewModel.getHeartPtsMsg().observe(getViewLifecycleOwner(),heartPtsObserver);
        final Observer<String> currentMessageObserver = s -> {
            textViewCenter.post(() -> {
                if ((s != null) && (s.length() > 0)) {
                    textViewCenter.setText(s);
                    crossFadeIn(textViewCenter);
                }else
                    crossFadeOut(textViewCenter);
                //Declare the timer
                int state = mSavedStateViewModel.getState();
                if ((state != WORKOUT_SETUP) && (state != WORKOUT_LIVE)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (textViewCenter != null) {
                            textViewCenter.setText(ATRACKIT_EMPTY);
                            crossFadeOut(textViewCenter);
                        }
                    }, 3000L);
                }
            });
        };
        mMessagesViewModel.getCurrentMsg().observe(getViewLifecycleOwner(), currentMessageObserver);
        return rootView;
    }  // [ end onCreateView]


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        if ((userPrefs == null && mWorkout != null) && ((mWorkout.userID != null) && (mWorkout.userID.length() > 0)))
            userPrefs = UserPreferences.getPreferences(getContext(),mWorkout.userID);
    }
    private void crossFadeIn(View contentView) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                            if (contentView.getId() == R.id.chronoViewCenter){
                           //     ((Chronometer) contentView).start();
                            }
                            if (contentView.getId() == R.id.home_progress_view){
                             //   if (!((ProgressBar) contentView).isAnimating()) ((ProgressBar) contentView).animate().start();
                            }
                        if (contentView.getId() == R.id.button_home_start){
                            if (mSavedStateViewModel.getState() == WORKOUT_PAUSED) broadcastToast(getString(R.string.label_session_paused));
                            //   if (!((ProgressBar) contentView).isAnimating()) ((ProgressBar) contentView).animate().start();
                        }
                    }
                });

    }
    private void crossFadeOut(View contentView) {
        contentView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentView.setVisibility(View.GONE);
                    }
                });
    }

    public void doObserveWorkoutSet(WorkoutSet workoutSet, Context context){
        if (workoutSet == null) return;
        int currentState = mSavedStateViewModel.getState();
        String exerciseName = (workoutSet.exerciseName != null)?  workoutSet.exerciseName : ATRACKIT_EMPTY;
        String weight;
        float tempWeight = 0;
        if (workoutSet.weightTotal != null)
            if (bUseKg)
                tempWeight = workoutSet.weightTotal;
            else
                tempWeight = Utilities.KgToPoundsDisplay(workoutSet.weightTotal);
        double intWeight =  Math.floor(tempWeight);
        if (tempWeight % intWeight != 0)
            weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
        else
            weight = new DecimalFormat("#").format(intWeight);
        if (!bUseKg)
            weight += Constants.LBS_TAIL;
        final String weightLabel = weight;
        String repsLabel =  String.format(Locale.getDefault(),getString(R.string.set_reps_label), workoutSet.repCount);
        String setsLabel = String.format(Locale.getDefault(), getString(R.string.set_sets_label), workoutSet.setCount);
        String resistanceTypeImage = "ic_question_mark_white";
        String resistanceTypeName = "Unknown";
        if ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED)){
            Log.e(LOG_TAG, "observeSet LIVE " + currentState + " " + workoutSet.toString());
            if (Utilities.isGymWorkout(workoutSet.activityID)) {
                if (workoutSet.scoreTotal == Constants.FLAG_NON_TRACKING){
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            textViewMsgBottomLeft.setText(ATRACKIT_EMPTY);
                            textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            textViewMsgBottomRight.setText(ATRACKIT_EMPTY);
                            textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            mSavedStateViewModel.setViewState(9, 80);
                            mSavedStateViewModel.addLocationMsg(workoutSet.activityName);
                            textViewMsgCenterLeft.setText(ATRACKIT_EMPTY);
                            textViewMsgCenterLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            textViewMsgCenterRight.setText(ATRACKIT_EMPTY);
                            textViewMsgCenterRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        }
                    });
                }else {
                    if ((resistanceTypeList.size() > 0) && (workoutSet.resistance_type != null) && (workoutSet.resistance_type > 0)) {
                        for (ResistanceType rt : resistanceTypeList) {
                            if (workoutSet.resistance_type == rt._id) {
                                resistanceTypeImage = rt.imageName;
                                resistanceTypeName = rt.resistanceName;
                                break;
                            }
                        }
                    }
                    final int drawable_res = res.getIdentifier(resistanceTypeImage, ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
                    final String sLabel = resistanceTypeName;
                    final ColorStateList tintList = AppCompatResources.getColorStateList(context, R.color.primaryTextColor);
                    textViewMsgBottomLeft.post(() -> {
                        textViewMsgBottomLeft.setText(sLabel);
                        textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable_res, 0);
                        textViewMsgBottomLeft.setCompoundDrawablePadding(6);
                        TextViewCompat.setCompoundDrawableTintList(textViewMsgBottomLeft, tintList);
                    });
                    textViewMsgBottomRight.setText(setsLabel);
                    if ((exerciseName.length() > 0) && (textViewCenter2 != null)) {
                        mSavedStateViewModel.setViewState(9, 80);
                        mSavedStateViewModel.addLocationMsg(exerciseName);
                    }
                    Drawable lbs = AppCompatResources.getDrawable(context, R.drawable.ic_pounds_weight);
                    Utilities.setColorFilter(lbs, ContextCompat.getColor(context, R.color.primaryTextColor));
                    final Drawable iconWeights = (bUseKg) ? AppCompatResources.getDrawable(context, R.drawable.ic_weight) : lbs;
                    final Drawable iconReps = AppCompatResources.getDrawable(context, R.drawable.ic_action_barbell_vector_dark);

                    if (textViewMsgCenterLeft != null) {
                        textViewMsgCenterLeft.post(() -> {
                            textViewMsgCenterLeft.setText(weightLabel);
                            textViewMsgCenterLeft.setCompoundDrawablesWithIntrinsicBounds(null, iconWeights, null, null);
                            TextViewCompat.setCompoundDrawableTintList(textViewMsgCenterLeft, tintList);
                        });
                    }
                    if ((workoutSet.repCount != null) && (workoutSet.repCount > 0) && (textViewMsgCenterRight != null)) {
                        textViewMsgCenterRight.post(() -> {
                            textViewMsgCenterRight.setText(repsLabel);
                            textViewMsgCenterRight.setCompoundDrawablesWithIntrinsicBounds(null, iconReps, null, null);
                            TextViewCompat.setCompoundDrawableTintList(textViewMsgCenterRight, tintList);
                        });
                    }
                }
            }
            // tennis fitness type
            if (workoutSet.activityID == Constants.WORKOUT_TYPE_TENNIS){
                textViewCenter2.post(() -> textViewCenter2.setText(workoutSet.score_card));
            }
        }else{
            Log.e(LOG_TAG, "observeSet not live " + currentState + " " + workoutSet.toString());
        }
    }

    private void setLocationText(boolean bUseLoc){
            final int currentState = mSavedStateViewModel.isSessionSetup() ? mSavedStateViewModel.getState() : WORKOUT_INVALID;
            final boolean bisGym = mSavedStateViewModel.getIsGym();
            //  ONLY WORKOUT_PENDING states
            int resId=0; String sValue = "";
            Context context = mConstraintLayout.getContext();
            int iType = mSavedStateViewModel.getViewState(9);
            Resources res = getResources();
            if ((currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_LIVE)) {
                if (iType != 0) {
                    resId = res.getIdentifier("ic_placeholder", Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
                    sValue = mSavedStateViewModel.getLocationAddress();
                    if (sValue == null || sValue.length() == 0){
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        long endTime = System.currentTimeMillis();
                        cal.add(Calendar.HOUR_OF_DAY,-1);
                        long startTime = cal.getTimeInMillis();
                        List<ATrackItLatLng> aList = mSessionViewModel.getATrackItLatLngsByDates(startTime,endTime);
                        ListIterator listIterator = aList.listIterator(aList.size());
                        while (listIterator.hasPrevious()){
                            ATrackItLatLng aT = (ATrackItLatLng) listIterator.previous();
                            if ((aT != null) && (aT.shortName.length() > 0)){
                                sValue = aT.shortName;
                                break;
                            }

                        }
                    }
                } else {
                    resId = res.getIdentifier("ic_motion_white", Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
                    if (mMessagesViewModel.getActivityMsg().getValue() != null)
                        sValue = mMessagesViewModel.getActivityMsg().getValue();
                    else
                        sValue = getString(R.string.label_unknown);
                }
                if (sValue.length() == 0) sValue = getString(R.string.label_unknown);

            }else{
                if (!bisGym && (mSavedStateViewModel.getActiveWorkout().getValue() != null)) {
                    sValue = mSavedStateViewModel.getActiveWorkout().getValue().activityName;
                }else {
                    if ((mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) && (currentState != WORKOUT_COMPLETED)){
                        sValue = mSavedStateViewModel.getActiveWorkoutSet().getValue().exerciseName;
                    }
                }
              //  if (sValue.length() == 0) sValue = mMessagesViewModel.getLocationMsg().getValue();
                resId = 0; // dont set icon on live sessions
            }
            String s = ((textViewCenter2 != null) ? textViewCenter2.getText().toString() : null);
            if ((sValue != null) && !sValue.equals(s)) {
                final int tintColor = (!isAmbient) ? ContextCompat.getColor(context, R.color.primaryTextColor) : ContextCompat.getColor(context, R.color.ambientForeground);
                Drawable drawable = null;
                if (resId > 0) {
                    drawable = VectorDrawableUtils.loadVectorDrawableWithTint(resId, ((!isAmbient) ? R.color.primaryTextColor : R.color.ambientForeground)
                            ,R.dimen.home_button_icon_size,context);
                  //  int iconSize = res.getDimensionPixelSize(R.dimen.home_button_icon_size);
                  //  drawable.setBounds(0, 0, iconSize, iconSize);
                   // Utilities.setColorFilter(drawable, tintColor);
                }
                final Drawable resourceId = (resId > 0) ? drawable : null;
                final String sMsg = sValue;

                textViewCenter2.post(() -> {
                    if ((sMsg != null) && (sMsg.length() > 0)) {
                        float fSize = Utilities.getLabelFontSize(sMsg);
                        textViewCenter2.setTextSize(TypedValue.COMPLEX_UNIT_SP, fSize);
                        textViewCenter2.setText(sMsg);
                    }
                    textViewCenter2.setTextColor(tintColor);
                    textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(resourceId, null, null, null);
                    textViewCenter2.setCompoundDrawablePadding(6);

                });
            }
        }


    public void refreshPersonalImage(){
        new Handler(Looper.getMainLooper()).post(() -> {
            boolean bUseRounded = false;
            int iLogoSource = 0;
            String sImageSource = ATRACKIT_EMPTY;
            if (userPrefs != null) {
                bUseRounded = userPrefs.getUseRoundedImage();
                bUseKg = userPrefs.getUseKG();
            }else bUseKg = (UnitLocale.getDefault() == UnitLocale.Metric);
            CircularRevealCardView revealCardView = mCardConstraint.findViewById(R.id.home_reveal_view);
            int state = mSavedStateViewModel.getState();
            Context context = getContext();
            switch (state) {
                case WORKOUT_SETUP:
                    Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                    if (src != null) {
                        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
                        if (dr != null) mImageView.setImageDrawable(dr);
                        mImageView.setVisibility(View.VISIBLE);
                    }
                    crossFadeIn(mProgressBar);
                    if (!isAmbient) { // check if ambient
                        circularRevealFromMiddle(revealCardView, 2000);
                    }
                    break;
                case WORKOUT_INVALID:
                    final int bgColor1 = ContextCompat.getColor(context, R.color.primaryDarkColor);
                    String sSource = ((userPrefs != null) ? userPrefs.getPrefStringByLabel(Constants.LABEL_LOGO_SOURCE): Constants.LABEL_LOGO);
                    if (sSource.length() > 0) {
                        switch (sSource){
                            case Constants.LABEL_LOGO:
                                iLogoSource = 0;
                                break;
                            case Constants.LABEL_INT_FILE:
                                iLogoSource = 1;
                                sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_INT_FILE);
                                break;
                            case Constants.LABEL_CAMERA_FILE:
                                iLogoSource = 2;
                                sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_CAMERA_FILE);
                                break;
                            case Constants.LABEL_EXT_FILE:
                                iLogoSource = 3;
                                sImageSource = userPrefs.getPrefStringByLabel(Constants.LABEL_EXT_FILE);
                                break;
                        }
                    }
                    Bitmap bitmap = null;
                    try {
                        if ((iLogoSource > 0) && (sImageSource != null)) {
                            Uri uriImage = Uri.parse(sImageSource);
                            bitmap =  Utilities.getMyBitmap(context, uriImage);
                            if (bitmap == null)
                                bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                        }else
                            bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);  // iSource = logo
                    } catch (Exception e) {
                        Log.e(LOG_TAG,"setupUI error " + e.getMessage());
                        bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                    }finally {
                        if ((bitmap != null) && (iLogoSource <= 1)) {
                            if (bUseRounded){
                                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, bitmap);
                                dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                                if (dr != null) mImageView.setImageDrawable(dr);
                            }else
                            if (bitmap != null) mImageView.setImageBitmap(bitmap);
                        }else{
                            if (bitmap != null) mImageView.setImageBitmap(bitmap);
                        }
                        mImageView.setBackgroundColor(bgColor1);
                    }
                    break;
            }
        });
    }

    private void doGoCountdown(Integer iCountDown){
        mSavedStateViewModel.setViewState(5,1);
        // find the users default countdown
        if (iCountDown == 0){
            iCountDown = 10;
        }
        mTimerViewModel.setCountdownTime(iCountDown.longValue());

    }
    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        Context context = getContext();
        msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, msg);
        msgIntent.putExtra(KEY_FIT_TYPE, 2);
        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
        getActivity().sendBroadcast(msgIntent);
    }
    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        isAmbient = true;
      //  IsLowBitAmbient =
       //         ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
    //    DoBurnInProtection =
      //          ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        Context context = getContext();
        int backColor = ContextCompat.getColor(context, R.color.ambientBackground);
        int foreColor = ContextCompat.getColor(context, R.color.ambientForeground);
        mImageViewColorFilter = mImageView.getColorFilter();
        int aState =  WORKOUT_INVALID;
        try {
                 aState = mSavedStateViewModel.getState();
        }catch (Exception e){
            Log.e(LOG_TAG,"enterAmbient " + e.getMessage());
        }
        final ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.ambientForeground);
        final ColorStateList colorStateList2 = ContextCompat.getColorStateList(context, R.color.ambientBackground);
        final int currentState = aState;
        new Handler(Looper.getMainLooper()).post(() -> {
            mConstraintLayout.setBackgroundTintList(colorStateList2);
            //btn_home.setVisibility(View.GONE);
            mProgressBar.setVisibility(ProgressBar.GONE);

            if (mImageView.getAnimation() != null) mImageView.getAnimation().cancel();
            mImageView.setVisibility(View.GONE);
            textViewMsgCenterLeft.setVisibility(View.GONE);
            textViewMsgCenterRight.setVisibility(View.GONE);
            for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
                View v = mConstraintLayout.getChildAt(i);
                if (v instanceof MaterialButton) {
                    v.setVisibility(View.GONE);
                }else if (v instanceof TextView) {
                    if (v.getId() == R.id.textViewBottom){
                        TextViewCompat.setCompoundDrawableTintList((TextView) v, colorStateList);
                        if (v.getId() != R.id.chronoClock)
                            ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                        Paint textPaint1 = ((TextView) v).getPaint();
                        textPaint1.setAntiAlias(false);
                        textPaint1.setStyle(Paint.Style.STROKE);
                        textPaint1.setStrokeWidth(2);
                        ((TextView) v).setTextColor(foreColor);
                        //     }
                    } else {
                        ((TextView) v).setTextColor(foreColor);
                        TextViewCompat.setCompoundDrawableTintList((TextView) v, colorStateList);
                    }
                }
            }
            for (int i = 0; i < mCardConstraint.getChildCount(); i++) {
                View v = mCardConstraint.getChildAt(i);
                if ((v instanceof Chronometer) && (v.getVisibility() == Chronometer.VISIBLE)) {
                    ((Chronometer) v).setTextColor(foreColor);
                } else if (v instanceof TextView) {
                    if ((v.getId() == R.id.textViewCenter) || (v.getId() == R.id.textViewCenter3) || (v.getId() == R.id.textViewCenter1)){

                    }else
                    if ((v.getId() == R.id.textViewMsgLeft) || (v.getId() == R.id.textViewMsgRight)) {
                        ((TextView) v).setTextColor(foreColor);
                        ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                        TextViewCompat.setCompoundDrawableTintList((TextView) v, colorStateList);
                    } else if ((v.getId() == R.id.chronoClock)) {
                        TextViewCompat.setCompoundDrawableTintList((TextView) v, colorStateList);
                        if ((currentState == WORKOUT_LIVE) || (currentState == WORKOUT_PAUSED))
                            ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                        Paint textPaint1 = ((TextView) v).getPaint();
                        textPaint1.setAntiAlias(false);
                        textPaint1.setStyle(Paint.Style.STROKE);
                        textPaint1.setStrokeWidth(2);
                        ((TextView) v).setTextColor(foreColor);
                        //     }
                    } else {
                        ((TextView) v).setTextColor(foreColor);
                        TextViewCompat.setCompoundDrawableTintList((TextView) v, colorStateList);
                    }
                }
            }
            loadDataAndUpdateScreen();
            prevAmbient = isAmbient;
        });
    }

    @Override
    public void loadDataAndUpdateScreen() {
        Context context = getContext();
        final int tintColor = (!isAmbient) ? ContextCompat.getColor(context, R.color.primaryTextColor) : ContextCompat.getColor(context, R.color.ambientForeground);
        final boolean newAmbient = (isAmbient != prevAmbient);
        long aLong = (mTimerViewModel.getCurrentTime().getValue() != null) ? mTimerViewModel.getCurrentTime().getValue() : System.currentTimeMillis();
        if ((chronometerClock != null) && (chronometerClock.getTag().equals(0L))){
            chronometerClock.setText(Utilities.getTimeString(aLong));
            if (!isAmbient && bDeviceConnected)
                chronometerClock.setTextColor(colorConnected);
            else
                chronometerClock.setTextColor(tintColor);
        }
        final SensorDailyTotals sdt = mSavedStateViewModel.getSDT();
        if (sdt == null) return;
        int iType = mSavedStateViewModel.getViewState(3);
        String sValue = ATRACKIT_EMPTY;
        DailyCounter stepCounter = mSavedStateViewModel.getSteps();
        final int currentState = mSavedStateViewModel.getState();
        final boolean bGym = mSavedStateViewModel.getIsGym();
        DateFormat dateFormat = getTimeInstance();
        int nowSteps = 0; long lastUpdated = 0;
        if (iType == 0) {nowSteps = (mMessagesViewModel.getDeviceStepsMsg().getValue() != null) ? Integer.parseInt(mMessagesViewModel.getDeviceStepsMsg().getValue())
                    : sdt.deviceStep;
            lastUpdated = sdt.lastDeviceStep; }
        if (iType == 1) {nowSteps = (mMessagesViewModel.getStepsMsg().getValue() != null) ? Integer.parseInt(mMessagesViewModel.getStepsMsg().getValue())
                : sdt.fitStep;lastUpdated = sdt.lastFitStep;}
        if (iType == 2) { sValue = mMessagesViewModel.getDevice2StepsMsg().getValue();
            if ((sValue != null) && Utilities.isInteger(sValue, 0)){
                nowSteps = Integer.parseInt(sValue); lastUpdated = 0;
            }
        }
        if (nowSteps > 0) {
            if (!bGym && (currentState == WORKOUT_LIVE || currentState == WORKOUT_PAUSED) && (stepCounter != null)) {
                nowSteps = nowSteps - Math.toIntExact(stepCounter.FirstCount);
                String steps = String.format(Locale.getDefault(), SINGLE_INT,nowSteps);
                steps = "[" + steps + "]";
                // + Constants.SHOT_XY_DELIM + String.format(Locale.getDefault(), SINGLE_INT,nowSteps);
                textViewMsgRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                textViewMsgLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                textViewMsgRight.setText(steps);
                if (newAmbient) textViewMsgRight.setTextColor(tintColor);
            }else{
                textViewMsgRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewMsgLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                String steps = String.format(Locale.getDefault(), SINGLE_INT,nowSteps);
                textViewMsgRight.setText(steps);
                if (newAmbient) textViewMsgRight.setTextColor(tintColor);
            }
        }else
            textViewMsgRight.setText(getString(R.string.label_na));
        if (iType == 0) textViewMsgRight.setCompoundDrawablesWithIntrinsicBounds(resId_Step_device,0,0,0);
        if (iType == 1) textViewMsgRight.setCompoundDrawablesWithIntrinsicBounds(resId_Step_fit,0,0,0);
        if (iType == 2) textViewMsgRight.setCompoundDrawablesWithIntrinsicBounds(resId_Step_device2,0,0,0);
        textViewMsgRight.setCompoundDrawablePadding(6);
        //textViewMsgRight.setTextColor(colorDayNight);

        iType = mSavedStateViewModel.getViewState(2);
        float nowBPM = 0; lastUpdated = 0;
        if (iType == 0) {nowBPM = (mMessagesViewModel.getDeviceBpmMsg().getValue() != null) ? Integer.parseInt(mMessagesViewModel.getDeviceBpmMsg().getValue())
                : sdt.deviceBPM;lastUpdated = sdt.lastDeviceBPM; }
        if (iType == 1) {nowBPM = (mMessagesViewModel.getBpmMsg().getValue() != null) ? Integer.parseInt(mMessagesViewModel.getBpmMsg().getValue())
                : sdt.fitBPM;lastUpdated = sdt.lastFitBPM; }
        if (iType == 2) { sValue = mMessagesViewModel.getDevice2BpmMsg().getValue();
            if ((sValue != null) && Utilities.isFloat(sValue, 0)){
                nowBPM = Float.parseFloat(sValue); lastUpdated = 0;
            }
        }
        nowSteps = Math.round(nowBPM);
        if (nowSteps > 0) {
            String steps = String.format(Locale.getDefault(), SINGLE_INT, nowSteps);
            textViewMsgLeft.setText(steps);
        }else
            textViewMsgLeft.setText(getString(R.string.label_na));

        if (iType == 0) textViewMsgLeft.setCompoundDrawablesWithIntrinsicBounds(0,0,resId_BPM_device,0);
        if (iType == 1) textViewMsgLeft.setCompoundDrawablesWithIntrinsicBounds(0,0,resId_BPM_fit,0);
        if (iType == 2) textViewMsgLeft.setCompoundDrawablesWithIntrinsicBounds(0,0,resId_BPM_device2,0);
        textViewMsgLeft.setCompoundDrawablePadding(6);
        if (newAmbient) textViewMsgLeft.setTextColor(tintColor);
        if ((currentState != WORKOUT_LIVE)&&(currentState != WORKOUT_PAUSED) && (currentState != WORKOUT_CALL_TO_LINE)) {
            iType = mSavedStateViewModel.getViewState(10);
            if (iType == 0) {
                sValue = getString(R.string.label_na);
                if (mMessagesViewModel.getBatteryMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getBatteryMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue = getString(R.string.label_na);
                }
                String sName = "ic_battery_full_white";
                ColorStateList tList = ContextCompat.getColorStateList(context, R.color.primaryTextColor);
                if (sValue.equals(getString(R.string.label_na))) {
                    sName = getString(R.string.label_battery_full);
                    tList = ContextCompat.getColorStateList(context, R.color.power_factor_1);
                } else {
                    int iVal = Integer.valueOf(sValue);
                    if ((iVal >= 0) && (iVal <= 15)) {
                        sName = getString(R.string.label_battery_empty);
                        tList = ContextCompat.getColorStateList(context, R.color.power_factor_10);
                    }
                    if ((iVal > 15) && (iVal <= 25)) {
                        sName = getString(R.string.label_battery_25);
                        tList = ContextCompat.getColorStateList(context, R.color.power_factor_7);
                    }
                    if ((iVal > 25) && (iVal <= 50)) {
                        sName = getString(R.string.label_battery_50);
                        tList = ContextCompat.getColorStateList(context, R.color.power_factor_3);
                    }
                    if ((iVal > 50) && (iVal <= 75)) {
                        sName = getString(R.string.label_battery_75);
                        tList = ContextCompat.getColorStateList(context, R.color.power_factor_1);
                    }
                    if (iVal > 75)
                        sName = getString(R.string.label_battery_full);
                    sValue += "%";

                }
                int resourceId = context.getResources().getIdentifier(sName, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
                Drawable drawable = AppCompatResources.getDrawable(context, resourceId);
                Utilities.setColorFilter(drawable, tintColor);
                final Drawable finalDrawable = drawable;
                final ColorStateList tintList = tList;
                final String sMsg = sValue;
                new Handler(Looper.getMainLooper()).post(() -> {
                    textViewMsgBottomLeft.setText(sMsg);
                    textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(finalDrawable, null, null, null);
                    textViewMsgBottomLeft.setCompoundDrawablePadding(6);
                    TextViewCompat.setCompoundDrawableTintList(textViewMsgBottomLeft, tintList);
                });
            }
            if (iType == 1) {
                double aFloat = (mMessagesViewModel.getAltitudeMsg().getValue() == null) ? 0F : mMessagesViewModel.getAltitudeMsg().getValue();
                final String sLabel = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, aFloat) + Constants.ALT_TAIL;
                final ColorStateList tintList = AppCompatResources.getColorStateList(context,((isAmbient) ?R.color.ambientForeground: R.color.primaryTextColor));
                new Handler(Looper.getMainLooper()).post(() -> {
                    textViewMsgBottomLeft.setText(sLabel);
                    textViewMsgBottomLeft.setCompoundDrawablesWithIntrinsicBounds(resId_Altitude, 0, 0, 0);
                    textViewMsgBottomLeft.setCompoundDrawablePadding(6);
                    TextViewCompat.setCompoundDrawableTintList(textViewMsgBottomLeft, tintList);
                });
            }

            iType = mSavedStateViewModel.getViewState(11);
            if (iType == 0) {
                nowSteps = Math.round(sdt.pressure);
                lastUpdated = sdt.lastDeviceOther;
                if (nowSteps > 0) {
                    String sPressure = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_pressure_hpa);
                    textViewMsgBottomRight.setText(sPressure);
                    if (lastUpdated > 0) textViewMsgBottomRight.setTag(dateFormat.format(lastUpdated));
                    else textViewMsgBottomRight.setTag(null);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_na));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Barometer, 0, 0, 0);
            }
            if (iType == 1) {
                nowSteps = Math.round(sdt.temperature);
                lastUpdated = sdt.lastDeviceOther;
                String sTemp = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_temperature_celcius);
                textViewMsgBottomRight.setText(sTemp);
                if (nowSteps > 0) {
                    textViewMsgBottomRight.setText(sTemp);
                    if (lastUpdated > 0) textViewMsgBottomRight.setTag(dateFormat.format(lastUpdated));
                    else textViewMsgBottomRight.setTag(null);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_not_available0));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Temperature, 0, 0, 0);
            }
            if (iType == 2) {
                nowSteps = Math.round(sdt.humidity);
                lastUpdated = sdt.lastDeviceOther;
                String sTemp = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_relative_humidity);
                textViewMsgBottomRight.setText(sTemp);
                if (nowSteps > 0) {
                    textViewMsgBottomRight.setText(sTemp);
                    if (lastUpdated > 0) textViewMsgBottomRight.setTag(dateFormat.format(lastUpdated));
                    else textViewMsgBottomRight.setTag(null);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_not_available0));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Humidity, 0, 0, 0);
            }
            if (iType == 3) {
                nowSteps = Math.round(sdt.pressure2);
                lastUpdated = sdt.lastDevice2Other;
                if (nowSteps > 0) {
                    String sPressure = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_pressure_hpa);
                    textViewMsgBottomRight.setText(sPressure);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_na));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Barometer, 0, resId_Device, 0);
            }
            if (iType == 4) {
                nowSteps = Math.round(sdt.temperature2);
                lastUpdated = sdt.lastDevice2Other;
                String sTemp = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_temperature_celcius);
                textViewMsgBottomRight.setText(sTemp);
                if (nowSteps > 0) {
                    textViewMsgBottomRight.setText(sTemp);
                    if (lastUpdated > 0) textViewMsgBottomRight.setTag(dateFormat.format(lastUpdated));
                    else textViewMsgBottomRight.setTag(null);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_not_available0));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Temperature, 0, resId_Device, 0);
            }
            if (iType == 5) {
                nowSteps = Math.round(sdt.humidity2);
                lastUpdated = sdt.lastDevice2Other;
                String sTemp = String.format(Locale.getDefault(), SINGLE_INT, nowSteps) + Constants.ATRACKIT_SPACE + getString(R.string.label_relative_humidity);
                textViewMsgBottomRight.setText(sTemp);
                if (nowSteps > 0) {
                    textViewMsgBottomRight.setText(sTemp);
                    if (lastUpdated > 0) textViewMsgBottomRight.setTag(dateFormat.format(lastUpdated));
                    else textViewMsgBottomRight.setTag(null);
                }else
                    textViewMsgBottomRight.setText(getString(R.string.label_not_available0));
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Humidity, 0, resId_Device, 0);
            }
            if (iType == 6) {
                sValue = ATRACKIT_EMPTY;
                if (mMessagesViewModel.getSpeedMsg().getValue() != null) {
                    sValue = mMessagesViewModel.getSpeedMsg().getValue();
                    if (!Utilities.isInteger(sValue, 0))
                        sValue =  getString(R.string.label_na);
                }
                textViewMsgBottomRight.setText(sValue);
                textViewMsgBottomRight.setCompoundDrawablesWithIntrinsicBounds(resId_Speed, 0, 0, 0);
            }
            if (newAmbient) textViewMsgBottomRight.setTextColor(tintColor);

            // location stuff
            iType = mSavedStateViewModel.getViewState(9);
            if (iType == 0) {
                String sMotion = mMessagesViewModel.getActivityMsg().getValue();
                if ((sMotion != null) && (sMotion.length() > 0)) {
                    String sLabel = sMotion;
                    textViewCenter2.setText(sLabel);
                } else {
                    String sLabel = getString(R.string.label_unknown);
                    textViewCenter2.setText(sLabel);
                }
                textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(resId_Motion, 0, 0, 0);
            }
            if (iType == 1) {
                boolean bUse = (mMessagesViewModel.getUseLocation().getValue() != null) ? mMessagesViewModel.getUseLocation().getValue() : true;
                String sTemp = mSavedStateViewModel.getLocationAddress();
                if ((sTemp != null) && (sTemp.length() > 0) && bUse)
                    textViewCenter2.setText(sTemp);
                else
                    textViewCenter2.setText(getString(R.string.label_not_available0));
                textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(resId_Location, 0, 0, 0);
            }
            if (iType == 2) {
                String sState = Utilities.currentStateTitle(context, currentState);
                if (sState.length() > 0)
                    textViewCenter2.setText(sState);
                else
                    textViewCenter2.setText(getString(R.string.label_not_available0));
                //textViewCenter2.setTextSize(24F);
                textViewCenter2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                if (newAmbient) textViewCenter2.setTextColor(tintColor);
            }
        }
    }


    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
        try {
            isAmbient = false;
            Context context = (getActivity() != null) ? getActivity() : mConstraintLayout.getContext();
            final int currentState = mSavedStateViewModel.getState();
            final int foreColor = ContextCompat.getColor(context, R.color.primaryTextColor);
            final int foreColor2 = ContextCompat.getColor(context, R.color.secondaryTextColor);
            //int backColor = ContextCompat.getColor(context, R.color.primaryColor);
            final ColorStateList tintList = ContextCompat.getColorStateList(context, R.color.primaryTextColor);

            final CircularRevealCardView revealCardView = mCardConstraint.findViewById(R.id.home_reveal_view);
       //     IsLowBitAmbient = false;
        //    DoBurnInProtection = false;
            new Handler(Looper.getMainLooper()).post(() -> {
            btn_home.setTextColor(foreColor);
            crossFadeIn(btn_home);

            textViewMsgCenterLeft.setVisibility(View.VISIBLE);
            textViewMsgCenterRight.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            chronometerClock.setTextColor(colorDayNight);
            TextViewCompat.setCompoundDrawableTintList(chronometerClock, tintList);
            if ((currentState == WORKOUT_PAUSED) || (currentState == WORKOUT_LIVE) || (currentState == WORKOUT_CALL_TO_LINE)) {
                int iColorActivity = (mSavedStateViewModel.getColorID() != null) ? mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
                final int iColor = ContextCompat.getColor(context, iColorActivity);
                int iconActivityId = mSavedStateViewModel.getIconID();
                final int id = (iconActivityId > 0) ? iconActivityId : R.drawable.noti_white_logo;
                Drawable imageDrawable = AppCompatResources.getDrawable(context, id);
                AnimationDrawable animatedDrawable = (AnimationDrawable) AppCompatResources.getDrawable(context, R.drawable.recording_animation);
                animatedDrawable.addFrame(imageDrawable, 2000);

                Utilities.setColorFilter(animatedDrawable, iColor);
                final AnimationDrawable activityDrawable = animatedDrawable;
                new Handler(Looper.getMainLooper()).post(() -> {
                            mCardView.setStrokeColor(iColor);
                            mCardView.setStrokeWidth(2);
                            mCardView.setElevation(8f);
                            mImageView.setVisibility(ImageView.VISIBLE);
                            chronometerViewCenter.setVisibility(Chronometer.VISIBLE);
                            chronometerViewCenter.start();
                            mProgressBar.setIndeterminate(true);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mProgressBar.bringToFront();
                            if (!mProgressBar.isAnimating()) mProgressBar.animate().start();
                            revealCardView.setBackgroundColor(iColor);
                        });
                circularRevealFromMiddle(revealCardView, 2000);
                activityDrawable.start();

            } else
                crossFadeIn(mImageView);

            if (mMessagesViewModel != null)
                mMessagesViewModel.setCloudAvailable(isNetworkConnected());
                new Handler(Looper.getMainLooper()).post(() -> {
                            float bigTextSize = 30f;
                            for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
                                View v = mConstraintLayout.getChildAt(i);
                                if (v instanceof MaterialButton) {
                                    v.setVisibility(View.VISIBLE);
                                }
                                if (v instanceof TextView) {
                                    if (v.getId() == R.id.textViewBottom) {
                                        ((TextView) v).setTextColor(foreColor);
                                        TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                        ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, bigTextSize);
                                        Paint textPaint2 = ((TextView) v).getPaint();
                                        textPaint2.setAntiAlias(true);
                                        textPaint2.setStyle(Paint.Style.FILL);
                                        textPaint2.setStrokeWidth(1);
                                    } else {
                                        ((TextView) v).setTextColor(foreColor);
                                        ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                                        TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                    }
                                }
                            }
                            for (int i = 0; i < mCardConstraint.getChildCount(); i++) {
                                View v = mCardConstraint.getChildAt(i);
                                if ((v instanceof Chronometer) &&  (v.getId() != R.id.chronoClock) && (v.getVisibility() == Chronometer.VISIBLE)) {
                                    ((Chronometer) v).setTextColor(foreColor);
                                    ((Chronometer) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                                } else if (v instanceof TextView) {
                                    if ((v.getId() == R.id.textViewCenter3) || (v.getId() == R.id.textViewCenter1)){

                                    }else
                                    if ((v.getId() == R.id.textViewMsgLeft) || (v.getId() == R.id.textViewMsgRight)) {
                                        ((TextView) v).setTextColor(foreColor);
                                        ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                                        TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                    }else
                                        if ((v.getId() == R.id.textViewCenter)) {
                                            ((TextView) v).setTextColor(foreColor2);
                                            TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                        } else if (v.getId() == R.id.chronoClock)  {
                                            ((TextView) v).setTextColor(foreColor);
                                            TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                            ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, bigTextSize);
                                            Paint textPaint2 = ((TextView) v).getPaint();
                                            textPaint2.setAntiAlias(true);
                                            textPaint2.setStyle(Paint.Style.FILL);
                                            textPaint2.setStrokeWidth(1);
                                        } else {
                                            ((TextView) v).setTextColor(foreColor);
                                            TextViewCompat.setCompoundDrawableTintList((TextView) v, tintList);
                                        }
                                }
                            }
                        });
            loadDataAndUpdateScreen();
            prevAmbient = isAmbient;
            });
        }catch (Exception e){
            Log.e(LOG_TAG,"exitAmbient " + e.getMessage());
        }
    }


    private <T extends View & CircularRevealWidget> void circularRevealFromMiddle(@NonNull final T circularRevealWidget, final int durationMs) {
        circularRevealWidget.post(() -> {
            int viewWidth = circularRevealWidget.getWidth();
            int viewHeight = circularRevealWidget.getHeight();
            Context context = circularRevealWidget.getContext();
            if (context == null) context = getActivity().getApplicationContext();
            if (context == null) return;
            int viewDiagonal = (int) Math.sqrt(viewWidth * viewWidth + viewHeight * viewHeight);
            int scrimColor = ContextCompat.getColor(context, R.color.secondaryColor);
            try {
                final AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(
                        CircularRevealCompat.createCircularReveal(circularRevealWidget, viewWidth / 2, viewHeight / 2, 10, viewDiagonal / 2),
                        ObjectAnimator.ofArgb(circularRevealWidget, CircularRevealWidget.CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR, scrimColor, Color.TRANSPARENT));
                animatorSet.setDuration(durationMs);
                animatorSet.start();
            }catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG, e.getMessage());
            }
        });
    }
    private boolean isNetworkConnected() {
        boolean isConnected = false;
        try {
            final ConnectivityManager cm = (getContext() != null) ? (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE) : null;
            if (cm != null) {
                final Network n = cm.getActiveNetwork();

                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                    isConnected =(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                }
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            isConnected = false;
        }

        return isConnected;
    }
}
