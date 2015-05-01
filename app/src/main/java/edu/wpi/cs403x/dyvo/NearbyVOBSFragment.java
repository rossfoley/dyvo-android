package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.getbase.floatingactionbutton.AddFloatingActionButton;

import edu.wpi.cs403x.dyvo.api.DyvoServer;
import edu.wpi.cs403x.dyvo.api.DyvoServerAction;
import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NearbyVOBSFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NearbyVOBSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyVOBSFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_VOB_ID = "vob_id";

    private OnFragmentInteractionListener mListener;

    private ListView vobList;
    private SwipeRefreshLayout refreshLayout;
    private AddFloatingActionButton addTextVOBButton;

    private SharedPreferences settings;
    private VobViewCursorAdapter adapter;
    private VobsDbAdapter dbHelper;
    private DyvoServer server;

    public static NearbyVOBSFragment newInstance(int sectionNumber) {
        NearbyVOBSFragment fragment = new NearbyVOBSFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyVOBSFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby_vob, container, false);
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

        refreshVobDatabase();
        initializeActionMenu();
        initializeListView();

        refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_vobs);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshVobDatabase();
            }
        });
    }

    private void initializeListView() {
        // Initialize the database helper
        dbHelper = new VobsDbAdapter(getActivity());
        dbHelper.open();

        Cursor cursor = dbHelper.fetchNearbyVobs();

        adapter = new VobViewCursorAdapter(getActivity(), cursor, 0);

        vobList = (ListView) getView().findViewById(R.id.vob_list);
        vobList.setAdapter(adapter);

        vobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) vobList.getItemAtPosition(position);
            String vobId = cursor.getString(cursor.getColumnIndexOrThrow(VobsDbAdapter.KEY_ROW_ID));
            Intent intent = new Intent(getActivity(), VOBDetailActivity.class);
            intent.putExtra(EXTRA_VOB_ID, vobId);
            startActivity(intent);
            }
        });
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
        Location loc = LocationHelper.getInstance().getLocation();
        server.refreshVobDatabaseDistanceBased(loc.getLatitude(), loc.getLongitude(), .5, new DyvoServerAction() {
            @Override
            public void onSuccess() {
                adapter.changeCursor(dbHelper.fetchNearbyVobs());
                refreshLayout.setRefreshing(false);
            }
        });
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
