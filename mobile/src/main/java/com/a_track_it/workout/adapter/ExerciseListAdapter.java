package com.a_track_it.workout.adapter;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.InjectorUtils;
import com.a_track_it.workout.common.data_model.Exercise;
import com.a_track_it.workout.common.data_model.WorkoutRepository;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListAdapter extends ArrayAdapter <Exercise> {
    private List<Exercise> dataList;
    private int itemLayout;
    private OnItemClickListener onItemClickListener;
    private WorkoutRepository mRepository;
    private ExerciseListAdapter.ListFilter listFilter = new ExerciseListAdapter.ListFilter();

    public ExerciseListAdapter(@NonNull Context context, int resource, @NonNull List<Exercise> objects) {
        super(context, resource, objects);
        mRepository = InjectorUtils.getWorkoutRepository((Application)context.getApplicationContext());
        itemLayout = resource;
        dataList = (List<Exercise>)objects;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getCount() {
        return (dataList != null) ? dataList.size(): 0;
    }

    @Override
    public Exercise getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            if (itemLayout > 0)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.autocomplete_dropdown_item, parent, false);
        }
        final Exercise item = getItem(position);
        TextView strName = (TextView) view.findViewById(R.id.textViewAutoItem);
        strName.setText(item.name);
        strName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v,item);

            }
        });
        return view;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Exercise viewModel);
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = new ArrayList<String>();
                    results.count = 0;
                }
            } else {
                final String searchStr = prefix.toString() + "%";
                //Call to database to get matching records using room
                List<Exercise> matchValues =
                        mRepository.getExercisesLikeName(searchStr);

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                dataList = new ArrayList<>();
                if (results.values != null) {
                    if (results.values instanceof List) {
                        for (Object o : (List)results.values){
                            if (o instanceof Exercise)
                                dataList.add((Exercise)o);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
