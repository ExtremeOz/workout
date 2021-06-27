package com.a_track_it.workout.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.activity.IEntityFragmentActivityCallback;
import com.a_track_it.workout.adapter.ExerciseAdapter;
import com.a_track_it.workout.adapter.ExerciseListAdapter;
import com.a_track_it.workout.adapter.NameListAdapter;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Exercise;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by Daniel Haywood
 *
 * Input form used to add exercise.workout.a_track_it.com manual entry.
 */

public class ExerciseFragment extends Fragment {
    public static final String ARG_EXERCISE_ID = "ARG_EXERCISE_ID";
    public static final String ARG_EXERCISE_OBJECT = "ARG_EXERCISE_OBJECT";
    public static final String TAG = ExerciseFragment.class.getSimpleName();
    private List<Exercise> exerciseNameAutoList = new ArrayList<>();
    private List<Exercise> pendingList  = new ArrayList<>();     // exercises requiring editing
    private List<Exercise> matchingList  = new ArrayList<>();     // similar existing exercises
    private List<String> matchingAutoList  = new ArrayList<>();  // auto complete workout exercises matches
    private Calendar cal = Calendar.getInstance();
    private ReferencesTools mRefTools;
    private long mExerciseID;
    private Exercise mExercise;
    private Exercise mMatchingExercise;
    private String[] resistanceTypeList;
    private int[] resistanceTypeIDList;
    private String[] bodypartNames;
    private int[] bodypartIDs;
    private boolean bLoading;
    private int pendingFlag;
    private View rootView;
    private IEntityFragmentActivityCallback fragmentActivityCallback;
    private FragmentInterface mListener;
    private ExerciseAdapter listAdapter;
    private ExerciseAdapter pendingListAdapter;
    private ExerciseAdapter matchingListAdapter;
    private int shortAnimationDuration;
    RecyclerView recyclerViewExercise;
    RecyclerView recyclerViewMatchingExercise;
    AutoCompleteTextView autoCompleteTextView;
    AutoCompleteTextView autoCompleteFindTextView;
    AutoCompleteTextView autoCompleteWorkoutExerciseTextView;
    MaterialButton btnUseMatch;
    Spinner spinnerResistanceType;
    TextView textViewMaxHistoryText;
    TextView textViewLabelMaxHistory;
    TextView textViewLastTrained;
    com.google.android.material.textfield.TextInputEditText editTextLastSets;
    com.google.android.material.textfield.TextInputEditText editTextLastReps;
    com.google.android.material.textfield.TextInputEditText editTextLastWeight;
    Spinner spinnerBodypartPrimary;
    Spinner spinnerBodypartSecondary;
    Spinner spinnerBodypartThird;
    Spinner spinnerBodypartFourth;

    private ExerciseAdapter.OnItemClickListener matchingListClickListener = new ExerciseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, Exercise viewModel) {
            mMatchingExercise = viewModel;
            bLoading = true;
            new Handler(Looper.myLooper()).post(new Runnable() {
                @Override
                public void run() {
                    loadMatchingExercise();
                    btnUseMatch.setVisibility(View.VISIBLE);
                }
            });
            bLoading = false;
            mListener.OnFragmentInteraction(recyclerViewMatchingExercise.getId(),viewModel._id,viewModel.name);
        }
    };
    private ExerciseAdapter.OnItemClickListener exerciseListClickListener = new ExerciseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, Exercise viewModel) {

            if ((pendingFlag == 0) && (mExercise._id != viewModel._id)){
                Log.e(ExerciseFragment.class.getSimpleName(),"doing a save");
                fragmentActivityCallback.onSaveCurrent();
            }
            mExercise = viewModel;
            new Handler(Looper.myLooper()).post(new Runnable() {
                @Override
                public void run() {
                    bLoading = true;
                    btnUseMatch.setVisibility(View.INVISIBLE);
                    autoCompleteTextView.setText(mExercise.name,false);
                    autoCompleteTextView.dismissDropDown();
                    mExerciseID = mExercise._id;
                    matchingListAdapter.clearList();

                    // trigger matching lookups
                    mListener.OnFragmentInteraction(recyclerViewExercise.getId(), viewModel._id,viewModel.name);

                    if (mExercise.bodypartCount < 0){

                    }else{
                        if ((mExercise.workoutExercise != null) &&(mExercise.workoutExercise.length() > 0))
                            autoCompleteWorkoutExerciseTextView.setText(mExercise.workoutExercise);
                        autoCompleteWorkoutExerciseTextView.dismissDropDown();
                    }
                    if ((mExercise.resistanceType != null) && (mExercise.resistanceType > 0)){
                        for(int i=0; i < resistanceTypeIDList.length; i++){
                            if (mExercise.resistanceType == resistanceTypeIDList[i]){
                                spinnerResistanceType.setSelection(i);
                                break;
                            }
                        }
                    }else
                        spinnerResistanceType.setSelection(0);

                    if (mExercise.lastTrained > 0)
                        textViewLastTrained.setText(Utilities.getTimeDateString(mExercise.lastTrained));
                    else
                        textViewLastTrained.setText(getString(R.string.exercise_last_trained_none));

                    if (mExercise.lastSets > 0){
                        String s = String.format(Locale.getDefault(), "%d",mExercise.lastSets);
                        editTextLastSets.setText(s);
                    }
                    if (mExercise.lastReps > 0){
                        String s = String.format(Locale.getDefault(), "%d",mExercise.lastReps);
                        editTextLastReps.setText(s);
                    }
                    if (mExercise.lastAvgWeight > 0F){
                        String s = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,mExercise.lastAvgWeight);
                        editTextLastWeight.setText(s);
                    }
                    if ((mExercise.maxWeight > 0) && (mExercise.lastMaxWeight > 0L)){
                        String sTemp = Utilities.getTimeDateString(mExercise.lastMaxWeight);
                        textViewLabelMaxHistory.setVisibility(TextView.VISIBLE);
                        textViewLabelMaxHistory.setText(sTemp);
                        textViewMaxHistoryText.setVisibility(TextView.VISIBLE);
                        sTemp = String.format(Locale.getDefault(), getString(R.string.exercise_max_kg_format),mExercise.maxWeight, mExercise.maxReps);
                        textViewMaxHistoryText.setText(sTemp);
                    }else{
                        textViewMaxHistoryText.setText(Constants.ATRACKIT_EMPTY);
                        textViewLabelMaxHistory.setText(Constants.ATRACKIT_EMPTY);
                        textViewLabelMaxHistory.setVisibility(TextView.GONE);
                        textViewMaxHistoryText.setVisibility(TextView.GONE);
                    }
                    if ((mExercise.first_BPID != null) && (mExercise.first_BPID > 0)){
                        for(int i=0; i < bodypartIDs.length; i++){
                            if (mExercise.first_BPID == bodypartIDs[i]){
                                spinnerBodypartPrimary.setSelection(i);
                                break;
                            }
                        }
                    }else
                        spinnerBodypartPrimary.setSelection(0);
                    if ((mExercise.second_BPID != null)&&(mExercise.second_BPID > 0)){
                        for(int i=0; i < bodypartIDs.length; i++){
                            if (mExercise.second_BPID == bodypartIDs[i]){
                                spinnerBodypartSecondary.setSelection(i);
                                break;
                            }
                        }
                    }else
                        spinnerBodypartSecondary.setSelection(0);
                    if ((mExercise.third_BPID != null) && (mExercise.third_BPID > 0)){
                        for(int i=0; i < bodypartIDs.length; i++){
                            if (mExercise.third_BPID == bodypartIDs[i]){
                                spinnerBodypartThird.setSelection(i);
                                break;
                            }
                        }
                    }else
                        spinnerBodypartThird.setSelection(0);
                    if ((mExercise.fourth_BPID != null) && (mExercise.fourth_BPID > 0)){
                        for(int i=0; i < bodypartIDs.length; i++){
                            if (mExercise.fourth_BPID == bodypartIDs[i]){
                                spinnerBodypartFourth.setSelection(i);
                                break;
                            }
                        }
                    }else
                        spinnerBodypartFourth.setSelection(0);

                    fragmentActivityCallback.onChangedState(false);
                    bLoading = false;
                }
            });

        }
    };
    // set the controls from the returned exercise object
    private  ExerciseListAdapter.OnItemClickListener autoCompleteNameItemClickListener = new ExerciseListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, Exercise viewModel) {
            mExercise = viewModel;
            autoCompleteTextView.setText(mExercise.name,false);
            autoCompleteTextView.dismissDropDown();
            mExerciseID = mExercise._id;
            bLoading = true;
            if ((mExercise.resistanceType != null) && (mExercise.resistanceType > 0)){
                for(int i=0; i < resistanceTypeIDList.length; i++){
                    if (mExercise.resistanceType == resistanceTypeIDList[i]){
                        spinnerResistanceType.setSelection(i);
                        break;
                    }
                }
            }else
                spinnerResistanceType.setSelection(0);
            if (mExercise.lastTrained > 0)
                textViewLastTrained.setText(Utilities.getTimeDateString(mExercise.lastTrained));
            else
                textViewLastTrained.setText(getString(R.string.exercise_last_trained_none));

            if (mExercise.lastSets > 0){
                String s = String.format(Locale.getDefault(), "%d",mExercise.lastSets);
                editTextLastSets.setText(s);
            }
            if (mExercise.lastReps > 0){
                String s = String.format(Locale.getDefault(), "%d",mExercise.lastReps);
                editTextLastReps.setText(s);
            }
            if (mExercise.lastAvgWeight > 0F){
                String s = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,mExercise.lastAvgWeight);
                editTextLastWeight.setText(s);
            }
            if ((mExercise.maxWeight > 0) && (mExercise.lastReps > 0L)){
                String sTemp = Utilities.getTimeDateString(mExercise.lastMaxWeight);
                String sFormat = getString(R.string.exercise_max_kg_format);
                textViewLabelMaxHistory.setVisibility(TextView.VISIBLE);
                textViewLabelMaxHistory.setText(sTemp);
                textViewMaxHistoryText.setVisibility(TextView.VISIBLE);
                try {
                    sTemp = String.format(Locale.getDefault(), sFormat, mExercise.maxWeight, mExercise.lastReps);
                    textViewMaxHistoryText.setText(sTemp);
                }catch (Exception e){
                    textViewMaxHistoryText.setText(Constants.ATRACKIT_EMPTY);
                }
            }else{
                textViewMaxHistoryText.setText(Constants.ATRACKIT_EMPTY);
                textViewLabelMaxHistory.setText(Constants.ATRACKIT_EMPTY);
                textViewLabelMaxHistory.setVisibility(TextView.GONE);
                textViewMaxHistoryText.setVisibility(TextView.GONE);
            }
            if ((mExercise.first_BPID != null) && (mExercise.first_BPID > 0)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.first_BPID == bodypartIDs[i]){
                        spinnerBodypartPrimary.setSelection(i);
                        break;
                    }
                }
            }else
                spinnerBodypartPrimary.setSelection(0);
            if ((mExercise.second_BPID != null) && (mExercise.second_BPID > 0)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.second_BPID == bodypartIDs[i]){
                        spinnerBodypartSecondary.setSelection(i);
                        break;
                    }
                }
            }else spinnerBodypartSecondary.setSelection(0);
            if ((mExercise.third_BPID != null) && (mExercise.third_BPID > 0)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.third_BPID == bodypartIDs[i]){
                        spinnerBodypartThird.setSelection(i);
                        break;
                    }
                }
            }else
                spinnerBodypartThird.setSelection(0);
            if ((mExercise.fourth_BPID != null) &&(mExercise.fourth_BPID > 0)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.fourth_BPID == bodypartIDs[i]){
                        spinnerBodypartFourth.setSelection(i);
                        break;
                    }
                }
            }else spinnerBodypartFourth.setSelection(0);
            fragmentActivityCallback.onChangedState(false);
            bLoading = false;
        }
    };
    private NameListAdapter.OnItemClickListener autoWorkoutExerciseItemClickListener = new NameListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, String name) {
            mExercise.workoutExercise = name;
        }
    };
    private  ExerciseListAdapter.OnItemClickListener autoCompleteFindPossibleClickListener = new ExerciseListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, Exercise viewModel) {
            autoCompleteFindTextView.setText(viewModel.name,false);
            autoCompleteFindTextView.dismissDropDown();
            mMatchingExercise = viewModel;
            bLoading = true;
            loadMatchingExercise();
            fragmentActivityCallback.onChangedState(false);
         //   btnUseMatch.setEnabled(true);
            ArrayList<Exercise> aList = new ArrayList<>();
            aList.add(mMatchingExercise);
            setMatchingList(aList);
            bLoading = false;
        }
    };

    public static ExerciseFragment create(long exerciseID, Exercise ex) {
        Bundle args = new Bundle();
        args.putLong(ARG_EXERCISE_ID, exerciseID);
        if (ex != null) args.putParcelable(ARG_EXERCISE_OBJECT, ex);
        ExerciseFragment fragment = new ExerciseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bLoading = true;
        mExerciseID = getArguments().getLong(ARG_EXERCISE_ID);
        if (getArguments().containsKey(ARG_EXERCISE_OBJECT)){
            mExercise = getArguments().getParcelable(ARG_EXERCISE_OBJECT);
        }else
            mExercise = new Exercise();
        if (getArguments().containsKey(Constants.KEY_FIT_TYPE)) pendingFlag = getArguments().getInt(Constants.KEY_FIT_TYPE);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(getContext().getApplicationContext());
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        resistanceTypeList =  getResources().getStringArray(R.array.resistance_types);
        resistanceTypeIDList =  getResources().getIntArray(R.array.resistance_types_ids);
        bodypartIDs = getResources().getIntArray(R.array.bodypart_ids);
        bodypartNames = getResources().getStringArray(R.array.bodypart_shortnames);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_exercise, container, false);
        bLoading = true;

        autoCompleteTextView = rootView.findViewById(R.id.autoCompleteTextView);
        autoCompleteWorkoutExerciseTextView = rootView.findViewById(R.id.autoCompleteWorkoutExerciseTextView);
        btnUseMatch = rootView.findViewById(R.id.btnExerciseUseMatch);
        spinnerResistanceType = rootView.findViewById(R.id.spinnerResistanceType);
        textViewMaxHistoryText = rootView.findViewById(R.id.textViewMaxHistoryText);
        textViewLabelMaxHistory = rootView.findViewById(R.id.textViewLabelMaxHistory);
        textViewLastTrained = rootView.findViewById(R.id.textViewLastTrained);
        editTextLastSets = rootView.findViewById(R.id.editTextLastSets);
        editTextLastReps = rootView.findViewById(R.id.editTextLastReps);
        editTextLastWeight = rootView.findViewById(R.id.editTextLastWeight);
        recyclerViewExercise = rootView.findViewById(R.id.listExercise);
        recyclerViewMatchingExercise = rootView.findViewById(R.id.listPossibleExercise);

        ExerciseListAdapter exerciseNameListAdapter = new ExerciseListAdapter(container.getContext(), R.layout.autocomplete_dropdown_item, exerciseNameAutoList);
        exerciseNameListAdapter.setOnItemClickListener(autoCompleteNameItemClickListener);
        NameListAdapter workoutExerciseListAdapter = new NameListAdapter(getContext(),R.layout.autocomplete_dropdown_item,matchingAutoList, Constants.OBJECT_TYPE_EXERCISE);
        workoutExerciseListAdapter.setOnItemClickListener(autoWorkoutExerciseItemClickListener);
        if (pendingFlag > 0) {
            pendingListAdapter = new ExerciseAdapter(getContext(),1);
            if (pendingList.size() > 0){
                ArrayList<Exercise> aList = new ArrayList<>();
                aList.addAll(pendingList);
                pendingListAdapter.setItems(aList);
            }
            pendingListAdapter.setOnItemClickListener(exerciseListClickListener);

            recyclerViewExercise.setAdapter(pendingListAdapter);
            recyclerViewExercise.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewExercise.setLayoutManager(llm);
            recyclerViewExercise.setVisibility(View.VISIBLE);
            ((TextView)rootView.findViewById(R.id.labelEditExercise)).setText("Select match or update new exercise");
            matchingListAdapter = new ExerciseAdapter(getContext(),1);
            matchingListAdapter.setOnItemClickListener(matchingListClickListener);

            rootView.findViewById(R.id.editInputLayoutFindPossible).setVisibility(View.VISIBLE);
            autoCompleteFindTextView = rootView.findViewById(R.id.autoCompleteFindTextView);
            ExerciseListAdapter exerciseFindNameListAdapter = new ExerciseListAdapter(getContext(), R.layout.autocomplete_dropdown_item, exerciseNameAutoList);
            exerciseFindNameListAdapter.setOnItemClickListener(autoCompleteFindPossibleClickListener);
            autoCompleteFindTextView.setAdapter(exerciseFindNameListAdapter);
            recyclerViewMatchingExercise.setAdapter(matchingListAdapter);
            recyclerViewMatchingExercise.setHasFixedSize(true);
            LinearLayoutManager llm2 = new LinearLayoutManager(getContext());
            llm2.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewMatchingExercise.setLayoutManager(llm2);
            if (matchingList.size() > 0){
                ArrayList<Exercise> aList = new ArrayList<>();
                aList.addAll(matchingList);
                matchingListAdapter.setItems(aList);
                recyclerViewMatchingExercise.setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.labelList2).setVisibility(View.VISIBLE);

            }
            btnUseMatch.setVisibility(MaterialButton.INVISIBLE);
            crossFadeIn(rootView.findViewById(R.id.pendingCard));
        }
        else {
            crossFadeOut(rootView.findViewById(R.id.pendingCard));
/*            rootView.findViewById(R.id.labelList).setVisibility(View.GONE);
            rootView.findViewById(R.id.labelList1).setVisibility(View.GONE);
            rootView.findViewById(R.id.labelList2).setVisibility(View.GONE);
            rootView.findViewById(R.id.editInputLayoutFindPossible).setVisibility(View.GONE);
            //autoCompleteFindTextView.setVisibility(View.GONE);
            recyclerViewExercise.setVisibility(View.GONE);
            recyclerViewMatchingExercise.setVisibility(View.GONE);
            btnUseMatch.setVisibility(MaterialButton.GONE);*/
        }
        if (textViewMaxHistoryText == null) textViewMaxHistoryText = rootView.findViewById(R.id.textViewMaxHistoryText);
        if (textViewLabelMaxHistory == null) textViewLabelMaxHistory = rootView.findViewById(R.id.textViewLabelMaxHistory);
        if (textViewLastTrained == null) textViewLastTrained = rootView.findViewById(R.id.textViewLastTrained);
        if (editTextLastSets == null) editTextLastSets = rootView.findViewById(R.id.editTextLastSets);
        if (spinnerResistanceType == null) spinnerResistanceType = rootView.findViewById(R.id.spinnerResistanceType);
        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.resistance_types, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResistanceType.setAdapter(adapter);

        if (editTextLastReps == null) editTextLastReps = rootView.findViewById(R.id.editTextLastReps);
        if (editTextLastWeight == null) editTextLastWeight = rootView.findViewById(R.id.editTextLastWeight);
        if (spinnerBodypartPrimary == null) spinnerBodypartPrimary = rootView.findViewById(R.id.spinnerBodypartPrimary);
        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapterBP1 = ArrayAdapter.createFromResource(getContext(), R.array.bodypart_shortnames, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBP1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodypartPrimary.setAdapter(adapterBP1);

        if (spinnerBodypartSecondary == null) spinnerBodypartSecondary = rootView.findViewById(R.id.spinnerBodypartSecondary);
        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapterBP2 = ArrayAdapter.createFromResource(getContext(), R.array.bodypart_shortnames, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBP2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodypartSecondary.setAdapter(adapterBP2);
        if (spinnerBodypartThird == null) spinnerBodypartThird = rootView.findViewById(R.id.spinnerBodypartThird);
        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapterBP3 = ArrayAdapter.createFromResource(getContext(), R.array.bodypart_shortnames, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBP3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodypartThird.setAdapter(adapterBP3);
        if (spinnerBodypartFourth == null) spinnerBodypartFourth = rootView.findViewById(R.id.spinnerBodypartFourth);
        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> adapterBP4 = ArrayAdapter.createFromResource(getContext(), R.array.bodypart_shortnames, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBP4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodypartFourth.setAdapter(adapterBP4);
        if (btnUseMatch == null) btnUseMatch = rootView.findViewById(R.id.btnExerciseUseMatch);
        btnUseMatch.setOnClickListener(v -> {
            Exercise selectedMatch = matchingListAdapter.getSelectedExercise();
            if (selectedMatch != null)
                mListener.OnFragmentInteraction(Constants.UID_btnExerciseUseMatch,selectedMatch._id,selectedMatch.name);
               else Toast.makeText(getContext(), "select a match then click apply", Toast.LENGTH_SHORT).show();
        });
        autoCompleteTextView.setAdapter(exerciseNameListAdapter);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
              if (!bLoading)  mExercise.name = s.toString();
            }
        });
        autoCompleteWorkoutExerciseTextView.setAdapter(workoutExerciseListAdapter);
        autoCompleteWorkoutExerciseTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
               if (!bLoading) mExercise.workoutExercise = s.toString();
            }
        });
        spinnerResistanceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExercise.resistanceType = (long)resistanceTypeIDList[position];
                mExercise.resistanceTypeName = resistanceTypeList[position];
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }
        });
        spinnerBodypartPrimary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExercise.first_BPID = (long)bodypartIDs[position];
                mExercise.first_BPName = bodypartNames[position];
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mExercise.first_BPID = 0L;
                mExercise.first_BPName  = Constants.ATRACKIT_EMPTY;
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }
        });
        spinnerBodypartSecondary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExercise.second_BPID = (long)bodypartIDs[position];
                mExercise.second_BPName = bodypartNames[position];
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mExercise.second_BPID = 0L;
                mExercise.second_BPName  = Constants.ATRACKIT_EMPTY;
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }
        });
        spinnerBodypartThird.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExercise.third_BPID = (long)bodypartIDs[position];
                mExercise.third_BPName = bodypartNames[position];
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mExercise.third_BPID = 0L;
                mExercise.third_BPName  = Constants.ATRACKIT_EMPTY;
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }
        });
        spinnerBodypartFourth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExercise.fourth_BPID = (long)bodypartIDs[position];
                mExercise.fourth_BPName = bodypartNames[position];
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mExercise.fourth_BPID = 0L;
                mExercise.fourth_BPName  = Constants.ATRACKIT_EMPTY;
                if (!bLoading) fragmentActivityCallback.onChangedState(true);
            }
        });
        editTextLastSets.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    String str = s.toString();
                    if (TextUtils.isDigitsOnly(str)) {
                        mExercise.lastSets = Integer.parseInt(str);
                        if (!bLoading) fragmentActivityCallback.onChangedState(true);
                    }
                }
            }
        });
        editTextLastReps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    String str = s.toString();
                    if (TextUtils.isDigitsOnly(str)) {
                        mExercise.lastReps = Integer.parseInt(str);
                        if (!bLoading) fragmentActivityCallback.onChangedState(true);
                    }
                }
            }
        });
        editTextLastWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    String str = s.toString();
                    if (TextUtils.isDigitsOnly(str)) {
                        mExercise.lastAvgWeight = Float.parseFloat(str);
                        if (!bLoading) fragmentActivityCallback.onChangedState(true);
                    }
                }
            }
        });
        if (pendingFlag == 0) loadUI();

        // trigger a load from first pending
/*        if (pendingList.size() > 0){
            pendingListAdapter.setTargetId(pendingList.get(0)._id);
        }*/
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT, ExerciseFragment.class.getSimpleName());
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(mExerciseID));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IEntityFragmentActivityCallback) {
            fragmentActivityCallback = (IEntityFragmentActivityCallback) context;
        }
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
        }

    }

    @Override
    public void onDetach() {
        fragmentActivityCallback = null;
        mListener = null;
        super.onDetach();
    }
    public void loadMatchingExercise(){
        if ((mMatchingExercise.resistanceType != null) && (mMatchingExercise.resistanceType > 0)){
            for(int i=0; i < resistanceTypeIDList.length; i++){
                if (mMatchingExercise.resistanceType == resistanceTypeIDList[i]){
                    spinnerResistanceType.setSelection(i);
                    break;
                }
            }
        }
        autoCompleteTextView.setText(mMatchingExercise.name);
        autoCompleteWorkoutExerciseTextView.setText(mMatchingExercise.workoutExercise);
        if (mMatchingExercise.lastTrained > 0)
            textViewLastTrained.setText(Utilities.getTimeDateString(mMatchingExercise.lastTrained));
        else
            textViewLastTrained.setText(getString(R.string.exercise_last_trained_none));

        if (mMatchingExercise.lastSets > 0){
            String s = String.format(Locale.getDefault(), "%d",mMatchingExercise.lastSets);
            editTextLastSets.setText(s);
        }
        if (mMatchingExercise.lastReps > 0){
            String s = String.format(Locale.getDefault(), "%d",mMatchingExercise.lastReps);
            editTextLastReps.setText(s);
        }
        if (mMatchingExercise.lastAvgWeight > 0F){
            String s = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,mMatchingExercise.lastAvgWeight);
            editTextLastWeight.setText(s);
        }
        if ((mMatchingExercise.maxWeight > 0) && (mMatchingExercise.lastMaxWeight > 0L)){
            String sTemp = Utilities.getTimeDateString(mExercise.lastMaxWeight);
            String sFormat = getString(R.string.exercise_max_kg_format);
            textViewLabelMaxHistory.setVisibility(TextView.VISIBLE);
            textViewLabelMaxHistory.setText(sTemp);
            textViewMaxHistoryText.setVisibility(TextView.VISIBLE);
            try {
                sTemp = String.format(Locale.getDefault(), sFormat, mExercise.maxWeight, mExercise.lastReps);
                textViewMaxHistoryText.setText(sTemp);
            }catch (Exception e){
                textViewMaxHistoryText.setText(Constants.ATRACKIT_EMPTY);
            }
        }else{
            textViewMaxHistoryText.setText(Constants.ATRACKIT_EMPTY);
            textViewLabelMaxHistory.setText(Constants.ATRACKIT_EMPTY);
            textViewLabelMaxHistory.setVisibility(TextView.GONE);
            textViewMaxHistoryText.setVisibility(TextView.GONE);
        }
        if ((mMatchingExercise.first_BPID != null) && (mMatchingExercise.first_BPID > 0)){
            for(int i=0; i < bodypartIDs.length; i++){
                if (mMatchingExercise.first_BPID == bodypartIDs[i]){
                    spinnerBodypartPrimary.setSelection(i);
                    break;
                }
            }
        }else
            spinnerBodypartPrimary.setSelection(0);
        if ((mMatchingExercise.second_BPID != null) && (mMatchingExercise.second_BPID > 0)){
            for(int i=0; i < bodypartIDs.length; i++){
                if (mMatchingExercise.second_BPID == bodypartIDs[i]){
                    spinnerBodypartSecondary.setSelection(i);
                    break;
                }
            }
        }else spinnerBodypartSecondary.setSelection(0);
        if ((mMatchingExercise.third_BPID != null) && (mMatchingExercise.third_BPID > 0)){
            for(int i=0; i < bodypartIDs.length; i++){
                if (mMatchingExercise.third_BPID == bodypartIDs[i]){
                    spinnerBodypartThird.setSelection(i);
                    break;
                }
            }
        }else
            spinnerBodypartThird.setSelection(0);
        if ((mMatchingExercise.fourth_BPID != null) && (mMatchingExercise.fourth_BPID > 0)){
            for(int i=0; i < bodypartIDs.length; i++){
                if (mMatchingExercise.fourth_BPID == bodypartIDs[i]){
                    spinnerBodypartFourth.setSelection(i);
                    break;
                }
            }
        }else spinnerBodypartFourth.setSelection(0);
    }

    public void loadUI(){
            bLoading = true;
            if ((mExercise != null) && (mExercise.name != null) && (mExercise.name.length() > 0)) {
                ((TextView)rootView.findViewById(R.id.labelEditExercise)).setText(R.string.action_edit);
                autoCompleteTextView.setText(mExercise.name, false);
                if ((mExercise.workoutExercise != null) && (mExercise.workoutExercise.length() > 0) && (mExercise.bodypartCount >= 0)){
                    autoCompleteWorkoutExerciseTextView.setText(mExercise.workoutExercise);
                }else{
                    autoCompleteWorkoutExerciseTextView.setText(Constants.ATRACKIT_EMPTY);
                }
            }
            if ((mExercise != null) && (mExercise.resistanceType != null) && (mExercise.resistanceType > 0)){
              //  ((TextView)rootView.findViewById(R.id.labelEditExercise)).setVisibility(View.GONE);
                for(int i=0; i < resistanceTypeIDList.length; i++){
                    if (mExercise.resistanceType == resistanceTypeIDList[i]){
                        spinnerResistanceType.setSelection(i);
                        break;
                    }
                }
            }
            if (mExercise != null){
                if (mExercise.lastTrained > 0){
                    textViewLastTrained.setText(Utilities.getTimeDateString(mExercise.lastTrained));
                }else
                    textViewLastTrained.setText(getString(R.string.exercise_last_trained_none));
            }else
                textViewLastTrained.setText(getString(R.string.exercise_last_trained_none));

            if ((mExercise != null) && (mExercise.maxWeight > 0) && (mExercise.lastMaxWeight > 0L)) {
                String sTemp = Utilities.getTimeDateString(mExercise.lastMaxWeight);
                textViewLabelMaxHistory.setVisibility(TextView.VISIBLE);
                textViewLabelMaxHistory.setText(sTemp);
                textViewMaxHistoryText.setVisibility(TextView.VISIBLE);
                if ((mExercise.maxWeight >0f) && (mExercise.maxReps > 0)) {
                    try {
                        sTemp = Constants.ATRACKIT_EMPTY;
                        sTemp = String.format(Locale.getDefault(), getString(R.string.exercise_max_kg_format), mExercise.maxWeight, mExercise.maxReps);
                        textViewMaxHistoryText.setText(sTemp);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else{
                textViewLabelMaxHistory.setVisibility(TextView.GONE);
                textViewMaxHistoryText.setVisibility(TextView.GONE);
            }

            if ((mExercise != null) && (mExercise._id > 0)){
                String s = String.format(Locale.getDefault(), "%d",mExercise.lastSets);
                editTextLastSets.setText(s);
            }
            if ((mExercise != null) && (mExercise._id > 0)){
                String s = String.format(Locale.getDefault(), "%d",mExercise.lastReps);
                editTextLastReps.setText(s);
            }
            if ((mExercise != null) && (mExercise._id > 0)){
                String s = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,mExercise.lastAvgWeight);
                editTextLastWeight.setText(s);
            }
            if ((mExercise != null) && (mExercise._id > 0) && (mExercise.first_BPID != null)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.first_BPID == bodypartIDs[i]){
                        spinnerBodypartPrimary.setSelection(i);
                        break;
                    }
                }
            }
            if ((mExercise != null) && (mExercise._id > 0) && (mExercise.second_BPID != null)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.second_BPID == bodypartIDs[i]){
                        spinnerBodypartSecondary.setSelection(i);
                        break;
                    }
                }
            }
            if ((mExercise != null) && (mExercise._id > 0) && (mExercise.third_BPID != null)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.third_BPID == bodypartIDs[i]){
                        spinnerBodypartThird.setSelection(i);
                        break;
                    }
                }
            }

            if ((mExercise != null) && (mExercise._id > 0) && (mExercise.fourth_BPID != null)){
                for(int i=0; i < bodypartIDs.length; i++){
                    if (mExercise.fourth_BPID == bodypartIDs[i]){
                        spinnerBodypartFourth.setSelection(i);
                        break;
                    }
                }
            }

        if (fragmentActivityCallback != null) fragmentActivityCallback.onChangedState(false);
        bLoading = false;
    }
    public Exercise getPendingExercise(){
        if (pendingListAdapter.getSelectedExercise() != null)
            return pendingListAdapter.getSelectedExercise();
        if (pendingList.size() > 0)
            return pendingList.get(0);
        else
            return null;

    }
    public Exercise getExercise(){
        return  mExercise;
    }
    public Exercise getMatchingExercise(){ return  mMatchingExercise;  }
    public void setExercise(Exercise exercise){
        mExercise = exercise;
    }
    public  int getPendingFlag(){ return pendingFlag;}
    public void setPendingFlag(int flag){
        pendingFlag = flag;
    }
    public void setList(List<Exercise> exercises){
        if (pendingList.size() > 0)
            pendingList.clear();
        pendingList.addAll(exercises);
        new Handler(Looper.myLooper()).post(() -> {
            if (listAdapter != null) {
                ArrayList<Exercise> aList = new ArrayList<>();
                aList.addAll(pendingList);
                listAdapter.clearList();
                listAdapter.setItems(aList);
                btnUseMatch.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void setMatchingList(List<Exercise> inlist){
        if (matchingList.size() > 0)
            matchingList.clear();
        matchingList.addAll(inlist);
        mMatchingExercise = new Exercise(); // non null
        if (matchingListAdapter != null) {
            ArrayList<Exercise> aList = new ArrayList<>();
            aList.addAll(matchingList);
            matchingListAdapter.clearList();
            matchingListAdapter.setItems(aList);
            matchingListAdapter.notifyDataSetChanged();
        }
    }
    public void setMatchingAutoList(List<String> inlist){
        if (matchingAutoList.size() > 0)
            matchingAutoList.clear();
        matchingAutoList.addAll(inlist);

        if (matchingListAdapter != null) {
            ArrayList<Exercise> aList = new ArrayList<>();
            aList.addAll(matchingList);
            matchingListAdapter.setItems(aList);
        }
    }
    private void crossFadeIn(View contentView) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        // final int idView = contentView.getId();
        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                    }
                });

    }
    private void crossFadeOut(View contentView) {
        contentView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentView.setVisibility(View.GONE);
                    }
                });

    }
    public List<Exercise> getExerciseList(){return  pendingList;}
}
