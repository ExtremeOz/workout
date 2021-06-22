package com.a_track_it.fitdata.common.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by Chris Black
 */

public class CacheResultReceiver extends ResultReceiver {

    public final static String TAG = "CacheResultReceiver";
    private Receiver mReceiver;

    public CacheResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            Receiver receiver = mReceiver;
            if (receiver != null) {
                receiver.onReceiveResult(resultCode, resultData);
            } else {
                mReceiver.onReceiveResult(resultCode, resultData);
                Log.e(TAG, "Weak listener is NULL: " + resultData.getString("ResultTag"));
            }
        }else
            Log.e(TAG, "mReceiver is null");
    }
    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
