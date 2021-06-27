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
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Configuration;
import com.a_track_it.workout.common.data_model.UserDailyTotals;
import com.google.android.gms.fitness.data.DataType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class GoalListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Configuration> listConfigs = new ArrayList<>();
    private GoalListAdapter.OnItemClickListener onItemClickListener;
    final Context mContext;
    final Resources mResources;
    private UserDailyTotals userDailyTotals;
    private boolean bUseKg;

    class GoalViewHolder extends RecyclerView.ViewHolder {
        final ImageView dialImageView;
        final TextView titleTextView;
        final TextView percentTextView;
        final TextView ofTextView;
        final TextView currentTextView;
        final TextView goalTextView;

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

    public GoalListAdapter(Context context, UserDailyTotals udt, boolean KG){
        mContext = context;
        mResources = context.getResources();
        bUseKg = KG;
        if (udt != null) userDailyTotals = udt;
    }
    public void AddUserDailyTotal(UserDailyTotals udt){
        userDailyTotals = udt;
    }

    public void AddGoal(Configuration goalConfig){
        Iterator<Configuration> iterator = listConfigs.iterator();
        boolean bFound = false;
        while (iterator.hasNext()){
            Configuration conf = iterator.next();
            if (conf.stringName.equals(goalConfig.stringName)){
                conf.longValue = goalConfig.longValue;
                conf.stringValue = goalConfig.stringValue;
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
                conf.longValue = (currentValue);
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
        Configuration goalConfig = (position < listConfigs.size()) ? listConfigs.get(position) : null;
        if (goalConfig != null) {
            String sTemp1 = goalConfig.stringValue;
            double goalValue = (sTemp1.length() > 0) ? Double.parseDouble(sTemp1) : 0;
            double current = Double.parseDouble(Long.toString(goalConfig.longValue));
            double percentage = (goalValue > 0) ? (current / goalValue) * 100.0d : 0;
            String sTemp = Math.round(percentage) + "%";
            ((GoalViewHolder) holder).percentTextView.setText(sTemp);
            // empty holder!
            if (goalConfig.stringName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)) {
                ((GoalViewHolder) holder).titleTextView.setText(mContext.getString(R.string.label_no_goals));
                ((GoalViewHolder) holder).goalTextView.setText(mContext.getString(R.string.label_no_goals_advice));
                ((GoalViewHolder) holder).currentTextView.setVisibility(TextView.GONE);
                ((GoalViewHolder) holder).percentTextView.setVisibility(TextView.GONE);
                ((GoalViewHolder) holder).ofTextView.setVisibility(TextView.GONE);
                ImageView imageView = ((GoalViewHolder) holder).dialImageView;
                imageView.setVisibility(View.INVISIBLE);
                return;
            }
            if (goalConfig.stringName.equals(DataType.TYPE_MOVE_MINUTES.getName())) {
                ((GoalViewHolder) holder).titleTextView.setText(mContext.getString(R.string.label_move_minutes));
                sTemp = Math.round(goalValue) + Constants.MOVE_MINS_TAIL;
                ((GoalViewHolder) holder).goalTextView.setText(sTemp);
                sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
                ((GoalViewHolder) holder).currentTextView.setText(sTemp);
            }
            if (goalConfig.stringName.equals(DataType.TYPE_HEART_POINTS.getName())) {
                ((GoalViewHolder) holder).titleTextView.setText(mContext.getString(R.string.label_heart_points));
                sTemp = Math.round(goalValue) + Constants.HEART_PTS_TAIL;
                ((GoalViewHolder) holder).goalTextView.setText(sTemp);
                sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
                ((GoalViewHolder) holder).currentTextView.setText(sTemp);
            }
            if (goalConfig.stringName.equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
                ((GoalViewHolder) holder).titleTextView.setText(mContext.getString(R.string.label_steps_goal));
                sTemp = Math.round(goalValue) + Constants.ATRACKIT_SPACE; // + mContext.getString(R.string.label_steps);
                ((GoalViewHolder) holder).goalTextView.setText(sTemp);
                sTemp = Math.round(current) + Constants.ATRACKIT_SPACE;
                ((GoalViewHolder) holder).currentTextView.setText(sTemp);
            }
            AnimationDrawable animatedDrawable = (AnimationDrawable) AppCompatResources.getDrawable(mContext, com.a_track_it.workout.common.R.drawable.dial_animation);
            int iCounter = 10;
            while (iCounter <= percentage && (iCounter <= 100)) {
                String sName = "ic_dial_" + iCounter;
                int resId = mResources.getIdentifier(sName, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
                if (resId > 0) {
                    Drawable item = AppCompatResources.getDrawable(mContext, resId);
                    animatedDrawable.addFrame(item, 100);
                }
                iCounter += 10;
            }
            final AnimationDrawable ani = animatedDrawable;
            ImageView imageView = ((GoalViewHolder) holder).dialImageView;
            imageView.setImageDrawable(null);
            imageView.setBackground(ani);
            ani.start();
        }
        else if(userDailyTotals != null){
            ((GoalViewHolder) holder).currentTextView.setVisibility(TextView.GONE);
            ((GoalViewHolder) holder).percentTextView.setVisibility(TextView.GONE);
            ((GoalViewHolder) holder).ofTextView.setVisibility(TextView.GONE);
            ImageView imageView = ((GoalViewHolder) holder).dialImageView;
            imageView.setImageDrawable(AppCompatResources.getDrawable(mContext,R.drawable.ic_report_white));
            imageView.setVisibility(View.VISIBLE);
            DateFormat dateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);
            String sSummary = Constants.ATRACKIT_EMPTY; // = "As at " + dateFormat.format(new Date(userDailyTotals._id)) + Constants.ATRACKIT_SPACE;
            if (userDailyTotals.stepCount > 0) sSummary = mContext.getString(R.string.sensor_fit_steps) + Constants.ATRACKIT_SPACE + userDailyTotals.stepCount + Constants.LINE_DELIMITER;
            String sTemp = Math.round(userDailyTotals.distanceTravelled) + Constants.ALT_TAIL;
            if (userDailyTotals.distanceTravelled > 0){
                if (userDailyTotals.distanceTravelled > 999) sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, (userDailyTotals.distanceTravelled/1000)) + Constants.KM_TAIL;
                if (!bUseKg){
                    try {
                        double inches = (39.3701 * userDailyTotals.distanceTravelled);
                        int feet = (int) (inches / 12);
                        int miles = (feet / 5280);
                        int milesConversion = (miles > 0) ? (miles - (Math.round(userDailyTotals.distanceTravelled) % miles)) : 0;
                        int feetConversion = ((miles > 0) && (feet > 0)) ? ((miles - milesConversion) % feet) : 0;
                        if (milesConversion > 0)
                            sTemp = milesConversion + " mi " + feetConversion + " ft";
                        else
                            sTemp = feet + " ft";
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                sSummary += mContext.getString(R.string.sensor_fit_distance) + Constants.ATRACKIT_SPACE + sTemp + Constants.LINE_DELIMITER;
            }
            if (userDailyTotals.caloriesExpended > 0){
                if (userDailyTotals.caloriesExpended > 1000)
                sSummary += mContext.getString(R.string.sensor_fit_calories) + Constants.ATRACKIT_SPACE + Math.round(userDailyTotals.caloriesExpended/1000) + Constants.KCAL_TAIL + Constants.LINE_DELIMITER;
                else
                sSummary += mContext.getString(R.string.sensor_fit_calories) + Constants.ATRACKIT_SPACE + Math.round(userDailyTotals.caloriesExpended) + Constants.CAL_TAIL + Constants.LINE_DELIMITER;
            }
            if (userDailyTotals.maxBPM > 0) sSummary += "max BPM " + Math.round(userDailyTotals.maxBPM) + Constants.LINE_DELIMITER;
            if (userDailyTotals.minBPM > 0) sSummary += "min BPM " + Math.round(userDailyTotals.minBPM) + Constants.LINE_DELIMITER;
            if (userDailyTotals.avgBPM > 0) sSummary += "avg BPM " + Math.round(userDailyTotals.avgBPM) + Constants.LINE_DELIMITER;
            if (userDailyTotals.avgSpeed > 0) sSummary += "avg Speed " + Math.round(userDailyTotals.avgSpeed) + Constants.LINE_DELIMITER;
            //if (userDailyTotals.lastLocation.length() > 0) sSummary += "At " + userDailyTotals.lastLocation;
            if ((userDailyTotals.durationStill > 0) ||(userDailyTotals.durationVehicle > 0)||(userDailyTotals.durationWalking > 0)||(userDailyTotals.durationBiking > 0)
                ||(userDailyTotals.durationOnFoot > 0)||(userDailyTotals.durationTilting > 0)||(userDailyTotals.durationUnknown > 0)){
                sSummary = sSummary + "\nDetected Activity\n";
                ReferencesTools rf = ReferencesTools.setInstance(mContext);
                if (userDailyTotals.durationStill > 0)
                    sSummary += rf.getFitnessActivityTextById(3) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationStill);
                if (userDailyTotals.durationVehicle > 0)
                    sSummary += rf.getFitnessActivityTextById(0) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationVehicle);
                if (userDailyTotals.durationOnFoot > 0)
                    sSummary += rf.getFitnessActivityTextById(2) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationOnFoot);
                if (userDailyTotals.durationBiking > 0)
                    sSummary += rf.getFitnessActivityTextById(1) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationBiking);
                if (userDailyTotals.durationUnknown > 0)
                    sSummary += rf.getFitnessActivityTextById(4) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationUnknown);
                if (userDailyTotals.durationTilting > 0)
                    sSummary += rf.getFitnessActivityTextById(5) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationTilting);
                if (userDailyTotals.durationWalking > 0)
                    sSummary += rf.getFitnessActivityTextById(7) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationWalking);
                if (userDailyTotals.durationRunning > 0)
                    sSummary += rf.getFitnessActivityTextById(8) + Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(userDailyTotals.durationRunning);

            }
            ((GoalViewHolder) holder).titleTextView.setText("Summary Today");
            ((GoalViewHolder) holder).goalTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            ((GoalViewHolder) holder).goalTextView.setTextSize(16f);
            ((GoalViewHolder) holder).goalTextView.setText(sSummary);

        }
    }

    @Override
    public int getItemCount() {
        int itemCount = listConfigs.size();
        if (userDailyTotals != null) itemCount++;
        return itemCount;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, Configuration viewModel);
    }
}
