package com.a_track_it.fitdata.service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.a_track_it.fitdata.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.zzd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.a_track_it.fitdata.common.Constants.COUNT_PATH;

public class DataLayerListenerService extends WearableListenerService {
    private Map<String, Node> mapNodes = new HashMap<>();
    private static final String TAG = "DataLayerListenerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    public DataLayerListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();

            if (COUNT_PATH.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                // Instantiates clients without member variables, as clients are inexpensive to
                // create. (They are cached and shared between GoogleApi instances.)
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(this)
                                .sendMessage(nodeId, DATA_ITEM_RECEIVED_PATH, payload);

                sendMessageTask.addOnCompleteListener(
                        new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(Task<Integer> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Message sent successfully");
                                } else {
                                    Log.d(TAG, "Message failed.");
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);
        if (!mapNodes.containsKey(node.getId()))
            mapNodes.put(node.getId(),node);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
        if (mapNodes.containsKey(node.getId()))
            mapNodes.remove(node);
    }

    @Override
    public void onConnectedNodes(List<Node> list) {
        super.onConnectedNodes(list);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        super.onCapabilityChanged(capabilityInfo);
    }

    @Override
    public void onNotificationReceived(zzd zzd) {
        super.onNotificationReceived(zzd);
    }
}
