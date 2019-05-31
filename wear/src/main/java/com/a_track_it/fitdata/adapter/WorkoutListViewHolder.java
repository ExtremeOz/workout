package com.a_track_it.fitdata.adapter;


import androidx.wear.widget.WearableRecyclerView;
import android.view.View;
import android.widget.TextView;

import com.a_track_it.fitdata.R;

public class WorkoutListViewHolder extends WearableRecyclerView.ViewHolder{
    public final TextView left_text1;
    public final TextView left_text2;
    public final TextView right_text1;
    public final TextView right_text2;
    public final TextView right_text3;
    
    public WorkoutListViewHolder(View itemView) {
        super(itemView);
        left_text1 = itemView.findViewById(R.id.left_text1);
        left_text2 = itemView.findViewById(R.id.left_text2);
        right_text1 = itemView.findViewById(R.id.right_text1);
        right_text2 = itemView.findViewById(R.id.right_text2);
        right_text3 = itemView.findViewById(R.id.right_text3);
    }
}
