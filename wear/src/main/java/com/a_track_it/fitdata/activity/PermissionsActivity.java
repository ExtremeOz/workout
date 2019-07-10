package com.a_track_it.fitdata.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.google.android.gms.fitness.data.DataType;

public class PermissionsActivity extends AppCompatActivity implements CustomConfirmDialog.ICustomConfirmDialog,
        ActivityCompat.OnRequestPermissionsResultCallback, AmbientModeSupport.AmbientCallbackProvider {
    public static final String TAG = PermissionsActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_BODY_SENSORS = 5005;
    private static final int PERMISSION_REQUEST_LOCATION = 5006;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 5007;
    public static final String ARG_REQUESTS = "SENSORY_TYPE";
    public static final String REQUEST_ARG_RECORDING = "RECORDING";
    public static final String REQUEST_ARG_LOCATION = "LOCATION";
    public static final String REQUEST_ARG_SENSORS = "SENSORS";
    private boolean permissions_Ok = false;
    private boolean permissionsInProgress = false;
    private CustomConfirmDialog mCustomConfirmDialog;

    private boolean bRequestAll = false;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        mTextView = (TextView) findViewById(R.id.text_permissions);
        if (mTextView != null) mTextView.setText(R.string.checking_device_permissions);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            bRequestAll = true;
            requestLocationPermission();
        }else
            if (extras.containsKey(ARG_REQUESTS)){
                String sType = extras.getString(ARG_REQUESTS);
                if (sType.equals(REQUEST_ARG_RECORDING))
                    requestMicroPhonePermission();
                if (sType.equals(REQUEST_ARG_LOCATION))
                    requestLocationPermission();
            }

    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new thisAmbientCallback();
    }

    private class thisAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
            //  android.util.Log.d(TAG, "onEnterAmbient() " + ambientDetails);

            boolean mIsLowBitAmbient =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
            boolean mDoBurnInProtection =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
            /* Clears Handler queue (only needed for updates in active mode). */
            if (mCustomConfirmDialog != null) mCustomConfirmDialog.onEnterAmbientInFragment(ambientDetails);


        }
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
            /*
             * If the screen requires burn-in protection, views must be shifted around periodically
             * in ambient mode. To ensure that content isn't shifted off the screen, avoid placing
             * content within 10 pixels of the edge of the screen.
             *
             * Since we're potentially applying negative padding, we have ensured
             * that the containing view is sufficiently padded (see res/layout/activity_main.xml).
             *
             * Activities should also avoid solid white areas to prevent pixel burn-in. Both of
             * these requirements only apply in ambient mode, and only when this property is set
             * to true.

            if (mDoBurnInProtection) {
                int x = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                int y = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                if (mSwipeDismissFrameLayout != null) mSwipeDismissFrameLayout.setPadding(x, y, 0, 0);  //TODO: investigate this!
            }
              */
        }
        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            android.util.Log.d(TAG, "onExitAmbient()");
            if (mCustomConfirmDialog != null) mCustomConfirmDialog.onExitAmbientInFragment();

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        Context context = this.getApplicationContext();
        if (requestCode == PERMISSION_REQUEST_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                String sMsg = getResources().getString(R.string.location_permission_granted);
                Toast.makeText(context, sMsg, Toast.LENGTH_SHORT).show();
                UserPreferences.setConfirmUseLocation(context, true);
                if (bRequestAll)
                    permissions_Ok = (ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED);
                else
                    permissions_Ok = true;
                if (!permissions_Ok) {
                    permissionsInProgress = true;
                    requestSensorsPermission();
                }else {
                    permissionsInProgress = false;
                    Intent result = new Intent();
                    result.putExtra("permission_result", 1);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }

            } else {
                // Permission request was denied.
                mCustomConfirmDialog = CustomConfirmDialog.newInstance(1,getString(R.string.location_permission_denied), PermissionsActivity.this);
                mCustomConfirmDialog.setSingleButton(true);
                mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
            }
        }
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                String sMsg = getResources().getString(R.string.sensors_permission_granted);
                Toast.makeText(context, sMsg, Toast.LENGTH_SHORT).show();
                permissionsInProgress = false;
                permissions_Ok = true;
                String sLabel = getString(R.string.label_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_STEP_COUNT_CUMULATIVE.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_LOCATION_SAMPLE.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_ACTIVITY_SAMPLES.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                sLabel = getString(R.string.label_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
                UserPreferences.setPrefByLabel(context, sLabel, true);
                UserPreferences.setConfirmUseSensors(context, true);
                Intent result = new Intent();
                result.putExtra("permission_result", 1);
                setResult(Activity.RESULT_OK, result);
                finish();
            } else {
                permissionsInProgress = false;
                permissions_Ok = false;
                // Permission request was denied.
                mCustomConfirmDialog = CustomConfirmDialog.newInstance(1,getString(R.string.sensors_permission_denied), PermissionsActivity.this);
                mCustomConfirmDialog.setSingleButton(true);
                mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
            }

        }
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                String sMsg = getResources().getString(R.string.recording_permission_granted);
                Toast.makeText(context, sMsg, Toast.LENGTH_SHORT).show();
                UserPreferences.setConfirmUseRecord(context, true);
                permissionsInProgress = false;
                permissions_Ok = true;
                Intent result = new Intent();
                result.putExtra("permission_result", 1);
                setResult(Activity.RESULT_OK, result);
                finish();
            } else {
                permissionsInProgress = false;
                permissions_Ok = false;
                // Permission request was denied.
                mCustomConfirmDialog = CustomConfirmDialog.newInstance(1, getString(R.string.recording_permission_denied), PermissionsActivity.this);
                mCustomConfirmDialog.setSingleButton(true);
                mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        mCustomConfirmDialog.dismiss();
        mCustomConfirmDialog = null;
        if (question == 1) {
                permissionsInProgress = false;
                permissions_Ok = false;
                UserPreferences.setAppSetupCompleted(PermissionsActivity.this, false);
                Toast.makeText(this, getString(R.string.must_quit), Toast.LENGTH_LONG).show();
                Intent result = new Intent();
                result.putExtra("permission_result", 0);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        if (question == 2){
            switch (button) {
                case 1:
                    if (mTextView != null) mTextView.setText(getString(R.string.sensors_access_required));
                    // Request the permission
                    ActivityCompat.requestPermissions(PermissionsActivity.this,
                            new String[]{Manifest.permission.BODY_SENSORS},
                            PERMISSION_REQUEST_BODY_SENSORS);
                    break;
                case -1:
                    Toast.makeText(this, getString(R.string.must_quit), Toast.LENGTH_LONG).show();
                    Intent result = new Intent();
                    result.putExtra("permission_result", 0);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                    break;
            }
        }
        if (question == 3){
            switch (button) {
                case 1:
                    // Request the permission
                    ActivityCompat.requestPermissions(PermissionsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                    break;
                case -1:
                    Toast.makeText(this, getString(R.string.must_quit), Toast.LENGTH_LONG).show();
                    Intent result = new Intent();
                    result.putExtra("permission_result", 0);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                    break;
            }
        }
        if (question == 4){
            switch (button) {
                case 1:
                    // Request the permission
                    ActivityCompat.requestPermissions(PermissionsActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_RECORD_AUDIO);
                    break;
                case -1:
                    Toast.makeText(this, getString(R.string.no_audio_input), Toast.LENGTH_LONG).show();
                    Intent result = new Intent();
                    result.putExtra("permission_result", 0);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                    break;
            }
        }
    }



    /**
     * Requests the {@link android.Manifest.permission#BODY_SENSORS} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a CustomConfirmDialog that includes additional information.
     */

    private void requestSensorsPermission() {
        // Permission has not been granted and must be requested.
        Log.i(TAG, "requestSensorsPermission");
        if (mTextView != null) mTextView.setText(R.string.sensors_access_required);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERMISSION_REQUEST_BODY_SENSORS);
        }
/*        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.BODY_SENSORS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a AlertDialog with cda button to request the missing permission.
            // Permission grant is required
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(2,getString(R.string.sensors_access_required), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");

        } else {
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(2,getString(R.string.sensors_body_unavailable), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        }*/
    }
    /**
     * Requests the {@link android.Manifest.permission#ACCESS_FINE_LOCATION} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a AlertDialog that includes additional information.
     */
    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        Log.i(TAG, "requestLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
/*        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a CustomConfirmDialog with cda button to request the missing permission.
            // Permission grant is required
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(3,getString(R.string.location_access_required), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        } else {
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(3,getString(R.string.sensors_location_unavailable), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        }*/
    }

    /**
     * Requests the {@link android.Manifest.permission#RECORD_AUDIO} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a AlertDialog that includes additional information.
     */
    private void requestMicroPhonePermission() {
        // Permission has not been granted and must be requested.
        Log.i(TAG, "requestSensorsPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
/*        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a CustomConfirmDialog with cda button to request the missing permission.
            // Permission grant is required
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(4,getString(R.string.microphone_access_required), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        } else {
            mCustomConfirmDialog = CustomConfirmDialog.newInstance(4,getString(R.string.microphone_access_required), PermissionsActivity.this);
            mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        }*/
    }

    
}
