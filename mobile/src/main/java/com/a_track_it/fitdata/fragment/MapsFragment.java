package com.a_track_it.fitdata.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.ATrackItLatLng;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsFragment extends Fragment {
    public static final String LOG_TAG = MapsFragment.class.getSimpleName();
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    private GoogleMap mMap;
    private Location mLocation;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private List<ATrackItLatLng> listLatLng = new ArrayList<>();
    private Polyline mPolyline1;
    private Polyline mPolyline2;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMyLocationButtonClickListener(myLocationButtonClickListener);
            mMap.setOnMyLocationClickListener(myLocationClickListener);
            mMap.setOnCameraMoveListener(myCameraMoveListener);
            mMap.setOnPolylineClickListener(myPolylineClickListener);
            enableMyLocation();

        }
    };

    public MapsFragment() {

    }

    public static MapsFragment newInstance() {
        final MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    public void AddLatLng(ATrackItLatLng aLatLng) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
                List<LatLng> existingList = new ArrayList<>();
                if (mPolyline1 != null) {
                    existingList = mPolyline1.getPoints();
                    mPolyline1.remove();
                }
                LatLng myLatLng = new LatLng(aLatLng.Lat, aLatLng.Lng);
                existingList.add(myLatLng);
                polylineOptions.addAll(existingList);
                mPolyline1 = mMap.addPolyline(polylineOptions);
                mPolyline1.setStartCap(new RoundCap());
                mPolyline1.setWidth(12);
                mPolyline1.setClickable(true);
                mPolyline1.setColor(R.color.secondaryColor);
                mPolyline1.setEndCap(new SquareCap());
                if (myLatLng != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16.1498F));
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        mSessionViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        final androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.map_toolbar);
        if (mSavedStateViewModel.getLatitude() > 0) {
            mLocation = new Location(Constants.ATRACKIT_ATRACKIT_CLASS);
            mLocation.setLatitude(mSavedStateViewModel.getLatitude());
            mLocation.setLongitude(mSavedStateViewModel.getLongitude());
        }
        listLatLng = mSessionViewModel.get500ATrackItLatLngs();
        if (mSavedStateViewModel.isSessionSetup()) {
            mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if (mWorkout != null) {
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mWorkout.name.length() > 0)
                    toolbar.setTitle(getString(R.string.action_map) + Constants.ATRACKIT_SPACE + mWorkout.name);
                else
                    toolbar.setTitle(getString(R.string.action_map) + Constants.ATRACKIT_SPACE + mWorkout.activityName);
            }
        } else
            toolbar.setTitle(getString(R.string.nav_location));
        if (toolbar != null) {
            Drawable drawableUnChecked = AppCompatResources.getDrawable(getContext(), R.drawable.ic_close_white);
            Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(getContext(), R.color.secondaryTextColor));
            toolbar.setNavigationIcon(drawableUnChecked);
        }
        toolbar.setNavigationOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().remove(MapsFragment.this).commit();
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    GoogleMap.OnMyLocationClickListener myLocationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            //    Toast.makeText(MainActivity.this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        }
    };
    GoogleMap.OnMyLocationButtonClickListener myLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            //Toast.makeText(MainActivity.this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
            // Return false so that we don't consume the event and the default behavior still occurs
            // (the camera animates to the user's current position).
            return false;
        }
    };
    GoogleMap.OnCameraMoveListener myCameraMoveListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            CameraPosition position = mMap.getCameraPosition();
            //  Log.e(LOG_TAG, "camera move " + position.toString());
        }
    };
    GoogleMap.OnPolylineClickListener myPolylineClickListener = new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            //   Log.e(LOG_TAG, "polyline click " + polyline.toString());
        }
    };

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (mMap != null) {
            Log.i(LOG_TAG, "enableMyLocation");
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setPadding(16,60,16,60);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            LatLng lastLocation = null;
            if (listLatLng.size() > 0){
                PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
                int iCounter = 0;
                for (ATrackItLatLng trackItLatLng : listLatLng){
                    LatLng latlng = new LatLng(trackItLatLng.Lat, trackItLatLng.Lng);
                    if (iCounter == 0){
                        if (trackItLatLng.shortName.length() > 0)
                            mMap.addMarker(new MarkerOptions().position(latlng).title(trackItLatLng.shortName));
                        else
                            mMap.addMarker(new MarkerOptions().position(latlng).title("Start"));
                    }
                    polylineOptions.add(latlng);
                    iCounter++;
                    if ((iCounter > 1) && (iCounter == listLatLng.size())){
                        mMap.addMarker((new MarkerOptions().position(latlng).title("Finish")));
                        lastLocation = new LatLng(latlng.latitude, latlng.longitude);
                    }
                }
                mPolyline1 = mMap.addPolyline(polylineOptions);
                mPolyline1.setStartCap(new RoundCap());
                mPolyline1.setWidth(12);
                mPolyline1.setClickable(true);
                mPolyline1.setColor(R.color.secondaryColor);
                mPolyline1.setEndCap(new SquareCap());
                if (lastLocation != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 16.1498F));
            }else
                if (mLocation != null){
                    LatLng lastLoc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    Log.i(LOG_TAG,"have a location " + lastLocation.toString());
                    if (mSavedStateViewModel.getLocationAddress().length() > 0)
                        mMap.addMarker(new MarkerOptions().position(lastLoc)
                                .title(mSavedStateViewModel.getLocationAddress()));
                    else
                        mMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                                .title(Constants.ATRACKIT_SPACE));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 16.1498F));
                }else{
                    LatLng sydney = new LatLng(-16.92, 145.78);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Cairns"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13.2F));
                }
        }
    }
}
