package com.a_track_it.workout.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.SettingsLogoAdapter;
import com.a_track_it.workout.adapter.SimpleListAdapter;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Configuration;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.user_model.MessagesViewModel;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.wearable.Node;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;

/**
 * to handle interaction events.
 * Use the {@link SettingsDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsDialog extends DialogFragment {
    public static final String TAG = SettingsDialog.class.getSimpleName();
    private FragmentInterface mListener;
    private View rootView;
    public int flagFragment;
    private boolean resetBindings;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private ReferencesTools mReferenceTools;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private GoogleSignInAccount mGoogleAccount;
    public Set<Node> wearNodesWithApp = new HashSet<>();
    private static final int REQUEST_IMAGE_PICKER = 5010;
    public SettingsDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsDialog.
     */

    public static SettingsDialog newInstance(int iIndicator, Set<Node> nodeSet) {
        final SettingsDialog fragment = new SettingsDialog();
        fragment.flagFragment = iIndicator;
        if (nodeSet != null) {
             fragment.wearNodesWithApp.addAll(nodeSet);
        }
        return fragment;
    }
    View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, null);
        }
    };

/*    private class setupNodesList implements Runnable{
        private Context context;
        setupNodesList(Context c){ this.context = c; }

        @Override
        public void run() {
        if ((flagFragment > 0) && (wearNodesWithApp == null) || wearNodesWithApp.isEmpty()) {
            Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(context)
                    .getCapability(Constants.WEAR_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE);
            capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
                @Override
                public void onComplete(Task<CapabilityInfo> task) {
                    if (task.isSuccessful()) {
                        CapabilityInfo capabilityInfo = task.getResult();
                        Set<Node> connectedNodes = capabilityInfo.getNodes();
                        String sNodeText = Constants.ATRACKIT_EMPTY;
                        if (capabilityInfo.getName().equals(Constants.WEAR_CAPABILITY_NAME))
                            if (connectedNodes.size() > 0)
                                for (Node node : connectedNodes) {
                                    if (!wearNodesWithApp.contains(node)) {
                                        Log.e(TAG, "PHONE WEAR CAPABILITY node " + node.getDisplayName());
                                        wearNodesWithApp.add(node);
                                        if (sNodeText.length() == 0)
                                            sNodeText = node.getDisplayName();
                                        else
                                            sNodeText += Constants.LINE_DELIMITER + node.getDisplayName();
                                    }
                                }
                            else {
                                if (appPrefs.getLastNodeName().length() > 0) {
                                    sNodeText = appPrefs.getLastNodeName() + " n/a";
                                } else
                                    sNodeText = "Not setup yet";
                            }
                        mMessagesViewModel.setNodeDisplayName(sNodeText);
                    }
                }
            });
        }
        }
    }
 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
        mMessagesViewModel.addLiveIntent(refreshIntent);
        if (resetBindings)
            mListener.onItemSelected(Constants.SELECTION_SENSOR_BINDINGS,0,null,0,null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = (container != null) ? container.getContext() : getActivity().getApplicationContext();
        rootView = inflater.inflate(R.layout.dialog_settings, container, false);
        mReferenceTools = ReferencesTools.setInstance(context);
        mGoogleAccount = GoogleSignIn.getLastSignedInAccount(context);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(context.getApplicationContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        final AssetManager asm = context.getAssets();
        appPrefs = ApplicationPreferences.getPreferences(context);
        final String sUserId = (mGoogleAccount != null) ? mGoogleAccount.getId() : appPrefs.getLastUserID();
        userPrefs = UserPreferences.getPreferences(context, sUserId);
        final Drawable drawableChecked = AppCompatResources.getDrawable(context,R.drawable.ic_outline_check_white);
        final Drawable drawableUseLocation = AppCompatResources.getDrawable(context,R.drawable.ic_location_enabled_white);

        Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(context, R.color.colorSplash));
        Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(context, R.color.secondaryTextColor));
        Drawable drawableDeniedLocation = AppCompatResources.getDrawable(context,R.drawable.ic_location_disabled_white);
        Typeface typeface = Typeface.createFromAsset(asm, Constants.ATRACKIT_FONT);
        Long lHistoryStart = 0L;
        Long lHistoryEnd = 0L;
        Configuration configHistory = null;
        List<Configuration> existingConfigs = mSessionViewModel.getConfigurationLikeName(Constants.MAP_HISTORY_RANGE, sUserId);
        if (existingConfigs.size() > 0) {
            configHistory = existingConfigs.get(0);
            lHistoryStart = Long.parseLong(configHistory.stringValue1);
            lHistoryEnd = Long.parseLong(configHistory.stringValue2);
        }

       RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.settings_logoRecyclerView);
       RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
       recyclerView.setLayoutManager(layoutManager);
       final SettingsLogoAdapter logoAdapter = new SettingsLogoAdapter(context, userPrefs);
       logoAdapter.setOnItemClickListener((view, useRounded, position) -> {
           if (userPrefs.getUseRoundedImage() != useRounded){
               userPrefs.setUseRoundedImage(useRounded);
           }
          switch (position){
              case 0:
                  userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_LOGO);
                  break;
              case 1:
                  userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_INT_FILE);
                  break;
              case 2:
                  userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_EXT_FILE);
                  break;
              case 3:
                  userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE,Constants.LABEL_CAMERA_FILE);
                  break;
          }
          Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
          refreshIntent.putExtra(Constants.KEY_FIT_USER, sUserId);
          refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
          context.sendBroadcast(refreshIntent);
       });
       logoAdapter.setUseRounded(userPrefs.getUseRoundedImage());
       recyclerView.setAdapter(logoAdapter);
       TextView textViewUserName = rootView.findViewById(R.id.settings_user_name);
       final String sUser = (mGoogleAccount != null) ? mGoogleAccount.getDisplayName() : userPrefs.getLastUserName();
        if (this.flagFragment > 0){
            final androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.toolbar);
            toolbar.setTitle("Settings");
            if (toolbar != null){
                Drawable drawableUnChecked2 = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
                Utilities.setColorFilter(drawableUnChecked2, ContextCompat.getColor(context, R.color.secondaryTextColor));
                toolbar.setNavigationIcon(drawableUnChecked2);
            }
            toolbar.setNavigationOnClickListener(v -> {
                mListener.OnFragmentInteraction(android.R.id.home,0,null);
            });
        }
        textViewUserName.setText(sUser);
        if (typeface != null)
            textViewUserName.setTypeface(typeface);
        if (sUser.length() <= 15)
            textViewUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
        if (sUser.length() <= 23)
            textViewUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP,26);
        else
            textViewUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP,21);

        TextView textViewPhoneName = rootView.findViewById(R.id.settings_phone_name);
        mMessagesViewModel.getNodeDisplayName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String sPhone = s;
                if (sPhone.length() == 0)
                    sPhone = context.getString(R.string.label_settings_phone_name);
                textViewPhoneName.setText(sPhone);
            }
        });
        com.google.android.material.button.MaterialButton signout_phone = rootView.findViewById(R.id.settings_sign_out_button);
        signout_phone.setTag(Constants.UID_settings_sign_out_button);
        signout_phone.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton find_phone = rootView.findViewById(R.id.settings_find_phone_button);
        find_phone.setTag(Constants.UID_settings_find_phone_button);
        find_phone.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton setup_age = rootView.findViewById(R.id.settings_age_button);
        setup_age.setTag(Constants.UID_settings_age_button);
        setup_age.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton setup_height = rootView.findViewById(R.id.settings_height_button);
        setup_height.setTag(Constants.UID_settings_height_button);
        setup_height.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton btnNotifications = rootView.findViewById(R.id.settings_notifications_button);
        btnNotifications.setTag(Constants.UID_settings_notifications_button);
        btnNotifications.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton settings_has_device_toggle = rootView.findViewById(R.id.settings_has_device_toggle);
        settings_has_device_toggle.setChecked(userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE));
        com.google.android.material.button.MaterialButton settings_sensors = rootView.findViewById(R.id.btn_settings_sensors);
        settings_sensors.setTag(Constants.UID_settings_sensors_button);
        settings_sensors.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton settings_show_goals_toggle = rootView.findViewById(R.id.settings_show_goals_toggle);
        settings_show_goals_toggle.setChecked(userPrefs.getPrefByLabel(Constants.USER_PREF_SHOW_GOALS));
        settings_show_goals_toggle.setTag(Constants.UID_settings_show_goals_button);
        settings_show_goals_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            int setValue = (isChecked) ? 1 : 0;
            mListener.OnFragmentInteraction(Constants.UID_settings_show_goals_button,setValue, Constants.ATRACKIT_EMPTY); // pass to MessagesViewModel in parent Activity
            if (isChecked)
                ((MaterialButton)v).setIcon(drawableChecked);
            else
                ((MaterialButton)v).setIcon(drawableUnChecked);
        });
        if (userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_ASKED) && userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE)){
            settings_has_device_toggle.setText(getString(R.string.has_device_text));
            settings_has_device_toggle.setIcon(drawableChecked);
            rootView.findViewById(R.id.settings_phone_name).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.settings_phone_label).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.settings_find_phone_button).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.settings_get_info).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.settings_send_info).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.settings_device_int_label).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.btnSettingsDeviceIntPlus).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.btnSettingsDeviceInt).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.btnSettingsDeviceIntMinus).setVisibility(View.VISIBLE);
            mMessagesViewModel.setNodeDisplayName(appPrefs.getLastNodeName());
           // new setupNodesList(context).run();
        }else {
            settings_has_device_toggle.setText(getString(R.string.label_settings_dont_use));
            settings_has_device_toggle.setIcon(drawableUnChecked);
            rootView.findViewById(R.id.settings_phone_name).setVisibility(View.GONE);
            rootView.findViewById(R.id.settings_phone_label).setVisibility(View.GONE);
            rootView.findViewById(R.id.settings_find_phone_button).setVisibility(View.GONE);
            rootView.findViewById(R.id.settings_get_info).setVisibility(View.GONE);
            rootView.findViewById(R.id.settings_send_info).setVisibility(View.GONE);
            rootView.findViewById(R.id.btnSettingsDeviceIntPlus).setVisibility(View.GONE);
            rootView.findViewById(R.id.settings_device_int_label).setVisibility(View.GONE);
            rootView.findViewById(R.id.btnSettingsDeviceInt).setVisibility(View.GONE);
            rootView.findViewById(R.id.btnSettingsDeviceIntMinus).setVisibility(View.GONE);
        }
        final MaterialButton btnSettingsDeviceIntPlus = rootView.findViewById(R.id.btnSettingsDeviceIntPlus);
        final TextView btnSettingsDeviceInt = rootView.findViewById(R.id.btnSettingsDeviceInt);
        btnSettingsDeviceIntPlus.setOnClickListener(v -> {
            long lValue = appPrefs.getPhoneSyncInterval();
            lValue = TimeUnit.MILLISECONDS.toMinutes(lValue);
            lValue++;
            String sText = Long.toString(lValue);
            btnSettingsDeviceInt.setText(sText);
            appPrefs.setPhoneSyncInterval(TimeUnit.MINUTES.toMillis(lValue));
        });

        long lValue11 = appPrefs.getPhoneSyncInterval();
        String sText11 = Long.toString(TimeUnit.MILLISECONDS.toMinutes(lValue11));
        btnSettingsDeviceInt.setText(sText11);
        final MaterialButton btnSettingsDeviceIntMinus = rootView.findViewById(R.id.btnSettingsDeviceIntMinus);
        btnSettingsDeviceIntMinus.setOnClickListener(v -> {
            long lValue2 = appPrefs.getPhoneSyncInterval();
            lValue2 = TimeUnit.MILLISECONDS.toMinutes(lValue2);
            if ((lValue2-1) >= 15) lValue2--;
            else{
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, "15 min is minimum allowed");
                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                mMessagesViewModel.addLiveIntent(msgIntent);
            }
            String sText2 = Long.toString(lValue2);
            btnSettingsDeviceInt.setText(sText2);
            appPrefs.setPhoneSyncInterval(TimeUnit.MINUTES.toMillis(lValue2));
        });
        settings_has_device_toggle.setOnClickListener((View.OnClickListener) v -> {
            boolean isChecked = !userPrefs.getPrefByLabel(Constants.LABEL_DEVICE_USE);
            int setValue = (isChecked) ? 1 : 0;
            userPrefs.setPrefByLabel(Constants.LABEL_DEVICE_USE, isChecked);
            mListener.OnFragmentInteraction(Constants.UID_settings_has_device_toggle,setValue, Constants.ATRACKIT_EMPTY); // pass to MessagesViewModel in parent Activity
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (isChecked) {
                        ((MaterialButton) v).setText(getString(R.string.has_device_text));
                        ((MaterialButton) v).setIcon(drawableChecked);
                        rootView.findViewById(R.id.settings_phone_name).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.settings_phone_label).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.settings_get_info).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.settings_send_info).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.settings_find_phone_button).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.settings_device_int_label).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.btnSettingsDeviceIntPlus).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.btnSettingsDeviceInt).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.btnSettingsDeviceIntMinus).setVisibility(View.VISIBLE);
                    } else {
                        ((MaterialButton) v).setText(getString(R.string.label_settings_dont_use));
                        ((MaterialButton) v).setIcon(drawableUnChecked);
                        rootView.findViewById(R.id.settings_phone_name).setVisibility(View.GONE);
                        rootView.findViewById(R.id.settings_phone_label).setVisibility(View.GONE);
                        rootView.findViewById(R.id.settings_get_info).setVisibility(View.GONE);
                        rootView.findViewById(R.id.settings_send_info).setVisibility(View.GONE);
                        rootView.findViewById(R.id.settings_find_phone_button).setVisibility(View.GONE);
                        rootView.findViewById(R.id.settings_device_int_label).setVisibility(View.GONE);
                        rootView.findViewById(R.id.btnSettingsDeviceIntPlus).setVisibility(View.GONE);
                        rootView.findViewById(R.id.btnSettingsDeviceInt).setVisibility(View.GONE);
                        rootView.findViewById(R.id.btnSettingsDeviceIntMinus).setVisibility(View.GONE);
                    }
                }
            });
        });
        com.google.android.material.button.MaterialButton btnPhoneGet = rootView.findViewById(R.id.settings_get_info);
        btnPhoneGet.setTag(Constants.UID_settings_get_info);
        btnPhoneGet.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton btnPhoneSend = rootView.findViewById(R.id.settings_send_info);
        btnPhoneSend.setTag(Constants.UID_settings_send_info);
        btnPhoneSend.setOnClickListener(myClicker);
       /* com.google.android.material.button.MaterialButton settings_firebase_toggle = rootView.findViewById(R.id.settings_firebase_toggle);
        settings_firebase_toggle.setEnabled(appPrefs.getFirebaseAvail());
        settings_firebase_toggle.setChecked(userPrefs.getUseFirebase());
        if (appPrefs.getFirebaseAvail()) {
            if (userPrefs.getUseFirebase()) {
                settings_firebase_toggle.setText(getString(R.string.label_settings_firebase_online));
                settings_firebase_toggle.setIcon(drawableChecked);
            }else {
                settings_firebase_toggle.setText(getString(R.string.label_settings_firebase_offline));
                settings_firebase_toggle.setIcon(drawableUnChecked);
            }

        }else{
            if (appPrefs.getLastNodeName().length() > 0) {
                settings_firebase_toggle.setText(getString(R.string.label_settings_firebase_via_phone));
                settings_firebase_toggle.setIcon(drawableChecked);
            }else {
                settings_firebase_toggle.setText(getString(R.string.label_settings_firebase_unavail));
                settings_firebase_toggle.setIcon(drawableUnChecked);
            }
        }
        settings_firebase_toggle.setOnClickListener(v -> {
            boolean isChecked = !userPrefs.getUseFirebase();
            userPrefs.setUseFirebase(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setText(getString(R.string.label_settings_firebase_online));
                ((MaterialButton) v).setIcon(drawableChecked);
            }else {
                ((MaterialButton) v).setText(getString(R.string.label_settings_firebase_offline));
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });*/
        if (lHistoryStart == 0) {
            ((TextView) rootView.findViewById(R.id.settings_history_start)).setText(getString(R.string.label_settings_history_not_loaded));
            ((TextView) rootView.findViewById(R.id.settings_history_end)).setVisibility(View.GONE);
        }else {
            ((TextView) rootView.findViewById(R.id.settings_history_start)).setText(Utilities.getDateString(lHistoryStart));
            ((TextView) rootView.findViewById(R.id.settings_history_end)).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.settings_history_start)).setText(Utilities.getDateString(lHistoryEnd));
        }
        com.google.android.material.button.MaterialButton btnHistory = rootView.findViewById(R.id.settings_load_history_button);
        btnHistory.setTag(Constants.UID_settings_load_history_button);
        btnHistory.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton btnGoals = rootView.findViewById(R.id.settings_load_goals_button);
        btnGoals.setTag(Constants.UID_settings_load_goals_button);
        btnGoals.setOnClickListener(myClicker);
        final com.google.android.material.button.MaterialButton settings_auto_start_toggle = rootView.findViewById(R.id.settings_auto_start_toggle);
        settings_auto_start_toggle.setChecked(userPrefs.getRestAutoStart());
        if (userPrefs.getRestAutoStart()){
            settings_auto_start_toggle.setIcon(drawableChecked);
        }else {
            settings_auto_start_toggle.setIcon(drawableUnChecked);
        }
        settings_auto_start_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setRestAutoStart(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        final com.google.android.material.button.MaterialButton btnRest = rootView.findViewById(R.id.btnSettingsRest);
        int iWeightRest = userPrefs.getWeightsRestDuration();
        setDurationRest(iWeightRest);
        btnRest.setTag(Constants.UID_btnSettingsRest);
        btnRest.setOnClickListener(myClicker);
        boolean bTemp = userPrefs.getTimedRest();
        com.google.android.material.button.MaterialButton confirm_use_timed_rest_toggle = rootView.findViewById(R.id.settings_use_timed_rest_toggle);
        confirm_use_timed_rest_toggle.setChecked(bTemp);
        if (bTemp){
            confirm_use_timed_rest_toggle.setIcon(drawableChecked);
            settings_auto_start_toggle.setVisibility(View.VISIBLE);
            btnRest.setVisibility(MaterialButton.VISIBLE);
        }else {
            confirm_use_timed_rest_toggle.setIcon(drawableUnChecked);
            settings_auto_start_toggle.setVisibility(View.GONE);
            btnRest.setVisibility(MaterialButton.GONE);
        }
        confirm_use_timed_rest_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setTimedRest(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
                settings_auto_start_toggle.setVisibility(View.VISIBLE);
                btnRest.setVisibility(MaterialButton.VISIBLE);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                settings_auto_start_toggle.setVisibility(View.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
            }
        });
        com.google.android.material.button.MaterialButton use_notification_toggle = rootView.findViewById(R.id.settings_use_notifications);
        bTemp = userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION);
        use_notification_toggle.setChecked(bTemp);
        if (bTemp)
            use_notification_toggle.setIcon(drawableChecked);
        else
            use_notification_toggle.setIcon(drawableUnChecked);
        use_notification_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION, isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton use_audio_toggle = rootView.findViewById(R.id.settings_use_audio);
        if (!Utilities.hasSpeaker(context)){
            use_audio_toggle.setText(getString(R.string.label_settings_speaker_not_avail));
            use_audio_toggle.setEnabled(false);
            use_audio_toggle.setChecked(false);
        }else {
            bTemp = userPrefs.getPrefByLabel(Constants.USER_PREF_USE_AUDIO);
            use_audio_toggle.setChecked(bTemp);
            if (bTemp)
                use_audio_toggle.setIcon(drawableChecked);
            else
                use_audio_toggle.setIcon(drawableUnChecked);
            use_audio_toggle.setOnClickListener(v -> {
                boolean isChecked = ((MaterialButton) v).isChecked();
                userPrefs.setPrefByLabel(Constants.USER_PREF_USE_AUDIO, isChecked);
                if (isChecked) {
                    ((MaterialButton) v).setIcon(drawableChecked);
                } else {
                    ((MaterialButton) v).setIcon(drawableUnChecked);
                }
            });
        }
        com.google.android.material.button.MaterialButton use_vibrate_toggle = rootView.findViewById(R.id.settings_use_vibrate);
        if (!Utilities.hasVibration(context)){
            use_vibrate_toggle.setText(getString(R.string.confirm_use_vibrate_not_avail));
            use_vibrate_toggle.setEnabled(false);
            use_audio_toggle.setChecked(false);
        }else {
            bTemp = userPrefs.getPrefByLabel(Constants.USER_PREF_USE_VIBRATE);
            use_vibrate_toggle.setChecked(bTemp);
            if (bTemp)
                use_vibrate_toggle.setIcon(drawableChecked);
            else
                use_vibrate_toggle.setIcon(drawableUnChecked);
            use_vibrate_toggle.setOnClickListener(v -> {
                boolean isChecked = ((MaterialButton) v).isChecked();
                userPrefs.setPrefByLabel(Constants.USER_PREF_USE_VIBRATE, isChecked);
                if (isChecked) {
                    ((MaterialButton) v).setIcon(drawableChecked);
                } else {
                    ((MaterialButton) v).setIcon(drawableUnChecked);
                }
            });
        }        
        com.google.android.material.button.MaterialButton confirm_start_toggle = rootView.findViewById(R.id.settings_start_toggle);
        bTemp = userPrefs.getConfirmStartSession();
        confirm_start_toggle.setChecked(bTemp);
        if (bTemp)
            confirm_start_toggle.setIcon(drawableChecked);
        else
            confirm_start_toggle.setIcon(drawableUnChecked);
        confirm_start_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setConfirmStartSession( isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton confirm_set_toggle = rootView.findViewById(R.id.settings_set_toggle);
        bTemp = userPrefs.getConfirmSetSession();
        confirm_set_toggle.setChecked(bTemp);
        if (bTemp)
            confirm_set_toggle.setIcon(drawableChecked);
        else
            confirm_set_toggle.setIcon(drawableUnChecked);
        confirm_set_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setConfirmSetSession(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton confirm_deletetoggle = rootView.findViewById(R.id.settings_delete_toggle);
        bTemp = userPrefs.getConfirmDeleteSession();
        confirm_deletetoggle.setChecked(bTemp);
        if (bTemp)
            confirm_deletetoggle.setIcon(drawableChecked);
        else
            confirm_deletetoggle.setIcon(drawableUnChecked);
        confirm_deletetoggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setConfirmDeleteSession(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton confirm_end_toggle = rootView.findViewById(R.id.settings_end_toggle);
        bTemp = userPrefs.getConfirmEndSession();
        confirm_end_toggle.setChecked(bTemp);
        if (bTemp)
            confirm_end_toggle.setIcon(drawableChecked);
        else
            confirm_end_toggle.setIcon(drawableUnChecked);
        confirm_end_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setConfirmEndSession(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton settings_vehicle_toggle = rootView.findViewById(R.id.settings_vehicle_toggle);
        bTemp = userPrefs.getPrefByLabel(Constants.USER_PREF_STOP_INVEHICLE);
        settings_vehicle_toggle.setChecked(bTemp);
        if (bTemp)
            settings_vehicle_toggle.setIcon(drawableChecked);
        else
            settings_vehicle_toggle.setIcon(drawableUnChecked);
        settings_vehicle_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setPrefByLabel(Constants.USER_PREF_STOP_INVEHICLE,isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton confirm_exit_toggle = rootView.findViewById(R.id.settings_exit_toggle);
        bTemp = userPrefs.getConfirmExitApp();
        confirm_exit_toggle.setChecked(bTemp);
        if (bTemp)
            confirm_exit_toggle.setIcon(drawableChecked);
        else
            confirm_exit_toggle.setIcon(drawableUnChecked);
        confirm_exit_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            userPrefs.setConfirmExitApp(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        com.google.android.material.button.MaterialButton settings_use_kg = rootView.findViewById(R.id.settings_use_kg);
        bTemp = userPrefs.getUseKG();
        settings_use_kg.setChecked(bTemp);
        if (bTemp) {
            settings_use_kg.setText(getString(R.string.label_settings_use_kg_units));
        }else
            settings_use_kg.setText(getString(R.string.label_settings_use_lbs_units));

        settings_use_kg.setOnClickListener(v -> {
            boolean isChecked = !userPrefs.getUseKG();
            userPrefs.setUseKG(isChecked);
            if (isChecked)
                ((MaterialButton)v).setText(getString(R.string.label_settings_use_kg_units));
            else
                ((MaterialButton)v).setText(getString(R.string.label_settings_use_lbs_units));
        });
        com.google.android.material.button.MaterialButton settings_read_permissions = rootView.findViewById(R.id.settings_read_permissions);
        settings_read_permissions.setTag(Constants.UID_settings_read_permissions);
        final boolean bHasFitnessPermission = hasOAuthPermission();

        settings_read_permissions.setChecked(bHasFitnessPermission);
        if (bHasFitnessPermission)
            settings_read_permissions.setIcon(drawableChecked);
        else
            settings_read_permissions.setIcon(drawableUnChecked);
        settings_read_permissions.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
                ((MaterialButton) v).setText(getString(R.string.recog_permission_granted));
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                ((MaterialButton) v).setText(getString(R.string.recog_permission_denied));
            }
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),0,null);
            if (!isChecked) {
                if (getDialog() != null) getDialog().dismiss();
            }

        });

        boolean bSensorPermission = (ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED);
        String sSensorMsg = (bSensorPermission) ? getString(R.string.sensors_permission_granted) : getString(R.string.sensors_permission_available);
        com.google.android.material.button.MaterialButton settings_sensor_permissions = rootView.findViewById(R.id.settings_sensors_permissions);
        settings_sensor_permissions.setTag(Constants.UID_settings_sensors_permissions);
        settings_sensor_permissions.setText(sSensorMsg);
        settings_sensor_permissions.setCheckable(true);
        settings_sensor_permissions.setChecked(bSensorPermission);
        if (bSensorPermission)
            settings_sensor_permissions.setIcon(drawableChecked);
        else
            settings_sensor_permissions.setIcon(drawableUnChecked);
        settings_sensor_permissions.setOnClickListener(myClicker);
        com.google.android.material.button.MaterialButton settings_sensor_use = rootView.findViewById(R.id.settings_sensors_use);
        settings_sensor_use.setTag(Constants.UID_settings_sensors_use);
        settings_sensor_use.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableUseLocation);
                ((MaterialButton) v).setText(getString(R.string.sensors_permission_used));
                mListener.OnFragmentInteraction((int)v.getTag(),1,null);
            } else {
                ((MaterialButton) v).setIcon(drawableDeniedLocation);
                ((MaterialButton) v).setText(getString(R.string.sensors_permission_denied));
                mListener.OnFragmentInteraction((int)v.getTag(),0,null);
            }
        });
        if (!appPrefs.getUseSensors()) {
            settings_sensor_use.setChecked(false);
            if (bSensorPermission)
                settings_sensor_use.setText(getString(R.string.sensors_permission_not_used));
            else
                settings_sensor_use.setText(sSensorMsg);
            settings_sensor_use.setIcon(drawableDeniedLocation);
        }else {
            settings_sensor_use.setChecked(bSensorPermission);
            if (bSensorPermission)
                settings_sensor_use.setText(getString(R.string.sensors_permission_used));
            else
                settings_sensor_use.setText(sSensorMsg);
            if (bSensorPermission)
                settings_sensor_use.setIcon(drawableUseLocation);
            else
                settings_sensor_use.setIcon(drawableDeniedLocation);
        }

        com.google.android.material.button.MaterialButton settings_location_permissions = rootView.findViewById(R.id.settings_location_permissions);
        com.google.android.material.button.MaterialButton settings_location_use = rootView.findViewById(R.id.settings_location_use);
        settings_location_permissions.setTag(Constants.UID_settings_location_permissions);
        settings_location_use.setTag(Constants.UID_settings_location_use);
        final boolean bLocationPermission = (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        String sLocMsg = (bLocationPermission) ? getString(R.string.location_permission_granted) : getString(R.string.location_permission_not_granted);
        settings_location_permissions.setChecked(bLocationPermission);
        settings_location_permissions.setText(sLocMsg);
        settings_sensor_permissions.setChecked(bSensorPermission);
        if (bLocationPermission)
            settings_location_permissions.setIcon(drawableChecked);
        else
            settings_location_permissions.setIcon(drawableUnChecked);
        settings_location_permissions.setOnClickListener(myClicker);
        if (!appPrefs.getUseLocation()) {
            settings_location_use.setChecked(false);
            if (bLocationPermission)
                settings_location_use.setText(getString(R.string.location_permission_not_used));
            else
                settings_location_use.setText(sLocMsg);
            settings_location_use.setIcon(drawableDeniedLocation);
        }else {
            settings_location_use.setChecked(bLocationPermission);
            if (!bLocationPermission)
                settings_location_use.setText(sLocMsg);
            else
                settings_location_use.setText(getString(R.string.location_permission_used));
            if (bLocationPermission)
                settings_location_use.setIcon(drawableUseLocation);
            else
                settings_location_use.setIcon(drawableDeniedLocation);
        }

        settings_location_use.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton)v).isChecked();
            if (!isChecked) {
                mListener.OnFragmentInteraction(Constants.UID_settings_location_use,0,null);
                mMessagesViewModel.setUseLocation(true);
            }else{
                mListener.OnFragmentInteraction(Constants.UID_settings_location_use,1,null);
                mMessagesViewModel.setUseLocation(false);

            }
            if (!isChecked) {
                settings_location_use.setText(getString(R.string.location_permission_denied));
                settings_location_use.setIcon(drawableDeniedLocation);
            }else {
                settings_location_use.setText(getString(R.string.location_permission_granted));
                settings_location_use.setIcon(drawableUseLocation);
            }

        });
        long lValue = userPrefs.getDefaultNewSets();
        String sText = Long.toString(lValue);
        final MaterialButton btnSettingsSetsPlus = rootView.findViewById(R.id.btnSettingsSetsPlus);
        final TextView btnSettingsSets = rootView.findViewById(R.id.btnSettingsSets);
        btnSettingsSets.setText(sText);
        btnSettingsSetsPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer lValue = userPrefs.getDefaultNewSets() + 1;
                String sText = Integer.toString(lValue);
                userPrefs.setDefaultNewSets(lValue);
                mSavedStateViewModel.setSetsDefault(lValue);
                btnSettingsSets.setText(sText);
            }
        });

        final MaterialButton btnSettingsSetsMinus = rootView.findViewById(R.id.btnSettingsSetsMinus);
        btnSettingsSetsMinus.setOnClickListener(v -> {
            if ((userPrefs.getDefaultNewSets() - 1) > 0) {
                Integer lValue15 = userPrefs.getDefaultNewSets() - 1;
                String sText15 = Integer.toString(lValue15);
                userPrefs.setDefaultNewSets(lValue15);
                mSavedStateViewModel.setSetsDefault(lValue15);
                btnSettingsSets.setText(sText15);
            }
        });
        final MaterialButton btnSettingsRepsPlus = rootView.findViewById(R.id.btnSettingsRepsPlus);
        final TextView btnSettingsReps = rootView.findViewById(R.id.btnSettingsReps);
        btnSettingsRepsPlus.setOnClickListener(v -> {
            Integer lValue16 = userPrefs.getDefaultNewReps() + 1;
            String sText16 = Integer.toString(lValue16);
            userPrefs.setDefaultNewReps(lValue16);
            mSavedStateViewModel.setRepsDefault(lValue16);
            btnSettingsReps.setText(sText16);
        });

        lValue = userPrefs.getDefaultNewReps();
        sText = Long.toString(lValue);
        btnSettingsReps.setText(sText);
        final MaterialButton btnSettingsRepsMinus = rootView.findViewById(R.id.btnSettingsRepsMinus);
        btnSettingsRepsMinus.setOnClickListener(v -> {
            if ((userPrefs.getDefaultNewReps() - 1) > 0) {
                Integer lValue17 = userPrefs.getDefaultNewReps() - 1;
                String sText17 = Integer.toString(lValue17);
                userPrefs.setDefaultNewReps(lValue17);
                mSavedStateViewModel.setRepsDefault(lValue17);
                btnSettingsReps.setText(sText17);
            }
        });
        final MaterialButton btnConfirmDurationPlus = rootView.findViewById(R.id.btnConfirmDurationPlus);
        final TextView btnConfirmDuration = rootView.findViewById(R.id.btnConfirmDuration);
        btnConfirmDurationPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long lValue = TimeUnit.MILLISECONDS.toSeconds(userPrefs.getConfirmDuration());
                lValue++;
                String sText = Long.toString(lValue);
                userPrefs.setConfirmDuration(TimeUnit.SECONDS.toMillis(lValue));
                btnConfirmDuration.setText(sText);
            }
        });

        lValue = userPrefs.getConfirmDuration();
        sText = Long.toString(TimeUnit.MILLISECONDS.toSeconds(lValue));
        btnConfirmDuration.setText(sText);
        final MaterialButton btnConfirmDurationMinus = rootView.findViewById(R.id.btnConfirmDurationMinus);
        btnConfirmDurationMinus.setOnClickListener(v -> {
            Long lValue18 = TimeUnit.MILLISECONDS.toSeconds(userPrefs.getConfirmDuration());
            if ((lValue18-1) >= 0) lValue18--;
            String sText18 = Long.toString(lValue18);
            userPrefs.setConfirmDuration(TimeUnit.SECONDS.toMillis(lValue18));
            btnConfirmDuration.setText(sText18);
        });
        final MaterialButton btnSettingsDailyPlus = rootView.findViewById(R.id.btnSettingsDailyPlus);
        final TextView btnSettingsDaily = rootView.findViewById(R.id.btnSettingsDaily);
        btnSettingsDailyPlus.setOnClickListener(v -> {
            long lValue19 = appPrefs.getDailySyncInterval();
            lValue19 = TimeUnit.MILLISECONDS.toMinutes(lValue19);
            lValue19++;
            String sText19 = Long.toString(lValue19);
            btnSettingsDaily.setText(sText19);
            appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(lValue19));
        });

        lValue = appPrefs.getDailySyncInterval();
        sText = Long.toString(TimeUnit.MILLISECONDS.toMinutes(lValue));
        btnSettingsDaily.setText(sText);
        final MaterialButton btnSettingsDailyMinus = rootView.findViewById(R.id.btnSettingsDailyMinus);
        btnSettingsDailyMinus.setOnClickListener(v -> {
            long lValue1 = appPrefs.getDailySyncInterval();
            lValue1 = TimeUnit.MILLISECONDS.toMinutes(lValue1);
            if ((lValue1-1) >= 0) lValue1--;
            String sText1 = Long.toString(lValue1);
            btnSettingsDaily.setText(sText1);
            appPrefs.setDailySyncInterval(TimeUnit.MINUTES.toMillis(lValue1));
        });
        final MaterialButton btnSettingsCloudIntPlus = rootView.findViewById(R.id.btnCloudSampleRatePlus);
        final TextView btnSettingsCloudInt = rootView.findViewById(R.id.btnCloudSampleRate);
        btnSettingsCloudIntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long lValue = appPrefs.getLastSyncInterval();
                lValue = TimeUnit.MILLISECONDS.toMinutes(lValue);
                lValue++;
                String sText = Long.toString(lValue);
                btnSettingsCloudInt.setText(sText);
                appPrefs.setLastSyncInterval(TimeUnit.MINUTES.toMillis(lValue));
            }
        });

        lValue = appPrefs.getLastSyncInterval();
        sText = Long.toString(TimeUnit.MILLISECONDS.toMinutes(lValue));
        btnSettingsCloudInt.setText(sText);
        final MaterialButton btnSettingsCloudIntMinus = rootView.findViewById(R.id.btnCloudSampleRateMinus);
        btnSettingsCloudIntMinus.setOnClickListener(v -> {
            long lValue1 = appPrefs.getLastSyncInterval();
            lValue1 = TimeUnit.MILLISECONDS.toMinutes(lValue1);
            if ((lValue1-1) >= 15) lValue1--;
            else{
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, "15 min is minimum allowed");
                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                mMessagesViewModel.addLiveIntent(msgIntent);
            }
            String sText1 = Long.toString(lValue1);
            btnSettingsCloudInt.setText(sText1);
            appPrefs.setLastSyncInterval(TimeUnit.MINUTES.toMillis(lValue1));
        });
        String sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "step";
        String sDeviceName;
        int sensorCount = appPrefs.getStepsSensorCount();
        if (sensorCount > 0){
            sDeviceName = appPrefs.getPrefStringByLabel(sLabelS);
            ((TextView)rootView.findViewById(R.id.steps_sample_devicelabel)).setText(sDeviceName);
            rootView.findViewById(R.id.steps_sample_devicelabel).setVisibility(View.VISIBLE);
            final MaterialButton btnStepsSampleRatePlus = rootView.findViewById(R.id.btnStepsSampleRatePlus);
            final TextView btnStepsSampleRate = rootView.findViewById(R.id.btnStepsSampleRate);
            btnStepsSampleRatePlus.setOnClickListener(v -> {
                Long lValue12 = userPrefs.getStepsSampleRate() + 10;
                String sText12 = Long.toString(lValue12);
                userPrefs.setStepsSampleRate(lValue12);
                resetBindings = true;
                btnStepsSampleRate.setText(sText12);
            });

            lValue = userPrefs.getStepsSampleRate();
            sText = Long.toString(lValue);
            btnStepsSampleRate.setText(sText);
            final MaterialButton btnStepsSampleRateMinus = rootView.findViewById(R.id.btnStepsSampleRateMinus);
            btnStepsSampleRateMinus.setOnClickListener(v -> {
                Long lValue13 = userPrefs.getStepsSampleRate() - 10;
                String sText13 = Long.toString(lValue13);
                resetBindings = true;
                userPrefs.setStepsSampleRate(lValue13);
                btnStepsSampleRate.setText(sText13);
            });
        }else {
            String sMessage = getString(R.string.label_settings_steps_not_avail);
            if (appPrefs.getPrefByLabel(sLabelS))
                sMessage = sMessage.replace("Not", "Is");
            ((TextView) rootView.findViewById(R.id.steps_sample_devicelabel)).setText(getString(R.string.label_settings_steps_not_avail));
            rootView.findViewById(R.id.steps_sample_rate_label).setVisibility(View.GONE);
            ((MaterialButton) rootView.findViewById(R.id.btnStepsSampleRatePlus)).setVisibility(MaterialButton.GONE);
            ((MaterialButton) rootView.findViewById(R.id.btnStepsSampleRateMinus)).setVisibility(MaterialButton.GONE);
            rootView.findViewById(R.id.btnStepsSampleRate).setVisibility(View.GONE);
        }
        sDeviceName = Constants.ATRACKIT_EMPTY;
        sensorCount = appPrefs.getBPMSensorCount();
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "bpm";
        if ((sensorCount > 0)){
            sDeviceName = appPrefs.getPrefStringByLabel(sLabelS);
            ((TextView)rootView.findViewById(R.id.bpm_sample_devicelabel)).setText(sDeviceName);
            rootView.findViewById(R.id.bpm_sample_devicelabel).setVisibility(View.VISIBLE);
            final MaterialButton btnBPMSampleRatePlus = rootView.findViewById(R.id.btnBPMSampleRatePlus);
            final TextView btnBPMSampleRate = rootView.findViewById(R.id.btnBPMSampleRate);
            lValue = userPrefs.getBPMSampleRate();
            sText = Long.toString(lValue);
            btnBPMSampleRate.setText(sText);

            btnBPMSampleRatePlus.setOnClickListener(v -> {
                Long lValue14 = userPrefs.getBPMSampleRate() + 10;
                String sText14 = Long.toString(lValue14);
                resetBindings = true;
                userPrefs.setBPMSampleRate(lValue14);
                btnBPMSampleRate.setText(sText14);
            });
            final MaterialButton btnBPMSampleRateMinus = rootView.findViewById(R.id.btnBPMSampleRateMinus);
            btnBPMSampleRateMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Long lValue = userPrefs.getBPMSampleRate() - 10;
                    String sText = Long.toString(lValue);
                    userPrefs.setBPMSampleRate(lValue);
                    resetBindings = true;
                    btnBPMSampleRate.setText(sText);
                }
            });
        }else{
            ((TextView) rootView.findViewById(R.id.bpm_sample_devicelabel)).setText(getString(R.string.label_settings_bpm_not_avail));
            rootView.findViewById(R.id.bpm_sample_rate_label).setVisibility(View.GONE);
            ((MaterialButton) rootView.findViewById(R.id.btnBPMSampleRatePlus)).setVisibility(MaterialButton.GONE);
            ((MaterialButton) rootView.findViewById(R.id.btnBPMSampleRateMinus)).setVisibility(MaterialButton.GONE);
            ((TextView) rootView.findViewById(R.id.btnBPMSampleRate)).setVisibility(TextView.GONE);
        }


        sensorCount = appPrefs.getPressureSensorCount() + appPrefs.getTempSensorCount() + appPrefs.getPressureSensorCount();
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "bpm";
        if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0) sensorCount++;
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "step";
        if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0) sensorCount++;

        sDeviceName = Constants.ATRACKIT_EMPTY;
        List<String> otherDeviceList = new ArrayList<>();
        otherDeviceList.add("Device Sensors: " + sensorCount);
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "bpm";
        if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0) otherDeviceList.add(appPrefs.getPrefStringByLabel(sLabelS));
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "step";
        if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0) otherDeviceList.add(appPrefs.getPrefStringByLabel(sLabelS));
        if (appPrefs.getTempSensorCount() > 0) {
            sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "temp";
            if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0)
                otherDeviceList.add(appPrefs.getPrefStringByLabel(sLabelS));
        }
        if (appPrefs.getPressureSensorCount() > 0) {
            sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "hPa";
            if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0)
                otherDeviceList.add(appPrefs.getPrefStringByLabel(sLabelS));
        }
        if (appPrefs.getHumiditySensorCount() > 0) {
            sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_sensor_name) + "humidity";
            if (appPrefs.getPrefStringByLabel(sLabelS).length() > 0)
                otherDeviceList.add(appPrefs.getPrefStringByLabel(sLabelS));
        }
        final MaterialButton btnOtherSampleRatePlus = rootView.findViewById(R.id.btnOtherSampleRatePlus);
        final TextView btnOtherSampleRate = rootView.findViewById(R.id.btnOtherSampleRate);
        lValue = userPrefs.getOthersSampleRate();
        sText = Long.toString(lValue);
        btnOtherSampleRate.setText(sText);
        btnOtherSampleRatePlus.setOnClickListener(v -> {
            Long lValue18 = userPrefs.getOthersSampleRate() + 10;
            String sText18 = Long.toString(lValue18);
            resetBindings = true;
            userPrefs.setOthersSampleRate(lValue18);
            btnOtherSampleRate.setText(sText18);
        });
        final MaterialButton btnOtherSampleRateMinus = rootView.findViewById(R.id.btnOtherSampleRateMinus);
        btnOtherSampleRateMinus.setOnClickListener(v -> {
            Long lValue17 = userPrefs.getOthersSampleRate() - 10;
            String sText17 = Long.toString(lValue17);
            resetBindings = true;
            userPrefs.setOthersSampleRate(lValue17);
            btnOtherSampleRate.setText(sText17);
        });
        sDeviceName = Constants.ATRACKIT_EMPTY;
        otherDeviceList.add("Google Sensors");
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
        String sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_STEP_COUNT_DELTA.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google Step: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google Steps ");
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_HEART_RATE_BPM.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google BPM: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google BPM" );
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_LOCATION_SAMPLE.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_LOCATION_SAMPLE.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google Location: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google Location");
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_WORKOUT_EXERCISE.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google Exercises: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google Exercises");
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_CALORIES_EXPENDED.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google Calories: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google Calories Expended");
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_MOVE_MINUTES.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Google Move Mins: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Google Move Mins");
        }
        sLabelS = context.getString(com.a_track_it.workout.common.R.string.label_fit_sensor) + DataType.TYPE_HEART_POINTS.getName();
        sLabelSource = context.getString(com.a_track_it.workout.common.R.string.label_fit_name) + DataType.TYPE_HEART_POINTS.getName();
        if (appPrefs.getPrefByLabel(sLabelS)){
            if (appPrefs.getPrefStringByLabel(sLabelSource).length() > 0)
                otherDeviceList.add("Heart Points: " + appPrefs.getPrefStringByLabel(sLabelSource));
            else
                otherDeviceList.add("Heart Points");
        }
        if (otherDeviceList.size() == 0) {
            ((TextView) rootView.findViewById(R.id.other_sample_devicelabel)).setText(getString(R.string.label_settings_other_not_avail));
            ((TextView) rootView.findViewById(R.id.other_sample_devicelabel)).setTextSize(18);
            ((RecyclerView)rootView.findViewById(R.id.other_device_list)).setVisibility(RecyclerView.GONE);
        }else{
            final RecyclerView listOtherDevices = (RecyclerView)rootView.findViewById(R.id.other_device_list);
            listOtherDevices.setHasFixedSize(true);
            // use a linear layout manager
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(context);
            listOtherDevices.setLayoutManager(layoutManager2);
            SimpleListAdapter otherAdapter = new SimpleListAdapter(context, otherDeviceList);
            listOtherDevices.setAdapter(otherAdapter);
        }
        resetBindings = false;
        return rootView;
    }
    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = mReferenceTools.getFitnessSignInOptions(0);
        if (mGoogleAccount == null) mGoogleAccount = GoogleSignIn.getAccountForExtension(getContext(), fitnessOptions);
        return GoogleSignIn.hasPermissions(mGoogleAccount, fitnessOptions);
    }
    public void setDurationRest(int iRestSeconds){
        com.google.android.material.button.MaterialButton btnRest = rootView.findViewById(R.id.btnSettingsRest);
        long milliseconds = (TimeUnit.SECONDS.toMillis(iRestSeconds));
        String sTemp;
        if (milliseconds == 0)
            btnRest.setText(getString(R.string.action_untimed_rest));
        else {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            sTemp = getString(R.string.label_rest_countdown) + Constants.ATRACKIT_SPACE;
            if (hours > 0) {
                sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, hours) + Constants.ATRACKIT_SPACE + Constants.HOURS_TAIL;
                if (hours == 1)
                    sTemp = sTemp.substring(0, sTemp.length() - 1);
                if (minutes > 1)
                    sTemp += Constants.ATRACKIT_SPACE;
            }
            if (minutes > 0) {
                if (seconds == 0) {
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, minutes) + Constants.ATRACKIT_SPACE + Constants.MINS_TAIL;
                    if (minutes == 1)
                        sTemp = sTemp.substring(0, sTemp.length() - 1);
                } else
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, minutes) + Constants.SHOT_XY_DELIM + String.format(Locale.getDefault(), Constants.SINGLE_INT, seconds);
            } else {
                if (seconds > 0)
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, seconds) + Constants.ATRACKIT_SPACE + Constants.SECS_TAIL;
            }
            if (btnRest != null) btnRest.setText(sTemp);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
        } 
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

  }
