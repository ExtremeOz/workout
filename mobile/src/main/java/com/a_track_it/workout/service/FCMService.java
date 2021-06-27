package com.a_track_it.workout.service;

import android.content.Intent;
import android.util.Log;

import com.a_track_it.workout.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;

public class FCMService extends FirebaseMessagingService {
    private final static String LOG_TAG = FCMService.class.getSimpleName();
    public FCMService() {
    }
    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.setAction(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
        sendBroadcast(msgIntent);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String sMsg = remoteMessage.getData().toString();
            Log.d(LOG_TAG, "Message data payload: " + sMsg);
            broadcastToast(sMsg);

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
             //   scheduleJob();
            } else {
                // Handle message within 10 seconds
             //   handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String sBody = remoteMessage.getNotification().getBody();
            Log.d(LOG_TAG, "Message Notification Body: " + sBody);
            broadcastToast(sBody);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
     //  Context context = getApplicationContext();
      //  if (context != null) UserPreferences.setPrefStringByLabel(context, "idToken", s);
        String sTemp = getString(R.string.msg_token_fmt, s);
        Log.w(LOG_TAG, sTemp);
       // broadcastToast(s);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
