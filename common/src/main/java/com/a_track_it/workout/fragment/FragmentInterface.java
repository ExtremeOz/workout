package com.a_track_it.workout.fragment;

public interface FragmentInterface {

   void OnFragmentInteraction(int srcId, long selectedId, String text);
   void onItemSelected(int pos, long id, String title, long resId, String identifier);

}
