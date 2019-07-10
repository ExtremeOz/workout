package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.data_model.Bodypart;
import com.a_track_it.fitdata.user_model.Utilities;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;


public class BodypartAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<ATrackItListViewHolder>{

    public static final String TAG = "BodypartAdapter";

    private ArrayList<Bodypart> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    private Context mContext;


    public BodypartAdapter(Context context) {
        this.mContext = context;
    }

    public void setItems(ArrayList<Bodypart> itemsList){
        for (Bodypart bp : itemsList){
            if (!this.items.contains(bp)) this.items.add(bp);
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
        item = items.get(position);
        holder.text.setText(item.shortName);
        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    Log.d(TAG, "text onclick " + Integer.toString(items.size()));
                    onItemClickListener.onItemClick(view, item);
                }
            }
        });
        int vibrant = ContextCompat.getColor(mContext, R.color.colorAccent);
        //holder.container.setBackgroundColor(vibrant);
        holder.image.setColorFilter(ContextCompat.getColor(mContext,android.R.color.white), PorterDuff.Mode.SRC_IN);
        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    Log.d(TAG, "text onclick " + Integer.toString(items.size()));
                    onItemClickListener.onItemClick(view, item);
                }
            }
        });
        holder.text.setTag(item);

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
        return  (items == null) ? 0 : items.size();
    }


    public interface OnItemClickListener {
        void onItemClick(View view, Bodypart viewModel);
    }
}
