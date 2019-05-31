package com.a_track_it.fitdata.animation;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Chris Black
 */
public class ItemAnimator extends DefaultItemAnimator
{
    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if(oldHolder != null)
        {
            //oldHolder.itemView.setVisibility(View.INVISIBLE);
            dispatchChangeFinished(oldHolder, true);
        }

        if(newHolder != null)
        {
            //newHolder.itemView.setVisibility(View.VISIBLE);
            newHolder.itemView.setAlpha(1.0F);
            //ViewCompat.setAlpha(newHolder.itemView, 1.0F);
            dispatchChangeFinished(newHolder, false);
        }

        return false;
    }
}
