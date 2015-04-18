package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MyVOBsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private OnFragmentInteractionListener mListener;

    private ListView vobList;
    private FloatingActionsMenu actionMenu;
    private FloatingActionButton addTextVOBButton;
    private FloatingActionButton addPictureVOBButton;

    private SharedPreferences settings;
    private ArrayAdapter<String> adapter;

    public static MyVOBsFragment newInstance(int sectionNumber) {
        MyVOBsFragment fragment = new MyVOBsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MyVOBsFragment() {}

    @Override
    public void onStart() {
        super.onStart();

        // Initialize the settings
        settings = getActivity().getSharedPreferences(FacebookLoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

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

        // Initialize ListView
        vobList = (ListView) getView().findViewById(R.id.vob_list);
        String[] values = new String[] { "VOB A", "VOB B", "VOB C"};

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
        vobList.setAdapter(adapter);

        vobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String vobText = (String) vobList.getItemAtPosition(position);
                String alert = "You selected VOB #" + position + ": " + vobText;
                Toast.makeText(getActivity(), alert, Toast.LENGTH_SHORT).show();
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
