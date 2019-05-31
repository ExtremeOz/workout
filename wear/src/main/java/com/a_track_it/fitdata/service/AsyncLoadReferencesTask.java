package com.a_track_it.fitdata.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.a_track_it.fitdata.activity.IDataLoaderCallback;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Bodypart;
import com.a_track_it.fitdata.common.model.Exercise;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;


public class AsyncLoadReferencesTask extends AsyncTask<Void, Void, java.lang.Integer> {
    private static final String TAG = "AsyncLoadReferencesTask";


    private static int totalFinal = 0;
    private Context mContext;
    private ArrayList<FitnessActivity> fitnessActivityArrayList = new ArrayList<>();
    private ArrayList<Bodypart> bodypartArrayList = new ArrayList<>();
    private ArrayList<Exercise> exerciseArrayList = new ArrayList<>();
    private IDataLoaderCallback callback;

    public AsyncLoadReferencesTask(IDataLoaderCallback activityCallback, Context context){
        this.callback = activityCallback;
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer anInt) {
        super.onPostExecute(anInt);
        callback.actionCompleted(true,1,4);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Gson gson = new Gson();
        int i;
        totalFinal = 0;
        // Log.i(TAG,"setup added " + Integer.toString(i) + " body parts");
        String sJSON = GetFromLocalFile(Constants.ACTIVITY_FILENAME);
        if (sJSON.length() > 0) {
            FitnessActivity[] act_array = gson.fromJson(sJSON, FitnessActivity[].class);
            fitnessActivityArrayList.clear();
            fitnessActivityArrayList = new ArrayList<>(Arrays.asList(act_array));
            i = fitnessActivityArrayList.size();
            Log.i(TAG, "loaded activities " + Integer.toString(i));
            if (i > 0) callback.setActivityList(fitnessActivityArrayList);
            totalFinal += i;
        }
        sJSON = "";
        sJSON = GetFromLocalFile(Constants.BODYPART_FILENAME);
        if (sJSON.length() > 0) {
            Bodypart[] bp_array = gson.fromJson(sJSON, Bodypart[].class);
            bodypartArrayList.clear();
            bodypartArrayList = new ArrayList<>(Arrays.asList(bp_array));
            i = bodypartArrayList.size();
            Log.i(TAG, "loaded body parts " + Integer.toString(i));
            if (i > 0) callback.setBodypartList(bodypartArrayList);
            totalFinal += i;
        }
        sJSON = "";
        sJSON = GetFromLocalFile(Constants.EXERCISE_FILENAME);
        if (sJSON.length() > 0) {
            Exercise[] ex_array = gson.fromJson(sJSON, Exercise[].class);
            exerciseArrayList = new ArrayList<>(Arrays.asList(ex_array));
            i = exerciseArrayList.size();
            Log.i(TAG, "loaded exercises " + Integer.toString(i));
            if (i > 0) callback.setExerciseList(exerciseArrayList);
            totalFinal += i;
        }
        return Integer.valueOf(totalFinal);
    }

    private String GetFromLocalFile(String filename){
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        FileInputStream inputStream;
        String result = "";
        try {
            inputStream = mContext.openFileInput(filename);
            Reader in = new InputStreamReader(inputStream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            result = out.toString();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return result;
        }

    }
}
