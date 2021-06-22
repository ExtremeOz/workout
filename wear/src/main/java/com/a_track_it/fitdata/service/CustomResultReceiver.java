package com.a_track_it.fitdata.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;

public class CustomResultReceiver  extends ResultReceiver {
    private AppReceiver appReceiver;

    public interface AppReceiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public CustomResultReceiver(Handler handler, AppReceiver appReceiver) {
        super(handler);
        this.appReceiver = appReceiver;
    }

    @Override
    public void send(int resultCode, Bundle resultData) {
        super.send(resultCode, resultData);
        appReceiver.onReceiveResult(resultCode, resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (appReceiver != null) {
            /*
             * Step 2: We pass the resulting data from the service to the activity
             * using the AppReceiver interface
             */
            appReceiver.onReceiveResult(resultCode, resultData);
        }
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
