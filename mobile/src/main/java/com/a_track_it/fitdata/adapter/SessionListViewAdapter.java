package com.a_track_it.fitdata.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class SessionListViewAdapter extends RecyclerView.Adapter<SessionListViewAdapter.ListViewHolder>{
    private static final String LOG_TAG = SessionListViewAdapter.class.getSimpleName();
    private static final String EMPTY_LIST = "Empty list";
    private OnItemClickListener onItemClickListener;
    private static ReferencesTools mRefTools;
    private Workout[] mList;
    private WorkoutSet[] mSetList;
    private boolean mUseKG;
    private boolean mAllowSelect = true;
    private int selectedPos = RecyclerView.NO_POSITION;
    private long mTargetId;
    private int shortAnimationTime;
    private Drawable drawablePlus;
    private Drawable drawableMinus;
    private Drawable drawablePlay;
    private Drawable drawableDelete;
    private Drawable drawableReport;

    // An object of RecyclerView.RecycledViewPool
    // is created to share the Views
    // between the child and
    // the parent RecyclerViews
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public SessionListViewAdapter(boolean useKG, Workout[] list){
        super();
        mList = list;
        mUseKG = useKG;
    }
    private ArrayList<WorkoutSet> getSets(long workoutID){
        ArrayList<WorkoutSet> retSet = new ArrayList<>();
        if ((mSetList != null) && (mSetList.length > 0))
            for (WorkoutSet s: mSetList){
                if (s.workoutID == workoutID) retSet.add(s);
            }
        return retSet;
    }
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_item, parent, false);
        Context context = parent.getContext();
        Resources res = context.getResources();
        int iconSize = res.getDimensionPixelSize(R.dimen.row_button_icon_size);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(context);
        if (drawablePlus == null) {
            drawablePlus = AppCompatResources.getDrawable(context, R.drawable.ic_down_arrow);
        }
        if (drawableMinus == null) {
            drawableMinus = AppCompatResources.getDrawable(context, R.drawable.ic_up_arrow);
        }
        if (drawablePlay == null) {
            drawablePlay = AppCompatResources.getDrawable(context, R.drawable.ic_forward_white);
            drawablePlay.setBounds(0, 0, iconSize, iconSize);
        }
        if (drawableDelete == null) {
            drawableDelete = AppCompatResources.getDrawable(context, R.drawable.ic_trash_black);
            drawableDelete.setBounds(0, 0, iconSize, iconSize);
            Utilities.setColorFilter(drawableDelete, ContextCompat.getColor(context,R.color.secondaryTextColor));
        }
        if (drawableReport == null) {
            drawableReport = AppCompatResources.getDrawable(context, R.drawable.ic_analytics);
        }
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationTime = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        ListViewHolder vh = new ListViewHolder(itemView);
        return vh;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    private void crossFadeIn(View contentView, int shortAnimationDuration) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                        Log.w(LOG_TAG, "cross fade int " + contentView.getId());
                    }
                });
    }
    private void crossFadeOut(View contentView, int shortAnimationDuration) {
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
    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        final View.OnClickListener myClicker = view -> {
            Workout item = (Workout) holder.buttonReport.getTag();
            int iTag = Constants.UID_btn_recycle_session_report;
            if (this.onItemClickListener != null){
                 if (holder.buttonReport.getId() == view.getId()){
                    if (item.parentID != null && (item.parentID < 0))
                        iTag = Constants.UID_btn_recycle_session_edit;
                    onItemClickListener.onItemClick(iTag, item);
                }else
                    if  (holder.buttonStart.getId() == view.getId()){
                        if (item.parentID != null && (item.parentID < 0))
                            iTag = Constants.UID_btn_recycle_session_delete;
                        onItemClickListener.onItemClick(iTag, item);
                    }else
                        onItemClickListener.onItemClick(iTag, item);
            }
            if ((view.getId() == holder.imageExpander.getId()) && (Integer) holder.imageExpander.getTag() > 0) {
                WorkoutAdapter wa = (WorkoutAdapter) holder.childRecyclerView.getAdapter();
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                if ((item != null) && (wa != null)) {
                    if (wa.getItemCount() == 0) {
                        ArrayList<WorkoutSet> sets = getSets(item._id);
                        sets.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                        wa.setWorkoutSetArrayList(sets);
                    }
                    if (wa.getItemCount() > 0) {
                        if (holder.childRecyclerView.getVisibility() != View.VISIBLE) {
                            crossFadeIn(holder.childRecyclerView, shortAnimationTime);
                            if (drawableMinus != null)
                                holder.imageExpander.setImageDrawable(drawableMinus);
                        } else {
                            crossFadeOut(holder.childRecyclerView, shortAnimationTime);
                            if (drawablePlus != null)
                                holder.imageExpander.setImageDrawable(drawablePlus);
                        }
                    }
                }
                if (selectedPos > 0){
                    mTargetId = 0; notifyItemChanged(selectedPos);
                }
                return;
            }
            if (mAllowSelect && (selectedPos != holder.getLayoutPosition())) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                mTargetId = 0;
                notifyItemChanged(selectedPos);
            }
        };
        Workout item = mList[position];
        boolean isTemplate = (item.parentID != null && item.parentID < 0);
        holder.itemView.setTag(Constants.UID_btn_recycle_item_select);
        //holder.itemView.setOnClickListener(myClicker);
        if (item.activityName.equals(EMPTY_LIST)){
            holder.text.setText(item.activityName);
        }else
            if (!isTemplate) {
                holder.text.setText(mRefTools.workoutShortText(item));
            }else {
                holder.text.setText(mRefTools.workoutTemplateText(item));
            }
        holder.text.setTag(Constants.UID_btn_recycle_item_select);
        holder.text.setOnClickListener(myClicker);
        holder.image.setTag(Constants.UID_btn_recycle_item_select);
        holder.image.setOnClickListener(myClicker);
        holder.buttonReport.setTag(item);
        holder.buttonReport.setOnClickListener(myClicker);
        holder.buttonStart.setOnClickListener(myClicker);
      //  holder.container.setOnClickListener(myClicker);
        holder.imageExpander.setOnClickListener(myClicker);
        if (item.packageName != null && item.packageName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)
        && !item.activityName.equals(EMPTY_LIST)) {
            if (isTemplate) {
                holder.buttonReport.setIcon(drawablePlay);
                holder.buttonReport.setVisibility(View.VISIBLE);
                //Drawable d = AppCompatResources.getDrawable()
                holder.buttonStart.setIcon(drawableDelete);
                holder.buttonStart.setVisibility(View.VISIBLE);
            }else
                holder.buttonReport.setVisibility(View.VISIBLE);
        } else {
            holder.buttonReport.setVisibility(View.GONE);
            holder.buttonStart.setVisibility(item.activityName.equals(EMPTY_LIST)? View.GONE:View.VISIBLE);
            holder.image.setVisibility(View.GONE);
            holder.imageExpander.setVisibility(View.GONE);
        }
        if (Utilities.isGymWorkout(item.activityID) || Utilities.isShooting(item.activityID)){
            holder.imageExpander.setImageDrawable(drawablePlus);
            holder.imageExpander.setVisibility(View.VISIBLE);
        }else
            holder.imageExpander.setVisibility(View.GONE);

      // holder.container.setBackgroundColor(mRefTools.getFitnessActivityColorById(item.activityID));
        int resId = (item.activityID > 0) ?  mRefTools.getFitnessActivityIconResById(item.activityID) : R.drawable.ic_outline_cancel_white ;
        holder.image.setImageResource(resId);
        ArrayList<WorkoutSet> sets = getSets(item._id);
        int setsSize = sets.size();
        holder.imageExpander.setTag(setsSize);
        if (mAllowSelect) // dont select the workout if sets exist!
            if (mTargetId == 0)
                holder.itemView.setSelected(selectedPos == position);
            else
                holder.itemView.setSelected(item._id == mTargetId);

        if (setsSize > 1){
            // Create a layout manager
            // to assign a layout
            // to the RecyclerView.
            holder.imageExpander.setVisibility(View.VISIBLE);
            holder.imageExpander.setImageDrawable(drawablePlus);
            // Here we have assigned the layout
            // as LinearLayout with vertical orientation
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(
                    holder.childRecyclerView.getContext(),
                    LinearLayoutManager.VERTICAL,
                    false);
            // Since this is a nested layout, so
            // to define how many child items
            // should be prefetched when the
            // child RecyclerView is nested
            // inside the parent RecyclerView,
            // we use the following method
            layoutManager.setInitialPrefetchItemCount(setsSize);
            WorkoutAdapter setsAdapter = new WorkoutAdapter(holder.childRecyclerView.getContext(),null,null,mUseKG);
            setsAdapter.setListType(false);
            setsAdapter.setEditMode(false);
            setsAdapter.setChildMode(1);
            setsAdapter.setAllowSelection(true);

            setsAdapter.setOnItemClickListener((view, viewModel, position1) -> this.onItemClickListener.onItemClick(view, viewModel));
            holder.childRecyclerView.setLayoutManager(layoutManager);
            holder.childRecyclerView.setAdapter(setsAdapter);
            holder.childRecyclerView.setRecycledViewPool(viewPool);
        }
        else
            holder.imageExpander.setVisibility(View.INVISIBLE);
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (mList != null) ?  mList.length : 0;
    }


    public void setAllowSelection(boolean bAllow){ this.mAllowSelect = bAllow; }
    public boolean isSelectable(){ return this.mAllowSelect; }

    public void setTargetId(long targetId){
        if (targetId > 0){
            mTargetId = targetId;
            notifyDataSetChanged();
        }else mTargetId = 0;
    }

    public void clearSelected(){
        mTargetId = 0;
        selectedPos = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void swapList(Workout[] newList){
        mList = newList;
        notifyDataSetChanged();
    }
    public void swapSets(WorkoutSet[] newList){
        mSetList = newList;
        //TODO: not sure if we need  notifyDataSetChanged();
    }
    public void setIsEmpty(){
        Workout w = new Workout();
        w.activityName = EMPTY_LIST ;
        mList = new Workout[1];
        mList[0] = w;
        notifyDataSetChanged();
    }
    public class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageView imageExpander;
        public ImageView image;
        public MaterialButton buttonReport;
        public MaterialButton buttonStart;
        public View container;
        public RecyclerView childRecyclerView;

        public ListViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            imageExpander = (ImageView) itemView.findViewById(R.id.image_expander);
            image = (ImageView) itemView.findViewById(R.id.image);
            container = itemView.findViewById(R.id.container);
            buttonReport = (MaterialButton) itemView.findViewById(R.id.list_report_button);
            buttonStart = (MaterialButton) itemView.findViewById(R.id.list_start_button);
            childRecyclerView = (RecyclerView) itemView.findViewById(R.id.child_recyclerview);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int UID, Object viewModel);
    }
}

