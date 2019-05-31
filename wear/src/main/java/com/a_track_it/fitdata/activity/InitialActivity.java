package com.a_track_it.fitdata.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.widget.SwipeDismissFrameLayout;

import com.a_track_it.fitdata.BuildConfig;
import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.model.UserPreferences;
import com.a_track_it.fitdata.service.AsyncSetupTask;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class InitialActivity extends androidx.fragment.app.FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        IInitialActivityCallback, CustomConfirmDialog.ICustomConfirmDialog {
    private final static String TAG = InitialActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 5003;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 5004;
    private static final int REQUEST_CHOOSE_ACCOUNT_CODE = 5001;
    private static final int RC_SIGN_IN = 5002;
    private static final int REQUEST_PERMISSIONS_ACTIVITY = 5005;

    private boolean mNetworkConnected = false;
    private boolean mInitialLoad = false;
    private boolean permissions_Ok = false;
    private boolean permissionsInProgress = false;
    private boolean authInProgress = false;
    private boolean connectedClient = false;
    private CustomConfirmDialog mCustomConfirmDialog;
    private ReferencesTools mRefTools;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleAccount;
    private GoogleApiClient mClient;
    private AsyncSetupTask myAsyncSetupTask;
    private TextView loadMessage;
   //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screen);
        SwipeDismissFrameLayout swipeDismissFrameLayout = findViewById(R.id.initial_swipe_layout);
        swipeDismissFrameLayout.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(SwipeDismissFrameLayout layout) {
                if (authInProgress) Log.i(TAG, "attempt to quit while auth in progress"); else quitApp();

            }
        });
        loadMessage = findViewById(R.id.load_message);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(this);
        mNetworkConnected = mRefTools.isNetworkConnected();
        mInitialLoad = (!Utilities.fileExist(this, Constants.BODYPART_FILENAME));
        // set default user settings immediately
        if (mInitialLoad){
            UserPreferences.setConfirmDismissSession(this, false);
            UserPreferences.setConfirmUseSensors(this, false);
            UserPreferences.setConfirmUseRecord(this, false);
            UserPreferences.setConfirmUseLocation(this, false);
            UserPreferences.setConfirmStartSession(this, false);
            UserPreferences.setConfirmDuration(this, 3000);
            UserPreferences.setWeightsRestDuration(this, 90);
            UserPreferences.setArcheryRestDuration(this, 270);
            UserPreferences.setConfirmEndSession(this, true);
            UserPreferences.setAppSetupCompleted(this, false);
            UserPreferences.setUseKG(this, true);
            UserPreferences.setWorkOffline(this, false);
        }else{
            // don't if we have done it all before !
            if (UserPreferences.getAppSetupCompleted(this)) {
                startMainActivity();
                return;
            }
        }
        if (mInitialLoad && mNetworkConnected) {
            if (!permissions_Ok && !permissionsInProgress) {
                if (checkPlayServices()) {
                    if (!permissionsInProgress && mInitialLoad) {
                        permissions_Ok = false;
                        permissionsInProgress = true;
                        Intent permissionsIntent = new Intent(this, PermissionsActivity.class);
                        InitialActivity.this.startActivityForResult(permissionsIntent,REQUEST_PERMISSIONS_ACTIVITY);
                        return;
                    }else
                        checkUserAuth();
                } else {
                    // No PLAY SERVICES = FAIL
                    mCustomConfirmDialog = CustomConfirmDialog.newInstance(0,getString(R.string.no_google_play), InitialActivity.this);
                    mCustomConfirmDialog.setSingleButton(true);
                    mCustomConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
                    Log.w(TAG, "Error: No Google Play Services available");
                    quitApp();
                }
            }else {
                checkUserAuth();
            }
        }
     }

    @Override
    protected void onStart() {
        super.onStart();
        mNetworkConnected = mRefTools.isNetworkConnected();
        if (!mNetworkConnected){
            Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
            loadMessage.setText(getString(R.string.no_network_connection));
            Log.w(TAG, "Network not connected");
        }
        if (!permissionsInProgress && !authInProgress) checkUserAuth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (connectedClient && !permissionsInProgress && !authInProgress) disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!permissionsInProgress && !authInProgress) {
            checkUserAuth();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == android.app.Activity.RESULT_OK) checkUserAuth();
                break;
            case REQUEST_PERMISSIONS_ACTIVITY:
                if (resultCode == Activity.RESULT_OK){
                    int outcome = data.getIntExtra("permission_result", -2);
                    permissionsInProgress = false;
                    if (outcome != 1){
                        Log.d(TAG, "No permissions outcome from PermissionsActivity!");
                        UserPreferences.setAppSetupCompleted(getApplicationContext(), false);
                        permissions_Ok = false;
                        quitApp();
                    }else {
                        permissions_Ok = true;
                        checkUserAuth();
                    }
                }
                break;
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
            case REQUEST_CHOOSE_ACCOUNT_CODE:
                String sUserAccountName = (resultCode == RESULT_OK)? data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) : "";
                Log.d(TAG, "Account Picked " + sUserAccountName);
                checkUserAuth();
                break;
            case REQUEST_OAUTH_REQUEST_CODE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result != null) {
                        mGoogleAccount = result.getSignInAccount();
                        Log.i(TAG, "Fitness permissions have been granted");
                        if (!GoogleSignIn.hasPermissions(mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE) ,new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))) {
                            authInProgress = true;
                            Log.i(TAG, "Requesting permissions for extra permissions " + mGoogleAccount.getDisplayName());
                            GoogleSignIn.requestPermissions(InitialActivity.this, REQUEST_OAUTH_REQUEST_CODE, mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE), new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
                            return;
                        }else
                            checkUserAuth();
                    }else {
                        Log.e(TAG, "Null result from REQUEST_OAUTH - drive api scope");
                        quitApp();
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setAction(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // app settings
                    Uri uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    quitApp();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /** AsyncSetupTask callback  **/
    @Override
    public void onSetupComplete() {
        Log.i(TAG, "SETUP FINISHED!");
        if (mNetworkConnected && connectedClient) {
            Log.i(TAG, "Setup finished with sign-in authInProgress " + Boolean.toString(authInProgress) );
        }
        UserPreferences.setAppSetupCompleted(this, true);
        UserPreferences.setStepsSampleRate(this, 30);
        UserPreferences.setBPMSampleRate(this, 20);
        UserPreferences.setOthersSampleRate(this, 30);
        startMainActivity();
    }

    @Override
    public void onCustomConfirmButtonClicked(int question, int button) {
        mCustomConfirmDialog.dismiss();
        mCustomConfirmDialog = null;
        if (question == 0) {
            switch (button) {
                case 1:
                    permissionsInProgress = false;
                    permissions_Ok = false;
                    UserPreferences.setConfirmUseSensors(InitialActivity.this, false);
                    UserPreferences.setAppSetupCompleted(InitialActivity.this, false);
                    Toast.makeText(this, getString(R.string.must_quit), Toast.LENGTH_LONG).show();
                    quitApp();
                    break;
                case -1:
                    break;
            }

        }
        if (question == 1) {
            switch (button) {
                case 1:
                    permissionsInProgress = false;
                    permissions_Ok = false;
                    UserPreferences.setConfirmUseSensors(InitialActivity.this, false);
                    UserPreferences.setAppSetupCompleted(InitialActivity.this, false);
                    Toast.makeText(this, getString(R.string.must_quit), Toast.LENGTH_LONG).show();
                    quitApp();
                    break;
                case -1:
                    break;
            }
        }
    }

    /** GoogleApiClient callbacks  **/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connectedClient = true;
        if (!Utilities.fileExist(this, Constants.BODYPART_FILENAME)) {
            // immediately start the async setup task
            myAsyncSetupTask = new AsyncSetupTask(InitialActivity.this, getApplicationContext());
            myAsyncSetupTask.execute();

        }else {
            Log.i(TAG, "starting main after connection finished");
            startMainActivity();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedClient = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connectedClient = false;
    }

    private void startMainActivity(){
        Intent myMainIntent = new Intent(getApplicationContext(), MainActivity.class);
        myMainIntent.putExtra("MSG", getString(R.string.action_setup_complete));
        startActivity(myMainIntent);
        Log.i(TAG, "started main finishing InitialActivity");
        finish();
    }

    public void connect() {
        if (mClient == null || !connectedClient) {
            connectedClient = false;
            mClient = buildFitnessClient();
            mClient.connect();
            loadMessage.setText(R.string.connecting_google);
        } else if (!mClient.isConnecting() && !mClient.isConnected()) {
            Log.v(TAG, "Connecting client.");
            loadMessage.setText(R.string.connecting_google);
            mClient.reconnect();
        }
    }

    public void disconnect() {
        if (mClient != null && (mClient.isConnected() || mClient.isConnecting())) {
            Log.v(TAG, "Disconnecting client.");
            mClient.disconnect();
        }
        mClient = null;
        connectedClient = false;
    }

        /**
         * Check the device to make sure it has the Google Play Services APK. If
         * it doesn't, display a_track_it.com dialog that allows users to download the APK from
         * the Google Play Store or enable it in the device's system settings.
         */
        private boolean checkPlayServices() {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                } else {
                    Log.w(TAG, "This device is not supported - missing Google Play.");
                }
                return false;
            }
            return true;
        }

    private void checkUserAuth(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        if (mGoogleSignInClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(InitialActivity.this, gso);
        }
        if (!authInProgress) {
            mGoogleAccount =  GoogleSignIn.getLastSignedInAccount(this);
            if (mGoogleAccount != null) {
                String PersonName = mGoogleAccount.getDisplayName();
                String PersonId = mGoogleAccount.getId();
                Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                Log.i(TAG, "mGoogleAccount is " + PersonName );
                UserPreferences.setLastUserID(this, PersonId);
                UserPreferences.setLastUserPhotoUri(this, PersonPhotoUri.toString());
                UserPreferences.setLastUserName(this, PersonName);
                if (hasOAuthPermission()) {
                    if (!GoogleSignIn.hasPermissions(mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE) ,new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))) {
                        authInProgress = true;
                        Log.i(TAG, "Requesting permissions for extra permissions " + PersonName);
                        GoogleSignIn.requestPermissions(InitialActivity.this, REQUEST_OAUTH_REQUEST_CODE, mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE), new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
                        return;
                    }
                    if (!Utilities.fileExist(this, Constants.BODYPART_FILENAME)) {
                        UserPreferences.setShowDataPoints(this, true);
                        // immediately start the async setup task
                        myAsyncSetupTask = new AsyncSetupTask(InitialActivity.this, getApplicationContext());
                        myAsyncSetupTask.execute();

                    }
                } else {
                    requestOAuthPermission();  // don't have permission - request it - callback
                }
            } else {
                // never logged in or revoked previously
                authInProgress = true;
                signIn();
                /*Intent accountSelector = AccountPicker.newChooseAccountIntent(null, null,
                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false,
                        getString(R.string.select_account_for_access), null, null, null);
                startActivityForResult(accountSelector, REQUEST_CHOOSE_ACCOUNT_CODE);*/
            }
        }  // finish if not authInProgress already

    }
    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(mGoogleAccount, fitnessOptions);
    }

    /** Launches the Google SignIn activity to request OAuth permission for the user. */
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
                InitialActivity.this,
                REQUEST_OAUTH_REQUEST_CODE,
                mGoogleAccount,
                fitnessOptions);
    }



    /** Gets {@link FitnessOptions} in order to check or request OAuth permission for the user. */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_POWER_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
    }


    /**
     *  Build a_track_it.com {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a_track_it.com known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or
     *  having multiple accounts on the device and needing to specify which account to use, etc.
     */
    private  GoogleApiClient buildFitnessClient() {
        Log.i(TAG, "Creating the Google API Client with context: " + this.getClass().getName());
        // Create the Google API Client
        return new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Fitness.SENSORS_API)
                .setAccountName(mGoogleAccount.getEmail())
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loadMessage.setText(getString(R.string.signing_in_google));
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signIn]

    // [START signInSilent]
    private void signInSilent() {
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
        // There's no immediate result ready, displays some progress indicator and waits for the
        // async callback.

        task.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    authInProgress = false;
                    // There's immediate result available.
                    mGoogleAccount = task.getResult();
                    if (mGoogleAccount != null) connect();  else Log.e(TAG, "Silent Sign-in NULL"); // already setup - just connect
                }else {
                    Log.i(TAG, "silent sign-in NOT successful... signing in normally ");
                    signIn();
                }
            }
        });




    }
    // [END signInSilent]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            mGoogleAccount = completedTask.getResult(ApiException.class);
            if (mGoogleAccount != null) {
                String PersonName = mGoogleAccount.getDisplayName();
                String PersonId = mGoogleAccount.getId();
                Uri PersonPhotoUri = mGoogleAccount.getPhotoUrl();
                UserPreferences.setLastUserID(this, PersonId);
                UserPreferences.setLastUserName(this, PersonName);
                UserPreferences.setLastUserPhotoUri(this, PersonPhotoUri.toString());
                UserPreferences.setUserEmail(this, mGoogleAccount.getEmail());
                loadMessage.setText("Welcome " + PersonName);
                authInProgress = false;
                if (hasOAuthPermission()) {
                    if (!GoogleSignIn.hasPermissions(mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE), new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))) {
                        authInProgress = true;
                        Log.i(TAG, "Requesting permissions for extra permissions " + PersonName);
                        GoogleSignIn.requestPermissions(InitialActivity.this, REQUEST_OAUTH_REQUEST_CODE, mGoogleAccount, new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.FITNESS_LOCATION_READ_WRITE), new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
                        return;
                    }
                    if (!Utilities.fileExist(this, Constants.BODYPART_FILENAME)) {
                        // immediately start the async setup task
                        myAsyncSetupTask = new AsyncSetupTask(InitialActivity.this, getApplicationContext());
                        myAsyncSetupTask.execute();

                    }
                } else {
                    requestOAuthPermission();  // don't have permission - request it - callback
                }
            }else Log.e(TAG, "handleSignInResult NULL"); // already setup - just connect


            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            String sMsg = "signInResult:failed code=" + e.getStatusCode();
            Log.w(TAG, sMsg);
            Toast.makeText(getApplicationContext(),"Sign In Error " + sMsg, Toast.LENGTH_LONG).show();
            authInProgress = false;
            // updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START quitApp]
    private void quitApp() {
        if (connectedClient) disconnect();
        finish();
        System.exit(0);
    }
    // [END quitApp]

}