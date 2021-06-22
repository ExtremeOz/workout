package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class SkuJobIntentService extends JobIntentService {
    private final static String LOG_TAG = SkuJobIntentService.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private BillingClient billingClient;
    private static final String KEY_BASE_PRODUCT = "base_product";
    private static final String KEY_FIREBASE_PRODUCT = "firebase_product";
    private static final String KEY_PHONE_PRODUCT = "phone_product";
    private PurchasesUpdatedListener mPurchaseUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

        }
    };
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 4;

    public SkuJobIntentService() { super(); }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SkuJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Bundle resultBundle = new Bundle();
        List<String> skuList = new ArrayList<>();
        if (intent.hasExtra(KEY_BASE_PRODUCT))
            skuList.add(intent.getStringExtra(KEY_BASE_PRODUCT));
        if (intent.hasExtra(KEY_FIREBASE_PRODUCT))
            skuList.add(intent.getStringExtra(KEY_FIREBASE_PRODUCT));
        if (intent.hasExtra(KEY_PHONE_PRODUCT))
            skuList.add(intent.getStringExtra(KEY_PHONE_PRODUCT));
        Context context = getApplicationContext();
        ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        appPrefs.setLastSKUCheck(System.currentTimeMillis());
        ResultReceiver resultReceiver = intent.getParcelableExtra(Constants.KEY_FIT_REC);

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                String sMsg;
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    sMsg = "billing response OK";
                    Log.d(LOG_TAG, sMsg);
                   // doSKUPLookupJob(skuList);
                    resultBundle.putString(LOG_TAG,sMsg);
                    resultReceiver.send(200, resultBundle);
                } else {
                    sMsg = "onBillingSetupFinished() error code: " + billingResult.getResponseCode();
                    Log.e(LOG_TAG, sMsg);
                    resultBundle.putString(LOG_TAG,sMsg);
                    resultReceiver.send(505, resultBundle);
                }
                showToast(sMsg);
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e(LOG_TAG, "onBillingServiceDisconnected");
                showToast("onBillingServiceDisconnected");

            }
        });
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        billingClient = BillingClient.newBuilder(getApplicationContext())
                .enablePendingPurchases()
             //   .setChildDirected(BillingClient.ChildDirected.CHILD_DIRECTED)
             //   .setUnderAgeOfConsent(BillingClient.UnderAgeOfConsent.UNDER_AGE_OF_CONSENT)
                .setListener(mPurchaseUpdatedListener).build();

    }

    @Override
    public void onDestroy() {
        if ((billingClient != null) && billingClient.isReady()) billingClient.endConnection();
        super.onDestroy();
    }

    private void doSKUPLookupJob(List<String> skuList){
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                if ("base_product".equals(sku)) {
                                    //  premiumUpgradePrice = price;
                                } else if ("firebase_functions".equals(sku)) {
                                    //  gasPrice = price;
                                }
                            }
                        }
                    }
                });
    }
    // Helper for showing tests
    void showToast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SkuJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
