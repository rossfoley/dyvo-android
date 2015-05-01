package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_VOB_ID = "vob_id";

    private OnFragmentInteractionListener mListener;

    private SwipeRefreshLayout refreshLayout;
    private AddFloatingActionButton addTextVOBButton;

    private SharedPreferences settings;
    private VobViewCursorAdapter adapter;
    private VobsDbAdapter dbHelper;
    private DyvoServer server;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VobsMapView.
     */
    // TODO: Rename and change types and number of parameters
    public static VobsMapView newInstance(int sectionNumber) {
        VobsMapView fragment = new VobsMapView();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
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

    @Override
    public void onResume() {
        super.onResume();
        refreshVobDatabase();
    }


    private void initializeListView() {
        //adapter = new VobViewCursorAdapter(getActivity(), getCursor(), 0);

//        vobList = (ListView) getView().findViewById(R.id.vob_list);
//        vobList.setAdapter(adapter);
//
//        vobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Cursor cursor = (Cursor) vobList.getItemAtPosition(position);
//                String vobId = cursor.getString(cursor.getColumnIndexOrThrow(VobsDbAdapter.KEY_ROW_ID));
//                Intent intent = new Intent(getActivity(), VOBDetailActivity.class);
//                intent.putExtra(EXTRA_VOB_ID, vobId);
//                startActivity(intent);
//            }
//        });
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
//        server.refreshVobDatabase(new DyvoServerAction() {
//            @Override
//            public void onSuccess() {
//                adapter.changeCursor(getCursor());
//                refreshLayout.setRefreshing(false);
//            }
//        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_vobs, container, false);
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

}
