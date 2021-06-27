package com.a_track_it.workout.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Workout;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chris Black
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.WorkoutViewHolder> implements View.OnClickListener {
    public static final String TAG = "RecyclerViewAdapter";
    private List<Workout> filteredItems;
    private List<Workout> items;
    private OnItemClickListener onItemClickListener;
    private String timeDesc = "Today";
    private boolean filtering = false;
    private ReferencesTools mRefTools;
    private boolean useGrid = false;
    private Drawable icon1;
    private Drawable iconGMS;
    private Drawable iconFIT;

    public RecyclerViewAdapter(List<Workout> items, Context context, final String time, boolean useGridView) {
        this.items = items;
        this.filteredItems = new ArrayList<>();
        this.context = context;
        this.timeDesc = time;
        this.mRefTools = ReferencesTools.getInstance();
        this.mRefTools.init(context);
        this.useGrid = useGridView;
        try {
            icon1 = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_home);
            //context.getPackageManager().getApplicationIcon(Constants.ATRACKIT_ATRACKIT_CLASS);
            iconGMS = context.getPackageManager().getApplicationIcon(Constants.ATRACKIT_PLAY_CLASS);
            iconFIT = AppCompatResources.getDrawable(context, R.drawable.ic_google_fit_logo);
        }catch (Exception e){
            Log.e(RecyclerViewAdapter.class.getSimpleName(), e.getMessage());
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void clearItems(){
        items.clear();
        notifyDataSetChanged();
    }
    /**
     * Do some fun animation for remove and add. If the data is fresh or some how smaller,
     * than just reset the data set normally.
     *
     * @param newItems
     * @param time
     */
    public void setItems(final List<Workout> newItems, final String time) {
        timeDesc = time;
    //    Log.i(TAG, "1. New item size: " +  newItems.size() + " Old item size: " + items.size() + " Last position: " + lastPosition);
        if (newItems.size() > items.size()) {
            lastPosition = items.size() - 1;
        }
        if (!useGrid) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();

        } else
        if (newItems.size() > 0) {
            if (items.size() == 0) {
                items.add(newItems.get(0));
                notifyItemInserted(0);
            } else {
                items.set(0, newItems.get(0));
                notifyItemChanged(0);
            }
            if(items.size() > 0) {
                int itemSize = items.size() - 1;
                for (int i = itemSize; i > 0; i--) {
                    if (i >= newItems.size()) {
                        items.remove(i);
                        notifyItemRemoved(i);
                    }
                }
            }
        //    Log.i(TAG, "2. New item size: " +  newItems.size() + " Old item size: " + items.size() + " Last position: " + lastPosition);
            if(newItems.size() > 0) {
                int itemSize = items.size();
                for (int i = 1; i < newItems.size(); i++) {
                    if (i >= itemSize) {
                        items.add(newItems.get(i));
                        notifyItemInserted(i);
                    } else {
                        items.set(i, newItems.get(i));
                        notifyItemChanged(i);
                    }
                }
            }
        }
        else {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }
    }
    public void setUseGrid(final boolean bUse){
        this.useGrid = bUse;
    }
    public boolean getUseGrid(){
        return this.useGrid;
    }
    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());

        filteredItems.clear();
        if (charText.length() == 0) {
            filtering = false;
        } else {
            for (Workout wp : items) {
                if (wp.activityName.length() == 0) wp.activityName = this.mRefTools.getFitnessActivityTextById(wp.activityID);
                if (wp.activityName.toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    filteredItems.add(wp);
                }
            }
            filtering = true;
        }
        notifyDataSetChanged();
    }

    public void setNeedsAnimate() {
        lastPosition = 0;
    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate((useGrid) ? R.layout.grid_card : R.layout.list_card, parent, false);
        v.setOnClickListener(this);
        return new WorkoutViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        Workout item = null;
        if(filtering && (filteredItems.size() > position)) {
            item = filteredItems.get(position);
        } else {
            if (position < items.size()) item = items.get(position);
        }
        if (item == null) return;
        int resid = 0;
        if (item.activityID == Constants.WORKOUT_TYPE_TIME) {
            holder.text.setText(timeDesc);
        } else {
            if (item.activityName.length() > 0)
                holder.text.setText(item.activityName);
            else {
                if (useGrid)
                    holder.text.setText(Utilities.getSummaryActivityName(mRefTools.getMyContext(), item.activityID));
                else
                    holder.text.setText(mRefTools.getFitnessActivityTextById(item.activityID));
            }
        }
        if ((mRefTools.getMyContext() == null) && (this.context != null)) mRefTools.init(this.context);
        // summary or not !
        if (useGrid){
            if (Utilities.isDetectedActivity(item.activityID)) {
                resid = mRefTools.getFitnessActivityIconResById(item.activityID);
            }else{
                if (item.activityID == Constants.WORKOUT_TYPE_TIME) {
                    if (item.duration > 0)
                        resid = Utilities.getSummaryIconID(context, item.activityID);
                    else
                        resid = R.drawable.ic_standing_up_man_white;
                }else
                    resid = Utilities.getSummaryIconID(context, item.activityID);

            }
        }else {
            resid = mRefTools.getFitnessActivityIconResById(item.activityID);
        }
        if (resid > 0){
            int iconColor = context.getColor(R.color.secondaryDarkColor);
            Drawable d = AppCompatResources.getDrawable(context, resid);
            Utilities.setColorFilter(d,iconColor);
            holder.image.setImageDrawable(d);
        }
        try{
            if (useGrid){
                if ((item.activityID >= DetectedActivity.IN_VEHICLE) && (item.activityID <= DetectedActivity.RUNNING)) holder.package_imageView.setImageDrawable(iconGMS);
                else holder.package_imageView.setVisibility(ImageView.GONE);
            }else
            if ((item.packageName != null) && item.packageName.length() > 0 && (item.activityID != Constants.WORKOUT_TYPE_TIME)) {
                 if (item.packageName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)) holder.package_imageView.setImageDrawable(icon1);
                else if (item.packageName.equals(Constants.ATRACKIT_PLAY_CLASS)) holder.package_imageView.setImageDrawable(iconGMS);
                else if (item.packageName.equals(Constants.ATRACKIT_GFIT_CLASS)) holder.package_imageView.setImageDrawable(iconFIT);
                else {
                    Drawable icon = this.context.getPackageManager().getApplicationIcon(item.packageName);
                    holder.package_imageView.setImageDrawable(icon);
                }
            }else
                holder.package_imageView.setVisibility(ImageView.GONE);
        }catch (PackageManager.NameNotFoundException e) {
            Log.e(RecyclerViewAdapter.class.getSimpleName(), "error loading icon " +  item.packageName);
            e.printStackTrace();
            holder.package_imageView.setVisibility(ImageView.GONE);
        }
        holder.itemView.setTag(item);
        String label = Constants.ATRACKIT_EMPTY;
        int vibrant;
        if (!useGrid)
            vibrant = ContextCompat.getColor(context, mRefTools.getFitnessActivityColorById(item.activityID));
        else
            vibrant = ContextCompat.getColor(context, mRefTools.getSummaryActivityColor(item.activityID));
        if(item.activityID == Constants.WORKOUT_TYPE_TIME) {
            if (item.duration > 0) {
                label = context.getText(R.string.active_label) + " " + Utilities.getDurationBreakdown(item.duration);
                if (item.stepCount > 0)
                    label += Constants.LINE_DELIMITER + item.stepCount + " " + context.getText(R.string.step_label);
                if (item.scoreTotal > 0)
                    label += Constants.LINE_DELIMITER + item.scoreTotal + " " + context.getText(R.string.label_workout_history);
            }else {
                label = "No activity recorded";
                vibrant = ContextCompat.getColor(context,R.color.steps);
            }
            holder.detail.setText(label);
        }else if(item.activityID == Constants.WORKOUT_TYPE_STEPCOUNT) {
            label = item.stepCount + " " + context.getText(R.string.step_label);
            holder.detail.setText(label);
        } else {
            if (Utilities.isDetectedActivity(item.activityID))
                holder.detail.setText(Utilities.getDurationBreakdown(item.duration));
            else {
                if (useGrid) {
                    if (item.activityID == Constants.SELECTION_ACTIVITY_GYM) {
                        label = mRefTools.workoutGymSummaryText(item);
                        holder.detail.setText(label);
                    }else {
                        String sText = Constants.ATRACKIT_EMPTY;
                        if (item.scoreTotal > 0)
                            sText = item.scoreTotal + " sessions";
                        if (item.duration > 0)
                            sText += "\n for " + Utilities.getDurationBreakdown(item.duration);

                        if (item.stepCount > 0)
                            sText += "\n " + item.stepCount + " steps";

                        holder.detail.setText(sText);
                    }
                }else{
                    holder.detail.setText(mRefTools.workoutListText(item));
                }
            }
        }

        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.2f));
        //((MaterialCardView)holder.container.getParent()).setBackgroundColor(vibrant);
        setAnimation(holder.container, position);
    }

    private Context context;
    private int lastPosition = 0;

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition && position > 0)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animation.setInterpolator(new DecelerateInterpolator());
            if(position < 6) {
                animation.setStartOffset(150 * (position));
            } else if (position % 2 != 0) {
                animation.setStartOffset(150);
            }
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        if (filtering) {
            return filteredItems.size();
        } else {
            return items.size();
        }
    }

    @Override
    public void onClick(final View v) {
        if (onItemClickListener != null) {
            Workout w = (Workout)v.getTag();
            onItemClickListener.onItemClick(v, w);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Workout viewModel);
    }

    /**
     * Created by Chris Black
     */
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;
        public final ImageView package_imageView;
        public final TextView text;
        public final TextView detail;
        public final ConstraintLayout container;

        public WorkoutViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            detail = (TextView) itemView.findViewById(R.id.summary_text);
            package_imageView = (ImageView) itemView.findViewById(R.id.package_image);
            container = (ConstraintLayout) itemView.findViewById(R.id.container);
        }
    }
}