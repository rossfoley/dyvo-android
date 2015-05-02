package edu.wpi.cs403x.dyvo;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.wpi.cs403x.dyvo.api.FaceBookHelper;
import edu.wpi.cs403x.dyvo.api.FaceBookHelperAction;
import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class VOBDetailActivity extends ActionBarActivity {
    private Cursor vob;

    private VobsDbAdapter dbHelper;
    private LatLng latLng;
    private TextView nameView;
    private TextView distanceView;
    private TextView timeView;
    private ImageView profileView;
    private Button centerBtn;
    private MapFragment mapFragment;
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vobdetail);

        // Enable the back button in the action bar
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("VOB Detail");
        }

        // Open a database connection
        dbHelper = new VobsDbAdapter(this);
        dbHelper.open();

        // Extract the VOB ID from the intent
        Intent intent = getIntent();
        String vobId = intent.getStringExtra(MyVOBsFragment.EXTRA_VOB_ID);
        vob = dbHelper.fetchVobById(vobId);

        // Display the VOB details
        TextView vobContent = (TextView) findViewById(R.id.vob_content);
        String content = vob.getString(vob.getColumnIndexOrThrow(VobsDbAdapter.KEY_CONTENT));
        vobContent.setText(content);

        // Get UI elements
        nameView = (TextView) findViewById(R.id.vob_detail_user_name);
        profileView = (ImageView) findViewById(R.id.vob_detail_profile);
        distanceView = (TextView) findViewById(R.id.vob_detail_distance);
        timeView = (TextView) findViewById(R.id.vob_detail_time);
        centerBtn = (Button) findViewById(R.id.vob_detail_center_btn);

        // Setup Map and Related info
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.vob_detail_map);
        googleMap = mapFragment.getMap();
        double sLat = vob.getDouble(vob.getColumnIndexOrThrow(VobsDbAdapter.KEY_LATITUDE));
        double sLong = vob.getDouble(vob.getColumnIndexOrThrow(VobsDbAdapter.KEY_LONGITUDE));
        latLng = new LatLng(sLat, sLong);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("VOB"));
        googleMap.setMyLocationEnabled(true);

        String distanceStr = LocationHelper.getInstance().getDistanceToAsText(latLng);
        distanceView.setText(distanceStr);

        String timeStr = vob.getString(vob.getColumnIndex(VobsDbAdapter.KEY_CREATED_AT));
        timeStr = getTimeDisplay(timeStr);
        timeView.setText(timeStr);

        // Do Facebook info
        String fbIdStr = vob.getString(vob.getColumnIndexOrThrow(VobsDbAdapter.KEY_USER_ID));
        FaceBookHelper fb = new FaceBookHelper();
        fb.requestFaceBookDetails(this, fbIdStr, new FaceBookHelperAction(){
            @Override
            public void onSuccess(String name, Bitmap bitmap) {
                nameView.setText(name);
                profileView.setImageBitmap(bitmap);
            }
            @Override
            public void onFailure() {
                //do nothing, no facebook available
            }
        });

        // Add center button behavour
        centerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vobdetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
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
