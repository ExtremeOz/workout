package com.a_track_it.workout.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.wear.widget.WearableRecyclerView;

public class DayMonthAdapter extends androidx.wear.widget.WearableRecyclerView.Adapter<DayMonthAdapter.DayMonthListViewHolder>{
    public static final String TAG = "DayMonthAdapter";

    private ArrayList<String> items = new ArrayList<>();
    private DayMonthAdapter.OnItemClickListener onItemClickListener;

    private Context mContext;
    private int mAdapterType;

    public interface OnItemClickListener {
        void onItemClick(View view, String value, int pos);
    }
    public DayMonthAdapter(Context context, int adapterType) {
        this.mContext = context;
        this.mAdapterType = adapterType;
        if (adapterType == Constants.SELECTION_DAYS) {
            String[] days_month = context.getResources().getStringArray(R.array.days_of_month);
            this.items = new ArrayList<>(Arrays.asList(days_month));
        }
        if (adapterType == Constants.SELECTION_MONTHS) {
            String[] month_year = context.getResources().getStringArray(R.array.month_of_year);
            this.items = new ArrayList<>(Arrays.asList(month_year));
        }
    }
    public class DayMonthListViewHolder extends WearableRecyclerView.ViewHolder{
        public final TextView text;
        public final LinearLayout container;

        DayMonthListViewHolder(View itemView){
            super(itemView);
            text=(TextView)itemView.findViewById(R.id.single_text);
            container=(LinearLayout)itemView.findViewById(R.id.row_single_container);
        }
    }
    public ArrayList<String> getItems(){
        return this.items;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public DayMonthListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_single_item, viewGroup, false);
        return new DayMonthAdapter.DayMonthListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayMonthListViewHolder setDayMonthListViewHolder, int position) {
        final String item;  final int pos;
        item = items.get(position);
        pos = position;
        setDayMonthListViewHolder.text.setText(item);
        setDayMonthListViewHolder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    Log.d(TAG, "text onclick " + item);
                    onItemClickListener.onItemClick(view, item, pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
