package com.a_track_it.fitdata.adapter;

import androidx.wear.widget.WearableRecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.a_track_it.fitdata.R;

public class ATrackItListViewHolder extends WearableRecyclerView.ViewHolder{
    public final ImageView image;
    public final TextView text;
    public final LinearLayout container;

    public ATrackItListViewHolder(View itemView){
        super(itemView);
        image=(ImageView)itemView.findViewById(R.id.image);
        text=(TextView)itemView.findViewById(R.id.text);
        container=(LinearLayout)itemView.findViewById(R.id.row_container);
    }
}
