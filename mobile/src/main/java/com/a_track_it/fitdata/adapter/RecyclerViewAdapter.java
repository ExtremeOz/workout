package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chris Black
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<WorkoutViewHolder> implements View.OnClickListener {

    public static final String TAG = "RecyclerViewAdapter";
    private List<Workout> filteredItems;
    private List<Workout> items;
    private OnItemClickListener onItemClickListener;
    private String timeDesc = "Today";
    private boolean filtering = false;
    private ReferencesTools mRefTools;
    public RecyclerViewAdapter(List<Workout> items, Context context, final String time) {
        this.items = items;
        this.filteredItems = new ArrayList<>();
        this.context = context;
        this.timeDesc = time;
        this.mRefTools = ReferencesTools.getInstance();
        this.mRefTools.init(context);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
        Log.i(TAG, "1. New item size: " +  newItems.size() + " Old item size: " + items.size() + " Last position: " + lastPosition);
        if(newItems.size() > items.size()) {
            lastPosition = items.size() - 1;
        }
        /*if (newItems.size() < items.size()) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();

        } else */
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
            Log.i(TAG, "2. New item size: " +  newItems.size() + " Old item size: " + items.size() + " Last position: " + lastPosition);
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
        } else {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        v.setOnClickListener(this);
        return new WorkoutViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        Workout item;
        if(filtering) {
            item = filteredItems.get(position);
        } else {
            item = items.get(position);
        }

        if(item.activityID == Constants.WORKOUT_TYPE_TIME) {
            holder.text.setText(timeDesc);
        } else {
            holder.text.setText(item.activityName);
        }
      //  sIconName = mRefTools.getFitnessActivityIconById(item.regionID);
        int resid = mRefTools.getFitnessActivityIconResById(item.activityID);
        holder.image.setImageResource(resid);
        holder.image.setColorFilter(this.context.getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        holder.itemView.setTag(item);
        if (item == null) Log.e(TAG, "workout item is null");
        // This is just not working as expected. Switching to use hard coded color.
        // TODO: Colorize image to match pre-defined color. Allowing user to select the color at some point.
        int vibrant = ContextCompat.getColor(context, mRefTools.getFitnessActivityColorById(item.activityID));
        /*
        Bitmap bitmap = ((BitmapDrawable)holder.image.getDrawable()).getBitmap();
        Palette palette = Palette.from(bitmap).generate();
        Palette.Swatch swatch = palette.getLightVibrantSwatch();

        int vibrant = 0xFF110000;
        if (swatch != null) {
            vibrant = swatch.getRgb();//palette.getVibrantColor(0xFF110000);
        }
        if(vibrant == 0xFF110000) {
            swatch = palette.getVibrantSwatch();
            //vibrant = palette.getLightMutedColor(0x000000);
            if (swatch != null) {
                vibrant = swatch.getRgb();//palette.getVibrantColor(0xFF110000);
            }
        }
        */

        if(item.activityID == Constants.WORKOUT_TYPE_TIME) {
            final String label = context.getText(R.string.active_label) + " " + WorkoutReport.getDurationBreakdown(item.duration);
            holder.detail.setText(label);
        }else if(item.activityID == Constants.WORKOUT_TYPE_STEPCOUNT) {
            final String label = item.stepCount + " " + context.getText(R.string.step_label);
            holder.detail.setText(label);
        } else {
            holder.detail.setText(WorkoutReport.getDurationBreakdown(item.duration));
        }

        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
        holder.container.setBackgroundColor(vibrant);
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
            if (w == null) Log.e(TAG, "workout w is null on click");
            onItemClickListener.onItemClick(v, w);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Workout viewModel);
    }
}