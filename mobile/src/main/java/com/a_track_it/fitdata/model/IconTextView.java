package com.a_track_it.fitdata.model;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

public class IconTextView extends androidx.appcompat.widget.AppCompatTextView {

    private Context context;

    public IconTextView(Context context) {
        super(context);
        this.context = context;
        createView();
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        createView();
    }

    private void createView(){
        setGravity(Gravity.CENTER);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"fonts/FA5Free-Regular-400.otf"); //Maiandra GD Regular  or FA5Free-Regular-400.otf
        setTypeface(typeface);
    }
}
