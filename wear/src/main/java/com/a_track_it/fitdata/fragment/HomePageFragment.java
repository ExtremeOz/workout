package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.user_model.UserPreferences;

/**
 * Activities that contain this fragment must implement the
 * {@link OnHomePageFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class HomePageFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "HomePageFragment";
    public static final String ARG_HOMEPAGE_IMAGE_ID = "homepage_image_id";
    public static final String ARG_HOMEPAGE_TEXT_ID = "homepage_text_id";
    public static final String ARG_HOMEPAGE_COLOR_ID = "homepage_color_id";
    public static final String ARG_HOMEPAGE_MESSAGE_ID = "homepage_message_id";
    public static final String ARG_HOMEPAGE_MSG_SOURCE_ID = "homepage_msg_source_id";

    public static final int MSG_HOME = 0;
    public static final int MSG_BMP = 1;
    public static final int MSG_STEP = 2;
    public static final int MSG_LOCATION = 3;

    private RelativeLayout mRelative;
    private ScrollView mScrollView;
    private ImageView mHomeImageView;
    private MessagesViewModel messagesViewModel;

    private TextView mTextView;
    private ColorFilter mImageViewColorFilter;
    private ColorFilter mBackgroundColorFilter;
    private TextView mMessageText;
    private int mParam1;
    private String mParam2;
    private int mParam3;
    private String mParamMessage;
    private int mMessageSource = 0;
    private OnHomePageFragmentInteractionListener mListener;

    public HomePageFragment() {
        // Required empty public constructor
    }
/*
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 int resource id
     * @param param2 String - heading
     * @param param3 int - color resource id
     * @return A new instance of fragment HomPageFragment.
     */

    public static HomePageFragment newInstance(int imageid, String text, int colorid, String message, int inSource) {
        final HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HOMEPAGE_IMAGE_ID, imageid);
        args.putString(ARG_HOMEPAGE_TEXT_ID, text);
        args.putInt(ARG_HOMEPAGE_COLOR_ID, colorid);
        args.putString(ARG_HOMEPAGE_MESSAGE_ID, message);
        args.putInt(ARG_HOMEPAGE_MSG_SOURCE_ID, inSource);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_HOMEPAGE_IMAGE_ID)) mParam1 = getArguments().getInt(ARG_HOMEPAGE_IMAGE_ID); else mParam1 = 0;
            if (getArguments().containsKey(ARG_HOMEPAGE_TEXT_ID)) mParam2 = getArguments().getString(ARG_HOMEPAGE_TEXT_ID); else mParam2 = getString(R.string.my_empty_string);
            if (getArguments().containsKey(ARG_HOMEPAGE_COLOR_ID)) mParam3 = getArguments().getInt(ARG_HOMEPAGE_COLOR_ID); else mParam3 = 0;
            if (getArguments().containsKey(ARG_HOMEPAGE_MESSAGE_ID)) mParamMessage = getArguments().getString(ARG_HOMEPAGE_MESSAGE_ID); else mParamMessage = getString(R.string.my_empty_string);
            if (getArguments().containsKey(ARG_HOMEPAGE_MSG_SOURCE_ID)) mMessageSource = getArguments().getInt(ARG_HOMEPAGE_MSG_SOURCE_ID); else mMessageSource = 0;
        }
        messagesViewModel = ViewModelProviders.of(requireActivity()).get(MessagesViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home_material, container, false);
       // mLinearTitle = rootView.findViewById(R.id.linear_title);
        mRelative = rootView.findViewById(R.id.home_relative);
        mScrollView = rootView.findViewById(R.id.observable_scrollview);

        mTextView = rootView.findViewById(R.id.home_text);
        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onHomePageFragmentLongClick(1);
                return false;
            }
        });
        mMessageText = rootView.findViewById(R.id.home_message);
        if ((mParamMessage != null) && (mParamMessage.length() > 0)) mMessageText.setText(mParamMessage);

        mHomeImageView = rootView.findViewById(R.id.home_image_view);
        mHomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHomePageFragmentInteraction(0, mParam1, mParam2, mParam3);
            }
        });
        mHomeImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onHomePageFragmentLongClick(0);
                return false;
            }
        });
        mImageViewColorFilter = mHomeImageView.getColorFilter();
        com.google.android.material.button.MaterialButton btn1 = rootView.findViewById(R.id.home_action1_image);
        mBackgroundColorFilter = btn1.getBackground().getColorFilter();
        rootView.findViewById(R.id.home_action1_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(1, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action2_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(2, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action3_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(3, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action4_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(4, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action5_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(5, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action6_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(6, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action7_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(7, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action8_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(8, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action9_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(9, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action10_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(10, mParam1, mParam2, mParam3);
            }
        });
        rootView.findViewById(R.id.home_action11_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(11, mParam1, mParam2, mParam3);
            }
        });
        messagesViewModel.CurrentMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_HOME) mMessageText.setText(s);
            }
        });

        messagesViewModel.getBpmMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_BMP) mMessageText.setText(s);
            }
        });
        messagesViewModel.getLocationMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_LOCATION) mMessageText.setText(s);
            }
        });
        messagesViewModel.getStepsMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_STEP) mMessageText.setText(s);
            }
        });
        mMessageText.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     String sPackageName = getActivity().getPackageName();
                     int resId = getResources().getIdentifier("ic_clock","drawable",sPackageName);
                     if (mMessageSource == MSG_LOCATION) mMessageSource = MSG_HOME; else mMessageSource++;
                     mListener.onHomePageFragmentInteraction(13, mMessageSource, mParam2, mParam3);
                     switch (mMessageSource){
                         case MSG_HOME:
                             resId = getResources().getIdentifier("ic_clock","drawable",sPackageName);
                             sPackageName = messagesViewModel.getMessage();
                             break;
                         case MSG_BMP:
                             resId = getResources().getIdentifier("ic_heart_solid","drawable",sPackageName);
                             sPackageName = messagesViewModel.getBpmMsg().getValue();
                             break;
                         case MSG_STEP:
                             resId = getResources().getIdentifier("ic_footsteps_silhouette_variant","drawable",sPackageName);
                             sPackageName = messagesViewModel.getStepsMsg().getValue();
                             break;
                         case MSG_LOCATION:
                             resId = getResources().getIdentifier("ic_placeholder","drawable",sPackageName);
                             sPackageName = messagesViewModel.getLocationMsg().getValue();
                             break;
                     }
                     final int resourceId = resId;
                     if (sPackageName == null) sPackageName = getString(R.string.requesting_data);
                     if (sPackageName.length() == 0) sPackageName = getString(R.string.requesting_data);
                     final String sMsg = sPackageName; // latest value from each msg type
                     new Handler(Looper.getMainLooper()).post(new Runnable() {
                         @Override
                         public void run() {
                             mMessageText.setCompoundDrawablesWithIntrinsicBounds(resourceId,0,0,0);
                             if ((sMsg != null) && (sMsg.length() > 0)) mMessageText.setText(sMsg);
                         }
                     });
                 }
             }
        );

        // image callbacks to MainActivity

/*        mTitleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onHomePageFragmentInteraction(12, mParam1, mParam2, mParam3);
            }
        });
        mTitleImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onHomePageFragmentLongClick(mParam1);
                return true;
            }
        });*/
        return rootView;
    }
/*    private void updateIcon(com.google.android.material.button.MaterialButton btn){
        if (btn.getIcon() != null) {
            Drawable icon = btn.getIcon();
            btn.setCompoundDrawables(null, icon, null, null);
        }
    }*/

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
       // Log.d(TAG, "HomepageFragment.onEnterAmbient() " + ambientDetails);
        int backColor = getResources().getColor(R.color.colorAmbientBackground, null);
        int foreColor = getResources().getColor(R.color.colorAmbientForeground, null);
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);

        mRelative.setBackgroundColor(backColor);
        //mLinearTitle.getBackground().setColorFilter(mBackgroundColorFilter);
        TextView textView =  mRelative.findViewById(R.id.home_text);
        if (textView != null){
            textView.setText(R.string.app_name);
            textView.setTextColor(foreColor);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,26);
            Paint textPaint = textView.getPaint();
            textPaint.setAntiAlias(false);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(2);
        }
        // Convert image to gray scale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        mHomeImageView.setColorFilter(filter);
        com.google.android.material.button.MaterialButton imageHome1 = mRelative.findViewById(R.id.home_action1_image);
        imageHome1.setBackgroundColor(backColor);
        imageHome1.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome2 = mRelative.findViewById(R.id.home_action2_image);
        buttonHome2.setBackgroundColor(backColor);
        buttonHome2.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome3 = mRelative.findViewById(R.id.home_action3_image);
        buttonHome3.setBackgroundColor(backColor);
        buttonHome3.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome4 = mRelative.findViewById(R.id.home_action4_image);
        buttonHome4.setBackgroundColor(backColor);
        buttonHome4.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome5 = mRelative.findViewById(R.id.home_action5_image);
        buttonHome5.setBackgroundColor(backColor);
        buttonHome5.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome6 = mRelative.findViewById(R.id.home_action6_image);
        buttonHome6.setBackgroundColor(backColor);
        buttonHome6.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome7 = mRelative.findViewById(R.id.home_action7_image);
        buttonHome7.setBackgroundColor(backColor);
        buttonHome7.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome8 = mRelative.findViewById(R.id.home_action8_image);
        buttonHome8.setBackgroundColor(backColor);
        buttonHome8.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome9 = mRelative.findViewById(R.id.home_action9_image);
        buttonHome9.setBackgroundColor(backColor);
        buttonHome9.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome10 = mRelative.findViewById(R.id.home_action10_image);
        buttonHome10.setBackgroundColor(backColor);
        buttonHome10.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome11 = mRelative.findViewById(R.id.home_action11_image);
        buttonHome11.setBackgroundColor(backColor);
        buttonHome11.setTextColor(foreColor);
        mMessageText.setTextColor(foreColor);
        mMessageText.getPaint().setAntiAlias(false);
        mTextView.setTextColor(foreColor);
        mTextView.getPaint().setAntiAlias(false);
    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "HomepageFragment.onExitAmbient()");
        Context context = getContext();
       
        int foreColor = ContextCompat.getColor(context, R.color.semiWhite);
        int backColor= ContextCompat.getColor(context, R.color.my_app_primary_color);
      //  Drawable bgDrawable = ContextCompat.getDrawable(context, R.drawable.bg_selector);
        mRelative.setBackgroundColor(backColor);

        //mLinearTitle.findViewById(R.id.home_text).setVisibility(View.VISIBLE);
        TextView textView =  mRelative.findViewById(R.id.home_text);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
            textView.setTextColor(foreColor);
            Paint textPaint = textView.getPaint();
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL);
        }

        mHomeImageView.setColorFilter(mImageViewColorFilter);
        mHomeImageView.setVisibility(View.VISIBLE);
        mMessageText.setTextColor(foreColor);
        mMessageText.getPaint().setAntiAlias(true);
        mTextView.setTextColor(foreColor);
        mTextView.getPaint().setAntiAlias(true);

        com.google.android.material.button.MaterialButton imageHome1 = mRelative.findViewById(R.id.home_action1_image);
        imageHome1.setBackgroundColor(backColor);
        imageHome1.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome2 = mRelative.findViewById(R.id.home_action2_image);
        buttonHome2.setBackgroundColor(backColor);
        buttonHome2.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome3 = mRelative.findViewById(R.id.home_action3_image);
        buttonHome3.setBackgroundColor(backColor);
        buttonHome3.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome4 = mRelative.findViewById(R.id.home_action4_image);
        buttonHome4.setBackgroundColor(backColor);
        buttonHome4.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome5 = mRelative.findViewById(R.id.home_action5_image);
        buttonHome5.setBackgroundColor(backColor);
        buttonHome5.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome6 = mRelative.findViewById(R.id.home_action6_image);
        buttonHome6.setBackgroundColor(backColor);
        buttonHome6.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome7 = mRelative.findViewById(R.id.home_action7_image);
        buttonHome7.setBackgroundColor(backColor);
        buttonHome7.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome8 = mRelative.findViewById(R.id.home_action8_image);
        buttonHome8.setBackgroundColor(backColor);
        buttonHome8.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome9 = mRelative.findViewById(R.id.home_action9_image);
        buttonHome9.setBackgroundColor(backColor);
        buttonHome9.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome10 = mRelative.findViewById(R.id.home_action10_image);
        buttonHome10.setBackgroundColor(backColor);
        buttonHome10.setTextColor(foreColor);
        com.google.android.material.button.MaterialButton buttonHome11 = mRelative.findViewById(R.id.home_action11_image);
        buttonHome11.setBackgroundColor(backColor);
        buttonHome11.setTextColor(foreColor);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomePageFragmentInteractionListener) {
            mListener = (OnHomePageFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnHomePageFragmentInteractionListener {
        void onHomePageFragmentInteraction(int src, int id, String text, int color);
        void onHomePageFragmentComplete(int id);
        void onHomePageFragmentLongClick(int id);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mParam1 = savedInstanceState.getInt(HomePageFragment.ARG_HOMEPAGE_IMAGE_ID);
            mParam2 = savedInstanceState.getString(HomePageFragment.ARG_HOMEPAGE_TEXT_ID);
            mParam3 = savedInstanceState.getInt(HomePageFragment.ARG_HOMEPAGE_COLOR_ID);
        }
        setHomeImageViewURI();
        mListener.onHomePageFragmentComplete(mParam1);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HomePageFragment.ARG_HOMEPAGE_IMAGE_ID, mParam1);
        outState.putString(HomePageFragment.ARG_HOMEPAGE_TEXT_ID, mParam2);
        outState.putInt(HomePageFragment.ARG_HOMEPAGE_COLOR_ID, mParam3);
    }

    public void setHomeImageViewURI(){
        final Context context = getContext();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String sURI = UserPreferences.getLastUserPhotoInternal(context);
                if ((mHomeImageView != null) && (sURI.length() > 0)){
                    Uri ourUri = Uri.parse(sURI);
                    mHomeImageView.setImageURI(ourUri);
                }else{
                      Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
                      mHomeImageView.setImageDrawable(drawable);
                }
                mHomeImageView.setVisibility(View.VISIBLE);
                Log.d(TAG, "scrolldown");
                mScrollView.fullScroll(mScrollView.FOCUS_DOWN);
            }
        });


    }
}
