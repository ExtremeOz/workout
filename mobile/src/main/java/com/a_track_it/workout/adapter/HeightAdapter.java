package com.a_track_it.workout.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeightAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter <HeightAdapter.HeightViewHolder> {
    private final LayoutInflater mInflater;
    private List<String> listMetric = new ArrayList<>();
    private List<String> listImperical = new ArrayList<>();
    public static final String CM_TAIL = " cm";
    public static final String FT_TAIL = " ft";
    private static final int minHeight = 150;
    private static final int maxHeight = 221;
    private float tempFloat;
    private String tempString;
    private OnHeightClickListener mListener;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String mTargetMetric;

    class HeightViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final TextView heightMetricView;
        private final TextView heightImpericalView;

        private HeightViewHolder(View itemView) {
            super(itemView);
            heightMetricView = (TextView)itemView.findViewById(R.id.heightName1);
            heightImpericalView = (TextView) itemView.findViewById(R.id.heightName2);
        }
    }

    public HeightAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        for (int i= minHeight; i < maxHeight; i++){
            tempString = i + CM_TAIL;
            listMetric.add(tempString);
            tempFloat = (i * (Constants.METRE_TO_FEET/100));
            tempString = String.format(Locale.getDefault(), "%1$.2f", tempFloat) + FT_TAIL;
            listImperical.add(tempString);
        }
        setHasStableIds(true);

    }
    public void setTarget(String s){
        mTargetMetric = s + CM_TAIL;
        selectedPos = (Integer.parseInt(s) - minHeight);
        notifyDataSetChanged();
    }
    public List<String> getListMetric(){
        return listMetric;
    }

    public void setOnItemClickListener(OnHeightClickListener listener){ this.mListener = listener;}

    @NonNull
    @Override
    public HeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recycleritem_height, parent, false);
        return new HeightViewHolder(itemView);
    }

    @Override
    public long getItemId(int position) {
        return  (long)minHeight+position;
    }

    @Override
    public void onBindViewHolder(@NonNull HeightViewHolder holder, int position) {
        final String sImperical = listImperical.get(position);
        final String sMetric = listMetric.get(position);
        holder.heightMetricView.setText(sMetric);
        if ((mTargetMetric == null) || (mTargetMetric.length() == 0))
            holder.itemView.setSelected(position == selectedPos);
        else
            holder.itemView.setSelected(mTargetMetric.equals(sMetric));
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPos);
            selectedPos = holder.getLayoutPosition();
            mTargetMetric = Constants.ATRACKIT_EMPTY;
            notifyItemChanged(selectedPos);
            if (mListener != null)
                mListener.onItemClick(v, sMetric.replace(CM_TAIL,Constants.ATRACKIT_EMPTY));
        });

        holder.heightMetricView.setOnClickListener(v -> {
            notifyItemChanged(selectedPos);
            selectedPos = holder.getLayoutPosition();
            mTargetMetric = Constants.ATRACKIT_EMPTY;
            notifyItemChanged(selectedPos);
            if (mListener != null)
                mListener.onItemClick(v, sMetric.replace(CM_TAIL,Constants.ATRACKIT_EMPTY));
        });
        holder.heightImpericalView.setText(sImperical);
        holder.heightImpericalView.setOnClickListener(v -> {
            notifyItemChanged(selectedPos);
            selectedPos = holder.getLayoutPosition();
            mTargetMetric = Constants.ATRACKIT_EMPTY;
            notifyItemChanged(selectedPos);
            if (mListener != null)
                mListener.onItemClick(v, sMetric.replace(CM_TAIL,Constants.ATRACKIT_EMPTY));

        });
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPos);
            selectedPos = holder.getLayoutPosition();
            mTargetMetric = Constants.ATRACKIT_EMPTY;
            notifyItemChanged(selectedPos);
            if (mListener != null)
                mListener.onItemClick(v, sMetric.replace(CM_TAIL,Constants.ATRACKIT_EMPTY));
        });
    }

    @Override
    public int getItemCount() {
        return listImperical.size();
    }
    public interface OnHeightClickListener {
        void onItemClick(View view, String viewModel);
    }
}
