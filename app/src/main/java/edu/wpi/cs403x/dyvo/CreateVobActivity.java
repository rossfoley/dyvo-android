package edu.wpi.cs403x.dyvo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class CreateVobActivity extends ActionBarActivity {

    private Button dropBtn;
    private TextView textView;
    private SharedPreferences settings;
    private VobsDbAdapter dbHelper;
    private VobViewCursorAdapter adapter;
    private MapFragment mapFragment;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vob);

        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the Action Bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
           actionBar.setTitle(R.string.create_vob_activity_title);
        }

        // Initialize the settings
        settings = getSharedPreferences(FacebookLoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize the database helper and adapter
        dbHelper = new VobsDbAdapter(this);
        dbHelper.open();

        // Initialize TextView
        textView = (TextView) findViewById(R.id.create_vob_text_content);

        // Map Stuff
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.vob_create_map);
        googleMap = mapFragment.getMap();
        Location loc = LocationHelper.getInstance().getLocation();
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.setMyLocationEnabled(true);

        // Add Action to 'Drop' button
        dropBtn = (Button) findViewById(R.id.btn_drop);
        dropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushVobToDb();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_vob, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void pushVobToDb(){
        final Context ctx = this;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("content", textView.getText());
        params.put("longitude", LocationHelper.getInstance().getLocation().getLongitude());
        params.put("latitude", LocationHelper.getInstance().getLocation().getLatitude());
        client.addHeader("X-User-Email", settings.getString("email", ""));
        client.addHeader("X-User-Token", settings.getString("authentication_token", ""));

        client.post("http://dyvo.herokuapp.com/api/vobs", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject vob = response.getJSONObject("data");
                    String content = vob.getString("content");
                    String userId = vob.getString("user_id");
                    float longitude = (float) vob.getJSONArray("location").getDouble(0);
                    float latitude = (float) vob.getJSONArray("location").getDouble(1);
                    dbHelper.createVob(content, userId, longitude, latitude, "");
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String message = "Failed to refresh My VOBs.  Error code: " + statusCode;
                Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
