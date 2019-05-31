package com.a_track_it.fitdata.common.model;

import com.a_track_it.fitdata.common.R;

/**
 *  FitnessActivity class is used for list displays of Google Fitness Activity Data Type enumerations
 *  used to initiate a new session.
 *
 */
public class FitnessActivity {
    public long _id;
    public String name;
    public int resource_id;
    public int color;
    public String identifier;

    public FitnessActivity(){
        _id = 0L;
        name = "";
        resource_id = 0;
        color = R.color.white;
        identifier = "";
    }
    @Override
    public String toString(){
        return  "{ _id=" + Long.toString(_id) +
                ", name=\"" + name + '\"' +
                ", resource_id=" + Integer.toString(resource_id) +
                ", color=" + Integer.toString(color) +
                ", identifier=\"" + identifier + '\"' + "}";
    }
}
