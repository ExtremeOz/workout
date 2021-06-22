package com.a_track_it.fitdata.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.ObjectAggregate;
import com.a_track_it.fitdata.common.data_model.Workout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ObjectAggregateDetailAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "ObjectAggregateDetailAdapter";
    private ArrayList<Exercise> listExercise =  new ArrayList<>();
    private ArrayList<Bodypart> listBodypart =  new ArrayList<>();
    private ArrayList<Workout> listWorkout =  new ArrayList<>();
    private Map<Long, ObjectAggregate> items = new HashMap<>();
    private OnItemClickListener onItemClickListener;
    private boolean bUseKG;
    private int mObjectType;
    private Context mContext;
    private int selectedPos = RecyclerView.NO_POSITION;
    private long mTargetId;
    private List<String> resistanceTypeList = new ArrayList<>();


    public ObjectAggregateDetailAdapter(Context context,int objectType, boolean useKG) {
        mContext = context; mObjectType = objectType; bUseKG = useKG;
        int i = 0;
        while (i < 7){
            String sName = Constants.ATRACKIT_EMPTY;
            switch (i){
                case 0:
                    sName ="ic_question_mark_white";
                    break;
                case 1:
                    sName ="ic_action_barbell_vector_dark";
                    break;
                case 2:
                    sName ="ic_cable_crossover";
                    break;
                case 3:
                    sName ="ic_dumbbell_pair";
                    break;
                case 4:
                    sName ="ic_kettlebell_white";
                    break;
                case 5:
                    sName ="ic_gym_equipmemt";
                    break;
                case 6:
                    sName ="ic_standing_up_man_white";
                    break;
            }
            resistanceTypeList.add(sName);
            i++;
        }
    }
    public int getObjectType(){
        return mObjectType;
    }

    class ExerciseStatsViewHolder extends RecyclerView.ViewHolder {
        final ImageView objectImage;
        final TextView objectName;
        final TextView labelLastTrained;
        final TextView lastTrained;
        final TextView labelLastSets;
        final TextView lastSets;
        final TextView labelLastReps;
        final TextView lastReps;
        final TextView LastAvgWeight;
        final TextView labelLastAvgWeight;
        final TextView LastWatts;
        final TextView labelMaxWeight;
        final TextView maxWeight;
        final TextView lastMaxWeight;
        final TextView maxWatts;
        final TextView labelMaxWatts;
        final TextView lastMaxWatts;
        final TextView typeName;
        final TextView BP1Name;
        final TextView BP2Name;
        final TextView BP3Name;
        final TextView BP4Name;
        final TextView sessionCount;
        final TextView labelMins;
        final TextView setMin;
        final TextView labelMax;
        final TextView setMax;
        final TextView labelTotal;
        final TextView setTotal;
        final TextView labelSets;
        final TextView labelReps;
        final TextView labelWeight;
        final TextView repMin;
        final TextView repMax;
        final TextView repTotal;
        final ImageView objectImage2;
        final TextView weightMin;
        final TextView weightMax;
        final TextView weightTotal;


        private ExerciseStatsViewHolder(View itemView) {
            super(itemView);
            objectImage = itemView.findViewById(R.id.objectImage);
            objectName = itemView.findViewById(R.id.objectNameTextView);
            objectImage2 = itemView.findViewById(R.id.objectImage2);
            lastTrained = itemView.findViewById(R.id.LastTrained);
            labelLastTrained = itemView.findViewById(R.id.labelLastTrained);
            lastSets = itemView.findViewById(R.id.LastSets);
            labelLastSets = itemView.findViewById(R.id.labelLastSets);
            lastReps = itemView.findViewById(R.id.LastReps);
            labelLastReps = itemView.findViewById(R.id.labelLastReps);
            LastAvgWeight = itemView.findViewById(R.id.LastAvgWeight);
            labelLastAvgWeight = itemView.findViewById(R.id.labelLastAvgWeight);
            LastWatts = itemView.findViewById(R.id.LastWatts);
            labelMaxWeight = itemView.findViewById(R.id.labelMaxWeight);
            maxWeight = itemView.findViewById(R.id.maxWeight);
            lastMaxWeight = itemView.findViewById(R.id.lastMaxWeight);
            maxWatts = itemView.findViewById(R.id.maxWatts);
            labelMaxWatts = itemView.findViewById(R.id.labelMaxWatts);
            lastMaxWatts = itemView.findViewById(R.id.lastMaxWatts);
            typeName = itemView.findViewById(R.id.typeName);
            BP1Name = itemView.findViewById(R.id.BP1Name);
            BP2Name = itemView.findViewById(R.id.BP2Name);
            BP3Name = itemView.findViewById(R.id.BP3Name);
            BP4Name = itemView.findViewById(R.id.BP4Name);
            sessionCount = itemView.findViewById(R.id.sessionCount);
            labelMins = itemView.findViewById(R.id.labelMins);
            setMin = itemView.findViewById(R.id.setMin);
            labelMax = itemView.findViewById(R.id.labelMax);
            setMax = itemView.findViewById(R.id.setMax);
            labelTotal = itemView.findViewById(R.id.labelTotal);
            setTotal = itemView.findViewById(R.id.setTotal);
            labelSets = itemView.findViewById(R.id.labelSets);
            labelReps = itemView.findViewById(R.id.labelReps);
            labelWeight = itemView.findViewById(R.id.labelWeights);
            repMin = itemView.findViewById(R.id.repMin);
            repMax = itemView.findViewById(R.id.repMax);
            repTotal = itemView.findViewById(R.id.repTotal);
            weightMin = itemView.findViewById(R.id.weightMin);
            weightMax = itemView.findViewById(R.id.weightMax);
            weightTotal = itemView.findViewById(R.id.weightTotal);
        }
    }
    class BodypartStatsViewHolder extends RecyclerView.ViewHolder {
        final ImageView objectImage;
        final TextView objectName;
        final TextView objectFullNameTextView;
        final TextView labelLastTrained;
        final TextView lastTrained;
        final TextView labelLastSets;
        final TextView lastSets;
        final TextView lastReps;
        final TextView labelLastReps;
        final TextView LastAvgWeight;
        final TextView labelLastAvgWeight;
        final TextView LastWatts;
        final TextView maxWeight;
        final TextView lastMaxWeight;
        final TextView labelMaxWeight;
        final TextView maxWatts;
        final TextView labelMaxWatts;
        final TextView lastMaxWatts;
        final TextView regionName;
        final TextView labelBodyparts;
        final TextView Child1Name;
        final TextView Child2Name;
        final TextView Child3Name;
        final TextView Child4Name;
        final TextView Child5Name;
        final TextView exerciseCount;
        final TextView sessionCount;
        final TextView setMin;
        final TextView setMax;
        final TextView setTotal;
        final TextView repMin;
        final TextView repMax;
        final TextView repTotal;
        final ImageView objectImage2;
        final TextView weightMin;
        final TextView weightMax;
        final TextView weightTotal;

        private BodypartStatsViewHolder(View itemView) {
            super(itemView);
            objectImage = itemView.findViewById(R.id.objectImage);
            objectName = itemView.findViewById(R.id.objectNameTextView);
            objectFullNameTextView = itemView.findViewById(R.id.objectFullNameTextView);
            objectImage2 = itemView.findViewById(R.id.objectImage2);
            lastTrained = itemView.findViewById(R.id.LastTrained);
            labelLastTrained = itemView.findViewById(R.id.labelLastTrained);
            labelLastSets = itemView.findViewById(R.id.labelLastSets);
            lastSets = itemView.findViewById(R.id.LastSets);
            lastReps = itemView.findViewById(R.id.LastReps);
            labelLastReps = itemView.findViewById(R.id.labelLastReps);
            LastAvgWeight = itemView.findViewById(R.id.LastAvgWeight);
            labelLastAvgWeight = itemView.findViewById(R.id.labelLastAvgWeight);
            LastWatts = itemView.findViewById(R.id.LastWatts);
            maxWeight = itemView.findViewById(R.id.maxWeight);
            lastMaxWeight = itemView.findViewById(R.id.lastMaxWeight);
            labelMaxWeight = itemView.findViewById(R.id.labelMaxWeight);
            maxWatts = itemView.findViewById(R.id.maxWatts);
            labelMaxWatts = itemView.findViewById(R.id.labelMaxWatts);
            lastMaxWatts = itemView.findViewById(R.id.lastMaxWatts);
            labelBodyparts = itemView.findViewById(R.id.labelBodyparts);
            regionName = itemView.findViewById(R.id.regionName);
            Child1Name = itemView.findViewById(R.id.Child1Name);
            Child2Name = itemView.findViewById(R.id.Child2Name);
            Child3Name = itemView.findViewById(R.id.Child3Name);
            Child4Name = itemView.findViewById(R.id.Child4Name);
            Child5Name = itemView.findViewById(R.id.Child5Name);
            setMin = itemView.findViewById(R.id.setMin);
            setMax = itemView.findViewById(R.id.setMax);
            setTotal = itemView.findViewById(R.id.setTotal);
            repMin = itemView.findViewById(R.id.repMin);
            repMax = itemView.findViewById(R.id.repMax);
            repTotal = itemView.findViewById(R.id.repTotal);
            exerciseCount = itemView.findViewById(R.id.exerciseCount);
            sessionCount = itemView.findViewById(R.id.sessionCount);
            weightMin = itemView.findViewById(R.id.weightMin);
            weightMax = itemView.findViewById(R.id.weightMax);
            weightTotal = itemView.findViewById(R.id.weightTotal);            
        }
    }

    public void setItems(ArrayList<ObjectAggregate> itemsList){
        this.items.clear();
        for (ObjectAggregate a : itemsList)
            items.put(a.objectID, a);
    }
    public void setExerciseItems(ArrayList<Exercise> exerciseList){
        listExercise = exerciseList;
        notifyDataSetChanged();
    }
    public void sortItems(long iPos){
        if (mObjectType == Constants.SELECTION_EXERCISE_AGG) {
            if (iPos == 1)
                listExercise.sort((o1, o2) -> ((o1.lastTrained < o2.lastTrained) ? -1 : ((o1.lastTrained > o2.lastTrained) ? 1 : 0)));
            if (iPos == 2)
                listExercise.sort((o1, o2) -> ((o1.lastSets < o2.lastSets) ? -1 : ((o1.lastSets > o2.lastSets) ? 1 : 0)));
            if (iPos == 3)
                listExercise.sort((o1, o2) -> ((o1.lastReps < o2.lastReps) ? -1 : ((o1.lastReps > o2.lastReps) ? 1 : 0)));
            if (iPos == 4)
                listExercise.sort((o1, o2) -> ((o1.lastAvgWeight < o2.lastAvgWeight) ? -1 : ((o1.lastAvgWeight > o2.lastAvgWeight) ? 1 : 0)));
        }else{
            //                 listBodypart.sort((o1, o2) -> (Long.compare(o1.lastTrained, o2.lastTrained)));
            if (iPos == 1)
                Collections.sort(listBodypart, new Comparator<Bodypart>() {
                    @Override
                    public int compare(Bodypart o1, Bodypart o2) {
                        return Long.compare(o1.lastTrained, o2.lastTrained);
                    }
                });
            if (iPos == 2)
                listBodypart.sort((o1, o2) -> (Integer.compare(o1.lastSets, o2.lastSets)));
            if (iPos == 3)
                listBodypart.sort((o1, o2) -> (Integer.compare(o1.lastReps, o2.lastReps)));
            if (iPos == 4)
                listBodypart.sort((o1, o2) -> (Float.compare(o1.lastWeight, o2.lastWeight)));
        }
        notifyDataSetChanged();
    }
    public void setBodypartItems(ArrayList<Bodypart> itemsList, Long regionId){
        this.listBodypart.clear();
        if ((regionId != null) && (regionId > 0)) {
            if (regionId > 5){  // push pull
                if (regionId == 6){
                    for (Bodypart bp : itemsList) {
                        if (bp.flagPushPull > 0)
                            if (!this.listBodypart.contains(bp)) this.listBodypart.add(bp);
                    }
                }
                if (regionId == 7){
                    for (Bodypart bp : itemsList) {
                        if (bp.flagPushPull < 0)
                            if (!this.listBodypart.contains(bp)) this.listBodypart.add(bp);
                    }
                }
                return;
            }else
                for (Bodypart bp : itemsList) {
                    if (bp.regionID == regionId)
                        if (!this.listBodypart.contains(bp)){
                            this.listBodypart.add(bp);
                        }
                }
        }else{
            listBodypart = new ArrayList<>(itemsList);
        }
        notifyDataSetChanged();
    }

    public void setWorkoutItems(ArrayList<Workout> workoutList){
        listWorkout = workoutList;
        notifyDataSetChanged();
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        int rows = 0;
        if (mObjectType == Constants.OBJECT_TYPE_EXERCISE)
            rows = (listExercise == null) ? 0 : listExercise.size();
        if (mObjectType == Constants.OBJECT_TYPE_BODYPART)
            rows = (listBodypart == null) ? 0 : listBodypart.size();
        if (mObjectType == Constants.OBJECT_TYPE_WORKOUT)
            rows =  (listWorkout == null) ? 0 : listWorkout.size();
        return rows;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (mObjectType == Constants.OBJECT_TYPE_EXERCISE){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_exercise_stats, parent, false);
            return new ExerciseStatsViewHolder(v);
        }
        if (mObjectType == Constants.OBJECT_TYPE_BODYPART){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_bodypart_stats, parent, false);
            return new BodypartStatsViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Resources res = mContext.getResources();
        Bodypart bodypart = new Bodypart();
        Exercise exercise = new Exercise();
        Workout workout = new Workout();
        ObjectAggregate objectAggregate = new ObjectAggregate();
        if (mObjectType == Constants.OBJECT_TYPE_BODYPART) {
            bodypart = listBodypart.get(position);
            objectAggregate = items.get(bodypart._id);
        }
        if (mObjectType == Constants.OBJECT_TYPE_EXERCISE) {
            exercise = listExercise.get(position);
            objectAggregate = items.get(exercise._id);
        }
        if (mObjectType == Constants.OBJECT_TYPE_WORKOUT) {
            workout = listWorkout.get(position);
            objectAggregate = items.get(workout._id);
        }
        final Bodypart bpart = bodypart;
        final Exercise exer = exercise;
        final Workout work = workout;
        final ObjectAggregate item = objectAggregate;
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm", Locale.US);
        final View.OnClickListener myClicker = v -> {
            if (onItemClickListener != null) {
                mTargetId = 0;
                if (mObjectType == Constants.OBJECT_TYPE_BODYPART)
                    onItemClickListener.onItemClick(v, bpart);
                if (mObjectType == Constants.OBJECT_TYPE_EXERCISE)
                    onItemClickListener.onItemClick(v, exer);
                if (mObjectType == Constants.OBJECT_TYPE_WORKOUT)
                    onItemClickListener.onItemClick(v, work);
            }
        };
        String sUnit = bUseKG ? mContext.getString(R.string.label_weight_units_kg) : mContext.getString(R.string.label_weight_units_lbs);
        if (mTargetId == 0)
            holder.itemView.setSelected(selectedPos == position);
        else
            holder.itemView.setSelected(item._id == mTargetId);

        if (mObjectType == Constants.OBJECT_TYPE_EXERCISE) {
            ExerciseStatsViewHolder exerciseStatsViewHolder = (ExerciseStatsViewHolder) holder;
            if (exercise._id == 0) return;
            exerciseStatsViewHolder.objectImage.setOnClickListener(myClicker);
            exerciseStatsViewHolder.objectName.setText(exercise.name);
            exerciseStatsViewHolder.objectName.setOnClickListener(myClicker);
            if ((exercise.lastTrained > 0) && (exercise.lastReps > 0)) {
                exerciseStatsViewHolder.lastTrained.setText(simpleDateFormat.format(exercise.lastTrained));
                String sTemp = Constants.ATRACKIT_EMPTY + exercise.lastSets;
                exerciseStatsViewHolder.lastSets.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY + exercise.lastReps;
                exerciseStatsViewHolder.lastReps.setText(sTemp);
            }else{
                exerciseStatsViewHolder.labelLastTrained.setVisibility(View.GONE);
                exerciseStatsViewHolder.lastTrained.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelLastSets.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelLastReps.setVisibility(View.GONE);
                exerciseStatsViewHolder.lastSets.setVisibility(View.GONE);
                exerciseStatsViewHolder.lastReps.setVisibility(View.GONE);
            }
            String weight;
            String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
            float tempWeight = 0f;
            double intWeight = 0d;
            if ((exercise.lastAvgWeight > 0) && (exercise.lastAvgWatts > 0)) {
                tempWeight = ((bUseKG) ? exercise.lastAvgWeight : Utilities.KgToPoundsDisplay(exercise.lastAvgWeight));
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += tail;
                exerciseStatsViewHolder.LastAvgWeight.setText(weight);

                tempWeight = exercise.lastAvgWatts;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += Constants.KJ_TAIL;
                exerciseStatsViewHolder.LastWatts.setText(weight);
            }else{
                exerciseStatsViewHolder.LastWatts.setText(Constants.ATRACKIT_EMPTY);
                exerciseStatsViewHolder.LastAvgWeight.setText(Constants.ATRACKIT_EMPTY);
                exerciseStatsViewHolder.labelLastAvgWeight.setVisibility(View.GONE);
                exerciseStatsViewHolder.LastAvgWeight.setVisibility(View.GONE);
                exerciseStatsViewHolder.LastWatts.setVisibility(View.GONE);
            }
            if (exercise.maxWeight > 0) {
                tempWeight = exercise.maxWeight;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += tail;
                exerciseStatsViewHolder.maxWeight.setText(weight);
                weight = Constants.AT_HEAD + simpleDateFormat.format(exercise.lastMaxWeight);
                exerciseStatsViewHolder.lastMaxWeight.setText(weight);
            }
            if (exercise.totalWatts > 0){
                    tempWeight = exercise.totalWatts;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += Constants.KJ_TAIL;
                exerciseStatsViewHolder.maxWatts.setText(weight);
                weight = Constants.AT_HEAD + simpleDateFormat.format(exercise.lastTotalWatts);
                exerciseStatsViewHolder.lastMaxWatts.setText(weight);
            }
            String sTemp = null;
            if (exercise.sessionCount == 0){
                sTemp = Constants.ATRACKIT_EMPTY + exercise.sessionCount;
                exerciseStatsViewHolder.sessionCount.setText(sTemp);
                exerciseStatsViewHolder.labelMins.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelMax.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelTotal.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelReps.setVisibility(View.GONE);
                exerciseStatsViewHolder.repMin.setVisibility(View.GONE);
                exerciseStatsViewHolder.repMax.setVisibility(View.GONE);
                exerciseStatsViewHolder.repTotal.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelSets.setVisibility(View.GONE);
                exerciseStatsViewHolder.setMin.setVisibility(View.GONE);
                exerciseStatsViewHolder.setMax.setVisibility(View.GONE);
                exerciseStatsViewHolder.setTotal.setVisibility(View.GONE);
                exerciseStatsViewHolder.labelWeight.setVisibility(View.GONE);
                exerciseStatsViewHolder.weightMin.setVisibility(View.GONE);
                exerciseStatsViewHolder.weightMax.setVisibility(View.GONE);
                exerciseStatsViewHolder.weightTotal.setVisibility(View.GONE);
            }
            else
            if (item == null || item._id == 0) {
                sTemp = Constants.ATRACKIT_EMPTY + exercise.sessionCount;
                exerciseStatsViewHolder.sessionCount.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.setMin.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.setMax.setText(sTemp);
                if (exercise.minReps > 0) sTemp = Constants.ATRACKIT_EMPTY + exercise.minReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repMin.setText(sTemp);
                if (exercise.maxReps > 0) sTemp = Constants.ATRACKIT_EMPTY + exercise.maxReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repMax.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repTotal.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightMin.setText(sTemp);
                if (exercise.maxWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, exercise.maxWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightMax.setText(sTemp);
                if (exercise.lastAvgWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, exercise.lastAvgWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightTotal.setText(sTemp);
            }else {
                sTemp = Constants.ATRACKIT_EMPTY + item.countSessions;
                exerciseStatsViewHolder.sessionCount.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.setMin.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.setMax.setText(sTemp);
                if (item.minReps > 0) sTemp = Constants.ATRACKIT_EMPTY + item.minReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repMin.setText(sTemp);
                if (item.maxReps > 0)sTemp = Constants.ATRACKIT_EMPTY + item.maxReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repMax.setText(sTemp);
                if (item.avgReps > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.avgReps);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.repTotal.setText(sTemp);
                if (item.minWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.minWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightMin.setText(sTemp);
                if (item.maxWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.maxWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightMax.setText(sTemp);
                if (item.avgWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.avgWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                exerciseStatsViewHolder.weightTotal.setText(sTemp);
            }
            exerciseStatsViewHolder.typeName.setText(exercise.resistanceTypeName);
            if ((exercise.first_BPName != null) && (exercise.first_BPName.length() > 0)) exerciseStatsViewHolder.BP1Name.setText(exercise.first_BPName);
            if ((exercise.second_BPName != null) && (exercise.second_BPName.length() > 0)) exerciseStatsViewHolder.BP2Name.setText(exercise.second_BPName);
            if ((exercise.third_BPName != null) && (exercise.third_BPName.length() > 0)) exerciseStatsViewHolder.BP3Name.setText(exercise.third_BPName);
            if ((exercise.fourth_BPName != null) && (exercise.fourth_BPName.length() > 0)) exerciseStatsViewHolder.BP4Name.setText(exercise.fourth_BPName);
            String sImage = (exercise.resistanceType != null) ? resistanceTypeList.get(Math.toIntExact(exercise.resistanceType)) : "ic_action_barbell_vector_dark";
            int resImage = res.getIdentifier(sImage, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
            Drawable drawable = AppCompatResources.getDrawable(mContext, resImage);
           // Bitmap bitmap = BitmapFactory.decodeResource(res, resImage);
            if (drawable != null) {
                exerciseStatsViewHolder.objectImage.setImageDrawable(drawable);
                exerciseStatsViewHolder.objectImage2.setImageDrawable(drawable);
            }else {
                //exerciseStatsViewHolder.objectImage.setVisibility(View.GONE);
                exerciseStatsViewHolder.objectImage2.setVisibility(View.GONE);
            }
        }
        if (mObjectType == Constants.OBJECT_TYPE_BODYPART){
            if (bodypart._id == 0) return;
            BodypartStatsViewHolder bodypartStatsViewHolder = (BodypartStatsViewHolder)holder;
            bodypartStatsViewHolder.objectImage.setOnClickListener(myClicker);
            bodypartStatsViewHolder.objectName.setText(bodypart.shortName);
            int resImage = res.getIdentifier(bodypart.imageName, Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
            Bitmap bitmap = BitmapFactory.decodeResource(res, resImage);
            if (bitmap != null)
                bodypartStatsViewHolder.objectImage2.setImageBitmap(bitmap);
            bodypartStatsViewHolder.objectFullNameTextView.setText(bodypart.fullName);
            if ((bodypart.lastTrained > 0) && (bodypart.lastReps > 0)) {
                bodypartStatsViewHolder.lastTrained.setText(simpleDateFormat.format(bodypart.lastTrained));
                String sTemp = Constants.ATRACKIT_EMPTY + bodypart.lastSets;
                bodypartStatsViewHolder.lastSets.setText(sTemp);
                sTemp = Constants.ATRACKIT_EMPTY + bodypart.lastReps;
                bodypartStatsViewHolder.lastReps.setText(sTemp);
            }else{
                String sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.labelLastTrained.setVisibility(View.GONE);
                bodypartStatsViewHolder.lastTrained.setVisibility(View.GONE);
                bodypartStatsViewHolder.labelLastSets.setVisibility(View.GONE);
                bodypartStatsViewHolder.labelLastReps.setVisibility(View.GONE);
                bodypartStatsViewHolder.lastSets.setVisibility(View.GONE);
                bodypartStatsViewHolder.lastReps.setVisibility(View.GONE);
            }
            String weight;
            String tail = (bUseKG) ? Constants.KG_TAIL : Constants.LBS_TAIL;
            float tempWeight = 0f; double intWeight = 0d;
            if ((bodypart.lastWeight > 0) && (bodypart.lastReps > 0) && (bodypart.lastAvgWatts > 0)) {
                tempWeight = ((bUseKG) ? bodypart.lastWeight : Utilities.KgToPoundsDisplay(bodypart.lastWeight));
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += tail;
                bodypartStatsViewHolder.LastAvgWeight.setText(weight);
                tempWeight = bodypart.lastAvgWatts;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += Constants.KJ_TAIL;
                bodypartStatsViewHolder.LastWatts.setText(weight);
            }else {
                bodypartStatsViewHolder.LastWatts.setText(Constants.ATRACKIT_EMPTY);
                bodypartStatsViewHolder.LastAvgWeight.setText(Constants.ATRACKIT_EMPTY);
                bodypartStatsViewHolder.labelLastAvgWeight.setVisibility(View.GONE);
            }
            if ((bodypart.maxWeight > 0)  && (bodypart.maxWatts > 0) && (bodypart.lastReps > 0)){
                tempWeight = bodypart.maxWeight;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += tail;
                bodypartStatsViewHolder.maxWeight.setText(weight);
                weight = Constants.AT_HEAD + simpleDateFormat.format(bodypart.lastMaxWeight);
                bodypartStatsViewHolder.lastMaxWeight.setText(weight);
                tempWeight = bodypart.maxWatts;
                intWeight = Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    weight = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    weight = new DecimalFormat("#").format(intWeight);
                weight += Constants.KJ_TAIL;
                bodypartStatsViewHolder.maxWatts.setText(weight);
                weight = Constants.AT_HEAD + simpleDateFormat.format(bodypart.lastTotalWatts);
                bodypartStatsViewHolder.lastMaxWatts.setText(weight);
            }else{
                bodypartStatsViewHolder.labelMaxWeight.setVisibility(View.GONE);
                bodypartStatsViewHolder.maxWeight.setVisibility(View.GONE);
                bodypartStatsViewHolder.lastMaxWeight.setVisibility(View.GONE);
                bodypartStatsViewHolder.labelMaxWatts.setVisibility(View.GONE);
                bodypartStatsViewHolder.maxWatts.setVisibility(View.GONE);
                bodypartStatsViewHolder.lastMaxWatts.setVisibility(View.GONE);
            }
            bodypartStatsViewHolder.regionName.setText(bodypart.regionName);
            if ((bodypart.parentName == null) || (bodypart.parentName.length() == 0)){
                bodypartStatsViewHolder.labelBodyparts.setText(res.getString(R.string.label_region));
                int iCounter = 0;
                for (Bodypart bp : listBodypart){
                    if ((bp != null) && (bp.parentID != null) && (bp.parentID == bodypart._id)){
                        iCounter++;
                        switch (iCounter){
                            case 1:
                                bodypartStatsViewHolder.Child1Name.setText(bp.shortName);
                                break;
                            case 2:
                                bodypartStatsViewHolder.Child2Name.setText(bp.shortName);
                                break;
                            case 3:
                                bodypartStatsViewHolder.Child3Name.setText(bp.shortName);
                                break;
                            case 4:
                                bodypartStatsViewHolder.Child4Name.setText(bp.shortName);
                                break;
                            case 5:
                                bodypartStatsViewHolder.Child5Name.setText(bp.shortName);
                                break;
                        }
                    }
                }
            }else{
                bodypartStatsViewHolder.labelBodyparts.setText(res.getString(R.string.label_parent));
                bodypartStatsViewHolder.Child1Name.setText(bodypart.parentName);
                bodypartStatsViewHolder.Child2Name.setText(Constants.ATRACKIT_EMPTY);
                bodypartStatsViewHolder.Child3Name.setText(Constants.ATRACKIT_EMPTY);
                bodypartStatsViewHolder.Child4Name.setText(Constants.ATRACKIT_EMPTY);
                bodypartStatsViewHolder.Child5Name.setText(Constants.ATRACKIT_EMPTY);
            }
            String sTemp = Constants.ATRACKIT_EMPTY + bodypart.exerciseCount;
            bodypartStatsViewHolder.exerciseCount.setText(sTemp);
            if (item == null || item._id == 0) {
                sTemp = Constants.ATRACKIT_EMPTY + bodypart.sessionCount;
                bodypartStatsViewHolder.sessionCount.setText(sTemp);
                if (bodypart.setMin > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.setMin;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.setMin.setText(sTemp);
                if (bodypart.setMax > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.setMax;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.setMax.setText(sTemp);
                if (bodypart.repMin > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.repMin;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repMin.setText(sTemp);
                if (bodypart.repMax > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.repMax;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repMax.setText(sTemp);
                if (bodypart.avgReps > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, bodypart.avgReps);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repTotal.setText(sTemp);
                if (bodypart.lastWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, bodypart.lastWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightMin.setText(sTemp);
                if (bodypart.maxWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, bodypart.maxWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightMax.setText(sTemp);
                if (bodypart.avgWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, bodypart.avgWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightTotal.setText(sTemp);
            }else {
                sTemp = Constants.ATRACKIT_EMPTY + item.countSessions;
                bodypartStatsViewHolder.sessionCount.setText(sTemp);
                if (bodypart.setMin > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.setMin;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.setMin.setText(sTemp);
                if (bodypart.setMax > 0) sTemp = Constants.ATRACKIT_EMPTY + bodypart.setMax;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.setMax.setText(sTemp);
                if (item.minReps > 0) sTemp = Constants.ATRACKIT_EMPTY + item.minReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repMin.setText(sTemp);
                if (item.maxReps > 0) sTemp = Constants.ATRACKIT_EMPTY + item.maxReps;
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repMax.setText(sTemp);
                if (item.avgReps > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.avgReps);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.repTotal.setText(sTemp);
                if (item.minWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.minWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightMin.setText(sTemp);
                if (item.maxWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.maxWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightMax.setText(sTemp);
                if (item.avgWeight > 0) sTemp = Constants.ATRACKIT_EMPTY + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, item.avgWeight);
                else sTemp = Constants.ATRACKIT_EMPTY;
                bodypartStatsViewHolder.weightTotal.setText(sTemp);
            }
            if ((item != null) && item.countSets != null) {
                sTemp = Constants.ATRACKIT_EMPTY + Math.toIntExact(item.countSets);
                bodypartStatsViewHolder.setTotal.setText(sTemp);
            }else
                bodypartStatsViewHolder.setTotal.setVisibility(View.INVISIBLE);

        }

    }


    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            int pos = RecyclerView.NO_POSITION;
            if (mObjectType == Constants.OBJECT_TYPE_BODYPART)
                for(Bodypart item : listBodypart){
                    pos++;
                    if (item._id == mTargetId){
                        selectedPos = pos;
                        break;
                    }
                }
            if (mObjectType == Constants.OBJECT_TYPE_EXERCISE)
                for(Exercise item : listExercise){
                    pos++;
                    if (item._id == mTargetId){
                        selectedPos = pos;
                        break;
                    }
                }
            if (mObjectType == Constants.OBJECT_TYPE_WORKOUT)
                for(Workout item : listWorkout){
                    pos++;
                    if (item._id == mTargetId){
                        selectedPos = pos;
                        break;
                    }
                }

            notifyDataSetChanged();
        }else mTargetId = 0;
    }
    public int getSelectedPos(){ return selectedPos;}


    public interface OnItemClickListener {
        void onItemClick(View view, Object viewModel);
    }
}
