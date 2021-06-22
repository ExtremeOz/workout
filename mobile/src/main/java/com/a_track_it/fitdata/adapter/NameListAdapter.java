package com.a_track_it.fitdata.adapter;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutRepository;

import java.util.ArrayList;
import java.util.List;

public class NameListAdapter extends ArrayAdapter<String> {
    private List<String> dataList;
    private int itemLayout;
    private OnItemClickListener onItemClickListener;
    private WorkoutRepository mRepository;
    private int objectType;
    private String sUserID;
    private NameListAdapter.ListFilter listFilter = new NameListAdapter.ListFilter();

    public NameListAdapter(Context context, int resource, List<String> nameDataLst, int objType) {
        super(context, resource, nameDataLst);
        dataList = nameDataLst;
        mRepository = InjectorUtils.getWorkoutRepository((Application)context.getApplicationContext());
        itemLayout = resource;
        objectType = objType;
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setUserID(String sID){ sUserID = sID;}
    @Override
    public int getCount() {
        return (dataList != null) ? dataList.size(): 0;
    }

    @Override
    public String getItem(int position) {
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

        final String item = getItem(position);
        TextView strName = (TextView) view.findViewById(R.id.textViewAutoItem);
        strName.setText(item);
        strName.setOnClickListener(v -> onItemClickListener.onItemClick(v,item));
        view.setOnClickListener(v -> onItemClickListener.onItemClick(v,item));
        return view;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, String name);
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
                if (objectType == Constants.OBJECT_TYPE_EXERCISE) {
                    List<Exercise> matchValues =
                            mRepository.getExercisesLikeWorkoutExerciseName(searchStr);
                    List<String> matchNames = new ArrayList<String>();
                    for (Exercise ex : matchValues) {
                        matchNames.add(ex.workoutExercise);
                    }
                    results.values = matchNames;
                    results.count = matchNames.size();
                }
                if (objectType == Constants.OBJECT_TYPE_WORKOUT){
                    List<Workout> matchValues =
                            mRepository.getWorkoutByName(sUserID,searchStr);
                    List<String> matchNames = new ArrayList<String>();
                    for (Workout w : matchValues) {
                        if (w.name.length() > 0)
                            matchNames.add(w.name);
                    }
                    results.values = matchNames;
                    results.count = matchNames.size();

                }
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
                            if (o instanceof String)
                                dataList.add((String)o);
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