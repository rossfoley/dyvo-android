package edu.wpi.cs403x.dyvo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.drm.DrmStore;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int currentPosition;

    private boolean isMapView = false;

    private SharedPreferences settings;

    private VobsMapView.GetCursorFunction getMyVobFunction = new VobsMapView.GetCursorFunction() {
        @Override
        public Cursor getCursor(VobsDbAdapter dbHelper) {
            return dbHelper.fetchVobsByUser(settings.getString("uid", ""));
        }
    };
    private VobsMapView.GetCursorFunction getNearVobsFunction = new VobsMapView.GetCursorFunction() {
        @Override
        public Cursor getCursor(VobsDbAdapter dbHelper) {
            return dbHelper.fetchNearbyVobs();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }
        if (AccessToken.getCurrentAccessToken() == null) {
            startActivity(new Intent(MainActivity.this, FacebookLoginActivity.class));
        }
        setContentView(R.layout.activity_main);

        //Initialize helper singletons
        LocationHelper.getInstance().initialize(this);

        // Initialize the settings
        settings = getSharedPreferences(FacebookLoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Close the drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment newFragment = null;
        currentPosition = position;
        isMapView = false;
        invalidateOptionsMenu();
        switch (position) {
            case 0:
                // My VOBs section
                newFragment = MyVOBsFragment.newInstance(position + 1);
                break;
            case 1:
                // Nearby VOBs section
                newFragment = NearbyVOBSFragment.newInstance(position + 1);
                break;
            default:
                break;
        }

        fragmentManager.beginTransaction().replace(R.id.container, newFragment).commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            default:
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.clear();

        if (isMapView){
            menu.add(0, R.id.action_list_view, Menu.NONE, R.string.action_list_view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(0, R.id.action_map_view, Menu.NONE, R.string.action_map_view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(0, R.id.action_logout, Menu.NONE, R.string.action_logout);
        restoreActionBar();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment newFragment;
        switch (id){
            case R.id.action_logout:
                Intent intent = new Intent(MainActivity.this, FacebookLoginActivity.class);
                LoginManager.getInstance().logOut();
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;
            case R.id.action_map_view:
                if (currentPosition == 0){
                    newFragment = VobsMapView.newInstance(1, false, getMyVobFunction);
                } else newFragment = VobsMapView.newInstance(2, true, getNearVobsFunction);

                isMapView = true;
                invalidateOptionsMenu();
                fragmentManager.beginTransaction().replace(R.id.container, newFragment).commit();
                break;
            case R.id.action_list_view:
                if (currentPosition == 0) {
                    newFragment = MyVOBsFragment.newInstance(1);
                } else newFragment = NearbyVOBSFragment.newInstance(2);

                isMapView = false;
                invalidateOptionsMenu();
                fragmentManager.beginTransaction().replace(R.id.container, newFragment).commit();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
