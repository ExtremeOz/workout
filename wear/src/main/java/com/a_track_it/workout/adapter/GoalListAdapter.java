package com.a_track_it.workout.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.data_model.Configuration;
import com.google.android.gms.fitness.data.DataType;

import java.util.ArrayList;
import java.util.Iterator;

public class GoalListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Configuration> listConfigs = new ArrayList<>();
    private GoalListAdapter.OnItemClickListener onItemClickListener;
    private Context mContext;
    private Resources mResources;
    class GoalViewHolder extends RecyclerView.ViewHolder {
        private final ImageView dialImageView;
        private final TextView titleTextView;
        private final TextView percentTextView;
        private final TextView ofTextView;
        private final TextView currentTextView;
        private final TextView goalTextView;

        private GoalViewHolder(View itemView) {
            super(itemView);
            dialImageView = itemView.findViewById(R.id.imageDial);
            titleTextView = itemView.findViewById(R.id.goalTitle);
            percentTextView = itemView.findViewById(R.id.percentText);
            ofTextView = itemView.findViewById(R.id.OfValue);
            currentTextView = itemView.findViewById(R.id.currentValue);
            goalTextView = itemView.findViewById(R.id.goalValue);
        }
    }

    public GoalListAdapter(Context context){
        mContext = context;
        mResources = context.getResources();
    }
    public void AddGoal(Configuration goalConfig){
        Iterator<Configuration> iterator = listConfigs.iterator();
        boolean bFound = false;
        while (iterator.hasNext()){
            Configuration conf = iterator.next();
            if (conf.stringName.equals(goalConfig.stringName)){
                conf.longValue = goalConfig.longValue;
                conf.stringValue = goalConfig.stringValue;
                conf.userValue = goalConfig.userValue;
                goalConfig.stringValue1 = goalConfig.stringValue1;
                goalConfig.stringValue2 = goalConfig.stringValue2;
                bFound = true;
                break;
            }
        }
        if (!bFound)
            listConfigs.add(goalConfig);
    }
    public void setGoalCurrentValue(String sName, long currentValue){
        Iterator<Configuration> iterator = listConfigs.iterator();
        while (iterator.hasNext()){
            Configuration conf = iterator.next();
            if (conf.stringName.equals(sName)){
                conf.longValue = currentValue;
                break;
            }
        }
    }
    public void setOnItemClickListener(GoalListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_goal_item, parent, false);
        return new GoalViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Configuration goalConfig = listConfigs.get(position);
        String sTemp1 = goalConfig.stringValue;
        double goalValue = (sTemp1.length() > 0)? Double.parseDouble(sTemp1):0;
        double current = Double.parseDouble(Long.toString(goalConfig.longValue));
        double percentage = (goalValue > 0) ? (current / goalValue) * 100.0d : 0;
        String sTemp = Math.round(percentage) + "%";
        ((GoalViewHolder)holder).percentTextView.setText(sTemp);
        // empty holder!
        if (goalConfig.stringName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)){
            ((GoalViewHolder)holder).titleTextView.setText(mContext.getString(R.string.label_no_goals));
            ((GoalViewHolder)holder).goalTextView.setText(mContext.getString(R.string.label_no_goals_advice));
            ((GoalViewHolder)holder).goalTextView.setTextSize(12f);
            ((GoalViewHolder)holder).currentTextView.setVisibility(TextView.GONE);
            ((GoalViewHolder)holder).percentTextView.setVisibility(TextView.GONE);
            ((GoalViewHolder)holder).ofTextView.setVisibility(TextView.GONE);
            ImageView imageView = ((GoalViewHolder)holder).dialImageView;
            imageView.setVisibility(View.INVISIBLE);
            return;
        }else
            ((GoalViewHolder)holder).goalTextView.setTextSize(14f);
        if (goalConfig.stringName.equals(DataType.TYPE_MOVE_MINUTES.getName())){
            ((GoalViewHolder)holder).titleTextView.setText(mContext.getString(R.string.label_move_minutes));
            sTemp = Math.round(goalValue) + Constants.MOVE_MINS_TAIL;
            ((GoalViewHolder)holder).goalTextView.setText(sTemp);
            sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
            ((GoalViewHolder)holder).currentTextView.setText(sTemp);
        }
        if (goalConfig.stringName.equals(DataType.TYPE_HEART_POINTS.getName())){
            ((GoalViewHolder)holder).titleTextView.setText(mContext.getString(R.string.label_heart_points));
            sTemp = Math.round(goalValue) + Constants.HEART_PTS_TAIL;
            ((GoalViewHolder)holder).goalTextView.setText(sTemp);
            sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
            ((GoalViewHolder)holder).currentTextView.setText(sTemp);
        }
        if (goalConfig.stringName.equals(DataType.TYPE_STEP_COUNT_DELTA.getName())){
            ((GoalViewHolder)holder).titleTextView.setText(mContext.getString(R.string.label_steps_goal));
            sTemp = Math.round(goalValue) + Constants.ATRACKIT_SPACE;// + mContext.getString(R.string.label_steps);
            ((GoalViewHolder)holder).goalTextView.setText(sTemp);
            sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
            ((GoalViewHolder)holder).currentTextView.setText(sTemp);
        }
        AnimationDrawable animatedDrawable = (AnimationDrawable) AppCompatResources.getDrawable(mContext, com.a_track_it.workout.common.R.drawable.dial_animation);
        int iCounter = 10;
        while (iCounter <= percentage && (iCounter <= 100)){
            String sName = "ic_dial_" + iCounter;
            int resId = mResources.getIdentifier(sName, Constants.ATRACKIT_DRAWABLE,Constants.ATRACKIT_ATRACKIT_CLASS);
            if (resId > 0) {
                Drawable item = AppCompatResources.getDrawable(mContext, resId);
                animatedDrawable.addFrame(item, 100);
            }
            iCounter += 10;
        }
        final AnimationDrawable ani = animatedDrawable;
        ImageView imageView = ((GoalViewHolder)holder).dialImageView;
        imageView.setImageDrawable(null);
        imageView.setBackground(ani);
        ani.start();
    }

    @Override
    public int getItemCount() {
        return listConfigs.size();
    }
    public interface OnItemClickListener {
        void onItemClick(View view, Configuration viewModel);
    }
}
