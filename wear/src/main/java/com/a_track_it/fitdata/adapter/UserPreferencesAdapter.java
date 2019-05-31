package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.model.UserPreferences;

import java.util.ArrayList;

public class UserPreferencesAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<ATrackItListViewHolder> {
    private Context mContext;
    private ReferencesTools mRefTools;
    public static final String TAG = "UserPreferencesAdapter";

    public class UserPreferenceItem {
        public UserPreferenceItem() {
            this._id = 0L;
            this.label = "";
            this.name = "";
            this.value = false;
        }

        public long _id;
        public String label;
        public String name;
        public boolean value;
    }

    private ArrayList<UserPreferenceItem> preferenceItems = new ArrayList<>();

    private UserPreferencesAdapter.OnItemClickListener onItemClickListener;

    public UserPreferencesAdapter(Context context) {
        int iTemp;
        this.mContext = context;
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(context);
        UserPreferenceItem item = new UserPreferenceItem();
        item._id = 1;
        item.label = "ConfirmStartSession";
        item.name = context.getString(R.string.user_pref_1);
        item.value = UserPreferences.getConfirmStartSession(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 2;
        item.label = "ConfirmEndSession";
        item.name = context.getString(R.string.user_pref_2);
        item.value = UserPreferences.getConfirmEndSession(context);
        preferenceItems.add(item);


        item = new UserPreferenceItem();
        item._id = 3;
        item.label = "ConfirmDismissSession";
        item.name = context.getString(R.string.user_pref_3);
        item.value = UserPreferences.getConfirmDismissSession(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 4;
        item.label = "ConfirmUseSensors";
        item.name = context.getString(R.string.user_pref_4);
        item.value = UserPreferences.getConfirmUseSensors(context);
        preferenceItems.add(item);


        item = new UserPreferenceItem();
        item._id = 5;
        item.label = "setFeedbackMute";
        item.name = context.getString(R.string.user_pref_5);
        item.value = UserPreferences.getFeedbackMute(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 6;
        item.label = "useKG";
        item.name = context.getString(R.string.user_pref_6);
        item.value = UserPreferences.getUseKG(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 7;
        item.label = "timedRest";
        item.name = context.getString(R.string.user_pref_7);
        item.value = UserPreferences.getTimedRest(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 8;
        item.label = "workOffline";
        item.name = context.getString(R.string.confirm_work_offline);
        item.value = UserPreferences.getWorkOffline(context);
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 9;
        item.label = "showShowDataPoints";
        item.name = "Show data points";
        item.value = UserPreferences.getShowDataPoints(context);
        preferenceItems.add(item);
/*        item = new UserPreferenceItem();
        item._id = 8;
        item.label = "activityID1";
        iTemp = UserPreferences.getActivityID1(context);
        if (iTemp > 0) {
            item.name = mRefTools.getFitnessActivityTextById(iTemp);
        }else{
            item.name = context.getString(R.string.user_pref_8);
        }
        item.value = (int) iTemp;
        preferenceItems.add(item);

        item = new UserPreferenceItem();
        item._id = 9;
        item.label = "activityID2";
        iTemp = UserPreferences.getActivityID2(context);
        if (iTemp > 0) {
            item.name = mRefTools.getFitnessActivityTextById(iTemp);
        }else{
            item.name = context.getString(R.string.user_pref_9);
        }
        item.value = (int) iTemp;
        preferenceItems.add(item);
        item = new UserPreferenceItem();
        item._id = 10;
        item.label = "activityID3";
        iTemp = UserPreferences.getActivityID2(context);
        if (iTemp > 0) {
            item.name = mRefTools.getFitnessActivityTextById(iTemp);
        }else{
            item.name = context.getString(R.string.user_pref_10);
        }
        item.value = (int) iTemp;
        preferenceItems.add(item);
        item = new UserPreferenceItem();
        item._id = 11;
        item.label = "activityID4";
        iTemp = UserPreferences.getActivityID2(context);
        if (iTemp > 0) {
            item.name = mRefTools.getFitnessActivityTextById(iTemp);
        }else{
            item.name = context.getString(R.string.user_pref_11);
        }
        item.value = (int) iTemp;
        preferenceItems.add(item);*/
    }

    public void setOnItemClickListener(UserPreferencesAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public ATrackItListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_item, viewGroup, false);
        return new ATrackItListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ATrackItListViewHolder viewHolder, final int i) {
        final UserPreferenceItem item = preferenceItems.get(i);
        viewHolder.text.setText(item.name);
        viewHolder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    UserPreferenceItem item = preferenceItems.get(i);
                    item.value = ! item.value;
                    preferenceItems.set(i, item);

                    onItemClickListener.onItemClick(view, item);
                    notifyDataSetChanged();
                }
            }
        });

        int resId =  item.value ? mContext.getResources().getIdentifier("ic_outline_check_white", "drawable", mContext.getPackageName()) : mContext.getResources().getIdentifier("ic_outline_cancel_white", "drawable", mContext.getPackageName());
        viewHolder.image.setImageResource(resId);
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    if (i < 8) {
                        UserPreferenceItem item = preferenceItems.get(i);
                        item.value = !item.value;
                        preferenceItems.set(i, item);
                        Log.d(TAG, "text onclick " + item.label);
                        onItemClickListener.onItemClick(view, item);
                    }
                    if (i == 8){
                        UserPreferenceItem item = preferenceItems.get(i);
                        item.value = !item.value;
                        preferenceItems.set(i, item);
                        Log.d(TAG, "text onclick " + item.label);
                        onItemClickListener.onItemClick(view, item);
                    }
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return preferenceItems.get(position)._id;
    }

    @Override
    public int getItemCount() {
        return preferenceItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UserPreferenceItem viewModel);
    }
}
