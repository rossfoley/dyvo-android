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
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import edu.wpi.cs403x.dyvo.api.DyvoServer;
import edu.wpi.cs403x.dyvo.api.DyvoServerAction;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class MyVOBsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_VOB_ID = "vob_id";

    private OnFragmentInteractionListener mListener;

    private ListView vobList;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionsMenu actionMenu;
    private FloatingActionButton addTextVOBButton;
    private FloatingActionButton addPictureVOBButton;

    private SharedPreferences settings;
    private VobViewCursorAdapter adapter;
    private VobsDbAdapter dbHelper;
    private DyvoServer server;

    public static MyVOBsFragment newInstance(int sectionNumber) {
        MyVOBsFragment fragment = new MyVOBsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MyVOBsFragment() {}

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
        adapter = new VobViewCursorAdapter(getActivity(), getCursor(), 0);

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
        // Initialize floating action menu items
        actionMenu = (FloatingActionsMenu) getView().findViewById(R.id.add_vob_menu);
        addTextVOBButton = (FloatingActionButton) getView().findViewById(R.id.add_text_vob);
        addPictureVOBButton = (FloatingActionButton) getView().findViewById(R.id.add_picture_vob);

        addTextVOBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateVobActivity.class);
                startActivity(intent);
                actionMenu.collapse();
            }
        });

        addPictureVOBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Add Picture VOB Clicked!", Toast.LENGTH_SHORT).show();
                actionMenu.collapse();
            }

        });
    }

    private void refreshVobDatabase() {
        server.refreshVobDatabase(new DyvoServerAction() {
            @Override
            public void onSuccess() {
                adapter.changeCursor(getCursor());
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private Cursor getCursor() {
        return dbHelper.fetchVobsByUser(settings.getString("uid", ""));
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
