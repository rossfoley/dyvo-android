package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class MyVOBsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private OnFragmentInteractionListener mListener;

    private ListView vobList;
    private FloatingActionsMenu actionMenu;
    private FloatingActionButton addTextVOBButton;
    private FloatingActionButton addPictureVOBButton;

    private SharedPreferences settings;
    private SimpleCursorAdapter adapter;
    private VobsDbAdapter dbHelper;

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

        refreshVobDatabase();
        initializeActionMenu();
        initializeListView();
    }

    private void initializeListView() {
        Cursor cursor = dbHelper.fetchAllVobs();
        String[] columns = new String[] {
                VobsDbAdapter.KEY_CONTENT,
                VobsDbAdapter.KEY_LONGITUDE,
                VobsDbAdapter.KEY_LATITUDE,
                VobsDbAdapter.KEY_USER_ID
        };
        int[] to = new int[] {
                R.id.content,
                R.id.longitude,
                R.id.latitude,
                R.id.user_id
        };

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.vob_info, cursor, columns, to, 0);
        vobList = (ListView) getView().findViewById(R.id.vob_list);
        vobList.setAdapter(adapter);

        vobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) vobList.getItemAtPosition(position);
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String message = "You selected VOB with content: " + content;
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "Add Text VOB Clicked!", Toast.LENGTH_SHORT).show();
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
        // TODO: this only refreshes the current user's VOBs.  Eventually, add another
        // database table to differentiate all VOBs and the current user's VOBs
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-User-Email", settings.getString("email", ""));
        client.addHeader("X-User-Token", settings.getString("authentication_token", ""));
        client.get(getActivity(), "http://dyvo.herokuapp.com/api/users/vobs", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getActivity(), "Successfully refreshed My VOBs", Toast.LENGTH_LONG).show();
                dbHelper.deleteAllVobs();
                try {
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject vob = data.getJSONObject(i);
                        String content = vob.getString("content");
                        String userId = vob.getString("user_id");
                        float longitude = (float) vob.getJSONArray("location").getDouble(0);
                        float latitude = (float) vob.getJSONArray("location").getDouble(1);
                        dbHelper.createVob(content, userId, longitude, latitude);
                    }
                    adapter.changeCursor(dbHelper.fetchAllVobs());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String message = "Failed to refresh My VOBs.  Error code: " + statusCode;
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
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
