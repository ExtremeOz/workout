package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Workout;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by Chris Black
 */
public class WorkoutListViewAdapter extends CursorRecyclerViewAdapter<WorkoutListViewAdapter.ListViewHolder> implements View.OnClickListener{

    private OnItemClickListener onItemClickListener;
    private static ReferencesTools mRefTools;
    public WorkoutListViewAdapter(Context context, Cursor cursor){
        super(context, cursor);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_item, parent, false);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(parent.getContext());
        ListViewHolder vh = new ListViewHolder(itemView);
        vh.deleteButton.setOnClickListener(this);
        return vh;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, Cursor cursor) {
        Workout item = cupboard().withCursor(cursor).get(Workout.class);

        holder.text.setText(item.shortText());
        holder.deleteButton.setTag(item);
        if (item.packageName != null && item.packageName.equals("com.a_track_it.com.fitdata")) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.INVISIBLE);
        }
        holder.container.setBackgroundColor(mRefTools.getFitnessActivityColorById(item.activityID));
        int resid = mRefTools.getFitnessActivityIconResById(item.activityID);
        holder.image.setImageResource(resid);
    }

    @Override
    public void onClick(final View v) {

        if (onItemClickListener != null && v.getId() == R.id.close_button) {
            onItemClickListener.onItemClick(v, (Workout) v.getTag());
        }
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageView image;
        public ImageView deleteButton;
        public View container;

        public ListViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            image = (ImageView) itemView.findViewById(R.id.image);
            container = itemView.findViewById(R.id.container);
            deleteButton = (ImageView) itemView.findViewById(R.id.close_button);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Workout viewModel);
    }
}
