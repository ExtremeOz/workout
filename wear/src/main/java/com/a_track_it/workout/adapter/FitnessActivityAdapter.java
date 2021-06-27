package com.a_track_it.workout.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.FitnessActivity;

import java.util.ArrayList;


public class FitnessActivityAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<ATrackItListViewHolder>{

    public static final String TAG = "FitnessActivityAdapter";

    private ArrayList<FitnessActivity> items;
    private OnItemClickListener onItemClickListener;

    private Context mContext;
    private int mFilter;
    private int selectedPos = RecyclerView.NO_POSITION;
    private long mTargetId;

    public FitnessActivityAdapter(Context context, int itemFilter) {
        this.mContext = context;
        this.mFilter = itemFilter;
        this.items = new ArrayList<>();
    }
    public ArrayList<FitnessActivity> getItems(){
        return this.items;
    }
    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            notifyDataSetChanged();
        }else mTargetId = 0;
    }
    public void setItems(ArrayList<FitnessActivity> itemsList){
        
        if ((mFilter == 0) || (mFilter == Constants.SELECTION_FITNESS_ACTIVITY)){
            this.items = itemsList;
        }else{
            for (FitnessActivity activity : itemsList){
                switch (mFilter){
                    case Constants.SELECTION_ACTIVITY_BIKE:
                        if ((activity._id == 1)||((activity._id >= 14)&&(activity._id <= 19))){
                               if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_CARDIO:
                        if (Utilities.isCardioWorkout(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_GYM:
                        if (Utilities.isGymWorkout(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_RUN:
                        if (Utilities.isRunning(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_WATER:
                        if (Utilities.isAquatic(activity._id)){
                            items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_WINTER:
                        if (Utilities.isWinterWorkout(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_SPORT:
                        if (Utilities.isSportWorkout(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_MISC:
                        if (Utilities.isMiscellaneousWorkout(activity._id)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                }
            }
        }
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ATrackItListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_item, parent, false);
        return new ATrackItListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ATrackItListViewHolder holder, final int position) {
        final FitnessActivity item;

        item = items.get(position);
        holder.text.setText(item.name);
        if (mTargetId == 0)
            holder.itemView.setSelected(selectedPos == position);
        else
            holder.itemView.setSelected(item._id == mTargetId);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, item);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
        holder.text.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, item);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
        holder.image.setImageResource(item.resource_id);
        holder.image.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, items.get(position));
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
        int vibrant = ContextCompat.getColor(mContext, item.color);
      //  Drawable dark_bg =  AppCompatResources.getDrawable(mContext, R.drawable.bg_color_dark);
     //   holder.image.setBackground(dark_bg);
        holder.image.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(vibrant, BlendModeCompat.SRC_IN));
     //   holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
    //    holder.text.setTag(item);

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

    public void clearList(){
        if (items != null)
            items.clear();
    }

    @Override
    public int getItemCount() {
        return  (items == null) ? 0 : items.size();
    }


    public interface OnItemClickListener {
        void onItemClick(View view, FitnessActivity viewModel);
    }
}
