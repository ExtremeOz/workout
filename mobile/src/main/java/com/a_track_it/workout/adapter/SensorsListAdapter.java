package com.a_track_it.workout.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.a_track_it.workout.R;

import java.io.Serializable;
import java.util.ArrayList;

public class SensorsListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ATrackItListViewHolder>{
    public static final String TAG = "SensorsListAdapter";
    private ArrayList<SensorItem> sensorItems = new ArrayList<>();
    private Context mContext;
    private SensorsListAdapter.OnItemClickListener onItemClickListener;

    public final class SensorItem implements Serializable {
        private boolean bAvailable;
        private String sSensorText;
        private int iconResource;
        public SensorItem() {
        }

        public SensorItem(boolean set, String sText, int icon) {
            bAvailable = set;
            sSensorText = sText;
            iconResource = icon;
        }

        public boolean isAvailable() {
            return bAvailable;
        }
        public int getIcon(){ return iconResource;}
        public void setIcon(int i){ iconResource = i;}

        public String getText() {
            return sSensorText;
        }

        public void setAvail(boolean bSet) {
            bAvailable = bSet;
        }

        public void setText(String y) {
            sSensorText = y;
        }
    }

    public SensorsListAdapter(Context c){
        mContext = c;
    }
    public void setOnItemClickListener(SensorsListAdapter.OnItemClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
    }
    public void setItem(boolean bAvail, String sText, int icon){
        SensorItem item = new SensorItem(bAvail, sText, icon);
        sensorItems.add(item);
    }

    private void crossFadeIn(View contentView) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        // final int idView = contentView.getId();
        contentView.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                    }
                });

    }

    @NonNull
    @Override
    public ATrackItListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_item, parent, false);
        return new ATrackItListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ATrackItListViewHolder holder, int position) {
        final SensorItem sensorItem = sensorItems.get(position);
        if (sensorItem != null) {
            holder.text.setText(sensorItem.getText());

            if (sensorItem.getIcon() > 0) {
                Drawable d = AppCompatResources.getDrawable(mContext,sensorItem.getIcon());
                holder.text.setCompoundDrawablesWithIntrinsicBounds(null,null, d, null);
            }
            if (sensorItem.isAvailable())
                holder.image.setImageResource(R.drawable.ic_checkbox_white);
            else
                holder.image.setImageResource(R.drawable.ic_outline_cancel_white);

            holder.container.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v,sensorItem);
                }
            });
        }else{
            holder.image.setVisibility(View.INVISIBLE);
            holder.text.setText(sensorItem.getText());
        }
        crossFadeIn(holder.container);
    }

    @Override
    public int getItemCount() {
        return sensorItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, SensorItem viewModel);
    }

}
