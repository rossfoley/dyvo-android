package edu.wpi.cs403x.dyvo;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import edu.wpi.cs403x.dyvo.api.FaceBookHelper;
import edu.wpi.cs403x.dyvo.api.FaceBookHelperAction;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class VOBDetailActivity extends ActionBarActivity {
    private Cursor vob;

    private VobsDbAdapter dbHelper;

    private TextView nameView;
    private ImageView profileView;


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
}
