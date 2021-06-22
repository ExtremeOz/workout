package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class WorkoutAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<WorkoutAdapter.WorkoutListViewHolder>{
    public static final String LOG_TAG = WorkoutAdapter.class.getSimpleName();

    private ArrayList<Workout> workoutArrayList = new ArrayList<>();
    private ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private ReferencesTools mRefTools;
    private Context mContext;
    private boolean mShowSession = true;
    private boolean mUseKGs = true;
    private boolean mAllowSelect = true;
    private boolean mAllowEdit = true;
    private int selectedPos = RecyclerView.NO_POSITION;
    private long mTargetId;
    private int childMode = 0;
    private Drawable icon1;
    private Drawable iconGMS;
    private Drawable iconFIT;

    public WorkoutAdapter(Context context, ArrayList<Workout> sessions, ArrayList<WorkoutSet> sets, boolean useKG) {
        this.mContext = context;
        this.mRefTools = ReferencesTools.setInstance(context);
        if (sessions != null) this.workoutArrayList = sessions;
        if (sets != null) this.workoutSetArrayList = sets;
        this.mUseKGs = useKG;
        this.selectedPos = RecyclerView.NO_POSITION;
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
    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            notifyDataSetChanged();
        }else mTargetId = 0;
    }
    public void clearSelected(){
        mTargetId = 0;
        selectedPos = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }
    public void setEditMode(boolean bAllow){ mAllowEdit = bAllow;}
    public boolean getEditMode(){ return mAllowEdit;}
    public int getSelectedPos(){ return this.selectedPos;}
    public void setChildMode(int mode){ this.childMode = mode;}
    public void clearList(){
        if (mShowSession)
            this.workoutArrayList.clear();
        else
            this.workoutSetArrayList.clear();
    }
    public void setAllowSelection(boolean bAllow){ this.mAllowSelect = bAllow; }
    public boolean isSelectable(){ return this.mAllowSelect; }

    public void setListType(boolean showSessions){
        mShowSession = showSessions;
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
    public long getTargetId(){
        return mTargetId;
    }
    public void setWorkoutArrayList(ArrayList<Workout> routines){
        if (routines != null) {
            if ((this.workoutArrayList != null) && (this.workoutArrayList.size() > 0)) this.workoutArrayList.clear();
            this.workoutArrayList.addAll(routines);
            notifyDataSetChanged();
        }
    }
    public List<Workout> getWorkoutList(){
        return workoutArrayList;
    }
    public void setWorkoutSetArrayList(ArrayList<WorkoutSet> sets){
        if (sets != null) {
            if ((this.workoutSetArrayList != null) && (this.workoutSetArrayList.size() > 0)) this.workoutSetArrayList.clear();
            this.workoutSetArrayList.addAll(sets);
            notifyDataSetChanged();
        }
    }
    public List<WorkoutSet> getWorkoutSetList(){
        return this.workoutSetArrayList;
    }
    @Override
    public WorkoutAdapter.WorkoutListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_workout_item, parent, false);
        return new WorkoutAdapter.WorkoutListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutListViewHolder holder, final int position) {
        final String labelTemplate = mContext.getString(R.string.label_template);
        final Drawable iconOpen = AppCompatResources.getDrawable(mContext,R.drawable.ic_folder_open_white);
        final Workout workout = (mShowSession && (workoutArrayList != null) && (workoutArrayList.size() > 0)) ? workoutArrayList.get(position) : null;
        final WorkoutSet set = (!mShowSession && (workoutSetArrayList != null) && (workoutSetArrayList.size() > 0)) ? workoutSetArrayList.get(position) : null;
        if (childMode > 0)
           holder.itemView.setBackground(AppCompatResources.getDrawable(mContext,R.drawable.bg_selector_child_recycleview));


        final View.OnClickListener myClicker = view -> {
            if (onItemClickListener != null) {
                int UID = ((int)view.getTag());
                if (mShowSession)
                    onItemClickListener.onItemClick(UID, workout, position);
                else
                    onItemClickListener.onItemClick(UID, set, position);

                if (selectedPos >= 0) notifyItemChanged(selectedPos);
                if (selectedPos != position) {
                    selectedPos = position;
                    mTargetId = 0;
                    notifyItemChanged(selectedPos);
                }else
                    clearSelected();
            }
        };
        if (mAllowSelect)
            if (mTargetId == 0)
                holder.itemView.setSelected(selectedPos == position);
            else {
                if (mShowSession)
                    holder.itemView.setSelected(workout._id == mTargetId);
                else
                    holder.itemView.setSelected(set._id == mTargetId);
        }
        holder.itemView.setOnClickListener(myClicker);
        holder.itemView.setTag(Constants.UID_btn_recycle_item_select);
        if (mAllowEdit) {
            holder.btn_recycle_item_delete.setOnClickListener(myClicker);
            holder.btn_recycle_item_delete.setTag(Constants.UID_btn_recycle_item_delete);
            holder.btn_recycle_item_copy.setOnClickListener(myClicker);
            holder.btn_recycle_item_copy.setTag(Constants.UID_btn_recycle_item_copy);
        }else{
            holder.btn_recycle_item_delete.setVisibility(View.GONE);
            holder.btn_recycle_item_copy.setVisibility(View.GONE);
        }

      //  holder.
        if (mShowSession) {
            if (workout == null) return;
            try{
                if ((workout.packageName != null) && workout.packageName.length() > 0) {
                    if (workout.packageName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)) holder.package_imageView.setImageDrawable(icon1);
                    else if (workout.packageName.equals(Constants.ATRACKIT_PLAY_CLASS)) holder.package_imageView.setImageDrawable(iconGMS);
                    else if (workout.packageName.equals(Constants.ATRACKIT_GFIT_CLASS)) holder.package_imageView.setImageDrawable(iconFIT);
                    else {
                        Drawable icon = mContext.getPackageManager().getApplicationIcon(workout.packageName);
                        holder.package_imageView.setImageDrawable(icon);
                    }
                }else
                    holder.package_imageView.setVisibility(ImageView.GONE);
            }catch (PackageManager.NameNotFoundException e)
            {
                Log.e(LOG_TAG, "error loading icon " +  workout.packageName);
                e.printStackTrace();
                holder.package_imageView.setVisibility(ImageView.GONE);
            }
            if (Utilities.isGymWorkout(workout.activityID)) {
                if (workout.scoreTotal == Constants.WORKOUT_TEMPLATE)
                    holder.right_text1.setText(labelTemplate);
                else
                    holder.right_text1.setText(workout.activityName);
                String sTemp = Constants.ATRACKIT_EMPTY;
                if (workout.start > 0) sTemp = Utilities.getTimeString(workout.start) + Constants.ATRACKIT_SPACE;
                if (workout.duration > 0)
                    sTemp = Constants.DURATION_HEAD.toLowerCase() + Utilities.getDurationBreakdown(workout.duration);
                holder.right_text2.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                if (workout.exerciseCount > 0)
                    sTemp = workout.exerciseCount + " exercises";
                if (workout.bodypartCount > 0)
                    sTemp += workout.bodypartCount + " bodyparts";
                if (workout.setCount > 0)
                    sTemp += " " + workout.setCount + " sets" + Constants.ATRACKIT_SPACE;
                if (workout.repCount == 1)
                    sTemp += workout.repCount + " rep";
                else if (workout.repCount > 0) sTemp += workout.repCount + " reps";
                holder.right_text3.setText(sTemp);
                Drawable img = AppCompatResources.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(workout.activityID));
                holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
            }
            else {
                holder.right_text1.setText(workout.activityName);
                String sTemp = Constants.ATRACKIT_EMPTY;
                if (workout.start > 0) sTemp = Utilities.getTimeString(workout.start) + Constants.ATRACKIT_SPACE;
                holder.right_text2.setText(sTemp);
                if (workout.duration > 0) {
                    sTemp = Constants.DURATION_HEAD + Utilities.getDurationBreakdown(workout.duration);
                    //TimeUnit.MILLISECONDS.toMinutes(item.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(item.duration) + " sec";
                    holder.right_text3.setText(sTemp);
                }
                int activityRes = mRefTools.getFitnessActivityIconResById(workout.activityID);
                Drawable img = AppCompatResources.getDrawable(mContext, activityRes);
                holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                holder.left_text2.setText(currentWorkoutStateString(workout));
                if (mTargetId == 0)
                    holder.itemView.setSelected(selectedPos == position);
                else
                    holder.itemView.setSelected(workout._id == mTargetId);
            }
/*            if ((workout.start > 0) && (workout.end > 0)) {
                holder.btn_recycle_item_copy.setVisibility(View.GONE);
            }else
                holder.btn_recycle_item_delete.setVisibility(View.GONE);*/
        }
        else{
            if (set == null) return;
            holder.package_imageView.setVisibility(ImageView.GONE);
            if (Utilities.isGymWorkout(set.activityID)) {  // gym
                String sTemp = Constants.ATRACKIT_EMPTY;
                if (set.exerciseName != null) {
                    holder.right_text1.setText(set.exerciseName);
                    sTemp = set.exerciseName;
                }else {
                    holder.right_text1.setText(set.activityName);
                    sTemp = set.activityName;
                }
                if (sTemp.length() > 25)
                    holder.right_text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                sTemp = Constants.ATRACKIT_EMPTY;
                if (set.start > 0) {
                    if (childMode == 0)
                        sTemp += "@" + Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
                    if (set.duration > 0)
                        sTemp += Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(set.duration); // + TimeUnit.MILLISECONDS.toMinutes(set.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(set.duration) + " sec";
                    holder.right_text2.setText(sTemp);
                    sTemp = Constants.ATRACKIT_EMPTY;
                    if (set.rest_duration != null && set.rest_duration > 0){
                        sTemp = mContext.getString(R.string.label_rest_countdown);
                        sTemp += Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(set.rest_duration);
                    }else
                        if (set.resistance_type != null)
                            sTemp = Utilities.getResistanceType(set.resistance_type);
                    if ((set.bodypartName != null) && (set.bodypartName.length() > 0))
                        sTemp += Constants.ATRACKIT_SPACE + set.bodypartName;
                }
                else {
                    sTemp = Constants.ATRACKIT_EMPTY;
                    holder.right_text2.setText(sTemp);
                    sTemp = Constants.ATRACKIT_EMPTY;
                    if (set.resistance_type != null)
                        sTemp += Utilities.getResistanceType(set.resistance_type);
                    if ((set.bodypartName != null) && (set.bodypartName.length() > 0))
                        sTemp += Constants.ATRACKIT_SPACE + set.bodypartName;
                }
                holder.right_text3.setText(sTemp);
                if (set.repCount != null && set.repCount <= 20) {
                    try {
                        int numberRes = getNumberDrawableIdentifier(mContext, set.repCount);
                        Drawable img = AppCompatResources.getDrawable(mContext, numberRes);
                        if (img != null) {
                            holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                            holder.left_text1.setCompoundDrawablePadding(2);
                            sTemp =  Constants.REPS_TAIL + Constants.ATRACKIT_SPACE;
                            holder.left_text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                        } else {
                            holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                            sTemp = Integer.toString(set.repCount) + Constants.REPS_TAIL;
                        }
                    }catch (Exception e){
                        Log.e(LOG_TAG,"rep image error " + e.getMessage());
                    }
                }else
                    sTemp = (set.repCount != null) ? (set.repCount) + Constants.REPS_TAIL :  8 + Constants.REPS_TAIL;

                holder.left_text1.setText(sTemp);
                if (mUseKGs) {
                    double intWeight = (set.weightTotal != null) ? Math.floor(set.weightTotal) : 0;
                    if (set.weightTotal % intWeight != 0)
                        sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, set.weightTotal) + Constants.KG_TAIL;
                    else
                        sTemp = new DecimalFormat("#").format(intWeight) + Constants.KG_TAIL;
                }else
                    sTemp = (set.weightTotal != null) ? Utilities.KgToPoundsDisplay(set.weightTotal) + Constants.LBS_TAIL : 0 + Constants.LBS_TAIL;
                holder.left_text2.setText(sTemp);
                if (set.scoreTotal != Constants.FLAG_PENDING) {
                    holder.btn_recycle_item_copy.setVisibility(View.GONE);
                    holder.btn_recycle_item_delete.setVisibility(View.GONE);
                }else {
                    holder.btn_recycle_item_copy.setVisibility(View.VISIBLE);
                    holder.btn_recycle_item_delete.setVisibility(View.VISIBLE);
                }
                if (mTargetId == 0)
                    holder.itemView.setSelected(selectedPos == position);
                else {
                    holder.itemView.setSelected(set._id == mTargetId);
                }
            }else{
                String sTemp = Utilities.getTimeString(set.start) + Constants.ATRACKIT_SPACE;
                sTemp += " " + TimeUnit.MILLISECONDS.toMinutes(set.duration) + Constants.MINS_TAIL;
                holder.right_text2.setText(sTemp);
                if (Utilities.isShooting(set.activityID)) {
                    if (set.start > 0) {
                        holder.right_text1.setText(set.score_card);
                        sTemp = Utilities.getTimeDateString(set.start) + Constants.ATRACKIT_SPACE;
                        if (set.duration > 0)
                            sTemp += " " + Utilities.getDurationBreakdown(set.duration); // + TimeUnit.MILLISECONDS.toMinutes(set.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(set.duration) + " sec";
                        holder.right_text2.setText(sTemp);
                        if (set.rest_duration != null &&  set.rest_duration > 0){
                            sTemp = mContext.getString(R.string.label_rest_countdown);
                            sTemp += Constants.ATRACKIT_SPACE + Utilities.getDurationBreakdown(set.rest_duration);
                        }else
                            sTemp = Constants.ATRACKIT_EMPTY;
                        holder.right_text3.setText(sTemp);
                    } else {
                        holder.right_text1.setText(set.activityName);

                        sTemp = " ";
                        holder.left_text1.setText(sTemp);
                        Drawable img = AppCompatResources.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(set.activityID));
                        holder.left_text2.setText(sTemp);
                        holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                    }
                }
                if (mTargetId == 0)
                    holder.itemView.setSelected(selectedPos == position);
                else
                    holder.itemView.setSelected(workout._id == mTargetId);
            }
        }
        //int vibrant = ContextCompat.getColor(mContext, mRefTools.getFitnessActivityColorById(item.activityID));
        //holder.container.setBackgroundColor(vibrant);
//        holder.image.setColorFilter(ContextCompat.getColor(mContext,android.R.color.white), PorterDuff.Mode.SRC_IN);
//        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
//        holder.image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (onItemClickListener != null) {
//                    Log.d(TAG, "image onclick " + Integer.toString(workoutArrayList.size()));
//                    onItemClickListener.onItemClick(view, workoutArrayList.get(position));
//                }
//            }
//        });
        //int vibrant = ContextCompat.getColor(mContext, R.color.colorAccent);  //item.color
//        holder.container.setBackgroundColor(vibrant);
//        holder.image.setColorFilter(ContextCompat.getColor(mContext,android.R.color.white), PorterDuff.Mode.SRC_IN);
//        //holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
//        holder.text.setTag(item);

        // This is just not working as expected. Switching to use hard coded color.
        // TODO: Colorize image to match pre-defined color. Allowing user to select the color at some point.
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
        //   holder.container.setBackgroundColor(vibrant);
        //setAnimation(holder.container, position);
    }

    // private Context context;
    private String currentWorkoutStateString(Workout workout){
        String retState = "Invalid";

        if (workout != null) {
            if (((workout.name != null) && (workout.name.length() > 0))
                    && (((workout.parentID != null) && (workout.parentID < 0))))
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
    }
    private int getNumberDrawableIdentifier(Context context, int iVal){
        Resources res = context.getResources();
        String sIdent = "ic_number_"; String sTail = "_circle_white";
        switch (iVal){
            case 1:
                sIdent += "one" + sTail;
                break;
            case 2:
                sIdent += "two" + sTail;
                break;
            case 3:
                sIdent += "three" + sTail;
                break;
            case 4:
                sIdent += "four" + sTail;
                break;
            case 5:
                sIdent += "five" + sTail;
                break;
            case 6:
                sIdent += "six" + sTail;
                break;
            case 7:
                sIdent += "seven" + sTail;
                break;
            case 8:
                sIdent += "eight" + sTail;
                break;
            case 9:
                sIdent += "nine" + sTail;
                break;
            case 10:
                sIdent += "ten" + sTail;
                break;
            case 11:
                sIdent += "eleven_circle";
                break;
            case 12:
                sIdent += "twelve_circle";
                break;
            case 13:
                sIdent = "ic_thirteen_circle";
                break;
            case 14:
                sIdent += "fourteen_circle";
                break;
            case 15:
                sIdent += "fifteen_circle";
                break;
            case 16:
                sIdent += "sixteen_circle";
                break;
            case 17:
                sIdent += "seventeen_circle";
                break;
            case 18:
                sIdent += "eighteen_circle";
                break;
            case 19:
                sIdent += "nineteen_circle";
                break;
            case 20:
                sIdent += "twenty_circle";
                break;
            default:
                sIdent = "ic_question_mark_button";
                break;
        }
        int result = res.getIdentifier(sIdent,Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        return result;
    }
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
  public  class WorkoutListViewHolder extends RecyclerView.ViewHolder{
        public final TextView left_text1;
        public final ImageView package_imageView;
        public final TextView left_text2;
        public final TextView right_text1;
        public final TextView right_text2;
        public final TextView right_text3;
        public final MaterialButton btn_recycle_item_delete;
        public final MaterialButton btn_recycle_item_copy;

        public WorkoutListViewHolder(View itemView) {
            super(itemView);
            left_text1 = itemView.findViewById(R.id.left_text1);
            package_imageView = itemView.findViewById(R.id.package_image_view);
            left_text2 = itemView.findViewById(R.id.left_text2);
            right_text1 = itemView.findViewById(R.id.right_text1);
            right_text2 = itemView.findViewById(R.id.right_text2);
            right_text3 = itemView.findViewById(R.id.right_text3);
            btn_recycle_item_delete = itemView.findViewById(R.id.btn_recycle_item_delete);
            btn_recycle_item_copy = itemView.findViewById(R.id.btn_recycle_item_copy);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int UID, Object viewModel, int position);
    }
}
