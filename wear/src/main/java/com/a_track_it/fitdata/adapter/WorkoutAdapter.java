package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.wear.widget.WearableRecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class WorkoutAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<WorkoutAdapter.WorkoutListViewHolder>{
    public static final String TAG = WorkoutAdapter.class.getSimpleName();
    private ArrayList<Workout> workoutArrayList = new ArrayList<>();
    private ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private ReferencesTools mRefTools;
    private Context mContext;
    private boolean mShowSession = true;
    private boolean mUseKGs = true;
    private boolean mAllowSelect = true;
    private int selectedPos = androidx.wear.widget.WearableRecyclerView.NO_POSITION;
    private long mTargetId;
    private int startMode = 0;
    private Drawable icon1;
    private Drawable iconGMS;
    private Drawable iconFIT;

    public WorkoutAdapter(Context context, ArrayList<Workout> sessions, ArrayList<WorkoutSet> sets, boolean useKG) {
        this.mContext = context;
        this.mRefTools = ReferencesTools.getInstance();
        this.mRefTools.init(context);
        if (sessions != null) this.workoutArrayList = sessions;
        if (sets != null) this.workoutSetArrayList = sets;
        this.mUseKGs = useKG;
        this.mTargetId = 0L;
        this.startMode = 0;
        try {
            icon1 = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_home);
            //context.getPackageManager().getApplicationIcon(Constants.ATRACKIT_ATRACKIT_CLASS);
            iconGMS = context.getPackageManager().getApplicationIcon(Constants.ATRACKIT_PLAY_CLASS);
            iconFIT = AppCompatResources.getDrawable(context, R.drawable.ic_google_fit_logo);
            //  context.getPackageManager().getApplicationIcon(Constants.ATRACKIT_GFIT_CLASS);
        }catch (Exception e){
            Log.e(WorkoutAdapter.class.getSimpleName(), e.getMessage());
        }
    }
    // set show Start buttons
    public void setStartMode(int mode){ startMode = mode;}
    public int getStartMode(){ return startMode;}

    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            notifyDataSetChanged();
        }else mTargetId = 0;
    }
    public void clearSelected(){
        this.mTargetId = 0;
        this.selectedPos = androidx.wear.widget.WearableRecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }
    public int getSelectedPos(){ return this.selectedPos;}

    public void setAllowSelection(boolean bAllow){ this.mAllowSelect = bAllow; }
    public boolean isSelectable(){ return this.mAllowSelect; }

    public void setListType(boolean showSessions){
        mShowSession = showSessions;
    }
    public void clearList(){
        if (mShowSession){
            if (this.workoutArrayList != null){
                this.workoutArrayList.clear();
            }
        }else{
            if (this.workoutSetArrayList != null)
                this.workoutSetArrayList.clear();
        }
        notifyDataSetChanged();
    }
    public boolean getListType() { return mShowSession;}

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setSelectedSet(int iTarget){
        if (this.workoutSetArrayList != null){
            for (WorkoutSet set : workoutSetArrayList){
                if (set.setCount == iTarget){
                    setTargetId(set._id);
                    break;
                }
            }
        }
    }
    public void setWorkoutArrayList(ArrayList<Workout> routines){
        if (routines != null) {
            for (Workout inRoutine: routines){
                if (!this.workoutArrayList.contains(inRoutine)) this.workoutArrayList.add(inRoutine);
            }
            notifyDataSetChanged();
        }
    }
    public void setWorkoutSetArrayList(ArrayList<WorkoutSet> sets){
        if (sets != null) {
            this.workoutSetArrayList = sets;
            notifyDataSetChanged();
        }
    }
    @Override
    public WorkoutListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_workout_item, parent, false);
        return new WorkoutListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutListViewHolder holder, final int position) {
        final Workout item = (mShowSession && (workoutArrayList != null) && (workoutArrayList.size() > 0)) ? workoutArrayList.get(position) : null;
        final WorkoutSet set = (!mShowSession && (workoutSetArrayList != null) && (workoutSetArrayList.size() > 0)) ? workoutSetArrayList.get(position) : null;
        final Drawable iconOpen = AppCompatResources.getDrawable(mContext,R.drawable.ic_folder_open_white);
        final String labelTemplate = mContext.getString(R.string.label_template);
        final View.OnClickListener myClicker = view -> {
            if ((onItemClickListener != null)){
                if (mShowSession && (item != null))
                    if ((startMode == 1) && ((view.getId() == R.id.left_text1) || (view.getId() == R.id.left_text2)))
                        onItemClickListener.onItemClick(view, item,1, position);
                    else
                        onItemClickListener.onItemClick(view, item,0, position);
                if (!mShowSession && (set != null))
                    if ((startMode == 1) && ((view.getId() == R.id.left_text1) || (view.getId() == R.id.left_text2)))
                        onItemClickListener.onItemClick(view, set,1, position);
                    else
                        onItemClickListener.onItemClick(view, set,0, position);
                if (this.mAllowSelect) {
                    if (selectedPos >= 0) notifyItemChanged(selectedPos);
                    if (selectedPos != position) {
                        selectedPos = position;
                        mTargetId = 0;
                        notifyItemChanged(selectedPos);
                    } else
                        clearSelected();
                }
            }
        };
        holder.itemView.setOnClickListener(myClicker);

        if (mShowSession) {
            if (item == null) return;
            if (Utilities.isGymWorkout(item.activityID)){
                if (item.scoreTotal == Constants.WORKOUT_TEMPLATE)
                    holder.right_text1.setText(labelTemplate);
                else
                    holder.right_text1.setText(item.activityName);
                String sTemp = Constants.ATRACKIT_EMPTY;
                if (item.start > 0) sTemp = Utilities.getTimeString(item.start) + Constants.ATRACKIT_SPACE;
                if (item.duration > 0) sTemp = Constants.DURATION_HEAD.toLowerCase() + Utilities.getDurationBreakdown(item.duration);
                holder.right_text2.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                if (item.exerciseCount > 0)
                    sTemp = item.exerciseCount + " exercises";
                if (item.bodypartCount > 0)
                    sTemp += item.bodypartCount + " bodyparts";
                if (item.setCount > 0)
                    sTemp += " " + item.setCount + " sets" + Constants.ATRACKIT_SPACE;
                if (item.repCount == 1)
                    sTemp += item.repCount + " rep";
                else
                    if (item.repCount > 0) sTemp += item.repCount + " reps";
                holder.right_text3.setText(sTemp);
                Drawable img = AppCompatResources.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(item.activityID));
                int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.home_button_icon_size);
                if (img != null) img.setBounds(0, 0, iconSize, iconSize);
                holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                if (startMode == 1)
                    holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, iconOpen);
                else{
                    if ((item.packageName != null) && item.packageName.length() > 0) {
                        if (item.packageName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)) holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,icon1);
                        else if (item.packageName.equals(Constants.ATRACKIT_PLAY_CLASS)) holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,iconGMS);
                        else if (item.packageName.equals(Constants.ATRACKIT_GFIT_CLASS)) holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,iconFIT);
                        else {
                            try {
                                Drawable icon = mContext.getPackageManager().getApplicationIcon(item.packageName);
                                holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
                            }catch (PackageManager.NameNotFoundException nf){
                                holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null);
                            }
                        }
                    }else
                        holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null);
                }

            }else
                if (Utilities.isShooting(item.activityID)){

                }else {
                    holder.right_text1.setText(item.activityName);
                    String sTemp = Constants.ATRACKIT_EMPTY;
                    if (item.start > 0) sTemp = Utilities.getTimeString(item.start) + Constants.ATRACKIT_SPACE;
                    holder.right_text2.setText(sTemp);
                    if (item.duration > 0) {
                        sTemp = Constants.DURATION_HEAD + Utilities.getDurationBreakdown(item.duration);
                        //TimeUnit.MILLISECONDS.toMinutes(item.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(item.duration) + " sec";
                        holder.right_text3.setText(sTemp);
                    }
                    Drawable img = AppCompatResources.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(item.activityID));
                    int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.home_button_icon_size);
                    if (img != null) img.setBounds(0, 0, iconSize, iconSize);
                    holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                    if (startMode == 1 && !Utilities.isDetectedActivity(item.activityID))
                        holder.left_text2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, iconOpen);
                }
            //holder.left_text2.setText(currentWorkoutStateString(item));
            if (mTargetId == 0) {
                holder.itemView.setSelected(selectedPos == position);
            }else
                holder.itemView.setSelected(item._id == mTargetId);
        }
        else{
            if (set == null) return;
            if (mTargetId == 0)
                holder.itemView.setSelected(selectedPos == position);
            else
                holder.itemView.setSelected(set._id == mTargetId);
            if (Utilities.isGymWorkout(set.activityID)) {  // gym
                if (set.exerciseName != null)
                    holder.right_text1.setText(set.exerciseName);
                else
                    holder.right_text1.setText(set.activityName);
                String sTemp = Constants.ATRACKIT_EMPTY;
                if (set.start > 0) {
                    sTemp = Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
                    if (set.duration > 0)
                        sTemp += " " + Utilities.getDurationBreakdown(set.duration); // + TimeUnit.MILLISECONDS.toMinutes(set.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(set.duration) + " sec";
                    holder.right_text2.setText(sTemp);
                    if (set.rest_duration != null &&  set.rest_duration > 0){
                        sTemp = mContext.getString(R.string.label_rest_countdown);
                        sTemp += Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(set.rest_duration);
                    }else
                        if (set.resistance_type != null)
                            sTemp = Utilities.getResistanceType(set.resistance_type);

                    if ((set.bodypartName != null) && (set.bodypartName.length() > 0))
                        sTemp += Constants.ATRACKIT_SPACE + set.bodypartName;
                    holder.right_text3.setText(sTemp);
                }else {
                    sTemp = Constants.ATRACKIT_EMPTY;
                    holder.right_text2.setText(sTemp);
                    if (set.resistance_type != null)
                        sTemp = Utilities.getResistanceType(set.resistance_type);
                    holder.right_text3.setText(sTemp);
                }
                if (set.repCount <= 20) {
                    try {
                        Drawable img = mRefTools.getRepsDrawableByCount(set.repCount);
                        int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.home_button_icon_size);
                        if (img != null) {
                            img.setBounds(0, 0, iconSize, iconSize);
                            holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                            sTemp =  Constants.REPS_TAIL + Constants.ATRACKIT_SPACE;
                            holder.left_text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                        } else {
                            sTemp = Integer.toString(set.repCount) + Constants.REPS_TAIL;
                        }
                    }catch (Exception e){
                        Log.e(TAG,"rep image error " + e.getMessage());
                        sTemp = Integer.toString(set.repCount) + Constants.REPS_TAIL;
                    }
                }else
                    sTemp = Integer.toString(set.repCount) + Constants.REPS_TAIL;

                holder.left_text1.setText(sTemp);
                if (mUseKGs)
                    sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,set.weightTotal) + Constants.KG_TAIL;
                else
                    sTemp = Utilities.KgToPoundsDisplay(set.weightTotal) + Constants.LBS_TAIL;
                holder.left_text2.setText(sTemp);
            }else{
                if (set.exerciseName != null)
                    holder.right_text1.setText(set.exerciseName);
                else
                    holder.right_text1.setText(set.activityName);

                String sTemp = Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
                sTemp += " " + TimeUnit.MILLISECONDS.toMinutes(set.duration) + Constants.MINS_TAIL + TimeUnit.MILLISECONDS.toSeconds(set.duration) + Constants.SECS_TAIL;
                holder.right_text2.setText(sTemp);
                sTemp = " ";
                holder.left_text1.setText(sTemp);
                Drawable img = AppCompatResources.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(set.activityID));
                int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.home_button_icon_size);
                if (img != null) img.setBounds(0, 0, iconSize, iconSize);
                holder.left_text2.setText(sTemp);
                holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
            }
        }
    }

/*    // private Context context;
    private String currentWorkoutStateString(Workout workout){
        String retState = "Invalid";

        if (workout != null) {
            if (((workout.name != null) && (workout.name.length() > 0))
                    && (workout.parentID < 0))
                retState = "template";  // template
            else {
                if (workout.start == 0) {
                    if (workout.activityID > 0) retState = "pending";
                } else {
                    if (workout.end == 0)
                        retState = "live";
                    else
                        retState = "completed";
                }
            }
            // if (workout.last_sync > 0) retState += WORKOUT_SYNCD;
        }
        return retState;
    }*/

    /**
     * Here is the key method to apply the animation
     *
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
     *
     */
    @Override
    public int getItemCount() {
        int i;
        if (mShowSession)
            i =  (workoutArrayList == null) ? 0 : workoutArrayList.size();
        else
            i = (workoutSetArrayList == null) ? 0 : workoutSetArrayList.size();
        return i;
    }


    public interface OnItemClickListener {
        void onItemClick(View view, Object viewModel, int startMode, int position);
    }

    public static class WorkoutListViewHolder extends WearableRecyclerView.ViewHolder{
        public final TextView left_text1;
        public final TextView left_text2;
        public final TextView right_text1;
        public final TextView right_text2;
        public final TextView right_text3;

        public WorkoutListViewHolder(View itemView) {
            super(itemView);
            left_text1 = itemView.findViewById(R.id.left_text1);
            left_text2 = itemView.findViewById(R.id.left_text2);
            right_text1 = itemView.findViewById(R.id.right_text1);
            right_text2 = itemView.findViewById(R.id.right_text2);
            right_text3 = itemView.findViewById(R.id.right_text3);
        }
    }
}
