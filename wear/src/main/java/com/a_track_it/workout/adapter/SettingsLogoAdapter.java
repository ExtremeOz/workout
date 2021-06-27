package com.a_track_it.workout.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.model.UserPreferences;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SettingsLogoAdapter extends RecyclerView.Adapter<SettingsLogoAdapter.LogoViewHolder> {
    private List<String> mLabelList = new ArrayList<>();
    private List<Bitmap> mImageList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String mTargetLabel = Constants.ATRACKIT_EMPTY;
    private String mSelected_Source;
    private boolean bUseRounded;
    private Resources res;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class LogoViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView textView;

        public LogoViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.logo_imageView);
            textView = v.findViewById(R.id.logo_textView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SettingsLogoAdapter(Context context, UserPreferences userPreferences) {
        res = context.getResources();
        ContentResolver resolver = context.getContentResolver();
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
        bUseRounded = (userPreferences != null) ? userPreferences.getUseRoundedImage() : false;
        mImageList.add(src);
        mLabelList.add(Constants.LABEL_LOGO);
        mSelected_Source = userPreferences.getPrefStringByLabel(Constants.LABEL_LOGO_SOURCE);
        if (mSelected_Source.equals(Constants.LABEL_LOGO)){
            mTargetLabel = Constants.LABEL_LOGO; selectedPos = 0;
        }
        String sInternal = userPreferences.getPrefStringByLabel(Constants.LABEL_INT_FILE);
        if (sInternal.length() > 0) {
            try {
                Uri ourUri = Uri.parse(sInternal);
                Bitmap bitmap = Utilities.getMyBitmap(context, ourUri);
                if (bitmap != null) {
                    mImageList.add(bitmap);
                    mLabelList.add(Constants.LABEL_PROFILE);
                    if (mSelected_Source.equals(Constants.LABEL_INT_FILE)) {
                        mTargetLabel = Constants.LABEL_PROFILE;
                        selectedPos = (mImageList.size() - 1);
                    }
                }
               // Log.e(SettingsLogoAdapter.class.getSimpleName(), "loading Profile " + ourUri.getPath());
            } catch (Exception e) {
                if (e.equals(FileNotFoundException.class)){
                    userPreferences.setPrefStringByLabel(Constants.LABEL_INT_FILE,Constants.ATRACKIT_EMPTY); // cause a fix on reload homepage
                }
                Log.e(SettingsLogoAdapter.class.getSimpleName(), e.getMessage());
            }
        }
        sInternal = userPreferences.getPrefStringByLabel(Constants.LABEL_EXT_FILE);
        if (sInternal.length() > 0){
            try {
                Uri ourUri = Uri.parse(sInternal);
                Bitmap bitmap =  Utilities.getMyBitmap(context, ourUri);
                if (bitmap != null) {
                    mImageList.add(bitmap);
                    mLabelList.add(Constants.LABEL_FILE);
                    if (mSelected_Source.equals(Constants.LABEL_EXT_FILE)){
                        mTargetLabel = Constants.LABEL_FILE;
                        selectedPos = (mImageList.size()-1);
                    }
                }
               // Log.e(SettingsLogoAdapter.class.getSimpleName(), "loading sExternal " + ourUri.getPath());
            }catch (Exception e){
                if (e.equals(FileNotFoundException.class)){
                    userPreferences.setPrefStringByLabel(Constants.LABEL_EXT_FILE,Constants.ATRACKIT_EMPTY); // cause a fix on reload homepage
                }
                Log.e(SettingsLogoAdapter.class.getSimpleName(), e.getMessage());
            }
        }
        sInternal = userPreferences.getPrefStringByLabel(Constants.LABEL_CAMERA_FILE);
        if (sInternal.length() > 0){
            try {
                Uri ourUri = Uri.parse(sInternal);
                Bitmap bitmap = Utilities.getMyBitmap(context, ourUri);
                if (bitmap != null) {
                    mImageList.add(bitmap);
                    mLabelList.add("Camera");
                    if (mSelected_Source.equals(Constants.LABEL_CAMERA_FILE)){
                        mTargetLabel = Constants.LABEL_CAMERA;
                        selectedPos = (mImageList.size()-1);
                    }
                }
              //  Log.e(SettingsLogoAdapter.class.getSimpleName(), "loading sCamera " + ourUri.getPath());
            }catch (Exception e){
                if (e.equals(FileNotFoundException.class)){
                    userPreferences.setPrefStringByLabel(Constants.LABEL_CAMERA_FILE,Constants.ATRACKIT_EMPTY); // cause a fix on reload homepage
                }
                Log.e(SettingsLogoAdapter.class.getSimpleName(), e.getMessage());
            }
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public SettingsLogoAdapter.LogoViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_settings_logo_recycleitem, parent, false);
        LogoViewHolder vh = new LogoViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(LogoViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final View.OnClickListener myClicker = view -> {
            if (onItemClickListener != null) {
                if (position == selectedPos) // same position toggle use rounding
                    bUseRounded = !bUseRounded;

                onItemClickListener.onItemClick(view, bUseRounded, position);
                notifyItemChanged(selectedPos);
                selectedPos = position;
                mTargetLabel = Constants.ATRACKIT_EMPTY;
                notifyItemChanged(selectedPos);
            }
        };
        holder.textView.setText(mLabelList.get(position));
        if (!bUseRounded)
            holder.imageView.setImageBitmap(mImageList.get(position));
        else{
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, mImageList.get(position));
            dr.setCornerRadius(Math.max(mImageList.get(position).getWidth(), mImageList.get(position).getHeight()) / 2.0f);
            if (dr != null) holder.imageView.setImageDrawable(dr);
        }
        holder.textView.setOnClickListener(myClicker);
        holder.imageView.setOnClickListener(myClicker);
        holder.itemView.setOnClickListener(myClicker);
        if (mTargetLabel.equals(Constants.ATRACKIT_EMPTY))
            holder.itemView.setSelected(selectedPos == position);
        else {
            holder.itemView.setSelected(mTargetLabel.equals(mLabelList.get(position)));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mLabelList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, boolean useRounded, int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setUseRounded(boolean bUse){
        this.bUseRounded = bUse;
    }
    public void setTargetId(String targetId){
            mTargetLabel = targetId;
            notifyDataSetChanged();

    }
    public void clearSelected(){
        mTargetLabel = Constants.ATRACKIT_EMPTY;
        selectedPos = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }
}
