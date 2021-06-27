package com.a_track_it.workout.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.data_model.Bodypart;

import java.util.ArrayList;


public class BodypartAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<ATrackItListViewHolder>{
    public static final String TAG = "BodypartAdapter";

    private ArrayList<Bodypart> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    private Context mContext;
    private int selectedPos = androidx.wear.widget.WearableRecyclerView.NO_POSITION;
    private long mTargetId;
    public int getSelectedPos(){ return selectedPos;}
    public BodypartAdapter(Context context) {
        this.mContext = context;
    }
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
    public ArrayList<Bodypart> getItems(){
        return items;
    }

    public void setItems(ArrayList<Bodypart> itemsList, Long regionId){
        this.items.clear();
        if ((regionId != null) && (regionId > 0)) {
            if (regionId > 5){
                if (regionId == 6){
                    for (Bodypart bp : itemsList) {
                        if (bp.flagPushPull > 0)
                            if (!this.items.contains(bp)) this.items.add(bp);
                    }
                }
                if (regionId == 7){
                    for (Bodypart bp : itemsList) {
                        if (bp.flagPushPull < 0)
                            if (!this.items.contains(bp)) this.items.add(bp);
                    }
                }
                return;
            }else
                for (Bodypart bp : itemsList) {
                    if (bp.regionID == regionId)
                        if (!this.items.contains(bp)) this.items.add(bp);
                }
            for (Bodypart bp : itemsList) {
                if (!this.items.contains(bp)) this.items.add(bp);
            }
        }else{
            for (Bodypart bp : itemsList) {
                if (!this.items.contains(bp)) this.items.add(bp);
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
        final Bodypart item;
        final Resources res = mContext.getResources();
        item = items.get(position);
        if (mTargetId == 0)
            holder.itemView.setSelected(selectedPos == position);
        else
            holder.itemView.setSelected(item._id == mTargetId);
        holder.container.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
                onItemClickListener.onItemClick(v, item);
            }
        });
        holder.text.setText(item.shortName);
        holder.text.setOnClickListener(view -> {
            if (onItemClickListener != null) { ;
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
                onItemClickListener.onItemClick(view, item);
            }
        });
        int vibrant = res.getIdentifier(item.imageName, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        Bitmap bitmap = BitmapFactory.decodeResource(res, vibrant);
        if (bitmap != null)
            holder.image.setImageBitmap(bitmap);
        holder.image.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
                onItemClickListener.onItemClick(view, item);
            }
        });
     //   holder.text.setTag(item);

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
        void onItemClick(View view, Bodypart viewModel);
    }
}
