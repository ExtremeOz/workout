package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.wear.widget.WearableRecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class SetsRepsWeightAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<SetsRepsWeightAdapter.SetRepsWeightListViewHolder>{

    public static final String TAG = "SetsRepsWeightAdapter";

    private ArrayList<String> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    private Context mContext;
    private int mAdapterType;
    private String mTargetId;
    private int selectedPos = WearableRecyclerView.NO_POSITION;

    public SetsRepsWeightAdapter(Context context, int adapterType) {
        this.mContext = context;
        this.mAdapterType = adapterType;
        if (adapterType == Constants.SELECTION_REPS){
            String[] setreps = context.getResources().getStringArray(R.array.sets_reps);
            this.items = new ArrayList<>(Arrays.asList(setreps));
        }
        if (adapterType == Constants.SELECTION_WEIGHT_KG) {
            String[] setreps = context.getResources().getStringArray(R.array.array_weights_kg);
            this.items = new ArrayList<>(Arrays.asList(setreps));
        }
        if (adapterType == Constants.SELECTION_WEIGHT_LBS) {
            String[] setreps = context.getResources().getStringArray(R.array.array_weights_lbs);
            this.items = new ArrayList<>(Arrays.asList(setreps));
        }
        if (adapterType == Constants.SELECTION_WEIGHT_BODYWEIGHT) {
            String[] setreps = context.getResources().getStringArray(R.array.array_weights_bodyweight);
            this.items = new ArrayList<>(Arrays.asList(setreps));
        }
        if (adapterType == Constants.SELECTION_TARGET_FIELD) {
            String[] fields = context.getResources().getStringArray(R.array.archery_division);
            this.items = new ArrayList<>(Arrays.asList(fields));
        }
        if (adapterType == Constants.SELECTION_TARGET_DISTANCE_TARGET) {
            String[] equipment = context.getResources().getStringArray(R.array.archery_distance_indoor);
            Arrays.sort(equipment, Collections.reverseOrder());
            this.items = new ArrayList<>(Arrays.asList(equipment));
        }
        if (adapterType == Constants.SELECTION_TARGET_DISTANCE_FIELD) {
            String[] equipment = context.getResources().getStringArray(R.array.archery_distance_field);
            this.items = new ArrayList<>(Arrays.asList(equipment));
        }
        if (adapterType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET) {
            String[] equipment = context.getResources().getStringArray(R.array.archery_target_size_indoor);
            this.items = new ArrayList<>(Arrays.asList(equipment));
        }
        if (adapterType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) {
            String[] equipment = context.getResources().getStringArray(R.array.archery_target_size_field);
            this.items = new ArrayList<>(Arrays.asList(equipment));
        }
        if (adapterType == Constants.SELECTION_TARGET_EQUIPMENT) {
            String[] equipment = context.getResources().getStringArray(R.array.archery_equipment);
            this.items = new ArrayList<>(Arrays.asList(equipment));
        }
        if (adapterType == Constants.SELECTION_TARGET_ENDS) {
            String[] ends = context.getResources().getStringArray(R.array.archery_ends_field);
            this.items = new ArrayList<>(Arrays.asList(ends));
        }
        if (adapterType == Constants.SELECTION_TARGET_SHOTS_PER_END) {
            String[] ends = context.getResources().getStringArray(R.array.archery_ends_field);
            this.items = new ArrayList<>(Arrays.asList(ends));
        }
        if (adapterType == Constants.SELECTION_TARGET_POSSIBLE_SCORE) {
            String[] ends = context.getResources().getStringArray(R.array.archery_ends_field);
            Arrays.sort(ends, Collections.reverseOrder());
            this.items = new ArrayList<>(Arrays.asList(ends));
        }
        if ((adapterType == Constants.SELECTION_REST_DURATION_SETTINGS) || (adapterType == Constants.SELECTION_REST_DURATION_GYM) || (adapterType == Constants.SELECTION_INCOMPLETE_DURATION)){
            String[] durations = context.getResources().getStringArray(R.array.array_rest_duration_gym);
        //    Arrays.sort(durations, Collections.reverseOrder());
            this.items = new ArrayList<>(Arrays.asList(durations));
        }
        if (adapterType == Constants.SELECTION_REST_DURATION_TARGET) {
            String[] durations = context.getResources().getStringArray(R.array.array_rest_duration_target);
      //      Arrays.sort(durations, Collections.reverseOrder());
            this.items = new ArrayList<>(Arrays.asList(durations));
        }
    }
    public class SetRepsWeightListViewHolder extends WearableRecyclerView.ViewHolder{
        public final TextView text;
        public final LinearLayout container;
        private int selectedPos = WearableRecyclerView.NO_POSITION;
        private String mTargetId;

        SetRepsWeightListViewHolder(View itemView){
            super(itemView);
            text=(TextView)itemView.findViewById(R.id.single_text);
            container=(LinearLayout)itemView.findViewById(R.id.row_single_container);
        }
    }
    public ArrayList<String> getItems(){
        return this.items;
    }
    public void setItems(ArrayList<String> itemsList){
        this.items = itemsList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setTargetId(String targetId){
        if (targetId.length() > 0){
            mTargetId = targetId;
            int pos = WearableRecyclerView.NO_POSITION;
            for(String item : items){
                pos++;
                if (mTargetId.equals(item)){
                    selectedPos = pos;
                    break;
                }
            }
            notifyDataSetChanged();
        }else mTargetId = "";
    }
    @Override
    public SetRepsWeightListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_gym_item, parent, false);
        return new SetRepsWeightListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SetRepsWeightListViewHolder holder, final int position) {
        final String item;  final int pos;
        item = items.get(position);
        pos = position;
        holder.text.setText(item);
        if ((mTargetId != null) && (mTargetId.length() > 0))
            holder.itemView.setSelected(item.equals(mTargetId));
        else
            holder.itemView.setSelected(selectedPos == position);
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                // Log.d(TAG, "text onclick " + Integer.toString(items.size()));
                onItemClickListener.onItemClick(view, item, pos);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = "";
                notifyItemChanged(selectedPos);
            }
        });
        holder.text.setOnClickListener(view -> {
            if (onItemClickListener != null) {
               // Log.d(TAG, "text onclick " + Integer.toString(items.size()));
                onItemClickListener.onItemClick(view, item, pos);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = "";
                notifyItemChanged(selectedPos);
            }
        });

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
        return  (items == null) ? 0 : items.size();
    }


    public interface OnItemClickListener {
        void onItemClick(View view, String value, int pos);
    }
}
