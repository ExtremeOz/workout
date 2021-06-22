package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;

import java.util.List;

public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.SimpleViewHolder> {
    private static final String TAG = SimpleListAdapter.class.getSimpleName();
    private  OnItemClickListener mListener;

    class SimpleViewHolder extends RecyclerView.ViewHolder {
        private final TextView simpleItemView;

        private SimpleViewHolder(View itemView) {
            super(itemView);
            simpleItemView = itemView.findViewById(R.id.single_text);
        }
    }

    private final LayoutInflater mInflater;
    private List<String> mLists; // Cached copy of regions
    private int selectedPos = RecyclerView.NO_POSITION;
    private String mTargetId;

    public SimpleListAdapter(Context context, List<String> inList) {
        mInflater = LayoutInflater.from(context);
        if (inList != null)
            mLists = inList;
    }
    public void setTargetId(String targetId){
            mTargetId = targetId;
            notifyDataSetChanged();
    }
    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recycler_row_single_item, parent, false);
        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        if ((mLists == null) && (mLists.size() == 0)) return;
        final String current = mLists.get(position);
        holder.simpleItemView.setText(current);
        if ((mTargetId == null) || (mTargetId.length() == 0))
            holder.itemView.setSelected(selectedPos == position);
        else
            holder.itemView.setSelected(current.equals(mTargetId));
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(v, current);
                notifyItemChanged(selectedPos); // previous - clear it
                selectedPos = holder.getLayoutPosition();
                mTargetId = Constants.ATRACKIT_EMPTY;
                notifyItemChanged(selectedPos);
            }
        });
        holder.simpleItemView.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(view, current);
                notifyItemChanged(selectedPos); // previous - clear it
                selectedPos = holder.getLayoutPosition();
                mTargetId = Constants.ATRACKIT_EMPTY;
                notifyItemChanged(selectedPos);
            }
        });
    }
    public void setOnItemClickListener(OnItemClickListener listener){ this.mListener = listener;}

    public void setSimples(List<String> list){
        mLists = list;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mLists has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mLists != null)
            return mLists.size();
        else return 0;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, String item);
    }
}