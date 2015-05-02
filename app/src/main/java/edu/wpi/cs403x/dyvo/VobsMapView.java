package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import edu.wpi.cs403x.dyvo.api.DyvoServer;
import edu.wpi.cs403x.dyvo.api.DyvoServerAction;
import edu.wpi.cs403x.dyvo.api.FaceBookHelper;
import edu.wpi.cs403x.dyvo.api.FaceBookHelperAction;
import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VobsMapView.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VobsMapView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VobsMapView extends Fragment {

    public interface GetCursorFunction{
        public Cursor getCursor(VobsDbAdapter dbHelper);
    }

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_VOB_ID = "vob_id";

    private OnFragmentInteractionListener mListener;

    private AddFloatingActionButton addTextVOBButton;

    private GoogleMap googleMap;
    private SupportMapFragment fragment;
    private SharedPreferences settings;
    private VobViewCursorAdapter adapter;
    private VobsDbAdapter dbHelper;
    private DyvoServer server;

    private GetCursorFunction getCursorFunction;
    private boolean onlyNearVobs;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VobsMapView.
     */
    // TODO: Rename and change types and number of parameters
    public static VobsMapView newInstance(int sectionNumber, boolean onlyNearby, GetCursorFunction getCursorFunction) {
        VobsMapView fragment = new VobsMapView();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.getCursorFunction = getCursorFunction;
        fragment.onlyNearVobs = onlyNearby;
        return fragment;
    }

    private Cursor getCursor() {
        return getCursorFunction.getCursor(dbHelper);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the settings
        settings = getActivity().getSharedPreferences(FacebookLoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize the database helper
        dbHelper = new VobsDbAdapter(getActivity());
        dbHelper.open();

        // Initialize the server API
        server = new DyvoServer(
                settings.getString("email", ""),
                settings.getString("authentication_token", ""),
                dbHelper,
                getActivity());

        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.vob_map_main_frame);
        if (fragment == null){
            fragment = SupportMapFragment.newInstance().newInstance();
            fm.beginTransaction().replace(R.id.vob_map_main_frame, fragment).commit();
        }


        initializeActionMenu();


    }

    @Override
    public void onResume() {
        super.onResume();
        initializeListView();
        refreshVobDatabase();

    }


    private void initializeListView() {
        adapter = new VobViewCursorAdapter(getActivity(), getCursor(), 0);

       // mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.vob_map_view_map);
        googleMap = fragment.getMap();
        Location loc = LocationHelper.getInstance().getLocation();
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.setMyLocationEnabled(true);
    }

    private void initializeActionMenu() {
        // Initialize floating action button
        addTextVOBButton = (AddFloatingActionButton) getView().findViewById(R.id.add_vob_button);

        addTextVOBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateVobActivity.class));
            }
        });
    }

    private void refreshVobDatabase() {
        DyvoServerAction serverAction = new DyvoServerAction() {
            @Override
            public void onSuccess() {

                Cursor c = getCursor();
                final Map<Marker, String> markerToVobID = new HashMap<Marker, String>();
                googleMap.clear();

                c.moveToFirst();
                while (c.moveToNext()){

                    final double sLat = c.getDouble(c.getColumnIndex(VobsDbAdapter.KEY_LATITUDE));
                    final double sLong = c.getDouble(c.getColumnIndex(VobsDbAdapter.KEY_LONGITUDE));
                    final String fbStr = c.getString(c.getColumnIndex(VobsDbAdapter.KEY_USER_ID));
                    final String timeStr = c.getString(c.getColumnIndex(VobsDbAdapter.KEY_CREATED_AT));
                    final String vobId = c.getString(c.getColumnIndex(VobsDbAdapter.KEY_ROW_ID));
                    FaceBookHelper fb = new FaceBookHelper();

                    fb.requestFaceBookDetails(getActivity(), fbStr,
                            new FaceBookHelperAction() {
                                @Override
                                public void onSuccess(String name, Bitmap bitmap) {
                                    BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                    if (settings.getString("uid", "").equals(fbStr)){
                                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                                    }

                                    final Marker m = googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(sLat, sLong))
                                            .snippet(getTimeDisplay(timeStr))
                                            .title(name)
                                            .icon(icon));
                                    markerToVobID.put(m, vobId);
                                }

                                @Override
                                public void onFailure() {

                                }
                            });
                }

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String vid = markerToVobID.get(marker);
                        Intent intent = new Intent(getActivity(), VOBDetailActivity.class);
                        intent.putExtra(EXTRA_VOB_ID, vid);
                        startActivity(intent);
                    }
                });
            }
        };

        if (!onlyNearVobs){
            server.refreshVobDatabase(serverAction);
        } else {
            Location loc = LocationHelper.getInstance().getLocation();
            server.refreshVobDatabaseDistanceBased(loc.getLatitude(), loc.getLongitude(), 2, serverAction);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vobs_map_view, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Reimplement this later if necessary
//            mListener = (OnFragmentInteractionListener) activity;
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private String getTimeDisplay(String timeStr){

        DateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat formatter = new SimpleDateFormat("MMM d ''yy' at 'h:mm a");
        try {
            Date d = parser.parse(timeStr);
            int offset = TimeZone.getDefault().getOffset(new Date().getTime());
            d.setTime(d.getTime() + offset);

            String output = formatter.format(d);
            timeStr = output;
        } catch (ParseException e){
            e.printStackTrace();
        }
        return timeStr;
    }

}
