package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;


public class WorkoutAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<WorkoutListViewHolder>{

    public static final String TAG = "WorkoutAdapter";

    private ArrayList<Workout> workoutArrayList = new ArrayList<>();
    private ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private ReferencesTools mRefTools;
    private Context mContext;
    private boolean mShowSession = true;
    private boolean mUseKGs = true;

    public WorkoutAdapter(Context context, ArrayList<Workout> sessions, ArrayList<WorkoutSet> sets, boolean useKG) {
        this.mContext = context;
        this.mRefTools = ReferencesTools.getInstance();
        this.mRefTools.init(context);
        if (sessions != null) this.workoutArrayList = sessions;
        if (sets != null) this.workoutSetArrayList = sets;
        this.mUseKGs = useKG;
    }
    public void setListType(boolean showSessions){
        mShowSession = showSessions;
    }
    public boolean getListType() { return mShowSession;}
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
            for (WorkoutSet inSet : sets){
             if (!this.workoutSetArrayList.contains(inSet)) this.workoutSetArrayList.add(inSet);
            }
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
        final Workout item;
        final WorkoutSet set;
        if (mShowSession) {
            item = workoutArrayList.get(position);
            holder.right_text1.setText(item.activityName);
            holder.right_text1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(view, item);
                    }
                }
            });
            String sTemp = Utilities.getTimeDateString(item.start) + " ";
            holder.right_text2.setText(sTemp);
            sTemp = "duration " + Utilities.getDurationBreakdown(item.duration);
                    //TimeUnit.MILLISECONDS.toMinutes(item.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(item.duration) + " sec";
            holder.right_text3.setText(sTemp);
            Drawable img = ContextCompat.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(item.activityID));

            holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
            holder.left_text1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        Log.d(TAG, "image onclick " + item.activityName);
                        onItemClickListener.onItemClick(view, item);
                    }
                }
            });
        }else{
            set = workoutSetArrayList.get(position);
            if (set.exerciseID > 0) {  // gym
                holder.right_text1.setText(set.exerciseName);
                holder.right_text1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener != null) {
                            // Log.d(TAG, "text onclick " + Integer.toString(workoutArrayList.size()));
                            //onItemClickListener.onItemClick(view, item);
                        }
                    }
                });
                String sTemp; String sReps;
                if (set.start > 0)
                    sTemp = Utilities.getTimeDateString(set.start) + " ";
                else
                    sTemp = "00:00";

                if (set.duration > 0)
                    sTemp += " " + Utilities.getDurationBreakdown(set.duration); // + TimeUnit.MILLISECONDS.toMinutes(set.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(set.duration) + " sec";
                holder.right_text2.setText(sTemp);
                if (set.rest_duration > 0){
                    sTemp = mContext.getString(R.string.label_recovery);
                    sTemp += mContext.getString(R.string.my_space_string) + Utilities.getDurationBreakdown(set.rest_duration);
                }else
                    sTemp = Utilities.getResistanceType(set.resistance_type);
                holder.right_text3.setText(sTemp);

                sReps = mContext.getString(R.string.label_rep).toLowerCase();
                if (set.repCount < 11) {
                    Drawable img = mRefTools.getRepsDrawableByCount(set.repCount);
                    if (img != null) {
                        holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                        sTemp = " " + sReps +  " ";
                        holder.left_text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                    }else {
                        sTemp = Integer.toString(set.repCount) + " " + sReps;
                    }
                }else
                    sTemp = Integer.toString(set.repCount) + " " + sReps;
                holder.left_text1.setText(sTemp);
                sTemp = String.format("%.1f",set.weightTotal) + " kg";
                holder.left_text2.setText(sTemp);
                holder.left_text1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener != null) {
                            Log.d(TAG, "image onclick " + set.activityName);
                            //onItemClickListener.onItemClick(view, item);
                        }
                    }
                });
            }else{
                if (set.exerciseName.length() > 0)
                    holder.right_text1.setText(set.exerciseName);
                else
                    holder.right_text1.setText(set.activityName);

                holder.right_text1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener != null) {
                            // Log.d(TAG, "text onclick " + Integer.toString(workoutArrayList.size()));
                            //onItemClickListener.onItemClick(view, item);
                        }
                    }
                });
                String sTemp = Utilities.getTimeDateString(set.start) + " ";
                sTemp += " " + TimeUnit.MILLISECONDS.toMinutes(set.duration) + " min" + TimeUnit.MILLISECONDS.toSeconds(set.duration) + " sec";
                holder.right_text2.setText(sTemp);
                sTemp = " ";
                holder.left_text1.setText(sTemp);
                Drawable img = ContextCompat.getDrawable(mContext, mRefTools.getFitnessActivityIconResById(set.activityID));

                holder.left_text2.setText(sTemp);
                holder.left_text1.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                holder.left_text1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener != null) {
                            Log.d(TAG, "image onclick " + set.activityName);
                            //onItemClickListener.onItemClick(view, item);
                        }
                    }
                });
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
        void onItemClick(View view, Workout viewModel);
    }
}
