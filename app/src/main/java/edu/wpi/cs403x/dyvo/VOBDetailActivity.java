package edu.wpi.cs403x.dyvo;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;


public class VOBDetailActivity extends ActionBarActivity {
    private Cursor vob;

    private VobsDbAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vobdetail);

        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
