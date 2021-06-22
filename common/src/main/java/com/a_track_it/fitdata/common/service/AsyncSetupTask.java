package com.a_track_it.fitdata.common.service;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.activity.IInitialActivityCallback;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.FitnessActivity;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.ArrayList;


public class AsyncSetupTask extends AsyncTask<Void, Void, java.lang.Integer> {
private static final String TAG = "AsyncSetupTask";


private static int totalFinal = 0;
private Context mContext;
private ArrayList<FitnessActivity> fitnessActivityArrayList = new ArrayList<>();
private ArrayList<Bodypart> bodypartArrayList = new ArrayList<>();
private ArrayList<Exercise> exerciseArrayList = new ArrayList<>();
private IInitialActivityCallback callback;

    public AsyncSetupTask(IInitialActivityCallback activityCallback, Context newContext){
        this.callback = activityCallback;
        this.mContext = newContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Gson gson = new Gson();
        Resources resources = mContext.getResources();
        ReferencesTools referencesTools = ReferencesTools.getInstance();
        referencesTools.init(mContext);
        String[] names = resources.getStringArray(R.array.activity_names_sorted);
        int[] ids = resources.getIntArray(R.array.activity_id_names_sorted);
        String[] icons = resources.getStringArray(R.array.activity_icon_name_sorted);
        String[] identifiers = resources.getStringArray(R.array.activity_ident_name_sorted);
        int i = 0;

        while (i < ids.length){
            FitnessActivity fa = new FitnessActivity();
            fa._id = ids[i];
            fa.name = names[i];
            fa.resource_id = mContext.getResources().getIdentifier(icons[i], Constants.ATRACKIT_DRAWABLE, mContext.getPackageName());
            fa.color = referencesTools.getFitnessActivityColorById(ids[i]);
            fa.identifier = identifiers[i];
            fitnessActivityArrayList.add(fa);
            i++;
        }
        SaveToLocalFile(Constants.ACTIVITY_FILENAME, gson.toJson(fitnessActivityArrayList));

        int[] bp_ids = resources.getIntArray(R.array.bodypart_ids);
        String[] bp_fullname = resources.getStringArray(R.array.bodypart_fullnames);
        String[] bp_shortname = resources.getStringArray(R.array.bodypart_shortnames);
        String[] bp_imagename = resources.getStringArray(R.array.bodypart_images);
        int[] bp_regionid = resources.getIntArray(R.array.bodypart_regionids);
        String[] bp_regionNames = resources.getStringArray(R.array.bodypart_regionnames);
        int[] bp_setmins = resources.getIntArray(R.array.bodypart_setmin);
        int[] bp_setmaxs = resources.getIntArray(R.array.bodypart_setmax);
        int[] bp_repmins = resources.getIntArray(R.array.bodypart_repmin);
        int[] bp_repmaxs = resources.getIntArray(R.array.bodypart_repmax);
        String[] bp_powerfactors = resources.getStringArray(R.array.bodypart_power_rating);
        int[] bp_parentids = resources.getIntArray(R.array.bodypart_parentids);
        String[] bp_parentNames = resources.getStringArray(R.array.bodypart_parentnames);
        i = 0;
        while (i < bp_ids.length){
            Bodypart bodypart = new Bodypart();
            bodypart._id = (long) bp_ids[i];
            bodypart.shortName = bp_shortname[i];
            bodypart.fullName = bp_fullname[i];
            bodypart.imageName = bp_imagename[i];
            bodypart.regionID = (long)bp_regionid[i];
            bodypart.regionName = bp_regionNames[i];
            bodypart.setMin = bp_setmins[i];
            bodypart.setMax = bp_setmaxs[i];
            bodypart.repMin = bp_repmins[i];
            bodypart.repMax = bp_repmaxs[i];
            if (bp_parentids[i] > 0)
                bodypart.parentID = (long)bp_parentids[i];
            bodypart.parentName = bp_parentNames[i];
            if ((bp_powerfactors[i] != null) && (bp_powerfactors[i].length() > 0)) bodypart.powerFactor = Float.parseFloat(bp_powerfactors[i]);

            bodypartArrayList.add(bodypart);
            i++;
        }
       // Log.d(TAG,"setup added " + Integer.toString(i) + " body parts");
        SaveToLocalFile(Constants.BODYPART_FILENAME, gson.toJson(bodypartArrayList));
        // setBodypartList(bodypartArrayList);
        totalFinal = i;
        i = 0;  // start again with exercises now!
        String ex_fullname[] = resources.getStringArray(R.array.exercise_name_list);
        String ex_othernames[] = resources.getStringArray(R.array.exercise_other_name_list);
        int[] ex_resistancetype = resources.getIntArray(R.array.exercise_resistance_type);
        int[] ex_bodypartcount = resources.getIntArray(R.array.exercise_bodypart_count);
        int[] ex_bodypart1 = resources.getIntArray(R.array.exercise_bodypart1_id);
        String[] ex_bp1_name = resources.getStringArray(R.array.exercise_bodypart1_name);
        int[] ex_bodypart2 = resources.getIntArray(R.array.exercise_bodypart2_id);
        String[] ex_bp2_name = resources.getStringArray(R.array.exercise_bodypart2_name);
        int[] ex_bodypart3 = resources.getIntArray(R.array.exercise_bodypart3_id);
        String[] ex_bp3_name = resources.getStringArray(R.array.exercise_bodypart3_name);
        int[] ex_bodypart4 = resources.getIntArray(R.array.exercise_bodypart4_id);
        String[] ex_bp4_name = resources.getStringArray(R.array.exercise_bodypart4_name);
        String[] ex_powerfactor = resources.getStringArray(R.array.exercise_power_factor);
        String[] ex_workoutexercises = resources.getStringArray(R.array.exercise_workout_exercise_list);
        while(i < ex_fullname.length){
            Exercise exercise = new Exercise();
            exercise._id = (i+1);
            exercise.name = ex_fullname[i];
            exercise.otherNames = ex_othernames[i];
            exercise.resistanceType = (long)ex_resistancetype[i];
            exercise.resistanceTypeName = Utilities.getResistanceType(ex_resistancetype[i]);
            exercise.bodypartCount = ex_bodypartcount[i];
            exercise.first_BPID = (long)ex_bodypart1[i];
            exercise.first_BPName = ex_bp1_name[i];
            exercise.second_BPID = (long)ex_bodypart2[i];
            exercise.second_BPName = ex_bp2_name[i];
            exercise.third_BPID = (long)ex_bodypart3[i];
            exercise.third_BPName = ex_bp3_name[i];
            exercise.fourth_BPID = (long)ex_bodypart4[i];
            exercise.fourth_BPName = ex_bp4_name[i];
            exercise.workoutExercise = ex_workoutexercises[i];
            if ((ex_powerfactor[i] != null) && (ex_powerfactor[i].length() > 0)) exercise.powerFactor = Float.parseFloat(ex_powerfactor[i]);
            exerciseArrayList.add(exercise);
            i++;

        }
        SaveToLocalFile(Constants.EXERCISE_FILENAME, gson.toJson(exerciseArrayList));
        callback.onSetupComplete();
        totalFinal += i;
        return Integer.valueOf(totalFinal);
    }

    private void SaveToLocalFile(String filename, String sJSON){
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(sJSON.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
