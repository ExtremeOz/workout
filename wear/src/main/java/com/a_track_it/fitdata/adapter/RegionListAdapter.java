package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.data_model.BodyRegion;

import java.util.List;

public class RegionListAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<RegionListAdapter.RegionViewHolder> {
    private static final String TAG = RegionListAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private List<BodyRegion> mRegions; // Cached copy of regions
    private  OnItemClickListener mListener;
    private int selectedPos = RecyclerView.NO_POSITION;
    private long mTargetId;
    private ImageView mImageView;
    class RegionViewHolder extends RecyclerView.ViewHolder {
        private final TextView regionItemView;
        private final ImageView regionImageView;

        private RegionViewHolder(View itemView) {
            super(itemView);
            regionItemView = (TextView)itemView.findViewById(R.id.regionName);
            regionImageView = (ImageView) itemView.findViewById(R.id.regionImage);
        }
    }
    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            notifyDataSetChanged();
        }else mTargetId = 0;
    }

    public RegionListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public RegionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new RegionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RegionViewHolder holder, int position) {
        if ((mRegions == null) && (mRegions.size() == 0)) return;
        final Resources res = mInflater.getContext().getResources();
        final BodyRegion current = mRegions.get(position);
        holder.regionItemView.setText(current.regionName);
        if (mTargetId == 0)
            holder.itemView.setSelected(selectedPos == position);
        else
            holder.itemView.setSelected(current._id == mTargetId);
        int vibrant = res.getIdentifier(current.imageName, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        Bitmap bitmap = BitmapFactory.decodeResource(res, vibrant);
        if (bitmap != null)
            holder.regionImageView.setImageBitmap(bitmap);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(v, current);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
        holder.regionImageView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(v, current);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
        holder.regionItemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(v, current);
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        });
    }
    public void setOnItemClickListener(OnItemClickListener listener){ this.mListener = listener;}
    public void setRegions(List<BodyRegion> regions){
        mRegions = regions;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mRegions has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mRegions != null)
            return mRegions.size();
        else return 0;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, BodyRegion viewModel);
    }
}
