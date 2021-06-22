package com.a_track_it.fitdata.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Exercise;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a_track_it.com service on a_track_it.com separate handler thread.
 * <p>
 */
public class MySetupIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_SETUP
    private static final String TAG = "MySetupIntentService";
    public MySetupIntentService() {
        super("MySetupIntentService");
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a_track_it.com task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSetup(Context context) {
        Intent intent = new Intent(context, MySetupIntentService.class);
        intent.setAction(Constants.INTENT_SETUP);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.INTENT_SETUP.equals(action)) {
                handleActionSetup();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetup() {
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        int i = 0;
        // Handle action Setup
     //   if (!UserPreferences.getAppSetupCompleted(context)){
            Log.w(TAG,"setup started");
       //     SQLiteDatabase db = SimpleDBHelper.INSTANCE.open(context);
       //     if (db != null && db.isOpen()) {
                int[] bp_ids = resources.getIntArray(R.array.bodypart_ids);
                String[] bp_fullname = resources.getStringArray(R.array.bodypart_fullnames);
                String[] bp_shortname = resources.getStringArray(R.array.bodypart_shortnames);
                int[] bp_types = resources.getIntArray(R.array.bodypart_regionids);
                String[] bp_regionNames = resources.getStringArray(R.array.bodypart_regionnames);
                int[] bp_setmins = resources.getIntArray(R.array.bodypart_setmin);
                int[] bp_setmaxs = resources.getIntArray(R.array.bodypart_setmax);
                int[] bp_repmins = resources.getIntArray(R.array.bodypart_repmin);
                int[] bp_repmaxs = resources.getIntArray(R.array.bodypart_repmax);
                String[] bp_powerfactors = resources.getStringArray(R.array.bodypart_power_rating);
                int[] bp_parentids = resources.getIntArray(R.array.bodypart_parentids);
                String[] bp_parentNames = resources.getStringArray(R.array.bodypart_parentnames);
                while (i < bp_ids.length) {
                    Bodypart bodypart = new Bodypart();
                    bodypart._id = bp_ids[i];
                    bodypart.shortName = bp_shortname[i];
                    bodypart.fullName = bp_fullname[i];
                    bodypart.regionID = (long)bp_types[i];
                    bodypart.regionName = bp_regionNames[i];
                    bodypart.setMin = bp_setmins[i];
                    bodypart.setMax = bp_setmaxs[i];
                    bodypart.repMin = bp_repmins[i];
                    bodypart.repMax = bp_repmaxs[i];
                    if (bp_parentids[i] > 0)
                        bodypart.parentID = (long)bp_parentids[i];
                    bodypart.parentName = bp_parentNames[i];
                    if ((bp_powerfactors[i] != null) && (bp_powerfactors[i].length() > 0)) bodypart.powerFactor = Float.parseFloat(bp_powerfactors[i]);
                //    cupboard().withDatabase(db).put(bodypart);
                    //bodypartArrayList.add(bodypart);
                    i++;
                }
                Log.i(TAG, "setup added " + Integer.toString(i) + " bodyparts");
                i = 0;  // start again with exercises now!
                String ex_fullname[] = resources.getStringArray(R.array.exercise_name_list);
                String ex_othernames[] = resources.getStringArray(com.a_track_it.fitdata.common.R.array.exercise_other_name_list);
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
                while (i < ex_fullname.length) {
                    Exercise exercise = new Exercise();
                    exercise._id = (i + 1);
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
                    if ((ex_powerfactor[i] != null) && (ex_powerfactor[i].length() > 0)) exercise.powerFactor = Float.parseFloat(ex_powerfactor[i]);
                  //  cupboard().withDatabase(db).put(exercise);
                   // exerciseArrayList.add(exercise);
                    i++;

                }
                Log.i(TAG,"setup added " + Integer.toString(i) + " exercises");
                Log.i(TAG,"setup finished successfully");
         //   }else{
         //       Log.w(TAG,"setup failed - db unavailable");
          //      UserPreferences.setAppSetupCompleted(context, false);
          //  }
    }
       // }
        //throw new UnsupportedOperationException("Not yet implemented");
}

