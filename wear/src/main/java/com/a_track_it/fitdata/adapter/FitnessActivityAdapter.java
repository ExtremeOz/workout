package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.a_track_it.fitdata.common.model.Utilities;

import java.util.ArrayList;


public class FitnessActivityAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<ATrackItListViewHolder>{

    public static final String TAG = "FitnessActivityAdapter";

    private ArrayList<FitnessActivity> items;
    private OnItemClickListener onItemClickListener;

    private Context mContext;
    private int mFilter;

    public FitnessActivityAdapter(Context context, int itemFilter) {
        this.mContext = context;
        this.mFilter = itemFilter;
        this.items = new ArrayList<>();
    }
    public ArrayList<FitnessActivity> getItems(){
        return this.items;
    }
    public void setItems(ArrayList<FitnessActivity> itemsList){
        
        if (mFilter == 0){
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
                        if ((activity._id == 9)||(activity._id == 21)||(activity._id == 117)||(activity._id == 25)||(activity._id == 39)||(activity._id == 103)||(activity._id == 118)
                                ||(activity._id == 49)||(activity._id == 54)||(activity._id == 77)||(activity._id == 78)||(activity._id == 88)||(activity._id == 7)||(activity._id == 93)
                                ||(activity._id == 94)||(activity._id == 95)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_GYM:
                        if ((activity._id == 80)||(activity._id == 22)||(activity._id == 97)||(activity._id == 41)||(activity._id == 47)||(activity._id == 113)||(activity._id == 114)||(activity._id == 115)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_RUN:
                        if ((activity._id == 13)||(activity._id == 2)||(activity._id == 8)||(activity._id == 56)||(activity._id == 57)||(activity._id == 58)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_WATER:
                        if ((activity._id == 102)||(activity._id == 40)||(activity._id == 69)||(activity._id == 48)||(activity._id == 53)||(activity._id == 59)||(activity._id == 60)
                                ||(activity._id == 79)||(activity._id == 81)||(activity._id == 82)||(activity._id == 83)||(activity._id == 84)||(activity._id == 92)||(activity._id == 96)
                                ||(activity._id == 99)){
                            items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_WINTER:
                        if ((activity._id == 106)||(activity._id == 35)||(activity._id == 104)||(activity._id == 50)||(activity._id == 52)||(activity._id == 70)||(activity._id == 61)
                                ||(activity._id == 62)||(activity._id == 63)||(activity._id == 64)||(activity._id == 65)||(activity._id == 66)||(activity._id == 67)
                                ||(activity._id == 68)||(activity._id == 71)||(activity._id == 73)||(activity._id == 74)||(activity._id == 75)||(activity._id == 89)||(activity._id == 90)||(activity._id == 91)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_SPORT:
                        if ((activity._id == 10)||(activity._id == 11)||(activity._id == 12)||(activity._id == 20)||(activity._id == 23)||(activity._id == 42)||(activity._id == 44)
                                ||(activity._id == 46) ||(activity._id == 27)||(activity._id == 28)||(activity._id == 29)||(activity._id == 32)||(activity._id == 33)||(activity._id == 34)
                                ||(activity._id == 36)||(activity._id == 37)||(activity._id == 69)||(activity._id == 51)||(activity._id == 55)||(activity._id == 120)
                                ||(activity._id == 76)||(activity._id == 85)||(activity._id == 86)||(activity._id == 87)){
                            if (!items.contains(activity)) items.add(activity);
                        }
                        break;
                    case Constants.SELECTION_ACTIVITY_MISC:
                        if ((activity._id == 24)||(activity._id == 26)||(activity._id == 30)||(activity._id == 31)||(activity._id == 38)||(activity._id == 45)
                                ||(activity._id == 108)||(activity._id == 72)||(activity._id == 112)||(activity._id == 110)||(activity._id == 109)
                                ||(activity._id == 111)||(activity._id == 116)||(activity._id == 98)||(activity._id == 100)||(activity._id == 101)){
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
        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, item);
                }
            }
        });
        holder.image.setImageResource(item.resource_id);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, items.get(position));
                }
            }
        });
        int vibrant = ContextCompat.getColor(mContext, item.color);
        Drawable dark_bg =  ContextCompat.getDrawable(mContext, R.drawable.bg_color_green_dark);
        holder.image.setBackground(dark_bg);
        holder.image.setColorFilter(ContextCompat.getColor(mContext,android.R.color.white), PorterDuff.Mode.SRC_IN);
        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));

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
        void onItemClick(View view, FitnessActivity viewModel);
    }
}
